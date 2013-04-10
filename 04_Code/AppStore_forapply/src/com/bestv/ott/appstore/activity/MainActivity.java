package com.bestv.ott.appstore.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.bestv.ott.appstore.activity.ClassActivity.DownReceiver;
import com.bestv.ott.appstore.animation.AnimationButton;
import com.bestv.ott.appstore.animation.AnimationControl;
import com.bestv.ott.appstore.animation.AnimationImageButton;
import com.bestv.ott.appstore.animation.AppSearchLayout;
import com.bestv.ott.appstore.animation.AppSearchLayout.OnSlidingAtEndListener;
import com.bestv.ott.appstore.animation.CategoryLayout;
import com.bestv.ott.appstore.animation.CenterAppLayout;
import com.bestv.ott.appstore.animation.AnimationMoreButton;
import com.bestv.ott.appstore.animation.AnimationTwoImages;
import com.bestv.ott.appstore.animation.DownloadTextView;
import com.bestv.ott.appstore.animation.EditTextButton;
import com.bestv.ott.appstore.animation.MenuButton;
import com.bestv.ott.appstore.animation.SearchAnimationButton;
import com.bestv.ott.appstore.animation.SearchTextView;
import com.bestv.ott.appstore.common.AppsBean;
import com.bestv.ott.appstore.common.Constants;
import com.bestv.ott.appstore.common.PageBean;
import com.bestv.ott.appstore.db.DBUtils;
import com.bestv.ott.appstore.parser.AppsParser;
import com.bestv.ott.appstore.parser.MenusParser;
import com.bestv.ott.appstore.thread.LoadAppThread;
import com.bestv.ott.appstore.utils.AppLog;
import com.bestv.ott.appstore.utils.CaCheManager;
import com.bestv.ott.appstore.utils.NetUtil;
import com.bestv.ott.appstore.utils.RequestParam;
import com.bestv.ott.appstore.utils.Utils;
import com.bestv.ott.appstore.R;

public class MainActivity extends BaseActivity implements View.OnClickListener,View.OnFocusChangeListener{
	/** Called when the activity is first created. */
	private ArrayList<AnimationButton> mArrayAnimationButton = new ArrayList<AnimationButton>();
	private ArrayList<MenuButton> mArrayMenuButton = new ArrayList<MenuButton>();
	private ArrayList<TextView> starArray=new ArrayList<TextView>();
	private List <Map<String, Object>> mMenuList=new ArrayList<Map<String, Object>>();
	private LoadAppThread mThread;
	private LatestThread mThreadLatest;
	private SearchAnimationButton all_app;
	private SearchAnimationButton all_queen;
	private AnimationMoreButton mMoreLayout;
	private AnimationImageButton search;
	private EditTextButton edit;
	private AppReceiver appReceiver;
	private AnimationControl control = AnimationControl.getInstance();
	
	private List<AppsBean> mRecommendApps;//推荐
	private List<AppsBean> mLatestApps;//最新
	
	private static final int MSG_NET_ERROR=1;
	private static final int MSG_STOP_PROGRESS=2;
	
 	private DownloadTextView mDownloadRecord;
 	private SearchTextView mSearch;
 	private Boolean isSearchButton = false;
 	private ImageView mImageView;//移动背景图片
    private ViewFlipper mViewFlipper;
    private GridLayout  mGridLayout;
    private LayoutInflater  mLayoutInflater;
    private CategoryLayout mMenuLinearLayout;
    
    private DBUtils mDBUtils;
    private Utils mUtils;
    private NetUtil mNetUtil;
    
    private ImageView mLeftButton;
    private ImageView mRightButton;
    //private ImageView mTopTitle;
    
    private  TextView pageView1;
    private  TextView pageView2;
    private  TextView pageView3;
    private  TextView pageView4;
    private  TextView pageView5;
    private  ImageView pageSearch;
    
    private String typeName;//当前类型名称
    private int mLoadNum=0;//当加载完菜单，应用时，2才会取消进度条
    
    private int pageNumPage=-1;
    private int pageTotal=0;//页面实际总页数
    private PageBean mPageBean;
    private long mPressTime;
    
    private RelativeLayout mRootLayout;
    private AppSearchLayout mAppSearchLayoutBottom;
    private AppSearchLayout mAppEditTextlayout;
    
    private LoadMenuTask mLoadMenuTask = null;

