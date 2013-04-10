package com.joysee.appstore.thread;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;

import com.joysee.appstore.common.ApplicationBean;
import com.joysee.appstore.common.AppsBean;
import com.joysee.appstore.common.DataOperate;
import com.joysee.appstore.utils.AppLog;

/**
 *请求服务器，返回可更新应用
 */
public class SearchUpdateThread extends Thread{
	private static final String TAG = "SearchUpdateFromSer";
	Context con;
	DataOperate operate;
	List<AppsBean> searchlist;     //返回回去的list<AppsBean>
	List<ApplicationBean> mList;   //传递过来的本地应用list
	List<ApplicationBean> mListRes;//服务器返回的搜索结果list
	private Boolean tag = false;
	
	public SearchUpdateThread(Context con,List<ApplicationBean> list){
		this.con = con;
		operate = new DataOperate(con);
		mListRes = new ArrayList<ApplicationBean>();
		searchlist = new ArrayList<AppsBean>();
		mList = list;
	}
	
	public void run() {
		super.run();
		mListRes = operate.getNativeUpdateAppsList(mList);
		if(null!=mListRes){
			tag = true;
			for(ApplicationBean app : mListRes){
				AppsBean upApp = new AppsBean();
				upApp.setAppName(app.getAppName());
				upApp.setPkgName(app.getPkgName());
				upApp.setApkUrl(app.getUrl());
				upApp.setVersion(app.getVersion());
				upApp.setSerImageUrl(app.getIconUrl());
				upApp.setTypeName(app.getTypeName());
				searchlist.add(upApp);
				AppLog.d(TAG, "---Having an upgrade: "+app.getAppName()+"--IconUrl"+app.getIconUrl()+"---------");
			}			
		}
	}
	
	public List<AppsBean> getUpdate(){
		if(tag){
			AppLog.d(TAG, "--------------getUpdate is true-------------------");
			return searchlist;
		}else{
			AppLog.d(TAG, "--------------getUpdate is false-------------------");
			return null;
		}
	}
}
