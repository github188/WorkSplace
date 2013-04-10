package com.lenovo.settings;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnFocusChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lenovo.settings.deviceinfo.StorageVolumeCategory;

public class SystemInfo extends Fragment {

    private static final String FILENAME_MSV = "/sys/board_properties/soc/msv";
	private static final String TAG = "SystemInfo";
    // Updates the memory usage bar graph.
	private View mView;
	private TextView mTextHard;
	private TextView mTextModel;
	private TextView mTextSoft;
	private TextView mTextOS;
	private TextView mTextCpu;
	private TextView mTextMac;
	private TextView mAgreement;
	
	private StorageManager mStorageManager = null;
	private Holder mHolder;
	private StorageVolumeCategory mInternalStorageVolumeCategory;
    private StorageVolumeCategory[] mStorageVolumeCategories;
	private StatFs mDataFileStats;
	private StatFs mSDCardFileStats;
	private long mLastSdFreeStorage;
	private long mLastFreeStorage;
	private ProgressBar mFlashProgressBar;
	private ProgressBar mSdProgressBar;
	
	//动态加载
	private RelativeLayout mLoadingLayout;
	private AnimationDrawable mAnimationLoading;
	private Thread mThread = null;
	public static final int STATUS_OK = 0;
    public static final int STATUS_NOT_FOUND = 1;
    public static final int STATUS_READ_ERROR = 2;
    public static final int STATUS_EMPTY_FILE = 3;
    public static final int STATUS_SHOW_LOADING = 4;
    public static final int STATUS_SHOW_TEXT = 5;
	protected static final int MSG_HANDLER_EXIT = 1000;
	private boolean isExit = false;
	private boolean isShowText = false;
	private LinearLayout mTextLayout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mView = inflater.inflate(R.layout.system_info, container, false);
		mHolder = new Holder();
		mFlashProgressBar = (ProgressBar) mView.findViewById(R.id.progressbar_flash);		
		mSdProgressBar = (ProgressBar) mView.findViewById(R.id.progressbar_sdcard);	
		mTextModel = (TextView) mView.findViewById(R.id.text_model);
		mTextHard = (TextView) mView.findViewById(R.id.text_hardware);
		mTextSoft = (TextView) mView.findViewById(R.id.text_software);
		mTextOS = (TextView) mView.findViewById(R.id.text_os_version);
		mTextMac = (TextView) mView.findViewById(R.id.text_mac);
		mTextCpu = (TextView) mView.findViewById(R.id.text_cpu_use);
		mAgreement = (TextView) mView.findViewById(R.id.agreement_textView);
		mLoadingLayout = (RelativeLayout) mView.findViewById(R.id.toastLayout);
		mTextLayout = (LinearLayout) mView.findViewById(R.id.LayoutTextView);
        mTextLayout.setVisibility(View.INVISIBLE);
        isExit = false;
		isShowText = false;
		
//		mHolder.textFlashUsed = (TextView) mView.findViewById(R.id.text_flase_used);
		mHolder.textFlashUnused = (TextView) mView.findViewById(R.id.text_flash_unused);
//		mHolder.textSdUsed = (TextView) mView.findViewById(R.id.text_sd_used);
		mHolder.textSdUnused = (TextView) mView.findViewById(R.id.text_sd_unused);
//		mHolder.layoutFlash = (LinearLayout) mView.findViewById(R.id.layout_flash_used);
//		mHolder.layoutSd = (LinearLayout) mView.findViewById(R.id.layout_sd_used);
		mTextModel.setText(Build.MODEL + getMsvSuffix());
		mTextHard.setText(Build.HARDWARE);
		mTextSoft.setText(Build.VERSION.INCREMENTAL);
		mTextOS.setText(Build.VERSION.RELEASE);
		mTextMac.setText(getMac());
		mTextCpu.setText(getUseCPu());
		//mAgreement.setText(getAgreeMent());
		
