package com.bestv.ott.appstore.thread;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.util.Log;

import com.bestv.ott.appstore.common.AppsBean;
import com.bestv.ott.appstore.common.PageBean;
import com.bestv.ott.appstore.parser.AppsParser;
import com.bestv.ott.appstore.parser.MenusParser;
import com.bestv.ott.appstore.utils.AppLog;
import com.bestv.ott.appstore.utils.CaCheManager;
import com.bestv.ott.appstore.utils.NetUtil;
import com.bestv.ott.appstore.utils.RequestParam;

/**
 * 监听是否有网络，并进行预加载
 * 
 * @author benz
 * 
 */
public class PreparingTheData extends Thread {

	private static final String TAG = "com.bestv.ott.appstore.thread.PreparingTheData";
	
	private Context context;
	
	public PreparingTheData(Context context){
		this.context=context;
	}

	public void run() {
		Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        super.run();
        PageBean mPageBean = new PageBean();
		//加载推荐应用信息
		mPageBean.setPageSize(40);
		new LoadAppThread(context,mPageBean,RequestParam.Action.GETRECOMMENDLIST).start();
		mPageBean.setPageSize(8);
		new LoadAppThread(context,mPageBean,RequestParam.Action.GETLATESTAPPS).start();
		AppLog.e(TAG, "-----------------loading Home------------------");
		

		// 加载菜单
		String urlStr2=RequestParam.SERVICE_ACTION_URL+RequestParam.Action.GETAPPTYPELIST;
		List<AppsBean> tMenuList = (List<AppsBean>) new NetUtil(context).getNetData(null,urlStr2, null, new MenusParser());
		if (tMenuList != null) {
			for (int i = 0; i < tMenuList.size(); i++) {
						NetUtil.loadImage(tMenuList.get(i).getSerImageUrl(), tMenuList.get(i).getNatImageUrl());
						AppLog.e(TAG, "-----------------loading Menu------------------"+tMenuList.get(1).getNatImageUrl());
			}
			
			//对每个分类再加载第一页数据
			for(int i=0;i<tMenuList.size();i++){
				mPageBean.setPageNo(1);
				mPageBean.setPageSize(16);
 				mPageBean.setTypeId(tMenuList.get(i).getID());
				new LoadAppThread(context,mPageBean,RequestParam.Action.GETAPPLIST).start();
			}
		}
		try{
			sleep(1000*10);
		}catch(Exception e){
		}
		Intent intentSer = new Intent("com.bestv.ott.appstore.service.DownloadService");
		context.startService(intentSer);
		
	}
}
