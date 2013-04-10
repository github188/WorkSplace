package com.joysee.adtv.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DefaultParameter.ViewMessage;
import com.joysee.adtv.common.DvbLog;
import com.joysee.adtv.common.DvbMessage;
import com.joysee.adtv.controller.ViewController;
import com.joysee.adtv.db.Channel;
import com.joysee.adtv.logic.SettingManager;
import com.joysee.adtv.logic.bean.EpgEvent;
import com.joysee.adtv.server.ADTVService;

public class ProgramReserveAlertWindow implements IDvbBaseView {
    private static final DvbLog log = new DvbLog(
            "com.joysee.adtv.ui.ProgramReserveAlertWindow",DvbLog.DebugType.D);
    private Activity mActivity;
    private ImageView mProgramReserveAlertUpImageView;
//    private ImageView mProgramReserveAlertDownImageView;
    private Button mProgramReserveAlertWatchButton;
    private Button mProgramReserveAlertCancelButton;
    private List<EpgEvent> mProgramReserveAlertList;
    private int mProgramReserveAlertTag;
//    private TextView mProgramReserveAlertDateTextView;
//    private TextView mProgramReserveAlertTimeTextView;
    private TextView mProgramReserveAlertChannelNameTextView;
    private TextView mProgramReserveAlertProgramNameTextView;
    private TextView mProgramReserveAlertSecondTextView;
    private int mProgramReserveTimerSecond;
    private SettingManager mSettingManager;
    private Timer mTimer;
    private TimerTask mTask;
//    private static final int RESERVE_STATUS_ON = 1;
    private static final int RESERVE_STATUS_OFF = 0;
    private double mTimeZone;

    private boolean mIsSelected; 
    private static final int REFRESH_TIMER = 1000;
    private static final int DISMISS_WINDOW = 1001;
    
    private View mActivityView;
    private PopupWindow mProgramReserveAlertWindow;
    private View mProgramReserveAlertView;
    private PopupContainer mPopupContainer;
    private ViewController mViewController;
    

    public ProgramReserveAlertWindow(Activity activity) {
        this.mActivity = activity;
        mActivityView = mActivity.getWindow().getDecorView();
        mSettingManager = SettingManager.getSettingManager();
        mProgramReserveAlertList = new ArrayList<EpgEvent>();
    }
    
