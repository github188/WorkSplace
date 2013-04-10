package com.ismartv.ui;

import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.util.Log;
import android.widget.*;
import android.view.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View.OnClickListener;
import android.os.Message;
import android.os.Looper;
import android.os.Handler;
import android.view.animation.AnimationUtils;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;

import com.ismartv.doc.ISTVVodItemDetailDoc;
import com.ismartv.doc.ISTVResource;
import com.ismartv.util.Constants;

public class ISTVVodItemDetail extends ISTVVodActivity{
	private static final String TAG = "ISTVVodItemDetail";
	private static final int relateitemnum = 4;
	
	public static final int RES_STR_ITEM_TITLE = 0;
	public static final int RES_DOUBLE_ITEM_RATINGAVERAGE = 1;
	public static final int RES_STR_ITEM_ATTRS = 2;
	public static final int RES_STR_ITEM_DESCRIPTION = 3;
	public static final int RES_BMP_ITEM_POSTERURL = 4;
	public static final int RES_BMP_ITEM_PLAYURL = 5;
	public static final int RES_INT_RELATEITEML_COUNT = 6;
	public static final int RES_STR_RELATEITEML_TITLE = 7;
	public static final int RES_STR_RELATEITEML_DESCRIPTION = 8;
	public static final int RES_BMP_RELATEITEML = 9;
	public static final int RES_INT_RELATEITEML_ITEMPK = 10;
	public static final int RES_INT_EPISODE_COUNT = 11;
	public static final int RES_INT_EPISODE_REALCOUNT = 12;
	public static final int RES_INT_EPISODE_SUBITEM_PK = 13;	
	public static final int RES_BOOL_BOOKMARKED = 14;
	public static final int RES_BOOL_RELATEITEML_ISCOMPLEX = 15;
	public static final int RES_BOOL_RATINGAVERAGE_ISSUCCESS = 16;
	public static final int RES_BOOL_BOOKMARK_ADD_ISSUCCESS = 17;
	public static final int RES_BOOL_BOOKMARK_REMOVED_ISSUCCESS = 18;
	public static final int RES_INT_FINALEPISODE_SUBITEM_PK = 19;

	public static final long DELAY_BUTTON_CLICKED = 1000 ; 
	 
	//test, 18896, 52312, 18968, 18852, 18859, 49585, 49568, 19370, 49572, 19623, 53699
	private int itemPK;
	private ISTVVodItemDetailDoc doc;
	private String title;
	private double ratingaverage;
	private String attrs;
	private String description;
	private Bitmap posterurl;
	private URL playurl;
	private int relateitemsize;
	private int relateitembmpcompletenum = 0;
	private String relateitemtitles[];
	private String relateitemdescriptions[];
	private Bitmap relateitemBmps[];
	private int relateitempks[];
	private boolean relateIsComplex[];
	private int episodecount;
	private int episoderealcount;
	private int episodesubitempks[];
	private boolean clipBookmarked=false;
	private int finalEpisodePK = -1;
	private boolean updatePlay = false;
	private boolean needRequest=false;

	/*ui control*/
	private RelativeLayout itemdataillayout;

	
	private TextView ui_itemtitle;
	private RatingBar ui_ratingbar;
	private TextView ui_ratingaverage;
	private TextView ui_itemexpense;
	private TextView ui_itemattrs;
	private ImageView ui_itemposter;
	private Button ui_itemplay;
	private Button ui_itemepisodeenter;
	private Button ui_itemratingaverage;
	private Button ui_itembookmark;
	private TextView ui_itemdescriptiontitle;
	private TextView ui_itemdescriptioninfo;
	private TextView ui_relatetitle;
	private ListView ui_relatelist;
	private RelateAdapter relateAdapter;
	private Button ui_morelate;
	private int cur_select_item = -1;

	private ImageView ui_leftmask;
	private ImageView ui_rightmask;

	private LinearLayout itemrelateLayout;
	private RelativeLayout bufferLayout;
	private AnimationDrawable bufferAnim;
	
	private LinearLayout right_bufferLayout;
	private AnimationDrawable right_bufferAnim;

	private Timer loadTimer = null;	
	private Handler handler = null;
	
	private RelativeLayout ui_ratingbarmenubg;
	private TextView ui_PopupRateText;
	private Handler successHandler=new Handler();
	private Runnable runnable=new Runnable() {
		public void run() {
			hideSuccess();
		}
	};
	
