package com.lenovo.settings.update;

import java.io.File;
import java.util.List;
import java.io.InputStream;
import java.io.FileInputStream;
import java.net.URL;
import com.lenovo.settings.Util.PullParserXml;
import com.lenovo.settings.Object.ErrorCode;
import java.io.IOException;
import com.lenovo.settings.R;
import com.lenovo.settings.SettingBroadcastReceiver;
import com.lenovo.settings.Object.Patchs;
import com.lenovo.settings.Object.Version;
import com.lenovo.settings.Object.Firmware;
import com.lenovo.settings.Object.Patch;
import com.lenovo.settings.Object.UpdateData;
import com.lenovo.settings.Object.Versions;
import com.lenovo.settings.Util.FileUtils;
import com.lenovo.settings.Util.MD5;
import com.lenovo.settings.Util.Recovery;
import com.lenovo.settings.Util.SerialNumber;
import com.lenovo.settings.Util.UpdateHttpClient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.SystemProperties;
import android.util.Log;

public class UpdateCheck {
	
	private static final String TAG = "UpdateCheck";
	public static final int MSG_TITLE = 0;
	public static final int MSG_DOWNLOAD = 1;
	public static final int MSG_CHECK = 2;
	private Context mContext;
	private UpdateHttpClient mUpdateHttpClient = null;
	private FileUtils mFileUtils;
	private String attr;
	public static final String XML_SDCARD = "/mnt/sdcard/external_sdcard/";
	public static final String XML_DATA = "/data/tsup/";

	public UpdateCheck(Context context){
		mContext = context;
		mFileUtils = new FileUtils();
	}
	
