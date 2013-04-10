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
import android.view.View.OnKeyListener;
import android.content.Context;
import android.content.Intent;
import android.view.View.OnClickListener;
import android.os.Message;
import android.os.Looper;
import android.os.Handler;
import android.view.animation.AnimationUtils;
import android.graphics.drawable.AnimationDrawable;

import com.ismartv.doc.ISTVVodItemEpisodeDoc;
import com.ismartv.doc.ISTVResource;
import com.ismartv.util.Constants;

public class ISTVVodItemEpisode extends ISTVVodActivity{
	private static final String TAG = "ISTVVodItemEpisode";
	private static final int episode_uilistpageitemnum = 100;
	
	public static final int RES_BMP_ITEM_POSTERURL = 0;
	public static final int RES_STR_ITEM_TITLE = 1;
	public static final int RES_DOUBLE_ITEM_RATINGAVERAGE = 2;
	public static final int RES_INT_EPISODE_COUNT = 3;
	public static final int RES_INT_EPISODE_REALCOUNT = 4;
	public static final int RES_INT_EPISODE_SUBITEM_PK = 5;
	public static final int RES_INT_EPISODE_SUBITEM_TITLE = 6;
	public static final int RES_INT_EPISODE_CLIP_LENGTH = 7;
	public static final int RES_INT_EPISODE_OFFSET_LENGTH = 8;
	public static final int RES_STR_ITEM_ATTRS = 9;
	
	private int itemPK;
	private ISTVVodItemEpisodeDoc doc;
	private Bitmap posterurl;
	private String title;
	private String attrs;
	private double ratingaverage;
	private int episodecount;
	private int episoderealcount;
	private int episodesubitempks[];
	private String episodesubitemtitles[];
	private int episodesubitemclips[];
	private int episodesubitemoffsets[];

	/*ui control*/
	private RelativeLayout itemepisodelayout,itemepisodeNum;

	private TextView ui_itemattrs;
	private ImageView ui_itemposter;
	private TextView ui_itemtitle;
	private RatingBar ui_ratingbar;
	private TextView ui_ratingaverage;
	private TextView ui_episodeinfo;
	private TextView ui_itemexpense;
	private Button ui_btnepisodenum1;
	private Button ui_btnepisodenum2;
	private Button ui_btnepisodenum3;
	private GridView ui_episodegrid;
	private EpisodeAdapter episodeAdapter;
	private int cur_select_item = -1;
	
	private int btnlist_pagenum = 0;
	
	private int btnlist_pageitemnum = 0;
	
	private int btnlist_cur_pageindex = 0;
	
	/*if btnlist_pagenum>3, we need to manager it*/
	private int btnepisodenum1_cur_pageindex = 0;
	private int btnepisodenum2_cur_pageindex = 1;
	private int btnepisodenum3_cur_pageindex = 2;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		Bundle bundle = this.getIntent().getExtras();
		if(bundle==null){
			itemPK = 18896;
		}else{
			itemPK = bundle.getInt("itemPK");
			ratingaverage = bundle.getDouble("itemAverage");
		}

		episodecount = 0;
		episoderealcount = 0;

		if(episodesubitempks!=null){
			episodesubitempks = null;
		}

		if(episodesubitemtitles!=null){
			episodesubitemtitles = null;
		}

		if(episodesubitemclips!=null){
			episodesubitemclips = null;
		}	

