package novel.supertv.dvb.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import novel.supertv.dvb.DvbApplication;
import novel.supertv.dvb.R;
import novel.supertv.dvb.adapter.EpgChannelListAdapter;
import novel.supertv.dvb.adapter.EpgProgramListAdapter;
import novel.supertv.dvb.adapter.ProgramReservesListAdapter;
import novel.supertv.dvb.jni.JniChannelPlay;
import novel.supertv.dvb.jni.JniChannelPlay.OnMonitorListener;
import novel.supertv.dvb.jni.JniChannelPlay.OnPfListener;
import novel.supertv.dvb.jni.JniEpgSearch;
import novel.supertv.dvb.jni.JniEpgSearch.OnEpgListener;
import novel.supertv.dvb.jni.JniSetting;
import novel.supertv.dvb.jni.struct.tagAudioStereoMode;
import novel.supertv.dvb.jni.struct.tagDVBService;
import novel.supertv.dvb.jni.struct.tagEpgEvent;
import novel.supertv.dvb.jni.struct.tagMiniEPGNotify;
import novel.supertv.dvb.provider.Channel;
import novel.supertv.dvb.utils.AdapterViewSelectionUtil;
import novel.supertv.dvb.utils.CaNotifyString;
import novel.supertv.dvb.utils.ChannelNumberUtil;
import novel.supertv.dvb.utils.ChannelNumberUtil.OnSwitchChannelListener;
import novel.supertv.dvb.utils.ChannelTypeNumUtil;
import novel.supertv.dvb.utils.DateFormatUtil;
import novel.supertv.dvb.utils.DefaultParameter;
import novel.supertv.dvb.utils.DvbKeyEvent;
import novel.supertv.dvb.utils.DvbLog;
import novel.supertv.dvb.utils.VideoLayerUtils;
import novel.supertv.dvb.utils.ViewGroupUtil;
import novel.supertv.dvb.utils.ViewGroupUtil.OnItemNeedHideListener;
import novel.supertv.dvb.utils.ViewGroupUtil.OnItemNeedShowListener;
import novel.supertv.dvb.utils.VolumeModeUtil;
import novel.supertv.dvb.view.CustomProgressDialog;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Instrumentation;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

public class PlayActivity extends Activity {


    private static final DvbLog log = new DvbLog(
            "novel.supertv.dvb.app.activity.PlayActivity",DvbLog.DebugType.D);

    /**
     * 工作用的Handler对象
     */
    private Handler workHandler;
    
    private Handler keyWorkHandler;

    /**
     * 工作线程
     */
    private HandlerThread workThread = new HandlerThread("play work thread");
    
    private HandlerThread keyWorkThread = new HandlerThread("deal keyevent thread");

    /**
     * 查询出的频道cursor
     * 在初始化后交给activity管理
     */
    private Cursor channelCursor;

    /**
     * 当前频道的索引
     */
    private int mCurrentChannelIndex;

    /**
     * 当前频道号
     */
    private int mCurrentChannelNumber;

    /**
     * 上一个频道的索引
     */
    private int mPreChannelIndex;

    /**
     * 播放电视的视图
     */
    private SurfaceView surfaceView;

    private SurfaceHolder surfaceHolder; 

    /**
     * 播放管理器
     */
    private MediaPlayer mediaPlayer;

    /**
     * 测试jni播放
     */
    private JniChannelPlay mJniPlay;
    private JniSetting mJniSetting;

    /**
     * 上一个频道的播放参数
     */
    private int mPrePmtPid;
    private int mPreVideoPid;
    private int mPreAudioPid;

    private List<Uri> uriList = new ArrayList<Uri>();

    /**
     * 对频道号对应的喜爱标记进行一次备份,使喜爱列表能正常显示.
     */
    private Map<String,String> mFavoritesMap = new HashMap<String,String>();

    /**
     * 对频道号对应的音量值进行一次拷贝
     * 用于调节音量和保存到DB
     */
    private Map<Integer,Integer> mVolumesMap = new HashMap<Integer,Integer>();

    private int positionOfUriList;

    // views
    // *******************************************
    private LayoutInflater layoutInflater;
    private FrameLayout layout_tv;
    private FrameLayout layout_bc;
    // public used
    
    private ViewGroupUtil viewGroup = new ViewGroupUtil();

//    CaDialogManager mCaDialogManager = new CaDialogManager(PlayActivity.this);

    private boolean isFirstIn = false;
    
    private boolean mIsRepeating = false;

    private int mDelayTime = 700;
    /**
     * view的显示时间宏定义
     */
    public class ViewShowTime{
        private static final int VIEW_HIDE_TIME_VOLUMEBAR = 3000;
        private static final int VIEW_HIDE_TIME_MUTE = -1;
        public static final int VIEW_HIDE_TIME_KEY_MENU = 10000;
        private static final int VIEW_HIDE_TIME_CHNUMBER = 5000;
        private static final int VIEW_HIDE_TIME_CHINFO = 5000;
        private static final int VIEW_HIDE_TIME_SUBMENU = 0;
    }

    /**
     * 给View编号
     *
     */
    private class ViewGroupKey {
        private static final int VIEW_GROUP_KEY_VOLUMEBAR = 0;
        private static final int VIEW_GROUP_KEY_MUTE = 1;
        private static final int VIEW_GROUP_KEY_MENU = 2;
        private static final int VIEW_GROUP_KEY_CHNUMBER = 3;
        private static final int VIEW_GROUP_KEY_CHINFO = 4;
        private static final int VIEW_GROUP_KEY_SUBMENU = 5;
    }

    /**
     * 将View加入到工具中
     */
    private void addToViewGroup() {
        List<Integer> list = new ArrayList<Integer>();
        viewGroup.addItem(ViewGroupKey.VIEW_GROUP_KEY_VOLUMEBAR, volumeLinear,
                ViewShowTime.VIEW_HIDE_TIME_VOLUMEBAR, null);
        
        list.add(ViewGroupKey.VIEW_GROUP_KEY_CHINFO);
        list.add(ViewGroupKey.VIEW_GROUP_KEY_MENU);
        list.add(ViewGroupKey.VIEW_GROUP_KEY_SUBMENU);
        list.add(ViewGroupKey.VIEW_GROUP_KEY_CHNUMBER);
        viewGroup.addItem(ViewGroupKey.VIEW_GROUP_KEY_MUTE,
                volumeMuteLinear, ViewShowTime.VIEW_HIDE_TIME_MUTE, list);
        
        list = new ArrayList<Integer>();
        list.add(ViewGroupKey.VIEW_GROUP_KEY_SUBMENU);
        list.add(ViewGroupKey.VIEW_GROUP_KEY_MENU);
        list.add(ViewGroupKey.VIEW_GROUP_KEY_MUTE);
        viewGroup.addItem(ViewGroupKey.VIEW_GROUP_KEY_MENU, showMenuWindow, 
                ViewShowTime.VIEW_HIDE_TIME_KEY_MENU,list);
        
        list.add(ViewGroupKey.VIEW_GROUP_KEY_CHINFO);
        list.add(ViewGroupKey.VIEW_GROUP_KEY_MUTE);
        viewGroup.addItem(ViewGroupKey.VIEW_GROUP_KEY_CHNUMBER,
                channelNumberLinear, ViewShowTime.VIEW_HIDE_TIME_CHNUMBER, list);
        
        list = new ArrayList<Integer>();
        list.add(ViewGroupKey.VIEW_GROUP_KEY_CHNUMBER);
        list.add(ViewGroupKey.VIEW_GROUP_KEY_MUTE);
        viewGroup.addItem(ViewGroupKey.VIEW_GROUP_KEY_CHINFO, channelInfoView,
                ViewShowTime.VIEW_HIDE_TIME_CHINFO, list);
        
        viewGroup.setOnItemNeedShowListener(new OnItemNeedShowListener() {
            
            public void onItemNeedShow(int itemId, Object viewItem) {
                switch (itemId) {
                case ViewGroupKey.VIEW_GROUP_KEY_VOLUMEBAR:
                    if (volumeLinear != null)
                        volumeLinear.setVisibility(View.VISIBLE);
                    break;
                case ViewGroupKey.VIEW_GROUP_KEY_MUTE:
                    if (volumeMuteLinear != null)
                        volumeMuteLinear.setVisibility(View.VISIBLE);
                    break;
                case ViewGroupKey.VIEW_GROUP_KEY_CHNUMBER:
                    if (channelNumberLinear != null)
                        channelNumberLinear.setVisibility(View.VISIBLE);
                    break;
                case ViewGroupKey.VIEW_GROUP_KEY_CHINFO:
                    if (channelInfoView != null)
                        channelInfoView.setVisibility(View.VISIBLE);
                    break;
                case ViewGroupKey.VIEW_GROUP_KEY_MENU:
                    
                    if(mCurrentPlayType == PLAY_TYPE_TV){
                        showMenuWindow(PopupMsgId.SHOW_MENU, PopMenuList.CHANNEL_LIST);
                    }else if(mCurrentPlayType == PLAY_TYPE_BC){
                        showMenuWindow(PopupMsgId.SHOW_BROADCASE_MENU, PopMenuList.CHANNEL_LIST);
                    }
                    
                    break;
                }
            }
            
        });

        viewGroup.setOnItemNeedHideListener(new OnItemNeedHideListener() {
            
            public void onItemNeedHide(int itemId, Object viewItem) {
                switch (itemId) {
                case ViewGroupKey.VIEW_GROUP_KEY_VOLUMEBAR:
                    if (volumeLinear != null)
                        volumeLinear.setVisibility(View.INVISIBLE);
                    break;
                case ViewGroupKey.VIEW_GROUP_KEY_MUTE:
                    if (volumeMuteLinear != null)
                        volumeMuteLinear.setVisibility(View.INVISIBLE);
                    break;
                case ViewGroupKey.VIEW_GROUP_KEY_CHNUMBER:
                    if (channelNumberLinear != null)
                        channelNumberLinear.setVisibility(View.INVISIBLE);
                    break;
                case ViewGroupKey.VIEW_GROUP_KEY_CHINFO:
                    
                    if(mCurrentPlayType == PLAY_TYPE_TV){
                        if (channelInfoView != null)
                            channelInfoView.setVisibility(View.INVISIBLE);
                    }
                    
                    break;
                case ViewGroupKey.VIEW_GROUP_KEY_MENU:
                    if(showMenuWindow != null){
                        showMenuWindow.dismiss();
                    }
                    break;
                }
            }
        });
    }

    /** 频道编号资源 */
    private final static int[] SERVICE_ID_RES = { R.drawable.channel_number_0,
            R.drawable.channel_number_1, R.drawable.channel_number_2,
            R.drawable.channel_number_3, R.drawable.channel_number_4,
            R.drawable.channel_number_5, R.drawable.channel_number_6,
            R.drawable.channel_number_7, R.drawable.channel_number_8,
            R.drawable.channel_number_9 };

    /**
     * 数字键换台工具
     * 如果执行换台则必须实现setOnSwitchChannelListener监听
     */
    private ChannelNumberUtil chnumberUtil = new ChannelNumberUtil();

    /**
     * 是否进入确定键快速换台模式
     */
    private boolean isQuckSwitchChannel;

    // tv views
    /** 频道条，显示PF */
    private FrameLayout channelInfoView;
    /** 加锁图标 */
    @SuppressWarnings("unused")
    private ImageView lockView;
    /** 频道号视图，用于控制频道号显示与否的引用 */
    private FrameLayout channelNumberLinear;
    /** 静音条 */
    private FrameLayout volumeMuteLinear;
    /** 3位频道号,可扩充 */
    private ImageView [] channelNumberImage = new ImageView[3];
    /** 声音条布局 */
    private FrameLayout volumeLinear;
    /** 声音进度条 */
    private SeekBar volumeSeekBar;
    /** 声音进度百分数 */
    private TextView volumeSeekValueText;
    /** 频道条使用的视图 */
    private TextView channelNumText;
    private TextView channelNameText;
    private ImageView channelLove;
    private ImageView channelMoney;
    private ImageView channlePesType;
    @SuppressWarnings("unused")
    private ImageView channelLock;
    private TextView currentProgram;
    private TextView currentProgramTime;
    private TextView nextProgram;
    private TextView nextProgramTime;
    private TextView mEpgVolume;
    private TextView mEpgLanguage;
    @SuppressWarnings("unused")
    private ProgressBar channelinfoProgressBar;
    
    /** 广播背景图，存放在LinearLayout */
    private LinearLayout mBroadCastBackGround;

    /** 获取CA提醒字符串的工具类 */
    private CaNotifyString mCaNotifyUtil;

    // *******************************************

    /**
     * 全局记录当前的播放状态
     */
    private int mCurrentPlayType = PLAY_TYPE_TV;

    /**
     * 当前播放的模式，电视模式
     */
    private static final int PLAY_TYPE_TV = DefaultParameter.ServiceType.digital_television_service;

    /**
     * 当前播放的模式，广播模式
     */
    private static final int PLAY_TYPE_BC = DefaultParameter.ServiceType.digital_radio_sound_service;

    /**
     * 当前播放的模式，NVOD播放模式
     */
    @SuppressWarnings("unused")
    private static final int PLAY_TYPE_NVOD = 2;

    
    // *******************************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log.D("onCreate()");
        
        // 设置窗口透明,第一个是Alpha值
        ColorDrawable colorDrawable = new ColorDrawable(Color.argb(0, 0, 0, 0));
        getWindow().setBackgroundDrawable(colorDrawable);
        
        
        // 初始化布局显示
//        layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        // 初次加载布局，可能是电视的也可能是广播的
//        changeLayout(currentPlayType);
//        startProgressDialog();
//        mainHandler.sendEmptyMessageDelayed(MainHandlerMsg.DISMISS_INIT_DIALOG, 25*1000);
        
        setContentView(R.layout.play_layout_tv);
        
        findViews();
        addToViewGroup();
        
        // 如果是广播类型，则播放广播，同时修改广播的背景
        mCurrentPlayType = ChannelTypeNumUtil.getPlayChannelType(PlayActivity.this);
        log.D("onCreate() mCurrentPlayType = "+mCurrentPlayType);
        changePlayMode(mCurrentPlayType);
        
        jniPlayInit();
        if(mJniEpgSearch == null){
            mJniEpgSearch = new JniEpgSearch();
        }
    }

    private CustomProgressDialog progressDialog;

    public void startProgressDialog(){
        if (progressDialog == null){
            progressDialog = CustomProgressDialog.createDialog(PlayActivity.this);
        }
        progressDialog.show();
    }

    private void findViews(){
        log.D("findViews()");
        
        channelNumberLinear = (FrameLayout) findViewById(R.id.channelNumberLinear);
        volumeMuteLinear = (FrameLayout) findViewById(R.id.volume_Mute_Linear);
        channelNumberImage[0] = (ImageView) findViewById(R.id.channel_number_id_1) ;
        channelNumberImage[1] = (ImageView) findViewById(R.id.channel_number_id_2) ;
        channelNumberImage[2] = (ImageView) findViewById(R.id.channel_number_id_3) ;
        volumeLinear = (FrameLayout) findViewById(R.id.volumeLinear);
        volumeSeekBar = (SeekBar) findViewById(R.id.volume_seekBar);
        volumeSeekValueText = (TextView) findViewById(R.id.volume_seekValueText);
        
        lockView = (ImageView) findViewById(R.id.tvMainLock);
        channelInfoView = (FrameLayout)findViewById(R.id.channelInfoView);
        channelNumText=(TextView)findViewById(R.id.program_num);
        channelNameText=(TextView)findViewById(R.id.program_name);
        channelLove = (ImageView)findViewById(R.id.channelFavorite);
        channelMoney = (ImageView)findViewById(R.id.channelMoney);
        channlePesType = (ImageView) this.findViewById(R.id.volume_pestype);
        channelLock = (ImageView)findViewById(R.id.channelLock);
        mEpgVolume = (TextView) this.findViewById(R.id.miniepg_volume);
        mEpgLanguage = (TextView) this.findViewById(R.id.miniepg_language);
        currentProgram=(TextView)findViewById(R.id.current_program);
        currentProgramTime=(TextView)findViewById(R.id.current_program_time);
        nextProgram=(TextView)findViewById(R.id.next_program);
        nextProgramTime=(TextView)findViewById(R.id.next_program_time);
        
        mBroadCastBackGround = (LinearLayout) findViewById(R.id.broadcast_bg);
    }

    /**
     * 改变当前播放模式
     * @param layoutType 电视or广播
     */
    private void changePlayMode(int layoutType){
        log.D("changePlayMode layoutType = "+layoutType);
        switch(layoutType){
        case PLAY_TYPE_TV:
            mCurrentPlayType = PLAY_TYPE_TV;
            
            mBroadCastBackGround.setVisibility(View.INVISIBLE);
            
            break;
        case PLAY_TYPE_BC:
            mCurrentPlayType = PLAY_TYPE_BC;
            
            mBroadCastBackGround.setVisibility(View.VISIBLE);
            
            break;
        }
        
    }

