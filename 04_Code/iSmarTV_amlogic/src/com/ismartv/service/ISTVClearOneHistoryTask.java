package com.ismartv.service;

import java.net.URL;
import java.util.HashMap;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.Collection;
import java.lang.StringBuilder;

public class ISTVClearOneHistoryTask extends ISTVJsonTask{
	
	private static final String TAG="ISTVClearOneHistoryTask";
	private int itemPk;
	
	ISTVEvent createEvent(){
		ISTVEvent evt = new ISTVEvent(ISTVEvent.Type.REMOVE_HISTORY);
		evt.itemPK = itemPk;
		return evt;
	}

	ISTVClearOneHistoryTask(ISTVTaskManager man , int pk){
		super(man, "/api/history/remove/", PRIO_DATA);
		if(getEpg().needLogin()){
			return;
		}
		if(pk==-1){
			return;
		}
		itemPk = pk;
		setJsonMode(false);
		
		addPostData("pk="+itemPk);
		addPostData("model_name="+"item");
		addPostData("offset="+0);
		addPostData("access_token="+getEpg().getAccessToken());
		start();
	}

	boolean onGotResponse(String str){
		ISTVEvent evt = createEvent();
		getService().sendEvent(evt);
		return true;
	}

	void onGetDataError(){
		setError(ISTVError.CANNOT_CLEAR_HISTORY);
	}
}
