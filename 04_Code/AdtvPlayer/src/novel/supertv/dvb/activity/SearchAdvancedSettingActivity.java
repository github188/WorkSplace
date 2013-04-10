
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
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * 搜索高级选项
 */
public class SearchAdvancedSettingActivity extends Activity implements OnClickListener {
    private static final DvbLog log = new DvbLog(
            "novel.supertv.dvb.activity.SearchAdvancedSettingActivity", DvbLog.DebugType.D);

    public static final int AUTOSEARCH_RESPONSECODE = 2001;
    public static final String FREQUENCY = "frequency";
    public static final String SYMBOLRATE = "symbol rate";
    public static final String MODULATION = "qam";
    public static final String SEARCHTYPE="type";
    private MyEditText mFrequencyEditText;
    private MyEditText mSymbolRateEditText;
    
    private Spinner mSpinner;

    private Button mSaveButton;
    private Button mCancleButton;
    private TuningParam mDefaultTransponder;

    private String stringExtra;

    private TextView mAutoSearchTitle;
    
    private static final int DIALOG_ALERT_DISMISS = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.auto_search_advanced);
        findViews();
        setupViews();
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

    private void setupViews() {

        mSaveButton.setOnClickListener(this);
        mCancleButton.setOnClickListener(this);

        stringExtra = getIntent().getStringExtra(SEARCHTYPE);
        if (SearchMainActivity.FULLSEARCH.equals(stringExtra)) {
            mDefaultTransponder = TransponderUtil.getDefaultTransponder(
                    this,
                    DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_MAINTP);
            mAutoSearchTitle.setText(R.string.full_search_advanced_title);
        } else {
            mDefaultTransponder = TransponderUtil.getDefaultTransponder(
                    this,
                    DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_AUTO);
        }
        mFrequencyEditText.setText("" + mDefaultTransponder.getFrequency() / 1000);
        mSymbolRateEditText.setText("" + mDefaultTransponder.getSymbolRate());

        switch (mDefaultTransponder.getModulation()) {
            case DefaultParameter.ModulationType.MODULATION_64QAM:
                mSpinner.setSelection(0);
                break;
            case DefaultParameter.ModulationType.MODULATION_128QAM:
                mSpinner.setSelection(1);
                break;
            case DefaultParameter.ModulationType.MODULATION_256QAM:
                mSpinner.setSelection(2);
                break;
            default:
                break;
        }
        
        mFrequencyEditText.setRange(
                DefaultParameter.SearchParameterRange.FREQUENCY_MIN,
                DefaultParameter.SearchParameterRange.FREQUENCY_MAX);
        mSymbolRateEditText.setRange(
                DefaultParameter.SearchParameterRange.SYMBOLRATE_MIN,
                DefaultParameter.SearchParameterRange.SYMBOLRATE_MAX);
        
        mFrequencyEditText.setOnInputDataErrorListener(new OnInputDataErrorListener() {
            
            public void onInputDataError(int errorType) {
                switch (errorType) {
                    case MyEditText.INPUT_DATA_ERROR_TYPE_NULL:
                        // 弹出对话框提示输入错误。
//                        SearchAdvancedSettingActivity.this.showDialog(DialogId.DIALOG_ALERT_FREQUENCY_NULL);
                        AdapterViewSelectionUtil.showToast(SearchAdvancedSettingActivity.this, R.string.frequency_null);
                        mFrequencyEditText.setText("" + mDefaultTransponder.getFrequency() / 1000);
                        break;
                        
                    case MyEditText.INPUT_DATA_ERROR_TYPE_OUT:
//                        SearchAdvancedSettingActivity.this.showDialog(DialogId.DIALOG_ALERT_FREQUENCY);
                        AdapterViewSelectionUtil.showToast(SearchAdvancedSettingActivity.this, R.string.frequency_out_of_range);
                        mFrequencyEditText.setText("" + mDefaultTransponder.getFrequency() / 1000);
                        break;
                        
                    case MyEditText.INPUT_DATA_ERROR_TYPE_NORMAL:
                        break;
                        
                        }
                    }
                });
        mSymbolRateEditText.setText("" + mDefaultTransponder.getSymbolRate());
        
        mSymbolRateEditText
                .setOnInputDataErrorListener(new OnInputDataErrorListener() {
                    
                    public void onInputDataError(int errorType) {
                        switch (errorType) {
                            case MyEditText.INPUT_DATA_ERROR_TYPE_NULL:
                                AdapterViewSelectionUtil.showToast(SearchAdvancedSettingActivity.this, R.string.symbol_rate_null);
                                mSymbolRateEditText.setText("" + mDefaultTransponder.getSymbolRate());
                                break;
                            case MyEditText.INPUT_DATA_ERROR_TYPE_OUT:
                                AdapterViewSelectionUtil.showToast(SearchAdvancedSettingActivity.this, R.string.symbol_rate_out_of_range);
                                mSymbolRateEditText.setText("" + mDefaultTransponder.getSymbolRate());
                                break;
                            case MyEditText.INPUT_DATA_ERROR_TYPE_NORMAL:
                                break;
                        }
                    }
                });
    }
    

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
                alertDialog.setMessage(getResources().getString(
                        R.string.frequency_out_of_range));
                break;
                
            case DialogId.DIALOG_ALERT_FREQUENCY_NULL:
                alertDialog.setMessage(getResources().getString(
                        R.string.frequency_null));
                break;
                
            /*case DialogId.DIALOG_ALERT_SYMBOL:
                alertDialog.setMessage(getResources().getString(
                        R.string.symbol_rate_out_of_range));
                break;*/
                
            case DialogId.DIALOG_ALERT_SYMBOL_OUT:
                alertDialog.setMessage(getResources().getString(
                        R.string.symbol_rate_out_range));
                break;
                
            case DialogId.DIALOG_ALERT_SYMBOL_NULL:
                alertDialog.setMessage(getResources().getString(
                        R.string.symbol_rate_null));
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
        mFrequencyEditText = (MyEditText) findViewById(R.id.frequency_edit_fast_search_advanced);
        mSymbolRateEditText = (MyEditText) findViewById(R.id.symbol_rate_edit_fast_search_advanced);
        mSaveButton = (Button) findViewById(R.id.btn_save_auto);
        mCancleButton = (Button) findViewById(R.id.btn_cancle_auto);
        mAutoSearchTitle = (TextView) findViewById(R.id.tv_auto_search_main);
        
        
        
        mSpinner = (Spinner) this.findViewById(R.id.search_adjust_method);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.adjust_method, R.layout.search_spinner_button);
        adapter.setDropDownViewResource(R.layout.search_spinner_item);
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    switch(position){
                        case 0://64
                            mDefaultTransponder.setModulation(DefaultParameter.ModulationType.MODULATION_64QAM);
                            break;
                        case 1://128
                            mDefaultTransponder.setModulation(DefaultParameter.ModulationType.MODULATION_128QAM);
                            break;
                        case 2://256
                            mDefaultTransponder.setModulation(DefaultParameter.ModulationType.MODULATION_256QAM);
                            break;
                    }
                }

                public void onNothingSelected(AdapterView<?> parent) {
                    AdapterViewSelectionUtil.showToast(SearchAdvancedSettingActivity.this, "Spinner1: unselected");
                }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_save_auto:
                String frequencyStr = mFrequencyEditText.getText().toString();
                String symbolRateStr = mSymbolRateEditText.getText().toString();
                mDefaultTransponder.setFrequency(Integer.parseInt(frequencyStr) * 1000);
                mDefaultTransponder.setSymbolRate(Integer.parseInt(symbolRateStr));
                
                // 点击保存后设置频点信息到bundle 返回搜索时带着频点数据
                Intent resultIntent = new Intent();
                Bundle mBundle = new Bundle();
                mBundle.putInt(MODULATION, mDefaultTransponder.getModulation());
                mBundle.putInt(FREQUENCY, Integer.parseInt(frequencyStr) * 1000);
                mBundle.putInt(SYMBOLRATE, Integer.parseInt(symbolRateStr));
                resultIntent.putExtras(mBundle);
                setResult(AUTOSEARCH_RESPONSECODE, resultIntent);
                
                // 保存tp信息到sp
                if (SearchMainActivity.FULLSEARCH.equals(stringExtra)) {
                    log.D("save mainTP");
                    TransponderUtil.saveDefaultTransponer(
                            this,
                            DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_MAINTP,
                            mDefaultTransponder);
                } else {
                    log.D("save autoTp");
                    TransponderUtil.saveDefaultTransponer(
                            this,
                            DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_AUTO,
                            mDefaultTransponder);
                }
                finish();
                break;
            case R.id.btn_cancle_auto:
                finish();
                break;
        }
    }
}
