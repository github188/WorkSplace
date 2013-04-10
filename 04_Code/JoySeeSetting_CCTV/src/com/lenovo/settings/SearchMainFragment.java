package com.lenovo.settings;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.joysee.adtv.aidl.ISearchService;
import com.joysee.adtv.aidl.OnSearchEndListener;
import com.joysee.adtv.aidl.OnSearchNewTransponderListener;
import com.joysee.adtv.aidl.OnSearchProgressChangeListener;
import com.joysee.adtv.aidl.OnSearchReceivedNewChannelsListener;
import com.joysee.adtv.aidl.OnSearchTunerSignalStateListener;
import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.adtv.logic.bean.Transponder;
import com.joysee.adtv.logic.bean.TunerSignal;
import com.lenovo.settings.Util.DefaultParameter;
import com.lenovo.settings.Util.TransponderUtil;
import com.lenovo.settings.adapter.ChannelSearchedAdapter;


public class SearchMainFragment extends Fragment implements OnClickListener {

    private View mMainView;
    private Button mSearchButton;
    private Button mAdvancedButton;
    private ProgressBar mProgressBar;
    private TextView mProgress_text;
    private TextView mCurrent_frequency;
    private TextView mCurrent_strong_text;
    private TextView mCurrent_quality_text;
    private TextView mTotalChannelCountTextView;
    private ListView mChannelListView;
    private TextView mSearchTitle;
//    private TextView mTvChannelCountTextView;
//    private TextView mBcChannelCountTextView;
    private ChannelSearchedAdapter mChannelListAdapter;
    private int mCurrentSearchType;
    private Transponder mDefaultTransponder;
    private boolean isCompleted;
    protected int mTvCount;
    protected int mBcCount;
    protected boolean mSearchModeKey;
    private int mProgress;
    private Handler workHandler;
    private HandlerThread workThread = new HandlerThread(
            "fast search work thread");
    private ISearchService mBoundService;
    protected String mChannelFrequency;
//    private List<ServiceType> mServiceTypes;
    protected int SEARCH_END = 0;
    private int SEARH_STOP = 1;
    private long mCurrentKeyTime;
    private long mLastKeyTime;
    
