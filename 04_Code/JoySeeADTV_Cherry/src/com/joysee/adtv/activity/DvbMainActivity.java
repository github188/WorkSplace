package com.joysee.adtv.activity;


import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DefaultParameter;
import com.joysee.adtv.common.DefaultParameter.DvbIntent;
import com.joysee.adtv.common.DvbKeyEvent;
import com.joysee.adtv.common.DvbLog;
import com.joysee.adtv.controller.ActivityController;
import com.joysee.adtv.ui.BCMainBackground;
import com.joysee.adtv.ui.ChannelNumberView;
import com.joysee.adtv.ui.DVBErrorNotify;
import com.joysee.adtv.ui.EpgGuideWindow;
import com.joysee.adtv.ui.LiveGuideWindow;
import com.joysee.adtv.ui.Menu;
import com.joysee.adtv.ui.MiniEpgPanel;
import com.joysee.adtv.ui.OsdPopupWindow;
import com.joysee.adtv.ui.ProgramReserveAlertWindow;
import com.joysee.adtv.ui.VAINotifyWindow;
/**
 * 播放主界面
 * @author wangguohua
 */
public class DvbMainActivity extends Activity {
	private static final String TAG = "DvbMainActivity";
	private static final DvbLog log = new DvbLog(TAG, DvbLog.DebugType.D);
    private ActivityController mController;

	private boolean isKeyRepeating = false;
	private final int mKeyRepeatInterval = 150;
	private long mLastKeyDownTime = -1;
	
	/** 频道号 */
	private ChannelNumberView mDVBChannelNumberView;
	
	private boolean isUserinputing;
	private int mUserInputNumber;
	public boolean isBackToHome;
	
	private Menu mMenu;
	private Dialog mExitDialog;
	
