package com.lenovo.settings;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.*;  

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.os.SystemClock;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;

public class DateSettings extends Fragment implements
		/*TimePickerDialog.OnTimeSetListener,*/ DatePickerDialog.OnDateSetListener{
	public static final String TAG = "DateSettings";
	private static final int MSG_TIMER = 1000;
	private static final int TIME_FORMAT = 1001;
//	private static final String mUrl = "http://lily.tvxio.com/media/geoid.json";
//	private static final String BROADCAST_CITY_ID_ACTION = "com.lenovo.CITYID";
//	private static final String BROADCAST_HAVE_CITY_ACTION = "com.lenovo.HAVECITY";
	//private static final int HAVE_CITY_DATA = 1;
//	private Button dateyes,dateno;
	private View mView;
	private TextView /*timeSyncSpinner, */timeHourSpinner,/* timeFormatSpinner,
			citySettingSpinner,*//*time_format_12,time_format_24,*/mTextHour,mTextMin,mTextSecond,mTextYear,mTextMonth,mTextDay/*mTextTimeFormat,*/
			/*mTextTimePoint,mTextLine1,mTextLine2*/;
	ArrayAdapter<CharSequence> adapter,mCityAdapter;

    private static final int DIALOG_DATEPICKER = 0;
    private static final int DIALOG_TIMEPICKER = 1;
    private static final int VAR_INC = 0;
    private static final int VAR_DEC = 1;
//	protected static final String STR_CITY_NAME = "persist.sys.cityname";
//	protected static final String STR_CITY_ID = "persist.sys.cityid";

//    public static final Uri CONTENT_URI_SYS = CityProvider.Citys.CONTENT_URI;

//	private ArrayList<Map<String,Object>> mCityData = new ArrayList<Map<String,Object>>();
//	ArrayList<String> mCitys = new ArrayList();
	Calendar now;
//	private AutoCompleteTextView autoCompleteCity;
//	private String mCityId;
//	private String mCityName;
//	protected CityDataService mService = null;
	protected boolean mBound = false;
	private IntentFilter mFilter;
	private BroadcastReceiver mReceiver;
//	private EditText mCityEdit;
	private ListView mDropdownList;
	private ArrayList<String> mArray = new ArrayList<String>();
	private ArrayAdapter<String> mAdapter;
	protected boolean mEnableEditor = false;
	protected boolean mListEnd = false;
	private int mArraySize;
	private RelativeLayout mListLayout;
	private DropdownMenu mDropMenu;
//	private int mIndexSync;
//	private String[] mSyncArrays;
	private int mIndexTime;
	private String[] mTimeArrays;
	private String[] mDateArrays;
	private int mIndexDate;
	//private RelativeLayout mHourLayout,mDateLayout;
	private RelativeLayout mYearLayout,mMonthLayout,mDayLayout,mHourLayout,mMiniteLayout,mSecondLayout;
	private ArrayList<Integer> mArrayYear = new ArrayList<Integer>();
	private ArrayList<Integer> mArrayMonth = new ArrayList<Integer>();
	private ArrayList<Integer> mArrayDay = new ArrayList<Integer>();
	private ArrayList<Integer> mArrayHour = new ArrayList<Integer>();
	private ArrayList<Integer> mArrayMinite = new ArrayList<Integer>();
	private ArrayList<Integer> mArraySecond = new ArrayList<Integer>();
	protected int mHours,mMinutes,mSeconds,mYear,mMonth,mDay;
	protected boolean mEnableHourFocus = false;
	protected boolean mEnableMiniteFocus = false;
	protected boolean mEnableSecondFocus = false;
	protected boolean mEnableYearFocus = false;
	protected boolean mEnableMonthFocus = false;
	protected boolean mEnableDayFocus = false;
	
	Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
	
		    Calendar time = Calendar.getInstance();
		    final Activity activity = getActivity();
			int color;//选中字后的color
			switch(msg.what){
			case MSG_TIMER:
	            if (activity != null) {
	                updataTimeOfSeconds(activity);
	            }
				mHandler.sendEmptyMessageDelayed(MSG_TIMER, 1000);
				break;
			case TIME_FORMAT:
				updateTimeAndDateDisplay(activity);
				break;
			case DropdownMenu.DROPDOWN_MENU_ITEM_CHICK:
				if(msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_CAN_EDIT){
					EditText edit = (EditText) msg.obj;
				}else if(msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_NOT_EDIT){
					TextView text = (TextView) msg.obj;
					int position = msg.arg2;
					if(text == timeHourSpinner){
						if (getIndex(is24Hour()) != position){
							set24Hour(position);
						}
						updateTimeAndDateDisplay(getActivity());
					}else{
						break;
					}
				}
				break;
			case DropdownMenu.DROPDOWN_MENU_GET_ADAPTER:
				if(msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_CAN_EDIT){
					EditText edit = (EditText) msg.obj;
				}else if(msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_NOT_EDIT){
					TextView text = (TextView) msg.obj;
					if(text == timeHourSpinner){
						mDropMenu.setListViewPosition(0, 2);
						mDropMenu.setListViewAdapter(mTimeArrays, mIndexTime);						
					}
					mDropMenu.showDropdownListEnable(true);
				}
				break;
            case TimeDateUtils.KEY_DOWN:
				if((Integer)msg.obj == R.id.hour) {
					mHours = updateVar(mHours,time.getActualMinimum(Calendar.HOUR_OF_DAY),
					    time.getActualMaximum(Calendar.HOUR_OF_DAY),VAR_DEC);
					System.out.println("mHours==updown="+mHours);
					setTime(mHours,mMinutes,mSeconds);
				} else if((Integer)msg.obj == R.id.minite) {
					mMinutes = updateVar(mMinutes,time.getActualMinimum(Calendar.MINUTE),
				    		time.getActualMaximum(Calendar.MINUTE),VAR_DEC);
					setTime(mHours,mMinutes,mSeconds);
				} else if((Integer)msg.obj == R.id.second) {
					mSeconds = updateVar(mSeconds,time.getActualMinimum(Calendar.SECOND),
				    		time.getActualMaximum(Calendar.SECOND),VAR_DEC);
					setTime(mHours,mMinutes,mSeconds);
				} 
				else if((Integer)msg.obj == R.id.year || (Integer)msg.obj == R.id.month
						|| (Integer)msg.obj == R.id.data){
					updateDateValue((Integer)msg.obj,VAR_DEC);
				}
				break;
            case TimeDateUtils.KEY_UP:
				if((Integer)msg.obj == R.id.hour) {
					mHours = updateVar(mHours,time.getActualMinimum(Calendar.HOUR_OF_DAY),
					    time.getActualMaximum(Calendar.HOUR_OF_DAY),VAR_INC);
					System.out.println("mHours==up="+mHours);
					setTime(mHours,mMinutes,mSeconds);
				} else if((Integer)msg.obj == R.id.minite) {
					mMinutes = updateVar(mMinutes,time.getActualMinimum(Calendar.MINUTE),
				    		time.getActualMaximum(Calendar.MINUTE),VAR_INC);
					setTime(mHours,mMinutes,mSeconds);
				} else if((Integer)msg.obj == R.id.second) {
					mSeconds = updateVar(mSeconds,time.getActualMinimum(Calendar.SECOND),
				    		time.getActualMaximum(Calendar.SECOND),VAR_INC);
					setTime(mHours,mMinutes,mSeconds);
				} 
				else if((Integer)msg.obj == R.id.year || (Integer)msg.obj == R.id.month
							|| (Integer)msg.obj == R.id.data){
					updateDateValue((Integer)msg.obj,VAR_INC);
				}
				break;
				
				
				
            case TimeDateUtils.KEY_LEFT:
            	Log.d(TAG, "left");
//            	timeHourSpinner.setFocusable(false);
//            	timeHourSpinner.setEnabled(false);
//				if((Integer)msg.obj == R.id.hour) {
//					setLayoutFocus(3,true);
//				} else if((Integer)msg.obj == R.id.minite) {
//					setLayoutFocus(4,true);
//				} else if((Integer)msg.obj == R.id.second) {
//					setLayoutFocus(5,true);
//				} else if((Integer)msg.obj == R.id.year) {
//					setLayoutFocus(0,true);
//				} else if((Integer)msg.obj == R.id.month) {
//					setLayoutFocus(1,true);
//				} else if((Integer)msg.obj == R.id.data) {
//					setLayoutFocus(2,true);
//				} 
				break;
				
            case TimeDateUtils.KEY_RIGHT:
            	Log.d(TAG, "right");
//            	timeHourSpinner.setFocusable(false);
//            	timeHourSpinner.setEnabled(false);
//				if((Integer)msg.obj == R.id.hour) {
//					setLayoutFocus(3,true);
//				} else if((Integer)msg.obj == R.id.minite) {
//					setLayoutFocus(4,true);
//				} else if((Integer)msg.obj == R.id.second) {
//					setLayoutFocus(5,true);
//				} else if((Integer)msg.obj == R.id.year) {
//					setLayoutFocus(0,true);
//				} else if((Integer)msg.obj == R.id.month) {
//					setLayoutFocus(1,true);
//				} else if((Integer)msg.obj == R.id.data) {
//					setLayoutFocus(2,true);
//				} 
				break;
			
            case TimeDateUtils.FOCUSED:
        		Log.e(TAG,"FOCUSED"+" id = "+msg.obj);
				color = getResources().getColor(R.color.yellow);
            	if((Integer)msg.obj == R.id.relativeLayoutHour ) {
            		Log.e(TAG,"relativeLayoutTime");
            		mHourLayout.setBackgroundResource(R.drawable.time_setting_foucs);
					HourSettingColor(color);
            	}else if((Integer)msg.obj == R.id.relativeLayoutMinite){
            		Log.e(TAG,"relativeLayoutDate");
            		mMiniteLayout.setBackgroundResource(R.drawable.time_setting_foucs);
					MiniteSettingColor(color);
            	}else if((Integer)msg.obj == R.id.relativeLayoutSecond){
            		Log.e(TAG,"relativeLayoutDate");
            		mSecondLayout.setBackgroundResource(R.drawable.time_setting_foucs);
					SecondSettingColor(color);
            	}else if((Integer)msg.obj == R.id.relativeLayoutYear){
            		Log.e(TAG,"relativeLayoutDate");
            		mYearLayout.setBackgroundResource(R.drawable.time_setting_foucs);
					YearSettingColor(color);
            	}else if((Integer)msg.obj == R.id.relativeLayoutMonth){
            		Log.e(TAG,"relativeLayoutDate");
            		mMonthLayout.setBackgroundResource(R.drawable.time_setting_foucs);
				    MonthSettingColor(color);
            	}else if((Integer)msg.obj == R.id.relativeLayoutData){
            		Log.e(TAG,"relativeLayoutDate");
            		mDayLayout.setBackgroundResource(R.drawable.time_setting_foucs);
					DaySettingColor(color);
            	}
				break;
            case TimeDateUtils.UNFOCUSED: 
				color = getResources().getColor(R.color.date_color_normal);
            	if((Integer)msg.obj == R.id.relativeLayoutHour ) {
            		if(mEnableHourFocus)
            			break;
            		mHourLayout.setBackgroundResource(R.drawable.time_setting_nofoucs);
					HourSettingColor(color);
            	}else if((Integer)msg.obj == R.id.relativeLayoutMinite ) {
                		if(mEnableMiniteFocus)
                			break;
                		mMiniteLayout.setBackgroundResource(R.drawable.time_setting_nofoucs);
						MiniteSettingColor(color);
                }else if((Integer)msg.obj == R.id.relativeLayoutSecond ) {
            		if(mEnableSecondFocus)
            			break;
            		mSecondLayout.setBackgroundResource(R.drawable.time_setting_nofoucs);
					SecondSettingColor(color);
	            }else if((Integer)msg.obj == R.id.relativeLayoutYear ) {
	        		if(mEnableYearFocus)
	        			break;
	        		mYearLayout.setBackgroundResource(R.drawable.time_setting_nofoucs);
					YearSettingColor(color);
	            }else if((Integer)msg.obj == R.id.relativeLayoutMonth ) {
            		if(mEnableMonthFocus)
            			break;
            		mMonthLayout.setBackgroundResource(R.drawable.time_setting_nofoucs);
					MonthSettingColor(color);
	            }else if((Integer)msg.obj == R.id.relativeLayoutData ) {
            		if(mEnableDayFocus)
            			break;
            		mDayLayout.setBackgroundResource(R.drawable.time_setting_nofoucs);
					DaySettingColor(color);
	            }
				break;
            case TimeDateUtils.KEY_CENTER:
            	
            	if((Integer)msg.obj == R.id.relativeLayoutHour ) {
            		mEnableHourFocus = true;
					HourSettingFocus(true);
					mTextHour.requestFocus();
//	            	mHourLayout.setBackgroundResource(R.drawable.edittext_small_focus);
				}else if((Integer)msg.obj == R.id.relativeLayoutMinite ){
					mEnableMiniteFocus = true;
					MiniteSettingFocus(true);
					mTextMin.requestFocus();
//					mMiniteLayout.setBackgroundResource(R.drawable.edittext_small_focus);
				} else if((Integer)msg.obj == R.id.relativeLayoutSecond ){
					mEnableSecondFocus = true;
					SecondSettingFocus(true);
					mTextSecond.requestFocus();
//					mSecondLayout.setBackgroundResource(R.drawable.edittext_small_focus);
				} else if((Integer)msg.obj == R.id.relativeLayoutYear){
					mEnableYearFocus = true;
					YearSettingFocus(true);
					mTextYear.requestFocus();
					//点击后换一个背京
//					mYearLayout.setBackgroundResource(R.drawable.setting_time_focus);
				} else if((Integer)msg.obj == R.id.relativeLayoutMonth ){
					mEnableMonthFocus = true;
					MonthSettingFocus(true);
					mTextMonth.requestFocus();
//					mMonthLayout.setBackgroundResource(R.drawable.edittext_small_focus);
				} else if((Integer)msg.obj == R.id.relativeLayoutData ){
					mEnableDayFocus = true;
					DaySettingFocus(true);
					mTextDay.requestFocus();
//					mDayLayout.setBackgroundResource(R.drawable.edittext_small_focus);
				} 
				break;					
			case TimeDateUtils.KEY_BACK:
				
				timeHourSpinner.setFocusable(true);
            	timeHourSpinner.setEnabled(true);
            	setLayoutFocusRecover();
				
				if((Integer)msg.obj == R.id.hour) {
					HourSettingFocus(false);
					mEnableHourFocus  = false;
					mHourLayout.requestFocus();
				}else if((Integer)msg.obj == R.id.minite){
					mEnableMiniteFocus  = false;
					MiniteSettingFocus(false);
					mMiniteLayout.requestFocus();
				}else if((Integer)msg.obj == R.id.second){
					mEnableSecondFocus  = false;
					SecondSettingFocus(false);
					mSecondLayout.requestFocus();
				}else if((Integer)msg.obj == R.id.year){
					mEnableYearFocus  = false;
					YearSettingFocus(false);
					mYearLayout.requestFocus();
				}else if((Integer)msg.obj == R.id.month){
					mEnableMonthFocus  = false;
					MonthSettingFocus(false);
					mMonthLayout.requestFocus();
				}else if((Integer)msg.obj == R.id.data){
					mEnableDayFocus  = false;
					DaySettingFocus(false);
					mDayLayout.requestFocus();
				}        				
				break;
			}
			updateTimeAndDateDisplay(getActivity());
			
		}
		
	};
	private TimeDateUtils mHourUtils,mMiniteUtils,mSecondUtils,mYearUtils,mMonthUtils,mDayUtils;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		//TODO
		mView = inflater.inflate(R.layout.date_settings, container, false);
		
		final LinearLayout inputLayout = (LinearLayout) mView.findViewById(R.id.input_layout);
		timeHourSpinner = (TextView) mView.findViewById(R.id.timeHourSpinner);
		timeHourSpinner.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View arg0, boolean arg1) {
				if(arg1){
					inputLayout.setBackgroundResource(R.drawable.edit_text_focus);
				}else{
					inputLayout.setBackgroundResource(R.drawable.edit_text_nofocus);
				}
			}
		});
		

		mHourLayout = (RelativeLayout)mView.findViewById(R.id.relativeLayoutHour);
		mTextHour = (TextView)mView.findViewById(R.id.hour);
		mArrayHour.add(R.id.hour);
		mHourUtils = new TimeDateUtils(getActivity(), 
								R.id.relativeLayoutHour, mArrayHour, mHandler);
		
		mMiniteLayout = (RelativeLayout)mView.findViewById(R.id.relativeLayoutMinite);
		mTextMin = (TextView)mView.findViewById(R.id.minite);
		mArrayMinite.add(R.id.minite);
		mMiniteUtils = new TimeDateUtils(getActivity(), 
								R.id.relativeLayoutMinite, mArrayMinite, mHandler);
		
		mSecondLayout = (RelativeLayout)mView.findViewById(R.id.relativeLayoutSecond);
		mTextSecond = (TextView)mView.findViewById(R.id.second);
		mArraySecond.add(R.id.second);
		mSecondUtils = new TimeDateUtils(getActivity(), 
								R.id.relativeLayoutSecond, mArraySecond, mHandler);
		
		//年框
		mYearLayout = (RelativeLayout)mView.findViewById(R.id.relativeLayoutYear);
		mTextYear = (TextView)mView.findViewById(R.id.year);
		mArrayYear.add(R.id.year);
		mYearUtils = new TimeDateUtils(getActivity(), 
								R.id.relativeLayoutYear, mArrayYear, mHandler);
		
		mMonthLayout = (RelativeLayout)mView.findViewById(R.id.relativeLayoutMonth);
		mTextMonth = (TextView)mView.findViewById(R.id.month);
		mArrayMonth.add(R.id.month);
		mMonthUtils = new TimeDateUtils(getActivity(), 
								R.id.relativeLayoutMonth, mArrayMonth, mHandler);
		
		mDayLayout = (RelativeLayout)mView.findViewById(R.id.relativeLayoutData);
		mTextDay = (TextView)mView.findViewById(R.id.data);
		mArrayDay.add(R.id.data);
		mDayUtils = new TimeDateUtils(getActivity(), 
								R.id.relativeLayoutData, mArrayDay, mHandler);
		
		
		mTextHour.setOnFocusChangeListener(mHourUtils.getFocusChangeListener());
		mHourLayout.setOnFocusChangeListener(mHourUtils.getFocusChangeListener());
	
		mTextMin.setOnFocusChangeListener(mMiniteUtils.getFocusChangeListener());
		mMiniteLayout.setOnFocusChangeListener(mMiniteUtils.getFocusChangeListener());
		
		mTextSecond.setOnFocusChangeListener(mSecondUtils.getFocusChangeListener());
		mSecondLayout.setOnFocusChangeListener(mSecondUtils.getFocusChangeListener());

		mTextHour.setOnKeyListener(mHourUtils.getKeyListener());
		mTextMin.setOnKeyListener(mMiniteUtils.getKeyListener());
		mTextSecond.setOnKeyListener(mSecondUtils.getKeyListener());
		
		mHourLayout.setOnKeyListener(mHourUtils.getKeyListener());
		mMiniteLayout.setOnKeyListener(mMiniteUtils.getKeyListener());
		mSecondLayout.setOnKeyListener(mSecondUtils.getKeyListener());
		
    	mHourLayout.setBackgroundResource(R.drawable.time_setting_nofoucs);
    	mMiniteLayout.setBackgroundResource(R.drawable.time_setting_nofoucs);
    	mSecondLayout.setBackgroundResource(R.drawable.time_setting_nofoucs);
		
    	mTextYear.setOnFocusChangeListener(mYearUtils.getFocusChangeListener());
    	mTextMonth.setOnFocusChangeListener(mMonthUtils.getFocusChangeListener());
    	mTextDay.setOnFocusChangeListener(mDayUtils.getFocusChangeListener());
    	
		mYearLayout.setOnFocusChangeListener(mYearUtils.getFocusChangeListener());
		mMonthLayout.setOnFocusChangeListener(mMonthUtils.getFocusChangeListener());
		mDayLayout.setOnFocusChangeListener(mDayUtils.getFocusChangeListener());
		
		mTextYear.setOnKeyListener(mYearUtils.getKeyListener());
		mTextMonth.setOnKeyListener(mMonthUtils.getKeyListener());
		mTextDay.setOnKeyListener(mDayUtils.getKeyListener());
		
		mYearLayout.setOnKeyListener(mYearUtils.getKeyListener());
		mMonthLayout.setOnKeyListener(mMonthUtils.getKeyListener());
		mDayLayout.setOnKeyListener(mDayUtils.getKeyListener());
		
		//年　月　日
		mYearLayout.setBackgroundResource(R.drawable.time_setting_nofoucs);
		mMonthLayout.setBackgroundResource(R.drawable.time_setting_nofoucs);
		mDayLayout.setBackgroundResource(R.drawable.time_setting_nofoucs);
		
	    

		YearSettingFocus(false);
		MonthSettingFocus(false);
		DaySettingFocus(false);
		HourSettingFocus(false);
		MiniteSettingFocus(false);
		SecondSettingFocus(false);
		
		YearSettingColor(getResources().getColor(R.color.date_color_normal));
		MonthSettingColor(getResources().getColor(R.color.date_color_normal));
		DaySettingColor(getResources().getColor(R.color.date_color_normal));
		HourSettingColor(getResources().getColor(R.color.date_color_normal));
		MiniteSettingColor(getResources().getColor(R.color.date_color_normal));
		SecondSettingColor(getResources().getColor(R.color.date_color_normal));
		
