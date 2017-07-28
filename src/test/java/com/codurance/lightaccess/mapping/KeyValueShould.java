package com.codurance.lightaccess.mapping;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyValueShould {

    @Test public void
    return_key_and_value() {
        KeyValue<Integer, String> keyValue = new KeyValue<>(1, "Value");

        assertThat(keyValue.getKey()).isEqualTo(1);
        assertThat(keyValue.getValue()).isEqualTo("Value");
    }

    @Test(expected = UnsupportedOperationException.class) public void
    throw_unsupported_operation_exception_if_mutation_is_attempted() {
        KeyValue<Integer, String> keyValue = new KeyValue<>(1, "Value");

        keyValue.setValue("");
    }

}