		if(episodesubitemoffsets!=null){
			episodesubitemoffsets = null;
		}		

		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		ISTVVodItemEpisodeUiInit();
		
	}
	
	@Override
	protected void onResume() {
		doc = new ISTVVodItemEpisodeDoc(itemPK);
		setDoc(doc);
		super.onResume();
	}

	public void onDestroy(){
		doc.dispose();
		super.onDestroy();
	}	

	protected void onDocGotResource(ISTVResource res){
		switch(res.getType()){
			case RES_BMP_ITEM_POSTERURL:
				posterurl = res.getBitmap();
				break;
				
			case RES_STR_ITEM_TITLE:
				title = res.getString();
				Log.d(TAG, "--------------RES_STR_ITEM_TITLE="+title);
				break;
				
			case RES_STR_ITEM_ATTRS:
				attrs = res.getString();
				Log.d(TAG, "--------------RES_STR_ITEM_ATTRS="+attrs);
				break;
				
			case RES_DOUBLE_ITEM_RATINGAVERAGE:
				if(ratingaverage == 0){
					ratingaverage = res.getDouble();
				}
				break;
				
			case RES_INT_EPISODE_COUNT:
				episodecount = res.getInt();
				if(episodecount > 0){
					episodesubitempks = new int[episodecount];
					episodesubitemtitles = new String[episodecount];
					episodesubitemclips = new int[episodecount];
					episodesubitemoffsets = new int[episodecount];
				}else{
					episodesubitempks = null;
					episodesubitemtitles = null;
					episodesubitemclips = null;
					episodesubitemoffsets = null;
				}
				break;

			case RES_INT_EPISODE_REALCOUNT:
				episoderealcount = res.getInt();
				if(episoderealcount > 0) {
					episodecount = episoderealcount;
					episodesubitempks = new int[episoderealcount];
					episodesubitemtitles = new String[episoderealcount];
					episodesubitemclips = new int[episoderealcount];
					episodesubitemoffsets = new int[episoderealcount];
				}
				/*calc btnlist_pagenum*/
				btnlist_pagenum = episoderealcount/episode_uilistpageitemnum;
				if(episoderealcount%episode_uilistpageitemnum > 0){
					btnlist_pagenum = btnlist_pagenum + 1;
				}
				/*test*/
				/*btnlist_pagenum = 5;*/

				if(btnlist_cur_pageindex != (btnlist_pagenum - 1)){
					btnlist_pageitemnum = episode_uilistpageitemnum;
				}else{
					btnlist_pageitemnum = episoderealcount % episode_uilistpageitemnum;
					if(btnlist_pageitemnum == 100) {
						btnlist_pageitemnum = 0;
					}else if(btnlist_pageitemnum == 0) {
						btnlist_pageitemnum = 100;
					}
				}
				Log.d(TAG, "btnlist_pageitemnum="+btnlist_pageitemnum+",btnlist_cur_pageindex="+btnlist_cur_pageindex+",btnlist_pagenum="+btnlist_pagenum);
				Log.d(TAG, "episoderealcount="+episoderealcount+",episode_uilistpageitemnum="+episode_uilistpageitemnum);
				break;					
				
			case RES_INT_EPISODE_SUBITEM_PK:
				if(episodesubitempks!=null){
					episodesubitempks[res.getID()] = res.getInt();
				}
				break;

			case RES_INT_EPISODE_SUBITEM_TITLE:
				if(episodesubitemtitles!=null){
					episodesubitemtitles[res.getID()] = res.getString();
				}
				break;

			case RES_INT_EPISODE_CLIP_LENGTH:
				if(episodesubitemclips!=null){
					episodesubitemclips[res.getID()] = res.getInt() * 1000;
				}
				break;	

			case RES_INT_EPISODE_OFFSET_LENGTH:
				if(episodesubitemoffsets!=null){
					Log.d(TAG, "res.getID()="+res.getID()+",res.getInt()="+res.getInt());
					episodesubitemoffsets[res.getID()] = res.getInt() * 1000;
				}
				break;
				
			default:
				break;
		}
	}

	protected void onDocUpdate(){
		/*Redraw the activity*/
		ISTVVodItemEpisodeUiSetPoster();
		ISTVVodItemEpisodeUiSetTitle();
		ISTVVodItemEpisodeUiSetAttrs();
		ISTVVodItemEpisodeUiSetRatingAverage();
		ISTVVodItemEpisodeUiSetInfo(0);
		ISTVVodItemEpisodeUiSetExpense();
		ISTVVodItemEpisodeUiSetNum();
		ISTVVodItemEpisodeUiSetBtnList();
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		Log.d(TAG, "-------dispatchKeyEvent------event.key="+event.getKeyCode());
		return super.dispatchKeyEvent(event);
	}

	private void ISTVVodItemEpisodeUiInit(){
		setContentView(R.layout.vod_itemepisode);
		rootView=this.findViewById(R.id.ItemEpisodeLayout);
		
		itemepisodelayout = (RelativeLayout)findViewById(R.id.ItemEpisodeLayout);
		itemepisodelayout.setVisibility(View.VISIBLE);

		ui_itemattrs = (TextView)findViewById(R.id.itemattrs);
		ui_itemposter = (ImageView)findViewById(R.id.itemposter);		
		ui_itemtitle = (TextView)findViewById(R.id.itemtitle);
		ui_ratingbar = (RatingBar)findViewById(R.id.ratingBar);
		ui_ratingaverage = (TextView)findViewById(R.id.ratingaverage);
		ui_episodeinfo = (TextView)findViewById(R.id.itemepisodeinfo);
		ui_itemexpense = (TextView)findViewById(R.id.itemexpense);
		itemepisodeNum=(RelativeLayout)findViewById(R.id.itemepisodeNum);

		ui_btnepisodenum1 = (Button)findViewById(R.id.itemepisodenum1);
		ui_btnepisodenum1.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					btnlist_cur_pageindex = btnepisodenum1_cur_pageindex;

					Log.d(TAG, " btnepisodenum1_cur_pageindex = " + btnepisodenum1_cur_pageindex);

					if(btnlist_cur_pageindex != (btnlist_pagenum - 1)){
						btnlist_pageitemnum = episode_uilistpageitemnum;
					}else{
						btnlist_pageitemnum = episoderealcount % episode_uilistpageitemnum;
					}			

					ISTVVodItemEpisodeUiSetBtnList();
				}
				});
		ui_btnepisodenum1.setOnFocusChangeListener(new Button.OnFocusChangeListener(){

			public void onFocusChange(View v, boolean hasFocus) {

				// TODO Auto-generated method stub
				
				if(hasFocus == true) {
					ui_btnepisodenum1.setTextColor(getResources().getColor(R.color.vod_itemdetail_firsttextcolor));
				}else{
					ui_btnepisodenum1.setTextColor(getResources().getColor(R.color.vod_itemdetail_secondtextcolor));
				}
			}
		});
		ui_btnepisodenum1.setOnKeyListener(new OnKeyListener(){

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				Log.d(TAG, "OnKey: v = " + v + "id = " + v.getId()
				+ ",keyCode = " + keyCode+";btnlist_pagenum="+btnlist_pagenum);
				if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
					if(btnepisodenum1_cur_pageindex==0){
						return true;
					}
				}
				if ((keyCode == KeyEvent.KEYCODE_DPAD_LEFT) && (btnlist_pagenum > 3)){
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						if(btnepisodenum1_cur_pageindex > 0){
							btnepisodenum1_cur_pageindex--;
							btnepisodenum2_cur_pageindex--;
							btnepisodenum3_cur_pageindex--;
							
							Log.d(TAG, " btnlist_pagenum = " + btnlist_pagenum
											+ " btnepisodenum1_cur_pageindex = " + btnepisodenum1_cur_pageindex 
											+ " btnepisodenum2_cur_pageindex = " + btnepisodenum2_cur_pageindex
											+ " btnepisodenum3_cur_pageindex = " + btnepisodenum3_cur_pageindex);
							
							ISTVVodItemEpisodeUiSetNum();
							return true;
						}
					}
				}
				return false;
			}

		});

		ui_btnepisodenum2 = (Button)findViewById(R.id.itemepisodenum2);
		ui_btnepisodenum2.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					btnlist_cur_pageindex = btnepisodenum2_cur_pageindex;

					Log.d(TAG, " btnepisodenum2_cur_pageindex = " + btnepisodenum2_cur_pageindex);

					if(btnlist_cur_pageindex != (btnlist_pagenum - 1)){
						btnlist_pageitemnum = episode_uilistpageitemnum;
					}else{
						btnlist_pageitemnum = episoderealcount % episode_uilistpageitemnum;
					}					

					ISTVVodItemEpisodeUiSetBtnList();
					
				}
				});
		ui_btnepisodenum2.setOnFocusChangeListener(new Button.OnFocusChangeListener(){

			public void onFocusChange(View v, boolean hasFocus) {

				// TODO Auto-generated method stub
				
				if(hasFocus == true) {
					ui_btnepisodenum2.setTextColor(getResources().getColor(R.color.vod_itemdetail_firsttextcolor));
				}else{
					ui_btnepisodenum2.setTextColor(getResources().getColor(R.color.vod_itemdetail_secondtextcolor));
				}
			}
		});	
		ui_btnepisodenum2.setOnKeyListener(new OnKeyListener(){
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				Log.d(TAG, "-ui_btnepisodenum2---onKey-----btnlist_pagenum="+btnlist_pagenum+";keyCode="+keyCode);
				if(keyCode==KeyEvent.KEYCODE_DPAD_RIGHT){
					if(btnlist_pagenum==2){
						return true;
					}
				}
				Log.d(TAG, "---------page----");
				return false;
			}
		});
		

		ui_btnepisodenum3 = (Button)findViewById(R.id.itemepisodenum3);
		ui_btnepisodenum3.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					btnlist_cur_pageindex = btnepisodenum3_cur_pageindex;
					
					Log.d(TAG, " btnepisodenum3_cur_pageindex = " + btnepisodenum3_cur_pageindex);

					if(btnlist_cur_pageindex != (btnlist_pagenum - 1)){
						btnlist_pageitemnum = episode_uilistpageitemnum;
					}else{
						btnlist_pageitemnum = episoderealcount % episode_uilistpageitemnum;
					}					
					
					ISTVVodItemEpisodeUiSetBtnList();					
				}
				});
		ui_btnepisodenum3.setOnFocusChangeListener(new Button.OnFocusChangeListener(){

			public void onFocusChange(View v, boolean hasFocus) {

				// TODO Auto-generated method stub
				
				if(hasFocus == true) {
					ui_btnepisodenum3.setTextColor(getResources().getColor(R.color.vod_itemdetail_firsttextcolor));
				}else{
					ui_btnepisodenum3.setTextColor(getResources().getColor(R.color.vod_itemdetail_secondtextcolor));
				}
			}
		});	
		ui_btnepisodenum3.setOnKeyListener(new OnKeyListener(){

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				Log.d(TAG, "OnKey: v = " + v + "id = " + v.getId()
				+ ",keyCode = " + keyCode+";btnlist_pagenum="+btnlist_pagenum);
				if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
					if(btnepisodenum3_cur_pageindex==btnlist_pagenum-1){
						return true;
					}
				}
				if ((keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) && (btnlist_pagenum > 3)){
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						if(btnepisodenum3_cur_pageindex < (btnlist_pagenum - 1)){
							btnepisodenum1_cur_pageindex++;
							btnepisodenum2_cur_pageindex++;
							btnepisodenum3_cur_pageindex++;
							Log.d(TAG, " btnlist_pagenum = " + btnlist_pagenum
											+ " btnepisodenum1_cur_pageindex = " + btnepisodenum1_cur_pageindex 
											+ " btnepisodenum2_cur_pageindex = " + btnepisodenum2_cur_pageindex
											+ " btnepisodenum3_cur_pageindex = " + btnepisodenum3_cur_pageindex);


							ISTVVodItemEpisodeUiSetNum();
							return true;
						}
					}
				}
				return false;
			}

		});
		

		episodeAdapter = new EpisodeAdapter(this);

		ui_episodegrid = (GridView) findViewById(R.id.itempoisode_gridview);
		ui_episodegrid.setAdapter(episodeAdapter);

		ui_episodegrid.setOnItemSelectedListener(mOnSelectedListener);

		ui_episodegrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			  	int index= btnlist_cur_pageindex * episode_uilistpageitemnum + position;
				Log.d(TAG, " ui_episodegrid = " + index+";episoderealcount="+episoderealcount+";episodesubitempks.length="+episodesubitempks.length);
				if(index < episoderealcount){
			  		//gotoPlayer(itemPK, episodesubitempks[index]);
				}
			  }
		});	
		ui_episodegrid.setOnKeyListener(new OnKeyListener(){
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(event.getAction() == KeyEvent.ACTION_DOWN&&(event.getKeyCode()==KeyEvent.KEYCODE_ENTER||event.getKeyCode()==KeyEvent.KEYCODE_DPAD_CENTER)){
					int index= btnlist_cur_pageindex * episode_uilistpageitemnum + cur_select_item;
					Log.d(TAG, "--setOnKeyListener-------cur_select_item="+cur_select_item+";index="+index+";episoderealcount="+episoderealcount);
					if(index < episoderealcount){
				  		gotoPlayer(itemPK, episodesubitempks[index]);
					}
				}
				return false;
			}
		});

		ui_episodegrid.setOnFocusChangeListener(new ListView.OnFocusChangeListener(){

			public void onFocusChange(View v, boolean hasFocus) {

				// TODO Auto-generated method stub
				Log.d(TAG, "------onFocusChange----------");

				if(hasFocus == true) {
					
				}else{
				}
			}
		});				

	}

	private void ISTVVodItemEpisodeUiDeinit(){
		
	}

	private void ISTVVodItemEpisodeUiSetPoster(){
		if(posterurl != null){
			ui_itemposter.setImageBitmap(posterurl);
		}
	}

	private void ISTVVodItemEpisodeUiSetTitle(){
		if(title != null){
			ui_itemtitle.setText(title);
			ui_itemtitle.setSelected(true);
		}
		//setTextColor
		//setTextSize
	}
	
	private void ISTVVodItemEpisodeUiSetAttrs(){
		Log.d(TAG,"----ISTVVodItemEpisodeUiSetAttrs");
		if(attrs != null){
			ui_itemattrs.setText(attrs);
		}
	}

	private void ISTVVodItemEpisodeUiSetRatingAverage(){
		float tmp_ratingaverage = (float)ratingaverage;

		Log.d(TAG, "ratingaverage" + tmp_ratingaverage);

		ui_ratingbar.setRating(tmp_ratingaverage);

		String str_tmp = new Double(ratingaverage).toString();

		ui_ratingaverage.setText(str_tmp.substring(0, 3));
	}

	private void ISTVVodItemEpisodeUiSetInfo(int index){
		String tmp_str = getResources().getString(R.string.vod_episode_count);
		
		if(episodesubitemtitles!=null)
//			ui_episodeinfo.setText(episodecount + tmp_str + "/" + episodesubitemtitles[index + btnlist_cur_pageindex * episode_uilistpageitemnum]);
			ui_episodeinfo.setText(episodecount + tmp_str );
	}


	private void ISTVVodItemEpisodeUiSetExpense(){
		ui_itemexpense.setText(R.string.vod_itemdexpense);
	}

	private void ISTVVodItemEpisodeUiSetNum(){
		String tmp_str = getResources().getString(R.string.vod_episode);
		
		if(btnlist_pagenum == 1){
			itemepisodeNum.setVisibility(View.GONE);
			ui_btnepisodenum1.setVisibility(View.GONE);
			ui_btnepisodenum2.setVisibility(View.GONE);
			ui_btnepisodenum3.setVisibility(View.GONE);
		}else if(btnlist_pagenum == 2){
			itemepisodeNum.setVisibility(View.VISIBLE);
			ui_btnepisodenum1.setVisibility(View.VISIBLE);
			ui_btnepisodenum2.setVisibility(View.VISIBLE);
			ui_btnepisodenum3.setVisibility(View.GONE);

			ui_btnepisodenum1.setText(1 + "-" + 100 + tmp_str);
			ui_btnepisodenum2.setText(101 + "-" + 200 + tmp_str);
		}else if(btnlist_pagenum == 3){
			itemepisodeNum.setVisibility(View.VISIBLE);
			ui_btnepisodenum1.setVisibility(View.VISIBLE);
			ui_btnepisodenum2.setVisibility(View.VISIBLE);
			ui_btnepisodenum3.setVisibility(View.VISIBLE);

			ui_btnepisodenum1.setText(1 + "-" + 100 + tmp_str);
			ui_btnepisodenum2.setText(101 + "-" + 200 + tmp_str);
			ui_btnepisodenum3.setText(201 + "-" + 300 + tmp_str);
		}else if(btnlist_pagenum > 3){
			itemepisodeNum.setVisibility(View.VISIBLE);
			ui_btnepisodenum1.setVisibility(View.VISIBLE);
			ui_btnepisodenum2.setVisibility(View.VISIBLE);
			ui_btnepisodenum3.setVisibility(View.VISIBLE);

			int episode_range1 = 0, episode_range2 = 0; 

			Log.d(TAG, "ISTVVodItemEpisodeUiSetNum "
							+ " btnlist_pagenum = " + btnlist_pagenum
							+ " btnepisodenum1_cur_pageindex = " + btnepisodenum1_cur_pageindex 
							+ " btnepisodenum2_cur_pageindex = " + btnepisodenum2_cur_pageindex
							+ " btnepisodenum3_cur_pageindex = " + btnepisodenum3_cur_pageindex);
			
			episode_range1 = btnepisodenum1_cur_pageindex * 100 + 1;
			episode_range2 = btnepisodenum1_cur_pageindex * 100 + 100;
			
			ui_btnepisodenum1.setText(episode_range1 + "-" + episode_range2 + tmp_str);

			episode_range1 = btnepisodenum2_cur_pageindex * 100 + 1;
			episode_range2 = btnepisodenum2_cur_pageindex * 100 + 100;
			
			ui_btnepisodenum2.setText(episode_range1 + "-" + episode_range2 + tmp_str);

			episode_range1 = btnepisodenum3_cur_pageindex * 100 + 1;
			episode_range2 = btnepisodenum3_cur_pageindex * 100 + 100;
			
			ui_btnepisodenum3.setText(episode_range1 + "-" + episode_range2 + tmp_str);
		}
		

	}	

	private void ISTVVodItemEpisodeUiSetBtnList(){
		episodeAdapter.notifyDataSetChanged();
	}	

	private class EpisodeAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private int selectItem;
		
		public EpisodeAdapter(Context context) {
			super();
			mInflater=LayoutInflater.from(context);				
		}

		public int getCount() {
			return btnlist_pageitemnum;
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
				convertView = mInflater.inflate(R.layout.vod_itemepisodebtn, null);
			  
				holder = new ViewHolder();
				holder.episodebtn = (TextView) convertView.findViewById(R.id.itemepisodebtn);
				convertView.setTag(holder);
			}else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}
			
			// Bind the data efficiently with the holder.
			int index= btnlist_cur_pageindex * episode_uilistpageitemnum + position;

			if(index < episoderealcount){
				int spisodenum = index + 1;;
				
				String str_tmp = new Integer(spisodenum).toString();
				holder.episodebtn.setText(str_tmp);

				if(episodesubitemoffsets[index] < 0){
				//	holder.episodebtn.setBackgroundResource(R.drawable.vod_episodepast);
						holder.episodebtn.setBackgroundResource(R.drawable.vod_episodefuture);
						holder.episodebtn.setTextColor(0xff000000);				
				}else if(episodesubitemoffsets[index] == 0){
					holder.episodebtn.setBackgroundResource(R.drawable.vod_episodefuture);
				}else if(episodesubitemoffsets[index] < episodesubitemclips[index]){
					int percent = episodesubitemoffsets[index] * 100/episodesubitemclips[index];					
					if((percent >= 0) && (percent < 25)){
						holder.episodebtn.setBackgroundResource(R.drawable.vod_episodeoffset_percent20);
					}else if((percent >= 25) && (percent < 50)){
						holder.episodebtn.setBackgroundResource(R.drawable.vod_episodeoffset_percent40);
					}else if((percent >= 50) && (percent < 75)){
						holder.episodebtn.setBackgroundResource(R.drawable.vod_episodeoffset_percent60);
					}else if((percent >= 75) && (percent < 100)){
						holder.episodebtn.setBackgroundResource(R.drawable.vod_episodeoffset_percent80);
					}else if(percent >= 100){
						holder.episodebtn.setBackgroundResource(R.drawable.vod_episodefuture);
						holder.episodebtn.setTextColor(0xff000000);
					}
				}
			}	
		  
			return convertView;
		}

		private class ViewHolder {
			TextView episodebtn;
		}		
	}

	private AdapterView.OnItemSelectedListener mOnSelectedListener = new AdapterView.OnItemSelectedListener()
	{
		public void onItemSelected(AdapterView<?> parent, View v, int position, long id)
		{
			ui_episodegrid = (GridView) findViewById(R.id.itempoisode_gridview);
/*			
			if(ui_episodegrid.hasFocus() == true){
			}
*/			
			cur_select_item = position;
			Log.d(TAG, "-----------cur_select_item="+cur_select_item);
			ISTVVodItemEpisodeUiSetInfo(position);
		}
		
		public void onNothingSelected(AdapterView<?> parent)
		{
			;
		}
	}; 			

	private void gotoPlayer(int pk, int sub_pk){
		Log.d(TAG, "----gotoPlayer----pk="+pk+";sub_pk="+sub_pk);
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, ">>>>>>>>>>>>>>>>onActivityResult-resultCode="+resultCode);
		if(resultCode== Constants.CODE_PLAYER_TO_EPISODE){
			int tempItemPk=data.getIntExtra("itemPK", -1);
			int tempSubItemPK=data.getIntExtra("subItemPK", -1);
			Log.d(TAG, "--tempItemPk="+tempItemPk+";tempSubItemPK="+tempSubItemPK);
			
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}

