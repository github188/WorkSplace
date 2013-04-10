package com.ismartv.service;

import java.net.URL;
import java.util.HashMap;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.Collection;
import java.lang.StringBuilder;
import android.util.Base64;

public class ISTVImportHistoryTask extends ISTVJsonTask{
	private static final String TAG="ISTVImportHistoryTask";
	private static ISTVImportHistoryTask task;

	private static synchronized boolean setRunningTask(ISTVImportHistoryTask t){
		if(t==null){
			task=null;
			return true;
		}else{
			if(task==null){
				task = t;
				return true;
			}
		}

		return false;
	}

	ISTVEvent createEvent(){
		ISTVEvent evt = new ISTVEvent(ISTVEvent.Type.IMPORT_HISTORY);
		return evt;
	}

	ISTVImportHistoryTask(ISTVTaskManager man){
		super(man, "/api/histories/import/", PRIO_DATA);

		if(getEpg().needLogin())
			return;

		if(setRunningTask(this)){
			Collection<ISTVItem> items = getEpg().getHistoryList();

			if(items.size()!=0){

				addPostData("access_token="+getEpg().getAccessToken());
				setJsonMode(false);

				StringBuilder sb = new StringBuilder();
				int count=0;

				sb.append("[");
				for(ISTVItem item : items){
					if(item!=null){
						if(count!=0){
							sb.append(",");
						}
						sb.append("{");
						sb.append("\"pk\":"+item.pk+",");
						sb.append("\"model_name\":"+(item.isSubItem?"\"subitem\"":"\"item\"")+",");
						sb.append("\"offset\":"+item.offset);
						sb.append("}");
						count++;
					}

					if(count>=50)
						break;
				}
				sb.append("]");

				addPostData("data="+Base64.encodeToString(sb.toString().getBytes(), Base64.NO_WRAP));
				start();
			}
		}
	}

	void onCancel(){
		setRunningTask(null);
	}

	boolean onGotResponse(String str){
		ISTVEvent evt = createEvent();
		getService().sendEvent(evt);
		getEpg().setImportHistoryList();
		return true;
	}

	void onGetDataError(){
		setError(ISTVError.CANNOT_IMPORT_HISTORY);
	}
}
