package com.joysee.launcher.common;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.Gallery;

import com.joysee.launcher.activity.R;
import com.joysee.launcher.utils.LauncherLog;

public class Coverflow extends Gallery {
	
	private static final String TAG = "com.joysee.launcher.common.Coverflow";
	private Bitmap mArrowLeft;
	private Bitmap mArrowRight;
	
	private CoverflowItem mLastSelectItem = null;

	public Coverflow(Context context) {
		this(context,null);
	}

	public Coverflow(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public Coverflow(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setAnimationDuration(400);
		mArrowLeft = BitmapFactory.decodeResource(getResources(), R.drawable.launcher_coverflow_arrow_left);
		mArrowRight = BitmapFactory.decodeResource(getResources(), R.drawable.launcher_coverflow_arrow_right);
		
	}
	
	long mLastKeyDownTime = -1;
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
			if(System.currentTimeMillis() - mLastKeyDownTime <= 500){
				return true;
			}
			mLastKeyDownTime = System.currentTimeMillis();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
			mLastKeyDownTime = -1;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
//		canvas.save();
		canvas.drawBitmap(mArrowLeft, 770, 58, null);
		canvas.drawBitmap(mArrowRight, 1124, 58, null);
	}
	
	@Override
	protected boolean getChildStaticTransformation(View child, Transformation t) {
		
		
		
		if(child == getSelectedView()){
			if(child instanceof CoverflowItem){
				CoverflowItem item = (CoverflowItem)child;
				LauncherLog.log_D(TAG, "child == getSelectedView()   tag = " + item.getTag());
				item.setItemSelect(true);
				if(mLastSelectItem != null && mLastSelectItem != item)
					mLastSelectItem.setItemSelect(false);
				mLastSelectItem = item;
			}
		}else{
			LauncherLog.log_D(TAG, "child != getSelectedView()   tag = " + child.getTag());
		}
		return true;
	}
}
