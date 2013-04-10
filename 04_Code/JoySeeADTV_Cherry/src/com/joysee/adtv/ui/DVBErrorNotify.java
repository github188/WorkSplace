package com.joysee.adtv.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.joysee.adtv.R;
import com.joysee.adtv.activity.SearchMenuActivity;
import com.joysee.adtv.common.DefaultParameter;
import com.joysee.adtv.common.DefaultParameter.DvbIntent;
import com.joysee.adtv.common.DefaultParameter.ServiceType;
import com.joysee.adtv.common.DefaultParameter.ViewMessage;
import com.joysee.adtv.common.DvbLog;
import com.joysee.adtv.common.DvbMessage;
import com.joysee.adtv.common.ToastUtil;
import com.joysee.adtv.controller.ViewController;


public class DVBErrorNotify implements IDvbBaseView {

	private static final String TAG = "com.joysee.adtv.ui.DVBErrorNotify";
	private static final DvbLog log = new DvbLog(TAG, DvbLog.DebugType.D);
	private Activity mActivity;
	private Dialog mErrorNotifyWindow;
	private View mNoChannelNotifyView;
	private Button mNoChannelNotifySearchBtn = null;
	private Button mNoChannelNotifyCancleBtn = null;
	
	private View mSymbolNotifyView;
	private View mCaNotifyView;
	private TextView mCaNotifyText;
	private Handler mHandler = new Handler();
	
	private ViewController mViewController;
	
	public DVBErrorNotify(Activity activity){
		this.mActivity = activity;
	}
	
