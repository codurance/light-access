package com.codurance.lightaccess.mapping;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class LAResultSetShould {

    private static final SimpleDateFormat YYYY_MM_DD_date_format = new SimpleDateFormat("yyyy-MM-dd");

    private static final LocalDate TODAY_LOCAL_DATE = LocalDate.of(2017, 07, 29);
    private static final java.sql.Date TODAY_SQL_DATE = localDateToSqlDate(TODAY_LOCAL_DATE);
    private static final java.util.Date TODAY_UTIL_DATE = sqlDateToUtilDate(TODAY_SQL_DATE);

    @Mock ResultSet resultSet;

    private LAResultSet laResultSet;

    @Before
    public void initialise() {
        laResultSet = new LAResultSet(resultSet);
    }

    @Test public void
    return_zero_when_int_field_is_null() throws SQLException {
        given(resultSet.getInt(1)).willReturn(0);

        assertThat(laResultSet.getInt(1)).isEqualTo(0);
    }

    @Test public void
    return_int_when_int_field_has_value() throws SQLException {
        given(resultSet.getInt(1)).willReturn(10);

        assertThat(laResultSet.getInt(1)).isEqualTo(10);
    }

    @Test public void
    return_empty_string_when_string_field_is_null() throws SQLException {
        given(resultSet.getString(1)).willReturn(null);

        assertThat(laResultSet.getString(1)).isEqualTo(null);
    }

    @Test public void
    return_string_when_string_field_has_value() throws SQLException {
        given(resultSet.getString(1)).willReturn("value");

        assertThat(laResultSet.getString(1)).isEqualTo("value");
    }

    @Test public void
    return_optional_empty_string_when_string_field_is_null() throws SQLException {
        given(resultSet.getString(1)).willReturn(null);

        assertThat(laResultSet.getOptionalString(1)).isEmpty();
    }

    @Test public void
    return_optional_string_when_string_field_has_value() throws SQLException {
        given(resultSet.getString(1)).willReturn("value");

        assertThat(laResultSet.getOptionalString(1)).contains("value");
    }

    @Test public void
    return_null_local_date_when_date_field_is_null() throws SQLException {
        given(resultSet.getDate(1)).willReturn(null);

        assertThat(laResultSet.getLocalDate(1)).isNull();
    }

    @Test public void
    return_local_date_when_date_field_has_value() throws SQLException {
        given(resultSet.getDate(1)).willReturn(TODAY_SQL_DATE);
        
        assertThat(laResultSet.getLocalDate(1)).isEqualTo(TODAY_LOCAL_DATE);
    }

    @Test public void
    return_null_util_date_when_date_field_is_null() throws SQLException {
        given(resultSet.getDate(1)).willReturn(null);

        assertThat(laResultSet.getDate(1)).isNull();
    }

    @Test public void
    return_date_when_date_field_has_value() throws SQLException {
        given(resultSet.getDate(1)).willReturn(TODAY_SQL_DATE);

        assertThat(laResultSet.getDate(1)).isEqualTo(TODAY_UTIL_DATE);
    }

    @Test public void
    return_optional_empty_local_date_when_date_field_is_null() throws SQLException {
        given(resultSet.getDate(1)).willReturn(null);

        Optional<LocalDate> date = laResultSet.getOptionalLocalDate(1);

        assertThat(date.isPresent()).isFalse();
    }

    @Test public void
    return_optional_local_date_when_date_field_has_value() throws SQLException {
        given(resultSet.getDate(1)).willReturn(TODAY_SQL_DATE);

        Optional<LocalDate> date = laResultSet.getOptionalLocalDate(1);

        assertThat(date).contains(TODAY_LOCAL_DATE);
    }

    @Test public void
    return_optional_empty_when_result_set_has_no_records() throws SQLException {
        given(resultSet.next()).willReturn(false);
        
        Optional<Person> person = laResultSet.onlyResult(this::toPerson);

        assertThat(person).isEmpty();
    }

    @Test public void
    return_optional_person_when_result_set_one_value() throws SQLException {
        Person aPerson = new Person(1, "Person");
        given(resultSet.next()).willReturn(true);
        given(resultSet.getInt(1)).willReturn(aPerson.id);
        given(resultSet.getString(2)).willReturn(aPerson.name);

        Optional<Person> personOptional = laResultSet.onlyResult(this::toPerson);

        assertThat(personOptional).contains(aPerson);
    }

    @Test public void
    return_empty_list_when_mapping_empty_result_set() throws SQLException {
        given(resultSet.next()).willReturn(false);

        List<Person> persons = laResultSet.mapResults(this::toPerson);

        assertThat(persons).isEmpty();
    }

    @Test public void
    return_list_of_entities_when_mapping_result_set() throws SQLException {
        Person person_1 = new Person(1, "Person 1");
        Person person_2 = new Person(2, "Person 2");
        given(resultSet.next()).willReturn(true, true, false);
        given(resultSet.getInt(1)).willReturn(person_1.id, person_2.id);
        given(resultSet.getString(2)).willReturn(person_1.name, person_2.name);

        List<Person> persons = laResultSet.mapResults(this::toPerson);

        assertThat(persons).containsExactlyInAnyOrder(person_1, person_2);
    }

    @Test public void
    return_a_normalised_one_to_many_structure_after_a_join_statement() throws SQLException {
        Person person_1 = new Person(1, "Person 1");
        Person person_2 = new Person(2, "Person 2");
        Person person_3 = new Person(3, "Person 3");
        Product product_1 = new Product(10, "product 10");
        Product product_2 = new Product(20, "product 20");

        given(resultSet.next()).willReturn(true, true, true, true, false);
        given(resultSet.getInt(1)).willReturn(person_1.id, person_1.id, person_2.id, person_3.id);
        given(resultSet.getString(2)).willReturn(person_1.name, person_1.name, person_2.name, person_3.name);
        given(resultSet.getInt(3)).willReturn(product_1.id, product_2.id, product_1.id, 0);
        given(resultSet.getString(4)).willReturn(product_1.description, product_2.description, product_1.description, null);

        OneToMany<Person, Product> persons = laResultSet.normaliseOneToMany(this::toPersonWithProducts);

        OneToMany<Person, Product> expected = new OneToMany<>();
        expected.put(keyValue(person_1, product_1));
        expected.put(keyValue(person_1, product_2));
        expected.put(keyValue(person_2, product_1));
        expected.put(keyValue(person_3, null));

        assertThat(persons).isEqualTo(expected);
    }

    @Test public void
    move_to_next_record() throws SQLException {
        laResultSet.nextRecord();

        verify(resultSet).next();
    }

    private KeyValue<Person, Optional<Product>> toPersonWithProducts(LAResultSet laResultSet) {
        int personId = laResultSet.getInt(1);
        String personName = laResultSet.getString(2);
        Person person = new Person(personId, personName);

        int productId = laResultSet.getInt(3);
        String productDescription = laResultSet.getString(4);
        Optional<Product> product = ofNullable((productId > 0) ? new Product(productId, productDescription) : null);

        return new KeyValue<>(person, product);
    }

    private KeyValue<Person, Optional<Product>> keyValue(Person person, Product product) {
        return new KeyValue<>(person, Optional.ofNullable(product));
    }

    private Person toPerson(LAResultSet laResultSet) {
        return new Person(laResultSet.getInt(1), laResultSet.getString(2));
    }

    private static java.util.Date sqlDateToUtilDate(java.sql.Date sqlDate) {
        return new java.util.Date(sqlDate.getTime());
    }

    private static LocalDate utilDateToLocalDate(java.util.Date date) {
        return (date != null) ? LocalDate.parse(YYYY_MM_DD_date_format.format(date)) : null;
    }

    private static java.sql.Date localDateToSqlDate(LocalDate localDate) {
        return Date.valueOf(localDate);
    }

    class Person {
        private final Integer id;
        private final String name;

        Person(Integer id, String name) {
            this.id = id;
            this.name = name;
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
            return "Person{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    class Product {
        private final Integer id;
        private final String description;

        Product(Integer id, String description) {
            this.id = id;
            this.description = description;
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
            return "Product{" +
                    "id=" + id +
                    ", description='" + description + '\'' +
                    '}';
        }
    }

}