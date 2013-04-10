
package com.bestv.ott.appstore.db;


import java.util.ArrayList;
import java.util.List;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import com.bestv.ott.appstore.common.ApplicationBean;
import com.bestv.ott.appstore.common.Constants;
import com.bestv.ott.appstore.common.TaskBean;
import com.bestv.ott.appstore.common.ThreadBean;
import com.bestv.ott.appstore.db.DatabaseHelper.ApplicationColumn;
import com.bestv.ott.appstore.db.DatabaseHelper.ApplicationColumnIndex;
import com.bestv.ott.appstore.db.DatabaseHelper.DownloadTaskColumn;
import com.bestv.ott.appstore.db.DatabaseHelper.DownloadTaskColumnIndex;
import com.bestv.ott.appstore.db.DatabaseHelper.DownloadThreadColumn;
import com.bestv.ott.appstore.utils.AppLog;
import com.bestv.ott.appstore.utils.AppManager;
import com.bestv.ott.appstore.utils.AppStoreConfig;

public class DBUtils {
    private String TAG = "com.joysee.appstore.DBUtils";
    
    private Context mContext=null;
    
    private  Object lock = new Object();
    
    public DBUtils(Context context) {
        mContext = context;
    }
    
    /**
     * insert into task  one row because id is primary key ,consider be or not exists
     * @param tBean
     * @return
     */
    
    public  int createTask(TaskBean tBean){
    	int taskId = -1 ;
//    	List<TaskBean> taskList =queryTaskByUrl(tBean.getUrl(),tBean.getAppName());
//    	if(taskList.size()>0){
//    		taskId = 	taskList.get(0).getId();
//    	}else{
    		ContentResolver contentResolver = mContext.getContentResolver();
        	ContentValues task = new ContentValues();
        	task.put(DownloadTaskColumn.APPNAME, tBean.getAppName());
        	task.put(DownloadTaskColumn.DOWNDIR, tBean.getDownloadDir());
        	task.put(DownloadTaskColumn.FINALFILENAME, tBean.getFinalFileName());
        	task.put(DownloadTaskColumn.TMPFILENAME, tBean.getTmpFileName());
        	task.put(DownloadTaskColumn.SUMSIZE, tBean.getSumSize());
        	task.put(DownloadTaskColumn.DOWNLOADSIZE, tBean.getDownloadSize());
        	task.put(DownloadTaskColumn.URL, tBean.getUrl());
        	task.put(DownloadTaskColumn.STATUS, tBean.getStatus());
        	task.put(DownloadTaskColumn.ICONURL, tBean.getIconUrl());
        	task.put(DownloadTaskColumn.ICON, tBean.getIcon());
        	task.put(DownloadTaskColumn.SERAPPID, tBean.getSerAppID());
        	task.put(DownloadTaskColumn.APPTYPEID, tBean.getTypeID());
        	task.put(DownloadTaskColumn.TYPENAME, tBean.getTypeName());
        	task.put(DownloadTaskColumn.VERSION, tBean.getVersion());
        	task.put(DownloadTaskColumn.PKGNAME, tBean.getPkgName());
        	Uri  uri=contentResolver.insert(DownloadTaskColumn.CONTENT_URI, task);
        	
        	AppLog.d(TAG, "======createTask()===uri="+uri.toString());
        	
        	taskId =(int)ContentUris.parseId(uri);
//    	}
    	return taskId;
    }
    
    public int createOneThread(ThreadBean tBean){
    	int key = -1 ;
    	SQLiteOpenHelper tDatabaseHelper = DatabaseHelper.getInstance(mContext);
    	SQLiteDatabase db = tDatabaseHelper.getWritableDatabase();
//    	waitDBLock(db);
    	ContentResolver contentResolver = mContext.getContentResolver();
    	ContentValues task = new ContentValues();
    	task.put(DownloadThreadColumn.TASK_ID, tBean.getTaskId());
    	task.put(DownloadThreadColumn.THREADID, tBean.getThreadId());
    	task.put(DownloadThreadColumn.POSITION, tBean.getPosition());
    	task.put(DownloadThreadColumn.DOWNLENGTH, tBean.getDownLength());
    	Uri  uri=contentResolver.insert(DownloadThreadColumn.CONTENT_URI, task);
    	key =(int)ContentUris.parseId(uri);
    	return key;
    }
    
    /**
     * downlad complete create one application in appstore_apps
     * @param tBean
     * @return application id
     */
    public int downloadComplete(TaskBean tBean){
    	SQLiteOpenHelper tDatabaseHelper = DatabaseHelper.getInstance(mContext);
    	SQLiteDatabase db = tDatabaseHelper.getWritableDatabase();
//    	waitDBLock(db);
    	int appId= -1;
    	db.beginTransaction();//开始事务  
            try{  
            	ContentValues application = new ContentValues();
//    			ApplicationBean appBean = queryApplicationByName(tBean.getUrl(), tBean.getAppName());
            	ApplicationBean appBean = queryApplicationByPkgName(tBean.getPkgName());//只通过appName查找
    			AppLog.d(TAG, "-------downloadComplete-appName="+tBean.getAppName()+";version="+tBean.getVersion()+";pkgName="+tBean.getPkgName());
    			if(null!=appBean){
    				AppLog.d(TAG, "-------downloadComplete----Update");
    				appId =appBean.getId();
    				application.put(ApplicationColumn.DOWNDIR, tBean.getDownloadDir());
                	application.put(ApplicationColumn.FILENAME, tBean.getFinalFileName());
                	application.put(ApplicationColumn.APPNAME, tBean.getAppName());
                	application.put(ApplicationColumn.URL, tBean.getUrl());
                	if(tBean.getPkgName()!=null&&!tBean.getPkgName().equals("")){
                		application.put(ApplicationColumn.PKGNAME, tBean.getPkgName());
                	}
                	application.put(ApplicationColumn.VERSION, tBean.getVersion());
                	application.put(ApplicationColumn.ICON, tBean.getIcon());
                	application.put(ApplicationColumn.SERAPPID, tBean.getSerAppID());
                	application.put(ApplicationColumn.APPTYPEID, tBean.getTypeID());
                	application.put(ApplicationColumn.TYPENAME, tBean.getTypeName());
                	if(appBean.getAppSource()==Constants.APP_SOURCE_INNER){
                		application.put(ApplicationColumn.APPSOURCE, Constants.APP_SOURCE_INNER_UPDATE);
                	}
                	db.update(DatabaseHelper.TABLE_APPLICATIONS, application,ApplicationColumn.ID+"= ? ", new String[]{""+appId});
    			}else{
    				AppLog.d(TAG, "-------downloadComplete----Insert");
    				application.put(ApplicationColumn.DOWNDIR, tBean.getDownloadDir());
                	application.put(ApplicationColumn.FILENAME, tBean.getFinalFileName());
                	application.put(ApplicationColumn.APPNAME, tBean.getAppName());
                	application.put(ApplicationColumn.URL, tBean.getUrl());
                	application.put(ApplicationColumn.STATUS, Constants.APP_STATUS_DOWNLOADED);
                	application.put(ApplicationColumn.PKGNAME, tBean.getPkgName());
                	application.put(ApplicationColumn.VERSION, tBean.getVersion());
                	application.put(ApplicationColumn.ICON, tBean.getIcon());
                	application.put(ApplicationColumn.SERAPPID, tBean.getSerAppID());
                	application.put(ApplicationColumn.APPTYPEID, tBean.getTypeID());
                	application.put(ApplicationColumn.TYPENAME, tBean.getTypeName());
                	application.put(ApplicationColumn.APPSOURCE, Constants.APP_SOURCE_STORE);
//                	db.delete(DatabaseHelper.TABLE_APPLICATIONS,ApplicationColumn.PKGNAME+"= ? " , new String[]{tBean.getPkgName()});
    				appId=(int)db.insert(DatabaseHelper.TABLE_APPLICATIONS, null, application);
    				AppLog.d(TAG, "----------sucess--------");
    			}
            	//int taskId=db.delete(DatabaseHelper.TABLE_TASK, DownloadTaskColumn.ID+"=? ", new String[]{""+tBean.getId()});改为当安装成功后删除
            	
                db.setTransactionSuccessful();//设置事务标记为successful
//                queryCompletedTask();
            }catch(Exception e){
            	e.printStackTrace();
            	return appId;
            }finally{
            	db.endTransaction();//结束事务
//            	if(db!=null){
//            		db.close();
//            	}
            mContext.getContentResolver().notifyChange(DownloadTaskColumn.CONTENT_URI, null);
        	mContext.getContentResolver().notifyChange(ApplicationColumn.CONTENT_URI, null);
        }
        return appId;
    }
    
