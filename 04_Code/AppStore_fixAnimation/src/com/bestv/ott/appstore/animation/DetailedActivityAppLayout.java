package com.bestv.ott.appstore.animation;

import com.bestv.ott.appstore.utils.AppLog;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;

public class DetailedActivityAppLayout extends GridLayout {

	public static final String TAG = "com.joysee.appstore.DetailedActivityAppLayout";
	private FocusFinder mFocusF = FocusFinder.getInstance();
	private AnimationControl control = AnimationControl.getInstance();
	private Context mContext;

	private ImageView mBackImageView;

//	private boolean mIsRepeating;
//	private int mInterval = 30;
//	private int mCurrentDirection;

	private OnSlidingAtEndListener onSlidingAtEndListener;

//	private static final int SCROLL_LEFT = 0;
//	private static final int SCROLL_RIGHT = 1;
//	private static final int SCROLL_UP = 2;
//	private static final int SCROLL_DOWN = 3;
	
//	private ViewGroup mFocusGroup;

	public DetailedActivityAppLayout(Context context) {
		super(context);
		this.mContext = context;
	}

	public DetailedActivityAppLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
	}

	public DetailedActivityAppLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int keyCode = event.getKeyCode();
		int keyAction = event.getAction();
//		AppLog.log_D(TAG, "DetailedActivityAppLayout:dispatchKeyEvent()  begin   mIsRepeating= " + mIsRepeating);

