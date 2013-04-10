package com.ismartv.client;

import java.util.Collection;
import android.util.Log;
import com.ismartv.service.ISTVEvent;
import com.ismartv.service.ISTVItem;
import com.ismartv.service.ISTVRequest;

abstract public class ISTVVodHomeListSignal extends ISTVSignal{
	private static String TAG="ISTVVodHomeListSignal";
	private Collection<ISTVItem> items;

	public ISTVVodHomeListSignal(ISTVClient c){
		super(c);
		refresh();
	}

	public Collection<ISTVItem> getItemList(){
		return items;
	}

	boolean match(ISTVEvent evt){
		if(evt.type==ISTVEvent.Type.VOD_HOME_LIST){
			return true;
		}

		return false;
	}

	boolean copyData(ISTVEvent evt){
		items = evt.items;
		return true;
	}

	boolean sendRequest(){
		ISTVRequest req = new ISTVRequest(ISTVRequest.Type.VOD_HOME_LIST);

		client.sendRequest(req);
		return true;
	}
}

