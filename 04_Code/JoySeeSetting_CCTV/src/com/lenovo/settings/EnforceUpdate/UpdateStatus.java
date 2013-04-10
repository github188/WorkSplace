package com.lenovo.settings.EnforceUpdate;

import android.content.Context;
import android.content.Intent;

public class UpdateStatus {
	public static final String UPDATE_MSG_CHANGE_ACTION = "com.lenovo.Update.MsgChanged";
	public static final String UPDATE_DONWLOAD_FINISHED_ACTION = "lenovo.intent.action.DOWNLOAD_COMPLETED";

	public static final String UPDATE_ATTRIBUTE_NORMAL = "normal";
	public static final String UPDATE_ATTRIBUTE_IMPORTANC = "importance";
	public static final String UPDATE_ATTRIBUTE_ENFORCE = "enforce";

	public static final int UPDATE_MODE_AUTO = 0;
	public static final int UPDATE_MODE_MANUAL = 1;
	public static final int UPDATE_MODE_LOCAL = 2;
	
	public static final boolean DEBUG = true;
	
	
	private static String mTitleMsg = "";
	private static String mDownloadMsg = "";
	private static String mCheckMsg = "";
	
	private static boolean mIsError = false;
	
	public static void clearUpdateStatus(){
		mTitleMsg = "";
		mDownloadMsg = "";
		mCheckMsg = "";
		mIsError = false;
	}

	public static void setError(boolean error){
		mIsError = error;
	}
	
	public static boolean isError(){
		return mIsError;
	}
	
	public static void setTitleMsg(String msg){
		mTitleMsg = msg;
	}
	
	public static String getTitleMsg(){
		return mTitleMsg;
	}
	
	public static void setDownloadMsg(String msg){
		mDownloadMsg = msg;
	}
	
	public static String getDownloadMsg(){
		return mDownloadMsg;
	}
	
	public static void setCheckMsg(String msg){
		mCheckMsg = msg;
	}
	
	public static String getCheckMsg(){
		return mCheckMsg;
	}
	
	public static void sendBroadcastReceiver(Context context,String action){
		Intent intent = new Intent(action);       
		context.sendBroadcast(intent); 	
	}
}
