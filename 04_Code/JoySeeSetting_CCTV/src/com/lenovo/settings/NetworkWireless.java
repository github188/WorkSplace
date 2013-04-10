package com.lenovo.settings;

import static android.net.wifi.WifiConfiguration.INVALID_NETWORK_ID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Dialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.IpAssignment;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiConfiguration.ProxySettings;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.util.AsyncChannel;
import com.lenovo.settings.Object.FilterChars;
import com.lenovo.settings.wifi.AccessPoint;
import com.lenovo.settings.wifi.WifiDialog;
import com.lenovo.settings.wifi.WifiUtil;

public class NetworkWireless extends Fragment {

    private static final String KEYSTORE_SPACE = "keystore://";
	private static final String TAG = "NetworkWireless";
	private boolean DEBUG = true;
	private ListView lv;
	private ArrayList<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
	// private ListViewAdapter listViewAdapter;
	private View mView;
	private LinearLayout wirelessAdd;
	private TextView mwifiSpinner;
//	private TextView mrefresh;
	private WifiManager mWifiManager;
	private IntentFilter mFilter;
	private WifiListAdapter mListAdapter;
	private AtomicBoolean mConnected = new AtomicBoolean(false);
	private DetailedState mLastState;
	private WifiInfo mLastInfo;
	private final Scanner mScanner;
	private final BroadcastReceiver mReceiver;
	private  List<ScanResult> restltList;
	private static final int WIFI_RESCAN_INTERVAL_MS = 5 * 1000;
	private static final int WIFI_SECURITY_NONE = 0;
	private static final int WIFI_SECURITY = 1;
	private static final int WIFI_CONNECTED = 2;
	private static final int WIFI_SAVED = 3;
	private static final int WIFI_NOT_SAVED = 3;
	private static final int WIFI_NOT_IN_RANGE = 4;
	private static int mIndexWireless = 0;
	int wifiStatus = -1;

    
	private int[] imgeIDs = { R.drawable.wifi_lock_signal_1,
			R.drawable.wifi_lock_signal_2, R.drawable.wifi_lock_signal_3,
			R.drawable.wifi_lock_signal_4, R.drawable.wifi_lock_signal_5 };
	private ArrayList<AccessPoint> mAccessPoints = new ArrayList<AccessPoint>();
	public static Object bLock = new Object();
	public boolean lock = false;
    private AccessPoint mSelectedAccessPoint = null;
    
    private IpAssignment mIpAssignment = IpAssignment.UNASSIGNED;
    private ProxySettings mProxySettings = ProxySettings.UNASSIGNED;
    private LinkProperties mLinkProperties = new LinkProperties();
	private ListView mDropdownList;
	private RelativeLayout mListLayout;
	private ArrayAdapter<CharSequence> mAdapter;
	protected boolean mListEnd = false;
	protected boolean mEnableList = false;
	private DropdownMenu mDropMenu;
	private String[] mWirelessArrays;
	private WifiDialog mWifiDialog = null;
	
	private TextView mTextviewAdd;
	private static final int HandlerMSG_DISCONNECT = 100;
	private static final int HandlerMSG_CONNECTED = 101;
	private static final int HandlerMSG_AUTHENTICATING_ERROR = 102;
	/**首次进入界面不弹出连接成功提示*/
	private boolean isFirst = true;
	/**是否正在显示Dialog*/
	private boolean isShowing = false;
  //Hazel add {
   ItemInfo mIT;
  //Hazel add }
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
	
		    Log.d(TAG," handleMessage msg = " + msg.toString());
			switch(msg.what){
			case DropdownMenu.DROPDOWN_MENU_ITEM_CHICK:
				if(msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_CAN_EDIT){
					EditText edit = (EditText) msg.obj;
				}else if(msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_NOT_EDIT){
					TextView text = (TextView) msg.obj;
					int position = msg.arg2;
					if(text == mwifiSpinner){
						  if(mIndexWireless != position){
							  mIndexWireless = position;
								if (position == 1)
									mWifiManager.setWifiEnabled(false);
								else
									mWifiManager.setWifiEnabled(true);
								wirelessAdd.setEnabled((mIndexWireless == 0) ? true : false);
								wirelessAdd.setFocusable((mIndexWireless == 0) ? true : false);
								mTextviewAdd.setAlpha((mIndexWireless == 0) ? 1f : 0.3f);
						  }
					}else{
						break;
					}
				}
				break;
			case DropdownMenu.DROPDOWN_MENU_GET_ADAPTER:
				if(msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_CAN_EDIT){
					EditText edit = (EditText) msg.obj;
				}else if(msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_NOT_EDIT){
					TextView text = (TextView) msg.obj;
					if(text == mwifiSpinner){
						mDropMenu.setListViewPosition(0, 2);
						mDropMenu.setListViewAdapter(mWirelessArrays, mIndexWireless);
					}else{
						break;
					}
					mDropMenu.showDropdownListEnable(true);
				}
				break;
			case HandlerMSG_DISCONNECT:
			    if(!isShowing){
			        showToast(getActivity(), R.string.no_net_connect);
			    }
			    break;
			case HandlerMSG_CONNECTED:
			    if(!isFirst){
			        if(!isShowing){
//			            showToast(getActivity(), R.string.wifi_connect_success);
			        }
			    }
			    break;
			case HandlerMSG_AUTHENTICATING_ERROR:
                if (!isShowing) {
                    if (mSelectedAccessPoint != null) {
                        showDialog(mSelectedAccessPoint, 1);
                    }
                }
			    break;
			}
		}
		
	};
	
