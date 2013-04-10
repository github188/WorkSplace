package com.joysee.appstore.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import android.util.Log;

public class AppLog {
    private static final String App_Tag = "AppStore";
     
    /**
     * 支持将log写入SDCARD文件
     */
    public static final boolean ENABLE_LOG_FILE = false; 
    public static final boolean TEST= false;
    public static final String SDCARD_LOG_FILE = "/sdcard/"+App_Tag+".txt" ;
    
    public static void d(String msg){
    	d(App_Tag,msg);
    }
    
	public static void d(String TAG, String msg) {
		if (!AppStoreConfig.DEBUG)
			return;
		if (ENABLE_LOG_FILE) {
			log(TAG, msg);
		}
		Log.d(TAG, formatMsg(TAG, msg));

	}
    	
	public static void e(String msg){
    	e(App_Tag,msg);
    }
    
	public static void e(String TAG, String msg) {
		if (AppStoreConfig.DEBUGE){
			Log.d(TAG, formatMsg(TAG, msg));
		}
	}
	
    
    private static String formatMsg(String tag, String msg) {
        return tag + " - " + msg;
    }
    
    /**
     * Log on sdcard
     * @param tag
     * @param msg
     */
    private static void log(String tag,String msg){
    	if(android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)){
    		java.io.File SDFile =new java.io.File(SDCARD_LOG_FILE);
    		if(!SDFile.exists()){
    			try {
					SDFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
    		}
    		try{
	    		 FileOutputStream outputStream = new FileOutputStream(SDCARD_LOG_FILE,true);
	             outputStream.write((new Date().toLocaleString()+"["+tag+"]"+" : "+msg+"\r\n").getBytes());
	             outputStream.flush();
	             outputStream.close();
    		}catch (IOException e) {
				e.printStackTrace();
			}
    	}
    }

}
