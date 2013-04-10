package com.lenovo.settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
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
import android.text.TextUtils;
import android.text.format.Formatter;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lenovo.settings.deviceinfo.StorageVolumeCategory;

public class SystemInfo2 extends Fragment {
	private static final String FILENAME_MSV = "/sys/board_properties/soc/msv";
	private static final String TAG = "SystemInfo";

	private View mView;
	private TextView mTextCpuType;
	private TextView mTextMemoryTotal;
	private TextView mTextHard;
	private TextView mTextMac;
	private TextView mTextAndroid;
	private TextView mTextHeart;
	private TextView mTextJoysee;
	private Button mButtonOpensource;
	
	private StorageManager mStorageManager = null;
	private Holder mHolder;
	private StorageVolumeCategory mInternalStorageVolumeCategory;
    private StorageVolumeCategory[] mStorageVolumeCategories;
    private long mLastSdFreeStorage;
	private long mLastFreeStorage;
	private StatFs mDataFileStats;
	private StatFs mSDCardFileStats;
	
//	private static final int REGISTER_LOADING_LICENSE_START = 30;
//	private static final int REGISTER_LOADING_LICENSE_STOP = 31;
//	private static final int REGISTER_LICENSE_DIALOG = 32;
//	private static final int MSG_HANDLER_EXIT = 1000;
//	
//	private RelativeLayout mLoadingLayout;
//	private AnimationDrawable mAnimationLoading ;//= new AnimationDrawable();
	
	// 动态加载
	private RelativeLayout mLoadingLayout;
	private AnimationDrawable mAnimationLoading;
	private Thread mThread = null;
	private Thread mWThread = null;
	public static final int STATUS_OK = 0;
	public static final int STATUS_NOT_FOUND = 1;
	public static final int STATUS_READ_ERROR = 2;
	public static final int STATUS_EMPTY_FILE = 3;
	public static final int STATUS_SHOW_LOADING = 4;
	public static final int STATUS_SHOW_TEXT = 5;
	public static final int STATUS_SHOW_MSG = 6;
	protected static final int MSG_HANDLER_EXIT = 1000;
	private boolean isExit = false;
	private boolean isShowText = false;
//	private LinearLayout mTextLayout;
	private Dialog AgreementDialog;
	
	private TextView mAgreement;
	private RelativeLayout mLicensesLayout;
	
	String mText; 
	private Handler mHandler = new Handler(){
		 @Override
	        public void handleMessage(Message msg) {
	            super.handleMessage(msg);
				if(isExit){
					Log.e(TAG,"mHandler exit");
					return;
				}
	            if (msg.what == STATUS_OK ) {
	            	if(mLoaddingDialog == null || !mLoaddingDialog.isShowing()){
	            		mButtonOpensource.setFocusable(true);
	            		LenovoSettingsActivity.isShownText = false;
	            		return;
	            	}
	            	InitAgreementDialog(mText);
	                hideLoadingView();
	                LenovoSettingsActivity.isShownText = false;
	                mButtonOpensource.setFocusable(true);
	            }else if (msg.what == STATUS_SHOW_LOADING){
	            	showLoadingView(getActivity().getResources().getString(R.string.Account_license_loading),true);
	            	mButtonOpensource.setFocusable(false);
	            }else if (msg.what == STATUS_SHOW_TEXT){
	            	mText = (String) msg.obj;
	                isShowText = false;
	                LenovoSettingsActivity.isShownText = true;
	            }else {
	                showErrorAndFinish();
	            }
	        }
		
	};
	
//    mLicensesLayout.setVisibility(View.VISIBLE);
//    mAgreement.requestFocus();
//	            	mAgreement.append(mText);
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mView = inflater.inflate(R.layout.system_info_new, container, false);
		mHolder = new Holder();
		mTextCpuType = (TextView) mView.findViewById(R.id.text_cpu_type);
		mTextMemoryTotal =  (TextView) mView.findViewById(R.id.memory_total);
		mTextHard =(TextView) mView.findViewById(R.id.text_hardware_version);
		mTextMac =(TextView) mView.findViewById(R.id.mac_info);
		mTextAndroid =(TextView) mView.findViewById(R.id.android_version);
		mTextHeart =(TextView) mView.findViewById(R.id.heart_version);
		mTextJoysee = (TextView) mView.findViewById(R.id.joysee_sofware_version);
		mButtonOpensource  = (Button) mView.findViewById(R.id.btn_opensource);
		mAgreement = (TextView) mView.findViewById(R.id.textViewLicens);
		mLicensesLayout = (RelativeLayout) mView.findViewById(R.id.licensLayout);
		mLicensesLayout.setVisibility(View.INVISIBLE);
		
		
		mLoadingLayout = (RelativeLayout) mView.findViewById(R.id.toastLayout);
		mHolder.textFlashTotal= (TextView) mView.findViewById(R.id.memory_flash);
		mHolder.textFlashUnused = (TextView) mView.findViewById(R.id.memory_flash_unuse);
		mHolder.textSdTotal= (TextView) mView.findViewById(R.id.memory_sdcard);
		mHolder.textSdUnused = (TextView) mView.findViewById(R.id.memory_sdcard_unuse);
		
