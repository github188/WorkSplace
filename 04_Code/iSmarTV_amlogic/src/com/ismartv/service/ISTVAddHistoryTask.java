package com.ismartv.service;

import java.net.URL;
import java.util.HashMap;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.Collection;
import java.lang.StringBuilder;

public class ISTVAddHistoryTask extends ISTVJsonTask{
	private static final String TAG="ISTVAddHistoryTask";

	private int itemPK;
	private int subItemPK;
	private int offset;

	ISTVEvent createEvent(){
		ISTVEvent evt = new ISTVEvent(ISTVEvent.Type.ADD_HISTORY);
		evt.itemPK = itemPK;
		evt.subItemPK = subItemPK;
		return evt;
	}

	ISTVAddHistoryTask(ISTVTaskManager man, int pk, int sub, int offset){
		super(man, "/api/histories/create/", PRIO_DATA);

		this.itemPK = pk;
		this.subItemPK = sub;
		this.offset = offset;

		if(getEpg().needLogin())
			return;

		addPostData("access_token="+getEpg().getAccessToken());

		if(subItemPK==-1){
			addPostData("item="+itemPK);
		}else{
			addPostData("subitem="+subItemPK);
		}

		addPostData("offset="+offset);
		start();
	}

	boolean onGotJson(JSONObject obj) throws Exception{
		ISTVEvent evt = createEvent();
		getService().sendEvent(evt);
		return true;
	}

	void onGetDataError(){
		setError(ISTVError.CANNOT_ADD_HISTORY);
	}
}
