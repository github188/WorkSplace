package com.lenovo.settings.wifi;

import java.net.InetAddress;
import java.util.Iterator;

import com.lenovo.settings.DropdownMenu;
import com.lenovo.settings.R;
import com.lenovo.settings.Object.FilterChars;

import static android.net.wifi.WifiConfiguration.INVALID_NETWORK_ID;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkUtils;
import android.net.RouteInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.IpAssignment;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiConfiguration.ProxySettings;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.security.Credentials;
import android.security.KeyStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WifiDialog  extends Dialog implements View.OnClickListener,
										TextWatcher,View.OnKeyListener {

	private static final String TAG = "WifiDialog";
    private static final String KEYSTORE_SPACE = "keystore://";
    private static final int DHCP = 0;
    private static final int STATIC_IP = 1;
    
    
	private View.OnClickListener mListener;
	private AccessPoint mAccessPoint;
	private int mSecurity;
	private TextView mTextTitle,mTextSecurity,mTextStrength,mIpAddressView,mGatewayView,
					mNetworkPrefixLengthView,mDns1View,mDns2View,mPasswordView,mTextStatus,
					/*mTextSpeed,*/mTextLinkIp,mTextEap,mTextEapAuthentication,mEditIdentity,  //20120627whj remove mTextSpeed
					mEditAnonymous,mSpinnerIpSetting;

	private DropdownMenu mDropMenu;
	private int mIndexIpSetting;
	private String[] mIpSettinsArrays;
	private Button mBtnConfirm,mBtnForget,mBtnCancel;
    private IpAssignment mIpAssignment = IpAssignment.UNASSIGNED;
    private ProxySettings mProxySettings = ProxySettings.UNASSIGNED;
    private LinkProperties mLinkProperties = new LinkProperties();
	private String[] mEapMethodArrays;
	private int mIndexEapMethod;
	private String[] mEapAuthenticationArrays;
	private int mIndexEapAuthentication;
	private String[] mEapCAArrays;
	private String[] mEapUserArrays;
	private int mIndexEapCA;
	private int mIndexEapUser;
	private View mView;
	/**密码验证状态，为1则标示验证失败。*/
	public int mState;
	/**请求连接*/
	private static final int STATE_CONNECT = 0;
	/**密码验证失败*/
	private static final int STATE_NOPASS = 1;
	

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){

			case DropdownMenu.DROPDOWN_MENU_ITEM_CHICK:
				if(msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_CAN_EDIT){
					EditText edit = (EditText) msg.obj;
				}else if(msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_NOT_EDIT){
					TextView text = (TextView) msg.obj;
					int position = msg.arg2;
					int id = text.getId();
					switch(id){
					case R.id.eapmethodSpinner:
						if(mIndexEapMethod != position){
							mIndexEapMethod = position;
						}
						break;
					case R.id.eapAuthenticationSpinner:
						if(mIndexEapAuthentication != position){
							mIndexEapAuthentication = position;
						}
						break;
					case R.id.ipStettingSpinner:
						if(mIndexIpSetting != position){
							mIndexIpSetting = position;
							showIpConfigFields(mView);
							validate();
						}
						break;
					}
				}
				break;
			case DropdownMenu.DROPDOWN_MENU_GET_ADAPTER:
				if(msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_CAN_EDIT){
					EditText edit = (EditText) msg.obj;
				}else if(msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_NOT_EDIT){
					TextView text = (TextView) msg.obj;
					int id = text.getId();
					Log.e(TAG," handleMessage id = "+id);
					int top;
					switch(id){
					case R.id.eapmethodSpinner:
					    top = (int) getContext().getResources().getDimension(
                                R.dimen.wifi_dialog_top_off0);
						mDropMenu.setListViewPosition(top, 50,
								(mEapMethodArrays.length > 4) ? 4 : mEapMethodArrays.length);
						mDropMenu.setListViewAdapter(mEapMethodArrays, mIndexEapMethod);
						break;
					case R.id.eapAuthenticationSpinner:
					    top = (int) getContext().getResources().getDimension(
                                R.dimen.wifi_dialog_top_off1);
						mDropMenu.setListViewPosition(top, 50,
								(mEapAuthenticationArrays.length > 4) ? 4 : mEapAuthenticationArrays.length);
						mDropMenu.setListViewAdapter(mEapAuthenticationArrays, mIndexEapAuthentication);
						break;
					case R.id.ipStettingSpinner:
					    top = (int) getContext().getResources().getDimension(
                                R.dimen.wifi_dialog_top_off2);
						mDropMenu.setListViewPosition(top, 50,
								(mIpSettinsArrays.length > 4) ? 4 : mIpSettinsArrays.length);
						mDropMenu.setListViewAdapter(mIpSettinsArrays, mIndexIpSetting);
						break;
					}
					mDropMenu.showDropdownListEnable(true);
				}
				break;
			}			
		}
		
	};

    /**
     * @param context
     * @param accessPoint
     * @param listener
     * @param state 验证密码失败则为1
     */
    public WifiDialog(Context context, AccessPoint accessPoint,
            View.OnClickListener listener, int state) {
        super(context, R.style.MiddleWifiDialogStyle);
        mListener = listener;
        mAccessPoint = accessPoint;
        mSecurity = (accessPoint == null) ? AccessPoint.SECURITY_NONE : accessPoint.security;
        mState = state;
        Log.d(TAG, " WifiDialog mSecurity = " + mSecurity + " mState = " + mState);
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi_dialog_new);
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		mTextTitle = (TextView) findViewById(R.id.title);
		String text;
        if (mState == STATE_NOPASS) {
            text = mAccessPoint.getSSID()
                    + getContext().getResources().getString(
                            R.string.wifi_connect_password_error);
        } else {
            text = mAccessPoint.getSSID();
        }
        mTextTitle.setText(text);
        if (mState == STATE_NOPASS) {
            showDialog();
//            mState = STATE_CONNECT;
        } else if (mAccessPoint.getState() != null) {
            Log.d(TAG, "---showConnectFields---");
            showConnectFields();
        } else if (mAccessPoint.getConfig() != null) {
            Log.d(TAG, "---showConfigFields---");
            showConfigFields();
        } else {
            Log.d(TAG, "---showDialog---");
            showDialog();
        }
        showButtonFields();
        validate();
	}
	
	void showDialog(){
		boolean isEap = false;

		if(mSecurity == AccessPoint.SECURITY_EAP){
			isEap = true;
			mView = findViewById(R.id.layout_eap);
		}else{
			mView = findViewById(R.id.layout_normal);
		}
		mView.setVisibility(View.VISIBLE);
		mTextSecurity = (TextView) mView.findViewById(R.id.security);
		mTextStrength = (TextView) mView.findViewById(R.id.strength);
		mTextSecurity.setText(getSecurityString());
		mTextStrength.setText(getStrengthString());
		showPasswordFields(mView);
		mSpinnerIpSetting = (TextView) mView.findViewById(R.id.ipStettingSpinner);
        mIpSettinsArrays = getContext().getResources().getStringArray(R.array.wifi_ip_setting);
		boolean isStaticIp = false;
        if (mAccessPoint.networkId != INVALID_NETWORK_ID) {
            WifiConfiguration config = mAccessPoint.getConfig();
            if (config.ipAssignment == IpAssignment.STATIC) {
            	mSpinnerIpSetting.setText(mIpSettinsArrays[STATIC_IP]);
            	mIndexIpSetting = STATIC_IP;
            	isStaticIp = true;
            } else {
            	mSpinnerIpSetting.setText(mIpSettinsArrays[DHCP]);
            	mIndexIpSetting = DHCP;
            }
        }
		
		RelativeLayout list_layout = (RelativeLayout) mView.findViewById(R.id.listLayout); 
        ListView list = (ListView) mView.findViewById(R.id.dropdownlist);
        mDropMenu = new DropdownMenu(getContext(),list_layout,list,mHandler,R.layout.dlg_spinner_item );
		mDropMenu.setListViewListener();
        mDropMenu.setButtonListener(mSpinnerIpSetting,mIpSettinsArrays[mIndexIpSetting]);
        Log.d(TAG, " showDialog isEap = " + isEap + " isStaticIp = "+ isStaticIp);
        if(isEap){
        	showEapFields(mView);
        }
        
        if(isStaticIp){
        	showIpConfigFields(mView);
        }
	}
	
	private void showEapFields(View view) {

        mTextEap = (TextView) view.findViewById(R.id.eapmethodSpinner); 
        mTextEapAuthentication = (TextView) view.findViewById(R.id.eapAuthenticationSpinner);
        mEditIdentity = (EditText) view.findViewById(R.id.eap_identity);
        mEditAnonymous = (EditText) view.findViewById(R.id.eap_anonymous);
        
		mEapMethodArrays = getContext().getResources().getStringArray(R.array.wifi_eap_method);
		mIndexEapMethod = 0;
		mDropMenu.setButtonListener(mTextEap,mEapMethodArrays[mIndexEapMethod]);
		
		mEapAuthenticationArrays = getContext().getResources().getStringArray(R.array.wifi_eap_authentication);
		mIndexEapAuthentication = 0;
		mDropMenu.setButtonListener(mTextEapAuthentication,mEapAuthenticationArrays[mIndexEapAuthentication]);
		
		mEapCAArrays = loadCertificates(Credentials.CA_CERTIFICATE);
		mIndexEapCA = 0;
		
		mEapUserArrays = loadCertificates(Credentials.USER_PRIVATE_KEY);
		mIndexEapUser = 0;
	}

	private void showPasswordFields(View view) {

        if (mSecurity == AccessPoint.SECURITY_NONE) {
        	view.findViewById(R.id.layout_password).setVisibility(View.GONE);
            return;
        }
        view.findViewById(R.id.layout_password).setVisibility(View.VISIBLE);
        if(mPasswordView == null){
	        mPasswordView = (EditText) view.findViewById(R.id.password);
//	        mPasswordView.setKeyListener(FilterChars.numberKeyListener);
	        mPasswordView.addTextChangedListener(this);
	        ((CheckBox) view.findViewById(R.id.show_password)).setOnClickListener(this);
        }
        setPasswordAttr(mSecurity,mPasswordView);
	}

	private void showConfigFields() {
   
		View view = findViewById(R.id.layout_config);
		view.setVisibility(View.VISIBLE);
		if(mAccessPoint.getRssi() == Integer.MAX_VALUE){
			view.findViewById(R.id.layout_status).setVisibility(View.INVISIBLE);
			view.findViewById(R.id.layout_strength).setVisibility(View.INVISIBLE);
			view.findViewById(R.id.layout_link_ip).setVisibility(View.GONE);
			mTextSecurity = (TextView) view.findViewById(R.id.security);
			mTextSecurity.setText(getSecurityString());
		}else{
			view.findViewById(R.id.layout_status).setVisibility(View.GONE);
			mTextSecurity = (TextView) view.findViewById(R.id.security);
			mTextStrength = (TextView) view.findViewById(R.id.strength);
			mTextLinkIp = (TextView) view.findViewById(R.id.link_ip);
			mTextSecurity.setText(getSecurityString());
			mTextStrength.setText(getStrengthString());
			if(mAccessPoint.getInfo() == null){
				mTextLinkIp.setText(R.string.IPAddr_text);
			}else{
				mTextLinkIp.setText(Formatter.formatIpAddress(mAccessPoint.getInfo().getIpAddress()));
			}
		}
		
	}

	private void showConnectFields() {

		int connect = 0;
		View view = findViewById(R.id.layout_connect);
		view.setVisibility(View.VISIBLE);
		if(mAccessPoint.getState() == DetailedState.CONNECTED){
			connect = 1;
		}else if((mAccessPoint.getState() == DetailedState.CONNECTING)
				|| (mAccessPoint.getState() == DetailedState.OBTAINING_IPADDR)){
			connect = 2;
		}else if((mAccessPoint.getState() == DetailedState.AUTHENTICATING)){
			connect = 3;
		}else{
			return;
		}
		
		mTextSecurity = (TextView) view.findViewById(R.id.security);
		mTextStrength = (TextView) view.findViewById(R.id.strength);
		mTextStatus = (TextView) view.findViewById(R.id.status);
		//mTextSpeed = (TextView) view.findViewById(R.id.speed); //20120627whj remove mTextSpeed
		mTextSecurity.setText(getSecurityString());
		mTextStrength.setText(getStrengthString());
		Log.d(TAG, " showConnectFields connect = " + connect);
		if(connect == 1){
			view.findViewById(R.id.layout_link_ip).setVisibility(View.VISIBLE);
			mTextLinkIp = (TextView) view.findViewById(R.id.link_ip);
	    	//mTextSpeed.setText(mAccessPoint.getInfo().getLinkSpeed() + WifiInfo.LINK_SPEED_UNITS); //20120627whj remove mTextSpeed
			mTextLinkIp.setText(Formatter.formatIpAddress(mAccessPoint.getInfo().getIpAddress()));
			mTextStatus.setText(R.string.wifi_status_connected);
		}else if(connect == 2){
			view.findViewById(R.id.layout_link_ip).setVisibility(View.GONE);
	    	//mTextSpeed.setText("0" + WifiInfo.LINK_SPEED_UNITS); //20120627whj remove mTextSpeed
			mTextStatus.setText(R.string.wifi_status_connecting);
		}else if(connect == 3){
			view.findViewById(R.id.layout_link_ip).setVisibility(View.GONE);
	    	//mTextSpeed.setText("0" + WifiInfo.LINK_SPEED_UNITS); //20120627whj remove mTextSpeed
			mTextStatus.setText(R.string.wifi_status_checking);
		}
	}
	

    private void showIpConfigFields(View view) {
        WifiConfiguration config = null;


        if (mAccessPoint != null && mAccessPoint.networkId != INVALID_NETWORK_ID) {
            config = mAccessPoint.getConfig();
        }

        if (mIndexIpSetting == STATIC_IP) {
        	view.findViewById(R.id.layout_static_ip).setVisibility(View.VISIBLE);
            if (mIpAddressView == null) {
                mIpAddressView = (TextView) view.findViewById(R.id.ipaddress);
                mIpAddressView.addTextChangedListener(this);
                mGatewayView = (TextView) view.findViewById(R.id.gateway);
                mGatewayView.addTextChangedListener(this);
                mNetworkPrefixLengthView = (TextView) view.findViewById(
                        R.id.network_prefix_length);
                mNetworkPrefixLengthView.addTextChangedListener(this);
                mDns1View = (TextView) view.findViewById(R.id.dns1);
                mDns1View.addTextChangedListener(this);
                mDns2View = (TextView) view.findViewById(R.id.dns2);
                mDns2View.addTextChangedListener(this);
            }
            if (config != null) {
                LinkProperties linkProperties = config.linkProperties;
                Iterator<LinkAddress> iterator = linkProperties.getLinkAddresses().iterator();
                if (iterator.hasNext()) {
                    LinkAddress linkAddress = iterator.next();
                    mIpAddressView.setText(linkAddress.getAddress().getHostAddress());
                    mNetworkPrefixLengthView.setText(Integer.toString(linkAddress
                            .getNetworkPrefixLength()));
                }

                for (RouteInfo route : linkProperties.getRoutes()) {
                    if (route.isDefaultRoute()) {
                        mGatewayView.setText(route.getGateway().getHostAddress());
                        break;
                    }
                }

                Iterator<InetAddress> dnsIterator = linkProperties.getDnses().iterator();
                if (dnsIterator.hasNext()) {
                    mDns1View.setText(dnsIterator.next().getHostAddress());
                }
                if (dnsIterator.hasNext()) {
                    mDns2View.setText(dnsIterator.next().getHostAddress());
                }
            }
        } else {
        	view.findViewById(R.id.layout_static_ip).setVisibility(View.GONE);
        }
    }	
	
	public void setPasswordAttr(int security,TextView password){
		switch(security){
		case AccessPoint.SECURITY_NONE:
		default:
			break;
		case AccessPoint.SECURITY_PSK:
			password.setHint(R.string.wifi_add_psk_password_hint);
			password.setFilters(new InputFilter[]{new InputFilter.LengthFilter(64)});
			break;
		case AccessPoint.SECURITY_EAP:
		case AccessPoint.SECURITY_WEP:
			password.setHint(R.string.wifi_add_password_hint);
			password.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});
			break;
		}
	}

	private void showButtonFields() {
   
		boolean isForget = false;
		boolean isEdit = false;
		View view;
		if((mAccessPoint.getConfig() != null) && (mAccessPoint.getRssi() != Integer.MAX_VALUE)
				&& ((mAccessPoint.getState() == null) 
				|| (mAccessPoint.getState() == DetailedState.DISCONNECTED))){
			findViewById(R.id.layout_two_button).setVisibility(View.GONE);
			view = findViewById(R.id.layout_three_button);
			view.setVisibility(View.VISIBLE);
			isForget = true;
		}else{
			view = findViewById(R.id.layout_two_button);
			view.setVisibility(View.VISIBLE);
			findViewById(R.id.layout_three_button).setVisibility(View.GONE);
		}
		mBtnConfirm = (Button) view.findViewById(R.id.confirm);
		mBtnCancel = (Button) view.findViewById(R.id.cancel);
		mBtnConfirm.setOnClickListener(mListener);
		mBtnCancel.setOnClickListener(mListener);
		if(isForget){
			mBtnConfirm.setText(R.string.wifi_connect);
			mBtnForget = (Button) view.findViewById(R.id.forget);
			mBtnForget.setText(R.string.wifi_forget);
			mBtnForget.setOnClickListener(mListener);
		}else{
			if(mAccessPoint.networkId != INVALID_NETWORK_ID){
				mBtnConfirm.setText(R.string.wifi_forget);
			}else{
				mBtnConfirm.setText(R.string.wifi_connect);
				isEdit = true;
			}
		}
		mBtnCancel.setText(R.string.dlg_cancel);
		if(!isEdit){
			mBtnCancel.requestFocus();
		}
	}
	
	private String getStrengthString() {

        String[] level_arr = getContext().getResources().getStringArray(R.array.wifi_level);
        if((mAccessPoint.getLevel() < 0) || (mAccessPoint.getLevel() > 4)){
        	return level_arr[5];
        }
        return level_arr[mAccessPoint.getLevel()];
	}
	
	private String getSecurityString() {

        String[] security_arr = getContext().getResources().getStringArray(R.array.wifi_security);
        if((mSecurity >= AccessPoint.SECURITY_NONE) || (mSecurity <= AccessPoint.SECURITY_EAP)){
        	return security_arr[mSecurity];
        }
		return security_arr[0];
	}
	

	public WifiConfiguration getConfig(){
		WifiConfiguration config = new WifiConfiguration();
		if (mAccessPoint.networkId == INVALID_NETWORK_ID) {
            config.SSID = AccessPoint.convertToQuotedString(
            		mAccessPoint.ssid);
        } else {
            config.networkId = mAccessPoint.networkId;
        }
		
        switch (mAccessPoint.getSecurity()) {
        case AccessPoint.SECURITY_NONE:
            config.allowedKeyManagement.set(KeyMgmt.NONE);
            break;

        case AccessPoint.SECURITY_WEP:
            config.allowedKeyManagement.set(KeyMgmt.NONE);
            config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
            config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
            if (mPasswordView.length() != 0) {
                int length = mPasswordView.length();
                String password = mPasswordView.getText().toString();
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
            if (mPasswordView.length() != 0) {
                String password = mPasswordView.getText().toString();
                if (password.matches("[0-9A-Fa-f]{64}")) {
                    config.preSharedKey = password;
                } else {
                    config.preSharedKey = '"' + password + '"';
                }
            }
            break;

        case AccessPoint.SECURITY_EAP:
            config.allowedKeyManagement.set(KeyMgmt.WPA_EAP);
            config.allowedKeyManagement.set(KeyMgmt.IEEE8021X);
            config.eap.setValue((String) mEapMethodArrays[mIndexEapMethod]);

            config.phase2.setValue((mIndexEapAuthentication == 0) ? "" :
                    "auth=" + mEapAuthenticationArrays[mIndexEapAuthentication]);
            config.ca_cert.setValue((mIndexEapCA == 0) ? "" :
                    KEYSTORE_SPACE + Credentials.CA_CERTIFICATE +
                    (String) mEapCAArrays[mIndexEapCA]);
            config.client_cert.setValue((mIndexEapUser == 0) ?
                    "" : KEYSTORE_SPACE + Credentials.USER_CERTIFICATE +
                    (String) mEapUserArrays[mIndexEapUser]);
            config.private_key.setValue((mIndexEapUser == 0) ?
                    "" : KEYSTORE_SPACE + Credentials.USER_PRIVATE_KEY +
                    (String) mEapUserArrays[mIndexEapUser]);
            config.identity.setValue((mEditIdentity.length() == 0) ? "" :
            		mEditIdentity.getText().toString());
            config.anonymous_identity.setValue((mEditAnonymous.length() == 0) ? "" :
            		mEditAnonymous.getText().toString());
            if (mPasswordView.length() != 0) {
                config.password.setValue(mPasswordView.getText().toString());
            }
            break;
        }

	    config.proxySettings = mProxySettings;
	    config.ipAssignment = mIpAssignment;
	    config.linkProperties = new LinkProperties(mLinkProperties);

		return config;
	}
	
    private void validate() {
        // TODO: make sure this is complete.
    	boolean enabled = false;
    	
        if (((mAccessPoint == null || mAccessPoint.networkId == INVALID_NETWORK_ID) &&
                ((mSecurity == AccessPoint.SECURITY_WEP && mPasswordView.length() == 0) ||
                (mSecurity == AccessPoint.SECURITY_EAP && mPasswordView.length() == 0) ||		
                (mSecurity == AccessPoint.SECURITY_PSK && mPasswordView.length() < 8)))) {
        	enabled = false;
        } else {
            if (ipAndProxyFieldsAreValid()) {
                enabled = true;
            } else {
                enabled = false;
            }
        }
        if (mState == STATE_NOPASS) {
            if (mPasswordView == null
                    || (mPasswordView != null && mPasswordView.length() < 8)) {
                enabled = false;
            }
        } 
        Log.d(TAG, "------validate---- mState = " + mState + " enabled = " + enabled);
    	mBtnConfirm.setEnabled(enabled);
    	mBtnConfirm.setFocusable(enabled);
    	mBtnConfirm.setAlpha(enabled ? 1f : 0.7f);
    }

	@Override
	public void afterTextChanged(Editable s) {

		validate();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

		
	}

	@Override
	public void onClick(View view) {

		mPasswordView.setInputType(
                InputType.TYPE_CLASS_TEXT | (((CheckBox) view).isChecked() ?
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                InputType.TYPE_TEXT_VARIATION_PASSWORD));
//		mPasswordView.setKeyListener(FilterChars.numberKeyListener);
	}
	

    private boolean ipAndProxyFieldsAreValid() {
        mLinkProperties.clear();
        mIpAssignment = (mSpinnerIpSetting != null && 
        					mIndexIpSetting == STATIC_IP) ?
        					IpAssignment.STATIC : IpAssignment.DHCP;

        if (mIpAssignment == IpAssignment.STATIC) {
            int result = validateIpConfigFields(mLinkProperties);
            if (result != 0) {
                return false;
            }
        }
        /*
        mProxySettings = (mProxySettingsSpinner != null &&
                mProxySettingsSpinner.getSelectedItemPosition() == PROXY_STATIC) ?
                ProxySettings.STATIC : ProxySettings.NONE;

        if (mProxySettings == ProxySettings.STATIC) {
            String host = mProxyHostView.getText().toString();
            String portStr = mProxyPortView.getText().toString();
            String exclusionList = mProxyExclusionListView.getText().toString();
            int port = 0;
            int result = 0;
            try {
                port = Integer.parseInt(portStr);
                result = ProxySelector.validate(host, portStr, exclusionList);
            } catch (NumberFormatException e) {
                result = R.string.proxy_error_invalid_port;
            }
            if (result == 0) {
                ProxyProperties proxyProperties= new ProxyProperties(host, port, exclusionList);
                mLinkProperties.setHttpProxy(proxyProperties);
            } else {
                return false;
            }
        }*/
        return true;
    }

    private int validateIpConfigFields(LinkProperties linkProperties) {
        String ipAddr = mIpAddressView.getText().toString();
        InetAddress inetAddr = null;
        try {
            inetAddr = NetworkUtils.numericToInetAddress(ipAddr);
        } catch (IllegalArgumentException e) {
            return R.string.wifi_ip_settings_invalid_ip_address;
        }

        int networkPrefixLength = -1;
        try {
            networkPrefixLength = Integer.parseInt(mNetworkPrefixLengthView.getText().toString());
        } catch (NumberFormatException e) {
            // Use -1
        }
        if (networkPrefixLength < 0 || networkPrefixLength > 32) {
            return R.string.wifi_ip_settings_invalid_network_prefix_length;
        }
        linkProperties.addLinkAddress(new LinkAddress(inetAddr, networkPrefixLength));

        String gateway = mGatewayView.getText().toString();
        InetAddress gatewayAddr = null;
        try {
            gatewayAddr = NetworkUtils.numericToInetAddress(gateway);
        } catch (IllegalArgumentException e) {
            return R.string.wifi_ip_settings_invalid_gateway;
        }
        linkProperties.addRoute(new RouteInfo(gatewayAddr));

        String dns = mDns1View.getText().toString();
        InetAddress dnsAddr = null;
        try {
            dnsAddr = NetworkUtils.numericToInetAddress(dns);
        } catch (IllegalArgumentException e) {
            return R.string.wifi_ip_settings_invalid_dns;
        }
        linkProperties.addDns(dnsAddr);
        if (mDns2View.length() > 0) {
            dns = mDns2View.getText().toString();
            try {
                dnsAddr = NetworkUtils.numericToInetAddress(dns);
            } catch (IllegalArgumentException e) {
                return R.string.wifi_ip_settings_invalid_dns;
            }
            linkProperties.addDns(dnsAddr);
        }
        return 0;
    }


    private String[] loadCertificates(String prefix) {
        final Context context = getContext();
        final String unspecified = context.getString(R.string.wifi_unspecified);

        String[] certs = KeyStore.getInstance().saw(prefix);
        if (certs == null || certs.length == 0) {
            certs = new String[] {unspecified};
        } else {
            final String[] array = new String[certs.length + 1];
            array[0] = unspecified;
            System.arraycopy(certs, 0, array, 1, certs.length);
            certs = array;
        }
        return certs;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        
        Log.d(TAG, " view id = " + v.getId() + " keyCode = " + keyCode + event.getAction());
        return false;
    }
}
