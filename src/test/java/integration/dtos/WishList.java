package integration.dtos;

import java.time.LocalDate;


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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WishList wishList = (WishList) o;

        if (id != null ? !id.equals(wishList.id) : wishList.id != null) return false;
        if (userId != null ? !userId.equals(wishList.userId) : wishList.userId != null) return false;
        if (name != null ? !name.equals(wishList.name) : wishList.name != null) return false;
        return creationDate != null ? creationDate.equals(wishList.creationDate) : wishList.creationDate == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        return result;
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
