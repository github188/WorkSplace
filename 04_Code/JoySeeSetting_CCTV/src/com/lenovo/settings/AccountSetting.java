package com.lenovo.settings;

import android.app.Fragment;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lenovo.tvcustom.lenovo.LenovoService;
import com.lenovo.tvcustom.utils.CommUtils;

public class AccountSetting extends Fragment{

	protected static final String TAG = "AccountSetting";
	private static final String NAME_FORMAT_ERROR = "USS-0100";
	private static final String PASSWORD_ERROR = "USS-0101";
	private static final String NAME_NOT_EXIST = "USS-0103";
	private static final String ACCOUNT_NOT_ACTIVE = "USS-0105";
	private static final String ACCOUNT_DISABLED = "USS-0111";
	private static final String ACCOUNT_LOCKED = "USS-0151";
	private static final String CONNECT_ERROR = "USS-0-1";
	private static final String PROXY_AUTHORIZATION = "USS-0403";
	private static final String PROXY_REFUSED = "USS-0407";
	private static final int MSG_LOGIN_OK = 0;
	private static final int MSG_NAME_FORMAT_ERROR = 1;
	private static final int MSG_PASSWORD_ERROR = 2;
	private static final int MSG_NAME_NOT_EXIST = 3;
	private static final int MSG_ACCOUNT_NOT_ACTIVE = 4;
	private static final int MSG_ACCOUNT_DISABLED = 5;
	private static final int MSG_ACCOUNT_LOCKED = 6;
	private static final int MSG_CONNECT_ERROR = 7;
	private static final int MSG_PROXY_AUTHORIZATION = 8;
	private static final int MSG_PROXY_REFUSED = 9;
	private static final int MSG_PASSWORD_FORMAT_ERROR = 10;
	protected static final int MSG_PASSWORD_VERIFY_OK = 11;
	protected static final int MSG_PASSWORD_VERIFY_FAIL = 12;
	private static final int MSG_NAME_FORMAT_OK = 13;
	private static final int MSG_NAME_IS_EMPTY = 14;
	private static final int MSG_HANDLER_EXIT = 1000;
	private View mAccountView;
	private Button mBtnRegister;
	private EditText mEdAccountName;
	private Button mBtnLogin;
	private Button mBtnForget;
	private EditText mEdAccountPassword;
	private TextView mTvAccountName;  
	private TextView mTvAccountPassword;  
	private RelativeLayout mRelative; 
	private RelativeLayout mLoadingLayout;
	private Toast mToast;
	private AnimationDrawable mAnimationLoading;
	private boolean bError = false;
	protected boolean isExit = false;
	//Hazel add {
	ItemInfo mIT;
	//Hazel add }
	private Handler mHandler = new Handler(){
		String Msg_Prompt = null;
		@Override
		public void handleMessage(Message msg) {
	
			Log.e(TAG,"cur thread = "+Thread.currentThread().getId());
			Log.e(TAG,"message = "+msg.what);
			if(isExit){
				Log.e(TAG,"mHandler exit");
				return;
			}
			switch(msg.what){
			case MSG_LOGIN_OK:
				hideLoadingView();
				Fragment fragment = (Fragment) new AccountLogined();
				SettingFragment sf = (SettingFragment) new SettingFragment(getActivity());
				sf.setFragment(fragment,false);
				break;
			case MSG_PASSWORD_VERIFY_OK:
				mTvAccountPassword.setText("");
				//mImgRegistName.setImageResource(R.drawable.validity_name);
				break;
			case MSG_PASSWORD_FORMAT_ERROR:
				if(mEdAccountPassword.getText().length() == 0){
					mTvAccountPassword.setText(R.string.Account_name_presentation);
				}else{
					mTvAccountPassword.setText(R.string.account_password_format_error);
				}
				break;
			case MSG_NAME_FORMAT_ERROR:
				mTvAccountName.setText(R.string.account_name_format_error);
				break;
			case MSG_NAME_FORMAT_OK:
				mTvAccountName.setText("");
				break;
			case MSG_NAME_IS_EMPTY:
				mTvAccountName.setText(R.string.Account_name_presentation);
				break;
			case MSG_PASSWORD_ERROR:
				mTvAccountPassword.setText(R.string.account_password_error);
				break;
			case MSG_NAME_NOT_EXIST:
				mTvAccountName.setText(R.string.account_name_not_exist);
				break;
			case MSG_ACCOUNT_NOT_ACTIVE:
				mTvAccountName.setText(R.string.account_name_not_active);
				break;
			case MSG_ACCOUNT_DISABLED:
				mTvAccountName.setText(R.string.account_name_disable);
				break;
			case MSG_ACCOUNT_LOCKED:
				mTvAccountName.setText(R.string.account_name_locked);
				break;
			case MSG_CONNECT_ERROR:
				showToastView(getActivity().getResources().getString(R.string.account_network_disconnected));
				break;
			case MSG_PROXY_AUTHORIZATION:
				mTvAccountName.setText(R.string.account_proxy_authorization);
				break;
			case MSG_PROXY_REFUSED:
				mTvAccountName.setText(R.string.account_proxy_refused);
				break;
			case MSG_HANDLER_EXIT:
				isExit = true;
				break;
			}			
		}  
      
	};
	
