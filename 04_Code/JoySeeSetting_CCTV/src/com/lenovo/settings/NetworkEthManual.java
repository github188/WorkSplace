package com.lenovo.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lenovo.settings.Util.IpAddress;


import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.LinearGradient;
import android.graphics.Shader;
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
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class NetworkEthManual extends Fragment {

	private static final int ADDRESS_IP = 0;
	private static final int ADDRESS_NETMASK = 1;
	private static final int ADDRESS_GATEWAY = 2;
	private static final int ADDRESS_DNS = 3;
	private static final int ADDRESS_DNS2 = 4;
	private static final int CONNECT_FAIL = 5;
	private static final int HW_DISCONNECTED = 6;
	private static final String TAG = "NetworkEthManual";
	private View mView;
//	private EditText ip1, ip2, ip3, ip4;
//	private EditText netmask_1, netmask_2, netmask_3, netmask_4;
//	private EditText gateway_1, gateway_2, gateway_3, gateway_4;
//	private EditText dns1_1, dns1_2, dns1_3, dns1_4;
//	private EditText dns2_1, dns2_2, dns2_3, dns2_4;
	
	
	//#################################ipaddress
	private IpAddress ipAddress;
	private IpAddress ipAddress1;
	private IpAddress ipAddress2;
	private IpAddress ipAddress3;
	
	
	private Button eth_ok,eth_cancel;
	private EthernetManager mEthManager;
	private RelativeLayout mRelative;
	private Toast mToast;

	private String addr_filter = "^(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])$";
	private EthernetDevInfo mEthInfo;
	private boolean mHasConfig = false;
	private boolean mConnecting = false;
	private IntentFilter mFilter;
	private BroadcastReceiver mReceiver;
	private AnimationDrawable mAnimationLoading;
	private RelativeLayout mLoadingLayout;
	private boolean mConnectFailEnable = false;
	/**用来存储手动设置的Ip信息*/
	private SharedPreferences mPreferences;
	public static final String PRENAME = "saveed_ip_pre";
	public static final String mIp = "Ip";
	public static final String mMask = "Mask";
	public static final String mGateway = "Gateway";
	public static final String mDns = "Dns";
	private boolean HWdisconnected = false;
	private static final int MSG_HW_DISCONNECT = 0;
	private static final int MSG_DISCONNECT = 1;
	private static final int MSG_CONNECTED = 2;
	private Dialog mErrorDialog;
	private Handler mhandler = new Handler(){
	    @Override
	    public void handleMessage(Message msg) {
	        
	        switch (msg.what) {
            case MSG_HW_DISCONNECT:
                hideLoadingView();
                showErrorDialog(HW_DISCONNECTED);
                break;
            case MSG_CONNECTED:
                hideLoadingView();
//              getActivity().getFragmentManager().popBackStack();
                NetworkWireless.showToast(getActivity(), R.string.wifi_connect_success);
                break;
            case MSG_DISCONNECT:
                hideLoadingView();
                showErrorDialog(CONNECT_FAIL);
                break;
            }
	    }
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		getActivity().getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mPreferences = getActivity().getSharedPreferences(PRENAME,
                Context.MODE_PRIVATE);
		mConnecting = false;
		mConnectFailEnable = false;
        mFilter = new IntentFilter();
        mFilter.addAction(EthernetManager.ETH_STATE_CHANGED_ACTION);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                Log.e(TAG,"action = "+action);
                if (action.equals(EthernetManager.ETH_STATE_CHANGED_ACTION)) {
                	updateEth(context, intent);
                }
            }
        };
	}


	@Override
	public void onStart() {

		getActivity().registerReceiver(mReceiver, mFilter);
		super.onStart();
	}


	@Override
	public void onStop() {

	    mhandler.removeMessages(MSG_HW_DISCONNECT);
		getActivity().unregisterReceiver(mReceiver);
        if (mErrorDialog != null && mErrorDialog.isShowing()) {
            mErrorDialog.dismiss();
        }
		super.onStop();
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mView = inflater.inflate(R.layout.network_eth_manual, container, false);
		mRelative = (RelativeLayout) inflater.inflate(R.layout.toast_info, container, false);
		mLoadingLayout = (RelativeLayout) mView.findViewById(R.id.toastLayout);	

//		ip1 = (EditText) mView.findViewById(R.id.ip_1);
//		ip2 = (EditText) mView.findViewById(R.id.ip_2);
//		ip3 = (EditText) mView.findViewById(R.id.ip_3);
//		ip4 = (EditText) mView.findViewById(R.id.ip_4);
//
//		netmask_1 = (EditText) mView.findViewById(R.id.netmask_1);
//		netmask_2 = (EditText) mView.findViewById(R.id.netmask_2);
//		netmask_3 = (EditText) mView.findViewById(R.id.netmask_3);
//		netmask_4 = (EditText) mView.findViewById(R.id.netmask_4);
//
//		gateway_1 = (EditText) mView.findViewById(R.id.gateway_1);
//		gateway_2 = (EditText) mView.findViewById(R.id.gateway_2);
//		gateway_3 = (EditText) mView.findViewById(R.id.gateway_3);
//		gateway_4 = (EditText) mView.findViewById(R.id.gateway_4);
//
//		dns1_1 = (EditText) mView.findViewById(R.id.dns1_1);
//		dns1_2 = (EditText) mView.findViewById(R.id.dns1_2);
//		dns1_3 = (EditText) mView.findViewById(R.id.dns1_3);
//		dns1_4 = (EditText) mView.findViewById(R.id.dns1_4);
		
		
		//###############################ipaddress
		ipAddress = (IpAddress) mView.findViewById(R.id.ipaddress);
        ipAddress1 = (IpAddress) mView.findViewById(R.id.ipaddress1);
        ipAddress2 = (IpAddress) mView.findViewById(R.id.ipaddress2);
        ipAddress3 = (IpAddress) mView.findViewById(R.id.ipaddress3);
        ipAddress.initIpAddress();
        ipAddress1.initIpAddress();
        ipAddress2.initIpAddress();
        ipAddress3.initIpAddress();

		
		mEthManager = (EthernetManager) getActivity().getSystemService(Context.ETH_SERVICE);
		String[] Devs = mEthManager.getDeviceNameList();	
		if (Devs != null) {
            if (mEthManager.isEthConfigured()) {
                mEthInfo = mEthManager.getSavedEthConfig();
                String ip = null, mask = null, gateway = null, dns = null;
                ip = mEthInfo.getIpAddress();
                mask = mEthInfo.getNetMask();
                gateway = mEthInfo.getRouteAddr();
                dns = mEthInfo.getDnsAddr();
                Log.i(TAG, " getSavedEthConfig ip= " + ip + " mask= " + mask + " gateway = "
                        + gateway + " dn1 = " + dns + " infoname = " + mEthInfo.getIfName());
                if (mPreferences != null) {
                    ip = mPreferences.getString(mIp, ip);
                    mask = mPreferences.getString(mMask, mask);
                    gateway = mPreferences.getString(mGateway, gateway);
                    dns = mPreferences.getString(mDns, dns);
                    Log.i(TAG, " mPreferences ip= " + ip + "mask= " + mask + "gateway = "
                            + gateway + "dn1 = " + dns);
                }
                if (ip == null && mask == null && gateway == null
                        && dns == null) {
                    DhcpInfo dhcpinfo = mEthManager.getDhcpInfo();
                    if (dhcpinfo != null) {
                        ip = NetworkEth.getAddress(dhcpinfo.ipAddress);
                        mask = NetworkEth.getAddress(dhcpinfo.netmask);
                        gateway = NetworkEth.getAddress(dhcpinfo.gateway);
                        dns = NetworkEth.getAddress(dhcpinfo.dns1);
                    }
                    Log.i(TAG, " getDhcpInfo ip= " + ip + "mask= " + mask + "gateway = "
                            + gateway + "dn1 = " + dns);
                }
                if (ip != null) {
                    updateAddress(ip, ADDRESS_IP);
                }
                if (mask != null) {
                    updateAddress(mask, ADDRESS_NETMASK);
                }
                if (gateway != null) {
                    updateAddress(gateway, ADDRESS_GATEWAY);
                }
                if (dns != null) {
                    updateAddress(dns, ADDRESS_DNS);
                }
                mHasConfig = true;
            }
		}
		try {
		    if(!mHasConfig){
	            mEthInfo = new EthernetDevInfo();
	            mEthInfo.setIfName(mEthManager.getDeviceNameList()[0]);
	        }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
		
		eth_ok = (Button) mView.findViewById(R.id.ethmanual_ok);
		eth_cancel =  (Button) mView.findViewById(R.id.ethmanual_no);
		eth_cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				getActivity().getFragmentManager().popBackStack();	
			}
		});
		//eth_ok.requestFocus();
		eth_ok.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                
                // #########################################ipaddress
                Editor editor = mPreferences.edit();
                String ip = ipAddress.getIpAddress();
                String netmask = ipAddress1.getIpAddress();
                String gateway = ipAddress2.getIpAddress();
                String dns1 = ipAddress3.getIpAddress();

                // String ip = getIpAddress(ip1.getEditableText().toString(),
                // ip2.getEditableText().toString(),
                // ip3.getEditableText().toString(),
                // ip4.getEditableText().toString());
                //
                // String netmask =
                // getIpAddress(netmask_1.getEditableText().toString(),
                // netmask_2.getEditableText().toString(),
                // netmask_3.getEditableText().toString(),
                // netmask_4.getEditableText().toString());
                //
                // String gateway =
                // getIpAddress(gateway_1.getEditableText().toString(),
                // gateway_2.getEditableText().toString(),
                // gateway_3.getEditableText().toString(),
                // gateway_4.getEditableText().toString());
                //
                // String dns1 =
                // getIpAddress(dns1_1.getEditableText().toString(),
                // dns1_2.getEditableText().toString(),
                // dns1_3.getEditableText().toString(),
                // dns1_4.getEditableText().toString());

                Log.i(TAG, " onclick  ip= " + ip + "netmask= " + netmask + "gateway = "
                        + gateway + "dns1 = " + dns1);
                /*
                 * if(!ip.matches(addr_filter)||!netmask.matches(addr_filter)||!
                 * gateway.matches(addr_filter)||!dns1.matches(addr_filter)) {
                 * showToastView
                 * (getActivity().getResources().getString(R.string.
                 * ipaddr_format_error)); }
                 */
                if ((!ip.matches(addr_filter)) || (ip.equals("error"))||(ip.equals("0.0.0.0"))) {
                    showErrorDialog(ADDRESS_IP);
                } else if ((!netmask.matches(addr_filter))
                        || (netmask.equals("error"))||(ip.equals("0.0.0.0"))) {
                    showErrorDialog(ADDRESS_NETMASK);
                } else if ((!gateway.matches(addr_filter))
                        || (gateway.equals("error"))) {
                    showErrorDialog(ADDRESS_GATEWAY);
                } else if ((!dns1.matches(addr_filter))
                        || (dns1.equals("error"))) {
                    showErrorDialog(ADDRESS_DNS);
                } else {
                    editor.putString(mIp, ip.trim());
                    editor.putString(mMask, netmask.trim());
                    editor.putString(mGateway, gateway.trim());
                    editor.putString(mDns, dns1.trim());
                    editor.commit();
                    if (HWdisconnected) {
//                        showErrorDialog(HW_DISCONNECTED);
//                        return;
                    }
                    mhandler.sendEmptyMessageDelayed(MSG_HW_DISCONNECT, 5000);
                    mEthManager.setEthEnabled(true);
//                    NetworkUtils.stopDhcp(mEthManager.getSavedEthConfig().getIfName());
                    showLoadingView(
                            getActivity().getString(R.string.dlg_connecting),
                            true);
                    mEthInfo.setConnectMode(EthernetDevInfo.ETH_CONN_MODE_MANUAL);
                    mEthInfo.setIpAddress(ip.trim());
                    mEthInfo.setNetMask(netmask.trim());
                    mEthInfo.setRouteAddr(gateway.trim());
                    mEthInfo.setDnsAddr(dns1.trim());
                    mEthManager.updateEthDevInfo(mEthInfo);
                }
            }
		});
		LenovoSettingsActivity.setTitleFocus(false);
		ipAddress.requestFocus();
		return mView;
	}

	
	private void updateAddress(String addr, int type) {

		Log.e(TAG,"ipaddr = "+addr);
		//addr = addr.replace(".", "a");
		String strs[] = addr.split("\\.");
		switch(type){
		case ADDRESS_IP:
			ipAddress.setIpAddress(addr);
//			ip1.setText(strs[0]);
//			ip2.setText(strs[1]);
//			ip3.setText(strs[2]);
//			ip4.setText(strs[3]);
			break;
		case ADDRESS_NETMASK:
			ipAddress1.setIpAddress(addr);
//			netmask_1.setText(strs[0]);
//			netmask_2.setText(strs[1]);
//			netmask_3.setText(strs[2]);
//			netmask_4.setText(strs[3]);
			break;
		
		case ADDRESS_GATEWAY:
			ipAddress2.setIpAddress(addr);
//			gateway_1.setText(strs[0]);
//			gateway_2.setText(strs[1]);
//			gateway_3.setText(strs[2]);
//			gateway_4.setText(strs[3]);
			break;
		case ADDRESS_DNS:
			ipAddress3.setIpAddress(addr);
//			dns1_1.setText(strs[0]);
//			dns1_2.setText(strs[1]);
//			dns1_3.setText(strs[2]);
//			dns1_4.setText(strs[3]);
			break;
		}
	}


	void showToastView( String msg){ 
		TextView textView = (TextView)mRelative.findViewById(R.id.toast_text);
		ImageView img = (ImageView)mRelative.findViewById(R.id.toastImage);
		textView.setText(msg);
		img.setVisibility(View.GONE);
		mToast = new Toast(getActivity());
		mToast.setView(mRelative);
		mToast.setDuration(Toast.LENGTH_LONG);
		mToast.setGravity(Gravity.CENTER, 0, 0);
		mToast.show();		
	}
	

	private void showErrorDialog(int type){
		if((type == CONNECT_FAIL) && mConnectFailEnable){
			return ;
		}
		TextView msg = null;
		Button confirm = null;
		if(mErrorDialog==null){
		    mErrorDialog = new Dialog(mView.getContext(),R.style.DialogStyle);
	        mErrorDialog.setContentView(R.layout.net_manual_error_dialog);
		}
		msg = (TextView) mErrorDialog.findViewById(R.id.textview_dialog);
        confirm = (Button) mErrorDialog.findViewById(R.id.btn_dlg_confirm);
        switch(type){
        case ADDRESS_IP:
            msg.setText(R.string.dlg_ip_format_error);
        	break;
        case ADDRESS_NETMASK:
            msg.setText(R.string.dlg_netmask_format_error);
        	break;
        case ADDRESS_GATEWAY:
            msg.setText(R.string.dlg_gateway_format_error);
        	break;
        case ADDRESS_DNS:
            msg.setText(R.string.dlg_dns_format_error);
        	break;
        case CONNECT_FAIL:
        	mConnectFailEnable  = true;
            msg.setText(R.string.dlg_connect_failed);
        	break;
        case HW_DISCONNECTED:
            mConnectFailEnable  = true;
            msg.setText(R.string.dlg_connect_hw_disconnected);
        }
        confirm.setText(R.string.dlg_i_know);
        confirm.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
		
				mErrorDialog.dismiss();
				mConnectFailEnable = false;
			}
        	
        });
        mErrorDialog.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
		
				if(keyCode == KeyEvent.KEYCODE_BACK){
					mConnectFailEnable = false;
				}
				return false;
			}
        	
        });
        mErrorDialog.show();
	}
	
	void showLoadingView(String msg, boolean hasLoading){	
		mConnecting = true;
		TextView textView = (TextView)mLoadingLayout.findViewById(R.id.toast_text);
		ImageView img = (ImageView)mLoadingLayout.findViewById(R.id.toastImage);
		textView.setText(msg);
		mLoadingLayout.setVisibility(View.VISIBLE);
		Log.e(TAG,"showLoadingView");
		if(hasLoading){
			img.setVisibility(View.VISIBLE);
			mAnimationLoading = (AnimationDrawable)img.getBackground();
			if(!mAnimationLoading.isRunning()){
				mAnimationLoading.start();
			}
		}else{
			img.setVisibility(View.GONE);
		}
	}
	
	void hideLoadingView(){
		//ImageView img = (ImageView)mLoadingLayout.findViewById(R.id.toastImage);
//		if(!mConnecting){
//			return;
//		}
		Log.e(TAG,"hideLoadingView");
        if (mAnimationLoading != null && mAnimationLoading.isRunning()) {
            mAnimationLoading.stop();
        }
		//img.setVisibility(View.INVISIBLE);
		mLoadingLayout.setVisibility(View.INVISIBLE);
		mConnecting = false;
	}

	// ===== Ethernet ===================================================================
    private final void updateEth(Context context, Intent intent) {
        final int event = intent.getIntExtra(EthernetManager.EXTRA_ETH_STATE, EthernetStateTracker.EVENT_INTERFACE_CONFIGURATION_FAILED);
        Log.d(TAG, "updateEth getAction = " + intent.getAction() + " event = " + event + " mConnecting = " + mConnecting);
        switch (event) {
                // else fallthrough
            case EthernetStateTracker.EVENT_INTERFACE_CONFIGURATION_SUCCEEDED: 
                EthernetManager ethManager = (EthernetManager) context.getSystemService(Context.ETH_SERVICE);
                HWdisconnected = false;
                if (ethManager.isEthDeviceAdded()) {
                    if (!mConnecting) {
                        return;
                    }
                    mhandler.removeMessages(MSG_HW_DISCONNECT);
                    mhandler.removeMessages(MSG_DISCONNECT);
                    mhandler.sendEmptyMessage(MSG_CONNECTED);
                    return;
                }
            case EthernetStateTracker.EVENT_INTERFACE_CONFIGURATION_FAILED:
//                if (!mConnecting) {
//                    return;
//                }
                mhandler.removeMessages(MSG_HW_DISCONNECT);
                mhandler.removeMessages(MSG_CONNECTED);
            	mhandler.sendEmptyMessage(MSG_DISCONNECT);
                return;
            case EthernetStateTracker.EVENT_HW_CONNECTED:
            case EthernetStateTracker.EVENT_HW_PHYCONNECTED:
                HWdisconnected = false;
            	return;
            case EthernetStateTracker.EVENT_DHCP_START:
                return;
            case EthernetStateTracker.EVENT_HW_CHANGED:
                return;
            case EthernetStateTracker.EVENT_HW_DISCONNECTED:
                HWdisconnected = true;
                if (mConnecting) {
                    return;
                }
                WifiManager wifiManager = (WifiManager) getActivity()
                        .getSystemService(Context.WIFI_SERVICE);
                if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED)
                    return;
                hideLoadingView();
//                showErrorDialog(HW_DISCONNECTED);
                return;
        }
    }
    
    public String getIpAddress(String ip1, String ip2, String ip3, String ip4){
    	String ip;
    	int iIp1 = Integer.parseInt(ip1); 
    	int iIp2 = Integer.parseInt(ip2);  
    	int iIp3 = Integer.parseInt(ip3);  
    	int iIp4 = Integer.parseInt(ip4);   
    	if(((iIp1 < 0) && (iIp1 > 255)) || ((iIp2 < 0) && (iIp2 > 255))
    			||((iIp3 < 0) && (iIp3 > 255)) || ((iIp4 < 0) && (iIp4 > 255))){
    		return "error";
    	}
    	ip = new String(iIp1+"."+iIp2+"."+iIp3+"."+iIp4);
    	return ip;
    }
    
}
