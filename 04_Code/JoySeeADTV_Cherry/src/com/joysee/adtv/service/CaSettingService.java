package com.joysee.adtv.service;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.joysee.adtv.R;
import com.joysee.adtv.aidl.ca.ICaSettingService;
import com.joysee.adtv.logic.CaManager;
import com.joysee.adtv.logic.DVBPlayManager;
import com.joysee.adtv.logic.bean.LicenseInfo;
import com.joysee.adtv.logic.bean.WatchTime;

public class CaSettingService extends Service {
    public static final String TAG = "CaSettingService";
    private DVBPlayManager mDvbPlayManager;
    private CaManager mCaManager = CaManager.getCaManager();
    public void Log(String str){
        Log.d(TAG, str);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log(" onBind " + intent.toString());
        if (mIBinder == null) {
            Log.e(TAG, " onBind mIBinder is null!!!!!!!!!!!");
        }else{
            mDvbPlayManager = DVBPlayManager.getInstance(getApplicationContext());
            mDvbPlayManager.init();
        }
        return mIBinder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        mDvbPlayManager.uninit();
        return super.onUnbind(intent);
    }
    private final ICaSettingService.Stub mIBinder = new ICaSettingService.Stub() {
        
        @Override
        public int setWatchTime(String pwd, int iStarthour, int iStartMin,
                int iEndHour, int iEndMin) throws RemoteException {
            // TODO Auto-generated method stub
            WatchTime watchTime = new WatchTime();
            watchTime.startHour = iStarthour;
            watchTime.startMin = iStartMin;
            watchTime.startSec = 0;
            watchTime.endHour = iEndHour;
            watchTime.endMin = iEndMin;
            watchTime.endSec = 0;
            int success = mCaManager.nativeSetWatchTime(pwd, watchTime);
            Log(" setWatchTime success = " + success);
            return success;
        }
        
        @Override
        public int setWatchLevel(String pin, int level) throws RemoteException {
            // TODO Auto-generated method stub
            int success = mCaManager.nativeSetWatchLevel(pin, level);
            Log(" setWatchLevel success = " + success);
            return success;
        }
        
        @Override
        public int[] getWatchTime() throws RemoteException {
            // TODO Auto-generated method stub
            Log( "----begin getWatchTime ----");
            WatchTime watchTime = new WatchTime();
            int success = mCaManager.nativeGetWatchTime(watchTime);
            Log.d(TAG, " getWatchTime success = " + success);
            int[] intArray = {
                    success, watchTime.startHour,
                    watchTime.startMin, watchTime.endHour, watchTime.endMin
            };
            return intArray;
        }
        
        @Override
        public int getWatchLevel() throws RemoteException {
            // TODO Auto-generated method stub
            int success = mCaManager.nativeGetWatchLevel();
            return success;
        }
        
        @Override
        public List getOperatorID() throws RemoteException {
            // TODO Auto-generated method stub
            Vector<Integer> vec = new Vector<Integer>();
            int sucess = mCaManager.nativeGetOperatorID(vec);
            Log(" getOperatorID = " + " sucess = " + sucess);
            int size = vec.size();
            Log(" getOperatorID size = " + size);
            List list = vec.subList(0, size);
            return list;
        }
        
        @Override
        public String getCardSN() throws RemoteException {
            // TODO Auto-generated method stub
            return mCaManager.nativeGetCardSN();
        }
        
        @Override
        public ArrayList<Map<String, String>> getAuthorization(int operID)
                throws RemoteException {
            // TODO Auto-generated method stub
            Vector<LicenseInfo> tvec = new Vector<LicenseInfo>();
            int sucess = mCaManager.nativeGetAuthorization(operID, tvec);
            Log(" getAuthorization operID = " + operID + " sucess = " + sucess);
            ArrayList<Map<String, String>> mList = new ArrayList<Map<String, String>>();
            for (int j = 0; j < tvec.size(); j++) {
                Map<String, String> map = new HashMap<String, String>();
                LicenseInfo t = tvec.get(j);
                map.put("number", t.product_id + "");
                map.put("time", getDateString(t.expired_time));
                if (t.is_record) {
                    map.put("record", getResources().getString(R.string.config_auth_yes_record));
                } else {
                    map.put("record", getResources().getString(R.string.config_auth_no_record));
                }
                mList.add(map);
            }
            return mList;
        }
        
        @Override
        public int changePincode(String oldPwd, String newPwd)
                throws RemoteException {
            // TODO Auto-generated method stub
            int sucess = mCaManager.nativeChangePinCode(oldPwd, newPwd);
            Log(" changePincode oldPwd = " + oldPwd + " newPwd = " + newPwd
                    + " sucess = " + sucess);
            return sucess;
        }
    };

    private String getDateString(int day) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy"
                + getString(R.string.ca_charge_interval_year) + "MM"
                + getString(R.string.ca_charge_interval_month) + "dd"
                + getString(R.string.ca_charge_interval_day));
        cal.set(2000, 0, 1);// 从1月1号开始
        cal.add(Calendar.DATE, day);
        Date d = new Date();
        d = cal.getTime();
        String date = format.format(d);
        return date;
    }
}
