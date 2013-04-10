package com.lenovo.settings.db;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;

public class ParentalControlProvider extends ContentProvider {
    private ParentalControlOpenHelper mOpenHelper;
    private String TAG = "com.lenovo.settings.db.ParentalControlProvider";
    @Override
    public boolean onCreate() {
        Log.d(TAG, "---------onCreate------");
        mOpenHelper = ParentalControlOpenHelper.getInstance(getContext());
//        mOpenHelper = new ParentalControlOpenHelper(getContext());
        return true;
    }
    @Override
    public ContentProviderResult[] applyBatch(
            ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();// 开始事务
        try {
            ContentProviderResult[] results = super.applyBatch(operations);
            db.setTransactionSuccessful();// 设置事务标记为successful
            return results;
        } finally {
            db.endTransaction();// 结束事务
        }
    }
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        Log.d(TAG, " ----query uri = " + uri);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Cursor cursor = null;
        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
        qBuilder.setTables(AppInfo.TABLE_NAME);
        cursor = qBuilder.query(db,
                projection, selection, selectionArgs, null, null,
                sortOrder, null);

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(TAG, " ----insert uri = " + uri);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long result = -1;
        Uri uri0 = null;
        result = db.insert(AppInfo.TABLE_NAME, null, values);
        if (result > 0) {
            uri0 = Uri.withAppendedPath(uri, "" + result);
            getContext().getContentResolver().notifyChange(uri0, null);
            return uri0;
        }
        throw new SQLException("Failed to insert row into " + uri0);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, " ----delete uri = " + uri);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int result = -1;
        result = db.delete(AppInfo.TABLE_NAME,
                selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return result;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.d(TAG, " ----update uri = " + uri);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = -1;
        count = db.update(AppInfo.TABLE_NAME, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

}
