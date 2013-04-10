package novel.supertv.dvb.activity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import novel.supertv.dvb.R;
import novel.supertv.dvb.adapter.ChannelSearchedAdapter;
import novel.supertv.dvb.jni.struct.TuningParam;
import novel.supertv.dvb.jni.struct.tagTunerSignal;
import novel.supertv.dvb.service.SearchService;
import novel.supertv.dvb.utils.DefaultParameter;
import novel.supertv.dvb.utils.DvbLog;
import novel.supertv.dvb.utils.TransponderUtil;
import novel.supertv.dvb.view.MyEditText;
import novel.supertv.dvb.view.MyEditText.OnInputDataErrorListener;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

public class SearchActivity extends TabActivity {

    private static final DvbLog log = new DvbLog(
            "novel.supertv.dvb.activity.SearchActivity",DvbLog.DebugType.D);
    private String mChannelFrequency;
    // 搜索类型,对应于STVMode
    /** 手动搜索 */
    public static final int SEARCH_TYPE_MANUAL = 0;
    /** 全频搜索 */
    public static final int SEARCH_TYPE_FULL   = 1;
    /** 自动搜索 */
    public static final int SEARCH_TYPE_AUTO   = 2;

    /**
     * 当前使用的搜索类型
     */
    private int mCurrentSearchType = SEARCH_TYPE_FULL;

    private SearchService mBoundService;
    
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((SearchService.LocalBinder)service).getService();
            log.D("mConnection onServiceConnected mBoundService = "
                    +mBoundService.toString());
            
