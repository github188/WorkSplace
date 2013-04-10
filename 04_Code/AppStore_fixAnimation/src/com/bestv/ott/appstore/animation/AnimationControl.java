package com.bestv.ott.appstore.animation;

import java.util.LinkedList;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.EditText;
import android.widget.ListView;

import com.bestv.ott.appstore.utils.AppLog;
import com.bestv.ott.appstore.R;

/**
 * 
 * @author yanhailong
 * @function 控制浮动图片的移动 和大小
 */

public class AnimationControl {
	public static final String TAG = "com.joysee.appstore.AnimationControl";

	private static final int[] mLoc1s = new int[2];// 原view 所在位置
	private static final int[] mLoc2s = new int[2];// 目的view 所在位置
	private static final int[] mLocback = new int[2];// 背景 view 所在位置
	private static final float[] mDimen1s = new float[2];// 原view 的宽高
	private static final float[] mDimen2s = new float[2];// 目的view 宽高
	private static final float[] mDimenback = new float[2];// 背景 view 宽高

	public static LinkedList<Message> mList = new LinkedList<Message>();
	public static final int MSG_ANIMATION = 1;// handler动画消息menuview
	public static final int MSG_ANIMATION_MUL = 2;// handler动画消息gridview

	public static final int ANIMATION_MOVE_TIME = 150;
	public static final int MOVE_WIDGET_DISPATCH = 20;

	private static Animation alpha = null;
	private static Animation myAnimation_Alpha = null;
	private static View mSourceImage = null;
	private static View mDestImage = null;
	private static View mFirstImage = null;
	private static Context thisCon = null;
	private static AnimationControl instance;
	private ScaleTransAnimation scaleanimation;

	private AnimationControl() {
	}

	public static AnimationControl getInstance() {

		if (instance == null) {
			synchronized (AnimationControl.class) {
				if (instance == null) {
					instance = new AnimationControl();
				}// end inner if
			}// end synchronized
		}// end outter if
		return instance;
	}// end getInstance()

