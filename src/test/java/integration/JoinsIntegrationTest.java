package integration;

import com.codurance.lightaccess.LightAccess;
import com.codurance.lightaccess.executables.DDLCommand;
import com.codurance.lightaccess.mapping.KeyValue;
import com.codurance.lightaccess.mapping.LAResultSet;
import com.codurance.lightaccess.mapping.OneToMany;
import integration.dtos.*;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

public class JoinsIntegrationTest {

    private static final String CREATE_USERS_TABLE = "CREATE TABLE users (userId integer PRIMARY KEY, name VARCHAR(255))";
    private static final String CREATE_WISHLISTS_TABLE = "CREATE TABLE wishlists (wishListId integer PRIMARY KEY, userId integer, name VARCHAR(255), creationDate TIMESTAMP)";
    private static final String CREATE_PRODUCTS_TABLE = "CREATE TABLE products (productId integer PRIMARY KEY, name VARCHAR(255), date TIMESTAMP)";
    private static final String CREATE_WISHLIST_PRODUCT_TABLE = "CREATE TABLE wishlist_product (id integer PRIMARY KEY, wishListId integer, productId integer)";

    private static final String ID_SEQUENCE = "id_sequence";
    private static final String CREATE_SEQUENCE_DDL = "CREATE SEQUENCE " + ID_SEQUENCE + " START WITH 1";
    private static final String DROP_ALL_OBJECTS = "DROP ALL OBJECTS";

    private static final String INSERT_USER_SQL = "insert into users (userId, name) values (?, ?)";
    private static final String INSERT_PRODUCT_SQL = "insert into products (productId, name, date) values (?, ?, ?)";
    private static final String INSERT_WISHLIST_SQL = "insert into wishlists (wishListId, userId, name, creationDate) values (?, ?, ?, ?)";
    private static final String INSERT_WISHLIST_PRODUCT_SQL = "insert into wishlist_product (id, wishListId, productId) values (?, ?, ?)";

    private static final String SELECT_WISHLISTS_PER_USER_SQL =
            "select u.userId, u.name, w.wishListId, w.userId, w.name, w.creationDate " +
                    "from users u " +
                    "left join wishlists w on u.userId = w.userId";
    private static final String SELECT_WISHLISTS_WITH_PRODUCTS_PER_USER_SQL =
            "select w.wishListId, w.userId, w.name, w.creationDate, p.productId, p.name, p.date " +
                    "from wishlists w " +
                    "left join wishlist_product wp on w.wishListId = wp.wishListId " +
                    "left join products p on wp.productId = p.productId " +
                    "where w.userId = ?";

    private static final User JOHN = new User(1, "John");
    private static final User SALLY = new User(2, "Sally");

    private static final Product MACBOOK_PRO = new Product(10, "MacBook Pro");
    private static final Product IPHONE = new Product(20, "iPhone");
    private static final Product IPAD = new Product(30, "iPad");

    private static final LocalDate TODAY = LocalDate.of(2017, 07, 27);

    private static final WishList XMAS_WISHLIST = new WishList(1, JOHN.id(), "Xmas", TODAY);
    private static final WishList BIRTHDAY_WISHLIST = new WishList(2, JOHN.id(), "Birthday", TODAY);
    private static final WishList FATHERS_DAY_WISHLIST = new WishList(3, JOHN.id(), "Father's day", TODAY);

    private static LightAccess lightAccess;
    private static JdbcConnectionPool jdbcConnectionPool;

