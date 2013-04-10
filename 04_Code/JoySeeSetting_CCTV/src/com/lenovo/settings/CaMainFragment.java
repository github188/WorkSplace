package com.lenovo.settings;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.joysee.adtv.aidl.ca.ICaSettingService;

public class CaMainFragment extends Fragment implements View.OnClickListener {
    private boolean DEBUG = true;
    private String TAG = "CaMainFragment";
    public static final String GET_CASERVICE_ACTION = "com.joysee.adtv.service.CaSettingService";
    private SettingFragment settingFragment;
    /**通过AIDL获得的ICaSettingService代理*/
    private ICaSettingService mICaSettingService;
    private ServiceConnection mConnection ;
    /**主View*/
    private View mMainView;
    /** setting 主界面工作时段按钮 */
    private TextView mWorkTime;
    /** setting 主界面授权信息按钮 */
    private TextView mAuthorMessage;
    /** setting 主界面修改密码按钮 */
    private TextView mModifyPassword;
    /** setting watch level button */
    private TextView mWatchLevel;
    /** setting 主界面本机卡号 */
    private TextView mCardNumber;
    /**卡号*/
    private String mCardNumberStr ;
    /** 记录当前页面 0：工作时段 1：观看级别 2：授权信息 3：修改密码 */
    private static int mWitchPage = 0;
    /**
     * UI主线程的Handler
     */
    private Handler mainHandler = new Handler() {
        public void dispatchMessage(Message msg) {
            switch (msg.what) {
            }
        }
    };

    private void Logcat(String msg) {
        if (DEBUG) {
            Log.d(TAG, "---------" + msg);
        }
    }

    public static final class OperateMsg {
        /** 操作成功 */
        public static final int CDCA_RC_OK = 0;
        /** 未知错误 */
        public static final int CDCA_RC_UNKNOWN = 1;
        /** 指针为空 */
        public static final int CDCA_RC_POINTER_INVALID = 2;
        /** 智能卡不在机顶盒内或者是无效卡 */
        public static final int CDCA_RC_CARD_INVALID = 3;
        /** 输入pin 码无效 不在0x00~0x09之间 */
        public static final int CDCA_RC_PIN_INVALID = 4;
    }
    @Override
    public void onClick(View v) {
        
        switch (v.getId()) {
        case R.id.config_work_time:
            CaWorkTimeFragment caWorkTimeFragment = new CaWorkTimeFragment();
            settingFragment.setFragment(caWorkTimeFragment, true);
            mWitchPage = 0;
            break;
        case R.id.config_watch_level:
            CaWatchLevelFragment caWatchLevelFragment = new CaWatchLevelFragment();
            settingFragment.setFragment(caWatchLevelFragment, true);
            mWitchPage = 1;
            break;
        case R.id.config_authorise_message:
            CaAuthoriseInfoFragment caAuthoriseInfoFragment = new CaAuthoriseInfoFragment();
            settingFragment.setFragment(caAuthoriseInfoFragment, true);
            mWitchPage = 2;
            break;
        case R.id.config_modify_password:
            CaModifyPinFragment caModifyPinFragment = new CaModifyPinFragment();
            settingFragment.setFragment(caModifyPinFragment, true);
            mWitchPage = 3;
            break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Logcat("onCreate");
        mWitchPage = 0;
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Logcat("onCreateView");
        settingFragment = new SettingFragment(getActivity());
        mMainView = inflater.inflate(R.layout.ca_setting_menu_layout, container, false);
        mWorkTime = (TextView) mMainView.findViewById(R.id.config_work_time);
        mAuthorMessage = (TextView) mMainView.findViewById(R.id.config_authorise_message);
        mModifyPassword = (TextView) mMainView.findViewById(R.id.config_modify_password);
        mWatchLevel = (TextView) mMainView.findViewById(R.id.config_watch_level);
        mCardNumber = (TextView) mMainView.findViewById(R.id.config_card_number);

        mWorkTime.setOnClickListener(this);
        mAuthorMessage.setOnClickListener(this);
        mModifyPassword.setOnClickListener(this);
        mWatchLevel.setOnClickListener(this);

        LenovoSettingsActivity.setTitleFocus(false);
        Logcat(" getConnectOfService() = " + getConnectOfService());
        return mMainView;
    }
    /**bindService
     * return false if bind false
     * */
    public boolean getConnectOfService() {
        if (null == mICaSettingService) {
            mConnection = new ServiceConnection() {
                public void onServiceConnected(ComponentName className,
                        IBinder service) {
                    mICaSettingService = ICaSettingService.Stub
                            .asInterface(service);
                    if (mICaSettingService == null) {
                        Log.e(TAG,
                                " onServiceConnected mICaSettingService is null,Error!!");
                    }else{
                        try {
                            mCardNumberStr = mICaSettingService.getCardSN();
                            if(mCardNumberStr != null){
                                mCardNumber.setText(mCardNumberStr);
                            }
                        } catch (RemoteException e) {
                            
                            e.printStackTrace();
                        }
                    }
                }
                public void onServiceDisconnected(ComponentName className) {
                    mICaSettingService = null;
                }
            };
        }
        Intent intent = new Intent(GET_CASERVICE_ACTION);
        return this.getActivity().bindService(intent, mConnection,
                Context.BIND_AUTO_CREATE);
    }
    @Override
    public void onStart() {
        Logcat("onStart");
        setFocusable(true, mWitchPage);
        super.onStart();
    }
    @Override
    public void onResume() {
        if(mCardNumberStr != null){
            mCardNumber.setText(mCardNumberStr);
        }
        super.onResume();
    }
    @Override
    public void onPause() {
        Logcat("onPause");
        if (mConnection != null) {
            Logcat(" begin unbindService ");
            long begin = System.currentTimeMillis();
            this.getActivity().unbindService(mConnection);
            long end = System.currentTimeMillis();
            Logcat(" end unbindService use time = " + (end - begin));
        }
        super.onPause();
    }
    public void setFocusable(boolean isFocus,int whitch){
        if (isFocus) {
            mWorkTime.setFocusable(true);
            mAuthorMessage.setFocusable(true);
            mModifyPassword.setFocusable(true);
            mWatchLevel.setFocusable(true);
            if (whitch == 0) {
                mWorkTime.requestFocus();
            } else if (whitch == 1) {
                mWatchLevel.requestFocus();
            } else if (whitch == 2) {
                mAuthorMessage.requestFocus();
            } else if (whitch == 3) {
                mModifyPassword.requestFocus();
            }
        }else{
            mWorkTime.setFocusable(false);
            mAuthorMessage.setFocusable(false);
            mModifyPassword.setFocusable(false);
            mWatchLevel.setFocusable(false);
        }
    }
}