	private void log(String msg){
		Log.d(TAG,"####### >> :" + msg );
	}
	
	private VodReceiver vodReceiver;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		Bundle bundle = this.getIntent().getExtras();
		if(bundle==null){
			itemPK = 18896;
		}else{
			itemPK = bundle.getInt("itemPK");
		}

		//Log.d(TAG, "itemPK = " + itemPK);

		relateitemsize = 0;

		if(relateitemtitles!=null){
			relateitemtitles = null;
		}

		if(relateitemdescriptions!=null){
			relateitemdescriptions = null;
		}	

		if(relateitemBmps!=null){
			relateitemBmps = null;
		}

		if(relateitempks!=null){
			relateitempks = null;
		}

		if(relateIsComplex!=null){
			relateIsComplex = null;
		}		

		episodecount = 0;
		episoderealcount= 0;

		if(episodesubitempks!=null){
			episodesubitempks = null;
		}

		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		ISTVVodItemDetailUiInit();
		
		doc = new ISTVVodItemDetailDoc(itemPK);
		setDoc(doc);

		handler = new Handler(){
			public void handleMessage(Message msg) {
				if(msg.what==1984){
					hideRightBuffer();
					loadTimer.cancel();
				}
			}
		};

		loadTimer = new Timer();
		loadTimer.schedule(new TimerTask() {
				public void run() {
					Message msg = handler.obtainMessage(1984);
					handler.sendMessage(msg);					
				}
			}, 8*1000, 8*1000);		
		
