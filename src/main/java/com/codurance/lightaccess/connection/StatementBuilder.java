package com.codurance.lightaccess.connection;

import com.codurance.lightaccess.executables.Throwables;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class StatementBuilder {

    private Statement statement;
    private String sql;

    StatementBuilder(Connection connection, String sql) {
        Throwables.execute(() -> this.statement = connection.createStatement());
        this.sql = sql;
    }

    public void execute() {
        try {
            statement.execute(sql);
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
