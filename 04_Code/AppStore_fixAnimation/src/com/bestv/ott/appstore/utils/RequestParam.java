package com.bestv.ott.appstore.utils;

/**
 * 网络请求的参数定义
 * @author Administrator
 *
 */
public class RequestParam {
	
    /* 黄俊最近搭建 */
//    public static final String NET_DNS = "http://61.172.244.22";
//    public static final String NET_DNS_IMG="http://61.172.244.22:8080";
    
//	public static final String NET_DNS="http://192.168.11.80";
//	public static final String NET_DNS_IMG="http://192.168.11.155:8080";
	
   
//	public static final String NET_DNS="http://221.221.146.139";//joysee-->百视通的公网
//	public static final String NET_DNS_IMG="http://221.221.146.139:8080";
    
//	public static final String NET_DNS="http://117.79.92.76:8080"; // Market的公网
//	public static final String NET_DNS_IMG="http://117.79.92.76:8080";
	
//	  public static final String NET_DNS="http://192.168.0.18:80"; //joysee
//	  public static final String NET_DNS_IMG="http://192.168.0.18:8080";
	
    /* 百视通连接地址 */
    public static final String NET_DNS="http://btop.bbtv.cn:80";  
    public static final String NET_DNS_IMG="http://btop.bbtv.cn:8080";
    
    public static final String SERVICE_CONNECT_URL = NET_DNS+"/btinterface";
    public static final String SERVICE_ACTION_URL = NET_DNS+"/btinterface/app/appInterface?action=";
    public static final String SERVICE_IMAGE_URL = NET_DNS_IMG+"/filemanage/UpShop/productApp/"; 
    
    
    /**
     * 首页获取正在销售的大礼包列表
     */
    public static final String URL_PACK_SALING = NET_DNS+"/btinterface/app/appInterface?action=getAppPackageList";
    /**
     * 大礼包详情接口
     */
    public static final String URL_PACK_DETAILS = NET_DNS+"/btinterface/app/appInterface?action=getAppPackageDetail&appPackageId=";

    public static final String NET_ERROR="NET_ERROR";
    
    /**
     * 请求ACTION种类
     */
    public static final class Action{
        public static final String GETLIST="getList";//获得列表
        public static final String GETPAYAPPLIST="getPayAppList";//获得订购应用列表
        public static final String GETAPPTYPELIST="getAppTypeList";//获得类型列表
        public static final String GETAPPLIST="getAppList";//获得应用列表
        public static final String GETAPPDETAIL="getAppDetail";//获得应用明细信息
        public static final String GETTOPLIST="getTopList";//获得应用排行榜信息
        public static final String GETRECOMMENDLIST="getRecommendList";//获得应用推荐榜信息
        public static final String GETLATESTAPPS="getLatestapps";//获得最新应用
        public static final String GETCOMMENTLIST="getCommentList";//获得应用评论列表
        public static final String PAYMENT="Payment";//付费
        public static final String USERVOTE="userVote";//用户评分信息
        public static final String GETUSERISSCORE="getUserIsScore";//获取用户对某一应用评分信息
        public static final String RECORDAPPDOWNLOADS="recordAppDownloads";//让服务端记录下载次数
        public static final String GETAPPPACKAGELIST="getAppPackageList";//获取大礼包信息
    }
    
    /**
     * 请求ACTION种类index
     */
    public static final class ActionIndex{
        public static final int GETLIST=0;//获得列表
        public static final int GETPAYAPPLIST=1;//获得订购应用列表`
        public static final int GETAPPTYPELIST=2;//获得类型列表
        public static final int GETAPPLIST=3;//获得应用列表
        public static final int GETAPPDETAIL=4;//获得应用明细信息
        public static final int GETTOPLIST=5;//获得应用排行榜信息
        public static final int GETRECOMMENDLIST=6;//获得应用推荐榜信息
        public static final int GETCOMMENTLIST=7;//获得应用评论列表
        public static final int PAYMENT=8;//付费
        public static final int USERVOTE=9;//用户评分信息
        public static final int GETSEARCHLIST=10;//获取搜索列表
    }
    
    
    /**
     * 请求中可能会带到的参数
     */
    public static final class Param{
        public static final String USERID="userId";//请求用户ID
        public static final String SESSIONID="sessionId";//请求SESSIONID
        public static final String PAGENO="pageNo";//请求第几页
        public static final String LINENUMBER="pageSize";//每页要显示的数据
        public static final String TYPEID="typeId";//请求类型id
        public static final String KEYWORD="keyWord";//请求关键字
        public static final String APPID="appId";//请求应用ID
        public static final String LETTERNM="letterNm";//关键字
        public static final String SUM="sum";//付费金额
        public static final String SCORE="score";//评分
        public static final String GROUPID="groupId";//请求用户组ID
        public static final String PAYFLAG="payFlag";//请求用户组ID
    }
    
    /**
     * 分类ID,这里只有最新,最热的，别的分类ID都是服务器给的
     * @author Administrator
     *
     */
    public static final class CalssID{
        public static final int NEW_ID=-2;//最新
        public static final int HOT_ID=-1;//最热
        public static final int ALL_ID=-3;//全部
    }

}