    private Handler mainHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_TIMER:
                    mProgramReserveTimerSecond--;
                    if(mProgramReserveTimerSecond <= 0){
                        if(mTimer!=null){
                            mTimer.cancel();
                            mTimer = null;
                        }
                        if(mTask!=null){
                            mTask = null;
                        }
                        alertToPlay();
                        deleteAlertReserveData();
                        return;
                    }
                    mProgramReserveAlertSecondTextView.setText(String.valueOf(mProgramReserveTimerSecond));
                    break;
                case DISMISS_WINDOW:
                	if(mProgramReserveAlertWindow != null && mProgramReserveAlertWindow.isShowing())
                		mProgramReserveAlertWindow.dismiss();
                    break;
                default:
                    break;
            }
        }
    };
    
    private class PopupContainer extends LinearLayout {

		public PopupContainer(Context context) {
			super(context);
			this.setClickable(true);
			this.setBackgroundColor(Color.TRANSPARENT);
			this.setOrientation(VERTICAL);
		}
    	
		@Override
	    public boolean dispatchKeyEvent(KeyEvent event) {
	        switch (event.getKeyCode()) {
	            case KeyEvent.KEYCODE_HOME:
	                Log.d("songwenxuan","program reserve alert ... HOME key down!!!!!!!!!!!!!!!!");
	                if(mProgramReserveAlertWindow != null && mProgramReserveAlertWindow.isShowing())
                		mProgramReserveAlertWindow.dismiss();
	                deleteAlertReserveData();
	                mProgramReserveAlertList.clear();
	                
	                mainHandler.removeMessages(REFRESH_TIMER);
	                if(mTimer != null){
	                    mTimer.cancel();
	                    mTimer = null;
	                }
	                if(mTask != null){
	                    mTask = null;
	                }
	                mIsSelected = true;
	                break;
	            case KeyEvent.KEYCODE_DPAD_UP:
	                if(event.getAction() == KeyEvent.ACTION_DOWN){
	                    if(mProgramReserveAlertList.size()>0){
		                    --mProgramReserveAlertTag;
		                    if(mProgramReserveAlertTag < 0){
		                        mProgramReserveAlertTag = mProgramReserveAlertList.size()-1;
		                    }
		                    refreshProgramReserveAlertData(mProgramReserveAlertTag);
	                    }
	                }
	                break;
	            case KeyEvent.KEYCODE_DPAD_DOWN:
	                if(event.getAction() == KeyEvent.ACTION_DOWN){
	                	 if(mProgramReserveAlertList.size()>0){
		                    ++mProgramReserveAlertTag;
		                    if(mProgramReserveAlertTag > mProgramReserveAlertList.size()-1){
		                        mProgramReserveAlertTag = 0;
		                    }
		                    refreshProgramReserveAlertData(mProgramReserveAlertTag);
	                	 }
	                }
	                break;
	            default:
	                break;
	        }
	        return super.dispatchKeyEvent(event);
	    }
    }

    /** 获取系统时间与dvb事件差long值 */
    private long getCompensate(){
        String realTimeStr = mSettingManager.nativeGetTimeFromTs();
        String[] splitTime = realTimeStr.split(":");
        long realTime = Long.valueOf(splitTime[0])*1000 + (long)((8-mTimeZone)*3600*1000);
        long timeCompensate = System.currentTimeMillis()-realTime;
        return timeCompensate;
    }
    /** 刷新预约数据 */
    private void refreshProgramReserveAlertData(int i) {
        
        EpgEvent tEpgEvent = mProgramReserveAlertList.get(i);
        //设置显示数据
//        long startTime = tEpgEvent.getStartTime();
//        String date = DateFormatUtil.getDateFromMillis(startTime);
//        String time = DateFormatUtil.getTimeFromMillis(startTime);
        String programName = tEpgEvent.getProgramName();
        String channelName = tEpgEvent.getProgramDescription();
        
//        mProgramReserveAlertDateTextView.setText(date);
//        mProgramReserveAlertTimeTextView.setText(time);
        mProgramReserveAlertChannelNameTextView.setText(channelName);
        mProgramReserveAlertProgramNameTextView.setText(programName);
    }
    /** 删除预约弹出数据 */
    private void deleteAlertReserveData() {
        if(mProgramReserveAlertList.size()>0){
            for (EpgEvent event : mProgramReserveAlertList) {
                int id = event.getId();
                int serviceId = event.getServiceId();
                long startTime = (event.getStartTime()-(long)(8-mTimeZone)*3600*1000)/1000;
                mActivity.getContentResolver().delete(Channel.URI.TABLE_RESERVES, 
                        Channel.TableReservesColumns.STARTTIME+"=? and "+Channel.TableReservesColumns.SERVICEID+"=? ", 
                        new String[]{startTime+"",serviceId+""});
                ADTVService.getService().getEpg().removeProgram(String.valueOf(serviceId)+String.valueOf(startTime));
                removeReserveProgramFromAlarm(id);
            }
        }
    }
    /** 删除预约闹钟 */
    private void removeReserveProgramFromAlarm(int id) {
        Intent intent = new Intent("program alarm");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mActivity,id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) mActivity.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
    /** 切台 */
    private void alertToPlay() {
        if(mIsSelected){
            log.D("isSelected");
            return;
        }
        Log.d("songwenxuan","alert to play!!!!");
        EpgEvent epgEvent = mProgramReserveAlertList.get(mProgramReserveAlertTag);
        mViewController.switchChannelFromNum(epgEvent.getChannelNumber());
        if(mProgramReserveAlertWindow != null && mProgramReserveAlertWindow.isShowing())
    		mProgramReserveAlertWindow.dismiss();
        deleteAlertReserveData();
        mProgramReserveAlertList.clear();

    }
    private long getUtcTime() {
        String utcTimeStr = mSettingManager.nativeGetTimeFromTs();
        String[] utcTime = utcTimeStr.split(":");
        long currentTimeMillis = Long.valueOf(utcTime[0])*1000;
        return currentTimeMillis;
    }

	private void init(){
		if(mProgramReserveAlertView == null){
			mTimeZone = (double)(TimeZone.getDefault().getRawOffset())/1000/3600;
			mProgramReserveAlertView = LayoutInflater.from(mActivity).inflate(R.layout.program_reserves_alert, null);
			mProgramReserveAlertUpImageView = (ImageView) mProgramReserveAlertView.findViewById(R.id.program_reserve_alert_up);
//	        mProgramReserveAlertDownImageView = (ImageView) mProgramReserveAlertView.findViewById(R.id.program_reserve_alert_down);
	        mProgramReserveAlertWatchButton = (Button) mProgramReserveAlertView.findViewById(R.id.program_reserve_alert_button_watch);
	        mProgramReserveAlertCancelButton = (Button) mProgramReserveAlertView.findViewById(R.id.program_reserve_alert_button_cancel);
	        mProgramReserveAlertSecondTextView = (TextView) mProgramReserveAlertView.findViewById(R.id.program_reserve_alert_seconds);
//	        mProgramReserveAlertDateTextView = (TextView) mProgramReserveAlertView.findViewById(R.id.program_reserve_alert_date);
//	        mProgramReserveAlertTimeTextView = (TextView) mProgramReserveAlertView.findViewById(R.id.program_reserve_alert_time);
	        mProgramReserveAlertChannelNameTextView = (TextView) mProgramReserveAlertView.findViewById(R.id.program_reserve_alert_channel_name);
	        mProgramReserveAlertProgramNameTextView = (TextView) mProgramReserveAlertView.findViewById(R.id.program_reserve_alert_program_name);
	        mPopupContainer = new PopupContainer(mActivity);
	        mPopupContainer.addView(mProgramReserveAlertView);
		}
        
		if (mProgramReserveAlertWindow == null) {
			mProgramReserveAlertWindow = new PopupWindow();
		}
	}
	
	private void setupView() {
        mProgramReserveAlertWatchButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if(mTimer != null){
                    mTimer.cancel();
                    mTimer = null;
                }
                if(mTask != null){
                    mTask = null;
                }
                alertToPlay();
                //删除预约数据
                deleteAlertReserveData();
                mainHandler.removeMessages(REFRESH_TIMER);
                mIsSelected = true;
            }

        });
        
        mProgramReserveAlertCancelButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                //消失
            	if(mProgramReserveAlertWindow != null && mProgramReserveAlertWindow.isShowing())
            		mProgramReserveAlertWindow.dismiss();
                if(mTimer != null){
                    mTimer.cancel();
                    mTimer = null; 
                }
                if(mTask != null){
                    mTask = null; 
                }
                //删除预约数据
                deleteAlertReserveData();
                mProgramReserveAlertList.clear();
                mainHandler.removeMessages(REFRESH_TIMER);
                mIsSelected = true;
            }
        });
        
        mProgramReserveTimerSecond = 60;
        mProgramReserveAlertSecondTextView.setText(""+mProgramReserveTimerSecond);
        mTimer = new Timer();
        mTask = new TimerTask() {
            @Override
            public void run() {
                Log.d("songwenxuan", "refresh time task %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
                mainHandler.sendEmptyMessage(REFRESH_TIMER);
            }
        };
        mTimer.schedule(mTask, 0, 1000);
        mProgramReserveAlertWatchButton.requestFocus();
        mIsSelected = false;
	}
	
	private void showProgramReserveAlertWindow() {
		
		if (mProgramReserveAlertWindow != null && mProgramReserveAlertWindow.isShowing()) {
			return;
		}
		
		init();
        checkReserveData();
        if(mProgramReserveAlertList.size() <= 0)
        	return;
        setupView();
		mProgramReserveAlertWindow.setContentView(mPopupContainer);
		mProgramReserveAlertWindow.setWidth((int)mActivity.getResources().getDimension(R.dimen.input_psd_dialog_weight));
		mProgramReserveAlertWindow.setHeight((int)mActivity.getResources().getDimension(R.dimen.input_psd_dialog_height));
		mProgramReserveAlertWindow.setFocusable(true);
		mProgramReserveAlertWindow.showAtLocation(mActivityView, Gravity.CENTER, 0, 0);
		Log.d("songwenxuan", "---mProgramReserveAlertWindow.showAtLocatio---" );

	}
	private void checkReserveData() {
        mProgramReserveAlertList.clear();
        mProgramReserveAlertTag = 0;
        Cursor managedQuery = mActivity.getContentResolver().query(Channel.URI.TABLE_RESERVES, null, null, null, null);
        long timeCompensate = getCompensate();
        while(managedQuery.moveToNext()){
            long startTime = (long)managedQuery.getInt(managedQuery.getColumnIndex(Channel.TableReservesColumns.STARTTIME));
            //测试 使用12000秒   && (startTime*1000 - System.currentTimeMillis() > 0) && (startTime*1000 + timeCompensate - System.currentTimeMillis() > 0)
            //&& (startTime*1000 - getUtcTime() > 0)
            Log.d("songwenxuan","startTime = " + startTime + "   utcTime = " + getUtcTime());
            if((startTime*1000 - getUtcTime() < 120*1000) && startTime*1000 - getUtcTime() > 0){
                int reserveStatus = managedQuery.getInt(managedQuery.getColumnIndex(Channel.TableReservesColumns.RESERVESTATUS));
                if(reserveStatus == RESERVE_STATUS_OFF){
                    log.D("reserve status is 0 , continue;");
                    continue;
                }
                Log.d("songwenxuan", "start time = "+ startTime*1000 +" timeCompensate = " + timeCompensate);
                String programName = managedQuery.getString(managedQuery.getColumnIndex(Channel.TableReservesColumns.PROGRAMNAME));
                String channelName = managedQuery.getString(managedQuery.getColumnIndex(Channel.TableReservesColumns.CHANNELNAME));
                int serviceId = managedQuery.getInt(managedQuery.getColumnIndex(Channel.TableReservesColumns.SERVICEID));
                int id = managedQuery.getInt(managedQuery.getColumnIndex(Channel.TableReservesColumns.ID));
                int channelNumber = managedQuery.getInt(managedQuery.getColumnIndex(Channel.TableReservesColumns.CHANNELNUMBER));
                
                EpgEvent epgEvent = new EpgEvent();
                epgEvent.setStartTime(startTime*1000 + getTimeZoneCompensate());
                epgEvent.setProgramName(programName);
                epgEvent.setServiceId(serviceId);
                epgEvent.setId(id);
                epgEvent.setProgramDescription(channelName);
                epgEvent.setChannelNumber(channelNumber);
                
                mProgramReserveAlertList.add(epgEvent);
            }
        }
        managedQuery.close();
//        //TODO 测试
//        for (int i = 1; i <= 2; i++) {
//        	 EpgEvent epgEvent = new EpgEvent();
//        	 epgEvent.setProgramName("麻辣女兵"+i);
//        	 epgEvent.setProgramDescription("CCTV "+i);
//        	 mProgramReserveAlertList.add(epgEvent);
//		}
        
        Log.d("songwenxuan", " mProgramResoveAlertList.size() : "+mProgramReserveAlertList.size());
        if(mProgramReserveAlertList.size()>0){
            if(mProgramReserveAlertList.size()>1){
                mProgramReserveAlertUpImageView.setVisibility(View.VISIBLE);
//                mProgramReserveAlertDownImageView.setVisibility(View.VISIBLE);
                refreshProgramReserveAlertData(mProgramReserveAlertTag);
            }else {
                mProgramReserveAlertUpImageView.setVisibility(View.INVISIBLE);
//                mProgramReserveAlertDownImageView.setVisibility(View.INVISIBLE);
                refreshProgramReserveAlertData(mProgramReserveAlertTag);
            }
        }
	}
	
	@Override
	public void processMessage(Object sender, DvbMessage msg) {
		mViewController = (ViewController) sender;
		switch (msg.what) {
		case ViewMessage.SHOW_PROGRAM_RESERVE_ALERT:
		    mIsSelected = false;
			Log.d("songwenxuan", "---showProgramReserveAlertWindow---" );
			Log.d("songwenxuan", "---本地时间---"+System.currentTimeMillis()+"  --  "+new Date().toLocaleString());
			Log.d("songwenxuan", "---getUtcTime()---" + getUtcTime()+"  --  "+new Date(getUtcTime()).toLocaleString());
			showProgramReserveAlertWindow();
			break;
		default:
			break;
		}
	}
	
	private long getTimeZoneCompensate() {
        return (long)((8-mTimeZone)*3600*1000);
    }

}