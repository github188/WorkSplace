package com.bestv.ott.appstore.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;

import com.bestv.ott.appstore.thread.PreparingTheData;
import com.bestv.ott.appstore.utils.AppLog;
import com.bestv.ott.appstore.utils.CaCheManager;
import com.bestv.ott.appstore.utils.Utils;
/**
 * 监听系统启动完成后的广播，从服务器上下载部分数据
 * @author Administrator
 *
 */
public class BootCompletedReceiver extends BroadcastReceiver {
    
    private static final String TAG="com.joysee.appstore.receiver.BootCompletedReceiver";
    
    private boolean net_connected = false;
    
    private boolean sdcard_mount = false;
    
    private boolean bootCompleted = false;
    
    private Context mContext;
    
    @Override
    public void onReceive(final Context context, Intent intent) {
        if(intent.getAction()=="android.intent.action.BOOT_COMPLETED"){
        	AppLog.e(TAG, "---------------It is android.intent.action.BOOT_COMPLETED--------------");
            //Intent intentLoad = new Intent(context, LoadAppsService.class);
            //context.startService(intentLoad);
        	//CaCheManager manager = new CaCheManager();
    		//manager.setPowerOff(); //把查询索引开关关掉
        	bootCompleted = true;
        }else if(intent.getAction()=="android.net.conn.CONNECTIVITY_CHANGE"){
        	AppLog.e(TAG, "---------------It is android.net.conn.CONNECTIVITY_CHANGE--------------");
        	mContext=context;
        	if(Utils.checkNet(context)){
        		net_connected = true;
            	AppLog.e(TAG, "---------------------The Network is useing---------------------");
//            	startTask(context);
            	handler.sendEmptyMessageDelayed(0, 1000);
//            	new PreparingTheData(context).start();
        		//网络可用，进行预加载图片。
//            	if(Utils.checkSDcard()){
//            		new PreparingTheData().start();
//            		Intent intentSer = new Intent("com.bestv.ott.appstore.service.DownloadService");
//            		context.startService(intentSer);
//            	}
        	}
        }else if(intent.getAction()=="android.intent.action.MEDIA_MOUNTED"){
        	AppLog.e(TAG, "---------------------The sdsdsdsdsdsdsd is useing---------------------");
        	sdcard_mount = true;
//        	startTask(context);
//        	if(checkNet(context)){
//        		new PreparingTheData().start();
//        		Intent intentSer = new Intent("com.bestv.ott.appstore.service.DownloadService");
//        		context.startService(intentSer);
//        	}
        }
    }
    
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	boolean flag = Utils.checkSDcard();
        	if (!flag){
        		AppLog.e(TAG, "---------------------Wait for loading SDCard.............");
        		handler.sendEmptyMessageDelayed(0, 1000);
        	}else{
        		AppLog.e(TAG, "---------------------SDCard is loading up................");
        		new PreparingTheData(mContext).start();
        	}
        
        }
    };
    
    
    
    private void startTask(Context context){
    	if(net_connected&&sdcard_mount&&bootCompleted){
    		new PreparingTheData(context).start();
    		
    		net_connected = false;
    		sdcard_mount = false;
    		bootCompleted = false;
    	}
    }
    
    
    
}