//		dateyes = (Button) mView.findViewById(R.id.date_yes);
//		dateno = (Button) mView.findViewById(R.id.date_no);
//		time_format_12 = (TextView) mView.findViewById(R.id.time_format_12);
//		time_format_24 = (TextView) mView.findViewById(R.id.time_format_24);
//		dateyes.setOnClickListener(new dateOncliListener());
//		dateno.setOnClickListener(new dateOncliListener());
//		time_format_12.setOnClickListener(new dateOncliListener());
//		time_format_24.setOnClickListener(new dateOncliListener());
		
//		update_city();
//		
//		mCityName = SystemProperties.get(STR_CITY_NAME, getActivity().getString(R.string.system_default_city_name));
//		mCityId = SystemProperties.get(STR_CITY_ID, getActivity().getString(R.string.system_default_city_id));
		
		mDropMenu = new DropdownMenu(getActivity(),mView,mHandler);
		mDropMenu.setListViewListener();
		
//		mSyncArrays = getResources().getStringArray(R.array.frequency_array);
//		mIndexSync = getIndex(isAutoDateTimeEnabled());
//		mDropMenu.setButtonListener(timeSyncSpinner,mSyncArrays[mIndexSync]);
		
		mTimeArrays = getResources().getStringArray(R.array.timeformat_array);
		mIndexTime = getIndex(is24Hour());
		mDropMenu.setButtonListener(timeHourSpinner,mTimeArrays[mIndexTime]);
		
