package com.joysee.appstore.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.joysee.appstore.activity.DetailedActivity;
import com.joysee.appstore.common.ApplicationBean;
import com.joysee.appstore.common.AppsBean;
import com.joysee.appstore.common.Constants;
import com.joysee.appstore.common.TaskBean;
import com.joysee.appstore.common.TaskDownSpeed;
import com.joysee.appstore.common.ThreadBean;
import com.joysee.appstore.db.DBUtils;
import com.joysee.appstore.db.DatabaseHelper;
import com.joysee.appstore.db.DatabaseHelper.DownloadTaskColumn;
import com.joysee.appstore.receiver.AppStoreReceiver;
import com.joysee.appstore.utils.AppLog;
import com.joysee.appstore.utils.AppManager;
import com.joysee.appstore.utils.AppStoreConfig;
import com.joysee.appstore.utils.CaCheManager;
import com.joysee.appstore.utils.FileUtils;
import com.joysee.appstore.utils.Utils;
import com.joysee.appstore.R;

import android.content.pm.IPackageInstallObserver;
import android.content.pm.IPackageDeleteObserver;


public class DownloadService extends Service {

	private static final boolean DEBUG = true;
	private static final String TAG = "com.joysee.appstore.DownloadService";
	// ==========================================================================

	private static final String TMPFILE_SUFFIX = ".NBDOWNLOAD";
	
	public static Map<Integer, List<DownloadThread>> multiThreadMap = new ConcurrentHashMap<Integer, List<DownloadThread>>();
	
	public final static Map<Integer,TaskDownSpeed> speedMap=new TreeMap<Integer,TaskDownSpeed>();
	
	//系统内置应用列表
	public static final Map<String,ApplicationBean> systemApp=new HashMap<String,ApplicationBean>();

//	private Map<String, Integer> taskStatusMap = new ConcurrentHashMap<String, Integer>();
	
	private ContentObserver observer = null;
	
	 /**
     * backgroud thread handler
     */
    private Handler workHandler;
    
    private Handler checkHandler; //TODO maybe detele checkHandler and checkthread by yuhongkun
    
    
    
    public static final int CHECKMSG_CHECK_TASK = 99;
    
    public static final int WORKMSG_CREATE_TASK = 100;
    
    public static final int WORKMSG_PAUSE_TASK =WORKMSG_CREATE_TASK+ 1;
    
    public static final int WORKMSG_DELETE_TASK = WORKMSG_CREATE_TASK+2;
    
    public static final int WORKMSG_CONTINUE_TASK = WORKMSG_CREATE_TASK+4 ;
    
    public static final int WORKMSG_INSTALL_COMPLETE = WORKMSG_CREATE_TASK+5;
    
    public static final int WORKMSG_START_SERVICE = WORKMSG_CREATE_TASK+6;
    
    public static final int WORKMSG_DOWNLOAD_ERROR = WORKMSG_CREATE_TASK+7;
    
    public static final int WORKMSG_CONNECT_APKSERVICE = WORKMSG_CREATE_TASK+8;
    
    public static final int WORKMSG_APKSERVICE_ADDAPP = WORKMSG_CREATE_TASK+9;
    
    public static final int WORKMSG_APKSERVICE_DELAPP = WORKMSG_CREATE_TASK+10;
    
    public static final int WORKMSG_DOWNLOAD_COMPLETE = WORKMSG_CREATE_TASK+11;
    
    public static final int MAINMSG_START_INSTALL =200;
    
    public static final int MAINMSG_DOWNLOAD_ERROR =201;
    
    public static final int MAINMSG_TIP =202;
    
    
    private static Map<Integer, List<DownloadThread>> mProgressMap = new ConcurrentHashMap<Integer, List<DownloadThread>>();
    
    private static Map<Integer, Integer> TaskCountMap = new ConcurrentHashMap<Integer,Integer>();
    
    /**
     *backgroud work thread
     */
    private HandlerThread workThread = new HandlerThread("service work thread");
    /**
     *backgroud check download thread
     */
    private HandlerThread mCheckThread= new HandlerThread("service check download thread");
    
    
    
	@Override
	public IBinder onBind(Intent intent) {
		AppLog.d(TAG,"===========enter onBind()======");
		return mServiceBinder;
//		DownloadServiceAIDL downloadServiceAIDL = new DownloadServiceAIDL();
//		if (downloadServiceAIDL != null)
//			downloadServiceAIDL.linkToDeath(downloadServiceAIDL, 0);
//		else
//			Log.d(TAG,"downloadServiceAIDL.onBind(), create binder failed! ");
//		return downloadServiceAIDL;
	}

	private IBinder mServiceBinder = new ServiceBinder();

	public class ServiceBinder extends Binder {
		
		public ServiceBinder getService() {
            return ServiceBinder.this;
        }

		public boolean pauseTask(final TaskBean task) {
//			Message message = workHandler.obtainMessage(WORKMSG_PAUSE_TASK);
//			message.obj = task;
//			workHandler.sendMessage(message);
			new Thread(){
				public void run(){
					pauseTaskBean(task);
				}
			}.start();
			return true;
		}

		public boolean continueTask(final TaskBean task){
//			Message message = workHandler.obtainMessage(WORKMSG_CONTINUE_TASK);
//			message.obj = task;
//			workHandler.sendMessage(message);
			AppLog.d(TAG, "---------Continue download----:"+task.getDownloadDir());
			new Thread(){
				public void run(){
					executePauseTask(task);
				}
			}.start();
			return true;
		}
		public void startDownload(final String downloadUrl,
				final String fileSaveDir, final String appName,final String iconUrl) {
			TaskParemeter taskParemeter= new TaskParemeter(downloadUrl, fileSaveDir, appName,iconUrl);
			Message message = workHandler.obtainMessage(WORKMSG_CREATE_TASK);
			message.obj = taskParemeter;
			workHandler.sendMessageAtFrontOfQueue(message);
		}
		
		public void startDownload(final AppsBean appBean) {
//			TaskParemeter taskParemeter= new TaskParemeter(downloadUrl, fileSaveDir, appName,iconUrl);
//			Message message = workHandler.obtainMessage(WORKMSG_CREATE_TASK);
//			message.obj = appBean;
//			workHandler.sendMessageAtFrontOfQueue(message);
			new Thread(){
				public void run(){
                	TaskBean tTaskBean = prepareTask(appBean);
                	speedMap.put(tTaskBean.getId(), Utils.taskBeanToSpeed(tTaskBean));
					if(tTaskBean!=null){
						executeNewTask(tTaskBean);
					}
				}
			}.start();
		}
		
		public boolean delTask(final TaskBean task) {
//			Message message = workHandler.obtainMessage(WORKMSG_DELETE_TASK);
//			message.obj = task;
//			workHandler.sendMessage(message);
			new Thread(){
				public void run(){
					deleteTask(task);
				}
			}.start();
			return true;
		}
		
		public int getErrorCount(int taskId){
			int errorCount = TaskCountMap.get(taskId);
        	if(errorCount<=0){
        		errorCount = 0;
        	}
        	return errorCount;
		}
		
