package integration.dtos;

import java.util.List;

import static java.util.Collections.unmodifiableList;

public class UserWithWishList {

    private final User user;
    private final List<WishList> wishLists;

    public UserWithWishList(User user, List<WishList> wishLists) {
        this.user = user;
        this.wishLists = unmodifiableList(wishLists);
    }

    public User user() {
        return user;
    }

    public List<WishList> wishLists() {
        return unmodifiableList(wishLists);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserWithWishList that = (UserWithWishList) o;

        if (user != null ? !user.equals(that.user) : that.user != null) return false;
        return wishLists != null ? wishLists.equals(that.wishLists) : that.wishLists == null;
    }

    @Override
    public int hashCode() {
        int result = user != null ? user.hashCode() : 0;
        result = 31 * result + (wishLists != null ? wishLists.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UserWithWishList{" +
                "user=" + user +
                ", wishLists=" + wishLists +
                '}';
    }
}
