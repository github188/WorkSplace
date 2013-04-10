package com.joysee.appstore.animation;


import java.util.LinkedList;
import java.util.Queue;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.TextView;

import com.joysee.appstore.R;
import com.joysee.appstore.common.AppsBean;
import com.joysee.appstore.utils.AppLog;

public class MoveControl {
	public static final String TAG = "com.joysee.appstore.animation.MoveControl";

	public int ANIMATION_MOVE_TIME = AnimUtils.ANIMATION_MOVE_SLOW;
	
	public final static int ANIMATION_DEAYLE = 1;

	public View mMoveView = null;
	public Context mCon = null;
	public boolean mTranslate;//是否在移动中
	public boolean mDelay;//表示是否在延迟中
	public Queue<View> focusQueue=new LinkedList<View>();//view队列
	final int[] nextLocations = new int[2];// 目的view 所在位置
	final int[] currentLocations = new int[2];// 背景 view 所在位置
	final float[] nextWH = new float[2];// 目的view 宽高
	final float[] currentWH = new float[2];// 背景 view 宽高
	public long firstTime,lastTime;

	private AnimationDrawable mAnimation;
	private Animation alpha_all , alpha_path; //alpha_all只呼吸 　alpha_path为呼吸加变大

	public MoveControl(Context con,View moveView){
		this.mCon=con;
		mMoveView=moveView;
		alpha_all = AnimationUtils.loadAnimation(mCon, R.anim.float_alpha_all);
		alpha_path = AnimationUtils.loadAnimation(mCon, R.anim.float_alpha_path);
	}
	
	
	/* start--stop */
	public void startAnimation(){
		mTranslate=false;
		mMoveView.setVisibility(View.VISIBLE);
		mMoveView.setBackgroundResource(R.anim.focus_frame);
		mAnimation = (AnimationDrawable) mMoveView.getBackground();
		mAnimation.setOneShot(false);
		mAnimation.start();
		mMoveView.invalidate();
		mMoveView.bringToFront();
		mMoveView.startAnimation(alpha_all); //只有呼吸框
	}
	public void stopAnimation(){
		mMoveView.clearAnimation();
		mMoveView.setBackgroundResource(R.drawable.item_focus_25);
	}
	
	public void hideMoveView(){
		stopAnimation();
		mMoveView.setVisibility(View.INVISIBLE);
	}
	
	
	public void addFocusView(View v){
		focusQueue.add(v);
		if(!mTranslate){
			transform(focusQueue.peek(),true);
		}
	}
	
