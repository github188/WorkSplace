package com.bestv.ott.appstore.activity;

import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
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
import android.widget.Toast;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Button;
import com.bestv.ott.appstore.R;
import com.bestv.ott.appstore.common.Constants;
import com.bestv.ott.appstore.common.TaskBean;
import com.bestv.ott.appstore.common.TaskDownSpeed;
import com.bestv.ott.appstore.db.DBUtils;
import com.bestv.ott.appstore.db.DatabaseHelper.ApplicationColumn;
import com.bestv.ott.appstore.db.DatabaseHelper.ApplicationColumnIndex;
import com.bestv.ott.appstore.db.DatabaseHelper.DownloadTaskColumn;
import com.bestv.ott.appstore.db.DatabaseHelper.DownloadTaskColumnIndex;
import com.bestv.ott.appstore.service.DownloadService;
import com.bestv.ott.appstore.service.DownloadService.ServiceBinder;
import com.bestv.ott.appstore.utils.AppLog;
import com.bestv.ott.appstore.utils.AppStoreConfig;
import com.bestv.ott.appstore.utils.Utils;

public class DownloadRecordActivity extends Activity {

	private final static String TAG = "com.joysee.appstore.DownloadRecordActivity";
	
	public static final int FLAG_DOWNLOADED = 1;

	public static final int FLAG_DOWNLOADING = 2;

	public static final int FLAG_INSTALLED = 3;

	private ImageButton mCloseButton;

//	private TextView mTab_downloaded;

	private TextView mTab_downloading;

	private TextView mTab_installed;
	
	private ImageView mArrowHead;

	private LinearLayout mListView;
	
	private Cursor mCursor = null;
	
	private ContentObserver mDownloadingObserver = null;
	
	private ContentObserver mApplicationObserver = null;
	
	private SimpleCursorAdapter taskAdapter = null;
	
	private AnimationDrawable draw = null;
	
	private ServiceBinder downloadService;
	
//	private IDownloadService downloadService;
	
	 private AlertDialog mOpeListDialog=null; 
	 
	 private int mCurrentType;//当前类型FLAG_DOWNLOADING，FLAG_INSTALLED
	 
	 /**
	     *backgroud work thread
	     */
	 private HandlerThread workThread = new HandlerThread("downloadmgr work thread");
	 /**
	     * backgroud thread handler
	     */
	 private Handler workHandler;
	 
	 /**
	     *backgroud clear thread
	     */
	 private HandlerThread clearThread = new HandlerThread("downloadmgr clear thread");
	 /**
	     * backgroud clear thread handler
	     */
	 private Handler clearHandler;
	 
	 private final int DelayTime = 500;
	 private final static int CONTINUE_PAUSE_DELAY = 300;
	 
	 private final int MSG_GET_PROGRESS = 99;
	 
	 private final int MSG_REMOVE_EXTRA = MSG_GET_PROGRESS+1;
	 private final static int MSG_PAUSE_CONTINUE=10;
	 public static HashMap<String,Bitmap> bitMap=new HashMap<String,Bitmap>(); 
	 
	 
	 private final int CLEARDELAYTIME = 333;
	 
	 public boolean refeshFinish=true;//控制一段时间来进行刷新
	 private int selectType=0;//被选择，用来刷新数据后重新定位的，1表示应用图标，2表示删除按钮
	 private int selectIndex=-1;//被选择的行数
	 private DBUtils mDBUtils;
	 private Context mContext;
	 public boolean isActivie=true;
	 
