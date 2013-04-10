package com.joysee.adtv.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.activity.SearchMainActivity;
import com.joysee.adtv.common.DefaultParameter;
import com.joysee.adtv.common.DefaultParameter.EmailStatus;
import com.joysee.adtv.common.DefaultParameter.ViewMessage;
import com.joysee.adtv.common.DefaultParameter.OsdShowType;
import com.joysee.adtv.common.DvbLog;
import com.joysee.adtv.common.DvbMessage;
import com.joysee.adtv.common.ToastUtil;

public class OsdPopupWindow implements IDvbBaseView {
	private DvbLog log = new DvbLog("OsdPopupWindow", DvbLog.DebugType.D);
	private Context mContext;
	private Activity mActivity;
    /* 显示OSD和Email */
	private PopupWindow mOsdPopWindowTop,mOsdPopWindowBottom;
	private AutoScrollTextView mOsdPopWindowViewTop,mOsdPopWindowViewBottom;
	private ImageView mEmailIcon;
	private int mCurrentEmailIconStatus = EmailStatus.EMAIL_HIDE;
	
	private PopupWindow mGotoSearchNotifyWindow;
	
	/* 指纹 */
	private TextView mFingerInfoTv = null;
	
	public OsdPopupWindow(Activity activity){
		mContext = activity.getApplicationContext();
		mActivity = activity;
	}
	public void setEmailcon(ImageView emailIcom){
		mEmailIcon = emailIcom;
	}
	public void setFingerInfoTv(TextView fingerInfoTv){
		mFingerInfoTv = fingerInfoTv;
	}
	
	/**
	 * 显示OSD信息View
	 * @param osdMsg    OSD信息
	 * @param showType OSD的显示方式 0x01: 显示在屏幕上方 0x02：显示在屏幕下方 0x03：整屏显示 0x04：半屏显示
	 */
    private void showOsdView(String osdMsg, int showType) {
        switch (showType) {
        case OsdShowType.OSD_SHOW_BOTTOM_FULL:
            if (null == mOsdPopWindowViewBottom) {
                mOsdPopWindowViewBottom = new AutoScrollTextView(mContext);
                mOsdPopWindowBottom = new PopupWindow(mOsdPopWindowViewBottom);
            }
            mOsdPopWindowBottom.setWidth((int) mContext.getResources().getDimension(R.dimen.osd_popupwindow_width));
            mOsdPopWindowBottom.setHeight((int) mContext.getResources().getDimension(R.dimen.osd_popupwindow_height));
            mOsdPopWindowViewBottom.setText(osdMsg);
            mOsdPopWindowViewBottom.invalidate();
            mOsdPopWindowViewBottom.init(mActivity.getWindowManager());
            mOsdPopWindowBottom.showAtLocation(mActivity.getWindow().getDecorView(),
                    Gravity.BOTTOM | Gravity.RIGHT, 0, 10);
            mOsdPopWindowViewBottom.startScroll();
            break;
        case OsdShowType.OSD_SHOW_TOP_FULL:
            if (null == mOsdPopWindowViewTop) {
                mOsdPopWindowViewTop = new AutoScrollTextView(
                        mContext);
                mOsdPopWindowTop = new PopupWindow(mOsdPopWindowViewTop);
            }
            mOsdPopWindowTop.setWidth((int) mContext.getResources().getDimension(R.dimen.osd_popupwindow_width));
            mOsdPopWindowTop.setHeight((int) mContext.getResources().getDimension(R.dimen.osd_popupwindow_height));
            mOsdPopWindowViewTop.setText(osdMsg);
            mOsdPopWindowViewTop.invalidate();
            mOsdPopWindowViewTop.init(mActivity.getWindowManager());
            mOsdPopWindowTop.showAtLocation(mActivity.getWindow().getDecorView(),
                    Gravity.TOP | Gravity.RIGHT, 0, 10);
            mOsdPopWindowViewTop.startScroll();
            break;
        }
    }
    
    /**
  	 * 隐藏OSD信息View
  	 */
    private void hideOsdView(int type) {
          switch (type) {
          case OsdShowType.OSD_SHOW_BOTTOM_FULL:
              if (null != mOsdPopWindowBottom) {
                  mOsdPopWindowViewBottom.stopScroll();
                  mOsdPopWindowBottom.dismiss();
              } else {
                  log.E(" ------  HideOsd error !!! mOsdPopWindowBottom = "
                          + mOsdPopWindowBottom);
              }
              break;
          case OsdShowType.OSD_SHOW_TOP_FULL:
              if (null != mOsdPopWindowTop) {
                  mOsdPopWindowViewTop.stopScroll();
                  mOsdPopWindowTop.dismiss();
              } else {
                  log.E(" ------  HideOsd error !!! mOsdPopWindowTop = "
                          + mOsdPopWindowTop);
              }
              break;
          }
      }
      
