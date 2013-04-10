package com.ismartv.client;

import com.ismartv.service.ISTVEvent;
import com.ismartv.service.ISTVRequest;
import android.util.Log;

abstract public class ISTVRateSignal extends ISTVSignal{
	private static final String TAG = "ISTVRateSignal";
	private int itemPK;
	private int value;
	private boolean isSuccess = false;

	public ISTVRateSignal(ISTVClient c, int pk, int v){
		super(c);
		itemPK = pk;
		value = v;

		if(v < 0){
			return;
		}

		refresh();
	}

	public boolean getSuccess() {
		return isSuccess;
	}
	
	public void rate(int v){
		if(v>=0){
			value = v;
			refresh();
		}
	}

	boolean match(ISTVEvent evt){
		Log.d(TAG, "evt.type="+evt.type+",evt.itemPk="+evt.itemPK+",itempk="+itemPK);
		if((evt.type==ISTVEvent.Type.RATE) && (evt.itemPK==itemPK)){
			return true;
		}

		return false;
	}

	boolean copyData(ISTVEvent evt){
		if(evt.type == ISTVEvent.Type.ERROR) {
			isSuccess = false;
		} else {
			isSuccess = true;
		}
		Log.d(TAG, "copyData isSuccess="+isSuccess);
		return true;
	}

	boolean sendRequest(){
		ISTVRequest req = new ISTVRequest(ISTVRequest.Type.RATE);

		req.itemPK = itemPK;
		req.value  = value;
		client.sendRequest(req);
		return true;
	}
}
