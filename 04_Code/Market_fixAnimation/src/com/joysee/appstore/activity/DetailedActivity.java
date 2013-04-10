package com.joysee.appstore.activity;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.joysee.appstore.R;
import com.joysee.appstore.animation.AnimUtils;
import com.joysee.appstore.animation.MoveControl;
import com.joysee.appstore.common.ApplicationBean;
import com.joysee.appstore.common.AppsBean;
import com.joysee.appstore.common.Constants;
import com.joysee.appstore.common.DataOperate;
import com.joysee.appstore.common.PageBean;
import com.joysee.appstore.common.TaskBean;
import com.joysee.appstore.db.DBUtils;
import com.joysee.appstore.parser.DetailedParser;
import com.joysee.appstore.service.DownloadService;
import com.joysee.appstore.service.DownloadService.ServiceBinder;
import com.joysee.appstore.thread.LoadAppThread;
import com.joysee.appstore.utils.AppLog;
import com.joysee.appstore.utils.CaCheManager;
import com.joysee.appstore.utils.NetUtil;
import com.joysee.appstore.utils.RequestParam;
import com.joysee.appstore.utils.Utils;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class DetailedActivity extends BaseActivity implements View.OnClickListener,View.OnFocusChangeListener{


    public static final String TAG = "com.joysee.appstore.activity.DetailedActivity";

	
    public static final int MSG_SHOW_APP_DETAIL=4;//显示一个应用的详情
    public static final int MSG_START_DOWNLOAD = 5;
    public static final int MSG_NET_NOT_CONNECT=6;//网络连接不通
    public static final int MSG_SD_ERROR=7;//sd卡错误
    public static final int MSG_DOWN_APK = 8;//下载
    public static final int MSG_SHOW_DOWNING=11;
    public static final int MSG_SHOW_INSTALL=13;//当安装完成，要改变文字
    public static final int MSG_FOCUS = 19;
    public static final int MSG_SCORE_INFO = 20;
    public static final int MSG_DOWNLOAD_ERROR = 23;

	private static final int MSG_TURN_FAVORITES_IAMGE_YES = 20;//改变收蔵状态  （已收蔵）
	private static final int MSG_TURN_FAVORITES_IAMGE_NO = 21;//改变收蔵状态    (未收蔵)

	protected static final int MSG_SCOR_TRUE = 22; //获取评分状态回来后，发此消息给主线程去显示评价按钮
	private static final int MSG_REFRESH_APP_SCORE = 24; //刷新评分
	private static final int MSG_REFRESH_WATI_TIME = 25; //延时刷新
	private static final int MSG_PAUSE_CONTINUE=26;//暂停／继续
	private static final int MSG_CHANGE_STATUS_PROGRESS=27;
	
	private int requestCode = 1;

    private ViewFlipper mViewFlipper;
    private GridLayout mLayout;
    private LayoutInflater mLayoutInflater;
    private ViewGroup parent;
    
    private ImageView upButton;
    private ImageView downButton;
    private ImageView imageView;
    private TextView mRunButton,mUpdateButton,progressBarText;
    private TextView downInstallButton;
    private TextView mEvaluate,mFavorites;
    private ProgressBar mProgressbar;
    
    private TextView mDownRecord;
    private TextView mSearch;
    private RelativeLayout mRunAndUpdate,downinstallLayout,progressLayout;
    private TextView nameView; 
    private String action_type=RequestParam.Action.GETRECOMMENDLIST;//当前请求类型url,默认为推荐应用
    private int app_id;
    private PageBean mPageBean;
    private AppsBean appsBean; 
    private DataOperate mDataOperate;
    private DBUtils tDBUtils ;
    private NetUtil mNetUtil;
    private boolean trunDownOrup = true;//true为向下翻　　false为向上翻
    private boolean fristIn = true;//第一次进入祥情时为true
    private Boolean mHaveBeenEvaluated = false; 
    private Map<Boolean,Integer> mSroceMap; 
    private Integer mUserScore;
    private String mUserId = null; 
    private LoadAppThread mThread = null;
    
    private HandlerThread workThread;
    private Handler workHandler;
    private ServiceBinder downloadService;
    private AppReceiver mAppReceiver = null;
    private SharedPreferences sp;
    private int viewGroupFocus = -100;
    private boolean isPage=false;//表示正在翻页,加载数据
    
    private LoadDetailedTask mLoadDetailedTask = null;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppLog.d(TAG," onCreate ");
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.app_detailed_layout);
        
        parent = (ViewGroup)findViewById(R.id.detailed_relativelayout);
        viewGroupFocus = parent.getDescendantFocusability();
        parent.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        
        mPageBean=new PageBean();
        mPageBean.setPageSize(3);
        tDBUtils = new DBUtils(DetailedActivity.this);
        mNetUtil = new NetUtil(DetailedActivity.this);
        mDataOperate=new DataOperate(DetailedActivity.this);
        sp = getSharedPreferences("user_favorites",Context.MODE_PRIVATE);//暂时放用户收蔵
        setupViews();
        Intent intentSer = new Intent("com.joysee.appstore.service.DownloadService");
        bindService(intentSer,mServiceConnection, Context.BIND_AUTO_CREATE);
        initWorkThread();
        if(!workThread.isAlive()){
            workThread.start();
        }
        processExtraData();
        IntentFilter filter=new IntentFilter();
        filter.addAction(Constants.INTENT_DOWNLOAD_STARTED);
        filter.addAction(Constants.INTENT_DOWNLOAD_ERROR);
        filter.addAction(Constants.INTENT_DOWNLOAD_PROGRESS);
        filter.addAction(Constants.INTENT_INSTALL_COMPLETED);
        filter.addAction(Constants.INTENT_UNINSTALL_COMPLETED);
        filter.addAction(Constants.INTENT_DOWNLOAD_DETLET);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Constants.INTENT_ACTION_UPDATESCORE_SUCCESS);
        filter.addAction(Constants.INTENT_INSTALL_FAIL);
        filter.addAction(Constants.INTENT_DOWNLOAD_PAUSE);
        filter.addAction(Constants.INTENT_UPDATE_COMPLETED);
        mAppReceiver=new AppReceiver();
        this.registerReceiver(mAppReceiver, filter);
    }
    
    public void setupViews(){
    	mLayout = new GridLayout(this);
    	mImageView = (ImageView) this.findViewById(R.id.animation_imageview);
    	control=new MoveControl(this,mImageView);
        mViewFlipper = (ViewFlipper) this.findViewById(R.id.app_detailed_viewfillper);
        mSroceMap = new HashMap<Boolean, Integer>();
        imageView=(ImageView)findViewById(R.id.app_image);//单元格imageview
        upButton=(ImageView)findViewById(R.id.up_image);
        downButton=(ImageView)findViewById(R.id.down_imgae);
        nameView=(TextView)findViewById(R.id.app_name);
        mRunAndUpdate=(RelativeLayout)findViewById(R.id.run_update);
        downInstallButton=(TextView)findViewById(R.id.down_install);
        mDownRecord=(TextView)findViewById(R.id.top_down_record);
        mRunButton=(TextView)findViewById(R.id.run_button);
        mUpdateButton=(TextView)findViewById(R.id.update_button);
        mEvaluate=(TextView)findViewById(R.id.evaluate);
        mFavorites=(TextView)findViewById(R.id.favorites);
        mProgressbar = (ProgressBar) findViewById(R.id.progressbar);
        mSearch=(TextView) findViewById(R.id.top_search);
        progressBarText = (TextView) findViewById(R.id.down_progress_text);
        downinstallLayout=(RelativeLayout)this.findViewById(R.id.downinstallLayout);
        progressLayout=(RelativeLayout)this.findViewById(R.id.progressLayout);
        
        mRunButton.setNextFocusUpId(mSearch.getId());
        mRunButton.setNextFocusDownId(mEvaluate.getId());
        
        mUpdateButton.setNextFocusUpId(mSearch.getId());
        mUpdateButton.setNextFocusDownId(mEvaluate.getId());
        
        progressLayout.setNextFocusDownId(mEvaluate.getId());
        
        downinstallLayout.setOnClickListener(this);
        mDownRecord.setOnClickListener(this);
        mRunButton.setOnClickListener(this);
        mUpdateButton.setOnClickListener(this);
        mEvaluate.setOnClickListener(this);
        mEvaluate.setOnFocusChangeListener(this);
        mFavorites.setOnClickListener(this);
        mSearch.setOnClickListener(this);
        progressLayout.setOnClickListener(this);
        mSearch.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(event.getAction()==KeyEvent.ACTION_DOWN&&keyCode==KeyEvent.KEYCODE_DPAD_LEFT){
					if(downinstallLayout.getVisibility()==View.VISIBLE){
						downinstallLayout.setFocusable(true);
						downinstallLayout.requestFocus();
					}else if(progressLayout.getVisibility()==View.VISIBLE){
						progressLayout.setFocusable(true);
						progressLayout.requestFocus();
					}
				}
				return false;
			}
		});
        
        //up
        mRunButton.setNextFocusUpId(mSearch.getId());
        mUpdateButton.setNextFocusUpId(mSearch.getId());
        downinstallLayout.setNextFocusUpId(mSearch.getId());
        
        //down
        mDownRecord.setNextFocusDownId(mViewFlipper.getChildCount());
        mRunButton.setNextFocusDownId(mEvaluate.getId());
        mUpdateButton.setNextFocusDownId(mEvaluate.getId());
        downinstallLayout.setNextFocusDownId(mEvaluate.getId());
        
        //right
        mRunButton.setNextFocusRightId(mLayout.getId());
        mUpdateButton.setNextFocusRightId(mLayout.getId());
        downinstallLayout.setNextFocusRightId(mLayout.getId());
        
