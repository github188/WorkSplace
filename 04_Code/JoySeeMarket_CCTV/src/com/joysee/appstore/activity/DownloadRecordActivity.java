package com.joysee.appstore.activity;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import com.joysee.appstore.R;
import com.joysee.appstore.common.ApplicationBean;
import com.joysee.appstore.common.AppsBean;
import com.joysee.appstore.common.Constants;
import com.joysee.appstore.common.CustomUninstallDialog;
import com.joysee.appstore.common.TaskBean;
import com.joysee.appstore.common.TaskDownSpeed;
import com.joysee.appstore.db.DBUtils;
import com.joysee.appstore.db.DatabaseHelper.ApplicationColumn;
import com.joysee.appstore.db.DatabaseHelper.ApplicationColumnIndex;
import com.joysee.appstore.db.DatabaseHelper.DownloadTaskColumn;
import com.joysee.appstore.db.DatabaseHelper.DownloadTaskColumnIndex;
import com.joysee.appstore.service.DownloadService;
import com.joysee.appstore.service.DownloadService.ServiceBinder;
import com.joysee.appstore.utils.AppLog;
import com.joysee.appstore.utils.CaCheManager;
import com.joysee.appstore.utils.Utils;

public class DownloadRecordActivity extends Activity {

	private final static String TAG = "com.joysee.appstore.DownloadRecordActivity";
	
	public static final int FLAG_DOWNLOADED = 1;

	public static final int FLAG_DOWNLOADING = 2;

	public static final int FLAG_INSTALLED = 3;
	
	public static final int REFRESH_APPLIST = 4;
	
	//重置焦点位置
	public static final int RESET_FOCUS_INSTALLED = 5;
	public static final int RESET_FOCUS_DOWNLOADING = 6;

	private ImageButton mCloseButton;
	private TextView mTab_downloading;
	private TextView mTab_installed;
	private LinearLayout mListView;
	private Cursor mCursor = null;
	private ContentObserver mDownloadingObserver = null;
	private ContentObserver mApplicationObserver = null;
	private ServiceBinder downloadService;
	public static HashMap<String,Bitmap> bitMap=new HashMap<String,Bitmap>(); 
	private int mCurrentType;//当前类型FLAG_DOWNLOADING，FLAG_INSTALLED
	 
	 /** BACKGROUD work thread */
	 private HandlerThread workThread = new HandlerThread("downloadmgr work thread");
	 private Handler workHandler;
	 private final int DelayTime = 100;
	 private final static int CONTINUE_PAUSE_DELAY = 300;
	 private final static int MSG_PAUSE_CONTINUE=10;
	 
	 public boolean refeshFinish=true;//控制一段时间来进行刷新
	 private int selectType=0;//被选择，用来刷新数据后重新定位的，1表示应用图标，2表示删除按钮
	 private int selectIndex=-1;//被选择的行数
	 private DBUtils mDBUtils;
	 public boolean isActivie=true;
	 
