package com.joysee.appstore.activity;

import java.util.List;
import com.joysee.appstore.R;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.FocusFinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.joysee.appstore.animation.AnimUtils;
import com.joysee.appstore.animation.MoveControl;
import com.joysee.appstore.common.AppsBean;
import com.joysee.appstore.common.Constants;
import com.joysee.appstore.common.CustomProgressDialog;
import com.joysee.appstore.common.NetErrorDialog;
import com.joysee.appstore.common.TaskBean;
import com.joysee.appstore.db.DBUtils;
import com.joysee.appstore.utils.AppLog;
import com.joysee.appstore.utils.Utils;

public class BaseActivity extends Activity{
	
	private final static String TAG="com.joysee.appstore.activity.BaseActivity";
	
	public FocusFinder mFocusF = FocusFinder.getInstance();
	public MoveControl control;
	public int viewGroupFocus;
	public  ViewGroup parent;
	public int downTimes=0;
	public int mMoveDirect;//移动方向
	public boolean isRunning=false;
	public ImageView mImageView;//移动背景图片
	public boolean isActive=false;
	
	private CustomProgressDialog progressDialog;
	private NetErrorDialog netErrorDialog;
	
	public void refreshAppsList(List<AppsBean> appList){};
	public void refreshMenusList(List<AppsBean> appList){};
	public void refreshDetailed(AppsBean appsBean){};
	public void refreshSearchList(List<AppsBean> appList){};
	
	
	public Runnable mRepeater = new Runnable() {
		public void run() {
			if(!isRunning){
				return;
			}
			View nextFocus=null;
			if (mMoveDirect == View.FOCUS_LEFT) {
				nextFocus = mFocusF.findNextFocus(parent, BaseActivity.this.getCurrentFocus(), View.FOCUS_LEFT);
			} else if (mMoveDirect == View.FOCUS_RIGHT) {
				nextFocus = mFocusF.findNextFocus(parent, BaseActivity.this.getCurrentFocus(), View.FOCUS_RIGHT);
			} else if (mMoveDirect == View.FOCUS_DOWN) {
				nextFocus = mFocusF.findNextFocus(parent, BaseActivity.this.getCurrentFocus(), View.FOCUS_DOWN);
			} else if (mMoveDirect == View.FOCUS_UP) {
				nextFocus = mFocusF.findNextFocus(parent, BaseActivity.this.getCurrentFocus(), View.FOCUS_UP);
			}
			if(nextFocus!=null){
				nextFocus.requestFocus();
			}
			if (isRunning) {
				parent.postDelayed(this, 100);
			}
		}
	};
	
   /**
     * 取setting背景图，用于设置成这个activity的背景
     * @return
     */
    public Drawable getThemePaper(){
        String url = Settings.System.getString(this.getContentResolver(), "settings.theme.url");
        if(url!=null && url.length()>0){
                Bitmap bitmap = BitmapFactory.decodeFile(url);
                Drawable drawable = new BitmapDrawable(bitmap);
                return drawable;
        }
        return null;
    }
    
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
	
	public void requestFocusFirst(View view){
		if(view==null) 
			return;
		MarginLayoutParams paramImage=(MarginLayoutParams)mImageView.getLayoutParams();
		if(paramImage.leftMargin==0){//当数据没有加载完,按返回,加载进度圆圈消失
			parent.setDescendantFocusability(viewGroupFocus);
			view.setFocusable(true);
			view.setFocusableInTouchMode(true);
			view.setAlpha(AnimUtils.VIEW_ALPHA_FLAG);
			view.requestFocus();
		}
	}
	
	public void startNetErrorDialog(){
		stopProgressDialog();
		if (netErrorDialog==null){
			//netErrorDialog=NetErrorDialog.createDialog(BaseActivity.this);
//			Toast.makeText(getApplicationContext(), R.string.network_error,1).show();
			Utils.showTipToast(Gravity.CENTER,BaseActivity.this, BaseActivity.this.getString(R.string.network_error));
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
		/*LayoutInflater inflater = getLayoutInflater();
		View view=inflater.inflate(R.layout.down_error_toast_layout, null);
		((TextView)view.findViewById(R.id.tv_error)).setText(appName);
		Toast toast = new Toast(getApplicationContext());
		toast.setView(view);
		toast.show();*/
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
    	IntentFilter filter = new IntentFilter();
	    filter.addAction(Constants.INTENT_DOWNLOAD_ERROR);
	    registerReceiver(mReciever, filter);
	}
	
	
	
	@SuppressWarnings("unused")
	private static final int MAX_TRY = 3 ;
	
	public void onBackPressed() {
		super.onBackPressed();
		AppLog.d(TAG, "===========onBackPressed============");
		if(mImageView!=null){
			control.stopAnimation();
		}
	}

	protected void onStart() {
		super.onStart();
	}

	protected void onRestart() {
		super.onRestart();
	}

	protected void onResume() {
		super.onResume();
		isActive=true;
		if(control!=null&&mImageView!=null&&mImageView.getVisibility()==View.VISIBLE){
			control.startAnimation();
		}
	}

	protected void onPause() {
		super.onPause();
		isActive=false;
		AppLog.d(TAG, "------------control="+control+";mImageView="+mImageView);
		if(mImageView!=null){
			control.stopAnimation();
		}
	}

	protected void onStop() {
		super.onStop();
		stopNetErrorDialog();
//		stopProgressDialog();
	}

	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReciever);
	}
	
	/* low momery quit */
	public void onLowMemory() {
		super.onLowMemory();
		Utils.showTipToast(Gravity.CENTER, this, this.getString(R.string.low_momery));
		finish();
	}
	
}
