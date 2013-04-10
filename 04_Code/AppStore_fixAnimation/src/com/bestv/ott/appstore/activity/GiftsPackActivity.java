package com.bestv.ott.appstore.activity;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
 
import com.bestv.ott.appstore.activity.DetailedActivity.AppReceiver;
import com.bestv.ott.appstore.animation.AnimUtils;
import com.bestv.ott.appstore.animation.MoveControl;
import com.bestv.ott.appstore.common.AppPackageBean; 
import com.bestv.ott.appstore.common.AppsBean;
import com.bestv.ott.appstore.common.Constants;
import com.bestv.ott.appstore.parser.AppsPackParser;
import com.bestv.ott.appstore.pay.PayMng;
import com.bestv.ott.appstore.pay.PayMng.CallbackActionListener;
import com.bestv.ott.appstore.utils.AppLog;
import com.bestv.ott.appstore.utils.CaCheManager;
import com.bestv.ott.appstore.utils.NetUtil;
import com.bestv.ott.appstore.utils.OrdersCaCheManager;
import com.bestv.ott.appstore.utils.RequestParam;
import com.bestv.ott.appstore.utils.StringUtils;
import com.bestv.ott.appstore.utils.Utils;
import com.bestv.ott.appstore.R;
import com.bestv.ott.framework.BesTVServicesMgr;
import com.bestv.ott.framework.IBesTVServicesConnListener;
import com.bestv.ott.framework.services.ApkInfoService;
import com.bestv.ott.framework.services.OrderService;
import com.bestv.ott.framework.services.UserService;
import com.bestv.ott.util.bean.ResultParam;
import com.bestv.ott.util.bean.SubsParam;
import com.bestv.ott.util.bean.SubsProduct;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 大礼包详情页面 
 */
public class GiftsPackActivity extends BaseActivity implements View.OnKeyListener, OnFocusChangeListener,View.OnClickListener{ 
	
	private static final String TAG = "GiftsPackActivity";
	
	private static final int TAG_UPGRADE = 0;
	private static final int TAG_UPGRADE_STATUS = 1;
	private static final int TAG_STATUS = 2;
	protected static final int MSG_FOCUS = 10;
	private static final int TAG_CHECKMNG = 100;
	protected static final int MSG_STOP_DIALOG = 101;
	protected static final int MSG_CHECK_OVER = 102;
	protected static final int MSG_CHECK_SUCCESS = 103;

	private static final String T_APP_PACK_ID = "pack_id";
	private static final String T_APP_CODE = "app_product_code";

	private LoadDetailedTask mLoadDetailedTask = null;
	private NetUtil mNetUtil;
	private String pkgId ;
	private String pkgCode ;
	private AppPackageBean mAppPackageBean ;
	
	private TextView mPackName ;
	private TextView mPriceTV ;
	private TextView mPromPriceTV ;
	private TextView mPkgDesc ;
	private TextView mPkgTotalPrice ;
	private TextView mPackPage ;
	private ImageView mPageLeft ,mPageRight ;
	
	private Button mPackOrder ;
	private ListView mAppsList ;
	private MyFolderListAdapter mListAdapter ;
	
	private static PayMng mPayMng ;
	
