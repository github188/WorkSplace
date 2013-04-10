package com.joysee.adtv.common;
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
	private static final String TAG = "VideoLayerUtils";
	private static String VIDEO_ROTATE_DEV = "/sys/class/ppmgr/angle";
	private static String VIDEO_AXIS_DEV = "/sys/class/video/axis";
	private static String VIDEOLAYER_DEV = "/sys/class/video/disable_video";
	private static String VIDEO_SCREEN_DEV = "/sys/class/video/screen_mode";
	private static String VIDEO_SCREEN = "/sys/class/display/mode";
	
	//video screen_mode
	public static final int VSCREEN_NORMAL = 0;
	public static final int VSCREEN_FULLSTRETCH = 1;
	public static final int VSCREEN_RATIO4_3 = 2;
	public static final int VSCREEN_RATIO16_9 = 3;
		
	public static boolean setVideoLayerLayer(boolean isOn){
		File file = new File(VIDEOLAYER_DEV);
		if (!file.exists()) {
			Log.e(TAG,"sysfs device: "+VIDEOLAYER_DEV+" can't access!");
        	return false;
        }
		if(isOn){
			try
			{
				BufferedWriter out = new BufferedWriter(new FileWriter(VIDEOLAYER_DEV),32);
	    		try
	    		{
	    			out.write("2");    
	    			Log.d(TAG, "Enable videolayer");
	    		} finally {
					out.close();
				}				
			}
			catch (IOException e) {

				e.printStackTrace();
				Log.e(TAG, "IOException when write "+VIDEOLAYER_DEV);
				return false;
			}
		}else{
	    	String ifDisable = null;
			try
			{
				BufferedReader in = new BufferedReader(new FileReader(VIDEOLAYER_DEV),32);
				try
				{
					ifDisable = in.readLine();					
				} finally {
					in.close();
	    		} 
				if (ifDisable.equals("2"))
				{
					Log.d(TAG, "VideoLayer is disable.");	
					return true;
				}				
			}
			catch (IOException e) {
				e.printStackTrace();
				Log.e(TAG, "IOException when read "+VIDEOLAYER_DEV);
				return false;
			} 
			
			//write
			try
			{
				BufferedWriter out = new BufferedWriter(new FileWriter(VIDEOLAYER_DEV), 32);
	    		try
	    		{
	    			out.write("2");    
	    			Log.d(TAG, "Disable VideoLayer");
	    		} finally {
					out.close();
				}				
			}
			catch (IOException e) {

				e.printStackTrace();
				Log.e(TAG, "IOException when write "+VIDEOLAYER_DEV);
				return false;
			}
		}
		return true;
		
	}
	public static boolean setVideoRotateAngle(int angle){
	
    	String buf = null;
    	String angle_str = null;
		File file = new File(VIDEO_ROTATE_DEV);
		if (!file.exists()) {   
			Log.d(TAG,"sysfs device: "+VIDEO_ROTATE_DEV+" can't access!");
        	return false;
        }
		
		//read
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(VIDEO_ROTATE_DEV), 32);
			try
			{
				angle_str = in.readLine();
				Log.d(TAG, angle_str);
				if(angle_str.startsWith("current angel is ")) {
	                String temp = angle_str.substring(17, 18);
	                Log.d(TAG, "current angle is " + temp);
					if((temp != null) && (angle != Integer.parseInt(temp))){
						buf = Integer.toString(angle);
						Log.d(TAG,buf);
					}
				}
			} finally {
    			in.close();
    		} 
		}
		catch (IOException e) {

			e.printStackTrace();
			Log.e(TAG, "IOException when read " + VIDEO_ROTATE_DEV);
		} 
		if(buf == null) {
			return false;
		}
		//write
		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(VIDEO_ROTATE_DEV), 32);
    		try
    		{
    			Log.d(TAG, "write :"+buf);
    			out.write(buf);
    		} finally {
				out.close();
			}
			return true;
		}
		catch (IOException e) {			
			e.printStackTrace();
			Log.e(TAG, "IOException when write " + VIDEO_ROTATE_DEV);
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
			BufferedWriter out = new BufferedWriter(new FileWriter(VIDEO_AXIS_DEV), 32);
    		try
    		{
    			out.write(buf);    
    			Log.d(TAG, "set video window as:"+buf);
    		} finally {
				out.close();
			}			
		}
		catch (IOException e) {
			Log.e(TAG, "IOException when write "+VIDEO_AXIS_DEV);
			return false;
		}
		return true;
	}	
	public static boolean setVideoScreenMode(int mode)
	{

		File file = new File(VIDEO_SCREEN_DEV);
		if (!file.exists()) {    
			Log.e(TAG,"file: "+VIDEO_SCREEN_DEV+" not exists");
        	return false;
        }
		
		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(VIDEO_SCREEN_DEV), 32);
    		try
    		{
//    			out.write(mode);    
    		    // modify by dingran 20120417 must type of String
    			out.write(""+mode);
    			Log.d(TAG, "set Screen Mode to:"+mode);
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
		File file = new File(VIDEO_SCREEN_DEV);
		if (!file.exists()) {        	
        	return 0;
        }
		
		String mode = null;
		int ret = 0;
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(VIDEO_SCREEN_DEV), 32);
			try
			{
				mode = in.readLine();
				Log.d(TAG, "The current Screen Mode is :"+mode);
				mode = mode.substring(0, 1);
				Log.d(TAG, "after substring is :"+mode);
				ret = Integer.parseInt(mode);
				Log.d(TAG, "after parseInt is :"+ret);
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
	 
   public static String  getVideoScreen()
    {
        File file = new File(VIDEO_SCREEN);
        if (!file.exists()) {           
            return null;
        }
        
        String mode = null;
        try
        {
            BufferedReader in = new BufferedReader(new FileReader(VIDEO_SCREEN), 32);
            try
            {
                mode = in.readLine();
                Log.d(TAG, "The current Screen Mode is :"+mode);
            } finally {
                in.close();
            }
            return mode;
        }
        catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "IOException when setScreenMode ");
            return null;
        }
    }
	
}
