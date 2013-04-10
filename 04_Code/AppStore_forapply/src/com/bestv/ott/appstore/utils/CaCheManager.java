package com.bestv.ott.appstore.utils;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import android.os.Environment;
import android.os.StatFs;

/**
 * 缓存管理
 */
public class CaCheManager {

	private static final String TAG = "CacheManage";
	private long ALLOW_CACHE_SIZE = 50; //allow total cache = 50MB
	private File[] files;
	private int removeSize;
	private static CaCheManager manager;
	
	public static CaCheManager getInstance() {
		if(manager==null){
			AppLog.e(TAG, "---new CaCheManager()---");
			manager = new CaCheManager();
		}
		return manager; 
	}
	 
	/* check cache */
	public synchronized Boolean isCaCheSDCard(String nativeUrl) {
		boolean result = false;
		File imgFile = new File(nativeUrl);
		if (imgFile.exists() && imgFile.length() > 0) {
			result = true;
		}
		return result;
	}
	
	/* after MainActivity onDestory , refreshCache */
	public void refreshCaChe() {
		File file = new File(getCacheImgPath());
		double cachesize = getSize(file);/* total cache */
		AppLog.e(TAG, "------cache size = " + cachesize);
		if(cachesize>ALLOW_CACHE_SIZE){
			files = file.listFiles();
			removeSize = (int) ((0.2 * files.length) + 1);
			Arrays.sort(files, new FileLastModifSort());
			clearThread clear = new clearThread();
			if (!clear.isAlive()) {
				AppLog.e(TAG,"---the clearThread is start---");
				clear.start();
			}
		}
	}

	/* isFree ? */
	public Boolean isFreeSpace(long apkSize) {
		Boolean res = false;
		long freeSize = freeSpaceOnSd() * 1024;
		AppLog.e(TAG, "--------apkSize: " + apkSize/1024+ "KB    |----free sdcard :" + freeSize + "KB");
		if (apkSize/1024 > freeSize) {
			res = true;
		}
		return res;
	}

	/* free memory */
	private long freeSpaceOnSd() {
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			String sdcard = Environment.getExternalStorageDirectory().getPath();
			StatFs statFs = new StatFs(sdcard);
			long blockSize = statFs.getBlockSize();
			long blocks = statFs.getAvailableBlocks();
			long availableSpare = (blocks * blockSize) / (1024 * 1024);
			AppLog.e(TAG, "------------SDCard free size :" +availableSpare+"MB");
			return availableSpare;
		}
		return 0;
	}

	/* 返回图片存到sd卡的路径 */
	public static String getCacheImgPath() {
		File sdDir = null;
		boolean sdCardExist = checkSDcard();
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();// 获取根目录
		} else {
			return null;
		}
		String path = sdDir.toString() + "/joysee/images/";
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

	/* return 大小，单位：MB 返回已缓存图片大小 */
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

	/* 修改文件的最后访问时间 */
	public synchronized void refreshLastTime(String dir) {
		File file = new File(dir, getName(dir));
		file.setLastModified(System.currentTimeMillis());
	}

	/* 根据文件的最后修改时间进行排序  */
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
	
	/* clearThread */
	private class clearThread extends Thread {
		public void run() {
			super.run();
			for (int i = 0; i < removeSize; i++) {
				AppLog.e(TAG, "----delete : "+files[i].getName());
				files[i].delete();
				try {
					Thread.sleep(20);
				} catch (Exception e) {}
			}
		}
	}

	private String getName(String url) {
		String imageName = url
				.substring(url.lastIndexOf("/") + 1, url.length());
		return imageName;
	}

	/* clear sdCard's APK */
	public static void clearDataCache(String cacheUrl){
		AppLog.e(TAG, "the apk cacheURl --> : "+cacheUrl);
		File apkFile = new File(cacheUrl);
		if (apkFile.exists() && apkFile.length() > 0) {
			AppLog.e(TAG, "delete the apkCache : "+apkFile.getName());
			apkFile.delete();
		}
	}
}
