package com.ismartv.client;

import java.net.URL;
import android.graphics.Bitmap;
import com.ismartv.service.ISTVEvent;
import com.ismartv.service.ISTVRequest;
import com.ismartv.service.ISTVItem;
import android.util.Log;

abstract public class ISTVItemBitmapSignal extends ISTVSignal{
	private static final String TAG="ISTVItemBitmapSignal";
	public static final int POSTER=0;
	public static final int THUMB=1;
	public static final int ADLET=2;

	private static final int FOUND=0;
	private static final int REQUEST=1;
	private static final int NOT_FOUND=2;

	private ISTVItem item;
	private Bitmap bmp;
	private int type;
	private int mask=0;

	public ISTVItemBitmapSignal(ISTVClient c, ISTVItem item, int type){
		super(c);
		init(item, type);
	}

	public ISTVItemBitmapSignal(ISTVClient c, ISTVItem item){
		super(c);
		init(item, THUMB);
	}

	public ISTVItemBitmapSignal(ISTVClient c, int id, ISTVItem item, int type){
		super(c, id);
		init(item, type);
	}

	public ISTVItemBitmapSignal(ISTVClient c, int id, ISTVItem item){
		super(c, id);
		init(item, THUMB);
	}

	private boolean checkURLInCache(URL url){
		if(url==null)
			return false;

		bmp = client.getBitmapCache().requestBitmap(url);
		if(bmp!=null){
			Log.d(TAG, "bitmap "+url.toString());
			return true;
		}

		return false;
	}

	private void requestBitmap(URL url){
		ISTVRequest req = new ISTVRequest(ISTVRequest.Type.BITMAP);

		Log.d(TAG, "request item bitmap:"+((item.title!=null)?item.title:"")+" url:"+url.toString());

		req.url = url;
		client.sendRequest(req);
	}

	private URL getURL(int type){
		URL url = null;

		switch(type){
			case THUMB:
				url = item.thumbURL;
				break;
			case POSTER:
				url = item.posterURL;
				break;
			case ADLET:
				url = item.adletURL;
				break;
		}

		return url;
	}

	private int checkBitmap(int type){
		if((mask & (1<<type)) != 0)
			return NOT_FOUND;

		mask |= (1<<type);
		URL url = getURL(type);

		if(url==null)
			return NOT_FOUND;

		if(checkURLInCache(url))
			return FOUND;

		requestBitmap(url);
		return REQUEST;
	}

	private int checkAllBitmap(){
		int ret;

		ret = checkBitmap(type);
		if(ret!=NOT_FOUND)
			return ret;

		ret = checkBitmap(THUMB);
		if(ret!=NOT_FOUND)
			return ret;

		ret = checkBitmap(POSTER);
		if(ret!=NOT_FOUND)
			return ret;

		ret = checkBitmap(ADLET);
		if(ret!=NOT_FOUND)
			return ret;

		return ret;
	}

	private void init(ISTVItem item, int type){
		URL url = null;

		this.item = item;
		this.type = type;
		this.mask = 0;
		this.bmp  = null;

		if(type==ADLET){
			if(item.posterURL!=null && checkURLInCache(item.posterURL)){
				dataExist();
				return;
			}else if(item.thumbURL!=null && checkURLInCache(item.thumbURL)){
				dataExist();
				return;
			}
		}else if(type==THUMB){
			if(item.posterURL!=null && checkURLInCache(item.posterURL)){
				dataExist();
				return;
			}
		}

		refresh();
	}

	public Bitmap getBitmap(){
		return bmp;
	}

	public void setItem(ISTVItem item){
		if(item!=this.item){
			init(item, THUMB);
		}
	}
       
	boolean checkError(){
		if(mask==7)
			return false;

		Log.d(TAG, "request item bitmap:"+((item.title!=null)?item.title:"")+" error");

		return sendRequest();
	}

	boolean sendRequest(){
		int ret;

		ret = checkAllBitmap();
		if(ret!=REQUEST)
			return false;

		return true;
	}

	boolean match(ISTVEvent evt){
		if(evt.type==ISTVEvent.Type.BITMAP){
			if(evt.url.equals(item.thumbURL) || evt.url.equals(item.adletURL) || evt.url.equals(item.posterURL)){
				return true;
			}
		}

		return false;
	}

	boolean copyData(ISTVEvent evt){
		return checkURLInCache(evt.url);
	}
}