//		mDateArrays = getResources().getStringArray(R.array.date_format_values);
//		mIndexDate = getFormatIndex(getDateFormat());
//		mDropMenu.setButtonListener(timeFormatSpinner,mDateArrays[mIndexDate]);
		
//		mDropMenu.setButtonListener(mCityEdit,mCityName);	
		LenovoSettingsActivity.setTitleFocus(false);	
//		timeSyncSpinner.requestFocus();
//		time_format_12.requestFocus();
		
		rLayouts.add(mYearLayout);
		rLayouts.add(mMonthLayout);
		rLayouts.add(mDayLayout);
		rLayouts.add(mHourLayout);
		rLayouts.add(mMiniteLayout);
		rLayouts.add(mSecondLayout);
		
		timeHourSpinner.requestFocus();
		
		return mView;
	} 
	
//	public void saveCity(String name){
//		String city_id = null;
//		//Editor sharedata = getActivity().getSharedPreferences("Settings", 0).edit(); 
//		if(name.equals(mCityName)){
//			return;
//		}
//		for(int i=0; i<mCitys.size();i++){
//			String str = mCitys.get(i);
//			if(name.equals(str)){
//				if(mCityData == null){
//					city_id = mCityId;					
//				}else{
//					city_id = (String) mCityData.get(i).get("id");
//				}
//			}
//		}
//		mCityName = name;
//		mCityId = city_id;
//		SystemProperties.set(STR_CITY_NAME, name);
//		SystemProperties.set(STR_CITY_ID, city_id);
//		Intent intent = new Intent(BROADCAST_CITY_ID_ACTION);  
//        intent.putExtra("city_id", city_id);				        
//        getActivity().sendBroadcast(intent); 	
//        Log.e(TAG,"send msg = "+BROADCAST_CITY_ID_ACTION+" id = "+city_id);
//	}
	

    public Dialog onCreateDialog(int id) {
        Dialog d;

        switch (id) {
        case DIALOG_DATEPICKER: {
            final Calendar calendar = Calendar.getInstance();
            d = new DatePickerDialog(
                getActivity(),
                this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
            break;
        }
//        case DIALOG_TIMEPICKER: {
//            final Calendar calendar = Calendar.getInstance();
//            d = new TimePickerDialog(
//                    getActivity(),
//                    this,
//                    calendar.get(Calendar.HOUR_OF_DAY),
//                    calendar.get(Calendar.MINUTE),
//                    DateFormat.is24HourFormat(getActivity()));
//            break;
//        }
        default:
            d = null;
            break;
        }

        return d;
    }
    
	private boolean isAutoDateTimeEnabled() {
		try {
			return Settings.System.getInt(getActivity().getContentResolver(),
					Settings.System.AUTO_TIME) > 0;
		} catch (SettingNotFoundException e) {
			return true;
		}
	}

	private int getIndex(Boolean enable) {

		if (enable)
			return 0;
		return 1;
	}

	private void setAutoDateTime(int index) {
		Settings.System.putInt(getActivity().getContentResolver(),
				Settings.System.AUTO_TIME, index == 0 ? 1 : 0);
	}

	private boolean is24Hour() {
		return DateFormat.is24HourFormat(getActivity());
	}

	private void set24Hour(int is24Hour) {
		Settings.System.putString(getActivity().getContentResolver(),
				Settings.System.TIME_12_24, is24Hour == 0 ? "24" : "12");
	}

	private String getDateFormat() {
		return Settings.System.getString(getActivity().getContentResolver(),
				Settings.System.DATE_FORMAT);
	}

	private Boolean setDateFormat(String format) {
		return Settings.System.putString(getActivity().getContentResolver(),
				Settings.System.DATE_FORMAT, format);
	}

	private int getFormatIndex(String format) {
		if (format == null)
			return 0;
		return findIndexOfEntry(format,
				getResources().getStringArray(R.array.date_format));
	}

	private int findIndexOfEntry(String value, String[] entry) {
		if (value != null && entry != null) {
			for (int i = entry.length - 1; i >= 0; i--) {
				if (entry[i].equals(value)) {
					return i;
				}
			}
		}
		return 0;
	}

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        setDate(year, month, day);
        final Activity activity = getActivity();
        if (activity != null) {
            updateTimeAndDateDisplay(activity);
        }
    }

