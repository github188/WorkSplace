package com.lenovo.settings;


import java.net.SocketException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Dialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkUtils;
import android.net.ProxyProperties;
import android.net.ethernet.EthernetDevInfo;
import android.net.ethernet.EthernetManager;
import android.net.ethernet.EthernetStateTracker;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.IpAssignment;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Networksettings extends Fragment {
	private static final String TAG = "Networksettings";
	private EthernetManager mEthManager;
	private EthernetDevInfo mEthInfo;
	private TextView IPAddr, Netmask, Gateway, DNS, GetWay,ConnectSpeed,SignalStrength,
				SignalStrengthTitle,Connection;
	private LinearLayout BtnEth, BtnWirless, BtnProxy;
	private TextView mbtn_proxy,mbtn_wireless,mbtn_eth;
	private LinearLayout mLayout;
	private View mView;
	private ListView mListView;
	private ListViewAdapter mListViewAdapter;
	private int[] mArrayName = {R.string.Network_eth,R.string.Network_wireless,R.string.Network_proxy};
	private IntentFilter mFilter;
	private BroadcastReceiver mReceiver;
	private boolean mEthernetConnected = false;
	private boolean mWifiConnected = false;
	private boolean mGetIpAddress = false;
	private WifiManager mWifiManager;
	private AtomicBoolean mConnected = new AtomicBoolean(false);
	private WifiInfo mWifiConnectInfo;
  
  ItemInfo mIT;
  
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
        mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION);
        mFilter.addAction(WifiManager.LINK_CONFIGURATION_CHANGED_ACTION);
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        mFilter.addAction(WifiManager.ERROR_ACTION);
        mFilter.addAction(EthernetManager.ETH_STATE_CHANGED_ACTION);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleEvent(context, intent);
            }
        };
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mView = inflater.inflate(R.layout.network_settings, container,false);
//		mListView = (ListView) mView.findViewById(R.id.network_list);
//		mListViewAdapter = new ListViewAdapter(getActivity(), mArrayName);
//		mListView.setAdapter(mListViewAdapter);
		Connection = (TextView) mView.findViewById(R.id.connection);
		ConnectSpeed = (TextView) mView.findViewById(R.id.Connectionspeed);
		SignalStrength = (TextView) mView.findViewById(R.id.Signaltext);
		SignalStrengthTitle = (TextView) mView.findViewById(R.id.Signalstrength);
		ConnectSpeed = (TextView) mView.findViewById(R.id.Connectionspeed);
		SignalStrengthTitle.setVisibility(View.INVISIBLE);
		SignalStrength.setVisibility(View.INVISIBLE);
		IPAddr = (TextView) mView.findViewById(R.id.IPAddrText);
		Netmask = (TextView) mView.findViewById(R.id.NetmaskText);
		Gateway = (TextView) mView.findViewById(R.id.GatewayText);
		DNS = (TextView) mView.findViewById(R.id.DNSText);
		GetWay = (TextView) mView.findViewById(R.id.GetWayText);
		BtnEth = (LinearLayout) mView.findViewById(R.id.btn_eth);
		BtnWirless = (LinearLayout) mView.findViewById(R.id.btn_wireless);
		BtnProxy = (LinearLayout) mView.findViewById(R.id.btn_proxy);
		mbtn_proxy = (TextView) mView.findViewById(R.id.btn_proxy_text);
		mbtn_wireless = (TextView) mView.findViewById(R.id.btn_wireless_text);
		mbtn_eth = (TextView) mView.findViewById(R.id.btn_eth_text);
		
