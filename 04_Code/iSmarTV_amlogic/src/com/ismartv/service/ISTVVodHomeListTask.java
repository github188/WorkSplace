package com.ismartv.service;

import java.net.URL;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.Collection;

public class ISTVVodHomeListTask extends ISTVJsonTask{
	private static final String TAG="ISTVVodHomeListTask";
	private static ISTVVodHomeListTask task;

	private static synchronized boolean setRunningTask(ISTVVodHomeListTask t){
		if(t==null){
			task = null;
			return true;
		}else if(task==null){
			task = t;
			return true;
		}else if(task!=null){
			task=t;
			return true;
		}

		return false;
	}

	ISTVEvent createEvent(){
		ISTVEvent evt = new ISTVEvent(ISTVEvent.Type.VOD_HOME_LIST);
		return evt;
	}

	ISTVVodHomeListTask(ISTVTaskManager man){
		super(man, "/api/tv/section/tvhome/", PRIO_DATA);
//不用缓存
//		Collection<ISTVItem> items=getEpg().getVodHomeList();
//		if(items!=null){
//			ISTVEvent evt = createEvent();
//			evt.items = items;
//
//			getService().sendEvent(evt);
//			return;
//		}

//		if(setRunningTask(this)){
			start();
//		}
	}

	void onCancel(){
		setRunningTask(null);
	}

	boolean onGotJson(JSONObject obj) throws Exception{
		int cnt = obj.getInt("count");
		JSONArray array = obj.getJSONArray("objects");
		int length = array.length();

		ISTVEvent evt = createEvent();

		evt.items = new ArrayList<ISTVItem>();

		for(int i=0; i<length; i++){
			JSONObject iobj = array.getJSONObject(i);
			ISTVItem item = new ISTVItem(getEpg(), iobj);

			//item.dump();

			evt.items.add(item);
		}

		getEpg().setVodHomeList(evt.items);
		getService().sendEvent(evt);

		return true;
	}

	void onGetDataError(){
		setError(ISTVError.CANNOT_GET_VOD_HOME);
	}
}
