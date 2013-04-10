package com.bestv.ott.appstore.activity;

import java.util.List;

import com.bestv.ott.appstore.R;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.FocusFinder;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;
import android.widget.Toast;

import com.bestv.ott.appstore.animation.AnimUtils;
import com.bestv.ott.appstore.animation.MoveControl;
import com.bestv.ott.appstore.common.AppsBean;
import com.bestv.ott.appstore.common.Constants;
import com.bestv.ott.appstore.common.CustomProgressDialog;
import com.bestv.ott.appstore.common.NetErrorDialog;
import com.bestv.ott.appstore.common.TaskBean;
import com.bestv.ott.appstore.db.DBUtils;
import com.bestv.ott.appstore.parser.AppsParser;
import com.bestv.ott.appstore.pay.PayMng;
import com.bestv.ott.appstore.utils.AppLog;
import com.bestv.ott.appstore.utils.OrdersCaCheManager;
import com.bestv.ott.framework.BesTVServicesMgr;
import com.bestv.ott.framework.IBesTVServicesConnListener;
import com.bestv.ott.util.bean.UserOrder;
import com.bestv.ott.util.bean.UserProfile;

public class BaseActivity extends Activity{
	
	private final static String TAG="com.bestv.ott.appstore.activity.BaseActivity";
	
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
    public SharedPreferences sp;
    
    private static String PERIPHERALS_A = "1";
    private static String PERIPHERALS_B = "2";
    private static String PERIPHERALS_C = "3";
    private static String PERIPHERALS_D = "4";
    
    private PayMng mPay = null ;
    
    /* 外设对应的四张图片 */
    private static int[] iconBuf = {R.drawable.peripherals_a,
                                    R.drawable.peripherals_b,
                                    R.drawable.peripherals_c,
                                    R.drawable.peripherals_d};
    
    private static int[] viewID = {R.id.peripherals_a,
    							   R.id.peripherals_b,
    							   R.id.peripherals_c};
	
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
    	mPay = new PayMng(getApplicationContext());
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
	    if(sp.getString("groupID", "-1").equals("-1")){
	        mPay.authUser(new PayMng.CallbackActionListener() {
	            public void execute() {
	                getOrdersHistory();
	            }
	        });  
	    }else{
	        getOrdersHistory();
	    }
	}
	
	
	/* 获取历史订单 */
    private void getOrdersHistory(){
        if(OrdersCaCheManager.mUserOrders!=null){
            AppLog.d(TAG, "------------OrdersCaCheManager.mUserOrders!=null-------------");
            return;
        }
        mPay.loadAllUserOrders(new PayMng.CallbackActionListener() {
            public void execute() {
                AppLog.d(TAG, "------------getOrdersHistory  callback-------------");
                if(mPay.getUserOrders().size()>0){
                    AppLog.d(TAG, "------------mPay.getUserOrders().size()>0-------------");
                    OrdersCaCheManager.getInstance().addOrders(mPay.getUserOrders());
                    List a = mPay.getUserOrders();
                    for(int i=0;i<a.size();i++){
                        UserOrder b = (UserOrder) a.get(i);
                        Log.d(TAG, ">>>>>>>>>>>>>>>history>>>>>>> ProductCode = "+b.getProductCode());
                        Log.d(TAG, ">>>>>>>>>>>>>>>history>>>>>>> OrderTime = "+ b.getOrderTime());
                    }
                }
            }
        } , this);
    }
	
	/* 设置 外设类型/是否精品 图标显示 */
	public void setPropertyIcon(View view , AppsBean bean){
		if(bean.isBoutique()){
			ImageView boutique = (ImageView)view.findViewById(R.id.boutique);
			boutique.setVisibility(View.VISIBLE);
		}
		
		/*  */
		view.findViewById(viewID[0]).setVisibility(View.GONE);
		view.findViewById(viewID[1]).setVisibility(View.GONE);
		view.findViewById(viewID[2]).setVisibility(View.GONE);
		
		String str[] = AppsParser.setSplit(bean.getPeripherals());
		if(null==str){
			return;
		}
		int len = str.length;
		AppLog.d(TAG, "--------  str.length = "+len);
		int num = -1;
		for(int i=0;i<len;i++){
		    AppLog.d(TAG, "========str["+i+"]"+"="+str[i]);
			if(i==4){
				return;
			}
			if(num>2){ // 0  1  2  三图片
			    return;
			}
			if(str[i].equals(PERIPHERALS_A)){
				num++;
				ImageView view1 = (ImageView)view.findViewById(viewID[num]);
				view1.setImageResource(iconBuf[0]);
				view1.setVisibility(View.VISIBLE);
			}else if(str[i].equals(PERIPHERALS_B)){
				num++;
				ImageView view1 = (ImageView)view.findViewById(viewID[num]);
				view1.setImageResource(iconBuf[1]);
				view1.setVisibility(View.VISIBLE);
			}else if(str[i].equals(PERIPHERALS_C)){
				num++;
				ImageView view1 = (ImageView)view.findViewById(viewID[num]);
				view1.setImageResource(iconBuf[2]);
				view1.setVisibility(View.VISIBLE);
			}else if(str[i].equals(PERIPHERALS_D)){
				num++;
				ImageView view1 = (ImageView)view.findViewById(viewID[num]);
				view1.setImageResource(iconBuf[3]);
				view1.setVisibility(View.VISIBLE);
			}
			else{
			}
		}
		num = -1;
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
		isActive=true;
		if(control!=null&&mImageView!=null&&mImageView.getVisibility()==View.VISIBLE){
			control.startAnimation();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		isActive=false;
		AppLog.d(TAG, "------------control="+control+";mImageView="+mImageView);
		if(mImageView!=null){
			control.stopAnimation();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		stopNetErrorDialog();
		stopProgressDialog();
	}

	@Override
	protected void onDestroy() {
		if(mPay != null){
			mPay.destroy();
		}
		super.onDestroy();
		unregisterReceiver(mReciever);
	}
	
}