	public static final String TAG = "com.joysee.appstore.MainActivity";
	private final static int CWJ_HEAP_SIZE = 16*1024*1024;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppLog.d(TAG," onCreate ");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dalvik.system.VMRuntime.getRuntime().setMinimumHeapSize(CWJ_HEAP_SIZE);
        setContentView(R.layout.app_store_layout);	
        mPageBean=new PageBean();
        mPageBean.setPageSize(40);  
        mDBUtils=new DBUtils(MainActivity.this);
        mUtils = new Utils();
        mNetUtil = new NetUtil(MainActivity.this);
        setupViews();
        AppLog.d(TAG, "--------new LoadMenuTask().execute()----");
        if (mLoadMenuTask != null && mLoadMenuTask.getStatus() != LoadMenuTask.Status.FINISHED) {
        	mLoadMenuTask.cancel(true);
        }        
        mLoadMenuTask = (LoadMenuTask) new LoadMenuTask().execute();
        //new LoadAppsThread(MainActivity.this).start();
//        mThreadLatest=new LatestThread(mPageBean,RequestParam.Action.GETLATESTAPPS);
//        mThreadLatest.setPriority(Thread.MIN_PRIORITY);
//        mThreadLatest.start();
        mThread=new LoadAppThread(MainActivity.this,mPageBean,RequestParam.Action.GETRECOMMENDLIST);
        mThread.setPriority(Thread.MIN_PRIORITY);
        mThread.start();
        AppLog.d(TAG, "--------end----");
        startProgressDialog();
        IntentFilter filter=new IntentFilter();
        filter.addAction(Constants.INTENT_INSTALL_COMPLETED);
        filter.addAction(Constants.INTENT_UNINSTALL_COMPLETED);
        filter.addAction(Constants.INTENT_UPDATE_COMPLETED);
        appReceiver=new AppReceiver();
        this.registerReceiver(appReceiver, filter);
	}
	
	 private void initializeByIntent() {
		 Intent intent = getIntent();
	     String action = intent.getAction();

	     if (Intent.ACTION_GET_CONTENT.equalsIgnoreCase(action)) {
	        	
	     }
	 }
	 
	private void setupViews() {
		mRootLayout = (RelativeLayout)findViewById(R.id.root_relativelayout);
		mDownloadRecord = (DownloadTextView) this.findViewById(R.id.top_down_record);
		mSearch = (SearchTextView) this.findViewById(R.id.top_search);
		mLeftButton = (ImageView) this.findViewById(R.id.app_store_left_image);
		mRightButton = (ImageView) this.findViewById(R.id.app_store_right_image);
		mImageView = (ImageView) this.findViewById(R.id.animation_imageview);
		mViewFlipper = (ViewFlipper) this.findViewById(R.id.app_store_viewflipper);
		mMenuLinearLayout = (CategoryLayout) this.findViewById(R.id.app_store_menu_layout);
		mMoreLayout=(AnimationMoreButton)this.findViewById(R.id.top2_more);
		//mTopTitle=(ImageView)this.findViewById(R.id.top2_left);
		mDownloadRecord.setOnClickListener(this);
		mSearch.setOnClickListener(this);
		mDownloadRecord.setBackImageView(mImageView);
		mSearch.setBackImageView(mImageView);
		mSearch.setNextFocusDownId(mMoreLayout.getId());
		mDownloadRecord.setNextFocusDownId(mMoreLayout.getId());
/*		mDownloadRecord.setOnKeyListener(new View.OnKeyListener(){
			@Override
			public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
				if(arg2.getKeyCode()==KeyEvent.KEYCODE_DPAD_DOWN){
					mMoreLayout.requestFocus();
					return true;
				}
				return false;
			}
		});*/
		mMoreLayout.setImage1Resource(R.drawable.new_title);
//		mMoreLayout.setImage2Resource(R.drawable.newmore1);
		mMoreLayout.setBackImageView(mImageView);
		//mMoreLayout.setNextFocusUpId(mDownloadRecord.getId());
		mMoreLayout.setNextFocusUpId(mSearch.getId());
		mMoreLayout.setOnKeyListener(new View.OnKeyListener(){
			@Override
			public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
				if(arg2.getKeyCode()==KeyEvent.KEYCODE_DPAD_RIGHT
						&& arg2.getAction() == KeyEvent.ACTION_DOWN){
					nextFlipperPage(false);
					return true;
				}else if(arg2.getKeyCode()==KeyEvent.KEYCODE_DPAD_LEFT
						&& arg2.getAction() == KeyEvent.ACTION_DOWN){
					previousFlipperPage(false);
					return true;
				}
				return false;
			}
		});
		
		typeName = MainActivity.this.getString(R.string.new_app);
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
    	mLoadNum++;
    	if(mLoadNum>=2){
    		mHandler.sendEmptyMessage(MSG_STOP_PROGRESS);
    	}
    	if(null==appList){
    		mHandler.sendEmptyMessage(MSG_NET_ERROR);
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
    AnimationButton requestBut;
    public void makeAllPage(){
    	int mostNum=mRecommendApps.size();
    	AppLog.d(TAG, "--------------mostNum="+mostNum);
    	int allPage=mostNum%8==0?mostNum/8:mostNum/8+1;
    	AppLog.d(TAG, "-------------------allPage="+allPage);
    	for(int i=0;i<allPage;i++){
    		pageTotal++;
    		AppLog.d(TAG, "-------------------current="+i);
    		CenterAppLayout layout = new CenterAppLayout(this);
            layout.setColumnCount(4);
    		layout.setFocusGroup(mRootLayout);
    		AppLog.d(TAG, "---------------------00");
    		if(mRecommendApps!=null){
    			for(int p=0;p<8;p++){
        			if((i*8+p+1)<=mRecommendApps.size()){
        				layout.addView(makeOneItem(p,mRecommendApps.get(i*8+p)));
        			}
        		}
    		}
//    		for(int j=0;j<4;j++){//第一排最新
//    			if((i*4+j+1)<=mLatestApps.size()){
//    				layout.addView(makeOneItem(j,mLatestApps.get(i*4+j)),j);
//    			}else{
//    				layout.addView(new TextView(MainActivity.this));//不够用TextView填充
//    			}
//    		}
//    		AppLog.log_D(TAG, "---------------------11");
//    		if(mRecommendApps!=null){
//    			for(int p=0;p<4;p++){//第二排推荐
//        			if((i*4+p+1)<=mRecommendApps.size()){
//        				layout.addView(makeOneItem(4+p,mRecommendApps.get(i*4+p)), p+4);
//        			}
//        		}
//    		}
    		
    		layout.setOnSlidingAtEndListener(new CenterAppLayout.OnSlidingAtEndListener() {
    			
    			public boolean onSlidingAtRight(CenterAppLayout layout, View view,boolean fastMode) {
    				long begin = SystemClock.elapsedRealtime();
    				nextFlipperPage(fastMode);
    				long end = SystemClock.elapsedRealtime();
    				AppLog.d(TAG, "nextFlipperPage  take      " + (end-begin));
    				return true;
    			}
    			
    			public boolean onSlidingAtLeft(CenterAppLayout layout, View view,boolean fastMode) {
    				long begin = SystemClock.elapsedRealtime();
    				previousFlipperPage(fastMode);
    				long end = SystemClock.elapsedRealtime();
    				AppLog.d(TAG, "previousflipperpage  take      " + (end-begin));
    				return true;
    			}
    		});
    		mViewFlipper.addView(layout);
            if(pageTotal==1){
          	    requestBut=(AnimationButton) layout.getChildAt(0).findViewById(R.id.home_item_img);
            }
            layout.setBackImageView(mImageView);
    	}
        addSearchViewFillper();
        setPage(pageTotal, pageNumPage);
        if(requestBut!=null){
        	requestBut.requestFocus();
      	    control.transformAnimation(mImageView, mImageView, requestBut, this, false, true);
      	    requestBut.startAnimation();
        }
    }
    
    public View makeOneItem(int index,AppsBean appsBean){
  	    View view = mLayoutInflater.inflate(R.layout.home_item_layout, null);
    	AnimationButton button = (AnimationButton) view.findViewById(R.id.home_item_img);
    	TextView itemtext = (TextView)view.findViewById(R.id.home_item_name);
    	TextView itemcomment = (TextView)view.findViewById(R.id.home_item_comment);
    	RatingBar bar = (RatingBar)view.findViewById(R.id.home_rating_bar);
        ImageView popIV = (ImageView)view.findViewById(R.id.home_item_pop);
        ImageView popTvT = (ImageView)view.findViewById(R.id.pop_update);
    	bar.setFocusable(false);
		if(index < 4){
    		view.setNextFocusUpId(mMoreLayout.getId());
    		button.setNextFocusUpId(mMoreLayout.getId());
    	}
    	String soc=appsBean.getScore()==null?"0":appsBean.getScore().trim();
		float res=Integer.valueOf(soc.equals("")?"0":soc);
		res = res / 2;
		bar.setRating((float)res);
    	button.setOnClickListener(this);
    	button.setImageView(mImageView);
    	button.setTag(appsBean);
    	if(null==appsBean.getNatImageUrl()||appsBean.getNatImageUrl().equals("")){
    		button.setImageRes(R.drawable.app_default_img);
		}else{
			
			button.setMainImageURi(Uri.parse(appsBean.getNatImageUrl()));
//	       	button.setImageURI(Uri.parse(appsBean.getNatImageUrl()));
//			button.setButtonIcon(appsBean.getNatImageUrl()) ;
		}
    	
    	itemtext.setText(appsBean.getAppName());
    	int status = mDBUtils.queryStatusByPkgNameFormApplication(appsBean.getPkgName());
    	switch(status){
    		case 4: //在升级
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
    			
    	}
        return view;
    }
    
	int index = -1;
	public void refreshAppsListOneLayout(List<AppsBean> appList){
		if(appList==null||appList.size()<1){
			nextPage();
		    return;
		}
		pageTotal++;
		CenterAppLayout layout = new CenterAppLayout(this);
        layout.setColumnCount(4);
		  layout.setFocusGroup(mRootLayout);
        for(int i=0;i<appList.size();i++){
      	    View view = mLayoutInflater.inflate(R.layout.home_item_layout, null);
        	AnimationButton button = (AnimationButton) view.findViewById(R.id.home_item_img);
        	TextView itemtext = (TextView)view.findViewById(R.id.home_item_name);
        	TextView itemcomment = (TextView)view.findViewById(R.id.home_item_comment);
        	RatingBar bar = (RatingBar)view.findViewById(R.id.home_rating_bar);
        	bar.setFocusable(false);
			if(i < 4){
        		view.setNextFocusUpId(mMoreLayout.getId());
        		button.setNextFocusUpId(mMoreLayout.getId());
        	}
        	String soc=appList.get(i).getScore()==null?"0":appList.get(i).getScore().trim();
        	AppLog.d(TAG, "---------------soc="+soc);
			float res=Integer.valueOf(soc.equals("")?"0":soc);
			res = res / 2;
			AppLog.d(TAG, "---------------res="+res+";(float)res="+(float)res);
			bar.setRating((float)res);
        	button.setOnClickListener(this);
        	button.setImageView(mImageView);
        	AppLog.d(TAG, "--------------size="+appList.size()+";i="+i);
        	AppsBean bean = appList.get(i);
        	button.setTag(bean);
        	if(null==appList.get(i).getNatImageUrl()||appList.get(i).getNatImageUrl().equals("")){
        		button.setImageRes(R.drawable.app_default_img);
			}else{
				button.setMainImageURi(Uri.parse(appList.get(i).getNatImageUrl()));
//				button.setImageURI(Uri.parse(appList.get(i).getNatImageUrl()));
			}
        	layout.addView(view);
        	itemtext.setText(bean.getAppName());
        	int status = mDBUtils.queryStatusByPkgName(bean.getPkgName());
        	switch(status){
        		case 4: //在升级
        		case 3: //已安装
        			itemcomment.setText(R.string.installed);
    			default :
    				break;
        			
        	}
        }
        layout.setOnSlidingAtEndListener(new CenterAppLayout.OnSlidingAtEndListener() {
			
			public boolean onSlidingAtRight(CenterAppLayout layout, View view,boolean fastMode) {
				long begin = SystemClock.elapsedRealtime();
				AppLog.d(TAG,    "---------------onSlidingAtRight  begin----------------------");
				nextFlipperPage(fastMode);
				long end = SystemClock.elapsedRealtime();
				AppLog.d(TAG, "nextFlipperPage  take      " + (end-begin));
				return true;
			}
			
			public boolean onSlidingAtLeft(CenterAppLayout layout, View view,boolean fastMode) {
				long begin = SystemClock.elapsedRealtime();
				previousFlipperPage(fastMode);
				long end = SystemClock.elapsedRealtime();
				AppLog.d(TAG, "previousflipperpage  take      " + (end-begin));
				return true;
			}
		});
        
        /*//翻页到处理
    	if(appList.size()==1){
			AnimationButton button=(AnimationButton) layout.getChildAt(0).findViewById(R.id.home_item_img);
			button.setOnKeyListener(new LeftAndRightListener());
		}else if(appList.size()<5){
			AnimationButton button1=(AnimationButton) layout.getChildAt(0).findViewById(R.id.home_item_img);
			button1.setOnKeyListener(new LeftListener());
			AnimationButton button2=(AnimationButton) layout.getChildAt(appList.size()-1).findViewById(R.id.home_item_img);
			button2.setOnKeyListener(new RightListener());
		}else {
			AnimationButton button1=(AnimationButton) layout.getChildAt(0).findViewById(R.id.home_item_img);
			button1.setOnKeyListener(new LeftListener());
			AnimationButton button2=(AnimationButton) layout.getChildAt(3).findViewById(R.id.home_item_img);
			button2.setOnKeyListener(new RightListener());
			AnimationButton button3=(AnimationButton) layout.getChildAt(4).findViewById(R.id.home_item_img);
			button3.setOnKeyListener(new LeftListener());
			AnimationButton button4=(AnimationButton) layout.getChildAt(appList.size()-1).findViewById(R.id.home_item_img);
			button4.setOnKeyListener(new RightListener());
		}
    	if(appList.size()>=4){
    		AnimationButton button0=(AnimationButton) layout.getChildAt(3).findViewById(R.id.home_item_img);
			button0.setOnKeyListener(new RightAndUPListener());
    	}
        if(pageTotal==1){
      	    LayoutAnimationController controller = new LayoutAnimationController(AnimationUtils.loadAnimation(this, R.anim.list_animation), 1);
      	    layout.setLayoutAnimation(controller);
        }*/
        mViewFlipper.addView(layout);
        if(pageTotal==1){
      	    AnimationButton requestBut=(AnimationButton) layout.getChildAt(0).findViewById(R.id.home_item_img);
      	    requestBut.requestFocus();
      	    requestBut.requestFocus();
      	    control.transformAnimation(mImageView, mImageView, requestBut, this, false, true);
      	    requestBut.startAnimation();
      	  
        }
        if(mPageBean.getPageNo()>5){
        	addSearchViewFillper();
        	setPage(pageTotal, pageNumPage);
        	return;
        }else{
        	nextPage();
        }
        layout.setBackImageView(mImageView);
        AppLog.d(TAG, "mViewFlipper.getChildCount()"+mViewFlipper.getChildCount());
	}
	
	class LeftAndRightListener implements View.OnKeyListener{
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if(keyCode==KeyEvent.KEYCODE_DPAD_RIGHT){
				nextFlipperPage(true);
				return true;
			}else if(keyCode==KeyEvent.KEYCODE_DPAD_LEFT){
				previousFlipperPage(true);
				return true;
			}
			return false;
		}
	}
	
	class LeftListener implements View.OnKeyListener{
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if(keyCode==KeyEvent.KEYCODE_DPAD_LEFT){
				previousFlipperPage(true);
				return true;
			}
			return false;
		}
	}
	class RightListener implements View.OnKeyListener{
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if(keyCode==KeyEvent.KEYCODE_DPAD_RIGHT){
				nextFlipperPage(true);
				return true;
			}
			return false;
		}
	}
	
	class RightAndUPListener implements View.OnKeyListener{
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if(keyCode==KeyEvent.KEYCODE_DPAD_RIGHT){
				nextFlipperPage(true);
				return true;
			}else if(keyCode==KeyEvent.KEYCODE_DPAD_UP){
				mMoreLayout.requestFocus();
				return true;
			}
			return false;
		}
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		setEnable(true);
		switch (event.getKeyCode()) {
		case KeyEvent.KEYCODE_DPAD_DOWN:
		case KeyEvent.KEYCODE_DPAD_UP:
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_DPAD_RIGHT:
//			if (System.currentTimeMillis() - mPressTime < 300) {
//				return true;
//			}
			break;
		default:
			break;
		}
		mPressTime = System.currentTimeMillis();
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
    
    public void setEnable(boolean bol){
		for(int i=0;i<mArrayMenuButton.size();i++){
			MenuButton button =	mArrayMenuButton.get(i);
			button.setFocusable(bol);
			button.setEnabled(bol);
		}
		if(edit!=null){
			edit.getEditText().setFocusable(bol);
			edit.getEditText().setEnabled(bol);
		}
		mDownloadRecord.setFocusable(bol);
		mDownloadRecord.setEnabled(bol);
	}
	
	public void nextFlipperPage(boolean fastMode){
		ViewGroup lastGroup = (ViewGroup) mViewFlipper.getCurrentView();
		View lastFocus = null;
		if(GridLayout.class.isInstance(lastGroup)){
			lastFocus = lastGroup.getFocusedChild();
			if(lastFocus instanceof LinearLayout){
				View focusView = ((ViewGroup)lastFocus).getChildAt(0);
				
				if (focusView != null) {
					if (AnimationButton.class.isInstance(focusView)) {
						AnimationButton button = (AnimationButton) focusView;
						button.stopAnimation();
					}
				}
			}
			AppLog.d(TAG, "nextFlipperPage lastFocus is " + lastFocus);
		}else{
			if(mAppEditTextlayout !=null && mAppEditTextlayout.hasFocus()){
				lastFocus = mAppEditTextlayout.getFocusedChild();
				AppLog.d(TAG, "nextFlipperPage lastFocus is mAppEditTextlayout   " + lastFocus);
			}else if (all_app != null && all_app.hasFocus()){
				lastFocus = all_app;
				((SearchAnimationButton)lastFocus).stopAnimation();
				AppLog.d(TAG, "nextFlipperPage lastFocus is all_app   " + lastFocus);
			}else if (all_queen != null && all_queen.hasFocus()){
				lastFocus = all_queen;
				((SearchAnimationButton)lastFocus).stopAnimation();
				AppLog.d(TAG, "nextFlipperPage lastFocus is all_queen   " + lastFocus);
			}
		}
		
		if(fastMode){
			mViewFlipper.setInAnimation(null);
			mViewFlipper.setOutAnimation(null);
		}else{
			mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.left_in));
			mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.left_out));
		}
		
		setEnable(false);//焦点冲突问题
		mViewFlipper.showNext();
		if(GridLayout.class.isInstance(mViewFlipper.getCurrentView())){
			AppLog.d(TAG, "GridLayout.class.isInstance(mViewFlipper.getCurrentView())");
			mGridLayout = (GridLayout) mViewFlipper.getCurrentView();
			if(mGridLayout!=null&&!mMoreLayout.hasFocus()){
				AnimationButton button = (AnimationButton) mGridLayout.getChildAt(0).findViewById(R.id.home_item_img);
				if(button != null){
					Log.i(TAG, "button != null");
					all_queen.stopAnimation();
					control.transformAnimation(mImageView, lastFocus != null ? lastFocus:mImageView, button, this, false, true);
					button.requestFocus();
					button.startAnimation();
				}
			}
			mMoreLayout.setImage1Resource(R.drawable.new_title);
			pageSearch.setBackgroundResource(R.drawable.home_search);
		}else{
			if(all_app!=null&&!mMoreLayout.hasFocus()){
				AppLog.d(TAG, "all_app!=null&&!mMoreLayout.hasFocus()");
				all_app.requestFocus();
				all_app.startAnimation();
				control.transformAnimation(mImageView, mImageView, all_app, this, false, true);
			}
			mMoreLayout.setImage1Resource(R.drawable.searchtitle1);
			pageSearch.setBackgroundResource(R.drawable.home_search_big);
		}
		pageNumPage++;
		if(pageNumPage >pageTotal){
			pageNumPage = 0;
		}
		setPage(pageTotal, pageNumPage);
	}
	
	public void previousFlipperPage(boolean fastMode){
		AppLog.d(TAG, "111111111");
		ViewGroup lastGroup = (ViewGroup) mViewFlipper.getCurrentView();
		View lastFocus = null;
		if(GridLayout.class.isInstance(lastGroup)){
			lastFocus = lastGroup.getFocusedChild();
			if(lastFocus instanceof LinearLayout){
				View focusView = ((ViewGroup)lastFocus).getChildAt(0);
				
				if (focusView != null) {
					if (AnimationButton.class.isInstance(focusView)) {
						AnimationButton button = (AnimationButton) focusView;
						button.stopAnimation();
					}
				}
			}
			AppLog.d(TAG, "previousFlipperPage lastFocus is " + lastFocus);
		}else{
			if(mAppEditTextlayout !=null && mAppEditTextlayout.hasFocus()){
				lastFocus = mAppEditTextlayout.getFocusedChild();
				AppLog.d(TAG, "nextFlipperPage lastFocus is mAppEditTextlayout");
			}else if (all_app != null && all_app.hasFocus()){
				lastFocus = all_app;
				((SearchAnimationButton)lastFocus).stopAnimation();
				AppLog.d(TAG, "nextFlipperPage lastFocus is all_app");
			}else if (all_queen != null && all_queen.hasFocus()){
				lastFocus = all_queen;
				((SearchAnimationButton)lastFocus).stopAnimation();
				AppLog.d(TAG, "nextFlipperPage lastFocus is all_queen");
			}
		}
		
		if(fastMode){
			mViewFlipper.setInAnimation(null);
			mViewFlipper.setOutAnimation(null);
		}else{
			mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.right_in));
			mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.right_out));
			
		}
		
		setEnable(false);
		mViewFlipper.showPrevious();
		if(GridLayout.class.isInstance(mViewFlipper.getCurrentView())){
			mGridLayout = (GridLayout) mViewFlipper.getCurrentView();
			if(null!= mGridLayout&&!mMoreLayout.hasFocus()){
				View item=null ;
				AppLog.d(TAG, "---------------mGridLayout.getChildCount()="+mGridLayout.getChildCount());
//				if(!TextView.class.isInstance(mGridLayout.getChildAt(3))){
//					item=mGridLayout.getChildAt(3);
//				}else if(!TextView.class.isInstance(mGridLayout.getChildAt(2))){
//					item=mGridLayout.getChildAt(2);
//				}else if(!TextView.class.isInstance(mGridLayout.getChildAt(1))){
//					item=mGridLayout.getChildAt(1);
//				}else if(!TextView.class.isInstance(mGridLayout.getChildAt(0))){
//					item=mGridLayout.getChildAt(0);
//				}
				if(mGridLayout.getChildCount()>4){
					item=mGridLayout.getChildAt(3);
				}else{
					item=mGridLayout.getChildAt(mGridLayout.getChildCount()-1);
				}
				AnimationButton button = null;
				if (item != null){
					button = (AnimationButton)item.findViewById(R.id.home_item_img);
				}else{
					return;
				}
				if(button != null){
					control.transformAnimation(mImageView, lastFocus != null ? lastFocus:mImageView, button, this, false, true);
					button.requestFocus();
					button.startAnimation();
				} else {
					AnimationButton buttonFirst = (AnimationButton) mGridLayout.getChildAt(0).findViewById(R.id.home_item_img);
					control.transformAnimation(mImageView, mImageView, buttonFirst, this, false, true);
					button.requestFocus();
					button.startAnimation();
				}
			}
			if(mMoreLayout.hasFocus()){
				mMoreLayout.requestFocus();
			}
			mMoreLayout.setImage1Resource(R.drawable.new_title);
			pageSearch.setBackgroundResource(R.drawable.home_search);
		}else{
			if(all_app!=null&&!mMoreLayout.hasFocus()){
				AppLog.d(TAG, "------previousFlipperPage------");
				all_app.requestFocus();
				all_app.startAnimation();
				control.transformAnimation(mImageView, mImageView, all_app, this, false, true);
				AppLog.d(TAG, "------previousFlipperPage---requestFocus---");
			}
			mMoreLayout.setImage1Resource(R.drawable.searchtitle1);
			pageSearch.setBackgroundResource(R.drawable.home_search_big);
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
		AppLog.d(TAG, "pageNum="+pageNum+"---pageTotal="+pageTotal);
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
    	
    	mAppEditTextlayout = (AppSearchLayout)view.findViewById(R.id.app_search_edittext_layout);
    	mAppEditTextlayout.setBackImageView(mImageView);
    	mAppEditTextlayout.setFocusGroup(mRootLayout);
    	mAppEditTextlayout.setOnSlidingAtEndListener(new OnSlidingAtEndListener() {
			public boolean onSlidingAtLeft(AppSearchLayout layout, View view,boolean fastMode) {
				long begin = SystemClock.elapsedRealtime();
				previousFlipperPage(fastMode);
				long end = SystemClock.elapsedRealtime();
				AppLog.d(TAG, "previousflipperpage  take      " + (end-begin));
				return true;
			}
			public boolean onSlidingAtRight(AppSearchLayout layout, View view,boolean fastMode) {
				long begin = SystemClock.elapsedRealtime();
				nextFlipperPage(fastMode);
				long end = SystemClock.elapsedRealtime();
				AppLog.d(TAG, "previousflipperpage  take      " + (end-begin));
				return true;
			}
		});
    	
    	mAppSearchLayoutBottom = (AppSearchLayout)view.findViewById(R.id.app_search_layout_bottom);
    	mAppSearchLayoutBottom.setBackImageView(mImageView);
    	mAppSearchLayoutBottom.setFocusGroup(mRootLayout);
    	mAppSearchLayoutBottom.setOnSlidingAtEndListener(new OnSlidingAtEndListener() {
			public boolean onSlidingAtLeft(AppSearchLayout layout, View view,boolean fastMode) {
				previousFlipperPage(fastMode);
				return true;
			}
			public boolean onSlidingAtRight(AppSearchLayout layout, View view,boolean fastMode) {
				nextFlipperPage(fastMode);
				return true;
			}
		});
    	edit = (EditTextButton) view.findViewById(R.id.search_edittext);
    	mAppEditTextlayout.setEditText(edit.getEditText());
    	search = (AnimationImageButton) view.findViewById(R.id.search_button_search);
    	AnimationImageButton voice = (AnimationImageButton) view.findViewById(R.id.search_button_voice);
        all_app = (SearchAnimationButton)view.findViewById(R.id.search_all_app);
        all_queen = (SearchAnimationButton)view.findViewById(R.id.search_all_queen);
    	
    	search.setImageResource(R.drawable.btn_search);
    	voice.setImageResource(R.drawable.btn_voice);
    	all_app.setImageRes(R.drawable.app_all);
    	all_queen.setImageRes(R.drawable.app_ranking);
    	edit.setButtonBackImage(R.drawable.search_edittext);
    	
    	search.setFlag(false);
    	voice.setFlag(false);
    	
    	edit.setNextFocusUpId(mMoreLayout.getId());
    	search.setNextFocusUpId(mMoreLayout.getId());
    	voice.setNextFocusUpId(mMoreLayout.getId());
    	
    	all_app.setOnClickListener(this);
    	all_queen.setOnClickListener(this);
    	search.setOnClickListener(this);
//    	all_queen.setOnKeyListener(new View.OnKeyListener() {
//			@Override
//			public boolean onKey(View v, int keyCode, KeyEvent event) {
//				if(event.getAction()==KeyEvent.ACTION_DOWN&&keyCode==KeyEvent.KEYCODE_DPAD_RIGHT){
//					nextFlipperPage();
//					return true;
//				}
//				return false;
//			}
//		});
//    	all_app.setOnKeyListener(new View.OnKeyListener() {
//			@Override
//			public boolean onKey(View v, int keyCode, KeyEvent event) {
//				if(event.getAction()==KeyEvent.ACTION_DOWN&&keyCode==KeyEvent.KEYCODE_DPAD_LEFT){
//					previousFlipperPage();
//					return true;
//				}
//				return false;
//			}
//		});
    	edit.getEditText().setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				AppLog.d(TAG, "-------------------edit------------keyCode="+keyCode);
				if(keyCode==KeyEvent.KEYCODE_ENTER || keyCode==KeyEvent.KEYCODE_DPAD_CENTER){
					InputMethodManager im = ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE));
					im.showSoftInput(edit.getEditText(),0);
					EditTextButton.isH = true;
					return true;
				}
				if(keyCode==KeyEvent.KEYCODE_ESCAPE||keyCode==KeyEvent.KEYCODE_BACK){
					if(EditTextButton.isH){
						InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
						imm.hideSoftInputFromWindow(edit.getEditText().getWindowToken(),0);
						EditTextButton.isH = false;
						return true;
					}
				}
				return false;
			}
		});
