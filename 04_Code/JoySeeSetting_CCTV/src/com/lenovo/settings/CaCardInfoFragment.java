package com.lenovo.settings;

import com.joysee.adtv.aidl.ca.ICaSettingService;
import com.lenovo.settings.CaMainFragment.OperateMsg;
import com.lenovo.settings.Util.AdapterViewSelectionUtil;

import android.app.Activity;
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
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CaCardInfoFragment extends Fragment implements View.OnClickListener{
    private boolean DEBUG = true;
    private String TAG = "CaModifyPinFragment";
    /**通过AIDL获得的ICaSettingService代理*/
    private ICaSettingService mICaSettingService;
    private ServiceConnection mConnection ;
    /**主View*/
    private View mMainView;
    private Activity mContext;
  //卡信息
    /** 本机卡号 */
    private TextView mCardNumberTv;
    /** STBID */
    private TextView mSTDIDTv;
    /** bootloader */
    private TextView mBootloaderTv;
    /** update time */
    private TextView mUpdateTimeTv;
    /** update state */
    private TextView mUpdateStateTv;
    /** Ca version */
    private TextView mCaVersionTv;
    public static String CA_UPDATE_STATE_FALSE = "false";
    public static String CA_UPDATE_STATE_SUCESS = "sucess";
    private String mCardNumberStr = "";
    private String mSTDIDNumberStr = "";
    /**
     * str[0] time
     * str[1] progress
     */
    String [] str;
    
    private void Logcat(String msg) {
        if (DEBUG) {
            Log.d(TAG, "---------" + msg);
        }
    }
    private static final int MSG_GETINFO = 0;
    /**
     * UI主线程的Handler
     */
    private Handler mainHandler = new Handler() {
        public void dispatchMessage(Message msg) {
            switch (msg.what) {
            case MSG_GETINFO:
                try {
                    str = new String[2];
                    //str = mICaSettingService.getCaUpdateInfo();
                    String time = str[0];
                    int temp = Integer.parseInt(str[1]);
                    String state = "";
                    if (time.equals("")) {
                        time = getResources().getString(R.string.ca_noupdate);
                    }
                    if (temp == 0) {
                        state = getResources().getString(R.string.ca_noupdate);
                    } else if (temp < 100 && temp > 0) {
                        state = getResources().getString(R.string.ca_update_false);
                    } else {
                        state = getResources().getString(R.string.ca_update_success);
                    }
                    String version = "";
                    //version = mICaSettingService.getCAVersion();
                    //mCardNumberStr = mICaSettingService.getCardSN();
                    //mSTDIDNumberStr = mICaSettingService.getSTBId();
                    mCardNumberTv.setText(mCardNumberStr);
                    mSTDIDTv.setText(mSTDIDNumberStr);
                    mUpdateStateTv.setText(getResources().getString(R.string.config_card_update_state, state));
                    mUpdateTimeTv.setText(getResources().getString(R.string.config_card_update_time, time));
                    if (version != null) {
                        mCaVersionTv.setText(getResources().getString(
                                R.string.config_card_ca_version_text, version));
                    }
                } catch (Exception e) {
                    
                    e.printStackTrace();
                    Logcat(" changePincode error !!!");
                }
                break;
            }
        };
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        Logcat("onCreate");
        Logcat(" getConnectOfService() = " + getConnectOfService());
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Logcat("onCreateView");
        mContext = this.getActivity();
        mMainView = inflater.inflate(R.layout.config_cardinfo_layout, container, false);
        initView();
        return mMainView;
    }
    /**
     *修改密码布局初始化
     */
    private void initView(){
        mCardNumberTv = (TextView) mMainView.findViewById(R.id.config_card_number_tv);
        mSTDIDTv = (TextView) mMainView.findViewById(R.id.config_stbid_number_tv);
        mBootloaderTv = (TextView) mMainView.findViewById(R.id.config_cardloader_tv);
        mUpdateStateTv = (TextView) mMainView.findViewById(R.id.config_update_state_tv);
        mUpdateTimeTv = (TextView) mMainView.findViewById(R.id.config_updatetime_tv);
        mCaVersionTv = (TextView) mMainView.findViewById(R.id.config_update_ca_version_tv);

        mUpdateStateTv.setText(getResources().getString(R.string.config_card_update_state, ""));
        mUpdateTimeTv.setText(getResources().getString(R.string.config_card_update_time, ""));
        mCaVersionTv.setText(getResources().getString(R.string.config_card_ca_version_text, ""));
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
                        mainHandler.sendEmptyMessage(MSG_GETINFO);
                    }
                }
                public void onServiceDisconnected(ComponentName className) {
                    mICaSettingService = null;
                }
            };
            Intent intent = new Intent(CaMainFragment.GET_CASERVICE_ACTION);
            return this.getActivity().bindService(intent, mConnection,
                    Context.BIND_AUTO_CREATE);
        }
        return false;
    }
    @Override
    public void onResume() {
        
        super.onResume();
    }
    @Override
    public void onDestroy() {
        
        Logcat("onDestroy");
        if (mConnection != null) {
            this.getActivity().unbindService(mConnection);
        }
        super.onDestroy();
    }

    /**
     * 弹出操作提示框
     * @param witch
     */
    public void showOperateMsg(int witch) {
        switch (witch) {
        case OperateMsg.CDCA_RC_OK:
            AdapterViewSelectionUtil.showToast(this.getActivity(),
                    R.string.config_midify_password_success);
            getActivity().getFragmentManager().popBackStack();
            break;
        case OperateMsg.CDCA_RC_CARD_INVALID:
            AdapterViewSelectionUtil.showToast(this.getActivity(),
                    R.string.config_midify_card_not_finded);
            break;
        case OperateMsg.CDCA_RC_PIN_INVALID:
            AdapterViewSelectionUtil.showToast(this.getActivity(),
                    R.string.config_midify_old_wrong);
            break;
        case OperateMsg.CDCA_RC_POINTER_INVALID:
            AdapterViewSelectionUtil.showToast(this.getActivity(),
                    R.string.ca_modify_password_error);
            break;
        case OperateMsg.CDCA_RC_UNKNOWN:
        default:
            AdapterViewSelectionUtil.showToast(this.getActivity(),
                    R.string.ca_modify_password_error);
            break;
        }
    }

    @Override
    public void onClick(View v) {
        
        
    }
}
