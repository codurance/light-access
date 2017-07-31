package integration.entities;

import java.time.LocalDate;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

public class WishList {

    private final Integer id;
    private final Integer userId;
    private final String name;
    private final LocalDate creationDate;

    public WishList(Integer id, Integer userId, String name, LocalDate creationDate) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.creationDate = creationDate;
    }

    public Integer id() {
        return id;
    }

    public Integer userId() {
        return userId;
    }

    public String name() {
        return name;
    }

    public LocalDate creationDate() {
        return creationDate;
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
        return "WishList{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", creationDate=" + creationDate +
                '}';
    }
}
