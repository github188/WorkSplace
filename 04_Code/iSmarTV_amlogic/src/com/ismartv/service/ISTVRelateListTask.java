package com.ismartv.service;

import java.net.URL;
import java.util.HashMap;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.Collection;

public class ISTVRelateListTask extends ISTVJsonTask{
	private static final String TAG="ISTVRelateListTask";
	private static HashMap<Integer,ISTVRelateListTask> taskMap=new HashMap<Integer,ISTVRelateListTask>();

	private int itemPK;

	private static synchronized boolean setRunningTask(int pk, ISTVRelateListTask t){
		if(t==null){
			taskMap.remove(pk);
			return true;
		}else{
			ISTVRelateListTask old = taskMap.get(pk);
			if(old==null){
				taskMap.put(pk, t);
				return true;
			}
		}

		return false;
	}

	ISTVEvent createEvent(){
		ISTVEvent evt = new ISTVEvent(ISTVEvent.Type.RELATE_LIST);
		evt.itemPK = itemPK;
		return evt;
	}

	ISTVRelateListTask(ISTVTaskManager man, int pk){
		super(man, "/api/tv/relate/"+pk+"/", PRIO_DATA);

		setArrayMode(true);

		this.itemPK  = pk;

		if(setRunningTask(pk, this)){
			start();
		}
	}

	void onCancel(){
		setRunningTask(itemPK, null);
	}

	boolean onGotJson(JSONArray array) throws Exception{
		ISTVEvent evt = createEvent();
		ArrayList<ISTVItem> items = new ArrayList<ISTVItem>();
		int cnt = array.length();

		for(int i=0; i<cnt; i++){
			JSONObject obj = array.getJSONObject(i);
			ISTVItem item = new ISTVItem(getEpg(), obj);
			items.add(item);

			//item.dump();
		}

		evt.items  = items;

		getEpg().addItemList(items);
		getService().sendEvent(evt);
		return true;
	}

	void onGetDataError(){
		setError(ISTVError.CANNOT_GET_RELATE_LIST, ""+itemPK);
	}
}