		public void deletePackage(String pkgname){
			deleteSilence(pkgname);
		}

	}

	
//	class DownloadServiceAIDL extends IDownloadService.Stub implements IBinder.DeathRecipient {
//
//		public DownloadServiceAIDL(){
//			
//		}
//		@Override
//		public void binderDied() {
//			
//		}
//
//		@Override
//		public boolean pauseTask(TaskBean task) throws RemoteException {
//			Message message = workHandler.obtainMessage(WORKMSG_PAUSE_TASK);
//			message.obj = task;
//			workHandler.sendMessage(message);
//			return true;
//		}
//
//		@Override
//		public boolean continueTask(TaskBean task) throws RemoteException {
//			Message message = workHandler.obtainMessage(WORKMSG_CONTINUE_TASK);
//			message.obj = task;
//			workHandler.sendMessage(message);
//			return true;
//		}
//
//		@Override
//		public void startDownload(AppsBean appBean) throws RemoteException {
//			Message message = workHandler.obtainMessage(WORKMSG_CREATE_TASK);
//			message.obj = appBean;
//			workHandler.sendMessageAtFrontOfQueue(message);
//		}
//
//		@Override
//		public boolean delTask(TaskBean task) throws RemoteException {
//			Message message = workHandler.obtainMessage(WORKMSG_DELETE_TASK);
//			message.obj = task;
//			workHandler.sendMessage(message);
//			return true;
//		}
//
//		@Override
//		public int getErrorCount(int taskId) throws RemoteException {
//			int errorCount = TaskCountMap.get(taskId);
//        	if(errorCount<=0){
//        		errorCount = 0;
//        	}
//        	return errorCount;
//		}
//		
//	}
	
	
	@Override
	public void onStart(Intent intent, int startId) {
		//
		super.onStart(intent, startId);
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
//		Message message = workHandler.obtainMessage(WORKMSG_START_SERVICE);
//		workHandler.sendMessage(message);
		AppLog.d(TAG, "-------------onStartCommand------------");
		new Thread(){
			public void run(){
				Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
				try{
					Thread.sleep(1000);//开机5秒后再断点续传
				}catch(Exception e){
				}
				DBUtils tDBUtils = new DBUtils(DownloadService.this);
        		List<TaskBean> taskList=tDBUtils.queryUnCompletedTask();
        		Log.d(TAG, "begin unCompletedTask ");
        		for(TaskBean task :taskList){
        			if(!multiThreadMap.containsKey(task.getId())){
        				AppLog.d(TAG, "---------executeExitStopTask---: "+task.getAppName());
						executeExitStopTask(task);
        			}
        		}
        		Log.d(TAG, "begin completedTask but not install");
        		List<TaskBean> completedTaskList=tDBUtils.queryCompletedTask();
        		for(TaskBean compTask : completedTaskList){
        			Log.d(TAG, "----onStartCommand--pkgname="+compTask.getPkgName());
        			if(!compTask.getPkgName().equals(DownloadService.this.getPackageName())){
        				Log.d(TAG, "---111111111111");
        				downloadComplete(compTask);
        			}else{
        				
        			}
        		}
			}
		}.start();
		workHandler.removeMessages(MAINMSG_DOWNLOAD_ERROR);
    	workHandler.sendEmptyMessageDelayed(MAINMSG_DOWNLOAD_ERROR, Constants.Delayed.SEC_10);
		return super.onStartCommand(intent, flags, startId);
	}

	Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MAINMSG_START_INSTALL:
				String tFileName =(String)msg.obj;
//				startInstall(tFileName);
				break;
			case MAINMSG_TIP:
			    String str=(String)msg.obj;
			    Utils.showInstallComToast(Gravity.TOP|Gravity.RIGHT,DownloadService.this, str);
			    break;
			default:
				break;

			}
		}

	};
	
	private void startWorkThread(){
        
        workThread.start();
        
        workHandler = new Handler(workThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what){
                
                case WORKMSG_START_SERVICE:
                	DBUtils tDBUtils = new DBUtils(DownloadService.this);
            		List<TaskBean> taskList=tDBUtils.queryUnCompletedTask();
            		for(TaskBean task :taskList){
            			if(!multiThreadMap.containsKey(task.getId())){
    						executeExitStopTask(task);
            			}
            		}
                	break;
                case WORKMSG_CREATE_TASK:
                	AppsBean taskParemeter =(AppsBean)msg.obj;
                	TaskBean tTaskBean = prepareTask(taskParemeter);
                	if(AppStoreConfig.DEBUG){
				//	Log.d(TAG,"==================createTask()==========taskId="+tTaskBean.getId());
                	}
					if(tTaskBean!=null){
						executeNewTask(tTaskBean);
					}
                	break;
                case WORKMSG_CONTINUE_TASK:
                	TaskBean taskBean =(TaskBean)msg.obj;
                	executePauseTask(taskBean);
                	break;
                case WORKMSG_PAUSE_TASK:
                	TaskBean pauseTask =(TaskBean)msg.obj;
                	pauseTaskBean(pauseTask);
                	break;
                case WORKMSG_DELETE_TASK:
                	TaskBean delTask =(TaskBean)msg.obj;
                	deleteTask(delTask);
                	break;
                case WORKMSG_INSTALL_COMPLETE:
                	Intent installIntent=new Intent();
                	DBUtils pDBUtils = new DBUtils(DownloadService.this);
                	if(msg.obj==null){
                		Log.d(TAG, "-----------WORKMSG_INSTALL_COMPLETE---pkgname-is-null");
                		return;
                	}
                	ApplicationBean tApplicationBean = pDBUtils.queryApplicationByPkgName((String)msg.obj);
                	if(tApplicationBean==null||tApplicationBean.getPkgName()==null){//表示此应用在下载安装过程中，被删除,此时要被卸载
                		if((String)msg.obj!=null){
                			Log.d(TAG, "---------WORKMSG_INSTALL_COMPLETE-------deleteSilence");
                			deleteSilence((String)msg.obj);
                		}
                		return;
                	}
                	if(AppStoreConfig.THIRDPARTDEBUG){
            			Log.d(TAG, "install complete code = " + msg.arg1+"===========pakagename="+msg.obj);
            		}
                	if (msg.arg1 != PackageManager.INSTALL_SUCCEEDED) {
                		if(AppStoreConfig.THIRDPARTDEBUG){
                			Log.d(TAG, "Failed to install with error code = " + msg.arg1+"===========pakagename="+msg.obj);
                		}
                        	pDBUtils.deleteOneApplication((String)msg.obj);
                        	installIntent.setAction(Constants.INTENT_INSTALL_FAIL);
                        	String str=tApplicationBean.getAppName()+DownloadService.this.getString(R.string.installFail);
                            Message mes=mHandler.obtainMessage(MAINMSG_TIP, str);
                            mHandler.sendMessage(mes);
                		
                    }else{
                    	if(AppStoreConfig.THIRDPARTDEBUG){
                			Log.d(TAG, "install success code = " + msg.arg1+"===========pakagename="+msg.obj);
                		}
                    	installIntent.setAction(Constants.INTENT_INSTALL_COMPLETED);
                    	pDBUtils.updateAppStatusById(tApplicationBean.getId(),Constants.APP_STATUS_INSTALLED);
                    	String str=tApplicationBean.getAppName()+DownloadService.this.getString(R.string.installSucc);
                        Message mes=mHandler.obtainMessage(MAINMSG_TIP, str);
                        mHandler.sendMessage(mes);
                    }
                	pDBUtils.deleteOneTaskByPkgName(tApplicationBean.getPkgName());
                	AppLog.d(TAG, "------------------------appName="+tApplicationBean.getAppName());
                	installIntent.putExtra("app_id",tApplicationBean.getSerAppID());
                	installIntent.putExtra("app_name",tApplicationBean.getAppName());
                	installIntent.putExtra("pkg_name",tApplicationBean.getPkgName());
                	AppLog.d(TAG, "------------------install completed----------");
                	sendBroadcast(installIntent);
                	
                	//安装完成,删除apk
                	CaCheManager.clearDataCache(tApplicationBean.getDownDir()+"/"+tApplicationBean.getFileName());
                	break;
                case WORKMSG_DOWNLOAD_ERROR:
                	multiThreadMap.remove(msg.arg1);
                	workHandler.removeMessages(MAINMSG_DOWNLOAD_ERROR);
                	workHandler.sendEmptyMessageDelayed(MAINMSG_DOWNLOAD_ERROR, Constants.Delayed.SEC_10);
//                	if(AppStoreConfig.DOWNLOADDEBUG){
//            			Log.d(TAG, "download error = " + msg.arg1+"===========threadid="+msg.arg2);
//            		}
//                	Log.d(TAG, "handler WORKMSG_DOWNLOAD_ERROR  TaskCountMap= "+TaskCountMap+"======taskid=" + msg.arg1
//                			+"===========threadid="+msg.arg2+"========TaskCountMap.get(msg.arg1)="+TaskCountMap.get(msg.arg1));
//                	if(TaskCountMap.containsKey(msg.arg1)){
//                		DBUtils sDBUtils = new DBUtils(DownloadService.this);
//                		TaskBean task = sDBUtils.queryTaskById(msg.arg1);
//                		int errorCount = TaskCountMap.get(msg.arg1);
//                    	if(errorCount<=0){
//                    		errorCount = 0;
//                    	}else if(errorCount<Contants.MAXERRORCOUNT){//TODO if error exceed 5 notification user
//                    		TaskCountMap.put(msg.arg1, ++errorCount);
//                        	executeErrorTask(task,msg.arg2);
//                    	}else{
//                    		multiThreadMap.remove(msg.arg1);
//                    		sDBUtils.deleteOneTask(task);
//                    	}
//                	}else{
//                		TaskCountMap.put(msg.arg1, 1);
//                	}
//                	
                	 break;
                	 
                case WORKMSG_CONNECT_APKSERVICE:
            		break;
                case WORKMSG_APKSERVICE_ADDAPP:
                	ApplicationBean tBean =(ApplicationBean) msg.obj;
//                	if(AppStoreConfig.THIRDPARTDEBUG){
//		    			Log.d(TAG, "========enter workhandler WORKMSG_APKSERVICE_ADDAPP======apkService= " + apkService);
//		    		}
//                	if(null!=apkService){
//                		if(AppStoreConfig.THIRDPARTDEBUG){
//    		    			Log.d(TAG, "==============enter addAppInfo(tApp)========AppName="
//    		    					+tBean.getAppName()+"====Packagename="+tBean.getPkgName()+
//    		    					"===TypeName="+tBean.getTypeName()+"====Controlmode="+tBean.getVersion());
//    		    		}
//						App tApp = new App();
//						tApp.setAppname(tBean.getAppName());
//						tApp.setPackagename(tBean.getPkgName());
//						tApp.setApptag(tBean.getTypeName());
//						tApp.setControlmode(tBean.getVersion());
//						apkService.addAppInfo(tApp);
//						if(AppStoreConfig.THIRDPARTDEBUG){
//    		    			Log.d(TAG, "==============finish addAppInfo(tApp)=============");
//    		    		}
//					}else{
//					    workHandler.sendEmptyMessage(WORKMSG_CONNECT_APKSERVICE);
//					    Message tMessage=workHandler.obtainMessage(WORKMSG_APKSERVICE_ADDAPP);
//						tMessage.obj=msg.obj;
//						workHandler.sendMessageDelayed(tMessage, 300);
//					}
                	break;
                case WORKMSG_APKSERVICE_DELAPP:
//                	if(null!=apkService){	
//                    	apkService.deleteAppInfo((String)msg.obj);
//                    	}else{
//                    		workHandler.sendEmptyMessage(WORKMSG_CONNECT_APKSERVICE);
//							Message pMessage = workHandler.obtainMessage(WORKMSG_APKSERVICE_DELAPP);
//							pMessage.obj = msg.obj;
//							workHandler.sendMessageDelayed(pMessage, 300);
//							
//                    	}
                	break;
                case WORKMSG_DOWNLOAD_COMPLETE :
                	final TaskBean pTaskBean = (TaskBean) msg.obj;
                	new Thread(new Runnable(){
                        @Override
                        public void run() {
                            downloadComplete(pTaskBean);
                        }
                	    
                	}).start();
                	break;
                	
                /* Receive error and reDownload */
                case MAINMSG_DOWNLOAD_ERROR:
    				DBUtils reDownUtil = new DBUtils(DownloadService.this);
    				List<TaskBean> reDownList=reDownUtil.queryDownErrorTask();
    				if(reDownList!=null && reDownList.size()>0){
    					dealDownError(reDownList);
    				}
    				break;
                }
                super.handleMessage(msg);
            }
        };
        
    }
	
	public void startComplete(){
		
	}
	
	
	/* if down error, continue to down */
	private void dealDownError(List<TaskBean> reDownList) {
		if(Utils.checkNet(DownloadService.this)){
			for(TaskBean task :reDownList){
				if(!multiThreadMap.containsKey(task.getId())){
					if(Utils.checkConnect(task.getUrl())){
		    			DBUtils sDBUtils = new DBUtils(DownloadService.this);
		    			int errorCount = 0;
		    			if(TaskCountMap.containsKey(task.getId())){
		    				errorCount = TaskCountMap.get(task.getId());
		    			}
		            	AppLog.d(TAG, "-----reDownName="+task.getAppName()+";errorCount="+errorCount);
		                if(errorCount<0){
		                	errorCount = 0;
		                }else if(errorCount<Constants.MAXERRORCOUNT){//TODO if error exceed 5 notification user
		                	TaskCountMap.put(task.getId(), ++errorCount);
		                    executeErrorTask(task);
		                }else{
		                	multiThreadMap.remove(task.getId());
		                	deleteTask(task);
		                }
					}else{
					    int errorCount = 0;
                        if(TaskCountMap.containsKey(task.getId())){
                            errorCount = TaskCountMap.get(task.getId());
                        }
                        if(errorCount<Constants.MAXERRORCOUNT){
                            TaskCountMap.put(task.getId(), ++errorCount);
                        }else{
                            multiThreadMap.remove(task.getId());
                            deleteTask(task);
                        }
						workHandler.sendEmptyMessageDelayed(MAINMSG_DOWNLOAD_ERROR, Constants.Delayed.SEC_10);
					}
				}
			}
		}else{
			workHandler.sendEmptyMessageDelayed(MAINMSG_DOWNLOAD_ERROR, Constants.Delayed.SEC_10);
		}
	}
	
	
	private void startCheckThread(){
		mCheckThread.start();
		checkHandler = new Handler(mCheckThread.getLooper()){
            public void handleMessage(Message msg) {
                switch(msg.what){
                case CHECKMSG_CHECK_TASK:
                	checkDownload();
                	break;
                }
            }
		};
	}
	
	
	private BroadcastReceiver mReciever = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Constants.INTENT_DOWNLOAD_COMPLETED)) {
				TaskBean tBean = intent.getParcelableExtra("task");
				if(tBean==null||tBean.getPkgName()==null||tBean.getPkgName().equals("")){
					return;
				}
				Log.d(TAG, "***********enter service onReceive=====task="+tBean.getAppName());
				if(tBean.getPkgName().equals(context.getPackageName())){
					downloadComplete(tBean);
					return;
				}
				Message message = workHandler.obtainMessage(WORKMSG_DOWNLOAD_COMPLETE);
				message.obj = tBean;
				workHandler.sendMessageAtFrontOfQueue(message);
			}else if(intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")){
				AppLog.d(TAG, "-----mReciever-----android.net.conn.CONNECTIVITY_CHANGE----------");
				if(Utils.checkNet(context)){
					new Thread(){
						public void run(){
							try{
								sleep(1000*3);
						    }catch(Exception e){
							     AppLog.d(TAG, "--------------sleeping Exception------------");
						    }
							Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
							DBUtils tDBUtils = new DBUtils(DownloadService.this);
			        		List<TaskBean> taskList=tDBUtils.queryUnCompletedTask();
			        		for(TaskBean task :taskList){
			        			if(!multiThreadMap.containsKey(task.getId())){
									executeExitStopTask(task);
			        			}
			        		}
						}
					};
				}
			}else if(intent.getAction().equals(Constants.INTENT_UPDATE_COMPLETED)){
				String pkg=intent.getStringExtra("pkg_name");
				AppLog.d(TAG, "-----------Constants.INTENT_UPDATE_COMPLETED-------pkg="+pkg);
				if(pkg==null||pkg.equals("")){
					return;
				}
				DBUtils tDBUtils = new DBUtils(DownloadService.this);
				ApplicationBean tBean = tDBUtils.queryApplicationByPkgName(pkg);
				AppLog.d(TAG, "-----------Constants.INTENT_UPDATE_COMPLETED-------appName="+tBean.getAppName());
				Log.d(TAG, "==============Constants.INTENT_UPDATE_COMPLETED- addAppInfo(tApp)========AppName="
    					+tBean.getAppName()+"====Packagename="+tBean.getPkgName()+
    					"===TypeName="+tBean.getTypeName()+"====Controlmode="+tBean.getVersion());
//				App tApp = new App();
//				tApp.setAppname(tBean.getAppName());
//				tApp.setPackagename(tBean.getPkgName());
//				tApp.setApptag(tBean.getTypeName());
//				tApp.setControlmode(tBean.getVersion());
//				apkService.addAppInfo(tApp);
				AppLog.d(TAG, "-------apkService.addAppInfo-------");
			}else if(intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)){
				AppLog.d(TAG, "-----------ACTION_MEDIA_MOUNTED-------");
        		if(Utils.checkNet(context)){
        			AppLog.d(TAG, "-----------");
					new Thread(){
						public void run(){
							AppLog.d(TAG, "-------run----");
							DBUtils tDBUtils = new DBUtils(DownloadService.this);
			        		List<TaskBean> taskList=tDBUtils.queryUnCompletedTask();
			        		for(TaskBean task :taskList){
			        			AppLog.d(TAG, "multiThreadMap.containsKey(task.getId())="+multiThreadMap.containsKey(task.getId())+";id="+task.getId());
			        			if(!multiThreadMap.containsKey(task.getId())){
			        				AppLog.d(TAG, "----ACTION_MEDIA_MOUNTED-----continueDownload---: "+task.getAppName());
			        				executeErrorTask(task);
			        			}
			        		}
						}
					}.start();
				}
			}
		}

	};
	
	public void downloadComplete(TaskBean tBean) {
		DBUtils tDBUtils = new DBUtils(DownloadService.this);
		if(null==tBean){//TODO this why tBean is null by yuhongkun
			return;
		}
		String tFileName = renameFile(tBean);
		if (null != tFileName) {
			FileUtils.setPermission(tFileName);
			PackageManager pm = getPackageManager();
			PackageInfo info=null;
			try{
				info = pm.getPackageArchiveInfo(tFileName,PackageManager.GET_ACTIVITIES);
			}catch (Exception e) {
				
			}finally{
				if (info != null) {
					ApplicationInfo appInfo = info.applicationInfo;
					if (null != appInfo) {
						String packageName = appInfo.packageName;
						String version = "" + info.versionCode;
						tBean.setPkgName(packageName);
//						tBean.setVersion(tBean.getVersion());
						int appId = tDBUtils.downloadComplete(tBean);
						multiThreadMap.remove(tBean.getId());
						speedMap.remove(tBean.getId());
						if (AppStoreConfig.THIRDPARTDEBUG) {
							Log.d(TAG,
									"===========checkDownload()=== "
											+  "====AppName()="
											+ tBean.getAppName()
											+ "====Packagename="
											+ tBean.getPkgName() + "===TypeName="
											+ tBean.getTypeName()
											+ "====Controlmode="
											+ tBean.getVersion());
						}
						//只有在安装成功了才调用中间件
//						Message msg = workHandler
//								.obtainMessage(WORKMSG_APKSERVICE_ADDAPP);
//						msg.obj = tBean;
//						workHandler.sendMessageAtFrontOfQueue(msg);
//						sendBroadcast(new Intent(Contants.INTENT_DOWNLOAD_COMPLETED));
						// workHandler.sendMessage(msg);
						AppLog.d(TAG, "---------startSilenceInstall-----pkgname : "+packageName);
						startSilenceInstall(tFileName, packageName);
//						startInstall(tFileName);
					}
				}else{
					AppLog.d(TAG, "--------------downloadComplete-------------package is error");
			    	tDBUtils.deleteOneTask(tBean);
			    	tDBUtils.deleteOneApplication(tBean.getPkgName());
			    	multiThreadMap.remove(tBean.getId());
			    	speedMap.remove(tBean.getId());
					Intent tIntent=new Intent();
					Intent installIntent=new Intent();
					tIntent.setAction(Constants.INTENT_ACTION_INSTALL_FAIL);
	            	installIntent.setAction(Constants.INTENT_INSTALL_FAIL);
	            	installIntent.putExtra("app_id",tBean.getSerAppID());
	            	installIntent.putExtra("app_name",tBean.getAppName());
	            	installIntent.putExtra("pkg_name",tBean.getPkgName());
	            	tIntent.putExtra("textParam", tBean.getAppName());
	            	String str=tBean.getAppName()+DownloadService.this.getString(R.string.installFail);
	            	Message mes=mHandler.obtainMessage(MAINMSG_TIP, str);
	            	mHandler.sendMessage(mes);
	            	AppLog.d(TAG, "------------------------appName="+tBean.getAppName());
	            	sendBroadcast(installIntent);
			    	sendBroadcast(tIntent);
				}
			}
		}
	}
				
	public void checkDownload(){
		DBUtils tDBUtils = new DBUtils(DownloadService.this);
		List<TaskBean> taskList=tDBUtils.queryCompletedTask();
		for(int i=0;i<taskList.size();i++){
			TaskBean tBean =taskList.get(i);
			if(tBean.getPkgName().equals(this.getPackageName())){
				continue;
			}
			String tFileName = renameFile(tBean);
//		            Drawable icon = pm.getApplicationIcon(appInfo);//得到图标信息
//		            TextView tv = (TextView)findViewById(R.id.tv); //显示图标
//		            tv.setBackgroundDrawable(icon);
			if(null!=tFileName){
				PackageManager pm = getPackageManager();  
		        PackageInfo info = pm.getPackageArchiveInfo(tFileName, PackageManager.GET_ACTIVITIES);  
		        if(info != null){  
		            ApplicationInfo appInfo = info.applicationInfo;  
		            if(null!=appInfo){
			            String packageName = appInfo.packageName;  
			            String version=""+info.versionCode;
			            tBean.setPkgName(packageName);
						int appId=tDBUtils.downloadComplete(tBean);
						multiThreadMap.remove(tBean.getId());
						speedMap.remove(tBean.getId());
						if(AppStoreConfig.THIRDPARTDEBUG){
							Log.d(TAG, "===========checkDownload()===apkService= " +"====AppName()="
									+tBean.getAppName()+"====Packagename="+tBean.getPkgName()+
									"===TypeName="+tBean.getTypeName()+"====Controlmode="+tBean.getVersion());
						}
						Message msg = workHandler.obtainMessage(WORKMSG_APKSERVICE_ADDAPP);
						msg.obj = tBean;
						workHandler.sendMessageAtFrontOfQueue(msg);
						startSilenceInstall(tFileName, packageName);
						AppLog.d(TAG, "checkDownload****************begin startInstall");
		            }
		        }
			}
		}
	}
	/**
	 * delete one task
	 * @param task
	 * @return
	 */
	public boolean deleteTask(TaskBean task){
		DBUtils tDBUtils = new DBUtils(DownloadService.this);
		int status=tDBUtils.queryStatusByPkgNameFormApplication(task.getPkgName());
		AppLog.d(TAG, "-----------deleteTask--------status="+status);
		AppLog.d(TAG, "-----deleteTask---name="+task.getAppName()+";id="+task.getId());
		List<DownloadThread> taskList = multiThreadMap.get(task.getId());
		if(taskList!=null&&taskList.size()>0){
			for (int i = 0; i < taskList.size(); i++) {
				DownloadThread tFileDownloadThread = taskList.get(i);
				if (tFileDownloadThread != null)
					tFileDownloadThread.threadPause();
			}
			multiThreadMap.remove(task.getId());
			speedMap.remove(task.getId());
		}
		AppLog.d(TAG, "-----------deleteTask--------status="+status);
		if(status==Constants.APP_STATUS_UPDATE){//删除正在升级的应用后，改状态为已安装，并发广播，修改首页，分类页对应的状态
			tDBUtils.updateAppStatusByPkgName(task.getPkgName(),Constants.APP_STATUS_INSTALLED);
			Intent installIntent=new Intent();
            installIntent.setAction(Constants.INTENT_INSTALL_COMPLETED);
            installIntent.putExtra("app_id",task.getSerAppID());
        	installIntent.putExtra("app_name",task.getAppName());
        	installIntent.putExtra("pkg_name",task.getPkgName());
        	sendBroadcast(installIntent);
        	AppLog.d(TAG, "-----------deleteTask-------appName="+task.getAppName());
		}else if(status==Constants.APP_STATUS_DOWNLOADED){
			tDBUtils.deleteOneApplication(task.getPkgName());
		}
		int count =tDBUtils.deleteOneTask(task);
		AppLog.d(TAG, "--------------deleteOneTask.count="+count);
		if(count>0&&status!=Constants.APP_STATUS_UPDATE){
			 Intent i = new Intent();
     		 i.setAction(Constants.INTENT_DOWNLOAD_DETLET);
     		 i.putExtra("app_name", task.getAppName());
     		 i.putExtra("app_id", task.getSerAppID());
     		 i.putExtra("pkg_name", task.getPkgName());
     		 sendBroadcast(i);
     		 AppLog.d(TAG, "--------------Constants.INTENT_DOWNLOAD_DETLET");
			return true;
		}else{
			return false;
		}
	}

	/**
	 * pause one download task
	 * @param task
	 */
	public void pauseTaskBean(TaskBean task) {
	
		DBUtils tDBUtils = new DBUtils(DownloadService.this);
		TaskBean bean = tDBUtils.queryTaskById(task.getId());
		if(bean.getDownloadSize() == bean.getSumSize()){
			return;
		}
		AppLog.d(TAG, "================multiThreadMap.size()="+multiThreadMap.size());
		List<DownloadThread> taskList = multiThreadMap.get(task.getId());
		if(null!=taskList){
		for (int i = 0; i < taskList.size(); i++) {
			DownloadThread tFileDownloadThread = taskList.get(i);
			if (tFileDownloadThread != null)
				tFileDownloadThread.threadPause();
		}
		}
		multiThreadMap.remove(task.getId());
		tDBUtils.updateTaskStatus(task,Constants.DOWNLOAD_STATUS_PAUSE);
	}

	/**
	 * prepare a download task by downloadurl ,savedir,thread number,appname,iconUrl
	 * this task is maybe exists or is new
	 * 
	 */
	private TaskBean prepareTask(AppsBean taskParemeter) {
		
		AppLog.d(TAG, "============prepareTask()==downloadUrl="+taskParemeter.getApkUrl());
		
		TaskBean tTaskBean = new TaskBean();
		HttpURLConnection conn  = null;
		DBUtils tDBUtils = new DBUtils(DownloadService.this);
		try {
			List<TaskBean> taskList =tDBUtils.queryTaskByUrl(taskParemeter.getApkUrl(),taskParemeter.getPkgName());
			if(taskList.size()==1){
				tTaskBean = taskList.get(0);
			}else{
			int tFileSize = 0;
			File tTmpFile = null;
			File tFinalFile = null;
			File tDownloadDir = new File(taskParemeter.getDownloadDir());
			FileUtils.setPermission(tDownloadDir.getAbsolutePath());
			if (!tDownloadDir.exists()) {
				tDownloadDir.mkdirs();
			}
			AppLog.d(TAG, "===========taskParemeter.getApkUrl()="+taskParemeter.getApkUrl());
			URL url = new URL(taskParemeter.getApkUrl());
			conn = (HttpURLConnection) url.openConnection();
			if(taskParemeter.getPkgName().equals(this.getPackageName())){
			    conn.setConnectTimeout(Constants.TIMEOUT);
			}else{
			    conn.setConnectTimeout(1000*20);
			}
			conn.setRequestMethod("GET");
			conn.setRequestProperty(
					"Accept",
					"image/gif, image/jpeg, image/pjpeg, image/pjpeg, "
							+ "application/x-shockwave-flash, application/xaml+xml, "
							+ "application/vnd.ms-xpsdocument, application/x-ms-xbap, "
							+ "application/x-ms-application, application/vnd.ms-excel, "
							+ "application/vnd.ms-powerpoint, application/msword, */*");
			conn.setRequestProperty("Accept-Language", "zh-CN");
			conn.setRequestProperty("Referer", taskParemeter.getApkUrl());
			conn.setRequestProperty("Charset", "UTF-8");
			conn.setRequestProperty(
					"User-Agent",
					"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; "
							+ "Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET "
							+ "CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.connect();
			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				tFileSize = conn.getContentLength();
				
				AppLog.d(TAG, "============prepareTask()==name="+taskParemeter.getAppName()+"===========tFileSize="+tFileSize);
				
				if (tFileSize <= 0){
					Log.w(TAG, "can not find the file size");
					return null;
				}
				String filename = getFileName(taskParemeter.getApkUrl(),conn);
				tFinalFile = new File(tDownloadDir, filename);
				tTmpFile = new File(tDownloadDir, getSaveName(filename));

				byte[] icon=null ;
				if(null == taskParemeter.getNatImageUrl()||taskParemeter.getNatImageUrl().equals("")){
					AppLog.d(TAG, "--------------icon-----isnot-------exists");
					if(!taskParemeter.getPkgName().equals(this.getPackageName())){
						icon=downLoadIcon(taskParemeter.getSerImageUrl());
					}
				}else{
					File iconFile=new File(taskParemeter.getNatImageUrl());
					if(iconFile.exists()){
						AppLog.d(TAG, "--------------icon-----is-------exists");
						icon=FileUtils.image2Bytes(taskParemeter.getNatImageUrl());
					}else{
						AppLog.d(TAG, "--------------icon-----isnot-------exists");
						if(!taskParemeter.getPkgName().equals(this.getPackageName())){
							icon=downLoadIcon(taskParemeter.getSerImageUrl());
						}
					}
				}
				tTaskBean.setUrl(taskParemeter.getApkUrl());
				tTaskBean.setDownloadDir(tDownloadDir.getAbsolutePath());
				tTaskBean.setFinalFileName(tFinalFile.getName());
				tTaskBean.setTmpFileName(tTmpFile.getName());
				tTaskBean.setAppName(taskParemeter.getAppName());
				tTaskBean.setSumSize(tFileSize);
				tTaskBean.setIconUrl(taskParemeter.getSerImageUrl());
				tTaskBean.setIcon(icon);
				tTaskBean.setStatus(Constants.DOWNLOAD_STATUS_EXECUTE);
				tTaskBean.setSerAppID(String.valueOf(taskParemeter.getID()));
				tTaskBean.setTypeID(String.valueOf(taskParemeter.getTypeID()));
				tTaskBean.setTypeName(taskParemeter.getTypeName());
				tTaskBean.setVersion(taskParemeter.getVersion());
				tTaskBean.setPkgName(taskParemeter.getPkgName());
				
				AppLog.d(TAG,"========prepareTask()==========tDBUtils="+tDBUtils+"==========tTaskBean="+tTaskBean);
				
				int taskId = tDBUtils.createTask(tTaskBean);
				
				tTaskBean.setId(taskId);
				// check down
//				checkAndClearData();
			} else {
				Log.e(TAG, "server can nont response");
				int status=tDBUtils.queryStatusByPkgNameFormApplication(taskParemeter.getPkgName());
				if(status==Constants.APP_STATUS_UPDATE){
					tDBUtils.updateAppStatusByPkgName(taskParemeter.getPkgName(),Constants.APP_STATUS_INSTALLED);
				}
			}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "can nont connect the url"+taskParemeter.getApkUrl());
			int status=tDBUtils.queryStatusByPkgNameFormApplication(taskParemeter.getPkgName());
			if(status==Constants.APP_STATUS_UPDATE){
				tDBUtils.updateAppStatusByPkgName(taskParemeter.getPkgName(),Constants.APP_STATUS_INSTALLED);
			}
		}finally{
			if(null!=conn){
				conn.disconnect();
			}
		}
		return tTaskBean;
	}
	
	private void registerTaskObserver(){
		observer = new TaskContentObserver(mHandler);
		getContentResolver().registerContentObserver(DownloadTaskColumn.CONTENT_URI, true, observer);
	}
	
	
	private void unregisterTaskObserver(){
		if(observer!=null){
			getContentResolver().unregisterContentObserver(observer);
		}
	}
	
	private void executeNewTask(TaskBean taskBean) {
		if(!speedMap.containsKey(taskBean.getId())){
			speedMap.put(taskBean.getId(), Utils.taskBeanToSpeed(taskBean));
		}
		if(multiThreadMap.containsKey(taskBean.getId())){
		    Log.d(TAG, "---executeNewTask---taskID="+taskBean.getId()+";--has exits-----");
		    return;
		}
		int taskId = taskBean.getId();
		int fileSize = taskBean.getSumSize();
		try {
		DBUtils tDBUtils = new DBUtils(DownloadService.this);
		URL url = new URL(taskBean.getUrl());
		List<ThreadBean> list = tDBUtils.queryThreadsByTask(taskBean);
		List<DownloadThread> tThreadList = new ArrayList<DownloadThread>();
		File tTmpFile = new File(taskBean.getDownloadDir(),taskBean.getTmpFileName());
		AppLog.d(TAG, "=========executeNewTask===============tTmpFile="+tTmpFile.getAbsolutePath()
				+"FileName()="+taskBean.getTmpFileName()+"==========dirName="+taskBean.getDownloadDir());
		int block = fileSize / Constants.THREADNUM + 1;
		if(fileSize >Constants.TIGGERAPP){
			block = fileSize/ Constants.THREADMULTNUM+1;
		}
//			ThreadBean[] threads = new ThreadBean[Contants.THREADNUM];
			tDBUtils.updateDownloadSizeByTask(taskBean, 0);
			int length = Constants.THREADNUM;
			if(fileSize > Constants.TIGGERAPP){
				length = Constants.THREADMULTNUM;
			}
			for (int i = 0; i < length; i++) {
					int threadId = taskId * Constants.RATE + i;
					
					AppLog.d(TAG, "=============executeNewTask()=======fileSize="+fileSize+"======block="+block);
					
					int startPostion = (block * i);
					int downLength = 0;
					ThreadBean tThreadBean = new ThreadBean();
					tThreadBean.setTaskId(taskId);
					tThreadBean.setThreadId(threadId);
					tThreadBean.setPosition(startPostion);
					tThreadBean.setDownLength(downLength);
					tDBUtils.createOneThread(tThreadBean);
					RandomAccessFile randOut = new RandomAccessFile(tTmpFile,"rws");
					if (fileSize > 0) {
						randOut.setLength(fileSize);
					}
					randOut.seek(startPostion);
					DownloadThread tFileDownloadThread = new DownloadThread(DownloadService.this,workHandler,
							url, randOut, block, startPostion, taskId, threadId);
					tFileDownloadThread.setPriority(Thread.MIN_PRIORITY);
//					Process.setThreadPriority((int)tFileDownloadThread.getId(),Process.THREAD_PRIORITY_LOWEST);
					//No permission to modify given thread
					
					AppLog.d(TAG, "=======executeTask()====name="+taskBean.getAppName()+"=========filesize="+taskBean.getSumSize()+"==========block="+block);
					
//					tFileDownloadThread.start();
					tThreadList.add(tFileDownloadThread);
					if(taskBean.getPkgName().equals(this.getPackageName())){
					    tFileDownloadThread.start();
					}else{
					    ThreadPool.getInstance().submit(tFileDownloadThread);
					}
				}
			Intent intent = new Intent(Constants.INTENT_DOWNLOAD_STARTED);
			intent.putExtra("app_name", taskBean.getAppName());
			intent.putExtra("taskId", taskId);
			intent.putExtra("app_id",taskBean.getSerAppID());
			intent.putExtra("pkg_name",taskBean.getPkgName());
			sendBroadcast(intent);
			multiThreadMap.put(taskId, tThreadList);
			} catch (MalformedURLException e){
				e.printStackTrace();
			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
	}
	
	
	private void executeErrorTask(TaskBean taskBean) {
		if(!speedMap.containsKey(taskBean.getId())){
			speedMap.put(taskBean.getId(), Utils.taskBeanToSpeed(taskBean));
		}
		int taskId = taskBean.getId();
		int fileSize = taskBean.getSumSize();
		try {
		DBUtils tDBUtils = new DBUtils(DownloadService.this);
		URL url = new URL(taskBean.getUrl());
		List<ThreadBean> list = tDBUtils.queryThreadsByTask(taskBean);
		List<DownloadThread> tThreadList = new ArrayList<DownloadThread>();
		File tTmpFile = new File(taskBean.getDownloadDir(),taskBean.getTmpFileName());
		AppLog.d(TAG, "=========executeErrorTask===============tTmpFile="+tTmpFile.getAbsolutePath()
				+"FileName()="+taskBean.getTmpFileName()+"==========dirName="+taskBean.getDownloadDir());
		int block = fileSize / Constants.THREADNUM + 1;
		if(fileSize >Constants.TIGGERAPP){
			block = fileSize/ Constants.THREADMULTNUM+1;
		}
		if(list.size()>0&&tTmpFile.exists()){
			AppLog.d(TAG, "=========executeErrorTask===============file exists and thread list >0");
			if(taskBean.getStatus()==Constants.DOWNLOAD_STATUS_ERROR){
				for (int i = 0; i < list.size(); i++) {
					ThreadBean tThreadBean = list.get(i);
//				if(threadID==tThreadBean.getThreadId()){
					RandomAccessFile randOut;
					randOut = new RandomAccessFile(tTmpFile, "rws");
					if (fileSize > 0) {
						randOut.setLength(fileSize);
					}
					randOut.seek(tThreadBean.getPosition());
					DownloadThread tFileDownloadThread = new DownloadThread(
							DownloadService.this,workHandler, url, randOut, block,
							tThreadBean.getPosition(), taskBean.getId(),
							tThreadBean.getThreadId());
					
					AppLog.d(TAG, "=======executeErrorTask()====name="+taskBean.getAppName()+"========tFileDownloadThread.getId()="+tFileDownloadThread.getId()+"=========filesize="+taskBean.getSumSize()+"==========block="+block);
					
					tFileDownloadThread.setPriority(Thread.MIN_PRIORITY);
//					tFileDownloadThread.start();
					if(taskBean.getPkgName().equals(this.getPackageName())){
                        tFileDownloadThread.start();
                    }else{
                        ThreadPool.getInstance().submit(tFileDownloadThread);
                    }
					tThreadList.add(tFileDownloadThread);
					tDBUtils.updateTaskStatus(taskId,Constants.DOWNLOAD_STATUS_EXECUTE);
					multiThreadMap.put(taskId, tThreadList);
					}
//				}
				sendStartBroadcast(taskBean);
			}
		}else{
			Log.d(TAG, "executeErrorTask taskBean.getDownloadDir()="+taskBean.getDownloadDir()+";tThreadList.size="+tThreadList.size());
			File files=new File(taskBean.getDownloadDir());
			if(files.exists()){
				AppLog.d(TAG, "=========executeErrorTask===============file is not exists or  thread list < 0");
//				ThreadBean[] threads = new ThreadBean[Contants.THREADNUM];
				tDBUtils.updateDownloadSizeByTask(taskBean, 0);
				int length = Constants.THREADNUM;
				if(fileSize > Constants.TIGGERAPP){
					length = Constants.THREADMULTNUM;
				}
				for (int i = 0; i < length; i++) {
						int threadId = taskId * Constants.RATE + i;
						
						AppLog.d(TAG, "=============executeErrorTask()=======fileSize="+fileSize+"======block="+block);
						
						int startPostion = (block * i);
						int downLength = 0;
						ThreadBean tThreadBean = new ThreadBean();
						tThreadBean.setTaskId(taskId);
						tThreadBean.setThreadId(threadId);
						tThreadBean.setPosition(startPostion);
						tThreadBean.setDownLength(downLength);
						tDBUtils.createOneThread(tThreadBean);
						RandomAccessFile randOut = new RandomAccessFile(tTmpFile,"rws");
						if (fileSize > 0) {
							randOut.setLength(fileSize);
						}
						randOut.seek(startPostion);
						DownloadThread tFileDownloadThread = new DownloadThread(DownloadService.this,workHandler,
								url, randOut, block, startPostion, taskId, threadId);
						tFileDownloadThread.setPriority(Thread.MIN_PRIORITY);
//						Process.setThreadPriority((int)tFileDownloadThread.getId(),Process.THREAD_PRIORITY_LOWEST);
						//No permission to modify given thread
						
						AppLog.d(TAG, "=======executeTask()====name="+taskBean.getAppName()+"=========filesize="+taskBean.getSumSize()+"==========block="+block);
						
//						tFileDownloadThread.start();
						tThreadList.add(tFileDownloadThread);
						if(taskBean.getPkgName().equals(this.getPackageName())){
	                        tFileDownloadThread.start();
	                    }else{
	                        ThreadPool.getInstance().submit(tFileDownloadThread);
	                    }
					}
				multiThreadMap.put(taskId, tThreadList);
				tDBUtils.updateTaskStatus(taskId,Constants.DOWNLOAD_STATUS_EXECUTE);
				sendStartBroadcast(taskBean);
			}
			
			}
			} catch (MalformedURLException e){
				e.printStackTrace();
			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}finally{
				
			}
	}
	
	
	public void sendStartBroadcast(TaskBean taskBean){
		Intent intent = new Intent(Constants.INTENT_DOWNLOAD_STARTED);
		intent.putExtra("app_name", taskBean.getAppName());
		intent.putExtra("taskId", taskBean.getId());
		intent.putExtra("app_id",taskBean.getSerAppID());
		intent.putExtra("pkg_name",taskBean.getPkgName());
		sendBroadcast(intent);
	}
	
	private void executePauseTask(TaskBean taskBean) {  //继续下载时,　需判断已下载内容存放在哪
		
		continueDownload(taskBean);
	}

	
	/* 继续下载 */
	private void continueDownload(TaskBean taskBean) {
		if(!speedMap.containsKey(taskBean.getId())){
			speedMap.put(taskBean.getId(), Utils.taskBeanToSpeed(taskBean));
		}
		int taskId = taskBean.getId();
		int fileSize = taskBean.getSumSize();
		try {
		DBUtils tDBUtils = new DBUtils(DownloadService.this);
		URL url = new URL(taskBean.getUrl());
		List<ThreadBean> list = tDBUtils.queryThreadsByTask(taskBean);
		List<DownloadThread> tThreadList = new ArrayList<DownloadThread>();
		File tTmpFile = new File(taskBean.getDownloadDir(),taskBean.getTmpFileName());
		AppLog.d(TAG, "============executePauseTask============tTmpFile="+tTmpFile.getAbsolutePath()
				+"FileName()="+taskBean.getTmpFileName()+"==========dirName="+taskBean.getDownloadDir());
		int block = fileSize / Constants.THREADNUM + 1;
		if(fileSize >Constants.TIGGERAPP){
			block = fileSize/ Constants.THREADMULTNUM+1;
		}
		if(list.size()>0 && tTmpFile.exists()){
			if(taskBean.getStatus()==Constants.DOWNLOAD_STATUS_PAUSE){
				for (int i = 0; i < list.size(); i++) {
					ThreadBean tThreadBean = list.get(i);
					RandomAccessFile randOut = new RandomAccessFile(tTmpFile,"rws");
					if (fileSize > 0) {
						randOut.setLength(fileSize);
					}
					randOut.seek(tThreadBean.getPosition());
					DownloadThread tFileDownloadThread = new DownloadThread(
							DownloadService.this,workHandler, url, randOut, block,
							tThreadBean.getPosition(), taskBean.getId(),
							tThreadBean.getThreadId());
					
					AppLog.d(TAG, "=======executeTask()====name="+taskBean.getAppName()+"========tFileDownloadThread.getId()="+tFileDownloadThread.getId()+"=========filesize="+taskBean.getSumSize()+"==========block="+block);
					
					tFileDownloadThread.setPriority(Thread.MIN_PRIORITY);
//					tFileDownloadThread.start();
					tThreadList.add(tFileDownloadThread);
					if(taskBean.getPkgName().equals(this.getPackageName())){
                        tFileDownloadThread.start();
                    }else{
                        ThreadPool.getInstance().submit(tFileDownloadThread);
                    }
				}
		multiThreadMap.put(taskId, tThreadList);
		tDBUtils.updateTaskStatus(taskBean,Constants.DOWNLOAD_STATUS_EXECUTE);
			}
		}else{
			
			AppLog.d(TAG, "");
//			ThreadBean[] threads = new ThreadBean[Contants.THREADNUM];
			tDBUtils.updateDownloadSizeByTask(taskBean, 0);
			int length = Constants.THREADNUM;
			if(fileSize > Constants.TIGGERAPP){
				length = Constants.THREADMULTNUM;
			}
			for (int i = 0; i < length; i++) {
					int threadId = taskId * Constants.RATE + i;
					
					AppLog.d(TAG, "=============executeNewTask()=======fileSize="+fileSize+"======block="+block);
					
					int startPostion = (block * i);
					int downLength = 0;
					ThreadBean tThreadBean = new ThreadBean();
					tThreadBean.setTaskId(taskId);
					tThreadBean.setThreadId(threadId);
					tThreadBean.setPosition(startPostion);
					tThreadBean.setDownLength(downLength);
					tDBUtils.createOneThread(tThreadBean);
					RandomAccessFile randOut = new RandomAccessFile(tTmpFile,"rws");
					if (fileSize > 0) {
						randOut.setLength(fileSize);
					}
					randOut.seek(startPostion);
					DownloadThread tFileDownloadThread = new DownloadThread(DownloadService.this,workHandler,
							url, randOut, block, startPostion, taskId, threadId);
					tFileDownloadThread.setPriority(Thread.MIN_PRIORITY);
//					Process.setThreadPriority((int)tFileDownloadThread.getId(),Process.THREAD_PRIORITY_LOWEST);
					//No permission to modify given thread
					
					AppLog.d(TAG, "=======executeTask()====name="+taskBean.getAppName()+"=========filesize="+taskBean.getSumSize()+"==========block="+block);
					
//					tFileDownloadThread.start();
					tThreadList.add(tFileDownloadThread);
					if(taskBean.getPkgName().equals(this.getPackageName())){
                        tFileDownloadThread.start();
                    }else{
                        ThreadPool.getInstance().submit(tFileDownloadThread);
                    }
				}
			multiThreadMap.put(taskId, tThreadList);
			tDBUtils.updateTaskStatus(taskBean,Constants.DOWNLOAD_STATUS_EXECUTE);
			}
			} catch (MalformedURLException e){
				e.printStackTrace();
			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}finally{
				Intent intent = new Intent(Constants.INTENT_DOWNLOAD_STARTED);
				intent.putExtra("app_name", taskBean.getAppName());
				intent.putExtra("taskId", taskId);
				intent.putExtra("app_id",taskBean.getSerAppID());
				intent.putExtra("pkg_name",taskBean.getPkgName());
				sendBroadcast(intent);
			}
	}
	
	
	private void executeExitStopTask(TaskBean taskBean) { 
		/* TODO 是否要监听插入SD卡,继续SD内未完成的任务??? */
		
		//开机时,执行未完成的下载任务,如无sd卡,但任务目录又在sd卡,则返回不做处理
		if(!taskBean.getDownloadDir().equals(Constants.APKROOT)){
			if(!CaCheManager.checkSDcard()){
				return;
			}
		}
		AppLog.d(TAG, "-------------taskBean.getStatus : "+taskBean.getStatus());
		if(!speedMap.containsKey(taskBean.getId())){
			speedMap.put(taskBean.getId(), Utils.taskBeanToSpeed(taskBean));
		}
		int taskId = taskBean.getId();
		int fileSize = taskBean.getSumSize();
		try {
		DBUtils tDBUtils = new DBUtils(DownloadService.this);
		URL url = new URL(taskBean.getUrl());
		List<ThreadBean> list = tDBUtils.queryThreadsByTask(taskBean);
		List<DownloadThread> tThreadList = new ArrayList<DownloadThread>();
		File tTmpFile = new File(taskBean.getDownloadDir(),taskBean.getTmpFileName());
		AppLog.d(TAG, "============executeExitStopTask============tTmpFile="+tTmpFile.getAbsolutePath()+"FileName()="+taskBean.getTmpFileName()+"==========dirName="+taskBean.getDownloadDir());
		int block = fileSize / Constants.THREADNUM + 1;
		if(fileSize >Constants.TIGGERAPP){
			block = fileSize/ Constants.THREADMULTNUM+1;
		}
//		if(multiThreadMap.containsKey(taskId)){
//			return;
//		}
		AppLog.d(TAG, "=============executeExitStopTask==list.size="+list.size()+";tTmpFile.exists()="+tTmpFile.exists());
		if(list.size()>0&&tTmpFile.exists()){
			if(taskBean.getStatus()!=Constants.DOWNLOAD_STATUS_PAUSE){
				for (int i = 0; i < list.size(); i++) {
					ThreadBean tThreadBean = list.get(i);
					RandomAccessFile randOut = new RandomAccessFile(tTmpFile,"rws");
					if (fileSize > 0) {
						randOut.setLength(fileSize);
					}
					randOut.seek(tThreadBean.getPosition());
					DownloadThread tFileDownloadThread = new DownloadThread(
							DownloadService.this,workHandler, url, randOut, block,
							tThreadBean.getPosition(), taskBean.getId(),
							tThreadBean.getThreadId());
					
					AppLog.d(TAG, "=======executeTask()====name="+taskBean.getAppName()+"========tFileDownloadThread.getId()="+tFileDownloadThread.getId()+"=========filesize="+taskBean.getSumSize()+"==========block="+block);
					
					tFileDownloadThread.setPriority(Thread.MIN_PRIORITY);
//					tFileDownloadThread.start();
					tThreadList.add(tFileDownloadThread);
					if(taskBean.getPkgName().equals(this.getPackageName())){
                        tFileDownloadThread.start();
                    }else{
                        ThreadPool.getInstance().submit(tFileDownloadThread);
                    }
				}
		multiThreadMap.put(taskId, tThreadList);
		tDBUtils.updateTaskStatus(taskId,Constants.DOWNLOAD_STATUS_EXECUTE);
			}
		}else{
//			ThreadBean[] threads = new ThreadBean[Contants.THREADNUM];
			tDBUtils.updateDownloadSizeByTask(taskBean, 0);
			int length = Constants.THREADNUM;
			if(fileSize > Constants.TIGGERAPP){
				length = Constants.THREADMULTNUM;
			}
			for (int i = 0; i < length; i++) {
					int threadId = taskId * Constants.RATE + i;
					
					AppLog.d(TAG, "=============executeNewTask()=======fileSize="+fileSize+"======block="+block);
					
					int startPostion = (block * i);
					int downLength = 0;
					ThreadBean tThreadBean = new ThreadBean();
					tThreadBean.setTaskId(taskId);
					tThreadBean.setThreadId(threadId);
					tThreadBean.setPosition(startPostion);
					tThreadBean.setDownLength(downLength);
					tDBUtils.createOneThread(tThreadBean);
					RandomAccessFile randOut = new RandomAccessFile(tTmpFile,"rws");
					if (fileSize > 0) {
						randOut.setLength(fileSize);
					}
					randOut.seek(startPostion);
					DownloadThread tFileDownloadThread = new DownloadThread(DownloadService.this,workHandler,
							url, randOut, block, startPostion, taskId, threadId);
					tFileDownloadThread.setPriority(Thread.MIN_PRIORITY);
//					Process.setThreadPriority((int)tFileDownloadThread.getId(),Process.THREAD_PRIORITY_LOWEST);
					//No permission to modify given thread
					
					AppLog.d(TAG, "=======executeTask()====name="+taskBean.getAppName()+"=========filesize="+taskBean.getSumSize()+"==========block="+block);
					
//					tFileDownloadThread.start();
					tThreadList.add(tFileDownloadThread);
					if(taskBean.getPkgName().equals(this.getPackageName())){
                        tFileDownloadThread.start();
                    }else{
                        ThreadPool.getInstance().submit(tFileDownloadThread);
                    }
				}
			multiThreadMap.put(taskId, tThreadList);
			tDBUtils.updateTaskStatus(taskId,Constants.DOWNLOAD_STATUS_EXECUTE);
			}
			} catch (MalformedURLException e){
				e.printStackTrace();
			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}finally{
				Intent intent = new Intent(Constants.INTENT_DOWNLOAD_STARTED);
				intent.putExtra("app_name", taskBean.getAppName());
				intent.putExtra("taskId", taskId);
				intent.putExtra("app_id",taskBean.getSerAppID());
				intent.putExtra("pkg_name",taskBean.getPkgName());
				sendBroadcast(intent);
			}
	}
	


	private String getSaveName(String fileName) {
		return fileName + TMPFILE_SUFFIX;
	}

	private String getFileNameForUrl(String url) {
		String fileName = "";
		try {
			if (url.indexOf("?") != -1) {
				fileName = url.substring((url.lastIndexOf("/") + 1),
						url.indexOf("?"));
			} else {
				fileName = url.substring(url.lastIndexOf("/") + 1);
			}
		} catch (Exception e) {
			fileName = "";
			Log.e(TAG, e.toString());
		}
		return fileName;
	}

	public static synchronized byte[] downLoadIcon(String url) {
		AndroidHttpClient client = AndroidHttpClient.newInstance("http");
		HttpGet getRequest = new HttpGet(url);
		try {
			HttpResponse response = client.execute(getRequest);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				return null;
			}
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream inputStream = null;
				try {
					inputStream = entity.getContent();
					long length = entity.getContentLength();
					byte[] buffer = new byte[(int) length];
					ByteArrayOutputStream byteArrayoutputStream = new ByteArrayOutputStream(
							(int) length);
					int len1 = 0;
					while ((len1 = inputStream.read(buffer)) > 0) {
						byteArrayoutputStream.write(buffer, 0, len1);
					}
					return byteArrayoutputStream.toByteArray();
				} finally {
					if (inputStream != null) {
						inputStream.close();
					}
					entity.consumeContent();
				}
			}
		} catch (Exception e) {
			getRequest.abort();
			e.printStackTrace();
		} finally {
			if (client != null) {
				client.close();
			}
		}
		return null;
	}

	private String getFileName(String downloadUrl,HttpURLConnection conn) {
		URL url;
		String filename="";
		try {
			url = new URL(downloadUrl);
			filename = getFileNameForUrl(url.toString());
			if (filename == null || "".equals(filename.trim())) {
				for (int i = 0;; i++) {
					String mine = conn.getHeaderField(i);
					if (mine == null)
						break;
					if ("content-disposition".equals(conn.getHeaderFieldKey(i)
							.toLowerCase())) {
						Matcher m = Pattern.compile(".*filename=(.*)").matcher(
								mine.toLowerCase());
						if (m.find())
							return m.group(1);
					}
				}
				filename = UUID.randomUUID() + ".tmp";
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return filename;
	}

	public void checkAndClearData() {
		if (DEBUG) {
			Log.e(TAG, "checkAndClearData");
		}
		boolean isClearData = false;
		if (DEBUG) {
			Log.e(TAG, "isClearData:" + isClearData);
		}
//		if (mSaveFile.exists()) {
//			if (downloadSize == 0) {
//				mSaveFile.delete();
//				threadDownPostionMap.clear();
//			}
//		} else {
//			downloadSize = 0;
//			threadDownPostionMap.clear();
//		}

	}

	@Override
	public void onCreate() {
		super.onCreate();
		startWorkThread();
//		startCheckThread();
//		registerTaskObserver();
		IntentFilter filter = new IntentFilter();
	    filter.addAction(Constants.INTENT_DOWNLOAD_COMPLETED);
	    filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
	    filter.addAction(Constants.INTENT_UPDATE_COMPLETED);
	    IntentFilter filter1 = new IntentFilter();
	    filter1.addAction(Intent.ACTION_MEDIA_MOUNTED);
	    filter1.addDataScheme("file");
	    registerReceiver(mReciever, filter);
	    registerReceiver(mReciever, filter1);
	    
		Log.d(TAG, "=========onCreate()==============");
	}

	@Override
	public void onDestroy() {
		
		AppLog.d(TAG, "===============onDestroy()============");
		unregisterReceiver(mReciever);
		Intent intentBroad = new Intent();
		intentBroad.setAction(Constants.INTENT_ACTION_RESTART_SERVICES);
		sendBroadcast(intentBroad);
		AppLog.d(TAG, "---------------send reStart broad---------");
		
		super.onDestroy();
		
		
//		DBUtils tDBUtils = new DBUtils(DownloadService.this);
//		tDBUtils.updateTaskWithUnCompleted();
//		unregisterTaskObserver();
	}
	
	/**
	 * rename tmp file name
	 * @param tBean
	 * @return
	 */
	public String renameFile(TaskBean tBean){
		if(null!=tBean){
			String dir = tBean.getDownloadDir();
			String tmpName =tBean.getTmpFileName();
			String finalName =tBean.getFinalFileName();
			if(null!=dir&&null!=finalName&&null!=tmpName){
				File tFinalFile = new File(dir, finalName);
				File tTmpFile = new File(dir, tmpName);
				boolean ret = tTmpFile.renameTo(tFinalFile);
				if(ret){
					return tFinalFile.getAbsolutePath();
				}else{
					return null;
				}
			}else{
				return null;
			}
		}else{
			return null;
		}
		
	}
	
	/**
	 * install apk
	 * @param fileName
	 */
	public void startInstall(String fileName) {
		
		AppLog.d(TAG, "startInstall-fileName:" + fileName);
        
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        File installFile = new File(fileName);
        Uri uri = Uri.fromFile(installFile);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        startActivity(intent);
        
    }
	
	/**
	 * install apk
	 * @param fileName
	 */
	public void startSilenceInstall(String fileName,String pkgName) {
        if(AppStoreConfig.THIRDPARTDEBUG) {
            Log.d(TAG, "=========startSilenceInstall()=============apkService="+"==startInstall-PkgName:" + pkgName);
            AppLog.d(TAG, "-------fileName : "+fileName);
        }
        Uri uri = Uri.fromFile(new File(fileName));
        
        int installFlags = 0;
        PackageManager pm = getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(pkgName,PackageManager.GET_UNINSTALLED_PACKAGES);
            if(pi != null) {
                installFlags |= PackageManager.INSTALL_REPLACE_EXISTING;
            }
        } catch (NameNotFoundException e) {
        }


//        String installerPackageName = getIntent().getStringExtra(
//                Intent.EXTRA_INSTALLER_PACKAGE_NAME);
       
        PackageInstallObserver observer = new PackageInstallObserver();
        pm.installPackage(uri, observer, installFlags, pkgName);
    }
	
	
    //删除
	public void deleteSilence(String pkgName){
		AppLog.d(TAG, "--------deleteSilence-------");
		PackageManager pm = getPackageManager();
		PackageDeleteObserver deleteObserver = new PackageDeleteObserver();
		//pm.deletePackage(pkgName,deleteObserver,PackageManager.DONT_DELETE_DATA);
		if(pkgName.equals(this.getPackageName())){
		    pm.deletePackage(pkgName,deleteObserver,PackageManager.DONT_DELETE_DATA);
		}else{
		    pm.deletePackage(pkgName,deleteObserver,0);
		}
		DBUtils tDBUtils = new DBUtils(DownloadService.this);
		tDBUtils.updateAppStatusByPkgName(pkgName, Constants.APP_STATUS_UNINSTALLING);
	}
	

	
	class PackageInstallObserver extends IPackageInstallObserver.Stub { 
        public void packageInstalled(String packageName, int returnCode) {
        	if(AppStoreConfig.THIRDPARTDEBUG){
    			Log.d(TAG, "packageInstalled ()==========packageName= " + packageName+"===========returnCode="+returnCode);
    		}
            Message msg = workHandler.obtainMessage(WORKMSG_INSTALL_COMPLETE);
            msg.arg1 = returnCode;
            msg.obj = packageName;
            workHandler.sendMessageAtFrontOfQueue(msg);
        }
    };
    

	class PackageDeleteObserver extends IPackageDeleteObserver.Stub { 
		public void packageDeleted(String packageName, int returnCode) throws RemoteException {
			AppLog.d(TAG, "---------------packageName="+packageName+";returnCode="+returnCode);
			if(returnCode!=1){
				DBUtils tDBUtils = new DBUtils(DownloadService.this);
				tDBUtils.updateAppStatusByPkgName(packageName, Constants.APP_STATUS_INSTALLED);
			}
			
		}
    }
    
	public class TaskContentObserver extends ContentObserver{

		public TaskContentObserver(Handler handler) {
			super(new Handler());
			if(AppStoreConfig.THIRDPARTDEBUG){
			Log.d(TAG,"====TaskContentObserver====TaskContentObserver()=============");
			}
		}

		@Override
		public boolean deliverSelfNotifications() {
			return super.deliverSelfNotifications();
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			if(AppStoreConfig.THIRDPARTDEBUG){
			Log.d(TAG,"====DownloadServices====onChange()============================================================");
			}
			checkHandler.removeMessages(CHECKMSG_CHECK_TASK);
			Message msg = checkHandler.obtainMessage(CHECKMSG_CHECK_TASK);
			checkHandler.sendMessage(msg);
		}
		
	}
	
	class TaskParemeter {
		
		public TaskParemeter(){}
		
		public TaskParemeter(String apkUrl, String downloadDir,
				String apkName,String imgUrl){
			
			this.downloadUrl = apkUrl;
			this.fileSaveDir = downloadDir;
			this.appName = apkName;
			this.iconUrl = imgUrl;
		}
		
		private String downloadUrl;
		 
		private String fileSaveDir;
		 
		private String appName;
		 
		private String iconUrl;

		public String getDownloadUrl() {
			return downloadUrl;
		}

		public void setDownloadUrl(String downloadUrl) {
			this.downloadUrl = downloadUrl;
		}

		public String getFileSaveDir() {
			return fileSaveDir;
		}

		public void setFileSaveDir(String fileSaveDir) {
			this.fileSaveDir = fileSaveDir;
		}

		public String getAppName() {
			return appName;
		}

		public void setAppName(String appName) {
			this.appName = appName;
		}

		public String getIconUrl() {
			return iconUrl;
		}

		public void setIconUrl(String iconUrl) {
			this.iconUrl = iconUrl;
		}
		 
		 
	}

}
