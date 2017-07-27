package com.codurance.lightaccess.connection;

import com.codurance.lightaccess.executables.Throwables;
import com.codurance.lightaccess.mapping.LAResultSet;

import java.sql.CallableStatement;
import java.sql.Connection;

public class CallableStatementBuilder {

    private CallableStatement callableStatement;

    public CallableStatementBuilder(Connection connection, String sql) {
        this.callableStatement = Throwables.executeQuery(() -> connection.prepareCall(sql));
    }

    public LAResultSet executeQuery() {
        return Throwables.executeQuery(() -> new LAResultSet(callableStatement.executeQuery()));
    }
}
