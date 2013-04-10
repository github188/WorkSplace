package com.bestv.ott.appstore.activity;

import java.util.List;

import com.bestv.ott.appstore.common.Constants;
import com.bestv.ott.appstore.common.DataOperate;
import com.bestv.ott.appstore.pay.PayMng;
import com.bestv.ott.appstore.thread.ScoreThread;
import com.bestv.ott.appstore.utils.AppLog;
import com.bestv.ott.appstore.utils.CaCheManager;
import com.bestv.ott.appstore.utils.Utils;
import com.bestv.ott.appstore.R;
import com.bestv.ott.framework.BesTVServicesMgr;
import com.bestv.ott.framework.IBesTVServicesConnListener;
import com.bestv.ott.framework.services.OrderService;
import com.bestv.ott.framework.services.UserService;
import com.bestv.ott.util.bean.ResultParam;
import com.bestv.ott.util.bean.SubsParam;
import com.bestv.ott.util.bean.SubsProduct;
import com.bestv.ott.util.bean.UserOrder; 

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 透明窗体评价窗口
 * 
 * @author benz
 */
public class AppChargeDemoActivity extends Activity {
	private static final String TAG = "AppChargeDemoActivity";
	private OrderService mOrderService ;
	
	private List<UserOrder> mUserOrders ;
	
	private BesTVServicesMgr mBesTVServicesMgr ;  
	
	private UserService mUserService=null; 
	
	private PayMng mPayMng = null ;
	
	private String PRODUCT_CODE ="BTOP_APP_PRO_003" ;
	private String SERVICE_CODE = "BTOP_APP_003" ;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mPayMng = new PayMng(AppChargeDemoActivity.this);
		
