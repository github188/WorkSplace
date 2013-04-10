package com.joysee.settings.applock;

import com.lenovo.settings.LenovoSettingsActivity;
import com.lenovo.settings.R;
import com.lenovo.settings.SettingFragment;
import com.lenovo.settings.Util.AdapterViewSelectionUtil;
import com.lenovo.settings.db.AppInfoBean;
import com.lenovo.settings.db.DBUtil;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AppLockSettings extends Fragment implements View.OnClickListener{
	protected static final String TAG = "AppLockSettings";
    private SettingFragment appLockFragment;
	private View mView;
	private LinearLayout mAppLockOnOff;
	private LinearLayout mAppLockSetPwd;
    private Activity mContext;  //
    private AppInfoBean mAppInfoBean;
    private ArrayList<AppInfoBean> appList = new ArrayList<AppInfoBean>();
    private DBUtil mDButil;
    private TextView textView;

    private Dialog mDialog;
    /**密码*/
    private String mPassWordStr;
    /**开关*/
    private int mOnOff ;
    
    public static final class OperateMsg {
        /** 操作成功 */
        public static final int APPLOCK_PWD_OK = 0;
        /** 旧密码输入错误 */
        public static final int APPLOCK_PWD_OLD_INVALID = 1;
        /** 指针为空 */
        public static final int APPLOCK_PWD_POINTER_INVALID = 2;
        /** 输入pin 码无效 不在0x00~0x09之间 */
        public static final int APPLOCK_PWD_INVALID = 4;
    }
    
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.applock_set_onoff:
                SetLockOnOff();
                break;
            case R.id.applock_psswd:
                AppLockPwdFragment appLockPwdFragment = new AppLockPwdFragment();
                appLockFragment.setFragment(appLockPwdFragment, true);
                break;
        }
    }
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOnOff = 0;
    }
    
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        mContext = this.getActivity();
		appLockFragment = new SettingFragment(getActivity());
		mView = inflater.inflate(R.layout.applock_setting, container, false);
		textView = (TextView) mView.findViewById(R.id.applock_status);
		LenovoSettingsActivity.setTitleFocus(false);
		initView();
		initData();
		return mView;
	}
	
    @Override
    public void onResume() {
        super.onResume();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
	
    private void initView() {
        mAppLockOnOff = (LinearLayout) mView.findViewById(R.id.applock_set_onoff);
        mAppLockSetPwd = (LinearLayout) mView.findViewById(R.id.applock_psswd);
        /*
         * mAppLockOnOff.setOnClickListener(new OnClickListener() {
         * @Override public void onClick(View v) { SetLockOnOff(); } });
         */
        mAppLockOnOff.setOnClickListener(this);
        mAppLockSetPwd.setOnClickListener(this);

        mAppLockOnOff.requestFocus();
    }

    private void initData() {
        mDButil = DBUtil.getInstance(getActivity());
        appList = mDButil.getAllAppRecord();
        if (appList.size() > 0) {
            //获取密码
            mAppInfoBean = appList.get(0);
            if (mAppInfoBean.lockState == 1) {
                textView.setText(getResources().getText(R.string.applock_txt_status_on));
            }else{
                textView.setText(getResources().getText(R.string.applock_txt_status_off));
            }
        }
    }
    private void SetLockOnOff() {
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
            int width = (int) getResources().getDimension(R.dimen.applock_pwd_dialog_width);
            int height = (int) getResources().getDimension(R.dimen.applock_pwd_dialog_height);
            mDialog.setContentView(vi, new LinearLayout.LayoutParams(width, height));
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
                        if (mAppInfoBean != null) {
                            if (mPassWordStr.equals(mAppInfoBean.password)) {
                                Intent intent = null;
                                Log.d(TAG, " mAppInfoBean  = " + mAppInfoBean);
                                if (mAppInfoBean.lockState == 0) {
                                    mAppInfoBean.lockState = 1;
                                } else {
                                    mAppInfoBean.lockState = 0;
                                }
                                mDButil.updateRecord(1, mPassWordStr, mAppInfoBean.lockState);
                                if (mAppInfoBean.lockState == 0) {
                                    textView.setText(getResources().getText(
                                            R.string.applock_txt_status_off));
                                    AdapterViewSelectionUtil.showToast(mContext,
                                            R.string.applock_off_ok);
                                    intent = new Intent(
                                            "joysee.intent.cation_PARENTALCONTROL_TURN_OFF");
                                    mContext.sendBroadcast(intent);
                                } else {
                                    textView.setText(getResources().getText(
                                            R.string.applock_txt_status_on));
                                    AdapterViewSelectionUtil.showToast(mContext,
                                            R.string.applock_on_ok);
                                    intent = new Intent(
                                            "joysee.intent.cation_PARENTALCONTROL_TURN_ON");
                                    mContext.sendBroadcast(intent);
                                }
                                Log.d(TAG, " intent = " + intent);
                            } else {
                                AdapterViewSelectionUtil.showToast(mContext,
                                        R.string.config_midify_old_wrong);
                            }
                        }
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

    @Override
    public void onStop() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        super.onPause();
    }
}