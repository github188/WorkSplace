package com.joysee.appstore.db;

import java.util.List;

import com.joysee.appstore.common.ApplicationBean;
import com.joysee.appstore.common.Constants;
import com.joysee.appstore.provider.AppStoreProvider;
import com.joysee.appstore.utils.AppLog;
import com.joysee.appstore.utils.AppManager;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;


public  class DatabaseHelper extends SQLiteOpenHelper {
	
		public static final String TAG = "com.joysee.appstore.DatabaseHelper";
		
	 	private static final String DATABASE_NAME = "appstore.db";

	    private static final int DATABASE_VERSION = 1;//from  version =4 start create db

	    public static final String AUTHORITY = "com.joysee.appstore.authority";

	    public static final String TABLE_TASK = "downloadtask";

	    public static final String TABLE_THREAD = "downloadthread";
	    
	    public static final String TABLE_APPLICATIONS = "appstore_apps";
	    
	    public static final String TABLE_APPS_TEMP = "apps_temp";//从服务器中同步的部分应用,目前没有用到
	    
	    public static final String VIRTURAL_TASK_COMPLETE = "taskcomplete";
	    
	    public static final String TRIGGER_DELETE_TASK = "t_del_task";
	    
	    private static SQLiteOpenHelper mSQLiteOpenHelper =null;
	    
	    private static Context mContext;
	    
	 private static final String CREATE_DOWNLOAD_TASK_TABLE = "CREATE TABLE IF NOT EXISTS "+TABLE_TASK
	    		+" ("+DownloadTaskColumn.ID +" INTEGER primary key autoincrement ,"
	    		+DownloadTaskColumn.APPNAME +" TEXT UNIQUE,"
	    		+DownloadTaskColumn.DOWNDIR +" TEXT,"
	    		+DownloadTaskColumn.FINALFILENAME +" TEXT,"
	            +DownloadTaskColumn.TMPFILENAME +" TEXT,"
	            +DownloadTaskColumn.SUMSIZE +" integer,"
	            +DownloadTaskColumn.DOWNLOADSIZE +" integer,"
	            +DownloadTaskColumn.URL +" TEXT UNIQUE,"
	            +DownloadTaskColumn.STATUS +" INTEGER ,"
	            +DownloadTaskColumn.ICONURL +" TEXT,"
	            +DownloadTaskColumn.ICON +" BLOB,"	            
	            +DownloadTaskColumn.SERAPPID +" TEXT,"
	            +DownloadTaskColumn.APPTYPEID +" TEXT,"
	            +DownloadTaskColumn.TYPENAME +" TEXT,"
	            +DownloadTaskColumn.VERSION +" TEXT,"
	            +DownloadTaskColumn.PKGNAME + " TEXT )";

	    private static final String CREATE_DOWNLOAD_THREAD_TABLE = "CREATE TABLE IF NOT EXISTS "+TABLE_THREAD 
	    	+" ("+DownloadThreadColumn.ID +" INTEGER primary key autoincrement,"
	        +DownloadThreadColumn.TASK_ID +" INTEGER ,"
	        +DownloadThreadColumn.THREADID +" INTEGER ,"
	        +DownloadThreadColumn.POSITION+" INTEGER ,"
	        +DownloadThreadColumn.DOWNLENGTH+" INTEGER )";
	    
	    private static final String CREATE_APPSTORE_APPLICATIONS_TABLE = "CREATE TABLE IF NOT EXISTS "+TABLE_APPLICATIONS 
	    		+" ("+ApplicationColumn.ID +" INTEGER primary key autoincrement ,"
	            +ApplicationColumn.DOWNDIR +" TEXT,"
	            +ApplicationColumn.FILENAME +" TEXT,"
	            +ApplicationColumn.APPNAME +" TEXT ,"
	            +ApplicationColumn.URL +" TEXT,"
	            +ApplicationColumn.STATUS +" INTEGER ,"
	            +ApplicationColumn.PKGNAME+" TEXT ,"
	            +ApplicationColumn.ICON +" BLOB,"
	            +ApplicationColumn.VERSION+" TEXT,"
	            +ApplicationColumn.SERAPPID +" TEXT,"
	            +ApplicationColumn.APPTYPEID +" TEXT,"
	            +ApplicationColumn.TYPENAME +" TEXT ,"
	            +ApplicationColumn.APPSOURCE+" INTEGER DEFAULT 0 )";
	    
