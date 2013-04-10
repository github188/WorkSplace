package com.bestv.ott.appstore.activity;

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
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import com.bestv.ott.appstore.R;
import com.bestv.ott.appstore.common.Constants;
import com.bestv.ott.appstore.common.TaskBean;
import com.bestv.ott.appstore.db.DatabaseHelper.ApplicationColumn;
import com.bestv.ott.appstore.db.DatabaseHelper.ApplicationColumnIndex;
import com.bestv.ott.appstore.db.DatabaseHelper.DownloadTaskColumn;
import com.bestv.ott.appstore.db.DatabaseHelper.DownloadTaskColumnIndex;
import com.bestv.ott.appstore.service.DownloadService;
import com.bestv.ott.appstore.service.DownloadService.ServiceBinder;
import com.bestv.ott.appstore.utils.AppLog;
import com.bestv.ott.appstore.utils.AppStoreConfig;
import com.bestv.ott.appstore.utils.Utils;

public class DownloadMgrActivity extends Activity {

	private final static String TAG = "com.joysee.appstore.DownloadMgrActivity";
	
	public static final int FLAG_DOWNLOADED = 1;

	public static final int FLAG_DOWNLOADING = 2;

	public static final int FLAG_INSTALLED = 3;

	private ImageButton mCloseButton;

//	private TextView mTab_downloaded;

	private TextView mTab_downloading;

	private TextView mTab_installed;
	
	private ImageView mArrowHead;

	private ListView mListView;
	
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
	 
	 private final int DelayTime = 100;
	 
	 private final int MSG_GET_PROGRESS = 99;
	 
	 private final int MSG_REMOVE_EXTRA = MSG_GET_PROGRESS+1;
	 
	 
	 
	 private final int CLEARDELAYTIME = 333;
	 
	 public boolean refeshFinish=true;//控制一段时间来进行刷新
	 
	 
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what){
			case DownloadingObserver.MSG_REFRESH_DOWNLOADING_LISTVIEW:
			case ApplicationObserver.MSG_REFRESH_APP_LISTVIEW:
				refreshUI();
				break;
//			case MSG_GET_PROGRESS:
//				refreshUI();
//				mHandler.sendEmptyMessageDelayed(MSG_GET_PROGRESS, DelayTime);
//				break;
			}
		}
		
	};
	
	private void refreshUI(){
		if(taskAdapter!=null){//TODO here only refresh downloading listview by yuhongkun
			boolean hasFocus = false;
			int selectIndex = 0;
			if(mListView.hasFocus()||mListView.isFocused())
			{
				hasFocus = true;
				selectIndex = mListView.getSelectedItemPosition();
			}
			mCursor.deactivate();
			mCursor.requery();
			AppLog.d(TAG, "-----------------###########------mListView="+mListView.hasFocus());
			taskAdapter.notifyDataSetChanged();
			if(hasFocus){
				AppLog.d(TAG, "-----------------###########------selectIndex="+selectIndex);
				mListView.requestFocus();
				mListView.setSelection(selectIndex);
			}
			
		}
		refeshFinish=true;
	}
	
	private BroadcastReceiver mReciever = new  BroadcastReceiver(){
		
		public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals(Constants.INTENT_DOWNLOAD_STARTED)
					||intent.getAction().equals(Constants.INTENT_DOWNLOAD_COMPLETED)
					||intent.getAction().equals(Constants.INTENT_INSTALL_COMPLETED)){
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
//    			mHandler.removeMessages(msg.what);
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
        	AppLog.d(TAG, "================enter onServiceConnected====");
        	downloadService = ((DownloadService.ServiceBinder)service).getService();
        }
//		public void onServiceConnected(ComponentName name, IBinder service) {
//			try {
//				service.linkToDeath(new IBinder.DeathRecipient() {
//
//					public void binderDied() {
//						onServiceDied();
//					}
//				}, 0);
//			} catch (RemoteException e) {
//				e.printStackTrace();
//			}
//			downloadService = IDownloadService.Stub
//					.asInterface(service);
//
//		}
//
//		public void onServiceDisconnected(ComponentName name) {
//			onServiceDied();
//		}
    };
    
    protected void onServiceDied() {
		downloadService = null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.downloadmgr);
		Intent intent = new Intent("com.bestv.ott.appstore.service.DownloadService");
	    bindService(intent,mServiceConnection, Context.BIND_AUTO_CREATE);
		setupViews();
		startWorkThread();
		IntentFilter filter = new IntentFilter();
	    filter.addAction(Constants.INTENT_DOWNLOAD_STARTED);
