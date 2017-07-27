package com.codurance.lightaccess.mapping;

import java.util.Map;

public class KeyValue<K, V> implements Map.Entry {

    private final K key;
    private V value;

    public KeyValue(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public Object setValue(Object value) {
        V oldValue = (V) value;
        this.value = (V) value;
        return oldValue;
    }

    @Override
    public String toString() {
        return "KeyValue{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }
}