    /**
     * wait database lock
     * @param db
     */
    public void waitDBLock(SQLiteDatabase db){
    	while(db.isDbLockedByOtherThreads()){
    		try {
				Thread.sleep(6);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    }
    /**
     * not complete,delete it
     * @param tBean
     * @return
     */
    public int deleteOneTask(TaskBean tBean){
    	int ret = -1;
    	ContentResolver contentResolver = mContext.getContentResolver();
    	ret =contentResolver.delete(DownloadTaskColumn.CONTENT_URI, DownloadTaskColumn.ID+"=? ", new String[]{""+tBean.getId()});
    	if(ret!=-1){
    		mContext.getContentResolver().notifyChange(DownloadTaskColumn.CONTENT_URI, null);
    	}
    	return ret;
    }
    
    public int deleteOneTaskByPkgName(String pkgname){
    	int ret = -1;
    	ContentResolver contentResolver = mContext.getContentResolver();
    	ret =contentResolver.delete(DownloadTaskColumn.CONTENT_URI, DownloadTaskColumn.PKGNAME+"=? ", new String[]{""+pkgname});
    	AppLog.d(TAG, "-----------------deleteOneTaskByPkgName---pkgname="+pkgname+";ret="+ret);
    	if(ret!=-1){
    		mContext.getContentResolver().notifyChange(DownloadTaskColumn.CONTENT_URI, null);
    	}
    	return ret;
    }
    
    /**
     * delete one application by appName
     * @param appName
     * @return
     */
    public int deleteOneApplication(String pkgName){
    	int ret = -1;
    	ContentResolver contentResolver = mContext.getContentResolver();
    	ret =contentResolver.delete(ApplicationColumn.CONTENT_URI, ApplicationColumn.PKGNAME+"=? ", new String[]{pkgName});
    	if(ret!=-1){
    	mContext.getContentResolver().notifyChange(ApplicationColumn.CONTENT_URI, null);
    	}
    	return ret;
    }
    
    /**
     * update download size by threadbean and offset
     * @param tBean
     * @param offSet
     * @return
     */
    public boolean updateProgress(ThreadBean tBean,int offSet){
    	SQLiteOpenHelper mDatabaseHelper = DatabaseHelper.getInstance(mContext);
    	SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
//    	waitDBLock(db);
    	db.beginTransaction();//开始事务  
        try{  
        	ContentValues task = new ContentValues();
        	task.put(DownloadThreadColumn.POSITION, tBean.getPosition());
        	task.put(DownloadThreadColumn.DOWNLENGTH, tBean.getDownLength());
        	String whereClause = DownloadThreadColumn.TASK_ID+" = ? and "+DownloadThreadColumn.THREADID +" =?";
        	db.update(DatabaseHelper.TABLE_THREAD, task, whereClause, new String[]{""+tBean.getTaskId(),""+tBean.getThreadId()});
        	
        	String sql = "UPDATE "+DatabaseHelper.TABLE_TASK +" SET "+DownloadTaskColumn.DOWNLOADSIZE + "= ("+DownloadTaskColumn.DOWNLOADSIZE+"+"
        	+offSet+") where "+DownloadTaskColumn.ID +"="+tBean.getTaskId()+";";
        	synchronized(lock){
        		db.execSQL(sql);
        	}
            db.setTransactionSuccessful();//设置事务标记为successful
        }catch(Exception e){
        	e.printStackTrace();
        	return false;
        }finally{
        	db.endTransaction();//结束事务
//        	if(db!=null){
//        		db.close();
//        	}
//        	List<TaskBean> data  =queryCompletedTask();//TODO here may be need optimize by yuhongkun
        	TaskBean taskBean = queryTaskById(tBean.getTaskId());
        	if(AppStoreConfig.DOWNLOADDEBUG){
				Log.d(TAG, "***********updateprogress()=====task="+taskBean.getAppName()
						+"=======downloadSize="+taskBean.getDownloadSize()+"******sumsize="+taskBean.getSumSize());
			}
        	Intent progress_intent = new Intent(Constants.INTENT_DOWNLOAD_PROGRESS);
        	progress_intent.putExtra("app_name",taskBean.getAppName());
        	progress_intent.putExtra("pkg_name",taskBean.getPkgName());
        	progress_intent.putExtra("server_app_id",taskBean.getSerAppID());
        	progress_intent.putExtra("file_sum",taskBean.getSumSize());
        	progress_intent.putExtra("download_size",taskBean.getDownloadSize());
        	progress_intent.putExtra("app_version",taskBean.getVersion());
        	mContext.sendBroadcast(progress_intent);
//            if(data.contains(tBean)){
        		if(taskBean.getDownloadSize()>=taskBean.getSumSize()){
//            	int index =data.indexOf(tBean);
        		if(AppStoreConfig.DOWNLOADDEBUG){
        				Log.d(TAG, "***********download complete=====task="+taskBean.getAppName()
        						+"=======downloadSize="+taskBean.getDownloadSize()+"******sumsize="+taskBean.getSumSize());
        			}	
            	Intent intent = new Intent();
            	intent.setAction(Constants.INTENT_DOWNLOAD_COMPLETED);
//            	intent.putExtra("task",data.get(index));
            	intent.putExtra("task",taskBean);
            	mContext.sendBroadcast(intent);
            	if(AppStoreConfig.DOWNLOADDEBUG){
    				Log.d(TAG, "***********download complete=====broadcast finished=======");
    			}
        		}
//            }
            Uri uri =Uri.withAppendedPath(DownloadTaskColumn.CONTENT_URI, ""+tBean.getTaskId());
        	mContext.getContentResolver().notifyChange(uri, null);//TODO this need verify by yuhongkun
        	
//        	int random = (int)(Math.random() * 6);
//        	if(random==5){
//        			
//        	}
        }
        return true;
    }
    /**
     * update task downloadsize 
     */
    public int updateDownloadSizeByTask(TaskBean tBean,int downloadSize){
    	int count = 0;
    	ContentResolver contentResolver = mContext.getContentResolver();
    	ContentValues task = new ContentValues();
    	task.put(DownloadTaskColumn.DOWNLOADSIZE, downloadSize);
    	count=contentResolver.update(DownloadTaskColumn.CONTENT_URI, task, DownloadTaskColumn.ID+"= ?", new String[]{""+tBean.getId()});
    	return count;
    }
    
    /**
     * update task download task status 
     */
    public int updateTaskStatus(TaskBean tBean,int status){
    	int count = 0;
    	ContentResolver contentResolver = mContext.getContentResolver();
    	ContentValues task = new ContentValues();
    	task.put(DownloadTaskColumn.STATUS, status);
    	count=contentResolver.update(DownloadTaskColumn.CONTENT_URI, task, DownloadTaskColumn.ID+"= ?", new String[]{""+tBean.getId()});
    	if(count>0){
    		mContext.getContentResolver().notifyChange(DownloadTaskColumn.CONTENT_URI, null);
    	}
    	AppLog.d(TAG, "-----updateTaskStatus--status="+status);
    	if(status==Constants.DOWNLOAD_STATUS_PAUSE){
    		Intent pauseInt=new Intent(Constants.INTENT_DOWNLOAD_PAUSE);
    		pauseInt.putExtra("app_id", tBean.getSerAppID());
    		pauseInt.putExtra("app_name", tBean.getAppName());
    		pauseInt.putExtra("pkg_name", tBean.getPkgName());
    		mContext.sendBroadcast(pauseInt);
    		AppLog.d(TAG, "------Constants.INTENT_DOWNLOAD_PAUSE-----pkg_name="+tBean.getPkgName()+";app_name="+tBean.getAppName());
    	}else if(status==Constants.DOWNLOAD_STATUS_EXECUTE){
    		Intent executeInt=new Intent(Constants.INTENT_DOWNLOAD_STARTED);
    		executeInt.putExtra("app_id", tBean.getSerAppID());
    		executeInt.putExtra("app_name", tBean.getAppName());
    		executeInt.putExtra("pkg_name", tBean.getPkgName());
			mContext.sendBroadcast(executeInt);
			AppLog.d(TAG, "------Constants.INTENT_DOWNLOAD_STARTED-----pkg_name="+tBean.getPkgName()+";app_name="+tBean.getAppName());
    	}
    	return count;
    }
    
    /**
     * update task download task status 
     */
    public int updateTaskStatus(int taskId,int status){
    	int count = 0;
    	ContentResolver contentResolver = mContext.getContentResolver();
    	ContentValues task = new ContentValues();
    	task.put(DownloadTaskColumn.STATUS, status);
    	count=contentResolver.update(DownloadTaskColumn.CONTENT_URI, task, DownloadTaskColumn.ID+"= ?", new String[]{""+taskId});
    	if(count>0){
    		mContext.getContentResolver().notifyChange(DownloadTaskColumn.CONTENT_URI, null);
    	}
    	return count;
    }
    /**
     * update application status by pkgName
     * @param pkgName
     * @param status
     * @return
     */
    
    public int updateAppStatusByName(String pkgName,int status){
    	int count = 0;
    	ContentResolver contentResolver = mContext.getContentResolver();
    	ContentValues application = new ContentValues();
    	application.put(ApplicationColumn.STATUS, status);
    	count=contentResolver.update(ApplicationColumn.CONTENT_URI, application, ApplicationColumn.PKGNAME+"= ?", new String[]{pkgName});
    	if(count>0){
    		mContext.getContentResolver().notifyChange(ApplicationColumn.CONTENT_URI, null);
    	}
    	return count;
    }
    
    public int updateAppStatusByPkgName(String pkgname,int status){
    	int count = 0;
    	ContentResolver contentResolver = mContext.getContentResolver();
    	ContentValues application = new ContentValues();
    	application.put(ApplicationColumn.STATUS, status);
    	count=contentResolver.update(ApplicationColumn.CONTENT_URI, application, ApplicationColumn.PKGNAME+"= ?", new String[]{pkgname});
    	if(count>0){
    		mContext.getContentResolver().notifyChange(ApplicationColumn.CONTENT_URI, null);
    	}
    	return count;
    }
    
    /**
     * update application status by id
     * @param pkgName
     * @param status
     * @return update row count
     */
    
    public int updateAppStatusById(int id,int status){
    	int count = 0;
    	ContentResolver contentResolver = mContext.getContentResolver();
    	ContentValues application = new ContentValues();
    	application.put(ApplicationColumn.STATUS, status);
    	count=contentResolver.update(ApplicationColumn.CONTENT_URI, application, ApplicationColumn.ID+"= ?", new String[]{""+id});
//    	SQLiteOpenHelper mDatabaseHelper = DatabaseHelper.getInstance(mContext);
//    	SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
//    	String sql = "update"
//    	db.execSQL(sql)
    	if(count>0){
    		mContext.getContentResolver().notifyChange(ApplicationColumn.CONTENT_URI, null);
    	}
    	return count;
    }
    
    /**
     * query application list from table appstore_apps by status
     * @param status
     * @return application list
     */
    
    public List<ApplicationBean> queryApplicationByStatus(int status){
    	List<ApplicationBean> appList = new ArrayList<ApplicationBean>();
    	Cursor appCursor=null;
    	ContentResolver contentResolver = mContext.getContentResolver();
    	try{
    		appCursor=contentResolver.query(ApplicationColumn.CONTENT_URI, null,
    				ApplicationColumn.STATUS + "=?",
    				new String[]{""+status},null);
    		if(appCursor.getCount()>0)
    		{
    			while(appCursor.moveToNext()){
    				ApplicationBean appBean = new ApplicationBean();
               		int appId = appCursor.getInt(ApplicationColumnIndex.ID);
               		String downDir = appCursor.getString(ApplicationColumnIndex.DOWNDIR);
               		String fileName = appCursor.getString(ApplicationColumnIndex.FILENAME);
               		String name = appCursor.getString(ApplicationColumnIndex.APPNAME);
               		String appUrl =appCursor.getString(ApplicationColumnIndex.URL);
               		int appStatus = appCursor.getInt(ApplicationColumnIndex.STATUS);
               		String pkgName = appCursor.getString(ApplicationColumnIndex.PKGNAME);
               		byte[] icon = appCursor.getBlob(ApplicationColumnIndex.ICON);
               		String version = appCursor.getString(ApplicationColumnIndex.VERSION);
               		String serAppID = appCursor.getString(ApplicationColumnIndex.SERAPPID);
               		String typeID = appCursor.getString(ApplicationColumnIndex.APPTYPEID);
               		String typeName = appCursor.getString(ApplicationColumnIndex.TYPENAME);
               		int appSource = appCursor.getInt(ApplicationColumnIndex.APPSOURCE);
               		
               		appBean.setId(appId);
               		appBean.setDownDir(downDir);
               		appBean.setFileName(fileName);
               		appBean.setAppName(name);
               		appBean.setUrl(appUrl);
               		appBean.setStatus(appStatus);
               		appBean.setPkgName(pkgName);
               		appBean.setIcon(icon);
               		appBean.setVersion(version);
               		appBean.setSerAppID(serAppID);
               		appBean.setTypeID(typeID);
               		appBean.setTypeName(typeName);
               		appBean.setAppSource(appSource);
               		appList.add(appBean);
               }
    		}
    	}finally{
       		if(appCursor!=null){
       			appCursor.close();
       		}
       	}
    	return appList;
    }
    /**
     * query application from table appstore_apps by packageName
     * @param packageName
     * @return
     */
    
    public ApplicationBean queryApplicationByPkgName(String packageName){
    	ApplicationBean appBean = null;
    	Cursor appCursor=null;
    	ContentResolver contentResolver = mContext.getContentResolver();
    	try{
    		appCursor=contentResolver.query(ApplicationColumn.CONTENT_URI, null,
    				ApplicationColumn.PKGNAME + "=?",
    				new String[]{packageName},null);
    		if(appCursor.getCount()==1)
    		{
    			if(appCursor.moveToFirst()){
    				appBean = new ApplicationBean();
               		int appId = appCursor.getInt(ApplicationColumnIndex.ID);
               		String downDir = appCursor.getString(ApplicationColumnIndex.DOWNDIR);
               		String fileName = appCursor.getString(ApplicationColumnIndex.FILENAME);
               		String name = appCursor.getString(ApplicationColumnIndex.APPNAME);
               		String appUrl =appCursor.getString(ApplicationColumnIndex.URL);
               		int status = appCursor.getInt(ApplicationColumnIndex.STATUS);
               		String pkgName = appCursor.getString(ApplicationColumnIndex.PKGNAME);
               		byte[] icon = appCursor.getBlob(ApplicationColumnIndex.ICON);
               		String version = appCursor.getString(ApplicationColumnIndex.VERSION);
               		String serAppID = appCursor.getString(ApplicationColumnIndex.SERAPPID);
               		String typeID = appCursor.getString(ApplicationColumnIndex.APPTYPEID);
               		String typeName = appCursor.getString(ApplicationColumnIndex.TYPENAME);
               		int appSource = appCursor.getInt(ApplicationColumnIndex.APPSOURCE);
               		
               		appBean.setId(appId);
               		appBean.setDownDir(downDir);
               		appBean.setFileName(fileName);
               		appBean.setAppName(name);
               		appBean.setUrl(appUrl);
               		appBean.setStatus(status);
               		appBean.setPkgName(pkgName);
               		appBean.setIcon(icon);
               		appBean.setVersion(version);
               		appBean.setSerAppID(serAppID);
               		appBean.setTypeID(typeID);
               		appBean.setTypeName(typeName);
               		appBean.setAppSource(appSource);
               }
    		}
    	}finally{
       		if(appCursor!=null){
       			appCursor.close();
       		}
       	}
    	return appBean;
    }
    
//    /**
//     * query application from table appstore_apps by packageName
//     * @param appName
//     * @return
//     */
//    
//    public ApplicationBean queryApplicationByAppName(String appName){
//        ApplicationBean appBean = null;
//        Cursor appCursor=null;
//        ContentResolver contentResolver = mContext.getContentResolver();
//        try{
//            appCursor=contentResolver.query(ApplicationColumn.CONTENT_URI, null,
//                    ApplicationColumn.APPNAME + "=?",
//                    new String[]{appName},null);
//            if(appCursor.getCount()==1)
//            {
//                if(appCursor.moveToFirst()){
//                    appBean = new ApplicationBean();
//                    int appId = appCursor.getInt(ApplicationColumnIndex.ID);
//                    String downDir = appCursor.getString(ApplicationColumnIndex.DOWNDIR);
//                    String fileName = appCursor.getString(ApplicationColumnIndex.FILENAME);
//                    String name = appCursor.getString(ApplicationColumnIndex.APPNAME);
//                    String appUrl =appCursor.getString(ApplicationColumnIndex.URL);
//                    int status = appCursor.getInt(ApplicationColumnIndex.STATUS);
//                    String pkgName = appCursor.getString(ApplicationColumnIndex.PKGNAME);
//                    byte[] icon = appCursor.getBlob(ApplicationColumnIndex.ICON);
//                    String version = appCursor.getString(ApplicationColumnIndex.VERSION);
//                    String serAppID = appCursor.getString(ApplicationColumnIndex.SERAPPID);
//               		String typeID = appCursor.getString(ApplicationColumnIndex.APPTYPEID);
//               		String typeName = appCursor.getString(ApplicationColumnIndex.TYPENAME);
//                    
//                    appBean.setId(appId);
//                    appBean.setDownDir(downDir);
//                    appBean.setFileName(fileName);
//                    appBean.setAppName(name);
//                    appBean.setUrl(appUrl);
//                    appBean.setStatus(status);
//                    appBean.setPkgName(pkgName);
//                    appBean.setIcon(icon);
//                    appBean.setVersion(version);
//                    appBean.setSerAppID(serAppID);
//               		appBean.setTypeID(typeID);
//               		appBean.setTypeName(typeName);
//               }
//            }
//        }finally{
//            if(appCursor!=null){
//                appCursor.close();
//            }
//        }
//        return appBean;
//    }
   
//    /**
//     * query application from table appstore_apps by url and appName
//     * @param url
//     * @param appName
//     * @return
//     */
//    public ApplicationBean queryApplicationByName(String url,String appName){
//    	ApplicationBean appBean = null;
//    	Cursor appCursor=null;
//    	ContentResolver contentResolver = mContext.getContentResolver();
//    	try{
//    		appCursor=contentResolver.query(ApplicationColumn.CONTENT_URI, null,
//    				ApplicationColumn.APPNAME + "= ? and "+ApplicationColumn.URL+ "=? ",
//    				new String[]{appName,url},null);
//    		if(appCursor.getCount()==1)
//    		{
//    			if(appCursor.moveToFirst()){
//    				appBean = new ApplicationBean();
//               		int appId = appCursor.getInt(ApplicationColumnIndex.ID);
//               		String downDir = appCursor.getString(ApplicationColumnIndex.DOWNDIR);
//               		String fileName = appCursor.getString(ApplicationColumnIndex.FILENAME);
//               		String name = appCursor.getString(ApplicationColumnIndex.APPNAME);
//               		String appUrl =appCursor.getString(ApplicationColumnIndex.URL);
//               		int status = appCursor.getInt(ApplicationColumnIndex.STATUS);
//               		String pkgName = appCursor.getString(ApplicationColumnIndex.PKGNAME);
//               		byte[] icon = appCursor.getBlob(ApplicationColumnIndex.ICON);
//               		String version = appCursor.getString(ApplicationColumnIndex.VERSION);
//               		String serAppID = appCursor.getString(ApplicationColumnIndex.SERAPPID);
//               		String typeID = appCursor.getString(ApplicationColumnIndex.APPTYPEID);
//               		String typeName = appCursor.getString(ApplicationColumnIndex.TYPENAME);
//               		
//               		appBean.setId(appId);
//               		appBean.setDownDir(downDir);
//               		appBean.setFileName(fileName);
//               		appBean.setAppName(name);
//               		appBean.setUrl(appUrl);
//               		appBean.setStatus(status);
//               		appBean.setPkgName(pkgName);
//               		appBean.setIcon(icon);
//               		appBean.setVersion(version);
//               		appBean.setSerAppID(serAppID);
//               		appBean.setTypeID(typeID);
//               		appBean.setTypeName(typeName);
//               }
//    		}
//           
//       	}finally{
//       		if(appCursor!=null){
//       			appCursor.close();
//       		}
//       	}
//    	return appBean;
//    }
    
    
//    /**
//     * query application from table appstore_apps by url and appName
//     * @param url
//     * @param appName
//     * @return
//     */
//    public ApplicationBean queryApplicationByNameOnly(String appName){
//    	ApplicationBean appBean = null;
//    	Cursor appCursor=null;
//    	ContentResolver contentResolver = mContext.getContentResolver();
//    	try{
//    		appCursor=contentResolver.query(ApplicationColumn.CONTENT_URI, null,
//    				ApplicationColumn.APPNAME + "= ? ",
//    				new String[]{appName},null);
//    		if(appCursor.getCount()==1)
//    		{
//    			if(appCursor.moveToFirst()){
//    				appBean = new ApplicationBean();
//               		int appId = appCursor.getInt(ApplicationColumnIndex.ID);
//               		String downDir = appCursor.getString(ApplicationColumnIndex.DOWNDIR);
//               		String fileName = appCursor.getString(ApplicationColumnIndex.FILENAME);
//               		String name = appCursor.getString(ApplicationColumnIndex.APPNAME);
//               		String appUrl =appCursor.getString(ApplicationColumnIndex.URL);
//               		int status = appCursor.getInt(ApplicationColumnIndex.STATUS);
//               		String pkgName = appCursor.getString(ApplicationColumnIndex.PKGNAME);
//               		byte[] icon = appCursor.getBlob(ApplicationColumnIndex.ICON);
//               		String version = appCursor.getString(ApplicationColumnIndex.VERSION);
//               		String serAppID = appCursor.getString(ApplicationColumnIndex.SERAPPID);
//               		String typeID = appCursor.getString(ApplicationColumnIndex.APPTYPEID);
//               		String typeName = appCursor.getString(ApplicationColumnIndex.TYPENAME);
//               		
//               		appBean.setId(appId);
//               		appBean.setDownDir(downDir);
//               		appBean.setFileName(fileName);
//               		appBean.setAppName(name);
//               		appBean.setUrl(appUrl);
//               		appBean.setStatus(status);
//               		appBean.setPkgName(pkgName);
//               		appBean.setIcon(icon);
//               		appBean.setVersion(version);
//               		appBean.setSerAppID(serAppID);
//               		appBean.setTypeID(typeID);
//               		appBean.setTypeName(typeName);
//               }
//    		}
//           
//       	}finally{
//       		if(appCursor!=null){
//       			appCursor.close();
//       		}
//       	}
//    	return appBean;
//    }
    
    
    /**
     * get all threads info by task 
     * @param tBean
     * @return
     */
    public List<ThreadBean> queryThreadsByTask(TaskBean tBean){
    	List<ThreadBean> threadsList = new ArrayList<ThreadBean>();
    	int id = tBean.getId();
    	ContentResolver contentResolver = mContext.getContentResolver();
    	Cursor threadCursor=null;
    	try{
    	 threadCursor=contentResolver.query(DownloadThreadColumn.CONTENT_URI, null,DownloadThreadColumn.TASK_ID + "= ?",new String[]{""+id},null);
        while(threadCursor.moveToNext()){
        	int key = threadCursor.getInt(threadCursor.getColumnIndex(DownloadThreadColumn.ID));
            int taskId = threadCursor.getInt(threadCursor.getColumnIndex(DownloadThreadColumn.TASK_ID));
            int  threadId = threadCursor.getInt(threadCursor.getColumnIndex(DownloadThreadColumn.THREADID));
            int position = threadCursor.getInt(threadCursor.getColumnIndex(DownloadThreadColumn.POSITION));
            int downLength = threadCursor.getInt(threadCursor.getColumnIndex(DownloadThreadColumn.DOWNLENGTH));
            
            ThreadBean tThreadBean = new ThreadBean();
            tThreadBean.setId(key);
            tThreadBean.setTaskId(taskId);
            tThreadBean.setThreadId(threadId);
            tThreadBean.setPosition(position);
            tThreadBean.setDownLength(downLength);
            threadsList.add(tThreadBean);
        }
    	}finally{
    		if(threadCursor!=null){
    			threadCursor.close();
    		}
    	}
    	return threadsList;
    }
    /**
     * get all complete task
     * @return
     */
    public List<TaskBean> queryCompletedTask(){
    	List<TaskBean> data = new ArrayList<TaskBean>();
    	List<ThreadBean> threadList = new ArrayList<ThreadBean>();
    	Cursor taskCursor=null;
    	Cursor threadCursor = null;
    	 try{
    	ContentResolver contentResolver = mContext.getContentResolver();
    	String whereClause = DownloadTaskColumn.SUMSIZE+" <= "+ DownloadTaskColumn.DOWNLOADSIZE;//TODO have one question downloadsize >=sumsize
    	taskCursor =contentResolver.query(DownloadTaskColumn.CONTENT_URI, null,whereClause , null,null);
    	while(taskCursor.moveToNext()){
    		TaskBean tTaskBean = new TaskBean();
            int id = taskCursor.getInt(taskCursor.getColumnIndex(DownloadTaskColumn.ID));
            String name = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.APPNAME));
            String downloadDir = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.DOWNDIR));
            String finalFileName = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.FINALFILENAME));
            String tmpFileName = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.TMPFILENAME));
            int downloadLength =taskCursor.getInt(taskCursor.getColumnIndex(DownloadTaskColumn.DOWNLOADSIZE));
            int sumSize = taskCursor.getInt(taskCursor.getColumnIndex(DownloadTaskColumn.SUMSIZE));
            int status = taskCursor.getInt(taskCursor.getColumnIndex(DownloadTaskColumn.STATUS));
            String url = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.URL));
            String iconUrl = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.ICONURL));
            byte[] icon = taskCursor.getBlob(taskCursor.getColumnIndex(DownloadTaskColumn.ICON));
            String serAppID = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.SERAPPID));
       		String typeID = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.APPTYPEID));
       		String typeName = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.TYPENAME));
       		String version = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.VERSION));
       		String pkgName = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.PKGNAME));
            
            
            threadList.clear();
            threadCursor=contentResolver.query(DownloadThreadColumn.CONTENT_URI, null,DownloadThreadColumn.TASK_ID + "= ?",new String[]{""+id},null);
            while(threadCursor.moveToNext()){
            	int key = threadCursor.getInt(threadCursor.getColumnIndex(DownloadThreadColumn.ID));
                int taskId = threadCursor.getInt(threadCursor.getColumnIndex(DownloadThreadColumn.TASK_ID));
                int  threadId = threadCursor.getInt(threadCursor.getColumnIndex(DownloadThreadColumn.THREADID));
                int position = threadCursor.getInt(threadCursor.getColumnIndex(DownloadThreadColumn.POSITION));
                int downLength = threadCursor.getInt(threadCursor.getColumnIndex(DownloadThreadColumn.DOWNLENGTH));
                
                ThreadBean tThreadBean = new ThreadBean();
                tThreadBean.setId(key);
                tThreadBean.setTaskId(taskId);
                tThreadBean.setThreadId(threadId);
                tThreadBean.setPosition(position);
                tThreadBean.setDownLength(downLength);
                threadList.add(tThreadBean);
            }
            
            tTaskBean.setId(id);
            tTaskBean.setAppName(name);
            tTaskBean.setDownloadDir(downloadDir);
            tTaskBean.setFinalFileName(finalFileName);
            tTaskBean.setTmpFileName(tmpFileName);
            tTaskBean.setSumSize(sumSize);
            tTaskBean.setDownloadSize(downloadLength);
            tTaskBean.setStatus(status);
            tTaskBean.setUrl(url);
            tTaskBean.setIconUrl(iconUrl);
            tTaskBean.setPkgName(pkgName);
            tTaskBean.setIcon(icon);
            tTaskBean.setSerAppID(serAppID);
            tTaskBean.setTypeID(typeID);
            tTaskBean.setTypeName(typeName);
            tTaskBean.setVersion(version);
            tTaskBean.setThreads(threadList);
            
            data.add(tTaskBean);
    		}
    	 }finally{
        	if(taskCursor!=null){
        		taskCursor.close();
        	}
        	if(threadCursor!=null){
        		threadCursor.close();
        	}
        }
    	return data;
    }
    
    public int queryTaskStatusById(int taskId){
    	int status = Constants.DOWNLOAD_STATUS_EXECUTE;
    	Cursor taskCursor =null;
    	try{
    	  ContentResolver contentResolver = mContext.getContentResolver();
    	  taskCursor =contentResolver.query(DownloadTaskColumn.CONTENT_URI, new String[]{DownloadTaskColumn.STATUS}, DownloadTaskColumn.ID+" = ? ", new String[]{""+taskId},null);
    	  if(null!=taskCursor&&taskCursor.getCount()==1){
    		  if(taskCursor.moveToFirst()){
      			status = taskCursor.getInt(0);
      		}
    	  }else{
    		  status =Constants.DOWNLOAD_STATUS_EXECUTE; 
    	  }
    	}finally{
    		 if(taskCursor!=null){
    			 taskCursor.close();
    		 }
    	}
    	return status;
    }
    
    /**
     * query task by taskId
     * @param taskId
     * @return
     */
    public TaskBean queryTaskById(int taskId){
    	TaskBean tTaskBean = new TaskBean();
    	List<ThreadBean> threadList = new ArrayList<ThreadBean>();
    	Cursor taskCursor=null;
    	Cursor threadCursor = null;
    	 try{
    	ContentResolver contentResolver = mContext.getContentResolver();
    	String whereClause = DownloadTaskColumn.ID +"="+taskId;
    	
    	String[] select={DownloadTaskColumn.ID,DownloadTaskColumn.APPNAME,DownloadTaskColumn.DOWNDIR,DownloadTaskColumn.FINALFILENAME,DownloadTaskColumn.TMPFILENAME
    			,DownloadTaskColumn.DOWNLOADSIZE,DownloadTaskColumn.SUMSIZE,DownloadTaskColumn.STATUS,DownloadTaskColumn.URL,DownloadTaskColumn.ICONURL,
    			DownloadTaskColumn.ICON,DownloadTaskColumn.SERAPPID,DownloadTaskColumn.APPTYPEID,DownloadTaskColumn.TYPENAME,DownloadTaskColumn.VERSION,DownloadTaskColumn.PKGNAME};
    	
    	taskCursor =contentResolver.query(DownloadTaskColumn.CONTENT_URI, select,whereClause , null,null);
    	AppLog.d(TAG, "-------------------count="+taskCursor.getCount()+"/taskId:" + taskId);
    	if(taskCursor.moveToFirst()){
    		AppLog.d(TAG, "==============taskCursor.getColumnIndex(DownloadTaskColumn.APPNAME)="+taskCursor.getColumnIndex(DownloadTaskColumn.ID));
            int id = taskCursor.getInt(taskCursor.getColumnIndex(DownloadTaskColumn.ID));
            AppLog.d(TAG, "===================id="+id);
            String name = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.APPNAME));
            String downloadDir = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.DOWNDIR));
            String finalFileName = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.FINALFILENAME));
            String tmpFileName = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.TMPFILENAME));
            int downloadLength =taskCursor.getInt(taskCursor.getColumnIndex(DownloadTaskColumn.DOWNLOADSIZE));
            int sumSize = taskCursor.getInt(taskCursor.getColumnIndex(DownloadTaskColumn.SUMSIZE));
            int status = taskCursor.getInt(taskCursor.getColumnIndex(DownloadTaskColumn.STATUS));
            String url = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.URL));
            String iconUrl = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.ICONURL));
            byte[] icon = taskCursor.getBlob(taskCursor.getColumnIndex(DownloadTaskColumn.ICON));
            String serAppID = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.SERAPPID));
       		String typeID = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.APPTYPEID));
       		String typeName = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.TYPENAME));
       		String version = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.VERSION));
       		String pkgName =taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.PKGNAME));
            
            threadList.clear();
            threadCursor=contentResolver.query(DownloadThreadColumn.CONTENT_URI, null,DownloadThreadColumn.TASK_ID + "= ?",new String[]{""+taskId},null);
            while(threadCursor.moveToNext()){
            	int key = threadCursor.getInt(threadCursor.getColumnIndex(DownloadThreadColumn.ID));
                int  threadId = threadCursor.getInt(threadCursor.getColumnIndex(DownloadThreadColumn.THREADID));
                int position = threadCursor.getInt(threadCursor.getColumnIndex(DownloadThreadColumn.POSITION));
                int downLength = threadCursor.getInt(threadCursor.getColumnIndex(DownloadThreadColumn.DOWNLENGTH));
                
                ThreadBean tThreadBean = new ThreadBean();
                tThreadBean.setId(key);
                tThreadBean.setTaskId(taskId);
                tThreadBean.setThreadId(threadId);
                tThreadBean.setPosition(position);
                tThreadBean.setDownLength(downLength);
                threadList.add(tThreadBean);
            }
            
            tTaskBean.setId(id);
            tTaskBean.setAppName(name);
            tTaskBean.setDownloadDir(downloadDir);
            tTaskBean.setFinalFileName(finalFileName);
            tTaskBean.setTmpFileName(tmpFileName);
            tTaskBean.setSumSize(sumSize);
            tTaskBean.setDownloadSize(downloadLength);
            tTaskBean.setStatus(status);
            tTaskBean.setUrl(url);
            tTaskBean.setIconUrl(iconUrl);
            tTaskBean.setPkgName(pkgName);
            tTaskBean.setIcon(icon);
            tTaskBean.setThreads(threadList);
            tTaskBean.setSerAppID(serAppID);
            tTaskBean.setTypeID(typeID);
            tTaskBean.setTypeName(typeName);
            tTaskBean.setVersion(version);
    		}
    	 }finally{
        	if(taskCursor!=null){
        		taskCursor.close();
        	}
        	if(threadCursor!=null){
        		threadCursor.close();
        	}
        }
    	return tTaskBean;
    }
    
    /**
     * query task by taskId
     * @param taskId
     * @return
     */
    public TaskBean queryTaskByPkgName(String pkgname){
    	TaskBean tTaskBean = new TaskBean();
    	List<ThreadBean> threadList = new ArrayList<ThreadBean>();
    	Cursor taskCursor=null;
    	Cursor threadCursor = null;
    	 try{
    	ContentResolver contentResolver = mContext.getContentResolver();
    	String whereClause = DownloadTaskColumn.PKGNAME +" = ? ";
    	taskCursor =contentResolver.query(DownloadTaskColumn.CONTENT_URI, null,whereClause , new String[]{pkgname+""},null);
    	if(taskCursor.moveToFirst()){
    		
            int id = taskCursor.getInt(taskCursor.getColumnIndex(DownloadTaskColumn.ID));
            String name = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.APPNAME));
            String downloadDir = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.DOWNDIR));
            String finalFileName = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.FINALFILENAME));
            String tmpFileName = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.TMPFILENAME));
            int downloadLength =taskCursor.getInt(DownloadTaskColumnIndex.DOWNLOADSIZE);
            int sumSize = taskCursor.getInt(taskCursor.getColumnIndex(DownloadTaskColumn.SUMSIZE));
            int status = taskCursor.getInt(taskCursor.getColumnIndex(DownloadTaskColumn.STATUS));
            String url = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.URL));
            String iconUrl = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.ICONURL));
            byte[] icon = taskCursor.getBlob(taskCursor.getColumnIndex(DownloadTaskColumn.ICON));
            String serAppID = taskCursor.getString(DownloadTaskColumnIndex.SERAPPID);
       		String typeID = taskCursor.getString(DownloadTaskColumnIndex.APPTYPEID);
       		String typeName = taskCursor.getString(DownloadTaskColumnIndex.TYPENAME);
       		String version = taskCursor.getString(DownloadTaskColumnIndex.VERSION);
       		String pkgName =taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.PKGNAME));
            
            threadList.clear();
            threadCursor=contentResolver.query(DownloadThreadColumn.CONTENT_URI, null,DownloadThreadColumn.TASK_ID + "= ?",new String[]{""+id},null);
            while(threadCursor.moveToNext()){
            	int key = threadCursor.getInt(threadCursor.getColumnIndex(DownloadThreadColumn.ID));
                int  threadId = threadCursor.getInt(threadCursor.getColumnIndex(DownloadThreadColumn.THREADID));
                int position = threadCursor.getInt(threadCursor.getColumnIndex(DownloadThreadColumn.POSITION));
                int downLength = threadCursor.getInt(threadCursor.getColumnIndex(DownloadThreadColumn.DOWNLENGTH));
                
                ThreadBean tThreadBean = new ThreadBean();
                tThreadBean.setId(key);
                tThreadBean.setTaskId(id);
                tThreadBean.setThreadId(threadId);
                tThreadBean.setPosition(position);
                tThreadBean.setDownLength(downLength);
                threadList.add(tThreadBean);
            }
            
            tTaskBean.setId(id);
            tTaskBean.setAppName(name);
            tTaskBean.setDownloadDir(downloadDir);
            tTaskBean.setFinalFileName(finalFileName);
            tTaskBean.setTmpFileName(tmpFileName);
            tTaskBean.setSumSize(sumSize);
            tTaskBean.setDownloadSize(downloadLength);
            tTaskBean.setStatus(status);
            tTaskBean.setUrl(url);
            tTaskBean.setIconUrl(iconUrl);
            tTaskBean.setPkgName(pkgName);
            tTaskBean.setIcon(icon);
            tTaskBean.setThreads(threadList);
            tTaskBean.setSerAppID(serAppID);
            tTaskBean.setTypeID(typeID);
            tTaskBean.setTypeName(typeName);
            tTaskBean.setVersion(version);
    		}
    	 }finally{
        	if(taskCursor!=null){
        		taskCursor.close();
        	}
        	if(threadCursor!=null){
        		threadCursor.close();
        	}
        }
    	return tTaskBean;
    }
    
    /**
     * query uncompleted task with not manual pause 
     * @return list TaskBean
     */
    public List<TaskBean> queryUnCompletedTask(){
    	List<TaskBean> data = new ArrayList<TaskBean>();
    	List<ThreadBean> threadList = new ArrayList<ThreadBean>();
    	Cursor taskCursor=null;
    	Cursor threadCursor = null;
    	 try{
    	ContentResolver contentResolver = mContext.getContentResolver();
    	String whereClause = DownloadTaskColumn.STATUS +"!="+Constants.DOWNLOAD_STATUS_PAUSE +" and "+DownloadTaskColumn.DOWNLOADSIZE+"<"+DownloadTaskColumn.SUMSIZE;
    	taskCursor =contentResolver.query(DownloadTaskColumn.CONTENT_URI, null,whereClause , null,null);
    	while(taskCursor.moveToNext()){
    		TaskBean tTaskBean = new TaskBean();
            int id = taskCursor.getInt(taskCursor.getColumnIndex(DownloadTaskColumn.ID));
            String name = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.APPNAME));
            String downloadDir = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.DOWNDIR));
            String finalFileName = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.FINALFILENAME));
            String tmpFileName = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.TMPFILENAME));
            int sumSize = taskCursor.getInt(taskCursor.getColumnIndex(DownloadTaskColumn.SUMSIZE));
            int downloadLength =taskCursor.getInt(DownloadTaskColumnIndex.DOWNLOADSIZE);
            AppLog.d(TAG, "&&&&&&&&&&&&&queryUnCompletedTask----------------------appName="+name+";downloadLength="+downloadLength+";sumSize="+sumSize);
            int status = taskCursor.getInt(taskCursor.getColumnIndex(DownloadTaskColumn.STATUS));
            String url = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.URL));
            String iconUrl = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.ICONURL));
            byte[] icon = taskCursor.getBlob(taskCursor.getColumnIndex(DownloadTaskColumn.ICON));
            String serAppID = taskCursor.getString(DownloadTaskColumnIndex.SERAPPID);
       		String typeID = taskCursor.getString(DownloadTaskColumnIndex.APPTYPEID);
       		String typeName = taskCursor.getString(DownloadTaskColumnIndex.TYPENAME);
       		String version = taskCursor.getString(DownloadTaskColumnIndex.VERSION);
       		String pkgName =taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.PKGNAME));
            
            threadList.clear();
            threadCursor=contentResolver.query(DownloadThreadColumn.CONTENT_URI, null,DownloadThreadColumn.TASK_ID + "= ?",new String[]{""+id},null);
            while(threadCursor.moveToNext()){
            	int key = threadCursor.getInt(threadCursor.getColumnIndex(DownloadThreadColumn.ID));
                int taskId = threadCursor.getInt(threadCursor.getColumnIndex(DownloadThreadColumn.TASK_ID));
                int  threadId = threadCursor.getInt(threadCursor.getColumnIndex(DownloadThreadColumn.THREADID));
                int position = threadCursor.getInt(threadCursor.getColumnIndex(DownloadThreadColumn.POSITION));
                int downLength = threadCursor.getInt(threadCursor.getColumnIndex(DownloadThreadColumn.DOWNLENGTH));
                
                ThreadBean tThreadBean = new ThreadBean();
                tThreadBean.setId(key);
                tThreadBean.setTaskId(taskId);
                tThreadBean.setThreadId(threadId);
                tThreadBean.setPosition(position);
                tThreadBean.setDownLength(downLength);
                threadList.add(tThreadBean);
            }
            
            tTaskBean.setId(id);
            tTaskBean.setAppName(name);
            tTaskBean.setDownloadDir(downloadDir);
            tTaskBean.setFinalFileName(finalFileName);
            tTaskBean.setTmpFileName(tmpFileName);
            tTaskBean.setSumSize(sumSize);
            tTaskBean.setDownloadSize(downloadLength);
            tTaskBean.setStatus(status);
            tTaskBean.setUrl(url);
            tTaskBean.setIconUrl(iconUrl);
            tTaskBean.setPkgName(pkgName);
            tTaskBean.setIcon(icon);
            tTaskBean.setThreads(threadList);
            tTaskBean.setSerAppID(serAppID);
            tTaskBean.setTypeID(typeID);
            tTaskBean.setTypeName(typeName);
            tTaskBean.setVersion(version);
            
            data.add(tTaskBean);
    		}
    	 }finally{
        	if(taskCursor!=null){
        		taskCursor.close();
        	}
        	if(threadCursor!=null){
        		threadCursor.close();
        	}
        }
    	return data;
    }
    
    /**
     * update execute task status is uncompleted when service destroy 
     */
    public void updateTaskWithUnCompleted(){
    	Cursor taskCursor=null;
    	 try{
    	ContentResolver contentResolver = mContext.getContentResolver();
    	String whereClause = DownloadTaskColumn.STATUS +"="+Constants.DOWNLOAD_STATUS_EXECUTE;
    	taskCursor =contentResolver.query(DownloadTaskColumn.CONTENT_URI, null,whereClause , null,null);
    	ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
    	while(taskCursor.moveToNext()){
            ops.add(ContentProviderOperation.newUpdate(DownloadTaskColumn.CONTENT_URI).
            		withValue(DownloadTaskColumn.STATUS, Constants.DOWNLOAD_STATUS_EXIT_STOP)
            		.withSelection(DownloadTaskColumn.ID+"= ?", new String[]{""+taskCursor.getInt(DownloadTaskColumnIndex.ID)}).build());
    		}
    	try {
			contentResolver.applyBatch(DatabaseHelper.AUTHORITY, ops);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			e.printStackTrace();
			}
    	 }finally{
         	if(taskCursor!=null){
         		taskCursor.close();
         	}
         }
    }
    
    //当卸载预置应用的更新后，要恢复之前预置应用的相关信息，版本号，数据图标
    public void revertSystemApp(String pkgname){
    	PackageManager pManager = mContext.getPackageManager();
		List<PackageInfo> packages = pManager.getInstalledPackages(0);
		for(PackageInfo pakg:packages){
			if(pakg.packageName.equals(pkgname)){
				ContentResolver contentResolver = mContext.getContentResolver();
				ContentValues application = new ContentValues();
		    	application.put(ApplicationColumn.VERSION,pakg.versionCode);
		    	Drawable drawable = pakg.applicationInfo.loadIcon(pManager);
				Bitmap b = AppManager.drawableToBitmap(drawable);
				byte[] icon = AppManager.Bitmap2Bytes(b);
		    	application.put(ApplicationColumn.ICON,icon);
		    	application.put(ApplicationColumn.STATUS, Constants.APP_STATUS_INSTALLED);
		    	application.put(ApplicationColumn.APPSOURCE, Constants.APP_SOURCE_INNER);
		    	int count=contentResolver.update(ApplicationColumn.CONTENT_URI, application, ApplicationColumn.PKGNAME+"= ?", new String[]{pkgname});
		    	if(count>0){
		    		mContext.getContentResolver().notifyChange(ApplicationColumn.CONTENT_URI, null);
		    	}
		    	return;
			}
		}
    }
    
    /**
     * get task by url and appName 
     * @param url
     * @param appName
     * @return
     */
    public List<TaskBean> queryTaskByUrl(String url,String pgkname){
    	List<TaskBean> data = new ArrayList<TaskBean>();
    	List<ThreadBean> threadList = new ArrayList<ThreadBean>();
    	Cursor taskCursor=null;
    	Cursor threadCursor = null;
    	 try{
    	ContentResolver contentResolver = mContext.getContentResolver();
    	taskCursor =contentResolver.query(DownloadTaskColumn.CONTENT_URI, null, DownloadTaskColumn.URL+"=? and "+DownloadTaskColumn.PKGNAME+"=?", new String[]{url,pgkname},null);
    	while(taskCursor.moveToNext()){
    		TaskBean tTaskBean = new TaskBean();
            int id = taskCursor.getInt(taskCursor.getColumnIndex(DownloadTaskColumn.ID));
            String name = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.APPNAME));
            String downloadDir = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.DOWNDIR));
            String finalFileName = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.FINALFILENAME));
            String tmpFileName = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.TMPFILENAME));
            int sumSize = taskCursor.getInt(taskCursor.getColumnIndex(DownloadTaskColumn.SUMSIZE));
            int downloadLength =taskCursor.getInt(DownloadTaskColumnIndex.DOWNLOADSIZE);
            int status = taskCursor.getInt(taskCursor.getColumnIndex(DownloadTaskColumn.STATUS));
            String urlName = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.URL));
            String iconUrl = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.ICONURL));
            byte[] icon = taskCursor.getBlob(taskCursor.getColumnIndex(DownloadTaskColumn.ICON));
            String serAppID = taskCursor.getString(DownloadTaskColumnIndex.SERAPPID);
       		String typeID = taskCursor.getString(DownloadTaskColumnIndex.APPTYPEID);
       		String typeName = taskCursor.getString(DownloadTaskColumnIndex.TYPENAME);
       		String version = taskCursor.getString(DownloadTaskColumnIndex.VERSION);
       		String pkgName =taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.PKGNAME));
            
            threadList.clear();
            threadCursor=contentResolver.query(DownloadThreadColumn.CONTENT_URI, null,DownloadThreadColumn.TASK_ID + "= ?",new String[]{""+id},null);
            while(threadCursor.moveToNext()){
            	int key = threadCursor.getInt(threadCursor.getColumnIndex(DownloadThreadColumn.ID));
                int taskId = threadCursor.getInt(threadCursor.getColumnIndex(DownloadThreadColumn.TASK_ID));
                int  threadId = threadCursor.getInt(threadCursor.getColumnIndex(DownloadThreadColumn.THREADID));
                int position = threadCursor.getInt(threadCursor.getColumnIndex(DownloadThreadColumn.POSITION));
                int downLength = threadCursor.getInt(threadCursor.getColumnIndex(DownloadThreadColumn.DOWNLENGTH));
                
                ThreadBean tThreadBean = new ThreadBean();
                tThreadBean.setId(key);
                tThreadBean.setTaskId(taskId);
                tThreadBean.setThreadId(threadId);
                tThreadBean.setPosition(position);
                tThreadBean.setDownLength(downLength);
                threadList.add(tThreadBean);
            }
            
            tTaskBean.setId(id);
            tTaskBean.setAppName(name);
            tTaskBean.setDownloadDir(downloadDir);
            tTaskBean.setFinalFileName(finalFileName);
            tTaskBean.setTmpFileName(tmpFileName);
            tTaskBean.setSumSize(sumSize);
            tTaskBean.setDownloadSize(downloadLength);
            tTaskBean.setStatus(status);
            tTaskBean.setUrl(urlName);
            tTaskBean.setIconUrl(iconUrl);
            tTaskBean.setPkgName(pkgName);
            tTaskBean.setIcon(icon);
            tTaskBean.setThreads(threadList);
            tTaskBean.setSerAppID(serAppID);
            tTaskBean.setTypeID(typeID);
            tTaskBean.setTypeName(typeName);
            tTaskBean.setVersion(version);
            
            data.add(tTaskBean);
    		}
    	 }finally{
    		if(taskCursor!=null){
    			taskCursor.close();
    		}
    		if(threadCursor!=null){
        		threadCursor.close();
        	}
    	 }
    	 return data;
    }
    
    /**
     * query app status by appname  
     * @param appName APP_STATUS_DOWNLOADING = 1;
	public static final int APP_STATUS_DOWNLOADED = 2;
	public static final int APP_STATUS_INSTALLED = 3;
	public static final int APP_STATUS_UNDOWNLOAD = 4;
     * @return status 1.downloading 2.downloaded 3.installed 4.undownload
     */
    /**queryStatusByAppName*/
	public int queryStatusByPkgName(String pkgname){
    	int status = Constants.APP_STATUS_UNDOWNLOAD;
    	if(pkgname==null||pkgname.equals("")){
    		AppLog.d(TAG, "------------pakname is null--------");
    		return status;
    	}
    	Cursor taskCursor=null;
    	Cursor appCursor =null;
    	try{
    	ContentResolver contentResolver = mContext.getContentResolver();
    	taskCursor =contentResolver.query(DownloadTaskColumn.CONTENT_URI, new String[]{DownloadTaskColumn.STATUS}, DownloadTaskColumn.PKGNAME+"= ?", new String[]{pkgname},null);
    	if(null!=taskCursor&&taskCursor.getCount()==1){
    		if(taskCursor.moveToFirst()){
    			status = taskCursor.getInt(0);
    			AppLog.d(TAG, "---------DownloadTask---&&&&&&&&&&&&&&&taskCursor  status="+status);
    		}
    	}else{
    	  appCursor =contentResolver.query(ApplicationColumn.CONTENT_URI, new String[]{ApplicationColumn.STATUS}, ApplicationColumn.PKGNAME+"= ?", new String[]{pkgname},null);
    	  if(null!=appCursor&&appCursor.getCount()==1){
    		  if(appCursor.moveToFirst()){
      			status = appCursor.getInt(0);
      			AppLog.d(TAG, "----------Application-------*******************appCursor  status="+status);
      		}
    	  }else{
    		  status =Constants.APP_STATUS_UNDOWNLOAD; 
    	  }
    	}
    	}finally{
    		 if(appCursor!=null){
    			 appCursor.close();
    		 }
        	if(taskCursor!=null){
        		taskCursor.close();
        	}
    	}
    	return status;
    }
	
	public int queryStatusByPkgNameAndVersion(String pkgname,String version){
    	int status = Constants.APP_STATUS_UNDOWNLOAD;
    	if(pkgname==null||pkgname.equals("")){
    		AppLog.d(TAG, "------------pakname is null--------");
    		return status;
    	}
    	Cursor taskCursor=null;
    	Cursor appCursor =null;
    	try{
    	ContentResolver contentResolver = mContext.getContentResolver();
    	taskCursor =contentResolver.query(DownloadTaskColumn.CONTENT_URI, new String[]{DownloadTaskColumn.STATUS}, DownloadTaskColumn.PKGNAME+"= ? and "+DownloadTaskColumn.VERSION+"=? ", new String[]{pkgname,""+version},null);
    	if(null!=taskCursor&&taskCursor.getCount()==1){
    		if(taskCursor.moveToFirst()){
    			status = taskCursor.getInt(0);
    			AppLog.d(TAG, "---------DownloadTask---&&&&&&&&&&&&&&&taskCursor  status="+status);
    		}
    	}else{
    	  appCursor =contentResolver.query(ApplicationColumn.CONTENT_URI, new String[]{ApplicationColumn.STATUS}, ApplicationColumn.PKGNAME+"= ? and "+ApplicationColumn.VERSION+"=?", new String[]{pkgname,""+version},null);
    	  if(null!=appCursor&&appCursor.getCount()==1){
    		  if(appCursor.moveToFirst()){
      			status = appCursor.getInt(0);
      			AppLog.d(TAG, "----------Application-------*******************appCursor  status="+status);
      		}
    	  }else{
    		  status =Constants.APP_STATUS_UNDOWNLOAD; 
    	  }
    	}
    	}finally{
    		 if(appCursor!=null){
    			 appCursor.close();
    		 }
        	if(taskCursor!=null){
        		taskCursor.close();
        	}
    	}
    	return status;
    }
	
	/**queryStatusBypkgName*/
	public int queryStatusByPKGName(String pkgname){
    	int status = Constants.APP_STATUS_UNDOWNLOAD;
    	if(pkgname==null||pkgname.equals("")){
    		AppLog.d(TAG, "------------pakname is null--------");
    		return status;
    	}
    	Cursor taskCursor=null;
    	Cursor appCursor =null;
    	try{
    	  ContentResolver contentResolver = mContext.getContentResolver();
    	  appCursor =contentResolver.query(ApplicationColumn.CONTENT_URI, new String[]{ApplicationColumn.STATUS}, ApplicationColumn.PKGNAME+"= ?", new String[]{pkgname},null);
    	  if(null!=appCursor&&appCursor.getCount()==1){
    		  if(appCursor.moveToFirst()){
      			status = appCursor.getInt(0);
      			AppLog.d(TAG, "-----------------*******************appCursor  status="+status);
      		}
    	  }else{
    		  status =Constants.APP_STATUS_UNDOWNLOAD; 
    	  }
    	}finally{
    		 if(appCursor!=null){
    			 appCursor.close();
    		 }
        	if(taskCursor!=null){
        		taskCursor.close();
        	}
    	}
    	return status;
    }
	
	public int queryStatusByPkgNameFormApplication(String pkgname){
    	int status = Constants.APP_STATUS_UNDOWNLOAD;
    	if(pkgname==null||pkgname.equals("")){
//    		AppLog.d(TAG, "------------pakname is null--------");
    		return status;
    	}
    	Cursor appCursor =null;
    	try{
    	  ContentResolver contentResolver = mContext.getContentResolver();
    	  appCursor =contentResolver.query(ApplicationColumn.CONTENT_URI, new String[]{ApplicationColumn.STATUS}, ApplicationColumn.PKGNAME+" = ? ", new String[]{pkgname},null);
    	  if(null!=appCursor&&appCursor.getCount()==1){
    		  if(appCursor.moveToFirst()){
      			status = appCursor.getInt(0);
//      			AppLog.d(TAG, "-----------------*******************appCursor  status="+status);
      		}
    	  }else{
    		  status =Constants.APP_STATUS_UNDOWNLOAD; 
    	  }
    	}finally{
    		 if(appCursor!=null){
    			 appCursor.close();
    		 }
    	}
//    	AppLog.d(TAG, "------------------------status="+status);
    	return status;
    }
	
    /**
     * 查询app版本
     * @param appName
     * @return
     */
    public int queryTaskIDByPkgName(String pkgname){
        int taskID = -1;
        if(pkgname==null||pkgname.equals("")){
        	AppLog.d(TAG, "------------pakname is null--------");
    		return taskID;
    	}
        Cursor taskCursor =null;
        try{
            ContentResolver contentResolver = mContext.getContentResolver();
            taskCursor =contentResolver.query(DownloadTaskColumn.CONTENT_URI, new String[]{DownloadTaskColumn.ID}, DownloadTaskColumn.PKGNAME+"= ?", new String[]{pkgname},null);
            AppLog.d(TAG, "-----------taskCursor.getCount()"+(taskCursor==null?"null":taskCursor.getCount()));
            if(null!=taskCursor&&taskCursor.getCount()==1){
                if(taskCursor.moveToFirst()){
                    taskID = taskCursor.getInt(0);
                }
            }
        }finally{
            if(taskCursor!=null){
                taskCursor.close();
            }
        }
        return taskID;
    }
    
