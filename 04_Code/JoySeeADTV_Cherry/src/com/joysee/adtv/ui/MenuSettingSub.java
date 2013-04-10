package com.joysee.adtv.ui;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.common.ViewGroupUtil;
import com.joysee.adtv.controller.ViewController;
import com.joysee.adtv.ui.Menu.InterceptKeyListener;
import com.joysee.adtv.ui.Menu.MenuListener;

public class MenuSettingSub extends FrameLayout implements MenuListener{
	private ViewController mController;
	private MenuSetting menuSetting;
	private Context mContext;
	private ViewGroupUtil mViewGroupUtil;
	private ListView mSettingMenuList;
	private TextView mSettingTextView;
//	private boolean isFirstIn = true;
	private View mLastFocusView;
	private ImageView mSubView;
	private final int OFFSET = (int) getResources().getDimension(R.dimen.menu_margin_top);
	/** 声道设置 STrack = SoundTrack */
	private ArrayAdapter<String> mSTrackSettingPopupMenuListAdapter;
	/** 伴音切换 AI = AudioIndex */
	private ArrayAdapter<String> mAISettingPopupMenuListAdapter;
	/** 画面设置 S = Screen */
	private ArrayAdapter<String> mSSettingPopupMenuListAdapter;
	private InterceptKeyListener mInterceptKeyListener;
	
	private View mLastClickView;
	private int mLastClickPosition;
	

	public MenuSettingSub(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		mViewGroupUtil = new ViewGroupUtil();
	}

	public MenuSettingSub(Context context, AttributeSet attrs) {
		this(context, attrs, -1);
	}

	public MenuSettingSub(Context context) {
		this(context, null);
		mContext = context;
	}

	@Override
	protected void onFinishInflate() {
		mSettingMenuList = (ListView) findViewById(R.id.setting_menu_sub_list);
		mSettingTextView = (TextView) findViewById(R.id.setting_sub);
		mSubView = (ImageView) findViewById(R.id.subFocus);
		super.onFinishInflate();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int keyCode = event.getKeyCode();
		int action = event.getAction();
		if (action == KeyEvent.ACTION_DOWN) {
			if (keyCode == KeyEvent.KEYCODE_BACK
					|| keyCode == KeyEvent.KEYCODE_ESCAPE) {
				menuSetting.refreshData();
			}
		}
		if(mInterceptKeyListener.onKeyEvent(keyCode, action))
			return true;

		return super.dispatchKeyEvent(event);
	}
	public void setInterceptKeyListener(
			InterceptKeyListener interceptKeyListener) {
		mInterceptKeyListener = interceptKeyListener;
	}

	private void setFocusTextColor(boolean isFocus, View view) {
		TextView TextView = (TextView) view.findViewById(R.id.menu_sound_text);
		ImageView point = (ImageView) view.findViewById(R.id.menu_sub_point);

		if (isFocus) {
			point.setImageDrawable(getResources().getDrawable(R.drawable.menu_point_focus));
			TextView.setTextColor(getResources().getColor(
					R.color.menu_list_focus));
		} else {
			point.setImageDrawable(getResources().getDrawable(R.drawable.menu_point_unfocus));
			TextView.setTextColor(getResources().getColor(
					R.color.menu_text_unfocus));
		}
	}

