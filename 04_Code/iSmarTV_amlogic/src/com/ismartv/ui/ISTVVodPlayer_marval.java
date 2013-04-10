package com.ismartv.ui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import android.app.Activity;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.MessageQueue.IdleHandler;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.widget.*;
import android.view.*;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.graphics.PixelFormat;
import android.graphics.Canvas;
import android.view.animation.AnimationUtils;
import android.graphics.drawable.AnimationDrawable;
import android.widget.AdapterView.OnItemClickListener;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.ismartv.doc.ISTVVodPlayerDoc;
import com.ismartv.doc.ISTVResource; 
import com.ismartv.util.JniSetting; 
import com.ismartv.util.Constants; 
import com.sven.auth.HttpClient;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * 用反射获取videoview中的mediaplayer，并设置OnInfoListener,用于非amlogic平台
 *
 */
public class ISTVVodPlayer_marval extends ISTVVodActivity{
	private static final String TAG="ISTVVodPlayer";

	private static final String PREFS_NAME="com.ismartv.ui.play";
	
	private static final int PANEL_HIDE_TIME=6;
	private static final int SHORT_FB_STEP=30000;
	private static final int SHORT_FF_STEP=30000;
	private static final int LONG_FB_PERCENT=20;
	private static final int LONG_FF_PERCENT=20;
	private static final int KEY_REPEAT_COUNT=4;
	private static final int BUFFER_TIMEOUT_TIME = 120; //120 s

	public final int RES_URL_CLIP_ADAPTIVE=0;
	public static final int RES_URL_CLIP_LOW=1;
	public static final int RES_URL_CLIP_MEDIUM=2;
	public static final int RES_URL_CLIP_NORMAL=3;
	public static final int RES_URL_CLIP_HIGH=4;
	public static final int RES_URL_CLIP_ULTRA=5;
	public static final int RES_STR_ITEM_TITLE=6;
	public static final int RES_INT_CLIP_LENGTH=7;
	public static final int RES_BOOL_BOOKMARKED=8;
	public static final int RES_INT_OFFSET=9;
	public static final int RES_INT_EPISODE_REALCOUNT = 10;

	protected static final int NET_BE_AVAILABLE = 11;

	protected static final int NET_NOT_AVAILABLE = 12;
	
	protected static final int REFRESH_BUFF_PRECENT = 13;
	
	protected static final int REFRESH_BUFF_INIT = 14;

	private ISTVVodPlayerDoc doc;
	private int itemPK=18821;
	private int subItemPK=-1;
//	private URL urls[] = new URL[6];
	private String urls[] = new String[6];
	private String itemTitle;

	private int clipLength=0;
	private int currPosition=0;
	private int clipOffset=0;
	private boolean clipBookmarked=false;
	private int episoderealcount;

//	private URL currURL = null;
	private String currURL = null;
	private int currQuality=0;
	private boolean urlUpdate=false;
	private boolean playing=false;
	private boolean prepared=false;
	private boolean buffering=false;
	private boolean seeking=false;
	private boolean needSeek=false;
	private boolean paused=false;
	private boolean panelShow=false;
	private boolean bufferShow=false;
	private int panelHideCounter=0;

	private Animation panelShowAnimation;
	private Animation panelHideAnimation;
	private Animation bufferShowAnimation;
	private Animation bufferHideAnimation;


	private VideoView videoView;
	private RelativeLayout bufferLayout;
	private TextView buff_precent;//缓冲百分比
	private AnimationDrawable bufferAnim;
	private LinearLayout panelLayout;
	private TextView titleText;
	private TextView timeText;
	private TextView mHDText;
	
	private TextView totalTimeText;
	private SeekBar timeBar;
	private ImageView playPauseImage;
	private ImageView ffImage;
	private ImageView fbImage;

	private boolean keyOKDown=false;
	private boolean keyLeftDown=false;
	private boolean keyRightDown=false;
	private boolean seekBarDown=false;	
	private int keyLeftRepeat=0;
	private int keyRightRepeat=0;

	private VodTimer playerTimer;
	private VodTimer storeTimer;
	private boolean firstPlay;

	private RelativeLayout ui_ratingbarmenubg;
	private TextView ui_PopupRateText;
	
	private int popupstatus = 0;
	/*private VodTimer replayTimer;
	private boolean replaytimerflag = false;*/

