package com.joysee.adtv.ui;

import java.util.ArrayList;
import java.util.LinkedList;

import javax.crypto.spec.IvParameterSpec;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DateFormatUtil;
import com.joysee.adtv.common.DvbLog;
import com.joysee.adtv.common.DefaultParameter.ViewMessage;
import com.joysee.adtv.common.DvbMessage;
import com.joysee.adtv.common.ThreadPoolManager;
import com.joysee.adtv.controller.ViewController;
import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.adtv.logic.bean.MiniEpgNotify;
import com.joysee.adtv.logic.bean.NETEventInfo;

/**
 * epg面板
 * 
 * @author xubin
 * 
 */
public class MiniEpgPanel extends RelativeLayout implements IDvbBaseView {

	LinearLayout panel;
	LinearLayout panel_epglist;

	private TextView mNowTime, mVoice_model1, mVoice_model2;
	private ImageView mSoundEffect;// 声效
	private ImageView mAd;// 广告
	private ViewController mViewController;

	private String[] language_items = null;
	private String[] soundtrack_items = null;

	public boolean isVisibility = false; // 是否显示
	public static final int HIDE_EPG_TIME = 5000;
	public static final int REFRESH_EPG_INFO = 9;
	public static final int HIDE_EPG = 10;
	public static final int REFLESH_PANEL = 11;
	public static final int SHOW_EPG = 12;
	public static final int KEY_DOWN = 13;
	public static final int KEY_UP = 14;
	private static final String TAG = "MiniEpgPanel";
	private ArrayList<NETEventInfo[]> mInfoQueens = new ArrayList<NETEventInfo[]>();
	private ArrayList<DvbService> mDvbServiceQuenes = new ArrayList<DvbService>();
	private SparseArray<AsyncTask> mTasks = new SparseArray<AsyncTask>();
	private SparseArray<NETEventInfo[]> mNetEventCache = new SparseArray<NETEventInfo[]>();
	private ArrayList<Integer> mEventFromTS = new ArrayList<Integer>();
	private SparseArray<DvbService> mDvbServiceCache = new SparseArray<DvbService>();
	private int currentShowPosition = -1;

	private FolderViewHolder viewHolder;
	LayoutInflater inflater;
	Context mContext;
	private boolean isSwitchChannel;

	public MiniEpgPanel(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}

	public MiniEpgPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public MiniEpgPanel(Context context) {
		super(context);
		mContext = context;
	}

	protected class FolderViewHolder {
		public TextView channel_number_top_a;
		public TextView channel_name_top_a;
		public TextView channel_content_time_top_a;
		public TextView channel_content_top_a;
		public TextView channel_content_episode_top_a;

		public TextView channel_number_top_b;
		public TextView channel_name_top_b;
		public TextView channel_content_time_top_b;
		public TextView channel_content_top_b;
		public TextView channel_content_episode_top_b;

		public TextView channel_number_cen;
		public TextView channel_name_cen;
		public TextView channel_content_time_cen;
		public TextView channel_content_cen;
		public TextView channel_content_episode_cen;
		public ProgressBar channel_progressbar_cen;
		public TextView next_channel_time_cen;
		public TextView next_channel_content_cen;
		public TextView next_channel_content_episode_cen;

		public TextView channel_number_bottom;
		public TextView channel_name_bottom;
		public TextView channel_content_time_bottom;
		public TextView channel_content_bottom;
		public TextView channel_content_episode_bottom;
	}

