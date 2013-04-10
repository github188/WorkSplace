package com.joysee.appstore.aidl;
import com.joysee.appstore.common.AppsBean;

interface IAppStoreSearch{

    //查询所有可更新应用
	List<AppsBean> searchAllUpgrade();
	
	//查询多个应用是否可升级
	List<AppsBean> searchSomeUpgrade(in List<String> pkgNameList);

	//查询某个应用是否有升级版本,有则直接升级,返回<pkgname,version> / null
	AppsBean isUpgradeForOne(String pkgName);
	
	//查询某个应用是否可升级,查出但不升级
	AppsBean isUpgrade(String pkgName);
	
	//升级某个已知可更新应用
	//void upgradeOneApp(in AppsBean appsBean); 
	
	//根据关键字搜索
	List<AppsBean> searchInAppStore(String condition);
}