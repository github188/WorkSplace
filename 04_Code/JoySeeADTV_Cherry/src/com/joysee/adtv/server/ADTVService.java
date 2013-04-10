package com.joysee.adtv.server;

import android.util.Log;

public class ADTVService{
	private static String TAG="ADTVService";
	private static ADTVService service;
	private ADTVEpg         epg;
	private ADTVBitmapCache cache;
	private ADTVTaskManager taskMan;


	public static synchronized ADTVService getService(){
		if(service==null){
			Log.d(TAG, "create service");
			service = new ADTVService();
		}
		return service;
	}
	
	public synchronized void setNullService(int ty){
		Log.d(TAG, "*********************setNullService");
		if(epg!=null)
			epg     = null;
		if(taskMan!=null)
			taskMan = null;
		if(cache!=null)
			cache   = null;
		service=null;
	}

	private ADTVService(){
		epg     = new ADTVEpg();
		taskMan = new ADTVTaskManager(this);
		cache   = new ADTVBitmapCache();

	}

	public ADTVTaskManager getTaskManager(){
		return taskMan;
	}

	public ADTVEpg getEpg(){
		return epg;
	}

	public ADTVBitmapCache getBitmapCache(){
		return cache;
	}
}

