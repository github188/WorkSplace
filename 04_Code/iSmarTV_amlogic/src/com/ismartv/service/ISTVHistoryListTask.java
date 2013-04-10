package com.ismartv.service;

import java.net.URL;
import java.util.HashMap;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.Collection;

public class ISTVHistoryListTask extends ISTVJsonTask{
	private static final String TAG="ISTVHistoryListTask";
	private static ISTVHistoryListTask task;

	private static synchronized boolean setRunningTask(ISTVHistoryListTask t){
		if(t==null){
			task=null;
			return true;
		}else{
			if(task==null){
				task = t;
				return true;
			}
		}

		return false;
	}

	ISTVEvent createEvent(){
		ISTVEvent evt = new ISTVEvent(ISTVEvent.Type.HISTORY_LIST);
		return evt;
	}

	ISTVHistoryListTask(ISTVTaskManager man){
		super(man, "/api/histories/", PRIO_DATA);
		
		ISTVEvent evt = createEvent();
		getService().sendEvent(evt);
//注掉不从服务器上获取数据
//		if(getEpg().hasGotServerHistoryList())
//			return;
//
//		if(getEpg().needLogin())
//			return;
//
//		if(setRunningTask(this)){
//			setAccessToken();
//			setArrayMode(true);
//			start();
//		}
	}

	void onCancel(){
		setRunningTask(null);
	}

	boolean onGotJson(JSONArray array) throws Exception{
		ISTVEvent evt = createEvent();
		ArrayList<ISTVItem> items = new ArrayList<ISTVItem>();
		int len = array.length();
		int i;

		Log.d(TAG, "get "+len+" history items from server");
		for(i=0; i<len; i++){
			JSONObject obj = array.getJSONObject(i);
			ISTVItem item = new ISTVItem(getEpg(), obj);
			item.dump();
			items.add(item);
		}

//		getEpg().setHistoryListFromServer(items);//注掉

		getService().sendEvent(evt);
		return true;
	}

	void onGetDataError(){
		setError(ISTVError.CANNOT_GET_HISTORY_LIST);
	}
}