@Override
	public void onDestroy() {

		super.onDestroy();
		Log.d(TAG,"###onDestroy!!");
		//list is dirty. need clean
		ConfigFocus.Items_t.clear();
		if(mHandler!=null){
		    mHandler.removeMessages(HandlerMSG_DISCONNECT);
	        mHandler.removeMessages(HandlerMSG_CONNECTED);
	        mHandler.removeMessages(HandlerMSG_AUTHENTICATING_ERROR);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		
		getActivity().getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		mView = inflater.inflate(R.layout.network_wireless, container, false);
		mwifiSpinner = (TextView) mView.findViewById(R.id.wifiSpinner);
//		mrefresh = (TextView) mView.findViewById(R.id.wifi_refresh);
		mDropdownList = (ListView) mView.findViewById(R.id.dropdownlist);
		mListLayout = (RelativeLayout) mView.findViewById(R.id.listLayout);
		wirelessAdd = (LinearLayout) mView.findViewById(R.id.btn_wirelessadd);
		mTextviewAdd = (TextView) mView.findViewById(R.id.textview_add);
		

		mDropMenu = new DropdownMenu(getActivity(),mView,mHandler );
		mWirelessArrays = getResources().getStringArray(R.array.frequency_array);
		mIndexWireless = getWifiStatus();
		wirelessAdd.setEnabled((mIndexWireless == 0) ? true : false);
		wirelessAdd.setFocusable((mIndexWireless == 0) ? true : false);
		mTextviewAdd.setAlpha((mIndexWireless == 0) ? 1f : 0.3f);

		mDropMenu.setButtonListener(mwifiSpinner,mWirelessArrays[mIndexWireless]);
		mDropMenu.setListViewListener();
	
		
		lv = (ListView) mView.findViewById(R.id.wifi_list);
//		lv.setDivider(null);
//		lv.setDividerHeight(0);
		mListAdapter = new WifiListAdapter(getActivity(), listItems);
		lv.setAdapter(mListAdapter);
		lock = true;
		lv.setOnItemClickListener(new OnItemClickListener(){ 
	    	 public void onItemClick(AdapterView<?> parent, View view, 
	    			 		int position, long id) {  
	    		 Log.e(TAG,"position = "+position);
	    		 AccessPoint accesspoint = mAccessPoints.get(position);
	    		 Log.e(TAG,"ssid = "+accesspoint.getSSID());
	    		 
	    		 /*DetailedState state = accesspoint.getState();
	    		 if((state == DetailedState.AUTHENTICATING) || (state == DetailedState.CONNECTING) ||
	    				 (state == DetailedState.DISCONNECTING) || (state == DetailedState.OBTAINING_IPADDR)){
	    			 Log.e(TAG,"wifi is setting,can not click!");
	    			 return;
	    		 }*/
	    		 
	    		 Log.e(TAG,"Security = "+accesspoint.getSecurity());
	    		 mSelectedAccessPoint = accesspoint;

				 showDialog(accesspoint,0);
	    		 /*if(accesspoint.getSecurity() == AccessPoint.SECURITY_EAP){
	    			 if((accesspoint.getConfig() != null) 
	    					 || (accesspoint.getState() == DetailedState.CONNECTED)){
		    			 showWifiConnectDialog(getActivity(),accesspoint.getSSID(),
		    					 accesspoint.getLevel(),accesspoint.getSecurity());
	    			 }else{
	    				 showDialog(accesspoint);
	    			 }
	    		 }else{
	    			 showWifiConnectDialog(getActivity(),accesspoint.getSSID(),
	    					 accesspoint.getLevel(),accesspoint.getSecurity());
	    		 }*/
	    		 /*if (WifiUtil.getSecurity(res) == WifiUtil.SECURITY_NONE) {
	                 mWifiManager.connectNetwork(WifiUtil.generateOpenNetworkConfig(res));
	             }else{
	            	 showWifiConnectDialog(getActivity(),mView);
	             }
	            */ 
	            // else
	    		// WifiConfiguration config = WifiUtil.CreateWifiInfo(res.SSID,"54321geniatech",WifiUtil.getSecurity(res));
	    		// mWifiManager.connectNetwork(config);
	    		// WifiInfo  info = mWifiManager.getConnectionInfo();
	    	 }
	    	}); 



		

		wirelessAdd.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
		
				Fragment fragment = (Fragment) new NetworkWirelessAdd();
				SettingFragment sf = (SettingFragment) new SettingFragment(
						getActivity());
				sf.setFragment(fragment,true);
			 mIT.VId=R.id.btn_wirelessadd;
			}
		});

		//mwifiSpinner.requestFocus();
		//mwifiSpinner.setFocusable(true);
		//mwifiSpinner.setFocusableInTouchMode(true);

		// reSetListViewHeight(lv);
		LenovoSettingsActivity.setTitleFocus(false);
		
		//Hazel add {
	   mIT=new ItemInfo();
		if(ConfigFocus.Items_t.size()==0)
	 	{
	 		Log.d(TAG,"^^^ConfigFocus.Items_t.size()==0");
	 			mwifiSpinner.requestFocus();
	 		  mIT.VId=R.id.wifiSpinner;
	 		  ConfigFocus.Items_t.add(mIT);
		}
		else
		{
			 ItemInfo gItemId=ConfigFocus.Items_t.get(0);	
			 findFxView(gItemId.VId);
			 Log.d(TAG,"$$$ConfigFocus.Items_t.size()!=NULL");
		}
		//Hazel add }
    	ConfigFocus.Items_t.set(0,mIT);
    	isFirst = true;
		return mView;

	}

	private void getListItems(ArrayList<AccessPoint> accesspoints) {
		synchronized(bLock){
			listItems.clear();
			//ArrayList<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
			if(accesspoints==null)
				return ;
	
			/*for(int i=0;i<accesspoints.size();i++){
				WifiConfiguration config = accesspoints.get(i).getConfig();
				Map<String, Object> map = new HashMap<String, Object>();
				if(config != null){
					Log.d(TAG,"SSID = "+config.SSID);
					map.put("connect", 1);
					map.put("title", AccessPoint.removeDoubleQuotes(config.SSID));
					map.put("secure", WifiUtil.getSecurity(config));
					map.put("signal" ,config);
					listItems.add(map);
				}
			}*/
			for(AccessPoint accesspoint : accesspoints){
				Map<String, Object> map = new HashMap<String, Object>();
				String secure_str;
				boolean lock = true;
				//Log.d(TAG,"SSID = "+accesspoint.getSSID());
				/*if(accesspoint.getState() != null){
					mSelectedAccessPoint = accesspoint;
					//mrefresh.setText(getActivity().getResources().getString(
					//		R.string.wifi_connected_prompt,mSelectedAccessPoint.getSSID()));
				}
				map.put("connect", accesspoint.getInfo() == null ? 0 : 1);
				map.put("title", accesspoint.getSSID());
				map.put("secure", accesspoint.getSecurity());
				map.put("signal" ,accesspoint.getRssi());*/

				switch(accesspoint.getSecurity()){
		        case 0:
		        default:
		        	secure_str = getActivity().getResources().getString(R.string.wifi_secure_none);
		        	lock = false;
		        	break;
		        case 1:
		        	secure_str = getActivity().getResources().getString(R.string.wifi_secure_wep);
		        	break;
		        case 2:
		        	secure_str = getActivity().getResources().getString(R.string.wifi_secure_wpa);
		        	break;
		        case 3:
		        	secure_str = getActivity().getResources().getString(R.string.wifi_secure_eap);
		        	break;
				}
				map.put("connect", (accesspoint.getState() == DetailedState.CONNECTED) ? 1 : 0);
				map.put("title", accesspoint.getSSID());
				map.put("signal" ,accesspoint.getRssi());
//				Log.d(TAG, "-----------accesspoint.getState() = " + accesspoint.getState());
				if(accesspoint.getState() == DetailedState.CONNECTED){
					String str = getActivity().getResources().getString(R.string.wifi_connected) + secure_str;
					map.put("secure", str);
				}else if(accesspoint.getState() == DetailedState.OBTAINING_IPADDR){
					String str = getActivity().getResources().getString(R.string.wifi_obtaining_ipaddr);
					map.put("secure", str);
                } else if (accesspoint.getState() == DetailedState.AUTHENTICATING
                        || accesspoint.getState() == DetailedState.CONNECTING){
                    String str = getActivity().getResources().getString(R.string.wifi_status_checking);
                    map.put("secure", str);
                }else if(accesspoint.getRssi() == Integer.MAX_VALUE){
					String str = getActivity().getResources().getString(R.string.wifi_no_range);
					map.put("secure", str);
				}else if(accesspoint.getConfig() != null){
					String str = getActivity().getResources().getString(R.string.wifi_has_save) + secure_str;
					map.put("secure", str);
                } else{
					map.put("secure", secure_str);
				}
				map.put("lock", lock);
				listItems.add(map);
			}
			//listItems = items;
			//mListAdapter.notifyDataSetChanged();
		}
	}



	public static void reSetListViewHeight(ListView listView) {

		ListAdapter listAdapter = listView.getAdapter();

		if (listAdapter == null) {

			// pre-condition
			return;
		}

		int totalHeight = 0;

		for (int i = 0; i < listAdapter.getCount(); i++) {

			View listItem = listAdapter.getView(i, null, listView);

			listItem.measure(0, 0);

			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();

		params.height = totalHeight
				+ (listView.getDividerHeight() * ((listAdapter.getCount() + 1) - 1));
		Log.i("clei", "params.height = " + params.height);
		listView.setLayoutParams(params);

	}

    public NetworkWireless() {
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

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleEvent(context, intent);
            }
        };
        mScanner = new Scanner();
        
    }
	
	
	
	
	public int getWifiStatus() {
		mWifiManager = (WifiManager) getActivity().getSystemService(
				Context.WIFI_SERVICE);
		 mWifiManager.asyncConnect(getActivity(), new WifiServiceHandler());
		Log.i("getWifiStatus", "getWifiState = " + mWifiManager.getWifiState());
		switch (mWifiManager.getWifiState()) {
		case WifiManager.WIFI_STATE_ENABLING:
//			mrefresh.setText(R.string.wifi_enabling);
			return 0;
		case WifiManager.WIFI_STATE_ENABLED:
//			mrefresh.setText("");
			return 0;
		default:
//			mrefresh.setText(R.string.wireless_failed);
			return 1;

		}
		
	}

    private class WifiServiceHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AsyncChannel.CMD_CHANNEL_HALF_CONNECTED:
                    if (msg.arg1 == AsyncChannel.STATUS_SUCCESSFUL) {
                        //AsyncChannel in msg.obj
                    } else {
                        //AsyncChannel set up failure, ignore
                        Log.e("CMD_CHANNEL_HALF_CONNECTED", "Failed to establish AsyncChannel connection");
                    }
                    break;
                case WifiManager.CMD_WPS_COMPLETED:
                    WpsResult result = (WpsResult) msg.obj;
                    Log.i("clei","");
                    break;
                //TODO: more connectivity feedback
                default:
                    //Ignore
                    break;
            }
        }
    }
    
    
    private void updateWifiState(int state) {
        if (DEBUG) {
            Log.i(TAG, "------------ updateWifiState " + "state = "
                    + state);
        }
        switch (state) {
        case WifiManager.WIFI_STATE_ENABLING:
            // mrefresh.setText(R.string.wifi_enabling);
            break;
        case WifiManager.WIFI_STATE_ENABLED:
            // mrefresh.setText("");
            mScanner.resume();
            return;
        case WifiManager.WIFI_STATE_DISABLING:
            // mrefresh.setText(R.string.wifi_disabling);
            break;
        case WifiManager.WIFI_STATE_DISABLED:
            listItems.clear();
            mListAdapter.setAdapterData(listItems);
            mListAdapter.notifyDataSetChanged();
            // mrefresh.setText(R.string.wifi_disabled);
            break;
        default:
            break;
        }
        mLastInfo = null;
        mLastState = null;
        mScanner.pause();

    }
	
    private void updateAccessPoints() {
        final int wifiState = mWifiManager.getWifiState();
        // mrefresh.setText(R.string.wirelessRefresh);
        if (DEBUG) {
            Log.i(TAG,
                    "------------- updateAccessPoints wifiState = "
                            + wifiState);
        }
        switch (wifiState) {
        case WifiManager.WIFI_STATE_ENABLED:
            // AccessPoints are automatically sorted with TreeSet.
            final Collection<AccessPoint> accessPoints = constructAccessPoints();
            mAccessPoints = (ArrayList<AccessPoint>) accessPoints;
            getListItems(mAccessPoints);
            break;

        case WifiManager.WIFI_STATE_ENABLING:
            listItems.clear();
            break;
        }
        // Log.d(TAG,"listItems size = "+listItems.size());
        mListAdapter.setAdapterData(listItems);
        mListAdapter.notifyDataSetChanged();

    }

    /** Returns sorted list of access points */
    private List<AccessPoint> constructAccessPoints() {
    	List<AccessPoint> accessPoints = new ArrayList<AccessPoint>();
        
        /** Lookup table to more quickly update AccessPoints by only considering objects with the
         * correct SSID.  Maps SSID -> List of AccessPoints with the given SSID.  */

        final List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        if (configs != null) {
            Log.d(TAG, " ------------------getConfiguredNetworks List size = " + configs.size());
            for (WifiConfiguration config : configs) {
            	Log.e(TAG,"config ssid = "+config.SSID);
                AccessPoint accessPoint = new AccessPoint(getActivity(), config);
                accessPoint.update(mLastInfo, mLastState);
                if(mAccessPoints != null){
	                for(AccessPoint ap : mAccessPoints){
	                	if(ap.ssid.equals(accessPoint.ssid) && 
	                			(ap.security == accessPoint.security)){
	                		accessPoint.setRssi(ap.getRssi());
	                	}
	                }   
                }
                accessPoints.add(accessPoint);
            }
        }else{
            Log.d(TAG, " -----------get WifiConfiguration List Error!!!!!!!!!!!!!!!!!! ");
        }

        final List<ScanResult> results = mWifiManager.getScanResults();
        if (results != null) {
            Log.d(TAG, " ------------------getScanResults List size = " + results.size());
            for (ScanResult result : results) {
                // Ignore hidden and ad-hoc networks.
                if (result.SSID == null || result.SSID.length() == 0 ||
                        result.capabilities.contains("[IBSS]")) {
                    continue;
                }

            	//Log.d(TAG,"result ssid = "+result.SSID);
                boolean found = false;
                for (AccessPoint accessPoint : accessPoints) {
                    if (accessPoint.update(result))
                        found = true;
                }
                if (!found) {
                    AccessPoint accessPoint = new AccessPoint(getActivity(), result);
                    accessPoints.add(accessPoint);
                }
            }
        }else{
            Log.d(TAG, " -----------get ScanResult List Error!!!!!!!!!!!!!!!!!! ");
        }

        // Pre-sort accessPoints to speed preference insertion
        Mycomparator comp = new Mycomparator();
        Collections.sort(accessPoints,comp);
        return accessPoints;
    }
    /**
     * @param state
     * @param errorCode 验证密码返回消息，为1则表示验证失败。
     */
    private void updateConnectionState(DetailedState state ,int errorCode) {
        /* sticky broadcasts can call this when wifi is disabled */
        if (!mWifiManager.isWifiEnabled()) {
            mScanner.pause();
            return;
        }
        if (DEBUG) {
            Log.d(TAG, "-----------updateConnectionState DetailedState = "
                    + state + " errorCode = " + errorCode);
        }
        if (state == DetailedState.OBTAINING_IPADDR) {
//        	mrefresh.setText(getActivity().getResources().
//        			getString(R.string.wifi_get_ip,mWifiManager.getConnectionInfo().getSSID()));
            mScanner.pause();
        } else if (state == DetailedState.CONNECTED){
            mHandler.removeMessages(HandlerMSG_DISCONNECT);
            mHandler.removeMessages(HandlerMSG_AUTHENTICATING_ERROR);
            mHandler.sendEmptyMessage(HandlerMSG_CONNECTED);
        	WifiInfo info = mWifiManager.getConnectionInfo();
//        	mrefresh.setText(getActivity().getString(R.string.wifi_connected_prompt, info.getSSID()));
        }else if (state == DetailedState.CONNECTING){ 
            mHandler.removeMessages(HandlerMSG_DISCONNECT);
            mHandler.removeMessages(HandlerMSG_AUTHENTICATING_ERROR);
        } else if (state == DetailedState.SCANNING){ 
//        	mrefresh.setText(R.string.wirelessRefresh);
        } else if (state == DetailedState.DISCONNECTED){ 
//        	mrefresh.setText("");
            if (errorCode == 1) {
                mHandler.removeMessages(HandlerMSG_DISCONNECT);
                mHandler.removeMessages(HandlerMSG_AUTHENTICATING_ERROR);
                mHandler.sendEmptyMessageDelayed(HandlerMSG_AUTHENTICATING_ERROR,1000);
            }else{
                mHandler.removeMessages(HandlerMSG_DISCONNECT);
                mHandler.removeMessages(HandlerMSG_AUTHENTICATING_ERROR);
                mHandler.sendEmptyMessageDelayed(HandlerMSG_DISCONNECT, 5000);
            }
        } else if (state == DetailedState.IDLE){ 
//        	mrefresh.setText("");
        }else{
        	mScanner.resume();
        }
        

        mLastInfo = mWifiManager.getConnectionInfo();
        if (state != null) {
            mLastState = state;
        }
        if(mAccessPoints != null){
	        for(AccessPoint accesspoint : mAccessPoints){
	        	//Log.e(TAG,"123456  accesspoint rssi = "+accesspoint.getRssi());
	        	int rssi = accesspoint.getRssi();
	        	accesspoint.update(mLastInfo, mLastState);
	        	state = accesspoint.getState();
	        	if((state == DetailedState.OBTAINING_IPADDR) || 
	        			(state == DetailedState.AUTHENTICATING) ||
	        			(state == DetailedState.CONNECTING) || 
	        			(state == DetailedState.CONNECTED) || 
	        			(state == DetailedState.DISCONNECTING)){
	        		accesspoint.setRssi(rssi);
	        	}
	        	//Log.e(TAG,"111111  accesspoint rssi = "+accesspoint.getRssi());
	        }
        }

    }
    
	@Override
	public void onStart() {
		getActivity().registerReceiver(mReceiver, mFilter);
		Log.v("NetworkWireless", "onStart");
		super.onResume();
	}
	
	@Override
	public void onStop() {
		getActivity().unregisterReceiver(mReceiver);
		Log.v("NetworkWireless", "onStop");
        if (mWifiDialog != null) {
            mWifiDialog.dismiss();
            mWifiDialog = null;
            isShowing = false;
        }
		super.onStop();
	}

	
    private void handleEvent(Context context, Intent intent) {
        String action = intent.getAction();
        if (DEBUG) {
            Log.d(TAG, "--------------handleEvent wireless action = " + action);
        }
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            updateWifiState(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN));
        } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)
                || WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION
                        .equals(action)
                || WifiManager.LINK_CONFIGURATION_CHANGED_ACTION.equals(action)) {
             updateAccessPoints();
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                updateAccessPoints();
                // if(getActivity().getString(R.string.wirelessRefresh).equals(
                // mrefresh.getText().toString())){
                // mrefresh.setText("");
                // }
            }
        } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
            if (!mConnected.get()) {
                if (DEBUG) {
                    Log.d(TAG,
                            "--------------handleEvent supplicant_state_changed_action");
                }
                updateConnectionState(
                        WifiInfo.getDetailedStateOf((SupplicantState) intent
                                .getParcelableExtra(WifiManager.EXTRA_NEW_STATE)),
                        intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR,
                                0));
                updateAccessPoints();
            }

        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            NetworkInfo info = (NetworkInfo) intent
                    .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            mConnected.set(info.isConnected());
            // changeNextButtonState(info.isConnected())
            DetailedState state= info.getDetailedState();
            if (DEBUG) {
                Log.d(TAG,
                        "--------------handleEvent network_state_changed_action " + state);
            }
            updateConnectionState(state,0);
            updateAccessPoints();
        } else if (WifiManager.RSSI_CHANGED_ACTION.equals(action)) {
            updateConnectionState(null,0);
        } else if (WifiManager.ERROR_ACTION.equals(action)) {
            int errorCode = intent.getIntExtra(WifiManager.EXTRA_ERROR_CODE, 0);
            if (false) {
                Log.d(TAG, "--------------handleEvent extra_error_code  = " + errorCode);
            }
            switch (errorCode) {
            case WifiManager.WPS_OVERLAP_ERROR:
                Toast.makeText(context, R.string.wifi_wps_overlap_error,
                        Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }
    
    
    private class Scanner extends Handler {
        private int mRetry = 0;

        void resume() {
            if (!hasMessages(0)) {
                sendEmptyMessage(0);
            }
        }

        void forceScan() {
            removeMessages(0);
            sendEmptyMessage(0);
        }

        void pause() {
            mRetry = 0;
            removeMessages(0);
        }

        @Override
        public void handleMessage(Message message) {
            if (mWifiManager.startScanActive()) {
                mRetry = 0;
            } else if (++mRetry >= 3) {
                mRetry = 0;
                Toast.makeText(getActivity(), R.string.wifi_fail_to_scan,
                        Toast.LENGTH_LONG).show();
                return;
            }
            sendEmptyMessageDelayed(0, WIFI_RESCAN_INTERVAL_MS);
        }
    }	
	  
	public class WifiListAdapter extends BaseAdapter {    
	    private static final String TAG = "WifiListAdapter";
		private Context context;                        
	    private ArrayList<Map<String, Object>> listItems;    
	    private LayoutInflater listContainer;         
	    public final class ListItemView{                    
	            public ImageView image;      
	            public TextView title;        
	            public TextView secure;      
	            public ImageView signal;    
	        
	     }      
	        
	    public WifiListAdapter(Context context, ArrayList<Map<String, Object>> listItems) {    
	        this.context = context;             
	        listContainer = LayoutInflater.from(context);  
			//synchronized(NetworkWireless.bLock){   
				this.listItems = (ArrayList<Map<String, Object>>) listItems.clone(); 
			//}
	    }  
	    
	    public void setAdapterData(ArrayList<Map<String, Object>> listItems){
	    	this.listItems = (ArrayList<Map<String, Object>>) listItems.clone();
	    }
	   
	    public int getCount() {    
	           
			//synchronized(bLock){    
				return this.listItems.size();    
			//}
	    }    
	   
	    public Object getItem(int arg0) {    
	            
	        return null;    
	    }    
	   
	    public long getItemId(int arg0) {    
	            
	        return 0;    
	    }
	        
	           
	    /**   
	     * ListView Item
	     */   
	    public View getView(int position, View convertView, ViewGroup parent) {    
	        
	        final int selectID = position; 
	        if(position == (this.listItems.size() - 1)){
	        	lock = false;
	        }
			/*synchronized(bLock)*/{      
		        ListItemView  listItemView = null;    
		        if (convertView == null) {    
		            listItemView = new ListItemView();     
		            convertView = listContainer.inflate(R.layout.wifi_list_item, null);    
		            listItemView.image = (ImageView)convertView.findViewById(R.id.imageItem);    
		            listItemView.title = (TextView)convertView.findViewById(R.id.titleItem);     
		            listItemView.secure = (TextView)convertView.findViewById(R.id.secureItem);    
		            listItemView.signal = (ImageView)convertView.findViewById(R.id.signalItem);         
		            convertView.setTag(listItemView);
		        }else {    
		            listItemView = (ListItemView)convertView.getTag();    
		        }    
		        //Log.d(TAG,"listItems size = "+this.listItems.size()+" position = "+position);
		        //if(position >= this.listItems.size()){
		        //	return convertView;
		        //}
		        Map<String, Object> item = this.listItems.get(position);
		        int connect = (Integer)item.get("connect");
		         if(connect==0)
		        	listItemView.image.setImageDrawable(null);
		         else
		            listItemView.image.setImageResource(R.drawable.wireless_sel);  
		        /*switch((Integer)item.get("secure")){
			        case 0:
			        default:
			        	listItemView.secure.setText(R.string.wifi_secure_none);
			        	break;
			        case 1:
			        	listItemView.secure.setText(R.string.wifi_secure_wep);
			        	break;
			        case 2:
			        	listItemView.secure.setText(R.string.wifi_secure_wpa);
			        	break;
			        case 3:
			        	listItemView.secure.setText(R.string.wifi_secure_eap);
			        	break;
		        }*/

		        listItemView.secure.setText((String) item.get("secure"));
		        listItemView.title.setText((String) item.get("title"));
		        
		        
		        //ScanResult result = (ScanResult) item.get("signal");
		        
		
		        setImageSource(listItemView.signal,(Integer)item.get("signal"),(Boolean)item.get("lock"));
		        
		       // listItemView.signal.setBackgroundResource((Integer) listItems.get(    
		        //        position).get("signal")); 
			}    
	        return convertView;    
	    }   
	   
	    
	    private void setImageSource(ImageView signal,int mRssi ,boolean lock)
	    {
	    	
	        /*if (mRssi == Integer.MAX_VALUE) {
	            signal.setImageDrawable(null);
	        } else */{
	            signal.setImageLevel(WifiUtil.getLevel(mRssi));//(WifiUtil.getLevel(mRssi));
	            signal.setImageResource(R.drawable.wifi_signal);
	            signal.setImageState(lock ? WifiUtil.STATE_SECURED : WifiUtil.STATE_NONE, true);
	        }
	    }
	}   

    private void saveNetwork(WifiConfiguration config) {
        mWifiManager.saveNetwork(config);
    }


    
	private void showWifiConnectDialog(Context context,String ssid,final int level,final int security){
		final Dialog dialog;
		final Button btnConfirm;
		Button btnCancel;
		TextView tv_ssid,tv_security,tv_level;
		final TextView ed_password;
		CheckBox cb_password;
		Log.e(TAG,"level is "+level);
		dialog = new Dialog(context,R.style.DialogStyle);
		if(mSelectedAccessPoint.getState() == DetailedState.CONNECTED){
			wifiStatus = WIFI_CONNECTED;
		}else if(mSelectedAccessPoint.getConfig() != null){
			wifiStatus = WIFI_SAVED;
		}else if(mSelectedAccessPoint.getSecurity() == AccessPoint.SECURITY_NONE){
			wifiStatus = WIFI_SECURITY_NONE;
		}else{
			wifiStatus = WIFI_SECURITY;
		}
        switch(wifiStatus){
        case WIFI_SECURITY_NONE:
        case WIFI_SAVED:
            dialog.setContentView(R.layout.wifi_connect_none_dialog);
        	break;
        case WIFI_SECURITY:
            dialog.setContentView(R.layout.wifi_connect_dialog);
        	break;
        case WIFI_CONNECTED:
            dialog.setContentView(R.layout.wifi_connected_dialog);
        	break;
        }
        //dialog.setCancelable(true);
        tv_ssid = (TextView) dialog.findViewById(R.id.text_ssid);
        tv_level = (TextView) dialog.findViewById(R.id.text_wifi_level);
        tv_security = (TextView) dialog.findViewById(R.id.text_wifi_security); 
        Log.d(TAG,"dialog security = "+security);
        String[] security_arr = getResources().getStringArray(R.array.wifi_security);

    	
        switch(security){
        case 0:
        default:
        	//tv_security.setText(R.string.wifi_secure_none);
        	tv_security.setText(security_arr[0]);
        	break;
        case 1:
        	//tv_security.setText(R.string.wifi_secure_wep);
        	tv_security.setText(security_arr[1]);
        	break;
        case 2:
        	//tv_security.setText(R.string.wifi_secure_wpa);
        	tv_security.setText(security_arr[2]);
        	break;
        case 3:
        	//tv_security.setText(R.string.wifi_secure_eap);
        	tv_security.setText(security_arr[3]);
        	break;
        }
        String[] level_arr = getResources().getStringArray(R.array.wifi_level);
        switch(level){
        case 0:
        	//tv_level.setText(R.string.wifi_secure_none);
        	tv_level.setText(level_arr[0]);
        	break;
        case 1:
        	//tv_level.setText(R.string.wifi_secure_wep);
        	tv_level.setText(level_arr[1]);
        	break;
        case 2:
        	//tv_level.setText(R.string.wifi_secure_wpa);
        	tv_level.setText(level_arr[2]);
        	break;
        case 3:
        	//tv_level.setText(R.string.wifi_secure_eap);
        	tv_level.setText(level_arr[3]);
        	break;
        case 4:
        	//tv_level.setText(R.string.wifi_secure_eap);
        	tv_level.setText(level_arr[4]);
        	break;
        default:
        	tv_level.setText(level_arr[5]);
        	break;
        }
        //tv_security.setText(context.getResources().getStringArray(R.array.wifi_security)[security]);
        //tv_level.setText(context.getResources().getStringArray(R.array.wifi_level)[level]);
        tv_ssid.setText(ssid);
        btnConfirm = (Button) dialog.findViewById(R.id.btn_dlg_confirm);        
        btnCancel = (Button) dialog.findViewById(R.id.btn_dlg_cancel);
        btnCancel.setText(R.string.dlg_cancel);
        switch(wifiStatus){
        case WIFI_SECURITY_NONE:
        	btnConfirm.setText(R.string.wifi_connect);
        	break;
        case WIFI_SECURITY:
        	setConfirmButtonEnable(btnConfirm,false);
            ed_password = (TextView) dialog.findViewById(R.id.ed_wifi_password);
            cb_password = (CheckBox) dialog.findViewById(R.id.checkBox_password);
        	setPasswordHint(security,ed_password);
        	setPasswordMaxLength(security,ed_password);
            if(cb_password.isChecked()){
    			ed_password.setInputType(InputType.TYPE_CLASS_TEXT |InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);   
    		}else{
    			ed_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);  
    		}
        	ed_password.setKeyListener(FilterChars.numberKeyListener);
            ed_password.addTextChangedListener(new TextWatcher() {
			    String tmp = "";   
			    String digits = getActivity().getString(R.string.name_password_digits); 
			    
				@Override
				public void afterTextChanged(Editable s) {
			
					 
			       /* Log.d(TAG, "<><>afterTextChanged<><>" + s.toString());   
			           
			        String str = s.toString();   
			        if(str.equals(tmp)){   
			            return;   
			        }   
			           
			        StringBuffer sb = new StringBuffer();   
			        for(int i = 0; i < str.length(); i++){   
			            if(digits.indexOf(str.charAt(i)) >= 0){   
			                sb.append(str.charAt(i));   
			            }   
			        }
			        tmp = sb.toString(); 
			        */
			        //ed_password.setTextKeepState(tmp);
			        //ed_password.setText(tmp);
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
			
					tmp = s.toString();   
				}

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) { 
					if(security != AccessPoint.SECURITY_NONE){
						int min_size = 1;
						int max_size = 16;
						if(security == AccessPoint.SECURITY_PSK){
							min_size = 8;
							max_size = 64;
						}
						String password = ed_password.getText().toString();
						int password_len = password.getBytes().length;
						if((password_len < min_size) || (password_len > max_size)){
							setConfirmButtonEnable(btnConfirm,false);	
						}else{
							setConfirmButtonEnable(btnConfirm,true);	
						}
					}else{
						setConfirmButtonEnable(btnConfirm,true);
					}
				}
            	
            });
            cb_password.setOnCheckedChangeListener(new OnCheckedChangeListener() {

    			@Override
    			public void onCheckedChanged(CompoundButton buttonView,
    					boolean isChecked) {
    		
    				if(isChecked){
    					ed_password.setInputType(InputType.TYPE_CLASS_TEXT |InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);   
    				}else{
    					ed_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);  
    				}
    				ed_password.setKeyListener(FilterChars.numberKeyListener);
    			}
            	
            });
            /*if(mSelectedAccessPoint.getState() != null){
            	btnConfirm.setText(R.string.wifi_forget);         	
            }else{*/
            	btnConfirm.setText(R.string.wifi_connect);
            //}
        	break;
        case WIFI_CONNECTED:
        	btnConfirm.setText(R.string.wifi_forget);
        	TextView tv_speed = (TextView) dialog.findViewById(R.id.text_wifi_speed);
        	TextView tv_ipaddr = (TextView) dialog.findViewById(R.id.text_wifi_ipaddr);
        	tv_ipaddr.setText(Formatter.formatIpAddress(mSelectedAccessPoint.getInfo().getIpAddress()));
        	tv_speed.setText(mSelectedAccessPoint.getInfo().getLinkSpeed() + WifiInfo.LINK_SPEED_UNITS);
        	break;
        case WIFI_SAVED:
        	if(level == -1){
            	btnConfirm.setText(R.string.wifi_forget);
        	}else{
        		btnConfirm.setText(R.string.wifi_connect);
                btnCancel.setText(R.string.wifi_forget);
        	}
        	break;
        }
        btnConfirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
		
		        switch(wifiStatus){
		        case WIFI_SECURITY_NONE:
		        case WIFI_SECURITY:
					//mrefresh.setText(R.string.ethconnect_wait);
					WifiConfiguration config = getConfig(dialog);
					if (config.networkId != INVALID_NETWORK_ID) {
	                    if (mSelectedAccessPoint != null) {
	                        saveNetwork(config);
	                    }
					}
	                mWifiManager.connectNetwork(config);
		        	break;
		        case WIFI_CONNECTED:
					//mrefresh.setText(R.string.wifi_forget);
					mWifiManager.forgetNetwork(mSelectedAccessPoint.networkId);
		        	break;
		        case WIFI_SAVED:
		        	if(level == -1){
		        		mWifiManager.forgetNetwork(mSelectedAccessPoint.networkId);
		        	}else{
		        		mWifiManager.connectNetwork(mSelectedAccessPoint.getConfig());
		        	}
		        	break;
		        }
				/*if(mSelectedAccessPoint != null && mSelectedAccessPoint.getInfo() != null){
					mrefresh.setText(R.string.wifi_forget);
					mWifiManager.forgetNetwork(mSelectedAccessPoint.networkId);
					//mWifiManager.disableNetwork(mSelectedAccessPoint.networkId);
				}else{
					mrefresh.setText(R.string.ethconnect_wait);
					WifiConfiguration config = getConfig(dialog);
					if (config.networkId != INVALID_NETWORK_ID) {
	                    if (mSelectedAccessPoint != null) {
	                        saveNetwork(config);
	                    }
					}
	                mWifiManager.connectNetwork(config);
				}*/
                updateAccessPoints();
				dialog.dismiss();
			}
        	
        });
        btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
		
		
				if((wifiStatus == WIFI_SAVED) && (level != -1)){
					mWifiManager.forgetNetwork(mSelectedAccessPoint.networkId);
				}
				dialog.dismiss();
			}
        	
        });
        dialog.show();
	}
	
	WifiConfiguration getConfig(Dialog dialog){
		TextView ed_password = (TextView) dialog.findViewById(R.id.ed_wifi_password);
		WifiConfiguration config = new WifiConfiguration();
		if (mSelectedAccessPoint.networkId == INVALID_NETWORK_ID) {
            config.SSID = AccessPoint.convertToQuotedString(
            		mSelectedAccessPoint.ssid);
        } else {
            config.networkId = mSelectedAccessPoint.networkId;
        }
		
        switch (mSelectedAccessPoint.getSecurity()) {
        case AccessPoint.SECURITY_NONE:
            config.allowedKeyManagement.set(KeyMgmt.NONE);
            break;

        case AccessPoint.SECURITY_WEP:
            config.allowedKeyManagement.set(KeyMgmt.NONE);
            config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
            config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
            if (ed_password.length() != 0) {
                int length = ed_password.length();
                String password = ed_password.getText().toString();
                // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
                if ((length == 10 || length == 26 || length == 58) &&
                        password.matches("[0-9A-Fa-f]*")) {
                    config.wepKeys[0] = password;
                } else {
                    config.wepKeys[0] = '"' + password + '"';
                }
            }
            break;

        case AccessPoint.SECURITY_PSK:
            config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
            if (ed_password.length() != 0) {
                String password = ed_password.getText().toString();
                if (password.matches("[0-9A-Fa-f]{64}")) {
                    config.preSharedKey = password;
                } else {
                    config.preSharedKey = '"' + password + '"';
                }
            }
            break;

        case AccessPoint.SECURITY_EAP:
            /*config.allowedKeyManagement.set(KeyMgmt.WPA_EAP);
            config.allowedKeyManagement.set(KeyMgmt.IEEE8021X);
            config.eap.setValue((String) mEapMethodSpinner.getSelectedItem());

            config.phase2.setValue((mPhase2Spinner.getSelectedItemPosition() == 0) ? "" :
                    "auth=" + mPhase2Spinner.getSelectedItem());
            config.ca_cert.setValue((mEapCaCertSpinner.getSelectedItemPosition() == 0) ? "" :
                    KEYSTORE_SPACE + Credentials.CA_CERTIFICATE +
                    (String) mEapCaCertSpinner.getSelectedItem());
            config.client_cert.setValue((mEapUserCertSpinner.getSelectedItemPosition() == 0) ?
                    "" : KEYSTORE_SPACE + Credentials.USER_CERTIFICATE +
                    (String) mEapUserCertSpinner.getSelectedItem());
            config.private_key.setValue((mEapUserCertSpinner.getSelectedItemPosition() == 0) ?
                    "" : KEYSTORE_SPACE + Credentials.USER_PRIVATE_KEY +
                    (String) mEapUserCertSpinner.getSelectedItem());
            config.identity.setValue((mEapIdentityView.length() == 0) ? "" :
                    mEapIdentityView.getText().toString());
            config.anonymous_identity.setValue((mEapAnonymousView.length() == 0) ? "" :
                    mEapAnonymousView.getText().toString());
            if (mPasswordView.length() != 0) {
                config.password.setValue(mPasswordView.getText().toString());
            }*/
            break;
        }

	    config.proxySettings = mProxySettings;
	    config.ipAssignment = mIpAssignment;
	    config.linkProperties = new LinkProperties(mLinkProperties);

		return config;
	}
 
    public class Mycomparator implements Comparator {
    	private AccessPoint map1;
    	private AccessPoint map2;
    	public Mycomparator(){}
    	public int compare(Object o1, Object o2) {
    		map1 = (AccessPoint)o1;
    		map2 = (AccessPoint)o2;

            if (map1.getInfo() != map2.getInfo()) {
                return (map1.getInfo() != null) ? -1 : 1;
            }
            // Reachable one goes before unreachable one.
            if ((map1.getRssi() ^ map2.getRssi()) < 0) {
                return (map1.getRssi() != Integer.MAX_VALUE) ? -1 : 1;
            }
            // Configured one goes before unconfigured one.
            if ((map1.networkId ^ map2.networkId) < 0) {
                return (map1.networkId != -1) ? -1 : 1;
            }
            // Sort by signal strength.
            int difference = WifiManager.compareSignalLevel(map2.getRssi(), map1.getRssi());
            if (difference != 0) {
                return difference;
            }
            // Sort by ssid.
            return map1.getSSID().compareToIgnoreCase(map2.getSSID());
    	}
    }
 public void findFxView(int View_ID)
  {
     switch(View_ID)
   	{
   		
   		case R.id.wifiSpinner:
   		mwifiSpinner.requestFocus();
   		break;
   		
   		case R.id.btn_wirelessadd:
   	  wirelessAdd.requestFocus();
   		break;
   
   	
   	
   	}
   
   
  }
 
 	void setConfirmButtonEnable(Button button,boolean enable){
 		if(enable){
 			button.setEnabled(true);
 			button.setFocusable(true);
 		}else{
 			button.setEnabled(false);
 			button.setFocusable(false);
 		}
 	}
 	
	public void setPasswordHint(int security,TextView password){
		switch(security){
		case AccessPoint.SECURITY_NONE:
		default:
			break;
		case AccessPoint.SECURITY_PSK:
			password.setHint(R.string.wifi_add_psk_password_hint);
			break;
		case AccessPoint.SECURITY_EAP:
		case AccessPoint.SECURITY_WEP:
			password.setHint(R.string.wifi_add_password_hint);
			break;
		}
	}
	
	public void setPasswordMaxLength(int security,TextView password){
		switch(security){
		case AccessPoint.SECURITY_NONE:
		default:
			break;
		case AccessPoint.SECURITY_PSK:
			password.setFilters(new InputFilter[]{new InputFilter.LengthFilter(64)});
			break;
		case AccessPoint.SECURITY_EAP:
		case AccessPoint.SECURITY_WEP:
			password.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});
			break;
		}		
	}
	
	public OnClickListener mDialogListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
	
			int id = v.getId();
			switch(id){
			case R.id.forget:
				mWifiManager.forgetNetwork(mSelectedAccessPoint.networkId);
				mWifiDialog.dismiss();
				isShowing = false;
				break;
			case R.id.confirm:
				if((mSelectedAccessPoint.getRssi() == Integer.MAX_VALUE)
								|| (mSelectedAccessPoint.getState() != null)){
					mWifiManager.forgetNetwork(mSelectedAccessPoint.networkId);
				}else{
					WifiConfiguration config = null;
					if(mSelectedAccessPoint.getConfig() != null && mWifiDialog.mState != 1){
						config = mSelectedAccessPoint.getConfig();
						Log.d(TAG, "-----mSelectedAccessPoint.getConfig()");
					}else{
						config = mWifiDialog.getConfig();
						Log.d(TAG, "------mWifiDialog.getConfig() mSelectedAccessPoint = " + mSelectedAccessPoint);
						if (config.networkId != INVALID_NETWORK_ID) {
		                    if (mSelectedAccessPoint != null) {
		                        saveNetwork(config);
		                    }
						}
					}
	                mWifiManager.connectNetwork(config);
				}
                updateAccessPoints();
				mWifiDialog.dismiss();
				isShowing = false;
				break;
			case R.id.cancel:
				mWifiDialog.dismiss();
				isShowing = false;
				break;
			}
		}
		
	};
	
	public void showDialog(AccessPoint accesspoint,int state){
		if(mWifiDialog != null){
			mWifiDialog.dismiss();
			isShowing = false;
			mWifiDialog = null;
		}
		//mWifiDialog = new WifiEapDialog(getActivity(),R.style.DialogStyle,accesspoint,mEapDialogListener);
		mWifiDialog = new WifiDialog(this.getActivity(),accesspoint,mDialogListener,state);
		mWifiDialog.show();
		isFirst = false;
		isShowing = true;
	}
    
    /**
     * 用于在屏幕中央弹出toast提示
     * @param context
     * @param stringId 字符串资源ID
     */
    public static void showToast(Context context, int stringId) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater
                .inflate(R.layout.wifi_connect_result_dialog, null);
        TextView text = (TextView) view.findViewById(R.id.connect_result);
        text.setText(context.getResources().getString(stringId));
        Log.d(TAG, " -------------showToast " + view.getWidth() + view.getHeight());
