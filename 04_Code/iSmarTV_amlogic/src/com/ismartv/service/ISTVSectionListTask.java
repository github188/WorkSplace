package com.ismartv.service;

import java.net.URL;
import java.util.HashMap;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.Collection;

public class ISTVSectionListTask extends ISTVJsonTask{
	private static final String TAG="ISTVsectionListTask";
	private static HashMap<String,ISTVSectionListTask> taskMap=new HashMap<String,ISTVSectionListTask>();

	private String chanID;

	private static synchronized boolean setRunningTask(String chanID, ISTVSectionListTask t){
		if(t==null){
			taskMap.remove(chanID);
			return true;
		}else{
			ISTVSectionListTask old;

			old = taskMap.get(chanID);
			if(old==null){
				taskMap.put(chanID, t);
				return true;
			}
		}

		return false;
	}

	ISTVEvent createEvent(){
		ISTVEvent evt = new ISTVEvent(ISTVEvent.Type.SECTION_LIST);
		evt.chanID = chanID;
		return evt;
	}

	ISTVSectionListTask(ISTVTaskManager man, String chan){
		super(man, "/api/tv/sections/"+chan+"/", PRIO_DATA);

		setArrayMode(true);

		chanID = chan;
//不用缓存
//		Collection<ISTVSection> secs = getEpg().getSectionList(chanID);
//		if(secs!=null){
//			ISTVEvent evt = createEvent();
//
//			evt.chanID   = chanID;
//			evt.sections = secs;
//
//			getService().sendEvent(evt);
//
//			return;
//		}

//		if(setRunningTask(chanID, this)){
			start();
//		}
	}

	void onCancel(){
		setRunningTask(chanID, null);
	}

	boolean onGotJson(JSONArray array) throws Exception{
		ISTVEvent evt = createEvent();

		ArrayList<ISTVSection> secs = new ArrayList<ISTVSection>();
		int length = array.length();

		for(int i=0; i<length; i++){
			JSONObject obj = array.getJSONObject(i);

			ISTVSection sec = new ISTVSection(obj);
			secs.add(sec);
		}

		getEpg().addSectionList(chanID, secs);
		evt.sections = secs;
		getService().sendEvent(evt);
		return true;
	}

	void onGetDataError(){
		setError(ISTVError.CANNOT_GET_SECTION_LIST, chanID);
	}
}

