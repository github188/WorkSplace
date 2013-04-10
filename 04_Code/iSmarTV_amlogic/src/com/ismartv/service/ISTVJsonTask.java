package com.ismartv.service;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.NetworkInterface;
import java.net.HttpURLConnection;
import java.net.UnknownHostException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.StringBuilder;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONArray;

import com.ismartv.util.JniSetting;

import java.net.ConnectException;
import java.util.Properties;

abstract public class ISTVJsonTask extends ISTVTask{
	private static final String TAG="ISTVJsonTask";
	static String entry = "http://cord.tvxio.com";
	static String host = "cord.tvxio.com";
	static String server = entry;
	static String devName ="A11C";
	static String devVersion = "1.0";
	static String mac=null;
	static String accredit="";

	private boolean isArray=false;
	private boolean isJson=true;

	String authorzation;
	String verification;
	String accessToken;
	String postData=null;
	
	/* HttpURLConnection代理 */
	private Proxy proxy = null;

	static String getDevVersion(){
		return devVersion;
	}

	static synchronized String getMACAddress(){
		if(mac==null){
			try{
				byte addr[];
				addr=NetworkInterface.getByName("eth0").getHardwareAddress();
				mac="";
				for(int i=0; i<6; i++){
					mac+=String.format("%02X",addr[i]);
				}
			}catch(Exception e){
				mac = "00112233445566";
			}
		}
//		return "000DFE7453B1";
		return mac;
	}
	
//	/**
//	 * 获取芯片ID,用于取代mac
//	 * @return
//	 */
//	static synchronized String getSTBID(){
//		String snid=JniSetting.getInstance().getSTBID();
//		Log.d(TAG, "------------------------snid="+snid);
//		return snid;
//	}

	static String getUserAgent(){
		return devName+"/"+devVersion+" "+getMACAddress();
	}

	protected synchronized static URL getURL(String path){
		String addr = server + path;
		URL u=null;

		try {
			u = new URL(addr);
		}catch(Exception e){
			Log.d(TAG, "parse URL failed!");
		}

		return u;
	}
	
	protected synchronized static URL getUrlPath(String path){
        URL u=null;
        try {
            u = new URL(path);
        }catch(Exception e){
            Log.d(TAG, "parse URL failed!");
        }

        return u;
    }

	protected synchronized static URL getDeoIdURL(String path){
		String addr = "http://lily.tvxio.com/media/" + path;
		URL u=null;

		try {
			u = new URL(addr);
		}catch(Exception e){
			Log.d(TAG, "parse URL failed!");
		}

		return u;
	}

	protected void setAccessToken(){
		accessToken = getEpg().getAccessToken();
	}

	protected void setAuthorzation(String str){
		authorzation = str;
	}

	protected void setVerification(String str){
		verification = str;
	}

	protected void setPostData(String data){
		postData = data;
	}

	protected void addPostData(String data){
		if(postData==null){
			postData = data;
		}else{
			postData += "&"+data;
		}
	}

	protected synchronized static void setServer(String s){
		Log.d(TAG, "setServer="+s);
		server = "http://"+s;
	}

	
	ISTVJsonTask(ISTVTaskManager man, String path, int prio, int times){
		super(man, getURL(path), prio, times);
	}

	ISTVJsonTask(ISTVTaskManager man, String path, int prio){
		super(man, getURL(path), prio, 1);
	}

	ISTVJsonTask(ISTVTaskManager man, String path, int prio, String type){	
			super(man,getDeoIdURL(path),prio,1);
	}
	
	ISTVJsonTask(ISTVTaskManager man,String path,int prio,int times,String str){
	    super(man, getUrlPath(path), prio, times);
	}

	protected void setArrayMode(boolean a){
		isArray = a;
	}

	protected void setJsonMode(boolean m){
		isJson = m;
	}