//    /**
//     * 改变当前布局
//     * @param layoutType 布局类型
//     */
//    private void changeLayout(int layoutType){
//        log.D("changeLayout layoutType = "+layoutType);
//        switch(layoutType){
//        case PLAY_TYPE_TV:
//            mCurrentPlayType = PLAY_TYPE_TV;
//            layout_tv = (FrameLayout) layoutInflater.inflate(R.layout.play_layout_tv, null);
//            this.setContentView(layout_tv);
//            findView(layout_tv);
//            
//            addToViewGroup();
//            
//            break;
//        case PLAY_TYPE_BC:
//            mCurrentPlayType = PLAY_TYPE_BC;
//            layout_bc = (FrameLayout) layoutInflater.inflate(R.layout.play_layout_bc, null);
//            this.setContentView(layout_bc);
//            
//            findView(layout_bc);
//            
//            break;
//        }
//        
//    }

    /**
     * 装载电视播放相关布局，这里面有一部分引用是和广播共用的,待广播布局加入后区分划出
     */
    private void findView(FrameLayout layout){
        log.D("findView()");
        
        // 电视和广播共用部分
//        surfaceView = (SurfaceView) layout.findViewById(R.id.PlaySurfaceView);
        channelNumberLinear = (FrameLayout) layout.findViewById(R.id.channelNumberLinear);
        volumeMuteLinear = (FrameLayout) layout.findViewById(R.id.volume_Mute_Linear);
        channelNumberImage[0] = (ImageView) layout.findViewById(R.id.channel_number_id_1) ;
        channelNumberImage[1] = (ImageView) layout.findViewById(R.id.channel_number_id_2) ;
        channelNumberImage[2] = (ImageView) layout.findViewById(R.id.channel_number_id_3) ;
        volumeLinear = (FrameLayout) layout.findViewById(R.id.volumeLinear);
        volumeSeekBar = (SeekBar) layout.findViewById(R.id.volume_seekBar);
        volumeSeekValueText = (TextView) layout.findViewById(R.id.volume_seekValueText);
        
        // 电视播放特有的
        lockView = (ImageView) layout_tv.findViewById(R.id.tvMainLock);
        channelInfoView = (FrameLayout)layout_tv.findViewById(R.id.channelInfoView);
        channelNumText=(TextView)layout_tv.findViewById(R.id.program_num);
        channelNameText=(TextView)layout_tv.findViewById(R.id.program_name);
        channelLove = (ImageView)layout_tv.findViewById(R.id.channelFavorite);
        channelMoney = (ImageView)findViewById(R.id.channelMoney);
        channelLock = (ImageView)layout_tv.findViewById(R.id.channelLock);
        currentProgram=(TextView)layout_tv.findViewById(R.id.current_program);
        currentProgramTime=(TextView)layout_tv.findViewById(R.id.current_program_time);
        nextProgram=(TextView)layout_tv.findViewById(R.id.next_program);
        nextProgramTime=(TextView)layout_tv.findViewById(R.id.next_program_time);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        log.D("onNewIntent() ");
        
        // 如果是广播类型，则播放广播，同时修改广播的背景
        int type = ChannelTypeNumUtil.getPlayChannelType(PlayActivity.this);
        log.D("onNewIntent() type = "+type);
        if(type == PLAY_TYPE_TV){
            mainHandler.sendEmptyMessage(MainHandlerMsg.CHANGE_LAYOUT_TV);
        }
        
    }

    @Override
    protected void onResume() {
        super.onResume();
        log.D("onResume()");
        // 如果是从后台回来的，那么就不用再执行了
        if(!workThread.isAlive()){
            startWorkThread();
        }
        if(!keyWorkThread.isAlive()){
        	startkeyWorkThread();
        }
        
//        workHandler.sendEmptyMessage(WorkHandlerMsg.JNI_INIT);
        
        // 应发哥的要求，init只能在进入应用时初始化一次
//        jniPlayTest();
        
        workHandler.sendEmptyMessage(WorkHandlerMsg.CHANNEL_CURSOR_REFRESH);
        
        workHandler.sendEmptyMessageDelayed(WorkHandlerMsg.PROGRAM_RESERVE_BIND_ALARM ,5000);
        
        IntentFilter filter = new IntentFilter();
        filter.addAction("program alarm");
        if(programReservesBroadcastReceiver ==  null){
            programReservesBroadcastReceiver = new ProgramReservesBroadcastReceiver();
        }
        PlayActivity.this.registerReceiver(programReservesBroadcastReceiver, filter);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        log.D("onAttachedToWindow()");
        setChNumberUtilListener();
        
        mCaNotifyUtil = new CaNotifyString(getApplicationContext());
        
        catFormLauncher();
    }

    @Override
    public void onDetachedFromWindow() {
        log.D("onDetachedFromWindow()");
        // 隐藏消失所有对话框及popupWindow
        if(showMenuWindow != null){
            showMenuWindow.dismiss();
        }
        
        if(showChannelsWindow != null){
            showChannelsWindow.dismiss();
        }
        
        if(showFavoritesWindow != null){
            showFavoritesWindow.dismiss();
        }
        
        if(showSymbolNoWindow != null){
            showSymbolNoWindow.dismiss();
        }
        
        if(showCaNotifyWindow != null){
            showCaNotifyWindow.dismiss();
        }
        
        super.onDetachedFromWindow();
    }

    /**
     * 设置数字键换台工具的监听，里面实现换台的功能
     */
    private void setChNumberUtilListener(){
        if(chnumberUtil != null){
            chnumberUtil.setOnSwitchChannelListener(new OnSwitchChannelListener() {
                
                public void OnSwitchChannel(int channelNum) {
                    
                    // 退出确定键快速换台模式
                    isQuckSwitchChannel = false;
                    
                    // change channel by number key
                    if (channelNum == 0) {
                        log.D("channel number is 0 !");
                        AdapterViewSelectionUtil.showToast(PlayActivity.this, R.string.channel_count_out);
                        return;
                    }
                    
                    if(channelCursor != null){
                        if(channelNum > channelCursor.getCount()){
                            
                            log.D("channel number is out max count !");
                            AdapterViewSelectionUtil.showToast(PlayActivity.this, R.string.channel_count_out);
                            chnumberUtil.clear();
                            chnumberUtil.removeMessages();
                            
                            return;
                        }
                    }
                    
                    int channelIndex = channelNum - 1;
                    
                    Message msg = new Message();
                    msg.what = WorkHandlerMsg.SWITCH_TO_SPECIAL;
                    msg.arg1 = channelIndex;
                    workHandler.sendMessage(msg);
                    
                    chnumberUtil.clear();
                    chnumberUtil.removeMessages();
                }
            });
        }
    }

    /**
     * 开始工作线程
     */
    private void startWorkThread(){
        log.D("startWorkThread()");
        
        workThread.start();
        
        workHandler = new Handler(workThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                log.D("workHandler handleMessage msg.what = "+msg.what);
                switch(msg.what){
                case WorkHandlerMsg.PLAY_START:
                    
                    playCurrentCursorInWorkThread(channelCursor);
                    
                    break;
                case WorkHandlerMsg.PLAY_STOP:
                    
                    playStopInWorkThread();
                    
                    break;
                case WorkHandlerMsg.CHANNEL_CURSOR_REFRESH:
                    
                    changeChannelCursorInWorkThread(mCurrentPlayType);
                    
                    if(channelCursor == null){
                        return;
                    }
                    if(channelCursor.getCount() > 0){
                        // 加个保险
                        removeDialog(DialogId.NO_CHANNEL_SEARCH_ASK);
                        //开机默认播放上次退出时的频道
                        int index = ChannelTypeNumUtil.getPlayChannelNum(getApplicationContext(),mCurrentPlayType);
                        
                        if(index >= 0 && index < channelCursor.getCount()){
                            channelCursor.moveToPosition(index);
                            mCurrentChannelIndex = index;
                        }else{
                            channelCursor.moveToFirst();
                            mCurrentChannelIndex = 0;
                        }
                        
                        int logicNumber = 0;
                        int columnt_channelNum = channelCursor.getColumnIndex(Channel.TableChannelsColumns.LOGICCHNUMBER);
                        if(columnt_channelNum >= 0){
                            logicNumber = channelCursor.getInt(columnt_channelNum);
                            log.D("WorkHandlerMsg.CHANNEL_CURSOR_REFRESH logicNumber ="+logicNumber);
                            mCurrentChannelNumber = logicNumber;
                        }
                        
                        playCurrentCursorInWorkThread(channelCursor);
                        mainHandler.sendEmptyMessage(MainHandlerMsg.SHOW_CHANNEL_INFO);
                        
                    }else{
                        
                        workHandler.sendEmptyMessage(WorkHandlerMsg.PLAY_STOP);
                        // 没有频道，需要显示自动搜索对话框，如果取消这个对话框就显示无频道的popWindow
                        log.D("no channels so show dialog to goto search ...");
                        showDialog(DialogId.NO_CHANNEL_SEARCH_ASK);
                    }
                    
                    break;
                case WorkHandlerMsg.SWITCH_TO_NEXT:
                    
                    switchToNextInWorkThread();
                    
                    Message msgNext = Message.obtain(mainHandler);
                    msgNext.what = MainHandlerMsg.SHOW_CHANNEL_INFO;
                    msgNext.obj = channelCursor;
                    mainHandler.sendMessage(msgNext);
                    
                    workHandler.removeMessages(WorkHandlerMsg.PLAY_START);
                    workHandler.sendEmptyMessageDelayed(WorkHandlerMsg.PLAY_START, 200);
                    
                    break;
                case WorkHandlerMsg.SWITCH_TO_PREVIOUS:
                    
                    switchToPreviousInWorkThread();
                    
                    Message msgPre = Message.obtain(mainHandler);
                    msgPre.what = MainHandlerMsg.SHOW_CHANNEL_INFO;
                    msgPre.obj = channelCursor;
                    mainHandler.sendMessage(msgPre);
                    
                    workHandler.removeMessages(WorkHandlerMsg.PLAY_START);
                    workHandler.sendEmptyMessageDelayed(WorkHandlerMsg.PLAY_START, 200);
                    
                    break;
                case WorkHandlerMsg.SWITCH_TO_SPECIAL:
                    
                    int position = msg.arg1;
                    switchToSpecialInWorkThread(position);
                    
                    Message msgSpe = Message.obtain(mainHandler);
                    msgSpe.what = MainHandlerMsg.SHOW_CHANNEL_INFO;
                    msgSpe.obj = channelCursor;
                    mainHandler.sendMessage(msgSpe);
                    
                    workHandler.sendEmptyMessage(WorkHandlerMsg.PLAY_START);
                    
                    break;
                case WorkHandlerMsg.VOLUME_DOWN:
                    
                    volumeChangeInWorkThread(false);
                    
                    break;
                case WorkHandlerMsg.VOLUME_UP:
                    
                    volumeChangeInWorkThread(true);
                    
                    break;
                case WorkHandlerMsg.VOLUME_MUTE:
                    
                    volumeMuteInWorkThread();
                    
                    break;
                case WorkHandlerMsg.VOLUME_SAVE:
                    
                    saveCurrentVolumeInWorkThread();
                    
                    break;
                case WorkHandlerMsg.JNI_INIT:
                    
                    // 应发哥的要求，init只能在进入应用时初始化一次
                    jniPlayInit();
                    
                    break;
                case WorkHandlerMsg.JNI_SETTING_VOLUME://设置声道
                    int volume = msg.arg1 ;
                    log.D("jni setting volume ===="+volume);
                    mJniSetting.setAudioChannel(volume);
                    break;
                case WorkHandlerMsg.JNI_SETTING_SCREEN://画面设置
                    int vo = msg.arg1;
                    log.D("set screen vo ="+vo);
                    mJniSetting.setDisplayMode(vo);
                    break;
                case WorkHandlerMsg.JNI_SETTING_LANGUAGE://多语言切换
                    int v = msg.arg1;
                    mJniSetting.setAudioLanguage(v);
                    break;
                case WorkHandlerMsg.JNI_GETTING_VOLUME_LANGUAGE://获取epg声道
                    int vol = mJniSetting.getVolume();
//                    int lan = mJniSetting.getAudioLanguage();
                    int audioindex = 0;
                    int audioindex_index = channelCursor.getColumnIndex(Channel.TableChannelsColumns.AUDIOINDEX);
                    if(audioindex_index >=0){
                        audioindex = channelCursor.getInt(audioindex_index);
                        log.D("playCurrentCursor audioindex = "+audioindex);
                    }
                    Message m = new Message();
                    m.what = MainHandlerMsg.EPG_VOLUME_SHOW;
                    m.arg1 = vol;
                    m.arg2 = audioindex;
                    mainHandler.sendMessage(m);
                    break;
                case WorkHandlerMsg.PROGRAM_RESERVE_TIMER_START:
                    mainHandler.sendEmptyMessage(MainHandlerMsg.PROGRAM_RESERVE_REFRESH_TIME);
                    break;
                case WorkHandlerMsg.PROGRAM_RESERVE_BIND_ALARM:
                    Cursor programReserveBindCursor = managedQuery(Channel.URI.TABLE_RESERVES, null, null, null, Channel.TableReservesColumns.STARTTIME);
                    while(programReserveBindCursor.moveToNext()){
                        int id = programReserveBindCursor.getInt(programReserveBindCursor.getColumnIndex(Channel.TableReservesColumns.ID));
                        long startTime = (long)programReserveBindCursor.getInt(programReserveBindCursor.getColumnIndex(Channel.TableReservesColumns.STARTTIME));
                        String realTimeStr = mJniEpgSearch.getUTCTime();
                        log.D("------------------------------------------------------------------------get UTC time is "+realTimeStr);
                        //+ Long.valueOf(splitTime[1])*1000;
                        String[] splitTime = realTimeStr.split(":");
                        long realTime = Long.valueOf(splitTime[0])*1000;
                        long timeCompensate = realTime - System.currentTimeMillis();
                        if(Long.valueOf(splitTime[0])==0){
                            log.D("time commpenstae is 0");
                            timeCompensate = 0;
                        }
                        if((startTime*1000+timeCompensate) < System.currentTimeMillis()){
                            continue;
                        }else {
                            addReServeProgramToAlam(id, startTime*1000+timeCompensate-60*1000);
                        }
                    }
                    break;
                }
                super.handleMessage(msg);
            }  
        };
        
    }

    /**
     * 开始处理按键工作线程
     */
    private void startkeyWorkThread(){
        log.D("startWorkThread()");
        
        keyWorkThread.start();
        
        keyWorkHandler = new Handler(keyWorkThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                log.D("keyWorkHandler handleMessage msg.what = "+msg.what);
                switch(msg.what){       
                case WorkHandlerMsg.SWITCH_TO_NEXT:
                    switchToNextInWorkThread();
                    
                    Message msgNext = Message.obtain(mainHandler);
                    msgNext.what = MainHandlerMsg.SHOW_CHANNEL_INFO;
                    msgNext.obj = channelCursor;
                    mainHandler.sendMessage(msgNext);
                    
                    workHandler.removeMessages(WorkHandlerMsg.PLAY_START);
                    workHandler.sendEmptyMessageDelayed(WorkHandlerMsg.PLAY_START, 200);
                    
                    break;
                case WorkHandlerMsg.SWITCH_TO_PREVIOUS:
                    
                    switchToPreviousInWorkThread();
                    
                    Message msgPre = Message.obtain(mainHandler);
                    msgPre.what = MainHandlerMsg.SHOW_CHANNEL_INFO;
                    msgPre.obj = channelCursor;
                    mainHandler.sendMessage(msgPre);
                    
                    workHandler.removeMessages(WorkHandlerMsg.PLAY_START);
                    workHandler.sendEmptyMessageDelayed(WorkHandlerMsg.PLAY_START, 200);
                    
                    break;
                }
                super.handleMessage(msg);
            }  
        };
        
    }

    private long mPauseStartTime;

    @Override
    protected void onPause() {
        super.onPause();
        log.D("onPause()");
        
        removeAllMessage();
        
        mPauseStartTime = System.currentTimeMillis();
        
        long a = System.currentTimeMillis();
        
//        workHandler.sendEmptyMessage(WorkHandlerMsg.PLAY_STOP);
        
        log.D("mJniPlay.stop()");
        mJniPlay.stop();
        
        long b = System.currentTimeMillis();
        
        log.D("onPause() time = "+(b -a));// 205
        
        long c = System.currentTimeMillis();
        
        log.D("onPause() finish time = "+(c - b));// 3
        
    }

    @Override
    protected void onStop() {
        super.onStop();
        log.D("onStop()");
        PlayActivity.this.unregisterReceiver(programReservesBroadcastReceiver);
        programReservesBroadcastReceiver = null;
        Cursor programReserveUnbindCursor = managedQuery(Channel.URI.TABLE_RESERVES, null, null, null, Channel.TableReservesColumns.STARTTIME);
        while(programReserveUnbindCursor.moveToNext()){
            int id = programReserveUnbindCursor.getInt(programReserveUnbindCursor.getColumnIndex(Channel.TableReservesColumns.ID));
            removeReserveProgramFromAlarm(id);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        log.D("onDestroy() $$$$$$$*************************************************************$$$$$$$");
        // 847 无JNI时的时间
        // 16000
        log.D("onDestroy() from back to destroy time = "+(System.currentTimeMillis() - mPauseStartTime));
        
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        log.D("dispatchKeyEvent event.getKeyCode = "+event.getKeyCode());
        switch(event.getKeyCode()){
        case KeyEvent.KEYCODE_VOLUME_DOWN:
            
            if(event.getAction() == KeyEvent.ACTION_DOWN){
                workHandler.sendEmptyMessage(WorkHandlerMsg.VOLUME_DOWN);
            }else{
                workHandler.removeMessages(WorkHandlerMsg.VOLUME_SAVE);
                workHandler.sendEmptyMessageDelayed(WorkHandlerMsg.VOLUME_SAVE, 200);
            }
            
            return true;
        case KeyEvent.KEYCODE_VOLUME_UP:
            
            if(event.getAction() == KeyEvent.ACTION_DOWN){
                workHandler.sendEmptyMessage(WorkHandlerMsg.VOLUME_UP);
            }else{
                workHandler.removeMessages(WorkHandlerMsg.VOLUME_SAVE);
                workHandler.sendEmptyMessageDelayed(WorkHandlerMsg.VOLUME_SAVE, 200);
            }
            
            return true;
        case KeyEvent.KEYCODE_VOLUME_MUTE:
            
            if(event.getAction() == KeyEvent.ACTION_DOWN){
                workHandler.sendEmptyMessage(WorkHandlerMsg.VOLUME_MUTE);
            }
            
            return true;
        case KeyEvent.KEYCODE_ESCAPE:
        case KeyEvent.KEYCODE_BACK:
           
            if(event.getAction() == KeyEvent.ACTION_DOWN){
                showDialog(DialogId.CONFIRM_QUIT_ADTV);
            }
            
            return true;
        case KeyEvent.KEYCODE_HOME:
            
            if(event.getAction() == KeyEvent.ACTION_UP){
                log.D("dispatchKeyEvent KEYCODE_HOME");
                
                jniPlayUninit();
                
            }
            
            break;
        }
        
        return super.dispatchKeyEvent(event);
    }

    private long startTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        startTime = System.currentTimeMillis();
        log.D("=========onKeyDown=========startTime="+startTime);
        if(mIsRepeating&&(keyCode==KeyEvent.KEYCODE_DPAD_DOWN||keyCode==KeyEvent.KEYCODE_DPAD_UP)){
            return true;
        }
        // 数字键换台
        if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
            numKeyDown(keyCode - KeyEvent.KEYCODE_0);
        }
        
        // 如果是小键盘锁打开的情况下
        if(event.isNumLockOn()){
            // 小键盘数字键
            if (keyCode >= KeyEvent.KEYCODE_NUMPAD_0 && keyCode <= KeyEvent.KEYCODE_NUMPAD_9) {
                numKeyDown(keyCode - KeyEvent.KEYCODE_NUMPAD_0);
            }
        }
        
        switch(keyCode){
        case KeyEvent.KEYCODE_DPAD_UP:
            mUpOrDown = KEYUP;
            mIsRepeating = true;
            workHandler.sendEmptyMessage(WorkHandlerMsg.SWITCH_TO_NEXT);
            keyWorkHandler.postDelayed(mRepeater, mDelayTime);
//            workHandler.sendEmptyMessage(WorkHandlerMsg.SWITCH_TO_NEXT);
            return true;
        case KeyEvent.KEYCODE_DPAD_DOWN:
            mUpOrDown = KEYDOWN ;
            mIsRepeating = true;
            workHandler.sendEmptyMessage(WorkHandlerMsg.SWITCH_TO_PREVIOUS);
            keyWorkHandler.postDelayed(mRepeater, mDelayTime);
//            workHandler.sendEmptyMessage(WorkHandlerMsg.SWITCH_TO_PREVIOUS);
            
            return true;
        case KeyEvent.KEYCODE_DPAD_LEFT:
            
            workHandler.sendEmptyMessage(WorkHandlerMsg.VOLUME_DOWN);
            
            return true;
        case KeyEvent.KEYCODE_DPAD_RIGHT:
            
            workHandler.sendEmptyMessage(WorkHandlerMsg.VOLUME_UP);
            
            return true;
        case KeyEvent.KEYCODE_DPAD_CENTER:
        case KeyEvent.KEYCODE_ENTER:
            
            if(isQuckSwitchChannel){
                chnumberUtil.switchChannelNow();
                isQuckSwitchChannel = false;
                
            }
            
            break;
        }
        
        return super.onKeyDown(keyCode, event);
    }

    private int mUpOrDown = -1;
    private static final int KEYUP = 601;
    private static final int KEYDOWN = 602 ;
    private final int mInterval  = 2000;
    private Runnable mRepeater = new Runnable() {
        public void run() {
            doRepeat(mUpOrDown);
            if (mIsRepeating) {
                keyWorkHandler.postDelayed(this, mInterval);
            }
        }
     };

     /**
      * 
      * @param upOrDown
      */
     private void doRepeat(int upOrDown){
         if(mUpOrDown==KEYDOWN){
             workHandler.sendEmptyMessage(WorkHandlerMsg.SWITCH_TO_PREVIOUS); 
         }else if(mUpOrDown == KEYUP){
             workHandler.sendEmptyMessage(WorkHandlerMsg.SWITCH_TO_NEXT);
         }else{
            
         }
     }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        long endTime = System.currentTimeMillis();
        log.D("=========OnKeyUp=========(endTime-startTime)="+(endTime-startTime));
        switch(keyCode){
        case KeyEvent.KEYCODE_DPAD_LEFT:
            
            workHandler.removeMessages(WorkHandlerMsg.VOLUME_SAVE);
            workHandler.sendEmptyMessageDelayed(WorkHandlerMsg.VOLUME_SAVE, 200);
            
            return true;
        case KeyEvent.KEYCODE_DPAD_RIGHT:
            
            workHandler.removeMessages(WorkHandlerMsg.VOLUME_SAVE);
            workHandler.sendEmptyMessageDelayed(WorkHandlerMsg.VOLUME_SAVE, 200);
            
            return true;
        case DvbKeyEvent.KEYCODE_TV_BC:// 58 电视广播键
            log.D("KEYCODE_TV_BC");
            
            if(mCurrentPlayType == PLAY_TYPE_TV){
                
                mainHandler.sendEmptyMessage(MainHandlerMsg.CHANGE_LAYOUT_BC);
                
            }else if(mCurrentPlayType == PLAY_TYPE_BC){
                
                mainHandler.sendEmptyMessage(MainHandlerMsg.CHANGE_LAYOUT_TV);
                
            }
            
            return true;
        case DvbKeyEvent.KEYCODE_LIST:// 频道列表
            
            viewGroup.hideAllViews();
            showChannelsWindow();
            
            return true;
        case KeyEvent.KEYCODE_MENU:
            
            viewGroup.showView(ViewGroupKey.VIEW_GROUP_KEY_MENU);
            
            return true;
        case DvbKeyEvent.KEYCODE_FAVORITE:// 喜爱键
            
            viewGroup.hideAllViews();
            showFavoritesWindow();
            
            return true;
        case DvbKeyEvent.KEYCODE_BACK_SEE:// 回看键
            
            backSee();
            
            return true;
        case DvbKeyEvent.KEYCODE_INFO:// 信息键
            
            if(channelInfoView != null && mCurrentPlayType == PLAY_TYPE_TV){
                if(!channelInfoView.isShown()){
                    viewGroup.showView(ViewGroupKey.VIEW_GROUP_KEY_CHINFO);
                }else{
                    if(mCurrentPlayType == PLAY_TYPE_TV){
                        channelInfoView.setVisibility(View.INVISIBLE);
                    }
                }
            }
            
            return true;
        case DvbKeyEvent.KEYCODE_PROGRAM_GUIDE:// 指南键
            
            viewGroup.hideAllViews();
            showEpgWindow();
            
            return true;
        case DvbKeyEvent.KEYCODE_VOLUME_SET:// 声道设置键
            
            
            
            return true;
        case KeyEvent.KEYCODE_DPAD_UP:
        case KeyEvent.KEYCODE_DPAD_DOWN:
            mUpOrDown = -1;
            mIsRepeating=false;
            keyWorkHandler.removeCallbacks(mRepeater);
            return true;
        }
        
        return super.onKeyUp(keyCode, event);
    }

    /**
     * 工作线程的Handler使用的消息
     *
     */
    private static class WorkHandlerMsg{
        /** 查询出所有频道 */
        public static final int CHANNEL_CURSOR_REFRESH = 1000;
        
        public static final int PLAY_START = 1001;
        public static final int PLAY_STOP = 1002;
        public static final int SWITCH_TO_NEXT = 1003;
        public static final int SWITCH_TO_PREVIOUS = 1004;
        public static final int SWITCH_TO_SPECIAL = 1005;
        public static final int VOLUME_UP = 1006;
        public static final int VOLUME_DOWN = 1007;
        public static final int VOLUME_MUTE = 1008;
        public static final int VOLUME_SAVE = 1009;
        
        public static final int JNI_INIT = 1999;
        /** 设置声道 */
        public static final int JNI_SETTING_VOLUME = 2000;
        /** 画面设置 */
        public static final int JNI_SETTING_SCREEN = 2001;
        /** 多语言切换 */
        public static final int JNI_SETTING_LANGUAGE = 2002;
        /** 获取声道 和 伴音*/
        public static final int JNI_GETTING_VOLUME_LANGUAGE = 2003;
        /** 预约提示倒计时开始 */
        public static final int PROGRAM_RESERVE_TIMER_START = 3000;
        /** 预约绑定闹钟 */
        public static final int PROGRAM_RESERVE_BIND_ALARM = 3001;
    }

    /**
     * 当离开当前页面，移除所有消息
     */
    private void removeAllMessage(){
        log.D("removeAllMessage()");
        // mainHandler：
        mainHandler.removeMessages(MainHandlerMsg.CHANGE_LAYOUT_TV);
        mainHandler.removeMessages(MainHandlerMsg.CHANGE_LAYOUT_BC);
        mainHandler.removeMessages(MainHandlerMsg.SHOW_CHANNEL_INFO);
        mainHandler.removeMessages(MainHandlerMsg.SHOW_VOLUME_BAR);
        mainHandler.removeMessages(MainHandlerMsg.SHOW_MUTE_BAR);
        mainHandler.removeMessages(MainHandlerMsg.DISMISS_INIT_DIALOG);
        mainHandler.removeMessages(MainHandlerMsg.EXIT_APP);
        mainHandler.removeMessages(MainHandlerMsg.NOTIFY_LOCKED_FAILED);
        mainHandler.removeMessages(MainHandlerMsg.NOTIFY_LOCKED_OK);
        mainHandler.removeMessages(MainHandlerMsg.EPG_SHOW);
        mainHandler.removeMessages(MainHandlerMsg.EPG_INIT_CHANNEL_ITEM_BACKGROUND);
        mainHandler.removeMessages(MainHandlerMsg.PROGRAM_RESERVE_REFRESH_TIME);
        
        // workHandler:
        workHandler.removeMessages(WorkHandlerMsg.CHANNEL_CURSOR_REFRESH);
        workHandler.removeMessages(WorkHandlerMsg.PLAY_START);
        workHandler.removeMessages(WorkHandlerMsg.PLAY_STOP);
        workHandler.removeMessages(WorkHandlerMsg.SWITCH_TO_NEXT);
        workHandler.removeMessages(WorkHandlerMsg.SWITCH_TO_PREVIOUS);
        workHandler.removeMessages(WorkHandlerMsg.SWITCH_TO_SPECIAL);
        workHandler.removeMessages(WorkHandlerMsg.VOLUME_UP);
        workHandler.removeMessages(WorkHandlerMsg.VOLUME_DOWN);
        workHandler.removeMessages(WorkHandlerMsg.VOLUME_MUTE);
        workHandler.removeMessages(WorkHandlerMsg.JNI_INIT);
    }

    /**
     * 主线程的Handler使用的消息
     */
    public static class MainHandlerMsg{
        
        /** 切换电视布局 */
        public static final int CHANGE_LAYOUT_TV = 01;
        /** 切换广播布局 */
        public static final int CHANGE_LAYOUT_BC = 02;
        
        public static final int SHOW_CHANNEL_INFO  = 03;
        public static final int SHOW_VOLUME_BAR = 04;
        public static final int SHOW_MUTE_BAR = 05;
        
        public static final int DISMISS_INIT_DIALOG = 06;
        
        public static final int PF_REFRESH = 07;
        
        public static final int EPG_VOLUME_SHOW = 8;
        
        public static final int EPG_SHOW = 11;
        public static final int EPG_INIT_CHANNEL_ITEM_BACKGROUND = 12;
        public static final int PROGRAM_RESERVE_REFRESH_TIME = 13;
        /** 退出应用 */
        public static final int EXIT_APP = 10;
        
        /** 无信号 */
        public static final int NOTIFY_LOCKED_FAILED = 100;
        /** 有信号 */
        public static final int NOTIFY_LOCKED_OK = 101;
        /** CA消息 */
        public static final int NOTIFY_CA_MESSAGE = 102;
    }

    /**
     * UI主线程的Handler
     */
    private Handler mainHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
            case MainHandlerMsg.CHANGE_LAYOUT_TV:
                
