package com.joysee.appstore.parser;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.joysee.appstore.common.AppsBean;
import com.joysee.appstore.common.PageBean;
import com.joysee.appstore.utils.StringUtils;
import com.joysee.appstore.utils.Utils;

public class MenusParser extends BaseParser<List<AppsBean>>{ 
	public static final String TAG_AID = "@id" ;
	public static final String TAG_NAME = "@name" ;
	public static final String TAG_NIMG = "@nImg" ;
	public static final String TAG_TYPES = "Types" ; 
	public static final String TAG_SEPRATE2 = "//"; 
	
	@Override
	public List<AppsBean> parseJSON(String paramString,PageBean pageBean) throws JSONException {
		if(checkResponse(paramString)==null){
			return null;
		}
		List<AppsBean> menusList=new ArrayList<AppsBean>();
		JSONArray typesObj=new JSONObject(paramString).getJSONArray(TAG_TYPES);
        JSONObject appObj;
        for(int i=0;i<typesObj.length();i++){
            appObj=typesObj.getJSONObject(i);
            AppsBean apps=new AppsBean();
            apps.setID(appObj.getInt(TAG_AID));
            apps.setAppName(appObj.getString(TAG_NAME));
            String imageSrc=appObj.getString(TAG_NIMG);
            String[] tempStr=imageSrc.split(TAG_SEPRATE2);
            String[] temp2Str=tempStr[1].split(TAG_SEPRATE);
            apps.setNatImageUrl(Utils.getImagePath()+temp2Str[temp2Str.length-1]);
            apps.setSerImageUrl(imageSrc);
            if(!StringUtils.isBlank(appObj.getString(TAG_AID))){
            	menusList.add(apps);
            }
        }
		return menusList;
	}

	@Override
	public String checkResponse(String paramString) throws JSONException {
		if(paramString == null||!paramString.contains(TAG_TYPES)){
			return null;
		}
		return paramString;
	}

}