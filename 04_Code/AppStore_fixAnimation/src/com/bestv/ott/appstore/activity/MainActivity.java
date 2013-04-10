package com.bestv.ott.appstore.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
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
import android.graphics.drawable.Drawable;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
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

import com.bestv.ott.aidl.App;
import com.bestv.ott.appstore.activity.ClassActivity.DownReceiver;
import com.bestv.ott.appstore.animation.AnimUtils;
import com.bestv.ott.appstore.animation.MoveControl;
import com.bestv.ott.appstore.common.AppsBean;
import com.bestv.ott.appstore.common.Constants;
import com.bestv.ott.appstore.common.PageBean;
import com.bestv.ott.appstore.common.SpreeBean;
import com.bestv.ott.appstore.db.DBUtils;
import com.bestv.ott.appstore.parser.AppsParser;
import com.bestv.ott.appstore.parser.MenusParser;
import com.bestv.ott.appstore.parser.SpreeParser;
import com.bestv.ott.appstore.thread.LoadAppThread;
import com.bestv.ott.appstore.utils.AppLog;
import com.bestv.ott.appstore.utils.CaCheManager;
import com.bestv.ott.appstore.utils.FileUtils;
import com.bestv.ott.appstore.utils.NetUtil;
import com.bestv.ott.appstore.utils.RequestParam;
import com.bestv.ott.appstore.utils.Utils;
import com.bestv.ott.appstore.R;

public class MainActivity extends BaseActivity implements View.OnClickListener,View.OnFocusChangeListener{
	/** Called when the activity is first created. */
	private ArrayList<TextView> starArray=new ArrayList<TextView>();
	private List <Map<String, Object>> mMenuList=new ArrayList<Map<String, Object>>();
	private LoadAppThread mThread;
//	private LatestThread mThreadLatest;
	private ImageView all_app;
	private ImageView all_queen;
	private ImageView search;
	private EditText edit;
	private AppReceiver appReceiver;
	
	private List<AppsBean> mRecommendApps; //推荐
	private List<AppsBean> mLatestApps;    //最新
	
	private List<SpreeBean> sprees; //礼包信息
	private int spreesSize = 0;
	private int spreeLoopTimes = -1;//礼包显示次数
	private Map<Integer,Drawable> spreeIcon = new HashMap<Integer, Drawable>();//礼包图标
	private static Drawable spreeDrawable;
	private int mpackageId = -100;//礼包pakgeID
	private String mProductCode;
	private static boolean timerRun = false;
	
	private static final int MSG_NET_ERROR=1;
	private static final int MSG_STOP_PROGRESS=2;
	private static final int MSG_REFRESHSPREE = 1001;
	private static final int MSG_INIT_SPREE = 1002;
	
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
    
    private int mLoadNum=0;//当加载完菜单，应用时，2才会取消进度条
    
    private int pageNumPage=-1;
    private int pageTotal=0;//页面实际总页数
    private PageBean mPageBean;
    private boolean isHasSoftKey = false; //是否有软键盘
    private boolean isHaveFoucs = false; 
    private boolean isHaveSpree = false;
    
    private RelativeLayout mRootLayout;
    private TextView top2_spree;
    private ImageView titleImage;
    
