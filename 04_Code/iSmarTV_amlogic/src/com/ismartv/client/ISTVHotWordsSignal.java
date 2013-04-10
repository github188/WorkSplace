package com.ismartv.client;

import java.util.Collection;
import com.ismartv.service.ISTVEvent;
import com.ismartv.service.ISTVRequest;

abstract public class ISTVHotWordsSignal extends ISTVSignal{
	private Collection<String> words;

	public ISTVHotWordsSignal(ISTVClient c){
		super(c);
		refresh();
	}

	public Collection<String> getHotWords(){
		return words;
	}

	boolean match(ISTVEvent evt){
		if(evt.type==ISTVEvent.Type.HOT_WORDS){
			words = evt.words;
			return true;
		}

		return false;
	}

	boolean copyData(ISTVEvent evt){
		words = evt.words;
		return true;
	}

	boolean sendRequest(){
		ISTVRequest req = new ISTVRequest(ISTVRequest.Type.HOT_WORDS);
		client.sendRequest(req);

		return true;
	}
}

