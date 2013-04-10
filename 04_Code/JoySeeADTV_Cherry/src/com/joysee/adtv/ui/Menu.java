package com.joysee.adtv.ui;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ViewFlipper;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DefaultParameter.ViewMessage;
import com.joysee.adtv.common.DvbKeyEvent;
import com.joysee.adtv.common.DvbMessage;
import com.joysee.adtv.controller.ViewController;

public class Menu implements IDvbBaseView  {
	private Activity mActivity;
	private Context mContext;
	private PopupWindow mPopupMenu;
	private View mMenuView;
	private ViewFlipper mMenuCotent;
	private LayoutInflater mInflater;
	
	private static final int MENU_PROGRAM_GUIDE = 0;
	private static final int MENU_CHANNEL_LIST = 1;
	private static final int MENU_SETTINGS = 2;
	
	private MenuProgramGuide mProgramGuideView;
	private MenuChannelList mChannelListView;
	private MenuSetting mSettingView;
	private MenuSettingSub mSettingSubView;
	private MenuSystemInfo mMenuSysInfoView;
	
	private int mIndex;
	
	private ViewController mController;
	private LinearLayout mMenuTitle;
	
	private LinearLayout mProgramGuideFocusView;
	private LinearLayout mChannelListFocusView;
	private LinearLayout mSettingsFocusView;
	private ImageView mProgramGuideNoFocusView;
	private ImageView mChannelListNoFocusView;
	private ImageView mSettingsNoFocusView;
	
	public Menu(Activity activity){
		mActivity = activity;
		mInflater = activity.getLayoutInflater();
		mContext = mActivity.getApplicationContext();
	}
	
	public void showMenu(int index) {
		
		if(mPopupMenu == null)
			mPopupMenu = new PopupWindow();
		
		if(mMenuView == null){
			
			mMenuView = mActivity.getLayoutInflater().inflate(R.layout.menu, null);
			mMenuTitle = (LinearLayout) mMenuView.findViewById(R.id.menu_title);
			mMenuCotent = (ViewFlipper) mMenuView.findViewById(R.id.main_menu_container);
			
			mProgramGuideNoFocusView	 = (ImageView) mMenuView.findViewById(R.id.menu_title_pg_nofocus);
			mChannelListNoFocusView	 = (ImageView) mMenuView.findViewById(R.id.menu_title_cl_nofocus);
			mSettingsNoFocusView	 = (ImageView) mMenuView.findViewById(R.id.menu_title_settings_nofocus);
			
			mProgramGuideFocusView = (LinearLayout) mMenuView.findViewById(R.id.menu_title_pg_focus);
			mChannelListFocusView = (LinearLayout) mMenuView.findViewById(R.id.menu_title_cl_focus);
			mSettingsFocusView = (LinearLayout) mMenuView.findViewById(R.id.menu_title_settings_focus);
		}
		
		mProgramGuideView = (MenuProgramGuide) mInflater.inflate(R.layout.menu_program_guide, null);
		mChannelListView = (MenuChannelList) mInflater.inflate(R.layout.menu_channel_list, null);
		mSettingView = (MenuSetting) mInflater.inflate(R.layout.menu_setting, null);
		mSettingSubView = (MenuSettingSub) mInflater.inflate(R.layout.menu_setting_sub, null);
		
		
		mChannelListView.fillData(mController);
		mSettingView.fillData(mController);
		mSettingSubView.setMenuSetting(mSettingView);
		mProgramGuideView.setController(mController);
		
		mProgramGuideView.setInterceptKeyListener(mInterceptKeyListener);
		mChannelListView.setInterceptKeyListener(mInterceptKeyListener);
		mSettingView.setInterceptKeyListener(mInterceptKeyListener);
		mSettingSubView.setInterceptKeyListener(mInterceptKeyListener);

		
		mMenuCotent.removeAllViews();
 		mMenuCotent.addView(mProgramGuideView);
		mMenuCotent.addView(mChannelListView);
		mMenuCotent.addView(mSettingView);
		mMenuCotent.addView(mSettingSubView);
		
		int width = (int) mContext.getResources().getDimension(R.dimen.menu_width);
		int height = (int) mContext.getResources().getDimension(R.dimen.menu_height);
		
		mPopupMenu.setWidth(width);
		mPopupMenu.setHeight(height);
		
		mPopupMenu.setFocusable(true);
		mPopupMenu.setContentView(mMenuView);
//		mProgramGuideView.getFirstFocusView().requestFocus();
		if(index == MENU_CHANNEL_LIST){
			mChannelListView.setFromChannelListKey();
		}
		enterSubMenu(index);
		mPopupMenu.showAtLocation(mActivity.getWindow().getDecorView(), Gravity.RIGHT, 0, 0);
	}
	
	private void enterSubMenu(int index) {
		if(mIndex == 3)
			mMenuTitle.setAlpha(1.0f);
		mIndex = index;
		mMenuCotent.setDisplayedChild(index);
		setTitleFocus(index);
		((MenuListener)mMenuCotent.getChildAt(index)).getFocus();
	}
	
