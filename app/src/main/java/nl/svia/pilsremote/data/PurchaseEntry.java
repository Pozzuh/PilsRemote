package nl.svia.pilsremote.data;

import android.provider.BaseColumns;

public class PurchaseEntry implements BaseColumns {
    public static final String TABLE_NAME = "purchase";

    // Purchaser
    public static final String COLUMN_USER = "user_id";

    // Id of the product purchased
    public static final String COLUMN_PRODUCT = "product_id";

    // Amount of items purchased
    public static final String COLUMN_AMOUNT = "amount";

    // Total price of purchase
    public static final String COLUMN_PRICE = "price";

    // Balance BEFORE the purchase
    public static final String COLUMN_OLD_BALANCE = "balance_before";

    // Moment of purchase
    public static final String COLUMN_TIMESTAMP = "time";
}
