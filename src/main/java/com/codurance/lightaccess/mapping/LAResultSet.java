package com.codurance.lightaccess.mapping;

import com.codurance.lightaccess.Throwables;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class LAResultSet {
    private final SimpleDateFormat YYYY_MM_DD_date_format = new SimpleDateFormat("yyyy-MM-dd");
    private ResultSet resultSet;

    public LAResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    public int getInt(int columnIndex) {
        return Throwables.executeQuery(() -> resultSet.getInt(columnIndex));
    }

    public String getString(int columnIndex) {
        return Throwables.executeQuery(() -> resultSet.getString(columnIndex));
    }

    public Date getDate(int columnIndex) {
        return Throwables.executeQuery(() -> resultSet.getDate(columnIndex));
    }

    public Optional<LocalDate> getOptionalDate(int columnIndex) {
        Date value = getDate(columnIndex);
        return (value != null)
                ? Optional.of(LocalDate.parse(YYYY_MM_DD_date_format.format(value)))
                : Optional.empty();
    }

    public Optional<String> getOptionalString(int columnIndex) {
        String value = getString(columnIndex);
        return (value != null) ? Optional.of(value) : Optional.empty();
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

    private boolean next() {
        return Throwables.executeQuery(() -> resultSet.next());
    }
}
