package com.lenovo.settings.applications;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lenovo.settings.LenovoSettingsActivity;
import com.lenovo.settings.R;
import com.lenovo.settings.SettingFragment;
import com.lenovo.settings.LenovoSettingsActivity.ListViewAdapter;
import com.lenovo.settings.LenovoSettingsActivity.ListViewAdapter.ViewHolder;
import com.lenovo.settings.R.drawable;
import com.lenovo.settings.R.id;
import com.lenovo.settings.R.layout;
import com.lenovo.settings.R.string;
import com.lenovo.settings.applications.ApplicationsState.AppEntry;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.GridView;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.internal.content.PackageHelper;

final class CanBeOnSdCardChecker {
	final IPackageManager mPm;
	int mInstallLocation;

	CanBeOnSdCardChecker() {
		mPm = IPackageManager.Stub.asInterface(ServiceManager
				.getService("package"));
	}

	void init() {
		try {
			mInstallLocation = mPm.getInstallLocation();
		} catch (RemoteException e) {
			Log.e("CanBeOnSdCardChecker", "Is Package Manager running?");
			return;
		}
	}

	boolean check(ApplicationInfo info) {
		boolean canBe = false;
		if (ManageApplications.DEBUG) {
			Log.d(ManageApplications.TAG,
					"------ check ApplicationInfo info.flags = " + info.flags);
		}
		if ((info.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
			canBe = true;
		} else {
			if ((info.flags & ApplicationInfo.FLAG_FORWARD_LOCK) == 0
					&& (info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
				if (info.installLocation == PackageInfo.INSTALL_LOCATION_PREFER_EXTERNAL
						|| info.installLocation == PackageInfo.INSTALL_LOCATION_AUTO) {
					canBe = true;
				} else if (info.installLocation == PackageInfo.INSTALL_LOCATION_UNSPECIFIED) {
					if (mInstallLocation == PackageHelper.APP_INSTALL_EXTERNAL) {
						// For apps with no preference and the default value set
						// to install on sdcard.
						canBe = true;
					}
				}
			}
		}
		return canBe;
	}
}

public class ManageApplications extends Fragment implements OnItemClickListener {

	public static final String TAG = "ManageApplications";
	static final boolean DEBUG = false;
	private static final int INSTALLED_APP_DETAILS = 1;

	public static final int SIZE_TOTAL = 0;
	public static final int SIZE_INTERNAL = 1;
	public static final int SIZE_EXTERNAL = 2;

	private View mView;
	private GridView mAppGrid;
	private List<Map<String, Object>> mData;
	public LayoutInflater mInflater;
	private ListAdapter mGridViewAdapter;
	private TextView mFlashUnused;
	private TextView mFlashTotal;
	private TextView mSdUnused;
	private TextView mSdTotal;
	private ProgressBar mFlashProgressBar;
	private ProgressBar mSdProgressBar;
	private int mFlashUnusedSize;
	private int mSdUnusedSize;
	private int mFlashTotalSize;
	private int mSdTotalSize;
	/**
	 * 添加SDcard插拔监听
	 */
	private IntentFilter mSdcardFilter;
	private BroadcastReceiver mSdcardStateReceiver;

	// sort order that can be changed through the menu can be sorted
	// alphabetically
	// or size(descending)
	private static final int MENU_OPTIONS_BASE = 0;
	// Filter options used for displayed list of applications
	public static final int FILTER_APPS_ALL = MENU_OPTIONS_BASE + 0;
	public static final int FILTER_APPS_THIRD_PARTY = MENU_OPTIONS_BASE + 1;
	public static final int FILTER_APPS_SDCARD = MENU_OPTIONS_BASE + 2;

	public static final int SORT_ORDER_ALPHA = MENU_OPTIONS_BASE + 4;
	public static final int SORT_ORDER_SIZE = MENU_OPTIONS_BASE + 5;
	public static final int SHOW_RUNNING_SERVICES = MENU_OPTIONS_BASE + 6;
	public static final int SHOW_BACKGROUND_PROCESSES = MENU_OPTIONS_BASE + 7;
	// sort order
	private int mSortOrder = SORT_ORDER_ALPHA;
	// Filter value
	private int mFilterApps = FILTER_APPS_THIRD_PARTY;
	private ApplicationsState mApplicationsState;
	private ApplicationsAdapter mApplicationsAdapter;
	private int mCurView;
	private boolean mCreatedRunning;

	private boolean mResumedRunning;
	private boolean mActivityResumed;

	private int mSelectPostion = 0;

	private String mCurrentPkgName;
	private StatFs mDataFileStats;
	private StatFs mSDCardFileStats;
	private CharSequence mInvalidSizeStr;
	private boolean mLastShowedInternalStorage = true;
	private long mLastUsedStorage, mLastAppStorage, mLastFreeStorage;
	private long mLastSdUsedStorage, mLastSdAppStorage, mLastSdFreeStorage;
	private boolean mAppGridHasFocus;
	
	
	int w = 0;
	int h = 0;
	public ManageApplications(WindowManager windowManager) {
		getDisplay(windowManager);
	}

	// View Holder used when displaying views
	static class AppViewHolder {
		ApplicationsState.AppEntry entry;
		TextView appName;
		ImageView appIcon;

		// ImageView appSdIcon;
		// TextView appSize;
		// TextView appTime;

		void updateSizeText(ManageApplications ma, int whichSize) {
			if (DEBUG)
				Log.d(TAG, "updateSizeText of " + entry.label + " " + entry
						+ ": " + entry.sizeStr);
			if (entry.sizeStr != null) {
				switch (whichSize) {
				case SIZE_INTERNAL:
					// appSize.setText(entry.internalSizeStr);
					break;
				case SIZE_EXTERNAL:
					// appSize.setText(entry.externalSizeStr);
					break;
				default:
					// appSize.setText(entry.sizeStr);
					break;
				}
			} else if (entry.size == ApplicationsState.SIZE_INVALID) {
				// appSize.setText(ma.mInvalidSizeStr);
			}
		}
	}

	/*
	 * Custom adapter implementation for the ListView This adapter maintains a
	 * map for each displayed application and its properties An index value on
	 * each AppInfo object indicates the correct position or index in the list.
	 * If the list gets updated dynamically when the user is viewing the list of
	 * applications, we need to return the correct index of position. This is
	 * done by mapping the getId methods via the package name into the internal
	 * maps and indices. The order of applications in the list is mirrored in
	 * mAppLocalList
	 */
	class ApplicationsAdapter extends BaseAdapter implements Filterable,
			ApplicationsState.Callbacks, AbsListView.RecyclerListener {
		private final ApplicationsState mState;
		private final ArrayList<View> mActive = new ArrayList<View>();
		private ArrayList<ApplicationsState.AppEntry> mBaseEntries;
		private ArrayList<ApplicationsState.AppEntry> mEntries;
		private boolean mResumed = false;
		private int mLastFilterMode = -1, mLastSortMode = -1;
		private boolean mWaitingForData;
		private int mWhichSize = SIZE_TOTAL;
		CharSequence mCurFilterPrefix;

		private Filter mFilter = new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				ArrayList<ApplicationsState.AppEntry> entries = applyPrefixFilter(
						constraint, mBaseEntries);
				FilterResults fr = new FilterResults();
				fr.values = entries;
				fr.count = entries.size();
				return fr;
			}

			@Override
			protected void publishResults(CharSequence constraint,
					FilterResults results) {
				mCurFilterPrefix = constraint;
				mEntries = (ArrayList<ApplicationsState.AppEntry>) results.values;
				notifyDataSetChanged();
				updateStorageUsage();
			}
		};

		public ApplicationsAdapter(ApplicationsState state) {
			mState = state;
		}

		public void resume(int filter, int sort) {
			if (DEBUG)
				Log.i(TAG, "Resume!  mResumed=" + mResumed);
			Log.d(TAG, "---------------------------Resume!  mResumed="
					+ mResumed);
			if (!mResumed) {
				mResumed = true;
				mState.resume(this);
				mLastFilterMode = filter;
				mLastSortMode = sort;
				rebuild(true);
			} else {
				rebuild(filter, sort);
			}
		}

		public void pause() {
			if (mResumed) {
				mResumed = false;
				mState.pause();
			}
		}

		public void rebuild(int filter, int sort) {
			if (filter == mLastFilterMode && sort == mLastSortMode) {
				return;
			}
			mLastFilterMode = filter;
			mLastSortMode = sort;
			rebuild(true);
		}

		public void rebuild(boolean eraseold) {
			if (DEBUG)
				Log.i(TAG, "Rebuilding app list...");
			ApplicationsState.AppFilter filterObj;
			Comparator<AppEntry> comparatorObj;
			boolean emulated = Environment.isExternalStorageEmulated();
			/*
			 * if (emulated) { mWhichSize = SIZE_TOTAL; } else { mWhichSize =
			 * SIZE_INTERNAL; }
			 */
			mWhichSize = SIZE_TOTAL; // Rony modify 20120321
			switch (mLastFilterMode) {
			case FILTER_APPS_THIRD_PARTY:
				filterObj = ApplicationsState.THIRD_PARTY_FILTER;
				break;
			case FILTER_APPS_SDCARD:
				filterObj = ApplicationsState.ON_SD_CARD_FILTER;
				if (!emulated) {
					mWhichSize = SIZE_EXTERNAL;
				}
				break;
			default:
				filterObj = null;
				break;
			}
			Log.d(TAG, "mWhichSize = " + mWhichSize);
			switch (mLastSortMode) {
			case SORT_ORDER_SIZE:
				switch (mWhichSize) {
				case SIZE_INTERNAL:
					comparatorObj = ApplicationsState.INTERNAL_SIZE_COMPARATOR;
					break;
				case SIZE_EXTERNAL:
					comparatorObj = ApplicationsState.EXTERNAL_SIZE_COMPARATOR;
					break;
				default:
					comparatorObj = ApplicationsState.SIZE_COMPARATOR;
					break;
				}
				break;
			default:
				comparatorObj = ApplicationsState.ALPHA_COMPARATOR;
				break;
			}
			ArrayList<ApplicationsState.AppEntry> entries = mState.rebuild(
					filterObj, comparatorObj);
			if (entries == null && !eraseold) {
				// Don't have new list yet, but can continue using the old one.
				return;
			}
			mBaseEntries = entries;
			if (mBaseEntries != null) {
				mEntries = applyPrefixFilter(mCurFilterPrefix, mBaseEntries);
			} else {
				mEntries = null;
			}
			notifyDataSetChanged();
			updateStorageUsage();

			if (entries == null) {
				mWaitingForData = true;
			} else {
			}
		}

		ArrayList<ApplicationsState.AppEntry> applyPrefixFilter(
				CharSequence prefix,
				ArrayList<ApplicationsState.AppEntry> origEntries) {
			if (prefix == null || prefix.length() == 0) {
				return origEntries;
			} else {
				String prefixStr = ApplicationsState.normalize(prefix
						.toString());
				final String spacePrefixStr = " " + prefixStr;
				ArrayList<ApplicationsState.AppEntry> newEntries = new ArrayList<ApplicationsState.AppEntry>();
				for (int i = 0; i < origEntries.size(); i++) {
					ApplicationsState.AppEntry entry = origEntries.get(i);
					String nlabel = entry.getNormalizedLabel();
					if (nlabel.startsWith(prefixStr)
							|| nlabel.indexOf(spacePrefixStr) != -1) {
						newEntries.add(entry);
					}
				}
				return newEntries;
			}
		}

		@Override
		public void onRunningStateChanged(boolean running) {
			getActivity().setProgressBarIndeterminateVisibility(running);
		}

		@Override
		public void onRebuildComplete(ArrayList<AppEntry> apps) {
			mWaitingForData = false;
			mBaseEntries = apps;
			mEntries = applyPrefixFilter(mCurFilterPrefix, mBaseEntries);
			notifyDataSetChanged();
			updateStorageUsage();
		}

		@Override
		public void onPackageListChanged() {
			rebuild(false);
		}

		@Override
		public void onPackageIconChanged() {
			// We ensure icons are loaded when their item is displayed, so
			// don't care about icons loaded in the background.
		}

		@Override
		public void onPackageSizeChanged(String packageName) {
			for (int i = 0; i < mActive.size(); i++) {
				AppViewHolder holder = (AppViewHolder) mActive.get(i).getTag();
				if (holder.entry.info.packageName.equals(packageName)) {
					synchronized (holder.entry) {
						holder.updateSizeText(ManageApplications.this,
								mWhichSize);
					}
					if (holder.entry.info.packageName.equals(mCurrentPkgName)
							&& mLastSortMode == SORT_ORDER_SIZE) {
						// We got the size information for the last app the
						// user viewed, and are sorting by size... they may
						// have cleared data, so we immediately want to resort
						// the list with the new size to reflect it to the user.
						rebuild(false);
					}
					updateStorageUsage();
					return;
				}
			}
		}

		@Override
		public void onAllSizesComputed() {
			if (mLastSortMode == SORT_ORDER_SIZE) {
				rebuild(false);
			}
		}

		public int getCount() {
			return mEntries != null ? mEntries.size() : 0;
		}

		public Object getItem(int position) {
			return mEntries.get(position);
		}

		public ApplicationsState.AppEntry getAppEntry(int position) {
			return mEntries.get(position);
		}

		public long getItemId(int position) {
			return mEntries.get(position).id;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// A ViewHolder keeps references to children views to avoid
			// unnecessary calls
			// to findViewById() on each row.
			AppViewHolder holder;

			// When convertView is not null, we can reuse it directly, there is
			// no need
			// to reinflate it. We only inflate a new View when the convertView
			// supplied
			// by ListView is null.
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.app_gridview_item,
						null);

				// 必须手动设宽高  by xubin
				if(w == 1280){
					convertView.setLayoutParams(new GridView.LayoutParams(130, 130));
				}else if(w == 1920){
					convertView.setLayoutParams(new GridView.LayoutParams(195, 195));
				}else{
					//默认1280p
					convertView.setLayoutParams(new GridView.LayoutParams(130, 130));
				}

				// Creates a ViewHolder and store references to the two children
				// views
				// we want to bind data to.
				holder = new AppViewHolder();
				holder.appName = (TextView) convertView
						.findViewById(R.id.text_app_grid_name);
				holder.appIcon = (ImageView) convertView
						.findViewById(R.id.image_app_icon);
				// holder.appSdIcon = (ImageView)
				// convertView.findViewById(R.id.image_app_sd);
				// holder.appSize = (TextView)
				// convertView.findViewById(R.id.text_app_grid_size);
				// holder.appTime = (TextView)
				// convertView.findViewById(R.id.text_app_grid_time);
				convertView.setTag(holder);
			} else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (AppViewHolder) convertView.getTag();
			}
			if (convertView.hasFocus()) {
				Log.d(TAG, "gridview has focus id is " + position);
			}

			// Bind the data efficiently with the holder
			ApplicationsState.AppEntry entry = mEntries.get(position);
			final boolean emulatedStorage = Environment
					.isExternalStorageEmulated();
			synchronized (entry) {
				holder.entry = entry;
				if (entry.label != null) {
					holder.appName.setText(entry.label);
				}
				mState.ensureIcon(entry);
				if (entry.icon != null) {
					holder.appIcon.setImageDrawable(entry.icon);
				}
				if (emulatedStorage) {
					// holder.appSdIcon.setImageResource(R.drawable.sd_none);
				}
				if (((entry.info.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0)) {
					// holder.appSdIcon.setImageResource(R.drawable.sd_bg);
				} else {
					// holder.appSdIcon.setImageResource(R.drawable.sd_none);
				}
				holder.updateSizeText(ManageApplications.this, mWhichSize);
				if ((!mAppGrid.isFocused()) && (mAppGrid.getCount() != 0)
						&& mAppGridHasFocus) {
					Log.e(TAG, "focus enable2 = " + mAppGrid.isFocused()
							+ " count = " + mAppGrid.getCount());

					mAppGrid.setSelection(0);
					// mAppGrid.setFocusable(true);
					// mAppGrid.setFocusableInTouchMode(true);
					mAppGrid.requestFocus();
					mAppGridHasFocus = false;
				}
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd");
				Date curDate = new Date(entry.time);

				// holder.appTime.setText(formatter.format(curDate));
			}
			mActive.remove(convertView);
			mActive.add(convertView);
			return convertView;
		}

		@Override
		public Filter getFilter() {
			return mFilter;
		}

		@Override
		public void onMovedToScrapHeap(View view) {
			mActive.remove(view);
		}
	}

	
	public void getDisplay(WindowManager windowManager){
		DisplayMetrics dm = new DisplayMetrics();
		windowManager.getDefaultDisplay().getMetrics(dm);
		w = dm.widthPixels;
		h = dm.heightPixels;
		Log.d(TAG, "w = "+w +"  h = "+h);
	}
	
	
	int wight = 0;
	int height = 0;
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Log.d(TAG, "---onCreate----");
		mAppGridHasFocus = true;
		mApplicationsState = ApplicationsState.getInstance(getActivity()
				.getApplication());
		mApplicationsAdapter = new ApplicationsAdapter(mApplicationsState);
		mDataFileStats = new StatFs("/data");
		mSDCardFileStats = new StatFs(Environment.getExternalStorage2Directory()
				.toString());
		mSdcardFilter = new IntentFilter();
		mSdcardFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);// 表明sd对象是存在并具有读/写权限
		mSdcardFilter.addAction(Intent.ACTION_MEDIA_REMOVED); // 完全拔出
		mSdcardFilter.addDataScheme("file"); // 必须要有此行，否则无法收到广播
		mSdcardStateReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {

				String action = intent.getAction();
				Log.d(TAG, "---mSdcardStateReceiver   action = " + action);
				if (action.equals(Intent.ACTION_MEDIA_REMOVED)
						|| action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
					updateStorageUsage();
				}
			}
		};
		getActivity().registerReceiver(mSdcardStateReceiver, mSdcardFilter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mInflater = inflater;
		mView = inflater
				.inflate(R.layout.application_setting, container, false);
		mAppGrid = (GridView) mView.findViewById(R.id.gridViewApp);
		mFlashUnused = (TextView) mView
				.findViewById(R.id.text_app_flash_unused);
		mFlashTotal = (TextView) mView.findViewById(R.id.text_app_flash_total);
		mSdUnused = (TextView) mView.findViewById(R.id.text_app_sd_unused);
		mSdTotal = (TextView) mView.findViewById(R.id.text_app_sd_total);
		mFlashProgressBar = (ProgressBar) mView
				.findViewById(R.id.progressbar_flash);
		mSdProgressBar = (ProgressBar) mView.findViewById(R.id.progressbar_sd);
		mAppGrid.setOnItemClickListener(this);
		// mAppGrid.setSaveEnabled(true);
		// mAppGrid.setTextFilterEnabled(true);
		mAppGrid.setRecyclerListener(mApplicationsAdapter);
		mAppGrid.setAdapter(mApplicationsAdapter);
		LenovoSettingsActivity.setTitleFocus(false);
		mAppGrid.requestFocus();
		mAppGrid.setSelected(true);
		mAppGrid.setFocusable(true);
		// mAppGrid.setSelection(0);
		// mAppGrid.setSelector(new ColorDrawable(R.color.Transparent));
		// mAppGrid.setFocusableInTouchMode(true);

		/*
		 * mAppGrid.setOnKeyListener(new OnKeyListener(){
		 * 
		 * @Override public boolean onKey(View v, int keyCode, KeyEvent event) {
		 * 
		 * mAppGridHasFocus = false; return true; }
		 * 
		 * });
		 */
		/*
		 * mAppGrid.setOnItemClickListener(new OnItemClickListener() {
		 * 
		 * @Override public void onItemClick(AdapterView<?> arg0, View view, int
		 * position, long arg3) {
		 * 
		 * Log.d(TAG,"select = "+position); Fragment fragment = (Fragment) new
		 * InstalledAppDetails((String)mData.get(position).get("title"));
		 * SettingFragment sf = (SettingFragment) new
		 * SettingFragment(getActivity()); sf.setFragment(fragment,true); }
		 * 
		 * });
		 */
		// mGridViewAdapter = new GridViewAdapter(mView.getContext());
		// mAppGrid.setAdapter(mGridViewAdapter);

		// mAppGrid.setOnItemSelectedListener(new OnItemSelectedListener() {
		// public void onItemSelected(AdapterView<?> parent, View arg1,
		// int position, long arg3) {
		// for(int i=0;i<parent.getCount();i++){
		// if(i == position){
		// if(parent.getSelectedView()!=null){
		// parent.getSelectedView().setBackgroundResource(R.drawable.edit_text_focus);
		// }
		// }else{
		// parent.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
		// }
		// }
		// }
		//
		// public void onNothingSelected(AdapterView<?> parent) {
		// for(int i=0;i<parent.getCount();i++){
		// parent.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
		// }
		// }
		// });
		// mAppGrid.setOnFocusChangeListener(new OnFocusChangeListener() {
		// public void onFocusChange(View arg0, boolean arg1) {
		// if(arg1){
		// Log.d(TAG,
		// "--- has Focus ---mAppGrid.getSelectedView()="+mAppGrid.getSelectedView());
		// if(mAppGrid.getSelectedView()!=null){
		// mAppGrid.getSelectedView().setBackgroundResource(R.drawable.edit_text_focus);
		// }
		// }else{
		// if(mAppGrid.getSelectedView()!=null){
		// mAppGrid.getSelectedView().setBackgroundColor(Color.TRANSPARENT);
		// }
		// }
		// }
		// });

		return mView;
	}

	private int getScrollData(double scroll_max, double data, double data_max) {
		int scroll_data = 0;
		if (data_max <= 0) {
			scroll_data = (int) scroll_max;
		} else {
			scroll_data = (int) ((scroll_max * (data)) / data_max);
		}
		return scroll_data;
	}

	private int getScrollData(int scroll_max, int data, int data_max) {
		int scroll_data = 0;
		if (data_max <= 0) {
			scroll_data = scroll_max;
		} else {
			scroll_data = (int) ((scroll_max * (data)) / data_max);
		}
		return scroll_data;
	}

	@Override
	public void onStart() {
		super.onStart();
		mApplicationsAdapter.resume(mFilterApps, mSortOrder);
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "---onResume----");
		updateStorageUsage();
		mActivityResumed = true;
		mApplicationsAdapter.resume(mFilterApps, mSortOrder);
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "---onPause----");
		mActivityResumed = false;
		mApplicationsAdapter.pause();
	}

	@Override
	public void onDestroy() {

		Log.d(TAG, "---onDestroy----");
		getActivity().unregisterReceiver(mSdcardStateReceiver);
		super.onDestroy();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == INSTALLED_APP_DETAILS && mCurrentPkgName != null) {
			mApplicationsState.requestSize(mCurrentPkgName);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		ApplicationsState.AppEntry entry = mApplicationsAdapter
				.getAppEntry(position);
		mCurrentPkgName = entry.info.packageName;
		// startApplicationDetailsActivity();
		Fragment fragment = (Fragment) new InstalledAppDetails(mCurrentPkgName);
		SettingFragment sf = (SettingFragment) new SettingFragment(
				getActivity());
		sf.setFragment(fragment, true);
	}

	static final int VIEW_NOTHING = 0;
	static final int VIEW_LIST = 1;
	static final int VIEW_RUNNING = 2;

	void updateStorageUsage() {

		long freeStorage = 0;
		long totalStorage = 0;
		long freeSdStorage = 0;
		long totalSdStorage = 0;
		mSDCardFileStats.restat(Environment.getExternalStorage2Directory()
				.toString());
		try {
			totalSdStorage = (long) mSDCardFileStats.getBlockCount()
					* mSDCardFileStats.getBlockSize();
			freeSdStorage = (long) mSDCardFileStats.getAvailableBlocks()
					* mSDCardFileStats.getBlockSize();
		} catch (IllegalArgumentException e) {
			// use the old value of mFreeMem
		}
		mDataFileStats.restat("/data");
		try {
			totalStorage = (long) mDataFileStats.getBlockCount()
					* mDataFileStats.getBlockSize();
			freeStorage = (long) mDataFileStats.getAvailableBlocks()
					* mDataFileStats.getBlockSize();
		} catch (IllegalArgumentException e) {
		}
		freeStorage += mApplicationsState.sumCacheSizes();

		if (totalSdStorage > 0) {
			// if (mLastSdFreeStorage != freeSdStorage)
			{
				mLastSdFreeStorage = freeSdStorage;
				String sizeStr = Formatter.formatShortFileSize(getActivity(),
						freeSdStorage);
				mSdUnused.setText(sizeStr);
				sizeStr = Formatter.formatShortFileSize(getActivity(),
						totalSdStorage);
				mSdTotal.setText(getActivity().getResources().getString(
						R.string.App_total_size, sizeStr));
				mSdProgressBar.setProgress(getScrollData(100,
						(totalSdStorage - freeSdStorage), totalSdStorage));
			}
		} else {
			// if (mLastSdFreeStorage != -1)
			{
				mLastSdFreeStorage = -1;
				String sizeStr = Formatter
						.formatShortFileSize(getActivity(), 0);
				mSdUnused.setText(sizeStr);
				mSdTotal.setText(getActivity().getResources().getString(
						R.string.App_total_size, sizeStr));
				mSdProgressBar.setProgress(0);
			}
		}

		if (totalStorage > 0) {
			// if (mLastFreeStorage != freeStorage)
			{
				mLastFreeStorage = freeStorage;
				String sizeStr = Formatter.formatShortFileSize(getActivity(),
						freeStorage);
				mFlashUnused.setText(sizeStr);
				sizeStr = Formatter.formatShortFileSize(getActivity(),
						totalStorage);
				mFlashTotal.setText(getActivity().getResources().getString(
						R.string.App_total_size, sizeStr));
				mFlashProgressBar.setProgress(getScrollData(100,
						(totalStorage - freeStorage), totalStorage));
			}
		} else {
			// if (mLastFreeStorage != -1)
			{
				mLastFreeStorage = -1;
				String sizeStr = Formatter
						.formatShortFileSize(getActivity(), 0);
				mFlashUnused.setText(sizeStr);
				mFlashTotal.setText(getActivity().getResources().getString(
						R.string.App_total_size, sizeStr));
				mFlashProgressBar.setProgress(0);
			}
		}
	}

}
