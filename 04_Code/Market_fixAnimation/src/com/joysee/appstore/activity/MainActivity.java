package com.joysee.appstore.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.json.JSONException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CacheManager;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import com.joysee.appstore.R;
import com.joysee.appstore.activity.ClassActivity.DownReceiver;
import com.joysee.appstore.animation.AnimUtils;
import com.joysee.appstore.animation.MoveControl;
import com.joysee.appstore.common.AppsBean;
import com.joysee.appstore.common.Constants;
import com.joysee.appstore.common.PageBean;
import com.joysee.appstore.common.TaskBean;
import com.joysee.appstore.db.DBUtils;
import com.joysee.appstore.parser.AppsParser;
import com.joysee.appstore.parser.MenusParser;
import com.joysee.appstore.thread.LoadAppThread;
import com.joysee.appstore.utils.AppLog;
import com.joysee.appstore.utils.BitmapCache;
import com.joysee.appstore.utils.CaCheManager;
import com.joysee.appstore.utils.NetUtil;
import com.joysee.appstore.utils.RequestParam;
import com.joysee.appstore.utils.Utils;

public class MainActivity extends BaseActivity implements View.OnClickListener,View.OnFocusChangeListener{
	
	private ArrayList<TextView> starArray=new ArrayList<TextView>();
	private List <Map<String, Object>> mMenuList=new ArrayList<Map<String, Object>>();
	private List<AppsBean> mRecommendApps;//推荐
	private List<AppsBean> mLatestApps;//最新
	
	private LoadAppThread mThread;
	private LatestThread mThreadLatest;
	
	private ImageView all_app;
	private ImageView all_queen;
	private RelativeLayout mMoreLayout;
	private ImageView titleImage;
	private ImageView search;
	private EditText edit;
	
	private AppReceiver appReceiver;
	
	private static final int MSG_NET_ERROR=1;
	private static final int MSG_STOP_PROGRESS=2;
	
 	private TextView mDownloadRecord;
 	private TextView mSearch;
 	private Boolean isSearchButton = false;
    private ViewFlipper mViewFlipper;
    private GridLayout  mGridLayout;
    private LayoutInflater  mLayoutInflater;
    private LinearLayout mMenuLinearLayout;
     
    private DBUtils mDBUtils;
    private NetUtil mNetUtil;
    
    private  TextView pageView1;
    private  TextView pageView2;
    private  TextView pageView3;
    private  TextView pageView4;
    private  TextView pageView5;
    private  ImageView pageSearch;
    
    private int mLoadNum=0;    //当加载完菜单，应用时，2才会取消进度条
    private int pageNumPage=-1;
    private int pageTotal=0;   //页面实际总页数
    private PageBean mPageBean;
    private LoadMenuTask mLoadMenuTask = null;
    private AlertDialog urlInputDialog; 
    
    
    private boolean isHasSoftKey = false; //是否有软键盘
    
    private static final int URLINPUT_DIALOG = 4;
    private EditText mAppUrlEditor,mFileUrlEditor;
	public static final String TAG = "com.joysee.appstore.MainActivity";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppLog.d(TAG," onCreate ");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dalvik.system.VMRuntime.getRuntime().setMinimumHeapSize(16*1024*1024);
        setContentView(R.layout.app_store_layout);	
        parent=(ViewGroup)this.findViewById(R.id.root_relativelayout);
        mPageBean=new PageBean();
        mPageBean.setPageSize(40);
        mDBUtils=new DBUtils(MainActivity.this);
        mNetUtil = new NetUtil(MainActivity.this);
        setupViews();
        viewGroupFocus=parent.getDescendantFocusability();
        parent.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        AppLog.d(TAG, "--------new LoadMenuTask().execute()----");
        if (mLoadMenuTask != null && mLoadMenuTask.getStatus() != LoadMenuTask.Status.FINISHED) {
        	mLoadMenuTask.cancel(true);
        }        
        mLoadMenuTask = (LoadMenuTask) new LoadMenuTask().execute();
        mThread=new LoadAppThread(MainActivity.this,mPageBean,RequestParam.Action.GETRECOMMENDLIST);
        mThread.setPriority(Thread.MIN_PRIORITY);
        mThread.start();
        AppLog.d(TAG, "--------end----");
        startProgressDialog();
        IntentFilter filter=new IntentFilter();
        filter.addAction(Constants.INTENT_INSTALL_COMPLETED);
        filter.addAction(Constants.INTENT_UNINSTALL_COMPLETED);
        filter.addAction(Constants.INTENT_UPDATE_COMPLETED);
        filter.addAction(Constants.INTENT_DOWNLOAD_PROGRESS);
        filter.addAction(Constants.INTENT_DOWNLOAD_STARTED);
        
        filter.addAction(Constants.INTENT_DOWNLOAD_DETLET);
        filter.addAction(Constants.INTENT_INSTALL_FAIL);
        filter.addAction(Constants.INTENT_DOWNLOAD_ERROR);
        filter.addAction(Constants.INTENT_DOWNLOAD_PAUSE);
        appReceiver=new AppReceiver();
        this.registerReceiver(appReceiver, filter);
	}
	