    @BeforeClass
    public static void before_all_tests() throws SQLException {
        jdbcConnectionPool = JdbcConnectionPool.create("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "user", "password");
        lightAccess = new LightAccess(jdbcConnectionPool);
    }

    @Before
    public void before_each_test() throws Exception {
        lightAccess.executeDDLCommand(createTables());
    }

    @After
    public void after_each_test() throws Exception {
        lightAccess.executeDDLCommand(dropAllObjects());
    }

    @Test public void
    one_to_many_mapping_with_left_join_collected_to_a_list_of_dto() {
        givenWeHaveUsers(JOHN, SALLY);
        givenWeHaveProducts(MACBOOK_PRO, IPHONE, IPAD);
        givenWeHaveAWishListFor(JOHN, XMAS_WISHLIST, MACBOOK_PRO, IPHONE);
        givenWeHaveAWishListFor(JOHN, BIRTHDAY_WISHLIST, IPAD);

        OneToMany<User, WishList> wishListsPerUser = whenWeReturnAllWishListsPerUser();

        assertThat(wishListsPerUser.collect(this::toUserWithWishLists))
                .containsExactlyInAnyOrder(
                    new UserWithWishList(JOHN, asList(wishList(1, JOHN, "Xmas"),
                                                      wishList(2, JOHN, "Birthday"))),
                    new UserWithWishList(SALLY, emptyList()));
    }

    @Test public void
    collect_data_from_many_to_many_into_a_list_of_dto() {
        givenWeHaveUsers(JOHN, SALLY);
        givenWeHaveProducts(MACBOOK_PRO, IPHONE, IPAD);
        givenWeHaveAWishListFor(JOHN, XMAS_WISHLIST, MACBOOK_PRO, IPHONE);
        givenWeHaveAWishListFor(JOHN, BIRTHDAY_WISHLIST, IPAD);
        givenWeHaveAWishListFor(JOHN, FATHERS_DAY_WISHLIST);

        OneToMany<WishList, Product> userWishListWithProducts = whenWeReturnAllWishListsWithProductsBelongingTo(JOHN);

        assertThat(userWishListWithProducts.collect(this::toWishListProducts))
                .containsExactlyInAnyOrder(
                        wishListWithProducts(XMAS_WISHLIST, MACBOOK_PRO, IPHONE),
                        wishListWithProducts(BIRTHDAY_WISHLIST, IPAD),
                        wishListWithProducts(FATHERS_DAY_WISHLIST));
    }

    private WishListProduct wishListWithProducts(WishList wishList, Product... products) {
        return new WishListProduct(wishList, asList(products));
    }

    private WishListProduct toWishListProducts(WishList wishList, List<Product> products) {
        return new WishListProduct(wishList, products);
    }

    private OneToMany<WishList, Product> whenWeReturnAllWishListsWithProductsBelongingTo(User user) {
        return lightAccess.executeQuery((conn -> conn.prepareStatement(SELECT_WISHLISTS_WITH_PRODUCTS_PER_USER_SQL)
                                                        .withParam(user.id())
                                                        .executeQuery()
                                                        .normaliseOneToMany(this::toWithListWithProduct)));
    }

    private KeyValue<WishList, Optional<Product>> toWithListWithProduct(LAResultSet rs) {
        WishList wishList = new WishList(rs.getInt(1),
                                            rs.getInt(2),
                                            rs.getString(3),
                                            rs.getLocalDate(4));
        Optional<Product> product = Optional.ofNullable((rs.getInt(5) > 0)
                                        ? new Product(rs.getInt(5), rs.getString(6), rs.getLocalDate(7))
                                        : null);
        return new KeyValue<>(wishList, product);
    }

    private OneToMany<User, WishList> whenWeReturnAllWishListsWithProductsPerUser() {
        throw new UnsupportedOperationException();
    }

    private WishList wishList(int id, User user, String name, Product... products) {
        return new WishList(id, user.id(), name, TODAY);
    }

    private List<WishList> allWishLists() {
        return lightAccess.executeQuery(c -> c.prepareStatement("select * from wishlists")
                                                .executeQuery()
                                                .mapResults(rs -> new WishList(rs.getInt(1),
                                                                                rs.getInt(2),
                                                                                rs.getString(3),
                                                                                rs.getLocalDate(4))));
    }

    private List<Product> allProducts() {
        return lightAccess.executeQuery(c -> c.prepareStatement("select * from products")
                                                .executeQuery()
                                                .mapResults(rs -> new Product(rs.getInt(1), rs.getString(2))));
    }

    private List<User> allUsers() {
        return lightAccess.executeQuery(c -> c.prepareStatement("select * from users")
                                                .executeQuery()
                                                .mapResults(result -> new User(result.getInt(1), result.getString(2))));
    }

    private UserWithWishList toUserWithWishLists(User user, List<WishList> wishLists) {
        return new UserWithWishList(user, wishLists);
    }

    private OneToMany<User, WishList> whenWeReturnAllWishListsPerUser() {
        return lightAccess.executeQuery((conn -> conn.prepareStatement(SELECT_WISHLISTS_PER_USER_SQL)
                                                        .executeQuery()
                                                        .normaliseOneToMany(this::toUserWishList)));
    }

    private KeyValue<User, Optional<WishList>> toUserWishList(LAResultSet laResultSet) {
        User user = new User(laResultSet.getInt(1), laResultSet.getString(2));

        Optional<WishList> wishList = Optional.ofNullable((laResultSet.getInt(3) > 0)
                                                    ? new WishList(laResultSet.getInt(3),
                                                                    laResultSet.getInt(4),
                                                                    laResultSet.getString(5),
                                                                    laResultSet.getLocalDate(6))
                                                    : null);
        return new KeyValue<>(user, wishList);
    }

    private void givenWeHaveAWishListFor(User user, WishList wishList, Product... products) {
        insertWithList(user, wishList);
        addProductsToWishList(wishList, products);
    }

    private void insertWithList(User user, WishList wishList) {
        lightAccess.executeCommand(conn -> conn.prepareStatement(INSERT_WISHLIST_SQL)
                                                .withParam(wishList.id())
                                                .withParam(user.id())
                                                .withParam(wishList.name())
                                                .withParam(TODAY)
                                                .executeUpdate());
    }

    private void addProductsToWishList(WishList wishList, Product[] products) {
        lightAccess.executeCommand((conn -> asList(products).forEach(product -> {
            int id = lightAccess.nextId(ID_SEQUENCE);
            conn.prepareStatement(INSERT_WISHLIST_PRODUCT_SQL)
                    .withParam(id)
                    .withParam(wishList.id())
                    .withParam(product.id())
                    .executeUpdate();
        })));
    }

    private void givenWeHaveUsers(User... users) {
        lightAccess.executeCommand((conn -> asList(users).forEach(
                user -> conn.prepareStatement(INSERT_USER_SQL)
                            .withParam(user.id())
                            .withParam(user.name())
                            .executeUpdate())));
    }

    private void givenWeHaveProducts(Product... products) {
        lightAccess.executeCommand((conn -> asList(products).forEach(
                product -> conn.prepareStatement(INSERT_PRODUCT_SQL)
                        .withParam(product.id())
                        .withParam(product.name())
                        .withParam(product.date())
                        .executeUpdate())));
    }

    private DDLCommand createTables() {
        return (conn) -> {
            conn.statement(CREATE_SEQUENCE_DDL).execute();
            conn.statement(CREATE_USERS_TABLE).execute();
            conn.statement(CREATE_WISHLISTS_TABLE).execute();
            conn.statement(CREATE_PRODUCTS_TABLE).execute();
            conn.statement(CREATE_WISHLIST_PRODUCT_TABLE).execute();
        };
    }

    private DDLCommand dropAllObjects() {
        return (conn) -> conn.statement(DROP_ALL_OBJECTS).execute();
    }

}
