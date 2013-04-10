package com.bestv.ott.appstore.animation;

import com.bestv.ott.appstore.R;
import com.bestv.ott.appstore.common.Constants;
import com.bestv.ott.appstore.utils.AppLog;

import android.content.Context;
import android.graphics.Rect;
import android.text.InputFilter;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class EditTextButton  extends LinearLayout {
	private EditText mTextView;
	private Context mContext;
	private ImageView mBackImageView;// move picture need config 
	public static boolean isH = false;
	
	public EditTextButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public EditTextButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public EditTextButton(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context){
		this.mContext = context;
		this.mTextView = new EditText(mContext);

		// some config
		setClickable(true);      
		//setFocusable(true); 
		setOrientation(LinearLayout.VERTICAL); 
		setGravity(Gravity.CENTER);
		mTextView.setGravity(Gravity.LEFT);
		mTextView.setTextSize(20);
		mTextView.setHeight(40);
		mTextView.setSingleLine();
		mTextView.setEnabled(true);
		mTextView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Constants.MAX_TEXT_INPUT_LENGTH)});
		mTextView.setBackgroundResource(R.drawable.search_edittext);
		addView(mTextView); 
		mTextView.setOnFocusChangeListener(new OnFocusChangeListener(){
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus){
					String tex=mTextView.getText().toString();
					if(tex!=null&&tex.length()>0){
						mTextView.setSelection(tex.length());
					}
				}else{
					if(isH){
						InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE); 
						imm.hideSoftInputFromWindow(mTextView.getWindowToken(),0);
						isH = false;
					}
				}
			}
		});
	}
	
	public void setBackImageView(ImageView imageview){
		this.mBackImageView = imageview;
	}
	
	public void setRequestFocus(){
		mTextView.requestFocus();
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
	
	public EditText getEditText(){
		return mTextView;
	}
	
	public void setText(String str){
		mTextView.setText(str);
	}
	
	public String getText(){
		return mTextView.getText().toString();
	}
//	
//	@Override
//	protected void onFocusChanged(boolean gainFocus, int direction,
//			Rect previouslyFocusedRect) {
//		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
//		if(gainFocus){
//			mTextView.requestFocus();
//			if(mBackImageView!=null){
//				synchronized (mBackImageView) {
//					AnimationControl control = AnimationControl.getInstance();
//					synchronized(control){
//						control.transformAnimationForImage(mBackImageView, EditTextButton.this, mContext,true,false);
//					}
//					
//				}
//			}
//		}else{
//		}
//		
//	}
	
	
}
