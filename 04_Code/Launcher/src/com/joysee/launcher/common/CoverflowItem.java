package com.joysee.launcher.common;

import com.joysee.launcher.activity.R;
import com.joysee.launcher.utils.LauncherLog;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class CoverflowItem extends FrameLayout {
	
	private static final String TAG = "com.joysee.launcher.common.CoverflowItem";

	private ImageView mImage;
	private ImageView mSelectImage;
	private TextView mItemTitle;
	
	private Bitmap mImageRes;
	private Bitmap mSelectImageRes;
	private String mItemTitleRes;
	
	public Intent appIntent;
	
	
	
	public CoverflowItem(Context context) {
		super(context);
	}

	public CoverflowItem(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CoverflowItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
		mImage = (ImageView) findViewById(R.id.coverflow_item_pic);
		mSelectImage = (ImageView) findViewById(R.id.coverflow_item_pic_select);
		mItemTitle = (TextView) findViewById(R.id.coverflow_item_title);
		
	}
	
	public void setItemSelect(boolean b){
		LauncherLog.log_D(TAG, " param   b " + b);
		if(b){
			this.mImage.setBackgroundDrawable(new BitmapDrawable(mSelectImageRes));
		}else{
			this.mImage.setBackgroundDrawable(new BitmapDrawable(mImageRes));
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		canvas.drawBitmap(mImageRes,0,0,null);
	}

	public ImageView getImage() {
		return mImage;
	}

	public void setImage(ImageView mImage) {
		this.mImage = mImage;
	}

	public ImageView getSelectImage() {
		return mSelectImage;
	}

	public void setSelectImage(ImageView mSelectImage) {
		this.mSelectImage = mSelectImage;
	}

	public TextView getItemTitle() {
		return mItemTitle;
	}

	public void setItemTitle(TextView mItemTitle) {
		this.mItemTitle = mItemTitle;
	}

	public Bitmap getImageRes() {
		return mImageRes;
	}

	public void setImageRes(Bitmap mImageRes) {
		this.mImageRes = mImageRes;
	}

	public Bitmap getSelectImageRes() {
		return mSelectImageRes;
	}

	public void setSelectImageRes(Bitmap mSelectImageRes) {
		this.mSelectImageRes = mSelectImageRes;
	}

	public String getItemTitleRes() {
		return mItemTitleRes;
	}

	public void setItemTitleRes(String mItemTitleRes) {
		this.mItemTitleRes = mItemTitleRes;
	}
	
}
