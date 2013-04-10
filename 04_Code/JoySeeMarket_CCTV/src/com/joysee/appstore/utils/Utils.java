package com.joysee.appstore.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.Shader.TileMode;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.joysee.appstore.R;
import com.joysee.appstore.common.Constants;
import com.joysee.appstore.common.TaskBean;
import com.joysee.appstore.common.TaskDownSpeed;
import com.joysee.appstore.db.DatabaseHelper.DownloadTaskColumn;
import com.joysee.appstore.db.DatabaseHelper.DownloadTaskColumnIndex;

public class Utils {
	
	public static final String TAG = "com.joysee.appstore.Utils";
	 
	public static boolean isURL( String str ){
		 if(str==null||str.trim().equals("")){
			 return false;
		 }
	        String regex = "http://(([a-zA-z0-9]|-){1,}\\.){1,}[a-zA-z0-9]{1,}-*" ;
	        return match( regex ,str );
	}
	 
	 /**判断字符串是否为数字*/
	 public static boolean isString(String str){
		  int temp = 0; 
		  try{
		      temp = Integer.parseInt(str);
		  }catch(Exception e){
		      return false;
		  }
		  return true;
	  }
	 
	 /** 
	     * @param regex 正则表达式字符串
	     * @param str   要匹配的字符串
	     * @return 如果str 符合 regex的正则表达式格式,返回true, 否则返回 false;
	     */
	 private static boolean match( String regex ,String str ){
	     Pattern pattern = Pattern.compile(regex);
	     Matcher  matcher = pattern.matcher( str );
	     return matcher.matches();
	 }
	 
	/**
	 * check sd card 
	 * @return  true  usefull false dismiss
	 */
	public static boolean checkSDcard(){
		String status = android.os.Environment.getExternalStorageState();
		if (!status.equals(Environment.MEDIA_MOUNTED)){
			return false;
		}else{
			return true;
		}
	}
	/**
	 * 获取sd path
	 * @return
	 */
	public static String getSDPath(){
	    File sdDir = null;
	    boolean sdCardExist = checkSDcard();
	    if(sdCardExist)
	    {                              
	        sdDir = Environment.getExternalStorageDirectory();//获取跟目录
	    }else{
	        return Constants.IMAGEROOT;
	    }
	    return sdDir.toString()+"/joysee/images/";
	}
	
	
	public static boolean checkConnect(String path){
		URL url;
		HttpURLConnection conn = null;
		try {
			url = new URL(path);
			conn = (HttpURLConnection)url.openConnection();
			conn.setConnectTimeout(Constants.TIMEOUT);
			int status = conn.getResponseCode();
			AppLog.d(TAG, "==========checkConnect()============status="+status);
			if((status / 100) != 2){
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}catch(Error e){
			e.printStackTrace();
			return false;
		}finally{
			if(conn!=null){
				conn.disconnect();
			}
		}
		return true;
	}
	
	public static void showTipToast(int gravity,Context con,String text){
		LayoutInflater inflater = LayoutInflater.from(con);   
		View view=inflater.inflate(R.layout.toast_layout, null);
		TextView textv=(TextView)view.findViewById(R.id.text_view);
		textv.setText(text);
		Toast toast = new Toast(con);
		toast.setGravity(gravity,0,0);
		toast.setView(view);
		toast.show();
	}
	
	public static void showInstallComToast(int gravity,Context con,String text){
		LayoutInflater inflater = LayoutInflater.from(con);   
		View view=inflater.inflate(R.layout.toast_installed_layout, null);
		TextView textv=(TextView)view.findViewById(R.id.text_view);
		textv.setText(text);
		Toast toast = new Toast(con);
		toast.setGravity(gravity,0,0);
		toast.setView(view);
		toast.show();
	}
	
	public static boolean isBreakpointCon(URL downUrl){
		boolean ret = true;
		HttpURLConnection http = null;
		try {
			http = (HttpURLConnection) downUrl.openConnection();
		if (null != http) {
			try {
				http.setRequestMethod("GET");
			} catch (ProtocolException e) {
				e.printStackTrace();
				Log.e(TAG, "=====setRequestMethod catch ProtocolException"+e.toString());
			}
			http.setRequestProperty("Range", "bytes=" +10+"-");
			int status = 0;
			try {
				status = http.getResponseCode();
			} catch (IOException e1) {
				e1.printStackTrace();
				Log.e(TAG, "=====http.getResponseCode() catch ProtocolException"+e1.toString());
			}
			if (AppStoreConfig.DOWNLOADDEBUG) {
				Log.e(TAG, "====================================status="+status);
			}
			if(status !=206){
				ret = false;
			}
		}
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "=====openConnection catch ioexception"+e.toString());
			ret = false ;
		}finally{
			if(null!=http){
				http.disconnect();
			}
		}
		return ret;
	}
	
