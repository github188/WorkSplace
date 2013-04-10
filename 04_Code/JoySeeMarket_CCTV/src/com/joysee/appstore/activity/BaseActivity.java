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
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.FocusFinder;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager.LayoutParams;
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
import com.joysee.appstore.service.DownloadService;
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
		/** 设全屏 */
		progressDialog.getWindow().setLayout(1280, 720);
		WindowManager.LayoutParams lp = progressDialog.getWindow().getAttributes();
		lp.alpha = 0.5f;
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
	
	public boolean isError = false;
	public void startNetErrorDialog(){
		stopProgressDialog();
		if(isError){ //防止二次弹框（分类数据和菜单数据请求失败都会发起弹框）
			return;
		}
		Utils.showTipToast(Gravity.CENTER,BaseActivity.this, BaseActivity.this.getString(R.string.network_error));
		isError = true;
	}
	
	public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	AppLog.d(TAG, "---------oncreate");
    	IntentFilter filter = new IntentFilter();
	    filter.addAction(Constants.INTENT_DOWNLOAD_ERROR);
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
		Constants.activitys++;
		Constants.update=false;
		Log.d(TAG, "-----onResume------activitys.size="+Constants.activitys);
	}

	protected void onPause() {
		super.onPause();
		isActive=false;
		AppLog.d(TAG, "------------control="+control+";mImageView="+mImageView);
		if(mImageView!=null){
			control.stopAnimation();
		}
		Constants.activitys--;
		Log.d(TAG, "-----onPause------activitys.size="+Constants.activitys);
		if(Constants.activitys==0){
            sendUpdate();
		}
	}

	protected void onStop() {
		super.onStop();
	}
	
	public void sendUpdate(){
	    new Handler().postDelayed(runnable, 3000);
	}
	
	Runnable runnable = new Runnable(){
        @Override
        public void run() {
            Log.d(TAG, "-----runnable------activitys.size="+Constants.activitys);
            if(Constants.activitys==0){
                Constants.update=true;
                DBUtils dbu=new DBUtils(BaseActivity.this);
                TaskBean updateBean=dbu.queryTaskByPkgName(BaseActivity.this.getPackageName());
                if(updateBean!=null&&updateBean.getDownloadSize()>=updateBean.getSumSize()){
                    AppLog.d(TAG, "------updateBean pagk="+updateBean.getPkgName());
                    if(dbu.needStartService("com.joysee.appstore.service.DownloadService")){
                        Intent intent = new Intent();
                        intent.setAction(Constants.INTENT_DOWNLOAD_COMPLETED);
                        intent.putExtra("task",updateBean);
                        BaseActivity.this.sendBroadcast(intent);
                    }else{
                        AppLog.d(TAG, "-------INTENT_ACTION_RESTART_SERVICES---------");
                        Intent intentSer = new Intent(BaseActivity.this,DownloadService.class);
                        BaseActivity.this.startService(intentSer);
                        Intent intent = new Intent();
                        intent.setAction(Constants.INTENT_DOWNLOAD_COMPLETED);
                        intent.putExtra("task",updateBean);
                        BaseActivity.this.sendBroadcast(intent);
                    }
                }
                Constants.update=true;
                Log.d(TAG, "--------update="+Constants.update);
            }
        }
	};

	protected void onDestroy() {
		super.onDestroy();
		isError = false;
	}
	
	/* low momery quit */
	public void onLowMemory() {
		super.onLowMemory();
		Utils.showTipToast(Gravity.CENTER, this, this.getString(R.string.low_momery));
		finish();
	}
	
}
