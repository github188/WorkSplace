package com.ismartv.ui;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.ismartv.doc.ISTVResource;
import com.ismartv.doc.ISTVVodHomeDoc;
import com.ismartv.ui.widget.VodHomeItemListScrollView;

public class ISTVVodHome extends ISTVVodActivity {
	private static String TAG = "ISTVVodHome";
	public static final int RES_INT_CHANNEL_COUNT = 0;
	public static final int RES_STR_CHANNEL_ID = 1;
	public static final int RES_STR_CHANNEL_NAME = 2;
	public static final int RES_BMP_CHANNEL_NORMAL = 3;
	public static final int RES_BMP_CHANNEL_FOCUSED = 4;
	public static final int RES_INT_RECOMMEND_COUNT = 5;
	public static final int RES_BMP_RECOMMEND = 6;
	public static final int RES_STR_RECOMMEND_TITLE = 7;
	public static final int RES_INT_RECOMMEND_PK = 8;
	public static final int RES_BMP_TOP_VIDEO_POSTER = 9;
	public static final int RES_URL_TOP_VIDEO = 10;
	public static final int RES_STR_TOP_VIDEO_CHAN = 11;
	public static final int RES_STR_TOP_VIDEO_TITLE = 12;
	public static final int RES_STR_TOP_VIDEO_SEC = 13;
	public static final int RES_BMP_TOP_IMAGE = 14;
	public static final int RES_STR_TOP_IMAGE_CHAN = 15;
	public static final int RES_STR_TOP_IMAGE_TITLE = 16;
	public static final int RES_STR_TOP_IMAGE_SEC = 17;
	public static final int RES_STR_TOP_IMAGE_PK = 18;
	public static final int RES_STR_TOP_VIDEO_PK = 19;
	public static final int RES_BOOL_RECOMMEND_ISCOMPLEX = 20;

	public static final int GEO_WEATHER_NAME = 21;
	public static final int GEO_WEATHER_NAME_EN = 22;
	public static final int GEO_WEATHER_DATE = 23;
	public static final int GEO_WEATHER_WIND_DIRECTION = 24;
	public static final int GEO_WEATHER_WIND_POWER = 25;
	public static final int GEO_WEATHER_PHENOMENON = 26;
	public static final int GEO_WEATHER_TEMPERATURE = 27;
	public static final int GEO_WEATHER_GEOID = 28;

	private static final int VOD_HOME_MODULES_ID_TOP_L = 0x10;
	private static final int VOD_HOME_MODULES_ID_TOP_R = 0x20;
	private static final int VOD_HOME_MODULES_ID_RECOMMEND = 0x30;
	private static final int VOD_HOME_MODULES_ID_CHANNEL_LIST = 0x40;
	private static final int VOD_HOME_MODULES_ID_RECOMMEND_TEMP = 0x50;
	private static final int VOD_HOME_MODULES_ID_MYAPP_ITEM = 0x60;
	private static final int VOD_HOME_MODULES_ID_MYAPP_ITEM_BOTTOM = 0x70;

	private static int geo_weather_complete_flag = 0;
	private static boolean geo_weather_view_created_flag = false;

	private ISTVVodHomeDoc doc;
	private int chanNumber = 0;
	private String chanNames[];
	private String chanIDs[];
	private Bitmap chanNormalBmps[];
	private Bitmap chanFocusedBmps[];

	private Bitmap homeListBmps[];
	private String homeListTitle[];
	private int homeListItemPK[];
	private boolean homeListIsComplex[];

	private String topVideoTitle;
	private Bitmap topVideoBmp;
	private URL topVideoURL;
	private String topVideoCHAN;
	private String topVideoSecId;
	private String topImageTitle;
	private int topVideoPK = -1;

	private Bitmap topImageBmp;
	private int topImagePK = -1;
	private String topImageChan;
	private String topImageSecId;

	private ImageButton button_app_store;
	private ImageButton button_my_app;
	private ImageButton button_video_online;
	private ImageButton button_local_play;
	private ImageButton button_settings;

	private ImageButton button_media_music;
	private ImageButton button_media_video;
	private ImageButton button_media_photo;

	private ImageButton button_settings_base;
	private ImageButton button_settings_app;
	private ImageButton button_settings_network;
	private ImageButton button_settings_input;
	private ImageButton button_settings_time_geo;
	private ImageButton button_settings_system;
	private ImageButton button_settings_user;

