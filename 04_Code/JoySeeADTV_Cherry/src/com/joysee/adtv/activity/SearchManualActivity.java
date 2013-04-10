
package com.joysee.adtv.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
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
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.joysee.adtv.common.DefaultParameter.ModulationType;
import com.joysee.adtv.common.DvbLog;
import com.joysee.adtv.common.ToastUtil;
import com.joysee.adtv.common.TransponderUtil;
import com.joysee.adtv.db.Channel;
import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.adtv.logic.bean.Transponder;
import com.joysee.adtv.logic.bean.TunerSignal;
import com.joysee.adtv.ui.SearchEditText;
import com.joysee.adtv.ui.SearchEditText.OnInputDataErrorListener;
import com.joysee.adtv.ui.adapter.ChannelSearchedAdapter;

/**
 * 手动搜索界面
 * 
 * @author songwenxuan
 */
public class SearchManualActivity extends Activity implements OnClickListener {

    private static final DvbLog log = new DvbLog(
            "com.joysee.adtv.activity.SearchManualActivity", DvbLog.DebugType.D);

    public static final String FULLSEARCH = "full";
    public static final String SEARCHTYPE = "searchType";
    public static final String MANUALSEARCH = "manual";
    public static class WorkHandlerMsg {
        public static final int START_SEARCH = 1001;
        public static final int STOP_SEARCH = 1002;
    }
    /**
     * 主线程消息集合
     */
    private static class MainHandlerMsg {
        public static final int CHANNEL_SEARCH_RESULT_PROGRESS = 1;
        public static final int CHANNEL_SEARCH_RESULT_FREQUENCY = 2;
        public static final int CHANNEL_SEARCH_RESULT_NAME_LIST = 3;
        public static final int CHANNEL_SEARCH_RESULT_END = 4;
        public static final int CHANNEL_SEARCH_RESULT_TUNERSIGNAL_STATE = 6;
    }

    private ChannelSearchedAdapter mChannelListAdapter;
    private TextView mProgressTextView;
    private TextView mCurrentFrequencyTextView;
    private TextView mCurrentStrongTextView;
    private TextView mCurrentQualityTextView;
    private ListView mChannelListView;
    private ISearchService mSearchService;
    private Transponder mTransponder;
    private Button mSearchButton;
    private Handler workHandler;
//    private TextView mChannelCountTextView;
    private ImageView mProgressImageView;
	private LinearLayout mProgressLayout;
	private LinearLayout mQamLinearLayout;
	private TextView mQamTextView;
	private ImageView mFocusView;
	private ImageView mQamImageview;
	private TextView mLastTextView;
    private Dialog mAlertDialog;
    private Dialog mModulationDialog;
    private boolean mSearchModeKey;
    private HandlerThread workThread = new HandlerThread("fast search work thread");
    /** 搜索调制方式 */
    /** 搜索频率 */
    private SearchEditText mFrequencyEditText;
    /** 搜索符号率 */
    private SearchEditText mSymbolRateEditText;
    public static final int AUTOSEARCH_REQUESTCODE = 2001;
    private int mCurrentSearchType = SearchMainActivity.SEARCH_TYPE_MANUAL;
    
