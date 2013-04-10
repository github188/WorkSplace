package com.lenovo.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.ethernet.EthernetManager;
import android.net.ethernet.EthernetStateTracker;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.lenovo.settings.EnforceUpdate.EnforceUpdateCheck;
import com.lenovo.settings.EnforceUpdate.EnforceUpdateService;
import com.lenovo.settings.Object.UpdateData;
import com.lenovo.settings.Util.FileUtils;
import com.lenovo.settings.Util.MD5;
import com.lenovo.settings.update.UpdateCheck;
import com.lenovo.settings.update.UpdateService;
import com.lenovo.settings.update.UpdateStatus;

public class SettingBroadcastReceiver extends BroadcastReceiver{	

	private static final String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
	private static final String TAG = "SettingBroadcastReceiver";
	private boolean mReadJsonStart = false;
	private boolean mAutoUpdateStart = false;
	private boolean mEnforceUpdateStart = false;
	private Context mContext = null;
	private boolean DEBUG = true;
	
	private SharedPreferences mShareData;
	private int mUpdateMode;
	
    private static final long timing = 4 * 3600 * 1000L;
	
	private static final String START_SERVICE_INTENT = "com.joysee.action.START_SERVICE";
	
	public static final String Update_SharePre = "update_state_pre";
	public static final int MSG_CHECKUPDATE = 0;
	public static final int MSG_CHECKUPDATE_AUTO = 1;
	
	public Handler mHandler = new Handler(){
	    @Override
	    public void handleMessage(Message msg) {
	        // TODO Auto-generated method stub
	        switch(msg.what){
	            case MSG_CHECKUPDATE:
	                requestTime(mContext);
	                checkUpdateState();
	                break;
	            case MSG_CHECKUPDATE_AUTO:
	                startAutoUpdateThread();
	                break;
	        }
	        super.handleMessage(msg);
	    }
	};

