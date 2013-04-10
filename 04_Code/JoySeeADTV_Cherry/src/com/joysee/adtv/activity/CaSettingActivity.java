package com.joysee.adtv.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DefaultParameter;
import com.joysee.adtv.common.DvbLog;
import com.joysee.adtv.common.ToastUtil;
import com.joysee.adtv.logic.CaManager;
import com.joysee.adtv.logic.bean.LicenseInfo;
import com.joysee.adtv.logic.bean.WatchTime;
import com.joysee.adtv.ui.adapter.SettingAuthAdapter;

/**
 * 智能卡设置界面
 * @author wuhao
 */
public class CaSettingActivity extends Activity implements View.OnClickListener{

    private static final DvbLog log = new DvbLog(
            "com.joysee.adtv.activity.CaSettingActivity",DvbLog.DebugType.D);

    /**
     * 用于控制界面的退出
     * 防止在子界面时 esc 直接退出activity
     */
    private boolean isFinished = true;
    
    private ImageView mFocusView;
    
    /**
     * 各界面的顶部标题
     */
    private TextView mTitleTextView;

    /** 背景图 */
    private RelativeLayout backGround;

    private RelativeLayout config_watch_level;
    private RelativeLayout config_work_time;
    private RelativeLayout config_authorise_message_bg;
    private RelativeLayout config_modify_password_bg;
    private RelativeLayout config_cardinfo_layout;

    // 智能卡布局控件
    /** setting 主界面工作时段按钮 */
//    private Button mWorkTime;
    private LinearLayout mWorkTime;
    private TextView mWorkTimeMsg;
    /** setting 主界面授权信息按钮 */
    private LinearLayout mAuthorMessage;
    private TextView mAuthorMessageMsg;
    /** setting 主界面修改密码按钮 */
    private LinearLayout mModifyPassword;
    private TextView mModifyPasswordMsg;
    /** setting watch level button */
    private LinearLayout mWatchLevel;
    private TextView mWatchLevelMsg;
    /** cardinfo button */
    private LinearLayout mCardInfo;
    private TextView mCardInfoMsg;
    
    private TextView mCardNum;

    //工作时段布局控件
    /** setting 工作时段开始工作时间小时edit */
    private TextView mEditSH;
    /** setting 工作时段开始工作时间分钟edit */
    private TextView mEditSM;
    /** setting 工作时段开始工作时间秒edit */
    private TextView mEditSS;
    /** setting 工作时段结束工作时间小时edit */
    private TextView mEditEH;
    /** setting 工作时段结束工作时间分钟edit */
    private TextView mEditEM;
    /** setting 工作时段结束工作时间秒edit */
    private TextView mEditES;
    /** setting 工作时段保存按钮 */
    private Button mBtnSave;
    private Button mBtnCancel;
    //新布局 spinner 列表
//    private Spinner mSpinnerSH;
//    private Spinner mSpinnerSM;
//    private Spinner mSpinnerSS;
//    private Spinner mSpinnerEH;
//    private Spinner mSpinnerEM;
//    private Spinner mSpinnerES;
    //观看级别布局控件
    private TextView mWatchDropTv;
    private Button mWatchSave;
    private Button mWatchCancel;
    private Spinner mWatchSpinner;

    //授权信息布局控件
    private TextView mAuthorDropTv;
//    private Spinner mAuthSpinner;
    /** 授权信息 list */
    private ListView mListView;
    /** 授权信息 数据适配 */
    private SettingAuthAdapter mAdapter;
    /**显示用户特征*/
    private TextView mOperaterFeatureTv;
    private ArrayList<String> mAuthorIDs;
    //修改密码布局控件
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
    //卡信息
    /** 本机卡号 */
    private TextView mCardNumberTv;
    /** STBID */
    private TextView mSTDIDTv;
//    /** bootloader */
//    private TextView mBootloaderTv;
    /** update time */
    private TextView mUpdateTimeTv;
    /** update state */
    private TextView mUpdateStateTv;
    /** Ca version */
    private TextView mCaVersionTv;
    private String mCardNumberStr = null;
    private String mSTDIDNumberStr = null;
    private String mCaVersionStr = null;

    /** 主线程处理  mainhandler */
    private Handler mMainHandler;
    
    private Dialog mInputPsdDia;
    private EditText mInputPsdEditText;
    private TextView mInputPsdErrorText;
    private Button mInputBtnOk;
    private Button mInputBtnCancel;
    
    private CaManager mCaManager;
    
    private static int mNowPage = NowPage.NOWPAGE_MENU;

    private static class NowPage{
        /**menu*/
        public static final int NOWPAGE_MENU = 0;
        /**worktime*/
        public static final int NOWPAGE_WORK_TIME = 1;
        /**authorise*/
        public static final int NOWPAGE_AUTHORISE = 2;
        /**modify password*/
        public static final int NOWPAGE_MODIFY_PASSWD= 3;
        /**watch level*/
        public static final int NOWPAGE_WATCH_LEVEL = 4;
        /**cardinfo*/
        public static final int NOWPAGE_CARD_INFO = 5;
    }
    private String workTime_sh = "sh";
    private String workTime_sm = "sm";
    private String workTime_ss = "ss";
    private String workTime_eh = "eh";
    private String workTime_em = "em";
    private String workTime_es = "es";
    private String password_old = "old";
    private String password_new = "new";
    /**密码长度*/
    private int mPasswordLength = 6;

