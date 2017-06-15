package com.codurance.lightaccess;

import static com.codurance.lightaccess.Throwables.execute;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class PGPreparedStatementBuilder {

    private final PreparedStatement preparedStatement;
    private int paramIndex = 0;

    public PGPreparedStatementBuilder(Connection connection, String sql) {
        try {
            this.preparedStatement = connection.prepareStatement(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public PGPreparedStatementBuilder withParam(String param) {
        return withParam((paramIndex) -> execute(() -> preparedStatement.setString(paramIndex, param)));
    }

    public PGPreparedStatementBuilder withOptionalStringParam(Optional<String> param) {
        return withParam(param.isPresent() ? param.get() : "");
    }

    public PGPreparedStatementBuilder withParam(int param) {
        return withParam((paramIndex) -> execute(() -> preparedStatement.setInt(paramIndex, param)));
    }

    public PGPreparedStatementBuilder withParam(Date param) {
        return withParam((paramIndex) -> execute(() -> preparedStatement.setDate(paramIndex, param)));
    }

    public PGPreparedStatementBuilder withOptionalLocalDateParam(Optional<LocalDate> param) {
        return withParam(param.isPresent() ? new Date(param.get().toEpochDay()) : null);
    }

    public void executeUpdate() {
        execute(() -> {
            preparedStatement.executeUpdate();
            preparedStatement.close();
        });

    }

    public PGResultSet executeQuery() {
        return Throwables.executeQuery(() -> new PGResultSet(preparedStatement.executeQuery()));
    }

    private interface SetParam {
        void execute(int paramCount);
    }

    private PGPreparedStatementBuilder withParam(SetParam setParam) {
        paramIndex += 1;
        execute(() -> setParam.execute(paramIndex));
        return this;
    }

}
