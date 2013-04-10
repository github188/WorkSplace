package com.bestv.ott.appstore.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.bestv.ott.appstore.common.Constants;
import com.bestv.ott.appstore.common.TaskBean;
import com.bestv.ott.appstore.common.TaskDownSpeed;
import com.bestv.ott.appstore.db.DatabaseHelper.DownloadTaskColumn;
import com.bestv.ott.appstore.db.DatabaseHelper.DownloadTaskColumnIndex;

public class Utils {
	
	public static final String TAG = "com.joysee.appstore.Utils";
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
	        return null;
	    }
	    return sdDir.toString();
	}
	
	
	public static boolean checkConnect(String path){
		URL url;
		HttpURLConnection conn=null;
		try {
			url = new URL(path);
				conn = (HttpURLConnection)url.openConnection();
				conn.setConnectTimeout(Constants.TIMEOUT);
				int status = conn.getResponseCode();
				
				AppLog.d(TAG, "==========checkConnect()============status="+status);
				 
//				if(status>=HttpURLConnection.HTTP_BAD_REQUEST){
				if((status / 100) != 2){
					return false;
				}
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}catch(Error e){
				e.printStackTrace();
				return false;
			}finally{
			if(conn!=null)
			{
				conn.disconnect();
			}
		}
		return true;
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
    
    
	/**
	 * gridview 合并单元格
	 */
	
//	public static void MergeRows(GridView gvw, int sCol, int eCol)//gvw 需要合并的GridView，sCol要合并开始列（从0开始），eCol要合并的结束列  
//    { 
//        for (int rowIndex = gvw.Rows.Count - 2; rowIndex >= 0; rowIndex--) 
//        { 
//            GridViewRow row = gvw.Rows[rowIndex]; 
//  
//            GridViewRow previousRow = gvw.Rows[rowIndex + 1]; 
//  
//            for (int i = sCol; i < eCol + 1; i++) 
//            { 
//                if (row.Cells[i].Text != "" && row.Cells[i].Text != " ") 
//                { 
//                    if (row.Cells[i].Text == previousRow.Cells[i].Text) 
//                    { 
//                        row.Cells[i].RowSpan = previousRow.Cells[i].RowSpan < 1 ? 2 : previousRow.Cells[i].RowSpan + 1; 
//  
//                        previousRow.Cells[i].Visible = false; 
//                    } 
//                } 
//            } 
//        } 
//    } 
}