	public void setListen() {
		mSettingMenuList
				.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						Log.d("songwenxuan","onItemSelected() .... MenuSettingSub");
//						if (isFirstIn) {
							if (mLastFocusView != null) {
								setFocusTextColor(false, mLastFocusView);
							}
							setFocusTextColor(true, view);
//						}
						int[] location = new int[2];
						view.getLocationInWindow(location);
						MarginLayoutParams params = (MarginLayoutParams) mSubView
								.getLayoutParams();
						params.topMargin = location[1] - OFFSET;
						mSubView.setVisibility(View.VISIBLE);
						mSubView.setLayoutParams(params);
//						Animation anim = new AlphaAnimation(0.0f, 1.0f);
//						anim.setDuration(300);
//						anim.setFillAfter(true);
//						anim.setFillEnabled(true);
//						mSubView.startAnimation(anim);
						mLastFocusView = view;
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
					}
				});

	}

	/**
	 * 声道设置
	 * 
	 * @param isChild
	 * @param focusView
	 * @param selection
	 */
	public void showSoundTrackSettingWindow() {
		setListen();

		final int channelNum = mController.getChannelNum();
		String[] adjust_items = channelNum == -1 ? new String[0] : this
				.getResources().getStringArray(R.array.soundtrack_items);
		mSTrackSettingPopupMenuListAdapter = new ArrayAdapter<String>(mContext,
				R.layout.dvb_setting_sound, R.id.menu_sound_text, adjust_items);
		mSettingMenuList.setAdapter(mSTrackSettingPopupMenuListAdapter);
		mSettingTextView.setText(R.string.menu_setting_sound);

		if (channelNum != -1) {
			int soundTrack = mController.getSoundTrack();
			if (soundTrack < 0 || soundTrack > 2)
				soundTrack = 0;
			mSettingMenuList.setSelection(soundTrack);
			mLastClickPosition = soundTrack;
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					View point = mSettingMenuList.getSelectedView().findViewById(R.id.menu_sub_point);
					point.setVisibility(View.VISIBLE);
					mLastClickView = point;
				}
			}, 100);
		}

		mSettingMenuList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mController.setSoundTrack(position);
				ImageView point = (ImageView) view.findViewById(R.id.menu_sub_point);
				point.setImageDrawable(MenuSettingSub.this.getResources().getDrawable(R.drawable.menu_point_focus));
				point.setVisibility(View.VISIBLE);
				if(position != mLastClickPosition && mLastClickView != null){
					View lastPoint = mLastClickView.findViewById(R.id.menu_sub_point);
					lastPoint.setVisibility(View.INVISIBLE);
				}
				mLastClickView = view;
				mLastClickPosition = position;
			}
		});
		mSettingMenuList.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				return processMenuListKeyEvent(event, mSettingMenuList,
						mSTrackSettingPopupMenuListAdapter);
			}
		});

	}

	/**
	 * 伴音
	 * 
	 * @param event
	 * @param list
	 * @param adapter
	 * @return
	 */

	public void showAudioIndexSettingWindow() {
		setListen();
		String[] str = mContext.getResources().getStringArray(
				R.array.language_items);
		final int sum = mController.getCurrentAudioIndexSum();
		String[] adjust_items = new String[sum];
		mSettingTextView.setText(R.string.menu_setting_audio);

		for (int i = 0; i < (sum>3?3:sum); i++) {
			adjust_items[i] = str[i];
		}

		mAISettingPopupMenuListAdapter = new ArrayAdapter<String>(mContext,
				R.layout.dvb_setting_sound, R.id.menu_sound_text, adjust_items);
		mSettingMenuList.setAdapter(mAISettingPopupMenuListAdapter);
		mSettingMenuList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mController.setAudioIndex(position);
				ImageView point = (ImageView) view.findViewById(R.id.menu_sub_point);
				point.setImageDrawable(MenuSettingSub.this.getResources().getDrawable(R.drawable.menu_point_focus));
				point.setVisibility(View.VISIBLE);
				if(position != mLastClickPosition && mLastClickView != null){
					View lastPoint = mLastClickView.findViewById(R.id.menu_sub_point);
					lastPoint.setVisibility(View.INVISIBLE);
					mLastClickView = point;
				}
				mLastClickView = view;
				mLastClickPosition = position;
			}
		});
		mSettingMenuList.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				return processMenuListKeyEvent(event, mSettingMenuList,
						mAISettingPopupMenuListAdapter);
			}
		});
		mSettingMenuList.requestFocus();
		final int channelNum = mController.getChannelNum();
		if (channelNum != -1) {
			int audioLanguage = mController.getAudioIndex();
			if (audioLanguage < 0 || audioLanguage > 2)
				audioLanguage = 0;
			mSettingMenuList.setSelection(audioLanguage);
			mLastClickPosition = audioLanguage;
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					View point = mSettingMenuList.getSelectedView().findViewById(R.id.menu_sub_point);
					point.setVisibility(View.VISIBLE);
					mLastClickView = point;
				}
			}, 100);
		}

	}

	/**
	 * 画面比例
	 */
	public void showScreenSettingWindow() {
		setListen();
		String[] adjust_items = this.getResources().getStringArray(
				R.array.screen_items);
		mSSettingPopupMenuListAdapter = new ArrayAdapter<String>(mContext,
				R.layout.dvb_setting_sound, R.id.menu_sound_text, adjust_items);

		mSettingMenuList.setAdapter(mSSettingPopupMenuListAdapter);
		mSettingTextView.setText(R.string.menu_setting_screen);

		int displayMode = mController.getDisplayMode();
		if (displayMode != 0 && displayMode != 1)
			displayMode = 0;
		mSettingMenuList.setSelection(displayMode);
		mLastClickPosition = displayMode;
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				View point = mSettingMenuList.getSelectedView().findViewById(R.id.menu_sub_point);
				point.setVisibility(View.VISIBLE);
				mLastClickView = point;
			}
		}, 100);
		mSettingMenuList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mController.setDisplayMode(position);
				ImageView point = (ImageView) view.findViewById(R.id.menu_sub_point);
				point.setImageDrawable(MenuSettingSub.this.getResources().getDrawable(R.drawable.menu_point_focus));
				point.setVisibility(View.VISIBLE);
				if(position != mLastClickPosition && mLastClickView != null){
					View lastPoint = mLastClickView.findViewById(R.id.menu_sub_point);
					lastPoint.setVisibility(View.INVISIBLE);
				}
				mLastClickView = view;
				mLastClickPosition = position;
			}
		});
		mSettingMenuList.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				return processMenuListKeyEvent(event, mSettingMenuList,
						mSSettingPopupMenuListAdapter);
			}
		});
		
