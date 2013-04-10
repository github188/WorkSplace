package com.bestv.ott.appstore.activity;

import com.bestv.ott.appstore.common.Constants;
import com.bestv.ott.appstore.common.CustomProgressDialog;
import com.bestv.ott.appstore.common.DataOperate;
import com.bestv.ott.appstore.pay.PayMng;
import com.bestv.ott.appstore.thread.ScoreThread;
import com.bestv.ott.appstore.utils.AppLog;
import com.bestv.ott.appstore.utils.FileUtils;
import com.bestv.ott.appstore.utils.StringUtils;
import com.bestv.ott.appstore.utils.Utils;
import com.bestv.ott.appstore.R;
import com.bestv.ott.epay.client.IBesTVWalletServiceCallBackListener;
import com.bestv.ott.epay.pay.BestvWalletPay;
import com.bestv.ott.epay.service.BesTVpayResult;
import com.bestv.ott.util.bean.ResultParam;
import com.bestv.ott.util.bean.SubsParam;
import com.bestv.ott.util.bean.SubsProduct;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AppChargeConfirmActivity extends Activity { 
	protected static final String TAG = "AppChargeConfirmActivity";

	public static final int TYPE_ORDER_SUCCESS = 0 ;
	public static final int TYPE_ORDER_FAILED = 1 ; 
	
	public static final String T_CODE = "code";
	public static final String T_SERVICE_CODE = "s_code";
	public static final String T_NAME = "name";
	public static final String T_PRICE = "price";
	public static final String T_VALID = "time";
	public static final String T_APPID = "app_id";
	public static final String T_ORDER_TYPE = "order_type";
	public static final String T_ORDER_CYCLE = "order_cycle";
	public static final String T_SUBSPRODUCT="subsproduct";
	public static final String T_PKGNAME="pkgname";
	
	public static final int MSG_ORDER_SUCCESS = 200;

    protected static final int MSG_CLOSE = 201;
    protected static final int MSG_TO_MONERY=202;
    protected static final int MSG_NOT_MONERY = 203;
    protected static final int MSG_RECHARGE_SUCCESS = 204;
    protected static final int MSG_RECHARGE_FAIL = 205;
    protected static final int MSG_RECHARGE_ERROR = 206;
	
    private TextView mTextView1;
	private TextView mTextView2;
	private TextView mTextView3;
	private TextView mTextView4;
	private TextView mTextView5;
	private TextView mTextView6;
	private TextView mTextTitle;
	private ImageView mImageIcon;
    private String title ;
    private Drawable titleIcon ;
    private View.OnClickListener okClickListener ;
    private View.OnClickListener cancelClickListener ;
    private Button mOkButton;
    private ImageButton mCloseButton;
	private Intent intent;
	private int mAppId;
	private int mAmount;
	private String mUserID;
    
	private String code ;
	private String scode;
	private String name ;
	private float price ;
	private String time ;
	private String pkgName;
	private CustomProgressDialog progressDialog;
	private static Bitmap mIcon;
	private Toast myToast;
	private View toastView;
	private TextView toastText;
	
	private PayMng mPayMng ;
	private boolean isCheck = false;
	
	public void setTitle(String title) {
		this.title = title;
	}

	public void setTitleIcon(Drawable titleIcon) {
		this.titleIcon = titleIcon;
	}

	public void setOkClickListener(View.OnClickListener okClickListener) {
		this.okClickListener = okClickListener;
	}

	public void setCancelClickListener(View.OnClickListener cancelClickListener) {
		this.cancelClickListener = cancelClickListener;
	}
	
	
	public Handler confirHandler=new Handler(){

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
            case MSG_ORDER_SUCCESS:
                isCheck = false;
                stopProgressDialog();
                break;
            case MSG_NOT_MONERY:
                
                toastText.setText( AppChargeConfirmActivity.this.getString(R.string.monery_not_enough));
                myToast.show();
                
//                Toast.makeText(AppChargeConfirmActivity.this,
//                        AppChargeConfirmActivity.this.getString(R.string.monery_not_enough),Toast.LENGTH_LONG).show();
                confirHandler.sendEmptyMessageDelayed(MSG_TO_MONERY, 2000);
                break;
                /* 跳转到充值 */
            case MSG_TO_MONERY:
                if(mBestvWalletPay!=null){
                    mBestvWalletPay.rechargePay(pkgName, onCallBackListener,  String.valueOf(price), AppChargeConfirmActivity.this);
                }
                break;
            case MSG_CLOSE:
                //充值回调成功
                if(mBestvWalletPay!=null){
                    mBestvWalletPay.unregisterCallback("com.bestv.epay.view", onCallBackListener);
                    mBestvWalletPay.disconnectService();
                }
                break;
            case MSG_RECHARGE_SUCCESS:
                
                toastText.setText(AppChargeConfirmActivity.this.getString(R.string.charge_success));
                myToast.show();
                
//                Toast.makeText(AppChargeConfirmActivity.this,
//                        AppChargeConfirmActivity.this.getString(R.string.charge_success),Toast.LENGTH_LONG).show();
                break;
            case MSG_RECHARGE_FAIL:
                
                toastText.setText(AppChargeConfirmActivity.this.getString(R.string.charge_fail));
                myToast.show();
                
//                Toast.makeText(AppChargeConfirmActivity.this,
//                        AppChargeConfirmActivity.this.getString(R.string.charge_fail),Toast.LENGTH_LONG).show();
                break;
            case MSG_RECHARGE_ERROR:
                
                toastText.setText(AppChargeConfirmActivity.this.getString(R.string.charge_fail));
                myToast.show();
                
//                Toast.makeText(AppChargeConfirmActivity.this,
//                        AppChargeConfirmActivity.this.getString(R.string.charge_fail),Toast.LENGTH_SHORT).show();
                break;
            }
        }
	};
	
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
	    if(keyCode == KeyEvent.KEYCODE_BACK){
	        if(confirHandler!=null){
	            if(confirHandler.hasMessages(MSG_TO_MONERY)){
	                confirHandler.removeMessages(MSG_TO_MONERY);
	                return true;
	            }
	        }
	        finish();
	    }
	    return false;
	};
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mPayMng = new PayMng(this);
		setContentView(R.layout.app_charge_confirm);
		setupViews();
		processIntent();
	}
	
	public void startProgressDialog(){
		if (progressDialog == null){
			progressDialog = CustomProgressDialog.createDialog(AppChargeConfirmActivity.this);
		}
    	progressDialog.show();
	}
	
	public void stopProgressDialog(){
		if (progressDialog != null){
			progressDialog.dismiss();
			progressDialog = null;
		}
	}
	
	@Override
	protected void onDestroy() {
		if(mPayMng != null){
			mPayMng.destroy();
		}
		if(mBestvWalletPay!=null){
            mBestvWalletPay.unregisterCallback("com.bestv.epay.view", onCallBackListener);
        }
		super.onDestroy();
	}

	private void processIntent() {
		Intent intent = getIntent();
		if(intent != null ){
			code = intent.getStringExtra(T_CODE) ;
			Log.d(TAG,"processIntent PPPPPPPPPPPP################# code:" + code );
			scode = intent.getStringExtra(T_SERVICE_CODE);
			name = intent.getStringExtra(T_NAME);
			price = intent.getFloatExtra(T_PRICE,(float)0);
			mAppId=intent.getIntExtra(T_APPID, -1);
			pkgName=intent.getStringExtra(T_PKGNAME);
			
			mTextView2.setText(name);
			mTextView4.setText(this.getString(R.string.app_order_price_val,StringUtils.formatMoney(price)));
			mImageIcon.setImageBitmap(mIcon);
			
			int order_type=intent.getIntExtra(T_ORDER_TYPE, -1);
			int order_cycle=intent.getIntExtra(T_ORDER_CYCLE, -1);
			Log.d(TAG, "type="+order_type+";cycle="+order_cycle);
			if(order_type==0){
				time=""+order_cycle+this.getString(R.string.hour_unit);
			}else if(order_type==1){
				time=""+order_cycle+this.getString(R.string.month_unit);
			}
			mTextView6.setText(""+time);
		}
	}

	/**
	 * 初始化
	 */
	public void setupViews() {
		
		mOkButton = (Button) findViewById(R.id.ok_btn);
		mCloseButton = (ImageButton) findViewById(R.id.close);
		mTextView1 = (TextView) findViewById(R.id.text_1);
		
		myToast = new Toast(AppChargeConfirmActivity.this);
		toastView = getLayoutInflater().inflate(R.layout.order_toast_layout, null);
		toastText = (TextView)toastView.findViewById(R.id.text);
		myToast.setGravity(Gravity.BOTTOM , 0, 20);
		myToast.setDuration(Toast.LENGTH_SHORT);
		myToast.setView(toastView);
		
		mTextView2 = (TextView) findViewById(R.id.text_2);
		mTextView3 = (TextView) findViewById(R.id.text_3);
		mTextView4 = (TextView) findViewById(R.id.text_4);
		mTextView5 = (TextView) findViewById(R.id.text_5);
		mTextView6 = (TextView) findViewById(R.id.text_6);
		mTextTitle = (TextView) findViewById(R.id.title);
		mImageIcon = (ImageView) findViewById(R.id.icon);
		
		mCloseButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (v.getId() == R.id.close) {
					if(cancelClickListener != null){
						cancelClickListener.onClick(v);
					}
					finish();
				}
			}
		});
		setCloseFocuseChange(mCloseButton);
		
		mOkButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
			    Log.d(TAG,"ddddddddddddddddddddddddddddddddddddddddddddddddd ok = " + code + "/" + scode);
                if (v.getId() == R.id.ok_btn) {
                    if(okClickListener != null){
                        okClickListener.onClick(v);
                    } 
                    startProgressDialog();
                    if(isCheck){
                        return;
                    }
                    new Thread(){
                        public void run() {
                            SubsParam subsParam = new SubsParam();
                            AppLog.d(TAG, "---subsParam.setServiceCodes = "+scode);
                            AppLog.d(TAG, "---subsParam.setProductCode = "+code);
                            subsParam.setServiceCodes(scode);
                            subsParam.setProductCode(code);
                            subsParam.setAuthType(PayMng.AUTHTYPE_PRODUCT);
                            subsParam.setBizType(PayMng.BIZTYPE_APP) ;
                            final boolean b = mPayMng.order(new PayMng.CallbackActionListener() { 
                                @Override
                                public void execute() {
                                    Log.d(TAG," ------------  after order is ok -----------");
                                    confirHandler.sendEmptyMessage(MSG_ORDER_SUCCESS);
                                    if(mPayMng.getResult()!=null){
                                        if(ResultParam.class.isInstance(mPayMng.getResult())){
                                            ResultParam rp=(ResultParam)mPayMng.getResult();
                                            AppLog.d(TAG, " >>>>>>>>>>>reCode = "+rp.getReturnCode());
                                            if(rp.getReturnCode()==PayMng.REQ_ORDER_SUCCESS){
                                                Intent intent=new Intent(Constants.INTENT_ACTION_ORDER_OK);
                                                intent.putExtra("app_id", ""+mAppId);
                                                intent.putExtra("product_Code", code);
                                                sendBroadcast(intent);
                                                AppChargeConfirmActivity.this.finish();
                                            }else if(rp.getReturnCode()==PayMng.REQ_MONEY_NOT_ENOUGH){
                                                recharge();//TODO  子线程 调 异步方法会有问题不?
                                                confirHandler.sendEmptyMessage(MSG_NOT_MONERY);
                                            }else{
                                                Log.d(TAG, "serCode="+scode+";proCode="+code+";pgkName="+pkgName+";price="+price);
                                                AppChargeMsgActivity.openActivityOrderFail(AppChargeConfirmActivity.this,
                                                        AppChargeConfirmActivity.this.getString(R.string.order_failed), 
                                                        AppChargeConfirmActivity.this.getString(R.string.order_exception),
                                                        scode,code,pkgName,
                                                        price,
                                                        mAppId,
                                                        AppChargeMsgActivity.TYPE_ORDER_AGAIN,mIcon);
                                                AppChargeConfirmActivity.this.finish();
                                            }
                                        }
                                    }else{
                                        AppLog.d(TAG, "------------mPayMng.getResult()==null-----------");
                                    }
                                }
                            },subsParam); 
                        }
                    }.start();
                }
			}
		});
		mCloseButton.setNextFocusDownId(mOkButton.getId());
		/* 确定按钮动效 */
		mOkButton.setOnFocusChangeListener(new OnFocusChangeListener() {
			AnimationDrawable draw = null;
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					mOkButton.setBackgroundResource(R.drawable.yellow_bt_ok);
				} else {
					mOkButton.setBackgroundResource(R.drawable.blue_bt_ok);
				}
			}
		});
		  
		
		//TODO for test
		mTextView2.setText("搜索");
		mTextView4.setText(getString(R.string.app_order_price_val,"5.0"));
		mTextView6.setText(getString(R.string.app_order_expired_val,"3")); 
		mOkButton.requestFocus();
	}
  
	
	private void setCloseFocuseChange(View view) {
		view.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			AnimationDrawable draw = null;
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					((ImageView) v).setImageResource(R.drawable.close_selected_animation);
					draw = (AnimationDrawable) ((ImageView) v).getDrawable();
					draw.start();
				} else {
					if (null != draw && draw.isRunning()) {
						((ImageView) v).setImageDrawable(null);
						draw.stop();
					}
				}
			}
		});
	}
 
	protected void onPause() {
		super.onPause();
	}
	protected void onResume() {
	    super.onResume();
	    AppLog.d(TAG, "---------------AppChargeConfirmActivity------------onResume");
	}
	
	public static void openConfirmOrderDialog(Context context , SubsProduct subsProduct,String nativeServiceCode,String pkgName,int appid,Bitmap icon){
		Intent intent=new Intent();
		intent.setClass(context, AppChargeConfirmActivity.class); 
		intent.putExtra(T_CODE, subsProduct.getCode());
		
//		intent.putExtra(T_SERVICE_CODE, subsProduct.getServiceCodes());
		intent.putExtra(T_SERVICE_CODE, nativeServiceCode);//serviceCode用本地的，因鉴权时不会返回serviceCode
		
		intent.putExtra(T_NAME, subsProduct.getName());
		intent.putExtra(T_PRICE, subsProduct.getPrice());
		intent.putExtra(T_VALID, subsProduct.getEndTime().getTime());
		intent.putExtra(T_APPID, appid);//appid 为-100  是礼包
		intent.putExtra(T_ORDER_TYPE, subsProduct.getOrderType());
		intent.putExtra(T_ORDER_CYCLE, subsProduct.getOrderCycle());
		intent.putExtra(T_PKGNAME, pkgName);
		mIcon=icon;
		context.startActivity(intent);
	}
	
	/* ----------------------------------------支付成功后的处理---------------------------------------------- */
	private BesTVpayResult mBesTVpayResult;
	private BestvWalletPay mBestvWalletPay = null;
	private IBesTVWalletServiceCallBackListener onCallBackListener ;
	/* 跳转到充值 */
	public void recharge(){
	    mBestvWalletPay = new com.bestv.ott.epay.pay.BestvWalletPay();
	    onCallBackListener = new IBesTVWalletServiceCallBackListener(){
            public void getResult(BesTVpayResult arg0) {
                mBesTVpayResult = arg0;
                AppLog.d("---------------getResult-------------", arg0.getResult());
                try {
                    if(arg0.getResult() == null|| "".equals(arg0.getResult())){
                        confirHandler.sendEmptyMessage(MSG_RECHARGE_ERROR);
                    }else if("0".equals(arg0.getResult())){
                        if (arg0.getTransState() == null|| arg0.getTransState().equals("")) {
                            confirHandler.sendEmptyMessage(MSG_RECHARGE_SUCCESS);
                        } else if ("0".equals(arg0.getTransState())) {
                            confirHandler.sendEmptyMessage(MSG_RECHARGE_SUCCESS);
                        } else {
                            confirHandler.sendEmptyMessage(MSG_RECHARGE_FAIL);
                        }
                    }else{
                        confirHandler.sendEmptyMessage(MSG_RECHARGE_FAIL);
                    }
                } catch (Exception e) {
                }finally{
                    confirHandler.sendEmptyMessageDelayed(MSG_CLOSE, 1000);
                }
            }
	    };
	}
}