	private static final int NUM_CHANNEL_SWITCH=1;
	private static final int NUM_CHANNEL_SWITCH_NOW=2;
	private static final int SWITCH_CHANNEL_TIMEOUT=3000;
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(mUserInputNumber<=0)
				return;
			switch (msg.what) {
				case NUM_CHANNEL_SWITCH:
					Log.d(TAG, "currentNumber ="+ currentNumber  +" now = "+mController.getChannelNum());
					if(currentNumber != mController.getChannelNum()){
						Log.d(TAG, "--- give up the keyNum to Channel ---");
						break;
					}
					mController.executeNumKey(mUserInputNumber,true);
					break;
				case NUM_CHANNEL_SWITCH_NOW:
					mController.switchChannelFromNum(mUserInputNumber,true);
					break;
			}
			isUserinputing=false;
			mUserInputNumber = 0;
		};
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	log.D("DvbMainActivity onCreate()------------------------------------------");
        super.onCreate(savedInstanceState);
        ColorDrawable colorDrawable = new ColorDrawable(Color.argb(0, 0, 0, 0));
        getWindow().setBackgroundDrawable(colorDrawable);
        setContentView(R.layout.dvb_main);
        mController = new ActivityController(this.getApplicationContext());
        Log.d("type", "--- type = "+mController.getClass());
        initViews();
        mController.init();
    }
    
    private void initViews() {
    	
    	mDVBChannelNumberView = (ChannelNumberView) findViewById(R.id.dvb_channelnum_textview);
		VAINotifyWindow mVAINotifyWindow = new VAINotifyWindow(this);
		DVBErrorNotify mDvbErrorNotify = new DVBErrorNotify(this);
		OsdPopupWindow mOsdPopupWindow = new OsdPopupWindow(this);
		FrameLayout bcMainLayout = (FrameLayout) findViewById(R.id.dvb_bc_main_layout);
		LiveGuideWindow programGuideView = new LiveGuideWindow(this);
		EpgGuideWindow epgGuideView = new EpgGuideWindow(this); 
		ProgramReserveAlertWindow programReserveAlertWindow = new ProgramReserveAlertWindow(this);
		mMenu = new Menu(this);
		
		MiniEpgPanel epgPanel = (MiniEpgPanel) findViewById(R.id.dvb_mini_epg_info_sp);
		mController.registerView(epgPanel);
		
		mController.registerView(mDVBChannelNumberView);
		mController.registerView(mVAINotifyWindow);
		mController.registerView(mDvbErrorNotify);
		mController.registerView(mOsdPopupWindow);
		mController.registerView(mMenu);
		mController.registerView(new BCMainBackground(bcMainLayout));
		
		mController.registerView(programGuideView);
		mController.registerView(epgGuideView);
		mController.registerView(programReserveAlertWindow);
		
		ImageView emailIcon = (ImageView) findViewById(R.id.dvb_mainlayout_email_icon_iv);
		TextView fingerInfoTv = (TextView) findViewById(R.id.dvb_main_fingerinfo_tv);
		mOsdPopupWindow.setEmailcon(emailIcon);
		mOsdPopupWindow.setFingerInfoTv(fingerInfoTv);
	}

    @Override
	public boolean dispatchKeyEvent(KeyEvent event) {
    	int keyCode = event.getKeyCode();
		int action = event.getAction();
		log.D(" keyCode "+keyCode +" action "+action);
		switch (keyCode) {
		case 27:
			return true;
		case KeyEvent.KEYCODE_BACK:
		case KeyEvent.KEYCODE_ESCAPE:
			if (action == KeyEvent.ACTION_DOWN) {
//				finish();
				showAlertDialog();
			}
			return true;
		case KeyEvent.KEYCODE_F12:
			mController.downF12();
			return true;
			
		case KeyEvent.KEYCODE_VOLUME_DOWN:
		case KeyEvent.KEYCODE_VOLUME_UP:
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			 if (action == KeyEvent.ACTION_DOWN) {
					if (mController.isChannelEnable()) {
						final long currenKeyDownTime = SystemClock.uptimeMillis();
						final long interval = currenKeyDownTime - mLastKeyDownTime;
						if (isKeyRepeating && interval < mKeyRepeatInterval) {
							return true;
						}
						mLastKeyDownTime = currenKeyDownTime;
						isKeyRepeating = true;
						if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
							mController.changeVolume(-1);
						else
							mController.changeVolume(1);
						return true;
					}
				} else {
					if (mController.isChannelEnable()) {
						isKeyRepeating = false;
						return true;
					}
				}
			break;
            // TODO :解决mmm编译问题,但是以后是否在frameworks添加KEYCODE_DVB键?---by wuhao 2012-11-05
            // case KeyEvent.KEYCODE_DVB:
            // return true;
		case KeyEvent.KEYCODE_DPAD_UP:
		case KeyEvent.KEYCODE_DPAD_DOWN:
		case KeyEvent.KEYCODE_PAGE_UP:
		case KeyEvent.KEYCODE_PAGE_DOWN:
			if (action == KeyEvent.ACTION_DOWN) {
				final long currenKeyDownTime = SystemClock.uptimeMillis();
				final long interval = currenKeyDownTime - mLastKeyDownTime;
				DvbLog.D("time"," key"+isKeyRepeating+"  "+interval);
				if (isKeyRepeating && interval < mKeyRepeatInterval) {
					return true;
				}
				mLastKeyDownTime = currenKeyDownTime;
				isKeyRepeating = true;
				switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_UP:
					case KeyEvent.KEYCODE_PAGE_UP:
						mController.nextChannel();
						break;
					case KeyEvent.KEYCODE_DPAD_DOWN:
					case KeyEvent.KEYCODE_PAGE_DOWN:
						mController.previousChannel();
						break;
				}
				return true;
			}else if(action == KeyEvent.ACTION_UP){
				mController.actionUp();
				isKeyRepeating = false;
				return true;
			}
		default:
			break;
		}
		return super.dispatchKeyEvent(event);
   }
    private void showAlertDialog() {
    	mExitDialog = new Dialog(this,R.style.alertDialogTheme);
    	View view  = View.inflate(this, R.layout.alert_exit_dialog_layout, null);
    	TextView tv = (TextView) view.findViewById(R.id.alert_title);
    	if(mController.isBCPlaying())
    		tv.setText(R.string.dvb_bc_confirm_exit);
    	Button btOk = (Button) view.findViewById(R.id.alert_confirm_btn);
    	Button btCancel = (Button) view.findViewById(R.id.alert_cancle_btn);
    	btOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DvbMainActivity.this.finish();
			}
		});
    	btCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mExitDialog.dismiss();
			}
		});
    	int width = (int) getResources().getDimension(R.dimen.dvb_exit_width);
        int height = (int) getResources().getDimension(R.dimen.dvb_exit_heigth);
    	mExitDialog.setContentView(view, new LayoutParams(width, height));
    	mExitDialog.show();
	}

	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_HOME) {
    		//TODO 回到桌面
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			
		}else if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
			executeNumKey(keyCode - KeyEvent.KEYCODE_0);
		} else if (keyCode >= KeyEvent.KEYCODE_NUMPAD_0 && keyCode <= KeyEvent.KEYCODE_NUMPAD_9) {
			if (event.isNumLockOn()) {
				executeNumKey(keyCode - KeyEvent.KEYCODE_NUMPAD_0);
			}
		} else if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			if(isUserinputing){
				mHandler.removeMessages(NUM_CHANNEL_SWITCH);
				mHandler.sendEmptyMessage(NUM_CHANNEL_SWITCH_NOW);
			}else{
				mController.showChannelInfo();
			}
		}
        return super.onKeyDown(keyCode, event);
    }
    @Override
    protected void onStart() {
    	super.onStart();
    	log.D("DvbMainActivity onStart()------------------------------------------");
//    	mController.onStart();
    }
    @Override
    protected void onResume() {
    	super.onResume();
    	log.D("DvbMainActivity onResume()------------------------------------------");
    	mController.play(getIntent());
    }
    @Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		resolveIntent(getIntent());
	}
    @Override
    protected void onPause() {
    	super.onPause();
    	mHandler.removeMessages(NUM_CHANNEL_SWITCH);
    	log.D("DvbMainActivity onPause()------------------------------------------");
    	if(mExitDialog!=null && mExitDialog.isShowing())
			mExitDialog.dismiss();
    	mController.stop();
    }
    @Override
    protected void onStop() {
    	log.D("DvbMainActivity onStop()------------------------------------------");
    	super.onStop();
//    	mController.stop();
    }
    @Override
    protected void onDestroy() {
    	log.D("DvbMainActivity onDestroy()------------------------------------------");
    	mController.uninit();
    	mController = null;
    	super.onDestroy();
    }
    
    private void resolveIntent(Intent intent) {
		final String showMenu = intent.getStringExtra(DvbIntent.INTENT_SHOW_MENU);
		if (showMenu == null || showMenu.length() == 0 || !mController.isChannelEnable()) {
			log.D("resolveIntent showMenu is NULL,don't show any menu!");
			return;
		}

		if (showMenu.equals(DefaultParameter.DvbIntent.INTENT_CHANNEL_LIST)) {
			mController.showChannelList();
		} else if (showMenu.equals(DefaultParameter.DvbIntent.INTENT_FAVORITE_LIST)) {
			mController.showFavoriteChannel();
		} 
	}
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
    	switch (keyCode) {
		case KeyEvent.KEYCODE_MENU:// 菜单键
			mController.showMainMenu();
			break;
		case DvbKeyEvent.KEYCODE_TV_BC:// 58 电视广播键:
			mController.switchPlayMode();
			break;
		case DvbKeyEvent.KEYCODE_LIST:// 频道列表
			mController.showChannelList();
			break;
		case DvbKeyEvent.KEYCODE_FAVORITE:// 喜爱键
			mController.showFavoriteChannel();
			break;
		case DvbKeyEvent.KEYCODE_BACK_SEE:// 回看键
		case 29:// 回看键
			mController.backSee();
			break;
		case DvbKeyEvent.KEYCODE_INFO:// 信息键
			mController.showChannelInfo();
			break;
		case DvbKeyEvent.KEYCODE_PROGRAM_GUIDE:// 节目指南键
			mController.showProgramGuide();
			break;
		case DvbKeyEvent.KEYCODE_SOUNDTRACK_SET:// 声道设置键
			mController.showSoundTrackSetting();
			break;
		case KeyEvent.KEYCODE_DPAD_UP:
		case KeyEvent.KEYCODE_DPAD_DOWN:
		case KeyEvent.KEYCODE_PAGE_DOWN:
		case KeyEvent.KEYCODE_PAGE_UP:
			isKeyRepeating = false;
			break;
		case KeyEvent.KEYCODE_ENVELOPE:
			Intent intentStartEmail = new Intent(DvbMainActivity.this, EmailActivity.class);
			startActivity(intentStartEmail);
			break;
		}
    	return super.onKeyUp(keyCode, event);
    }
    
    public int currentNumber;
    
	private void executeNumKey(int inputNum) {
		isUserinputing = true;
		if (mDVBChannelNumberView.getVisibility() != View.VISIBLE) {
			mUserInputNumber = 0;;
		}
		mUserInputNumber = mUserInputNumber * 10 + inputNum;
		mUserInputNumber = mUserInputNumber%1000;
		
		mController.executeNumKey(mUserInputNumber, false);
		currentNumber = mController.getChannelNum();
		Log.d(TAG, "currentNumber = "+currentNumber)
;		mHandler.removeMessages(NUM_CHANNEL_SWITCH);
		mHandler.sendEmptyMessageDelayed(NUM_CHANNEL_SWITCH,SWITCH_CHANNEL_TIMEOUT);
	}

}
