package com.ismartv.service;

import java.net.URL;
import java.util.HashMap;
import android.util.Log;
import android.content.Context;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.Collection;
import android.util.Base64;
import com.lenovo.tvcustom.lenovo.LenovoService;

public class ISTVLoginTask extends ISTVJsonTask{
	private static final String TAG="ISTVLoginTask";
	private static String clientID="a97756bc4038b26847093078a965e3";
	private static String clientSecret="17aec1b876a1c440ce3ced3786ccf3";

	private String user;
	private String passwd;
	private Context ctxt;

	ISTVEvent createEvent(){
		ISTVEvent evt = new ISTVEvent(ISTVEvent.Type.LOGIN_FAILED);
		return evt;
	}

	ISTVLoginTask(ISTVTaskManager man, Context ctxt, String user, String passwd){
		super(man, "/oauth2/token?grant_type=client_credentials"+
				"&client_id="+clientID+
				"&client_secret="+clientSecret+
				"&scope=",
				PRIO_ACCESS);
		/*super(man, "/oauth2/token", PRIO_ACCESS);
		addPostData("grant_type=client_credentials");
		addPostData("client_id="+clientID);
		addPostData("client_secret="+clientSecret);
		addPostData("scope=");*/

		this.ctxt   = ctxt;
		this.user   = user;
		this.passwd = passwd;

		boolean ok=true;

		if(passwd!=null){
			String ret = LenovoService.loginUser(ctxt, user, passwd);
			Log.d(TAG, "lenovo login: "+ret);

			if(ret.equals("USS-0100")||ret.equals("USS-0101")||ret.equals("USS-0103")||ret.equals("USS-0105")||ret.equals("USS-0111")
					||ret.equals("USS-0151")||ret.equals("USS-0-1")||ret.equals("USS-0403")||ret.equals("USS-0407")){
				ok = false;
				onGetDataError(ret);
			}

		}

		if(ok){
			LenovoService.getStData(ctxt, "vod.tvlenovo.com", 
				new LenovoService.OnAuthenListener() {
					public void onFinished(boolean l, String lang){
						Log.d(TAG, "lenovo get stdata " + l + " " + lang);
						if(!l){
							onGetDataError();
						}else{
							login(lang);
						}
					}
				}
			);
		}
	}

	void login(String secret){
		String str, authStr, vStr;

		str = clientID+":"+clientSecret;
		authStr = Base64.encodeToString(str.getBytes(), Base64.NO_WRAP);

		str = user+":"+secret;
		vStr = Base64.encodeToString(str.getBytes(), Base64.NO_WRAP);

		setAuthorzation("basic "+authStr);
		setVerification(vStr);

		Log.d(TAG, "send "+user+" "+secret);

		start();
	}

	boolean onGotJson(int code, JSONObject obj) throws Exception{
		ISTVEvent evt = createEvent();

		if(code < 300){
			String token, expire;

			evt.type = ISTVEvent.Type.LOGIN_SUCCESS;
			token  = obj.getString("access_token");
			expire = obj.getString("expire_in");
			getEpg().setAccessToken(token);
			Log.d(TAG, "login token: "+token+" expire: "+expire);
		}else{
			String err, msg;

			evt.type = ISTVEvent.Type.LOGIN_FAILED;
			err = obj.getString("error");
			msg = obj.getString("error_description");

			Log.d(TAG, "login failed "+error+" msg: "+msg);
		}
		
		getService().sendEvent(evt);
		return true;
	}

	void onGetDataError(){
		ISTVEvent evt = createEvent();
		getService().sendEvent(evt);
	}

	void onGetDataError(String info){
		ISTVEvent evt = createEvent();
		evt.errInfo = info;
		getService().sendEvent(evt);
	}
}
