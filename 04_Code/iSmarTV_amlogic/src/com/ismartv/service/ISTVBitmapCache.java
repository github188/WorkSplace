package com.ismartv.service;

import java.util.HashMap;
import java.net.URL;
import java.util.ArrayList;
import android.graphics.Bitmap;
import android.util.Log;

class ISTVBitmapCacheEntry{
	private URL    url;
	private Bitmap bmp;
	private int    size;
	private ISTVBitmapCache cache;

	ISTVBitmapCacheEntry(ISTVBitmapCache c, URL u, Bitmap bmp){
		this.cache = c;
		this.url   = u;
		this.bmp   = bmp;
		this.size  = bmp.getHeight()*bmp.getRowBytes();
	}

	synchronized Bitmap getBitmap(){
		return bmp;
	}

	synchronized int getSize(){
		return size;
	}

	synchronized URL getURL(){
		return url;
	}	
}

public class ISTVBitmapCache{
	private static final String TAG="ISTVBitmapCache";
	private static final int MAX_CACHED_SIZE = 32*1024*1024;
	private static final int MAX_ENTRY_COUNT = 500;
	private HashMap<URL, ISTVBitmapCacheEntry> map = new HashMap<URL, ISTVBitmapCacheEntry>();
	private ArrayList<ISTVBitmapCacheEntry> list = new ArrayList<ISTVBitmapCacheEntry>();
	private int totalSize;

	private synchronized void clearExpire(){
		while((list.size()>=MAX_ENTRY_COUNT) || (totalSize>MAX_CACHED_SIZE)){
			ISTVBitmapCacheEntry expire = list.get(0);
			map.remove(expire.getURL());
			list.remove(0);
			totalSize -= expire.getSize();
			Log.d(TAG, "remove expire "+expire.getSize()+" left "+totalSize);
			try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
		}
	}
	
	synchronized void addBitmap(URL url, Bitmap bmp){
		ISTVBitmapCacheEntry entry = new ISTVBitmapCacheEntry(this, url, bmp);

		list.add(entry);
		map.put(url, entry);

		totalSize += entry.getSize();
		clearExpire();
	}

	public synchronized Bitmap requestBitmap(URL url){
		ISTVBitmapCacheEntry entry = map.get(url);

		if(entry!=null){
			list.remove(entry);
			list.add(entry);
			return entry.getBitmap();
		}

		return null;
	}

	public synchronized Bitmap checkBitmap(URL url){
		ISTVBitmapCacheEntry entry = map.get(url);

		if(entry!=null){
			return entry.getBitmap();
		}

		return null;
	}
}

