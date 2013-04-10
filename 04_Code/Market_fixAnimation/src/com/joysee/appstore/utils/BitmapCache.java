package com.joysee.appstore.utils;

import java.util.HashMap;
import java.util.ArrayList;
import android.graphics.Bitmap;

	public class BitmapCache {
		private static final String TAG = "BitmapCache";
		private static final int MAX_CACHED_SIZE = 42 * 1024 * 1024;
		private static final int MAX_ENTRY_COUNT = 500;
		public static HashMap<String, BitmapCacheBean> map = new HashMap<String, BitmapCacheBean>();
		private ArrayList<BitmapCacheBean> list = new ArrayList<BitmapCacheBean>();
		private int totalSize;
	
		private synchronized void clearExpire() {
			if ((list.size() >= MAX_ENTRY_COUNT)|| (totalSize > MAX_CACHED_SIZE)) {
				BitmapCacheBean cacheBean = list.get(0);
				map.remove(cacheBean.getURL());
				list.remove(0);
				totalSize -= cacheBean.getSize();
				AppLog.e(TAG, "remove size " + cacheBean.getSize() + " | totalSize:"
						+ totalSize);
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
	}

	synchronized void addBitmap(String url, Bitmap bmp) {
		if(bmp!=null){
			BitmapCacheBean entry = new BitmapCacheBean(this, url, bmp);
			list.add(entry);
			map.put(url, entry);
			totalSize += entry.getSize();
			clearExpire();
		}else{
			BitmapCacheBean entry = new BitmapCacheBean(this, url);
			list.add(entry);
			map.put(url, entry);
		}
	}

	public synchronized Bitmap requestBitmap(String url) {
		BitmapCacheBean entry = map.get(url);
		if (entry != null) {
			list.remove(entry);
			list.add(entry);
			return entry.getBitmap();
		}
		return null;
	}

	public synchronized Bitmap checkBitmap(String url) {
		BitmapCacheBean entry = map.get(url);
		if (entry != null) {
			return entry.getBitmap();
		}
		return null;
	}
	
	
	/* cache bean */
	class BitmapCacheBean {
		private String url;
		private Bitmap bmp = null;
		private int size;
		BitmapCacheBean(BitmapCache c, String u){
			this.url = u;
			this.size = 0;
		}
		BitmapCacheBean(BitmapCache c, String u, Bitmap bmp) {
			this.url = u;
			this.bmp = bmp;
			this.size = bmp.getHeight() * bmp.getRowBytes();
		}
	
		synchronized Bitmap getBitmap() {
			return bmp;
		}
	
		synchronized int getSize() {
			return size;
		}
	
		synchronized String getURL() {
			return url;
		}
	}

}
