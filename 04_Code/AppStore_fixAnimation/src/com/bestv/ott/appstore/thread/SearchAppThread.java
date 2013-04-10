package com.bestv.ott.appstore.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bestv.ott.appstore.activity.BaseActivity;
import com.bestv.ott.appstore.common.AppsBean;
import com.bestv.ott.appstore.common.PageBean;
import com.bestv.ott.appstore.utils.AppLog;
import com.bestv.ott.appstore.utils.NetUtil;
import com.bestv.ott.appstore.utils.RequestParam;

import android.content.Context;
import android.os.Process;

public class SearchAppThread extends Thread{
	
	private final static String TAG="com.bestv.ott.appstore.LoadAppThread";
	
	private Context mActivity;
	private PageBean mPageBean;
	private String mAction=RequestParam.Action.GETAPPLIST;
	private boolean flag=true;
	
	public SearchAppThread(Context tActivity,PageBean pageBean,String action){
		mActivity = tActivity;
		mPageBean=pageBean;
		if(null!=action){
			mAction=action;
		}
	}
	public void run(){
		Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
		List<AppsBean> tempList=null;
		String urlStr=RequestParam.SERVICE_ACTION_URL+mAction;
		AppLog.d(TAG, "===========================urlStr="+urlStr);
		
		Map<String,String> tParam=new TreeMap<String,String>();
		if(null!=mPageBean){
			tParam.put(RequestParam.Param.PAGENO,String.valueOf(mPageBean.getPageNo()));
			tParam.put(RequestParam.Param.LINENUMBER, String.valueOf(mPageBean.getPageSize()));
			tParam.put(RequestParam.Param.KEYWORD, mPageBean.getKeyWord());
		}
		if(flag){
			String jsonStr=new NetUtil(mActivity).getAppsByStatus(tParam, urlStr);
			if(null==jsonStr){//网络连接有误
				tempList=null;
			}else{
				try {
					tempList=new ArrayList<AppsBean>();
		            JSONObject allObj=new JSONObject(jsonStr);
		            JSONObject pageObj=allObj.getJSONObject("page");
		            JSONArray listArr=pageObj.getJSONArray("list");
		            Object pageTotal=pageObj.get("totalLine");
		            if(Integer.valueOf(pageTotal.toString())==0){
		            	mPageBean.calculatePageTotal(0);
		            }else{
		            	mPageBean.calculatePageTotal(Integer.valueOf(pageTotal.toString()));
			            JSONObject appObj;
			            for(int i=0;i<listArr.length();i++){
			                appObj=listArr.getJSONObject(i);
			                AppsBean apps=new AppsBean();
			                apps.setID(appObj.getInt("id"));
			                apps.setAppName(appObj.getString("name"));
			                apps.setVersion("("+appObj.getString("version")+")");
			                apps.setTypeName(""+appObj.getString("typeNames").replace("[", "").replace("\"", "").replace("]", ""));
			                String imageSrc=appObj.getString("image");
			                apps.setSerImageUrl(RequestParam.SERVICE_IMAGE_URL+imageSrc);
			                if(appObj.getString("id")!=null&&!appObj.getString("id").equals("")
			                		&&appObj.getString("id")!=null&&!appObj.getString("id").equals("")
			                		&&appObj.getString("name")!=null&&!appObj.getString("name").equals("")){
			                	tempList.add(apps);
			                }
			            }
		            }
		        } catch (JSONException e) {
		            e.printStackTrace();
		        }
			}
		}
		final List<AppsBean> fAppList = tempList;
		if(null!=mActivity&&flag && mActivity instanceof BaseActivity){
			
			((BaseActivity)mActivity).runOnUiThread(new Runnable(){
				@Override
				public void run() {
					((BaseActivity)mActivity).refreshSearchList(fAppList);
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
