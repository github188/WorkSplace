package com.bestv.ott.appstore.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

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
        Bitmap bitmap=BitmapFactory.decodeFile(src);
        return new BitmapDrawable(bitmap);
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
    
   /* *//**
     * @param newVersion
     * @param oldVersion
     * @return 版本比较结果
     *//*
    public boolean CompareVersion(String nowVersion,String oldVersion){
    	//换成不带点的字符串
    	String now = nowVersion.replace(".", "");
    	String old = oldVersion.replace(".", "");
    	if(now.length()>old.length()){
    		int len = now.length()-old.length();
    		for(int i=0;i<len;i++){
    			old = old.concat("0");//在字符串右侧补0
    		}
    	}else if(now.length()<old.length()){
    		int len = old.length()-now.length();
    		for(int i=0;i<len;i++){
    			now = now.concat("0");//在字符串右侧补0
    		}
    	}
    	if(Integer.parseInt(now)>Integer.parseInt(old)){
    		return true;
    	}else{
    		return false;
    	}    	
    }
*/
}

