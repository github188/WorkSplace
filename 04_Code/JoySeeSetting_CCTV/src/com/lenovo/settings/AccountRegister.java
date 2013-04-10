package com.lenovo.settings;

import java.io.IOException;
import java.io.InputStream;

import android.app.Dialog;
import android.app.Fragment;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lenovo.leos.push.PsCaptServerToolkit;
import com.lenovo.leos.push.PsUserServerToolkitL;
import com.lenovo.tvcustom.lenovo.LenovoService;
import com.lenovo.tvcustom.utils.CommUtils;

public class AccountRegister extends Fragment implements OnClickListener {

	protected static final int REGISTER_OK = 0;
	protected static final int REGISTER_NAME_FORMAT_ERROR = 100;
	protected static final int REGISTER_NAME_HAS_EXISTED = 104;
	protected static final int REGISTER_VERIFY_CODE_ERROR = 140;
	protected static final int REGISTER_PASSWORD_FORMAT_ERROR = 170;
	protected static final int REGISTER_NETWORK_DISCONNECTED = -1;
	protected static final int REGISTER_PROXY_AUTHORIZATION = 403;
	protected static final int REGISTER_PROXY_REFUSED = 407;
	protected static final int REGISTER_VERIFY_START = 10;
	protected static final int REGISTER_VERIFY_STOP = 11;
	protected static final int REGISTER_NAME_VERIFY_OK = 12;
	protected static final int REGISTER_NAME_VERIFY_FAIL = 13;
	protected static final int REGISTER_PASSWORD_VERIFY_OK = 14;
	protected static final int REGISTER_PASSWORD_VERIFY_FAIL = 15;
	protected static final int REGISTER_CONFIRM_PASSWORD_VERIFY_OK = 16;
	protected static final int REGISTER_CONFIRM_PASSWORD_VERIFY_FAIL = 17;
	protected static final int REGISTER_VERIFY_CODE_VERIFY_OK = 18;
	protected static final int REGISTER_VERIFY_CODE_VERIFY_FAIL = 19;
	protected static final int REGISTER_LOADING_START = 20;
	protected static final int REGISTER_LOADING_STOP = 21;
	protected static final int REGISTER_AGREEMENT_DIALOG = 22;
	protected static final int REGISTER_ENABLE_REGISTER = 23;
	protected static final int REGISTER_DISABLE_REGISTER = 24;
	protected static final int REGISTER_LOADING_AGREEMENT_START = 25;
	protected static final int REGISTER_LOADING_AGREEMENT_STOP = 26;
	private static final int MSG_HANDLER_EXIT = 1000;
	protected static final String TAG = "AccountRegister";

	private View mView;
	private EditText mEdName,mEdPassword,mEdPasswordConfirm,mEdVerify;
	private CheckBox mCbConfirm;
	private ImageView mImgVerify,mImgRegistName;
	private TextView mTvName,mTvPassword,mTvPasswordConfirm,mTvVerify,mAgreement;
	private Button mBtnDeclaration,mBtnRegister;
	private ImageView mBtnRefurbish;
	private AnimationDrawable mAnimationDrawable = new AnimationDrawable();
	protected Drawable mDwVerify;
	private String mCaptchaId;
	private String mName,mPassword,mPasswordConfirm,mVerifyCode;
	private Toast mToast;
	private RelativeLayout mRelative;
	private RelativeLayout mLoadingLayout;
	private AnimationDrawable mAnimationLoading = new AnimationDrawable();
	
	private Thread mRegisterThread = null;
	private Thread mThreadVerify = null;
	private Thread mThreadName = null;
	private Thread mThreadPassword = null;
	private Thread mThreadConfirmPassword = null;
	protected boolean isExit = false;
	
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
	
