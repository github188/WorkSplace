package novel.supertv.dvb.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import novel.supertv.dvb.R;
import novel.supertv.dvb.adapter.SettingAuthAdapter;
import novel.supertv.dvb.jni.JniSetting;
import novel.supertv.dvb.jni.struct.tagEntitle;
import novel.supertv.dvb.jni.struct.tagWatchTime;
import novel.supertv.dvb.utils.AdapterViewSelectionUtil;
import novel.supertv.dvb.utils.DvbLog;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 智能卡设置界面
 * @author yanhailong
 */
public class CaSettingActivity extends Activity implements View.OnClickListener{

    private static final DvbLog log = new DvbLog(
            "novel.supertv.dvb.app.activity.CaSettingActivity",DvbLog.DebugType.D);

    /**
     * 用于控制界面的退出
     * 防止在子界面时 esc 直接退出activity
     */
    private boolean isFinished = true;

    /**
     * 各界面的顶部标题
     */
    private TextView mTitleTextView;

    
    // 智能卡布局控件
    /** setting 主界面工作时段按钮 */
    private Button mWorkTime;
    /** setting 主界面授权信息按钮 */
    private Button mAuthorMessage;
    /** setting 主界面修改密码按钮 */
    private Button mModifyPassword;
    /** setting watch level button */
    private Button mWatchLevel;
    /** setting 主界面本机卡号 */
    private TextView mCardNumber;

    //工作时段布局控件
    /** setting 工作时段开始工作时间小时edit */
    private EditText mEditSH;
    /** setting 工作时段开始工作时间分钟edit */
    private EditText mEditSM;
    /** setting 工作时段结束工作时间小时edit */
    private EditText mEditEH;
    /** setting 工作时段结束工作时间分钟edit */
    private EditText mEditEM;
    /** setting 工作时段保存按钮 */
    private Button mBtnSave;
    private Button mBtnCancel;
    //新布局 spinner 列表
    private Spinner mSpinnerSH;
    private Spinner mSpinnerSM;
    private Spinner mSpinnerEH;
    private Spinner mSpinnerEM;
    
    //观看级别布局控件
    private Button mWatchSave;
    private Button mWatchCancel;
    private Spinner mWatchSpinner;

    //授权信息布局控件
    private Spinner mAuthSpinner;
    /** 授权信息 list */
    private ListView mListView;
    /** 授权信息 数据适配 */
    private SettingAuthAdapter mAdapter;

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

    /** 工作线程 */
    private HandlerThread mHandlerThread = new HandlerThread("play work thread");
    /** 工作线程处理 handler */
    private Handler mThreadHandler;
    /** 主线程处理  mainhandler */
    private Handler mMainHandler;

    private JniSetting mJniSetting;
    
    private String mCardNumberStr = null;
    private boolean isFirst = true;
    
    private static int mNowPage = NowPage.NOWPAGE_MENU;

