package acceptance;

import com.codurance.lightaccess.PGConnection;
import com.codurance.lightaccess.PGResultSet;
import com.codurance.lightaccess.PGSQLExecutor;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.IsEqual.equalTo;

public class AcceptanceTest {

    private static final String CREATE_ENTITIES_TABLE = "CREATE TABLE entities (id VARCHAR(255) PRIMARY KEY, name VARCHAR(255))";

    private static final String INSERT_ENTITY_SQL = "insert into entities (id, name) values (?, ?)";
    private static final String UPDATE_ENTITY_SQL = "update entities set name = ? where id = ?";
    private static final String SELECT_ALL_ENTITIES_SQL = "select * from entities";

    private DataSource dataSource;
    private PGSQLExecutor executor;
    private JdbcConnectionPool jdbcConnectionPool;

    @Before
    public void setUp() throws Exception {
        jdbcConnectionPool = JdbcConnectionPool.create("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "user", "password");
        this.dataSource = jdbcConnectionPool;
        executor = new PGSQLExecutor(dataSource);

        Connection connection = dataSource.getConnection();
        connection.createStatement().executeUpdate(CREATE_ENTITIES_TABLE);
        connection.close();
    }

    @After
    public void tearDown() throws Exception {
        Connection connection = jdbcConnectionPool.getConnection();
        connection.prepareCall("DROP ALL OBJECTS").execute();
        connection.close();
    }

    @Test
    public void executeQueryAndMapResultsThenCloseConnection() throws Exception {
        executor.execute((conn) -> create(conn, new DummyEntity(2, "some name")));
        executor.execute((conn) -> create(conn, new DummyEntity(4, "other name")));

        List<DummyEntity> list = executor.execute((conn) -> fetch(SELECT_ALL_ENTITIES_SQL, conn));

        assertThat(list, containsInAnyOrder(new DummyEntity(2, "some name"), new DummyEntity(4, "other name")));
        assertThat(jdbcConnectionPool.getActiveConnections(), equalTo(0));
    }

    @Test
    public void executeUpdateQueryThenCloseConnection() throws Exception {
        executor.execute((conn) -> create(conn, new DummyEntity(2, "some name")));

        executor.executeUpdate((conn) -> updateName(conn, 2, "updated name"));

        List<DummyEntity> list = executor.execute((conn) -> fetch(SELECT_ALL_ENTITIES_SQL, conn));
        assertThat(list, containsInAnyOrder(new DummyEntity(2, "updated name")));
        assertThat(jdbcConnectionPool.getActiveConnections(), equalTo(0));
    }

    @Test
    public void queryNextIdThenCloseConnection() throws Exception {
        Connection connection = dataSource.getConnection();
        connection.createStatement().executeUpdate("CREATE SEQUENCE aSequence START WITH 2");
        connection.close();

        Integer id = executor.nextId("aSequence", (x) -> x);

        assertThat(id, equalTo(2));
        assertThat(jdbcConnectionPool.getActiveConnections(), equalTo(0));
    }


    private List<DummyEntity> fetch(String ALL_ENTITIES_SQL, PGConnection conn) {
        return conn.prepareStatement(ALL_ENTITIES_SQL)
                .executeQuery()
                .mapResults(this::actionsFrom);
    }

    private DummyEntity create(PGConnection conn, DummyEntity entity) {
        conn.prepareStatement(INSERT_ENTITY_SQL)
                .withParam(entity.id)
                .withParam(entity.name)
                .executeUpdate();
        return entity;
    }

    private void updateName(PGConnection conn, int id, String name) {
        conn.prepareStatement(UPDATE_ENTITY_SQL)
                .withParam(name)
                .withParam(id)
                .executeUpdate();
    }

    private DummyEntity actionsFrom(PGResultSet pgResultSet) {
        return new DummyEntity(pgResultSet.getInt(1), pgResultSet.getString(2));
    }

    private class DummyEntity {
        private int id;
        private String name;

        public DummyEntity(int id, String name) {

            this.id = id;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DummyEntity that = (DummyEntity) o;

            if (id != that.id) return false;
            return name != null ? name.equals(that.name) : that.name == null;
        }

        @Override
        public int hashCode() {
            int result = id;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "DummyEntity{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
}
