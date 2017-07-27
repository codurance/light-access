package com.codurance.lightaccess.connection;

import java.sql.Connection;

public class LAConnection implements AutoCloseable {

    private Connection connection;

    public LAConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * Used for SQL statements with parameters and that can be executed
     * multiple times.
     *
     * @param sql SQL query or command.
     * @return
     */
    public PreparedStatementBuilder prepareStatement(String sql) {
        return new PreparedStatementBuilder(connection, sql);
    }

    /**
     * Used for DDL commands.
     *
     * @param ddl DDL statement.
     * @return
     */
    public StatementBuilder statement(String ddl) {
        return new StatementBuilder(connection, ddl);
    }

    /**
     * Used for invoking stored procedures and sequences.
     *
     * @param sql SQL statement for calling stored procedures or sequences.
     * @return
     */
    public CallableStatementBuilder callableStatement(String sql) {
        return new CallableStatementBuilder(connection, sql);
    }

    /**
     * Closes the connection.
     *
     * @throws Exception
     */
    @Override
    public void close() throws Exception {
        connection.close();
    }
}
