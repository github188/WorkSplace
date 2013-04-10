package com.lenovo.settings.EnforceUpdate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.app.Instrumentation;
import android.view.KeyEvent;
import android.graphics.drawable.AnimationDrawable;
import com.lenovo.settings.R;
import com.lenovo.settings.update.UpdateService;
import com.lenovo.settings.update.UpdateStatus;
import com.lenovo.settings.Util.Recovery;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
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

public class EnforceUpdateService extends Service{
	public static final String UPDATE_SERVICE_NAME = "com.lenovo.settings.EnforceUpdate.EnforceUpdateService";
	public static final String BROADCAST_UPGRADE_ACTION = "com.lenovo.UPGRADE";
	public static final String TAG = "EnforceUpdateService";
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
	protected boolean mConnectStatus,mStop;
	private String mMd5;
	private Context mContext;
	private IntentFilter mFilterMedia;
	private AlertDialog mDialog;
	private Dialog mPowerOffDialog;
	
	private BroadcastReceiver mMediaStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
	
			String action = intent.getAction();
			if(action.equals(Intent.ACTION_MEDIA_EJECT)){
				String path = intent.getData().toString().substring("file://".length());
				Log.d(TAG, "-------------------ACTION_MEDIA_EJECT"+path);
				if(path.equals("/mnt/sdcard")){
					mStop = true;
//					String msg = getString(R.string.update_no_sdcard);
//					UpdateStatus.setError(true);
//					UpdateStatus.clearUpdateStatus();
//					UpdateStatus.setTitleMsg(msg);
//					UpdateStatus.sendBroadcastReceiver(getBaseContext(), UpdateStatus.UPDATE_MSG_CHANGE_ACTION);
//					//mConnectStatus = false;
//					//mDownloadThread.interrupt();
//					stopSelf();
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
		if(intent==null){
			return  super.onStartCommand(intent, flags, startId);
		}
		if(DEBUG){
			Log.d(TAG, "===========onStartCommand=======intent="+intent);
			Log.d(TAG, "===========onStartCommand=======intent="+intent+"=============url="+intent.getStringExtra("url"));
		}
		mUrl = intent.getStringExtra("url");
		mFileName = intent.getStringExtra("name");
		mPath = intent.getStringExtra("path");
		mMd5 = intent.getStringExtra("md5");
		mFileSize = intent.getLongExtra("size", 0);
//		mRefreshHandler.sendEmptyMessageDelayed(DOWNLOADING, 1000);
//		update by yuhongkun 20120806
		try{
			mDownloadThread.start();
		}catch(IllegalThreadStateException ex){
			Log.e(TAG, "the thread is started already!");
		}
		registerReceiver(mMediaStatusReceiver, mFilterMedia);
		sendUpgradeBroadcast();
		Log.e("onStart", "url = " + mUrl + ",filename = " + 
				mFileName+ ",path "+mPath+",filesize = "+mFileSize +
				",Md5 = "+mMd5);
		return super.onStartCommand(intent, flags, startId);
	}


//	public boolean isConnectInternet() {
//
//		ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//
//		NetworkInfo networkInfo = conManager.getActiveNetworkInfo();
//
//		if (networkInfo != null) { // 注意，这个判断一定要的哦，要不然会出错
//			return networkInfo.isAvailable();
//
//		}
//		return false;
//
//	}
	
	private boolean checkNetConnected(){

        ConnectivityManager conManager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo[] networkInfo = conManager.getAllNetworkInfo();

        if(networkInfo != null)
        {
            for (int i = 0; i < networkInfo.length; i++)
            {
                if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED)
                {
                     Log.i("isConnectInternet", "============isConnectInternet============== i = "+i);
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
		mDownloadThread.interrupt();
		unregisterReceiver(mMediaStatusReceiver);
	}



	@Override
	public IBinder onBind(Intent intent) {

		return null;
	}
	
	Handler mRefreshHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
	
			String str;
			System.out.println("mConnectStatus:"+mConnectStatus);
			switch(msg.what){
			case SHOW_UPDATE_DIALOG:
				new Thread(new Runnable() {
					public void run() {
						Instrumentation inst = new Instrumentation();
						inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP);
					}
				}).start();
				//showUpdateDialog();
				final String strParameter =(String) msg.obj;
				LayoutInflater inflater2 =LayoutInflater.from(EnforceUpdateService.this);
				RelativeLayout linearLayout2 = (RelativeLayout)inflater2.inflate(R.layout.update_dialog, null);
                
                TextView tMsgTextView = (TextView) linearLayout2.findViewById(R.id.textview_dialog);
                tMsgTextView.setText(R.string.dlg_enforce_update_msg);
                Button btnConfirm = (Button) linearLayout2.findViewById(R.id.btn_dlg_confirm);
                btnConfirm.requestFocus();
                btnConfirm.setText(R.string.update_yes);
                Button btnCancel = (Button) linearLayout2.findViewById(R.id.btn_dlg_cancel);
                btnCancel.setText(R.string.update_no);
                
//                TextView textView = (TextView) linearLayout2.findViewById(R.id.update_dialog);
//                textView.setText(""+getResources().getString(R.string.appointment_program_ask_left)+""+
//                        tChannelName+" "+tEventName+""
//                           +getResources().getString(R.string.appointment_program_ask_right));
//                final Button btOk = (Button) linearLayout2.findViewById(R.id.appointment_button_ok);
//                final Button btNo = (Button) linearLayout2.findViewById(R.id.appointment_button_no);
                mDialog =new AlertDialog.Builder(EnforceUpdateService.this.getApplicationContext()).create();
                Window window = mDialog.getWindow();
                window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);//if  not set then show failed
                window.requestFeature(Window.FEATURE_NO_TITLE);
                WindowManager.LayoutParams winManager = window.getAttributes();
                winManager.y = 50;
                
                btnConfirm.setOnClickListener(new OnClickListener() {

        			@Override
        			public void onClick(View v) {
        		

        				mRefreshHandler.sendEmptyMessage(MSG_SHOW_POWER_OFF_DIALOG);
        				Message msg = new Message();
        				msg.what = MSG_SHOW_UPDATE_REBOOT;
        				msg.obj = strParameter;// Rony modify 20120425
        				mRefreshHandler.sendMessage(msg);
        				 if(mDialog.isShowing()){
                             mDialog.dismiss();
                         }
        				/*try {
        					File file = new File(mPath);
        					RecoverySystem.installPackage(UpdateDialog.this, file);
        				} catch (IOException e) {
        						
        				}*/
        				
        			}
                	
                });
                
                btnCancel.setOnClickListener(new OnClickListener() {

        			@Override
        			public void onClick(View v) {
        		
        				sendBroadcast();
        				Recovery.reboot(EnforceUpdateService.this, strParameter);
        				String str = getString(R.string.dlg_update_power_on_msg);
        				showToastView(str);
        				 if(mDialog.isShowing()){
                             mDialog.dismiss();
                         }
        				 stopSelf();
        			}
                	
                });
//                btOk.setOnClickListener(new OnClickListener(){
//                    public void onClick(View v) {
//                        isOperate = true;
//                        watchProgram(event);
//                        if(mDialog.isShowing()){
//                            mDialog.dismiss();
//                        }
//                    }
//                    
//                });
//                btNo.setOnClickListener(new OnClickListener() {
//                    
//                    public void onClick(View v) {
//                        isOperate = true;
//                        if(mDialog.isShowing()){
//                            mDialog.dismiss();
//                        }
//                    }
//                });
//                btOk.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                    public void onFocusChange(View v, boolean hasFocus) {
//                        if (hasFocus) {
//                            btOk.setTextColor(Color.WHITE);
//                        } else {
//                            btOk.setTextColor(Color.WHITE);
//                        }
//                    }
//                });
//                btNo.setOnFocusChangeListener(new OnFocusChangeListener() {
//
//                    public void onFocusChange(View v, boolean hasFocus) {
//                        if (hasFocus) {
//                            btNo.setTextColor(Color.WHITE);
//                        } else {
//                            btNo.setTextColor(Color.WHITE);
//                        }
//                    }
//                });
                mDialog.setView(linearLayout2);
                mDialog.show();
				break;
				
			case MSG_SHOW_UPDATE_REBOOT:
				String path = (String) msg.obj;
				Log.e(TAG,"update path = "+path);
				Recovery.reboot(EnforceUpdateService.this, path);
				break;
			case MSG_SHOW_POWER_OFF_DIALOG:
				// Rony add customer power off dialog for lenovo,
				//	framework must be close the dialog 20120405 
				//Intent intent = new Intent(UpdateDialog.this,PowerOffDialog.class); 
				//intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				//startActivity(intent);
//				PowerOffDialog.showPowerOffDialog(EnforceUpdateService.this.getApplicationContext());
				
				mPowerOffDialog =new Dialog(EnforceUpdateService.this,R.style.DialogStyle);
                Window window2 = mPowerOffDialog.getWindow();
                window2.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);//if  not set then show failed
                window2.requestFeature(Window.FEATURE_NO_TITLE);
                WindowManager.LayoutParams winManager2 = window2.getAttributes();
                winManager2.y = 50;
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


	void sendBroadcast(){
		UpdateStatus.clearUpdateStatus();
		UpdateStatus.setError(true);
		UpdateStatus.sendBroadcastReceiver(this, UpdateStatus.UPDATE_MSG_CHANGE_ACTION);	
	}
	
	
	void showToastView( String msg){ 
		View view = LayoutInflater.from(this).inflate(R.layout.toast_info, null);
		TextView textView = (TextView)view.findViewById(R.id.toast_text);
		ImageView img = (ImageView)view.findViewById(R.id.toastImage);
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
	
			File file;
			FileOutputStream fileOutputStream = null;
			EnforceUpdateCheck check = new EnforceUpdateCheck(getBaseContext());
			String str = "";
			System.out.println("mDownloadThread+++");
			http_request_loop:
			while(mConnectStatus){
				try {
					Thread.sleep(1000);
					//String filename = mPath+mFileName;
					String filename = getFileName(mPath, mFileName); // Rony modify download use tmp 20120425
					file = new File(filename);
					FileUtils.setPermissions(filename, 0666, -1, -1);
					if (file.exists()) {
						FileInputStream fis = new FileInputStream(file);
						mCurSize = fis.available();
					} else {
						file.createNewFile();
						mCurSize = 0;
					}
					
					if((mCurSize / 1024) == mFileSize){ // Rony modify 20120425
						if(!check.checkMD5(filename,mMd5)){
							file.delete();
							continue http_request_loop; 
						}
//						mRefreshHandler.sendEmptyMessageDelayed(DOWNLOADING, 1000);
					}else if((mCurSize / 1024) > mFileSize){
						file.delete();
						continue http_request_loop; 
					}else{
						URL url = new URL(mUrl);
						HttpURLConnection httpConnection = (HttpURLConnection) url
								.openConnection();
						httpConnection.setRequestProperty("User-Agent", "Android");
						httpConnection.setConnectTimeout(5000);
						httpConnection.setReadTimeout(5000);
						String sProperty = "bytes=" + mCurSize + "-";
						httpConnection.setRequestProperty("RANGE", sProperty);
	
						InputStream is = httpConnection.getInputStream(); 
						//long length = httpConnection.getContentLength()+mCurSize;
						fileOutputStream = new FileOutputStream(file,true);
	                    byte[] buf = new byte[1024*8];
						if (is != null) {
												
							int ch = -1;
						  while (((ch = is.read(buf)) != -1) && mConnectStatus&&!mStop) {					
								fileOutputStream.write(buf, 0, ch);
								fileOutputStream.flush();
								fileOutputStream.getFD().sync();
								mCurSize += ch;
							}
						}
						fileOutputStream.flush();
						fileOutputStream.close();
					}
//					if(mConnectStatus == false){
//						break;
//					}
					if(mStop){
						break;
					}
					Log.d(TAG,"download firmware ok!");
//					String str = getString(R.string.update_download_ok);
//					check.sendBroadcast(mContext, EnforceUpdateCheck.MSG_DOWNLOAD, str, false);

					Log.d(TAG,"checking firmware!");
//					mConnectStatus =false;
//					str = getString(R.string.update_checking);
//					check.sendBroadcast(mContext, EnforceUpdateCheck.MSG_CHECK, str, false);
					if(!check.checkMD5(filename,mMd5)){
//						str = getString(R.string.update_check_fail);
//						check.sendBroadcast(mContext, EnforceUpdateCheck.MSG_CHECK, str, true);
						file.delete();
						break;
					}
					
					//file.renameTo(new File(mPath+"/update.zip"));	
					file.renameTo(new File(mPath+"/"+mFileName));// Rony modify download use tmp 20120425							
					//runRootCommand("busybox cp -rf "+filename+"  /mnt/sdcard/update.img");
					file.delete();
					Log.d(TAG,"check firmware ok!");
//					str = getString(R.string.update_check_ok);
//					check.sendBroadcast(mContext, EnforceUpdateCheck.MSG_CHECK, str, false);
					

					mRefreshHandler.removeMessages(SHOW_UPDATE_DIALOG);
					Message tDialogMsg = mRefreshHandler.obtainMessage(SHOW_UPDATE_DIALOG);
					tDialogMsg.obj = mPath+"/"+mFileName;
					mRefreshHandler.sendMessage(tDialogMsg);
					mStop = true;
//					Intent intent = new Intent(getBaseContext(),UpdateDialog.class); 
//					intent.putExtra("path", mPath);
//					intent.putExtra("name", mFileName); // Rony modify 20120425 
//					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//					startActivity(intent);
//					stopSelf();
					//getBaseContext().sendBroadcast(intent); 
					//mRefreshHandler.sendEmptyMessage(UpdateStatus);
				} catch (InterruptedException e) {
					
					//e.printStackTrace();
				} catch (FileNotFoundException e) {
					
					//e.printStackTrace();
				} catch (IOException e) {
					
					//e.printStackTrace();
					Log.e(TAG,"http request fail!");
					mConnectStatus = checkNetConnected();
//					if(!mConnectStatus){
//						UpdateStatus.clearUpdateStatus();
//						str =getString(R.string.connect_service_fail);
//						check.sendBroadcast(mContext, UpdateCheck.MSG_CHECK, str, false);
//					}
					continue http_request_loop; 
				}catch(Exception e){
					mConnectStatus = checkNetConnected();
//					if(!mConnectStatus){
//						UpdateStatus.clearUpdateStatus();
//						str =getString(R.string.connect_service_fail);
//						check.sendBroadcast(mContext, UpdateCheck.MSG_CHECK, str, false);
//					}
				}finally{
					mStop = true;
					if(null!=fileOutputStream){
						try{
							fileOutputStream.close();
						} catch (IOException e) {
							e.printStackTrace();
							Log.e(TAG,"close fileoutputstream fail!");
						}
						
					}
				}
			}
		}
		
	};
	
	int getProgress(long cur_size,long total_size){
		return (int) ((cur_size * 100) / total_size);
	}
	
	void sendUpgradeBroadcast(){
		Intent intent = new Intent(BROADCAST_UPGRADE_ACTION);
		mContext.sendBroadcast(intent);
		Log.e(TAG,"send action = "+BROADCAST_UPGRADE_ACTION);
	}
	
	private String getFileName(String path, String name){
		int end = name.lastIndexOf(".");
		return new String(path + "/" + name.substring(0, end) + ".tmp");
	}
}
