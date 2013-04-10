package com.ismartv.client;

import java.util.Collection;
import com.ismartv.service.ISTVEvent;
import com.ismartv.service.ISTVItem;
import com.ismartv.service.ISTVRequest;

abstract public class ISTVRelateListSignal extends ISTVSignal{
	private int itemPK;
	private Collection<ISTVItem> items;

	public ISTVRelateListSignal(ISTVClient c, int pk){
		super(c);
		itemPK = pk;
		refresh();
	}

	public Collection<ISTVItem> getItemList(){
		return items;
	}

	public void setItemPK(int pk){
		if(pk!=itemPK){
			itemPK = pk;
			items = null;
			refresh();
		}
	}

	boolean match(ISTVEvent evt){
		if(evt.type==ISTVEvent.Type.RELATE_LIST){
			if(evt.itemPK==itemPK){
				return true;
			}
		}

		return false;
	}

	boolean copyData(ISTVEvent evt){
		items = evt.items;
		return true;
	}

	boolean sendRequest(){
		ISTVRequest req = new ISTVRequest(ISTVRequest.Type.RELATE_LIST);

		req.itemPK = itemPK;
		client.sendRequest(req);
		return true;
	}
}

