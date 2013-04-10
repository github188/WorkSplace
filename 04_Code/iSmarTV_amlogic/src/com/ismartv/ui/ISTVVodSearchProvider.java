package com.ismartv.ui;

import android.content.ClipDescription;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.ContentProvider.PipeDataWriter;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.LiveFolders;
import android.text.TextUtils;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import android.net.Uri;
import android.provider.BaseColumns;

import com.ismartv.doc.ISTVDoc;
import com.ismartv.doc.ISTVVodSearchDoc;
import com.ismartv.client.*;
import com.ismartv.service.*;
import java.util.ArrayList;
import com.ismartv.doc.ISTVResource;
import android.database.MergeCursor;

public class ISTVVodSearchProvider extends ContentProvider implements ISTVDoc.Callback{
	private static final String TAG = "ISTVVodSearchProvider";

	private ISTVVodSearchDoc doc = null;
	private String mKeyWord = "";
	private	String mCategory = "";
	private	int mPageIndex = -1;
	private ArrayList<HashMap<String, Object>> searchMap = new ArrayList<HashMap<String, Object>>();
	private ArrayList<String> hotwords = new ArrayList<String>();
	private ArrayList<String> suggests = new ArrayList<String>();
	private boolean mFinish = false;
	
	private static final String DATABASE_NAME = "vodsearch.db";

	private static final int DATABASE_VERSION = 2;
	
	private static HashMap<String, String> sVODProjectionMap;		
	
	private static HashMap<String, String> sHotWordsProjectionMap;
	
	private static HashMap<String, String> sSuggestWordsProjectionMap;

    private static final int VIDEOS = 1;

    private static final int VIDEOS_ID = 2;
	
	private static final UriMatcher sUriMatcher;

	private DatabaseHelper mOpenHelper;
	
	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(Videos.AUTHORITY, "search", VIDEOS);
		sUriMatcher.addURI(Videos.AUTHORITY, "search/#", VIDEOS_ID);
		
		sVODProjectionMap = new HashMap<String, String>();
		sVODProjectionMap.put(Videos._ID, Videos._ID);
		sVODProjectionMap.put(Videos._COUNT, Videos._COUNT);
		sVODProjectionMap.put(Videos.COLUMN_NAME_CATEGORY, Videos.COLUMN_NAME_CATEGORY);
		sVODProjectionMap.put(Videos.COLUMN_NAME_NAME, Videos.COLUMN_NAME_NAME); 
		sVODProjectionMap.put(Videos.COLUMN_NAME_FOCUS, Videos.COLUMN_NAME_FOCUS); 
		sVODProjectionMap.put(Videos.COLUMN_NAME_DETAIL_URL, Videos.COLUMN_NAME_DETAIL_URL); 
		sVODProjectionMap.put(Videos.COLUMN_NAME_IMAGE_URL, Videos.COLUMN_NAME_IMAGE_URL); 
		sVODProjectionMap.put(Videos.COLUMN_NAME_POSITION, Videos.COLUMN_NAME_POSITION); 
		sVODProjectionMap.put(Videos.COLUMN_NAME_IS_COMPLEX, Videos.COLUMN_NAME_IS_COMPLEX); 
		sVODProjectionMap.put(Videos.COLUMN_NAME_IS_HD, Videos.COLUMN_NAME_IS_HD); 
		sVODProjectionMap.put(Videos.COLUMN_NAME_PRICE, Videos.COLUMN_NAME_PRICE); 
		
		sHotWordsProjectionMap = new HashMap<String, String>();
		sHotWordsProjectionMap.put(Videos._ID, Videos._ID);
		sHotWordsProjectionMap.put(Videos.COLUMN_NAME_NAME, Videos.COLUMN_NAME_NAME + " AS hotwords");
		sHotWordsProjectionMap.put(Videos.COLUMN_NAME_CATEGORY, Videos.COLUMN_NAME_CATEGORY);
		sHotWordsProjectionMap.put(Videos.COLUMN_NAME_FOCUS, Videos.COLUMN_NAME_FOCUS); 
		sHotWordsProjectionMap.put(Videos.COLUMN_NAME_DETAIL_URL, Videos.COLUMN_NAME_DETAIL_URL); 
		sHotWordsProjectionMap.put(Videos.COLUMN_NAME_IMAGE_URL, Videos.COLUMN_NAME_IMAGE_URL); 
		sHotWordsProjectionMap.put(Videos.COLUMN_NAME_POSITION, Videos.COLUMN_NAME_POSITION); 
		sHotWordsProjectionMap.put(Videos.COLUMN_NAME_IS_COMPLEX, Videos.COLUMN_NAME_IS_COMPLEX); 
		sHotWordsProjectionMap.put(Videos.COLUMN_NAME_IS_HD, Videos.COLUMN_NAME_IS_HD); 
		sHotWordsProjectionMap.put(Videos.COLUMN_NAME_PRICE, Videos.COLUMN_NAME_PRICE); 		
		
