package com.ismartv.client;

import com.ismartv.service.ISTVEvent;
import com.ismartv.service.ISTVItem;
import com.ismartv.service.ISTVRequest;

abstract public class ISTVItemDetailSignal extends ISTVSignal{
	private int itemPK;
	private ISTVItem item;

	public ISTVItemDetailSignal(ISTVClient c, int pk){
		super(c);
		itemPK = pk;
		refresh();
	}

	public ISTVItem getItem(){
		return item;
	}

	public void setItemPK(int pk){
		if(pk!=itemPK){
			itemPK = pk;
			item = null;
			refresh();
		}
	}

	boolean match(ISTVEvent evt){
		if(evt.type==ISTVEvent.Type.ITEM_DETAIL){
			if(evt.itemPK==itemPK){
				return true;
			}
		}

		return false;
	}

	boolean copyData(ISTVEvent evt){
		item = evt.item;
		return true;
	}

	boolean sendRequest(){
		ISTVRequest req = new ISTVRequest(ISTVRequest.Type.ITEM_DETAIL);

		req.itemPK = itemPK;
		client.sendRequest(req);

		return true;
	}
}