	 private LayoutInflater mLayoutInflater;
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what){
			case DownloadingObserver.MSG_REFRESH_DOWNLOADING_LISTVIEW:
			case ApplicationObserver.MSG_REFRESH_APP_LISTVIEW:
				mHandler.sendEmptyMessageDelayed(REFRESH_APPLIST, Constants.Delayed.TIME20);
				break;
			case MSG_PAUSE_CONTINUE:
				TaskBean pauseTask=(TaskBean)msg.obj;
				AppLog.d(TAG, "-----------mHandler-------MSG_PAUSE_CONTINUE-------status="+pauseTask.getStatus()+";name="+pauseTask.getAppName());
				if(pauseTask.getStatus()==Constants.DOWNLOAD_STATUS_EXECUTE){
					downloadService.pauseTask(pauseTask);
				}else if(pauseTask.getStatus()==Constants.DOWNLOAD_STATUS_PAUSE){
					if(null!=pauseTask.getDownloadDir() && null!=pauseTask.getAppName()){
						if(!pauseTask.getDownloadDir().equals(Constants.APKROOT)){
							if(!CaCheManager.checkSDcard()){
								Utils.showTipToast(Gravity.BOTTOM, DownloadRecordActivity.this, DownloadRecordActivity.this.getString(R.string.data_missing));
								return;
							}
						}
						downloadService.continueTask(pauseTask);
					}
				}
				break;
			case REFRESH_APPLIST:
				mHandler.removeMessages(REFRESH_APPLIST);
				refreshUI();
				break;
			case RESET_FOCUS_INSTALLED:
				AppsBean ab = (AppsBean) msg.obj;
				int count = mListView.getChildCount();
				for(int i=0;i<count;i++){
					View view = mListView.getChildAt(i).findViewById(R.id.delete);
					if(view.getTag() == ab){
						view.setBackgroundResource(R.drawable.public_cannot_click);
						//让删除按钮不可点
						view.setFocusable(false);
						//让删除的应用图标不可点
						mListView.getChildAt(i).findViewById(R.id.app_icon).setFocusable(false);
						
						if(i==0){
							mTab_installed.requestFocus();
						}else{
							View other = mListView.getChildAt(i-1).findViewById(R.id.delete);
							other.requestFocus();
						}
					}
				}
				break;
			case RESET_FOCUS_DOWNLOADING:
				TaskBean task = (TaskBean) msg.obj;
				int downingCount = mListView.getChildCount();
				for(int i=0;i<downingCount;i++){
					View view = mListView.getChildAt(i).findViewById(R.id.delete);
					if(view.getTag() == task){
						View pause_start = mListView.getChildAt(i).findViewById(R.id.pause_start);
						View icon = mListView.getChildAt(i).findViewById(R.id.app_icon);
						
						if(view.hasFocus() || pause_start.hasFocus() || icon.hasFocus()){
							if(i==0){
								mTab_downloading.requestFocus();
							}else{
								View other = mListView.getChildAt(i-1).findViewById(R.id.delete);
								other.requestFocus();
							}
						}
						view.setFocusable(false);
						icon.setFocusable(false);
						pause_start.setFocusable(false);
						view.setBackgroundResource(R.drawable.public_cannot_click);
						pause_start.setBackgroundResource(R.drawable.public_cannot_click);
					}
				}
				break;
			}
		}
	};
	
	private void refreshUI(){
		boolean closeFocus=mCloseButton.hasFocus();
		mCursor.deactivate();
		mCursor.requery();
		AppLog.d(TAG, "-------IconFocusListener----selectIndex="+selectIndex+";selectType="+selectType+";mListView.getChildCount()="+mListView.getChildCount());
		setRequest(false);
		resreshView();
		refeshFinish=true;
		if(closeFocus){
			setRequest(true);
			mCloseButton.requestFocus();
			return;
		}
		if(selectType>0&&selectIndex>-1&&mListView!=null&&mListView.getChildCount()>0){
			int flag=0;
			for(int i=0;i<mListView.getChildCount();i++){
				View view=mListView.getChildAt(i);
				int viewTag=(Integer)view.getTag();
				flag++;
				if(viewTag==selectIndex){
					flag--;
					if(selectType==1){
						view.findViewById(R.id.app_icon).requestFocus();
					}else if(selectType==2){
						view.findViewById(R.id.delete).requestFocus();
					}else if(selectType==3){
						view.findViewById(R.id.pause_start).requestFocus();
					}
				}
			}
			AppLog.d(TAG, "-----------flag="+flag+";mListView.getChildCount()="+mListView.getChildCount());
			if(flag==mListView.getChildCount()){
				View view2=mListView.getChildAt(0);
				if(selectType==1){
					view2.findViewById(R.id.app_icon).requestFocus();
				}else if(selectType==2){
					view2.findViewById(R.id.delete).requestFocus();
				}else if(selectType==3){
					view2.findViewById(R.id.pause_start).requestFocus();
				}
			}
		}
		setRequest(true);
	}

	
	private void startWorkThread(){
		workThread.start();
		workHandler = new Handler(workThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what){
    			case DownloadingObserver.MSG_REFRESH_DOWNLOADING_LISTVIEW:
    			case ApplicationObserver.MSG_REFRESH_APP_LISTVIEW:
    				mHandler.sendMessageDelayed(mHandler.obtainMessage(msg.what),DelayTime);
    				break;
            }
		}
		
	};
	}
	
	private ServiceConnection mServiceConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
        	downloadService = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
        	downloadService = ((DownloadService.ServiceBinder)service).getService();
        }
    };
    
    protected void onServiceDied() {
		downloadService = null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.downloadmgr);
		mLayoutInflater = this.getLayoutInflater();
		mDBUtils=new DBUtils(DownloadRecordActivity.this);
		Intent intent = new Intent("com.joysee.appstore.service.DownloadService");
		try{
			bindService(intent,mServiceConnection, Context.BIND_AUTO_CREATE);
		}catch(Exception e){
			Log.d(TAG, "----------bindService-------");
			bindService(intent,mServiceConnection, Context.BIND_AUTO_CREATE);
		}
		setupViews();
		startWorkThread();
	}

	
	private void initListView(final int flag){
		switch(flag){
		case FLAG_DOWNLOADING: 
			AppLog.d(TAG, "===============enter initListView========flag="+flag);
			
			mCurrentType=FLAG_DOWNLOADING;
			mCursor = getContentResolver().query(DownloadTaskColumn.CONTENT_URI, null, DownloadTaskColumn.PKGNAME+"!=? ", new String[]{DownloadRecordActivity.this.getPackageName()}, null);
			AppLog.d(TAG, "--------------initview-FLAG_DOWNLOADING---mCursor.getCount()="+mCursor.getCount());
			resreshView();
	        registerDownloadObserver(FLAG_DOWNLOADING);
			break;
			
		case FLAG_INSTALLED:
			
			AppLog.d(TAG, "===============enter initListView========flag="+flag);
			
			mCurrentType=FLAG_INSTALLED;
			mCursor = getContentResolver().query(ApplicationColumn.CONTENT_URI, null, ApplicationColumn.STATUS+" = ? and "+ApplicationColumn.APPSOURCE+"!=? and "+ApplicationColumn.PKGNAME+"!=? ",
					new String[]{""+Constants.APP_STATUS_INSTALLED,""+Constants.APP_SOURCE_INNER,DownloadRecordActivity.this.getPackageName()}, ApplicationColumn.ID+" desc");
			AppLog.d(TAG, "--------------initview-FLAG_INSTALLED---mCursor.getCount()="+mCursor.getCount());
			resreshView();
	        registerDownloadObserver(FLAG_INSTALLED);
			break;
			
		}
		
	}
	
	public void resreshView(){
		if(!isActivie)
			return;
		AppLog.d(TAG, ">>>>> resreshView <<<<<<");
		mListView.removeAllViewsInLayout();
		mListView.removeAllViews();
		int index=0;
		if(mCurrentType==FLAG_DOWNLOADING){ 
				AppLog.d(TAG, "--------------resreshView-FLAG_DOWNLOADING---"+mCursor.getCount());
	        	while(mCursor.moveToNext()){
	        		AppLog.d(TAG, "-------FLAG_DOWNLOADING----"+mCursor.getString(DownloadTaskColumnIndex.APPNAME));
	        		View view=mLayoutInflater.inflate(R.layout.listview_downloading_item, null);
	        		view.setTag(mCursor.getInt(DownloadTaskColumnIndex.ID));
	        		byte b[]=mCursor.getBlob(DownloadTaskColumnIndex.ICON);
	        		final TaskBean task = Utils.cursorToTaskBean(mCursor);
	            	if(null!=b){
	            		ImageButton imageButton=(ImageButton)view.findViewById(R.id.app_icon);
	            		if(bitMap.containsKey(task.getPkgName())){
	            			imageButton.setImageBitmap(bitMap.get(task.getPkgName()));
	            		}else{
	            			BitmapFactory.Options option = new BitmapFactory.Options();
		            		option.inSampleSize = 4; //将图片设为原来宽高的1/2，防止内存溢出
		            		Bitmap bm = Utils.getRoundedCornerBitmap(BitmapFactory.decodeByteArray(b, 0, b.length, option), 10.0f);
		            		imageButton.setImageBitmap(bm);
		            		bitMap.put(task.getPkgName(), bm);
	            		}
	            		imageButton.setTag(task.getSerAppID());
	            		imageButton.setOnClickListener(new OnClickListener(){
							public void onClick(View v) {
								Intent intent=new Intent();
								intent.setClass(DownloadRecordActivity.this, DetailedActivity.class);
								intent.putExtra("app_id",Integer.parseInt((String)v.getTag()));
								startActivity(intent);
							}
	            		});
	            		imageButton.setOnFocusChangeListener(new IconFocusListener(mCursor.getInt(DownloadTaskColumnIndex.ID)));
	            	}
	            	((TextView)view.findViewById(R.id.downmgr_item_name)).setText(""+mCursor.getString(DownloadTaskColumnIndex.APPNAME));
	            	long down=mCursor.getInt( DownloadTaskColumnIndex.DOWNLOADSIZE);
	            	long sum=mCursor.getInt( DownloadTaskColumnIndex.SUMSIZE);
	            	if(sum==0){
	            		((ProgressBar)view.findViewById(R.id.downmgr_item_progress)).setProgress(0);
	            	}else{
	            		int res=(int)(down*100/sum);
	            		((ProgressBar)view.findViewById(R.id.downmgr_item_progress)).setProgress(res);
	            	}
	            	Button deleteBut=(Button)view.findViewById(R.id.delete);
	            	deleteBut.setTag(task);
	            	deleteBut.setOnClickListener(new OnClickListener(){
						public void onClick(View v) {
							/** 删除中，让删除按钮不可点击 */
							Message msg = mHandler.obtainMessage(RESET_FOCUS_DOWNLOADING);
							msg.obj = task;
							msg.what = RESET_FOCUS_DOWNLOADING;
							mHandler.sendMessage(msg);
							/*------------------------------*/
							downloadService.delTask((TaskBean)v.getTag());
						}
	            	});
	            	deleteBut.setOnKeyListener(new OnKeyListener() {
						public boolean onKey(View v, int keyCode, KeyEvent event) {
							if(keyCode == event.KEYCODE_DPAD_RIGHT){
								return true;
							}
							return false;
						}
					});
	            	deleteBut.setOnFocusChangeListener(new DeleteFocusListener(mCursor.getInt(DownloadTaskColumnIndex.ID)));
	            	final Button pro_layout=(Button)view.findViewById(R.id.pause_start);
	            	pro_layout.setFocusable(true);
	            	pro_layout.setTag(task.getId());
	            	
	            	//按钮状态
	            	int buttonStatus = mCursor.getInt(DownloadTaskColumnIndex.STATUS);
	            	if(buttonStatus==Constants.DOWNLOAD_STATUS_EXECUTE){
						pro_layout.setText(getResources().getText(R.string.down_pause));
					}else if(buttonStatus==Constants.DOWNLOAD_STATUS_PAUSE){
						pro_layout.setText(getResources().getText(R.string.down_continue));
					}
	            	
	            	pro_layout.setOnClickListener(new OnClickListener(){
						public void onClick(View v) {
							mHandler.removeMessages(MSG_PAUSE_CONTINUE);
							Message mes=mHandler.obtainMessage(MSG_PAUSE_CONTINUE);
							TaskBean clickTask=mDBUtils.queryTaskById((Integer)v.getTag());
							if(clickTask==null){
								return;
							}
							mes.obj=clickTask;
							mHandler.sendMessageDelayed(mes, CONTINUE_PAUSE_DELAY);
						}
	            	});
	            	pro_layout.setOnFocusChangeListener(new ProgressFocusListener(mCursor.getInt(DownloadTaskColumnIndex.ID)));
	            	
	            	TextView pro_tex=(TextView)view.findViewById(R.id.down_progress_text);
	            	int status=mCursor.getInt(DownloadTaskColumnIndex.STATUS);
	            	if(status==Constants.DOWNLOAD_STATUS_ERROR){
	            		pro_tex.setText(DownloadRecordActivity.this.getString(R.string.download_error));
	            	}else if(status==Constants.DOWNLOAD_STATUS_EXECUTE){
	            		TaskDownSpeed tds=DownloadService.speedMap.get(task.getId());
	            		if(tds!=null){
	            			pro_tex.setText(""+tds.getSpeed()+"  "+DownloadRecordActivity.this.getString(R.string.down_speed));
	            			//TODO 下载完成后，把按钮设为不可点
	            			if(tds.getSumSize() == tds.getDownSize()){
	            				Message msg = mHandler.obtainMessage(RESET_FOCUS_DOWNLOADING);
								msg.obj = task;
								msg.what = RESET_FOCUS_DOWNLOADING;
								mHandler.sendMessage(msg);
	            			}
	            		}else{
	            			pro_tex.setText("0  "+DownloadRecordActivity.this.getString(R.string.down_speed));
	            		}
	            	}else if(status==Constants.DOWNLOAD_STATUS_PAUSE){
	            		pro_tex.setText(DownloadRecordActivity.this.getString(R.string.pause_download));
	            	}
	            	index++;
	            	mListView.addView(view);
	        	}
	        	
	        	/** 列表为空，把焦点设到Tab上 */
	        	if(mListView.getChildCount()==0){
	        		mTab_downloading.requestFocus();
	        	}
		}else if(mCurrentType==FLAG_INSTALLED){
				AppLog.d(TAG, "--------------resreshView-FLAG_INSTALLED---"+mCursor.getCount());
	        	while(mCursor.moveToNext()){
	        		final View view=mLayoutInflater.inflate(R.layout.listview_installed_item, null);
	        		view.setTag(mCursor.getInt(ApplicationColumnIndex.ID));
	        		byte b[]=mCursor.getBlob(ApplicationColumnIndex.ICON);
	        		String pakName=mCursor.getString(ApplicationColumnIndex.PKGNAME);
	            	if(b!=null){
	            		ImageButton imageButton=(ImageButton)view.findViewById(R.id.app_icon);
	            		if(bitMap.containsKey(pakName)){
	            			imageButton.setImageBitmap(bitMap.get(pakName));
	            		}else{
	            			BitmapFactory.Options option = new BitmapFactory.Options();
		            		option.inSampleSize = 4; //将图片设为原来宽高的1/2，防止内存溢出
		            		Bitmap bm = Utils.getRoundedCornerBitmap(BitmapFactory.decodeByteArray(b, 0, b.length, option), 10.0f);
		            		imageButton.setImageBitmap(bm);
		            		bitMap.put(pakName, bm);
	            		}
	            		 imageButton.setTag(mCursor.getString(ApplicationColumnIndex.PKGNAME));
	            		 imageButton.setOnClickListener(new OnClickListener(){
							public void onClick(View v) {
								Intent intent = new Intent();
			    				intent = getPackageManager().getLaunchIntentForPackage((String)v.getTag());
			    				if(null==intent){
			    					Toast toastSD=Toast.makeText(DownloadRecordActivity.this,DownloadRecordActivity.this.getString(R.string.no_app), Toast.LENGTH_LONG);
			    		            toastSD.setGravity(Gravity.CENTER, 0,0);
			    		            toastSD.show();
			    				}else{
			    					startActivity(intent);
			    				}
							}
	            		 });
	            		 imageButton.setOnFocusChangeListener(new IconFocusListener(mCursor.getInt(ApplicationColumnIndex.ID)));
	            	}
	            	((TextView)view.findViewById(R.id.application_name)).setText(""+mCursor.getString(ApplicationColumnIndex.APPNAME));
	            	final Button deleteBut=(Button)view.findViewById(R.id.delete);
	            	
	            	String pkgName = mCursor.getString(ApplicationColumnIndex.PKGNAME);
	            	String appName = mCursor.getString(ApplicationColumnIndex.APPNAME);
	            	AppsBean appsBean = new AppsBean();
	            	appsBean.setPkgName(pkgName);
	            	appsBean.setAppName(appName);
	            	deleteBut.setTag(appsBean);
	            	deleteBut.setOnClickListener(new OnClickListener(){
						public void onClick(View v) {
							deleteApp((AppsBean)v.getTag());
						}
	            	});
	            	deleteBut.setOnKeyListener(new OnKeyListener() {
						public boolean onKey(View v, int keyCode, KeyEvent event) {
							if(keyCode == event.KEYCODE_DPAD_RIGHT){
								return true;
							}
							return false;
						}
					});
	            	deleteBut.setOnFocusChangeListener(new DeleteFocusListener(mCursor.getInt(ApplicationColumnIndex.ID)));
	            	index++;
	            	mListView.addView(view);
	        	}
	        	
	        	/** 列表为空，把焦点设到Tab上 */
	        	if(mListView.getChildCount()==0){
	        		mTab_installed.requestFocus();
	        	}
		}
	}
	
	
	public void deleteApp(AppsBean appsBean){
		if(appsBean==null){
			return;
		}
		ApplicationBean appl=mDBUtils.queryApplicationByPkgName(appsBean.getPkgName());
		if(appl==null){
			return;
		}
		if(appl.getStatus()==Constants.APP_STATUS_UNINSTALLING){
			return;
		}
		showUnstallDialog(appsBean);
	}
	
	
	/**卸载弹框  */
	public void showUnstallDialog(final AppsBean appsBean){
		final String pkgName = appsBean.getPkgName();
		CustomUninstallDialog.Builder dialog = new CustomUninstallDialog.Builder(DownloadRecordActivity.this);
		dialog.setTitle(appsBean.getAppName());
		dialog.setPositiveButton(R.string.Uninstall, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				/** 删除中，让删除行所有按钮不可点击 */
				Message msg = mHandler.obtainMessage(RESET_FOCUS_INSTALLED);
				msg.obj = appsBean;
				msg.what = RESET_FOCUS_INSTALLED;
				mHandler.sendMessage(msg);
				/*---------------------------------------------*/
				downloadService.deletePackage(pkgName);
				bitMap.remove(pkgName);
				dialog.dismiss();
			}
		});
		
		dialog.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		
		CustomUninstallDialog alertDialog = dialog.create();
		Window window = alertDialog.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();  
		lp.alpha = 0.9f;
		lp.width = 1280;
		lp.height = 300;
		window.setAttributes(lp);
		dialog.create().show();
	}
	
	
	class IconFocusListener implements OnFocusChangeListener{
		int mIndex;
		public IconFocusListener(int index){
			mIndex=index;
		}
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if(hasFocus){
				selectType=1;
				selectIndex=mIndex;
			}else{
				selectType=0;
				selectIndex=-1;
			}
		}
	}
	
	
	class ProgressFocusListener implements OnFocusChangeListener{
		int mIndex;
		public ProgressFocusListener(int index){
			mIndex=index;
		}
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if(hasFocus){
				selectType=3;
				selectIndex=mIndex;
			}else{
				selectType=0;
				selectIndex=-1;
			}
		}
	}
	
	
	class DeleteFocusListener implements OnFocusChangeListener{
		int mIndex;
		public DeleteFocusListener(int index){
			mIndex=index;
		}
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if(hasFocus){
				selectType=2;
				selectIndex=mIndex;
			}else{
				selectType=0;
				selectIndex=-1;
			}
		}
	}
	
	
	private void setupViews() {
		
		/**
		 * mTab_downloading、mTab_installed的背景选择器有两种色，一种是黄色，一种是透明色。
		 * 当他们点击后，背景色替换为一张灰色  
		 * */
		mCloseButton = (ImageButton) findViewById(R.id.close);
		mTab_downloading = (TextView) findViewById(R.id.tab_2);
		mTab_installed = (TextView) findViewById(R.id.tab_3);
		mListView = (LinearLayout) findViewById(R.id.appslist);
		mCloseButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mCloseButton.setBackgroundResource(R.drawable.close_click);
				mCloseButton.postDelayed(new Runnable() {
					public void run() {
						mCloseButton.setBackgroundResource(R.drawable.close_focus);
					}
				}, 250);
				mCloseButton.postDelayed(new Runnable() {
					public void run() {
						finish();
					}
				}, 650);
			}	
		});
		
		mTab_downloading.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mTab_downloading.setBackgroundColor(getResources().getColor(R.color.down_load_tab_click));
				mTab_downloading.postDelayed(new Runnable() {
					public void run() {
						updateTabSelect(FLAG_DOWNLOADING);
					}
				}, 200);
				selectType=0;
				selectIndex=-1;
				initListView(FLAG_DOWNLOADING);
				mCurrentType=FLAG_DOWNLOADING;
			}
		});
		mTab_installed.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mTab_installed.setBackgroundColor(getResources().getColor(R.color.down_load_tab_click));
				mTab_installed.postDelayed(new Runnable() {
					public void run() {
						updateTabSelect(FLAG_INSTALLED);
					}
				}, 200);
				selectType=0;
				selectIndex=-1;
				initListView(FLAG_INSTALLED);
				mCurrentType=FLAG_INSTALLED;
				
			}
		});
		mTab_downloading.requestFocus();
		initFirstIn();
	}
	
	/** 第一次默认显示[下载中] */
	private void initFirstIn(){
		selectType=0;
		selectIndex=-1;
		initListView(FLAG_DOWNLOADING);
		mCurrentType=FLAG_DOWNLOADING;
		updateTabSelect(FLAG_DOWNLOADING);
	}
	
	/** 更新TAG选中页背景色 */
	public void updateTabSelect(int tag){
		if(tag == FLAG_DOWNLOADING){
			mTab_downloading.setBackgroundResource(R.drawable.downrecord_tab_click);
			mTab_installed.setBackgroundResource(R.drawable.downrecord_tab_selected);
		}else if(tag == FLAG_INSTALLED){
			mTab_installed.setBackgroundResource(R.drawable.downrecord_tab_click);
			mTab_downloading.setBackgroundResource(R.drawable.downrecord_tab_selected);
		}
	}
	
	//TODO 防止焦点错乱
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if(event.getAction()==KeyEvent.ACTION_DOWN){
			if(event.getKeyCode()==KeyEvent.KEYCODE_DPAD_DOWN){
				AppLog.d(TAG, "view count = "+mListView.getChildCount());
				if(mTab_installed.hasFocus() || mTab_downloading.hasFocus() || mCloseButton.hasFocus()){
					if(mListView.getChildCount()<=0){
						return true;
					}
				}
			}else if(event.getKeyCode()==KeyEvent.KEYCODE_DPAD_LEFT){
				if(mTab_downloading.hasFocus()){
					return true;
				}else if(mCloseButton.hasFocus()){
					mTab_downloading.setFocusable(false);
					mTab_installed.requestFocus();
					return true;
				}else if(mTab_installed.hasFocus()){
					mTab_downloading.setFocusable(true);
					mTab_downloading.requestFocus();
					return true;
				}
			}else if(event.getKeyCode()==KeyEvent.KEYCODE_DPAD_UP){
				if(mTab_installed.hasFocus() || mTab_downloading.hasFocus()){
					return true;
				}else if(mListView.getChildCount()>0 && mListView.getChildAt(0)!=null){
					View view = mListView.getChildAt(0);
					if(mCurrentType == FLAG_DOWNLOADING){
						if(view.findViewById(R.id.app_icon).hasFocus() || view.findViewById(R.id.pause_start).hasFocus() || //
								view.findViewById(R.id.delete).hasFocus()){
							
							mTab_downloading.requestFocus();
							return true;
						}
					}else if(mCurrentType == FLAG_INSTALLED){
						if(view.findViewById(R.id.app_icon).hasFocus() || view.findViewById(R.id.delete).hasFocus()){
							mTab_installed.requestFocus();
							return true;
						}
					}
				}
			}
		}
		return super.dispatchKeyEvent(event);
	}
	
	
	public void setRequest(boolean flag){
//		mTab_downloading.setFocusable(flag);
//		mTab_installed.setFocusable(flag);
//		mCloseButton.setFocusable(flag);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter();
	    filter.addAction(Constants.INTENT_DOWNLOAD_STARTED);
	    filter.addAction(Constants.INTENT_DOWNLOAD_PAUSE);
	    filter.addAction(Constants.INTENT_INSTALL_COMPLETED);
	    isActivie=true;
	    Constants.activitys++;
	    Constants.update=false;
	}
	
	public void sendUpdate(){
        new Handler().postDelayed(runnable, 3000);
   }
       
       Runnable runnable = new Runnable(){
           @Override
           public void run() {
               Log.d(TAG, "-----runnable------activitys.size="+Constants.activitys);
               if(Constants.activitys==0){
                   Constants.update=true;
                   DBUtils dbu=new DBUtils(DownloadRecordActivity.this);
                   TaskBean updateBean=dbu.queryTaskByPkgName(DownloadRecordActivity.this.getPackageName());
                   if(updateBean!=null&&updateBean.getDownloadSize()>=updateBean.getSumSize()){
                       AppLog.d(TAG, "------updateBean pagk="+updateBean.getPkgName());
                       if(dbu.needStartService("com.joysee.appstore.service.DownloadService")){
                           Intent intent = new Intent();
                           intent.setAction(Constants.INTENT_DOWNLOAD_COMPLETED);
                           intent.putExtra("task",updateBean);
                           DownloadRecordActivity.this.sendBroadcast(intent);
                       }else{
                           AppLog.d(TAG, "-------INTENT_ACTION_RESTART_SERVICES---------");
                           Intent intentSer = new Intent(DownloadRecordActivity.this,DownloadService.class);
                           DownloadRecordActivity.this.startService(intentSer);
                           Intent intent = new Intent();
                           intent.setAction(Constants.INTENT_DOWNLOAD_COMPLETED);
                           intent.putExtra("task",updateBean);
                           DownloadRecordActivity.this.sendBroadcast(intent);
                       }
                   }
                   Constants.update=true;
                   Log.d(TAG, "--------update="+Constants.update);
               }
           }
       };

	
	private void registerDownloadObserver(int flag){
		switch(flag){
		case FLAG_DOWNLOADING:
			mDownloadingObserver = new DownloadingObserver(mHandler);
			if(mDownloadingObserver!=null){
				getContentResolver().registerContentObserver(DownloadTaskColumn.CONTENT_URI,true,mDownloadingObserver);
			}
			break;
		case FLAG_DOWNLOADED:
		case FLAG_INSTALLED:
			mApplicationObserver = new ApplicationObserver(mHandler);
			if(mApplicationObserver!=null){
				getContentResolver().registerContentObserver(ApplicationColumn.CONTENT_URI,true,mApplicationObserver);
			}
			break;
		}
	}
	
	private void unregisterDownloadObserver(){
		if(mDownloadingObserver!=null){
			getContentResolver().unregisterContentObserver(mDownloadingObserver);
		}
	}

	@Override
	protected void onDestroy() {
		unbindService(mServiceConnection);
		if(mCursor!=null){
			mCursor.close();
		}
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return super.onKeyUp(keyCode, event);
	}

	@Override
	protected void onPause() {
		super.onPause();
		isActivie=false;
		unregisterDownloadObserver();
	}
	
	protected void onStop(){
		super.onStop();
	}

