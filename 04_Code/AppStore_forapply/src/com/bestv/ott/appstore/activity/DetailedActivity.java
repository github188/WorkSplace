package com.bestv.ott.appstore.activity;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import com.bestv.ott.BesTVOttServices.BesTVOttMng;
import com.bestv.ott.BesTVOttServices.BesTVOttServices;
import com.bestv.ott.BesTVOttServices.IBesTVOttConnListener;
import com.bestv.ott.BesTVOttServices.UserService.IUserService;
import com.bestv.ott.appstore.R;
import com.bestv.ott.appstore.animation.AnimationButton;
import com.bestv.ott.appstore.animation.AnimationControl;
import com.bestv.ott.appstore.animation.AnimationImageButton;
import com.bestv.ott.appstore.animation.AnimationProgress;
import com.bestv.ott.appstore.animation.AnimationTextButton;
import com.bestv.ott.appstore.animation.AnimationTwoImages;
import com.bestv.ott.appstore.animation.DetailedActivityAppLayout;
import com.bestv.ott.appstore.animation.DownloadTextView;
import com.bestv.ott.appstore.animation.SearchTextView;
import com.bestv.ott.appstore.animation.TextButton;
import com.bestv.ott.appstore.common.ApplicationBean;
import com.bestv.ott.appstore.common.AppsBean;
import com.bestv.ott.appstore.common.Constants;
import com.bestv.ott.appstore.common.DataOperate;
import com.bestv.ott.appstore.common.PageBean;
import com.bestv.ott.appstore.common.TaskBean;
import com.bestv.ott.appstore.db.DBUtils;
import com.bestv.ott.appstore.parser.DetailedParser;
import com.bestv.ott.appstore.service.DownloadService;
import com.bestv.ott.appstore.service.DownloadService.ServiceBinder;
import com.bestv.ott.appstore.thread.LoadAppThread;
import com.bestv.ott.appstore.utils.AppLog;
import com.bestv.ott.appstore.utils.CaCheManager;
import com.bestv.ott.appstore.utils.NetUtil;
import com.bestv.ott.appstore.utils.RequestParam;
import com.bestv.ott.appstore.utils.Utils;
import com.bestv.ott.util.bean.User;
import com.bestv.ott.util.bean.UserProfile;

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
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.FocusFinder;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.ProgressBar;

public class DetailedActivity extends BaseActivity implements View.OnClickListener{

public static final String TAG = "com.bestv.ott.appstore.DetailedActivity";
	private FocusFinder mFocusF = FocusFinder.getInstance();
	private AnimationControl control = AnimationControl.getInstance();
		
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

    private ImageView mImageView;
    private ViewFlipper mViewFlipper;
    private DetailedActivityAppLayout mLayout;
    private LayoutInflater mLayoutInflater;
    
    private ImageView upButton;
    private ImageView downButton;
    private ImageView imageView;
    private AnimationImageButton mRunButton,mUpdateButton;
    private AnimationTextButton downInstallButton;
    private AnimationTwoImages mEvaluate,mFavorites;
    private AnimationProgress mProgressLay;
    private ProgressBar mDownProgress;
    private TextView mDownProgressText; 
    
    private DownloadTextView mDownRecord;
    private RelativeLayout mRunAndUpdate;
    private SearchTextView mSearch;
    
    private TextView nameView;
    private TextView app_conprovider_View;
    private TextView conprovider_name;
    private String action_type=RequestParam.Action.GETRECOMMENDLIST;//当前请求类型url,默认为推荐应用
    private int app_id;
    private PageBean mPageBean;
    private AppsBean appsBean; 
    private DataOperate mDataOperate;
    private DBUtils tDBUtils ;
    private NetUtil mNetUtil;
    public boolean refeshDown=true;//控制一段时间来进行刷新
    private int turnDown=0;//0表示第一次进，另边不要上焦点，1表示向下翻页，2表示向上翻页
    private Boolean mHaveBeenEvaluated = false; //是否已评价
    private Map<Boolean,Integer> mSroceMap; //请求评分时返回的MAP
    private Integer mUserScore;//用户对应此APP的评分
    private String mUserId = null; //用中间件去获取用户ID
    public LoadAppThread mThread = null;
    
    public BesTVOttMng mng = null; 
    public IUserService mIUserService;
    
    public HandlerThread workThread;
    public Handler workHandler;
    private ServiceBinder downloadService;
    private AppReceiver mAppReceiver = null;
    private SharedPreferences sp;
    
    private LoadDetailedTask mLoadDetailedTask = null;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppLog.d(TAG," onCreate ");
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.app_detailed_layout);
        mPageBean=new PageBean();
        mPageBean.setPageSize(3);
        tDBUtils = new DBUtils(DetailedActivity.this);
        mNetUtil = new NetUtil(DetailedActivity.this);
        mDataOperate=new DataOperate(DetailedActivity.this);
        sp = getSharedPreferences("user_favorites",Context.MODE_PRIVATE);//暂时放用户收蔵
        setupViews();
        Intent intentSer = new Intent("com.bestv.ott.appstore.service.DownloadService");
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
    	mImageView = (ImageView) this.findViewById(R.id.animation_imageview);
        mViewFlipper = (ViewFlipper) this.findViewById(R.id.app_detailed_viewfillper);
        mSroceMap = new HashMap<Boolean, Integer>();
        imageView=(ImageView)findViewById(R.id.app_image);//单元格imageview
        upButton=(ImageView)findViewById(R.id.up_image);
        downButton=(ImageView)findViewById(R.id.down_imgae);
        nameView=(TextView)findViewById(R.id.app_name);
        app_conprovider_View = (TextView)findViewById(R.id.app_conprovider);
        downInstallButton=(AnimationTextButton)findViewById(R.id.down_install);
        mDownRecord=(DownloadTextView)findViewById(R.id.top_down_record);
        mRunAndUpdate=(RelativeLayout)findViewById(R.id.run_update);
        mRunButton=(AnimationImageButton)findViewById(R.id.run_button);
        mUpdateButton=(AnimationImageButton)findViewById(R.id.update_button);
        mEvaluate=(AnimationTwoImages)findViewById(R.id.evaluate);
        mFavorites=(AnimationTwoImages)findViewById(R.id.favorites);
        mProgressLay=(AnimationProgress)findViewById(R.id.progressbar_layout);
        mSearch=(SearchTextView) findViewById(R.id.top_search);
        conprovider_name = (TextView)findViewById(R.id.conprovider_name);
        
