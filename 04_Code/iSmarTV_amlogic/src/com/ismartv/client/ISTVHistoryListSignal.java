package com.ismartv.client;

import java.util.Collection;
import com.ismartv.service.ISTVEvent;
import com.ismartv.service.ISTVItem;
import com.ismartv.service.ISTVRequest;

abstract public class ISTVHistoryListSignal extends ISTVSignal{
	private Collection<ISTVItem> histories;

	public ISTVHistoryListSignal(ISTVClient c){
		super(c);
		refresh();
	}
	
	public void removeHistory(int pk){
		client.getEpg().removeHistoryPK(pk);
		refresh();
	}

	public Collection<ISTVItem> getHistoryList(){
		return histories;
	}

	boolean match(ISTVEvent evt){
		if(evt.type==ISTVEvent.Type.HISTORY_LIST){
			return true;
		}

		return false;
	}

	boolean copyData(ISTVEvent evt){
		histories = client.getEpg().getHistoryList();
		return true;
	}

	boolean isEnd(){
		return client.getEpg().hasGotServerHistoryList();
	}

	boolean sendRequest(){
		histories = client.getEpg().getHistoryList();

		if(!isEnd()){
			//不请求服务器历史记录
//			ISTVRequest req = new ISTVRequest(ISTVRequest.Type.HISTORY_LIST);
//			client.sendRequest(req);
		}

		return false;
	}
}

