package com.codurance.lightaccess;

import java.util.*;
import java.util.function.BiFunction;

public class PGOneToMany<K, V> {

    private Map<K, List<V>> data = new HashMap<>();

    public void put(PGKeyValue<K, Optional<V>> keyValue) {
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
}
