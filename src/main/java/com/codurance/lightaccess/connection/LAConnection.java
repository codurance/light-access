package com.codurance.lightaccess.connection;

import java.sql.CallableStatement;
import java.sql.Connection;

import static com.codurance.lightaccess.Throwables.executeQuery;

public class LAConnection implements AutoCloseable {

    private Connection connection;

    public LAConnection(Connection connection) {
        this.connection = connection;
    }

    public PreparedStatementBuilder prepareStatement(String sql) {
        return new PreparedStatementBuilder(connection, sql);
    }

    public StatementBuilder statement(String sql) {
        return new StatementBuilder(connection, sql);
    }

    //TODO Check if this method can be written as statement and prepareStatement
    public CallableStatement prepareCall(String sql) {
        return executeQuery(() -> connection.prepareCall(sql));
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}
