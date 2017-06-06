package com.codurance.lightaccess;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

@RunWith(Parameterized.class)
public class PGOneToManyShould {


    private Map<Integer, List<Optional>> actual;
    private List<Tuple> expected;

    public PGOneToManyShould(Map<Integer, List<Optional>> actual, List<Tuple> expected) {
        this.actual = actual;
        this.expected = expected;
    }

    @Test
    public void putAnEmptyValueAndNotStoreAnyValue() throws Exception {
        PGOneToMany<Integer, Integer> pgOneToMany =
                new PGOneToMany<>();

        actual.forEach((key, optionals) ->
                optionals.forEach(optional ->
                        pgOneToMany.put(new PGKeyValue<>(key, optional))));

        assertThat(pgOneToMany.collect((a,b) -> new Tuple(a,b)), equalTo(expected));
    }

    @Parameters
    public static Collection<Object[]> data() {
        HashMap<Integer, List<Optional>> first = new HashMap<>();
        first.put(1, Arrays.asList(Optional.empty()));

        HashMap<Integer, List<Optional>> second = new HashMap<>();
        second.put(1, Arrays.asList(Optional.of(3)));

        HashMap<Integer, List<Optional>> third = new HashMap<>();
        third.put(1, Arrays.asList(Optional.of(3), Optional.of(4)));

        return Arrays.asList(new Object[][] {
                {new HashMap<Integer, List<Optional>>(), new ArrayList<>()},
                {first, Arrays.asList(new Tuple(1, new ArrayList<>()))},
                {second, Arrays.asList(new Tuple(1, Arrays.asList(3)))},
                {third, Arrays.asList(new Tuple(1, Arrays.asList(3, 4)))},
        });
    }


    private static class Tuple {
        public Integer first;
        public List<Integer> second;

        public Tuple(Integer first, List<Integer> second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Tuple tuple = (Tuple) o;

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
