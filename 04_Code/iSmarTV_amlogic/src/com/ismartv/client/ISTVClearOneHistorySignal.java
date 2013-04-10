package com.ismartv.client;

import com.ismartv.service.ISTVEvent;
import com.ismartv.service.ISTVRequest;

abstract public class ISTVClearOneHistorySignal extends ISTVSignal{
	
	private static final String TAG="ISTVClearOneHistorySignal";
	private int itemPk;
	
	public ISTVClearOneHistorySignal(ISTVClient c , int itemPk){
		super(c);
//		client.getEpg().clearHistory();
//		client.getEpg().storeHistory();
		this.itemPk = itemPk;
		refresh();
	}

	boolean match(ISTVEvent evt){
		if(evt.type==ISTVEvent.Type.REMOVE_HISTORY){
			return true;
		}

		return false;
	}

	boolean copyData(ISTVEvent evt){
		return true;
	}

	boolean sendRequest(){
		ISTVRequest req = new ISTVRequest(ISTVRequest.Type.REMOVE_HISTORY);
		req.itemPK = itemPk;
		client.sendRequest(req);
		return false;
	}
}

