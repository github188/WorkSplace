package com.ismartv.client;

import android.util.Log;
import com.ismartv.service.ISTVEvent;
import com.ismartv.service.ISTVRequest;

abstract public class ISTVClearBookmarkSignal extends ISTVSignal{
	private static final String TAG="ISTVClearHistorySignal";

	public ISTVClearBookmarkSignal(ISTVClient c){
		super(c);

		client.getEpg().clearBookmark();
		refresh();
	}

	boolean match(ISTVEvent evt){
		if(evt.type==ISTVEvent.Type.CLEAR_BOOKMARK){
			return true;
		}

		return false;
	}

	boolean copyData(ISTVEvent evt){
		return true;
	}

	boolean sendRequest(){
		
		ISTVRequest req = new ISTVRequest(ISTVRequest.Type.CLEAR_BOOKMARK);

		client.sendRequest(req);
		return false;
	}
}