	private void leaveMenu(int index) {
		((MenuListener)mMenuCotent.getChildAt(index)).loseFocus();
	}

	private InterceptKeyListener mInterceptKeyListener = new InterceptKeyListener() {
		
		private MenuReservationList mMenuReservationList;
		private MenuFavoriteList mMenuFavoriteList;

		@Override
		public boolean onKeyEvent(int keyCode,int action) {
			if (action == KeyEvent.ACTION_UP) {
				if (keyCode == KeyEvent.KEYCODE_ESCAPE
						|| keyCode == KeyEvent.KEYCODE_BACK
						|| keyCode == KeyEvent.KEYCODE_HOME) {
					if (mIndex == 3) {
						backSettingParent();
					} else {
						dismiss();
					}
					return true;
				} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
					System.out.println(" mIndex " + mIndex);
					if (mIndex > 0 && mIndex <=2) {
						leaveMenu(mIndex);
						mIndex--;
						// mMenuCotent.setInAnimation(mContext,
						// R.anim.push_left_in);
						// mMenuCotent.setOutAnimation(mContext,
						// R.anim.push_left_out);
						mMenuCotent.showPrevious();
						enterSubMenu(mIndex);
					}
					return true;
				} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
					System.out.println(" mIndex " + mIndex);
					if (mIndex>=0 & mIndex <=1 ) {
						leaveMenu(mIndex);
						mIndex++;
						// mMenuCotent.setInAnimation(mContext,
						// R.anim.push_right_in);
						// mMenuCotent.setOutAnimation(mContext,
						// R.anim.push_right_out);
						mMenuCotent.showNext();
						enterSubMenu(mIndex);
					}
					return true;
				} else if (keyCode == KeyEvent.KEYCODE_MENU) {
					mIndex = 0;
					if (mPopupMenu != null && mPopupMenu.isShowing())
						mPopupMenu.dismiss();
					return true;
				} else if (keyCode == DvbKeyEvent.KEYCODE_LIST) {
					if (mIndex != MENU_CHANNEL_LIST) {
						enterSubMenu(MENU_CHANNEL_LIST);
					} else {
						dismiss();
					}
					return true;
				}
			}

			return false;
		}

		@Override
		public void showSettingSub(int index) {
//			mMenuCotent.setInAnimation(mContext, R.anim.push_right_in);
//			mMenuCotent.setOutAnimation(mContext, R.anim.push_right_out);
			switch (index) {
			case MenuSetting.SHOW_APPOINTMENT://2
//				if(mMenuReservationList == null){
					mMenuReservationList = (MenuReservationList) mInflater.inflate(R.layout.menu_reservation_list, null);
//				}
				mMenuReservationList.fillData(mActivity);
				mMenuReservationList.setInterceptKeyListener(mInterceptKeyListener);
				if(mMenuCotent.getChildCount() > 3){
					mMenuCotent.removeViewAt(3);
				}
				mMenuCotent.addView(mMenuReservationList);
				mMenuCotent.showNext();
				mMenuReservationList.getFocus();
				break;
			case MenuSetting.SHOW_FAVORITE_CHANNEL://1
				if(mMenuFavoriteList == null){
					mMenuFavoriteList = (MenuFavoriteList) mInflater.inflate(R.layout.menu_favorite_list_layout, null);
				}
				mMenuFavoriteList.fillData(mActivity ,mController);
				mMenuFavoriteList.setInterceptKeyListener(mInterceptKeyListener);
				if(mMenuCotent.getChildCount() > 3){
					mMenuCotent.removeViewAt(3);
				}
				mMenuCotent.addView(mMenuFavoriteList);
				mMenuCotent.showNext();
				mMenuFavoriteList.getFocus();
				break;
			case MenuSetting.SHOW_SOUND_TRACK:
			case MenuSetting.SHOW_AUDIO_INDEX:
			case MenuSetting.SHOW_PICTURE_PROPORTION:
				if(mSettingSubView == null){
					mSettingSubView = (MenuSettingSub) mInflater.inflate(R.layout.menu_setting_sub, null);
				}
				mSettingSubView.setInterceptKeyListener(mInterceptKeyListener);
				mSettingSubView.setController(mController);
				if(mMenuCotent.getChildCount() > 3){
					mMenuCotent.removeViewAt(3);
				}
				mMenuCotent.addView(mSettingSubView);
				if(index == MenuSetting.SHOW_SOUND_TRACK){
					mSettingSubView.showSoundTrackSettingWindow();
				}
				if(index == MenuSetting.SHOW_AUDIO_INDEX){
					mSettingSubView.showAudioIndexSettingWindow();
				}
				if(index == MenuSetting.SHOW_PICTURE_PROPORTION){
					mSettingSubView.showScreenSettingWindow();
				}
				mMenuCotent.showNext();
				mSettingSubView.getFocus();
			break;
			case MenuSetting.SHOW_SYSTEM_INFORMATION:
				if(mMenuSysInfoView == null){
					mMenuSysInfoView = (MenuSystemInfo) mInflater.inflate(R.layout.menu_system_info, null);
				}
				mMenuSysInfoView.fillData();
				mMenuSysInfoView.setInterceptKeyListener(mInterceptKeyListener);
				if(mMenuCotent.getChildCount() > 3){
					mMenuCotent.removeViewAt(3);
				}
				mMenuCotent.addView(mMenuSysInfoView);
				mMenuCotent.showNext();
				mMenuSysInfoView.getFocus();
				break;
			default:
				break;
			}
			mMenuTitle.setAlpha(0.4f);
			mIndex = 3;
		}

