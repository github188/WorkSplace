package com.bestv.ott.appstore.utils;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.http.AndroidHttpClient;
import android.util.Log;

import com.bestv.ott.appstore.common.ApplicationBean;
import com.bestv.ott.appstore.common.PageBean;
import com.bestv.ott.appstore.parser.BaseParser;


public class NetUtil {

    public static String TAG="com.joysee.appstore.utils.NetUtil";
    
    public static String path = AppStoreConfig.APPSTORE_UPDATE_ADDRESS;
    private SharedPreferences sp;
    private String mUserID = "-1";
    private String mGroupID = "-1";
    private static CaCheManager manager;
    
    //初始化用户信息
    public NetUtil(Context context){
    	if(context==null){
    		mUserID="-1";
    		mGroupID="-1";
    		manager = CaCheManager.getInstance();
    	}else{
    		sp = context.getSharedPreferences("user_information", Context.MODE_PRIVATE);
        	mUserID = sp.getString("userID", "-1");
        	mGroupID = sp.getString("groupID", "-1");
        	manager = CaCheManager.getInstance();
    	}
    	AppLog.d(TAG, "========mUserID = "+mUserID);
    	AppLog.d(TAG, "========mGroudID = "+mGroupID);
    }
    /**
     * 从服务器获取相应的应用信息
     * @param <T>
     * @param paramMap
     * @param url
     * @return
     * @throws UnsupportedEncodingException 
     */
    public  Object  getNetData(Map<String,String> param,String url,PageBean pageBean,BaseParser<?> parser) {
    	StringBuffer urlStr=new StringBuffer();
    	urlStr.append(url);
//        HttpGet request = new HttpGet( url); // 根据内容来源地址创建一个Http请求
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        if(null!=param){
        	//请求数据时，带上用户信息
        	param.put(RequestParam.Param.USERID, mUserID);
        	param.put(RequestParam.Param.GROUPID, mGroupID);
        	Set<String> keySet=param.keySet();
            Iterator<String> iterator = keySet.iterator(); 
            while(iterator.hasNext()){
                String keyValue=String.valueOf(iterator.next());
                try {
                	if(keyValue.equals(RequestParam.Param.GROUPID)){
                		urlStr.append("&"+keyValue+"="+param.get(keyValue));
                	}else{
                		String str = "";
                    	str = URLEncoder.encode(param.get(keyValue),"UTF-8");
                		urlStr.append("&"+keyValue+"="+str);
                	}
    			} catch (UnsupportedEncodingException e) {
    				e.printStackTrace();
    			}
                
            }
        }else{
			try {
				params.add(new BasicNameValuePair(RequestParam.Param.USERID,URLEncoder.encode(mUserID,"UTF-8")));
				params.add(new BasicNameValuePair(RequestParam.Param.GROUPID,mGroupID));
				urlStr.append("&"+RequestParam.Param.USERID+"="+URLEncoder.encode(mUserID,"UTF-8"));
				urlStr.append("&"+RequestParam.Param.GROUPID+"="+mGroupID);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
        }
        AndroidHttpClient client = null;
        try {
//            request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
//            request.setParams(params);
//            AppLog.d(TAG, "request.getEntity:"+request.getEntity().toString());
//            AppLog.d(TAG, "url="+urlStr.toString());
        	HttpGet request=new HttpGet(urlStr.toString());
            client = AndroidHttpClient.newInstance("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; "
                    + "Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; "
                    + ".NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; "
                    + ".NET CLR 3.5.30729)");
            HttpResponse httpResponse = client.execute(request);// 发送请求并获取反馈
            if (httpResponse.getStatusLine().getStatusCode() == 200)
            {
                    String result = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                    AppLog.d(TAG, "--------result = "+result.toString());
                    try {
						return parser.parseJSON(result, pageBean);
					} catch (JSONException e) {
						AppLog.d(TAG, "--------json is error");
						e.printStackTrace();
					}
            }else{
                Log.e("com.supertv.util.NetUtil", "net work error !!!");
                return null;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }  catch (ClientProtocolException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }finally{
        	if (client != null) {
                client.close();
            }
        }
        
        return null;
    }
    
    /**
     * 从服务器获取相应的应用信息
     * @param paramMap
     * @param url
     * @return
     * @throws UnsupportedEncodingException 
     */
    public  String getAppsByStatus(Map<String,String> param,String url) {
    	StringBuffer urlStr=new StringBuffer();
    	urlStr.append(url);
//      HttpPost request = new HttpPost(url); // 根据内容来源地址创建一个Http请求
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        if(null!=param){
        	//请求数据时，带上用户信息
        	param.put(RequestParam.Param.USERID, mUserID);
        	param.put(RequestParam.Param.GROUPID, mGroupID);
        	Set<String> keySet=param.keySet();
            Iterator<String> iterator = keySet.iterator(); 
            while(iterator.hasNext()){
                String keyValue=String.valueOf(iterator.next());
                try {
                	if(keyValue.equals(RequestParam.Param.GROUPID)){
                		urlStr.append("&"+keyValue+"="+param.get(keyValue));
                	}else{
                		String str = "";
                    	str = URLEncoder.encode(param.get(keyValue),"UTF-8");
                		urlStr.append("&"+keyValue+"="+str);
                	}
    			} catch (UnsupportedEncodingException e) {
    				e.printStackTrace();
    			}
            }
        }else{
			try {
				params.add(new BasicNameValuePair(RequestParam.Param.USERID,URLEncoder.encode(mUserID,"UTF-8")));
				params.add(new BasicNameValuePair(RequestParam.Param.GROUPID,mGroupID));
				urlStr.append("&"+RequestParam.Param.USERID+"="+URLEncoder.encode(mUserID,"UTF-8"));
				urlStr.append("&"+RequestParam.Param.GROUPID+"="+mGroupID);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
        }
        AndroidHttpClient client = null;
        try {
        	HttpGet request=new HttpGet(urlStr.toString());
//        	  AppLog.d(TAG, "-------url="+urlStr.toString());
//            request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
//            AppLog.d(TAG, "request.getEntity:"+request.getEntity().toString());
            client = AndroidHttpClient.newInstance("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; "
                    + "Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; "
                    + ".NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; "
                    + ".NET CLR 3.5.30729)");
//            AppLog.log_D(TAG,"-----------------httpResponse----- client.execute(request)---");
            HttpResponse httpResponse = client.execute(request);// 发送请求并获取反馈
//            AppLog.d(TAG, "-----------------httpResponse.getStatusLine().getStatusCode()="+httpResponse.getStatusLine().getStatusCode());
            if (httpResponse.getStatusLine().getStatusCode() == 200)
            {
                    String result = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
//                    AppLog.d(TAG, "--------result = "+result.toString());
                    return result;
            }else{
                Log.e("com.supertv.util.NetUtil", "net work error !!!");
                return RequestParam.NET_ERROR;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }  catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
        	if (client != null) {
                client.close();
            }
        }
        
        return RequestParam.NET_ERROR;
    }
    
    
    /* 加载网络图片 */
    public static boolean loadImage(String serverUrl,String nativeUrl){
    	if(manager.isCaCheExists(nativeUrl)){
    		manager.refreshLastTime(nativeUrl);
			return true;
		}
    	return downImg(serverUrl, nativeUrl);
    }
    
    /* down image */
	private static boolean downImg(String serverUrl, String nativeUrl) {
	    AppLog.d(TAG, "------downimage--------");
		AndroidHttpClient client = AndroidHttpClient.newInstance("http");
		HttpGet getRequest = new HttpGet(serverUrl);
		try {
			HttpResponse response = client.execute(getRequest);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				return false;
			}
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream inputStream = null;
				try {
					inputStream = entity.getContent();
					long allSize = entity.getContentLength();
	                int downSize=0;
					FileOutputStream fos = new FileOutputStream(nativeUrl);
					byte[] bytes = new byte[1024]; 
	                int len = -1; 
	                while((len = inputStream.read(bytes))!=-1) 
	                { 
	                    fos.write(bytes, 0, len);
	                    downSize=downSize+len;
	                }
	                fos.close();
	                if(downSize<allSize){ //下载图片不完整
	                    File file=new File(nativeUrl);
	                  	if(file.exists()){
	                  		file.delete();
	                  		//return false; //删掉后，返回false，在LoadAppThread里把该图片本地图片地址设为null
	                  	}
	                  	return false;
	                }else if(downSize==allSize){
	                	CaCheManager.addBitmap(nativeUrl);
	                	AppLog.e(TAG, "-----> download a piture --> addBitmap");
	                }
	                return true;
				} finally {
					if (inputStream != null) {
						inputStream.close();
					}
//					entity.consumeContent();
				}
			}
		} catch (Exception e) {
			getRequest.abort();
			e.printStackTrace();
			File file=new File(nativeUrl);
          	if(file.exists()){
          		file.delete();
          	}
			return false;
		} finally {
			if (client != null) {
				client.close();
			}
		}
		return true;
	}
    

    public static String connect(Context context,List<ApplicationBean> appList) throws Exception {
        Log.d("com.supertv.util.NetUtil", "appList.size() = "+appList.size());
        
        JSONObject outjson = new JSONObject();
        JSONArray arr = new JSONArray(); 
        for (int i = 0; i < appList.size(); i++) {
                  JSONObject jsonin = new JSONObject();
                  jsonin.put("packageName", appList.get(i).getPkgName());
                  jsonin.put("versionCode", appList.get(i).getVersion());
                  AppLog.d(TAG, "------------name:"+appList.get(i).getPkgName()+"-----vesrion:"+appList.get(i).getVersion());
                  arr.put(jsonin);
              }
        outjson.put("updateAppList", arr);
        
        return getMetaDataFromInterface(path, outjson);
    }

    @SuppressWarnings("unchecked")
    private static String getMetaDataFromInterface(String url,JSONObject arr) throws Exception{
    	Log.d("com.supertv.util.NetUtil", "getMetaDataFromInterface()");
        
        try {
            HttpPost request = new HttpPost(url); // 根据内容来源地址创建一个Http请求
            List params = new ArrayList();
            AppLog.d(TAG, "-----------arr.tostring:"+arr.toString());
            params.add(new BasicNameValuePair("updateAppList", arr.toString())); // 添加必须的参数
            request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8)); // 设置参数的编码
            HttpResponse httpResponse = new DefaultHttpClient().execute(request); // 发送请求并获取反馈
            // 解析返回的内容
            
            AppLog.d(TAG, "result code = "+httpResponse.getStatusLine().getStatusCode());
            
            if (httpResponse.getStatusLine().getStatusCode() == 200)
            {
                    String result = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                    AppLog.d(TAG, "-------------------result ()---------- = "+result.toString());
                    
                    return result;
            }else{
                Log.e("com.supertv.util.NetUtil", "net work error !!!");
            }
            
            } catch (Exception e) {
            }
            
            return null;
    }
}