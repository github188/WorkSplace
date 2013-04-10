package com.ismartv.client;

import com.ismartv.service.ISTVEvent;
import com.ismartv.service.ISTVTopVideo;
import com.ismartv.service.ISTVRequest;

abstract public class ISTVTopVideoSignal extends ISTVSignal{
	private ISTVTopVideo topVideo;

	public ISTVTopVideoSignal(ISTVClient c){
		super(c);
		refresh();
	}

	public ISTVTopVideo getTopVideo(){
		return topVideo;
	}

	boolean match(ISTVEvent evt){
		if(evt.type==ISTVEvent.Type.TOP_VIDEO){
			return true;
		}

		return false;
	}

	boolean copyData(ISTVEvent evt){
		topVideo = evt.topVideo;
		return true;
	}

	boolean sendRequest(){
		ISTVRequest req = new ISTVRequest(ISTVRequest.Type.TOP_VIDEO);

		client.sendRequest(req);
		return true;
	}
}
