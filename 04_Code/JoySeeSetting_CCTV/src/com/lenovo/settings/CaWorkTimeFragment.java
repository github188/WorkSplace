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
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class CaWorkTimeFragment extends Fragment implements OnClickListener{
    private boolean DEBUG = true;
    private String TAG = "CaWorkTimeFragment";
    /**通过AIDL获得的ICaSettingService代理*/
    private ICaSettingService mICaSettingService;
    private ServiceConnection mConnection ;
    /**主View*/
    private View mMainView;
//    /** setting 工作时段开始工作时间小时edit */
//    private EditText mEditSH;
//    /** setting 工作时段开始工作时间分钟edit */
//    private EditText mEditSM;
//    /** setting 工作时段结束工作时间小时edit */
//    private EditText mEditEH;
//    /** setting 工作时段结束工作时间分钟edit */
//    private EditText mEditEM;
    /** setting 工作时段保存按钮 */
    private Button mBtnSave;
    private Button mBtnCancel;
    //新布局 spinner 列表
    private Spinner mSpinnerSH;
    private Spinner mSpinnerSM;
    private Spinner mSpinnerEH;
    private Spinner mSpinnerEM;
    /***/
    private Dialog mDialog;
    private String mPassWordStr;
    private Activity mContext;
    private void Logcat(String msg) {
        if (DEBUG) {
            Log.d(TAG, "---------" + msg);
        }
    }
    /**存储工作时段*/
    private int[] mWorkTimeArray = new int[5];
    private static final int MSG_GET_WORKTIME = 0;
    private static final int MSG_SAVE_WORKTIME = 1;
    /**
     * UI主线程的Handler
     */
    private Handler mainHandler = new Handler() {
        public void dispatchMessage(Message msg) {
            switch (msg.what) {
            case MSG_GET_WORKTIME:
                try {
                    mWorkTimeArray = mICaSettingService.getWatchTime();
                    for (int i = 0; i < mWorkTimeArray.length; i++) {
                        Logcat("mWorkTimeArray[" + i + "] = "
                                + mWorkTimeArray[i]);
                    }
                    //获取工作时段成功
                    if (mWorkTimeArray[0] >= 0) {
                        setDataToView(mWorkTimeArray[1], mWorkTimeArray[2],
                                mWorkTimeArray[3], mWorkTimeArray[4]);
                    }
                } catch (Exception e) {
                    
                    e.printStackTrace();
                }
                break;
            case MSG_SAVE_WORKTIME:
                Bundle bun = msg.getData();
                int sh = 0,sm = 0,eh = 0,em = 0;
                if(bun != null){
                    sh = Integer.valueOf(bun.getString("sh"));
                    sm = Integer.valueOf(bun.getString("sm"));
                    eh = Integer.valueOf(bun.getString("eh"));
                    em = Integer.valueOf(bun.getString("em"));
                }
                try {
                    if (mPassWordStr != null) {
                        int flag = mICaSettingService.setWatchTime(
                                mPassWordStr, sh, sm, eh, em);
                        showOperateMsg(flag);
                        mPassWordStr = null;
                    }
                } catch (Exception e) {
                    
                    e.printStackTrace();
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
        mMainView = inflater.inflate(R.layout.config_work_time_layout,
                container, false);
        initView();
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
                    } else {
                        mainHandler.sendEmptyMessage(MSG_GET_WORKTIME);
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
     *工作时段布局初始化
     */
    private void initView(){
//        mEditSH = (EditText) mMainView.findViewById(R.id.config_work_time_sth_edittext);
//        mEditSM = (EditText) mMainView.findViewById(R.id.config_work_time_stm_edittext);
//        mEditEH = (EditText) mMainView.findViewById(R.id.config_work_time_enh_edittext);
//        mEditEM = (EditText) mMainView.findViewById(R.id.config_work_time_enm_edittext);
        
        mBtnSave = (Button) mMainView.findViewById(R.id.config_work_time_btn_save);
        mBtnCancel = (Button) mMainView.findViewById(R.id.config_work_time_btn_cancel);
        
        mSpinnerSH = (Spinner) mMainView.findViewById(R.id.config_set_worktime_hour);
        mSpinnerSM = (Spinner) mMainView.findViewById(R.id.config_set_worktime_minute);
        mSpinnerEH = (Spinner) mMainView.findViewById(R.id.config_set_worktime_hour_end);
        mSpinnerEM = (Spinner) mMainView.findViewById(R.id.config_set_worktime_minute_end);
        
        mBtnSave.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);
        
        setSpinnerValue(getListTime(0,23), mSpinnerSH);
        setSpinnerValue(getListTime(0,59), mSpinnerSM);
        setSpinnerValue(getListTime(0,23), mSpinnerEH);
        setSpinnerValue(getListTime(0,59), mSpinnerEM);
        mSpinnerSH.requestFocus();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        /*
         * 工作时段布局相关
         */
        case R.id.config_work_time_btn_save:// 保存并返回
            workTimeSave();
            break;
        case R.id.config_work_time_btn_cancel:// 取消
//            initView();
            getActivity().getFragmentManager().popBackStack();
            break;
        }
    }
    /**
     * 工作时段保存
     */
    private void workTimeSave() {
        String sth = mSpinnerSH.getSelectedItem().toString();
        String stm = mSpinnerSM.getSelectedItem().toString();
        String seh = mSpinnerEH.getSelectedItem().toString();
        String sem = mSpinnerEM.getSelectedItem().toString();
        int bh = Integer.valueOf(sth);
        int bm = Integer.valueOf(stm);
        int eh = Integer.valueOf(seh);
        int em = Integer.valueOf(sem);
        int begin_time = bh * 60 + bm;
        int end_time = eh * 60 + em;
        // 判断开始时间是不是小于等于结束时间
        if (end_time > begin_time) {
            showDia(sth, stm, seh, sem);
        } else {
            AdapterViewSelectionUtil.showToast(this.getActivity(),
                    R.string.ca_modify_worktime_error0);
        }
    }
    /**
     * 弹出设置工作时段密码输入框
     * @param sth 开始小时
     * @param stm 开始分钟
     * @param seh 结束小时
     * @param sem 结束分钟
     */
    private void showDia(final String sth,final String stm,final String seh,final String sem){
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
                        m.what = MSG_SAVE_WORKTIME;
                        Bundle bun = new Bundle();
                        bun.putString("sh", sth);
                        bun.putString("sm", stm);
                        bun.putString("eh", seh);
                        bun.putString("em", sem);
                        m.setData(bun);
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
    /**
     * 弹出操作提示框
     * @param witch
     */
    public void showOperateMsg(int witch) {
        switch (witch) {
        case OperateMsg.CDCA_RC_OK:
            AdapterViewSelectionUtil.showToast(this.getActivity(),
                    R.string.config_work_time_save_success);
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
        case OperateMsg.CDCA_RC_UNKNOWN:
        default:
            AdapterViewSelectionUtil.showToast(this.getActivity(),
                    R.string.ca_modify_worktime_error);
            break;
        }
    }
    /**
     * 设置工作时段View显示
     * @param startH
     * @param startM
     * @param endH
     * @param endM
     */
    public void setDataToView(int startH,int startM,int endH,int endM){
        Logcat(" setDataToView ");
        mSpinnerSH.setSelection(startH);
        mSpinnerSM.setSelection(startM);
        mSpinnerEH.setSelection(endH);
        mSpinnerEM.setSelection(endM);
    }
}