			if(isExit){
				Log.e(TAG,"mHandler exit");
				return;
			}
			switch(msg.what){
			case REGISTER_VERIFY_START:
				if(!mAnimationDrawable.isRunning()){
					Log.e(TAG,"mAnimationDrawable start");
					mImgVerify.setBackgroundResource(R.drawable.register_verify_anim);
					mAnimationDrawable = (AnimationDrawable) mImgVerify.getBackground();
					mAnimationDrawable.start();
				}
				break;
			case REGISTER_VERIFY_STOP:
				if(mAnimationDrawable.isRunning()){
					mAnimationDrawable.stop();
				}
				if(mDwVerify == null){
					mImgVerify.setBackgroundResource(R.drawable.bg_verify);
				}else{
					mImgVerify.setBackgroundDrawable(mDwVerify);
				}
				break;
			case REGISTER_NAME_HAS_EXISTED:
				mTvName.setText(R.string.account_name_has_existed);
				mImgRegistName.setImageResource(R.drawable.invalidity_name);
				break;
			case REGISTER_NAME_VERIFY_OK:
				mTvName.setText("");
				mImgRegistName.setImageResource(R.drawable.validity_name);
				break;
			case REGISTER_NAME_VERIFY_FAIL:
				if(mName.length() == 0){
					mTvName.setText(R.string.Account_name_presentation);
				}else{
					mTvName.setText(R.string.account_name_format_error);
				}
				mImgRegistName.setImageResource(R.drawable.invalidity_name);
				break;
			case REGISTER_PASSWORD_VERIFY_OK:
				mTvPassword.setText("");
				//mImgRegistName.setImageResource(R.drawable.validity_name);
				break;
			case REGISTER_PASSWORD_VERIFY_FAIL:
			case REGISTER_PASSWORD_FORMAT_ERROR:
				if(mEdPassword.length() == 0){
					mTvPassword.setText(R.string.Account_name_presentation);
				}else{
					mTvPassword.setText(R.string.account_password_format_error);
				}
				//mImgRegistName.setImageResource(R.drawable.invalidity_name);
				break;
			case REGISTER_CONFIRM_PASSWORD_VERIFY_OK:
				mTvPasswordConfirm.setText("");
				//mImgRegistName.setImageResource(R.drawable.validity_name);
				break;
			case REGISTER_CONFIRM_PASSWORD_VERIFY_FAIL:
				if(mEdPasswordConfirm.length() == 0){
					mTvPasswordConfirm.setText(R.string.Account_name_presentation);
				}else{
					mTvPasswordConfirm.setText(R.string.account_confirm_password_error);
				}
				//mImgRegistName.setImageResource(R.drawable.invalidity_name);
				break;
			case REGISTER_VERIFY_CODE_VERIFY_OK:
				mTvVerify.setText("");
				//mImgRegistName.setImageResource(R.drawable.validity_name);
				break;
			case REGISTER_VERIFY_CODE_VERIFY_FAIL:
			case REGISTER_VERIFY_CODE_ERROR:
				mTvVerify.setText(R.string.account_verify_code_error);
				//mImgRegistName.setImageResource(R.drawable.invalidity_name);
				break;
			case REGISTER_NETWORK_DISCONNECTED:
				mAgreement.setText(R.string.account_network_disconnected);
				showToastView(getActivity().getResources().getString(R.string.account_network_disconnected),true);
				break;
			case REGISTER_PROXY_AUTHORIZATION:
				mAgreement.setText(R.string.account_proxy_authorization);
				break;
			case REGISTER_PROXY_REFUSED:
				mAgreement.setText(R.string.account_proxy_refused);
				break;
			case REGISTER_OK:
				Fragment fragment = (Fragment) new AccountLogined();
				SettingFragment sf = (SettingFragment) new SettingFragment(getActivity());
				sf.setFragment(fragment,false);
				break;
			case REGISTER_LOADING_START:
				showLoadingView(getActivity().getResources().getString(R.string.Account_register_loading),true);
				break;
			case REGISTER_LOADING_STOP:
				hideLoadingView();
				break;
			case REGISTER_LOADING_AGREEMENT_START:
				showLoadingView(getActivity().getResources().getString(R.string.Account_agreement_loading),true);
				break;
			case REGISTER_LOADING_AGREEMENT_STOP:
				hideLoadingView();
				break;

			case REGISTER_AGREEMENT_DIALOG:
				String text = (String) msg.obj;
				showAgreementDialog(/*getActivity().getResources().getString(R.string.system_settings),*/
						getActivity().getResources().getString(R.string.System_low_agreement),
						text);
				break;
			case REGISTER_ENABLE_REGISTER:
				setRegisterButtonEnalbe(true);
				mBtnRegister.requestFocus();
				mBtnRegister.setAlpha(1f);
				break;
			case REGISTER_DISABLE_REGISTER:
				setRegisterButtonEnalbe(false);
				mBtnRegister.setAlpha(0.7f);
				break;
			case MSG_HANDLER_EXIT:
				isExit = true;
				break;
			}
		}
		
	};
	private Thread mAgreementThread;
	protected boolean mRegisterClicked = false;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mView =  inflater.inflate(R.layout.account_register, container, false); 
		mRelative = (RelativeLayout) inflater.inflate(R.layout.toast_info, container, false);
		mLoadingLayout = (RelativeLayout) mView.findViewById(R.id.toastLayout);	
		mEdName = (EditText) mView.findViewById(R.id.registerName);
		mEdPassword = (EditText) mView.findViewById(R.id.registerPassword);
		mEdPasswordConfirm = (EditText) mView.findViewById(R.id.registerConfirmPassword);
		mEdVerify = (EditText) mView.findViewById(R.id.verification_code);
		mCbConfirm = (CheckBox) mView.findViewById(R.id.checkBoxConfirm);
		mImgVerify = (ImageView) mView.findViewById(R.id.img_verify);
		mImgRegistName = (ImageView) mView.findViewById(R.id.imgRegistName);
		mTvName = (TextView) mView.findViewById(R.id.textName);
		mTvPassword = (TextView) mView.findViewById(R.id.textPassword);
		mTvPasswordConfirm = (TextView) mView.findViewById(R.id.textPasswordConfirm);
		mTvVerify = (TextView) mView.findViewById(R.id.textVerify);
		mAgreement = (TextView) mView.findViewById(R.id.textAgreement);
		mBtnRefurbish = (ImageView) mView.findViewById(R.id.btn_refurbish);
		mBtnDeclaration = (Button) mView.findViewById(R.id.btn_Declaration);
		mBtnRegister = (Button) mView.findViewById(R.id.btn_register);
		isExit = false;
		//mEdName.requestFocus();
		//mEdName.setFocusable(true);
		//mEdName.setFocusableInTouchMode(true);
		mEdName.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
		
				mName = mEdName.getText().toString();
				if(hasFocus){
					//mEdName.setGravity(Gravity.LEFT);
				}else{
					if(mName.length() == 0){
						mHandler.sendEmptyMessage(REGISTER_NAME_VERIFY_FAIL);
					}else{
						verifyName();
					}
				}
			}
			
		});
		mEdPassword.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
		
				mPassword = mEdPassword.getText().toString();
				if(hasFocus){
					
				}else{
					if((mPassword.length() < 4) || (mPassword.length() > 20)){
						mHandler.sendEmptyMessage(REGISTER_PASSWORD_VERIFY_FAIL);
					}else{
						verifyPassword();
					}
				}
			}
			
		});
		mEdPasswordConfirm.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
		
				mPasswordConfirm = mEdPasswordConfirm.getText().toString();
				if(hasFocus){
					
				}else{
					if(((mPasswordConfirm.length() < 4) || (mPasswordConfirm.length() > 20))
							|| (!mPasswordConfirm.equals(mPassword))){
						mHandler.sendEmptyMessage(REGISTER_CONFIRM_PASSWORD_VERIFY_FAIL);
					}else{
						verifyConfirmPassword();
					}
				}
			}
			
		});
		mEdVerify.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
		
				mVerifyCode = mEdVerify.getText().toString();
				if(hasFocus){
					
				}else{
					if(mVerifyCode.length() == 0){
						//mHandler.sendEmptyMessage(REGISTER_VERIFY_CODE_VERIFY_FAIL);
						mTvVerify.setText(R.string.Account_name_presentation);
					}else{
						mTvVerify.setText("");
					}
				}
			}
			
		});
		mImgVerify.setBackgroundResource(R.drawable.register_verify_anim);
		mAnimationDrawable = (AnimationDrawable)mImgVerify.getBackground();
		mBtnRefurbish.setOnClickListener(this);
		mBtnDeclaration.setOnClickListener(this);
		mBtnRegister.setOnClickListener(this);
		mCbConfirm.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
		
				setRegisterButtonEnalbe(isChecked);
				mBtnRegister.setAlpha(isChecked ? 1f : 0.7f);
			}
			
		});
		LenovoSettingsActivity.setTitleFocus(false);
		mEdName.requestFocus(); 
		updateVerifyDrawable(); 
		//showLoadingView("jsdklfjsdkf",true);
		return mView;
	}

	@Override
	public void onClick(View v) {

		switch(v.getId()){
			case R.id.btn_refurbish:
				System.out.println("click me btn_refurbish");
				updateVerifyDrawable();
				break;
			case R.id.btn_Declaration:
				Log.d(TAG,"showUserAgreement ");
				showUserAgreement();
				mHandler.sendEmptyMessage(REGISTER_LOADING_STOP);	
				break;
			case R.id.btn_register:			
				mName = mEdName.getText().toString();
				mPassword = mEdPassword.getText().toString();
				mPasswordConfirm = mEdPasswordConfirm.getText().toString();
				if(mRegisterClicked){
					Log.e(TAG,"the register is begin");
					return;
				}
				if((!mPassword.equals(mPasswordConfirm)) || (mPassword.length() < 4) 
						|| (mName.length() == 0)){
					mHandler.sendEmptyMessage(REGISTER_CONFIRM_PASSWORD_VERIFY_FAIL);
					return;
				}
				mVerifyCode = mEdVerify.getText().toString();
				if(mVerifyCode.length() == 0){
					mHandler.sendEmptyMessage(REGISTER_VERIFY_CODE_VERIFY_FAIL);
					return;
				}
				mTvVerify.setText("");
				mAgreement.setText("");
				mEdVerify.setText("");
				mEdPassword.setText("");
				mEdPasswordConfirm.setText("");
				//showLoadingView(getActivity().getResources().getString(R.string.Account_register_loading),true);
				registerAccount();
				break;
		}
	}
	
	void updateVerifyDrawable(){
		mThreadVerify  = new Thread(new Runnable(){

			@Override
			public void run() {
		
				Log.e(TAG,"updateVerifyDrawable");
				mHandler.sendEmptyMessage(REGISTER_VERIFY_START);
				mDwVerify = getVerifyDrawable();
				mHandler.sendEmptyMessage(REGISTER_VERIFY_STOP);
				Log.e(TAG,"updateVerifyDrawable1");			
			}
			
		});
		mThreadVerify.start();
	}
	
	void verifyName(){
		mThreadName  = new Thread(new Runnable(){

			@Override
			public void run() {
		
				Log.e(TAG,"verifyName");
				boolean ret = CommUtils.checkLenovoUsernameFormat (mName);
				if(ret){
					mHandler.sendEmptyMessage(REGISTER_NAME_VERIFY_OK);
				}else{
					mHandler.sendEmptyMessage(REGISTER_NAME_VERIFY_FAIL);					
				}
				Log.e(TAG,"verifyName1");
				
			}
			
		});
		mThreadName.start();
	}
	
	void verifyPassword(){
		mThreadPassword  = new Thread(new Runnable(){

			@Override
			public void run() {
		
				Log.e(TAG,"verifyPassword");
				boolean ret =  CommUtils.checkLenovoPasswordFormat (mPassword);
				if(ret){
					mHandler.sendEmptyMessage(REGISTER_PASSWORD_VERIFY_OK);
				}else{
					mHandler.sendEmptyMessage(REGISTER_PASSWORD_VERIFY_FAIL);					
				}
				Log.e(TAG,"verifyPassword1");
				
			}
			
		});
		mThreadPassword.start();
	}
	
	void verifyConfirmPassword(){
		mThreadConfirmPassword  = new Thread(new Runnable(){

			@Override
			public void run() {
					
				Log.e(TAG,"verifyConfirmPassword");
				boolean ret =  CommUtils.checkLenovoPasswordFormat (mPasswordConfirm);
				if(ret){
					mHandler.sendEmptyMessage(REGISTER_CONFIRM_PASSWORD_VERIFY_OK);
				}else{
					mHandler.sendEmptyMessage(REGISTER_CONFIRM_PASSWORD_VERIFY_FAIL);					
				}			
				Log.e(TAG,"verifyConfirmPassword1");	
			}
			
		});
		mThreadConfirmPassword.start();
	}

	 /*void verifyCode() {

			mHandler.post(new Runnable(){

				@Override
				public void run() {
			
					boolean ret =  CommUtils.checkLenovoPasswordFormat (mPasswordConfirm);
					if(ret){
						mHandler.sendEmptyMessage(REGISTER_VERIFY_CODE_VERIFY_OK);
					}else{
						mHandler.sendEmptyMessage(REGISTER_VERIFY_CODE_VERIFY_FAIL);					
					}
					
				}
				
			});
		
	}*/
	
	Drawable getVerifyDrawable(){
		Drawable img = null;
		Time time = new Time ();
		time.setToNow ();
		mCaptchaId = time.toString ().substring (0, 18);
		img = PsCaptServerToolkit.captGetImage (getActivity(), mCaptchaId);
		return img;
	}
	
	void showUserAgreement(){
		mAgreementThread  = new Thread(new Runnable(){

			@Override
			public void run() {
		
				String text = null;	
				Log.e(TAG,"showUserAgreement");
				mHandler.sendEmptyMessage(REGISTER_LOADING_AGREEMENT_START);

				//Log.d(TAG,"showUserAgreement dsfsdfsdfsdf");
				try {
					InputStream is = getActivity().getAssets().open("agreement.txt");
					int size = is.available();
					byte[] buffer = new byte[size];
					is.read(buffer);
					is.close();
					text = new String(buffer, "UTF-8");
				} catch (IOException e) {
					
					e.printStackTrace();
				}
				Message msg = new Message();
				msg.what = REGISTER_AGREEMENT_DIALOG;
				msg.obj = text;
				mHandler.sendMessage(msg);
				mHandler.sendEmptyMessage(REGISTER_LOADING_STOP);	
				Log.e(TAG,"showUserAgreement1");
			}
			
		});
		mAgreementThread.start();
		/*String text = null;
		try {
			InputStream is = getActivity().getAssets().open("agreement.txt");
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			text = new String(buffer, "UTF-8");
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		showAgreementDialog(getActivity().getResources().getString(R.string.system_settings),
				getActivity().getResources().getString(R.string.System_low_agreement),
				text);*/
	}
	
	void registerAccount(){
		mRegisterThread  = new Thread(new Runnable(){

			@Override
			public void run() {
		
				//showLoadingView("dshfkljsdklfjsd",true);
				Log.e(TAG,"registerAccount");
				mRegisterClicked  = true;
				mHandler.sendEmptyMessage(REGISTER_LOADING_START);
				mHandler.sendEmptyMessage(REGISTER_DISABLE_REGISTER);
				int ret =  PsUserServerToolkitL.registerAccount (getActivity(), mName, mPassword, mCaptchaId, mVerifyCode);
				if(ret != REGISTER_OK){
					mHandler.sendEmptyMessage(REGISTER_LOADING_STOP);
					mHandler.sendEmptyMessage(REGISTER_ENABLE_REGISTER);
					updateVerifyDrawable(); 
					mHandler.sendEmptyMessage(ret);
				}else{
					String ret_str = LenovoService.loginUser(getActivity(), mName, mPassword);
					mHandler.sendEmptyMessage(REGISTER_OK);
					/*
					if(ret.equals(NAME_FORMAT_ERROR)){
						msg_what = MSG_NAME_FORMAT_ERROR;
					}else if(ret.equals(PASSWORD_ERROR)){
						msg_what = MSG_PASSWORD_ERROR;
					}else if(ret.equals(NAME_NOT_EXIST)){
						msg_what = MSG_NAME_NOT_EXIST;
					}else if(ret.equals(ACCOUNT_NOT_ACTIVE)){
						msg_what = MSG_ACCOUNT_NOT_ACTIVE;
					}else if(ret.equals(ACCOUNT_DISABLED)){
						msg_what = MSG_ACCOUNT_DISABLED;
					}else if(ret.equals(ACCOUNT_LOCKED)){
						msg_what = MSG_ACCOUNT_LOCKED;
					}else if(ret.equals(CONNECT_ERROR)){
						msg_what = MSG_CONNECT_ERROR;
					}else if(ret.equals(PROXY_AUTHORIZATION)){
						msg_what = MSG_PROXY_AUTHORIZATION;
					}else if(ret.equals(PROXY_REFUSED)){
						msg_what = MSG_PROXY_REFUSED;
					}else{
						msg_what = MSG_LOGIN_OK;
					}
					mHandler.sendEmptyMessage(msg_what);*/
					//mHandler.sendEmptyMessage(msg_what);
				}
				mHandler.sendEmptyMessage(REGISTER_LOADING_STOP);
				mRegisterClicked = false;
				Log.e(TAG,"registerAccount1");
			}
			
		});
		mRegisterThread.start();
	}
	
	void showToastView( String msg, boolean hasLoading){ 
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
	
	
	void hideToastView(){
		mToast.cancel();
	}
	
	void showLoadingView(String msg, boolean hasLoading){	
		TextView textView = (TextView)mLoadingLayout.findViewById(R.id.toast_text);
		ImageView img = (ImageView)mLoadingLayout.findViewById(R.id.toastImage);
		textView.setText(msg);
		if(hasLoading){
			if(!mAnimationLoading.isRunning()){
				img.setVisibility(View.VISIBLE);
				mAnimationLoading = (AnimationDrawable)img.getBackground();
				mAnimationLoading.start();
			}
		}else{
			img.setVisibility(View.INVISIBLE);
		}
		mLoadingLayout.setVisibility(View.VISIBLE);
	}
	
	void hideLoadingView(){
		if(mAnimationLoading.isRunning()){
			mAnimationLoading.stop();
		}
		mLoadingLayout.setVisibility(View.INVISIBLE);
	}

	private Dialog AgreementDialog;
	private void showAgreementDialog(String title,/*String title2,*/String msg){
		TextView text_msg,text_title,text_title2;
		AgreementDialog = new Dialog(mView.getContext(),R.style.MiddleDialogStyle);
        AgreementDialog.setContentView(R.layout.system_info_open_source_dialog);
        text_title = (TextView) AgreementDialog.findViewById(R.id.textview_title);
        //text_title2 = (TextView) dialog.findViewById(R.id.textview_title2);
        text_msg = (TextView) AgreementDialog.findViewById(R.id.textView1);
        text_msg.setTextColor(0xffbbbbbb);
        text_msg.setMovementMethod(ScrollingMovementMethod.getInstance());
        text_title.setText(title);
        //text_title2.setText(title2);
        text_msg.setText(msg);
        AgreementDialog.show();
	}	
	
	void setRegisterButtonEnalbe(boolean enable){
		mBtnRegister.setEnabled(enable);
		mBtnRegister.setFocusable(enable);
		//mBtnRegister.setFocusableInTouchMode(enable);
	}
	
	@Override
	public void onPause() {

		super.onPause();
		mHandler.sendEmptyMessage(MSG_HANDLER_EXIT);
	}
	@Override
	public void onStop() {
	    
        if (AgreementDialog != null && AgreementDialog.isShowing()) {
            AgreementDialog.dismiss();
        }
	    super.onStop();
	}
}
