package com.bestv.ott.appstore.animation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;

import com.bestv.ott.appstore.R;

public class SearchAnimationButton extends LinearLayout {
	private Context mContext;
	private AnimationDrawable mAnimation;
	private ImageView mImageView;
	private ViewGroup.LayoutParams lp=null ;
	private ImageView mBackImageView;//移动图片  需外部设置
	private boolean mHave=true;//默认为true 做动画

	public SearchAnimationButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init(context);
//		this.setBackgroundResource(R.drawable.frame_animation_list_center);
	}

	public SearchAnimationButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
		mContext = context;
//		this.setBackgroundResource(R.drawable.frame_animation_list_center);
	}
	
	public void setmHave(boolean bol){
		this.mHave = bol;
	}

	public SearchAnimationButton(Context context) {
		super(context);
		init(context);
		mContext = context;
//		this.setBackgroundResource(R.drawable.frame_animation_list_center);
	}
	
	private void init(Context context){
		this.mContext = context;
		this.mImageView = new ImageView(mContext);
		//mImageView.setImageResource(R.drawable.app_default_img);
		// some config
		setClickable(true);      
		setFocusable(true); 
		setOrientation(LinearLayout.VERTICAL); 
		setPadding(13,14,13,13);
		setGravity(Gravity.CENTER);
		mImageView.setAdjustViewBounds(true);
		addView(mImageView); 
	}
	
	
	public void setImageView(ImageView image){
		this.mBackImageView = image;
	}
	
	public void startAnimation(){
		this.setBackgroundResource(R.anim.frame_list_animation_center);
		mAnimation = (AnimationDrawable) this.getBackground();
		mAnimation.setOneShot(false);
		mAnimation.start();
	}
	
	public void stopAnimation(){
		if(mAnimation!=null){
			mAnimation.stop();
		}
		this.setBackgroundResource(R.drawable.search_app_back);
	}
	
	public void setImageURI(Uri uri){
		mImageView.setImageURI(uri);
	}
	
	public void setImageRes(int resId){
		mImageView.setImageResource(resId);
	}
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
//				}
//			}
//		}else{
//			stopAnimation();
//		}
//	}
	
}
