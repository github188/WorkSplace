package com.joysee.adtv.ui;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DvbMessage;
import com.joysee.adtv.common.DefaultParameter.ViewMessage;

import android.app.Activity;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * VAI VolumeAudioIndex 
 * 声道和伴音设置通知
 * @author wgh
 *
 */
public class VAINotifyWindow extends PopupWindow implements IDvbBaseView {
	private Activity mActivity;
	public View mVAINotifyView;
	public TextView mVAINotifyText;
	
	public VAINotifyWindow(Activity mActivity) {
		this.mActivity = mActivity;
	}
	private void init() {
		if(mVAINotifyView==null){
			mVAINotifyView = mActivity.getLayoutInflater().inflate(R.layout.dvb_volumechannel_audioindex_notify, null);
			mVAINotifyText = (TextView) mVAINotifyView.findViewById(R.id.volume_channel_pop_textview);
			setContentView(mVAINotifyView);
			setWidth((int)mActivity.getResources().getDimension(R.dimen.vai_notify_window_width));
			setHeight((int)mActivity.getResources().getDimension(R.dimen.vai_notify_window_height));
			setFocusable(false);
		}
	}

	private static final int HIDE = 1;
	private static final int SHOWTIME= 5000;
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(isShowing())
				dismiss();
		};
	};

	@Override
	public void processMessage(Object sender,DvbMessage msg) {
		switch (msg.what) {
		case ViewMessage.FINISHED_SOUNDTRACK_AUDIOINDEX_SET:
			init();
			mVAINotifyText.setText((String)msg.obj);
			showAtLocation(mActivity.getWindow().getDecorView(), Gravity.LEFT | Gravity.TOP,
					100, 100);
			mHandler.removeMessages(HIDE);
			mHandler.sendEmptyMessageDelayed(HIDE, SHOWTIME);
			break;
		case ViewMessage.SWITCH_PLAY_MODE:
		case ViewMessage.STOP_PLAY:
			mHandler.removeMessages(HIDE);
			mHandler.sendEmptyMessage(HIDE);
			break;
		}

	}
	

}
