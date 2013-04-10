
package com.lenovo.settings.db;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ParentalControlOpenHelper extends SQLiteOpenHelper {

    private ParentalControlOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        Log.d(TAG, "this is DvbDatabaseHelper ");
    }

    private static final String TAG = "com.lenovo.settings.db.ParentalControlOpenHelper";
    public static final String DB_NAME = "applocks.db";

    /**
     * 数据库版本号，用于数据库升级
     */
    private static final int DB_VERSION = 20;

    /**
     * 构造，由Provider使用
     * 
     * @param context
     */
    private static ParentalControlOpenHelper databaseHelper = null;

    public static ParentalControlOpenHelper getInstance(Context context) {
        Log.d(TAG, "-----getInstance-----");
        if (databaseHelper == null) {
            databaseHelper = new ParentalControlOpenHelper(context);
        }
        return databaseHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create_sql = "CREATE TABLE " + AppInfo.TABLE_NAME + "( "
                + AppInfo.AppInfoColumns.ID + " integer PRIMARY KEY, "
                + AppInfo.AppInfoColumns.PACKAGE_NAME + " varchar, "
                + AppInfo.AppInfoColumns.PASSWORD + " varchar, "
                + AppInfo.AppInfoColumns.LOCK_FLAG + " integer " + ");";
        String insert_sql = " INSERT INTO " + AppInfo.TABLE_NAME + " ("
                + AppInfo.AppInfoColumns.PASSWORD + "," + AppInfo.AppInfoColumns.LOCK_FLAG + ") "
                + " VALUES ('" + "000000" + "', '" + 0 + "')";
        String insert_appstore_sql = " INSERT INTO " + AppInfo.TABLE_NAME + " ("
                + AppInfo.AppInfoColumns.PASSWORD + "," + AppInfo.AppInfoColumns.LOCK_FLAG + ","
                + AppInfo.AppInfoColumns.PACKAGE_NAME + ") "
                + " VALUES ('" + "000000" + "', '" + 1 + "', '" + "com.joysee.appstore" + "')";
        try {
            Log.d(TAG, "-------------onCreat()-------- create_sql  ");
            Log.d(TAG, "-------------onCreat()-------- insert_sql  ");
            db.execSQL(create_sql);
            db.execSQL(insert_sql);
            db.execSQL(insert_appstore_sql);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        if (oldVersion < 30) {
            db.execSQL("DROP TABLE IF EXISTS " + AppInfo.TABLE_NAME);
            onCreate(db);
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (db.isReadOnly()) {
            Log.d(TAG, "onOpen(), SQLite is opened read-only!!! db=" + db);
        }
    }
}
