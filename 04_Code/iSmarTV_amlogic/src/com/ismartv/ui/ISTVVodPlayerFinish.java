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
import android.content.Context;
import android.content.Intent;
import android.view.View.OnClickListener;
import android.os.Message;
import android.os.Looper;
import android.os.Handler;
import android.view.animation.AnimationUtils;
import android.graphics.drawable.AnimationDrawable;

import com.ismartv.doc.ISTVVodPlayerFinishDoc;
import com.ismartv.doc.ISTVResource;

public class ISTVVodPlayerFinish extends ISTVVodActivity{
	private static final String TAG = "ISTVVodPlayerFinish";
	private static final int relateitemnum = 9;
	
	public static final int RES_STR_ITEM_TITLE = 0;
	public static final int RES_DOUBLE_ITEM_RATINGAVERAGE = 1;
	public static final int RES_BMP_ITEM_POSTERURL = 2;
	public static final int RES_INT_RELATEITEML_COUNT = 3;
	public static final int RES_STR_RELATEITEML_TITLE = 4;
	public static final int RES_STR_RELATEITEML_DESCRIPTION = 5;
	public static final int RES_BMP_RELATEITEML = 6;
	public static final int RES_INT_RELATEITEML_ITEMPK = 7;
	public static final int RES_INT_EPISODE_COUNT = 8;
	public static final int RES_INT_EPISODE_REALCOUNT = 9;
	public static final int RES_INT_EPISODE_SUBITEM_PK = 10;		
	public static final int RES_BOOL_BOOKMARKED = 11;
	public static final int RES_BOOL_RELATEITEML_ISCOMPLEX = 12;
	public static final int RES_BOOL_RATINGAVERAGE_ISSUCCESS = 13;
	public static final int RES_BOOL_BOOKMARK_ADD_ISSUCCESS = 14;
	public static final int RES_BOOL_BOOKMARK_REMOVED_ISSUCCESS = 15;	

	//test, 18896, 52312, 18968, 18852, 18859, 49585, 49568, 19370, 49572, 19623, 53699
	private int itemPK;
	private ISTVVodPlayerFinishDoc doc;
	private String title;
	private double ratingaverage;
	private Bitmap posterurl;
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
	
	/*ui control*/
	private RelativeLayout playerfinishlayout;

	
	private TextView ui_itemtitle;
	private RatingBar ui_ratingbar;
	private TextView ui_ratingaverage;
	private ImageView ui_itemposter;
	private Button ui_itemreplay;
	private Button ui_itemratingaverage;
	private Button ui_itembookmark;
	private TextView ui_relatetitle;
	private GridView ui_relategrid;
	private RelateAdapter relateAdapter;
	private int cur_select_item = -1;

	private ImageView ui_leftmask;
	private ImageView ui_rightmask;

	private LinearLayout itemrelateLayout;
	private LinearLayout bufferLayout;
	private AnimationDrawable bufferAnim;

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

		ISTVVodPlayerFinishUiInit();
		
		doc = new ISTVVodPlayerFinishDoc(itemPK);
		setDoc(doc);

