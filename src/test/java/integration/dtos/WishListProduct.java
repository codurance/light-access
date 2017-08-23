package integration.dtos;

import java.util.List;


public class WishListProduct {

    private final WishList wishList;
    private final List<Product> products;

    public WishListProduct(WishList wishList, List<Product> products) {
        this.wishList = wishList;
        this.products = products;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WishListProduct that = (WishListProduct) o;

        if (wishList != null ? !wishList.equals(that.wishList) : that.wishList != null) return false;
        return products != null ? products.equals(that.products) : that.products == null;
    }

    @Override
    public int hashCode() {
        int result = wishList != null ? wishList.hashCode() : 0;
        result = 31 * result + (products != null ? products.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "WithListProduct{" +
                "wishList=" + wishList +
                ", products=" + products +
                '}';
    }
}
