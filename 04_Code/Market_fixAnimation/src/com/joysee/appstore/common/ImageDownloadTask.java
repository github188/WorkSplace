package com.joysee.appstore.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;

import com.joysee.appstore.R;
import com.joysee.appstore.utils.AppLog;

/**
 * 异步下载图片
 * @author Administrator 
 *
 */
public class ImageDownloadTask extends AsyncTask<Object, Void, Bitmap> {
    
    private static final String TAG="com.joysee.appstore.home.ImageDownloaderTask";
    Handler handler;
    int Msg;
    
    @Override
    protected Bitmap doInBackground(Object... params) {
        AppLog.d(TAG, "----doInBackground");
//        AppsBean appsBean=(AppsBean)params[0];
        long firstTime=System.currentTimeMillis();
        List<AppsBean> appList = (List<AppsBean>)params[0];
        handler=(Handler)params[1];
        Msg=(Integer)params[2];
        Bitmap bitmap=null;
        for(AppsBean appsBean:appList){
          File imgFile=new File(appsBean.getNatImageUrl());
          AppLog.d(TAG, "---------------imageURL="+appsBean.getSerImageUrl());
          if(imgFile.exists()&&imgFile.length()>0){
              handler.sendEmptyMessage(Msg);
          }else{
        	  String urlStr = appsBean.getSerImageUrl();
              URL url = null;
              
              try {
                  url = new URL(urlStr);
                  URLConnection connection = url.openConnection();
                  connection.setConnectTimeout(Constants.TIMEOUT);
                  connection.connect();
                  if(null==connection){
                      AppLog.d(TAG,"---connection fail");
                      handler.sendEmptyMessage(Msg);
                      continue;
                  }
                  int allSize=connection.getContentLength();
                  int downSize=0;
                  InputStream inputStream = connection.getInputStream();
                  if(null==inputStream){
                      AppLog.d(TAG,"---inputStream is null ");
                      handler.sendEmptyMessage(Msg);
                      continue;
                  }
                  FileOutputStream fos = new FileOutputStream(appsBean.getNatImageUrl()); 
                  byte[] bytes = new byte[1024]; 
                  int len = -1; 
                  while((len = inputStream.read(bytes))!=-1) 
                  { 
                      fos.write(bytes, 0, len); 
                      downSize=downSize+len;
                  }
                  fos.close();
                  if(downSize<allSize){
                  	File file=new File(appsBean.getNatImageUrl());
                  	if(file.exists()){
                  		file.delete();
                  	}
                  	appsBean.setNatImageUrl(null);
                  }
                  AppLog.d(TAG, "----------allSize="+allSize+";downSize="+downSize);
                  inputStream.close();
              } catch (Exception e) {
                  File file=new File(appsBean.getNatImageUrl());
                	if(file.exists()){
                		file.delete();
                	}
                	appsBean.setNatImageUrl(null);
                  //e.printStackTrace();
              } 
              handler.sendEmptyMessage(Msg);
          }
        }
        long lastTime=System.currentTimeMillis();
        AppLog.d(TAG, "-------------ImageDownloadTask----------down "+appList.size()+" images take time="+(lastTime-firstTime));
        return bitmap;
    }


    @Override
    protected void onPostExecute(Bitmap result) {
//        handler.sendEmptyMessage(Msg);
        super.onPostExecute(result);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


}