	public synchronized void transformAnimation(final View sourceImage, final View back, View second, Context context,
			boolean bol, final boolean after) {
		final int[] Loc2s = new int[2];// 目的view 所在位置
		final int[] Locback = new int[2];// 背景 view 所在位置
		final float[] Dimen2s = new float[2];// 目的view 宽高
		final float[] Dimenback = new float[2];// 背景 view 宽高
		thisCon = context;
		if (scaleanimation != null)
			scaleanimation.cancel();
		mSourceImage = sourceImage;
		final View mDestImage = second;
		mDestImage.getLocationInWindow(Loc2s);
		back.getLocationInWindow(Locback);

		//RuntimeException e1 = new RuntimeException();
		// e1.printStackTrace();
		MarginLayoutParams lp1 = (MarginLayoutParams) mSourceImage.getLayoutParams();
		AppLog.d(TAG, "topimage is " + lp1.leftMargin + "    locback  is " + lp1.topMargin);
		AppLog.d(TAG, "loc2s is " + Loc2s[0] + "    locback  is " + Locback[0]);
		AppLog.d(TAG, "loc2s height is " + Loc2s[1] + "    locback height  is " + Locback[1]);

		Dimen2s[0] = mDestImage.getWidth();
		Dimen2s[1] = mDestImage.getHeight();

		Dimenback[0] = back.getWidth();
		Dimenback[1] = back.getHeight();

		float dx = Loc2s[0] - Locback[0];
		float dy = Loc2s[1] - Locback[1];
		float sx = Dimen2s[0] / (Dimenback[0]);
		float sy = Dimen2s[1] / (Dimenback[1]);

		AppLog.d(TAG, "back   is " + back);
		AppLog.d(TAG, "mDestImage    is " + mDestImage);

		if (mDestImage instanceof ListView) {
			AppLog.d(TAG, "mSourceImage.setVisibility(View.GONE);     " + mSourceImage.getAlpha());
			mSourceImage.setVisibility(View.GONE);
			mSourceImage.setAlpha(0);
			return;
		}

		if (mSourceImage.getVisibility() == View.GONE) {
			AppLog.d(TAG, "mSourceImage.getVisibility() == View.GONE");
			mSourceImage.setAlpha((float) 0.5);
			MarginLayoutParams lp = (MarginLayoutParams) mSourceImage.getLayoutParams();
			lp.width = (int) Dimenback[0];
			lp.height = (int) Dimenback[1];
			lp.leftMargin = Locback[0];
			lp.topMargin = Locback[1];
			mSourceImage.setLayoutParams(lp);
			mSourceImage.setVisibility(View.VISIBLE);
			AppLog.d(TAG, "mSourceImage.getVisibility() ==View.VISIBLE"+(mSourceImage.getVisibility()==View.VISIBLE));
		}

		// else if (back instanceof ListView){
		// mSourceImage.setVisibility(View.VISIBLE);
		// }

		if (bol) {
			AppLog.d(TAG, "   " + dx + "   " + dy + "   " + sx + "   " + sy);
			scaleanimation = new ScaleTransAnimation(dx, dy, sx, sy);
			scaleanimation.setFillEnabled(true);
			scaleanimation.setFillAfter(true);
			scaleanimation.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					mSourceImage.setVisibility(View.VISIBLE);
					MarginLayoutParams lp = (MarginLayoutParams) mSourceImage.getLayoutParams();
					lp.width = (int) Dimen2s[0];
					lp.height = (int) Dimen2s[1];
					lp.leftMargin = Loc2s[0];
					lp.topMargin = Loc2s[1];
					mSourceImage.setLayoutParams(lp);
					Animation alpha_all = AnimationUtils.loadAnimation(thisCon, R.anim.alpha_animation_action);
					alpha_all.setDuration(MOVE_WIDGET_DISPATCH);
					alpha_all.setFillEnabled(true);
					alpha_all.setFillAfter(after);
					mSourceImage.startAnimation(alpha_all);

					if (AnimationButton.class.isInstance(mDestImage)) {
						AppLog.d(TAG, "AnimationButton    begin" + mDestImage);
//						RuntimeException e = new RuntimeException();
//						e.printStackTrace();
						AnimationButton button = (AnimationButton) mDestImage;
						button.startAnimation();
					}
					if (SearchAnimationButton.class.isInstance(mDestImage)) {
						
						AppLog.d(TAG, "SearchAnimationButton    begin" + mDestImage);
						SearchAnimationButton buttonSear = (SearchAnimationButton) mDestImage;
						buttonSear.startAnimation();
					}

					if (back instanceof ViewGroup) {
						ViewGroup viewGroup = (ViewGroup) back;
						View lastFocus = viewGroup.getChildAt(0);

						if (lastFocus != null) {
							if (AnimationButton.class.isInstance(lastFocus)) {
								AppLog.d(TAG, "AnimationButton    end" + lastFocus);
								AnimationButton button = (AnimationButton) lastFocus;
								button.stopAnimation();
							}
							if (SearchAnimationButton.class.isInstance(lastFocus)) {
								AppLog.d(TAG, "SearchAnimationButton    end" + lastFocus);
								SearchAnimationButton buttonSear = (SearchAnimationButton) lastFocus;
								buttonSear.stopAnimation();
							}
						}
					}
					if (AnimationButton.class.isInstance(back)) {
						AppLog.d(TAG, " second AnimationButton    end" + back);
						AnimationButton button = (AnimationButton) back;
						button.stopAnimation();
					} else if (SearchAnimationButton.class.isInstance(back)) {
						AppLog.d(TAG, " second SearchAnimationButton    end" + back);
						SearchAnimationButton buttonSear = (SearchAnimationButton) back;
						buttonSear.stopAnimation();
					}

				}
			});

			scaleanimation.setDuration(ANIMATION_MOVE_TIME);
			mSourceImage.bringToFront();
			mSourceImage.startAnimation(scaleanimation);
		} else {
			mSourceImage.setVisibility(View.VISIBLE);
			MarginLayoutParams lp = (MarginLayoutParams) mSourceImage.getLayoutParams();
			lp.width = (int) Dimen2s[0];
			lp.height = (int) Dimen2s[1];
			lp.leftMargin = Loc2s[0];
			lp.topMargin = Loc2s[1];
			mSourceImage.setLayoutParams(lp);
			Animation alpha_all = AnimationUtils.loadAnimation(thisCon, R.anim.alpha_animation_action);
			alpha_all.setDuration(MOVE_WIDGET_DISPATCH);
			alpha_all.setFillEnabled(true);
			alpha_all.setFillAfter(after);
			mSourceImage.startAnimation(alpha_all);

			if (AnimationButton.class.isInstance(mDestImage)) {
				AppLog.d(TAG, "AnimationButton    begin" + mDestImage);
				AnimationButton button = (AnimationButton) mDestImage;
				button.startAnimation();
			}
			if (SearchAnimationButton.class.isInstance(mDestImage)) {
				AppLog.d(TAG, "SearchAnimationButton    begin" + mDestImage);
				SearchAnimationButton buttonSear = (SearchAnimationButton) mDestImage;
				buttonSear.startAnimation();
			}

			if (back instanceof ViewGroup) {
				ViewGroup viewGroup = (ViewGroup) back;
				View lastFocus = viewGroup.getChildAt(0);

				if (lastFocus != null) {
					if (AnimationButton.class.isInstance(lastFocus)) {
						AppLog.d(TAG, "AnimationButton    end");
						AnimationButton button = (AnimationButton) lastFocus;
						button.stopAnimation();
					}
					if (SearchAnimationButton.class.isInstance(lastFocus)) {
						AppLog.d(TAG, "SearchAnimationButton    end");
						SearchAnimationButton buttonSear = (SearchAnimationButton) lastFocus;
						buttonSear.stopAnimation();
					}
				}
			} 
			if (AnimationButton.class.isInstance(back)) {
				AppLog.d(TAG, " second AnimationButton    end" + back);
				AnimationButton button = (AnimationButton) back;
				button.stopAnimation();
			} else if (SearchAnimationButton.class.isInstance(back)) {
				AppLog.d(TAG, " second SearchAnimationButton    end" + back);
				SearchAnimationButton buttonSear = (SearchAnimationButton) back;
				buttonSear.stopAnimation();
			}
		}

	}

	public synchronized void transformAnimation(View back, View second, Context context, boolean bol,
			final boolean after) {
		final int[] Loc2s = new int[2];// 目的view 所在位置
		final int[] Locback = new int[2];// 背景 view 所在位置
		final float[] Dimen2s = new float[2];// 目的view 宽高
		final float[] Dimenback = new float[2];// 背景 view 宽高
		thisCon = context;
		mSourceImage = back;
		mDestImage = second;
		mDestImage.getLocationInWindow(Loc2s);
		mSourceImage.getLocationInWindow(Locback);

		Dimen2s[0] = mDestImage.getWidth();
		Dimen2s[1] = mDestImage.getHeight();

		Dimenback[0] = mSourceImage.getWidth();
		Dimenback[1] = mSourceImage.getHeight();

		float dx = Loc2s[0] - Locback[0];
		float dy = Loc2s[1] - Locback[1];
		float sx = Dimen2s[0] / (Dimenback[0]);
		float sy = Dimen2s[1] / (Dimenback[1]);

		if (bol) {
			ScaleTransAnimation scaleanimation = new ScaleTransAnimation(dx, dy, sx, sy);
			scaleanimation.setFillEnabled(true);
			scaleanimation.setFillAfter(true);
			scaleanimation.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					mSourceImage.setVisibility(View.VISIBLE);
					MarginLayoutParams lp = (MarginLayoutParams) mSourceImage.getLayoutParams();
					lp.width = (int) Dimen2s[0];
					lp.height = (int) Dimen2s[1];
					lp.leftMargin = Loc2s[0];
					lp.topMargin = Loc2s[1];
					mSourceImage.setLayoutParams(lp);
					Animation alpha_all = AnimationUtils.loadAnimation(thisCon, R.anim.alpha_animation_action);
					alpha_all.setDuration(MOVE_WIDGET_DISPATCH);
					alpha_all.setFillEnabled(true);
					alpha_all.setFillAfter(after);
					mSourceImage.startAnimation(alpha_all);

					if (AnimationButton.class.isInstance(mDestImage)) {
						AnimationButton button = (AnimationButton) mDestImage;
						button.startAnimation();
					}
					if (SearchAnimationButton.class.isInstance(mDestImage)) {
						SearchAnimationButton buttonSear = (SearchAnimationButton) mDestImage;
						buttonSear.startAnimation();
					}

				}
			});

			scaleanimation.setDuration(ANIMATION_MOVE_TIME);
			mSourceImage.bringToFront();
			mSourceImage.startAnimation(scaleanimation);
		} else {
			mSourceImage.setVisibility(View.VISIBLE);
			MarginLayoutParams lp = (MarginLayoutParams) mSourceImage.getLayoutParams();
			lp.width = (int) Dimen2s[0];
			lp.height = (int) Dimen2s[1];
			lp.leftMargin = Loc2s[0];
			lp.topMargin = Loc2s[1];
			mSourceImage.setLayoutParams(lp);
			Animation alpha_all = AnimationUtils.loadAnimation(thisCon, R.anim.alpha_animation_action);
			alpha_all.setDuration(MOVE_WIDGET_DISPATCH);
			alpha_all.setFillEnabled(true);
			alpha_all.setFillAfter(after);
			mSourceImage.startAnimation(alpha_all);
		}

	}

	public synchronized void transformAnimationForImage(final View sourceImage, final View back, View second,
			Context context, boolean bol, final boolean after) {
		final int[] Loc2s = new int[2];// 目的view 所在位置
		final int[] Locback = new int[2];// 背景 view 所在位置
		final float[] Dimen2s = new float[2];// 目的view 宽高
		final float[] Dimenback = new float[2];// 背景 view 宽高
		thisCon = context;
		mSourceImage = sourceImage;
		mDestImage = second;
		mDestImage.getLocationInWindow(Loc2s);
		back.getLocationInWindow(Locback);

		Dimen2s[0] = mDestImage.getWidth();
		Dimen2s[1] = mDestImage.getHeight();

		Dimenback[0] = back.getWidth();
		Dimenback[1] = back.getHeight();

		float dx = Loc2s[0] - Locback[0] - (int) Dimen2s[0] / 20;
		float dy = Loc2s[1] - Locback[1] - (int) Dimen2s[1] / 10;
		float sx = Dimen2s[0] / (Dimenback[0] * 5 / 6);
		float sy = Dimen2s[1] / (Dimenback[1] * 10 / 11);

		if (mSourceImage.getVisibility() == View.GONE) {
			AppLog.d(TAG, "mSourceImage.getVisibility() == View.GONE");
			mSourceImage.setAlpha((float) 0.5);
			MarginLayoutParams lp = (MarginLayoutParams) mSourceImage.getLayoutParams();
			lp.width = (int) Dimenback[0];
			lp.height = (int) Dimenback[1];
			lp.leftMargin = Locback[0];
			lp.topMargin = Locback[1];
			mSourceImage.setLayoutParams(lp);
			mSourceImage.setVisibility(View.VISIBLE);
		}

		if (bol) {
			ScaleTransAnimation scaleanimation = new ScaleTransAnimation(dx, dy, sx, sy);
			scaleanimation.setFillEnabled(true);
			scaleanimation.setFillAfter(true);
			scaleanimation.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					mSourceImage.setVisibility(View.VISIBLE);
					MarginLayoutParams lp = (MarginLayoutParams) mSourceImage.getLayoutParams();
					lp.width = (int) Dimen2s[0] * 11 / 10 + 10;
					lp.height = (int) Dimen2s[1] * 6 / 5 + 12;
					lp.leftMargin = Loc2s[0] - (int) Dimen2s[0] / 20 - 5;
					lp.topMargin = Loc2s[1] - (int) Dimen2s[1] / 10 - 7;
					mSourceImage.setLayoutParams(lp);
					Animation alpha_all = AnimationUtils.loadAnimation(thisCon, R.anim.alpha_animation_action);
					alpha_all.setDuration(MOVE_WIDGET_DISPATCH);
					alpha_all.setFillEnabled(true);
					alpha_all.setFillAfter(after);
					mSourceImage.startAnimation(alpha_all);

					if (AnimationButton.class.isInstance(mDestImage)) {
						AppLog.d(TAG, "AnimationButton    begin");
						AnimationButton button = (AnimationButton) mDestImage;
						button.startAnimation();
					}
					if (SearchAnimationButton.class.isInstance(mDestImage)) {
						AppLog.d(TAG, "SearchAnimationButton    begin");
						SearchAnimationButton buttonSear = (SearchAnimationButton) mDestImage;
						buttonSear.startAnimation();
					}
				}
			});

			scaleanimation.setDuration(ANIMATION_MOVE_TIME);
			mSourceImage.bringToFront();
			mSourceImage.startAnimation(scaleanimation);
		} else {
			mSourceImage.setVisibility(View.VISIBLE);
			MarginLayoutParams lp = (MarginLayoutParams) mSourceImage.getLayoutParams();
			lp.width = (int) Dimen2s[0] * 11 / 10 + 15;
			lp.height = (int) Dimen2s[1] * 6 / 5 + 15;
			lp.leftMargin = Loc2s[0] - (int) Dimen2s[0] / 20 - 5;
			lp.topMargin = Loc2s[1] - (int) Dimen2s[1] / 10 - 5;
			mSourceImage.setLayoutParams(lp);
			Animation alpha_all = AnimationUtils.loadAnimation(thisCon, R.anim.alpha_animation_action);
			alpha_all.setDuration(MOVE_WIDGET_DISPATCH);
			alpha_all.setFillEnabled(true);
			alpha_all.setFillAfter(after);
			mSourceImage.startAnimation(alpha_all);
		}

	}

	public synchronized void transformAnimationForImage(final View back, View second, Context context, boolean bol,
			final boolean after) {
		final int[] Loc2s = new int[2];// 目的view 所在位置
		final int[] Locback = new int[2];// 背景 view 所在位置
		final float[] Dimen2s = new float[2];// 目的view 宽高
		final float[] Dimenback = new float[2];// 背景 view 宽高
		thisCon = context;
		mSourceImage = back;
		mDestImage = second;
		mDestImage.getLocationInWindow(Loc2s);
		mSourceImage.getLocationInWindow(Locback);

		Dimen2s[0] = mDestImage.getWidth();
		Dimen2s[1] = mDestImage.getHeight();

		Dimenback[0] = mSourceImage.getWidth();
		Dimenback[1] = mSourceImage.getHeight();

		float dx = Loc2s[0] - Locback[0] - (int) Dimen2s[0] / 20;
		float dy = Loc2s[1] - Locback[1] - (int) Dimen2s[1] / 10;
		float sx = Dimen2s[0] / (Dimenback[0] * 5 / 6);
		float sy = Dimen2s[1] / (Dimenback[1] * 10 / 11);

		if (bol) {
			ScaleTransAnimation scaleanimation = new ScaleTransAnimation(dx, dy, sx, sy);
			scaleanimation.setFillEnabled(true);
			scaleanimation.setFillAfter(true);
			scaleanimation.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					mSourceImage.setVisibility(View.VISIBLE);
					MarginLayoutParams lp = (MarginLayoutParams) mSourceImage.getLayoutParams();
					lp.width = (int) Dimen2s[0] * 11 / 10 + 10;
					lp.height = (int) Dimen2s[1] * 6 / 5 + 12;
					lp.leftMargin = Loc2s[0] - (int) Dimen2s[0] / 20 - 5;
					lp.topMargin = Loc2s[1] - (int) Dimen2s[1] / 10 - 7;
					mSourceImage.setLayoutParams(lp);
					Animation alpha_all = AnimationUtils.loadAnimation(thisCon, R.anim.alpha_animation_action);
					alpha_all.setDuration(MOVE_WIDGET_DISPATCH);
					alpha_all.setFillEnabled(true);
					alpha_all.setFillAfter(after);
					mSourceImage.startAnimation(alpha_all);

					if (AnimationButton.class.isInstance(mDestImage)) {
						AppLog.d(TAG, "AnimationButton    begin");
						AnimationButton button = (AnimationButton) mDestImage;
						button.startAnimation();
					}
					if (SearchAnimationButton.class.isInstance(mDestImage)) {
						AppLog.d(TAG, "SearchAnimationButton    begin");
						SearchAnimationButton buttonSear = (SearchAnimationButton) mDestImage;
						buttonSear.startAnimation();
					}
				}
			});
			scaleanimation.setDuration(ANIMATION_MOVE_TIME);
			mSourceImage.bringToFront();
			mSourceImage.startAnimation(scaleanimation);
		} else {
			mSourceImage.setVisibility(View.VISIBLE);
			MarginLayoutParams lp = (MarginLayoutParams) mSourceImage.getLayoutParams();
			lp.width = (int) Dimen2s[0] * 11 / 10 + 10;
			lp.height = (int) Dimen2s[1] * 6 / 5 + 12;
			lp.leftMargin = Loc2s[0] - (int) Dimen2s[0] / 20 - 5;
			lp.topMargin = Loc2s[1] - (int) Dimen2s[1] / 10 - 7;
			mSourceImage.setLayoutParams(lp);
			Animation alpha_all = AnimationUtils.loadAnimation(thisCon, R.anim.alpha_animation_action);
			alpha_all.setDuration(MOVE_WIDGET_DISPATCH);
			alpha_all.setFillEnabled(true);
			alpha_all.setFillAfter(after);
			mSourceImage.startAnimation(alpha_all);
		}

	}
	
	//搜索出结果的时候，清掉搜索按钮上的焦点背景
	public void clearFource(){
		mSourceImage.setVisibility(View.GONE);
		mSourceImage.setAlpha(0);
	}
	
	//搜索出结果的时候，清掉搜索按钮上的焦点背景
	public void showFource(){
		mSourceImage.setVisibility(View.VISIBLE);
		mSourceImage.setAlpha(1);
	}
	
}
