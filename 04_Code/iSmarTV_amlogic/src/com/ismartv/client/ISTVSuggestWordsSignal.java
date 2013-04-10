package com.ismartv.client;

import java.util.Collection;
import com.ismartv.service.ISTVEvent;
import com.ismartv.service.ISTVRequest;

abstract public class ISTVSuggestWordsSignal extends ISTVSignal{
	private String word;
	private Collection<String> suggests;

	public ISTVSuggestWordsSignal(ISTVClient c, String s){
		super(c);
		word = s;
		refresh();
	}

	public Collection<String> getSuggestWords(){
		return suggests;
	}

	public void setWord(String w){
		if(w!=word){
			word = w;
			refresh();
		}
	}

	boolean match(ISTVEvent evt){
		if(evt.type==ISTVEvent.Type.SUGGEST_WORDS){
			if(evt.word==word){
				return true;
			}
		}

		return false;
	}

	boolean copyData(ISTVEvent evt){
		suggests = evt.words;
		return true;
	}


	boolean sendRequest(){
		ISTVRequest req = new ISTVRequest(ISTVRequest.Type.SUGGEST_WORDS);

		req.word = word;
		client.sendRequest(req);
		return true;
	}
}

