package integration;

import com.codurance.lightaccess.LightAccess;
import com.codurance.lightaccess.executables.DDLCommand;
import com.codurance.lightaccess.executables.SQLCommand;
import com.codurance.lightaccess.executables.SQLQuery;
import com.codurance.lightaccess.mapping.LAResultSet;
import integration.dtos.Product;
import integration.dtos.ProductID;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

public class LightAccessIntegrationTest {

    private static final String CREATE_PRODUCTS_TABLE = "CREATE TABLE products (id VARCHAR(255) PRIMARY KEY, name VARCHAR(255), date TIMESTAMP)";
    private static final String CREATE_SEQUENCE_DDL = "CREATE SEQUENCE %s START WITH %s";
    private static final String DROP_ALL_OBJECTS = "DROP ALL OBJECTS";

    private static final String INSERT_PRODUCT_SQL = "insert into products (id, name, date) values (?, ?, ?)";
    private static final String DELETE_PRODUCTS_SQL = "delete from products";
    private static final String DELETE_PRODUCT_SQL = "delete from products where id = ?";
    private static final String UPDATE_PRODUCT_NAME_SQL = "update products set name = ? where id = ?";
    private static final String SELECT_ALL_PRODUCTS_SQL = "select * from products";
    private static final String SELECT_PRODUCT_BY_ID_SQL = "select * from products where id = ?";

    private static final LocalDate TODAY = LocalDate.of(2017, 07, 27);
    private static final LocalDate YESTERDAY = LocalDate.of(2017, 07, 26);

    private static Product PRODUCT_ONE = new Product(1, "Product 1", YESTERDAY);
    private static Product PRODUCT_TWO = new Product(2, "Product 2", TODAY);

    private static LightAccess lightAccess;
    private static JdbcConnectionPool jdbcConnectionPool;

