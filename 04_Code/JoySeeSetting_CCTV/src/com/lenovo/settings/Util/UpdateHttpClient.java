package com.lenovo.settings.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.util.Log;

import com.lenovo.settings.Object.ErrorCode;
import com.lenovo.settings.update.UpdateStatus;

public class UpdateHttpClient {

	private final static String TAG = "UpdateHttpClient";
	//public final String URL = "http://10.18.20.122:8080/PanHub.RDS.Lib.svc/";
	//public final String MARKET_URL = "http://10.18.20.122:8080/PanHub.RDS.Lib.svc/Download/PanHub.Market.xml";
	//public final String DEVICES_URL = "http://10.18.20.122:8080/PanHub.RDS.Lib.svc/Download/PanHub.Market.Devices.xml";
	//public final String URL = "http://panhub.net:8080/PanHub.RDS.V1/PanHub.RDS.Lib.svc/";
	public static final String URL = "http://192.168.1.33/update/update.xml";
	public static final String MARKET_URL = "http://market.panhub.net/Market/PanHub.Market.xml";
	public static final String DEVICES_URL = "http://market.panhub.net/Market/PanHub.Market.Devices.xml";
	public static final String URL_PARAMETER = "?Action=Android&DeviceId=";
	public static final String URL_VERSION = "&Version=";
	public UpdateHttpClient() {
	}
	
	public State getNetworkState(Context mContext) {
		ConnectivityManager conMan = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		//wifi
		State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		return wifi;
		//return State.CONNECTED;
	}
	
	public boolean connectServerByURL() {
		try {
			HttpGet httpRequest = new HttpGet(URL);
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse httpResponse = httpclient.execute(httpRequest);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				return true;
			}
		}
		catch (ClientProtocolException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}  
		return false;
	}
	
	public int getDownloadSizeByURL(String url) {
		int fileSize = 0;
//  update by yuhongkun 20120806
		if(null==url||"".equals(url)){
			return 0;
		}
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpRequest = new HttpGet(url);
			HttpResponse httpResponse = httpclient.execute(httpRequest);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				fileSize = (int)httpResponse.getEntity().getContentLength();
				if(UpdateStatus.DEBUG)
					Log.d(TAG, "get size:" + fileSize);
			}
		}
		catch (ClientProtocolException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return fileSize;
	}

	public int getHttpResponse(String url) {
		int statusCode = HttpStatus.SC_OK;
		HttpGet httpRequest = null;
		
		try {
			httpRequest = new HttpGet(url);
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse httpResponse = httpclient.execute(httpRequest);
			statusCode = httpResponse.getStatusLine().getStatusCode();
			if(UpdateStatus.DEBUG)
				Log.d(TAG, "getHttpResponse, ResponseCode: " + statusCode);
		}
		catch (ClientProtocolException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return statusCode;
	}
	
	public Object getServerData(String url) {
		InputStream inputStream = null;
		Object obj = null;
		
		if(url == null) {
			Log.e(TAG, "getServerData is null!");
			return null;	
		}
		
		inputStream = getInputStreamByGet(url);
		if(inputStream == null) {
			Log.e(TAG, "getServerData: can not get inputstream!");
			return null;
		}

		try {
			PullParserXml handler = new PullParserXml();
			obj = handler.getServerData(inputStream);
		}
		catch (Exception e) {
			e.printStackTrace();
			return new ErrorCode(ErrorCode.ERROR_PARSER);
		}

		try {
			inputStream.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return obj;
	}

	public Object getServerData(String url, List<NameValuePair> params) {
		InputStream inputStream = null;
		Object obj = null;
		
		if(url == null || params != null) {
			Log.e(TAG, "getServerData is null!");
			return null;	
		}
		
		inputStream = getInputStreamByPost(url, params);
		if(inputStream == null) {
			Log.e(TAG, "getServerData: can not get inputstream!");
			return null;
		}
		try {
			PullParserXml handler = new PullParserXml();
			obj = handler.getServerData(inputStream);
		}
		catch (Exception e) {
			e.printStackTrace();
			return new ErrorCode(ErrorCode.ERROR_PARSER);
		}
		
		try {
			inputStream.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return obj;
	}
	
	public int downFile(String device,String file, String url) {
		InputStream inputStream = null;
		FileUtils fileUtils = new FileUtils();

		if((device ==  null) || (file == null) || (url == null)) {
			return -1;
		}
		
		if(fileUtils.exists(device+file)) {
			return 1;
		}
		
		//downloading
		inputStream = getInputStreamByGet(url);
		if(inputStream == null) {
			return -1;
		}
		
		File resultFile = fileUtils.writeFromInput(device, file, inputStream);
		if(resultFile == null) {
			return -1;
		}
		
		try {
			inputStream.close();
		}
		catch(IOException e) {
			e.printStackTrace();
			return -1;
		}
		catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		
		return 0;
	}
	
	public InputStream getInputStreamByGet(String url) {
		InputStream inputStream = null;
		HttpGet httpRequest = null;
		
		try {
			httpRequest = new HttpGet(url);
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse httpResponse = httpclient.execute(httpRequest);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				inputStream = httpResponse.getEntity().getContent();
			}
		}
		catch (ClientProtocolException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return inputStream;
	}

	public InputStream getInputStreamByPost(String url, List<NameValuePair> params) {
		InputStream inputStream = null;
		HttpPost httpRequest = null;

		try {
			httpRequest = new HttpPost(url);
			HttpEntity httpentity = new UrlEncodedFormEntity(params, "utf-8");
			httpRequest.setEntity(httpentity);
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse httpResponse = httpclient.execute(httpRequest);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				inputStream = httpResponse.getEntity().getContent();
			}
		}
		catch (ClientProtocolException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return inputStream;
	}
}
