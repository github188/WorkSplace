package com.lenovo.settings;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;


import java.util.HashMap;

import android.provider.BaseColumns;

public class CityProvider extends ContentProvider {
	private static final String TAG = "CityProvider";
	
	private static final String DATABASE_NAME = "city.db";
	
	

	private static final int DATABASE_VERSION = 2;
	
	private static HashMap<String, String> sCityProjectionMap;		

    private static final int CITYS = 1;

    private static final int CITYS_ID = 2;
	
	private static final UriMatcher sUriMatcher;

	private DatabaseHelper mOpenHelper;
	
	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(Citys.AUTHORITY, "citys", CITYS);
		sUriMatcher.addURI(Citys.AUTHORITY, "citys/#", CITYS_ID);
		
		sCityProjectionMap = new HashMap<String, String>();
		sCityProjectionMap.put(Citys._ID, Citys._ID);
		sCityProjectionMap.put(Citys.COLUMN_NAME_ALPHABET, Citys.COLUMN_NAME_ALPHABET);
		sCityProjectionMap.put(Citys.COLUMN_NAME_CITY_NAME_EN, Citys.COLUMN_NAME_CITY_NAME_EN);
		sCityProjectionMap.put(Citys.COLUMN_NAME_CITY_NAME, Citys.COLUMN_NAME_CITY_NAME);
		sCityProjectionMap.put(Citys.COLUMN_NAME_CITY_ID, Citys.COLUMN_NAME_CITY_ID); 
	}
	
	static class DatabaseHelper extends SQLiteOpenHelper {
	   DatabaseHelper(Context context) {
		   super(context, DATABASE_NAME, null, DATABASE_VERSION);
	   }

	   public void onCreate(SQLiteDatabase db) {
		   db.execSQL("CREATE TABLE " + Citys.TABLE_NAME + " ("
				   + Citys._ID + " INTEGER PRIMARY KEY,"
				   + Citys.COLUMN_NAME_ALPHABET + " TEXT,"
				   + Citys.COLUMN_NAME_CITY_NAME_EN + " TEXT,"
				   + Citys.COLUMN_NAME_CITY_NAME + " TEXT,"
				   + Citys.COLUMN_NAME_CITY_ID + " TEXT"
				   + ");");
	   }

	   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		   // Logs that the database is being upgraded
		   Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
				   + newVersion + ", which will destroy all old data");
		   // Kills the table and existing data
		   db.execSQL("DROP TABLE IF EXISTS apps");
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

	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
		   String sortOrder) {

	   // Constructs a new query builder and sets its table name
	   SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
	   qb.setTables(Citys.TABLE_NAME);

	   /**
		* Choose the projection and adjust the "where" clause based on URI pattern-matching.
		*/
	   switch (sUriMatcher.match(uri)) {
		   // If the incoming URI is for notes, chooses the Notes projection
		   case CITYS:
			   qb.setProjectionMap(sCityProjectionMap);
			   break;

		   /* If the incoming URI is for a single note identified by its ID, chooses the
			* note ID projection, and appends "_ID = <noteID>" to the where clause, so that
			* it selects that single note
			*/
		   case CITYS_ID:
			   qb.setProjectionMap(sCityProjectionMap);
			   qb.appendWhere(
				   Citys._ID +    // the name of the ID column
				   "=" +
				   // the position of the note ID itself in the incoming URI
				   uri.getPathSegments().get(Citys.CITY_ID_PATH_POSITION));
			   break;
		   default:
			   // If the URI doesn't match any of the known patterns, throw an exception.
			   throw new IllegalArgumentException("Unknown URI " + uri);
	   }


	   String orderBy;
	   // If no sort order is specified, uses the default
	   if (TextUtils.isEmpty(sortOrder)) {
		   orderBy = Citys.DEFAULT_SORT_ORDER;
	   } else {
		   // otherwise, uses the incoming sort order
		   orderBy = sortOrder;
	   }

	   // Opens the database object in "read" mode, since no writes need to be done.
	   SQLiteDatabase db = mOpenHelper.getReadableDatabase();

	   /*
		* Performs the query. If no problems occur trying to read the database, then a Cursor
		* object is returned; otherwise, the cursor variable contains null. If no records were
		* selected, then the Cursor object is empty, and Cursor.getCount() returns 0.
		*/
	   Cursor c = qb.query(
		   db,            // The database to query
		   projection,    // The columns to return from the query
		   selection,     // The columns for the where clause
		   selectionArgs, // The values for the where clause
		   null,          // don't group the rows
		   null,          // don't filter by row groups
		   orderBy        // The sort order
	   );

	   // Tells the Cursor what URI to watch, so it knows when its source data changes
	   c.setNotificationUri(getContext().getContentResolver(), uri);
	   return c;
	}

	public String getType(Uri uri) {

	   /**
		* Chooses the MIME type based on the incoming URI pattern
		*/
	   switch (sUriMatcher.match(uri)) {

		   // If the pattern is for notes or live folders, returns the general content type.
		   case CITYS:
			   return Citys.CONTENT_TYPE;

		   // If the pattern is for note IDs, returns the note ID content type.
		   case CITYS_ID:
			   return Citys.CONTENT_ITEM_TYPE;

		   // If the URI pattern doesn't match any permitted patterns, throws an exception.
		   default:
			   throw new IllegalArgumentException("Unknown URI " + uri);
	   }
	}

    public Uri insert(Uri uri, ContentValues initialValues) {

        // Validates the incoming URI. Only the full provider URI is allowed for inserts.
        if (sUriMatcher.match(uri) != CITYS) {
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
        Long now = Long.valueOf(System.currentTimeMillis());

        // If the values map doesn't contain the creation date, sets the value to the current time.
        if (values.containsKey(Citys.COLUMN_NAME_ALPHABET) == false) {
            values.put(Citys.COLUMN_NAME_ALPHABET, "");
        }

		if (values.containsKey(Citys.COLUMN_NAME_CITY_NAME) == false) {
			values.put(Citys.COLUMN_NAME_CITY_NAME, "");
		}

        // If the values map doesn't contain a title, sets the value to the default title.
        if (values.containsKey(Citys.COLUMN_NAME_CITY_NAME_EN) == false) {
            values.put(Citys.COLUMN_NAME_CITY_NAME_EN, "");
        }

		if (values.containsKey(Citys.COLUMN_NAME_CITY_ID) == false) {
			values.put(Citys.COLUMN_NAME_CITY_ID, "");
		}
        // Opens the database object in "write" mode.
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        // Performs the insert and returns the ID of the new note.NotePad.Notes
        long rowId = db.insert(
            Citys.TABLE_NAME,        // The table to insert into.
            null,  // A hack, SQLite sets this column value to null
                                             // if values is empty.
            values                           // A map of column names, and the values to insert
                                             // into the columns.
        );

        // If the insert succeeded, the row ID exists.
        if (rowId > 0) {
            // Creates a URI with the note ID pattern and the new row ID appended to it.
            Uri noteUri = ContentUris.withAppendedId(Citys.CONTENT_ID_URI_BASE, rowId);

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
            case CITYS:
                count = db.delete(
                    Citys.TABLE_NAME,  // The database table name
                    where,                     // The incoming where clause column names
                    whereArgs                  // The incoming where clause values
                );
                break;

                // If the incoming URI matches a single note ID, does the delete based on the
                // incoming data, but modifies the where clause to restrict it to the
                // particular note ID.
            case CITYS_ID:
                /*
                 * Starts a final WHERE clause by restricting it to the
                 * desired note ID.
                 */
                finalWhere =
                        Citys._ID +                              // The ID column name
                        " = " +                                          // test for equality
                        uri.getPathSegments().                           // the incoming note ID
                            get(Citys.CITY_ID_PATH_POSITION)
                ;

                // If there were additional selection criteria, append them to the final
                // WHERE clause
                if (where != null) {
                    finalWhere = finalWhere + " AND " + where;
                }

                // Performs the delete.
                count = db.delete(
                    Citys.TABLE_NAME,  // The database table name.
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
            case CITYS:

                // Does the update and returns the number of rows updated.
                count = db.update(
                    Citys.TABLE_NAME, // The database table name.
                    values,                   // A map of column names and new values to use.
                    where,                    // The where clause column names.
                    whereArgs                 // The where clause column values to select on.
                );
                break;

            // If the incoming URI matches a single note ID, does the update based on the incoming
            // data, but modifies the where clause to restrict it to the particular note ID.
            case CITYS_ID:
                // From the incoming URI, get the note ID
                String noteId = uri.getPathSegments().get(Citys.CITY_ID_PATH_POSITION);

                /*
                 * Starts creating the final WHERE clause by restricting it to the incoming
                 * note ID.
                 */
                finalWhere =
                        Citys._ID +                              // The ID column name
                        " = " +                                          // test for equality
                        uri.getPathSegments().                           // the incoming note ID
                            get(Citys.CITY_ID_PATH_POSITION)
                ;

                // If there were additional selection criteria, append them to the final WHERE
                // clause
                if (where !=null) {
                    finalWhere = finalWhere + " AND " + where;
                }


                // Does the update and returns the number of rows updated.
                count = db.update(
                    Citys.TABLE_NAME, // The database table name.
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
        	
	public static final class Citys implements BaseColumns {
		
		private	Citys(){}
		
	    public static final String AUTHORITY = "com.lenovo.settings.city";

		public static final String TABLE_NAME = "citys";

		private static final String SCHEME = "content://";

		private static final String PATH_CITYS = "/citys";

		private static final String PATH_CITY_ID = "/citys/";
		
		public static final int CITY_ID_PATH_POSITION = 1;

		public static final Uri CONTENT_URI =  Uri.parse(SCHEME + AUTHORITY + PATH_CITYS);
		
		public static final Uri CONTENT_ID_URI_BASE
					= Uri.parse(SCHEME + AUTHORITY + PATH_CITY_ID);

		public static final Uri CONTENT_ID_URI_PATTERN
					= Uri.parse(SCHEME + AUTHORITY + PATH_CITY_ID + "/#");

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/com.lenovo.settings";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/com.lenovo.settings";
		
		public static final String DEFAULT_SORT_ORDER = "_id DESC";
		
		public static final String COLUMN_NAME_ALPHABET = "alphabet";
		
		public static final String COLUMN_NAME_CITY_NAME_EN = "name_en";
			
		public static final String COLUMN_NAME_CITY_NAME = "name";
		
		public static final String COLUMN_NAME_CITY_ID = "id";	
	}
}



