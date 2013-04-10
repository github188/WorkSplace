package com.lenovo.settings.Util;

import java.io.File;
import java.io.FileReader;

import com.lenovo.settings.update.UpdateStatus;

import android.util.Log;

public class SerialNumber {
	private static final String TAG = "SerialNumber";
	private final static String CONFIG_PATH = "/sys/class/efuse/userdata";
	private final static String SAMPLE_NUMBER = "7013020411600033";
	//private final static String PUBLIC_ON_NUMBER = "701302";
	private File mFile = null;
	private int sample_len= 0;
	public SerialNumber() {
		mFile = new File(CONFIG_PATH);
		sample_len = SAMPLE_NUMBER.length();
	}

	public String getSerialNumber() {
		String r_string = null;
		int len = 0;
		if(!mFile.exists()) {
			return null;
		}
		
		try {
			FileReader fr = new FileReader(mFile);
			char[] buf = new char[100];
			
			len = fr.read(buf);
			
			r_string = new String(buf);
			fr.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		if(UpdateStatus.DEBUG)
			Log.d(TAG, "len" + Integer.toString(len)
				+", sample_len" + Integer.toString(sample_len));
		
		if((r_string != null) && (len >= sample_len)) {
			r_string = r_string.substring(0, sample_len);
			if(r_string != null) {
				if(UpdateStatus.DEBUG)
					Log.d(TAG, "SAMPLE_NUMBER:"+r_string);
				if(CheckSerialNumber(r_string)) {
					return r_string;
				}
			}
		}
		else {
			Log.e(TAG, "get serial number is null!");
		}
		return null;
	}
	
	public boolean CheckSerialNumber(String serialnumber) {
		if(serialnumber == null)
			return false;
		    
		int count = serialnumber.length();
		char temp;
		if(count <=  0)
			return false;
		
		for(int i=0; i<count ; i++) {
			temp = serialnumber.charAt(i);
			
			if((temp < 0x20) || (temp > 0x7e)){
				Log.e(TAG, "This serial number is not whole all digital!");
				return false;
			}
		}
		
		return true;
	}
}