    private boolean isStopSearching;
    private boolean isStarted;
    private boolean isAuto = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	if(getArguments()!=null){
    		isAuto = getArguments().getBoolean("isCCTVSearch");
    		Log.d("songwenxuan","onCreateView() isAuto = " +isAuto);
    		mCurrentSearchType = 2;
    	}
        mMainView = inflater.inflate(R.layout.auto_search, container, false);
        initView();
        setupView();
        mSettingFragment = new SettingFragment(getActivity());
        mSearchButton.requestFocus();
        return mMainView;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (!workThread.isAlive()) {
            startWorkThread();
        }
        doBindService();
        isStopSearching = false;
        Log.d("songwenxuan", "onResume dobindservice000000000000000000000");
        if(isAuto){
        	Log.d("songwenxuan","isAuto = " + isAuto);
        	workHandler.sendEmptyMessageDelayed(WorkHandlerMsg.START_SEARCH, 600);
        	mSearchButton.setText(R.string.channel_search_stopsearchtext);
        }
    }
    
    private void doBindService() {
        Log.d("songwenxuan","dobindService999999999999");
        Log.d("songwenxuan",getActivity().getComponentName().getClassName());
        Intent intent = new Intent();
        intent.setAction("com.joysee.adtv.aidl.search");
        boolean isBind = getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        Log.d("songwenxuan","isBind = "+isBind);
    }

    private void setupView() {
//        channels = new ArrayList<stChannel>();
//        mServiceTypes = new ArrayList<ServiceType>();
        mSearchButton.setOnClickListener(this);
        mAdvancedButton.setOnClickListener(this);
        mChannelListView.setFocusable(false);
        mChannelListView.setFocusableInTouchMode(false);
        mChannelListView.setItemsCanFocus(false);
        
        mChannelListAdapter = new ChannelSearchedAdapter(getActivity(),getActivity().getLayoutInflater());
        mChannelListView.setAdapter(mChannelListAdapter);
        if(mCurrentSearchType == SearchMenuFragment.AUTOSEARCH){
            mDefaultTransponder = TransponderUtil.getTransponderFromXml(getActivity(),
                    DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_AUTO);
            mSearchTitle.setText(R.string.fast_search_main);
        }else{
            mDefaultTransponder = TransponderUtil.getTransponderFromXml(getActivity(),
                    DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_ALL);
            mSearchTitle.setText(R.string.full_search_title);
        }
        mSearchButton.setOnKeyListener(new OnKeyListener() {
            
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                    case KeyEvent.KEYCODE_DPAD_UP:
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                    case KeyEvent.KEYCODE_BACK:
                        if (mSearchModeKey) {
                            return true;
                        }else {
                            mCurrentKeyTime = System.currentTimeMillis();
                            if(mCurrentKeyTime - mLastKeyTime < 700){
                                return true;
                            }
                        }
                        break;
                    case KeyEvent.KEYCODE_HOME:
                        Log.d("songwenxuan","search main fragment Home down");
                        mSearchModeKey = false;
                        clearAll();
                        stopSearch();
                        getActivity().getFragmentManager().popBackStack();
                        break;
                }
                return false;
            }
        });
    }

    private void initView() {
        mSearchButton = (Button) mMainView.findViewById(R.id.bt_start_search);
        mAdvancedButton = (Button) mMainView.findViewById(R.id.bt_advanced_search);
        mProgressBar = (ProgressBar) mMainView.findViewById(R.id.progress_bar);
        mProgress_text = (TextView) mMainView.findViewById(R.id.search_progress);
        mCurrent_frequency = (TextView) mMainView.findViewById(R.id.current_frequency);
        mCurrent_strong_text = (TextView) mMainView.findViewById(R.id.current_strong_text);
        mCurrent_quality_text = (TextView) mMainView.findViewById(R.id.current_quality_text);
        mChannelListView = (ListView) mMainView.findViewById(R.id.channel_list);
        mTotalChannelCountTextView = (TextView) mMainView.findViewById(R.id.channel_count);
        mSearchTitle = (TextView) mMainView.findViewById(R.id.tv_fast_search_main);
//        mTvChannelCountTextView = (TextView) mMainView.findViewById(R.id.tv_channel_count);
//        mBcChannelCountTextView = (TextView) mMainView.findViewById(R.id.bc_channel_count);
    }
    public int getSearchType() {
        return mCurrentSearchType;
    }

    public void setSearchType(int searchType) {
        this.mCurrentSearchType = searchType;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_start_search:
                mCurrentKeyTime = System.currentTimeMillis();
                if(mCurrentKeyTime - mLastKeyTime <700){
//                    mLastKeyTime = System.currentTimeMillis();
                    return;
                }
                if (mSearchButton.getText().toString().equals(
                        getResources().getString(R.string.channel_search_startsearchtext))
                        || mSearchButton.getText().toString().equals(
                                getResources().getString(R.string.channel_search_researchtext))) {
                    clearAll();
                    if(mBoundService == null){
                        return;
                    }
                    // start search
                    workHandler.sendEmptyMessage(WorkHandlerMsg.START_SEARCH);
                    mSearchButton.setText(R.string.channel_search_stopsearchtext);
                    
                } else {
                    // stop search
                	if(isStarted){
	                	if(isStopSearching)
	                		return;
	                    isCompleted = false;
	                    mSearchModeKey = false;
	//                    workHandler.sendEmptyMessage(WorkHandlerMsg.STOP_SEARCH);
	                    stopSearch();
	                    mSearchButton.setText(R.string.search_stoping);
	                    isStopSearching = true;
                	}
                }
                break;
            case R.id.bt_advanced_search:
                SearchAdvancedSettingFragment searchAdvancedSettingFragment  = new SearchAdvancedSettingFragment();
                searchAdvancedSettingFragment.setSearchType(mCurrentSearchType);
                mSettingFragment.setFragment(searchAdvancedSettingFragment, true);
                isAuto = false;
                break;
        }
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
        mProgress = 0;
        mProgressBar.setProgress(0);
        mProgress_text.setText("" + 0 + "%");
        mTotalChannelCountTextView.setText("" + 0);
