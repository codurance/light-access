package integration.dtos;


public class ProductID {
    private int id;

    public ProductID(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProductID productID = (ProductID) o;

        return id == productID.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