		setContentView(R.layout.app_charge_demo);
		setupViews();
	}
	
	/**
	 * 初始化
	 */
	public void setupViews() {
		//每次都让评价
		//mRatingBar.setOnRatingBarChangeListener(new RatingBarListener());
		Button   button = (Button) findViewById(R.id.button0); 
		button.setText("connect");
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) { 
//				auth();
				auth2();
			}
		});
		button = (Button) findViewById(R.id.button1); 
		button.setText("confirm?");
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) { 
//				Intent intent=new Intent();
//				intent.setClass(AppChargeDemoActivity.this, AppChargeConfirmActivity.class); 
//				startActivity(intent);
			    checkMng();
//				auth();
			}
		});
		button = (Button) findViewById(R.id.button2); 
		button.setText("message");
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {  
				Intent intent=new Intent();
				intent.setClass(AppChargeDemoActivity.this, AppChargeMsgActivity.class); 
				intent.putExtra("title", "连接信息");
				intent.putExtra("content", "连接信息，XXXXXXXXXXXXXXXXXXXXXXX");
				intent.putExtra("flag", 0 ); 
				startActivity(intent);
			}
		});
		button = (Button) findViewById(R.id.button3); 
		button.setText("moeny");
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {  
				Intent intent=new Intent();
				intent.setClass(AppChargeDemoActivity.this, AppChargeSwitchActivity.class);
				intent.putExtra(AppChargeSwitchActivity.TAG_PACKAGE_NAME, "com.chaozh.iReaderFree");
				intent.putExtra(AppChargeSwitchActivity.TAG_CHARGE_MONEY, "2500");
				
				startActivity(intent);
				
			}
		});
		

		button = (Button) findViewById(R.id.button4); 
		button.setText("gift packs ");
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {  
				GiftsPackActivity.openGiftsOrderDialog(AppChargeDemoActivity.this, "BTOP_APP_PRO_TASKM","12312") ; 
			}
		});

		button = (Button) findViewById(R.id.button5); 
		button.setText("order ");
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d("DEMO","$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ order");
				order();
			}
		});
		
		button = (Button) findViewById(R.id.button6); 
		button.setText("get apkservice ");
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {  
				mPayMng.apk(new PayMng.CallbackActionListener() { 
					@Override
					public void execute() { 
						Log.d(TAG,"apk after >>>>>>>>>>>>>>>>>>>>>>" + mPayMng.getResult() ) ; 
					}
				});
			}
		});
		button = (Button) findViewById(R.id.button7); 
		button.setText("order list ");
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {  
				mPayMng.loadAllUserOrders(new PayMng.CallbackActionListener() {
					
					@Override
					public void execute() {
						// TODO Auto-generated method stub
						mUserOrders = mPayMng.getUserOrders();
						if(mUserOrders != null){
							for(UserOrder u:mUserOrders){
								Log.d(TAG,"dddddddddddddddd://" + u.getContentName() + "/" + u.getPrice());
							}
						}
					}
				}, AppChargeDemoActivity.this) ; 
			}
		});
	}
	

	private void doOrderService() {
		// TODO Auto-generated method stub
		Log.d("PayMng"," doOrderService >>>>>>>>>>>>>>>>>>>>" + mOrderService );
		if(mOrderService == null ){
			Intent intent=new Intent();
			intent.setClass(AppChargeDemoActivity.this, AppChargeMsgActivity.class); 
			intent.putExtra("title", "连接失败");
			intent.putExtra("content", "连接失败，请检查网络是否正常或者相关服务是否启动");
			intent.putExtra("flag", 1);
			startActivity(intent);
		}else{ 
			SubsParam subsParam = new SubsParam();
			subsParam.setProductCode("BTOP_APP_PRO_TASKM");
			
			subsParam.setAuthType(PayMng.AUTHTYPE_PRODUCT);
			subsParam.setBizType(PayMng.BIZTYPE_APP) ;
			ResultParam r = mOrderService.auth(subsParam) ;
			
			Log.d(TAG,r.getReturnCode() + ">>>>>>" + r.getReturnDec() + ">>\r\n>>" + r.getOrderProduct());
			
				Intent intent=new Intent();
				intent.setClass(AppChargeDemoActivity.this, AppChargeMsgActivity.class); 
				intent.putExtra("title", "连接成功");
				intent.putExtra("content", "连接成功，恭喜你，交易完成!");
				intent.putExtra("flag", 0);
				startActivity(intent);  
		}
	}
	
	@Override
	protected void onDestroy() {
		if(mPayMng != null ){
			mPayMng.destroy();
		}
		super.onDestroy();
	}

	public void auth(){
		SubsParam subsParam = new SubsParam();
		subsParam.setProductCode(PRODUCT_CODE);
		subsParam.setServiceCodes(SERVICE_CODE );
		subsParam.setAuthType(PayMng.AUTHTYPE_PRODUCT);
		subsParam.setBizType(PayMng.BIZTYPE_APP) ; 
		boolean b = mPayMng.authOrder(new PayMng.CallbackActionListener() { 
			@Override
			public void execute() { 
				Log.d(TAG,"aaaaaaaaaaaaaaaaaaaaaa after auth!");
				doAfterAuth();
			}
		}, subsParam); 
		
		if(b){
			openConfirmOrderDialog();
		}else{
			openOrderMsgDialog();
		}
	}
	
	
	protected void doAfterAuth() {
		// TODO Auto-generated method stub
		Log.d(TAG,"doAfterAuth>>>>>>>>>>>>>>>>:" + mPayMng.getResult() );
		if(mPayMng.getResult()!= null){
			ResultParam mResultParam = (ResultParam)mPayMng.getResult() ;		
			List<SubsProduct> list = mResultParam.getOrderProduct();
			Log.d(TAG,"ssssssssssssssssss:" + list.size());
			for(SubsProduct p : list){
				Log.d(TAG,"#############" + p.getCode() + "//" + p.getDescription() + "/" + p.getName() + "/" + p.getPrice()) ;
			}
		}

	}
	
	private void openConfirmOrderDialog(){
		Intent intent=new Intent();
		intent.setClass(AppChargeDemoActivity.this, AppChargeConfirmActivity.class); 
		startActivity(intent);
	}

	private void openOrderMsgDialog(){
		Intent intent=new Intent();
		intent.setClass(AppChargeDemoActivity.this, AppChargeMsgActivity.class); 
		startActivity(intent);
	}

	private BesTVServicesMgr mMgr=null;
	private UserService user=null;
	
	
	public void auth2(){ 
		Log.d(TAG,">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> auth2"  );
		mMgr = BesTVServicesMgr.getInstance(this);
		boolean b = mMgr.connect(new IBesTVServicesConnListener(){
			public void onBesTVServicesConnected(BesTVServicesMgr client){
				test();
			}
		});
		
		Log.d(TAG,">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> auth2/" + b  );
	}
	
	
	public void test(){
		Log.d(TAG,">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> test"  );
		new Thread(new Runnable() {
			public void run() {
						user=mMgr.getUserService();
						Log.d(TAG, "####################### userid=" + user.getUserID() + ", usergroup=" + user.getUserGroup() + ", usertoken=" + user.getUserToken());
						OrderService oOrderService = mMgr.getOrderService();
						if(oOrderService != null ){
							SubsParam subsParam = new SubsParam();
							subsParam.setProductCode(PRODUCT_CODE);
							subsParam.setServiceCodes(SERVICE_CODE);
							subsParam.setAuthType(1);
							subsParam.setBizType(2) ;  
							ResultParam r = oOrderService.auth(subsParam);
							Log.d(TAG,">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + r.getReturnCode() + "/" + r.getReturnDec() );
						}
						
						Log.d(TAG,">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + oOrderService );
				
			}
		}).start();
	}
	
	public void order(){
		SubsParam subsParam = new SubsParam();
		subsParam.setProductCode(PRODUCT_CODE);
		subsParam.setServiceCodes(SERVICE_CODE );
		
		mPayMng.order(new PayMng.CallbackActionListener() {
			
			@Override
			public void execute() { 
				Log.d(TAG,"aaaaaaaaaaaaaaaaaaaaaa after order!");
			}
		},subsParam); 
	}
	
	public void checkMng(){
	    SubsParam subsParam = new SubsParam();
	    subsParam.setServiceCodes("BTOP_APP_003_1");
        subsParam.setProductCode("BTOP_APP_PRO_003_1");
        subsParam.setAuthType(PayMng.AUTHTYPE_PRODUCT);
        subsParam.setBizType(PayMng.BIZTYPE_APP) ;
        
        final Boolean res=mPayMng.authOrder(new PayMng.CallbackActionListener() { 
            @Override
            public void execute() {
                if(mPayMng.getResult() != null ){
                    ResultParam mResult = (ResultParam)mPayMng.getResult() ;
                    int retCode = mResult.getReturnCode() ;
                    Log.d(TAG, "retCode = "+retCode);
                    if(retCode == PayMng.REQ_AUTH_MSG_EXCEPTION || retCode == PayMng.REQ_NETWORK_ERROR){
                        AppChargeMsgActivity.openActivity(AppChargeDemoActivity.this,AppChargeDemoActivity.this.getString(R.string.order_failed),mResult.getReturnDec() , PayMng.REQ_AUTH_MSG_EXCEPTION);
                    }else if(retCode == PayMng.REQ_MONEY_NOT_ENOUGH){ 
                        AppLog.d(TAG, "========retCode == PayMng.REQ_MONEY_NOT_ENOUGH");
                    }else if(retCode == PayMng.REQ_AUTH_FAILED){
                        AppLog.d(TAG, "========retCode == PayMng.REQ_AUTH_FAILED");
                    }else if(retCode == PayMng.REQ_AUTH_SUCCESS){
                        AppLog.d(TAG, "========retCode == PayMng.REQ_AUTH_SUCCESS");
                    }
                    
                }
            }
        },subsParam); 
	}
	
	
	
	
}