//        mTvChannelCountTextView.setText("" + 0);
//        mBcChannelCountTextView.setText("" + 0);
        mCurrent_frequency.setText("");
        mCurrent_strong_text.setText("");
        mCurrent_quality_text.setText("");
    }
    /**
     * UI主线程的Handler
     */
    private Handler mainHandler = new Handler() {
        
        @Override
        public void handleMessage(final Message msg) {
            
            switch (msg.what) {
                case MainHandlerMsg.CHANNEL_SEARCH_RESULT_STOP:
                	isStopSearching = false;
            		mSearchButton.setText(R.string.channel_search_researchtext);
                 // 关闭搜索模式键，此后不拦截按键
                    mSearchModeKey = false;
                    mSearchButton.setText(R.string.channel_search_researchtext);
                    if (getActivity() != null && !getActivity().isFinishing()) {
                        showSearchEndAlertDialog(SEARH_STOP);
                        mTvCount = 0;
                        mBcCount = 0;
                    }
                    break;
                case MainHandlerMsg.CHANNEL_SEARCH_RESULT_PROGRESS:
                	Log.d("songwenxuan","CHANNEL_SEARCH_RESULT_PROGRESS");
                    // progress bar
                    mProgressBar.setProgress(msg.arg2);
                    // progress %
                    mProgress_text.setText("" + msg.arg2 + "%");
                    mProgress = msg.arg2;
                    break;
                case MainHandlerMsg.CHANNEL_SEARCH_RESULT_FREQUENCY:
                	mCurrent_frequency.setText(msg.arg2+"Khz");
                    break;
                case MainHandlerMsg.CHANNEL_SEARCH_RESULT_TUNERSIGNAL_STATE:
                    TunerSignal signal = (TunerSignal) msg.obj;
                    Log.d("songwenxuan","signal.getCN() = " + signal.getCN() + "signal.getLevel() = " + signal.getLevel());
                    if (signal.getLevel()> 90 || signal.getLevel()<0) {
                    	mCurrent_strong_text.setText("0%");
                    } else if ((signal.getLevel()) == 0) {
                    	mCurrent_strong_text.setText("");
                    } else {
                    	mCurrent_strong_text.setText("" + signal.getLevel() + "%");
                    }
                    if (signal.getCN() > 90 || signal.getCN()<0) {
                    	mCurrent_quality_text.setText("0%");
                    } else if (signal.getCN() == 0) {
                    	mCurrent_quality_text.setText("");
                    } else {
                    	mCurrent_quality_text.setText("" + signal.getCN() + "%");
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
//                    	mTvChannelCountTextView.setText("" + mTvCount);
//                        mBcChannelCountTextView.setText("" + mBcCount);
                		mChannelListAdapter.add(service.getChannelName(), service.getServiceType(), String.valueOf(service.getFrequency()));
                		mChannelListView.setAdapter(mChannelListAdapter);
                		mChannelListAdapter.notifyDataSetChanged();
                    	mTotalChannelCountTextView.setText("" + mChannelListAdapter.getCount());
                    }
                    
                    
                    break;
                case MainHandlerMsg.CHANNEL_SEARCH_RESULT_END:
                    
                    // 关闭搜索模式键，此后不拦截按键
                    mSearchModeKey = false;
                    isAuto = false;
                    mSearchButton.setText(R.string.channel_search_researchtext);
                    
                    // 如果Activity退出了就不显示对话框了，否则异常
                    if (!getActivity().isFinishing()) {
                        showSearchEndAlertDialog(SEARCH_END);
//                        mTvCount = 0;
//                        mBcCount = 0;
                    }
                    break;
//                case MainHandlerMsg.DIALOG_ALERT_DISMISS:
//                    getActivity().dismissDialog(msg.arg1);
//                    break;
            }
            
            super.handleMessage(msg);
        }
    };
    /**
     * 主线程消息集合
     */
    private static class MainHandlerMsg {
    	public static final int CHANNEL_SEARCH_RESULT_PROGRESS = 1;
        public static final int CHANNEL_SEARCH_RESULT_FREQUENCY = 2;
        public static final int CHANNEL_SEARCH_RESULT_NAME_LIST = 3;
        public static final int CHANNEL_SEARCH_RESULT_END = 4;
        public static final int CHANNEL_SEARCH_RESULT_STOP = 5;
        public static final int CHANNEL_SEARCH_RESULT_TUNERSIGNAL_STATE = 6;
    }
    private void startWorkThread() {
        workThread.start();
        
        workHandler = new Handler(workThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
//                try {
                    switch (msg.what) {
                        case WorkHandlerMsg.START_SEARCH:
                            startSearch();
                            
                            break;
                        case WorkHandlerMsg.STOP_SEARCH:
                            
                            stopSearch();
                            
                            break;
                        case WorkHandlerMsg.SAVE_DB:
//                          saveChannels(channels);
//                            mBoundService.saveChannels(channels,mServiceTypes);
                            Intent intent = new Intent();
                            intent.setClassName("com.joysee.adtv", "com.joysee.adtv.activity.DvbMainActivity");
//                            getActivity().sendBroadcast(new Intent(DefaultParameter.FINISH_SEARCH));
//                            if(progressDialog != null){
//                                progressDialog.dismiss();
//                            }
                            getActivity().startActivity(intent);
                            getActivity().finish();
                            break;
                    }
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }
                super.handleMessage(msg);
            }
        };
    }
    public static class WorkHandlerMsg {
        public static final int START_SEARCH = 1001;
        public static final int STOP_SEARCH = 1002;
        
        /** 保存搜索的频道到数据库 */
        public static final int SAVE_DB = 1003;
    }
    
