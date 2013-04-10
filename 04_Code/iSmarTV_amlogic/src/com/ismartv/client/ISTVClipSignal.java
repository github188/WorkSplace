package com.ismartv.client;

import com.ismartv.service.ISTVEvent;
import com.ismartv.service.ISTVClip;
import com.ismartv.service.ISTVRequest;

abstract public class ISTVClipSignal extends ISTVSignal{
	private int clipID;
	private ISTVClip clip;

	public ISTVClipSignal(ISTVClient c, int pk){
		super(c);
		clipID = pk;

		refresh();
	}

	public ISTVClip getClip(){
		return clip;
	}

	public void setClipPK(int pk){
		if(clipID!=pk){
			clipID = pk;
			clip = null;
			refresh();
		}
	}

	boolean match(ISTVEvent evt){
		if(evt.type==ISTVEvent.Type.CLIP){
			if(evt.clipPK==clipID){
				return true;
			}
		}

		return false;
	}

	boolean copyData(ISTVEvent evt){
		clip = evt.clip;
		return true;
	}

	boolean sendRequest(){
		ISTVRequest req = new ISTVRequest(ISTVRequest.Type.CLIP);

		req.clipPK = clipID;
		client.sendRequest(req);
		return true;
	}
}

