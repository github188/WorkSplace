package com.lenovo.settings;


import static android.net.ethernet.EthernetManager.ETH_STATE_DISABLED;
import static android.net.ethernet.EthernetManager.ETH_STATE_ENABLED;
import static android.net.ethernet.EthernetManager.ETH_STATE_UNKNOWN;

import java.util.Locale;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.net.DhcpInfo;
import android.net.NetworkUtils;
import android.net.ethernet.EthernetDevInfo;
import android.net.ethernet.EthernetManager;
import android.net.ethernet.EthernetStateTracker;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class NetworkEth extends Fragment {

	private static final int CONNECT_MODE_STYPE = 101;
	private EthernetManager mEthManager;
	private ImageView ethConnectState;
	private TextView ethConnectText;
	private static final int FAILED = 0, CONNECT = 1, CONNECTED = 2;
	protected static final String TAG = "NetworkEth";
	private View mView;
	private RelativeLayout ethAdd;
	private LinearLayout connect_mode;
	private ImageView conn_mode_icon_auto;
	private ImageView conn_mode_icon_manual;
//	private TextView conn_mode_msg;
	private EthernetDevInfo mEthInfo;
	//private TextView mConnectmode;
	private LinearLayout mConnectmode;
	private static boolean isConfig, isRunning = true;
	private int connect_index;
	private int mIndex = 0;
	private AnimationDrawable mAnimationDrawable;
	private Activity mContext;// Ethernet
    boolean mEthernetConnected, mEthernetWaitingDHCP;
	private IntentFilter mFilter;
	private BroadcastReceiver mReceiver;
	private Toast mToast;
	private RelativeLayout mRelative;
//	private ListView mDropdownList;
//	private RelativeLayout mListLayout;
//	private ArrayAdapter<CharSequence> mAdapter;
	protected boolean mListEnd = false;
	protected boolean mEnableList = false;
	private String[] mEthArrays;
	//private DropdownMenu mDropMenu;
	ItemInfo mIT;
	private TextView mTextviewManual;
    private boolean DEBUG = true;
    private Context mContent;
    private boolean canClick = true;

    public void MyLog(String log) {
        if (DEBUG) {
            Log.d(TAG, "-------" + log);
        }
    }
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
	

			switch(msg.what){
			case CONNECT_MODE_STYPE:
                MyLog(" handleMessage mIndexEth =  " + mIndexEth);
                if(mEthManager.isEthDeviceAdded() && (mEthManager.getEthState() == EthernetManager.ETH_STATE_ENABLED)){
                    saveDhcpEth();
                }
				if(mIndexEth == 0){  //0启动，1未启动
					mIndexEth = 1;
//					conn_mode_msg.setText(R.string.start_close);
//					conn_mode_icon.setVisibility(View.INVISIBLE);
//					if(mEthManager.isEthDeviceAdded() && (mEthManager.getEthState() == EthernetManager.ETH_STATE_ENABLED)){
//					    saveEth(1);
//					}
//					setEthAddButtonEnable(true);
//					mTextviewManual.setAlpha(1f);
				}else{
					mIndexEth = 0;
//					conn_mode_msg.setText(R.string.start_on);
//					conn_mode_icon.setVisibility(View.VISIBLE);
//					if(mEthManager.isEthDeviceAdded() && (mEthManager.getEthState() == EthernetManager.ETH_STATE_ENABLED)){
//					    saveDhcpEth();
//					}
//					setEthAddButtonEnable(false);
//					mTextviewManual.setAlpha(0.3f);
				}
				break;
			case DropdownMenu.DROPDOWN_MENU_ITEM_CHICK:
//				if(msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_CAN_EDIT){
//					EditText edit = (EditText) msg.obj;
//				}else if(msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_NOT_EDIT){
//					TextView text = (TextView) msg.obj;
//					int position = msg.arg2;
//					if(text == mConnectmode){
//						  if(mIndexEth != position){
//							  mIndexEth = position;
//								if (position == 0) {
//									saveDhcpEth();
//									setEthAddButtonEnable(false);
//								}
//
//								else {
//									saveEth(position);
//									setEthAddButtonEnable(true);
//								}
//						  }
//					}else{
//						break;
//					}
//				}
				break;
			case DropdownMenu.DROPDOWN_MENU_GET_ADAPTER:
//				if(msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_CAN_EDIT){
//					EditText edit = (EditText) msg.obj;
//				}else if(msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_NOT_EDIT){
//					TextView text = (TextView) msg.obj;
//					if(text == mConnectmode){
//						mDropMenu.setListViewPosition(0, 2);
//						mDropMenu.setListViewAdapter(mEthArrays, mIndexEth);
//					}else{
//						break;
//					}
//					mDropMenu.showDropdownListEnable(true);
//				}
				break;
			}
		}
		
	};
	private int mIndexEth = 0;
	private EthernetDevInfo ethInfo;
    
    public NetworkEth() {
        mFilter = new IntentFilter();
        mFilter.addAction(EthernetManager.ETH_STATE_CHANGED_ACTION);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
//                Log.e(TAG,"action = "+action);
                MyLog(" onReceive  action = " + action);
                if (action.equals(EthernetManager.ETH_STATE_CHANGED_ACTION)) {
                	updateEth(context, intent);
                }
            }
        };
    }