    private boolean isHaveChanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log.D("SearchHandActivity create.");
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.search_manual_search_layout);
//        FrameLayout hand_search_bg = (FrameLayout) findViewById(R.id.hand_search_bg);
//        if (getThemePaper() != null) {
//            hand_search_bg.setBackgroundDrawable(getThemePaper());
//        }
        initView();
        setupView();
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
        mSearchButton.requestFocus();
    }

    private void doBindService() {
    	Log.d("songwenxuan","manual enter doBindService()");
        Intent intent = new Intent();
        intent.setAction("com.joysee.adtv.aidl.search");
        boolean isBind = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        log.D("manual isBind = " + isBind);
        Log.d("songwenxuan","manual leave doBindService()");
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            mSearchService = ISearchService.Stub.asInterface(service);
            log.D("mConnection onServiceConnected mBoundService = " + mSearchService.toString());
        }

        public void onServiceDisconnected(ComponentName className) {
            try {
                mSearchService.setOnSearchEndListener(null);
                mSearchService.setOnSearchReceivedNewChannelsListener(null);
                mSearchService.setOnSearchNewTransponder(null);
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
                case MainHandlerMsg.CHANNEL_SEARCH_RESULT_PROGRESS:
                    log.D("search progress = " + msg.arg2);
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mProgressImageView.getLayoutParams();
                    layoutParams.width = (int)((msg.arg2/100f) * mProgressLayout.getWidth());
                    mProgressImageView.setLayoutParams(layoutParams);
                    mProgressImageView.invalidate();
                    mProgressTextView.setText("" + msg.arg2 + "%");
                    break;
                case MainHandlerMsg.CHANNEL_SEARCH_RESULT_FREQUENCY:
                    log.D("search frequency = " + msg.arg2 / 1000 + "MHz");
                    mCurrentFrequencyTextView.setText("" + msg.arg2 / 1000 + "MHz");
                    break;
                case MainHandlerMsg.CHANNEL_SEARCH_RESULT_TUNERSIGNAL_STATE:
                    TunerSignal signal = (TunerSignal) msg.obj;
                    if ((signal.getCN() * 100 / 255) > 90) {
                        mCurrentStrongTextView.setText("90%");
                    } else if ((signal.getCN() * 100 / 255) == 0) {
                        mCurrentStrongTextView.setText("");
                    } else {
                        mCurrentStrongTextView.setText("" + signal.getCN() * 100 / 255 + "%");
                    }
                    if ((signal.getLevel() * 100 / 255) > 90) {
                        mCurrentQualityTextView.setText("90%");
                    } else if ((signal.getLevel() * 100 / 255) == 0) {
                        mCurrentQualityTextView.setText("");
                    } else {
                        mCurrentQualityTextView.setText("" + signal.getLevel() * 100 / 255 + "%");
                    }
                	break;
                case MainHandlerMsg.CHANNEL_SEARCH_RESULT_NAME_LIST:
                    if (msg.obj == null) {
                        break;
                    }
					@SuppressWarnings("unchecked")
					ArrayList<DvbService> services = (ArrayList<DvbService>)msg.obj;
                    for(DvbService service : services){
                    	mChannelListAdapter.add(service.getChannelName(), service.getServiceType(), String.valueOf(service.getFrequency()));
                    	mChannelListView.setAdapter(mChannelListAdapter);
                    	mChannelListAdapter.notifyDataSetChanged();
                    }
//                    mChannelCountTextView.setText("" + mChannelListAdapter.getCount());
                    break;
                case MainHandlerMsg.CHANNEL_SEARCH_RESULT_END:
                	Log.d("songwenxuan","CHANNEL_SEARCH_RESULT_END");
                    // 关闭搜索模式键，此后不拦截按键
                    mSearchModeKey = false;
                    // 如果Activity退出了就不显示对话框了，否则异常
                    if (!isFinishing()) {
                        showSearchEndAlertDialog();
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
    private void startSearch() {
        mSearchModeKey = true;
        log.D("tp:" + mTransponder.toString() + "search type is " + mCurrentSearchType);
        try {
        	mSearchService.setOnSearchEndListener(mSearchEndBinder);
        	mSearchService.setOnSearchReceivedNewChannelsListener(mSearchFindNewChBinder);
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
        public void onSearchEnd(List<DvbService> channels) throws RemoteException {
            Log.d("onSearchEnd()", "onSearchEnd()");
            mainHandler.sendEmptyMessage(MainHandlerMsg.CHANNEL_SEARCH_RESULT_END);
        }
    };
    
    private OnSearchReceivedNewChannelsListener.Stub mSearchFindNewChBinder = new OnSearchReceivedNewChannelsListener.Stub() {
		@Override
		public boolean onSearchReceivedNewChannelsListener(
				List<DvbService> services) throws RemoteException {
			if(services.size() > 0){
				isHaveChanel = true;
			}else {
				isHaveChanel = false;
			}
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
                finish();
                break;
        }
        return super.dispatchKeyEvent(event);
    }
    private void initView() {
        log.D("inintViews()");
        mSearchButton = (Button) findViewById(R.id.bt_start_search);
        mProgressTextView = (TextView) findViewById(R.id.search_progress);
        mCurrentFrequencyTextView = (TextView) findViewById(R.id.current_frequency);
        mCurrentStrongTextView = (TextView) findViewById(R.id.current_strong_text);
        mCurrentQualityTextView = (TextView) findViewById(R.id.current_quality_text);
        mChannelListView = (ListView) findViewById(R.id.channel_list);
//        mChannelCountTextView = (TextView) findViewById(R.id.channel_count);
        mFrequencyEditText = (SearchEditText) this.findViewById(R.id.frequency_edit);
        mSymbolRateEditText = (SearchEditText) this.findViewById(R.id.symbol_rate_edit);
        mProgressLayout = (LinearLayout) findViewById(R.id.progress_linear_layout);
        mProgressImageView = (ImageView) findViewById(R.id.progress_imageview);
        
        mQamLinearLayout = (LinearLayout) findViewById(R.id.search_settings_qam_linear);
        mQamTextView = (TextView) findViewById(R.id.search_settings_qam_textview);
        mQamImageview = (ImageView) findViewById(R.id.search_settings_qam_imageview);
    }

    private void setFocus(boolean bol) {
        mFrequencyEditText.setFocusable(bol);
        mSymbolRateEditText.setFocusable(bol);
        mQamLinearLayout.setFocusable(bol);
        mSearchButton.setFocusable(bol);
    }

    private void setupView() {
        log.D("setupViews()");
        mSearchButton.setOnClickListener(this);
        mChannelListView.setFocusable(false);
        mChannelListView.setFocusableInTouchMode(false);
        mChannelListView.setItemsCanFocus(false);
        mChannelListAdapter = new ChannelSearchedAdapter(this,
                getLayoutInflater());
        mChannelListView.setAdapter(mChannelListAdapter);
        mQamLinearLayout.setOnFocusChangeListener(onQamLinearFocusChangeListener);
        mQamLinearLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				alert();
			}
		});
        mTransponder = TransponderUtil.getTransponderFromXml(this,
                DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_MANUAL);
        mFrequencyEditText.setText("" + mTransponder.getFrequency() / 1000);
        mSymbolRateEditText.setText("" + mTransponder.getSymbolRate());
        switch (mTransponder.getModulation()) {
            case DefaultParameter.ModulationType.MODULATION_64QAM:
            	mQamTextView.setText(R.string.search_64);
                break;
            case DefaultParameter.ModulationType.MODULATION_128QAM:
            	mQamTextView.setText(R.string.search_128);
                break;
            case DefaultParameter.ModulationType.MODULATION_256QAM:
            	mQamTextView.setText(R.string.search_256);
                break;
        }
        mFrequencyEditText.setRange(
                DefaultParameter.SearchParameterRange.FREQUENCY_MIN,
                DefaultParameter.SearchParameterRange.FREQUENCY_MAX);
        mSymbolRateEditText.setRange(
                DefaultParameter.SearchParameterRange.SYMBOLRATE_MIN,
                DefaultParameter.SearchParameterRange.SYMBOLRATE_MAX);
        mFrequencyEditText.setOnInputDataErrorListener(new OnInputDataErrorListener() {
            public void onInputDataError(int errorType) {
                switch (errorType) {
                    case SearchEditText.INPUT_DATA_ERROR_TYPE_NULL:
                        // 弹出对话框提示输入错误。
                        ToastUtil.showToast(SearchManualActivity.this, R.string.search_frequency_null);
                        mFrequencyEditText.setText("" + mTransponder.getFrequency() / 1000);
                        break;
                    case SearchEditText.INPUT_DATA_ERROR_TYPE_OUT:
                        ToastUtil.showToast(SearchManualActivity.this, R.string.search_frequency_out);
                        mFrequencyEditText.setText("" + mTransponder.getFrequency() / 1000);
                        break;
                    case SearchEditText.INPUT_DATA_ERROR_TYPE_NORMAL:
                        break;
                }
            }
        });
        mSymbolRateEditText.setText("" + mTransponder.getSymbolRate());
        mSymbolRateEditText.setOnInputDataErrorListener(new OnInputDataErrorListener() {
            public void onInputDataError(int errorType) {
                switch (errorType) {
                    case SearchEditText.INPUT_DATA_ERROR_TYPE_NULL:
                        ToastUtil.showToast(SearchManualActivity.this,
                                R.string.search_symbolrate_null);
                        mSymbolRateEditText.setText("" + mTransponder.getSymbolRate());
                        break;
                    case SearchEditText.INPUT_DATA_ERROR_TYPE_OUT:
                        ToastUtil.showToast(SearchManualActivity.this, R.string.search_symbolrate_out);
                        mSymbolRateEditText.setText("" + mTransponder.getSymbolRate());
                        break;
                    case SearchEditText.INPUT_DATA_ERROR_TYPE_NORMAL:
                        break;
                }
            }
        });
    }

    private void getTransponder() {
        String frequency = mFrequencyEditText.getText().toString();
        String symbolRate = mSymbolRateEditText.getText().toString();
        String modulation = mQamTextView.getText().toString();
        mTransponder.setFrequency(Integer.parseInt(frequency) * 1000);
        mTransponder.setSymbolRate(Integer.parseInt(symbolRate));
        switch (Integer.parseInt(modulation)) {
		case 64:
			mTransponder.setModulation(ModulationType.MODULATION_64QAM);
			break;
		case 128:
			mTransponder.setModulation(ModulationType.MODULATION_128QAM);
			break;
		case 256:
			mTransponder.setModulation(ModulationType.MODULATION_256QAM);
			break;
		default:
			break;
		}
    }

    @Override
    public void onClick(View v) {
        Log.d("onClick", "mButton");
        switch (v.getId()) {
            case R.id.bt_start_search:
                if (mSearchButton.getText().toString().equals(
                        getResources().getString(R.string.search_start_search))
                        || mSearchButton.getText().toString().equals(
                                getResources().getString(R.string.search_research))) {
                    getTransponder();
                    clearAll();
                    if (mSearchService == null) {
                        return;
                    }
                    workHandler.sendEmptyMessage(WorkHandlerMsg.START_SEARCH);
                    setFocus(false);
                }
                break;
        }
    }
    /**
     * 清除搜索结果的显示
     */
    private void clearAll() {
        mChannelListAdapter.clear();
        mChannelListAdapter.notifyDataSetChanged();
        mProgressTextView.setText("" + 0 + "%");
//        mChannelCountTextView.setText("" + 0);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mProgressImageView.getLayoutParams();
        layoutParams.width = 0;
        mProgressImageView.setLayoutParams(layoutParams);
        mProgressImageView.invalidate();
        mCurrentFrequencyTextView.setText("");
        mCurrentStrongTextView.setText("");
        mCurrentQualityTextView.setText("");
    }
    private void stopSearch() {
    	clearAll();
        if (mSearchService != null) {
            try {
                mSearchService.stopSearch(false);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
    private void doUnbindService() {
    	Log.d("songwenxuan","manual doUnbindService");
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
        workHandler.removeMessages(WorkHandlerMsg.STOP_SEARCH);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
    /**
     * 取setting背景图，用于设置成这个activity的背景
     * 
     * @return
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
    private void showSearchEndAlertDialog() {
    	getContentResolver().delete(Channel.URI.TABLE_RESERVES, null, null);
        View alertDialogView = this.getLayoutInflater().inflate(
                R.layout.alert_dialog_include_button_layout, null);
        Button confirmBtn = (Button) alertDialogView.findViewById(R.id.confirm_btn);
        Button cancleBtn = (Button) alertDialogView.findViewById(R.id.cancle_btn);
        TextView titleTextView = (TextView) alertDialogView.findViewById(R.id.epg_alert_title);
        if (!isHaveChanel) {
            titleTextView.setText(R.string.search_no_channel);
        } else {
            titleTextView.setText(R.string.search_completed);
        }
        confirmBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                log.D("onClick to save db");
                if (!isHaveChanel) {
                    clearAll();
                    mAlertDialog.dismiss();
                    setFocus(true);
                    mSearchButton.requestFocus();
                } else {
                	ChannelTypeNumUtil.savePlayChannel(
                            getApplicationContext(),
                            DefaultParameter.ServiceType.BC,
                            1
                            );
                    // 再将电视频道号设为0，并且最终频道类型是电视
                    ChannelTypeNumUtil.savePlayChannel(
                            getApplicationContext(),
                            DefaultParameter.ServiceType.TV,
                            1
                            );
                	Intent intent = new Intent();
                	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_NEW_TASK);
                	intent.setClass(SearchManualActivity.this, DvbMainActivity.class);
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
                setFocus(true);
                mSearchButton.requestFocus();
            }
        });
        confirmBtn.setOnKeyListener(dialogOnkeyListener);
        cancleBtn.setOnKeyListener(dialogOnkeyListener);
        if (mAlertDialog == null) {
            mAlertDialog = new Dialog(this, R.style.alertDialogTheme);
        }
        final int windowHeight = (int) getResources()
                .getDimension(R.dimen.alert_dialog_include_button_height);
        final int windowWidth = (int) getResources().getDimension(R.dimen.alert_dialog_include_button_width);
        
        mAlertDialog.setContentView(alertDialogView, new android.view.ViewGroup.LayoutParams(windowWidth, windowHeight));
        mAlertDialog.show();
    }
    private OnKeyListener dialogOnkeyListener = new OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_HOME) {
                Log.d("songwenxuan", "SearchManualFragment AlertDialog HOME down");
                clearAll();
                mAlertDialog.dismiss();
            }
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                clearAll();
                setFocus(true);
                mAlertDialog.dismiss();
            }
            return false;
        }
    };
    
    public void alert() {
    	
		mQamLinearLayout.setBackgroundResource(R.drawable.search_et_normal);
		mQamTextView.setTextColor(getResources().getColor(R.color.search_main_text));
		mQamTextView.setPadding((int)getResources().getDimension(R.dimen.search_down_textview_padding), 0, 0, 0);
		mQamImageview.setImageResource(R.drawable.search_settings_arrows_unfocus);
		View view = LayoutInflater.from(this).inflate(
				R.layout.search_down_list_layout, null);
        mFocusView = (ImageView)view.findViewById(R.id.ivFocus);
		ListView downListView = (ListView) view.findViewById(R.id.search_down_listview);
		Integer[] qams = {64,128,256};
		ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this, R.layout.search_down_list_item, R.id.search_down_list_textview, qams);
		downListView.setAdapter(adapter);
		if (mModulationDialog == null) {
			mModulationDialog = new Dialog(this, R.style.searchDownListTheme);
		}
		
		downListView.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if(mLastTextView != null)
					mLastTextView.setTextColor(getResources().getColor(R.color.search_main_text));
				TextView textView = (TextView)view;
				textView.setTextColor(getResources().getColor(R.color.search_text_green));
				int [] location = new int [2];
				view.getLocationInWindow(location);
				mFocusView.setVisibility(View.VISIBLE);
				if(location[1] == 0)
					return;
				MarginLayoutParams params = (MarginLayoutParams) mFocusView.getLayoutParams();
				params.topMargin = location[1];
				
				Log.d("songwenxuan","onFocusChange() , params.topMargin = " + params.topMargin);
				mFocusView.setLayoutParams(params);
				
				Animation anim = new AlphaAnimation(0.0f, 1.0f);
				anim.setDuration(300);
				anim.setFillAfter(true);
				anim.setFillEnabled(true);
				mFocusView.startAnimation(anim);
				mLastTextView = textView;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		});
		
		downListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mQamLinearLayout.setBackgroundResource(R.drawable.search_et_selector);
				mQamTextView.setPadding((int)getResources().getDimension(R.dimen.search_down_textview_padding), 0, 0, 0);
				mQamTextView.setTextColor(getResources().getColor(R.color.search_text_green));
				mQamImageview.setImageResource(R.drawable.search_settings_arrows_focus);
				switch (position) {
				case 0:
					mQamTextView.setText(R.string.search_64);
					mModulationDialog.dismiss();
					break;
				case 1:
					mQamTextView.setText(R.string.search_128);
					mModulationDialog.dismiss();
					break;
				case 2:
					mQamTextView.setText(R.string.search_256);
					mModulationDialog.dismiss();
					break;
				default:
					break;
				}
			}
		});
		
		mModulationDialog.setContentView(view);
		Window window = mModulationDialog.getWindow();
		LayoutParams params = new LayoutParams();
		int [] location = new int [2]; 
		mQamTextView.getLocationInWindow(location);
		int height = mQamTextView.getHeight();
		
		Log.d("songwenxuan","location[0] = " + location[0] + "  location[1] = " + location[1]);
		Log.d("songwenxuan","height = " + height);
		
//		Display display = getWindowManager().getDefaultDisplay();
		//dialog的零点
		int x = (int)getResources().getDimension(R.dimen.screen_width)/2;
		int y = (int)getResources().getDimension(R.dimen.screen_height)/2;
		params.width = mQamLinearLayout.getWidth()-2;
		params.height = (int)getResources().getDimension(R.dimen.search_down_list_height);;
		params.dimAmount = 0.4f;
		params.flags = LayoutParams.FLAG_DIM_BEHIND;
		params.x = location[0] - x + params.width/2;
		params.y = location[1] + height -y + params.height/2;
		window.setAttributes(params);
		mModulationDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if(event.getKeyCode() == KeyEvent.KEYCODE_ESCAPE || event.getKeyCode() == KeyEvent.KEYCODE_BACK){
					mQamLinearLayout.setBackgroundResource(R.drawable.search_et_selector);
					mQamTextView.setTextColor(getResources().getColor(R.color.search_text_green));
					mQamImageview.setImageResource(R.drawable.search_settings_arrows_focus);
					mQamTextView.setPadding((int)getResources().getDimension(R.dimen.search_down_textview_padding), 0, 0, 0);
					mModulationDialog.dismiss();
					return true;
				}
				return false;
			}
		});
		mModulationDialog.show();
	}
    
    OnFocusChangeListener onQamLinearFocusChangeListener = new OnFocusChangeListener() {
		
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if(hasFocus){
				TextView textview = (TextView) v.findViewById(R.id.search_settings_qam_textview);
				ImageView imageView = (ImageView) findViewById(R.id.search_settings_qam_imageview);
				textview.setTextColor(getResources().getColor(R.color.search_text_green));
				imageView.setImageResource(R.drawable.search_settings_arrows_focus);
			}else{
				TextView textview = (TextView) v.findViewById(R.id.search_settings_qam_textview);
				ImageView imageView = (ImageView) findViewById(R.id.search_settings_qam_imageview);
				textview.setTextColor(getResources().getColor(R.color.search_main_text));
				imageView.setImageResource(R.drawable.search_settings_arrows_unfocus);
			}
		}
	};
}
