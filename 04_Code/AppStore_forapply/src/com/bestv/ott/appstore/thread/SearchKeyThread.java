package com.bestv.ott.appstore.thread;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;

import com.bestv.ott.appstore.common.AppsBean;
import com.bestv.ott.appstore.common.DataOperate;
import com.bestv.ott.appstore.utils.AppLog;
import com.bestv.ott.appstore.utils.RequestParam;

/**
 * 根据关键字请求服务器数据
 */
public class SearchKeyThread extends Thread{
	DataOperate operate;
	Context con;
	List<AppsBean> searchlist;
	
	public SearchKeyThread(DataOperate operate,Context con) {
		this.con = con;
		this.operate = operate;
		searchlist = new ArrayList<AppsBean>();//服务器返回的搜索结果
	}
	public void run() {
		super.run();
		searchlist = operate.getDataFormService(RequestParam.ActionIndex.GETSEARCHLIST);
		AppLog.d("---------SearchFromService", "------------------searchlist.size : "+searchlist.size());
	}
	
	public List<AppsBean> getSearch(){
		return searchlist;
	}
}