		mAgreement.setMovementMethod(ScrollingMovementMethod.getInstance());
		mAgreement.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
		
				if(hasFocus){
					Selection.setSelection((Spannable) mAgreement.getText(), 0);
				}
			}
			
		});
		
        mDataFileStats = new StatFs("/data");
        mSDCardFileStats = new StatFs(Environment.getExternalStorage2Directory().toString());
		setStorageInfo(mView.getContext());
		LenovoSettingsActivity.setTitleFocus(false);
		mAgreement.requestFocus();
		
		mHandler.sendEmptyMessage(STATUS_SHOW_LOADING);
		LoadingThread();
		
		return mView;
	}	

	@Override
	public void onResume() {

		super.onResume();
		updateStorageUsage();
	}
	
	@Override
	public void onPause() {

		super.onPause();
		isExit = true;
		mThread.interrupt();
		mThread = null;
	}

	private void setStorageInfo(Context context) {

		updateStorageUsage();
	}
	void updateStorageUsage() {

        long freeStorage = 0;
        long totalStorage = 0;
        long freeSdStorage = 0;
        long totalSdStorage = 0;

        mSDCardFileStats.restat(Environment.getExternalStorage2Directory().toString());
        try {
            totalSdStorage = (long)mSDCardFileStats.getBlockCount() *
                    mSDCardFileStats.getBlockSize();
            freeSdStorage = (long) mSDCardFileStats.getAvailableBlocks() *
            mSDCardFileStats.getBlockSize();
        } catch (IllegalArgumentException e) {
        }
        mDataFileStats.restat("/data");
        try {
            totalStorage = (long)mDataFileStats.getBlockCount() *
                    mDataFileStats.getBlockSize();
            freeStorage = (long) mDataFileStats.getAvailableBlocks() *
                mDataFileStats.getBlockSize();
        } catch (IllegalArgumentException e) {
        }

        if (totalSdStorage > 0) {
            /*if (mLastSdFreeStorage != freeSdStorage)*/ {
                mLastSdFreeStorage = freeSdStorage;
                String sizeStr = Formatter.formatShortFileSize(getActivity(), (totalSdStorage -freeSdStorage));
//                mHolder.textSdUsed.setText(getActivity().getResources().getString(
//                        R.string.System_info_flash_used, sizeStr));   
                sizeStr = Formatter.formatShortFileSize(getActivity(), freeSdStorage );  
                mHolder.textSdUnused.setText(getActivity().getResources().getString(
                        R.string.System_info_flash_unused, sizeStr));  
                mSdProgressBar.setProgress(getScrollData(100,(totalSdStorage - freeSdStorage),totalSdStorage));
            }
        } else {
                mLastSdFreeStorage = -1;
                String sizeStr = Formatter.formatShortFileSize(getActivity(), 0);
//                mHolder.textSdUsed.setText(getActivity().getResources().getString(
//                        R.string.System_info_flash_used, sizeStr));   
                mHolder.textSdUnused.setText(getActivity().getResources().getString(
                        R.string.System_info_flash_unused, sizeStr));  
                mSdProgressBar.setProgress(0);
        }
       // mHolder.layoutSd.addView(getDrewView(getActivity(),freeSdStorage,totalSdStorage)); 
        
        if (totalStorage > 0) {
            /*if (mLastFreeStorage != freeStorage)*/ {
                mLastFreeStorage = freeStorage;
                String sizeStr = Formatter.formatShortFileSize(getActivity(), (totalStorage - freeStorage));
//                mHolder.textFlashUsed.setText(getActivity().getResources().getString(
//                        R.string.System_info_flash_used, sizeStr));   
                sizeStr = Formatter.formatShortFileSize(getActivity(), freeStorage);  
                mHolder.textFlashUnused.setText(getActivity().getResources().getString(
                        R.string.System_info_flash_unused, sizeStr));  
                mFlashProgressBar.setProgress(getScrollData(100,(totalStorage - freeStorage),totalStorage));  
            }
        } else {
            /*if (mLastFreeStorage != -1)*/ {
                mLastFreeStorage = -1;
                String sizeStr = Formatter.formatShortFileSize(getActivity(), 0);
//                mHolder.textFlashUsed.setText(getActivity().getResources().getString(
//                        R.string.System_info_flash_used, sizeStr));   
                sizeStr = Formatter.formatShortFileSize(getActivity(), 0);  
                mHolder.textFlashUnused.setText(getActivity().getResources().getString(
                        R.string.System_info_flash_unused, sizeStr)); 
                mFlashProgressBar.setProgress(0); 
            }
        }
       // mHolder.layoutFlash.addView(getDrewView(getActivity(),freeStorage,totalStorage)); 
    }
	private View getDrewView(Activity activity, long availSize,
			long totalSize) {

		DrewView view;
    	float angle = 0;
    	if((totalSize <= 0) || (availSize < 0) || (availSize > totalSize)){
    		angle = 360;
    	}else{
        	angle = (float) ((360 * availSize) / totalSize);    		
    	}
    	Log.d(TAG,"angle = "+angle);
    	view = new DrewView(activity,0,angle);
    	view.setMinimumHeight(500);
		view.setMinimumWidth(500);
		//repaint view components
		view.invalidate();
		view.setRotationY(180);
    	return view;  
	}

	/**
     * Reads a line from the specified file.
     * @param filename the file to read from
     * @return the first line, if any.
     * @throws IOException if the file couldn't be read
     */
    private String readLine(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename), 256);
        try {
            return reader.readLine();
        } finally {
            reader.close();
        }
    }

    /**
     * Returns " (ENGINEERING)" if the msv file has a zero value, else returns "".
     * @return a string to append to the model number description.
     */
    private String getMsvSuffix() {
        // Production devices should have a non-zero value. If we can't read it, assume it's a
        // production device so that we don't accidentally show that it's an ENGINEERING device.
        try {
            String msv = readLine(FILENAME_MSV);
            // Parse as a hex number. If it evaluates to a zero, then it's an engineering build.
            if (Long.parseLong(msv, 16) == 0) {
                return " (ENGINEERING)";
            }
        } catch (IOException ioe) {
            // Fail quietly, as the file may not exist on some devices.
        } catch (NumberFormatException nfe) {
            // Fail quietly, returning empty string should be sufficient
        }
        return "";
    }

	public class Holder { 
		public TextView textFlashUsed;
		public TextView textFlashUnused;
		public TextView textSdUsed;
		public TextView textSdUnused;
		public LinearLayout layoutFlash;
		public LinearLayout layoutSd;
	} 
	private int getScrollData(double scroll_max,double data,double data_max){
		int scroll_data = 0;
    	if(data_max <= 0){
    		scroll_data = (int) scroll_max;
    	}else{
    		scroll_data = (int) ((scroll_max * (data)) / data_max);    		
    	}
		return scroll_data;
	}
	//获取有限mac地址
	private String getMac(){
		String mac;
		if(SystemProperties.get("ubootenv.var.ethaddr","")!=null){
			mac = SystemProperties.get("ubootenv.var.ethaddr","");
		}else{
			WifiManager wifi = (WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);
			WifiInfo info = wifi.getConnectionInfo();   
			mac = info.getMacAddress();
			System.out.println("mac/物理地址:"+info.getMacAddress());
		}
		return mac; 
	}
	//获取cpu使用率
	public String getUseCPu() {
			String str1 = "/proc/stat";
			String str2="";
			String cpus[];
			long total = 0;
			long idlspu = 0; //空闲
			double usr = 0;
			FileReader fr;
			try {
				fr = new FileReader(str1);
				BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
				str2 = localBufferedReader.readLine();
				cpus =str2.split("\\s+");
				for(int i = 1;i<cpus.length;i++){
					total += Long.parseLong(cpus[i]);
				}
				idlspu = Long.parseLong(cpus[4]);
				usr =100 * (total - idlspu)/total;
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			return usr+"%";
		}
	
	//获取用户协议
	public String getAgreeMent() {
		String text = "";
		try {
			InputStream is = getActivity().getAssets().open("source_permit.txt");
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			text = new String(buffer, "UTF-8");
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		return text;
	} 
	
	private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

			if(isExit){
				Log.e(TAG,"mHandler exit");
				return;
			}
            if (msg.what == STATUS_OK) {
            	System.out.println("STATUS_OK--->");
                hideLoadingView();
                mTextLayout.setVisibility(View.VISIBLE);
                mAgreement.requestFocus();
            }else if (msg.what == STATUS_SHOW_LOADING){
            	System.out.println("STATUS_SHOW_LOADING--->");
        		showLoadingView(getActivity().getString(
        				R.string.settings_license_activity_loading),true);
            }else if (msg.what == STATUS_SHOW_TEXT){
            	System.out.println("STATUS_SHOW_TEXT--->");
                String text = (String) msg.obj;
                mAgreement.append(text);
            	isShowText = false;
            }else {
               // showErrorAndFinish();
            }
        }
    };
    
	void LoadingThread(){
		mThread = new Thread(){

			@Override
			public void run() {
				int status = STATUS_OK;
				//String data = null;
				String data_str = new String();
				//StringBuilder data = new StringBuilder(2048);
				InputStream inputStream = null;
				InputStreamReader inputReader = null;
				try {
					inputStream = getActivity().getAssets().open("agreement.txt");
	                char[] tmp = new char[2048];
	                byte[] buffer = new byte[2048];
	                int numRead;
	                inputReader = new InputStreamReader(inputStream);
	                while(((numRead = inputStream.read(buffer)) >= 0) && (!isExit)){
	                	data_str = new String(buffer, "UTF-8");
	                	isShowText = true;
	                	Message msg = mHandler.obtainMessage(STATUS_SHOW_TEXT, null);
	                	msg.obj = data_str;
	                	mHandler.sendMessage(msg);
	                	while(isShowText && (!isExit)){
		                    try {
								Thread.sleep(10);
							} catch (InterruptedException e) {
							}
	                	}
	                    try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
						}
	                }
				} catch (IOException e) {
					
					status = STATUS_EMPTY_FILE;
				} finally {
	                try {
						if(inputStream != null){
							inputStream.close();
						}
						if(inputReader != null){
							inputReader.close();
						}
	                } catch (IOException e) {
	                }
				}
	            if ((status == STATUS_OK) && TextUtils.isEmpty(data_str)) {
	                Log.e(TAG, "License is empty ");
	                status = STATUS_EMPTY_FILE;
	            }

	            // Tell the UI thread that we are finished.
	            Message msg = mHandler.obtainMessage(status, null);
	            if (status == STATUS_OK) {
	                //msg.obj = data.toString();
	            	//msg.obj = data_str;
	            }
	            mHandler.sendMessage(msg);
			}
			
		};
		mThread.start();
	}
	
	void hideLoadingView(){
		if(mAnimationLoading.isRunning()){
			mAnimationLoading.stop();
		}
		mLoadingLayout.setVisibility(View.INVISIBLE);
	}
	void showLoadingView(String msg, boolean hasLoading){	
		TextView textView = (TextView)mLoadingLayout.findViewById(R.id.toast_text);
		ImageView img = (ImageView)mLoadingLayout.findViewById(R.id.toastImage);
		textView.setText(msg);
		if(hasLoading){
			img.setVisibility(View.VISIBLE);
			mAnimationLoading = (AnimationDrawable)img.getBackground();
			if(!mAnimationLoading.isRunning()){
				mAnimationLoading.start();
			}
		}else{
			img.setVisibility(View.INVISIBLE);
		}
		mLoadingLayout.setVisibility(View.VISIBLE);
	}
}