//		mbtn_proxy.setAlpha(0.3f);
//		mbtn_wireless.setAlpha(0.3f);
//		mbtn_eth.setAlpha(0.3f);

		mWifiManager = (WifiManager) getActivity().getSystemService(
				Context.WIFI_SERVICE);
		//mWifiManager.asyncConnect(getActivity(), new WifiServiceHandler());
        BtnEth.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                
                Log.e(TAG, "eth button is click! mWifiConnected = " + mWifiConnected);
                if (!mWifiConnected) {
                    Fragment fragment = (Fragment) new NetworkEth();
                    SettingFragment sf = (SettingFragment) new SettingFragment(
                            getActivity());
                    sf.setFragment(fragment, true);
                    mIT.MasterId = R.id.btn_eth;
                } else {
                    NetworkWireless.showToast(getActivity(),
                            R.string.network_tips_usewifi);
                }
            }
        });
		//BtnEth.requestFocus();

		BtnWirless.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
		
				Fragment fragment = (Fragment) new NetworkWireless();
				SettingFragment sf = (SettingFragment) new SettingFragment(
						getActivity());
				sf.setFragment(fragment,true);
				mIT.MasterId=R.id.btn_wireless;
			}
		});

		BtnProxy.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
		
				Fragment fragment = (Fragment) new NetworkProxy();
				SettingFragment sf = (SettingFragment) new SettingFragment(
						getActivity());
				sf.setFragment(fragment,true);
				mIT.MasterId=R.id.btn_proxy;
			}
		});
		
		LenovoSettingsActivity.setTitleFocus(false);
	  mIT=new ItemInfo();
	  if(ConfigFocus.Master.size()==0)
		{
			Log.d(TAG,"####ConfigFocus.Master.size()==0");//first time---let the list get dirty
			BtnEth.requestFocus();
			mIT.MasterId=R.id.btn_eth;
		  ConfigFocus.Master.add(mIT);  
	  }	
	  else
	  {
      Log.d(TAG,"####ConfigFocus.Master.size()!=0");//first time---let the list get dirty
		  ItemInfo MasterId_t=ConfigFocus.Master.get(0);	
	  	findFxView(MasterId_t.MasterId);
	  
	  }
	  
	  //Hazel add }
	  //updateNetworkDisconnected();
	  updateNetwordDisplay();
	  //BtnEth.setFocusable(true);
	  //BtnEth.setFocusableInTouchMode(true);
	  ConfigFocus.Master.set(0,mIT);
		
