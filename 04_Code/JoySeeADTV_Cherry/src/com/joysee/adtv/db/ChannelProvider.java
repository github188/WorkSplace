package com.joysee.adtv.db;

import java.util.ArrayList;

import com.joysee.adtv.common.DvbLog;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class ChannelProvider extends ContentProvider {

    private static final DvbLog log = new DvbLog(
            "com.joysee.adtv.db.ChannelProvider",DvbLog.DebugType.D);
    private DvbDatabaseHelper mOpenHelper;
    public Context context;
    private static final int TABLE_RESERVES = 5;
    private static final int TABLE_SERVICE_TYPE = 6;
    private static final UriMatcher uriMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(Channel.AUTHORITY, DvbDataContent.TABLE_RESERVES,
                TABLE_RESERVES);
        uriMatcher.addURI(Channel.AUTHORITY, DvbDataContent.TABLE_SERVICE_TYPE,
                TABLE_SERVICE_TYPE);
    }

    @Override
    public boolean onCreate() {
        log.D("onCreate()");
        mOpenHelper = DvbDatabaseHelper.getInstance(getContext(), "dvb");
        context = this.getContext();
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
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        log.D("delete " + uri);
        int match = uriMatcher.match(uri);
        log.D("match rst " + match);
        int result = -1;
        String tableName = "";
        switch (match) {
        case TABLE_RESERVES:
            tableName = DvbDataContent.TABLE_RESERVES;
            break;
        case TABLE_SERVICE_TYPE:
            tableName = DvbDataContent.TABLE_SERVICE_TYPE;
            break;
        default:
        	throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        result =  db.delete(tableName, 
        		selection, selectionArgs);
        return result;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        log.D("insert " + uri);
        int match = uriMatcher.match(uri);
        log.D("match rst " + match);
        long result = -1;
        String tableName = "";
        Uri u = null;
        switch (match) {
        case TABLE_RESERVES:
            tableName = DvbDataContent.TABLE_RESERVES;
            if(tableName.equals("") == false){
                result = db.insert( tableName, null, values);
            }
            u = Uri.withAppendedPath(uri, "" + result);
            break;
        case TABLE_SERVICE_TYPE:
            tableName = DvbDataContent.TABLE_SERVICE_TYPE;
            if(tableName.equals("") == false){
                result = db.insert( tableName, null, values);
            }
            u = Uri.withAppendedPath(uri, "" + result);
            break;
        default:
        	throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        
        return u;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        log.D("query " + uri);
        int match = uriMatcher.match(uri);
        log.D("match rst " + match);
        Cursor cursor = null;
        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
        switch (match) {
        case TABLE_RESERVES:
            qBuilder.setTables(DvbDataContent.TABLE_RESERVES);
            break;
        case TABLE_SERVICE_TYPE:
            qBuilder.setTables(DvbDataContent.TABLE_SERVICE_TYPE);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        cursor = qBuilder.query(db,
                projection, selection, selectionArgs, null, null,
                sortOrder, null);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        log.D("update " + uri);
        int match = uriMatcher.match(uri);
        log.D("match rst " + match);
        
        int result = 0;
        String tableName = "";
        switch (match) {
        case TABLE_RESERVES:
            tableName = DvbDataContent.TABLE_RESERVES;
            break;
        case TABLE_SERVICE_TYPE:
            tableName = DvbDataContent.TABLE_SERVICE_TYPE;
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        
        result = db.update(tableName, values, selection, selectionArgs);
        return result;
    }
}
