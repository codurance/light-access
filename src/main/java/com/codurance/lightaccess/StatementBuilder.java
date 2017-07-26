package com.codurance.lightaccess;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class StatementBuilder {

    private final Statement statement;
    private final String sql;

    StatementBuilder(Connection connection, String sql) {
        try {
            this.statement = connection.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