//		View point = mSettingMenuList.getSelectedView().findViewById(R.id.menu_sub_point);
//		point.setVisibility(View.VISIBLE);

	}

	private boolean processMenuListKeyEvent(KeyEvent event, ListView list,
			BaseAdapter adapter) {

		final int keyCode = event.getKeyCode();
		final int action = event.getAction();
		switch (keyCode) {
		case KeyEvent.KEYCODE_HOME:
			if (action == KeyEvent.ACTION_DOWN) {
				mViewGroupUtil.hideView(ViewGroupUtil.KEY_MENU,
						ViewGroupUtil.FLAG_NONE);
				return true;
			}
			break;
		case KeyEvent.KEYCODE_DPAD_UP:
			if (action == KeyEvent.ACTION_DOWN && list != mSettingMenuList) {
				if (adapter.getCount() > 0
						&& list.getSelectedItemPosition() == 0) {
					list.setSelection(adapter.getCount() - 1);
					return true;
				}
			}
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			if (action == KeyEvent.ACTION_DOWN && list != mSettingMenuList) {
				if (adapter.getCount() > 0
						&& list.getSelectedItemPosition() == (adapter
								.getCount() - 1)) {
					list.setSelection(0);
					return true;
				}
			}
			break;
		case KeyEvent.KEYCODE_PLUS:
		case KeyEvent.KEYCODE_EQUALS:
		case KeyEvent.KEYCODE_NUMPAD_ADD:
			if (action == KeyEvent.ACTION_DOWN) {
				mController.doInjectKeyEvent(KeyEvent.KEYCODE_DPAD_UP);
				return true;
			}
			break;
		case KeyEvent.KEYCODE_MINUS:
			// 还应该有一个，没找到
		case KeyEvent.KEYCODE_NUMPAD_SUBTRACT:
			if (action == KeyEvent.ACTION_DOWN) {
				mController.doInjectKeyEvent(KeyEvent.KEYCODE_DPAD_DOWN);
				return true;
			}
			break;
		}
		return false;
	}

	@Override
	public void getFocus(){
		mSettingMenuList.requestFocus();
	}
	
	@Override
	public void loseFocus() {
		
	}

	public void setController(ViewController viewController) {
		mController = viewController;

	}

	public void setMenuSetting(MenuSetting menuSetting) {
		this.menuSetting = menuSetting;
	}

}
