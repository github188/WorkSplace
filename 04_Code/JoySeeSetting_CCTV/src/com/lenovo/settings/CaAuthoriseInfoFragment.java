package com.lenovo.settings;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.joysee.adtv.aidl.ca.ICaSettingService;
import com.joysee.adtv.logic.bean.LicenseInfo;
import com.lenovo.settings.CaMainFragment.OperateMsg;
import com.lenovo.settings.Util.AdapterViewSelectionUtil;
import com.lenovo.settings.adapter.SettingAuthAdapter;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class CaAuthoriseInfoFragment extends Fragment {
    private boolean DEBUG = true;
    private String TAG = "CaAuthoriseInfoFragment";
    /**通过AIDL获得的ICaSettingService代理*/
    private ICaSettingService mICaSettingService;
    private ServiceConnection mConnection ;
    /**主View*/
    private View mMainView;
    private Activity mContext;
    /**授权信息布局控件*/
    private Spinner mAuthSpinner;
    /** 授权信息 list */
    private ListView mListView;
    /** 授权信息 数据适配 */
    private SettingAuthAdapter mAdapter;
    private Vector<LicenseInfo> vector = new Vector<LicenseInfo>();
    private List<Integer> mList;
    private void Logcat(String msg) {
        if (DEBUG) {
            Log.d(TAG, "---------" + msg);
        }
    }
    private static final int MSG_GET_DATA = 0;
    private static final int MSG_GET_ID = 1;
    /**
     * UI主线程的Handler
     */
    private Handler mainHandler = new Handler() {
        public void dispatchMessage(Message msg) {
            switch (msg.what) {
            case MSG_GET_DATA:
                try {
                    String id = (String) msg.obj;
                    if (id != null && id.length() > 0) {
                        Logcat(" getOperatorID id = " + id);
                        setAdapter(mListView,
                                mICaSettingService.getAuthorization(Integer
                                        .parseInt(id)));
                    } else {
                        Logcat("getAuthorization error ,operater id = null or operater id length <=0");
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                    Logcat("getAuthorization error !!! ");
                }
                break;
            case MSG_GET_ID:
                try {
                    mList = mICaSettingService.getOperatorID();
                    if (mList != null && mList.size() > 0) {
                        Logcat(" getOperatorID size = " + mList.size());
                        setSpinnerInteger(mList, mAuthSpinner);
                    }else{
                        showOperateMsg(CaMainFragment.OperateMsg.CDCA_RC_UNKNOWN);
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                    Logcat("getOperatorID error !!! ");
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
        mMainView = inflater.inflate(R.layout.config_authorise_message_layout, container, false);
        initView();
        return mMainView;
    }
    /**
     *授权信息布局初始化
     */
    private void initView(){
        mListView = (ListView) mMainView.findViewById(R.id.config_authrise_listview);
        mAuthSpinner = (Spinner) mMainView.findViewById(R.id.config_authorise_spinner_id);
        mAuthSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Message m = new Message();
                m.what = MSG_GET_DATA;
                m.obj = mAuthSpinner.getSelectedItem().toString();
                mainHandler.sendMessage(m);
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        mainHandler.sendEmptyMessage(MSG_GET_ID);
    }

    private void setAdapter(ListView list,List list2){
        mAdapter = new SettingAuthAdapter(mContext, list2);
        list.setAdapter(mAdapter);
    }
    /**
     * 对授权信息的数据适配
     * @param list
     * @param sp
     */
    private void setSpinnerInteger(List<Integer> list, final Spinner sp){
        ArrayAdapter adapter = new ArrayAdapter(mContext, R.layout.search_spinner_button, list);
        adapter.setDropDownViewResource(R.layout.search_spinner_item);
        sp.setAdapter(adapter);
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
    /**
     * 对授权信息整合
     * @function  test
     * @return
     */
    public ArrayList<Map<String, String>> getTestData() {
        ArrayList<Map<String, String>> mList = new ArrayList<Map<String, String>>();
        for (int j = 0; j < vector.size(); j++) {
            Map<String, String> map = new HashMap<String, String>();
            LicenseInfo t = vector.get(j);
            map.put("number", t.product_id + "");
            map.put("time", getDateString(t.expired_time));
            if (t.is_record) {
                map.put("record",
                        getResources().getString(
                                R.string.config_auth_yes_record));
            } else {
                map.put("record",
                        getResources()
                                .getString(R.string.config_auth_no_record));
            }

            mList.add(map);
        }
        return mList;
    }
    private String getDateString(int day){
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy"+getString(R.string.ca_charge_interval_year)+
                            "MM"+getString(R.string.ca_charge_interval_month)+"dd"+getString(R.string.ca_charge_interval_day));
        cal.set(2000, 0, 1);//从1月1号开始算
        cal.add(Calendar.DATE, day);
        Date d = new Date();
        d = cal.getTime();
        String date = format.format(d);
        return date;
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
                    R.string.ca_getAuthorise_error);
            break;
        }
    }
}
