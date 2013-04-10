package com.ismartv.service;

import java.net.URL;
import java.util.HashMap;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.Collection;

public class ISTVRateTask extends ISTVJsonTask{
	private static final String TAG="ISTVRateTask";

	private int itemPK;
	private int value;

	ISTVEvent createEvent(){
		ISTVEvent evt = new ISTVEvent(ISTVEvent.Type.RATE);
		evt.itemPK = itemPK;
		return evt;
	}

	ISTVRateTask(ISTVTaskManager man, int pk, int value){
		super(man, "/api/rate/"+pk+"/", PRIO_DATA);

		this.itemPK  = pk;
		this.value   = value;

		setAccessToken();
		addPostData("value="+value);

		setJsonMode(false);

		start();
	}

	boolean onGotResponse(String str){
		Log.d(TAG, "onGotResponse");
		ISTVEvent evt = createEvent();

		getService().sendEvent(evt);
		return true;
	}

	void onGetDataError(){
		setError(ISTVError.CANNOT_RATE);
	}
}
