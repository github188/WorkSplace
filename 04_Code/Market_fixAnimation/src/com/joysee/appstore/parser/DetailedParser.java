package com.joysee.appstore.parser;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.joysee.appstore.common.AppsBean;
import com.joysee.appstore.common.PageBean;
import com.joysee.appstore.utils.RequestParam;
import com.joysee.appstore.utils.StringUtils;
import com.joysee.appstore.utils.Utils;

public class DetailedParser extends BaseParser<AppsBean>{
	public static final String TAG_APP = "app";   
	public static final String TAG_CNAME = "cname";
	public static final String TAG_REMARK = "remark";
	public static final String TAG_SCROE = "score";
	public static final String TAG_FILEPATH = "filepath";
	public static final String TAG_SELECT_ICON = "selectIcon";
	
	public static final String TAG_SIZE = "size"; 
	public static final String TAG_VERSION = "version"; 
	public static final String TAG_VERSION_CODE = "versionCode";
	public static final String TAG_TYPENAME = "typeName"; 
	public static final String TAG_UPDATETIME = "updateTime"; 
	public static final String TAG_CREATETIME = "createtime"; 
	public static final String TAG_VOTENUM = "voteNum"; 
	public static final String TAG_PKGNAME="packageName";
	
	public Context con;
	public DetailedParser(Context cont){
		this.con=cont;
	}

	@Override
	public AppsBean parseJSON(String paramString, PageBean pageBean)
			throws JSONException {
		if(checkResponse(paramString)==null){
			return null;
		}
		AppsBean appBean=new AppsBean();
		JSONObject jsonObj=new JSONObject(paramString);
        JSONObject appObj=null;
        if(jsonObj.has(TAG_APP)){
        	appObj=jsonObj.getJSONObject(TAG_APP);
        	/**
        	 * 为什么这里是空值才返回
        	 * TODO by penghui
        	 */
        	if(StringUtils.isBlank(appObj.getString(TAG_CNAME))){
        		return appBean;
        	}
        }else{
        	return appBean;
        }
        appBean.setID(appObj.getInt(TAG_ID));
        appBean.setAppName(appObj.getString(TAG_CNAME));
        appBean.setRemark(appObj.getString(TAG_REMARK));
        appBean.setScore(appObj.getString(TAG_SCROE));
        if(appObj.has(TAG_PKGNAME)){
        	appBean.setPkgName(appObj.getString(TAG_PKGNAME));
        }else{
        	appBean.setPkgName("");
        }
        appBean.setApkUrl(RequestParam.getFileServerUrl(con)+appObj.getString(TAG_FILEPATH));
        String imageSrc=appObj.getString("selectIcon");
        String[] tempStr=imageSrc.split(TAG_SEPRATE);
        appBean.setNatImageUrl(Utils.getImagePath()+tempStr[tempStr.length-1]);
        appBean.setSerImageUrl(RequestParam.getFileServerUrl(con)+imageSrc);
        appBean.setSize(appObj.getInt(TAG_SIZE));
        appBean.setVersion(appObj.getString(TAG_VERSION_CODE));
        if(appObj.has(TAG_VERSION)){
        	appBean.setVersionName(appObj.getString(TAG_VERSION));
        }else{
        	appBean.setVersionName("");
        }
        appBean.setTypeName(appObj.getString(TAG_TYPENAME));
        if(appObj.has(TAG_UPDATETIME)){
        	appBean.setCreateTime(appObj.getString(TAG_UPDATETIME));
        }else if(appObj.has(TAG_CREATETIME)){
        	appBean.setCreateTime(appObj.getString(TAG_CREATETIME));
        }
        appBean.setVoteNum(appObj.getInt(TAG_VOTENUM));
		return appBean;
	}

	@Override
	public String checkResponse(String paramString) throws JSONException {
		if(paramString == null||!paramString.contains(TAG_APP)){
			return null;
		}
		return paramString;
	}

}
