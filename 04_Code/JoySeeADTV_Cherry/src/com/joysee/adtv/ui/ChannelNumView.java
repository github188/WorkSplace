package com.joysee.adtv.ui;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DvbMessage;
import com.joysee.adtv.common.DefaultParameter.ServiceType;
import com.joysee.adtv.common.DefaultParameter.ViewMessage;
import com.joysee.adtv.controller.ViewController;
import com.joysee.adtv.logic.bean.DvbService;

public class ChannelNumView extends LinearLayout implements IDvbBaseView {


	private ImageView mChannelNum1;
	private ImageView mChannelNum2;
	private ImageView mChannelNum3;
	private Drawable[] mChannelNumDrawables = { null, null, null };
	private int mChannelSize = 3;

	private boolean isUserinputing = false;

	private static final int HANDLE_SWITCH_CHANNEL = 1;
	private static final int HIDE = 2;
	private static final int SHOW_TIMEOUT = 3000;
	private static final int SHOW_TIME =5000;
	public Handler mHandler = new Handler(Looper.getMainLooper()) {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HIDE:
				setVisibility(View.INVISIBLE);
				break;
			}
			super.handleMessage(msg);
		}
	};
	public ChannelNumView(Context context) {
		this(context, null);
	}

	public ChannelNumView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ChannelNumView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
//		mChannelNum1 = (ImageView) findViewById(R.id.dvb_channel_number_1);
//		mChannelNum2 = (ImageView) findViewById(R.id.dvb_channel_number_2);
//		mChannelNum3 = (ImageView) findViewById(R.id.dvb_channel_number_3);
		mChannelNumDrawables[0] = mChannelNum1.getDrawable();
		mChannelNumDrawables[1] = mChannelNum2.getDrawable();
		mChannelNumDrawables[2] = mChannelNum3.getDrawable();
	}

	public boolean isUserinputing(){
		return isUserinputing;
	}
	

	public void setChannelNum(int num) {
		for (int i = mChannelSize - 1; i >= 0; i--) {
			mChannelNumDrawables[i].setLevel(num % 10);
			num = num / 10;
		}
	}
	
	public void switchNow() {
		if (isUserinputing) {
			mHandler.removeMessages(HANDLE_SWITCH_CHANNEL);
			mHandler.sendEmptyMessage(HANDLE_SWITCH_CHANNEL);
		}
	}

	@Override
	public void processMessage(Object sender,DvbMessage msg) {
		switch (msg.what) {
		case ViewMessage.RECEIVED_CHANNEL_INFO_KEY:
		case ViewMessage.SWITCH_CHANNEL:
			ViewController viewController = (ViewController) sender;
			DvbService service = viewController.getCurrentChannel();
			setChannelNum(service.getLogicChNumber());
			Log.d("position", " logicNum = "+service.getLogicChNumber());
			setVisibility(View.VISIBLE);
			mHandler.removeMessages(HIDE);
			if((service.getServiceType()&0x0F) == ServiceType.TV){
				mHandler.sendEmptyMessageDelayed(HIDE, SHOW_TIME);
			}
			break;
		case ViewMessage.RECEIVED_NUMBER_KEY:
			setChannelNum(msg.arg1);
			setVisibility(View.VISIBLE);
			mHandler.removeMessages(HIDE);
			mHandler.sendEmptyMessageDelayed(HIDE, SHOW_TIMEOUT);
			break;
		case ViewMessage.SWITCH_PLAY_MODE:
	    case ViewMessage.STOP_PLAY:
			mHandler.removeMessages(HIDE);
			mHandler.sendEmptyMessage(HIDE);
			break;
		}
	}
}
