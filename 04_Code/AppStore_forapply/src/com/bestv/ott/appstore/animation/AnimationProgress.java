package com.bestv.ott.appstore.animation;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.FocusFinder;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bestv.ott.appstore.R;

public class AnimationProgress extends LinearLayout {
	
	private FocusFinder mFocusF = FocusFinder.getInstance();
	private AnimationControl control = AnimationControl.getInstance();
	private ProgressBar mProgressBar;
	private TextView mTextView;
	private Context mContext;
	private AnimationDrawable mAnimation;
	private ImageView mBackImageView;// move picture need config 
	public AnimationProgress(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public AnimationProgress(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public AnimationProgress(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context){
		this.mContext = context;
		View view=LayoutInflater.from(mContext).inflate(R.layout.progressbar_layout, null);
		mProgressBar =(ProgressBar)view.findViewById(R.id.down_progress);
		mTextView=(TextView)view.findViewById(R.id.down_progress_text);
		// some config
		setClickable(true);      
		setFocusable(true); 
		this.setPadding(15, 15, 15,15);
		setOrientation(LinearLayout.VERTICAL); 
		setGravity(Gravity.CENTER);
		addView(view);
	}
	
	public void setBackImageView(ImageView imageview){
		this.mBackImageView = imageview;
	}
	
	
	public void setProgress(int progress){
		mProgressBar.setProgress(progress);
	}
	
	public void setText(String tex){
		mTextView.setText(tex);
	}

	
	public void startAnimation(){
		mAnimation = (AnimationDrawable) this.getBackground();
		mAnimation.setOneShot(false);
		mAnimation.start();
		
	}
	
	public void stopAnimation(){
		mAnimation.stop();
	}
	
//	
//	@Override
//	protected void onFocusChanged(boolean gainFocus, int direction,
//			Rect previouslyFocusedRect) {
//		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
//		if(gainFocus){
//			if(mBackImageView!=null){
//				synchronized (mBackImageView) {
//					AnimationControl control = AnimationControl.getInstance();
//					synchronized(control){
//						control.transformAnimation(mBackImageView, this, mContext,true,false);
//					}
//					
//					
//				}
//			}	
//		}else{
//		}
//		
//	}
	
	
	
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
	
}
