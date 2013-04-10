package com.joysee.adtv.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DvbLog;
import com.joysee.adtv.controller.ViewController;
import com.joysee.adtv.db.Channel;
import com.joysee.adtv.logic.SettingManager;
import com.joysee.adtv.logic.bean.Program;
import com.joysee.adtv.server.ADTVService;
import com.joysee.adtv.ui.Menu.InterceptKeyListener;
import com.joysee.adtv.ui.Menu.MenuListener;
import com.joysee.adtv.ui.adapter.ProgramReservesListAdapter;

public class MenuReservationList extends FrameLayout implements MenuListener{
	
	private static final DvbLog log = new DvbLog(
            "MenuReservationList",DvbLog.DebugType.D);

	private ListView mProgramReservesListView;
    private TextView mProgramReserveNoChannelTextView;
    private Activity mActivity;
    
    private List<Integer> mReservePositionList;
    private Cursor mProgramReserveCursor;
    private ArrayList<Integer> mProgramReserveList;
    private SettingManager mSettingManager;
    private ProgramReservesListAdapter mProgramReservesListAdapter;
    private static final int PROGRAM_LIST_TYPE = 2;
    private static final int RESERVE_STATUS_ON = 1;
    private static final int RESERVE_STATUS_OFF = 0;
    private double mTimeZone;
    private ViewController mViewController;
    private final int OFFSET = (int) getResources().getDimension(R.dimen.menu_margin_top);
    
    private InterceptKeyListener mInterceptKeyListener;
	private TextView mBackTextView;
	private ImageView mFocusView;
	private View mLastFocusView;

	public MenuReservationList(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public MenuReservationList(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MenuReservationList(Context context) {
		super(context);
	}
	
	@Override
	protected void onFinishInflate() {
		mTimeZone = (double)(TimeZone.getDefault().getRawOffset())/1000/3600;
		mProgramReservesListView = (ListView) findViewById(R.id.menu_reservation_listview);
        mProgramReserveNoChannelTextView = (TextView)findViewById(R.id.progaram_reserve_no_channel_textview);
        mProgramReservesListView.setOnKeyListener(mOnKeyListener);
        mProgramReserveNoChannelTextView.setOnKeyListener(mOnKeyListener);
        mBackTextView = (TextView) findViewById(R.id.menu_reservation_back_textview);
        mFocusView = (ImageView) findViewById(R.id.ivFocus);
//        mBackTextView.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				mInterceptKeyListener.backSettingParent();
//			}
//		});
//        mBackTextView.setOnFocusChangeListener(mOnFocusChangeListener);
        mProgramReservesListView.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d("songwenxuan","onItemSelected() + position = " + position);
				int [] location = new int [2];
				view.getLocationInWindow(location);
				MarginLayoutParams params = (MarginLayoutParams) mFocusView
						.getLayoutParams();
				params.topMargin = location[1] - OFFSET;
				mFocusView.setVisibility(View.VISIBLE);
				mFocusView.setLayoutParams(params);
				if(mLastFocusView != null){
					Log.d("songwenxuan","mLastFocusView != null");
					setFocusTextColor(false, mLastFocusView);
				}
				setFocusTextColor(true, view);
//				Animation anim = new AlphaAnimation(0.0f, 1.0f);
//				anim.setDuration(300);
//				anim.setFillAfter(true);
//				anim.setFillEnabled(true);
//				mFocusView.startAnimation(anim);
				mLastFocusView = view;
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {}
		});
		super.onFinishInflate();
	}
	
