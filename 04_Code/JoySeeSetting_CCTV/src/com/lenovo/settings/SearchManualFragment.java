package com.lenovo.settings;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.app.Fragment;
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
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.lenovo.settings.Util.AdapterViewSelectionUtil;
import com.lenovo.settings.Util.DefaultParameter;
import com.lenovo.settings.Util.TransponderUtil;
import com.lenovo.settings.adapter.ChannelSearchedAdapter;
import com.lenovo.settings.view.MyEditText;
import com.lenovo.settings.view.MyEditText.OnInputDataErrorListener;


public class SearchManualFragment extends Fragment implements OnClickListener {

    private View mainView;
    private Button mSearchButton;
    private Button mAdvancedButton;
    private ProgressBar mProgressBar;
    private TextView mProgress_text;
    private TextView mCurrent_frequency;
    private TextView mCurrent_strong_text;
    private TextView mCurrent_quality_text;
    private ListView mChannelListView;
    private TextView mSearchTitle;
    private TextView mChannel_count;
    private MyEditText mFrequencyEditText;
    private MyEditText mSymbolRateEditText;
//    private Spinner mSpinner;
//    private List<stChannel> channels;
//    private List<ServiceType> mServiceTypes;
    private Transponder mDefaultTransponder;
    private HandlerThread workThread = new HandlerThread("manual search work thread");
    private Handler workHandler;
    protected boolean mSearchModeKey;
    private ISearchService mBoundService;
    private boolean isHaveChanel;
    protected String mChannelFrequency;
    private ChannelSearchedAdapter mChannelListAdapter;
    private int mCurrentSearchType;
    private boolean isBack;
    protected int SEARCH_END = 1;
    private long mCurrentKeyTime;
    private long mLastKeyTime;
    private LinearLayout mQamLinearLayout;
//	private TextView mLastTextView;
	private ImageView mQamImageview;
	private Dialog mSearchEndDialog;
    
    public static class WorkHandlerMsg {
        public static final int START_SEARCH = 1001;
        public static final int STOP_SEARCH = 1002;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.hand_search, container,false);
        initView();
        setupView();
        mSearchButton.requestFocus();
        return mainView;
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

