
package com.joysee.adtv.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.aidl.ISearchService;
import com.joysee.adtv.aidl.OnSearchEndListener;
import com.joysee.adtv.aidl.OnSearchNewTransponderListener;
import com.joysee.adtv.aidl.OnSearchProgressChangeListener;
import com.joysee.adtv.aidl.OnSearchReceivedNewChannelsListener;
import com.joysee.adtv.aidl.OnSearchTunerSignalStateListener;
import com.joysee.adtv.common.ChannelTypeNumUtil;
import com.joysee.adtv.common.DefaultParameter;
import com.joysee.adtv.common.DvbLog;
import com.joysee.adtv.common.TransponderUtil;
import com.joysee.adtv.db.Channel;
import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.adtv.logic.bean.Transponder;
import com.joysee.adtv.logic.bean.TunerSignal;
import com.joysee.adtv.ui.CustomProgressDialog;
import com.joysee.adtv.ui.adapter.ChannelSearchedAdapter;

/**
 * 快速搜索和全频搜索Activity.
 * 
 * @author songwenxuan
 */
public class SearchMainActivity extends Activity implements OnClickListener {

    private static final DvbLog log = new DvbLog(
            "com.joysee.adtv.activity.SearchMainActivity", DvbLog.DebugType.D);
    public static final String ALL_SEARCH = "full";
    public static final String MANUAL_SEARCH = "manual";
    public static final String SEARCH_TYPE = "searchType";
    public static final String SEARCH_SYMBOLRATE = "symbolrate";
    private ChannelSearchedAdapter mChannelListAdapter;
//    private ProgressBar mSearchProgressBar;
    private int mProgress;
    private TextView mProgressTextView;
    private TextView mCurrentStrongTextView;
    private TextView mCurrentQualityTextView;
    private TextView mSearchTitleTextView;
    private TextView mTotalChannelCountTextView;
    private ListView mChannelListView;
    private Button mAdvancedButton;
    private Button mSearchButton;
    private boolean mSearchModeKey;
    private boolean isCompleted = true;
    private long mCurrentKeyDownTime;
    private long mLastKeyDownTime;
    private CustomProgressDialog mSearchCustomProgressDialog;
    private ISearchService mSearchService;
    private Transponder mTransponder;
    private Handler workHandler;
    private Dialog mAlertDialog;
	private ImageView mProgressImageView;
	private LinearLayout progress_layout;
    private static final int SEARCH_END = 0;
    private HandlerThread workThread = new HandlerThread(
            "fast search work thread");
    /** 手动搜索 */
    public static final int SEARCH_TYPE_MANUAL = 0;
    /** 全频搜索 */
    public static final int SEARCH_TYPE_ALL = 1;
    /** 快速搜索 */
    public static final int SEARCH_TYPE_FAST = 2;
    private int mCurrentSearchType = SEARCH_TYPE_FAST;
    private int mTvCount;
    private int mBcCount;
    private boolean isFromNotify;
    public static final int AUTOSEARCH_REQUESTCODE = 2001;
    private boolean isStopSearching;
    private boolean isStarted;

    /**
     * 主线程消息
     */
    private static class MainHandlerMsg {
        public static final int CHANNEL_SEARCH_RESULT_PROGRESS = 1;
        public static final int CHANNEL_SEARCH_RESULT_FREQUENCY = 2;
        public static final int CHANNEL_SEARCH_RESULT_NAME_LIST = 3;
        public static final int CHANNEL_SEARCH_RESULT_END = 4;
        public static final int CHANNEL_SEARCH_RESULT_STOP = 5;
        public static final int CHANNEL_SEARCH_RESULT_TUNERSIGNAL_STATE = 6;
        public static final int CHANNEL_SEARCH_RESULT_STOPED = 7;
    }

    /**
     * 工作线程消息
     */
    public static class WorkHandlerMsg {
        public static final int START_SEARCH = 1001;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log.D("AutoSearchActivity create.");
        setContentView(R.layout.search_fast_search_layout);
        initViews();
        setupSearchType();
        setupView();
    }

