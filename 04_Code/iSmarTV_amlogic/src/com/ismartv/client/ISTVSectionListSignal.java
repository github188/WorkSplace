package com.ismartv.client;

import java.util.Collection;
import com.ismartv.service.ISTVEvent;
import com.ismartv.service.ISTVSection;
import com.ismartv.service.ISTVRequest;
import android.util.Log;

abstract public class ISTVSectionListSignal extends ISTVSignal{
	private static final String TAG="ISTVSectionListSignal";
	private String channel;
	private Collection<ISTVSection> sections;

	public ISTVSectionListSignal(ISTVClient c, String chanID){
		super(c);
		channel = chanID;
		refresh();
	}

	public void setChannel(String chanID){
		if(!channel.equals(chanID)){
			channel = chanID;
			sections = null;
			refresh();
		}
	}

	public Collection<ISTVSection> getSectionList(){
		return sections;
	}

	boolean match(ISTVEvent evt){
		if(evt.type==ISTVEvent.Type.SECTION_LIST){
			if(evt.chanID.equals(channel)){
				return true;
			}
		}

		return false;
	}

	boolean copyData(ISTVEvent evt){
		sections = evt.sections;
		return true;
	}

	boolean sendRequest(){
		ISTVRequest req = new ISTVRequest(ISTVRequest.Type.SECTION_LIST);

		req.chanID = channel;

		client.sendRequest(req);
		return true;
	}
}

