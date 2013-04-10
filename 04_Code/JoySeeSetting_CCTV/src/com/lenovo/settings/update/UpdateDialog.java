package com.lenovo.settings.update;

import java.io.File;
import java.io.IOException;

import com.lenovo.settings.PowerOffDialog;
import com.lenovo.settings.R;
import com.lenovo.settings.Util.Recovery;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RecoverySystem;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class UpdateDialog extends Activity {

	private static final int MSG_SHOW_UPDATE_REBOOT = 1000;
	private static final int MSG_SHOW_POWER_OFF_DIALOG = 1001;

	protected static final String TAG = "UpdateDialog";
	
	private Button btnCancel;
	private Button btnConfirm;
	private TextView msg;
	private String mPath;
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
	
			switch(msg.what){
			case MSG_SHOW_UPDATE_REBOOT:
				String path = (String) msg.obj;
				Log.e(TAG,"update path = "+path);
				Recovery.reboot(UpdateDialog.this, path);
				break;
			case MSG_SHOW_POWER_OFF_DIALOG:
				// Rony add customer power off dialog for lenovo,
				//	framework must be close the dialog 20120405 
				//Intent intent = new Intent(UpdateDialog.this,PowerOffDialog.class); 
				//intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				//startActivity(intent);
				PowerOffDialog.showPowerOffDialog(UpdateDialog.this);
				break;
			}
		}
		
	};
	private String mName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE); 
		getWindow().setBackgroundDrawableResource(R.drawable.sd_none);
		setContentView(R.layout.update_dialog);
		//setTheme(R.style.DialogStyle);
		Intent intent = this.getIntent();
		mPath = intent.getStringExtra("path");
		mName = intent.getStringExtra("name");// Rony modify 20120425

        msg = (TextView) findViewById(R.id.textview_dialog);
        msg.setText(R.string.dlg_auto_update_msg);
        btnConfirm = (Button) findViewById(R.id.btn_dlg_confirm);
        btnConfirm.requestFocus();
        btnConfirm.setText(R.string.update_yes);
        btnConfirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
				mHandler.sendEmptyMessage(MSG_SHOW_POWER_OFF_DIALOG);
				Message msg = new Message();
				String path = mPath + "/" + mName;// Rony modify 20120425
				msg.what = MSG_SHOW_UPDATE_REBOOT;
				msg.obj = path;// Rony modify 20120425
				mHandler.sendMessage(msg);
				/*try {
					File file = new File(mPath);
					RecoverySystem.installPackage(UpdateDialog.this, file);
				} catch (IOException e) {
						
				}*/
				
			}
        	
        });
        
        btnCancel = (Button) findViewById(R.id.btn_dlg_cancel);
        btnCancel.setText(R.string.update_no);
        btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
		
				sendBroadcast();
				String path = mPath + "/" + mName;// Rony modify 20120425
				Recovery.reboot(UpdateDialog.this, path);
				String str = getString(R.string.dlg_update_power_on_msg);
				showToastView(str);
				finish();
			}
        });
		//showUpdateDialog(path);
	}


	void sendBroadcast(){
		UpdateStatus.clearUpdateStatus();
		UpdateStatus.setError(true);
		UpdateStatus.sendBroadcastReceiver(this, UpdateStatus.UPDATE_MSG_CHANGE_ACTION);	
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if(keyCode == KeyEvent.KEYCODE_BACK){
			sendBroadcast();
		}
		return super.onKeyDown(keyCode, event);
	}

	void showToastView( String msg){ 
		View view = LayoutInflater.from(this).inflate(R.layout.toast_info, null);
		TextView textView = (TextView)view.findViewById(R.id.toast_text);
		ImageView img = (ImageView)view.findViewById(R.id.toastImage);
		textView.setText(msg);
		img.setVisibility(View.GONE);
		Toast toast = new Toast(this);
		toast.setView(view);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();		
	}
	
	
}
