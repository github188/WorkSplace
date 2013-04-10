package com.ismartv.ui;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class ISTVVodApplication extends Application{
    
    private static String TAG = "ISTVVodApplication";
    
    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "-----ISTVVodApplication------onCreate---");
        mContext=this.getApplicationContext();
    }

}
