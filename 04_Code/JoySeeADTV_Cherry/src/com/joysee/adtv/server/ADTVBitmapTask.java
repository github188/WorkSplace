package com.joysee.adtv.server;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.util.HashMap;
import android.graphics.Bitmap;
import android.util.Log;
import android.graphics.BitmapFactory;
import java.net.ConnectException;

abstract public class ADTVBitmapTask extends ADTVTask{
	private static final String TAG="ADTVBitmapTask";
	private static HashMap<String,ADTVBitmapTask> taskMap=new HashMap<String,ADTVBitmapTask>();
	
	private Bitmap bmp=null;
	public int programId;

	public ADTVBitmapTask(int programId,String url){
		super(url, ADTVTask.PRIO_BITMAP);
		this.programId=programId;
		Log.d(TAG, "---------url="+url);

		bmp = getBitmapCache().checkBitmap(url);
		if(bmp!=null){
		    Log.d(TAG, "----bmp!=null-----url="+url);
		    onSingal();
			return;
		}

//		if(setRunningTask(url, this)){
			start();
//		}
	}
	
	public Bitmap getBitmap(){
	    return bmp;
	}


	private static synchronized boolean setRunningTask(String url, ADTVBitmapTask t){
		if(t==null){
			taskMap.remove(url);
			return true;
		}else{
			ADTVBitmapTask old = taskMap.get(url);
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
		    URL mUrl=new URL(url);
//			Log.d(TAG, "url: "+url.toString());
			conn = (HttpURLConnection) mUrl.openConnection();

			conn.connect();

			if(isRunning() && conn.getResponseCode()<300){
				input = conn.getInputStream();
				bmp = BitmapFactory.decodeStream(input);

				Log.d(TAG, "get bitmap "+url+" ("+bmp.getWidth()+"x"+bmp.getHeight()+")");
				getBitmapCache().addBitmap(url, bmp);
				
				ret = true;
			}else{
				Log.d(TAG, "download bitmap failed! http return "+conn.getResponseCode());
				setError(ADTVError.CANNOT_GET_BITMAP);
			}
		}catch(ConnectException e){
			Log.d(TAG, "connect to download bitmap failed!"+e.getMessage());
			setError(ADTVError.CANNOT_CONNECT_TO_SERVER);
		}catch (MalformedURLException e) {
		    Log.d(TAG, "url is error"+e.getMessage());
            e.printStackTrace();
            setError(ADTVError.CANNOT_CONNECT_TO_SERVER);
        }catch(Exception e){
            Log.d(TAG, "download bitmap failed! "+e.getMessage());
            setError(ADTVError.CANNOT_GET_BITMAP);
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

