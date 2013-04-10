package com.ismartv.service;

import java.net.URL;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.Collection;

public class ISTVChannelListTask extends ISTVJsonTask{
	private static final String TAG="ISTVChannelListTask";
	private static ISTVChannelListTask task;

	private static synchronized boolean setRunningTask(ISTVChannelListTask t){
		if(t==null){
			task = null;
			return true;
		}else if(task==null){
			task = t;
			return true;
		}else if(task!=null){
			task=t;
			return true;
		}

		return false;
	}

	ISTVEvent createEvent(){
		ISTVEvent evt = new ISTVEvent(ISTVEvent.Type.CHANNEL_LIST);
		return evt;
	}


	ISTVChannelListTask(ISTVTaskManager man){
		super(man, "/api/tv/channels/", PRIO_DATA);

		setArrayMode(true);
//不用缓存
//		Collection<ISTVChannel> chans = getEpg().getChannelList();
//		if(chans!=null){
//			ISTVEvent evt = createEvent();
//
//			evt.channels = chans;
//
//			getService().sendEvent(evt);
//
//			return;
//		}

//		if(setRunningTask(this)){
			start();
//		}
	}

	void onCancel(){
		setRunningTask(null);
	}

	boolean onGotJson(JSONArray array) throws Exception{
		ISTVEvent evt = createEvent();
		ArrayList<ISTVChannel> chans = new ArrayList<ISTVChannel>();
		int length = array.length();

		for(int i=0; i<length; i++){
			JSONObject obj = array.getJSONObject(i);

			ISTVChannel chan = new ISTVChannel(obj);
			chans.add(chan);
		}

		getEpg().setChannelList(chans);
		evt.channels = chans;
		getService().sendEvent(evt);
		return true;
	}

	void onGetDataError(){
		setError(ISTVError.CANNOT_GET_CHANNEL_LIST);
	}
}