    boolean checkNetwordConnected() {
        ConnectivityManager conManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfo = conManager.getAllNetworkInfo();
        if (networkInfo != null) {
            for (int i = 0; i < networkInfo.length; i++) {
                if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
                    Log.i(TAG, " checkNetwordConnected " + networkInfo[i].toString());
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkUpdate() {
        String str;
        String serial_number;
        String url;
        String mUrl;
        Object obj = null;
        //1.检查网络状态
        if (!checkNetwordConnected()) {
            str = mContext.getString(R.string.network_disconnection);
            sendBroadcast(mContext, MSG_TITLE, str, true);
            return false;
        }
        url = getUrl();
        mUrl = url + UpdateHttpClient.URL_PARAMETER + getMac()+ UpdateHttpClient.URL_VERSION + Build.ID;
        str = mContext.getString(R.string.connect_service);
        sendBroadcast(mContext, MSG_TITLE, str, false);
        mUpdateHttpClient = new UpdateHttpClient();
        Log.d(TAG, "update mUrl = " + mUrl);
        obj = mUpdateHttpClient.getServerData(mUrl);
        if (obj == null) {
            str = mContext.getString(R.string.connect_service_fail);
            sendBroadcast(mContext, MSG_TITLE, str, true);
            return false;
        } else if (obj instanceof Error) {
            str = mContext.getString(R.string.connect_service_fail);
            sendBroadcast(mContext, MSG_TITLE, str, true);
            return false;
        } else if (!(obj instanceof Versions)) {
            str = mContext.getString(R.string.not_new_version);
            sendBroadcast(mContext, MSG_TITLE, str, true);
            return false;
        }
        
        Versions versions = (Versions) obj;
        //2.检查新版本
        if (!checkVersion(versions)) {
            str = mContext.getString(R.string.not_new_version);
            sendBroadcast(mContext, MSG_TITLE, str, true);
            return false;
        }
        str = mContext.getString(R.string.found_new_version) + UpdateData.Version;
        sendBroadcast(mContext, MSG_TITLE, str, false);
        //3.获取升级包大小
        int filesizeInByte = mUpdateHttpClient.getDownloadSizeByURL(UpdateData.URL);
        int filesize = filesizeInByte / 1024;
        Log.d(TAG, new UpdateData().toString());
        // update by yuhongkun 20120806
        if (filesize <= 0) {
            return false;
        }
        if ((UpdateData.SizeInKb != filesize) && (filesize > 0)) {
            UpdateData.SizeInKb = filesize;
        }
        //4.检查磁盘空间
        if (!checkMemory(UpdateData.SizeInKb, FileUtils.DATA_PARH)) {// 检查data空间
            try {
                //空间不足删除/cache/路径文件
                delAllFile(FileUtils.DATA_PARH);
            } catch (Exception e) {
                e.printStackTrace();
                str = mContext.getString(R.string.cache_no_space);
                sendBroadcast(mContext, MSG_TITLE, str, true);
                return false;
            }
            if (!checkMemory(UpdateData.SizeInKb, FileUtils.DATA_PARH)) {// 检查data空间
             // 没有空间
                str = mContext.getString(R.string.cache_no_space);
                sendBroadcast(mContext, MSG_TITLE, str, true);
                return false;
            }

           /* if (!checkSDCard()) {//检查sdcard是否挂载
                return false;
            }
            if (!checkMemory(UpdateData.SizeInKb, FileUtils.SD_PARH)) {// data没有空间则检查sdcard空间
                 //没有空间
                str = mContext.getString(R.string.memory_no_space);
                sendBroadcast(mContext, MSG_TITLE, str, true);
                return false;
            }*/
        }
        mContext.getSharedPreferences(SettingBroadcastReceiver.Update_SharePre,
                Context.MODE_WORLD_READABLE).edit().putString("Version", UpdateData.Id)
                .commit();
        mContext.getSharedPreferences(SettingBroadcastReceiver.Update_SharePre,
                Context.MODE_WORLD_READABLE).edit().putString("md5", UpdateData.Md5)
                .commit();
        mContext.getSharedPreferences(SettingBroadcastReceiver.Update_SharePre,
                Context.MODE_WORLD_READABLE).edit().putString("path", UpdateData.Path + "update.zip")
                .commit();
        mContext.getSharedPreferences(SettingBroadcastReceiver.Update_SharePre,
                Context.MODE_WORLD_READABLE).edit().putLong("size", filesizeInByte).commit();
        if (UpdateData.Path.equals(FileUtils.SD_PARH)) {
            str = mContext.getString(R.string.update_downloading_sdcard, UpdateStatus.getProgress()
                    + "%");
        } else {
            str = mContext.getString(R.string.update_downloading_data, UpdateStatus.getProgress()
                    + "%");
        }
        sendBroadcast(mContext, MSG_DOWNLOAD, str, false);
        return true;
    }
	
//	public boolean checkLocalUpdate(){
//		String str;
//		String path;
//		File file;
//		UpdateData.clearUpdateData();
//		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
//			path = "/mnt/sdcard/update.zip";// Rony modify 20120425
//			file = new File(path);// Rony modify 20120425
//			if(file.exists()){
//				if(Recovery.verifyPackage(file)){
//					UpdateData.Path = path;
//					return true;
//				}
//			}else{
//				str = mContext.getString(R.string.not_new_version);
//				sendBroadcast(mContext,MSG_TITLE,str,true);
//				return false;
//			}
//		}else{
//			str = mContext.getString(R.string.update_no_sdcard);
//    		sendBroadcast(mContext,MSG_TITLE,str,true);
//    		return false;
//		}
//		
//		return false;
//	}
	/**
	 * 检查本地升级文件
	 * @param type 0 标示sdcard 1 标示data
	 * @param xmlPath 升级文件路径
	 * @return
	 */
    public boolean checkLocalUpdate_xml(int type, String path) {
        Log.d(TAG, " checkLocalUpdate_xml type = " + type + " path = " + path);
        String str;
        String zipPath;
        File file;
        File xmlFile = null;
        Object obj = null;
        InputStream inputStream = null;
        UpdateData.clearUpdateData();
        // sdcard升级
        if (type == 0) {
            // 检查sdcard
            Log.d(TAG,
                    " Environment.getExternalStorage2State() = "
                            + Environment.getExternalStorage2State()
                            + " getExternalStorage2Directory()  = "
                            + Environment.getExternalStorage2Directory());
            if (!Environment.getExternalStorage2State().equals(Environment.MEDIA_MOUNTED)) {
                str = mContext.getString(R.string.update_no_sdcard);
                sendBroadcast(mContext, MSG_TITLE, str, true);
                return false;
            }
        }
        xmlFile = new File(path + "update.xml");
        if (!xmlFile.exists()) {
            str = mContext.getString(R.string.not_new_xml);
            sendBroadcast(mContext, MSG_TITLE, str, true);
            return false;
        } else {
            try {
                // URL url = new URL(xmlPath);
                // inputStream = url.openStream();
                inputStream = new FileInputStream(xmlFile);
                PullParserXml handler = new PullParserXml();
                obj = handler.getServerData(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                if (null != inputStream) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (obj == null) {
            str = mContext.getString(R.string.not_new_version);
            sendBroadcast(mContext, MSG_TITLE, str, true);
            return false;
        } else if (obj instanceof Error) {
            str = mContext.getString(R.string.not_new_version);
            sendBroadcast(mContext, MSG_TITLE, str, true);
            return false;
        } else if (!(obj instanceof Versions)) {
            str = mContext.getString(R.string.not_new_version);
            sendBroadcast(mContext, MSG_TITLE, str, true);
            return false;
        }
        
        Versions versions = (Versions) obj;
        if (!checkVersion(versions)) {
            str = mContext.getString(R.string.not_new_version);
            sendBroadcast(mContext, MSG_TITLE, str, true);
            return false;
        }
        str = mContext.getString(R.string.found_new_version) + UpdateData.Version;
        sendBroadcast(mContext, MSG_TITLE, str, false);
        zipPath = path + "update.zip";
        file = new File(zipPath);
        if (file.exists()) {
            str = mContext.getString(R.string.update_checking);
            sendBroadcast(mContext, UpdateCheck.MSG_CHECK, str, false);
            if (!checkMD5(zipPath, UpdateData.Md5)) {
                str = mContext.getString(R.string.update_check_fail);
                sendBroadcast(mContext, UpdateCheck.MSG_CHECK, str, true);
                return false;
            }
            if (Recovery.verifyPackage(file)) { 
                UpdateData.Path = zipPath;
                return true;
            }
        } else {
            str = mContext.getString(R.string.not_new_version);
            sendBroadcast(mContext, MSG_TITLE, str, true);
            return false;
        }
        return false;
    }
    
    private String getUrl() {
        return SystemProperties.get("online.ip", UpdateHttpClient.URL);
    }
    
    private String gerSerialNumber() {
        SerialNumber mSerialNumber = new SerialNumber();
        return mSerialNumber.getSerialNumber();
    }
    
    /**
     * @param versions
     * @return 返回检查结果 没有新版本返回false
     */
    private boolean checkVersion(Versions versions) {
        int ret = -1;
        int i, count;
        
        List<Version> list = versions.getVersions();
        count = list.size();
        if (count < 1) {
            return false;
        }
        for (i = 0; i < count; i++) {
            Firmware firmware = list.get(i).getFirmware();
            if (firmware == null) {
                continue;
            }
            String id = firmware.getId();
            attr = firmware.getAttribute();
            ret = checkFirmwareVersion(firmware.getId());
            if (ret == 1) {
                // if((!attr.equals(UpdateStatus.UPDATE_ATTRIBUTE_ENFORCE)) &&
                // (i == (count - 1))){//update by yuhongkun 20120829
                if (i == (count - 1)) {
                    setUpdataData(firmware);
                    return true;
                }
            } else if ((ret == 0) && (i == (count - 1))) {
                Patchs patchs = list.get(i).getPatchs();
                return checkPatchs(patchs);
            }
        }
        return false;
    }
	
	public boolean checkPatchs(Patchs patchs){
		int i,count;
		Log.e(TAG,"patchs = "+patchs);
		if(patchs == null){
			return false;
		}
		List<Patch> list = patchs.getPatchs();
		count = list.size();
		if(count < 1){
			return false;
		}
		for(i=0;i<count;i++){
			Patch patch = list.get(i);
			if(patch == null){
				continue;
			}
			 attr = patch.getAttribute();
			if(checkPatchVersion(patch.getId())){
				if((attr.equals(UpdateStatus.UPDATE_ATTRIBUTE_IMPORTANC)) || (i == (count - 1))){
					setUpdataData(patch);
					return true;
				}
			}
		}
		return false;
	}
	
	public void setUpdataData(Firmware firmware){
		UpdateData.clearUpdateData();
		UpdateData.Version = firmware.getVersion();
		String size = firmware.getSizeInKB();
		UpdateData.SizeInKb = Long.parseLong(size);
		UpdateData.Id = firmware.getId();
		UpdateData.Name = firmware.getName();
		UpdateData.URL = firmware.getURL();
		UpdateData.Md5 = firmware.getMD5();
		UpdateData.attr = firmware.getAttribute();
		if(UpdateStatus.DEBUG){
			Log.d(TAG,"update id = "+firmware.getId());
			Log.d(TAG,"update name = "+firmware.getName());
			Log.d(TAG,"update version = "+firmware.getVersion());
			Log.d(TAG,"update md5 = "+firmware.getMD5());
			Log.d(TAG,"update size in kb = "+firmware.getSizeInKB());
			Log.d(TAG,"update url = "+firmware.getURL());
			Log.d(TAG,"update filesize = "+firmware+"KB");
			Log.d(TAG,"update attr = "+firmware.getAttribute());
		}
	}
	
	public void setUpdataData(Patch patch){
		UpdateData.clearUpdateData();
		UpdateData.Version = patch.getVersion();
		String size = patch.getSizeInKB();
		UpdateData.SizeInKb = Long.parseLong(size);
		UpdateData.Id = patch.getId();
		UpdateData.Name = patch.getName();
		UpdateData.URL = patch.getURL();
		UpdateData.Md5 = patch.getMD5();
		UpdateData.attr = patch.getAttribute();
		if(UpdateStatus.DEBUG){
			Log.d(TAG,"update id = "+patch.getId());
			Log.d(TAG,"update name = "+patch.getName());
			Log.d(TAG,"update version = "+patch.getVersion());
			Log.d(TAG,"update md5 = "+patch.getMD5());
			Log.d(TAG,"update size in kb = "+patch.getSizeInKB());
			Log.d(TAG,"update url = "+patch.getURL());
			Log.d(TAG,"update filesize = "+patch+"KB");
			Log.d(TAG,"update attr = "+patch.getAttribute());
		}
	}
	
	public boolean checkPatchVersion(String id) {
		String filename = Patch.PATCH_PATH + id + ".txt";
		Log.d(TAG,"Patch file = "+filename);
		File file = new File(filename);
		if(file.exists()){
			return false;
		}
		return true;
	}

    public int checkFirmwareVersion(String version) {
        String current_version = Build.ID;
        Log.d(TAG, " checkFirmwareVersion()  current_version = " + current_version
                + " Server version = " + version);
        if (current_version.equals(version.trim())) {
            return 0;
        } else if (current_version.compareTo(version.trim()) < 0) {
            return 1;
        }
        return -1;
    }
	
    public boolean checkSDCard() {
        if (Environment.getExternalStorage2State().equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            String str = mContext.getString(R.string.update_no_sdcard);
            sendBroadcast(mContext, MSG_TITLE, str, true);
            return false;
        }
    }
    
    // add by wuhao in 2012-11-16 begin
    public boolean checkMemory(Long size) {
        String str;
        long free_size = 0;
        free_size = mFileUtils.getFreeSpaceInKB(FileUtils.SD_PARH);
        Log.d(TAG, " checkMemory size = " + size + " free_size = " + free_size);
        if (free_size > size) {
            UpdateData.Path = FileUtils.SD_PARH;
        } else {
            str = mContext.getString(R.string.memory_no_space);
            sendBroadcast(mContext, MSG_TITLE, str, true);
            return false;
        }
        return true;
    }
    /**
     * 检查指定路径剩余空间
     * @param size 所需要的磁盘空间
     * @param path 指定路径
     * @return 返回检查结果 true 标示有空间
     */
    public boolean checkMemory(Long size, String path) {
        String str;
        long free_size = 0;
        free_size = mFileUtils.getFreeSpaceInKB(path);
        Log.d(TAG, " checkMemory path = " + path + " size = " + size + " free_size = " + free_size);
        if (free_size > size) {
            UpdateData.Path = path;
        } else {
            return false;
        }
        return true;
    }
    // add by wuhao in 2012-11-16 end
	
//	public boolean checkMemory(Long size){
//	    Log.d(TAG, " checkMemory ");
//		String str;
//		long free_size = 0;
//    	if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
//    		free_size = mFileUtils.getFreeSpaceInKB(FileUtils.SD_PARH);
//    		if(free_size > size){
//    			UpdateData.Path = FileUtils.SD_PARH;
//    		}else{
//    			free_size = mFileUtils.getFreeSpaceInKB(FileUtils.INTERNAL_MEMORY_PATH);
//    			if(free_size >= size) {
//    				UpdateData.Path = FileUtils.INTERNAL_MEMORY_PATH;					
//				}
//				else {
//		    		str = mContext.getString(R.string.memory_no_space);
//		    		sendBroadcast(mContext,MSG_TITLE,str,true);
//					return false;
//				}
//    		}
//    	}else{
//    		/*
//			free_size = mFileUtils.getFreeSpaceInKB(FileUtils.INTERNAL_MEMORY_PATH);
//			if(free_size > size) {
//				UpdateData.Path = FileUtils.INTERNAL_MEMORY_PATH;				
//			}
//			else {
//				return false;
//			}   
//			*/
//
//    		str = mContext.getString(R.string.update_no_sdcard);
//    		sendBroadcast(mContext,MSG_TITLE,str,true);
//    		return false;
//    	}
//    	
//    	return true;
//	}
	
	public boolean checkVersion(String version){
		String current_version = Build.ID;
		if(current_version.compareTo(version) < 0){
			return true;
		}
		return false;
	}
	
	public boolean checkMD5(String file,String md5){
		MD5 mMD5 = new MD5();
		String md5_str = mMD5.md5sum(file);
		if(md5_str == null) {
			Log.e(TAG, "get download file md5 string error!");
			return false;
		}
		else {
			if(UpdateStatus.DEBUG)
				Log.d(TAG, "file md5:" + md5_str + " and url md5:" + md5);
			if(md5_str.equalsIgnoreCase(md5)) {
				return true;
			}
			else {
				Log.e(TAG, "the download file md5 string is not equal string from network!");
				return false;
			}
		}
	}
	
    public void startUpdateService() {
        Log.d(TAG, "UpdateService.class-->" + attr);
        if (UpdateData.Path == null) {
            Log.d(TAG, " UpdateData.Path = " + UpdateData.Path);
            return;
        }
        Intent service_intent = new Intent(mContext, UpdateService.class);
        service_intent.putExtra("url", UpdateData.URL);
        service_intent.putExtra("name", UpdateData.Name);
        service_intent.putExtra("md5", UpdateData.Md5);
        service_intent.putExtra("path", UpdateData.Path);
        service_intent.putExtra("size", UpdateData.SizeInKb);
        service_intent.putExtra("attr", UpdateData.attr);
        mContext.startService(service_intent);
        // }
    }
	
	public void stopUpdateService(){
    	Intent service_intent = new Intent(mContext,
    								UpdateService.class);
    	mContext.stopService(service_intent);
	}
	
	public void sendBroadcast(Context context, int type, String msg, boolean isError){
		UpdateStatus.setError(isError);
		Log.d(TAG, " type = " + type + "msg = " + msg + " isError = " + isError);
		switch(type){
		case MSG_TITLE:
			UpdateStatus.setTitleMsg(msg);
			break;
		case MSG_DOWNLOAD:
			UpdateStatus.setDownloadMsg(msg);
			break;
		case MSG_CHECK:
			UpdateStatus.setCheckMsg(msg);
			break;
//		default:
//		    UpdateStatus.clearUpdateStatus();
//		    break;
		}
		UpdateStatus.sendBroadcastReceiver(context, UpdateStatus.UPDATE_MSG_CHANGE_ACTION);
	}
	
	// 获取有限mac地址
	private String getMac() {
		String mac;
		if (SystemProperties.get("ubootenv.var.ethaddr", "") != null) {
			mac = SystemProperties.get("ubootenv.var.ethaddr", "");
		} else {
			WifiManager wifi = (WifiManager) mContext.getSystemService(
					Context.WIFI_SERVICE);
			WifiInfo info = wifi.getConnectionInfo();
			mac = info.getMacAddress();
		}
		return mac;
	}
	
    private boolean deleteDirectory(String path) {
        boolean isDelete = false;
        File file = new File(path);
        File[] fileList = file.listFiles();
        Log.d(TAG, " deleteDirectory path = " + path);
        String dirPath = null;
        File temp = null;
        if (fileList != null) {
            for (int i = 0; i < fileList.length; i++) {
                temp = new File(fileList[i].getPath());
                Log.d(TAG, " getPath = " + fileList[i].getPath());
                Log.d(TAG, " temp.isFile() = " + temp.isFile());
                if (temp.isFile()) {
                    if (temp.delete()) {
                        isDelete = true;
                    }
                }
                if (temp.isDirectory()) {
                    dirPath = temp.getPath();
                    if (!dirPath.equalsIgnoreCase("/cache/lost+found")) {
                        if (deleteDirectory(dirPath)) {
                            isDelete = true;
                        }
                    }
                }
            }
        }
//        if (file.delete()) {
//            isDelete = true;
//        }
        return isDelete;
    }
    
    /**
     * 删除文件夹及下面所有文件
     * @param folderPath
     */
    
    public void delFolder(String folderPath) {
        try {
            delAllFile(folderPath); // 删除完里面所有内容
            String filePath = folderPath;
            filePath = filePath.toString();
            java.io.File myFilePath = new java.io.File(filePath);
            myFilePath.delete(); // 删除空文件夹
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 删除文件夹下所有文件
     * @param path
     * @return
     */
    public boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        if (tempList.length == 1 && tempList[0].equalsIgnoreCase("lost+found")) {
            flag = true;
            return flag;
        }
        for (int i = 0; i < tempList.length; i++) {
            if (tempList[i].equalsIgnoreCase("lost+found")) {
                continue;
            }
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);// 先删除文件夹里面的文件
                delFolder(path + "/" + tempList[i]);// 再删除空文件夹
                flag = true;
            }
        }
        return flag;
    }
}