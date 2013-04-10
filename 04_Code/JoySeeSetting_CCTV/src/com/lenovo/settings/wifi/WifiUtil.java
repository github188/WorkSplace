package com.lenovo.settings.wifi;    
   
import java.util.List;    
import java.util.Map;    

import com.lenovo.settings.R;
import com.lenovo.settings.R.attr;

import android.app.AlertDialog;    
import android.content.Context;    
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.util.Log;    
import android.view.LayoutInflater;    
import android.view.View;    
import android.view.ViewGroup;   
import android.widget.BaseAdapter;      
import android.widget.ImageView;    
import android.widget.TextView;
   
public class WifiUtil  {    
     public static final int[] STATE_SECURED = {
        R.attr.state_encrypted
    };
     public static final int[] STATE_NONE = {};
    
    /** These values are matched in string arrays -- changes must be kept in sync */
    static final int SECURITY_NONE = 0;
    static final int SECURITY_WEP = 1;
    static final int SECURITY_PSK = 2;
    static final int SECURITY_EAP = 3;

    enum PskType {
        UNKNOWN,
        WPA,
        WPA2,
        WPA_WPA2
    }   
        

    
    public static  int getLevel(int mRssi ) {
        if (mRssi == Integer.MAX_VALUE) {
            return 5;
        }
        return WifiManager.calculateSignalLevel(mRssi, 5);
    }
    
    
    public static int getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_PSK)) {
            return SECURITY_PSK;
        }
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_EAP) ||
                config.allowedKeyManagement.get(KeyMgmt.IEEE8021X)) {
            return SECURITY_EAP;
        }
        return (config.wepKeys[0] != null) ? SECURITY_WEP : SECURITY_NONE;
    }
    
    public static  int getSecurity(ScanResult result) {
        if (result.capabilities.contains("WEP")) {
            return SECURITY_WEP;
        } else if (result.capabilities.contains("PSK")) {
            return SECURITY_PSK;
        } else if (result.capabilities.contains("EAP")) {
            return SECURITY_EAP;
        }
        return SECURITY_NONE;
    }
    
    public static  WifiConfiguration  generateOpenNetworkConfig(ScanResult result) {
    	 WifiConfiguration mConfig =  new WifiConfiguration();
        if (getSecurity(result) != SECURITY_NONE)
            throw new IllegalStateException();
        mConfig.SSID = convertToQuotedString(result.SSID);
        mConfig.allowedKeyManagement.set(KeyMgmt.NONE);
        
        return mConfig;
    }
    
    
    static String convertToQuotedString(String string) {
        return "\"" + string + "\"";
    } 
    

    public static WifiConfiguration CreateWifiInfo(String SSID, String Password, int Type)  
    {  
       WifiConfiguration config = new WifiConfiguration();    
        config.allowedAuthAlgorithms.clear();  
        config.allowedGroupCiphers.clear();  
        config.allowedKeyManagement.clear();  
        config.allowedPairwiseCiphers.clear();  
        config.allowedProtocols.clear();  
       config.SSID = "\"" + SSID + "\"";    
       if(Type == SECURITY_NONE)  
       {  
            config.wepKeys[0] = "";  
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);  
            config.wepTxKeyIndex = 0;  
       }  
       if(Type == SECURITY_WEP)  
       {  
           config.preSharedKey = "\""+Password+"\"";   
           config.hiddenSSID = true;    
           config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);  
           config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);  
           config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);  
           config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);  
           config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);  
           config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);  
           config.wepTxKeyIndex = 0;  
       }  
       if(Type == SECURITY_PSK)  
       {  
       config.preSharedKey = "\""+Password+"\"";  
       config.hiddenSSID = true;    
       config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);    
       config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);                          
       config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);                          
       config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);                     
       config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);                       
       config.status = WifiConfiguration.Status.ENABLED;    
       }  
       else  
       {  
           return null;  
       }  
       return config;  
    }  

}   
