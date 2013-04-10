package com.bestv.ott.appstore.animation;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.text.TextUtils.TruncateAt;
import android.text.method.SingleLineTransformationMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.FocusFinder;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

import com.bestv.ott.appstore.R;
import com.bestv.ott.appstore.common.Constants;
import com.bestv.ott.appstore.utils.AppLog;

public class AnimationTextButton extends RelativeLayout {
	private FocusFinder mFocusF = FocusFinder.getInstance();
	private AnimationControl control = AnimationControl.getInstance();
	
	private TextView mTextView;
	private Context mContext;
	private AnimationDrawable mAnimation;
	private ImageView mBackImageView;// move picture need config 
	private boolean flag=true;
	public AnimationTextButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public AnimationTextButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public AnimationTextButton(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context){
		this.mContext = context;
		this.mTextView = new TextView(mContext);
		// some config
		setClickable(true);      
		setFocusable(true); 
		setGravity(Gravity.CENTER);
		mTextView.setTextSize(22);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		mTextView.setSelected(true);
		mTextView.setSingleLine(true);
		mTextView.setEllipsize(TruncateAt.MARQUEE);
		mTextView.setTransformationMethod(SingleLineTransformationMethod.getInstance());
		mTextView.setLayoutParams(params);
		mTextView.setTextColor(getResources().getColor(R.color.white));
		mTextView.getPaint().setFakeBoldText(true);
		addView(mTextView); 
	}
	
	public void setBackImageViewff(){
		
	}
	
	public void setBackImageView(ImageView imageview){
		this.mBackImageView = imageview;
	}
	
	public ImageView getBackImageView(){
		return this.mBackImageView;
	}
	
	public void setImageResource(int resId){
		mTextView.setBackgroundResource(resId);
	}
	
	public void setText(String str){
		mTextView.setText(str);
	}
	
	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}
	
	public boolean dispatchKeyEvent(KeyEvent event) {
		int keyCode = event.getKeyCode();
		int keyAction = event.getAction();
		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && keyAction == KeyEvent.ACTION_DOWN)
			return true;
		if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && keyAction == KeyEvent.ACTION_DOWN) {

			View lastFocusView = this;
			View nextFocus = mFocusF.findNextFocus((ViewGroup)this.getRootView(), lastFocusView, View.FOCUS_RIGHT);
			
			if (lastFocusView == null)
				return true;
			
			execute(lastFocusView, nextFocus, true);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && keyAction == KeyEvent.ACTION_DOWN) {

			View lastFocusView = this;
			View nextFocus = mFocusF.findNextFocus((ViewGroup)this.getRootView(), lastFocusView, View.FOCUS_DOWN);
			
			if (lastFocusView == null)
				return true;
			
			execute(lastFocusView, nextFocus, true);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP && keyAction == KeyEvent.ACTION_DOWN) {

			View lastFocusView = this;
			View nextFocus = mFocusF.findNextFocus((ViewGroup)this.getRootView(), lastFocusView, View.FOCUS_UP);
			
			if (lastFocusView == null)
				return true;
			
			execute(lastFocusView, nextFocus, true);
			return true;
		}
		
		
		return super.dispatchKeyEvent(event);
	}
	
	public void execute(View lastFocus, View nextFocus, boolean animation) {
		if (lastFocus == null || nextFocus == null)
			return;
		synchronized (control) {
			control.transformAnimation(mBackImageView, lastFocus, nextFocus, mContext, animation, true);
		}
		nextFocus.requestFocus();
	}
	
//	@Override
//	protected void onFocusChanged(boolean gainFocus, int direction,
//			Rect previouslyFocusedRect) {
//		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
//		if(gainFocus){
//			if(mBackImageView!=null){
//				synchronized (mBackImageView) {
//					AnimationControl control = AnimationControl.getInstance();
//					synchronized(control){
//						if(flag){
//							control.transformAnimationForImage(mBackImageView, this, mContext,true,false);
//						}else{
//							control.transformAnimation(mBackImageView, this, mContext,true,false);
//						}
//					}
//					
//				}
//			}
//		}else{
//		}
//		
//	}
	
	
}