//                changeLayout(PLAY_TYPE_TV);
                changePlayMode(PLAY_TYPE_TV);
                
                workHandler.sendEmptyMessage(WorkHandlerMsg.CHANNEL_CURSOR_REFRESH);
                
                break;
            case MainHandlerMsg.CHANGE_LAYOUT_BC:
                
//                changeLayout(PLAY_TYPE_BC);
                changePlayMode(PLAY_TYPE_BC);
                
                workHandler.sendEmptyMessage(WorkHandlerMsg.CHANNEL_CURSOR_REFRESH);
                
                break;
            case MainHandlerMsg.SHOW_CHANNEL_INFO:
                
                Cursor cur = (Cursor) msg.obj;
                
                if(cur != null){
                    showChannelInfo(cur);
                    showChannelNum(cur.getPosition() + 1);
                }else{
                    log.D("MainHandlerMsg.SHOW_CHANNEL_INFO cur == null , so this is first come in");
                    showChannelInfo(channelCursor);
                    showChannelNum(channelCursor.getPosition() + 1);
                }
                
                viewGroup.showView(ViewGroupKey.VIEW_GROUP_KEY_CHINFO);
                viewGroup.showView(ViewGroupKey.VIEW_GROUP_KEY_CHNUMBER);
                
                break;
            case MainHandlerMsg.SHOW_VOLUME_BAR:
                
                volumeSeekBar.setProgress(msg.arg1);
                volumeSeekValueText.setText(""+msg.arg1);
                viewGroup.showView(ViewGroupKey.VIEW_GROUP_KEY_VOLUMEBAR);
                
                break;
            case MainHandlerMsg.SHOW_MUTE_BAR:
                
                viewGroup.showView(ViewGroupKey.VIEW_GROUP_KEY_MUTE);
                
                break;
            case MainHandlerMsg.DISMISS_INIT_DIALOG:
                
                progressDialog.dismiss();
                
                break;
            case MainHandlerMsg.PF_REFRESH:
                
                
                refreshPf((tagMiniEPGNotify)msg.obj);
                
                break;
            case MainHandlerMsg.EPG_VOLUME_SHOW:
                int volume = msg.arg1;
                int lan = msg.arg2;
                if(volume<0||lan<0) return;
                tagAudioStereoMode tag = tagAudioStereoMode.values()[volume];
                String[] adjust_items = null;
                String[] adjust_lans = null;
                adjust_items = getResources().getStringArray(R.array.volume_items);
                log.D("adjust_item          ="+adjust_items.length);
                adjust_lans = getResources().getStringArray(R.array.language_items);
                switch(tag){
                    case AUDIO_MODE_MONO://立体
                        mEpgVolume.setText(adjust_items[0]);
                        break;
                    case AUDIO_MODE_LEFT://左
                        mEpgVolume.setText(adjust_items[1]);
                        break;
                    case AUDIO_MODE_RIGHT://右
                        mEpgVolume.setText(adjust_items[2]);
                        break;
                    default :
                        mEpgVolume.setText(adjust_items[0]);//默认立体声
                        break;
                }
                switch(lan){
                    case 0:
                        mEpgLanguage.setText(adjust_lans[0]);
                        break;
                    case 1:
                        mEpgLanguage.setText(adjust_lans[1]);
                        break;
                    case 2:
                        mEpgLanguage.setText(adjust_lans[2]);
                        break;
                    default :
                        mEpgLanguage.setText(adjust_lans[0]);
                }
                break;
            case MainHandlerMsg.EXIT_APP:
                
                jniPlayUninit();
                
                break;
            case MainHandlerMsg.EPG_INIT_CHANNEL_ITEM_BACKGROUND:
                View findViewWithTag = mEpgChannelListView.findViewWithTag(mCurrentChannelIndex);
                if(findViewWithTag!=null){
                    findViewWithTag.setBackgroundResource(R.drawable.epg_listview_selected);
                }
                break;
            case MainHandlerMsg.EPG_SHOW:
                showFirstDayProgramList();
                
                break;
            case MainHandlerMsg.NOTIFY_LOCKED_FAILED:
                
                showSymbolNoWindow();
                
                break;
            case MainHandlerMsg.NOTIFY_LOCKED_OK:
                
                if(showSymbolNoWindow != null){
                    showSymbolNoWindow.dismiss();
                }
                
                break;
            case MainHandlerMsg.PROGRAM_RESERVE_REFRESH_TIME:
                mProgramReserveTimerSecond--;
                if(mProgramReserveTimerSecond <= 0){
                    showProgramReservesAlert.dismiss();
                    deleteAlertReserveData();
                    return;
                }
                workHandler.sendEmptyMessageDelayed(WorkHandlerMsg.PROGRAM_RESERVE_TIMER_START, 1000);
                mProgramReserveAlertSecondTextView.setText(""+mProgramReserveTimerSecond);
                break;
            case MainHandlerMsg.NOTIFY_CA_MESSAGE:
                
                log.D("MainHandlerMsg.NOTIFY_CA_MESSAGE arg1 = "+msg.arg1);
                String notifyString = mCaNotifyUtil.getCaNotifyString(msg.arg1);
                if(notifyString != null && !notifyString.equals("")){
                    showCaNotifyWindow(notifyString);
                }
                
                break;
            }
            super.handleMessage(msg);
        }
        
    };

    // 测试频道列表
    
    private ListView mChannelListView;
    
    private PopupWindow showChannelsWindow = null;
    private TextView mChannelTitle = null;
    private View mdateView;
    private SimpleCursorAdapter dateAdapter = null;
    private boolean isChanBackMenu = false;
    
    private void showChannelsWindow() {
        log.D("showChannelsWindow()");
        if (showChannelsWindow == null) {
            mdateView = getLayoutInflater().inflate(
                    R.layout.channels_layout, null);
            showChannelsWindow = new PopupWindow(mdateView);
            showChannelsWindow.setWidth(565);
            showChannelsWindow.setHeight(1080);
            showChannelsWindow.setFocusable(true);
            
        }
        
        mChannelListView = (ListView) mdateView.findViewById(R.id.favorites_list);
        mChannelTitle = (TextView) mdateView.findViewById(R.id.favorite_title_text);
        
        mChannelListView.setDividerHeight(0);
        
        String[] selectionArgs = new String[1];
        
        switch(mCurrentPlayType){
        case PLAY_TYPE_TV:
            mChannelTitle.setText(R.string.menu_channel_list);
            selectionArgs[0] = ""+DefaultParameter.ServiceType.digital_television_service;
            
            break;
        case PLAY_TYPE_BC:
            mChannelTitle.setText(R.string.menu_broadcast_list);
            selectionArgs[0] = ""+DefaultParameter.ServiceType.digital_radio_sound_service;
            
            break;
        }
        
        Cursor cur = getContentResolver().query(Channel.URI.TABLE_CHANNELS,
                null, Channel.TableChannelsColumns.SERVICETYPE+" = ? ", 
                selectionArgs, Channel.TableChannelsColumns.LOGICCHNUMBER);
        
        if(cur == null){
            log.D("@dingran@ cur == null");
            return;
        }
        log.D("@dingran@ cur == "+cur);
        log.D("@dingran@ cur count== "+cur.getCount());
        mdateView.setFocusable(false);
        if(cur.getCount()==0){
            mdateView.setFocusable(true);
            mdateView.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    switch(keyCode){
                  case KeyEvent.KEYCODE_ESCAPE:
                      if(event.getAction() == KeyEvent.ACTION_DOWN){
                          log.D("showDateWindow.dismiss()");
                          showChannelsWindow.dismiss();
                          return true;
                      }
                  }
                    return false;
                }
            });
        }
        dateAdapter = new SimpleCursorAdapter(getApplicationContext(), 
                R.layout.channel_list_item, cur, 
                new String[]{Channel.TableChannelsColumns.FAVORITE,Channel.TableChannelsColumns.LOGICCHNUMBER,Channel.TableChannelsColumns.SERVICENAME}, 
                new int[]{R.id.channel_list_icon,R.id.channel_list_num,R.id.channel_list_name}
                );
        
//        cur.close();
        dateAdapter.setViewBinder(new ViewBinder() {
            
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                
                if(cursor.getColumnIndex(Channel.TableChannelsColumns.FAVORITE)==columnIndex){
                    
                    if(cursor.getInt(columnIndex) == 1){
                        view.setVisibility(View.VISIBLE);
                    }else{
                        view.setVisibility(View.INVISIBLE);
                    }
                    
                    return true;
                }
                
//                if(cursor.getColumnIndex(Channel.TableChannelsColumns.ID)==columnIndex){
//                    
//                    ((TextView)view).setText(""+(cursor.getPosition() + 1));
//                    
//                    return true;
//                }
                
                return false;
            }
        });
        
        mChannelListView.setAdapter(dateAdapter);
        
        mChannelListView.setOnItemClickListener(new OnItemClickListener() {
            
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                
//                Message msg = Message.obtain();
//                msg.what = WorkHandlerMsg.SWITCH_TO_SPECIAL;
//                msg.arg1 = position;
//                workHandler.sendMessage(msg);
                
                // 精确换台
                SimpleCursorAdapter adapter = (SimpleCursorAdapter) mChannelListView.getAdapter();
                Cursor cursorMoved = (Cursor)adapter.getItem(position);
                int channelIndex = cursorMoved.getInt(cursorMoved
                        .getColumnIndex(Channel.TableChannelsColumns.LOGICCHNUMBER));
                channelIndex--;
                log.D("channelIndex = "+channelIndex);
                
                if(channelIndex >= 0 && channelIndex < channelCursor.getCount()){
                    
                    if((channelIndex+1) != mCurrentChannelNumber){
                        Message msg = Message.obtain();
                        msg.what = WorkHandlerMsg.SWITCH_TO_SPECIAL;
                        msg.arg1 = channelIndex;
                        workHandler.sendMessage(msg);
                    }
                    
                }else{
                    log.E("channel list index out channel cursor ! please check !");
                }
                
                showChannelsWindow.dismiss();
            }
        });
        
        mChannelListView.setOnKeyListener(new View.OnKeyListener() {
            
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                
                switch(keyCode){
                case KeyEvent.KEYCODE_BACK:
                    if(event.getAction() == KeyEvent.ACTION_UP){
                        log.D("showDateWindow.dismiss()");
                        showChannelsWindow.dismiss();
                        if(isChanBackMenu){
                            if(mCurrentPlayType==PLAY_TYPE_TV){
                                showMenuWindow(PopupMsgId.SHOW_MENU, PopMenuList.CHANNEL_LIST);
                            }else if(mCurrentPlayType==PLAY_TYPE_BC){
                                showMenuWindow(PopupMsgId.SHOW_BROADCASE_MENU,PopMenuList.CHANNEL_LIST);
                            }
                            
                        }
                        isChanBackMenu = false;
                    }
                    return true;
                case KeyEvent.KEYCODE_DPAD_UP:
                    
                    if(event.getAction() == KeyEvent.ACTION_DOWN){
                        if(dateAdapter.getCount() > 0 
                                && mChannelListView.getSelectedItemPosition() == 0){
                            mChannelListView.setSelection(dateAdapter.getCount() - 1);
                            return true;
                        }
                    }
                    
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    
                    if(event.getAction() == KeyEvent.ACTION_DOWN){
                        if(dateAdapter.getCount() > 0 
                                && mChannelListView.getSelectedItemPosition() 
                                == (dateAdapter.getCount() - 1)){
                            mChannelListView.setSelection(0);
                            return true;
                        }
                    }
                    
                    break;
                case KeyEvent.KEYCODE_PLUS:
                case KeyEvent.KEYCODE_EQUALS:
                case KeyEvent.KEYCODE_NUMPAD_ADD:
                    Log.d("dispatchKeyEvent", "KeyEvent.KEYCODE_PLUS");
                    if (event.getAction() == KeyEvent.ACTION_UP) {
                        /* Start the key-simulation in a thread 
                         * so we do not block the GUI. */ 
                        new Thread(new Runnable() {
                            public void run() {
                                /* Simulate a KeyStroke to the menu-button. */
                                doInjectKeyEvent(KeyEvent.KEYCODE_PAGE_UP);
                            }
                        }).start(); /* And start the Thread. */
                        
                        return true;
                    } else {
                        return true;
                    }
                case KeyEvent.KEYCODE_MINUS:
                    // 还应该有一个，没找到
                case KeyEvent.KEYCODE_NUMPAD_SUBTRACT:
                    Log.d("dispatchKeyEvent", "KeyEvent.KEYCODE_MINUS");
                    if (event.getAction() == KeyEvent.ACTION_UP) {
                        
                      /* Start the key-simulation in a thread 
                      * so we do not block the GUI. */ 
                     new Thread(new Runnable() {
                         public void run() {
                             /* Simulate a KeyStroke to the menu-button. */
                             doInjectKeyEvent(KeyEvent.KEYCODE_PAGE_DOWN);
                         }
                     }).start(); /* And start the Thread. */
                        
                        return true;
                    } else {
                        return true;
                    }
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                case KeyEvent.KEYCODE_VOLUME_UP:
                case KeyEvent.KEYCODE_VOLUME_MUTE:
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    
                    log.D("default key return true");
                    
                    if(event.getAction() == KeyEvent.ACTION_DOWN){
                        showChannelsWindow.dismiss();
                        final int key = keyCode;
                        new Thread(new Runnable() {
                            public void run() {
                                doInjectKeyEvent(key);
                            }
                        }).start();
                    }
                    
                    return true;
                }
                
                return false;
            }
        });
        