//    @Override
//    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//        setTime(hourOfDay, minute);
//        final Activity activity = getActivity();
//        if (activity != null) {
//            updateTimeAndDateDisplay(activity);
//        }
//    }


@Override
    public void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        updateTimeAndDateDisplay(getActivity());
    }
    

@Override
    public void onResume() {
        super.onResume();

//        IntentFilter filter = new IntentFilter();
//        filter.addAction(Intent.ACTION_TIME_TICK);
//        filter.addAction(Intent.ACTION_TIME_CHANGED);
//        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
//        getActivity().registerReceiver(mIntentReceiver, filter, null, null);
        
        mHandler.sendEmptyMessage(MSG_TIMER);

        updateTimeAndDateDisplay(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
//        getActivity().unregisterReceiver(mIntentReceiver);
        mHandler.removeMessages(MSG_TIMER);
    }
  private void timeUpdated() {
        Intent timeChanged = new Intent(Intent.ACTION_TIME_CHANGED);
        getActivity().sendBroadcast(timeChanged);
    }
  
  static void setDate(int newYear, int newMonth, int newDay) {
      Calendar c = Calendar.getInstance();
      int oldYear = c.get(Calendar.YEAR);
      int oldMonth = c.get(Calendar.MONTH);
      int oldDay = c.get(Calendar.DAY_OF_MONTH);
      if(oldYear != newYear){
    	  c.set(Calendar.YEAR, newYear);
      }else if(oldMonth != newMonth){
          if (oldMonth == 11 && newMonth == 0) {
              c.add(Calendar.MONTH, 1);
          } else if (oldMonth == 0 && newMonth == 11) {
              c.add(Calendar.MONTH, -1);
          } else {
              c.add(Calendar.MONTH, newMonth - oldMonth);
          }
    	  
      }else if(oldDay != newDay){
          int maxDayOfMonth = c.getActualMaximum(Calendar.DAY_OF_MONTH);
          if (oldDay == maxDayOfMonth && newDay == 1) {
              c.add(Calendar.DAY_OF_MONTH, 1);
          } else if (oldDay == 1 && newDay == maxDayOfMonth) {
              c.add(Calendar.DAY_OF_MONTH, -1);
          } else {
              c.add(Calendar.DAY_OF_MONTH, newDay - oldDay);
          }
    	  
      }

      long when = c.getTimeInMillis();

      if (when / 1000 < Integer.MAX_VALUE) {
          SystemClock.setCurrentTimeMillis(when);
      }
  }

	private void setTime(int hour, int minute,int second) {
		Calendar c = Calendar.getInstance();
		
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND,second);
		c.set(Calendar.MILLISECOND, 0);
		long when = c.getTimeInMillis();

		if (when / 1000 < Integer.MAX_VALUE) {
			SystemClock.setCurrentTimeMillis(when);
			System.out.println("settting time ok");
		}
	}


	
	
