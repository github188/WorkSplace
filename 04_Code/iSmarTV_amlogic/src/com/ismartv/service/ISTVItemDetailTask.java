package com.ismartv.service;

import java.net.URL;
import java.util.HashMap;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.Collection;

public class ISTVItemDetailTask extends ISTVJsonTask{
	private static final String TAG="ISTVItemDetailTask";
	private static HashMap<Integer,ISTVItemDetailTask> taskMap=new HashMap<Integer,ISTVItemDetailTask>();

	private int itemPK;

	private static synchronized boolean setRunningTask(int pk, ISTVItemDetailTask t){
		if(t==null){
			taskMap.remove(pk);
			return true;
		}else{
			ISTVItemDetailTask old = taskMap.get(pk);
			if(old==null){
				taskMap.put(pk, t);
				return true;
			}
		}

		return false;
	}

	ISTVEvent createEvent(){
		ISTVEvent evt = new ISTVEvent(ISTVEvent.Type.ITEM_DETAIL);
		evt.itemPK = itemPK;
		return evt;
	}

	ISTVItemDetailTask(ISTVTaskManager man, int pk){
		super(man, "/api/item/"+pk+"/", PRIO_DATA);

		this.itemPK  = pk;
//注掉,为了能刷新评分,不然从第一次以后都是从缓存中取
//		ISTVEvent evt = createEvent();
//		evt.item   = getEpg().getItem(itemPK);一

//		if((evt.item!=null) && evt.item.hasGotDetail()){
//			getService().sendEvent(evt);
//			return;
//		}

		if(setRunningTask(pk, this)){
			start();
		}
	}

	void onCancel(){
		setRunningTask(itemPK, null);
	}

	boolean onGotJson(JSONObject obj) throws Exception{
		ISTVEvent evt = createEvent();
		ISTVItem item = new ISTVItem(getEpg(), obj);

		//item.dump();

		evt.item   = item;

		getEpg().addItemDetail(item);
		getService().sendEvent(evt);
		return true;
	}

	void onGetDataError(){
		setError(ISTVError.CANNOT_GET_ITEM_DETAIL, ""+itemPK);
	}
}
