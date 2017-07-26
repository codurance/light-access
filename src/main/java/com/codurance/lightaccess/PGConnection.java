package com.codurance.lightaccess;

import java.sql.CallableStatement;
import java.sql.Connection;

import static com.codurance.lightaccess.Throwables.executeQuery;

public class PGConnection implements AutoCloseable {

    private Connection connection;

    public PGConnection(Connection connection) {
        this.connection = connection;
    }

    public Connection connection() {
        return this.connection;
    }

    public PGPreparedStatementBuilder prepareStatement(String sql) {
        return new PGPreparedStatementBuilder(connection, sql);
    }

    public StatementBuilder statement(String sql) {
        return new StatementBuilder(connection, sql);
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }

    protected CallableStatement prepareCall(String sql) {
        return executeQuery(() -> connection.prepareCall(sql));
    }
}