//	    filter.addAction(Contants.INTENT_DOWNLOAD_COMPLETED);
	    filter.addAction(Constants.INTENT_INSTALL_COMPLETED);
	    registerReceiver(mReciever, filter);
	}

	
	private void initListView(final int flag){
		
		switch(flag){
		case FLAG_DOWNLOADING: 
			AppLog.d(TAG, "===============enter initListView========flag="+flag);
			 
			mCursor = getContentResolver().query(DownloadTaskColumn.CONTENT_URI, null, null, null, null);
			mListView = (ListView) findViewById(R.id.appslist);
			
			String[] fromColumns_1 = new String[] {
	                DownloadTaskColumn.ID,
	                DownloadTaskColumn.ICON,
	                DownloadTaskColumn.APPNAME,
	                DownloadTaskColumn.DOWNLOADSIZE
	                };
	        int[] toLayoutIDs_1 = new int[] {R.id.task_id,
	                R.id.app_icon,
	                R.id.downmgr_item_name,
	                R.id.downmgr_item_progress};
	        taskAdapter = new SimpleCursorAdapter(this,
	                R.layout.listview_downloading_item, mCursor, fromColumns_1,
	                toLayoutIDs_1);
	        taskAdapter.setViewBinder(downloadingviewBinder);
	        mListView.setAdapter(taskAdapter);
	        taskAdapter.notifyDataSetChanged();
	        mListView.invalidate();
	        registerDownloadObserver(FLAG_DOWNLOADING);
			break;
//		case FLAG_DOWNLOADED:
//			if(AppStoreConfig.DEBUG){
//			Log.d(TAG, "===============enter initListView========flag="+flag);
//			}
//			mCursor = getContentResolver().query(ApplicationColumn.CONTENT_URI, null, null,
//					null, null);
//			mListView = (ListView) findViewById(R.id.appslist);
//			
//			String[] fromColumns_2 = new String[] {
//					ApplicationColumn.ID,
//					ApplicationColumn.ICON,
//					ApplicationColumn.APPNAME,
//					ApplicationColumn.PKGNAME
//	                };
//	        int[] toLayoutIDs_2 = new int[] {R.id.application_id,
//	                R.id.application_icon,
//	                R.id.application_name,
//	                R.id.appliation_pkgname};
//	        taskAdapter = new SimpleCursorAdapter(this,
//	                R.layout.listview_downloaded_item, mCursor, fromColumns_2,
//	                toLayoutIDs_2);
//	        taskAdapter.setViewBinder(downloadedviewBinder);
//	        mListView.setAdapter(taskAdapter);
//	        taskAdapter.notifyDataSetChanged();
//	        mListView.invalidate();
//	        registerDownloadObserver(FLAG_DOWNLOADED);
//			break;
			
		case FLAG_INSTALLED:
			
			AppLog.d(TAG, "===============enter initListView========flag="+flag);
			
			mCursor = getContentResolver().query(ApplicationColumn.CONTENT_URI, null, ApplicationColumn.STATUS+" = ? and "+ApplicationColumn.APPSOURCE+" = ?",
					new String[]{""+Constants.APP_STATUS_INSTALLED,""+Constants.APP_SOURCE_STORE}, null);
			String[] fromColumns_3 = new String[] {
					ApplicationColumn.ID,
					ApplicationColumn.ICON,
					ApplicationColumn.APPNAME,
					ApplicationColumn.PKGNAME
	                };
	        int[] toLayoutIDs_3 = new int[] {R.id.application_id,
	                R.id.app_icon,
	                R.id.application_name,
	                R.id.appliation_pkgname};
	        taskAdapter = new SimpleCursorAdapter(this,
	                R.layout.listview_installed_item, mCursor, fromColumns_3,
	                toLayoutIDs_3);
	        taskAdapter.setViewBinder(installedviewBinder);
	        mListView.setAdapter(taskAdapter);
	        taskAdapter.notifyDataSetChanged();
	        mListView.invalidate();
	        registerDownloadObserver(FLAG_INSTALLED);
			break;
			
		}
		setItemClickEvent(flag);
		
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if(event.getAction()==KeyEvent.ACTION_DOWN){
			if(event.getKeyCode()==KeyEvent.KEYCODE_ENTER||event.getKeyCode()==KeyEvent.KEYCODE_DPAD_CENTER){
				if(mListView.isFocusable()&&mListView.isFocused()){
					if(mCursor.moveToPosition(mListView.getSelectedItemPosition())){
						if(mCurrentType==FLAG_DOWNLOADING){
							AppLog.d(TAG, "------------------dispatchKeyEvent downing");
							TaskBean task = Utils.cursorToTaskBean(mCursor);
							Intent intent=new Intent();
							intent.setClass(DownloadMgrActivity.this, DetailedActivity.class);
							intent.putExtra("app_id",Integer.parseInt(task.getSerAppID()));
							startActivity(intent);
							finish();
						}else if(mCurrentType==FLAG_INSTALLED){
							String pkgName = mCursor.getString(ApplicationColumnIndex.PKGNAME);
							Intent intent = new Intent();
		    				intent = getPackageManager().getLaunchIntentForPackage(pkgName);
							startActivity(intent);
							finish();
						}
					}
				}
			}
        }
		return super.dispatchKeyEvent(event);
	}
	
	private void setItemClickEvent(final int flag){
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
//				switch(flag){
//				case FLAG_DOWNLOADING:
//					if(mCursor.moveToPosition(position)){
//						TaskBean task = Utils.cursorToTaskBean(mCursor);
//						Intent intent = new Intent();
////						intent.setPackage("com.bestv.ott.appstore");
////						intent.setAction("bestv.ott.action.appstore");
//						intent.setClass(view.getContext(), AppDetailedActivity.class);
//						intent.putExtra("app_id",Integer.parseInt(task.getSerAppID()));
//						startActivity(intent);
//						finish();
//					}
//					break;
//				case FLAG_INSTALLED:
//					if(mCursor.moveToPosition(position)){
//						String pkgName = mCursor.getString(ApplicationColumnIndex.PKGNAME);
//						Intent intent = new Intent();
//	    				intent = getPackageManager().getLaunchIntentForPackage(pkgName);
//						startActivity(intent);
//						finish();
//					}
//					break;
//				}
			}
		});
	}
	/*
	private void setItemClickEvent(final int flag){
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
					AlertDialog.Builder builder = new AlertDialog.Builder(DownloadMgrActivity.this);
			        View viewLayout = LayoutInflater.from(DownloadMgrActivity.this).inflate(R.layout.app_layout, null);
//			        viewLayout.setBackgroundColor(R.color.transparent_background);
//			        viewLayout.setBackgroundResource(R.drawable.down_bg);
			        ListView listView = (ListView) viewLayout.findViewById(R.id.appslist);
			        TextView titleView = (TextView) viewLayout.findViewById(R.id.title);
				switch(flag){
				case FLAG_DOWNLOADING:
					List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
					final TaskBean task = Utils.cursorToTaskBean(taskAdapter.getCursor());
					titleView.setText(task.getAppName());
			        Map<String, Object> map = new HashMap<String, Object>();
			        map.put("img", R.drawable.down_record_icon);
			        if(task.getStatus()==Contants.DOWNLOAD_STATUS_EXECUTE)
                	{
			        	map.put("title", getString(R.string.pause_download));
                	}else if(task.getStatus()==Contants.DOWNLOAD_STATUS_PAUSE){
                		map.put("title", getString(R.string.continue_download));
                	}
			        list.add(map);
			        map = new HashMap<String, Object>();
			        map.put("img", R.drawable.down_record_icon);
			        map.put("title", getString(R.string.cancel));
			        list.add(map);
			        SimpleAdapter adapter = new SimpleAdapter(DownloadMgrActivity.this,
			                list, R.layout.app_item_layout,
			                new String[] { "img", "title" }, new int[] { R.id.img,
			                        R.id.title });
			        listView.setAdapter(adapter);
			        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			            @Override
			            public void onItemClick(AdapterView<?> parent, View view,
			                    int position, long id) {
			            	mOpeListDialog.dismiss();
			                switch(position){
			                case 0:// pause
			                	if(task.getStatus()==Contants.DOWNLOAD_STATUS_EXECUTE)
			                	{
			                		downloadService.pauseTask(task);
			                	}else if(task.getStatus()==Contants.DOWNLOAD_STATUS_PAUSE){
			                		downloadService.executeTask(task);
			                	}
			                    break;
			                case 1:// cancel
			                	downloadService.delTask(task);
			                    break;
			                }
			            }
			        });
			        builder.setView(viewLayout);
			        mOpeListDialog = builder.create();
			        ImageButton close = (ImageButton) viewLayout.findViewById(R.id.close);
			        setCloseFocuseChange(close);
			        close.setOnClickListener(new OnClickListener() {
			            public void onClick(View v) {
			            	mOpeListDialog.dismiss();
			            }
			        });
//			        mOpeListDialog.getWindow().setLayout(350, 400);
			        mOpeListDialog.show();
					break;
				case FLAG_DOWNLOADED:
					
					break;
				case FLAG_INSTALLED:
					
					break;
				}
				
				
				
			}
		});
	}
	*/
	
	private void setupViews() {
		mCloseButton = (ImageButton) findViewById(R.id.close);
//		mTab_downloaded = (TextView) findViewById(R.id.tab_1);
		mTab_downloading = (TextView) findViewById(R.id.tab_2);
		mTab_installed = (TextView) findViewById(R.id.tab_3);
		mListView = (ListView) findViewById(R.id.appslist);
		mArrowHead = (ImageView) findViewById(R.id.tab_arrowhead);
		mCloseButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(v.getId()==R.id.close){
					finish();
				}
			}	
		});
		
		setCloseFocuseChange(mCloseButton);
		
