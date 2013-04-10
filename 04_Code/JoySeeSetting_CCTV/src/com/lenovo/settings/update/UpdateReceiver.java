package com.lenovo.settings.update;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.List;


public class UpdateReceiver extends BroadcastReceiver{
	
	private static final String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
	private static final String MANUAL_STARTED = "lenovo.intent.action.MANUAL_STARTED";
	private static final String TAG = "UpdateReceiver";
	private SharedPreferences mSharedSettings;
	private Context mContext;
	
	@Override
	public void onReceive(Context context, Intent intent) {

		mContext = context;
        final String action = intent.getAction();
        mSharedSettings = context.getSharedPreferences("com.lenovo.settings", Context.MODE_WORLD_READABLE
				| Context.MODE_WORLD_WRITEABLE);
        int update_mode = mSharedSettings.getInt("UpdateMode", UpdateStatus.UPDATE_MODE_ONLINE);
        Log.e(TAG,"action  = "+action);
        if (action.equals(BOOT_COMPLETED) || action.equals(MANUAL_STARTED)) {
        	if(update_mode == UpdateStatus.UPDATE_MODE_AUTO){
	        	//updateEth(context, intent);
        		if(isServiceRunning(UpdateService.UPDATE_SERVICE_NAME)){
        			Log.v(TAG,"UpdateReceiver Running!");
        			return;
        		}
        		update_thread();
        	}
        }else if(action.equals(UpdateStatus.UPDATE_DONWLOAD_FINISHED_ACTION)){
        	//String path = intent.getStringExtra("path");
        	//if(path != null){
        	//	showUpdateDialog(path);
        	//}
        }
        
	}	
	
	public boolean isServiceRunning(String serviceName) { 

        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningServiceInfo> infos = manager.getRunningServices(30); 
        for (RunningServiceInfo info : infos) { 
        	//Log.d(TAG,"Running service is "+info.service.getClassName());
            if (info.service.getClassName().equals(serviceName)) { 
                return true; 
            } 
        }
        return false; 
    } 
	
	public void update_thread() {
		new Thread(){

			@Override
			public void run() {
				UpdateCheck check = new UpdateCheck(mContext);
	            Log.d(TAG,"UpdateReceiver start!");
        		if(check.checkUpdate()){
        			check.startUpdateService();
		            Log.d(TAG,"UpdateService start ok!");
        		}
	            Log.d(TAG,"UpdateReceiver start1!");
			}
			
		}.start();
	}
	
}
