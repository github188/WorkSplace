package com.joysee.adtv.server;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

import android.util.Log;

abstract public class ADTVJsonTask extends ADTVTask{
	private static final String TAG="ADTVJsonTask";
	static String entry = "http://210.14.137.99:8080/interfaceepg/app/product";
//	static String host = "cord.tvxio.com";
	static String server = entry;
//	static String devName ="A11C";
//	static String devVersion = "1.0";
	static String mac=null;
	static String accredit="";

	String authorzation;
	String verification;
	String accessToken;
	String postData=null;
	
	/* HttpURLConnection代理 */
	private Proxy proxy = null;

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
//		return "000102650475";
		return mac;
	}
	
//	static String getUserAgent(){
//		return devName+"/"+devVersion+" "+getMACAddress();
//	}

	protected synchronized static String getURL(String path){
		String addr = server + path;
		return addr;
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

	/**
	 * path为相对路径
	 */
	ADTVJsonTask(String path, int prio, int times){
		super(getURL(path), prio, times);
	}
	
	/**
	 * path为绝对路径,str=null
	 */
	ADTVJsonTask(String path,int prio,int times,String str){
	    super(path, prio, times);
	}

	boolean process(){
		HttpURLConnection conn=null;
		InputStream input = null;
		OutputStream output = null;
		boolean ret = false;
		Log.d(TAG, "process="+url);
		try{
		    if(postData!=null){
		        url=url+"?"+postData;
		    }
		    URL mUrl=new URL(url);
			Log.d(TAG, ">>>>>>>> URL : "+url.toString() + " proxy " + checkProxy());
			if(checkProxy()){
				conn = (HttpURLConnection) mUrl.openConnection(getProxy());//使用代理访问
			}else{
				conn = (HttpURLConnection) mUrl.openConnection();
			}
			conn.setUseCaches(false);
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			conn.setDoInput(true);

			Log.d(TAG, "---------postData="+postData);
			conn.setRequestMethod("GET");
			conn.connect();

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

                if (isRunning()) {
                    Log.d(TAG, ">>>>>>>>>>JSON : " + str);
                    if (onGotResponse(conn.getResponseCode(), str))
                        ret = true;

                }
			}

			ret = true;
		}catch(ConnectException e){
			Log.d(TAG, "connect to server failed! ConnectException "+e);
			setError(ADTVError.CANNOT_CONNECT_TO_SERVER, getURL());
		}catch(UnknownHostException e){
			Log.d(TAG, "connect to server failed! UnknownHostException "+e);
			setError(ADTVError.CANNOT_CONNECT_TO_SERVER, getURL());
		}catch(SocketTimeoutException e){
			Log.d(TAG, "connect to server failed! SocketTimeoutException "+e);
			setError(ADTVError.CANNOT_CONNECT_TO_SERVER, getURL());
		}catch(SocketException e){
			Log.d(TAG, "connect to server failed! SocketException "+e);
			setError(ADTVError.CANNOT_CONNECT_TO_SERVER, getURL());
		}catch(NullPointerException e){
			Log.d(TAG, "connect to server failed! ----NullPointerException "+e+";\n url="+getURL()+";/n prio="+prio);
			if(prio!=PRIO_ACCESS){
				setError(ADTVError.CANNOT_GET_DATA, getURL());
			}
		}catch (MalformedURLException e) {
            Log.d(TAG, "url is error "+e.getMessage());
            e.printStackTrace();
            setError(ADTVError.CANNOT_CONNECT_TO_SERVER);
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


	void onGetDataError(){
		setError(ADTVError.CANNOT_GET_DATA, getURL());
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

