
package com.joysee.adtv.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DefaultParameter;
import com.joysee.adtv.common.DvbLog;
import com.joysee.adtv.common.ToastUtil;
import com.joysee.adtv.common.TransponderUtil;
import com.joysee.adtv.logic.bean.Transponder;
import com.joysee.adtv.ui.SearchEditText;
import com.joysee.adtv.ui.SearchEditText.OnInputDataErrorListener;

/**
 * 搜索高级选项 用于设置搜索的频率，符号率，调制。
 * 
 * @author songwenxuan
 */
public class SearchAdvancedOptionActivity extends Activity implements OnClickListener {
    private static final DvbLog log = new DvbLog(
            "com.joysee.adtv.activity.SearchAdvancedOptionActivity", DvbLog.DebugType.D);
    public static final int AUTOSEARCH_RESPONSECODE = 2001;
    public static final String FREQUENCY = "frequency";
    public static final String SYMBOLRATE = "symbol rate";
    public static final String MODULATION = "qam";
    public static final String SEARCHTYPE = "type";
    private SearchEditText mFrequencyEditText;
    private SearchEditText mSymbolRateEditText;
    private Dialog mAlertDialog;
    private Button mSaveButton;
    private Transponder mTransponder;
    private String mStringExtra;
    private TextView mTitleTextView;
	private TextView mQamTextView;
	private ImageView mFocusView;
	private LinearLayout mQamLinearLayout;
	private TextView mLastTextView;
	private ImageView mQamImageview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.search_advanced_option_layout);
        LinearLayout auto_search_advance_bg = (LinearLayout) findViewById(R.id.search_advanced_option_ll);
        if (getThemePaper() != null) {
            auto_search_advance_bg.setBackgroundDrawable(getThemePaper());
        }
        findViews();
        setupViews();
    }

    private void setupViews() {

        mSaveButton.setOnClickListener(this);
        mStringExtra = getIntent().getStringExtra(SEARCHTYPE);
        if (SearchMainActivity.ALL_SEARCH.equals(mStringExtra)) {
            mTransponder = TransponderUtil.getTransponderFromXml(
                    this,
                    DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_ALL);
            mTitleTextView.setText(R.string.search_full_search_advanced_title);
        } else {
            mTransponder = TransponderUtil.getTransponderFromXml(
                    this,
                    DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_AUTO);
        }
        mFrequencyEditText.setText("" + mTransponder.getFrequency() / 1000);
        mSymbolRateEditText.setText("" + mTransponder.getSymbolRate());

        switch (mTransponder.getModulation()) {
            case DefaultParameter.ModulationType.MODULATION_64QAM:
            	mQamTextView.setText(R.string.search_64);
                break;
            case DefaultParameter.ModulationType.MODULATION_128QAM:
            	mQamTextView.setText(R.string.search_128);
                break;
            case DefaultParameter.ModulationType.MODULATION_256QAM:
            	mQamTextView.setText(R.string.search_256);
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
                    case SearchEditText.INPUT_DATA_ERROR_TYPE_NULL:
                        ToastUtil.showToast(SearchAdvancedOptionActivity.this,
                                R.string.search_frequency_null);
                        mFrequencyEditText.setText("" + mTransponder.getFrequency() / 1000);
                        break;

                    case SearchEditText.INPUT_DATA_ERROR_TYPE_OUT:
                        ToastUtil.showToast(SearchAdvancedOptionActivity.this,
                                R.string.search_frequency_out);
                        mFrequencyEditText.setText("" + mTransponder.getFrequency() / 1000);
                        break;

                    case SearchEditText.INPUT_DATA_ERROR_TYPE_NORMAL:
                        break;

                }
            }
        });
        mSymbolRateEditText.setText("" + mTransponder.getSymbolRate());
        mSymbolRateEditText.setOnInputDataErrorListener(
                new OnInputDataErrorListener() {
                    public void onInputDataError(int errorType) {
                        switch (errorType) {
                            case SearchEditText.INPUT_DATA_ERROR_TYPE_NULL:
                                ToastUtil.showToast(SearchAdvancedOptionActivity.this,
                                        R.string.search_symbolrate_null);
                                mSymbolRateEditText.setText(""
                                        + mTransponder.getSymbolRate());
                                break;
                            case SearchEditText.INPUT_DATA_ERROR_TYPE_OUT:
                                ToastUtil.showToast(SearchAdvancedOptionActivity.this,
                                        R.string.search_symbolrate_out);
                                mSymbolRateEditText.setText(""
                                        + mTransponder.getSymbolRate());
                                break;
                            case SearchEditText.INPUT_DATA_ERROR_TYPE_NORMAL:
                                break;
                        }
                    }
                });
        
        mQamLinearLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				alert(SearchAdvancedOptionActivity.this);
			}
		});
        mQamLinearLayout.setOnFocusChangeListener(onQamLinearFocusChangeListener);
    }

    private void findViews() {
        mFrequencyEditText = (SearchEditText) findViewById(R.id.search_advanced_option_frequency);
        mSymbolRateEditText = (SearchEditText) findViewById(R.id.search_advanced_option_symbolrate);
        mSaveButton = (Button) findViewById(R.id.search_advanced_option_save_button);
        mQamTextView = (TextView) findViewById(R.id.search_settings_qam_textview);
        
        mTitleTextView = (TextView) findViewById(R.id.search_advanced_option_title);
        mQamLinearLayout = (LinearLayout) findViewById(R.id.search_settings_qam_linear);
        mQamImageview = (ImageView) findViewById(R.id.search_settings_qam_imageview);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.search_advanced_option_save_button:
        	
            String frequencyStr = mFrequencyEditText.getText().toString();
            String symbolRateStr = mSymbolRateEditText.getText().toString();
            String modulationStr = mQamTextView.getText().toString();
            log.D("frequencyStr = " + frequencyStr + "    symbolRateStr = " + symbolRateStr + "modulationStr = " + modulationStr);
            
            mTransponder.setFrequency(Integer.parseInt(frequencyStr) * 1000);
            mTransponder.setSymbolRate(Integer.parseInt(symbolRateStr));
            
            switch (Integer.parseInt(modulationStr)) {
			case 64:
				mTransponder.setModulation(DefaultParameter.ModulationType.MODULATION_64QAM);
				break;
			case 128:
				mTransponder.setModulation(DefaultParameter.ModulationType.MODULATION_128QAM);
				break;
			case 256:
				mTransponder.setModulation(DefaultParameter.ModulationType.MODULATION_256QAM);
				break;
			default:
				break;
			}
            
            log.D("frequency  = " + mTransponder.getFrequency() + "symbolRateStr = " + mTransponder.getSymbolRate());
            log.D("mStringExtra = " + mStringExtra);
            // 保存tp信息到sp
            if (SearchMainActivity.ALL_SEARCH.equals(mStringExtra)) {
                log.D("save all search transponder");
                TransponderUtil.saveTransponerToXml(
                                this,
                                DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_ALL,
                                mTransponder);
            } else {
                log.D("save fast search transponder");
                TransponderUtil.saveTransponerToXml(
                        this,
                        DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_AUTO,
                        mTransponder);
            }
            finish();
            break;
            default:
            	break;
        }
    }

    /**
     * 取setting背景图，用于设置成这个activity的背景
     * 
     * @return
     */
    public Drawable getThemePaper() {
        String url = Settings.System.getString(this.getContentResolver(), "settings.theme.url");
        if (url != null && url.length() > 0) {
            Bitmap bitmap = BitmapFactory.decodeFile(url);
            Drawable drawable = new BitmapDrawable(bitmap);
            return drawable;
        }
        return null;
    }

	public void alert(Context context) {
		mQamLinearLayout.setBackgroundResource(R.drawable.search_et_normal);
		mQamTextView.setTextColor(getResources().getColor(R.color.search_main_text));
		mQamTextView.setPadding((int)getResources().getDimension(R.dimen.search_down_textview_padding), 0, 0, 0);
		mQamImageview.setImageResource(R.drawable.search_settings_arrows_unfocus);
		View view = LayoutInflater.from(context).inflate(
				R.layout.search_down_list_layout, null);
        mFocusView = (ImageView)view.findViewById(R.id.ivFocus);
		ListView downListView = (ListView) view.findViewById(R.id.search_down_listview);
		Integer[] qams = {64,128,256};
		ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(context, R.layout.search_down_list_item, R.id.search_down_list_textview, qams);
		downListView.setAdapter(adapter);
		if (mAlertDialog == null) {
			mAlertDialog = new Dialog(context, R.style.searchDownListTheme);
		}
		
		downListView.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if(mLastTextView != null)
					mLastTextView.setTextColor(getResources().getColor(R.color.search_main_text));
				TextView textView = (TextView)view;
				textView.setTextColor(getResources().getColor(R.color.search_text_green));
				int [] location = new int [2];
				view.getLocationInWindow(location);
				mFocusView.setVisibility(View.VISIBLE);
				if(location[1] == 0)
					return;
				MarginLayoutParams params = (MarginLayoutParams) mFocusView.getLayoutParams();
				params.topMargin = location[1];
				
				Log.d("songwenxuan","onFocusChange() , params.topMargin = " + params.topMargin);
				mFocusView.setLayoutParams(params);
				
				Animation anim = new AlphaAnimation(0.0f, 1.0f);
				anim.setDuration(300);
				anim.setFillAfter(true);
				anim.setFillEnabled(true);
				mFocusView.startAnimation(anim);
				mLastTextView = textView;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		});
		
		downListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mQamLinearLayout.setBackgroundResource(R.drawable.search_et_selector);
				mQamTextView.setPadding((int)getResources().getDimension(R.dimen.search_down_textview_padding), 0, 0, 0);
				mQamTextView.setTextColor(getResources().getColor(R.color.search_text_green));
				mQamImageview.setImageResource(R.drawable.search_settings_arrows_focus);
				switch (position) {
				case 0:
					mQamTextView.setText(R.string.search_64);
					mAlertDialog.dismiss();
					break;
				case 1:
					mQamTextView.setText(R.string.search_128);
					mAlertDialog.dismiss();
					break;
				case 2:
					mQamTextView.setText(R.string.search_256);
					mAlertDialog.dismiss();
					break;
				default:
					break;
				}
			}
		});
		
		mAlertDialog.setContentView(view);
		Window window = mAlertDialog.getWindow();
		LayoutParams params = new LayoutParams();
		int [] location = new int [2]; 
		mQamTextView.getLocationInWindow(location);
		int height = mQamTextView.getHeight();
		
		Log.d("songwenxuan","location[0] = " + location[0] + "  location[1] = " + location[1]);
		Log.d("songwenxuan","height = " + height);
		