		mTextHard.setText(Build.HARDWARE);
		mTextCpuType.setText(Build.CPU_ABI/*getCpuInfo()*/);
		mTextMemoryTotal.setText(getMemoryTotal()+"GB");
		mTextMac.setText(getMac());
		mTextAndroid.setText(Build.VERSION.RELEASE);
		mTextHeart.setText(getVersion());
		mTextJoysee.setText(Build.VERSION.INCREMENTAL);
		
		mDataFileStats = new StatFs("/data");
	    mSDCardFileStats = new StatFs(Environment.getExternalStorage2Directory().toString());
	    setStorageInfo(mView.getContext());
	    
	    isExit = false;
	    isShowText = false;
			
	    mButtonOpensource.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				
				mHandler.sendEmptyMessage(STATUS_SHOW_LOADING);
				LoadingThread();
				
//				ReadFile task = new ReadFile();
//				task.execute("source_permit.txt");
			}
		});
	    
	    mButtonOpensource.requestFocus();
		LenovoSettingsActivity.setTitleFocus(false);
		
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
//		isExit = true;
//		if(mThread!=null)
//			mThread.interrupt();
//			mThread = null;
	}
	
	


	@Override
	public void onDestroy() {
		super.onDestroy();
		isExit = true;
		if(mThread!=null){
			mThread.interrupt();
			mThread = null;
		}
	}


	@Override
	public void onStop() {
	    if (AgreementDialog != null && AgreementDialog.isShowing()) {
            AgreementDialog.dismiss();
        }
	    if (mLoaddingDialog != null && mLoaddingDialog.isShowing()) {
            mLoaddingDialog.dismiss();
        }
		super.onStop();
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
                        R.string.memory_sdcard_unuse, sizeStr));  
                
                String sdtotal = Formatter.formatShortFileSize(getActivity(), totalSdStorage);
                mHolder.textSdTotal.setText(getActivity().getResources().getString(
                        R.string.memory_sdcard_total, sdtotal));  
//                mSdProgressBar.setProgress(getScrollData(100,(totalSdStorage - freeSdStorage),totalSdStorage));
            }
        } else {
                mLastSdFreeStorage = -1;
                String sizeStr = Formatter.formatShortFileSize(getActivity(), 0);
//                mHolder.textSdUsed.setText(getActivity().getResources().getString(
//                        R.string.System_info_flash_used, sizeStr));   
                mHolder.textSdUnused.setText(getActivity().getResources().getString(
                        R.string.memory_sdcard_unuse, sizeStr));  
                String sdtotal = Formatter.formatShortFileSize(getActivity(), totalSdStorage);
                mHolder.textSdTotal.setText(getActivity().getResources().getString(
                        R.string.memory_sdcard_total, sdtotal));
//                mSdProgressBar.setProgress(0);
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
                        R.string.memory_flash_unuse, sizeStr));  
                String sdtotal = Formatter.formatShortFileSize(getActivity(), totalStorage);
                mHolder.textFlashTotal.setText(getActivity().getResources().getString(
                        R.string.memory_flash_total, sdtotal));
