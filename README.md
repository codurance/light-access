**TODO:** Add the last CI run indicator 

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

**TODO:** Create an index here so people can jump straight to the section they want to see. 

# Installing Light Access (????)

**TODO:** Check how other libraries say this (installing?). 

**TODO:** Add information about group, artifact id, name for Maven and Gradle.  

# Getting started

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

### DDLCommand

The `LightAccess.executeDDLCommand(DDLCommand command)` receives a `DDLCommand` as parameter. 

```java
    public interface DDLCommand {
        void execute(LAConnection connection) throws SQLException;
    }
```   

With that, you can pass in any lambda that satisfy the `execute(LAConnection connection)` method signature. 

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

### Normalising one to many joins

**TODO:** Create an example using Order (1) -> (*) Product. Probably better to have an integration test.

**TODO:** Create an example of collecting it to a different DTO.

Let's say we have a table with orders and a table with product:

```java
"CREATE TABLE baskets (basketId integer PRIMARY KEY, userId integer, created TIMESTAMP)";
"CREATE TABLE products (productId integer PRIMARY KEY, name VARCHAR(255), date TIMESTAMP)";
"CREATE TABLE basket_items (basketId integer, productId integer, quantity integer)";
```
Now, let's say we want to populate an object called basket:

```java
public class Basket {
    
}
```

### Insert, Delete, and Update   

## Calling sequences (PostgreSQL / H2)

## Creating Statement, PreparedStatement and CallableStatement

An instance of `LAConnection` will be received in all queries and commands represented by `DDLCommand`, `SQLCommand` 
and `SQLQuery`.  With this instance you can create a [Statement][?], [PreparedStatement][?] and CallableStatement[?], 
according to your need. 

As a guideline, we normally use a `Statement` for DDL, a `PreparedStatement` for DML and `CallableStatement` for calling
stored procedures or sequences. 

# Further documentation 

Please check the [tests][?] for more details in how to use this library.  

### Databases tested

We have only tested this library with [Amazon RDS][?] for [PostgreSQL][?]. 

#### History

This library was first created by [Sandro Mancuso][?] while refactoring and removing duplication from multiple 
repositories in one of the [Codurance][?]'s internal projects.

[1]: https://docs.oracle.com/javase/tutorial/jdbc/basics/
[2]: http://link to LightAccess class on github.  
[3]: link to the repository building block (DDD)
[4]: link to h2 database. 
[?]: link to the test package
[?]: link to JDBC Statement
[?]: link to JDBC PreparedStatement
[?]: link to JDBC CallableStatement
[?]: link to Amazon RDS
[?]: link to PostgreSQL
[?]: http://twitter.com/sandromancuso
[?]: http://codurance.com