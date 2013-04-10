package com.bestv.ott.appstore.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

/***
 * 文件图片，相关的操作
 * @author Administrator
 *
 */
public class FileUtils {
    
    private final static String TAG="com.supertv.utils.FileUtils";
    
    /**
     * 将Bitmap转成图片保存
     * @param bmp
     * @param filePath
     */
    public static void saveBitmap(final Bitmap bmp,final String filePath,String format){
        //AppLog.log_D(TAG,"----saveBitmap----byte="+bmp.getByteCount());
        new Thread(new Runnable() {
            public void run() {
                File file = new File(filePath);
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    if (bmp.compress(Bitmap.CompressFormat.JPEG, 90, out)) {
                        out.flush();
                        out.close();
                        //AppLog.log_D(TAG,"------end Bitmap");
                    }
                } catch (FileNotFoundException e) {
                    System.out.println("FileNotFoundException");
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println("FileNotFoundException");
                    e.printStackTrace();
                }
            }
        }).start();
    }
    
    /**
     * Drawableת转成Bitmap 
    */
    public static Bitmap drawableToBitmap(Drawable drawable) {  
        if(null==drawable){
            AppLog.d(TAG,"---drawable is null");  
            return null;
        }
        BitmapDrawable bd = (BitmapDrawable) drawable;
        return bd.getBitmap();
     } 
    
    public static Drawable resTODrawable(int resId,Context con){
    	Resources res = con.getResources();
    	Bitmap bmp = BitmapFactory.decodeResource(res, resId);
    	return  new BitmapDrawable(res, bmp);
    }
    
    /**
     * bytes转成Bitmap
     * @param bytes
     * @return
     */
    public static Bitmap byteToBitmap(byte[] bytes){
        if(null==bytes||bytes.length<1){
            AppLog.d(TAG,"---bytes is null");  
            return null;
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
    }
    
	public static byte[] Bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}
	
	public static byte[] image2Bytes(String fileName){
		Bitmap bitmap=BitmapFactory.decodeFile(fileName);
		return Bitmap2Bytes(bitmap);
	}
    
    public static Drawable fileToDrawable(String src){
    	File imageE=new File(src);
    	if(!imageE.isFile()||!imageE.exists()){
    		return null;
    	}
    	InputStream is = null;
    	try {
    		is = new FileInputStream(src);
    		BitmapFactory.Options options=new BitmapFactory.Options();
    		Bitmap bitmap =BitmapFactory.decodeStream(is,null,options);
    		if(bitmap==null||bitmap.getByteCount()<0||bitmap.getRowBytes()<0){
    			File icon=new File(src);
    			if(icon.exists()){
    				icon.delete();
    			}
    			AppLog.e(TAG, "------ spreeIcon is null -------");
    			return null;
    		}else{
    			BitmapDrawable drawable=new BitmapDrawable(zoomBitmap(bitmap));
    			AppLog.e(TAG, "------ return drawable -------");
    			return drawable;
    		}
		} catch (Exception e) {
			AppLog.e(TAG, "------ spreeIcon is null -------");
			return null;
		}
    }
    
    public static boolean delete(String src){
        File delFile=new File(src);
        if(delFile.exists()){
            delFile.delete();
            return true;
        }else{
            AppLog.d(TAG, "delete fail src="+src);
        }
        return false;
    }
    
    
    public static Bitmap zoomBitmap(Bitmap target){
		
		int TARGET_WIDTH = 45;
		int TARGET_HEIGHT = 45;
		int width = target.getWidth();
		int height = target.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidth = ((float)TARGET_WIDTH)/ width;
		float scaleHeight = ((float)TARGET_HEIGHT)/ height;
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap result = Bitmap.createBitmap(target, 0, 0, width, height, matrix, true);   
		return result;
    }
    
//    public static Drawable zoomDrawable(Drawable drawable){
//    	int TARGET_WIDTH = 64;
//		int TARGET_HEIGHT = 64;
//		int width = drawable.getIntrinsicWidth();
//		int height = drawable.getIntrinsicHeight();
//		Bitmap b = drawableToBitmap(drawable);
//		Matrix matrix = new Matrix();
//		float scaleWidth = ((float)TARGET_WIDTH)/ width;
//		float scaleHeight = ((float)TARGET_HEIGHT)/ height;
//		matrix.postScale(scaleWidth, scaleHeight);
//		Bitmap result = Bitmap.createBitmap(b, 0, 0, width, height, matrix, true);
//		return new BitmapDrawable(result);
//    }
}

