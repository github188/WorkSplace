package com.lenovo.settings;

import com.lenovo.tvcustom.lenovo.LenovoService;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.util.Log;
public class AccountLogined extends Fragment { 

	protected static final int MSG_LOGOUT_OK = 0;
	protected static final int MODIFY_MODIFY_LOADING_START = 10;
	protected static final int MODIFY_MODIFY_LOADING_STOP = 11;
	private static final int MSG_HANDLER_EXIT = 1000;
	private View mView;
	private Button mLogout;
	private Button mModifyPassword;
	private TextView mLoginInfo;
	private RelativeLayout mLoadingLayout;	
	private AnimationDrawable mAnimationLoading;	
	public static final String TAG="AccountLogined";
	//Hazel add {
	ItemInfo mIT;
	protected boolean isExit = false; 
	//Hazel add }
	private Handler mHandler = new Handler(){
		
		@Override
		public void handleMessage(Message msg) {
	
			if(isExit){
				Log.e(TAG,"mHandler exit");
				return;
			}
			switch(msg.what){
			case MSG_LOGOUT_OK:
//				showLogoutedDialog();
				Fragment fragment = (Fragment) new AccountSetting();
				SettingFragment sf = (SettingFragment) new SettingFragment(getActivity());
				sf.setFragment(fragment,false);
				Intent intent = new Intent();
				intent.setAction("action.for.com.novel.suptertv.vod");
				getActivity().sendBroadcast(intent);
				break;
			case MODIFY_MODIFY_LOADING_START:
				showLoadingView(getActivity().getResources().getString(R.string.account_dlg_logouting),true);
				break;
			case MODIFY_MODIFY_LOADING_STOP:
				hideLoadingView();
				break;
			case MSG_HANDLER_EXIT:
				isExit = true;
				break;
			default:
				break;
			}			
		}  
      
	};
	protected boolean mLogoutEnable = false;
	
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

		mView = inflater.inflate(R.layout.account_logined, container, false);
		mLoadingLayout = (RelativeLayout) mView.findViewById(R.id.toastLayout);	
		mLogout = (Button) mView.findViewById(R.id.btn_logout);
		mModifyPassword = (Button) mView.findViewById(R.id.btn_modify_password);
		mLogoutEnable = false;
		isExit = false;
		//mLogout.requestFocus();
		//mLogout.setFocusable(true);
		//mLogout.setFocusableInTouchMode(true);
	   //
	   
		//mLogout.requestFocus(); 
		mLoginInfo = (TextView) mView.findViewById(R.id.textLogLogin);
		String log = getActivity().getResources().getString(R.string.account_logined);
		mLoginInfo.setText(getAccountName()+log);
		mLogout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
		
