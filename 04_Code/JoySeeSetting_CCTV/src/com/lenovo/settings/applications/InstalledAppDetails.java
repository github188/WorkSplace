
package com.lenovo.settings.applications;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageMoveObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.usb.IUsbManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lenovo.settings.LenovoSettingsActivity;
import com.lenovo.settings.R;
import com.lenovo.settings.Util.AdapterViewSelectionUtil;
import com.lenovo.settings.applications.ApplicationsState.AppEntry;
import com.lenovo.settings.db.AppInfoBean;
import com.lenovo.settings.db.DBUtil;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class InstalledAppDetails extends Fragment implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener,
        ApplicationsState.Callbacks {

    public static final String TAG = "InstalledAppDetails";
    public static final boolean SUPPORT_DISABLE_APPS = false;
    private static final boolean localLOGV = false;
    private static final boolean DEBUG = true;

    public static final String COLUMN_NAME_PACKAGE_NAME = "packagename";

    public static final String ARG_PACKAGE_NAME = "package";
    public static final String COLUMN_NAME_COMMON_APP = "common";

    public static final String COLUMN_NAME_CREATE_DATE = "created";

    public static final String COLUMN_NAME_LAST_USED_DATE = "lasttime";
    public static final String AUTHORITY = "com.geniatech.provider.MyApplication";

    public static final String TABLE_NAME = "applications";

    private static final String SCHEME = "content://";

    private static final String PATH_APPS = "/apps";

    public static final int APP_ID_PATH_POSITION = 1;

    public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_APPS);

    private PackageManager mPm;
    private IUsbManager mUsbManager;
    private DevicePolicyManager mDpm;
    private ApplicationsState mState;
    private static ApplicationsState.AppEntry mAppEntry;
    private PackageInfo mPackageInfo;

    private static View mView;
    private ImageView mIcon;
    private boolean mMoveInProgress = false;
    private boolean mUpdatedSysApp = false;
    private boolean mCanClearData = true;
    private String mPackageName;
    /** 应用名称 */
    private TextView mLabel;
    private TextView mTotalSize;
    private TextView mAppTime;
    /** flash 数据 */
    private TextView mDataSize;
    /** SDcard数据 */
    private TextView mExternalDataSize;
    /** 缓存数据 */
    private TextView mCacheSize;
    private ClearCacheObserver mClearCacheObserver;
    private ClearUserDataObserver mClearDataObserver;
    private Button mMoveAppButton;
    private Button mUninstallButton;
    private Button mClearDataButton;
    private Button mClearCacheButton;
    private CanBeOnSdCardChecker mCanBeOnSdCardChecker;

    private PackageMoveObserver mPackageMoveObserver;
    private IntentFilter mFilter;

    private boolean mHaveSizes = false;
    private long mLastCodeSize = -1;
    private long mLastDataSize = -1;
    private long mLastExternalCodeSize = -1;
    private long mLastExternalDataSize = -1;
    private long mLastCacheSize = -1;
    private long mLastTotalSize = -1;

    // internal constants used in Handler
    private static final int OP_SUCCESSFUL = 1;
    private static final int OP_FAILED = 2;
    private static final int CLEAR_USER_DATA = 1;
    private static final int CLEAR_CACHE = 3;
    private static final int PACKAGE_MOVE = 4;

    // invalid size value used initially and also when size retrieval through
    // PackageManager
    // fails for whatever reason
    private static final int SIZE_INVALID = -1;

    // Resource strings
    private CharSequence mInvalidSizeStr = "0.00B";
    private CharSequence mComputingStr;

    // Dialog identifiers used in showDialog
    private static final int DLG_BASE = 0;
    private static final int DLG_CLEAR_DATA = DLG_BASE + 1;
    private static final int DLG_FACTORY_RESET = DLG_BASE + 2;
    private static final int DLG_APP_NOT_FOUND = DLG_BASE + 3;
    private static final int DLG_CANNOT_CLEAR_DATA = DLG_BASE + 4;
    private static final int DLG_FORCE_STOP = DLG_BASE + 5;
    private static final int DLG_MOVE_FAILED = DLG_BASE + 6;
    private static final int DLG_DISABLE = DLG_BASE + 7;

    //for app lock
    private Button mLockButton;
    private AppInfoBean mAppLock;
    private AppInfoBean mAppInfoBean;
    private ArrayList<AppInfoBean> appList = new ArrayList<AppInfoBean>();
    private DBUtil mDButil;
    private Context mContext;
    
    private ArrayList<String> mPackageNameList = new ArrayList<String>();
    private InstallOperation mInstallOperation = new InstallOperation();
    private Dialog mDialog = null;
    private Dialog mMoveDialog = null;
    private StorageManager mStorageManager;
    private StorageEventListener mStorageEventListener = new StorageEventListener() {
        @Override
        public void onStorageStateChanged(String path, String oldState,
                String newState) {

            MyLog(" --- onStorageStateChanged From " + oldState + " to " + newState);
            if (newState.equals(Environment.MEDIA_MOUNTED)) {
                if (mMoveAppButton != null) {
                    mMoveAppButton.setEnabled(true);
                    mMoveAppButton.setText(getResources().getString(R.string.app_mounted_sdcard));
                }
            } else if (newState.equals(Environment.MEDIA_REMOVED)) {
                if (mMoveAppButton != null) {
                    mMoveAppButton.setEnabled(false);
                    mMoveAppButton.setText(getResources().getString(R.string.app_move_nosdcard));
                }
            }
            super.onStorageStateChanged(path, oldState, newState);
        }
    };

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String packageName = intent.getDataString();
            Log.d(TAG, "action is " + action + " ,packageName=" + packageName);

            if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                int pos = packageName.indexOf(":");
                String name = packageName.substring(pos + 1);
                Log.d(TAG, "name=" + name);
                // showUnInstalling(name);
                if (mDialog != null) {
                    mDialog.dismiss();
                }
                showUnInstalledDialog();
                deleteApp(name);
            }
        }
    };

    public InstalledAppDetails(String pkgName) {
        mPackageName = pkgName;
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            // If the fragment is gone, don't process any more messages.
            if (getView() == null) {
                return;
            }
            switch (msg.what) {
                case CLEAR_USER_DATA:
                    processClearMsg(msg);
                    break;
                case CLEAR_CACHE:
                    // Refresh size info
                    mState.requestSize(mAppEntry.info.packageName);
                    break;
                case PACKAGE_MOVE:
                    processMoveMsg(msg);
                    break;
                default:
                    break;
            }
        }
    };
    private Drawable mDialogIcon;
    private String mDialogLabel;
    private View mToastView;

    class ClearUserDataObserver extends IPackageDataObserver.Stub {
        public void onRemoveCompleted(final String packageName, final boolean succeeded) {
            final Message msg = mHandler.obtainMessage(CLEAR_USER_DATA);
            msg.arg1 = succeeded ? OP_SUCCESSFUL : OP_FAILED;
            mHandler.sendMessage(msg);
        }
    }

    class ClearCacheObserver extends IPackageDataObserver.Stub {
        public void onRemoveCompleted(final String packageName, final boolean succeeded) {
            final Message msg = mHandler.obtainMessage(CLEAR_CACHE);
            msg.arg1 = succeeded ? OP_SUCCESSFUL : OP_FAILED;
            mHandler.sendMessage(msg);
        }
    }

    class PackageMoveObserver extends IPackageMoveObserver.Stub {
        public void packageMoved(String packageName, int returnCode) throws RemoteException {
            final Message msg = mHandler.obtainMessage(PACKAGE_MOVE);
            msg.arg1 = returnCode;
            Log.d(TAG, " PackageMoveObserver returnCode = " + returnCode);
            mHandler.sendMessage(msg);
        }
    }

    private String getSizeStr(long size) {
        MyLog(" getSizeStr size = " + size);
        if (size == SIZE_INVALID) {
            return mInvalidSizeStr.toString();
        }
        MyLog(" getSizeStr = " + Formatter.formatFileSize(getActivity(), size));
        return Formatter.formatFileSize(getActivity(), size);
    }

    private void initDataButtons() {
        if ((mAppEntry.info.flags & (ApplicationInfo.FLAG_SYSTEM
                | ApplicationInfo.FLAG_ALLOW_CLEAR_USER_DATA))
                    == ApplicationInfo.FLAG_SYSTEM
                || mDpm.packageHasActiveAdmins(mPackageInfo.packageName)) {
            mClearDataButton.setText(R.string.clear_user_data_text);
            mClearDataButton.setEnabled(false);
            mClearDataButton.setFocusable(false);
            mClearDataButton.setAlpha(0.7f);
            mCanClearData = false;
        } else {
            mClearDataButton.setText(R.string.clear_user_data_text);
            mClearDataButton.setOnClickListener(this);
        }
    }

    private CharSequence getMoveErrMsg(int errCode) {
        MyLog(" getMoveErrMsg errCode = " + errCode);
        switch (errCode) {
            case PackageManager.MOVE_FAILED_INSUFFICIENT_STORAGE:
                return getActivity().getString(R.string.insufficient_storage);
            case PackageManager.MOVE_FAILED_DOESNT_EXIST:
                return getActivity().getString(R.string.does_not_exist);
            case PackageManager.MOVE_FAILED_FORWARD_LOCKED:
                return getActivity().getString(R.string.app_forward_locked);
            case PackageManager.MOVE_FAILED_INVALID_LOCATION:
                return getActivity().getString(R.string.invalid_location);
            case PackageManager.MOVE_FAILED_SYSTEM_PACKAGE:
                return getActivity().getString(R.string.system_package);
            case PackageManager.MOVE_FAILED_INTERNAL_ERROR:
                return getActivity().getString(R.string.insufficient_storage);
        }
        return getActivity().getString(R.string.move_app_failed);
    }

    private void initMoveButton() {
        if (Environment.isExternalStorageEmulated()) {
            mMoveAppButton.setVisibility(View.INVISIBLE);
            return;
        }
        boolean dataOnly = false;
        dataOnly = (mPackageInfo == null) && (mAppEntry != null);
        boolean moveDisable = true;
        if (dataOnly) {
            mMoveAppButton.setText(R.string.move_app);
        } else if ((mAppEntry.info.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
            mMoveAppButton.setText(R.string.move_app_to_internal);
            // Always let apps move to internal storage from sdcard.
            moveDisable = false;
        } else {
            mMoveAppButton.setText(R.string.move_app_to_sdcard);
            mCanBeOnSdCardChecker.init();
            moveDisable = !mCanBeOnSdCardChecker.check(mAppEntry.info);
        }
        if (moveDisable) {
            mMoveAppButton.setEnabled(false);
            mMoveAppButton.setFocusable(false);
            mMoveAppButton.setAlpha(0.7f);
        } else {
            mMoveAppButton.setEnabled(true);
            mMoveAppButton.setFocusable(true);
            mMoveAppButton.setAlpha(1f);
        }
        String state = android.os.Environment.getExternalStorage2State();
        MyLog(" initMoveButton state = " + state);
        // 判断SdCard是否存在并且是可用的
        if (android.os.Environment.MEDIA_MOUNTED.equals(state)) {
            mMoveAppButton.setEnabled(true);
            mMoveAppButton.setFocusable(true);
            mMoveAppButton.setClickable(true);
            mMoveAppButton.setOnClickListener(this);
            // mMoveAppButton.setText(getResources().getString(R.string.App_move_sd));
        } else {
            mMoveAppButton.setEnabled(false);
            mMoveAppButton.setFocusable(false);
            mMoveAppButton.setClickable(false);
            mMoveAppButton.setText(getResources().getString(R.string.app_move_nosdcard));
        }
    }
    
    private void initLockButton() {
        // 当童锁密码不存在或者功能没有开启时
        if (mAppLock == null || mAppLock.lockState == 0) {
            mLockButton.setEnabled(false);
            mLockButton.setFocusable(false);
//            mUninstallButton.requestFocus();
        } else {
            mLockButton.setEnabled(true);
            mLockButton.setFocusable(true);
            mLockButton.requestFocus();
            mLockButton.setOnClickListener(this);
            if (mAppInfoBean.lockState == 1) {
                mLockButton.setText(getResources().getString(R.string.applock_unlocked));
            } else {
                mLockButton.setText(getResources().getString(R.string.applock_locked));
            }
        }
    }

    private void initUninstallButtons() {
        mUpdatedSysApp = (mAppEntry.info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
        boolean enabled = true;
        if (mUpdatedSysApp) {
            mUninstallButton.setText(R.string.app_factory_reset);
        } else {
            if ((mAppEntry.info.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                enabled = false;
                if (SUPPORT_DISABLE_APPS) {
                    try {
                        // Try to prevent the user from bricking their phone
                        // by not allowing disabling of apps signed with the
                        // system cert and any launcher app in the system.
                        PackageInfo sys = mPm.getPackageInfo("android",
                                PackageManager.GET_SIGNATURES);
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setPackage(mAppEntry.info.packageName);
                        List<ResolveInfo> homes = mPm.queryIntentActivities(intent, 0);
                        if ((homes != null && homes.size() > 0) ||
                                (mPackageInfo != null && mPackageInfo.signatures != null &&
                                sys.signatures[0].equals(mPackageInfo.signatures[0]))) {
                            // Disable button for core system applications.
                            mUninstallButton.setText(R.string.disable_text);
                        } else if (mAppEntry.info.enabled) {
                            mUninstallButton.setText(R.string.disable_text);
                            enabled = true;
                        } else {
                            mUninstallButton.setText(R.string.enable_text);
                            enabled = true;
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        Log.w(TAG, "Unable to get package info", e);
                    }
                }
            } else {
                mUninstallButton.setText(R.string.uninstall_text);
            }
        }
        // If this is a device admin, it can't be uninstall or disabled.
        // We do this here so the text of the button is still set correctly.
        if (mDpm.packageHasActiveAdmins(mPackageInfo.packageName)) {
            enabled = false;
        }
        mUninstallButton.setEnabled(enabled);
        mUninstallButton.setFocusable(enabled);
        mUninstallButton.setAlpha(enabled ? 1f : 0.7f);
        mUninstallButton.requestLayout();
        if (enabled) {
            // Register listener
            mUninstallButton.setOnClickListener(this);
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mState = ApplicationsState.getInstance(getActivity().getApplication());
        mPm = getActivity().getPackageManager();
        IBinder b = ServiceManager.getService(Context.USB_SERVICE);
        mUsbManager = IUsbManager.Stub.asInterface(b);
        mDpm = (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);

        mCanBeOnSdCardChecker = new CanBeOnSdCardChecker();
        mFilter = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
        mFilter.addDataScheme("package");
        mStorageManager = (StorageManager) getActivity().getSystemService(
                Context.STORAGE_SERVICE);
        mStorageManager.registerListener(mStorageEventListener);
        mContext = getActivity();
    }

    public void initData(){
        mDButil = DBUtil.getInstance(getActivity());
        appList = mDButil.getAllAppRecord();
        if (appList != null && appList.size() > 0) {
            // 获取密码和锁状态
            mAppLock = appList.get(0);
            MyLog(" mAppLock = " + mAppLock.toString());
            // 童锁开启
            if (mAppLock != null && mAppLock.lockState == 1) {
                if (appList.size() > 1) {
                    for (int i = 1; i < appList.size(); i++) {
                        Log.d(TAG, " for list  i = " + i + "  " + appList.get(i));
                        String packagename= appList.get(i).packageName;
                        if (packagename != null && packagename.equals(mPackageName)) {
                            // 获得应用锁的记录
                            mAppInfoBean = appList.get(i);
                            break;
                        }
                    }
                    //没有此应用的加锁数据
                    if (mAppInfoBean == null) {
                        // 没有记录则生成一条数据
                        initThisAppInfo();
                    }
                } else {
                    initThisAppInfo();
                }
                MyLog("----initData--- mAppInfoBean :" + mAppInfoBean.toString());
            }
        }
    }
    
    public void initThisAppInfo() {
        int id = mDButil.insertRecord(mPackageName, mAppLock.password, 0);
        mAppInfoBean = new AppInfoBean();
        mAppInfoBean.packageName = mPackageName;
        mAppInfoBean.password = mAppLock.password;
        mAppInfoBean.lockState = 0;
        mAppInfoBean.ID = id;
        MyLog("----initThisAppInfo--- mAppInfoBean :" + mAppInfoBean.toString());
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.application_info, container, false);
        mToastView = inflater.inflate(R.layout.toast_info, container, false);
        mComputingStr = getActivity().getText(R.string.computing_size);
        mIcon = (ImageView) mView.findViewById(R.id.imageViewApp);
        mLabel = (TextView) mView.findViewById(R.id.text_app_name);
        mTotalSize = (TextView) mView.findViewById(R.id.text_app_size);
        mAppTime = (TextView) mView.findViewById(R.id.text_app_time);
        mDataSize = (TextView) mView.findViewById(R.id.text_app_flash_data_size);
        mExternalDataSize = (TextView) mView.findViewById(R.id.text_app_sd_data_size);
        mCacheSize = (TextView) mView.findViewById(R.id.text_app_cache_size);

        mMoveAppButton = (Button) mView.findViewById(R.id.btn_app_move_sd);
        mUninstallButton = (Button) mView.findViewById(R.id.btn_app_uninstall);
        mClearDataButton = (Button) mView.findViewById(R.id.btn_app_clear_data);
        mClearCacheButton = (Button) mView.findViewById(R.id.btn_app_clear_cache);
        mLockButton = (Button)mView.findViewById(R.id.btn_app_lock);

        LenovoSettingsActivity.setTitleFocus(false);
        mUninstallButton.requestFocus();
        // mUninstallButton.setFocusable(true);
        // mUninstallButton.setFocusableInTouchMode(true);
        initData();
        return mView;
    }

    // Utility method to set applicaiton label and icon.
    private void setAppLabelAndIcon(PackageInfo pkgInfo) {
        mState.ensureIcon(mAppEntry);
        mIcon.setImageDrawable(mAppEntry.icon);
        // Set application name.
        mLabel.setText(mAppEntry.label);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd");
        Date curDate = new Date(mAppEntry.time);
        mAppTime.setText(formatter.format(curDate));
    }

    @Override
    public void onResume() {
        super.onResume();

        mState.resume(this);
        if (!refreshUi()) {
            setIntentAndFinish(true, true);
        }
        getActivity().registerReceiver(mIntentReceiver, mFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        MyLog(" ----onPause---------");
        mState.pause();
        getActivity().unregisterReceiver(mIntentReceiver);
        if (mStorageManager == null) {
            mStorageManager = (StorageManager) getActivity().getSystemService(
                    Context.STORAGE_SERVICE);
        }
        mStorageManager.unregisterListener(mStorageEventListener);
    }

    @Override
    public void onStop() {

        if (unInstallDialog != null && unInstallDialog.isShowing()) {
            unInstallDialog.dismiss();
        }
        if (unInstalledDialog != null && unInstalledDialog.isShowing()) {
            unInstalledDialog.dismiss();
        }
        if (unInstallingDialog != null && unInstallingDialog.isShowing()) {
            unInstallingDialog.dismiss();
        }
        if (mMoveDialog != null && mMoveDialog.isShowing()) {
            mMoveDialog.dismiss();
        }
        super.onStop();
    }

    @Override
    public void onAllSizesComputed() {
    }

    @Override
    public void onPackageIconChanged() {
    }

    @Override
    public void onPackageListChanged() {
        refreshUi();
    }

    @Override
    public void onRebuildComplete(ArrayList<AppEntry> apps) {
    }

    @Override
    public void onPackageSizeChanged(String packageName) {
        MyLog(" ----onPackageSizeChanged--------- packageName = " + packageName);
        try {
            if (packageName.equals(mAppEntry.info.packageName)) {
                refreshSizeInfo();
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    @Override
    public void onRunningStateChanged(boolean running) {
    }

    private boolean refreshUi() {
        MyLog(" ----refreshUi---------");
        if (mMoveInProgress) {
            return true;
        }
        /*
         * final Bundle args = getArguments(); String packageName = (args !=
         * null) ? args.getString(ARG_PACKAGE_NAME) : null; if (packageName ==
         * null) { Intent intent = (args == null) ? getActivity().getIntent() :
         * (Intent) args.getParcelable("intent"); if (intent != null) {
         * packageName = intent.getData().getSchemeSpecificPart(); } } mAppEntry
         * = mState.getEntry(mPackageName);
         */
        mAppEntry = mState.getEntry(mPackageName);

        if (mAppEntry == null) {
            return false; // onCreate must have failed, make sure to exit
        }

        // Get application info again to refresh changed properties of
        // application
        try {
            mPackageInfo = mPm.getPackageInfo(mAppEntry.info.packageName,
                    PackageManager.GET_DISABLED_COMPONENTS |
                            PackageManager.GET_UNINSTALLED_PACKAGES |
                            PackageManager.GET_SIGNATURES);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Exception when retrieving package:" + mAppEntry.info.packageName, e);
            return false; // onCreate must have failed, make sure to exit
        }

        // Get list of preferred activities
        List<ComponentName> prefActList = new ArrayList<ComponentName>();

        // Intent list cannot be null. so pass empty list
        List<IntentFilter> intentList = new ArrayList<IntentFilter>();
        mPm.getPreferredActivities(intentList, prefActList, mPackageName);
        if (localLOGV)
            Log.i(TAG, "Have " + prefActList.size() + " number of activities in prefered list");
        boolean hasUsbDefaults = false;
        try {
            hasUsbDefaults = mUsbManager.hasDefaults(mPackageName);
        } catch (RemoteException e) {
            Log.e(TAG, "mUsbManager.hasDefaults", e);
        }

        // Screen compatibility section.
        ActivityManager am = (ActivityManager)
                getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        int compatMode = am.getPackageScreenCompatMode(mPackageName);

        setAppLabelAndIcon(mPackageInfo);
        refreshButtons();
        refreshSizeInfo();
        return true;
    }

    private void setIntentAndFinish(boolean finish, boolean appChanged) {
        if (localLOGV)
            Log.i(TAG, "appChanged=" + appChanged);
        /*
         * Intent intent = new Intent();
         * intent.putExtra(ManageApplications.APP_CHG, appChanged);
         * PreferenceActivity pa = (PreferenceActivity)getActivity();
         * pa.finishPreferencePanel(this, Activity.RESULT_OK, intent);
         */
    }

    private void refreshSizeInfo() {
        MyLog(" refreshSizeInfo name = " + mAppEntry.label + "mTotalSize = "
                + mAppEntry.size + " codeSize = " + mAppEntry.codeSize
                + " dataSize = " + mAppEntry.dataSize + " mCacheSize = "
                + mAppEntry.cacheSize + " externalDataSize = "
                + mAppEntry.externalDataSize + " externalCodeSize = "
                + mAppEntry.externalCodeSize + " externalSize = "
                + mAppEntry.externalSize + " internalSize = "
                + mAppEntry.internalSize);
        if (mAppEntry.size == ApplicationsState.SIZE_INVALID
                || mAppEntry.size == ApplicationsState.SIZE_UNKNOWN) {
            mLastCodeSize = mLastDataSize = mLastCacheSize = mLastTotalSize = -1;
            if (!mHaveSizes) {
                mDataSize.setText(mComputingStr);
                mExternalDataSize.setText(mComputingStr);
                mCacheSize.setText(mComputingStr);
                mTotalSize.setText(mComputingStr);
            }
            mClearDataButton.setEnabled(false);
            mClearDataButton.setFocusable(false);
            mClearCacheButton.setEnabled(false);
            mClearCacheButton.setFocusable(false);

            mClearDataButton.setAlpha(0.7f);
            mClearCacheButton.setAlpha(0.7f);
        } else {
            MyLog(" refreshSizeInfo mHaveSizes = " + mHaveSizes + " Update UI ---");
            mHaveSizes = true;
            if (mLastCodeSize != mAppEntry.codeSize) {
                mLastCodeSize = mAppEntry.codeSize;
            }
            if (mLastDataSize != mAppEntry.dataSize) {
                mLastDataSize = mAppEntry.dataSize;
                mDataSize.setText(getSizeStr(mAppEntry.dataSize));
            }
            /*
             * if (mLastExternalCodeSize != mAppEntry.externalCodeSize) {
             * mLastExternalCodeSize = mAppEntry.externalCodeSize;
             * mExternalCodeSize
             * .setText(getSizeStr(mAppEntry.externalCodeSize)); }
             */
            if (mLastExternalDataSize != mAppEntry.externalSize) {
                mLastExternalDataSize = mAppEntry.externalSize;
                mExternalDataSize.setText(getSizeStr(mAppEntry.externalSize));
            }
            if (mLastCacheSize != mAppEntry.cacheSize) {
                mLastCacheSize = mAppEntry.cacheSize;
                mCacheSize.setText(getSizeStr(mAppEntry.cacheSize));
            }
            if (mLastTotalSize != mAppEntry.internalSize) {
                mLastTotalSize = mAppEntry.internalSize;
                mTotalSize.setText(getSizeStr(mAppEntry.internalSize));
            }

            if (mAppEntry.dataSize <= 0 || !mCanClearData) {
                mClearDataButton.setEnabled(false);
                mClearDataButton.setFocusable(false);
                mClearDataButton.setAlpha(0.7f);
            } else {
                mClearDataButton.setEnabled(true);
                mClearDataButton.setFocusable(true);
                mClearDataButton.setAlpha(1f);
                mClearDataButton.setOnClickListener(this);
            }
            if (mAppEntry.cacheSize <= 0) {
                mClearCacheButton.setEnabled(false);
                mClearCacheButton.setFocusable(false);
                mClearCacheButton.setAlpha(0.7f);
            } else {
                mClearCacheButton.setEnabled(true);
                mClearCacheButton.setFocusable(true);
                mClearCacheButton.setAlpha(1f);
                mClearCacheButton.setOnClickListener(this);
            }
        }
    }

    /*
     * Private method to handle clear message notification from observer when
     * the async operation from PackageManager is complete
     */
    private void processClearMsg(Message msg) {
        int result = msg.arg1;
        String packageName = mAppEntry.info.packageName;
        mClearDataButton.setText(R.string.clear_user_data_text);
        if (result == OP_SUCCESSFUL) {
            Log.d(TAG, "Cleared user data for package : " + packageName);
            mState.requestSize(mAppEntry.info.packageName);
        } else {
            mClearDataButton.setEnabled(true);
            mClearDataButton.setFocusable(true);
            mClearDataButton.setAlpha(0.7f);
        }
    }

    private void refreshButtons() {
        if (!mMoveInProgress) {
            initUninstallButtons();
            initDataButtons();
            initMoveButton();
            initLockButton();
        } else {
            mMoveAppButton.setText(R.string.moving);
            mMoveAppButton.setEnabled(false);
            mMoveAppButton.setFocusable(false);
            mUninstallButton.setEnabled(false);
            mUninstallButton.setFocusable(false);
            mLockButton.setEnabled(false);
            mLockButton.setFocusable(false);

            mMoveAppButton.setAlpha(0.7f);
            mUninstallButton.setAlpha(0.7f);
            mLockButton.setAlpha(0.7f);
        }
    }

    private void processMoveMsg(Message msg) {
        int result = msg.arg1;
        MyLog(" processMoveMsg move failed result = " + result);
        String packageName = mAppEntry.info.packageName;
        // Refresh the button attributes.
        if (mMoveDialog != null) {
            mMoveDialog.cancel();
            mMoveDialog = null;
        }
        mMoveInProgress = false;
        if (result == PackageManager.MOVE_SUCCEEDED) {
            Log.i(TAG, "Moved resources for " + packageName);
            // Refresh size information again.
            mState.requestSize(mAppEntry.info.packageName);
        } else {
            showDialogInner(DLG_MOVE_FAILED, result);
        }
        refreshUi();
    }

    /*
     * Private method to initiate clearing user data when the user clicks the
     * clear data button for a system package
     */
    private void initiateClearUserData() {
        mClearDataButton.setEnabled(false);
        mClearDataButton.setFocusable(false);
        mClearDataButton.setAlpha(0.7f);
        // Invoke uninstall or clear user data based on sysPackage
        String packageName = mAppEntry.info.packageName;
        Log.d(TAG, "Clearing user data for package : " + packageName);
        if (mClearDataObserver == null) {
            mClearDataObserver = new ClearUserDataObserver();
        }
        ActivityManager am = (ActivityManager)
                getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        boolean res = am.clearApplicationUserData(packageName, mClearDataObserver);
        if (!res) {
            // Clearing data failed for some obscure reason. Just log error for
            // now
            Log.d(TAG, "Couldnt clear application user data for package:" + packageName);
            showDialogInner(DLG_CANNOT_CLEAR_DATA, 0);
        } else {
            mClearDataButton.setText(R.string.recompute_size);
        }
    }

    private void showDialogInner(int id, int moveErrorCode) {
        DialogFragment newFragment = MyAlertDialogFragment.newInstance(id, moveErrorCode);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "dialog " + id);
    }

    /**
     * Dialog
     * 
     * @author xubin
     */
    public static class MyAlertDialogFragment extends DialogFragment {
        public static MyAlertDialogFragment newInstance(int id, int moveErrorCode) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("id", id);
            args.putInt("moveError", moveErrorCode);
            frag.setArguments(args);
            return frag;
        }

        InstalledAppDetails getOwner() {
            return (InstalledAppDetails) getTargetFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            int moveErrorCode = getArguments().getInt("moveError");
            final Dialog dialog;
            dialog = new Dialog(mView.getContext(), R.style.DialogStyle);
            dialog.setContentView(R.layout.application_dialog);
            // ImageView icon = (ImageView) dialog.findViewById(R.id.img_icon);
            // TextView title = (TextView)
            // dialog.findViewById(R.id.textview_title);
            TextView msg = (TextView) dialog.findViewById(R.id.textview_msg);
            Button btn_confirm = (Button) dialog.findViewById(R.id.btn_dlg_confirm);
            Button btn_cancel = (Button) dialog.findViewById(R.id.btn_dlg_cancel);
            Button btn_only_confirm = (Button) dialog.findViewById(R.id.btn_dlg_big_confirm);
            // icon.setImageDrawable(mAppEntry.icon);
            Log.d("joyseee", "onCreateDialog : " + id);
            switch (id) {
                case DLG_CLEAR_DATA:
                    // title.setText(R.string.clear_data_dlg_title);
                    msg.setText(R.string.clear_data_dlg_text);
                    btn_confirm.setVisibility(View.VISIBLE);
                    btn_cancel.setVisibility(View.VISIBLE);
                    btn_confirm.setText(R.string.dlg_confirm);
                    btn_cancel.setText(R.string.dlg_cancel);
                    btn_confirm.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            getOwner().initiateClearUserData();
                            dialog.dismiss();
                        }

                    });
                    btn_cancel.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            dialog.dismiss();
                        }

                    });
                    return dialog;
                case DLG_FACTORY_RESET:
                    // title.setText(R.string.app_factory_reset_dlg_title);
                    msg.setText(R.string.app_factory_reset_dlg_text);
                    btn_confirm.setVisibility(View.VISIBLE);
                    btn_cancel.setVisibility(View.VISIBLE);
                    btn_confirm.setText(R.string.dlg_confirm);
                    btn_cancel.setText(R.string.dlg_cancel);
                    btn_confirm.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            getOwner().uninstallPkg(getOwner().mAppEntry.info.packageName);
                            dialog.dismiss();
                        }

                    });
                    btn_cancel.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            dialog.dismiss();
                        }

                    });
                    return dialog;
                case DLG_APP_NOT_FOUND:
                    // title.setText(R.string.app_not_found_dlg_title);
                    msg.setText(R.string.app_not_found_dlg_text);
                    btn_confirm.setVisibility(View.VISIBLE);
                    btn_cancel.setVisibility(View.VISIBLE);
                    btn_confirm.setText(R.string.dlg_confirm);
                    btn_cancel.setText(R.string.dlg_cancel);
                    btn_confirm.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            getOwner().setIntentAndFinish(true, true);
                            dialog.dismiss();
                        }

                    });
                    btn_cancel.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            dialog.dismiss();
                        }

                    });
                    return dialog;
                case DLG_CANNOT_CLEAR_DATA:
                    // title.setText(R.string.clear_failed_dlg_title);
                    msg.setText(R.string.clear_failed_dlg_text);
                    btn_only_confirm.setVisibility(View.VISIBLE);
                    btn_only_confirm.setText(R.string.dlg_confirm);
                    btn_only_confirm.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            getOwner().mClearDataButton.setFocusable(false);
                            getOwner().mClearDataButton.setAlpha(0.7f);
                            // force to recompute changed value
                            getOwner().setIntentAndFinish(false, false);
                            dialog.dismiss();
                        }

                    });
                    return dialog;
                case DLG_MOVE_FAILED:
                    // CharSequence str =
                    // getActivity().getString(R.string.move_app_failed_dlg_text,
                    // getOwner().getMoveErrMsg(moveErrorCode));
                    CharSequence str = getOwner().getMoveErrMsg(moveErrorCode);
                    // title.setText(R.string.move_app_failed_dlg_title);
                    msg.setText(str);
                    btn_only_confirm.setVisibility(View.VISIBLE);
                    btn_only_confirm.setText(R.string.dlg_confirm);
                    btn_only_confirm.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            // getOwner().mClearDataButton.setFocusable(false);
                            // force to recompute changed value
                            getOwner().setIntentAndFinish(false, false);
                            dialog.dismiss();
                        }

                    });
                    return dialog;
                case DLG_DISABLE:
                    // title.setText(R.string.app_disable_dlg_title);
                    msg.setText(R.string.app_disable_dlg_title);
                    btn_confirm.setVisibility(View.VISIBLE);
                    btn_cancel.setVisibility(View.VISIBLE);
                    btn_confirm.setText(R.string.dlg_confirm);
                    btn_cancel.setText(R.string.dlg_cancel);
                    btn_confirm.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            new DisableChanger(getOwner(), getOwner().mAppEntry.info,
                                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER)
                                    .execute((Object) null);
                            dialog.dismiss();
                        }

                    });
                    btn_cancel.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            dialog.dismiss();
                        }

                    });
                    return dialog;
            }
            throw new IllegalArgumentException("unknown id " + id);
        }

        @Override
        public void dismiss() {

            if (getDialog() != null && getDialog().isShowing()) {
                getDialog().dismiss();
            }
            super.dismiss();
        }
    }

    private void uninstallPkg(String packageName) {
        // Create new intent to launch Uninstaller activity
        /*
         * Uri packageURI = Uri.parse("package:"+packageName); Intent
         * uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
         * startActivity(uninstallIntent); setIntentAndFinish(true, true);
         */

        // mUninstallButton.setEnabled(false);
        // mUninstallButton.setFocusable(false);
        unInstallDialog(packageName);
    }

    static class DisableChanger extends AsyncTask<Object, Object, Object> {
        final PackageManager mPm;
        final WeakReference<InstalledAppDetails> mActivity;
        final ApplicationInfo mInfo;
        final int mDisableState;

        DisableChanger(InstalledAppDetails activity, ApplicationInfo info, int state) {
            mPm = activity.mPm;
            mActivity = new WeakReference<InstalledAppDetails>(activity);
            mInfo = info;
            mDisableState = state;
        }

        @Override
        protected Object doInBackground(Object... params) {
            mPm.setApplicationEnabledSetting(mInfo.packageName, mDisableState, 0);
            return null;
        }
    }

    /*
     * Method implementing functionality of buttons clicked
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    public void onClick(View v) {
        if (mAppEntry == null) {
            return;
        }
        String packageName = mAppEntry.info.packageName;
        if (v == mUninstallButton) {
            if (mUpdatedSysApp) {
                showDialogInner(DLG_FACTORY_RESET, 0);
            } else {
                if ((mAppEntry.info.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                    if (mAppEntry.info.enabled) {
                        showDialogInner(DLG_DISABLE, 0);
                    } else {
                        new DisableChanger(this, mAppEntry.info,
                                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT)
                                .execute((Object) null);
                    }
                } else {
                    uninstallPkg(packageName);
                }
            }
        } else if (v == mClearDataButton) {
            if (mAppEntry.info.manageSpaceActivityName != null) {
                Intent intent = new Intent(Intent.ACTION_DEFAULT);
                intent.setClassName(mAppEntry.info.packageName,
                        mAppEntry.info.manageSpaceActivityName);
                startActivityForResult(intent, -1);
            } else {
                showDialogInner(DLG_CLEAR_DATA, 0);
            }
        } else if (v == mClearCacheButton) {
            // Lazy initialization of observer
            if (mClearCacheObserver == null) {
                mClearCacheObserver = new ClearCacheObserver();
            }
            mPm.deleteApplicationCacheFiles(packageName, mClearCacheObserver);
        } else if (v == mMoveAppButton) {
            if (mPackageMoveObserver == null) {
                mPackageMoveObserver = new PackageMoveObserver();
            }
            int moveFlags = (mAppEntry.info.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0 ?
                    PackageManager.MOVE_INTERNAL : PackageManager.MOVE_EXTERNAL_MEDIA;
            mMoveInProgress = true;
            // String msg = getActivity().getString(R.string.sd_move_prompt);
            // ToastView.show(getActivity(), mToastView, msg, Toast.LENGTH_LONG,
            // false);
            showMoveDialog(mAppEntry.label, moveFlags);
            refreshButtons();
            mPm.movePackage(mAppEntry.info.packageName, mPackageMoveObserver, moveFlags);
        } else if (v == mLockButton) {
            showAppLockDialog();
        }
    }
    //童锁弹出Dialog
    private Dialog mAppLockDialog;

    private void showAppLockDialog() {
        if (mAppLockDialog == null) {
            mAppLockDialog = new Dialog(getActivity(), R.style.config_text_dialog);
            View vi = LayoutInflater.from(getActivity()).inflate(
                    R.layout.config_edit_dialog, null);
            final EditText edit = (EditText) vi
                    .findViewById(R.id.config_dialog_eidt);
            Button ok = (Button) vi
                    .findViewById(R.id.config_dialog_btn_confirm);
            Button cancel = (Button) vi
                    .findViewById(R.id.config_dialog_btn_cancel);
            int width = (int) getResources().getDimension(R.dimen.applock_pwd_dialog_width);
            int height = (int) getResources().getDimension(R.dimen.applock_pwd_dialog_height);
            mAppLockDialog.setContentView(vi, new LinearLayout.LayoutParams(width, height));

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAppLockDialog.dismiss();
                    mAppLockDialog = null;
                }
            });
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String mPassWordStr = null;
                    mPassWordStr = edit.getText().toString();
                    if (mPassWordStr != null && mPassWordStr.length() == 6) {
                        MyLog(" showAppLockDialog mAppInfoBean = " + mAppInfoBean + " mPassWordStr = " + mPassWordStr);
                        if (mAppInfoBean != null) {
                            if (mPassWordStr.equals(mAppLock.password)) {
                                if (mAppInfoBean.lockState == 0) {
                                    mAppInfoBean.lockState = 1;
                                } else {
                                    mAppInfoBean.lockState = 0;
                                }
                                mDButil.updateRecord(mAppInfoBean.ID, mAppLock.password, mAppInfoBean.lockState);
                                if (mAppInfoBean.lockState == 1) {
                                    mLockButton.setText(getResources().getString(R.string.applock_unlocked));
                                } else {
                                    mLockButton.setText(getResources().getString(R.string.applock_locked));
                                }
                                Intent intent = new Intent("joysee.intent.cation_PARENTALCONTROL_APPLOCK_STATE_CHANGE");
                                mContext.sendBroadcast(intent);
                            } else {
                                AdapterViewSelectionUtil.showToast(getActivity(),
                                        R.string.config_midify_old_wrong);
                            }
                        }
                        mAppLockDialog.dismiss();
                        mAppLockDialog = null;
                    } else {
                        AdapterViewSelectionUtil.showToast(getActivity(),
                                R.string.config_midify_password_input_wrong);
                    }
                }
            });
        }
        mAppLockDialog.show();
    }
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        String packageName = mAppEntry.info.packageName;
        ActivityManager am = (ActivityManager)
                getActivity().getSystemService(Context.ACTIVITY_SERVICE);
    }

    public void queryApp() {
        mPackageNameList.clear();
        Cursor cursor = getActivity().getContentResolver().query(
                CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                String packageName = cursor.getString(
                        cursor.getColumnIndex(
                                COLUMN_NAME_PACKAGE_NAME));

                mPackageNameList.add(packageName);
            }
            cursor.close();
        }
    }

    public void insertApp(String packageName) {
        long i = 0;
        ContentValues values = new ContentValues();
        values.put(
                COLUMN_NAME_PACKAGE_NAME,
                packageName);
        values.put(
                COLUMN_NAME_COMMON_APP,
                0);
        values.put(
                COLUMN_NAME_CREATE_DATE,
                i);
        values.put(
                COLUMN_NAME_LAST_USED_DATE,
                i);
        Uri uri = getActivity().getContentResolver().insert(
                CONTENT_URI, values);

        // mCommonPackageList.add(packageName);
    }

    public void deleteApp(String packageName) {
        Cursor cursor = getActivity().getContentResolver().query(
                CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(
                        cursor.getColumnIndex(
                                COLUMN_NAME_PACKAGE_NAME));
                if (packageName.equals(name)) {
                    String id = cursor.getString(
                            cursor.getColumnIndex(
                                    "_id"));
                    Uri deleteUri = Uri.withAppendedPath(
                            CONTENT_URI, id);
                    getActivity().getContentResolver().delete(deleteUri, null, null);
                    cursor.close();
                    // mCommonPackageList.remove(packageName);
                    return;
                }
            }
            cursor.close();
        }
    }

    // 卸载
    private Dialog unInstallDialog;

    private void unInstallDialog(String packageName) {

        final String name = packageName;
        Button btnConfirm, btnCancel;
        if (unInstallDialog == null) {
            unInstallDialog = new Dialog(getActivity(), R.style.DialogStyle);
            unInstallDialog.setContentView(R.layout.system_dialog);
        }
        TextView msg_summary = (TextView) unInstallDialog.findViewById(R.id.msg_summary);
        msg_summary.setText(R.string.uninstall_summary);

        btnConfirm = (Button) unInstallDialog.findViewById(R.id.confirm);
        btnConfirm.setText(R.string.dlg_confirm);
        btnConfirm.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mDButil.dropTable(mPackageName);
                mInstallOperation.start(name);
                unInstallDialog.dismiss();
                mDialog = showUnInstalling(name);
            }

        });

        btnCancel = (Button) unInstallDialog.findViewById(R.id.cancel);
        btnCancel.setText(R.string.dlg_cancel);
        btnCancel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                unInstallDialog.dismiss();
            }

        });
        unInstallDialog.show();
        Log.d("joyseee", "unInstallDialog");
    }

    // 清除数据
    private Dialog unInstalledDialog;

    private void showUnInstalledDialog() {
        if (unInstalledDialog == null) {
            unInstalledDialog = new Dialog(getActivity(), R.style.DialogStyle);
            unInstalledDialog.setContentView(R.layout.application_dialog);
        }
        // ImageView icon = (ImageView) dialog.findViewById(R.id.img_icon);
        // TextView title = (TextView) dialog.findViewById(R.id.textview_title);
        TextView msg = (TextView) unInstalledDialog.findViewById(R.id.textview_msg);
        Button btn_only_confirm = (Button) unInstalledDialog.findViewById(R.id.btn_dlg_big_confirm);
        // icon.setImageDrawable(mDialogIcon);
        // title.setText(mDialogLabel);
        msg.setText(R.string.uninstalled);

        btn_only_confirm.setText(R.string.dlg_confirm);
        btn_only_confirm.setVisibility(View.VISIBLE);
        btn_only_confirm.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                unInstalledDialog.dismiss();
                // initUninstallButtons();
                getActivity().getFragmentManager().popBackStack();
            }
        });
        unInstalledDialog.show();
        Log.d("joyseee", "showUnInstalledDialog");
    }

    private Dialog unInstallingDialog;

    private Dialog showUnInstalling(String packageName) {
        if (unInstallingDialog == null) {
            unInstallingDialog = new Dialog(getActivity(), R.style.DialogStyle);
            unInstallingDialog.setContentView(R.layout.uninstall_dialog);
        }
        ImageView imageView = (ImageView) unInstallingDialog.findViewById(R.id.animation_view);
        // ImageView icon = (ImageView)
        // dialog.findViewById(R.id.uninstall_icon);
        // TextView text = (TextView) dialog.findViewById(R.id.uninstall_text);
        AnimationDrawable animation = (AnimationDrawable) imageView.getBackground();
        mDialogIcon = mAppEntry.icon;
        mDialogLabel = mAppEntry.label;
        // icon.setImageDrawable(mAppEntry.icon);
        // text.setText(mAppEntry.label);
        animation.start();
        unInstallingDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                    KeyEvent event) {

                Log.d(TAG, "KeyCode = " + keyCode);
                return false;
            }

        });
        unInstallingDialog.show();
        Log.d("joyseee", "showUnInstalling");
        return unInstallingDialog;
    }

    // 移至SD卡
    private void showMoveDialog(String packageName, int moveflag) {
        if (mMoveDialog != null) {
            mMoveDialog.cancel();
            mMoveDialog = null;
        }
        mMoveDialog = new Dialog(getActivity(), R.style.DialogStyle);
        mMoveDialog.setContentView(R.layout.system_alert_dialog);
        // TextView title = (TextView) mMoveDialog.findViewById(R.id.title);
        TextView text = (TextView) mMoveDialog.findViewById(R.id.msg);
        ImageView imageView = (ImageView) mMoveDialog.findViewById(R.id.animation_view);
        AnimationDrawable animation = (AnimationDrawable) imageView.getBackground();
        animation.start();
        // title.setText(R.string.move_app_title);
        String name = "\"" + packageName + "\"";
        String msg = null;
        if (moveflag == PackageManager.MOVE_INTERNAL) {
            msg = getActivity().getString(R.string.move_to_nand_prompt, name);
        } else if (moveflag == PackageManager.MOVE_EXTERNAL_MEDIA) {
            msg = getActivity().getString(R.string.move_to_sd_prompt, name);
        }
        text.setText(msg);
        mMoveDialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {

                mMoveDialog = null;
            }

        });
        mMoveDialog.show();
        Log.d("joyseee", "showMoveDialog");
    }

    /*
     * private void showUnInstalling(String packageName) { RelativeLayout
     * relativeLayout = (RelativeLayout)mView.findViewById(R.id.uninstalling);
     * ImageView imageView = (ImageView)mView.findViewById(R.id.animation_view);
     * ImageView icon = (ImageView)mView.findViewById(R.id.uninstall_icon);
     * TextView text = (TextView)mView.findViewById(R.id.uninstall_text);
     * AnimationDrawable animation = (AnimationDrawable)
     * imageView.getBackground(); if(relativeLayout.getVisibility() ==
     * View.VISIBLE) { relativeLayout.setVisibility(View.GONE);
     * animation.stop(); } else { relativeLayout.setVisibility(View.VISIBLE);
     * animation.start(); icon.setImageDrawable(mAppEntry.icon);
     * text.setText(mAppEntry.label); } }
     */

    public class InstallOperation {
        private String mPackageName;
        private Thread mThread;
        private ApkHandleTask mApkHandleTask = new ApkHandleTask();

        InstallOperation() {
        }

        public void start(String packageName) {
            mPackageName = packageName;
            mThread = new Thread(mApkHandleTask);
            mThread.start();
        }

        class ApkHandleTask implements Runnable {

            // public static final String TAG = "ApkHandleTask";

            class PackageDeleteObserver extends IPackageDeleteObserver.Stub {
                String pkgpath = null;

                public void packageDeleted(String packageName, int returnCode) {
                    if (returnCode == PackageManager.DELETE_SUCCEEDED) {
                        Log.d(TAG, "packageDeleted");
                    }
                }
            }

            public void uninstall_apk_slient(String apk_pkgname) {
                PackageDeleteObserver observer = new PackageDeleteObserver();
                observer.pkgpath = apk_pkgname;
                PackageManager pm = getActivity().getPackageManager();
                pm.deletePackage(apk_pkgname, observer, 0);
            }

            public void run() {
                uninstall_apk_slient(mPackageName);
            }
        }
    }

    public void MyLog(String log) {
        if (DEBUG) {
            Log.d(TAG, "-----------" + log);
        }
    }
}