//        TextView text = new TextView(context);
//        text.setWidth(400);
//        text.setHeight(200);
//        text.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.dlg_bg));
//        text.setText(context.getResources().getString(stringId));
//        text.setTextSize(30);
//        text.setGravity(Gravity.CENTER);
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(view);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}


/*package com.lenovo.settings;

import static android.net.wifi.WifiConfiguration.INVALID_NETWORK_ID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Dialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.IpAssignment;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiConfiguration.ProxySettings;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.util.AsyncChannel;
import com.lenovo.settings.Object.FilterChars;
import com.lenovo.settings.wifi.AccessPoint;
import com.lenovo.settings.wifi.WifiDialog2;
import com.lenovo.settings.wifi.WifiUtil;

public class NetworkWireless extends Fragment {

    private static final String KEYSTORE_SPACE = "keystore://";
	private static final String TAG = "NetworkWireless";
	private ListView lv;
	private ArrayList<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
	// private ListViewAdapter listViewAdapter;
	private View mView;
	private LinearLayout wirelessAdd;
	private TextView mwifiSpinner;
//	private TextView mrefresh;
	private WifiManager mWifiManager;
	private IntentFilter mFilter;
	private WifiListAdapter mListAdapter;
	private AtomicBoolean mConnected = new AtomicBoolean(false);
	private DetailedState mLastState;
	private WifiInfo mLastInfo;
	private final Scanner mScanner;
	private final BroadcastReceiver mReceiver;
	private  List<ScanResult> restltList;
	private static final int WIFI_RESCAN_INTERVAL_MS = 5 * 1000;
	private static final int WIFI_SECURITY_NONE = 0;
	private static final int WIFI_SECURITY = 1;
	private static final int WIFI_CONNECTED = 2;
	private static final int WIFI_SAVED = 3;
	private static final int WIFI_NOT_SAVED = 3;
	private static final int WIFI_NOT_IN_RANGE = 4;
	private static int mIndexWireless = 0;
	int wifiStatus = -1;

    
	private int[] imgeIDs = { R.drawable.wifi_lock_signal_1,
			R.drawable.wifi_lock_signal_2, R.drawable.wifi_lock_signal_3,
			R.drawable.wifi_lock_signal_4, R.drawable.wifi_lock_signal_5 };
	private ArrayList<AccessPoint> mAccessPoints = new ArrayList<AccessPoint>();
	public static Object bLock = new Object();
	public boolean lock = false;
    private AccessPoint mSelectedAccessPoint = null;
    
    private IpAssignment mIpAssignment = IpAssignment.UNASSIGNED;
    private ProxySettings mProxySettings = ProxySettings.UNASSIGNED;
    private LinkProperties mLinkProperties = new LinkProperties();
	private ListView mDropdownList;
	private RelativeLayout mListLayout;
	private ArrayAdapter<CharSequence> mAdapter;
	protected boolean mListEnd = false;
	protected boolean mEnableList = false;
	private DropdownMenu mDropMenu;
	private String[] mWirelessArrays;
	private WifiDialog2 mWifiDialog = null;
	
  //Hazel add {
   ItemInfo mIT;
  //Hazel add }
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
	

			switch(msg.what){
			case DropdownMenu.DROPDOWN_MENU_ITEM_CHICK:
				if(msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_CAN_EDIT){
					EditText edit = (EditText) msg.obj;
				}else if(msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_NOT_EDIT){
					TextView text = (TextView) msg.obj;
					int position = msg.arg2;
					if(text == mwifiSpinner){
						  if(mIndexWireless != position){
							  mIndexWireless = position;
								if (position == 1)
									mWifiManager.setWifiEnabled(false);
								else
									mWifiManager.setWifiEnabled(true);
								wirelessAdd.setEnabled((mIndexWireless == 0) ? true : false);
								wirelessAdd.setFocusable((mIndexWireless == 0) ? true : false);
						  }
					}else{
						break;
					}
				}
				break;
			case DropdownMenu.DROPDOWN_MENU_GET_ADAPTER:
				if(msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_CAN_EDIT){
					EditText edit = (EditText) msg.obj;
				}else if(msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_NOT_EDIT){
					TextView text = (TextView) msg.obj;
					if(text == mwifiSpinner){
						mDropMenu.setListViewPosition(0, 2);
						mDropMenu.setListViewAdapter(mWirelessArrays, mIndexWireless);
					}else{
						break;
					}
					mDropMenu.showDropdownListEnable(true);
				}
				break;
			}
		}
		
	};
	
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


		mView = inflater.inflate(R.layout.network_wireless, container, false);
		mwifiSpinner = (TextView) mView.findViewById(R.id.wifiSpinner);
//		mrefresh = (TextView) mView.findViewById(R.id.wifi_refresh);
		mDropdownList = (ListView) mView.findViewById(R.id.dropdownlist);
		mListLayout = (RelativeLayout) mView.findViewById(R.id.listLayout);
		wirelessAdd = (LinearLayout) mView.findViewById(R.id.btn_wirelessadd);
		

		mDropMenu = new DropdownMenu(getActivity(),mView,mHandler );
		mWirelessArrays = getResources().getStringArray(R.array.frequency_array);
		mIndexWireless = getWifiStatus();
		wirelessAdd.setEnabled((mIndexWireless == 0) ? true : false);
		wirelessAdd.setFocusable((mIndexWireless == 0) ? true : false);

		mDropMenu.setButtonListener(mwifiSpinner,mWirelessArrays[mIndexWireless]);
		mDropMenu.setListViewListener();
	
		
		lv = (ListView) mView.findViewById(R.id.wifi_list);
		mListAdapter = new WifiListAdapter(getActivity(), listItems);
		lv.setAdapter(mListAdapter);
		lock = true;
		lv.setOnItemClickListener(new OnItemClickListener(){ 
	    	 public void onItemClick(AdapterView<?> parent, View view, 
	    			 		int position, long id) {  
	    		 Log.e(TAG,"position = "+position);
	    		 AccessPoint accesspoint = mAccessPoints.get(position);
	    		 Log.e(TAG,"ssid = "+accesspoint.getSSID());
	    		 
	    		 DetailedState state = accesspoint.getState();
	    		 if((state == DetailedState.AUTHENTICATING) || (state == DetailedState.CONNECTING) ||
	    				 (state == DetailedState.DISCONNECTING) || (state == DetailedState.OBTAINING_IPADDR)){
	    			 Log.e(TAG,"wifi is setting,can not click!");
	    			 return;
	    		 }
	    		 
	    		 Log.e(TAG,"Security = "+accesspoint.getSecurity());
	    		 mSelectedAccessPoint = accesspoint;

				 showDialog(accesspoint);
	    		 if(accesspoint.getSecurity() == AccessPoint.SECURITY_EAP){
	    			 if((accesspoint.getConfig() != null) 
	    					 || (accesspoint.getState() == DetailedState.CONNECTED)){
		    			 showWifiConnectDialog(getActivity(),accesspoint.getSSID(),
		    					 accesspoint.getLevel(),accesspoint.getSecurity());
	    			 }else{
	    				 showDialog(accesspoint);
	    			 }
	    		 }else{
	    			 showWifiConnectDialog(getActivity(),accesspoint.getSSID(),
	    					 accesspoint.getLevel(),accesspoint.getSecurity());
	    		 }
	    		 if (WifiUtil.getSecurity(res) == WifiUtil.SECURITY_NONE) {
	                 mWifiManager.connectNetwork(WifiUtil.generateOpenNetworkConfig(res));
	             }else{
	            	 showWifiConnectDialog(getActivity(),mView);
	             }
	             
	            // else
	    		// WifiConfiguration config = WifiUtil.CreateWifiInfo(res.SSID,"54321geniatech",WifiUtil.getSecurity(res));
	    		// mWifiManager.connectNetwork(config);
	    		// WifiInfo  info = mWifiManager.getConnectionInfo();
	    	 }
	    	}); 



		

		wirelessAdd.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
		
				Fragment fragment = (Fragment) new NetworkWirelessAdd();
				SettingFragment sf = (SettingFragment) new SettingFragment(
						getActivity());
				sf.setFragment(fragment,true);
			 mIT.VId=R.id.btn_wirelessadd;
			}
		});

		//mwifiSpinner.requestFocus();
		//mwifiSpinner.setFocusable(true);
		//mwifiSpinner.setFocusableInTouchMode(true);

		// reSetListViewHeight(lv);
		LenovoSettingsActivity.setTitleFocus(false);
		
		//Hazel add {
	   mIT=new ItemInfo();
		if(ConfigFocus.Items_t.size()==0)
	 	{
	 		Log.d(TAG,"^^^ConfigFocus.Items_t.size()==0");
	 			mwifiSpinner.requestFocus();
	 		  mIT.VId=R.id.wifiSpinner;
	 		  ConfigFocus.Items_t.add(mIT);
		}
		else
		{
			 ItemInfo gItemId=ConfigFocus.Items_t.get(0);	
			 findFxView(gItemId.VId);
			 Log.d(TAG,"$$$ConfigFocus.Items_t.size()!=NULL");
		}
		//Hazel add }
    	ConfigFocus.Items_t.set(0,mIT);
		return mView;

	}

	private void getListItems(ArrayList<AccessPoint> accesspoints) {
		synchronized(bLock){
			listItems.clear();
			//ArrayList<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
			if(accesspoints==null)
				return ;
	
			for(int i=0;i<accesspoints.size();i++){
				WifiConfiguration config = accesspoints.get(i).getConfig();
				Map<String, Object> map = new HashMap<String, Object>();
				if(config != null){
					Log.d(TAG,"SSID = "+config.SSID);
					map.put("connect", 1);
					map.put("title", AccessPoint.removeDoubleQuotes(config.SSID));
					map.put("secure", WifiUtil.getSecurity(config));
					map.put("signal" ,config);
					listItems.add(map);
				}
			}
			for(AccessPoint accesspoint : accesspoints){
				Map<String, Object> map = new HashMap<String, Object>();
				String secure_str;
				boolean lock = true;
				//Log.d(TAG,"SSID = "+accesspoint.getSSID());
				if(accesspoint.getState() != null){
					mSelectedAccessPoint = accesspoint;
					//mrefresh.setText(getActivity().getResources().getString(
					//		R.string.wifi_connected_prompt,mSelectedAccessPoint.getSSID()));
				}
				map.put("connect", accesspoint.getInfo() == null ? 0 : 1);
				map.put("title", accesspoint.getSSID());
				map.put("secure", accesspoint.getSecurity());
				map.put("signal" ,accesspoint.getRssi());

				switch(accesspoint.getSecurity()){
		        case 0:
		        default:
		        	secure_str = getActivity().getResources().getString(R.string.wifi_secure_none);
		        	lock = false;
		        	break;
		        case 1:
		        	secure_str = getActivity().getResources().getString(R.string.wifi_secure_wep);
		        	break;
		        case 2:
		        	secure_str = getActivity().getResources().getString(R.string.wifi_secure_wpa);
		        	break;
		        case 3:
		        	secure_str = getActivity().getResources().getString(R.string.wifi_secure_eap);
		        	break;
				}
				map.put("connect", (accesspoint.getState() == DetailedState.CONNECTED) ? 1 : 0);
				map.put("title", accesspoint.getSSID());
				map.put("signal" ,accesspoint.getRssi());
				if(accesspoint.getState() == DetailedState.CONNECTED){
					String str = getActivity().getResources().getString(R.string.wifi_connected) + secure_str;
					map.put("secure", str);
				}else if(accesspoint.getState() == DetailedState.OBTAINING_IPADDR){
					String str = getActivity().getResources().getString(R.string.wifi_obtaining_ipaddr);
					map.put("secure", str);
				}else if(accesspoint.getRssi() == Integer.MAX_VALUE){
					String str = getActivity().getResources().getString(R.string.wifi_no_range);
					map.put("secure", str);
				}else if(accesspoint.getConfig() != null){
					String str = getActivity().getResources().getString(R.string.wifi_has_save) + secure_str;
					map.put("secure", str);
				}else{
					map.put("secure", secure_str);
				}
				map.put("lock", lock);
				
				listItems.add(map);
			}
			//listItems = items;
			//mListAdapter.notifyDataSetChanged();
		}
	}



	public static void reSetListViewHeight(ListView listView) {

		ListAdapter listAdapter = listView.getAdapter();

		if (listAdapter == null) {

			// pre-condition
			return;
		}

		int totalHeight = 0;

		for (int i = 0; i < listAdapter.getCount(); i++) {

			View listItem = listAdapter.getView(i, null, listView);

			listItem.measure(0, 0);

			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();

		params.height = totalHeight
				+ (listView.getDividerHeight() * ((listAdapter.getCount() + 1) - 1));
		Log.i("clei", "params.height = " + params.height);
		listView.setLayoutParams(params);

	}

    public NetworkWireless() {
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

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleEvent(context, intent);
            }
        };
        mScanner = new Scanner();
        
    }
	
	
	
	
	public int getWifiStatus() {
		mWifiManager = (WifiManager) getActivity().getSystemService(
				Context.WIFI_SERVICE);
		 mWifiManager.asyncConnect(getActivity(), new WifiServiceHandler());
		Log.i("getWifiStatus", "getWifiState = " + mWifiManager.getWifiState());
		switch (mWifiManager.getWifiState()) {
		case WifiManager.WIFI_STATE_ENABLING:
//			mrefresh.setText(R.string.wifi_enabling);
			return 0;
		case WifiManager.WIFI_STATE_ENABLED:
//			mrefresh.setText("");
			return 0;
		default:
//			mrefresh.setText(R.string.wireless_failed);
			return 1;

		}
		
	}

    private class WifiServiceHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AsyncChannel.CMD_CHANNEL_HALF_CONNECTED:
                    if (msg.arg1 == AsyncChannel.STATUS_SUCCESSFUL) {
                        //AsyncChannel in msg.obj
                    } else {
                        //AsyncChannel set up failure, ignore
                        Log.e("CMD_CHANNEL_HALF_CONNECTED", "Failed to establish AsyncChannel connection");
                    }
                    break;
                case WifiManager.CMD_WPS_COMPLETED:
                    WpsResult result = (WpsResult) msg.obj;
                    Log.i("clei","");
                    break;
                //TODO: more connectivity feedback
                default:
                    //Ignore
                    break;
            }
        }
    }
    
    
	private void updateWifiState(int state) {
		Log.i(TAG,"NetworkWireless handleWifiStateChanged "+"state = "+state);
		switch (state) {
		case WifiManager.WIFI_STATE_ENABLING:
//			mrefresh.setText(R.string.wifi_enabling);
			break;
		case WifiManager.WIFI_STATE_ENABLED:
//			mrefresh.setText("");
			mScanner.resume();
			return;
		case WifiManager.WIFI_STATE_DISABLING:
//			mrefresh.setText(R.string.wifi_disabling);
			break;
		case WifiManager.WIFI_STATE_DISABLED:
			listItems.clear();
	        mListAdapter.setAdapterData(listItems);
			mListAdapter.notifyDataSetChanged();
//			mrefresh.setText(R.string.wifi_disabled);
			break;
		default:
			break;
		}
	  mLastInfo = null;
	  mLastState = null;
	  mScanner.pause();

	}
	
    private void updateAccessPoints() {
		Log.i(TAG,"NetworkWireless updateAccessPoints ");
        final int wifiState = mWifiManager.getWifiState();
    	//mrefresh.setText(R.string.wirelessRefresh);
        switch (wifiState) {
        case WifiManager.WIFI_STATE_ENABLED:
            // AccessPoints are automatically sorted with TreeSet.
            final Collection<AccessPoint> accessPoints = constructAccessPoints();
            mAccessPoints = (ArrayList<AccessPoint>) accessPoints;
            getListItems(mAccessPoints);
            break;

        case WifiManager.WIFI_STATE_ENABLING:
        	listItems.clear();    	
            break;
        }
        //Log.d(TAG,"listItems size = "+listItems.size());
        mListAdapter.setAdapterData(listItems);
  	  	mListAdapter.notifyDataSetChanged();

    }

    *//** Returns sorted list of access points *//*
    private List<AccessPoint> constructAccessPoints() {
    	List<AccessPoint> accessPoints = new ArrayList<AccessPoint>();
        
        *//** Lookup table to more quickly update AccessPoints by only considering objects with the
         * correct SSID.  Maps SSID -> List of AccessPoints with the given SSID.  *//*

        final List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        if (configs != null) {
            for (WifiConfiguration config : configs) {
            	Log.e(TAG,"config ssid = "+config.SSID);
                AccessPoint accessPoint = new AccessPoint(getActivity(), config);
                accessPoint.update(mLastInfo, mLastState);
                if(mAccessPoints != null){
	                for(AccessPoint ap : mAccessPoints){
	                	if(ap.ssid.equals(accessPoint.ssid) && 
	                			(ap.security == accessPoint.security)){
	                		accessPoint.setRssi(ap.getRssi());
	                	}
	                }   
                }
                accessPoints.add(accessPoint);
            }
        }

        final List<ScanResult> results = mWifiManager.getScanResults();
        if (results != null) {
            for (ScanResult result : results) {
                // Ignore hidden and ad-hoc networks.
                if (result.SSID == null || result.SSID.length() == 0 ||
                        result.capabilities.contains("[IBSS]")) {
                    continue;
                }

            	//Log.d(TAG,"result ssid = "+result.SSID);
                boolean found = false;
                for (AccessPoint accessPoint : accessPoints) {
                    if (accessPoint.update(result))
                        found = true;
                }
                if (!found) {
                    AccessPoint accessPoint = new AccessPoint(getActivity(), result);
                    accessPoints.add(accessPoint);
                }
            }
        }

        // Pre-sort accessPoints to speed preference insertion
        Mycomparator comp = new Mycomparator();
        Collections.sort(accessPoints,comp);
        return accessPoints;
    }
    
    private void updateConnectionState(DetailedState state) {
         sticky broadcasts can call this when wifi is disabled 
        if (!mWifiManager.isWifiEnabled()) {
            mScanner.pause();
            return;
        }
        
        Log.e(TAG,"updateConnectionState DetailedState = "+state);
        if (state == DetailedState.OBTAINING_IPADDR) {
//        	mrefresh.setText(getActivity().getResources().
//        			getString(R.string.wifi_get_ip,mWifiManager.getConnectionInfo().getSSID()));
            mScanner.pause();
        } else if (state == DetailedState.CONNECTED){ 
        	WifiInfo info = mWifiManager.getConnectionInfo();
//        	mrefresh.setText(getActivity().getString(R.string.wifi_connected_prompt, info.getSSID()));
        } else if (state == DetailedState.SCANNING){ 
//        	mrefresh.setText(R.string.wirelessRefresh);
        } else if (state == DetailedState.DISCONNECTED){ 
//        	mrefresh.setText("");
        } else if (state == DetailedState.IDLE){ 
//        	mrefresh.setText("");
        }else{
        	mScanner.resume();
        }
        

        mLastInfo = mWifiManager.getConnectionInfo();
        if (state != null) {
            mLastState = state;
        }
        if(mAccessPoints != null){
	        for(AccessPoint accesspoint : mAccessPoints){
	        	//Log.e(TAG,"123456  accesspoint rssi = "+accesspoint.getRssi());
	        	int rssi = accesspoint.getRssi();
	        	accesspoint.update(mLastInfo, mLastState);
	        	state = accesspoint.getState();
	        	if((state == DetailedState.OBTAINING_IPADDR) || 
	        			(state == DetailedState.AUTHENTICATING) ||
	        			(state == DetailedState.CONNECTING) || 
	        			(state == DetailedState.CONNECTED) || 
	        			(state == DetailedState.DISCONNECTING)){
	        		accesspoint.setRssi(rssi);
	        	}
	        	//Log.e(TAG,"111111  accesspoint rssi = "+accesspoint.getRssi());
	        }
        }

    }
    
	@Override
	public void onStart() {
		getActivity().registerReceiver(mReceiver, mFilter);
		Log.v("NetworkWireless", "onStart");
		super.onResume();
	}
	
	@Override
	public void onStop() {
		getActivity().unregisterReceiver(mReceiver);
		Log.v("NetworkWireless", "onStop");
		super.onStop();
	}

	
    private void handleEvent(Context context, Intent intent) {
        String action = intent.getAction();
        Log.e(TAG,"wireless action is "+action);
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            updateWifiState(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN));
        } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action) ||
                WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION.equals(action) ||
                WifiManager.LINK_CONFIGURATION_CHANGED_ACTION.equals(action)) {
                //updateAccessPoints();
                if(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)){
                    updateAccessPoints();
//                	if(getActivity().getString(R.string.wirelessRefresh).equals(
//                			mrefresh.getText().toString())){
//                		mrefresh.setText("");
//                	}
                }
        } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {

            if (!mConnected.get()) {
                updateConnectionState(WifiInfo.getDetailedStateOf((SupplicantState)
                        intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE)));
            }

        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(
                    WifiManager.EXTRA_NETWORK_INFO);
            mConnected.set(info.isConnected());
           // changeNextButtonState(info.isConnected());
            updateConnectionState(info.getDetailedState());
            updateAccessPoints();
        } else if (WifiManager.RSSI_CHANGED_ACTION.equals(action)) {
            updateConnectionState(null);
        } else if (WifiManager.ERROR_ACTION.equals(action)) {
            int errorCode = intent.getIntExtra(WifiManager.EXTRA_ERROR_CODE, 0);
            switch (errorCode) {
                case WifiManager.WPS_OVERLAP_ERROR:
                    Toast.makeText(context, R.string.wifi_wps_overlap_error,
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
    
    
    private class Scanner extends Handler {
        private int mRetry = 0;

        void resume() {
            if (!hasMessages(0)) {
                sendEmptyMessage(0);
            }
        }

        void forceScan() {
            removeMessages(0);
            sendEmptyMessage(0);
        }

        void pause() {
            mRetry = 0;
            removeMessages(0);
        }

        @Override
        public void handleMessage(Message message) {
            if (mWifiManager.startScanActive()) {
                mRetry = 0;
            } else if (++mRetry >= 3) {
                mRetry = 0;
                Toast.makeText(getActivity(), R.string.wifi_fail_to_scan,
                        Toast.LENGTH_LONG).show();
                return;
            }
            sendEmptyMessageDelayed(0, WIFI_RESCAN_INTERVAL_MS);
        }
    }	
	  
	public class WifiListAdapter extends BaseAdapter {    
	    private static final String TAG = "WifiListAdapter";
		private Context context;                        
	    private ArrayList<Map<String, Object>> listItems;    
	    private LayoutInflater listContainer;         
	    public final class ListItemView{                    
	            public ImageView image;      
	            public TextView title;        
	            public TextView secure;      
	            public ImageView signal;    
	        
	     }      
	        
	    public WifiListAdapter(Context context, ArrayList<Map<String, Object>> listItems) {    
	        this.context = context;             
	        listContainer = LayoutInflater.from(context);  
			//synchronized(NetworkWireless.bLock){   
				this.listItems = (ArrayList<Map<String, Object>>) listItems.clone(); 
			//}
	    }  
	    
	    public void setAdapterData(ArrayList<Map<String, Object>> listItems){
	    	this.listItems = (ArrayList<Map<String, Object>>) listItems.clone();
	    }
	   
	    public int getCount() {    
	           
			//synchronized(bLock){    
				return this.listItems.size();    
			//}
	    }    
	   
	    public Object getItem(int arg0) {    
	            
	        return null;    
	    }    
	   
	    public long getItemId(int arg0) {    
	            
	        return 0;    
	    }
	        
	           
	    *//**   
	     * ListView Item
	     *//*   
	    public View getView(int position, View convertView, ViewGroup parent) {    
	        
	        final int selectID = position; 
	        if(position == (this.listItems.size() - 1)){
	        	lock = false;
	        }
			synchronized(bLock){      
		        ListItemView  listItemView = null;    
		        if (convertView == null) {    
		            listItemView = new ListItemView();     
		            convertView = listContainer.inflate(R.layout.wifi_list_item, null);    
		            listItemView.image = (ImageView)convertView.findViewById(R.id.imageItem);    
		            listItemView.title = (TextView)convertView.findViewById(R.id.titleItem);     
		            listItemView.secure = (TextView)convertView.findViewById(R.id.secureItem);    
		            listItemView.signal = (ImageView)convertView.findViewById(R.id.signalItem);         
		            convertView.setTag(listItemView);    
		        }else {    
		            listItemView = (ListItemView)convertView.getTag();    
		        }    
		        //Log.d(TAG,"listItems size = "+this.listItems.size()+" position = "+position);
		        //if(position >= this.listItems.size()){
		        //	return convertView;
		        //}
		        Map<String, Object> item = this.listItems.get(position);
		        int connect = (Integer)item.get("connect");
		         if(connect==0)
		        	listItemView.image.setImageDrawable(null);
		         else
		            listItemView.image.setImageResource(R.drawable.wireless_sel);  
		        switch((Integer)item.get("secure")){
			        case 0:
			        default:
			        	listItemView.secure.setText(R.string.wifi_secure_none);
			        	break;
			        case 1:
			        	listItemView.secure.setText(R.string.wifi_secure_wep);
			        	break;
			        case 2:
			        	listItemView.secure.setText(R.string.wifi_secure_wpa);
			        	break;
			        case 3:
			        	listItemView.secure.setText(R.string.wifi_secure_eap);
			        	break;
		        }

		        listItemView.secure.setText((String) item.get("secure"));   
		        listItemView.title.setText((String) item.get("title"));   
		        
		        
		        //ScanResult result = (ScanResult) item.get("signal");
		        
		
		        setImageSource(listItemView.signal,(Integer)item.get("signal"),(Boolean)item.get("lock"));
		        
		       // listItemView.signal.setBackgroundResource((Integer) listItems.get(    
		        //        position).get("signal")); 
			}    
	        return convertView;    
	    }   
	   
	    
	    private void setImageSource(ImageView signal,int mRssi ,boolean lock)
	    {
	    	
	        if (mRssi == Integer.MAX_VALUE) {
	            signal.setImageDrawable(null);
	        } else {
	            signal.setImageLevel(WifiUtil.getLevel(mRssi));//(WifiUtil.getLevel(mRssi));
	            signal.setImageResource(R.drawable.wifi_signal);
	            signal.setImageState(lock ? WifiUtil.STATE_SECURED : WifiUtil.STATE_NONE, true);
	        }
	    }
	}   

    private void saveNetwork(WifiConfiguration config) {
        mWifiManager.saveNetwork(config);
    }


    
	private void showWifiConnectDialog(Context context,String ssid,final int level,final int security){
		final Dialog dialog;
		final Button btnConfirm;
		Button btnCancel;
		TextView tv_ssid,tv_security,tv_level;
		final TextView ed_password;
		CheckBox cb_password;
		Log.e(TAG,"level is "+level);
		dialog = new Dialog(context,R.style.DialogStyle);
		if(mSelectedAccessPoint.getState() == DetailedState.CONNECTED){
			wifiStatus = WIFI_CONNECTED;
		}else if(mSelectedAccessPoint.getConfig() != null){
			wifiStatus = WIFI_SAVED;
		}else if(mSelectedAccessPoint.getSecurity() == AccessPoint.SECURITY_NONE){
			wifiStatus = WIFI_SECURITY_NONE;
		}else{
			wifiStatus = WIFI_SECURITY;
		}
        switch(wifiStatus){
        case WIFI_SECURITY_NONE:
        case WIFI_SAVED:
            dialog.setContentView(R.layout.wifi_connect_none_dialog);
        	break;
        case WIFI_SECURITY:
            dialog.setContentView(R.layout.wifi_connect_dialog);
        	break;
        case WIFI_CONNECTED:
            dialog.setContentView(R.layout.wifi_connected_dialog);
        	break;
        }
        //dialog.setCancelable(true);
        tv_ssid = (TextView) dialog.findViewById(R.id.text_ssid);
        tv_level = (TextView) dialog.findViewById(R.id.text_wifi_level);
        tv_security = (TextView) dialog.findViewById(R.id.text_wifi_security); 
        Log.d(TAG,"dialog security = "+security);
        String[] security_arr = getResources().getStringArray(R.array.wifi_security);

    	
        switch(security){
        case 0:
        default:
        	//tv_security.setText(R.string.wifi_secure_none);
        	tv_security.setText(security_arr[0]);
        	break;
        case 1:
        	//tv_security.setText(R.string.wifi_secure_wep);
        	tv_security.setText(security_arr[1]);
        	break;
        case 2:
        	//tv_security.setText(R.string.wifi_secure_wpa);
        	tv_security.setText(security_arr[2]);
        	break;
        case 3:
        	//tv_security.setText(R.string.wifi_secure_eap);
        	tv_security.setText(security_arr[3]);
        	break;
        }
        String[] level_arr = getResources().getStringArray(R.array.wifi_level);
        switch(level){
        case 0:
        	//tv_level.setText(R.string.wifi_secure_none);
        	tv_level.setText(level_arr[0]);
        	break;
        case 1:
        	//tv_level.setText(R.string.wifi_secure_wep);
        	tv_level.setText(level_arr[1]);
        	break;
        case 2:
        	//tv_level.setText(R.string.wifi_secure_wpa);
        	tv_level.setText(level_arr[2]);
        	break;
        case 3:
        	//tv_level.setText(R.string.wifi_secure_eap);
        	tv_level.setText(level_arr[3]);
        	break;
        case 4:
        	//tv_level.setText(R.string.wifi_secure_eap);
        	tv_level.setText(level_arr[4]);
        	break;
        default:
        	tv_level.setText(level_arr[5]);
        	break;
        }
        //tv_security.setText(context.getResources().getStringArray(R.array.wifi_security)[security]);
        //tv_level.setText(context.getResources().getStringArray(R.array.wifi_level)[level]);
        tv_ssid.setText(ssid);
        btnConfirm = (Button) dialog.findViewById(R.id.btn_dlg_confirm);        
        btnCancel = (Button) dialog.findViewById(R.id.btn_dlg_cancel);
        btnCancel.setText(R.string.dlg_cancel);
        switch(wifiStatus){
        case WIFI_SECURITY_NONE:
        	btnConfirm.setText(R.string.wifi_connect);
        	break;
        case WIFI_SECURITY:
        	setConfirmButtonEnable(btnConfirm,false);
            ed_password = (TextView) dialog.findViewById(R.id.ed_wifi_password);
            cb_password = (CheckBox) dialog.findViewById(R.id.checkBox_password);
        	setPasswordHint(security,ed_password);
        	setPasswordMaxLength(security,ed_password);
            if(cb_password.isChecked()){
    			ed_password.setInputType(InputType.TYPE_CLASS_TEXT |InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);   
    		}else{
    			ed_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);  
    		}
        	ed_password.setKeyListener(FilterChars.numberKeyListener);
            ed_password.addTextChangedListener(new TextWatcher() {
			    String tmp = "";   
			    String digits = getActivity().getString(R.string.name_password_digits); 
			    
				@Override
				public void afterTextChanged(Editable s) {
			
					 
			        Log.d(TAG, "<><>afterTextChanged<><>" + s.toString());   
			           
			        String str = s.toString();   
			        if(str.equals(tmp)){   
			            return;   
			        }   
			           
			        StringBuffer sb = new StringBuffer();   
			        for(int i = 0; i < str.length(); i++){   
			            if(digits.indexOf(str.charAt(i)) >= 0){   
			                sb.append(str.charAt(i));   
			            }   
			        }
			        tmp = sb.toString(); 
			        
			        //ed_password.setTextKeepState(tmp);
			        //ed_password.setText(tmp);
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
			
					tmp = s.toString();   
				}

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) { 
					if(security != AccessPoint.SECURITY_NONE){
						int min_size = 1;
						int max_size = 16;
						if(security == AccessPoint.SECURITY_PSK){
							min_size = 8;
							max_size = 64;
						}
						String password = ed_password.getText().toString();
						int password_len = password.getBytes().length;
						if((password_len < min_size) || (password_len > max_size)){
							setConfirmButtonEnable(btnConfirm,false);	
						}else{
							setConfirmButtonEnable(btnConfirm,true);	
						}
					}else{
						setConfirmButtonEnable(btnConfirm,true);
					}
				}
            	
            });
            cb_password.setOnCheckedChangeListener(new OnCheckedChangeListener() {

    			@Override
    			public void onCheckedChanged(CompoundButton buttonView,
    					boolean isChecked) {
    		
    				if(isChecked){
    					ed_password.setInputType(InputType.TYPE_CLASS_TEXT |InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);   
    				}else{
    					ed_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);  
    				}
    				ed_password.setKeyListener(FilterChars.numberKeyListener);
    			}
            	
            });
            if(mSelectedAccessPoint.getState() != null){
            	btnConfirm.setText(R.string.wifi_forget);         	
            }else{
            	btnConfirm.setText(R.string.wifi_connect);
            //}
        	break;
        case WIFI_CONNECTED:
        	btnConfirm.setText(R.string.wifi_forget);
        	TextView tv_speed = (TextView) dialog.findViewById(R.id.text_wifi_speed);
        	TextView tv_ipaddr = (TextView) dialog.findViewById(R.id.text_wifi_ipaddr);
        	tv_ipaddr.setText(Formatter.formatIpAddress(mSelectedAccessPoint.getInfo().getIpAddress()));
        	tv_speed.setText(mSelectedAccessPoint.getInfo().getLinkSpeed() + WifiInfo.LINK_SPEED_UNITS);
        	break;
        case WIFI_SAVED:
        	if(level == -1){
            	btnConfirm.setText(R.string.wifi_forget);
        	}else{
        		btnConfirm.setText(R.string.wifi_connect);
                btnCancel.setText(R.string.wifi_forget);
        	}
        	break;
        }
        btnConfirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
		
		        switch(wifiStatus){
		        case WIFI_SECURITY_NONE:
		        case WIFI_SECURITY:
					//mrefresh.setText(R.string.ethconnect_wait);
					WifiConfiguration config = getConfig(dialog);
					if (config.networkId != INVALID_NETWORK_ID) {
	                    if (mSelectedAccessPoint != null) {
	                        saveNetwork(config);
	                    }
					}
	                mWifiManager.connectNetwork(config);
		        	break;
		        case WIFI_CONNECTED:
					//mrefresh.setText(R.string.wifi_forget);
					mWifiManager.forgetNetwork(mSelectedAccessPoint.networkId);
		        	break;
		        case WIFI_SAVED:
		        	if(level == -1){
		        		mWifiManager.forgetNetwork(mSelectedAccessPoint.networkId);
		        	}else{
		        		mWifiManager.connectNetwork(mSelectedAccessPoint.getConfig());
		        	}
		        	break;
		        }
				if(mSelectedAccessPoint != null && mSelectedAccessPoint.getInfo() != null){
					mrefresh.setText(R.string.wifi_forget);
					mWifiManager.forgetNetwork(mSelectedAccessPoint.networkId);
					//mWifiManager.disableNetwork(mSelectedAccessPoint.networkId);
				}else{
					mrefresh.setText(R.string.ethconnect_wait);
					WifiConfiguration config = getConfig(dialog);
					if (config.networkId != INVALID_NETWORK_ID) {
	                    if (mSelectedAccessPoint != null) {
	                        saveNetwork(config);
	                    }
					}
	                mWifiManager.connectNetwork(config);
				}
                updateAccessPoints();
				dialog.dismiss();
			}
        	
        });
        btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
		
		
				if((wifiStatus == WIFI_SAVED) && (level != -1)){
					mWifiManager.forgetNetwork(mSelectedAccessPoint.networkId);
				}
				dialog.dismiss();
			}
        	
        });
        dialog.show();
	}
	
	WifiConfiguration getConfig(Dialog dialog){
		TextView ed_password = (TextView) dialog.findViewById(R.id.ed_wifi_password);
		WifiConfiguration config = new WifiConfiguration();
		if (mSelectedAccessPoint.networkId == INVALID_NETWORK_ID) {
            config.SSID = AccessPoint.convertToQuotedString(
            		mSelectedAccessPoint.ssid);
        } else {
            config.networkId = mSelectedAccessPoint.networkId;
        }
		
        switch (mSelectedAccessPoint.getSecurity()) {
        case AccessPoint.SECURITY_NONE:
            config.allowedKeyManagement.set(KeyMgmt.NONE);
            break;

        case AccessPoint.SECURITY_WEP:
            config.allowedKeyManagement.set(KeyMgmt.NONE);
            config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
            config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
            if (ed_password.length() != 0) {
                int length = ed_password.length();
                String password = ed_password.getText().toString();
                // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
                if ((length == 10 || length == 26 || length == 58) &&
                        password.matches("[0-9A-Fa-f]*")) {
                    config.wepKeys[0] = password;
                } else {
                    config.wepKeys[0] = '"' + password + '"';
                }
            }
            break;

        case AccessPoint.SECURITY_PSK:
            config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
            if (ed_password.length() != 0) {
                String password = ed_password.getText().toString();
                if (password.matches("[0-9A-Fa-f]{64}")) {
                    config.preSharedKey = password;
                } else {
                    config.preSharedKey = '"' + password + '"';
                }
            }
            break;

        case AccessPoint.SECURITY_EAP:
          
            break;
        }

	    config.proxySettings = mProxySettings;
	    config.ipAssignment = mIpAssignment;
	    config.linkProperties = new LinkProperties(mLinkProperties);

		return config;
	}
 
    public class Mycomparator implements Comparator {
    	private AccessPoint map1;
    	private AccessPoint map2;
    	public Mycomparator(){}
    	public int compare(Object o1, Object o2) {
    		map1 = (AccessPoint)o1;
    		map2 = (AccessPoint)o2;

            if (map1.getInfo() != map2.getInfo()) {
                return (map1.getInfo() != null) ? -1 : 1;
            }
            // Reachable one goes before unreachable one.
            if ((map1.getRssi() ^ map2.getRssi()) < 0) {
                return (map1.getRssi() != Integer.MAX_VALUE) ? -1 : 1;
            }
            // Configured one goes before unconfigured one.
            if ((map1.networkId ^ map2.networkId) < 0) {
                return (map1.networkId != -1) ? -1 : 1;
            }
            // Sort by signal strength.
            int difference = WifiManager.compareSignalLevel(map2.getRssi(), map1.getRssi());
            if (difference != 0) {
                return difference;
            }
            // Sort by ssid.
            return map1.getSSID().compareToIgnoreCase(map2.getSSID());
    	}
    }
 public void findFxView(int View_ID)
  {
     switch(View_ID)
   	{
   		
   		case R.id.wifiSpinner:
   		mwifiSpinner.requestFocus();
   		break;
   		
   		case R.id.btn_wirelessadd:
   	  wirelessAdd.requestFocus();
   		break;
   
   	
   	
   	}
   
   
  }
 
 	void setConfirmButtonEnable(Button button,boolean enable){
 		if(enable){
 			button.setEnabled(true);
 			button.setFocusable(true);
 		}else{
 			button.setEnabled(false);
 			button.setFocusable(false);
 		}
 	}
 	
	public void setPasswordHint(int security,TextView password){
		switch(security){
		case AccessPoint.SECURITY_NONE:
		default:
			break;
		case AccessPoint.SECURITY_PSK:
			password.setHint(R.string.wifi_add_psk_password_hint);
			break;
		case AccessPoint.SECURITY_EAP:
		case AccessPoint.SECURITY_WEP:
			password.setHint(R.string.wifi_add_password_hint);
			break;
		}
	}
	
	public void setPasswordMaxLength(int security,TextView password){
		switch(security){
		case AccessPoint.SECURITY_NONE:
		default:
			break;
		case AccessPoint.SECURITY_PSK:
			password.setFilters(new InputFilter[]{new InputFilter.LengthFilter(64)});
			break;
		case AccessPoint.SECURITY_EAP:
		case AccessPoint.SECURITY_WEP:
			password.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});
			break;
		}		
	}
	
	public OnClickListener mDialogListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
	
			int id = v.getId();
			switch(id){
			case R.id.forget:
				mWifiManager.forgetNetwork(mSelectedAccessPoint.networkId);
				mWifiDialog.dismiss();
				break;
			case R.id.confirm:
				if((mSelectedAccessPoint.getRssi() == Integer.MAX_VALUE)
								|| (mSelectedAccessPoint.getState() != null)){
					mWifiManager.forgetNetwork(mSelectedAccessPoint.networkId);
				}else{
					WifiConfiguration config = null;
					if(mSelectedAccessPoint.getConfig() != null){
						config = mSelectedAccessPoint.getConfig();
					}else{
						config = mWifiDialog.getConfig();
						if (config.networkId != INVALID_NETWORK_ID) {
		                    if (mSelectedAccessPoint != null) {
		                        saveNetwork(config);
		                    }
						}
					}
	                mWifiManager.connectNetwork(config);
				}
                updateAccessPoints();
				mWifiDialog.dismiss();
				//显示链接的dialog
				//链接成功或者失败的dialog
				break;
			case R.id.cancel:
				mWifiDialog.dismiss();
				break;
			}
		}
		
	};
	
	public void showDialog(AccessPoint accesspoint){
		if(mWifiDialog != null){
			mWifiDialog.dismiss();
			mWifiDialog = null;
		}
		//mWifiDialog = new WifiEapDialog(getActivity(),R.style.DialogStyle,accesspoint,mEapDialogListener);
		mWifiDialog = new WifiDialog2(getActivity(),accesspoint,mDialogListener);
		mWifiDialog.show();
		
	}
    
    
}*/