	private int mFirstFocusOffset;
    
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mCaManager = CaManager.getCaManager();
        initMainHandler();
        mFirstFocusOffset = (int)getResources().getDimension(R.dimen.ca_setting_foucus_offset);
    }

    @Override
    protected void onResume() {
        super.onResume();
        switch (mNowPage) {
            case NowPage.NOWPAGE_MENU:
                isFinished = true;
                initMenuView();
                break;
            case NowPage.NOWPAGE_WORK_TIME:
                isFinished = false;
                initWorkTimeView();
                break;
            case NowPage.NOWPAGE_AUTHORISE:
                isFinished = false;
                initAuthoriseView();
                break;
            case NowPage.NOWPAGE_MODIFY_PASSWD:
                isFinished = false;
                initPasswordView();
                break;
            case NowPage.NOWPAGE_CARD_INFO:
                isFinished = false;
                initCardInfoView();
                break;
            case NowPage.NOWPAGE_WATCH_LEVEL:
                isFinished = false;
                initWatchLevelView();
                break;
        }
        mWorkTime.requestFocus();
    }

    /**
     *智能卡主菜单初始化
     */
    public void initMenuView(){
        setContentView(R.layout.ca_settings_layout);
        
        backGround = (RelativeLayout) this.findViewById(R.id.ca_settings_layout_bg);
        
//        if(getThemePaper() != null){
//            backGround.setBackgroundDrawable(getThemePaper());
//        }else{
//            log.D("getThemePaper() is null");
//        }
        
        mWorkTime = (LinearLayout) this.findViewById(R.id.ca_setting_work_time_layout);
        mAuthorMessage = (LinearLayout) this.findViewById(R.id.ca_setting_authorise_message_layout);
        mModifyPassword = (LinearLayout) this.findViewById(R.id.ca_setting_modify_password_layout);
        mWatchLevel = (LinearLayout) this.findViewById(R.id.ca_setting_watch_level_layout);
        mCardInfo = (LinearLayout) this.findViewById(R.id.ca_setting_cardinfo_layout);

        mWorkTimeMsg = (TextView) findViewById(R.id.ca_setting_work_time_range);
        mAuthorMessageMsg = (TextView) findViewById(R.id.ca_setting_authorise_message);
        mModifyPasswordMsg = (TextView) findViewById(R.id.ca_setting_modify_password_text);
        mWatchLevelMsg = (TextView) findViewById(R.id.ca_setting_watch_level_text);
        mCardInfoMsg = (TextView) findViewById(R.id.ca_setting_card_info_message_text);
        
        mTitleTextView = (TextView) this.findViewById(R.id.ca_setting_title);
        mCardNum = (TextView) findViewById(R.id.ca_setting_card_num);
        mFocusView = (ImageView) findViewById(R.id.ca_iv_focus);
        mWorkTime.setOnClickListener(this);
        mAuthorMessage.setOnClickListener(this);
        mModifyPassword.setOnClickListener(this);
        mWatchLevel.setOnClickListener(this);
        mCardInfo.setOnClickListener(this);
        
        mWorkTime.setOnFocusChangeListener(onFocusChangeListener);
        mAuthorMessage.setOnFocusChangeListener(onFocusChangeListener);
        mModifyPassword.setOnFocusChangeListener(onFocusChangeListener);
        mWatchLevel.setOnFocusChangeListener(onFocusChangeListener);
        mCardInfo.setOnFocusChangeListener(onFocusChangeListener);
        
        switch (mNowPage) {
	        case NowPage.NOWPAGE_AUTHORISE:
	            mAuthorMessage.requestFocus();
	            break;
	        case NowPage.NOWPAGE_MODIFY_PASSWD:
	            mModifyPassword.requestFocus();
	            break;
	        case NowPage.NOWPAGE_WATCH_LEVEL:
	            mWatchLevel.requestFocus();
	            break;
	        case NowPage.NOWPAGE_WORK_TIME:
	            mWorkTime.requestFocus();
	            break;
	        case NowPage.NOWPAGE_CARD_INFO:
	            mCardInfo.requestFocus();
	            break;
	        case NowPage.NOWPAGE_MENU:
	            mWorkTime.requestFocus();
	            break;
        }
        isFinished = true;
        mNowPage = NowPage.NOWPAGE_MENU;
        
        mMainHandler.sendEmptyMessage(HandlerMsg.CONFIG_GET_WORK_TIME);
        mMainHandler.sendEmptyMessage(HandlerMsg.CONFIG_WATCH_LEVEL_GET);
        mMainHandler.sendEmptyMessage(HandlerMsg.CONFIG_CARDINFO);
    }
    private static final class ModifyPwdMsg{
        /**操作成功*/
        public static final int CDCA_RC_OK = 0;
        /**未知错误*/
        public static final int CDCA_RC_UNKNOWN = 1;
        /**指针为空*/
        public static final int CDCA_RC_POINTER_INVALID = 2;
        /**智能卡不在机顶盒内或者是无效卡*/
        public static final int CDCA_RC_CARD_INVALID = 3;
        /**输入pin 码无效 不在0x00~0x09之间*/
        public static final int CDCA_RC_PIN_INVALID = 4;
        /**没有找到运营商*/
        private static final int CDCA_RC_DATA_NOT_FIND = 8;
    }
    /**
     * 主线程handler 刷新界面
     */
	public void initMainHandler(){
        mMainHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what){
                    case HandlerMsg.CONFIG_SET_WORK_TIME://设置工作时间
                        Bundle bun = msg.getData();
                        int sh = 0,sm = 0,ss = 0 ,eh = 0,em = 0,es = 0;
                        if (bun != null) {
                            sh = Integer.valueOf(bun.getString(workTime_sh));
                            sm = Integer.valueOf(bun.getString(workTime_sm));
                            ss = Integer.valueOf(bun.getString(workTime_ss));
                            eh = Integer.valueOf(bun.getString(workTime_eh));
                            em = Integer.valueOf(bun.getString(workTime_em));
                            es = Integer.valueOf(bun.getString(workTime_es));
                        }
                        log.D(" ----- nativeSetWatchTime sh = " + sh + " sm = " + sm + " ss = " + ss);
                        log.D(" ----- nativeSetWatchTime eh = " + eh + " em = " + em + " es = " + es);
                        WatchTime watchTime = new WatchTime(sh, sm, ss, eh, em, es);
                        int wong = mCaManager.nativeSetWatchTime(passwd, watchTime);
                        passwd = null;
                        log.D("----------mCaManager.nativeSetWatchTime back = " + wong);
                        showErrorInfo(HandlerMsg.CONFIG_SET_WORK_TIME, wong);
                        break;
                    case HandlerMsg.CONFIG_MODIFY_PASSWORD://修改密码返回
                        Bundle bundle = msg.getData();
                        if(bundle==null){
                            return ;
                        }
                        String old = bundle.getString(password_old);
                        String newP = bundle.getString(password_new);

                        int change = mCaManager.nativeChangePinCode(old, newP);
                        log.D("----------mCaManager.nativeChangePinCode back = "+change);
                        showErrorInfo(HandlerMsg.CONFIG_MODIFY_PASSWORD, change);
                        break;
                    case HandlerMsg.CONFIG_WATCH_LEVEL_SET://设置观看级别返回
                        String level = (String) msg.obj;
                        int lev =0;
                        if(level != null){
                             lev = Integer.valueOf(level);
                        }
                        int wong1 = mCaManager.nativeSetWatchLevel(passwd, lev);
                        passwd = null;
                        log.D("----------mCaManager.nativeSetWatchLevel back = " + wong1);
                        showErrorInfo(HandlerMsg.CONFIG_WATCH_LEVEL_SET, wong1);
                        break;
                    case HandlerMsg.CONFIG_AUTHOR_OPERTER_ID://获取到运营商ID
                        Vector<Integer> vec = new Vector<Integer>();
                        int wrong = mCaManager.nativeGetOperatorID(vec);
                        log.D("-------mCaManager.nativeGetOperatorID back = " + wrong);
                        if (vec.size() > 0) {
                        	mAuthorDropTv.setText(String.valueOf(vec.get(0)));
                        	Message m = new Message();
		                     m.what = HandlerMsg.CONFIG_AUTHOR_GET_MESSAGE;
		                     m.obj = String.valueOf(vec.get(0));
		                     mMainHandler.sendMessage(m);
                        	mAuthorIDs = new ArrayList<String>();
                        	for (int i: vec) {
                        		mAuthorIDs.add(String.valueOf(i));
							}
//                            setSpinnerInteger(vec, mAuthSpinner);
                        } else {
                            ToastUtil.showToast(CaSettingActivity.this,
                                    R.string.config_midify_card_not_finded);
                        }
                        break;
                    case HandlerMsg.CONFIG_AUTHOR_GET_MESSAGE://获取到授权信息
                        String id = (String) msg.obj;
                        if (id != null && id.length() > 0) {
                            log.D("operater id = " + id);
                            vectorAuthInfo.clear();
                            int back0 = mCaManager.nativeGetAuthorization(Integer.valueOf(id), vectorAuthInfo);
                            log.D("------mJniSetting.getAuthorization back = " + back0);
                            showErrorInfo(HandlerMsg.CONFIG_AUTHOR_GET_MESSAGE,back0);
                            if (back0 == 0) {
                                mOperaterFeature.clear();
                                int back1 = mCaManager.nativeGetOperatorACs(Integer.valueOf(id),
                                        mOperaterFeature);
                                log.D("------mJniSetting.getOperatorAC back = " + back1);
                                mOperaterFeatureTv.setText(getResources()
                                        .getString(R.string.config_authorise_operate_feature,
                                                getUserFeature()));
                                setAdapter(mListView);
                            }
                        } else {
                            log.D("operater id = null || operater id length <=0");
                        }
                        break;
                    case HandlerMsg.CONFIG_GET_WORK_TIME://获取到工作时段
                        WatchTime time = new WatchTime();
                        int back = mCaManager.nativeGetWatchTime(time);
                        showErrorInfo(HandlerMsg.CONFIG_GET_WORK_TIME, back);
                        log.D("--------mCaManager.nativeGetWatchTime back = " + back);
                        setWorkTime(time);
                        break;
                    case HandlerMsg.CONFIG_WATCH_LEVEL_GET://获取观看级别
                        int level1 = mCaManager.nativeGetWatchLevel();
                        log.D("--------mCaManager.nativeGetWatchLevel() back = " + level1);
                        log.D("config_watch_level_get=" + level1);
                        if (level1 > 0) {
                        	if(mNowPage == NowPage.NOWPAGE_MENU){
                        		mWatchLevelMsg.setText(getResources().getString(R.string.config_watch_level_age_up, String.valueOf(level1)));
                        	}else{
//                        		mWatchSpinner.setSelection(level1 - 4);// 观看级别从4岁开始
                        		mWatchDropTv.setText(String.valueOf(level1));
                        	}
                        }
                        break;
                    case HandlerMsg.CONFIG_CARDINFO:
                        mCardNumberStr = null;
                        mSTDIDNumberStr = null;
                        mCaVersionStr = null;
                        if(mNowPage == NowPage.NOWPAGE_MENU){
                        	mCardNumberStr =  mCaManager.nativeGetCardSN();
                        	log.D(" mCardNumberStr "+mCardNumberStr);
                        	if(mCardNumberStr == null || mCardNumberStr.equals("")){
	                        		ToastUtil.showToast(CaSettingActivity.this,
	                        				R.string.config_midify_card_not_finded);
	                        		return;
                        		}
                        	mCardNum.setText(getResources().getString(R.string.config_card_number)+mCardNumberStr);
                        }else if(mNowPage == NowPage.NOWPAGE_CARD_INFO){
                        	mCardNumberStr =  mCaManager.nativeGetCardSN();
                        	mSTDIDNumberStr =  mCaManager.nativeGetSTBID();
                        	mCaVersionStr =  mCaManager.nativeGetCAVersionInfo();
                        	log.D("getCardSN mCardNumberStr = " + mCardNumberStr + " stbid = "
                        			+ mSTDIDNumberStr + " version = " + mCaVersionStr);
                        	if (mCardNumberStr == null || mSTDIDNumberStr == null
                        			|| mCaVersionStr == null || mCardNumberStr.equals("")
                        			|| mCaVersionStr.equals("")
                        			|| mSTDIDNumberStr.equals("")) {
                        		ToastUtil.showToast(CaSettingActivity.this,
                        				R.string.config_midify_card_not_finded);
                        		return;
                        	}
                        	mCardNumberTv.setText(mCardNumberStr);
                        	mSTDIDTv.setText(mSTDIDNumberStr);
                        	mCaVersionTv.setText(getResources().getString(R.string.config_card_ca_version_text, mCaVersionStr));
                        }
                    default:
                        break;
                }
                super.handleMessage(msg);
            }

        };
    }

    private void clearPasswdEdit() {
        mEditOld.setText("");
        mEditNew.setText("");
        mEditNewC.setText("");
    }

    private static final class HandlerMsg{
//        /** 获取本机卡号 */
//        public static final int CONFIG_GET_CARD_NUMBER = 0;
        /** 设置工作时段 */
        public static final int CONFIG_SET_WORK_TIME = 1;
        /** 修改密码 */
        public static final int CONFIG_MODIFY_PASSWORD = 2;
        /** 获取运营商ID */
        private static final int CONFIG_AUTHOR_OPERTER_ID = 3;
        /** 获取授权信息 */
        private static final int CONFIG_AUTHOR_GET_MESSAGE = 4;
        /** 获取工作时段 */
        private static final int CONFIG_GET_WORK_TIME = 5;
        /**获取观看级别*/
        private static final int CONFIG_WATCH_LEVEL_GET = 6;
        /**设置观看级别*/
        private static final int CONFIG_WATCH_LEVEL_SET = 7;
        /**卡信息*/
        private static final int CONFIG_CARDINFO = 8;
    }

    /**
     *工作时段布局初始化
     */
    private void initWorkTimeView(){
        setContentView(R.layout.ca_work_time_layout);
        
        config_work_time = (RelativeLayout) this.findViewById(R.id.config_work_time);
        
        if(getThemePaper() != null){
            config_work_time.setBackgroundDrawable(getThemePaper());
        }
        
        mTitleTextView = (TextView) this.findViewById(R.id.ca_setting_title);
        mEditSH = (TextView) this.findViewById(R.id.config_work_time_sth_edittext);
        mEditSM = (TextView) this.findViewById(R.id.config_work_time_stm_edittext);
        mEditSS = (TextView) this.findViewById(R.id.config_work_time_sts_edittext);
        mEditEH = (TextView) this.findViewById(R.id.config_work_time_enh_edittext);
        mEditEM = (TextView) this.findViewById(R.id.config_work_time_enm_edittext);
        mEditES = (TextView) this.findViewById(R.id.config_work_time_ens_edittext);
        
        mBtnSave = (Button) this.findViewById(R.id.config_work_time_btn_save);
        mBtnCancel = (Button) this.findViewById(R.id.config_work_time_btn_cancel);
        
        
//        mSpinnerSH = (Spinner) this.findViewById(R.id.config_set_worktime_hour);
//        mSpinnerSM = (Spinner) this.findViewById(R.id.config_set_worktime_minute);
//        mSpinnerSS = (Spinner) this.findViewById(R.id.config_set_worktime_second);
//        mSpinnerEH = (Spinner) this.findViewById(R.id.config_set_worktime_hour_end);
//        mSpinnerEM = (Spinner) this.findViewById(R.id.config_set_worktime_minute_end);
//        mSpinnerES = (Spinner) this.findViewById(R.id.config_set_worktime_second_end);
        
        mEditSH.setOnClickListener(this);
        mEditSM.setOnClickListener(this);
        mEditSS.setOnClickListener(this);
        mEditEH.setOnClickListener(this);
        mEditEM.setOnClickListener(this);
        mEditES.setOnClickListener(this);
        
        mBtnSave.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);
        
