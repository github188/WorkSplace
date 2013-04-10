package com.joysee.appstore.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.joysee.appstore.R;
import com.joysee.appstore.animation.AnimUtils;
import com.joysee.appstore.animation.MoveControl;
import com.joysee.appstore.common.AppsBean;
import com.joysee.appstore.common.Constants;
import com.joysee.appstore.common.PageBean;
import com.joysee.appstore.common.TaskBean;
import com.joysee.appstore.db.DBUtils;
import com.joysee.appstore.parser.MenusParser;
import com.joysee.appstore.thread.LoadAppThread;
import com.joysee.appstore.utils.AppLog;
import com.joysee.appstore.utils.CaCheManager;
import com.joysee.appstore.utils.NetUtil;
import com.joysee.appstore.utils.RequestParam;
import com.joysee.appstore.utils.Utils;

import android.R.color;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class ClassActivity extends BaseActivity implements View.OnClickListener,View.OnFocusChangeListener{
	
public static final String TAG = "com.joysee.appstore.ClassActivity";
    
    public static final int MSG_CHANGE_CLASS_STYE=6;//给分类改变样式
    public static final int MSG_NET_NOT_CONNECT=7;//网络连接不通
    public static final int MSG_UPDATE_PROGRESS=8;//更新进度条
    public static final int MSG_SHOW_INSTALL=9;//安装成功
    public static final int MSG_SHOW_UNINSTALL=11;//卸载成功
    public static final int MSG_SHOW_DOWN_ERROR=13;//下载失败
    public static final int MSG_FOCUS=10;
    public static final int MSG_HIDE_UPDATE=12;//升级完取消可升级图标
    
    private RelativeLayout mRootLayout;
    private ViewFlipper mViewFlipper;
    private GridLayout  mGridLayout;
    private LayoutInflater  mLayoutInflater;
    private LinearLayout mMenuLinearLayout;
    
    private TextView pageView;
    private TextView mDownRecord;
    private TextView mSearch;//右上角搜索按钮 
    private List <Map<String, Object>> menuAdpaterList=new ArrayList<Map<String, Object>>();
    private static List<AppsBean> mList=null;//当前显示的应用
    private String mActionType=RequestParam.Action.GETRECOMMENDLIST;//当前请求类型url,默认为推荐应用
    private PageBean mPageBean;
    private long mPressTime;
    private DBUtils tDBUtils ;
    private NetUtil mNetUtil;
    private int turnRight=0;//1表示向右翻页，2表示向左翻页
    private int mLoadNum=0;//当加载完菜单，应用时，2才会取消进度条
    private Utils mUtils;
    private int typeID;//分类ID
    private boolean isLunchGame=false;//是否从lunach中游戏 工具进入
    
    public Handler workHandler;
    private LoadAppThread mThread;
    private DownReceiver downReceiver;
    
    private LoadMenuTask mLoadMenuTask= null;
	
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        AppLog.d(TAG," onCreate ");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.app_class_layout);
        tDBUtils=new DBUtils(ClassActivity.this);
        mUtils = new Utils();
        mNetUtil = new NetUtil(ClassActivity.this);
        parent=(ViewGroup)this.findViewById(R.id.rootLayout);
        setupViews();
        viewGroupFocus=parent.getDescendantFocusability();
        parent.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        mPageBean=new PageBean();
        processExtraData();
        IntentFilter filter=new IntentFilter();
        filter.addAction(Constants.INTENT_INSTALL_COMPLETED);
        filter.addAction(Constants.INTENT_UNINSTALL_COMPLETED);
        filter.addAction(Constants.INTENT_DOWNLOAD_PROGRESS);
        filter.addAction(Constants.INTENT_DOWNLOAD_DETLET);
        filter.addAction(Constants.INTENT_UPDATE_COMPLETED);
        filter.addAction(Constants.INTENT_INSTALL_FAIL);
        filter.addAction(Constants.INTENT_DOWNLOAD_ERROR);
        filter.addAction(Constants.INTENT_DOWNLOAD_STARTED);
        filter.addAction(Constants.INTENT_DOWNLOAD_PAUSE);
        downReceiver=new DownReceiver();
        this.registerReceiver(downReceiver, filter);
    }
    
    public void setupViews(){
    	pageView=(TextView)this.findViewById(R.id.page);
    	mImageView = (ImageView) this.findViewById(R.id.animation_imageview);
    	control=new MoveControl(this,mImageView);
        mDownRecord=(TextView)this.findViewById(R.id.top_down_record);
        mSearch = (TextView)this.findViewById(R.id.top_search);
        mRootLayout = (RelativeLayout)findViewById(R.id.rootLayout);
        mViewFlipper = (ViewFlipper) this.findViewById(R.id.app_class_viewflipper);
        mMenuLinearLayout = (LinearLayout) this.findViewById(R.id.class_menu_linearlayout);
        
        mDownRecord.setOnClickListener(this);
        mSearch.setOnClickListener(this);
        mSearch.setOnFocusChangeListener(this);
        mDownRecord.setOnFocusChangeListener(this);
        mLayoutInflater = this.getLayoutInflater();
        mDownRecord.setTag("mDownloadRecord");
        mSearch.setTag("mSearch");
    }
    
    public Handler mainHandler=new Handler(){
        public void handleMessage(Message msg) {
            switch(msg.what){
            case MSG_CHANGE_CLASS_STYE:
				setMenuStyle(typeID);
                break;
            case MSG_NET_NOT_CONNECT:
                break;
            case MSG_UPDATE_PROGRESS:
            	AppLog.d(TAG, "------------MSG_UPDATE_PROGRESS-------------");
            	if(mGridLayout==null){
            		return;
            	}
            	int index=msg.arg1;
            	View viewItem=mGridLayout.getChildAt(index);
            	if(viewItem==null){
            		return;
            	}
            	Intent data=(Intent)msg.obj;
            	int file_sum=data.getIntExtra("file_sum", -1);
            	int down_size=data.getIntExtra("download_size", -1);
            	if(viewItem.getVisibility()!=View.VISIBLE){
            		return;
            	}
            	ProgressBar progress=(ProgressBar)viewItem.findViewById(R.id.down_progress);
                if(progress.getVisibility()!=View.VISIBLE){
                	RatingBar bar = (RatingBar)viewItem.findViewById(R.id.class_rating_bar);
                	bar.setVisibility(View.GONE);
                	progress.setVisibility(View.VISIBLE);
            	}
                TextView priceView=(TextView) mGridLayout.getChildAt(index).findViewById(R.id.item_price);
            	priceView.setText(""+ClassActivity.this.getString(R.string.download_tab_2));
            	priceView.setVisibility(View.VISIBLE);
                if(file_sum==0){
                	progress.setProgress(0);
                }else{
                	long down=down_size;
                    long sum=file_sum;
                    int res=(int)(down*100/sum);
                	progress.setProgress(res);
                }
            	refeshDown=true;
            	break;
            case MSG_SHOW_DOWN_ERROR:
            	AppLog.d(TAG, "------------MSG_SHOW_DOWN_ERROR-------------");
            	View viewError=mGridLayout.getChildAt(msg.arg1);
            	ProgressBar progressErr=(ProgressBar)viewError.findViewById(R.id.down_progress);
            	AppLog.d(TAG, "------------MSG_SHOW_DOWN_ERROR------------progressErr.getVisibility()="+progressErr.getVisibility());
            	if(progressErr.getVisibility()==View.VISIBLE){
            		TextView price=(TextView)viewError.findViewById(R.id.item_price);
                	price.setText(""+ClassActivity.this.getString(R.string.download_error));
            	}
            	break;
            case MSG_SHOW_INSTALL:
            	AppLog.d(TAG, "------------MSG_SHOW_INSTALL------------index="+msg.arg1);
            	int indexInstall=msg.arg1;
            	if(null!=mGridLayout){
            		View view=mGridLayout.getChildAt(indexInstall);
            		if(null!=view&&view.getVisibility()==View.VISIBLE){
            			AppLog.d(TAG, "------------MSG_SHOW_INSTALL----");
            			TextView textView=((TextView)view.findViewById(R.id.item_price));
            			textView.setText(""+ClassActivity.this.getString(R.string.download_tab_3));
            			textView.setVisibility(View.VISIBLE);
                    	(view.findViewById(R.id.down_progress)).setVisibility(View.GONE);
                    	(view.findViewById(R.id.class_rating_bar)).setVisibility(View.VISIBLE);
            		}
            	}
            	break;
            case MSG_SHOW_UNINSTALL:
            	AppLog.d(TAG, "------------MSG_SHOW_UNINSTALL------------index="+msg.arg1);
            	int indexUnInstall=msg.arg1;
            	if(null!=mGridLayout){
            		View view=mGridLayout.getChildAt(indexUnInstall);
            		if(null!=view&&view.getVisibility()==View.VISIBLE){
            			AppLog.d(TAG, "------------MSG_SHOW_UNINSTALL----");
            			TextView textView=((TextView)view.findViewById(R.id.item_price));
            			textView.setText(""+ClassActivity.this.getString(R.string.noinstalled));
            			textView.setVisibility(View.VISIBLE);
                    	(view.findViewById(R.id.down_progress)).setVisibility(View.GONE);
                    	(view.findViewById(R.id.class_rating_bar)).setVisibility(View.VISIBLE);
                    	(view.findViewById(R.id.class_item_pop)).setVisibility(View.GONE);
            			(view.findViewById(R.id.pop_update)).setVisibility(View.GONE);
            		}
            	}
            	break;
            case MSG_HIDE_UPDATE:
            	AppLog.d(TAG, "------------MSG_HIDE_UPDATE------------index="+msg.arg1);
            	int indexUpdate=msg.arg1;
            	if(null!=mGridLayout){
            		View view=mGridLayout.getChildAt(indexUpdate);
            		if(null!=view&&view.getVisibility()==View.VISIBLE){
            			AppLog.d(TAG, "------------MSG_HIDE_UPDATE----");
            			(view.findViewById(R.id.class_item_pop)).setVisibility(View.GONE);
            			(view.findViewById(R.id.pop_update)).setVisibility(View.GONE);
            		}
            	}
            	break;
            case MSG_FOCUS:
            	break;
            }
            super.handleMessage(msg);
        }
    };
    
    public void refreshMenusList(List<AppsBean> menuList){
    	parent.setDescendantFocusability(viewGroupFocus);
    	if(null==menuList){
    		AppLog.d(TAG, "-----------refreshMenusList---------menuList=null");
    		if(isLunchGame){
    		    startNetErrorDialog();    		    
    		}
    		return;
    	}
    	mLoadNum++;
    	if(mLoadNum>=2){
    		stopProgressDialog();
    	}
    	getMenuData(menuList);
    	initMenu();
    	String enterT=this.getIntent().getStringExtra("enter_type");
    	if(mLoadNum<2&&enterT!=null&&enterT.equals("outer")&&typeID>=0){
    		AppLog.d(TAG, "-------enterType--is---outer----typeID="+typeID);
    		if(typeID==0){
    			for(AppsBean appB:menuList){
    				AppLog.d(TAG, "game="+ClassActivity.this.getString(R.string.type_game));
    				if(appB.getAppName().equals(ClassActivity.this.getString(R.string.type_game))){//游戏
    					AppLog.d(TAG, "---------id="+appB.getID()+";name="+appB.getAppName());
        				typeID=appB.getID();
        			}
    			}
    		}else if(typeID==1){
    			for(AppsBean appB:menuList){
    				AppLog.d(TAG, "tools="+ClassActivity.this.getString(R.string.type_tools));
    				if(appB.getAppName().equals(ClassActivity.this.getString(R.string.type_tools))){//工具
    					AppLog.d(TAG, "---------id="+appB.getID()+";name="+appB.getAppName());
        				typeID=appB.getID();
    				}
    			}
    		}
    		mPageBean.setTypeId(typeID);
    		mainHandler.sendEmptyMessageDelayed(MSG_CHANGE_CLASS_STYE, 100);
    		AppLog.d(TAG, "-------enterType--is---outer----typeID="+typeID+";mActionType="+mActionType);
    		mThread=new LoadAppThread(ClassActivity.this,mPageBean,mActionType);
            mThread.start();
            startProgressDialog();
    	}else{
    		mainHandler.sendEmptyMessageDelayed(MSG_CHANGE_CLASS_STYE, 100);
    	}
	}
        
	View viewFirst;
	View viewLast;
    public void initMenu(){
    	mMenuLinearLayout.removeAllViews();
    	AppLog.d(TAG, "-----  --------"+menuAdpaterList.size());
    	for(int i=0;i<menuAdpaterList.size();i++){
    		View view = mLayoutInflater.inflate(R.layout.class_menu_item_layout, null);
    		TextView button = (TextView) view.findViewById(R.id.menu_item_img);
    		Map<String, Object> map = menuAdpaterList.get(i);
    		String name = (String) map.get("name");
    		AppLog.d(TAG, "-------------"+name);
            button.setText(name);
            button.setTag((Integer)map.get("id"));
            view.setTag((Integer)map.get("id"));
            view.setOnClickListener(this);
            view.setOnFocusChangeListener(this);
        	mMenuLinearLayout.addView(view);
        	if(i==0){
        		viewFirst = view;
        		viewFirst.setId(Constants.MENU_LEFT_ID);
        	}
        	if(i==menuAdpaterList.size()-1){
        		viewLast = view;
        		viewLast.setId(Constants.MENU_RIGHT_ID);
        	}
    	}
    	viewFirst.setNextFocusLeftId(viewLast.getId());
    	viewLast.setNextFocusRightId(viewFirst.getId());
    	
    	int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        mMenuLinearLayout.measure(w, h);
        int width = mMenuLinearLayout.getMeasuredWidth();
        LayoutParams lp1 = mMenuLinearLayout.getLayoutParams();
        lp1.width = width;
    }
    
    public void initViewFillper(int listSize) {
    	mViewFlipper.removeAllViews();
		mGridLayout = new GridLayout(this);
		mGridLayout.setColumnCount(4);
		for (int i = 0; i < listSize; i++) {
			View view = mLayoutInflater.inflate(R.layout.class_item_layout,null);
			ImageView button = (ImageView) view.findViewById(R.id.class_item_img);
			button.setFocusable(true);
			button.setOnClickListener(this);
			button.setOnFocusChangeListener(this);
			mGridLayout.addView(view);
		}
		LayoutAnimationController controller = new LayoutAnimationController(AnimationUtils.loadAnimation(this, R.anim.list_animation), 1);
		mGridLayout.setLayoutAnimation(controller);
		mViewFlipper.addView(mGridLayout);
		AppLog.d(TAG,"mViewFlipper.getChildCount()" + mViewFlipper.getChildCount());
	}
    
    View refreshFocusView=null;
    public void refreshAppsList(List<AppsBean> appList){
    	mLoadNum++;
    	if(mLoadNum>=2){
    		stopProgressDialog();
    	}
    	if(appList==null){//网络异常
    		parent.setDescendantFocusability(viewGroupFocus);
    		startNetErrorDialog();
    		return;
    	}
    	refreshFocusView=null;
    	parent.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
    	initViewFillper(appList.size());
    	mList=appList;
    	pageView.setText(ClassActivity.this.getString(R.string.page, appList.size()>0?mPageBean.getPageNo():0,mPageBean.getPageTotal()));
		AppLog.d(TAG, "-----------appList size="+appList.size());
		for(int i=0;i<appList.size();i++){
			View view=mGridLayout.getChildAt(i);
			ImageView ab=(ImageView)view.findViewById(R.id.class_item_img);
			ab.setOnClickListener(this);
			ab.setTag(appList.get(i));
			AppLog.d(TAG, "------i="+i+";appname="+appList.get(i).getAppName());
			if(null==appList.get(i).getNatImageUrl()||appList.get(i).getNatImageUrl().equals("")){
				ab.setImageResource(R.drawable.app_default_img);
			}else{
//				ab.setImageURI(Uri.parse(appList.get(i).getNatImageUrl()));
				Bitmap bitmap = CaCheManager.requestBitmap(appList.get(i).getNatImageUrl());
				if(bitmap!=null){
					ab.setImageBitmap(bitmap);
				}else{
					ab.setImageResource(R.drawable.app_default_img);
					Log.d("benz", "---bitmap==null   setDefault---");
				}
			}
			((TextView)view.findViewById(R.id.item_name)).setText(appList.get(i).getAppName());
			ImageView popIv = (ImageView)view.findViewById(R.id.class_item_pop);
			ImageView popIvT = (ImageView)view.findViewById(R.id.pop_update);
			TextView priceView=(TextView) view.findViewById(R.id.item_price);
			ProgressBar progress=(ProgressBar)view.findViewById(R.id.down_progress);
			RatingBar bar = (RatingBar)view.findViewById(R.id.class_rating_bar);
			bar.setFocusable(false);
			String soc=appList.get(i).getScore()==null?"0":appList.get(i).getScore().trim();
			float res=Integer.valueOf(soc.equals("")?"0":soc);
			res = res / 2;
			AppLog.d(TAG, "---------------res="+res+";(float)res="+(float)res+";pkgname="+appList.get(i).getPkgName());
			bar.setRating((float)res);
			double score=(appList.get(i).getPrice());
			int appStatus = tDBUtils.queryStatusByPkgName(appList.get(i).getPkgName());
			switch(appStatus){
			case Constants.APP_STATUS_DOWNLOADING:
			case Constants.DOWNLOAD_STATUS_ERROR:
			case Constants.DOWNLOAD_STATUS_PAUSE:
            case Constants.DOWNLOAD_STATUS_EXIT_STOP:
            	int appSts=tDBUtils.queryStatusByPkgNameAndVersion(appList.get(i).getPkgName(),appList.get(i).getVersion());
            	AppLog.d(TAG, "-----------------------appSts="+appSts);
            	switch(appSts){
	            	case Constants.APP_STATUS_DOWNLOADING:
	    			case Constants.DOWNLOAD_STATUS_ERROR:
	    			case Constants.DOWNLOAD_STATUS_PAUSE:
	                case Constants.DOWNLOAD_STATUS_EXIT_STOP:
	                	if(appStatus==Constants.DOWNLOAD_STATUS_ERROR){
	                		priceView.setText(""+ClassActivity.this.getString(R.string.download_error));
	                	}else if(appStatus==Constants.DOWNLOAD_STATUS_PAUSE){
	                		priceView.setText(""+ClassActivity.this.getString(R.string.pause_download));
	                	}else{
	                		priceView.setText(""+ClassActivity.this.getString(R.string.download_tab_2));
	                	}
	                	progress.setVisibility(View.VISIBLE);
	                	int sumSize=tDBUtils.querySumSizeByPkgName(appList.get(i).getPkgName());
	                	int downSize=tDBUtils.queryDowningSizeByPkgName(appList.get(i).getPkgName());
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
	                	int appS=tDBUtils.queryStatusByPkgNameFormApplication(appList.get(i).getPkgName());
	                	if(appS==Constants.APP_STATUS_UPDATE||appS==Constants.APP_STATUS_INSTALLED){
	                		priceView.setText(""+ClassActivity.this.getString(R.string.download_tab_3));
	                		//对比已安装的应用的新旧版本
		                	String newVersion = appList.get(i).getVersion(); //------by xubin
		                	String oldVersion = tDBUtils.queryVersionByPkgName(appList.get(i).getPkgName());
		                	AppLog.d(TAG, "-----------------newVersion :"+newVersion +"| -------oldVersion :" +oldVersion);
		                	if(Utils.CompareVersion(newVersion, oldVersion)){
		                		popIv.setVisibility(View.VISIBLE);
		                    	popIvT.setVisibility(View.VISIBLE);
		                	}
	                	}else if(appS==Constants.APP_STATUS_UNDOWNLOAD){
	                		priceView.setText(""+ClassActivity.this.getString(R.string.noinstalled));
	                	}
	                	bar.setVisibility(View.VISIBLE);
	                	break;
            	}
            	break;
            case Constants.APP_STATUS_DOWNLOADED: 
            	priceView.setText(""+ClassActivity.this.getString(R.string.download_tab_2));
            	progress.setVisibility(View.VISIBLE);
            	progress.setProgress(100);
            	popIv.setVisibility(View.GONE);
            	popIvT.setVisibility(View.GONE);
            	break;
            case Constants.APP_STATUS_INSTALLED://已安装
            	priceView.setText(""+ClassActivity.this.getString(R.string.download_tab_3));
            	bar.setVisibility(View.VISIBLE);
            	//对比已安装的应用的新旧版本
            	String newVersion = appList.get(i).getVersion(); //------by xubin
            	String oldVersion = tDBUtils.queryVersionByPkgName(appList.get(i).getPkgName());
            	AppLog.d(TAG, "-----------------newVersion :"+newVersion +"| -------oldVersion :" +oldVersion);
            	if(Utils.CompareVersion(newVersion, oldVersion)){
            		popIv.setVisibility(View.VISIBLE);
                	popIvT.setVisibility(View.VISIBLE);
            	}
            	break;
            default:
            	if(score<=0){
    				//priceView.setText(""+ClassActivity.this.getString(R.string.app_free));
    			}else{
    				//priceView.setText(""+ClassActivity.this.getString(R.string.app_no_free,score));
    			}
            	priceView.setText(""+ClassActivity.this.getString(R.string.noinstalled));
            	bar.setVisibility(View.VISIBLE);
            	break;
			}
			view.setVisibility(View.VISIBLE);
			if(i==0){
				refreshFocusView=ab;
			}
		}
		parent.setDescendantFocusability(viewGroupFocus);
		if(refreshFocusView!=null){
			refreshFocusView.setFocusable(true);
			refreshFocusView.setAlpha(AnimUtils.VIEW_ALPHA_FLAG);
			refreshFocusView.setFocusableInTouchMode(true);
			refreshFocusView.requestFocus();
		}else{
			requestMenu(typeID);
		}
		for(int i=0;i<mGridLayout.getChildCount();i++){
			ImageView button=(ImageView) mGridLayout.getChildAt(0).findViewById(R.id.class_item_img);
			button.setOnKeyListener(null);
		}
		if(appList.size()==1){
			ImageView button=(ImageView) mGridLayout.getChildAt(0).findViewById(R.id.class_item_img);
			button.setOnKeyListener(new LeftAndRightListener());
		}else if(appList.size()>1&&appList.size()<5){
			ImageView button1=(ImageView) mGridLayout.getChildAt(0).findViewById(R.id.class_item_img);
			button1.setOnKeyListener(new LeftListener());
			ImageView button2=(ImageView) mGridLayout.getChildAt(appList.size()-1).findViewById(R.id.class_item_img);
			button2.setOnKeyListener(new RightListener());
		}else if(appList.size()>=5&&appList.size()<=8){
			ImageView button1=(ImageView) mGridLayout.getChildAt(0).findViewById(R.id.class_item_img);
			button1.setOnKeyListener(new LeftListener());
			ImageView button2=(ImageView) mGridLayout.getChildAt(3).findViewById(R.id.class_item_img);
			button2.setOnKeyListener(new RightListener());
			ImageView button3=(ImageView) mGridLayout.getChildAt(4).findViewById(R.id.class_item_img);
			button3.setOnKeyListener(new LeftListener());
			ImageView button4=(ImageView) mGridLayout.getChildAt(appList.size()-1).findViewById(R.id.class_item_img);
			button4.setOnKeyListener(new RightListener());
		}
	}
    
    class LeftAndRightListener implements View.OnKeyListener{
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if(event.getAction()==KeyEvent.ACTION_DOWN&&keyCode==KeyEvent.KEYCODE_DPAD_RIGHT){
				nextPage();
				return true;
			}else if(event.getAction()==KeyEvent.ACTION_DOWN&&keyCode==KeyEvent.KEYCODE_DPAD_LEFT){
				previousPage();
				return true;
			}
			return false;
		}
	}
	
	class LeftListener implements View.OnKeyListener{
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if(event.getAction()==KeyEvent.ACTION_DOWN&&keyCode==KeyEvent.KEYCODE_DPAD_LEFT){
				previousPage();
				return true;
			}
			return false;
		}
	}
	class RightListener implements View.OnKeyListener{
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if(event.getAction()==KeyEvent.ACTION_DOWN&&keyCode==KeyEvent.KEYCODE_DPAD_RIGHT){
				nextPage();
				return true;
			}
			return false;
		}
	}

    public void setMenuStyle(int position){
    	TextView clickBut=null;
        for(int i=0;i<mMenuLinearLayout.getChildCount();i++){
            View view=mMenuLinearLayout.getChildAt(i);
            TextView tb=(TextView)view.findViewById(R.id.menu_item_img);
            tb.setBackgroundDrawable(null);
            tb.setTextColor(getResources().getColor(R.color.lantingxihei));
            if((Integer)tb.getTag()==position){
            	clickBut=tb;
            }
        }
        if(clickBut!=null){
        	clickBut.setBackgroundResource(R.drawable.button_selected);
        	clickBut.setTextColor(getResources().getColor(R.color.white));
        }
    }
    
    public void requestMenu(int position){
    	for(int i=0;i<mMenuLinearLayout.getChildCount();i++){
            View view=mMenuLinearLayout.getChildAt(i);
            TextView tb=(TextView)view.findViewById(R.id.menu_item_img);
            tb.setBackgroundDrawable(null);
            tb.setTextColor(getResources().getColor(R.color.lantingxihei));
            AppLog.d(TAG, "----position="+position+";tb.getTag()="+tb.getTag());
            if((Integer)tb.getTag()==position){
            	tb.setBackgroundResource(R.drawable.button_selected);
            	tb.setTextColor(getResources().getColor(R.color.white));
            	return;
            }
        }
    }
    /**
     * 下一页
     */
    public void nextPage(){
    	if(mThread.isAlive()){
    		return;
    	}
        if(!mPageBean.nextPage()){
        	if(mPageBean.getPageTotal()<2){
        		return;
        	}
        	mPageBean.setPageNo(1);
        }
        turnRight=1;
        mThread=new LoadAppThread(ClassActivity.this,mPageBean,mActionType);
        mThread.start();
        startProgressDialog();
    }
    
    /**
     * 上一页
     */
    public void previousPage(){
    	if(mThread.isAlive()){
    		return;
    	}
        if(!mPageBean.previousPage()){
        	if(mPageBean.getPageTotal()<2){
        		return;
        	}
        	mPageBean.setPageNo(mPageBean.getPageTotal());
        }
        turnRight=2;
        mThread=new LoadAppThread(ClassActivity.this,mPageBean,mActionType);
        mThread.start();
        startProgressDialog();
        
    }
    
    /**
     * 进入下载管理 
     */
    public void downloadMgr() {
        Intent intent = new Intent();
        intent.setClass(ClassActivity.this, DownloadRecordActivity.class);
        startActivity(intent);
    }
    
    @Override
	public boolean dispatchKeyEvent(KeyEvent event) {
    	mPressTime = System.currentTimeMillis();
    	if(event.getAction()==KeyEvent.ACTION_DOWN){
    		if(event.getKeyCode()==KeyEvent.KEYCODE_DPAD_DOWN||event.getKeyCode()==KeyEvent.KEYCODE_DPAD_LEFT||
    				event.getKeyCode()==KeyEvent.KEYCODE_DPAD_RIGHT||event.getKeyCode()==KeyEvent.KEYCODE_DPAD_UP){
    			downTimes++;
    			requestFocusFirst(mSearch);
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
//        		AppLog.d(TAG, "-------control.mDelay="+control.mDelay);
        		if(control.mDelay){//表示正在翻页中,由于翻页有延迟
        			return true;
        		}
        		View nextview=mFocusF.findNextFocus(parent, this.getCurrentFocus(),mMoveDirect);
//        		AppLog.d(TAG, "----nextview-"+nextview);
        		if(mViewFlipper!=null&&mViewFlipper.getChildCount()>0){
        			if(nextview==null){
            			if(mMoveDirect==View.FOCUS_LEFT&&AppsBean.class.isInstance(this.getCurrentFocus().getTag())&&control.focusQueue.size()==0){
            				this.previousPage();
            			}
            			isRunning=false;
            			return true;
            		}else{
            			if(nextview.getTag()!=null&&this.getCurrentFocus()!=null&&AppsBean.class.isInstance(this.getCurrentFocus().getTag())&&String.class.isInstance(nextview.getTag())){//当前为推荐位页面的翻页
            				if(mMoveDirect==View.FOCUS_RIGHT&&((String)nextview.getTag()).equals("mDownloadRecord")||((String)nextview.getTag()).equals("mSearch")){
            					if(control.focusQueue.size()==0){
            						this.nextPage();
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
    	}
		return super.dispatchKeyEvent(event);
	}

    /**
     * 分类上的数据
     * @return
     */
    private void getMenuData(List<AppsBean> menuList) { 
        Map<String, Object> map=new HashMap<String, Object>();
        menuAdpaterList.clear();
        map= new HashMap<String, Object>();
        map.put("name", getString(R.string.all_app));
        map.put("id", -3);
        menuAdpaterList.add(map);
        map= new HashMap<String, Object>();
        map.put("name", getString(R.string.new_app));
        map.put("id", -2);
        menuAdpaterList.add(map);
        map= new HashMap<String, Object>();
        map.put("name", getString(R.string.hot_app));
        map.put("id", -1);
        menuAdpaterList.add(map);
        for (AppsBean tAppsBean : menuList) {
			map = new HashMap<String, Object>();
			map.put("name", tAppsBean.getAppName());
			map.put("id", (Integer)tAppsBean.getID());
			menuAdpaterList.add(map);
		}
        AppLog.d(TAG, "---------menuAdpaterList.size="+menuAdpaterList.size());
    }
    
    @Override
	protected void onNewIntent(Intent intent) {
    	AppLog.d(TAG, "---------onNewIntent()---------intent"+intent.getIntExtra("app_id",-1));
		setIntent(intent);
		processExtraData();
	}

    private void processExtraData(){
    	Intent intent =getIntent();
    	typeID=this.getIntent().getIntExtra("type_id",RequestParam.CalssID.NEW_ID);
        mPageBean.setTypeId(typeID);
        AppLog.d(TAG, "typeId"+typeID);
        if (mLoadMenuTask != null && mLoadMenuTask.getStatus() != LoadMenuTask.Status.FINISHED) {
        	mLoadMenuTask.cancel(true);
        }        
        mLoadMenuTask = (LoadMenuTask) new LoadMenuTask().execute();
        switch(typeID){
        case RequestParam.CalssID.HOT_ID:
            mActionType=RequestParam.Action.GETTOPLIST;
            break;
        case RequestParam.CalssID.NEW_ID:
            mActionType=RequestParam.Action.GETLATESTAPPS;
            break;
        default:
            mActionType=RequestParam.Action.GETAPPLIST;
            break;
        }
        
        if(mThread!=null&&mThread.isAlive()){
        	mThread.setFlag(false);
        	mThread = null;
        }
        String enterType=this.getIntent().getStringExtra("enter_type");
        if(enterType!=null&&enterType.equals("outer")&&typeID>=0){
            isLunchGame=true;
        }else{
            isLunchGame=false;
        	mThread=new LoadAppThread(ClassActivity.this,mPageBean,mActionType);
            mThread.start();
            startProgressDialog();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        AppLog.d(TAG," onResume ");
        RelativeLayout mRootLayout = (RelativeLayout)findViewById(R.id.rootLayout);
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
        if (mLoadMenuTask != null && mLoadMenuTask.getStatus() != LoadMenuTask.Status.FINISHED) {
//        	mLoadMenuTask.cancel(true);
        }
    }
    
    protected void onDestroy(){
    	super.onDestroy();
    	if(downReceiver!=null){
    		this.unregisterReceiver(downReceiver);
    	}
    }
    
    
    private class LoadMenuTask extends AsyncTask<Object, Void, List<AppsBean>>{
		List<AppsBean> tMenuList;
		@Override
		protected List<AppsBean> doInBackground(Object... params) {
			AppLog.d(TAG, "----------LoadMenuTask-----doInBackground-------");
			String urlStr=RequestParam.getAppServerUrl(ClassActivity.this)+RequestParam.Action.GETAPPTYPELIST;
			tMenuList=(List<AppsBean>)mNetUtil.getNetData(null, urlStr, null, new MenusParser());
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

	@Override
	public void onClick(View v) {
		AppLog.d(TAG, "v.id="+v.getId());
		switch(v.getId()){
		case R.id.top_down_record:
			downloadMgr();
			break;
		case R.id.class_item_img:
			AppLog.d(TAG, "---------class_item_img----v.getTag()="+v.getTag());
			if(v.getTag()==null) return;
			AppsBean arg2=(AppsBean)v.getTag();
             Intent intent=new Intent(ClassActivity.this,DetailedActivity.class);
             intent.putExtra("app_id",arg2.getID());
             intent.putExtra("action_type", mActionType);
             intent.putExtra("type_id",typeID);
             AppLog.d(TAG, "-----app_id="+intent.getIntExtra("app_id",-1));
             startActivity(intent);
             overridePendingTransition(R.anim.zoomin,R.anim.zoomout);
			break;
		case R.id.menu_item:
		case Constants.MENU_LEFT_ID:
		case Constants.MENU_RIGHT_ID:
			int position = (Integer)v.getTag();
			typeID=position;
			setMenuStyle(position);
			 AppLog.d(TAG, "position="+position);
             if(position==RequestParam.CalssID.ALL_ID){//全部
                 mActionType=RequestParam.Action.GETAPPLIST;
                 mPageBean.setTypeId(-1);
             }else if(position==RequestParam.CalssID.NEW_ID){//最新 
                 mActionType=RequestParam.Action.GETLATESTAPPS;
             }else if(position==RequestParam.CalssID.HOT_ID){//最热
                 mActionType=RequestParam.Action.GETTOPLIST;
             }else{
                 mActionType=RequestParam.Action.GETAPPLIST;
                 mPageBean.setTypeId(position);
             }
             mPageBean.setPageNo(1);
             if(mThread.isAlive()){
            	 mThread.setFlag(false);
             }
             mThread=new LoadAppThread(ClassActivity.this,mPageBean,mActionType);
             mThread.setFlag(true);
             mThread.start();
             startProgressDialog();
			break;
		case R.id.top_search:
			Intent intentSearch=new Intent(ClassActivity.this,AppSearchActivity.class);
			startActivity(intentSearch);
			break;
		}
	}
	
	public boolean refeshDown=true;//控制一段时间来进行刷新
	
	public class DownReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			AppLog.d(TAG, "--------DownReceiver-----------action="+arg1.getAction());
			if(null==mList||mList.size()<1||mGridLayout==null){
				return;
			}
			if(arg1.getAction().equals(Constants.INTENT_DOWNLOAD_PROGRESS)){
				if(arg1.getStringExtra("server_app_id")==null||arg1.getStringExtra("server_app_id").trim().equals("")||arg1.getStringExtra("server_app_id").trim().equals("null")){
					return;
				}
				int app_id=Integer.valueOf(arg1.getStringExtra("server_app_id"));
				AppLog.d(TAG, "----------downing----app_id="+app_id);
				for(int i=0;i<mList.size();i++){
					if(mList.get(i).getID()==app_id){
						Message msg=mainHandler.obtainMessage();
						msg.what=MSG_UPDATE_PROGRESS;
						msg.arg1=i;
						msg.obj=arg1;
						mainHandler.sendMessageDelayed(msg, Constants.Delayed.TIME1);
						return;
					}
				}
			}else if(arg1.getAction().equals(Constants.INTENT_DOWNLOAD_STARTED)){
				if(arg1.getStringExtra("app_id")==null||arg1.getStringExtra("app_id").trim().equals("")||arg1.getStringExtra("app_id").trim().equals("null")){
					return;
				}
				int app_id=Integer.valueOf(arg1.getStringExtra("app_id"));
				for(int i=0;i<mList.size();i++){
					if(mList.get(i).getID()==app_id){
						View viewItem=mGridLayout.getChildAt(i);
						if(viewItem.getVisibility()==View.VISIBLE){
							ProgressBar progress=(ProgressBar)viewItem.findViewById(R.id.down_progress);
			                if(progress.getVisibility()!=View.VISIBLE){
			                	RatingBar bar = (RatingBar)viewItem.findViewById(R.id.class_rating_bar);
			                	bar.setVisibility(View.GONE);
			                	progress.setVisibility(View.VISIBLE);
			            	}
			                TextView priceView=(TextView) mGridLayout.getChildAt(i).findViewById(R.id.item_price);
		                	priceView.setText(""+ClassActivity.this.getString(R.string.download_tab_2));
		                	priceView.setVisibility(View.VISIBLE);
			                TaskBean task=tDBUtils.queryTaskByPkgName(mList.get(i).getPkgName());
			                if(task.getSumSize()>0){
			                	long downSize=task.getDownloadSize();
			                	long sumSize=task.getSumSize();
			                	int res=(int)(downSize*100/sumSize);
			                	progress.setProgress(res);
			                }
						}
					}
				}
			}else if(arg1.getAction().equals(Constants.INTENT_INSTALL_COMPLETED)){
				if(arg1.getStringExtra("app_id")==null||arg1.getStringExtra("app_id").trim().equals("")||arg1.getStringExtra("app_id").trim().equals("null")){
					return;
				}
				if(arg1.getStringExtra("pkg_name")==null||arg1.getStringExtra("pkg_name").trim().equals("")){
					return;
				}
				int status=tDBUtils.queryStatusByPkgName(arg1.getStringExtra("pkg_name"));
				AppLog.d(TAG, "------------Constants.INTENT_INSTALL_COMPLETED-------------status="+status);
				if(status!=Constants.APP_STATUS_INSTALLED){
					return;
				}
				String pkg_name=arg1.getStringExtra("pkg_name");
				AppLog.d(TAG, "---------install---&&&&&&&&&&&&&&&&&&&7---pkg_name="+pkg_name);
				for(int i=0;i<mList.size();i++){
					if(mList.get(i).getPkgName().equals(pkg_name)){
						Message msg=mainHandler.obtainMessage();
						msg.what=MSG_SHOW_INSTALL;
						msg.arg1=i;
						mainHandler.sendMessage(msg);
					}
				}
			}else if(arg1.getAction().equals(Constants.INTENT_INSTALL_FAIL)){
				if(arg1.getStringExtra("pkg_name")==null||arg1.getStringExtra("pkg_name").trim().equals("")){
					return;
				}
				String pkg_name=arg1.getStringExtra("pkg_name");
				AppLog.d(TAG, "---------uninstall------pkg_name="+pkg_name);
				for(int i=0;i<mList.size();i++){
					if(mList.get(i).getPkgName().equals(pkg_name)){
						Message msg=mainHandler.obtainMessage();
						msg.what=MSG_SHOW_UNINSTALL;
						msg.arg1=i;
						mainHandler.sendMessage(msg);
					}
				}
			}else if(arg1.getAction().equals(Constants.INTENT_UNINSTALL_COMPLETED)){
				if(arg1.getStringExtra("pkg_name")==null||arg1.getStringExtra("pkg_name").trim().equals("")){
					return;
				}
				int status=tDBUtils.queryStatusByPkgName(arg1.getStringExtra("pkg_name"));
				if(status!=Constants.APP_STATUS_UNDOWNLOAD){
					return;
				}
				String pkg_name=arg1.getStringExtra("pkg_name");
				for(int i=0;i<mList.size();i++){
					if(mList.get(i).getPkgName().equals(pkg_name)){
						Message msg=mainHandler.obtainMessage();
						msg.what=MSG_SHOW_UNINSTALL;
						msg.arg1=i;
						mainHandler.sendMessage(msg);
					}
				}
			}else if(arg1.getAction().equals(Constants.INTENT_DOWNLOAD_DETLET)){
				if(arg1.getStringExtra("pkg_name")==null||arg1.getStringExtra("pkg_name").trim().equals("")){
					return;
				}
				int status=tDBUtils.queryStatusByPkgName(arg1.getStringExtra("pkg_name"));
				if(status!=Constants.APP_STATUS_UNDOWNLOAD){
					return;
				}
				AppLog.d(TAG, "---------uninstall------pkg_name="+arg1.getStringExtra("pkg_name"));
				for(int i=0;i<mList.size();i++){
					if(mList.get(i).getPkgName().equals(arg1.getStringExtra("pkg_name"))){
						Message msg=mainHandler.obtainMessage();
						msg.what=MSG_SHOW_UNINSTALL;
						msg.arg1=i;
						mainHandler.sendMessage(msg);
					}
				}
			}else if(arg1.getAction().equals(Constants.INTENT_DOWNLOAD_ERROR)){
				if(arg1.getStringExtra("app_id")==null||arg1.getStringExtra("app_id").trim().equals("")||arg1.getStringExtra("app_id").trim().equals("null")){
					return;
				}
				if(arg1.getStringExtra("pkg_name")==null||arg1.getStringExtra("pkg_name").trim().equals("")){
					return;
				}
				for(int i=0;i<mList.size();i++){
					if(mList.get(i).getID()==Integer.valueOf(arg1.getStringExtra("app_id"))){
						Message msg=mainHandler.obtainMessage();
						msg.what=MSG_SHOW_DOWN_ERROR;
						msg.arg1=i;
						mainHandler.sendMessage(msg);
					}
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
				int app_id=Integer.valueOf(arg1.getStringExtra("app_id"));
				String pkg_name=arg1.getStringExtra("pkg_name");
				AppLog.d(TAG, "---------uninstall------app_id="+app_id);
				for(int i=0;i<mList.size();i++){
//					if(mList.get(i).getID()==app_id){
					if(mList.get(i).getPkgName().equals(pkg_name)){
						String versionNet=tDBUtils.queryVersionByPkgName(pkg_name);
						if(!Utils.CompareVersion(mList.get(i).getVersion(), versionNet)){
							Message msg=mainHandler.obtainMessage();
							msg.what=MSG_HIDE_UPDATE;
							msg.arg1=i;
							mainHandler.sendMessage(msg);
						}
					}
				}
			}else if(arg1.getAction().equals(Constants.INTENT_DOWNLOAD_PAUSE)){
				Log.d(TAG, "-----Constants.INTENT_DOWNLOAD_PAUSE--");
				if(arg1.getStringExtra("pkg_name")==null||arg1.getStringExtra("pkg_name").trim().equals("")){
					return;
				}
				String pkg_name=arg1.getStringExtra("pkg_name");
				AppLog.d(TAG, "pkg_name="+pkg_name);
				for(int i=0;i<mList.size();i++){
					if(mList.get(i).getPkgName().equals(pkg_name)){
						TextView priceView=(TextView) mGridLayout.getChildAt(i).findViewById(R.id.item_price);
	                	priceView.setText(""+ClassActivity.this.getString(R.string.pause_download));
					}
				}
			}
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if(hasFocus){
			control.addFocusView(v);
		}
	}

}
