package com.lenovo.settings;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AbsoluteLayout;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.joysee.settings.applock.AppLockSettings;
import com.lenovo.leos.push.PsAuthenServiceL;
import com.lenovo.settings.applications.ManageApplications;
import com.lenovo.settings.theme.ThemeSetting;

//Hazel add }
public class LenovoSettingsActivity extends Activity {
    public static final String TAG = "LenovoSettingsActivity";
    public static final String REGISTER = "register";
    public static final String FORGETPASSWORD = "forgetPassword";
    public static boolean isProxy = false;
    public static boolean isShownText = false;
	private static ListView mListView;
	
    int[] mArrayRes = {
            R.string.resolution_setting, 
            R.string.tvsearch_settings, R.string.time_settings,
            R.string.language_settings, R.string.network_settings,
            R.string.audio_output, R.string.mouse_setting,
            R.string.input_method_settings,
            R.string.System_update, R.string.application_settings,
            R.string.System_info, R.string.super_card, R.string.applock_set, R.string.theme_setting,
            R.string.account_settings
    };
	private ListViewAdapter mListViewAdapter;
	private int mCurCheckPosition = 0;
	private int mPosition = 0;
	private int mShownCheckPosition = -1;
	private LinearLayout mLeftLayout;
	private LinearLayout mRightLayout;
	protected boolean mBound = false;
	private String mSubChoice = "";
//	protected static CityDataService mService; 
	
	private RelativeLayout mMainLayout;
	private ImageView muteImage;
	private ProgressBar volumeProgress;
	private TextView volumeText;
	private TextView muteText;

	boolean mIsMute = false;
	private boolean volKeyDown = false;
	private int storedVol;
	private Handler mHandler;
	/**记录Listview 被选Item位置*/
	private int mLeftListViewSelectPos;
	/**记录ListView　被点击的item位置,第一个默认被点击*/
	private int mLeftListViewClickPos=0;
	private LinearLayout beClickItem = null;
	
	@Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "-----------------onNewIntent-----------");
        setIntent(intent);
        Bundle bundle = new Bundle();
        bundle = this.getIntent().getExtras();
        int position = 0;
        boolean isShowFrament = false;
        if (null != bundle) {
            position = bundle.getInt("curChoice", 0);
            mSubChoice = bundle.getString("subChoice", "");
            isShowFrament = true;
        }else{
            Log.d(TAG, " bundle is null!!");
        }
        Log.d(TAG, "--------------onNewIntent----------- mCurCheckPosition = "
                + mCurCheckPosition + " position = " + position);
        if (position != mCurCheckPosition) {
            mCurCheckPosition = position;
        }
        if(isShowFrament){
            showFragment();
        }
    }

    @Override
    protected void onUserLeaveHint() {
        
//        this.finish();
        Log.d(TAG, "-----------------onUserLeaveHint-----------");
        super.onUserLeaveHint();
    }
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "-----------------onCreat----------- begin");
        //设置无标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        int screen_Width = getWindowManager().getDefaultDisplay().getRawWidth();
        int screen_Height = getWindowManager().getDefaultDisplay()
                .getRawHeight();
//        try {
//            //For amlogic A18
//            SystemProperties.set("vplayer.hideStatusBar.enable","true");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        RelativeLayout layout = new RelativeLayout(this);
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.main, null);
        layout.addView(view, screen_Width, screen_Height);
        setContentView(layout);

        DisplayMetrics DM = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(DM);
        Log.d(TAG, " DisplayMetrics width = " + DM.widthPixels + " height = " + DM.heightPixels
                + " screen_Width = " + screen_Width + " screen_Height = " + screen_Height);
        
        mMainLayout = (RelativeLayout)findViewById(R.id.main_layout);