private static char[] formatOffset(int off) {
        off = off / 1000 / 60;

        char[] buf = new char[9];
        buf[0] = 'G';
        buf[1] = 'M';
        buf[2] = 'T';

        if (off < 0) {
            buf[3] = '-';
            off = -off;
        } else {
            buf[3] = '+';
        }

        int hours = off / 60;
        int minutes = off % 60;

        buf[4] = (char) ('0' + hours / 10);
        buf[5] = (char) ('0' + hours % 10);

        buf[6] = ':';

        buf[7] = (char) ('0' + minutes / 10);
        buf[8] = (char) ('0' + minutes % 10);

        return buf;
    }
    
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final Activity activity = getActivity();
            if (activity != null) {
                updateTimeAndDateDisplay(activity);
            }
        }
    };
    
    
public void updateTimeAndDateDisplay(Context context) {
       final Calendar time = Calendar.getInstance();
//       mHourLayout.setEnabled(!isAutoDateTimeEnabled());
//       mMiniteLayout.setFocusable(!isAutoDateTimeEnabled());
//       mSecondLayout.setEnabled(!isAutoDateTimeEnabled());
//       mYearLayout.setFocusable(!isAutoDateTimeEnabled());
//       mMonthLayout.setEnabled(!isAutoDateTimeEnabled());
//       mDayLayout.setFocusable(!isAutoDateTimeEnabled());
    
       mHours = time.get(Calendar.HOUR_OF_DAY);
       if(is24Hour()){
    	   mTextHour.setText(getTimeString(mHours));
//           mTextTimeFormat.setText("");
       }else{
    	   int hour = time.get(Calendar.HOUR);
           if(hour == 0){
        	   mTextHour.setText("12");
           }else{
        	   mTextHour.setText(getTimeString(hour));
           }
//           if(time.get(Calendar.AM_PM) == Calendar.AM){
//        	   mTextTimeFormat.setText(getActivity().getString(R.string.data_am));
//           }else{
//        	   mTextTimeFormat.setText(getActivity().getString(R.string.data_pm));        	   
//           }
       }
       mMinutes = time.get(Calendar.MINUTE);
       if(mMinutes < 10){
    	   mTextMin.setText("0"+getTimeString(mMinutes));
       }else{
    	   mTextMin.setText(getTimeString(mMinutes));
       }
       mYear = time.get(Calendar.YEAR);
       mMonth = time.get(Calendar.MONTH);
       mDay = time.get(Calendar.DAY_OF_MONTH);
       updateDateDisplay(mYear,mMonth,mDay);
}  
	