//		mCloseButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//			
//			@Override
//			public void onFocusChange(View v, boolean hasFocus) {
//				if (hasFocus) {
////					mCloseButton.setBackgroundResource(R.drawable.close_selected_animation);
////					draw = (AnimationDrawable) mCloseButton.getBackground(); 
//					mCloseButton.setImageResource(R.drawable.close_selected_animation);
//					draw = (AnimationDrawable) mCloseButton.getDrawable();
//					draw.start();
////					Animation closeAnimation = AnimationUtils.loadAnimation(v.getContext(), R.drawable.close_selected_animation);
////					closeAnimation.setFillAfter(true);
////					mCloseButton.startAnimation(closeAnimation);
//				}else{
//					
//					if(null!=draw&&draw.isRunning()){
////						mCloseButton.setBackgroundDrawable(null);
//						mCloseButton.setImageDrawable(null);
//						draw.stop();
//					}
//				}
//			}
//		});
        
//		mTab_downloaded
//				.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//					public void onFocusChange(View v, boolean hasFocus) {
//						if (hasFocus) {
//							initListView(FLAG_DOWNLOADED);
//							mArrowHead.setY(mTab_downloaded.getY());
//						}
//						if(AppStoreConfig.DEBUG){
//						Log.i(TAG, "   y=" + mTab_downloading.getY() + "  " + mTab_downloaded.getY()
//								+ "  " + mTab_installed.getY() + "  " + mArrowHead.getY());
//						}
//					}
//				});

		mTab_downloading
				.setOnFocusChangeListener(new View.OnFocusChangeListener() {

					public void onFocusChange(View v, boolean hasFocus) {
						if (hasFocus) {
							initListView(FLAG_DOWNLOADING);
							mCurrentType=FLAG_DOWNLOADING;
							mArrowHead.setY(mTab_downloading.getY());
						}
					}
				});

		mTab_installed
				.setOnFocusChangeListener(new View.OnFocusChangeListener() {

					public void onFocusChange(View v, boolean hasFocus) {
						if (hasFocus) {
							initListView(FLAG_INSTALLED);
							mCurrentType=FLAG_INSTALLED;
							mArrowHead.setY(mTab_installed.getY());
						}
					}
				});

		mTab_downloading.requestFocus();
		
		
	}
	
	
	/**
	 * viewBinder:show corresponding info eg: text image
	 * */
    private ViewBinder downloadingviewBinder = new ViewBinder() {
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            switch (columnIndex) {
            case DownloadTaskColumnIndex.ID:
            	taskAdapter.setViewText((TextView)view,""+cursor.getInt(DownloadTaskColumnIndex.ID));
                break;
            case DownloadTaskColumnIndex.ICON:
            	byte b[]=cursor.getBlob(DownloadTaskColumnIndex.ICON);
            	if(b!=null){
            		BitmapFactory.Options option = new BitmapFactory.Options();
            		 option.inSampleSize = 2; //将图片设为原来宽高的1/2，防止内存溢出
            		 Bitmap bm =BitmapFactory.decodeByteArray(b, 0, b.length, option);
//                	taskAdapter.setViewImage(((ImageViwe)view), bt);
                	((ImageView)view).setImageBitmap(bm);
            	}
                break;
            case DownloadTaskColumnIndex.APPNAME:
            	taskAdapter.setViewText((TextView)view,""+cursor.getString(DownloadTaskColumnIndex.APPNAME));
//            	Log.d(TAG,"**************appName ="+cursor.getString(DownloadTaskColumnIndex.APPNAME));
//            	((TextView)view).setText(cursor.getString(DownloadTaskColumnIndex.APPNAME));
                break;
            case DownloadTaskColumnIndex.DOWNLOADSIZE:
//            	if(AppStoreConfig.DEBUG){
//            	Log.d(TAG,"**************appName ="+cursor.getString(DownloadTaskColumnIndex.APPNAME)+"***downloadsize="
//            			+cursor.getInt( DownloadTaskColumnIndex.DOWNLOADSIZE)+"*******sumsize="+cursor.getInt( DownloadTaskColumnIndex.SUMSIZE)
//            			+"****progress="+cursor.getInt( DownloadTaskColumnIndex.DOWNLOADSIZE)*100/cursor.getInt( DownloadTaskColumnIndex.SUMSIZE));
//            	}
            	((ProgressBar)view).setProgress(cursor.getInt( DownloadTaskColumnIndex.DOWNLOADSIZE)*100/cursor.getInt( DownloadTaskColumnIndex.SUMSIZE));
                break;
            }
            return true;
        }
    };
    
    /**
	 * viewBinder:show corresponding info eg: text image
	 * */
    private ViewBinder downloadedviewBinder = new ViewBinder() {
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            switch (columnIndex) {
            case ApplicationColumnIndex.ID:
            	taskAdapter.setViewText((TextView)view,""+cursor.getInt(ApplicationColumnIndex.ID));
                break;
            case ApplicationColumnIndex.ICON:
            	byte b[]=cursor.getBlob(ApplicationColumnIndex.ICON);
            	if(b!=null){
            		BitmapFactory.Options option = new BitmapFactory.Options();
            		 option.inSampleSize = 2; //将图片设为原来宽高的1/2，防止内存溢出
            		 Bitmap bm =BitmapFactory.decodeByteArray(b, 0, b.length, option);
//                	taskAdapter.setViewImage(((ImageViwe)view), bt);
                	((ImageView)view).setImageBitmap(bm);
            	}
                break;
            case ApplicationColumnIndex.APPNAME:
            	taskAdapter.setViewText((TextView)view,""+cursor.getString(ApplicationColumnIndex.APPNAME));
//            	if(AppStoreConfig.DEBUG){
//            	Log.d(TAG,"**************appName ="+cursor.getString(ApplicationColumnIndex.APPNAME));
//            	}
                break;
//            case ApplicationColumnIndex.PKGNAME:
//            	taskAdapter.setViewText((TextView)view,""+cursor.getString(ApplicationColumnIndex.PKGNAME));
//                break;
            }
            return true;
        }
    };
    
    /**
	 * viewBinder:show corresponding info eg: text image
	 * */
    private ViewBinder installedviewBinder = new ViewBinder() {
    	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            switch (columnIndex) {
            case ApplicationColumnIndex.ID:
            	taskAdapter.setViewText((TextView)view,""+cursor.getInt(ApplicationColumnIndex.ID));
                break;
            case ApplicationColumnIndex.ICON:
            	byte b[]=cursor.getBlob(ApplicationColumnIndex.ICON);
            	if(b!=null){
            		BitmapFactory.Options option = new BitmapFactory.Options();
            		 option.inSampleSize = 2; //将图片设为原来宽高的1/2，防止内存溢出
            		 Bitmap bm =BitmapFactory.decodeByteArray(b, 0, b.length, option);
//                	taskAdapter.setViewImage(((ImageViwe)view), bt);
                	((ImageView)view).setImageBitmap(bm);
            	}
                break;
            case ApplicationColumnIndex.APPNAME:
            	taskAdapter.setViewText((TextView)view,""+cursor.getString(ApplicationColumnIndex.APPNAME));
            	if(AppStoreConfig.DEBUG){
            	Log.d(TAG,"**************appName ="+cursor.getString(ApplicationColumnIndex.APPNAME));
            	}
                break;
//            case ApplicationColumnIndex.PKGNAME:
//            	taskAdapter.setViewText((TextView)view,""+cursor.getString(ApplicationColumnIndex.PKGNAME));
//                break;
            }
            return true;
        }
    };

	@Override
	protected void onStart() {
		super.onStart();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
//		mTab_downloading.requestFocus();
//		mHandler.sendEmptyMessage(MSG_GET_PROGRESS);
//		startClearThread();
	}

	
	private void setCloseFocuseChange(View view){
		view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			AnimationDrawable draw = null;
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
//					mCloseButton.setBackgroundResource(R.drawable.close_selected_animation);
//					draw = (AnimationDrawable) mCloseButton.getBackground(); 
					((ImageView)v).setImageResource(R.drawable.close_selected_animation);
					draw = (AnimationDrawable) ((ImageView)v).getDrawable();
					draw.start();
//					Animation closeAnimation = AnimationUtils.loadAnimation(v.getContext(), R.drawable.close_selected_animation);
//					closeAnimation.setFillAfter(true);
//					mCloseButton.startAnimation(closeAnimation);
				}else{
					
					if(null!=draw&&draw.isRunning()){
//						mCloseButton.setBackgroundDrawable(null);
						((ImageView)v).setImageDrawable(null);
						draw.stop();
					}
				}
			}
		});
	}
	private void showApps(int flag) {

	}

	private void initEvents() {

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
		super.onDestroy();
		unbindService(mServiceConnection);
		unregisterDownloadObserver();
		unregisterReceiver(mReciever);
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
//		mHandler.removeMessages(MSG_GET_PROGRESS);
//		clearThread.getLooper().quit();
	}

