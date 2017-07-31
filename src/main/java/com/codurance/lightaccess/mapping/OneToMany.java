package com.codurance.lightaccess.mapping;

import java.util.*;
import java.util.function.BiFunction;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;

public class OneToMany<K, V> {

    private Map<K, List<V>> data = new HashMap<>();

    public void put(KeyValue<K, Optional<V>> keyValue) {
        if (keyValue.getValue().isPresent()) {
            this.put(keyValue.getKey(), keyValue.getValue());
        } else {
            this.put(keyValue.getKey(), Optional.<V>empty());
        }
    }

    public void put(K key, Optional<V> value) {
        List<V> children = data.getOrDefault(key, new ArrayList<>());
        value.ifPresent(children::add);
        data.put(key, children);
    }

    public <T> List<T> collect(BiFunction<K, List<V>, T> collect) {
        List<T> list = new ArrayList<>();
        data.forEach((key, value) -> list.add(collect.apply(key, value)));
        return list;
    }

    @Override
    public boolean equals(Object other) {
        return reflectionEquals(this, other);
    }

    @Override
    public String toString() {
        return "OneToMany{" +
                "data=" + data +
                '}';
    }
}
