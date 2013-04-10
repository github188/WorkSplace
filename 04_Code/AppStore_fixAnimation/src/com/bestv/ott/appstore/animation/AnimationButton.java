package com.bestv.ott.appstore.animation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.bestv.ott.appstore.R;
import com.bestv.ott.appstore.utils.AppLog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class AnimationButton extends LinearLayout {
	private Context mContext;
	private AnimationDrawable mAnimation;
	private ImageView mImageView;
	private ViewGroup.LayoutParams lp=null ;
	private ImageView mBackImageView;//移动图片  需外部设置
	private boolean mHave=true;//默认为true 做动画
	private static ArrayList bitmapList = new ArrayList();  

	public AnimationButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init(context);
		this.setBackgroundResource(R.drawable.frame_animation_list_center);
	}

	public AnimationButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
		mContext = context;
		this.setBackgroundResource(R.drawable.frame_animation_list_center);
	}
	
	public void setmHave(boolean bol){
		this.mHave = bol;
	}

	public AnimationButton(Context context) {
		super(context);
		init(context);
		mContext = context;
		this.setBackgroundResource(R.drawable.frame_animation_list_center);
	}
	
	private void init(Context context){
		this.mContext = context;
		this.mImageView = new ImageView(mContext);
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
		this.setBackgroundResource(R.drawable.frame_animation_list_center);
	}
	
	/*分类页用此方法*/
	public void setImageURI(Uri uri){
//	    Bitmap oldBitmap = null;
//	    Drawable oldDrawable = mImageView.getDrawable();
//	    if(oldDrawable!=null &&  oldDrawable instanceof BitmapDrawable){
//	        oldBitmap = ((BitmapDrawable) oldDrawable).getBitmap();
//	    }
		File imageE=new File(uri.getPath());
		if(!imageE.isFile()||!imageE.exists()){
			mImageView.setImageResource(R.drawable.app_default_img);
			return;
		}
	    InputStream is = null;
		try {
			is = new FileInputStream(uri.getPath());
		    BitmapFactory.Options options=new BitmapFactory.Options();
		    options.inJustDecodeBounds = false;
		    options.inPreferredConfig =Config.ARGB_8888;
//		    options.inSampleSize = 2; 
		    Bitmap newBitmap =BitmapFactory.decodeStream(is,null,options);
		    if(newBitmap==null||newBitmap.getByteCount()<0||newBitmap.getRowBytes()<0){
		    	File imageF=new File(uri.getPath());
				if(imageF.exists()){
					imageF.delete();
				}
				mImageView.setImageResource(R.drawable.app_default_img);
		    }else{
		    	mImageView.setImageBitmap(newBitmap);
		    }
		} catch (Exception e) {//如果转换出错，表示图片有问题，删除此图片
			AppLog.d("com.bestv.ott.appsore.animation.AnimationButton", "-------Exception-----error");
			e.printStackTrace();
			File imageF=new File(uri.getPath());
			if(imageF.exists()){
				imageF.delete();
			}
			mImageView.setImageResource(R.drawable.app_default_img);
		}catch(Error e){
			AppLog.d("com.bestv.ott.appsore.animation.AnimationButton", "------Error-----error");
			e.printStackTrace();
			File imageF=new File(uri.getPath());
			if(imageF.exists()){
				imageF.delete();
			}
			mImageView.setImageResource(R.drawable.app_default_img);
		}finally{
			if(is!=null){
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
			
//	    if (oldBitmap!=null  && !oldBitmap.isRecycled()) {
//	        oldBitmap.recycle();
//	        System.gc();
//	        AppLog.d("AnimationButton", "-------------oldBitmap.recycle()--------------");
//	    }
	}
	
	
	/*首页用此方法,在此方法内对首页bitmap进行对象记录,以便最后回收*/
	public void setMainImageURi(Uri uri){
		File imageE=new File(uri.getPath());
		if(!imageE.isFile()||!imageE.exists()){
			mImageView.setImageResource(R.drawable.app_default_img);
			return;
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
				mImageView.setImageResource(R.drawable.app_default_img);
		    }else{
		    	mImageView.setImageBitmap(newBitmap);
		    	if(null!=bitmapList){
		    		bitmapList.add(newBitmap);  //记录已绘制的bitmap
		    	}else{
		    		AppLog.d("com.bestv.ott.appsore.animation.AnimationButton", "----------------bitmapList=null");
		    		bitmapList = new ArrayList();
		    		bitmapList.add(newBitmap);  //记录已绘制的bitmap
		    	}
		    }
		} catch (Exception e) {//如果转换出错，表示图片有问题，删除此图片
			AppLog.d("com.bestv.ott.appsore.animation.AnimationButton", "-------Exception-----error");
			e.printStackTrace();
			File imageF=new File(uri.getPath());
			if(imageF.exists()){
				imageF.delete();
			}
			mImageView.setImageResource(R.drawable.app_default_img);
		}catch(Error e){
			AppLog.d("com.bestv.ott.appsore.animation.AnimationButton", "------Error-----error");
			e.printStackTrace();
			File imageF=new File(uri.getPath());
			if(imageF.exists()){
				imageF.delete();
			}
			mImageView.setImageResource(R.drawable.app_default_img);
		}finally{
			if(is!=null){
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	public void setImageRes(int resId){
		 Bitmap oldBitmap = null;
	     Drawable oldDrawable = mImageView.getDrawable();
	     if(oldDrawable!=null &&  oldDrawable instanceof BitmapDrawable){
	         oldBitmap = ((BitmapDrawable) oldDrawable).getBitmap();
	     } 
	     InputStream is = this.getResources().openRawResource(resId);
	     BitmapFactory.Options options=new BitmapFactory.Options();
	     options.inJustDecodeBounds = false;
	     options.inPreferredConfig =Config.ARGB_8888;
	     Bitmap newBitmap =BitmapFactory.decodeStream(is,null,options);
	     mImageView.setImageBitmap(newBitmap);
	     if (oldBitmap!=null  && !oldBitmap.isRecycled()) {
	         oldBitmap.recycle();
	         System.gc();
	     } 
	}
	
	public ImageView getImageView(){
		return mImageView;
	}
	
	
	/*手动回收内存*/
	public static void recycleMemory(){
		if(null!=bitmapList){
			AppLog.d("AnimationButton", "------------bitmapList.size  : "+bitmapList.size() +" --------");
			for(int i = 0; i < bitmapList.size(); i++){
				Bitmap bitmap = (Bitmap) bitmapList.get(i);
				//释放bitmap所占的内存  
				if (!bitmap.isRecycled()) {  
					bitmap.recycle(); 
					bitmap = null;
					AppLog.d("AnimationButton", "----------------recycleMemory-----------------");
				}  
//				bitmapList.remove(i);  
			}
			bitmapList = null;
		}
	}

			
}
