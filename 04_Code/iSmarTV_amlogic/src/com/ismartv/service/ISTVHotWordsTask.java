package com.ismartv.service;

import java.net.URL;
import java.util.HashMap;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.Collection;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

public class ISTVHotWordsTask extends ISTVJsonTask{
	private static final String TAG="ISTVHotWordsTask";

	ISTVEvent createEvent(){
		ISTVEvent evt = new ISTVEvent(ISTVEvent.Type.HOT_WORDS);		
		return evt;
	}
	
	ISTVHotWordsTask(ISTVTaskManager man){	
		super(man, "/api/tv/hotwords/", PRIO_DATA);
		setArrayMode(true);
		start();
	}

	void onCancel(){

	}

	boolean onGotJson(JSONArray array) throws Exception{
		ISTVEvent evt = createEvent();
		
		evt.count = array.length();
		
		evt.words = new ArrayList<String>();	
		for(int i = 0; i < evt.count; i++) {
			JSONObject object = array.optJSONObject(i);
			if(object != null){
				evt.words.add(object.optString("title"));
			}
		}
		getService().sendEvent(evt);
		return true;
	}

	void onGetDataError(){
		setError(ISTVError.CANNOT_SEARCH_HOTWORDS);
	}	
}