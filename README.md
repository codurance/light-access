[![CircleCI](https://circleci.com/gh/codurance/light-access/tree/master.svg?style=shield)](https://circleci.com/gh/codurance/light-access/tree/master)

Light Access
============

Light Access is a small library on top of [JDBC][1].

# Who should use this library?

This library is for Java developers who:
 
* Want full control of their code.
* Want a nice and fluid API on top of JDBC, using lambdas.
* Prefer to use non-intrusive small libraries instead of intrusive ORM frameworks.
* Want to reduce boiler plate code from their repositories.
* Don't want to deal with JDBC's complexities and annoying exception handling.
* Don't like to use any sort of automatic binding between their data structures and database.


# Table of Contents

1. [Installing LightAccess](#installation)
2. [Getting started](#start)
3. [DDL statements](#ddlstatements) 
    1. [DDLCommand](#ddlcommand)
    2. [Executing multiple DDL commands](#multipleddlstatements)
4. [DML statements](#dmlstatements)
    1. [Select - single result](#selectsingleresult)
    2. [Select - multiple results](#selectmultipleresults)
    3. [Normalising one to many joins](#onetomanyjoins)    
    4. [Insert](#insert)
    5. [Update](#udpate)
    6. [Delete](#delete)
    7. [Statement, PreparedStatement and CallableStatement](#jdbcstatements)
5. [Further documentation](#furtherdocumentation)
    1. [Databases tested](#databases)
6. [History](#history)


<a name="installation"></a>
## Installing Light Access (????) 

**TODO:** Check how other libraries say this (installing?). 

**TODO:** Add information about group, artifact id, name for Maven and Gradle.  

<a name="start"></a>    
## Getting started 

The main class to look at is [LightAccess][2]. We recommend to have this class injected into your [repositories][3]. 

LightAccess receives a Datasource in its constructor and you can pass a connection pool to it. Let's do it using [h2][4].  

```Java
import com.codurance.lightaccess.LightAccess;
import org.h2.jdbcx.JdbcConnectionPool;
``` 

```Java
JdbcConnectionPool jdbcConnectionPool = JdbcConnectionPool.create("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "user", "password");
LightAccess lightAccess = new LightAccess(jdbcConnectionPool);
``` 

<a name="ddlstatements"></a>       
## Executing DDL statements     

First let's define a DDL statement which create a table called 'products' with 3 fields. 

```java
    private static final String CREATE_PRODUCTS_TABLE = 
        "CREATE TABLE products (id integer PRIMARY KEY, name VARCHAR(255), date TIMESTAMP)";
```

So now, the only thing we need to do is to use the LightAccess to execute this DDL command.

```java
    lightAccess.executeDDLCommand((conn) -> conn.statement(CREATE_PRODUCTS_TABLE).execute());
``` 

And that's it. No exception handling or dealings with database connections. It is all handled for you.

Alternatively, you can extract the lambda to a method. 

```java
    private DDLCommand createProductsTable() {
        return (conn) -> conn.statement(CREATE_PRODUCTS_TABLE).execute();
    }
```

And use it like this. 

```java
    lightAccess.executeDDLCommand(createProductsTable());
```

<a name="ddlcommand"></a>
### DDLCommand 

The `LightAccess.executeDDLCommand(DDLCommand command)` receives a `DDLCommand` as parameter. 

```java
    public interface DDLCommand {
        void execute(LAConnection connection) throws SQLException;
    }
```   

With that, you can pass in any lambda that satisfy the `execute(LAConnection connection)` method signature.

<a name="multipleddlstatements"></a>
### Executing multiple DDL statements

It is possible to execute multiple commands in one go:

```java
    private static final String CREATE_USERS_TABLE = "CREATE TABLE users (userId integer PRIMARY KEY, name VARCHAR(255))";
    private static final String CREATE_WISHLISTS_TABLE = "CREATE TABLE wishlists (wishListId integer PRIMARY KEY, userId integer, name VARCHAR(255), creationDate TIMESTAMP)";
    private static final String CREATE_PRODUCTS_TABLE = "CREATE TABLE products (productId integer PRIMARY KEY, name VARCHAR(255), date TIMESTAMP)";
    private static final String CREATE_WISHLIST_PRODUCT_TABLE = "CREATE TABLE wishlist_product (id integer PRIMARY KEY, wishListId integer, productId integer)";
```

```java
    public void create_all_tables() {
        lightAccess.executeDDLCommand(createTables());
    }
    
    private DDLCommand createTables() {
        return (conn) -> {
            conn.statement(CREATE_USERS_TABLE).execute();
            conn.statement(CREATE_WISHLISTS_TABLE).execute();
            conn.statement(CREATE_PRODUCTS_TABLE).execute();
            conn.statement(CREATE_WISHLIST_PRODUCT_TABLE).execute();
        };
    }
``` 

<a name="dmlstatements"></a>
## Executing DML statements

Let's assume we have an object `Product` that we want to map to the `products` table. 

```java
    public class Product {
        private int id;
        private String name;
        private LocalDate date;    
        
        Product(int id, String name, LocalDate date) {
            this.id = id;
            this.name = name;
            this.date = date;
        }
    
        // getters
        
        // equals and hashcode    
    }
```   

<a name="selectsingleresult"></a>
### Select - single result 

Let's take the following select statement.

```java
    private static final String SELECT_PRODUCT_BY_ID_SQL = "select * from products where id = ?";
```

Now let's create a method that returns a lambda for this select statement. As we are looking for a single entity and 
we may not find it, it would be good if our query (`SQLQuery`) returned an `Optional<Product>`.  

```java
    private SQLQuery<Optional<Product>> retrieveProductWithId(int id) {
        return conn -> conn.prepareStatement(SELECT_PRODUCT_BY_ID_SQL)
                            .withParam(id)
                            .executeQuery()
                            .onlyResult(this::toProduct);
    }
```

In case we find a product with this ID, we need to map the result set to the Product object. This is done in the 
`toProduct` method passed to `.onlyResult()` above.

```java
    private Product toProduct(LAResultSet laResultSet) {
        return new Product(laResultSet.getInt(1),
                           laResultSet.getString(2),
                           laResultSet.getLocalDate(3));
    }
```

Now we only need to execute the select statement. 

```java
    Optional<Product> product = lightAccess.executeQuery(retrieveProductWithId(10));
```

In case you prefer an inline version, you can use:

```java
    Optional<Product> product = lightAccess.executeQuery(conn -> conn.prepareStatement(SELECT_PRODUCT_BY_ID_SQL)
                                                                        .withParam(PRODUCT_TWO.id)
                                                                        .executeQuery()
                                                                        .onlyResult(this::toProduct));
```

<a name="selectmultipleresults"></a>
### Select - multiple results 

Let's take the following select statement:

```java
    private static final String SELECT_ALL_PRODUCTS_SQL = "select * from products";
```

Now let's create a method that returns a lambda:

```java
    private SQLQuery<List<Product>> retrieveAllProducts() {
        return conn -> conn.prepareStatement(SELECT_ALL_PRODUCTS_SQL)
                            .executeQuery()
                            .mapResults(this::toProduct);
    }
```

Note that now we are calling `mapResults(this::toProduct)` instead of `onlyResult(this::toProduct)`, and the `SQLQuery` 
is parameterised to return `List<Product>`.

Now we just need to invoke the query like before. 

```java
    List<Product> products = lightAccess.executeQuery(retrieveAllProducts());
```

And in case you prefer the inlined version:

```java
    List<Product> products = lightAccess.executeQuery(conn -> conn.prepareStatement(SELECT_ALL_PRODUCTS_SQL)
                                                                    .executeQuery()
                                                                    .mapResults(this::toProduct));
```

<a name="onetomanyjoins"></a>
### Normalising one to many joins

Let's say we have a table with users and a table with wish lists:

```sql
CREATE TABLE users (userId integer PRIMARY KEY, name VARCHAR(255));
CREATE TABLE wishlists (wishListId integer PRIMARY KEY, userId integer, name VARCHAR(255), creationDate TIMESTAMP);
```

Now let's assume we want to have all users and their respective wish lists, including the users without wish list. 

```sql
select u.userId, u.name, w.wishListId, w.userId, w.name, w.creationDate
    from users u 
    left join wishlists w on u.userId = w.userId
```

We want the result to be stored in a list containing the following DTO:

```java
    public class UserWithWishList {
    
        private final User user;
        private final List<WishList> wishLists;
    
        public UserWithWishList(User user, List<WishList> wishLists) {
            this.user = user;
            this.wishLists = unmodifiableList(wishLists);
        }
    
        // getters, equals, hashcode.
    }
```

For this to work we need to have a DTO for user and a DTO for the wish list:

```java
   public class User {
   
       private final Integer id;
       private final String name;
   
       public User(Integer id, String name) {
           this.id = id;
           this.name = name;
       }

       // getters, equals, hashcode.
    }
```

```java
    public class WishList {
    
        private final Integer id;
        private final Integer userId;
        private final String name;
        private final LocalDate creationDate;
    
        public WishList(Integer id, Integer userId, String name, LocalDate creationDate) {
            this.id = id;
            this.userId = userId;
            this.name = name;
            this.creationDate = creationDate;
        }
    }
```

So now we are ready to get a list of `UserWithWishList` objects:

```java

    public List<UserWithWishList> usersWithWishLists() {
        OneToMany<User, WishList> wishListsPerUser = lightAccess.executeQuery((conn -> 
                conn.prepareStatement(SELECT_WISHLISTS_PER_USER_SQL)
                     .executeQuery()
                     .normaliseOneToMany(this::mapToUserWishList)))
        
        return wishListsPerUser.collect((user, wishLists) -> new UserWithWishList(user, wishLists));
    }
    
    private KeyValue<User, Optional<WishList>> mapToUserWishList(LAResultSet laResultSet) {
        User user = new User(laResultSet.getInt(1), laResultSet.getString(2));

        Optional<WishList> wishList = Optional.ofNullable((laResultSet.getInt(3) > 0)
                                                    ? new WishList(laResultSet.getInt(3),
                                                                    laResultSet.getInt(4),
                                                                    laResultSet.getString(5),
                                                                    laResultSet.getLocalDate(6))
                                                    : null);
        return new KeyValue<>(user, wishList);
    }    
```

For more details, please check the [integration tests for joins][5]

<a name="insert"></a>
### Insert

Let's assume we have the following product table:

```sql
CREATE TABLE products (id VARCHAR(255) PRIMARY KEY, name VARCHAR(255), date TIMESTAMP)
```

And we have the following Product DTO. 

```java
    public class Product {
        private int id;
        private String name;
        private LocalDate date;
    
        public Product(int id, String name, LocalDate date) {
            this.id = id;
            this.name = name;
            this.date = date;
        }
    
        public int id() {
            return id;
        }
    
        public String name() {
            return name;
        }
    
        public LocalDate date() {
            return date;
        }
        
        // equals, hashcode
    }
```

For inserting a product, we just need to do the following:

```java
    INSERT_PRODUCT_SQL = "insert into products (id, name, date) values (?, ?, ?)";

    Product product = new Product(1, "Product 1", LocalDate.of(2017, 07, 26));

    lightAccess.executeCommand(conn -> conn.prepareStatement(INSERT_PRODUCT_SQL)
                                            .withParam(product.id())
                                            .withParam(product.name())
                                            .withParam(product.date())
                                            .executeUpdate());
```

And as always, can extract the lambda to a method:

```java
    private SQLCommand insert(Product product) {
        return conn -> conn.prepareStatement(INSERT_PRODUCT_SQL)
                            .withParam(product.id())
                            .withParam(product.name())
                            .withParam(product.date())
                            .executeUpdate();
    }
```

And call it like that:

```java
    lightAccess.executeCommand(insert(produt));
```
 
<a name="update"></a> 
### Update 

Let's say that we wan to update the name of the given product.

```java
private static final String UPDATE_PRODUCT_NAME_SQL = "update products set name = ? where id = ?";
```

Now we can execute the update:

```java
    lightAccess.executeCommand(updateProductName(1, "Another name"));
```

```java
    private SQLCommand updateProductName(int id, String name) {
        return conn -> conn.prepareStatement(UPDATE_PRODUCT_NAME_SQL)
                            .withParam(name)
                            .withParam(id)
                            .executeUpdate();
    }
```

<a name="delete"></a>
### Delete

Delete is exactly the same as inserts and updates. 

## Calling sequences (PostgreSQL / H2)

Let's first create a sequence:

```java
    private static final String ID_SEQUENCE = "id_sequence";
    private static final String CREATE_SEQUENCE_DDL = "CREATE SEQUENCE " + ID_SEQUENCE + " START WITH 1";
```

```java
    lightAccess.executeDDLCommand((conn) -> conn.statement(CREATE_SEQUENCE_DDL).execute());
```

Now we can read the next ID from it. 

```java
   int id = lightAccess.nextId(ID_SEQUENCE);
```

In case we don't want an int ID, we can also map the ID to something else:

```java
    ProductID secondId = lightAccess.nextId(ID_SEQUENCE, ProductID::new);
```

Where `ProductID` is:

```java
    public class ProductID {
        private int id;
    
        public ProductID(int id) {
            this.id = id;
        }
        
        // getter, equals, hashcode
    }
```

We can also map that to String or any other object:

```java
    String stringID = lightAccess.nextId(ID_SEQUENCE, Object::toString);
```

<a name="jdbcstatements"></a>
### Creating Statement, PreparedStatement and CallableStatement

An instance of `LAConnection` will be received in all queries and commands represented by `DDLCommand`, `SQLCommand` 
and `SQLQuery`.  With this instance you can create a [Statement][6], [PreparedStatement][7] and [CallableStatement][8], 
according to your need. 

As a guideline, we normally use a `Statement` for DDL, a `PreparedStatement` for DML and `CallableStatement` for calling
stored procedures or sequences. 

<a name="furtherdocumentation"></a>
# Further documentation 

Please check the [tests][9] for more details in how to use this library.  

<a name="databases"></a>
### Databases tested

We have only tested this library with [Amazon RDS][10] for [PostgreSQL][11]. 

<a name="history"></a>
## History

This library was first created by [Sandro Mancuso][12] while refactoring and removing duplication from multiple 
repositories in one of the [Codurance][13]'s internal projects.

[1]: https://docs.oracle.com/javase/tutorial/jdbc/basics/
[2]: https://github.com/codurance/light-access/blob/master/src/main/java/com/codurance/lightaccess/LightAccess.java  
[3]: https://martinfowler.com/eaaCatalog/repository.html
[4]: http://www.h2database.com/html/main.html 
[5]: https://github.com/codurance/light-access/blob/master/src/test/java/integration/JoinsIntegrationTest.java
[6]: https://docs.oracle.com/javase/8/docs/api/java/sql/Statement.html
[7]: https://docs.oracle.com/javase/8/docs/api/java/sql/PreparedStatement.html
[8]: https://docs.oracle.com/javase/8/docs/api/java/sql/CallableStatement.html
[9]: https://github.com/codurance/light-access/tree/master/src/test/java
[10]: https://aws.amazon.com/rds/
[11]: https://www.postgresql.org/
[12]: http://twitter.com/sandromancuso
[13]: http://codurance.com