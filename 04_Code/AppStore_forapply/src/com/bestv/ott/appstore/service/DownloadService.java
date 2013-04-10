package com.bestv.ott.appstore.service;

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
import android.util.Log;

import com.bestv.ott.BesTVOttServices.BesTVOttMng;
import com.bestv.ott.BesTVOttServices.BesTVOttServices;
import com.bestv.ott.BesTVOttServices.IBesTVOttConnListener;
import com.bestv.ott.BesTVOttServices.ApkManageService.IApkManageService;
import com.bestv.ott.appstore.common.ApplicationBean;
import com.bestv.ott.appstore.common.AppsBean;
import com.bestv.ott.appstore.common.Constants;
import com.bestv.ott.appstore.common.TaskBean;
import com.bestv.ott.appstore.common.TaskDownSpeed;
import com.bestv.ott.appstore.common.ThreadBean;
import com.bestv.ott.appstore.db.DBUtils;
import com.bestv.ott.appstore.db.DatabaseHelper;
import com.bestv.ott.appstore.db.DatabaseHelper.DownloadTaskColumn;
import com.bestv.ott.appstore.utils.AppLog;
import com.bestv.ott.appstore.utils.AppManager;
import com.bestv.ott.appstore.utils.AppStoreConfig;
import com.bestv.ott.appstore.utils.CaCheManager;
import com.bestv.ott.appstore.utils.FileUtils;
import com.bestv.ott.appstore.utils.NetUtil;
import com.bestv.ott.appstore.utils.RequestParam;
import com.bestv.ott.appstore.utils.Utils;
import com.bestv.ott.util.bean.App;
import android.content.pm.IPackageInstallObserver;



public class DownloadService extends Service {

	private static final boolean DEBUG = true;
	private static final String TAG = "com.joysee.appstore.DownloadService";
	// ==========================================================================

	private static final String TMPFILE_SUFFIX = ".NBDOWNLOAD";
	
	private static Map<Integer, List<DownloadThread>> multiThreadMap = new ConcurrentHashMap<Integer, List<DownloadThread>>();
	
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
    
    private static String UNCOMPLETE_APK_LAST_NAME = ".NBDOWNLOAD";
    
    
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
    
    private BesTVOttMng mng = null; 
    
