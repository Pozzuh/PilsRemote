package nl.svia.pilsremote.misc;

public class ProductModel {
    private int mId;
    private String mName;
    private double mPrice;

    public ProductModel(int id, String name, double price) {
        mId = id;
        mName = name;
        mPrice = price;
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public double getPrice() {
        return mPrice;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        ProductModel product = (ProductModel) obj;

        if (mId != product.mId) {
            return false;
        }

        if (mPrice != product.mPrice) {
            return false;
        }

        return mName != null ? mName.equals(product.mName) : product.mName == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (mId ^ (mId >>> 32));
        result = 31 * result + (mName != null ? mName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ProductModel: " + mName + ", " + mId + ", €" + mPrice;
    }
}