public ArrayList<Map<String,Object>> getCityData(String url){
		ArrayList<Map<String,Object>> data = new ArrayList<Map<String,Object>>();
		/*HttpGet request = new HttpGet(mUrl); 
		HttpResponse httpResponse;
		try {
			
			httpResponse = new DefaultHttpClient().execute(request);	
			if (httpResponse.getStatusLine().getStatusCode() == 200){		
				String retSrc = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");	
				JSONArray arrary = new JSONArray(retSrc);
				Log.d(TAG,"JSON Array length = "+arrary.length());
				for(int i=0;i<arrary.length();i++){
					Map<String,Object> item = new HashMap<String,Object>();
					JSONObject object = arrary.getJSONObject(i);
					item.put("alphabet", object.get("alphabet"));
					item.put("name_en", object.get("name_en"));
					item.put("name", object.get("name"));
					item.put("id", object.get("id"));
					data.add(item);					
				}
			}
		} catch (ClientProtocolException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		} catch (JSONException e) {
			
			e.printStackTrace();
		}*/
		


		try {
			InputStream is = getActivity().getAssets().open("geoid.json");
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			String text = new String(buffer, "UTF-8");
			JSONArray arrary = new JSONArray(text);
			Log.d(TAG,"JSON Array length = "+arrary.length());
			for(int i=0;i<arrary.length();i++){
				Map<String,Object> item = new HashMap<String,Object>();
				JSONObject object = arrary.getJSONObject(i);
				item.put("alphabet", object.get("alphabet"));
				item.put("name_en", object.get("name_en"));
				item.put("name", object.get("name"));
				item.put("id", object.get("id"));
				Log.d(TAG,"city name = "+item.get("name"));
				data.add(item);					
			}
		} catch (IOException e) {
			
			e.printStackTrace();
		} catch (JSONException e) {
			
			e.printStackTrace();
		}
       /* try {
			Log.d(TAG, "url: "+url);
    		URL Url = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) Url.openConnection();
            conn.setDoInput(true);
            //conn.setRequestProperty("User-Agent", getUserAgent());
            conn.setRequestProperty("Charset", "UTF-8"); 
			conn.connect();
			if(conn.getResponseCode() < 300){
                InputStream input = conn.getInputStream();
                InputStreamReader in=new InputStreamReader(input);
                StringBuilder sb = new StringBuilder();
                char buf[] = new char[4096];
                int cnt;
                String str;

                while((cnt=in.read(buf, 0, buf.length))!=-1 ){
                        sb.append(buf, 0, cnt);
                }

                str = sb.toString();
				JSONArray arrary = new JSONArray(str);
				for(int i=0;i<arrary.length();i++){
					Map<String,Object> item = new HashMap<String,Object>();
					JSONObject object = arrary.getJSONObject(i);
					item.put("alphabet", object.get("alphabet"));
					item.put("name_en", object.get("name_en"));
					item.put("name", object.get("name"));
					item.put("id", object.get("id"));
					if(i<10){
						Log.d(TAG,"name = "+item.get("name"));
					}
					data.add(item);
				}
			}
		} catch (IOException e) {
			
			e.printStackTrace();
		} catch (JSONException e) {
			
			e.printStackTrace();
		}*/
		return data; 			
	}
	
