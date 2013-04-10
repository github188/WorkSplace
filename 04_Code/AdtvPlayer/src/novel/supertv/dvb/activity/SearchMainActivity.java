
package novel.supertv.dvb.activity;

import java.util.ArrayList;
import java.util.List;

import novel.supertv.dvb.R;
import novel.supertv.dvb.adapter.ChannelSearchedAdapter;
import novel.supertv.dvb.jni.struct.TuningParam;
import novel.supertv.dvb.jni.struct.stChannel;
import novel.supertv.dvb.jni.struct.tagTunerSignal;
import novel.supertv.dvb.service.SearchService;
import novel.supertv.dvb.utils.ChannelTypeNumUtil;
import novel.supertv.dvb.utils.DefaultParameter;
import novel.supertv.dvb.utils.DvbLog;
import novel.supertv.dvb.utils.TransponderUtil;
import novel.supertv.dvb.view.CustomProgressDialog;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SearchMainActivity extends Activity implements OnClickListener {

    private static final DvbLog log = new DvbLog(
            "novel.supertv.dvb.activity.AutoSearchActivity", DvbLog.DebugType.D);
    private String mChannelFrequency;
    public static final String FULLSEARCH = "full";
    public static final String SEARCHTYPE = "searchType";
    public static final String MANUALSEARCH = "manual";

    public static class WorkHandlerMsg {
        public static final int START_SEARCH = 1001;
        public static final int STOP_SEARCH = 1002;
        
        /** 保存搜索的频道到数据库 */
        public static final int SAVE_DB = 1003;
    }

    /**
     * 主线程消息集合
     */
    private static class MainHandlerMsg {
        public static final int CHANNEL_SEARCH_RESULT_PROGRESS = 01;
        public static final int CHANNEL_SEARCH_RESULT_FREQUENCY = 02;
        public static final int CHANNEL_SEARCH_RESULT_NAME_LIST = 04;
        /** 搜索结束 */
        public static final int CHANNEL_SEARCH_RESULT_END = 05;
        
        /** dismiss 提示对话框 */
        public static final int DIALOG_ALERT_DISMISS = 06;
        /** 停止搜索 */
        public static final int CHANNEL_SEARCH_RESULT_STOP=07;
    }

    private ChannelSearchedAdapter mChannelListAdapter;
    private ProgressBar mProgressBar;
    private TextView mProgress_text;
    private TextView mCurrent_frequency;
    private TextView mCurrent_strong_text;
    private TextView mCurrent_quality_text;
    private ListView mChannelListView;
    private SearchService mBoundService;
    private TuningParam mDefaultTransponder;
    private Button mSearchButton;
    private Handler workHandler;
    private TextView mChannel_count;
    private boolean isCompleted = true;
    private HandlerThread workThread = new HandlerThread(
            "fast search work thread");
    /** 手动搜索 */
    public static final int SEARCH_TYPE_MANUAL = 0;
    /** 全频搜索 */
    public static final int SEARCH_TYPE_FULL = 1;
    /** 自动搜索 */
    public static final int SEARCH_TYPE_AUTO = 2;

    public static final int AUTOSEARCH_REQUESTCODE = 2001;
    private int mCurrentSearchType = SEARCH_TYPE_AUTO;
    private boolean isAdvanced;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log.D("AutoSearchActivity create.");
        setContentView(R.layout.auto_search);
        initView();
        setupSearchType();
        setupView();
    }

    /** 设置默认的搜索类型 */
    private void setupSearchType() {
        isNoChannel = getIntent().getBooleanExtra("isNoChannel", false);
        String searchType = getIntent().getStringExtra(SEARCHTYPE);
        if (FULLSEARCH.equals(searchType)) {
            log.D("full search");
            mCurrentSearchType = SEARCH_TYPE_FULL;
            mSearchTitle.setText(R.string.full_search_title);
        } else {
            Bundle bundle = getIntent().getBundleExtra(SearchManualActivity.MANUALSEARCH);
            if (bundle != null) {
                mSearchTitle.setText(R.string.manual_search_main);
                log.D("bundle 不为空");
                if (mDefaultTransponder == null) {
                    mDefaultTransponder = new TuningParam();
                }
                mAdvancedButton.setVisibility(View.INVISIBLE);
                boolean isManual = bundle.getBoolean("isManual");
                if (isManual) {
                    mCurrentSearchType = SEARCH_TYPE_MANUAL;
                    mSearchButton.setVisibility(View.INVISIBLE);
                }
                int frequency = bundle.getInt(SearchManualActivity.FREQUENCY);
                int modulation = bundle.getInt(SearchManualActivity.MODULATION);
                int symbolRate = bundle.getInt(SearchManualActivity.SYMBOLRATE);
                mDefaultTransponder.setFrequency(frequency);
                mDefaultTransponder.setModulation(modulation);
                mDefaultTransponder.setSymbolRate(symbolRate);
                log.D(mDefaultTransponder.toString());
            } else {
                mCurrentSearchType = SEARCH_TYPE_AUTO;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        log.D("onResume()");
        if (!workThread.isAlive()) {
            startWorkThread();
            log.D("start workThread!!");
        }
        doBindService();
        if (mCurrentSearchType == SEARCH_TYPE_MANUAL) {
            workHandler.sendEmptyMessageDelayed(WorkHandlerMsg.START_SEARCH, 500);
        }
        if(isNoChannel){
            workHandler.sendEmptyMessageDelayed(WorkHandlerMsg.START_SEARCH, 500);
            mAdvancedButton.setVisibility(View.INVISIBLE);
            mSearchButton.setText(R.string.stop_search);
        }
    }

    private void doBindService() {
        // Establish a connection with the service. We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        bindService(new Intent(this, SearchService.class), mConnection, Context.BIND_AUTO_CREATE);

    }

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((SearchService.LocalBinder) service).getService();
            log.D("mConnection onServiceConnected mBoundService = " + mBoundService.toString());
        }

        public void onServiceDisconnected(ComponentName className) {
            mBoundService.setOnSearchEndListener(null);
            mBoundService.setOnSearchFindNewChannelListener(null);
            mBoundService.setOnSearchNewTransponderListener(null);
            mBoundService.setOnSearchProgressChangeListener(null);
            mBoundService = null;
            log.D("mConnection onServiceDisconnected");
        }
    };

    /**
     * UI主线程的Handler
     */
    private Handler mainHandler = new Handler() {
        
        @Override
        public void handleMessage(final Message msg) {
            
            switch (msg.arg1) {
                case MainHandlerMsg.CHANNEL_SEARCH_RESULT_STOP:
                    
                 // 关闭搜索模式键，此后不拦截按键
                    mSearchModeKey = false;
                    
                    mSearchButton.setText(R.string.channel_search_researchtext);
                    
                    if (!isFinishing()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                SearchMainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                        builder.setTitle(getResources().getString(R.string.stop_search_title));
                        builder.setMessage(getResources().getString(R.string.isSave));
                        builder.setPositiveButton(
                                getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        log.D("onClick to save db");
                                        workHandler.sendEmptyMessage(WorkHandlerMsg.SAVE_DB);
                                        startProgressDialog();
                                    }
                                });

                        builder.setNegativeButton(
                                getResources().getString(R.string.no),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                            int whichButton) {
                                        
                                        clearAll();
                                        
                                        cancelSaveChannel();
                                    }
                                });
                        builder.create().show();
                    }
                    break;
                case MainHandlerMsg.CHANNEL_SEARCH_RESULT_PROGRESS:
                    log.D("search progress = " + msg.arg2);
                    // progress bar
                    mProgressBar.setProgress(msg.arg2);
                    // progress %
                    mProgress_text.setText("" + msg.arg2 + "%");
                    
                    break;
                case MainHandlerMsg.CHANNEL_SEARCH_RESULT_FREQUENCY:
                    log.D("search frequency = " + msg.arg2 / 1000 + "MHz");
                    mCurrent_frequency.setText("" + msg.arg2 / 1000 + "MHz");
                    mChannelFrequency = "" + msg.arg2 / 1000 + "MHz";
                    
                    tagTunerSignal signal = (tagTunerSignal) msg.obj;
                    if((signal.getCN()*100/255)>90){
                        mCurrent_strong_text.setText("90%");
                    }else if((signal.getCN()*100/255)<5){
                        mCurrent_strong_text.setText("5%");
                    }else{
                        mCurrent_strong_text.setText("" + signal.getCN()*100/255 + "%");
                    }
                    if((signal.getLevel()*100/255)>90){
                        mCurrent_quality_text.setText("90%");
                    }else if((signal.getLevel()*100/255)<5){
                        mCurrent_quality_text.setText("5%");
                    }else {
                        mCurrent_quality_text.setText("" + signal.getLevel()*100/255 + "%");
                    }
                    break;
                case MainHandlerMsg.CHANNEL_SEARCH_RESULT_NAME_LIST:
                    log.D("search channel name = " + (String) msg.obj);
                    log.D("search channel type = " + msg.arg2);
                    if (msg.obj == null) {
                        break;
                    }
                    
                    mChannelListAdapter.add((String) msg.obj, msg.what, mChannelFrequency);
                    mChannelListAdapter.notifyDataSetChanged();
                    mChannelListView.setAdapter(mChannelListAdapter);
                    
                    // channel count
                    mChannel_count.setText("" + mChannelListAdapter.getCount());
                    
                    break;
                case MainHandlerMsg.CHANNEL_SEARCH_RESULT_END:
                    
                    // 关闭搜索模式键，此后不拦截按键
                    mSearchModeKey = false;
                    
                    mSearchButton.setText(R.string.channel_search_researchtext);
                    
                    // 如果Activity退出了就不显示对话框了，否则异常
                    if (!isFinishing()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                SearchMainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                        builder.setTitle(getResources().getString(R.string.alert));
                        builder.setMessage(getResources().getString(R.string.search_completed));
                        builder.setPositiveButton(
                                getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        log.D("onClick to save db");
                                        workHandler.sendEmptyMessage(WorkHandlerMsg.SAVE_DB);
                                        startProgressDialog();
                                    }
                                });

                        builder.setNegativeButton(
                                getResources().getString(R.string.no),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                            int whichButton) {
                                        
                                        clearAll();
                                        /*if(mCurrentSearchType != SEARCH_TYPE_MANUAL){
                                            cancelSaveChannel();
                                        }*/
                                    }
                                });
                        builder.create().show();
                    }
                    
                    break;
                case MainHandlerMsg.DIALOG_ALERT_DISMISS:
                    
                    dismissDialog(msg.arg1);
                    
                    break;
            }
            
            super.handleMessage(msg);
        }
        
    };

    /**
     * 取消刚才保存的频道
     */
    private void cancelSaveChannel() {
        
        if (mBoundService != null) {
            mBoundService.deleteChannels();
        }
    }

    private void startWorkThread() {
        workThread.start();
        
        workHandler = new Handler(workThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case WorkHandlerMsg.START_SEARCH:
                        log.D("开始搜索" + mDefaultTransponder.toString());
                        startSearch();
                        
                        break;
                    case WorkHandlerMsg.STOP_SEARCH:
                        
                        stopSearch();
                        
                        break;
                    case WorkHandlerMsg.SAVE_DB:
                        
                        mBoundService.saveChannels(channels);
                        
                        // 复位正在播放频道类型和频道号的记录
                        // 先将广播频道号设为0
                        ChannelTypeNumUtil.savePlayChannel(
                                getApplicationContext(),
                                DefaultParameter.ServiceType.digital_radio_sound_service,
                                0
                                );
                        // 再将电视频道号设为0，并且最终频道类型是电视
                        ChannelTypeNumUtil.savePlayChannel(
                                getApplicationContext(),
                                DefaultParameter.ServiceType.digital_television_service,
                                0
                                );
                        
                        Intent intent = new Intent();
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setClass(SearchMainActivity.this, PlayActivity.class);
                        // you can put something into intent.
                        
                        SearchMainActivity.this.startActivity(intent);
                        SearchMainActivity.this.finish();
                        
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }
    List<stChannel> channels;
    /**
     * Start to search
     */
    private void startSearch() {
        mSearchModeKey = true;
        saveTp();
        System.gc();
        log.D("tp:" + mDefaultTransponder.toString()+"search type is "+ mCurrentSearchType);
        if(mCurrentSearchType == SEARCH_TYPE_FULL || mCurrentSearchType==SEARCH_TYPE_AUTO){
            cancelSaveChannel();
        }
        mBoundService.startSearch(mCurrentSearchType, mDefaultTransponder);
        
        mBoundService.setOnSearchEndListener(new SearchService.OnSearchEndListener() {
            
            public void onSearchEnd(List<stChannel> channels) {
                Log.d("onSearchEnd()", "onSearchEnd()");
                SearchMainActivity.this.channels=channels;
                if(isCompleted){
                    Message msg = Message.obtain();
                    msg.arg1 = MainHandlerMsg.CHANNEL_SEARCH_RESULT_END;
                    mainHandler.sendMessage(msg);
                }else {
                    Message msg = Message.obtain();
                    msg.arg1 = MainHandlerMsg.CHANNEL_SEARCH_RESULT_STOP;
                    mainHandler.sendMessage(msg);
                }
            }
        });

        mBoundService
                .setOnSearchFindNewChannelListener(new SearchService.OnSearchFindNewChannelListener() {
                    
                    public boolean onFindNewChannel(String arg0, int arg1) {
                        Log.d("onFindNewChannel()", "Channel name = " + arg0);
                        Message msg = Message.obtain();
                        msg.arg1 = MainHandlerMsg.CHANNEL_SEARCH_RESULT_NAME_LIST;
                        msg.what = arg1;
                        msg.obj = arg0;
                        mainHandler.sendMessage(msg);
                        return true;
                    }
                });

        mBoundService
                .setOnSearchNewTransponderListener(new SearchService.OnSearchNewTransponderListener() {
                    
                    public void onSearchNewTransponder(TuningParam transponder,
                            tagTunerSignal signal) {
                        log.D("onSearchNewTransponder() Transponder = "
                                + transponder.getFrequency());
                        Message msg = Message.obtain();
                        msg.arg1 = MainHandlerMsg.CHANNEL_SEARCH_RESULT_FREQUENCY;
                        msg.arg2 = transponder.getFrequency();
                        msg.obj = signal;
                        mainHandler.sendMessage(msg);
                    }
                });

        mBoundService
                .setOnSearchProgressChangeListener(new SearchService.OnSearchProgressChangeListener() {
                    
                    public void onSearchProgressChanged(int arg0) {
                        log.D("onSearchProgressChanged() arg0 =" + arg0);
                        Message msg = Message.obtain();
                        msg.arg1 = MainHandlerMsg.CHANNEL_SEARCH_RESULT_PROGRESS;
                        msg.arg2 = arg0;
                        mainHandler.sendMessage(msg);
                    }
                });
    }

    private void saveTp() {

        if (mDefaultTransponder == null) {
            log.D("onPause() defaultTransponder == null !");
            mDefaultTransponder = new TuningParam();
        }
        if (mCurrentSearchType == SEARCH_TYPE_AUTO && !isAdvanced) {
//            mDefaultTransponder.setFrequency(DefaultParameter.DefaultTpValue.FREQUENCY);
//            mDefaultTransponder.setModulation(DefaultParameter.DefaultTpValue.MODULATION);
//            mDefaultTransponder.setSymbolRate(DefaultParameter.DefaultTpValue.SYMBOL_RATE);
            TransponderUtil.saveDefaultTransponer(
                    this,
                    DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_AUTO,
                    mDefaultTransponder);
        }
        if (mCurrentSearchType == SEARCH_TYPE_MANUAL) {
            TransponderUtil.saveDefaultTransponer(
                    this,
                    DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_MANUAL,
                    mDefaultTransponder);
        }
        if (mCurrentSearchType == SEARCH_TYPE_FULL) {
            TransponderUtil.saveDefaultTransponer(
                    this,
                    DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_MAINTP,
                    mDefaultTransponder);
        }
    }

    /**
     * 键盘锁 用于频道搜索过程中 不允许操作部分按键： 左右键，返回键
     */
    private boolean mSearchModeKey;
    private Button mAdvancedButton;
    private TextView mSearchTitle;
    private boolean isNoChannel;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_BACK:
                if (mSearchModeKey) {
                    log.D("dispatchKeyEvent in search processing so can not move !");
                    return true;
                }
                
        }
        return super.dispatchKeyEvent(event);
    }

    private void initView() {
        log.D("inintViews()");
        mSearchButton = (Button) findViewById(R.id.bt_start_search);
        mAdvancedButton = (Button) findViewById(R.id.bt_advanced_search);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgress_text = (TextView) findViewById(R.id.search_progress);
        mCurrent_frequency = (TextView) findViewById(R.id.current_frequency);
        mCurrent_strong_text = (TextView) findViewById(R.id.current_strong_text);
        mCurrent_quality_text = (TextView) findViewById(R.id.current_quality_text);
        mChannelListView = (ListView) findViewById(R.id.channel_list);
        mChannel_count = (TextView) findViewById(R.id.channel_count);
        mSearchTitle = (TextView) findViewById(R.id.tv_fast_search_main);
    }

    private void setupView() {
        log.D("setupViews()");
        channels=new ArrayList<stChannel>();
        mSearchButton.setOnClickListener(this);
        mAdvancedButton.setOnClickListener(this);
        mChannelListView.setFocusable(false);
        mChannelListView.setFocusableInTouchMode(false);
        mChannelListView.setItemsCanFocus(false);
        
        mChannelListAdapter = new ChannelSearchedAdapter(this,
                getLayoutInflater());
        
        mChannelListView.setAdapter(mChannelListAdapter);
        log.D("search type " + mCurrentSearchType);
        switch (mCurrentSearchType) {
            case SearchActivity.SEARCH_TYPE_FULL:
                mDefaultTransponder = TransponderUtil.getDefaultTransponder(this,
                        DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_MAINTP);
                break;
            case SearchActivity.SEARCH_TYPE_AUTO:
                mDefaultTransponder = TransponderUtil.getDefaultTransponder(this,
                        DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_AUTO);
                break;
            case SearchActivity.SEARCH_TYPE_MANUAL:
                mDefaultTransponder = TransponderUtil.getDefaultTransponder(this,
                        DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_MANUAL);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        Log.d("onClick", "mButton");
        switch (v.getId()) {
            case R.id.bt_start_search:
                if (mSearchButton.getText().toString().equals(
                        getResources().getString(R.string.channel_search_startsearchtext))
                        || mSearchButton.getText().toString().equals(
                                getResources().getString(R.string.channel_search_researchtext))) {
                    
                    clearAll();
                    
                    mSearchButton.setText(R.string.channel_search_stopsearchtext);
                    
                    // start search
                    workHandler.sendEmptyMessage(WorkHandlerMsg.START_SEARCH);
                    
                } else {// stop search
                    isCompleted = false;
                    workHandler.sendEmptyMessage(WorkHandlerMsg.STOP_SEARCH);
//                    mSearchButton.setText(getResources().getString(
//                            R.string.channel_search_startsearchtext));
                }
                break;
            case R.id.bt_advanced_search:
                Intent anvancedSettingIntent = new Intent(
                        SearchMainActivity.this,
                        SearchAdvancedSettingActivity.class);
                if (mCurrentSearchType == SEARCH_TYPE_FULL) {
                    anvancedSettingIntent.putExtra(SearchAdvancedSettingActivity.SEARCHTYPE,
                            SearchMainActivity.FULLSEARCH);
                }
                startActivityForResult(anvancedSettingIntent, AUTOSEARCH_REQUESTCODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (SearchAdvancedSettingActivity.AUTOSEARCH_RESPONSECODE == resultCode) {
            Bundle tpExtras = data.getExtras();
            if (tpExtras != null) {
                log.D("tpExtras is not null ");
                mDefaultTransponder.setFrequency(tpExtras
                        .getInt(SearchAdvancedSettingActivity.FREQUENCY));
                mDefaultTransponder.setModulation(tpExtras
                        .getInt(SearchAdvancedSettingActivity.MODULATION));
                mDefaultTransponder.setSymbolRate(tpExtras
                        .getInt(SearchAdvancedSettingActivity.SYMBOLRATE));
                isAdvanced = true;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 清除搜索结果的显示
     */
    private void clearAll() {
        // clear channel name list
        new Handler().post(new Runnable() {
            
            public void run() {
                mChannelListAdapter.clear();
                mChannelListAdapter.notifyDataSetChanged();
            }
        });
        // clear channel count.
        mProgressBar.setProgress(0);
        mProgress_text.setText("" + 0 + "%");
        mChannel_count.setText("" + 0);
    }

    private void stopSearch() {
        
        if (mBoundService != null) {
            mBoundService.stopSearch();
        }
    }

    private void doUnbindService() {
        // Detach our existing connection.
        unbindService(mConnection);
    }

    @Override
    protected void onPause() {
        super.onPause();
        
        // saveTp();
        
        this.removeAllHandlerMessages();
    }

    private void removeAllHandlerMessages() {
        mainHandler.removeMessages(MainHandlerMsg.CHANNEL_SEARCH_RESULT_PROGRESS);
        mainHandler.removeMessages(MainHandlerMsg.CHANNEL_SEARCH_RESULT_FREQUENCY);
        mainHandler.removeMessages(MainHandlerMsg.CHANNEL_SEARCH_RESULT_NAME_LIST);
        mainHandler.removeMessages(MainHandlerMsg.CHANNEL_SEARCH_RESULT_END);
        mainHandler.removeMessages(MainHandlerMsg.DIALOG_ALERT_DISMISS);
        
        workHandler.removeMessages(WorkHandlerMsg.START_SEARCH);
        workHandler.removeMessages(WorkHandlerMsg.STOP_SEARCH);
        workHandler.removeMessages(WorkHandlerMsg.SAVE_DB);
    }

    @Override
    protected void onStop() {
        doUnbindService();
        super.onStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
                /*AlertDialog.Builder exitSearchBuilder = new Builder(this,
                        AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                exitSearchBuilder.setTitle(R.string.search_dialog_title);
                exitSearchBuilder.setMessage(R.string.search_dialog_exit_message);
                
                exitSearchBuilder.setPositiveButton(R.string.search_dialog_confirm, new DialogInterface.OnClickListener() {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                exitSearchBuilder.setNegativeButton(R.string.search_dialog_cancel, new DialogInterface.OnClickListener() {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                exitSearchBuilder.setOnKeyListener(new OnKeyListener() {
                    
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            return true;
                        }
                        return false;
                    }
                }).create().show();*/
                finish();
            }
        return super.onKeyDown(keyCode, event);
    }

    private CustomProgressDialog progressDialog;

    /**
     * 显示圆形进度提示框
     */
    public void startProgressDialog(){
        if (progressDialog == null){
            progressDialog = CustomProgressDialog.createDialog(SearchMainActivity.this);
        }
        progressDialog.show();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        
        if(progressDialog != null){
            progressDialog.dismiss();
        }
        
    }

    
}
