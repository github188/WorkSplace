package com.ismartv.client;

import java.util.Collection;
import com.ismartv.service.ISTVEvent;
import com.ismartv.service.ISTVChannel;
import com.ismartv.service.ISTVRequest;

abstract public class ISTVChannelListSignal extends ISTVSignal{
	private Collection<ISTVChannel> channels;

	public ISTVChannelListSignal(ISTVClient c){
		super(c);
		refresh();
	}

	public Collection<ISTVChannel> getChannelList(){
		return channels;
	}

	boolean match(ISTVEvent evt){
		if(evt.type==ISTVEvent.Type.CHANNEL_LIST){
			return true;
		}

		return false;
	}

	boolean copyData(ISTVEvent evt){
		channels = evt.channels;
		return true;
	}

	boolean sendRequest(){
		ISTVRequest req = new ISTVRequest(ISTVRequest.Type.CHANNEL_LIST);

		client.sendRequest(req);

		return true;
	}
}

