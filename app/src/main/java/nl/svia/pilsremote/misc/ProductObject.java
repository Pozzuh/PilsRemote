package nl.svia.pilsremote.misc;

public class ProductObject {
    private int mId;
    private String mName;
    private double mPrice;

    public ProductObject(int id, String name, double mPrice) {
        mId = id;
        mName = name;
        mPrice = mPrice;
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

        ProductObject product = (ProductObject) obj;

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
        return "ProductObject: " + mName + ", " + mId + ", €" + mPrice;
    }
}