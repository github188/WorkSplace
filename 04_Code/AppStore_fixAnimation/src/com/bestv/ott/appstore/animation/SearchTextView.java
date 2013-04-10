package com.bestv.ott.appstore.animation;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.FocusFinder;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bestv.ott.appstore.R;

public class SearchTextView extends LinearLayout {
	
	private FocusFinder mFocusF = FocusFinder.getInstance();
	private AnimationControl control = AnimationControl.getInstance();
	private ImageView mImageView;
	private TextView mTextView;
	private Context mContext;
	private AnimationDrawable mAnimation;
	private ImageView mBackImageView;// move picture need config 
	public SearchTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public SearchTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SearchTextView(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context){
		this.mContext = context;
		this.mImageView = new ImageView(mContext);
		this.mTextView = new TextView(mContext);

		setClickable(true);      
		setFocusable(true); 
		setOrientation(LinearLayout.HORIZONTAL); 
		setGravity(Gravity.CENTER);
		mImageView.setImageResource(R.drawable.app_search);
		mTextView.setText(R.string.app_search);
		mTextView.setPadding(25, 0, 0, 0);
		mTextView.setTextSize(19);
		mTextView.setTextColor(Color.BLACK);
		addView(mImageView); 
		addView(mTextView); 
	}
	
	public boolean dispatchKeyEvent(KeyEvent event) {

		int keyCode = event.getKeyCode();
		int keyAction = event.getAction();
		
		if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && keyAction == KeyEvent.ACTION_DOWN) {
			View nextFocus = mFocusF.findNextFocus((ViewGroup) this.getParent().getParent(), this, View.FOCUS_DOWN);
			if (nextFocus == null)
				return true;
			selectDown(this, nextFocus, true);
			return true;
		}
		
		if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && keyAction == KeyEvent.ACTION_DOWN){//往右 焦点转到下载记录
			View nextFocus = mFocusF.findNextFocus((ViewGroup) this.getParent(), this, View.FOCUS_RIGHT);
			if (nextFocus == null){
				return true;
			}
			selectDown(this,nextFocus,true);
			return true;
		}
		
		if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT
				|| keyCode == KeyEvent.KEYCODE_DPAD_UP){
			return true;
		}
		
		return super.dispatchKeyEvent(event);
	}
	
	public void selectDown(View lastFocus, View nextFocus, boolean animation) {
		if (lastFocus == null || nextFocus == null)
			return;
		// mIsRepeating = true;
		synchronized (control) {
			control.transformAnimation(mBackImageView, lastFocus, nextFocus, mContext, animation, true);
		}
		nextFocus.requestFocus();
	}
	
	public void setBackImageView(ImageView imageview){
		this.mBackImageView = imageview;
	}
	
	public void setImageResource(int resId){
		mImageView.setImageResource(R.drawable.button_download);
	}
	
	public void setImageURI(Uri uri){
		mImageView.setImageURI(uri);
	}
	
	public void setText(String str){
		mTextView.setText(R.string.downloadmrg_button_txt);
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
//	
	
}
