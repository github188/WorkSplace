package com.lenovo.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.net.ethernet.EthernetManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.security.Credentials;
import android.security.KeyStore;

import com.android.internal.util.AsyncChannel;
import com.lenovo.settings.Object.FilterChars;
import com.lenovo.settings.wifi.AccessPoint;

public class NetworkWirelessAdd extends Fragment implements OnCheckedChangeListener {

	protected static final String TAG = "NetworkWirelessAdd";
	private View mView;
	private EditText ssidText, ssidPwd,mEapIdentity,mEapAnonymous;
	private CheckBox ssidCheckBox;
	private Button ssid_save, ssid_cancel;
	private TextView mSpinner,mEapMethodSpinner,mEapAuthenticationSpinner,
					mEapCACertificateSpinner,mEapUserCertificateSpinner;
	private WifiManager mWifiManager;
	private WifiConfiguration  mWifiConfig = new WifiConfiguration();
	protected boolean mEnableList = false;
	protected boolean mListEnd = false;
	private DropdownMenu mDropMenu;
	private String[] mSecurityArrays,mEapMethodArrays,mEapAuthenticationArrays,mEapCAArrays,mEapUserArrays;
	private int mIndexSecurity,mIndexEapMethod,mIndexEapAuthentication,mIndexEapCA,mIndexEapUser;

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
					int id = text.getId();
					switch(id){
					case R.id.safeSpinner:
						if(mIndexSecurity != position){
							mIndexSecurity = position;
							setPasswordHint();
							setPasswordMaxlength();
							if(position == 3){
								mLayoutPassword.setVisibility(View.VISIBLE);
								mLayoutEap.setVisibility(View.VISIBLE);
							}else if(position == 0){
								mLayoutPassword.setVisibility(View.GONE);
								mLayoutEap.setVisibility(View.GONE);
							}else{
								mLayoutPassword.setVisibility(View.VISIBLE);
								mLayoutEap.setVisibility(View.GONE);								
							}
							setSaveButton();
						}
						break;
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
					case R.id.eapCACertificateSpinner:
						if(mIndexEapCA != position){
							mIndexEapCA = position;
						}
						break;
					case R.id.eapUserCertificateSpinner:
						if(mIndexEapUser != position){
							mIndexEapUser = position;
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
					case R.id.safeSpinner:
						int safeOffset = (int) getResources().getDimension(R.dimen.net_wireless_add_safe_spinner_offset);
						mDropMenu.setListViewPosition(safeOffset, mSecurityArrays.length);
						mDropMenu.setListViewAdapter(mSecurityArrays, mIndexSecurity);
						break;
					case R.id.eapmethodSpinner:
						int eapOffset = (int) getResources().getDimension(R.dimen.net_wireless_add_eap_spinner_offset);
						mDropMenu.setListViewPosition(eapOffset, 
								(mEapMethodArrays.length > 4) ? 4 : mEapMethodArrays.length);
						mDropMenu.setListViewAdapter(mEapMethodArrays, mIndexEapMethod);
						break;
					case R.id.eapAuthenticationSpinner:
						int eapAuthenticationOffset = (int) getResources().getDimension(R.dimen.net_wireless_add_id_spinner_offset);
						mDropMenu.setListViewPosition(eapAuthenticationOffset, 
								(mEapAuthenticationArrays.length > 4) ? 4 : mEapAuthenticationArrays.length);
						mDropMenu.setListViewAdapter(mEapAuthenticationArrays, mIndexEapAuthentication);
						break;
					case R.id.eapCACertificateSpinner:
						int eapCAOffset = (int) getResources().getDimension(R.dimen.net_wireless_add_ca_spinner_offset);
						mDropMenu.setListViewPosition(eapCAOffset, 
								(mEapCAArrays.length >= 4) ? 4 : mEapCAArrays.length);
						mDropMenu.setListViewAdapter(mEapCAArrays, mIndexEapCA);
						break;
					case R.id.eapUserCertificateSpinner:
						int eapUserOffset = (int) getResources().getDimension(R.dimen.net_wireless_add_user_spinner_offset);
						mDropMenu.setListViewPosition(eapUserOffset, 
								(mEapUserArrays.length >= 4) ? 4 : mEapUserArrays.length);
						mDropMenu.setListViewAdapter(mEapUserArrays, mIndexEapUser);
						break;
					}
					mDropMenu.showDropdownListEnable(true);
				}
				break;
			}
		}
		
	};
	private RelativeLayout mLayoutPassword,mLayoutEap;
	
	private static final String KEYSTORE_SPACE = "keystore://";
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mView = inflater.inflate(R.layout.network_wireless_add, container,
				false);

		ssidText = (EditText) mView.findViewById(R.id.ssidText);
		ssidPwd = (EditText) mView.findViewById(R.id.ssidPwd);
		mEapIdentity = (EditText) mView.findViewById(R.id.eap_identity);
		mEapAnonymous = (EditText) mView.findViewById(R.id.eap_anonymous);
		ssidCheckBox = (CheckBox) mView.findViewById(R.id.ssidCheckBox);
		mLayoutPassword = (RelativeLayout) mView.findViewById(R.id.relativeLayoutPassword);
		mLayoutEap = (RelativeLayout) mView.findViewById(R.id.relativeLayoutEap);
		mSpinner = (TextView) mView.findViewById(R.id.safeSpinner);
		mEapMethodSpinner = (TextView) mView.findViewById(R.id.eapmethodSpinner);
		mEapAuthenticationSpinner = (TextView) mView.findViewById(R.id.eapAuthenticationSpinner);
		mEapCACertificateSpinner = (TextView) mView.findViewById(R.id.eapCACertificateSpinner);
		mEapUserCertificateSpinner = (TextView) mView.findViewById(R.id.eapUserCertificateSpinner);
		
		
		//ssidText.requestFocus();
		ssidCheckBox.setOnCheckedChangeListener(this);
		mWifiManager = (WifiManager) getActivity().getSystemService(
				Context.WIFI_SERVICE);
		mWifiManager.asyncConnect(getActivity(), new WifiServiceHandler());
		


		mDropMenu = new DropdownMenu(getActivity(),mView,mHandler );
		mSecurityArrays = getResources().getStringArray(R.array.wifi_security);
		mIndexSecurity = 0;
		mDropMenu.setButtonListener(mSpinner,mSecurityArrays[mIndexSecurity]);
		mDropMenu.setListViewListener();

		mEapMethodArrays = getResources().getStringArray(R.array.wifi_eap_method);
		mIndexEapMethod = 0;
		mDropMenu.setButtonListener(mEapMethodSpinner,mEapMethodArrays[mIndexEapMethod]);
		
		mEapAuthenticationArrays = getResources().getStringArray(R.array.wifi_eap_authentication);
		mIndexEapAuthentication = 0;
		mDropMenu.setButtonListener(mEapAuthenticationSpinner,mEapAuthenticationArrays[mIndexEapAuthentication]);

		mEapCAArrays = loadCertificates(Credentials.CA_CERTIFICATE);
		mIndexEapCA = 0;
		mDropMenu.setButtonListener(mEapCACertificateSpinner,mEapCAArrays[mIndexEapCA]);
		
		mEapUserArrays = loadCertificates(Credentials.USER_PRIVATE_KEY);
		mIndexEapUser = 0;
		mDropMenu.setButtonListener(mEapUserCertificateSpinner,mEapUserArrays[mIndexEapUser]);
		
		ssidText.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
		
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
		
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
		
				/*String ssid = ssidText.getText().toString();
				int len = ssid.getBytes().length;
				//if((ssidText.getText().length() > 0) && (ssidText.getText().toString().indexOf(" ") == -1)){
				if((len > 0) && (len <= 32)){
					if(mIndexSecurity != 0){
						if(ssidPwd.getText().length() > 6){
							showSaveButtonEnable(true);	
						}else{
							showSaveButtonEnable(false);	
						}
					}else{
						showSaveButtonEnable(true);	
					}
				}else{
					showSaveButtonEnable(false);	
				}*/
				setSaveButton();
			}
			
		});
		ssidPwd.setKeyListener(FilterChars.numberKeyListener);
		ssidPwd.addTextChangedListener(new TextWatcher(){
		    String tmp = "";   
		    String digits = getActivity().getString(R.string.name_password_digits); 

			@Override
			public void afterTextChanged(Editable s) {
		
				 
		        /*Log.d(TAG, "<><>afterTextChanged<><>" + s.toString());   
		           
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
		        ssidPwd.setTextKeepState(tmp);*/
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
		
				//tmp = s.toString(); 
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
		
				/*if((ssidPwd.getText().length() >= 6) && (ssidText.getText().length() > 0)){
					showSaveButtonEnable(true);					
				}else{
					showSaveButtonEnable(false);
				}*/
				setSaveButton();
			}
			
		});
		ssid_save = (Button) mView.findViewById(R.id.ssid_save);

		ssid_save.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Log.d("rony test", "save wifi network!");
				saveWifiNetwork(mIndexSecurity);
				getActivity().getFragmentManager().popBackStack(); 
			}
		});
		ssid_cancel = (Button) mView.findViewById(R.id.ssid_cancel);

		ssid_cancel.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Log.i("ssid_cancel", "onClick");
				//getActivity().finish();
				getActivity().getFragmentManager().popBackStack(); 
			}
		});
		LenovoSettingsActivity.setTitleFocus(false);
		ssidText.requestFocus();
		//ssidText.setFocusable(true);
		//ssidText.setFocusableInTouchMode(true);
		showSaveButtonEnable(false);

		return mView;

	}
	
	public void setSaveButton(){

		String ssid = ssidText.getText().toString();
		int len = ssid.getBytes().length;
		//if((ssidText.getText().length() > 0) && (ssidText.getText().toString().indexOf(" ") == -1)){
		if((len > 0) && (len <= 32)){
			if(mIndexSecurity != AccessPoint.SECURITY_NONE){
				int min_size = 1;
				int max_size = 16;
				if(mIndexSecurity == AccessPoint.SECURITY_PSK){
					min_size = 8;
					max_size = 64;
				}
				String password = ssidPwd.getText().toString();
				int password_len = password.getBytes().length;
				if((password_len < min_size) || (password_len > max_size)){
					showSaveButtonEnable(false);	
				}else{
					showSaveButtonEnable(true);	
				}
			}else{
				showSaveButtonEnable(true);	
			}
		}else{
			showSaveButtonEnable(false);	
		}
	}
	
	public void setPasswordHint(){
		switch(mIndexSecurity){
		case AccessPoint.SECURITY_NONE:
		default:
			break;
		case AccessPoint.SECURITY_PSK:
			ssidPwd.setHint(R.string.wifi_add_psk_password_hint);
			break;
		case AccessPoint.SECURITY_EAP:
		case AccessPoint.SECURITY_WEP:
			ssidPwd.setHint(R.string.wifi_add_password_hint);
			break;
		}
	}
	
	public void setPasswordMaxlength(){
		switch(mIndexSecurity){
		case AccessPoint.SECURITY_NONE:
		default:
			break;
		case AccessPoint.SECURITY_PSK:
			ssidPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(64)});
			break;
		case AccessPoint.SECURITY_EAP:
		case AccessPoint.SECURITY_WEP:
			ssidPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});
			break;
		}		
	}

	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

		ssidPwd.setInputType(
                InputType.TYPE_CLASS_TEXT | (ssidCheckBox.isChecked() ?
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                InputType.TYPE_TEXT_VARIATION_PASSWORD));

		ssidPwd.setKeyListener(FilterChars.numberKeyListener);
	}

	
	private void saveWifiNetwork(int postion)
	{

		mWifiConfig.SSID = AccessPoint.convertToQuotedString(
				ssidText.getText().toString());
		//mWifiConfig.SSID='"'+ssidText.getEditableText().toString()+'"';
		mWifiConfig.hiddenSSID = true;
		//mWifiConfig.status = WifiConfiguration.Status.ENABLED;
		switch (postion)
		{
		   case 0:
			   mWifiConfig.allowedKeyManagement.set(KeyMgmt.NONE);
			   break;
		   case 1:
			   mWifiConfig.allowedKeyManagement.set(KeyMgmt.NONE);
			   mWifiConfig.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
			   mWifiConfig.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
			   
               if (ssidPwd.length() != 0) {
                   int length = ssidPwd.length();
                   String password = ssidPwd.getText().toString();
                   
                   // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
                   if ((length == 10 || length == 26 || length == 58) &&
                           password.matches("[0-9A-Fa-f]*")) {
                	   mWifiConfig.wepKeys[0] = password;
                   } else {
                	   mWifiConfig.wepKeys[0] = '"' + password + '"';
                   }
               }
               break;
		  case  2:
			  mWifiConfig.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
              if (ssidPwd.length() != 0) {
                  String password = ssidPwd.getText().toString();
                  if (password.matches("[0-9A-Fa-f]{64}")) {
                	  mWifiConfig.preSharedKey = password;
                  } else {
                	  mWifiConfig.preSharedKey = '"' + password + '"';
                  }
              }
			  
            break;
            
		  case 3:
			  mWifiConfig.allowedKeyManagement.set(KeyMgmt.WPA_EAP);
			  mWifiConfig.allowedKeyManagement.set(KeyMgmt.IEEE8021X);
			  mWifiConfig.eap.setValue((String) mEapMethodArrays[mIndexEapMethod]);

			  mWifiConfig.phase2.setValue((mIndexEapAuthentication == 0) ? "" :
                      "auth=" + mEapAuthenticationArrays[mIndexEapAuthentication]);
			  mWifiConfig.ca_cert.setValue((mIndexEapCA == 0) ? "" :
                      KEYSTORE_SPACE + Credentials.CA_CERTIFICATE +
                      (String) mEapCAArrays[mIndexEapCA]);
			  mWifiConfig.client_cert.setValue((mIndexEapUser == 0) ?
                      "" : KEYSTORE_SPACE + Credentials.USER_CERTIFICATE +
                      (String) mEapUserArrays[mIndexEapUser]);
			  mWifiConfig.private_key.setValue((mIndexEapUser == 0) ?
                      "" : KEYSTORE_SPACE + Credentials.USER_PRIVATE_KEY +
                      (String) mEapUserArrays[mIndexEapUser]);
			  mWifiConfig.identity.setValue((mEapIdentity.length() == 0) ? "" :
				  		mEapIdentity.getText().toString());
			  mWifiConfig.anonymous_identity.setValue((mEapAnonymous.length() == 0) ? "" :
				  		mEapAnonymous.getText().toString());
              if (ssidPwd.length() != 0) {
            	  mWifiConfig.password.setValue(ssidPwd.getText().toString());
              }
              break;             

		}
		 mWifiManager.saveNetwork(mWifiConfig);
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
   

   void showPassowrdEnable(boolean enable){
	   ssidPwd.setEnabled(enable);
	   ssidPwd.setFocusable(enable);
   }
   

   void showSaveButtonEnable(boolean enable){
	   ssid_save.setEnabled(enable);
	   ssid_save.setFocusable(enable);
   }
   

   private String[] loadCertificates(String prefix) {
       final Context context = getActivity();
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
