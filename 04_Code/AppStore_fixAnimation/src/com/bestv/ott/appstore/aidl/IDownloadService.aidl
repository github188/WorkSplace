package com.bestv.ott.appstore.aidl;

import com.bestv.ott.appstore.common.TaskBean;
import com.bestv.ott.appstore.common.AppsBean;

interface IDownloadService{
	
		 boolean pauseTask(in TaskBean task) ;

		 boolean continueTask(in TaskBean task);
		
		 void startDownload(in AppsBean appBean) ;
		
		 boolean delTask(in TaskBean task);
		 
		 int getErrorCount(int taskId);

}