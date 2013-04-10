package com.ismartv.service;

import java.net.URL;
import java.util.HashMap;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.Collection;
import java.lang.StringBuilder;

public class ISTVClearHistoryTask extends ISTVJsonTask{
	private static final String TAG="ISTVClearHistoryTask";

	ISTVEvent createEvent(){
		ISTVEvent evt = new ISTVEvent(ISTVEvent.Type.CLEAR_HISTORY);
		return evt;
	}

	ISTVClearHistoryTask(ISTVTaskManager man){
		super(man, "/api/histories/empty/", PRIO_DATA);

		if(getEpg().needLogin())
			return;

		setJsonMode(false);
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
