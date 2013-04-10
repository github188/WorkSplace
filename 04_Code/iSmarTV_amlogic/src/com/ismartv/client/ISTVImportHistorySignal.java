package com.ismartv.client;

import java.util.Collection;
import com.ismartv.service.ISTVEvent;
import com.ismartv.service.ISTVItem;
import com.ismartv.service.ISTVRequest;

abstract public class ISTVImportHistorySignal extends ISTVSignal{
	public ISTVImportHistorySignal(ISTVClient c){
		super(c);
		refresh();
	}

	boolean match(ISTVEvent evt){
		if(evt.type==ISTVEvent.Type.IMPORT_HISTORY){
			return true;
		}

		return false;
	}

	boolean copyData(ISTVEvent evt){
		return true;
	}

	boolean sendRequest(){
		ISTVRequest req = new ISTVRequest(ISTVRequest.Type.IMPORT_HISTORY);
		client.sendRequest(req);

		return true;
	}
}

