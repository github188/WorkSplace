package com.joysee.adtv.doc;

import com.joysee.adtv.server.ADTVCallback;
import com.joysee.adtv.server.ADTVService;

public class ADTVDoc{
	private static final String TAG="ADTVDoc";

	private ADTVCallback cb;
	private ADTVService service;

	public ADTVDoc(){
	    service=ADTVService.getService();
	}

	public ADTVService getService(){
	    return service;
	}

	public void registerCallback(ADTVCallback cb){
		this.cb = cb;
	}


	final protected void onGotResource(ADTVResource res){
		if(cb!=null)
			cb.onGotResource(res);
	}

	final protected void onUpdate(){
		if(cb!=null)
			cb.onUpdate();
	}
}
