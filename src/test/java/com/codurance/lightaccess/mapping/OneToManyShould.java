package com.codurance.lightaccess.mapping;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class OneToManyShould {

    private static final Company COMPANY_1 = new Company(1, "Company 1");
    private static final Company COMPANY_2 = new Company(2, "Company 2");
    private static final Company COMPANY_3 = new Company(2, "Company 3");

    private static final Contact JOHN = new Contact(10, "John");
    private static final Contact BRIAN = new Contact(10, "Brian");
    private static final Contact SALLY = new Contact(10, "Sally");


    @Test
    public void
    collect_data_into_a_list_of_objects_containing_key_and_values() {
        OneToMany<Company, Contact> oneToMany = new OneToMany<>();
        oneToMany.put(COMPANY_1, Optional.of(JOHN));
        oneToMany.put(COMPANY_1, Optional.of(BRIAN));
        oneToMany.put(COMPANY_2, Optional.of(SALLY));
        oneToMany.put(COMPANY_3, Optional.empty());

        List<CompanyWithContacts> companyWithContacts = oneToMany.collect(CompanyWithContacts::new);

        assertThat(companyWithContacts).containsExactlyInAnyOrder(
                new CompanyWithContacts(COMPANY_1, JOHN, BRIAN),
                new CompanyWithContacts(COMPANY_2, SALLY),
                new CompanyWithContacts(COMPANY_3));
    }

    static class Company {
        private final Integer id;
        private final String name;

        Company(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Company company = (Company) o;

            if (id != null ? !id.equals(company.id) : company.id != null) return false;
            return name != null ? name.equals(company.name) : company.name == null;
        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
        }
    }

    static class Contact {
        private final Integer id;
        private final String name;

        Contact(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Contact contact = (Contact) o;

            if (id != null ? !id.equals(contact.id) : contact.id != null) return false;
            return name != null ? name.equals(contact.name) : contact.name == null;
        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
        }
    }

    static class CompanyWithContacts {
        private Company company;
        private List<Contact> contacts = new ArrayList<>();

        CompanyWithContacts(Company company, Contact... contacts) {
            this(company, asList(contacts));
        }

        CompanyWithContacts(Company company, List<Contact> contacts) {
            this.company = company;
            this.contacts = contacts;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CompanyWithContacts that = (CompanyWithContacts) o;

            if (company != null ? !company.equals(that.company) : that.company != null) return false;
            return contacts != null ? contacts.equals(that.contacts) : that.contacts == null;
        }

        @Override
        public int hashCode() {
            int result = company != null ? company.hashCode() : 0;
            result = 31 * result + (contacts != null ? contacts.hashCode() : 0);
            return result;
        }
    }

}