	public static final int BUTTON_APP_STORE = 0;
	public static final int BUTTON_MY_APP = 1;
	public static final int BUTTON_VIDEO_ONLINE = 2;
	public static final int BUTTON_LOCAL_PLAY = 3;
	public static final int BUTTON_SETTINGS = 4;
	private int vod_home_button_status;
	private LinearLayout curr_content_layout;

	private VideoView videoView;
	private ImageView topVideoImageView;
	private View firstRecommendVideo ;

	private URL currURL = null;
	private boolean playing = false;
	private boolean prepared = false;
//	private boolean pause = false;

	private RelativeLayout bufferLayout;
	private RelativeLayout VodHomeLayout;
	private AnimationDrawable bufferAnim;
	private boolean bufferShow = false;
	private static Bundle geo_weather_bundle;

	private VodTimer VodHomeTimer;
	private VodHomeItemListScrollView mItemScrollView = null;

	private Animation showAnimation;
	private Animation hideAnimation;

	public static final String SYSTEM_NAME = "tv.media.status";
	public static final String SYSTEM_VALUE_PLAY = "play";
	public static final String SYSTEM_VALUE_STOP = "stop";
	public static final String SERVICE_STOP = "com.Lenovo.musicservice.exit";

	public static final Uri CONTENT_URI = Uri
			.parse("content://com.geniatech.provider.MyApplication//apps");
	public static final String COLUMN_NAME_PACKAGE_NAME = "packagename";
	public static final String COLUMN_NAME_COMMON_APP = "common";
	public static final String COLUMN_NAME_CREATE_DATE = "created";

	private static Context mContext;

	public static Context getContext() {
		return mContext;
	}

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		mContext = this;
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		setContentView(R.layout.vod_home);
		rootView=this.findViewById(R.id.rootView);

		doc = new ISTVVodHomeDoc();
		setDoc(doc);

		initTopvideoLeft();

		currURL = null;
		playing = false;
		prepared = false;