//    /**
//     * 根据应用名查询app版本
//     * @param appName
//     * @return
//     */
//    public String queryVersionByPkgName(String pkgname){
//        String version = "";
//        Cursor appCursor =null;
//        try{
//            ContentResolver contentResolver = mContext.getContentResolver();
//            appCursor =contentResolver.query(ApplicationColumn.CONTENT_URI, new String[]{ApplicationColumn.VERSION}, ApplicationColumn.PKGNAME+"= ?", new String[]{pkgname},null);
//            if(null!=appCursor&&appCursor.getCount()==1){
//                if(appCursor.moveToFirst()){
//                    version = appCursor.getString(0);
//                }
//            }
//        }finally{
//            if(appCursor!=null){
//                appCursor.close();
//            }
//        }
//        return version;
//    }
    
    /**
     * 根据pkgName查版本号
     * @param pkgName
     * @return
     */
    public String queryVersionByPkgName(String pkgName){
    	String version = "";
        Cursor appCursor =null;
        try{
            ContentResolver contentResolver = mContext.getContentResolver();
            appCursor =contentResolver.query(ApplicationColumn.CONTENT_URI, new String[]{ApplicationColumn.VERSION}, ApplicationColumn.PKGNAME+"= ?", new String[]{pkgName},null);
            if(null!=appCursor&&appCursor.getCount()==1){
                if(appCursor.moveToFirst()){
                    version = appCursor.getString(0);
                }
            }
        }finally{
            if(appCursor!=null){
                appCursor.close();
            }
        }
        return version;
    }
    
    /**
     * 查询已下载app的大小
     * @param appName
     * @return
     */
    public int queryDowningSizeByPkgName(String pkgname){
        int size =0;
        Cursor appCursor =null;
        try{
            ContentResolver contentResolver = mContext.getContentResolver();
            appCursor =contentResolver.query(DownloadTaskColumn.CONTENT_URI, new String[]{DownloadTaskColumn.DOWNLOADSIZE}, DownloadTaskColumn.PKGNAME+"= ?", new String[]{pkgname},null);
            if(null!=appCursor&&appCursor.getCount()==1){
                if(appCursor.moveToFirst()){
                    size = appCursor.getInt(0);
                }
            }
        }finally{
            if(appCursor!=null){
                appCursor.close();
            }
        }
        return size;
    }
    
    /**
     * 查询已下载app的大小
     * @param appName
     * @return querySumSizeByAppName
     */
    public int querySumSizeByPkgName(String pkgname){
        int size =0;
        Cursor appCursor =null;
        try{
            ContentResolver contentResolver = mContext.getContentResolver();
            appCursor =contentResolver.query(DownloadTaskColumn.CONTENT_URI, new String[]{DownloadTaskColumn.SUMSIZE}, DownloadTaskColumn.PKGNAME+"= ?", new String[]{pkgname},null);
            if(null!=appCursor&&appCursor.getCount()==1){
                if(appCursor.moveToFirst()){
                    size = appCursor.getInt(0);
                }
            }
        }finally{
            if(appCursor!=null){
                appCursor.close();
            }
        }
        return size;
    }
	
    /**
     * get all download task
     * @return
     */
    public List<TaskBean> queryAllTask(){
    	List<TaskBean> data = new ArrayList<TaskBean>();
    	List<ThreadBean> threadList = new ArrayList<ThreadBean>();
    	Cursor taskCursor=null;
    	Cursor threadCursor = null;
    	 try{
    	ContentResolver contentResolver = mContext.getContentResolver();
    	taskCursor =contentResolver.query(DownloadTaskColumn.CONTENT_URI, null, null, null,null);
    	while(taskCursor.moveToNext()){
    		TaskBean tTaskBean = new TaskBean();
            int id = taskCursor.getInt(taskCursor.getColumnIndex(DownloadTaskColumn.ID));
            String name = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.APPNAME));
            String downloadDir = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.DOWNDIR));
            String finalFileName = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.FINALFILENAME));
            String tmpFileName = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.TMPFILENAME));
            int sumSize = taskCursor.getInt(taskCursor.getColumnIndex(DownloadTaskColumn.SUMSIZE));
            int downloadLength =taskCursor.getInt(DownloadTaskColumnIndex.DOWNLOADSIZE);
            int status = taskCursor.getInt(taskCursor.getColumnIndex(DownloadTaskColumn.STATUS));
            String url = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.URL));
            String iconUrl = taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.ICONURL));
            byte[] icon = taskCursor.getBlob(taskCursor.getColumnIndex(DownloadTaskColumn.ICON));
            String serAppID = taskCursor.getString(DownloadTaskColumnIndex.SERAPPID);
       		String typeID = taskCursor.getString(DownloadTaskColumnIndex.APPTYPEID);
       		String typeName = taskCursor.getString(DownloadTaskColumnIndex.TYPENAME);
       		String version = taskCursor.getString(DownloadTaskColumnIndex.VERSION);
       		String pkgName =taskCursor.getString(taskCursor.getColumnIndex(DownloadTaskColumn.PKGNAME));
            
            threadList.clear();
            threadCursor=contentResolver.query(DownloadThreadColumn.CONTENT_URI, null,DownloadThreadColumn.TASK_ID + "= ?",new String[]{""+id},null);
            while(threadCursor.moveToNext()){
            	int key = threadCursor.getInt(threadCursor.getColumnIndex(DownloadThreadColumn.ID));
                int taskId = threadCursor.getInt(threadCursor.getColumnIndex(DownloadThreadColumn.TASK_ID));
                int  threadId = threadCursor.getInt(threadCursor.getColumnIndex(DownloadThreadColumn.THREADID));
                int position = threadCursor.getInt(threadCursor.getColumnIndex(DownloadThreadColumn.POSITION));
                int downLength = threadCursor.getInt(threadCursor.getColumnIndex(DownloadThreadColumn.DOWNLENGTH));
                
                ThreadBean tThreadBean = new ThreadBean();
                tThreadBean.setId(key);
                tThreadBean.setTaskId(taskId);
                tThreadBean.setThreadId(threadId);
                tThreadBean.setPosition(position);
                tThreadBean.setDownLength(downLength);
                threadList.add(tThreadBean);
            }
            
            tTaskBean.setId(id);
            tTaskBean.setAppName(name);
            tTaskBean.setDownloadDir(downloadDir);
            tTaskBean.setFinalFileName(finalFileName);
            tTaskBean.setTmpFileName(tmpFileName);
            tTaskBean.setSumSize(sumSize);
            tTaskBean.setDownloadSize(downloadLength);
            tTaskBean.setStatus(status);
            tTaskBean.setUrl(url);
            tTaskBean.setPkgName(pkgName);
            tTaskBean.setIconUrl(iconUrl);
            tTaskBean.setIcon(icon);
            tTaskBean.setThreads(threadList);
            tTaskBean.setSerAppID(serAppID);
            tTaskBean.setTypeID(typeID);
            tTaskBean.setTypeName(typeName);
            tTaskBean.setVersion(version);
            
            data.add(tTaskBean);
    		}
    	 }finally{
    		 if(threadCursor!=null){
    			 threadCursor.close();
    		 }
        	if(taskCursor!=null){
        		taskCursor.close();
        	}
        }
    	return data;
    }
    
    /**
     * wrap provider 
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return
     */
    public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder){
    	ContentResolver contentResolver = mContext.getContentResolver();
    	Cursor cursor = contentResolver.query(uri, projection, selection,
    			selectionArgs, sortOrder);
    	return cursor;
    }
}