	private boolean isContinue=true;
	private boolean isChangeUrl = false;
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case NET_BE_AVAILABLE:
				Log.d(TAG, "--------->>>>>>>>>> net is available");
				rePlayByTimeout();
				break;
			case NET_NOT_AVAILABLE:
				Log.d(TAG, "--------->>>>>>>>>>  net is not available");
				showPopupDialog(DIALOG_NET_BROKEN, getResources().getString(R.string.vod_net_broken_error));
				break;
			case REFRESH_BUFF_PRECENT:
				int percent = msg.arg1;
				/* 大于100还在缓冲,显示98% */
				if(percent>100 && buffering){
					percent = 98;
				}
				/* 大于100没有缓冲,显示100% */
				if(percent > 100 && !buffering){
					percent = 100;
				}
				/* 小于100没有缓冲,显示100% */
				if(percent<100 && !buffering){
					percent = 100;
				}
				buff_precent.setText(percent +"%");
				break;
			case REFRESH_BUFF_INIT:
				buff_precent.setText("0%");
				break;
			default:
				break;
			}
		};
	};
	
	public void showBuffer(){
		if(!bufferShow){
			
			/* 缓冲提示弹出时,把缓冲进百分比初始化为0% */
			buff_precent.setText("0%");
//			mHandler.sendEmptyMessage(REFRESH_BUFF_INIT);
			
			Log.d(TAG, "show buffer");
			bufferLayout.startAnimation(bufferShowAnimation);
			bufferLayout.setVisibility(View.VISIBLE);
			bufferAnim.start();
			bufferShow=true;
		}
	}

	public void hideBuffer(){
		if(bufferShow && !buffering && !seeking){
			Log.d(TAG, "hide buffer");
			bufferLayout.startAnimation(bufferHideAnimation);
			bufferLayout.setVisibility(View.GONE);
			bufferAnim.stop();
			bufferShow=false;
		}
	}

	public void showSuccess(String text) {
		if(ui_ratingbarmenubg.getVisibility() == View.GONE) {
			ui_PopupRateText.setText(text);
			ui_ratingbarmenubg.setVisibility(View.VISIBLE);
			mHandler.postDelayed(new Runnable(){

				@Override
				public void run() {
					hideSuccess();					
				}
				
			}, 1000) ;
		}
	}
	
	public void hideSuccess() {
		if(ui_ratingbarmenubg.getVisibility() == View.VISIBLE) {
			ui_ratingbarmenubg.setVisibility(View.GONE);
			ui_PopupRateText.setText("");
		}
	}
	
	public void onCreate(Bundle savedInstanceState){
		/*before super, because onCreateVodMenu use subItemPK*/
		Bundle bundle = this.getIntent().getExtras();
		if(bundle!=null){
			itemPK = bundle.getInt("itemPK", itemPK);
			subItemPK = bundle.getInt("subItemPK", subItemPK);
		}
		
		super.onCreate(savedInstanceState);

		episoderealcount = 0;
		
		firstPlay=true;
		itemTitle=null;
		clipLength=0;
		currPosition=0;
		currURL = null;
		currQuality=0;
		urlUpdate=false;
		playing=false;
		prepared=false;
		seeking=false;
		buffering=false;
		panelShow=false;
		bufferShow=false;
		keyOKDown=false;
		keyLeftDown=false;
		keyRightDown=false;
		isPlayer=true;
		
		isContinue = getSharedPreferences(PREFS_NAME, 0).getBoolean("continue_play", true);
		Log.d(TAG, "isContinue="+isContinue); 
		
		panelShowAnimation = AnimationUtils.loadAnimation(this, R.anim.fly_up);
		panelHideAnimation = AnimationUtils.loadAnimation(this, R.anim.fly_down);
		bufferShowAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
		bufferHideAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		setContentView(R.layout.vod_player);

		videoView = (VideoView)findViewById(R.id.VideoView);

		ui_ratingbarmenubg = (RelativeLayout)findViewById(R.id.successlayout);
		ui_PopupRateText = (TextView)findViewById(R.id.PopupRateText);
		
		panelLayout = (LinearLayout)findViewById(R.id.PanelLayout);
		titleText = (TextView)findViewById(R.id.TitleText);
		timeText = (TextView)findViewById(R.id.TimeText);
		mHDText = (TextView)findViewById(R.id.HDText);
		totalTimeText = (TextView)findViewById(R.id.TotalTimeText);
		timeBar = (SeekBar)findViewById(R.id.TimeSeekBar);
		playPauseImage = (ImageView)findViewById(R.id.PlayPauseImage);
		ffImage = (ImageView)findViewById(R.id.FFImage);
		fbImage = (ImageView)findViewById(R.id.FBImage);

		playPauseImage.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				if(!keyOKDown){
					if(!paused){
						pauseItem();
						playPauseImage.setImageResource(R.drawable.vod_player_play);
					}else{
						resumeItem();
						playPauseImage.setImageResource(R.drawable.vod_player_pause_focus);
					}
					showPanel();
				}
			}
		});
		ffImage.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				if(!keyRightDown){
					showPanel();
					fastForward(SHORT_FF_STEP);
					seekTo();
				}
			}
		});
		fbImage.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				if(!keyLeftDown){
					showPanel();
					fastBackward(SHORT_FB_STEP);
					seekTo();
				}
			}
		});
		timeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
			
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
				if(fromUser){
					currPosition = progress*clipLength/100;
					onDocUpdate();
				}
			}
			public void onStartTrackingTouch(SeekBar seekBar){
				seekBarDown = true;
			}
			public void onStopTrackingTouch(SeekBar seekBar){
				seekBarDown = false;
				showPanel();
				seekTo();
			}
		});

		bufferLayout = (RelativeLayout)findViewById(R.id.BufferLayout);
		buff_precent = (TextView)findViewById(R.id.buff_precent);
		bufferAnim = (AnimationDrawable)((ImageView)findViewById(R.id.BufferImage)).getBackground();
		showBuffer();

		videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){
				public void onPrepared(MediaPlayer mp) {
					hideBuffer();
					Log.d(TAG, "video prepared");
					if(!prepared){
						prepared=true;
						if(currPosition!=0){
							seekTo();
						}
					}
				}
			});

		
		videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
				public void onCompletion(MediaPlayer mp) {
					Log.d(TAG, "!!!!!!!!!video complete");			
					onDocUpdate();
					clipOffset = -1;					
					storeOffset();
					
					int sub = doc.getNextSubItemPK();
					if(sub!=-1){
						doc.setItemPK(itemPK, sub);
						for(int i=0; i<6; i++){
							urls[i]=null;
						}
						itemTitle=null;
						clipLength=0;
						currPosition=0;
						currURL = null;
						currQuality=0;
						urlUpdate=false;
						playing=false;
						prepared=false;
						seeking=false;
						buffering=false;
						firstPlay=false;						
						showBuffer();
					}else{
						gotoplayerfinishpage(itemPK);
					}
				}
			}); 

		videoView.setOnErrorListener(new MediaPlayer.OnErrorListener(){
				public boolean onError (MediaPlayer mp, int what, int extra)  {

					if(playing && prepared){
					//stopVideo();

					//replaytimerflag = false;
					popupstatus = 1;
					showPopupDialog(DIALOG_OK_CANCEL, getResources().getString(R.string.vod_player_error_dialog));

					/*if(!(doc.isNetConnected())){
						replaytimerflag = true;
						if(replayTimer!=null){
							replayTimer = addVodTimer(2, 60);
						}
					}*/
					}
					
					return true;
				}
		}); 

		doc = new ISTVVodPlayerDoc(itemPK, subItemPK);
		setDoc(doc);
		playerTimer = addVodTimer(0, 1);
		storeTimer = addVodTimer(1, 60);

		showPanel();