//        if(sp.getBoolean("isFavorite", false)){ 
//        	mFavorites.setBackgroundResource(0);
//        	Editor editor = sp.edit();
//        	editor.putBoolean("isFavorite", true); 
//        	editor.commit();
//        }else if(!sp.getBoolean("isFavorite", false)){ 
//        	mFavorites.setBackgroundResource(0);
//        	Editor editor = sp.edit();
//        	editor.putBoolean("isFavorite", false);  
//        	editor.commit();
//        }
        downinstallLayout.setTag("downinstallLayout");
        downinstallLayout.setOnFocusChangeListener(this);
        mUpdateButton.setOnFocusChangeListener(this);
        mDownRecord.setOnFocusChangeListener(this);
        mRunButton.setOnFocusChangeListener(this);
        mSearch.setOnFocusChangeListener(this);
        mEvaluate.setOnFocusChangeListener(this);
        mFavorites.setOnFocusChangeListener(this);
        progressLayout.setOnFocusChangeListener(this);
    }
    
    public void initWorkThread(){
    	workThread=new HandlerThread("handler_thread");
        workThread.start();
        workHandler=new Handler(workThread.getLooper()){
            public void handleMessage(Message msg) {
                switch(msg.what){
                case MSG_DOWN_APK:
                	if(appsBean==null){
                		AppLog.d(TAG, "----appsbean is null---");
                		return;
                	}
        			int status = tDBUtils.queryStatusByPkgName(appsBean.getPkgName());
        			String versionNet=tDBUtils.queryVersionByPkgName(appsBean.getPkgName());
                    AppLog.d(TAG, "-------------native version="+versionNet+";service version="+appsBean.getVersion());
        			AppLog.d(TAG, "---------MSG_DOWN_APK------status="+status);
        			switch(status){
        			case Constants.APP_STATUS_UNDOWNLOAD:
        				downApp(false);
        				break;
        			case Constants.APP_STATUS_INSTALLED:
        				if(Utils.CompareVersion(appsBean.getVersion(), versionNet)){//update
        					downApp(true);
        				}else{
        					runApp();
        				}
        				break;
        		    default:
        				break;
        			}
                    break;
                case MSG_SCORE_INFO:
                	if(mUserId != null){
                		AppLog.d(TAG, "--------mUser---------");
                		mSroceMap = mDataOperate.getScorFormService(mUserId, appsBean.getID()); 
                		if(mSroceMap!=null){
                			if(mSroceMap.containsKey(true)){ 
                				mHaveBeenEvaluated = true;
                				mUserScore = mSroceMap.get(true);
                				mainHandler.sendEmptyMessage(MSG_SCOR_TRUE);
                				AppLog.d(TAG, "-----------------mSroceMap.containsKey(true)--------------");
                			}else if(mSroceMap.containsKey(false)){ 
                				mHaveBeenEvaluated = false;
                				mainHandler.sendEmptyMessage(MSG_SCOR_TRUE);
                			}
                		}
                		AppLog.d(TAG, "--------------getScorFormService, appsBean.getID() = ;"+appsBean.getID()+" ;--------------");
                	}
                	break;
                }
            }
        };
    }
    
    public Handler mainHandler=new Handler(){
        public void handleMessage(Message msg) {
//        	Editor editor = sp.edit(); 
        	parent.setDescendantFocusability(viewGroupFocus);
            switch(msg.what){
            case MSG_SHOW_APP_DETAIL:
            	stopProgressDialog();
            	if(appsBean==null){//网络问题
            		startNetErrorDialog();
            		return;
            	}
                ((TextView)findViewById(R.id.app_comment)).setText(DetailedActivity.this.getString(R.string.app_comment,appsBean.getVoteNum()));
                ((TextView)findViewById(R.id.app_size)).setText(DetailedActivity.this.getString(R.string.app_size,Utils.transformByte(appsBean.getSize())));
                ((TextView)findViewById(R.id.app_verson)).setText(DetailedActivity.this.getString(R.string.app_verson,appsBean.getVersionName()));
                ((TextView)findViewById(R.id.app_update)).setText(DetailedActivity.this.getString(R.string.app_update_time,appsBean.getCreateTime()));
                ((TextView)findViewById(R.id.app_lable)).setText(appsBean.getTypeName());
                ((TextView)findViewById(R.id.app_remark)).setText(appsBean.getRemark()+"");
                findViewById(R.id.score_tex).setVisibility(View.VISIBLE);
                findViewById(R.id.score_unit).setVisibility(View.VISIBLE);
                AppLog.d(TAG, "------;appsBean.getTypeName()="+appsBean.getTypeName());
                if(appsBean.getNatImageUrl()==null||appsBean.getNatImageUrl().trim().equals("")){
                	AppLog.d(TAG, "***********image null -------------");
                	imageView.setImageResource(R.drawable.detailed_default_img);
                }else{
                	Bitmap bm = CaCheManager.requestBitmap(appsBean.getNatImageUrl());
                	if(null!=bm){
                		imageView.setImageBitmap(bm);
                	}else{
                		imageView.setImageResource(R.drawable.detailed_default_img);
                	}
                }
                nameView.setText(appsBean.getAppName()+"");
                nameView.setSelected(true);
                ((TextView)findViewById(R.id.top_left)).setText(DetailedActivity.this.getString(R.string.app_name_type,appsBean.getTypeName()));
                ((TextView)findViewById(R.id.rating_bar)).setText(appsBean.getScore());
                int appStatus = tDBUtils.queryStatusByPkgName(appsBean.getPkgName());
                AppLog.d(TAG, "----------------------------appStatus="+appStatus+";appName="+appsBean.getAppName()+";pkgName="+appsBean.getPkgName());
                mRunAndUpdate.setVisibility(View.GONE);
            	mRunButton.setVisibility(View.GONE);
            	mUpdateButton.setVisibility(View.GONE);
                switch(appStatus){
                    case Constants.DOWNLOAD_STATUS_ERROR:
                    case Constants.APP_STATUS_DOWNLOADING:
                    case Constants.DOWNLOAD_STATUS_EXIT_STOP:
                    case Constants.APP_STATUS_UPDATE:
                    case Constants.DOWNLOAD_STATUS_PAUSE:
                    	int appSts=tDBUtils.queryStatusByPkgNameAndVersion(appsBean.getPkgName(), appsBean.getVersion());
                    	AppLog.d(TAG, "-----------------------appSts="+appSts);
                    	switch(appSts){
	                    	case Constants.DOWNLOAD_STATUS_ERROR:
	                        case Constants.APP_STATUS_DOWNLOADING:
	                        case Constants.DOWNLOAD_STATUS_EXIT_STOP:
	                        case Constants.APP_STATUS_UPDATE:
	                        case Constants.DOWNLOAD_STATUS_PAUSE:
	                        	progressLayout.setFocusable(true);
	                        	int sumSize=tDBUtils.querySumSizeByPkgName(appsBean.getPkgName());
	                        	int downSize=tDBUtils.queryDowningSizeByPkgName(appsBean.getPkgName());
	                        	AppLog.d(TAG, "------------------------------------downSize="+downSize+";sumSize="+sumSize);
	                        	if(sumSize==0){
	                        		mProgressbar.setProgress(100);
	                        	}else{
	                        		long down=downSize;
	                            	long sum=sumSize;
	                            	int res=(int)(down*100/sum);
	                            	mProgressbar.setProgress(res);
	                        	}
	                        	if(appStatus==Constants.DOWNLOAD_STATUS_ERROR){
	                        		progressBarText.setText(DetailedActivity.this.getString(R.string.download_error));
	                        	}else if(appStatus==Constants.DOWNLOAD_STATUS_PAUSE){
	                        		progressBarText.setText(DetailedActivity.this.getString(R.string.pause_download));
	                        	}else {
	                        		progressBarText.setText(DetailedActivity.this.getString(R.string.download_tab_2));
	                        	}
	                        	downinstallLayout.setVisibility(View.GONE);
	                        	progressLayout.setVisibility(View.VISIBLE);
	                        	progressLayout.setFocusable(true);
	                        	progressLayout.setFocusableInTouchMode(true);
	                        	progressLayout.setAlpha(AnimUtils.VIEW_ALPHA_FLAG);
	                            progressLayout.requestFocus();
	                            AppLog.d(TAG, "-------------break----------");
	                            break;
	                        case Constants.APP_STATUS_INSTALLED: 
	                            String versionNet=tDBUtils.queryVersionByPkgName(appsBean.getPkgName());
	                            AppLog.d(TAG, "-------------native version="+versionNet+";service version="+appsBean.getVersion());
	                            if(Utils.CompareVersion(appsBean.getVersion(), versionNet)){//need update
	                            	progressLayout.setVisibility(View.GONE);
		                            downinstallLayout.setVisibility(View.VISIBLE);
		                            downinstallLayout.setFocusable(true);
		                            downinstallLayout.requestFocus();
	                                downInstallButton.setText(DetailedActivity.this.getString(R.string.update));
	                            }else{
	                            	progressLayout.setVisibility(View.GONE);
	                            	downinstallLayout.setVisibility(View.VISIBLE);
	                            	downinstallLayout.setFocusable(true);
	                                downInstallButton.setText(DetailedActivity.this.getString(R.string.run_app));
	                            }
	                            
	                            downinstallLayout.setFocusable(true);
	                            downinstallLayout.setAlpha(AnimUtils.VIEW_ALPHA_FLAG);
	                            downinstallLayout.setFocusableInTouchMode(true);
	                            downinstallLayout.requestFocus();
	                            
//	                            Message fousMes3=mainHandler.obtainMessage();
//	                            fousMes3.obj=downInstallButton;
//	                            fousMes3.what=MSG_FOCUS;
//	                            mainHandler.sendMessageDelayed(fousMes3, 100);
	                            break;
	                        default:
	                        	AppLog.d(TAG, "-------------"+appsBean.getPrice());
	                        	progressLayout.setVisibility(View.GONE);
	                        	downinstallLayout.setVisibility(View.VISIBLE);
	                        	if(appsBean.getPrice()>0){
	                        		downInstallButton.setText(DetailedActivity.this.getString(R.string.charge_down_install,String.valueOf(appsBean.getPrice())));
	                        	}else{
	                        		downInstallButton.setText(DetailedActivity.this.getString(R.string.down_install));
	                        	}
	                        	downinstallLayout.setFocusable(true);
	                        	downinstallLayout.setAlpha(AnimUtils.VIEW_ALPHA_FLAG);
	                        	downinstallLayout.setFocusableInTouchMode(true);
	                        	downinstallLayout.requestFocus();
	                        	
//	                            Message fousMes4=mainHandler.obtainMessage();
//	                            fousMes4.obj=downInstallButton;
//	                            fousMes4.what=MSG_FOCUS;
//	                            mainHandler.sendMessageDelayed(fousMes4, 100);
	                            break;
                    	}
                        break;
                    case Constants.APP_STATUS_INSTALLED: 
                        String versionNet=tDBUtils.queryVersionByPkgName(appsBean.getPkgName());
                        AppLog.d(TAG, "-------------native version="+versionNet+";service version="+appsBean.getVersion());
                        if(Utils.CompareVersion(appsBean.getVersion(), versionNet)){//need update
                        	progressLayout.setVisibility(View.GONE);
                        	downinstallLayout.setVisibility(View.VISIBLE);
                            downInstallButton.setText(DetailedActivity.this.getString(R.string.update));
                        }else{
                        	progressLayout.setVisibility(View.GONE);
                        	downinstallLayout.setVisibility(View.VISIBLE);
                            downInstallButton.setText(DetailedActivity.this.getString(R.string.run_app));
                        }
                        downinstallLayout.setFocusable(true);
                        downinstallLayout.setAlpha(AnimUtils.VIEW_ALPHA_FLAG);
                        downinstallLayout.setFocusableInTouchMode(true);
                        downinstallLayout.requestFocus();
//                        Message fousMes3=mainHandler.obtainMessage();
//                        fousMes3.obj=downInstallButton;
//                        fousMes3.what=MSG_FOCUS;
//                        mainHandler.sendMessageDelayed(fousMes3, 100);
                        break;
                    default:
                    	AppLog.d(TAG, "------1111-------"+appsBean.getPrice());
                    	progressLayout.setVisibility(View.GONE);
                    	downinstallLayout.setVisibility(View.VISIBLE);
                    	if(appsBean.getPrice()>0){
                    		downInstallButton.setText(DetailedActivity.this.getString(R.string.charge_down_install,String.valueOf(appsBean.getPrice())));
                    	}else{
                    		downInstallButton.setText(DetailedActivity.this.getString(R.string.down_install));
                    	}
                    	downinstallLayout.setFocusable(true);
                    	downinstallLayout.setAlpha(AnimUtils.VIEW_ALPHA_FLAG);
                    	downinstallLayout.setFocusableInTouchMode(true);
                    	downinstallLayout.requestFocus();
//                        Message fousMes4=mainHandler.obtainMessage();
//                        fousMes4.obj=downInstallButton;
//                        fousMes4.what=MSG_FOCUS;
//                        mainHandler.sendMessageDelayed(fousMes4, 100);
                        break;
                }
                
                break;
            case MSG_FOCUS:
            	
            	break;
            case MSG_START_DOWNLOAD:
            	mSearch.setNextFocusLeftId(progressLayout.getId());
            	parent.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            	control.hideMoveView();
            	mRunAndUpdate.setVisibility(View.GONE);
            	mRunButton.setVisibility(View.GONE);
            	mUpdateButton.setVisibility(View.GONE);
            	downinstallLayout.setVisibility(View.GONE);
            	progressLayout.setVisibility(View.VISIBLE);
            	parent.setDescendantFocusability(viewGroupFocus);
                mProgressbar.setProgress(0);
                progressLayout.setAlpha(AnimUtils.VIEW_ALPHA_FLAG);
                progressLayout.setFocusableInTouchMode(true);
                progressLayout.requestFocus();
//                Message fousMes0=mainHandler.obtainMessage();
//                fousMes0.obj=mProgressLay;
//                fousMes0.what=MSG_FOCUS;
//                mainHandler.sendMessageDelayed(fousMes0, 50);
                break;
            case MSG_NET_NOT_CONNECT:
                break;
            case MSG_SD_ERROR:
                Utils.showTipToast(Gravity.CENTER,DetailedActivity.this, DetailedActivity.this.getString(R.string.chcekSdcard));
                break;
            case MSG_SHOW_DOWNING:
                AppLog.d(TAG, "-----------MSG_SHOW_DOWNING-msg.arg1="+msg.arg1+";msg.arg2="+msg.arg2+";value="+(msg.arg1*100/msg.arg2));
                if(progressLayout.getVisibility()==View.VISIBLE){
                	progressBarText.setText(DetailedActivity.this.getString(R.string.download_tab_2));
                	long down=msg.arg1;
                	long sum=msg.arg2;
                	int res=(int)(down*100/sum);
                	mProgressbar.setProgress(res);
                }else{
                }
                break;
            case MSG_DOWNLOAD_ERROR:
            	if(progressLayout.getVisibility()==View.VISIBLE){
                }
            	break;
            case MSG_SHOW_INSTALL:
            	mSearch.setNextFocusLeftId(mRunAndUpdate.getId());
            	AppLog.d(TAG, "$$$$$$$$$$$$$$$$$$$$$msg             MSG_SHOW_INSTALL");
            	if(isActive){
            		parent.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
                	control.hideMoveView();
            	}
            	mRunAndUpdate.setVisibility(View.GONE);
            	mRunButton.setVisibility(View.GONE);
            	mUpdateButton.setVisibility(View.GONE);
            	progressLayout.setVisibility(View.GONE);
                String versionNet=tDBUtils.queryVersionByPkgName(appsBean.getPkgName());
                if(Utils.CompareVersion(appsBean.getVersion(), versionNet)){
                	downInstallButton.setText(DetailedActivity.this.getString(R.string.update));
                }else{
                	downInstallButton.setText(DetailedActivity.this.getString(R.string.run_app));
                }
                downinstallLayout.setVisibility(View.VISIBLE);
                if(isActive){
                	parent.setDescendantFocusability(viewGroupFocus);
                    downinstallLayout.setFocusable(true);
                    downinstallLayout.setFocusableInTouchMode(true);
                    downinstallLayout.setAlpha(AnimUtils.VIEW_ALPHA_FLAG);
                    downinstallLayout.requestFocus();
                    control.startAnimation();
                }
//                Message fousMes=mainHandler.obtainMessage();
//                fousMes.obj=downInstallButton;
//                fousMes.what=MSG_FOCUS;
//                mainHandler.sendMessageDelayed(fousMes, 50);
                ApplicationBean appBean=tDBUtils.queryApplicationByPkgName((String)msg.obj);
                if(appBean==null){
                	return;
                }
            	for(int i=0;i<mLayout.getChildCount();i++){
            		View viewM=mLayout.getChildAt(i);
            		if(viewM.getVisibility()==View.VISIBLE){
            			AppsBean appsM=(AppsBean)viewM.getTag();
//            			AppLog.d(TAG, "pkgM="+appsM.getPkgName()+";pkgBean"+appBean.getPkgName()+"appsM.vserion="+appsM.getVersion()+";appBean.version="+appBean.getVersion());
            			if(appsM!=null&&appBean.getPkgName().equals(appsM.getPkgName())&&Utils.CompareVersion(appsM.getVersion(),appBean.getVersion())){
            				ImageView popIv = (ImageView) viewM.findViewById(R.id.detail_item_pop);
            				ImageView popTvT = (ImageView) viewM.findViewById(R.id.pop_update);
            				popIv.setVisibility(View.VISIBLE);
            				popTvT.setVisibility(View.VISIBLE);
            			}
            		}
            	}
                break;
                 
                
            case MSG_TURN_FAVORITES_IAMGE_YES:
//    			editor.putBoolean("isFavorite", false);
//    			editor.commit();
            	break;
            case MSG_TURN_FAVORITES_IAMGE_NO:
//    			editor.putBoolean("isFavorite", true);
//    			editor.commit();
            	break;
            case MSG_SCOR_TRUE:
            	mEvaluate.setVisibility(View.VISIBLE);
            	//mFavorites.setVisibility(View.VISIBLE);
            	break;
            case MSG_REFRESH_WATI_TIME:
	            //在评价窗口退出后，在此执行评论人数，评分值刷新
	    		if(appsBean!=null&&appsBean.getID()!=-1){
	    			new RefreshScoreTask().execute(appsBean.getID());
	    		}
	    		break;
            case MSG_REFRESH_APP_SCORE: //刷新评分
            	((TextView)findViewById(R.id.app_comment)).setText(DetailedActivity.this.getString(R.string.app_comment,appsBean.getVoteNum()));
            	((TextView)findViewById(R.id.rating_bar)).setText(appsBean.getScore());
            	AppLog.d(TAG, "-----------------now is refresh the score's state of affairs");
            	break;
            case MSG_PAUSE_CONTINUE:
            	TaskBean pauseTask=tDBUtils.queryTaskByPkgName(appsBean.getPkgName());
            	if(pauseTask==null||pauseTask.getPkgName()==null){
            		return;
            	}
				AppLog.d(TAG, "-----------mHandler-------MSG_PAUSE_CONTINUE-------status="+pauseTask.getStatus()+";name="+pauseTask.getAppName());
				if(pauseTask.getStatus()==Constants.DOWNLOAD_STATUS_EXECUTE){
					downloadService.pauseTask(pauseTask);
//					mProgressLay.setText(DetailedActivity.this.getString(R.string.pause_download));
				}else if(pauseTask.getStatus()==Constants.DOWNLOAD_STATUS_PAUSE){
					AppLog.d(TAG, "------downloadDir : ---"+pauseTask.getDownloadDir());
					if(!pauseTask.getDownloadDir().equals(Constants.APKROOT)){
						if(!CaCheManager.checkSDcard()){
							AppLog.d(TAG, "----------no SDCard-----------");
							Utils.showTipToast(Gravity.CENTER, DetailedActivity.this, DetailedActivity.this.getString(R.string.data_missing));
							return;
						}
						AppLog.d(TAG, "---------have SDCard----------");
					}
					downloadService.continueTask(pauseTask);
//					mProgressLay.setText(DetailedActivity.this.getString(R.string.download_tab_2));
				}
            	break;
            case MSG_CHANGE_STATUS_PROGRESS:
            	TaskBean task=tDBUtils.queryTaskByPkgName((String)msg.obj);
            	if(task==null){
            		return;
            	}
				AppLog.d(TAG, "-----------mHandler-------MSG_CHANGE_STATUS_PROGRESS-------status="+task.getStatus()+";name="+task.getAppName());
				if(task.getStatus()==Constants.DOWNLOAD_STATUS_EXECUTE){
					progressBarText.setText(DetailedActivity.this.getString(R.string.download_tab_2));
				}else if(task.getStatus()==Constants.DOWNLOAD_STATUS_PAUSE){
					progressBarText.setText(DetailedActivity.this.getString(R.string.pause_download));
				}
            	break;
            }
            super.handleMessage(msg);
        }
    };
    
    
    public void initViewFlipper(){
      	mLayoutInflater = this.getLayoutInflater();
      	mLayout.setColumnCount(1);
      	mLayout.setRowCount(3);
           for(int i=0;i<3;i++){
	   	       View view = mLayoutInflater.inflate(R.layout.detailed_item_layout, null);
	   	       ImageView button = (ImageView) view.findViewById(R.id.item_img);
	   	       button.setOnClickListener(this);
	   	       if(i==0){
 	     		   view.setNextFocusUpId(mDownRecord.getId());
 	     		   button.setOnKeyListener(new View.OnKeyListener() {
 					  public boolean onKey(View v, int keyCode, KeyEvent event) {
 						 if(event.getAction()==KeyEvent.ACTION_DOWN&&keyCode==KeyEvent.KEYCODE_DPAD_LEFT){
 							setLeftFocus();
 							return true;
 						 }
 						 return false;
 					  }
 				  });
	   	       }else if(i==2){
	     		   button.setOnKeyListener(new View.OnKeyListener() {
					  public boolean onKey(View v, int keyCode, KeyEvent event) {

						 return false;
					  }
				   });
	     	   }
   	     	   mLayout.addView(view);
           }
           mViewFlipper.addView(mLayout);
    }
    
    @Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if(event.getAction()==KeyEvent.ACTION_DOWN){
    		if(event.getKeyCode()==KeyEvent.KEYCODE_DPAD_DOWN||event.getKeyCode()==KeyEvent.KEYCODE_DPAD_LEFT||
    				event.getKeyCode()==KeyEvent.KEYCODE_DPAD_RIGHT||event.getKeyCode()==KeyEvent.KEYCODE_DPAD_UP){
    			downTimes++;
    			requestFocusFirst(mSearch);
    			if(downTimes>0){
            		switch(event.getKeyCode()){
            		case KeyEvent.KEYCODE_DPAD_DOWN:
            			mMoveDirect=View.FOCUS_DOWN;
            			break;
            		case KeyEvent.KEYCODE_DPAD_UP:
            			mMoveDirect=View.FOCUS_UP;
            			break;
            		case KeyEvent.KEYCODE_DPAD_LEFT:
            			mMoveDirect=View.FOCUS_LEFT;
            			break;
            		case KeyEvent.KEYCODE_DPAD_RIGHT:
            			mMoveDirect=View.FOCUS_RIGHT;
            			break;
            		}
            		if(control.mDelay){//表示正在翻页中,由于翻页有延迟
            			return true;
            		}
            		View nextview=mFocusF.findNextFocus(parent, this.getCurrentFocus(),mMoveDirect);
            		AppLog.d(TAG, "----nextview-"+nextview);
            		if(mViewFlipper!=null&&mViewFlipper.getChildCount()>0){
            			if(nextview!=null){
//            				AppLog.d(TAG, "getCurrentFocus().getTag()="+getCurrentFocus().getTag()+";mMoveDirect="+mMoveDirect+";control.focusQueue.size()="+control.focusQueue.size()+";isPage="+isPage);
                			if(this.getCurrentFocus()!=null&&AppsBean.class.isInstance(this.getCurrentFocus().getTag())&&TextView.class.isInstance(nextview)){//向上
                				if(mMoveDirect==View.FOCUS_UP){
                					if(control.focusQueue.size()==0&&!isPage){
                						if(mPageBean.getPageNo()>1){
                							this.previousPage();
                							return true;
                						}
                					}else{
                    					return true;
                    				}
                				}else if(mMoveDirect==View.FOCUS_DOWN){
                					if(control.focusQueue.size()==0&&!isPage){
                						if(mPageBean.getPageNo()<mPageBean.getPageTotal()){
                							this.nextPage();
                							return true;
                						}
                					}else{
                    					return true;
                    				}
                				}
                			}
                		}
            		}
            	}
    		}
    	}else if(event.getAction()==KeyEvent.ACTION_UP){
    		downTimes=0;
    	}
		return super.dispatchKeyEvent(event);
	}
    
    
    public void refreshAppsList(List<AppsBean> appList){
        if(appList==null){//网络有误
        	startNetErrorDialog();
        	isPage=false;
    		return;
    	}
    	if(mViewFlipper.getChildCount()==0){
        	initViewFlipper();
        }
    	AppLog.d(TAG, "--------appList.size="+appList.size());
		for(int i=0;i<appList.size();i++){
			View view=mLayout.getChildAt(i);
			if(view==null){
				continue;
			}
			ImageView button=(ImageView)view.findViewById(R.id.item_img);
			button.setTag(appList.get(i));
			if(null==appList.get(i).getNatImageUrl()||appList.get(i).getNatImageUrl().equals("")){
				button.setImageResource(R.drawable.app_default_img);
			}else{
//				button.setImageURI(Uri.parse(appList.get(i).getNatImageUrl()));
				Bitmap bitmap = CaCheManager.requestBitmap(appList.get(i).getNatImageUrl());
				if(null!=bitmap){
					button.setImageBitmap(bitmap);
				}else{
					button.setImageResource(R.drawable.app_default_img);
				}
			}
			button.setOnFocusChangeListener(this);
			((TextView)view.findViewById(R.id.item_name)).setText(appList.get(i).getAppName());
			view.setVisibility(View.VISIBLE);
			view.setTag(appList.get(i));
			showPOP(appList, i, view);
		}
		if(appList.size()<3){
			for(int i=appList.size();i<3;i++){
				mLayout.getChildAt(i).setVisibility(View.GONE);
			}
			if(appList.size()==1){ //只有一个或两个时,设最后一个的向下焦点
				View lastView = mLayout.getChildAt(0).findViewById(R.id.item_img);
				lastView.setNextFocusDownId(mEvaluate.getId());
				mEvaluate.setNextFocusRightId(lastView.getId());
			}else{
				View lastView = mLayout.getChildAt(1).findViewById(R.id.item_img);
				lastView.setNextFocusDownId(mEvaluate.getId());
				mEvaluate.setNextFocusRightId(lastView.getId());
			}
		}
		/* 翻页焦点控制 */
		if(trunDownOrup && !fristIn){
    		View vDown=mLayout.getChildAt(0).findViewById(R.id.item_img);
    		vDown.setAlpha(AnimUtils.VIEW_ALPHA_FLAG);
    		vDown.setFocusableInTouchMode(true);
    		vDown.requestFocus();
    	}else if(!trunDownOrup && !fristIn){
    		View vUp=mLayout.getChildAt(2).findViewById(R.id.item_img);
    		vUp.setAlpha(AnimUtils.VIEW_ALPHA_FLAG);
    		vUp.setFocusableInTouchMode(true);
    		vUp.requestFocus();
    	}
		
		if(mPageBean.getPageNo()==1){
            upButton.setVisibility(View.INVISIBLE);
        }else{
            upButton.setVisibility(View.VISIBLE);
        }
        if(mPageBean.getPageNo()==mPageBean.getPageTotal()){
            downButton.setVisibility(View.INVISIBLE);
        }else{
            downButton.setVisibility(View.VISIBLE);
        }
        if(mPageBean.getPageNo()>1&&mPageBean.getPageNo()<mPageBean.getPageTotal()){
            upButton.setVisibility(View.VISIBLE);
            downButton.setVisibility(View.VISIBLE);
        }
        if(mPageBean.getPageTotal()<2){
            upButton.setVisibility(View.INVISIBLE);
            downButton.setVisibility(View.INVISIBLE);
        }
        fristIn = false;
        isPage=false;
	}

    /* show the app update POP */
	private void showPOP(List<AppsBean> appList, int i, View view) {
		ImageView popIv = (ImageView) view.findViewById(R.id.detail_item_pop);
		ImageView popTvT = (ImageView) view.findViewById(R.id.pop_update);
		int appStatus = tDBUtils.queryStatusByPkgName(appList.get(i).getPkgName());
		if(appStatus==Constants.APP_STATUS_INSTALLED||appStatus==Constants.APP_STATUS_UPDATE){
			String newVersion = appList.get(i).getVersion();
			String oldVersion = tDBUtils.queryVersionByPkgName(appList.get(i).getPkgName());
			AppLog.d(TAG, "-----------------newVersion :"+newVersion +"| -------oldVersion :" +oldVersion);
			if(Utils.CompareVersion(newVersion, oldVersion)){
				popIv.setVisibility(View.VISIBLE);
				popTvT.setVisibility(View.VISIBLE);
			}else{
				popIv.setVisibility(View.GONE);
				popTvT.setVisibility(View.GONE);
			}
		}else{
			popIv.setVisibility(View.GONE);
			popTvT.setVisibility(View.GONE);
		}
	}
    
    /* 下一页 */
    public void nextPage(){
    	if(mThread.isAlive()){
    		return;
    	}
        if(!mPageBean.nextPage()){
        	AppLog.d(TAG, "no nextPage");
//			mEvaluate.requestFocus();
            return;
        }
        isPage=true;
        AppLog.d(TAG, "-------nextPage");
        trunDownOrup = true;
        mThread=new LoadAppThread(DetailedActivity.this,mPageBean,action_type);
        mThread.start();
    }
    
    /* 上一页 */
    public void previousPage(){
    	if(mThread.isAlive()){
    		return;
    	}
        if(!mPageBean.previousPage()){//表示没有上一页
            return;
        }
        isPage=true;
        AppLog.d(TAG, "-------beforePage");
        trunDownOrup=false;
        mThread=new LoadAppThread(DetailedActivity.this,mPageBean,action_type);
        mThread.start();
    }
    
    /* 向左 */
    public void setLeftFocus(){
    	if(downinstallLayout.getVisibility()==View.VISIBLE){
    		downinstallLayout.requestFocus();
    	}else if(progressLayout.getVisibility()==View.VISIBLE){
    		progressLayout.requestFocus();
    	}
    }
    
    /* 启动App */
    public void runApp(){
    	ApplicationBean bean=tDBUtils.queryApplicationByPkgName(appsBean.getPkgName());
		Intent intent = new Intent();
		intent = getPackageManager().getLaunchIntentForPackage(bean.getPkgName());
		if(null==intent){
            Utils.showTipToast(Gravity.CENTER,DetailedActivity.this, DetailedActivity.this.getString(R.string.no_app));
		}else{
			AppLog.d(TAG, "-----"+intent.getPackage());
			startActivity(intent);
		}
    }
    
    public void downApp(boolean update){
    	AppLog.d(TAG, "----appsBean.getApkUrl()="+appsBean.getApkUrl());
    	if(appsBean.getPkgName()==null||appsBean.getPkgName().equals("")){
    		AppLog.d(TAG, "---------downApp----pkgname-is-null;id="+appsBean.getID());
            Utils.showTipToast(Gravity.CENTER,DetailedActivity.this, DetailedActivity.this.getString(R.string.no_pkgname));
    		return;
    	}
    	if(appsBean.getNatImageUrl()==null||appsBean.getNatImageUrl().trim().equals("")){
    		AppLog.d(TAG, "---------downApp----NatImageUrl-is-null;id="+appsBean.getID());
            Utils.showTipToast(Gravity.CENTER,DetailedActivity.this, DetailedActivity.this.getString(R.string.no_icon));
    		return;
    	}
    	try{
    		if(!Utils.checkConnect(appsBean.getApkUrl())){
                startNetErrorDialog();
                return;
            }
    	}catch(Exception e){
    		AppLog.d(TAG, "----error-----check Connect url="+appsBean.getApkUrl());
    	}
        
    	//change by sdcard or data/data
    	if(CaCheManager.checkSDcard()){
    		downToSDCard(update);
    	}else{
    		downToRAM(update);
    	}
    	
    }
    
    
    /*下载到data/data*/
    private void downToRAM(boolean update) {
		if(!new CaCheManager().isFreeRAMSpace(appsBean.getSize())){
			Utils.showTipToast(Gravity.CENTER,DetailedActivity.this, DetailedActivity.this.getString(R.string.ram_full));
			return;
		}
		File downDir = new File(Constants.APKROOT);
		AppLog.d(TAG, "-----------downDir : "+downDir.getAbsolutePath());
		appsBean.setDownloadDir(downDir.getAbsolutePath());
		if(downloadService!=null){
        	mainHandler.sendEmptyMessage(MSG_START_DOWNLOAD);
            downloadService.startDownload(appsBean);
            if(update){
            	DownloadRecordActivity.bitMap.remove(appsBean.getPkgName());
            	tDBUtils.updateAppStatusByPkgName(appsBean.getPkgName(),Constants.APP_STATUS_UPDATE);
            	AppLog.d(TAG, "-------------update "+appsBean.getAppName()+" status="+Constants.APP_STATUS_UPDATE);
            }
        }
	}

    
    /*下载到sdcard*/
	private void downToSDCard(boolean update) {
		//判断SD卡可用空间，小于要下载APK的大小，返回
        if(new CaCheManager().isFreeSpace(appsBean.getSize())){
        	Utils.showTipToast(Gravity.CENTER,DetailedActivity.this, DetailedActivity.this.getString(R.string.sdcard_full));
        	return;
        }
        File baseDir = Environment.getExternalStorageDirectory();
        File downDir = new File(baseDir, "joysee");
        AppLog.d(TAG, "***********************url="+appsBean.getSerImageUrl()+";fileSaveDir="+downDir.getAbsolutePath()+";appName="+appsBean.getAppName()+";iconUrl="+appsBean.getSerImageUrl());
        AppLog.d(TAG, "downDir="+ downDir.getAbsolutePath());
        appsBean.setDownloadDir(downDir.getAbsolutePath());
        if(downloadService!=null){
        	mainHandler.sendEmptyMessage(MSG_START_DOWNLOAD);
            downloadService.startDownload(appsBean);
            if(update){
            	DownloadRecordActivity.bitMap.remove(appsBean.getPkgName());
            	tDBUtils.updateAppStatusByPkgName(appsBean.getPkgName(),Constants.APP_STATUS_UPDATE);
            	AppLog.d(TAG, "-------------update "+appsBean.getAppName()+" status="+Constants.APP_STATUS_UPDATE);
            }
        }
	}

	
	/**
     * 进入下载管理 
     */
    public void downloadMgr() {
        Intent intent = new Intent();
        intent.setClassName(DetailedActivity.this, "com.joysee.appstore.activity.DownloadRecordActivity");
        startActivity(intent);
    }
    
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName name) {
            downloadService = null;
        }
        public void onServiceConnected(ComponentName name, IBinder service) {
        	AppLog.d(TAG, "================enter onServiceConnected====");
        	downloadService = ((DownloadService.ServiceBinder)service).getService();
        }
    };
    
    private class LoadDetailedTask extends AsyncTask<Object, Void, List<AppsBean>>{
		protected List<AppsBean> doInBackground(Object... params) {
			AppLog.d(TAG, "----------LoadDetailedTask-----doInBackground-------");
			String urlStr=RequestParam.getAppServerUrl(DetailedActivity.this)+RequestParam.Action.GETAPPDETAIL;
			Map<String,String> param=new TreeMap<String,String>();
			param.put(RequestParam.Param.APPID,String.valueOf(params[0]));
			appsBean=(AppsBean)mNetUtil.getNetData(param, urlStr, null, new DetailedParser(DetailedActivity.this));
			if(appsBean!=null&&!appsBean.getSerImageUrl().trim().equals("")){
				boolean res=NetUtil.loadImage(appsBean.getSerImageUrl(), appsBean.getNatImageUrl());
				if(!res){
					appsBean.setNatImageUrl(null);
				}
				//workHandler.sendEmptyMessage(MSG_SCORE_INFO);//请求APP是否评分		
			}
			return null;
		}
		@Override
		protected void onPostExecute(List<AppsBean> result) {
			mainHandler.sendEmptyMessage(MSG_SHOW_APP_DETAIL);
		}
		@Override
		protected void onCancelled(List<AppsBean> result) {
			super.onCancelled(result);
		}
		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
	}
    
    /**
     * 评论后刷新祥情里的评论值
     */
    private class RefreshScoreTask extends AsyncTask<Object, Void, List<AppsBean>>{
		protected List<AppsBean> doInBackground(Object... params) {
			AppLog.d(TAG, "----------RefreshCommentTask-----doInBackground-------");
			String urlStr=RequestParam.getAppServerUrl(DetailedActivity.this)+RequestParam.Action.GETAPPDETAIL;
			Map<String,String> param=new TreeMap<String,String>();
			param.put(RequestParam.Param.APPID,String.valueOf(params[0]));
			appsBean=(AppsBean)mNetUtil.getNetData(param, urlStr, null, new DetailedParser(DetailedActivity.this));
			return null;
		}
		
		//发消息刷新评分值和评论人数
		protected void onPostExecute(List<AppsBean> result) {
			if(appsBean!=null && appsBean.getScore()!=mUserScore+""){ // 如果后台返回的最新评分没改变，则不去刷新当前页面
				mainHandler.sendEmptyMessage(MSG_REFRESH_APP_SCORE);
			}
			super.onPostExecute(result);
		}
    }
    
    protected void onResume() {
        super.onResume();
        AppLog.d(TAG," onResume ");
        RelativeLayout mRootLayout = (RelativeLayout)findViewById(R.id.detailed_relativelayout);
		Drawable bg = getThemePaper();
        if (bg != null) {
            mRootLayout.setBackgroundDrawable(bg);
        } else {
            AppLog.d("getThemePaper() is null");
        }
    }
    
    @Override
    protected void onStop(){
    	super.onStop();
    	AppLog.d(TAG, "-----------onPause-------");
		if(mLoadDetailedTask!=null&&mLoadDetailedTask.getStatus()!=LoadDetailedTask.Status.FINISHED){
//        	mLoadDetailedTask.cancel(true);
//        	mLoadDetailedTask = null;
        }
    }
    
    protected void onDestroy(){
    	super.onDestroy();
    	AppLog.d(TAG, "-----------onDestory-------");
    	this.unbindService(mServiceConnection);
    	if(mAppReceiver!=null){
    		this.unregisterReceiver(mAppReceiver);
    	}
    }
    
    @Override
	protected void onNewIntent(Intent intent) {
    	AppLog.d(TAG, "---------onNewIntent()---------intent"+intent.getIntExtra("app_id",-1));
		setIntent(intent);
		processExtraData();
	}

    private void processExtraData(){
    	Intent intent =getIntent();
    	action_type=intent.getStringExtra("action_type");
    	if(action_type==null||action_type.trim().equals("")){
    		action_type=RequestParam.Action.GETRECOMMENDLIST;
    	}
    	int typeId=intent.getIntExtra("type_id", -1);
    	mPageBean.setTypeId(typeId); 
        AppLog.d(TAG, "------before----app_id"+app_id);
        app_id=intent.getIntExtra("app_id",-1);
        AppLog.d(TAG, "------after----app_id"+app_id);
        if(mThread!=null&&mThread.isAlive()){
        	mThread.setFlag(false);
        	mThread = null;
        }
        AppLog.d(TAG, "--action_type="+action_type);
        mThread=new LoadAppThread(DetailedActivity.this,mPageBean,action_type);
        startProgressDialog();
        mThread.start();
        if(mLoadDetailedTask!=null&&mLoadDetailedTask.getStatus()!=LoadDetailedTask.Status.FINISHED){
        	mLoadDetailedTask.cancel(true);
        }
        mLoadDetailedTask = (LoadDetailedTask)new LoadDetailedTask().execute(app_id);
    }
    
	public class AppReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			AppLog.d(TAG, "---onReceive------BroadcastReceiver----------------"+arg1.getAction());
			if(appsBean==null||appsBean.getAppName()==null){
				return;
			}
            if(arg1.getAction().equals(Constants.INTENT_INSTALL_COMPLETED)){
            	if(arg1.getStringExtra("app_id")==null||arg1.getStringExtra("app_id").trim().equals("")||arg1.getStringExtra("app_id").trim().equals("null")){
					return;
				}
				int tapp_id=Integer.valueOf(arg1.getStringExtra("app_id"));
				AppLog.d(TAG, "---------BroadcastReceiver-----INTENT_INSTALL_COMPLETED----------app_id="+tapp_id);
				if(appsBean!=null){
					if(tapp_id==appsBean.getID()){
						Message mes=mainHandler.obtainMessage(MSG_SHOW_INSTALL);
						mes.obj=arg1.getStringExtra("pkg_name");
						AppLog.d(TAG, "-----down completed  pkgname = :"+arg1.getStringExtra("pkg_name"));
						
						mainHandler.sendMessage(mes);
					}
				}
			}else if(arg1.getAction().equals(Constants.INTENT_INSTALL_FAIL)){
				AppLog.d(TAG, "----------Constants.INTENT_INSTALL_FAIL------------");
				if(appsBean==null||appsBean.getAppName()==null){
					return;
				}
				if(arg1.getStringExtra("pkg_name")==null||arg1.getStringExtra("pkg_name").trim().equals("")){
					return;
				}
				if(arg1.getStringExtra("pkg_name").equals(appsBean.getPkgName())){
					progressLayout.setVisibility(View.GONE);
	                downInstallButton.setText(DetailedActivity.this.getString(R.string.down_install));
	                downinstallLayout.setVisibility(View.VISIBLE);
				}
			}else if(arg1.getAction().equals(Constants.INTENT_DOWNLOAD_PROGRESS)){
				Log.d(TAG, "--------INTENT_DOWNLOAD_PROGRESS--------receive-----");
				if(appsBean!=null){
					if(arg1.getStringExtra("server_app_id")==null||arg1.getStringExtra("server_app_id").trim().equals("")||arg1.getStringExtra("server_app_id").trim().equals("null")){
						return;
					}
					int app_id=Integer.valueOf(arg1.getStringExtra("server_app_id"));
					AppLog.d(TAG, "-----------app_id="+app_id+";----beanID"+appsBean.getID());
					if(app_id==appsBean.getID()){
						Message mesg=mainHandler.obtainMessage();
						mesg.what=MSG_SHOW_DOWNING;
						int file_sum=arg1.getIntExtra("file_sum", -1);
		            	int down_size=arg1.getIntExtra("download_size", -1);
						mesg.arg1=down_size;
						mesg.arg2=file_sum;
						AppLog.d(TAG, "--------------down_size="+down_size+";file_sum="+file_sum);
						mainHandler.sendMessage(mesg);
					}
				}
			}else if(arg1.getAction().equals(Constants.INTENT_DOWNLOAD_ERROR)){
				AppLog.d(TAG, "--------INTENT_DOWNLOAD_ERROR--------receive-----");
				if(arg1.getStringExtra("pkg_name")==null||arg1.getStringExtra("pkg_name").trim().equals("")){
					return;
				}
				AppLog.d(TAG, "--------INTENT_DOWNLOAD_ERROR--------receive-----pkg_name="+arg1.getStringExtra("pkg_name"));
				if(arg1.getStringExtra("pkg_name").equals(appsBean.getPkgName())){
					downinstallLayout.setVisibility(View.GONE);
					progressLayout.setVisibility(View.VISIBLE);
					progressBarText.setText(DetailedActivity.this.getString(R.string.download_error));
				}
			}else if(arg1.getAction().equals(Constants.INTENT_UNINSTALL_COMPLETED)){
				AppLog.d(TAG, "----------INTENT_UNINSTALL_COMPLETED------------");
				if(appsBean==null||appsBean.getAppName()==null){
					return;
				}
				if(arg1.getStringExtra("pkg_name")==null||arg1.getStringExtra("pkg_name").trim().equals("")){
					return;
				}
				if(arg1.getStringExtra("pkg_name").equals(appsBean.getPkgName())){
					int status=tDBUtils.queryStatusByPkgName(arg1.getStringExtra("pkg_name"));
					if(status!=Constants.APP_STATUS_UNDOWNLOAD){
						return;
					}
					progressLayout.setVisibility(View.GONE);
	                downInstallButton.setText(DetailedActivity.this.getString(R.string.down_install));
	                downinstallLayout.setVisibility(View.VISIBLE);
				}
			}else if(arg1.getAction().equals(Constants.INTENT_DOWNLOAD_DETLET)){
				AppLog.d(TAG, "----------INTENT_UNINSTALL_COMPLETED------------");
				if(arg1.getStringExtra("pkg_name")==null||arg1.getStringExtra("pkg_name").trim().equals("")){
					return;
				}
				if(arg1.getStringExtra("pkg_name").equals(appsBean.getPkgName())){
					int status=tDBUtils.queryStatusByPkgName(arg1.getStringExtra("pkg_name"));
					if(status==Constants.APP_STATUS_UNDOWNLOAD){
						progressLayout.setVisibility(View.GONE);
		                downInstallButton.setText(DetailedActivity.this.getString(R.string.down_install));
		                downinstallLayout.setVisibility(View.VISIBLE);
					}
				}
			}else if(arg1.getAction().equals(Constants.INTENT_ACTION_UPDATESCORE_SUCCESS)){
				AppLog.d(TAG, "-----------------now is received the score up to success-------------");
				mainHandler.sendEmptyMessage(MSG_REFRESH_WATI_TIME);
			}else if(arg1.getAction().equals(Constants.INTENT_DOWNLOAD_PAUSE)||arg1.getAction().equals(Constants.INTENT_DOWNLOAD_STARTED)){
				AppLog.d(TAG, "pgk_name="+arg1.getStringExtra("pkg_name"));
				if(arg1.getStringExtra("pkg_name")==null||arg1.getStringExtra("pkg_name").trim().equals("")){
					return;
				}
				int appId=Integer.valueOf(arg1.getStringExtra("app_id"));
				AppLog.d(TAG, "appId="+appId);
				if(appsBean.getPkgName().equals(arg1.getStringExtra("pkg_name"))&&appId==appsBean.getID()){
					Message mes=mainHandler.obtainMessage(MSG_CHANGE_STATUS_PROGRESS);
					mes.obj=appsBean.getPkgName();
					mainHandler.sendMessageDelayed(mes, Constants.Delayed.TIME1);
				}
			}else if(arg1.getAction().equals(Constants.INTENT_UPDATE_COMPLETED)){
				if(arg1.getStringExtra("app_id")==null||arg1.getStringExtra("app_id").trim().equals("")||arg1.getStringExtra("app_id").trim().equals("null")){
					return;
				}
				if(arg1.getStringExtra("pkg_name")==null||arg1.getStringExtra("pkg_name").trim().equals("")){
					return;
				}
				int status=tDBUtils.queryStatusByPkgName(arg1.getStringExtra("pkg_name"));
				if(status!=Constants.APP_STATUS_INSTALLED){
					return;
				}
				for(int i=0;i<mLayout.getChildCount();i++){
					View view=mLayout.getChildAt(i);
					AppsBean ab=(AppsBean)view.getTag();
					if(ab.getPkgName().equals(arg1.getStringExtra("pkg_name"))){
						(view.findViewById(R.id.detail_item_pop)).setVisibility(View.GONE);
						(view.findViewById(R.id.pop_update)).setVisibility(View.GONE);
					}
				}
			}
		}
    }

	
	/* 进入评价 */
	public void evaluate() {
		if(null == appsBean){
			return;
		}
		Intent intent = new Intent();
		intent.setClassName(DetailedActivity.this,"com.joysee.appstore.activity.AppAppraisalActivity");
		intent.putExtra("app_id", appsBean.getID());
		if(mUserId==null){
    		mUserId = "-1";
    	}
		intent.putExtra("user_id", mUserId);
		if(mHaveBeenEvaluated){
			intent.putExtra("isScore", true);
			intent.putExtra("amount", appsBean.getVoteNum());//评分人数
		}else{
			intent.putExtra("isScore", false);
			intent.putExtra("amount", appsBean.getVoteNum());//评分人数
		}
		String soc=appsBean.getScore()==null?"0":appsBean.getScore().trim();
		float res=Integer.valueOf(soc.equals("")?"0":soc);
		res = res / 2;
		intent.putExtra("ScoreValue", res); //用户评分值
		startActivityForResult(intent, requestCode);
	}
	
	/**
	 * 
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//AppLog.log_D(TAG, "------------------onActivityResult-----------------");
		//mainHandler.sendEmptyMessageDelayed(MSG_REFRESH_WATI_TIME, 1000);
	}
	
	
	/*我的收蔵*/
	public void myFavorites(){  
		if(sp.getBoolean("isFavorite", false)){  
			mainHandler.sendEmptyMessage(MSG_TURN_FAVORITES_IAMGE_YES); 
		}
		if(!sp.getBoolean("isFavorite", false)){ 
			mainHandler.sendEmptyMessage(MSG_TURN_FAVORITES_IAMGE_NO);
		}
	}
	
	public void search(){
		Intent intent = new Intent(DetailedActivity.this,AppSearchActivity.class);
		startActivity(intent);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.top_down_record:
				downloadMgr();
				break;
			case R.id.downinstallLayout:
				workHandler.sendEmptyMessage(MSG_DOWN_APK);
				break;
			case R.id.item_img:
				if(v.getTag()==null) return;
				AppsBean arg2 = (AppsBean)v.getTag();
				app_id=arg2.getID();
				startProgressDialog();
				if(mLoadDetailedTask!=null&&mLoadDetailedTask.getStatus()!=LoadDetailedTask.Status.FINISHED){
		        	mLoadDetailedTask.cancel(true);
		        }
		        mLoadDetailedTask = (LoadDetailedTask)new LoadDetailedTask().execute(app_id);
                break;
			case R.id.run_button:
				runApp();
				break;
			case R.id.update_button:
				downApp(true);
				break;
			case R.id.evaluate:
				evaluate();
				break;
			case R.id.favorites:
				myFavorites();
				break;
			case R.id.top_search:
				search();
				break;
			case R.id.progressLayout:
				mainHandler.removeMessages(MSG_PAUSE_CONTINUE);
				mainHandler.sendEmptyMessageDelayed(MSG_PAUSE_CONTINUE, Constants.Delayed.TIME2);
				break;
			default:
				break;
		}
	}
	
	public void onFocusChange(View v, boolean hasFocus){
		if(hasFocus){
			control.addFocusView(v);
		}
	}
}
