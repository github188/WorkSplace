package com.bestv.ott.appstore.parser;

import java.util.ArrayList;
import java.util.List;
 
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
 

import android.util.Log;

import com.bestv.ott.appstore.common.AppPackageBean;
import com.bestv.ott.appstore.common.AppsBean;
import com.bestv.ott.appstore.common.PageBean; 
import com.bestv.ott.appstore.utils.AppLog;
import com.bestv.ott.appstore.utils.RequestParam;
import com.bestv.ott.appstore.utils.StringUtils;
import com.bestv.ott.appstore.utils.Utils;

public class AppsPackParser extends BaseParser<List<AppPackageBean>>{
	private static final String TAG = "AppsPackParser";
	
	public static final String TAG_appPackageId = "appPackageId"  ;
	public static final String TAG_name = "name"  ;
	public static final String TAG_originalPrice = "originalPrice"  ;
	public static final String TAG_remark = "remark"  ;
	public static final String TAG_promotionPrice = "promotionPrice"  ;
	public static final String TAG_appPackageProductCode = "appPackageProductCode"  ;
	public static final String TAG_image = "image" ;
	public static final String TAG_APPSERVICECODE = "serviceCode";
	
	
	public static final String TAG_TOTAL_LINE = "totalLine"; 
	public static final String TAG_PAGE = "page"; 
	public static final String TAG_LIST = "list";
	

	@Override
	public List<AppPackageBean> parseJSON(String paramString,PageBean pageBean) throws JSONException {
		if(checkResponse(paramString)==null){
			pageBean.calculatePageTotal(0);
			return null;
		}
		List<AppPackageBean> appsList=new ArrayList<AppPackageBean>();
		
//		AppLog.e(TAG,"ppppppppppppppp>>>>>>>>>>>>>>>>>>>:" + paramString);
		
		JSONObject jsonObj=new JSONObject(paramString); 
		
        AppPackageBean apps = new AppPackageBean();
		apps.setAppPackageId(jsonObj.getString(TAG_appPackageId));
		apps.setName(jsonObj.getString(TAG_name)); 
		apps.setOriginalPrice((Double) jsonObj.getDouble(TAG_originalPrice));
		apps.setRemark(jsonObj.getString(TAG_remark));
		apps.setPromotionPrice((Double)jsonObj.getDouble(TAG_promotionPrice));
		apps.setAppPackageProductCode(jsonObj.getString(TAG_appPackageProductCode));
		
		String serImage = jsonObj.getString(TAG_image); 
		
		
		JSONObject appBeans  = jsonObj.getJSONObject("appPage");
		if(appBeans != null ){
			apps.setPageNo(appBeans.getInt("pageNo"));
			apps.setTotalPages(appBeans.getInt("totalPages"));
			
			appBeans.getString("totalPages");
			JSONArray jsa = appBeans.getJSONArray("list");
			int len = jsa.length();
			if(len > 0){
				AppsBean[] beans = new AppsBean[len];
				for(int i=0;i<len;i++){
					JSONObject obj = jsa.getJSONObject(i);
					beans[i] =new AppsBean();
					beans[i].setID(obj.getInt(TAG_ID));
					beans[i].setAppName(obj.getString("cname"));
		            if(obj.has(TAG_originalPrice)){
		            	if(StringUtils.isBlank(obj.getDouble(TAG_originalPrice))){
		            		beans[i].setOriginalPrice(0d); 
		            	}else{
		            		beans[i].setOriginalPrice(obj.getDouble(TAG_originalPrice)); 
		            	} 
		            } 
		            
		            beans[i].setAppPackageId(obj.getString("appPackageId"));
		            beans[i].setAppPackageProductCode(obj.getString("appPackageProductCode"));
		            beans[i].setAppProductCode(obj.getString("appProductCode"));
		            beans[i].setServiceCode(obj.getString("serviceCode"));

				}
				apps.setAppsBean(beans);
			}
		} 
		String[] tempStr=serImage.split(TAG_SEPRATE);
//		Log.d(TAG, "-------tempStr = "+tempStr.toString());
		apps.setNativeImage(Utils.getImagePath()+tempStr[tempStr.length-1]);
//		Log.d(TAG, "-------NativeImage = "+(Utils.getImagePath()+tempStr[tempStr.length-1]));
		apps.setImage(RequestParam.SERVICE_IMAGE_URL+serImage);
		appsList.add(apps);
		return appsList;
	}

	@Override
	public String checkResponse(String paramString) throws JSONException {
		if(paramString == null){
			return null;
		}
		return paramString;
	}

}
