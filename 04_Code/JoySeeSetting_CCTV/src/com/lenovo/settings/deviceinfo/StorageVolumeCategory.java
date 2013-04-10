package com.lenovo.settings.deviceinfo;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.format.Formatter;
import android.util.Log;

import com.lenovo.settings.DrewView;
import com.lenovo.settings.R;
import com.lenovo.settings.SystemInfo.Holder;
import com.lenovo.settings.deviceinfo.StorageMeasurement.MeasurementReceiver;

public class StorageVolumeCategory implements MeasurementReceiver {

	private static final String TAG = "StorageVolumeCategory";
    static final int TOTAL_SIZE = 0;
    static final int APPLICATIONS = 1;
    static final int DCIM = 2; // Pictures and Videos
    static final int MUSIC = 3;
    static final int DOWNLOADS = 4;
    static final int MISC = 5;
    static final int AVAILABLE = 6;
    
    static final int MEMORY_FLASH = 0;
    static final int MEMORY_SD = 1;

    private StorageVolume mStorageVolume;

    private StorageManager mStorageManager = null;

    private StorageMeasurement mMeasurement;

    private boolean mAllowFormat;
    
	private Holder mHolder;
	private Context mContext;

    


    public static final Set<String> sPathsExcludedForMisc = new HashSet<String>();

    static class MediaCategory {
        final String[] mDirPaths;
        final int mCategory;
        //final int mMediaType;

        public MediaCategory(int category, String... directories) {
            mCategory = category;
            final int length = directories.length;
            mDirPaths = new String[length];
            for (int i = 0; i < length; i++) {
                final String name = directories[i];
                final String path = Environment.getExternalStoragePublicDirectory(name).
                        getAbsolutePath();
                mDirPaths[i] = path;
                sPathsExcludedForMisc.add(path);
            }
        }
    }

    static final MediaCategory[] sMediaCategories = new MediaCategory[] {
        new MediaCategory(DCIM, Environment.DIRECTORY_DCIM, Environment.DIRECTORY_MOVIES,
                Environment.DIRECTORY_PICTURES),
        new MediaCategory(MUSIC, Environment.DIRECTORY_MUSIC, Environment.DIRECTORY_ALARMS,
                Environment.DIRECTORY_NOTIFICATIONS, Environment.DIRECTORY_RINGTONES,
                Environment.DIRECTORY_PODCASTS)
    };

