
package com.lenovo.settings.update;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import android.app.Instrumentation;
import android.view.KeyEvent;
import android.graphics.drawable.AnimationDrawable;

import com.lenovo.settings.LenovoSettingsActivity;
import com.lenovo.settings.PowerOffDialog;
import com.lenovo.settings.R;
import com.lenovo.settings.SettingBroadcastReceiver;
import com.lenovo.settings.Object.UpdateData;
import com.lenovo.settings.Util.Recovery;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RecoverySystem;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.view.Gravity;
import android.widget.Toast;

public class UpdateService extends Service {
    public static final String UPDATE_SERVICE_NAME = "com.lenovo.settings.update.UpdateService";
    public static final String BROADCAST_UPGRADE_ACTION = "com.lenovo.UPGRADE";
    public static final String TAG = "UpdateService";
    public static final boolean DEBUG = true;
    
    public static final int DOWNLOADING = 1;
    public static final int SHOW_UPDATE_DIALOG = 2;
    private static final int MSG_SHOW_UPDATE_REBOOT = 1000;
    private static final int MSG_SHOW_POWER_OFF_DIALOG = 1001;
    
    private String mUrl;
    private String mFileName;
    private String mPath;
    
    protected long mCurSize = 0;
    protected long mFileSize = 0;
    protected boolean mConnectStatus, mStop;
    private String mMd5;
    private String mUpdateType;
    private Context mContext;
    private IntentFilter mFilterMedia;
    private Dialog mDialog;
    private Dialog mPowerOffDialog;
    
    private BroadcastReceiver mMediaStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
                String path = intent.getData().toString().substring("file://".length());
                Log.d(TAG, " ACTION_MEDIA_EJECT  onReceive path = " + path);
                if (path.equals("/mnt/sdcard")
                        && UpdateData.Path.equals(com.lenovo.settings.Util.FileUtils.SD_PARH)) {
                    String msg = getString(R.string.update_no_sdcard);
                    UpdateStatus.setError(true);
                    UpdateStatus.clearUpdateStatus();
                    UpdateStatus.setTitleMsg(msg);
                    UpdateStatus.sendBroadcastReceiver(getBaseContext(),
                            UpdateStatus.UPDATE_MSG_CHANGE_ACTION);
                    mConnectStatus = false;
                    try {
                        mDownloadThread.interrupt();
                        mDownloadThread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    mStop = true;
                    stopSelf();
                }
            }
        }
    };
    
    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("server onCreate");
        mFilterMedia = new IntentFilter();
        mFilterMedia.addAction(Intent.ACTION_MEDIA_MOUNTED);
        mFilterMedia.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        mFilterMedia.addAction(Intent.ACTION_MEDIA_EJECT);
        mFilterMedia.addAction(Intent.ACTION_MEDIA_REMOVED);
        mFilterMedia.addDataScheme("file");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("server onStart");
        mContext = getBaseContext();
        mConnectStatus = true;
        mStop = false;
        if (intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }
        if (DEBUG) {
            Log.d(TAG,
                    " onStartCommand intent= " + intent + " url= " + intent.getStringExtra("url"));
        }
        mUrl = intent.getStringExtra("url");
        mFileName = intent.getStringExtra("name");
        mPath = intent.getStringExtra("path");
        mMd5 = intent.getStringExtra("md5");
        mFileSize = intent.getLongExtra("size", 0);
        mUpdateType = intent.getStringExtra("attr");
        // update by yuhongkun 20120806
        try {
            mDownloadThread.start();
            mRefreshHandler.sendEmptyMessageDelayed(DOWNLOADING, 2000);
        } catch (IllegalThreadStateException ex) {
            Log.e(TAG, "the thread is started already!");
        }
        registerReceiver(mMediaStatusReceiver, mFilterMedia);
        sendUpgradeBroadcast();
        Log.e(TAG, " onStart url = " + mUrl + ",filename = " +
                mFileName + ",path " + mPath + ",filesize = " + mFileSize +
                ",Md5 = " + mMd5);
        return super.onStartCommand(intent, flags, startId);
    }
    
