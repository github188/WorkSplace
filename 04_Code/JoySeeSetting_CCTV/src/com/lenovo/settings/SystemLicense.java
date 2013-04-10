package com.lenovo.settings;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import android.app.Fragment;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SystemLicense extends Fragment{
	
	
	
	
	private static final String TAG = "SystemLicense";

    private static final String DEFAULT_LICENSE_PATH = "/system/etc/NOTICE.html.gz";
    private static final String PROPERTY_LICENSE_PATH = "ro.config.license_path";
    public static final int STATUS_OK = 0;
    public static final int STATUS_NOT_FOUND = 1;
    public static final int STATUS_READ_ERROR = 2;
    public static final int STATUS_EMPTY_FILE = 3;
    public static final int STATUS_SHOW_LOADING = 4;
    public static final int STATUS_SHOW_TEXT = 5;
	protected static final int MSG_HANDLER_EXIT = 1000;

	
	private View mView;
	private WebView mWebView;
	private AnimationDrawable mAnimationLoading;
	private RelativeLayout mRelative;
	private RelativeLayout mLoadingLayout;
	private Toast mToast;
	private TextView mTextView;
	private Thread mThread = null;
	private boolean isExit = false;
	private boolean isShowText = false;
	
	private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

			if(isExit){
				Log.e(TAG,"mHandler exit");
				return;
			}
            if (msg.what == STATUS_OK) {
                String text = (String) msg.obj;
                hideLoadingView();
                //mTextView.setText(text);
                mTextLayout.setVisibility(View.VISIBLE);
                mTextView.requestFocus();
                //showPageOfText(text);
            }else if (msg.what == STATUS_SHOW_LOADING){
        		showLoadingView(getActivity().getString(
        				R.string.settings_license_activity_loading),true);
            }else if (msg.what == STATUS_SHOW_TEXT){
                String text = (String) msg.obj;
            	mTextView.append(text);
            	isShowText = false;
            }else {
                showErrorAndFinish();
            }
        }
    };

	private LinearLayout mTextLayout;

/*
	private class LicenseFileLoader implements Runnable {

        private static final String INNER_TAG = "SettingsLicenseActivity.LicenseFileLoader";
        public static final int STATUS_OK = 0;
        public static final int STATUS_NOT_FOUND = 1;
        public static final int STATUS_READ_ERROR = 2;
        public static final int STATUS_EMPTY_FILE = 3;

        private String mFileName;
        private Handler mHandler;

        public LicenseFileLoader(String fileName, Handler handler) {
            mFileName = fileName;
            mHandler = handler;
        }

        public void run() {

            int status = STATUS_OK;

            InputStreamReader inputReader = null;
            StringBuilder data = new StringBuilder(2048);
            try {
                char[] tmp = new char[2048];
                int numRead;
                if (mFileName.endsWith(".gz")) {
                    inputReader = new InputStreamReader(
                        new GZIPInputStream(new FileInputStream(mFileName)));
                } else {
                    inputReader = new FileReader(mFileName);
                }

                while ((numRead = inputReader.read(tmp)) >= 0) {
                    data.append(tmp, 0, numRead);
                }
            } catch (FileNotFoundException e) {
                Log.e(INNER_TAG, "License HTML file not found at " + mFileName, e);
                status = STATUS_NOT_FOUND;
            } catch (IOException e) {
                Log.e(INNER_TAG, "Error reading license HTML file at " + mFileName, e);
                status = STATUS_READ_ERROR;
            } finally {
                try {
                    if (inputReader != null) {
                        inputReader.close();
                    }
                } catch (IOException e) {
                }
            }

            if ((status == STATUS_OK) && TextUtils.isEmpty(data)) {
                Log.e(INNER_TAG, "License HTML is empty (from " + mFileName + ")");
                status = STATUS_EMPTY_FILE;
            }

            // Tell the UI thread that we are finished.
            Message msg = mHandler.obtainMessage(status, null);
            if (status == STATUS_OK) {
                msg.obj = data.toString();
            }
            mHandler.sendMessage(msg);
        }
    }
*/
	private class LicenseFileLoader implements Runnable {
		

		@Override
		public void run() {
	
			int status = STATUS_OK;
			String data = null;
			try {
				InputStream is = getActivity().getAssets().open("source_permit.txt");
				int size = is.available();
				byte[] buffer = new byte[size];
				is.read(buffer);
				is.close();
				data = new String(buffer, "UTF-8");
			} catch (IOException e) {
				
				//e.printStackTrace();
				status = STATUS_EMPTY_FILE;
			}
            if ((status == STATUS_OK) && TextUtils.isEmpty(data)) {
                Log.e(TAG, "License is empty ");
                status = STATUS_EMPTY_FILE;
            }

            // Tell the UI thread that we are finished.
            Message msg = mHandler.obtainMessage(status, null);
            if (status == STATUS_OK) {
                msg.obj = data.toString();
            }
            mHandler.sendMessage(msg);
		}
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {


		mView = inflater.inflate(R.layout.system_license, container, false);
		mRelative = (RelativeLayout) inflater.inflate(R.layout.toast_info, container, false);
		mLoadingLayout = (RelativeLayout) mView.findViewById(R.id.toastLayout);	
		mTextLayout = (LinearLayout) mView.findViewById(R.id.LayoutTextView);
        mTextLayout.setVisibility(View.INVISIBLE);
		isExit = false;
		isShowText = false;

		mTextView = (TextView) mView.findViewById(R.id.license_textView);
		mTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
		mTextView.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
		
				if(hasFocus){
					Selection.setSelection((Spannable) mTextView.getText(), 0);
				}
			}
			
		});
		mTextView.setText("");
		mHandler.sendEmptyMessage(STATUS_SHOW_LOADING);
        //Thread thread = new Thread(new LicenseFileLoader());
       // thread.start();
		LoadingThread();
		return mView;
	}
	
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
					inputStream = getActivity().getAssets().open("source_permit.txt");
	                char[] tmp = new char[2048];
	                byte[] buffer = new byte[2048];
	                int numRead;
	                inputReader = new InputStreamReader(inputStream);
	                /*while (((numRead = inputReader.read(tmp)) >= 0) && (!isExit) ) {
	                	//StringBuilder data = new StringBuilder(2048);
	                    //data.append(tmp, 0, numRead);
	                	data_str = new String(tmp);
	                	Message msg = mHandler.obtainMessage(STATUS_SHOW_TEXT, null);
	                	msg.obj = data_str;
	                	mHandler.sendMessage(msg);
	                	
	                    try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
						}
	                }*/
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

    private void showErrorAndFinish() {
    	Log.e(TAG,"showErrorAndFinish");
    	hideLoadingView();
        showToastView(getActivity().getString
        		(R.string.settings_license_activity_unavailable),false);
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
	
	void hideLoadingView(){
		if(mAnimationLoading.isRunning()){
			mAnimationLoading.stop();
		}
		mLoadingLayout.setVisibility(View.INVISIBLE);
	}
	
	void showToastView( String msg, boolean hasLoading){ 
		TextView textView = (TextView)mRelative.findViewById(R.id.toast_text);
		ImageView img = (ImageView)mRelative.findViewById(R.id.toastImage);
		textView.setText(msg);
		img.setVisibility(View.GONE);
		mToast = new Toast(getActivity());
		mToast.setView(mRelative);
		mToast.setDuration(Toast.LENGTH_SHORT);
		mToast.setGravity(Gravity.CENTER, 0, 0);
		mToast.show();		
	}
	

	@Override
	public void onPause() {

		super.onPause();
		isExit = true;
		mThread.interrupt();
		mThread = null;
	}

}
