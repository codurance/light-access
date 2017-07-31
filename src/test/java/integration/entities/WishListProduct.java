package integration.entities;

import java.util.List;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

public class WishListProduct {

    private final WishList wishList;
    private final List<Product> products;

    public WishListProduct(WishList wishList, List<Product> products) {
        this.wishList = wishList;
        this.products = products;
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
        return "WithListProduct{" +
                "wishList=" + wishList +
                ", products=" + products +
                '}';
    }
}