    private IApkManageService apkService =null;
    
    
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
						//向服务器发送下载记录
						Map<String,String> param=new TreeMap<String,String>(); 
						String url=RequestParam.SERVICE_ACTION_URL+RequestParam.Action.PAYMENT;
						param.put(RequestParam.Param.APPID, ""+appBean.getID());
						param.put(RequestParam.Param.PAYFLAG, "1");
						new NetUtil(DownloadService.this).getAppsByStatus(param,url);
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
		new Thread(){
			public void run(){
				Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
				try{
					Thread.sleep(5000);//开机5秒后再断点续传
				}catch(Exception e){
				}
				DBUtils tDBUtils = new DBUtils(DownloadService.this);
        		List<TaskBean> taskList=tDBUtils.queryUnCompletedTask();
        		for(TaskBean task :taskList){
        			if(!multiThreadMap.containsKey(task.getId())){
						executeExitStopTask(task);
        			}
        		}
        		/*List<TaskBean> completedTaskList=tDBUtils.queryCompletedTask();
        		for(TaskBean compTask : completedTaskList){
        			downloadComplete(compTask);
        		}*/
			}
		}.start();
		return super.onStartCommand(intent, flags, startId);
	}

	Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MAINMSG_START_INSTALL:
				String tFileName =(String)msg.obj;
				startInstall(tFileName);
				break;
			case MAINMSG_DOWNLOAD_ERROR:
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
                	Intent tIntent = new Intent();
                	Intent installIntent=new Intent();
                	DBUtils pDBUtils = new DBUtils(DownloadService.this);
                	ApplicationBean tApplicationBean = pDBUtils.queryApplicationByPkgName((String)msg.obj);
                	if(AppStoreConfig.THIRDPARTDEBUG){
            			Log.d(TAG, "install complete code = " + msg.arg1+"===========pakagename="+msg.obj);
            		}
                	if (msg.arg1 != PackageManager.INSTALL_SUCCEEDED) {
                		if(AppStoreConfig.THIRDPARTDEBUG){
                			Log.d(TAG, "Failed to install with error code = " + msg.arg1+"===========pakagename="+msg.obj);
                		}
                		if(AppStoreConfig.THIRDPARTDEBUG){
	            			Log.d(TAG, "==============apkService= " + apkService);
	            		}
                		if(null!=apkService){
							Message tMessage = workHandler.obtainMessage(WORKMSG_APKSERVICE_DELAPP);
							tMessage.obj = msg.obj;
							workHandler.sendMessageAtFrontOfQueue(tMessage);
						}else{
							workHandler.sendMessageAtFrontOfQueue(workHandler.obtainMessage(WORKMSG_CONNECT_APKSERVICE));
							workHandler.sendEmptyMessage(WORKMSG_CONNECT_APKSERVICE);
							Message pMessage = workHandler.obtainMessage(WORKMSG_APKSERVICE_DELAPP);
							pMessage.obj = msg.obj;
							workHandler.sendMessageDelayed(msg, 300);
						}
                		if(tApplicationBean.getAppSource()==Constants.APP_SOURCE_STORE){
                			pDBUtils.deleteOneApplication((String)msg.obj);
                		}else{//内置应用
                			pDBUtils.revertSystemApp((String)msg.obj);
                		}
                        	pDBUtils.deleteOneTaskByPkgName(tApplicationBean.getPkgName());
                        	tIntent.setAction(Constants.INTENT_ACTION_INSTALL_FAIL);
                        	installIntent.setAction(Constants.INTENT_INSTALL_FAIL);
                        	installIntent.putExtra("app_id",tApplicationBean.getSerAppID());
                        	installIntent.putExtra("app_name",tApplicationBean.getAppName());
                        	installIntent.putExtra("pkg_name",tApplicationBean.getPkgName());
                        	sendBroadcast(installIntent);
                    }else{
                    	if(AppStoreConfig.THIRDPARTDEBUG){
                			Log.d(TAG, "install success code = " + msg.arg1+"===========pakagename="+msg.obj);
                		}
                    	if(tApplicationBean.getStatus()!=Constants.APP_STATUS_UPDATE){//应用升级不用再向中间件发消息
                    		Message addmsg = workHandler.obtainMessage(WORKMSG_APKSERVICE_ADDAPP);
                        	addmsg.obj = tApplicationBean;
    						workHandler.sendMessageAtFrontOfQueue(addmsg);
                    	}
                    	tIntent.setAction(Constants.INTENT_ACTION_INSTALL_SUC);
                    }
                	AppLog.d(TAG, "------------------------appName="+tApplicationBean.getAppName());
                	tIntent.putExtra("textParam", tApplicationBean.getAppName());
                	sendBroadcast(tIntent);
                	AppLog.d(TAG, "------------------install completed----------");
                	
                	/* download compete and delete APK */
                	String apkUrl = tApplicationBean.getDownDir() + "/" + tApplicationBean.getFileName();
                	CaCheManager.clearDataCache(apkUrl);
                	
                	break;
                case WORKMSG_DOWNLOAD_ERROR:
                	if(AppStoreConfig.DOWNLOADDEBUG){
            			Log.d(TAG, "download error = " + msg.arg1+"===========threadid="+msg.arg2);
            		}
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
//                	//TODO发广播给中间件，应用下载失败
                	List<DownloadThread> list=multiThreadMap.get(msg.arg1);
                	if(null!=list){
                		for(DownloadThread downThread:list){
                    		downThread.threadPause();
                    	}
                	}
                	DBUtils sDBUtils = new DBUtils(DownloadService.this);
                	TaskBean task = sDBUtils.queryTaskById(msg.arg1);
