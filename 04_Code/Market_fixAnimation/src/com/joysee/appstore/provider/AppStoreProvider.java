package com.joysee.appstore.provider;

import java.util.ArrayList;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.joysee.appstore.db.DatabaseHelper;
import com.joysee.appstore.db.DatabaseHelper.DownloadTaskColumn;
import com.joysee.appstore.db.DatabaseHelper.DownloadThreadColumn;

public class AppStoreProvider extends ContentProvider {

	private static final String TAG = "com.joysee.appstore.AppStoreProvider";

	public static final String AUTHORITY = "com.joysee.appstore.authority";
	
	private static final int UriMatchTask = 1;
	private static final int UriMatchThread = 2;
	private static final int UriMatchApplication = 3;
	private static final int UriMatchTaskComplete = 4;

	private static UriMatcher mUriMatcher;

	private SQLiteOpenHelper mDatabaseHelper=null;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
		int re = 0;
		try{
//			waitDBLock(db);
        switch (mUriMatcher.match(uri)) {
            case UriMatchTask:
                re = db.delete(DatabaseHelper.TABLE_TASK, selection, selectionArgs);
                break;
            case UriMatchThread:
                re = db.delete(DatabaseHelper.TABLE_THREAD, selection, selectionArgs);
                break;
            case UriMatchApplication:
                re = db.delete(DatabaseHelper.TABLE_APPLICATIONS, selection, selectionArgs);
                break;
            default:
                break;
        }
        }finally{
//        	if(db!=null){
//        		db.close();
//        	}
        }
        return re;
	}

	@Override
	public String getType(Uri arg0) {
		//
		return null;
	}

	@Override
	public boolean onCreate() {
		mDatabaseHelper = DatabaseHelper.getInstance(getContext());
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(AUTHORITY, DatabaseHelper.TABLE_TASK, UriMatchTask);
        mUriMatcher.addURI(AUTHORITY, DatabaseHelper.TABLE_THREAD, UriMatchThread);
        mUriMatcher.addURI(AUTHORITY, DatabaseHelper.TABLE_APPLICATIONS, UriMatchApplication);
        mUriMatcher.addURI(AUTHORITY, DatabaseHelper.VIRTURAL_TASK_COMPLETE, UriMatchTaskComplete);
        return true;
	}
	
	 /**
     * wait database lock
     * @param db
     */
    public void waitDBLock(SQLiteDatabase db){
    	while(db.isDbLockedByOtherThreads()){
    		try {
				Thread.sleep(6);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    }
	
	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
		Uri retUri = null;
		try{
//			waitDBLock(db);
		switch (mUriMatcher.match(arg0)) {
		case UriMatchTask:
			long rowId = db.insert(DatabaseHelper.TABLE_TASK, null, arg1);
            if (rowId > 0) {
            	retUri = ContentUris.withAppendedId(DownloadTaskColumn.CONTENT_URI, rowId);
                getContext().getContentResolver().notifyChange(retUri, null);
            } else {
                throw new SQLException("Failed to insert row into " + arg0);
            }
			break;
		case UriMatchThread:
			long id =db.insert(DatabaseHelper.TABLE_THREAD, null, arg1);
			if (id > 0) {
            	retUri = ContentUris.withAppendedId(DownloadThreadColumn.CONTENT_URI, id);
                getContext().getContentResolver().notifyChange(retUri, null);
            } else {
                throw new SQLException("Failed to insert row into " + arg0);
            }
			break;
		case UriMatchApplication:
			long appId =db.insert(DatabaseHelper.TABLE_APPLICATIONS, null, arg1);
			if (appId > 0) {
            	retUri = ContentUris.withAppendedId(DownloadThreadColumn.CONTENT_URI,appId);
                getContext().getContentResolver().notifyChange(retUri, null);
            } else {
                throw new SQLException("Failed to insert row into " + arg0);
            }
			break;
		default:
			break;
		}
		}finally{
//			if(db!=null){
//				db.close();
//			}
		}
		return retUri;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
		Cursor cur = null;
		try{
		switch (mUriMatcher.match(uri)) {
		case UriMatchTask:
			cur = db.query(DatabaseHelper.TABLE_TASK, projection, selection,
					selectionArgs, null, null, sortOrder);
			break;
		case UriMatchThread:
			cur = db.query(DatabaseHelper.TABLE_THREAD, projection, selection,
					selectionArgs, null, null, sortOrder);
			break;
		case UriMatchApplication:
			cur = db.query(DatabaseHelper.TABLE_APPLICATIONS, projection, selection,
					selectionArgs, null, null, sortOrder);
			break;
		case UriMatchTaskComplete:
//			String sql = "select "+ DownloadThreadColumn.TASK_ID + ", sum(position) from "+DatabaseHelper.TABLE_THREAD +"group by "+DownloadThreadColumn.TASK_ID ;
//			cur = db.rawQuery(sql, selectionArgs);
			String whereClause = DownloadTaskColumn.SUMSIZE+" = "+ DownloadTaskColumn.DOWNLOADSIZE;
			cur = db.query(DatabaseHelper.TABLE_TASK, null, whereClause, null, null, null, null);
			// add by dingran
//			cur.setNotificationUri(getContext().getContentResolver(), uri)
		default:
			break;
		}
		}finally{
//			if(db!=null){
//				db.close();
//			}
		}
		return cur;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
		int count = 0;
		try{
//			waitDBLock(db);
		switch (mUriMatcher.match(uri)) {
		case UriMatchTask:
	        count =db.update(DatabaseHelper.TABLE_TASK, values, selection, selectionArgs);
			break;
		case UriMatchThread:
			count=db.update(DatabaseHelper.TABLE_THREAD, values, selection, selectionArgs);
			break;
		case UriMatchApplication:
			count=db.update(DatabaseHelper.TABLE_APPLICATIONS, values, selection, selectionArgs);
			break;
		default:
			break;
		}
		}finally{
//			if(db!=null){
//				db.close();
//			}
		}
		return count;
	}

	@Override
    public ContentProviderResult[] applyBatch(
            ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();  
        db.beginTransaction();//开始事务  
        try{  
            ContentProviderResult[]results = super.applyBatch(operations);  
            db.setTransactionSuccessful();//设置事务标记为successful
            return results;
        }finally{
            db.endTransaction();//结束事务
        }
    }
}
