package com.lenovo.settings.db;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;

public class DBUtil {
    private static DBUtil mDbUtil = null;
    private String TAG = "DBUtil";
    private DBUtil(){
    }
    private static Context mContext;

    public static DBUtil getInstance(Context context) {
        if (mDbUtil == null) {
            mDbUtil = new DBUtil();
        }
        mContext = context;
        return mDbUtil;
    }
    /**
     * 调用 ContentProvider 的更新接口
     * @param recordNo 第几条
     * @param password 密码
     * @param lockstate 锁状态
     */
    public void updateRecord(int recordNo, String password, int lockstate) {
        Uri uri = ContentUris.withAppendedId(AppInfo.AppInfoColumns.CONTENT_URI, recordNo);
        ContentValues values = new ContentValues();
        values.put(AppInfo.AppInfoColumns.PASSWORD, password);
        values.put(AppInfo.AppInfoColumns.LOCK_FLAG, lockstate);
        String where = AppInfo.AppInfoColumns.ID + "=?";
        String[] selectionArgs = {
                "" + recordNo
        };
        mContext.getContentResolver().update(uri, values, where, selectionArgs);
    }
    /**
     * 调用 ContentProvider 的更新接口
     * @param password 密码
     */
    public void updateRecord(String password) {
        Uri uri = AppInfo.AppInfoColumns.CONTENT_URI;
        ContentValues values = new ContentValues();
        values.put(AppInfo.AppInfoColumns.PASSWORD, password);
        mContext.getContentResolver().update(uri, values, null, null);
    }

    /**
     * 调用 ContentProvider 的查询接口 获取所有数据
     * @return
     */
    public ArrayList<AppInfoBean> getAllAppRecord() {
        ArrayList<AppInfoBean> arrayList = new ArrayList<AppInfoBean>();
        String[] columns = new String[] {
                AppInfo.AppInfoColumns.ID, AppInfo.AppInfoColumns.PACKAGE_NAME,
                AppInfo.AppInfoColumns.PASSWORD,
                AppInfo.AppInfoColumns.LOCK_FLAG,
        };
        Uri contacts = AppInfo.AppInfoColumns.CONTENT_URI;
        Cursor cur = mContext.getContentResolver().query(contacts, columns, null, null, null);
        if (cur.moveToFirst()) {
            while (cur.getPosition() != cur.getCount()) {
                AppInfoBean appinfo = new AppInfoBean();
                appinfo.ID = cur.getInt(cur.getColumnIndex(AppInfo.AppInfoColumns.ID));
                appinfo.packageName = cur.getString(cur
                        .getColumnIndex(AppInfo.AppInfoColumns.PACKAGE_NAME));
                appinfo.password = cur.getString(cur
                        .getColumnIndex(AppInfo.AppInfoColumns.PASSWORD));
                appinfo.lockState = cur
                        .getInt(cur.getColumnIndex(AppInfo.AppInfoColumns.LOCK_FLAG));
                arrayList.add(appinfo);
                cur.moveToNext();
            }
        }
        cur.close();
        Log.d(TAG, "------ getAllAppRecord size = " + arrayList.size());
        return arrayList;
    }
    
    public int insertRecord(String packageName, String password, int lockstate) {
        ContentValues values = new ContentValues();
        values.put(AppInfo.AppInfoColumns.PACKAGE_NAME, packageName);
        values.put(AppInfo.AppInfoColumns.PASSWORD, password);
        values.put(AppInfo.AppInfoColumns.LOCK_FLAG, lockstate);
        Uri uri = mContext.getContentResolver().insert(AppInfo.AppInfoColumns.CONTENT_URI, values);
        Log.d(TAG, " ----- insertRecord uri = " + uri);
        return (int) ContentUris.parseId(uri);
    }

    public void dropTable(String packageName) {
        String where = AppInfo.AppInfoColumns.PACKAGE_NAME + "=?";
        String[] selectionArgs = {
                packageName
        };
        mContext.getContentResolver().delete(AppInfo.AppInfoColumns.CONTENT_URI, where,
                selectionArgs);
    }
}
