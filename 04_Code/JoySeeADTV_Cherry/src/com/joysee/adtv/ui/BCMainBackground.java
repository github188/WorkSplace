package com.joysee.adtv.ui;

import android.view.View;
import android.widget.FrameLayout;

import com.joysee.adtv.common.DefaultParameter.ViewMessage;
import com.joysee.adtv.common.DvbMessage;

public class BCMainBackground implements IDvbBaseView {
	private FrameLayout mBCMainLayout;
	public BCMainBackground(FrameLayout bCMainLayout){
		mBCMainLayout = bCMainLayout;
	}
	@Override
	public void processMessage(Object sender,DvbMessage msg) {
		switch (msg.what) {
		case ViewMessage.START_PLAY_BC:
			mBCMainLayout.setVisibility(View.VISIBLE);
			break;
		case ViewMessage.START_PLAY_TV:
			mBCMainLayout.setVisibility(View.INVISIBLE);
			break;
		}
	}

}
