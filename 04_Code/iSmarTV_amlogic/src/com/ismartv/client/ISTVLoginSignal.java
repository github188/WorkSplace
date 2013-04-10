package com.ismartv.client;

import java.util.Collection;
import com.ismartv.service.ISTVEvent;
import com.ismartv.service.ISTVRequest;
import android.content.Context;

abstract public class ISTVLoginSignal extends ISTVSignal{
	private String user;
	private String passwd;
	private String errInfo;
	private Context ctxt;
	private boolean success=false;

	public ISTVLoginSignal(ISTVClient c){
		super(c);
	}

	public void login(Context ctxt, String user, String passwd){
		if(user!=null){
			this.user   = user;
			this.passwd = passwd;
			this.ctxt   = ctxt;
			refresh();
		}
	}

	public boolean isOK(){
		return success;
	}

	public String getErrorInfo(){
		return errInfo;
	}

	boolean match(ISTVEvent evt){
		if((evt.type==ISTVEvent.Type.LOGIN_FAILED) ||
				(evt.type==ISTVEvent.Type.LOGIN_SUCCESS)){
			return true;
		}

		return false;
	}

	boolean copyData(ISTVEvent evt){
		if(evt.type==ISTVEvent.Type.LOGIN_FAILED){
			errInfo = evt.errInfo;
			success = false;
		}else if(evt.type==ISTVEvent.Type.LOGIN_SUCCESS){
			errInfo = "SUCCESS";
			success = true;
		}

		return true;
	}


	boolean sendRequest(){
		ISTVRequest req = new ISTVRequest(ISTVRequest.Type.LOGIN);

		req.user      = user;
		req.passwd    = passwd;
		req.context   = ctxt;

		client.sendRequest(req);
		return true;
	}
}

