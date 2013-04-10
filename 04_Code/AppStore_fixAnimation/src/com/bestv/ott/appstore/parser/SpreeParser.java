package com.bestv.ott.appstore.parser;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.bestv.ott.appstore.common.PageBean;
import com.bestv.ott.appstore.common.SpreeBean;
import com.bestv.ott.appstore.utils.RequestParam;
import com.bestv.ott.appstore.utils.StringUtils;
import com.bestv.ott.appstore.utils.Utils;

/* 礼包Parser */
public class SpreeParser extends BaseParser<List<SpreeBean>>{

	private static final String TAG_LIST = "list";
	private static final String APPPACKAGEID = "appPackageId";
	private static final String APPPACKAGEPRODUCTCODE = "appPackageProductCode";
	private static final String APPPAGE = "appPage";
	private static final String IMAGE = "image";
	private static final String NAME = "name";
	private static final String ORIGINALPRICE = "originalPrice";
	private static final String PROMOTIONPRICE = "promotionPrice";
	private static final String REMARK = "remark";
	
	public List<SpreeBean> parseJSON(String paramString, PageBean pageBean)
			throws JSONException {
		if(checkResponse(paramString)==null){
			return null;
		}
		List<SpreeBean> spreeList = new ArrayList<SpreeBean>();
		JSONObject jsonObj=new JSONObject(paramString);
		if(!paramString.contains(TAG_LIST)){
			return spreeList;
		}
		JSONArray listArr=jsonObj.getJSONArray(TAG_LIST);
		int len = listArr.length();
		JSONObject spreeObj;
		for(int i=0;i<len;i++){
			spreeObj = listArr.getJSONObject(i);
			SpreeBean bean = new SpreeBean();
			bean.setApppackageid(spreeObj.getInt(APPPACKAGEID));
			bean.setApppackageproductcode(spreeObj.getString(APPPACKAGEPRODUCTCODE));
			bean.setApppage(spreeObj.getString(APPPAGE));
			bean.setName(spreeObj.getString(NAME));
			bean.setOriginalprice(spreeObj.getString(ORIGINALPRICE));//原价
			bean.setPromotionprice(spreeObj.getString(PROMOTIONPRICE));//现价
			bean.setRemark(spreeObj.getString(REMARK));
			
			String serIma = spreeObj.getString(IMAGE);
			String[] tempStr=serIma.split(TAG_SEPRATE);
			bean.setNatImageUrl(Utils.getImagePath()+tempStr[tempStr.length-1]);
			bean.setSerImageUrl(RequestParam.SERVICE_IMAGE_URL+spreeObj.getString(IMAGE));
			if(!StringUtils.isBlank(bean.getApppackageid()) && !StringUtils.isBlank(bean.getName())){
				spreeList.add(bean);
			}
		}
		return spreeList;
	}

	
	public String checkResponse(String paramString) throws JSONException {
		if(paramString == null){
			return null;
		}
		return paramString;
	}
	
}