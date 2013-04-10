package com.bestv.ott.appstore.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bestv.ott.appstore.activity.DetailedActivity.AppReceiver;
import com.bestv.ott.appstore.animation.AnimationButton;
import com.bestv.ott.appstore.animation.AnimationControl;
import com.bestv.ott.appstore.animation.AnimationImageButton;
import com.bestv.ott.appstore.animation.AnimationProgress;
import com.bestv.ott.appstore.animation.AnimationTextButton;
import com.bestv.ott.appstore.animation.DetailedActivityAppLayout;
import com.bestv.ott.appstore.animation.DownloadTextView;
import com.bestv.ott.appstore.animation.EditTextButton;
import com.bestv.ott.appstore.animation.SearchResultLayout;
import com.bestv.ott.appstore.common.AppsBean;
import com.bestv.ott.appstore.common.Constants;
import com.bestv.ott.appstore.common.DataOperate;
import com.bestv.ott.appstore.common.ImageDownloadTask;
import com.bestv.ott.appstore.common.PageBean;
import com.bestv.ott.appstore.db.DBUtils;
import com.bestv.ott.appstore.utils.AppLog;
import com.bestv.ott.appstore.utils.RequestParam;
import com.bestv.ott.appstore.utils.Utils;
import com.bestv.ott.appstore.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils.TruncateAt;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class AppSearchActivity extends BaseActivity implements OnFocusChangeListener,OnClickListener{
	private static final String TAG = "com.joysee.appstore.AppSearchActivity";
	
	private FocusFinder mFocusF = FocusFinder.getInstance();
	private AnimationControl control = AnimationControl.getInstance();
	
	public static final int MSG_SHOW_APP=0;//显示应用数据
    public static final int MSG_GET_APP=1;//获取应用数据
    public static final int MSG_FINISH_ONE_APP=2;//下载完成一张应用图片
    public static final int MSG_SHOW_LIST=3;//获取分类数据
    public static final int MSG_GET_CLASS=4;//获取分类数据
    public static final int MSG_FINISH_ONE_CLASS=5;//下载完成一张分类图片
    public static final int MSG_NET_NOT_CONNECT=6;//网络连接不通
    public static final int MSG_DETAILED=7;//进入详情页
    public static final int MSG_FOCUS=8;
    public static final int MSG_NO_KEYWORD = 11;

	private static final int BENZ = 9;
	private Boolean isTag = true;
	private static int loadNum = 0;//下载够三张图片里,才setData

    
    private DownloadTextView mDownRecord;
    
    
	private Context mContext;
	private PageBean mPageBean=new PageBean();
	private List <Map<String, Object>> mNewAdpaterList=new ArrayList<Map<String, Object>>();
	private DataOperate mDataOperate;
	// view
	private EditTextButton mEditText;
	private AnimationImageButton mButtonSearch;
	private AnimationImageButton mButtonVoice;
	private DownloadTextView mDownloadTextView;
	private ViewFlipper mViewFlipper;
	private LayoutInflater mLayoutInflater;
	private DetailedActivityAppLayout mLayout;
	private ImageView mImageView;
	private ImageView mUpImageView;
	private ImageView mDownImageView;
	private ListView mListView;
	private int turnDown=0;//0表示第一次进，另边不要上焦点，1表示向下翻页，2表示向上翻页
	private ArrayList<AnimationButton> mArrayAnimationButton = new ArrayList<AnimationButton>();
	private DBUtils tDBUtils ;
	
	private SimpleAdapter mListViewAdapter;
	
	private List<AppsBean> mAppList=null;//最新应用信息
	private List<AppsBean> mSearchList=null;//搜索信息
	public HandlerThread mWorkThread; 
	public Handler mWorkHandler;
	private AppReceiver mAppReceiver = null;
	
	private SearchResultLayout mAppSearchResultEdittextLayout;
	private RelativeLayout mRootLayout;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 设为全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  
                	WindowManager.LayoutParams.FLAG_FULLSCREEN);
        AppLog.d(TAG," onCreate ");
		mContext = this;
		tDBUtils=new DBUtils(AppSearchActivity.this);
        setContentView(R.layout.app_search_result_layout);
        initView();
        
        threadHandler();//启动线程
        initListView();
        String keyWord=this.getIntent().getStringExtra("keyWord");
	     AppLog.d(TAG, "-------------keyWord="+keyWord);
	     if(!mWorkThread.isAlive()){
	        	mWorkThread.start();
	        }
	     if(keyWord!=null&&!"".equals(keyWord.trim())){
	    	 mEditText.setText(keyWord);
	    	 mWorkHandler.sendEmptyMessage(MSG_SHOW_APP);
	     }
	     IntentFilter filter=new IntentFilter();
	     filter.addAction(Constants.INTENT_UPDATE_COMPLETED);
	     mAppReceiver=new AppReceiver();
	     mWorkHandler.sendEmptyMessage(MSG_GET_APP);
	     
	}
	
	public void threadHandler(){
		  mWorkThread=new HandlerThread("handler_thread");
          mWorkThread.start();
          mWorkHandler=new Handler(mWorkThread.getLooper()){
             public void handleMessage(Message msg) {
                 switch(msg.what){
                   case MSG_SHOW_APP:
                	   AppLog.d(TAG, "search activity msg show app thread");
                	   String str = mEditText.getText().toString();
                	   if(null==str||str.trim().equals("")){
                		   if(null!=mListView && null!=mSearchList){
                			   mSearchList.clear();
                			   setAdapterData(mSearchList);
                			   mSearchMainHandler.sendEmptyMessage(MSG_SHOW_LIST);
                		   }else{
                			   mSearchMainHandler.sendEmptyMessage(MSG_NO_KEYWORD);
                		   }
                		   return;
                	   }
                	    setData(str);
                	    if(null==mSearchList){
                	    	mSearchMainHandler.sendEmptyMessage(MSG_NET_NOT_CONNECT);
                            return;
                        }
						setAdapterData(mSearchList);
						//刷新 搜索结果
						mSearchMainHandler.sendEmptyMessage(MSG_SHOW_LIST);
                	   break;
                   case MSG_GET_APP:
                	   mAppList=mDataOperate.getDataFormService(RequestParam.ActionIndex.GETRECOMMENDLIST);//获取推荐应用
                	   if(null==mAppList){
                	    	mSearchMainHandler.sendEmptyMessage(MSG_NET_NOT_CONNECT);
                            return;
                       }
                	   new ImageDownloadTask().execute(mAppList,mSearchMainHandler,MSG_FINISH_ONE_APP);
                       break;
                   case MSG_DETAILED:
                       AppLog.d(TAG, "MSG_DETAILED");
                       int app_id=msg.arg1;
                       Intent intent=new Intent(AppSearchActivity.this,DetailedActivity.class);
                       intent.putExtra("app_id",app_id);
                       startActivity(intent);
                       overridePendingTransition(R.anim.zoomin,R.anim.zoomout);
                       break;
                 }
            }
        };
	}
	
    /**
     * 进入下载管理 
     */
    public void downloadMgr() {
        Intent intent = new Intent();
        intent.setClass(AppSearchActivity.this, DownloadRecordActivity.class);
        startActivity(intent);
    }
    
	public void initView(){//初始化
		// view init
		mEditText = (EditTextButton) this.findViewById(R.id.search_edittext);
		mImageView = (ImageView) this.findViewById(R.id.animation_imageview);
		mListView = (ListView) this.findViewById(R.id.search_result_listview);
		mListView.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				AppLog.d(TAG, "onKey");
				AppLog.d(TAG, "keyCode == KeyEvent.KEYCODE_DPAD_RIGHT     " + (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT));
				AppLog.d(TAG, "event.getAction() == KeyEvent.ACTION_DOWN    " + (event.getAction() == KeyEvent.ACTION_DOWN));
				if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN){
					AppLog.d(TAG, "111111111111");
//					if(!mListView.hasFocus()){
						AppLog.d(TAG, "22222222222");
						ViewGroup mGridLayout = (ViewGroup) mViewFlipper.getCurrentView();
						AnimationButton button = (AnimationButton) ((ViewGroup)mGridLayout.getChildAt(0)).getChildAt(0);
						button.requestFocus();
						control.transformAnimation(mImageView, mListView, button, AppSearchActivity.this, false, true);
						button.startAnimation();
//					}
						return true;
				} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN){
					if(mListView.getSelectedItem() == mListViewAdapter.getItem(0)){
						mEditText.requestFocus();
						control.transformAnimationForImage(mImageView, mListView, mEditText, AppSearchActivity.this, false, true);
						return true;
					}
				} else if(keyCode==KeyEvent.KEYCODE_DPAD_DOWN&&event.getAction()==KeyEvent.ACTION_DOWN){
					if(mListView.getSelectedItemPosition()==(mListViewAdapter.getCount()-1)){
						return true;
					}
				}
				return false;
			}
		});
		mUpImageView = (ImageView) this.findViewById(R.id.up_image);
		mDownImageView = (ImageView) this.findViewById(R.id.down_imgae);
		mViewFlipper = (ViewFlipper) this.findViewById(R.id.app_search_viewflipper);
		mDownloadTextView = (DownloadTextView) this.findViewById(R.id.top_down_record);
		mButtonVoice = (AnimationImageButton) this.findViewById(R.id.search_button_voice);
		mButtonSearch = (AnimationImageButton) this.findViewById(R.id.search_button_search);
        mDownRecord=(DownloadTextView)findViewById(R.id.top_down_record);
		mButtonSearch.setOnClickListener(this);
		mButtonSearch.setOnClickListener(this);
		mDownloadTextView.setOnClickListener(this);
		mEditText.setOnClickListener(this);
		mRootLayout = (RelativeLayout)findViewById(R.id.app_search_result_rootview);
		
		mAppSearchResultEdittextLayout = (SearchResultLayout) findViewById(R.id.search_edit_linearlayout);
		mAppSearchResultEdittextLayout.setBackImageView(mImageView);
		mAppSearchResultEdittextLayout.setFocusGroup(mRootLayout);
		mAppSearchResultEdittextLayout.setEditText(mEditText.getEditText());
    	
		mEditText.getEditText().setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				AppLog.d(TAG, "-------------------edit------------keyCode="+keyCode);
				if(keyCode==KeyEvent.KEYCODE_ENTER || keyCode==KeyEvent.KEYCODE_DPAD_CENTER){
					InputMethodManager im = ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE));
					im.showSoftInput(mEditText.getEditText(),0);
					EditTextButton.isH = true;
					return true;
				}
				if(keyCode==KeyEvent.KEYCODE_ESCAPE||keyCode==KeyEvent.KEYCODE_BACK){
					if(EditTextButton.isH){
						InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
						imm.hideSoftInputFromWindow(mEditText.getEditText().getWindowToken(),0);
						EditTextButton.isH = false;
						return true;
					}
				}
				return false;
			}
		});
		
		mButtonVoice.setBackImageView(mImageView);
		mButtonSearch.setBackImageView(mImageView);
		mDownloadTextView.setBackImageView(mImageView);
		mEditText.setBackImageView(mImageView);
		
		mButtonSearch.setImageResource(R.drawable.btn_search);
		mButtonVoice.setImageResource(R.drawable.btn_voice);
		
		mDataOperate=new DataOperate(mContext,mSearchMainHandler);
		mPageBean.setPageSize(3);
		mDataOperate.setPageBean(mPageBean);
		
		Message mm=mSearchMainHandler.obtainMessage();
		mm.obj=mButtonSearch;
		mm.what=MSG_FOCUS;
		mSearchMainHandler.sendMessageDelayed(mm, 100);
	}
	
	public void  initListView(){
		mListViewAdapter = new SimpleAdapter(mContext, mNewAdpaterList, 
				    R.layout.app_search_item_layout, new String[]{"name","type"}, new int[]{R.id.app_search_item_appname,R.id.app_search_item_appclass});
		mListView.setAdapter(mListViewAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(mSearchList!=null){
					AppsBean app = mSearchList.get(position);
					Intent intent=new Intent(AppSearchActivity.this,DetailedActivity.class);
					AppLog.d(TAG, "---------------------------appid"+app.getID());
	                intent.putExtra("app_id", app.getID());
	                startActivity(intent);
	                overridePendingTransition(R.anim.zoomin,R.anim.zoomout);
				}
			}
		});
		mListView.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				for(int i=0;i<parent.getCount();i++){
					if(i==position){
						parent.getChildAt(position).setBackgroundResource(R.drawable.search_selected);
					}else{
						parent.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
					}
					
				}
				
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				for(int i=0;i<parent.getCount();i++){
					parent.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
				}
			}
		});
		mListView.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus){
					if(mListView!=null&&mListView.getSelectedView()!=null){
						mListView.getSelectedView().setBackgroundColor(Color.TRANSPARENT);
						Utils.setFocus(mListView);
					}
				}else{
					if(mListView.getSelectedView()!=null){
						mListView.getSelectedView().setBackgroundResource(R.drawable.search_selected);
					}
				}
			}
		});
	}
	
    public void initViewFlipper(){
    	 mLayoutInflater = this.getLayoutInflater();
    	 mLayout = new DetailedActivityAppLayout(this);
    	 mLayout.setColumnCount(1);
    	 mLayout.setRowCount(3);
    	 mLayout.setBackImageView(mImageView);
      	 mLayout.setColumnCount(1);
      	 mLayout.setOnSlidingAtEndListener(new DetailedActivityAppLayout.OnSlidingAtEndListener() {
			public boolean onSlidingAtUp(DetailedActivityAppLayout layout, View view) {
				AppLog.d(TAG, "-----------------onSlidingAtUp-----------");
				if(mPageBean.getPageNo() == 1){
					mDownRecord.requestFocus();
					control.transformAnimation(mImageView, view, mDownRecord, AppSearchActivity.this, true, true);
					return true;
				}
				previousPage();
				return true;
			}
			public boolean onSlidingAtDown(DetailedActivityAppLayout layout, View view) {
				AppLog.d(TAG, "-----------------onSlidingAtDown-----------");
				nextPage();
				return true;
			}
		});
         for(int i=0;i<3;i++){
	       	 View view = mLayoutInflater.inflate(R.layout.detailed_item_layout, null);
	     	 AnimationButton button = (AnimationButton) view.findViewById(R.id.item_img);
	     	 button.setOnClickListener(this);
	     	 button.setImageView(mImageView);
//	     	 if(i==0){
//	     		 button.setOnKeyListener(new View.OnKeyListener() {
//					@Override
//					public boolean onKey(View v, int keyCode, KeyEvent event) {
//						if(event.getAction()==KeyEvent.ACTION_DOWN&&keyCode==KeyEvent.KEYCODE_DPAD_UP){
//							if(mPageBean.getPageNo()==1){//表示没有上一页
//					            return false;
//							}
//							previousPage();
//							return true;
//						}
//						return false;
//					}
//				});
//	     	 }
//	     	 if(i==2){
//	     		 button.setOnKeyListener(new View.OnKeyListener() {
//					@Override
//					public boolean onKey(View v, int keyCode, KeyEvent event) {
//						if(event.getAction()==KeyEvent.ACTION_DOWN&&keyCode==KeyEvent.KEYCODE_DPAD_DOWN){
//							nextPage();
//							return true;
//						}
//						return false;
//					}
//				});
//	     	 }
	     	 mArrayAnimationButton.add(button);
	     	 mLayout.addView(view);
         }
         mViewFlipper.addView(mLayout);
         AppLog.d(TAG, "mViewFlipper.getChildCount()"+mViewFlipper.getChildCount());
    }
    
    @Override
	protected void onNewIntent(Intent intent) {
    	AppLog.d(TAG, "---------onNewIntent()---------intent"+intent.getIntExtra("app_id",-1));
		setIntent(intent);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		   super.onResume();
	        AppLog.d(TAG," onResume ");
	        
	     
	}
	
    public void setData(){
    	
    	if(mAppList==null){
    		return;
    	}
    	if(turnDown==1){
    		((AnimationButton)((LinearLayout)mLayout.getChildAt(2)).getChildAt(0)).stopAnimation();
    		control.transformAnimation(mImageView, mLayout.getChildAt(2), mLayout.getChildAt(0), AppSearchActivity.this, false, true);
			((LinearLayout)mLayout.getChildAt(0)).getChildAt(0).requestFocus();
			((AnimationButton)((LinearLayout)mLayout.getChildAt(0)).getChildAt(0)).startAnimation();
    	}else if(turnDown==2){
    		((AnimationButton)((LinearLayout)mLayout.getChildAt(0)).getChildAt(0)).stopAnimation();
    		control.transformAnimation(mImageView, mLayout.getChildAt(0), mLayout.getChildAt(2), AppSearchActivity.this, false, true);
			((LinearLayout)mLayout.getChildAt(2)).getChildAt(0).requestFocus();
			((AnimationButton)((LinearLayout)mLayout.getChildAt(2)).getChildAt(0)).startAnimation();
    	}else if(turnDown == 0){
    		if(isTag){
    			mSearchMainHandler.sendEmptyMessage(BENZ);
    		}
    		isTag = false;
    	}
    	
    	//先进行内存的释放
    	if(null!=mLayout){
    		for(int i=0;i<mLayout.getChildCount();i++){
    			AnimationButton button = (AnimationButton)mLayout.getChildAt(i).findViewById(R.id.item_img);
    			BitmapDrawable drawable =(BitmapDrawable)button.getImageView().getDrawable();
    			if(null!=drawable){//第一次进搜索结果页,drawable会为null
    				Bitmap bmp = drawable.getBitmap();
    				if (null != bmp && !bmp.isRecycled()){
    					bmp.recycle();
    					bmp = null;
    					AppLog.d(TAG, "---------------bmp.recycle()-----------------");
    				}
    			}
    		}
    	}
    	//设置右侧图片
		for(int i=0;i<mAppList.size();i++){
			View view=mLayout.getChildAt(i);
			AnimationButton ab=(AnimationButton)view.findViewById(R.id.item_img);
			ab.setTag(mAppList.get(i).getID());
			if(null==mAppList.get(i).getNatImageUrl()||mAppList.get(i).getNatImageUrl().equals("")){
				ab.setImageRes(R.drawable.app_default_img);
			}else{
				ab.setImageURI(Uri.parse(mAppList.get(i).getNatImageUrl()));
			}
			((TextView)view.findViewById(R.id.item_name)).setText(mAppList.get(i).getAppName());
			view.setVisibility(View.VISIBLE);
			int appStatus = tDBUtils.queryStatusByPkgName(mAppList.get(i).getPkgName());
			ImageView popIv = (ImageView) view.findViewById(R.id.detail_item_pop);
			ImageView popTvT = (ImageView) view.findViewById(R.id.pop_update);
			if(appStatus==Constants.APP_STATUS_INSTALLED){
				String newVersion = mAppList.get(i).getVersion(); 
            	String oldVersion = tDBUtils.queryVersionByPkgName(mAppList.get(i).getPkgName());
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
		if(mAppList.size()<3){
			for(int i=mAppList.size();i<3;i++){
				mLayout.getChildAt(i).setVisibility(View.GONE);
			}
		}
	}

	public void setData(String str) {//
		if(str == null){
			mPageBean.setKeyWord("");
		}else{
			mPageBean.setKeyWord(str);
		}
		mDataOperate.setPageBean(mPageBean);
		mSearchList = mDataOperate.getDataFormService(RequestParam.ActionIndex.GETSEARCHLIST);
	}
	
	public Handler mSearchMainHandler= new Handler(){//
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			 case MSG_SHOW_APP:
                 AppLog.d(TAG, "MSG_SHOW_APP");
                 if(mViewFlipper.getCurrentView()==null){
                 	initViewFlipper();
                 }
                 setData();
                 
                 if(mPageBean.getPageNo()==1){
                     mUpImageView.setVisibility(View.INVISIBLE);
                 }else{
                	 mUpImageView.setVisibility(View.VISIBLE);
                 }
                 if(mPageBean.getPageNo()==mPageBean.getPageTotal()){
                     mDownImageView.setVisibility(View.INVISIBLE);
                 }else{
                	 mDownImageView.setVisibility(View.VISIBLE);
                 }
                 if(mPageBean.getPageNo()>1&&mPageBean.getPageNo()<mPageBean.getPageTotal()){
                	 mUpImageView.setVisibility(View.VISIBLE);
                	 mDownImageView.setVisibility(View.VISIBLE);
                 }
                 if(mPageBean.getPageTotal()<2){
                	 mUpImageView.setVisibility(View.INVISIBLE);
                	 mDownImageView.setVisibility(View.INVISIBLE);
                 }
            	 break;
			 case MSG_SHOW_LIST:
				 mListViewAdapter.notifyDataSetChanged();
//				 mImageView.setVisibility(View.INVISIBLE);
				 if(mListViewAdapter!=null&&mListViewAdapter.getCount()>0){
					 mListView.requestFocus();
					 mListView.setSelection(0);
					 control.clearFource();
				 }else{
					 Toast.makeText(getApplicationContext(), R.string.search_null, 1).show();
				 }
				 break;
			 case MSG_NO_KEYWORD:
				 Toast.makeText(getApplicationContext(), R.string.search_null, 1).show();
				 break;
	    	 case MSG_FINISH_ONE_APP:
	    		 AppLog.d(TAG, "msg_show_one_app");
	    		 loadNum = loadNum +1;
	    		 if(loadNum == mAppList.size()){
	    			 mSearchMainHandler.sendEmptyMessage(MSG_SHOW_APP);
	    			 loadNum = 0;
	    		 }
                 break; 
                 
	    	 case MSG_FOCUS:
	    		 if(AnimationImageButton.class.isInstance(msg.obj)){
	    			 ((AnimationImageButton)msg.obj).requestFocus();
	    			 control.transformAnimationForImage(mImageView,(AnimationImageButton)msg.obj , AppSearchActivity.this,true,false);
	    		 }else if(AnimationProgress.class.isInstance(msg.obj)){
	    			 ((AnimationProgress)msg.obj).requestFocus();
	    			 control.transformAnimationForImage(mImageView,(AnimationProgress)msg.obj , AppSearchActivity.this,true,false);
	    		 }else if(AnimationTextButton.class.isInstance(msg.obj)){
	    			 ((AnimationTextButton)msg.obj).requestFocus();
	    			 control.transformAnimationForImage(mImageView,(AnimationTextButton)msg.obj , AppSearchActivity.this,true,false);
	    		 }
	    		 break;
	    	 case BENZ:
	    		 if(mListViewAdapter == null || mListViewAdapter.getCount()<=0){
	    			 //AppLog.d(TAG, "------------mListViewAdapter.getCount() <= 0--------------------");
	    			 break;
	    		 }else{
	    			 mListView.requestFocus();
					 mListView.setSelection(0);
	    			 control.clearFource();
	    			 //AppLog.d(TAG, "------------mListViewAdapter.getCount() > 0--------------------");
	    		 }
	    		 break;
	        }
			super.handleMessage(msg);
		}
	};
		
	public void Test(){
		if(mAppList==null){
			AppLog.d(TAG, "mAppList ==null");
			return;
		}
		int size = mAppList.size();
		if(size>0){
			for(int i=0;i<size;i++){
				AppsBean ap = mAppList.get(i);
				AppLog.d(TAG,ap.getAppName()+ap.getPkgName() );
			}
		}
	}
	
    /**
     * 下一页
     */
    public void nextPage(){
        if(!mPageBean.nextPage()){//表示没有下一页
            return;
        }
        turnDown=1;
        mWorkHandler.removeMessages(MSG_GET_APP);
        mWorkHandler.sendEmptyMessageDelayed(MSG_GET_APP,Constants.Delayed.TIME5);
    }
    
    /**
     * 上一页
     */
    public void previousPage(){
        if(!mPageBean.previousPage()){//表示没有上一页
            return;
        }
        turnDown=2;
        mWorkHandler.removeMessages(MSG_GET_APP);
        mWorkHandler.sendEmptyMessageDelayed(MSG_GET_APP,Constants.Delayed.TIME5);
    }
    
    
    /**
     * 组装adpater数据
     * @param listApp
     */
    public void setAdapterData(List<AppsBean> listApp){
        Map<String, Object> map;
        mNewAdpaterList.clear();
        for(int i=0;i<listApp.size();i++){
            map= new HashMap<String, Object>();
            map.put("name",listApp.get(i).getAppName()+listApp.get(i).getVersion());
            map.put("type", listApp.get(i).getTypeName());
            mNewAdpaterList.add(map);
        }
    }
    
    public class AppReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			AppLog.d(TAG, "---------BroadcastReceiver----------------");
			if(arg1.getAction().equals(Constants.INTENT_UPDATE_COMPLETED)){
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

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.search_button_search:
				mWorkHandler.sendEmptyMessage(MSG_SHOW_APP);//sou suo jieguo
				break;
			case R.id.search_button_voice:
				//语音搜索
				break;
			case R.id.item_img:
                if(v.getTag()==null) return;
				int type = (Integer) v.getTag();
                Message msg=mWorkHandler.obtainMessage();
                msg.what=MSG_DETAILED;
                msg.arg1=type;
                mWorkHandler.sendMessage(msg);
				break;
			case R.id.top_down_record:
				downloadMgr();
				break;
		}
	}
}