	 private LayoutInflater mLayoutInflater;
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what){
			case DownloadingObserver.MSG_REFRESH_DOWNLOADING_LISTVIEW:
			case ApplicationObserver.MSG_REFRESH_APP_LISTVIEW:
				refreshUI();
				break;
			case MSG_PAUSE_CONTINUE:
				TaskBean pauseTask=(TaskBean)msg.obj;
				AppLog.d(TAG, "-----------mHandler-------MSG_PAUSE_CONTINUE-------status="+pauseTask.getStatus()+";name="+pauseTask.getAppName());
				if(pauseTask.getStatus()==Constants.DOWNLOAD_STATUS_EXECUTE){
					downloadService.pauseTask(pauseTask);
				}else if(pauseTask.getStatus()==Constants.DOWNLOAD_STATUS_PAUSE){
					downloadService.continueTask(pauseTask);
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
		resreshView();
		if(closeFocus){
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
						view.findViewById(R.id.down_progress_layout).requestFocus();
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
					view2.findViewById(R.id.down_progress_layout).requestFocus();
				}
			}
		}
		
	}
	
	private BroadcastReceiver mReciever = new  BroadcastReceiver(){
		
		public void onReceive(Context context, Intent intent) {
			AppLog.d(TAG, "---------onReceive action="+intent.getAction());
				if(intent.getAction().equals(Constants.INTENT_UNINSTALL_COMPLETED)){
					refreshUI();
				}
		}
	};
	
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
	
	
	private void startClearThread(){
		clearThread.start();
		clearHandler = new Handler(clearThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what){
    			case MSG_REMOVE_EXTRA:
    				workHandler.removeMessages(DownloadingObserver.MSG_REFRESH_DOWNLOADING_LISTVIEW);
    				workHandler.removeMessages(ApplicationObserver.MSG_REFRESH_APP_LISTVIEW);
    				mHandler.removeMessages(DownloadingObserver.MSG_REFRESH_DOWNLOADING_LISTVIEW);
    				mHandler.removeMessages(ApplicationObserver.MSG_REFRESH_APP_LISTVIEW);
    				clearHandler.sendMessageDelayed(clearHandler.obtainMessage(MSG_REMOVE_EXTRA),CLEARDELAYTIME);
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
		setContentView(R.layout.downloadmgr);
		mContext=this;
		mLayoutInflater = LayoutInflater.from(DownloadRecordActivity.this);
		mDBUtils=new DBUtils(DownloadRecordActivity.this);
		Intent intent = new Intent("com.bestv.ott.appstore.service.DownloadService");
	    bindService(intent,mServiceConnection, Context.BIND_AUTO_CREATE);
		setupViews();
		startWorkThread();
	}

	
	private void initListView(final int flag){
		
		switch(flag){
		case FLAG_DOWNLOADING: 
			AppLog.d(TAG, "===============enter initListView========flag="+flag);
			
			mCurrentType=FLAG_DOWNLOADING;
			mCursor = getContentResolver().query(DownloadTaskColumn.CONTENT_URI, null, null, null, null);
			AppLog.d(TAG, "--------------initview-FLAG_DOWNLOADING---mCursor.getCount()="+mCursor.getCount());
			resreshView();
	        registerDownloadObserver(FLAG_DOWNLOADING);
			break;
			
		case FLAG_INSTALLED:
			
			AppLog.d(TAG, "===============enter initListView========flag="+flag);
			
			mCurrentType=FLAG_INSTALLED;
			mCursor = getContentResolver().query(ApplicationColumn.CONTENT_URI, null, ApplicationColumn.STATUS+" = ? and "+ApplicationColumn.APPSOURCE+"!=? ",
					new String[]{""+Constants.APP_STATUS_INSTALLED,""+Constants.APP_SOURCE_INNER}, ApplicationColumn.ID+" desc");
			AppLog.d(TAG, "--------------initview-FLAG_INSTALLED---mCursor.getCount()="+mCursor.getCount());
			resreshView();
	        registerDownloadObserver(FLAG_INSTALLED);
			break;
			
		}
		
	}
	
	public void resreshView(){
		AppLog.d(TAG, "-------------resreshView");
		mListView.removeAllViewsInLayout();
		mListView.removeAllViews();
		int index=0;
		if(mCurrentType==FLAG_DOWNLOADING){ 
				AppLog.d(TAG, "--------------resreshView-FLAG_DOWNLOADING---"+mCursor.getCount());
	        	while(mCursor.moveToNext()){
	        		AppLog.d(TAG, "-------FLAG_DOWNLOADING----"+mCursor.getString(DownloadTaskColumnIndex.APPNAME));
	        		View view=mLayoutInflater.inflate(R.layout.listview_downloading_item, null);
	        		((TextView)view.findViewById(R.id.task_id)).setText(""+mCursor.getInt(DownloadTaskColumnIndex.ID));
	        		view.setTag(mCursor.getInt(DownloadTaskColumnIndex.ID));
	        		byte b[]=mCursor.getBlob(DownloadTaskColumnIndex.ICON);
	        		TaskBean task = Utils.cursorToTaskBean(mCursor);
	            	if(b!=null){
	            		 ImageButton imageButton=(ImageButton)view.findViewById(R.id.app_icon);
	            		 if(bitMap.containsKey(task.getPkgName())){
		            		 imageButton.setImageBitmap(bitMap.get(task.getPkgName()));
		            	 }else{
		            		 BitmapFactory.Options option = new BitmapFactory.Options();
			            	 option.inSampleSize = 2; //将图片设为原来宽高的1/2，防止内存溢出
			            	 Bitmap bm =BitmapFactory.decodeByteArray(b, 0, b.length, option);
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
	            	((Button)view.findViewById(R.id.delete)).setOnClickListener(new OnClickListener(){
						public void onClick(View v) {
							AppLog.d(TAG, "------------delete AppName="+((TaskBean)v.getTag()).getAppName());
							downloadService.delTask((TaskBean)v.getTag());
							DownloadRecordActivity.bitMap.remove(((TaskBean)v.getTag()).getPkgName());
						}
	            	});
	            	deleteBut.setOnFocusChangeListener(new DeleteFocusListener(mCursor.getInt(DownloadTaskColumnIndex.ID)));
	            	LinearLayout pro_layout=(LinearLayout)view.findViewById(R.id.down_progress_layout);
	            	pro_layout.setFocusable(true);
	            	pro_layout.setTag(task.getId());
	            	pro_layout.setOnClickListener(new OnClickListener(){
						public void onClick(View v) {
							mHandler.removeMessages(MSG_PAUSE_CONTINUE);
							Message mes=mHandler.obtainMessage(MSG_PAUSE_CONTINUE);
							TaskBean clickTask=mDBUtils.queryTaskById((Integer)v.getTag());
							if(clickTask==null)
								return;
							mes.obj=clickTask;
							mHandler.sendMessageDelayed(mes, CONTINUE_PAUSE_DELAY);
						}
	            	});
	            	pro_layout.setOnFocusChangeListener(new ProgressFocusListener(mCursor.getInt(DownloadTaskColumnIndex.ID)));
	            	TextView pro_tex=(TextView)view.findViewById(R.id.down_progress_text);
	            	int status=mCursor.getInt(DownloadTaskColumnIndex.STATUS);
	            	AppLog.d(TAG, "-------------resreshView----status="+status);
	            	if(status==Constants.DOWNLOAD_STATUS_ERROR){
	            		pro_tex.setText(DownloadRecordActivity.this.getString(R.string.download_error));
	            	}else if(status==Constants.DOWNLOAD_STATUS_EXECUTE){
	            		TaskDownSpeed tds=DownloadService.speedMap.get(task.getId());
	            		if(tds!=null){
		            		pro_tex.setText(""+tds.getSpeed()+"  "+DownloadRecordActivity.this.getString(R.string.down_speed));
	            		}else{
	            			pro_tex.setText("0  "+DownloadRecordActivity.this.getString(R.string.down_speed));
	            		}
	            	}else if(status==Constants.DOWNLOAD_STATUS_PAUSE){
	            		pro_tex.setText(DownloadRecordActivity.this.getString(R.string.pause_download));
	            	}
	            	index++;
	            	mListView.addView(view);
	        	}
		}else if(mCurrentType==FLAG_INSTALLED){
				AppLog.d(TAG, "--------------resreshView-FLAG_INSTALLED---"+mCursor.getCount());
	        	while(mCursor.moveToNext()){
//	        		AppLog.d(TAG, "------FLAG_INSTALLED-----name="+mCursor.getString(ApplicationColumnIndex.APPNAME)+";pkgname="+mCursor.getString(ApplicationColumnIndex.PKGNAME));
	        		View view=mLayoutInflater.inflate(R.layout.listview_installed_item, null);
	        		view.setTag(mCursor.getInt(ApplicationColumnIndex.ID));
	        		((TextView)view.findViewById(R.id.application_id)).setText(""+mCursor.getInt(ApplicationColumnIndex.ID));
	        		byte b[]=mCursor.getBlob(ApplicationColumnIndex.ICON);
	        		String pakName=mCursor.getString(ApplicationColumnIndex.PKGNAME);
	            	if(b!=null){
	            		 ImageButton imageButton=(ImageButton)view.findViewById(R.id.app_icon);
	            		 if(bitMap.containsKey(pakName)){
		            		 imageButton.setImageBitmap(bitMap.get(pakName));
		            	 }else{
		            		 BitmapFactory.Options option = new BitmapFactory.Options();
			            	 option.inSampleSize = 2; //将图片设为原来宽高的1/2，防止内存溢出
			            	 Bitmap bm =BitmapFactory.decodeByteArray(b, 0, b.length, option);
			            	 imageButton.setImageBitmap(bm);
			            	 bitMap.put(pakName, bm);
		            	 }
	            		 imageButton.setTag(pakName);
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
	            	((TextView)view.findViewById(R.id.appliation_pkgname)).setText(""+mCursor.getString(ApplicationColumnIndex.PKGNAME));
	            	Button deleteBut=(Button)view.findViewById(R.id.delete);
	            	deleteBut.setTag(mCursor.getString(ApplicationColumnIndex.PKGNAME));
	            	((Button)view.findViewById(R.id.delete)).setOnClickListener(new OnClickListener(){
						public void onClick(View v) {
							AppLog.d(TAG, "------------task_id="+v.getTag());
							deleteApp((String)v.getTag());
						}
	            	});
	            	deleteBut.setOnFocusChangeListener(new DeleteFocusListener(mCursor.getInt(ApplicationColumnIndex.ID)));
	            	index++;
	            	mListView.addView(view);
	        	}
		}
	}
	
	public void deleteApp(String pkgName){
		Uri packageURI = Uri.parse("package:"+pkgName);   
		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);   
		startActivity(uninstallIntent);
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
		mCloseButton = (ImageButton) findViewById(R.id.close);
		mTab_downloading = (TextView) findViewById(R.id.tab_2);
		mTab_installed = (TextView) findViewById(R.id.tab_3);
		mListView = (LinearLayout) findViewById(R.id.appslist);
		mArrowHead = (ImageView) findViewById(R.id.tab_arrowhead);
		mCloseButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(v.getId()==R.id.close){
					finish();
				}
			}	
		});
		setCloseFocuseChange(mCloseButton);
		mTab_downloading.setOnFocusChangeListener(new View.OnFocusChangeListener() {
					public void onFocusChange(View v, boolean hasFocus) {
						if (hasFocus) {
							selectType=0;
							selectIndex=-1;
							initListView(FLAG_DOWNLOADING);
							mCurrentType=FLAG_DOWNLOADING;
							mArrowHead.setY(mTab_downloading.getY());
						}
					}
				});

		mTab_installed.setOnFocusChangeListener(new View.OnFocusChangeListener() {
					public void onFocusChange(View v, boolean hasFocus) {
						if (hasFocus) {
							selectType=0;
							selectIndex=-1;
							initListView(FLAG_INSTALLED);
							mCurrentType=FLAG_INSTALLED;
							mArrowHead.setY(mTab_installed.getY());
						}
					}
				});

		mTab_downloading.requestFocus();
	}
	

	@Override
	protected void onStart() {
		super.onStart();
		IntentFilter filter = new IntentFilter();
	    filter.addAction(Constants.INTENT_UNINSTALL_COMPLETED);
	    registerReceiver(mReciever, filter);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		isActivie=true;
	}

	
	private void setCloseFocuseChange(View view){
		view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			AnimationDrawable draw = null;
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					((ImageView)v).setImageResource(R.drawable.close_selected_animation);
					draw = (AnimationDrawable) ((ImageView)v).getDrawable();
					draw.start();
				}else{
					if(null!=draw&&draw.isRunning()){
						((ImageView)v).setImageDrawable(null);
						draw.stop();
					}
					if(mListView.getChildCount()<1){
						mTab_downloading.requestFocus();
					}
				}
			}
		});
	}
	
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
		AppLog.d(TAG, "------------------onDestroy-----------------");
		super.onDestroy();
		unbindService(mServiceConnection);
		unregisterDownloadObserver();
		if(mCursor!=null){
			mCursor.close();
		}
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
	}
	
	protected void onStop(){
		super.onStop();
		unregisterReceiver(mReciever);
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
			workHandler.removeMessages(MSG_REFRESH_DOWNLOADING_LISTVIEW);
			Message msg=workHandler.obtainMessage(MSG_REFRESH_DOWNLOADING_LISTVIEW);
			workHandler.sendMessageDelayed(msg,DelayTime);
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
		workHandler.removeMessages(MSG_REFRESH_APP_LISTVIEW);
		Message msg=workHandler.obtainMessage(MSG_REFRESH_APP_LISTVIEW);
		workHandler.sendMessageDelayed(msg,DelayTime);
	}
	
	
}
}