	@Override
    public void onReceive(Context context, Intent intent) {
        
        final String action = intent.getAction();
        if (DEBUG) {
            Log.d(TAG, "---------------action = " + action);
        }
        mContext = context;
        
        if (action.equals("android.intent.action.BOOT_COMPLETED")) {
            doAlarmTask();
            mHandler.sendEmptyMessageDelayed(MSG_CHECKUPDATE, 15000);
        } else if (action.equals(START_SERVICE_INTENT)) {
            if (DEBUG) {
                Log.d(TAG, " ============enter alarm task =====");
            }
            mHandler.sendEmptyMessageDelayed(MSG_CHECKUPDATE_AUTO, 10000);
        } else if (action.equals(SystemUpdate.START_ALARM_INTENT)) {
            if (DEBUG) {
                Log.d(TAG, " ============enter START_ALARM_INTENT connected =====mUpdateMode="
                        + mUpdateMode);
            }
            mHandler.sendEmptyMessageDelayed(MSG_CHECKUPDATE_AUTO, 10000);
            doAlarmTask();
        } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            if (checkNet(context)) {
                if (DEBUG) {
                    Log.d(TAG, " ============enter internet connected =====");
                }
                requestTime(context);
            }
        }
    }

	
	private void doAlarmTask(){
		AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		PendingIntent sender = PendingIntent.getBroadcast(mContext, 0, new Intent(START_SERVICE_INTENT), 0);
		am.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), timing, sender);
	}
	
	
    private void handleWireless(DetailedState state) {
        if (DEBUG) {
            Log.e(TAG, " -----------------wifi state = " + state);
        }
        if (state == DetailedState.CONNECTED) {
            // startCityDataService();
        	startAutoUpdateService();
            startEnforceUpdateService();
        }
    }


	private void handleEth(Context context, Intent intent) {
		final int event = intent.getIntExtra(EthernetManager.EXTRA_ETH_STATE, EthernetStateTracker.EVENT_HW_DISCONNECTED);
	    Log.e(TAG, "updateEth event=" + event);
	    switch(event){
//    update by yuhongkun 20120806
	    case EthernetStateTracker.EVENT_HW_PHYCONNECTED: 
	    case EthernetStateTracker.EVENT_INTERFACE_CONFIGURATION_SUCCEEDED:
//        	startCityDataService();
        	startAutoUpdateService();
//        	startEnforceUpdateService();
	    	break;
	    }
		
	}

	
	/**
     * 判断网络是否可用
     * @param context
     * @return
     */
    public  boolean checkNet(Context context) {  
        
        ConnectivityManager manager = (ConnectivityManager) context  
               .getApplicationContext().getSystemService(  
                      Context.CONNECTIVITY_SERVICE);  
        if (manager == null) {  
            return false;  
        }  
        NetworkInfo networkinfo = manager.getActiveNetworkInfo();  
         
        if (networkinfo == null || !networkinfo.isAvailable()) {  
            return false;  
        }  
    
        return true;  
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
	
    void startAutoUpdateService() {
        Log.d(TAG, " startAutoUpdateService begin ");
        if (mAutoUpdateStart) {
            return;
        }
        
        mAutoUpdateStart = true;
        if (isServiceRunning(UpdateService.UPDATE_SERVICE_NAME)) {
            Log.d(TAG, " UpdateReceiver Running! ");
            return;
        }
        startAutoUpdateThread();
        Log.d(TAG, " startAutoUpdateService end ");
    }

    void startAutoUpdateThread() {
        Log.d(TAG, " startAutoUpdateThread begin! ");
        if (isServiceRunning(UpdateService.UPDATE_SERVICE_NAME)) {
            Log.d(TAG, " UpdateReceiver Running! ");
            return;
        }
        new Thread() {
            @Override
            public void run() {
                UpdateCheck check = new UpdateCheck(mContext);
                int count = 3;
                while ((count--) > 0) {
                    boolean state = check.checkUpdate();
                    if (state) {
                        check.startUpdateService();
                        Log.d(TAG, "UpdateService start ok!");
                        return;
                    } else {
                        Log.d(TAG, " startAutoUpdateThread state = " + state);
                    }
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        
                        e.printStackTrace();
                    }
                    Log.d(TAG, "startAutoUpdateThread count = " + count);
                }
            }
            
        }.start();
        Log.d(TAG, " startAutoUpdateThread end! ");
    }

	//启动强制升级service
	void startEnforceUpdateService(){
		if(mEnforceUpdateStart){
			return;
		}
		
		mEnforceUpdateStart = true;
    	//updateEth(context, intent);
		if(isServiceRunning(EnforceUpdateService.UPDATE_SERVICE_NAME)){
			Log.e(TAG,"UpdateReceiver Running!");
			return;
		}
		startEnforceUpdateThread();
	}
	
	void startEnforceUpdateThread(){

		new Thread(){

			@Override
			public void run() {
				EnforceUpdateCheck check = new EnforceUpdateCheck(mContext);
				int count = 3;
				Log.e(TAG,"startAutoUpdateThread ok!");
				while((count -- ) > 0){
	        		if(check.checkUpdate()){
	        			check.startUpdateService();
			            Log.e(TAG,"EnforceUpdateThread start ok!");
			            return;
	        		}
	        		try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					}
					Log.e(TAG,"startEnforceUpdateThread count = "+count);
				}
			}
			
		}.start();
	}
    
    /**
     * 校验时间
     */
    private void requestTime(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netWorkInfo = cm.getActiveNetworkInfo();
        if (netWorkInfo != null) {
            new Thread() {
                public void run() {
                    try {
                        URL url = new URL("http://open.baidu.com/special/time/");
                        URLConnection con = url.openConnection();
                        con.connect();
                        long time = con.getDate();
                        if (time != 0) {
                            SystemClock.setCurrentTimeMillis(time + 8 * 60 * 60 * 1000);
                        }
//                        Date date = new Date(time + 8 * 60 * 60 * 1000); 
//                        Log.d(TAG, "--------------- requestTime time = " + time
//                                + " toLocaleString = " + date.toLocaleString() + " toGMTString = "
//                                + date.toGMTString() + " " + date.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    public void checkUpdateState() {
        String version = mContext.getSharedPreferences(SettingBroadcastReceiver.Update_SharePre,
                Context.MODE_WORLD_READABLE).getString("Version", "");
        String mMd5 = mContext.getSharedPreferences(SettingBroadcastReceiver.Update_SharePre,
                Context.MODE_WORLD_READABLE).getString("md5", "");
        String path = mContext.getSharedPreferences(SettingBroadcastReceiver.Update_SharePre,
                Context.MODE_WORLD_READABLE).getString("path", "");
        Long size = mContext.getSharedPreferences(SettingBroadcastReceiver.Update_SharePre,
                Context.MODE_WORLD_READABLE).getLong("size", 0);
        if (version != null && version != "") {
            Log.d(TAG, " checkUpdateState version = " + version + " build id = " + Build.ID
                    + " mMd5 = " + mMd5 + " path = " + path + " size = " + size);
            if (version.compareTo(Build.ID) > 0) {
                Log.d(TAG, "-----version.compareTo(Build.ID) > 0 -------");
            } else {
                File file = new File(path);
                Log.d(TAG, "  file.exists() = " + file.exists());
                if (file.exists()) {
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(file);
//                        if (fis.available() == size && checkMD5(path, mMd5)) {
                        if (checkMD5(path, mMd5)) {
                            Log.d(TAG, " -----   Delete  ");
                            file.delete();
                        }
                        fis.close();
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }finally{
                        try {
                            if (fis != null) {
                                fis.close();
                            }
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
    
    public boolean checkMD5(String file, String md5) {
        MD5 mMD5 = new MD5();
        String md5_str = mMD5.md5sum(file);
        if (md5_str == null || file == null) {
            Log.e(TAG, "get download file md5 string error!");
            return false;
        }
        else {
            if (UpdateStatus.DEBUG)
                Log.d(TAG, "file md5:" + md5_str + " and url md5:" + md5);
            if (md5_str.equalsIgnoreCase(md5)) {
                return true;
            }
            else {
                Log.e(TAG, "the download file md5 string is not equal string from network!");
                return false;
            }
        }
    }

}
