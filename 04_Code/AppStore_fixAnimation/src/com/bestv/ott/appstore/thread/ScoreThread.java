package com.bestv.ott.appstore.thread;

import android.content.Context;
import android.content.Intent;
import android.os.Process;

import com.bestv.ott.appstore.common.Constants;
import com.bestv.ott.appstore.common.DataOperate;
import com.bestv.ott.appstore.utils.AppLog;

public class ScoreThread extends Thread {

	private String mUserID;
	private Integer mAppId;
	private Float mScore;
	private DataOperate db;
	private Boolean isUpateSc = false;
	private Context mContext;	
	
	public ScoreThread(String mUserID, int mAppId, Float mScore,Context context){
		this.mUserID = mUserID;
		this.mAppId = mAppId;
		this.mScore = mScore;
		db = new DataOperate(context);
		this.mContext = context;
	}
	
	public void run(){
		Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
		isUpateSc = db.setScoreToService(mUserID, mAppId, mScore);
		if(isUpateSc!=null){
			AppLog.d("TAG","----------------------ScoreThread : Update the score----------------"+isUpateSc+"---");
			if(isUpateSc){//提交成功，发送广播，激活刷新
				Intent intent = new Intent();
				intent.setAction(Constants.INTENT_ACTION_UPDATESCORE_SUCCESS);
				mContext.sendBroadcast(intent);
			}
		}else{
			AppLog.d("TAG","-----------------------ScoreThread : Update the score----------------"+isUpateSc+"---");

		}
	}

}
