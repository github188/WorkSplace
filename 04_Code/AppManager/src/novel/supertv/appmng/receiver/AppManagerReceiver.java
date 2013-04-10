package novel.supertv.appmng.receiver;

import novel.supertv.appmng.Constants;
import novel.supertv.appmng.Utils;
import novel.supertv.appmng.db.DatabaseHelper.ApplicationColumn;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.util.Log;

public class AppManagerReceiver extends BroadcastReceiver{

	public static final String TAG = "novel.supertv.appmng.receiver.AppManagerReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "-------action="+intent.getAction());
		if(intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)){
            String packageName = intent.getDataString();
            Log.d(TAG, "------added---packageName="+packageName);
            int index = packageName.indexOf(":");
            if(index!=-1){
            	packageName=packageName.substring(index+1);
            }
            try {
				PackageInfo packInfo=context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
				ContentResolver contentResolver = context.getContentResolver();
				Cursor cur=contentResolver.query(ApplicationColumn.CONTENT_URI, new String[]{ApplicationColumn.PKGNAME}, ApplicationColumn.PKGNAME+"=? ", new String[]{packageName}, null);
				if(cur!=null&&cur.getCount()>0){
					ContentValues values = new ContentValues();
					values.put(ApplicationColumn.STATUS, Constants.APP_STATUS_ON);
					contentResolver.update(ApplicationColumn.CONTENT_URI, values, ApplicationColumn.PKGNAME+"=? ", new String[]{packageName});
				}else{
					ContentValues values = new ContentValues();
					values.put(ApplicationColumn.APPNAME,""+packInfo.applicationInfo.loadLabel(context.getPackageManager()));
					values.put(ApplicationColumn.PKGNAME,packInfo.packageName);
					values.put(ApplicationColumn.VERSION_NAME,packInfo.versionName);
					values.put(ApplicationColumn.VERSION_CODE,packInfo.versionCode);
					values.put(ApplicationColumn.ICON,Utils.DrawableToBytes(packInfo.applicationInfo.loadIcon(context.getPackageManager())));
					values.put(ApplicationColumn.STATUS, Constants.APP_STATUS_ON);
					contentResolver.insert(ApplicationColumn.CONTENT_URI, values);
				}
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}else if(intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)){
			String packageName = intent.getDataString();
        	Log.d(TAG,"--------removed------packageName="+packageName);
        	int index = packageName.indexOf(":");
            if(index!=-1){
                packageName=packageName.substring(index+1);
            }
            ContentValues values = new ContentValues();
			values.put(ApplicationColumn.STATUS, Constants.APP_STATUS_OFF);
			ContentResolver contentResolver = context.getContentResolver();
			contentResolver.update(ApplicationColumn.CONTENT_URI, values, ApplicationColumn.PKGNAME+"=? ", new String[]{packageName});
		}
	}

}
