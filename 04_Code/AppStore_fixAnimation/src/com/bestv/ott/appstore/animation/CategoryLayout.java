package com.bestv.ott.appstore.animation;

import com.bestv.ott.appstore.utils.AppLog;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class CategoryLayout extends LinearLayout {

	
	public static final String TAG = "com.joysee.appstore.CategoryLayout";
	private ImageView mBackImageView;

	private static final int SCROLL_LEFT = 0;
	private static final int SCROLL_RIGHT = 1;

	private boolean mIsRepeating = false;
	private int mInterval = 15;
	private int mDelayTime = 450;
	private int mCurrentDirection;

	private FocusFinder mFocusF = FocusFinder.getInstance();
	private AnimationControl control = AnimationControl.getInstance();
	private Context mContext;
	
	private ViewGroup mFocusGroup;

	public CategoryLayout(Context context) {
		super(context);
		this.mContext = context;
	}

	public CategoryLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
	}

	public CategoryLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
	}
	long begin = -1;
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		AppLog.d(TAG, "BottomMenulayout:dispatchKeyEvent()  begin   mIsRepeating= " + mIsRepeating);
		
		int keyCode = event.getKeyCode();
		int keyAction = event.getAction();
		
		if (mIsRepeating && keyAction == KeyEvent.ACTION_DOWN)
			return true;

		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && keyAction == KeyEvent.ACTION_DOWN) {

			mCurrentDirection = SCROLL_LEFT;
			postDelayed(mRepeater, mDelayTime);

			View lastFocusView = getFocusedChild();
			View nextFocus = mFocusF.findNextFocus((ViewGroup) this, getFocusedChild(), View.FOCUS_LEFT);
			AppLog.d(TAG, "lastFocusView       " + lastFocusView);
			AppLog.d(TAG, "nextFocus       " + nextFocus);
			if (lastFocusView == null || nextFocus == null)
				return true;

			selectLeft(lastFocusView, nextFocus, true);

			// nextFocus.requestFocus();
			// synchronized (control) {
			// control.transformAnimation(mBackImageView, lastFocusView,
			// nextFocus, mContext, true, true);
			// }
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && keyAction == KeyEvent.ACTION_DOWN) {

			mCurrentDirection = SCROLL_RIGHT;
			begin = System.currentTimeMillis();
			AppLog.d(TAG, "------------------------begin------------------------------------" + begin);
			postDelayed(mRepeater, mDelayTime);

			View lastFocusView = getFocusedChild();
			
			View nextFocus = mFocusF.findNextFocus((ViewGroup) this, getFocusedChild(), View.FOCUS_RIGHT);
			AppLog.d(TAG, "-----lastFocusView----------" + lastFocusView);
			AppLog.d(TAG, "-----nextFocus----------" + nextFocus);
			if (lastFocusView == null || nextFocus == null)
				return true;

			selectRight(lastFocusView, nextFocus, true);

			// nextFocus.requestFocus();
			// synchronized (control) {
			// control.transformAnimation(mBackImageView, lastFocusView,
			// nextFocus, mContext, true, true);
			// }
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && keyAction == KeyEvent.ACTION_DOWN) {
			View lastFocusView = getFocusedChild();
			View nextFocus = mFocusF.findNextFocus((ViewGroup) this.getParent().getParent().getParent().getParent(), getFocusedChild(), View.FOCUS_DOWN);
			if (nextFocus != null) {
				nextFocus.requestFocus();
				synchronized (control) {
					control.transformAnimation(mBackImageView, lastFocusView, nextFocus, mContext, true, true);
				}
			}
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP && keyAction == KeyEvent.ACTION_DOWN) {
			View lastFocusView = getFocusedChild();
			View nextFocus = mFocusF.findNextFocus((ViewGroup) this.getParent().getParent(), getFocusedChild(), View.FOCUS_UP);
			if (nextFocus != null) {
				nextFocus.requestFocus();
				synchronized (control) {
					control.transformAnimation(mBackImageView, lastFocusView, nextFocus, mContext, true, true);
				}
			}
			return true;
		}

		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && keyAction == KeyEvent.ACTION_UP) {
			repeatCount = 0;
			removeCallbacks(mRepeater);
			mIsRepeating = false;
			AppLog.d(TAG, "mIsReprating   is  " + mIsRepeating);
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && keyAction == KeyEvent.ACTION_UP) {
			removeCallbacks(mRepeater);
			repeatCount = 0;
			mIsRepeating = false;
			long end = System.currentTimeMillis();
			AppLog.d(TAG, begin + "------------------------begin---------------end-begin---------------------" + (end - begin));

			AppLog.d(TAG, "mIsReprating   is  " + mIsRepeating);
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && keyAction == KeyEvent.ACTION_UP) {
			repeatCount = 0;
			removeCallbacks(mRepeater);
			mIsRepeating = false;
			AppLog.d(TAG, "mIsReprating   is  " + mIsRepeating);
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP && keyAction == KeyEvent.ACTION_UP) {
			repeatCount = 0;
			removeCallbacks(mRepeater);
			mIsRepeating = false;
			AppLog.d(TAG, "mIsReprating   is  " + mIsRepeating);
		}

		return super.dispatchKeyEvent(event);
	}

	private Runnable mRepeater = new Runnable() {
		public void run() {
			long begin = SystemClock.elapsedRealtime();
			doRepeat(false);
			if (mIsRepeating) {
				postDelayed(this, mInterval);
			}
			long end = SystemClock.elapsedRealtime();
			AppLog.d(TAG, "repeatCount" + repeatCount + "takes time   " + (end - begin));
		}
	};

	private int repeatCount = 0;
	private void doRepeat(boolean last) {
		repeatCount++;
		AppLog.d(TAG, "BottomMenuLayout:doRepeat                 "              + repeatCount);
		View lastFocusView = getFocusedChild();
		if (mCurrentDirection == SCROLL_LEFT) {
			View nextFocus = mFocusF.findNextFocus((ViewGroup) this, lastFocusView, View.FOCUS_LEFT);
			selectLeft(lastFocusView, nextFocus, false);
			// synchronized (control) {
			// control.transformAnimation(mBackImageView, lastFocusView,
			// nextFocus, mContext, false, true);
			// }
		} else if (mCurrentDirection == SCROLL_RIGHT) {
			View nextFocus = mFocusF.findNextFocus((ViewGroup) this, lastFocusView, View.FOCUS_RIGHT);
			selectRight(lastFocusView, nextFocus, false);
			// synchronized (control) {
			// control.transformAnimation(mBackImageView, lastFocusView,
			// nextFocus, mContext, false, lastFocusViewtrue);
			// }
		}
	}

	private void selectLeft(View lastFocus, View nextFocus, boolean animation) {
		
		if (lastFocus == null || nextFocus == null)
			return;
		
		ViewParent parent = this.getParent();
		if(parent !=null){
			int scrollWidth = ((ViewGroup)parent).getWidth();
			int[] nextLoc = new int[2];
			nextFocus.getLocationOnScreen(nextLoc);
			int needWidth = nextLoc[0] - 30;
			AppLog.d(TAG, "nextLoc[0]  = " + nextLoc[0] + " nextFocus.getWidth() = " + nextFocus.getWidth());
			
			ScrollView sv = (ScrollView)this.getParent();
			if (needWidth < 0) {
				
				sv.scrollBy(needWidth, 0);
				mScrollOffset = mScrollOffset + needWidth;
			} else if(needWidth + nextFocus.getWidth() > scrollWidth){
				sv.scrollBy(needWidth + nextFocus.getWidth() - sv.getWidth(), 0);
				mScrollOffset = needWidth + nextFocus.getWidth()  - sv.getWidth();
			}
			AppLog.d(TAG,"mScrollOffset  " + mScrollOffset);
		}
		
		
		nextFocus.requestFocus();
		mIsRepeating = true;

		synchronized (control) {
			control.transformAnimation(mBackImageView, lastFocus, nextFocus, mContext, animation, true);
		}
	}

	
	private int mScrollOffset;
	
	private void selectRight(View lastFocus, View nextFocus, boolean animation) {
		AppLog.d(TAG, "this width is " + this.getWidth() + "this.getSuggestedMinimumWidth()    " + this.getMeasuredWidth());
		ViewParent parent = this.getParent();
		if(parent !=null){
			int scrollWidth = ((ViewGroup)parent).getWidth();
			if(nextFocus != null){
				int[] nextLoc = new int[2];
				nextFocus.getLocationOnScreen(nextLoc);
				int needWidth = nextLoc[0] + nextFocus.getWidth() - 30;
				AppLog.d(TAG, "nextLoc[0]  = " + nextLoc[0] + " nextFocus.getWidth() = " + nextFocus.getWidth());
				ScrollView sv = (ScrollView)this.getParent();
				if (needWidth > scrollWidth) {
					sv.scrollBy(needWidth - sv.getWidth(), 0);
					mScrollOffset = mScrollOffset + (needWidth - sv.getWidth());
				} else if(nextLoc[0] < 0){
					sv.scrollBy(-mScrollOffset, 0);
					mScrollOffset = 0;
				}
				AppLog.d(TAG,"mScrollOffset  " + mScrollOffset);
			}
		}
		
		if (nextFocus == null)
			return;
		nextFocus.requestFocus();
		mIsRepeating = true;
		if(lastFocus == null || nextFocus == null || mBackImageView == null)
			return;
		synchronized (control) {
			control.transformAnimation(mBackImageView, lastFocus, nextFocus, mContext, animation, true);
		}	
	}


	public void setBackImageView(ImageView mBackImageView) {
		this.mBackImageView = mBackImageView;
	}
	
	public void setFocusGroup(ViewGroup group) {
		this.mFocusGroup = group;
	}
}
