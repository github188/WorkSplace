package com.lenovo.settings.wifi;

import static android.net.wifi.WifiConfiguration.INVALID_NETWORK_ID;

import com.lenovo.settings.R;
import com.lenovo.settings.Object.FilterChars;
import com.lenovo.settings.DropdownMenu;
import com.lenovo.settings.R.array;
import com.lenovo.settings.R.id;
import com.lenovo.settings.R.layout;
import com.lenovo.settings.R.string;

import android.app.Dialog;
import android.content.Context;
import android.net.LinkProperties;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.IpAssignment;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiConfiguration.ProxySettings;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.security.Credentials;
import android.security.KeyStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WifiEapDialog extends Dialog implements View.OnClickListener,
						TextWatcher {

	private static final String TAG = "WifiEapDialog";
    private static final String KEYSTORE_SPACE = "keystore://";
	private View.OnClickListener mListener;
	private AccessPoint mAccessPoint;
	private int mSecurity;
	private TextView mTextSsid,mTextLevel,mTextSecurity,mTextEap,mTextEapAuthentication;
	private Button mBtnConfirm,mBtnCancel;
	private EditText mEditIdentity,mEditAnonymous,mEditPassword;
	private CheckBox mShowPassword;	
	private String mDigits,mTmpStr;
	private DropdownMenu mDropMenu;
	private String[] mEapMethodArrays;
	private int mIndexEapMethod;
	private String[] mEapAuthenticationArrays;
	private int mIndexEapAuthentication;
    private IpAssignment mIpAssignment = IpAssignment.UNASSIGNED;
    private ProxySettings mProxySettings = ProxySettings.UNASSIGNED;
    private LinkProperties mLinkProperties = new LinkProperties();
	private String[] mEapCAArrays;
	private String[] mEapUserArrays;
	private int mIndexEapCA;
	private int mIndexEapUser;
	
	
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
					}
				}
				break;
			case DropdownMenu.DROPDOWN_MENU_GET_ADAPTER:
				if(msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_CAN_EDIT){
					EditText edit = (EditText) msg.obj;
				}else if(msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_NOT_EDIT){
					TextView text = (TextView) msg.obj;
					int id = text.getId();
					Log.e(TAG,"id = "+id);
					switch(id){
					case R.id.eapmethodSpinner:
						mDropMenu.setListViewPosition(0, 50,
								(mEapMethodArrays.length > 4) ? 4 : mEapMethodArrays.length);
						mDropMenu.setListViewAdapter(mEapMethodArrays, mIndexEapMethod);
						break;
					case R.id.eapAuthenticationSpinner:
						mDropMenu.setListViewPosition(90, 50,
								(mEapAuthenticationArrays.length > 4) ? 4 : mEapAuthenticationArrays.length);
						mDropMenu.setListViewAdapter(mEapAuthenticationArrays, mIndexEapAuthentication);
						break;
					}
					mDropMenu.showDropdownListEnable(true);
				}
				break;
			}			
		}
		
	};

	public WifiEapDialog(Context context, int theme, AccessPoint accessPoint,
			View.OnClickListener listener) {
		super(context, theme);
		
        mListener = listener;
        mAccessPoint = accessPoint;
        mSecurity = (accessPoint == null) ? AccessPoint.SECURITY_NONE : accessPoint.security;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState); 
		showEapDialog();
	}
	
	void showEapDialog(){
		setContentView(R.layout.wifi_eap_connect_dialog);
		mTextSsid = (TextView) findViewById(R.id.text_ssid);
        mTextLevel = (TextView) findViewById(R.id.text_wifi_level);
        mTextSecurity = (TextView) findViewById(R.id.text_wifi_security); 
        mTextEap = (TextView) findViewById(R.id.eapmethodSpinner); 
        mTextEapAuthentication = (TextView) findViewById(R.id.eapAuthenticationSpinner);
        mEditIdentity = (EditText) findViewById(R.id.eap_identity);
        mEditAnonymous = (EditText) findViewById(R.id.eap_anonymous);
        mEditPassword = (EditText) findViewById(R.id.ed_wifi_password);
        mShowPassword = (CheckBox) findViewById(R.id.checkBox_password);
        mBtnConfirm = (Button) findViewById(R.id.btn_dlg_confirm);        
        mBtnCancel = (Button) findViewById(R.id.btn_dlg_cancel);
        mBtnConfirm.setText(R.string.wifi_connect);
        mBtnCancel.setText(R.string.dlg_cancel);
        
        mTextSsid.setText(mAccessPoint.ssid);
        String[] security_arr = getContext().getResources().getStringArray(R.array.wifi_security);
        if((mSecurity >= AccessPoint.SECURITY_NONE) || (mSecurity <= AccessPoint.SECURITY_EAP)){
        	mTextSecurity.setText(security_arr[mSecurity]);
        }
        String[] level_arr = getContext().getResources().getStringArray(R.array.wifi_level);
        if((mAccessPoint.getLevel() < 0) || (mAccessPoint.getLevel() > 4)){
        	mTextLevel.setText(level_arr[5]);
        }else{
        	mTextLevel.setText(level_arr[mAccessPoint.getLevel()]);
        }    

        mEditPassword.setKeyListener(FilterChars.numberKeyListener);
        mEditPassword.addTextChangedListener(this);
        mShowPassword.setOnClickListener(this);
        mBtnConfirm.setOnClickListener(mListener);
        mBtnCancel.setOnClickListener(mListener);
        mDigits = getContext().getString(R.string.name_password_digits); 
        
        RelativeLayout list_layout = (RelativeLayout) findViewById(R.id.listLayout); 
        ListView list = (ListView) findViewById(R.id.dropdownlist);
        mDropMenu = new DropdownMenu(getContext(),list_layout,list,mHandler,R.layout.dlg_spinner_item );
		mDropMenu.setListViewListener();
        
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
		
        validate();
	}

	@Override
	public void onClick(View view) {

		mEditPassword.setInputType(
                InputType.TYPE_CLASS_TEXT | (((CheckBox) view).isChecked() ?
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                InputType.TYPE_TEXT_VARIATION_PASSWORD));
		mEditPassword.setKeyListener(FilterChars.numberKeyListener);
	}
	
	@Override
	public void afterTextChanged(Editable s) {

		validate();
        /*Log.d(TAG, "<><>afterTextChanged<><>" + s.toString());   
        
        String str = s.toString();   
        if(str.equals(mTmpStr)){   
            return;   
        }   
           
        StringBuffer sb = new StringBuffer();   
        for(int i = 0; i < str.length(); i++){   
            if(mDigits.indexOf(str.charAt(i)) >= 0){   
                sb.append(str.charAt(i));   
            }   
        }
        mTmpStr = sb.toString(); 
        mEditPassword.setTextKeepState(mTmpStr);*/
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

		//mTmpStr = s.toString();   
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

		
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
            if (mEditPassword.length() != 0) {
                int length = mEditPassword.length();
                String password = mEditPassword.getText().toString();
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
            if (mEditPassword.length() != 0) {
                String password = mEditPassword.getText().toString();
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
            if (mEditPassword.length() != 0) {
                config.password.setValue(mEditPassword.getText().toString());
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
        if ((mTextSsid != null && mTextSsid.length() == 0) ||
                ((mAccessPoint == null || mAccessPoint.networkId == -1) &&
                ((mSecurity == AccessPoint.SECURITY_WEP && mEditPassword.length() == 0) ||
                (mSecurity == AccessPoint.SECURITY_EAP && mEditPassword.length() == 0) ||		
                (mSecurity == AccessPoint.SECURITY_PSK && mEditPassword.length() < 8)))) {
        	mBtnConfirm.setEnabled(false);
        } else {
        	mBtnConfirm.setEnabled(true);
        }
        
        if((mSecurity == AccessPoint.SECURITY_WEP) || (mSecurity == AccessPoint.SECURITY_EAP)){
        	mEditPassword.setHint(R.string.wifi_add_password_hint);
        	mEditPassword.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});
        }else if(mSecurity == AccessPoint.SECURITY_PSK){
        	mEditPassword.setHint(R.string.wifi_add_psk_password_hint);
        	mEditPassword.setFilters(new InputFilter[]{new InputFilter.LengthFilter(64)});
        }
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
    
}