//        mRunButton.setNextFocusUpId(mDownRecord.getId());
//        mUpdateButton.setNextFocusUpId(mDownRecord.getId());
//        downInstallButton.setNextFocusUpId(mDownRecord.getId());
        //往上的焦点先到［搜索］
        mRunButton.setNextFocusUpId(mSearch.getId());
        mUpdateButton.setNextFocusUpId(mSearch.getId());
        downInstallButton.setNextFocusUpId(mSearch.getId());
        
        mRunButton.setNextFocusDownId(mEvaluate.getId());
        mUpdateButton.setNextFocusDownId(mEvaluate.getId());
        downInstallButton.setNextFocusDownId(mEvaluate.getId());
        
        
        downInstallButton.setOnClickListener(this);
        mDownRecord.setOnClickListener(this);
        mRunButton.setOnClickListener(this);
        mUpdateButton.setOnClickListener(this);
        mEvaluate.setOnClickListener(this);
        mEvaluate.setImage1Resource(R.drawable.app_detail_pinjia);
        mEvaluate.setImage2Resource(R.drawable.app_detail_pinjia_text); 
        mFavorites.setOnClickListener(this);
        mSearch.setOnClickListener(this);
        mProgressLay.setOnClickListener(this);
        
//        if(sp.getBoolean("isFavorite", false)){
//        	mFavorites.setImage1Resource(R.drawable.collection_cancle); 
//        	Editor editor = sp.edit();
//        	editor.putBoolean("isFavorite", true); 
//        	editor.commit();
//        }else if(!sp.getBoolean("isFavorite", false)){ 
//        	mFavorites.setImage1Resource(R.drawable.collection); 
//        	Editor editor = sp.edit();
//        	editor.putBoolean("isFavorite", false);  
//        	editor.commit();
//        }
        downInstallButton.setBackImageView(mImageView);
        mDownRecord.setBackImageView(mImageView);
        mRunButton.setBackImageView(mImageView);
        mUpdateButton.setBackImageView(mImageView);
        mFavorites.setBackImageView(mImageView);
        mEvaluate.setBackImageView(mImageView);
        mProgressLay.setBackImageView(mImageView);
        mSearch.setBackImageView(mImageView);
    }
    
    public void initWorkThread(){
    	workThread=new HandlerThread("handler_thread");
        workThread.start();
        workHandler=new Handler(workThread.getLooper()){
            public void handleMessage(Message msg) {
                switch(msg.what){
                case MSG_DOWN_APK:
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
                	if(mUserId != null){ //用户ID为空,则不用去获取用户信息
                		AppLog.d(TAG, "--------mUser---------");
                		mSroceMap = mDataOperate.getScorFormService(mUserId, appsBean.getID()); 
                		//mSroceMap = mDataOperate.getScorFormService("11", appsBean.getID()); 

                		if(mSroceMap!=null){
                			if(mSroceMap.containsKey(true)){ //已评
                				mHaveBeenEvaluated = true;
                				mUserScore = mSroceMap.get(true);
                				mainHandler.sendEmptyMessage(MSG_SCOR_TRUE);
                				AppLog.d(TAG, "-----------------mSroceMap.containsKey(true)--------------");
                			}else if(mSroceMap.containsKey(false)){ //没评
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
        	Editor editor = sp.edit(); //更改收蔵图片状态
            switch(msg.what){
            case MSG_SHOW_APP_DETAIL:
            	stopProgressDialog();
            	if(appsBean==null){//网络问题
            		startNetErrorDialog();
            		return;
            	}
            	if(mLayout!=null){
            		for(int i=0;i<mLayout.getChildCount();i++){
                		((AnimationButton)((LinearLayout)mLayout.getChildAt(i)).getChildAt(0)).stopAnimation();
                	}
            	}
                ((TextView)findViewById(R.id.app_comment)).setText(DetailedActivity.this.getString(R.string.app_comment,appsBean.getVoteNum()));
                ((TextView)findViewById(R.id.app_size)).setText(DetailedActivity.this.getString(R.string.app_size,Utils.transformByte(appsBean.getSize())));
                ((TextView)findViewById(R.id.app_verson)).setText(DetailedActivity.this.getString(R.string.app_verson,appsBean.getVersionName()));
                ((TextView)findViewById(R.id.app_update)).setText(DetailedActivity.this.getString(R.string.app_update_time,appsBean.getCreateTime()));
                ((TextView)findViewById(R.id.app_lable)).setText(DetailedActivity.this.getString(R.string.label,appsBean.getTypeName()));
                ((TextView)findViewById(R.id.app_remark)).setText(appsBean.getRemark()+"");
                if(!"".equals(appsBean.getContentProvider())){
                	app_conprovider_View.setText(appsBean.getContentProvider());
                	app_conprovider_View.setSelected(true);
                	conprovider_name.setVisibility(View.VISIBLE);
                }
                findViewById(R.id.score_tex).setVisibility(View.VISIBLE);
                findViewById(R.id.score_unit).setVisibility(View.VISIBLE);
                AppLog.d(TAG, "------;appsBean.getTypeName()="+appsBean.getTypeName());
//				BitmapDrawable drawable =(BitmapDrawable)imageView.getDrawable();
//				if(null!=drawable&&appsBean.getNatImageUrl()!=null){
//					Bitmap bmp = drawable.getBitmap();
//					if (null != bmp && !bmp.isRecycled()){
//						bmp.recycle();
//						bmp = null;
//						AppLog.d(TAG, "----------------bmp.recycle()------------------");
//					}
//				}
                if(appsBean.getNatImageUrl()==null||appsBean.getNatImageUrl().trim().equals("")){
                	imageView.setImageResource(R.drawable.detailed_default_img);
                }else{
                	BitmapFactory.Options options=new BitmapFactory.Options();
                	Bitmap bmp = BitmapFactory.decodeFile(appsBean.getNatImageUrl(),options); 
                	if(bmp==null){
                		File imageF=new File(appsBean.getNatImageUrl());
                		if(imageF.exists()){
                			imageF.delete();
                		}
                		imageView.setImageResource(R.drawable.detailed_default_img);
                	}else{
                		imageView.setImageBitmap(bmp);
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
	                        	mProgressLay.setFocusable(true);
	                        	int sumSize=tDBUtils.querySumSizeByPkgName(appsBean.getPkgName());
	                        	int downSize=tDBUtils.queryDowningSizeByPkgName(appsBean.getPkgName());
	                        	AppLog.d(TAG, "------------------------------------downSize="+downSize+";sumSize="+sumSize);
	                        	if(sumSize==0){
	                        		mProgressLay.setProgress(100);
	                        	}else{
	                        		long down=downSize;
	                            	long sum=sumSize;
	                            	int res=(int)(down*100/sum);
	                        		mProgressLay.setProgress(res);
	                        	}
	                        	if(appStatus==Constants.DOWNLOAD_STATUS_ERROR){
	                        		mProgressLay.setText(DetailedActivity.this.getString(R.string.download_error));
	                        	}else if(appStatus==Constants.DOWNLOAD_STATUS_PAUSE){
	                        		mProgressLay.setText(DetailedActivity.this.getString(R.string.pause_download));
	                        	}else {
	                        		mProgressLay.setText(DetailedActivity.this.getString(R.string.download_tab_2));
	                        	}
	                            downInstallButton.setVisibility(View.GONE);
	                            mProgressLay.setVisibility(View.VISIBLE);
	                            Message fousMes1=mainHandler.obtainMessage();
	                            fousMes1.obj=mProgressLay;
	                            fousMes1.what=MSG_FOCUS;
	                            mainHandler.sendMessageDelayed(fousMes1, 100);
	                            AppLog.d(TAG, "-------------break----------");
	                            break;
	                        case Constants.APP_STATUS_INSTALLED: 
	                            String versionNet=tDBUtils.queryVersionByPkgName(appsBean.getPkgName());
	                            AppLog.d(TAG, "-------------native version="+versionNet+";service version="+appsBean.getVersion());
	                            if(Utils.CompareVersion(appsBean.getVersion(), versionNet)){//need update
	                            	mProgressLay.setVisibility(View.GONE);
	                                downInstallButton.setVisibility(View.VISIBLE);
	                                downInstallButton.setFocusable(true);
	                                downInstallButton.setText(DetailedActivity.this.getString(R.string.update));
	                            }else{
	                                mProgressLay.setVisibility(View.GONE);
	                                downInstallButton.setVisibility(View.VISIBLE);
	                                downInstallButton.setFocusable(true);
	                                downInstallButton.setText(DetailedActivity.this.getString(R.string.run_app));
	                            }
	                            Message fousMes3=mainHandler.obtainMessage();
	                            fousMes3.obj=downInstallButton;
	                            fousMes3.what=MSG_FOCUS;
	                            mainHandler.sendMessageDelayed(fousMes3, 100);
	                            break;
	                        default:
	                        	AppLog.d(TAG, "-------------"+appsBean.getPrice());
	                        	mProgressLay.setVisibility(View.GONE);
	                        	downInstallButton.setVisibility(View.VISIBLE);
	                        	if(appsBean.getPrice()>0){
	                        		downInstallButton.setText(DetailedActivity.this.getString(R.string.charge_down_install,String.valueOf(appsBean.getPrice())));
	                        	}else{
	                        		downInstallButton.setText(DetailedActivity.this.getString(R.string.down_install));
	                        	}
	                        	downInstallButton.setFocusable(true);
	                            Message fousMes4=mainHandler.obtainMessage();
	                            fousMes4.obj=downInstallButton;
	                            fousMes4.what=MSG_FOCUS;
	                            mainHandler.sendMessageDelayed(fousMes4, 100);
	                            break;
                    	}
                    	
                        break;
                    case Constants.APP_STATUS_DOWNLOADED:
                    	mProgressLay.setFocusable(true);
                        downInstallButton.setVisibility(View.GONE);
                        mProgressLay.setVisibility(View.VISIBLE);
                        mProgressLay.setProgress(100);
                        mProgressLay.setText(DetailedActivity.this.getString(R.string.download_tab_2));
                        Message fousMes2=mainHandler.obtainMessage();
                        fousMes2.obj=mProgressLay;
                        fousMes2.what=MSG_FOCUS;
                        mainHandler.sendMessageDelayed(fousMes2, 100);
                    	break;
                    case Constants.APP_STATUS_INSTALLED: 
                        String versionNet=tDBUtils.queryVersionByPkgName(appsBean.getPkgName());
                        AppLog.d(TAG, "-------------native version="+versionNet+";service version="+appsBean.getVersion());
                        if(Utils.CompareVersion(appsBean.getVersion(), versionNet)){//need update
                        	mProgressLay.setVisibility(View.GONE);
                            downInstallButton.setVisibility(View.VISIBLE);
                            downInstallButton.setFocusable(true);
                            downInstallButton.setText(DetailedActivity.this.getString(R.string.update));
                        }else{
                            mProgressLay.setVisibility(View.GONE);
                            downInstallButton.setVisibility(View.VISIBLE);
                            downInstallButton.setFocusable(true);
                            downInstallButton.setText(DetailedActivity.this.getString(R.string.run_app));
                        }
                        Message fousMes3=mainHandler.obtainMessage();
                        fousMes3.obj=downInstallButton;
                        fousMes3.what=MSG_FOCUS;
                        mainHandler.sendMessageDelayed(fousMes3, 100);
                        break;
                    default:
                    	AppLog.d(TAG, "-------------"+appsBean.getPrice());
                    	mProgressLay.setVisibility(View.GONE);
                    	downInstallButton.setVisibility(View.VISIBLE);
                    	if(appsBean.getPrice()>0){
                    		downInstallButton.setText(DetailedActivity.this.getString(R.string.charge_down_install,String.valueOf(appsBean.getPrice())));
                    	}else{
                    		downInstallButton.setText(DetailedActivity.this.getString(R.string.down_install));
                    	}
                    	downInstallButton.setFocusable(true);
                        Message fousMes4=mainHandler.obtainMessage();
                        fousMes4.obj=downInstallButton;
                        fousMes4.what=MSG_FOCUS;
                        mainHandler.sendMessageDelayed(fousMes4, 100);
                        break;
                }
                
                break;
            case MSG_FOCUS:
            	if(AnimationImageButton.class.isInstance(msg.obj)){
            		((AnimationImageButton)msg.obj).requestFocus();
            		if(turnDown==0){
            			control.transformAnimationForImage(mImageView,(AnimationImageButton)msg.obj , DetailedActivity.this,false,false);
            		}else{
            			control.transformAnimationForImage(mImageView,(AnimationImageButton)msg.obj , DetailedActivity.this,true,false);
            		}
            	}else if(AnimationProgress.class.isInstance(msg.obj)){
            		((AnimationProgress)msg.obj).requestFocus();
            		if(turnDown==0){
            			control.transformAnimationForImage(mImageView,(AnimationProgress)msg.obj , DetailedActivity.this,false,false);
            		}else{
            			control.transformAnimationForImage(mImageView,(AnimationProgress)msg.obj , DetailedActivity.this,true,false);
            		}
            	}else if(AnimationTextButton.class.isInstance(msg.obj)){
            		((AnimationTextButton)msg.obj).requestFocus();
            		if(turnDown==0){
            			control.transformAnimationForImage(mImageView,(AnimationTextButton)msg.obj , DetailedActivity.this,false,false);
            		}else{
            			control.transformAnimationForImage(mImageView,(AnimationTextButton)msg.obj , DetailedActivity.this,true,false);
            		}
            	}
            	break;
            case MSG_START_DOWNLOAD:
            	mRunAndUpdate.setVisibility(View.GONE);
            	mRunButton.setVisibility(View.GONE);
            	mUpdateButton.setVisibility(View.GONE);
                downInstallButton.setVisibility(View.GONE);
                mProgressLay.setVisibility(View.VISIBLE);
                mProgressLay.setProgress(0);
                AppLog.d(TAG, "--------------------MSG_START_DOWNLOAD-----------");
                Message fousMes0=mainHandler.obtainMessage();
                fousMes0.obj=mProgressLay;
                fousMes0.what=MSG_FOCUS;
                mainHandler.sendMessageDelayed(fousMes0, 50);
                break;
            case MSG_NET_NOT_CONNECT:
                break;
            case MSG_SD_ERROR:
                Toast toastSD=Toast.makeText(DetailedActivity.this,DetailedActivity.this.getString(R.string.chcekSdcard), Toast.LENGTH_LONG);
                toastSD.setGravity(Gravity.CENTER, 0,0);
                toastSD.show();
                break;
            case MSG_SHOW_DOWNING:
            	refeshDown=true;
                AppLog.d(TAG, "-----------MSG_SHOW_DOWNING-msg.arg1="+msg.arg1+";msg.arg2="+msg.arg2+";value="+(msg.arg1*100/msg.arg2));
                if(mProgressLay.getVisibility()==View.VISIBLE){
                	mProgressLay.setText(DetailedActivity.this.getString(R.string.download_tab_2));
                	long down=msg.arg1;
                	long sum=msg.arg2;
                	if(sum>0){
                		int res=(int)(down*100/sum);
                    	mProgressLay.setProgress(res);
                	}
                }else{
                	AppLog.d(TAG, "---------------------");
                }
                break;
            case MSG_DOWNLOAD_ERROR:
            	if(mProgressLay.getVisibility()==View.VISIBLE){
                }
            	break;
            case MSG_SHOW_INSTALL:
            	AppLog.d(TAG, "$$$$$$$$$$$$$$$$$$$$$msg             MSG_SHOW_INSTALL");
            	mRunAndUpdate.setVisibility(View.GONE);
            	mRunButton.setVisibility(View.GONE);
            	mUpdateButton.setVisibility(View.GONE);
                mProgressLay.setVisibility(View.GONE);
                String versionNet=tDBUtils.queryVersionByPkgName(appsBean.getPkgName());
                AppLog.d(TAG, "-------------versionNet="+versionNet+";appsBean.getVersion()="+appsBean.getVersion());
                if(Utils.CompareVersion(appsBean.getVersion(), versionNet)){
                	downInstallButton.setText(DetailedActivity.this.getString(R.string.update));
                }else{
                	downInstallButton.setText(DetailedActivity.this.getString(R.string.run_app));
                }
                downInstallButton.setVisibility(View.VISIBLE);
                Message fousMes=mainHandler.obtainMessage();
                fousMes.obj=downInstallButton;
                fousMes.what=MSG_FOCUS;
                mainHandler.sendMessageDelayed(fousMes, 50);
                ApplicationBean appBean=tDBUtils.queryApplicationByPkgName((String)msg.obj);
            	for(int i=0;i<mLayout.getChildCount();i++){
            		View viewM=mLayout.getChildAt(i);
            		if(viewM.getVisibility()==View.VISIBLE){
            			AppsBean appsM=(AppsBean)viewM.getTag();
            			AppLog.d(TAG, "pkgM="+appsM.getPkgName()+";pkgBean"+appBean.getPkgName()+"appsM.vserion="+appsM.getVersion()+";appBean.version="+appBean.getVersion());
            			if(appBean.getPkgName().equals(appsM.getPkgName())&&Utils.CompareVersion(appsM.getVersion(),appBean.getVersion())){
            				ImageView popIv = (ImageView) viewM.findViewById(R.id.detail_item_pop);
            				ImageView popTvT = (ImageView) viewM.findViewById(R.id.pop_update);
            				popIv.setVisibility(View.VISIBLE);
            				popTvT.setVisibility(View.VISIBLE);
            			}
            		}
            	}
                break;
            case MSG_TURN_FAVORITES_IAMGE_YES:
            	mFavorites.setImage1Resource(R.drawable.collection);
    			//Toast.makeText(getApplicationContext(), "取消收蔵！", 0).show();
    			editor.putBoolean("isFavorite", false);
    			editor.commit();
            	break;
            case MSG_TURN_FAVORITES_IAMGE_NO:
            	mFavorites.setImage1Resource(R.drawable.collection_cancle);
    			//Toast.makeText(getApplicationContext(), "收蔵成功！", 0).show();
    			editor.putBoolean("isFavorite", true);
    			editor.commit();
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
            	if(pauseTask==null){
            		return;
            	}
				AppLog.d(TAG, "-----------mHandler-------MSG_PAUSE_CONTINUE-------status="+pauseTask.getStatus()+";name="+pauseTask.getAppName());
				if(pauseTask.getStatus()==Constants.DOWNLOAD_STATUS_EXECUTE){
					downloadService.pauseTask(pauseTask);
//					mProgressLay.setText(DetailedActivity.this.getString(R.string.pause_download));
				}else if(pauseTask.getStatus()==Constants.DOWNLOAD_STATUS_PAUSE){
					downloadService.continueTask(pauseTask);
//					mProgressLay.setText(DetailedActivity.this.getString(R.string.download_tab_2));
				}
            	break;
            case MSG_CHANGE_STATUS_PROGRESS:
            	AppLog.d(TAG, "--------pgkname="+msg.obj);
            	TaskBean task=tDBUtils.queryTaskByPkgName((String)msg.obj);
            	if(task==null){
            		return;
            	}
				AppLog.d(TAG, "-----------mHandler-------MSG_CHANGE_STATUS_PROGRESS-------status="+task.getStatus()+";name="+task.getAppName());
				if(task.getStatus()==Constants.DOWNLOAD_STATUS_EXECUTE){
					mProgressLay.setText(DetailedActivity.this.getString(R.string.download_tab_2));
				}else if(task.getStatus()==Constants.DOWNLOAD_STATUS_PAUSE){
					mProgressLay.setText(DetailedActivity.this.getString(R.string.pause_download));
				}
            	break;
            }
            super.handleMessage(msg);
        }
    };
    
    public void initViewFlipper(){
      	 mLayoutInflater = this.getLayoutInflater();
      	 mLayout = new DetailedActivityAppLayout(this);
      	 mLayout.setBackImageView(mImageView);
      	 mLayout.setColumnCount(1);
      	 mLayout.setOnSlidingAtEndListener(new DetailedActivityAppLayout.OnSlidingAtEndListener() {
			public boolean onSlidingAtUp(DetailedActivityAppLayout layout, View view) {
				AppLog.d(TAG, "----------onSlidingAtUp--------");
//				if(view instanceof LinearLayout){
//					AnimationButton focusView = (AnimationButton)((LinearLayout) view).getChildAt(0);
//					if(focusView != null)
//						focusView.stopAnimation();
//				}
				if(mPageBean.getPageNo() == 1){
					//mDownRecord.requestFocus();
					//control.transformAnimation(mImageView, view, mDownRecord, DetailedActivity.this, true, true);
					mSearch.requestFocus();
					control.transformAnimation(mImageView, view, mSearch, DetailedActivity.this, true, true);
					return true;
				}
				previousPage();
				return true;
			}
			public boolean onSlidingAtDown(DetailedActivityAppLayout layout, View view) {
				AppLog.d(TAG, "----------onSlidingAtDown--------");
//				if(view instanceof LinearLayout){
//					AnimationButton focusView = (AnimationButton)((LinearLayout) view).getChildAt(0);
//					if(focusView != null)
//						focusView.stopAnimation();
//				}
//				if (mPageBean.nextPage()) {
//					nextPage();
//					return true;
//				}
				nextPage();
				return true;
			}
		});
      	 mLayout.setRowCount(3);
           for(int i=0;i<3;i++){
   	       	 View view = mLayoutInflater.inflate(R.layout.detailed_item_layout, null);
   	     	 AnimationButton button = (AnimationButton) view.findViewById(R.id.item_img);
   	     	 button.setOnClickListener(this);
   	     	 button.setImageView(mImageView);
   	     	 if(i==0){
   	     		 mDownRecord.setNextFocusDownId(button.getId());
//   	     		 view.setNextFocusUpId(mDownRecord.getId());
//   	     		 button.setOnKeyListener(new View.OnKeyListener() {
//   					@Override
//   					public boolean onKey(View v, int keyCode, KeyEvent event) {
//   						if(event.getAction()==KeyEvent.ACTION_DOWN&&keyCode==KeyEvent.KEYCODE_DPAD_UP){
//   						  if(mPageBean.getPageNo()==1){//表示没有上一页
//   					            return false;
//   					      }
//   							previousPage();
//   							return true;
//   						}else if(event.getAction()==KeyEvent.ACTION_DOWN&&keyCode==KeyEvent.KEYCODE_DPAD_LEFT){
//   							setLeftFocus();
//   							return true;
//   						}
//   						return false;
//   					}
//   				});
   	     	 }
//   	     	 if(i==2){
//   	     		 button.setOnKeyListener(new View.OnKeyListener() {
//   					@Override
//   					public boolean onKey(View v, int keyCode, KeyEvent event) {
//   						if(event.getAction()==KeyEvent.ACTION_DOWN&&keyCode==KeyEvent.KEYCODE_DPAD_DOWN){
//   							nextPage();
//   							return true;
//   						}
//   						return false;
//   					}
//   				});
//   	     	 }
   	     	mLayout.addView(view);
           }
           mViewFlipper.addView(mLayout);
           AppLog.d(TAG, "mViewFlipper.getChildCount()"+mViewFlipper.getChildCount());
      }
    
    public void refreshAppsList(List<AppsBean> appList){
    	AppLog.d(TAG, "-------------------------------");
        if(appList==null){//网络有误
        	startNetErrorDialog();
    		return;
    	}
    	if(mViewFlipper.getChildCount()==0){
        	initViewFlipper();
        }
    	if(turnDown==1){
    		((AnimationButton)((LinearLayout)mLayout.getChildAt(2)).getChildAt(0)).stopAnimation();
    		control.transformAnimation(mImageView, mLayout.getChildAt(2), mLayout.getChildAt(0), DetailedActivity.this, false, true);
			((LinearLayout)mLayout.getChildAt(0)).getChildAt(0).requestFocus();
			((AnimationButton)((LinearLayout)mLayout.getChildAt(0)).getChildAt(0)).startAnimation();
    	}else if(turnDown==2){
    		((AnimationButton)((LinearLayout)mLayout.getChildAt(0)).getChildAt(0)).stopAnimation();
    		control.transformAnimation(mImageView, mLayout.getChildAt(0), mLayout.getChildAt(2), DetailedActivity.this, false, true);
			((LinearLayout)mLayout.getChildAt(2)).getChildAt(0).requestFocus();
			((AnimationButton)((LinearLayout)mLayout.getChildAt(2)).getChildAt(0)).startAnimation();
    	}
    	AppLog.d(TAG, "--------appList.size="+appList.size());
    	
    	//先进行内存的释放
    	/*if(null!=mLayout&&mLayout.getChildCount()>0){
    		for(int i=0;i<mLayout.getChildCount();i++){
    			AnimationButton button = (AnimationButton)mLayout.getChildAt(i).findViewById(R.id.item_img);
    			BitmapDrawable drawable =(BitmapDrawable)button.getImageView().getDrawable();
    			if(null!=drawable){//第一次进祥情,drawable会为null
    				Bitmap bmps = drawable.getBitmap();
    				if (null != bmps && !bmps.isRecycled()){
    					bmps.recycle();
    					bmps = null;
    					AppLog.d(TAG, "---------------Apps.recycle()-----------------");
    				}
    			}
    		}
    	}*/
    	//设置新的图片
		for(int i=0;i<appList.size();i++){
			View view=mLayout.getChildAt(i);
			AnimationButton ab=(AnimationButton)view.findViewById(R.id.item_img);
			ab.setTag(appList.get(i).getID());
			if(null==appList.get(i).getNatImageUrl()||appList.get(i).getNatImageUrl().equals("")){
				ab.setImageRes(R.drawable.app_default_img);
			}else{
				ab.setImageURI(Uri.parse(appList.get(i).getNatImageUrl()));
			}
			((TextView)view.findViewById(R.id.item_name)).setText(appList.get(i).getAppName());
			view.setVisibility(View.VISIBLE);
			view.setTag(appList.get(i));
			ImageView popIv = (ImageView) view.findViewById(R.id.detail_item_pop);
			ImageView popTvT = (ImageView) view.findViewById(R.id.pop_update);
			int appStatus = tDBUtils.queryStatusByPkgName(appList.get(i).getPkgName());
			if(appStatus==Constants.APP_STATUS_INSTALLED||appStatus==Constants.APP_STATUS_UPDATE){
				String newVersion = appList.get(i).getVersion(); //------by xubin
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
		
		if(appList.size()<3){
			for(int i=appList.size();i<3;i++){
				mLayout.getChildAt(i).setVisibility(View.GONE);
			}
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
	}
    
    /**
     * 当焦点在推荐位向右移动时，设在下载安装上
     */
    public void setLeftFocus(){
    	if(downInstallButton.getVisibility()==View.VISIBLE){
    		downInstallButton.requestFocus();
    	}else if(mProgressLay.getVisibility()==View.VISIBLE){
    		mProgressLay.requestFocus();
    	}
    }
    
    /**
     * 下一页
     */
    public void nextPage(){
    	if(mThread.isAlive()){
    		return;
    	}
        if(!mPageBean.nextPage()){//表示没有下一页
            return;
        }
        turnDown=1; 
        mThread=new LoadAppThread(DetailedActivity.this,mPageBean,action_type);
        mThread.start();
    }
    
    /**
     * 上一页
     */
    public void previousPage(){
    	if(mThread.isAlive()){
    		return;
    	}
        if(!mPageBean.previousPage()){//表示没有上一页
            return;
        }
        turnDown=2;
        mThread=new LoadAppThread(DetailedActivity.this,mPageBean,action_type);
        mThread.start();
    }
    
    private boolean checkSDcard(){
        if(!Utils.checkSDcard()){
            Message msg = mainHandler.obtainMessage(MSG_SD_ERROR);
            mainHandler.sendMessage(msg);
            return false;
        }
        return true;
    }
    
    /**
     * 启动App
     */
    public void runApp(){
    	ApplicationBean bean=tDBUtils.queryApplicationByPkgName(appsBean.getPkgName());
		Intent intent = new Intent();
		intent = getPackageManager().getLaunchIntentForPackage(bean.getPkgName());
		if(null==intent){
			Toast toastSD=Toast.makeText(DetailedActivity.this,DetailedActivity.this.getString(R.string.no_app), Toast.LENGTH_LONG);
            toastSD.setGravity(Gravity.CENTER, 0,0);
            toastSD.show();
		}else{
			startActivity(intent);
		}
    }
    
    public void downApp(boolean update){
    	if(appsBean.getPkgName()==null||appsBean.getPkgName().equals("")){
    		AppLog.d(TAG, "---------downApp----pkgname-is-null;id="+appsBean.getID());
    		Toast toastSD=Toast.makeText(DetailedActivity.this,DetailedActivity.this.getString(R.string.no_pkgname), Toast.LENGTH_LONG);
            toastSD.setGravity(Gravity.CENTER, 0,0);
            toastSD.show();
    		return;
    	}
    	if(appsBean.getNatImageUrl()==null||appsBean.getNatImageUrl().trim().equals("")){
    		AppLog.d(TAG, "---------downApp----NatImageUrl-is-null;id="+appsBean.getID());
    		Toast toastSD=Toast.makeText(DetailedActivity.this,DetailedActivity.this.getString(R.string.no_icon), Toast.LENGTH_LONG);
            toastSD.setGravity(Gravity.CENTER, 0,0);
            toastSD.show();
            return;
    	}
    	AppLog.d(TAG, "----appsBean.getApkUrl()="+appsBean.getApkUrl());
    	try{
    		if(!Utils.checkConnect(appsBean.getApkUrl())){
                startNetErrorDialog();
                return;
            }
    	}catch(Exception e){
    		AppLog.d(TAG, "----error-----check Connect url="+appsBean.getApkUrl());
    	}
        if(!checkSDcard()){
            return;
        }
        //判断SD卡可用空间，小于要下载APK的大小，返回
        if(new CaCheManager().isFreeSpace(appsBean.getSize())){
        	Toast.makeText(getApplicationContext(), R.string.sdcard_full , 0).show();
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
        intent.setClassName(DetailedActivity.this, "com.bestv.ott.appstore.activity.DownloadRecordActivity");
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
		@Override
		protected List<AppsBean> doInBackground(Object... params) {
			AppLog.d(TAG, "----------LoadDetailedTask-----doInBackground-------");
			String urlStr=RequestParam.SERVICE_ACTION_URL+RequestParam.Action.GETAPPDETAIL;
			Map<String,String> param=new TreeMap<String,String>();
			param.put(RequestParam.Param.APPID,String.valueOf(params[0]));
			appsBean=(AppsBean)mNetUtil.getNetData(param, urlStr, null, new DetailedParser());
			if(appsBean!=null&&!appsBean.getSerImageUrl().trim().equals("")){
				boolean res=mNetUtil.loadImage(appsBean.getSerImageUrl(), appsBean.getNatImageUrl());
				if(!res){
					appsBean.setNatImageUrl(null);
				}
				//workHandler.sendEmptyMessage(MSG_SCORE_INFO);//请求APP是否评分		
			}
			if(appsBean!=null){
				AppLog.d(TAG, "--------appsBean--"+appsBean.getPkgName());
			}else{
				AppLog.d(TAG, "--------appsBean--");
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
			String urlStr=RequestParam.SERVICE_ACTION_URL+RequestParam.Action.GETAPPDETAIL;
			Map<String,String> param=new TreeMap<String,String>();
			param.put(RequestParam.Param.APPID,String.valueOf(params[0]));
			appsBean=(AppsBean)mNetUtil.getNetData(param, urlStr, null, new DetailedParser());
			return null;
		}
		
		//发消息刷新评分值和评论人数
		protected void onPostExecute(List<AppsBean> result) {
			if(appsBean!=null && appsBean.getScore()!= null){
				mainHandler.sendEmptyMessage(MSG_REFRESH_APP_SCORE);
			}
			super.onPostExecute(result);
		}
    }
    
    protected void onResume() {
        super.onResume();
        AppLog.d(TAG," onResume ");
        
		mng = BesTVOttServices.createOttConManage(DetailedActivity.this);
		mng.connect(new IBesTVOttConnListener() {
			@Override
			public void onBesTVOttServiceConnected() {
				// 表示中间件内部链接上了。此时，调用B接口提供的功能才可用
				AppLog.d(TAG, "--------------------create mIUserVervice"+"--------------------");
				IUserService us = BesTVOttServices.createUserService();
				while(!(us.isLogin())){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				AppLog.d(TAG, "--------------------get user now--------------------");
				//如果只是获得用户ID，不需要等用户登录成功，只要isOpen=true就可以获取到了
				mUserId = us.getUserID();
				AppLog.d(TAG, "---------------------User userID :="+mUserId+"------------------------");
				UserProfile userProfile = us.getProfile();
				if (null != userProfile) {
					AppLog.d(TAG, "-------------userProfile.getUserID()="+userProfile.getUserID());
					AppLog.d(TAG, "-------------userProfile.getUserGroup()="+userProfile.getUserGroup());
				}
			}
		});
    }
    
    @Override
    protected void onStop(){
    	super.onStop();
    	AppLog.d(TAG, "-----------onPause-------");
		if(mLoadDetailedTask!=null&&mLoadDetailedTask.getStatus()!=LoadDetailedTask.Status.FINISHED){
        	mLoadDetailedTask.cancel(true);
        	mLoadDetailedTask = null;
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
    	mPageBean.setTypeId(typeId); //TODO need to set userID and groudID to pageBean
        AppLog.d(TAG, "------before----app_id"+app_id);
        app_id=intent.getIntExtra("app_id",-1);
        AppLog.d(TAG, "------after----app_id"+app_id);
        if(mThread!=null&&mThread.isAlive()){
        	mThread.setFlag(false);
        	mThread = null;
        }
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
			AppLog.d(TAG, "---------BroadcastReceiver----------------");
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
					mProgressLay.setVisibility(View.GONE);
	                downInstallButton.setText(DetailedActivity.this.getString(R.string.down_install));
	                downInstallButton.setVisibility(View.VISIBLE);
				}
			}else if(arg1.getAction().equals(Constants.INTENT_DOWNLOAD_PROGRESS)){
				Log.d(TAG, "--------INTENT_DOWNLOAD_PROGRESS--------receive-----");
				if(appsBean!=null){
					if(arg1.getStringExtra("server_app_id")==null||arg1.getStringExtra("server_app_id").trim().equals("")||arg1.getStringExtra("server_app_id").trim().equals("null")){
						return;
					}
					int app_id=Integer.valueOf(arg1.getStringExtra("server_app_id"));
					Log.d(TAG, "-----------app_id="+app_id+";----beanID"+appsBean.getID());
					if(app_id==appsBean.getID()){
						TaskBean task=tDBUtils.queryTaskByPkgName(arg1.getStringExtra("pkg_name"));
						if(task.getStatus()==Constants.DOWNLOAD_STATUS_EXECUTE){
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
				}
			}else if(arg1.getAction().equals(Constants.INTENT_DOWNLOAD_ERROR)){
				Log.d(TAG, "--------INTENT_DOWNLOAD_ERROR--------receive-----");
				if(arg1.getStringExtra("pkg_name")==null||arg1.getStringExtra("pkg_name").trim().equals("")){
					return;
				}
				Log.d(TAG, "--------INTENT_DOWNLOAD_ERROR--------receive-----pkg_name="+arg1.getStringExtra("pkg_name"));
				if(arg1.getStringExtra("pkg_name").equals(appsBean.getPkgName())){
					downInstallButton.setVisibility(View.GONE);
					mProgressLay.setVisibility(View.VISIBLE);
					mProgressLay.setText(DetailedActivity.this.getString(R.string.download_error));
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
					mProgressLay.setVisibility(View.GONE);
	                downInstallButton.setText(DetailedActivity.this.getString(R.string.down_install));
	                downInstallButton.setVisibility(View.VISIBLE);
				}
			}else if(arg1.getAction().equals(Constants.INTENT_DOWNLOAD_DETLET)){
				AppLog.d(TAG, "----------INTENT_UNINSTALL_COMPLETED------------");
				if(arg1.getStringExtra("pkg_name")==null||arg1.getStringExtra("pkg_name").trim().equals("")){
					return;
				}
				if(arg1.getStringExtra("pkg_name").equals(appsBean.getPkgName())){
					int status=tDBUtils.queryStatusByPkgName(arg1.getStringExtra("pkg_name"));
					if(status==Constants.APP_STATUS_UNDOWNLOAD){
						mProgressLay.setVisibility(View.GONE);
		                downInstallButton.setText(DetailedActivity.this.getString(R.string.down_install));
		                downInstallButton.setVisibility(View.VISIBLE);
					}
				}
			}else if(arg1.getAction().equals(Constants.INTENT_ACTION_UPDATESCORE_SUCCESS)){
				AppLog.d(TAG, "-----------------now is received the score up to success-------------");
				mainHandler.sendEmptyMessage(MSG_REFRESH_WATI_TIME);
			}else if(arg1.getAction().equals(Constants.INTENT_DOWNLOAD_PAUSE)||arg1.getAction().equals(Constants.INTENT_DOWNLOAD_STARTED)){
				if(arg1.getStringExtra("pkg_name")==null||arg1.getStringExtra("pkg_name").trim().equals("")){
					return;
				}
				if(appsBean.getPkgName().equals(arg1.getStringExtra("pkg_name"))){
					Message mes=mainHandler.obtainMessage(MSG_CHANGE_STATUS_PROGRESS);
					mes.obj=appsBean.getPkgName();
					mainHandler.sendMessage(mes);
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

	
	/**
	 * 进入评价
	 */
	public void evaluate() {
		Intent intent = new Intent();
		intent.setClassName(DetailedActivity.this,"com.bestv.ott.appstore.activity.AppAppraisalActivity");
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
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//AppLog.log_D(TAG, "------------------onActivityResult-----------------");
		//mainHandler.sendEmptyMessageDelayed(MSG_REFRESH_WATI_TIME, 1000);
	}
	
	/**
	 * 我的收蔵
	 */
	public void myFavorites(){  
		
		if(sp.getBoolean("isFavorite", false)){  //如果获取当前为已收蔵(图标为取消收蔵),  则点击后变成(收蔵)
			//mFavorites.setImage1Resource(R.drawable.app_detail_shouchang);
			//workHandler.sendEmptyMessage(MSG_turn_favorites_iamge_yes);
			mainHandler.sendEmptyMessage(MSG_TURN_FAVORITES_IAMGE_YES); 
		}
		
		if(!sp.getBoolean("isFavorite", false)){ //如果获取当前为未收蔵(图标为收蔵)， 则点击后变成(取消收蔵)
			//mFavorites.setImage1Resource(R.drawable.app_detail_no_shouchang);
			//workHandler.sendEmptyMessage(MSG_turn_favorites_iamge_no);
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
			case R.id.down_install:
				workHandler.sendEmptyMessage(MSG_DOWN_APK);
				break;
			case R.id.item_img:
				if(v.getTag()==null) return;
				int arg2 = (Integer)v.getTag();
				app_id=arg2;
				startProgressDialog();
				if(mLoadDetailedTask!=null&&mLoadDetailedTask.getStatus()!=LoadDetailedTask.Status.FINISHED){
		        	mLoadDetailedTask.cancel(true);
		        }
		        mLoadDetailedTask = (LoadDetailedTask)new LoadDetailedTask().execute(arg2);
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
			case R.id.progressbar_layout:
				mainHandler.removeMessages(MSG_PAUSE_CONTINUE);
				mainHandler.sendEmptyMessageDelayed(MSG_PAUSE_CONTINUE, Constants.Delayed.TIME2);
				break;
			default:
				break;
		}
	}

}
