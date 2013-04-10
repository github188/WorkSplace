package com.bestv.ott.appstore.activity;

import java.util.LinkedList;
import java.util.List;

import com.bestv.ott.appstore.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;
import android.view.View;
import android.widget.TextView;

import com.bestv.ott.appstore.common.AppsBean;
import com.bestv.ott.appstore.common.Constants;
import com.bestv.ott.appstore.common.CustomProgressDialog;
import com.bestv.ott.appstore.common.NetErrorDialog;
import com.bestv.ott.appstore.common.TaskBean;
import com.bestv.ott.appstore.db.DBUtils;
import com.bestv.ott.appstore.service.DownloadService;
import com.bestv.ott.appstore.utils.AppLog;
import com.bestv.ott.appstore.utils.AppStoreConfig;
import com.bestv.ott.BesTVOttServices.BesTVOttMng;
import com.bestv.ott.BesTVOttServices.BesTVOttServices;
import com.bestv.ott.BesTVOttServices.IBesTVOttConnListener;
import com.bestv.ott.BesTVOttServices.UserService.IUserService;
import com.bestv.ott.util.bean.UserProfile;

public class BaseActivity extends Activity{
	
	private final static String TAG="com.bestv.ott.appstore.activity.BaseActivity";
	
	private CustomProgressDialog progressDialog;
	private NetErrorDialog netErrorDialog;
	public BesTVOttMng mng = null; 
    public IUserService mIUserService;
    public SharedPreferences sp;
	
	public void refreshAppsList(List<AppsBean> appList){};
	
	public void refreshMenusList(List<AppsBean> appList){};
	
	public void refreshDetailed(AppsBean appsBean){};
	
	public void startProgressDialog(){
		if (progressDialog == null){
			progressDialog = CustomProgressDialog.createDialog(BaseActivity.this);
		}
    	progressDialog.show();
	}
	
	public void stopProgressDialog(){
		if (progressDialog != null){
			progressDialog.dismiss();
			progressDialog = null;
		}
	}
	
	public void startNetErrorDialog(){
		stopProgressDialog();
		if (netErrorDialog==null){
			//netErrorDialog=NetErrorDialog.createDialog(BaseActivity.this);
			Toast.makeText(getApplicationContext(), R.string.network_error,1).show();
		}
		//netErrorDialog.show();
	}
	
	public void stopNetErrorDialog(){
		if (netErrorDialog != null){
			netErrorDialog.dismiss();
			netErrorDialog = null;
		}
	}
	
	public void downErrorToast(String appName){
//		LayoutInflater inflater = getLayoutInflater();
//		View view=inflater.inflate(R.layout.down_error_toast_layout, null);
//		((TextView)view.findViewById(R.id.tv_error)).setText(appName);
//		Toast toast = new Toast(getApplicationContext());
//		toast.setView(view);
//		toast.show();
	}
	
	private BroadcastReceiver mReciever = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(Constants.INTENT_DOWNLOAD_ERROR)){
				AppLog.d(TAG, "-------------------"+Constants.INTENT_DOWNLOAD_ERROR);
				DBUtils tDBUtils = new DBUtils(BaseActivity.this);
				int taskId=intent.getIntExtra("taskid", -1);
				if(taskId<0){
					return;
				}
				TaskBean tb=tDBUtils.queryTaskById(taskId) ;
				downErrorToast(tb.getAppName()+BaseActivity.this.getString(R.string.download_error));
			}

		}

	};
	
	public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	AppLog.d(TAG, "---------oncreate");
    	sp = getSharedPreferences("user_information", Context.MODE_PRIVATE);
    	getUserAndGroudID();;//每一个Activity创建的时候，重新设置用户信息
    	IntentFilter filter = new IntentFilter();
	    filter.addAction(Constants.INTENT_DOWNLOAD_ERROR);
	    registerReceiver(mReciever, filter);
	    
	}
	
	private static final int MAX_TRY = 3 ;
	
	/**
	 * 获取用户ID 组ID
	 */
	private void getUserAndGroudID(){
		mng = BesTVOttServices.createOttConManage(BaseActivity.this);
		mng.connect(new IBesTVOttConnListener() {
			
			public void onBesTVOttServiceConnected() {//中间件 is connection
				AppLog.d(TAG, "--------------------BesTV service is connected--------------------");
				IUserService us = BesTVOttServices.createUserService();
				
				int count = 0 ;
				boolean flag = false ;
				do{
					count ++ ; 
					try {
						flag = us.isLogin();
						if(flag){
							UserProfile userProfile = us.getProfile();
							if (null != userProfile && null != userProfile.getUserID()) {
								Editor editor = sp.edit();
								editor.putString("userID", userProfile.getUserID()).commit();
								editor.putString("groupID",  userProfile.getUserGroup()).commit();
								AppLog.d(TAG, "----------------userProfile.getUserGroup() := "+ userProfile.getUserGroup()+"  -----------------");
								AppLog.d(TAG, "----------------userProfile.getUserID() := "+userProfile.getUserID()+"  -------------");
							}
						}else{
							Thread.sleep(100);
						}
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					AppLog.d(TAG, "-----------count="+count+";flag="+flag);
				}while(count <= MAX_TRY && !flag) ;
				
				if(!flag){
					//TODO return and close appstore 
					finish();
					return ;
				}
				
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		stopNetErrorDialog();
		stopProgressDialog();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReciever);
	}
	
}
