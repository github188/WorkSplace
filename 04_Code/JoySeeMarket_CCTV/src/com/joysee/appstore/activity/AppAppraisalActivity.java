package com.joysee.appstore.activity;

import com.joysee.appstore.thread.ScoreThread;
import com.joysee.appstore.utils.AppLog;
import com.joysee.appstore.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.Layout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

/**
 * 透明窗体评价窗口
 * 
 * @author benz
 */
public class AppAppraisalActivity extends Activity {
	
	//各个等级评分值
	private float ONE = 1/2;
	private float TWO = 1;
	private float THREE = 3/2;
	private float FOUR = 4/2;
	private float FIVE = 5/2;
	private float SIX = 6/2;
	private float SEVEN = 7/2;
	private float EIGHT = 8/2;
	private float NINE = 9/2;
	private float TEN = 10/2;
	private float ZERO = 0;
	
	private static final String TAG = "AppAppraisalActivity";
	protected static final long MOVE_WIDGET_DISPATCH = 20;
	protected static final int VISIBLE = 100;
	protected static final int NO_VISIBLE = 0;
	protected static final int CLOSE = 1;
	private RatingBar mRatingBar = null;
	private ImageButton mCloseButton;
//	private TextView mRatingBarText;
	private TextView mTextView1;
	private TextView mTextView2;
	private Intent intent;
	private float mScoreValue;//记录评分值
	private Boolean mHaveBeenEvaluated = false;//是否已评价,由服务器通知
	private Boolean mIsMove = false; //用户是否移动过评分
	private int mAppId; //应用id
	private String mUserID; //用户id
	private int mAmount; //已评人数
	private Context context;
    private String mUserInfo;//用户信息
    private Boolean mIsSuer = false;//是否点过确认键
//    private AnimationDrawable animationDrawable; 
//    private LinearLayout bar;
    
    public HandlerThread workThread;
    public Handler workHandler;
    Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			finish();
		}
    	
    };

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.app_appraisal);
		setupViews();
	}
	
	/**
	 * 初始化
	 */
	public void setupViews() {
		
		intent = getIntent();
		mAppId = intent.getIntExtra("app_id", 0);//获取对应app 的ID
		mAmount = intent.getIntExtra("amount", 0); //评论人数
		mUserID = intent.getStringExtra("user_id");//用户id
		mHaveBeenEvaluated = intent.getBooleanExtra("isScore", false);//获取是否评分
		mScoreValue =  intent.getFloatExtra("ScoreValue", 0);//获取已评分数
		
//		if("-1".equals(mUserID)){ //祥情里没获取userid,则默认此时已评价（不提交评分信息）
//			mHaveBeenEvaluated = true;
//		}
		
		mRatingBar = (RatingBar) findViewById(R.id.ratingBar);
//		bar = (LinearLayout) findViewById(R.id.bar);
//		mRatingBarText = (TextView) findViewById(R.id.text);
//		animationDrawable = (AnimationDrawable) bar.getBackground();
		
		if(mHaveBeenEvaluated){ //已评价
			evaluated();
			//mRatingBar.setFocusable(false);//让星星失去焦点
			AppLog.d("benz","-------------------------id--"+mAppId +"--已评价----------------");
		}else{
			mRatingBar.setRating(mScoreValue);
			mRatingBar.setOnRatingBarChangeListener(new RatingBarListener());
			AppLog.d("benz","-------------------------id--"+mAppId +"--未评价----------------");
		}
		//每次都让评价
		//mRatingBar.setOnRatingBarChangeListener(new RatingBarListener());
		
		mCloseButton = (ImageButton) findViewById(R.id.close);
		mTextView1 = (TextView) findViewById(R.id.text_1);//已有多少人评价
		mTextView2 = (TextView) findViewById(R.id.text_2);//使用说明

		mCloseButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (v.getId() == R.id.close) {
					mCloseButton.setBackgroundResource(R.drawable.close_click);
					mCloseButton.postDelayed(new Runnable() {
						public void run() {
							mCloseButton.setBackgroundResource(R.drawable.close_focus);
						}
					}, 250);
					mCloseButton.postDelayed(new Runnable() {
						public void run() {
							finish();
						}
					}, 650);
				}
			}
		});
//		setCloseFocuseChange(mCloseButton); //去掉按钮特效
		mRatingBar.requestFocus();