//		mListView.setOnItemClickListener(new OnItemClickListener() {
//			@Override
//			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
//					long arg3) {
//				if(position == 0){
//					Fragment fragment = (Fragment) new NetworkEth();
//					SettingFragment sf = (SettingFragment) new SettingFragment(
//							getActivity());
//					sf.setFragment(fragment,true);
//					mIT.MasterId=R.id.network_list/*btn_eth*/;
//				}
//					
//			}
//		});
	  BtnEth.requestFocus();
		
	  return mView;

	}
	
    public void onStart() {
        super.onStart(); 
		getActivity().registerReceiver(mReceiver, mFilter); 
    } 

	@Override
	public void onStop() {
		super.onStop();
		getActivity().unregisterReceiver(mReceiver);
	}

	private static String getAddress(int addr) {
	        return NetworkUtils.intToInetAddress(addr).getHostAddress();
	}
    void updateEthAddress() {
        Log.d(TAG, "-----updateEthAddress----");
        mEthManager = (EthernetManager) getActivity().getSystemService(
                Context.ETH_SERVICE);
        String[] Devs = mEthManager.getDeviceNameList();
        int i = 1;
        if (Devs != null) {
            Log.d(TAG, " onCreat mEthManager.getEthState() = "
                    + mEthManager.getEthState() + " isEthDeviceAdded = "
                    + mEthManager.isEthDeviceAdded() + " ethConfigured = "
                    + mEthManager.ethConfigured() + " isEthDeviceUp = "
                    + mEthManager.isEthDeviceUp() + " isEthConfigured = "
                    + mEthManager.isEthConfigured());
            Log.d(TAG, " mEthManager.getDeviceNameList length = " + Devs.length + " Devs = " + Devs[0]);
            SignalStrengthTitle.setVisibility(View.INVISIBLE);
            SignalStrength.setVisibility(View.INVISIBLE);
            if (mEthManager.isEthConfigured()) {
                mEthInfo = mEthManager.getSavedEthConfig();
                if (mEthInfo.getConnectMode().equals(EthernetDevInfo.ETH_CONN_MODE_DHCP)) {
                    Log.d(TAG, "---set dhcp EthConfig--");
                    Log.d(TAG, " ip = " + mEthInfo.getIpAddress()
                            + " RouteAddr = " + mEthInfo.getRouteAddr()
                            + " DnsAddr = " + mEthInfo.getDnsAddr()
                            + " NetMask = " + mEthInfo.getNetMask());
                    DhcpInfo dhcpInfo = mEthManager.getDhcpInfo();
                    Log.d(TAG, "ip = " + dhcpInfo.ipAddress + " dns = " + dhcpInfo.dns1 + " mEthInfo = " + mEthInfo.getIpAddress());
                    if (dhcpInfo != null && dhcpInfo.ipAddress != 0) {
                        IPAddr.setText(getAddress(dhcpInfo.ipAddress));
                        Gateway.setText(getAddress(dhcpInfo.gateway));
                        DNS.setText(getAddress(dhcpInfo.dns1));
                        Netmask.setText(getAddress(dhcpInfo.netmask));
                        GetWay.setText(getResources().getText(
                                R.string.GetwayDhcp));
                        mbtn_eth.setText(R.string.network_connected);
                        Connection.setText(getActivity().getString(
                                R.string.eth_connection, i));
                        ConnectSpeed.setText(R.string.Connection_text);
                        mWifiConnected = false;
                    }
                } else {
                    Log.d(TAG, "---set Saved EthConfig--");
                    Log.d(TAG, " ip = " + mEthInfo.getIpAddress()
                            + " RouteAddr = " + mEthInfo.getRouteAddr()
                            + " DnsAddr = " + mEthInfo.getDnsAddr()
                            + " NetMask = " + mEthInfo.getNetMask());
                    if (mEthInfo.getIpAddress() != null
                            && mEthInfo.getIpAddress() != "0.0.0.0"
                            && mEthInfo.getIpAddress() != "") {
                        IPAddr.setText(mEthInfo.getIpAddress());
                        Gateway.setText(mEthInfo.getRouteAddr());
                        DNS.setText(mEthInfo.getDnsAddr());
                        Netmask.setText(mEthInfo.getNetMask());
                        GetWay.setText(getResources().getText(
                                R.string.GetwayManul));
                        mbtn_eth.setText(R.string.network_connected);
                        Connection.setText(getActivity().getString(
                                R.string.eth_connection, i));
                        ConnectSpeed.setText(R.string.Connection_text);
                        mWifiConnected = false;
                    }
                }
            }
            Log.d(TAG, " mWifiManager.isWifiEnabled() = " + mWifiManager.isWifiEnabled());
            if (mWifiManager.isWifiEnabled()) {
                mbtn_wireless.setText(R.string.network_noconnected);
            } else {
                mbtn_wireless.setText(R.string.network_disable);
            }
        }
    }
	
	void updateWirelessAddress(){
	    Log.d(TAG, "-----updateWirelessAddress----");
		int level = 4;

		mWifiManager = (WifiManager) getActivity().getSystemService(
				Context.WIFI_SERVICE);
		mWifiConnectInfo = mWifiManager.getConnectionInfo();

		SignalStrengthTitle.setVisibility(View.VISIBLE);
		SignalStrength.setVisibility(View.VISIBLE);
		if(mWifiConnectInfo.getSSID() == null){
			Connection.setText(getActivity().getString(R.string.ethconnect_wait));
		}else{
			Connection.setText(getActivity().getString(R.string.wifi_connection, mWifiConnectInfo.getSSID()));
		}
    	if(mWifiConnectInfo.getLinkSpeed() == -1){
    		ConnectSpeed.setText("0" + WifiInfo.LINK_SPEED_UNITS);    	
    	}else{
    		ConnectSpeed.setText(mWifiConnectInfo.getLinkSpeed() + WifiInfo.LINK_SPEED_UNITS);
    	}
    	
    	int rssi = mWifiConnectInfo.getRssi();
    	level =  WifiManager.calculateSignalLevel(rssi, 5);
        if ((level < 0) || (level > 4)) {
        	level =  5;
        }
        String[] level_arr = getResources().getStringArray(R.array.wifi_level);
		SignalStrength.setText(level_arr[level]);
		DhcpInfo dhcpInfo = mWifiManager.getDhcpInfo();
		IPAddr.setText(getAddress(dhcpInfo.ipAddress));
		Gateway.setText(getAddress(dhcpInfo.gateway));
		DNS.setText(getAddress(dhcpInfo.dns1));
		Netmask.setText(getAddress(dhcpInfo.netmask));
		List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
		IpAssignment ipAssignment = null;
		for(WifiConfiguration config : configs){
			if(config.networkId == mWifiConnectInfo.getNetworkId()){
				ipAssignment = config.ipAssignment;
			}
		}
		
		if(ipAssignment == null){
			GetWay.setText(R.string.wifi_ip_address_dhcp);
		}else{
			if(ipAssignment == IpAssignment.STATIC){
				GetWay.setText(R.string.wifi_ip_address_static);
			}else{
				GetWay.setText(R.string.wifi_ip_address_dhcp);
			}
		}
		
		mbtn_wireless.setText(R.string.network_connected);
		mbtn_eth.setText(R.string.network_disable);
//        if ((mWifiConnectInfo != null) && (mWifiManager.isWifiEnabled())) {
//            mbtn_wireless.setText(R.string.network_noconnected);
//        } else {
//            mbtn_wireless.setText(R.string.network_disable);
//        }
	}
	

	void updateNetworkDisconnected(){
	    Log.d(TAG, "-----updateNetworkDisconnected----");
	    mWifiConnected = false;
	    try {
            Thread.currentThread().sleep(1000);
        } catch (Exception e) {
            // TODO: handle exception
            Log.d(TAG, e.getMessage());
        }
		SignalStrengthTitle.setVisibility(View.INVISIBLE);
		SignalStrength.setVisibility(View.INVISIBLE);
    	Connection.setText(R.string.network_disconnection);
    	ConnectSpeed.setText("");
		SignalStrength.setText("");
		IPAddr.setText("");
		Gateway.setText("");
		DNS.setText("");
		Netmask.setText("");
		GetWay.setText("");
		Log.d(TAG, " mWifiManager.isWifiEnabled() = " + mWifiManager.isWifiEnabled());
		if(mWifiManager.isWifiEnabled()){
            mbtn_wireless.setText(R.string.network_noconnected);
        }else{
            mbtn_wireless.setText(R.string.network_disable);
        }
		mbtn_eth.setText(R.string.network_disable);
	}
	
	void updateNetwordDisplay(){
	    Log.d(TAG, "-----updateNetwordDisplay----");
		if(checkNetwordConnected()){
			mWifiManager = (WifiManager) getActivity().getSystemService(
					Context.WIFI_SERVICE);
			mEthManager = (EthernetManager) getActivity().getSystemService(
					Context.ETH_SERVICE);
			
			mWifiConnectInfo = mWifiManager.getConnectionInfo();
			if((mWifiConnectInfo != null) && (mWifiManager.isWifiEnabled())
					&& (mWifiConnectInfo.getLinkSpeed() != -1)
					&& (mWifiConnectInfo.getSupplicantState() == SupplicantState.COMPLETED)){
				Log.e(TAG,"have wifi connect");
				updateWirelessAddress();
//				mbtn_wireless.setText(R.string.network_connected);
//				mbtn_eth.setText(R.string.network_disable);
            } else if (mEthManager.getEthState() == EthernetManager.ETH_STATE_ENABLED
                    && mEthManager.isEthDeviceAdded()){
				updateEthAddress();
//				mbtn_eth.setText(R.string.network_connected);
//				mbtn_wireless.setText(R.string.network_disable);
			}else{
				updateNetworkDisconnected();
//				mbtn_wireless.setText(R.string.network_disable);
//				mbtn_eth.setText(R.string.network_disable);
			}
		}else{
			updateNetworkDisconnected();
//			mbtn_wireless.setText(R.string.network_disable);
//			mbtn_eth.setText(R.string.network_disable);
		}
		if(getProxyVal() == 0){
			mbtn_proxy.setText(R.string.propty_disable);
		}else{
			mbtn_proxy.setText(R.string.propty_start);
		}
	}
	
    boolean checkNetwordConnected() {
        ConnectivityManager conManager = (ConnectivityManager) getActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfo = conManager.getAllNetworkInfo();
        NetworkInfo activityInfo = conManager.getActiveNetworkInfo();
        if (activityInfo != null) {
            Log.d(TAG, activityInfo.toString());
        }
        if (networkInfo != null) {
            for (int i = 0; i < networkInfo.length; i++) {
                if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
                    Log.d(TAG,
                            " checkNetwordConnected i = "
                                    + i + " net info = " + networkInfo[i].getTypeName());
                    return true;
                }
            }
        }
        return false;
    }
	
	private void handleEvent(Context context, Intent intent) {
	        String action = intent.getAction();
	        Log.d(TAG,"handleEvent action is "+action);
            if (action.equals(EthernetManager.ETH_STATE_CHANGED_ACTION)) {
            	updateEth(context, intent);
            }else{
            	updateWireless(context, intent);
            }
	}

	private void updateEth(Context context, Intent intent) {
        final int event = intent.getIntExtra(EthernetManager.EXTRA_ETH_STATE, EthernetStateTracker.EVENT_HW_DISCONNECTED);
        Log.d(TAG, " -------updateEth ---    begin"  );
        Log.d(TAG, " -------updateEth event=" + event + " mGetIpAddress = " + mGetIpAddress + " mWifiConnected = " + mWifiConnected );
    	//if(mWifiConnected)
    	//	return;
        switch(event){

        case EthernetStateTracker.EVENT_INTERFACE_CONFIGURATION_SUCCEEDED: 
        {
        	if(mWifiConnected == true){
        		updateWirelessAddress();
        		return;
        	}
            EthernetManager ethManager = (EthernetManager) context.getSystemService(Context.ETH_SERVICE);
            if (ethManager.isEthDeviceAdded()) {
            	updateEthAddress();
            	mGetIpAddress = false;
            	mEthernetConnected = true;
            }
            return;
        }
        case EthernetStateTracker.EVENT_INTERFACE_CONFIGURATION_FAILED:
            mEthernetConnected = false;
            if(mWifiConnected == false){
                updateNetworkDisconnected();
            }
            return;
        case EthernetStateTracker.EVENT_HW_CONNECTED:
        case EthernetStateTracker.EVENT_HW_PHYCONNECTED:
            if (mGetIpAddress)
                return;
            break;
        case EthernetStateTracker.EVENT_DHCP_START:
            // There will be a DISCONNECTED and PHYCONNECTED when doing a
            // DHCP request. Ignore them.
        	if(mEthernetConnected)
        		return;
        	mGetIpAddress = true;
            //mEthernetConnected = true;
        	if(!mWifiConnected){
                Connection.setText(R.string.eth_get_ip);
        	}
            return;
        case EthernetStateTracker.EVENT_HW_CHANGED: 
        	if(mWifiConnected){
        		return;
        	}
        	Log.d(TAG,"  update eth hw changed,mEthernetConnected = "+mEthernetConnected);
        	EthernetManager ethManager = (EthernetManager) context.getSystemService(Context.ETH_SERVICE);
	        if (ethManager.isEthDeviceAdded()) {
	        	updateEthAddress();
	        	mGetIpAddress = false;
	        	mEthernetConnected = true;
	        }
            return;
        case EthernetStateTracker.EVENT_HW_DISCONNECTED:
            if (mGetIpAddress)
                return;
            mEthernetConnected  = false;
            if(mWifiConnected == false){
                updateNetworkDisconnected();
            }else{
            	updateWirelessAddress();
            }
            return;
        }
        updateNetwordDisplay();
        Log.d(TAG, " -------updateEth ---    end---"  );
	}
    
    private void updateConnectionState(DetailedState state) {
        /* sticky broadcasts can call this when wifi is disabled */
    	//if(mEthernetConnected){
    	//	return;
    	//}
        Log.d(TAG, "-----updateConnectionState  state = " + state);
        if (!mWifiManager.isWifiEnabled()) {
        	mWifiConnected = false;
            return;
        }
        

        if (state == DetailedState.OBTAINING_IPADDR) {
        	if(mEthernetConnected){
        		return;
        	}
        	if(mGetIpAddress){
        		return;
        	}
        	Connection.setText(R.string.eth_get_ip);
        } else if (state == DetailedState.DISCONNECTED){ 
        	if(mEthernetConnected){
        		updateEthAddress();
        		return;
        	}
        	mWifiConnected = false;
        	updateNetworkDisconnected();
        } else if (state == DetailedState.CONNECTED) { 
        	mWifiConnected = true;
        	mEthernetConnected = false;
        	updateWirelessAddress();
        } else{
        	if(mEthernetConnected){
        		return;
        	}
        	if(mWifiConnected && state == null){
        		Log.e(TAG,"sdjklfjksdljfskdl");
        		updateWirelessAddress();
        	}
        }
    }
	
	private void updateWireless(Context context, Intent intent) {

        String action = intent.getAction();
        Log.d(TAG,"wireless action is "+action);
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            updateWifiState(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN));
        } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {

            if (!mConnected.get()) {
                updateConnectionState(WifiInfo.getDetailedStateOf((SupplicantState)
                        intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE)));
            }

        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(
                    WifiManager.EXTRA_NETWORK_INFO);
            mConnected.set(info.isConnected());
            updateConnectionState(info.getDetailedState());
        } else if (WifiManager.RSSI_CHANGED_ACTION.equals(action)) {
            updateConnectionState(null);
        }
	}
	
	private void updateWifiState(int state) {
		Log.d(TAG,"NetworkWireless handleWifiStateChanged "+"state = "+state);
		switch (state) {
		case WifiManager.WIFI_STATE_DISABLED:
			mWifiConnected = false;
			break;
		default:
			break;
		}

	}
	 public void findFxView(int View_ID)
	  {
	     switch(View_ID)
	   	 {
//	   	 	 case R.id.btn_eth:
//	   	 	 BtnEth.requestFocus();
//	   	 	 break;
//	   	 	 
//	   	 	 case R.id.btn_wireless:
//	   	 	 BtnWirless.requestFocus();
//	   	 	 break;
//	   	 	 
//	   	 	 case R.id.btn_proxy:
//	   	 	 BtnProxy.requestFocus();
//	   	 	 break;
	  
	   	 }
	  }
	
	
	public class ListViewAdapter extends BaseAdapter {
	   	 
		public int[]  mArray;
		private LayoutInflater mLayoutInflater;
		private int selectedPosition = -1;  
		
		public void setSelectedPosition(int position) {   
			selectedPosition = position;   
		}

		public ListViewAdapter(Context context,int[] array) { 
            mArray = array; 
            mLayoutInflater = LayoutInflater.from(context); 
        } 

		@Override
		public int getCount() {
	
			return mArray.length;
		}

		@Override
		public Object getItem(int position) {
	
			return mArray.length;
		}

		@Override
		public long getItemId(int position) {
	
			return position;
		}
 
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
	
			ViewHolder holder = null;
			if (convertView == null) { 
				holder = new ViewHolder();
				convertView = mLayoutInflater.inflate(R.layout.network_list, null); 
				holder.textViewName = (TextView) convertView.findViewById(R.id.network_list_name);
				holder.textViewState = (TextView) convertView.findViewById(R.id.network_list_state);
				holder.textViewMore = (TextView) convertView.findViewById(R.id.network_list_more);
				holder.layout = (RelativeLayout) convertView.findViewById(R.id.networkLayout);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.textViewName.setText(mArray[position]);
			holder.textViewState.setText(R.string.wifi_status_connected);
			holder.textViewMore.setText(">>");
			
			return convertView; 
		}

		public class ViewHolder { 
		    public TextView textViewName;
		    public TextView textViewState;
		    public TextView textViewMore;
		    public RelativeLayout layout;
		} 
    }
	
	private static final String STR_HTTP_PROXY_SWITCHER = "HttpProxy";
	private int getProxyVal(){
		int val = 0;
		ConnectivityManager cm =
		           (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

		ProxyProperties mProxy =  cm.getGlobalProxy();
		if(mProxy!=null){
			val = 1;
		}
		       
//		
//		   SharedPreferences preference = getActivity().getSharedPreferences("com.lenovo.settings", Context.MODE_WORLD_READABLE
//					| Context.MODE_WORLD_WRITEABLE);     
//		   val = preference.getInt(STR_HTTP_PROXY_SWITCHER, 0);//如果取不到值就取值后面的参数 
		return val;
	}
	 
}
