package com.lenovo.settings;



import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.joysee.adtv.logic.bean.Transponder;
import com.lenovo.settings.Util.AdapterViewSelectionUtil;
import com.lenovo.settings.Util.DefaultParameter;
import com.lenovo.settings.Util.TransponderUtil;
import com.lenovo.settings.view.MyEditText;
import com.lenovo.settings.view.MyEditText.OnInputDataErrorListener;

public class SearchAdvancedSettingFragment extends Fragment implements OnClickListener {

    private View mainView;

    protected Transponder mDefaultTransponder;
    private MyEditText mFrequencyEditText;
    private MyEditText mSymbolRateEditText;
    private Button mSaveButton;
//    private Button mCancleButton;
//    private Spinner mSpinner;
    private int mCurrentSearchType;
    private TextView mSettingTitleTextView;
//	private View mFreView;
//	private View mSymView;
	private ImageView mFocusView;
	
	private LinearLayout mQamLinearLayout;
//	private TextView mLastTextView;
	private ImageView mQamImageview;
    

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mainView = inflater.inflate(R.layout.auto_search_advanced, container, false);
        initViews();
        setupViews();
        mFrequencyEditText.requestFocus();
        return mainView;
    }

    private void setupViews() {
        mSaveButton.setOnClickListener(this);
//        mCancleButton.setOnClickListener(this);
        switch (mCurrentSearchType) {
            case SearchMenuFragment.AUTOSEARCH:
                mDefaultTransponder = TransponderUtil.getTransponderFromXml(
                        getActivity(),
                        DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_AUTO);
                mSettingTitleTextView.setText(R.string.fast_search_advanced_title);
                break;
            case SearchMenuFragment.FULLSEARCH:
                mDefaultTransponder = TransponderUtil.getTransponderFromXml(
                        getActivity(),
                        DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_ALL);
                mSettingTitleTextView.setText(R.string.full_search_advanced_title);
                break;
        }
        
        mFrequencyEditText.setText("" + mDefaultTransponder.getFrequency() / 1000);
        mSymbolRateEditText.setText("" + mDefaultTransponder.getSymbolRate());

        switch (mDefaultTransponder.getModulation()) {
            case DefaultParameter.ModulationType.MODULATION_64QAM:
//                mSpinner.setSelection(0);
            	mQamTextView.setText(R.string.sixtyfour_qam);
                break;
            case DefaultParameter.ModulationType.MODULATION_128QAM:
            	mQamTextView.setText(R.string.onetwoeight_qam);
//                mSpinner.setSelection(1);
                break;
            case DefaultParameter.ModulationType.MODULATION_256QAM:
            	mQamTextView.setText(R.string.twofivesix_qam);
//                mSpinner.setSelection(2);
                break;
            default:
                break;
        }
        mFrequencyEditText.setOnFocusChangeListener(onFocusChangeListener);
        mSymbolRateEditText.setOnFocusChangeListener(onFocusChangeListener);
        
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
                        AdapterViewSelectionUtil.showToast(getActivity(), R.string.frequency_null);
                        mFrequencyEditText.setText("" + mDefaultTransponder.getFrequency() / 1000);
                        break;
                        
                    case MyEditText.INPUT_DATA_ERROR_TYPE_OUT:
                        AdapterViewSelectionUtil.showToast(getActivity(), R.string.frequency_out_of_range);
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
                                AdapterViewSelectionUtil.showToast(getActivity(), R.string.symbol_rate_null);
                                mSymbolRateEditText.setText("" + mDefaultTransponder.getSymbolRate());
                                break;
                            case MyEditText.INPUT_DATA_ERROR_TYPE_OUT:
                                AdapterViewSelectionUtil.showToast(getActivity(), R.string.symbol_rate_out_of_range);
                                mSymbolRateEditText.setText("" + mDefaultTransponder.getSymbolRate());
                                break;
                            case MyEditText.INPUT_DATA_ERROR_TYPE_NORMAL:
                                break;
                        }
                    }
                });
        
        mQamLinearLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				alert(getActivity());
			}
		});
        mQamLinearLayout.setOnFocusChangeListener(onQamLinearFocusChangeListener);
    }

    private void initViews() {
        mFrequencyEditText = (MyEditText)mainView.findViewById(R.id.frequency_edit_fast_search_advanced);
        mSymbolRateEditText = (MyEditText) mainView.findViewById(R.id.symbol_rate_edit_fast_search_advanced);
        mSettingTitleTextView = (TextView)mainView.findViewById(R.id.tv_auto_search_main);
        mSaveButton = (Button) mainView.findViewById(R.id.btn_save_auto);
        mFocusView = (ImageView) mainView.findViewById(R.id.ivFocus);
        
        mQamTextView = (TextView) mainView.findViewById(R.id.search_settings_qam_textview);
        
//        mTitleTextView = (TextView) mainView.findViewById(R.id.search_advanced_option_title);
        mQamLinearLayout = (LinearLayout) mainView.findViewById(R.id.search_settings_qam_linear);
        mQamImageview = (ImageView) mainView.findViewById(R.id.search_settings_qam_imageview);
//        mFreView = mainView.findViewById(R.id.ll_fre);
//        mSymView = mainView.findViewById(R.id.ll_sym);
        
//        mCancleButton = (Button) mainView.findViewById(R.id.btn_cancle_auto);
        
//        mSpinner = (Spinner) mainView.findViewById(R.id.search_adjust_method);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.adjust_method, R.layout.search_spinner_button);
        adapter.setDropDownViewResource(R.layout.search_spinner_item);
//        mSpinner.setAdapter(adapter);
//        mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
//                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                    switch(position){
//                        case 0://64
//                            mDefaultTransponder.setModulation(DefaultParameter.ModulationType.MODULATION_64QAM);
//                            break;
//                        case 1://128
//                            mDefaultTransponder.setModulation(DefaultParameter.ModulationType.MODULATION_128QAM);
//                            break;
//                        case 2://256
//                            mDefaultTransponder.setModulation(DefaultParameter.ModulationType.MODULATION_256QAM);
//                            break;
//                    }
//                }
//
//                public void onNothingSelected(AdapterView<?> parent) {
//                    AdapterViewSelectionUtil.showToast(getActivity(), "Spinner1: unselected");
//                }
//        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_save_auto:
                String frequencyStr = mFrequencyEditText.getText().toString();
                String symbolRateStr = mSymbolRateEditText.getText().toString();
                mDefaultTransponder.setFrequency(Integer.parseInt(frequencyStr) * 1000);
                mDefaultTransponder.setSymbolRate(Integer.parseInt(symbolRateStr));
                int mod = Integer.parseInt(mQamTextView.getText().toString());
                switch (mod) {
					case 64:
						mDefaultTransponder.setModulation(DefaultParameter.ModulationType.MODULATION_64QAM);
						break;
					case 128:
						mDefaultTransponder.setModulation(DefaultParameter.ModulationType.MODULATION_128QAM);
						break;
					case 256:
						mDefaultTransponder.setModulation(DefaultParameter.ModulationType.MODULATION_256QAM);
						break;
					default:
						break;
				}
                // 保存tp信息到sp
                Log.d("songwenxuan","mCurrentType = " + mCurrentSearchType);
                switch (mCurrentSearchType) {
                    case SearchMenuFragment.AUTOSEARCH:
                        TransponderUtil.saveTransponerToXml(
                                getActivity(),
                                DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_AUTO,
                                mDefaultTransponder);
                        break;
                    case SearchMenuFragment.FULLSEARCH:
                        TransponderUtil.saveTransponerToXml(
                                getActivity(),
                                DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_ALL,
                                mDefaultTransponder);
                        break;
                }
                // show auto searchmSettingFragment
                getActivity().onBackPressed();
//                SearchMainFragment searchMainFragment = new SearchMainFragment();
//                searchMainFragment.setSearchType(mCurrentSearchType);
//                mSettingFragment.setFragment(new SearchMainFragment(), false);
                break;
//            case R.id.btn_cancle_auto:
                // show auto searchsearchMainFragment
//                searchMainFragment = new SearchMainFragment();
//                mSettingFragment.setFragment(new SearchMainFragment(), false);
//                getActivity().onBackPressed();
//                break;
        }
    }
    
    public void setSearchType(int searchType){
        mCurrentSearchType = searchType;
    }
    private OnFocusChangeListener onFocusChangeListener = new OnFocusChangeListener() {
		
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if(hasFocus){
				EditText et = (EditText)v;
				et.setTextColor(getResources().getColor(R.color.yellow));
				mFocusView.setVisibility(View.VISIBLE);
				int [] location = new int [2];
				v.getLocationInWindow(location);
				if(location[1] == 0)
					return;
				MarginLayoutParams params = (MarginLayoutParams) mFocusView
						.getLayoutParams();
				int topOffset = (int)getResources().getDimension(R.dimen.search_option_top_offset);
				params.topMargin = location[1]-topOffset;
				Log.d("songwenxuan","params.leftMargin = " + location[0]);
				int leftOffset = (int)getResources().getDimension(R.dimen.search_option_left_offset);
				params.leftMargin = location[0] - leftOffset;
				Log.d("songwenxuan","onFocusChange() , params.topMargin = " + params.topMargin +"params.leftMargin = " + params.leftMargin);
				mFocusView.setLayoutParams(params);
			}else{
				EditText et = (EditText)v;
				et.setTextColor(getResources().getColor(R.color.white));
			}
		}
	};
	
	private TextView mTitleTextView;
	private TextView mQamTextView;
	private Dialog mAlertDialog;
	public void alert(Context context) {
		mFocusView.setVisibility(View.INVISIBLE);
//		mQamLinearLayout.setBackgroundResource(R.drawable.search_et_normal);
		mQamTextView.setTextColor(getResources().getColor(R.color.white));
//		mQamTextView.setPadding((int)getResources().getDimension(R.dimen.search_down_textview_padding), 0, 0, 0);
		mQamImageview.setImageResource(R.drawable.arrow_up_down_unfocus);
		View view = LayoutInflater.from(context).inflate(
				R.layout.search_down_list_layout, null);
//        mFocusView = (ImageView)view.findViewById(R.id.ivFocus);
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
//				if(mLastTextView != null)
//					mLastTextView.setTextColor(getResources().getColor(R.color.setting_left_item_text_focus));
//				TextView textView = (TextView)view;
//				textView.setTextColor(getResources().getColor(R.color.setting_time_title));
//				int [] location = new int [2];
//				view.getLocationInWindow(location);
//				mFocusView.setVisibility(View.VISIBLE);
//				if(location[1] == 0)
//					return;
//				MarginLayoutParams params = (MarginLayoutParams) mFocusView.getLayoutParams();
//				params.topMargin = location[1];
//				
//				Log.d("songwenxuan","onFocusChange() , params.topMargin = " + params.topMargin);
//				mFocusView.setLayoutParams(params);
				
//				Animation anim = new AlphaAnimation(0.0f, 1.0f);
//				anim.setDuration(300);
//				anim.setFillAfter(true);
//				anim.setFillEnabled(true);
//				mFocusView.startAnimation(anim);
//				mLastTextView = textView;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		});
		
		downListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
//				mQamLinearLayout.setBackgroundResource(R.drawable.search_et_selector);
//				mQamTextView.setPadding((int)getResources().getDimension(R.dimen.search_down_textview_padding), 0, 0, 0);
				mQamTextView.setTextColor(getResources().getColor(R.color.yellow));
				mQamImageview.setImageResource(R.drawable.arrow_up_down_focus);
				switch (position) {
				case 0:
					mQamTextView.setText(R.string.sixtyfour_qam);
					mAlertDialog.dismiss();
					mFocusView.setVisibility(View.VISIBLE);
					break;
				case 1:
					mQamTextView.setText(R.string.onetwoeight_qam);
					mAlertDialog.dismiss();
					mFocusView.setVisibility(View.VISIBLE);
					break;
				case 2:
					mQamTextView.setText(R.string.twofivesix_qam);
					mAlertDialog.dismiss();
					mFocusView.setVisibility(View.VISIBLE);
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
		int xOffset = (int)getResources().getDimension(R.dimen.search_down_xoffset);
		params.x = location[0] - x + params.width/2 - xOffset;
		params.y = location[1] + height -y + params.height/2;
		window.setAttributes(params);
		mAlertDialog.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if(event.getKeyCode() == KeyEvent.KEYCODE_ESCAPE || event.getKeyCode() == KeyEvent.KEYCODE_BACK){
					mQamTextView.setTextColor(getResources().getColor(R.color.yellow));
					mQamImageview.setImageResource(R.drawable.arrow_up_down_focus);
//					mQamTextView.setPadding((int)getResources().getDimension(R.dimen.search_down_textview_padding), 0, 0, 0);
					mAlertDialog.dismiss();
					mFocusView.setVisibility(View.VISIBLE);
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
				ImageView imageView = (ImageView) v.findViewById(R.id.search_settings_qam_imageview);
				textview.setTextColor(getResources().getColor(R.color.yellow));
				imageView.setImageResource(R.drawable.arrow_up_down_focus);
				mFocusView.setVisibility(View.VISIBLE);
				int [] location = new int [2];
				v.getLocationInWindow(location);
				if(location[1] == 0)
					return;
				MarginLayoutParams params = (MarginLayoutParams) mFocusView
						.getLayoutParams();
				int topOffset = (int)getResources().getDimension(R.dimen.search_option_top_offset);
				params.topMargin = location[1]-topOffset;
				Log.d("songwenxuan","params.leftMargin = " + location[0]);
				int leftOffset = (int)getResources().getDimension(R.dimen.search_option_left_offset);
				params.leftMargin = location[0] - leftOffset;
				Log.d("songwenxuan","onFocusChange() , params.topMargin = " + params.topMargin +"params.leftMargin = " + params.leftMargin);
				mFocusView.setLayoutParams(params);
			}else{
				mFocusView.setVisibility(View.INVISIBLE);
				TextView textview = (TextView) v.findViewById(R.id.search_settings_qam_textview);
				ImageView imageView = (ImageView) v.findViewById(R.id.search_settings_qam_imageview);
				textview.setTextColor(getResources().getColor(R.color.white));
				imageView.setImageResource(R.drawable.arrow_up_down_unfocus);
			}
		}
	};
    
}