	protected void onFinishInflate() {

		mNowTime = (TextView) findViewById(R.id.time);
		mVoice_model1 = (TextView) findViewById(R.id.stereo_sound);
		mVoice_model2 = (TextView) findViewById(R.id.sound);// 伴音
		mSoundEffect = (ImageView) findViewById(R.id.dubi);
		mAd = (ImageView) findViewById(R.id.advertisement_s);

		soundtrack_items = getResources().getStringArray(R.array.soundtrack_items);
		language_items = getResources().getStringArray(R.array.language_items);
		panel_epglist = (LinearLayout) findViewById(R.id.epg_info_list);

		viewHolder = new FolderViewHolder();
		viewHolder.channel_number_top_a = (TextView) findViewById(R.id.channel_number_top_a);
		viewHolder.channel_name_top_a = (TextView) findViewById(R.id.channel_name_top_a);
		viewHolder.channel_content_time_top_a = (TextView) findViewById(R.id.channel_content_time_top_a);
		viewHolder.channel_content_top_a = (TextView) findViewById(R.id.channel_content_top_a);
		viewHolder.channel_content_episode_top_a = (TextView) findViewById(R.id.channel_content_episode_top_a);

		viewHolder.channel_number_top_b = (TextView) findViewById(R.id.channel_number_top_b);
		viewHolder.channel_name_top_b = (TextView) findViewById(R.id.channel_name_top_b);
		viewHolder.channel_content_time_top_b = (TextView) findViewById(R.id.channel_content_time_top_b);
		viewHolder.channel_content_top_b = (TextView) findViewById(R.id.channel_content_top_b);
		viewHolder.channel_content_episode_top_b = (TextView) findViewById(R.id.channel_content_episode_top_b);

		viewHolder.channel_number_cen = (TextView) findViewById(R.id.channel_number_cen);
		viewHolder.channel_name_cen = (TextView) findViewById(R.id.channel_name_cen);
		viewHolder.channel_content_time_cen = (TextView) findViewById(R.id.channel_content_time_cen);
		viewHolder.channel_content_cen = (TextView) findViewById(R.id.channel_content_cen);
		viewHolder.channel_content_episode_cen = (TextView) findViewById(R.id.channel_content_episode_cen);
		viewHolder.channel_progressbar_cen = (ProgressBar) findViewById(R.id.channel_progressbar);
		viewHolder.next_channel_time_cen = (TextView) findViewById(R.id.next_channel_time_cen);
		viewHolder.next_channel_content_cen = (TextView) findViewById(R.id.next_channel_content_cen);
		viewHolder.next_channel_content_episode_cen = (TextView) findViewById(R.id.next_channel_content_episode_cen);

		viewHolder.channel_number_bottom = (TextView) findViewById(R.id.channel_number_bottom);
		viewHolder.channel_name_bottom = (TextView) findViewById(R.id.channel_name_bottom);
		viewHolder.channel_content_time_bottom = (TextView) findViewById(R.id.channel_content_time_bottom);
		viewHolder.channel_content_bottom = (TextView) findViewById(R.id.channel_content_bottom);
		viewHolder.channel_content_episode_bottom = (TextView) findViewById(R.id.channel_content_episode_bottom);

		initAd();
	}

	
	Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case REFRESH_EPG_INFO:
				NETEventInfo[] result = (NETEventInfo[]) msg.obj;
				mInfoQueens.add(result);
				if(mInfoQueens.size() == 4){
					refreshChannlePF();
				}
				break;
			case HIDE_EPG:
				hideEpg();
				mInfoQueens.clear();
				break;
			case REFLESH_PANEL:
				setChannelMediaInfo(mViewController.getCurrentChannel());
				break;
			case KEY_DOWN:
				resetEpgInfo();
				break;
			case KEY_UP:
				
				break;

