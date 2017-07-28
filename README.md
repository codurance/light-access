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
        "CREATE TABLE products (id VARCHAR(255) PRIMARY KEY, name VARCHAR(255), date TIMESTAMP)";
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


## Calling sequences (PostgreSQL / H2)

## Creating Statement, PreparedStatement and CallableStatement

An instance of `LAConnection` will be received in all queries and commands represented by `DDLCommand`, `SQLCommand` 
and `SQLQuery`.  With this instance you can create a [Statement][?], [PreparedStatement][?] and CallableStatement[?], 
according to your need. 

As a guideline, we normally use a `Statement` for DDL, a `PreparedStatement` for DML and `CallableStatement` for calling
stored procedures or sequences. 

# Further documentation 

Please check the tests for more details in how to use this library.  

### Databases tested

We have only tested this library with Amazon RDS for PostgreSQL. 

[1]: https://docs.oracle.com/javase/tutorial/jdbc/basics/
[2]: http://link to LightAccess class on github.  
[3]: link to the repository building block (DDD)
[4]: link to h2 database. 
[?]: link to JDBC Statement
[?]: link to JDBC PreparedStatement
[?]: link to JDBC CallableStatement