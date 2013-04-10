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

public class CaModifyPinFragment extends Fragment implements View.OnClickListener{
    private boolean DEBUG = true;
    private String TAG = "CaModifyPinFragment";
    /**通过AIDL获得的ICaSettingService代理*/
    private ICaSettingService mICaSettingService;
    private ServiceConnection mConnection ;
    /**主View*/
    private View mMainView;
    private Activity mContext;
    /** 旧密码的 edit */
    private EditText mEditOld;
    /** 新密码 edit */
    private EditText mEditNew;
    /** 新密码确认 edit */
    private EditText mEditNewC;
    /** 修改 保存 按钮 */
    private Button mModifySave;
    /** 修改 取消 按钮 */
    private Button mModifyCancel;
    private void Logcat(String msg) {
        if (DEBUG) {
            Log.d(TAG, "---------" + msg);
        }
    }
    private static final int MSG_MODIFY = 0;
    private static final int MSG_SAVE = 1;
    /**
     * UI主线程的Handler
     */
    private Handler mainHandler = new Handler() {
        public void dispatchMessage(Message msg) {
            switch (msg.what) {
            case MSG_MODIFY:
                Bundle bundle = msg.getData();
                if (bundle == null) {
                    return;
                }
                String oldPwd = bundle.getString("old");
                String newPwd = bundle.getString("new");
                try {
                    int flag = mICaSettingService.changePincode(oldPwd, newPwd);
                    Logcat(" changePincode back is = " + flag);
                    showOperateMsg(flag);
                } catch (Exception e) {
                    
                    e.printStackTrace();
                    Logcat(" changePincode error !!!");
                }
                break;
            case MSG_SAVE:
                passwordSave();
                break;
            }
        };
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Logcat("onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Logcat("onCreateView");
        Logcat(" getConnectOfService() = " + getConnectOfService());
        mContext = this.getActivity();
        mMainView = inflater.inflate(R.layout.config_modify_password_layout, container, false);
        initView();
        return mMainView;
    }
    /**
     *修改密码布局初始化
     */
    private void initView(){
        mEditOld = (EditText) mMainView.findViewById(R.id.config_modify_password_edit_old);
        mEditNew = (EditText) mMainView.findViewById(R.id.config_modify_password_edit_new);
        mEditNewC = (EditText) mMainView.findViewById(R.id.config_modify_password_edit_new_confirm);
        mModifySave = (Button) mMainView.findViewById(R.id.config_modify_password_save);
        mModifyCancel = (Button) mMainView.findViewById(R.id.config_modify_password_cancel);

        mModifySave.setOnClickListener(this);
        mModifyCancel.setOnClickListener(this);
        mEditOld.requestFocus();
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
                    }
                }
                public void onServiceDisconnected(ComponentName className) {
                    mICaSettingService = null;
                }
            };
        }
        Intent intent = new Intent(CaMainFragment.GET_CASERVICE_ACTION);
        return this.getActivity().bindService(intent, mConnection,
                Context.BIND_AUTO_CREATE);
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

    @Override
    public void onClick(View v) {
        
        switch (v.getId()) {
        case R.id.config_modify_password_save:
            mainHandler.removeMessages(MSG_SAVE);
            Message msg = new Message();
            msg.what = MSG_SAVE;
            mainHandler.sendMessageDelayed(msg, 300);
            break;
        case R.id.config_modify_password_cancel:
            this.getFragmentManager().popBackStack();
            break;
        }
    }
    /**
     * 修改密码保存
     */
    private void passwordSave() {
        String oPasswd = mEditOld.getText().toString();
        String nPasswd = mEditNew.getText().toString();
        String nPasswdC = mEditNewC.getText().toString();
        if(nPasswd.equals(nPasswdC)){
            if(oPasswd.length() == nPasswd.length()){
                Message m = new Message();
                m.what = MSG_MODIFY;
                Bundle bun = new Bundle();
                bun.putString("old", oPasswd);
                bun.putString("new", nPasswdC);
                m.setData(bun);
                mainHandler.sendMessage(m);
            }else{
                AdapterViewSelectionUtil.showToast(mContext, R.string.config_midify_password_input_wrong);
            }
            
        }else{
            mEditNew.setText("");
            mEditNewC.setText("");
            AdapterViewSelectionUtil.showToast(mContext, R.string.config_midify_password_input_compare);
        }
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
}
