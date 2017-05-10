package nl.svia.pilsremote.misc;

public class PurchaseModel {
    private int mId;
    private int mProductId;
    private int mAmount;
    private double mPrice;
    private double mOldBalance;
    private long mTimestamp;

    public PurchaseModel(int id, int productId, int amount, int price, int oldBalance, long timestamp) {
        mId = id;
        mProductId = productId;
        mAmount = amount;
        mPrice = (double) (price) / 100;
        mOldBalance = (double) (oldBalance) / 100;
        mTimestamp = timestamp;
    }

    public int getId() {
        return mId;
    }

    public int getProductId() {
        return mProductId;
    }

    public int getAmount() {
        return mAmount;
    }

    public double getPrice() {
        return mPrice;
    }

    public double getOldBalance() {
        return mOldBalance;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        PurchaseModel p = (PurchaseModel) obj;

        return mId == p.getId();
    }

    @Override
    public int hashCode() {
//        int result = (int) (mId ^ (mId >>> 32));
//        result = 31 * result + (mName != null ? mName.hashCode() : 0);
//        return result;
        return mId;
    }

    @Override
    public String toString() {
        return "PurchaseModel: " + mId;
    }
}