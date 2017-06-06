package com.codurance.lightaccess;

import org.junit.Test;
import java.util.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class PGOneToManyShould {

    private static final int SOME_KEY = 1;
    public static final int SOME_VALUE = 3;
    private static final Integer SOME_OTHER_VALUE = 4;

    @Test
    public void initiateWithNoElements() throws Exception {
        PGOneToMany<Integer, Integer> pgOneToMany = new PGOneToMany<>();

        assertThat(pgOneToMany.collect((key, values) -> new KeyValuePair(key,values)),
                equalTo(new ArrayList<>()));
    }

    @Test
    public void storeAKeyWithNoValuesIfValueIsNotGiven() throws Exception {
        PGOneToMany<Integer, Integer> pgOneToMany = new PGOneToMany<>();

        pgOneToMany.put(new PGKeyValue<>(SOME_KEY, Optional.empty()));

        assertThat(pgOneToMany.collect((key,values) -> new KeyValuePair(key,values)),
                equalTo(Arrays.asList(new KeyValuePair(SOME_KEY, new ArrayList<>()))));
    }

    @Test
    public void storeAKeyWithOneValue() throws Exception {
        PGOneToMany<Integer, Integer> pgOneToMany = new PGOneToMany<>();

        pgOneToMany.put(new PGKeyValue<>(SOME_KEY, Optional.of(SOME_VALUE)));

        assertThat(pgOneToMany.collect((key,values) -> new KeyValuePair(key,values)),
                equalTo(Arrays.asList(new KeyValuePair(SOME_KEY, Arrays.asList(SOME_VALUE)))));
    }

    @Test
    public void storeAKeyWithTwoValue() throws Exception {
        PGOneToMany<Integer, Integer> pgOneToMany = new PGOneToMany<>();

        pgOneToMany.put(new PGKeyValue<>(SOME_KEY, Optional.of(SOME_VALUE)));
        pgOneToMany.put(new PGKeyValue<>(SOME_KEY, Optional.of(SOME_OTHER_VALUE)));

        assertThat(pgOneToMany.collect((key,values) -> new KeyValuePair(key,values)),
                equalTo(Arrays.asList(new KeyValuePair(SOME_KEY, Arrays.asList(SOME_VALUE, SOME_OTHER_VALUE)))));
    }

    @Test
    public void mapEntriesWhenCollect() throws Exception {
        PGOneToMany<Integer, Integer> pgOneToMany = new PGOneToMany<>();

        pgOneToMany.put(new PGKeyValue<>(SOME_KEY, Optional.of(SOME_VALUE)));
        pgOneToMany.put(new PGKeyValue<>(SOME_KEY, Optional.of(SOME_OTHER_VALUE)));

        int numberOfValues = 2;

        assertThat(pgOneToMany.collect((key, values) -> values.size()),
                equalTo(Arrays.asList(numberOfValues)));
    }

    private static class KeyValuePair {
        public Integer first;
        public List<Integer> second;

        public KeyValuePair(Integer first, List<Integer> second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            KeyValuePair tuple = (KeyValuePair) o;

            if (first != null ? !first.equals(tuple.first) : tuple.first != null) return false;
            return second != null ? second.equals(tuple.second) : tuple.second == null;
        }

        @Override
        public int hashCode() {
            int result = first != null ? first.hashCode() : 0;
            result = 31 * result + (second != null ? second.hashCode() : 0);
            return result;
        }
    }
}
