package com.codurance.lightaccess;

import org.junit.Before;
import org.junit.Test;
import java.util.*;

import static java.util.Arrays.*;
import static java.util.Collections.*;
import static java.util.Optional.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.IsEqual.equalTo;

public class OneToManyShould {

    private static final String KEY_WITHOUT_VALUES = "Key without value";
    private static final String KEY_WITH_SINGLE_VALUE = "Key with single value";
    private static final String KEY_WITH_MULTIPLE_VALUES = "Key with multiple value";
    private static final String SOME_VALUE = "Value 1";
    private static final String SOME_OTHER_VALUE = "Value 2";
    private OneToMany<String, String> oneToMany;

    @Before
    public void setup() {
        oneToMany = new OneToMany<>();
    }

    @Test
    public void collectMappedValuesGroupedByKey() throws Exception {
        oneToMany.put(new KeyValue<>(KEY_WITH_MULTIPLE_VALUES, of(SOME_VALUE)));
        oneToMany.put(new KeyValue<>(KEY_WITH_MULTIPLE_VALUES, of(SOME_OTHER_VALUE)));
        oneToMany.put(new KeyValue<>(KEY_WITH_SINGLE_VALUE, of(SOME_VALUE)));

        assertThat(oneToMany.collect(MappedKeyValues::new),
                containsInAnyOrder(
                        new MappedKeyValues(KEY_WITH_MULTIPLE_VALUES, asList(SOME_VALUE, SOME_OTHER_VALUE)),
                        new MappedKeyValues(KEY_WITH_SINGLE_VALUE, singletonList(SOME_VALUE))));
    }

    @Test
    public void collectEmptyMappedValuesWhenAllKeyValuePairsForGivenKeyHaveEmptyValues() throws Exception {
        oneToMany.put(new KeyValue<>(KEY_WITHOUT_VALUES, empty()));

        assertThat(oneToMany.collect(MappedKeyValues::new),
                equalTo(singletonList(new MappedKeyValues(KEY_WITHOUT_VALUES, emptyList()))));
    }

    @Test
    public void collectMappedValuesOnlyForKeyValuePairsWithNonEmptyValues() throws Exception {
        oneToMany.put(new KeyValue<>(KEY_WITH_SINGLE_VALUE, of(SOME_VALUE)));
        oneToMany.put(new KeyValue<>(KEY_WITH_SINGLE_VALUE, empty()));

        assertThat(oneToMany.collect(MappedKeyValues::new),
                equalTo(singletonList(new MappedKeyValues(KEY_WITH_SINGLE_VALUE, singletonList(SOME_VALUE)))));
    }

    @Test
    public void returnEmptyMappedValuesWhenThereIsNoMappedValue() throws Exception {
        assertThat(oneToMany.collect(MappedKeyValues::new),
                equalTo(new ArrayList<>()));
    }

    private static class MappedKeyValues {
        String key;
        List<String> values;

        MappedKeyValues(String key, List<String> values) {
            this.key = key;
            this.values = values;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MappedKeyValues tuple = (MappedKeyValues) o;

            if (key != null ? !key.equals(tuple.key) : tuple.key != null) return false;
            return values != null ? values.equals(tuple.values) : tuple.values == null;
        }

        @Override
        public int hashCode() {
            int result = key != null ? key.hashCode() : 0;
            result = 31 * result + (values != null ? values.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "MappedKeyValues{" +
                    "key='" + key + '\'' +
                    ", values=" + values +
                    '}';
        }
    }
}