	private void showNoChannelDialog(final int type) {
		if (mNoChannelNotifyView == null) {
			mNoChannelNotifyView = mActivity.getLayoutInflater().inflate(R.layout.alert_dialog_include_button_layout,
					null);
			mNoChannelNotifyCancleBtn = (Button) mNoChannelNotifyView.findViewById(R.id.cancle_btn);
			mNoChannelNotifySearchBtn = (Button) mNoChannelNotifyView.findViewById(R.id.confirm_btn);
			mNoChannelNotifySearchBtn.setText(R.string.dvb_nochannel_notify_search_btn);
			mNoChannelNotifyCancleBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					DVBErrorNotify.this.removeAllNotify();
					mActivity.finish();
				}
			});
			OnKeyListener keyLis = new OnKeyListener() {
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					int action = event.getAction();
					switch (keyCode) {
					case KeyEvent.KEYCODE_BACK:
					case KeyEvent.KEYCODE_ESCAPE:
						if (action == KeyEvent.ACTION_DOWN) {
							DVBErrorNotify.this.removeAllNotify();
							mActivity.finish();
						}
						return true;
					default:
						break;
					}
					return false;
				}
			};
			mNoChannelNotifyCancleBtn.setOnKeyListener(keyLis);
			mNoChannelNotifySearchBtn.setOnKeyListener(keyLis);
			mNoChannelNotifySearchBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					DVBErrorNotify.this.removeAllNotify();
					final Intent intent = new Intent();
					intent.setClass(mActivity, SearchMenuActivity.class);
					if (type == ServiceType.BC) {
						intent.putExtra(DvbIntent.INTENT_SHOW_MENU, DvbIntent.INTENT_BROADCAST);
					}
					mActivity.startActivity(intent);
				}
			});
		}
		
		final int windowHeight = (int) mActivity.getResources().getDimension(R.dimen.alert_dialog_include_button_height);
		final int windowWidth = (int) mActivity.getResources().getDimension(R.dimen.alert_dialog_include_button_width);
		mErrorNotifyWindow.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		mErrorNotifyWindow.setContentView(mNoChannelNotifyView, new LayoutParams(windowWidth, windowHeight));
		mErrorNotifyWindow.show();
		mNoChannelNotifySearchBtn.requestFocus();
	}
	
	private void showSymbolWindow(){
		if (mSymbolNotifyView == null) {
			mSymbolNotifyView = mActivity.getLayoutInflater().inflate(R.layout.dvb_symbol_notify_layout, null);
		}
		final int windowHeight = (int) mActivity.getResources().getDimension(R.dimen.alert_dialog_no_button_height);
		final int windowWidth = (int) mActivity.getResources().getDimension(R.dimen.alert_dialog_no_button_width);
		mErrorNotifyWindow.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		mErrorNotifyWindow.setContentView(mSymbolNotifyView, new LayoutParams(windowWidth, windowHeight));
		mErrorNotifyWindow.show();
	}
	
	private void showCAWindow(String notifyString){
		if (mCaNotifyView == null || mCaNotifyText == null) {
			mCaNotifyView = mActivity.getLayoutInflater().inflate(R.layout.dvb_ca_notify_layout, null);
			mCaNotifyText = (TextView) mCaNotifyView.findViewById(R.id.notify_text);
		}
		mCaNotifyText.setText(notifyString);
		final int windowHeight = (int) mActivity.getResources().getDimension(R.dimen.alert_dialog_no_button_height);
		final int windowWidth = (int) mActivity.getResources().getDimension(R.dimen.alert_dialog_no_button_width);
		mErrorNotifyWindow.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		mErrorNotifyWindow.setContentView(mCaNotifyView, new LayoutParams(windowWidth, windowHeight));
		mErrorNotifyWindow.show();
		
	}
	
	private void removeAllNotify(){
		if (mErrorNotifyWindow != null && mErrorNotifyWindow.isShowing()) {
			mErrorNotifyWindow.dismiss();
		}
		mHandler.removeCallbacks(mHideNotifyTmporarily);
		log.D("removeAllNotify.!");
	}

	private void refreshDVBNotify(int type, boolean channel, boolean tuner, boolean ca) {
		removeAllNotify();
		if (channel && tuner && ca) {
			log.D( "DVBErrorNotify. dismiss ErrorWindow");
		} else {
			if (mErrorNotifyWindow == null) {
				mErrorNotifyWindow = new Dialog(mActivity, R.style.dvbErrorDialogTheme);
			}
			if (!channel) {
				showNoChannelDialog(type);
			} else if (!tuner) {
				showSymbolWindow();
			} else if (!ca) {
				String notifyStr = getCaNotifyString(mViewController.getLastCAParam());
				showCAWindow(notifyStr);
			}

			log.D( "DVBErrorNotify. show ErrorWindow");
		}
	}
	
	public void hideNotifyViewTemporarily(long hideNotifyViewTemporarily){
		if (mErrorNotifyWindow != null && mErrorNotifyWindow.isShowing()) {
			mErrorNotifyWindow.dismiss();
			mHandler.postDelayed(mHideNotifyTmporarily, hideNotifyViewTemporarily);
		}
	}
	
	Runnable mHideNotifyTmporarily = new Runnable() {
		public void run() {
			try {
				if (mErrorNotifyWindow != null && !mErrorNotifyWindow.isShowing()) {
					mErrorNotifyWindow.show();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	
    public String getCaNotifyString(int notify){
        
        switch(notify){
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_BADCARD_TYPE:
            
            return mActivity.getString(R.string.ca_message_badcard_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_BLACKOUT_TYPE:
            
            return mActivity.getString(R.string.ca_message_blackout_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_CALLBACK_TYPE:
            
            return mActivity.getString(R.string.ca_message_callback_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_CANCEL_TYPE:
            
            return mActivity.getString(R.string.ca_message_cancel_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_DECRYPTFAIL_TYPE:
            
            return mActivity.getString(R.string.ca_message_decryptfail_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_ERRCARD_TYPE:
            
            return mActivity.getString(R.string.ca_message_errcard_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_ERRREGION_TYPE:
            
            return mActivity.getString(R.string.ca_message_errregion_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_EXPICARD_TYPE:
            
            return mActivity.getString(R.string.ca_message_expicard_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_FREEZE_TYPE:
            
            return mActivity.getString(R.string.ca_message_freeze_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_INSERTCARD_TYPE:
            
            return mActivity.getString(R.string.ca_message_insertcard_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_LOWCARDVER_TYPE:
            
            return mActivity.getString(R.string.ca_message_lowcardver_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_MAXRESTART_TYPE:
            
            return mActivity.getString(R.string.ca_message_maxrestart_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_NEEDFEED_TYPE:
            
            return mActivity.getString(R.string.ca_message_needfeed_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_NOENTITLE_TYPE:
            
            return mActivity.getString(R.string.ca_message_noentitle_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_NOMONEY_TYPE:
            
            return mActivity.getString(R.string.ca_message_nomoney_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_NOOPER_TYPE:
            
            return mActivity.getString(R.string.ca_message_nooper_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_OUTWORKTIME_TYPE:
            
            return mActivity.getString(R.string.ca_message_outworktime_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_PAIRING_TYPE:
            
            return mActivity.getString(R.string.ca_message_pairing_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_STBFREEZE_TYPE:
            
            return mActivity.getString(R.string.ca_message_stbfreeze_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_STBLOCKED_TYPE:
            
            return mActivity.getString(R.string.ca_message_stblocked_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_UPDATE_TYPE:
            
            return mActivity.getString(R.string.ca_message_update_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_VIEWLOCK_TYPE:
            
            return mActivity.getString(R.string.ca_message_viewlock_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_WATCHLEVEL_TYPE:
            
            return mActivity.getString(R.string.ca_message_watchlevel_type);
        }
        
        return null;
    }
    private static final int TEMP_SHOW_TIME = 2700;
	private Object[] objs;
	@Override
	public void processMessage(Object sender,DvbMessage msg) {
		mViewController = (ViewController) sender;
		switch (msg.what) {
		case ViewMessage.STOP_PLAY: 
			removeAllNotify();
			break;
		case ViewMessage.DVB_INIT_FAILED: 
			ToastUtil.showToast(mActivity.getApplicationContext(), R.string.dvb_init_failed);
			mHandler.postDelayed(new Runnable() {
				public void run() {
					ToastUtil.showToast(mActivity.getApplicationContext(), R.string.dvb_init_failed);
					mActivity.finish();
				}
			},2000);
			break;
		case ViewMessage.ERROR_WITHOUT_CHANNEL: 
			hideNotifyViewTemporarily(TEMP_SHOW_TIME);
			ToastUtil.showToast(mActivity, R.string.dvb_without_this_channel);
			break;
		case ViewMessage.RECEIVED_ERROR_NOTIFY:
			objs = (Object[]) msg.obj;
			refreshDVBNotify((Integer)objs[0],(Boolean) objs[1], (Boolean)objs[2], (Boolean)objs[3]);
			break;
		case ViewMessage.SHOW_LIVE_GUIDE:
		case ViewMessage.SHOW_PROGRAM_GUIDE:
			removeAllNotify();
			break;
		case ViewMessage.EXIT_LIVE_GUIDE:
		case ViewMessage.EXIT_PROGRAM_GUIDE:
			if(objs != null){
				refreshDVBNotify((Integer)objs[0],(Boolean) objs[1], (Boolean)objs[2], (Boolean)objs[3]);
			}
			break;
		}
	}
}
