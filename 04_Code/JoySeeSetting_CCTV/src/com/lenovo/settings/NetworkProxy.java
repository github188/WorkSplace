package com.lenovo.settings;

import java.util.regex.Pattern;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.ProxyProperties;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class NetworkProxy extends Fragment { 
	private View mView;
	private TextView proxyIPText,proxyPortText,proxySkipText;
	private EditText proxyaddr,proxyport;
	private TextView httpSpinner,httpSkipSpinner;
	private DropdownMenu mDropMenu;
	private String[] mHttpArrays;
	private int http_index;
	private String[] mHttpSkipArrays;
	private int httpskip_index;	
	private static String TAG = "NetworkProxy";

    // Matches blank input, ips, and domain names
    private static final String HOSTNAME_REGEXP =
            "^$|^[a-zA-Z0-9]+(\\-[a-zA-Z0-9]+)*(\\.[a-zA-Z0-9]+(\\-[a-zA-Z0-9]+)*)*$";
    private static final Pattern HOSTNAME_PATTERN;
    private static final String EXCLUSION_REGEXP =
            "$|^(.?[a-zA-Z0-9]+(\\-[a-zA-Z0-9]+)*(\\.[a-zA-Z0-9]+(\\-[a-zA-Z0-9]+)*)*)+$";
    private static final Pattern EXCLUSION_PATTERN;
	private static final String STR_HTTP_PROXY_SWITCHER = "HttpProxy";
	private static final String STR_HTTP_PROXY_SKIP = "HttpProxySkip";
	private static final String STR_HTTP_PROXY_ADDRESS = "HttpProxyAddress";
	private static final String STR_HTTP_PROXY_PORT = "HttpProxyPort";
    static {
        HOSTNAME_PATTERN = Pattern.compile(HOSTNAME_REGEXP);
        EXCLUSION_PATTERN = Pattern.compile(EXCLUSION_REGEXP);
    }
	
	
	
    private Handler mHandler = new Handler() {
        
        @Override
        public void handleMessage(Message msg) {
            
            switch (msg.what) {
                case DropdownMenu.DROPDOWN_MENU_ITEM_CHICK:
                    if (msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_CAN_EDIT) {
                        EditText edit = (EditText) msg.obj;
                    } else if (msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_NOT_EDIT) {
                        TextView text = (TextView) msg.obj;
                        int position = msg.arg2;
                        if (text == httpSpinner) {
                            if (http_index != position) {
                                http_index = position;
                                if (position == 1) {
                                    enableProxyWidget(true);
                                    setWidgetAlpha(1f);
                                } else {
                                    // clearProxy();
                                    enableProxyWidget(false);
                                    setWidgetAlpha(0.3f);
                                }
                                settingSharedPreferences(STR_HTTP_PROXY_SWITCHER, http_index);
                            }
                        } else if (text == httpSkipSpinner) {
                            if (httpskip_index != position) {
                                httpskip_index = position;
                            }
                        } else {
                            break;
                        }
                    }
                    break;
                case DropdownMenu.DROPDOWN_MENU_GET_ADAPTER:
                    if (msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_CAN_EDIT) {
                        EditText edit = (EditText) msg.obj;
                    } else if (msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_NOT_EDIT) {
                        TextView text = (TextView) msg.obj;
                        if (text == httpSpinner) {
                            int top = (int) getResources().getDimension(
                                    R.dimen.net_proxy_down_list_top_offset1);
                            mDropMenu.setListViewPosition(top, 2);
                            mDropMenu.setListViewAdapter(mHttpArrays, http_index);
                        } else if (text == httpSkipSpinner) {
                            // mDropMenu.setListViewPosition(263, 2);
                            int top = (int) getResources().getDimension(
                                    R.dimen.net_proxy_down_list_top_offset2);
                            mDropMenu.setListViewPosition(top, 2);
                            mDropMenu.setListViewAdapter(mHttpSkipArrays, httpskip_index);
                        } else {
                            break;
                        }
                        mDropMenu.showDropdownListEnable(true);
                    }
                    break;
            }
        }
        
    };
	private SharedPreferences settings;
	private Editor editor;
	
	
	@Override
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		 LenovoSettingsActivity.isProxy = true;
		 mView = inflater.inflate(R.layout.network_proxy, container, false);
		 proxyIPText = (TextView) mView.findViewById(R.id.proxyIPText);
		 proxyPortText = (TextView) mView.findViewById(R.id.proxyPortText);
		 proxySkipText = (TextView) mView.findViewById(R.id.proxySkipText);
		
		
		 proxyaddr = (EditText) mView.findViewById(R.id.proxyaddr);
		 proxyport = (EditText) mView.findViewById(R.id.proxyport);
		  
		 httpSpinner = (TextView) mView.findViewById(R.id.httpSpinner);
		 httpSkipSpinner = (TextView) mView.findViewById(R.id.httpSkipSpinner);
		  
		 mDropMenu = new DropdownMenu(getActivity(),mView,mHandler );
		 
		 settings = getActivity().getSharedPreferences("com.lenovo.settings", Context.MODE_WORLD_READABLE
					| Context.MODE_WORLD_WRITEABLE);
		 editor = settings.edit();
		 //http_index = getIndexProxy();
		 initProxy();
		 //http_index = SystemProperties.getInt(STR_HTTP_PROXY_SWITCHER, 0);
		 http_index = settings.getInt(STR_HTTP_PROXY_SWITCHER, 0);
        if (http_index == 1) {
            enableProxyWidget(true);
            setWidgetAlpha(1f);
        } else {
            enableProxyWidget(false);
            setWidgetAlpha(0.3f);
        }
		 mHttpArrays = getResources().getStringArray(R.array.proxy_array);
		 mDropMenu.setButtonListener(httpSpinner,mHttpArrays[http_index]);

		 mHttpSkipArrays = getResources().getStringArray(R.array.proxy_array);
		 mDropMenu.setButtonListener(httpSkipSpinner,mHttpSkipArrays[httpskip_index]);
		 mDropMenu.setListViewListener();
		 
		/* proxyport.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
			
					if ((keyCode == KeyEvent.KEYCODE_ENTER) || (keyCode == KeyEvent.KEYCODE_DPAD_CENTER)) {
						Log.i("proxyport","KEYCODE_ENTER");
						saveProxy();
						return true;
					}
					return false;
				}
		});*/

		LenovoSettingsActivity.setTitleFocus(false);
		httpSpinner.requestFocus();
		//httpSkipSpinner.setFocusable(true);
		//httpSkipSpinner.setFocusableInTouchMode(true);
		

		return mView;
	}
	
	@Override
	public void onStop() {

	    Log.d(TAG, " ----onStop()----");
	    saveProxy();
	    LenovoSettingsActivity.isProxy = false;
        if (ErrorDialog != null && ErrorDialog.isShowing()) {
            ErrorDialog.dismiss();
        }
        if (ExitDialog != null && ExitDialog.isShowing()) {
            ExitDialog.dismiss();
        }
		super.onStop();
	}

	private void initProxy(){

        String hostname = "";
        int port = -1;
        String exclList = "";
        String portStr = settings.getString(STR_HTTP_PROXY_PORT, "");
        hostname = settings.getString(STR_HTTP_PROXY_ADDRESS, "");
        httpskip_index = settings.getInt(STR_HTTP_PROXY_SKIP, 1);
        proxyaddr.setText(hostname); 
        proxyport.setText(portStr);
     /*   ConnectivityManager cm =
                (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        ProxyProperties proxy = cm.getGlobalProxy();
        if(proxy != null){
  	      	hostname = proxy.getHost();
  	      	port = proxy.getPort();
  	      	exclList = proxy.getExclusionList();        	
        }
	    if(hostname == null){
	    	hostname = "";
	    }
	    proxyaddr.setText(hostname); 
	    String portStr = port == -1 ? "" : Integer.toString(port);
	    proxyport.setText(portStr);
	    if(exclList != null){
	    	httpskip_index = 1;
	    }else{
	    	httpskip_index = 0;
	    }
*/	    
        Log.i(TAG, " initProxy hostname = " + hostname + " port = " + port + " exclList = "
                + exclList);
	}
	
   private int getIndexProxy()
   {
       ConnectivityManager cm =
           (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

      ProxyProperties proxy = cm.getGlobalProxy();
      if (cm.getGlobalProxy() == null) {
    	  return 0;
      }
      String hostname = proxy.getHost();
      if(hostname == null){
    	  hostname = "";
      }
      proxyaddr.setText(hostname);
      String portStr = proxy.getPort() == -1 ? "" : Integer.toString(proxy.getPort());      
      if(portStr == null){
    	  portStr = "";
      }
      proxyport.setText(portStr);
      
      return 1;
   }
	
    private void enableProxyWidget(Boolean enabled) {
        Log.i(TAG, " -- enableProxyWidget  enabled = " + enabled);
        if (enabled) {
            proxyIPText.setAlpha(1f);
            proxyPortText.setAlpha(1f);
            proxySkipText.setAlpha(1f);
        } else {
            proxyIPText.setAlpha(0.3f);
            proxyPortText.setAlpha(0.3f);
            proxySkipText.setAlpha(0.3f);
        }
        // proxyIPText.setEnabled(enabled);
        // proxyIPText.setFocusable(enabled);
        
        // proxyPortText.setEnabled(enabled);
        // proxyPortText.setFocusable(enabled);
        
        // proxySkipText.setEnabled(enabled);
        // proxySkipText.setFocusable(enabled);
        
        proxyaddr.setEnabled(enabled);
        proxyaddr.setFocusable(enabled);
        
        proxyport.setEnabled(enabled);
        proxyport.setFocusable(enabled);
        
        httpSkipSpinner.setEnabled(enabled);
        httpSkipSpinner.setFocusable(enabled);
    }
   
    private void setWidgetAlpha(float f) {
        proxyaddr.setAlpha(f);
        proxyport.setAlpha(f);
        httpSkipSpinner.setAlpha(f);
    }
   
   public void clearProxy(){
	   proxyaddr.setText("");
	   proxyport.setText("");
	   httpskip_index = 0;
	   httpSkipSpinner.setText(mHttpSkipArrays[httpskip_index]);
   }
   
   public boolean saveProxy()
   {
	   int port = 0;
	   String hostname= proxyaddr.getText().toString().trim();
	   String portstr = proxyport.getText().toString().trim();
	   String exclList;
	   if(httpskip_index == 1){
		   exclList = hostname;
	   }else{
		   exclList = "";
	   }
	   int ret = validate(hostname,portstr,exclList);
	   if(ret != 0){
		   showErrorDialog(ret);
		   return false;
	   }

       if (portstr.length() > 0) {
    	   port = Integer.parseInt(portstr);
       }
       
        Log.i(TAG, " saveProxy hostname = " + hostname + " port = " + port
                + " exclList = " + exclList + " http_index = " + http_index);
	   saveProxy(hostname,portstr,httpskip_index);
	   ProxyProperties p;
	   if(http_index == 0){
		    p= new ProxyProperties("", 0,null);
	   }else{
		    p = new ProxyProperties(hostname, port,exclList);
	   }
       ConnectivityManager cm =
           (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

       cm.setGlobalProxy(p);
       return true;
   }

   /**
    * validate syntax of hostname and port entries
    * @return 0 on success, string resource ID on failure
    */
   public static int validate(String hostname, String port, String exclList) {
//       Matcher match = HOSTNAME_PATTERN.matcher(hostname);
//       String exclListArray[] = exclList.split(",");
//
//       if (!match.matches()) return R.string.proxy_error_invalid_host;
//
//       for (String excl : exclListArray) {
//           Matcher m = EXCLUSION_PATTERN.matcher(excl);
//           if (!m.matches()){
//        	   return R.string.proxy_error_invalid_host;
//           }
//       }
       Log.d(TAG, "---- hostname = " + hostname + " port = " + port + " exclList = " + exclList);
       if (hostname.length() > 0 && port.length() == 0) {
           return R.string.proxy_error_empty_port;
       }

       if (port.length() > 0) {
           if (hostname.length() == 0) {
               return R.string.proxy_error_empty_host_set_port;
           }
           int portVal = -1;
           try {
               portVal = Integer.parseInt(port);
           } catch (NumberFormatException ex) {
               return R.string.proxy_error_invalid_port;
           }
           if (portVal <= 0 || portVal > 0xFFFF) {
               return R.string.proxy_error_invalid_port;
           }
       }
       return 0;
   }
   
    private Dialog ErrorDialog;
	private void showErrorDialog(int id){
		TextView msg;
		Button confirm;
		ErrorDialog = new Dialog(mView.getContext(),R.style.DialogStyle);
		ErrorDialog.setContentView(R.layout.net_manual_error_dialog);
		msg = (TextView) ErrorDialog.findViewById(R.id.textview_dialog);
		confirm = (Button) ErrorDialog.findViewById(R.id.btn_dlg_confirm);
		msg.setText(id);
		confirm.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
		
				ErrorDialog.dismiss();
				showExitDialog();
			}
       	
       });
		ErrorDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
		
				showExitDialog();
			}

			
		});
		
		ErrorDialog.show();
	}
	private Dialog ExitDialog;
	private void showExitDialog(){
		TextView msg;
		final Button btnConfirm;
		Button btnCancel;
		ExitDialog = new Dialog(mView.getContext(),R.style.DialogStyle);
		ExitDialog.setContentView(R.layout.resolution_dialog);
		msg = (TextView) ExitDialog.findViewById(R.id.textview_dialog);
        btnConfirm = (Button) ExitDialog.findViewById(R.id.btn_dlg_confirm);        
        btnCancel = (Button) ExitDialog.findViewById(R.id.btn_dlg_cancel);
        msg.setText(R.string.proxy_exit_msg);
    	btnConfirm.setText(R.string.str_continue);
        btnCancel.setText(R.string.str_exit);
        btnConfirm.requestFocus();
        btnConfirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
		
				ExitDialog.dismiss();
			}
        	
        });
        btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
		
				ExitDialog.dismiss();
				getActivity().getFragmentManager().popBackStack();
			}
        	
        });
        ExitDialog.show();
	}
	
	public void saveProxy(String address, String port, int skip){
		settingSharedPreferences(STR_HTTP_PROXY_ADDRESS,address);
		settingSharedPreferences(STR_HTTP_PROXY_PORT,port);
		settingSharedPreferences(STR_HTTP_PROXY_SKIP,skip);
	}
	

	private void settingSharedPreferences(String name, int value) {
		editor.putInt(name, value);
		editor.commit();
	}
	
	private void settingSharedPreferences(String name, String value) {
		editor.putString(name, value);
		editor.commit();
	}
	
}
