package com.ismartv.service;

import java.net.URL;
import android.util.Log;
import org.json.JSONObject;

public class ISTVActiveTask extends ISTVJsonTask{
	private static final String TAG="ISTVActiveTask";

	ISTVActiveTask(ISTVTaskManager man){
		super(man, "/trust/active/", PRIO_ACTIVE, 3);

//		addPostData("sn="+getMACAddress());
		addPostData("manufacture=Joysee");
		addPostData("kind=A11C");
		addPostData("version="+getDevVersion());

		start();
	}
	
	boolean onGotJson(JSONObject obj) throws Exception{
		String domain = obj.getString("domain");
		setServer(domain);

		Log.d(TAG, "domain: "+domain);
		return true;
	}

	void onGetDataError(){
		setError(ISTVError.CANNOT_ACTIVE);
	}

}