	public static TaskBean cursorToTaskBean(Cursor cur){
		TaskBean tTaskBean = new TaskBean();
        int id = cur.getInt(cur.getColumnIndex(DownloadTaskColumn.ID));
        String name = cur.getString(cur.getColumnIndex(DownloadTaskColumn.APPNAME));
        String downloadDir = cur.getString(cur.getColumnIndex(DownloadTaskColumn.DOWNDIR));
        String finalFileName = cur.getString(cur.getColumnIndex(DownloadTaskColumn.FINALFILENAME));
        String tmpFileName = cur.getString(cur.getColumnIndex(DownloadTaskColumn.TMPFILENAME));
        int sumSize = cur.getInt(cur.getColumnIndex(DownloadTaskColumn.SUMSIZE));
        int status = cur.getInt(cur.getColumnIndex(DownloadTaskColumn.STATUS));
        String urlName = cur.getString(cur.getColumnIndex(DownloadTaskColumn.URL));
        String iconUrl = cur.getString(cur.getColumnIndex(DownloadTaskColumn.ICONURL));
        byte[] icon = cur.getBlob(cur.getColumnIndex(DownloadTaskColumn.ICON));
        String serAppID = cur.getString(DownloadTaskColumnIndex.SERAPPID);
   		String typeID = cur.getString(DownloadTaskColumnIndex.APPTYPEID);
   		String typeName = cur.getString(DownloadTaskColumnIndex.TYPENAME);
   		String version = cur.getString(DownloadTaskColumnIndex.VERSION);
   		String pkgName= cur.getString(DownloadTaskColumnIndex.PKGNAME);
        
        tTaskBean.setId(id);
        tTaskBean.setAppName(name);
        tTaskBean.setDownloadDir(downloadDir);
        tTaskBean.setFinalFileName(finalFileName);
        tTaskBean.setTmpFileName(tmpFileName);
        tTaskBean.setSumSize(sumSize);
        tTaskBean.setStatus(status);
        tTaskBean.setUrl(urlName);
        tTaskBean.setIconUrl(iconUrl);
        tTaskBean.setIcon(icon);
        tTaskBean.setSerAppID(serAppID);
        tTaskBean.setTypeID(typeID);
        tTaskBean.setTypeName(typeName);
        tTaskBean.setVersion(version);
        tTaskBean.setPkgName(pkgName);
        
		return tTaskBean;
	}
	/**
	 * @function 处理gridview失去焦点
	 * @param v
	 */
	public static void setFocus(View v){
		try {
			@SuppressWarnings("unchecked")
			Class<ListView> c = (Class<ListView>) Class
					.forName("android.widget.ListView");
			Method[] flds = c.getDeclaredMethods();
			for (Method f : flds) {
				if ("setSelectionInt".equals(f.getName())) {
					f.setAccessible(true);
					f.invoke(v,
							new Object[] { Integer.valueOf(-1) });
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    	/**
	 * @param size
	 * @return
	 */
	public static String transformByte(float size){
	    float sizeM = size/1024f/1024f;
	    if(sizeM<0){
	        float sizeK = size/1024f;
	        return String.valueOf((float)(Math.round(sizeK*100))/100)+" K";
	    }
	    return String.valueOf((float)(Math.round(sizeM*100))/100)+ " M";
	}
	
	public static boolean isNULL(String str){
		boolean ret = false;
		
		if(str==null||"null".equals(str.trim())||"".equals(str.trim())){
			ret = true; 	
		}
		return ret;
	}
	
	public static String getImagePath(){
		String path;
		if(!checkSDcard()){
			path=Constants.IMAGEROOT;
		}else{
			path=Utils.getSDPath()+"/joysee/images/";
		}
		File file=new File(path);
	        if(!file.exists()){
	            file.mkdirs();
	    }
		return path;
	}
	
	
	/**
     * 判断网络是否可用
     * @param context
     * @return
     */
    public static boolean checkNet(Context context) {  
        
        ConnectivityManager manager = (ConnectivityManager) context  
               .getApplicationContext().getSystemService(  
                      Context.CONNECTIVITY_SERVICE);  
        if (manager == null) {  
            return false;  
        }  
        NetworkInfo networkinfo = manager.getActiveNetworkInfo();  
         
        if (networkinfo == null || !networkinfo.isAvailable()) {  
            return false;  
        }  
    
        return true;  
     } 
	
    /**
     * @param nowVersion
     * @param oldVersion
     * @return 版本对比结果
     */
    public static Boolean CompareVersion(String nowVersion,String oldVersion){
    	
    	if(nowVersion==null || nowVersion.equals("")){
    		return false;
    	}
    	if(oldVersion==null || oldVersion.equals("")){
    		return true;
    	}
    	String[] now = nowVersion.split("\\.");
		String[] old = oldVersion.split("\\.");
		if(now.length>old.length){
			for(int i=0;i<old.length;i++){
				int nowInt = Integer.parseInt(now[i]);
				int oldInt = Integer.parseInt(old[i]);
				if(nowInt>oldInt){//大于直接断定为真
					return true;
				}else if(nowInt<oldInt){//小于
					return false;
				}else if(i==old.length-1 && nowInt==oldInt){ //如果相同的角标全比较完了,就看多出的标角是否大于0
					int n = now.length-old.length;
					for(int m=0;m<n;m++){
						int N = Integer.parseInt(now[old.length+m]);
						if(N>0){
							return true;
						}
					}
				}
			}
		}else if(now.length<old.length){
			for(int i=0;i<now.length;i++){
				int nowInt = Integer.parseInt(now[i]);
				int oldInt = Integer.parseInt(old[i]);
				if(nowInt>oldInt){//大于直接断定为真
					return true;
				}else if(nowInt<oldInt){//小于
					return false;
				}/*else if(i==now.length-1 && nowInt==oldInt){ //如果对应的位置全比较完了
					return false;
				}*/
			}
		}else{
			for(int i=0;i<now.length;i++){
				int nowInt = Integer.parseInt(now[i]);
				int oldInt = Integer.parseInt(old[i]);
				if(nowInt>oldInt){
					return true;
				}/*else if(i==now.length-1 && nowInt==oldInt){
					return false;
				}*/
			}
		}
    	return false;
    }
    
    public static TaskDownSpeed taskBeanToSpeed(TaskBean taskBean){
    	TaskDownSpeed tds=new TaskDownSpeed();
    	tds.setTaskID(taskBean.getId());
    	tds.setDownSize(taskBean.getDownloadSize());
    	tds.setSumSize(taskBean.getSumSize());
    	return tds;
    }
    
 // 获得圆角图片的方法
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
    	
    	if(bitmap==null){
    		return null;
    	}
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
    
	/** 倒影 */
    public static Bitmap createReflectedImages(Bitmap originalImage) { 
        // 倒影图和原图之间的距离 
        final int reflectionGap = 5; 
        int index = 0; 
        
        int width = originalImage.getWidth(); 
        int height = originalImage.getHeight(); 
        // 创建矩阵对象 
        Matrix matrix = new Matrix(); 
        // 指定矩阵(x轴不变，y轴相反) 
        matrix.preScale(1, -1); 
        // 将矩阵应用到该原图之中，返回一个宽度不变，高度为原图1/2的倒影位图 
        Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0, 
                height / 2, width, height / 2, matrix, false); 
        // 创建一个宽度不变，高度为原图+倒影图高度的位图 
        Bitmap bitmapWithReflection = Bitmap.createBitmap(width, (height + 2*height / 5), Config.ARGB_8888); 
        // 将上面创建的位图初始化到画布 
        Canvas canvas = new Canvas(bitmapWithReflection); 
        canvas.drawBitmap(getRoundedCornerBitmap(originalImage, 15.0f), 0, 0, null); 
        Paint deafaultPaint = new Paint(); 
        deafaultPaint.setAntiAlias(false); 
        canvas.drawBitmap(getRoundedCornerBitmap(reflectionImage, 15.0f), 0, height + reflectionGap, null); 
        Paint paint = new Paint(); 
        paint.setAntiAlias(false); 
        /** 
         * 参数一:为渐变起初点坐标x位置， 参数二:为y轴位置， 参数三和四:分辨对应渐变终点， 最后参数为平铺方式， 
         * 这里设置为镜像Gradient是基于Shader类，所以我们通过Paint的setShader方法来设置这个渐变 
         */ 
        LinearGradient shader = new LinearGradient(0, 
                originalImage.getHeight(), 0, 
                bitmapWithReflection.getHeight() + reflectionGap, 
                0x70ffffff, 0x00ffffff, TileMode.MIRROR); 
        // 设置阴影 
        paint.setShader(shader); 
        paint.setXfermode(new PorterDuffXfermode( 
                android.graphics.PorterDuff.Mode.DST_IN)); 
        // 用已经定义好的画笔构建一个矩形阴影渐变效果 
        canvas.drawRect(0, height, width, bitmapWithReflection.getHeight() + reflectionGap, paint); 
        // 创建一个ImageView用来显示已经画好的bitmapWithReflection 
        return bitmapWithReflection; 
    }
    
    public static Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
        if (bitmap == null) {
            return null;
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) width / w);
        float scaleHeight = ((float) height / h);
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
        return newbmp;
    }
}
