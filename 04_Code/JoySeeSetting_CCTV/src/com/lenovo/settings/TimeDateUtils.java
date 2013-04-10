package com.lenovo.settings;

import 	android.view.KeyEvent;
import android.widget.TextView;
import 	android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.widget.LinearLayout;
import android.widget.Button;
import android.util.Log;
import android.os.Message;
import android.os.Handler;
import java.util.ArrayList;
import 	android.content.Context;

public class TimeDateUtils {
	private int mLayoutID;
	private ArrayList<Integer> mTextViewID;
	private Handler mHandler;
	private Context mContext;
	
	public static final int KEY_DOWN = 100;
	public static final int KEY_UP = 101; 
	public static final int FOCUSED = 102;
	public static final int UNFOCUSED = 103;
	public static final int KEY_CENTER = 104;
	public static final int KEY_BACK = 105;
	public static final int KEY_LEFT = 106;
	public static final int KEY_RIGHT = 107; 
	
	public TimeDateUtils(Context context, int layoutID, ArrayList<Integer> textViewID, Handler handler) {
		mContext = context;
		mLayoutID = layoutID;
		mTextViewID = textViewID;
		mHandler = handler;
	}
	
	public View.OnFocusChangeListener getFocusChangeListener() {
		return mListener;
	}
	
	public View.OnKeyListener getKeyListener() {
		return mKeyListener;
	}
	
	private View.OnFocusChangeListener mListener = new View.OnFocusChangeListener() {
		public void onFocusChange(View v, boolean hasFocus) {
			if(mLayoutID == v.getId()) {
				if(hasFocus) {
					Message msg = mHandler.obtainMessage(FOCUSED);
					msg.obj = v.getId();
					mHandler.sendMessage(msg);		
				} else {
					Message msg = mHandler.obtainMessage(UNFOCUSED);
					msg.obj = v.getId();
					mHandler.sendMessage(msg);		
				}
			}	

		}
	};
	
	private View.OnKeyListener mKeyListener = new View.OnKeyListener() {
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if(event.getAction() != KeyEvent.ACTION_DOWN) {
				return false;
			}

			if(v.getId() == mLayoutID) {
				if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
					keyCode == KeyEvent.KEYCODE_ENTER) {
					Message msg = mHandler.obtainMessage(KEY_CENTER);
					msg.obj = v.getId();
					mHandler.sendMessage(msg);		
					return true; 
				}
			}	
			if(mTextViewID.contains(v.getId())) {
				if(keyCode == KeyEvent.KEYCODE_BACK) {
					Message msg = mHandler.obtainMessage(KEY_BACK);
					msg.obj = v.getId();
					mHandler.sendMessage(msg);		
					return true;
				} else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
					Message msg = mHandler.obtainMessage(KEY_DOWN);
					msg.obj = v.getId();
					mHandler.sendMessage(msg);		
					return true;
				} else if(keyCode == KeyEvent.KEYCODE_DPAD_UP) {
					Message msg = mHandler.obtainMessage(KEY_UP);
					msg.obj = v.getId();
					mHandler.sendMessage(msg);		
					return true;
				}
				else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
					Message msg = mHandler.obtainMessage(KEY_LEFT);
					msg.obj = v.getId();
					mHandler.sendMessage(msg);
					return true;
				}else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
					Message msg = mHandler.obtainMessage(KEY_RIGHT);
					msg.obj = v.getId();
					mHandler.sendMessage(msg);
					return true;
				}			
			}
			return false;
		}
	};	
	
}
