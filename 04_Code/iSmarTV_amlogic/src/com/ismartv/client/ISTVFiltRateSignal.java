package com.ismartv.client;

import java.util.Collection;
import com.ismartv.service.ISTVItem;
import com.ismartv.service.ISTVEvent;
import com.ismartv.service.ISTVRequest;

abstract public class ISTVFiltRateSignal extends ISTVSignal{
	private String cm;
	private String attr;
	private int attrID;
	private Collection<ISTVItem> items;

	public ISTVFiltRateSignal(ISTVClient c, String cm, String attr, int id){
		super(c);
		this.cm   = cm;
		this.attr = attr;
		this.attrID = id;
		refresh();
	}

	public Collection<ISTVItem> getItemList(){
		return items;
	}

	boolean match(ISTVEvent evt){
		if(evt.type==ISTVEvent.Type.FILT_RATE){
			if(cm==evt.contentModel && attr==evt.attribute && attrID==evt.attrID){
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
		ISTVRequest req = new ISTVRequest(ISTVRequest.Type.FILT_RATE);

		req.contentModel = cm;
		req.attribute    = attr;
		req.attrID       = attrID;
		
		client.sendRequest(req);

		return true;
	}
}
