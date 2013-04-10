package com.joysee.adtv.logic;

/**
 * 2012-11-02
 * 设置电视声道 伴音 音量和相关参数
 * @author wuhao
 */
public class SettingManager {
    private SettingManager() {
    }

    private static SettingManager mSettingsManager = new SettingManager();
    
    synchronized public static SettingManager getSettingManager() {
        if (mSettingsManager == null) {
            mSettingsManager = new SettingManager();
        }
        return mSettingsManager;
    }

    /**
     * 设置多语言切换/伴音
     * @param language
     * @return 
     */
    public native int nativeSetAudioLanguage(int language);

    /**
     * 获取伴音个数
     * @return 
     */
    public native int nativeGetAudioLanguage();
    /**
     * 设置声道模式
     * @param Mode
     * @return
     */
    public native int nativeSetSoundTrackMode(int Mode);
    /**
     * 获取声道模式
     * @return
     */
    public native int nativeGetSoundTrackMode();
    /**
     * 设置视频窗口位置及大小
     */
    public native int nativeSetVideoWindow(int x, int y, int width, int height);
    /**
     * 获取全屏状态
     */
    public native boolean nativeIsVideoFull();

    /**
     * 设置当前画面比例
     */
    public native int nativeSetVideoAspectRatio(int DisplayAspectRatio);
    /**
     * 获取当前画面比例
     */
    public native int nativeGetVideoAspectRatio(int DisplayAspectRatio);
    /**
     * 从ts流中获取时间,标准东八区时间秒数.
     * @return
     */
    public native String nativeGetTimeFromTs();
    
}
