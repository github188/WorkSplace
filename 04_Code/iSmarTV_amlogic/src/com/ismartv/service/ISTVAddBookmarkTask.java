package com.ismartv.service;

import java.net.URL;
import java.util.HashMap;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.Collection;
import java.lang.StringBuilder;

public class ISTVAddBookmarkTask extends ISTVJsonTask{
	private static final String TAG="ISTVAddBookmarkTask";

	private int itemPK;

	ISTVEvent createEvent(){
		ISTVEvent evt = new ISTVEvent(ISTVEvent.Type.ADD_BOOKMARK);
		evt.itemPK = itemPK;
		return evt;
	}

	ISTVAddBookmarkTask(ISTVTaskManager man, int pk){
		super(man, "/api/bookmarks/create/", PRIO_DATA);

		this.itemPK = pk;

		if(getEpg().needLogin())
			return;

		addPostData("access_token="+getEpg().getAccessToken());

		String str;

		str = "item="+itemPK;

		addPostData(str);
		start();
	}

	boolean onGotJson(JSONObject obj) throws Exception{
		ISTVEvent evt = createEvent();
		getService().sendEvent(evt);
		return true;
	}

	void onGetDataError(){
		setError(ISTVError.CANNOT_ADD_BOOKMARK);
	}
}