//		showBuffer();
	}

	public void onDestroy(){
		doc.dispose();
		super.onDestroy();
	}

	private void seekComplete(){
		if(!seeking)
			return;

		seeking=false;
		if(needSeek){
			seekTo();
		}
		hideBuffer();
		if(!seeking){
			Log.d(TAG, "seek complete");
		}
	}
/*
	private URL getClipURL(){
		URL url = urls[currQuality];

		if(url!=null)
			return url;

		for(int i=0; i<6; i++){
			if(urls[i]!=null){
				currQuality = i;
				return urls[i];
			}
		}

		return null;
	}
*/
	private String getClipURL(){
		String url = urls[currQuality];
		if(url!=null){
			mHDText.setText(getHDString(currQuality + 1));
			mHDText.setBackgroundResource(R.drawable.hd_text_bg);
			Log.d(TAG, "-------> currQuality ="+currQuality);
			Log.d(TAG, "  ----->"+this.getString(getHDString(currQuality + 1)));
			return url;
		}
		for(int i=0; i<6; i++){
			if(urls[i]!=null){
				currQuality = i;
				int resId = getHDString(currQuality + 1) ;
				if(resId != -1){
					mHDText.setText(resId);
					mHDText.setBackgroundResource(R.drawable.hd_text_bg);
				}
				return urls[i];
			}else{
				mHDText.setText("");
			}
		}

		return null;
	}
	
	private void setQuality(int q){
		if(q!=currQuality){
			currQuality = q;
			urlUpdate = true;
			isChangeUrl = true;
			onDocUpdate();
			
			/* 在onDocUpdate中已设，故注掉 */
//			//set quality text for player
//			int resId = getHDString(currQuality+1) ;
//			if(resId != -1){
//				mHDText.setText(resId);
//				mHDText.setBackgroundResource(R.drawable.hd_text_bg);
//			}
		}
	}

	protected void onDocGotResource(ISTVResource res){
		switch(res.getType()){
			case RES_URL_CLIP_ADAPTIVE:
			case RES_URL_CLIP_LOW:
			case RES_URL_CLIP_MEDIUM:
			case RES_URL_CLIP_NORMAL:
			case RES_URL_CLIP_HIGH:
			case RES_URL_CLIP_ULTRA:
				Log.d(TAG, "RES_URL_CLIP_ULTRA="+res.getString());
				//urls[res.getType()] = res.getURL();
				urls[res.getType()] = res.getString();
				urlUpdate = true;
				break;
			case RES_STR_ITEM_TITLE:
				{
					showPanel();
					itemTitle = res.getString();
					titleText.setText(itemTitle);
					titleText.setSelected(true);
				}
				break;
			case RES_INT_CLIP_LENGTH:				
				//clipLength = res.getInt()*1000;
				Log.d(TAG, "RES_INT_CLIP_LENGTH=" + clipLength);
				break;
			case RES_BOOL_BOOKMARKED:
				clipBookmarked = res.getBoolean();
				break;
			case RES_INT_OFFSET:
				clipOffset = res.getInt();
				if(clipOffset>0 && firstPlay && isContinue) {
					currPosition = clipOffset*1000;
				} else {
					clipOffset = 0;
				}
				Log.d(TAG, "RES_INT_OFFSET currPosition="+currPosition+",clipOffset="+clipOffset+",firstPlay="+firstPlay);
				break;

			case RES_INT_EPISODE_REALCOUNT:
				episoderealcount = res.getInt();
				
			default:
				break;
		}
	}

	//reset quality text
	private int getHDString(int pos){
		int resId = -1 ;
		 		
		switch(pos){
		case 1:
			resId = R.string.vod_player_quality_adaptive; 
			break ;
		case 2:
			resId = R.string.vod_player_quality_low;
			break ;
		case 3:
			resId = R.string.vod_player_quality_medium;
			break ;
		case 4:
			resId = R.string.vod_player_quality_normal; 
			break ;
		case 5:
			resId = R.string.vod_player_quality_high; 
			break ;
		case 6:
			resId = R.string.vod_player_quality_ultra;
			break ;
		default:
			resId = -1 ;
			break ;
		}
		Log.d(TAG,"##### getHDString> resId:" + resId + "/pos:" + pos );
		return resId ;
	} 
	
	private String getTimeString(int ms){
		int left = ms;
		int hour = left/3600000;
		left %= 3600000;
		int min = left/60000;
		left %= 60000;
		int sec = left/1000;

		return String.format("%1$02d:%2$02d:%3$02d", hour, min, sec);
	}

	protected void onDocUpdate() {
		/*Redraw the activity*/
		if(urlUpdate){
			startVideo();
			urlUpdate = false;
		}

		if(clipLength>0){
			
			timeText.setText(getTimeString(currPosition));
			String text = "/"+getTimeString(clipLength);
			totalTimeText.setText(text);
			int val = currPosition*100/clipLength;
			//设置进度
			timeBar.setProgress(val);
		}
	}

	private void showPanel(){
		if(isVodMenuVisible())
			return;

		if(!panelShow){ 
			panelLayout.startAnimation(panelShowAnimation);
			panelLayout.setVisibility(View.VISIBLE);
			panelShow=true;
		}
		panelHideCounter = PANEL_HIDE_TIME;
	}

	private void hidePanel(){
		if(panelShow){
			panelLayout.startAnimation(panelHideAnimation);
			panelLayout.setVisibility(View.GONE);
			panelShow=false;
		}
	}

	private void pauseItem(){
		if(paused)
			return;

		Log.d(TAG, "pause");
		videoView.pause();
		paused = true;
	}

	private void resumeItem(){
		if(!paused)
			return;

		videoView.start();
		Log.d(TAG, "resume");
		paused = false;
	}

	private void fastForward(int step){
		if(currPosition == clipLength||clipLength<=0)
			return;
		currPosition += step;

		if(currPosition>clipLength)
			currPosition = clipLength;

		Log.d(TAG,"ff "+currPosition);
		onDocUpdate();
	}

	private void fastBackward(int step){
		if(currPosition == 0||clipLength<=0)
			return;
		currPosition -= step;

		if(currPosition<0)
			currPosition = 0;

		Log.d(TAG,"fb "+currPosition);
		onDocUpdate();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean ret = false;

		switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
				if(!isVodMenuVisible()){
					if(!keyLeftDown){
						keyLeftDown = true;
						keyLeftRepeat = 0;
						fbImage.setImageResource(R.drawable.vod_player_fb_focus);
						showPanel();
					}else{
						keyLeftRepeat++;
					}

					if(keyLeftRepeat==0){
						fastBackward(SHORT_FB_STEP);
					}else if((keyLeftRepeat%KEY_REPEAT_COUNT)==0){
						if(clipLength!=0)
							fastBackward(clipLength/LONG_FB_PERCENT);
						else
							fastBackward(SHORT_FB_STEP);
					}
					ret = true;
				}
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if(!isVodMenuVisible()){
					if(!keyRightDown){
						keyRightDown = true;
						keyRightRepeat = 0;  
						ffImage.setImageResource(R.drawable.vod_player_ff);
						showPanel();
					}else{
						keyRightRepeat++;
					}

					if(keyRightRepeat==0){
						fastForward(SHORT_FF_STEP);
					}else if((keyRightRepeat%KEY_REPEAT_COUNT)==0){
						if(clipLength!=0)
							fastForward(clipLength/LONG_FF_PERCENT);
						else
							fastForward(SHORT_FF_STEP);
					}
					ret = true;
				}
				break;

			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER:
				if(!isVodMenuVisible()){
					if(!keyOKDown){
						if(!paused){
							pauseItem();
							playPauseImage.setImageResource(R.drawable.vod_player_play);
						}else{
							resumeItem();
							playPauseImage.setImageResource(R.drawable.vod_player_pause_focus);
						}
						showPanel();
						keyOKDown = true;
					}
					ret = true;
				}
				break;
			case KeyEvent.KEYCODE_A:
			case KeyEvent.KEYCODE_F1:
			case KeyEvent.KEYCODE_PROG_RED:
				if(!isVodMenuVisible()){
					if(panelShow){
						hidePanel();
					} else {
						showPanel();
					}
					ret = true;
				}
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
				if(!isVodMenuVisible()){
					showPanel();
					ret = true;
				}
				break;
			case KeyEvent.KEYCODE_BACK:
				if(panelShow){
					hidePanel();
					ret = true;
				}else{
					popupstatus = 2;
					showPopupDialog(DIALOG_OK_CANCEL, getResources().getString(R.string.vod_player_exit_dialog));
					ret = true;
				}
				break;
			default:
				break;
		}

		if(ret==false){
			ret = super.onKeyDown(keyCode, event);
		}

		return ret;
	}

	public void seekTo(){
		Log.d(TAG, "seekTo currPosition="+currPosition);
		if(!prepared || !playing)
			return;
		if(!seeking){
			if(currPosition<0)
				currPosition=0;
			else if(currPosition>clipLength && clipLength != 0) {
				currPosition = clipLength;
				gotoplayerfinishpage(itemPK);
			}
			videoView.seekTo(currPosition);
			needSeek=false;
		}else{
			needSeek=true;
		}
		buffering=true;
		seeking=true;
//		showBuffer();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean ret = false;

		switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
				if(!isVodMenuVisible()){
					fbImage.setImageResource(R.drawable.vod_player_fb);
					Log.d(TAG,"seek to "+currPosition);
					seekTo();
					keyLeftDown=false;
					ret = true;
				}
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if(!isVodMenuVisible()){
					ffImage.setImageResource(R.drawable.vod_player_ff_focus);
					Log.d(TAG,"seek to "+currPosition);
					seekTo();
					keyRightDown=false;
					ret = true;
				}
				break;
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER:
				if(!isVodMenuVisible()){
					if(paused){
						playPauseImage.setImageResource(R.drawable.vod_player_play_focus);
					}else{
						playPauseImage.setImageResource(R.drawable.vod_player_pause);
					}
					keyOKDown = false;
					ret = true;
				}
				break;
			default:
				break;
		}

		if(ret==false){
			ret = super.onKeyUp(keyCode, event);
		}

		return ret;
	}

	@Override
	public boolean onTouchEvent(MotionEvent evt){
		boolean ret = false;

		switch(evt.getActionMasked()){
			case MotionEvent.ACTION_DOWN:
				if(!panelShow){
					showPanel();
					ret = true;
				}
				break;
			default:
				break;
		}

		return ret;
	}

	private void startVideo(){
		//URL url = getClipURL();
		String url = getClipURL();
		if((url!=null) && ((currURL==null) || !(url.equals(currURL)))){
			if(playing){
				videoView.stopPlayback();
				//videoView.pause();
				showBuffer();
			}

			Log.d(TAG, "play URL "+url.toString() + ",currPosition="+currPosition);		
			
			videoView.setVideoPath(url.toString());
			Class c = null;
			try {
				c = Class.forName("android.widget.VideoView");
			} catch (ClassNotFoundException e1) {
				Log.e(TAG, "", e1);
			}
			Field f = null;
			MediaPlayer mPlayer = null;
			try {
				Log.i(TAG, "mPlayer = " + mPlayer);
				f = c.getDeclaredField("mMediaPlayer");
				f.setAccessible(true);
				mPlayer = (MediaPlayer) f.get(videoView);
				Log.i(TAG, "mPlayer = " + mPlayer);
			} catch (Exception e) {
				Log.i(TAG, "e = " + e.getMessage());
				Log.e(TAG, "", e);
				e.printStackTrace();
			}
			if (mPlayer != null) {
				mPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
					public boolean onInfo(MediaPlayer mp, int what, int extra) {
						switch (what) {
						case MediaPlayer.MEDIA_INFO_BUFFERING_START:
							Log.d(TAG, "buffering start");
							buffering = true;
							showBuffer();
							break;
						case MediaPlayer.MEDIA_INFO_BUFFERING_END:
							Log.d(TAG, "buffering end");
							buffering = false;
							seekComplete();
							hideBuffer();
							if (isChangeUrl) {
								seekTo();
								isChangeUrl = false;
								buffering = false;
							}
							break;
						case 222:
							Log.d("TAG", "buffering  = " + extra);
							Message msg = new Message();
							msg.what = REFRESH_BUFF_PRECENT;
							msg.arg1 = extra;
							mHandler.sendMessage(msg);
							break;
						}
						return true;
					}
				});

				mPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
					public void onSeekComplete(MediaPlayer mp) {
//						Toast.makeText(ISTVVodPlayer.this, "Success!!!!", Toast.LENGTH_LONG).show();
						seekComplete();
					}
				});
			} else {
				Log.i(TAG,"mMediaPlayer is null!!!");
			}
			playing = true;
			prepared=false;
			seeking = false;
			currURL = url;
				
			if(currPosition!=0){
				seekTo();				
			} else if(currPosition == 0 && isContinue) {
				//videoView.seekTo(0);
				seekTo();
			}
			videoView.start();
			playPauseImage.setImageResource(R.drawable.vod_player_pause);
			showPanel();
		}
	}

	private void stopVideo(){
	 // add it by dgg 2012/04/07
        storeOffset();
		if(!playing)
			return;

		//
		videoView.stopPlayback();
		//videoView.pause();
		showBuffer();
		playing = false;
		prepared= false;

		currURL = null;
		
		showPanel();
	}

	private void storeOffset(){
		if(doc!=null && prepared){
			int offset = (clipOffset==-1)?-1:(currPosition/1000);
			doc.setOffset(offset);
		}
	}

	@Override
	protected void onPause(){
		Log.d(TAG, "!!!!!!!!!onPause");		
	
//		storeOffset();
		stopVideo();
		switchDeinterlace(true);
		super.onPause();
	}

	@Override
	protected void onResume(){	
		Log.d(TAG, "!!!!!!!!!onResume");
		switchDeinterlace(false);
	
		super.onResume();
		startVideo();
	}
	
    private void switchDeinterlace(boolean enable){
        String val = enable? "add default decoder deinterlace amvideo":"add default decoder amvideo";
            try {
                File f = new File("/sys/class/vfm/map");
                FileWriter fw = new FileWriter(f);
                BufferedWriter buf = new BufferedWriter(fw);
                buf.write("rm default");
                buf.close();
                
                f = new File("/sys/class/vfm/map");
                fw = new FileWriter(f);
                buf = new BufferedWriter(fw);
                buf.write(val);
                buf.close();
                
        } catch (IOException e){
                e.printStackTrace();
        }
    }
	
	private boolean episodeSubMenuCreated=false;

	private void addEpisodeSubMenu(ISTVVodMenu menu){
		if(episodeSubMenuCreated)
			return;

		if(doc.getEpisode() != -1){
			ISTVVodMenuItem episode_sub;

			episode_sub = menu.addSubMenu(12, getResources().getString(R.string.vod_player_episode));
			int i = 13;
			int j = 0;
			for(i = 13, j = 0; j < episoderealcount; i++, j++ ){
				int spisodenum = j + 1;

				String str_tmp1 = getResources().getString(R.string.vod_player_prefixepisode);
				String str_tmp2 = new Integer(spisodenum).toString();
				String str_tmp3 = getResources().getString(R.string.vod_player_postfixepisode);
				
				episode_sub.addItem(i, str_tmp1 + str_tmp2 + str_tmp3);
			}

			menu.enable_scroll(false);

			episodeSubMenuCreated=true;
		}
	}

	public boolean onCreateVodMenu(ISTVVodMenu menu) {
		ISTVVodMenuItem sub;
		ISTVVodMenuItem episode_sub;
		
		sub = menu.addSubMenu(0, getResources().getString(R.string.vod_player_quality_setting));
		sub.addItem(1, getResources().getString(R.string.vod_player_quality_adaptive));
		sub.addItem(2, getResources().getString(R.string.vod_player_quality_low));
		sub.addItem(3, getResources().getString(R.string.vod_player_quality_medium));
		sub.addItem(4, getResources().getString(R.string.vod_player_quality_normal));
		sub.addItem(5, getResources().getString(R.string.vod_player_quality_high));
		sub.addItem(6, getResources().getString(R.string.vod_player_quality_ultra));
		
//		menu.addItem(7, getResources().getString(R.string.vod_player_bookmark_setting));
		menu.addItem(8, getResources().getString(R.string.vod_player_related_setting));
		
		sub = menu.addSubMenu(9, getResources().getString(R.string.vod_player_continue_setting));
		sub.addItem(10, getResources().getString(R.string.vod_player_continue_on));
		sub.addItem(11, getResources().getString(R.string.vod_player_continue_off));
		
		sub = menu.addSubMenu(2000, getResources().getString(R.string.vod_player_screen_setting));
		sub.addItem(2001, getResources().getString(R.string.vod_player_screen_original));
		sub.addItem(2002, getResources().getString(R.string.vod_player_screen_full));
		sub.addItem(2003, getResources().getString(R.string.vod_player_screen_4v3));
		sub.addItem(2004, getResources().getString(R.string.vod_player_screen_16v9));
		
		return true;
	}
	
	public boolean onVodMenuOpened(ISTVVodMenu menu) {
		Log.d(TAG,">>>>>>>>>>>>>>> onVodMenuOpened ~ " );
		for(int i=0; i<6; i++){
			ISTVVodMenuItem item;

			item = menu.findItem(i+1);
			if(urls[i]==null){
				item.disable();
			}else{
				item.enable();
			}

			if(i==currQuality){
				Log.i(TAG, "============item.select();========== i=currQuality = "+currQuality);
				item.select();
			}else{
				item.unselect();
			}
		}
		int getMode=JniSetting.getInstance().getDisplayMode();
		Log.d(TAG, "---------getMode="+getMode);
		for(int i=0;i<4;i++){
			ISTVVodMenuItem item;
			item=menu.findItem(2001+i);
			if(getMode==i){
				item.select();
			}else{
				item.unselect();
			}
		}
		
		episodeSubMenuCreated = false;
		addEpisodeSubMenu(menu);

		if(doc.getEpisode() != -1){
			int curr = doc.getEpisode();
			int i;

			for(i = 0; i < episoderealcount; i++){
				ISTVVodMenuItem item;

				item = menu.findItem(i+13);
				if(item!=null){
					if(i==curr){
						item.select();
					}else{
						item.unselect();
					}
				}
			}
		}

		menu.setTitle(7, getResources().getString(!clipBookmarked?R.string.vod_player_bookmark_setting:R.string.vod_player_remove_bookmark_setting));

		if(panelShow){
			hidePanel();
		}
				
		if(isContinue) {
			menu.findItem(10).select();			
			menu.findItem(11).unselect();
		} else {			
			menu.findItem(10).unselect();			
			menu.findItem(11).select();
		}
		
		return true;
	}
	
	public boolean onVodMenuClicked(ISTVVodMenu menu, int id){
		Log.d(TAG,">>>>>>>>>>>>>>> onVodMenuClicked ~ " + id );
		if(id>0 && id<7){
			int pos = id-1;
			if(urls[pos]!=null){
				setQuality(pos);
			}
		}else if(id==7){/* 
			if(isNeedLogin()){
				popupstatus = 3;				
				showPopupLoginDialog(DIALOG_LOGIN);
			}else{
				if(!isNetConnected()){
					showPopupDialog(DIALOG_NET_BROKEN, getResources().getString(R.string.vod_net_broken_error));
				}else{
					Intent bookMark=new Intent(Constants.INTENT_BOOK_MARK);
					bookMark.putExtra(Constants.ITEM_PK, itemPK);
					Log.d(TAG, "---------ITEM_PK------ : "+itemPK);
					if(clipBookmarked){
						doc.removeBookmark();
						showSuccess(getResources().getString(R.string.vod_bookmark_remove_success));
						bookMark.putExtra(Constants.BOOK_TYPE, Constants.BOOK_REMOVE);
					}else{
						doc.addBookmark(); 
						showSuccess(getResources().getString(R.string.vod_bookmark_add_success));
						bookMark.putExtra(Constants.BOOK_TYPE, Constants.BOOK_ADD);
					}
					clipBookmarked = !clipBookmarked;
					this.sendBroadcast(bookMark);
					menu.setTitle(7, getResources().getString(!clipBookmarked?R.string.vod_player_bookmark_setting:R.string.vod_player_remove_bookmark_setting));
				}
			}
		*/}else if(id==8){
			gotorelatepage(itemPK);
		}else if(id==10 || id==11){
			SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, 0).edit();
			if(id == 10) {
				isContinue = true;
				editor.putBoolean("continue_play", true);
			} else {
				isContinue = false;
				editor.putBoolean("continue_play", false);
			}
			editor.commit();
			
		}else if(id>12 && id<2000){
			int index = id - 13; 
			Log.d(TAG, "----------id="+id+";index="+index+";doc.getEpisode()="+doc.getEpisode());
			if(index != doc.getEpisode()){
//				storeOffset();

				int sub = doc.getSubItemPK(index);

				Log.d(TAG, "episode id = " + id + " sub = " + sub);

				
				if(sub!=-1){
					
					stopVideo();
					doc.setItemPK(itemPK, sub);
					for(int i=0; i<6; i++){
						urls[i]=null;
					}

					itemTitle=null;
					clipLength=0;
					currPosition=0;
					currURL = null;
					currQuality=0;
					urlUpdate=false;
					playing=false;
					prepared=false;
					seeking=false;
					buffering=false;
					firstPlay=true;
					paused = false;

					
					showBuffer();
					timeText.setText("");
					totalTimeText.setText("");
					timeBar.setProgress(0);
				}
			}
		}
		if(id>=2000 && id<= 2004){
			resetScreenRect(id-2001) ;
		}
		
		return true;
	}

	/**
	 * 测试画面比例设置
	 */
	private void resetScreenRect(int t){
//			Runtime.getRuntime().exec("su /system/bin/sh echo "+t+" > /sys/class/video/screen_mode ");
			JniSetting.getInstance().setDisplayMode(t);	  
	}
	
	public boolean onVodMenuClosed(ISTVVodMenu menu){
		if(paused || !playing || !prepared){
			showPanel();
		}

		if(doc.getEpisode() != -1){
			menu.enable_scroll(true);
		}
		
		return true;
	}

	@Override
	public void onPopupDialogClicked(int which) {
		Log.d(TAG, "!!!!!!!!!onPopupDialogClicked");
	
		if(popupstatus == 1){
			if(which==0){
				/*if(!replaytimerflag){
					startVideo();
				}*/
				if(isNetConnected()){
					replayVideo();
				}
			}else if(which==1){
			}
			
			popupstatus = 0;
		}else if(popupstatus == 2){
			if(which==0){
				//videoView.setVideoPath("http://www.sina.com");
				//videoView.stopPlayback();
				Intent data=new Intent();
				data.putExtra("itemPK", itemPK);
				data.putExtra("subItemPK", subItemPK);
				Log.d(TAG, ">>>>>>>>>>>>>>>>itemPK="+itemPK+";subItemPK="+subItemPK);
				setResult(Constants.CODE_PLAYER_TO_EPISODE, data);
				finish();
			}else if(which==1){
			}
			popupstatus = 0;
		}else if(popupstatus == 3){
			popupstatus = 0;
		}
	}

	/* 已缓冲时长 */
	private int mBufferTime = 0;
	public boolean onVodTimer(VodTimer timer){
		if(timer==playerTimer){
			if(videoView.isPlaying()){
				/* 判断已经缓冲的时间，超时就reload */
				if(buffering){
					mBufferTime++;
					if(mBufferTime >= BUFFER_TIMEOUT_TIME){
						//reload
						mBufferTime = 0;
						Log.d(TAG, "---> buffering is Timeout !!!   then reload");
						checkNet_Available();
//						rePlayByTimeout();
					}
				}else{
					mBufferTime = 0;
				}
				
				closeDialog(DIALOG_NET_BROKEN);
				
				int dur = videoView.getDuration();
				Log.i(TAG, "---> videoView.getDuration : "+dur +" |  buffer time : "+mBufferTime);
				if(dur>=0){
					clipLength = dur;
				}
//				Log.d(TAG,keyLeftDown +"|"+ keyRightDown+"|"+seeking +"|"+buffering+"|"+seekBarDown);
				if(!keyLeftDown && !keyRightDown && !seeking && !buffering && !seekBarDown){
					int pos = videoView.getCurrentPosition();
					if(pos > 0 && pos<=clipLength){
						Log.d(TAG, "---> videoView.getCurrentPosition()="+pos+" ,currPosition="+currPosition);
						currPosition = pos;
					}
				}
				if(!seekBarDown){
					onDocUpdate();
				}
			}
			if(panelHideCounter>0 && !paused && playing && prepared && !buffering && !seeking && !seekBarDown){
				Log.d(TAG, "panel hide counter "+panelHideCounter);
				panelHideCounter--;
				if(panelHideCounter<=0){
					hidePanel();
				}
			}
		}else if(timer==storeTimer){
			// modify by dgg 2012/04/07
			storeOffset();
		}/*else if(timer==replayTimer){
			if(doc.isNetConnected()){
				Log.d(TAG,"!!!!!!!!!!!replay in NetConnected");

				if(replaytimerflag){
					startVideo();					
				}
				replayTimer.remove();
				replayTimer=null;
			}
		}*/

		return true;
	}

	private void gotoplayerfinishpage(int pk){
		if(pk==-1){
			return;
		}
		Bundle bundle = new Bundle();
		bundle.putInt("itemPK", pk);
		
		Intent intent = new Intent();
		intent.setClass(this, ISTVVodPlayerFinish.class);
		intent.putExtras(bundle);
		startActivity(intent);	
		finish();
	}	

	private void gotorelatepage(int pk){
		if(pk==-1){
			return;
		}
		Bundle bundle = new Bundle();
		bundle.putInt("itemPK", pk);
		
		Intent intent = new Intent();
		intent.setClass(this, ISTVVodRelateList.class);
		intent.putExtras(bundle);
		
		startActivity(intent);
		finish();
	}

	
	public void replayVideo(){
		if(playing){
			Log.d(TAG, "replay the program");
			stopVideo();
			startVideo();
		}
	}
	
	public void rePlayByTimeout(){
		stopVideo();
		String url = getClipURL();
		Log.d(TAG, "---> reload the video :"+url);
		startVideo();
	}
	
	public void onNetConnected(){
		//replayVideo();
	}
	
	public void showPopupDialog(int type, String msg){	
		if(type == DIALOG_OK_CANCEL || type == DIALOG_NET_BROKEN) {
			super.showPopupDialog(type,msg);
		}		
	}
	
	
	
	public void onHomeReceive(){
		doc.dispose();
		super.onHomeReceive();
		Log.d(TAG, "------onHomeReceive-------");
	}
	
	/* 缓冲超时时，先检查网络是否可用再reload */
	private void checkNet_Available(){
		boolean isUse = false;
		mHandler.post(new Runnable() {
			public void run() {
				String checkAddress = currURL;
				if(checkAddress==null){
					checkAddress = getClipURL();
				}
				AndroidHttpClient client = null;
				try {
					HttpGet request=new HttpGet(checkAddress);
					HttpResponse httpResponse = client.execute(request);
					int statuCode = httpResponse.getStatusLine().getStatusCode();
					Log.d("|--***--|", "-----checkNet_Available  httpResponse = "+statuCode);
					if(statuCode < 400){
						mHandler.sendEmptyMessage(NET_BE_AVAILABLE);
					}else{
						mHandler.sendEmptyMessage(NET_NOT_AVAILABLE);
					}
				} catch (Exception e) {
					Log.d("|--***--|", "----------- checkNet_Available  Exception ------------");
					mHandler.sendEmptyMessage(NET_NOT_AVAILABLE);
				}
			}
		});
	}
}
