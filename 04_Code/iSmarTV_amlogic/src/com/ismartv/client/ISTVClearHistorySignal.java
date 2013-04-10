package com.ismartv.client;

import android.util.Log;
import com.ismartv.service.ISTVEvent;
import com.ismartv.service.ISTVRequest;

abstract public class ISTVClearHistorySignal extends ISTVSignal{
	private static final String TAG="ISTVClearHistorySignal";

	public ISTVClearHistorySignal(ISTVClient c){
		super(c);

		client.getEpg().clearHistory();
		client.getEpg().storeHistory();

		refresh();
	}

	boolean match(ISTVEvent evt){
		if(evt.type==ISTVEvent.Type.CLEAR_HISTORY){
			return true;
		}

		return false;
	}

	boolean copyData(ISTVEvent evt){
		return true;
	}

	boolean sendRequest(){
		ISTVRequest req = new ISTVRequest(ISTVRequest.Type.CLEAR_HISTORY);
		client.sendRequest(req);
		return false;
	}
}