//	 private void initializeByIntent() {
//		 Intent intent = getIntent();
//	     String action = intent.getAction();
//
//	     if (Intent.ACTION_GET_CONTENT.equalsIgnoreCase(action)) {
//	        	
//	     }
//	 }
	 
	private void setupViews() {
		mDownloadRecord = (TextView) this.findViewById(R.id.top_down_record);
		mSearch = (TextView) this.findViewById(R.id.top_search);
		mImageView = (ImageView) this.findViewById(R.id.animation_imageview);
		mViewFlipper = (ViewFlipper) this.findViewById(R.id.app_store_viewflipper);
		control=new MoveControl(this,mImageView);
		mMenuLinearLayout = (LinearLayout) this.findViewById(R.id.app_store_menu_layout);
		mMoreLayout=(RelativeLayout)this.findViewById(R.id.top2);
		titleImage=(ImageView)this.findViewById(R.id.top2_more);
		mDownloadRecord.setOnClickListener(this);
		mDownloadRecord.setTag("mDownloadRecord");
		mSearch.setOnClickListener(this);
		mSearch.setTag("mSearch");
		mSearch.setOnFocusChangeListener(this);
		mDownloadRecord.setOnFocusChangeListener(this);
//		mMoreLayout.setOnFocusChangeListener(this);
		mMoreLayout.setTag("mMoreLayout");
//		mSearch.setNextFocusDownId(mMoreLayout.getId());
//		mDownloadRecord.setNextFocusDownId(mMoreLayout.getId());
		
		titleImage.setImageResource(R.drawable.new_title);
		mMoreLayout.setNextFocusUpId(mSearch.getId());
		mMoreLayout.setOnKeyListener(new View.OnKeyListener(){
			
			public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
				if(arg2.getAction() == KeyEvent.ACTION_DOWN&&arg2.getKeyCode()==KeyEvent.KEYCODE_DPAD_RIGHT){
					nextFlipperPage(true);
					return true;
				}else if(arg2.getAction() == KeyEvent.ACTION_DOWN&&arg2.getKeyCode()==KeyEvent.KEYCODE_DPAD_LEFT){
					previousFlipperPage(true);
					return true;
				}
				return false;
			}
		});
		
		pageNumPage = 0;
		initPage();
		pageSearch=(ImageView)this.findViewById(R.id.home_search2);
		mLayoutInflater = this.getLayoutInflater();
	}
	
	public Handler mHandler=new Handler(){
		public void handleMessage(Message msg) {
            switch(msg.what){
            case MSG_NET_ERROR:
            	startNetErrorDialog();
            	break;
            case MSG_STOP_PROGRESS:
            	stopProgressDialog();
            	break;
            }
		}
	};
	 
	public void startDefaultPage() {
	    setupViews();
	}
	
    public void refreshMenusList(List<AppsBean> menuList){
    	parent.setDescendantFocusability(viewGroupFocus);
    	if(null==menuList){
    		AppLog.d(TAG, "-----------refreshMenusList---------menuList=null");
    		mHandler.sendEmptyMessage(MSG_NET_ERROR);
    		return;
    	}
    	mLoadNum++;
    	if(mLoadNum>=2){
    		mHandler.sendEmptyMessage(MSG_STOP_PROGRESS);
    	}
    	getMenuData(menuList);
    	initMenuLayout();
	}
    
    /**
     * @param appList
     */
    public void refreshAppsList(List<AppsBean> appList){
    	parent.setDescendantFocusability(viewGroupFocus);
    	mLoadNum++;
    	if(mLoadNum>=2){
    		mHandler.sendEmptyMessage(MSG_STOP_PROGRESS);
    	}
    	if(null==appList){
    		mHandler.sendEmptyMessage(MSG_NET_ERROR);
    		mSearch.setFocusable(true);
        	mSearch.setAlpha(AnimUtils.VIEW_ALPHA_FLAG);
        	mSearch.setFocusableInTouchMode(true);
        	mSearch.requestFocus();
    		return;
    	}
    	mRecommendApps=appList;
    	makeAllPage();
    }
    
    public void latestAppsList(List<AppsBean> appList){
    	if(appList==null){
    		mHandler.sendEmptyMessage(MSG_NET_ERROR);
    		return;
    	}
    	mLatestApps=appList;
    	AppLog.d(TAG, "-----------------------------mLatestApps.size="+mLatestApps.size());
    	mThread=new LoadAppThread(MainActivity.this,mPageBean,RequestParam.Action.GETRECOMMENDLIST);
        mThread.start();
    }
    ImageView requestBut;
    public void makeAllPage(){
    	titleImage.setVisibility(View.VISIBLE);
    	int mostNum=mRecommendApps.size();
    	AppLog.d(TAG, "--------------mostNum="+mostNum);
    	int allPage=mostNum%8==0?mostNum/8:mostNum/8+1;
    	AppLog.d(TAG, "-------------------allPage="+allPage);
    	for(int i=0;i<allPage;i++){
    		pageTotal++;
    		AppLog.d(TAG, "-------------------current="+i);
    		GridLayout layout = new GridLayout(this);
            layout.setColumnCount(4);
            layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
    		AppLog.d(TAG, "---------------------00");
    		if(mRecommendApps!=null){
    			for(int p=0;p<8;p++){
        			if((i*8+p+1)<=mRecommendApps.size()){
        				layout.addView(makeOneItem(p,mRecommendApps.get(i*8+p)));
        			}
        		}
    		}
    		mViewFlipper.addView(layout);
    		int currentPageSize=layout.getChildCount();
    		if(currentPageSize==1){
    			ImageView button=(ImageView) layout.getChildAt(0).findViewById(R.id.home_item_img);
    			button.setNextFocusLeftId(-1);
    			button.setOnKeyListener(new LeftAndRightListener());
    		}else if(currentPageSize<5){
    			ImageView button1=(ImageView) layout.getChildAt(0).findViewById(R.id.home_item_img);
    			button1.setNextFocusLeftId(-1);
    			button1.setOnKeyListener(new LeftListener());
    			ImageView button2=(ImageView) layout.getChildAt(currentPageSize-1).findViewById(R.id.home_item_img);
    			button2.setNextFocusLeftId(-1);
    			button2.setOnKeyListener(new RightListener());
    		}else {
    			ImageView button1=(ImageView) layout.getChildAt(0).findViewById(R.id.home_item_img);
    			button1.setNextFocusLeftId(-1);
    			button1.setOnKeyListener(new LeftListener());
    			ImageView button2=(ImageView) layout.getChildAt(3).findViewById(R.id.home_item_img);
    			button2.setNextFocusLeftId(-1);
    			button2.setOnKeyListener(new RightListener());
    			ImageView button3=(ImageView) layout.getChildAt(4).findViewById(R.id.home_item_img);
    			button3.setNextFocusLeftId(-1);
    			button3.setOnKeyListener(new LeftListener());
    			ImageView button4=(ImageView) layout.getChildAt(currentPageSize-1).findViewById(R.id.home_item_img);
    			button4.setNextFocusLeftId(-1);
    			button4.setOnKeyListener(new RightListener());
    		}
    		if(pageTotal==1){
    			requestBut=(ImageView) layout.getChildAt(0).findViewById(R.id.home_item_img);
    		}
    	}
        addSearchViewFillper();
        setPage(pageTotal, pageNumPage);
        if(requestBut!=null){
        	requestBut.setFocusable(true);
        	requestBut.setAlpha(AnimUtils.VIEW_ALPHA_FLAG);
        	requestBut.setFocusableInTouchMode(true);
        	requestBut.requestFocus();
        }else{
        	mSearch.setFocusable(true);
        	mSearch.setAlpha(AnimUtils.VIEW_ALPHA_FLAG);
        	mSearch.setFocusableInTouchMode(true);
        	mSearch.requestFocus();
        }
    }
    
    public View makeOneItem(int index,AppsBean appsBean){
  	    View view = mLayoutInflater.inflate(R.layout.home_item_layout, null);
    	ImageView button = (ImageView) view.findViewById(R.id.home_item_img);
    	TextView itemtext = (TextView)view.findViewById(R.id.item_name);
    	TextView itemcomment = (TextView)view.findViewById(R.id.home_item_comment);
    	RatingBar bar = (RatingBar)view.findViewById(R.id.home_rating_bar);
        ImageView popIV = (ImageView)view.findViewById(R.id.home_item_pop);
        ImageView popTvT = (ImageView)view.findViewById(R.id.pop_update);
    	bar.setFocusable(false);
//		if(index < 4){
//    		view.setNextFocusUpId(mMoreLayout.getId());
//    		button.setNextFocusUpId(mMoreLayout.getId());
//    	}
    	String soc=appsBean.getScore()==null?"0":appsBean.getScore().trim();
		float res=Integer.valueOf(soc.equals("")?"0":soc);
		res = res / 2;
		bar.setRating((float)res);
		button.setFocusable(true);
    	button.setOnClickListener(this);
    	button.setOnFocusChangeListener(this);
    	button.setTag(appsBean);
    	if(null==appsBean.getNatImageUrl()||appsBean.getNatImageUrl().equals("")){
    		button.setImageResource(R.drawable.app_default_img);
		}else{
//			button.setImageURI(Uri.parse(appsBean.getNatImageUrl()));
			Bitmap bitmap = CaCheManager.requestBitmap(appsBean.getNatImageUrl());
			if(bitmap!=null){
				button.setImageBitmap(bitmap);
			}else{
				button.setImageResource(R.drawable.app_default_img);
				AppLog.e(TAG, "---bitmap==null   setDefault---");
			}
		}
    	ProgressBar progress=(ProgressBar)view.findViewById(R.id.down_progress);
    	itemtext.setText(appsBean.getAppName());
    	double score=appsBean.getPrice();
    	int appStatus = mDBUtils.queryStatusByPkgName(appsBean.getPkgName());
		switch(appStatus){
		case Constants.APP_STATUS_DOWNLOADING:
		case Constants.DOWNLOAD_STATUS_ERROR:
		case Constants.DOWNLOAD_STATUS_PAUSE:
        case Constants.DOWNLOAD_STATUS_EXIT_STOP:
        	int appSts=mDBUtils.queryStatusByPkgNameAndVersion(appsBean.getPkgName(),appsBean.getVersion());
        	AppLog.d(TAG, "-----------------------appSts="+appSts);
        	switch(appSts){
            	case Constants.APP_STATUS_DOWNLOADING:
    			case Constants.DOWNLOAD_STATUS_ERROR:
    			case Constants.DOWNLOAD_STATUS_PAUSE:
                case Constants.DOWNLOAD_STATUS_EXIT_STOP:
                	if(appStatus==Constants.DOWNLOAD_STATUS_ERROR){
                		itemcomment.setText(""+MainActivity.this.getString(R.string.download_error));
                	}else if(appStatus==Constants.DOWNLOAD_STATUS_PAUSE){
                		itemcomment.setText(""+MainActivity.this.getString(R.string.pause_download));
                	}else{
                		itemcomment.setText(""+MainActivity.this.getString(R.string.download_tab_2));
                	}
                	progress.setVisibility(View.VISIBLE);
                	int sumSize=mDBUtils.querySumSizeByPkgName(appsBean.getPkgName());
                	int downSize=mDBUtils.queryDowningSizeByPkgName(appsBean.getPkgName());
                	if(sumSize==0){
                		progress.setProgress(0);
                	}else{
                		long down=downSize;
                    	long sum=sumSize;
                    	int resPro=(int)(down*100/sum);
                    	progress.setProgress(resPro);
                	}
                	break;
                default:
                	int appS=mDBUtils.queryStatusByPkgNameFormApplication(appsBean.getPkgName());
                	if(appS==Constants.APP_STATUS_UPDATE||appS==Constants.APP_STATUS_INSTALLED){
                		itemcomment.setText(""+MainActivity.this.getString(R.string.download_tab_3));
                		//对比已安装的应用的新旧版本
	                	String newVersion = appsBean.getVersion(); 
	                	String oldVersion = mDBUtils.queryVersionByPkgName(appsBean.getPkgName());
	                	AppLog.d(TAG, "-----------------newVersion :"+newVersion +"| -------oldVersion :" +oldVersion);
	                	if(Utils.CompareVersion(newVersion, oldVersion)){
	                		popIV.setVisibility(View.VISIBLE);
	                		popTvT.setVisibility(View.VISIBLE);
	                	}
                	}else if(appS==Constants.APP_STATUS_UNDOWNLOAD){
                		itemcomment.setText(""+MainActivity.this.getString(R.string.noinstalled));
                	}
                	bar.setVisibility(View.VISIBLE);
                	break;
        	}
        	break;
        case Constants.APP_STATUS_DOWNLOADED: 
        	itemcomment.setText(""+MainActivity.this.getString(R.string.download_tab_2));
        	progress.setVisibility(View.VISIBLE);
        	progress.setProgress(100);
        	popIV.setVisibility(View.GONE);
        	popTvT.setVisibility(View.GONE);
        	break;
        case Constants.APP_STATUS_INSTALLED://已安装
        	itemcomment.setText(""+MainActivity.this.getString(R.string.download_tab_3));
        	bar.setVisibility(View.VISIBLE);
        	//对比已安装的应用的新旧版本
        	String newVersion = appsBean.getVersion(); 
        	String oldVersion = mDBUtils.queryVersionByPkgName(appsBean.getPkgName());
        	AppLog.d(TAG, "-----------------newVersion :"+newVersion +"| -------oldVersion :" +oldVersion);
        	if(Utils.CompareVersion(newVersion, oldVersion)){
        		popIV.setVisibility(View.VISIBLE);
        		popTvT.setVisibility(View.VISIBLE);
        	}
        	break;
        default:
        	if(score<=0){
				//priceView.setText(""+ClassActivity.this.getString(R.string.app_free));
			}else{
				//priceView.setText(""+ClassActivity.this.getString(R.string.app_no_free,score));
			}
        	itemcomment.setText(""+MainActivity.this.getString(R.string.noinstalled));
        	bar.setVisibility(View.VISIBLE);
        	break;
		}
    	/*int status = mDBUtils.queryStatusByPkgNameFormApplication(appsBean.getPkgName());
    	switch(status){
    		case 4: //在升级
    		case Constants.APP_STATUS_DOWNLOADING:
    		case Constants.DOWNLOAD_STATUS_ERROR:
			case Constants.DOWNLOAD_STATUS_PAUSE:
				
    			break;
    		case 3: //已安装
    			itemcomment.setText(R.string.installed);
    			//对比已安装的应用的新旧版本
    			String newVersion = appsBean.getVersion();//------by xubin
    			String oldVersion = mDBUtils.queryVersionByPkgName(appsBean.getPkgName());
    			AppLog.d(TAG, "-----------------newVersion :"+newVersion +"| -------oldVersion :" +oldVersion);
    			if(newVersion!=null && Utils.CompareVersion(newVersion, oldVersion)){ 
    				popIV.setVisibility(View.VISIBLE);
    				popTvT.setVisibility(View.VISIBLE);
    			}
			default :
				break;
    			
    	}*/
        return view;
    }

	
	class LeftAndRightListener implements View.OnKeyListener{
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if(event.getAction()==KeyEvent.ACTION_DOWN&&keyCode==KeyEvent.KEYCODE_DPAD_RIGHT){
				nextFlipperPage(true);
				return true;
			}else if(event.getAction()==KeyEvent.ACTION_DOWN&&keyCode==KeyEvent.KEYCODE_DPAD_LEFT){
				previousFlipperPage(true);
				return true;
			}
			return false;
		}
	}
	
	class LeftListener implements View.OnKeyListener{
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if(event.getAction()==KeyEvent.ACTION_DOWN&&keyCode==KeyEvent.KEYCODE_DPAD_LEFT){
				previousFlipperPage(true);
				return true;
			}
			return false;
		}
	}
	class RightListener implements View.OnKeyListener{
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if(event.getAction()==KeyEvent.ACTION_DOWN&&keyCode==KeyEvent.KEYCODE_DPAD_RIGHT){
				nextFlipperPage(true);
				return true;
			}
			return false;
		}
	}
	
	class RightAndUPListener implements View.OnKeyListener{
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if(event.getAction()==KeyEvent.ACTION_DOWN&&keyCode==KeyEvent.KEYCODE_DPAD_RIGHT){
				nextFlipperPage(true);
				return true;
			}else if(event.getAction()==KeyEvent.ACTION_DOWN&&keyCode==KeyEvent.KEYCODE_DPAD_UP){
				mSearch.requestFocus();
				return true;
			}
			return false;
		}
	}
	
	public long pressTime;
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
//		AppLog.d(TAG, "---dispatchKeyEvent--event.getAction="+event.getAction()+";keyCode="+event.getKeyCode());
		int keyCode = event.getKeyCode();
		if(event.isAltPressed() && keyCode == KeyEvent.KEYCODE_Q && event.getAction() == KeyEvent.ACTION_UP){
			showDialog(URLINPUT_DIALOG);
			return true; 
		}
		
		if(event.getAction()==KeyEvent.ACTION_DOWN){
//			control.firstTime=System.currentTimeMillis();
    		if(event.getKeyCode()==KeyEvent.KEYCODE_DPAD_DOWN||event.getKeyCode()==KeyEvent.KEYCODE_DPAD_LEFT||
    				event.getKeyCode()==KeyEvent.KEYCODE_DPAD_RIGHT||event.getKeyCode()==KeyEvent.KEYCODE_DPAD_UP){
    			downTimes++;
    			requestFocusFirst(mSearch);
    			//表示是长按事件
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
//        		AppLog.d(TAG, "mMoreLayout.hasFocus()="+mMoreLayout.hasFocus());
        		if(mMoreLayout.hasFocus()){//推荐位时快按或长按
        			if(mMoveDirect==View.FOCUS_LEFT){
        				long currentTime=System.currentTimeMillis();
        				if((currentTime-pressTime)>AnimUtils.ANIMATION_PAGE_TIME){
        					this.previousFlipperPage(true);
        					pressTime=currentTime;
        				}
        				return true;
        			}else if(mMoveDirect==View.FOCUS_RIGHT){
        				long currentTime=System.currentTimeMillis();
        				if((currentTime-pressTime)>AnimUtils.ANIMATION_PAGE_TIME){
        					this.nextFlipperPage(true);
        					pressTime=currentTime;
        				}
        				return true;
        			}
        		}
        		View nextview=mFocusF.findNextFocus(parent, this.getCurrentFocus(),mMoveDirect);
//        		AppLog.d(TAG, "nextView="+nextview+";mMoveDirect="+mMoveDirect);
        		if(mViewFlipper!=null&&mViewFlipper.getChildCount()>1){
        			if(nextview==null){
            			if(mMoveDirect==View.FOCUS_LEFT&&AppsBean.class.isInstance(this.getCurrentFocus().getTag())&&control.focusQueue.size()==0){
            				this.previousFlipperPage(true);
            			}
            			if(mMoveDirect==View.FOCUS_LEFT&&String.class.isInstance(this.getCurrentFocus().getTag())&&((String)this.getCurrentFocus().getTag()).equals("edit")&&control.focusQueue.size()==0){
            				this.previousFlipperPage(true);
            			}
            			isRunning=false;
            			return true;
            		}else{
//            			AppLog.d(TAG, "---nextview.tag="+nextview.getTag());
            			if(AppsBean.class.isInstance(this.getCurrentFocus().getTag())&&String.class.isInstance(nextview.getTag())){//当前为推荐位页面的翻页
            				if(mMoveDirect==View.FOCUS_RIGHT&&(((String)nextview.getTag()).equals("mDownloadRecord")||((String)nextview.getTag()).equals("mSearch"))){
            					if(control.focusQueue.size()==0){
            						AppLog.d(TAG, "------1111");
            						this.nextFlipperPage(true);
            					}
            					isRunning=false;
                    			return true;
            				}
            			}
            			if(String.class.isInstance(this.getCurrentFocus().getTag())){//当前为搜索页面的翻页
            				if(mMoveDirect==View.FOCUS_RIGHT&&((String)this.getCurrentFocus().getTag()).equals("search")){
            					if(control.focusQueue.size()==0){
            						this.nextFlipperPage(true);
            					}
            					isRunning=false;
                				return true;
            				}else if(mMoveDirect==View.FOCUS_LEFT&&((String)this.getCurrentFocus().getTag()).equals("edit")){
            					if(control.focusQueue.size()==0){
            						this.previousFlipperPage(true);
            					}
            					isRunning=false;
                				return true;
            				}
            			}
            		}
        		}
        		//长按事件
    			if(downTimes>1&&!isRunning){
        			isRunning=true;
        			parent.post(mRepeater);
        		}
        		if(isRunning){
    				return true;
    			}
        	
    		}
    	}else if(event.getAction()==KeyEvent.ACTION_UP){
    		downTimes=0;
    		isRunning=false;
    		control.ANIMATION_MOVE_TIME=AnimUtils.ANIMATION_MOVE_SLOW;
    	}
		return super.dispatchKeyEvent(event);
	}
	
	/**
     * 下一页
     */
    public void nextPage(){
        if(!mPageBean.nextPage()){//表示没有下一页
        	addSearchViewFillper();
        	setPage(pageTotal, pageNumPage);
        	stopProgressDialog();
            return;
        }
        if(mPageBean.getPageNo()>mPageBean.getPageTotal()){//防止网络错误，一直连接
        	stopProgressDialog();
        	return;
        }
        mThread=new LoadAppThread(MainActivity.this,mPageBean,RequestParam.Action.GETRECOMMENDLIST);
        mThread.start();
    }
    
    /**
     * 上一页
     */
    public void previousPage(){
        if(!mPageBean.previousPage()){//表示没有上一页
            return;
        }
        mThread=new LoadAppThread(MainActivity.this,mPageBean,RequestParam.Action.GETRECOMMENDLIST);
        mThread.start();
    }
    
    /**
     * 进入下载管理 
     */
    public void downloadMgr() {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, DownloadRecordActivity.class);
        startActivity(intent);
    }
    
	public void nextFlipperPage(boolean fastMode){
		AppLog.d(TAG, "nextFlipperPage");
		if(fastMode){
			mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.left_in));
			mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.left_out));
		}else{
			mViewFlipper.setInAnimation(null);
			mViewFlipper.setOutAnimation(null);
		}
		parent.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		AppLog.d(TAG, "--------showNext begin");
		mViewFlipper.showNext();
		AppLog.d(TAG, "--------showNext end");
		parent.setDescendantFocusability(viewGroupFocus);
		if(!mMoreLayout.hasFocus()){
			View currentView=mViewFlipper.getCurrentView();
			if(GridLayout.class.isInstance(currentView)){
				GridLayout tempGrid = (GridLayout) currentView;
				ImageView button = (ImageView) tempGrid.getChildAt(0).findViewById(R.id.home_item_img);
				if(button != null){
					button.setFocusable(true);
					button.setFocusableInTouchMode(true);
					if(fastMode){
						button.setAlpha(AnimUtils.VIEW_ALPHA_FLAG);
					}
					button.requestFocus();
				}else{
					control.mDelay=false;
				}
				titleImage.setImageResource(R.drawable.new_title);
				pageSearch.setBackgroundResource(R.drawable.home_search);
			}else{
				if(edit!=null){
					edit.setFocusable(true);
					edit.setFocusableInTouchMode(true);
					if(fastMode){
						edit.setAlpha(AnimUtils.VIEW_ALPHA_FLAG);
					}
					edit.requestFocus();
				}else{
					control.mDelay=false;
				}
				titleImage.setImageResource(R.drawable.searchtitle1);
				pageSearch.setBackgroundResource(R.drawable.home_search_big);
			}
		}else{
			View currentView=mViewFlipper.getCurrentView();
			if(GridLayout.class.isInstance(currentView)){
				titleImage.setImageResource(R.drawable.new_title);
				pageSearch.setBackgroundResource(R.drawable.home_search);
			}else{
				titleImage.setImageResource(R.drawable.searchtitle1);
				pageSearch.setBackgroundResource(R.drawable.home_search_big);
			}
			control.mDelay=false;
			mImageView.setVisibility(View.VISIBLE);
		}
		pageNumPage++;
		if(pageNumPage >pageTotal){
			pageNumPage = 0;
		}
		setPage(pageTotal, pageNumPage);
	}
	
	
	
	public void previousFlipperPage(boolean fastMode){
		control.mDelay=true;
		AppLog.d(TAG, "previousFlipperPage");
		if(fastMode){
			mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.left_in));
			mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.left_out));
		}else{
			mViewFlipper.setInAnimation(null);
			mViewFlipper.setOutAnimation(null);
		}
		parent.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		AppLog.d(TAG, "--------showPrevious begin");
		mViewFlipper.showPrevious();
		AppLog.d(TAG, "--------showPrevious end");
		parent.setDescendantFocusability(viewGroupFocus);
		if(!mMoreLayout.hasFocus()){
			View currentView=mViewFlipper.getCurrentView();
			if(GridLayout.class.isInstance(currentView)){
				GridLayout tempGrid = (GridLayout) currentView;
				ImageView button;
				if(tempGrid.getChildCount()<4){
					button = (ImageView) tempGrid.getChildAt(tempGrid.getChildCount()-1).findViewById(R.id.home_item_img);
				}else{
					button = (ImageView) tempGrid.getChildAt(3).findViewById(R.id.home_item_img);
				}
				if(button != null){
					button.setFocusable(true);
					button.setFocusableInTouchMode(true);
					if(fastMode){
						button.setAlpha(AnimUtils.VIEW_ALPHA_FLAG);
					}
					button.requestFocus();
				}else{
					control.mDelay=false;
				}
				titleImage.setImageResource(R.drawable.new_title);
				pageSearch.setBackgroundResource(R.drawable.home_search);
			}else{
				if(search!=null){
					search.setFocusable(true);
					search.setFocusableInTouchMode(true);
					if(fastMode){
						search.setAlpha(AnimUtils.VIEW_ALPHA_FLAG);
					}
					search.requestFocus();
				}else{
					control.mDelay=false;
				}
				titleImage.setImageResource(R.drawable.searchtitle1);
				pageSearch.setBackgroundResource(R.drawable.home_search_big);
			}
		}else{
			View currentView=mViewFlipper.getCurrentView();
			if(GridLayout.class.isInstance(currentView)){
				titleImage.setImageResource(R.drawable.new_title);
				pageSearch.setBackgroundResource(R.drawable.home_search);
			}else{
				titleImage.setImageResource(R.drawable.searchtitle1);
				pageSearch.setBackgroundResource(R.drawable.home_search_big);
			}
			control.mDelay=false;
			mImageView.setVisibility(View.VISIBLE);
		}
		pageNumPage--;
		if(pageNumPage <0){
			pageNumPage = pageTotal;
		}
		setPage(pageTotal, pageNumPage);
	}
    
    public void initPage(){
     	starArray.clear();
     	pageView1= (TextView) findViewById(R.id.home_page1);
     	pageView2= (TextView) findViewById(R.id.home_page2);
     	pageView3= (TextView) findViewById(R.id.home_page3);
     	pageView4= (TextView) findViewById(R.id.home_page4);
     	pageView5= (TextView) findViewById(R.id.home_page5);
     	starArray.add(pageView5);
     	starArray.add(pageView4);
     	starArray.add(pageView3);
     	starArray.add(pageView2);
     	starArray.add(pageView1);
    }

	public void setPage(int pageTotal,int pageNum){
//		AppLog.d(TAG, "pageNum="+pageNum+"---pageTotal="+pageTotal);
	   for(int i=0;i<pageTotal;i++){
		   if(pageNum==i){
			   TextView currentPageView= starArray.get(i);
			   String mNum=""+(i+1)+"";
			   currentPageView.setText(mNum);
			   currentPageView.setBackgroundResource(R.drawable.home_page_big);
		   }else{     
			   starArray.get(i).setTop(20);
			   starArray.get(i).setText("");
			   starArray.get(i).setBackgroundResource(R.drawable.home_switch_small);
		   }
		   
	   }
	}
	
	public void addSearchViewFillper(){
    	View view = mLayoutInflater.inflate(R.layout.app_search_layout, null);

    	edit = (EditText) view.findViewById(R.id.search_edittext);
    	search = (ImageView) view.findViewById(R.id.search_button_search);
        all_app = (ImageView)view.findViewById(R.id.search_all_app);
        all_queen = (ImageView)view.findViewById(R.id.search_all_queen);
        edit.setTag("edit");
    	edit.setOnFocusChangeListener(this);
    	search.setOnFocusChangeListener(this);
    	all_app.setOnFocusChangeListener(this);
    	all_queen.setOnFocusChangeListener(this);
    	search.setTag("search");
    	all_app.setTag("all_app");
    	all_queen.setTag("all_queen");
    	edit.setFocusable(true);
    	search.setFocusable(true);
    	all_app.setFocusable(true);
    	all_queen.setFocusable(true);
    	
    	edit.setNextFocusUpId(mMoreLayout.getId());
    	edit.setNextFocusLeftId(-1);
    	edit.setNextFocusRightId(search.getId());
    	search.setNextFocusUpId(mMoreLayout.getId());
    	search.setNextFocusRightId(-1);
    	
    	all_app.setOnClickListener(this);
    	all_queen.setOnClickListener(this);
    	search.setOnClickListener(this);
    	all_queen.setOnKeyListener(new RightListener());
    	all_app.setOnKeyListener(new LeftListener());
    	edit.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				AppLog.d(TAG, "-------------------edit------------keyCode="+keyCode);
				if(keyCode==KeyEvent.KEYCODE_ENTER||keyCode==KeyEvent.KEYCODE_DPAD_CENTER){
					InputMethodManager im = ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE));
					im.showSoftInput(edit,0);
					isHasSoftKey = true;
					return true;
				}
				if(keyCode==KeyEvent.KEYCODE_ESCAPE||keyCode==KeyEvent.KEYCODE_BACK){
					if(isHasSoftKey){
						isHasSoftKey=false;
						InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
						imm.hideSoftInputFromWindow(edit.getWindowToken(),0);
						return true;
					}
				}
				if(event.getAction()==KeyEvent.ACTION_DOWN&&keyCode==KeyEvent.KEYCODE_DPAD_LEFT){
					previousFlipperPage(true);
					return true;
				}
				return false;
			}
		});
    	search.setOnKeyListener(new RightListener());
    	pageSearch.setVisibility(View.VISIBLE);
    	mViewFlipper.addView(view);
	}
	
    View viewLeft;
    View viewRight;
    public void initMenuLayout(){
    	mMenuLinearLayout.removeAllViews();
    	AppLog.d(TAG, "-------------"+mMenuList.size());
    	for(int i=0;i<mMenuList.size();i++){
    		View view = mLayoutInflater.inflate(R.layout.menu_item_layout, null);
    		view.setTag(i);
    		view.setOnClickListener(this);
    		view.setOnFocusChangeListener(this);
    		ImageView button = (ImageView) view.findViewById(R.id.menu_item_img);
    		TextView nameText=(TextView) view.findViewById(R.id.menu_item_text);
    		Map<String, Object> map = mMenuList.get(i);
    		if(i<3){
    			button.setImageResource((Integer)map.get("image"));
    		}else{
    			if(map.get("image")==null||map.get("image").equals("")){
    				button.setImageResource(R.drawable.menu_default_img);
    			}else{
//    				button.setImageURI(Uri.parse((String) map.get("image")) );
    				Bitmap bp = CaCheManager.requestBitmap((String)map.get("image"));
    				if(null!=bp){
    					button.setImageBitmap(bp);
    				}else{
    					button.setImageResource(R.drawable.menu_default_img);
    				}
    			}
    		}
    		if(i==0){
    			viewLeft = view;
    			viewLeft.setId(Constants.MENU_LEFT_ID);
    		}
    		if(i==mMenuList.size()-1){
    			viewRight = view;
    			viewRight.setId(Constants.MENU_RIGHT_ID);
    		}
    		String name = (String) map.get("name");
    		nameText.setText(name+"");
    		view.setFocusable(true);
        	mMenuLinearLayout.addView(view);
    	}
    	viewLeft.setNextFocusLeftId(viewRight.getId());
    	viewRight.setNextFocusRightId(viewLeft.getId());
    	
    	int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        mMenuLinearLayout.measure(w, h);
        int width = mMenuLinearLayout.getMeasuredWidth();
        LayoutParams lp1 = mMenuLinearLayout.getLayoutParams();
        lp1.width = width;
    }
    
    /**
     * 分类上的数据
     * @return
     */
    private  void getMenuData(List<AppsBean> menuList) { 
        Map<String, Object> map=new HashMap<String, Object>();
        mMenuList.clear();
        map= new HashMap<String, Object>();
        map.put("name", getString(R.string.extend_all_app));
        map.put("image",R.drawable.iconup);
        map.put("id", (Integer)RequestParam.CalssID.ALL_ID);
        mMenuList.add(map);
        map= new HashMap<String, Object>();
        map.put("name", getString(R.string.new_app));
        map.put("image",R.drawable.ico_new);
        map.put("id", (Integer)RequestParam.CalssID.NEW_ID);
        mMenuList.add(map);
        map= new HashMap<String, Object>();
        map.put("name", getString(R.string.hot_app));
        map.put("image",R.drawable.ico_hot);
        map.put("id", (Integer)RequestParam.CalssID.HOT_ID);
        mMenuList.add(map);
		for (AppsBean tAppsBean : menuList) {
			map = new HashMap<String, Object>();
			map.put("name", tAppsBean.getAppName());
			map.put("image", tAppsBean.getNatImageUrl());
			map.put("id", (Integer)tAppsBean.getID());
			mMenuList.add(map);
		}
        AppLog.d(TAG, "---------menuAdpaterList.size="+menuList.size());
    }
	 
	private class LoadMenuTask extends AsyncTask<Object, Void, List<AppsBean>>{
		List<AppsBean> tMenuList;
		@Override
		protected List<AppsBean> doInBackground(Object... params) {
			AppLog.d(TAG, "----------LoadMenuTask-----doInBackground-------");
			String urlStr=RequestParam.getAppServerUrl(MainActivity.this)+RequestParam.Action.GETAPPTYPELIST;
			tMenuList=(List<AppsBean>)mNetUtil.getNetData(null, urlStr, null, new MenusParser());
			if(tMenuList!=null){
				for(int i=0;i<tMenuList.size();i++){
					boolean res=mNetUtil.loadImage(tMenuList.get(i).getSerImageUrl(), tMenuList.get(i).getNatImageUrl());
					if(!res){
						tMenuList.get(i).setNatImageUrl(null);
					}
				}
			}
			return null;
		}
		@Override
		protected void onPostExecute(List<AppsBean> result) {
			refreshMenusList(tMenuList);
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
	
	public class LatestThread extends Thread{
		
		private final static String TAG="com.joysee.appstore.MainActivity.LatestThread";
		
		private PageBean mPageBean;
		private String mAction=RequestParam.Action.GETRECOMMENDLIST;
		private boolean flag=true;
		
		public LatestThread(PageBean pageBean,String action){
			mPageBean=pageBean;
			if(null!=action){
				mAction=action;
			}
		}
		public void run(){
			Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
			AppLog.d(TAG, "----------LoadAppsThread-----run-------");
			List<AppsBean> tAppList=null;
			String urlStr=RequestParam.getAppServerUrl(MainActivity.this)+mAction;
			Map<String,String> tParam=new TreeMap<String,String>();
			if(null!=mPageBean){ //TODO  this need to set userID and groudID to param
				tParam.put(RequestParam.Param.PAGENO,String.valueOf(mPageBean.getPageNo()));
				tParam.put(RequestParam.Param.LINENUMBER, String.valueOf(mPageBean.getPageSize()));
			}
			if(flag){
				Object obj=mNetUtil.getNetData(tParam, urlStr, mPageBean, new AppsParser(MainActivity.this));
				if(null==obj){//网络连接有误
					tAppList=null;
				}else{
					tAppList=(List<AppsBean>)obj;
				}
			}
			if(tAppList!=null){
				AppLog.d(TAG, "--------LatestThread.size="+tAppList.size());
				for(int i=0;i<tAppList.size();i++){
					if(flag){
						boolean res=mNetUtil.loadImage(tAppList.get(i).getSerImageUrl(), tAppList.get(i).getNatImageUrl());
						if(!res){
							tAppList.get(i).setNatImageUrl(null);
						}
					}
				}
			}
			final List<AppsBean> fAppList = tAppList;
			if(flag){
				latestAppsList(fAppList);
			}
			AppLog.d(TAG, "----------LatestThread-----end-------");
		}
		public boolean isFlag() {
			return flag;
		}
		public void setFlag(boolean flag) {
			this.flag = flag;
		}
		
	}
	
	

	 @Override
	protected Dialog onCreateDialog(int id) {
		 switch (id) {
			case URLINPUT_DIALOG:
				LinearLayout linearLayout = (LinearLayout) getLayoutInflater()
						.inflate(R.layout.url_dialog, null);
				urlInputDialog = new AlertDialog.Builder(this).setPositiveButton(getResources().getString(R.string.ok),
						new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
//						if(!Utils.isURL(mAppUrlEditor.getText().toString())){
//							Toast.makeText(getApplicationContext(), "url is error",1).show();
//							return;
//						}
//						if(!Utils.isURL(mFileUrlEditor.getText().toString())){
//							Toast.makeText(getApplicationContext(), "url is error",1).show();
//							return;
//						}
						saveConnectUrl(mAppUrlEditor.getText().toString(),mFileUrlEditor.getText().toString());
						dialog.cancel();
					}
				}).setNegativeButton(getResources().getString(R.string.cancel),
						new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).create();

				urlInputDialog.setView(linearLayout);
				mAppUrlEditor = (EditText) linearLayout.findViewById(R.id.appurl);
				mFileUrlEditor =  (EditText) linearLayout.findViewById(R.id.fileurl);
				urlInputDialog.setTitle(getResources().getString(
						R.string.seturltitle));
				urlInputDialog.setOnKeyListener(new OnKeyListener() {
					public boolean onKey(DialogInterface dialog, int keyCode,
							KeyEvent event) {

						if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
//							if(!Utils.isURL(mAppUrlEditor.getText().toString())){
//								Toast.makeText(getApplicationContext(), "url is error",1).show();
//							}
//							if(!Utils.isURL(mFileUrlEditor.getText().toString())){
//								Toast.makeText(getApplicationContext(), "url is error",1).show();
//							}
							saveConnectUrl(mAppUrlEditor.getText().toString(),mFileUrlEditor.getText().toString());
							return true;
						} 

						return false;
					}
				});
				urlInputDialog
						.setOnCancelListener(new OnCancelListener() {

							public void onCancel(DialogInterface dialog) {
								if (mAppUrlEditor != null) {
									mAppUrlEditor.setText("");
									mFileUrlEditor.setText("");
									mAppUrlEditor.requestFocus();
								}
							}
						});

				return urlInputDialog;
		 }
		return super.onCreateDialog(id);
	}

	public void saveConnectUrl(String appUrl,String fileUrl){
	    	 SharedPreferences userInfo = getSharedPreferences("url",  Context.MODE_PRIVATE);
	    	 if(null!=appUrl&&!"".equals(appUrl)){
	    		 userInfo.edit().putString("app", appUrl).commit();  
	    	 }
	    	 if(null!=fileUrl&&!"".equals(fileUrl)){
	    		 userInfo.edit().putString("file", fileUrl).commit();   
	    	 }
              
	    }

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		switch(v.getId()){
		case R.id.top_search:
		case R.id.top_down_record:
		case R.id.top2:
		case R.id.menu_item:
		case Constants.MENU_LEFT_ID:
		case Constants.MENU_RIGHT_ID:
		case R.id.search_edittext:
		case R.id.search_button_search:
		case R.id.search_all_app:
		case R.id.search_all_queen:
		case R.id.home_item_img:
			if(hasFocus){
				control.addFocusView(v);
			}
			break;
		}
	}
	
	public void onResume(){
		super.onResume();
		if(isSearchButton){
			AppLog.d(TAG, "-------------after AppSearchLayout : -----main onResume----------");
			search.requestFocus();
			//search.setBackImageView(mImageView);
//			control.transformAnimation(mImageView, search, search, this	, true, true);
			isSearchButton = false;
		}
		RelativeLayout mRootLayout = (RelativeLayout)findViewById(R.id.root_relativelayout);
		Drawable bg = getThemePaper();
        if (bg != null) {
            mRootLayout.setBackgroundDrawable(bg);
        } else {
            AppLog.d("getThemePaper() is null");
        }
	}
	
	public void onPause(){
		super.onPause();
	}
	
	
	
	@Override
	protected void onStart() {
		super.onStart();
	}

	public void onStop(){
		super.onStop();
		if (mLoadMenuTask != null && mLoadMenuTask.getStatus() != LoadMenuTask.Status.FINISHED) {
//        	mLoadMenuTask.cancel(true);
        }
		if(mThread!=null&&mThread.isAlive()){
//			mThread.setFlag(false);
		}
		if(mThreadLatest!=null&&mThreadLatest.isAlive()){
			mThreadLatest.setFlag(false);
		}
	}
	
	
	
	@Override
	protected void onDestroy() {
		AppLog.d(TAG,"---------enter MainActivity destory()------");
		super.onDestroy();
		new CaCheManager().refreshCaChe();
		if(null!=appReceiver){
        	this.unregisterReceiver(appReceiver);
        }
	}

	public void onClick(View v) {

		switch(v.getId()){
			case R.id.home_item_img:
				if(v.getTag()==null) return;
				AppsBean appB = (AppsBean) v.getTag();
				int type=appB.getID();
                Intent intent=new Intent(MainActivity.this,DetailedActivity.class);
                intent.putExtra("app_id",type);
                AppLog.d(TAG, "-----app_id="+intent.getIntExtra("app_id",-1));
                startActivity(intent);
//                overridePendingTransition(R.anim.zoomin,R.anim.zoomout);
//                this.finish();
				break;
			case R.id.top_down_record:
				downloadMgr();
				break;
			case R.id.menu_item://分类
			case Constants.MENU_LEFT_ID:
			case Constants.MENU_RIGHT_ID:
				if(v.getTag()==null) return;
				int position = (Integer) v.getTag();
	                AppLog.d(TAG, "111111111111111111");
	                Intent intent_class=new Intent(MainActivity.this,ClassActivity.class);
	                intent_class.putExtra("type_id",(Integer)mMenuList.get(position).get("id"));
	                startActivity(intent_class);
//	                overridePendingTransition(R.anim.zoomin,R.anim.zoomout);
//	                this.finish();
				break;
			case R.id.search_button_search:
				Intent intentSearch = new Intent(MainActivity.this,AppSearchActivity.class);
				intentSearch.putExtra("keyWord", edit.getText().toString());
				AppLog.d(TAG, "--------------keyword="+edit.getText());
				startActivity(intentSearch);
//				overridePendingTransition(R.anim.zoomin,R.anim.zoomout);
				isSearchButton = true;//点一次搜索,设标志,为从搜索返回时,判断焦点的落处
				break;
			case R.id.search_all_app:
				Intent all_app_intent = new Intent(MainActivity.this,ClassActivity.class);
				all_app_intent.putExtra("type_id",RequestParam.CalssID.ALL_ID);
				startActivity(all_app_intent);
//				overridePendingTransition(R.anim.zoomin,R.anim.zoomout);
//				this.finish();
				break;
			case R.id.search_all_queen:
				Intent all_queen_intent = new Intent(MainActivity.this,ClassActivity.class);
				all_queen_intent.putExtra("type_id",RequestParam.CalssID.HOT_ID);
				startActivity(all_queen_intent);
//				overridePendingTransition(R.anim.zoomin,R.anim.zoomout);
//				this.finish();
				break;
			case R.id.top_search:
				Intent intentTopSearch= new Intent(MainActivity.this,AppSearchActivity.class);
				startActivity(intentTopSearch);
				break;
			default :
				break;
		}
	}
	
	public class AppReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			AppLog.d(TAG, "--------DownReceiver-----------action="+arg1.getAction());
			if(mViewFlipper==null){
				return;
			}
			if(arg1.getStringExtra("pkg_name")==null||arg1.getStringExtra("pkg_name").trim().equals("")){
				return;
			}
			AppLog.d(TAG, "-------1111111-------");
			if(arg1.getAction().equals(Constants.INTENT_INSTALL_COMPLETED)){
				if(arg1.getStringExtra("app_id")==null||arg1.getStringExtra("app_id").trim().equals("")||arg1.getStringExtra("app_id").trim().equals("null")){
					return;
				}
				int app_id=Integer.valueOf(arg1.getStringExtra("app_id"));
				String pkg_name=arg1.getStringExtra("pkg_name");
				int status=mDBUtils.queryStatusByPkgName(arg1.getStringExtra("pkg_name"));
				AppLog.d(TAG, "------------Constants.INTENT_INSTALL_COMPLETED-------------status="+status);
				if(status!=Constants.APP_STATUS_INSTALLED){
					return;
				}
				AppLog.d(TAG, "---------install---&&&&&&&&&&&&&&&&&&&7---app_id="+app_id+";app_name="+pkg_name);
				for(int i=0;i<mViewFlipper.getChildCount();i++){
					if(GridLayout.class.isInstance(mViewFlipper.getChildAt(i))){
						GridLayout tGrid=(GridLayout)mViewFlipper.getChildAt(i);
						for(int j=0;j<tGrid.getChildCount();j++){
							if(!TextView.class.isInstance(tGrid.getChildAt(j))){
								ImageView button = (ImageView) (tGrid.getChildAt(j)).findViewById(R.id.home_item_img);
								AppsBean apps=(AppsBean)button.getTag();
//								if((Integer)button.getTag()==app_id){
								if(apps.getPkgName().equals(pkg_name)){
									TextView tex=(TextView)(tGrid.getChildAt(j)).findViewById(R.id.home_item_comment);
									tex.setText(R.string.installed);
									tex.setVisibility(View.VISIBLE);
									((tGrid.getChildAt(j)).findViewById(R.id.down_progress)).setVisibility(View.GONE);
									((tGrid.getChildAt(j)).findViewById(R.id.home_rating_bar)).setVisibility(View.VISIBLE);
								}
							}
						}
					}
				}
			}else if(arg1.getAction().equals(Constants.INTENT_UNINSTALL_COMPLETED)){
				AppLog.d(TAG, "--------DownReceiver-----------action="+arg1.getAction());
				int status=mDBUtils.queryStatusByPkgName(arg1.getStringExtra("pkg_name"));
				if(status!=Constants.APP_STATUS_UNDOWNLOAD){
					return;
				}
				String pkg_name=arg1.getStringExtra("pkg_name");
				for(int i=0;i<mViewFlipper.getChildCount();i++){
					if(GridLayout.class.isInstance(mViewFlipper.getChildAt(i))){
						GridLayout tGrid=(GridLayout)mViewFlipper.getChildAt(i);
						for(int j=0;j<tGrid.getChildCount();j++){
							if(!TextView.class.isInstance(tGrid.getChildAt(j))){
								ImageView button = (ImageView) (tGrid.getChildAt(j)).findViewById(R.id.home_item_img);
								AppsBean apps=(AppsBean)button.getTag();
								if(apps.getPkgName().equals(pkg_name)){
//								if((Integer)button.getTag()==app_id){
									TextView tex=(TextView)(tGrid.getChildAt(j)).findViewById(R.id.home_item_comment);
									tex.setText(R.string.noinstalled);
									(tGrid.getChildAt(j).findViewById(R.id.home_item_pop)).setVisibility(View.GONE);
							        (tGrid.getChildAt(j).findViewById(R.id.pop_update)).setVisibility(View.GONE);
								}
							}
						}
					}
				}
			}else if(arg1.getAction().equals(Constants.INTENT_UPDATE_COMPLETED)){
				AppLog.d(TAG, "--------DownReceiver-----------action="+arg1.getAction());
				int status=mDBUtils.queryStatusByPkgName(arg1.getStringExtra("pkg_name"));
				if(status!=Constants.APP_STATUS_INSTALLED){
					return;
				}
				int app_id=Integer.valueOf(arg1.getStringExtra("app_id"));
				String pkg_name=arg1.getStringExtra("pkg_name");
				AppLog.d(TAG, "---------uninstall------app_id="+app_id);
				for(int i=0;i<mViewFlipper.getChildCount();i++){
					if(GridLayout.class.isInstance(mViewFlipper.getChildAt(i))){
						GridLayout tGrid=(GridLayout)mViewFlipper.getChildAt(i);
						for(int j=0;j<tGrid.getChildCount();j++){
							if(!TextView.class.isInstance(tGrid.getChildAt(j))){
								ImageView button = (ImageView) (tGrid.getChildAt(j)).findViewById(R.id.home_item_img);
//								if((Integer)button.getTag()==app_id){
								AppsBean apps=(AppsBean)button.getTag();
								if(apps.getPkgName().equals(pkg_name)){
									String versionNet=mDBUtils.queryVersionByPkgName(arg1.getStringExtra("pkg_name"));
									if(!Utils.CompareVersion(apps.getVersion(), versionNet)){
										(tGrid.getChildAt(j).findViewById(R.id.home_item_pop)).setVisibility(View.GONE);
								        (tGrid.getChildAt(j).findViewById(R.id.pop_update)).setVisibility(View.GONE);
								        ((tGrid.getChildAt(j)).findViewById(R.id.down_progress)).setVisibility(View.GONE);
										((tGrid.getChildAt(j)).findViewById(R.id.home_rating_bar)).setVisibility(View.VISIBLE);
									}
								}
							}
						}
					}
				}
			}else if(arg1.getAction().equals(Constants.INTENT_DOWNLOAD_PROGRESS)){
				if(arg1.getStringExtra("server_app_id")==null||arg1.getStringExtra("server_app_id").trim().equals("")||arg1.getStringExtra("server_app_id").trim().equals("null")){
					return;
				}
				int app_id=Integer.valueOf(arg1.getStringExtra("server_app_id"));
				if(mViewFlipper==null)
					return;
				for(int i=0;i<mViewFlipper.getChildCount();i++){
					if(GridLayout.class.isInstance(mViewFlipper.getChildAt(i))){
						GridLayout tGrid=(GridLayout)mViewFlipper.getChildAt(i);
						for(int j=0;j<tGrid.getChildCount();j++){
							ImageView button = (ImageView) (tGrid.getChildAt(j)).findViewById(R.id.home_item_img);
							AppsBean apps=(AppsBean)button.getTag();
							if(apps.getID()==app_id){
								int file_sum=arg1.getIntExtra("file_sum", -1);
				            	int down_size=arg1.getIntExtra("download_size", -1);
				            	ProgressBar progress=(ProgressBar)(tGrid.getChildAt(j)).findViewById(R.id.down_progress);
				                if(progress.getVisibility()!=View.VISIBLE){
				                	RatingBar bar = (RatingBar)(tGrid.getChildAt(j)).findViewById(R.id.home_rating_bar);
				                	bar.setVisibility(View.GONE);
				                	progress.setVisibility(View.VISIBLE);
				            	}
				                TextView priceView=(TextView) (tGrid.getChildAt(j)).findViewById(R.id.home_item_comment);
			                	priceView.setText(""+MainActivity.this.getString(R.string.download_tab_2));
			                	priceView.setVisibility(View.VISIBLE);
				                if(file_sum==0){
				                	progress.setProgress(0);
				                }else{
				                	long down=down_size;
				                    long sum=file_sum;
				                    int res=(int)(down*100/sum);
				                	progress.setProgress(res);
				                }
							}
						}
					}
				}
			}else if(arg1.getAction().equals(Constants.INTENT_DOWNLOAD_STARTED)){
				AppLog.d(TAG, "-----------arg1="+Constants.INTENT_DOWNLOAD_STARTED);
				if(arg1.getStringExtra("app_id")==null||arg1.getStringExtra("app_id").trim().equals("")||arg1.getStringExtra("app_id").trim().equals("null")){
					return;
				}
				int app_id=Integer.valueOf(arg1.getStringExtra("app_id"));
				AppLog.d(TAG, "-----------arg1="+Constants.INTENT_DOWNLOAD_STARTED+";app_id="+app_id);
				View tApp=getViewByAppId(app_id);
				if(tApp==null)
					return ;
				ImageView button = (ImageView) tApp.findViewById(R.id.home_item_img);
				AppsBean apps=(AppsBean)button.getTag();
				ProgressBar progress=(ProgressBar)tApp.findViewById(R.id.down_progress);
                if(progress.getVisibility()!=View.VISIBLE){
                	RatingBar bar = (RatingBar)tApp.findViewById(R.id.home_rating_bar);
                	bar.setVisibility(View.GONE);
                	progress.setVisibility(View.VISIBLE);
            	}
                TextView priceView=(TextView) tApp.findViewById(R.id.home_item_comment);
            	priceView.setText(""+MainActivity.this.getString(R.string.download_tab_2));
            	priceView.setVisibility(View.VISIBLE);
                TaskBean task=mDBUtils.queryTaskByPkgName(apps.getPkgName());
                if(task.getSumSize()>0){
                	long downSize=task.getDownloadSize();
                	long sumSize=task.getSumSize();
                	int res=(int)(downSize*100/sumSize);
                	progress.setProgress(res);
                }
			}else if(arg1.getAction().equals(Constants.INTENT_DOWNLOAD_DETLET)){
				if(arg1.getStringExtra("pkg_name")==null||arg1.getStringExtra("pkg_name").trim().equals("")){
					return;
				}
				int status=mDBUtils.queryStatusByPkgName(arg1.getStringExtra("pkg_name"));
				if(status!=Constants.APP_STATUS_UNDOWNLOAD){
					return;
				}
				String pkgName=arg1.getStringExtra("pkg_name");
				View tApp=getViewByAppPkg(pkgName);
				if(tApp==null)
					return;
				TextView textView=((TextView)tApp.findViewById(R.id.home_item_comment));
    			textView.setText(""+MainActivity.this.getString(R.string.noinstalled));
    			textView.setVisibility(View.VISIBLE);
            	(tApp.findViewById(R.id.down_progress)).setVisibility(View.GONE);
            	(tApp.findViewById(R.id.home_rating_bar)).setVisibility(View.VISIBLE);
            	(tApp.findViewById(R.id.home_item_pop)).setVisibility(View.GONE);
    			(tApp.findViewById(R.id.pop_update)).setVisibility(View.GONE);
			}else if(arg1.getAction().equals(Constants.INTENT_INSTALL_FAIL)){
				if(arg1.getStringExtra("pkg_name")==null||arg1.getStringExtra("pkg_name").trim().equals("")){
					return;
				}
				String pkgName=arg1.getStringExtra("pkg_name");
				View view=getViewByAppPkg(pkgName);
				if(view==null)
					return;
				TextView textView=((TextView)view.findViewById(R.id.home_item_comment));
    			textView.setText(""+MainActivity.this.getString(R.string.noinstalled));
    			textView.setVisibility(View.VISIBLE);
            	(view.findViewById(R.id.down_progress)).setVisibility(View.GONE);
            	(view.findViewById(R.id.home_rating_bar)).setVisibility(View.VISIBLE);
            	(view.findViewById(R.id.home_item_pop)).setVisibility(View.GONE);
    			(view.findViewById(R.id.pop_update)).setVisibility(View.GONE);
			}else if(arg1.getAction().equals(Constants.INTENT_DOWNLOAD_ERROR)){
				if(arg1.getStringExtra("app_id")==null||arg1.getStringExtra("app_id").trim().equals("")||arg1.getStringExtra("app_id").trim().equals("null")){
					return;
				}
				View view=getViewByAppId(Integer.valueOf(arg1.getStringExtra("app_id")));
				if(view==null)
					return;
				ProgressBar progressErr=(ProgressBar)view.findViewById(R.id.down_progress);
            	if(progressErr.getVisibility()==View.VISIBLE){
            		TextView priceView=(TextView)view.findViewById(R.id.home_item_comment);
                	priceView.setText(""+MainActivity.this.getString(R.string.download_error));
            	}
			}else if(arg1.getAction().equals(Constants.INTENT_DOWNLOAD_PAUSE)){
				Log.d(TAG, "-----Constants.INTENT_DOWNLOAD_PAUSE--");
				if(arg1.getStringExtra("pkg_name")==null||arg1.getStringExtra("pkg_name").trim().equals("")){
					return;
				}
				String pkg_name=arg1.getStringExtra("pkg_name");
				View view=getViewByAppPkg(pkg_name);
				Log.d(TAG, "pkg_name="+pkg_name+";view="+view);
				if(view==null)
					return;
				TextView priceView=(TextView) view.findViewById(R.id.home_item_comment);
	            priceView.setText(""+MainActivity.this.getString(R.string.pause_download));

			}
		}
	}
	
	public View getViewByAppId(int appId){
		if(appId<0){
			return null;
		}
		for(int i=0;i<mViewFlipper.getChildCount();i++){
			if(GridLayout.class.isInstance(mViewFlipper.getChildAt(i))){
				GridLayout tGrid=(GridLayout)mViewFlipper.getChildAt(i);
				for(int j=0;j<tGrid.getChildCount();j++){
					ImageView button = (ImageView) (tGrid.getChildAt(j)).findViewById(R.id.home_item_img);
					AppsBean apps=(AppsBean)button.getTag();
					if(apps.getID()==appId){
						return tGrid.getChildAt(j);
					}
				}
			}
		}
		return null;
	}
	
	public View getViewByAppPkg(String appPkg){
		if(appPkg==null||appPkg.trim().equals("")){
			return null;
		}
		for(int i=0;i<mViewFlipper.getChildCount();i++){
			if(GridLayout.class.isInstance(mViewFlipper.getChildAt(i))){
				GridLayout tGrid=(GridLayout)mViewFlipper.getChildAt(i);
				for(int j=0;j<tGrid.getChildCount();j++){
					ImageView button = (ImageView) (tGrid.getChildAt(j)).findViewById(R.id.home_item_img);
					AppsBean apps=(AppsBean)button.getTag();
					if(apps.getPkgName().equals(appPkg)){
						return tGrid.getChildAt(j);
					}
				}
			}
		}
		return null;
	}
  
}