package com.joysee.launcher.utils;
//package com.amlogic.amlsys;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.util.Log;
/**
 * @author 2011.12 05 by peter
 *
 */
public class VideoLayerUtils {
	private static final String TAG = "com.joysee.launcher.utils.VideoLayerUtils";
	private static String video_rotate_dev = "/sys/class/ppmgr/angle";
	private static String video_axis_dev = "/sys/class/video/axis";
	private static String videolayer_dev = "/sys/class/video/disable_video";
	private static String video_screen_dev = "/sys/class/video/screen_mode";
	
	//video screen_mode
	public static final int VSCREEN_NORMAL = 0;
	public static final int VSCREEN_FULLSTRETCH = 1;
	public static final int VSCREEN_RATIO4_3 = 2;
	public static final int VSCREEN_RATIO16_9 = 3;
		
	public static boolean setVideoLayerLayer(boolean isOn){
		File file = new File(videolayer_dev);
		if (!file.exists()) {
			Log.e(TAG,"sysfs device: "+videolayer_dev+" can't access!");
        	return false;
        }
		if(isOn){
			try
			{
				BufferedWriter out = new BufferedWriter(new FileWriter(videolayer_dev),32);
	    		try
	    		{
	    			out.write("2");    
	    			LauncherLog.log_D(TAG, "Enable videolayer");
	    		} finally {
					out.close();
				}				
			}
			catch (IOException e) {

				e.printStackTrace();
				Log.e(TAG, "IOException when write "+videolayer_dev);
				return false;
			}
		}else{
	    	String ifDisable = null;
			try
			{
				BufferedReader in = new BufferedReader(new FileReader(videolayer_dev),32);
				try
				{
					ifDisable = in.readLine();					
				} finally {
					in.close();
	    		} 
				if (ifDisable.equals("2"))
				{
					LauncherLog.log_D(TAG, "VideoLayer is disable.");	
					return true;
				}				
			}
			catch (IOException e) {
				e.printStackTrace();
				Log.e(TAG, "IOException when read "+videolayer_dev);
				return false;
			} 
			
			//write
			try
			{
				BufferedWriter out = new BufferedWriter(new FileWriter(videolayer_dev), 32);
	    		try
	    		{
	    			out.write("2");    
	    			LauncherLog.log_D(TAG, "Disable VideoLayer");
	    		} finally {
					out.close();
				}				
			}
			catch (IOException e) {

				e.printStackTrace();
				Log.e(TAG, "IOException when write "+videolayer_dev);
				return false;
			}
		}
		return true;
		
	}
	public static boolean setVideoRotateAngle(int angle){
	
    	String buf = null;
    	String angle_str = null;
		File file = new File(video_rotate_dev);
		if (!file.exists()) {   
			LauncherLog.log_D(TAG,"sysfs device: "+video_rotate_dev+" can't access!");
        	return false;
        }
		
		//read
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(video_rotate_dev), 32);
			try
			{
				angle_str = in.readLine();
				LauncherLog.log_D(TAG, angle_str);
				if(angle_str.startsWith("current angel is ")) {
	                String temp = angle_str.substring(17, 18);
	                LauncherLog.log_D(TAG, "current angle is " + temp);
					if((temp != null) && (angle != Integer.parseInt(temp))){
						buf = Integer.toString(angle);
						LauncherLog.log_D(TAG,buf);
					}
				}
			} finally {
    			in.close();
    		} 
		}
		catch (IOException e) {

			e.printStackTrace();
			Log.e(TAG, "IOException when read " + video_rotate_dev);
		} 
		if(buf == null) {
			return false;
		}
		//write
		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(video_rotate_dev), 32);
    		try
    		{
    			LauncherLog.log_D(TAG, "write :"+buf);
    			out.write(buf);
    		} finally {
				out.close();
			}
			return true;
		}
		catch (IOException e) {			
			e.printStackTrace();
			Log.e(TAG, "IOException when write " + video_rotate_dev);
			return false;
		}
	}
	public static boolean setVideoWindow(int x_pos,int y_pos,int width,int height){
		String buf;		
//		buf = x_pos+" "+y_pos+" "+(x_pos+width)+" "+(y_pos+height); 
		// modify by dingran 20120417 must have ","
		buf = x_pos+","+y_pos+","+(x_pos+width)+","+(y_pos+height); 
		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(video_axis_dev), 32);
    		try
    		{
    			out.write(buf);    
    			LauncherLog.log_D(TAG, "set video window as:"+buf);
    		} finally {
    			out.flush();
				out.close();
			}			
		}
		catch (IOException e) {
			Log.e(TAG, "IOException when write "+video_axis_dev);
			return false;
		}
		return true;
	}	
	public static boolean setVideoScreenMode(int mode)
	{

		File file = new File(video_screen_dev);
		if (!file.exists()) {    
			Log.e(TAG,"file: "+video_screen_dev+" not exists");
        	return false;
        }
		
		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(video_screen_dev), 32);
    		try
    		{
//    			out.write(mode);    
    		    // modify by dingran 20120417 must type of String
    			out.write(""+mode);
    			LauncherLog.log_D(TAG, "set Screen Mode to:"+mode);
    		} finally {
				out.close();
			}
			 
		}
		catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "IOException when setScreenMode ");
			return false;
		}
		
		return true;
	}	
	public static int getVideoScreenMode()
	{
		File file = new File(video_screen_dev);
		if (!file.exists()) {        	
        	return 0;
        }
		
		String mode = null;
		int ret = 0;
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(video_screen_dev), 32);
			try
			{
				mode = in.readLine();
				LauncherLog.log_D(TAG, "The current Screen Mode is :"+mode);
				mode = mode.substring(0, 1);
				LauncherLog.log_D(TAG, "after substring is :"+mode);
				ret = Integer.parseInt(mode);
				LauncherLog.log_D(TAG, "after parseInt is :"+ret);
			} finally {
				in.close();
    		}
			return ret;
		}
		catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "IOException when setScreenMode ");
			return 0;
		}
	}
	
}
