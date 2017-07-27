package com.codurance.lightaccess.connection;

import com.codurance.lightaccess.executables.Throwables;

import java.sql.Connection;
import java.sql.Statement;

import static com.codurance.lightaccess.executables.Throwables.executeWithResource;

public class StatementBuilder {

    private Statement statement;
    private String sql;

    StatementBuilder(Connection connection, String sql) {
        Throwables.execute(() -> this.statement = connection.createStatement());
        this.sql = sql;
    }

    public void execute() {
        executeWithResource(statement, () -> statement.execute(sql));
    }
}