	private int pageNo = 1;
	private ImageView mGiftIcon;
	private ImageView mImageView;
	private TextView mDownRecord;
	private int totalPage = 0;
	private TextView mSearch;
	private boolean isBuy = false;
	private String mProductCode;
	private String appPackId;
	private AppReceiver mAppReceiver = null;
	private static HashMap<Integer,String> hashMap=new HashMap<Integer,String>();
	private int indexApp=0;
	private List<AppsBean> appList;
	private boolean isFirstIn = false;//第一次进入禁止某些操作
	private String status_yes;
	private String status_not;
	private boolean isCheck = false;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.app_gifts_pack);
        
        parent = (ViewGroup)findViewById(R.id.root_relativelayout);
        viewGroupFocus = parent.getDescendantFocusability();
        isFirstIn = true;
        mPayMng = new PayMng(getApplicationContext());
        mNetUtil = new NetUtil(GiftsPackActivity.this);
        appList= new ArrayList<AppsBean>();
        Intent intent = getIntent();
        mProductCode = intent.getStringExtra(T_APP_CODE);
        appPackId = intent.getStringExtra(T_APP_PACK_ID);
        setupViews();
        processExtraData();
        
        IntentFilter filter=new IntentFilter();
        filter.addAction(Constants.INTENT_ACTION_ORDER_OK);
        mAppReceiver=new AppReceiver();
        this.registerReceiver(mAppReceiver, filter);
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy-------------------");
		hashMap.clear();
		if(mPayMng != null ){
			mPayMng.destroy();
		}
		if(mAppReceiver!=null){
            this.unregisterReceiver(mAppReceiver);
        }
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
	    stopProgressDialog();
	    super.onPause();
	}

	@SuppressWarnings("static-access")
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if(keyCode == event.KEYCODE_ESCAPE){
			finish();
		}
		return false;
	}
	
	private void processExtraData(){ 
    	Intent intent = getIntent();
    	startProgressDialog();
		if(intent != null ){
			/* 已购买 */
			if(OrdersCaCheManager.getInstance().findOrder(mProductCode)!=null){
			    AppLog.d(TAG, "----- has order -----");
			    stopProgressDialog();
			    isBuy = true;
			    mPackOrder.setText(GiftsPackActivity.this.getString(R.string.has_buy));
			    mDownRecord.setAlpha(AnimUtils.VIEW_ALPHA_FLAG);
			    mDownRecord.setFocusable(true);
			    mDownRecord.setFocusableInTouchMode(true);
			    mDownRecord.requestFocus();
			    
			    mPackOrder.setOnFocusChangeListener(this);
	            mPackOrder.setAlpha(AnimUtils.VIEW_ALPHA_FLAG);
	            mPackOrder.setFocusable(true);
	            mPackOrder.setFocusableInTouchMode(true);
	            mPackOrder.setOnClickListener(this);
			}else{
			    AppLog.d(TAG, "----- not order -----");
			    isBuy = false;
			    mPackOrder.setOnFocusChangeListener(this);
                mPackOrder.setAlpha(AnimUtils.VIEW_ALPHA_FLAG);
                mPackOrder.setFocusable(true);
                mPackOrder.setFocusableInTouchMode(true);
                mPackOrder.setOnClickListener(this);
		        mPackOrder.requestFocus();
			}
		}
		
        if(mLoadDetailedTask!=null&&mLoadDetailedTask.getStatus()!=LoadDetailedTask.Status.FINISHED){
        	mLoadDetailedTask.cancel(true);
        }
        AppLog.d(TAG,"----------PackId = " + appPackId );
        mLoadDetailedTask = new LoadDetailedTask();
        mLoadDetailedTask.execute(appPackId);
    }
	
	
	private void setupViews(){
	    
	    status_yes = this.getString(R.string.app_order_yes);
	    status_not = this.getString(R.string.app_order_not);
	    
	    mImageView = (ImageView) this.findViewById(R.id.animation_imageview);
	    mGiftIcon = (ImageView)this.findViewById(R.id.gift_icon);
		control = new MoveControl(GiftsPackActivity.this, mImageView); 
		
		mPackName = (TextView)this.findViewById(R.id.pack_name);
		TextPaint tp = mPackName.getPaint();
		tp.setFakeBoldText(true);//中文加粗
		
		mDownRecord = (TextView)findViewById(R.id.top_down_record);
		mDownRecord.setOnFocusChangeListener(this);
		mDownRecord.setOnClickListener(this);
		
		mSearch = (TextView)findViewById(R.id.top_search);
		mSearch.setOnFocusChangeListener(this);
		mSearch.setOnClickListener(this);
		
		mPriceTV = (TextView)this.findViewById(R.id.pack_price);
		mPriceTV.setText(getString(R.string.app_price,"0.00" ));
		mPriceTV.getPaint().setFlags(Paint. STRIKE_THRU_TEXT_FLAG ); 
		
		mPromPriceTV = (TextView)this.findViewById(R.id.pack_promotion);
		mPromPriceTV.setText(getString(R.string.pack_price,"0.00" ));
		
		mPkgDesc = (TextView)this.findViewById(R.id.pack_desc);
		mPkgDesc.setText(getString(R.string.pack_desc,""));
		
		mPkgTotalPrice = (TextView)this.findViewById(R.id.pack_total_price);
		mPkgTotalPrice.setText(getString(R.string.pack_total_price,"0.00"));
		
		mPackPage = (TextView)this.findViewById(R.id.pack_page);
		mPackPage.setText(getString(R.string.pack_page,new Object[]{""+1,""+1}));
		
		mAppsList = (ListView)this.findViewById(R.id.app_store_listview);
		
		mPageLeft = (ImageView)this.findViewById(R.id.app_store_left_image);
		mPageRight = (ImageView)this.findViewById(R.id.app_store_right_image); 
		
		mPackOrder = (Button)this.findViewById(R.id.pack_order);
		
		initListener();
	}
	
	
    private void initListener() {
        
        mAppsList.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                for(int i=0;i<parent.getCount();i++){
                    if(i==position){
                        if(!isFirstIn){
                            parent.getChildAt(position).setBackgroundResource(R.drawable.gift_select);
                        }
                        isFirstIn = false;
                    }else{
                        parent.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
                    }
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
                for(int i=0;i<parent.getCount();i++){
                    parent.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
                }
            }
        });
        
        
        mAppsList.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    control.hideMoveView();
                    if(mAppsList.getCount()>0){
                        mAppsList.setSelection(0);
                    }
                    if(mAppsList.getSelectedView()!=null){
                        mAppsList.getSelectedView().setBackgroundResource(R.drawable.gift_select);
                    }
                }else{
                    if(mAppsList !=null && mAppsList.getSelectedView()!=null){
                        mAppsList.getSelectedView().setBackgroundColor(Color.TRANSPARENT);
                        Utils.setFocus(mAppsList);
                    }
                }
            }
        });
        
		
		mAppsList.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.d(TAG, "----pageNo = "+pageNo   +"     |totalPage = "+totalPage);
                if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN){
                    AppLog.d(TAG, "mAppsList.setOnKeyListener-----right-----");
                    if(pageNo==totalPage){/* last page,if want to , do some remind */
                        //*****
                        return true;
                    }
                    gotopage(1);
                    return true;
                }else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN){
                    if(pageNo==1){/* first page ,set next left focus to mPackOrder */
                        mPackOrder.setAlpha(AnimUtils.VIEW_ALPHA_FLAG);
                        mPackOrder.requestFocus();
                        return true;
                    }
                    gotopage(-1);
                    return true;
                }else if(keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN){
                    if(mAppsList.getChildCount()>0 && mAppsList.getSelectedItemPosition()==0){
                        mSearch.setAlpha(AnimUtils.VIEW_ALPHA_FLAG);
                        mSearch.requestFocus();
                        return true;
                    }
                }
                
                if(event.getAction()==KeyEvent.ACTION_DOWN&&keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
                    if(mAppsList.getChildCount()>0 && mAppsList.getSelectedItemPosition()<3){
                        if(mAppsList.getSelectedItemPosition() == (mAppsList.getChildCount()-1)){
                            mPackOrder.setAlpha(AnimUtils.VIEW_ALPHA_FLAG);
                            mPackOrder.requestFocus();
                            return true;
                        }
                    }
                }
                return false;
            }
        });
    }
	
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getAction()==KeyEvent.ACTION_DOWN){
            if(event.getKeyCode()==KeyEvent.KEYCODE_DPAD_RIGHT  || event.getKeyCode()==KeyEvent.KEYCODE_DPAD_UP){
                if(mPackOrder.hasFocus() && mAppsList.getCount()>0){
                    mAppsList.requestFocus();
                    mAppsList.setSelection(0);
                    return true;
                }
            }else if(event.getKeyCode()==KeyEvent.KEYCODE_DPAD_LEFT){
                if(mSearch.hasFocus() && mAppsList.getCount()>0){
                    mAppsList.requestFocus();
                    mAppsList.setSelection(0);
                    return true;
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }
	
	/* turn page */
	protected void gotopage(int i) {
		int tempPage = pageNo + i ;
		AppLog.d(TAG,"###################gotopage = " + tempPage ); 
		if(tempPage < 1 && tempPage > totalPage){
			return ;
		}
		pageNo = tempPage ;
		if(mLoadDetailedTask!=null&&mLoadDetailedTask.getStatus()!=LoadDetailedTask.Status.FINISHED){
        	mLoadDetailedTask.cancel(true);
        }
		
        Log.d(TAG,"##################>>>>>>>>>>>>>>>>>>" + appPackId );
        mLoadDetailedTask = new LoadDetailedTask();
        mLoadDetailedTask.execute(appPackId);
        
	}

	/* get data */
	private class LoadDetailedTask extends AsyncTask<Object, Void, List<AppPackageBean>>{
		@Override
		protected List<AppPackageBean> doInBackground(Object... params) {
			Log.d(TAG, "-##########---------LoadPackDetailedTask-----doInBackground-------");
			String urlStr=  RequestParam.URL_PACK_DETAILS + String.valueOf(params[0]) ;
			Log.d(TAG,"uuuuuuuuuuuuuuuuuuuuu>>>>>>>>>>>>>>>>>>" + urlStr );
			Map<String,String> param=new TreeMap<String,String>();
			param.put("pageNo",String.valueOf(pageNo));
			param.put("pageSize", String.valueOf(6));
			List<AppPackageBean> list =(List<AppPackageBean>)mNetUtil.getNetData(param, urlStr, null, new AppsPackParser());
			if(list!=null && list.size() > 0){
				mAppPackageBean = list.get(0);
			}
			if(mAppPackageBean!=null){
			    NetUtil.loadImage(mAppPackageBean.getImage(), mAppPackageBean.getNativeImage());
			    AppLog.d(TAG, "----getNativeImage == "+mAppPackageBean.getNativeImage());
			}else{
				AppLog.d(TAG, "--------mAppPackageBean--");
			}
			return null;
		}
		@Override
		protected void onPostExecute(List<AppPackageBean> result) { 
			mainHandler.sendEmptyMessage(TAG_UPGRADE) ;
		}
		@Override
		protected void onCancelled(List<AppPackageBean> result) {
			super.onCancelled(result);
		}
		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
	}
	
	
	public Handler mainHandler=new Handler(){
		public void handleMessage(Message msg) {  
            switch(msg.what){ 
	            case TAG_UPGRADE:
	                stopProgressDialog();
	            	updateValue();
	            	break ; 
	            case TAG_UPGRADE_STATUS:
	                
	                /*----------------------------------自定义Toast----------------------------------------- */
	                Toast myToast = new Toast(GiftsPackActivity.this);
	                View toastView = getLayoutInflater().inflate(R.layout.order_toast_layout, null);
	                TextView ttoastText = (TextView)toastView.findViewById(R.id.text);
	                ttoastText.setText(GiftsPackActivity.this.getString(R.string.buy_success));
	                myToast.setGravity(Gravity.BOTTOM , 0, 20);
	                myToast.setDuration(Toast.LENGTH_SHORT);
	                myToast.setView(toastView);
	                myToast.show();
//	                Toast.makeText(getApplicationContext(), GiftsPackActivity.this.getString(R.string.buy_success), 0).show();
	                /*------------------------------------------------------------------------------------- */
	                orderSuccess();
	                break;
	            case TAG_STATUS:
	            	for(int i=0;i<appList.size();i++){
	            	    if(mAppsList.getChildAt(i)!=null){
	            	        TextView tv=(TextView)mAppsList.getChildAt(i).findViewById(R.id.app_status);
	            	        if(hashMap.containsKey(appList.get(i).getID())){
	            	            if(status_yes.equals(hashMap.get(appList.get(i).getID()))){
	            	                tv.setText(""+status_yes);
	            	                tv.setTextColor(Color.RED);
	            	            }else if(status_not.equals(hashMap.get(appList.get(i).getID()))){
	            	                tv.setText(""+status_not);
	            	            }
	            	        }else{
	            	            tv.setText("");
	            	        }
	            	    }
	            	}
	                break;
	            /* 反复鉴权 */
	            case TAG_CHECKMNG:
	                isCheck = true;
	                mainHandler.removeMessages(TAG_CHECKMNG);
	                startProgressDialog();
	                new Thread(){
	                    public void run() {
	                        checkGiftPayMng();
	                    }
	                }.start();
	                break;
	                
	            case MSG_STOP_DIALOG:
	                stopProgressDialog();
	                break;
	            case MSG_CHECK_OVER:
	                isCheck = false;
	                break;
	            case MSG_CHECK_SUCCESS:
	                stopProgressDialog();
	                OrdersCaCheManager.getInstance().addGiftCache(mProductCode);
	                mPackOrder.setText(GiftsPackActivity.this.getString(R.string.has_buy));
	                break;
            }
		}
	};

	protected void updateValue() {  
		
		if( mAppPackageBean != null ){
		    totalPage = mAppPackageBean.getTotalPages();
		    AppLog.d(TAG, "^^^^^^^^^^^^^^^^^totalPage = "+totalPage);
			mPackName.setText(mAppPackageBean.getName());
			mPriceTV.setText(getString(R.string.app_price,StringUtils.formatMoney(mAppPackageBean.getOriginalPrice()))); 
			mPromPriceTV.setText(getString(R.string.pack_price,StringUtils.formatMoney(mAppPackageBean.getPromotionPrice())));
			mPkgDesc.setText(getString(R.string.pack_desc,mAppPackageBean.getRemark())); 
			mPkgTotalPrice.setText(getString(R.string.pack_total_price,StringUtils.formatMoney(mAppPackageBean.getOriginalPrice())));
			if(mAppPackageBean.getTotalPages()==0){
			    mPackPage.setText(getString(R.string.pack_page,new Object[]{0+"",""+0}));
			}else{
			    mPackPage.setText(getString(R.string.pack_page,new Object[]{mAppPackageBean.getPageNo()+"",""+mAppPackageBean.getTotalPages()}));
			}
			if(mAppPackageBean.getAppsBean()!=null&&mAppPackageBean.getAppsBean().length>0){
				appList=Arrays.asList(mAppPackageBean.getAppsBean());// set data 
			}
			
			Bitmap bm = CaCheManager.requestBitmap(mAppPackageBean.getNativeImage());
			if(bm!=null){
			    mGiftIcon.setImageBitmap(bm);
			}else{
			    mGiftIcon.setImageResource(R.drawable.pack_icon);
			    File imageF=new File(mAppPackageBean.getNativeImage());
                if(imageF.exists()){
                    imageF.delete();
                }
			}
			
			if(pageNo==1){// first page
			    mPageLeft.setVisibility(View.GONE);
			}
			if(pageNo==totalPage){//last page
			    mPageRight.setVisibility(View.GONE);
			}
			if(totalPage==1){//only one page
			    mPageRight.setVisibility(View.GONE);
			    mPageLeft.setVisibility(View.GONE);
			}
			if(totalPage>1 && pageNo<totalPage){
                mPageRight.setVisibility(View.VISIBLE);
            }
			if(pageNo>1){
			    mPageLeft.setVisibility(View.VISIBLE);
			}
		}
		 
		mListAdapter = new MyFolderListAdapter(getLayoutInflater());
		mAppsList.setAdapter(mListAdapter);
		mAppsList.setOnItemClickListener(new OnItemClickListener() { 
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) { 
				AppLog.d(TAG,"setOnItemClickListener :" + arg2 + "/ " + arg3 );
				AppLog.d(TAG,"DDDDDDDDDDDDDDDDDDDDDDDD:" + appList.get(arg2).getID() ) ;
				DetailedActivity.open(GiftsPackActivity.this, appList.get(arg2).getID(), null, -1) ;
			} 
		});
		indexApp=0;
		new Thread(){
			public void run() {
				Log.d(TAG, "--run--indexApp="+indexApp);
				if(appList.size()>indexApp)
				setOneApp(appList.get(indexApp));
			}
		}.start();
	}
	
	public void setOneApp(final AppsBean app){
		if(hashMap.containsKey(app.getID())){
			mainHandler.sendEmptyMessage(TAG_STATUS);
			indexApp++;
            Log.d(TAG, "indexApp="+indexApp+";appList.size()="+appList.size());
            if(indexApp<appList.size()){
            	setOneApp(appList.get(indexApp));
            }
			return;
		}
		SubsParam subsParam2 = new SubsParam();
        subsParam2.setServiceCodes(app.getServiceCode());
        subsParam2.setProductCode(app.getAppProductCode());
        subsParam2.setAuthType(PayMng.AUTHTYPE_PRODUCT);
        subsParam2.setBizType(PayMng.BIZTYPE_APP);
        final boolean b = mPayMng.authOrder(new CallbackActionListener() {
            public void execute() {
                if(mPayMng.getResult()!=null){
                    ResultParam mResult = (ResultParam)mPayMng.getResult();
                    int retCode = mResult.getReturnCode();
                    AppLog.d(TAG, "---- checkAppPayMng --> retCode---- "+retCode+";appID="+app.getID()+";appName="+app.getAppName());
                    if(retCode==PayMng.REQ_AUTH_SUCCESS){
                        hashMap.put(app.getID(), status_yes);
                        mainHandler.sendEmptyMessage(TAG_STATUS);
                    }else{
                        hashMap.put(app.getID(), status_not);
                        mainHandler.sendEmptyMessage(TAG_STATUS);
                    }
                }
                indexApp++;
                Log.d(TAG, "indexApp="+indexApp+";appList.size()="+appList.size());
                if(indexApp<appList.size()){
                	setOneApp(appList.get(indexApp));
                }
            }
        }, subsParam2);
        if(!b){
//            mainHandler.postDelayed(runnableForOne, 2000);
        }
	}
	
	/* pay success */
	protected void orderSuccess(){
	    if(hashMap!=null){
	        hashMap.clear();
	    }
        mPackOrder.setText(GiftsPackActivity.this.getString(R.string.has_buy));
        mLoadDetailedTask = new LoadDetailedTask();
        mLoadDetailedTask.execute(appPackId);
	    isBuy = true;
	    isFirstIn = true;
	}
	
	public static void openGiftsOrderDialog(Context context ,String appPackId , String productCode){
		Intent intent=new Intent();
		intent.setClass(context, GiftsPackActivity.class);
		AppLog.d(TAG, "-----openGiftsOrderDialog --> appPackId ="+appPackId);
		AppLog.d(TAG, "-----openGiftsOrderDialog --> productCode ="+productCode);
		intent.putExtra(T_APP_PACK_ID, appPackId);
		intent.putExtra(T_APP_CODE, productCode);
		context.startActivity(intent);
	}
	
	
	/* ListView Adapter */
	protected class MyFolderListAdapter extends BaseAdapter {
		private LayoutInflater inflater;
		
		public MyFolderListAdapter() {
			super();
			inflater = getLayoutInflater();
			
		}

		public MyFolderListAdapter(LayoutInflater inflater) {
			super();
			this.inflater = inflater; 
		}

		@Override
		public int getCount() {
			return appList.size();
		}

		@Override
		public Object getItem(int position) {
			if (position < appList.size()) {
				return appList.get(position);
			} else
				return null;
		}

		@Override
		public long getItemId(int position) {
			if (position < appList.size()) {
				return appList.get(position).getID();
			} else
				return 0;
		}

		public View getView(int position, View convertView, ViewGroup vi) {
		    
			AppsBean folder = appList.get(position);
			View view;
			if (convertView != null ) { 
				view = convertView;
			} else {
				view = inflater.inflate(R.layout.listview_gift_pack, vi, false);
			}
			
			FolderViewHolder holder = (FolderViewHolder) view.getTag();
			if (holder == null) {
				holder = new FolderViewHolder();
				holder.app_index = (TextView) view.findViewById(R.id.app_index);
				holder.app_name = (TextView) view.findViewById(R.id.app_name);
				holder.app_price = (TextView) view.findViewById(R.id.app_price);
				holder.app_status = (TextView) view.findViewById(R.id.app_status);
//				holder.app_img = (ImageView) view.findViewById(R.id.app_img);
				view.setTag(holder);
			}

			if (folder != null) {
			    holder.app_index.setText(getString(R.string.pack_page_index,String.valueOf((pageNo-1)*6+position+1)));
				holder.app_name.setText(folder.getAppName());
				holder.app_price.setText(getString(R.string.app_order_price_val,StringUtils.formatMoney(folder.getOriginalPrice())));
//				holder.app_img.setVisibility(View.INVISIBLE);
				AppLog.d(TAG, "-------position = "+position +   "   name"+folder.getAppName()+"  id"+folder.getID());
			} else {
				holder.app_index.setText("");
				holder.app_name.setText("");
				holder.app_price.setText("");
				holder.app_status.setText("");
//				holder.app_img.setVisibility(View.INVISIBLE);
			}
			
			return view;
		}
 
	}
	
	
	protected class FolderViewHolder {
		public TextView app_index;
		public TextView app_name;
		public TextView app_price;
		public TextView app_status;
//		public ImageView app_img;
		
	}
	
	
	protected void onResume() {
	    isCheck = false;
		super.onResume();
	}
	
	/* Animation control */
	public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus){
            control.addFocusView(v);
        }
    }
	
	/* 鉴权大礼包 */
	private void checkGiftPayMng() {
        if(isBuy){
            AppLog.d(TAG, "------you have order------  then return");
            return;
        }
        AppLog.d(TAG, "------first to checkPayMng , please wait...------");
        /* first to 鉴权 */
        SubsParam subsParam1 = new SubsParam();
        subsParam1.setProductCode(mProductCode);
        subsParam1.setAuthType(PayMng.AUTHTYPE_PRODUCT);
        subsParam1.setBizType(PayMng.BIZTYPE_APP) ;
        final Boolean res=mPayMng.authOrder(new CallbackActionListener() {
            public void execute() {
                mainHandler.sendEmptyMessage(MSG_CHECK_OVER);
                if(mPayMng.getResult()!=null){
                    ResultParam mResult = (ResultParam)mPayMng.getResult();
                    int retCode = mResult.getReturnCode();
                    AppLog.d(TAG, "----retCode---- "+retCode);
                    /* -1:网络异常 */
                    if(retCode == PayMng.REQ_NETWORK_ERROR){
                        AppChargeMsgActivity.openActivity(GiftsPackActivity.this,
                                GiftsPackActivity.this.getString(R.string.check_pay_net_error_title),
                                GiftsPackActivity.this.getString(R.string.check_pay_net_error) , 
                                AppChargeMsgActivity.TYPE_ORDER_AGAIN);
                    }
                    /* 后台异常 */
                    else if(retCode == PayMng.REQ_AUTH_MSG_EXCEPTION){
                        AppChargeMsgActivity.openActivity(GiftsPackActivity.this,
                                GiftsPackActivity.this.getString(R.string.check_pay_service_error_title),
                                GiftsPackActivity.this.getString(R.string.check_pay_service_error), 
                                AppChargeMsgActivity.TYPE_ORDER_FAILED);
                    }
                    /* 0：鉴权失败/没有订购 */
                    else if(retCode == PayMng.REQ_AUTH_FAILED){
                        List<SubsProduct> list = mResult.getOrderProduct();
                        SubsProduct subProduct = null;
                        AppLog.d(TAG,"--------------getOrderProduct().list.size = " + list.size());
                        if(list.size() == 0){
                            AppChargeMsgActivity.openActivity(GiftsPackActivity.this, null,
                                    GiftsPackActivity.this.getString(R.string.app_order_no_product),
                                    AppChargeMsgActivity.TYPE_ORDER_FAILED);
                        }else{
                            for(int i=0;i<list.size();i++){
                                if(list.get(i).getCode().equals(mProductCode)){
                                    subProduct = list.get(i);
                                    break;
                                }
                            }
                            if(subProduct==null){ /* SubsProduct 为 null */
                                AppLog.d(TAG, "------result SubsProduct = null ---------");
                                AppChargeMsgActivity.openActivity(GiftsPackActivity.this, null,
                                        GiftsPackActivity.this.getString(R.string.app_order_no_product),
                                        AppChargeMsgActivity.TYPE_ORDER_FAILED);
                            }else{
                                Resources res = GiftsPackActivity.this.getResources();
                                Bitmap bm = CaCheManager.requestBitmap(mAppPackageBean.getNativeImage());
                                if(bm==null){ bm = BitmapFactory.decodeResource(res, R.drawable.pack_icon); }
                                /* gift has no ServiceCode */
                                String GiftServiceCode = null;
                                AppChargeConfirmActivity.openConfirmOrderDialog(GiftsPackActivity.this, 
                                        subProduct,GiftServiceCode,mAppPackageBean.getName(), -100,bm);
                            }
                        }
                    }else if(retCode==PayMng.REQ_AUTH_SUCCESS){
                        AppLog.d(TAG, "-------had order--------");
                        isBuy = true;
                        mainHandler.sendEmptyMessage(MSG_CHECK_SUCCESS);
                    }else{
                        //均没有对应的返回值
                        AppChargeMsgActivity.openActivity(GiftsPackActivity.this, null,
                                GiftsPackActivity.this.getString(R.string.app_order_no_product),
                                AppChargeMsgActivity.TYPE_ORDER_FAILED);
                    }
                }
            }
        }, subsParam1);
        AppLog.d(TAG,"-----------------order pack-----------------   res = "+res);
        if(!res){
            mainHandler.postDelayed(runnable, 3000);
        }
    }
	
	
	/* 进入下载管理  */
    private void downloadMgr() {
        Intent intent = new Intent();
        intent.setClassName(GiftsPackActivity.this, "com.bestv.ott.appstore.activity.DownloadRecordActivity");
        startActivity(intent);
    }
    
    /* in search */
    private void search(){
        Intent intent = new Intent(GiftsPackActivity.this,AppSearchActivity.class);
        startActivity(intent);
    }
    
    @Override
    public void onClick(View v) {
        switch(v.getId()){
        case R.id.top_down_record:
            downloadMgr();
            break;
        case R.id.top_search:
            search();
            break;
        case R.id.pack_order:
            if(isCheck){
                return;
            }
            if(isBuy){
                return;
            }
            mainHandler.removeMessages(TAG_CHECKMNG);
            mainHandler.sendEmptyMessage(TAG_CHECKMNG);
            break;
        }
    }
    
    public class AppReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent in) {
            if(in.getAction().equals(Constants.INTENT_ACTION_ORDER_OK)){
                if(in.getStringExtra("product_Code")==null || "".equals(in.getStringExtra("product_Code").trim()) || "null".equals(in.getStringExtra("product_Code").trim())){
                    return;
                }
                String boradReceCode = in.getStringExtra("product_Code");
                if(boradReceCode.equals(mProductCode)){
                    mainHandler.sendEmptyMessage(TAG_UPGRADE_STATUS);
                }
            }
        }
    }
    
    
    /* remove dialog */
    Runnable runnable = new Runnable(){
        public void run() {
            mainHandler.sendEmptyMessage(MSG_CHECK_OVER);
//            AppChargeMsgActivity.openActivity(GiftsPackActivity.this,GiftsPackActivity.this.getString(R.string.error_title),GiftsPackActivity.this.getString(R.string.error_content) , PayMng.REQ_AUTH_MSG_EXCEPTION);
        }  
    };
    Runnable runnableForOne = new Runnable(){
        public void run() {
//            indexApp++;
//            Log.d(TAG, "indexApp="+indexApp+";appList.size()="+appList.size());
//            if(indexApp<appList.size()){
//                setOneApp(appList.get(indexApp));
//            }
        }
    };
}