//    public boolean isConnectInternet() {
//        ConnectivityManager conManager = (ConnectivityManager)
//                getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = conManager.getActiveNetworkInfo();
//        if (networkInfo != null) { // 注意，这个判断一定要的哦，要不然会出错
//            return networkInfo.isAvailable();
//        }
//        return false;
//    }
    
    private boolean checkNetConnected() {
        ConnectivityManager conManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfo = conManager.getAllNetworkInfo();
        Log.i(TAG, " checkNetConnected networkInfo length = " + networkInfo.length);
        if (networkInfo != null) {
            for (int i = 0; i < networkInfo.length; i++) {
                if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
                    Log.i(TAG, " CONNECTED " + networkInfo[i].toString());
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        mConnectStatus = false;
        mStop = true;
        try {
             mDownloadThread.interrupt();
             mDownloadThread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (mMediaStatusReceiver != null) {
            unregisterReceiver(mMediaStatusReceiver);
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        
        return null;
    }
    
    Handler mRefreshHandler = new Handler() {
        
        @Override
        public void handleMessage(Message msg) {
            String str;
            Log.d(TAG, "mConnectStatus:" + mConnectStatus + " msg = " + msg.toString());
            switch (msg.what) {
                case DOWNLOADING:
                    if (mConnectStatus && !mStop) {
                        Log.e(TAG, "progress = " + UpdateStatus.getProgress());
                        String prg = Integer.toString(UpdateStatus.getProgress()) + "%";
                        if ((mCurSize / 1024) != 0) {
                            UpdateStatus.setProgress(getProgress(mCurSize / 1024, mFileSize));
                        }
                        if (UpdateData.Path.equals(com.lenovo.settings.Util.FileUtils.SD_PARH)) {
                            str = mContext.getString(R.string.update_downloading_sdcard, prg);
                        } else {
                            str = mContext.getString(R.string.update_downloading_data, prg);
                        }
                        UpdateStatus.clearUpdateStatus();
                        UpdateStatus.setError(false);
                        String titleStr = mContext.getString(R.string.found_new_version)
                                + UpdateData.Version;
                        UpdateStatus.setTitleMsg(titleStr);
                        UpdateStatus.setDownloadMsg(str);
                        UpdateStatus.sendBroadcastReceiver(getBaseContext(),
                                UpdateStatus.UPDATE_MSG_CHANGE_ACTION);
                        mRefreshHandler.sendEmptyMessageDelayed(DOWNLOADING, 1000);
                    } else {
                        UpdateStatus.clearUpdateStatus();
                        UpdateStatus.sendBroadcastReceiver(getBaseContext(),
                                UpdateStatus.UPDATE_MSG_CHANGE_ACTION);
                    }
                    break;
                case SHOW_UPDATE_DIALOG:
                    final String strParameter = (String) msg.obj;
                    Log.d(TAG, " strParameter = " + strParameter + " mUpdateType = " + mUpdateType.trim());
                    if (UpdateStatus.UPDATE_ATTRIBUTE_ENFORCE.equalsIgnoreCase(mUpdateType.trim())) {
                        ShowEnforceUpdateDialog(strParameter);
                    } else {
                        ShowAutoUpdateDialog(strParameter);
                    }
                    break;
                case MSG_SHOW_UPDATE_REBOOT:
                    String path = (String) msg.obj;
                    Log.d(TAG, "update path = " + path);
                    Recovery.reboot(UpdateService.this, path);
                    Log.d(TAG, "................reboot...." + path);
                    break;
                case MSG_SHOW_POWER_OFF_DIALOG:
                    if (mPowerOffDialog == null) {
                        mPowerOffDialog = new Dialog(UpdateService.this, R.style.DialogStyle);
                    }
                    if (mPowerOffDialog.isShowing()) {
                        mPowerOffDialog.dismiss();
                    }
                    Window window2 = mPowerOffDialog.getWindow();
                    window2.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//                    window2.requestFeature(Window.FEATURE_NO_TITLE);
//                    WindowManager.LayoutParams winManager2 = window2.getAttributes();
//                    winManager2.y = 50;
                    mPowerOffDialog.setContentView(R.layout.power_off_dialog);
                    ImageView imageView = (ImageView) mPowerOffDialog
                            .findViewById(R.id.animation_view);
                    // TextView title = (TextView)
                    // dialog.findViewById(R.id.textTitle);
                    TextView textMsg = (TextView) mPowerOffDialog
                            .findViewById(R.id.textMsg);
                    AnimationDrawable animation = (AnimationDrawable) imageView
                            .getBackground();
                    // title.setText(context.getText(R.string.power_off));
                    textMsg.setText(getText(R.string.shutdown_progress));
                    animation.start();
                    mPowerOffDialog.show();
                    break;
            }
        }
        
    };
    
    private void ShowAutoUpdateDialog(final String strParameter) {
        new Thread(new Runnable() {
            public void run() {
                Instrumentation inst = new Instrumentation();
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_F3);
            }
        }).start();
        // final String strParameter =(String) msg.obj;
        LayoutInflater inflater2 = LayoutInflater.from(UpdateService.this);
        RelativeLayout linearLayout2 = (RelativeLayout) inflater2.inflate(R.layout.update_dialog,
                null);
        
        TextView tMsgTextView = (TextView) linearLayout2.findViewById(R.id.textview_dialog);
        tMsgTextView.setText(R.string.dlg_auto_update_msg);
        final Button btnConfirm = (Button) linearLayout2.findViewById(R.id.btn_dlg_confirm);
        btnConfirm.setText(R.string.update_yes);
        Button btnCancel = (Button) linearLayout2.findViewById(R.id.btn_dlg_cancel);
        btnCancel.setText(R.string.update_no);
        if (mDialog == null) {
            mDialog = new Dialog(UpdateService.this, R.style.DialogStyle);
        }
        if(mDialog.isShowing()){
            mDialog.dismiss();
        }
        Window window = mDialog.getWindow();
        window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);//if  not set then show failed
//        window.requestFeature(Window.FEATURE_NO_TITLE);
//        WindowManager.LayoutParams winManager = window.getAttributes();
//        winManager.y = 50;
        
        btnConfirm.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                
                mRefreshHandler.sendEmptyMessage(MSG_SHOW_POWER_OFF_DIALOG);
                Message msg = new Message();
                msg.what = MSG_SHOW_UPDATE_REBOOT;
                msg.obj = strParameter;// Rony modify 20120425
                mRefreshHandler.sendMessage(msg);
                if (mDialog.isShowing()) {
                    mDialog.dismiss();
                }
                /*
                 * try {
                 * File file = new File(mPath);
                 * RecoverySystem.installPackage(UpdateDialog.this, file);
                 * } catch (IOException e) {
                 * }
                 */
                
            }
        });
        
        btnCancel.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                
                sendBroadcast();
                String str = getString(R.string.dlg_update_power_on_msg);
                showToastView(str);
                Recovery.reboot = false;
                Recovery.reboot(UpdateService.this, strParameter);
                if (mDialog.isShowing()) {
                    mDialog.dismiss();
                }
                stopSelf();
            }
        });
        mRefreshHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                btnConfirm.setFocusable(true);
                btnConfirm.requestFocus();
            }
        }, 500);
        mDialog.setContentView(linearLayout2);
        mDialog.show();
    }
    
    private void ShowEnforceUpdateDialog(final String strParameter) {
        new Thread(new Runnable() {
            public void run() {
                Instrumentation inst = new Instrumentation();
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_F3);
            }
        }).start();
        // final String strParameter =(String) msg.obj;
        LayoutInflater inflater2 = LayoutInflater.from(UpdateService.this);
        RelativeLayout linearLayout2 = (RelativeLayout) inflater2.inflate(R.layout.update_dialog,
                null);
        
        TextView tMsgTextView = (TextView) linearLayout2.findViewById(R.id.textview_dialog);
        tMsgTextView.setText(R.string.dlg_auto_update_msg);
        Button btnConfirm = (Button) linearLayout2.findViewById(R.id.btn_dlg_confirm);
        btnConfirm.requestFocus();
        btnConfirm.setText(R.string.update_yes);
        btnConfirm.setGravity(Gravity.CENTER_HORIZONTAL);
        Button btnCancel = (Button) linearLayout2.findViewById(R.id.btn_dlg_cancel);
        btnCancel.setText(R.string.update_no);
        btnCancel.setVisibility(View.GONE);
        
        if (mDialog == null) {
            mDialog = new Dialog(UpdateService.this, R.style.DialogStyle);
        }
        if(mDialog.isShowing()){
            mDialog.dismiss();
        }
        Window window = mDialog.getWindow();
        window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);//if  not set then show failed
