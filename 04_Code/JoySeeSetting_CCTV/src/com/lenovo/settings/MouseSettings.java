package com.lenovo.settings;


import android.app.Fragment;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.IWindowManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

public class MouseSettings extends Fragment implements SeekBar.OnSeekBarChangeListener{
	protected static final String TAG = "MouseSettings";
	private View mView;
	private SeekBar mSeekBar;
	private int mOldSpeed;
	private boolean mRestoredOldState;

	private boolean mTouchInProgress;

	private static final int MIN_SPEED = -7;
	private static final int MAX_SPEED = 7;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		
		mView = inflater.inflate(R.layout.mouse_setting, container, false);
		mSeekBar = (SeekBar) mView.findViewById(R.id.progressbar_mouse);
		
		
		//mSeekBar.setOnSeekBarChangeListener(this);
		
		LenovoSettingsActivity.setTitleFocus(false);
		mSeekBar.requestFocus();
		return mView;
	}

	@Override
	public void onStart() {
        mSeekBar.setMax(MAX_SPEED - MIN_SPEED);
        mOldSpeed = getSpeed(0);
        mSeekBar.setProgress(mOldSpeed - MIN_SPEED);
        mSeekBar.setOnSeekBarChangeListener(this);
		super.onStart();
	}
	
	

	@Override
	public void onStop() {
		final ContentResolver resolver = getActivity().getContentResolver();

        //restoreOldState();

        resolver.unregisterContentObserver(mSpeedObserver);
        
        setMouseInVisible();
		super.onStop();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
		final ContentResolver resolver = getActivity().getContentResolver();
		
		 if (!mTouchInProgress) {
	            setSpeed(progress + MIN_SPEED);
	      }
		 getActivity().getContentResolver().registerContentObserver(
	                Settings.System.getUriFor(Settings.System.POINTER_SPEED), true,
	                mSpeedObserver);
		 
		 Settings.System.putInt(resolver, Settings.System.POINTER_SPEED,
                 mSeekBar.getProgress() + MIN_SPEED);
		 
	     mRestoredOldState = false;
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		mTouchInProgress = true;
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		 mTouchInProgress = false;
	     setSpeed(seekBar.getProgress() + MIN_SPEED);
	}
	
	private void onSpeedChanged() {
        int speed = getSpeed(0);
        mSeekBar.setProgress(speed - MIN_SPEED);
    }
	
	private void restoreOldState() {
	        if (mRestoredOldState) return;

	        setSpeed(mOldSpeed);
	        mRestoredOldState = true;
	 }
	 
	 private void setSpeed(int speed) {
	        try {
	            IWindowManager wm = IWindowManager.Stub.asInterface(
	                    ServiceManager.getService("window"));
	            if (wm != null) {
	                wm.setPointerSpeed(speed);
	            }
	        } catch (RemoteException e) {
	      }
	  }
	 
	 private int getSpeed(int defaultValue) {
	        int speed = defaultValue;
	        try {
	            speed = Settings.System.getInt(getActivity().getContentResolver(),
	                    Settings.System.POINTER_SPEED);
	        } catch (SettingNotFoundException snfe) {
	        }
	        return speed;
	    }
	 private ContentObserver mSpeedObserver = new ContentObserver(new Handler()) {
	        @Override
	        public void onChange(boolean selfChange) {
	            onSpeedChanged();
	        }
	    };
	    
	 private void setMouseInVisible(){
//		 WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
//		 wm.setMouseStatus(false);
//		 wm.setMouseCursorType(MOUSE_CURSOR_NONE);
	 }
	 
	 private void setMouseVisible(){
//		 WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
//		 wm.setMouseStatus(true);
//		 wm.setMouseCursorType(MOUSE_CURSOR_OSD2);
	 }
}