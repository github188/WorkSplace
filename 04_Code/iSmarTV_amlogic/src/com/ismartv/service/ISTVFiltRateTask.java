package com.ismartv.service;

import java.net.URL;
import java.util.HashMap;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.Collection;

public class ISTVFiltRateTask extends ISTVJsonTask{
	private static final String TAG="ISTVFiltRateTask";

	private String contentModel;
	private String attribute;
	private int attrID;

	ISTVFiltRateTask(ISTVTaskManager man, String cm, String attr, int id){
		super(man, "/api/tv/filtrate/$"+cm+"/"+attr+"*"+id+"/", PRIO_DATA);

		this.contentModel  = cm;
		this.attribute     = attr;
		this.attrID        = id;

		start();
	}

	ISTVEvent createEvent(){
		ISTVEvent evt = new ISTVEvent(ISTVEvent.Type.FILT_RATE);
		evt.contentModel = contentModel;
		evt.attribute    = attribute;
		evt.attrID       = attrID;
		return evt;
	}

	void onCancel(){
	}

	boolean onGotJson(JSONObject obj) throws Exception{
		ISTVEvent evt = createEvent();
		JSONArray array = obj.getJSONArray("objects");

		ArrayList<ISTVItem> items = new ArrayList<ISTVItem>();

		int cnt = array.length();

		for(int i=0; i<cnt; i++){
			JSONObject iobj = array.getJSONObject(i);
			ISTVItem item = new ISTVItem(getEpg(), iobj);
			items.add(item);

			//item.dump();
		}

		evt.items        = items;

		getEpg().addItemList(items);
		getService().sendEvent(evt);
		return true;
	}

	void onGetDataError(){
		setError(ISTVError.CANNOT_FILT_RATE);
	}
}
