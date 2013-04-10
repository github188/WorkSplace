package com.joysee.appstore.common;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;

import com.joysee.appstore.utils.AppLog;
import com.joysee.appstore.utils.NetUtil;
import com.joysee.appstore.utils.RequestParam;
import com.joysee.appstore.utils.Utils;

/**
 * 请求数据的操作
 * @author Administrator
 *
 */
public class DataOperate {
    
    private static final String TAG="com.joysee.appstore.common.PageOperate";
    
    @SuppressWarnings("unused")
    private Context con; 
    //private String action_url;            //连接地址
    private Map<String,String> param=new TreeMap<String,String>();             //要查的参数
    private PageBean pageBean;
    private Handler handler;
    private int typeId;//分类ID,如果为-1,表示查询所有
    private String path;//图片存放路径
    private int DELAYED_TIME=200;
    private NetUtil mNetUtil;
    
    public DataOperate(Context con,Handler handler){
        this.con=con;
        this.handler=handler;
        //this.path=con.getFilesDir().getPath()+"/images/";
        this.path=Utils.getSDPath(); 
        AppLog.d(TAG,"--------cache path : "+ path+"---------------");
        File file=new File(path);
        if(!file.exists()){
            file.mkdirs();
        }
        mNetUtil = new NetUtil(con);
    }
    
    public DataOperate(Context con){
    	this.con=con;
    	this.path=Utils.getSDPath(); 
        File file=new File(path);
        AppLog.d(TAG,"--------cache path : "+ path+"---------------");
        if(!file.exists()){
            file.mkdirs();
        }
        mNetUtil = new NetUtil(con);
    }
    
    @SuppressWarnings("unchecked")
    public List getDataFormService(int action_index){
        List resList=null; 
        param.clear();
        switch(action_index){
        case RequestParam.ActionIndex.GETRECOMMENDLIST://获得应用推荐榜信息(最新)
            resList=getRecommendAppsList(RequestParam.Action.GETRECOMMENDLIST);
            break;
        case RequestParam.ActionIndex.GETAPPTYPELIST://获得类型列表
            resList=getAppsClassList(RequestParam.Action.GETAPPTYPELIST);
            break;
        case RequestParam.ActionIndex.GETAPPLIST://获得应用列表
            resList=getAppsList(RequestParam.Action.GETAPPLIST);
            break;
        case RequestParam.ActionIndex.GETTOPLIST://获得应用排行榜信息(最热)
            resList=getTopAppsList(RequestParam.Action.GETTOPLIST);
            break;
        case RequestParam.ActionIndex.GETSEARCHLIST://获取搜索列表
        	resList = getAppsSearchList(RequestParam.Action.GETAPPLIST);
        	break;
        }
        return resList;
    }
    