		handler = new Handler(){
			public void handleMessage(Message msg) {
				if(msg.what==1984){
					hideBuffer();	
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
	}

	public void onDestroy(){
		loadTimer.cancel();
		doc.dispose();
		super.onDestroy();
	}	

	protected void onDocGotResource(ISTVResource res){
		String text;
		switch(res.getType()){
			case RES_STR_ITEM_TITLE:
				title = res.getString();
				break;
				
			case RES_DOUBLE_ITEM_RATINGAVERAGE:
				ratingaverage = res.getDouble();
				break;
				
			case RES_BMP_ITEM_POSTERURL:
				posterurl = res.getBitmap();
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
						hideBuffer();
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
				break;

			case RES_INT_EPISODE_REALCOUNT:
				episoderealcount = res.getInt();
				break;				
				
			case RES_INT_EPISODE_SUBITEM_PK:
				if(episodesubitempks!=null){
					if(res.getID()<episodesubitempks.length){
						episodesubitempks[res.getID()] = res.getInt();
					}else{
						Log.d(TAG, "----error----episodesubitempks.length="+episodesubitempks.length+";res.getID()="+res.getID());
					}
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
				successHandler.removeCallbacks(runnable);
				successHandler.postDelayed(runnable, 1000);				
				break;				
			default:
				break;
		}
	}

	protected void onDocUpdate(){
		/*Redraw the activity*/
		ISTVVodPlayerFinishUiSetTitle();
		ISTVVodPlayerFinishUiSetRatingAverage();
		ISTVVodPlayerFinishUiSetPoster();
		ISTVVodPlayerFinishUiSetPlay();
		ISTVVodPlayerFinishUiSetRatingaverage();
		ISTVVodPlayerFinishUiSetBookmark();
		ISTVVodPlayerFinishUiSetRelate();
	}

	private void ISTVVodPlayerFinishUiInit(){
		setContentView(R.layout.vod_playerfinish);
		rootView=this.findViewById(R.id.PlayerFinishLayout);

		ui_ratingbarmenubg = (RelativeLayout)findViewById(R.id.successlayout);
		ui_PopupRateText = (TextView)findViewById(R.id.PopupRateText);
		
		playerfinishlayout = (RelativeLayout)findViewById(R.id.PlayerFinishLayout);
		playerfinishlayout.setVisibility(View.VISIBLE);
		
		ui_itemtitle = (TextView)findViewById(R.id.itemtitle);
		ui_ratingbar = (RatingBar)findViewById(R.id.ratingBar);
		ui_ratingaverage = (TextView)findViewById(R.id.ratingaverage);
		ui_itemposter = (ImageView)findViewById(R.id.itemposter);
		
		ui_itemreplay = (Button)findViewById(R.id.itemreplay);
		ui_itemreplay.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					if(episodesubitempks!=null){
						gotoPlayer(itemPK, episodesubitempks[0]);
					}else{
						gotoPlayer(itemPK, -1);
					}
				}
				});
		ui_itemreplay.setOnFocusChangeListener(new Button.OnFocusChangeListener(){

			public void onFocusChange(View v, boolean hasFocus) {

				// TODO Auto-generated method stub

				if(hasFocus == true) {
					ui_itemreplay.setTextColor(getResources().getColor(R.color.vod_itemdetail_thirdtextcolor));
					
					viewmask(0);
				}else{
					ui_itemreplay.setTextColor(getResources().getColor(R.color.vod_itemdetail_firsttextcolor));
				}
			}
		});

		
		ui_itemratingaverage = (Button)findViewById(R.id.itemratingaverage);
		ui_itemratingaverage.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					showPopupRateDialog(ratingaverage);
				}
				});
		ui_itemratingaverage.setOnFocusChangeListener(new Button.OnFocusChangeListener(){

			public void onFocusChange(View v, boolean hasFocus) {

				// TODO Auto-generated method stub

				if(hasFocus == true) { 
					ui_itemratingaverage.setTextColor(getResources().getColor(R.color.vod_onselect));
					viewmask(0);
					
				}else{
					ui_itemratingaverage.setTextColor(getResources().getColor(R.color.vod_unselect));
				}
				
			}
		});		
		
		ui_itembookmark = (Button)findViewById(R.id.itembookmark);
		ui_itembookmark.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					if(isNeedLogin()){
						showPopupLoginDialog(DIALOG_LOGIN);
					}else{
						if(clipBookmarked){
							doc.removeBookmark();
						}else{
							doc.addBookmark();
						}
						clipBookmarked = !clipBookmarked;
						
						ISTVVodPlayerFinishUiSetBookmark();
					}
				}
				});
		ui_itembookmark.setOnFocusChangeListener(new Button.OnFocusChangeListener(){

			public void onFocusChange(View v, boolean hasFocus) {

				// TODO Auto-generated method stub

				if(hasFocus == true) { 
					ui_itembookmark.setTextColor(getResources().getColor(R.color.vod_onselect));
					viewmask(0);
					
				}else{
					ui_itembookmark.setTextColor(getResources().getColor(R.color.vod_unselect));
				}
			}
		});			
		
		ui_relatetitle = (TextView)findViewById(R.id.relatetitle);

		relateAdapter = new RelateAdapter(this);

		ui_relategrid = (GridView) findViewById(R.id.relate_gridview);
		ui_relategrid.setAdapter(relateAdapter);

		ui_relategrid.setOnItemSelectedListener(mOnSelectedListener);

		ui_relategrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			  	if(position < relateitemsize){
					if(relateIsComplex[position]){
			 			gotorelateitemdetail(relateitempks[position]);
					}else{
						if(episodesubitempks!=null){
							gotoPlayer(relateitempks[position], episodesubitempks[0]);
						}else{
							gotoPlayer(relateitempks[position], -1);
						}
					}
				}
			  }
		});	

		ui_relategrid.setOnFocusChangeListener(new ListView.OnFocusChangeListener(){

			public void onFocusChange(View v, boolean hasFocus) {

				// TODO Auto-generated method stub

				if(hasFocus == true) {
					
					viewmask(1);
				}else{
					//ui_relategrid.setSelection(-1);
				}
			}
		});				

		ui_leftmask = (ImageView)findViewById(R.id.leftmask);
		ui_rightmask= (ImageView)findViewById(R.id.rightmask);	

			
		itemrelateLayout = (LinearLayout)findViewById(R.id.ItemRelateLayout);
		bufferLayout = (LinearLayout)findViewById(R.id.BufferLayout);
		bufferAnim = (AnimationDrawable)((ImageView)findViewById(R.id.BufferImage)).getBackground();
		showBuffer();
	}

	private void ISTVVodPlayerFinishUiDeinit(){
		
	}

	private void ISTVVodPlayerFinishUiSetTitle(){
		if(title != null){
			ui_itemtitle.setText(title);
			ui_itemtitle.setSelected(true);
		}
		//setTextColor
		//setTextSize
	}

	private void ISTVVodPlayerFinishUiSetRatingAverage(){
		float tmp_ratingaverage = (float)ratingaverage;

		Log.d(TAG, "ratingaverage" + tmp_ratingaverage);

		ui_ratingbar.setRating(tmp_ratingaverage);

		String str_tmp = new Double(ratingaverage).toString();

		ui_ratingaverage.setText(str_tmp.substring(0, 3));
	}

	private void ISTVVodPlayerFinishUiSetPoster(){
		if(posterurl != null){
			ui_itemposter.setImageBitmap(posterurl);
		}
	}

	private void ISTVVodPlayerFinishUiSetPlay(){
		ui_itemreplay.setText(R.string.vod_playerfinish_replay);
	}

	private void ISTVVodPlayerFinishUiSetRatingaverage(){
		ui_itemratingaverage.setText(R.string.vod_itemratingaverage);
	}

	private void ISTVVodPlayerFinishUiSetBookmark(){
		ui_itembookmark.setText(getString(!clipBookmarked?R.string.vod_itembookmark:R.string.vod_itemremovebookmark));
	}	

	private void ISTVVodPlayerFinishUiSetRelate(){
		ui_relatetitle.setText(R.string.vod_itemrelatetitle);
		relateAdapter.notifyDataSetChanged();
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

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.vod_playerfinishrelategrid, null);
			  
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
			ui_relategrid = (GridView) findViewById(R.id.relate_gridview);
			if(ui_relategrid.hasFocus() == true){
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
		if(pk==-1){
			return;
		}
		Bundle bundle = new Bundle();
		bundle.putInt("itemPK", pk);
		bundle.putInt("subItemPK", sub_pk);
		
		Intent intent = new Intent();
		intent.setClass(this, ISTVVodPlayer.class);
		intent.putExtras(bundle);
		startActivity(intent);
		finish();
	}

	private void gotorelateitemdetail(int pk){
		if(pk==-1){
			return;
		}
		Bundle bundle = new Bundle();
		bundle.putInt("itemPK", pk);
		
		Intent intent = new Intent();
		intent.setClass(this, ISTVVodItemDetail.class);
		intent.putExtras(bundle);
		startActivity(intent);
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
		itemrelateLayout.setVisibility(View.INVISIBLE);
		bufferLayout.setVisibility(View.VISIBLE);
		bufferAnim.start();
	}

	public void hideBuffer(){
		bufferLayout.setVisibility(View.INVISIBLE);
		itemrelateLayout.setVisibility(View.VISIBLE);
		bufferAnim.stop();
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
	
}

