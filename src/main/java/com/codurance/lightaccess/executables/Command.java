package com.codurance.lightaccess.executables;

import com.codurance.lightaccess.connection.LAConnection;

import java.sql.SQLException;

@FunctionalInterface
public interface Command {
    void execute(LAConnection connection) throws SQLException;
}
