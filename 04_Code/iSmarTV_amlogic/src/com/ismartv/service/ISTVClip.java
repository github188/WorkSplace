package com.ismartv.service;

import java.net.URL;
import org.json.JSONObject;
import android.util.Log;

import com.ismartv.api.AccessProxy;
import com.ismartv.bean.ClipInfo;
import com.ismartv.ui.ISTVVodApplication;
import com.ismartv.ui.ISTVVodHome;
import java.net.NetworkInterface;
public class ISTVClip{
	private static final String TAG="ISTVClip";
	public int     pk;
//	public URL     urls[]=new URL[ISTVItem.QUALITY_COUNT];
	public String     urls[]=new String[ISTVItem.QUALITY_COUNT];
	static String mac=null;
	static synchronized String getMACAddress(){
		if(mac==null){
			try{
				byte addr[];
				addr=NetworkInterface.getByName("eth0").getHardwareAddress();
				mac="";
				for(int i=0; i<6; i++){
					mac+=String.format("%02X",addr[i]);
				}
			}catch(Exception e){
				mac = "00112233445566";
			}
		}
//		return "000102650475";
		return mac;
	}
	private void parseURL(JSONObject obj, String name, int quality){
		String str;

		str = obj.optString(name);		
			
		if(str!=null && !str.equals("") && !str.equals("null")){
			try{				
				//urls[quality] = new URL(str);
				urls[quality] = str;
				Log.d(TAG, "str="+str+",name="+name+",urls="+urls[quality].toString());
			}catch(Exception e){
				Log.d(TAG, "urls error");
			}
		}
	}

	public ISTVClip(int pk, JSONObject obj) throws Exception{
		this.pk = pk;		
		/*
		Log.d(TAG, "JSONObject obj="+obj.toString());
		parseURL(obj, "adaptive", ISTVItem.QUALITY_ADAPTIVE);
		parseURL(obj, "low", ISTVItem.QUALITY_LOW);
		parseURL(obj, "high", ISTVItem.QUALITY_HIGH);
		parseURL(obj, "medium", ISTVItem.QUALITY_MEDIUM);
		parseURL(obj, "ultra", ISTVItem.QUALITY_ULTRA);
		parseURL(obj, "normal", ISTVItem.QUALITY_NORMAL);
		*/
		AccessProxy.init("A11C", "1.0", getMACAddress());
		ClipInfo cinfo = AccessProxy.parse("http://cord.tvxio.com/api/clip/"+pk+"/",
					ISTVService.getService().getEpg().getAccessToken(), ISTVVodApplication.getContext());
		urls[ISTVItem.QUALITY_ADAPTIVE] = cinfo.getAdaptive();
		urls[ISTVItem.QUALITY_LOW] = cinfo.getLow();
		urls[ISTVItem.QUALITY_MEDIUM] = cinfo.getMedium();
		urls[ISTVItem.QUALITY_NORMAL] = cinfo.getNormal();
		urls[ISTVItem.QUALITY_HIGH] = cinfo.getHigh();
		urls[ISTVItem.QUALITY_ULTRA] = cinfo.getUltra();
		for(String url : urls){
			Log.d(TAG, "url="+url);
		}
	}
	public ISTVClip(int pk,String[] url){
	    this.pk = pk;
	    urls=url;
	}
}