    private void setupView() {
//        channels=new ArrayList<stChannel>();
//        mServiceTypes = new ArrayList<ServiceType>();
        mSearchButton.setOnClickListener(this);
        mAdvancedButton.setOnClickListener(this);
        mChannelListView.setFocusable(false);
        mChannelListView.setFocusableInTouchMode(false);
        mChannelListView.setItemsCanFocus(false);
        
        mChannelListAdapter = new ChannelSearchedAdapter(getActivity(),
                getActivity().getLayoutInflater());
        
        mChannelListView.setAdapter(mChannelListAdapter);
        
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.adjust_method, R.layout.search_spinner_button);
        adapter.setDropDownViewResource(R.layout.search_spinner_item);
//        mSpinner.setAdapter(adapter);
//        mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
//            
//                    public void onItemSelected(
//                            AdapterView<?> parent, View view, int position, long id) {
//                        switch(position){
//                            case 0://64
//                                mDefaultTransponder.setModulation(DefaultParameter.ModulationType.MODULATION_64QAM);
//                                break;
//                            case 1://128
//                                mDefaultTransponder.setModulation(DefaultParameter.ModulationType.MODULATION_128QAM);
//                                break;
//                            case 2://256
//                                mDefaultTransponder.setModulation(DefaultParameter.ModulationType.MODULATION_256QAM);
//                                break;
//                        }
//                    }
//                    public void onNothingSelected(AdapterView<?> parent) {
//                        
//                    }
//          });
        mDefaultTransponder = TransponderUtil.getTransponderFromXml(getActivity(),
                DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_MANUAL);
        mFrequencyEditText.setText("" + mDefaultTransponder.getFrequency() / 1000);
        mSymbolRateEditText.setText("" + mDefaultTransponder.getSymbolRate());
        switch (mDefaultTransponder.getModulation()) {
            case DefaultParameter.ModulationType.MODULATION_64QAM:
            	mQamTextView.setText(R.string.sixtyfour_qam);
//                mSpinner.setSelection(0);
                break;
            case DefaultParameter.ModulationType.MODULATION_128QAM:
            	mQamTextView.setText(R.string.onetwoeight_qam);
//                mSpinner.setSelection(1);
                break;
            case DefaultParameter.ModulationType.MODULATION_256QAM:
            	mQamTextView.setText(R.string.twofivesix_qam);
//                mSpinner.setSelection(2);
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
                    case MyEditText.INPUT_DATA_ERROR_TYPE_NULL:
                        // 弹出对话框提示输入错误。
                        AdapterViewSelectionUtil.showToast(getActivity(), R.string.frequency_null);
                        mFrequencyEditText.setText("" + mDefaultTransponder.getFrequency() / 1000);
                        break;
                    case MyEditText.INPUT_DATA_ERROR_TYPE_OUT:
                        AdapterViewSelectionUtil.showToast(getActivity(), R.string.frequency_out_of_range);
                        mFrequencyEditText.setText("" + mDefaultTransponder.getFrequency() / 1000);
                        break;
                    case MyEditText.INPUT_DATA_ERROR_TYPE_NORMAL:
                        break;
                }
            }
        });
        mSymbolRateEditText.setText("" + mDefaultTransponder.getSymbolRate());
        mSymbolRateEditText.setOnInputDataErrorListener(new OnInputDataErrorListener() {
            
            public void onInputDataError(int errorType) {
                switch (errorType) {
                    case MyEditText.INPUT_DATA_ERROR_TYPE_NULL:
                        AdapterViewSelectionUtil.showToast(getActivity(), R.string.symbol_rate_null);
                        mSymbolRateEditText.setText("" + mDefaultTransponder.getSymbolRate());
                        break;
                    case MyEditText.INPUT_DATA_ERROR_TYPE_OUT:
                        AdapterViewSelectionUtil.showToast(getActivity(), R.string.symbol_rate_out_of_range);
                        mSymbolRateEditText.setText("" + mDefaultTransponder.getSymbolRate());
                        break;
                    case MyEditText.INPUT_DATA_ERROR_TYPE_NORMAL:
                        break;
                }
            }
        });
        mSearchButton.setOnKeyListener(new View.OnKeyListener() {
            
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                    case KeyEvent.KEYCODE_DPAD_UP:
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                    case KeyEvent.KEYCODE_BACK:
                        if(mSearchModeKey){
                            return true;
                        }
                        break;
                    case KeyEvent.KEYCODE_HOME:
                        mSearchModeKey = false;
                        clearAll();
                        stopSearch();
                        getActivity().getFragmentManager().popBackStack();
                }
                return false;
            }
        });
        
        mFrequencyEditText.setOnFocusChangeListener(onFocusChangeListener);
        mSymbolRateEditText.setOnFocusChangeListener(onFocusChangeListener);
        mQamLinearLayout.setOnFocusChangeListener(onQamLinearFocusChangeListener);
        mQamLinearLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				alert(getActivity());
			}
		});
    }

    private void initView() {
        mSearchButton = (Button)mainView.findViewById(R.id.bt_start_search);
        mAdvancedButton = (Button) mainView.findViewById(R.id.bt_advanced_search);
        mProgressBar = (ProgressBar) mainView.findViewById(R.id.progress_bar);
        mProgress_text = (TextView) mainView.findViewById(R.id.search_progress);
        mCurrent_frequency = (TextView) mainView.findViewById(R.id.current_frequency);
        mCurrent_strong_text = (TextView) mainView.findViewById(R.id.current_strong_text);
        mCurrent_quality_text = (TextView) mainView.findViewById(R.id.current_quality_text);
        mChannelListView = (ListView) mainView.findViewById(R.id.channel_list);
        mChannel_count = (TextView) mainView.findViewById(R.id.channel_count);
        mSearchTitle = (TextView) mainView.findViewById(R.id.tv_fast_search_main);
        mFrequencyEditText = (MyEditText) mainView.findViewById(R.id.frequency_edit);
        mSymbolRateEditText = (MyEditText)mainView.findViewById(R.id.symbol_rate_edit);
        mFocusView = (ImageView) mainView.findViewById(R.id.ivFocus);
        
        mQamTextView = (TextView) mainView.findViewById(R.id.search_settings_qam_textview);
        
//      mTitleTextView = (TextView) mainView.findViewById(R.id.search_advanced_option_title);
        mQamLinearLayout = (LinearLayout) mainView.findViewById(R.id.search_settings_qam_linear);
        mQamImageview = (ImageView) mainView.findViewById(R.id.search_settings_qam_imageview);
//        mSpinner = (Spinner) mainView.findViewById(R.id.search_adjust_method);
    }
    
    private void stopSearch() {
        if (mBoundService != null) {
            try {
                mBoundService.stopSearch(false);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * UI主线程的Handler
     */
    private Handler mainHandler = new Handler() {
        
        @Override
        public void handleMessage(final Message msg) {
            
            switch (msg.what) {
//                case MainHandlerMsg.CHANNEL_SEARCH_RESULT_STOP:
//                    
//                 // 关闭搜索模式键，此后不拦截按键
//                    mSearchModeKey = false;
//                    mSearchButton.setText(R.string.channel_search_researchtext);
//                    if (!isBack) {
//                    }
//                    break;
                case MainHandlerMsg.CHANNEL_SEARCH_RESULT_PROGRESS:
                    // progress bar
                    mProgressBar.setProgress(msg.arg2);
                    // progress %
                    mProgress_text.setText("" + msg.arg2 + "%");
                    
                    break;
                case MainHandlerMsg.CHANNEL_SEARCH_RESULT_FREQUENCY:
                    mCurrent_frequency.setText("" + msg.arg2 / 1000 + "MHz");
//                    mChannelFrequency = "" + msg.arg2 / 1000 + "MHz";
//                    
//                    TunerSignal signal = (TunerSignal) msg.obj;
//                    if((signal.getCN()*100/255)>90){
//                        mCurrent_strong_text.setText("90%");
//                    }else if((signal.getCN()*100/255) == 0){
//                        mCurrent_strong_text.setText("");
//                    }else{
//                        mCurrent_strong_text.setText("" + signal.getCN()*100/255 + "%");
//                    }
//                    if((signal.getLevel()*100/255)>90){
//                        mCurrent_quality_text.setText("90%");
//                    }else if((signal.getLevel()*100/255) == 0){
//                        mCurrent_quality_text.setText("");
//                    }else {
//                        mCurrent_quality_text.setText("" + signal.getLevel()*100/255 + "%");
//                    }
                    break;
                case MainHandlerMsg.CHANNEL_SEARCH_RESULT_TUNERSIGNAL_STATE:
                    TunerSignal signal = (TunerSignal) msg.obj;
                    if ((signal.getCN() * 100 / 255) > 90) {
                    	mCurrent_strong_text.setText("90%");
                    } else if ((signal.getCN() * 100 / 255) == 0) {
                    	mCurrent_strong_text.setText("");
                    } else {
                    	mCurrent_strong_text.setText("" + signal.getCN() * 100 / 255 + "%");
                    }
                    if ((signal.getLevel() * 100 / 255) > 90) {
                    	mCurrent_quality_text.setText("90%");
                    } else if ((signal.getLevel() * 100 / 255) == 0) {
                    	mCurrent_quality_text.setText("");
                    } else {
                    	mCurrent_quality_text.setText("" + signal.getLevel() * 100 / 255 + "%");
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
                    break;
                case MainHandlerMsg.CHANNEL_SEARCH_RESULT_END:
                    // 关闭搜索模式键，此后不拦截按键
                	Log.d("songwenxuan","CHANNEL_SEARCH_RESULT_END");
                    // 关闭搜索模式键，此后不拦截按键
                    mSearchModeKey = false;
                    // 如果Activity退出了就不显示对话框了，否则异常
                    if (!getActivity().isFinishing()) {
                        showSearchEndAlertDialog();
                    }
                    break;
//                case MainHandlerMsg.DIALOG_ALERT_DISMISS:
//                    
//                    getActivity().dismissDialog(msg.arg1);
//                    
//                    break;
            }
            
            super.handleMessage(msg);
        }
        
    };
    protected boolean isCompleted;
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_start_search:
                if(mSearchModeKey){
                    return;
                }
                mCurrentKeyTime = System.currentTimeMillis();
                if(mCurrentKeyTime - mLastKeyTime <700){
                    mLastKeyTime = System.currentTimeMillis();
                    return;
                }
                if (mSearchButton.getText().toString().equals(
                        getResources().getString(R.string.channel_search_startsearchtext))
                        || mSearchButton.getText().toString().equals(
                                getResources().getString(R.string.channel_search_researchtext))) {
                    saveTp();
                    clearAll();
                    if(mBoundService == null){
                        return;
                    }
                    mSearchModeKey = true;
                    workHandler.sendEmptyMessage(WorkHandlerMsg.START_SEARCH);
                    isCompleted = true;
                    setFocus(false);
                } else {// stop search
//                    isCompleted = false;
//                    workHandler.sendEmptyMessage(WorkHandlerMsg.STOP_SEARCH);
                }
                break;
        }
    }
    
    private void saveTp(){
        String frequency = mFrequencyEditText.getText().toString();
        String symbolRate = mSymbolRateEditText.getText().toString();
        
        mDefaultTransponder.setFrequency(Integer.parseInt(frequency) * 1000);
        mDefaultTransponder.setSymbolRate(Integer.parseInt(symbolRate));
//        mDefaultTransponder.setModulation(modulation);
        TransponderUtil.saveTransponerToXml(
                getActivity(),
                DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_MANUAL,
                mDefaultTransponder);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (!workThread.isAlive()) {
            startWorkThread();
        }
        isBack = false;
        doBindService();
    }
    


    @Override
    public void onPause() {
        Log.d("songwenxuan","SearchManualFragment onPause()");
        isBack = true;
        doUnbindService();
        super.onPause();
    }

    private void doBindService() {
        Intent intent = new Intent();
        intent.setAction("com.joysee.adtv.aidl.search");
        boolean isBind = getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        Log.d("songwenxuan","isBind = "+isBind);
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
    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ISearchService.Stub.asInterface(service);
        }

        public void onServiceDisconnected(ComponentName className) {
            try {
            	mBoundService.setOnSearchEndListener(null);
            	mBoundService.setOnSearchReceivedNewChannelsListener(null);
            	mBoundService.setOnSearchNewTransponder(null);
            	mBoundService.setOnSearchProgressChange(null);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mBoundService = null;
        }
    };
    private void startSearch() {
        
        try {
        	mBoundService.setOnSearchEndListener(mSearchEndBinder);
        	mBoundService.setOnSearchReceivedNewChannelsListener(mSearchFindNewChBinder);
        	mBoundService.setOnSearchNewTransponder(mSearchNewTpBinder);
        	mBoundService.setOnSearchProgressChange(mSearchProgressBinder);
        	mBoundService.setOnSearchTunerSignalStateListener(mSearchTunerSignalBinder);
        	mBoundService.startSearch(mCurrentSearchType, mDefaultTransponder);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    private void startWorkThread() {
        workThread.start();
        
        workHandler = new Handler(workThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
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
     * 保存并退出
     */
    private void saveChannelAndExit(){
//        try {
//            mBoundService.saveChannels(channels,mServiceTypes);
//            getActivity().sendBroadcast(new Intent(DefaultParameter.FINISH_SEARCH));
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
        Intent intent = new Intent();
        intent.setClassName("com.joysee.adtv", "com.joysee.adtvactivity.DvbMainActivity");
        getActivity().startActivity(intent);
        getActivity().finish();
    }
    /**
     * 清除搜索结果的显示
     */
    private void clearAll() {
        new Handler().post(new Runnable() {
            
            public void run() {
                mChannelListAdapter.clear();
                mChannelListAdapter.notifyDataSetChanged();
            }
        });
        mProgressBar.setProgress(0);
        mProgress_text.setText("" + 0 + "%");
        mChannel_count.setText("" + 0);
        mCurrent_frequency.setText("");
        mCurrent_strong_text.setText("");
        mCurrent_quality_text.setText("");
    }
    private void setFocus(boolean bol){
        mFrequencyEditText.setFocusable(bol);
        mSymbolRateEditText.setFocusable(bol);
        if(bol){
        	mSearchButton.setBackgroundResource(R.drawable.button_selector);
        }else{
        	mSearchButton.setBackgroundResource(R.drawable.button_unfocus);
        }
//        mSpinner.setFocusable(bol);
//        mSearchButton.setFocusable(bol);
    }
    private void showSearchEndAlertDialog(){
        mLastKeyTime = System.currentTimeMillis();
        View epgNotifyView = getActivity().getLayoutInflater().inflate(
                R.layout.search_alert_dialog_layout, null);
        Button confirmBtn = (Button) epgNotifyView.findViewById(R.id.epg_alert_confirm_btn);
        Button cancleBtn = (Button) epgNotifyView.findViewById(R.id.epg_alert_cancle_btn);
        TextView titleTextView = (TextView) epgNotifyView.findViewById(R.id.epg_alert_title);
        if (!isHaveChanel) {
            titleTextView.setText(R.string.search_no_channel);
        } else {
            titleTextView.setText(R.string.search_completed);
        }
        confirmBtn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
            	if (!isHaveChanel) {
                    clearAll();
                    mSearchEndDialog.dismiss();
                    setFocus(true);
                    mSearchButton.requestFocus();
                }else{
                	Log.d("songwenxuan"," ! mTvCount == 0 && mBcCount == 0");
                	Intent intent = new Intent();
                    intent.setClassName("com.joysee.adtv", "com.joysee.adtv.activity.DvbMainActivity");
//                    getActivity().sendBroadcast(new Intent(DefaultParameter.FINISH_SEARCH));
//                    if(progressDialog != null){
//                        progressDialog.dismiss();
//                    }
                    getActivity().startActivity(intent);
                    mSearchEndDialog.dismiss();
                    getActivity().finish();
                }
            }
        });
        confirmBtn.setOnKeyListener(dialogOnkeyListener);
            
        cancleBtn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
//            	try {
//            		mBoundService.saveOldChannels(channels);
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }
                clearAll();
                setFocus(true);
                mSearchEndDialog.dismiss();
            }
        });
        cancleBtn.setOnKeyListener(dialogOnkeyListener);
        if(mSearchEndDialog == null){
        	mSearchEndDialog = new Dialog(getActivity(),R.style.config_text_dialog);
        }
        
        int width = (int) getResources().getDimension(R.dimen.ca_worktime_pwd_dialog_width);
        int height = (int) getResources().getDimension(R.dimen.ca_worktime_pwd_dialog_height);
        mSearchEndDialog.setContentView(epgNotifyView,new LinearLayout.LayoutParams(width, height));
        mSearchEndDialog.show();
    }
    View.OnKeyListener dialogOnkeyListener = new View.OnKeyListener() {
        
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if(keyCode == KeyEvent.KEYCODE_HOME){
                Log.d("songwenxuan","SearchManualFragment AlertDialog HOME down");
                clearAll();
                mAlertDialog.dismiss();
            }
            if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
                clearAll();
                setFocus(true);
                mAlertDialog.dismiss();
            }
            return false;
        }
    };
    private void doUnbindService() {
        getActivity().unbindService(mConnection);
    }
    
    private TextView mQamTextView;
	private Dialog mAlertDialog;
	private ImageView mFocusView;
	public void alert(Context context) {
		mFocusView.setVisibility(View.INVISIBLE);
//		mQamLinearLayout.setBackgroundResource(R.drawable.search_et_normal);
		mQamTextView.setTextColor(getResources().getColor(R.color.white));
//		mQamTextView.setPadding((int)getResources().getDimension(R.dimen.search_down_textview_padding), 0, 0, 0);
		mQamImageview.setImageResource(R.drawable.arrow_up_down_unfocus);
		View view = LayoutInflater.from(context).inflate(
				R.layout.search_down_list_layout, null);
//        mFocusView = (ImageView)view.findViewById(R.id.ivFocus);
		ListView downListView = (ListView) view.findViewById(R.id.search_down_listview);
		Integer[] qams = {64,128,256};
		ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(context, R.layout.search_manual_down_list_item, R.id.search_down_list_textview, qams);
		downListView.setAdapter(adapter);
		if (mAlertDialog == null) {
			mAlertDialog = new Dialog(context, R.style.searchDownListTheme);
		}
		
		downListView.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		});
		
		downListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mQamTextView.setTextColor(getResources().getColor(R.color.yellow));
				mQamImageview.setImageResource(R.drawable.arrow_up_down_focus);
				switch (position) {
				case 0:
					mQamTextView.setText(R.string.sixtyfour_qam);
					mDefaultTransponder.setModulation(2);
					mAlertDialog.dismiss();
					mFocusView.setVisibility(View.VISIBLE);
					break;
				case 1:
					mQamTextView.setText(R.string.onetwoeight_qam);
					mAlertDialog.dismiss();
					mDefaultTransponder.setModulation(3);
					mFocusView.setVisibility(View.VISIBLE);
					break;
				case 2:
					mQamTextView.setText(R.string.twofivesix_qam);
					mAlertDialog.dismiss();
					mDefaultTransponder.setModulation(4);
					mFocusView.setVisibility(View.VISIBLE);
					break;
				default:
					break;
				}
			}
		});
		
		mAlertDialog.setContentView(view);
		Window window = mAlertDialog.getWindow();
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
		params.width = mQamLinearLayout.getWidth();
		params.height = (int)getResources().getDimension(R.dimen.search_down_list_height);
		params.dimAmount = 0.4f;
		params.flags = LayoutParams.FLAG_DIM_BEHIND;
		int xOffset = (int)getResources().getDimension(R.dimen.search_down_xoffset);
		params.x = location[0] - x + params.width/2 - xOffset;
		params.y = location[1] + height -y + params.height/2;
		window.setAttributes(params);
		mAlertDialog.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if(event.getKeyCode() == KeyEvent.KEYCODE_ESCAPE || event.getKeyCode() == KeyEvent.KEYCODE_BACK){
					mQamTextView.setTextColor(getResources().getColor(R.color.yellow));
					mQamImageview.setImageResource(R.drawable.arrow_up_down_focus);
//					mQamTextView.setPadding((int)getResources().getDimension(R.dimen.search_down_textview_padding), 0, 0, 0);
					mAlertDialog.dismiss();
					mFocusView.setVisibility(View.VISIBLE);
					return true;
				}
				return false;
			}
		});
		mAlertDialog.show();
	}
	
	OnFocusChangeListener onQamLinearFocusChangeListener = new OnFocusChangeListener() {
		
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if(hasFocus){
				TextView textview = (TextView) v.findViewById(R.id.search_settings_qam_textview);
				ImageView imageView = (ImageView) v.findViewById(R.id.search_settings_qam_imageview);
				textview.setTextColor(getResources().getColor(R.color.yellow));
				imageView.setImageResource(R.drawable.arrow_up_down_focus);
				mFocusView.setVisibility(View.VISIBLE);
				int [] location = new int [2];
				v.getLocationInWindow(location);
				if(location[1] == 0)
					return;
				MarginLayoutParams params = (MarginLayoutParams) mFocusView
						.getLayoutParams();
				int topOffset = (int)getResources().getDimension(R.dimen.search_manual_top_offset);
				params.topMargin = location[1] - topOffset;
				Log.d("songwenxuan","params.leftMargin = " + location[0]);
				int leftOffset = (int)getResources().getDimension(R.dimen.search_manual_left_offset);
				params.leftMargin = location[0] - leftOffset;
				Log.d("songwenxuan","onFocusChange() , params.topMargin = " + params.topMargin +"params.leftMargin = " + params.leftMargin);
				mFocusView.setLayoutParams(params);
			}else{
				mFocusView.setVisibility(View.INVISIBLE);
				TextView textview = (TextView) v.findViewById(R.id.search_settings_qam_textview);
				ImageView imageView = (ImageView) v.findViewById(R.id.search_settings_qam_imageview);
				textview.setTextColor(getResources().getColor(R.color.white));
				imageView.setImageResource(R.drawable.arrow_up_down_unfocus);
			}
		}
	};
	
	private OnFocusChangeListener onFocusChangeListener = new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus){
					EditText et = (EditText)v;
					et.setTextColor(getResources().getColor(R.color.yellow));
					mFocusView.setVisibility(View.VISIBLE);
					int [] location = new int [2];
					v.getLocationInWindow(location);
					if(location[1] == 0)
						return;
					MarginLayoutParams params = (MarginLayoutParams) mFocusView
							.getLayoutParams();
					int topOffset = (int)getResources().getDimension(R.dimen.search_manual_top_offset);
					params.topMargin = location[1] - topOffset;
					Log.d("songwenxuan","params.leftMargin = " + location[0]);
//					params.leftMargin = location[0] - 494-81-48-2-2-1-1;
					int leftOffset = (int)getResources().getDimension(R.dimen.search_manual_left_offset);
					params.leftMargin = location[0] - leftOffset;
					Log.d("songwenxuan","onFocusChange() , params.topMargin = " + params.topMargin +"params.leftMargin = " + params.leftMargin);
					mFocusView.setLayoutParams(params);
				}else{
					mFocusView.setVisibility(View.INVISIBLE);
					EditText et = (EditText)v;
					et.setTextColor(getResources().getColor(R.color.white));
				}
			}
		};
}
