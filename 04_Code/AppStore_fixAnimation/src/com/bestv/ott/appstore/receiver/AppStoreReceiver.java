package com.bestv.ott.appstore.receiver;


import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.bestv.ott.appstore.activity.BaseActivity;
import com.bestv.ott.appstore.common.ApplicationBean;
import com.bestv.ott.appstore.common.Constants;
import com.bestv.ott.appstore.common.TaskBean;
import com.bestv.ott.appstore.db.DBUtils;
import com.bestv.ott.appstore.db.DatabaseHelper;
import com.bestv.ott.appstore.db.DatabaseHelper.AppsTempColumn;
import com.bestv.ott.appstore.service.DownloadService;
import com.bestv.ott.appstore.service.DownloadThread;
import com.bestv.ott.appstore.service.DownloadService.ServiceBinder;
import com.bestv.ott.appstore.thread.PreparingTheData;
import com.bestv.ott.appstore.utils.AppLog;
import com.bestv.ott.appstore.utils.AppStoreConfig;
import com.bestv.ott.appstore.utils.Utils;
import com.bestv.ott.appstore.R;
import com.bestv.ott.framework.BesTVServicesMgr;
import com.bestv.ott.framework.IBesTVServicesConnListener;
import com.bestv.ott.framework.services.ApkInfoService;
import com.bestv.ott.framework.services.UserService;
import com.bestv.ott.util.bean.App;
import com.bestv.ott.util.bean.UserProfile;
import com.bestv.ott.appstore.pay.PayMng;
import com.bestv.ott.appstore.pay.PayService;


public class AppStoreReceiver extends BroadcastReceiver {
	
	public static final String TAG = "com.joysee.appstore.AppStoreReceiver";
	public static final int INSTALL_SUCC = 1;
	
	private ServiceBinder downloadService;
	
	private boolean bootCompleted = false;
	
	private Context mContext;
	public PayMng mng = null; 
	
	private boolean net_connected = false;
    
    public ApkInfoService apkService =null;
    
