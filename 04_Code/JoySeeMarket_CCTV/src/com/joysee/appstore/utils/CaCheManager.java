package com.joysee.appstore.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import com.joysee.appstore.common.Constants;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.Shader.TileMode;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

/**
 * 加有Map索引的缓存管理
 */
public class CaCheManager {

	private static BitmapCache bitmapCache = new BitmapCache();
	private static final String TAG = "CacheManage";
	private long FREE_SD_SPACE_NEEDED_TO_CACHE = 20;
	private static final long RAMSpace = 500;//ROM size
	private long CACHE_SIZE = 100;
	private int removeSize;
	private File[] files;

	public static Boolean isCaCheExists(String nativeUrl) {
		boolean result = false;
		String imageName = getName(nativeUrl);
		/* first check the map cache */
		if(BitmapCache.map.containsKey(imageName)){
			AppLog.e(TAG, "----map.containsKey :"+nativeUrl);
			result = true;
		}else{/* second check the sdCard or ROM cache */
			File imgFile = new File(nativeUrl);
			if (imgFile.exists() && imgFile.length()>0){
				result = true;
				AppLog.e(TAG, "----map no have key :"+nativeUrl);
				bitmapCache.addBitmap(imageName,synchronToRam(nativeUrl));
			}
		}
		return result;
	}
	
	public static void addBitmap(String nativeUrl){
		bitmapCache.addBitmap(getName(nativeUrl),synchronToRam(nativeUrl));
	}
	
	/* clear data/data's APK */
	public static void clearDataCache(String nativeUrl){
		AppLog.d(TAG, "--clearDataCache-the apk cacheURl : "+nativeUrl+"-----");
		File apkFile = new File(nativeUrl);
		if (apkFile.exists() && apkFile.length() > 0) {
			apkFile.delete();
			AppLog.d(TAG, "---delete the apk : "+apkFile.getName()+"------");
		}
	}
	
	public void refreshCaChe() {
		String sdDir = getCacheImgPath();
		String romDir = Constants.IMAGEROOT;
		AppLog.e(TAG, "--------sdDir : "+sdDir);
		AppLog.e(TAG, "--------romDir : "+romDir);
		if(!"".equals(sdDir)){
			doRefresh(sdDir);
			doRefresh(romDir);
		}else{
			doRefresh(romDir);
		}
	}

	private void doRefresh(String sdDir) {
		File file = new File(sdDir);
		double cachesize = getSize(file); 
		AppLog.e(TAG, "-------cache size = " + cachesize+ "----------");
		if((CACHE_SIZE-cachesize)<FREE_SD_SPACE_NEEDED_TO_CACHE){ 
			files = file.listFiles();
			removeSize = (int) ((0.3 * files.length) + 1);
			Arrays.sort(files, new FileLastModifSort());
			clearThread clear = new clearThread();
			if (!clear.isAlive()) {
				AppLog.e(TAG,"------> the clearThread is start");
				clear.start();
			}
		}
	}
	
	public Boolean isFreeSpace(long apkSize) {
		Boolean res = false;
		long freeSize = freeSpaceOnSd() * 1024;
		AppLog.e(TAG, "--------apkSize: " + apkSize/1024+ "KB    |----free sdcard :" + freeSize + "KB");
		if (apkSize/1024 > freeSize) {
			res = true;
		}
		return res;
	}
	
	/* data/data's free space  */
	public Boolean isFreeRAMSpace(long apkSize){
		Boolean res = false;
		File apkFile = new File(Constants.APKROOT);
		if (apkFile.exists()) {
			AppLog.e(TAG, "-----------apkSize :"+apkSize/(1024*1024) +"|----RAMSize :"+RAMSpace);
			if (apkSize/(1024*1024) < RAMSpace) {
				res = true;
			}else{
				res = false;
			}
		}else{
			apkFile.mkdir();
			res = true;
		}
		return res;
	}

