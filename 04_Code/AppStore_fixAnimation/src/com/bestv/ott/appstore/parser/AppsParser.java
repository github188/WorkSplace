package com.bestv.ott.appstore.parser;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.bestv.ott.appstore.common.AppsBean;
import com.bestv.ott.appstore.common.PageBean;
import com.bestv.ott.appstore.utils.AppLog;
import com.bestv.ott.appstore.utils.RequestParam;
import com.bestv.ott.appstore.utils.StringUtils;
import com.bestv.ott.appstore.utils.Utils;

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
	public static final String TAG_NULL1= "";
	public static final String TAG_NULL2 = "{}";
	public static final String TAG_BOUTIQUE = "boutique";//精品 非精品
	public static final String TAG_PERIPHERALS = "peripherals";//外设类型
	
	public static final String TAG_PROMOTIONPRICE = "promotionPrice"; 
	public static final String TAG_APPPRODUCTCODE = "appProductCode"; 
	public static final String TAG_APPPACKPRODUCTCODE = "appPackageProductCode"; 
	public static final String TAG_ORIGINALPRICE = "originalPrice"; 
	public static final String TAG_APPPACKID = "appPackageId"; 
	
	public static final int TAG_ONE = 1;
	private static final String TAG = "AppsParser";
	

	@Override
	public List<AppsBean> parseJSON(String paramString,PageBean pageBean) throws JSONException {
		if(checkResponse(paramString)==null){
			pageBean.calculatePageTotal(0);
			return null;
		}
		List<AppsBean> appsList=new ArrayList<AppsBean>();
		JSONObject jsonObj=new JSONObject(paramString);
		Log.d(TAG," 111 >>>>>>>>>>>>>>>>>>" + paramString );
		if(!paramString.contains(TAG_PAGE)||!paramString.contains(TAG_LIST)){
			return appsList;
		}
        JSONObject pageObj=jsonObj.getJSONObject(TAG_PAGE);
        JSONArray listArr=pageObj.getJSONArray(TAG_LIST);
        Object pageTotal=pageObj.get(TAG_TOTAL_LINE);
        
        Log.d(TAG," 222 >>>>>>>>>>>>>>>>>>" + pageObj );
        
//        AppLog.d("--------------------appsParser------pagetotal=",""+Integer.valueOf(pageTotal.toString()));
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
            
            /* 8-22增加[外设][精品]显示 */
            if(appObj.has(TAG_BOUTIQUE)){
            	apps.setBoutique(appObj.getBoolean(TAG_BOUTIQUE));
            }else{
            	apps.setBoutique(false);
            }
            if(appObj.has(TAG_PERIPHERALS)){
            	//TODO dropped by penghui
//            	apps.setPeripherals(setSplit(appObj.getString(TAG_PERIPHERALS)));
            	apps.setPeripherals(appObj.getString(TAG_PERIPHERALS));
            	AppLog.d(TAG, "----name ->"+appObj.getString(TAG_NAME) +"   ---->"+appObj.getString(TAG_PERIPHERALS));
            }else{
            	apps.setPeripherals("");
            }

            if(appObj.has(TAG_PROMOTIONPRICE)){
            	Log.d("AppsParser","OOOOOOOOOOOOO://" + appObj.getDouble(TAG_PROMOTIONPRICE));
            	if(!StringUtils.isBlank(appObj.getDouble(TAG_PROMOTIONPRICE))){ 
            		apps.setPromotionPrice(appObj.getDouble(TAG_PROMOTIONPRICE)); 
            	} 
            }

//            if(appObj.has(TAG_ORIGINALPRICE)){
//            	if(StringUtils.isBlank(appObj.getDouble(TAG_ORIGINALPRICE))){
//            		apps.setOriginalPrice(null); 
//            	}else{
//            		apps.setOriginalPrice(appObj.getDouble(TAG_ORIGINALPRICE)); 
//            	} 
//            } 
//          apps.setAppProductCode(appObj.getString(TAG_APPPRODUCTCODE));
//          apps.setAppPackageProductCode(appObj.getString(TAG_APPPACKPRODUCTCODE)); 
//    		apps.setPeripherals(appObj.getString(TAG_PERIPHERALS)); 
//    		apps.setAppPackageId(appObj.getString(TAG_APPPACKID)); 
            if(appObj.has("serviceCode")){
            	apps.setServiceCode(appObj.getString("serviceCode"));
            }
            
            String[] tempStr=imageSrc.split(TAG_SEPRATE);
            apps.setNatImageUrl(Utils.getImagePath()+tempStr[tempStr.length-1]);
            apps.setSerImageUrl(RequestParam.SERVICE_IMAGE_URL+imageSrc);
            if( !StringUtils.isBlank(appObj.getString(TAG_ID)) && !StringUtils.isBlank(appObj.getString(TAG_NAME)) ){
            	appsList.add(apps);
            }
//            Log.d(TAG, appObj.toString());
        }
        
		return appsList;
	}

	public String checkResponse(String paramString) throws JSONException {
		if(paramString == null){
			return null;
		}
		return paramString;
	}
	
	/* 切割返回串,获取支持外设类型 */
	public static String[] setSplit(String str){
		
		if(TAG_NULL1.equals(str)){
			return null;
		}
		
		int len = str.length();
		String peripherals[] = new String[len];
		
		if(len==TAG_ONE){
			peripherals[0] = str;
		}else if(str.contains(",")){
			peripherals = str.split(",");
		}else{
			return null;
		}
		return peripherals;
	}
}
