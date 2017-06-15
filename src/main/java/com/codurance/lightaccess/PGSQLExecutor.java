package com.codurance.lightaccess;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

public class PGSQLExecutor {

    private DataSource ds;

    public PGSQLExecutor(DataSource connection) {
        this.ds = connection;
    }

    public interface SQLQuery<T> {
        T execute(PGConnection connection) throws SQLException;
    }

    public interface SQLCommand {
        void execute(PGConnection connection) throws SQLException;
    }

    public <T> T execute(SQLQuery<T> sqlQuery) {
        try (PGConnection conn = pgConnection()) {
            return sqlQuery.execute(conn);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void executeUpdate(SQLCommand sqlCommand) {
        try (PGConnection conn = pgConnection()) {
            sqlCommand.execute(conn);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T nextId(String sequenceName, Function<Integer, T> nextId) {
        try (PGConnection conn = pgConnection()) {
            CallableStatement cs = conn.prepareCall("select nextval('" + sequenceName + "')");
            ResultSet resultSet = cs.executeQuery();
            resultSet.next();
            return nextId.apply(resultSet.getInt(1));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private PGConnection pgConnection() {
        try {
            return new PGConnection(ds.getConnection());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
