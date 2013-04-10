package com.bestv.ott.appstore.activity;

import java.util.List;

import com.bestv.ott.appstore.common.Constants;
import com.bestv.ott.appstore.common.DataOperate;
import com.bestv.ott.appstore.thread.ScoreThread;
import com.bestv.ott.appstore.utils.AppLog;
import com.bestv.ott.appstore.utils.Utils;
import com.bestv.ott.appstore.R;
import com.bestv.ott.epay.client.IBesTVWalletServiceCallBackListener;
import com.bestv.ott.epay.pay.BestvWalletPay;
import com.bestv.ott.epay.service.BesTVpayResult;
import com.bestv.ott.framework.BesTVServicesMgr;
import com.bestv.ott.framework.IBesTVServicesConnListener;
import com.bestv.ott.util.bean.ResultParam;
import com.bestv.ott.util.bean.SubsProduct;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class AppChargeSwitchActivity extends Activity {
	public static final String TAG_PACKAGE_NAME ="switch_package" ;
	public static final String TAG_CHARGE_MONEY ="charge_money" ;
	private static final String T_CODE = "code";
	private static final String T_MONEY = "money"; 
	
	public static final int MSG_CLOSE = 1; 
	
	//TODO by penghui
    private TextView mTextView1; 
    private String title ;
    private Drawable titleIcon ;
    private View.OnClickListener okClickListener ;
    private View.OnClickListener cancelClickListener ;
    private ImageButton mOkButton;
    private ImageButton mCloseButton;
	private Intent intent;
	private int mAppId;
	private int mAmount;
	private String mUserID;
     
	private IBesTVWalletServiceCallBackListener onCallBackListener ;
	private BestvWalletPay mBestvWalletPay = null ;
	private BesTVpayResult mBesTVpayResult ;
	private String packageName;
	private String orderAmt;
	private boolean bResult ;
	
	
	public void setTitle(String title) {
		this.title = title;
	}

	public void setTitleIcon(Drawable titleIcon) {
		this.titleIcon = titleIcon;
	}
	
	public void setOnCallBackListener(IBesTVWalletServiceCallBackListener callBack){
		this.onCallBackListener=callBack;
	}

	public void setOkClickListener(View.OnClickListener okClickListener) {
		this.okClickListener = okClickListener;
	}

	public void setCancelClickListener(View.OnClickListener cancelClickListener) {
		this.cancelClickListener = cancelClickListener;
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.app_charge_switch);
		setupViews();
	}
	
	/**
	 * 初始化
	 */
	public void setupViews() { 
		//每次都让评价 
		mTextView1 = (TextView) findViewById(R.id.text_1);  
		Intent intent = getIntent();
		if(intent != null){
			packageName = intent.getStringExtra(TAG_PACKAGE_NAME);
			orderAmt = intent.getStringExtra(TAG_CHARGE_MONEY);
		}
		mBestvWalletPay = new com.bestv.ott.epay.pay.BestvWalletPay();
		onCallBackListener = new IBesTVWalletServiceCallBackListener() {
			@Override
			public void getResult(BesTVpayResult res) {
				// TODO Auto-generated method stub
				mBesTVpayResult = res;
				/* 处理代码起另外一个线程，或者ui线程（涉及ui处理）来运行 */
				try {
					Log.d("=================getResult============================", res.getResult());
					if (res.getResult() == null|| res.getResult().equals("")) {
						//Toast.makeText(BestvmerchantActivity.this, "操作失败！未知错误！！！！",Toast.LENGTH_SHORT).show();
						mTextView1.setText(R.string.charge_fail);
					} else if (res.getResult().equals("0")) {
						if (res.getTransState() == null|| res.getTransState().equals("")) {
							//Toast.makeText(BestvmerchantActivity.this,"交易成功,交易金额:"+ new BigDecimal(mResponse.getOrderAmt()).divide(new BigDecimal(100)),Toast.LENGTH_SHORT).show();
							mTextView1.setText(R.string.charge_success);
						} else if (res.getTransState().equals("0")) {
							//Toast.makeText(BestvmerchantActivity.this,"交易成功,交易金额:"+ new BigDecimal(mResponse.getOrderAmt()).divide(new BigDecimal(100)),Toast.LENGTH_SHORT).show();
							mTextView1.setText(R.string.charge_success);
						} else {
							//Toast.makeText(BestvmerchantActivity.this,"交易失败!失败原因:" + mResponse.getTransMsg(),	Toast.LENGTH_SHORT).show();
							mTextView1.setText(R.string.charge_fail);
						}
					} else {
						//Toast.makeText(BestvmerchantActivity.this,"操作失败!失败原因:" + mResponse.getErrorMsg(),Toast.LENGTH_SHORT).show();
						mTextView1.setText(R.string.charge_fail);
					}
				} catch (Exception e) {
				}finally{
					mainHandler.sendEmptyMessageDelayed(MSG_CLOSE, 3000);
				}
			}
		}; 
		
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Log.d("-------------------------------------", "3333333333333333333");
				bResult = mBestvWalletPay.rechargePay(packageName, onCallBackListener, orderAmt, AppChargeSwitchActivity.this);
				Log.d("BesTVServicesMgr"," chargeswitch >>>>>>>>>>>>>>>>>>>>" + bResult + "/" + packageName + "/" + orderAmt +  " ///" + mBestvWalletPay );
				mainHandler.sendEmptyMessageDelayed(MSG_CLOSE, 3000);
			}
		}, 3000);
		
	}
	
	Handler mainHandler=new Handler(){
		public void handleMessage(Message msg) {
			switch(msg.what){
			case MSG_CLOSE:
				if(mBestvWalletPay!=null){
					mBestvWalletPay.unregisterCallback("com.bestv.epay.view", onCallBackListener);
					mBestvWalletPay.disconnectService();
				}
				//TODO should open order page
				AppChargeSwitchActivity.this.finish();
				break;
			}
		}
	};
    
	protected void onPause() {
		super.onPause();
		finish();
	}
	protected void onDestroy() {
	    super.onDestroy();
	}
	
	public static void openActivity(Context context , String packageName ,double price){
		Intent intent=new Intent();
		intent.setClass(context, AppChargeSwitchActivity.class);
		intent.putExtra(TAG_PACKAGE_NAME, packageName );
		intent.putExtra(TAG_CHARGE_MONEY, String.valueOf(price));
		context.startActivity(intent);
	}
	
}
