package novel.supertv.appmng.db;

import java.util.List;

import novel.supertv.appmng.Constants;
import novel.supertv.appmng.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper{
	
	public static final String TAG = "novel.supertv.appmng.db.DatabaseHelper";
	private Context mCon;
	
	private static final int VERSION = 1;
 	private static final String NAME = "myapp.db";
 	public static final String TABLE_APPS = "application";
 	public static final String APPS_LIMIT = "limit";
 	private static SQLiteOpenHelper mSQLiteOpenHelper =null;


	public DatabaseHelper(Context context) {
		super(context, NAME, null, VERSION);
		mCon=context;
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

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "----------onCreate---start-----------");
		db.execSQL(CREATE_TABLE_APPLICATION);
		Log.d(TAG, "----------onCreate---end-----------");
		
		List<PackageInfo> packageInfos = mCon.getPackageManager().getInstalledPackages(0);
		for (int i = 0; i < packageInfos.size(); i++) { 
			PackageInfo temp = packageInfos.get(i);
			if(Utils.filterApp(temp.applicationInfo)!=Constants.APP_SOURCE_INNER){
				ContentValues application = new ContentValues();
				application.put(ApplicationColumn.APPNAME,""+temp.applicationInfo.loadLabel(mCon.getPackageManager()));
				application.put(ApplicationColumn.PKGNAME, temp.packageName);
				application.put(ApplicationColumn.ICON, Utils.DrawableToBytes(temp.applicationInfo.loadIcon(mCon.getPackageManager())));
				application.put(ApplicationColumn.VERSION_NAME, temp.versionName);
				application.put(ApplicationColumn.VERSION_CODE, temp.versionCode);
				db.insert(TABLE_APPS, null, application);
			}else if(Constants.SHOWSYSTEMAPP.contains(temp.packageName)){
				ContentValues application = new ContentValues();
				application.put(ApplicationColumn.APPNAME,""+temp.applicationInfo.loadLabel(mCon.getPackageManager()));
				application.put(ApplicationColumn.PKGNAME, temp.packageName);
				application.put(ApplicationColumn.ICON, Utils.DrawableToBytes(temp.applicationInfo.loadIcon(mCon.getPackageManager())));
				application.put(ApplicationColumn.VERSION_NAME, temp.versionName);
				application.put(ApplicationColumn.VERSION_CODE, temp.versionCode);
				db.insert(TABLE_APPS, null, application);
			}
		}
		Log.d(TAG, "----------onCreate---insert--end-----------");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}
	
	public static final String CREATE_TABLE_APPLICATION = "CREATE TABLE IF NOT EXISTS "+TABLE_APPS
			+" ("+ApplicationColumn.ID + " INTEGER primary key autoincrement ,"
			+ApplicationColumn.APPNAME + " TEXT,"
			+ApplicationColumn.PKGNAME + " TEXT,"
			+ApplicationColumn.ICON + " BLOB,"
			+ApplicationColumn.VERSION_NAME + " TEXT,"
			+ApplicationColumn.VERSION_CODE + " INTEGER ,"
			+ApplicationColumn.COUNTS + " INTEGER DEFAULT 0," 
			+ApplicationColumn.STATUS + " INTEGER DEFAULT 0 )";
	
	public static final class ApplicationColumn  {
		public static final Uri CONTENT_URI = Uri.parse("content://" + Constants.AUTHORITY + "/"+TABLE_APPS);
		public static final String ID = "_id";
		public static final String APPNAME = "appname";
		public static final String PKGNAME = "pkgname";
		public static final String ICON = "icon";
        public static final String VERSION_NAME = "version_name";
        public static final String VERSION_CODE = "version_code";
        public static final String COUNTS = "counts";//点击次数
        public static final String STATUS = "status";//状态，0表示生效，1表示无效，主要是用于卸载、升级用的
	}
	
	public static final class ApplicationColumnIndex  {
		public static final int ID = 0;
		public static final int APPNAME = 1;
		public static final int PKGNAME = 2;
		public static final int ICON = 3;
        public static final int VERSION_NAME = 4;
        public static final int VERSION_CODE = 5;
        public static final int COUNTS = 6;//点击次数
        public static final int STATUS =7;
	}

}