				if(mLogoutEnable){
					return;
				}
				mIT.MasterId=R.id.btn_logout;
				showLogoutDialog();
			}
			
		});
		mModifyPassword.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
		
				Fragment fragment = (Fragment) new AccountModifyPassword();
				SettingFragment sf = (SettingFragment) new SettingFragment(getActivity());
				sf.setFragment(fragment,true);
					mIT.MasterId=R.id.btn_modify_password;
			}
			
		});
		LenovoSettingsActivity.setTitleFocus(false);
	    mIT=new ItemInfo();
	   	if(ConfigFocus.Master.size()==0)
	   	{
	     	Log.d(TAG,"%%%ConfigFocus.Master.size()==0");
	     	mLogout.requestFocus(); 
	     	mIT.MasterId=R.id.btn_logout;
	      	ConfigFocus.Master.add(mIT); 
	   	
	   	}else
	   	{
	   	   Log.d(TAG,"####ConfigFocus.Master.size()!=null");	
	       ItemInfo MasterId_t=ConfigFocus.Master.get(0);	
	  	   findFxView(MasterId_t.MasterId);
	   	}
		ConfigFocus.Master.set(0,mIT);
		return mView;
	} 

	String getAccountName(){		
		return LenovoService.getLenovoUserCache (getActivity());		
	}
	
	void setLogout(){
		
		new Thread(new Runnable(){
			@Override
			public void run() {
		
				int msg_what;
				mLogoutEnable  = true;
				mHandler.sendEmptyMessage(MODIFY_MODIFY_LOADING_START);
				msg_what = LenovoService.logout (getActivity());
				Log.e(TAG,"logout error code = "+msg_what);
				mHandler.sendEmptyMessage(MODIFY_MODIFY_LOADING_STOP);
				mHandler.sendEmptyMessage(msg_what);
				mLogoutEnable = false;
			}
			
		}).start();
	}
	private Dialog LogoutDialog;
	private void showLogoutDialog(){
		Button btnConfirm,btnCancel;
		TextView msg;
		LogoutDialog = new Dialog(mView.getContext(),R.style.DialogStyle);
        LogoutDialog.setContentView(R.layout.resolution_dialog);
        //dialog.setCancelable(true);
        msg = (TextView) LogoutDialog.findViewById(R.id.textview_dialog);
        msg.setText(R.string.account_dlg_logout);
        btnConfirm = (Button) LogoutDialog.findViewById(R.id.btn_dlg_confirm);
        btnConfirm.setText(R.string.dlg_confirm);
        btnConfirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
		  
				LogoutDialog.dismiss();
				//mHandler.sendEmptyMessage(MODIFY_MODIFY_LOADING_START);
				setLogout();
				//mHandler.sendEmptyMessage(MODIFY_MODIFY_LOADING_STOP);
			}
        	
        });
        
        btnCancel = (Button) LogoutDialog.findViewById(R.id.btn_dlg_cancel);
        btnCancel.setText(R.string.dlg_cancel);
        btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
		
		
				LogoutDialog.dismiss();
			}
        	
        });
        LogoutDialog.show();
	}	
	private Dialog LogoutedDialog;
	private void showLogoutedDialog(){
		TextView msg;
		Button confirm;
		LogoutedDialog = new Dialog(mView.getContext(),R.style.DialogStyle);
        LogoutedDialog.setContentView(R.layout.net_manual_error_dialog);
        msg = (TextView) LogoutedDialog.findViewById(R.id.textview_dialog);
        confirm = (Button) LogoutedDialog.findViewById(R.id.btn_dlg_confirm);
        msg.setText(R.string.account_dlg_logout_ok);
        confirm.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
		
				LogoutedDialog.dismiss();
			}
        	
        });
        LogoutedDialog.show();
	}
	
	void showLoadingView(String msg, boolean hasLoading){	
		TextView textView = (TextView)mLoadingLayout.findViewById(R.id.toast_text);
		ImageView img = (ImageView)mLoadingLayout.findViewById(R.id.toastImage);
		textView.setText(msg);
		mLoadingLayout.setVisibility(View.VISIBLE);
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
		//mAnimationLoading.stop();
		//img.setVisibility(View.INVISIBLE);
		mLoadingLayout.setVisibility(View.INVISIBLE);
	}
	
public void findFxView(int View_ID)
  {
     switch(View_ID)
   	 {
   	 	case R.id.btn_logout:
   	 	mLogout.requestFocus();
   	 	break;
   	 	
   	 	case R.id.btn_modify_password:
   	 	mModifyPassword.requestFocus();
   	 	break;
     	 	
   	 }
   	
  }

	@Override
	public void onPause() {

		super.onPause();
		mHandler.sendEmptyMessage(MSG_HANDLER_EXIT);
	}

    @Override
    public void onStop() {
        
        if (LogoutDialog != null && LogoutDialog.isShowing()) {
            LogoutDialog.dismiss();
        }
        if (LogoutedDialog != null && LogoutedDialog.isShowing()) {
            LogoutedDialog.dismiss();
        }
        super.onStop();
    }
}
