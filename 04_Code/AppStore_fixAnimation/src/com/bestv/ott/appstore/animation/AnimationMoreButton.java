package com.bestv.ott.appstore.animation;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.FocusFinder;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

import com.bestv.ott.appstore.R;
import com.bestv.ott.appstore.common.Constants;
import com.bestv.ott.appstore.utils.AppLog;

public class AnimationMoreButton extends RelativeLayout {
	
	private FocusFinder mFocusF = FocusFinder.getInstance();
	private AnimationControl control = AnimationControl.getInstance();
	private ImageView mImageView1;
	private Context mContext;
	private AnimationDrawable mAnimation;
	private ImageView mBackImageView;// move picture need config 
	private TextView move_textView;//礼包移动显示view
	private Animation aimation_move_textView;
	
	public AnimationMoreButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public AnimationMoreButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public AnimationMoreButton(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context){
		this.mContext = context;
		this.mImageView1 = new ImageView(mContext);

		setClickable(true);      
		setFocusable(true); 
		this.setPadding(15, 15,15,15);
		RelativeLayout.LayoutParams imageParams1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
		imageParams1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		addView(mImageView1,imageParams1);
	}
	
	public void setPaddingNat(int left,int top,int ringht,int bottom){
		this.setPadding(left, top, ringht, bottom);
	}
	
	public void setBackImageView(ImageView imageview){
		this.mBackImageView = imageview;
	}
	
	public void setImage1Resource(int resId){
		mImageView1.setImageResource(resId);
	}
	
	public void startAnimation(){
		mAnimation = (AnimationDrawable) this.getBackground();
		mAnimation.setOneShot(false);
		mAnimation.start();
		
	}
	
	public void stopAnimation(){
		mAnimation.stop();
	}
	
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int keyCode = event.getKeyCode();
		int keyAction = event.getAction();
		if (keyCode == KeyEvent.KEYCODE_DPAD_UP && keyAction == KeyEvent.ACTION_DOWN) {

			View nextFocus = mFocusF.findNextFocus((ViewGroup) this.getParent().getParent(), this, View.FOCUS_UP);
			if (nextFocus == null)
				return true;
			selectUp(this, nextFocus, true);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && keyAction == KeyEvent.ACTION_DOWN) {

			View nextFocus = mFocusF.findNextFocus((ViewGroup) this.getParent().getParent(), this, View.FOCUS_DOWN);
			if (nextFocus == null)
				return true;
			selectDown(this, nextFocus, true);
			return true;
		}
		
		return super.dispatchKeyEvent(event);
	}
	
	public void selectUp(View lastFocus, View nextFocus, boolean animation) {
		if (lastFocus == null || nextFocus == null)
			return;
		synchronized (control) {
			control.transformAnimation(mBackImageView, lastFocus, nextFocus, mContext, animation, true);
		}
		nextFocus.requestFocus();
	}
	
	public void selectDown(View lastFocus, View nextFocus, boolean animation) {
		if (lastFocus == null || nextFocus == null)
			return;
		synchronized (control) {
			if(nextFocus instanceof EditText){
				control.transformAnimationForImage(mBackImageView,nextFocus , mContext,true,false);
			}else{
				control.transformAnimation(mBackImageView, lastFocus, nextFocus, mContext, animation, true);
			}
		}
		nextFocus.requestFocus();
	}
	
	
	public void startTextAnimi(String showText , Drawable showIcon){
		if(move_textView!=null){
			move_textView.setText(showText);
			move_textView.setCompoundDrawablesWithIntrinsicBounds(showIcon, null, null, null);
			move_textView.startAnimation(aimation_move_textView);
		}
	}
	public void stopTextAnimi(){
		if(aimation_move_textView!=null){
			aimation_move_textView.cancel();
		}
	}
	public void setTextView(TextView v){
		move_textView = v;
		move_textView.getPaint().setFakeBoldText(true);
		aimation_move_textView = AnimationUtils.loadAnimation(mContext, R.anim.spree_move);
	}
	
	protected void onFocusChanged(boolean gainFocus, int direction,
			Rect previouslyFocusedRect) {
		if(gainFocus){
			/* hasFocus --> 停止移动动画,开始放大动画 */
			if(aimation_move_textView!=null){
				aimation_move_textView.cancel();
			}
		}else{
			/* no Focus --> 停止放大动画 */
		}
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
	}
}
