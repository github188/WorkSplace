package novel.supertv.dvb.jni.struct;


public class IPPlayStartParam {
//    public boolean      bUseLnb     = false;    /* 是否使用lnb */
//    public stLNB        lnbParam    = null; /* lnb参数 */
    public Transponder  tpParam     = null; /* 节目的调频参数 */
    public stChannel    service     = null;     /* 节目基本信息 */
    /* following parameters will be act after play successed, when valid value was set by user. otherwise nothing will happen.
    * 以下配置参数，如果有效才会在播放成功后立即设置，否者维持原样(即之前最后一次的有效设置) */
//    public Rect         winSize     = null;     /* 视频窗口坐标大小 */
//    public int          videoWin    = JniCommonDefination.eVideoWindow.eVideoWindow_MaxNum; /* 指定播放的视频窗口 eVideoWindow*/
//    public int          audioMode   = JniCommonDefination.eAudioChannelMode.eAudioChannelMode_Stereo;   /* 音频声道模式 */
}
