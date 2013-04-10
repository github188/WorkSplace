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

public class ISTVSearchTask extends ISTVJsonTask{
	private static final String TAG="ISTVSearchTask";
	private String contentModel;
	private String word;
	private int page;

	ISTVEvent createEvent(){
		ISTVEvent evt = new ISTVEvent(ISTVEvent.Type.SEARCH);
		evt.contentModel = contentModel;
		evt.word = word;
		evt.page  = page;
		return evt;
	}
	
	ISTVSearchTask(ISTVTaskManager man, String contentModel, String word, int page){	
//		super(man, "/api/tv/search/"+contentModel+"/"+ word +"/" +(page+1), PRIO_DATA);
	    super(man, "/api/tv/search/"+ word +"/" +(page+1), PRIO_DATA);
		this.contentModel = contentModel;
		this.word = word;
		this.page  = page;		
		start();

	}

	void onCancel(){

	}

	boolean onGotJson(JSONObject obj) throws Exception{		
		ISTVEvent evt = createEvent();
		Log.d(TAG, "obj="+obj);
		evt.count = obj.getInt("count");
		evt.countPerPage = ISTVSection.countPerPage;
		evt.pageCount    = (evt.count+evt.countPerPage-1)/evt.countPerPage;

		evt.items = new ArrayList<ISTVItem>();

		JSONArray itemArray=obj.getJSONArray("objects");
		int cnt = itemArray.length();
		Log.d(TAG, "itemArray="+itemArray);
		for(int i=0; i<cnt; i++){
			ISTVItem item=new ISTVItem(getEpg(), itemArray.getJSONObject(i));
			evt.items.add(item);			
			//item.dump();
		}
		Log.d(TAG, "onGotJson="+evt.count);
		getEpg().addItemList(evt.items);
		getService().sendEvent(evt);
		return true;
	}

	void onGetDataError(){
		setError(ISTVError.CANNOT_SEARCH);
	}	
}