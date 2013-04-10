package com.joysee.adtv.common;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * 用于获取画面填充类型 xml
 */
public class ScreenModeUtil {
	private static final DvbLog log = new DvbLog("com.joysee.adtv.common.ScreenModeUtil", DvbLog.DebugType.D);

	// Suppress default constructor for noninstantiability
	private ScreenModeUtil() {
		throw new AssertionError("Suppress default constructor for noninstantiability");
	}

	/**
	 * xml的名称
	 */
	public final static String PREFERENCE_DVB_SCREEN_NAME = "volume_mode";

	/** 画面填充模式属性名 */
	public final static String DVB_SCREEN_MODE = "dvb_screen_mode";

	/**
	 * 设置画面填充模式
	 * 
	 * @param context
	 * @param mode
	 */
	public static void saveScreenMode(Context context, int mode) {
		SharedPreferences share = context.getSharedPreferences(PREFERENCE_DVB_SCREEN_NAME, Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = share.edit();

		editor.putInt(DVB_SCREEN_MODE, mode);
		log.D("savePlayMode=" + mode);
		editor.commit();

	}

	/**
	 * 获取画面填充模式
	 * 
	 * @param context
	 * @return
	 */
	public static int getScreenMode(Context context) {
		new File("/data/data/com.joysee.adtv/shared_prefs/"+PREFERENCE_DVB_SCREEN_NAME+".xml.bak").delete();
		SharedPreferences share = context.getSharedPreferences(PREFERENCE_DVB_SCREEN_NAME, Activity.MODE_PRIVATE);
		int mode = share.getInt(DVB_SCREEN_MODE, 1);
		log.D("getPlayMode=" + mode);
		return mode;// 1为全屏 填充画面
	}
}
