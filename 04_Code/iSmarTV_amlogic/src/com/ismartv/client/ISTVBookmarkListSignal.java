package com.ismartv.client;

import java.util.Collection;
import com.ismartv.service.ISTVEvent;
import com.ismartv.service.ISTVItem;
import com.ismartv.service.ISTVRequest;

abstract public class ISTVBookmarkListSignal extends ISTVSignal{
	private Collection<ISTVItem> bookmarks;

	public ISTVBookmarkListSignal(ISTVClient c){
		super(c);
		refresh();
	}

	public Collection<ISTVItem> getBookmarkList(){
		return bookmarks;
	}

	boolean match(ISTVEvent evt){
		if(evt.type==ISTVEvent.Type.BOOKMARK_LIST){
			bookmarks = client.getEpg().getBookmarkList();
			return true;
		}

		return false;
	}

	boolean copyData(ISTVEvent evt){
		bookmarks = evt.items;
		return true;
	}

	boolean sendRequest(){
		ISTVRequest req = new ISTVRequest(ISTVRequest.Type.BOOKMARK_LIST);
		client.sendRequest(req);

		return true;
	}
}

