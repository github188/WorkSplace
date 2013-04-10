package com.joysee.appstore.aidl;

import com.joysee.appstore.common.TaskBean;
import com.joysee.appstore.common.AppsBean;

interface IDownloadService{
	
		 boolean pauseTask(in TaskBean task) ;

		 boolean continueTask(in TaskBean task);
		
		 void startDownload(in AppsBean appBean) ;
		
		 boolean delTask(in TaskBean task);
		 
		 int getErrorCount(int taskId);
		 
		 void deletePackage(String pkgname);

}