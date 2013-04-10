package com.lenovo.settings.EnforceUpdate;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.lenovo.settings.R;
import com.lenovo.settings.Object.UpdateData;


import android.app.ActivityManager;
import android.app.Dialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.RecoverySystem;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class EnforceUpdateReceiver extends BroadcastReceiver{
	
	private static final String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
	private static final String MANUAL_STARTED = "lenovo.intent.action.MANUAL_STARTED";
	private static final String TAG = "EnforceUpdateReceiver";
	private Context mContext;
	
	@Override
	public void onReceive(Context context, Intent intent) {

		mContext = context;
        final String action = intent.getAction();
        Log.e(TAG,"action  = "+action);
        if (action.equals(BOOT_COMPLETED) || action.equals(MANUAL_STARTED)) {
	        	//updateEth(context, intent);
        		if(isServiceRunning(EnforceUpdateService.UPDATE_SERVICE_NAME)){
        			Log.v(TAG,"UpdateReceiver Running!");
        			return;
        		}
        		update_thread();
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
				EnforceUpdateCheck check = new EnforceUpdateCheck(mContext);
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
