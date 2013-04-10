package com.lenovo.settings;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.lenovo.settings.EnforceUpdate.EnforceUpdateCheck;
import com.lenovo.settings.Object.UpdateData;
import com.lenovo.settings.Util.Recovery;
import com.lenovo.settings.update.UpdateCheck;
import com.lenovo.settings.update.UpdateDialog;
import com.lenovo.settings.update.UpdateService;
import com.lenovo.settings.update.UpdateStatus;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.AnimationDrawable;
import android.net.ethernet.EthernetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RecoverySystem;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class SystemUpdate extends Fragment{

	protected static final String TAG = "SystemUpdate";
	private static final int MSG_SHOW_LOCAL_UPDATE = 1000;
	private static final int MSG_SHOW_LOCAL_UPDATE_REBOOT = 1001;
	private static final int MSG_SHOW_POWER_OFF_DIALOG = 1002;
	private View mView;
	private Button mBtnUpdate,mBtnRestore;
	private TextView mTextTitle;
	private TextView mTextDownload;
	private TextView mTextCheck;
	private String mVersionStr;
	private TextView mUpdateSpn;
	private int mMessageStatus = -1;// 0:off 1:on
	private ArrayAdapter<CharSequence> adapter;
	private ListView mDropdownList;
//	private RelativeLayout mListLayout;
	private ArrayAdapter<CharSequence> mAdapter;
	protected boolean mEnableList = false;
	protected boolean mListEnd = false;
	private DropdownMenu mDropMenu;
	private String[] mUpdateArrays;
	private SharedPreferences mShareData;
	private int mDropMenuSize;
	private LayoutInflater mInflater;
	
	public static final String START_ALARM_INTENT = "com.joysee.action.START_ALARM";

	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
		    Log.d(TAG, " msg " + msg.toString());
			String path;
			switch(msg.what){
			case DropdownMenu.DROPDOWN_MENU_ITEM_CHICK:
				if(msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_CAN_EDIT){
					EditText edit = (EditText) msg.obj;
				}else if(msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_NOT_EDIT){
					TextView text = (TextView) msg.obj;
					int position = msg.arg2;
					if(text == mUpdateSpn){
						if(mUpdateMode != position){
							mUpdateMode = position;
							switch(mUpdateMode){
//							case UpdateStatus.UPDATE_MODE_AUTO:
//								// update by yuhongkun add alarm intent 20120807
//								clearUpdateMsg();
//								setUpdateModeAuto(UpdateStatus.UPDATE_MODE_AUTO);
//								setUpdateButtonEnable(false);
//								Intent alarm_intent = new Intent();
//								alarm_intent.setAction(START_ALARM_INTENT);
//								getActivity().sendBroadcast(alarm_intent);
//								break;
							case UpdateStatus.UPDATE_MODE_ONLINE:
								clearUpdateMsg();
								setUpdateModeAuto(UpdateStatus.UPDATE_MODE_ONLINE);
								setUpdateButtonEnable(true);
								//update by yuhongkun 20120807 after update manual mode stop updateservice 
//								if(isServiceRunning(UpdateService.UPDATE_SERVICE_NAME)){
//							    	Intent service_intent = new Intent(getActivity(),
//							    								UpdateService.class);
//							    	getActivity().stopService(service_intent);
//								}
								
//								if(isServiceRunning(UpdateService.UPDATE_SERVICE_NAME)){
//									//mUpdateCheck.stopUpdateService();
//									mUpdateCheck.stopUpdateService();
//									setUpdateButtonEnable(false);
//								}else{
//									setUpdateButtonEnable(true);
//									clearUpdateMsg();
//								}
								break;
							case UpdateStatus.UPDATE_MODE_LOCAL:
//								clearUpdateMsg();
                                if (isServiceRunning(UpdateService.UPDATE_SERVICE_NAME)) {
                                    Intent service_intent = new Intent(getActivity(),
                                            UpdateService.class);
                                    getActivity().stopService(service_intent);
                                }
							    clearUpdateMsg();
								setUpdateButtonEnable(true);
								setUpdateModeAuto(UpdateStatus.UPDATE_MODE_LOCAL);
								break;
							}
						}
					}else{
						break;
					}
				}
				break;
			case DropdownMenu.DROPDOWN_MENU_GET_ADAPTER:
				if(msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_CAN_EDIT){
					EditText edit = (EditText) msg.obj;
				}else if(msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_NOT_EDIT){
					TextView text = (TextView) msg.obj;
					if(text == mUpdateSpn){
						mDropMenu.setListViewPosition(0, mDropMenuSize);
						mDropMenu.setListViewAdapter(mUpdateArrays, mUpdateMode);
					}else{
						break;
					}
					mDropMenu.showDropdownListEnable(true);
				}
				break;
				case MSG_SHOW_LOCAL_UPDATE:
					path = (String) msg.obj;
					if(path == null){
						String str = getActivity().getString(R.string.not_new_version);
						mUpdateCheck.sendBroadcast(getActivity(),UpdateCheck.MSG_TITLE,str,true);
						break;
					}
					showLocalUpdateDialog(path);
				break;
				case MSG_SHOW_LOCAL_UPDATE_REBOOT:
                    path = (String) msg.obj;
                    Recovery.reboot(getActivity(), path);
                    Log.d(TAG, " ---------------------reboot ");
				break;
				case MSG_SHOW_POWER_OFF_DIALOG:
					// Rony add customer power off dialog for lenovo,
					//	framework must be close the dialog 20120405 
					//Intent intent = new Intent(getActivity(),PowerOffDialog.class); 
					//intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					//getActivity().startActivity(intent);
					PowerOffDialog.showPowerOffDialog(getActivity());
				break;
			}
		}
		
	};
	private IntentFilter mFilter;
	private BroadcastReceiver mReceiver;
	private IntentFilter mFilterMedia;
	private int mUpdateMode;
	protected UpdateCheck mUpdateCheck;
	protected EnforceUpdateCheck mEnforceUpdateCheck;

    private BroadcastReceiver mMediaStatusReceiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            
            String action = intent.getAction();
            Log.d(TAG, action);
            if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
                if (mUpdateMode == UpdateStatus.UPDATE_MODE_LOCAL) {
                    clearUpdateMsg();
                    setUpdateButtonEnable(false);
                    String str = getActivity().getString(R.string.update_no_sdcard);
                    mUpdateCheck.sendBroadcast(getActivity(),UpdateCheck.MSG_TITLE,str,true);
                }
            } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                if (mUpdateMode == UpdateStatus.UPDATE_MODE_LOCAL) {
                    clearUpdateMsg();
                    setUpdateButtonEnable(true);
                }
            }
        }
    };
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		mUpdateCheck = new UpdateCheck(getActivity());
		mEnforceUpdateCheck = new EnforceUpdateCheck(getActivity());
        mFilter = new IntentFilter();
        mFilter.addAction(UpdateStatus.UPDATE_MSG_CHANGE_ACTION);
        
        mFilterMedia = new IntentFilter();
		mFilterMedia.addAction(Intent.ACTION_MEDIA_MOUNTED);	
		mFilterMedia.addAction(Intent.ACTION_MEDIA_UNMOUNTED);	
		mFilterMedia.addAction(Intent.ACTION_MEDIA_EJECT);
		mFilterMedia.addAction(Intent.ACTION_MEDIA_REMOVED);
		mFilterMedia.addDataScheme("file");
        //mFilter.addAction(UpdateStatus.UPDATE_DONWLOAD_FINISHED_ACTION);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                Log.d(TAG, "action = " + action + " mUpdateMode = " + mUpdateMode
                        + " UpdateStatus.isError() = " + UpdateStatus.isError());
                if (action.equals(UpdateStatus.UPDATE_MSG_CHANGE_ACTION)) {
                    if (mUpdateMode == UpdateStatus.UPDATE_MODE_AUTO) {
                        setUpdateButtonEnable(false);
                    } else {
                        if (UpdateStatus.isError()) {
                            setUpdateButtonEnable(true);
                        } else {
                            setUpdateButtonEnable(false);
                        }
                    }
                    systemUpdateMsg();
                }
                
            }
        };
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) { 
		mInflater = inflater;
		mView = inflater.inflate(R.layout.system_update, container, false);
		mBtnUpdate = (Button) mView.findViewById(R.id.btn_update);
		mBtnRestore = (Button) mView.findViewById(R.id.btn_restore);
		mTextTitle = (TextView) mView.findViewById(R.id.text_find_update);
		mTextDownload = (TextView) mView.findViewById(R.id.text_updateing);
		mTextCheck = (TextView) mView.findViewById(R.id.text_update_status);
        final LinearLayout system_update_text_ll = (LinearLayout) mView
                .findViewById(R.id.system_update_text_ll);
        mUpdateSpn = (TextView) mView.findViewById(R.id.spn_update);
        mUpdateSpn.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View arg0, boolean arg1) {
                if (arg1) {
                    system_update_text_ll.setBackgroundResource(R.drawable.search_spiner_selected_larger);
                } else {
                    system_update_text_ll.setBackgroundResource(R.drawable.search_spiner_normal_larger);
                }
            }
        });
		mDropdownList = (ListView) mView.findViewById(R.id.dropdownlist);
