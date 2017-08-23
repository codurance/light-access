package com.codurance.lightaccess.mapping;

import java.util.*;
import java.util.function.BiFunction;


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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OneToMany<?, ?> oneToMany = (OneToMany<?, ?>) o;

        return data.equals(oneToMany.data);
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }

    @Override
    public String toString() {
        return "OneToMany{" +
                "data=" + data +
                '}';
    }
}
