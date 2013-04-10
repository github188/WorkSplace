/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.joysee.launcher.common;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.joysee.launcher.activity.LauncherApplication;
import com.joysee.launcher.activity.R;
import com.joysee.launcher.common.LauncherSettings.AppMenu;
import com.joysee.launcher.utils.LauncherLog;
import com.joysee.launcher.utils.Utilities;

public class LauncherProvider extends ContentProvider {
    private static final String TAG = "com.joysee.launcher.common.LauncherProvider";

    private static final String DATABASE_NAME = "launcher-joysee.db";

    private static final int DATABASE_VERSION = 9;

    static final String AUTHORITY = "com.joysee.launcher.settings";

    static final String TABLE_APPMENU = "appmenu";
    static final String PARAMETER_NOTIFY = "notify";

    
    private DatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
    	LauncherLog.log_D(TAG, "LauncherProvider    onCreate");
        mOpenHelper = new DatabaseHelper(getContext());
        mOpenHelper.getWritableDatabase();
        ((LauncherApplication) getContext()).setLauncherProvider(this);
        return true;
    }

    @Override
    public String getType(Uri uri) {
        SqlArguments args = new SqlArguments(uri, null, null);
        if (TextUtils.isEmpty(args.where)) {
            return "vnd.android.cursor.dir/" + args.table;
        } else {
            return "vnd.android.cursor.item/" + args.table;
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
    	LauncherLog.log_D(TAG, "LauncherProvider    query()   begin");
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(args.table);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Cursor result = qb.query(db, projection, args.where, args.args, null, null, sortOrder);
        result.setNotificationUri(getContext().getContentResolver(), uri);

        return result;
    }

    private static long dbInsertAndCheck(DatabaseHelper helper,
            SQLiteDatabase db, String table, String nullColumnHack, ContentValues values) {
        if (!values.containsKey(LauncherSettings.AppMenu._ID)) {
            throw new RuntimeException("Error: attempting to add item without specifying an id");
        }
        return db.insert(table, nullColumnHack, values);
    }

    private static void deleteId(SQLiteDatabase db, long id) {
        Uri uri = LauncherSettings.AppMenu.getContentUri(id, false);
        SqlArguments args = new SqlArguments(uri, null, null);
        db.delete(args.table, args.where, args.args);
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        SqlArguments args = new SqlArguments(uri);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final long rowId = dbInsertAndCheck(mOpenHelper, db, args.table, null, initialValues);
        if (rowId <= 0) return null;

        uri = ContentUris.withAppendedId(uri, rowId);
        sendNotify(uri);

        return uri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SqlArguments args = new SqlArguments(uri);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            int numValues = values.length;
            for (int i = 0; i < numValues; i++) {
                if (dbInsertAndCheck(mOpenHelper, db, args.table, null, values[i]) < 0) {
                    return 0;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        sendNotify(uri);
        return values.length;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.delete(args.table, args.where, args.args);
        if (count > 0) sendNotify(uri);

        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.update(args.table, values, args.where, args.args);
        if (count > 0) sendNotify(uri);

        return count;
    }

    private void sendNotify(Uri uri) {
        String notify = uri.getQueryParameter(PARAMETER_NOTIFY);
        if (notify == null || "true".equals(notify)) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
    }

    public long generateNewId() {
        return mOpenHelper.generateNewId();
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
    	private static final String TAG = "com.joysee.launcher.common.LauncherProvider.DatabaseHelper";
        private static final String TAG_FAVORITES = "favorites";
        private static final String TAG_FAVORITE = "favorite";
        private static final String TAG_SHORTCUT = "shortcut";
        private static final String TAG_FOLDER = "folder";

        private final Context mContext;
        private long mMaxId = -1;

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mContext = context;
            
            // In the case where neither onCreate nor onUpgrade gets called, we read the maxId from
            // the DB here
            if (mMaxId == -1) {
                mMaxId = initializeMaxId(getWritableDatabase());
                LauncherLog.log_D(TAG, "onCreate  mMaxId is " + mMaxId);
            }
            
            if(mMaxId == 0 ){
            	loadLauncherData(getWritableDatabase(), 0);
            }
            	

        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	LauncherLog.log_D(TAG, "creating new launcher database");
            db.execSQL("CREATE TABLE IF NOT EXISTS appmenu (" +
            		"_id integer primary key autoincrement," +
            		"appName TEXT, " +
					"intent TEXT," +
					"itemType INTEGER," +
					"parentItem INTEGER, " +
					"itemOrder INTEGER," +
					"iconType INTEGER," +
					"iconPackage TEXT," +
					"iconResource TEXT," +
					"icon BLOB," +
					"iconFocus BLOB," + 
					"uri TEXT)");
            

        }
        
        private long initializeMaxId(SQLiteDatabase db) {
            Cursor c = db.rawQuery("SELECT MAX(_id) FROM appmenu", null);

            // get the result
            final int maxIdIndex = 0; 
            long id = -1;
            if (c != null && c.moveToNext()) {
                id = c.getLong(maxIdIndex);
            }    
            if (c != null) {
                c.close();
            }    

            if (id == -1) {
                throw new RuntimeException("Error: could not query max id");
            }    

            return id;
        }
        
        private long getItemOrder(SQLiteDatabase db,int parentid) {
            Cursor c = db.rawQuery("SELECT MAX(itemOrder) FROM appmenu where parentItem = " + parentid, null);

            // get the result
            final int maxIdIndex = 0; 
            long itemOrder = -1;
            if (c != null && c.moveToNext()) {
            	itemOrder = c.getLong(maxIdIndex);
            }    
            if (c != null) {
                c.close();
            }    

            if (itemOrder == -1) {
                throw new RuntimeException("Error: could not query max id");
            }    

            return itemOrder;
        } 

        private int copyFromCursor(SQLiteDatabase db, Cursor c) {
            final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.AppMenu._ID);
            final int intentIndex = c.getColumnIndexOrThrow(LauncherSettings.AppMenu.INTENT);
            final int titleIndex = c.getColumnIndexOrThrow(LauncherSettings.AppMenu.APPNAME);
            final int iconTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.AppMenu.ICON_TYPE);
            final int iconIndex = c.getColumnIndexOrThrow(LauncherSettings.AppMenu.ICON);
            final int iconPackageIndex = c.getColumnIndexOrThrow(LauncherSettings.AppMenu.ICON_PACKAGE);
            final int iconResourceIndex = c.getColumnIndexOrThrow(LauncherSettings.AppMenu.ICON_RESOURCE);
            final int containerIndex = c.getColumnIndexOrThrow(LauncherSettings.AppMenu.CONTAINER);
            final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.AppMenu.ITEM_TYPE);
            final int uriIndex = c.getColumnIndexOrThrow(LauncherSettings.AppMenu.URI);
            final int displayModeIndex = c.getColumnIndexOrThrow(LauncherSettings.AppMenu.DISPLAY_MODE);

            ContentValues[] rows = new ContentValues[c.getCount()];
            int i = 0;
            while (c.moveToNext()) {
                ContentValues values = new ContentValues(c.getColumnCount());
                values.put(LauncherSettings.AppMenu._ID, c.getLong(idIndex));
                values.put(LauncherSettings.AppMenu.INTENT, c.getString(intentIndex));
                values.put(LauncherSettings.AppMenu.APPNAME, c.getString(titleIndex));
                values.put(LauncherSettings.AppMenu.ICON_TYPE, c.getInt(iconTypeIndex));
                values.put(LauncherSettings.AppMenu.ICON, c.getBlob(iconIndex));
                values.put(LauncherSettings.AppMenu.ICON_PACKAGE, c.getString(iconPackageIndex));
                values.put(LauncherSettings.AppMenu.ICON_RESOURCE, c.getString(iconResourceIndex));
                values.put(LauncherSettings.AppMenu.CONTAINER, c.getInt(containerIndex));
                values.put(LauncherSettings.AppMenu.ITEM_TYPE, c.getInt(itemTypeIndex));
                values.put(LauncherSettings.AppMenu.URI, c.getString(uriIndex));
                values.put(LauncherSettings.AppMenu.DISPLAY_MODE, c.getInt(displayModeIndex));
                rows[i++] = values;
            }

            db.beginTransaction();
            int total = 0;
            try {
                int numValues = rows.length;
                for (i = 0; i < numValues; i++) {
                    if (dbInsertAndCheck(this, db, TABLE_APPMENU, null, rows[i]) < 0) {
                        return 0;
                    } else {
                        total++;
                    }
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            return total;
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            LauncherLog.log_D(TAG, "onUpgrade triggered");
            int version = oldVersion;
            if (version != DATABASE_VERSION) {
            	LauncherLog.log_D(TAG, "Destroying all old data.");
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPMENU);
                onCreate(db);
            }
        }

        private boolean updateContactsShortcuts(SQLiteDatabase db) {
//            Cursor c = null;
//            final String selectWhere = buildOrWhereString(Favorites.ITEM_TYPE,
//                    new int[] { Favorites.ITEM_TYPE_SHORTCUT });
//
//            db.beginTransaction();
//            try {
//                // Select and iterate through each matching widget
//                c = db.query(TABLE_FAVORITES, new String[] { Favorites._ID, Favorites.INTENT },
//                        selectWhere, null, null, null, null);
//                
//                if (LOGD) Log.d(TAG, "found upgrade cursor count=" + c.getCount());
//                
//                final ContentValues values = new ContentValues();
//                final int idIndex = c.getColumnIndex(Favorites._ID);
//                final int intentIndex = c.getColumnIndex(Favorites.INTENT);
//                
//                while (c != null && c.moveToNext()) {
//                    long favoriteId = c.getLong(idIndex);
//                    final String intentUri = c.getString(intentIndex);
//                    if (intentUri != null) {
//                        try {
//                            Intent intent = Intent.parseUri(intentUri, 0);
//                            android.util.Log.d("Home", intent.toString());
//                            final Uri uri = intent.getData();
//                            final String data = uri.toString();
//                            if (Intent.ACTION_VIEW.equals(intent.getAction()) &&
//                                    (data.startsWith("content://contacts/people/") ||
//                                    data.startsWith("content://com.android.contacts/contacts/lookup/"))) {
//
//                                intent = new Intent("com.android.contacts.action.QUICK_CONTACT");
//                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
//                                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
//                                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
//
//                                intent.setData(uri);
//                                intent.putExtra("mode", 3);
//                                intent.putExtra("exclude_mimes", (String[]) null);
//
//                                values.clear();
//                                values.put(LauncherSettings.Favorites.INTENT, intent.toUri(0));
//    
//                                String updateWhere = Favorites._ID + "=" + favoriteId;
//                                db.update(TABLE_FAVORITES, values, updateWhere, null);                                
//                            }
//                        } catch (RuntimeException ex) {
//                            Log.e(TAG, "Problem upgrading shortcut", ex);
//                        } catch (URISyntaxException e) {
//                            Log.e(TAG, "Problem upgrading shortcut", e);                            
//                        }
//                    }
//                }
//                
//                db.setTransactionSuccessful();
//            } catch (SQLException ex) {
//                Log.w(TAG, "Problem while upgrading contacts", ex);
//                return false;
//            } finally {
//                db.endTransaction();
//                if (c != null) {
//                    c.close();
//                }
//            }
//
            return true;
        }

        private void normalizeIcons(SQLiteDatabase db) {
//            Log.d(TAG, "normalizing icons");
//
//            db.beginTransaction();
//            Cursor c = null;
//            SQLiteStatement update = null;
//            try {
//                boolean logged = false;
//                update = db.compileStatement("UPDATE favorites "
//                        + "SET icon=? WHERE _id=?");
//
//                c = db.rawQuery("SELECT _id, icon FROM favorites WHERE iconType=" +
//                        Favorites.ICON_TYPE_BITMAP, null);
//
//                final int idIndex = c.getColumnIndexOrThrow(Favorites._ID);
//                final int iconIndex = c.getColumnIndexOrThrow(Favorites.ICON);
//
//                while (c.moveToNext()) {
//                    long id = c.getLong(idIndex);
//                    byte[] data = c.getBlob(iconIndex);
//                    try {
//                        Bitmap bitmap = Utilities.resampleIconBitmap(
//                                BitmapFactory.decodeByteArray(data, 0, data.length),
//                                mContext);
//                        if (bitmap != null) {
//                            update.bindLong(1, id);
//                            data = ItemInfo.flattenBitmap(bitmap);
//                            if (data != null) {
//                                update.bindBlob(2, data);
//                                update.execute();
//                            }
//                            bitmap.recycle();
//                        }
//                    } catch (Exception e) {
//                        if (!logged) {
//                            Log.e(TAG, "Failed normalizing icon " + id, e);
//                        } else {
//                            Log.e(TAG, "Also failed normalizing icon " + id);
//                        }
//                        logged = true;
//                    }
//                }
//                db.setTransactionSuccessful();
//            } catch (SQLException ex) {
//                Log.w(TAG, "Problem while allocating appWidgetIds for existing widgets", ex);
//            } finally {
//                db.endTransaction();
//                if (update != null) {
//                    update.close();
//                }
//                if (c != null) {
//                    c.close();
//                }
//            }
        }

        // Generates a new ID to use for an object in your database. This method should be only
        // called from the main UI thread. As an exception, we do call it when we call the
        // constructor from the worker thread; however, this doesn't extend until after the
        // constructor is called, and we only pass a reference to LauncherProvider to LauncherApp
        // after that point
        public long generateNewId() {
            if (mMaxId < 0) {
                throw new RuntimeException("Error: max id was not initialized");
            }
            mMaxId += 1;
            return mMaxId;
        }

        String[] folders = {"系统设置","我的应用","直播电视","应用商店","在线视频"};
        int[] folderIconRes = {R.drawable.launcher_coverflow_setting,
        						R.drawable.launcher_coverflow_allapps,
        						R.drawable.launcher_coverflow_tv,
        						R.drawable.launcher_coverflow_appstore,
        						R.drawable.launcher_coverflow_vod};
        
        int[] folderIconFocusRes = {R.drawable.launcher_coverflow_setting_select,
				R.drawable.launcher_coverflow_allapps_select,
				R.drawable.launcher_coverflow_tv_select,
				R.drawable.launcher_coverflow_appstore_select,
				R.drawable.launcher_coverflow_vod_select};
        
        String[] settingListItem = {"音视频设置","网络设置","频道搜索","智能卡设置","时间设置","系统信息"};
        String[] allAppListItem = {"全部应用","应用管理","系统设置","应用商店","在线视频"};
        String[] tvListItem = {"频道列表","喜爱频道","节目指南","预约列表","广播"};
        String[] vodListItem = {"推荐视频","华语电影","海外电影","电视剧","少儿","纪录片"};
        String[] appStoreListItem = {"全部应用","最新应用","应用排行","游戏","工具"};
        /**
         * Loads the default set of favorite packages from an xml file.
         *
         * @param db The database to write the values into
         * @param filterContainerId The specific container id of items to load
         */
        private int loadLauncherData(SQLiteDatabase db, int workspaceResourceId) {
            ContentValues values = new ContentValues();
            String settingKey = "curChoice";
            String[][] folderIntents = {{"com.lenovo.settings","com.lenovo.settings.LenovoSettingsActivity"},
            							{"com.joysee.appstore","com.joysee.appstore.activity.MainActivity"},
            							{"novel.supertv.dvb","novel.supertv.dvb.activity.PlayActivity"},
            							{"com.joysee.appstore","com.joysee.appstore.activity.MainActivity"},
            							{"com.ismartv.ui","com.ismartv.ui.ISTVVodHome"}};
            
            for(int i=0;i<folders.length;i++){
            	values.clear();
            	values.put(AppMenu.APPNAME, folders[i]);
            	values.put(AppMenu.PARENTITEM, AppMenu.NOPARENT);
            	
            	Intent intent = new Intent(Intent.ACTION_MAIN, null);
            	intent.setClassName(folderIntents[i][0], folderIntents[i][1]);
            	if(i == 0){
            		Bundle bundle = new Bundle();
                	bundle.putInt(settingKey, 1);
                	intent.putExtras(bundle);
            	}
//            	
            	values.put(AppMenu.INTENT, intent.toURI());
            	long itemOrder = getItemOrder(db,AppMenu.NOPARENT);
            	values.put(AppMenu.ITEMORDER, itemOrder + 1);
            	Bitmap icon = BitmapFactory.decodeResource(mContext.getResources(), folderIconRes[i]);
            	byte[] bytes = ItemInfo.flattenBitmap(icon);
            	values.put(AppMenu.ICON, bytes);
            	Bitmap iconFocus = BitmapFactory.decodeResource(mContext.getResources(), folderIconFocusRes[i]);
            	byte[] bytesFocus = ItemInfo.flattenBitmap(iconFocus);
            	values.put(AppMenu.ICONFOCUS, bytesFocus);
            	icon.recycle();
            	iconFocus.recycle();
            	addFolder(db, values);
            }
            
            
            int[] settingParam = {6,5,0,13,3,12};
            //"音视频设置","网络设置","频道搜索","智能卡设置","时间设置","系统信息"
            
            for(int i=0;i<settingListItem.length;i++){
            	values.clear();
            	
            	
            	Intent intent1 = new Intent();   
            	Bundle bundle = new Bundle();
            	bundle.putInt(settingKey, settingParam[i]);
            	intent1.putExtras(bundle);
            	intent1.setClassName("com.lenovo.settings","com.lenovo.settings.LenovoSettingsActivity");
            	
            	
            	values.put(AppMenu.APPNAME, settingListItem[i]);
            	values.put(AppMenu.PARENTITEM, 1);
            	values.put(AppMenu.INTENT, intent1.toURI());
            	long itemOrder = getItemOrder(db,1);
            	values.put(AppMenu.ITEMORDER, itemOrder + 1);
            	addShortCutInfo(db,values);
            }
            //{"全部应用","应用管理","系统设置","应用商店","在线视频"};
            for(int i=0;i<allAppListItem.length;i++){
            	Intent intent1 = new Intent(Intent.ACTION_MAIN, null);
//            	intent1.setClassName("com.joysee.launcher.activity", "com.joysee.launcher.activity.AllAppsActivity");
            	if(i ==1){
            		intent1.setClassName("com.lenovo.settings","com.lenovo.settings.LenovoSettingsActivity");
            		Bundle bundle = new Bundle();
                	bundle.putInt(settingKey, 11);
                	intent1.putExtras(bundle);
            	}else if(i == 2){
            		Bundle bundle = new Bundle();
                	bundle.putInt(settingKey, 1);
                	intent1.putExtras(bundle);
            		intent1.setClassName("com.lenovo.settings","com.lenovo.settings.LenovoSettingsActivity");
            	}else if(i == 3){
            		intent1.setClassName("com.joysee.appstore","com.joysee.appstore.activity.MainActivity");
            	}else if(i == 4){
            		intent1.setClassName("com.ismartv.ui","com.ismartv.ui.ISTVVodHome");
            	}
            	
            	values.clear();
            	values.put(AppMenu.APPNAME, allAppListItem[i]);
            	values.put(AppMenu.PARENTITEM, 2);
            	values.put(AppMenu.INTENT, intent1.toURI());
            	long itemOrder = getItemOrder(db,2);
            	values.put(AppMenu.ITEMORDER, itemOrder + 1);
            	addShortCutInfo(db,values);
            }
            
            String tvKey = "com.joysee.key";
            String[] tvValue = {"com.joysee.intent.channel.list",
            					"com.joysee.intent.favorite.list",
            					"com.joysee.intent.program.guide",
            					"com.joysee.intent.reserve.list",
            					"com.joysee.intent.broadcast"}; 
            for(int i=0;i<tvListItem.length;i++){
            	values.clear();
            	
            	Intent intent1 = new Intent();   
            	intent1.putExtra(tvKey, tvValue[i]);
            	intent1.setClassName("novel.supertv.dvb","novel.supertv.dvb.activity.PlayActivity");
            	intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            	
            	values.put(AppMenu.APPNAME, tvListItem[i]);
            	values.put(AppMenu.PARENTITEM, 3);
            	values.put(AppMenu.INTENT, intent1.toURI());
            	long itemOrder = getItemOrder(db,3);
            	values.put(AppMenu.ITEMORDER, itemOrder + 1);
            	addShortCutInfo(db,values);
            }
            
            
            
            String[] channelIDs = {"chinesemovie","overseas","teleplay","comic","documentary"};
            String[] channelNames = {"华语电影","海外电影","电视剧","少儿","纪录片"};
            for(int i=0;i<vodListItem.length;i++){
            	Intent intent1;
            	if(i == 0){
            		intent1 = new Intent();
            		intent1.setClassName("com.ismartv.ui","com.ismartv.ui.ISTVVodHome");
            	}else{
            		intent1 = Utilities.gotoMovieList(channelIDs[i-1], channelNames[i-1]);
            	}
            	
            	intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            	values.clear();
            	values.put(AppMenu.APPNAME, vodListItem[i]);
            	values.put(AppMenu.PARENTITEM, 5);
            	values.put(AppMenu.INTENT, intent1.toURI());
            	long itemOrder = getItemOrder(db,5);
            	values.put(AppMenu.ITEMORDER, itemOrder + 1);
            	addShortCutInfo(db,values);
            }
            
            
            String appStoreKey = "enter_type";
            String appStoreValue = "outer";
            String appStoreTypeKey = "type_id";
            int[] appStoreTypeValue = {-3,-2,-1,0,1};
            //{"全部应用","最新应用","应用排行","游戏","工具"};
            for(int i=0;i<appStoreListItem.length;i++){
            	Intent intent1 = new Intent();
            	Bundle bundle = new Bundle();
            	
            	bundle.putString(appStoreKey, appStoreValue);
            	bundle.putInt(appStoreTypeKey, appStoreTypeValue[i]);
            	intent1.putExtras(bundle);
            	
            	intent1.setClassName("com.joysee.appstore","com.joysee.appstore.activity.ClassActivity");
            	intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            	
            	values.clear();
            	values.put(AppMenu.APPNAME, appStoreListItem[i]);
            	values.put(AppMenu.PARENTITEM, 4);
            	values.put(AppMenu.INTENT, intent1.toURI());
            	long itemOrder = getItemOrder(db,4);
            	values.put(AppMenu.ITEMORDER, itemOrder + 1);
            	addShortCutInfo(db,values);
            }
            
        	return 1;
        }
        
        private long addShortCutInfo(SQLiteDatabase db, ContentValues values){
        	values.put(AppMenu.ITEM_TYPE, AppMenu.ITEM_TYPE_SHORTCUT);
        	long id = generateNewId();
            values.put(AppMenu._ID, id);
            if (dbInsertAndCheck(this, db, TABLE_APPMENU, null, values) <= 0) {
                return -1;
            } else {
                return id;
            }
        	
        }

         private long addAppShortcut(SQLiteDatabase db, ContentValues values, TypedArray a,
                PackageManager packageManager, Intent intent) {
//            long id = -1;
//            ActivityInfo info;
//            String packageName = a.getString(R.styleable.Favorite_packageName);
//            String className = a.getString(R.styleable.Favorite_className);
//            try {
//                ComponentName cn;
//                try {
//                    cn = new ComponentName(packageName, className);
//                    info = packageManager.getActivityInfo(cn, 0);
//                } catch (PackageManager.NameNotFoundException nnfe) {
//                    String[] packages = packageManager.currentToCanonicalPackageNames(
//                        new String[] { packageName });
//                    cn = new ComponentName(packages[0], className);
//                    info = packageManager.getActivityInfo(cn, 0);
//                }
//                id = generateNewId();
//                intent.setComponent(cn);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
//                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
//                values.put(Favorites.INTENT, intent.toUri(0));
//                values.put(Favorites.TITLE, info.loadLabel(packageManager).toString());
//                values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_APPLICATION);
//                values.put(Favorites.SPANX, 1);
//                values.put(Favorites.SPANY, 1);
//                values.put(Favorites._ID, generateNewId());
//                if (dbInsertAndCheck(this, db, TABLE_FAVORITES, null, values) < 0) {
//                    return -1;
//                }
//            } catch (PackageManager.NameNotFoundException e) {
//                Log.w(TAG, "Unable to add favorite: " + packageName +
//                        "/" + className, e);
//            }
//            return id;
        	return -1;
        }

        private long addFolder(SQLiteDatabase db, ContentValues values) {
            values.put(AppMenu.ITEM_TYPE, AppMenu.ITEM_TYPE_FOLDER);
            long id = generateNewId();
            values.put(AppMenu._ID, id);
            if (dbInsertAndCheck(this, db, TABLE_APPMENU, null, values) <= 0) {
                return -1;
            } else {
                return id;
            }
        }

    }
    
    /**
     * Build a query string that will match any row where the column matches
     * anything in the values list.
     */
    static String buildOrWhereString(String column, int[] values) {
        StringBuilder selectWhere = new StringBuilder();
        for (int i = values.length - 1; i >= 0; i--) {
            selectWhere.append(column).append("=").append(values[i]);
            if (i > 0) {
                selectWhere.append(" OR ");
            }
        }
        return selectWhere.toString();
    }

    static class SqlArguments {
        public final String table;
        public final String where;
        public final String[] args;

        SqlArguments(Uri url, String where, String[] args) {
            if (url.getPathSegments().size() == 1) {
                this.table = url.getPathSegments().get(0);
                this.where = where;
                this.args = args;
            } else if (url.getPathSegments().size() != 2) {
                throw new IllegalArgumentException("Invalid URI: " + url);
            } else if (!TextUtils.isEmpty(where)) {
                throw new UnsupportedOperationException("WHERE clause not supported: " + url);
            } else {
                this.table = url.getPathSegments().get(0);
                this.where = "_id=" + ContentUris.parseId(url);                
                this.args = null;
            }
        }

        SqlArguments(Uri url) {
            if (url.getPathSegments().size() == 1) {
                table = url.getPathSegments().get(0);
                where = null;
                args = null;
            } else {
                throw new IllegalArgumentException("Invalid URI: " + url);
            }
        }
    }
}
