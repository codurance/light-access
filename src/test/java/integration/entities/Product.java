package integration.entities;

import java.time.LocalDate;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

public class Product {
    private int id;
    private String name;
    private LocalDate date;

    public Product(int id, String name, LocalDate date) {
        this.id = id;
        this.name = name;
        this.date = date;
    }

    public Product(int id, String name) {
        this(id, name, LocalDate.of(2017, 07, 29));
    }

    public int id() {
        return id;
    }

    public String name() {
        return name;
    }

    public LocalDate date() {
        return date;
    }

    @Override
    public boolean equals(Object o) {
        return reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", date=" + date +
                '}';
    }
}