//    private CustomProgressDialog progressDialog;
//    /**
//     * 显示圆形进度提示框
//     */
//    public void startProgressDialog(){
//        if (progressDialog == null){
//            progressDialog = CustomProgressDialog.createDialog(getActivity());
//        }
//        progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
//            @Override
//            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//                if(keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE){
//                    return true;
//                }
//                return false;
//            }
//        });
//        progressDialog.show();
//    }
    
    private OnSearchEndListener.Stub mSearchEndBinder = new OnSearchEndListener.Stub() {
        @Override
        public void onSearchEnd(List<DvbService> services) throws RemoteException {
            Log.d("onSearchEnd()", "onSearchEnd()");
            if(isCompleted && mProgress>80){
            	mainHandler.sendEmptyMessage(MainHandlerMsg.CHANNEL_SEARCH_RESULT_END);
            }else{
            	mainHandler.sendEmptyMessage(MainHandlerMsg.CHANNEL_SEARCH_RESULT_STOP);
            }
        }
    };
    
    private OnSearchReceivedNewChannelsListener.Stub mSearchReceivedChannelsBinder = new OnSearchReceivedNewChannelsListener.Stub() {
        @Override
        public boolean onSearchReceivedNewChannelsListener(List<DvbService> services) throws RemoteException {
//        	log.D("on find new Channel");
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
    
    /**
     * Start to search
     */
    private void startSearch() {
    	isStarted = true;
    	isCompleted = true;
        mSearchModeKey = true;
        cancelSaveChannel();
//        getActivity().sendBroadcast(new Intent(DefaultParameter.START_SEARCH));
        if(mBoundService==null){
            Log.d("songwenxuan","mBoundService is null!!!!!!!!!!");
        }
        try {
        	mBoundService.setOnSearchEndListener(mSearchEndBinder);
        	mBoundService.setOnSearchReceivedNewChannelsListener(mSearchReceivedChannelsBinder);
        	mBoundService.setOnSearchNewTransponder(mSearchNewTpBinder);
        	mBoundService.setOnSearchProgressChange(mSearchProgressBinder);
        	mBoundService.setOnSearchTunerSignalStateListener(mSearchTunerSignalBinder);
        	mBoundService.startSearch(mCurrentSearchType, mDefaultTransponder);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    /**
     * 取消刚才保存的频道
     */
    private void cancelSaveChannel() {
        
        if (mBoundService != null) {
            try {
                mBoundService.deleteChannels();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
    private void stopSearch() {
        clearAll();
        if (mBoundService != null) {
            try {
                mBoundService.stopSearch(false);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mSearchModeKey = false;
        isStarted = false;
        isAuto = false;
    }
    
    
    @Override
    public void onPause() {
    	if(mBoundService != null){
            try {
                mBoundService.stopSearch(false);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        doUnbindService();
        isAuto = false;
        if(getArguments()!= null){
        	getArguments().clear();
        }
    	super.onPause();
    }
    
    private void doUnbindService() {
        getActivity().unbindService(mConnection);
    }
    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d("songwenxuan","onServiceConnected..........");
            mBoundService = ISearchService.Stub.asInterface(service);
            if(mBoundService==null){
                Log.d("songwenxuan","service is null!!!!!!!!!!!!!1");
            }
        }
        public void onServiceDisconnected(ComponentName className) {
            try {
            	mBoundService.setOnSearchEndListener(null);
            	mBoundService.setOnSearchNewTransponder(null);
            	mBoundService.setOnSearchReceivedNewChannelsListener(null);
            	mBoundService.setOnSearchProgressChange(null);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mBoundService = null;
            isAuto = false;
        }
    };
//    private List<stChannel> channels;
    private SettingFragment mSettingFragment;
    private Dialog mAlertDialog;
    private void showSearchEndAlertDialog(int type){
        mLastKeyTime = System.currentTimeMillis();
        View epgNotifyView = getActivity().getLayoutInflater().inflate(
                R.layout.search_alert_dialog_layout, null);
        Button confirmBtn = (Button) epgNotifyView.findViewById(R.id.epg_alert_confirm_btn);
        Button cancleBtn = (Button) epgNotifyView.findViewById(R.id.epg_alert_cancle_btn);
        TextView titleTextView = (TextView) epgNotifyView.findViewById(R.id.epg_alert_title);
//        if(channels.size() == 0){
//            titleTextView.setText(R.string.search_no_channel);
//        }else if(type == SEARCH_END){
//            titleTextView.setText(R.string.search_completed);
//        }else {
//            titleTextView.setText(R.string.search_stop_save_ask);
//        }
        if (mTvCount == 0 && mBcCount == 0) {
            titleTextView.setText(R.string.search_no_channel);
        } else if (type == SEARCH_END) {
            titleTextView.setText(R.string.search_completed);
        }
        confirmBtn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
//                if(channels.size() == 0){
//                    clearAll();
//                    mAlertDialog.dismiss();
//                }
//            else {
            	if (mTvCount == 0 && mBcCount == 0) {
            		Log.d("songwenxuan","mTvCount == 0 && mBcCount == 0");
                    clearAll();
                    mAlertDialog.dismiss();
                } else {
                	Log.d("songwenxuan"," ! mTvCount == 0 && mBcCount == 0");
                	Intent intent = new Intent();
                    intent.setClassName("com.joysee.adtv", "com.joysee.adtv.activity.DvbMainActivity");
//                    getActivity().sendBroadcast(new Intent(DefaultParameter.FINISH_SEARCH));
//                    if(progressDialog != null){
//                        progressDialog.dismiss();
//                    }
                    getActivity().startActivity(intent);
                    mAlertDialog.dismiss();
                    getActivity().finish();
                    mTvCount = 0;
                    mBcCount = 0;
//                    startProgressDialog();
                }
            }
        });
        confirmBtn.setOnKeyListener(dialogOnkeyListener);
            
        cancleBtn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                clearAll();
                mAlertDialog.dismiss();
            }
        });
        cancleBtn.setOnKeyListener(dialogOnkeyListener);
        if(mAlertDialog == null){
            mAlertDialog = new Dialog(getActivity(),R.style.config_text_dialog);
        }
        int width = (int) getResources().getDimension(R.dimen.ca_worktime_pwd_dialog_width);
        int height = (int) getResources().getDimension(R.dimen.ca_worktime_pwd_dialog_height);
        mAlertDialog.setContentView(epgNotifyView,new LayoutParams(width, height));
        mAlertDialog.show();
    }
    OnKeyListener dialogOnkeyListener = new OnKeyListener() {
        
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if(keyCode == KeyEvent.KEYCODE_HOME){
                Log.d("songwenxuan","SearchMainFragment AlertDialog HOME down");
                mAlertDialog.dismiss();
            }
            if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
                mAlertDialog.dismiss();
                clearAll();
                return true;
            }
            return false;
        }
    };
}