	boolean process(){
		HttpURLConnection conn=null;
		InputStream input = null;
		OutputStream output = null;
		boolean ret = false;
		Log.d(TAG, "process="+url);
		try{
			if(accessToken!=null){
				String str = url.toString();
				str += "?access_token="+accessToken;
				url = new URL(str);
			}
			if(prio==PRIO_ACTIVE&&getEpg().getAccredit()!=null&&!getEpg().getAccredit().trim().equals("")&&!getEpg().getAccredit().trim().equals("null")&&!postData.contains("&sn=")){
			    addPostData("sn="+getEpg().getAccredit());
			}else if(prio==PRIO_ACTIVE&&!postData.contains("&sn=")&&(getEpg().getAccredit()==null||getEpg().getAccredit().trim().equals(""))){//目前加上，如果获取不到，用旧的mac地址的方式
			    addPostData("sn="+getMACAddress());
			}

			Log.d(TAG, ">>>>>>>> URL : "+url.toString() + " proxy " + checkProxy());
			if(checkProxy()){
				conn = (HttpURLConnection) url.openConnection();//使用代理访问
				Properties prop = System.getProperties();
				String host=android.net.Proxy.getDefaultHost();
				int port =android.net.Proxy.getDefaultPort();
				Log.d(TAG, " host : " +host +"     |||   port "+ port);
			    prop.put("proxySet", "true");
			    prop.put("proxyHost", host);
			    prop.put("proxyPort", port);
			}else{
				conn = (HttpURLConnection) url.openConnection();
				Properties prop = System.getProperties();
				prop.put("proxySet", "false");
			}
			conn.setUseCaches(false);
			conn.setConnectTimeout(1000*60);
			conn.setReadTimeout(1000*60);

			if(postData!=null)
				conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestProperty("Host", host);
			conn.setRequestProperty("User-Agent", getUserAgent());

			Log.d(TAG, "---------postData="+postData);
			if(postData!=null){
				conn.setRequestProperty("Content-Length", ""+postData.length());
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				//Log.d(TAG, "Content-Length: "+""+postData.length()+":"+postData);
				conn.setRequestMethod("POST");
			}else{
				conn.setRequestMethod("GET");
			}

			if(authorzation!=null){
				conn.setRequestProperty("AUTHORZATION", authorzation);
				Log.d(TAG, "AUTHORZATION: "+authorzation);
			}
			if(verification!=null){
				conn.setRequestProperty("VERIFICATION", verification);
				Log.d(TAG, "VERIFICATION: "+verification);
			}

			conn.connect();

			if(isRunning() && postData!=null){
				output = new BufferedOutputStream(conn.getOutputStream());

				output.write(postData.getBytes());

				output.flush();
			}

			if(isRunning()){
				input = conn.getInputStream();
				
				InputStreamReader in=new InputStreamReader(input);
				StringBuilder sb = new StringBuilder();
				char buf[] = new char[4096];
				int cnt;
				String str;
				
				while((cnt=in.read(buf, 0, buf.length))!=-1 && isRunning()){
					sb.append(buf, 0, cnt);
				}
				
				str = sb.toString();

				if(isRunning()){
					Log.d(TAG, ">>>>>>>>>>JSON : "+str);

					if(!isJson){
						if(onGotResponse(conn.getResponseCode(), str))
							ret = true;
					}else if(isArray){
						JSONArray array;

						array = new JSONArray(str);

						if(onGotJson(conn.getResponseCode(), array))
							ret = true;
					}else{
						JSONObject obj;

						obj = new JSONObject(str);

						if(onGotJson(conn.getResponseCode(), obj))
							ret = true;
					}
				}
			}

			ret = true;
		}catch(ConnectException e){
			Log.d(TAG, "connect to server failed! ConnectException "+e);
			setError(ISTVError.CANNOT_CONNECT_TO_SERVER, getURL().toString());
		}catch(UnknownHostException e){
			Log.d(TAG, "connect to server failed! UnknownHostException "+e);
			setError(ISTVError.CANNOT_CONNECT_TO_SERVER, getURL().toString());
		}catch(SocketTimeoutException e){
			Log.d(TAG, "connect to server failed! SocketTimeoutException "+e);
			setError(ISTVError.CANNOT_CONNECT_TO_SERVER, getURL().toString());
		}catch(SocketException e){
			Log.d(TAG, "connect to server failed! SocketException "+e);
			setError(ISTVError.CANNOT_CONNECT_TO_SERVER, getURL().toString());
		}catch(NullPointerException e){
			Log.d(TAG, "connect to server failed! ----NullPointerException "+e+";\n url="+getURL().toString()+";/n prio="+prio);
			if(prio!=PRIO_ACCESS){
				setError(ISTVError.CANNOT_GET_DATA, getURL().toString());
			}
		}catch(Exception e){
			Log.d(TAG, "http request failed! "+e);
			onGetDataError();
		}finally{
			try{
				if(output!=null)
					output.close();
				if(input!=null)
					input.close();
				if(conn!=null)
					conn.disconnect();
			}catch(Exception e){
			}
		}

		return ret;
	}

	boolean onGotResponse(String str){		
		return true;
	}

	boolean onGotResponse(int code, String str){
		Log.d(TAG, "onGotResponse="+code);
		if(code<300)
			return onGotResponse(str);

		onGetDataError();
		return true;
	}


	boolean onGotJson(JSONObject obj) throws Exception{
		return true;
	}

	boolean onGotJson(int code, JSONObject obj) throws Exception{
		Log.d(TAG, "onGotJson="+code);
		if(code<300)
			return onGotJson(obj);

		onGetDataError();
		return true;
	}

	boolean onGotJson(JSONArray array) throws Exception{
		return true;
	}

	boolean onGotJson(int code, JSONArray array) throws Exception{
		if(code<300)
			return onGotJson(array);

		onGetDataError();
		return true;
	}

	void onGetDataError(){
		setError(ISTVError.CANNOT_GET_DATA, getURL().toString());
	}
	
	private boolean checkProxy(){
		String host=android.net.Proxy.getDefaultHost();
		int port =android.net.Proxy.getDefaultPort();
		if(host==null || port == -1){
			return false;
		}
		return true;
	}
	
	private Proxy getProxy(){
		if(proxy==null){
			/* 获取系统代理端口 */
			String host=android.net.Proxy.getDefaultHost();
			int port =android.net.Proxy.getDefaultPort();
			Log.d(TAG, " host : " +host +"     |||   port "+ port);
			SocketAddress sa=new InetSocketAddress(host,port);
			proxy=new Proxy(java.net.Proxy.Type.HTTP,sa);
		}
	    return proxy;
	}
}