//        alert(getApplicationContext(), mEditSH, getListTime(0,23));
        
//        setSpinnerValue(getListTime(0,23), mSpinnerSH);
//        setSpinnerValue(getListTime(0,59), mSpinnerSM);
//        setSpinnerValue(getListTime(0,59), mSpinnerSS);
//        setSpinnerValue(getListTime(0,23), mSpinnerEH);
//        setSpinnerValue(getListTime(0,59), mSpinnerEM);
//        setSpinnerValue(getListTime(0,59), mSpinnerES);
        
        mEditSH.requestFocus();
        setTitleString(R.string.config_work_time);
        
        mMainHandler.sendEmptyMessage(HandlerMsg.CONFIG_GET_WORK_TIME);
        
        mNowPage = NowPage.NOWPAGE_WORK_TIME;
    }
    private void initWatchLevelView(){
        setContentView(R.layout.ca_watch_level_layout);
        
        config_watch_level = (RelativeLayout) this.findViewById(R.id.config_watch_level);
        if(getThemePaper() != null){
            config_watch_level.setBackgroundDrawable(getThemePaper());
        }
        
        mWatchDropTv = (TextView) findViewById(R.id.config_watch_level_edittext);
        mTitleTextView = (TextView) this.findViewById(R.id.ca_setting_title);
        
        mWatchSpinner = (Spinner) this.findViewById(R.id.config_watch_level_spinner);
        
        mWatchSave = (Button) this.findViewById(R.id.config_watch_level_save);
        mWatchCancel = (Button) this.findViewById(R.id.config_watch_level_cancel);
        
        mWatchSave.setOnClickListener(this);
        mWatchCancel.setOnClickListener(this);
        mWatchDropTv.setOnClickListener(this);
//        setSpinnerValue(getListTime(4,18), mWatchSpinner);
        
        mWatchDropTv.requestFocus();
        setTitleString(R.string.config_watch_level);
        
        mMainHandler.sendEmptyMessage(HandlerMsg.CONFIG_WATCH_LEVEL_GET);
        mNowPage = NowPage.NOWPAGE_WATCH_LEVEL;
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
            list.add(String.valueOf(i));
        }
        return list;
    }

    /**
     * 给spinner 赋值
     * @param list
     * @param sp
     */
