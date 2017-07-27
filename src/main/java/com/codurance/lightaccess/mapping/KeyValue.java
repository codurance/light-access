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


    /**
     * I would rather have this class immutable but this method
     * had to be implemented because of the Map.Entry interface.
     *
     * @see java.util.Map.Entry#setValue(Object)
     */
    @Override
    public Object setValue(Object value) {
        V oldValue = (V) this.value;
        this.value = (V) value;
        return oldValue;
    }
}