//                	deleteTask(task);先不自动删除
                	multiThreadMap.remove(msg.arg1);
                	speedMap.remove(msg.arg1);
                	 break;
                	 
                case WORKMSG_CONNECT_APKSERVICE:
                	mng = BesTVOttServices.createOttConManage(DownloadService.this);
            		mng.connect(new IBesTVOttConnListener(){
            		public void onBesTVOttServiceConnected() {
            		//表示中间件内部链接上了。此时，调用B接口提供的功能才可用
            		apkService = BesTVOttServices.createApkManageService();
            		}
            		});
            		break;
                case WORKMSG_APKSERVICE_ADDAPP:
                	ApplicationBean tBean =(ApplicationBean) msg.obj;
                	if(AppStoreConfig.THIRDPARTDEBUG){
		    			Log.d(TAG, "========enter workhandler WORKMSG_APKSERVICE_ADDAPP======apkService= " + apkService);
		    		}
                	if(null!=apkService){
                		if(AppStoreConfig.THIRDPARTDEBUG){
    		    			Log.d(TAG, "==============enter addAppInfo(tApp)========AppName="
    		    					+tBean.getAppName()+"====Packagename="+tBean.getPkgName()+
    		    					"===TypeName="+tBean.getTypeName()+"====Controlmode="+tBean.getVersion());
    		    		}
						App tApp = new App();
						tApp.setAppname(tBean.getAppName());
						tApp.setPackagename(tBean.getPkgName());
						tApp.setApptag(tBean.getTypeName());
						tApp.setControlmode(tBean.getVersion());
						apkService.addAppInfo(tApp);
						if(AppStoreConfig.THIRDPARTDEBUG){
    		    			Log.d(TAG, "==============finish addAppInfo(tApp)=============");
    		    		}
					}else{
					    workHandler.sendEmptyMessage(WORKMSG_CONNECT_APKSERVICE);
					    Message tMessage=workHandler.obtainMessage(WORKMSG_APKSERVICE_ADDAPP);
						tMessage.obj=msg.obj;
						workHandler.sendMessageDelayed(tMessage, 300);
					}
                	break;
                case WORKMSG_APKSERVICE_DELAPP:
                	if(null!=apkService){	
                    	apkService.deleteAppInfo((String)msg.obj);
                    	}else{
                    		workHandler.sendEmptyMessage(WORKMSG_CONNECT_APKSERVICE);
							Message pMessage = workHandler.obtainMessage(WORKMSG_APKSERVICE_DELAPP);
							pMessage.obj = msg.obj;
							workHandler.sendMessageDelayed(pMessage, 300);
							
                    	}
                	break;
                case WORKMSG_DOWNLOAD_COMPLETE :
                	TaskBean pTaskBean = (TaskBean) msg.obj;
                	downloadComplete(pTaskBean);
                	break;
            		
                }
                super.handleMessage(msg);
            }  
        };
        
    }
	
	private void startCheckThread(){
		mCheckThread.start();
		checkHandler = new Handler(mCheckThread.getLooper()){
            @Override
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
				if(AppStoreConfig.DOWNLOADDEBUG){
					Log.d(TAG, "***********enter service onReceive=====task="+tBean.getAppName());
				}
				Message message = workHandler.obtainMessage(WORKMSG_DOWNLOAD_COMPLETE);
				message.obj = tBean;
				workHandler.sendMessageAtFrontOfQueue(message);
			}else if(intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")){
				AppLog.d(TAG, "-----mReciever-----android.net.conn.CONNECTIVITY_CHANGE----------");
				if(Utils.checkNet(context)&&Utils.checkSDcard()){
					new Thread(){
						public void run(){
							try{
							AppLog.d(TAG, "-------------------------");
								sleep(1000*3);
						    }catch(Exception e){
							     AppLog.d(TAG, "---------------------sleeping Exception------------------");
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
				//目前不发，百视通说升级不调中间件service
				Message tMessage=workHandler.obtainMessage(WORKMSG_APKSERVICE_ADDAPP);
				tMessage.obj=tBean;
				workHandler.sendMessageDelayed(tMessage, 300);
				AppLog.d(TAG, "-------apkService.addAppInfo-------");
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
									"===========checkDownload()===apkService= "
											+ apkService + "====AppName()="
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
						startSilenceInstall(tFileName, packageName);
					}
				}else{
					AppLog.d(TAG, "--------------downloadComplete-------------package is error");
			    	tDBUtils.deleteOneTask(tBean);
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
//			            tBean.setVersion(version);
						int appId=tDBUtils.downloadComplete(tBean);
						multiThreadMap.remove(tBean.getId());
						speedMap.remove(tBean.getId());
						if(AppStoreConfig.THIRDPARTDEBUG){
							Log.d(TAG, "===========checkDownload()===apkService= " + apkService+"====AppName()="
									+tBean.getAppName()+"====Packagename="+tBean.getPkgName()+
									"===TypeName="+tBean.getTypeName()+"====Controlmode="+tBean.getVersion());
						}
						//只有在安装成功了才调用中间件
//						Message msg = workHandler.obtainMessage(WORKMSG_APKSERVICE_ADDAPP);
//						msg.obj = tBean;
//						workHandler.sendMessageAtFrontOfQueue(msg);
//						sendBroadcast(new Intent(Contants.INTENT_DOWNLOAD_COMPLETED));
//						workHandler.sendMessage(msg);
						startSilenceInstall(tFileName, packageName);
		            }
//					Message msg = mHandler.obtainMessage(MAINMSG_START_INSTALL);
//					msg.obj = tFileName;
//					mHandler.sendMessage(msg);
//		            startInstall(tFileName);
		            
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
		
		TaskBean tempTask=tDBUtils.queryTaskByPkgName(task.getPkgName());
		if(tempTask.getDownloadSize()>=tempTask.getSumSize()){//正在安装的应用不能删除
			return false;
		}
		
		/* delete the unComplete APK */
		String unCompleteApk = tempTask.getDownloadDir() + "/" + tempTask.getFinalFileName()+  UNCOMPLETE_APK_LAST_NAME; 
		CaCheManager.clearDataCache(unCompleteApk);
		
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
			if (!tDownloadDir.exists()) {
				tDownloadDir.mkdirs();
			}
			AppLog.d(TAG, "===========taskParemeter.getApkUrl()="+taskParemeter.getApkUrl());
			URL url = new URL(taskParemeter.getApkUrl());
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(Constants.TIMEOUT);
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

				byte[] icon ;
				if(null == taskParemeter.getNatImageUrl()||taskParemeter.getNatImageUrl().equals("")){
					AppLog.d(TAG, "--------------icon-----isnot-------exists");
					icon=downLoadIcon(taskParemeter.getSerImageUrl());
				}else{
					File iconFile=new File(taskParemeter.getNatImageUrl());
					if(iconFile.exists()){
						AppLog.d(TAG, "--------------icon-----is-------exists");
						icon=FileUtils.image2Bytes(taskParemeter.getNatImageUrl());
					}else{
						AppLog.d(TAG, "--------------icon-----isnot-------exists");
						icon=downLoadIcon(taskParemeter.getSerImageUrl());
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
			int status=tDBUtils.queryStatusByPkgNameFormApplication(taskParemeter.getPkgName());
			if(status==Constants.APP_STATUS_UPDATE){
				tDBUtils.updateAppStatusByPkgName(taskParemeter.getPkgName(),Constants.APP_STATUS_INSTALLED);
			}
			Log.e(TAG, "can nont connect the url"+taskParemeter.getApkUrl());
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
					ThreadPool.getInstance().submit(tFileDownloadThread);
					tThreadList.add(tFileDownloadThread);
				}
			Intent intent = new Intent(Constants.INTENT_DOWNLOAD_STARTED);
			intent.putExtra("appname", taskBean.getAppName());
			intent.putExtra("taskId", taskId);
			intent.putExtra("app_id",taskBean.getSerAppID());
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
	
	
	private void executeErrorTask(TaskBean taskBean,int threadID) {
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
				if(threadID==tThreadBean.getThreadId()){
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
					ThreadPool.getInstance().submit(tFileDownloadThread);
					tThreadList.add(tFileDownloadThread);
					tDBUtils.updateTaskStatus(taskId,Constants.DOWNLOAD_STATUS_EXECUTE);
					multiThreadMap.put(taskId, tThreadList);
					}
				}
				
			}
		}else{
			AppLog.d(TAG, "=========executeErrorTask===============file is not exists or  thread list < 0");
//			ThreadBean[] threads = new ThreadBean[Contants.THREADNUM];
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
//					Process.setThreadPriority((int)tFileDownloadThread.getId(),Process.THREAD_PRIORITY_LOWEST);
					//No permission to modify given thread
					
					AppLog.d(TAG, "=======executeTask()====name="+taskBean.getAppName()+"=========filesize="+taskBean.getSumSize()+"==========block="+block);
					
//					tFileDownloadThread.start();
					ThreadPool.getInstance().submit(tFileDownloadThread);
					tThreadList.add(tFileDownloadThread);
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
				sendBroadcast(new Intent(Constants.INTENT_DOWNLOAD_STARTED));
			}
	}
	
	
	private void executePauseTask(TaskBean taskBean) {
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
		if(list.size()>0&&tTmpFile.exists()){
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
					ThreadPool.getInstance().submit(tFileDownloadThread);
					tThreadList.add(tFileDownloadThread);
				}
		multiThreadMap.put(taskId, tThreadList);
		tDBUtils.updateTaskStatus(taskBean,Constants.DOWNLOAD_STATUS_EXECUTE);
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
					ThreadPool.getInstance().submit(tFileDownloadThread);
					tThreadList.add(tFileDownloadThread);
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
				sendBroadcast(new Intent(Constants.INTENT_DOWNLOAD_STARTED));
			}
	}
	
	
	
	private void executeExitStopTask(TaskBean taskBean) {
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
					ThreadPool.getInstance().submit(tFileDownloadThread);
					tThreadList.add(tFileDownloadThread);
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
					ThreadPool.getInstance().submit(tFileDownloadThread);
					tThreadList.add(tFileDownloadThread);
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
				sendBroadcast(new Intent(Constants.INTENT_DOWNLOAD_STARTED));
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
	    registerReceiver(mReciever, filter);
		mng = BesTVOttServices.createOttConManage(DownloadService.this);
		mng.connect(new IBesTVOttConnListener(){
		public void onBesTVOttServiceConnected() {
		//表示中间件内部链接上了。此时，调用B接口提供的功能才可用
		apkService = BesTVOttServices.createApkManageService();
		}
		});
		Log.d(TAG, "=========onCreate()=============apkService="+apkService);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		AppLog.d(TAG, "===============onDestroy()============");
		
		DBUtils tDBUtils = new DBUtils(DownloadService.this);
		tDBUtils.updateTaskWithUnCompleted();
//		unregisterTaskObserver();
		unregisterReceiver(mReciever);
		BesTVOttServices.destoryOttConManage(mng); 
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
            Log.d(TAG, "=========startSilenceInstall()=============apkService="+apkService+"==startInstall-fileName:" + fileName);
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
