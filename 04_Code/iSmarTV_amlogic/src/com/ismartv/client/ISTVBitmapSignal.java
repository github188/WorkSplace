package com.ismartv.client;

import java.net.URL;
import android.graphics.Bitmap;
import com.ismartv.service.ISTVEvent;
import com.ismartv.service.ISTVRequest;
import android.util.Log;

abstract public class ISTVBitmapSignal extends ISTVSignal{
	private static final String TAG="ISTVBitmapSignal";
	private URL url;
	private Bitmap bmp;

	public ISTVBitmapSignal(ISTVClient c, int id, URL u){
		super(c, id);
		url = u;

		refresh();
	}

	public ISTVBitmapSignal(ISTVClient c, URL u){
		super(c);
		url = u;

		refresh();
	}

	public Bitmap getBitmap(){
		return bmp;
	}

	public void setURL(URL u){
		if(!url.equals(u)){
			url = u;
			bmp = null;
			refresh();
		}
	}

	public URL getURL(){
		return url;
	}

	boolean sendRequest(){
		bmp = client.getBitmapCache().requestBitmap(url);
		if(bmp!=null){
			return false;
		}

		ISTVRequest req = new ISTVRequest(ISTVRequest.Type.BITMAP);
		req.url = url;
		client.sendRequest(req);

		return true;
	}

	boolean match(ISTVEvent evt){
		if(evt.type==ISTVEvent.Type.BITMAP){
			if(evt.url.equals(url)){
				Bitmap bmp = client.getBitmapCache().requestBitmap(url);
				if(bmp!=null){
					this.bmp = bmp;
					return true;
				}else{
					Log.d(TAG, "received bitmap event, but cannot find it in cache");
				}
			}
		}

		return false;
	}

	boolean copyData(ISTVEvent evt){
		return true;
	}
}

