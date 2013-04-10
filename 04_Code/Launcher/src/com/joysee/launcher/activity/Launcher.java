
package com.joysee.launcher.activity;

import java.util.ArrayList;
import java.util.HashMap;

import novel.supertv.dvb.aidl.IPlayService;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.joysee.launcher.common.AllAppViewAdapter;
import com.joysee.launcher.common.ApplicationInfo;
import com.joysee.launcher.common.Coverflow;
import com.joysee.launcher.common.CoverflowAdapter;
import com.joysee.launcher.common.CoverflowItem;
import com.joysee.launcher.common.DropdownListAdapter;
import com.joysee.launcher.common.DropdownListItem;
import com.joysee.launcher.common.FolderInfo;
import com.joysee.launcher.common.ItemInfo;
import com.joysee.launcher.common.ShortcutInfo;
import com.joysee.launcher.common.TimeView;
import com.joysee.launcher.utils.LauncherLog;
import com.joysee.launcher.utils.Utilities;
import com.joysee.launcher.utils.VideoLayerUtils;

public class Launcher extends Activity implements LauncherModel.Callbacks {

	private static final String TAG = "com.joysee.launcher.activity.Launcher";

	private FrameLayout mLauncherFullLayout;
	private FrameLayout mLauncherMenuLayout;
	private FrameLayout mLauncherAllAppslayout;
	private Coverflow mLauncherMenu;
	private FrameLayout mAdvertisingLayout;
	private TimeView mTimeView;
	private FrameLayout mMenuDropDownLayout;
	private LauncherModel mModel;
	private CoverflowAdapter mMenuAdapter;
	private ListView mDropDownList;
	private DropdownListAdapter mDropDownListAdapter;

	private GridView mAllAppGridview;
	private AllAppViewAdapter mAllAppAdapter;

	private IPlayService mPlayService;

	private enum State {
		WORKSPACE, APPS_CUSTOMIZE
	};

	private State mState = State.WORKSPACE;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
			case 1:
				MessageBean v = (MessageBean) msg.obj;
				if (v == null)
					return;
				int position = v.position;
				View view = v.view;

				long begin = System.currentTimeMillis();
				LauncherLog.log_D(TAG, "----onItemSelected   position = ---" + position);
				if (position >= 5) {
					position = position % 5;
				} else if (position < 0) {
					position = 5 - (Math.abs(position) % 5);
				}
				LauncherLog.log_D(TAG, "----onItemSelected   position = ---" + position);

				ArrayList<ShortcutInfo> dropdownItems = getDropDownListData((Long) view.getTag());

				if (mDropDownListAdapter == null) {
					mDropDownListAdapter = new DropdownListAdapter(Launcher.this, dropdownItems);
					mDropDownList.setAdapter(mDropDownListAdapter);
				} else {
					mDropDownListAdapter.setApps(dropdownItems);
				}

				mDropDownList.setOnItemClickListener(itemClickLis);

				mDropDownListAdapter.notifyDataSetChanged();
				LauncherLog.log_D(TAG, "onItemSelected takes time    " + (System.currentTimeMillis() - begin));