@Override
	public void onDestroy() {

		super.onDestroy();
		Log.d(TAG,"###onDestroy!!");
		//list is dirty. need clean
		ConfigFocus.Items_t.clear();
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
	    mContent = this.getActivity().getApplicationContext();
		mView = inflater.inflate(R.layout.network_eth, container, false);
		mContext = getActivity();
		mEthManager = (EthernetManager) getActivity().getSystemService(
				Context.ETH_SERVICE);
//		mDropMenu = new DropdownMenu(getActivity(),mView,mHandler );

		ethConnectState = (ImageView) mView.findViewById(R.id.ethsetting_state);
		ethConnectText = (TextView) mView.findViewById(R.id.ethsetting_text);
		ethAdd = (RelativeLayout) mView.findViewById(R.id.btn_ethmanual);
		connect_mode = (LinearLayout) mView.findViewById(R.id.connect_mode);
		conn_mode_icon_auto =(ImageView) mView.findViewById(R.id.conn_mode_icon_auto);
		conn_mode_icon_manual =(ImageView) mView.findViewById(R.id.conn_mode_icon_manual);
//		conn_mode_msg = (TextView)mView.findViewById(R.id.conn_mode_msg);
		mTextviewManual = (TextView)mView.findViewById(R.id.textview_manual);
		
		ethConnectState.setBackgroundResource(R.drawable.eth_anim);
		mAnimationDrawable = (AnimationDrawable)ethConnectState.getBackground();
		Log.d(TAG,"mAnimationDrawable = "+mAnimationDrawable);
		mConnectmode = (LinearLayout) mView.findViewById(R.id.connect_mode);
//		mDropdownList = (ListView) mView.findViewById(R.id.dropdownlist);
//		mListLayout = (RelativeLayout) mView.findViewById(R.id.listLayout);
//		mAdapter = ArrayAdapter.createFromResource(getActivity(),
//				R.array.frequency_array, R.layout.dropdown_item);
        if(mEthManager.getEthState() != ETH_STATE_ENABLED){
			mEthManager.setEthEnabled(true);
		}
        try {
//            MyLog(" onCreat mEthManager.getEthState() = "
//                    + mEthManager.getEthState() + " isEthDeviceAdded = "
//                    + mEthManager.isEthDeviceAdded() + " ethConfigured = "
//                    + mEthManager.ethConfigured() + " isEthDeviceUp = "
//                    + mEthManager.isEthDeviceUp() + " isEthConfigured = "
//                    + mEthManager.isEthConfigured());
            mIndexEth = getEthState();
            if (mEthManager != null && !mEthManager.isEthDeviceUp()
                    && !mEthManager.isEthDeviceAdded()) {
                //saveDhcpEth();
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
//		if(mIndexEth == 0){                                     //20120627whj
//			conn_mode_msg.setText(R.string.start_on);
//		}else{
//			conn_mode_msg.setText(R.string.start_close);
//		}
			
		
		mEthArrays = getResources().getStringArray(R.array.frequency_array);
//		mDropMenu.setButtonListener(mConnectmode,mEthArrays[mIndexEth]);
//		mDropMenu.setListViewListener();
		//mAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
		//mDropdownList.setAdapter(mAdapter);
		//mDropdownList.setDivider(null);
		//mDropdownList.setDividerHeight(0);

        try {
            // if(mEthManager.isEthDeviceAdded() && (mEthManager.getEthState()
            // == EthernetManager.ETH_STATE_ENABLED)){
            if (mEthManager.getDhcpInfo().ipAddress != 0) {
                ethConnectState.setBackgroundResource(R.drawable.ethsetting_ok);
                ethConnectText.setText(R.string.ethconnect_ok);
//                conn_mode_msg.setText(R.string.start_on);
            } else {
                ethConnectState
                        .setBackgroundResource(R.drawable.ethsetting_fail);
                ethConnectText.setText(R.string.ethconnect_fail);
//                conn_mode_msg.setText(R.string.start_close);
            }
        } catch (Exception e) {
            // TODO: handle exception
            ethConnectState.setBackgroundResource(R.drawable.ethsetting_fail);
            ethConnectText.setText(R.string.ethconnect_fail);
//            conn_mode_msg.setText(R.string.start_close);
        }

		//ethAdd.requestFocus();
		ethAdd.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
		
				Fragment fragment = (Fragment) new NetworkEthManual();
				SettingFragment sf = (SettingFragment) new SettingFragment(
						getActivity());
				sf.setFragment(fragment, true);
				mIT.VId=R.id.btn_ethmanual;
				//mConnectmode.setSelection(1);
			}
		});
		
		connect_mode.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				MyLog("  connect_mode  OnClick  mEthernetWaitingDHCP = " + mEthernetWaitingDHCP + " canClick = " + canClick);
                if (canClick) {
                    canClick = false;
                    Message msg = new Message();
                    msg.what = CONNECT_MODE_STYPE;
                    mHandler.removeMessages(CONNECT_MODE_STYPE);
                    mHandler.sendMessageDelayed(msg, 500);
                    setConnectMode(true);
                }
			}
		});
		/*
		mConnectmode.setOnKeyListener(new OnKeyListener(){


			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
		
				if((keyCode == KeyEvent.KEYCODE_ENTER) || (keyCode == KeyEvent.KEYCODE_DPAD_CENTER)){
					mEnableList  = true;
					showDropdownListEnable(true);		
					return true;
				}else if(keyCode == KeyEvent.KEYCODE_BACK){
					if(mEnableList){
						mEnableList = false;
						showDropdownListEnable(false);	
						return true;
					}
				}else if(keyCode == KeyEvent.KEYCODE_DPAD_UP){
					if(mEnableList){
						return true;
					}
				}
				return false;
			}
        	
        });
		updateDropdownList(mConnectmode,0);*/
		//mConnectmode.setFocusable(true);
		//mConnectmode.setFocusableInTouchMode(true);
		LenovoSettingsActivity.setTitleFocus(false);
		//Hazel add {
		  mIT=new ItemInfo();
		if(ConfigFocus.Items_t.size()==0)
	 	{
	 		  mConnectmode.requestFocus();
	 		  mIT.VId=R.id.connect_mode;
	 		  ConfigFocus.Items_t.add(mIT);
//	 		 conn_mode_icon.setVisibility(View.VISIBLE);
//	 		conn_mode_icon.setBackgroundResource(R.drawable.sel_icon);
		}
		else
		{
//			conn_mode_icon.setVisibility(View.INVISIBLE);
			 ItemInfo gItemId=ConfigFocus.Items_t.get(0);	
			 findFxView(gItemId.VId);
			 Log.d(TAG,"$$$ConfigFocus.Items_t.size()!=NULL");
		}
		//Hazel add }
