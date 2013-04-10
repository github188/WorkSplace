package com.bestv.ott.appstore.animation;

import com.bestv.ott.appstore.utils.AppLog;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class SearchResultLayout extends LinearLayout {
	
	public static final String TAG = "com.joysee.appstore.AppSearchLayout";

	private FocusFinder mFocusF = FocusFinder.getInstance();
	private AnimationControl control = AnimationControl.getInstance();
	private Context mContext;

	private ImageView mBackImageView;

	private boolean mIsRepeating;
	private int mInterval = 30;
	private int mCurrentDirection;
	private EditText editText;

	private OnSlidingAtEndListener onSlidingAtEndListener;

	private static final int SCROLL_LEFT = 0;
	private static final int SCROLL_RIGHT = 1;
	private static final int SCROLL_UP = 2;
	private static final int SCROLL_DOWN = 3;
	
	private ViewGroup mFocusGroup;

	public SearchResultLayout(Context context) {
		super(context);
		this.mContext = context;
	}

	public SearchResultLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
	}

	public SearchResultLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int keyCode = event.getKeyCode();
		int keyAction = event.getAction();
		if(null==editText){
			return false;
		}
		int len =editText.getText().length();
		int startIndex = editText.getSelectionStart();//输入光标位置
		AppLog.d(TAG, "---------len : "+len +"  -----startIndex : "+startIndex+"-------------");
		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && keyAction == KeyEvent.ACTION_DOWN) {
			AppLog.d(TAG, "************left****");
			if(editText.hasFocus() && startIndex>0){
				editText.setSelection(startIndex-1);
			}else if(!editText.hasFocus()){
				editText.setSelection(len);
				View lastFocusView = getFocusedChild();
				View nextFocus = mFocusF.findNextFocus((ViewGroup)this.getParent(), getFocusedChild(), View.FOCUS_LEFT);
				AppLog.d(TAG, "lastFocusView   = " + lastFocusView);
				AppLog.d(TAG, "nextFocus       = " + nextFocus);
				if(nextFocus == null) {
					if (onSlidingAtEndListener != null) {
						boolean result = onSlidingAtEndListener.onSlidingAtLeft(SearchResultLayout.this, getFocusedChild(),true);
						if (result){
							AppLog.d(TAG, "-----onSlidingAtEnd return true");
							mIsRepeating = false;
							return true;
						}
					}
				}
				if (lastFocusView == null)
					return true;
				mCurrentDirection = SCROLL_LEFT;
				execute(lastFocusView, nextFocus, true);
			}
			return true;
			
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && keyAction == KeyEvent.ACTION_DOWN) {
			AppLog.d(TAG, "************right****");
			if(startIndex<len){
				editText.setSelection(startIndex+1);
			}else if(startIndex == len){
				View lastFocusView = getFocusedChild();
				View nextFocus = mFocusF.findNextFocus((ViewGroup)this.getParent(), getFocusedChild(), View.FOCUS_RIGHT);
				AppLog.d(TAG, "lastFocusView   = " + lastFocusView);
				AppLog.d(TAG, "nextFocus       = " + nextFocus);
				if(nextFocus == null) {
					if (onSlidingAtEndListener != null) {
						boolean result = onSlidingAtEndListener.onSlidingAtRight(SearchResultLayout.this, getFocusedChild(),true);
						if (result){
							AppLog.d(TAG, "-----onSlidingAtEnd return true");
							mIsRepeating = false;
							return true;
						}
					}
				}
				if (lastFocusView == null)
					return true;
				mCurrentDirection = SCROLL_RIGHT;
				execute(lastFocusView, nextFocus, true);
			}
			return true;
			
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP && keyAction == KeyEvent.ACTION_DOWN) {
			if(EditTextButton.isH){ 
				InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE); 
		        imm.hideSoftInputFromWindow(editText.getWindowToken(),0);
		        EditTextButton.isH = false;
				return true;
			}
			mCurrentDirection = SCROLL_UP;
			View lastFocusView = getFocusedChild();
			View nextFocus = mFocusF.findNextFocus(mFocusGroup != null ? mFocusGroup : (ViewGroup) this.getParent(), getFocusedChild(), View.FOCUS_UP);
			AppLog.d(TAG, "lastFocusView   = " + lastFocusView);
			AppLog.d(TAG, "nextFocus       = " + nextFocus);
			if (lastFocusView == null || nextFocus == null)
				return true;
			execute(lastFocusView, nextFocus, true);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && keyAction == KeyEvent.ACTION_DOWN) {
			if(EditTextButton.isH){
				InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE); 
		        imm.hideSoftInputFromWindow(editText.getWindowToken(),0);
		        EditTextButton.isH = false;
				return true;
			}
			mCurrentDirection = SCROLL_DOWN;
			View lastFocusView = getFocusedChild();
			View nextFocus = mFocusF.findNextFocus(mFocusGroup != null ? mFocusGroup : (ViewGroup) this.getParent(), getFocusedChild(), View.FOCUS_DOWN);
			AppLog.d(TAG, "lastFocusView   = " + lastFocusView);
			AppLog.d(TAG, "nextFocus       = " + nextFocus);
			if (lastFocusView == null || nextFocus == null)
				return true;
			execute(lastFocusView, nextFocus, true);
			return true;
		}

		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && keyAction == KeyEvent.ACTION_UP) {
			mIsRepeating = false;
			AppLog.d(TAG, "mIsReprating   is  " + mIsRepeating);
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && keyAction == KeyEvent.ACTION_UP) {
			mIsRepeating = false;
			AppLog.d(TAG, "mIsReprating   is  " + mIsRepeating);
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP && keyAction == KeyEvent.ACTION_UP) {
			mIsRepeating = false;
			AppLog.d(TAG, "mIsReprating   is  " + mIsRepeating);
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && keyAction == KeyEvent.ACTION_UP) {
			mIsRepeating = false;
			AppLog.d(TAG, "mIsReprating   is  " + mIsRepeating);
		}

		return super.dispatchKeyEvent(event);
	}

	public void execute(View lastFocus, View nextFocus, boolean animation) {
		if (lastFocus == null || nextFocus == null)
			return;
		mIsRepeating = true;
		synchronized (control) {
			if(nextFocus instanceof EditText){
				if(lastFocus instanceof ViewGroup){
					ViewGroup viewGroup = (ViewGroup)lastFocus;
					View focusChild = viewGroup.getChildAt(0);
					
					if (focusChild != null) {
						if (AnimationButton.class.isInstance(focusChild)) {
							AppLog.d(TAG, "AnimationButton    end");
							AnimationButton button = (AnimationButton) focusChild;
							button.stopAnimation();
						}
						if (SearchAnimationButton.class.isInstance(focusChild)) {
							AppLog.d(TAG, "SearchAnimationButton    end");
							SearchAnimationButton buttonSear = (SearchAnimationButton) focusChild;
							buttonSear.stopAnimation();
						}
					}
				}
				control.transformAnimationForImage(mBackImageView,nextFocus , mContext,true,false);
			}else{
				control.transformAnimation(mBackImageView, lastFocus, nextFocus, mContext, animation, true);
			}
		}
		nextFocus.requestFocus();
	}


	public void setBackImageView(ImageView mBackImageView) {
		this.mBackImageView = mBackImageView;
	}

	
	public void setOnSlidingAtEndListener(OnSlidingAtEndListener l) {
		this.onSlidingAtEndListener = l;
	}

	public interface OnSlidingAtEndListener {
		boolean onSlidingAtLeft(SearchResultLayout layout, View view,boolean fastMode);
		boolean onSlidingAtRight(SearchResultLayout layout, View view,boolean fastmode);
	}

	public void setFocusGroup(ViewGroup view) {
		this.mFocusGroup = view;
	}
	
	public void setEditText(EditText editText){
		this.editText = editText;
	}
}
