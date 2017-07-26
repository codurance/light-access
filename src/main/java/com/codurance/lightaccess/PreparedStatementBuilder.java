package com.codurance.lightaccess;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

import static com.codurance.lightaccess.Throwables.execute;

public class PreparedStatementBuilder {

    private final PreparedStatement preparedStatement;
    private int paramIndex = 0;

    public PreparedStatementBuilder(Connection connection, String sql) {
        try {
            this.preparedStatement = connection.prepareStatement(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public PreparedStatementBuilder withParam(String param) {
        return withParam((paramIndex) -> execute(() -> preparedStatement.setString(paramIndex, param)));
    }

    public PreparedStatementBuilder withOptionalStringParam(Optional<String> param) {
        return withParam(param.isPresent() ? param.get() : "");
    }

    public PreparedStatementBuilder withParam(int param) {
        return withParam((paramIndex) -> execute(() -> preparedStatement.setInt(paramIndex, param)));
    }

    public PreparedStatementBuilder withParam(Date param) {
        return withParam((paramIndex) -> execute(() -> preparedStatement.setDate(paramIndex, param)));
    }

    public PreparedStatementBuilder withOptionalLocalDateParam(Optional<LocalDate> param) {
        return withParam(param.isPresent() ? new Date(param.get().toEpochDay()) : null);
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
