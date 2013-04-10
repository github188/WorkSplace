package com.joysee.settings.applock;

import com.joysee.settings.applock.AppLockSettings.OperateMsg;
import com.lenovo.settings.R;
import com.lenovo.settings.Util.AdapterViewSelectionUtil;
import com.lenovo.settings.db.AppInfoBean;
import com.lenovo.settings.db.DBUtil;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class AppLockPwdFragment extends Fragment implements View.OnClickListener{
    private boolean DEBUG = true;
    private String TAG = "AppLockPwdFragment";
    private AppInfoBean mAppInfoBean;
    private ArrayList<AppInfoBean> appList = new ArrayList<AppInfoBean>();
    private DBUtil mDButil;

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
                    if (mAppInfoBean != null && oldPwd.equals(mAppInfoBean.password)) {
                        mDButil.updateRecord(newPwd);
                        showOperateMsg(OperateMsg.APPLOCK_PWD_OK);
                    } else {
                        showOperateMsg(OperateMsg.APPLOCK_PWD_OLD_INVALID);
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
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Logcat("onCreateView");
        mContext = this.getActivity();
        mMainView = inflater.inflate(R.layout.applock_modify_password, container, false);
        initView();
        initData();
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

    public void initData() {
        mDButil = DBUtil.getInstance(getActivity());
        appList = mDButil.getAllAppRecord();
        if (appList.size() > 0) {
            // 获取密码
            mAppInfoBean = appList.get(0);
        }
    }
    @Override
    public void onResume() {
        
        super.onResume();
    }
    @Override
    public void onDestroy() {

 
        super.onDestroy();
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
        case OperateMsg.APPLOCK_PWD_OK:
            AdapterViewSelectionUtil.showToast(this.getActivity(),
                    R.string.config_password_modified_success);
            getActivity().getFragmentManager().popBackStack();
            break;
        case OperateMsg.APPLOCK_PWD_OLD_INVALID:
            AdapterViewSelectionUtil.showToast(this.getActivity(),
                    R.string.config_old_password_input_wrong);
            break;
        default:
            AdapterViewSelectionUtil.showToast(this.getActivity(),
                    R.string.config_password_modified_faild);
            break;
        }
    }
}
