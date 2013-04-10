package com.ismartv.service;

import java.net.URL;
import java.util.HashMap;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.Collection;
import java.lang.StringBuilder;

public class ISTVRemoveBookmarkTask extends ISTVJsonTask{
	private static final String TAG="ISTVRemoveBookmarkTask";

	private int itemPK;

	ISTVEvent createEvent(){
		ISTVEvent evt = new ISTVEvent(ISTVEvent.Type.REMOVE_BOOKMARK);
		evt.itemPK = itemPK;
		return evt;
	}

	ISTVRemoveBookmarkTask(ISTVTaskManager man, int pk){
		super(man, "/api/bookmark/remove/", PRIO_DATA);

		this.itemPK = pk;

		if(getEpg().needLogin())
			return;

		setJsonMode(false);
		addPostData("access_token="+getEpg().getAccessToken());

		String str;

		str = "item="+itemPK;

		addPostData(str);
		start();
	}

	boolean onGotResponse(String str){		
		ISTVEvent evt = createEvent();
		getService().sendEvent(evt);
		return true;
	}

	void onGetDataError(){
		setError(ISTVError.CANNOT_REMOVE_BOOKMARK);
	}
}
