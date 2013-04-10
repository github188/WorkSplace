
package novel.supertv.dvb.activity;

import novel.supertv.dvb.R;
import novel.supertv.dvb.jni.struct.TuningParam;
import novel.supertv.dvb.utils.AdapterViewSelectionUtil;
import novel.supertv.dvb.utils.DefaultParameter;
import novel.supertv.dvb.utils.DvbLog;
import novel.supertv.dvb.utils.TransponderUtil;
import novel.supertv.dvb.view.MyEditText;
import novel.supertv.dvb.view.MyEditText.OnInputDataErrorListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * 手动搜索界面
 */
public class SearchManualActivity extends Activity implements OnClickListener {
    
    private static final DvbLog log = new DvbLog(
            "novel.supertv.dvb.activity.SearchManualActivity", DvbLog.DebugType.D);
    
    public static final String MANUALSEARCH = "search bundle";
    public static final String MODULATION = "maduation manual";
    public static final String FREQUENCY = "frequency manual";
    public static final String SYMBOLRATE = "symbol rate manual";
    protected static final int DIALOG_ALERT_DISMISS = 11;
    private MyEditText mFrequencyEditText;
    private MyEditText mSymbolRateEditText;
//    private ImageView mSixteenImage;
//    private ImageView mThirthtytwoImage;
    private ImageView mSixtyfourImage;
    private ImageView mOneTwoEightImage;
    private ImageView mTwoFiveSixImage;
//    private LinearLayout mSixteenText;
//    private LinearLayout mThirthtytwoText;
    private LinearLayout mSixtyfourLinearLayout;
    private LinearLayout mOneTwoEightLinearLayout;
    private LinearLayout mTwoFiveSixLinearLayout;
    private Button mStartSearchButton;
    private Button mCancelButton;
    private TuningParam mDefaultTransponder;
    private boolean isManualSearch = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.manual_search);
        findViews();
        setupViews();
    }

    private void setupViews() {
//        mSixteenText.setOnClickListener(this);
//        mThirthtytwoText.setOnClickListener(this);
        mSixtyfourLinearLayout.setOnClickListener(this);
        mOneTwoEightLinearLayout.setOnClickListener(this);
        mTwoFiveSixLinearLayout.setOnClickListener(this);
        mStartSearchButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        mDefaultTransponder = TransponderUtil.getDefaultTransponder(
                this,
                DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_MANUAL);
        mFrequencyEditText.setText("" + mDefaultTransponder.getFrequency() / 1000);
        mSymbolRateEditText.setText("" + mDefaultTransponder.getSymbolRate());
        switch (mDefaultTransponder.getModulation()) {
            /*case 0:
                mSixteenImage.setVisibility(View.VISIBLE);
                break;
            case 1:
                mThirthtytwoImage.setVisibility(View.VISIBLE);
                break;*/
            case DefaultParameter.ModulationType.MODULATION_64QAM:
                mSixtyfourImage.setVisibility(View.VISIBLE);
                break;
            case DefaultParameter.ModulationType.MODULATION_128QAM:
                mOneTwoEightImage.setVisibility(View.VISIBLE);
                break;
            case DefaultParameter.ModulationType.MODULATION_256QAM:
                mTwoFiveSixImage.setVisibility(View.VISIBLE);
                break;
        }
//        mFrequencyEditText.setRange(
//                DefaultParameter.SearchParameterRange.FREQUENCY_MIN,
//                DefaultParameter.SearchParameterRange.FREQUENCY_MAX);
        mFrequencyEditText.setOnInputDataErrorListener(new OnInputDataErrorListener() {
            
            public void onInputDataError(int errorType) {
                switch (errorType) {
                    case MyEditText.INPUT_DATA_ERROR_TYPE_NULL:
                        // 弹出对话框提示输入错误。
//                        SearchManualActivity.this.showDialog(DialogId.DIALOG_ALERT_FREQUENCY_NULL);
                        AdapterViewSelectionUtil.showToast(SearchManualActivity.this, R.string.frequency_null);
                        mFrequencyEditText.setText("" + mDefaultTransponder.getFrequency() / 1000);
                        break;
                    case MyEditText.INPUT_DATA_ERROR_TYPE_OUT:
//                        SearchManualActivity.this.showDialog(DialogId.DIALOG_ALERT_FREQUENCY);
//                        mFrequencyEditText.setText("" + mDefaultTransponder.getFrequency() / 1000);
                        break;
                    case MyEditText.INPUT_DATA_ERROR_TYPE_NORMAL:
                        break;
                }
            }
        });
        
        mSymbolRateEditText.setText("" + mDefaultTransponder.getSymbolRate());
        
        mSymbolRateEditText.setOnInputDataErrorListener(new OnInputDataErrorListener() {
            
            public void onInputDataError(int errorType) {
                switch (errorType) {
                    case MyEditText.INPUT_DATA_ERROR_TYPE_NULL:
//                        SearchManualActivity.this.showDialog(DialogId.DIALOG_ALERT_SYMBOL_NULL);
                        AdapterViewSelectionUtil.showToast(SearchManualActivity.this, R.string.symbol_rate_null);
                        mSymbolRateEditText.setText("" + mDefaultTransponder.getSymbolRate());
                        break;
                    case MyEditText.INPUT_DATA_ERROR_TYPE_OUT:
//                        SearchManualActivity.this.showDialog(DialogId.DIALOG_ALERT_SYMBOL);
//                        mSymbolRateEditText.setText("" + mDefaultTransponder.getSymbolRate());
                        break;
                    case MyEditText.INPUT_DATA_ERROR_TYPE_NORMAL:
                        break;
                }
            }
        });
        mTwoFiveSixLinearLayout.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction()==KeyEvent.ACTION_DOWN&&keyCode==KeyEvent.KEYCODE_DPAD_DOWN){
                    mStartSearchButton.requestFocus();
                    return true;
                }
                return false;
            }
        });
    }
    
    /**处理对话框消失*/
    private Handler mainHandler = new Handler(){
        
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case DIALOG_ALERT_DISMISS:
                    log.D("MainHandlerMsg.DIALOG_ALERT_DISMISS dialog id = "+msg.arg1);
                    dismissDialog(msg.arg1);
                    break;
                }
                
                super.handleMessage(msg);
            }
        };

    /**
     * 所有的对话框集合
     */
    private static final class DialogId {
        private static final int DIALOG_ALERT_FREQUENCY = 0;
        private static final int DIALOG_ALERT_SYMBOL = 1;
        private static final int DIALOG_ALERT_FREQUENCY_NULL = 2;
        private static final int DIALOG_ALERT_SYMBOL_NULL = 3;
        public static final int DIALOG_ALERT_SYMBOL_OUT = 4;
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DialogId.DIALOG_ALERT_FREQUENCY:
            case DialogId.DIALOG_ALERT_FREQUENCY_NULL:
            case DialogId.DIALOG_ALERT_SYMBOL:
            case DialogId.DIALOG_ALERT_SYMBOL_NULL:
            case DialogId.DIALOG_ALERT_SYMBOL_OUT:
                
                AlertDialog alert = new AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_DARK).create();
                alert.setTitle(getResources().getString(R.string.alert));
                alert.setOnKeyListener(new OnKeyListener() {
                    public boolean onKey(DialogInterface arg0, int arg1,
                            KeyEvent event) {
                        // 任意按键都能让提示对话框消失
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            arg0.dismiss();
                        }
                        return true;
                    }
                });
                // 这个是用来在组装对话框时必须的，用来显示消息内容
                // 可以在onPrepareDialog中改变的，如果这里没有那么
                // 对话框就没这个组件了
                alert.setMessage("");
                return alert;
        }
        return super.onCreateDialog(id);
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        AlertDialog alertDialog = (AlertDialog) dialog;
        switch (id) {
            case DialogId.DIALOG_ALERT_FREQUENCY:
                alertDialog.setMessage(getResources().getString(R.string.frequency_out_of_range));
                break;
            case DialogId.DIALOG_ALERT_FREQUENCY_NULL:
                alertDialog.setMessage(getResources().getString(R.string.frequency_null));
                break;
            /*case DialogId.DIALOG_ALERT_SYMBOL:
                alertDialog.setMessage(getResources().getString(R.string.symbol_rate_out_of_range));
                break;*/
            case DialogId.DIALOG_ALERT_SYMBOL_NULL:
                alertDialog.setMessage(getResources().getString(R.string.symbol_rate_null));
                break;
            case DialogId.DIALOG_ALERT_SYMBOL_OUT:
                alertDialog.setMessage(getResources().getString(
                        R.string.symbol_rate_out_range));
                break;
        }
        Message msg = new Message();
        msg.what = DIALOG_ALERT_DISMISS;
        msg.arg1 = id;
        mainHandler.sendMessageDelayed(msg, 3000);
        log.D("message: dismiss dialog"+msg.arg1);
        super.onPrepareDialog(id, dialog);
    }

    private void findViews() {
        
        mFrequencyEditText = (MyEditText) findViewById(R.id.frequency_edit);
        mSymbolRateEditText = (MyEditText) findViewById(R.id.symbol_rate_edit);
        
//        mSixteenImage = (ImageView) findViewById(R.id.sixteen_qam_manual_iv);
//        mThirthtytwoImage = (ImageView) findViewById(R.id.thirthtytwo_qam_manual_iv);
        mSixtyfourImage = (ImageView) findViewById(R.id.sixtyfour_qam_manual_iv);
        mOneTwoEightImage = (ImageView) findViewById(R.id.onetwoeight_qam_manual_iv);
        mTwoFiveSixImage = (ImageView) findViewById(R.id.twofivesix_qam_manual_iv);
        
//        mSixteenText = (LinearLayout) findViewById(R.id.sixteen_qam_manual_tv);
//        mThirthtytwoText = (LinearLayout) findViewById(R.id.thirthtytwo_qam_manual_tv);
        mSixtyfourLinearLayout = (LinearLayout) findViewById(R.id.sixtyfour_qam_manual_tv);
        mOneTwoEightLinearLayout = (LinearLayout) findViewById(R.id.onetwoeight_qam_manual_tv);
        mTwoFiveSixLinearLayout = (LinearLayout) findViewById(R.id.twofivesix_qam_manual_tv);
        
        mStartSearchButton = (Button) findViewById(R.id.btn_search_manual);
        mCancelButton = (Button) findViewById(R.id.btn_cancle_manual);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            /*case R.id.sixteen_qam_manual_tv:
                mSixteenImage.setVisibility(View.VISIBLE);
                mThirthtytwoImage.setVisibility(View.INVISIBLE);
                mSixtyfourImage.setVisibility(View.INVISIBLE);
                mOneTwoEightImage.setVisibility(View.INVISIBLE);
                mTwoFiveSixImage.setVisibility(View.INVISIBLE);
                mDefaultTransponder.setModulation(0);
                break;
                
            case R.id.thirthtytwo_qam_manual_tv:
                mSixteenImage.setVisibility(View.INVISIBLE);
                mThirthtytwoImage.setVisibility(View.VISIBLE);
                mSixtyfourImage.setVisibility(View.INVISIBLE);
                mOneTwoEightImage.setVisibility(View.INVISIBLE);
                mTwoFiveSixImage.setVisibility(View.INVISIBLE);
                mDefaultTransponder.setModulation(1);
                break;*/
                
            case R.id.sixtyfour_qam_manual_tv:
//                mSixteenImage.setVisibility(View.INVISIBLE);
//                mThirthtytwoImage.setVisibility(View.INVISIBLE);
                mSixtyfourImage.setVisibility(View.VISIBLE);
                mOneTwoEightImage.setVisibility(View.INVISIBLE);
                mTwoFiveSixImage.setVisibility(View.INVISIBLE);
                mDefaultTransponder.setModulation(DefaultParameter.ModulationType.MODULATION_64QAM);
                break;
                
            case R.id.onetwoeight_qam_manual_tv:
//                mSixteenImage.setVisibility(View.INVISIBLE);
//                mThirthtytwoImage.setVisibility(View.INVISIBLE);
                mSixtyfourImage.setVisibility(View.INVISIBLE);
                mOneTwoEightImage.setVisibility(View.VISIBLE);
                mTwoFiveSixImage.setVisibility(View.INVISIBLE);
                mDefaultTransponder.setModulation(DefaultParameter.ModulationType.MODULATION_128QAM);
                break;
                
            case R.id.twofivesix_qam_manual_tv:
//                mSixteenImage.setVisibility(View.INVISIBLE);
//                mThirthtytwoImage.setVisibility(View.INVISIBLE);
                mSixtyfourImage.setVisibility(View.INVISIBLE);
                mOneTwoEightImage.setVisibility(View.INVISIBLE);
                mTwoFiveSixImage.setVisibility(View.VISIBLE);
                mDefaultTransponder.setModulation(DefaultParameter.ModulationType.MODULATION_256QAM);
                break;
                
            case R.id.btn_search_manual:
                String frequency = mFrequencyEditText.getText().toString();
                String symbolRate = mSymbolRateEditText.getText().toString();
                
                Bundle bundle = new Bundle();
                bundle.putInt(FREQUENCY, Integer.parseInt(frequency) * 1000);
                bundle.putInt(SYMBOLRATE, Integer.parseInt(symbolRate));
                bundle.putInt(MODULATION, mDefaultTransponder.getModulation());
                bundle.putBoolean("isManual", isManualSearch);
                mDefaultTransponder.setFrequency(Integer.parseInt(frequency) * 1000);
                mDefaultTransponder.setSymbolRate(Integer.parseInt(symbolRate));
                TransponderUtil.saveDefaultTransponer(
                        this,
                        DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_MANUAL,
                        mDefaultTransponder);
                
                Intent autoSearchIntent = new Intent(this, SearchHandActivity.class);
                autoSearchIntent.putExtra(MANUALSEARCH, bundle);
                startActivity(autoSearchIntent);
                break;
                
            case R.id.btn_cancle_manual:
                finish();
                break;
        }
    }
}