//		if (mIsRepeating && keyAction == KeyEvent.ACTION_DOWN)
//			if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
//				return true;

		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && keyAction == KeyEvent.ACTION_DOWN) {

			View lastFocusView = getFocusedChild();
			View nextFocus = mFocusF.findNextFocus((ViewGroup)this.getParent().getParent().getParent(), getFocusedChild(), View.FOCUS_LEFT);
			
			
			
			if (lastFocusView == null)
				return true;

//			postDelayed(mRepeater, 300);
//			mCurrentDirection = SCROLL_LEFT;
			
			
			if(lastFocusView instanceof ViewGroup){
				ViewGroup viewGroup = (ViewGroup)lastFocusView;
				View lastFocus = viewGroup.getChildAt(0);
				
				if (lastFocus != null) {
					if (AnimationButton.class.isInstance(lastFocus)) {
						AnimationButton button = (AnimationButton) lastFocus;
						button.stopAnimation();
					}
					if (SearchAnimationButton.class.isInstance(lastFocus)) {
						SearchAnimationButton buttonSear = (SearchAnimationButton) lastFocus;
						buttonSear.stopAnimation();
					}
				}
			}
			
			selectLeft(lastFocusView, nextFocus, true);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && keyAction == KeyEvent.ACTION_DOWN) {

			View lastFocusView = getFocusedChild();
			View nextFocus = mFocusF.findNextFocus(this, getFocusedChild(), View.FOCUS_RIGHT);
			
			
			if (lastFocusView == null)
				return true;
			
//			postDelayed(mRepeater, 300);
//			mCurrentDirection = SCROLL_RIGHT;
			selectRight(lastFocusView, nextFocus, true);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP && keyAction == KeyEvent.ACTION_DOWN) {
			// postDelayed(mRepeater, 300);
//			mCurrentDirection = SCROLL_UP;

			View lastFocusView = getFocusedChild();
			View nextFocus = mFocusF.findNextFocus(this, getFocusedChild(), View.FOCUS_UP);
			
			if(nextFocus == null) {
				if (onSlidingAtEndListener != null) {
					boolean result = onSlidingAtEndListener.onSlidingAtUp(DetailedActivityAppLayout.this, getFocusedChild());
					if (result){
						return true;
					}
				}
			}
			
			if (lastFocusView == null || nextFocus == null)
				return true;
			selectUp(lastFocusView, nextFocus, true);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && keyAction == KeyEvent.ACTION_DOWN) {
			// postDelayed(mRepeater, 300);
//			mCurrentDirection = SCROLL_DOWN;

			View lastFocusView = getFocusedChild();
			View nextFocus = mFocusF.findNextFocus(this, getFocusedChild(), View.FOCUS_DOWN);
			
			if(nextFocus == null) {
				if (onSlidingAtEndListener != null) {
					boolean result = onSlidingAtEndListener.onSlidingAtDown(DetailedActivityAppLayout.this, getFocusedChild());
					if (result){
						return true;
					}
				}
			}
			
			if (lastFocusView == null || nextFocus == null)
				return true;
			selectDown(lastFocusView, nextFocus, true);
			return true;
		}

		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && keyAction == KeyEvent.ACTION_UP) {
//			mIsRepeating = false;
//			removeCallbacks(mRepeater);
//			AppLog.log_D(TAG, "mIsReprating   is  " + mIsRepeating);
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && keyAction == KeyEvent.ACTION_UP) {
//			mIsRepeating = false;
//			removeCallbacks(mRepeater);
//			AppLog.log_D(TAG, "mIsReprating   is  " + mIsRepeating);
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP && keyAction == KeyEvent.ACTION_UP) {
//			mIsRepeating = false;
//			removeCallbacks(mRepeater);
//			AppLog.log_D(TAG, "mIsReprating   is  " + mIsRepeating);
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && keyAction == KeyEvent.ACTION_UP) {
//			mIsRepeating = false;
//			removeCallbacks(mRepeater);
//			AppLog.log_D(TAG, "mIsReprating   is  " + mIsRepeating);
		}

		return super.dispatchKeyEvent(event);
	}

	public void selectLeft(View lastFocus, View nextFocus, boolean animation) {
		if (lastFocus == null || nextFocus == null)
			return;
		
		AppLog.d(TAG, "nextFocus is " + nextFocus);
		AppLog.d(TAG, "nextFocus height is " + nextFocus.getHeight());
//		mIsRepeating = true;
		synchronized (control) {
			
			if (nextFocus instanceof AnimationImageButton){
				if(((AnimationImageButton) nextFocus).isFlag())
					control.transformAnimationForImage(mBackImageView, nextFocus, mContext,true,false);
				else
					control.transformAnimation(mBackImageView, lastFocus, nextFocus, mContext, animation, false);
			} else if (nextFocus instanceof AnimationTextButton){
				if(((AnimationTextButton) nextFocus).isFlag())
					control.transformAnimationForImage(mBackImageView, nextFocus, mContext,true,false);
				else
					control.transformAnimation(mBackImageView, lastFocus, nextFocus, mContext, animation, false);
			} else
				control.transformAnimation(mBackImageView, lastFocus, nextFocus, mContext, animation, false);
			
		}
		nextFocus.requestFocus();
	}

	public void selectRight(View lastFocus, View nextFocus, boolean animation) {
		if (lastFocus == null || nextFocus == null)
			return;
//		mIsRepeating = true;
		synchronized (control) {
			control.transformAnimation(mBackImageView, lastFocus, nextFocus, mContext, animation, true);
		}
		nextFocus.requestFocus();
	}

	public void selectUp(View lastFocus, View nextFocus, boolean animation) {
		if (lastFocus == null || nextFocus == null)
			return;
		// mIsRepeating = true;
		synchronized (control) {
			control.transformAnimation(mBackImageView, lastFocus, nextFocus, mContext, animation, true);
		}
		nextFocus.requestFocus();
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

	public void setBackImageView(ImageView mBackImageView) {
		this.mBackImageView = mBackImageView;
	}

//	private Runnable mRepeater = new Runnable() {
//		public void run() {
//			doRepeat(false);
//			if (mIsRepeating) {
//				postDelayed(this, mInterval);
//			}
//		}
//
//		private void doRepeat(boolean b) {
//			AppLog.log_D(TAG, "DetailedActivityAppLayout:doRepeat   ");
//			View lastFocusView = getFocusedChild();
//			if (mCurrentDirection == SCROLL_LEFT) {
//				View nextFocus = mFocusF.findNextFocus(DetailedActivityAppLayout.this, lastFocusView,
//						View.FOCUS_LEFT);
//				if (nextFocus == null) {
//					if (onSlidingAtEndListener != null) {
//						boolean result = onSlidingAtEndListener.onSlidingAtLeft(DetailedActivityAppLayout.this, getFocusedChild());
//						if (result){
//							AppLog.log_D(TAG, "-----onSlidingAtEnd return true");
//							mIsRepeating = false;
//							removeCallbacks(mRepeater);
//							return;
//						}
//
//					}
//				}
//				selectLeft(lastFocusView, nextFocus, false);
//			} else if (mCurrentDirection == SCROLL_RIGHT) {
//				View nextFocus = mFocusF.findNextFocus(DetailedActivityAppLayout.this, lastFocusView,
//						View.FOCUS_RIGHT);
//				if (nextFocus == null) {
//					if (onSlidingAtEndListener != null) {
//						boolean result = onSlidingAtEndListener.onSlidingAtRight(DetailedActivityAppLayout.this, getFocusedChild());
//						if (result){
//							AppLog.log_D(TAG, "-------onSlidingAtEnd return true");
//							mIsRepeating = false;
//							removeCallbacks(mRepeater);
//							return;
//						}
//							
//					}
//				}
//				selectRight(lastFocusView, nextFocus, false);
//			} else if (mCurrentDirection == SCROLL_UP) {
//				View nextFocus = mFocusF.findNextFocus(mFocusGroup != null ? mFocusGroup : (ViewGroup) DetailedActivityAppLayout.this.getParent(), lastFocusView,
//						View.FOCUS_UP);
//				selectUp(lastFocusView, nextFocus, false);
//			} else if (mCurrentDirection == SCROLL_DOWN) {
//				View nextFocus = mFocusF.findNextFocus(mFocusGroup != null ? mFocusGroup : (ViewGroup) DetailedActivityAppLayout.this.getParent(), lastFocusView,
//						View.FOCUS_DOWN);
//				selectDown(lastFocusView, nextFocus, false);
//			}
//		}
//	};

	public void setOnSlidingAtEndListener(OnSlidingAtEndListener l) {
		this.onSlidingAtEndListener = l;
	}

	public interface OnSlidingAtEndListener {
		boolean onSlidingAtUp(DetailedActivityAppLayout layout, View view);
		boolean onSlidingAtDown(DetailedActivityAppLayout layout, View view);
	}

//	public void setFocusGroup(ViewGroup view) {
//		this.mFocusGroup = view;
//	}
}
