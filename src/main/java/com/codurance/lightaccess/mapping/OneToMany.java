package com.codurance.lightaccess.mapping;

import java.util.*;
import java.util.function.BiFunction;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

public class OneToMany<K, V> {

    private Map<K, List<V>> data = new HashMap<>();

    public void put(KeyValue<K, Optional<V>> keyValue) {
        if (keyValue.getValue().isPresent()) {
            this.put(keyValue.getKey(), keyValue.getValue());
        } else {
            this.put(keyValue.getKey(), Optional.<V>empty());
        }
    }

    public <T> List<T> collect(BiFunction<K, List<V>, T> collect) {
        List<T> list = new ArrayList<>();
        data.entrySet().forEach(
                e -> list.add(collect.apply(e.getKey(), e.getValue())));
        return list;
    }

    private void put(K key, Optional<V> value) {
        List<V> children = data.getOrDefault(key, new ArrayList<>());
        if (value.isPresent()) {
            children.add(value.get());
        }
        data.put(key, children);
    }

    @Override
    public boolean equals(Object other) {
        return reflectionEquals(this, other);
    }

    @Override
    public int hashCode() {
        return reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return "OneToMany{" +
                "data=" + data +
                '}';
    }
}
