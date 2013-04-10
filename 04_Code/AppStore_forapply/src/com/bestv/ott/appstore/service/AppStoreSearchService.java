package com.bestv.ott.appstore.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import com.bestv.ott.appstore.aidl.IAppStoreSearch;
import com.bestv.ott.appstore.common.ApplicationBean;
import com.bestv.ott.appstore.common.AppsBean;
import com.bestv.ott.appstore.common.Constants;
import com.bestv.ott.appstore.common.DataOperate;
import com.bestv.ott.appstore.common.PageBean;
import com.bestv.ott.appstore.db.DBUtils;
import com.bestv.ott.appstore.service.DownloadService.ServiceBinder;
import com.bestv.ott.appstore.thread.SearchKeyThread;
import com.bestv.ott.appstore.thread.SearchUpdateThread;
import com.bestv.ott.appstore.utils.AppLog;
import com.bestv.ott.appstore.utils.CaCheManager;

/**
 * 给第三方提供的AIDL接口
 * @author benz
 */
public class AppStoreSearchService extends Service {
	private static final String TAG = "com.joysee.appstore.AppStoreSearchService";
	public IBinder onBind(Intent intent) {
		SearchSeviceAIDL search = new SearchSeviceAIDL();
		return search;
	}
	
	class SearchSeviceAIDL extends IAppStoreSearch.Stub implements IBinder.DeathRecipient{
		
		//查询所有可更新应用
		public List<AppsBean> searchAllUpgrade(){
			AppLog.d(TAG,"-------------------------searchTheUpdate---------------------------");
			List<ApplicationBean> applist = new ArrayList<ApplicationBean>();//已安装应用list
			applist = new DBUtils(AppStoreSearchService.this).queryApplicationByStatus(Constants.APP_STATUS_INSTALLED);
			AppLog.d(TAG,"-----------result applist.size() : -----"+applist.size()+"-------------");
			if(applist!=null && !"".equals(applist)){
				SearchUpdateThread sut = new SearchUpdateThread(AppStoreSearchService.this, applist);
				sut.start();
				try{
					sut.join();
				}catch(Exception e){
				}
				return sut.getUpdate();		
			}
			return null;
		}
		
		
		//查询多个应用是否有升级版本   return可升级列表
		public List<AppsBean> searchSomeUpgrade(List<String> pkgNameList){
			AppLog.d(TAG,"-------------------------searchSomeUpgrade------------------------pkgNameList.size＝"+pkgNameList.size());
			List<ApplicationBean> applist = new ArrayList<ApplicationBean>();
			ApplicationBean app = new ApplicationBean();
			//根据包名list查本地数据库,返回一个list<AppsBean> (包含版本号和包名)
			if(null==pkgNameList){
				return null;
			}
			DBUtils db = new DBUtils(AppStoreSearchService.this);
			for(String pkgName : pkgNameList){
				String version = db.queryVersionByPkgName(pkgName);
				app.setVersion(version);
				app.setPkgName(pkgName);
				applist.add(app);
			}
			//带着list<AppsBean>请求服务器
			SearchUpdateThread searchSome = new SearchUpdateThread(AppStoreSearchService.this, applist);
			searchSome.start();
			try{
				searchSome.join();
			}catch(Exception e){
				throw new RuntimeException();
			}
			//服务器返回list
			if(null != searchSome.getUpdate()){
				return searchSome.getUpdate();
			}
			return null;
		}		
		
		
		//查询某个应用是否有升级版本,有则直接升级
		public AppsBean isUpgradeForOne(String pkgName){
			AppLog.d(TAG,"-------------------------isUpgradeForOne-------------pkgName="+pkgName);
			AppsBean app = isUpgrade(pkgName);//调自己内部查询升级方法
			if(null == app){
				return null;
			}
			//调用内部升级应用方法
			upgradeOneApp(app);
			//返回<pkgname,version>
			AppLog.d(TAG, "----------- return map<"+app.getPkgName()+","+app.getVersion()+"> ----------");
			return app;
		}		
		

