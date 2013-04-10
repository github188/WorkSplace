package com.joysee.adtv.ui.adapter;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.controller.ViewController;
import com.joysee.adtv.db.Channel;
import com.joysee.adtv.logic.SettingManager;
import com.joysee.adtv.ui.MenuSetting;

public class SettingMenuAdapter extends BaseAdapter {
	
	
	private ViewController mViewController;
	private Context mContext;
	private String[] adjust_items_setting_menu;
	private LayoutInflater layoutInflater;
	private SettingManager mSettingManager;
	


	public SettingMenuAdapter(Context context, String[] adjust,
			ViewController mController) {
		this.mContext = context;
		layoutInflater = LayoutInflater.from(context);
		adjust_items_setting_menu = adjust;
		mViewController = mController;
		mSettingManager = SettingManager.getSettingManager();
	}

	@Override
	public int getCount() {
		return adjust_items_setting_menu.length;
	}

	@Override
	public Object getItem(int position) {
		return adjust_items_setting_menu[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.dvb_menu_list_item,
					null);
		}
		TextView menuText = (TextView) convertView
				.findViewById(R.id.menu_list_item_text);
		TextView menuState = (TextView) convertView
				.findViewById(R.id.menu_list_item_state);

		// 从list对象中为子组件赋值
		menuText.setText(adjust_items_setting_menu[position]);
		switch (position) {
		case  MenuSetting.SHOW_PICTURE_PROPORTION:
			int mode = mViewController.getDisplayMode();
			if (mode == 0) {
				menuState.setText(R.string.menu_settings_screen_mode_original);
			} else {
				menuState.setText(R.string.menu_settings_screen_mode_filling);
			}
			break;
		case  MenuSetting.SHOW_AUDIO_INDEX:
			int audioIndex = mViewController.getAudioIndex();
			menuState.setText(mContext.getResources().getString(R.string.menu_settings_audio_index) + audioIndex);
			break;
		case  MenuSetting.SHOW_FAVORITE_CHANNEL:
			int count = mViewController.getFavoriteCount();
			menuState.setText(count + mContext.getResources().getString(R.string.menu_settings_channel));
			break;
		case  MenuSetting.SHOW_SMART_CARD:

			break;
//		case  MenuSetting.SHOW_BRIGHTNESS:
//
//			break;
		case  MenuSetting.SHOW_SOUND_TRACK:
			mViewController.getSoundTrack();
			final int channelNum = mViewController.getChannelNum();
			if (channelNum != -1) {
				int soundTrack = mViewController.getSoundTrack();
				if (soundTrack == 0) {
					menuState.setText(R.string.menu_settings_audip_mode_stereo);
				} else if (soundTrack ==1) {
					menuState.setText(R.string.menu_settings__audio_mode_left);
				} else if (soundTrack ==2) {
					menuState.setText(R.string.menu_settings_audio_mode_right);
				}
			}
			break;
		case MenuSetting.SHOW_APPOINTMENT:
			menuState.setText(getReservationCount() + mContext.getResources().getString(R.string.menu_settings_reservation_channel));
			break;
		case MenuSetting.SHOW_SEARCH_CHANNEL:
			menuState.setText(mViewController.getAllChannelCount() + mContext.getResources().getString(R.string.menu_settings_channel));
			break;
		default:
			break;
		}
		return convertView;
	}

	public void setController(ViewController ViewController) {
		mViewController = ViewController;
	}
	
	public int getReservationCount(){
		Cursor cursor = mContext.getContentResolver().query(Channel.URI.TABLE_RESERVES, null, null, null, null);
		while (cursor.moveToNext()) {
            long startTimeDB = (long)cursor.getInt(cursor.getColumnIndex(
                    Channel.TableReservesColumns.STARTTIME));
            if(startTimeDB*1000 < getUtcTime()){
                mContext.getContentResolver().delete(Channel.URI.TABLE_RESERVES, 
                        Channel.TableReservesColumns.STARTTIME+"=?", new String[]{""+startTimeDB});
            }
        }
		cursor.requery();
		int count = cursor.getCount();
		cursor.close();
		return count;
	}
	private long getUtcTime() {
        String utcTimeStr = mSettingManager.nativeGetTimeFromTs();
        String[] utcTime = utcTimeStr.split(":");
        long currentTimeMillis = Long.valueOf(utcTime[0])*1000;
        return currentTimeMillis;
    }

}