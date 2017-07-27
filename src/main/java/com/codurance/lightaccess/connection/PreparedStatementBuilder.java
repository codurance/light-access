package com.codurance.lightaccess.connection;

import com.codurance.lightaccess.executables.Throwables;
import com.codurance.lightaccess.mapping.LAResultSet;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;

import static com.codurance.lightaccess.executables.Throwables.execute;

public class PreparedStatementBuilder {

    private PreparedStatement preparedStatement;
    private int paramIndex = 0;

    PreparedStatementBuilder(Connection connection, String sql) {
        execute(() -> this.preparedStatement = connection.prepareStatement(sql));
    }

    public PreparedStatementBuilder withParam(String param) {
        return withParam((paramIndex) -> execute(() -> preparedStatement.setString(paramIndex, param)));
    }

    public PreparedStatementBuilder withParam(int param) {
        return withParam((paramIndex) -> execute(() -> preparedStatement.setInt(paramIndex, param)));
    }

    public PreparedStatementBuilder withParam(LocalDate param) {
        return withParam((paramIndex) -> execute(() -> preparedStatement.setDate(paramIndex, Date.valueOf(param))));
    }

    public void executeUpdate() {
        execute(() -> {
            preparedStatement.executeUpdate();
            preparedStatement.close();
        });

    }

    public LAResultSet executeQuery() {
        return Throwables.executeQuery(() -> new LAResultSet(preparedStatement.executeQuery()));
    }

    private interface SetParam {
        void execute(int paramCount);
    }

    private PreparedStatementBuilder withParam(SetParam setParam) {
        paramIndex += 1;
        execute(() -> setParam.execute(paramIndex));
        return this;
    }

}
