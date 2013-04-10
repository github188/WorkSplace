package com.ismartv.client;

import com.ismartv.service.ISTVEvent;
import com.ismartv.service.ISTVRequest;

abstract public class ISTVBookmarkSignal extends ISTVSignal{
	private int itemPK;
	private boolean add;
	private boolean isSuccess = false;

	public ISTVBookmarkSignal(ISTVClient c){
		super(c);
	}

	public boolean isAdd() {
		return add;
	}
	
	public boolean getSuccess() {
		return isSuccess;
	}
	
	public void addBookmark(int pk){
		if(!client.getEpg().getBookmarkFlag(pk)){
			itemPK = pk;
			add = true;

			client.getEpg().addBookmark(pk);
			refresh();
		}
	}

	public void removeBookmark(int pk){
		if(client.getEpg().getBookmarkFlag(pk)){
			itemPK = pk;
			add = false;

			client.getEpg().removeBookmark(pk);
			refresh();
		}
	}

	boolean match(ISTVEvent evt){
		if(add?(evt.type==ISTVEvent.Type.ADD_BOOKMARK):(evt.type==ISTVEvent.Type.REMOVE_BOOKMARK)){
			if(evt.itemPK==itemPK){
				return true;
			}
		} else {
			if(evt.itemPK==itemPK){
				return true;
			}
		}

		return false;
	}

	boolean copyData(ISTVEvent evt){
		if(evt.type == ISTVEvent.Type.ERROR) {
			isSuccess = false;
		} else {
			isSuccess = true;
		}
		return true;
	}

	boolean sendRequest(){
		ISTVRequest req = new ISTVRequest(add?ISTVRequest.Type.ADD_BOOKMARK:ISTVRequest.Type.REMOVE_BOOKMARK);

		req.itemPK    = itemPK;
		client.sendRequest(req);

		return true;
	}
	
}