		videoView = (VideoView) findViewById(R.id.top_video_win);
		videoView.setFocusable(false);
		videoView.setFocusableInTouchMode(false);
		videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			public void onPrepared(MediaPlayer mp) {
				if (!prepared) {
					prepared = true;
					showVideo();
				}
			}
		});

		videoView
				.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					public void onCompletion(MediaPlayer mp) {
						if (playing) {
							setVideo(currURL, true);
						}
					}
				});

		videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
			public boolean onError(MediaPlayer mp, int what, int extra) {

				if (playing && prepared) {
					setVideo(currURL, true);
				}

				return true;
			}
		});
		
		videoView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				Log.d(TAG,"video focus changed !!!! >>>>>>>>>>>>>>>>>>>> ");
				if(firstRecommendVideo != null){
					firstRecommendVideo.requestFocus() ;
				}
			}
		}) ;
		
		Log.d("LOGabc", "onCreate");
	}

	public void showBuffer() {
	}

	public void hideBuffer() {
		if (bufferShow) {
			bufferLayout.setVisibility(View.INVISIBLE);
			// bufferLayout.setVisibility(View.GONE);
			// curr_content_layout.setVisibility(View.VISIBLE);
			bufferAnim.stop();
			bufferShow = false;

			// initFocusStatus();
		}
	}

	public boolean onVodTimer(VodTimer timer) {

		return true;
	}

	public void onDestroy() {
		doc.dispose();
		Log.d(TAG, "com.amlogic.weather.TopStatus.onDestroy");
		Log.d("LOGabc", "onDestroy");
		super.onDestroy();
	}

	protected void onDocGotResource(ISTVResource res) {
		switch (res.getType()) {
		case RES_INT_CHANNEL_COUNT:
			int size = res.getInt();
			if (size > 0) {
				chanNumber = size;
				chanNames = new String[size];
				chanIDs = new String[size];
				chanNormalBmps = new Bitmap[size];
				chanFocusedBmps = new Bitmap[size];
				initChannelList(size);
			}
			// Log.d("RES_INT_CHANNEL_COUNT","size"+size);
			break;
		case RES_STR_CHANNEL_ID:
			chanIDs[res.getID()] = res.getString();
			// Log.d("channID","channID "+res.getID()+" "+chanIDs[res.getID()]);
			break;
		case RES_STR_CHANNEL_NAME:
			chanNames[res.getID()] = res.getString();
			updateChannelList(res.getID(), RES_STR_CHANNEL_NAME);
			// Log.d("RES_STR_CHANNEL_NAME","channName "+res.getID()+" "+chanNames[res.getID()]);
			break;
		case RES_BMP_CHANNEL_NORMAL:
			chanNormalBmps[res.getID()] = res.getBitmap();
			updateChannelList(res.getID(), RES_BMP_CHANNEL_NORMAL);
			break;
		case RES_BMP_CHANNEL_FOCUSED:
			chanFocusedBmps[res.getID()] = res.getBitmap();
			updateChannelList(res.getID(), RES_BMP_CHANNEL_FOCUSED);
			break;
		case RES_INT_RECOMMEND_COUNT:
			Log.d("RES_INT_RECOMMEND_COUNT", "SIZE====" + res.getInt());
			if (res.getInt() > 0) {
				homeListTitle = new String[res.getInt()];
				homeListBmps = new Bitmap[res.getInt()];
				homeListItemPK = new int[res.getInt()];
				homeListIsComplex = new boolean[res.getInt()];
				Arrays.fill(homeListItemPK, -1);
				Arrays.fill(homeListIsComplex, false);
				initHomeListBmp(res.getInt());
			}
			break;
		case RES_BOOL_RECOMMEND_ISCOMPLEX:
			homeListIsComplex[res.getID()] = res.getBoolean();
			break;

		case RES_BMP_RECOMMEND:
			homeListBmps[res.getID()] = res.getBitmap();
			updateHomeListBmp(res.getID(), RES_BMP_RECOMMEND);
			break;
		case RES_STR_RECOMMEND_TITLE:
			homeListTitle[res.getID()] = res.getString();
			updateHomeListBmp(res.getID(), RES_STR_RECOMMEND_TITLE);
			Log.d("RES_INT_RECOMMEND_COUNT", "ID==" + res.getID() + " "
					+ homeListTitle[res.getID()]);

			break;
		case RES_INT_RECOMMEND_PK:
			homeListItemPK[res.getID()] = res.getInt();
			break;
		case RES_BMP_TOP_VIDEO_POSTER:
			topVideoBmp = res.getBitmap();
			updateTopvideoLeft();
			break;
		case RES_STR_TOP_VIDEO_PK:
			topVideoPK = res.getInt();
			break;
		case RES_URL_TOP_VIDEO:
			topVideoURL = res.getURL();
			currURL = topVideoURL;
			 setVideo(topVideoURL);
			 Log.d(TAG,"topVideoURL******"+topVideoURL.toString());
			break;
		case RES_STR_TOP_VIDEO_CHAN:
			topVideoCHAN = res.getString();
			// Log.d(TAG,"topVideoCHAN******"+topVideoCHAN);
			break;
		case RES_STR_TOP_VIDEO_SEC:
			topVideoSecId = res.getString();
			break;
		case RES_BMP_TOP_IMAGE:
			topImageBmp = res.getBitmap();
			updateTopvideoRight();
			break;
		case RES_STR_TOP_VIDEO_TITLE:
			topVideoTitle = res.getString();
			updateTopvideoLeft();
			// Log.d(TAG,"topVideoTitle******"+topVideoTitle);
			break;
		case RES_STR_TOP_IMAGE_TITLE:
			topImageTitle = res.getString();
			updateTopvideoRight();
			break;
		case RES_STR_TOP_IMAGE_CHAN:
			topImageChan = res.getString();
			Log.d(TAG, "RES_STR_TOP_IMAGE_CHAN" + topImagePK);
			break;
		case RES_STR_TOP_IMAGE_SEC:
			topImageSecId = res.getString();
			Log.d(TAG, "RES_STR_TOP_IMAGE_SEC" + topImagePK);
			break;
		case RES_STR_TOP_IMAGE_PK:
			topImagePK = res.getInt();
			Log.d(TAG, "RES_STR_TOP_IMAGE_PK" + topImagePK);
			break;

		default:
			break;
		}
	}

	private void showVideo() {

		topVideoImageView = (ImageView) findViewById(R.id.ImageView_top_left);

		int x_begin = topVideoImageView.getLeft();
		int x_end = topVideoImageView.getRight();
		int y_begin = topVideoImageView.getTop();
		int y_end = topVideoImageView.getBottom();
		 Log.d(TAG,"*******************"+x_begin+"***"+x_end+"***"+y_begin+"***"+y_end);

		topVideoImageView.setVisibility(View.INVISIBLE);
		FileWrite mControl = new FileWrite("/sys/class/video/screen_mode");
		mControl.setValue("1");
	}

	private void setVideo(URL url) {
		setVideo(url, false);
	}

	private void setVideo(URL url, boolean replay) {
		
		if (url == null)
			return;

		if (replay) {
			currURL = null;
		}

		if ((currURL == null) || !(url.equals(currURL))) {
			currURL = url;
		}

		if (playing) {
			videoView.stopPlayback();
			// videoView.pause();
		}

		Log.d(TAG, "play URL " + url.toString());
		videoView.setVideoPath(url.toString());
		videoView.start();
		playing = true;
		prepared = false;
		Log.d("LOGabc", "setVideo");
		topVideoImageView = (ImageView) findViewById(R.id.ImageView_top_left);
		topVideoImageView.setVisibility(View.VISIBLE);
	}

	private void stopVideo() {
		if (!playing)
			return;
		Log.d("LOGabc", "stopVideo");
		playing = false;
		prepared = false;
		videoView.stopPlayback();

		topVideoImageView = (ImageView) findViewById(R.id.ImageView_top_left);
		topVideoImageView.setVisibility(View.VISIBLE);
	}

	protected void onDocUpdate() {
		/* Redraw the activity */
	}

	public void initTopvideoLeft() {
		
		View view = null;

		LinearLayout linearLayout = (LinearLayout) this
				.findViewById(R.id.layout_video_play);

		view = (View) linearLayout;

		ImageView imgView = (ImageView) this
				.findViewById(R.id.ImageView_top_left);
		int id = (VOD_HOME_MODULES_ID_TOP_L << 16) | 0;
		view.setId(id);
		// view.setBackgroundResource(android.R.drawable.list_selector_background);
		view.setFocusable(false);
		view.setFocusableInTouchMode(false);
	//	view.setOnClickListener(new MouseClick());

		Log.d(TAG, "view left" + view.getLeft());
		Log.d(TAG, "view audio" + view.getTop());
		int[] location = new int[2];

		view.getLocationInWindow(location);

		Log.d(TAG, "getLocationInWindow 0 =" + location[0]);
		Log.d(TAG, "getLocationInWindow 1 =" + location[1]);

		view.getLocationOnScreen(location);
		Log.d(TAG, "getLocationOnScreen X = " + location[0]);
		Log.d(TAG, "getLocationOnScreen Y = " + location[1]);

	/*	view.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean isFocused) {
				// TODO Auto-generated method stub
				Log.d(TAG,"linearLayout setOnFocusChangeListener ************** ");
				if (isFocused == true) {
					v.setBackgroundResource(R.drawable.large_poster_focus);
				} else {
					v.setBackgroundResource(R.drawable.large_poster_normal);
				}
			}
		});

		view.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				HideMouse();
				switch (keyCode) {
				case KeyEvent.KEYCODE_DPAD_LEFT:
					return true;
				}
				return false;
			}
		});*/
	}

	public void updateTopvideoLeft() {

		View view = null;

		LinearLayout linearLayout = (LinearLayout) this
				.findViewById(R.id.layout_video_play);

		view = (View) linearLayout;

		ImageView imgView = (ImageView) this
				.findViewById(R.id.ImageView_top_left);

		imgView.setImageBitmap(topVideoBmp);

	}

	public void initTopvideoRight() {

	}

	public void updateTopvideoRight() {

	}

	public void addHomeListBmp() {
	}

	public void initHomeListBmp(int size) {
		LinearLayout linearLayout = (LinearLayout) this.findViewById(R.id.linearlayout_add);
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		for (int i = 0; i < 4; i++) { 
			View view = inflater.inflate(R.layout.item, null);
			// view.setLayoutParams(new
			// LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
			LinearLayout.LayoutParams para = new LinearLayout.LayoutParams(326,
					279);
			para.setMargins(0, 0, 14, 0);
			view.setLayoutParams(para);
			TextView text = (TextView) view.findViewById(R.id.ItemText);
			text.setAlpha(0.75f);
			int id = (VOD_HOME_MODULES_ID_RECOMMEND << 16) | i;

			view.setId(id);
			// view.setBackgroundResource(android.R.drawable.list_selector_background);
			// view.setBackgroundResource(R.drawable.small_poster_background);
			view.setFocusable(true);
			view.setFocusableInTouchMode(true);

			view.setOnClickListener(new MouseClick());

			view.setOnFocusChangeListener(new OnFocusChangeListener() {
				public void onFocusChange(View v, boolean isFocused) {
					if (isFocused == true) {
						v.setBackgroundResource(R.drawable.voditem_focus);
						TextView text = (TextView) v
								.findViewById(R.id.ItemText);
						text.setAlpha(1);
					} else {
						TextView text = (TextView) v
								.findViewById(R.id.ItemText);
						text.setAlpha(0.75f);
						v.setBackgroundDrawable(null);
					}
				}
			});
			if (i == 0) {
//				firstRecommendVideo = view ;
//				view.setOnKeyListener(new OnKeyListener() {
//					public boolean onKey(View v, int keyCode, KeyEvent event) {
//						switch (keyCode) {
//						case KeyEvent.KEYCODE_DPAD_UP:
//							return true;
//
//						}
//						return false;
//					}
//				});
			}
			if (i == (3)) {
				view.setOnKeyListener(new OnKeyListener() {
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						switch (keyCode) {
						case KeyEvent.KEYCODE_DPAD_RIGHT:
							return true;

						}
						return false;
					}
				});
			}

			linearLayout.addView(view);
		}
	}

	public void updateHomeListBmp(int id, int content) {
		Log.d("updateHomeListBmp", "id=" + id + "######" + content);
		if(id > 3)
			return;
		LinearLayout linearLayout = (LinearLayout) this
				.findViewById(R.id.linearlayout_add);

		Log.d("####", "***" + linearLayout.getChildCount());

		View view = linearLayout.getChildAt(id);

		if (content == RES_BMP_RECOMMEND) {
			ImageView imgView = (ImageView) view.findViewById(R.id.ItemImage);
			imgView.setImageBitmap(homeListBmps[id]);
		} else if (content == RES_STR_RECOMMEND_TITLE) {
			TextView text = (TextView) view.findViewById(R.id.ItemText);
			text.setText(homeListTitle[id]);
		}

		view.setFocusable(true);
		view.setFocusableInTouchMode(true);
		view.setOnClickListener(new MouseClick());
	}

	private String findChanneListNameByIdname(String name) {
		if (chanNumber == 0)
			return null;
		Log.d(TAG, "channelNumber=" + chanNumber);

		for (int i = 0; i < chanNumber; i++) {
			if ((chanIDs[i] != null) && (chanNames[i] != null)
					&& (name.equals(chanIDs[i])))
				return chanNames[i];
			else
				continue;
		}
		Log.d(TAG, "Dont find channel name!");
		return null;

	}

	class MouseClick implements android.view.View.OnClickListener {
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Log.d(TAG, "@@@" + v.getId());
			int id = v.getId();
			int sub_id = id & 0x0000ffff;
			
			if(id==-1){
			    /* -1,跳到搜索页 */
			    gotoMovieList("$search", null, null);
			    return;
			}
			
			switch ((id >> 16) & 0x0000ffff) {
			case VOD_HOME_MODULES_ID_TOP_L:
				Log.d(TAG, "VOD_HOME_MODULES_ID_TOP_L");

				if (isNetConnected() == false) {
					showPopupDialog(
							DIALOG_ITEM_CLICK_NET_BROKEN,
							getResources().getString(
									R.string.vod_net_broken_error));
				} else {
					if (topVideoCHAN != null && topVideoSecId != null) {
						Log.d(TAG, "@@" + topVideoCHAN + "@@" + topVideoSecId);
						gotoMovieList(topVideoCHAN,
								findChanneListNameByIdname(topVideoCHAN),
								topVideoSecId);
					} else if (topVideoPK != -1) {
						gotoItemDetail(topVideoPK);
					}
				}
				break;
			case VOD_HOME_MODULES_ID_TOP_R:
				Log.d(TAG, "VOD_HOME_MODULES_ID_TOP_R");
				if (isNetConnected() == false) {
					showPopupDialog(
							DIALOG_ITEM_CLICK_NET_BROKEN,
							getResources().getString(
									R.string.vod_net_broken_error));
				} else {
					if ((topImageChan != null) && (topImageSecId != null)) {

					} else if (topImagePK != -1) {
						gotoItemDetail(topImagePK);
					}
				}
				break;
			case VOD_HOME_MODULES_ID_RECOMMEND:
				Log.d(TAG, "VOD_HOME_MODULES_ID_RECOMMEND" + sub_id);
				if (isNetConnected() == false) {
					showPopupDialog(
							DIALOG_ITEM_CLICK_NET_BROKEN,
							getResources().getString(
									R.string.vod_net_broken_error));
				} else {

					if (homeListItemPK == null)
						return;
					int itemPK = homeListItemPK[sub_id];

					if (homeListIsComplex[sub_id]) {
						if (itemPK != -1) {
							gotoItemDetail(itemPK);
						}
					} else {
						if (itemPK != -1) {
							gotoPlayer(itemPK, -1);
						}
					}

					Log.d(TAG, "homeListItemPK" + itemPK);
				}
				break;
			case VOD_HOME_MODULES_ID_CHANNEL_LIST:
				Log.d(TAG, "VOD_HOME_MODULES_ID_CHANNEL_LIST" + chanIDs[sub_id]
						+ "***" + chanNames[sub_id] + "***" + sub_id);
				if (isNetConnected() == false) {
					showPopupDialog(
							DIALOG_ITEM_CLICK_NET_BROKEN,
							getResources().getString(
									R.string.vod_net_broken_error));
				} else {
					gotoMovieList(chanIDs[sub_id], chanNames[sub_id], null);
				}
				break;
			case VOD_HOME_MODULES_ID_MYAPP_ITEM:
				Log.d(TAG, "VOD_HOME_MODULES_ID_MYAPP_ITEM" + sub_id);
				break;
			}
		}
	}

	private void initFocusStatus() {
		View view = null;
		LinearLayout linearLayout = (LinearLayout) this
				.findViewById(R.id.linearlayout_add);
		if (linearLayout != null) {
			view = linearLayout.getChildAt(7);
			if (view != null) {
				view.setFocusable(true);
				view.setFocusableInTouchMode(true);
				view.requestFocus();
			}
		}

		linearLayout = (LinearLayout) this.findViewById(R.id.layout_video_play);
		if (linearLayout != null) {
			view = (View) linearLayout;
			view.setFocusable(true);
			view.setFocusableInTouchMode(true);
			view.requestFocus();
		}
	}

	public void initChannelList(int size) {
		
		if(size > 6){
        	ImageView iv = (ImageView) this.findViewById(R.id.channellist_down_arrow);
        	iv.setVisibility(View.VISIBLE);
        }
		
		for (int j = 0; j < 1; j++) {
			LinearLayout linearLayout = (LinearLayout) this
					.findViewById(R.id.linearLayoutChannelList);

			LayoutInflater inflater = (LayoutInflater) this
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.channel_list_item, null);
			view.setLayoutParams(new LinearLayout.LayoutParams(406, 100));

			TextView text = (TextView) view.findViewById(R.id.ItemText);
			text.setAlpha(0.5f);
			text.setTextSize(40);
			view.setFocusable(true);
			view.setFocusableInTouchMode(true);
			if (j == 0) {//TODO j==0 显示推荐　j==1　显示搜索
				int id = (VOD_HOME_MODULES_ID_CHANNEL_LIST << 16) | -2;
				view.setId(id);
				text.setText(R.string.vod_recommend_lable);
				// view.setBackgroundResource(R.drawable.vodhome_channellist_selected);
//				view.setBackgroundResource(R.drawable.vodhome_channellist_focus);
				view.setBackgroundResource(R.drawable.voditem_sec_focus);
				TextView text2 = (TextView) view.findViewById(R.id.ItemText);
				text2.setAlpha(1);
				view.setOnKeyListener(new OnKeyListener() {
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						switch (keyCode) {
						case KeyEvent.KEYCODE_DPAD_UP:
							View up_view = (View) findViewById(R.id.channellist_up_arrow);
							up_view.setVisibility(View.INVISIBLE);
							return true;
						}
						return false;
					}
				});
				view.requestFocus();
			} else {
				int id = (VOD_HOME_MODULES_ID_CHANNEL_LIST << 16) | -1;
				view.setId(id);
				text.setText(R.string.vod_search_lable);//TODO 搜索
				
				view.setBackgroundResource(R.drawable.voditem_sec_focus);
                TextView text2 = (TextView) view.findViewById(R.id.ItemText);
                text2.setAlpha(1);
                view.setOnKeyListener(new OnKeyListener() {
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_UP:
                            View up_view = (View) findViewById(R.id.channellist_up_arrow);
                            up_view.setVisibility(View.INVISIBLE);
                            return true;
                        }
                        return false;
                    }
                });
                view.requestFocus();
				
			}
			view.setOnFocusChangeListener(new OnFocusChangeListener() {
				public void onFocusChange(View v, boolean isFocused) {
					if (isFocused == true) {
//						v.setBackgroundResource(R.drawable.vodhome_channellist_focus);
						v.setBackgroundResource(R.drawable.voditem_sec_focus);
						TextView text0 = (TextView) v
								.findViewById(R.id.ItemText);
						text0.setAlpha(1);
					} else {
						v.setBackgroundDrawable(null);
						TextView text0 = (TextView) v
								.findViewById(R.id.ItemText);
						text0.setAlpha(0.5f);
					}
				}
			});
			view.setOnClickListener(new MouseClick());
			linearLayout.addView(view);

		}

		Log.d("ISTVVodHome--initChannelList--size", size + "");
		for (int i = 0; i < size; i++) {
			LinearLayout linearLayout = (LinearLayout) this
					.findViewById(R.id.linearLayoutChannelList);

			LayoutInflater inflater = (LayoutInflater) this
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.channel_list_item, null);
			view.setLayoutParams(new LinearLayout.LayoutParams(406, 100));
			TextView text = (TextView) view.findViewById(R.id.ItemText);
			text.setAlpha(0.5f);
			int id = (VOD_HOME_MODULES_ID_CHANNEL_LIST << 16) | i;
			view.setId(id);

			// view.setBackgroundResource(R.drawable.vodhome_channellist_background);
			view.setFocusable(true);
			view.setFocusableInTouchMode(true);
			view.setOnClickListener(new MouseClick());
			linearLayout.addView(view);

			Log.d("initChannelList", "view.id = " + view.getId());
			view.setOnFocusChangeListener(new OnFocusChangeListener() {
				public void onFocusChange(View v, boolean isFocused) {
					if (isFocused == true) {
//						v.setBackgroundResource(R.drawable.vodhome_channellist_focus);
						v.setBackgroundResource(R.drawable.voditem_sec_focus);
						TextView text0 = (TextView) v
								.findViewById(R.id.ItemText);
						text0.setAlpha(1);
					} else {
						v.setBackgroundDrawable(null);
						TextView text0 = (TextView) v
								.findViewById(R.id.ItemText);
						text0.setAlpha(0.5f);
					}
				}
			});

			if (i == size-9) {
				view.setOnKeyListener(new OnKeyListener() {
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						switch (keyCode) {
						case KeyEvent.KEYCODE_DPAD_UP:
							View down_view = (View) findViewById(R.id.channellist_down_arrow);
							down_view.setVisibility(View.VISIBLE);
						}
						return false;
					}
				});
			} else if (size > 6 && i == 6) {
				view.setOnKeyListener(new OnKeyListener() {
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						switch (keyCode) {
						case KeyEvent.KEYCODE_DPAD_DOWN:
							View up_view = (View) findViewById(R.id.channellist_up_arrow);
							up_view.setVisibility(View.VISIBLE);
						}
						return false;
					}
				});
			} else if (size > 6 && i == (size - 1)) {
				view.setOnKeyListener(new OnKeyListener() {
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						switch (keyCode) {
						case KeyEvent.KEYCODE_DPAD_DOWN:
							View down_view = (View) findViewById(R.id.channellist_down_arrow);
							down_view.setVisibility(View.INVISIBLE);
							return true;
						}
						return false;
					}
				});
			}

		}
	}

	public void updateChannelList(int id, int content) {

		LinearLayout linearLayout = (LinearLayout) this
				.findViewById(R.id.linearLayoutChannelList);

		// Log.d("####","***"+linearLayout.getChildCount());
		View view = linearLayout.getChildAt(id + 1);

		TextView text = (TextView) view.findViewById(R.id.ItemText);
		text.setText(chanNames[id]);

		view.setFocusable(true);
		view.setFocusableInTouchMode(true);
		// view.requestFocus();
		view.setOnClickListener(new MouseClick());

		View down_view = (View) findViewById(R.id.channellist_down_arrow);
		down_view.setVisibility(View.VISIBLE);

	}

	class VodHomeMediaPlayerButtonClick implements
			android.view.View.OnClickListener {

		public void onClick(View v) {
		}
	}

	private void gotoPlayer(int pk, int sub_pk) {
		if (pk == -1)
			return;

		HideStatus();
		Bundle bundle = new Bundle();
		bundle.putInt("itemPK", pk);
		bundle.putInt("subItemPK", sub_pk);

		Intent intent = new Intent();
		intent.setClass(this, ISTVVodPlayer.class);
		intent.putExtras(bundle);

		startActivityForResult(intent, 1);
	}

	private void gotoItemDetail(int pk) {
		if (pk == -1)
			return;

		HideStatus();
		Bundle bundle = new Bundle();
		bundle.putInt("itemPK", pk);

		Intent intent = new Intent();
		intent.setClass(this, ISTVVodItemDetail.class);
		intent.putExtras(bundle);

		startActivityForResult(intent, 1);
	}

	private void gotoMovieList(String channelID, String channelName,
			String topSectionId) {
		Bundle bundle = new Bundle();
		bundle.putString("chan_id", channelID);
		bundle.putString("from", "home");
		if (channelName != null)
			bundle.putString("chan_name", channelName);
		if (topSectionId != null)
			bundle.putString("top_section_Id", topSectionId);

		Intent intent = new Intent();
		if ("$histories".equals(channelID))
			intent.setClass(this, ISTVVodHistory.class);
		else if ("$bookmarks".equals(channelID)) {
			intent.setClass(this, ISTVVodBookmark.class);
		} 
		else if ("$search".equals(channelID)){
            intent.setClass(ISTVVodHome.this, ISTVVodSearch.class);
		}
		else{
		    intent.setClass(this, ISTVVodItemList.class);
		}
		intent.putExtras(bundle);

		startActivityForResult(intent, 1);
	}

	public void startAPK(ApplicationInfo applicationInfo) {
		PackageManager mPackageManager = this.getPackageManager();
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		final List<ResolveInfo> apps = mPackageManager.queryIntentActivities(
				mainIntent, 0);
		for (ResolveInfo resolve : apps) {
			if (resolve.activityInfo.applicationInfo.packageName
					.equals(applicationInfo.packageName)) {

				HideStatus();
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				intent.setComponent(new ComponentName(
						resolve.activityInfo.applicationInfo.packageName,
						resolve.activityInfo.name));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				startActivity(intent);
			}
		}
	}

	@Override
	protected void onPause() {
		stopVideo();
		Log.d("LOGabc", "onPause");
		super.onPause();

	}

	@Override
	protected void onResume() {
		super.onResume();
		setVideo(currURL, true);
		Log.d("LOGabc", "onResume");

	}

	public void HideStatus() {

	}

	public void HideMouse() {

		Log.d(TAG, "Hide mouse");
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		// wm.setMouseStatus(false);
		// wm.setMouseCursorType(WindowManager.MOUSE_CURSOR_NONE);

	}

	public void onNetConnected() {
		Log.d("LOGabc", "onNetConnected currURL="+currURL);
		if (currURL != null) {
			 setVideo(currURL, true);
		}
	}

	public void onNetDisconnected() {
		Log.d("LOGabc", "onNetDisconnected");
		stopVideo();
	}

	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_SEARCH:
		case KeyEvent.KEYCODE_Q:
			if (vod_home_button_status == BUTTON_MY_APP) {
				Bundle bundle = new Bundle();
				bundle.putString("layoutInstruction", "localApp");
				startSearch(null, false, bundle, true);
			} else if (vod_home_button_status == BUTTON_VIDEO_ONLINE) {
				Bundle bundle = new Bundle();
				bundle.putString("layoutInstruction", "vod");
				startSearch(null, false, bundle, true);
			}
			return true;
		}

		return super.onKeyUp(keyCode, event);
	}

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
//        if(keyCode == KeyEvent.KEYCODE_BACK){
//            Log.d(TAG, "Kill thread!Quit!!!!");
//            android.os.Process.killProcess(android.os.Process.myPid());
//            return true;          
//        }
        return super.onKeyDown(keyCode, event);
    }
    
    public void onHomeReceive(){
		doc.dispose();
		super.onHomeReceive();
		Log.d(TAG, "------onHomeReceive-------");
	}
}