	    private static final String CREATE_DELETE_TASK_TRIGGER =  "CREATE TRIGGER IF NOT EXISTS "+TRIGGER_DELETE_TASK+" AFTER DELETE ON "+ TABLE_TASK 
	    		+" FOR EACH ROW BEGIN DELETE FROM "+ TABLE_THREAD +" WHERE "+DownloadThreadColumn.TASK_ID+ "= OLD._id; END";
	    
	    private static final String CREATE_APPS_TEMP_TABLE = "CREATE TABLE IF NOT EXISTS "+TABLE_APPS_TEMP
	            + " ("+ AppsTempColumn.ID +" INTEGER primary key autoincrement ,"
	            + AppsTempColumn.APPNAME + " TEXT, "
	            + AppsTempColumn.PKGNAME + " TEXT, "
	            + AppsTempColumn.STATUS + " INTEGER, "
	            + AppsTempColumn.VERSON + " TEXT, "
	            + AppsTempColumn.SUMMARY + " TEXT, "
	            + AppsTempColumn.NATIMAGEURL + " TEXT, "
	            + AppsTempColumn.SERIMAGEURL + " TEXT, "
	            + AppsTempColumn.APKURL+" TEXT)";

	    /**
	     * DatabaseHelper constructor
	     * @param context
	     */
        private DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mContext = context;
        }
        
        public static SQLiteOpenHelper  getInstance(Context context){
        	if(mSQLiteOpenHelper==null){
        		synchronized(DatabaseHelper.class){
        			if(mSQLiteOpenHelper==null){
        				mSQLiteOpenHelper = new DatabaseHelper(context);
        			}
        		}
        	}
        	return mSQLiteOpenHelper;
        }
        
        /**
         * create database
         */
        public void onCreate(SQLiteDatabase db) {
        	
        	AppLog.d(TAG, "-----------------onCreate db start()-------------------");
            db.execSQL(CREATE_DOWNLOAD_TASK_TABLE);
            db.execSQL(CREATE_DOWNLOAD_THREAD_TABLE);
            db.execSQL(CREATE_APPSTORE_APPLICATIONS_TABLE);
            db.execSQL(CREATE_DELETE_TASK_TRIGGER);
            db.execSQL(CREATE_APPS_TEMP_TABLE);
            AppLog.d(TAG, "------------------onCreate db end()----------------------");
            
            //向CREATE_APPSTORE_APPLICATIONS_TABLE中插入所有应用数据
            AppManager manager = new AppManager(mContext);
            List<ApplicationBean> apps = manager.getAllInstallApp(); 
            AppLog.d(TAG, "@@@----------getThirdApp------count="+apps.size());
            if(db.isOpen()){
            	for(ApplicationBean app : apps){
            		ContentValues application = new ContentValues();
            		application.put(ApplicationColumn.APPNAME, app.getAppName());
            		application.put(ApplicationColumn.STATUS, Constants.APP_STATUS_INSTALLED);
            		application.put(ApplicationColumn.PKGNAME, app.getPkgName());
            		application.put(ApplicationColumn.VERSION, app.getVersion());
            		application.put(ApplicationColumn.ICON, app.getIcon());
            		application.put(ApplicationColumn.APPSOURCE, app.getAppSource());
            		db.insert(DatabaseHelper.TABLE_APPLICATIONS, null, application);
            	}
            }
        }

        /**
         * 数据库升级
         */
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	AppLog.d(TAG, "------------------onUpgrade triggered-----------oldVersion="+oldVersion+";newVersion="+newVersion);
            
