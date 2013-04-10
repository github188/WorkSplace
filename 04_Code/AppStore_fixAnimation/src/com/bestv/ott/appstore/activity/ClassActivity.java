package com.bestv.ott.appstore.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bestv.ott.appstore.R;
import com.bestv.ott.appstore.animation.AnimUtils;
import com.bestv.ott.appstore.animation.MoveControl;
import com.bestv.ott.appstore.common.AppsBean;
import com.bestv.ott.appstore.common.Constants;
import com.bestv.ott.appstore.common.PageBean;
import com.bestv.ott.appstore.common.TaskBean;
import com.bestv.ott.appstore.db.DBUtils;
import com.bestv.ott.appstore.parser.MenusParser;
import com.bestv.ott.appstore.thread.LoadAppThread;
import com.bestv.ott.appstore.utils.AppLog;
import com.bestv.ott.appstore.utils.CaCheManager;
import com.bestv.ott.appstore.utils.NetUtil;
import com.bestv.ott.appstore.utils.RequestParam;
import com.bestv.ott.appstore.utils.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
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
    
    public Handler workHandler;
    private LoadAppThread mThread;
    private DownReceiver downReceiver;
    private TextView showTitle;
    
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
        downReceiver=new DownReceiver();
        this.registerReceiver(downReceiver, filter);
    }
    
    public void setupViews(){
    	showTitle=(TextView)this.findViewById(R.id.top2_left);
    	pageView=(TextView)this.findViewById(R.id.page);
    	mImageView = (ImageView) this.findViewById(R.id.animation_imageview);
    	control=new MoveControl(this,mImageView);
        mDownRecord=(TextView)this.findViewById(R.id.top_down_record);
        mSearch = (TextView)this.findViewById(R.id.top_search);
        mRootLayout = (RelativeLayout)findViewById(R.id.rootLayout);
        mViewFlipper = (ViewFlipper) this.findViewById(R.id.app_class_viewflipper);
        mMenuLinearLayout = (LinearLayout) this.findViewById(R.id.class_menu_linearlayout);
        mDownRecord.setOnClickListener(this);
        mDownRecord.setOnFocusChangeListener(this);
        mDownRecord.setTag("mDownloadRecord");
		mSearch.setTag("mSearch");
        TextPaint tp = showTitle.getPaint(); 
		tp.setFakeBoldText(true); 
        mSearch.setOnClickListener(this);
        mSearch.setOnFocusChangeListener(this);
        mLayoutInflater = this.getLayoutInflater();
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
            	int index=(Integer)msg.obj;
            	View viewItem=mGridLayout.getChildAt(index);
            	int file_sum=msg.arg2;
            	int down_size=msg.arg1;
            	AppLog.d(TAG, "----file_sum="+file_sum+";down_size="+down_size);
            	if(viewItem.getVisibility()!=View.VISIBLE){
            		return;
            	}
            	ProgressBar progress=(ProgressBar)viewItem.findViewById(R.id.down_progress);
                if(progress.getVisibility()!=View.VISIBLE){
                	RatingBar bar = (RatingBar)viewItem.findViewById(R.id.class_rating_bar);
                	bar.setVisibility(View.GONE);
                	progress.setVisibility(View.VISIBLE);
                	TextView priceView=(TextView) mGridLayout.getChildAt(index).findViewById(R.id.item_price);
                	priceView.setText(""+ClassActivity.this.getString(R.string.download_tab_2));
                	priceView.setVisibility(View.VISIBLE);
            	}
                if(file_sum>=0){
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
            		TextView priceView=(TextView)viewError.findViewById(R.id.item_price);
                	priceView.setText(""+ClassActivity.this.getString(R.string.download_error));
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
    		return;
    	}
    	mLoadNum++;
    	if(mLoadNum>=2){
    		stopProgressDialog();
    	}
    	getMenuData(menuList);
    	initMenuLayout();
    	mainHandler.sendEmptyMessageDelayed(MSG_CHANGE_CLASS_STYE, 100);
	}
    
    View viewFirst;
    View viewLast;
    public void initMenuLayout(){
    	mMenuLinearLayout.removeAllViews();
//    	mImageView.setBackgroundColor(Color.RED);
    	AppLog.d(TAG, "-------------"+menuAdpaterList.size());
    	for(int i=0;i<menuAdpaterList.size();i++){
    		View view = mLayoutInflater.inflate(R.layout.menu_item_layout, null);
    		view.setOnClickListener(this);
    		view.setOnFocusChangeListener(this);
    		ImageView button = (ImageView) view.findViewById(R.id.menu_item_img);
    		TextView nameText=(TextView) view.findViewById(R.id.menu_item_text);
    		Map<String, Object> map = menuAdpaterList.get(i);
    		view.setTag((Integer)map.get("id"));
    		if(i<3){
    			button.setImageResource((Integer)map.get("image"));
    		}else{
    			if(map.get("image")==null||map.get("image").equals("")){
    				button.setImageResource(R.drawable.menu_default_img);
    			}else{
    				Bitmap bm = CaCheManager.requestBitmap((String) map.get("image"));
    				if(bm!=null && !bm.isRecycled()){
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
    		if(i==menuAdpaterList.size()-1){
    			viewLast = view;
    			viewLast.setId(Constants.MENU_RIGHT_ID);
    		}
    		String name = (String) map.get("name");
//    		AppLog.log_D(TAG, "-------------"+name);
    		nameText.setText(name);
            button.setOnClickListener(this);
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

    }
        
   /* TextButton tFirstBut=null;
	TextButton tLastBut=null;
    public void initMenu(){
    	mMenuLinearLayout.removeAllViews();
    	AppLog.d(TAG, "-----  --------"+menuAdpaterList.size());
    	for(int i=0;i<menuAdpaterList.size();i++){
    		View view = mLayoutInflater.inflate(R.layout.class_menu_item_layout, null);
    		TextButton button = (TextButton) view.findViewById(R.id.menu_item_img);
    		button.setBackImageView(mImageView);
    		Map<String, Object> map = menuAdpaterList.get(i);
    		String name = (String) map.get("name");
    		AppLog.d(TAG, "-------------"+name);
            button.setText(name);
            button.setTag((Integer)map.get("id"));
            AppLog.d(TAG, "-----------------------id="+(Integer)button.getTag());
            button.setOnClickListener(this);
        	mMenuLinearLayout.addView(view);
        	if(i==0){
        		viewFirst = view;
        		tFirstBut=button;
        		tFirstBut.setId(Constants.MENU_LEFT_ID);
        	}
        	if(i==menuAdpaterList.size()-1){
        		viewLast = view;
        		tLastBut=button;
        		tLastBut.setId(Constants.MENU_RIGHT_ID);
        	}
    	}
    	viewFirst.setNextFocusLeftId(tLastBut.getId());
    	viewLast.setNextFocusRightId(tFirstBut.getId());
    	
    	int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        mMenuLinearLayout.measure(w, h);
        int width = mMenuLinearLayout.getMeasuredWidth();
        LayoutParams lp1 = mMenuLinearLayout.getLayoutParams();
        lp1.width = width;
//    	tFirstBut.setOnKeyListener(new View.OnKeyListener(){
//			@Override
//			public boolean onKey(View v, int keyCode, KeyEvent event) {
//				if(keyCode==KeyEvent.KEYCODE_DPAD_LEFT){
//					tLastBut.requestFocus();
//					return true;
//				}
//				return false;
//			}
//    	});
//    	tLastBut.setOnKeyListener(new View.OnKeyListener(){
//			@Override
//			public boolean onKey(View v, int keyCode, KeyEvent event) {
//				if(keyCode==KeyEvent.KEYCODE_DPAD_RIGHT){
//					tFirstBut.requestFocus();
//					return true;
//				}
//				return false;
//			}
//    	});
        
        if(mLoadNum>=2){
        	AppLog.d(TAG, "------------initMenu-------------menuLinearCount="+mMenuLinearLayout.getChildCount());
			for(int i=0;i<mMenuLinearLayout.getChildCount();i++){
				View viewM=mMenuLinearLayout.getChildAt(i);
				TextButton tb=null;
				if(i==0){
					tb=(TextButton)viewM.findViewById(Constants.MENU_LEFT_ID);
				}else if(i==mMenuLinearLayout.getChildCount()-1){
					tb=(TextButton)viewM.findViewById(Constants.MENU_RIGHT_ID);
				}else{
					tb=(TextButton)viewM.findViewById(R.id.menu_item_img);
				}
				if(tb!=null&&((Integer)tb.getTag())==typeID){
					AppLog.d(TAG, "--------------------------");
					tb.requestFocus();
					tb.setBackImageView(mImageView);
					control.transformAnimation(mImageView, tb, this, false, true);
					control.showFource();
				}
			}
		}
    }*/
    
    public void initViewFillper(int listSize) {
//    	if(mViewFlipper!=null){
//    		CenterAppLayout lay=(CenterAppLayout)mViewFlipper.getChildAt(0);
//    		if(lay!=null){
//    			for(int i=0;i<lay.getChildCount();i++){
//    				AnimationButton button = (AnimationButton) lay.getChildAt(i).findViewById(R.id.class_item_img);
//    				BitmapDrawable drawable =(BitmapDrawable)button.getImageView().getDrawable();
//    				Bitmap bmp = drawable.getBitmap();
//    				if (null != bmp && !bmp.isRecycled()){
//    					bmp.recycle();
//    				    bmp = null;
//    				}
//    			}
//    		}
//    	}
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
    	setMenuStyle(typeID);
    	initViewFillper(appList.size());
    	mList=appList;
    	if(typeID==RequestParam.CalssID.NEW_ID||typeID==RequestParam.CalssID.HOT_ID){
    		if(mPageBean.getPageTotal()>2){
    			pageView.setText(ClassActivity.this.getString(R.string.page, appList.size()>0?mPageBean.getPageNo():0,2));
    		}else{
    			pageView.setText(ClassActivity.this.getString(R.string.page, appList.size()>0?mPageBean.getPageNo():0,mPageBean.getPageTotal()));
    		}
    	}else{
    		pageView.setText(ClassActivity.this.getString(R.string.page, appList.size()>0?mPageBean.getPageNo():0,mPageBean.getPageTotal()));
    	}
//		if(appList.size()==0){
//			AppLog.log_D(TAG, "-----------mList is null");
//			for(int i=0;i<8;i++){
//				mGridLayout.getChildAt(i).setVisibility(View.GONE);
//			}
//			return;
//		}
		AppLog.d(TAG, "-----------appList size="+appList.size());
		for(int i=0;i<appList.size();i++){
			View view=mGridLayout.getChildAt(i);
			
			AppsBean bean = appList.get(i);
			//设图外设/精品图标
			setPropertyIcon(view , bean);
			
			ImageView ab=(ImageView)view.findViewById(R.id.class_item_img);
			ab.setTag(bean);
			if(null==bean.getNatImageUrl()||bean.getNatImageUrl().equals("")){
				ab.setImageResource(R.drawable.app_default_img);
			}else{
//				ab.setImageURI(Uri.parse(bean.getNatImageUrl()));
				Bitmap bm = CaCheManager.requestBitmap(bean.getNatImageUrl());
				if(bm!=null){
				    AppLog.d(TAG, "-------------bm!=null-------name = "+bean.getAppName());
				    ab.setImageBitmap(bm);
				}else{
				    AppLog.d(TAG, "-------------bm==null-------name = "+bean.getAppName());
				    ab.setImageResource(R.drawable.app_default_img);
				}
			}
			((TextView)view.findViewById(R.id.item_name)).setText(bean.getAppName());
			ImageView popIv = (ImageView)view.findViewById(R.id.class_item_pop);
			ImageView popIvT = (ImageView)view.findViewById(R.id.pop_update);
			TextView priceView=(TextView) view.findViewById(R.id.item_price);
			ProgressBar progress=(ProgressBar)view.findViewById(R.id.down_progress);
			RatingBar bar = (RatingBar)view.findViewById(R.id.class_rating_bar);
			bar.setFocusable(false);
			String soc=bean.getScore()==null?"0":bean.getScore().trim();
			float res=Integer.valueOf(soc.equals("")?"0":soc);
			res = res / 2;
			bar.setRating((float)res);
			double score=(bean.getPrice());
			int appStatus = tDBUtils.queryStatusByPkgName(bean.getPkgName());
			AppLog.d(TAG, "---------------res="+res+";(float)res="+(float)res+";pkgname="+bean.getPkgName()+";appStatus="+appStatus+";name="+bean.getAppName());
			switch(appStatus){
			case Constants.APP_STATUS_DOWNLOADING:
			case Constants.DOWNLOAD_STATUS_ERROR:
			case Constants.DOWNLOAD_STATUS_PAUSE:
            case Constants.DOWNLOAD_STATUS_EXIT_STOP:
            	int appSts=tDBUtils.queryStatusByPkgNameAndVersion(bean.getPkgName(),bean.getVersion());
            	AppLog.d(TAG, "-----------------------appSts="+appSts);
            	switch(appSts){
	            	case Constants.APP_STATUS_DOWNLOADING:
	    			case Constants.DOWNLOAD_STATUS_ERROR:
	    			case Constants.DOWNLOAD_STATUS_PAUSE:
	                case Constants.DOWNLOAD_STATUS_EXIT_STOP:
	                	if(appStatus==Constants.DOWNLOAD_STATUS_ERROR){
	                		priceView.setText(""+ClassActivity.this.getString(R.string.download_error));
	                	}else{
	                		priceView.setText(""+ClassActivity.this.getString(R.string.download_tab_2));
	                	}
	                	progress.setVisibility(View.VISIBLE);
	                	int sumSize=tDBUtils.querySumSizeByPkgName(bean.getPkgName());
	                	int downSize=tDBUtils.queryDowningSizeByPkgName(bean.getPkgName());
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
	                	int appS=tDBUtils.queryStatusByPkgNameFormApplication(bean.getPkgName());
	                	if(appS==Constants.APP_STATUS_UPDATE||appS==Constants.APP_STATUS_INSTALLED){
	                		priceView.setText(""+ClassActivity.this.getString(R.string.download_tab_3));
	                		//对比已安装的应用的新旧版本
		                	String newVersion = bean.getVersion();
		                	String oldVersion = tDBUtils.queryVersionByPkgName(bean.getPkgName());
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
            	String newVersion = bean.getVersion();
            	String oldVersion = tDBUtils.queryVersionByPkgName(bean.getPkgName());
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
			parent.setDescendantFocusability(viewGroupFocus);
			if(refreshFocusView!=null){
				refreshFocusView.setFocusable(true);
				refreshFocusView.setAlpha(AnimUtils.VIEW_ALPHA_FLAG);
				refreshFocusView.setFocusableInTouchMode(true);
				refreshFocusView.requestFocus();
			}else{
//				requestMenu(typeID);
			}
		}
		parent.setDescendantFocusability(viewGroupFocus);
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
    	for(int i=0;i<menuAdpaterList.size();i++){
    		Map<String, Object> map=menuAdpaterList.get(i);
    		if(position==(Integer)map.get("id")){
    			AppLog.d(TAG, "**********"+map.get("name"));
    			showTitle.setText(""+map.get("name"));
    			
    		}
    	}
    	/*TextButton clickBut=null;
        for(int i=0;i<mMenuLinearLayout.getChildCount();i++){
            View view=mMenuLinearLayout.getChildAt(i);
            TextButton tb;
            if(i==0){
            	tb=(TextButton)view.findViewById(Constants.MENU_LEFT_ID);
            }else if (i == mMenuLinearLayout.getChildCount() -1){
            	tb=(TextButton)view.findViewById(Constants.MENU_RIGHT_ID);
            }else{
            	tb=(TextButton)view.findViewById(R.id.menu_item_img);
            }
            tb.setButtonBackImage(-1); 
            tb.setTextColor(getResources().getColor(R.color.black));
            if((Integer)tb.getTag()==position){
            	clickBut=tb;
            }
        }
        if(clickBut!=null){
        	clickBut.setButtonBackImage(R.drawable.button_selected);
        	clickBut.setTextColor(getResources().getColor(R.color.white));
        }*/
    }
    /**
     * 下一页
     */
    public void nextPage(){
		/*ViewGroup lastGroup = (ViewGroup) mViewFlipper.getCurrentView();
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
			Log.i("MainActivity", "nextFlipperPage lastFocus is " + lastFocus);
		}*/
    	AppLog.d(TAG, "----------nextPage-----------");
    	if(mThread.isAlive()){
    		return;
    	}
        if(!mPageBean.nextPage()){
        	if(mPageBean.getPageTotal()<2){
        		return;
        	}
        	mPageBean.setPageNo(1);
        }
        if(typeID==RequestParam.CalssID.HOT_ID||typeID==RequestParam.CalssID.NEW_ID){
    		if(mPageBean.getPageNo()>2){
    			mPageBean.setPageNo(1);
    		}
    	}
        turnRight=1;
        mThread=new LoadAppThread(ClassActivity.this,mPageBean,mActionType);
        mThread.start();
        startProgressDialog();
       /* if(GridLayout.class.isInstance(mViewFlipper.getCurrentView())){
			mGridLayout = (CenterAppLayout) mViewFlipper.getCurrentView();
			if(mGridLayout!=null){
				AnimationButton button = (AnimationButton) mGridLayout.getChildAt(0).findViewById(R.id.home_item_img);
				if(button != null){
					control.transformAnimation(mImageView, lastFocus != null ? lastFocus:mImageView, button, this, false, true);
					button.requestFocus();
					button.startAnimation();
				}
			}
		}*/
    }
    
    /**
     * 上一页
     */
    public void previousPage(){
		/*ViewGroup lastGroup = (ViewGroup) mViewFlipper.getCurrentView();
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
			Log.i("MainActivity", "nextFlipperPage lastFocus is " + lastFocus);
		}*/
    	if(mThread.isAlive()){
    		return;
    	}
        if(!mPageBean.previousPage()){
        	if(mPageBean.getPageTotal()<2){
        		return;
        	}
        	mPageBean.setPageNo(mPageBean.getPageTotal());
        }
        if(typeID==RequestParam.CalssID.HOT_ID||typeID==RequestParam.CalssID.NEW_ID){
    		if(mPageBean.getPageNo()>1){
    			mPageBean.setPageNo(2);
    		}
    	}
        turnRight=2;
        mThread=new LoadAppThread(ClassActivity.this,mPageBean,mActionType);
        mThread.start();
        startProgressDialog();
        
        /*if(GridLayout.class.isInstance(mViewFlipper.getCurrentView())){
			mGridLayout = (CenterAppLayout) mViewFlipper.getCurrentView();
			if(mGridLayout!=null){
				AnimationButton button = (AnimationButton) mGridLayout.getChildAt(3).findViewById(R.id.home_item_img);
				if(button != null){
					control.transformAnimation(mImageView, lastFocus != null ? lastFocus:mImageView, button, this, false, true);
					button.requestFocus();
					button.startAnimation();
				}else{
					AnimationButton buttonFirst = (AnimationButton) mGridLayout.getChildAt(0).findViewById(R.id.home_item_img);
					if(buttonFirst != null){
						control.transformAnimation(mImageView, lastFocus != null ? lastFocus:mImageView, buttonFirst, this, false, true);
						buttonFirst.requestFocus();
						buttonFirst.startAnimation();
					}
				}
			}
		}*/
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
        		AppLog.d(TAG, "-------control.mDelay="+control.mDelay);
        		if(control.mDelay){//表示正在翻页中,由于翻页有延迟
        			return true;
        		}
        		View nextview=mFocusF.findNextFocus(parent, this.getCurrentFocus(),mMoveDirect);
        		if(mViewFlipper!=null&&mViewFlipper.getChildCount()>0){
        			if(nextview==null){
            			if(mMoveDirect==View.FOCUS_LEFT&&AppsBean.class.isInstance(this.getCurrentFocus().getTag())&&control.focusQueue.size()==0){
            				this.previousPage();
            			}
            			isRunning=false;
            			return true;
            		}else{
            			AppLog.d(TAG, "nextview.getTag="+nextview.getTag());
            			if(nextview.getTag()!=null&&this.getCurrentFocus()!=null&&AppsBean.class.isInstance(this.getCurrentFocus().getTag())&&String.class.isInstance(nextview.getTag())){//当前为推荐位页面的翻页
            				AppLog.d(TAG, "mMoveDirect="+mMoveDirect+";event.getKeyCode()="+event.getKeyCode());
            				if(mMoveDirect==View.FOCUS_RIGHT&&(((String)nextview.getTag()).equals("mDownloadRecord")||((String)nextview.getTag()).equals("mSearch"))){
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
        map.put("id", (Integer)RequestParam.CalssID.ALL_ID);
        map.put("image",R.drawable.iconup);
        menuAdpaterList.add(map);
        map= new HashMap<String, Object>();
        map.put("name", getString(R.string.new_app));
        map.put("image",R.drawable.ico_new);
        map.put("id", (Integer)RequestParam.CalssID.NEW_ID);
        menuAdpaterList.add(map);
        map= new HashMap<String, Object>();
        map.put("name", getString(R.string.hot_app));
        map.put("image",R.drawable.ico_hot);
        map.put("id", (Integer)RequestParam.CalssID.HOT_ID);
        menuAdpaterList.add(map);
        for (AppsBean tAppsBean : menuList) {
			map = new HashMap<String, Object>();
			map.put("name", tAppsBean.getAppName());
			map.put("id", (Integer)tAppsBean.getID());
			map.put("image", tAppsBean.getNatImageUrl());
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
        mThread=new LoadAppThread(ClassActivity.this,mPageBean,mActionType);
        mThread.start();
        startProgressDialog();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        AppLog.d(TAG," onResume ");
    }
    
    @Override
    protected void onStop(){
        super.onStop();
        if (mLoadMenuTask != null && mLoadMenuTask.getStatus() != LoadMenuTask.Status.FINISHED) {
        	mLoadMenuTask.cancel(true);
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

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.top_down_record:
			downloadMgr();
			break;
		case R.id.class_item_img:
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
			if(null==mList||mList.size()<1){
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
						int file_sum=arg1.getIntExtra("file_sum", -1);
		            	int down_size=arg1.getIntExtra("download_size", -1);
		            	msg.arg1=down_size;
		            	msg.arg2=file_sum;
		            	msg.obj=i;
						mainHandler.sendMessage(msg);
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
			                	TextView priceView=(TextView) mGridLayout.getChildAt(i).findViewById(R.id.item_price);
			                	priceView.setText(""+ClassActivity.this.getString(R.string.download_tab_2));
			                	priceView.setVisibility(View.VISIBLE);
			            	}
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
				int status=tDBUtils.queryStatusByPkgNameFormApplication(arg1.getStringExtra("pkg_name"));
				AppLog.d(TAG, "------------Constants.INTENT_INSTALL_COMPLETED-------------status="+status);
				if(status!=Constants.APP_STATUS_INSTALLED){
					return;
				}
				String pkg_name=arg1.getStringExtra("pkg_name");
				String version=arg1.getStringExtra("version_code");
				AppLog.d(TAG, "---------install---&&&&&&&&&&&&&&&&&&&7---pkg_name="+pkg_name);
				for(int i=0;i<mList.size();i++){
					if(mList.get(i).getPkgName().equals(pkg_name)){
						Message msg=mainHandler.obtainMessage();
						msg.what=MSG_SHOW_INSTALL;
						msg.arg1=i;
						msg.obj=arg1;
						mainHandler.sendMessage(msg);
                        if(version!=null&&Utils.CompareVersion(mList.get(i).getVersion(),version)){
                        	AppLog.d(TAG, "mList.get(i).getVersion()"+mList.get(i).getVersion()+";version="+version);
                        	View view=mGridLayout.getChildAt(i);
                        	ImageView popIv = (ImageView)view.findViewById(R.id.class_item_pop);
                			ImageView popIvT = (ImageView)view.findViewById(R.id.pop_update);
                			popIv.setVisibility(View.VISIBLE);
                			popIvT.setVisibility(View.VISIBLE);
						}
					}
				}
			}else if(arg1.getAction().equals(Constants.INTENT_INSTALL_FAIL)){
				if(arg1.getStringExtra("app_id")==null||arg1.getStringExtra("app_id").trim().equals("")||arg1.getStringExtra("app_id").trim().equals("null")){
					return;
				}
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
				if(arg1.getStringExtra("app_id")==null||arg1.getStringExtra("app_id").trim().equals("")||arg1.getStringExtra("app_id").trim().equals("null")){
					return;
				}
				if(arg1.getStringExtra("pkg_name")==null||arg1.getStringExtra("pkg_name").trim().equals("")){
					return;
				}
				int status=tDBUtils.queryStatusByPkgName(arg1.getStringExtra("pkg_name"));
				if(status!=Constants.APP_STATUS_UNDOWNLOAD){
					return;
				}
				int app_id=Integer.valueOf(arg1.getStringExtra("app_id"));
				String pkg_name=arg1.getStringExtra("pkg_name");
				AppLog.d(TAG, "---------uninstall------app_id="+app_id);
				for(int i=0;i<mList.size();i++){
					if(mList.get(i).getPkgName().equals(pkg_name)){
						Message msg=mainHandler.obtainMessage();
						msg.what=MSG_SHOW_UNINSTALL;
						msg.arg1=i;
						mainHandler.sendMessage(msg);
					}
				}
			}else if(arg1.getAction().equals(Constants.INTENT_DOWNLOAD_DETLET)){
				if(arg1.getStringExtra("app_id")==null||arg1.getStringExtra("app_id").trim().equals("")||arg1.getStringExtra("app_id").trim().equals("null")){
					return;
				}
				if(arg1.getStringExtra("pkg_name")==null||arg1.getStringExtra("pkg_name").trim().equals("")){
					return;
				}
				int status=tDBUtils.queryStatusByPkgName(arg1.getStringExtra("pkg_name"));
				if(status!=Constants.APP_STATUS_UNDOWNLOAD){
					return;
				}
				int app_id=Integer.valueOf(arg1.getStringExtra("app_id"));
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
