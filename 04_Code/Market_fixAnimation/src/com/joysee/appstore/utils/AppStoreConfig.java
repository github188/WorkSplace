package com.joysee.appstore.utils;

public class AppStoreConfig {

//	public static final String APPSTORE_MAIN_ADDRESS = "http://192.168.11.227:8080/btinterface/app/adtvAppShop/base";
//	public static final String INNER_TEST_ADDRESS = "http://192.168.11.85:8080/btinterface/app/adtvAppShop/base";
//	public static final String INNER_TEST_ADDRESS = "http://221.10.89.62:8080/btinterface/app/adtvAppShop/base";
//	public static final String INNER_TEST_ADDRESS = "http://192.168.11.69/Apphtml/index.html";
//	public static final String INNER_TEST_ADDRESS = "http://192.168.11.5:8080/Apphtml/index.html";
//	public static final String INNER_TEST_ADDRESS = "http://192.168.11.126/Test3D/test3D.html";
//	public static final String INNER_TEST_ADDRESS = "http://221.10.89.59:8080/btinterface/app/adtvAppShop/base";
	public static final String APPSTORE_MAIN_ADDRESS = RequestParam.NET_DNS+"/btinterface/app/adtvAppShop/base";
	
//	public static final String INNER_TEST_ADDRESS = "http://192.168.11.5:8080/downLoad";
	
	
//	public static final String APK_ADDRESS = "http://192.168.11.126/Calculator.apk";
	public static final boolean DEBUG = true;
	
	/**
	 * 开机加载(网络、SD卡)监听、遍历系统APP、缓存查找  的log开关
	 */
	public static final boolean DEBUGE = false;
	
	public static final boolean DOWNLOADDEBUG = false;
	
	public static final boolean THIRDPARTDEBUG = true;
	
	
//	public static final String APPSTORE_UPDATE_ADDRESS = "http://192.168.11.172:8080/btopinterface/app/appInterface?action=getUpdateAppList";
	
	public static final String APPSTORE_UPDATE_ADDRESS = RequestParam.NET_DNS+"/btinterface/app/appInterface?action=getUpdateAppList";
	
	
}
