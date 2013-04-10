package com.bestv.ott.appstore.thread;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.content.Context;
import android.os.Process;

import com.bestv.ott.appstore.activity.BaseActivity;
import com.bestv.ott.appstore.common.AppsBean;
import com.bestv.ott.appstore.common.PageBean;
import com.bestv.ott.appstore.parser.AppsParser;
import com.bestv.ott.appstore.utils.AppLog;
import com.bestv.ott.appstore.utils.NetUtil;
import com.bestv.ott.appstore.utils.RequestParam;

public class LoadAppThread extends Thread{
	
	private final static String TAG="com.bestv.ott.appstore.LoadAppThread";
	
	private Context mActivity;
	private PageBean mPageBean;
	private String mAction=RequestParam.Action.GETRECOMMENDLIST;
	private boolean flag=true;
	
	public LoadAppThread(Context tActivity,PageBean pageBean,String action){
		mActivity = tActivity;
		mPageBean=pageBean;
		if(null!=action){
			mAction=action;
		}
	}
	public void run(){
		Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
//		AppLog.log_D(TAG, "----------LoadAppsThread-----run-------");
		List<AppsBean> tAppList=null;
		String urlStr=RequestParam.SERVICE_ACTION_URL+mAction;
		Map<String,String> tParam=new TreeMap<String,String>();
		if(null!=mPageBean){
			tParam.put(RequestParam.Param.PAGENO,String.valueOf(mPageBean.getPageNo()));
			tParam.put(RequestParam.Param.LINENUMBER, String.valueOf(mPageBean.getPageSize()));
			if(mPageBean.getTypeId()>=0&&mAction.equals(RequestParam.Action.GETAPPLIST)){ //当获取应用列表才加分类ID（最新，排行不加）
				tParam.put(RequestParam.Param.TYPEID,String.valueOf(mPageBean.getTypeId()));
			}
		}
		if(flag){
			Object obj=new NetUtil(mActivity).getNetData(tParam, urlStr, mPageBean, new AppsParser());
			if(null==obj){//网络连接有误
				tAppList=null;
			}else{
				tAppList=(List<AppsBean>)obj;
			}
		}
		if(tAppList!=null){
			for(int i=0;i<tAppList.size();i++){
				if(flag){
					boolean res=NetUtil.loadImage(tAppList.get(i).getSerImageUrl(), tAppList.get(i).getNatImageUrl());
					if(!res){
						tAppList.get(i).setNatImageUrl(null);
					}
				}
			}
		}
		final List<AppsBean> fAppList = tAppList;
		if(null!=mActivity&&flag&& mActivity instanceof BaseActivity){
			((BaseActivity)mActivity).runOnUiThread(new Runnable(){
				@Override
				public void run() {
					((BaseActivity)mActivity).refreshAppsList(fAppList);
				}
				
			});
		}
//		AppLog.log_D(TAG, "----------LoadAppThread-----end-------");
	}
	public boolean isFlag() {
		return flag;
	}
	public void setFlag(boolean flag) {
		this.flag = flag;
	}
	
}
