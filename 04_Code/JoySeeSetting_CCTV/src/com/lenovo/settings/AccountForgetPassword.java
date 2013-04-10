package com.lenovo.settings;

import com.lenovo.leos.push.PsAuthenServiceL;
import com.lenovo.leos.push.PsCaptServerToolkit;
import com.lenovo.leos.push.PsUserServerToolkitL;
import com.lenovo.tvcustom.utils.CommUtils;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AccountForgetPassword extends Fragment implements OnClickListener{

	protected static final int MSG_VERIFY_START = 10;
	protected static final int MSG_VERIFY_STOP = 11;
	protected static final int MSG_NAME_VERIFY_OK = 12;
	protected static final int MSG_NAME_VERIFY_FAIL = 13;
	protected static final int MSG_LOADING_START = 14;
	protected static final int MSG_LOADING_STOP = 15;
	protected static final int MSG_VERIFY_CODE_NULL = 16;
	protected static final int MSG_FORGET_OK = 0;
	protected static final int MSG_NAME_NOT_EXISTED = 103;
	protected static final int MSG_VERIFY_CODE_ERROR = 140;
	protected static final int MSG_NETWORK_FAIL = -1;
	protected static final int MSG_ACCOUNT_LOCKED = 151;
	protected static final int MSG_PROXY_AUTHORIZATION = 403;
	protected static final int MSG_PROXY_REFUSED = 407;
	protected static final int MSG_RETURN_DISABLE = 1000;
	protected static final int MSG_RETURN_ENABLE = 1001;
	private static final int MSG_HANDLER_EXIT = 1002;
	protected static final String TAG = "AccountForgetPassword";
	private View mView;
	private RelativeLayout mLoadingLayout;
	private EditText mEdName;
	private EditText mEdVerify;
	private ImageView mImgVerify;
	private ImageView mImgRegistName;
	private TextView mTvName;
	private TextView mTvVerify;
	private AnimationDrawable mAnimationDrawable;
	private ImageView mBtnRefurbish;
	private Button mBtnSendPassword;
	private Button mBtnReturn;
	private Thread mThreadName = null;
	private Thread mThreadVerify = null;
	private String mCaptchaId;
	protected Drawable mDwVerify;
	private Thread mThreadSend = null;
	private AnimationDrawable mAnimationLoading;
	private RelativeLayout mRelative;
	private Toast mToast;
	private boolean mThreadNameEnable = false;
	private boolean mThreadVerifyEnable = false;
	private boolean mThreadSendEnable = false;
	protected boolean isExit = false; 
	
	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
	
			if(isExit){
				Log.e(TAG,"mHandler exit");
				return;
			}
			switch(msg.what){
			case MSG_VERIFY_START:
				if(!mAnimationDrawable.isRunning()){
					Log.e(TAG,"mAnimationDrawable start");
					mImgVerify.setBackgroundResource(R.drawable.register_verify_anim);
					mAnimationDrawable = (AnimationDrawable) mImgVerify.getBackground();
					mAnimationDrawable.start();
				}
				break;
			case MSG_VERIFY_STOP:
				if(mAnimationDrawable.isRunning()){
					mAnimationDrawable.stop();
				}
				if(mDwVerify == null){
					mImgVerify.setBackgroundResource(R.drawable.bg_verify);
				}else{
					mImgVerify.setBackgroundDrawable(mDwVerify);
				}
				break;
			case MSG_NAME_VERIFY_FAIL:
				String name = mEdName.getText().toString();
				if(name.length() == 0){
					mTvName.setText(R.string.Account_name_presentation);
				}else{
					mTvName.setText(R.string.account_name_format_error);
				}
				mImgRegistName.setImageResource(R.drawable.invalidity_name);
				break;
			case MSG_NAME_VERIFY_OK:
				mTvName.setText("");
				mImgRegistName.setImageResource(R.drawable.validity_name);
				break;
			case MSG_LOADING_START:
				mTvVerify.setText("");
				mEdVerify.setText("");
				showLoadingView(getActivity().getResources().getString(R.string.Account_find_password)+"...",true);
				break;
			case MSG_LOADING_STOP:
				hideLoadingView();
				break;
			case MSG_FORGET_OK:
				showToastView(getActivity().getResources().getString(R.string.account_forget_password_ok));
				break;
			case MSG_NAME_NOT_EXISTED:
				mTvName.setText(R.string.Account_regist_name_normal);
				mImgRegistName.setImageResource(R.drawable.invalidity_name);
				break;
			case MSG_VERIFY_CODE_ERROR:
				mTvVerify.setText(R.string.account_verify_code_error);
				break;
			case MSG_VERIFY_CODE_NULL:
				mTvVerify.setText(R.string.Account_name_presentation);
				break;
			case MSG_NETWORK_FAIL:
				showToastView(getActivity().getResources().getString(R.string.account_network_connect_error));
				break;
			case MSG_ACCOUNT_LOCKED:
				showToastView(getActivity().getResources().getString(R.string.account_name_locked));
				break;
			case MSG_PROXY_AUTHORIZATION:
				showToastView(getActivity().getResources().getString(R.string.account_proxy_authorization));
				break;
			case MSG_PROXY_REFUSED:
				showToastView(getActivity().getResources().getString(R.string.account_proxy_refused));
				break;
			case MSG_RETURN_ENABLE:
				setRetrunButtonEnable(true);
				break;
			case MSG_RETURN_DISABLE:
				setRetrunButtonEnable(false);
				break;
			case MSG_HANDLER_EXIT:
				isExit = true;
				break;
			}
		}
		
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mRelative = (RelativeLayout) inflater.inflate(R.layout.toast_info, container, false);
		mView = (View) inflater.inflate(R.layout.account_forget_password, container, false);
		mLoadingLayout = (RelativeLayout) mView.findViewById(R.id.toastLayout);	
		mEdName = (EditText) mView.findViewById(R.id.registerName);
		mEdVerify = (EditText) mView.findViewById(R.id.verification_code);
		mImgVerify = (ImageView) mView.findViewById(R.id.img_verify);
		mImgRegistName = (ImageView) mView.findViewById(R.id.imgRegistName);
		mTvName = (TextView) mView.findViewById(R.id.textName);
		mTvVerify = (TextView) mView.findViewById(R.id.textVerify);
		mBtnRefurbish = (ImageView) mView.findViewById(R.id.btn_refurbish);
		mBtnSendPassword = (Button) mView.findViewById(R.id.btn_send_password);
		mBtnReturn = (Button) mView.findViewById(R.id.btn_return);
		isExit = false;
		//mEdName.requestFocus();
		//mEdName.setFocusable(true);
		//mEdName.setFocusableInTouchMode(true);
		LenovoSettingsActivity.setTitleFocus(false);
		mEdName.requestFocus(); 
		mEdName.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
		
				String name = mEdName.getText().toString();
				if(hasFocus){
					//mEdName.setGravity(Gravity.LEFT);
				}else{
					if(name.length() == 0){
						mHandler.sendEmptyMessage(MSG_NAME_VERIFY_FAIL);
					}else{
						verifyName(name);
					}
				}
			}
			
		});
		
		mEdVerify.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
		
				String verifycode = mEdVerify.getText().toString();
				if(hasFocus){
					//mEdName.setGravity(Gravity.LEFT);
				}else{
					if(verifycode.length() == 0){
						mHandler.sendEmptyMessage(MSG_VERIFY_CODE_NULL);
					}else{
						mTvVerify.setText("");
					}
				}
			}
			
		});
		
		mImgVerify.setBackgroundResource(R.drawable.register_verify_anim);
		mAnimationDrawable = (AnimationDrawable)mImgVerify.getBackground();
		mBtnRefurbish.setOnClickListener(this);
		mBtnSendPassword.setOnClickListener(this);
		mBtnReturn.setOnClickListener(this);
		updateVerifyDrawable();
		return mView;
	}

	@Override
	public void onClick(View v) {

		switch(v.getId()){
		case R.id.btn_refurbish:
			updateVerifyDrawable();
			break;
		case R.id.btn_send_password:
			if(mThreadSendEnable){
				return;
			}
			mTvVerify.setText("");
			sendPassword();
			break;
		case R.id.btn_return:	
			FragmentManager fragmentManager = getFragmentManager();
			if(fragmentManager.getBackStackEntryCount() == 0){
				Context context = getActivity().getBaseContext();
				int status = PsAuthenServiceL.getStatus(context);
				Fragment fragment = null;
				if(status == PsAuthenServiceL.LENOVOUSER_OFFLINE){
					fragment = (Fragment) new AccountSetting();					
				}else if(status == PsAuthenServiceL.LENOVOUSER_ONLINE){
					fragment = (Fragment) new AccountLogined();							
				}
				if(fragment != null){
					SettingFragment sf = (SettingFragment) new SettingFragment(getActivity());
					sf.setFragment(fragment,false);
				}
			}else{
				getActivity().getFragmentManager().popBackStack();		
			}
			break;
		}
		
	}
	
		
	@Override
	public void onStop() {

		super.onStop();
		if(mThreadVerify != null){
			mThreadVerify.interrupt();
		}
		if(mThreadName != null){
			mThreadName.interrupt();
		}
		if(mThreadSend != null){
			mThreadSend.interrupt();
		}
	}

	Drawable getVerifyDrawable(){
		Drawable img = null;
		Time time = new Time ();
		time.setToNow ();
		mCaptchaId = time.toString ().substring (0, 18);
		Log.e(TAG,"mCaptchaId = "+mCaptchaId);
		img = PsCaptServerToolkit.captGetImage (getActivity(), mCaptchaId);
		return img;
	}
	
	void updateVerifyDrawable(){
		if(mThreadVerifyEnable){
			return;
		}
		mThreadVerify  = new Thread(new Runnable(){

			@Override
			public void run() {
		
				mThreadVerifyEnable = true;
				mHandler.sendEmptyMessage(MSG_VERIFY_START);
				mDwVerify = getVerifyDrawable();
				mHandler.sendEmptyMessage(MSG_VERIFY_STOP);	
				Log.e(TAG,"updateVerifyDrawable start!");
				mThreadVerifyEnable = false;
			}
			
		});
		mThreadVerify.start();
	}
	
	void verifyName(final String name){
		if(mThreadNameEnable)
			return;
		mThreadName  = new Thread(new Runnable(){

			@Override
			public void run() {
		
				mThreadNameEnable = true;
				boolean ret = CommUtils.checkLenovoUsernameFormat (name);
				if(ret){
					mHandler.sendEmptyMessage(MSG_NAME_VERIFY_OK);
				}else{
					mHandler.sendEmptyMessage(MSG_NAME_VERIFY_FAIL);					
				}
				Log.e(TAG,"verifyName start!");
				mThreadNameEnable = false;
			}
			
		});
		mThreadName.start();
	}
	
	void sendPassword(){
		if(mThreadSendEnable){
			return;
		}
		mThreadSend  = new Thread(new Runnable(){

			@Override
			public void run() {
		
				String name = mEdName.getText().toString();
				String verifycode = mEdVerify.getText().toString();
				mThreadSendEnable = true;
				mHandler.sendEmptyMessage(MSG_RETURN_DISABLE);
				mHandler.sendEmptyMessage(MSG_LOADING_START);
				boolean bool = CommUtils.checkLenovoUsernameFormat (name);
				if(bool){
					mHandler.sendEmptyMessage(MSG_NAME_VERIFY_OK);
				}else{
					mHandler.sendEmptyMessage(MSG_NAME_VERIFY_FAIL);	
					mHandler.sendEmptyMessage(MSG_LOADING_STOP);
					mHandler.sendEmptyMessage(MSG_RETURN_ENABLE);
					mThreadSendEnable = false;
					return;
				}
				if(verifycode.length() == 0){
					mHandler.sendEmptyMessage(MSG_LOADING_STOP);
					mHandler.sendEmptyMessage(MSG_VERIFY_CODE_NULL);
					mHandler.sendEmptyMessage(MSG_RETURN_ENABLE);
					mThreadSendEnable = false;
					return;
				}
				int ret = PsUserServerToolkitL.forgetPassword (getActivity(), name, mCaptchaId, verifycode);
				if(mHandler == null){
					return;
				}
				mHandler.sendEmptyMessage(MSG_LOADING_STOP);
				mHandler.sendEmptyMessage(ret);
				mHandler.sendEmptyMessage(MSG_RETURN_ENABLE);
				updateVerifyDrawable();
				//Log.e(TAG,"sendPassword start!");
				mThreadSendEnable = false;
			}
			
		});
		mThreadSend.start();		
	}
	
	void showLoadingView(String msg, boolean hasLoading){	
		TextView textView = (TextView)mLoadingLayout.findViewById(R.id.toast_text);
		ImageView img = (ImageView)mLoadingLayout.findViewById(R.id.toastImage);
		textView.setText(msg);
		if(hasLoading){
			img.setVisibility(View.VISIBLE);
			mAnimationLoading = (AnimationDrawable)img.getBackground();
			mAnimationLoading.start();
		}else{
			img.setVisibility(View.INVISIBLE);
		}
		mLoadingLayout.setVisibility(View.VISIBLE);
	}
	
	void hideLoadingView(){
		mLoadingLayout.setVisibility(View.INVISIBLE);
		if(mAnimationLoading.isRunning()){
			mAnimationLoading.stop();
		}
	}
	
	void showToastView( String msg){ 
		TextView textView = (TextView)mRelative.findViewById(R.id.toast_text);
		ImageView img = (ImageView)mRelative.findViewById(R.id.toastImage);
		textView.setText(msg);
		img.setVisibility(View.INVISIBLE);
		mToast = new Toast(getActivity());
		mToast.setView(mRelative);
		mToast.setDuration(Toast.LENGTH_LONG);
		mToast.setGravity(Gravity.CENTER, 0, 0);
		mToast.show();		
	}
	
	void setRetrunButtonEnable(boolean enable){
		mBtnReturn.setEnabled(enable);
		mBtnReturn.setFocusable(enable);
	}
	
	@Override
	public void onPause() {

		super.onPause();
		mHandler.sendEmptyMessage(MSG_HANDLER_EXIT);
	}
}
