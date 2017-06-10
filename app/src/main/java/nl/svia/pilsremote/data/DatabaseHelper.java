package nl.svia.pilsremote.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import nl.svia.pilsremote.adapters.PurchaseAdapter;
import nl.svia.pilsremote.misc.PurchaseModel;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "pilsremote.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TAG = "DatabaseHelper";

    private static DatabaseHelper helperInstance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: https://bit.ly/6LRzfx
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

    public int getLastBalance(int userId) {
        SQLiteDatabase db = getReadableDatabase();

        String[] selectionArgs = {Integer.toString(userId)};

        Cursor c = db.rawQuery("SELECT balance_before - (price * amount) AS last_balance, user_id " +
                "FROM Purchase " +
                "WHERE user_id= ?  " +
                "ORDER BY _id " +
                "DESC LIMIT 1", selectionArgs);

        int balance = 0;

        if (c.moveToFirst()) {
            balance = c.getInt(0);
        }

        c.close();

        return balance;
    }

    public void deletePurchase(int id) {
        SQLiteDatabase db = getWritableDatabase();

        String[] selectionArgs = {Integer.toString(id)};
        db.delete(PurchaseEntry.TABLE_NAME, PurchaseEntry._ID + " = ?", selectionArgs);
    }

    public ArrayList<PurchaseModel> getPurchases(int userId) {
        SQLiteDatabase db = getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                PurchaseEntry._ID,
                PurchaseEntry.COLUMN_PRODUCT,
                PurchaseEntry.COLUMN_AMOUNT,
                PurchaseEntry.COLUMN_PRICE,
                PurchaseEntry.COLUMN_OLD_BALANCE,
                PurchaseEntry.COLUMN_TIMESTAMP
        };

        // Filter results WHERE "title" = 'My Title'
        String selection = PurchaseEntry.COLUMN_USER + " = ?";
        String[] selectionArgs = {Integer.toString(userId)};

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                PurchaseEntry.COLUMN_TIMESTAMP + " DESC";

        Cursor c = db.query(
                PurchaseEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );


        int idxId = c.getColumnIndex(PurchaseEntry._ID);
        int idxProduct = c.getColumnIndex(PurchaseEntry.COLUMN_PRODUCT);
        int idxAmount = c.getColumnIndex(PurchaseEntry.COLUMN_AMOUNT);
        int idxPrice = c.getColumnIndex(PurchaseEntry.COLUMN_PRICE);
        int idxOldBalance = c.getColumnIndex(PurchaseEntry.COLUMN_OLD_BALANCE);
        int idxTimestamp = c.getColumnIndex(PurchaseEntry.COLUMN_TIMESTAMP);

        ArrayList<PurchaseModel> list = new ArrayList<>();
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            PurchaseModel model = new PurchaseModel(c.getInt(idxId),
                    c.getInt(idxProduct),
                    c.getInt(idxAmount),
                    c.getInt(idxPrice),
                    c.getInt(idxOldBalance),
                    c.getLong(idxTimestamp));

            list.add(model);

            Log.d(TAG, "Added: " + model.getProductId());
        }

        c.close();

        return list;
    }
}
