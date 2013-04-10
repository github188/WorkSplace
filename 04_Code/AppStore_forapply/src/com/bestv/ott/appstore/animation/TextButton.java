package com.bestv.ott.appstore.animation;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bestv.ott.appstore.R;

public class TextButton extends LinearLayout {
	private TextView mTextView;
	private Context mContext;
	private ImageView mBackImageView;// move picture need config 
	public TextButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public TextButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public TextButton(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context){
		this.mContext = context;
		this.mTextView = new TextView(mContext);
		this.setPadding(15, 10, 15, 10);

		// some config
		setClickable(true);      
		setFocusable(true); 
		setOrientation(LinearLayout.VERTICAL); 
		setGravity(Gravity.CENTER);
		mTextView.setGravity(Gravity.CENTER);
		mTextView.setTextSize(22);
		addView(mTextView); 
	}
	
	public void setBackImageView(ImageView imageview){
		this.mBackImageView = imageview;
	}
	
	public void setTextColor(int color){
		this.mTextView.setTextColor(color);
	}
	
	public void setButtonBackImage(int resid){
		if(resid==-1){
			this.mTextView.setBackgroundDrawable(null);
		}else{
			this.mTextView.setBackgroundResource(resid);
		}
	}
	
	public void setText(String str){
		mTextView.setText(str);
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
//					
//				}
//			}
//		}else{
//		}
//		
//	}
	
	
}
