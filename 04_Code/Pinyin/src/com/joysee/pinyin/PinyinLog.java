package com.joysee.pinyin;

import android.util.Log;

public class PinyinLog {
    
    public static int TYPE = 2;
    
    private static final int INFO = 1;
    private static final int DEBUG = 2; 
    private static final int WARN = 3;
    private static final int ERROR = 4;
    
    public static void i(String TAG,String msg){
        if(TYPE <= INFO){
            Log.i(TAG,msg);
        }
    }
    public static void d(String TAG,String msg){
        if(TYPE <= DEBUG){
            Log.d(TAG,msg);
        }
    }
    public static void w(String TAG,String msg){
        if(TYPE <= WARN){
            Log.w(TAG,msg);
        }
    }
    public static void e(String TAG,String msg){
        if(TYPE <= ERROR){
            Log.e(TAG,msg);
        }
    }
}