//    	search.setOnKeyListener(new View.OnKeyListener() {
//			@Override
//			public boolean onKey(View v, int keyCode, KeyEvent event) {
//				if(event.getAction()==KeyEvent.ACTION_DOWN&&keyCode==KeyEvent.KEYCODE_DPAD_RIGHT){
//					nextFlipperPage();
//					return true;
//				}
//				return false;
//			}
//		});
    	search.setBackImageView(mImageView);
    	voice.setBackImageView(mImageView);
    	all_app.setImageView(mImageView);
    	all_queen.setImageView(mImageView);
    	edit.setBackImageView(mImageView);
    	mViewFlipper.addView(view);
    	if(mRecommendApps==null||mRecommendApps.size()<=0){
    		all_app.requestFocus();
    		control.transformAnimation(mImageView, mImageView, all_app, this, false, true);
    		pageSearch.setBackgroundResource(R.drawable.home_search_big);
    	}
	}
	
	MenuButton buttonfirst;
    MenuButton buttonlast;
    View viewFirst;
    View viewLast;
    public void initMenuLayout(){
    	mMenuLinearLayout.removeAllViews();
    	mMenuLinearLayout.setBackImageView(mImageView);
//    	mImageView.setBackgroundColor(Color.RED);
    	AppLog.d(TAG, "-------------"+mMenuList.size());
    	for(int i=0;i<mMenuList.size();i++){
    		View view = mLayoutInflater.inflate(R.layout.menu_item_layout, null);
    		MenuButton button = (MenuButton) view.findViewById(R.id.menu_item_img);
    		button.setTag(i);
    		button.setBackImageView(mImageView);
    		Map<String, Object> map = mMenuList.get(i);
    		if(i<3){
    			button.setImageResource((Integer)map.get("image"));
    		}else{
    			if(map.get("image")==null||map.get("image").equals("")){
    				button.setImageResource(R.drawable.menu_default_img);
    			}else{
    				button.setImageURI(Uri.parse((String) map.get("image")) );
    			}
    		}
    		if(i==0){
    			viewFirst = view;
    			buttonfirst = button;
    			buttonfirst.setId(Constants.MENU_LEFT_ID);
    		}
    		if(i==mMenuList.size()-1){
    			viewLast = view;
    			buttonlast = button;
    			buttonlast.setId(Constants.MENU_RIGHT_ID);
    		}
    		String name = (String) map.get("name");
//    		AppLog.log_D(TAG, "-------------"+name);
            button.setText(name);
            mArrayMenuButton.add(button);
            button.setOnClickListener(this);
        	mMenuLinearLayout.addView(view);
    	}
    	viewFirst.setNextFocusLeftId(buttonlast.getId());
    	viewLast.setNextFocusRightId(buttonfirst.getId());
    	
    	int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        mMenuLinearLayout.measure(w, h);
        int width = mMenuLinearLayout.getMeasuredWidth();
        LayoutParams lp1 = mMenuLinearLayout.getLayoutParams();
        lp1.width = width;
//    	buttonfirst.setOnKeyListener(new View.OnKeyListener() {
//			@Override
//			public boolean onKey(View v, int keyCode, KeyEvent event) {
//				AppLog.log_D(TAG, "buttonfirst-------onkey------");
//				if(keyCode==KeyEvent.KEYCODE_DPAD_LEFT){
//					AppLog.log_D(TAG, "buttonfirst-------------");
//					buttonlast.requestFocus();
//					return true;
//				}
//				return false;
//			}
//		});
//    	buttonlast.setOnKeyListener(new View.OnKeyListener() {
//			@Override
//			public boolean onKey(View v, int keyCode, KeyEvent event) {
//				AppLog.log_D(TAG, "buttonlast--------onkey-----");
//				if(keyCode==KeyEvent.KEYCODE_DPAD_RIGHT){
//					AppLog.log_D(TAG, "buttonlast-------------");
//					buttonfirst.requestFocus();
//					return true;
//				}
//				return false;
//			}
//		});
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
			String urlStr=RequestParam.SERVICE_ACTION_URL+RequestParam.Action.GETAPPTYPELIST;
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
		
		private final static String TAG="com.bestv.ott.appstore.MainActivity.LatestThread";
		
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
			String urlStr=RequestParam.SERVICE_ACTION_URL+mAction;
			Map<String,String> tParam=new TreeMap<String,String>();
			if(null!=mPageBean){ //TODO  this need to set userID and groudID to param
				tParam.put(RequestParam.Param.PAGENO,String.valueOf(mPageBean.getPageNo()));
				tParam.put(RequestParam.Param.LINENUMBER, String.valueOf(mPageBean.getPageSize()));
			}
			if(flag){
				Object obj=mNetUtil.getNetData(tParam, urlStr, mPageBean, new AppsParser());
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
	public void onFocusChange(View v, boolean hasFocus) {
		
	}
	
	public void onResume(){
		super.onResume();
		if(isSearchButton){
			AppLog.d(TAG, "-------------after AppSearchLayout : -----main onResume----------");
			search.requestFocus();
			//search.setBackImageView(mImageView);
			control.transformAnimation(mImageView, search, search, this	, true, true);
			isSearchButton = false;
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
        	mLoadMenuTask.cancel(true);
        }
		if(mThread!=null&&mThread.isAlive()){
			mThread.setFlag(false);
		}
		if(mThreadLatest!=null&&mThreadLatest.isAlive()){
			mThreadLatest.setFlag(false);
		}
	}
	
	
	
	@Override
	protected void onDestroy() {
		AppLog.d(TAG, "---------------onDestory-----------------");
		AnimationButton.recycleMemory();
		super.onDestroy();
		new CaCheManager().refreshCaChe();//刷新图片缓存
		if(null!=appReceiver){
        	this.unregisterReceiver(appReceiver);
        }
		
	}

	@Override
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
                overridePendingTransition(R.anim.zoomin,R.anim.zoomout);
				break;
			case R.id.top_down_record:
				downloadMgr();
				break;
			case R.id.menu_item_img://分类
			case Constants.MENU_LEFT_ID:
			case Constants.MENU_RIGHT_ID:
				if(v.getTag()==null) return;
				int position = (Integer) v.getTag();
	                AppLog.d(TAG, "111111111111111111");
	                Intent intent_class=new Intent(MainActivity.this,ClassActivity.class);
	                intent_class.putExtra("type_id",(Integer)mMenuList.get(position).get("id"));
	                //intent.putExtra("TypeName", typeName);
	                startActivity(intent_class);
	                overridePendingTransition(R.anim.zoomin,R.anim.zoomout);
				break;
			case R.id.search_button_search:
				Intent intentSearch = new Intent(MainActivity.this,AppSearchActivity.class);
				intentSearch.putExtra("keyWord", edit.getText());
				AppLog.d(TAG, "--------------keyword="+edit.getText());
				startActivity(intentSearch);
				overridePendingTransition(R.anim.zoomin,R.anim.zoomout);
				isSearchButton = true;//点一次搜索,设标志  
				break;
			case R.id.search_all_app:
				Intent all_app_intent = new Intent(MainActivity.this,ClassActivity.class);
				all_app_intent.putExtra("type_id",RequestParam.CalssID.ALL_ID);
				startActivity(all_app_intent);
				overridePendingTransition(R.anim.zoomin,R.anim.zoomout);
				break;
			case R.id.search_all_queen:
				Intent all_queen_intent = new Intent(MainActivity.this,ClassActivity.class);
				all_queen_intent.putExtra("type_id",RequestParam.CalssID.HOT_ID);
				startActivity(all_queen_intent);
				overridePendingTransition(R.anim.zoomin,R.anim.zoomout);
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
			if(arg1.getStringExtra("app_id")==null||arg1.getStringExtra("app_id").trim().equals("")||arg1.getStringExtra("app_id").trim().equals("null")){
				return;
			}
			if(arg1.getStringExtra("pkg_name")==null||arg1.getStringExtra("pkg_name").trim().equals("")){
				return;
			}
			if(arg1.getAction().equals(Constants.INTENT_INSTALL_COMPLETED)){
				int app_id=Integer.valueOf(arg1.getStringExtra("app_id"));
				String pkg_name=arg1.getStringExtra("pkg_name");
				String version=arg1.getStringExtra("version_code");
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
								AnimationButton button = (AnimationButton) (tGrid.getChildAt(j)).findViewById(R.id.home_item_img);
								AppsBean apps=(AppsBean)button.getTag();
//								if((Integer)button.getTag()==app_id){
								if(apps.getPkgName().equals(pkg_name)){
									TextView tex=(TextView)(tGrid.getChildAt(j)).findViewById(R.id.home_item_comment);
									tex.setText(R.string.installed);
									if(version!=null&&Utils.CompareVersion(apps.getVersion(),version)){
										AppLog.d(TAG, "apps.getVersion()"+apps.getVersion()+";version="+version);
										ImageView popIV = (ImageView)(tGrid.getChildAt(j)).findViewById(R.id.home_item_pop);
								        ImageView popTvT = (ImageView)(tGrid.getChildAt(j)).findViewById(R.id.pop_update);
								        popIV.setVisibility(View.VISIBLE);
								        popTvT.setVisibility(View.VISIBLE);
									}
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
				int app_id=Integer.valueOf(arg1.getStringExtra("app_id"));
				String pkg_name=arg1.getStringExtra("pkg_name");
				AppLog.d(TAG, "---------uninstall------app_id="+app_id);
				for(int i=0;i<mViewFlipper.getChildCount();i++){
					if(GridLayout.class.isInstance(mViewFlipper.getChildAt(i))){
						GridLayout tGrid=(GridLayout)mViewFlipper.getChildAt(i);
						for(int j=0;j<tGrid.getChildCount();j++){
							if(!TextView.class.isInstance(tGrid.getChildAt(j))){
								AnimationButton button = (AnimationButton) (tGrid.getChildAt(j)).findViewById(R.id.home_item_img);
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
								AnimationButton button = (AnimationButton) (tGrid.getChildAt(j)).findViewById(R.id.home_item_img);
//								if((Integer)button.getTag()==app_id){
								AppsBean apps=(AppsBean)button.getTag();
								if(apps.getPkgName().equals(pkg_name)){
									String versionNet=mDBUtils.queryVersionByPkgName(arg1.getStringExtra("pkg_name"));
									if(!Utils.CompareVersion(apps.getVersion(), versionNet)){
										(tGrid.getChildAt(j).findViewById(R.id.home_item_pop)).setVisibility(View.GONE);
								        (tGrid.getChildAt(j).findViewById(R.id.pop_update)).setVisibility(View.GONE);
									}
								}
							}
						}
					}
				}
			}
		}
	}
  
}