	public void fillData(Activity activity){
		mLastFocusView = null;
		mActivity = activity;
        if(null == mSettingManager){
            mSettingManager = SettingManager.getSettingManager();
        }
        if(null == mProgramReserveList){
            mProgramReserveList = new ArrayList<Integer>();
        }
        if(null == mReservePositionList){
            mReservePositionList = new ArrayList<Integer>();
        }
        mProgramReserveCursor = mActivity.getContentResolver().query(Channel.URI.TABLE_RESERVES, 
                null, null, null, Channel.TableReservesColumns.STARTTIME);
        while (mProgramReserveCursor.moveToNext()) {
            long startTimeDB = (long)mProgramReserveCursor.getInt(mProgramReserveCursor.getColumnIndex(
                    Channel.TableReservesColumns.STARTTIME));
            if(startTimeDB*1000 < getUtcTime()){
                mActivity.getContentResolver().delete(Channel.URI.TABLE_RESERVES, 
                        Channel.TableReservesColumns.STARTTIME+"=?", new String[]{""+startTimeDB});
            }
        }
        mProgramReserveCursor.requery();
        setAdatper();

        
        mProgramReservesListView.setOnItemClickListener(new OnItemClickListener() {
            
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mProgramReserveCursor!=null && mProgramReserveCursor.getCount()>0){
                    mProgramReserveCursor.moveToPosition(position);
                }
                View tClockTag =mProgramReservesListView.findViewWithTag(position);
                int tProgramReserveId = mProgramReserveCursor.getInt(
                        mProgramReserveCursor.getColumnIndex(Channel.TableReservesColumns.ID));
                long startTime = mProgramReserveCursor.getInt(
                        mProgramReserveCursor.getColumnIndex(Channel.TableReservesColumns.STARTTIME));
                int serviceId = mProgramReserveCursor.getInt(
                		mProgramReserveCursor.getColumnIndex(Channel.TableReservesColumns.SERVICEID));
//                ADTVService.getService().getEpg().getProgram(String.valueOf(serviceId)+String.valueOf(startTime*1000 + getTimeZoneCompensate()));
                if(mProgramReserveList.contains(tProgramReserveId)){
                    mProgramReserveList.remove(Integer.valueOf(tProgramReserveId));
                    mReservePositionList.remove(Integer.valueOf(position));
                    tClockTag.setVisibility(View.VISIBLE);
                    ContentValues values = new ContentValues();
                    values.put(Channel.TableReservesColumns.RESERVESTATUS, RESERVE_STATUS_ON);
                    mActivity.getContentResolver().update(
                            Channel.URI.TABLE_RESERVES, 
                            values, 
                            Channel.TableReservesColumns.ID+"=?", 
                            new String[]{tProgramReserveId+""});
                    
                    long endTime = mProgramReserveCursor.getInt(
                    		mProgramReserveCursor.getColumnIndex(Channel.TableReservesColumns.ENDTIME));
                    int programId = mProgramReserveCursor.getInt(
                    		mProgramReserveCursor.getColumnIndex(Channel.TableReservesColumns.PROGRAMID));
                    int channelNumber = mProgramReserveCursor.getInt(
                    		mProgramReserveCursor.getColumnIndex(Channel.TableReservesColumns.CHANNELNUMBER));
                    String programName = mProgramReserveCursor.getString(
                    		mProgramReserveCursor.getColumnIndex(Channel.TableReservesColumns.PROGRAMNAME));
                    String channelName = mProgramReserveCursor.getString(
                    		mProgramReserveCursor.getColumnIndex(Channel.TableReservesColumns.CHANNELNAME));
                    Program pro=new Program();
                    pro.setId(Integer.valueOf(tProgramReserveId));
                    pro.setChannelName(channelName);
                    pro.setChannelNumber(channelNumber);
                    pro.setEndTime(endTime);
                    pro.setName(programName);
                    pro.setProgramId(programId);
                    pro.setStartTime(startTime*1000 + getTimeZoneCompensate());
                    pro.setServiceId(serviceId);
                    pro.setStatus(RESERVE_STATUS_ON);
                    ADTVService.getService().getEpg().addProgram(String.valueOf(serviceId)+String.valueOf(startTime), pro);
                    addReServeProgramToAlam(tProgramReserveId, startTime*1000+getTimeZoneCompensate() + getSystemCompensate() - 60*1000);
                }else{
                    ContentValues values = new ContentValues();
                    values.put(Channel.TableReservesColumns.RESERVESTATUS, RESERVE_STATUS_OFF);
                    mActivity.getContentResolver().update(
                            Channel.URI.TABLE_RESERVES, 
                            values, 
                            Channel.TableReservesColumns.ID+"=?", 
                            new String[]{tProgramReserveId+""});
                    mProgramReserveList.add(tProgramReserveId);
                    mReservePositionList.add(position);
                    ADTVService.getService().getEpg().removeProgram(String.valueOf(serviceId)+String.valueOf(startTime));
                    tClockTag.setVisibility(View.INVISIBLE);
                    removeReserveProgramFromAlarm(tProgramReserveId);
                }
            }

            private long getTimeZoneCompensate() {
                return (long)((8-mTimeZone)*3600*1000);
            }
        });
        
        mProgramReservesListView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                    int totalItemCount) {
                for(Integer positon : mReservePositionList){
                    if(positon >= firstVisibleItem && positon<=firstVisibleItem + visibleItemCount - 1){
                        View tColockImage = mProgramReservesListView.findViewWithTag(positon);
                        tColockImage.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
        mProgramReservesListView.setFocusable(true);
        mProgramReservesListView.requestFocus();
        if(mProgramReserveCursor.getCount() == 0){
        	log.D("no reservation program");
        	mFocusView.setVisibility(View.INVISIBLE);
            mProgramReserveNoChannelTextView.setVisibility(View.VISIBLE);
            mProgramReservesListView.setFocusable(false);
            mProgramReserveNoChannelTextView.requestFocus();
        }else {
            mProgramReserveNoChannelTextView.setVisibility(View.INVISIBLE);
        }
	}
	
	private long getUtcTime() {
        String utcTimeStr = mSettingManager.nativeGetTimeFromTs();
        String[] utcTime = utcTimeStr.split(":");
        long currentTimeMillis = Long.valueOf(utcTime[0])*1000;
        return currentTimeMillis;
    }
	
	private void setAdatper() {
        Cursor tCursor = mActivity.getContentResolver().query(Channel.URI.TABLE_RESERVES, 
                null, null, null, Channel.TableReservesColumns.STARTTIME);
        List<Program> tProgramList = new ArrayList<Program>();
        while(tCursor.moveToNext()){
            Program program = new Program();
            long startTime =(long)tCursor.getInt(tCursor.getColumnIndex(Channel.TableReservesColumns.STARTTIME));
            program.setStartTime(startTime);
            String programName = tCursor.getString(tCursor.getColumnIndex(Channel.TableReservesColumns.PROGRAMNAME));
            program.setName(programName);
            String channelName = tCursor.getString(tCursor.getColumnIndex(Channel.TableReservesColumns.CHANNELNAME));
            program.setChannelName(channelName);
            Log.d("songwenxuan","startTime = " + startTime + "programName = " + programName +"channelName = " + channelName);
            tProgramList.add(program);
        }
        tCursor.close();
        if(mProgramReservesListAdapter == null){
            mProgramReservesListAdapter = new ProgramReservesListAdapter(tProgramList, LayoutInflater.from(mActivity));
            mProgramReservesListView.setAdapter(mProgramReservesListAdapter);
        }else {
            mProgramReservesListAdapter.setmProgramList(tProgramList);
            mProgramReservesListAdapter.setInflater(LayoutInflater.from(mActivity));
            mProgramReservesListAdapter.notifyDataSetChanged();
        }
    }
	
	/** 获取系统时间与dvb事件差long值 */
    private long getSystemCompensate(){
        String realTimeStr = mSettingManager.nativeGetTimeFromTs();
        String[] splitTime = realTimeStr.split(":");
        long realTime = Long.valueOf(splitTime[0])*1000 + (long)((8-mTimeZone)*3600*1000);
        long timeCompensate = System.currentTimeMillis()-realTime;
        return timeCompensate;
    }
    
    /** 删除预约闹钟 */
    private void removeReserveProgramFromAlarm(int id) {
        Log.d("songwenxuan","remove alarm id = "+id);
        Intent intent = new Intent("program alarm");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                mActivity,id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) mActivity.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
    
    /** 添加预约闹钟 */
    private void addReServeProgramToAlam(int id, long startTime){
        log.D("addReServeProgramToAlam() -- reserveId=" + id + ", startTime=" + startTime);
        Log.d("songwenxuan","bind alarm id = "+id);
        Intent intent = new Intent("program alarm");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mActivity,id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) mActivity.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, startTime, pendingIntent);
    }
    
    public void clear(){
    	deleteAlertReserveData(PROGRAM_LIST_TYPE);
        mProgramReserveList.clear();
        mReservePositionList.clear();
        if (mProgramReserveCursor != null)
        	mProgramReserveCursor.close();
        mLastFocusView = null;
    }
    
    /** 删除预约闹钟数据 */
    private void deleteAlertReserveData(int type) {
        switch (type) {
            case PROGRAM_LIST_TYPE:
                if(mProgramReserveList != null){
                    for (Integer id : mProgramReserveList) {
                        mActivity.getContentResolver().delete(
                                Channel.URI.TABLE_RESERVES, 
                                Channel.TableReservesColumns.ID+"=? ",
                                new String[]{""+id});
                        removeReserveProgramFromAlarm(id);
                    }
                }
                break;
            default:
                break;
        }
    }
    
    private OnKeyListener mOnKeyListener = new  OnKeyListener() {
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			log.D("on key ....");
			final int action = event.getAction();
			switch (keyCode) {
			case KeyEvent.KEYCODE_VOLUME_DOWN:
			case KeyEvent.KEYCODE_VOLUME_UP:
				if (action == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
						mViewController.changeVolume(-1);
					else
						mViewController.changeVolume(1);
					return true;
				}
				break;
//			case KeyEvent.KEYCODE_HOME:
//			case KeyEvent.KEYCODE_BACK:
//            case KeyEvent.KEYCODE_ESCAPE:
//            	if (action == KeyEvent.ACTION_DOWN) {
//            		log.D("back Setting Parent!!!!!");
//            		clear();
//            		mInterceptKeyListener.backSettingParent();
//            		return true;
//            	}
			}
		return false;
		}
	};
	
	public void setInterceptKeyListener(InterceptKeyListener interceptKeyListener){
		mInterceptKeyListener = interceptKeyListener;
	}
	
	private boolean mKeyRepeat = false;
	private long mLastKeyTime;
	private int mKeyRepeatInterval = 100;
	private int mPosition;
	private int mMaxPosition = 8;
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		Log.d("songwenxuan","dispatchKeyEvent()");
		int keyCode = event.getKeyCode();
		int action = event.getAction();
		if(keyCode == KeyEvent.KEYCODE_ESCAPE || action == KeyEvent.KEYCODE_BACK){
			log.D("event.getKeyCode() == KeyEvent.KEYCODE_ESCAPE" );
			clear();
		}
		
		if(keyCode == KeyEvent.KEYCODE_F11 && action == KeyEvent.ACTION_DOWN){
			mProgramReservesListView.smoothScrollToPositionFromTop(mProgramReservesListView.getFirstVisiblePosition()+1,0, 300);
			mProgramReservesListView.setSelectionFromTop(mProgramReservesListView.getLastVisiblePosition()-1,800);
		}
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			final long currenKeyDownTime = SystemClock.uptimeMillis();
			final long interval = currenKeyDownTime - mLastKeyTime;
			if (mKeyRepeat && interval < mKeyRepeatInterval ){
				log.D(mKeyRepeat+" 抛弃");
				return false;
			}
			mLastKeyTime = currenKeyDownTime;
			mKeyRepeat = true;
		}else{
			mKeyRepeat = false;
		}
		if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN && action == KeyEvent.ACTION_DOWN){
			int selectedItemPosition = mProgramReservesListView.getSelectedItemPosition();
			int firstVisiblePosition = mProgramReservesListView.getFirstVisiblePosition();
			if(mLastFocusView!=null && selectedItemPosition != mProgramReservesListView.getCount()-1){
				setFocusTextColor(false, mLastFocusView);
			}
			if(mPosition < mMaxPosition)
				mPosition++;
			if (mPosition == mMaxPosition ) {
				View view = mProgramReservesListView.getChildAt(selectedItemPosition-firstVisiblePosition+1);
				log.D("selectedPosition " + selectedItemPosition+" firstPosition"+firstVisiblePosition +" childAt "+(selectedItemPosition-firstVisiblePosition)+ " view "+view);
				if(view !=null ){
					setFocusTextColor(true, view);
				}
				log.D(" down long action down");
			}
			
		}else if(keyCode == KeyEvent.KEYCODE_DPAD_UP && action == KeyEvent.ACTION_DOWN){
			int selectedItemPosition = mProgramReservesListView.getSelectedItemPosition();
			if(mLastFocusView!=null && selectedItemPosition != 0){
				setFocusTextColor(false, mLastFocusView);
			}
			if(mPosition > 0)
				mPosition--;
			if (mPosition == 0 ) {
				int firstVisiblePosition = mProgramReservesListView.getFirstVisiblePosition();
            	View view = mProgramReservesListView.getChildAt(selectedItemPosition-firstVisiblePosition-1);
            	if(view !=null ){
            		setFocusTextColor(true, view);
            	}
			}
			
		}
		if(mInterceptKeyListener.onKeyEvent(keyCode,action)){
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public void getFocus() {
		if(mProgramReserveCursor.getCount() > 0){
			mProgramReservesListView.requestFocus();
		}else{
			mProgramReserveNoChannelTextView.requestFocus();
		}
	}

	@Override
	public void loseFocus() {
		
	}
	
	private void setFocusTextColor(boolean isFocus , View view){
		TextView dateTextView =(TextView) view.findViewById(R.id.program_reserves_date_textview);
		TextView timeTextView =(TextView) view.findViewById(R.id.program_reserves_time_textview);
		TextView programNameTextView = (TextView) view.findViewById(R.id.program_reserves_proram_name_textview);
		TextView channelNameTextView = (TextView) view.findViewById(R.id.program_reserves_channel_name_textview);
		ImageView reserveImage = (ImageView) view.findViewById(R.id.program_reserves_clock_image);
		if(isFocus){
			reserveImage.setImageResource(R.drawable.menu_reservation_clock_icon_focus);
			dateTextView.setTextColor(getResources().getColor(R.color.menu_list_focus));
			timeTextView.setTextColor(getResources().getColor(R.color.menu_list_focus));
			programNameTextView.setTextColor(getResources().getColor(R.color.menu_list_focus));
			dateTextView.setAlpha(1.0f);
			timeTextView.setAlpha(1.0f);
			channelNameTextView.setAlpha(1.0f);
		}else{
			reserveImage.setImageResource(R.drawable.menu_reservation_clock_icon_unfocus);
//			dateTextView.setTextColor(getResources().getColor(R.color.white));
//			timeTextView.setTextColor(getResources().getColor(R.color.white));
			programNameTextView.setTextColor(getResources().getColor(R.color.white_txt));
			dateTextView.setAlpha(0.4f);
			timeTextView.setAlpha(0.4f);
			channelNameTextView.setAlpha(0.4f);
		}
	}

//	private OnFocusChangeListener mOnFocusChangeListener = new OnFocusChangeListener() {
//		
//		@Override
//		public void onFocusChange(View v, boolean hasFocus) {
//			if(hasFocus){
//				int [] location = new int [2];
//				v.getLocationInWindow(location);
//				MarginLayoutParams params = (MarginLayoutParams) mFocusView
//						.getLayoutParams();
//				params.topMargin = location[1]-mOffset;
//				mFocusView.setLayoutParams(params);
//				Animation anim = new AlphaAnimation(0.0f, 1.0f);
//				anim.setDuration(300);
//				anim.setFillAfter(true);
//				anim.setFillEnabled(true);
//				mFocusView.startAnimation(anim);
//			}
//		}
//	};
	
}