    private void hideEmail() {
  		if (mEmailIcon != null) {
  			mEmailIcon.setVisibility(View.GONE);
  			mCurrentEmailIconStatus = EmailStatus.EMAIL_HIDE;
  		}
  	}
    private void showEmail() {
  		if (mEmailIcon != null) {
  			mEmailIcon.setVisibility(View.VISIBLE);
  			mCurrentEmailIconStatus = EmailStatus.EMAIL_SHOW;
  		}
  	}
      /**
  	 * 邮件图标闪烁
  	 */
    private void showTimerEmailIcon() {
  		mHandler.postDelayed(mSwitchEIconRunnable, 1000);
  	}
  	// E Email
  	private Runnable mSwitchEIconRunnable = new Runnable() {
  		public void run() {
  			if (mCurrentEmailIconStatus == EmailStatus.EMAIL_HIDE) {
  				showEmail();
  			} else {
  				hideEmail();
  			}
  			mHandler.postDelayed(this, 1000);
  		}
  	};
  	
	 /**显示指纹信息*/
  	private void showFingerInfo(int cardid,int ecmpid){
        log.D( " showFingerInfo  cardid = " + cardid + " ecmpid = " + ecmpid);
        if (cardid == 0) {
            mFingerInfoTv.setVisibility(View.INVISIBLE);
        }else{
            mFingerInfoTv.setText(""+cardid);
            mFingerInfoTv.setVisibility(View.VISIBLE);
        }
    }
    
  	private static final int HIDE_GOTO_SEACH_NOTIFY = 2;
  	private static final int GOTO_SEARCH = 3;
  	private Handler mHandler = new Handler(){
  		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HIDE_GOTO_SEACH_NOTIFY:
				if (mGotoSearchNotifyWindow != null && mGotoSearchNotifyWindow.isShowing())
					mGotoSearchNotifyWindow.dismiss();
				break;
			case GOTO_SEARCH:
				if (mGotoSearchNotifyWindow != null && mGotoSearchNotifyWindow.isShowing())
					mGotoSearchNotifyWindow.dismiss();
				Intent tIntent = new Intent(mContext, SearchMainActivity.class);
				tIntent.putExtra(DefaultParameter.TVNOTIFY_TO_SEARCH, true);
				mContext.startActivity(tIntent);
				break;
			}
		};
  	};
  	
  	@Override
  	public void processMessage(Object sender,DvbMessage msg) {
  		switch (msg.what) {
			case ViewMessage.RECEIVED_OSD_INFO_SHOW: 
				String osdMsg = (String)msg.obj;
				int type =  msg.arg1;
				showOsdView(osdMsg,type);
				break;
			case ViewMessage.RECEIVED_OSD_INFO_HIDE:
				int osdType = msg.arg1;
				hideOsdView(osdType);
				break;
			case ViewMessage.RECEIVED_EMAIL_SHOW:  		
				showEmail();
				break;
			case ViewMessage.RECEIVED_EMAIL_HIDE: 			
				hideEmail();
				break;
			case ViewMessage.RECEIVED_EMAIL_BLINK: 
				showTimerEmailIcon();
				break;
			case ViewMessage.RECEIVED_FINGER_INFO_SHOW: 
				int cardid = msg.arg1;
				int emcpid = msg.arg2;
				showFingerInfo(cardid, emcpid);
				break;
			case ViewMessage.STOP_PLAY: 
				hideOsdView(OsdShowType.OSD_SHOW_BOTTOM_FULL);
				hideOsdView(OsdShowType.OSD_SHOW_TOP_FULL);
				mHandler.removeCallbacks(mSwitchEIconRunnable);
				mHandler.removeMessages(HIDE_GOTO_SEACH_NOTIFY);
				mHandler.removeMessages(GOTO_SEARCH);
				break;
			case ViewMessage.RECEIVED_UPDATE_PROGRAM_NB_CHANGE: 
				if (mGotoSearchNotifyWindow == null)
					mGotoSearchNotifyWindow = ToastUtil.showPopToast(mContext,
							mContext.getResources().getString(R.string.dvb_update_program));
				mGotoSearchNotifyWindow.showAtLocation(mActivity.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
				mHandler.sendEmptyMessageDelayed(HIDE_GOTO_SEACH_NOTIFY, 3000);
				mHandler.sendEmptyMessageDelayed(GOTO_SEARCH, 5000);
				break;
		}
  	}
}
