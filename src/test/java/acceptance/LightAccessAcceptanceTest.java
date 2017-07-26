package acceptance;

import com.codurance.lightaccess.LightAccessExecutor;
import com.codurance.lightaccess.LightAccessExecutor.SQLCommand;
import com.codurance.lightaccess.LightAccessExecutor.SQLQuery;
import com.codurance.lightaccess.PGResultSet;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.assertj.core.api.Assertions.assertThat;

public class LightAccessAcceptanceTest {

    private static final String CREATE_ENTITIES_TABLE = "CREATE TABLE entities (id VARCHAR(255) PRIMARY KEY, name VARCHAR(255))";
    private static final String DROP_ALL_OBJECTS = "DROP ALL OBJECTS";

    private static final String INSERT_ENTITY_SQL = "insert into entities (id, name) values (?, ?)";
    private static final String DELETE_ENTITIES_SQL = "delete from entities";
    private static final String DELETE_ENTITY_SQL = "delete from entities where id = ?";
    private static final String UPDATE_ENTITY_NAME_SQL = "update entities set name = ? where id = ?";
    private static final String SELECT_ALL_ENTITIES_SQL = "select * from entities";
    private static final String SELECT_ENTITY_BY_ID_SQL = "select * from entities where id = ?";

    private static Entity ENTITY_ONE = new Entity(1, "Entity 1");
    private static Entity ENTITY_TWO = new Entity(2, "Entity 2");


    private static DataSource dataSource;
    private static LightAccessExecutor lightAccessExecutor;
    private static JdbcConnectionPool jdbcConnectionPool;

