package com.joysee.appstore.parser;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.joysee.appstore.common.AppsBean;
import com.joysee.appstore.common.PageBean;
import com.joysee.appstore.utils.AppLog;
import com.joysee.appstore.utils.RequestParam;
import com.joysee.appstore.utils.StringUtils;
import com.joysee.appstore.utils.Utils;

public class AppsParser extends BaseParser<List<AppsBean>>{
	public static final String TAG_PAGE = "page"; 
	public static final String TAG_LIST = "list";
	public static final String TAG_TOTAL_LINE = "totalLine"; 
	public static final String TAG_NAME = "name";
	public static final String TAG_IMAGE = "image";
	public static final String TAG_PRICE = "price";
	public static final String TAG_SCORE = "score";
	public static final String TAG_VERSION = "version"; 
	public static final String TAG_VERSION_CODE = "versionCode";
	public static final String TAG_PKGNAME = "packageName";
	
	public Context con;
	public AppsParser(Context cont){
		this.con=cont;
	}

	@Override
	public List<AppsBean> parseJSON(String paramString,PageBean pageBean) throws JSONException {
		if(checkResponse(paramString)==null){
			pageBean.calculatePageTotal(0);
			return null;
		}
		List<AppsBean> appsList=new ArrayList<AppsBean>();
		JSONObject jsonObj=new JSONObject(paramString);
        JSONObject pageObj=jsonObj.getJSONObject(TAG_PAGE);
        JSONArray listArr=pageObj.getJSONArray(TAG_LIST);
        Object pageTotal=pageObj.get(TAG_TOTAL_LINE);
        AppLog.d("--------------------appsParser------pagetotal=",""+Integer.valueOf(pageTotal.toString()));
        pageBean.calculatePageTotal(Integer.valueOf(pageTotal.toString()));
        JSONObject appObj;
        for(int i=0;i<listArr.length();i++){
            appObj=listArr.getJSONObject(i);
            AppsBean apps=new AppsBean();
            apps.setID(appObj.getInt(TAG_ID));
            apps.setAppName(appObj.getString(TAG_NAME));
            String imageSrc=appObj.getString(TAG_IMAGE);
            apps.setPrice((double)appObj.getDouble(TAG_PRICE));
            if(appObj.has(TAG_PKGNAME)){
            	apps.setPkgName(appObj.getString(TAG_PKGNAME));
            }else{
            	apps.setPkgName("");
            }
            if(appObj.has(TAG_SCORE)){
            	apps.setScore(appObj.getString(TAG_SCORE));
            } 
            if(appObj.has(TAG_VERSION)){
            	apps.setVersionName(appObj.getString(TAG_VERSION));
            }
            if(appObj.has(TAG_VERSION_CODE)){
            	apps.setVersion(appObj.getString(TAG_VERSION_CODE));
            }else{
            	apps.setVersion("");
            }
            String[] tempStr=imageSrc.split(TAG_SEPRATE);
            apps.setNatImageUrl(Utils.getImagePath()+tempStr[tempStr.length-1]);
            apps.setSerImageUrl(RequestParam.getFileServerUrl(con)+imageSrc);
            if( !StringUtils.isBlank(appObj.getString(TAG_ID)) && !StringUtils.isBlank(appObj.getString(TAG_NAME)) ){
            	appsList.add(apps);
            }
        }
        
        AppLog.d("--------------------appsParser------size=",""+appsList.size());
		return appsList;
	}

	@Override
	public String checkResponse(String paramString) throws JSONException {
		if(paramString == null||!paramString.contains(TAG_PAGE)||!paramString.contains(TAG_LIST)){
			return null;
		}
		return paramString;
	}

}