    @BeforeClass
    public static void before_all_tests() throws SQLException {
        jdbcConnectionPool = JdbcConnectionPool.create("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "user", "password");
        lightAccess = new LightAccess(jdbcConnectionPool);
    }

    @Before
    public void before_each_test() throws Exception {
        lightAccess.executeDDLCommand(createProductsTable());
    }

    @After
    public void after_each_test() throws Exception {
        lightAccess.executeDDLCommand(dropAllObjects());
    }

    @Test public void
    close_connection_after_executing_a_query() {
        lightAccess.executeQuery((conn) -> SELECT_ALL_PRODUCTS_SQL);

        assertThat(jdbcConnectionPool.getActiveConnections()).isEqualTo(0);
    }

    @Test public void
    close_connection_after_executing_a_command() {
        lightAccess.executeCommand(deleteProducts());

        assertThat(jdbcConnectionPool.getActiveConnections()).isEqualTo(0);
    }

    @Test public void
    close_connection_after_executing_a_DDL_command() {
        lightAccess.executeDDLCommand(dropAllObjects());

        assertThat(jdbcConnectionPool.getActiveConnections()).isEqualTo(0);
    }

    @Test public void
    insert_records() {
        lightAccess.executeCommand(insert(PRODUCT_ONE));
        lightAccess.executeCommand(insert(PRODUCT_TWO));

        List<Product> products = lightAccess.executeQuery(retrieveAllProducts());

        assertThat(products).containsExactlyInAnyOrder(PRODUCT_ONE, PRODUCT_TWO);
    }

    @Test public void
    retrieve_a_single_record_and_map_it_to_an_object() {
        lightAccess.executeCommand(insert(PRODUCT_ONE));
        lightAccess.executeCommand(insert(PRODUCT_TWO));

        Optional<Product> product = lightAccess.executeQuery(retrieveProductWithId(PRODUCT_TWO.id()));

        assertThat(product.get()).isEqualTo(PRODUCT_TWO);
    }

    @Test public void
    retrieve_an_empty_optional_when_not_record_is_found() {
        lightAccess.executeCommand(insert(PRODUCT_ONE));

        Optional<Product> product = lightAccess.executeQuery(retrieveProductWithId(PRODUCT_TWO.id()));

        assertThat(product.isPresent()).isEqualTo(false);
    }

    @Test public void
    delete_a_record() {
        lightAccess.executeCommand(insert(PRODUCT_ONE));
        lightAccess.executeCommand(insert(PRODUCT_TWO));

        lightAccess.executeCommand(delete(PRODUCT_ONE));

        List<Product> products = lightAccess.executeQuery(retrieveAllProducts());
        assertThat(products).containsExactlyInAnyOrder(PRODUCT_TWO);
    }

    @Test public void
    update_a_record() {
        lightAccess.executeCommand(insert(PRODUCT_ONE));
        lightAccess.executeCommand(updateProductName(1, "Another name"));

        Optional<Product> product = lightAccess.executeQuery(retrieveProductWithId(PRODUCT_ONE.id()));

        assertThat(product.get()).isEqualTo(new Product(PRODUCT_ONE.id(), "Another name", PRODUCT_ONE.date()));
    }

    @Test public void
    return_next_integer_id_using_sequence() throws Exception {
        lightAccess.executeDDLCommand(createSequence("id_sequence", "10"));

        int firstId = lightAccess.nextId("id_sequence");
        int secondId = lightAccess.nextId("id_sequence");

        assertThat(firstId).isEqualTo(10);
        assertThat(secondId).isEqualTo(11);
    }

    @Test public void
    return_next_id_converted_to_a_different_type() throws Exception {
        lightAccess.executeDDLCommand(createSequence("id_sequence", "10"));

        String firstId = lightAccess.nextId("id_sequence", Object::toString);
        ProductID secondId = lightAccess.nextId("id_sequence", ProductID::new);

        assertThat(firstId).isEqualTo("10");
        assertThat(secondId).isEqualTo(new ProductID(11));
    }

    private SQLCommand updateProductName(int id, String name) {
        return conn -> conn.prepareStatement(UPDATE_PRODUCT_NAME_SQL)
                            .withParam(name)
                            .withParam(id)
                            .executeUpdate();
    }

    private SQLCommand delete(Product product) {
        return conn -> conn.prepareStatement(DELETE_PRODUCT_SQL)
                            .withParam(product.id())
                            .executeUpdate();
    }

    private SQLCommand insert(Product product) {
        return conn -> conn.prepareStatement(INSERT_PRODUCT_SQL)
                            .withParam(product.id())
                            .withParam(product.name())
                            .withParam(product.date())
                            .executeUpdate();
    }

    private SQLQuery<Optional<Product>> retrieveProductWithId(int id) {
        return conn -> conn.prepareStatement(SELECT_PRODUCT_BY_ID_SQL)
                            .withParam(id)
                            .executeQuery()
                            .onlyResult(this::toProduct);
    }

    private SQLQuery<List<Product>> retrieveAllProducts() {
        return conn -> conn.prepareStatement(SELECT_ALL_PRODUCTS_SQL)
                            .executeQuery()
                            .mapResults(this::toProduct);
    }

    private SQLCommand deleteProducts() {
        return conn -> conn.prepareStatement(DELETE_PRODUCTS_SQL).executeUpdate();
    }

    private Product toProduct(LAResultSet laResultSet) {
        return new Product(laResultSet.getInt(1),
                          laResultSet.getString(2),
                          laResultSet.getLocalDate(3));
    }

    private DDLCommand createSequence(String sequenceName, String initialValue) {
        String id_sequence = format(CREATE_SEQUENCE_DDL, sequenceName, initialValue);
        return (conn) -> conn.statement(id_sequence).execute();
    }

    private DDLCommand createProductsTable() {
        return (conn) -> conn.statement(CREATE_PRODUCTS_TABLE).execute();
    }

    private DDLCommand dropAllObjects() {
        return (conn) -> conn.statement(DROP_ALL_OBJECTS).execute();
    }

}