//		setEthAddButtonEnable(mIndexEth == 0 ? false : true);
//		mTextviewManual.setAlpha(mIndexEth == 0 ? 0.3f : 1f);
//		conn_mode_msg.setText(mIndexEth == 0 ? R.string.start_on : R.string.start_close);
//		conn_mode_icon.setVisibility(mIndexEth == 0 ? View.VISIBLE : View.INVISIBLE);
		return mView;

	}

	private int  getEthState() {
		isConfig = mEthManager.isEthConfigured();
		if (!isConfig)
			connect_index = 0;
		else {
			String mode = mEthManager.getSavedEthConfig().getConnectMode();
			if (mode.equals(EthernetDevInfo.ETH_CONN_MODE_DHCP))
				connect_index = 0;
			else
				connect_index = 1;
		}
		MyLog(" getEthState  connect_index = " + connect_index);
       return connect_index;
	}

	private void saveEth(int postion) {
	    /*
        String mode = mEthManager.getSavedEthConfig().getConnectMode();
        */
        
        //mEthManager.setEthEnabled(false);
//		if (mode.equals(EthernetDevInfo.ETH_CONN_MODE_DHCP)) {                   //20120627
//			//ethConnectState.setBackgroundResource(R.drawable.ethsetting_fail);
//			//ethConnectText.setText(R.string.ethconnect_fail);
//			ethConnectText.setText(R.string.ethconnect_wait);
//			if(mAnimationDrawable.isRunning() == false){
//				Log.d(TAG,"AnimationDrawable start");
//				ethConnectState.setBackgroundResource(R.drawable.eth_anim);
//				mAnimationDrawable = (AnimationDrawable)ethConnectState.getBackground();
//				//ethConnectState.setImageDrawable(null);
//				mAnimationDrawable.start();
//			}
//		} 
		//EthernetDevInfo info = new EthernetDevInfo();
		//info.setIfName(mEthManager.getDeviceNameList()[0]);
		//info.setConnectMode(EthernetDevInfo.ETH_CONN_MODE_MANUAL);
        EthernetDevInfo info = getEthernetDevInfo();
		MyLog(" saveEth  ip = " + info.getIpAddress()
                            + " RouteAddr = " + info.getRouteAddr()
                            + " DnsAddr = " + info.getDnsAddr()
                            + " NetMask = " + info.getNetMask());
		mEthManager.updateEthDevInfo(info);
		/*if(mAnimationDrawable.isRunning()){
			mAnimationDrawable.stop();
		}
		ethConnectState.setBackgroundResource(R.drawable.ethsetting_fail);
		ethConnectText.setText(R.string.ethconnect_fail);
		mEthManager.setEthEnabled(false);*/
	}

	private void saveDhcpEth() {
		//mEthManager.setEthEnabled(false);
		EthernetDevInfo info = new EthernetDevInfo();
		String name = mEthManager.getDeviceNameList()[0];
		info.setIfName(name);
		info.setConnectMode(EthernetDevInfo.ETH_CONN_MODE_DHCP);
		mEthManager.updateEthDevInfo(info);
		//mEthManager.setEthEnabled(true);
		MyLog(" saveDhcpEth getDeviceName = " + name);
	}
	
	public void onStart() {
		Log.i("NetworkEth ","onStart");
		getEthState();
		getActivity().registerReceiver(mReceiver, mFilter);
        if (mEthManager != null && mEthManager.isEthConfigured()) {
            if (mEthManager.getSavedEthConfig().getConnectMode()
                    .equals(EthernetDevInfo.ETH_CONN_MODE_MANUAL)) {
                setConnectMode(false);
            } else {
                setConnectMode(true);
            }
        }
		super.onStart();
	}
	

    @Override
	public void onStop() {

		getActivity().unregisterReceiver(mReceiver);
		super.onStop();
	}

	// ===== Ethernet ===================================================================
    private final void updateEth(Context context, Intent intent) {
        final int event = intent.getIntExtra(EthernetManager.EXTRA_ETH_STATE,
                EthernetStateTracker.EVENT_INTERFACE_CONFIGURATION_FAILED);
        MyLog("updateEth event = " + event + " mEthernetWaitingDHCP = " + mEthernetWaitingDHCP);
        switch (event) {
                // else fallthrough
            case EthernetStateTracker.EVENT_INTERFACE_CONFIGURATION_SUCCEEDED: 
            {
                //Modify for get ip is zero for first time.
                DhcpInfo dhcpInfo = mEthManager.getDhcpInfo();
                if (dhcpInfo == null || dhcpInfo.ipAddress == 0) {
                    saveDhcpEth();
                    return;
                }

                mEthernetWaitingDHCP = false;
                // EthernetManager ethManager = (EthernetManager)
                // context.getSystemService(Context.ETH_SERVICE);
                if (mEthManager.isEthDeviceAdded()) {
                    mEthernetConnected = true;
                    ethConnectText.setText(R.string.ethconnect_ok);
                    // conn_mode_msg.setText(R.string.start_on);
                    if (mAnimationDrawable.isRunning()) {
                        mAnimationDrawable.stop();
                        // ethConnectState.setBackgroundDrawable(null);
                    }
                    ethConnectState.setBackgroundResource(R.drawable.ethsetting_ok);
                }
                canClick = true;
                if (mEthManager != null && mEthManager.isEthConfigured()) {
                    if (mEthManager.getSavedEthConfig().getConnectMode()
                            .equals(EthernetDevInfo.ETH_CONN_MODE_MANUAL)) {
                        setConnectMode(false);
                    } else {
                        setConnectMode(true);
                    }
                }
                return;
            }
            case EthernetStateTracker.EVENT_INTERFACE_CONFIGURATION_FAILED:
                mEthernetWaitingDHCP = false;
                mEthernetConnected = false;
				ethConnectText.setText(R.string.ethconnect_disable);
//				conn_mode_msg.setText(R.string.start_close);
				if(mAnimationDrawable.isRunning()){
					mAnimationDrawable.stop();
					//ethConnectState.setBackgroundDrawable(null);
				}
				ethConnectState.setBackgroundResource(R.drawable.ethsetting_fail);
				canClick = true;
                if (mEthManager != null && mEthManager.isEthConfigured()) {
                    if (mEthManager.getSavedEthConfig().getConnectMode()
                            .equals(EthernetDevInfo.ETH_CONN_MODE_MANUAL)) {
                        setConnectMode(false);
                    } else {
                        setConnectMode(true);
                    }
                }
                return;
            case EthernetStateTracker.EVENT_HW_CONNECTED:
            	Log.d(TAG,"connect info = "+mEthernetConnected);
            case EthernetStateTracker.EVENT_HW_PHYCONNECTED:
                MyLog(" mEthernetWaitingDHCP = " + mEthernetWaitingDHCP);
                if(mEthManager.getSavedEthConfig().getConnectMode().equals(EthernetDevInfo.ETH_CONN_MODE_MANUAL)){
                    MyLog(" manual mode so saveEth(1) ");
                    saveEth(1);
                }else{
                    saveDhcpEth();
                }
                if (mEthernetWaitingDHCP)
                    return;
                ethConnectText.setText(R.string.ethconnect_fail);
                ethConnectState.setBackgroundResource(R.drawable.ethsetting_fail);
                canClick = true;
                break;
            case EthernetStateTracker.EVENT_DHCP_START:
                // There will be a DISCONNECTED and PHYCONNECTED when doing a
                // DHCP request. Ignore them.
            	if(mEthernetConnected)
            		return;
                mEthernetWaitingDHCP = true;
                mEthernetConnected = true;
				ethConnectText.setText(R.string.ethconnect_wait);
				if(mAnimationDrawable.isRunning() == false){
					Log.d(TAG,"AnimationDrawable start");
					ethConnectState.setBackgroundResource(R.drawable.eth_anim);
					mAnimationDrawable = (AnimationDrawable)ethConnectState.getBackground();
					//ethConnectState.setImageDrawable(null);
					mAnimationDrawable.start();
//					conn_mode_msg.setText(R.string.ethconnect_wait);
				}
				
                return;
            case EthernetStateTracker.EVENT_HW_CHANGED:
                if (mEthManager.getEthState() == EthernetManager.ETH_STATE_ENABLED
                        && mEthManager.isEthDeviceAdded()) {
                    return;
                }
				ethConnectText.setText(R.string.ethconnect_disable);
//				conn_mode_msg.setText(R.string.start_close);
				if(mAnimationDrawable.isRunning()){
					mAnimationDrawable.stop();
					//ethConnectState.setBackgroundDrawable(null);
				}
				ethConnectState.setBackgroundResource(R.drawable.ethsetting_fail);
                return;
            case EthernetStateTracker.EVENT_HW_DISCONNECTED:
                if (mEthernetWaitingDHCP)
                    return;
                mEthernetConnected = false;
				if(mAnimationDrawable.isRunning()){
					mAnimationDrawable.stop();
				}
                if(mEthManager.isEthDeviceAdded() && (mEthManager.getEthState() == EthernetManager.ETH_STATE_ENABLED)){
                    if(mAnimationDrawable.isRunning() == false){
                        ethConnectState.setBackgroundResource(R.drawable.eth_anim);
                        mAnimationDrawable = (AnimationDrawable)ethConnectState.getBackground();
                        mAnimationDrawable.start();
//                        conn_mode_msg.setText(R.string.ethconnect_wait);
                    }
                    ethConnectText.setText(R.string.ethconnect_wait);
                }else{
                    ethConnectText.setText(R.string.ethconnect_fail);
//                    conn_mode_msg.setText(R.string.start_close);
                    ethConnectState.setBackgroundResource(R.drawable.ethsetting_fail);
                }
                canClick = true;
                return;
        }
    }
    
    void setEthAddButtonEnable(boolean enable){
		ethAdd.setEnabled(enable);
		ethAdd.setFocusable(enable);
    }
    
    
    EthernetDevInfo getEthernetDevInfo(){
//    	mEthManager = (EthernetManager) getActivity().getSystemService(
//				Context.ETH_SERVICE);
		String[] Devs = mEthManager.getDeviceNameList();
		EthernetDevInfo info = new EthernetDevInfo();
		if (Devs != null) {
	        info.setIfName(Devs[0]);
	        info.setConnectMode(EthernetDevInfo.ETH_CONN_MODE_MANUAL);
			Log.d(TAG,"---------mEthManager.getDeviceNameList = "+Devs.length);
			if (mEthManager.isEthConfigured()) {
//				ethInfo = mEthManager.getSavedEthConfig();
//				if(ethInfo.getIpAddress() == null||ethInfo.getIpAddress()=="0.0.0.0"){
//					DhcpInfo dhcpInfo = mEthManager.getDhcpInfo();
//					Log.d(TAG,"------no manual address so get dhcp");
//					info.setIpAddress(getAddress(dhcpInfo.ipAddress));
//					info.setDnsAddr(getAddress(dhcpInfo.dns1));
//					info.setNetMask(getAddress(dhcpInfo.netmask));
//					info.setRouteAddr(getAddress(dhcpInfo.gateway));
//				}else{
                    Log.d(TAG, "--------have manual address ");
                    DhcpInfo dhcpInfo = mEthManager.getDhcpInfo();
                    try {
                        SharedPreferences preferences = mContent
                                .getSharedPreferences(NetworkEthManual.PRENAME,
                                        Context.MODE_PRIVATE);
                        info.setIpAddress(preferences.getString(
                                NetworkEthManual.mIp, getAddress(dhcpInfo.ipAddress)));
                        info.setDnsAddr(preferences.getString(
                                NetworkEthManual.mDns, getAddress(dhcpInfo.dns1)));
                        info.setNetMask(preferences.getString(
                                NetworkEthManual.mMask, getAddress(dhcpInfo.netmask)));
                        info.setRouteAddr(preferences.getString(
                                NetworkEthManual.mGateway, getAddress(dhcpInfo.gateway)));
                    } catch (Exception e) {
                        // TODO: handle exception
                        MyLog(" Error getEthernetDevInfo mContent = " + mContent);
                    }
//				}
			}else{
			    MyLog(" error no eth config -----");
			}
		}else{
		    MyLog(" error getDeviceNameList is null null  null null");
		}
		return info;
    }

	static String getAddress(int addr) {
	        return NetworkUtils.intToInetAddress(addr).getHostAddress();
	}
	
    public void findFxView(int View_ID) {
        switch (View_ID) {
        case R.id.btn_ethmanual:
            ethAdd.requestFocus();
            break;
        }
    }
    /**
     * 设置连接方式
     * @param isAuto 自动连接方式
     */
    public void setConnectMode(boolean isAuto){
        if(isAuto){
            conn_mode_icon_auto.setVisibility(View.VISIBLE);
            conn_mode_icon_manual.setVisibility(View.INVISIBLE);
        }else{
            conn_mode_icon_auto.setVisibility(View.INVISIBLE);
            conn_mode_icon_manual.setVisibility(View.VISIBLE);
        }
    }
	
}