//    private void setSpinnerValue(final List<String> list, Spinner sp){
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_layout, list);
//        adapter.setDropDownViewResource(R.layout.spinner_item);
//        sp.setAdapter(adapter);
//    }

    private void setWorkTime(WatchTime tag) {
    	if(mNowPage == NowPage.NOWPAGE_MENU){
    		if(mWorkTimeMsg !=null ){
    			StringBuilder sb = new StringBuilder();
    			sb.append(tag.startHour<=9?"0"+String.valueOf(tag.startHour):String.valueOf(tag.startHour));
    			sb.append(":");
    			sb.append(tag.startMin<=9?"0"+String.valueOf(tag.startMin):String.valueOf(tag.startMin));
    			sb.append("-");
    			sb.append(tag.endHour<=9?"0"+String.valueOf(tag.endHour):String.valueOf(tag.endHour));
    			sb.append(":");
    			sb.append(tag.endMin<=9?"0"+String.valueOf(tag.endMin):String.valueOf(tag.endMin));
    			mWorkTimeMsg.setText(sb.toString());
    		}
    	}else{
	        if (mEditSH != null) {
	            mEditSH.setText(tag.startHour + "");
//	            mSpinnerSH.setSelection(tag.startHour);
	        }
	        if (mEditSM != null) {
	            mEditSM.setText(tag.startMin + "");
//	            mSpinnerSM.setSelection(tag.startMin);
	        }
	        if (mEditSS != null) {
	            mEditSS.setText(tag.startSec + "");
//	            mSpinnerSS.setSelection(tag.startSec);
	        }
	        if (mEditEH != null) {
	            mEditEH.setText(tag.endHour + "");
//	            mSpinnerEH.setSelection(tag.endHour);
	        }
	        if (mEditEM != null) {
	            mEditEM.setText(tag.endMin + "");
//	            mSpinnerEM.setSelection(tag.endMin);
	        }
	        if (mEditES != null) {
	            mEditES.setText(tag.endSec + "");
//	            mSpinnerES.setSelection(tag.endSec);
	        }
    	}
    }
    
    /**
     *授权信息布局初始化
     */
    private void initAuthoriseView(){
        setContentView(R.layout.ca_authoriseinfo_layout);
        
        config_authorise_message_bg = (RelativeLayout) this.findViewById(R.id.config_authorise_message_bg);
        
        if(getThemePaper() != null){
            config_authorise_message_bg.setBackgroundDrawable(getThemePaper());
        }
        
        mListView = (ListView) this.findViewById(R.id.config_authrise_listview);
        mListView.setClickable(false);
        mTitleTextView = (TextView) this.findViewById(R.id.ca_setting_title);
        mAuthorDropTv = (TextView) findViewById(R.id.config_authorise_operate_edit);
        mOperaterFeatureTv = (TextView) this.findViewById(R.id.config_authorise_operate_feature);
        
//        mAuthSpinner = (Spinner) this.findViewById(R.id.config_authorise_spinner_id);
//        mAuthSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
//            
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                Message m = new Message();
//                m.what = HandlerMsg.CONFIG_AUTHOR_GET_MESSAGE;
//                m.obj = mAuthSpinner.getSelectedItem().toString();
//                mMainHandler.sendMessage(m);
//            }
//
//            public void onNothingSelected(AdapterView<?> parent) {
//            }
//        });
        
        mAuthorDropTv.setOnClickListener(this);
        setTitleString(R.string.config_authorise_message);
        mAuthorDropTv.requestFocus();
        mOperaterFeatureTv.setText(getResources().getString(R.string.config_authorise_operate_feature, getUserFeature()));
        mMainHandler.sendEmptyMessage(HandlerMsg.CONFIG_AUTHOR_OPERTER_ID);
        mNowPage = NowPage.NOWPAGE_AUTHORISE;
    }

    private void setAdapter(ListView list){
        mAdapter = new SettingAuthAdapter(CaSettingActivity.this, getTestData());
        list.setAdapter(mAdapter);
    }

    /**
     *修改密码布局初始化
     */
    private void initPasswordView() {
        setContentView(R.layout.ca_modify_password_layout);
        
        config_modify_password_bg = (RelativeLayout) this
                .findViewById(R.id.config_modify_password_bg);
        
        if (getThemePaper() != null) {
            config_modify_password_bg.setBackgroundDrawable(getThemePaper());
        }
        
        mEditOld = (EditText) this.findViewById(R.id.config_modify_password_edit_old);
        mEditNew = (EditText) this.findViewById(R.id.config_modify_password_edit_new);
        mEditNewC = (EditText) this.findViewById(R.id.config_modify_password_edit_new_confirm);
        mModifySave = (Button) this.findViewById(R.id.config_modify_password_btn_save);
        mModifyCancel = (Button) this.findViewById(R.id.config_modify_password_btn_cancel);
        mTitleTextView = (TextView) this.findViewById(R.id.ca_setting_title);
        
        mModifySave.setOnClickListener(this);
        mModifyCancel.setOnClickListener(this);
        
        mEditOld.requestFocus();
        setTitleString(R.string.config_modify_password);
        
        mNowPage = NowPage.NOWPAGE_MODIFY_PASSWD;
    }
    /**
     *修改密码布局初始化
     */
    private void initCardInfoView(){
        setContentView(R.layout.ca_cardinfo_layout);

        config_cardinfo_layout = (RelativeLayout) this.findViewById(R.id.config_cardinfo_layout);

        if(getThemePaper() != null){
            config_cardinfo_layout.setBackgroundDrawable(getThemePaper());
        }

        mTitleTextView = (TextView) this.findViewById(R.id.ca_setting_title);
        mCardNumberTv = (TextView) this.findViewById(R.id.config_card_number_tv);
        mSTDIDTv = (TextView) this.findViewById(R.id.config_stbid_number_tv);
//        mBootloaderTv = (TextView) this.findViewById(R.id.config_cardloader_tv);
        mUpdateStateTv = (TextView) this.findViewById(R.id.config_update_state_tv);
        mUpdateTimeTv = (TextView) this.findViewById(R.id.config_updatetime_tv);
        mCaVersionTv = (TextView) this.findViewById(R.id.config_update_ca_version_tv);
        String time = "";
        int temp = 0;
        String state = "";
        try {
            time = getApplication()
                    .getSharedPreferences(
                            DefaultParameter.PREFERENCE_NAME,
                            Activity.MODE_PRIVATE).getString(DefaultParameter.TpKey.KEY_CA_UPDATE_TIME,"");
            temp = getApplication()
                    .getSharedPreferences(
                            DefaultParameter.PREFERENCE_NAME,
                            Activity.MODE_PRIVATE).getInt(DefaultParameter.TpKey.KEY_CA_UPDATE_STATE, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(time.equals("")){
            time = getResources().getString(R.string.ca_noupdate);
        }
        if (temp == 0) {
            state = getResources().getString(R.string.ca_noupdate);
        } else if (temp < 100 && temp > 0) {
            state = getResources().getString(R.string.ca_update_false);
        } else {
            state = getResources().getString(R.string.ca_update_success);
        }
        mUpdateStateTv.setText(getResources().getString(R.string.config_card_update_state, state));
        mUpdateTimeTv.setText(getResources().getString(R.string.config_card_update_time, time));
        mCaVersionTv.setText(getResources().getString(R.string.config_card_ca_version_text, ""));
        setTitleString(R.string.config_card_info_text);
        
        mNowPage = NowPage.NOWPAGE_CARD_INFO;
        mMainHandler.sendEmptyMessage(HandlerMsg.CONFIG_CARDINFO);
    }
    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.ca_setting_work_time_layout:// 显示工作时段布局
                isFinished = false;
                initWorkTimeView();
                break;
            case R.id.ca_setting_authorise_message_layout:// 显示授权信息布局
                isFinished = false;
                initAuthoriseView();
                break;
            case R.id.ca_setting_modify_password_layout:// 显示修改密码布局
                isFinished = false;
                initPasswordView();
                break;
            case R.id.ca_setting_watch_level_layout:// 显示观看级别布局
                isFinished = false;
                initWatchLevelView();
                break;
            case R.id.ca_setting_cardinfo_layout:// 显示卡信息
                isFinished = false;
                initCardInfoView();
                break;
            /*
             * 工作时段布局相关
             */
            case R.id.config_work_time_btn_save:// 保存并返回
                workTimeSave();
                break;
            case R.id.config_work_time_btn_cancel:// 取消
                initMenuView();
                break;
            /*
             * 修改密码布局相关
             */
            case R.id.config_modify_password_btn_save:
                passwordSave();
                break;
            case R.id.config_modify_password_btn_cancel:
                initMenuView();
                break;
            /*
             * 观看级别布局相关
             */
            case R.id.config_watch_level_save:
                watchLevelSave();
                break;
            case R.id.config_watch_level_cancel:
                initMenuView();
                break;
            case R.id.config_work_time_sth_edittext:
            case R.id.config_work_time_enh_edittext:
            	alert(CaSettingActivity.this,(TextView)arg0,getListTime(0,23));
            	break;
            case R.id.config_work_time_stm_edittext:
            case R.id.config_work_time_sts_edittext:
            case R.id.config_work_time_enm_edittext:
            case R.id.config_work_time_ens_edittext:
            	alert(CaSettingActivity.this,(TextView)arg0,getListTime(0,59));
                break;
            case R.id.config_watch_level_edittext:
            	alert(CaSettingActivity.this,(TextView)arg0,getListTime(4,18));
            	break;
            case R.id.config_authorise_operate_edit:
            	if(mAuthorIDs!=null && mAuthorIDs.size()>0)
            		alert(CaSettingActivity.this,(TextView)arg0,mAuthorIDs);
            	break;
        }
    }

    
    private String passwd = null;
    /**
     * 工作时段保存
     */
    private void workTimeSave() {
        String sth = mEditSH.getText().toString();
        String stm = mEditSM.getText().toString();
        String sts = mEditSS.getText().toString();
        String seh = mEditEH.getText().toString();
        String sem = mEditEM.getText().toString();
        String ses = mEditES.getText().toString();
        int bh = Integer.valueOf(sth);
        int bm = Integer.valueOf(stm);
        int bs = Integer.valueOf(sts);
        int eh = Integer.valueOf(seh);
        int em = Integer.valueOf(sem);
        int es = Integer.valueOf(ses);
        int begin_time = bh * 60 + bm + bs;
        int end_time = eh * 60 + em + es;
        // 判断开始时间是不是小于结束时间
        if (end_time > begin_time) {
            showDia(sth, stm, sts, seh, sem, ses);
        } else {
            ToastUtil.showToast(CaSettingActivity.this,
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
       private void showDia(final String sth, final String stm, final String sts, final String seh,
            final String sem, final String ses) {
    	  createDia();
        mInputBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	if(mInputPsdEditText.getVisibility() == View.GONE){
            		mInputPsdEditText.setVisibility(View.VISIBLE);
            		mInputPsdErrorText.setVisibility(View.GONE);
            		mInputPsdEditText.setText("");
            		mInputPsdEditText.requestFocus();
            		return;
            	}
                 passwd = mInputPsdEditText.getText().toString();
                if (passwd.length() == mPasswordLength) {
                    Message m = new Message();
                    m.what = HandlerMsg.CONFIG_SET_WORK_TIME;
                    Bundle bun = new Bundle();
                    bun.putString(workTime_sh, sth);
                    bun.putString(workTime_sm, stm);
                    bun.putString(workTime_ss, sts);
                    bun.putString(workTime_eh, seh);
                    bun.putString(workTime_em, sem);
                    bun.putString(workTime_es, ses);
                    m.setData(bun);
                    mMainHandler.sendMessage(m);
//                    mInputPsdDia.dismiss();
                }else{
//                     ToastUtil.showToast(CaSettingActivity.this, R.string.config_midify_password_input_wrong);
                	mInputPsdEditText.setVisibility(View.GONE);
                     mInputPsdErrorText.setText(R.string.config_midify_password_input_wrong);
                     mInputPsdErrorText.setVisibility(View.VISIBLE);
                 }
            }
        });
    }
   private void createDia(){
	   mInputPsdDia = new Dialog(this,R.style.inpute_psd_dialog_bg);
       View vi = LayoutInflater.from(CaSettingActivity.this).inflate(R.layout.edit_dialog_layout,null);
       mInputPsdEditText = (EditText) vi.findViewById(R.id.config_dialog_eidt);
       mInputPsdErrorText = (TextView) vi.findViewById(R.id.input_psd_wrong_txt);
       mInputBtnOk = (Button) vi.findViewById(R.id.config_dialog_btn_confirm);
       mInputBtnCancel = (Button) vi.findViewById(R.id.config_dialog_btn_cancel);
       int width = (int) getResources().getDimension(R.dimen.input_psd_dialog_weight);
       int height = (int) getResources().getDimension(R.dimen.input_psd_dialog_height);
       mInputPsdDia.setContentView(vi,new LinearLayout.LayoutParams(width, height));
       mInputPsdDia.show();
       mInputBtnCancel.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
           	mInputPsdDia.dismiss();
           }
       });
   }
    private void watchLevelSave(){
//        final String level = mWatchSpinner.getSelectedItem().toString();
        final String level = mWatchDropTv.getText().toString();
        createDia();
        mInputBtnOk.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
            	if(mInputPsdEditText.getVisibility() == View.GONE){
            		mInputPsdEditText.setVisibility(View.VISIBLE);
            		mInputPsdErrorText.setVisibility(View.GONE);
            		mInputPsdEditText.setText("");
            		mInputPsdEditText.requestFocus();
            		return;
            	}
                passwd = mInputPsdEditText.getText().toString();
                if (passwd.length() == mPasswordLength) {
                    Message m = new Message();
                    m.what = HandlerMsg.CONFIG_WATCH_LEVEL_SET;
                    m.obj = level;
                    mMainHandler.sendMessage(m);
                } else {
//                    ToastUtil.showToast(CaSettingActivity.this,
//                            R.string.config_midify_password_input_wrong);
                	mInputPsdEditText.setVisibility(View.GONE);
                    mInputPsdErrorText.setText(R.string.config_midify_password_input_wrong);
                    mInputPsdErrorText.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * 修改密码保存
     */
    private void passwordSave() {
        String oPasswd = mEditOld.getText().toString();
        String nPasswd = mEditNew.getText().toString();
        String nPasswdC = mEditNewC.getText().toString();
        
        if (nPasswd.equals(nPasswdC)) {
            if (oPasswd.length() == nPasswd.length() && nPasswd.length() == mPasswordLength) {
                Message m = new Message();
                m.what = HandlerMsg.CONFIG_MODIFY_PASSWORD;
                Bundle bun = new Bundle();
                bun.putString(password_old, oPasswd);
                bun.putString(password_new, nPasswdC);
                m.setData(bun);
                mMainHandler.sendMessage(m);
            } else {
                ToastUtil.showToast(this, R.string.config_midify_password_input_wrong);
            }
            
        } else {
            mEditNew.setText("");
            mEditNewC.setText("");
            ToastUtil.showToast(this, R.string.config_midify_password_input_compare);
        }
    }

    @Override
    public void onBackPressed() {
        if(isFinished){
            this.finish();
        }else{
            isFinished=true;
            initMenuView();
        }
        
    }

    /**
     * 对顶部标题字符串的变化设置
     */
    private void setTitleString(int res) {
        String str = getResources().getString(R.string.ca_setting_title_text);
        String add = getResources().getString(res);
        if (mTitleTextView != null) {
            mTitleTextView.setText(str + add);
        }
    }
    /**授权信息向量*/
    private Vector<LicenseInfo> vectorAuthInfo = new Vector<LicenseInfo>();
    /**运营商用户特征*/
    private ArrayList<Integer> mOperaterFeature = new ArrayList<Integer>();
    /**
     * 对授权信息整合
     * @function  test
     * @return
     */
    public ArrayList<Map<String, String>> getTestData() {
        ArrayList<Map<String, String>> mList = new ArrayList<Map<String, String>>();
        for (int j = 0; j < vectorAuthInfo.size(); j++) {
            Map<String, String> map = new HashMap<String, String>();
            LicenseInfo t = vectorAuthInfo.get(j);
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
    /**
     * 对用户特征值进行整合
     * @return
     */
    public String getUserFeature() {
        StringBuffer s = new StringBuffer();
        if (mOperaterFeature != null) {
            for (Integer integer : mOperaterFeature) {
                s.append(integer.intValue());
                s.append(" ");
            }
            return s.toString();
        }
        return "";
    }

    private String getDateString(int day){
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy"+getString(R.string.ca_charge_interval_year)+
                            "MM"+getString(R.string.ca_charge_interval_month)+"dd"+getString(R.string.ca_charge_interval_day),Locale.CHINA);
        cal.set(2000, 0, 1);//从1月1号开始算
        cal.add(Calendar.DATE, day);
        Date d = new Date();
        d = cal.getTime();
        String date = format.format(d);
        return date;
    }

    /**
     * 取setting背景图，用于设置成这个activity的背景
     * @return
     */
    public Drawable getThemePaper(){
        String url = Settings.System.getString(this.getContentResolver(), "settings.theme.url");
        if(url!=null && url.length()>0){
                Bitmap bitmap = BitmapFactory.decodeFile(url);
                Drawable drawable = new BitmapDrawable(bitmap);
                return drawable;
        }
        return null;
    }
    void showErrorInfo(int type, int id) {
    	if( id == ModifyPwdMsg.CDCA_RC_PIN_INVALID && type !=HandlerMsg.CONFIG_MODIFY_PASSWORD
    			&&mInputPsdDia!=null && mInputPsdDia.isShowing()){
    		mInputPsdEditText.setVisibility(View.GONE);
            mInputPsdErrorText.setText(R.string.ca_input_password_error);
            mInputPsdErrorText.setVisibility(View.VISIBLE);
    	}else{
    		if(mInputPsdDia!=null && mInputPsdDia.isShowing())
    			mInputPsdDia.dismiss();
	        switch (id) {
	            case ModifyPwdMsg.CDCA_RC_OK:
	                if (type == HandlerMsg.CONFIG_SET_WORK_TIME) {
	                    ToastUtil.showToast(CaSettingActivity.this,
	                            R.string.config_work_time_save_success);
	                    initMenuView();
	                }
	                if (type == HandlerMsg.CONFIG_WATCH_LEVEL_SET) {
	                    ToastUtil.showToast(CaSettingActivity.this,
	                            R.string.config_watch_level_set_success);
	                    initMenuView();
	                }
	                if (type == HandlerMsg.CONFIG_MODIFY_PASSWORD) {
	                    ToastUtil.showToast(CaSettingActivity.this,
	                            R.string.config_midify_password_success);
	                    initMenuView();
	                }
	                break;
	            case ModifyPwdMsg.CDCA_RC_CARD_INVALID:
	                ToastUtil.showToast(CaSettingActivity.this,
	                        R.string.config_midify_card_not_finded);
	                break;
	            case ModifyPwdMsg.CDCA_RC_PIN_INVALID:
	                if (type == HandlerMsg.CONFIG_MODIFY_PASSWORD) {
	                    clearPasswdEdit();
	                    ToastUtil.showToast(CaSettingActivity.this,
	                            R.string.config_midify_old_wrong);
	                } else {
	//                    ToastUtil.showToast(CaSettingActivity.this,
	//                            R.string.ca_input_password_error);//TODO 输入密码错误
	                }
	                break;
	            case ModifyPwdMsg.CDCA_RC_POINTER_INVALID:
	                if (type == HandlerMsg.CONFIG_MODIFY_PASSWORD) {
	                    ToastUtil.showToast(CaSettingActivity.this,
	                            R.string.ca_modify_password_error);
	                }
	                break;
	            case ModifyPwdMsg.CDCA_RC_DATA_NOT_FIND:
	                ToastUtil.showToast(CaSettingActivity.this,
	                        R.string.config_midify_data_not_find);
	                break;
	            case ModifyPwdMsg.CDCA_RC_UNKNOWN:
	            default:
	                if (type == HandlerMsg.CONFIG_MODIFY_PASSWORD) {
	                    ToastUtil.showToast(CaSettingActivity.this,
	                            R.string.ca_modify_password_error);
	                }
	                if (type == HandlerMsg.CONFIG_SET_WORK_TIME) {
	                    ToastUtil.showToast(CaSettingActivity.this,
	                            R.string.ca_modify_worktime_error);
	                }
	                break;
	        }
        }
    }
    private OnFocusChangeListener onFocusChangeListener = new OnFocusChangeListener() {
		
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if(hasFocus){
				int [] location = new int [2];
				v.getLocationInWindow(location);
				MarginLayoutParams params = (MarginLayoutParams) mFocusView
						.getLayoutParams();
				if(location[1] == 0 )
					location[1] = mLastFocusLocation ==0?mFirstFocusOffset:mLastFocusLocation;
				params.topMargin = location[1];
				mLastFocusLocation = location[1];
				mFocusView.setLayoutParams(params);
				log.D("location[1] " +location[1]);
				Animation anim = new AlphaAnimation(0.0f, 1.0f);
				anim.setDuration(300);
				anim.setFillAfter(true);
				anim.setFillEnabled(true);
				mFocusView.startAnimation(anim);
			}
		}
	};
	
	private int mLastFocusLocation;
	private Dialog mAlertDialog;
	private TextView mLastFocusItem;
	public void alert(Context context,final TextView textView,final List<String> list) {
				mTitleTextView.setFocusable(true);
				mTitleTextView.requestFocus();
				textView.clearFocus();
		        View view = LayoutInflater.from(context).inflate(
		                R.layout.ca_down_list_layout, null);
		        mFocusView = (ImageView)view.findViewById(R.id.ivFocus);
		        final ListView downListView = (ListView) view.findViewById(R.id.search_down_listview);
		        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.ca_time_list_item, R.id.ca_time_list_textview, list);
//		        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.search_down_list_item, list);
		        
		        downListView.setAdapter(adapter);
		        if (mAlertDialog == null) {
		            mAlertDialog = new Dialog(context, R.style.searchDownListTheme);
		        }
		        mAlertDialog.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						textView.requestFocus();
						mTitleTextView.setFocusable(false);
					}
				});
		        downListView.setOnItemSelectedListener(new OnItemSelectedListener() {

		            @Override
		            public void onItemSelected(AdapterView<?> parent, View view,
		                    int position, long id) {
		            	log.D(" setOnItemSelectedListener " );
		            	TextView tv = (TextView) view;
		            	tv.setTextColor(getResources().getColor(R.color.green_txt));
		            	mLastFocusItem = tv;
		            }

		            @Override
		            public void onNothingSelected(AdapterView<?> parent) {

		            }
		        });
		        downListView.setOnItemClickListener(new OnItemClickListener() {

		            @Override
		            public void onItemClick(AdapterView<?> parent, View view,
		                    int position, long id) {
		            	String oid = list.get(position);
		            	textView.setText(oid);
		            	log.D(position+ "   list.get(position) "+list.get(position));
		            	mAlertDialog.dismiss();
		            	if(textView == mAuthorDropTv ){
		            		 Message m = new Message();
		                     m.what = HandlerMsg.CONFIG_AUTHOR_GET_MESSAGE;
		                     m.obj = oid;
		                     mMainHandler.sendMessage(m);
		                     
		            	}
		            }
		        });

		        mAlertDialog.setContentView(view);
		        Window window = mAlertDialog.getWindow();
		        LayoutParams params = new LayoutParams();
		        int [] location = new int [2];
		        textView.getLocationInWindow(location);
		        int height = textView.getHeight();

		        log.D("location[0] = " + location[0] + " location[1] = " + location[1]);
		        log.D("height = " + height);

		        Display display = getWindowManager().getDefaultDisplay();
		        //dialog的零点
		        int x = display.getWidth()/2;
		        int y = (int) (getResources().getDimension(R.dimen.menu_height)/2);
		        params.width = textView.getWidth();
		        
		        int itemHeight = (int) (getResources().getDimension(R.dimen.ca_setting_alert_item_height));
		        params.height = display.getHeight() - location[1] -height-itemHeight ;
		        if(textView == mAuthorDropTv ){
		        	params.height = (itemHeight+1)*mAuthorIDs.size();
		        }
		        params.dimAmount = 0.4f;
		        params.flags = LayoutParams.FLAG_DIM_BEHIND;
		        params.x = location[0] - x + params.width/2;
		        params.y = location[1] + height -y + params.height/2;
		        window.setAttributes(params);
		        mAlertDialog.setOnKeyListener(new OnKeyListener() {
		            @Override
		            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		                if(event.getKeyCode() == KeyEvent.KEYCODE_ESCAPE || event.getKeyCode() == KeyEvent.KEYCODE_BACK){
		                    mAlertDialog.dismiss();
		                    return true;
		                }else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_DOWN){
		                	log.D(" DOWN action down  getSelectedItemPosition " +downListView.getSelectedItemPosition() + " getChildCount "+ downListView.getChildCount());
		                	log.D(" DOWN action down  getFirstVisiblePosition " +downListView.getFirstVisiblePosition() + " cha "+( downListView.getSelectedItemPosition()-downListView.getFirstVisiblePosition()));
		                	int selectedItemPosition = downListView.getSelectedItemPosition();
		                	int firstVisiblePosition = downListView.getFirstVisiblePosition();
		                	if(mLastFocusItem!=null && selectedItemPosition != (downListView.getCount() -1))
		                		mLastFocusItem.setTextColor(getResources().getColor(R.color.white_txt));
		                	TextView tv = (TextView) downListView.getChildAt(selectedItemPosition-firstVisiblePosition+1);
		                	if(tv!=null)
		                		tv.setTextColor(getResources().getColor(R.color.green_txt));
		                }
		                else if(keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN){
		                	log.D(" UP action up ");
		                	
		                	int selectedItemPosition = downListView.getSelectedItemPosition();
		                	int firstVisiblePosition = downListView.getFirstVisiblePosition();
		                	if(mLastFocusItem!=null && selectedItemPosition != 0)
		                		mLastFocusItem.setTextColor(getResources().getColor(R.color.white_txt));
		                	TextView tv = (TextView) downListView.getChildAt(selectedItemPosition-firstVisiblePosition-1);
		                	if(tv!=null)
		                		tv.setTextColor(getResources().getColor(R.color.green_txt));
		                }
		                return false;
		            }
		        });
		        mAlertDialog.show();
		        downListView.requestFocus();
		    }

	
}
