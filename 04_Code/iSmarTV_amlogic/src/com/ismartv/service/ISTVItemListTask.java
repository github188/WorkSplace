package com.ismartv.service;

import java.net.URL;
import java.util.HashMap;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.Collection;

public class ISTVItemListTask extends ISTVJsonTask{
	private static final String TAG="ISTVItemListTask";
	private static HashMap<String,HashMap<Integer,ISTVItemListTask>> taskMap=new HashMap<String,HashMap<Integer,ISTVItemListTask>>();

	private String secID;
	private int    page;

	private static synchronized boolean setRunningTask(String secID, int page, ISTVItemListTask t){
		HashMap<Integer,ISTVItemListTask> secMap;

		if(t==null){
			secMap = taskMap.get(secID);
			if(secMap!=null){
				secMap.remove(page);
			}
			return true;
		}else{
			secMap = taskMap.get(secID);
			if(secMap==null){
				secMap = new HashMap<Integer,ISTVItemListTask>();
				taskMap.put(secID, secMap);
			}

			ISTVItemListTask old = secMap.get(page);
			if(old==null){
				secMap.put(page, t);
				return true;
			}
		}

		return false;
	}

	ISTVEvent createEvent(){
		ISTVEvent evt = new ISTVEvent(ISTVEvent.Type.ITEM_LIST);
		evt.secID = secID;
		evt.page  = page;
		return evt;
	}

	ISTVItemListTask(ISTVTaskManager man, String secID, int page){
		super(man, "/api/tv/section/"+secID+"/"+(page+1)+"/", PRIO_DATA);

		this.secID = secID;
		this.page  = page;
//不用缓存
//		ISTVEvent evt = createEvent();
//		evt.items = getEpg().getItemList(secID, page);
//		if(evt.items!=null){
//			getService().sendEvent(evt);
//			return;
//		}

//		if(setRunningTask(secID, page, this)){
			start();
//		}
	}

	void onCancel(){
		setRunningTask(secID, page, null);
	}

	boolean onGotJson(JSONObject obj) throws Exception{
		ISTVEvent evt = createEvent();

		evt.count = obj.getInt("count");
		evt.countPerPage = ISTVSection.countPerPage;
		evt.pageCount    = (evt.count+evt.countPerPage-1)/evt.countPerPage;

		evt.items = new ArrayList<ISTVItem>();

		JSONArray itemArray=obj.getJSONArray("objects");
		int cnt = itemArray.length();

		for(int i=0; i<cnt; i++){
			ISTVItem item=new ISTVItem(getEpg(), itemArray.getJSONObject(i));
			evt.items.add(item);
			//item.dump();
		}

		getEpg().addItemList(secID, evt.count, page, evt.items);
		getService().sendEvent(evt);
		return true;
	}

	void onGetDataError(){
		setError(ISTVError.CANNOT_GET_ITEM_LIST, secID);
	}
}
