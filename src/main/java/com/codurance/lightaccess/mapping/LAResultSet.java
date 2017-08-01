package com.codurance.lightaccess.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

import static com.codurance.lightaccess.executables.Throwables.executeQuery;

public class LAResultSet {
    private final SimpleDateFormat YYYY_MM_DD_date_format = new SimpleDateFormat("yyyy-MM-dd");
    private ResultSet resultSet;

    public LAResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    public int getInt(int columnIndex) {
        return executeQuery(() -> resultSet.getInt(columnIndex));
    }

    public String getString(int columnIndex) {
        String stringValue = executeQuery(() -> resultSet.getString(columnIndex));
        return (stringValue != null) ? stringValue : "";
    }

    public Optional<String> getOptionalString(int columnIndex) {
        return Optional.ofNullable(executeQuery(() -> resultSet.getString(columnIndex)));
    }

    public LocalDate getLocalDate(int columnIndex) {
        return utilDateToLocalDate(getDate(columnIndex));
    }

    public Date getDate(int columnIndex) {
        return executeQuery(() -> sqlDateToUtilDate(columnIndex));
    }

    public Optional<LocalDate> getOptionalLocalDate(int columnIndex) {
        return Optional.ofNullable(getLocalDate(columnIndex));
    }

    public <T> Optional<T> onlyResult(Function<LAResultSet, T> mapOne) throws SQLException {
        if (resultSet.next()) {
            return Optional.of(mapOne.apply(this));
        }
        return Optional.empty();
    }

    public <T> List<T> mapResults(Function<LAResultSet, T> mapResults) {
        List<T> list = new ArrayList<>();
        while (this.next()) {
            list.add(mapResults.apply(this));
        }
        return list;
    }

    public <K, V> OneToMany<K, V> normaliseOneToMany(Function<LAResultSet, KeyValue<K, Optional<V>>> normalise) {
        OneToMany<K, V> oneToMany = new OneToMany<>();
        while (this.next()) {
            KeyValue<K, Optional<V>> kv = normalise.apply(this);
            oneToMany.put(kv);
        }
        return oneToMany;
    }

    public LAResultSet nextRecord() {
        next();
        return this;
    }

    private boolean next() {
        return executeQuery(() -> resultSet.next());
    }

    private Date sqlDateToUtilDate(int columnIndex) throws SQLException {
        java.sql.Date date = resultSet.getDate(columnIndex);
        return (date != null) ? new Date(date.getTime()) : null;
    }

    private LocalDate utilDateToLocalDate(Date date) {
        return (date != null) ? LocalDate.parse(YYYY_MM_DD_date_format.format(date)) : null;
    }

}
