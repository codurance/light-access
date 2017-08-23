package com.codurance.lightaccess.executables;

import com.codurance.lightaccess.connection.LAConnection;

import java.sql.SQLException;

@FunctionalInterface
public interface SQLQuery<T> {
    T execute(LAConnection connection) throws SQLException;
}
