package com.joysee.launcher.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import android.util.Log;

public class LauncherLog {
    private static final String App_Tag = "Launcher";
    
    public static final boolean DBG = true;
    public static final boolean DBGE = true;
    public static final boolean LogSD=false; 
    public static final boolean TEST= false;
    
    public static void log_D(String TAG,String msg){
        if(DBG){
            Log.d(TAG, msg);
        }
    }
    
    public static void log_E(String TAG,String msg){
        if(DBGE){
            Log.d(TAG, msg);
        }
    }
    
    public static String makeTag(Class cls){
    	return "Launcher_"+cls.getSimpleName();
    }
    
    private static String formatMsg(String tag, String msg) {
        return tag + " - " + msg;
    }

    public static void e(String tag, String msg) {
        android.util.Log.e(App_Tag, formatMsg(tag, msg));
    }

    public static void e(String tag, String msg, Throwable tr) {
        android.util.Log.e(App_Tag, formatMsg(tag, msg), tr);
    }

    public static void w(String tag, String msg) {
        android.util.Log.w(App_Tag, formatMsg(tag, msg));
    }

    public static void w(String tag, String msg, Throwable tr) {
        android.util.Log.w(App_Tag, formatMsg(tag, msg), tr);
    }

    public static void i(String tag, String msg) {
    	if(!DBG)
    		return;
        android.util.Log.i(App_Tag, formatMsg(tag, msg));
    }

    public static void i(String tag, String msg, Throwable tr) {
        android.util.Log.i(App_Tag, formatMsg(tag, msg), tr);
    }

    public static void d(String tag, String msg) {
    	if(!DBG)
    		return;
    	if(LogSD){
    		log(tag,msg);
    	}
        android.util.Log.d(App_Tag, formatMsg(tag, msg));
    }

    public static void d(String tag, String msg, Throwable tr) {
        android.util.Log.d(App_Tag, formatMsg(tag, msg), tr);
    }

    public static void v(String tag, String msg) {
        android.util.Log.v(App_Tag, formatMsg(tag, msg));
    }

    public static void v(String tag, String msg, Throwable tr) {
        android.util.Log.v(App_Tag, formatMsg(tag, msg), tr);
    }
    
    //Log on sdcard
    public static void log(String tag,String msg){
    	if(android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)){
    		String file="/sdcard/"+App_Tag+".txt";
    		java.io.File SDFile =new java.io.File(file);
    		if(!SDFile.exists()){
    			try {
					SDFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
    		}
    		try{
	    		 FileOutputStream outputStream = new FileOutputStream(file,true);
	             outputStream.write((new Date().toLocaleString()+"["+tag+"]"+": "+msg+"\r\n").getBytes());
	             outputStream.flush();
	             outputStream.close();
    		}catch (IOException e) {
				e.printStackTrace();
			}
    	}
    }

}