//		@Override
//		public void backSettingParent() {
////			mMenuCotent.setInAnimation(mContext, R.anim.push_left_in);
////			mMenuCotent.setOutAnimation(mContext, R.anim.push_left_out);
//			mMenuCotent.showPrevious();
//			mSettingView.refreshData();
//			mSettingView.getFocus();
//			mMenuTitle.setAlpha(1.0f);
//			mIndex = 2;
//		}

		@Override
		public void exitMenu() {
			dismiss();
		}
	};
	private void dismiss(){
		mIndex = 0;
		if(mPopupMenu !=null && mPopupMenu.isShowing()){
			mPopupMenu.dismiss();
			if(mMenuTitle!=null){
				mMenuTitle.setAlpha(1.0f);
			}
		}
	}
	interface InterceptKeyListener {
		boolean onKeyEvent(int keyCode,int action);
		void showSettingSub(int index);
		void exitMenu();
	}
	
	private void backSettingParent() {
		// mMenuCotent.setInAnimation(mContext, R.anim.push_left_in);
		// mMenuCotent.setOutAnimation(mContext, R.anim.push_left_out);
		mMenuCotent.showPrevious();
		mSettingView.refreshData();
		mSettingView.getFocus();
		mMenuTitle.setAlpha(1.0f);
		mIndex = 2;
	}
	
	interface MenuListener {
		void getFocus();
		void loseFocus();
	}
	
	@Override
	public void processMessage(Object sender, DvbMessage msg) {
		switch (msg.what) {
		case ViewMessage.SHOW_MAIN_MENU:
			mController = (ViewController) sender;
			showMenu(MENU_PROGRAM_GUIDE);
			break;
		case ViewMessage.SHOW_CHANNEL_LIST_WINDOWN:
			mController = (ViewController) sender;
			showMenu(MENU_CHANNEL_LIST);
			break;
		case ViewMessage.SHOW_FAVORITE_CHANNEL_WINDOWN:
			mController = (ViewController) sender;
			showMenu(MENU_SETTINGS);
			mInterceptKeyListener.showSettingSub(MenuSetting.SHOW_FAVORITE_CHANNEL);
			break;
		case ViewMessage.SHOW_SOUNDTRACK_WINDOWN:
			mController = (ViewController) sender;
			showMenu(MENU_SETTINGS);
			mInterceptKeyListener.showSettingSub(MenuSetting.SHOW_SOUND_TRACK);
			break;
		case ViewMessage.SHOW_PROGRAM_RESERVE_ALERT:
		case ViewMessage.SWITCH_PLAY_MODE:
		case ViewMessage.STOP_PLAY:
			dismiss();
			break;
		}
	}
	
	public void setTitleFocus(int index){
		switch (index) {
		case MENU_PROGRAM_GUIDE:
			mProgramGuideFocusView.setVisibility(View.VISIBLE); 
			mChannelListFocusView.setVisibility(View.GONE);
			mSettingsFocusView.setVisibility(View.GONE); 
			
			mProgramGuideNoFocusView.setVisibility(View.GONE);
			mChannelListNoFocusView.setVisibility(View.VISIBLE);
			mSettingsNoFocusView.setVisibility(View.VISIBLE);
			break;
		case MENU_CHANNEL_LIST:
			mProgramGuideFocusView.setVisibility(View.GONE); 
			mChannelListFocusView.setVisibility(View.VISIBLE);
			mSettingsFocusView.setVisibility(View.GONE); 
			
			mProgramGuideNoFocusView.setVisibility(View.VISIBLE);
			mChannelListNoFocusView.setVisibility(View.GONE);
			mSettingsNoFocusView.setVisibility(View.VISIBLE);
			break;
		case MENU_SETTINGS:
			mProgramGuideFocusView.setVisibility(View.GONE); 
			mChannelListFocusView.setVisibility(View.GONE);
			mSettingsFocusView.setVisibility(View.VISIBLE); 
			
			mProgramGuideNoFocusView.setVisibility(View.VISIBLE);
			mChannelListNoFocusView.setVisibility(View.VISIBLE);
			mSettingsNoFocusView.setVisibility(View.GONE);
			break;
		default:
			break;
		}
	}
}
