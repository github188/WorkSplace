package com.bestv.ott.appstore.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.bestv.ott.appstore.utils.AppLog;
/**
 * 当系统启动后，先加载服务器上的部分数据到本地
 * @author Administrator
 *
 */
public class LoadAppsService extends Service {

    private static final String TAG="com.joysee.appstore.service.LoadAppsService";
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        super.onUnbind(intent);
        AppLog.d(TAG, "onUnbind");
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AppLog.d(TAG, "onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppLog.d(TAG, "onDestroy");
    }

    @Override
    public void onStart(Intent intent, int startid) {
        super.onStart(intent, startid);
        AppLog.d(TAG, "onStart");
        //先获取所有分类应用个数
        /*String numJson=NetUtil.getAppsNum("");
        try {
            JSONArray numArr=new JSONArray(numJson);
            for(int i=0;i<numArr.length();i++){
                
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
*/    }

}
