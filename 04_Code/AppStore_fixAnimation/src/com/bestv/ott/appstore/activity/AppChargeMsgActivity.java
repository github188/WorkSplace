package com.bestv.ott.appstore.activity;

import java.util.List;

import com.bestv.ott.appstore.common.Constants;
import com.bestv.ott.appstore.common.CustomProgressDialog;
import com.bestv.ott.appstore.common.DataOperate;
import com.bestv.ott.appstore.pay.PayMng;
import com.bestv.ott.appstore.thread.ScoreThread;
import com.bestv.ott.appstore.utils.AppLog;
import com.bestv.ott.appstore.utils.FileUtils;
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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
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
 * success
 */
public class AppChargeMsgActivity extends Activity { 
	public static final String TAG_TITLE = "title" ;
	public static final String TAG_CONTENT = "content" ;
	public static final String TAG_FLAG = "flag" ;
	public static final String T_APPID = "app_id";
	public static final String T_PKKNAME = "pkgname";
	public static final String T_PRICE = "price";
	
	public static final int TYPE_ORDER_FAILED = 1 ;
	public static final int TYPE_ORDER_AGAIN = 2 ;
	
	public static final int MSG_DIALOG = 1 ;
	
	private static final String T_CODE = "code";
	private static final String T_PRODCUT_CODE = "product_code"; 
	private static final String TAG = "AppChargeMsgActivity";
    protected static final int MSG_ERROR = 10;
    protected static final int MSG_RECHARGE_ERROR = 11;
    protected static final int MSG_RECHARGE_SUCCESS = 12;
    protected static final int MSG_RECHARGE_FAIL = 13;
            protected static final int MSG_CLOSE = 14;;
    protected static final int MSG_NOT_MONERY = 15;
    protected static final int MSG_TO_MONERY = 16;
    
	//TODO by penghui
    private TextView mMsgText;
    private TextView mTitle;
    private ImageView mImageIcon;
    
    private String title ;
    private Drawable titleIcon ; 
    private Button mOkButton;
    private ImageButton mCloseButton;
	private Intent intent;
	private int mAppId;
	private int mAmount;
	private String mUserID;
	private CustomProgressDialog progressDialog;
	private int flag ; 
	private String pkgName;
	private double price;
	private PayMng mPayMng ;
	private static Bitmap mIcon;
	private Toast myToast;
    private View toastView;
    private TextView toastText;
	
