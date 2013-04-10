package com.joysee.appstore.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.joysee.appstore.animation.AnimUtils;
import com.joysee.appstore.animation.MoveControl;
import com.joysee.appstore.common.AppsBean;
import com.joysee.appstore.common.Constants;
import com.joysee.appstore.common.PageBean;
import com.joysee.appstore.db.DBUtils;
import com.joysee.appstore.thread.LoadAppThread;
import com.joysee.appstore.thread.SearchAppThread;
import com.joysee.appstore.utils.AppLog;
import com.joysee.appstore.utils.CaCheManager;
import com.joysee.appstore.utils.RequestParam;
import com.joysee.appstore.utils.Utils;
import com.joysee.appstore.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
import android.widget.EditText;
import android.widget.GridLayout;
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
	
	public static final int MSG_SHOW_APP=0;//显示应用数据
    public static final int MSG_GET_APP=1;//获取应用数据
    public static final int MSG_SHOW_LIST=3;//获取分类数据
    public static final int MSG_GET_CLASS=4;//获取分类数据
    public static final int MSG_FINISH_ONE_CLASS=5;//下载完成一张分类图片
    public static final int MSG_NET_NOT_CONNECT=6;//网络连接不通
    public static final int MSG_DETAILED=7;//进入详情页
    public static final int MSG_FOCUS=8;

	private static final int BENZ = 9;
	private boolean isTag = true;
	private static int loadNum = 0;
	private static int loadNums = 3;//下载够三张图片里,才setData
	private boolean isHasSoftKey = false; //是否有软键盘
    
	private Context mContext;
	private PageBean mPageBean=new PageBean();
	private PageBean mSearchPage=new PageBean();
	private List <Map<String, Object>> mNewAdpaterList=new ArrayList<Map<String, Object>>();
	// view
	private EditText mEditText;
	private TextView mButtonSearch;
	private TextView mDownloadTextView;
	private ViewFlipper mViewFlipper;
	private LayoutInflater mLayoutInflater;
	private GridLayout mLayout;
	private ImageView mUpImageView;
	private ImageView mDownImageView;
	private ListView mListView;
	private int turnDown=0;//0表示第一次进，另边不要上焦点，1表示向下翻页，2表示向上翻页
	private int searchDown=0;
	private DBUtils tDBUtils ;
	
	private SimpleAdapter mListViewAdapter;
	
	private List<AppsBean> mAppList=null;//最新应用信息
	private List<AppsBean> mSearchList=null;//搜索信息
	public Handler mWorkHandler;
	private AppReceiver mAppReceiver = null;
	private LoadAppThread mThread;
	private SearchAppThread mSearchThread;
	private boolean isPage=false,isPageSearch=false;//表示正在翻页
	
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
        parent=(ViewGroup)this.findViewById(R.id.app_search_result_rootview);
        initListView();
        String keyWord=this.getIntent().getStringExtra("keyWord");
	    AppLog.d(TAG, "-------------keyWord="+keyWord);

	    IntentFilter filter=new IntentFilter();
	    filter.addAction(Constants.INTENT_UPDATE_COMPLETED);
	    mAppReceiver=new AppReceiver();
	    mThread=new LoadAppThread(AppSearchActivity.this,mPageBean,RequestParam.Action.GETRECOMMENDLIST);
	    mThread.start();
	    mSearchPage.setPageSize(8);
	    if(keyWord!=null&&!"".equals(keyWord.trim())){
	        mEditText.setText(keyWord);
	    	mSearchPage.setKeyWord(keyWord);
	    	mSearchThread=new SearchAppThread(AppSearchActivity.this,mSearchPage,null);
	    	mSearchThread.start();
	    }
	}
	
	public void initView(){//初始化
		// view init
		mEditText = (EditText) this.findViewById(R.id.search_edittext);
		mImageView = (ImageView) this.findViewById(R.id.animation_imageview);
		control=new MoveControl(this,mImageView);
		mListView = (ListView) this.findViewById(R.id.search_result_listview);
		mListView.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN){
					ViewGroup mGridLayout = (ViewGroup) mViewFlipper.getCurrentView();
					ImageView button = (ImageView) ((ViewGroup)mGridLayout.getChildAt(0)).getChildAt(0);
					button.setAlpha(AnimUtils.VIEW_ALPHA_FLAG);
					button.requestFocus();
					return true;
				} else if(event.getAction()==KeyEvent.ACTION_DOWN&&(keyCode==KeyEvent.KEYCODE_ENTER||keyCode==KeyEvent.KEYCODE_DPAD_CENTER)){
					int position=mListView.getSelectedItemPosition();
					AppLog.d(TAG, "---------position="+position);
					if(mSearchList!=null){
						AppsBean app = mSearchList.get(position);
						Intent intent=new Intent(AppSearchActivity.this,DetailedActivity.class);
						AppLog.d(TAG, "---------------------------appid"+app.getID());
		                intent.putExtra("app_id", app.getID());
		                startActivity(intent);
		                overridePendingTransition(R.anim.zoomin,R.anim.zoomout);
					}
				}
				return false;
			}
		});
		mUpImageView = (ImageView) this.findViewById(R.id.up_image);
		mDownImageView = (ImageView) this.findViewById(R.id.down_imgae);
		mViewFlipper = (ViewFlipper) this.findViewById(R.id.app_search_viewflipper);
		mDownloadTextView = (TextView) this.findViewById(R.id.top_down_record);
		mButtonSearch = (TextView) this.findViewById(R.id.search_button_search);
		mButtonSearch.setOnClickListener(this);
		mDownloadTextView.setOnClickListener(this);
		mDownloadTextView.setTag("mDownloadTextView");
		mDownloadTextView.setNextFocusLeftId(mButtonSearch.getId());
		mEditText.setNextFocusRightId(mButtonSearch.getId());
		mButtonSearch.setNextFocusLeftId(mEditText.getId());
		mDownloadTextView.setOnFocusChangeListener(this);
		mEditText.setOnFocusChangeListener(this);
		mButtonSearch.setOnFocusChangeListener(this);
    	
		mEditText.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				AppLog.d(TAG, "-------------------edit------------keyCode="+keyCode);
				if(keyCode==KeyEvent.KEYCODE_ENTER || keyCode==KeyEvent.KEYCODE_DPAD_CENTER){
					InputMethodManager im = ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE));
					im.showSoftInput(mEditText,0);
					isHasSoftKey = true;
					return true;
				}
				if(keyCode==KeyEvent.KEYCODE_ESCAPE||keyCode==KeyEvent.KEYCODE_BACK){
					if(isHasSoftKey){
						isHasSoftKey=false;
						InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
						imm.hideSoftInputFromWindow(mEditText.getWindowToken(),0);
						return true;
					}
				}
				if(event.getAction()==KeyEvent.ACTION_DOWN&&keyCode==KeyEvent.KEYCODE_DPAD_DOWN){
					AppLog.d(TAG, "------------");
					if(mListViewAdapter!=null&&mListViewAdapter.getCount()>0){
						 mListView.requestFocus();
						 mListView.setSelection(0);
						 mImageView.setVisibility(View.INVISIBLE);
					 }
				}
				return false;
			}
		});
		
		mPageBean.setPageSize(3);
		
		mButtonSearch.setAlpha(AnimUtils.VIEW_ALPHA_FLAG);
		mButtonSearch.setFocusableInTouchMode(true);
		mButtonSearch.setFocusable(true);
		mButtonSearch.requestFocus();
	}
	
	public void  initListView(){
		mListViewAdapter = new SimpleAdapter(mContext, mNewAdpaterList, 
				    R.layout.app_search_item_layout, new String[]{"name","type"}, new int[]{R.id.app_search_item_appname,R.id.app_search_item_appclass});
		mListView.setAdapter(mListViewAdapter);
		mListView.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
			    for(int i=0;i<parent.getCount();i++){
                    if(i==position){
                      View v=parent.getChildAt(position);
                      Log.d(TAG, "---------->>>>>>>>>>>>>>>>i="+i);
                      if(v!=null){
//                          v.setBackgroundResource(R.drawable.listview_select_bg);
                      }
                    }else{
                        View v=parent.getChildAt(i);
                        if(v!=null){
                            v.setBackgroundColor(Color.TRANSPARENT);
                        }
                    }
                }
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				for(int i=0;i<parent.getCount();i++){
				    if(parent.getChildAt(i)!=null)
					parent.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
				}
			}
		});
		mListView.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus){
					if(mListView!=null&&mListView.getSelectedView()!=null){
						/*mListView.getSelectedView().setBackgroundColor(Color.TRANSPARENT);
						Utils.setFocus(mListView);*/
					    for(int i=0;i<mListView.getChildCount();i++){
                            View child=mListView.getChildAt(i);
                            if(child!=null){
                                child.setBackgroundColor(Color.TRANSPARENT);
                                child.setBackgroundDrawable(null);
                            }
                        }
					}
				}else{
					control.hideMoveView();
					AppLog.d(TAG, "----mListView--mImageView.setVisibility(View.GONE)");
					if(mListView.getSelectedView()!=null){
						AppLog.d(TAG, "----mListView--getSelectedView");
//						mListView.getSelectedView().setBackgroundResource(R.drawable.search_selected);
					}
				}
			}
		});
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if(event.getAction()==KeyEvent.ACTION_DOWN){
    		if(event.getKeyCode()==KeyEvent.KEYCODE_DPAD_DOWN||event.getKeyCode()==KeyEvent.KEYCODE_DPAD_LEFT||
    				event.getKeyCode()==KeyEvent.KEYCODE_DPAD_RIGHT||event.getKeyCode()==KeyEvent.KEYCODE_DPAD_UP){
    			downTimes++;
    			/*if(event.getKeyCode()==KeyEvent.KEYCODE_DPAD_DOWN){
        			if(mListView.hasFocus()&&mListView.getSelectedItemPosition()==(mListViewAdapter.getCount()-1)){
        				return true;
        			}
        			if(mEditText.hasFocus()&&mListViewAdapter.getCount()>0){
        				mListView.requestFocus();
    					mListView.setSelection(0);
        				return true;
        			}
        		}
        		if(event.getKeyCode()==KeyEvent.KEYCODE_DPAD_UP){
        			if(mListView.hasFocus()&&mListView.getSelectedItemPosition()==0){
        				mEditText.setAlpha(AnimUtils.VIEW_ALPHA_FLAG);
    					mEditText.requestFocus();
        				return true;
        			}
        		}*/
    			if(event.getKeyCode()==KeyEvent.KEYCODE_DPAD_DOWN){
                    if(mListView.hasFocus()&&mListView.getSelectedItemPosition()==(mListViewAdapter.getCount()-1)){
                        nextPageSearch(); 
                        return true;
                    }
                    if(mEditText.hasFocus()&&mListViewAdapter.getCount()>0){
                        mListView.requestFocus();
                        mListView.setSelection(0);
                        return true;
                    }
                }
                if(event.getKeyCode()==KeyEvent.KEYCODE_DPAD_UP){
                    if(mListView.hasFocus()&&mListView.getSelectedItemPosition()==0){
                        Log.d(TAG, "---pageNo="+mSearchPage.getPageNo()+";"+mSearchPage.getPageTotal());
                        if(mSearchPage.getPageNo()==1){
                            mEditText.setAlpha(AnimUtils.VIEW_ALPHA_FLAG);
                            mEditText.requestFocus();
                            return true;
                        }else{
                            previousPageSearch();
                            return true;
                        }
                    }
                }
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
            		AppLog.d(TAG, "----nextview : "+nextview);
            		if(mViewFlipper!=null&&mViewFlipper.getChildCount()>0){
            			if(nextview==null){
                			if(mMoveDirect==View.FOCUS_DOWN&&AppsBean.class.isInstance(this.getCurrentFocus().getTag())){//向下
                				if(control.focusQueue.size()==0){
                					if(mPageBean.getPageNo()<mPageBean.getPageTotal()&&!isPage){
                						this.nextPage();
                						return true;
                					}
                				}else{
                					return true;
                				}
                			}
                		}else{
                			if(AppsBean.class.isInstance(this.getCurrentFocus().getTag())&&TextView.class.isInstance(nextview)){//向上
                				if(mMoveDirect==View.FOCUS_UP){
                					if(control.focusQueue.size()==0&&!isPage){
                						if(mPageBean.getPageNo()>1){
                							this.previousPage();
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
	
	class LeftListener implements View.OnKeyListener{
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if(event.getAction()==KeyEvent.ACTION_DOWN&&keyCode==KeyEvent.KEYCODE_DPAD_LEFT){
				if(mListViewAdapter!=null&&mListViewAdapter.getCount()>0){
					 mListView.requestFocus();
					 mListView.setSelection(0);
					 return true;
				}else{
					mButtonSearch.requestFocus();
					return true;
				}
			}
			return false;
		}
	}
	
    public void initViewFlipper(){
    	 mLayoutInflater = this.getLayoutInflater();
    	 
    	 mLayout = new GridLayout(this);
    	 mLayout.setUseDefaultMargins(true);//TODO
    	 mLayout.setAlignmentMode(30);
    	 
    	 mLayout.setColumnCount(1);
    	 mLayout.setRowCount(3);
         for(int i=0;i<3;i++){
	       	 View view = mLayoutInflater.inflate(R.layout.detailed_item_layout, null);
	     	 ImageView button = (ImageView) view.findViewById(R.id.item_img);
	     	 button.setOnClickListener(this);
	     	 button.setOnKeyListener(new LeftListener());
	     	 button.setOnFocusChangeListener(this);
	     	 if(i==2){
	     		 button.setOnKeyListener(new View.OnKeyListener() {
					@Override
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						if(event.getAction()==KeyEvent.ACTION_DOWN&&keyCode==KeyEvent.KEYCODE_DPAD_LEFT){
							if(mListViewAdapter!=null&&mListViewAdapter.getCount()>0){
								 mListView.requestFocus();
								 mListView.setSelection(0);
								 return true;
							}else{
								mButtonSearch.requestFocus();
								return true;
							}
						}
						return false;
					}
				});
	     	 }
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
		AppLog.d(TAG," onResume ");
        RelativeLayout mRootLayout = (RelativeLayout)findViewById(R.id.app_search_result_rootview);
		Drawable bg = getThemePaper();
        if (bg != null) {
            mRootLayout.setBackgroundDrawable(bg);
        } else {
            AppLog.d("getThemePaper() is null");
        }
	}
	
	public void refreshSearchList(List<AppsBean> searchList){
		if(null==searchList){
            return;
        }
		mSearchList=searchList;
		AppLog.d(TAG, "----refreshSearchList---mSearchList.size="+mSearchList.size());
		setAdapterData(mSearchList);
		mListViewAdapter.notifyDataSetChanged();
		 if(mListViewAdapter!=null&&mListViewAdapter.getCount()>0){
		     Log.d(TAG, "--------->>>>>>>>>>>>>>searchDown="+searchDown);
		     if(searchDown==1){
		         mListView.requestFocus();
	             mListView.setSelection(0);
		     }else if(searchDown==2){
		         mListView.requestFocus();
	             mListView.setSelection(mSearchList.size()-1);
		     }
			 mImageView.setVisibility(View.INVISIBLE);
		 }else{
			 Toast.makeText(getApplicationContext(), R.string.search_null, 1).show();
		 }
		 
	}
	
    public void refreshAppsList(List<AppsBean> appList){
    	mAppList=appList;
    	if(mAppList==null){
    		isPage=false;
    		return;
    	}
    	if(mViewFlipper.getCurrentView()==null){
         	initViewFlipper();
        }
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
    	View tempV=null;
    	if(turnDown==1){
    		tempV=mLayout.getChildAt(0);
			
    	}else if(turnDown==2){
    		tempV=mLayout.getChildAt(2);
    	}else if(turnDown == 0){
    		tempV=null;
    	}
		for(int i=0;i<mAppList.size();i++){
			View view=mLayout.getChildAt(i);
			ImageView ab=(ImageView)view.findViewById(R.id.item_img);
			ab.setTag(mAppList.get(i));
			if(null==mAppList.get(i).getNatImageUrl()||mAppList.get(i).getNatImageUrl().equals("")){
				ab.setImageResource(R.drawable.app_default_img);
			}else{
				Bitmap bitmap = CaCheManager.requestBitmap(mAppList.get(i).getNatImageUrl());
				if(null!=bitmap){
					ab.setImageBitmap(bitmap);
				}else{
					ab.setImageResource(R.drawable.app_default_img);
				}
			}
			((TextView)view.findViewById(R.id.item_name)).setText(mAppList.get(i).getAppName());
			view.setVisibility(View.VISIBLE);
			int appStatus = tDBUtils.queryStatusByPkgName(mAppList.get(i).getPkgName());
			ImageView popTvT = (ImageView) view.findViewById(R.id.pop_update);
			if(appStatus==Constants.APP_STATUS_INSTALLED){
				String newVersion = mAppList.get(i).getVersion();
            	String oldVersion = tDBUtils.queryVersionByPkgName(mAppList.get(i).getPkgName());
            	AppLog.d(TAG, "-----------------newVersion :"+newVersion +"| -------oldVersion :" +oldVersion);
            	if(Utils.CompareVersion(newVersion, oldVersion)){
        			popTvT.setVisibility(View.VISIBLE);
            	}else{
        			popTvT.setVisibility(View.GONE);
            	}
			}else{
    			popTvT.setVisibility(View.GONE);
        	}
		}
		if(mAppList.size()<3){
			for(int i=mAppList.size();i<3;i++){
				mLayout.getChildAt(i).setVisibility(View.GONE);
			}
		}
		if(tempV!=null){
			ImageView tempImage=(ImageView)tempV.findViewById(R.id.item_img);
			tempImage.setAlpha(AnimUtils.VIEW_ALPHA_FLAG);
			tempImage.setFocusable(true);
			tempImage.setFocusableInTouchMode(true);
			tempImage.requestFocus();
		}
		isPage=false;
	}
    
    /**
     * 下一页
     */
    public void nextPageSearch(){
        if(mSearchThread.isAlive()&&isPageSearch){
            return;
        }
        if(!mSearchPage.nextPage()){//表示没有下一页
            return;
        }
        searchDown=1;
        isPageSearch=true;
        mSearchThread=new SearchAppThread(AppSearchActivity.this,mSearchPage,null);
        mSearchThread.start();
    }
    
    /**
     * 上一页
     */
    public void previousPageSearch(){
        if(mSearchThread.isAlive()&&isPageSearch){
            return;
        }
        if(!mSearchPage.previousPage()){//表示没有上一页
            return;
        }
        isPageSearch=true;
        searchDown=2;
        mSearchThread=new SearchAppThread(AppSearchActivity.this,mSearchPage,null);
        mSearchThread.start();
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
        isPage=true;
        turnDown=1;
        mThread=new LoadAppThread(AppSearchActivity.this,mPageBean,RequestParam.Action.GETRECOMMENDLIST);
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
        isPage=true;
        turnDown=2;
        mThread=new LoadAppThread(AppSearchActivity.this,mPageBean,RequestParam.Action.GETRECOMMENDLIST);
        mThread.start();
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
    
    /**
     * 进入下载管理 
     */
    public void downloadMgr() {
        Intent intent = new Intent();
        intent.setClass(AppSearchActivity.this, DownloadRecordActivity.class);
        startActivity(intent);
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
						(view.findViewById(R.id.pop_update)).setVisibility(View.GONE);
					}
				}
			}
		}
    }

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if(hasFocus){
			switch(v.getId()){
			case R.id.top_down_record:
			case R.id.search_edittext:
			case R.id.search_button_search:
			case R.id.item_img:
				AppLog.d(TAG, "--onFocusChange--v.getTag="+v.getTag());
				control.addFocusView(v);
				break;
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.search_button_search:
				String str = mEditText.getText().toString();
         	    if(null==str||str.trim().equals("")){
         	    	if(mSearchList!=null){
         	    		mSearchList.clear();
         	    		refreshSearchList(mSearchList);
         	    	}else{
         	    		Toast.makeText(getApplicationContext(), R.string.search_null, 1).show();
         	    	}
         		   return;
         	    }
         	    if(mSearchThread!=null&&mSearchThread.isAlive()){
         	    	return;
         	    }
         	    searchDown=1;
         	    mSearchPage.setPageNo(1);
		    	mSearchPage.setKeyWord(mEditText.getText().toString());
		    	mSearchThread=new SearchAppThread(AppSearchActivity.this,mSearchPage,null);
		    	mSearchThread.start();
				break;
			case R.id.item_img:
                if(v.getTag()==null) return;
				AppsBean type = (AppsBean) v.getTag();
				Intent intent=new Intent(AppSearchActivity.this,DetailedActivity.class);
                intent.putExtra("app_id",type.getID());
                startActivity(intent);
                overridePendingTransition(R.anim.zoomin,R.anim.zoomout);
				break;
			case R.id.top_down_record:
				downloadMgr();
				break;
		}
	}
}
