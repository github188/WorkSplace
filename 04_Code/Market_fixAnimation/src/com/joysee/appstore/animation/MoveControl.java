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
	final int[] Loc2s = new int[2];// 目的view 所在位置
	final int[] Locback = new int[2];// 背景 view 所在位置
	final float[] Dimen2s = new float[2];// 目的view 宽高
	final float[] Dimenback = new float[2];// 背景 view 宽高
	public long firstTime,lastTime;

	private AnimationDrawable mAnimation;
	private Animation alpha_all , alpha_path; //alpha_all只呼吸 　alpha_path为呼吸加变大
	private Animation appImage , appName;
	private View bigView , bigTextView , smallView;
	private View viewParent;

	public MoveControl(Context con,View moveView){
		this.mCon=con;
		mMoveView=moveView;
		appImage = AnimationUtils.loadAnimation(mCon, R.anim.scale_image);
		appName = AnimationUtils.loadAnimation(mCon, R.anim.scale_text);
		alpha_all = AnimationUtils.loadAnimation(mCon, R.anim.float_alpha_all);
		alpha_path = AnimationUtils.loadAnimation(mCon, R.anim.float_alpha_path);
	}
	
	
	/* start--stop */
	public void startAnimation(){
		mTranslate=false;
		mMoveView.setVisibility(View.VISIBLE);
		mMoveView.setBackgroundResource(R.anim.float_frame);
		mAnimation = (AnimationDrawable) mMoveView.getBackground();
		mAnimation.setOneShot(false);
		mAnimation.start();
		
		if(bigView!=null){
			Object obj = bigView.getTag();
			if(obj!=null && AppsBean.class.isInstance(obj)){
				trunBigApp();//　所有都变大+呼吸框
			}else{
//				alpha_all.setFillEnabled(true);
//				alpha_all.setFillAfter(true);
				mMoveView.bringToFront();
				mMoveView.startAnimation(alpha_all); //只有呼吸框
			}
		}
	}
	public void stopAnimation(){
		mMoveView.clearAnimation();
		mMoveView.setBackgroundResource(R.drawable.selected_application_background100);
		if(bigView!=null){
			trunSmallApp();
		}
	}
	
	
	/* 变大变小 */
	public synchronized void trunBigApp(){
		bigTextView = (View)viewParent.findViewById(R.id.item_name);
		if(bigTextView!=null){
			bigTextView.startAnimation(appName);
		}
		bigView.startAnimation(appImage);
//		alpha_path.setFillEnabled(true);
//		alpha_path.setFillAfter(true);
		mMoveView.bringToFront();
		mMoveView.startAnimation(alpha_path);
	}
	public synchronized void trunSmallApp(){
		bigView.clearAnimation();
		bigView = null;
		if(bigTextView!=null){
			bigTextView.clearAnimation();
			bigTextView = null;
		}
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
            	if(Dimen2s[0]==0||Dimen2s[1]==0||Loc2s[0]==0||Loc2s[1]==0){
            		tempView.getLocationInWindow(Loc2s);
                	Dimen2s[0] = tempView.getWidth();
            		Dimen2s[1] = tempView.getHeight();
            	}
//        		Log.d(TAG, "Loc2s[0]="+Loc2s[0]+";Loc2s[1]="+Loc2s[1]+";Dimen2s[0]="+Dimen2s[0]+";Dimen2s[1]="+Dimen2s[1]);
            	mMoveView.setVisibility(View.VISIBLE);
    			MarginLayoutParams lp = (MarginLayoutParams) mMoveView.getLayoutParams();
    			lp.width = (int) Dimen2s[0];
    			lp.height = (int) Dimen2s[1];
    			lp.leftMargin = Loc2s[0];
    			lp.topMargin = Loc2s[1];
    			mMoveView.setLayoutParams(lp);
    			bigView = focusQueue.poll();
    			viewParent = (View) bigView.getParent();
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
		nextView.getLocationInWindow(Loc2s);
		MarginLayoutParams newPrarm=(MarginLayoutParams)mMoveView.getLayoutParams();
//		Log.d(TAG, "newPrarm.leftMargin="+newPrarm.leftMargin+";newPrarm.topMargin="+newPrarm.topMargin+";newPrarm.width="+newPrarm.width+";newPrarm.heigh="+newPrarm.height);
		Locback[0]=newPrarm.leftMargin;
		Locback[1]=newPrarm.topMargin;

		Dimen2s[0] = nextView.getWidth();
		Dimen2s[1] = nextView.getHeight();
		Dimenback[0]=newPrarm.width;
		Dimenback[1]=newPrarm.height;
		
//		Log.d(TAG, "Loc2s[0]="+Loc2s[0]+";Loc2s[1]="+Loc2s[1]+";Dimen2s[0]="+Dimen2s[0]+";Dimen2s[1]="+Dimen2s[1]);
//		Log.d(TAG, "Locback[0]="+Locback[0]+";Locback[1]="+Locback[1]+";Dimenback[0]="+Dimenback[0]+";Dimenback[1]="+Dimenback[1]);

		float dx = Loc2s[0] - Locback[0];
		float dy = Loc2s[1] - Locback[1];
		float sx = Dimen2s[0] / (Dimenback[0]);
		float sy = Dimen2s[1] / (Dimenback[1]);
//		Log.d(TAG, "dx="+dx+";dy="+dy+";sx="+sx+";sy="+sy);
//		AppLog.d(TAG, "---bol="+bol+";nextView.getAlpha()="+nextView.getAlpha()+";mMoveView.getVisibility()="+mMoveView.getVisibility());
		if(nextView.getAlpha()==AnimUtils.VIEW_ALPHA_FLAG){
			bol=false;
			mMoveView.setVisibility(View.INVISIBLE);
			nextView.setAlpha(AnimUtils.VIEW_ALPHA_NORMAL);
		}
//		AppLog.d(TAG, "---bol="+bol+";nextView.getAlpha()="+nextView.getAlpha()+";mMoveView.getVisibility()="+mMoveView.getVisibility()+";ANIMATION_MOVE_TIME="+ANIMATION_MOVE_TIME);
		if (bol) {
			ScaleTransAnimation scaleanimation = new ScaleTransAnimation(dx, dy, sx, sy);
			scaleanimation.setFillEnabled(true);
			scaleanimation.setFillAfter(true);
			scaleanimation.setStartOffset(0);
			scaleanimation.setStartTime(0);
			scaleanimation.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {}
				@Override
				public void onAnimationRepeat(Animation animation) {}
				@Override
				public void onAnimationEnd(Animation animation) {
					mMoveView.setVisibility(View.VISIBLE);
					MarginLayoutParams lp = (MarginLayoutParams) mMoveView.getLayoutParams();
					lp.width = (int) Dimen2s[0];
					lp.height = (int) Dimen2s[1];
					lp.leftMargin = Loc2s[0];
					lp.topMargin = Loc2s[1];
//					Log.d(TAG, "lp.width="+lp.width+";lp.height="+lp.height+";lp.leftMargin="+lp.leftMargin+";lp.topMargin="+lp.topMargin);
					mMoveView.setLayoutParams(lp);
//					MarginLayoutParams newPrarm=(MarginLayoutParams)mMoveView.getLayoutParams();
//					Log.d(TAG, "leftMargin="+newPrarm.leftMargin+";newPrarm.topMargin="+newPrarm.topMargin+";newPrarm.width"+newPrarm.width+";newPrarm.height"+newPrarm.height);
					bigView = focusQueue.poll();
					viewParent = (View) bigView.getParent();
					int size=focusQueue.size();
//					Log.d(TAG, "---focusQueue.size()="+size);
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
			scaleanimation.setDuration(ANIMATION_MOVE_TIME);
			mMoveView.bringToFront();
			mMoveView.invalidate();
			nextView.invalidate();
			mMoveView.startAnimation(scaleanimation);
//			lastTime=System.currentTimeMillis();
//			AppLog.d(TAG, "take time="+(lastTime-firstTime));
		} else {
			mDelay=true;
			Message mesg=handler.obtainMessage(ANIMATION_DEAYLE);
			mesg.obj=nextView;
			handler.sendMessageDelayed(mesg, AnimUtils.ANIMATION_DEAYLE_TIME);
		}

	}
	
}
