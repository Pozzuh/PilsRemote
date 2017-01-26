package nl.svia.pilsremote.misc;

import android.os.Bundle;

public class ProductModel {
    private static final String BUNDLE_ID = "PRODUCT_ID";
    private static final String BUNDLE_NAME = "PRODUCT_NAME";
    private static final String BUNDLE_PRICE = "PRODUCT_PRICE";

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

    public Bundle getBundle() {
        Bundle b = new Bundle();

        b.putInt(BUNDLE_ID, this.mId);
        b.putString(BUNDLE_NAME, this.mName);
        b.putDouble(BUNDLE_PRICE, this.mPrice);

        return b;
    }

    public static ProductModel fromBundle(Bundle b) {
        int id = b.getInt(BUNDLE_ID);
        String name = b.getString(BUNDLE_NAME);
        double price = b.getDouble(BUNDLE_PRICE);

        return new ProductModel(id, name, price);
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