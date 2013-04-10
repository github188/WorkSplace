package com.joysee.adtv.ui;

import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DefaultParameter.ServiceType;
import com.joysee.adtv.common.DvbLog;
import com.joysee.adtv.common.ThreadPoolManager;
import com.joysee.adtv.controller.ViewController;
import com.joysee.adtv.logic.bean.NETEventInfo;
import com.joysee.adtv.logic.bean.ProgramType;
import com.joysee.adtv.ui.Menu.InterceptKeyListener;
import com.joysee.adtv.ui.Menu.MenuListener;

public class MenuProgramGuide extends LinearLayout implements OnClickListener,MenuListener{

	private TextView mMenuLiveGuideTextView;
	private TextView mMenuWeekGuideTextView;
	private RelativeLayout mRocommendView1;
	private RelativeLayout mRocommendView2;
	private NETEventInfo mNetEventInfo1;
	private NETEventInfo mNetEventInfo2;
	private ImageView mFocusView;
	private ViewController mController;
	private int mOffset = (int) getResources().getDimension(R.dimen.menu_margin_top);
	public MenuProgramGuide(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public MenuProgramGuide(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MenuProgramGuide(Context context) {
		super(context);
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int keyCode = event.getKeyCode();
		int action = event.getAction();
		
		System.out.println(" MenuProgramGuide dispatchKeyEvent");
		if(mInterceptKeyListener.onKeyEvent(keyCode,action)){
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	
	@Override
	protected void onFinishInflate() {
		mMenuLiveGuideTextView = (TextView) findViewById(R.id.menu_pg_live_guide_textivew);
		mMenuWeekGuideTextView = (TextView) findViewById(R.id.menu_pg_week_guide_textivew);
		mFocusView = (ImageView) findViewById(R.id.ivFocus);
		mRocommendView1 = (RelativeLayout) findViewById(R.id.recommend_program1);
		mRocommendView2 = (RelativeLayout) findViewById(R.id.recommend_program2);
		
		mMenuLiveGuideTextView.setOnFocusChangeListener(onFocusChangeListener);
		mMenuWeekGuideTextView.setOnFocusChangeListener(onFocusChangeListener);
		mRocommendView1.setOnFocusChangeListener(onFocusChangeListener);
		mRocommendView2.setOnFocusChangeListener(onFocusChangeListener);
		
		mRocommendView1.setOnClickListener(this);
		mRocommendView2.setOnClickListener(this);
		mMenuLiveGuideTextView.setOnClickListener(this);
		mMenuWeekGuideTextView.setOnClickListener(this);
		mMenuLiveGuideTextView.requestFocus();
		
		super.onFinishInflate();
	}
	
	private InterceptKeyListener mInterceptKeyListener;
	public void setInterceptKeyListener(InterceptKeyListener interceptKeyListener){
		mInterceptKeyListener = interceptKeyListener;
	}
	
	@Override
	public void getFocus(){
		mMenuLiveGuideTextView.requestFocus();
		getRecommendData();
	}
	
	@Override
	public void loseFocus() {
		mRocommendView1.setVisibility(View.INVISIBLE);
		mRocommendView2.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.menu_pg_live_guide_textivew:
			Log.d("songwenxuan","menu_pg_live_guide_textivew onclick!!!");
			mController.showLiveGuide();
			mInterceptKeyListener.exitMenu();
			break;
		case R.id.menu_pg_week_guide_textivew:
			Log.d("songwenxuan","menu_pg_week_guide_textivew onclick!!!");
			mController.showProgramGuide();
			mInterceptKeyListener.exitMenu();
			break;
		case R.id.recommend_program1:
			Log.d("songwenxuan","recommend_program1 onclick!!!");
			if(mNetEventInfo1 != null){
				mController.switchChannelFromNum(ServiceType.TV, mNetEventInfo1.getLogicNumer());
			}
			break;
		case R.id.recommend_program2:
			Log.d("songwenxuan","recommend_program2 onclick!!!");
			if(mNetEventInfo2 != null){
				mController.switchChannelFromNum(ServiceType.TV, mNetEventInfo2.getLogicNumer());
			}
			break;
		default:
			break;
		}
	}
	private OnFocusChangeListener onFocusChangeListener = new OnFocusChangeListener() {
		
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if(hasFocus ){
				if(v instanceof TextView){
					int [] location = new int [2];
					v.getLocationInWindow(location);
					if(location[1] == 0)
						return;
					MarginLayoutParams params = (MarginLayoutParams) mFocusView
							.getLayoutParams();
					params.topMargin = location[1]-mOffset;
					
					Log.d("songwenxuan","onFocusChange() , params.topMargin = " + params.topMargin);
					mFocusView.setLayoutParams(params);
					mFocusView.setVisibility(View.VISIBLE);
				}else{
					mFocusView.setVisibility(View.INVISIBLE);
				}
				
//				Animation anim = new AlphaAnimation(0.0f, 1.0f);
//				anim.setDuration(300);
//				anim.setFillAfter(true);
//				anim.setFillEnabled(true);
//				mFocusView.startAnimation(anim);
			}
		}
	};
	
	public void setController(ViewController controller){
		this.mController = controller;
	}

	public void getRecommendData() {
		mNetEventInfo1 = null;
		mNetEventInfo2 = null;
		ArrayList<ProgramType> types = new ArrayList<ProgramType>();
		types = mController.getProgramTypes(0xFF);
		for (ProgramType programType : types) {
			DvbLog.D("wgh", programType.toString());
		}
		ArrayList<NETEventInfo> mProgramInfoList = new ArrayList<NETEventInfo>();
		if(types.size() <1 )
			return;
		mProgramInfoList = mController.getProgramList(
				types.get(0)
                        .getId(), mController.getUtcTime(), 0);
		for (int i=0;i<mProgramInfoList.size();i++) {
			NETEventInfo netEventInfo = mProgramInfoList.get(i);
			DvbLog.D("wgh", netEventInfo.toString());
			if(i==0){
				mRocommendView1.setVisibility(View.VISIBLE);
				mNetEventInfo1 = netEventInfo;
				setData(mRocommendView1,netEventInfo);
			}else if(i==1){
				mRocommendView2.setVisibility(View.VISIBLE);
				mNetEventInfo2 = netEventInfo;
				setData(mRocommendView2,netEventInfo);
			}else{
				break;
			}
		}
	}
	private void setData(View view,NETEventInfo eventInfo){
		ImageView previewImage = (ImageView) view.findViewById(R.id.cell_view_preview_iv);
		ImageView progressImage = (ImageView) view.findViewById(R.id.cell_view_progress_iv);
		ImageView beginIconImage = (ImageView) view.findViewById(R.id.cell_view_begin_icon);
		TextView programName = (TextView) view.findViewById(R.id.cell_view_program_tv);
		TextView channelName = (TextView) view.findViewById(R.id.cell_view_channel_tv);
		programName.setText(eventInfo.getEname());
		channelName.setText(eventInfo.getChannelName());
		RelativeLayout.LayoutParams para = (RelativeLayout.LayoutParams) progressImage.getLayoutParams();
		long mCurrentTime = mController.getUtcTime()/1000;
		long beginTime = eventInfo.getBegintime();
		if(beginTime >= mCurrentTime){
			beginIconImage.setVisibility(View.VISIBLE);
			progressImage.setVisibility(View.GONE);
		}else{
			beginIconImage.setVisibility(View.GONE);
			float progress ;
			DvbLog.D("wgh", eventInfo.getDuration()+" duration");
			if(eventInfo.getDuration() <= 0){
				progress = 0;
			}else{
				progress = (float) (mCurrentTime - eventInfo.getBegintime())/eventInfo.getDuration();
			}
			if(progress < 0.02f)
				progress = 0.02f;
			DvbLog.D("wgh", progress+"");
			para.width = (int) (390 * progress);
			progressImage.setLayoutParams(para);
		}
		
		setRecommendImage(previewImage,eventInfo.getImgPath());
		
	}

	private void setRecommendImage(final ImageView previewImage, final String url) {
		if(url == null){
			previewImage.setImageResource(R.drawable.guide_poster);
			return;
		}
		
		new AsyncTask<Void, Void, Bitmap>(){
			@Override
			protected Bitmap doInBackground(Void... params) {
				HttpURLConnection conn=null;
				InputStream input=null;
				try{
					URL mUrl=new URL(url);
//			Log.d(TAG, "url: "+url.toString());
					conn = (HttpURLConnection) mUrl.openConnection();
					conn.connect();
					
					if(conn.getResponseCode()<300){
						input = conn.getInputStream();
						Bitmap bmp = BitmapFactory.decodeStream(input);
//				Log.d(TAG, "get bitmap "+url+" ("+bmp.getWidth()+"x"+bmp.getHeight()+")");
//				getBitmapCache().addBitmap(url, bmp);
						return bmp;
					}else{
//				Log.d(TAG, "download bitmap failed! http return "+conn.getResponseCode());
//				setError(ADTVError.CANNOT_GET_BITMAP);
					}
				}catch(ConnectException e){
//			Log.d(TAG, "connect to download bitmap failed!"+e.getMessage());
//			setError(ADTVError.CANNOT_CONNECT_TO_SERVER);
				}catch (MalformedURLException e) {
//		    Log.d(TAG, "url is error"+e.getMessage());
//            e.printStackTrace();
//            setError(ADTVError.CANNOT_CONNECT_TO_SERVER);
				}catch(Exception e){
//            Log.d(TAG, "download bitmap failed! "+e.getMessage());
//            setError(ADTVError.CANNOT_GET_BITMAP);
				}finally{
					try{
						if(input!=null)
							input.close();
						if(conn!=null)
							conn.disconnect();
					}catch(Exception e){
					}
				}
				return null;
			}

			@Override
			protected void onPostExecute(Bitmap result) {
				super.onPostExecute(result);
				if(result !=null ){
					previewImage.setImageBitmap(result);
				}else{
					previewImage.setImageResource(R.drawable.guide_poster);
				}
			}
			
		}.executeOnExecutor(ThreadPoolManager.getExecutor());
	}
}
