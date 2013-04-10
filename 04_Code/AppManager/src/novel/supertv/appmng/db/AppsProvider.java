package novel.supertv.appmng.db;

import novel.supertv.appmng.Constants;
import novel.supertv.appmng.db.DatabaseHelper.ApplicationColumn;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class AppsProvider extends ContentProvider {
	
	private static final String TAG = "novel.supertv.appmng.db.AppsProvider";
	
	private static UriMatcher mUriMatcher;
	private static final int UriMatchApp = 1;
	private static final int UriMatchAppLimit = 2;
	private SQLiteOpenHelper mDatabaseHelper=null;

	@Override
	public boolean onCreate() {
		mDatabaseHelper = DatabaseHelper.getInstance(getContext());
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(Constants.AUTHORITY, DatabaseHelper.TABLE_APPS, UriMatchApp);
        mUriMatcher.addURI(Constants.AUTHORITY, DatabaseHelper.TABLE_APPS+"/"+DatabaseHelper.APPS_LIMIT, UriMatchAppLimit);
		return true;
	}
	

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Log.d(TAG, "---query--uri="+uri);
		SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
		Cursor cur = null;
		switch (mUriMatcher.match(uri)) {
		case UriMatchApp:
			cur = db.query(DatabaseHelper.TABLE_APPS, projection, selection,
					selectionArgs, null, null, sortOrder);
			break;
		case UriMatchAppLimit:
			int limit=10;
			cur = db.query(DatabaseHelper.TABLE_APPS, projection, selection,
					selectionArgs, null, null, sortOrder,""+limit);
			break;
		}
		return cur;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.d(TAG, "---insert--uri="+uri);
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
		Uri retUri = null;
		switch(mUriMatcher.match(uri)){
		case UriMatchApp:
			long rowId = db.insert(DatabaseHelper.TABLE_APPS, null, values);
			if (rowId > 0) {
            	retUri = ContentUris.withAppendedId(ApplicationColumn.CONTENT_URI, rowId);
                getContext().getContentResolver().notifyChange(retUri, null);
            } else {
                throw new SQLException("Failed to insert row into " + uri);
            }
			break;
		}
		return retUri;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		Log.d(TAG, "---delete--uri="+uri);
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
		int re = 0;
		switch (mUriMatcher.match(uri)) {
        case UriMatchApp:
            re = db.delete(DatabaseHelper.TABLE_APPS, selection, selectionArgs);
            break;
		}
		return re;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		Log.d(TAG, "---update--uri="+uri);
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
		int count = 0;
		switch (mUriMatcher.match(uri)) {
		case UriMatchApp:
	        count =db.update(DatabaseHelper.TABLE_APPS, values, selection, selectionArgs);
			break;
		}
		return count;
	}

}
