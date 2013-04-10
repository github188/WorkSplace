package com.bestv.ott.appstore.common;

public class Constants {
	public final static int THREADNUM = 1;
	
	public final static int THREADMULTNUM = 2;
	
	public final static int MAXERRORCOUNT = 1;
	
	public static final long TIGGERAPP = 1024 * 1024*5l; // apk block >5M
	
	public static final int LAYERINDEX = 100;
	
	public final static int TIMEOUT = 6*1000;
	public final static int MAX_TIMEOUT = 60*1000;
	public final static int  WIDTH =1280;
	public final static int  HEIGHT=720;
	public final static int  RATE = 10;
	
	public static final int DOWNLOAD_STATUS_PAUSE = 0;
	public static final int DOWNLOAD_STATUS_EXECUTE = 1;
	public static final int DOWNLOAD_STATUS_EXIT_STOP = 7;//because application destory  uncompleted task
	public static final int DOWNLOAD_STATUS_ERROR = 8;//download error
	
	public static final int APP_STATUS_UNDOWNLOAD = -1;
	public static final int APP_STATUS_DOWNLOADING = DOWNLOAD_STATUS_EXECUTE;
	public static final int APP_STATUS_DOWNLOADED = 2;
	public static final int APP_STATUS_INSTALLED = 3;//已安装
	public static final int APP_STATUS_UPDATE = 4;//在升级
	public static final int APP_STATUS_DOWNLOAD_ERROR = 5;
	public static final int APP_STATUS_INSTALL_FAIL = 6;
	
	public static final int APP_SOURCE_STORE=0;//应用商城应用
	public static final int APP_SOURCE_INNER=1;//内置应用
	public static final int APP_SOURCE_INNER_UPDATE=2;//内置应用升级版本
	
	public static final String DEFAULT_VERSION="1.0.0";//内置应用默认版本
	
	public static final long MAX_PROGRESS_TIME = 1500;
	
	public static final int KEYCODE_ESC = 111;
	
	public static final int KEYCODE_BACK = 4;

	public static final String INTENT_DOWNLOAD_STARTED = "com.bestv.ott.appstore.DOWNLOAD_STARTED";
	
	public static final String INTENT_DOWNLOAD_PAUSE = "com.bestv.ott.appstore.DOWNLOAD_PAUSE";
	
	public static final String INTENT_DOWNLOAD_PROGRESS = "com.bestv.ott.appstore.DOWNLOAD_PROGRESS";
	
	public static final String INTENT_DOWNLOAD_COMPLETED = "com.bestv.ott.appstore.DOWNLOAD_COMPLETED";
	
	public static final String INTENT_DOWNLOAD_DETLET = "com.bestv.ott.appstore.DOWNLOAD_DETLET";
	
	public static final String INTENT_INSTALL_COMPLETED = "com.bestv.ott.appstore.INSTALL_COMPLETED";
	
	public static final String INTENT_UPDATE_COMPLETED = "com.bestv.ott.appstore.UPDATE_COMPLETED";
	
	public static final String INTENT_INSTALL_SUCCESS = "com.bestv.ott.appstore.INSTALL_SUCCESS";
	
	public static final String INTENT_INSTALL_FAIL = "com.bestv.ott.appstore.INSTALL_FAIL";
	
	public static final String INTENT_UNINSTALL_COMPLETED = "com.bestv.ott.appstore.UNINSTALL_COMPLETED";
	
	public static final String INTENT_DOWNLOAD_ERROR = "com.bestv.ott.appstore.DOWNLOAD_ERROR";
	
	public static final String PACKAGENAME = "com.bestv.ott.appstore";
	
	public static final String INTENT_ACTION_APPSTORE = "bestv.ott.action.appstore";
	
	public static final String INTENT_ACTION_INSTALL_SUC = "com.bestv.appinstall.success";
	
	public static final String INTENT_ACTION_INSTALL_FAIL = "com.bestv.appinstall.fail";
	
	public static final String INTENT_ACTION_UPDATESCORE_SUCCESS = "com.bestv.upscore.success";
	
	public static final int MAX_LENGTH=8;//我的应用列表显示最多的个数 
	
	public static final int FIRST_PAGE=0;      //第一页，第一次进入时显示第一页
	public static final int PREVIOUS_PAGE=1;   //上一页，删除appsMap右边，向左边插入数据
	public static final int NEXT_PAGE=2;       //下一页:删除appsMap的左边,向右边插入数据
	public static final int PAGE_NUM=5;        //缓存默认存多少页
	
	public static final String JPEG="jpeg";
	public static final String JPG="jpg";
	
	public static final String IMAGEROOT="/data/data/com.bestv.ott.appstore/files/images/";//如果没有sd card，就会把图片存在这个目录下
	
	/*服务器应用对应的类型*/
	public static final int APP_ALL=0;//全部
	
	public static final class Delayed{
		public static final int TIME1=100;
	    public static final int TIME2=300;//响应暂停/继续下载的时间间隔
	    public static final int TIME5=500;
	    public static final int TIME10=1000;
	    public static final int TIME20=2000;
	    public static final int TIME30=3000;
	}
	
	public static final int MENU_LEFT_ID=100;
	public static final int MENU_RIGHT_ID=101;
	
	public static final int MAX_TEXT_INPUT_LENGTH=20;
}
