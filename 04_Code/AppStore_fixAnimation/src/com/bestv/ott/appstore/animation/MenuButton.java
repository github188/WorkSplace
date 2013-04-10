package com.bestv.ott.appstore.animation;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bestv.ott.appstore.R;
import com.bestv.ott.appstore.utils.AppLog;

public class MenuButton extends LinearLayout {
	private ImageView mImageView;
	private TextView mTextView;
	private Context mContext;
	private AnimationDrawable mAnimation;
	private ImageView mBackImageView;// move picture need config 
	public MenuButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public MenuButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public MenuButton(Context context) {
		super(context);
		init(context);
		
		this.setLayoutParams(new ViewGroup.LayoutParams(130,130));
	}

	private void init(Context context){
		this.mContext = context;
		this.mImageView = new ImageView(mContext);
		this.mTextView = new TextView(mContext);
        this.setPadding(15, 15, 15, 15);
		// some config
		setClickable(true);      
		setFocusable(true); 
		setBackgroundColor(Color.alpha(0));
		setOrientation(LinearLayout.VERTICAL); 
		setGravity(Gravity.CENTER);
		mTextView.setGravity(Gravity.CENTER_HORIZONTAL);
		mTextView.setTextColor(Color.BLACK);
		mTextView.setTextSize(17);
		addView(mImageView); 
		addView(mTextView); 
	}
	
	public void setBackImageView(ImageView imageview){
		this.mBackImageView = imageview;
	}
	
	public void setImageResource(int resId){
		mImageView.setImageResource(resId);
	}
	
	public void setImageURI(Uri uri){
		System.gc();
		Bitmap oldBitmap = null;
		Drawable oldDrawable = mImageView.getDrawable();
		if(oldDrawable!=null &&  oldDrawable instanceof BitmapDrawable){
		     oldBitmap = ((BitmapDrawable) oldDrawable).getBitmap();
		} 
		InputStream is = null;
		try {
			is = new FileInputStream(uri.getPath());
			BitmapFactory.Options options=new BitmapFactory.Options();
			options.inJustDecodeBounds = false;
			options.inPreferredConfig =Config.ARGB_8888;
			Bitmap newBitmap =BitmapFactory.decodeStream(is,null,options);
			if(newBitmap==null||newBitmap.getByteCount()<0||newBitmap.getRowBytes()<0){
				File imageF=new File(uri.getPath());
				if(imageF.exists()){
					imageF.delete();
				}
				mImageView.setImageResource(R.drawable.menu_default_img);
			}else{
				mImageView.setImageBitmap(newBitmap);
			}
	    } catch (Exception e) {//如果转换出错，表示图片有问题，删除此图片
	    	AppLog.d("com.bestv.ott.appsore.animation.MunuButton", "-------Exception-----error");
			e.printStackTrace();
			File imageF=new File(uri.getPath());
			if(imageF.exists()){
				imageF.delete();
			}
			mImageView.setImageResource(R.drawable.menu_default_img);
		}catch(Error e){
			AppLog.d("com.bestv.ott.appsore.animation.MunuButton", "--------Error----error");
			e.printStackTrace();
			File imageF=new File(uri.getPath());
			if(imageF.exists()){
				imageF.delete();
			}
			mImageView.setImageResource(R.drawable.menu_default_img);
		}
		if (oldBitmap!=null  && !oldBitmap.isRecycled()) {
		     oldBitmap.recycle();
		     System.gc();
		}
			
	}
	
	public void setText(String str){
		mTextView.setText(str);
	}

	
	public void startAnimation(){
		mAnimation = (AnimationDrawable) this.getBackground();
		mAnimation.setOneShot(false);
		mAnimation.start();
		
	}
	
	public void stopAnimation(){
		mAnimation.stop();
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
