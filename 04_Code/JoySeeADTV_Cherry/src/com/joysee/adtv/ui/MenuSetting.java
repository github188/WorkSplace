package com.joysee.adtv.ui;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.activity.CaSettingActivity;
import com.joysee.adtv.activity.SearchMenuActivity;
import com.joysee.adtv.controller.ViewController;
import com.joysee.adtv.ui.Menu.InterceptKeyListener;
import com.joysee.adtv.ui.Menu.MenuListener;
import com.joysee.adtv.ui.adapter.SettingMenuAdapter;

public class MenuSetting extends FrameLayout implements MenuListener{
	private Context mContext;
	private ListView mSettingPopupMenuView;
	private LayoutInflater mSettingMenuInflater;
	private SettingMenuAdapter mSettingMenuAdapter;
	private ViewController mController;
	private boolean isFirstIn = true;
	private View mLastFocusView;
	private ImageView mSubView;
	private final int OFFSET = (int) getResources().getDimension(R.dimen.menu_margin_top);

	public static final int SHOW_PICTURE_PROPORTION = 0;
	public static final int SHOW_FAVORITE_CHANNEL = 1;
	public static final int SHOW_APPOINTMENT = 2;
	public static final int SHOW_SOUND_TRACK = 3;
	public static final int SHOW_AUDIO_INDEX = 4;
	public static final int SHOW_SEARCH_CHANNEL = 5;
	public static final int SHOW_SMART_CARD = 6;
	public static final int SHOW_SYSTEM_INFORMATION = 7;
	

	public MenuSetting(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}

	public MenuSetting(Context context, AttributeSet attrs) {
		this(context, attrs, -1);
	}

	public MenuSetting(Context context) {
		this(context, null);
	}


	private void setFocusTextColor(boolean isFocus, View view) {
		TextView text = (TextView) view.findViewById(R.id.menu_list_item_text);
		TextView state = (TextView) view.findViewById(R.id.menu_list_item_state);

		if (isFocus) {
			text.setTextColor(getResources().getColor(
					R.color.menu_list_focus));
			state.setAlpha(1f);
		} else {
			text.setTextColor(getResources().getColor(
					R.color.menu_text_unfocus));
			state.setAlpha(.4f);
		}
	}

	public void setListen() {
		mSettingPopupMenuView
				.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						if (isFirstIn) {
							if (mLastFocusView != null) {
								setFocusTextColor(false, mLastFocusView);
							}
							setFocusTextColor(true, view);
						}
						int[] location = new int[2];
						view.getLocationInWindow(location);
						MarginLayoutParams params = (MarginLayoutParams) mSubView
								.getLayoutParams();
						params.topMargin = (location[1] - OFFSET) <= 0 ? 0
								: (location[1] - OFFSET);
						Log.i("chaidandan",
								"onItemSelected   mSubView.topMargin = "
										+ params.topMargin);
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

	public void showMainSetting() {

		if (mSettingPopupMenuView == null) {
			mSettingMenuInflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (mSettingMenuInflater == null)
				throw new AssertionError("LayoutInflater not found");
		}
		mSettingPopupMenuView = (ListView) this
				.findViewById(R.id.setting_Menu_list);
		mSettingPopupMenuView.setOnItemClickListener(mMainMenuTVItemClickLis);
		String[] adjust_items_setting_menu = mContext.getResources()
				.getStringArray(R.array.adjust_items_setting_menu);

		mSettingMenuAdapter = new SettingMenuAdapter(mContext,
				adjust_items_setting_menu, mController);
		mSettingPopupMenuView.setAdapter(mSettingMenuAdapter);
		mSubView = (ImageView) findViewById(R.id.focus);
	}

	public void refreshData() {
		if (mSettingMenuAdapter != null) {
			mSettingMenuAdapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onFinishInflate() {

		super.onFinishInflate();
	}

	private OnItemClickListener mMainMenuTVItemClickLis = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			switch (position) {
			case SHOW_PICTURE_PROPORTION:
				mInterceptKeyListener.showSettingSub(SHOW_PICTURE_PROPORTION);
				break;
			case SHOW_FAVORITE_CHANNEL://
				mInterceptKeyListener.showSettingSub(SHOW_FAVORITE_CHANNEL);
				break;
			case SHOW_APPOINTMENT:// 预约管理
				mInterceptKeyListener.showSettingSub(SHOW_APPOINTMENT);
				break;
			case SHOW_SOUND_TRACK:// 声道
				mInterceptKeyListener.showSettingSub(SHOW_SOUND_TRACK);
				break;

			case SHOW_AUDIO_INDEX:// 伴音
				mInterceptKeyListener.showSettingSub(SHOW_AUDIO_INDEX);
				break;
			case SHOW_SEARCH_CHANNEL:
				mInterceptKeyListener.exitMenu();
				Intent searchIntent = new Intent(mContext,
						SearchMenuActivity.class);
				mContext.startActivity(searchIntent);
				break;
			case SHOW_SMART_CARD:// 智能卡
				mInterceptKeyListener.exitMenu();
				Intent caIntent = new Intent(mContext, CaSettingActivity.class);
				caIntent.putExtra("first", 1);
				mContext.startActivity(caIntent);
				break;
			case SHOW_SYSTEM_INFORMATION:
				mInterceptKeyListener.showSettingSub(SHOW_SYSTEM_INFORMATION);
				break;

			}
		}
	};

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int keyCode = event.getKeyCode();
		int action = event.getAction();
		if (mInterceptKeyListener.onKeyEvent(keyCode, action)) {
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	private InterceptKeyListener mInterceptKeyListener;

	public void setInterceptKeyListener(
			InterceptKeyListener interceptKeyListener) {
		mInterceptKeyListener = interceptKeyListener;
	}

	@Override
	public void getFocus(){
		mSettingPopupMenuView.requestFocus();
	}
	
	@Override
	public void loseFocus() {
		
	}

	public void fillData(ViewController controller) {
		this.mController = controller;
		showMainSetting();
		setListen();
	}

	public void setController(ViewController viewController) {
		mController = viewController;
	}
}