//                mFlashProgressBar.setProgress(getScrollData(100,(totalStorage - freeStorage),totalStorage));  
            }
        } else {
            /*if (mLastFreeStorage != -1)*/ {
                mLastFreeStorage = -1;
                String sizeStr = Formatter.formatShortFileSize(getActivity(), 0);
//                mHolder.textFlashUsed.setText(getActivity().getResources().getString(
//                        R.string.System_info_flash_used, sizeStr));   
                sizeStr = Formatter.formatShortFileSize(getActivity(), 0);  
                mHolder.textFlashUnused.setText(getActivity().getResources().getString(
                        R.string.memory_flash_unuse, sizeStr)); 
                String sdtotal = Formatter.formatShortFileSize(getActivity(), totalStorage);
                mHolder.textFlashTotal.setText(getActivity().getResources().getString(
                        R.string.memory_flash_total, sdtotal));
//                mFlashProgressBar.setProgress(0); 
            }
        }
       // mHolder.layoutFlash.addView(getDrewView(getActivity(),freeStorage,totalStorage)); 
    }
	

	// 获取CPU信息
	public String getCpuInfo() {
		String str1 = "/proc/cpuinfo";
		String str2 = "";
		String[] cpuInfo = { "", "" };
		String[] arrayOfString;
		try {
			FileReader fr = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
			str2 = localBufferedReader.readLine();
			arrayOfString = str2.split("\\s+");
			for (int i = 2; i < arrayOfString.length; i++) {
				cpuInfo[0] = cpuInfo[0] + arrayOfString[i] + " ";
			}
			str2 = localBufferedReader.readLine();
			arrayOfString = str2.split("\\s+");
			cpuInfo[1] += arrayOfString[2];
			localBufferedReader.close();
		} catch (IOException e) {
		}
		return cpuInfo[0];
	}
	
	public class Holder { 
		public TextView textFlashTotal;
		public TextView textSdTotal;
		public TextView textFlashUsed;
		public TextView textFlashUnused;
		public TextView textSdUsed;
		public TextView textSdUnused;
		public LinearLayout layoutFlash;
		public LinearLayout layoutSd;
	} 
	
	//获取cpu使用率
	public String getMemoryTotal() {
		DecimalFormat    df   = new DecimalFormat("##0.0");   
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		float blockSize = (float)stat.getBlockSize();
		float totalBlocks = (float)stat.getBlockCount();
		float totalMemory = totalBlocks * blockSize/1024/1024/1024;
		String str = df.format(totalMemory);
		return str;
	}

	// get mac address
	private String getMac() {
		String mac;
		if (SystemProperties.get("ubootenv.var.ethaddr", "") != null) {
			mac = SystemProperties.get("ubootenv.var.ethaddr", "");
		} else {
			WifiManager wifi = (WifiManager) getActivity().getSystemService(
					Context.WIFI_SERVICE);
			WifiInfo info = wifi.getConnectionInfo();
			mac = info.getMacAddress();
			System.out.println("mac/物理地址:" + info.getMacAddress());
		}
		return mac;
	}

	// get version
	public String getVersion() {
		String[] version = { "null", "null", "null", "null", "null", "null" };
		String str1 = "/proc/version";
		String str2;
		String[] arrayOfString;
		try {
			FileReader localFileReader = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(
					localFileReader, 8192);
			str2 = localBufferedReader.readLine();
			arrayOfString = str2.split("\\s+");
			version[0] = arrayOfString[2];// KernelVersion
			localBufferedReader.close();
		} catch (IOException e) {
		}
		return version[0];
	}
	
	private Thread mAgreementThread;
	String text = null;	
	int size = 0;
	void LoadingThread(){
		mThread = new Thread(){

			@Override
			public void run() {
				int status = STATUS_OK;
				String data_str = new String();
				InputStream inputStream = null;
				try {
					inputStream = getActivity().getAssets().open("source_permit.txt");
					size = inputStream.available();
	                byte[] buffer = new byte[size];
	                int numRead;
	                while(((numRead = inputStream.read(buffer)) >= 0) && (!isExit)){
	                	data_str = new String(buffer, "UTF-8");
	                	isShowText = true;
	                	Message msg = mHandler.obtainMessage(STATUS_SHOW_TEXT, null);
	                	msg.obj = data_str;
	                	mHandler.sendMessage(msg);
	                	while(isShowText && (!isExit)){
		                    try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
							}
	                	}
	                    try {
							Thread.sleep(1000);
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
	
	void writtingThread(){
		mWThread = new Thread(){
			@Override
			public void run() {
				super.run();
				while(true){
//					 text_msg.append(mText);
//					 Message msg = mHandler.obtainMessage(STATUS_SHOW_TEXT, null);
//	                mHandler.sendMessage(msg);
				}
			}
		};
		mWThread.start();
	}
	
	private Dialog mLoaddingDialog = null;
	
	private void showLoadingView(String msg, boolean hasLoading){	
//		TextView textView = (TextView)mLoadingLayout.findViewById(R.id.toast_text);
//		ImageView imgLicense = (ImageView)mLoadingLayout.findViewById(R.id.toastLicenseImage);
//		textView.setText(msg);
//		mLoadingLayout.setVisibility(View.VISIBLE);
//		if(hasLoading){
//			imgLicense.setVisibility(View.VISIBLE);
//			mAnimationLoading = (AnimationDrawable)imgLicense.getBackground();
//			System.out.println("showLoadingView isrunning:"+!mAnimationLoading.isRunning());
//			if(!mAnimationLoading.isRunning()){
//				mAnimationLoading.start();
//			}
//		}else{
//			imgLicense.setVisibility(View.INVISIBLE);
//		}
		
		mLoaddingDialog = new Dialog(getActivity(),R.style.DialogStyle);
		mLoaddingDialog.setContentView(R.layout.system_alert_dialog);
		TextView text = (TextView) mLoaddingDialog.findViewById(R.id.msg);
        ImageView imageView = (ImageView) mLoaddingDialog.findViewById(R.id.animation_view);
        AnimationDrawable animation = (AnimationDrawable) imageView.getBackground();
        animation.start();
        text.setText(msg);
        mLoaddingDialog.setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				mLoaddingDialog = null;
			}
    	});
    	mLoaddingDialog.show();
	}
	
	private void hideLoadingView(){
//		if(mAnimationLoading.isRunning()){
//			mAnimationLoading.stop();
//		}
//		mLoadingLayout.setVisibility(View.INVISIBLE);
		
		if(mLoaddingDialog!=null){
			mLoaddingDialog.cancel();
			mLoaddingDialog = null;
		}
	}
	/*String title*/
    private void InitAgreementDialog(String msg) {
        if (AgreementDialog == null) {
            TextView text_msg = null, text_title = null;
            AgreementDialog = new Dialog(mView.getContext(),
                    R.style.MiddleDialogStyle);
            AgreementDialog.setContentView(R.layout.system_info_open_source_dialog);
            text_title = (TextView) AgreementDialog
                    .findViewById(R.id.textview_title);
            text_msg = (TextView) AgreementDialog.findViewById(R.id.textView1);
            text_msg.setTextColor(0xffbbbbbb);
            text_msg.setMovementMethod(ScrollingMovementMethod.getInstance());
            text_title.setText(getActivity().getResources().getString(
                    R.string.System_low_license));
            text_msg.setText(msg);
        }
        AgreementDialog.show();
    }	
	
	 private void showErrorAndFinish() {
	    	Log.e(TAG,"showErrorAndFinish");
	    	hideLoadingView();
	    	ToastView totast = new ToastView(getActivity(),getString(R.string.settings_license_activity_unavailable),2000,false);
			totast.showTotast();
	}
	 

}