//        window.requestFeature(Window.FEATURE_NO_TITLE);
//        WindowManager.LayoutParams winManager = window.getAttributes();
//        winManager.y = 50;
        
        btnConfirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                
                mRefreshHandler.sendEmptyMessage(MSG_SHOW_POWER_OFF_DIALOG);
                Message msg = new Message();
                msg.what = MSG_SHOW_UPDATE_REBOOT;
                msg.obj = strParameter;// Rony modify 20120425
                mRefreshHandler.sendMessage(msg);
                if (mDialog.isShowing()) {
                    mDialog.dismiss();
                }
                /*
                 * try {
                 * File file = new File(mPath);
                 * RecoverySystem.installPackage(UpdateDialog.this, file);
                 * } catch (IOException e) {
                 * }
                 */
                
            }
            
        });
//        /* add by yuhongkun 过滤掉返回等按键，必须要升级。 */
//        linearLayout2.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
//                    return true;
//                }
//                return false;
//            }
//        });
        mDialog.setContentView(linearLayout2);
        mDialog.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
                    return true;
                }
                return false;
            }
        });
        mDialog.show();
//        LenovoSettingsActivity.isUpdateDialog = true;
    }
    
    void sendBroadcast() {
        UpdateStatus.clearUpdateStatus();
        UpdateStatus.setError(true);
        UpdateStatus.sendBroadcastReceiver(this, UpdateStatus.UPDATE_MSG_CHANGE_ACTION);
    }
    
    void showToastView(String msg) {
        View view = LayoutInflater.from(this).inflate(R.layout.toast_info, null);
        TextView textView = (TextView) view.findViewById(R.id.toast_text);
        ImageView img = (ImageView) view.findViewById(R.id.toastImage);
        textView.setText(msg);
        img.setVisibility(View.GONE);
        Toast toast = new Toast(this);
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
    
    Thread mDownloadThread = new Thread() {
        @Override
        public void run() {
            http_request_loop:
            while (mConnectStatus) {
                File file;
                FileOutputStream fileOutputStream = null;
                InputStream inputStream = null;
                BufferedInputStream bufferedInputStream = null;
                BufferedOutputStream bufferedOutputStream = null;
                UpdateCheck check = new UpdateCheck(getBaseContext());
                String str = "";
                try {
                    Thread.sleep(1000);
                    File updateFile = new File(mPath+mFileName);
                    //如果升级文件已经存在
                    if(updateFile.exists()){
                        if (DEBUG) {
                            Log.d(TAG, " mDownloadThread updateFile.exists() " + mPath + " mFileName = "
                                    + mFileName );
                        }
                        FileInputStream fis = new FileInputStream(updateFile);
                        mCurSize = fis.available();
                        if ((mCurSize / 1024) == mFileSize) {
                            mRefreshHandler.removeMessages(DOWNLOADING);
                            UpdateStatus.clearUpdateStatus();
                            str = getString(R.string.update_download_ok);
                            check.sendBroadcast(mContext, UpdateCheck.MSG_DOWNLOAD, str, false);
                            UpdateStatus.clearUpdateStatus();
                            str = getString(R.string.update_checking);
                            check.sendBroadcast(mContext, UpdateCheck.MSG_CHECK, str, false);
                            if (check.checkMD5(mPath + mFileName, mMd5)) {
                                UpdateStatus.clearUpdateStatus();
                                str = getString(R.string.update_check_ok);
                                check.sendBroadcast(mContext, UpdateCheck.MSG_CHECK, str, false);
                                mRefreshHandler.removeMessages(SHOW_UPDATE_DIALOG);
                                Message tDialogMsg = mRefreshHandler
                                        .obtainMessage(SHOW_UPDATE_DIALOG);
                                tDialogMsg.obj = mPath + mFileName;
                                mRefreshHandler.sendMessage(tDialogMsg);
                                mStop = true;
                            } else {
                                str = getString(R.string.update_check_fail);
                                check.sendBroadcast(mContext, UpdateCheck.MSG_CHECK, str, true);
                                updateFile.delete();
                                break;
                            }
                        }
                    }
                    String filename = getFileName(mPath, mFileName);
                    if (DEBUG) {
                        Log.d(TAG, " mDownloadThread mPath = " + mPath + " mFileName = "
                                + mFileName + " filename = " + filename);
                    }
                    if (DEBUG) {
                        Log.d(TAG, " mDownloadThread mCurSize=" + mCurSize
                                + " mCurSize/1024=" + mCurSize / 1024 + " mFileSize= "
                                + mFileSize);
                    }
                    file = new File(filename);
                    FileUtils.setPermissions(filename, 0666, -1, -1);
                    if (file.exists()) {
                        FileInputStream fis = new FileInputStream(file);
                        mCurSize = fis.available();
                    } else {
                        file.createNewFile();
                        mCurSize = 0;
                    }
                    if ((mCurSize / 1024) == mFileSize) { 
                        // mRefreshHandler.removeMessages(DOWNLOADING);
                        if (!check.checkMD5(filename, mMd5)) {
                            file.delete();
                            mCurSize = 0;
                            continue http_request_loop;
                        }
                        // mRefreshHandler.sendEmptyMessageDelayed(DOWNLOADING,
                        // 1000);
                    } else if ((mCurSize / 1024) > mFileSize) {
                        file.delete();
                        mCurSize = 0;
                        continue http_request_loop;
                    } else {
                        URL url = new URL(mUrl);
                        Log.d(TAG, " mDownloadThread mUrl = " + mUrl + " mStop = " + mStop);
                        HttpURLConnection httpConnection = (HttpURLConnection) url
                                .openConnection();
                        // Log.d(TAG, " httpConnection.getContentLength() " +
                        // httpConnection.getContentLength());
                        httpConnection.setRequestProperty("User-Agent", "Android");
                        httpConnection.setConnectTimeout(10000);
                        httpConnection.setReadTimeout(10000);
                        String sProperty = "bytes=" + mCurSize + "-";
                        // 因为这个是post请求,设立需要设置为true
                        httpConnection.setDoOutput(true);
                        httpConnection.setDoInput(true);
                        // 设置以POST方式
                        httpConnection.setRequestMethod("POST");
                        // Post 请求不能使用缓存
                        httpConnection.setUseCaches(false);
                        httpConnection.setInstanceFollowRedirects(true);
                        httpConnection.setRequestProperty("RANGE", sProperty);
                        httpConnection.connect();
                        inputStream = httpConnection.getInputStream();
                        bufferedInputStream = new BufferedInputStream(inputStream);
                        fileOutputStream = new FileOutputStream(file, true);
                        bufferedOutputStream = new BufferedOutputStream(
                                fileOutputStream);
                        byte[] buf = new byte[1024 * 8];
                        if (bufferedInputStream != null) {
                            int ch = -1;
                            while (((ch = bufferedInputStream.read(buf)) != -1) && mConnectStatus
                                    && !mStop) {
                                bufferedOutputStream.write(buf, 0, ch);
                                bufferedOutputStream.flush();
                                // fileOutputStream.getFD().sync();
                                mCurSize += ch;
                            }
                        }
                    }
                    bufferedOutputStream.close();
                    fileOutputStream.close();
                    bufferedInputStream.close();
                    inputStream.close();
                    if (mStop) {
                        break;
                    }
                    Log.d(TAG, "download firmware ok!");
                    str = getString(R.string.update_download_ok);
                    check.sendBroadcast(mContext, UpdateCheck.MSG_DOWNLOAD, str, false);
                    
                    Log.d(TAG, "checking firmware!");
                    str = getString(R.string.update_checking);
                    check.sendBroadcast(mContext, UpdateCheck.MSG_CHECK, str, false);
                    if (!check.checkMD5(filename, mMd5)) {
                        str = getString(R.string.update_check_fail);
                        check.sendBroadcast(mContext, UpdateCheck.MSG_CHECK, str, true);
                        file.delete();
                        break;
                    }
                    Log.d(TAG, file.getName() + file.getPath() + " renameTo " + mPath + mFileName);
                    file.renameTo(new File(mPath + mFileName));
                    boolean isDelete = file.delete();
                    Log.d(TAG, "check firmware ok! file.delete() = " + isDelete);
                    str = getString(R.string.update_check_ok);
                    check.sendBroadcast(mContext, UpdateCheck.MSG_CHECK, str, false);
                    
                    mRefreshHandler.removeMessages(SHOW_UPDATE_DIALOG);
                    Message tDialogMsg = mRefreshHandler.obtainMessage(SHOW_UPDATE_DIALOG);
                    tDialogMsg.obj = mPath + mFileName;
                    mRefreshHandler.sendMessage(tDialogMsg);
                    mStop = true;
                } catch (InterruptedException e) {
                    Log.e(TAG, " InterruptedException ");
                    e.printStackTrace();
                    break;
                } catch (FileNotFoundException e) {
                    Log.e(TAG, " FileNotFoundException ");
                    e.printStackTrace();
                } catch(SocketTimeoutException e){
                    e.printStackTrace();
                    mConnectStatus = checkNetConnected();
                    Log.e(TAG, " SocketTimeoutException mConnectStatus = " + mConnectStatus);
                    mRefreshHandler.removeMessages(DOWNLOADING);
                    UpdateStatus.clearUpdateStatus();
                    str = getString(R.string.connect_service_fail);
                    check.sendBroadcast(mContext, UpdateCheck.MSG_TITLE, str, true);
                }
//                catch (IOException e) {
//                    e.printStackTrace();
//                    mConnectStatus = checkNetConnected();
//                    Log.e(TAG, " IOException mConnectStatus = " + mConnectStatus);
//                    // if(!mConnectStatus){
//                    mRefreshHandler.removeMessages(DOWNLOADING);
//                    UpdateStatus.clearUpdateStatus();
//                    str = getString(R.string.update_io_fail);
//                    check.sendBroadcast(mContext, UpdateCheck.MSG_TITLE, str, true);
//                    // }
//                    // continue http_request_loop;
//                } 
                catch (Exception e) {
                    e.printStackTrace();
                    mConnectStatus = checkNetConnected();
                    Log.e(TAG, " mConnectStatus = " + mConnectStatus);
                    // if(!mConnectStatus){
                    mRefreshHandler.removeMessages(DOWNLOADING);
                    UpdateStatus.clearUpdateStatus();
                    str = getString(R.string.update_error);
                    check.sendBroadcast(mContext, UpdateCheck.MSG_TITLE, str, true);
                    // }
                } finally {
                    Log.e(TAG, " finally ");
                    mStop = true;
                    mConnectStatus = false;
                    if (null != bufferedOutputStream) {
                        try {
                            bufferedOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e(TAG, "close bufferedOutputStream fail!");
                        }
                    }
                    if (null != fileOutputStream) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e(TAG, "close fileoutputstream fail!");
                        }
                    }
                    if (null != bufferedInputStream) {
                        try {
                            bufferedInputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e(TAG, "close bufferedInputStream fail!");
                        }
                    }
                    if (null != inputStream) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e(TAG, "close inputStream fail!");
                        }
                    }
                    stopSelf();
                    try {
                        this.finalize();
                    } catch (Throwable e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
        
    };
    
    int getProgress(long cur_size, long total_size) {
        return (int) ((cur_size * 100) / total_size);
    }
    
    void sendUpgradeBroadcast() {
        Intent intent = new Intent(BROADCAST_UPGRADE_ACTION);
        mContext.sendBroadcast(intent);
        Log.e(TAG, "send action = " + BROADCAST_UPGRADE_ACTION);
    }
    
    private String getFileName(String path, String name) {
        int end = name.lastIndexOf(".");
        String str;
        if (end <= 0) {
            str = path + name + ".tmp";
        } else {
            str = path + name.substring(0, end) + ".tmp";
        }
        Log.d(TAG, "getFileName name = " + str);
        return str;
    }
}
