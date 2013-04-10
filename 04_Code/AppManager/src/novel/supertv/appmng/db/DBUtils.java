package novel.supertv.appmng.db;

import novel.supertv.appmng.Constants;
import novel.supertv.appmng.Utils;
import novel.supertv.appmng.db.DatabaseHelper.ApplicationColumn;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class DBUtils {

    private String TAG = "novel.supertv.appmng.db.DBUtils";
    
    private Context mContext=null;
    
    public DBUtils(Context context) {
        mContext = context;
    }
	
    public int updateAppStatusByPkgName(String pkgname,int status){
    	Log.d(TAG, "------updateAppStatusByPkgName---");
    	int count = 0;
    	ContentResolver contentResolver = mContext.getContentResolver();
    	ContentValues application = new ContentValues();
    	application.put(ApplicationColumn.STATUS, status);
    	count=contentResolver.update(ApplicationColumn.CONTENT_URI, application, ApplicationColumn.PKGNAME+"= ?", new String[]{pkgname});
    	if(count>0){
    		mContext.getContentResolver().notifyChange(ApplicationColumn.CONTENT_URI, null);
    	}
    	return count;
    }
    
    public void getCountByName(){
    	ContentResolver contentResolver = mContext.getContentResolver();
//    	Cursor cur=contentResolver.query(ApplicationColumn.CONTENT_URI,null,null, null, ApplicationColumn.COUNTS+" desc");
    	Uri uri=Uri.parse("content://novel.supertv.appmng.authority/application/limit");
    	Cursor cur=contentResolver.query(uri,new String[]{"appname","pkgname","counts"},"status=?", new String[]{"0"}, " counts desc");
    	if(cur!=null){
    		while(cur.moveToNext()){
    			Log.d(TAG, "----------appname="+cur.getString(0)+";pkgname"+cur.getString(1)+";count="+cur.getString(2));
    		}
    	}
    }
    
    public int addAppCountsByPkgName(String pkgname){
    	Log.d(TAG, "------updateAppStatusByPkgName---");
    	int count = 0;
    	ContentResolver contentResolver = mContext.getContentResolver();
    	Cursor cur=contentResolver.query(ApplicationColumn.CONTENT_URI, new String[]{ApplicationColumn.COUNTS}, ApplicationColumn.PKGNAME+"=? ", new String[]{pkgname}, null);
    	if(cur!=null&&cur.getCount()==1){
    		if(cur.moveToFirst()){
    			ContentValues values = new ContentValues();
    			values.put(ApplicationColumn.STATUS, Constants.APP_STATUS_ON);
    			values.put(ApplicationColumn.COUNTS, cur.getInt(0)+1);
    			Log.d(TAG, "-------update:counts="+cur.getInt(0));
    			contentResolver.update(ApplicationColumn.CONTENT_URI, values, ApplicationColumn.PKGNAME+"=? ", new String[]{pkgname});
    		}else{
    			Log.d(TAG, "-------cursor---is--error");
    		}
    	}else{
    		PackageInfo packInfo;
			try {
				packInfo = mContext.getPackageManager().getPackageInfo(pkgname, PackageManager.GET_UNINSTALLED_PACKAGES);
				ContentValues values = new ContentValues();
				values.put(ApplicationColumn.APPNAME,""+packInfo.applicationInfo.loadLabel(mContext.getPackageManager()));
				values.put(ApplicationColumn.PKGNAME,packInfo.packageName);
				values.put(ApplicationColumn.VERSION_NAME,packInfo.versionName);
				values.put(ApplicationColumn.VERSION_CODE,packInfo.versionCode);
				values.put(ApplicationColumn.ICON,Utils.DrawableToBytes(packInfo.applicationInfo.loadIcon(mContext.getPackageManager())));
				values.put(ApplicationColumn.STATUS, Constants.APP_STATUS_ON);
				values.put(ApplicationColumn.COUNTS, 1);
				contentResolver.insert(ApplicationColumn.CONTENT_URI, values);
				Log.d(TAG, "-------insert:appname="+packInfo.applicationInfo.loadLabel(mContext.getPackageManager())+";packagename="+packInfo.packageName);
			} catch (NameNotFoundException e) {
				Log.d(TAG, "-------NameNotFoundException----");
				e.printStackTrace();
			}
    	}
    	return count;
    }
    
}
