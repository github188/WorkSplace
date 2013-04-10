package com.ismartv.receiver;

import com.ismartv.util.Constants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ISTVVodReveiver extends BroadcastReceiver{
	
	private static final String TAG="ISTVVodReveiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "----------------action="+intent.getAction());
		if(intent.getAction().equals(Constants.INTENT_USER_LOGOUT)){
//			ISTVService service=ISTVService.getService();
//			service.getEpg().setAccessToken(null);
//			service.getEpg().setGotBookmarkList(false);
			android.os.Process.killProcess(android.os.Process.myPid());
		}
	}

}