//            if (oldVersion < 6) {
//            	
//                db.beginTransaction();
//                try {
//                	//由于要插入字段，如果有正在下载的就删除
//                	db.delete(DatabaseHelper.TABLE_TASK, DownloadTaskColumn.ID+">0",null);
//                	db.delete(DatabaseHelper.TABLE_THREAD, DownloadThreadColumn.ID+">0",null);
//                	
//                    db.execSQL(" ALTER TABLE "+TABLE_TASK +" ADD COLUMN "+DownloadTaskColumn.PKGNAME+" TEXT ;");
//                    
//                    db.execSQL(" ALTER TABLE "+TABLE_APPLICATIONS +" ADD COLUMN "+ApplicationColumn.APPSOURCE+" INTEGER	DEFAULT 0 ;");
//                    db.setTransactionSuccessful();
//                    AppLog.d(TAG,"-----------upgrade-success---------");
//                } catch (SQLException ex) {
//                    Log.e(TAG, ex.getMessage(), ex);
//                } finally {
//                    db.endTransaction();
//                }
//                AppLog.d(TAG, "--------insert--begin-----");
//                AppManager manager = new AppManager(mContext);
//                List<ApplicationBean> apps = manager.getAllInstallApp(); 
//                for(ApplicationBean app : apps){
//                	AppLog.d(TAG, "--------insert--query-----");
//                	Cursor cursor=db.query(TABLE_APPLICATIONS, null, ApplicationColumn.PKGNAME+" = ? ", new String[]{app.getPkgName()}, null, null, null);
//                	if(cursor!=null&&cursor.moveToFirst()){
//                		String version=cursor.getString(cursor.getColumnIndex(ApplicationColumn.VERSION));
//                		AppLog.d(TAG, "-----------------pkgname="+app.getPkgName()+"--------is---------exit,version="+version);
//                		ContentValues application = new ContentValues();
//            			application.put(ApplicationColumn.APPSOURCE, app.getAppSource());
//            			application.put(ApplicationColumn.VERSION, app.getVersion());
//            			int status=cursor.getInt(cursor.getColumnIndex(ApplicationColumn.STATUS));
//            			if(status==Constants.APP_STATUS_UPDATE){
//            				application.put(ApplicationColumn.STATUS, Constants.APP_STATUS_INSTALLED);
//            			}
//            			db.update(DatabaseHelper.TABLE_APPLICATIONS, application, ApplicationColumn.PKGNAME+" = ? ", new String[]{app.getPkgName()});
//                	}else{
//                		AppLog.d(TAG, "-----------------pkgname="+app.getPkgName()+"--------is----not-----exit");
//                		ContentValues application = new ContentValues();
//                		application.put(ApplicationColumn.APPNAME, app.getAppName());
//                    	application.put(ApplicationColumn.STATUS, Constants.APP_STATUS_INSTALLED);
//                    	application.put(ApplicationColumn.PKGNAME, app.getPkgName());
//                    	application.put(ApplicationColumn.VERSION, Constants.DEFAULT_VERSION);
//                    	application.put(ApplicationColumn.ICON, app.getIcon());
//                    	application.put(ApplicationColumn.APPSOURCE, app.getAppSource());
//                    	db.insert(DatabaseHelper.TABLE_APPLICATIONS, null, application);
//                	}
//                	cursor.close();
//            	}
//                AppLog.d(TAG, "--------insert--end-----");
//            }
        	return;
        }

    
    
    public static final class DownloadTaskColumn  {
    	
    	public static final Uri CONTENT_URI = Uri.parse("content://" + AppStoreProvider.AUTHORITY
                + "/downloadtask");
    	
        public static final String ID = "_id";
        
        public static final String APPNAME = "appname" ;
        
        public static final String DOWNDIR= "downdir";
        
        public static final String FINALFILENAME= "finalfilename";
        
        public static final String TMPFILENAME= "tmpfilename";

        public static final String SUMSIZE = "sumsize";
        
        public static final String DOWNLOADSIZE = "downloadsize";
        
        public static final String URL = "url";
        
        public static final String STATUS = "status";
        
        public static final String ICONURL = "iconurl";

        public static final String ICON = "icon";
        
        public static final String SERAPPID ="serappid";
        
        public static final String APPTYPEID="typeid";
        
        public static final String TYPENAME ="typename";
        
        public static final String VERSION = "version";
        
        public static final String PKGNAME = "pkgname" ;
        
        public static final Uri Download_URI = Uri.withAppendedPath(CONTENT_URI, DOWNLOADSIZE);

    }
    
    public static final class DownloadTaskColumnIndex {
		public static final int ID = 0;
		public static final int APPNAME = 1;
		public static final int DOWNDIR = 2;
		public static final int FINALFILENAME = 3;
		public static final int TMPFILENAME = 4;
		public static final int SUMSIZE = 5;
		public static final int DOWNLOADSIZE = 6;
		public static final int URL = 7;
		public static final int STATUS = 8;
		public static final int ICONURL = 9;
		public static final int ICON = 10;
		public static final int SERAPPID =11;        
        public static final int APPTYPEID=12;        
        public static final int TYPENAME =13;        
        public static final int VERSION = 14;
        public static final int PKGNAME = 15;
	}
    
    public static final class DownloadThreadColumn  {
    	
    	public static final Uri CONTENT_URI = Uri.parse("content://" + AppStoreProvider.AUTHORITY
                + "/downloadthread");
    	
    	public static final String ID = "_id";

        public static final String TASK_ID = "task_id";

        public static final String THREADID = "threadid";

        public static final String POSITION = "position";
        
        public static final String DOWNLENGTH = "downlength";

    }
    
    public static final class DownloadThreadColumnIndex {
		public static final int ID = 0;
		public static final int TASK_ID = 1;
		public static final int THREADID = 2;
		public static final int POSITION = 3;
		public static final int DOWNLENGTH = 4;
	}
    
    public static final class ApplicationColumn  {
    	
    	public static final Uri CONTENT_URI = Uri.parse("content://" + AppStoreProvider.AUTHORITY
                + "/appstore_apps");

        public static final String ID = "_id";

        public static final String DOWNDIR= "downdir";
        
        public static final String FILENAME= "filename";
        
        public static final String APPNAME = "name" ;
        
        public static final String URL = "url";
        
        public static final String STATUS = "status";

        public static final String PKGNAME = "pkgname";

        public static final String ICON = "icon";
        
        public static final String VERSION = "version";
        
        public static final String SERAPPID ="serappid";
        
        public static final String APPTYPEID="typeid";
        
        public static final String TYPENAME ="typename";
        
        public static final String APPSOURCE = "appsource";//是否第三方应用
    }
    
    public static final class ApplicationColumnIndex {
		public static final int ID = 0;
		public static final int DOWNDIR = 1;
		public static final int FILENAME = 2;
		public static final int APPNAME = 3;
		public static final int URL = 4;
		public static final int STATUS = 5;
		public static final int PKGNAME = 6;
		public static final int ICON = 7;
		public static final int VERSION = 8;
		public static final int SERAPPID =9;        
        public static final int APPTYPEID=10;        
        public static final int TYPENAME =11;   
        public static final int APPSOURCE=12;
	}
    
    public static final class TaskCompleteColumn {
    	public static final Uri CONTENT_URI = Uri.parse("content://" + AppStoreProvider.AUTHORITY
                + "/taskcomplete");
    }
    
    public static final class AppsTempColumn{
        public static final String ID="_id";
        public static final String APPNAME="appName";
        public static final String PKGNAME="pkgName";
        public static final String STATUS="status";
        public static final String VERSON="verson";
        public static final String SUMMARY="summary";
        public static final String NATIMAGEURL="natImageUrl";
        public static final String SERIMAGEURL="serImageUrl";
        public static final String APKURL="apkUrl";
    }
    public static final class AppsTempColumnIndex{
        public static final int ID=0;
        public static final int APPNAME=1;
        public static final int PKGNAME=2;
        public static final int STATUS=3;
        public static final int VERSON=4;
        public static final int SUMMARY=5;
        public static final int NATIMAGEURL=6;
        public static final int SERIMAGEURL=7;
        public static final int APKURL=8;
    }
    
}