		IntentFilter filter=new IntentFilter();
        filter.addAction(Constants.INTENT_BOOK_MARK);
        vodReceiver=new VodReceiver();
        this.registerReceiver(vodReceiver, filter);
	}

	public void onDestroy(){
		loadTimer.cancel();
		doc.dispose();
		if(vodReceiver!=null){
			this.unregisterReceiver(vodReceiver);
		}
		super.onDestroy();
	}	

	protected void onDocGotResource(ISTVResource res){
		log("onDocGotResource>>>:" + res.getType() );
		String text;
		switch(res.getType()){
			case RES_STR_ITEM_TITLE:
				title = res.getString();
				break;
			case RES_DOUBLE_ITEM_RATINGAVERAGE:
				ratingaverage = res.getDouble();
				break;
			case RES_STR_ITEM_ATTRS:
				attrs = res.getString();
				break;
			case RES_STR_ITEM_DESCRIPTION:
				description = res.getString();
				break;
			case RES_BMP_ITEM_POSTERURL:
				posterurl = res.getBitmap();
				break;
			case RES_BMP_ITEM_PLAYURL:
				playurl = res.getURL();
				break;
				
			case RES_INT_RELATEITEML_COUNT:
				relateitemsize = res.getInt();
				if(relateitemsize > 0){
					relateitemtitles = new String[relateitemsize];
					relateitemdescriptions = new String[relateitemsize];
					relateitemBmps = new Bitmap[relateitemsize];
					relateitempks = new int[relateitemsize];
					relateIsComplex = new boolean[relateitemsize];
					
				}else{
					relateitemtitles = null;
					relateitemdescriptions = null;
					relateitemBmps = null;
					relateitempks = null;
					relateIsComplex = null;
				}
				break;
			case RES_STR_RELATEITEML_TITLE:
				if(relateitemtitles!=null){
					relateitemtitles[res.getID()] = res.getString();
				}
				break;
			case RES_STR_RELATEITEML_DESCRIPTION:
				if(relateitemdescriptions!=null){
					relateitemdescriptions[res.getID()] = res.getString();
					relateitembmpcompletenum++;
					//if(relateitembmpcompletenum == relateitemsize){
					if(relateitembmpcompletenum == 1){
						hideRightBuffer();
						loadTimer.cancel();						
					}
				}
				break;				
			case RES_BMP_RELATEITEML:
				if(relateitemBmps!=null){
					relateitemBmps[res.getID()] = res.getBitmap();
				}
				break;
				
			case RES_INT_RELATEITEML_ITEMPK:
				if(relateitempks!=null){
					relateitempks[res.getID()] = res.getInt();
				}
				break;

			case RES_BOOL_RELATEITEML_ISCOMPLEX:
				relateIsComplex[res.getID()] =res.getBoolean();
				break;				
				
			case RES_INT_EPISODE_COUNT:
				episodecount = res.getInt();
				if(episodecount > 0){
					episodesubitempks = new int[episodecount];
				}else{
					episodesubitempks = null;
				}
				updatePlay = true;
				break;

			case RES_INT_EPISODE_REALCOUNT:
				episoderealcount = res.getInt();
				if(episoderealcount >0) {
					episodesubitempks = new int[episoderealcount];
				}
				updatePlay = true;
				break;				
				
			case RES_INT_EPISODE_SUBITEM_PK:
				if(episodesubitempks!=null){
					episodesubitempks[res.getID()] = res.getInt();
				}
				break;

			case RES_BOOL_BOOKMARKED:
				clipBookmarked = res.getBoolean();
				break;
			
			case RES_BOOL_RATINGAVERAGE_ISSUCCESS:				
				Log.d(TAG, "RES_BOOL_RATINGAVERAGE_ISSUCCESS is "+res.getBoolean());
				if(res.getBoolean()) {
					text = getResources().getString(R.string.vod_ratingaverage_success);					
				} else {
					text = getResources().getString(R.string.vod_ratingaverage_unsuccess);					
				}
				showSuccess(text);
				successHandler.removeCallbacks(runnable);
				successHandler.postDelayed(runnable, 1000);
				break;
			case RES_BOOL_BOOKMARK_ADD_ISSUCCESS:
				Log.d(TAG, "RES_BOOL_BOOKMARK_ADD_ISSUCCESS is "+res.getBoolean());
				if(res.getBoolean()) {
					text = getResources().getString(R.string.vod_bookmark_add_success);					
				} else {
					text = getResources().getString(R.string.vod_bookmark_add_unsuccess);					
				}
				showSuccess(text);
				ISTVVodItemDetailUiSetBookmark();
				successHandler.removeCallbacks(runnable);
				successHandler.postDelayed(runnable, 1000);				
				break;
			case RES_BOOL_BOOKMARK_REMOVED_ISSUCCESS:
				Log.d(TAG, "RES_BOOL_BOOKMARK_REMOVED_ISSUCCESS is "+res.getBoolean());
				if(res.getBoolean()) {
					text = getResources().getString(R.string.vod_bookmark_remove_success);					
				} else {
					text = getResources().getString(R.string.vod_bookmark_remove_unsuccess);					
				}
				showSuccess(text);
				ISTVVodItemDetailUiSetBookmark();
				successHandler.removeCallbacks(runnable);
				successHandler.postDelayed(runnable, 1000);				
				break;
			
			case RES_INT_FINALEPISODE_SUBITEM_PK:
				finalEpisodePK = res.getInt();
			default:
				break;
		}
	}

	protected void onDocUpdate(){
		log("onDocUpdate: 00000000"  );
		/*Redraw the activity*/
		ISTVVodItemDetailUiSetTitle();
		ISTVVodItemDetailUiSetRatingAverage();
		ISTVVodItemDetailUiSetExpense();
		ISTVVodItemDetailUiSetAttrs();
		ISTVVodItemDetailUiSetPoster();
		ISTVVodItemDetailUiSetPlay();
		ISTVVodItemDetailUiSetRatingaverage();
		ISTVVodItemDetailUiSetBookmark();
		ISTVVodItemDetailUiSetDes();
		ISTVVodItemDetailUiSetRelate();
		ui_itemplay.invalidate();
		ui_itemepisodeenter.invalidate();
		hideBuffer();
		
		log("onDocUpdate: 1111 "  );
	}

	private void ISTVVodItemDetailUiInit(){
		setContentView(R.layout.vod_itemdetail);
		rootView=this.findViewById(R.id.ItemdetailLayout);

		ui_ratingbarmenubg = (RelativeLayout)findViewById(R.id.successlayout);
		ui_PopupRateText = (TextView)findViewById(R.id.PopupRateText);
		
		itemdataillayout = (RelativeLayout)findViewById(R.id.ItemdetailLayout);
		itemdataillayout.setVisibility(View.VISIBLE);
		
		ui_itemtitle = (TextView)findViewById(R.id.itemtitle);
		ui_ratingbar = (RatingBar)findViewById(R.id.ratingBar);
		ui_ratingaverage = (TextView)findViewById(R.id.ratingaverage);
		ui_itemexpense = (TextView)findViewById(R.id.itemexpense);
		ui_itemattrs = (TextView)findViewById(R.id.itemattrs);
		ui_itemposter = (ImageView)findViewById(R.id.itemposter);
		
		ui_itemplay = (Button)findViewById(R.id.itemplay);
		ui_itemplay.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					/*if(episodesubitempks!=null){
						gotoPlayer(itemPK, episodesubitempks[0]);
					}else*/{
						gotoPlayer(itemPK, finalEpisodePK);
					}
				}
				});
		ui_itemplay.setOnFocusChangeListener(new Button.OnFocusChangeListener(){

			public void onFocusChange(View v, boolean hasFocus) {

				// TODO Auto-generated method stub

				if(hasFocus == true) {
					ui_itemplay.setTextColor(getResources().getColor(R.color.vod_itemdetail_thirdtextcolor));
					
					viewmask(0);
				}else{
					ui_itemplay.setTextColor(getResources().getColor(R.color.vod_itemdetail_firsttextcolor));
				}
			}
		});
		

		ui_itemepisodeenter = (Button)findViewById(R.id.itemepisode);
		ui_itemepisodeenter.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					gotoitemepisodepage(itemPK);
				}
				});
		ui_itemepisodeenter.setOnFocusChangeListener(new Button.OnFocusChangeListener(){

			public void onFocusChange(View v, boolean hasFocus) {

				// TODO Auto-generated method stub

				if(hasFocus == true) {
					ui_itemepisodeenter.setTextColor(getResources().getColor(R.color.vod_itemdetail_thirdtextcolor));
					
					viewmask(0);
				}else{
					ui_itemepisodeenter.setTextColor(getResources().getColor(R.color.vod_itemdetail_firsttextcolor));
				}
			}
		});

		
		ui_itemratingaverage = (Button)findViewById(R.id.itemratingaverage);
		ui_itemratingaverage.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					boolean conn = isNetConnected(); 
					if(!conn){
						showPopupDialog(DIALOG_NET_BROKEN, getResources().getString(R.string.vod_net_broken_error));
//						Toast.makeText(getApplicationContext(),R.string.vod_net_broken_error , Toast.LENGTH_SHORT).show(); 
						return ;
					} 

					if(isNeedLogin()){
						showPopupLoginDialog(DIALOG_LOGIN);
					}else{				
						showPopupRateDialog(ratingaverage);
					} 
				}
		});
		ui_itemratingaverage.setOnFocusChangeListener(new Button.OnFocusChangeListener(){

			public void onFocusChange(View v, boolean hasFocus) { 
				// TODO Auto-generated method stub

				if(hasFocus == true) { 
					ui_itemratingaverage.setTextColor(getResources().getColor(R.color.vod_itemdetail_thirdtextcolor));
					viewmask(0);
					
				}else{
					ui_itemratingaverage.setTextColor(getResources().getColor(R.color.vod_itemdetail_firsttextcolor));
				}
				
			}
		});		
		
		ui_itembookmark = (Button)findViewById(R.id.itembookmark);
		ui_itembookmark.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					boolean conn = isNetConnected();
					if(!conn){
						showPopupDialog(DIALOG_NET_BROKEN, getResources().getString(R.string.vod_net_broken_error));
//						Toast.makeText(getApplicationContext(),R.string.vod_net_broken_error , Toast.LENGTH_SHORT).show();
						return ;
					}
					if(isNeedLogin()){
						showPopupLoginDialog(DIALOG_LOGIN);
					}else{ 
						disableButton(ui_itembookmark);
						if(clipBookmarked){
							doc.removeBookmark();
						}else{
							doc.addBookmark();
						}
						clipBookmarked = !clipBookmarked;
						
						enableButton(ui_itembookmark);
					}
				}
				});
		ui_itembookmark.setOnFocusChangeListener(new Button.OnFocusChangeListener(){

			public void onFocusChange(View v, boolean hasFocus) { 
				// TODO Auto-generated method stub

				if(hasFocus == true) { 
					ui_itembookmark.setTextColor(getResources().getColor(R.color.vod_itemdetail_thirdtextcolor));
					viewmask(0);
					
				}else{
					ui_itembookmark.setTextColor(getResources().getColor(R.color.vod_itemdetail_firsttextcolor));
				}
			}
		});			
		
		ui_itemdescriptiontitle = (TextView)findViewById(R.id.itemdescriptiontitle);
		ui_itemdescriptioninfo = (TextView)findViewById(R.id.itemdescriptioninfo);
		ui_relatetitle = (TextView)findViewById(R.id.relatetitle);
		ui_relatelist = (ListView)findViewById(R.id.relatelist);		

		relateAdapter = new RelateAdapter(this);
	
		ui_relatelist.setAdapter(relateAdapter);

		ui_relatelist.setOnItemSelectedListener(mOnSelectedListener);

		ui_relatelist.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			  	if(position < relateitemsize){
					if(relateIsComplex[position]){
			 			gotorelateitemdetail(relateitempks[position]);
					}else{
						/*if(episodesubitempks!=null){
							gotoPlayer(relateitempks[position], episodesubitempks[0]);
						}else*/{
							gotoPlayer(relateitempks[position], -1);
						}
					}
				}
			  }
		});	

		ui_relatelist.setOnFocusChangeListener(new ListView.OnFocusChangeListener(){

			public void onFocusChange(View v, boolean hasFocus) {

				// TODO Auto-generated method stub

				if(hasFocus == true) {
					
					viewmask(1);
				}else{
					//ui_relatelist.setSelection(-1);
				}
			}
		});	

		ui_morelate = (Button)findViewById(R.id.morerelate);

		ui_morelate.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					gotorelatepage(itemPK);
				}
		});
		ui_morelate.setOnFocusChangeListener(new Button.OnFocusChangeListener(){

			public void onFocusChange(View v, boolean hasFocus) {

				// TODO Auto-generated method stub

				if(hasFocus == true) { 
					viewmask(1);
				}
			}
		});
		

		ui_leftmask = (ImageView)findViewById(R.id.leftmask);
		ui_rightmask= (ImageView)findViewById(R.id.rightmask);	

			
		itemrelateLayout = (LinearLayout)findViewById(R.id.ItemRelateLayout);
		bufferLayout = (RelativeLayout)findViewById(R.id.BufferLayout);
		bufferAnim = (AnimationDrawable)((ImageView)findViewById(R.id.BufferImage)).getBackground();
		if(rootView!=null){
			Drawable bg = getThemePaper();
	        if (bg != null) {
	        	bufferLayout.setBackgroundDrawable(bg);
	        } else {
	            Log.d(TAG, "getThemePaper fail");
	        }
		}

		right_bufferLayout = (LinearLayout)findViewById(R.id.rightBufferLayout);
		right_bufferLayout = (LinearLayout)findViewById(R.id.rightBufferLayout);
		right_bufferAnim = (AnimationDrawable)((ImageView)findViewById(R.id.rightBufferImage)).getBackground();				
		showRightBuffer();
		showBuffer();
	}

	/**
	 * 
	 * @param btn
	 */
	private void disableButton(Button btn){
		btn.setEnabled(false);
	}
	
	/**
	 * @bug 2820, 延迟处理按钮响应速度
	 * @param btn
	 */
	private void enableButton(final Button btn){
		handler.postDelayed(new Runnable(){ 
			@Override
			public void run() {
				btn.setEnabled(true); 
			}
			
		}, DELAY_BUTTON_CLICKED) ;
		
	}
	
	
	private void ISTVVodItemDetailUiDeinit(){
		
	}

	private void ISTVVodItemDetailUiSetTitle(){
		log("title:" + title );
		if(title != null){
			ui_itemtitle.setText(title);
			ui_itemtitle.setSelected(true);
		}
		//setTextColor
		//setTextSize
	}

	private void ISTVVodItemDetailUiSetRatingAverage(){
		float tmp_ratingaverage = (float)ratingaverage;

		Log.d(TAG, "ratingaverage" + tmp_ratingaverage);

		ui_ratingbar.setRating(tmp_ratingaverage);

		String str_tmp = new Double(ratingaverage).toString();
		
//		Toast.makeText(this, str_tmp+"===="+str_tmp.substring(0, 3), Toast.LENGTH_LONG).show();

		ui_ratingaverage.setText(str_tmp.substring(0, 3));
	}

	private void ISTVVodItemDetailUiSetExpense(){
		ui_itemexpense.setText(R.string.vod_itemdexpense);
	}

	private void ISTVVodItemDetailUiSetAttrs(){
		log("ISTVVodItemDetailUiSetAttrs:" + attrs );
		if(attrs != null){
			ui_itemattrs.setText(attrs);
		}
	}

	private void ISTVVodItemDetailUiSetPoster(){
		if(posterurl != null){
			ui_itemposter.setImageBitmap(posterurl);
		}
	}

	private void ISTVVodItemDetailUiSetPlay(){
		if(updatePlay == false) {
			return;
		}		
		ui_itemplay.setText(R.string.vod_detailplay);
		ui_itemepisodeenter.setText(R.string.vod_itemepisode);
		if(episodesubitempks!=null){
			ui_itemplay.setLayoutParams(new LinearLayout.LayoutParams(
							                241,
							                72));	
			ui_itemplay.setBackgroundResource(R.drawable.itemdetail_play_left);
			ui_itemplay.setVisibility(View.VISIBLE);
			ui_itemepisodeenter.setVisibility(View.VISIBLE);
			if(needRequest==false){
                needRequest=true;
                ui_itemplay.requestFocus();
            }
		}else{
			ui_itemplay.setLayoutParams(new LinearLayout.LayoutParams(
							                482,
							                72));	
			ui_itemplay.setBackgroundResource(R.drawable.itemdetail_play);
			ui_itemplay.setVisibility(View.VISIBLE);
			ui_itemepisodeenter.setVisibility(View.GONE);
			if(needRequest==false){
                needRequest=true;
                ui_itemplay.requestFocus();
            }
		}
	}

	private void ISTVVodItemDetailUiSetRatingaverage(){
		ui_itemratingaverage.setText(R.string.vod_itemratingaverage);
	}

	private void ISTVVodItemDetailUiSetBookmark(){
		ui_itembookmark.setText(getString(!clipBookmarked?R.string.vod_itembookmark:R.string.vod_itemremovebookmark));
	}	

	private void ISTVVodItemDetailUiSetDes(){
		ui_itemdescriptiontitle.setText(R.string.vod_itemdescription);

		if(description!=null){
			ui_itemdescriptioninfo.setText("    "+description);
		}
	}

	private void ISTVVodItemDetailUiSetRelate(){
		ui_relatetitle.setText(R.string.vod_itemrelatetitle);
		relateAdapter.notifyDataSetChanged();
		
		if(relateitemsize < relateitemnum){
			ui_morelate.setEnabled(false);
		}else{
			ui_morelate.setEnabled(true);
		}
	}

	private class RelateAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private int selectItem;
		
		public RelateAdapter(Context context) {
			super();
			mInflater=LayoutInflater.from(context);			  
		}

		public int getCount() {
			return relateitemnum;
		}

		public Object getItem(int position) {
			return position;
		}
		
		public long getItemId(int position) {
			return position;
		}

		public void setSelectItem(int position){
			this.selectItem = position;
		}
        
		public int getSelectItem(){
			return this.selectItem;
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.vod_itemdetailrelatelist, null);
			  
				holder = new ViewHolder();
				holder.texttitle = (TextView) convertView.findViewById(R.id.ItemTextTitle);
				holder.textdes = (TextView) convertView.findViewById(R.id.ItemTextDes);
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);
				convertView.setTag(holder);
			}else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}
			
			// Bind the data efficiently with the holder.
			if(position < relateitemsize)
			{
				if((relateitemtitles != null) && (relateitemtitles[position] != null)){
					holder.texttitle.setText(relateitemtitles[position] );
				}

				if((relateitemdescriptions != null) && (relateitemdescriptions[position] != null)){
					holder.textdes.setText(relateitemdescriptions[position] );
				}
				
				if((relateitemBmps != null) && (relateitemBmps[position] != null)){
					holder.icon.setImageBitmap (relateitemBmps[position]); 
				}
			}
		  
			return convertView;
		}

		private class ViewHolder {
			ImageView icon;			
			TextView texttitle;
			TextView textdes;
		}
	}

	private AdapterView.OnItemSelectedListener mOnSelectedListener = new AdapterView.OnItemSelectedListener()
	{
		public void onItemSelected(AdapterView<?> parent, View v, int position, long id)
		{
			ui_relatelist = (ListView) findViewById(R.id.relatelist);
			if(ui_relatelist.hasFocus() == true){
				//viewmask(1);
			}
			
			cur_select_item = position;

		}
		
		public void onNothingSelected(AdapterView<?> parent)
		{
			;
		}
	}; 		

	private void gotoPlayer(int pk, int sub_pk){
		if(pk==-1)
			return;

		Bundle bundle = new Bundle();
		bundle.putInt("itemPK", pk);
		bundle.putInt("subItemPK", sub_pk);
		
		Intent intent = new Intent();
		intent.setClass(this, ISTVVodPlayer.class);
		intent.putExtras(bundle);
		
		startActivityForResult(intent, 1);
	}

	private void gotorelateitemdetail(int pk){
		if(pk==-1)
			return;

		Bundle bundle = new Bundle();
		bundle.putInt("itemPK", pk);
		
		Intent intent = new Intent();
		intent.setClass(this, ISTVVodItemDetail.class);
		intent.putExtras(bundle);
		
		startActivityForResult(intent, 1);
	}

	private void gotorelatepage(int pk){
		if(pk==-1)
			return;

		Bundle bundle = new Bundle();
		bundle.putInt("itemPK", pk);
		
		Intent intent = new Intent();
		intent.setClass(this, ISTVVodRelateList.class);
		intent.putExtras(bundle);
		
		startActivityForResult(intent, 1);
	}	

	private void gotoitemepisodepage(int pk){
		if(pk==-1)
			return;

		Bundle bundle = new Bundle();
		bundle.putInt("itemPK", pk);
		bundle.putDouble("itemAverage", ratingaverage);
		Intent intent = new Intent();
		intent.setClass(this, ISTVVodItemEpisode.class);
		intent.putExtras(bundle);
		
		startActivityForResult(intent, 1);
	}	

	private void viewmask(int position)
	{
		if(position == 0){
			/*
			if(ui_leftmask.getVisibility() == View.VISIBLE){
				ui_leftmask.setVisibility(View.INVISIBLE);
			}

			if(ui_rightmask.getVisibility() == View.INVISIBLE){
				ui_rightmask.setVisibility(View.VISIBLE);
			}
			*/
			ui_leftmask.setVisibility(View.INVISIBLE);
			ui_rightmask.setVisibility(View.VISIBLE);
		}else if(position == 1){
			/*
			if(ui_leftmask.getVisibility() == View.INVISIBLE){
				ui_leftmask.setVisibility(View.VISIBLE);
			}

			if(ui_rightmask.getVisibility() == View.VISIBLE){
				ui_rightmask.setVisibility(View.INVISIBLE);
			}	
			*/
			ui_leftmask.setVisibility(View.VISIBLE);
			ui_rightmask.setVisibility(View.INVISIBLE);
		}

	}

	public void showBuffer(){
		bufferLayout.setVisibility(View.VISIBLE);
		bufferAnim.start();
	}

	public void hideBuffer(){
		bufferLayout.setVisibility(View.INVISIBLE);
		bufferAnim.stop();
	}	
	
	private void showRightBuffer(){
		itemrelateLayout.setVisibility(View.INVISIBLE);
		right_bufferLayout.setVisibility(View.VISIBLE);
		right_bufferAnim.start();
	}

	private void hideRightBuffer(){
		right_bufferLayout.setVisibility(View.INVISIBLE);
		itemrelateLayout.setVisibility(View.VISIBLE);
		right_bufferAnim.stop();
	}	

	@Override
	public void onPopupRateDialogClicked(float rate_value){
		int v = (int)rate_value;
		doc.rate(v);
	}

	public void showSuccess(String text) {
		if(ui_ratingbarmenubg.getVisibility() == View.GONE) {
			ui_PopupRateText.setText(text);
			ui_ratingbarmenubg.setVisibility(View.VISIBLE);
		}
	}
	
	public void hideSuccess() {
		if(ui_ratingbarmenubg.getVisibility() == View.VISIBLE) {
			ui_ratingbarmenubg.setVisibility(View.GONE);
		}
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {	
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}	
	
	public class VodReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction()==Constants.INTENT_BOOK_MARK){
				int itemPK_temp=intent.getIntExtra(Constants.ITEM_PK,-1);
				if(itemPK_temp==itemPK){
					String type=intent.getStringExtra(Constants.BOOK_TYPE);
					if(null!=type&&type.equals(Constants.BOOK_ADD)){
						ui_itembookmark.setText(getString(R.string.vod_itemremovebookmark));
					}else if(null!=type&&type.equals(Constants.BOOK_REMOVE)){
						ui_itembookmark.setText(getString(R.string.vod_itembookmark));
					}
				}
			}
		}
	}
}