	@Override
	public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        mContext = context;
        if("android.intent.action.PACKAGE_ADDED".equals(action)){
            String packageName = intent.getDataString();
            
            AppLog.d(TAG,"--------------before-" + packageName);
            
            int index = packageName.indexOf(":");
            if(index!=-1){
            	packageName=packageName.substring(index+1);
            }
            
            AppLog.d(TAG,"------------after---" + packageName);
            
            DBUtils tDBUtils = new DBUtils(context);
            ApplicationBean tBean = tDBUtils.queryApplicationByPkgName(packageName);
            if(null!=tBean){
            	
            	AppLog.d(AppStoreReceiver.TAG, "************packageName="+packageName+"*********appId="+tBean.getId());
         		
            	 NotificationManager mNotifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                 Notification notification = new Notification(  
                         R.drawable.drawable_install_succ, context  
                                 .getString(R.string.installSucc),  
                         System.currentTimeMillis());  
                 notification.flags = Notification.FLAG_AUTO_CANCEL; 
                 Intent openItent = new Intent();
                 openItent.setPackage(packageName);
                 openItent.setAction(Intent.ACTION_VIEW);
                 PendingIntent contentIntent = PendingIntent.getActivity(  
                         context, 0, openItent, 0);  
                 notification.setLatestEventInfo(context, context  
                         .getString(R.string.installSucc), null,  
                         contentIntent);
                 mNotifManager.notify(INSTALL_SUCC, notification);  
//                 tDBUtils.que
                 AppLog.d(TAG, "--------------------**********update  install*********------------------------");
                 tDBUtils.deleteOneTaskByPkgName(tBean.getPkgName());
                 int status=tDBUtils.queryStatusByPkgNameFormApplication(tBean.getPkgName());
                 AppLog.d(TAG, "-----------------------name="+tBean.getAppName()+";status="+status+";pkgname="+tBean.getPkgName());
                 AppLog.d(TAG, "apkService="+apkService);
                 if(status==Constants.APP_STATUS_UPDATE){//升级的应用
                	 AppLog.d(TAG, "---------------update---------finish------");
                	 Intent updateIntent=new Intent();
                	 updateIntent.setAction(Constants.INTENT_UPDATE_COMPLETED);//TODO
                	 updateIntent.putExtra("app_id",tBean.getSerAppID());
                	 updateIntent.putExtra("app_name",tBean.getAppName());
                	 updateIntent.putExtra("pkg_name",tBean.getPkgName());
                	 updateIntent.putExtra("version_code",tBean.getVersion());
                 	 mContext.sendBroadcast(updateIntent);
                 }
                 int count = tDBUtils.updateAppStatusById(tBean.getId(),Constants.APP_STATUS_INSTALLED);
                 Intent installIntent=new Intent();
                 installIntent.setAction(Constants.INTENT_INSTALL_COMPLETED);
                 installIntent.putExtra("app_id",tBean.getSerAppID());
             	 installIntent.putExtra("app_name",tBean.getAppName());
             	 installIntent.putExtra("pkg_name", tBean.getPkgName());
             	 installIntent.putExtra("version_code",tBean.getVersion());
             	 mContext.sendBroadcast(installIntent);
            }else{
            	AppLog.d(TAG, "---------ApplicationBean--is null--------------");
            }
           
        }else if(Intent.ACTION_BOOT_COMPLETED.equals(action)){
        	bootCompleted = true;
        }else if(Intent.ACTION_PACKAGE_REMOVED.equals(action)){
        	String packageName = intent.getDataString();
        	AppLog.d(TAG,"--------removed------packageName="+packageName);
        	int index = packageName.indexOf(":");
            if(index!=-1){
                packageName=packageName.substring(index+1);
            }
        	DBUtils tDBUtils = new DBUtils(context);
            ApplicationBean tBean = tDBUtils.queryApplicationByPkgName(packageName);
            if(null!=tBean){
            	AppLog.d(TAG, "-----------------status="+tBean.getStatus());
            	if(tBean.getStatus()==Constants.APP_STATUS_INSTALLED){
            		 //DOTO如果是系统应用不能删除
            		 AppLog.d(TAG, "-------------systemApp.count="+DownloadService.systemApp.size());
            		 if(tBean.getAppSource()==Constants.APP_SOURCE_STORE){
            			 int count = tDBUtils.deleteOneApplication(packageName);
                		 AppLog.d(TAG, "------------count="+count);
                		 Intent i = new Intent();
                  		 i.setAction(Constants.INTENT_UNINSTALL_COMPLETED);
                  		 i.putExtra("pkg_name", packageName);
                  		 i.putExtra("app_name", tBean.getAppName());
                  		 i.putExtra("app_id", tBean.getSerAppID());
                  		 context.sendBroadcast(i);
    	               	 if(count==-1){
    	               		 Log.e(TAG, "delete Application status error");
    	               	 }
            		 }else{//要回复预置应用之前的信息
            			 AppLog.d(TAG, "--------revertSystem--------");
            			 tDBUtils.revertSystemApp(packageName);
            		 }
            	}else if(tBean.getStatus()==Constants.APP_STATUS_UPDATE){//是来自“我的应用”中的卸载,或升级中静默安装的卸载
            		int status=tDBUtils.queryStatusByPkgName(tBean.getPkgName());
            		AppLog.d(TAG, "------------APP_STATUS_UPDATE---------------status="+status);
            		if(status==Constants.DOWNLOAD_STATUS_EXECUTE||status==Constants.DOWNLOAD_STATUS_ERROR){//管“我的应用中的卸载”
            			AppLog.d(TAG, "---------------------");
            			TaskBean taskBean=tDBUtils.queryTaskByPkgName(tBean.getPkgName());
            			if(taskBean==null){
            				return;
            			}
            			AppLog.d(TAG, "------------taskbean.downloadsize="+taskBean.getDownloadSize()+";sumsize="+taskBean.getSumSize());
            			if(taskBean.getDownloadSize()>=taskBean.getSumSize()){//表示升级已下载完，正在安装，指静默安装的卸载
            				AppLog.d(TAG, "");
            			}else{//表示删除正在升级下载的应用，是指从“我的应用”中卸载
            				tDBUtils.updateAppStatusByPkgName(taskBean.getPkgName(),Constants.APP_STATUS_INSTALLED);
                			tDBUtils.deleteOneTask(taskBean);
            			}
////            			downloadService.delTask(taskBean);
//            			DownloadService.speedMap.remove(taskBean.getId());
//            			int count = tDBUtils.deleteOneApplication(packageName);
            		}
            	}
             }
        }else if(ConnectivityManager.CONNECTIVITY_ACTION.equals(action)){
        	AppLog.d(TAG, "---------------It is android.net.conn.CONNECTIVITY_CHANGE--------------");
        	net_connected = true;
            AppLog.d(TAG, "---------------------The Network is useing ="+Utils.checkNet(mContext));
            if(Utils.checkNet(mContext)){
            	if(!startHandler){
            		startHandler=true;
            		handler.removeMessages(1);
            		handler.removeMessages(2);
            		handler.sendEmptyMessageDelayed(2,6000);
            		handler.sendEmptyMessageDelayed(1,8000);
            	}
            }
        }
        
	}
	
	public boolean execute=false;
	public boolean startHandler=false;
	private PreparingTheData pt;

	Handler handler = new Handler() {

		@Override
        public void handleMessage(Message msg) {
        	boolean sdCardFlag = Utils.checkSDcard();
        	boolean netFlag=Utils.checkNet(mContext);
        	AppLog.e(TAG, "----------------sdCardFlag="+sdCardFlag+";netFlag="+netFlag);
        	switch(msg.what){
        	case 1:
        		if (netFlag&&sdCardFlag&&!execute){
        			handler.removeMessages(1);
        			if(!execute){
        				execute=true;
        				AppLog.d(TAG, "---------------------begin---load---------------");
        				SQLiteOpenHelper tDatabaseHelper = DatabaseHelper.getInstance(mContext);//作为数据库升级用
        				SQLiteDatabase db = tDatabaseHelper.getWritableDatabase();
        				db.query(DatabaseHelper.TABLE_APPS_TEMP, null, DatabaseHelper.AppsTempColumn.ID+">0",null, null, null, null);
//        				if(null==pt){
//        					pt = new PreparingTheData(mContext);
//        					AppLog.d(TAG, "-------- PreparingTheData.Thread == null   (new it)------------");
//        					if(!pt.isAlive()){
//        						pt.start();
//        						AppLog.d(TAG, "--------------PreparingTheData.Thread.start-----------------");
//        					}
//        				}
        			}
            	}else{
            		if(!execute){
            			handler.removeMessages(1);
            			handler.sendEmptyMessageDelayed(1, 3000);
            		}
            	}
        		break;
        	case 2:
        	    /* 去掉图片加载和订单信息获取 */
//        		mng=new PayMng(mContext);
//				mng.authUser(new PayMng.CallbackActionListener() {
//					public void execute() {
//						apkService = mng.getApkInfoService();
//					}
//				});
//				Intent orderInfoService = new Intent(mContext,PayService.class);
//				mContext.startService(orderInfoService);
				
				Intent intentSer = new Intent("com.bestv.ott.appstore.service.DownloadService");
				mContext.startService(intentSer);
				
        		break;
        	}
        }
    };	
    
    
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
        	downloadService = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
        	downloadService = ((DownloadService.ServiceBinder)service).getService();
        }
    };
    
	/**
     * 判断网络是否可用
     * @param context
     * @return
     */
    public static boolean checkNet(Context context) {  
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

}