            // for test
//            workHandler.sendEmptyMessage(WorkHandlerMsg.START_SEARCH);
            
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService.setOnSearchEndListener(null);
            mBoundService.setOnSearchFindNewChannelListener(null);
            mBoundService.setOnSearchNewTransponderListener(null);
            mBoundService.setOnSearchProgressChangeListener(null);
            mBoundService = null;
            log.D("mConnection onServiceDisconnected");
        }
    };
    
    private void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        bindService(new Intent(this, 
                SearchService.class), mConnection, Context.BIND_AUTO_CREATE);
    }
    
    private void doUnbindService() {
        // Detach our existing connection.
        unbindService(mConnection);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log.D("onCreate()");
        
        setContentView(R.layout.search_content);
        
        setTabs();
        
        findViews();
        
        setupViews();
    }

    // default transponder from xml
    private TuningParam mDefaultTransponder;

    /** 当前频率 */
    private TextView mCurrent_Frequency_Text;
    /** 当前信号强度 */
    private TextView mCurrent_Strong_Text;
    /** 当前信号质量 */
    private TextView mCurrent_Quality_Text;

    private ProgressBar mProgressBar;
    private TextView mProgress_text;
    private TextView mChannel_count_text;

    private ListView mListView;

    private Button mButton;

    private ChannelSearchedAdapter mChannelListAdapter;

    /** 编辑框组 */
    private MyEditText mEditText[] = new MyEditText[3];
    /** 编辑框组，频率 */
    private static final int FREQUENCY = 0;
    /** 编辑框组，符号率 */
    private static final int SYMBOL_RATE = 1;
    /** 编辑框组，调制方式 */
    private static final int MODULATION = 2;
    

    private void findViews(){
        log.D("findViews()");
        switch(mCurrentSearchType){
        case SearchActivity.SEARCH_TYPE_FULL:
            
            mCurrent_Frequency_Text = (TextView) findViewById(R.id.current_frequency);
            mCurrent_Strong_Text = (TextView) findViewById(R.id.current_strong_text);
            mCurrent_Quality_Text = (TextView) findViewById(R.id.current_quality_text);
            mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
            mProgress_text = (TextView) findViewById(R.id.pregress_text);
            mChannel_count_text = (TextView) findViewById(R.id.channel_count_text);
            mListView = (ListView) findViewById(R.id.search_result_listview);
            mButton = (Button) findViewById(R.id.search_button);
            
            break;
        case SearchActivity.SEARCH_TYPE_MANUAL:
            
            mCurrent_Frequency_Text = (TextView) findViewById(R.id.current_frequency_manual);
            mCurrent_Strong_Text = (TextView) findViewById(R.id.current_strong_text_manual);
            mCurrent_Quality_Text = (TextView) findViewById(R.id.current_quality_text_manual);
            mProgressBar = (ProgressBar) findViewById(R.id.progress_bar_manual);
            mProgress_text = (TextView) findViewById(R.id.pregress_text_manual);
            mChannel_count_text = (TextView) findViewById(R.id.channel_count_text_manual);
            mListView = (ListView) findViewById(R.id.search_result_listview_manual);
            mButton = (Button) findViewById(R.id.search_button_manual);
            
            mEditText[FREQUENCY] = (MyEditText) findViewById(R.id.manual_setting_frequency_manual);
            mEditText[SYMBOL_RATE] = (MyEditText) findViewById(R.id.manual_setting_symbol_rate_manual);
            mEditText[MODULATION] = (MyEditText) findViewById(R.id.manual_setting_modulation_manual);
            
            break;
        case SearchActivity.SEARCH_TYPE_AUTO:
            
            mCurrent_Frequency_Text = (TextView) findViewById(R.id.current_frequency_auto);
            mCurrent_Strong_Text = (TextView) findViewById(R.id.current_strong_text_auto);
            mCurrent_Quality_Text = (TextView) findViewById(R.id.current_quality_text_auto);
            mProgressBar = (ProgressBar) findViewById(R.id.progress_bar_auto);
            mProgress_text = (TextView) findViewById(R.id.pregress_text_auto);
            mChannel_count_text = (TextView) findViewById(R.id.channel_count_text_auto);
            mListView = (ListView) findViewById(R.id.search_result_listview_auto);
            mButton = (Button) findViewById(R.id.search_button_auto);
            
            mEditText[FREQUENCY] = (MyEditText) findViewById(R.id.manual_setting_frequency_auto);
            mEditText[SYMBOL_RATE] = (MyEditText) findViewById(R.id.manual_setting_symbol_rate_auto);
            mEditText[MODULATION] = (MyEditText) findViewById(R.id.manual_setting_modulation_auto);
            
            break;
        }
    }

    /**
     * 初始化一些View的显示
     */
    private void setupViews(){
        log.D("setupViews()");
        mListView.setFocusable(false);
        mListView.setFocusableInTouchMode(false);
        mListView.setItemsCanFocus(false);
        
        mChannelListAdapter = new ChannelSearchedAdapter(this, getLayoutInflater());
        
        mListView.setAdapter(mChannelListAdapter);
        mButton.setText(R.string.channel_search_startsearchtext);
        mButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {

                Log.d("onClick", "mButton");
                // if the text == re search 重新搜索
                if (mButton.getText().toString().equals(getResources().getString(
                                R.string.channel_search_startsearchtext))||
                    mButton.getText().toString().equals(getResources().getString(
                                R.string.channel_search_researchtext))
                        ) {
                        
                        clearAll();
                        
                        mButton.setText(R.string.channel_search_stopsearchtext);
                        
                        // start search
                        
                        workHandler.sendEmptyMessage(WorkHandlerMsg.START_SEARCH);
                        
                } else {// stop search
                        workHandler.sendEmptyMessage(WorkHandlerMsg.STOP_SEARCH);
                        mButton.setText(getResources().getString(
                                R.string.channel_search_startsearchtext));
                        
                        // 暂时修改方便陈晓康测试
                        // 关闭搜索模式键，此后不拦截按键
                        mSearchModeKey = false;
                    }
                }
        });
        
        // 有各自特点的
        
        switch(mCurrentSearchType){
        case SearchActivity.SEARCH_TYPE_FULL:
            
            mDefaultTransponder = TransponderUtil.getDefaultTransponder(
                    this, 
                    DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_MANUAL);
            
            break;
        case SearchActivity.SEARCH_TYPE_AUTO:
            
            mDefaultTransponder = TransponderUtil.getDefaultTransponder(
                    this, 
                    DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_MAINTP);
            
            break;
        case SearchActivity.SEARCH_TYPE_MANUAL:
            
            mDefaultTransponder = TransponderUtil.getDefaultTransponder(
                    this, 
                    DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_MANUAL);
            
            break;
        }
        
        if(mCurrentSearchType == SearchActivity.SEARCH_TYPE_FULL){
            log.D("not need init editTest , so return;");
            return;
        }
        
        if(mDefaultTransponder == null){
            log.D("defaultTransponder from TransponderUtil is null");
            return;
        }
        
        mEditText[FREQUENCY].setRange(DefaultParameter.SearchParameterRange.FREQUENCY_MIN, 
                DefaultParameter.SearchParameterRange.FREQUENCY_MAX);
        
        mEditText[FREQUENCY].setText(""+mDefaultTransponder.getFrequency()/1000);
        
        mEditText[FREQUENCY].setOnInputDataErrorListener(new OnInputDataErrorListener() {
            
            public void onInputDataError(int errorType) {
                switch(errorType){
                case MyEditText.INPUT_DATA_ERROR_TYPE_NULL:
                    SearchActivity.this.showDialog(DialogId.DIALOG_ALERT_FREQUENCY_NULL);
                    mEditText[FREQUENCY].setText(""+mDefaultTransponder.getFrequency()/1000);
                    break;
                case MyEditText.INPUT_DATA_ERROR_TYPE_OUT:
                    SearchActivity.this.showDialog(DialogId.DIALOG_ALERT_FREQUENCY);
                    mEditText[FREQUENCY].setText(""+mDefaultTransponder.getFrequency()/1000);
                    break;
                case MyEditText.INPUT_DATA_ERROR_TYPE_NORMAL:
                    
                    break;
                }
            }
        });
        
        mEditText[SYMBOL_RATE].setRange(DefaultParameter.SearchParameterRange.SYMBOLRATE_MIN, 
                DefaultParameter.SearchParameterRange.SYMBOLRATE_MAX);
        
        mEditText[SYMBOL_RATE].setText(""+mDefaultTransponder.getSymbolRate());
        
        mEditText[SYMBOL_RATE].setOnInputDataErrorListener(new OnInputDataErrorListener() {
            
            public void onInputDataError(int errorType) {
                switch(errorType){
                case MyEditText.INPUT_DATA_ERROR_TYPE_NULL:
                    SearchActivity.this.showDialog(DialogId.DIALOG_ALERT_SYMBOL_NULL);
                    mEditText[SYMBOL_RATE].setText(""+mDefaultTransponder.getSymbolRate());
                    break;
                case MyEditText.INPUT_DATA_ERROR_TYPE_OUT:
                    SearchActivity.this.showDialog(DialogId.DIALOG_ALERT_SYMBOL);
                    mEditText[SYMBOL_RATE].setText(""+mDefaultTransponder.getSymbolRate());
                    break;
                case MyEditText.INPUT_DATA_ERROR_TYPE_NORMAL:
                    
                    break;
                }
            }
        });
        