	private Handler mAnimHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
	
			Log.d(TAG,"cur thread1 = "+Thread.currentThread().getId());
			switch(msg.what){
			case 0:
				//mBtnLogin.setEnabled(false);
				showLoadingView(getActivity().getResources().getString(R.string.account_login_loading),true);
				break;
			case 1:
				hideLoadingView();
				//mBtnLogin.setEnabled(true);
				break;
			}
		}
		
	};
	private Thread mThread = null;
	protected String mPassword;
	private Thread mThreadPassword;
	private Thread mThreadName;
	private boolean mThreadNameEnable = false;
	private boolean mThreadPassowrdEnable = false;
	private boolean mThreadLoginEnable = false;
	
	
	@Override
	public void onDestroy() {

		super.onDestroy();
		Log.d(TAG,"###onDestroy!!");
		//list is dirty. need clean
		ConfigFocus.Master.clear();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		//New participate
		getActivity().getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
	  	mIT=new ItemInfo();
	  
		mRelative = (RelativeLayout) inflater.inflate(R.layout.toast_info, container, false);
		mAccountView = inflater.inflate(R.layout.account_setting, container, false);
		mLoadingLayout = (RelativeLayout) mAccountView.findViewById(R.id.toastLayout);	
		mTvAccountName = (TextView) mAccountView.findViewById(R.id.textName);
		mTvAccountPassword = (TextView) mAccountView.findViewById(R.id.textPassword);
		mEdAccountName = (EditText) mAccountView.findViewById(R.id.editAccountName);
		mEdAccountPassword = (EditText) mAccountView.findViewById(R.id.editAccountPassword);
		mBtnLogin = (Button) mAccountView.findViewById(R.id.btn_login);
		mBtnRegister = (Button) mAccountView.findViewById(R.id.btn_register);
		mBtnForget = (Button) mAccountView.findViewById(R.id.btn_find_password);
		isExit = false;
		mLoadingLayout.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                
                Log.d(TAG, " mLoadingLayout setOnKeyListener keyCode = " + keyCode);
                if(keyCode==KeyEvent.KEYCODE_BACK){
                    mLoadingLayout.setVisibility(View.INVISIBLE);
                }
                return false;
            }
        });
	
	
	//	 mEdAccountName.requestFocus(); 
	  	
	  	mEdAccountName.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
		
				if(!hasFocus){
					String name = mEdAccountName.getText().toString();
					if(name.length() == 0){
						mHandler.sendEmptyMessage(MSG_NAME_IS_EMPTY);
					}else{
						verifyName();
					}
				}
			}
	  		
	  	});
	
		mEdAccountPassword.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
		
				mPassword = mEdAccountPassword.getText().toString();
				if(hasFocus){
					
				}else{
					if((mPassword.length() < 4) || (mPassword.length() > 20)){
						mHandler.sendEmptyMessage(MSG_PASSWORD_FORMAT_ERROR);
					}else{
						verifyPassword();
					}
				}
			}
			
		});
		
		mBtnRegister.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
		
				Fragment fragment = (Fragment) new AccountRegister();
				SettingFragment sf = (SettingFragment) new SettingFragment(getActivity());
				sf.setFragment(fragment,true);
				mIT.MasterId=R.id.btn_register;
			}
			
		});
		mBtnForget.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
		
				Fragment fragment = (Fragment) new AccountForgetPassword();
				SettingFragment sf = (SettingFragment) new SettingFragment(getActivity());
				sf.setFragment(fragment,true);
				mIT.MasterId=R.id.btn_find_password;
			}
			
		});
		mBtnLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
		
				if(mThreadLoginEnable){
					return;
				}
				String name = mEdAccountName.getText().toString();
				String password = mEdAccountPassword.getText().toString();
				mTvAccountName.setText("");
				mTvAccountPassword.setText("");
				Log.d(TAG,"cur thread2 = "+Thread.currentThread().getId());
				//showLoadingView(getActivity().getResources().getString(R.string.account_login_loading),true);
				setAccountLogin(name, password);
			}
			
		});
		LenovoSettingsActivity.setTitleFocus(false);
	  	//Hazel add {
		if(ConfigFocus.Master.size()==0)
		{
	    	Log.d(TAG,"####ConfigFocus.Master==null");	
	    	mEdAccountName.requestFocus(); 
	    	mIT.MasterId=R.id.editAccountName;
	    	ConfigFocus.Master.add(mIT);  
	    
		}
		else
		{
		
		}  
		Log.d(TAG,"####ConfigFocus.Master.size()!=null");	
	    ItemInfo MasterId_t=ConfigFocus.Master.get(0);	
	  	findFxView(MasterId_t.MasterId);
		ConfigFocus.Master.set(0,mIT);
		return mAccountView;
	}
	
	String getAccountName(){		
		return LenovoService.getLenovoUserCache (getActivity());		
	}
	

	void verifyName(){
		if(mThreadNameEnable ){
			return;
		}
		mThreadName  = new Thread(new Runnable(){

			@Override
			public void run() {
		
				mThreadNameEnable = true;
				Log.e(TAG,"verifyName");
				String name = mEdAccountName.getText().toString();
				boolean ret = CommUtils.checkLenovoUsernameFormat (name);
				if(ret){
					mHandler.sendEmptyMessage(MSG_NAME_FORMAT_OK);
				}else{
					mHandler.sendEmptyMessage(MSG_NAME_FORMAT_ERROR);					
				}
				Log.e(TAG,"verifyName1");
				mThreadNameEnable = false;
			}
			
		});
		mThreadName.start();
	}
	
	
	void setAccountLogin(final String name, final String password){
		
		mThread = new Thread(new Runnable(){

			@Override
			public void run() {
		
				int msg_what;
				mThreadLoginEnable = true;
				mAnimHandler.sendEmptyMessage(0);
				Log.d(TAG,"cur thread3 = "+Thread.currentThread().getId());
				String ret = LenovoService.loginUser(getActivity(), name, password);
				mAnimHandler.sendEmptyMessage(1);
				Log.d(TAG,"login user msg = "+ret);

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
				mHandler.sendEmptyMessage(msg_what);
				mThread = null;
				mThreadLoginEnable = false;
			}
			
		});
		mThread.start();
	}
	

	
	void showToastView(String msg){ 
		TextView textView = (TextView)mRelative.findViewById(R.id.toast_text);
		ImageView img = (ImageView)mRelative.findViewById(R.id.toastImage);
		img.setVisibility(View.INVISIBLE);
		textView.setText(msg);
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
		mLoadingLayout.setFocusable(true);
		mLoadingLayout.setVisibility(View.VISIBLE);
		mLoadingLayout.requestFocus();
		if(hasLoading){
			img.setVisibility(View.VISIBLE);
			mAnimationLoading = (AnimationDrawable)img.getBackground();
			if(!mAnimationLoading.isRunning()){
				mAnimationLoading.start();
			}
		}else{
			img.setVisibility(View.INVISIBLE);
		}
	}
	
	void hideLoadingView(){
		//ImageView img = (ImageView)mLoadingLayout.findViewById(R.id.toastImage);
		if(!mAnimationLoading.isRunning()){
			mAnimationLoading.stop();
		}
		//img.setVisibility(View.INVISIBLE);
		mLoadingLayout.setVisibility(View.INVISIBLE);
	}
	
	void verifyPassword(){
		if(mThreadPassowrdEnable){
			return;
		}
		mThreadPassword  = new Thread(new Runnable(){

			@Override
			public void run() {
		
				mThreadPassowrdEnable = true;
				boolean ret =  CommUtils.checkLenovoPasswordFormat (mPassword);
				if(ret){
					mHandler.sendEmptyMessage(MSG_PASSWORD_VERIFY_OK);
				}else{
					mHandler.sendEmptyMessage(MSG_PASSWORD_VERIFY_FAIL);					
				}
				mThreadPassowrdEnable = false;
			}
			
		});
		mThreadPassword.start();
	}
	
	/*boolean saveLoginName(String name){
		boolean ret;
		ret = 
		return ret;
	}*/
	
	public void findFxView(int View_ID)
  	{
     	switch(View_ID)
   	 	{
	   	 	case R.id.btn_login:
	   	 		mBtnLogin.requestFocus(); 
	   	 	break;
   	 	
	   	 	case R.id.btn_register:
	   	 	mBtnRegister.requestFocus(); 
	   	 	break;
   	 	
	   	 	case R.id.btn_find_password:
	   	 	mBtnForget.requestFocus(); 
	   	 	break;
   	 
	
   	 	}
   
  	}

	@Override
	public void onPause() {

		super.onPause();
		if(mLoadingLayout.getVisibility() == View.VISIBLE){
			mAnimHandler.sendEmptyMessage(1);
		}
		mHandler.sendEmptyMessage(MSG_HANDLER_EXIT);
	}
}
