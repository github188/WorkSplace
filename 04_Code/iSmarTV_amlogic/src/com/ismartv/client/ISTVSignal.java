package com.ismartv.client;

import com.ismartv.service.ISTVEvent;

abstract public class ISTVSignal{
	static final int STATUS_IDLE=0;
	static final int STATUS_RUNNING=1;
	static final int STATUS_END=2;
	static final int STATUS_ERROR=3;
	static final int STATUS_CHECK_END=4;

	int status=STATUS_IDLE;
	protected ISTVClient client;
	private int id;

	public ISTVSignal(ISTVClient c, int id){
		this.id = id;
		this.client = c;
		client.addSignal(this);
	}

	public ISTVSignal(ISTVClient c){
		this.id = 0;
		this.client = c;
		client.addSignal(this);
	}

	public void dispose(){
		client.removeSignal(this);
	}

	public void setID(int id){
		this.id = id;
	}

	public int getID(){
		return id;
	}
	
	boolean isEnd(){
		return true;
	}

	void setStatus(int s){
		status = s;
	}

	public void dataExist(){
		if(client.registered()){
			if(isEnd()){
				setStatus(ISTVSignal.STATUS_END);
			}
			onSignal();
		}else{
			setStatus(STATUS_CHECK_END);
		}
	}

	public void refresh(){
		if(sendRequest()){
			setStatus(STATUS_RUNNING);
		}else{
			dataExist();
		}
	}

	boolean checkError(ISTVEvent evt){
		return false;
	}

	abstract boolean match(ISTVEvent evt);

	abstract boolean copyData(ISTVEvent evt);

	abstract public void onSignal();

	abstract boolean sendRequest();
}

