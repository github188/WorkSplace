package novel.supertv.dvb.utils;

import android.view.KeyEvent;

/**
 * DVB应用使用的键值
 * 不同的遥控器都需要更改这个文件，以匹配
 * 目前的键值是针对铁岭项目的遥控器
 * @author dr
 *
 */
public class DvbKeyEvent {

    /////////////////////////////////////////////////////////////////////////////
    /////  adtv项目，三奥遥控器使用  /////
    ////////////////////////////////////////////////////////////////////////////
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
    public static final int KEYCODE_VOLUME_SET = KeyEvent.KEYCODE_Q;// 45
    /** 信息键 */
    public static final int KEYCODE_INFO = KeyEvent.KEYCODE_Y;// 53
    /** 点播切换键 */
    public static final int KEYCODE_DEMAND = KeyEvent.KEYCODE_LEFT_BRACKET;// 71
    /** 资讯键 */
    public static final int KEYCODE_NEWS = KeyEvent.KEYCODE_T;// 48
    /** *号键 */
    public static final int KEYCODE_XING = KeyEvent.KEYCODE_N;// 42
    /** #号键 */
    public static final int KEYCODE_JING = KeyEvent.KEYCODE_M;// 41
    /** 节目表键 */
    public static final int KEYCODE_LIST = KeyEvent.KEYCODE_O;// 43
    /** 高清键 */
    public static final int KEYCODE_HD = KeyEvent.KEYCODE_A;// 29
    /** 分类键 */
    public static final int KEYCODE_CATE = KeyEvent.KEYCODE_I;// 37
    
    //////////////////////////////////////////////////////////////////////////////////
    // 下面的备用
    /////////////////////////////////////////////////////////////////////////////////
    
    
    /** 画中画键 */
    public static final int KEYCODE_HUAZHONGHUA = 96;
    /** 画中画上键 */
    public static final int KEYCODE_HUAZHONGHUA_UP= 97;
    /** 画中画下键 */
    public static final int KEYCODE_HUAZHONGHUA_DOWN = 98;
    /** 画中画更换键 */
    public static final int KEYCODE_HUAZHONGHUA_CHANGE = 99;
    /** 向上翻页键 */
    public static final int KEYCODE_PAGE_UP = 101;
    /** 向下翻页键 */
    public static final int KEYCODE_PAGE_DOWN = 102;
    /** 音量加键 */
    public static final int KEYCODE_VOLUME_UP = 24;
    /** 音量减键 */
    public static final int KEYCODE_VOLUME_DOWN = 25;
    /** 静音键 */
    public static final int KEYCODE_MUTE = 91;
    /** 时移键 */
    public static final int KEYCODE_TIME_MOVE = 100;
    /** 快退键 */
    public static final int KEYCODE_LEFT_MOVE = 89;
    /** 快进键 */
    public static final int KEYCODE_RIGHT_MOVE = 90;
    /** 播放键 */
    public static final int KEYCODE_TIME_PLAY = 85;
    /** 停止键 */
    public static final int KEYCODE_TIME_STOP = 86;
    /** 上一个键 */
    public static final int KEYCODE_LAST = 88;
    /** 下一个键 */
    public static final int KEYCODE_NEXT = 87;
    /** 暂停键 */
    public static final int KEYCODE_PAUSE = 105;
    /** 录制键 */
    public static final int KEYCODE_REC = 94;
}