	@Override
	protected void onDestroy() {
		if(mPayMng != null){
			mPayMng.destroy();
		}
		if(mIcon!=null){
		    mIcon = null;
		}
		super.onDestroy();
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setTitleIcon(Drawable titleIcon) {
		this.titleIcon = titleIcon;
	} 
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.app_charge_msg);
		mPayMng = new PayMng(this);
		setupViews();
		processIntent();
	}
	
	public void startProgressDialog(){
		if (progressDialog == null){
			progressDialog = CustomProgressDialog.createDialog(AppChargeMsgActivity.this);
		}
    	progressDialog.show();
	}
	
	public void stopProgressDialog(){
		if (progressDialog != null){
			progressDialog.dismiss();
			progressDialog = null;
		}
	}
	
	private String code ;
	private String productCode;
	private String msg;
	
	private void processIntent() { 
		Intent intent = getIntent();
		if(intent != null ){
			code = intent.getStringExtra(T_CODE) ;
			productCode = intent.getStringExtra(T_PRODCUT_CODE) ; 
			title = getIntent().getStringExtra(TAG_TITLE);
			msg = getIntent().getStringExtra(TAG_CONTENT);
			flag = getIntent().getIntExtra(TAG_FLAG,0); 
			mAppId=intent.getIntExtra(T_APPID, -1);
			pkgName=intent.getStringExtra(T_PKKNAME);
			price=intent.getDoubleExtra(T_PRICE, (double)0);
		}
		
		if(msg != null){
			mMsgText.setText(msg);
		}
		if(title != null){
            mTitle.setText(title);
        }

		mOkButton.setBackgroundResource(R.drawable.app_order) ;
		if(flag == TYPE_ORDER_AGAIN){
		    mOkButton.setText(R.string.app_order_again);
		}else if(flag==TYPE_ORDER_FAILED){
		   
			mImageIcon.setImageResource(R.drawable.icon_prompt);
		}
		if(mIcon!=null){
		    mImageIcon.setImageBitmap(mIcon);
		}else{
		    mImageIcon.setImageResource(R.drawable.icon_prompt);
		}
		mOkButton.setVisibility(View.VISIBLE);
		mOkButton.requestFocus();
	}
	
	/**
	 * 初始化
	 */
	public void setupViews() {
	    
	    myToast = new Toast(AppChargeMsgActivity.this);
        toastView = getLayoutInflater().inflate(R.layout.order_toast_layout, null);
        toastText = (TextView)toastView.findViewById(R.id.text);
        myToast.setGravity(Gravity.BOTTOM , 0, 20);
        myToast.setDuration(Toast.LENGTH_SHORT);
        myToast.setView(toastView);
	    
		mOkButton = (Button) findViewById(R.id.ok_btn);
		mCloseButton = (ImageButton) findViewById(R.id.close);
		mMsgText = (TextView) findViewById(R.id.text_1);
		mTitle = (TextView) findViewById(R.id.id_title);
		mImageIcon = (ImageView) findViewById(R.id.icon);
		mCloseButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (v.getId() == R.id.close) { 
					finish();
				}
			}
		});
		setCloseFocuseChange(mCloseButton);
		
		mOkButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d(TAG, "  mOkButton onClick  flag="+flag);
				if (v.getId() == R.id.ok_btn) {
				    if(flag == TYPE_ORDER_AGAIN ){//只有网络失败才让重新订购
				        orderAgain();
				    }else{
				        finish();
				    }
				}
			}
		});
		mCloseButton.setNextFocusDownId(mOkButton.getId());
		
		/* 确定按钮动效 */
		mOkButton.setOnFocusChangeListener(new OnFocusChangeListener() {
			AnimationDrawable draw = null;
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					Log.d(TAG, "----sure  hasFcus-----");
					mOkButton.setBackgroundResource(R.drawable.yellow_bt_ok);
				} else {
					Log.d(TAG, "----no hasFcus-----");
					mOkButton.setBackgroundResource(R.drawable.blue_bt_ok);
				}
			}
		});
	}
	
	Handler mainHandler=new Handler(){
		public void handleMessage(Message msg) {
			switch(msg.what){
			case MSG_DIALOG:
				if((Boolean)msg.obj){
					startProgressDialog();
				}else{
					stopProgressDialog();
				}
				break;
			case MSG_ERROR:
			    Toast.makeText(getApplicationContext(), "服务错误，请稍后再试!", 0).show();
			    finish();
			    break;
			case MSG_NOT_MONERY:
			    toastText.setText(AppChargeMsgActivity.this.getString(R.string.monery_not_enough));
                myToast.show();
                mainHandler.sendEmptyMessageDelayed(MSG_TO_MONERY, 2000);
			    break;
			case MSG_TO_MONERY:
			    if(mBestvWalletPay!=null){
                    mBestvWalletPay.rechargePay(pkgName, onCallBackListener,  String.valueOf(price), AppChargeMsgActivity.this);
                }
			    break;
			case MSG_RECHARGE_ERROR:
			    toastText.setText(AppChargeMsgActivity.this.getString(R.string.charge_fail));
                myToast.show();
			    break;
			case MSG_RECHARGE_SUCCESS:
			    toastText.setText(AppChargeMsgActivity.this.getString(R.string.charge_success));
                myToast.show();
			    break;
			case MSG_RECHARGE_FAIL:
			    toastText.setText(AppChargeMsgActivity.this.getString(R.string.charge_fail));
                myToast.show();
			    break;
			case MSG_CLOSE:
			    if(mBestvWalletPay!=null){
                    mBestvWalletPay.unregisterCallback("com.bestv.epay.view", onCallBackListener);
                    mBestvWalletPay.disconnectService();
                }
			    break;
			}
		};
	};
	
  
	public void orderAgain(){
		startProgressDialog();
		SubsParam subsParam = new SubsParam();
		AppLog.d(TAG, "-------orderAgain--------");
		AppLog.d(TAG, "-------serviceCode :"+code);
		AppLog.d(TAG, "-------ProductCode :"+productCode);
		subsParam.setServiceCodes(code);
		subsParam.setProductCode(productCode);
		subsParam.setAuthType(PayMng.AUTHTYPE_PRODUCT);
		subsParam.setBizType(PayMng.BIZTYPE_APP) ;
		final boolean b = mPayMng.order(new PayMng.CallbackActionListener() { 
			@Override
			public void execute() {
				stopProgressDialog();
				Message mes=mainHandler.obtainMessage(MSG_DIALOG, false);
				mainHandler.sendMessage(mes);
				Log.d(TAG,"aaaaaaaaaaaaaaaaaaaaaa after order! ok ");
				if(mPayMng.getResult()!=null){
					if(ResultParam.class.isInstance(mPayMng.getResult())){
						ResultParam rp=(ResultParam)mPayMng.getResult();
						AppLog.d(TAG, "   >>>>>>>>>>>>>retCode="+rp.getReturnCode());
						if(rp.getReturnCode()==PayMng.REQ_ORDER_SUCCESS){//order success
							Intent intent=new Intent(Constants.INTENT_ACTION_ORDER_OK);
							intent.putExtra("app_id", ""+mAppId);
							intent.putExtra("product_code",productCode);
							sendBroadcast(intent);
							AppChargeMsgActivity.this.finish();
						}else if(rp.getReturnCode()==PayMng.REQ_MONEY_NOT_ENOUGH){
						    recharge();
						    mainHandler.sendEmptyMessage(MSG_NOT_MONERY);
						}else{
							/* 重新订购时，如果再失败，就提示错误，而不再让重复订购了 */
						    mainHandler.sendEmptyMessage(MSG_ERROR);
						}
					}
				}
			}
		},subsParam); 
	}
	
	/**
	 * 关闭按钮特效
	 */
	private void setCloseFocuseChange(View view) {
		view.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			AnimationDrawable draw = null;
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					((ImageView) v)
							.setImageResource(R.drawable.close_selected_animation);
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
	
	public static void openActivity(Context context , ResultParam resultParam,int flag){
		Intent intent=new Intent();
		intent.setClass(context, AppChargeMsgActivity.class); 
		intent.putExtra(T_CODE, ""+resultParam.getReturnCode());
		intent.putExtra(TAG_CONTENT, resultParam.getReturnDec()); 
		
		List<SubsProduct> list = resultParam.getOrderProduct();		
		Log.d(TAG,"openMsgActivity:" + list.size());
		for(SubsProduct p : list){
			intent.putExtra(T_PRODCUT_CODE, p.getCode() ); 
			Log.d(TAG,"#############" + p.getDescription() + "/" + p.getName() + "/" + p.getPrice()) ;
			break ;
		}
		
		context.startActivity(intent);
	}
	
	public static void openActivity(Context context , String title,String msg ,int flag){
		Intent intent=new Intent();
		intent.setClass(context, AppChargeMsgActivity.class); 
		intent.putExtra(TAG_TITLE, title);
		intent.putExtra(TAG_CONTENT, msg);
		intent.putExtra(TAG_FLAG, flag);  
		
		context.startActivity(intent);
	}
	
	public static void openActivityOrderFail(Context context , String title,String msg ,String serCode,String proCode,String pkgName,double price,int appID,int flag,Bitmap icon){
		Intent intent=new Intent();
		intent.setClass(context, AppChargeMsgActivity.class); 
		intent.putExtra(TAG_TITLE, title);
		intent.putExtra(TAG_CONTENT, msg);
		intent.putExtra(TAG_FLAG, flag);  
		intent.putExtra(T_APPID, appID);
		intent.putExtra(T_CODE, serCode);
		intent.putExtra(T_PRODCUT_CODE, proCode);
		intent.putExtra(T_PKKNAME, pkgName);
		intent.putExtra(T_PRICE, price);
		mIcon = icon;
		context.startActivity(intent);
	}
	
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
                        mainHandler.sendEmptyMessage(MSG_RECHARGE_ERROR);
                    }else if("0".equals(arg0.getResult())){
                        if (arg0.getTransState() == null|| arg0.getTransState().equals("")) {
                            mainHandler.sendEmptyMessage(MSG_RECHARGE_SUCCESS);
                        } else if ("0".equals(arg0.getTransState())) {
                            mainHandler.sendEmptyMessage(MSG_RECHARGE_SUCCESS);
                        } else {
                            mainHandler.sendEmptyMessage(MSG_RECHARGE_FAIL);
                        }
                    }else{
                        mainHandler.sendEmptyMessage(MSG_RECHARGE_FAIL);
                    }
                } catch (Exception e) {
                }finally{
                    mainHandler.sendEmptyMessageDelayed(MSG_CLOSE, 1000);
                }
            }
        };
    }
}