	public Handler handler=new Handler(){
		public void handleMessage(Message msg) {
            switch(msg.what){
            case ANIMATION_DEAYLE:
            	View tempView=(View)msg.obj;
            	if(nextWH[0]==0||nextWH[1]==0||nextLocations[0]==0||nextLocations[1]==0){
            		tempView.getLocationInWindow(nextLocations);
                	nextWH[0] = tempView.getWidth();
            		nextWH[1] = tempView.getHeight();
            	}
            	mMoveView.setVisibility(View.VISIBLE);
    			MarginLayoutParams lp = (MarginLayoutParams) mMoveView.getLayoutParams();
    			lp.width = (int) nextWH[0];
    			lp.height = (int) nextWH[1];
    			lp.leftMargin = nextLocations[0];
    			lp.topMargin = nextLocations[1];
    			mMoveView.setLayoutParams(lp);
    			focusQueue.poll();
    			mDelay=false;
    			if(focusQueue.size()<=0){
    				startAnimation();
    			}else{
    				transform(focusQueue.peek(),true);
    			}
            	break;
            }
		}
	};

	
	public void transform(View nextView, boolean bol) {
		mTranslate=true;
		stopAnimation();
//		nextView.getLocationOnScreen(nextLocations);
		nextView.getLocationInWindow(nextLocations);
		
		MarginLayoutParams newPrarm=(MarginLayoutParams)mMoveView.getLayoutParams();
		currentLocations[0]=newPrarm.leftMargin;
		currentLocations[1]=newPrarm.topMargin;

		nextWH[0] = nextView.getWidth();
		nextWH[1] = nextView.getHeight();
		
		currentWH[0]=newPrarm.width;
		currentWH[1]=newPrarm.height;

		float dx = nextLocations[0] - currentLocations[0];
		float dy = nextLocations[1] - currentLocations[1];
		float sx = nextWH[0] / (currentWH[0]);
		float sy = nextWH[1] / (currentWH[1]);
		if(nextView.getAlpha()==AnimUtils.VIEW_ALPHA_FLAG){
			bol=false;
			mMoveView.setVisibility(View.INVISIBLE);
			nextView.setAlpha(AnimUtils.VIEW_ALPHA_NORMAL);
		}
		if (bol) {
			AppLog.d(TAG, "dx="+dx+"  dy="+dy+"  sx="+sx+"  sy="+sy);
			/** ----------------------------------------------- */
//			int new_W = nextView.getWidth();
//			int new_H = nextView.getHeight();
//			int old_W = newPrarm.width;
//			int old_H = newPrarm.height;
			
			AnimationSet animationSet = new AnimationSet(false);
			
			TranslateAnimation tAnimation = new TranslateAnimation(0.0F,dx, 0.0F,dy);
			tAnimation.setDuration(200);
			ScaleAnimation sAnimation = new ScaleAnimation(1.0F, sx, 1.0F, sy);
			sAnimation.setDuration(200);
			
			animationSet.addAnimation(sAnimation);
			animationSet.addAnimation(tAnimation);
			animationSet.setFillEnabled(true);
			animationSet.setFillAfter(true);
			animationSet.setStartOffset(0);
			animationSet.setStartTime(0);
			animationSet.setAnimationListener(new AnimationListener() {
				public void onAnimationStart(Animation animation) {}
				public void onAnimationRepeat(Animation animation) {}
				public void onAnimationEnd(Animation animation) {
					mMoveView.setVisibility(View.VISIBLE);
					MarginLayoutParams lp = (MarginLayoutParams) mMoveView.getLayoutParams();
					lp.width = (int) nextWH[0];
					lp.height = (int) nextWH[1];
					lp.leftMargin = nextLocations[0];
					lp.topMargin = nextLocations[1];
					mMoveView.setLayoutParams(lp);
					focusQueue.poll();
					int size=focusQueue.size();
					if(size<=0){
						startAnimation();
					}else{
						if(size>1){
							ANIMATION_MOVE_TIME=AnimUtils.ANIMATION_MOVE_FAST;
						}else{
							ANIMATION_MOVE_TIME=AnimUtils.ANIMATION_MOVE_SLOW;
						}
						transform(focusQueue.peek(),true);
					}
				}
			});
			mMoveView.startAnimation(animationSet);
			mMoveView.bringToFront();
			mMoveView.invalidate();
			nextView.invalidate();
			/** ----------------------------------------------- */
			
//			ScaleTransAnimation scaleanimation = new ScaleTransAnimation(dx, dy, sx, sy);
//			scaleanimation.setFillEnabled(true);
//			scaleanimation.setFillAfter(true);
//			scaleanimation.setStartOffset(0);
//			scaleanimation.setStartTime(0);
//			scaleanimation.setAnimationListener(new AnimationListener() {
//				@Override
//				public void onAnimationStart(Animation animation) {}
//				@Override
//				public void onAnimationRepeat(Animation animation) {}
//				@Override
//				public void onAnimationEnd(Animation animation) {
//					mMoveView.setVisibility(View.VISIBLE);
//					MarginLayoutParams lp = (MarginLayoutParams) mMoveView.getLayoutParams();
//					lp.width = (int) nextWH[0];
//					lp.height = (int) nextWH[1];
//					lp.leftMargin = nextLocations[0]+1;
//					lp.topMargin = nextLocations[1]+1;
//					mMoveView.setLayoutParams(lp);
//					focusQueue.poll();
//					int size=focusQueue.size();
//					if(size<=0){
//						startAnimation();
//					}else{
//						if(size>1){
//							ANIMATION_MOVE_TIME=AnimUtils.ANIMATION_MOVE_FAST;
//						}else{
//							ANIMATION_MOVE_TIME=AnimUtils.ANIMATION_MOVE_SLOW;
//						}
//						transform(focusQueue.peek(),true);
//					}
//				}
//			});
//			scaleanimation.setDuration(100);
//			mMoveView.bringToFront();
//			mMoveView.invalidate();
//			nextView.invalidate();
		} else {
			mDelay=true;
			Message mesg=handler.obtainMessage(ANIMATION_DEAYLE);
			mesg.obj=nextView;
			handler.sendMessageDelayed(mesg, AnimUtils.ANIMATION_DEAYLE_TIME);
		}

	}
}