    @BeforeClass
    public static void before_all_tests() throws SQLException {
        jdbcConnectionPool = JdbcConnectionPool.create("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "user", "password");
        dataSource = jdbcConnectionPool;
        lightAccessExecutor = new LightAccessExecutor(dataSource);

    }

    @Before
    public void before_each_test() throws Exception {
        lightAccessExecutor.executeUpdate(createEntitiesTable());
    }

    @After
    public void after_each_test() throws Exception {
        lightAccessExecutor.executeUpdate(dropAllObjects());
    }

    @Test public void
    close_connection_after_executing_a_query() {
        lightAccessExecutor.execute((conn) -> SELECT_ALL_ENTITIES_SQL);

        assertThat(jdbcConnectionPool.getActiveConnections()).isEqualTo(0);
    }

    @Test public void
    close_connection_after_executing_a_command() {
        lightAccessExecutor.executeUpdate(deleteEntities());

        assertThat(jdbcConnectionPool.getActiveConnections()).isEqualTo(0);
    }

    @Test public void
    create_and_retrieve_entities() {
        lightAccessExecutor.executeUpdate(insert(ENTITY_ONE));
        lightAccessExecutor.executeUpdate(insert(ENTITY_TWO));

        List<Entity> entities = lightAccessExecutor.execute(retrieveAllEntities());

        assertThat(entities).containsExactlyInAnyOrder(ENTITY_ONE, ENTITY_TWO);
    }

    @Test public void
    retrieve_a_single_entity() {
        lightAccessExecutor.executeUpdate(insert(ENTITY_ONE));
        lightAccessExecutor.executeUpdate(insert(ENTITY_TWO));

        Optional<Entity> entity = lightAccessExecutor.execute(retrieveEntityWithId(ENTITY_TWO.id));

        assertThat(entity.get()).isEqualTo(ENTITY_TWO);
    }

    @Test public void
    retrieve_an_empty_optional_when_entity_is_not_found() {
        lightAccessExecutor.executeUpdate(insert(ENTITY_ONE));

        Optional<Entity> entity = lightAccessExecutor.execute(retrieveEntityWithId(ENTITY_TWO.id));

        assertThat(entity.isPresent()).isEqualTo(false);
    }

    @Test public void
    delete_entity() {
        lightAccessExecutor.executeUpdate(insert(ENTITY_ONE));
        lightAccessExecutor.executeUpdate(insert(ENTITY_TWO));

        lightAccessExecutor.executeUpdate(delete(ENTITY_ONE));

        List<Entity> entities = lightAccessExecutor.execute(retrieveAllEntities());
        assertThat(entities).containsExactlyInAnyOrder(ENTITY_TWO);
    }

    @Test public void
    update_entity() {
        lightAccessExecutor.executeUpdate(insert(ENTITY_ONE));
        lightAccessExecutor.executeUpdate(updateEntityName(1, "Another name"));

        Optional<Entity> entity = lightAccessExecutor.execute(retrieveEntityWithId(ENTITY_ONE.id));

        assertThat(entity.get()).isEqualTo(new Entity(1, "Another name"));
    }

    @Test
    public void return_next_id_using_sequence() throws Exception {
        createSequence("id_sequence", "10");

        Integer firstId = lightAccessExecutor.nextId("id_sequence", (x) -> x);
        Integer secondId = lightAccessExecutor.nextId("id_sequence", (x) -> x);

        assertThat(firstId).isEqualTo(10);
        assertThat(secondId).isEqualTo(11);
    }

    @Test
    public void return_next_id_converted_to_a_different_type() throws Exception {
        createSequence("id_sequence", "10");

        String firstId = lightAccessExecutor.nextId("id_sequence", Object::toString);
        EntityID secondId = lightAccessExecutor.nextId("id_sequence", (x) -> new EntityID(x));

        assertThat(firstId).isEqualTo("10");
        assertThat(secondId).isEqualTo(new EntityID(11));
    }

    private void createSequence(String sequenceName, String initialValue) throws SQLException {
        Connection connection = dataSource.getConnection();
        connection.createStatement().executeUpdate(format("CREATE SEQUENCE %s START WITH %s", sequenceName, initialValue));
        connection.close();
    }

    private SQLCommand updateEntityName(int id, String name) {
        return conn -> conn.prepareStatement(UPDATE_ENTITY_NAME_SQL)
                            .withParam(name)
                            .withParam(id)
                            .executeUpdate();
    }

    private SQLCommand delete(Entity entity) {
        return conn -> conn.prepareStatement(DELETE_ENTITY_SQL)
                            .withParam(entity.id)
                            .executeUpdate();
    }

    private SQLCommand insert(Entity entity) {
        return conn -> conn.prepareStatement(INSERT_ENTITY_SQL)
                                                .withParam(entity.id)
                                                .withParam(entity.name)
                                                .executeUpdate();
    }

    private SQLQuery<Optional<Entity>> retrieveEntityWithId(int id) {
        return conn -> conn.prepareStatement(SELECT_ENTITY_BY_ID_SQL)
                            .withParam(id)
                            .executeQuery()
                            .onlyResult(this::toEntity);
    }


    private SQLQuery<List<Entity>> retrieveAllEntities() {
        return conn -> conn.prepareStatement(SELECT_ALL_ENTITIES_SQL)
                            .executeQuery()
                            .mapResults(this::toEntity);
    }

    private SQLCommand deleteEntities() {
        return conn -> conn.prepareStatement(DELETE_ENTITIES_SQL).executeUpdate();
    }

    private Entity toEntity(PGResultSet pgResultSet) {
        return new Entity(pgResultSet.getInt(1), pgResultSet.getString(2));
    }

    private SQLCommand createEntitiesTable() {
        return (conn) -> conn.prepareStatement(CREATE_ENTITIES_TABLE).executeUpdate();
    }

    private SQLCommand dropAllObjects() {
        return (conn) -> conn.prepareStatement(DROP_ALL_OBJECTS).executeUpdate();
    }
    
    private static class Entity {
        private int id;
        private String name;

        Entity(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            return reflectionEquals(this, o);
        }

        @Override
        public int hashCode() {
            return reflectionHashCode(this);
        }

        @Override
        public String toString() {
            return "Entity{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    private static class EntityID {
        private int id;

        EntityID(int id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object other) {
            return reflectionEquals(this, other);
        }

        @Override
        public int hashCode() {
            return reflectionHashCode(this);
        }
    }
}