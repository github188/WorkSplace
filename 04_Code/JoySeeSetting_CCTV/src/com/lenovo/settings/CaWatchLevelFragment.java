package com.lenovo.settings;

import java.util.ArrayList;
import java.util.List;

import com.joysee.adtv.aidl.ca.ICaSettingService;
import com.lenovo.settings.CaMainFragment.OperateMsg;
import com.lenovo.settings.Util.AdapterViewSelectionUtil;

import android.app.Activity;
import android.app.Dialog;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class CaWatchLevelFragment extends Fragment implements OnClickListener{
    private boolean DEBUG = true;
    private String TAG = "CaWorkTimeFragment";
    /**通过AIDL获得的ICaSettingService代理*/
    private ICaSettingService mICaSettingService;
    private ServiceConnection mConnection ;
    /**主View*/
    private View mMainView;
    private Activity mContext;
    //观看级别布局控件
    private Button mWatchSave;
    private Button mWatchCancel;
    private Spinner mWatchSpinner;
    private Dialog mDialog;
    /**密码*/
    private String mPassWordStr;
    /**级别*/
    private int mLevel ;
    private static final int MSG_GET_LEVEL = 0;
    private static final int MSG_SAVE_LEVEL = 1;
    private void Logcat(String msg) {
        if (DEBUG) {
            Log.d(TAG, "---------" + msg);
        }
    }

    /**
     * UI主线程的Handler
     */
    private Handler mainHandler = new Handler() {
        public void dispatchMessage(Message msg) {
            switch (msg.what) {
            case MSG_GET_LEVEL:
                try {
                    mLevel = mICaSettingService.getWatchLevel();
                    Logcat(" getWatchLevel level = " + mLevel);
                    if (mLevel >= 4 && mLevel <= 18) {
                        mWatchSpinner.setSelection(mLevel-4);
                    }
                } catch (Exception e) {
                    
                    e.printStackTrace();
                }
                break;

            case MSG_SAVE_LEVEL:
                String level = (String) msg.obj;
                if(level != null){
                    mLevel = Integer.valueOf(level);
                }
                if (mPassWordStr != null) {
                    try {
                        int flag = mICaSettingService.setWatchLevel(mPassWordStr,
                                mLevel);
                        Logcat("--------- setWatchLevel back = " + flag);
                        showOperateMsg(flag);
                    } catch (Exception e) {
                        
                        e.printStackTrace();
                    }
                    mPassWordStr = null;
                }
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
        mMainView = inflater.inflate(R.layout.config_watch_level_layout,
                container, false);
        initView();
        return mMainView;
    }
    private void initView(){
        mWatchSpinner = (Spinner) mMainView.findViewById(R.id.config_watch_level_spinner);
        
        mWatchSave = (Button) mMainView.findViewById(R.id.config_watch_level_save);
        mWatchCancel = (Button) mMainView.findViewById(R.id.config_watch_level_cancel);
        
        mWatchSave.setOnClickListener(this);
        mWatchCancel.setOnClickListener(this);
        
        setSpinnerValue(getListTime(4,18), mWatchSpinner);
        
        mWatchSpinner.requestFocus();
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
                        mainHandler.sendEmptyMessage(MSG_GET_LEVEL);
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
    public void onStop() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
        super.onStop();
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
    /**
     * 弹出操作提示框
     * @param witch
     */
    public void showOperateMsg(int witch) {
        switch (witch) {
        case OperateMsg.CDCA_RC_OK:
            AdapterViewSelectionUtil.showToast(this.getActivity(),
                    R.string.config_watch_level_set_success);
            getActivity().getFragmentManager().popBackStack();
            break;
        case OperateMsg.CDCA_RC_CARD_INVALID:
            AdapterViewSelectionUtil.showToast(this.getActivity(),
                    R.string.config_midify_card_not_finded);
            break;
        case OperateMsg.CDCA_RC_PIN_INVALID:
            AdapterViewSelectionUtil.showToast(this.getActivity(),
                    R.string.config_work_time_input_passward_wrong);
            break;
        default:
            AdapterViewSelectionUtil.showToast(this.getActivity(),
                    R.string.ca_input_password_error);
            break;
        }
    }

    @Override
    public void onClick(View v) {
        
        switch (v.getId()) {
        case R.id.config_watch_level_save:
            watchLevelSave();
            break;
        case R.id.config_watch_level_cancel:
            getActivity().getFragmentManager().popBackStack();
            break;
        }
    }

    private void watchLevelSave() {
        final String level = mWatchSpinner.getSelectedItem().toString();
        if (mDialog == null) {
            mDialog = new Dialog(mContext, R.style.config_text_dialog);
            View vi = LayoutInflater.from(mContext).inflate(
                    R.layout.config_edit_dialog, null);
            final EditText edit = (EditText) vi
                    .findViewById(R.id.config_dialog_eidt);
            Button ok = (Button) vi
                    .findViewById(R.id.config_dialog_btn_confirm);
            Button cancel = (Button) vi
                    .findViewById(R.id.config_dialog_btn_cancel);
            int width = (int) getResources().getDimension(R.dimen.ca_worktime_pwd_dialog_width);
            int height = (int) getResources().getDimension(R.dimen.ca_worktime_pwd_dialog_height);
            mDialog.setContentView(vi,new LinearLayout.LayoutParams(width,height));
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                    mDialog = null;
                }
            });
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPassWordStr = edit.getText().toString();
                    if (mPassWordStr != null && mPassWordStr.length() == 6) {
                        Message m = new Message();
                        m.what = MSG_SAVE_LEVEL;
                        m.obj = level;
                        mainHandler.sendMessage(m);
                        mDialog.dismiss();
                        mDialog = null;
                    } else {
                        AdapterViewSelectionUtil.showToast(mContext,
                                R.string.config_midify_password_input_wrong);
                    }
                }
            });
        }
        mDialog.show();
    }
    /**
     * 给spinner 赋值
     * @param list
     * @param sp
     */
    private void setSpinnerValue(final List<String> list, Spinner sp){
        ArrayAdapter adapter = new ArrayAdapter(mContext, R.layout.search_spinner_button, list);
        adapter.setDropDownViewResource(R.layout.search_spinner_item);
        sp.setAdapter(adapter);
    }

    /**
     * 获取一个范围数组
     * @param min
     * @param max
     * @return
     */
    private List<String> getListTime(int min,int max){
        List<String> list = new ArrayList<String>();
        for(int i=min;i<=max;i++){
            list.add(i+"");
        }
        return list;
    }
}
