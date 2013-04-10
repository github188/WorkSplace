package com.lenovo.settings;

import com.lenovo.tvcustom.lenovo.LenovoService;
import com.lenovo.tvcustom.utils.CommUtils;

import android.app.Fragment;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AccountModifyPassword extends Fragment {

	protected static final int MODIFY_OK = 0;
	protected static final int MODIFY_MODIFY_LOADING_START = 10;
	protected static final int MODIFY_MODIFY_LOADING_STOP = 11;
	protected static final int MODIFY_VERIFY_PASSWORD_OK = 12;
	protected static final int MODIFY_VERIFY_PASSWORD_FAIL = 13;
	protected static final int MODIFY_PASSWORD_ERROR = 101;
	protected static final int MODIFY_PASSWORD_FORMAT_ERROR = 170;
	protected static final int MODIFY_PROXY_AUTHORIZATION = 403;
	protected static final int MODIFY_PROXY_REFUSED = 407;
	protected static final int MODIFY_NETWORK_FAIL = -1;
	protected static final int OLD_PASSWORD_ID = 0;
	protected static final int NEW_PASSWORD_ID = 1;
	protected static final int CONFIRM_PASSWORD_ID = 2;
	private static final int MSG_HANDLER_EXIT = 1000;
	protected static final String TAG = "AccountModifyPassword";
	private View mView;
	private EditText mEdOldPassword,mEdNewPassword,mEdConfirmPassword;
	private TextView mTvOldPassword,mTvNewPassword,mTvConfirmPassword;
	private ImageView mVerifyPassword;
	private Button mModify;
	private Toast mToast;
	private RelativeLayout mRelative;
	private RelativeLayout mLoadingLayout;
	private AnimationDrawable mAnimationLoading;
	private Thread mThreadPassword = null;
	private Thread mThreadModify = null;
	protected boolean isExit = false; 
	
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
	
			if(isExit){
				Log.e(TAG,"mHandler exit");
				return;
			}
			switch(msg.what){
			case MODIFY_OK:
				showToastView(getActivity().getResources().getString(R.string.account_save_password_ok),false);
				getActivity().getFragmentManager().popBackStack();
				break;
			case MODIFY_PASSWORD_ERROR:
				mTvOldPassword.setText(R.string.account_password_error);
				mVerifyPassword.setImageResource(R.drawable.validity_name);
				break;
			case MODIFY_PASSWORD_FORMAT_ERROR:
				mTvNewPassword.setText(R.string.account_password_error);
				break;
			case MODIFY_NETWORK_FAIL:
				showToastView(getActivity().getResources().getString(R.string.account_connect_error),false);
				break;
			case MODIFY_PROXY_AUTHORIZATION:
				showToastView(getActivity().getResources().getString(R.string.account_proxy_authorization),false);
				break;
			case MODIFY_PROXY_REFUSED:
				showToastView(getActivity().getResources().getString(R.string.account_proxy_refused),false);
				break;
			case MODIFY_MODIFY_LOADING_START:
				showLoadingView(getActivity().getResources().getString(R.string.Account_modifying_password),true);
				break;
			case MODIFY_MODIFY_LOADING_STOP:
				hideLoadingView();
				break;
			case MODIFY_VERIFY_PASSWORD_OK:
				switch(msg.arg1){
				case OLD_PASSWORD_ID:
					mTvOldPassword.setText("");
					mVerifyPassword.setImageDrawable(null);
					break;
				case NEW_PASSWORD_ID:
					mTvNewPassword.setText("");
					break;
				case CONFIRM_PASSWORD_ID:
					mTvConfirmPassword.setText("");
					break;
				}
				break;
			case MODIFY_VERIFY_PASSWORD_FAIL:
				switch(msg.arg1){
				case OLD_PASSWORD_ID:
					mTvOldPassword.setText(R.string.account_password_format_error);
					mVerifyPassword.setImageResource(R.drawable.invalidity_name);
					break;
				case NEW_PASSWORD_ID:
					mTvNewPassword.setText(R.string.account_password_format_error);
					break;
				case CONFIRM_PASSWORD_ID:
					mTvConfirmPassword.setText(R.string.account_password_format_error);
					break;
				}
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

		mView = (View) inflater.inflate(R.layout.account_modify_password, container, false);
		mRelative = (RelativeLayout) inflater.inflate(R.layout.toast_info, container, false);
		mLoadingLayout = (RelativeLayout) mView.findViewById(R.id.toastLayout);	
		mEdOldPassword = (EditText) mView.findViewById(R.id.oldPassword);
		mEdNewPassword = (EditText) mView.findViewById(R.id.newPassword);
		mEdConfirmPassword = (EditText) mView.findViewById(R.id.confirmPassword);
		mTvOldPassword = (TextView) mView.findViewById(R.id.textOldPassword);
		mTvNewPassword = (TextView) mView.findViewById(R.id.textNewPassword);
		mTvConfirmPassword = (TextView) mView.findViewById(R.id.textConfirmPassword);
		mVerifyPassword = (ImageView) mView.findViewById(R.id.imgOldPassword);
		mModify = (Button) mView.findViewById(R.id.btn_Modify);
		isExit = false;

		//mEdOldPassword.setFocusable(true);
		//mEdOldPassword.setFocusableInTouchMode(true);
		//mEdOldPassword.requestFocus();
		mEdOldPassword.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
		
				if(!hasFocus){
					String password = mEdOldPassword.getText().toString();
					if(password.length() == 0){
						mTvOldPassword.setText(R.string.Account_name_presentation);
						mVerifyPassword.setImageResource(R.drawable.invalidity_name);
					}else{
						verifyPassword(OLD_PASSWORD_ID,password);
					}
				}
			}
			
		});
		
		mEdNewPassword.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
		
				if(!hasFocus){
					String password = mEdNewPassword.getText().toString();
					if(password.length() == 0){
						mTvNewPassword.setText(R.string.Account_name_presentation);
					}else{
						verifyPassword(NEW_PASSWORD_ID,password);
					}					
				}
			}
			
		});
		
		mEdConfirmPassword.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
		
				if(!hasFocus){
					String password = mEdNewPassword.getText().toString();
					String confirmpassword = mEdConfirmPassword.getText().toString();
					if(password.length() == 0){
						mTvConfirmPassword.setText(R.string.Account_name_presentation);
					}else{
						if(password.equals(confirmpassword)){
							mTvConfirmPassword.setText("");			
						}else{
							mTvConfirmPassword.setText(R.string.account_confirm_password_error);						
						}	
					}
				}
			}
			
		});
		
		mModify.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
		
				String oldpassword,newpassword,confirmpassword;
				oldpassword = mEdOldPassword.getText().toString();
				newpassword = mEdNewPassword.getText().toString();
				confirmpassword = mEdConfirmPassword.getText().toString();
				if((oldpassword.length() == 0) || (newpassword.length() == 0) || (confirmpassword.length() == 0)
						|| (!newpassword.equals(confirmpassword))){
					return;
				}
				mTvOldPassword.setText("");
				mTvNewPassword.setText("");
				mTvConfirmPassword.setText("");
				mVerifyPassword.setVisibility(View.INVISIBLE);
				modifyPassword(oldpassword,newpassword);
			}
			
		});

		LenovoSettingsActivity.setTitleFocus(false);
		mEdOldPassword.requestFocus(); 
		
		return mView;
	}
	
	
	void verifyPassword(final int id,final String password){
		mThreadPassword  = new Thread(new Runnable(){

			@Override
			public void run() {
		
				boolean ret =  CommUtils.checkLenovoPasswordFormat (password);
				Message msg = new Message();
				msg.arg1 = id;
				if(ret){
					msg.what = MODIFY_VERIFY_PASSWORD_OK;
				}else{
					msg.what = MODIFY_VERIFY_PASSWORD_FAIL;		
				}
				mHandler.sendMessage(msg);				
			}
			
		});
		mThreadPassword.start();
	}
	
	void modifyPassword(final String oldPasswd, final String newPasswd){
		mThreadModify = new Thread(new Runnable(){

			@Override
			public void run() {
		
				int ret;
				mHandler.sendEmptyMessage(MODIFY_MODIFY_LOADING_START);
				ret = LenovoService.setPasswdInfo (getActivity(), oldPasswd, newPasswd);
				mHandler.sendEmptyMessage(MODIFY_MODIFY_LOADING_STOP);
				mHandler.sendEmptyMessage(ret);
			}
			
		});
		mThreadModify.start();
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
	}
	

	
	@Override
	public void onPause() {

		super.onPause();
		mHandler.sendEmptyMessage(MSG_HANDLER_EXIT);
	}
}