    private LoadMenuTask mLoadMenuTask = null;
    private LoadSpreeThread mLoadSpreeThread = null;

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
        parent=(ViewGroup)this.findViewById(R.id.root_relativelayout);
        mPageBean=new PageBean();
        mPageBean.setPageSize(40);  
        mDBUtils=new DBUtils(MainActivity.this);
        mNetUtil = new NetUtil(MainActivity.this);
        setupViews();
        viewGroupFocus=parent.getDescendantFocusability();
        parent.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        AppLog.d(TAG, "--------new LoadMenuTask().execute()----");
        initThread();//启动相关线程-->获取数据
        AppLog.d(TAG, "--------end----");
        startProgressDialog();
        IntentFilter filter=new IntentFilter();
        filter.addAction(Constants.INTENT_INSTALL_COMPLETED);
        filter.addAction(Constants.INTENT_UNINSTALL_COMPLETED);
        filter.addAction(Constants.INTENT_UPDATE_COMPLETED);
        appReceiver=new AppReceiver();
        this.registerReceiver(appReceiver, filter);
	}


	private void initThread() {
		if (mLoadMenuTask != null && mLoadMenuTask.getStatus() != LoadMenuTask.Status.FINISHED) {
        	mLoadMenuTask.cancel(true);
        }        
        mLoadMenuTask = (LoadMenuTask) new LoadMenuTask().execute();
        
        mThread=new LoadAppThread(MainActivity.this,mPageBean,RequestParam.Action.GETRECOMMENDLIST);
        mThread.setPriority(Thread.MIN_PRIORITY);
        mThread.start();
        
        mLoadSpreeThread = new LoadSpreeThread();
        mLoadSpreeThread.setPriority(Thread.MIN_PRIORITY);
        mLoadSpreeThread.start();
	}
	

	private void setupViews() {
		mRootLayout = (RelativeLayout)findViewById(R.id.root_relativelayout);
		mDownloadRecord = (TextView) this.findViewById(R.id.top_down_record);
		mSearch = (TextView) this.findViewById(R.id.top_search);
		mImageView = (ImageView) this.findViewById(R.id.animation_imageview);
		control=new MoveControl(this,mImageView);
		mViewFlipper = (ViewFlipper) this.findViewById(R.id.app_store_viewflipper);
		mMenuLinearLayout = (LinearLayout) this.findViewById(R.id.app_store_menu_layout);
		top2_spree = (TextView)this.findViewById(R.id.top2_spree);
		top2_spree.setFocusable(false);
		top2_spree.setVisibility(View.GONE);

		titleImage=(ImageView)this.findViewById(R.id.top2_more);
		mDownloadRecord.setOnClickListener(this);
		mSearch.setOnClickListener(this);
		mDownloadRecord.setOnFocusChangeListener(this);
		mSearch.setOnFocusChangeListener(this);
		mSearch.setNextFocusDownId(top2_spree.getId());
		mDownloadRecord.setNextFocusDownId(top2_spree.getId());
		mDownloadRecord.setTag("mDownloadRecord");
		mSearch.setTag("mSearch");
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
            case MSG_INIT_SPREE:
                top2_spree.setVisibility(View.VISIBLE);
                top2_spree.setFocusable(true);
                top2_spree.setFocusableInTouchMode(true);
                top2_spree.setTag("top2_spree");
                top2_spree.setNextFocusUpId(mSearch.getId());
                top2_spree.setOnClickListener(MainActivity.this);
                top2_spree.setOnFocusChangeListener(MainActivity.this);
                top2_spree.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    public void onFocusChange(View v, boolean hasFocus) {
                        if(hasFocus){
                            control.addFocusView(v);
                            isHaveFoucs = true;
                        }else{
                            isHaveFoucs = false;
                        }
                    }
                });
                break;
            case MSG_REFRESHSPREE:
            	/* 定时刷新大礼包信息 */
                if(spreesSize<=0){
                    break;
                }
                if(isHaveFoucs){
                    mHandler.removeMessages(MSG_REFRESHSPREE);
                    mHandler.sendEmptyMessageDelayed(MSG_REFRESHSPREE, 5);
                    break;
                }
                spreeLoopTimes++;
            	Drawable icon = null;
            	if(spreeIcon.containsKey(sprees.get(spreeLoopTimes).getApppackageid())){
            		icon = spreeIcon.get(sprees.get(spreeLoopTimes).getApppackageid());
            	}else{
            		icon = spreeDrawable;
            	}
            	String name = sprees.get(spreeLoopTimes).getName();
            	setSpree(name, icon,spreeLoopTimes);
            	if(spreeLoopTimes==spreesSize-1){
            		spreeLoopTimes = -1;
            	}
            	break;
            }
		}
	};
	 
	public synchronized void setSpree(String name,Drawable icon,int times){
	    if(timerRun){
	        top2_spree.setText(name);
	        top2_spree.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
	        mpackageId = sprees.get(spreeLoopTimes).getApppackageid();
            mProductCode = sprees.get(spreeLoopTimes).getApppackageproductcode();
            isHaveSpree = true;
	    }
	    AppLog.d(TAG, "----spree >>> "+name+"    >>>>index="+times);
	    mHandler.removeMessages(MSG_REFRESHSPREE);
	    mHandler.sendEmptyMessageDelayed(MSG_REFRESHSPREE, 5000);
	}
	
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
    		};
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
        setPropertyIcon(view, appsBean);//设置其它要显示的图标
    	bar.setFocusable(false);
		if(index < 4){
    		button.setNextFocusUpId(top2_spree.getId());
    	}
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
			Bitmap bm = CaCheManager.requestBitmap(appsBean.getNatImageUrl());
			if(bm!=null){
			    button.setImageBitmap(bm);
			}else{
			    button.setImageResource(R.drawable.app_default_img);
			}
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

		if(event.getAction()==KeyEvent.ACTION_DOWN){
			control.firstTime=System.currentTimeMillis();
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
//        		if(mMoreLayout.hasFocus()){//推荐位时快按或长按
//        			if(mMoveDirect==View.FOCUS_LEFT){
//        				long currentTime=System.currentTimeMillis();
//        				if((currentTime-pressTime)>AnimUtils.ANIMATION_PAGE_TIME){
//        					this.previousFlipperPage(true);
//        					pressTime=currentTime;
//        				}
//        				return true;
//        			}else if(mMoveDirect==View.FOCUS_RIGHT){
//        				long currentTime=System.currentTimeMillis();
//        				if((currentTime-pressTime)>AnimUtils.ANIMATION_PAGE_TIME){
//        					this.nextFlipperPage(true);
//        					pressTime=currentTime;
//        				}
//        				return true;
//        			}
//        		}
        		View nextview=mFocusF.findNextFocus(parent, this.getCurrentFocus(),mMoveDirect);
        		AppLog.d(TAG, "nextView="+nextview+";mMoveDirect="+mMoveDirect);
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
            			AppLog.d(TAG, "---nextview.tag="+nextview.getTag());
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
		control.mDelay=true;
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
		
//		if(!mMoreLayout.hasFocus()){
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
//		}else{
//			View currentView=mViewFlipper.getCurrentView();
//			if(GridLayout.class.isInstance(currentView)){
//				titleImage.setImageResource(R.drawable.new_title);
//				pageSearch.setBackgroundResource(R.drawable.home_search);
//			}else{
//				titleImage.setImageResource(R.drawable.searchtitle1);
//				pageSearch.setBackgroundResource(R.drawable.home_search_big);
//			}
//			control.mDelay=false;
//			mImageView.setVisibility(View.VISIBLE);
//		}
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
//		if(!mMoreLayout.hasFocus()){
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
//		}else{
//			View currentView=mViewFlipper.getCurrentView();
//			if(GridLayout.class.isInstance(currentView)){
//				titleImage.setImageResource(R.drawable.new_title);
//				pageSearch.setBackgroundResource(R.drawable.home_search);
//			}else{
//				titleImage.setImageResource(R.drawable.searchtitle1);
//				pageSearch.setBackgroundResource(R.drawable.home_search_big);
//			}
//			control.mDelay=false;
//			mImageView.setVisibility(View.VISIBLE);
//		}
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
    	
    	edit = (EditText) view.findViewById(R.id.search_edittext);
    	search = (ImageView) view.findViewById(R.id.search_button_search);
        all_app = (ImageView)view.findViewById(R.id.search_all_app);
        all_queen = (ImageView)view.findViewById(R.id.search_all_queen);
    	
        edit.setOnFocusChangeListener(this);
        search.setOnFocusChangeListener(this);
        all_app.setOnFocusChangeListener(this);
        all_queen.setOnFocusChangeListener(this);
        edit.setTag("edit");
        search.setTag("search");
    	all_app.setTag("all_app");
    	all_queen.setTag("all_queen");
    	edit.setNextFocusUpId(top2_spree.getId());
    	edit.setNextFocusLeftId(-1);
    	edit.setNextFocusRightId(search.getId());
    	search.setNextFocusUpId(top2_spree.getId());
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
				if(keyCode==KeyEvent.KEYCODE_ENTER || keyCode==KeyEvent.KEYCODE_DPAD_CENTER){
					InputMethodManager im = ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE));
					im.showSoftInput(edit,0);
					isHasSoftKey = true;
					return true;
				}
				if(keyCode==KeyEvent.KEYCODE_ESCAPE||keyCode==KeyEvent.KEYCODE_BACK){
					if(isHasSoftKey){
						InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
						imm.hideSoftInputFromWindow(edit.getWindowToken(),0);
						isHasSoftKey = false;
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
	
    View viewFirst;
    View viewLast;
    public void initMenuLayout(){
    	mMenuLinearLayout.removeAllViews();
    	AppLog.d(TAG, "-------------"+mMenuList.size());
    	for(int i=0;i<mMenuList.size();i++){
    		View view = mLayoutInflater.inflate(R.layout.menu_item_layout, null);
    		view.setTag(i);
    		view.setOnClickListener(this);
    		view.setOnFocusChangeListener(this);
//    		MenuButton button = (MenuButton) view.findViewById(R.id.menu_item_img);
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
    				Bitmap bm = CaCheManager.requestBitmap((String) map.get("image"));
    				if(bm!=null){
    				    button.setImageBitmap(bm);
    				}else{
    				    button.setImageResource(R.drawable.menu_default_img);
    				}
    			}
    		}
    		if(i==0){
    			viewFirst = view;
    			viewFirst.setId(Constants.MENU_LEFT_ID);
    		}
    		if(i==mMenuList.size()-1){
    			viewLast = view;
    			viewLast.setId(Constants.MENU_RIGHT_ID);
    		}
    		String name = (String) map.get("name");
    		nameText.setText(name);
        	mMenuLinearLayout.addView(view);
    	}
    	viewFirst.setNextFocusLeftId(viewLast.getId());
    	viewLast.setNextFocusRightId(viewFirst.getId());
    	
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
	
	/*public class LatestThread extends Thread{
		
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
			if(null!=mPageBean){
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
		
	}*/


	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if(hasFocus){
			control.addFocusView(v);
		}
	}
	
	public void onResume(){
		super.onResume();
		if(isSearchButton){
			AppLog.d(TAG, "-------------after AppSearchLayout : -----main onResume----------");
			search.requestFocus();
			isSearchButton = false;
		}
		AppLog.d(TAG, "-------onResume---------");
		mHandler.removeMessages(MSG_REFRESHSPREE);
		mHandler.sendEmptyMessageDelayed(MSG_REFRESHSPREE, 1000);
		spreeLoopTimes = -1;
		timerRun = true;
	}
	
	public void onPause(){
	    if(mHandler!=null){
	        mHandler.removeMessages(MSG_REFRESHSPREE);
	    }
		super.onPause();
		timerRun = false;
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
//		if(mThreadLatest!=null&&mThreadLatest.isAlive()){
//			mThreadLatest.setFlag(false);
//		}
	}
	
	
	
	@Override
	protected void onDestroy() {
		AppLog.d(TAG, "---------------onDestory-----------------");
		sprees.clear();
		spreeIcon.clear();
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
			case R.id.menu_item://分类
			case Constants.MENU_LEFT_ID:
			case Constants.MENU_RIGHT_ID:
				if(v.getTag()==null) return;
				int position = (Integer) v.getTag();
	                AppLog.d(TAG, "111111111111111111");
	                Intent intent_class=new Intent(MainActivity.this,ClassActivity.class);
	                intent_class.putExtra("type_id",(Integer)mMenuList.get(position).get("id"));
	                startActivity(intent_class);
	                overridePendingTransition(R.anim.zoomin,R.anim.zoomout);
				break;
			case R.id.search_button_search:
				Intent intentSearch = new Intent(MainActivity.this,AppSearchActivity.class);
				intentSearch.putExtra("keyWord", edit.getText().toString());
				AppLog.d(TAG, "--------------keyword="+edit.getText().toString());
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
			case R.id.top2_spree:/* 大礼包 */
			    if(isHaveSpree){
			        GiftsPackActivity.openGiftsOrderDialog(this, mpackageId+"",mProductCode) ;
			    }
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
								ImageView button = (ImageView) (tGrid.getChildAt(j)).findViewById(R.id.home_item_img);
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
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	
	/* 礼包 Thread */
	private class LoadSpreeThread extends Thread{
		private String urlStr = RequestParam.SERVICE_ACTION_URL+RequestParam.Action.GETAPPPACKAGELIST;
		@SuppressWarnings("unchecked")
		public void run() {
			super.run();
			Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
			Object obj = mNetUtil.getNetData(null, urlStr, null, new SpreeParser());
			if(obj!=null){
				sprees = (List<SpreeBean>) obj;
				spreesSize = sprees.size();
				if(spreesSize>0){
				    AppLog.e(TAG, "------ spreesSize = "+spreesSize);
				    for(int i=0;i<spreesSize;i++){
				        SpreeBean spreeBean = sprees.get(i);
				        AppLog.d(TAG, ">>>>>>>>>----spreeBean.name = : "+spreeBean.getName());
				        boolean res = NetUtil.loadImage(spreeBean.getSerImageUrl(), spreeBean.getNatImageUrl());
				        if(res){
				            if(!spreeIcon.containsKey(spreeBean.getApppackageid())){
				                Drawable icon = FileUtils.fileToDrawable(spreeBean.getNatImageUrl());
				                if(icon!=null){
				                    spreeIcon.put(spreeBean.getApppackageid(), icon);
				                }
				            }
				        }
				    }
				    if(spreeDrawable==null){
				        spreeDrawable = MainActivity.this.getResources().getDrawable(R.drawable.spree_icon);
				    }
				    
				    /* start spree turn */
				    mHandler.removeMessages(MSG_REFRESHSPREE);
				    mHandler.sendEmptyMessage(MSG_REFRESHSPREE);

				    /* init spree focus */
				    mHandler.sendEmptyMessage(MSG_INIT_SPREE);
				}
			}else{
				sprees = null;
				AppLog.e(TAG, "---- List<SpreeBean> = null");
			}
		}
	}
}