package com.bestv.ott.appstore.animation;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
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
import android.widget.TextView;

import com.bestv.ott.appstore.R;
import com.bestv.ott.appstore.common.Constants;
import com.bestv.ott.appstore.utils.AppLog;

public class AnimationImageButton extends LinearLayout {
	
	
	private FocusFinder mFocusF = FocusFinder.getInstance();
	private AnimationControl control = AnimationControl.getInstance();
	private ImageView mImageView;
	private Context mContext;
	private AnimationDrawable mAnimation;
	private ImageView mBackImageView;// move picture need config 
	private boolean flag=true;
	public AnimationImageButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public AnimationImageButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public AnimationImageButton(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context){
		this.mContext = context;
		this.mImageView = new ImageView(mContext);
		// some config
		setClickable(true);      
		setFocusable(true); 
		this.setPadding(10, 15, 10, 15);
		setOrientation(LinearLayout.VERTICAL); 
		setGravity(Gravity.CENTER);
		addView(mImageView); 
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
		mImageView.setImageResource(resId);
	}
	
	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}
	
	public void setImageParams(){
		final int[]		Loc2s		= new int[2];//目的view 所在位置
		final float[]	    Dimen2s	= new float[2];//目的view 宽高
		mBackImageView.getLocationInWindow(Loc2s);
		Dimen2s[0]= mImageView.getWidth();
		Dimen2s[1]= mImageView.getHeight();
		MarginLayoutParams lp = (MarginLayoutParams)getLayoutParams();
		lp.width = (int) Dimen2s[0]*11/10+10;
		lp.height = (int) Dimen2s[1]*6/5+12;
		lp.leftMargin = Loc2s[0]-(int)Dimen2s[0]/20-5;
		lp.topMargin = Loc2s[1]-(int)Dimen2s[1]/10-7;
		AppLog.d("com.bestv.ott.appstore.AnimationImageButton", "====lp.width="+lp.width+"=====lp.height="+lp.height);
		mBackImageView.setLayoutParams(lp);
	}

	
	public boolean dispatchKeyEvent(KeyEvent event) {
		int keyCode = event.getKeyCode();
		int keyAction = event.getAction();
		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && keyAction == KeyEvent.ACTION_DOWN){
			View lastFocusView = this;
			View nextFocus = mFocusF.findNextFocus((ViewGroup)this.getRootView(), lastFocusView, View.FOCUS_LEFT);
			
			if (lastFocusView == null)
				return true;
			
			execute(lastFocusView, nextFocus, true);
			return true;
		}
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
