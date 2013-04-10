package com.joysee.adtv.ui;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DvbMessage;
import com.joysee.adtv.common.DefaultParameter.ViewMessage;
import com.joysee.adtv.controller.ViewController;
import com.joysee.adtv.logic.bean.DvbService;

public class ChannelNumberView extends LinearLayout implements IDvbBaseView {

	private TextView number;
	private static final int HIDE = 2;
	private static final int SHOW = 3;
	private static final int SHOW_TIME =5000;
	private Handler mHandler = new Handler(Looper.getMainLooper()) {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HIDE:
				setVisibility(View.INVISIBLE);
				break;
			case SHOW:
				setVisibility(View.VISIBLE);
				removeMessages(HIDE);
				sendEmptyMessageDelayed(HIDE, SHOW_TIME);
				break;
			}
			super.handleMessage(msg);
		}
	};
	private ViewController viewController;
	public ChannelNumberView(Context context) {
		this(context, null);
	}

	public ChannelNumberView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ChannelNumberView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		number = (TextView) findViewById(R.id.channel_num_tv);
		setmHandler(mHandler);
	}

	@Override
	public void processMessage(Object sender,DvbMessage msg) {
		if(viewController==null){
			viewController = (ViewController) sender;
		}
		switch (msg.what) {
		case ViewMessage.RECEIVED_CHANNEL_INFO_KEY:
			getmHandler().removeMessages(HIDE);
			getmHandler().sendEmptyMessageDelayed(HIDE, SHOW_TIME);
			if(getVisibility() == View.VISIBLE){
				return;
			}
			DvbService service = viewController.getCurrentChannel();
			number.setText(service.getLogicChNumber()+"");
			setVisibility(View.VISIBLE);
			break;
		case ViewMessage.SWITCH_CHANNEL:
		case ViewMessage.RECEIVED_NUMBER_KEY:
			number.setText(msg.arg1+"");
			setVisibility(View.VISIBLE);
			getmHandler().removeMessages(HIDE);
			getmHandler().sendEmptyMessageDelayed(HIDE, SHOW_TIME);
			break;
		case ViewMessage.SWITCH_CHANNEL_UPDOWN:
			if(msg.arg1 == ViewMessage.KEYCODE_ACTION_UP)
				break;
			if(msg.arg2>=0)
				number.setText(String.valueOf(msg.arg2));
			setVisibility(View.VISIBLE);
			getmHandler().removeMessages(HIDE);
			getmHandler().sendEmptyMessageDelayed(HIDE, SHOW_TIME);
			break;
		}
	}

	public Handler getmHandler() {
		return mHandler;
	}
	
	//通过get set可以防止弱引用导致的内存泄露
	public void setmHandler(Handler mHandler) {
		this.mHandler = mHandler;
	}
}