	/*　sdcard's free space　*/
	private long freeSpaceOnSd() {
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			String sdcard = Environment.getExternalStorageDirectory().getPath();
			StatFs statFs = new StatFs(sdcard);
			long blockSize = statFs.getBlockSize();
			long blocks = statFs.getAvailableBlocks();
			long availableSpare = (blocks * blockSize) / (1024 * 1024);
			AppLog.e(TAG, "-----------SDCard free size :" +availableSpare+"MB");
			return availableSpare;
		}
		return 0;
	}

	public static String getCacheImgPath() {
		File sdDir = null;
		String path = "";
		boolean sdCardExist = checkSDcard();
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();// 获取根目录
			path = sdDir.toString() + "/joysee/images/";
		} 
		return path;
	}

	public static boolean checkSDcard() {
		String status = android.os.Environment.getExternalStorageState();
		if (!status.equals(Environment.MEDIA_MOUNTED)) {
			return false;
		} else {
			return true;
		}
	}
	
	private double getSize(File file) {
		if (file.exists()) {
			if (!file.isFile()) {
				File[] ff = file.listFiles();
				double size = 0;
				for (File f : ff) {
					size += getSize(f);
				}
				return size;
			} else {
				double size = (double) file.length() / 1024 / 1024;
				return size;
			}
		} else {
			return 0.0;
		}
	}
	
	/*　refresh file's last access time　*/
	public static void refreshLastTime(String dir) {
		File file = new File(dir, getName(dir));
		file.setLastModified(System.currentTimeMillis());
	}
	
	/*　sequence 　*/
	private class FileLastModifSort implements Comparator<File> {
		public int compare(File a, File b) {
			if (a.lastModified() > b.lastModified()) {
				return 1;
			} else if (a.lastModified() == b.lastModified()) {
				return 0;
			} else {
				return -1;
			}
		}
	}

	private class clearThread extends Thread {
		public void run() {
			super.run();
			for (int i = 0; i < removeSize; i++) {
				files[i].delete();
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static String getName(String url) {
		String imageName = url
				.substring(url.lastIndexOf("/") + 1, url.length());
		return imageName;
	}

	/* 缓存-->bitmap */
	public synchronized static Bitmap synchronToRam(String url){
		  System.gc();
	      InputStream is = null;
		  try {
			  is = new FileInputStream(url);
			  BitmapFactory.Options options=new BitmapFactory.Options();
			  options.inJustDecodeBounds = false;
			  options.inPreferredConfig =Config.ARGB_4444;
//			  options.inSampleSize = 2;
			  Bitmap newBitmap =BitmapFactory.decodeStream(is,null,options);
			  if(newBitmap==null||newBitmap.getByteCount()<0||newBitmap.getRowBytes()<0){
		    	  File imageF=new File(url);
				  if(imageF.exists()){
					  imageF.delete();
					  AppLog.e(TAG, "synchronToRam--->newBitmap==null");
					  return null;
				  }
		      }else{
		    	  AppLog.e(TAG, "cache--->synchronToRam--->bitmap");
		    	  return newBitmap;
		      }
		  } catch (Exception e) {
			  
			  e.printStackTrace();
			  File imageF=new File(url);
			  if(imageF.exists()){
				  imageF.delete();
				  AppLog.e(TAG, "synchronToRam--->Exception e");
				  return null;
			  }
		  } catch(Error e){
			  
			  e.printStackTrace();
			  File imageF=new File(url);
			  if(imageF.exists()){
				  imageF.delete();
				  AppLog.e(TAG, "synchronToRam--->Error e");
				  return null;
			  }
		  }
		  return null;
	}
	
	/* get bitmap from the map */
	public synchronized static Bitmap requestBitmap(String url){
		Log.d("xubin", "requestmap=====");
		return bitmapCache.requestBitmap(getName(url));
	}
	
	public synchronized static Bitmap requestIcon(String url){
	    Bitmap b = bitmapCache.requestBitmap(getName(url));
	    if(b!=null){
	    	Bitmap c = Utils.getRoundedCornerBitmap(b, 30.0f);
	    	if(null!=c){
	    		return getReflectionImageWithOrigin(c);
	    	}else{
	    		return null;
	    	}
	    }else{
	        return null;
	    }
	}
	/** 
     * 获得带倒影的图片 
     * @param bitmap 
     */
   public static Bitmap getReflectionImageWithOrigin(Bitmap bitmap) {
       
       // 原始图片和反射图片中间的间距
       final int reflectionGap = 0;
       int width = bitmap.getWidth();
       int height = bitmap.getHeight();
       
       // 反转  
       Matrix matrix = new Matrix();
       
       // 第一个参数为1表示x方向上以原比例为准保持不变，正数表示方向不变。   
       // 第二个参数为-1表示y方向上以原比例为准保持不变，负数表示方向取反。
       matrix.preScale(1, -1);
       Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, height / 2,
               width, height / 2, matrix, false);
       Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
               (height + height / 4), Config.RGB_565);
       Canvas canvas = new Canvas(bitmapWithReflection);
       canvas.drawBitmap(bitmap, 0, 0, null);
       Paint defaultPaint = new Paint();
       canvas.drawRect(0, height, width, height + reflectionGap, defaultPaint);
       canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);
       Paint paint = new Paint();
       LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0,
               bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff,
               0x00ffffff, TileMode.CLAMP);
       paint.setShader(shader);
       paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
       canvas.drawRect(0, height, width, bitmapWithReflection.getHeight()
               + reflectionGap, paint);
       return bitmapWithReflection;
   }
}