//        mMainLayout.setBackgroundDrawable(getThemePaper());
//        try {
//        	mPosition = Settings.System.getInt(this.getContentResolver(), "curChoice");
//        } catch (SettingNotFoundException e) {
//        	
//        	e.printStackTrace();
//        }
        
		Bundle bundle = new Bundle();
	    bundle = this.getIntent().getExtras();
	    
	    if(bundle != null){
//	    	if(mPosition == 1)
//	    		mCurCheckPosition = 4;
//	    	else
	    	mCurCheckPosition = bundle.getInt("curChoice",0);
	    	mSubChoice = bundle.getString("subChoice", "");
	    }else{
	        try {
                mCurCheckPosition = Settings.System.getInt(this.getContentResolver(), "curChoice");
            } catch (SettingNotFoundException e) {
                
                e.printStackTrace();
            }
	        Log.d(TAG, " bundle is null!!");
	    }
	    
	    if((mCurCheckPosition < 0) || (mCurCheckPosition > mArrayRes.length)){
	    	mCurCheckPosition = 0;
	    }
	    mLeftListViewClickPos = mCurCheckPosition;
        Log.d(TAG, "---- mCurCheckPosition = " + mCurCheckPosition + " mSubChoice = " + mSubChoice);

        mListView = (ListView)findViewById(R.id.list);
        mLeftLayout = (LinearLayout) findViewById(R.id.layout_left);
        mRightLayout = (LinearLayout) findViewById(R.id.layout_right);
        mListViewAdapter = new ListViewAdapter(this, mArrayRes);
		mListView.setAdapter(mListViewAdapter);
		mListView.setDivider(null);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> content, View view,
                    int position, long arg3) {
            	mLeftListViewClickPos = position;
                showDetails(position);
            }

        }); 
        mListView.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                changeListViewBg(hasFocus);
                
                if(hasFocus){
                	//获得焦点时，所选中的item
                	Log.d(TAG, "--- is hasFocus ---"+mListView.getSelectedItemPosition());
                	if(mLeftListViewClickPos == mListView.getSelectedItemPosition()){
//                		Log.d(TAG,"--- 重叠 ---");
                		if(getClickItem()!=null){
                			getClickItem().setBackgroundResource(R.drawable.tab_none);
                		}
                	}
                	
                }else{
                	//失去焦点时，所选中的item
                	Log.d(TAG, "--- is noFocus ---"+mListView.getSelectedItemPosition());
                	if(mLeftListViewClickPos == mListView.getSelectedItemPosition()){
//                		Log.d(TAG,"--- 重叠 并失去焦点 ---");
                		if(getClickItem()!=null){
                			getClickItem().setBackgroundResource(R.drawable.setting_left_text_nofoucs); 
                		}
                	}
                }
            }

        });
		mListView.setOnKeyListener(new OnKeyListener() {
			@Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                
                if ((keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
                        || (keyCode == KeyEvent.KEYCODE_DPAD_LEFT)) {
                    return true;
                }
                if ((mLeftListViewSelectPos == 0)
                        && (keyCode == KeyEvent.KEYCODE_DPAD_UP)) {
                    return true;
                }
                if ((mLeftListViewSelectPos == mArrayRes.length - 3)
                        && (keyCode == KeyEvent.KEYCODE_DPAD_DOWN)) {
                    return true;
                }
                return false;
            }
			
		});
        mListView.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> mAdapterView, View view,
                    int position, long arg3) {
                setImageVisibility(position);
                mLeftListViewSelectPos = position;
//                Log.d(TAG, "setOnItemSelectedListener : "+position  +"  重置点击item背景");
                if(mLeftListViewClickPos != position){
                	if(getClickItem()!=null){
                		getClickItem().setBackgroundResource(R.drawable.setting_left_text_nofoucs);
                	}
                }else{
                	if(getClickItem()!=null){
	                	getClickItem().setBackgroundResource(R.drawable.tab_none);
                	}
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        if(!mSubChoice.equals("cctv-search")){
        	changeListViewBg(true);
        }else{
        	changeListViewBg(false);
        }
		mHandler = new Handler();
		showFragment();
    }
    /**
     * add in 07-13 by wuhao
     */
    protected void onResume() {
//        try {
//            // For amlogic A18
//            SystemProperties.set("vplayer.hideStatusBar.enable","true");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        Bundle bundle = new Bundle();
        bundle = this.getIntent().getExtras();
        int position = 0;
        if (null != bundle) {
            position = bundle.getInt("curChoice", 0);
        }
        Log.d(TAG, "--------------onResume----------- mCurCheckPosition = "
                + mCurCheckPosition);
        if (position != mCurCheckPosition) {
            mCurCheckPosition = position;
        }
        super.onResume();
    }

    protected void changeListViewBg(boolean hasFocus) {
        
        if (hasFocus) {
            Log.d(TAG,"left has focus!");
            mLeftLayout.setAlpha(1);
            mRightLayout.setAlpha((float) 0.5);
        } else {
            Log.d(TAG,"left lost focus!");
            mLeftLayout.setAlpha((float) 0.5);
            mRightLayout.setAlpha(1);
        }
    }

	public class ListViewAdapter extends BaseAdapter {
    	 
    	//private Context mContext;
		public int[] mArray;
		private LayoutInflater mLayoutInflater;
		private int selectedPosition = -1;  
		
		public void setSelectedPosition(int position) {   
			selectedPosition = position;   
		}

		public ListViewAdapter(Context context, int[] array) { 
			Log.d(TAG,"array size = "+array.length);
            mArray = array; 
            mLayoutInflater = LayoutInflater.from(context); 
        } 

		@Override
        public int getCount() {
            return mArray.length - 2;
        }

		@Override
		public Object getItem(int position) {
	
			return mArray[position];
		}

		@Override
		public long getItemId(int position) {
	
			return position;
		}
 
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
	
			ViewHolder holder = null;
			if (convertView == null) { 
				holder = new ViewHolder();
				convertView = mLayoutInflater.inflate(R.layout.setting_list, null); 
				holder.textView = (TextView) convertView.findViewById(R.id.textTitle);
				holder.layout = (LinearLayout) convertView.findViewById(R.id.listLayout);
				holder.selectLayout = (LinearLayout) convertView.findViewById(R.id.select_layout);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.textView.setText(mArray[position]);
			
			if(selectedPosition == position){
				if(convertView.hasFocus()){
					holder.selectLayout.setBackgroundResource(R.drawable.tab_none); 					
				}else{
					Log.d(TAG,"--- getview  set nofoucs ---");
					holder.selectLayout.setBackgroundResource(R.drawable.setting_left_text_nofoucs);
					setClickItem(holder.selectLayout);
				}
			}else{
				holder.selectLayout.setBackgroundResource(R.drawable.tab_none); 
			}
			return convertView; 
		}

		public class ViewHolder { 
		    public TextView textView;
		    public LinearLayout layout;
		    public LinearLayout selectLayout;
		} 

    }
	
	public void setClickItem(LinearLayout item){
		beClickItem = item;
	}
	public LinearLayout getClickItem(){
		return beClickItem;
	}
	
    public static void setTitleFocus(boolean enable) {
        if (enable) {
            mListView.setFocusable(true);
            mListView.requestFocus();
        } else {
            if (mListView.isFocusable()) {
                mListView.clearFocus();
                mListView.setFocusable(false);
            }
        }
    }
	 
    private void showDetails(int index) {
        
        ConfigFocus.Items_t = new ArrayList<ItemInfo>();
        ConfigFocus.Master = new ArrayList<ItemInfo>();
        mCurCheckPosition = index;
        // Settings.System.putInt(this.getContentResolver(), "curChoice",
        // index);
        Fragment fragment = getFragment(index);
        if (fragment != null) {
            SettingFragment sf = new SettingFragment(this);
            if(mSubChoice.equals("cctv-search")){
            	sf.setFragment(fragment, true);
            }else{
            	sf.setFragment(fragment, false);
            }
            mShownCheckPosition = index;
            mListViewAdapter.setSelectedPosition(index);
            mListViewAdapter.notifyDataSetChanged();
        }
    }
	
	private Fragment getFragment(int index){
		System.out.println(" getFragment  index = "+index);
		Fragment fragment = null;
        switch (index) {
            case 0:
                fragment = (Fragment) new ResolutionSettings();
                break;
            case 1:
            	if(mSubChoice.equals("cctv-search")){
            		fragment = (Fragment) new SearchMainFragment();
            		Bundle args = new Bundle();
            		args.putBoolean("isCCTVSearch", true);
            		fragment.setArguments(args);
            	}else{
            		fragment = (Fragment) new SearchMenuFragment();
            	}
                break;
            case 2:
                fragment = (Fragment) new DateSettings();
                break;
            case 3:
                fragment = (Fragment) new LanguageSettings();
                break;
            case 4:
                fragment = (Fragment) new Networksettings();
                break;
            case 5:
                fragment = (Fragment) new AudioOuputSettings();
                break;
            case 6:
                fragment = (Fragment) new MouseSettings();
                break;
            case 7:
                fragment = (Fragment) new InputSettings();
                break;
            case 8:
                fragment = (Fragment) new SystemUpdate();
                break;
            case 9:
                fragment = (Fragment) new ManageApplications(getWindowManager());
                break;
            case 10:
                fragment = (Fragment) new SystemInfo2();
                break;
            case 11:
                fragment = (Fragment) new CaMainFragment();
                break;
            case 12:
                fragment = (Fragment) new AppLockSettings();
                break;
            case 13:
                fragment = (Fragment) new ThemeSetting();
                break;
            case 14:
                if (mSubChoice.equals(REGISTER)) {
                    fragment = (Fragment) new AccountRegister();
                } else if (mSubChoice.equals(FORGETPASSWORD)) {
                    fragment = (Fragment) new AccountForgetPassword();
                } else {
                    int status = PsAuthenServiceL.getStatus(this);
                    if (status == PsAuthenServiceL.LENOVOUSER_OFFLINE) {
                        fragment = (Fragment) new AccountSetting();
                    } else if (status == PsAuthenServiceL.LENOVOUSER_ONLINE) {
                        fragment = (Fragment) new AccountLogined();
                    }
                }
                mSubChoice = "";
                break;
        }
		return fragment;
	}
	
	
    @Override
    public void onAttachedToWindow() {
//        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
        super.onAttachedToWindow();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	mSubChoice = "";
            FragmentManager fragmentManager = getFragmentManager();
            Log.e(TAG, "back stack = " + fragmentManager.getBackStackEntryCount());
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                Settings.System.putInt(this.getContentResolver(), "curChoice",
                        0);
                if (isShownText == true) {
                    Log.d("songwenxuan", "isShownText=true");
                    System.out.println("key back");
                    return true;
                }
                if (isProxy) {
                    Log.d("songwenxuan", "isProxy");
                    Log.e(TAG, "save network proxy setting!");
                    NetworkProxy networkProxy = (NetworkProxy) fragmentManager
                            .findFragmentById(R.id.setting_content_fragment);
                    if (!networkProxy.saveProxy()) {
                        return true;
                    }
                    isProxy = false;
                }
                Log.d("songwenxuan",
                        "backStackEntryCount = "
                                + fragmentManager.getBackStackEntryCount()
                                + "listViewFocus is " + mListView.isFocusable());
                if ((fragmentManager.getBackStackEntryCount() == 0)
                        && !mListView.isFocusable()) {
                    setTitleFocus(true);
                    Log.d("songwenxuan", "setTitleFocus(true)");
                    return true;
                }
                Log.d("songwenxuan", "default............");
            }
            if (keyCode == KeyEvent.KEYCODE_HOME) {
                Log.d(TAG, "KEYCODE_HOME is press !!!");
                Settings.System.putInt(this.getContentResolver(), "curChoice",
                        mCurCheckPosition);
                // this.finish();
                // android.os.Process.killProcess(android.os.Process.myPid());
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    
	@Override
	protected void onStart() {
		IntentFilter f = new IntentFilter();
        f.addAction(ThemeSetting.THEME_CHANGE_ACTION);
		registerReceiver(mBroadcastReceiver, new IntentFilter(f));
		super.onStart();
	}

	@Override
    protected void onStop() {
        Log.d(TAG, "-----------------onStop-----------");
        unregisterReceiver(mBroadcastReceiver);
//        try {
//            //For amlogic A18
//            SystemProperties.set("vplayer.hideStatusBar.enable","false");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        super.onStop();
    }
	
//	public static CityDataService getService(){
//		return mService;		
//	}
	
	@Override
	protected void onDestroy() {
	    Log.d(TAG, "-----------------onDestroy-----------");
		super.onDestroy();
	}

	public boolean getServiceState(String service) {
		final ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE); 
		List<RunningServiceInfo> services = activityManager.getRunningServices(100); 
		for(RunningServiceInfo serviceInfo : services) {
			if(serviceInfo.service.getPackageName().equals(service)) {
				return true;
			}
		}
		return false;
	}
	
	private void setImageVisibility(int pos) { // 1 up,0 down
//		if (pos < mArrayRes.length - 8)
//			mImageDown.setVisibility(View.VISIBLE);
//		if (pos == mArrayRes.length - 1)
//			mImageDown.setVisibility(View.INVISIBLE);
//		if (pos > 8)
//			mImageUp.setVisibility(View.VISIBLE);
//		if (pos == 0)
//			mImageUp.setVisibility(View.INVISIBLE);
	}

	
	BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(ThemeSetting.THEME_CHANGE_ACTION)){
				
			}