//        mChannelListView.setSelection(mCurrentChannelIndex);
        switch(mCurrentPlayType){
        case PLAY_TYPE_TV:
            
            mChannelListView.setSelection(ChannelTypeNumUtil.getPlayChannelNum(getApplicationContext(), PLAY_TYPE_TV));
            
            break;
        case PLAY_TYPE_BC:
            
            mChannelListView.setSelection(ChannelTypeNumUtil.getPlayChannelNum(getApplicationContext(), PLAY_TYPE_BC));
            
            break;
        }
        
        //TODO 如果没有频道，空的频道列表怎么退出
        
        showChannelsWindow.showAtLocation(PlayActivity.this.getWindow()
                .getDecorView(), Gravity.RIGHT, 0, 0);
    }

    // 喜爱列表
    
    private PopupWindow showFavoritesWindow = null;
    private View mfavoView;
    private SimpleCursorAdapter mfavoAdapter = null;
    private Button mfavoBtn;
    private ListView mFavoriteListView;
    private boolean isFavBackMenu = false;
    
    private void showFavoritesWindow() {
        log.D("cursor position is "+ (channelCursor.getInt(channelCursor.getColumnIndex(Channel.TableChannelsColumns.LOGICCHNUMBER))-1));
        log.D("mCurrentChannelIndex is "+ mCurrentChannelIndex);
        channelCursor.moveToPosition(mCurrentChannelIndex);
        log.D("showFavoritesWindow()");
        if (showFavoritesWindow == null) {
            mfavoView = getLayoutInflater().inflate(
                    R.layout.favorites_channels_layout, null);
            showFavoritesWindow = new PopupWindow(mfavoView);
            showFavoritesWindow.setWidth(565);
            showFavoritesWindow.setHeight(1080);
            showFavoritesWindow.setFocusable(true);
            
        }
        
        mFavoriteListView = (ListView) mfavoView.findViewById(R.id.favorites_list);
        mfavoBtn = (Button) mfavoView.findViewById(R.id.favorites_add_button);
        // 首先判断当前频道是不是喜爱频道
        int favo = 0;
        if(channelCursor.getCount() > 0 && mCurrentChannelIndex < channelCursor.getCount()){
            channelCursor.moveToPosition(mCurrentChannelIndex);
            favo = channelCursor.getInt(channelCursor
                    .getColumnIndex(Channel.TableChannelsColumns.FAVORITE));
        }
        log.D("channelCursor mfavoBtn favo = "+favo);
        
        mFavoriteListView.setDividerHeight(0);
        
        String[] selectionArgs = new String[2];
        selectionArgs[0] = ""+DefaultParameter.FavoriteFlag.FAVORITE_IS;
        
        switch(mCurrentPlayType){
        case PLAY_TYPE_TV:
            
            selectionArgs[1] = ""+DefaultParameter.ServiceType.digital_television_service;
            
            break;
        case PLAY_TYPE_BC:
            
            selectionArgs[1] = ""+DefaultParameter.ServiceType.digital_radio_sound_service;
            
            break;
        }
        
        final Cursor cur = getContentResolver().query(Channel.URI.TABLE_CHANNELS,
                null, Channel.TableChannelsColumns.FAVORITE+" = ? " +
                "and "+Channel.TableChannelsColumns.SERVICETYPE+" = ?", selectionArgs,
                Channel.TableChannelsColumns.LOGICCHNUMBER);
        
        if(cur == null){
            log.D("@dingran@ cur == null");
            return;
        }
        log.D("@dingran@ cur == "+cur);
        log.D("@dingran@ cur count== "+cur.getCount());
        
        
//        cur.close();
        
        mfavoAdapter = new SimpleCursorAdapter(getApplicationContext(), 
                R.layout.favorites_list_item, cur, 
                new String[]{Channel.TableChannelsColumns.FAVORITE,Channel.TableChannelsColumns.LOGICCHNUMBER,Channel.TableChannelsColumns.SERVICENAME}, 
                new int[]{R.id.favorite_channel_icon,R.id.favorite_channel_num,R.id.favorite_channel_name},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        
        mfavoAdapter.setViewBinder(new ViewBinder() {
            
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                
                if(cursor.getColumnIndex(Channel.TableChannelsColumns.FAVORITE)==columnIndex){
                    if(cursor.getInt(columnIndex) == 1){
                        view.setVisibility(View.VISIBLE);
                        
//                      // 默认选择当前播放的频道，在喜爱列表上
//                        log.D("favorite mCurrentChannelNumber == "+mCurrentChannelNumber);
                        int position = -1;
                        
                        if(cursor.getInt(cursor.getColumnIndex(Channel.TableChannelsColumns.LOGICCHNUMBER))
                                == mCurrentChannelNumber){
                            
                            position = cursor.getPosition();
                            log.D("favorite cursor get position = "+position);
                        }
                        
                        if(cursor.getPosition() == position){
                            ((ImageView)view).setImageResource(R.drawable.faovrite_play_now);
                        }else{
                            ((ImageView)view).setImageResource(R.drawable.icon_favorite);
                        }
                        
                    }else{
                        view.setVisibility(View.INVISIBLE);
                    }
                    
                    return true;
                }
                
                return false;
            }
        });
        
        mFavoriteListView.setAdapter(mfavoAdapter);
        mFavoriteListView.setOnItemSelectedListener(new OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                if(mfavoBtn.hasFocus()){
                    AdapterViewSelectionUtil.setFocus(mFavoriteListView);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                
            }
            
        });
        mfavoBtn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                
                log.D("mfavoBtn onClick ...");
                
                if(mfavoBtn.getText().equals(getResources().getString(R.string.delete_love_channel))){
                    log.D("delete from favorites list ...");
                    // 删除当前频道从喜爱列表中
                    String[] whereArgs = new String[1];
                    int num = 0;
                    if(channelCursor.getCount() > 0 && mCurrentChannelIndex < channelCursor.getCount()){
                        channelCursor.moveToPosition(mCurrentChannelIndex);
                        num = channelCursor.getInt(channelCursor
                                .getColumnIndex(Channel.TableChannelsColumns.LOGICCHNUMBER));
                    }
                    
                    log.D("channelCursor current num = "+num);
                    whereArgs[0] = ""+num;
                    
                    ContentValues values = new ContentValues();
                    values.put(Channel.TableChannelsColumns.FAVORITE, DefaultParameter.FavoriteFlag.FAVORITE_NO);
                    
                    String where = ""+Channel.TableChannelsColumns.SERVICETYPE+"="+mCurrentPlayType+" and "
                            +Channel.TableChannelsColumns.LOGICCHNUMBER + "=?";
                    
                    getContentResolver().update(Channel.URI.TABLE_CHANNELS, values, where, whereArgs);
                    
                    cur.requery();
                    
                    channelCursor.requery();
                    channelCursor.moveToPosition(mCurrentChannelIndex);
                    
                    mfavoBtn.setText(R.string.set_love_channel);
                    channelLove.setVisibility(View.INVISIBLE);
                    
                }else{
                    log.D("add to favorites list ...");
                    // 添加当前频道到喜爱列表中
                    String[] whereArgs = new String[1];
                    
                    int num = 0;
                    if(channelCursor.getCount() > 0 && mCurrentChannelIndex < channelCursor.getCount()){
                        channelCursor.moveToPosition(mCurrentChannelIndex);
                        num = channelCursor.getInt(channelCursor
                                .getColumnIndex(Channel.TableChannelsColumns.LOGICCHNUMBER));
                    }
                    log.D("channelCursor current num = "+num);
                    whereArgs[0] = ""+num;
                    
                    ContentValues values = new ContentValues();
                    values.put(Channel.TableChannelsColumns.FAVORITE, DefaultParameter.FavoriteFlag.FAVORITE_IS);
                    
                    String where = ""+Channel.TableChannelsColumns.SERVICETYPE+"="+mCurrentPlayType+" and "
                            +Channel.TableChannelsColumns.LOGICCHNUMBER + "=?";
                    
                    getContentResolver().update(Channel.URI.TABLE_CHANNELS, values, where, whereArgs);
                    
                    cur.requery();
                    
                    channelCursor.requery();
                    channelCursor.moveToPosition(mCurrentChannelIndex);//喜爱操作后 回到当前播放频道
                    mfavoBtn.setText(R.string.delete_love_channel);
                    channelLove.setVisibility(View.VISIBLE);
                }
                
            }
        });
        
        mfavoBtn.setOnKeyListener(new OnKeyListener() {
            
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                
                switch(keyCode){
                case KeyEvent.KEYCODE_BACK:
                    if(event.getAction() == KeyEvent.ACTION_UP){
                        cur.close();
                        log.D("showFavoritesWindow.dismiss()");
                        showFavoritesWindow.dismiss();
                        if(isFavBackMenu){
                            if(mCurrentPlayType==PLAY_TYPE_BC){
                                showMenuWindow(PopupMsgId.SHOW_BROADCASE_MENU,PopMenuList.PROGRAM_GUIDE);
                            }else if(mCurrentPlayType==PLAY_TYPE_TV){
                                showMenuWindow(PopupMsgId.SHOW_MENU, PopMenuList.FAVORITE_CHANNEL);
                            }
                            
                        }
                        isFavBackMenu = false;
                        return true;
                    }
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                case KeyEvent.KEYCODE_VOLUME_UP:
                case KeyEvent.KEYCODE_VOLUME_MUTE:
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    
                    log.D("default key return true");
                    
                    if(event.getAction() == KeyEvent.ACTION_DOWN){
                        showFavoritesWindow.dismiss();
                        final int key = keyCode;
                        new Thread(new Runnable() {
                            public void run() {
                                doInjectKeyEvent(key);
                            }
                        }).start();
                    }
                    
                    return true;
                }
                return false;
            }
        });
        mfavoBtn.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    AdapterViewSelectionUtil.setFocus(mFavoriteListView);
                }
            }
        });
        mFavoriteListView.setOnItemClickListener(new OnItemClickListener() {
            
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                
                // 精确换台
                SimpleCursorAdapter adapter = (SimpleCursorAdapter) mFavoriteListView.getAdapter();
                Cursor cursorMoved = (Cursor)adapter.getItem(position);
                int channelIndex = cursorMoved.getInt(cursorMoved
                        .getColumnIndex(Channel.TableChannelsColumns.LOGICCHNUMBER));
                channelIndex--;
                log.D("channelIndex = "+channelIndex);
                
                if(channelIndex >= 0 && channelIndex < channelCursor.getCount()){
                    
                    if((channelIndex+1) != mCurrentChannelNumber){
                        Message msg = Message.obtain();
                        msg.what = WorkHandlerMsg.SWITCH_TO_SPECIAL;
                        msg.arg1 = channelIndex;
                        workHandler.sendMessage(msg);
                    }
                    
                }else{
                    log.E("favorite index out channel cursor ! please check !");
                }
                cur.close();
                showFavoritesWindow.dismiss();
            }
        });
        
        mFavoriteListView.setOnKeyListener(new View.OnKeyListener() {
            
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                
                switch(keyCode){
                case KeyEvent.KEYCODE_BACK:
                    if(event.getAction() == KeyEvent.ACTION_UP){
                        cur.close();
                        log.D("showFavoritesWindow.dismiss()");
                        showFavoritesWindow.dismiss();
                        if(isFavBackMenu){
                            if(mCurrentPlayType==PLAY_TYPE_BC){
                                showMenuWindow(PopupMsgId.SHOW_BROADCASE_MENU,PopMenuList.PROGRAM_GUIDE);
                            }else if(mCurrentPlayType==PLAY_TYPE_TV){
                                showMenuWindow(PopupMsgId.SHOW_MENU, PopMenuList.FAVORITE_CHANNEL);
                            }
                            
                        }
                        isFavBackMenu = false;
                        return true;
                    }
                    return true;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    
                    if(event.getAction() == KeyEvent.ACTION_DOWN){
                        if(mfavoAdapter.getCount() > 0 
                                && mFavoriteListView.getSelectedItemPosition() 
                                == (mfavoAdapter.getCount() - 1)){
                            mFavoriteListView.setSelection(0);
                            return true;
                        }
                    }
                    
                    break;
                case KeyEvent.KEYCODE_PLUS:
                case KeyEvent.KEYCODE_EQUALS:
                case KeyEvent.KEYCODE_NUMPAD_ADD:
                    Log.d("dispatchKeyEvent", "KeyEvent.KEYCODE_PLUS");
                    if (event.getAction() == KeyEvent.ACTION_UP) {
                        /* Start the key-simulation in a thread 
                         * so we do not block the GUI. */ 
                        new Thread(new Runnable() {
                            public void run() {
                                /* Simulate a KeyStroke to the menu-button. */
                                doInjectKeyEvent(KeyEvent.KEYCODE_PAGE_UP);
                            }
                        }).start(); /* And start the Thread. */
                        
                        return true;
                    } else {
                        return true;
                    }
                case KeyEvent.KEYCODE_MINUS:
                    // 还应该有一个，没找到
                case KeyEvent.KEYCODE_NUMPAD_SUBTRACT:
                    Log.d("dispatchKeyEvent", "KeyEvent.KEYCODE_MINUS");
                    if (event.getAction() == KeyEvent.ACTION_UP) {
                        
                      /* Start the key-simulation in a thread 
                      * so we do not block the GUI. */ 
                     new Thread(new Runnable() {
                         public void run() {
                             /* Simulate a KeyStroke to the menu-button. */
                             doInjectKeyEvent(KeyEvent.KEYCODE_PAGE_DOWN);
                         }
                     }).start(); /* And start the Thread. */
                        
                        return true;
                    } else {
                        return true;
                    }
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                case KeyEvent.KEYCODE_VOLUME_UP:
                case KeyEvent.KEYCODE_VOLUME_MUTE:
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    
                    log.D("default key return true");
                    
                    if(event.getAction() == KeyEvent.ACTION_DOWN){
                        showFavoritesWindow.dismiss();
                        final int key = keyCode;
                        new Thread(new Runnable() {
                            public void run() {
                                doInjectKeyEvent(key);
                            }
                        }).start();
                    }
                    
                    return true;
                }
                
                return false;
            }
        });
        
        if(favo == 1){
            // 是，按钮显示删除喜爱
               mfavoBtn.setText(R.string.delete_love_channel);
               
           }else{
            // 否，按键显示添加喜爱
               mfavoBtn.setText(R.string.set_love_channel);
           }
        
        mfavoBtn.requestFocus();
        showFavoritesWindow.showAtLocation(PlayActivity.this.getWindow()
                .getDecorView(), Gravity.RIGHT, 0, 0);
    }

    // 菜单
    private PopupWindow showMenuWindow = null;
    private View mMenuView;
    private ArrayAdapter<String> menuAdapter = null;

    /**
     * 菜单选项 id
     */
    private static final class PopupMsgId{
        /** 声道设置 */
        public static final int SHOW_VOLUME = 0;
        /** 多语言切换 */
        public static final int SHOW_LANGUAGE = 1;
        /** 画面设置 */
        public static final int SHOW_SCREEN = 2;
        /** 菜单 */
        public static final int SHOW_MENU = 3;
        /** 广播菜单 */
        public static final int SHOW_BROADCASE_MENU = 4;
        
        /** 画面设置对应的填充画面 */
        public static final int VOLUME_FULL = 1;
        /** 画面设置对应的原始画面 */
        public static final int VOLUME_SMALL = 0;
    }

    private static final class PopMenuList{
        /** 频道列表 */
        public static final int CHANNEL_LIST = 0;
        /** 节目指南 */
        public static final int PROGRAM_GUIDE = 1;
        /** 预约列表 */
        public static final int ORDER_LIST = 2;
        /** 喜爱列表 */
        public static final int FAVORITE_CHANNEL = 3;
        /** 画面设置 */
        public static final int SCREEN_SETTING = 4;
        /** 声道设置 */
        public static final int VOLUME_TRANSFER = 5;
        /** 多语音切换 */
        public static final int LANGUAGE_TRANSFER = 6;
        /** 智能卡设置 */
        public static final int CARD_MANAGER = 7;
        /** 频道搜索 */
        public static final int CHANNEL_SEARCH = 8;
        /** 广播节目 */
        public static final int BROADCASE_PROGRAM = 9;
        
        //对广播菜单列表
        /** 广播列表 */
        public static final int BROADCAST_CHANNEL_LIST =0;
        /** 广播喜爱列表 */
        public static final int BROADCAST_FAVORITE_CHANNEL = 1;
        /** 广播画面设置 */
        public static final int BROADCAST_SCREEN_SETTING = 2;
        /** 广播声道设置 */
        public static final int BROADCAST_VOLUME_TRANSFER = 2;
        /** 广播多语音切换 */
        public static final int BROADCAST_LANGUAGE_TRANSFER = 3;
        /** 直播电视 */
        public static final int PLAY_TV_PROGRAM = 4;
        
        
    }

    private ListView mMenuListView;
    
    /**
     * 
     * @param popmsg 用于判断菜单的层次 
     * @param position  用于回到菜单列表时返回时，回到对应子项
     */
    private void showMenuWindow(final int popmsg,int position) {
        log.D("showMenuWindow()");
        if (showMenuWindow == null) {
            mMenuView = getLayoutInflater().inflate(
                    R.layout.menu_layout, null);
            showMenuWindow = new PopupWindow(mMenuView);
            showMenuWindow.setWidth(565);
            showMenuWindow.setHeight(1080);
            showMenuWindow.setFocusable(true);
            
        }
        
        mMenuListView = (ListView) mMenuView.findViewById(R.id.favorites_list);
        
        mMenuListView.setDividerHeight(0);
        
        String[] adjust_items = null;
        
        switch(popmsg){
            case PopupMsgId.SHOW_MENU:
                adjust_items = getResources().getStringArray(R.array.adjust_items);
                break;
            case PopupMsgId.SHOW_VOLUME:
                adjust_items = getResources().getStringArray(R.array.volume_items);
                break;
            case PopupMsgId.SHOW_SCREEN:
                adjust_items = getResources().getStringArray(R.array.screen_items);
                break;
            case PopupMsgId.SHOW_LANGUAGE:
                int sum = mJniSetting.getAudioLanguage();
                log.D("mJniSetting.getAudioLanguage =="+sum);
                String[] str  = getResources().getStringArray(R.array.language_items);
                adjust_items = new String[sum];
                for(int i=0;i<sum;i++){
                    adjust_items[i]= str[i];
                }
                break;
            case PopupMsgId.SHOW_BROADCASE_MENU:
                adjust_items = getResources().getStringArray(R.array.adjust_items_broadcast);
                break;
        }
        
//        menuAdapter = new ArrayAdapter<String>(this.getApplicationContext(), 
//                R.layout.menu_list_item,adjust_items);
        menuAdapter = new ArrayAdapter<String>(this.getApplicationContext(), 
                R.layout.menu_list_item, R.id.menu_list_item_text, adjust_items);
        
        mMenuListView.setAdapter(menuAdapter);
        if(popmsg == PopupMsgId.SHOW_MENU||popmsg == PopupMsgId.SHOW_BROADCASE_MENU||popmsg == PopupMsgId.SHOW_LANGUAGE)
            mMenuListView.setSelection(position);
        
        mMenuListView.setOnItemClickListener(new OnItemClickListener() {
            
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                
//                <item>频道列表</item>0        <item>广播列表</item>       <item>立体声</item>0        <item>原始比例</item>0        <item>伴音0</item>0
//                <item>节目详情</item>1        <item>喜爱广播</item>       <item>左声道</item>1        <item>填充画面</item>1        <item>伴音1</item>0
//                <item>节目指南</item>2        <item>画面设置</item>       <item>右声道</item>2                                     <item>伴音2</item>0
//                <item>预约列表</item>3        <item>声道切换</item>
//                <item>喜爱频道</item>4        <item>多语言切换</item>
//                <item>画面设置</item>5        <item>广播节目</item>
//                <item>声道切换</item>6
//                <item>多语言切换</item>7
//                <item>智能卡管理</item>8
//                <item>频道搜索</item>9
//                <item>广播节目</item>10
                switch(position){
                case PopMenuList.CHANNEL_LIST:
                    switch(popmsg){
                        case PopupMsgId.SHOW_MENU:
                        case PopupMsgId.SHOW_BROADCASE_MENU:
//                            if(mCurrentPlayType == PLAY_TYPE_BC){
//                                // 发消息有延迟，直接调用了：
//    //                               mainHandler.sendEmptyMessage(MainHandlerMsg.CHANGE_LAYOUT_TV);
////                                   changeLayout(PLAY_TYPE_TV);
//                                changePlayMode(PLAY_TYPE_TV);
//                                
//                                workHandler.sendEmptyMessage(WorkHandlerMsg.CHANNEL_CURSOR_REFRESH);
//                               }
                               
                               showChannelsWindow();
                               isChanBackMenu = true;
                               showMenuWindow.dismiss();
                               break;
                        case PopupMsgId.SHOW_VOLUME://设置立体声
                            Message m = new Message();
                            m.what = WorkHandlerMsg.JNI_SETTING_VOLUME;
                            m.arg1 = tagAudioStereoMode.AUDIO_MODE_STEREO.ordinal();
                            workHandler.sendMessage(m);
                            break;
                        case PopupMsgId.SHOW_SCREEN://设置原始比例
                            VolumeModeUtil.savePlayMode(PlayActivity.this, PopupMsgId.VOLUME_SMALL);
                            Message tm = new Message();
                            tm.what = WorkHandlerMsg.JNI_SETTING_SCREEN;
//                            tm.arg1 = PopMenuList.CHANNEL_LIST;
                            tm.arg1 = PopupMsgId.VOLUME_SMALL;//原始比列值为0
                            workHandler.sendMessage(tm);
                            break;
                        case PopupMsgId.SHOW_LANGUAGE://设置伴音
                            Message th = new Message();
                            th.what = WorkHandlerMsg.JNI_SETTING_LANGUAGE;
                            th.arg1 = PopMenuList.CHANNEL_LIST;
                            workHandler.sendMessage(th);
                            setAudioIndex(PopMenuList.CHANNEL_LIST);
                            break;
                    }
                    break;
                case PopMenuList.PROGRAM_GUIDE:
                    switch(popmsg){
                        case PopupMsgId.SHOW_VOLUME://设置左声道
                            Message m = new Message();
                            m.what = WorkHandlerMsg.JNI_SETTING_VOLUME;
                            m.arg1 = tagAudioStereoMode.AUDIO_MODE_LEFT.ordinal();
                            workHandler.sendMessage(m);
                            break;
                        case PopupMsgId.SHOW_SCREEN://设置填充画面
                            VolumeModeUtil.savePlayMode(PlayActivity.this, PopupMsgId.VOLUME_FULL);
                            Message tm = new Message();
                            tm.what = WorkHandlerMsg.JNI_SETTING_SCREEN;
                            tm.arg1 = PopupMsgId.VOLUME_FULL;
                            workHandler.sendMessage(tm);
                            break;
                        case PopupMsgId.SHOW_BROADCASE_MENU://广播喜爱列表
                            showFavoritesWindow();
                            isFavBackMenu = true;
                            showMenuWindow.dismiss();
                            break;
                        case PopupMsgId.SHOW_MENU:
                            if(mCurrentPlayType == PLAY_TYPE_TV){
                                showEpgWindow();
                                showMenuWindow.dismiss();
                            }
                            break;
                        case PopupMsgId.SHOW_LANGUAGE://设置伴音1
                            Message th = new Message();
                            th.what = WorkHandlerMsg.JNI_SETTING_LANGUAGE;
                            th.arg1 = PopMenuList.PROGRAM_GUIDE;
                            workHandler.sendMessage(th);
                            setAudioIndex(PopMenuList.PROGRAM_GUIDE);
                            break;
                    }
                    break;
                case PopMenuList.ORDER_LIST:
                    switch(popmsg){
                        case PopupMsgId.SHOW_VOLUME://设置右声道
                            Message m = new Message();
                            m.what = WorkHandlerMsg.JNI_SETTING_VOLUME;
                            m.arg1 = tagAudioStereoMode.AUDIO_MODE_RIGHT.ordinal();
                            workHandler.sendMessage(m);
                            break;
                        case PopupMsgId.SHOW_BROADCASE_MENU://广播进声道设置
                            showMenuWindow(PopupMsgId.SHOW_VOLUME,PopMenuList.CHANNEL_LIST);
                            break;
                        case PopupMsgId.SHOW_MENU:
                            if(mCurrentPlayType == PLAY_TYPE_TV){
                                showProgramReservesWindow();
                                showMenuWindow.dismiss();
                            }
                            break;
                        case PopupMsgId.SHOW_LANGUAGE://设置伴音2
                            Message th = new Message();
                            th.what = WorkHandlerMsg.JNI_SETTING_LANGUAGE;
                            th.arg1 = PopMenuList.ORDER_LIST;
                            workHandler.sendMessage(th);
                            setAudioIndex(PopMenuList.ORDER_LIST);
                            break;
                    }
                    break;
                case PopMenuList.FAVORITE_CHANNEL:
                    switch(popmsg){
                        case PopupMsgId.SHOW_MENU:
//                            showProgramReservesAlert();
                            showFavoritesWindow();
                            isFavBackMenu = true;
                            showMenuWindow.dismiss();
                            break;
                        case PopupMsgId.SHOW_BROADCASE_MENU://广播进多语言
                            showMenuWindow(PopupMsgId.SHOW_LANGUAGE,PopMenuList.CHANNEL_LIST);
                            break;
                    }
                    break;
                case PopMenuList.SCREEN_SETTING:
                    switch(popmsg){
                        case PopupMsgId.SHOW_MENU:
                            showMenuWindow(PopupMsgId.SHOW_SCREEN,PopMenuList.CHANNEL_LIST);
                            break;
                        case PopupMsgId.SHOW_BROADCASE_MENU://广播进直播节目
                            if(mCurrentPlayType != PLAY_TYPE_TV){
                                changePlayMode(PLAY_TYPE_TV);
                                workHandler.sendEmptyMessage(WorkHandlerMsg.CHANNEL_CURSOR_REFRESH);
                            }
                            break;
                    }
                    break;
                case PopMenuList.VOLUME_TRANSFER:
                    switch(popmsg){
                        case PopupMsgId.SHOW_MENU:
                            showMenuWindow(PopupMsgId.SHOW_VOLUME,PopMenuList.CHANNEL_LIST);
                            break;
//                        case PopupMsgId.SHOW_BROADCASE_MENU://广播进直播节目
//                            if(mCurrentPlayType != PLAY_TYPE_TV){
//                                changePlayMode(PLAY_TYPE_TV);
//                                workHandler.sendEmptyMessage(WorkHandlerMsg.CHANNEL_CURSOR_REFRESH);
//                            }
//                            break;
                    }
                    break;
                case PopMenuList.LANGUAGE_TRANSFER:
                    switch(popmsg){
                        case PopupMsgId.SHOW_MENU:
                            int audioindex = 0;
                            int audioindex_index = channelCursor.getColumnIndex(Channel.TableChannelsColumns.AUDIOINDEX);
                            if(audioindex_index >=0){
                                audioindex = channelCursor.getInt(audioindex_index);
                                log.D("playCurrentCursor audioindex = "+audioindex);
                            }
                            if(audioindex<=2){
                                showMenuWindow(PopupMsgId.SHOW_LANGUAGE,audioindex);
                            }else{
                                showMenuWindow(PopupMsgId.SHOW_LANGUAGE,PopMenuList.CHANNEL_LIST);
                            }
                            
                            break;
                    }
                    break;
                case PopMenuList.CARD_MANAGER:
                    switch(popmsg){
                        case PopupMsgId.SHOW_MENU:
                            showMenuWindow.dismiss();
                            Intent tintent = new Intent(PlayActivity.this,CaSettingActivity.class);
                            tintent.putExtra("first", 1);
                            startActivity(tintent);
                            break;
                    }
                    break;
                case PopMenuList.CHANNEL_SEARCH:
                    switch(popmsg){
                        case PopupMsgId.SHOW_MENU:
                            showMenuWindow.dismiss();
                            Intent intent = new Intent();
                            intent.setClass(PlayActivity.this, SearchMenuActivity.class);
                            startActivity(intent);
                            break;
                    }
                    break;
                case PopMenuList.BROADCASE_PROGRAM:
                    switch(popmsg){
                        case PopupMsgId.SHOW_MENU:
                            // 发消息有延迟，直接调用了：
//                          mainHandler.sendEmptyMessage(MainHandlerMsg.CHANGE_LAYOUT_BC);
//                              changeLayout(PLAY_TYPE_BC);
                            if(mCurrentPlayType != PLAY_TYPE_BC){
                                changePlayMode(PLAY_TYPE_BC);
                                workHandler.sendEmptyMessage(WorkHandlerMsg.CHANNEL_CURSOR_REFRESH);
                            }
                            
//                              showChannelsWindow();
                              showMenuWindow.dismiss();
                            break;
                    }
                    break;
                
                }
                // 执行完，该消失了
//                showMenuWindow.dismiss();
            }
        });
        
        mMenuListView.setOnKeyListener(new View.OnKeyListener() {
            
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_UP){
                    if(keyCode == KeyEvent.KEYCODE_ESCAPE||keyCode == KeyEvent.KEYCODE_BACK){
                        switch(popmsg){
                            case PopupMsgId.SHOW_VOLUME:
                                if(mCurrentPlayType==PLAY_TYPE_BC){
                                    showMenuWindow(PopupMsgId.SHOW_BROADCASE_MENU,PopMenuList.BROADCAST_VOLUME_TRANSFER);
                                }else{
                                    showMenuWindow(PopupMsgId.SHOW_MENU,PopMenuList.VOLUME_TRANSFER);
                                }
                                if(viewGroup != null)  viewGroup.delayHideView();
                                return true;
                            case PopupMsgId.SHOW_LANGUAGE:
                                if(mCurrentPlayType==PLAY_TYPE_BC){
                                    showMenuWindow(PopupMsgId.SHOW_BROADCASE_MENU, PopMenuList.BROADCAST_LANGUAGE_TRANSFER);
                                }else{
                                    showMenuWindow(PopupMsgId.SHOW_MENU,PopMenuList.LANGUAGE_TRANSFER);
                                }
                                if(viewGroup != null)  viewGroup.delayHideView();
                                return true;
                            case PopupMsgId.SHOW_SCREEN:
                                if(mCurrentPlayType==PLAY_TYPE_BC){
                                    showMenuWindow(PopupMsgId.SHOW_BROADCASE_MENU, PopMenuList.BROADCAST_SCREEN_SETTING);
                                }else{
                                    showMenuWindow(PopupMsgId.SHOW_MENU,PopMenuList.SCREEN_SETTING);
                                }
                                if(viewGroup != null)  viewGroup.delayHideView();
                                return true;
                            case PopupMsgId.SHOW_MENU:
                                log.D("showMenuWindow.dismiss()");
                                showMenuWindow.dismiss();
                                return true;
                            case PopupMsgId.SHOW_BROADCASE_MENU:
                                showMenuWindow.dismiss();
                                return true;
                            default :
                                break;
                        }
                    }
                }
                
                switch(keyCode){
                case KeyEvent.KEYCODE_DPAD_UP:
                    
                    if(event.getAction() == KeyEvent.ACTION_DOWN){
                        if(menuAdapter.getCount() > 0 
                                && mMenuListView.getSelectedItemPosition() == 0){
                            mMenuListView.setSelection(menuAdapter.getCount() - 1);
                            return true;
                        }
                    }
                    
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    
                    if(event.getAction() == KeyEvent.ACTION_DOWN){
                        if(menuAdapter.getCount() > 0 
                                && mMenuListView.getSelectedItemPosition() 
                                == (menuAdapter.getCount() - 1)){
                            mMenuListView.setSelection(0);
                            return true;
                        }
                    }
                    
                    break;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                case KeyEvent.KEYCODE_VOLUME_UP:
                case KeyEvent.KEYCODE_VOLUME_MUTE:
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    
                    log.D("default key return true");
                    
                    if(event.getAction() == KeyEvent.ACTION_DOWN){
                        showMenuWindow.dismiss();
                        final int key = keyCode;
                        new Thread(new Runnable() {
                            public void run() {
                                doInjectKeyEvent(key);
                            }
                        }).start();
                    }
                    
                    return true;
                }
                
                return false;
            }
        });
        
        mMenuListView.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                if(viewGroup != null){
                    viewGroup.delayHideView();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                
            }
        });
        
        showMenuWindow.showAtLocation(PlayActivity.this.getWindow()
                .getDecorView(), Gravity.RIGHT, 0, 0);
    }

    /**
     * 切换上一个频道
     * 在工作线程中执行
     * @return 是否移动成功
     */
    private void switchToPreviousInWorkThread(){
        if(channelCursor == null){
            log.E("switchToPrevious() playCursor == null");
            return;
        }
        
        if(!channelCursor.moveToPrevious()){
            channelCursor.moveToLast();
        }
        
        mPreChannelIndex = mCurrentChannelIndex;
        
        mCurrentChannelIndex = channelCursor.getPosition();
        
        log.D("ChannelXmlUtil.savePlayChannel mCurrentChannelIndex = "+mCurrentChannelIndex);
    }

    /**
     * 切换下一个频道
     * 在工作线程中执行
     * @return 是否移动成功
     */
    private void switchToNextInWorkThread(){
        if(channelCursor == null){
            log.E("switchToNext() playCursor == null");
            return;
        }
        
        if(!channelCursor.moveToNext()){
            channelCursor.moveToFirst();
        }
        
        mPreChannelIndex = mCurrentChannelIndex;
        
        mCurrentChannelIndex = channelCursor.getPosition();
        
        log.D("ChannelXmlUtil.savePlayChannel mCurrentChannelIndex = "+mCurrentChannelIndex);
    }

    /**
     * 切换到指定频道 
     * 在工作线程中执行
     * @param position
     * @return 是否移动成功
     */
    private boolean switchToSpecialInWorkThread(int position){
        if(channelCursor == null){
            log.E("switchToSpecial() playCursor == null");
            return false;
        }
        
        mPreChannelIndex = mCurrentChannelIndex;
        
        mCurrentChannelIndex = position;
        
        log.D("ChannelXmlUtil.savePlayChannel mCurrentChannelIndex = "+mCurrentChannelIndex);
        
        return channelCursor.moveToPosition(position);
    }

    private long mTimeChangeChannel = 0;

    /**
     * 播放当前cursor里面的频道
     * 在工作线程中执行
     * @param cur
     */
    private void playCurrentCursorInWorkThread(Cursor cur){
        log.D("playCurrentCursor()");
//        mCaDialogManager.resetMatchStatus();
        if(cur == null){
            log.E("playCurrentCursor() cur == null , error !");
            return;
        }
        
        if(cur.getCount() == 0){
            log.W("playCurrentCursor() cur.count == 0 , no channel !");
            return;
        }
        
        mTimeChangeChannel = System.currentTimeMillis();
        
        // 从cursor中取出数据,主要是pmtid,videoPid,audioPid
        int pmtid = 0;
        int pmtid_index = cur.getColumnIndex(Channel.TableChannelsColumns.PMTPID);
        if(pmtid_index >= 0){
            pmtid = cur.getInt(pmtid_index);
            log.D("playCurrentCursor pmtid ="+pmtid);
        }
        
        int videoPid = 0;
        int videoPid_index = cur.getColumnIndex(Channel.TableChannelsColumns.VIDEOSTREAMPID);
        if(videoPid_index >= 0){
            videoPid = cur.getInt(videoPid_index);
            log.D("playCurrentCursor videoPid ="+videoPid);
        }
        
        int videoTyp = 0;
        int videoTyp_index = cur.getColumnIndex(Channel.TableChannelsColumns.VIDEOPESTYPE);
        if(videoTyp_index >= 0){
            videoTyp = cur.getInt(videoTyp_index);
            log.D("playCurrentCursor videoTyp ="+videoTyp);
        }
        
        int videoEcmPid = 0;
        int videoEcmPid_index = cur.getColumnIndex(Channel.TableChannelsColumns.VIDEOECMPID);
        if(videoEcmPid_index >= 0){
            videoEcmPid = cur.getInt(videoEcmPid_index);
            log.D("playCurrentCursor videoEcmPid ="+videoEcmPid);
        }
        
        int audioPid = 0;
        int audioPid_index = cur.getColumnIndex(Channel.TableChannelsColumns.AUDIOSTREAMPID+"0");
        if(audioPid_index >= 0){
            audioPid = cur.getInt(audioPid_index);
            log.D("playCurrentCursor audioPid ="+audioPid);
        }
        
        int audioTyp = 0;
        int audioTyp_index = cur.getColumnIndex(Channel.TableChannelsColumns.AUDIOPESTYPE+"0");
        if(audioTyp_index >= 0){
            audioTyp = cur.getInt(audioTyp_index);
            log.D("playCurrentCursor audioTyp ="+audioTyp);
        }
        
        int audioEcmPid = 0;
        int audioEcmPid_index = cur.getColumnIndex(Channel.TableChannelsColumns.AUDIOECMPID+"0");
        if(audioEcmPid_index >= 0){
            audioEcmPid = cur.getInt(audioEcmPid_index);
            log.D("playCurrentCursor audioEcmPid ="+audioEcmPid);
        }
        
        int audioPid1 = 0;
        int audioPid_index1 = cur.getColumnIndex(Channel.TableChannelsColumns.AUDIOSTREAMPID+"1");
        if(audioPid_index1 >= 0){
            audioPid1 = cur.getInt(audioPid_index1);
            log.D("playCurrentCursor audioPid1 ="+audioPid1);
        }
        
        int audioTyp1 = 0;
        int audioTyp_index1 = cur.getColumnIndex(Channel.TableChannelsColumns.AUDIOPESTYPE+"1");
        if(audioTyp_index1 >= 0){
            audioTyp1 = cur.getInt(audioTyp_index1);
            log.D("playCurrentCursor audioTyp1 ="+audioTyp1);
        }
        
        int audioEcmPid1 = 0;
        int audioEcmPid_index1 = cur.getColumnIndex(Channel.TableChannelsColumns.AUDIOECMPID+"1");
        if(audioEcmPid_index1 >= 0){
            audioEcmPid1 = cur.getInt(audioEcmPid_index1);
            log.D("playCurrentCursor audioEcmPid1 ="+audioEcmPid1);
        }
        
        int audioPid2 = 0;
        int audioPid_index2 = cur.getColumnIndex(Channel.TableChannelsColumns.AUDIOSTREAMPID+"2");
        if(audioPid_index2 >= 0){
            audioPid2 = cur.getInt(audioPid_index2);
            log.D("playCurrentCursor audioPid2 ="+audioPid2);
        }
        
        int audioTyp2 = 0;
        int audioTyp_index2 = cur.getColumnIndex(Channel.TableChannelsColumns.AUDIOPESTYPE+"2");
        if(audioTyp_index2 >= 0){
            audioTyp2 = cur.getInt(audioTyp_index2);
            log.D("playCurrentCursor audioTyp2 ="+audioTyp2);
        }
        
        int audioEcmPid2 = 0;
        int audioEcmPid_index2 = cur.getColumnIndex(Channel.TableChannelsColumns.AUDIOECMPID+"2");
        if(audioEcmPid_index2 >= 0){
            audioEcmPid2 = cur.getInt(audioEcmPid_index2);
            log.D("playCurrentCursor audioEcmPid ="+audioEcmPid2);
        }
        
        int serviceId = 0;
        int serviceId_index = cur.getColumnIndex(Channel.TableChannelsColumns.SERVICEID);
        if(serviceId_index >= 0){
            serviceId = cur.getInt(serviceId_index);
            log.D("playCurrentCursor serviceId ="+serviceId);
        }
        
        int emmPid = 0;
        int emmPid_index = cur.getColumnIndex(Channel.TableChannelsColumns.EMMPID);
        if(emmPid_index >= 0){
            emmPid = cur.getInt(emmPid_index);
            log.D("playCurrentCursor emmPid ="+emmPid);
        }
        
        int transId = 0;
        int transId_index = cur.getColumnIndex(Channel.TableChannelsColumns.TRANSPONDER_ID);
        if(transId_index >= 0){
            transId = cur.getInt(transId_index);
            log.D("playCurrentCursor transId ="+transId);
        }
        
        int audioindex = 0;
        int audioindex_index = cur.getColumnIndex(Channel.TableChannelsColumns.AUDIOINDEX);
        if(audioindex_index >=0){
            audioindex = cur.getInt(audioindex_index);
            log.D("playCurrentCursor audioindex = "+audioindex);
        }
        
        int frequency = 0;
        int modulation = 0;
        int symbol = 0;
        String[] selectionArgs = new String[1];
        selectionArgs[0] = ""+transId;
        Cursor tsCursor = getContentResolver().query(Channel.URI.TABLE_TRANSPONDERS,
                null, Channel.TableTranspondersColumns.ID+" = ? ", selectionArgs, null);
        log.D("playCurrentCursor tsCursor ="+tsCursor);
        if(tsCursor != null){
            log.D("playCurrentCursor tsCursor getCount ="+tsCursor.getCount());
            if(tsCursor.getCount() > 0){
                
                tsCursor.moveToFirst();
                
                int frequency_index = tsCursor.getColumnIndex(Channel.TableTranspondersColumns.FREQUENCY);
                if(frequency_index >= 0){
                    frequency = tsCursor.getInt(frequency_index);
                    log.D("playCurrentCursor frequency ="+frequency);
                }
                
                int modulation_index = tsCursor.getColumnIndex(Channel.TableTranspondersColumns.MODULATION);
                if(modulation_index >= 0){
                    modulation = tsCursor.getInt(modulation_index);
                    log.D("playCurrentCursor modulation ="+modulation);
                }
                
                int symbol_index = tsCursor.getColumnIndex(Channel.TableTranspondersColumns.SYMBOLRATE);
                if(symbol_index >= 0){
                    symbol = tsCursor.getInt(symbol_index);
                    log.D("playCurrentCursor symbol ="+symbol);
                }
                
            }
            
            tsCursor.close();
        }else{
            log.E("tsCursor == null ! error !");
        }
        log.D("change channel query data time = "+(System.currentTimeMillis() - mTimeChangeChannel));
        
        log.D("playCurrentCursorInWorkThread mJniPlay.stop()");
        mJniPlay.stop();
        log.D("playCurrentCursorInWorkThread mJniPlay.play()");
        mJniPlay.play();
        
        log.D("change channel stop and start time = "+(System.currentTimeMillis() - mTimeChangeChannel));
        
        log.D("playCurrentCursorInWorkThread mJniPlay.setService()  ********************************************");
        tagDVBService service = new tagDVBService();
        service.setFrequency(frequency);
        service.setSymbolRate(symbol);
        service.setModulation(modulation);
        service.setPmt_id(pmtid);
        service.setVideo_stream_pid(videoPid);
        service.setVideo_stream_type(videoTyp);
        service.setAudio_stream_pid(audioPid);
        service.setAudio_stream_type(audioTyp);
        service.setVideo_ecm_pid(videoEcmPid);
        service.setAudio_ecm_pid(audioEcmPid);
        service.setSid(serviceId);
        service.setEmm_pid(emmPid);
        
        service.setAudio_index(audioindex);
        service.setAudio_ecm_pid1(audioEcmPid1);
        service.setAudio_stream_type1(audioTyp1);
        service.setAudio_stream_pid1(audioPid1);
        
        service.setAudio_ecm_pid2(audioEcmPid2);
        service.setAudio_stream_type2(audioTyp2);
        service.setAudio_stream_pid2(audioPid2);
        
        mJniPlay.setService(service);
        
        log.D("change channel setService time = "+(System.currentTimeMillis() - mTimeChangeChannel));
        
        // 保存当前播放的频道类型和频道序号
        ChannelTypeNumUtil.savePlayChannel(getApplicationContext(),mCurrentPlayType,mCurrentChannelIndex);
        
        // 需重新设置下播放大小
        if(!isFirstIn){
            int mode = VolumeModeUtil.getPlayMode(this);
            String display = VideoLayerUtils.getVideoScreen();
            log.D("videolayerutils.getvideoscreen ="+display);
            if(display!=null){
                if(display.equals("720p")||display.equals("720i")){
                    mJniSetting.setDisplayMode(mode);
                    mJniSetting.setDisplayRect(0, 0, 1280, 720);
                    log.D("videolayerutils.getvideoscreen  = 1280-720 "+display);
                }else if (display.equals("576i")||display.equals("576p")){
                    mJniSetting.setDisplayMode(mode);
                    mJniSetting.setDisplayRect(0, 0, 1024, 576);
                    log.D("videolayerutils.getvideoscreen = 1024-576 "+display);
                }else{//1080p 1080i
                    mJniSetting.setDisplayMode(mode);
                    mJniSetting.setDisplayRect(0, 0, 1920, 1080);
                    log.D("videolayerutils.getvideoscreen  = 1920-1080 "+display);
                }
            }else{
                log.D("videolayerutils.getvideoscreen  =  null"+display);
            }
            isFirstIn = true;
            // 第一次进入，如果是静音状态就取消
            AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
            am.setStreamMute(AudioManager.STREAM_MUSIC, false);
        }
        
        // 设置当前频道的音量
        int audio = 0;
//        log.D("mVolumesMap = "+mVolumesMap);
//        log.D("mVolumesMap = "+mVolumesMap.toString());
//        log.D("mCurrentChannelNumber = "+mCurrentChannelNumber);
        
        if(mVolumesMap.containsKey(mCurrentChannelNumber)){
            audio = mVolumesMap.get(mCurrentChannelNumber);
        }else{
            log.E("mVolumesMap not have the key of mCurrentChannelNumber !!!");
        }
        
        log.D("playCurrentCursorInWorkThread audio = "+audio);
        
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        
        am.setStreamVolume(AudioManager.STREAM_MUSIC, audio, AudioManager.FLAG_VIBRATE);
        
        DvbApplication app = (DvbApplication)getApplication();
        if(app != null){
            app.setmCurrentChannelTag(service);
        }
        
        long time = System.currentTimeMillis();
        long count = time - mTimeChangeChannel;
        log.D("change channel totle time = "+count);
        
//        Toast.makeText(getApplicationContext(), ""+count, Toast.LENGTH_SHORT).show();
    }

    /**
     * 初始化频道cursor
     * 在工作线程中执行
     */
    private void changeChannelCursorInWorkThread(int playType){
        log.D("changeChannelCursor playType = "+playType);
        String[] selectionArgs = new String[1];
        
        switch(playType){
        case PLAY_TYPE_TV:
            
            selectionArgs[0] = ""+DefaultParameter.ServiceType.digital_television_service;
            
            break;
        case PLAY_TYPE_BC:
            
            selectionArgs[0] = ""+DefaultParameter.ServiceType.digital_radio_sound_service;
            
            break;
        }
        
        channelCursor = getContentResolver().query(Channel.URI.TABLE_CHANNELS,
                null, Channel.TableChannelsColumns.SERVICETYPE+" = ? ", 
                selectionArgs, Channel.TableChannelsColumns.LOGICCHNUMBER);
        
        if(channelCursor == null){
            log.D("@dingran@ channelCursor == null");
            return;
        }
        log.D("changeChannelCursorInWorkThread channelCursor count== "+channelCursor.getCount());
        // 交给activity管理
        this.startManagingCursor(channelCursor);
        
        if(channelCursor.moveToFirst()){
            for(int i=0;i<channelCursor.getCount();i++){
                int channelNum = channelCursor
                        .getInt(channelCursor.getColumnIndex(Channel.TableChannelsColumns.LOGICCHNUMBER));
                int channelVol = channelCursor
                        .getInt(channelCursor.getColumnIndex(Channel.TableChannelsColumns.VOLUME));
//                mFavoritesMap.put(channelNum, (i+1)+"");
//                log.D("channelNum = "+channelNum + " and volume = "+channelVol);
                mVolumesMap.put(channelNum, channelVol);
                
                channelCursor.moveToNext();
            }
        }
        
        log.D("mVolumesMap = "+mVolumesMap.toString());
    }

    /**
     * 转换数字键到频道号显示工具类
     * @param keyCode
     */
    private void numKeyDown(int keyCode) {
        // 开启确定键快速切台模式
        isQuckSwitchChannel = true;
        
        if (!channelNumberLinear.isShown()) {
            chnumberUtil.clear();
        }
        showChannelNum(chnumberUtil.numKeyDown(keyCode));
    }

    /**
     * show channel number
     * 
     * @param currentChannelNum
     */
    private void showChannelNum(int currentChannelNum) {
        
        for (int i = channelNumberImage.length - 1; i >= 0; i--) {
            channelNumberImage[i]
                    .setImageResource(SERVICE_ID_RES[currentChannelNum % 10]);
            currentChannelNum = currentChannelNum / 10;
        }
        
        viewGroup.showView(ViewGroupKey.VIEW_GROUP_KEY_CHNUMBER);
    }

    /**
     * 显示频道条
     * @param cur
     */
    public void showChannelInfo(Cursor cur) {
        if (cur == null){
            log.W("displayChannelInfo() cur is null !");
            return;
        }
        
        if(cur.getCount() == 0){
            log.W("displayChannelInfo() cur.count == 0 , no channel !");
            return;
        }
        
        int logicNumber = 0;
        int columnt_channelNum = cur.getColumnIndex(Channel.TableChannelsColumns.LOGICCHNUMBER);
        if(columnt_channelNum >= 0){
            logicNumber = cur.getInt(columnt_channelNum);
            log.D("playCurrentCursor logicNumber ="+logicNumber);
            mCurrentChannelNumber = logicNumber;
            channelNumText.setText("" + String.format(DefaultParameter.CHANNEL_NUMBER_FORMAT,logicNumber));
            
        }
        
        String name = "";
        int columnt_channelName = cur.getColumnIndex(Channel.TableChannelsColumns.SERVICENAME);
        if(columnt_channelName >= 0){
            name = cur.getString(columnt_channelName);
            log.D("playCurrentCursor name ="+name);
            if (name == null) {
                channelNameText.setText("");
            } else {
                
                channelNameText.setText("" + name);
            }
        }
        // 显示喜爱图标：
        int isFavorite = 0;// 0 非喜爱 1 喜爱
        int columnt_favorite = cur.getColumnIndex(Channel.TableChannelsColumns.FAVORITE);
        if(columnt_favorite >= 0){
            isFavorite = cur.getInt(columnt_favorite);
            if(isFavorite==1){//喜爱
                channelLove.setVisibility(View.VISIBLE);
            }else if(isFavorite == 0){//非喜爱
                log.D("showChannelInfo not favorite channel");
                channelLove.setVisibility(View.INVISIBLE);
            }
        }
        
        // 显示CA图标：
        int Vecm_pid = 0;
        int columnt_Vecm_pid = cur.getColumnIndex(Channel.TableChannelsColumns.VIDEOECMPID);
        
        int Aecm_pid = 0;
        int columnt_Aecm_pid = cur.getColumnIndex(Channel.TableChannelsColumns.AUDIOECMPID+"0");
        
        if(columnt_Vecm_pid >= 0 && columnt_Aecm_pid >= 0){
            Vecm_pid = cur.getInt(columnt_Vecm_pid);
            Aecm_pid = cur.getInt(columnt_Aecm_pid);
            
            if(Vecm_pid == 0 && Aecm_pid == 0){
                channelMoney.setVisibility(View.INVISIBLE);
            }else{
                channelMoney.setVisibility(View.VISIBLE);
            }
            
        }
        
        int column_pestype = cur.getColumnIndex(Channel.TableChannelsColumns.AUDIOPESTYPE+"0");
        if(column_pestype>=0){
            int type = cur.getInt(column_pestype);
            if(type==6||type==122||type==129){// Dobly 0x06 0x81 0x7a;
                channlePesType.setVisibility(View.VISIBLE);
            }else{
                channlePesType.setVisibility(View.INVISIBLE);
            }
        }
        //显示声道和伴音
        workHandler.sendEmptyMessage(WorkHandlerMsg.JNI_GETTING_VOLUME_LANGUAGE);
        
        currentProgram.setText("");
        nextProgram.setText("");
        currentProgramTime.setText("");
        nextProgramTime.setText("");
        
        // 显示PF
        if(mJniPlay != null){
            mJniPlay.setOnPfListener(new OnPfListener() {
                
                @Override
                public void showPf(tagMiniEPGNotify pf) {
                    log.D("mJniPlay.setOnPfListener showPf");
                    pfEpg = pf;
                    if(pf != null){
                        Message msg = Message.obtain();
                        msg.what = MainHandlerMsg.PF_REFRESH;
                        msg.obj = pf;
                        mainHandler.sendMessage(msg);
                    }
                }
            });
        }
        
    }

    /**
     * 刷新显示PF
     */
    private void refreshPf(tagMiniEPGNotify pfEpg){
        log.D("refreshPf()");
        
        if(pfEpg != null){
            currentProgram.setText(""+pfEpg.getCurrentEventName());
            nextProgram.setText(""+pfEpg.getNextEventName());
            
            log.D(""+pfEpg.getCurrentEventStartTime());
            log.D(""+pfEpg.getCurrentEventEndTime());
            log.D(""+pfEpg.getNextEventStartTime());
            log.D(""+pfEpg.getNextEventEndTime());
            
            String startTime = (String) DateFormat.format("kk:mm", pfEpg.getCurrentEventStartTime()*1000+8*3600000);
            String endTime = (String) DateFormat.format("kk:mm", pfEpg.getCurrentEventEndTime()*1000+8*3600000);
            
            currentProgramTime.setText(""+startTime+"~"+endTime);
            
            startTime = "";
            endTime = "";
            startTime = (String) DateFormat.format("kk:mm", pfEpg.getNextEventStartTime()*1000+8*3600000);
            endTime = (String) DateFormat.format("kk:mm", pfEpg.getNextEventEndTime()*1000+8*3600000);
            
            nextProgramTime.setText(""+startTime+"~"+endTime);
            log.D(System.currentTimeMillis()+"   end time----------------------------------------------------------------");
        }
        
    }

    /**
     * 当前显示的MiniEPG信息
     */
    private tagMiniEPGNotify pfEpg;

    /**
     * 调节音量
     * 在工作线程中执行
     * @param isAdd 是否增加
     *        true  增加音量
     *        false 减小音量
     */
    private void volumeChangeInWorkThread(boolean isAdd){
        log.D("volumeChange isAdd = "+isAdd);
        
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        
        if(!am.isMusicActive()){
            log.D("volumeChangeInWorkThread music is not active");
            return;
        }
        
        am.setStreamMute(AudioManager.STREAM_MUSIC, false);
        
        if(isAdd){
            
            if(am.getStreamVolume(AudioManager.STREAM_MUSIC) < 40){
                am.adjustVolume(AudioManager.ADJUST_RAISE, 0);
            }
            
        }else{
            
            if(am.getStreamVolume(AudioManager.STREAM_MUSIC) > 0){
                am.adjustVolume(AudioManager.ADJUST_LOWER, 0);
            }
        }
        
        Message msg = new Message();
        msg.what = MainHandlerMsg.SHOW_VOLUME_BAR;
        msg.arg1 = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        mainHandler.sendMessage(msg);
        
    }

    /**
     * 静音
     * 在工作线程中执行
     */
    private void volumeMuteInWorkThread(){
        log.D("volumeMute()");
        
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        
        if(!am.isMusicActive()){
            log.D("volumeChangeInWorkThread music is not active");
            return;
        }
        
//        am.isStreamMute(AudioManager.STREAM_MUSIC);
        
        // 根据静音图标显示来判断当前是否是静音状态
        if(volumeMuteLinear.isShown()){
            // 取消静音时
            am.setStreamMute(AudioManager.STREAM_MUSIC,false);
            Message msg = Message.obtain();
            msg.what = MainHandlerMsg.SHOW_VOLUME_BAR;
            msg.arg1 = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            mainHandler.sendMessage(msg);
        }else{
            am.setStreamMute(AudioManager.STREAM_MUSIC,true);
            mainHandler.sendEmptyMessage(MainHandlerMsg.SHOW_MUTE_BAR);
        }
    }

    /**
     * 停止播放
     * 在工作线程中执行
     */
    private void playStopInWorkThread(){
        log.D("playStopInWorkThread()");
        
        if(mJniPlay != null){
            log.D("playStopInWorkThread() mJniPlay.stop() start");
            mJniPlay.stop();
            log.D("playStopInWorkThread() mJniPlay.stop() end");
        }
        
    }

    /**
     * 所有的对话框集合
     */
    private static final class DialogId{
        /** 没有频道时的搜索询问对话框 */
        private static final int NO_CHANNEL_SEARCH_ASK = 0;
        /** 退出提示对话框 */
        private static final int CONFIRM_QUIT_ADTV = 1;
        /** 节目预约提示对话框 */
        private static final int PROGRAM_RESERVE_ASK = 2;
        /** 节目预约取消提示对话框 */
        private static final int PROGRAM_RESERVE_LIST_CANCEL = 3;
    }

    protected Dialog onCreateDialog(int id) {
        
        switch(id){
            case DialogId.NO_CHANNEL_SEARCH_ASK:
                // show ask dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                builder.setTitle(getResources().getString(R.string.alert));
                builder.setMessage(getResources().getString(R.string.is_or_not_search_ask));
                builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        
                        Intent intent = new Intent();
                        intent.setClass(PlayActivity.this, SearchMenuActivity.class);
                        intent.putExtra("isNoChannel", true);
                        PlayActivity.this.startActivity(intent);
                        
                    }
                });
    
                builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        log.D("pass cannel button exit.");
                        if(mCurrentPlayType == PLAY_TYPE_TV){
                            mainHandler.sendEmptyMessage(MainHandlerMsg.EXIT_APP);
                        }if(mCurrentPlayType == PLAY_TYPE_BC){
                            mainHandler.sendEmptyMessage(MainHandlerMsg.CHANGE_LAYOUT_TV);
                        }
                    }
                });
                builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        
                        if(keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE){
                            log.D("pass KEYCODE_BACK or KEYCODE_ESCAPE exit.");
                            if(event.getAction() == KeyEvent.ACTION_UP){
                                mainHandler.sendEmptyMessage(MainHandlerMsg.EXIT_APP);
                            }
                            
                            return true;
                        }
                        
                        return false;
                    }
                });
                return builder.create();
            case DialogId.CONFIRM_QUIT_ADTV:
                AlertDialog.Builder buf = new AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                buf.setTitle(getResources().getString(R.string.alert));
                buf.setMessage(getResources().getString(R.string.confirm_quit_application));
                buf.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        jniPlayUninit();
                    }
                });
                buf.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
                return buf.create();
            case DialogId.PROGRAM_RESERVE_ASK:
                AlertDialog.Builder proBuilder = new AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                proBuilder.setTitle(getResources().getString(R.string.alert));
                proBuilder.setMessage(getResources().getString(R.string.program_reverse_alert_message));
                proBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        channelCursor.moveToPosition(mCurrentChannelIndex);
                        int serviceId = channelCursor.getInt(channelCursor.getColumnIndex("ServiceId"));
                        log.D("program reserve ask service id is  "+serviceId+" and mCurrentChannelIndex is " +mCurrentChannelIndex);
                        String channelName = channelCursor.getString(channelCursor.getColumnIndex("ServiceName"));
                        long startTime = mEpgEvents[mProgramListPosition].getStartTime();
                        long endTime = mEpgEvents[mProgramListPosition].getEndTime();
                        String programName = mEpgEvents[mProgramListPosition].getProgramName();
                        
                        ContentValues values = new ContentValues();
                        values.put(Channel.TableReservesColumns.PROGRAMNAME, programName);
                        values.put(Channel.TableReservesColumns.SERVICEID, serviceId);
                        values.put(Channel.TableReservesColumns.CHANNELNAME, channelName);
                        values.put(Channel.TableReservesColumns.ENDTIME, endTime);
                        values.put(Channel.TableReservesColumns.STARTTIME, startTime);
                        
                        Cursor query = managedQuery(Channel.URI.TABLE_RESERVES, null, "startTime=? and serviceId=?", new String[]{""+startTime,""+serviceId}, null);
                        if(query.getCount() == 0){
                            Uri uri = getContentResolver().insert(Channel.URI.TABLE_RESERVES, values);
                            String realTimeStr = mJniEpgSearch.getUTCTime();
                            log.D("------------------------------------------------------------------------get UTC time is "+realTimeStr);
                            //+ Long.valueOf(splitTime[1])*1000;
                            String[] splitTime = realTimeStr.split(":");
                            long realTime = Long.valueOf(splitTime[0])*1000;
                            long timeCompensate = realTime - System.currentTimeMillis();
                            if(Long.valueOf(splitTime[0])==0){
                                log.D("time commpenstae is 0");
                                timeCompensate = 0;
                            }
                            log.D("start Time is "+startTime*1000);
                            if((startTime*1000+timeCompensate) < System.currentTimeMillis()){
                                AdapterViewSelectionUtil.showToast(PlayActivity.this,R.string.program_reserve_time_out);
                            }else{
                                View programReserveTagView = mEpgProgramListView.findViewWithTag(mProgramListPosition);
                                programReserveTagView.setVisibility(View.VISIBLE);
                                addReServeProgramToAlam(Integer.valueOf(uri.getLastPathSegment()), startTime*1000+timeCompensate-60*1000);
                                AdapterViewSelectionUtil.showToast(PlayActivity.this, R.string.program_reserve_success);
                            }
                        }else {
                            AdapterViewSelectionUtil.showToast(PlayActivity.this, R.string.program_reserve_failed);
                        }
                    }
                });
                proBuilder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dismissDialog(DialogId.PROGRAM_RESERVE_ASK);
                    }
                });
                return proBuilder.create();
            case DialogId.PROGRAM_RESERVE_LIST_CANCEL:
                AlertDialog.Builder cancelBuilder = new AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                cancelBuilder.setTitle(getResources().getString(R.string.alert));
                cancelBuilder.setMessage(getResources().getString(R.string.program_reserve_list_cancel));
                cancelBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        int startTime = mProgramReserveCursor.getInt(mProgramReserveCursor.getColumnIndex(Channel.TableReservesColumns.STARTTIME));
                        int serviceId = mProgramReserveCursor.getInt(mProgramReserveCursor.getColumnIndex(Channel.TableReservesColumns.SERVICEID));
                        int id =mProgramReserveCursor.getInt(mProgramReserveCursor.getColumnIndex(Channel.TableReservesColumns.ID));
                        removeReserveProgramFromAlarm(id);
                        getContentResolver().delete(Channel.URI.TABLE_RESERVES,
                                Channel.TableReservesColumns.STARTTIME+"=? and "+Channel.TableReservesColumns.SERVICEID+"=?",
                                new String[]{""+startTime,""+serviceId});
                        mProgramReserveCursor.requery();
                        if(mProgramReserveCursor.getCount() == 0){
                            mProgramReserveNoChannelTextView.setVisibility(View.VISIBLE);
                            mProgramReservesListView.setFocusable(false);
                            mProgramReserveNoChannelTextView.requestFocus();
                        }else {
                            mProgramReserveNoChannelTextView.setVisibility(View.INVISIBLE);
                        }
                        mProgramReservesListAdapter.setCursor(mProgramReserveCursor);
                        mProgramReservesListAdapter.notifyDataSetChanged();
                    }
                });
                cancelBuilder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dismissDialog(DialogId.PROGRAM_RESERVE_LIST_CANCEL);
                    }
                });
                return cancelBuilder.create();
        }
        
        return super.onCreateDialog(id);
    }
    private DisplayMetrics mDisplayMetrics = new DisplayMetrics();
    private Display mDisplay;
    /**
     * JNI播放初始化
     */
    private void jniPlayInit(){
        log.D("jniPlayInit()");
        
        mJniPlay = new JniChannelPlay();
        
        mJniSetting = new JniSetting();
        
        
        
        log.D("play.getUriString() = "+mJniPlay.getUriString());
        log.D("mJniPlay.init(); start ********************************************");
        mJniPlay.init();
        log.D("mJniPlay.init(); end   ********************************************");
        
        log.D("mJniPlay.play()   ********************************************");
        mJniPlay.play();
        
        mJniPlay.setOnMonitorListener(new OnMonitorListener() {
            
            @Override
            public void onMonitor(int monitorType, Object message) {
                log.D("onMonitor monitorType = "+monitorType);
                log.D("onMonitor message = "+message);
                switch (monitorType) {
                case DefaultParameter.NotificationAction.NOTIFICATION_TVNOTIFY_TUNER_SIGNAL:
                    int code = (Integer) message;
                    log.D("case tuner status lock = "+code);
                    if (code == DefaultParameter.NotificationAction.TunerStatus.ACTION_TUNER_UNLOCKED) {
                        // TODO send broadcast to notify other
                        mainHandler.sendEmptyMessage(MainHandlerMsg.NOTIFY_LOCKED_FAILED);
                    } else if (code == DefaultParameter.NotificationAction.TunerStatus.ACTION_TUNER_LOCKED) {
                        
                        mainHandler.sendEmptyMessage(MainHandlerMsg.NOTIFY_LOCKED_OK);
                        
                    }
                    
                    break;
                case DefaultParameter.NotificationAction.NOTIFICATION_TVNOTIFY_BUYMSG:
                    
                    int mage = (Integer) message;
                    
                    if(mage == DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_CANCEL_TYPE){
                        if(showCaNotifyWindow != null){
                            showCaNotifyWindow.dismiss();
                        }
                    }else{
                        if(showCaNotifyWindow != null){
                            showCaNotifyWindow.dismiss();
                        }
                        
                        Message msg = Message.obtain();
                        msg.what = MainHandlerMsg.NOTIFY_CA_MESSAGE;
                        msg.arg1 = mage;
                        mainHandler.sendMessage(msg);
                    }
                    
                    break;
                }
                
            }
        });
        
    }

    /**
     * JNI播放释放资源
     */
    private void jniPlayUninit(){
        log.D("jniPlayUninit()");
        
        log.D("mJniPlay.stop()");
        mJniPlay.stop();
        log.D("mJniPlay.uninit()");
        mJniPlay.uninit();
        
        finish();
    }

    /**
     * 回看功能
     */
    private void backSee(){
        log.D("backSee()");
        
        if(mPreChannelIndex == mCurrentChannelIndex){
            log.D("backSee() same channel no need back see ...");
            return;
        }
        
        Message msg = new Message();
        msg.what = WorkHandlerMsg.SWITCH_TO_SPECIAL;
        msg.arg1 = mPreChannelIndex;
        workHandler.sendMessage(msg);
        
        //TODO 待区分广播和电视的序号差别：
        switch(mCurrentPlayType){
        case PLAY_TYPE_TV:
            
            
            
            break;
        case PLAY_TYPE_BC:
            
            
            
            break;
        }
        
    }

    /**
     * 发送指定键值的模拟按键
     * @param key 指定的键值
     */
    private void doInjectKeyEvent(int key) {
        log.D("doInjectKeyEvent key = "+key);
        Instrumentation inst = new Instrumentation();
        inst.sendKeyDownUpSync(key);
    }

    /**
     * 保存当前音量，在音量调节键抬起的时候
     */
    private void saveCurrentVolumeInWorkThread(){
        log.D("saveCurrentVolumeInWorkThread()");
        int progress = volumeSeekBar.getProgress();
        log.D("volumeSeekBar.getProgress() = "+progress);
        log.D("saveCurrentVolumeInWorkThread mCurrentChannelNumber = "+mCurrentChannelNumber);
        // 存入map，以后还用呢
        mVolumesMap.put(mCurrentChannelNumber, progress);
        // save to DB
        String[] whereArgs = new String[1];
        log.D("channelCursor current channel number = " + mCurrentChannelNumber);
        whereArgs[0] = "" + mCurrentChannelNumber;
        
        ContentValues values = new ContentValues();
        values.put(Channel.TableChannelsColumns.VOLUME, progress);
        
        String where = ""+Channel.TableChannelsColumns.SERVICETYPE+"="+mCurrentPlayType+
                " and "+Channel.TableChannelsColumns.LOGICCHNUMBER + "=?";
        
        getContentResolver().update(Channel.URI.TABLE_CHANNELS, values, where, whereArgs);
        
        // 不能再查了，会乱的
//        channelCursor.requery();
//        channelCursor.moveToPosition(mCurrentChannelIndex);
        
    }

    private PopupWindow showSymbolNoWindow = null;

    private void showSymbolNoWindow() {

        if (showSymbolNoWindow == null) {
            View mAudioTrackView = getLayoutInflater().inflate(
                    R.layout.symbol_no_layout, null);
            showSymbolNoWindow = new PopupWindow(mAudioTrackView);
            showSymbolNoWindow.setWidth(716);
            showSymbolNoWindow.setHeight(173);
            showSymbolNoWindow.setFocusable(false);
        }
        showSymbolNoWindow.showAtLocation(this.getWindow().getDecorView(),
                Gravity.CENTER, 0, 0);
    }

    private PopupWindow showCaNotifyWindow = null;
    private void showCaNotifyWindow(String notifyString){
        showCaNotifyWindow = null;
        View mAudioTrackView = getLayoutInflater().inflate(
                R.layout.notify_layout, null);
        TextView text = (TextView) mAudioTrackView.findViewById(R.id.notify_text);
        text.setText(notifyString);
        showCaNotifyWindow = new PopupWindow(mAudioTrackView);
        showCaNotifyWindow.setWidth(716);
        showCaNotifyWindow.setHeight(173);
        showCaNotifyWindow.setFocusable(false);
        showCaNotifyWindow.showAtLocation(this.getWindow().getDecorView(),
                Gravity.CENTER, 0, 0);
    }

    /**
     * 判断启动的Intent的参数，以便直接进入某个菜单功能
     */
    private void catFormLauncher(){
        log.D("catFormLauncher()");
        Intent intent = getIntent();
        String value = intent.getStringExtra(DefaultParameter.DvbIntent.KEY);
        log.D("catFormLauncher() value = "+value);
        if(value == null || value.equals("")){
            return;
        }
        if(value.equals(DefaultParameter.DvbIntent.INTENT_BROADCAST)){
            
            mainHandler.sendEmptyMessage(MainHandlerMsg.CHANGE_LAYOUT_BC);
            
        }else if(value.equals(DefaultParameter.DvbIntent.INTENT_CHANNEL_LIST)){
            
            viewGroup.hideAllViews();
            showChannelsWindow();
            
        }else if(value.equals(DefaultParameter.DvbIntent.INTENT_FAVORITE_LIST)){
            
            viewGroup.hideAllViews();
            showFavoritesWindow();
            
        }else if(value.equals(DefaultParameter.DvbIntent.INTENT_PROGRAM_GUIDE)){
            // TODO
        }else if(value.equals(DefaultParameter.DvbIntent.INTENT_RESERVE_LIST)){
            // TODO
        }
    }

    /**
     * 保存当前伴音
     * @param index
     */
    private void setAudioIndex(int index){
        ContentValues valuse = new ContentValues();
        valuse.put(Channel.TableChannelsColumns.AUDIOINDEX, index);
        
        String[] whereArgs = new String[1];
        log.D("channelCursor current channel number = " + mCurrentChannelNumber);
        whereArgs[0] = "" + mCurrentChannelNumber;
        String where = ""+Channel.TableChannelsColumns.SERVICETYPE+"="+mCurrentPlayType+
                " and "+Channel.TableChannelsColumns.LOGICCHNUMBER + "=?";
        
        getContentResolver().update(Channel.URI.TABLE_CHANNELS, valuse, where, whereArgs);
        
        log.D("setAudioIndex========================="+index);
        channelCursor.requery();
        channelCursor.moveToPosition(mCurrentChannelIndex);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////   EPG 区域                                                                    /////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    /**
     *EPG
     *@author songwenxuan
     **/
    private JniEpgSearch mJniEpgSearch;
    private ListView mEpgChannelListView;
    private ListView mEpgProgramListView;
    private int mProgramListPosition;
    
    private LayoutInflater mEpgInflater;  
    private PopupWindow showEpgWindow = null;
    private View mEpgDateView;
    private EpgChannelListAdapter epgDateAdapter = null;
    private EpgProgramListAdapter epgProgramListAdapter = null;
    private int mlastEpgChannelListPosition = mCurrentChannelIndex;

    private TextView mProgramGuideWeekTextView;
    private TextView mProgramGuideDateTextView;
    private TextView mProgramGuideTimeTextView;
    
    private Calendar mAbsCalendar;
    private Calendar mCurrentCalendar;

    private ProgressBar mEpgProgressbar;
    private TextView mEpgProgressTitleTextView;
    
    private tagEpgEvent[] mEpgEvents;
    
    private boolean isEpg;
    private boolean isCallback;
    private boolean isGet;
    
    private int mLastTransportId;
    
    private void showEpgWindow() {
        mlastEpgChannelListPosition = mCurrentChannelIndex;
        log.D("show Epg Window");
        if (showEpgWindow == null) {
            mEpgDateView = getLayoutInflater().inflate(R.layout.program_guide, null);
            showEpgWindow = new PopupWindow(mEpgDateView);
            showEpgWindow.setWidth(1521);
            showEpgWindow.setHeight(811);
            showEpgWindow.setFocusable(true);
        }
        
        if(mJniEpgSearch == null){
            mJniEpgSearch = new JniEpgSearch();
        }
        //设置回调，当返回数据的时候调用。
        JniEpgSearch.listener =new OnEpgListener() {
            
            @Override
            public void onEpgDataReceived(int notifyCode) {
                //显示出当前频道当前时间的programs
                if(isEpg){
                    if(!isGet){
                        log.D("onEpgDataReceived SongWenxuan *************************************************************");
                        mainHandler.sendEmptyMessage(MainHandlerMsg.EPG_SHOW);
                        isCallback = true;
                    }
                }
            }
        };
        
        mEpgChannelListView = (ListView) mEpgDateView.findViewById(R.id.epg_channel_list);
        mEpgProgramListView = (ListView) mEpgDateView.findViewById(R.id.epg_program_list);
        
        mProgramGuideWeekTextView = (TextView) mEpgDateView.findViewById(R.id.program_guide_week);
        mProgramGuideDateTextView = (TextView) mEpgDateView.findViewById(R.id.program_guide_date);
        mProgramGuideTimeTextView = (TextView) mEpgDateView.findViewById(R.id.program_guide_time);
        
        mEpgProgressbar = (ProgressBar) mEpgDateView.findViewById(R.id.epg_progressbar);
        mEpgProgressTitleTextView = (TextView) mEpgDateView.findViewById(R.id.epg_progress_title_text);
        
        mEpgChannelListView.setDividerHeight(0);
        
        if(mEpgInflater == null){
            mEpgInflater = LayoutInflater.from(this);
        }
        
        epgDateAdapter = new EpgChannelListAdapter(channelCursor,mEpgInflater);
        
        epgProgramListAdapter = new EpgProgramListAdapter(PlayActivity.this,mEpgInflater);
        
        mEpgChannelListView.setAdapter(epgDateAdapter);
        
        mEpgProgramListView.setDividerHeight(0);
        
        mEpgProgramListView.setOnKeyListener(new OnKeyListener() {
            
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP){
                    showEpgWindow.dismiss();
                    showMenuWindow(PopupMsgId.SHOW_MENU, PopMenuList.PROGRAM_GUIDE);
                    isEpg = false;
                    return true;
                }
                if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN){
                    mEpgChannelListView.requestFocus();
                }
                //前一天
                if(keyCode == DvbKeyEvent.KEYCODE_YELLOW && event.getAction() == KeyEvent.ACTION_DOWN){
                    if(mCurrentCalendar.get(Calendar.DAY_OF_YEAR) == mAbsCalendar.get(Calendar.DAY_OF_YEAR)){
                        AdapterViewSelectionUtil.showToast(PlayActivity.this, R.string.program_guide_toast_before);
                        
                    } else {
                        mEpgEvents = null;
                        if(mCurrentCalendar.get(Calendar.DAY_OF_YEAR) == mAbsCalendar.get(Calendar.DAY_OF_YEAR)+1){
                            mCurrentCalendar.add(Calendar.DAY_OF_YEAR, -1);
                            mEpgEvents = mJniEpgSearch.getEpgDataByDuration(channelCursor.getInt(channelCursor.getColumnIndex("ServiceId")),mAbsCalendar.getTimeInMillis(),DateFormatUtil.getNextDate(mCurrentCalendar));
                        }else {
                            mCurrentCalendar.add(Calendar.DAY_OF_YEAR, -1);
                            mEpgEvents = mJniEpgSearch.getEpgDataByDuration(channelCursor.getInt(channelCursor.getColumnIndex("ServiceId")),mCurrentCalendar.getTimeInMillis(),DateFormatUtil.getNextDate(mCurrentCalendar));
                        }
                        //get programs
                        if(mEpgEvents!=null && mEpgEvents.length!=0){
                            log.D(" before  day epgevents length "+mEpgEvents.length);
                            epgProgramListAdapter.setEpgEvents(mEpgEvents);
                            epgProgramListAdapter.notifyDataSetChanged();
                            
                            mEpgProgressbar.setVisibility(View.INVISIBLE);
                            mEpgProgressTitleTextView.setVisibility(View.INVISIBLE);
                        }
                        refreshDate(mCurrentCalendar);
                    }
                    return true;
                }
                //后一天
                if(keyCode == DvbKeyEvent.KEYCODE_BLUE && event.getAction() == KeyEvent.ACTION_DOWN){
                    if(mCurrentCalendar.get(Calendar.DAY_OF_WEEK) == 1){
                        AdapterViewSelectionUtil.showToast(PlayActivity.this, R.string.program_guide_toast_after);
                    }else {
                        mCurrentCalendar.add(Calendar.DAY_OF_YEAR, 1);
                        //get programs
                        mEpgEvents = mJniEpgSearch.getEpgDataByDuration(channelCursor.getInt(channelCursor.getColumnIndex("ServiceId")),mCurrentCalendar.getTimeInMillis(),DateFormatUtil.getNextDate(mCurrentCalendar));
                        if(mEpgEvents!=null && mEpgEvents.length!=0){
                            log.D(" before  day epgevents length "+mEpgEvents.length);
                            epgProgramListAdapter.setEpgEvents(mEpgEvents);
                            epgProgramListAdapter.notifyDataSetChanged();
                            
                            mEpgProgressbar.setVisibility(View.INVISIBLE);
                            mEpgProgressTitleTextView.setVisibility(View.INVISIBLE);
                        }
                        
                        refreshDate(mCurrentCalendar);
                    }
                    return true;
                }
                return false;
            }
        });
        
        mEpgProgramListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //记录position；
                mProgramListPosition = position;
                showDialog(DialogId.PROGRAM_RESERVE_ASK);
            }
        });
        
        mEpgChannelListView.setVerticalScrollBarEnabled(false);
        mEpgChannelListView.setDividerHeight(0);
        mEpgChannelListView.setSelection(mCurrentChannelIndex);
        mEpgChannelListView.requestFocus();
        mEpgChannelListView.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP){
                    showEpgWindow.dismiss();
                    isEpg = false;
                    showMenuWindow(PopupMsgId.SHOW_MENU, PopMenuList.PROGRAM_GUIDE);
                    return true;
                }
                if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
                    mEpgProgramListView.setSelection(0);
                    mEpgProgramListView.requestFocus();
                    return true;
                }
                return false;
            }
            
        });
        
        mEpgChannelListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                
                /*channelCursor.moveToPosition(position);
                workHandler.sendEmptyMessage(WorkHandlerMsg.PLAY_START);*/
                //切台
                int channelIndex = position;
                Message msg = Message.obtain();
                msg.what = WorkHandlerMsg.SWITCH_TO_SPECIAL;
                msg.arg1 = channelIndex;
                workHandler.sendMessage(msg);
                workHandler.sendEmptyMessage(WorkHandlerMsg.PLAY_START);
                
                mEpgProgramListView.setVisibility(View.INVISIBLE);
                isGet = false;
                isCallback = false;
                //发消息，get
                mainHandler.sendEmptyMessage(MainHandlerMsg.EPG_SHOW);
                
                mCurrentChannelIndex = position;
                ChannelTypeNumUtil.savePlayChannel(PlayActivity.this,DefaultParameter.ServiceType.digital_television_service , mCurrentChannelIndex);
                //给提示
                mEpgProgressbar.setVisibility(View.VISIBLE);
                mEpgProgressTitleTextView.setText(R.string.program_guide_progressbar_title);
                mEpgProgressTitleTextView.setVisibility(View.VISIBLE);
                
                //设置选择的频道背景。
                if(mlastEpgChannelListPosition != -1 && position<=mlastEpgChannelListPosition+6 && position>=mlastEpgChannelListPosition-6){
                    System.out.println("lastEpgChannelListPosition is "+mlastEpgChannelListPosition );
                    View viewWithTag = mEpgChannelListView.findViewWithTag(mlastEpgChannelListPosition);
                    if(viewWithTag!= null){
                        viewWithTag.setBackgroundResource(R.color.Transparent);
                    }
                }
                view.setBackgroundResource(R.drawable.epg_listview_selected);
                mlastEpgChannelListPosition = position;
            }
        });
        mEpgChannelListView.setOnScrollListener(new OnScrollListener() {
            
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                
            }
            
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                    int totalItemCount) {
                if(mCurrentChannelIndex>=firstVisibleItem && mCurrentChannelIndex<=firstVisibleItem+visibleItemCount-1){
                    View findViewWithTag = mEpgChannelListView.findViewWithTag(mCurrentChannelIndex);
                    findViewWithTag.setBackgroundResource(R.drawable.epg_listview_selected);
                }
            }
        });
        
        initEpgDate();
        //首次显示第一天的epg
        showFirstDayProgramList();
        channelCursor.moveToPosition(mCurrentChannelIndex);
        showEpgWindow.showAtLocation(PlayActivity.this.getWindow().getDecorView(), Gravity.CENTER_HORIZONTAL, 0, 0);
        isEpg = true;
        mainHandler.sendEmptyMessageDelayed(MainHandlerMsg.EPG_INIT_CHANNEL_ITEM_BACKGROUND,500);
    }

    private void initEpgDate() {
        if(mAbsCalendar == null){
            mAbsCalendar = Calendar.getInstance();
        }
        if(mCurrentCalendar == null){
            mCurrentCalendar = Calendar.getInstance();
        }
        Date date = new Date();
        date.setTime(System.currentTimeMillis());
        mAbsCalendar.setTime(date);
        //当前日期0点的时间点
        mCurrentCalendar.set(mAbsCalendar.get(Calendar.YEAR), mAbsCalendar.get(Calendar.MONTH), mAbsCalendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        //刷新界面显示日期
        refreshDate(mAbsCalendar);
    }

    private void refreshDate(Calendar calendar) {
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);//星期
        int hour = mAbsCalendar.get(Calendar.HOUR_OF_DAY);//24小时显示
        int minute = mAbsCalendar.get(Calendar.MINUTE);
        //星期
        String weekFromInt = DateFormatUtil.getWeekFromInt(dayOfWeek);
        mProgramGuideWeekTextView.setText(getResources().getString(R.string.program_guide_week)+weekFromInt);
        //日期
        String dateStr = DateFormatUtil.getDate(calendar.getTime());
        mProgramGuideDateTextView.setText(dateStr);
        //时间
        mProgramGuideTimeTextView.setText((hour < 10 ? "0"+hour : hour)+":"+(minute<10 ? "0"+minute : minute));
    }
    
    private void showFirstDayProgramList(){
        initEpgDate();
        long currentTimeMillis = System.currentTimeMillis();
        channelCursor.moveToPosition(mCurrentChannelIndex);
        mEpgEvents = mJniEpgSearch.getEpgDataByDuration(channelCursor.getInt(channelCursor.getColumnIndex("ServiceId")),currentTimeMillis,DateFormatUtil.getNextDate(mCurrentCalendar));
        
        log.D("current channel service id = "+channelCursor.getInt(channelCursor.getColumnIndex("ServiceId")));
        if(isCallback && (mEpgEvents == null || mEpgEvents.length == 0)){
            mEpgProgressTitleTextView.setText(R.string.program_guide_prompts);
            mEpgProgressbar.setVisibility(View.INVISIBLE);
            mLastTransportId = channelCursor.getInt(channelCursor.getColumnIndex("Transponder_id"));
        }
        
        int tpId = channelCursor.getInt(channelCursor.getColumnIndex("Transponder_id"));
        if(mEpgEvents == null && tpId == mLastTransportId){
                mEpgProgressTitleTextView.setText(R.string.program_guide_prompts);
                mEpgProgressbar.setVisibility(View.INVISIBLE);
                mLastTransportId = channelCursor.getInt(channelCursor.getColumnIndex("Transponder_id"));
        }
        
        if(mEpgEvents != null && mEpgEvents.length != 0){
            log.D("is not null"+mEpgEvents.length);
            epgProgramListAdapter.setEpgEvents(mEpgEvents);
            mEpgProgramListView.setAdapter(epgProgramListAdapter);
            epgProgramListAdapter.notifyDataSetChanged();
            
            mEpgProgressbar.setVisibility(View.INVISIBLE);
            mEpgProgressTitleTextView.setVisibility(View.INVISIBLE);
            mEpgProgramListView.setVisibility(View.VISIBLE);
            isGet = true;
            mLastTransportId = channelCursor.getInt(channelCursor.getColumnIndex("Transponder_id"));
        }else {
            
        }
    }
    
    /**
     * 预约列表 
     */
    private PopupWindow showProgramReservesWindow;
    private View mProgramReservesView;
    private ProgramReservesListAdapter mProgramReservesListAdapter;
    private ListView mProgramReservesListView;
    private List<Integer> mReserveListTags;
    private TextView mProgramReserveNoChannelTextView;
