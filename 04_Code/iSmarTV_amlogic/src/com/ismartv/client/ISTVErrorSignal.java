package com.ismartv.client;

import android.util.Log;
import com.ismartv.service.ISTVEvent;
import com.ismartv.service.ISTVRequest;

abstract public class ISTVErrorSignal extends ISTVSignal{
	private static final String TAG="ISTVErrorSignal";
	int error;
	String errInfo;

	public ISTVErrorSignal(ISTVClient c){
		super(c);
		refresh();
	}

	public int getError(){
		return error;
	}

	public String getInfo(){
		return errInfo;
	}

	boolean match(ISTVEvent evt){
		if(evt.type==ISTVEvent.Type.ERROR){
			Log.d(TAG, "receive error event");
			return true;
		}

		return false;
	}

	boolean copyData(ISTVEvent evt){
		error = evt.error;
		errInfo = evt.errInfo;
		return true;
	}

	boolean isEnd(){
		return false;
	}

	boolean sendRequest(){
		return true;
	}
}