    static 
    {
        System.loadLibrary("adtv");
    }

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
    }
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        log.D("onCreate");
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//        int flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
//        getWindow().addFlags(flags);
        mJniSetting = new JniSetting();
        Intent in = this.getIntent();
        if(!in.hasExtra("first")){
            mJniSetting.caInit();
        }
        initView();
        initWorkThread();
        initMainHandler();
        getCardNumber();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if(!getIntent().hasExtra("first")){
            mJniSetting.caUnInit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!mHandlerThread.isAlive()){
            initWorkThread();
        }
        
        log.D("casettingactivity onResume----------------------------------------------------------------------------::");
        
        switch(mNowPage){
            case NowPage.NOWPAGE_MENU:
                isFinished=true;
                initView();
//                initWorkThread();
//                initMainHandler();
//                getCardNumber();
                break;
            case NowPage.NOWPAGE_WORK_TIME:
                isFinished=false;
                initWorkView();
                break;
            case NowPage.NOWPAGE_AUTHORISE:
                isFinished=false;
                initAuthoriseView();
                break;
            case NowPage.NOWPAGE_MODIFY_PASSWD:
                isFinished=false;
                initPasswordView();
                break;
        }
    }

    /**
     *智能卡布局初始化
     */
    private void initView(){
        setContentView(R.layout.config_layout);
        
        mWorkTime = (Button) this.findViewById(R.id.config_work_time);
        mAuthorMessage = (Button) this.findViewById(R.id.config_authorise_message);
        mModifyPassword = (Button) this.findViewById(R.id.config_modify_password);
        mWatchLevel = (Button) this.findViewById(R.id.config_watch_level);
        mTitleTextView = (TextView) this.findViewById(R.id.config_system_title);
        mCardNumber = (TextView) this.findViewById(R.id.config_card_number);
        
        mWorkTime.setOnClickListener(this);
        mAuthorMessage.setOnClickListener(this);
        mModifyPassword.setOnClickListener(this);
        mWatchLevel.setOnClickListener(this);
        
        mWorkTime.requestFocus();
        if(mCardNumberStr != null){
            mCardNumber.setText(mCardNumberStr);
        }
        
        mNowPage = NowPage.NOWPAGE_MENU;
    }

    /**
     * 工作线程
     */
    public void initWorkThread(){
        mHandlerThread.start();
        mThreadHandler = new Handler(mHandlerThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what){
                    case HandlerMsg.CONFIG_GET_CARD_NUMBER:
                        String str =  mJniSetting.getCardSN();
                        Message m = new Message();
                        m.what = HandlerMsg.CONFIG_GET_CARD_NUMBER;
                        m.obj = str;
                        log.D("222+"+str);
                        mMainHandler.sendMessageDelayed(m, 2000);
                        break;
                    case HandlerMsg.CONFIG_SET_WORK_TIME:
                        Bundle bun = msg.getData();
                        int sh = 0,sm = 0,eh = 0,em = 0;
                        if(bun != null){//前面已检查，
                            sh = Integer.valueOf(bun.getString("sh"));
                            sm = Integer.valueOf(bun.getString("sm"));
                            eh = Integer.valueOf(bun.getString("eh"));
                            em = Integer.valueOf(bun.getString("em"));
                        }
                        if(passwd != null){
                            int wong = mJniSetting.setWatchTime2(passwd,sh,sm,0,eh,em,0);
                            log.D("jnisetting setwatchtime2==============================="+wong);
                            Message setwork = new Message();
                            setwork.arg1 = wong;
                            setwork.what = HandlerMsg.CONFIG_SET_WORK_TIME;
                            mMainHandler.sendMessage(setwork);
                            passwd = null;
                        }
                       
                        break;
                    case HandlerMsg.CONFIG_WATCH_LEVEL_SET://设置观看级别
                        String level = (String) msg.obj;
                        int lev =0;
                        if(level != null){
                             lev = Integer.valueOf(level);
                        }
                        if(passwd != null){
                            int wong = mJniSetting.setWatchLevel(passwd, lev);
                            Message setlevel = new Message();
                            setlevel.arg1 = wong;
                            setlevel.what = HandlerMsg.CONFIG_WATCH_LEVEL_SET;
                            mMainHandler.sendMessage(setlevel);
                            passwd = null;
                        }
                        break;
                    case HandlerMsg.CONFIG_MODIFY_PASSWORD:
                        Bundle bundle = msg.getData();
                        if(bundle==null){
                            return ;
                        }
                        String old = bundle.getString("old");
                        String newP = bundle.getString("new");
                        
                        int change = mJniSetting.changePincode(old, newP);
                        Message pwd = new Message();
                        pwd.what = HandlerMsg.CONFIG_MODIFY_PASSWORD;
                        pwd.arg1 = change;
                        mMainHandler.sendMessage(pwd);
                        break;
                    case HandlerMsg.CONFIG_AUTHOR_OPERTER_ID:
                        Vector<Integer> vec = new Vector<Integer>();
                        int wrong = mJniSetting.getOperatorID(vec);
                        if(wrong>0){
                            log.D("mJnisetting getoperatorid =="+wrong);
                        }
                        Message tm = new Message();
                        tm.obj = vec;
                        tm.what = HandlerMsg.CONFIG_AUTHOR_OPERTER_ID;
                        mMainHandler.sendMessage(tm);
                        break;
                    case HandlerMsg.CONFIG_AUTHOR_GET_MESSAGE:
                        String id = (String) msg.obj;
                        if(id!=null &&id.length()>0){
                            log.D("operater id = "+id);
                            Vector<tagEntitle> tvec = new Vector<tagEntitle>();
                            int back = mJniSetting.getAuthorization(Integer.valueOf(id),tvec);
                            
                            log.D("mJniSetting.getAuthorization="+back);
                                
                            Message th = new Message();
                            th.obj = tvec;
                            th.arg1 = back;
                            th.what = HandlerMsg.CONFIG_AUTHOR_GET_MESSAGE;
                            mMainHandler.sendMessage(th);
                        }else{
                            log.D("operater id = null || operater id length <=0");
                        }
                        break;
                    case HandlerMsg.CONFIG_GET_WORK_TIME:
                        tagWatchTime time = new tagWatchTime();
                        int back = mJniSetting.getWatchTime(time);
                        log.D("mJniSetting.getWatchTime()=="+back);
                        
                        Message tim = new Message();
                        tim.obj = time;
                        tim.what = HandlerMsg.CONFIG_GET_WORK_TIME;
                        mMainHandler.sendMessage(tim);
                        break;
                    case HandlerMsg.CONFIG_WATCH_LEVEL_GET://获取观看级别
                        int level1 = mJniSetting.getWatchLevel();
                        Message lv = new Message();
                        lv.arg1 = level1;
                        lv.what = HandlerMsg.CONFIG_WATCH_LEVEL_GET;
                        mMainHandler.sendMessage(lv);
                        break;
                    default:
                        break;
                }
                super.handleMessage(msg);
            }
        };
        
    }

    private static final class ModifyPwdMsg{
        public static final int CDCA_RC_OK = 0;//操作成功
        public static final int CDCA_RC_UNKNOWN = 1;//未知错误
        public static final int CDCA_RC_POINTER_INVALID = 2;//指针为空
        public static final int CDCA_RC_CARD_INVALID = 3;//智能卡不在机顶盒内或者是无效卡
        public static final int CDCA_RC_PIN_INVALID = 4;//pin 码无效 不在0x00~0x09之间
    }
    /**
     * 主线程handler 刷新界面
     */
    public void initMainHandler(){
        mMainHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what){
                    case HandlerMsg.CONFIG_GET_CARD_NUMBER://获取到卡号，显示
                        mCardNumberStr = (String) msg.obj; 
                        if(mCardNumberStr == null|| mCardNumberStr==""){
                            AdapterViewSelectionUtil.showToast(CaSettingActivity.this, R.string.config_midify_card_not_finded);
                        }
                        mCardNumber.setText(mCardNumberStr);
                        dismissDialog(DialogId.CONFIG_MAIN_GET_CARD_NUMBER);
                        break;
                    case HandlerMsg.CONFIG_SET_WORK_TIME://设置工作时间成功
                        int set = msg.arg1;
                        switch(set){
                            case ModifyPwdMsg.CDCA_RC_OK:
                                AdapterViewSelectionUtil.showToast(CaSettingActivity.this, R.string.config_work_time_save_success);
                                initView();
                                break;
                            case ModifyPwdMsg.CDCA_RC_CARD_INVALID:
                                AdapterViewSelectionUtil.showToast(CaSettingActivity.this, R.string.config_midify_card_not_finded);
                                break;
                            case ModifyPwdMsg.CDCA_RC_PIN_INVALID:
                                AdapterViewSelectionUtil.showToast(CaSettingActivity.this, R.string.config_work_time_input_passward_wrong);
                                break;
                            case ModifyPwdMsg.CDCA_RC_UNKNOWN:
                            default:
                                AdapterViewSelectionUtil.showToast(CaSettingActivity.this, R.string.config_midify_unknow_wrong);
                                break;
                        }
                        break;
                    case HandlerMsg.CONFIG_MODIFY_PASSWORD://修改密码返回
                        int success = msg.arg1;
                        switch(success){
                            case ModifyPwdMsg.CDCA_RC_OK:
                                AdapterViewSelectionUtil.showToast(CaSettingActivity.this, R.string.config_midify_password_success);
                                initView();
                                break;
                            case ModifyPwdMsg.CDCA_RC_CARD_INVALID:
                                AdapterViewSelectionUtil.showToast(CaSettingActivity.this, R.string.config_midify_card_not_finded);
                                break;
                            case ModifyPwdMsg.CDCA_RC_PIN_INVALID:
                                clearPasswdEdit();
                                AdapterViewSelectionUtil.showToast(CaSettingActivity.this, R.string.config_midify_old_wrong);
                                break;
                            case ModifyPwdMsg.CDCA_RC_POINTER_INVALID:
                                AdapterViewSelectionUtil.showToast(CaSettingActivity.this, R.string.config_midify_unknow_wrong);
                                break;
                            case ModifyPwdMsg.CDCA_RC_UNKNOWN:
                            default:
                                AdapterViewSelectionUtil.showToast(CaSettingActivity.this, R.string.config_midify_unknow_wrong);
                                break;
                        }
                        break;
                    case HandlerMsg.CONFIG_WATCH_LEVEL_SET://设置观看级别返回
                        int level = msg.arg1;
                        switch(level){
                            case ModifyPwdMsg.CDCA_RC_OK:
                                AdapterViewSelectionUtil.showToast(CaSettingActivity.this, R.string.config_watch_level_set_success);
                                initView();
                                break;
                            case ModifyPwdMsg.CDCA_RC_CARD_INVALID:
                                AdapterViewSelectionUtil.showToast(CaSettingActivity.this, R.string.config_midify_card_not_finded);
                                break;
                            default:
                                AdapterViewSelectionUtil.showToast(CaSettingActivity.this, R.string.config_midify_unknow_wrong);
                                break;
                        }
                        break;
                    case HandlerMsg.CONFIG_AUTHOR_OPERTER_ID://获取到运营商ID
                        Vector<Integer> vec = (Vector<Integer>) msg.obj;
                        if(vec.size()>0){
                            setSpinnerInteger(vec, mAuthSpinner);
                        }else{
                            AdapterViewSelectionUtil.showToast(CaSettingActivity.this, R.string.config_midify_card_not_finded); 
                        }
                        break;
                    case HandlerMsg.CONFIG_AUTHOR_GET_MESSAGE://获取到授权信息
                        Vector<tagEntitle> message= (Vector<tagEntitle>) msg.obj;
                        int back = msg.arg1;
                        switch(back){//获取授权信息的一些提示信息
                            case ModifyPwdMsg.CDCA_RC_OK:
                                break;
                            case ModifyPwdMsg.CDCA_RC_CARD_INVALID:
                                AdapterViewSelectionUtil.showToast(CaSettingActivity.this, R.string.config_midify_card_not_finded);
                                break;
                            default:
                                break;
                        }
                        v = message;
                        setAdapter(mListView);
                        break;
                    case HandlerMsg.CONFIG_GET_WORK_TIME://获取到工作时段
                        tagWatchTime time = (tagWatchTime) msg.obj;
                        setWorkTime(time);
                        break;
                    case HandlerMsg.CONFIG_WATCH_LEVEL_GET://获取观看级别
                        int level1 = msg.arg1;
                        log.D("config_watch_level_get="+level1);
                        if(level1>0){
                            mWatchSpinner.setSelection(level1-4);//观看级别从4岁开始
                        }
                        break;
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
    
    /**
     * 获取本机卡号
     */
    private void getCardNumber(){
//      mThreadHandler.sendEmptyMessage(HandlerMsg.CONFIG_GET_CARD_NUMBER);
        if(getIntent().hasExtra("first")){
            mThreadHandler.sendEmptyMessage(HandlerMsg.CONFIG_GET_CARD_NUMBER);
        }else{
            mThreadHandler.sendEmptyMessageDelayed(HandlerMsg.CONFIG_GET_CARD_NUMBER,10000);//从外部进入 ca 卡需准备时间
        }
        showDialog(DialogId.CONFIG_MAIN_GET_CARD_NUMBER);
    }

    private static final class HandlerMsg{
        /** 获取本机卡号 */
        public static final int CONFIG_GET_CARD_NUMBER = 0;
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
    }

    /**
     *工作时段布局初始化
     */
    private void initWorkView(){
        setContentView(R.layout.config_work_time_layout);
        
        mTitleTextView = (TextView) this.findViewById(R.id.config_system_title);
        mEditSH = (EditText) this.findViewById(R.id.config_work_time_sth_edittext);
        mEditSM = (EditText) this.findViewById(R.id.config_work_time_stm_edittext);
        mEditEH = (EditText) this.findViewById(R.id.config_work_time_enh_edittext);
        mEditEM = (EditText) this.findViewById(R.id.config_work_time_enm_edittext);
        
        mBtnSave = (Button) this.findViewById(R.id.config_work_time_btn_save);
        mBtnCancel = (Button) this.findViewById(R.id.config_work_time_btn_cancel);
        
        mSpinnerSH = (Spinner) this.findViewById(R.id.config_set_worktime_hour);
        mSpinnerSM = (Spinner) this.findViewById(R.id.config_set_worktime_minute);
        mSpinnerEH = (Spinner) this.findViewById(R.id.config_set_worktime_hour_end);
        mSpinnerEM = (Spinner) this.findViewById(R.id.config_set_worktime_minute_end);
        
        mBtnSave.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);
        
        setSpinnerValue(getListTime(0,23), mSpinnerSH);
        setSpinnerValue(getListTime(0,59), mSpinnerSM);
        setSpinnerValue(getListTime(0,23), mSpinnerEH);
        setSpinnerValue(getListTime(0,59), mSpinnerEM);
        
        mSpinnerSH.requestFocus();
        setTitleString(R.string.config_work_time);
        
        mThreadHandler.sendEmptyMessage(HandlerMsg.CONFIG_GET_WORK_TIME);
        
        mNowPage = NowPage.NOWPAGE_WORK_TIME;
    }

    private void initWatchTime(){
        setContentView(R.layout.config_watch_level_layout);
        
        mTitleTextView = (TextView) this.findViewById(R.id.config_system_title);
        
        mWatchSpinner = (Spinner) this.findViewById(R.id.config_watch_level_spinner);
        
        mWatchSave = (Button) this.findViewById(R.id.config_watch_level_save);
        mWatchCancel = (Button) this.findViewById(R.id.config_watch_level_cancel);
        
        mWatchSave.setOnClickListener(this);
        mWatchCancel.setOnClickListener(this);
        
        setSpinnerValue(getListTime(4,18), mWatchSpinner);
        
        mWatchSpinner.requestFocus();
        setTitleString(R.string.config_watch_level);
        
        mThreadHandler.sendEmptyMessage(HandlerMsg.CONFIG_WATCH_LEVEL_GET);
        
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
            list.add(i+"");
        }
        return list;
    }

    /**
     * 给spinner 赋值
     * @param list
     * @param sp
     */
    private void setSpinnerValue(final List<String> list, Spinner sp){
        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.search_spinner_button, list);
        adapter.setDropDownViewResource(R.layout.search_spinner_item);
        sp.setAdapter(adapter);
    }

    /**
     * 对授权信息的数据适配
     * @param list
     * @param sp
     */
    private void setSpinnerInteger(List<Integer> list, final Spinner sp){
        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.search_spinner_button, list);
        adapter.setDropDownViewResource(R.layout.search_spinner_item);
        sp.setAdapter(adapter);
    }

    private void setWorkTime(tagWatchTime tag){
        if(mEditSH!=null){
            log.D("setworktime to edit starthour="+tag.startHour);
            mEditSH.setText(tag.startHour+"");
            mSpinnerSH.setSelection(tag.startHour);
        }
        if(mEditSM!=null){
            log.D("setworktime to edit startmin="+ tag.startMin);
            mEditSM.setText(tag.startMin+"");
            mSpinnerSM.setSelection(tag.startMin);
        }
        if(mEditEH!=null){
            log.D("setworktime to edit endhour="+tag.endHour);
            mEditEH.setText(tag.endHour+"");
            mSpinnerEH.setSelection(tag.endHour);
        }
        if(mEditEM!=null){
            log.D("setworktiem to edit endmin="+tag.endMin);
            mEditEM.setText(tag.endMin+"");
            mSpinnerEM.setSelection(tag.endMin);
        }
    }

    /**
     *授权信息布局初始化
     */
    private void initAuthoriseView(){
        setContentView(R.layout.config_authorise_message_layout);
        
        mListView = (ListView) this.findViewById(R.id.config_authrise_listview);
        mTitleTextView = (TextView) this.findViewById(R.id.config_system_title);
        mAuthSpinner = (Spinner) this.findViewById(R.id.config_authorise_spinner_id);
        
        mAuthSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Message m = new Message();
                m.what = HandlerMsg.CONFIG_AUTHOR_GET_MESSAGE;
                m.obj = mAuthSpinner.getSelectedItem().toString();
                mThreadHandler.sendMessage(m);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        
        setTitleString(R.string.config_authorise_message);
        mThreadHandler.sendEmptyMessage(HandlerMsg.CONFIG_AUTHOR_OPERTER_ID);
        
        mNowPage = NowPage.NOWPAGE_AUTHORISE;
    }

    private void setAdapter(ListView list){
        mAdapter = new SettingAuthAdapter(CaSettingActivity.this, getTestData());
        list.setAdapter(mAdapter);
    }

    /**
     *修改密码布局初始化
     */
    private void initPasswordView(){
        setContentView(R.layout.config_modify_password_layout);
        
        mEditOld = (EditText) this.findViewById(R.id.config_modify_password_edit_old);
        mEditNew = (EditText) this.findViewById(R.id.config_modify_password_edit_new);
        mEditNewC = (EditText) this.findViewById(R.id.config_modify_password_edit_new_confirm);
        mModifySave = (Button) this.findViewById(R.id.config_modify_password_save);
        mModifyCancel = (Button) this.findViewById(R.id.config_modify_password_cancel);
        mTitleTextView = (TextView) this.findViewById(R.id.config_system_title);
        
        mModifySave.setOnClickListener(this);
        mModifyCancel.setOnClickListener(this);
        
        mEditOld.requestFocus();
        setTitleString(R.string.config_modify_password);
        
        mNowPage = NowPage.NOWPAGE_MODIFY_PASSWD;
    }

    @Override
    public void onClick(View arg0) {
        switch(arg0.getId()){
            /*
             *显示智能卡布局
             */
            case R.id.config_work_time://显示工作时段布局
                isFinished=false;
                initWorkView();
                break;
            case R.id.config_authorise_message://显示授权信息布局
                isFinished=false;
                initAuthoriseView();
                break;
            case R.id.config_modify_password://显示修改密码布局
                isFinished=false;
                initPasswordView();
                break;
            case R.id.config_watch_level://显示观看级别布局
                isFinished = false;
                initWatchTime();
                break;
            /*
             *工作时段布局相关
             */
            case R.id.config_work_time_btn_save://保存并返回
                workTimeSave();
                break;
            case R.id.config_work_time_btn_cancel://取消
                initView();
                break;
            /*
             *修改密码布局相关
             */
            case R.id.config_modify_password_save:
                passwordSave();
                break;
            case R.id.config_modify_password_cancel:
                initView();
                break;
//                mWatchSave = (Button) this.findViewById(R.id.config_watch_level_save);
//                mWatchCancel = (Button) this.findViewById(R.id.config_watch_level_cancel);
            /*
             * 观看级别布局相关
             */
            case R.id.config_watch_level_save:
                watchLevelSave();
                break;
            case R.id.config_watch_level_cancel:
                initView();
                break;
            default :
                Toast.makeText(CaSettingActivity.this, "default", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    
    private String passwd = null;
    /**
     * 工作时段保存
     */
    private void workTimeSave() {
        String sth = mSpinnerSH.getSelectedItem().toString();
        String stm = mSpinnerSM.getSelectedItem().toString();
        String seh = mSpinnerEH.getSelectedItem().toString();
        String sem = mSpinnerEM.getSelectedItem().toString();
        
        showDia(sth,stm,seh,sem);
    }
    
    private void showDia(final String sth,final String stm,final String seh,final String sem){
        final Dialog dia = new Dialog(this,R.style.config_text_dialog);
        View vi = LayoutInflater.from(CaSettingActivity.this).inflate(R.layout.config_edit_dialog,null);
        final EditText edit = (EditText) vi.findViewById(R.id.config_dialog_eidt);
        Button ok = (Button) vi.findViewById(R.id.config_dialog_btn_confirm);
        Button cancel = (Button) vi.findViewById(R.id.config_dialog_btn_cancel);
        dia.setContentView(vi);
        dia.show();
        cancel.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                dia.dismiss();
            }
        });
        ok.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                 passwd = edit.getText().toString();
                 if(passwd.length()==6){
                     Message m = new Message();
                     m.what = HandlerMsg.CONFIG_SET_WORK_TIME;
                     Bundle bun = new Bundle();
                     bun.putString("sh", sth);
                     bun.putString("sm", stm);
                     bun.putString("eh", seh);
                     bun.putString("em", sem);
                     m.setData(bun);
                     mThreadHandler.sendMessage(m);
                     dia.dismiss();
                 }else{
                     AdapterViewSelectionUtil.showToast(CaSettingActivity.this, R.string.config_midify_password_input_wrong);
                 }
            }
        });
    }

    private void watchLevelSave(){
        final String level = mWatchSpinner.getSelectedItem().toString();
        
        final Dialog dia = new Dialog(this,R.style.config_text_dialog);
        View vi = LayoutInflater.from(CaSettingActivity.this).inflate(R.layout.config_edit_dialog,null);
        final EditText edit = (EditText) vi.findViewById(R.id.config_dialog_eidt);
        Button ok = (Button) vi.findViewById(R.id.config_dialog_btn_confirm);
        Button cancel = (Button) vi.findViewById(R.id.config_dialog_btn_cancel);
        dia.setContentView(vi);
        dia.show();
        cancel.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                dia.dismiss();
            }
        });
        ok.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                 passwd = edit.getText().toString();
                 if(passwd.length()==6){
                     Message m = new Message();
                     m.what = HandlerMsg.CONFIG_WATCH_LEVEL_SET;
                     m.obj = level;
                     mThreadHandler.sendMessage(m);
                     dia.dismiss();
                 }else{
                     AdapterViewSelectionUtil.showToast(CaSettingActivity.this, R.string.config_midify_password_input_wrong);
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
        
        if(nPasswd.equals(nPasswdC)){
            if(oPasswd.length() == nPasswd.length() && nPasswd.length() == 6){
                Message m = new Message();
                m.what = HandlerMsg.CONFIG_MODIFY_PASSWORD;
                Bundle bun = new Bundle();
                bun.putString("old", oPasswd);
                bun.putString("new", nPasswdC);
                m.setData(bun);
                mThreadHandler.sendMessage(m);
            }else{
                AdapterViewSelectionUtil.showToast(this, R.string.config_midify_password_input_wrong);
            }
            
        }else{
            mEditNew.setText("");
            mEditNewC.setText("");
            AdapterViewSelectionUtil.showToast(this, R.string.config_midify_password_input_compare);
        }
    }

    @Override
    public void onBackPressed() {
        if(isFinished){
            this.finish();
        }else{
            isFinished=true;
            initView();
        }
        
    }

    /**
     * 对顶部标题字符串的变化设置
     */
    private void setTitleString(int res){
        String str = getResources().getString(R.string.config_system);
        String add = getResources().getString(res);
        mTitleTextView.setText(str+"/"+add);
    }

    private Vector<tagEntitle> v = new Vector<tagEntitle>();

    /**
     * 对授权信息整合
     * @function  test
     * @return
     */
    public ArrayList<Map<String,String>> getTestData(){
        ArrayList<Map<String,String>> mList = new ArrayList<Map<String,String>>();
        for(int j=0;j<v.size();j++){
            Map<String,String> map = new HashMap<String,String>();
            tagEntitle t = v.get(j);
            map.put("number",t.product_id+"");
            map.put("time", getDateString(t.expired_time));
            if(t.is_record){
                map.put("record", getResources().getString(R.string.config_auth_yes_record));
            }else{
                map.put("record", getResources().getString(R.string.config_auth_no_record));
            }
            
            mList.add(map);
        }
        return mList;
    }

    private String getDateString(int day){
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy"+getString(R.string.ca_charge_interval_year)+
                            "MM"+getString(R.string.ca_charge_interval_month)+"dd"+getString(R.string.ca_charge_interval_day));
        cal.set(2000, 1, 1);//从1月1号开始 算
        cal.add(Calendar.DATE, day);
        Date d = new Date();
        d = cal.getTime();
        String date = format.format(d);
        return date;
    }

    /**
     * 所有的对话框集合
     */
    private static final class DialogId{
        /** 修改密码 新密码输入错误不相同dialog */
        private static final int CONFIG_MODIFY_PASSWD_COMPARE = 0;
        /** 修改密码 旧密码不匹配错误 dialog */
        private static final int CONFIG_MODIFY_PASSWD_INPUT_WRONG = 1;
        /** 工作时段 输入出错 */
        private static final int CONFIG_WORK_TIME_INPUT_WRONG = 2;
        /** 获取本机卡号时 */
        private static final int CONFIG_MAIN_GET_CARD_NUMBER = 3;
        /** 工作时段设置 输入密码 */
        private static final int CONFIG_WORK_TIME_INPUT_PASSWARD = 4;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog builder = new Dialog(this,R.style.config_text_dialog);
        View tview = LayoutInflater.from(CaSettingActivity.this).inflate(R.layout.config_text_dialog,null);
        TextView text = (TextView) tview.findViewById(R.id.config_dialog_text);
        switch(id){
            case DialogId.CONFIG_MODIFY_PASSWD_COMPARE:
                text.setText(R.string.config_midify_password_input_compare);//新旧密码不匹配
                builder.setContentView(tview);
                return builder;
            case DialogId.CONFIG_MODIFY_PASSWD_INPUT_WRONG:
                text.setText(R.string.config_midify_password_input_wrong);//密码输入错误为6位
                builder.setContentView(tview);
                return builder;
            case DialogId.CONFIG_WORK_TIME_INPUT_WRONG:
                text.setText(R.string.config_input_wrong);//工作时段输入错误
                builder.setContentView(tview);
                return builder;
            case DialogId.CONFIG_MAIN_GET_CARD_NUMBER://正在获取本机卡号，请稍后......
                text.setText(R.string.config_getting_card_number);
                builder.setContentView(tview);
                return builder;
            case DialogId.CONFIG_WORK_TIME_INPUT_PASSWARD://请输入密码
                
                break;
             default:
                break;
        }
        return super.onCreateDialog(id);
    }

}