    /**
     * 获取某个应用的详情
     * @param appID
     * @param action_url
     * @return
     */
    public AppsBean getAppDetail(int appID){
        AppsBean appBean=new AppsBean();
        param.clear();
        param.put(RequestParam.Param.APPID,String.valueOf(appID));
        String jsonStr=mNetUtil.getAppsByStatus(param, RequestParam.getAppServerUrl(con) +RequestParam.Action.GETAPPDETAIL);
        AppLog.d(TAG, "jsonStr="+jsonStr);
        if(jsonStr.equals(RequestParam.NET_ERROR)){
            return null;
        }
        try {
            JSONObject jsonObj=new JSONObject(jsonStr);
            JSONObject appObj=null;
            if(jsonObj.has("app")){
            	appObj=jsonObj.getJSONObject("app");
            	if(null==appObj.getString("cname")||appObj.getString("cname").equals("")){
            		return appBean;
            	}
            }else{
            	return appBean;
            }
            
            appBean.setID(appObj.getInt("id"));
            appBean.setAppName(appObj.getString("cname"));
            //appBean.setPkgName(appO);
            appBean.setRemark(appObj.getString("remark"));
            appBean.setScore(appObj.getString("score"));
            appBean.setApkUrl(RequestParam.getFileServerUrl(con)+appObj.getString("filepath"));
            String imageSrc=appObj.getString("icon");
            String[] tempStr=imageSrc.split("/");
            appBean.setNatImageUrl(path+tempStr[tempStr.length-1]);
            appBean.setSerImageUrl(RequestParam.getFileServerUrl(con)+imageSrc);
            appBean.setSize(appObj.getInt("size"));
            appBean.setVersion(appObj.getString("version"));
            appBean.setTypeName(appObj.getString("typeName"));
            appBean.setCreateTime(appObj.getString("createtime"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        loadOneImage(appBean,AppDetailedActivity.MSG_SHOW_APP_DETAIL);
        return appBean;
    }
    /**
     * 获得应用推荐榜信息,系统启动后默认的
     * @return
     */
    public List<AppsBean> getRecommendAppsList(String action_url){
        AppLog.d(TAG,"---getApps---action_url="+action_url+";pageNo="+pageBean.getPageNo());
        param.put(RequestParam.Param.PAGENO,String.valueOf(pageBean.getPageNo()));
        param.put(RequestParam.Param.LINENUMBER, String.valueOf(pageBean.getPageSize()));
        String jsonStr=mNetUtil.getAppsByStatus(param, RequestParam.getAppServerUrl(con) +action_url);
        if(jsonStr.equals(RequestParam.NET_ERROR)){
            return null;
        }
        List<AppsBean> tempList=new ArrayList<AppsBean>();
        try {
            JSONObject jsonObj=new JSONObject(jsonStr);
            if(null==jsonObj){
                pageBean.calculatePageTotal(0);
                return tempList;
            }
            JSONObject pageObj=jsonObj.getJSONObject("page");
            if(null==jsonObj){
                pageBean.calculatePageTotal(0);
                return tempList;
            }
            JSONArray listArr=pageObj.getJSONArray("list");
            Object pageTotal=pageObj.get("totalLine");
            pageBean.calculatePageTotal(Integer.valueOf(pageTotal.toString()));
            JSONObject appObj;
            setPath();
            for(int i=0;i<listArr.length();i++){
                appObj=listArr.getJSONObject(i);
                AppsBean apps=new AppsBean();
                apps.setID(appObj.getInt("id"));
                apps.setAppName(appObj.getString("name"));
                String imageSrc=appObj.getString("image");
                apps.setPrice(appObj.getDouble("price"));
                if(appObj.has("packageName")){
                	apps.setPkgName(appObj.getString("packageName"));
                }else{
                	apps.setPkgName("");
                }
                if(appObj.has("versionCode")){
                	apps.setVersion(appObj.getString("versionCode"));
                }
                String[] tempStr=imageSrc.split("/");
                apps.setNatImageUrl(path+tempStr[tempStr.length-1]);
                apps.setSerImageUrl(RequestParam.getFileServerUrl(con)+imageSrc);
                if(appObj.getString("id")!=null&&!appObj.getString("id").equals("")
                		&&appObj.getString("id")!=null&&!appObj.getString("id").equals("")
                		&&appObj.getString("name")!=null&&!appObj.getString("name").equals("")){
                	tempList.add(apps);
                }
                
            }
        } catch (JSONException e) {
            AppLog.d(TAG, "*********error************* getRecommendAppsList json is error");
            e.printStackTrace();
        }
        if(pageBean.getPageTotal()==0){
        	pageBean.setPageNo(0);
        }
        long time4=System.currentTimeMillis();
        return tempList;
    }
    
    
    /**
     * 获得应用排行榜信息 
     * @return
     */
    public List<AppsBean> getTopAppsList(String action_url){ 
        AppLog.d(TAG,"---getApps---action_url="+action_url+";pageNo="+pageBean.getPageNo());
        param.put(RequestParam.Param.PAGENO,String.valueOf(pageBean.getPageNo()));
        param.put(RequestParam.Param.LINENUMBER, String.valueOf(pageBean.getPageSize()));
        String jsonStr=mNetUtil.getAppsByStatus(param, RequestParam.getAppServerUrl(con) +action_url);
        if(jsonStr.equals(RequestParam.NET_ERROR)){
            return null;
        }
        List<AppsBean> tempList=new ArrayList<AppsBean>();
        try {
            JSONObject jsonObj=new JSONObject(jsonStr);
            if(null==jsonObj){
                pageBean.calculatePageTotal(0);
                return tempList;
            }
            JSONObject pageObj=jsonObj.getJSONObject("page");
            if(null==jsonObj){
                pageBean.calculatePageTotal(0);
                return tempList;
            }
            JSONArray listArr=pageObj.getJSONArray("list");
            Object pageTotal=pageObj.get("totalLine");
            pageBean.calculatePageTotal(Integer.valueOf(pageTotal.toString()));
            JSONObject appObj;
            setPath();
            for(int i=0;i<listArr.length();i++){
                appObj=listArr.getJSONObject(i);
                AppsBean apps=new AppsBean();
                apps.setID(appObj.getInt("id"));
                apps.setAppName(appObj.getString("name"));
                apps.setPrice(appObj.getDouble("price"));
                String imageSrc=appObj.getString("image");
                String[] tempStr=imageSrc.split("/");
                apps.setNatImageUrl(path+tempStr[tempStr.length-1]);
                apps.setSerImageUrl(RequestParam.getFileServerUrl(con)+imageSrc);
                if(appObj.getString("id")!=null&&!appObj.getString("id").equals("")
                		&&appObj.getString("id")!=null&&!appObj.getString("id").equals("")
                		&&appObj.getString("name")!=null&&!appObj.getString("name").equals("")){
                	tempList.add(apps);
                }
            }
        } catch (JSONException e) {
            AppLog.d(TAG, "*********error************* getRecommendAppsList json is error");
            e.printStackTrace();
        }
        if(pageBean.getPageTotal()==0){
        	pageBean.setPageNo(0);
        }
//        loadListImage(tempList,AppStoreActivity.MSG_FINISH_ONE_APP);
        return tempList;
    }
    
    /**
     * 根据分类id获得应用信息
     * @return
     */
    public List<AppsBean> getAppsList(String action_url){
        AppLog.d(TAG,"---getApps---action_url="+action_url+";pageNo="+pageBean.getPageNo());
        if(typeId>=0){
            param.put(RequestParam.Param.TYPEID,String.valueOf(typeId));
        }
        param.put(RequestParam.Param.PAGENO,String.valueOf(pageBean.getPageNo()));
        param.put(RequestParam.Param.LINENUMBER, String.valueOf(pageBean.getPageSize()));
        String jsonStr=mNetUtil.getAppsByStatus(param, RequestParam.getAppServerUrl(con) +action_url);
        if(jsonStr.equals(RequestParam.NET_ERROR)){
            return null;
        }
        List<AppsBean> tempList=new ArrayList<AppsBean>();
        try {
            JSONObject allObj=new JSONObject(jsonStr);
            if(null==allObj){
                pageBean.calculatePageTotal(0);
                return tempList;
            }
            JSONObject pageObj=allObj.getJSONObject("page");
            JSONArray listArr=pageObj.getJSONArray("list");
            Object pageTotal=pageObj.get("totalLine");
            pageBean.calculatePageTotal(Integer.valueOf(pageTotal.toString()));
            JSONObject appObj;
            setPath();
            for(int i=0;i<listArr.length();i++){
                appObj=listArr.getJSONObject(i);
                AppsBean apps=new AppsBean();
                apps.setID(appObj.getInt("id"));
                apps.setAppName(appObj.getString("name"));
//                String typeIDStr=appObj.getString("typeId");
                apps.setPrice(appObj.getDouble("price"));
//                apps.setTypeID(typeIDStr.equals("")?-1:Integer.valueOf(typeIDStr));
                String imageSrc=appObj.getString("image");
                String[] tempStr=imageSrc.split("/");
                apps.setNatImageUrl(path+tempStr[tempStr.length-1]);
                apps.setSerImageUrl(RequestParam.getFileServerUrl(con)+imageSrc);
                if(appObj.getString("id")!=null&&!appObj.getString("id").equals("")
                		&&appObj.getString("id")!=null&&!appObj.getString("id").equals("")
                		&&appObj.getString("name")!=null&&!appObj.getString("name").equals("")){
                	tempList.add(apps);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(pageBean.getPageTotal()==0){
        	pageBean.setPageNo(0);
        }
//        loadListImage(tempList,AppStoreActivity.MSG_FINISH_ONE_APP);
        return tempList;
    }
    
    
 
    
    /**
     * 根据分类id获得应用信息
     * @return
     */
    public List<AppsBean> getAppsSearchList(String action_url){
        AppLog.d(TAG,"---getApps---action_url="+action_url+";pageNo="+pageBean.getPageNo()+pageBean.getKeyWord());
//        if(typeId>=0){
//            param.put(RequestParam.Param.TYPEID,String.valueOf(typeId));
//        }
//        param.put(RequestParam.Param.PAGENO,String.valueOf(pageBean.getPageNo()));
//        param.put(RequestParam.Param.LINENUMBER, String.valueOf(pageBean.getPageSize()));
        param.put(RequestParam.Param.KEYWORD, pageBean.getKeyWord());
        
        String jsonStr=mNetUtil.getAppsByStatus(param, RequestParam.getAppServerUrl(con) +action_url);
        if(jsonStr.equals(RequestParam.NET_ERROR)){
            return null;
        }
        List<AppsBean> tempList=new ArrayList<AppsBean>();
        try {
            JSONObject allObj=new JSONObject(jsonStr);
            if(null==allObj){
                pageBean.calculatePageTotal(0);
                return tempList;
            }
            JSONObject pageObj=allObj.getJSONObject("page");
            JSONArray listArr=pageObj.getJSONArray("list");
            Object pageTotal=pageObj.get("totalLine");
            if(Integer.valueOf(pageTotal.toString())==0){
            	pageBean.calculatePageTotal(0);
                return tempList;
            }
            pageBean.calculatePageTotal(Integer.valueOf(pageTotal.toString()));
            JSONObject appObj;
            setPath();
            for(int i=0;i<listArr.length();i++){
                appObj=listArr.getJSONObject(i);
                AppsBean apps=new AppsBean();
                apps.setID(appObj.getInt("id"));
                apps.setAppName(appObj.getString("name"));
//                String typeIDStr=appObj.getString("typeId");
//                apps.setTypeID(typeIDStr.equals("")?-1:Integer.valueOf(typeIDStr));
                apps.setVersion("("+appObj.getString("version")+")");
                apps.setTypeName(""+appObj.getString("typeNames").replace("[", "").replace("\"", "").replace("]", ""));
                String imageSrc=appObj.getString("image");
                String[] tempStr=imageSrc.split("/");
                apps.setNatImageUrl(path+tempStr[tempStr.length-1]);
                apps.setSerImageUrl(RequestParam.getFileServerUrl(con)+imageSrc);
                if(appObj.getString("id")!=null&&!appObj.getString("id").equals("")
                		&&appObj.getString("id")!=null&&!appObj.getString("id").equals("")
                		&&appObj.getString("name")!=null&&!appObj.getString("name").equals("")){
                	tempList.add(apps);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return tempList;
    }
    
    /**
     * 根据json解析获取应用分类信息
     * @return
     */
    public List<AppsBean> getAppsClassList(String action_url){
        AppLog.d(TAG,"---getAppsClass---action_url="+action_url);
        String jsonStr=mNetUtil.getAppsByStatus(param, RequestParam.getAppServerUrl(con) +action_url);
        if(jsonStr.equals(RequestParam.NET_ERROR)){
            return null;
        }
        List<AppsBean> tempList=new ArrayList<AppsBean>();
        try {
            JSONArray typesObj=new JSONObject(jsonStr).getJSONArray("Types");
            JSONObject appObj;
            setPath();
            for(int i=0;i<typesObj.length();i++){
                appObj=typesObj.getJSONObject(i);
                AppsBean apps=new AppsBean();
                apps.setID(appObj.getInt("@id"));
                apps.setAppName(appObj.getString("@name"));
                String imageSrc=appObj.getString("@nImg");
                String[] tempStr=imageSrc.split("//");
                String[] temp2Str=tempStr[1].split("/");
                apps.setNatImageUrl(path+temp2Str[temp2Str.length-1]);
                apps.setSerImageUrl(imageSrc);
                if(appObj.getString("@id")!=null&&!appObj.getString("@id").equals("")){
                	tempList.add(apps);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        loadListImage(tempList,AppStoreActivity.MSG_FINISH_ONE_CLASS);
        return tempList;
    }
    /**
     * 获取已安装或已升级应用的分页数据
     * @return
     */
    public List<ApplicationBean> getOnePageAppsList(List<ApplicationBean> all){
        List<ApplicationBean> tempList=new ArrayList<ApplicationBean>();
        if(null==all||all.size()<1){
            return tempList;
        }
        if(all.size()<=pageBean.getPageSize()){
            return all;
        }else{
            int startIndex,endIndex;
            int pageNo=pageBean.getPageNo();
            startIndex=(pageNo-1)*pageBean.getPageSize();
            endIndex=pageNo*pageBean.getPageSize();
            if(pageNo==pageBean.getPageTotal()){
                endIndex=all.size();
            }
            for(int i=startIndex;i<endIndex;i++){
                tempList.add(all.get(i));
            }
        }
        return tempList;
    }
    
    /**
     * 获取可升级应用
     * @param install已安装应用
     * @return
     */
    public List<ApplicationBean> getNativeUpdateAppsList(List<ApplicationBean> install){
        List<ApplicationBean> tempList=new ArrayList<ApplicationBean>();
        try {
            String josnString = mNetUtil.connect(con.getApplicationContext(),install);
            AppLog.d(TAG, "--------------------josnString :-"+josnString);
            JSONObject json=new JSONObject(josnString);
            if(!json.has("appList")){
            	return null;
            }
            JSONArray arr = json.getJSONArray("appList");
            if(null==arr||arr.length()<1){
                return null;
            }
            JSONObject updObj;
            ApplicationBean tempApp;
            for(int i=0;i<arr.length();i++){
                tempApp=new ApplicationBean();
                updObj=arr.getJSONObject(i);
                if(updObj.has("id")){
                	tempApp.setId(updObj.getInt("id"));
                }
                tempApp.setPkgName(updObj.getString("packageName"));
                tempApp.setAppName(updObj.getString("appName"));
                tempApp.setVersion(updObj.getString("versionCode"));
                tempApp.setTypeName(updObj.getString("typeName"));
                tempApp.setUrl(RequestParam.getFileServerUrl(con)+updObj.getString("url"));
                tempApp.setIconUrl(RequestParam.getFileServerUrl(con)+updObj.getString("icon"));
                tempList.add(tempApp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return tempList;
    }
    
    /**
     * 下载图片
     * @param apps
     * @param action_url不同的url,执行的handler的message不同
     */
    public void loadListImage(List<AppsBean> list,int Msg){
        if(null==list||list.size()<1){
            return;
        }else{
        	AppLog.d(TAG,"=======================list.size()>0===");
        	 new ImageDownloadTask().execute(list,handler,Msg);
        }
//        for(int i=0;i<list.size();i++){
//            AppsBean app=list.get(i);
//            File imgFile=new File(app.getNatImageUrl());
//            if(imgFile.exists()&&imgFile.length()>0){
//                handler.sendEmptyMessageDelayed(Msg, DELAYED_TIME);
//            }else{
//            	++count;
//            }
//
//        }
        
        	
    }
    
    /**
     * 下载图片
     * @param apps
     * @param action_url不同的url,执行的handler的message不同
     */
    public void loadOneImage(AppsBean app,int Msg){
        File imgFile=new File(app.getNatImageUrl());
        if(imgFile.exists()&&imgFile.length()>0){
            handler.sendEmptyMessageDelayed(Msg, DELAYED_TIME);
        }else{
        	List<AppsBean> list = new ArrayList<AppsBean>();
        	list.add(app);
            new ImageDownloadTask().execute(list,handler,Msg);
        }
    }
    
    
    
    /**
     * 每次设置路径时，判断sd card在不在，不在就重新设置
     */
    public void setPath(){
        boolean sdCardExist=Utils.checkSDcard();
        if(!sdCardExist){
            AppLog.d(TAG, "sd card is not exist!!!!!!!!!");
            path=Constants.IMAGEROOT;
            File file=new File(path);
            if(!file.exists()){
                file.mkdirs();
            }
        }else{
            AppLog.d(TAG, "sd card is  exist");
        }
    }
 
    @SuppressWarnings("unchecked")
    public Map getParam() {
        return param;
    }

    @SuppressWarnings("unchecked")
    public void setParam(Map param) {
        this.param = param;
    }

    public PageBean getPageBean() {
        return pageBean;
    }

    public void setPageBean(PageBean pageBean) {
        this.pageBean = pageBean;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }
    
    
    /**
     * 提交用户评分
     * @param userId
     * @param appID
     * @param appScore
     */
    public Boolean setScoreToService(String userId,int appID,Float appScore){
    	 param.clear();
    	 param.put(RequestParam.Param.USERID,userId);
    	 param.put(RequestParam.Param.APPID,String.valueOf(appID));
    	 param.put(RequestParam.Param.SCORE,String.valueOf(appScore));
    	 String jsonStr=mNetUtil.getAppsByStatus(param, RequestParam.getAppServerUrl(con) +RequestParam.Action.USERVOTE);
    	 AppLog.d(TAG, "-----------------------setScoreToService jsonStr = "+jsonStr+"--------------------");
    	 if(jsonStr.equals(RequestParam.NET_ERROR)){
    		 return false;
         }
    	 try{
    		 JSONObject jsonObj=new JSONObject(jsonStr);
             if(jsonObj.has("success")){
            	 if(jsonObj.getBoolean("success")==true){
            		return true; 
            	 }else{
            		 return false;
            	 }
             }else{
            	 return false;
             }
    	 }catch(JSONException e){
    		 e.printStackTrace();
    	 }
    	 return null;
    }
    
    
    /**
     * 获取用户对应某个应用的评分信息
     * @param userID
     * @param appID
     * @return
     */
     public Map<Boolean,Integer> getScorFormService(String userId,int appID){
    //public Boolean getScorFormService(String userId,int appID){
    	param.clear();
    	param.put(RequestParam.Param.USERID,userId);
    	param.put(RequestParam.Param.APPID,appID+"");
    	String jsonStr=mNetUtil.getAppsByStatus(param, RequestParam.getAppServerUrl(con) +RequestParam.Action.GETUSERISSCORE);
    	Map<Boolean,Integer> map = new HashMap<Boolean, Integer>();
    	AppLog.d(TAG, "-----------------------getScorFormService jsonStr = "+jsonStr+"--------------------");
    	if(jsonStr.equals(RequestParam.NET_ERROR)){
   		   return null;
        }
    	try{
    		JSONObject jsonObj=new JSONObject(jsonStr);
    		if(jsonObj.has("score")){
            	if(jsonObj.getInt("score") == -1){
            		AppLog.d(TAG, "----------------------the user is no Score--------------------");
            		map.put(false, -1);
            		return map;
            	}else{
            		map.put(true, jsonObj.getInt("score"));
            		return map;
            	}
            }else{
            	return null;
            }
    	}catch(JSONException e){
    		AppLog.d(TAG, "-----------------------getScorFormService  ERROR!!!!!!!--------------------");
    	}
    	return null;
    }
    
     /** 获取应用商店升级 */
     public AppsBean getAppStoreUpdate(String pkgName,String verCode){
         AppsBean apps=new AppsBean();
         try {
             param.clear();
             param.put(RequestParam.Param.PKGNAME,pkgName);
             param.put(RequestParam.Param.VERCODE, verCode);
             param.put(RequestParam.Param.DOMAIN, "");
             List<ApplicationBean> install=new ArrayList<ApplicationBean>();
             ApplicationBean tempApp=new ApplicationBean();
             tempApp.setPkgName(pkgName);
             tempApp.setVersion(verCode);
             install.add(tempApp);
             String josnString = NetUtil.connect(con.getApplicationContext(),install,RequestParam.Action.APPSTOREUPDATE);
             AppLog.d(TAG, "--------------------josnString :"+josnString);
             if(null==josnString||josnString.equals("{}")){
                 return null;
             }
             JSONObject json=new JSONObject(josnString);
             apps.setPkgName(json.getString("packageName"));
             apps.setVersion(json.getString("versionCode"));
             apps.setApkUrl(RequestParam.getFileServerUrl(con)+json.getString("filepath"));
             apps.setAppName(json.getString("appName"));
             if(json.has("Force")){
            	 AppLog.d(TAG, "Force : "+json.getInt("Force"));
            	 apps.setUpdateTag(json.getInt("Force"));
             }else{
            	 apps.setUpdateTag(0);
             }
             apps.setSerImageUrl(null);
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         }
         return apps;
     }

}
