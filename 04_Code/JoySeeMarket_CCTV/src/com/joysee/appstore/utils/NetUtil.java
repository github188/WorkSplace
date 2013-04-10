package com.joysee.appstore.utils;

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
import com.joysee.appstore.common.ApplicationBean;
import com.joysee.appstore.common.PageBean;
import com.joysee.appstore.parser.BaseParser;


public class NetUtil {

    public static String TAG="com.joysee.appstore.utils.NetUtil";
    // 测试用的地址，主机是周慧春的
    public static String path = AppStoreConfig.APPSTORE_UPDATE_ADDRESS;
    private  String mUserID = "-1";
    private  String mGroupID = "-1";
    public NetUtil(Context context){
//    	if(context==null){
//    		AppLog.d(TAG, "context is null");
//    		mUserID="-1";
//    		mGroupID="-1";
//    	}else{
//    		sp = context.getSharedPreferences("user_information", Context.MODE_PRIVATE);
//        	mUserID = sp.getString("userID", "-1");
//        	mGroupID = sp.getString("groupID", "-1");
//    	}
//    	AppLog.d(TAG, "------NetUtil init--->   userID : "+mUserID+"  |   groudID : "+mGroupID+"-------------");
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
        HttpPost request = new HttpPost( url); // 根据内容来源地址创建一个Http请求
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        AppLog.e(TAG, "--------------userID="+mUserID+";------groudID="+mGroupID+";url="+url);
        if(null!=param){
        	//请求数据时，带上用户信息
        	param.put(RequestParam.Param.USERID, mUserID);
        	param.put(RequestParam.Param.GROUPID, mGroupID);
        	Set<String> keySet=param.keySet();
            Iterator<String> iterator = keySet.iterator(); 
            while(iterator.hasNext()){
                String keyValue=String.valueOf(iterator.next());
                AppLog.e(TAG, "-----@param "+"key="+keyValue+"; value="+param.get(keyValue));
                try {
                	String str = "";
                	str = URLEncoder.encode(param.get(keyValue),"UTF-8");
    				params.add(new BasicNameValuePair(keyValue,str));
    			} catch (UnsupportedEncodingException e) {
    				e.printStackTrace();
    			}
            }
        }else{
			try{
				params.add(new BasicNameValuePair(RequestParam.Param.USERID,URLEncoder.encode(mUserID,"UTF-8")));
				params.add(new BasicNameValuePair(RequestParam.Param.GROUPID,URLEncoder.encode(mGroupID,"UTF-8")));
			} catch(UnsupportedEncodingException e) {
				e.printStackTrace();
			}
        }
        AndroidHttpClient client = null;
        try {
            request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
//            AppLog.d(TAG, "request.getEntity:"+request.getEntity().toString());
            client = AndroidHttpClient.newInstance("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; "
                    + "Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; "
                    + ".NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; "
                    + ".NET CLR 3.5.30729)");
            HttpResponse httpResponse = client.execute(request);// 发送请求并获取反馈
            if (httpResponse.getStatusLine().getStatusCode() == 200){
                String result = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
//                AppLog.d(TAG, "result = "+result.toString());
                try{
					return parser.parseJSON(result, pageBean);
				}catch(JSONException e) {
					AppLog.d(TAG, "--------json is error");
					e.printStackTrace();
				}
            }else{
                AppLog.e("com.supertv.util.NetUtil", "net work error !!!");
                return null;
            }
        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
            return null;
        }catch(ClientProtocolException e) {
            e.printStackTrace();
            return null;
        }catch (IOException e){
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
        HttpPost request = new HttpPost( url); // 根据内容来源地址创建一个Http请求
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        AppLog.e(TAG, "--------------userID="+mUserID+";------groudID="+mGroupID);
        if(null!=param){
        	//take the userInfo
        	param.put(RequestParam.Param.USERID, mUserID);
        	param.put(RequestParam.Param.GROUPID, mGroupID);
        	Set<String> keySet=param.keySet();
            Iterator<String> iterator = keySet.iterator(); 
            while(iterator.hasNext()){
                String keyValue=String.valueOf(iterator.next());
                AppLog.e(TAG, "-----@param "+"key="+keyValue+"; value="+param.get(keyValue));
                try {
                	String str = "";
                	str = URLEncoder.encode(param.get(keyValue),"UTF-8");
                	AppLog.e(TAG, "-------------str="+str);
    				params.add(new BasicNameValuePair(keyValue,str));
    			} catch (UnsupportedEncodingException e) {
    				e.printStackTrace();
    			}
                
            }
        }else{
			try {
				params.add(new BasicNameValuePair(RequestParam.Param.USERID,URLEncoder.encode(mUserID,"UTF-8")));
				params.add(new BasicNameValuePair(RequestParam.Param.GROUPID,URLEncoder.encode(mGroupID,"UTF-8")));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
        }
        AndroidHttpClient client = null;
        try {
            request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            AppLog.d(TAG, "request.getEntity:"+request.getEntity().toString());
            client = AndroidHttpClient.newInstance("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; "
                    + "Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; "
                    + ".NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; "
                    + ".NET CLR 3.5.30729)");
            AppLog.e(TAG,"-----------------httpResponse----- client.execute(request)---");
            HttpResponse httpResponse = client.execute(request);// 发送请求并获取反馈
            AppLog.d(TAG, "-----------------httpResponse.getStatusLine().getStatusCode()="+httpResponse.getStatusLine().getStatusCode());
            if (httpResponse.getStatusLine().getStatusCode() == 200){
                String result = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
//                AppLog.e(TAG, "result = "+result.toString());
                return result;
            }else{
                AppLog.e("com.supertv.util.NetUtil", "net work error !!!");
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
    
    
    /* 加载网络图片，由CaCheManager管理是否需要加载或SD卡是否需要清理 */
    public static boolean loadImage(String serverUrl,String nativeUrl){
    	AppLog.e(TAG, "----loadimage----:"+nativeUrl);
    	if(CaCheManager.isCaCheExists(nativeUrl)){
    		CaCheManager.refreshLastTime(nativeUrl);
			return true;
		}
    	return downImg(serverUrl, nativeUrl);
    }
    
    /* down image */
	public static boolean downImg(String serverUrl, String nativeUrl) {
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
					byte[] bytes = new byte[1024*1024]; 
	                int len = -1; 
	                while((len = inputStream.read(bytes))!=-1) { 
	                    fos.write(bytes, 0, len); 
	                    downSize=downSize+len;
	                }
	                fos.close();
	                if(downSize<allSize){
	                    File file=new File(nativeUrl);
	                  	if(file.exists()){
	                  		file.delete();
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
        for(int i = 0; i < appList.size(); i++) {
            JSONObject jsonin = new JSONObject();
            jsonin.put("packageName", appList.get(i).getPkgName());
            jsonin.put("versionCode", appList.get(i).getVersion());
            AppLog.d(TAG, "------------name:"+appList.get(i).getPkgName()+"-----vesrion:"+appList.get(i).getVersion());
            arr.put(jsonin);
        }
        outjson.put("updateAppList", arr);
        return getMetaDataFromInterface(RequestParam.getAppServerUrl(context)+RequestParam.Action.GETUPDATEAPPLIST, outjson);
    }
    
    /** update for Market */
    public static String connect(Context context,List<ApplicationBean> appList,String action) throws Exception {
        Log.d("com.supertv.util.NetUtil", "appList.size() = "+appList.size());
        
        JSONObject outjson = new JSONObject();
        JSONArray arr = new JSONArray(); 
        for(int i = 0; i < appList.size(); i++) {
            JSONObject jsonin = new JSONObject();
            jsonin.put("packageName", appList.get(i).getPkgName());
            jsonin.put("versionCode", appList.get(i).getVersion());
            AppLog.d(TAG, "------------name:"+appList.get(i).getPkgName()+"-----vesrion:"+appList.get(i).getVersion());
            arr.put(jsonin);
        }
        outjson.put("updateAppList", arr);
        return getMetaDataFromInterface(RequestParam.getAppServerUrl(context)+action, outjson);
    }

    @SuppressWarnings("unchecked")
    private static String getMetaDataFromInterface(String url,JSONObject arr) throws Exception{
        Log.d("com.supertv.util.NetUtil", "getMetaDataFromInterface()");
        
        try {
            HttpPost request = new HttpPost(url); // 根据内容来源地址创建一个Http请求
            List params = new ArrayList();
            AppLog.d(TAG, "-----------arr.tostring:"+arr.toString());
            params.add(new BasicNameValuePair("appstoreUpdate", arr.toString())); // 添加必须的参数
            request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8)); // 设置参数的编码
            HttpResponse httpResponse = new DefaultHttpClient().execute(request); // 发送请求并获取反馈\
            
            AppLog.d("xx", "request url : "+url);
            AppLog.d("xx", "request params : "+(new BasicNameValuePair("updateAppList", arr.toString())).toString());
            AppLog.d(TAG, "new url="+request.getURI());
            AppLog.d(TAG, "params="+request.getParams());
            // 解析返回的内容
            
            AppLog.d(TAG, "result code = "+httpResponse.getStatusLine().getStatusCode());
            
            if(httpResponse.getStatusLine().getStatusCode() == 200){
                String result = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                AppLog.d(TAG, "-------------------result ()---------- = "+result.toString());
                return result;
            }else{
                AppLog.e("com.supertv.util.NetUtil", "net work error !!!");
            }
        } catch (Exception e) {
         
        }
        return null;
    }
}