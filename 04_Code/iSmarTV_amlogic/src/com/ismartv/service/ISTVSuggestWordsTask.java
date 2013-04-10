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

public class ISTVSuggestWordsTask extends ISTVJsonTask{
	private static final String TAG="ISTVSuggestWordsTask";
	private String word;

	ISTVEvent createEvent(){
		ISTVEvent evt = new ISTVEvent(ISTVEvent.Type.SUGGEST_WORDS);
		evt.word = word;
		return evt;
	}
	
	ISTVSuggestWordsTask(ISTVTaskManager man, String query){	
		super(man, "/api/tv/suggest/" + query + "/", PRIO_DATA);
		Log.d(TAG, "query="+query);
		word = query;
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
			evt.words.add(array.optString(i));			
		}
		getService().sendEvent(evt);
		return true;
	}

	void onGetDataError(){
		setError(ISTVError.CANNOT_SEARCH_SUGGEST);
	}	
}