				break;
			}

		};
	};

	private void showAllAppPage(boolean show) {
		if (mLauncherAllAppslayout != null) {
			if (show) {
				mLauncherAllAppslayout.setVisibility(View.VISIBLE);
				mLauncherMenuLayout.setVisibility(View.GONE);
				mState = State.APPS_CUSTOMIZE;
			} else {
				mLauncherAllAppslayout.setVisibility(View.GONE);
				mLauncherMenuLayout.setVisibility(View.VISIBLE);
				mState = State.WORKSPACE;
			}
		}
	}

	public void onBackPressed() {
		if (mState == State.APPS_CUSTOMIZE) {
			showAllAppPage(false);
		}
	};

	OnItemClickListener itemClickLis = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			if (view instanceof DropdownListItem) {
				String itemText = ((DropdownListItem) view).getText().toString();
				LauncherLog.log_D(TAG, "((DropdownListItem) view).mParentIndex  "
						+ ((DropdownListItem) view).mParentIndex);
				if ("全部应用".equals(itemText) && 2 == ((DropdownListItem) view).mParentIndex) {
					showAllAppPage(true);
					return;
				}
			}

			if (view instanceof DropdownListItem) {
				DropdownListItem item = (DropdownListItem) view;
				final Intent intent = item.mIntent;
				LauncherLog.log_D(TAG, "startIntent is " + intent.toString());
				try {
					if (mPlayService != null) {
						try {
							mPlayService.stop();
							LauncherLog.log_D(TAG, "mPlayService.stop(); ");
							mPlayService.uninit();
							LauncherLog.log_D(TAG, "mPlayService.uninit(); ");
							unbindService(serviceConnectionForPlay);
							if(serviceIntent_play != null){
								stopService(serviceIntent_play);
								LauncherLog.log_D(TAG, "onItemStop   stopService ");
							}
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
					mHandler.postDelayed(new Runnable() {
						public void run() {
							startActivity(intent);
							LauncherLog.log_D(TAG, "startActivity(intent); ");
						}
					},2000);
				} catch (ActivityNotFoundException e) {
					Toast.makeText(Launcher.this, getResources().getString(R.string.activity_not_found),
							Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
			}

		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ColorDrawable colorDrawable = new ColorDrawable(Color.argb(0, 0, 0, 0));
		getWindow().setBackgroundDrawable(colorDrawable);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		LauncherApplication app = (LauncherApplication) getApplication();
		mModel = app.setLauncher(this);

		setContentView(R.layout.main);
		setupView();

		mModel.startLoader(this, true);

	}

	private void setupView() {

		mLauncherMenuLayout = (FrameLayout) findViewById(R.id.launcher_menu_layout);
		mLauncherAllAppslayout = (FrameLayout) findViewById(R.id.launcher_allapps_layout);
		showAllAppPage(false);
		Bitmap launcherBgBitmap = Utilities.createLauncherBackground(R.drawable.joysee_launcher_bg, this);
		mLauncherMenuLayout.setBackgroundDrawable(new BitmapDrawable(launcherBgBitmap));
		mTimeView = (TimeView) findViewById(R.id.launcher_timeview);
		mLauncherMenu = (Coverflow) findViewById(R.id.launcher_menu);
		mMenuAdapter = new CoverflowAdapter(this);

		mLauncherMenu.setOnItemClickListener(mMenuClickLis);
		mLauncherMenu.setOnItemSelectedListener(mMenuSelectedLis);
		boolean hardware = mLauncherMenu.isHardwareAccelerated();
		LauncherLog.log_D(TAG, "hardware           is " + hardware);
		mAdvertisingLayout = (FrameLayout) findViewById(R.id.launcher_advertisinglayout);
		mMenuDropDownLayout = (FrameLayout) findViewById(R.id.launcher_menu_dropdown_layout);

		mDropDownList = (ListView) findViewById(R.id.dropdown_listview);

		mAllAppGridview = (GridView) findViewById(R.id.launcher_allapp_gridview);
	}

	private Intent serviceIntent_play;
	@Override
	protected void onResume() {
		super.onResume();
		Intent serviceIntent_play = new Intent("novel.supertv.dvb.service.PlayService");
		startService(serviceIntent_play);
		bindService(serviceIntent_play, serviceConnectionForPlay, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStart() {
		super.onStart();
		mTimeView.startPreview();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mTimeView != null)
			mTimeView.stopPreview();

//		if (mPlayService != null) {
//			try {
//				mPlayService.stop();
//				mPlayService.uninit();
//			} catch (RemoteException e) {
//				e.printStackTrace();
//			}
//		}
	}

	@Override
	protected void onStop() {
//		unbindService(serviceConnectionForPlay);
		super.onStop();
	}

	private ServiceConnection serviceConnectionForPlay = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			try {
				service.linkToDeath(new IBinder.DeathRecipient() {

					public void binderDied() {
						mPlayService = null;
						Log.e(TAG, "playService binder died");
					}
				}, 0);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			Log.d(TAG, "onServiceConnected serviceConnectionForPlay");
			mPlayService = IPlayService.Stub.asInterface(service);
			// just for test:
			try {
				mPlayService.init();
				mPlayService.play();
				mPlayService.setWinSize(1146, 371, 720, 540);
				
			} catch (RemoteException e) {
				e.printStackTrace();
			}

		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, "onServiceDisconnected serviceConnectionForPlay");
			mPlayService = null;
		}

	};

	private OnItemClickListener mMenuClickLis = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			LauncherLog.log_D(TAG, "coverflow  on item clickLis");
			
			if (view instanceof CoverflowItem) {
				LauncherLog.log_D(TAG, " view instanceof CoverflowItem");
				TextView title = ((CoverflowItem) view).getItemTitle();
				if(title == null)
					return;
				LauncherLog.log_D(TAG, " title is not null");
				String itemText = title.getText().toString();
				LauncherLog.log_D(TAG, "全部应用.equals(itemText) " + "全部应用".equals(itemText) + "    -" + itemText + "--");
				if ("我的应用".equals(itemText)) {
					showAllAppPage(true);
					return;
				}
			}
			if(view instanceof CoverflowItem){
				LauncherLog.log_D(TAG, " view instanceof CoverflowItem");
				CoverflowItem item = (CoverflowItem)view;
				Intent appIntent = item.appIntent;
				LauncherLog.log_D(TAG, "item intent is " + appIntent);
				if(appIntent != null){
					try {
						startActivity(appIntent);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
				
		}
	};

	private OnItemSelectedListener mMenuSelectedLis = new OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

			if (mHandler.hasMessages(1))
				mHandler.removeMessages(1);

			Message msg = new Message();
			msg.what = 1;
			msg.obj = new MessageBean(view, position);
			mHandler.sendMessageDelayed(msg, 400);
		}

		public void onNothingSelected(AdapterView<?> parent) {

		}
	};

	@Override
	public boolean setLoadOnResume() {
		return false;
	}

	@Override
	public int getCurrentWorkspaceScreen() {
		return 0;
	}

	@Override
	public void startBinding() {
		LauncherLog.log_D(TAG, "----Launcher   startBinding()-");
	}

	@Override
	public void bindItems(ArrayList<ItemInfo> shortcuts, int start, int end) {

	}

	private HashMap<Long, FolderInfo> mFolders;

	private ArrayList<ShortcutInfo> getDropDownListData(long position) {

		LauncherLog.log_D(TAG, "getDropDownListData   position is " + position);
		FolderInfo folder = mFolders.get(position);
		return folder.getChilds();
	}

	@Override
	public void bindFolders(HashMap<Long, FolderInfo> folders) {
		LauncherLog.log_D(TAG, "----Launcher   bindFolders()-");
		mFolders = folders;
		mMenuAdapter.setFolders(folders);

		mLauncherMenu.setAdapter(mMenuAdapter);
		mLauncherMenu.setSelection(2 + 100000);
	}

	@Override
	public void finishBindingItems() {

	}

	@Override
	public void bindAllApplications(ArrayList<ApplicationInfo> apps) {
		LauncherLog.log_D(TAG, "----Launcher   bindAllApplications()-" + apps.size());

		mAllAppAdapter = new AllAppViewAdapter(this, apps);
		mAllAppGridview.setAdapter(mAllAppAdapter);
		mAllAppGridview.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

			}

			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		mAllAppGridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Object tag = view.getTag();
				if (tag != null && tag instanceof Intent) {
					try {
						if (mPlayService != null) {
							try {
								mPlayService.stop();
								mPlayService.uninit();
							} catch (RemoteException e) {
								e.printStackTrace();
							}
						}
						startActivity((Intent) tag);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	@Override
	public void bindAppsAdded(ArrayList<ApplicationInfo> apps) {
		LauncherLog.log_D(TAG, "----Launcher   bindAppsAdded()-" + apps.size());
	}

	@Override
	public void bindAppsUpdated(ArrayList<ApplicationInfo> apps) {

	}

	@Override
	public void bindAppsRemoved(ArrayList<ApplicationInfo> apps, boolean permanent) {

	}

	@Override
	public void bindPackagesUpdated() {

	}

	@Override
	public boolean isAllAppsVisible() {
		return false;
	}

	@Override
	public void bindSearchablesChanged() {

	}

	class MessageBean {
		View view;
		int position;

		MessageBean(View view, int position) {
			this.view = view;
			this.position = position;
		}
	}
}