		sSuggestWordsProjectionMap = new HashMap<String, String>();
		sSuggestWordsProjectionMap.put(Videos._ID, Videos._ID);
		sSuggestWordsProjectionMap.put(Videos.COLUMN_NAME_NAME, Videos.COLUMN_NAME_NAME + " AS suggest_text_1");
		sSuggestWordsProjectionMap.put(Videos.COLUMN_NAME_CATEGORY, Videos.COLUMN_NAME_CATEGORY);
		sSuggestWordsProjectionMap.put(Videos.COLUMN_NAME_FOCUS, Videos.COLUMN_NAME_FOCUS); 
		sSuggestWordsProjectionMap.put(Videos.COLUMN_NAME_DETAIL_URL, Videos.COLUMN_NAME_DETAIL_URL); 
		sSuggestWordsProjectionMap.put(Videos.COLUMN_NAME_IMAGE_URL, Videos.COLUMN_NAME_IMAGE_URL); 
		sSuggestWordsProjectionMap.put(Videos.COLUMN_NAME_POSITION, Videos.COLUMN_NAME_POSITION); 
		sSuggestWordsProjectionMap.put(Videos.COLUMN_NAME_IS_COMPLEX, Videos.COLUMN_NAME_IS_COMPLEX); 
		sSuggestWordsProjectionMap.put(Videos.COLUMN_NAME_IS_HD, Videos.COLUMN_NAME_IS_HD); 
		sSuggestWordsProjectionMap.put(Videos.COLUMN_NAME_PRICE, Videos.COLUMN_NAME_PRICE); 				
	}
	
	static class DatabaseHelper extends SQLiteOpenHelper {
	   DatabaseHelper(Context context) {
		   super(context, DATABASE_NAME, null, DATABASE_VERSION);
	   }

	   public void onCreate(SQLiteDatabase db) {
		   db.execSQL("CREATE TABLE " + Videos.TABLE_NAME + " ("
				   + Videos._ID + " TEXT PRIMARY KEY,"
				   + Videos._COUNT + " TEXT,"
				   + Videos.COLUMN_NAME_CATEGORY + " TEXT,"
				   + Videos.COLUMN_NAME_NAME + " TEXT,"
				   + Videos.COLUMN_NAME_FOCUS + " TEXT,"
				   + Videos.COLUMN_NAME_DETAIL_URL + " TEXT,"
				   + Videos.COLUMN_NAME_IMAGE_URL + " TEXT,"
				   + Videos.COLUMN_NAME_POSITION + " TEXT,"
				   + Videos.COLUMN_NAME_IS_COMPLEX + " TEXT,"
				   + Videos.COLUMN_NAME_IS_HD + " TEXT,"
				   + Videos.COLUMN_NAME_PRICE + " TEXT"
				   + ");");
	   }

	   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		   // Logs that the database is being upgraded
		   Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
				   + newVersion + ", which will destroy all old data");
		   // Kills the table and existing data
		   db.execSQL("DROP TABLE IF EXISTS videos");
		   // Recreates the database with a new version
		   onCreate(db);
	   }
	}

	public boolean onCreate() {
	   // Creates a new helper object. Note that the database itself isn't opened until
	   // something tries to access it, and it's only created if it doesn't already exist.
	   mOpenHelper = new DatabaseHelper(getContext());
	   // Assumes that any failures will be reported by a thrown exception.
	   return true;
	}

	public void onGotResource(ISTVResource res){
		switch(res.getType()) {
			case ISTVVodSearchDoc.RES_INT_SEARCH_RESULT:
				HashMap<String, Object> hashMap = res.getSearchMap();
				ContentValues values = new ContentValues();
				values.put(Videos.COLUMN_NAME_CATEGORY, (String)hashMap.get("content_model"));
				values.put(Videos._COUNT, (String)hashMap.get("count"));
				values.put(Videos.COLUMN_NAME_DETAIL_URL, (String)hashMap.get("url"));
				values.put(Videos.COLUMN_NAME_FOCUS, (String)hashMap.get("focus"));
				values.put(Videos._ID, (String)hashMap.get("pk"));
				values.put(Videos.COLUMN_NAME_IMAGE_URL, (String)hashMap.get("adlet_url"));
				values.put(Videos.COLUMN_NAME_IS_COMPLEX, (String)hashMap.get("is_complex"));
				values.put(Videos.COLUMN_NAME_IS_HD, (String)hashMap.get("is_hd"));
				values.put(Videos.COLUMN_NAME_NAME, (String)hashMap.get("title"));
				values.put(Videos.COLUMN_NAME_POSITION, (String)hashMap.get("position"));
				values.put(Videos.COLUMN_NAME_PRICE, (String)hashMap.get("price"));				
				Log.d(TAG, "RES_INT_SEARCH_RESULT values=" + values.toString());
				delete(Videos.CONTENT_URI, Videos._ID + " = ?", new String[]{(String)hashMap.get("pk")});
				insert(Videos.CONTENT_URI, values);
				break;
			case ISTVVodSearchDoc.RES_INT_SEARCH_END:
				HashMap<String, Object> hashMap1 = res.getSearchMap();				
				for(HashMap<String, Object> hashMap2 : searchMap) {
					String content = (String)hashMap1.get("content_model");
					String category = (String)hashMap2.get("category");										
					Log.d(TAG, "RES_INT_SEARCH_END content="+content+",category="+category);
					if(category.equals(content)) {
						hashMap2.put("count", hashMap1.get("count"));
						hashMap2.put("end", 1);					
						Log.d(TAG, "search end = " + content);
					}
				}
				break;
			case ISTVVodSearchDoc.RES_STR_HOTWORDS_RESULT:
				hotwords.add(res.getString());
				int id = hotwords.size();
				ContentValues hotwordValue = new ContentValues();
				hotwordValue.put(Videos.COLUMN_NAME_NAME, res.getString());
				hotwordValue.put(Videos._ID, "" + id);
				insert(Videos.CONTENT_URI, hotwordValue);
				break;
			case ISTVVodSearchDoc.RES_INT_HOTWORDS_END:
				mFinish = true;
				break;
			case ISTVVodSearchDoc.RES_STR_SUGGESTS_RESULT:			
				suggests.add(res.getString());
				int suggestID = suggests.size();
				ContentValues suggestValue = new ContentValues();
				suggestValue.put(Videos.COLUMN_NAME_NAME, res.getString());
				suggestValue.put(Videos._ID, "" + suggestID);
				insert(Videos.CONTENT_URI, suggestValue);				
				break;
			case ISTVVodSearchDoc.RES_INT_SUGGESTS_END:
				mFinish = true;
				break;
			default:
				break;
		}
	}	
	
	public void onUpdate(){}	
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
		   String sortOrder) {
		Log.d(TAG, "*********************query uri="+uri+",selection="+selection + ",sortOrder="+sortOrder);
		
		String keyWord;
		String[] category = null;
		int pageIndex = 0;
		int pageSize = 0;
		String limit = "0";

		if(doc == null) {
			doc =  new ISTVVodSearchDoc();
			doc.registerCallback(this);			
		}
		
		delete(Videos.CONTENT_URI, null, null);		
		Long start = System.currentTimeMillis();
		String strUri = Uri.decode(uri.toString());
		Log.d(TAG, "strUri="+strUri);
		if(strUri.endsWith("search")){			
			searchMap.clear();
			keyWord = selectionArgs[0];
			category = selectionArgs[1].split(",");
			pageIndex = Integer.parseInt(selectionArgs[2]);
			pageSize = Integer.parseInt(selectionArgs[3]);
			limit = selectionArgs[3];			
			
			for(String model : category) {
				HashMap<String, Object> hashMap = new HashMap<String, Object>();
				hashMap.put("category", model);
				hashMap.put("end", 0);
				searchMap.add(hashMap);
				doc.getSearchResult(model, keyWord, pageIndex, pageSize); 
			}			
			while(true) {
				int i = 0;
				try{
					Thread.sleep(200);
				} catch (InterruptedException e) {
				}
				for(HashMap<String, Object> map : searchMap) {
					if((Integer)map.get("end") == 1) {
						i++;
					}
				}
				if(i == searchMap.size()) {
					break;
				}
				/*
				if(System.currentTimeMillis() - start > 10000) {
					break;
				}*/
			}
		} else if(strUri.endsWith("hotwords")){
			Log.d(TAG, "hotwords");
			hotwords.clear();
			mFinish = false;
			doc.getHotwordsResult();
			while(true) {
				try{
					Thread.sleep(200);
				}catch(InterruptedException e){
				}
				if(mFinish) {
					break;
				}
				
				if(System.currentTimeMillis() - start > 10000){
					break;
				}
			}
		} else if(strUri.contains("search_suggest_query")){
			Log.d(TAG, "suggest="+selectionArgs[0]);
			suggests.clear();
			mFinish = false;
			keyWord = selectionArgs[0];
			doc.getSuggestResult(keyWord);
			while(true) {
				try{
					Thread.sleep(200);
				}catch(InterruptedException e){
				}
				if(mFinish) {
					break;
				}
				
				if(System.currentTimeMillis() - start > 10000){
					break;
				}
			}						
		}else {
			return null;
		}
		
		
		Log.d(TAG, "Start query");
	    SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
	    qb.setTables(Videos.TABLE_NAME);		

	    String orderBy;
		orderBy = Videos.DEFAULT_SORT_ORDER;
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		if(strUri.endsWith("search")){
			qb.setProjectionMap(sVODProjectionMap);
			Cursor[] cursors = new Cursor[searchMap.size()];
			for(int i = 0; i < searchMap.size(); i++) {
				cursors[i] = qb.query(db, new String[]{Videos._ID, 
														Videos._COUNT, 
														Videos.COLUMN_NAME_CATEGORY, 
														Videos.COLUMN_NAME_DETAIL_URL, 
														Videos.COLUMN_NAME_FOCUS, 
														Videos.COLUMN_NAME_IMAGE_URL, 
														Videos.COLUMN_NAME_IS_COMPLEX, 
														Videos.COLUMN_NAME_IS_HD, 
														Videos.COLUMN_NAME_NAME,
														Videos.COLUMN_NAME_PRICE,
														Videos.COLUMN_NAME_POSITION}, 
							Videos.COLUMN_NAME_CATEGORY + " = ?", 
							new String[]{((String)searchMap.get(i).get("category")).substring(1)}, null, null, orderBy, limit);
				cursors[i].setNotificationUri(getContext().getContentResolver(), uri);
				Log.d(TAG, "Start query count=" + cursors[i].getCount() + "category=" + ((String)searchMap.get(i).get("category")).substring(1));				
			}
			Cursor merCur = new MergeCursor(cursors);
			Log.d(TAG, "Start query MergeCursor count=" + merCur.getCount());
			return merCur;
		} else if(strUri.endsWith("hotwords")){	
			qb.setProjectionMap(sHotWordsProjectionMap);
			Cursor cursor = qb.query(db, new String[]{Videos._ID, Videos.COLUMN_NAME_NAME}, null, null, null, null, orderBy);			
			return cursor;
		} else if(strUri.contains("search_suggest_query")){
			qb.setProjectionMap(sSuggestWordsProjectionMap);
			Cursor suggestCursor = qb.query(db, new String[]{Videos._ID, Videos.COLUMN_NAME_NAME}, null, null, null, null, orderBy);
			Log.d(TAG, "getColumnIndex="+ suggestCursor.getColumnIndex("suggest_text_1"));
			return suggestCursor;
		}
		return null;
	}

	public String getType(Uri uri) {

	   /**
		* Chooses the MIME type based on the incoming URI pattern
		*/
	   switch (sUriMatcher.match(uri)) {

		   // If the pattern is for notes or live folders, returns the general content type.
		   case VIDEOS:
			   return Videos.CONTENT_TYPE;

		   // If the pattern is for note IDs, returns the note ID content type.
		   case VIDEOS_ID:
			   return Videos.CONTENT_ITEM_TYPE;

		   // If the URI pattern doesn't match any permitted patterns, throws an exception.
		   default:
			   throw new IllegalArgumentException("Unknown URI " + uri);
	   }
	}

    public Uri insert(Uri uri, ContentValues initialValues) {

        // Validates the incoming URI. Only the full provider URI is allowed for inserts.
        if (sUriMatcher.match(uri) != VIDEOS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // A map to hold the new record's values.
        ContentValues values;

        // If the incoming values map is not null, uses it for the new values.
        if (initialValues != null) {
            values = new ContentValues(initialValues);

        } else {
            // Otherwise, create a new value map
            values = new ContentValues();
        }

        // Gets the current system time in milliseconds        

        // If the values map doesn't contain the creation date, sets the value to the current time.
        if (values.containsKey(Videos._ID) == false) {
            values.put(Videos._ID, "");
        }

		if (values.containsKey(Videos._COUNT) == false) {
			values.put(Videos._COUNT, "");
		}

        // If the values map doesn't contain a title, sets the value to the default title.
        if (values.containsKey(Videos.COLUMN_NAME_CATEGORY) == false) {
            values.put(Videos.COLUMN_NAME_CATEGORY, "");
        }

		if (values.containsKey(Videos.COLUMN_NAME_NAME) == false) {
			values.put(Videos.COLUMN_NAME_NAME, "");
		}
		
		if (values.containsKey(Videos.COLUMN_NAME_FOCUS) == false) {
			values.put(Videos.COLUMN_NAME_FOCUS, "");
		}

		if (values.containsKey(Videos.COLUMN_NAME_DETAIL_URL) == false) {
			values.put(Videos.COLUMN_NAME_DETAIL_URL, "");
		}

		if (values.containsKey(Videos.COLUMN_NAME_IMAGE_URL) == false) {
			values.put(Videos.COLUMN_NAME_IMAGE_URL, "");
		}

		if (values.containsKey(Videos.COLUMN_NAME_POSITION) == false) {
			values.put(Videos.COLUMN_NAME_POSITION, "");
		}		

		if (values.containsKey(Videos.COLUMN_NAME_IS_COMPLEX) == false) {
			values.put(Videos.COLUMN_NAME_IS_COMPLEX, "");
		}		

		if (values.containsKey(Videos.COLUMN_NAME_IS_HD) == false) {
			values.put(Videos.COLUMN_NAME_IS_HD, "");
		}		

		if (values.containsKey(Videos.COLUMN_NAME_PRICE) == false) {
			values.put(Videos.COLUMN_NAME_PRICE, "");
		}				
        // Opens the database object in "write" mode.
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        // Performs the insert and returns the ID of the new note.NotePad.Notes
        long rowId = db.insert(
            Videos.TABLE_NAME,        // The table to insert into.
            null,  // A hack, SQLite sets this column value to null
                                             // if values is empty.
            values                           // A map of column names, and the values to insert
                                             // into the columns.
        );

        // If the insert succeeded, the row ID exists.
        if (rowId > 0) {
            // Creates a URI with the note ID pattern and the new row ID appended to it.
            Uri noteUri = ContentUris.withAppendedId(Videos.CONTENT_ID_URI_BASE, rowId);

            // Notifies observers registered against this provider that the data changed.
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        // If the insert didn't succeed, then the rowID is <= 0. Throws an exception.
        throw new SQLException("Failed to insert row into " + uri);
    }

    public int delete(Uri uri, String where, String[] whereArgs) {

        // Opens the database object in "write" mode.
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String finalWhere;

        int count;

        // Does the delete based on the incoming URI pattern.
        switch (sUriMatcher.match(uri)) {

            // If the incoming pattern matches the general pattern for notes, does a delete
            // based on the incoming "where" columns and arguments.
            case VIDEOS:
                count = db.delete(
                    Videos.TABLE_NAME,  // The database table name
                    where,                     // The incoming where clause column names
                    whereArgs                  // The incoming where clause values
                );
                break;

                // If the incoming URI matches a single note ID, does the delete based on the
                // incoming data, but modifies the where clause to restrict it to the
                // particular note ID.
            case VIDEOS_ID:
                /*
                 * Starts a final WHERE clause by restricting it to the
                 * desired note ID.
                 */
                finalWhere =
                        Videos._ID +                              // The ID column name
                        " = " +                                          // test for equality
                        uri.getPathSegments().                           // the incoming note ID
                            get(Videos.VIDEO_ID_PATH_POSITION)
                ;

                // If there were additional selection criteria, append them to the final
                // WHERE clause
                if (where != null) {
                    finalWhere = finalWhere + " AND " + where;
                }

                // Performs the delete.
                count = db.delete(
                    Videos.TABLE_NAME,  // The database table name.
                    finalWhere,                // The final WHERE clause
                    whereArgs                  // The incoming where clause values.
                );
                break;

            // If the incoming pattern is invalid, throws an exception.
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        /*Gets a handle to the content resolver object for the current context, and notifies it
         * that the incoming URI changed. The object passes this along to the resolver framework,
         * and observers that have registered themselves for the provider are notified.
         */
        getContext().getContentResolver().notifyChange(uri, null);

        // Returns the number of rows deleted.
        return count;
    }

    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {

        // Opens the database object in "write" mode.
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        String finalWhere;

        // Does the update based on the incoming URI pattern
        switch (sUriMatcher.match(uri)) {

            // If the incoming URI matches the general notes pattern, does the update based on
            // the incoming data.
            case VIDEOS:

                // Does the update and returns the number of rows updated.
                count = db.update(
                    Videos.TABLE_NAME, // The database table name.
                    values,                   // A map of column names and new values to use.
                    where,                    // The where clause column names.
                    whereArgs                 // The where clause column values to select on.
                );
                break;

            // If the incoming URI matches a single note ID, does the update based on the incoming
            // data, but modifies the where clause to restrict it to the particular note ID.
            case VIDEOS_ID:
                // From the incoming URI, get the note ID
                String noteId = uri.getPathSegments().get(Videos.VIDEO_ID_PATH_POSITION);

                /*
                 * Starts creating the final WHERE clause by restricting it to the incoming
                 * note ID.
                 */
                finalWhere =
                        Videos._ID +                              // The ID column name
                        " = " +                                          // test for equality
                        uri.getPathSegments().                           // the incoming note ID
                            get(Videos.VIDEO_ID_PATH_POSITION)
                ;

                // If there were additional selection criteria, append them to the final WHERE
                // clause
                if (where !=null) {
                    finalWhere = finalWhere + " AND " + where;
                }


                // Does the update and returns the number of rows updated.
                count = db.update(
                    Videos.TABLE_NAME, // The database table name.
                    values,                   // A map of column names and new values to use.
                    finalWhere,               // The final WHERE clause to use
                                              // placeholders for whereArgs
                    whereArgs                 // The where clause column values to select on, or
                                              // null if the values are in the where argument.
                );
                break;
            // If the incoming pattern is invalid, throws an exception.
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        /*Gets a handle to the content resolver object for the current context, and notifies it
         * that the incoming URI changed. The object passes this along to the resolver framework,
         * and observers that have registered themselves for the provider are notified.
         */
        getContext().getContentResolver().notifyChange(uri, null);

        // Returns the number of rows updated.
        return count;
    }
        	
	public static final class Videos implements BaseColumns {
		
		private	Videos(){}
		
	    public static final String AUTHORITY = "com.lenovo.dll.nebula.vod.searchProvider";

		public static final String TABLE_NAME = "search";

		private static final String SCHEME = "content://";

		private static final String PATH_VIDEOS = "/search";

		private static final String PATH_VIDEO_ID = "/search/";
		
		public static final int VIDEO_ID_PATH_POSITION = 1;

		public static final Uri CONTENT_URI =  Uri.parse(SCHEME + AUTHORITY + PATH_VIDEOS);
		
		public static final Uri CONTENT_ID_URI_BASE
					= Uri.parse(SCHEME + AUTHORITY + PATH_VIDEO_ID);

		public static final Uri CONTENT_ID_URI_PATTERN
					= Uri.parse(SCHEME + AUTHORITY + PATH_VIDEO_ID + "/#");

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/com.geniatech.vod";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/com.geniatech.vod";
		
		public static final String DEFAULT_SORT_ORDER = "_id DESC";
		
		public static final String COLUMN_NAME_ID = "id";
		
		public static final String COLUMN_NAME_COUNT = "count";
			
		public static final String COLUMN_NAME_CATEGORY = "category";
		
		public static final String COLUMN_NAME_NAME = "name";	
		
		public static final String COLUMN_NAME_FOCUS = "focus";	
		
		public static final String COLUMN_NAME_DETAIL_URL = "detailUrl";	
		
		public static final String COLUMN_NAME_IMAGE_URL = "imageUrl";	
		
		public static final String COLUMN_NAME_POSITION = "position";	
		
		public static final String COLUMN_NAME_IS_COMPLEX = "isComplex";	
		
		public static final String COLUMN_NAME_IS_HD = "isHD";	
		
		public static final String COLUMN_NAME_PRICE = "price";	
	}
}