class DownloadingObserver extends ContentObserver{
		
		public static final int MSG_REFRESH_DOWNLOADING_LISTVIEW = 1;
		
		public static final int DELAY_TIME = 500;
		
		public DownloadingObserver(Handler handler) {
			super(handler);
		}

		@Override
		public boolean deliverSelfNotifications() {
			//
			return super.deliverSelfNotifications();
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
//			if(AppStoreConfig.DEBUG){
//			Log.d(TAG,"====DownloadMgrActivity====onChange()============================================================");
//			}
//			if(refeshFinish){
				Message msg=workHandler.obtainMessage(MSG_REFRESH_DOWNLOADING_LISTVIEW);
//				workHandler.removeMessages(MSG_REFRESH_DOWNLOADING_LISTVIEW);
				workHandler.sendMessageDelayed(msg,DelayTime);
				refeshFinish=false;
//			}
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
//		if(AppStoreConfig.DEBUG){
//		Log.d(TAG,"====DownloadMgrActivity====onChange()============================================================");
//		}
		Message msg=workHandler.obtainMessage(MSG_REFRESH_APP_LISTVIEW);
//		workHandler.removeMessages(MSG_REFRESH_APP_LISTVIEW);
		workHandler.sendMessageDelayed(msg,DelayTime);
//		workHandler.sendMessage(msg);
	}
	
	
}
}
