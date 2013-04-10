package com.lenovo.settings.EnforceUpdate;

import java.io.File;
import java.util.List;

import com.lenovo.settings.R;
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
import com.lenovo.settings.update.UpdateStatus;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.SystemProperties;
import android.util.Log;

public class EnforceUpdateCheck {
	
	private static final String TAG = "EnforceUpdateCheck";
	public static final int MSG_TITLE = 0;
	public static final int MSG_DOWNLOAD = 1;
	public static final int MSG_CHECK = 2;
	private Context mContext;
	private UpdateHttpClient mUpdateHttpClient = null;
	private FileUtils mFileUtils;
	String attr;

	public EnforceUpdateCheck(Context context){
		mContext = context;
		mFileUtils = new FileUtils();
	}
	
	boolean checkNetwordConnected(){

        ConnectivityManager conManager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo[] networkInfo = conManager.getAllNetworkInfo();

        if(networkInfo != null)
        {

            for (int i = 0; i < networkInfo.length; i++)
            {
                if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED)
                {
                     Log.i("isConnectInternet", "============isConnectInternet============== i = "+i);
                    return true;
                }
            }
        }
		return false;
	}
	
	public boolean checkUpdate(){
		String str;
		String serial_number;
		String url;
		String mUrl;
		Object obj = null;
		if(!checkNetwordConnected()){
			str = mContext.getString(R.string.network_disconnection);
			sendBroadcast(mContext,MSG_TITLE,str,true);
			return false;
		}	
		serial_number = gerSerialNumber();
		Log.d(TAG,"Serial number = "+serial_number);
	
		url = getUrl();
		
		//url typehttp://192.168.11.172:8080/interfaceshop/app/upgrade?Action=Android&DeviceId=00:01:02:65:04:75 whj20120607
		mUrl = url+"?Action=Android&DeviceId="+getMac();
		str = mContext.getString(R.string.connect_service);
		sendBroadcast(mContext,MSG_TITLE,str,false);
		mUpdateHttpClient  = new UpdateHttpClient();
		Log.d(TAG,"update url = "+mUrl);
		System.out.println("enforcmUrl+++"+mUrl);
		obj = mUpdateHttpClient.getServerData(mUrl);
		if(obj == null){
//			str = mContext.getString(R.string.connect_service_fail);
//			sendBroadcast(mContext,MSG_TITLE,str,true);
			return false;
		}else if(obj instanceof Error){
//			str = mContext.getString(R.string.connect_service_fail);
//			sendBroadcast(mContext,MSG_TITLE,str,true);
			return false;
		}else if(!(obj instanceof Versions)){
//			str = mContext.getString(R.string.not_new_version);
//			sendBroadcast(mContext,MSG_TITLE,str,true);
			return false;
		}
		
		Versions versions = (Versions) obj;
		if(!checkVersion(versions)){
			str = mContext.getString(R.string.not_new_version);
			sendBroadcast(mContext,MSG_TITLE,str,true);
			return false;
		}
		str = mContext.getString(R.string.found_new_version)+UpdateData.Version;		
		sendBroadcast(mContext,MSG_TITLE,str,false);
		
		int filesize = mUpdateHttpClient.getDownloadSizeByURL(UpdateData.URL) / 1024;
		if((UpdateData.SizeInKb != filesize) && (filesize != 0)){
			UpdateData.SizeInKb = filesize;
		}
		
		if(!checkMemory(UpdateData.SizeInKb)){
			//str = mContext.getString(R.string.memory_no_space);
			//sendBroadcast(mContext,MSG_DOWNLOAD,str,true);
			return false;			
		}
		
//		str = mContext.getString(R.string.update_downloading,"0%");
//		sendBroadcast(mContext,MSG_DOWNLOAD,str,false);
		return true;
	}
	
	public boolean checkLocalUpdate(){
		String str;
		String path;
		File file;
		UpdateData.clearUpdateData();
		if(Environment.getExternalStorage2State().equals(Environment.MEDIA_MOUNTED)){
			path = "/mnt/sdcard/update.zip";// Rony modify 20120425
			file = new File(path);// Rony modify 20120425
			if(file.exists()){
				if(Recovery.verifyPackage(file)){
					UpdateData.Path = path;
					return true;
				}
			}
		}
		str = mContext.getString(R.string.not_new_version);
		sendBroadcast(mContext,MSG_TITLE,str,true);
		return false;
	}
	
	private String getUrl() {		
		return SystemProperties.get("online.ip",UpdateHttpClient.URL);
	}

	private String gerSerialNumber() {
		SerialNumber mSerialNumber = new SerialNumber();
		return mSerialNumber.getSerialNumber();
	}

	private boolean checkVersion(Versions versions) {

		int ret = -1;
		int i,count;
		
		List<Version> list = versions.getVersions();
		count = list.size();
		if(count < 1){
			return false;
		}
		for(i=0;i<count;i++){
			Firmware firmware = list.get(i).getFirmware();
			if(firmware == null){
				continue;
			}
			String id = firmware.getId();
			attr = firmware.getAttribute();
			ret = checkFirmwareVersion(firmware.getId()); 
			if(ret == 1){
				if((attr.equals(UpdateStatus.UPDATE_ATTRIBUTE_ENFORCE)) && (i == (count - 1))){//update by yuhongkun 20120829 
					setUpdataData(firmware);
					return true;
				}
			}else if((ret == 0) && (i == (count - 1))){
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
				if((attr.equals(UpdateStatus.UPDATE_ATTRIBUTE_ENFORCE)) || (i == (count - 1))){
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
		if(UpdateStatus.DEBUG){
			Log.d(TAG,"update id = "+firmware.getId());
			Log.d(TAG,"update name = "+firmware.getName());
			Log.d(TAG,"update version = "+firmware.getVersion());
			Log.d(TAG,"update md5 = "+firmware.getMD5());
			Log.d(TAG,"update size in kb = "+firmware.getSizeInKB());
			Log.d(TAG,"update url = "+firmware.getURL());
			Log.d(TAG,"update filesize = "+firmware+"KB");
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
		if(UpdateStatus.DEBUG){
			Log.d(TAG,"update id = "+patch.getId());
			Log.d(TAG,"update name = "+patch.getName());
			Log.d(TAG,"update version = "+patch.getVersion());
			Log.d(TAG,"update md5 = "+patch.getMD5());
			Log.d(TAG,"update size in kb = "+patch.getSizeInKB());
			Log.d(TAG,"update url = "+patch.getURL());
			Log.d(TAG,"update filesize = "+patch+"KB");
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

	public int checkFirmwareVersion(String version){
		String current_version = Build.ID;
		
		if(current_version.equals(version.trim())){
			return 0;
		}else if(current_version.compareTo(version.trim()) < 0){
			return 1;
		}
		return -1;
	}

	public boolean checkMemory(Long size){
		String str;
		long free_size = 0;
    	if(Environment.getExternalStorage2State().equals(Environment.MEDIA_MOUNTED)){
    		free_size = mFileUtils.getFreeSpaceInKB(FileUtils.SD_PARH);
    		if(free_size > size){
    			UpdateData.Path = FileUtils.SD_PARH;
    		}else{
    			free_size = mFileUtils.getFreeSpaceInKB(FileUtils.INTERNAL_MEMORY_PATH);
    			if(free_size >= size) {
    				UpdateData.Path = FileUtils.INTERNAL_MEMORY_PATH;					
				}
				else {
		    		str = mContext.getString(R.string.memory_no_space);
		    		sendBroadcast(mContext,MSG_TITLE,str,true);
					return false;
				}
    		}
    	}else{
    		str = mContext.getString(R.string.update_no_sdcard);
    		sendBroadcast(mContext,MSG_TITLE,str,true);
    		return false;
    	}
    	
    	return true;
	}
	
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
	
	public void startUpdateService(){
		System.out.println("EnforUpdateService.class-->"+attr);
		if(attr.equals(UpdateStatus.UPDATE_ATTRIBUTE_ENFORCE)){
	    	Intent service_intent = new Intent(mContext,EnforceUpdateService.class);
	    	service_intent.putExtra("url", UpdateData.URL);
	    	service_intent.putExtra("name", UpdateData.Name);
	    	service_intent.putExtra("md5", UpdateData.Md5);
	    	service_intent.putExtra("path", UpdateData.Path);
	    	service_intent.putExtra("size", UpdateData.SizeInKb);
	    	mContext.startService(service_intent);
		}
	}
	
	public void stopUpdateService(){
    	Intent service_intent = new Intent(mContext,
    								EnforceUpdateService.class);
    	mContext.stopService(service_intent);
	}
	
	public void sendBroadcast(Context context, int type, String msg, boolean isError){
		UpdateStatus.clearUpdateStatus();
		UpdateStatus.setError(isError);
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
	
}