//		mListLayout = (RelativeLayout) mView.findViewById(R.id.listLayout);
		
		mShareData = getActivity().getSharedPreferences("com.lenovo.settings", 0);
		mUpdateMode = mShareData.getInt("UpdateMode", UpdateStatus.UPDATE_MODE_ONLINE);
		mDropMenu = new DropdownMenu(getActivity(),mView,mHandler );
		mUpdateArrays = getResources().getStringArray(R.array.update_array);
		if(mUpdateArrays.length > 4){
			mDropMenuSize = 4;
		}else{
			mDropMenuSize = mUpdateArrays.length;
		}
		mDropMenu.setButtonListener(mUpdateSpn,mUpdateArrays[mUpdateMode]);
		mDropMenu.setListViewListener();		

        mBtnUpdate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                
                if (mUpdateMode == UpdateStatus.UPDATE_MODE_ONLINE) {
                    clearUpdateMsg();
                    if (isServiceRunning(UpdateService.UPDATE_SERVICE_NAME)) {
                        mUpdateCheck.stopUpdateService();
                    }
                    setUpdateButtonEnable(false);
//                    if (isServiceRunning(EnforceUpdateService.UPDATE_SERVICE_NAME)) {
//                        mEnforceUpdateCheck.stopUpdateService();
//                    }
                    update_thread();
                } else if (mUpdateMode == UpdateStatus.UPDATE_MODE_LOCAL) {
                    setUpdateButtonEnable(false);
                    clearUpdateMsg();
                    /*
                     * if(mUpdateCheck.checkLocalUpdate()){
                     * Message msg = new Message();
                     * msg.what = MSG_SHOW_LOCAL_UPDATE;
                     * msg.obj = UpdateData.Path;
                     * mHandler.sendMessage(msg);
                     * //showLocalUpdateDialog(UpdateData.Path);
                     * }
                     */
                    updateLocalThread();
                }
            }
            
        });
		
		mBtnRestore.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showRestoreDialog();
			}
		});
		
		if(mUpdateMode == UpdateStatus.UPDATE_MODE_AUTO){
			systemUpdateMsg();
			Log.d(TAG, " UPDATE_MODE_AUTO setUpdateButtonEnable(false) " + mUpdateMode);
			setUpdateButtonEnable(false);
		}else if((mUpdateMode == UpdateStatus.UPDATE_MODE_ONLINE) && 
				(isServiceRunning(UpdateService.UPDATE_SERVICE_NAME))){
			systemUpdateMsg();
	         Log.d(TAG, " UPDATE_MODE_ONLINE setUpdateButtonEnable(false) is updating----" + mUpdateMode);
			setUpdateButtonEnable(false);
		}else{
			clearUpdateMsg();
		}		
		LenovoSettingsActivity.setTitleFocus(false);
		mUpdateSpn.requestFocus();
		return mView;
	}
	
	@Override
    public void onStart() {
        super.onStart();
        getActivity().registerReceiver(mReceiver, mFilter);
        getActivity().registerReceiver(mMediaStatusReceiver, mFilterMedia);
        if (UpdateStatus.isError()) {
            setUpdateButtonEnable(true);
        } else {
            setUpdateButtonEnable(false);
        }
        systemUpdateMsg();
    }

	@Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mReceiver);
        getActivity().unregisterReceiver(mMediaStatusReceiver);
        if (mUpdateDialog != null && mUpdateDialog.isShowing()) {
            mUpdateDialog.dismiss();
        }
        if (mRestoreDialog != null && mRestoreDialog.isShowing()) {
            mRestoreDialog.dismiss();
        }
    }
	
	void setUpdateButtonEnable(boolean enable){
		mBtnUpdate.setEnabled(enable);
		mBtnUpdate.setFocusable(enable);
		mBtnUpdate.setAlpha(enable ? 1f : 0.7f);
	}

	private void setUpdateModeAuto(int mode){
		Editor editor = mShareData.edit();
		editor.putInt("UpdateMode", mode);
		editor.commit();
	}
	
	void clearUpdateMsg(){
		Log.d(TAG,"clearUpdateMsg");
		UpdateStatus.clearUpdateStatus();
		systemUpdateMsg();
	}

	protected void systemUpdateMsg() {

		String titleMsg = UpdateStatus.getTitleMsg();
		String donwloadMsg = UpdateStatus.getDownloadMsg();
		String checkMsg = UpdateStatus.getCheckMsg();
		Log.d(TAG, " titleMsg = " + titleMsg + " donwloadMsg = " + donwloadMsg + " checkMsg = " + checkMsg);
		mTextTitle.setText(titleMsg);
		mTextDownload.setText(donwloadMsg);
		mTextCheck.setText(checkMsg);
	}
	
	public boolean isServiceRunning(String serviceName) { 

        ActivityManager manager = (ActivityManager) getActivity()
        								.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningServiceInfo> infos = manager.getRunningServices(30); 
        for (RunningServiceInfo info : infos) { 
        	//Log.d(TAG,"Running service is "+info.service.getClassName());
            if (info.service.getClassName().equals(serviceName)) { 
                return true; 
            } 
        }
        return false; 
    } 
	
    public void update_thread() {
        new Thread() {
            @Override
            public void run() {
                if (mUpdateCheck.checkUpdate()) {
                    mUpdateCheck.startUpdateService();
                }
            }
        }.start();
    }
	
    public void updateLocalThread() {
        new Thread() {
            @Override
            public void run() {
                if (mUpdateCheck.checkLocalUpdate_xml(0,UpdateCheck.XML_SDCARD)) {
                    Message msg = new Message();
                    msg.what = MSG_SHOW_LOCAL_UPDATE;
                    msg.obj = UpdateData.Path;
                    mHandler.sendMessage(msg);
                }
            }
        }.start();
    }
	private Dialog mUpdateDialog;
	private void showLocalUpdateDialog(final String path){
	    Log.d(TAG, " showLocalUpdateDialog path = " + path);
		Button btnConfirm,btnCancel;
		TextView msg;
		mUpdateDialog = new Dialog(mView.getContext(),R.style.DialogStyle);
		int width = (int) getResources().getDimension(R.dimen.ca_worktime_pwd_dialog_width);
        int height = (int) getResources().getDimension(R.dimen.ca_worktime_pwd_dialog_height);
        View view = mInflater.inflate(R.layout.resolution_dialog, null);
        mUpdateDialog.setContentView(view,new LinearLayout.LayoutParams(width, height));
        //dialog.setCancelable(true);
        msg = (TextView) mUpdateDialog.findViewById(R.id.textview_dialog);
        msg.setText(R.string.dlg_update_msg);
        btnConfirm = (Button) mUpdateDialog.findViewById(R.id.btn_dlg_confirm);
        btnConfirm.setText(R.string.dlg_confirm);
        btnConfirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mUpdateDialog.dismiss();
				/*try {
					File file = new File(path);
					RecoverySystem.installPackage(getActivity(), file);
				} catch (IOException e) {
						
				}*/
				mHandler.sendEmptyMessage(MSG_SHOW_POWER_OFF_DIALOG);
				Message msg = new Message();
				msg.what = MSG_SHOW_LOCAL_UPDATE_REBOOT;
				msg.obj = path;
				mHandler.sendMessage(msg);
				PowerOffDialog.showPowerOffDialog(getActivity());
			}
        	
        });
        btnConfirm.setFocusable(true);
        btnConfirm.setFocusableInTouchMode(true);
        btnConfirm.requestFocus();
        btnCancel = (Button) mUpdateDialog.findViewById(R.id.btn_dlg_cancel);
        btnCancel.setText(R.string.dlg_cancel);
        btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setUpdateButtonEnable(true);
				clearUpdateMsg();
				//Recovery.bootRecovery(getActivity(), path);
				mUpdateDialog.dismiss();
			}
        	
        });
        mUpdateDialog.show();
	}
	private Dialog mRestoreDialog;
	private void showRestoreDialog(){
		Button btnConfirm,btnCancel;
		TextView msg;
		mRestoreDialog = new Dialog(mView.getContext(),R.style.DialogStyle);
        mRestoreDialog.setContentView(R.layout.resolution_dialog);
        //dialog.setCancelable(true);
        msg = (TextView) mRestoreDialog.findViewById(R.id.textview_dialog);
        msg.setText(R.string.dlg_restore_msg);
        btnConfirm = (Button) mRestoreDialog.findViewById(R.id.btn_dlg_confirm);
        btnConfirm.setText(R.string.dlg_confirm);
        btnConfirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
                mRestoreDialog.dismiss();
                // Rony add customer power off dialog for lenovo,
                // framework must be close the dialog 20120405
                // Intent intent = new
                // Intent(getActivity(),PowerOffDialog.class);
                // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // startActivity(intent);
                PowerOffDialog.showPowerOffDialog(getActivity());
            }

        });
        
        btnCancel = (Button) mRestoreDialog.findViewById(R.id.btn_dlg_cancel);
        btnCancel.setText(R.string.dlg_cancel);
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mRestoreDialog.dismiss();
            }
        });
        mRestoreDialog.show();
	}

}