			default:
				break;
			}
		};
	};
	
	/** panel其它信息 */
	private void setChannelMediaInfo(DvbService info){
		if(info!=null){
			int audioVolumeIndex = info.getSoundTrack();
			int audioLanguageIndex = info.getAudioIndex();
			mNowTime.setText(DateFormatUtil.getTimeFromMillis(mViewController.getUtcTime()+8*3600*1000));
			mVoice_model2.setText(language_items[audioLanguageIndex]);
			mVoice_model1.setText(soundtrack_items[audioVolumeIndex]);
		}
	}
	
	public void initAd() {
		Drawable a = getResources().getDrawable(R.drawable.mini_epg_ad);
		BitmapDrawable bd = (BitmapDrawable) a;
		Bitmap bm = bd.getBitmap();
		Bitmap renren = getReflectionImageWithOrigin(bm);
		if(renren != null){
			mAd.setImageBitmap(renren);
		}
	}
	
	
	public void hideEpg(){
		isSwitchChannel = false;
		setVisibility(View.INVISIBLE);
		mNetEventCache.clear();
		mDvbServiceCache.clear();
		mEventFromTS.clear();
		Log.d(TAG, "mNetEventCache.clear mServiceCache.clear ");
		isVisibility = false;
		
	}
	public void processMessage(Object sender, DvbMessage msg) {
		switch (msg.what) {
		case ViewMessage.SWITCH_CHANNEL_UPDOWN: //切换频道
			if(mViewController == null)
				mViewController = (ViewController) sender;
			isSwitchChannel = true;
			Log.d(TAG, "msg.what = "+msg.what +"   msg.ag1="+msg.arg1+"   total="+mViewController.getTotalChannelSize());
			switch (msg.arg1) {
				case ViewMessage.KEYCODE_UP:
					Log.d(TAG, "--- up action-down----" +isVisibility);
					if(isVisibility){
						handler.sendEmptyMessageDelayed(KEY_DOWN, 600);
						up();
					}else{
						showEpg(sender);
					}
					break;
				case ViewMessage.KEYCODE_DOWN:
					Log.d(TAG, "--- down action-down----" +isVisibility);
					if(isVisibility){
						handler.sendEmptyMessageDelayed(KEY_DOWN, 600);
						down();
					}else{
						showEpg(sender);
					}
					break;
				case ViewMessage.KEYCODE_ACTION_UP:
					handler.removeMessages(KEY_DOWN);
					Log.d(TAG, "action-up");
					refreshEpgInfo();
					break;
			}
			break;
		case ViewMessage.RECEIVED_CHANNEL_INFO_KEY:
			Log.d(TAG, "-----------SHOW_EPG_INFO_ONCE------------");
			showEpg(sender);
			refreshEpgInfo();
			handler.sendEmptyMessage(REFLESH_PANEL);
			break;
		case ViewMessage.SWITCH_CHANNEL: //数字键切台、切换TV/BC   (每次都刷新信息)
			Log.d(TAG, "SHOW_EPG_INFO_ONEMORE : 数字键切台、切换TV/BC");
			showEpg(sender);
			refreshEpgInfo();
			handler.sendEmptyMessage(REFLESH_PANEL);
			break;
		case ViewMessage.SWITCH_PLAY_MODE:
			mNetEventCache.clear();
			mDvbServiceCache.clear();
			break;
		case ViewMessage.RECEIVED_CHANNEL_MINIEPG:
			if(isVisibility){
				refreshEpgInfo(msg.arg1,(MiniEpgNotify) msg.obj);
			}
			break;
		}
	}
	
	private void refreshEpgInfo(Integer serviceId,MiniEpgNotify obj) {
		if(!mEventFromTS.contains(serviceId))
			return;
		refreshEpgInfo(obj);
	}
	private void refreshEpgInfo(MiniEpgNotify obj) {
		viewHolder.channel_content_time_cen.setText(DateFormatUtil.getTimeFromMillis(obj.getCurrentEventStartTime()*1000 + 8*3600*1000));
		viewHolder.channel_content_cen.setText(obj.getCurrentEventName());
		viewHolder.channel_content_episode_cen.setText("");
		
		viewHolder.next_channel_time_cen.setText(DateFormatUtil.getTimeFromMillis(obj.getNextEventStartTime()*1000 + 8*3600*1000));
		viewHolder.next_channel_content_cen.setText(obj.getNextEventName());
		viewHolder.next_channel_content_episode_cen.setText("");
	}

	private void up(){
		int addPosition = surePositionTrue(mViewController.getTotalChannelSize() , currentShowPosition+3);
		DvbService destService = getChannelByListIndex(addPosition);
		
		mDvbServiceQuenes.remove(3);
		mDvbServiceQuenes.add(0, destService);
		refreshChannelName();
		currentShowPosition = surePositionTrue(mViewController.getTotalChannelSize(), currentShowPosition+1);
		
		handler.removeMessages(HIDE_EPG);
		handler.sendEmptyMessageDelayed(HIDE_EPG, HIDE_EPG_TIME);
	}
	
	private void down(){
		int addPosition = surePositionTrue(mViewController.getTotalChannelSize() , currentShowPosition-2);
		DvbService destService = getChannelByListIndex(addPosition);
		
		mDvbServiceQuenes.remove(0);
		mDvbServiceQuenes.add(destService);
		currentShowPosition = surePositionTrue(mViewController.getTotalChannelSize(), currentShowPosition-1);
		refreshChannelName();
		
		handler.removeMessages(HIDE_EPG);
		handler.sendEmptyMessageDelayed(HIDE_EPG, HIDE_EPG_TIME);
	}
	
	/** 同步刷新频道号和频道名称 */
	private void refreshChannelName(){
		DvbService ds;
		ds=mDvbServiceQuenes.get(0);
		if(ds !=null ){
			viewHolder.channel_number_top_a.setText(String.valueOf(ds.getLogicChNumber()));
			viewHolder.channel_name_top_a.setText(ds.getChannelName());
		}
		ds=mDvbServiceQuenes.get(1);
		if(ds !=null ){
			viewHolder.channel_number_top_b.setText(String.valueOf(ds.getLogicChNumber()));
			viewHolder.channel_name_top_b.setText(ds.getChannelName());
		}
		ds=mDvbServiceQuenes.get(2);
		if(ds !=null ){
			viewHolder.channel_number_cen.setText(String.valueOf(ds.getLogicChNumber()));
			viewHolder.channel_name_cen.setText(ds.getChannelName());
			setChannelMediaInfo(ds);
		}
		ds=mDvbServiceQuenes.get(3);
		if(ds !=null ){
			viewHolder.channel_number_bottom.setText(String.valueOf(ds.getLogicChNumber()));
			viewHolder.channel_name_bottom.setText(ds.getChannelName());
		}
	}
	
	/** 异步刷新EPG信息 */
	public void refreshChannlePF(){
		
		if(mInfoQueens.size()<=4){
			return;
		}
		
		if(mInfoQueens.get(0)[0]!=null){
			viewHolder.channel_content_time_top_a.setText(DateFormatUtil.getTimeFromMillis(mInfoQueens.get(0)[0].getBegintime()*1000 + 8*3600*1000));
			viewHolder.channel_content_top_a.setText(mInfoQueens.get(0)[0].getEname());
			viewHolder.channel_content_episode_top_a.setText("");
		}else{
			viewHolder.channel_content_time_top_a.setText("");
			viewHolder.channel_content_top_a.setText("");
			viewHolder.channel_content_episode_top_a.setText("");
		}
		
		if(mInfoQueens.get(1)[0]!=null){
			viewHolder.channel_content_time_top_b.setText(DateFormatUtil.getTimeFromMillis(mInfoQueens.get(1)[0].getBegintime()*1000 + 8*3600*1000));
			viewHolder.channel_content_top_b.setText(mInfoQueens.get(1)[0].getEname());
			viewHolder.channel_content_episode_top_b.setText("");;
		}else{
			viewHolder.channel_content_time_top_b.setText("");
			viewHolder.channel_content_top_b.setText("");
			viewHolder.channel_content_episode_top_b.setText("");;
		}
		
		if(mInfoQueens.get(2)[0]!=null){
			viewHolder.channel_content_time_cen.setText(DateFormatUtil.getTimeFromMillis(mInfoQueens.get(2)[0].getBegintime()*1000 + 8*3600*1000));
			viewHolder.channel_content_cen.setText(mInfoQueens.get(2)[0].getEname());
			viewHolder.channel_content_episode_cen.setText("");;
			
			viewHolder.next_channel_time_cen.setText(DateFormatUtil.getTimeFromMillis(mInfoQueens.get(2)[1].getBegintime()*1000 + 8*3600*1000));
			viewHolder.next_channel_content_cen.setText(mInfoQueens.get(2)[1].getEname());
			viewHolder.next_channel_content_episode_cen.setText("");
		}else{
			viewHolder.channel_content_time_cen.setText("");
			viewHolder.channel_content_cen.setText("");
			viewHolder.channel_content_episode_cen.setText("");;
			
			viewHolder.next_channel_time_cen.setText("");
			viewHolder.next_channel_content_cen.setText("");
			viewHolder.next_channel_content_episode_cen.setText("");
		}
		if(mInfoQueens.get(2)[0].getDuration()!=0){
			viewHolder.channel_progressbar_cen.setVisibility(View.VISIBLE);
			viewHolder.channel_progressbar_cen.setProgress(mInfoQueens.get(2)[0].getProgress());
		}else{
			viewHolder.channel_progressbar_cen.setVisibility(View.GONE);
		}
		
		if(mInfoQueens.get(3)[0]!=null){
			viewHolder.channel_content_time_bottom.setText(DateFormatUtil.getTimeFromMillis(mInfoQueens.get(3)[0].getBegintime()*1000 + 8*3600*1000));
			viewHolder.channel_content_bottom.setText(mInfoQueens.get(3)[0].getEname());
			viewHolder.channel_content_episode_bottom.setText("");
		}else{
			viewHolder.channel_content_time_bottom.setText("");
			viewHolder.channel_content_bottom.setText("");
			viewHolder.channel_content_episode_bottom.setText("");
		}
	}
	
	/** 重置EPG信息 */
	public void resetEpgInfo(){
		
		viewHolder.channel_content_time_top_a.setText("");
		viewHolder.channel_content_top_a.setText("");
		viewHolder.channel_content_episode_top_a.setText("");
	
		viewHolder.channel_content_time_top_b.setText("");
		viewHolder.channel_content_top_b.setText("");
		viewHolder.channel_content_episode_top_b.setText("");;
	
		viewHolder.channel_content_episode_cen.setText("");;
		
		viewHolder.next_channel_content_episode_cen.setText("");
		
		viewHolder.channel_content_time_cen.setText("");
		viewHolder.channel_content_cen.setText("");
		viewHolder.channel_content_episode_cen.setText("");;
		
		viewHolder.next_channel_time_cen.setText("");
		viewHolder.next_channel_content_cen.setText("");
		viewHolder.next_channel_content_episode_cen.setText("");
		
		viewHolder.channel_progressbar_cen.setVisibility(View.GONE);
	
		viewHolder.channel_content_time_bottom.setText("");
		viewHolder.channel_content_bottom.setText("");
		viewHolder.channel_content_episode_bottom.setText("");
	}
	
	/** AsyncTask去取EPG信息 */
	private void refreshEpgInfo(){
		int size = mTasks.size();
		for(int i=0;i<size;i++){
			AsyncTask task = mTasks.get(i);
			if(task != null)
				task.cancel(true);
		}
		mTasks.clear();
		for(int i=0;i<mDvbServiceQuenes.size();i++){
			getEpgInfo(i,mDvbServiceQuenes.get(i).getServiceId());
		}
	}
	
	private void getEpgInfo(final int i, final int serviceId) {
		NETEventInfo[] infos = mNetEventCache.get(serviceId);
		if(infos != null){
			Log.d(TAG, "缓存 ***  "+serviceId);
			setEpgInfo(infos,i);
		}else{
			setEpgInfo(getNull2(),i);
			AsyncTask<Integer, Void, NETEventInfo[]> task = new AsyncTask<Integer, Void, NETEventInfo[]>() {
				
				protected NETEventInfo[] doInBackground(Integer... params) {
					Log.d(TAG, "doInBackground ***  "+serviceId);
					NETEventInfo[] info = mViewController.getDvbController().getItemInfo(serviceId);
					return info;
				}
				protected void onPostExecute(NETEventInfo[] result) {
					if(result!=null && result[0].getEname()!=null){
						mNetEventCache.put(serviceId, result);
						setEpgInfo(result,i);
					}else{
						if(isSwitchChannel){
							mEventFromTS.add(serviceId);
						}else{
							if(i == 2){
								MiniEpgNotify tPf = new MiniEpgNotify(); 
								int ret = mViewController.getPf(mDvbServiceQuenes.get(2).getServiceId(), tPf);
								if(ret >= 0)
									refreshEpgInfo(tPf);
							}
						}
					}
					mTasks.remove(serviceId);
				};
				
			}.executeOnExecutor(ThreadPoolManager.getExecutor());
			mTasks.put(serviceId, task);
		}
	}

	private void setEpgInfo(NETEventInfo[] infos, int i) {
		switch (i) {
			case 0:
				if(infos[0]!=null){
					viewHolder.channel_content_time_top_a.setText(DateFormatUtil.getTimeFromMillis(infos[0].getBegintime()*1000 + 8*3600*1000));
					viewHolder.channel_content_top_a.setText(infos[0].getEname());
					viewHolder.channel_content_episode_top_a.setText("");
				}else{
					viewHolder.channel_content_time_top_a.setText("");
					viewHolder.channel_content_top_a.setText("");
					viewHolder.channel_content_episode_top_a.setText("");
				}
				break;
			case 1:
				if(infos[0]!=null){
					Log.d(TAG, "infos[0]!=null");
					viewHolder.channel_content_time_top_b.setText(DateFormatUtil.getTimeFromMillis(infos[0].getBegintime()*1000 + 8*3600*1000));
					viewHolder.channel_content_top_b.setText(infos[0].getEname());
					viewHolder.channel_content_episode_top_b.setText("");;
				}else{
					Log.d(TAG, "infos[0]==null");
					viewHolder.channel_content_time_top_b.setText("");
					viewHolder.channel_content_top_b.setText("");
					viewHolder.channel_content_episode_top_b.setText("");;
				}				
				break;
			case 2:
				if(infos[0]!=null){
					viewHolder.channel_content_time_cen.setText(DateFormatUtil.getTimeFromMillis(infos[0].getBegintime()*1000 + 8*3600*1000));
					viewHolder.channel_content_cen.setText(infos[0].getEname());
					viewHolder.channel_content_episode_cen.setText("");;
					
					viewHolder.next_channel_time_cen.setText(DateFormatUtil.getTimeFromMillis(infos[1].getBegintime()*1000 + 8*3600*1000));
					viewHolder.next_channel_content_cen.setText(infos[1].getEname());
					viewHolder.next_channel_content_episode_cen.setText("");
					if(infos[0].getDuration()!=0){
						viewHolder.channel_progressbar_cen.setVisibility(View.VISIBLE);
						viewHolder.channel_progressbar_cen.setProgress(infos[0].getProgress());
					}else{
						viewHolder.channel_progressbar_cen.setVisibility(View.GONE);
					}
				}else{
					viewHolder.channel_content_time_cen.setText("");
					viewHolder.channel_content_cen.setText("");
					viewHolder.channel_content_episode_cen.setText("");;
					
					viewHolder.next_channel_time_cen.setText("");
					viewHolder.next_channel_content_cen.setText("");
					viewHolder.next_channel_content_episode_cen.setText("");
					viewHolder.channel_progressbar_cen.setVisibility(View.GONE);
				}
				break;
			case 3:
				if(infos[0]!=null){
					viewHolder.channel_content_time_bottom.setText(DateFormatUtil.getTimeFromMillis(infos[0].getBegintime()*1000 + 8*3600*1000));
					viewHolder.channel_content_bottom.setText(infos[0].getEname());
					viewHolder.channel_content_episode_bottom.setText("");
				}else{
					viewHolder.channel_content_time_bottom.setText("");
					viewHolder.channel_content_bottom.setText("");
					viewHolder.channel_content_episode_bottom.setText("");
				}
				break;
	
			default:
				break;
		}
		
	}

	private NETEventInfo[] getNull(){
		NETEventInfo[] infos = new NETEventInfo[2];
		infos[0] = new NETEventInfo();
		infos[1] = new NETEventInfo();
		return infos;
	}
	
	private NETEventInfo[] getNull2(){
		NETEventInfo[] infos = new NETEventInfo[2];
		infos[0] = null;
		infos[1] = null;
		return infos;
	}
	
	
	/** 初始化EPG */
	public void showEpg(Object sender){
		
		long start = System.currentTimeMillis();
		if(mViewController == null){
			mViewController = (ViewController) sender;
		}
		currentShowPosition = mViewController.getCurrentPosition();
		mInfoQueens.clear();
		mDvbServiceQuenes.clear();
		int b , c , d , e;
		int total = mViewController.getTotalChannelSize();
		d = currentShowPosition;
		
		b = surePositionTrue(total, d+2);
		c = surePositionTrue(total, d+1);
		d = surePositionTrue(total, d);
		e = surePositionTrue(total, d-1);
		
		//----------------------- 
		mDvbServiceQuenes.add(getChannelByListIndex(b));
		mDvbServiceQuenes.add(getChannelByListIndex(c));
		mDvbServiceQuenes.add(getChannelByListIndex(d));
		mDvbServiceQuenes.add(getChannelByListIndex(e));
		
		refreshChannelName();
		//-----------------------
		setVisibility(View.VISIBLE);
		isVisibility = true;
		handler.removeMessages(HIDE_EPG);
		handler.sendEmptyMessageDelayed(HIDE_EPG, HIDE_EPG_TIME);
		DvbLog.D("time", " showEpg time : "+(System.currentTimeMillis()-start));
		
	}
	
	private DvbService getChannelByListIndex(int index){
		if(mDvbServiceCache.get(index) != null)
			return mDvbServiceCache.get(index);
		DvbService ds = mViewController.getChannelByListIndex(index);
		mDvbServiceCache.put(index, ds);
		return ds;
	}
	
	public int surePositionTrue(int total , int index){
		int position;
		if(index<0){
			position = total+index;
			return surePositionTrue(total, position);
		}else if(index==0){
			position = 0;
		}else if(index > total){
			position = index - total;
			return surePositionTrue(total, position);
		}else if(index == total){
			position = 0;
		}else{
			position = index;
		}
		
		return position;
	}

	
	public static Bitmap getReflectionImageWithOrigin(Bitmap bitmap) {

		// 原始图片和反射图片中间的间距
		final int reflectionGap = 0;
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		// 反转
		Matrix matrix = new Matrix();

		// 第一个参数为1表示x方向上以原比例为准保持不变，正数表示方向不变。
		// 第二个参数为-1表示y方向上以原比例为准保持不变，负数表示方向取反。
		matrix.preScale(1, -1);
		Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, height / 2,
				width, height / 2, matrix, false);
		Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
				(height + height / 5), Config.RGB_565);
		Canvas canvas = new Canvas(bitmapWithReflection);
		canvas.drawBitmap(bitmap, 0, 0, null);
		Paint defaultPaint = new Paint();
		canvas.drawRect(0, height, width, height + reflectionGap, defaultPaint);
		canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);
		Paint paint = new Paint();
		LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0,
				bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff,
				0x00ffffff, TileMode.CLAMP);
		paint.setShader(shader);
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		canvas.drawRect(0, height, width, bitmapWithReflection.getHeight()
				+ reflectionGap, paint);
		return bitmapWithReflection;
	}

	/**
	 * 获得圆角图片
	 * 
	 * @param bitmap
	 * @param roundPx
	 * @return
	 */
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.RGB_565);
		Canvas canvas = new Canvas(output);
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}

}