//		animationDrawable.start();
		mRatingBar.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mIsSuer = true;
				mTextView1.setGravity(Gravity.CENTER);
				mTextView1.setText(AppAppraisalActivity.this.getString(R.string.app_appraisal_text1,mAmount+1));
				handler.sendEmptyMessageDelayed(CLOSE, 700);
			}
		});
		setBarFocuseChange(mRatingBar);
		
		
		mRatingBar.setOnKeyListener(new OnKeyListener() {
			
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				
				if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
					if(mScoreValue == TEN){ //评分到达最右边时,不让焦点往上到达"close"
						AppLog.d(TAG, "-------mScoreValue == TEN------return true----------");
						return true;
					}
				}
				return false;
			}
		});
	}

	
	/**
	 * 已评价时，根据获取的评分进行显示
	 */
	private void evaluated() {
		if(!"-1".equals(mUserID)){
			
		}
		//Toast.makeText(AppAppraisalActivity.this, "你已评分", 0).show();
		mRatingBar.setRating(mScoreValue);
//		if(mScoreValue >= ZERO && mScoreValue <= TWO){
//			mRatingBarText.setText(R.string.bad_one);
//		}else if(mScoreValue >= THREE && mScoreValue <= FOUR){
//			mRatingBarText.setText(R.string.bad_two);
//		}else if(mScoreValue >= FIVE && mScoreValue <= SIX){
//			mRatingBarText.setText(R.string.bad_thr);
//		}else if(mScoreValue >= SEVEN && mScoreValue <= EIGHT){
//			mRatingBarText.setText(R.string.good);
//		}else if(mScoreValue >= NINE && mScoreValue <= TEN){
//			mRatingBarText.setText(R.string.very_good);
//		}
	}
	
	/**
	 * RatingBar监听
	 */
	private class RatingBarListener implements
			RatingBar.OnRatingBarChangeListener {

		public void onRatingChanged(RatingBar ratingBar, float rating,
				boolean fromUser) {
			AppLog.d("benz", "--------------------RatingBar------正在评价-------");
			mIsMove = true;
			if (rating >= ZERO && rating <= TWO) {
//				mRatingBarText.setText(R.string.bad_one);
				mScoreValue = rating;
			} else if (rating >= THREE && rating <= FOUR) {
//				mRatingBarText.setText(R.string.bad_two);
				mScoreValue = rating;
			} else if (rating >= FIVE && rating <= SIX) {
//				mRatingBarText.setText(R.string.bad_thr);
				mScoreValue = rating;
			} else if (rating >= SEVEN && rating <= EIGHT) {
//				mRatingBarText.setText(R.string.good);
				mScoreValue = rating;
			} else if (rating >= NINE && rating <= TEN) {
//				mRatingBarText.setText(R.string.very_good);
				mScoreValue = rating;
			} else if(rating == ZERO){
				mScoreValue = rating;
			}
			
		}

	}

	/**
	 * 关闭按钮特效
	 */
	private void setCloseFocuseChange(View view) {
		view.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			AnimationDrawable draw = null;
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					((ImageView) v).setImageResource(R.drawable.close_selected_animation);
					draw = (AnimationDrawable) ((ImageView) v).getDrawable();
					draw.start();
				} else {
					if (null != draw && draw.isRunning()) {
						((ImageView) v).setImageDrawable(null);
						draw.stop();
					}
				}
			}
		});
	}

	//Bar动效
	private void setBarFocuseChange(final View view) {
//		view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//
//			public void onFocusChange(View v, boolean hasFocus) {
//				if (hasFocus) {
//					animationDrawable.setAlpha(VISIBLE);
//					animationDrawable.start();
//				}else{
//					animationDrawable.setAlpha(NO_VISIBLE);
//					animationDrawable.stop();
//				}
//			}
//			
//		});
	}
	
	protected void onPause() {
		super.onPause();
		//if(mIsMove && mIsSuer){ 
		if(mIsSuer){ 
			AppLog.d("benz", "----------------RatingBar is close!!!------The scoring results  :--"+mScoreValue*2+"---");
			new ScoreThread(mUserID, mAppId, mScoreValue*2,AppAppraisalActivity.this).start();
		}else{
			AppLog.d("benz", "----------------------------May score or no score------------------------------------");
		}
		
		Intent intent = new Intent();
		intent.putExtra("OK", 1);
		this.setResult(RESULT_OK, intent);
		this.finish();
	}
}