    static {
        // Downloads
        sPathsExcludedForMisc.add(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
        // Apps
        sPathsExcludedForMisc.add(Environment.getExternalStorage2Directory().getAbsolutePath() +
                "/Android");
    }

    // Updates the memory usage bar graph.
    private static final int MSG_UI_UPDATE_APPROXIMATE = 1;

    // Updates the memory usage bar graph.
    private static final int MSG_UI_UPDATE_EXACT = 2;

    private Handler mUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UI_UPDATE_APPROXIMATE: {
                    Bundle bundle = msg.getData();
                    final long totalSize = bundle.getLong(StorageMeasurement.TOTAL_SIZE);
                    final long availSize = bundle.getLong(StorageMeasurement.AVAIL_SIZE);
                    updateApproximate(totalSize, availSize);
                    break;
                }
                case MSG_UI_UPDATE_EXACT: {
                    Bundle bundle = msg.getData();
                    final long totalSize = bundle.getLong(StorageMeasurement.TOTAL_SIZE);
                    final long availSize = bundle.getLong(StorageMeasurement.AVAIL_SIZE);
                    final long appsUsed = bundle.getLong(StorageMeasurement.APPS_USED);
                    final long downloadsSize = bundle.getLong(StorageMeasurement.DOWNLOADS_SIZE);
                    final long miscSize = bundle.getLong(StorageMeasurement.MISC_SIZE);
                    final long[] mediaSizes = bundle.getLongArray(StorageMeasurement.MEDIA_SIZES);
                    updateExact(totalSize, availSize, appsUsed, downloadsSize, miscSize,
                            mediaSizes);
                    break;
                }
            }
        }
    };

    public StorageVolumeCategory(Context context, Holder holder,
            StorageVolume storageVolume, StorageManager storageManager, boolean isPrimary) {
    	mContext = context;
        mHolder = holder;
        mStorageVolume = storageVolume;
        mStorageManager = storageManager;
        mMeasurement = StorageMeasurement.getInstance(context, storageVolume, isPrimary);
        mMeasurement.setReceiver(this);

        // Cannot format emulated storage
        mAllowFormat = mStorageVolume != null && !mStorageVolume.isEmulated() && mStorageVolume.isRemovable();
        // For now we are disabling reformatting secondary external storage
        // until some interoperability problems with MTP are fixed
        //if (!isPrimary) mAllowFormat = false;
    }
    
    public void init() {
    	updateDate(0,0,MEMORY_FLASH);
    	updateDate(0,0,MEMORY_SD);
    }

    public StorageVolume getStorageVolume() {
        return mStorageVolume;
    }


	protected void updateExact(long totalSize, long availSize, long appsUsed,
			long downloadsSize, long miscSize, long[] mediaSizes) {

		if (mMeasurement.isExternalSDCard()) {
            // TODO FIXME: external SD card will not report any size. Show used space in bar graph
			updateDate(totalSize,availSize,MEMORY_SD);
        }
	}

	protected void updateApproximate(long totalSize, long availSize) {

		updateDate(totalSize,availSize,MEMORY_FLASH);		
	}
	
	private void updateDate(long totalSize, long availSize, int category){

    	Log.d(TAG,"totalSize = "+totalSize+" availSize = "+availSize);
    	
    	switch(category){
    		case MEMORY_FLASH:
    	    	mHolder.textFlashUsed.setText(String.format(
									mContext.getString(R.string.System_info_flash_unused), 
									formatSize(totalSize - availSize)));
    	    	mHolder.textFlashUnused.setText(String.format(
    	    						mContext.getString(R.string.System_info_flash_unused), 
    	    						formatSize(totalSize)));
    			mHolder.layoutFlash.addView(getDrewView(mContext,availSize,totalSize));
    			break;
    		case MEMORY_SD:
    	    	mHolder.textSdUsed.setText(String.format(
									mContext.getString(R.string.System_info_flash_unused), 
									formatSize(totalSize - availSize)));
    	    	mHolder.textSdUnused.setText(String.format(
    	    						mContext.getString(R.string.System_info_flash_unused), 
    	    						formatSize(totalSize)));
    			mHolder.layoutSd.addView(getDrewView(mContext,availSize,totalSize));
    			break;
    		default:
    			break;
    	}
	}

	private DrewView getDrewView(Context context, double availSize, double totalSize){
		DrewView view;
    	float angle = 0;
    	if((totalSize <= 0) || (availSize < 0) || (availSize > totalSize)){
    		angle = 360;
    	}else{
        	angle = (float) ((360 * availSize) / totalSize);    		
    	}
    	Log.d(TAG,"angle = "+angle);
    	view = new DrewView(mContext,0,angle);
    	view.setMinimumHeight(500);
		view.setMinimumWidth(500);
		//repaint view components
		view.invalidate();
		view.setRotationY(180);
    	return view;    			
    }

    private void measure() {
        mMeasurement.invalidate();
        mMeasurement.measure();
    }

    public void onResume() {
        mMeasurement.setReceiver(this);
        measure();
    }

    public void onStorageStateChanged() {
        measure();
    }

    public void onMediaScannerFinished() {
        measure();
    }

    public void onPause() {
        mMeasurement.cleanUp();
    }
    
    private String formatSize(long size) {
        return Formatter.formatFileSize(mContext, size);
    }
	
	@Override
	public void updateApproximate(Bundle bundle) {

        final Message message = mUpdateHandler.obtainMessage(MSG_UI_UPDATE_APPROXIMATE);
        message.setData(bundle);
        mUpdateHandler.sendMessage(message);		
	}

	@Override
	public void updateExact(Bundle bundle) {

        final Message message = mUpdateHandler.obtainMessage(MSG_UI_UPDATE_EXACT);
        message.setData(bundle);
        mUpdateHandler.sendMessage(message);		
	}

}
