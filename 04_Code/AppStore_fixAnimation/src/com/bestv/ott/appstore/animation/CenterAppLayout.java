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
import android.widget.LinearLayout;

public class CenterAppLayout extends GridLayout {
	
	public static final String TAG = "com.joysee.appstore.CenterAppLayout";

	private FocusFinder mFocusF = FocusFinder.getInstance();
	private AnimationControl control = AnimationControl.getInstance();
	private Context mContext;

	private ImageView mBackImageView;

	private boolean mIsRepeating;
	private int mInterval = 30;
	private int mDelayTime = 450;
	private int mCurrentDirection;

	private OnSlidingAtEndListener onSlidingAtEndListener;

	private static final int SCROLL_LEFT = 0;
	private static final int SCROLL_RIGHT = 1;
	private static final int SCROLL_UP = 2;
	private static final int SCROLL_DOWN = 3;
	
	private ViewGroup mFocusGroup;

	public CenterAppLayout(Context context) {
		super(context);
		this.mContext = context;
	}

	public CenterAppLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
	}

	public CenterAppLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
	}
	long begin = -1;
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int keyCode = event.getKeyCode();
		int keyAction = event.getAction();
		AppLog.d(TAG, "CenterApplayout:dispatchKeyEvent()  begin   mIsRepeating= " + mIsRepeating);

		if (mIsRepeating && keyAction == KeyEvent.ACTION_DOWN)
			if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
				return true;

		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && keyAction == KeyEvent.ACTION_DOWN) {

			View lastFocusView = getFocusedChild();
			View nextFocus = mFocusF.findNextFocus(this, getFocusedChild(), View.FOCUS_LEFT);
			
			if(nextFocus == null) {
				if (onSlidingAtEndListener != null) {
					boolean result = onSlidingAtEndListener.onSlidingAtLeft(CenterAppLayout.this, getFocusedChild(),false);
					if (result){
						AppLog.d(TAG, "-----onSlidingAtEnd return true");
						mIsRepeating = false;
						removeCallbacks(mRepeater);
						return true;
					}
				}
			}
			
			if (lastFocusView == null)
				return true;

			postDelayed(mRepeater, mDelayTime);
			mCurrentDirection = SCROLL_LEFT;
			selectLeft(lastFocusView, nextFocus, true);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && keyAction == KeyEvent.ACTION_DOWN) {
			begin = System.currentTimeMillis();
			AppLog.d(TAG, "------------------------begin------------------------------------" + begin);
			
			View lastFocusView = getFocusedChild();
			View nextFocus = mFocusF.findNextFocus(this, getFocusedChild(), View.FOCUS_RIGHT);
			
			if(nextFocus == null) {
				if (onSlidingAtEndListener != null) {
					boolean result = onSlidingAtEndListener.onSlidingAtRight(CenterAppLayout.this, getFocusedChild(),false);
					if (result){
						AppLog.d(TAG, "-----onSlidingAtEnd return true");
						mIsRepeating = false;
						removeCallbacks(mRepeater);
						return true;
					}
				}
			}
			
			if (lastFocusView == null)
				return true;
			
			postDelayed(mRepeater, mDelayTime);
			mCurrentDirection = SCROLL_RIGHT;
			selectRight(lastFocusView, nextFocus, true);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP && keyAction == KeyEvent.ACTION_DOWN) {
			// postDelayed(mRepeater, 300);
			mCurrentDirection = SCROLL_UP;

			View lastFocusView = getFocusedChild();
			View nextFocus = mFocusF.findNextFocus(mFocusGroup != null ? mFocusGroup : (ViewGroup) this.getParent(), getFocusedChild(), View.FOCUS_UP);
			if (lastFocusView == null || nextFocus == null)
				return true;
			selectUp(lastFocusView, nextFocus, true);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && keyAction == KeyEvent.ACTION_DOWN) {
			// postDelayed(mRepeater, 300);
			mCurrentDirection = SCROLL_DOWN;

			View lastFocusView = getFocusedChild();
			View nextFocus = mFocusF.findNextFocus(mFocusGroup != null ? mFocusGroup : (ViewGroup) this.getParent(), getFocusedChild(), View.FOCUS_DOWN);
			if (lastFocusView == null || nextFocus == null)
				return true;
			selectDown(lastFocusView, nextFocus, true);
			return true;
		}

		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && keyAction == KeyEvent.ACTION_UP) {
			mIsRepeating = false;
			removeCallbacks(mRepeater);
			AppLog.d(TAG, "mIsReprating   is  " + mIsRepeating);
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && keyAction == KeyEvent.ACTION_UP) {
			long end = System.currentTimeMillis();
			AppLog.d(TAG, begin + "------------------------begin---------------end-begin---------------------" + (end - begin));

			mIsRepeating = false;
			removeCallbacks(mRepeater);
			AppLog.d(TAG, "mIsReprating   is  " + mIsRepeating);
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP && keyAction == KeyEvent.ACTION_UP) {
			mIsRepeating = false;
			removeCallbacks(mRepeater);
			AppLog.d(TAG, "mIsReprating   is  " + mIsRepeating);
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && keyAction == KeyEvent.ACTION_UP) {
			mIsRepeating = false;
			removeCallbacks(mRepeater);
			AppLog.d(TAG, "mIsReprating   is  " + mIsRepeating);
		}

		return super.dispatchKeyEvent(event);
	}

	public void selectLeft(View lastFocus, View nextFocus, boolean animation) {
		if (lastFocus == null || nextFocus == null)
			return;
		mIsRepeating = true;
		synchronized (control) {
			control.transformAnimation(mBackImageView, lastFocus, nextFocus, mContext, animation, true);
		}
		nextFocus.requestFocus();
	}

	public void selectRight(View lastFocus, View nextFocus, boolean animation) {
		if (lastFocus == null || nextFocus == null)
			return;
		mIsRepeating = true;
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

	private Runnable mRepeater = new Runnable() {
		public void run() {
			doRepeat(false);
			if (mIsRepeating) {
				postDelayed(this, mInterval);
			}
		}

		private void doRepeat(boolean b) {
			AppLog.d(TAG, "CenterApplayout:doRepeat   ");
			View lastFocusView = getFocusedChild();
			
			if(lastFocusView instanceof LinearLayout){
				View focusView = ((LinearLayout) lastFocusView).getChildAt(0);
				if(focusView instanceof AnimationButton){
					((AnimationButton)focusView).stopAnimation();
				}
			}
			
			if (mCurrentDirection == SCROLL_LEFT) {
				View nextFocus = mFocusF.findNextFocus(CenterAppLayout.this, lastFocusView,
						View.FOCUS_LEFT);
				if (nextFocus == null) {
					if (onSlidingAtEndListener != null) {
						boolean result = onSlidingAtEndListener.onSlidingAtLeft(CenterAppLayout.this, getFocusedChild(),true);
						if (result){
							AppLog.d(TAG, "-----onSlidingAtEnd return true");
							mIsRepeating = false;
							removeCallbacks(mRepeater);
							return;
						}

					}
				}
				selectLeft(lastFocusView, nextFocus, false);
			} else if (mCurrentDirection == SCROLL_RIGHT) {
				View nextFocus = mFocusF.findNextFocus(CenterAppLayout.this, lastFocusView,
						View.FOCUS_RIGHT);
				if (nextFocus == null) {
					if (onSlidingAtEndListener != null) {
						boolean result = onSlidingAtEndListener.onSlidingAtRight(CenterAppLayout.this, getFocusedChild(),true);
						if (result){
							AppLog.d(TAG, "-------onSlidingAtEnd return true");
							mIsRepeating = false;
							removeCallbacks(mRepeater);
							return;
						}
							
					}
				}
				selectRight(lastFocusView, nextFocus, false);
			} else if (mCurrentDirection == SCROLL_UP) {
				View nextFocus = mFocusF.findNextFocus(mFocusGroup != null ? mFocusGroup : (ViewGroup) CenterAppLayout.this.getParent(), lastFocusView,
						View.FOCUS_UP);
				selectUp(lastFocusView, nextFocus, false);
			} else if (mCurrentDirection == SCROLL_DOWN) {
				View nextFocus = mFocusF.findNextFocus(mFocusGroup != null ? mFocusGroup : (ViewGroup) CenterAppLayout.this.getParent(), lastFocusView,
						View.FOCUS_DOWN);
				selectDown(lastFocusView, nextFocus, false);
			}
		}
	};

	public void setOnSlidingAtEndListener(OnSlidingAtEndListener l) {
		this.onSlidingAtEndListener = l;
	}

	public interface OnSlidingAtEndListener {
		boolean onSlidingAtLeft(CenterAppLayout layout, View view,boolean fastMode);
		boolean onSlidingAtRight(CenterAppLayout layout, View view,boolean fastMode);
	}

	public void setFocusGroup(ViewGroup view) {
		this.mFocusGroup = view;
	}
	
	public boolean iSInRepeating() {
		return mIsRepeating;
	}
}