//    private int mReserveListTag;
//    private Map<Integer, tagEpgEvent> mEpgEventMap;

    private Cursor mProgramReserveCursor;

    private ProgramReservesBroadcastReceiver programReservesBroadcastReceiver;
    
    private void showProgramReservesWindow() {
        log.D("show program reserves window");
        if (showProgramReservesWindow == null) {
            mProgramReservesView = getLayoutInflater().inflate(R.layout.program_reserves_list, null);
            showProgramReservesWindow = new PopupWindow(mProgramReservesView);
            showProgramReservesWindow.setWidth(1131);
            showProgramReservesWindow.setHeight(642);
            showProgramReservesWindow.setFocusable(true);
        }
        if(mReserveListTags == null){
            mReserveListTags = new ArrayList<Integer>();
        }
        mProgramReservesListView = (ListView) mProgramReservesView.findViewById(R.id.program_reserves_listview);
        mProgramReserveNoChannelTextView = (TextView) mProgramReservesView.findViewById(R.id.progaram_reserve_no_channel_textview);
        mProgramReservesListView.setDividerHeight(0);
        mProgramReserveCursor = managedQuery(Channel.URI.TABLE_RESERVES, null, null, null, Channel.TableReservesColumns.STARTTIME);
        while (mProgramReserveCursor.moveToNext()) {
            int startTimeDB = mProgramReserveCursor.getInt(mProgramReserveCursor.getColumnIndex(Channel.TableReservesColumns.STARTTIME));
            long realStartTime = (long)startTimeDB;
            log.D(realStartTime*1000+"    db startTime");
            log.D(System.currentTimeMillis()+"    system time");
            if(realStartTime*1000 < System.currentTimeMillis()){
                getContentResolver().delete(Channel.URI.TABLE_RESERVES, Channel.TableReservesColumns.STARTTIME+"=?", new String[]{""+startTimeDB});
            }
        }
        mProgramReserveCursor.requery();
        if(mProgramReservesListAdapter == null){
            mProgramReservesListAdapter = new ProgramReservesListAdapter(mProgramReserveCursor, getLayoutInflater());
            mProgramReservesListView.setAdapter(mProgramReservesListAdapter);
        }else {
            mProgramReservesListAdapter.setCursor(mProgramReserveCursor);
            mProgramReservesListAdapter.setInflater(getLayoutInflater());
            mProgramReservesListAdapter.notifyDataSetChanged();
        }
        
        mProgramReservesListView.setOnKeyListener(new OnKeyListener() {
            
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP){
                    showProgramReservesWindow.dismiss();
                    showMenuWindow(PopupMsgId.SHOW_MENU, PopMenuList.ORDER_LIST);
                    return true;
                }
                return false;
            }
        });
        
       /* mProgramReservesView.setOnKeyListener(new OnKeyListener() {
            
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP){
                    showProgramReservesWindow.dismiss();
                    showMenuWindow(PopupMsgId.SHOW_MENU, PopMenuList.ORDER_LIST);
                    return true;
                }
                return false;
            }
        });*/
        
        mProgramReservesListView.setOnItemClickListener(new OnItemClickListener() {
            
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showDialog(DialogId.PROGRAM_RESERVE_LIST_CANCEL);
//                mReserveListTag = position;
                mProgramReserveCursor.moveToPosition(position);
            }
        });
        mProgramReservesListView.setFocusable(true);
        mProgramReservesListView.requestFocus();
        if(mProgramReserveCursor.getCount() == 0){
            mProgramReserveNoChannelTextView.setVisibility(View.VISIBLE);
            mProgramReservesListView.setFocusable(false);
            mProgramReserveNoChannelTextView.requestFocus();
        }else {
            mProgramReserveNoChannelTextView.setVisibility(View.INVISIBLE);
        }
        mProgramReserveNoChannelTextView.setOnKeyListener(new OnKeyListener() {
            
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP){
                    showProgramReservesWindow.dismiss();
                    showMenuWindow(PopupMsgId.SHOW_MENU, PopMenuList.ORDER_LIST);
                    return true;
                }
                return false;
            }
        });
        showProgramReservesWindow.showAtLocation(PlayActivity.this.getWindow().getDecorView(), Gravity.CENTER_HORIZONTAL, 0, 0);
    }
    
    /** 预约BroadcastReceiver */
    class ProgramReservesBroadcastReceiver extends BroadcastReceiver {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            showProgramReservesAlert();
        }
    }
    
    /** 预约提示 */
    private PopupWindow showProgramReservesAlert;
    private View mProgramReservesAlertView;

    private ImageView mProgramReserveAlertUpImageView;
    private ImageView mProgramReserveAlertDownImageView;

    private Button mProgramReserveAlertWatchButton;

    private Button mProgramReserveAlertCancelButton;
    private List<tagEpgEvent> mProgramResoveAlertList;
    private int mProgramReserveAlertTag;

    private TextView mProgramReserveAlertDateTextView;
    private TextView mProgramReserveAlertTimeTextView;
    private TextView mProgramReserveAlertChannelNameTextView;
    private TextView mProgramReserveAlertProgramNameTextView;
    private TextView mProgramReserveAlertSecondTextView;
    
    private int mProgramReserveTimerSecond; 
    /*private Timer mProgramReserveTimer;
    private TimerTask programReserveTask = new TimerTask() {
        
        @Override
        public void run() {
            mainHandler.sendEmptyMessage(MainHandlerMsg.PROGRAM_RESERVE_REFRESH_TIME);
        }
    };*/

    
    
    private void showProgramReservesAlert() {
        log.D("show program reserves window");
        if (showProgramReservesAlert == null) {
            mProgramReservesAlertView = getLayoutInflater().inflate(R.layout.program_reserves_alert, null);
            showProgramReservesAlert = new PopupWindow(mProgramReservesAlertView);
            showProgramReservesAlert.setWidth(1920);
            showProgramReservesAlert.setHeight(220);
            showProgramReservesAlert.setFocusable(true);
        }
        
        mProgramReserveAlertUpImageView = (ImageView) mProgramReservesAlertView.findViewById(R.id.program_reserve_alert_up);
        mProgramReserveAlertDownImageView = (ImageView) mProgramReservesAlertView.findViewById(R.id.program_reserve_alert_down);
        
        mProgramReserveAlertWatchButton = (Button) mProgramReservesAlertView.findViewById(R.id.program_reserve_alert_button_watch);
        mProgramReserveAlertCancelButton = (Button) mProgramReservesAlertView.findViewById(R.id.program_reserve_alert_button_cancel);
        
        mProgramReserveAlertSecondTextView = (TextView) mProgramReservesAlertView.findViewById(R.id.program_reserve_alert_seconds);
        mProgramReserveAlertDateTextView = (TextView) mProgramReservesAlertView.findViewById(R.id.program_reserve_alert_date);
        mProgramReserveAlertTimeTextView = (TextView) mProgramReservesAlertView.findViewById(R.id.program_reserve_alert_time);
        mProgramReserveAlertChannelNameTextView = (TextView) mProgramReservesAlertView.findViewById(R.id.program_reserve_alert_channel_name);
        mProgramReserveAlertProgramNameTextView = (TextView) mProgramReservesAlertView.findViewById(R.id.program_reserve_alert_program_name);
        
        if(mProgramResoveAlertList == null){
            mProgramResoveAlertList = new ArrayList<tagEpgEvent>();
        }
        mProgramReserveAlertTag = 0;
        Cursor managedQuery = managedQuery(Channel.URI.TABLE_RESERVES, null, null, null, null);
        String realTimeStr = mJniEpgSearch.getUTCTime();
        log.D("------------------------------------------------------------------------get UTC time is "+realTimeStr);
        //+ Long.valueOf(splitTime[1])*1000;
        String[] splitTime = realTimeStr.split(":");
        long realTime = Long.valueOf(splitTime[0])*1000;
        long timeCompensate = realTime - System.currentTimeMillis();
        if(Long.valueOf(splitTime[0])==0){
            log.D("time commpenstae is 0");
            timeCompensate = 0;
        }
        while(managedQuery.moveToNext()){
            long startTime = (long)managedQuery.getInt(managedQuery.getColumnIndex(Channel.TableReservesColumns.STARTTIME));
            //测试 使用12000秒   && (startTime*1000 - System.currentTimeMillis() > 0)
            if(((startTime*1000 + timeCompensate) - System.currentTimeMillis() < 120*1000) && (startTime*1000 + timeCompensate - System.currentTimeMillis() > 0)){
                
                String programName = managedQuery.getString(managedQuery.getColumnIndex(Channel.TableReservesColumns.PROGRAMNAME));
                String channelName = managedQuery.getString(managedQuery.getColumnIndex(Channel.TableReservesColumns.CHANNELNAME));
                int serviceId = managedQuery.getInt(managedQuery.getColumnIndex(Channel.TableReservesColumns.SERVICEID));
                int id = managedQuery.getInt(managedQuery.getColumnIndex(Channel.TableReservesColumns.ID));
                
                tagEpgEvent epgEvent = new tagEpgEvent();
                epgEvent.setStartTime(startTime*1000);
                epgEvent.setProgramName(programName);
                epgEvent.setServiceId(serviceId);
                epgEvent.setId(id);
                epgEvent.setProgramDescription(channelName);
                
                mProgramResoveAlertList.add(epgEvent);
            }
        }
        if(mProgramResoveAlertList.size()>0){
            if(mProgramResoveAlertList.size()>1){
                mProgramReserveAlertUpImageView.setVisibility(View.VISIBLE);
                mProgramReserveAlertDownImageView.setVisibility(View.VISIBLE);
                refreshProgramReserveAlertData(mProgramReserveAlertTag);
            }else {
                mProgramReserveAlertUpImageView.setVisibility(View.INVISIBLE);
                mProgramReserveAlertDownImageView.setVisibility(View.INVISIBLE);
                refreshProgramReserveAlertData(mProgramReserveAlertTag);
            }
        }else {
            showProgramReservesAlert.dismiss();
            return;
        }
        
        mProgramReserveAlertUpImageView.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                //上一个
                --mProgramReserveAlertTag;
                if(mProgramReserveAlertTag < 0){
                    mProgramReserveAlertTag = mProgramResoveAlertList.size()-1;
                }
                refreshProgramReserveAlertData(mProgramReserveAlertTag);
            }
        });
        
        mProgramReserveAlertDownImageView.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                //下一个
                ++mProgramReserveAlertTag;
                if(mProgramReserveAlertTag > mProgramResoveAlertList.size()-1){
                    mProgramReserveAlertTag = 0;
                }
                refreshProgramReserveAlertData(mProgramReserveAlertTag);
            }
        });
        
        mProgramReserveAlertWatchButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                //切台
                String channelName = mProgramReserveAlertChannelNameTextView.getText().toString();
                Cursor tChannelCursor= managedQuery(Channel.URI.TABLE_CHANNELS, new String []{Channel.TableChannelsColumns.LOGICCHNUMBER}, Channel.TableChannelsColumns.SERVICENAME+"=? ", new String[]{channelName}, null);
                tChannelCursor.moveToNext();
                int logicChNumber = tChannelCursor.getInt(tChannelCursor.getColumnIndex(Channel.TableChannelsColumns.LOGICCHNUMBER));
                int channelIndex = logicChNumber - 1;
                Message msg = Message.obtain();
                msg.what = WorkHandlerMsg.SWITCH_TO_SPECIAL;
                msg.arg1 = channelIndex;
                workHandler.sendMessage(msg);
                workHandler.sendEmptyMessage(WorkHandlerMsg.PLAY_START);
                showProgramReservesAlert.dismiss();
                deleteAlertReserveData();
                mProgramResoveAlertList.clear();
                mProgramResoveAlertList = null;
            }
        });
        
        mProgramReserveAlertCancelButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                showProgramReservesAlert.dismiss();
                //删除预约数据
                deleteAlertReserveData();
                mProgramResoveAlertList.clear();
                mProgramResoveAlertList = null;
            }
        });
        mProgramReserveTimerSecond = 60;
        mProgramReserveAlertSecondTextView.setText(""+mProgramReserveTimerSecond);
        showProgramReservesAlert.showAtLocation(PlayActivity.this.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
        /*if(mProgramReserveTimer == null){
            mProgramReserveTimer = new Timer();
        }
        mProgramReserveTimer.schedule(programReserveTask, 0, 1000);*/
        workHandler.sendEmptyMessage(WorkHandlerMsg.PROGRAM_RESERVE_TIMER_START);
    }
    
    /** 删除预约弹出数据 */
    private void deleteAlertReserveData() {
        if(mProgramResoveAlertList != null){
            for (tagEpgEvent event : mProgramResoveAlertList) {
                int id = event.getId();
                int serviceId = event.getServiceId();
                long startTime = event.getStartTime()/1000;
                log.D("delete program alert    start time"+startTime);
                getContentResolver().delete(Channel.URI.TABLE_RESERVES, 
                        Channel.TableReservesColumns.STARTTIME+"=? and "+Channel.TableReservesColumns.SERVICEID+"=? ", 
                        new String[]{startTime+"",serviceId+""});
                removeReserveProgramFromAlarm(id);
            }
        }
    }
    
    /** 删除预约闹钟 */
    private void removeReserveProgramFromAlarm(int id) {
        
        log.D("removeReserveProgramFromAlarm(), enter! reserveId=" + id);
        Intent intent = new Intent("program alarm");
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
    
    /** 刷新预约数据 */
    private void refreshProgramReserveAlertData(int i) {
        
        tagEpgEvent tagEpgEvent = mProgramResoveAlertList.get(i);
        //设置显示数据
        long startTime = tagEpgEvent.getStartTime();
        String date = DateFormatUtil.getDateFromMillis(startTime);
        String time = DateFormatUtil.getTimeFromMillis(startTime);
        String programName = tagEpgEvent.getProgramName();
        String channelName = tagEpgEvent.getProgramDescription();
        
        mProgramReserveAlertDateTextView.setText(date);
        mProgramReserveAlertTimeTextView.setText(time);
        mProgramReserveAlertChannelNameTextView.setText(channelName);
        mProgramReserveAlertProgramNameTextView.setText(programName);
    }
    
    /** 添加预约闹钟 */
    private void addReServeProgramToAlam(int id, long startTime){
        
        log.D("addReServeProgramToAlam() -- reserveId=" + id + ", startTime=" + startTime);
        Intent intent = new Intent("program alarm");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, startTime, pendingIntent);
    }
}