package com.ismartv.ISTVStatusBar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.net.ethernet.EthernetManager;
import android.os.SystemProperties;

public class ISTVStatusBarReceiver extends BroadcastReceiver {
	static final String TAG = "ISTVStatusBarReceiver";
	private static final String ACTION_BOOT_COMPLETED ="android.intent.action.BOOT_COMPLETED";
	//private static final String ACTION_BOOT_COMPLETED ="com.amlogic.weather.TopStatus.onCreate";

	@Override
	public void onReceive(Context context, Intent intent) {
		
		if (ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
  			Log.d(TAG,"ISTVStatusBarReceiver");
  			context.startService(new Intent(context, ISTVStatusBarService.class));			
		}   
		else if(intent.getAction().equals("com.amlogic.weather.TopStatus.onDestroy")){
			//context.stopService(new Intent(context, ISTVStatusBarService.class));	
		}
					
	}
	
}
