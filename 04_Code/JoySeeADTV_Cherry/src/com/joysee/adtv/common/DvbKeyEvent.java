package com.joysee.adtv.common;

import android.view.KeyEvent;

/**
 * DVB应用使用的键值 不同的遥控器都需要更改这个文件，以匹配 目前的键值是针对铁岭项目的遥控器
 * 
 * @author wgh
 */
public class DvbKeyEvent {

    // ///////////////////////////////////////////////////////////////////////////
    // /// adtv项目，三奥遥控器使用 /////
    // //////////////////////////////////////////////////////////////////////////
    /** 红色键 */
    public static final int KEYCODE_RED = KeyEvent.KEYCODE_PROG_RED;// 183
    /** 黄色键 */
    public static final int KEYCODE_YELLOW = KeyEvent.KEYCODE_PROG_YELLOW;// 185
    /** 绿色键 */
    public static final int KEYCODE_GREEN = KeyEvent.KEYCODE_PROG_GREEN;// 184
    /** 蓝色键 */
    public static final int KEYCODE_BLUE = KeyEvent.KEYCODE_PROG_BLUE;// 186
    /** 喜爱键 */
    public static final int KEYCODE_FAVORITE = KeyEvent.KEYCODE_D;// 32
    /** 回看键 */
    public static final int KEYCODE_BACK_SEE = KeyEvent.KEYCODE_MEDIA_REWIND;// 89
    /** 节目指南键 */
    public static final int KEYCODE_PROGRAM_GUIDE = KeyEvent.KEYCODE_R;// 46
    /** 电视/广播键 */
    public static final int KEYCODE_TV_BC = KeyEvent.KEYCODE_ALT_RIGHT;// 58
    /** 声道键 */
    public static final int KEYCODE_SOUNDTRACK_SET = KeyEvent.KEYCODE_Q;// 45
    /** 信息键 */
    public static final int KEYCODE_INFO = KeyEvent.KEYCODE_Y;// 53
    /** 点播切换键 */
    /** 节目表键 */
    public static final int KEYCODE_LIST = KeyEvent.KEYCODE_O;// 43
}