//	/** Defines callbacks for service binding, passed to bindService() */
//    private ServiceConnection mConnection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName className,
//                IBinder service) {
//            // We've bound to LocalService, cast the IBinder and get LocalService instance
//        	CityDataBinder binder = (CityDataBinder) service;
//            mService = binder.getService();
//            /*if(mCityData == null){
//	            mCityData = mService.getData();
//	            mData.clear();
//	    		if(mCityData == null){
//	    			mData.add(mCityName);
//	    		}else{
//	    			for(int i=0;i<mCityData.size();i++){
//	    				mData.add((String) mCityData.get(i).get("name"));
//	    			}			
//	    		}
//	    		mCityAdapter.notifyDataSetChanged();
//            }*/
//            mBound = true;
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName arg0) {
//            mBound = false;
//        }
//    };
	private Thread mCityThread;

	@Override
	public void onStart() {
		
		super.onStart();
	}

	@Override
	public void onStop() {
		
		super.onStop();
	}
	
//	void update_city(){
//		
//		mCityThread = new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//		
//				//mCityData = getCityData(mUrl);
//				queryCityFromProvider();
//				if(mCityData == null){
//					mCitys.add(mCityName);
//				}
//				
//			}
//			
//		});
//		mCityThread.start();
//	}
	
	
//    public void queryCityFromProvider() {
//    	
//
//		
//    	int i = 1;
//	    Cursor cursor = getActivity().getContentResolver().query(CONTENT_URI_SYS, null,null, null, null);
//	
//	    if(cursor!=null)
//	    	Log.e(TAG,"*********"+cursor.getCount());
//	    mCityData.clear();
//	    mCitys.clear();
//	    while(cursor.moveToNext()) {	    	
//		    String alphabet = cursor.getString(cursor.getColumnIndex(CityProvider.Citys.COLUMN_NAME_ALPHABET));
//		    String name_en = cursor.getString(cursor.getColumnIndex(CityProvider.Citys.COLUMN_NAME_CITY_NAME_EN));
//		    String name = cursor.getString(cursor.getColumnIndex(CityProvider.Citys.COLUMN_NAME_CITY_NAME));
//		    String id = cursor.getString(cursor.getColumnIndex(CityProvider.Citys.COLUMN_NAME_CITY_ID));
//		    Map<String,Object> item = new HashMap<String,Object>();
//		    item.put(CityProvider.Citys.COLUMN_NAME_ALPHABET, alphabet);
//		    item.put(CityProvider.Citys.COLUMN_NAME_CITY_NAME_EN, name_en);
//		    item.put(CityProvider.Citys.COLUMN_NAME_CITY_NAME, name);
//		    item.put(CityProvider.Citys.COLUMN_NAME_CITY_ID, id);
//		    mCitys.add(name);
//		    mCityData.add(item);
//		    i ++;
//	    }
//	    cursor.close();
//
//    }
    
    void setTextDisplay(TextView text, int var){
    	String str = (new Integer(var)).toString();
    	if(var < 10){
    		str = "0" + str;
    	}
    	text.setText(str);
    }
    
    String getTimeString(int var){
    	String str = (new Integer(var)).toString();
    	return str;
    }
    
    String getDateString(int var){
    	String str = (new Integer(var)).toString();
    	if(var < 10){ // Rony remove 
    		str = "0" + str;
    	}
    	return str;
    }
    
    int updateVar(int var,int min,int max,int direction){
    	System.out.println("min--->"+min+"---max--->"+max);
    	switch(direction){
    	case VAR_INC:
    		var ++;
    		if(var > max){
    			var = min;
    		}
    		break;
    	case VAR_DEC:
    		if(var > min){
    			var --;
    		}else{
    			var = max;
    		}
    		break;
    	}
    	return var;
    }
    private int getHours(){
    	if(mTextHour.getText().toString()!=null)
    		return Integer.parseInt(mTextHour.getText().toString());
    	return 0;
    }
    
    void updateDateDisplay(int year,int month,int day){
    	month += 1;
    	mTextYear.setText(getDateString(year));
    	mTextMonth.setText(getDateString(month));
    	mTextDay.setText(getDateString(day));
    }
    
    void updateDateValue(int id, int direction){
	    Calendar time = Calendar.getInstance();
    	switch(getFormatIndex(getDateFormat())){
    	case 0:
    	case 1: 
    	case 2: // yyyy-MM-dd
    		switch(id){
    		case R.id.year:
    			mYear = updateVar(mYear,time.getActualMinimum(Calendar.YEAR),
			    		time.getActualMaximum(Calendar.YEAR),direction);
    			break;
    		case R.id.month:
    			mMonth = updateVar(mMonth,time.getActualMinimum(Calendar.MONTH),
			    		time.getActualMaximum(Calendar.MONTH),direction);
    			break;
    		case R.id.data:
    			mDay = updateVar(mDay,time.getActualMinimum(Calendar.DAY_OF_MONTH),
			    		time.getActualMaximum(Calendar.DAY_OF_MONTH),direction);
    			break;
    		}
    		break;
    	}
    	setDate(mYear,mMonth,mDay);
    }
    
    void HourSettingFocus(boolean enable){    	  
        mTextHour.setEnabled(enable);
        mTextHour.setFocusable(enable);
    }
    
    void MiniteSettingFocus(boolean enable){  
        mTextMin.setEnabled(enable);
        mTextMin.setFocusable(enable);
    }
    
    void SecondSettingFocus(boolean enable){    	  
        mTextSecond.setEnabled(enable);
        mTextSecond.setFocusable(enable);
    }
   

    void YearSettingFocus(boolean enable){  
        mTextYear.setEnabled(enable);
        mTextYear.setFocusable(enable);
    }
    
    void MonthSettingFocus(boolean enable){  
        mTextMonth.setEnabled(enable);
        mTextMonth.setFocusable(enable);
    }
    void DaySettingFocus(boolean enable){  
        mTextDay.setEnabled(enable);
        mTextDay.setFocusable(enable);
    }

	void HourSettingColor(int color){
		mTextHour.setTextColor(color);
	}
	void MiniteSettingColor(int color){
		mTextMin.setTextColor(color);
	}
	void SecondSettingColor(int color){
		mTextSecond.setTextColor(color);
	}

    void YearSettingColor(int color){
		mTextYear.setTextColor(color);
    }
    void MonthSettingColor(int color){
		mTextMonth.setTextColor(color);
    }
    void DaySettingColor(int color){
		mTextDay.setTextColor(color);
    }
    public void updataTimeOfSeconds(Context context) {
		final Calendar time = Calendar.getInstance();
		mSeconds = time.get(Calendar.SECOND);
	       if(mSeconds < 10){
	    	   mTextSecond.setText("0"+getTimeString(mSeconds));
	       }else{
	    	   mTextSecond.setText(getTimeString(mSeconds));
	       }
	       if(mSeconds == 0)
	    	   updateTimeAndDateDisplay(getActivity());
	}
    
    class dateOncliListener implements View.OnClickListener{

		@Override
		public void onClick(View v) {
//			if(v == dateyes){
//				int mHours = Integer.parseInt(mTextHour.getText().toString());
//				int mMinite = Integer.parseInt(mTextMin.getText().toString());
//				int mSecond = Integer.parseInt(mTextSecond.getText().toString());
//				int mYear = Integer.parseInt(mTextYear.getText().toString());
//				int mMonth = Integer.parseInt(mTextMonth.getText().toString());
//				int mDay = Integer.parseInt(mTextDay.getText().toString());
//				setTime(mHours,mMinite,mSecond);
//				setDate(mYear,mMonth,mDay);
//				Toast.makeText(getActivity(), "设置时间成功", Toast.LENGTH_SHORT).show();
//			}
//			if(v == dateno){
//				updateTimeAndDateDisplay(getActivity());
//			}
//			if(v == time_format_12){
//				if (getIndex(is24Hour()) != 1){
//					set24Hour(1);
//					mHandler.sendEmptyMessage(TIME_FORMAT);
//					Toast.makeText(getActivity(), "12小时格式", Toast.LENGTH_SHORT).show();
//				}
//			}
//			if(v == time_format_24){
//				if (getIndex(is24Hour()) != 0){
//					set24Hour(0);
//					mHandler.sendEmptyMessage(TIME_FORMAT);
//					Toast.makeText(getActivity(), "24小时格式", Toast.LENGTH_SHORT).show();
//				}
//			}
		}
	}
    
    List<RelativeLayout> rLayouts = new ArrayList<RelativeLayout>();
    //处理layout焦点
    private void setLayoutFocus(int layoutId,boolean mboolean){
    	for(int i = 0;i<rLayouts.size();i++){
    		if(i != layoutId){
    			rLayouts.get(i).setFocusable(!mboolean);
    			rLayouts.get(i).setEnabled(!mboolean);
    		}else{
    			rLayouts.get(i).setFocusable(mboolean);
    			rLayouts.get(i).setEnabled(mboolean);
    		}
    	}
    }
    private void setLayoutFocusRecover(){
    	for(int i = 0;i<rLayouts.size();i++){
    			rLayouts.get(i).setFocusable(true);
    			rLayouts.get(i).setEnabled(true);
    	}
    }

}