		//查询某个应用是否可升级,查出但不升级
		public AppsBean isUpgrade(String pkgName){
			AppLog.d(TAG,"-------------------------isUpgrade------pkgName="+pkgName);
			if(null==pkgName || "".equals(pkgName)){
				return null;
			}
			List<ApplicationBean> applist = new ArrayList<ApplicationBean>();
			ApplicationBean app = new ApplicationBean();
			//根据包名搜本地数据库,查出版本号
			String version = new DBUtils(AppStoreSearchService.this).queryVersionByPkgName(pkgName);
			AppLog.d(TAG, "------------queryVersion by "+pkgName+" | version = "+version+"------------");
			//根据查出的版本号封装成Bean 请求服务器
			if(!"".equals(version)){
				app.setVersion(version);
				app.setPkgName(pkgName);
				applist.add(app);
				SearchUpdateThread searchOne = new SearchUpdateThread(AppStoreSearchService.this, applist);
				searchOne.start();
				try{
					searchOne.join();
				}catch(Exception e){
					throw new RuntimeException();
				}
				if(null != searchOne.getUpdate()){
					return searchOne.getUpdate().get(0);
				}
			}
			return null;
		}
		
		
		//升级某个已知可更新应用
		public void upgradeOneApp(final AppsBean appsBean){
			AppLog.d(TAG,"-------------------------upgradeOneApp------pkgName="+appsBean.getAppName());
			if(null == appsBean){
				return;
			}
			final DBUtils dbUtils = new DBUtils(AppStoreSearchService.this);
			File baseDir = Environment.getExternalStorageDirectory();
			File downDir = new File(baseDir, "joysee");
			appsBean.setDownloadDir(downDir.getAbsolutePath());// 设置下载路径
			ServiceConnection conn=new ServiceConnection() {
				ServiceBinder downloadService;
				public void onServiceConnected(ComponentName name,IBinder service) {
					AppLog.d(TAG, "---------------DownloadService is connection--------------");
					downloadService = ((DownloadService.ServiceBinder)service).getService();
					if(downloadService!=null && null!=appsBean){
						downloadService.startDownload(appsBean);
						AppLog.d(TAG, "------------------startDownload------------------");
						dbUtils.updateAppStatusByPkgName(appsBean.getPkgName(),Constants.APP_STATUS_UPDATE);
					}else{
						AppLog.d(TAG, "----------------downloadService or appsBean is null--------------");
					}
				}
				public void onServiceDisconnected(ComponentName name) {
					downloadService = null;
				}
			};
			//启动下载服务
			Intent intent = new Intent("com.bestv.ott.appstore.service.DownloadService");
			CaCheManager manager = new CaCheManager(); 
			if(CaCheManager.checkSDcard() && !manager.isFreeSpace(appsBean.getSize())){
				bindService(intent, conn, BIND_AUTO_CREATE);
			}else{
				AppLog.d(TAG, "------------------- SDCard is error or full -----------------");
			}
		}
		
		
		//根据关键字搜索
		public List<AppsBean> searchInAppStore(String condition) throws RemoteException {
			AppLog.d(TAG, "----------searchInAppStore  |  condition = "+condition+"----------------");
			List<AppsBean> searchlist = new ArrayList<AppsBean>();//服务器返回的搜索结果
			if(condition==null || condition.trim().equals("")){
				return null;
			}else{
				final PageBean pageBean=new PageBean();
				pageBean.setKeyWord(condition.trim()); //设置关键字
				DataOperate operate = new DataOperate(AppStoreSearchService.this);
				operate.setPageBean(pageBean);
				SearchKeyThread skt=new SearchKeyThread(operate,AppStoreSearchService.this);
				skt.start();
				try{
					skt.join();
				}catch(Exception e){
				}
				searchlist = skt.getSearch();
				AppLog.d(TAG, "----------- : "+ searchlist.size()+"---------");
				return searchlist;
			}
		}


		public void binderDied() {
			
		}
	}
}
