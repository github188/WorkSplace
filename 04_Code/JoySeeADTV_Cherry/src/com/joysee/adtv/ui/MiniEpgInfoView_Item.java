package com.joysee.adtv.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class MiniEpgInfoView_Item extends LinearLayout{

	private boolean mAdding;
	private int mOffset = 0;
	
	public MiniEpgInfoView_Item(Context context) {
		super(context);
	}
	
	public MiniEpgInfoView_Item(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MiniEpgInfoView_Item(Context context, AttributeSet attrs,int defStyle) {
		super(context, attrs, defStyle);
	}

	
	protected void dispatchDraw(Canvas canvas) {
		if(mAdding){
			canvas.translate(0, -mOffset);
		}
		super.dispatchDraw(canvas);
	}
	
	public void setAction(boolean isAdding){
		mAdding = isAdding;
	}
	
	public void setOffset(int offset){
		mOffset = offset;
	}
}
