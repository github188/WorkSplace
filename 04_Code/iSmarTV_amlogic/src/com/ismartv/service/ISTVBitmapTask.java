package com.ismartv.service;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.util.HashMap;
import android.graphics.Bitmap;
import android.util.Log;
import android.graphics.BitmapFactory;
import java.net.ConnectException;

public class ISTVBitmapTask extends ISTVTask{
	private static final String TAG="ISTVBitmapTask";
	private static HashMap<URL,ISTVBitmapTask> taskMap=new HashMap<URL,ISTVBitmapTask>();

	ISTVBitmapTask(ISTVTaskManager man, URL url){
		super(man, url, ISTVTask.PRIO_BITMAP);

		Bitmap bmp = getBitmapCache().checkBitmap(url);
		if(bmp!=null){
			ISTVEvent evt = createEvent();

			getService().sendEvent(evt);
			return;
		}

		if(setRunningTask(url, this)){
			start();
		}
	}

	ISTVEvent createEvent(){
		ISTVEvent evt = new ISTVEvent(ISTVEvent.Type.BITMAP);
		evt.url = url;
		return evt;
	}

	private static synchronized boolean setRunningTask(URL url, ISTVBitmapTask t){
		if(t==null){
			taskMap.remove(url);
			return true;
		}else{
			ISTVBitmapTask old = taskMap.get(url);
			if(old==null){
				taskMap.put(url, t);
				return true;
			}
		}

		return false;
	}

	void onCancel(){
		setRunningTask(url, null);
	}

	boolean process(){
		HttpURLConnection conn=null;
		InputStream input=null;
		boolean ret = false;

		try{
			Log.d(TAG, "url: "+url.toString());
			conn = (HttpURLConnection) url.openConnection();

			conn.connect();

			if(isRunning() && conn.getResponseCode()<300){
				input = conn.getInputStream();
				Bitmap bmp = BitmapFactory.decodeStream(input);

				ISTVEvent evt = createEvent();
				Log.d(TAG, "get bitmap "+url+" ("+bmp.getWidth()+"x"+bmp.getHeight()+")");
				getBitmapCache().addBitmap(url, bmp);
				getService().sendEvent(evt);

				ret = true;
			}else{
				Log.d(TAG, "download bitmap failed! http return "+conn.getResponseCode());
				setError(ISTVError.CANNOT_GET_BITMAP);
			}
		}catch(ConnectException e){
			Log.d(TAG, "connect to download bitmap failed! "+e.getMessage());
			setError(ISTVError.CANNOT_CONNECT_TO_SERVER);
		}catch(Exception e){
			Log.d(TAG, "download bitmap failed! "+e.getMessage());
			setError(ISTVError.CANNOT_GET_BITMAP);
		}finally{
			try{
				if(input!=null)
					input.close();
				if(conn!=null)
					conn.disconnect();
			}catch(Exception e){
			}
		}

		return ret;
	}
}

