package integration.dtos;

import java.util.List;

import static java.util.Collections.unmodifiableList;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

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
    public boolean equals(Object other) {
        return reflectionEquals(this, other);
    }

    @Override
    public int hashCode() {
        return reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return "UserWithWishList{" +
                "user=" + user +
                ", wishLists=" + wishLists +
                '}';
    }
}
