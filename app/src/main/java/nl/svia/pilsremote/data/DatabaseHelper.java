package nl.svia.pilsremote.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "pilsremote.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TAG = "DatabaseHelper";

    private static DatabaseHelper helperInstance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (helperInstance == null) {
            helperInstance = new DatabaseHelper(context.getApplicationContext());
        }

        return helperInstance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_PURCHASE_TABLE = "CREATE TABLE " + PurchaseEntry.TABLE_NAME + " (" +
                PurchaseEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                PurchaseEntry.COLUMN_USER + " INTEGER NOT NULL, " +
                PurchaseEntry.COLUMN_PRODUCT + " INTEGER NOT NULL, " +
                PurchaseEntry.COLUMN_AMOUNT + " INTEGER NOT NULL, " +
                PurchaseEntry.COLUMN_PRICE + " INTEGER NOT NULL, " +
                PurchaseEntry.COLUMN_OLD_BALANCE + " INTEGER NOT NULL, " +
                PurchaseEntry.COLUMN_TIMESTAMP + " INTEGER NOT NULL " +
                " );";

        db.execSQL(SQL_CREATE_PURCHASE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // TODO implement this when actually changing the database setup
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PurchaseEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    // Insert a post into the database
    public void addPurchase(PurchaseDbModel purchase) {
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(PurchaseEntry.COLUMN_USER, purchase.userId);
            values.put(PurchaseEntry.COLUMN_PRODUCT, purchase.productId);
            values.put(PurchaseEntry.COLUMN_AMOUNT, purchase.amount);
            values.put(PurchaseEntry.COLUMN_PRICE, purchase.price);
            values.put(PurchaseEntry.COLUMN_OLD_BALANCE, purchase.balance);
            values.put(PurchaseEntry.COLUMN_TIMESTAMP, System.currentTimeMillis());

            db.insertOrThrow(PurchaseEntry.TABLE_NAME, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add post to database");
        } finally {
            db.endTransaction();
        }
    }
}