//				mMainLayout.setBackgroundDrawable(getThemePaper());
				
				//meth2 :getdrawble from wallpaper is quckly
				//mMainLayout.setBackgroundDrawable(getWallpaper());
		}
	};
	
    public Drawable getThemePaper() {
        String url = Settings.System.getString(this.getContentResolver(),
                ThemeSetting.THEME_URL);
        if (url != null && url.length() > 0) {
            File file = new File(url);
            if (file.exists()) {
                Options options = new Options();
                options.inJustDecodeBounds = false;
                options.inSampleSize = 4;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(url, options);
                Drawable drawable = new BitmapDrawable(bitmap);
                return drawable;
            }
        }
//        Bitmap bitmapapp = BitmapFactory.decodeResource(getResources(),
//                R.drawable.bk_ground);
        Drawable drawableapp = getResources().getDrawable(R.drawable.bk_ground);
        return drawableapp;
    }

    public void showFragment() {
        Log.d(TAG, "--------------showFragment----------- mCurCheckPosition = "
                + mCurCheckPosition);
        setImageVisibility(mCurCheckPosition);
        mListView.setSelection(mCurCheckPosition);
        showDetails(mCurCheckPosition);
        if(!mSubChoice.equals("cctv-search")){
        	mHandler.post(new Runnable() {
        		@Override
        		public void run() {
        			mListView.setFocusable(true);
        			mListView.setFocusableInTouchMode(true);
        			mListView.requestFocus();
        		}
        	});
        }else{
        	changeListViewBg(false);
        }
    }
    
    
    @Override
    public void finish() {
        Log.d(TAG, "-----------------finish-----------");
//        try {
//            //For amlogic A18
//            SystemProperties.set("vplayer.hideStatusBar.enable","false");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        super.finish();
    }
    @Override
    protected void onPause() {
        Log.d(TAG, "-----------------onPause-----------");
        super.onPause();
    }
}
