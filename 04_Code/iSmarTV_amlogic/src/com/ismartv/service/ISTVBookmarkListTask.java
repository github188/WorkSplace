package com.ismartv.service;

import java.net.URL;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.Collection;

public class ISTVBookmarkListTask extends ISTVJsonTask{
	private static final String TAG="ISTVBookmarkListTask";
	private static ISTVBookmarkListTask task;

	private static synchronized boolean setRunningTask(ISTVBookmarkListTask t){
		if(t==null){
			task = null;
			return true;
		}else if(task==null){
			task = t;
			return true;
		}

		return false;
	}

	ISTVEvent createEvent(){
		ISTVEvent evt = new ISTVEvent(ISTVEvent.Type.BOOKMARK_LIST);
		return evt;
	}

	ISTVBookmarkListTask(ISTVTaskManager man){
		super(man, "/api/bookmarks/", PRIO_DATA);
		
		if(getEpg().needLogin()){
	    	Log.d(TAG, "========getEpg().needLogin()========");
	    	return;
	    }  //去掉说明:不去掉,第一次进收藏,会因为needLogin为flase而反回,而ISTVBookmarkListTask不会走第二次,所以会卡在那
		
		if(getEpg().hasGotBookmarkList()){
			Log.d(TAG, "============getEpg().hasGotBookmarkList()===========");
			Collection<ISTVItem> items = getEpg().getBookmarkList();
			ISTVEvent evt = createEvent();

			evt.items = items;
			getService().sendEvent(evt);
			return;
		}

		if(setRunningTask(this)){
			Log.d(TAG, "========ISTVBookmarkListTask     |     setRunningTask(this)========");
			setAccessToken();
			setArrayMode(true);
			start();
		}
	}

	void onCancel(){
		setRunningTask(null);
	}

	boolean onGotJson(JSONArray array) throws Exception{
		ISTVEvent evt = createEvent();
		int len = array.length();
		int i;
		ArrayList<ISTVItem> items = new ArrayList<ISTVItem>();

		Log.d(TAG, "get "+len+" bookmarks from server");
		for(i=0; i<len; i++){
			JSONObject obj = array.getJSONObject(i);
			ISTVItem item = new ISTVItem(getEpg(), obj);
			item.bookmarked = true;
			item.dump();
			if(item.contentModel==null||item.contentModel.equals("")){
			    Log.d(TAG, "-------name="+item.title+";contentMode="+item.contentModel);
			}
			items.add(item);
		}

		getEpg().setBookmarkList(items);

		evt.items = getEpg().getBookmarkList();
		getService().sendEvent(evt);
		return true;
	}

	void onGetDataError(){
		setError(ISTVError.CANNOT_GET_BOOKMARK_LIST);
	}
}