//		Display display = getWindowManager().getDefaultDisplay();
		//dialog的零点
		int x = (int)getResources().getDimension(R.dimen.screen_width)/2;
		int y = (int)getResources().getDimension(R.dimen.screen_height)/2;
		params.width = mQamLinearLayout.getWidth();
		params.height = (int)getResources().getDimension(R.dimen.search_down_list_height);
		params.dimAmount = 0.4f;
		params.flags = LayoutParams.FLAG_DIM_BEHIND;
		params.x = location[0] - x + params.width/2;
		params.y = location[1] + height -y + params.height/2;
		window.setAttributes(params);
		mAlertDialog.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if(event.getKeyCode() == KeyEvent.KEYCODE_ESCAPE || event.getKeyCode() == KeyEvent.KEYCODE_BACK){
					mQamLinearLayout.setBackgroundResource(R.drawable.search_et_selector);
					mQamTextView.setTextColor(getResources().getColor(R.color.search_text_green));
					mQamImageview.setImageResource(R.drawable.search_settings_arrows_focus);
					mQamTextView.setPadding((int)getResources().getDimension(R.dimen.search_down_textview_padding), 0, 0, 0);
					mAlertDialog.dismiss();
					return true;
				}
				return false;
			}
		});
		mAlertDialog.show();
	}
	
	OnFocusChangeListener onQamLinearFocusChangeListener = new OnFocusChangeListener() {
		
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if(hasFocus){
				TextView textview = (TextView) v.findViewById(R.id.search_settings_qam_textview);
				ImageView imageView = (ImageView) findViewById(R.id.search_settings_qam_imageview);
				textview.setTextColor(getResources().getColor(R.color.search_text_green));
				imageView.setImageResource(R.drawable.search_settings_arrows_focus);
			}else{
				TextView textview = (TextView) v.findViewById(R.id.search_settings_qam_textview);
				ImageView imageView = (ImageView) findViewById(R.id.search_settings_qam_imageview);
				textview.setTextColor(getResources().getColor(R.color.search_main_text));
				imageView.setImageResource(R.drawable.search_settings_arrows_unfocus);
			}
		}
	};

}