//        mEditText[MODULATION].setText(""+mDefaultTransponder.getModulation());
        
        mEditText[MODULATION].setText(turnModToUi(mDefaultTransponder.getModulation()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        log.D("onResume()");
        
        // 如果是从后台回来的，那么就不用再执行了
        if(!workThread.isAlive()){
            startWorkThread();
        }
        
        doBindService();
    }

    /**
     * 键盘锁
     * 用于频道搜索过程中
     * 不允许操作部分按键：
     * 左右键，返回键
     * 
     */
    private boolean mSearchModeKey;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch(event.getKeyCode()){
        case KeyEvent.KEYCODE_DPAD_DOWN:
        case KeyEvent.KEYCODE_DPAD_UP:
        case KeyEvent.KEYCODE_DPAD_LEFT:
        case KeyEvent.KEYCODE_DPAD_RIGHT:
        case KeyEvent.KEYCODE_BACK:
            
            if(mSearchModeKey){
                log.D("dispatchKeyEvent in search processing so can not move !");
                return true;
            }
            
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        log.D("onPause()");
        
        saveTp();
        
        this.removeAllHandlerMessages();
    }

    @Override
    protected void onStop() {
        super.onStop();
        log.D("onStop()");
        
        doUnbindService();
    }

    /**
     * 工作用的Handler对象
     */
    private Handler workHandler;

    /**
     * 工作线程
     */
    private HandlerThread workThread = new HandlerThread("search work thread");

    /**
     * 主线程消息集合
     */
    private static class MainHandlerMsg{
        public static final int CHANNEL_SEARCH_RESULT_PROGRESS = 01;
        public static final int CHANNEL_SEARCH_RESULT_FREQUENCY = 02;
        public static final int CHANNEL_SEARCH_RESULT_NAME_LIST = 04;
        /** 搜索结束 */
        public static final int CHANNEL_SEARCH_RESULT_END = 05;
        
        /** dismiss 提示对话框 */
        public static final int DIALOG_ALERT_DISMISS = 06;
    }

    /**
     * 工作线程消息集合
     *
     */
    public static class WorkHandlerMsg{
        public static final int START_SEARCH = 1001;
        public static final int STOP_SEARCH = 1002;
    }

    private void removeAllHandlerMessages(){
        mainHandler.removeMessages(MainHandlerMsg.CHANNEL_SEARCH_RESULT_PROGRESS);
        mainHandler.removeMessages(MainHandlerMsg.CHANNEL_SEARCH_RESULT_FREQUENCY);
        mainHandler.removeMessages(MainHandlerMsg.CHANNEL_SEARCH_RESULT_NAME_LIST);
        mainHandler.removeMessages(MainHandlerMsg.CHANNEL_SEARCH_RESULT_END);
        mainHandler.removeMessages(MainHandlerMsg.DIALOG_ALERT_DISMISS);
        
        workHandler.removeMessages(WorkHandlerMsg.START_SEARCH);
        workHandler.removeMessages(WorkHandlerMsg.STOP_SEARCH);
    }

    /**
     * UI主线程的Handler
     */
    private Handler mainHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {

            switch(msg.arg1){
            case MainHandlerMsg.CHANNEL_SEARCH_RESULT_PROGRESS:
                
                log.D("search progress = "+msg.arg2);
                
                // progress bar
                mProgressBar.setProgress(msg.arg2);
                // progress %
                mProgress_text.setText(""+msg.arg2+"%");
                
                break;
            case MainHandlerMsg.CHANNEL_SEARCH_RESULT_FREQUENCY:
                
                log.D("search frequency = "+msg.arg2/1000+"MHz");
                mCurrent_Frequency_Text.setText(""+msg.arg2/1000+"MHz");
                mChannelFrequency=""+msg.arg2/1000+"MHz";
                tagTunerSignal signal = (tagTunerSignal) msg.obj;
                
                mCurrent_Strong_Text.setText(""+signal.getCN());
                mCurrent_Quality_Text.setText(""+signal.getLevel());
                
                
                break;
            case MainHandlerMsg.CHANNEL_SEARCH_RESULT_NAME_LIST:
                
                if(msg.obj == null){
                    break;
                }
                
                log.D("search channel name = "+(String)msg.obj);
                log.D("search channel type = "+msg.arg2);
                
                mChannelListAdapter.add((String)msg.obj , msg.what, mChannelFrequency);
                mChannelListAdapter.notifyDataSetChanged();
                mListView.setAdapter(mChannelListAdapter);
                
                // channel count
                mChannel_count_text.setText(""+mChannelListAdapter.getCount());
                
                break;
            case MainHandlerMsg.CHANNEL_SEARCH_RESULT_END:
                
                // 关闭搜索模式键，此后不拦截按键
                mSearchModeKey = false;
                
                mButton.setText(R.string.channel_search_researchtext);
                
                // 如果Activity对出了就不显示对话框了，否则异常
                if(!isFinishing()){
                    showDialog(DialogId.SEARCHRESULT);
                }
                
                break;
            case MainHandlerMsg.DIALOG_ALERT_DISMISS:
                
                log.D("MainHandlerMsg.DIALOG_ALERT_DISMISS dialog id = "+msg.arg1);
                dismissDialog(msg.arg1);
                
                break;
            
            }
            
            super.handleMessage(msg);
        }
        
        
    };

    private void startWorkThread(){
        log.D("startWorkThread()");
        
        workThread.start();
        
        workHandler = new Handler(workThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what){
                case WorkHandlerMsg.START_SEARCH:
                    
                    startSearch();
                    
                    break;
                case WorkHandlerMsg.STOP_SEARCH:
                    
                    stopSearch();
                    
                    break;
                }
                super.handleMessage(msg);
            }  
        };
        
    }

    /**
     * Start to search
     */
    private void startSearch(){
        log.D("startSearch()");
        
        // 开始搜索模式键打开,此后直到结束，不允许按键
        mSearchModeKey = true;
        
        // 构建假参数：
        
        saveTp();
        
        mBoundService.startSearch(mCurrentSearchType, mDefaultTransponder);
        
        /*mBoundService.setOnSearchEndListener(new SearchService.OnSearchEndListener() {
            
            public void onSearchEnd() {
                Log.d("onSearchEnd()", "onSearchEnd()");
                
                Message msg = Message.obtain();
                msg.arg1 = MainHandlerMsg.CHANNEL_SEARCH_RESULT_END;
                mainHandler.sendMessage(msg);
            }
        });*/
        
        mBoundService.setOnSearchFindNewChannelListener(new SearchService.OnSearchFindNewChannelListener() {
            
            public boolean onFindNewChannel(String arg0, int arg1) {
                Log.d("onFindNewChannel()", "Channel name = "+arg0);
                
                Message msg = Message.obtain();
                msg.arg1 = MainHandlerMsg.CHANNEL_SEARCH_RESULT_NAME_LIST;
                msg.what = arg1;
                msg.obj = arg0;
                mainHandler.sendMessage(msg);
                return true;
            }
        });
        
        mBoundService.setOnSearchNewTransponderListener(new SearchService.OnSearchNewTransponderListener() {
            
            public void onSearchNewTransponder(TuningParam transponder, tagTunerSignal signal) {
                log.D("onSearchNewTransponder() Transponder = "+transponder.getFrequency());
                
                Message msg = Message.obtain();
                msg.arg1 = MainHandlerMsg.CHANNEL_SEARCH_RESULT_FREQUENCY;
                msg.arg2 = transponder.getFrequency();
                msg.obj = signal;
                mainHandler.sendMessage(msg);
            }
        });
        
        mBoundService.setOnSearchProgressChangeListener(new SearchService.OnSearchProgressChangeListener() {
            
            public void onSearchProgressChanged(int arg0) {
                log.D("onSearchProgressChanged() arg0 ="+arg0);
                
                Message msg = Message.obtain();
                msg.arg1 = MainHandlerMsg.CHANNEL_SEARCH_RESULT_PROGRESS;
                msg.arg2 = arg0;
                mainHandler.sendMessage(msg);
            }
        });
        
    }

    private void stopSearch(){
        
        if(mBoundService != null){
            mBoundService.stopSearch();
        }
    }

    /**
     * 所有的对话框集合
     */
    private static final class DialogId{
        private static final int DIALOG_ALERT_FREQUENCY = 0;
        private static final int DIALOG_ALERT_SYMBOL = 1;
        private static final int DIALOG_ALERT_FREQUENCY_NULL = 2;
        private static final int DIALOG_ALERT_SYMBOL_NULL = 3;
        
        private static final int SEARCHRESULT = 4;
        private static final int SEARCH_NO_CHANNEL = 5;
        private static final int SEARCH_STOP_ASK = 6;
        private static final int SEARCH_NO_CHANNEL_MANUAL = 7;
        private static final int DIALOG_ALERT_EXIT = 8;
    }

    protected Dialog onCreateDialog(int id) {
        
        switch(id){
        case DialogId.SEARCHRESULT:
            // show ask dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_DARK);
            builder.setTitle(getResources().getString(R.string.alert));
            builder.setMessage(getResources().getString(R.string.search_completed));
            builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    
                    Intent intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setClass(SearchActivity.this,
                            PlayActivity.class);
                    // you can put something into intent.

                    SearchActivity.this.startActivity(intent);
                    SearchActivity.this.finish();
                }
            });

            builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    
                    clearAll();
                    
                    cancelSaveChannel();
                }
            });
            return builder.create();
        case DialogId.SEARCH_STOP_ASK:
            // show ask dialog
            AlertDialog.Builder dialog_builder = new AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_DARK);
            dialog_builder.setTitle(getResources().getString(R.string.alert));
            dialog_builder.setMessage(getResources().getString(R.string.search_stop_save_ask));
            dialog_builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    
                    Intent intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setClass(SearchActivity.this,
                            PlayActivity.class);
                    // you can put something into intent.
                    
                    SearchActivity.this.startActivity(intent);
                    SearchActivity.this.finish();
                    
                }
            });
            
            dialog_builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    
                }
            });
            dialog_builder.setOnKeyListener(new OnKeyListener() {
                
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    
                    if(keyCode == KeyEvent.KEYCODE_BACK){
                        
                    }
                    
                    return false;
                }
            });
            
            return dialog_builder.create();
        case DialogId.SEARCH_NO_CHANNEL:
            
            AlertDialog alertDialog = new AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_DARK).create();
            alertDialog.setTitle(getResources().getString(R.string.alert));
            alertDialog.setMessage(getResources().getString(R.string.search_no_channel));
            alertDialog.setButton((CharSequence)(getResources().getString(R.string.ok)), new DialogInterface.OnClickListener() {
                
                public void onClick(DialogInterface dialog, int which) {
                    
                    dialog.dismiss();
                }
            });
            alertDialog.setOnKeyListener(new OnKeyListener() {
                
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    
                    if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||  keyCode == KeyEvent.KEYCODE_BACK){
                        log.D("KEYCODE_DPAD_CENTER return false");
                        return false;
                    }
                    
                    return true;
                }
            });
            
            return alertDialog;
        case DialogId.SEARCH_NO_CHANNEL_MANUAL:
            // show ask dialog
            AlertDialog.Builder askbuilder = new AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_DARK);
            askbuilder.setTitle(getResources().getString(R.string.alert));
            askbuilder.setMessage(getResources().getString(R.string.search_no_channel_manual));
            askbuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    
                    Intent intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setClass(SearchActivity.this,
                            PlayActivity.class);
                    // you can put something into intent.
                    
                    SearchActivity.this.startActivity(intent);
                    SearchActivity.this.finish();
                }
            });
            
            askbuilder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    
                }
            });
            return askbuilder.create();
        case DialogId.DIALOG_ALERT_FREQUENCY:
        case DialogId.DIALOG_ALERT_FREQUENCY_NULL:
        case DialogId.DIALOG_ALERT_SYMBOL:
        case DialogId.DIALOG_ALERT_SYMBOL_NULL:
            
            AlertDialog alert = new AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_DARK).create();
            alert.setTitle(getResources().getString(R.string.alert));
            alert.setOnKeyListener(new OnKeyListener() {
                public boolean onKey(DialogInterface arg0, int arg1,
                        KeyEvent event) {
                    // 任意按键都能让提示对话框消失
                    if(event.getAction() == KeyEvent.ACTION_DOWN){
                        arg0.dismiss();
                    }
                    return true;
                }
                
            });
            // 这个是用来在组装对话框时必须的，用来显示消息内容
            // 可以在onPrepareDialog中改变的，如果这里没有那么
            // 对话框就没这个组件了
            alert.setMessage("");
            return alert;
        //退出搜索提示对话框。
        case DialogId.DIALOG_ALERT_EXIT:
            
            AlertDialog.Builder exitSearchBuilder = new Builder(this,
                    AlertDialog.THEME_DEVICE_DEFAULT_DARK);
            exitSearchBuilder.setTitle("提示");
            exitSearchBuilder.setMessage("您确定退出搜索页面？");
            
            exitSearchBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            exitSearchBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            exitSearchBuilder.setOnKeyListener(new OnKeyListener() {
                
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    
                    if(keyCode == KeyEvent.KEYCODE_BACK){
                        return true;
                    }
                    return false;
                }
            });
            return exitSearchBuilder.create();
        }
        
        return super.onCreateDialog(id);
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        AlertDialog alertDialog = (AlertDialog)dialog;
        
        switch(id){
        case DialogId.DIALOG_ALERT_FREQUENCY:
            
            alertDialog.setMessage(getResources().getString(
                    R.string.frequency_out_of_range));
            break;
            
        case DialogId.DIALOG_ALERT_FREQUENCY_NULL:
            
            alertDialog.setMessage(getResources().getString(
                    R.string.frequency_null));
            break;
            
        case DialogId.DIALOG_ALERT_SYMBOL:
            
            alertDialog.setMessage(getResources().getString(
                    R.string.symbol_rate_out_of_range));
            break;
            
        case DialogId.DIALOG_ALERT_SYMBOL_NULL:
            
            alertDialog.setMessage(getResources().getString(
                    R.string.symbol_rate_null));
            break;
        }
        
        Message msg = new Message();
        msg.what = MainHandlerMsg.DIALOG_ALERT_DISMISS;
        msg.arg1 = id;
        mainHandler.sendMessageDelayed(msg, 3000);
        
        super.onPrepareDialog(id, dialog);
    }

    /**
     * 当页面不显示的时候保存tp数据
     */
    private void saveTp(){
        
        if(mDefaultTransponder == null){
            log.D("onPause() defaultTransponder == null !");
            mDefaultTransponder = new TuningParam();
        }
        

        
        switch(mCurrentSearchType){
        case SearchActivity.SEARCH_TYPE_FULL:
            
            break;
        case SearchActivity.SEARCH_TYPE_AUTO:
            
            // 按类型保存Transponder, 使用contentProvider
            mDefaultTransponder.setFrequency(Integer.parseInt(mEditText[FREQUENCY].getText().toString())*1000);
            mDefaultTransponder.setSymbolRate(Integer.parseInt(mEditText[SYMBOL_RATE].getText().toString()));
            mDefaultTransponder.setModulation(turnUiToMod(mEditText[MODULATION].getText().toString()));
            
            log.D("onPause() DEFAULT_TRANSPONDER_TYPE_MAINTP");
            TransponderUtil.saveDefaultTransponer(
                    this, 
                    DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_MAINTP,
                    mDefaultTransponder);
            
            log.D(""+mDefaultTransponder.toString());
            
            break;
        case SearchActivity.SEARCH_TYPE_MANUAL:
            log.D("onPause() SEARCHTYPE_MANUAL");
            
            // 按类型保存Transponder, 使用contentProvider
            mDefaultTransponder.setFrequency(Integer.parseInt(mEditText[FREQUENCY].getText().toString())*1000);
            mDefaultTransponder.setSymbolRate(Integer.parseInt(mEditText[SYMBOL_RATE].getText().toString()));
            mDefaultTransponder.setModulation(turnUiToMod(mEditText[MODULATION].getText().toString()));
            
            TransponderUtil.saveDefaultTransponer(
                    this, 
                    DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_MANUAL, 
                    mDefaultTransponder);
            
            log.D(""+mDefaultTransponder.toString());
            
            break;
        }
    }

    /**
     * 设置tab的布局和显示
     */
    private void setTabs(){
        
        TabHost tabHost = getTabHost();
        
        LayoutInflater.from(this).inflate(R.layout.search_layout, tabHost.getTabContentView(), true);

        tabHost.addTab(tabHost.newTabSpec(getResources().getString(R.string.channel_search_full))
                .setIndicator("")
                .setContent(R.id.view1));
        tabHost.addTab(tabHost.newTabSpec(getResources().getString(R.string.channel_search_manual))
                .setIndicator("")
                .setContent(R.id.view2));
        tabHost.addTab(tabHost.newTabSpec(getResources().getString(R.string.channel_search_auto))
                .setIndicator("")
                .setContent(R.id.view3));
        
        tabHost.setOnTabChangedListener(new OnTabChangeListener() {
            
            @Override
            public void onTabChanged(String tabId) {
                log.D("onTabChanged tabId = "+tabId);
                if(tabId.equals(getResources().getString(R.string.channel_search_full))){
                    
                    mCurrentSearchType = SEARCH_TYPE_FULL;
                    
                }else if(tabId.equals(getResources().getString(R.string.channel_search_manual))){
                    
                    mCurrentSearchType = SEARCH_TYPE_MANUAL;
                    
                }else if(tabId.equals(getResources().getString(R.string.channel_search_auto))){
                    
                    mCurrentSearchType = SEARCH_TYPE_AUTO;
                    
                }else{
                    log.E("no tab , error !");
                }
                
                // 重新设置view的引用
                findViews();
                
                setupViews();
            }
        });
        
        tabHost.getTabWidget().getChildTabViewAt(0).setBackgroundResource(R.drawable.search_tab_item_left_selector);
        
        tabHost.getTabWidget().getChildTabViewAt(1).setBackgroundResource(R.drawable.search_tab_item_middle_selector);
        
        tabHost.getTabWidget().getChildTabViewAt(2).setBackgroundResource(R.drawable.search_tab_item_right_selector);
        
    }

    /**
     * 转换调制方式
     * 对应关系：
     * 32 1
     * 64 2
     * 128 3
     * 256 4
     * @param mod JNI层需要的调制方式参数，如：2，3，4，5...
     * @return UI显示的调制方式如：64，128...
     */
    private String turnModToUi(int mod){
        log.D("turnModToUi mod ="+mod);
        
        log.D("turnModToUi mModMap.get(mod) = "+mModMap.get(mod));
        
        return mModMap.get(mod);
    }

    /**
     * 转换调制方式
     * 对应关系：
     * 32 1
     * 64 2
     * 128 3
     * 256 4
     * @param mod UI显示的调制方式如：64，128...
     * @return JNI层需要的调制方式参数，如：2，3，4，5...
     */
    private int turnUiToMod(String mod){
        log.D("turnUiToMod mod ="+mod);
        
        Iterator iterator = mModMap.keySet().iterator();
        while (iterator.hasNext()) {
            Object key = iterator.next();
            String value = mModMap.get(key);
            
            if(mod.equals(value)){
                
                log.D("turnUiToMod key ="+key);
                
                return (Integer) key;
            }
        } 
        return 0;
    }

    /**
     * 初始化键值表
     */
    private static Map<Integer,String> mModMap;

    static{
        mModMap = new HashMap<Integer,String>();
        mModMap.put(1, "32");
        mModMap.put(2, "64");
        mModMap.put(3, "128");
        mModMap.put(4, "256");
    }

    /**
     * 清除搜索结果的显示
     */
    private void clearAll(){
        log.D("clearAll()");
        // clear channel name list
        new Handler().post(new Runnable() {

            public void run() {
                mChannelListAdapter.clear();
                mChannelListAdapter.notifyDataSetChanged();
            }
        });
        // clear channel count.
        
        mProgressBar.setProgress(0);
        mProgress_text.setText(""+0+"%");
        mChannel_count_text.setText(""+0);
        
    }

    /**
     * 取消刚才保存的频道
     */
    private void cancelSaveChannel(){
        log.D("cancelSaveChannel()");
        
        if(mBoundService != null){
            mBoundService.deleteChannels();
        }
        
        
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            showDialog(DialogId.DIALOG_ALERT_EXIT);
        }
        return super.onKeyDown(keyCode, event);
    }
}