class DownloadingObserver extends ContentObserver{
		
		public static final int MSG_REFRESH_DOWNLOADING_LISTVIEW = 1;
		public static final int DELAY_TIME = 500;
		
		public DownloadingObserver(Handler handler) {
			super(handler);
		}

		@Override
		public boolean deliverSelfNotifications() {
			return super.deliverSelfNotifications();
		}
		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			if(!isActivie)
				return;
			AppLog.d(TAG, "-----DownloadingObserver---");
				Message msg=workHandler.obtainMessage(MSG_REFRESH_DOWNLOADING_LISTVIEW);
				workHandler.sendMessageDelayed(msg,DelayTime);
				refeshFinish=false;
		}
		
		
	}

class ApplicationObserver extends ContentObserver{
	
	public static final int MSG_REFRESH_APP_LISTVIEW = 2;
	
	public ApplicationObserver(Handler handler) {
		super(handler);
	}

	@Override
	public boolean deliverSelfNotifications() {
		return super.deliverSelfNotifications();
	}

	@Override
	public void onChange(boolean selfChange) {
		super.onChange(selfChange);
		if(!isActivie)
			return;
		Log.d(TAG, "---------------------ApplicationObserver--------");
		Message msg=workHandler.obtainMessage(MSG_REFRESH_APP_LISTVIEW);
		workHandler.sendMessageDelayed(msg,DelayTime);
	}
	
	
}
}