    /** 设置默认的搜索类型 */
    private void setupSearchType() {
        isFromNotify = getIntent().getBooleanExtra(DefaultParameter.TVNOTIFY_TO_SEARCH, false);
        String searchType = getIntent().getStringExtra(SEARCH_TYPE);
        if (ALL_SEARCH.equals(searchType)) {
            log.D("full search");
            mCurrentSearchType = SEARCH_TYPE_ALL;
            mSearchTitleTextView.setText(R.string.search_full_search_title);
            mAdvancedButton.setVisibility(View.INVISIBLE);
        } else {
            mCurrentSearchType = SEARCH_TYPE_FAST;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        log.D("onResume()");
        setTransponder();
        if (!workThread.isAlive()) {
            startWorkThread();
            log.D("start workThread!!");
        }
        doBindService();
        String searchType = getIntent().getStringExtra(SearchMainActivity.SEARCH_TYPE);
        if (ALL_SEARCH.equals(searchType)) {
        	mAdvancedButton.setVisibility(View.INVISIBLE);
        	mSearchButton.setText(R.string.search_stop_search);
        	workHandler.sendEmptyMessageDelayed(WorkHandlerMsg.START_SEARCH, 300);
        }
        if (isFromNotify) {
            mAdvancedButton.setVisibility(View.INVISIBLE);
            mSearchButton.setVisibility(View.INVISIBLE);
            workHandler.sendEmptyMessageDelayed(WorkHandlerMsg.START_SEARCH, 300);
        }
        isStopSearching = false;
    }
    
    private void setTransponder(){
        if(mCurrentSearchType == SearchMainActivity.SEARCH_TYPE_ALL){
            mTransponder = TransponderUtil.getTransponderFromXml(
                    this,
                    DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_ALL
                    );
        }else if(mCurrentSearchType == SearchMainActivity.SEARCH_TYPE_FAST){
            mTransponder = TransponderUtil.getTransponderFromXml(
                    this,
                    DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_AUTO
                    );
        }
    }

    private void doBindService() {
    	Log.d("songwenxuan","fast enter doBindService()");
        Intent intent = new Intent();
        intent.setAction("com.joysee.adtv.aidl.search");
        boolean isBind = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        Log.d("songwenxuan","fast isBind = "+isBind);
        Log.d("songwenxuan","fast enter doBindService()");
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            mSearchService = ISearchService.Stub.asInterface(service);
            log.D("mConnection onServiceConnected mBoundService = " + mSearchService.toString());
        }

        public void onServiceDisconnected(ComponentName className) {
            try {
                mSearchService.setOnSearchEndListener(null);
                mSearchService.setOnSearchNewTransponder(null);
                mSearchService.setOnSearchReceivedNewChannelsListener(null);
                mSearchService.setOnSearchProgressChange(null);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mSearchService = null;
            log.D("mConnection onServiceDisconnected");
        }
    };

    /**
     * UI主线程的Handler
     */
	private Handler mainHandler = new Handler() {

        @Override
        public void handleMessage(final Message msg) {

            switch (msg.what) {
            	case MainHandlerMsg.CHANNEL_SEARCH_RESULT_STOPED:
            		isStopSearching = false;
            		mSearchButton.setText(R.string.search_research);
            		break;
                case MainHandlerMsg.CHANNEL_SEARCH_RESULT_STOP:
                    // 关闭搜索模式键，此后不拦截按键
                    mSearchModeKey = false;
                    mSearchButton.setText(R.string.search_research);
                    if (!isFinishing()) {
                        mTvCount = 0;
                        mBcCount = 0;
                    }
                    break;
                case MainHandlerMsg.CHANNEL_SEARCH_RESULT_PROGRESS:
                    log.D("search progress = " + msg.arg2);
//                    mSearchProgressBar.setProgress(msg.arg2);
                    LayoutParams layoutParams = (LayoutParams) mProgressImageView.getLayoutParams();
                    layoutParams.width = (int)((msg.arg2/100f) * progress_layout.getWidth());
                    mProgressImageView.setLayoutParams(layoutParams);
                    mProgressImageView.invalidate();
                    mProgress = msg.arg2;
                    mProgressTextView.setText("" + msg.arg2 + "%");
                    break;
                case MainHandlerMsg.CHANNEL_SEARCH_RESULT_FREQUENCY:
                    log.D("search frequency = " + msg.arg2 / 1000 + "MHz");
                    break;
                case MainHandlerMsg.CHANNEL_SEARCH_RESULT_TUNERSIGNAL_STATE:
                    TunerSignal signal = (TunerSignal) msg.obj;
                    if (signal.getLevel() > 90) {
                        mCurrentStrongTextView.setText("0%");
                    } else if (signal.getLevel() == 0) {
                        mCurrentStrongTextView.setText("");
                    } else {
                        mCurrentStrongTextView.setText("" + signal.getLevel() * 100 / 255 + "%");
                    }
                    if (signal.getCN() > 90) {
                        mCurrentQualityTextView.setText("0%");
                    } else if (signal.getCN() == 0) {
                        mCurrentQualityTextView.setText("");
                    } else {
                        mCurrentQualityTextView.setText("" + signal.getCN() * 100 / 255 + "%");
                    }
                	break;
                case MainHandlerMsg.CHANNEL_SEARCH_RESULT_NAME_LIST:
                    if (msg.obj == null) {
                        break;
                    }
					@SuppressWarnings("unchecked")
					ArrayList<DvbService> services = (ArrayList<DvbService>)msg.obj;
                    for(DvbService service : services){
                    	if (service.getServiceType() == DefaultParameter.ServiceType.TV) {
                    		mTvCount++;
                    	} else if (service.getServiceType() == DefaultParameter.ServiceType.BC) {
                    		mBcCount++;
                    	}
                    	log.D("receive new channel ,CHANNEL_SEARCH_RESULT_NAME_LIST : " + service.toString());
                		mChannelListAdapter.add(service.getChannelName(), service.getServiceType(), String.valueOf(service.getFrequency()));
                		mChannelListView.setAdapter(mChannelListAdapter);
                		mChannelListAdapter.notifyDataSetChanged();
                    	mTotalChannelCountTextView.setText("" + mChannelListAdapter.getCount());
                    }
                    break;
                case MainHandlerMsg.CHANNEL_SEARCH_RESULT_END:
                    // 关闭搜索模式键，此后不拦截按键
                    mSearchModeKey = false;
                    if (isFromNotify) {//由于监控，发现表变化，启动搜索，强制性的,不弹出提示框。
                        //TODO 存储频道，底层实现，要接口。
//                        workHandler.sendEmptyMessage(WorkHandlerMsg.SAVE_DB);
//                        startProgressDialog();
                        return;
                    }
                    mSearchButton.setText(R.string.search_research);
                    if (!isFinishing()) {
                        showSearchEndAlertDialog(SEARCH_END);
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

	private void startWorkThread() {
        workThread.start();
        workHandler = new Handler(workThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case WorkHandlerMsg.START_SEARCH:
                        log.D("开始搜索" + mTransponder.toString());
                        startSearch();
                        break;
                    default:
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    /**
     * Start to search
     */
    private void startSearch() {
    	isStarted = true;
    	isCompleted = true;
        mSearchModeKey = true;
        Log.d("wgh1", "startSearch");
        log.D("transponder:" + mTransponder.toString() + "search type is " + mCurrentSearchType);
        try {
        	mSearchService.setOnSearchEndListener(mSearchEndBinder);
        	mSearchService.setOnSearchReceivedNewChannelsListener(mSearchReceivedChannelsBinder);
        	mSearchService.setOnSearchNewTransponder(mSearchNewTpBinder);
        	mSearchService.setOnSearchProgressChange(mSearchProgressBinder);
        	mSearchService.setOnSearchTunerSignalStateListener(mSearchTunerSignalBinder);
            mSearchService.startSearch(mCurrentSearchType, mTransponder);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    
    private OnSearchEndListener.Stub mSearchEndBinder = new OnSearchEndListener.Stub() {
        @Override
        public void onSearchEnd(List<DvbService> services) throws RemoteException {
            Log.d("wgh1", "onSearchEnd()");
            if(isCompleted && mProgress>80){
            	mainHandler.sendEmptyMessage(MainHandlerMsg.CHANNEL_SEARCH_RESULT_END);
            }else{
            	mainHandler.sendEmptyMessage(MainHandlerMsg.CHANNEL_SEARCH_RESULT_STOPED);
            }
            
        }
    };
    
    private OnSearchReceivedNewChannelsListener.Stub mSearchReceivedChannelsBinder = new OnSearchReceivedNewChannelsListener.Stub() {
        @Override
        public boolean onSearchReceivedNewChannelsListener(List<DvbService> services) throws RemoteException {
        	log.D("on find new Channel");
            Message msg = Message.obtain();
            msg.what = MainHandlerMsg.CHANNEL_SEARCH_RESULT_NAME_LIST;
            msg.obj = services;
            mainHandler.sendMessage(msg);
            return true;
        }
    };
    
    private OnSearchNewTransponderListener.Stub mSearchNewTpBinder = new OnSearchNewTransponderListener.Stub() {
        @Override
        public void onSearchNewTransponder(int frequency)
                throws RemoteException {
            Message msg = Message.obtain();
            msg.what = MainHandlerMsg.CHANNEL_SEARCH_RESULT_FREQUENCY;
            msg.arg2 = frequency;
            mainHandler.sendMessage(msg);
        }
    };
    
    private OnSearchProgressChangeListener.Stub mSearchProgressBinder = new OnSearchProgressChangeListener.Stub() {
        @Override
        public void onSearchProgressChanged(int progress) throws RemoteException {
            Message msg = Message.obtain();
            msg.what = MainHandlerMsg.CHANNEL_SEARCH_RESULT_PROGRESS;
            msg.arg2 = progress;
            mainHandler.sendMessage(msg);
        }
    };
    
    private OnSearchTunerSignalStateListener.Stub mSearchTunerSignalBinder = new OnSearchTunerSignalStateListener.Stub() {
		
		@Override
		public void onSearchTunerSignalState(TunerSignal tunerSignal)
				throws RemoteException {
			Message msg = Message.obtain();
            msg.what = MainHandlerMsg.CHANNEL_SEARCH_RESULT_TUNERSIGNAL_STATE;
            msg.obj = tunerSignal;
            mainHandler.sendMessage(msg);
		}
	};

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
                break;
            case KeyEvent.KEYCODE_HOME:
                mSearchModeKey = false;
                clearAll();
                stopSearch(false);
                finish();
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    private void initViews() {
        log.D("inintViews()");
        mSearchButton = (Button) findViewById(R.id.search_start_search_button);
        mAdvancedButton = (Button) findViewById(R.id.search_advanced_option_button);
//        mSearchProgressBar = (ProgressBar) findViewById(R.id.search_progress_bar);
        mProgressTextView = (TextView) findViewById(R.id.search_progress_textview);
        mCurrentStrongTextView = (TextView) findViewById(R.id.search_current_strength_textview);
        mCurrentQualityTextView = (TextView) findViewById(R.id.search_current_quality_textview);
        mTotalChannelCountTextView = (TextView) findViewById(R.id.search_all_channel_count);
        mSearchTitleTextView = (TextView) findViewById(R.id.search_fast_search_title);
        mChannelListView = (ListView) findViewById(R.id.search_channel_list);
        progress_layout = (LinearLayout) findViewById(R.id.progress_linear_layout);
        mProgressImageView = (ImageView) findViewById(R.id.progress_imageview);
    }

    private void setupView() {
        log.D("setupViews()");
        mSearchButton.setOnClickListener(this);
        mAdvancedButton.setOnClickListener(this);
        mChannelListView.setFocusable(false);
        mChannelListView.setFocusableInTouchMode(false);
        mChannelListView.setItemsCanFocus(false);
        mChannelListAdapter = new ChannelSearchedAdapter(this,
                getLayoutInflater());
        mChannelListView.setAdapter(mChannelListAdapter);
        log.D("search type " + mCurrentSearchType);
    }

    @Override
    public void onClick(View v) {
        Log.d("wgh1", " onClick " +isStopSearching);
        switch (v.getId()) {
            case R.id.search_start_search_button:
                mCurrentKeyDownTime = SystemClock.uptimeMillis();
                if (mCurrentKeyDownTime - mLastKeyDownTime < 700) {
                    return;
                }
                if (mSearchButton.getText().toString().equals(
                        getResources().getString(R.string.search_start_search))
                        || mSearchButton.getText().toString().equals(
                                getResources().getString(R.string.search_research))) {
                    clearAll();
                    if (mSearchService == null) {
                        return;
                    }
                    Log.d("wgh1", " onClick start");
                    workHandler.sendEmptyMessage(WorkHandlerMsg.START_SEARCH);
                    mSearchButton.setText(R.string.search_stop_search);
                } else {// stop search
                	Log.d("wgh1", " onClick stop");
                	if(isStarted){
	                	if(isStopSearching)
	                		return;
	                	isCompleted = false;
	                    mSearchModeKey = false;
	                    stopSearch(false);
	                    mSearchButton.setText(R.string.search_stoping);
	                    isStopSearching = true;
                	}
                }
                break;
            case R.id.search_advanced_option_button:
                Intent anvancedSettingIntent = new Intent(
                        SearchMainActivity.this,
                        SearchAdvancedOptionActivity.class);
                log.D("mCurrentSearchType = " + SEARCH_TYPE_ALL);
                if (mCurrentSearchType == SEARCH_TYPE_ALL) {
                    anvancedSettingIntent.putExtra(SearchAdvancedOptionActivity.SEARCHTYPE,
                            SearchMainActivity.ALL_SEARCH);
                }
                startActivity(anvancedSettingIntent);
            default:
                break;
        }
    }

    /**
     * 清除搜索结果的显示
     */
    private void clearAll() {
        // clear channel name list
        mChannelListAdapter.clear();
        mChannelListAdapter.notifyDataSetChanged();
//        mSearchProgressBar.setProgress(0);
        LayoutParams layoutParams = (LayoutParams) mProgressImageView.getLayoutParams();
        layoutParams.width = 0;
        mProgressImageView.setLayoutParams(layoutParams);
        mProgressImageView.invalidate();
        mProgress = 0;
        mProgressTextView.setText("" + 0 + "%");
        mTotalChannelCountTextView.setText("" + 0);
        mCurrentStrongTextView.setText("");
        mCurrentQualityTextView.setText("");
        mBcCount = 0;
        mTvCount = 0;
    }

    private void stopSearch(boolean isSave) {
    	clearAll();
        if (mSearchService != null) {
            try {
                mSearchService.stopSearch(isSave);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        isStarted = false;
    }

    private void doUnbindService() {
        unbindService(mConnection);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.removeAllHandlerMessages();
        doUnbindService();
    }

    private void removeAllHandlerMessages() {
        mainHandler.removeMessages(MainHandlerMsg.CHANNEL_SEARCH_RESULT_PROGRESS);
        mainHandler.removeMessages(MainHandlerMsg.CHANNEL_SEARCH_RESULT_FREQUENCY);
        mainHandler.removeMessages(MainHandlerMsg.CHANNEL_SEARCH_RESULT_NAME_LIST);
        mainHandler.removeMessages(MainHandlerMsg.CHANNEL_SEARCH_RESULT_END);
        workHandler.removeMessages(WorkHandlerMsg.START_SEARCH);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 显示圆形进度提示框
     */
    public void startProgressDialog() {
        if (mSearchCustomProgressDialog == null) {
            mSearchCustomProgressDialog = CustomProgressDialog.createDialog(SearchMainActivity.this);
        }
        mSearchCustomProgressDialog.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
                    return true;
                }
                return false;
            }
        });
        mSearchCustomProgressDialog.show();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mSearchCustomProgressDialog != null) {
            mSearchCustomProgressDialog.dismiss();
        }
    }

    /**
     * 取setting背景图，用于设置成这个activity的背景
     */
    public Drawable getThemePaper() {
        String url = Settings.System.getString(this.getContentResolver(), "settings.theme.url");
        if (url != null && url.length() > 0) {
            Bitmap bitmap = BitmapFactory.decodeFile(url);
            Drawable drawable = new BitmapDrawable(bitmap);
            return drawable;
        }
        return null;
    }

    private void showSearchEndAlertDialog(int type) {
    	getContentResolver().delete(Channel.URI.TABLE_RESERVES, null, null);
        mLastKeyDownTime = SystemClock.uptimeMillis();
        View alertDialogView = this.getLayoutInflater().inflate(
                R.layout.alert_dialog_include_button_layout, null);
        Button confirmBtn = (Button) alertDialogView.findViewById(R.id.confirm_btn);
        Button cancleBtn = (Button) alertDialogView.findViewById(R.id.cancle_btn);
        TextView titleTextView = (TextView) alertDialogView.findViewById(R.id.epg_alert_title);
        if (mTvCount == 0 && mBcCount == 0) {
            titleTextView.setText(R.string.search_no_channel);
        } else if (type == SEARCH_END) {
            titleTextView.setText(R.string.search_completed);
        }
        confirmBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                log.D("onClick to save db");
                if (mTvCount == 0 && mBcCount == 0) {
                    clearAll();
                    mAlertDialog.dismiss();
                } else {
//                	clearAll();
                	Log.d("songwenxuan","start activity");
                	ChannelTypeNumUtil.savePlayChannel(
                            getApplicationContext(),
                            DefaultParameter.ServiceType.BC,
                            0
                            );
                    // 再将电视频道号设为0，并且最终频道类型是电视
                    ChannelTypeNumUtil.savePlayChannel(
                            getApplicationContext(),
                            DefaultParameter.ServiceType.TV,
                            0
                            );
                	Intent intent = new Intent();
                	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_NEW_TASK);
                	intent.setClass(SearchMainActivity.this, DvbMainActivity.class);
                	startActivity(intent);
                    mAlertDialog.dismiss();
                }
            }
        });
        cancleBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAll();
                mAlertDialog.dismiss();
            }
        });
        if (mAlertDialog == null) {
            mAlertDialog = new Dialog(this, R.style.alertDialogTheme);
        }
        final int windowHeight = (int) getResources()
                .getDimension(R.dimen.alert_dialog_include_button_height);
        final int windowWidth = (int) getResources()
                .getDimension(R.dimen.alert_dialog_include_button_width);
        mAlertDialog.setContentView(
                alertDialogView, 
                new LayoutParams(windowWidth, windowHeight)
                );
        mAlertDialog.show();
    }
}
