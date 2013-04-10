package com.joysee.adtv.controller;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Instrumentation;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DateFormatUtil;
import com.joysee.adtv.common.DefaultParameter;
import com.joysee.adtv.common.DefaultParameter.AudioIndex;
import com.joysee.adtv.common.DefaultParameter.DisplayMode;
import com.joysee.adtv.common.DefaultParameter.DvbIntent;
import com.joysee.adtv.common.DefaultParameter.EmailStatus;
import com.joysee.adtv.common.DefaultParameter.FavoriteFlag;
import com.joysee.adtv.common.DefaultParameter.NotificationAction.CA;
import com.joysee.adtv.common.DefaultParameter.NotificationAction.TunerStatus;
import com.joysee.adtv.common.DefaultParameter.OsdStatus;
import com.joysee.adtv.common.DefaultParameter.ServiceType;
import com.joysee.adtv.common.DefaultParameter.ViewMessage;
import com.joysee.adtv.common.DvbLog;
import com.joysee.adtv.common.DvbMessage;
import com.joysee.adtv.common.ToastUtil;
import com.joysee.adtv.db.Channel;
import com.joysee.adtv.logic.ChannelManager;
import com.joysee.adtv.logic.DVBPlayManager;
import com.joysee.adtv.logic.DVBPlayManager.OnMonitorListener;
import com.joysee.adtv.logic.EPGManager;
import com.joysee.adtv.logic.SettingManager;
import com.joysee.adtv.logic.bean.AudioTrackMode;
import com.joysee.adtv.logic.bean.CaFinger;
import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.adtv.logic.bean.EpgEvent;
import com.joysee.adtv.logic.bean.MiniEpgNotify;
import com.joysee.adtv.logic.bean.NETEventInfo;
import com.joysee.adtv.logic.bean.OsdInfo;
import com.joysee.adtv.logic.bean.ProgramType;
import com.joysee.adtv.logic.bean.Transponder;
import com.joysee.adtv.ui.EpgGuideWindow;
import com.joysee.adtv.ui.Menu;
/**
 * 用于播放控制
 * @author wangguohua
 */
	public final class DvbController extends BaseController implements OnMonitorListener, OnAudioFocusChangeListener {
    
	private static final String TAG = "DvbController";
	private static final String MTAG = "DvbControllerEpg";
	private static final DvbLog log = new DvbLog(TAG, DvbLog.DebugType.D);
	
	private Context mContext;
	private DVBPlayManager mDvbPlayManager;
	private ChannelManager mChannelManager;
	private EPGManager mEPGManager;
	private AudioManager mAudioManager;	
	private SettingManager mSettingManager;
	private static int mCurrentChannelIndex = 0;
	private static int mCurrentPlayType = ServiceType.TV; 
    private int mLastChannelIndex;
	private HandlerThread mSwitchChannelThread;
	private SChannelHandler mSwitchChannelHandler;
	private static boolean mPause;
	private ViewController mViewController;
	private double mTimeZone;
	private boolean mIsBackSeeing = false;
	private boolean mIsSwitchPlayMode = false;
	private static final int CHANGE_MODE_TIMEOUT = 2500;//防止连续切换TV/BC
	
	private static final int SWITCH_CHANNEL_SPECIAL = 0;
	private static final int SWITCH_CHANNEL_NOW = 1;
	private static final int CHANNEL_NUM_OR_INFO = 2;
	
	private static final int SWITCH_CHAHHEN_DEDLAY_TIME = 410;
	private boolean mDvbInitRet;
	
	private Handler mHandler = new Handler();
	
	private static boolean isTunerEnable = true;
	private static boolean isCAEnable = true;
	
	private static int mLastCAParam = -1;
	private static int mLastTunerParam = -1;
	
//	private static long switchChannelbegin = -1;
//	private static boolean isTurnOnDeinterlace = true;
//	private static boolean isTurnOffDeinterlace = true;
	
	//防止回看和数字键延迟连续切台
	private static final int BACKSEE_TIMEOUT = 3000;
	/* 预约广播接收 */
	private ProgramReservesBroadcastReceiver programReservesBroadcastReceiver;
	
    public DvbController(Context context) {
        this.mContext = context;
        mDvbPlayManager = DVBPlayManager.getInstance(context);
        mChannelManager = ChannelManager.getInstance();
        mEPGManager = EPGManager.getInstance();
    }

    public void init() {
    	log.D("Controller init begin -----");
    	mViewController = new ViewController(this);
        mSettingManager = SettingManager.getSettingManager();
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mDvbInitRet = initDVBPlayer();
        
		mSwitchChannelThread = new HandlerThread("switch-channel");
		mSwitchChannelThread.start();
		mSwitchChannelHandler = new SChannelHandler(mSwitchChannelThread.getLooper());
		log.D("Controller init end -----");
    }

	private boolean initDVBPlayer() {
		int ret = mDvbPlayManager.init();
		log.D("mDvbPlayManager.init()   =    " + ret);
		if(ret < 0){
			log.D("mDvbPlayManager.init()<0  *******************************************");
			dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.DVB_INIT_FAILED));
			return false;
		}
		mDvbPlayManager.initChannels();
		mTimeZone = (double)(TimeZone.getDefault().getRawOffset())/1000/3600;
		return true;
	}

//    public void onStart() {
//    	mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
//    	IntentFilter filter = new IntentFilter();
//		filter.addAction("program alarm");
//		if (programReservesBroadcastReceiver == null) {
//			programReservesBroadcastReceiver = new ProgramReservesBroadcastReceiver();
//		}
//		mContext.registerReceiver(programReservesBroadcastReceiver, filter);
//    }

    public void play(Intent intent) {
    	mDvbPlayManager.init();
    	log.D("Controller play begin -------");
    	mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
    	mDvbPlayManager.setOnMonitorListener(this);
    	mCurrentPlayType = ServiceType.TV;
    	mPause = false;
		resolveIntentForBroadcast(intent);
		mHandler.postDelayed(new Runnable() {
			public void run() {
				mDvbPlayManager.setWinSize(0, 0, 1920, 1080);
			}
		}, 600);
		switchDeinterlace(true);
		mLastChannelIndex = -1;
		final DvbService retService = mDvbPlayManager.playLast(mCurrentPlayType);
		if (mCurrentPlayType == ServiceType.TV){
			mCurrentChannelIndex = DVBPlayManager.mCurrentTVChannelIndex;
			dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.START_PLAY_TV));
		}else{
			mCurrentChannelIndex = DVBPlayManager.mCurrentBCChannelIndex;
		}
		
		/** 进入应用时，初始化epg */
		if (retService != null) {
			showChannelInfo();
		}
		
		mDvbPlayManager.setDisplayMode(mDvbPlayManager.getDisplayMode());
		log.D("isTunerEnable = " + isTunerEnable);
		isTunerEnable = mDvbPlayManager.getTunerSignalStatus() == 0 ? true : false;//判断信号
		log.D( "after refreshTunerStatus isTunerEnable = " + isTunerEnable);
		Object[] objs = {mCurrentPlayType,isDVBChannelEnable(mCurrentPlayType), isTunerEnable, isCAEnable};
		if(mDvbInitRet)
			dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_ERROR_NOTIFY, objs));
		
		regitsterReserveProgram();
		mHandler.postDelayed(new Runnable() {
            public void run() {
                showOsdFromXml();
                showEmailIconFromXml();
            }
        }, 1000);
		
		log.D("Controller play end -------");
    }

//    public void onPause() {
//    	mPause = true;
//    	dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.STOP_PLAY));
//    	switchDeinterlace(false);
//    	mSwitchChannelHandler.removeMessages(SChannelHandler.SWITCH_TO_SPECIAL_CHANNEL);
//    	unRegisterReservePrograme();
//    	log.D("DVBMainActiity onPause  end");
//    }
    
    public void stop() {
    	log.D("Controller stop begin -------");
    	mPause=true;
    	dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.STOP_PLAY));
    	switchDeinterlace(false);
    	mSwitchChannelHandler.removeMessages(SWITCH_CHANNEL_SPECIAL);
    	mDvbPlayManager.stop();
    	mAudioManager.abandonAudioFocus(this);
    	unRegisterReservePrograme();
		log.D("Controller stop end -------");
    }

    public void uninit() {
    	log.D("Controller uninit begin----");
		if (mSwitchChannelThread != null && mSwitchChannelThread.isAlive()) {
    			log.D("sChannelThread.quit();");
    			mSwitchChannelThread.quit();
		}else {
			log.D("sChannelThread != null  = " + (mSwitchChannelThread != null));
			log.D("sChannelThread.isAlive()  = " + (mSwitchChannelThread.isAlive()));
		}
		mDvbPlayManager.uninit();
		log.D("Controller uninit end----");
    }
    
    private void regitsterReserveProgram() {
    	IntentFilter filter = new IntentFilter();
		filter.addAction("program alarm");
		if (programReservesBroadcastReceiver == null) {
			programReservesBroadcastReceiver = new ProgramReservesBroadcastReceiver();
		}
		mContext.registerReceiver(programReservesBroadcastReceiver, filter);
		
	    mTimeZone = (double)(TimeZone.getDefault().getRawOffset())/1000/3600;
		mHandler.postDelayed(new Runnable() {
			public void run() {
				Cursor programReserveBindCursor = mContext.getContentResolver().query(Channel.URI.TABLE_RESERVES, null, null,
						null, Channel.TableReservesColumns.STARTTIME);

				if (programReserveBindCursor == null || programReserveBindCursor.isClosed()
						|| programReserveBindCursor.getCount() <= 0) {
					return;
				}

				while (programReserveBindCursor.moveToNext()) {
					int id = programReserveBindCursor.getInt(programReserveBindCursor
							.getColumnIndex(Channel.TableReservesColumns.ID));
					long startTime = (long) programReserveBindCursor.getInt(programReserveBindCursor
							.getColumnIndex(Channel.TableReservesColumns.STARTTIME));
					String realTimeStr = mSettingManager.nativeGetTimeFromTs();
					log.D("get UTC time is " + realTimeStr);
					String[] splitTime = realTimeStr.split(":");
					long realTime = Long.valueOf(splitTime[0])*1000 + (long)((8-mTimeZone)*3600*1000);
					long timeCompensate = System.currentTimeMillis() - realTime;
					log.D("timecompensate=" + timeCompensate);
					if (Long.valueOf(splitTime[0]) == 0) {
						log.D("time commpenstae is 0");
						timeCompensate = 0;
					}
					if ((startTime * 1000 + (long)((8-mTimeZone)*3600*1000)) < realTime) {
						continue;
					} else {
						addReServeProgramToAlam(id, startTime*1000 + (long)((8-mTimeZone)*3600*1000) + timeCompensate - 60 * 1000);
						log.D("reserve success! reserve date="+DateFormatUtil.getDateFromMillis(
                                startTime*1000+timeCompensate)+DateFormatUtil.getTimeFromMillis(startTime*1000+timeCompensate));
					}
				}
				programReserveBindCursor.close();
			}
		}, 5000);
	}
    
	/** 添加预约闹钟 */
	private void addReServeProgramToAlam(int id, long startTime) {
		log.D("addReServeProgramToAlam() -- reserveId=" + id + ", startTime=" + startTime);
		Intent intent = new Intent("program alarm");
		PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC_WAKEUP, startTime, pendingIntent);
	}
	
	/** 删除预约闹钟 */
	private void removeReserveProgramFromAlarm(int id) {
		log.D("removeReserveProgramFromAlarm(), enter! reserveId=" + id);
		Intent intent = new Intent("program alarm");
		PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);
	}
	
	private void unRegisterReservePrograme() {
		mContext.unregisterReceiver(programReservesBroadcastReceiver);
		
		Cursor programReserveUnbindCursor = mContext.getContentResolver().query(Channel.URI.TABLE_RESERVES, null, null, null,
				Channel.TableReservesColumns.STARTTIME);
		log.D("programReserveUnbindCursor  =  " + programReserveUnbindCursor);
		while (programReserveUnbindCursor != null && programReserveUnbindCursor.moveToNext()) {
			int id = programReserveUnbindCursor.getInt(programReserveUnbindCursor
					.getColumnIndex(Channel.TableReservesColumns.ID));
			removeReserveProgramFromAlarm(id);
		}
		if (programReserveUnbindCursor != null)
			programReserveUnbindCursor.close();
	}
	
	/** 预约BroadcastReceiver */
	class ProgramReservesBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("songwenxuan", "---接受到预约提示广播---" );
			dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.SHOW_PROGRAM_RESERVE_ALERT));
		}
	}

	
   /**
    *  切台Handler
    */
	private class SChannelHandler extends Handler {
		public SChannelHandler(Looper looper) {
			super(looper);
		}
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SWITCH_CHANNEL_SPECIAL: 
				Log.d(MTAG, "--- SWITCH_TO_SPECIAL_CHANNEL ---");
				if(!mPause){
					DvbService destService = mDvbPlayManager.getChannelByIndex(mCurrentPlayType, mCurrentChannelIndex,DVBPlayManager.NATIVE_INDEX);
					mDvbPlayManager.switchToSpecialChannel(mCurrentPlayType,destService,mCurrentChannelIndex);
				}
				break;
			case SWITCH_CHANNEL_NOW:
				Log.d(MTAG, "--- SWITCH_CHANNEL_NOW ---");
				DvbService destService = mDvbPlayManager.getChannelByIndex(mCurrentPlayType, mCurrentChannelIndex,DVBPlayManager.NATIVE_INDEX);
				mDvbPlayManager.switchToSpecialChannel(mCurrentPlayType,destService,mCurrentChannelIndex);
				break;
			}
			super.handleMessage(msg);
		}
	}
	
	/** 设置喜爱频道后刷新频道信息 */
	public void refreshChannelInfo() {
		dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.FINISHED_FAVORITE_CHANNEL_SET));
	}
	
	/** 底层回调信息 */
	public void onMonitor(int monitorType, Object message) {
		log.D("DvbController onMonitor");
		log.D("onMonitor monitorType = " + monitorType);
		log.D("onMonitor message = " + message);
		switch (monitorType) {
		case DefaultParameter.NotificationAction.NOTIFICATION_TVNOTIFY_TUNER_SIGNAL:
			int code = (Integer) message;
			mLastTunerParam = code;
			isTunerEnable = code == TunerStatus.ACTION_TUNER_UNLOCKED ? true : false;
			Object[] objs = {mCurrentPlayType,isDVBChannelEnable(mCurrentPlayType), isTunerEnable, isCAEnable};
			dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_ERROR_NOTIFY, objs));
			break;
		case DefaultParameter.NotificationAction.NOTIFICATION_TVNOTIFY_BUYMSG:
			int mage = (Integer) message;
			mLastCAParam = mage;
			isCAEnable = mage == CA.NOTIFICATION_ACTION_CA_MESSAGE_CANCEL_TYPE ? true : false;
			Object[] objects = {mCurrentPlayType,isDVBChannelEnable(mCurrentPlayType), isTunerEnable, isCAEnable};
			dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_ERROR_NOTIFY, objects));
			break;
		// 底层回调OSD信息
		case DefaultParameter.NotificationAction.NOTIFICATION_TVNOTIFY_OSD:
			OsdInfo osdInfo = (OsdInfo) message;
			log.D(" --------------- getShowPosition = " + osdInfo.getShowPosition());
			if (null != osdInfo) {
				int state = osdInfo.getShowOrHide();
				log.D(" ----- struct.getShowOrHide() = " + state);
				switch (state) {
				// 隐藏OSD
				case OsdStatus.OSD_HIDE:
					dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_OSD_INFO_HIDE, osdInfo.getShowPosition()));
					break;
				// 显示OSD
				case OsdStatus.OSD_SHOW:
					dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_OSD_INFO_SHOW, osdInfo.getShowPosition(),0,osdInfo.getOsdMsg()));
					break;
				}
			}
			break;
		// 底层回调邮件消息
		case DefaultParameter.NotificationAction.NOTIFICATION_TVNOTIFY_MAIL_NOTIFY:
			int msgobject = (Integer) message;
			int msg = msgobject >> 24;
			int id = msgobject & 0x00ffffff;
			log.D(" ---------------  email notify msg =  " + msg + " id = " + id);
			switch (msg) {
			// 隐藏邮件通知
			case EmailStatus.EMAIL_HIDE:
				dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_EMAIL_HIDE));
				break;
			// 显示邮件通知
			case EmailStatus.EMAIL_SHOW:
				dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_EMAIL_SHOW));
				break;
			// 邮件空间已满
			case EmailStatus.EMAIL_NOSPACE:
				dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_EMAIL_BLINK));
				break;
			}
			break;
		/** PAT/PMT/SDT信息改变 */
		case DefaultParameter.NotificationAction.NOTIFICATION_TVNOTIFY_UPDATE_SERVICE:
			DvbService service = (DvbService) message;
			mDvbPlayManager.setService(service);
			mDvbPlayManager.nativeSyncServiceToProgram(mCurrentChannelIndex, service);
			break;
		// nit bat改变 搜台
		case DefaultParameter.NotificationAction.NOTIFICATION_TVNOTIFY_UPDATE_PROGRAM:
			dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_UPDATE_PROGRAM_NB_CHANGE));
			break;
		//指纹显示
		case DefaultParameter.NotificationAction.NOTIFICATION_TVNOTIFY_SHOW_FINGERPRINT:
		    CaFinger finger = (CaFinger) message;
            if (finger != null){
                dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_FINGER_INFO_SHOW, finger.getCard_id(), finger.getEcmp_id(), null));
            }
		    break;
		
		case DefaultParameter.NotificationAction.NOTIFICATION_TVNOTIFY_MINEPG:
			MiniEpgNotify pf = (MiniEpgNotify) message;
			DvbService currentService = mDvbPlayManager.getChannelByIndex(mCurrentPlayType, mCurrentChannelIndex,DVBPlayManager.NATIVE_INDEX);
			if(currentService!= null){
				final int sid = currentService.getServiceId();
				final int pfSid = pf.getServiceId();
				log.D("showPf()   sid = " + sid + "  pfSid = " + pfSid);
				if (sid == pfSid) {
					dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_CHANNEL_MINIEPG,sid,0,pf));
				}
			}
		    break;
		case DefaultParameter.NotificationAction.NOTIFICATION_TVNOTIFY_EPGCOMPLETE:
			dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.RECEIVE_EPG_CALLBACK));
			break;
		}
	}
	
	@Override
	public void onAudioFocusChange(int focusChange) {
	}
	/**
	 * 解析intent信息 确定播放方式
	 * @param intent
	 */
	private void resolveIntentForBroadcast(Intent intent) {
		if(intent != null){
			final String broadcast = intent.getStringExtra(DvbIntent.INTENT_SHOW_MENU);
			if (broadcast != null && broadcast.length() > 0) {
				if (broadcast.equals(DvbIntent.INTENT_BROADCAST)) {
					switchPlayMode(ServiceType.BC);
				}else if(broadcast.equals(DvbIntent.INTENT_TELEVISION)){
					switchPlayMode(ServiceType.TV);
				}
			}
		}
	}

	public void switchPlayMode(int type) {
		switchPlayMode(type, -1);
	}
	
	private void switchPlayMode(int type, int channelNum) {
		if (type == mCurrentPlayType){
			mIsSwitchPlayMode = false;
			return;
		}
		mDvbPlayManager.stop();
		mCurrentPlayType = type;
		mLastChannelIndex = -1;
		DvbService retService = null;
		if (type == ServiceType.TV) {
			log.D("call mDvbPlayManager.playLast()");
			retService = mDvbPlayManager.playLast(mCurrentPlayType);
			mCurrentChannelIndex = DVBPlayManager.mCurrentTVChannelIndex;
			dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.START_PLAY_TV));
		} else if (type == ServiceType.BC) {
			retService = mDvbPlayManager.playLast(mCurrentPlayType);
			mCurrentChannelIndex = DVBPlayManager.mCurrentBCChannelIndex;
			dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.START_PLAY_BC));//显示BC背景
		} else {
			throw new RuntimeException("INVALID PLAYMODE!");
		}
		dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.SWITCH_PLAY_MODE));
		
		Object[] objs = {mCurrentPlayType,isDVBChannelEnable(mCurrentPlayType), isTunerEnable, isCAEnable};
		dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_ERROR_NOTIFY, objs));
		if (retService != null) {
			Log.d("msg", "TV/BV切换 = "+retService.getLogicChNumber());
			dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.SWITCH_CHANNEL , retService.getLogicChNumber()));
		}
	}
	
	/** 当回到看电视界面时从XML保存的OSD状态值中恢复OSD状态 */
    private void showOsdFromXml() {
        SharedPreferences sharedPreferences = mContext
                .getSharedPreferences(DefaultParameter.PREFERENCE_NAME,
                        Activity.MODE_PRIVATE);
        if (null != sharedPreferences) {
            int status = sharedPreferences.getInt(
                    DefaultParameter.TpKey.KEY_OSD_STATE,
                    OsdStatus.STATUS_INVALID);
            String msg = sharedPreferences.getString(
                    DefaultParameter.TpKey.KEY_OSD_MSG, "");
            int position = sharedPreferences.getInt(
                    DefaultParameter.TpKey.KEY_OSD_POSITION, 0);
            log.D(" showOsdFromXml status = " + status + " msg = " + msg
                    + " position = " + position);
            switch (status) {
            case OsdStatus.STATUS_INVALID:
                break;
            case OsdStatus.OSD_HIDE:
                break;
            case OsdStatus.OSD_SHOW:
            	dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_OSD_INFO_SHOW,position,0,msg));
                break;
            }
        }else {
            log.E("sharedPreferences is null , showOsdFromXml Error!");
        }
    }
    
    /** 当回到看电视界面时从XML保存的邮件状态值中恢复邮件状态 */
    private void showEmailIconFromXml() {
        SharedPreferences sharedPreferences = mContext
                .getSharedPreferences(DefaultParameter.PREFERENCE_NAME,
                        Activity.MODE_PRIVATE);
        if (null != sharedPreferences) {
            int status = sharedPreferences.getInt(
                    DefaultParameter.TpKey.KEY_EMAIL_STATE,
                    EmailStatus.STATUS_INVALID);
            log.D(" showEmailIconFromXml status = " + status);
            switch (status) {
            case EmailStatus.STATUS_INVALID:
                break;
            case EmailStatus.EMAIL_HIDE:
            	dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_EMAIL_HIDE));
                break;
            case EmailStatus.EMAIL_SHOW:
            	dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_EMAIL_SHOW));
                break;
            case EmailStatus.EMAIL_NOSPACE:
            	dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_EMAIL_BLINK));
                break;
            }
        } else {
            log.E(" sharedPreferences is null , showEmailIconFromXml Error!");
        }
    }
	
	/**
	 * 发送指定键值的模拟按键
	 * @param key 	指定的键值
	 */
	public void doInjectKeyEvent(final int key) {
		log.D("doInjectKeyEvent key = " + key);
		mSwitchChannelHandler.post(new Runnable() {
			public void run() {
				Instrumentation inst = new Instrumentation();
				inst.sendKeyDownUpSync(key);
			}
		});
	}
	
	/** 判断是否有频道 */
	public boolean isChannelEnable(){
		return isDVBChannelEnable(mCurrentPlayType);
	}
	
	public boolean isTVService(){
		return mCurrentPlayType == ServiceType.TV;
	}
	
	public boolean isBCService(){
		return mCurrentPlayType == ServiceType.BC;
	}
	public int getCurrentIndexInChannelWindow(){
		if(mCurrentPlayType == ServiceType.TV){
			return DVBPlayManager.mTvIndexList.indexOf(mCurrentChannelIndex);
		}else if(mCurrentPlayType == ServiceType.BC){
			return DVBPlayManager.mBcIndexList.indexOf(mCurrentChannelIndex);
		}else{
			return 0;
		}
	}
	public int getChannelNum(){
		return mDvbPlayManager.getChannelNum(mCurrentPlayType, mCurrentChannelIndex);
	}
	/**
	 * 加入喜爱频道
	 * @param isAdd true添加 false删除
	 */
	public void setChannelFavorite(boolean isAdd){
		if(isAdd){
			mDvbPlayManager.setChannelFavorite(mCurrentPlayType,mCurrentChannelIndex, FavoriteFlag.FAVORITE_YES);
		}else{
			mDvbPlayManager.setChannelFavorite(mCurrentPlayType,mCurrentChannelIndex, FavoriteFlag.FAVORITE_NO);
		}
	}
	public void removeChannelFavorite(int channelNumber,int type){
		int nativeIndex = mChannelManager.nativeGetService(channelNumber,new DvbService(), type);
		Log.d("sognwenxuan", "nativeindex = " + nativeIndex);
		mDvbPlayManager.setChannelFavorite(type,nativeIndex, FavoriteFlag.FAVORITE_NO);
	}
	
	public void addChannelFavorite(int channelNumber,int type){
		int nativeIndex = mChannelManager.nativeGetService(channelNumber,new DvbService(), type);
		Log.d("sognwenxuan", "nativeindex = " + nativeIndex);
		mDvbPlayManager.setChannelFavorite(type,nativeIndex, FavoriteFlag.FAVORITE_YES);
	}
	
	
	public DvbService getCurrentChannel(){
		return mDvbPlayManager.getChannelByIndex(mCurrentPlayType, mCurrentChannelIndex,DVBPlayManager.NATIVE_INDEX);
	}
	public int getDisplayMode(){
		return mDvbPlayManager.getDisplayMode();
	}

	public void setDisplayMode(int mode) {
		if (mode == 0) {
			mDvbPlayManager.setDisplayMode(DisplayMode.DISPLAYMODE_NORMAL);
		} else if (mode == 1) {
			mDvbPlayManager.setDisplayMode(DisplayMode.DISPLAYMODE_FULL);
		}
	}
	
	public int getSoundTrack(){
		DvbService service = mDvbPlayManager.getChannelByIndex(mCurrentPlayType, mCurrentChannelIndex,DVBPlayManager.NATIVE_INDEX);
		if(service!=null){
			log.D("getSoundTrack(): " + service.getSoundTrack() + "   channel number = " + service.getLogicChNumber());
			return service.getSoundTrack();
		}
		return 0;
	}
	public void setSoundTrack(int position){
		int resId = -1;
		int soundTrack = -1;
		if (position == 0) {
			soundTrack = AudioTrackMode.AUDIO_MODE_STEREO.ordinal();
			resId = R.string.dvb_sound_track_all;
		} else if (position == 1) {
			soundTrack = AudioTrackMode.AUDIO_MODE_LEFT.ordinal();
			resId = R.string.dvb_sound_track_left;
		} else if (position == 2) {
			soundTrack = AudioTrackMode.AUDIO_MODE_RIGHT.ordinal();
			resId = R.string.dvb_sound_track_right;
		}
		mDvbPlayManager.setSoundTrack(mCurrentPlayType, mCurrentChannelIndex, soundTrack);
		String content = mContext.getString(resId);
		dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.FINISHED_SOUNDTRACK_AUDIOINDEX_SET, 0, 0, content));
	}
	
	public void switchPlayMode() {
		if(mIsSwitchPlayMode){
			Log.d(MTAG, " --- switchPlayMode --- return ");
			return;
		}
		if (mCurrentPlayType == ServiceType.TV){
			switchPlayMode(ServiceType.BC);
		}
		else{
			switchPlayMode(ServiceType.TV);
		}
		mIsSwitchPlayMode = true;
		mHandler.postDelayed(new Runnable() {
			public void run() {
				Log.d(MTAG, "--- mIsBackSeeing = false; ---");
				mIsSwitchPlayMode = false;
			}
		},CHANGE_MODE_TIMEOUT);
	}

	public void backSee() {
		if (mLastChannelIndex != -1) {
			mIsBackSeeing = true;
			int tempChannelIndex = mCurrentChannelIndex;
			mCurrentChannelIndex = mLastChannelIndex;
			switchChannel(mLastChannelIndex);
			mLastChannelIndex = tempChannelIndex;
			mHandler.postDelayed(new Runnable() {
				public void run() {
					mIsBackSeeing = false;
				}
			},BACKSEE_TIMEOUT);
		}
	}

	
	//  信息键、初始化显示
	public void showChannelInfo() {
		dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_CHANNEL_INFO_KEY));
	}
	//用户通过遥控器或键盘输入数字，但还没有按确定切台，或者没到3秒自动切台时，需要显示频道号时调用。
	private void inputNumber(int number){
		dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_NUMBER_KEY, number));
	}
	
	public int getCurrentAudioIndexSum(){
		return mDvbPlayManager.getCurrentAudioIndexSum();
	}

	public int getAudioIndex() {
		return  mDvbPlayManager.getAudioIndex(mCurrentPlayType, mCurrentChannelIndex);
	}

	public void setAudioIndex(int position) {
		int audioIndex = -1;
		int resId = -1;
		if (position == 0) {
			audioIndex = AudioIndex.AUDIOINDEX_0;
			resId = R.string.dvb_audio_index_0;
		} else if (position == 1) {
			audioIndex = AudioIndex.AUDIOINDEX_1;
			resId = R.string.dvb_audio_index_1;
		} else if (position == 2) {
			audioIndex = AudioIndex.AUDIOINDEX_2;
			resId = R.string.dvb_audio_index_2;
		}
		mDvbPlayManager.setAudioIndex(mCurrentPlayType, mCurrentChannelIndex, audioIndex);
		String content = mContext.getString(resId);
		dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.FINISHED_SOUNDTRACK_AUDIOINDEX_SET, 0, 0, content));
	}

	public void changeVolume(int value) {
		boolean mute = mAudioManager.isStreamMute(AudioManager.STREAM_MUSIC);
		if (mute){
			mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
		}
		int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) + value;
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_SHOW_UI);
	}
    
	protected void dispatchMessage(Object sender,DvbMessage msg) {
    	  int count  = mViews.size();
          for(int i=0;i<count;i++){
        	  mViews.get(i).processMessage(sender,msg);
          }
	}
	/**
	 * 用户通过按遥控器或键盘的数字键调用到切台时调用的方法
	 * @param channelNum 频道号
	 * @param now true 立刻切台
	 */
	public void switchChannelFromNum(int channelNum,boolean now){
		if(!now){
			return;
		}
		switchChannelFromNum(channelNum);
	}
    /**
     * 用户通过按遥控器或键盘的数字键调用到切台时调用的方法
     * @param channelNum 频道号
     */
    public void switchChannelFromNum(int channelNum){
    	int tempChannelIndex = mCurrentChannelIndex;
    	mCurrentChannelIndex = mChannelManager.nativeGetService(channelNum, new DvbService(),mCurrentPlayType);
    	log.D("switchChannelFromNum(): index  = " + mCurrentChannelIndex);
    	if (mCurrentChannelIndex < 0) {
			dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.ERROR_WITHOUT_CHANNEL));
			mCurrentChannelIndex = tempChannelIndex;
			return;
		}
    	mLastChannelIndex = tempChannelIndex;
    	switchChannel(mCurrentChannelIndex);
    }
    
    public void switchChannelFromNum(int serviceType,int num) {
		if(mCurrentPlayType !=serviceType)
			switchPlayMode();
		switchChannelFromNum(num);
    }

    public void reInitChannels(){
    	mDvbPlayManager.reInitChannels();
    }
	public void showSoundTrackSetting() {
		dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.SHOW_SOUNDTRACK_WINDOWN));
	}

	public void showMainMenu() {
		dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.SHOW_MAIN_MENU));
	}

	public void showFavoriteChannel() {
		dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.SHOW_FAVORITE_CHANNEL_WINDOWN));
	}

	public void showChannelList() {
		dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.SHOW_CHANNEL_LIST_WINDOWN));
	}
	
	
	/**
	 * 获取当前频道数量 主菜单使用
	 */
	public int getTotalChannelSize(){
		return DVBPlayManager.getTotalChannelSize(mCurrentPlayType);
	}
	/**
	 * 通过角标获取Service 0开始
	 */
	public DvbService getChannelByListIndex(int index){
		
//		return mDvbPlayManager.getChannelByIndex(mCurrentPlayType, index,DVBPlayManager.LIST_INDEX);
		
		return mDvbPlayManager.getChannelByIndex(mCurrentPlayType, index,DVBPlayManager.LIST_INDEX);
	}
	
	/**
	 * 通过角标获取Service 
	 * @param index 底层角标
	 * @return
	 */
	public DvbService getChannelByNativeIndex(int index){
		return mDvbPlayManager.getChannelByIndex(mCurrentPlayType,index,DVBPlayManager.NATIVE_INDEX);
	}

	public ArrayList<Integer> getFavouriteIndex() {
		return mDvbPlayManager.getFavouriteIndex(mCurrentPlayType);
	}
	
	/**
	 * 喜爱频道 使用
	 * @param channelIndex
	 * @return
	 */
	public DvbService getChannelByChannelIndex(int channelIndex) {
		return mDvbPlayManager.getChannelByIndex(mCurrentPlayType,channelIndex,DVBPlayManager.NATIVE_INDEX);
	}
	
	public MiniEpgNotify getPfByEpg(DvbService service){
		Log.d("songwenxuan","getMiniEpg()....");
		ArrayList<EpgEvent> epgList = new ArrayList<EpgEvent>();
		String tsTime = mSettingManager.nativeGetTimeFromTs();
		String[] strings = tsTime.split(":");
		long utcTime = Long.valueOf(strings[0]) * 1000;
		Log.d("songwenxuan","utcTime = "+ utcTime);
		mDvbPlayManager.nativeGetEpgDataByDuration(service.getServiceId(), epgList, utcTime, utcTime + 3600*8*1000);
		if(epgList.size() > 2){
			Log.d("songwenxuan","epgList.size()>2");
			MiniEpgNotify epgNotify = new MiniEpgNotify();
			epgNotify.setCurrentEventName(epgList.get(0).getProgramName());
			epgNotify.setCurrentEventStartTime(epgList.get(0).getStartTime());
			epgNotify.setCurrentEventEndTime(epgList.get(0).getEndTime());
			
			epgNotify.setNextEventName(epgList.get(1).getProgramName());
			epgNotify.setNextEventStartTime(epgList.get(1).getStartTime());
			epgNotify.setNextEventEndTime(epgList.get(1).getEndTime());
			Log.d("songwenxuan",epgNotify.toString());
			return epgNotify;
		}else{
			Log.d("songwenxuan","epgList.size() = " + epgList.size());
			return null;
		}
	}
	
	public int getPf(int serviceId,MiniEpgNotify pf){
		return mDvbPlayManager.nativeGetPFEventInfo(serviceId, pf);
	}

	public void showProgramGuide() {
		dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.SHOW_PROGRAM_GUIDE));
	}

	public void showProgramReserve() {
		dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.SHOW_PROGRAM_RESERVE));
	}
	public void getEpgDataByDuration(ArrayList<EpgEvent> mEpgEvents,
			long startTime, long endTime) {
		int serviceId = getCurrentChannel().getServiceId();
		mDvbPlayManager.nativeGetEpgDataByDuration(serviceId, mEpgEvents, startTime, endTime);
	}

	/**
	 * 供Launcher使用
	 */
	public void init2() {
		mDvbPlayManager = DVBPlayManager.getInstance(mContext);
		mDvbPlayManager.init();
	}
	
	/**
	 * 供Launcher使用
	 */
	public void uninit2(){
		mDvbPlayManager.uninit();
	}
	
	/**
	 * 供Launcher使用
	 * @param lastChannelNum
	 */
	public void playlast2(int lastChannelNum){
		mDvbPlayManager.playLast(ServiceType.TV,lastChannelNum);
	}
	
	/**
	 * 供Launcher使用
	 */
	public void stop2(){
		mDvbPlayManager.stop();
	}
	
    public static boolean isDVBChannelEnable(int type){
    	int totalNum = DVBPlayManager.getTotalChannelSize(type);
    	return totalNum > 0 ? true : false;
    }
    
    /**
	 * @param true 开deinterlace 隔行
	 */
	public static void switchDeinterlace(boolean enable) {
		String val = enable ? "add default decoder deinterlace amvideo" : "add default decoder amvideo";
		try {
			File f = new File("/sys/class/vfm/map");
			FileWriter fw = new FileWriter(f);
			BufferedWriter buf = new BufferedWriter(fw);
			buf.write("rm default");
			buf.close();

			f = new File("/sys/class/vfm/map");
			fw = new FileWriter(f);
			buf = new BufferedWriter(fw);
			buf.write(val);
			buf.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getLastTunerParam() {
		return mLastTunerParam;
	}

	public int getLastCAParam() {
		return mLastCAParam;
	}

	public void getAllChannelIndex(ArrayList<Integer> mTvChannelList,
			ArrayList<Integer> mBcChannelList) {
		mDvbPlayManager.getAllChannelIndex(mTvChannelList,mBcChannelList);
	}

	public void switchChannelFromIndex(int serviceType,int index) {
		if(mCurrentPlayType !=serviceType)
			switchPlayMode();
		mLastChannelIndex = mCurrentChannelIndex;
		mCurrentChannelIndex = index;
		switchChannel(index);
	}
	
	/**
	 * 获取当前节目信息 频道列表使用
	 * @param serviceId
	 * @return
	 */
	public NETEventInfo getCurrentProgramInfo(int serviceId) {
		long startTime = getUtcTime();
		ArrayList<Integer> programIdList = new ArrayList<Integer>();
		mEPGManager.nativeGetPFEvent(serviceId, startTime, programIdList);
		NETEventInfo eventInfo = new NETEventInfo();
		if(programIdList.size()>0){
			mEPGManager.nativeGetProgramInfo(programIdList.get(0), eventInfo);
			log.D(" dvbcontroller  "+eventInfo);
			return eventInfo;
		}
		else
			return null;
	}
	
	public String getChannelIconPath(int serviceId) {
		String path = mEPGManager.nativeGetTVIcons(serviceId);
		log.D(" dvbcontroller iconPath  "+path);
		if(path !=null && !path .equals(""))
			return path;
		return null;
	}
	
	
	public void switchChannel(int index){
		
		if (index < 0) {
			dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.ERROR_WITHOUT_CHANNEL));
			mCurrentChannelIndex = mLastChannelIndex;
			return;
		}
		DvbService destService = mDvbPlayManager.getChannelByIndex(mCurrentPlayType, index,DVBPlayManager.NATIVE_INDEX);
		if (destService.getLogicChNumber() <= 0) {
			dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.ERROR_WITHOUT_CHANNEL));
			return;
		}
		
		dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.SWITCH_CHANNEL , destService.getLogicChNumber()));
		
		if(mSwitchChannelHandler!=null){
			Message msg = mSwitchChannelHandler.obtainMessage();
			msg.what = SWITCH_CHANNEL_SPECIAL;
			msg.obj = destService;
			msg.arg1 = index;
			mSwitchChannelHandler.removeMessages(SWITCH_CHANNEL_SPECIAL);
			mSwitchChannelHandler.sendMessageDelayed(msg, SWITCH_CHAHHEN_DEDLAY_TIME);
		}
		
	}
	
	
	/** 响应遥控数字键切台 */
	public void executeNumKey(int keyNum , boolean now){
		Log.d(MTAG, "--- executeNumKey ---");
		if(!now){
			//即时更新右上角台号
			inputNumber(keyNum);
		}else{
			//切台
			int tempChannelIndex = mCurrentChannelIndex;
	    	mCurrentChannelIndex = mChannelManager.nativeGetService(keyNum, new DvbService(),mCurrentPlayType);
	    	log.D("switchChannelFromNum(): index  = " + mCurrentChannelIndex);
	    	if (mCurrentChannelIndex < 0) {
				dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.ERROR_WITHOUT_CHANNEL));
				mCurrentChannelIndex = tempChannelIndex;
				return;
			}
	    	mLastChannelIndex = tempChannelIndex;
	    	trunChannel();
	    	dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.SWITCH_CHANNEL, keyNum));
		}
	}
	
	public void acionUp(){
		dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.SWITCH_CHANNEL_UPDOWN, ViewMessage.KEYCODE_ACTION_UP));
	}
    /**下一个频道*/
    public void nextChannel(){
    	mLastChannelIndex = mCurrentChannelIndex;
		mCurrentChannelIndex = mChannelManager.nativeGetNextDVBService(mCurrentChannelIndex, mCurrentPlayType);
		
		int num = mDvbPlayManager.getChannelNum(mCurrentPlayType, mCurrentChannelIndex);
		dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.SWITCH_CHANNEL_UPDOWN, ViewMessage.KEYCODE_UP,num,null));
		trunChannelByNoAnimation();
    }
    /**上一个频道*/
    public void previousChannel(){
    	mLastChannelIndex = mCurrentChannelIndex;
    	mCurrentChannelIndex = mChannelManager.nativeGetLastDVBService(mCurrentChannelIndex, mCurrentPlayType);
    	
    	int num = mDvbPlayManager.getChannelNum(mCurrentPlayType, mCurrentChannelIndex);
    	dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.SWITCH_CHANNEL_UPDOWN, ViewMessage.KEYCODE_DOWN,num,null));
    	trunChannelByNoAnimation();
    }
    
    /** 切台 */
    private void trunChannel(){
    	Log.d(TAG, "---trunChannel---");
		mSwitchChannelHandler.removeMessages(SWITCH_CHANNEL_NOW);
		mSwitchChannelHandler.sendEmptyMessage(SWITCH_CHANNEL_NOW);
    }
    /** 无动画切台 */
    private void trunChannelByNoAnimation(){
    	Log.d(TAG, "---trunChannelByNoAnimation---");
    	mSwitchChannelHandler.removeMessages(SWITCH_CHANNEL_NOW);
		mSwitchChannelHandler.sendEmptyMessageDelayed(SWITCH_CHANNEL_NOW,SWITCH_CHAHHEN_DEDLAY_TIME);
    }
    /** getTime */
    public long getUtcTime() {
        String utcTimeStr = mSettingManager.nativeGetTimeFromTs();
        String[] utcTime = utcTimeStr.split(":");
        long currentTimeMillis = Long.valueOf(utcTime[0])*1000;
        return currentTimeMillis;
    }

    
    /** 根据position获取对应频道的信息 */
    public synchronized NETEventInfo[] getItemInfo(int serviceId){
    	
    	NETEventInfo[] infos = new NETEventInfo[2];
    	infos[0] = new NETEventInfo();
    	infos[1] = new NETEventInfo();
    	double progress = 0;

    	ArrayList<Integer> programIdList = new ArrayList<Integer>();
    	mEPGManager.nativeGetPFEvent(serviceId, getUtcTime(), programIdList);
    	
    	Log.d(MTAG, "---------programIdList.size = "+ programIdList.size() +"----------------");
    	if(programIdList.size() >= 2){
    		mEPGManager.nativeGetProgramInfo(programIdList.get(0) , infos[0]);
    		mEPGManager.nativeGetProgramInfo(programIdList.get(1) , infos[1]);
    		Log.d(MTAG, "infos[0].getDuration() = "+infos[0].getDuration());
    		if(infos[0].getDuration()>0){
    			progress = ((double)(getUtcTime()/1000 - infos[0].getBegintime())) / (double)infos[0].getDuration();
    		}
    	}
    	
    	infos[0].setProgress((int)(progress*100));
    	
    	Log.d(MTAG, "getItemInfo = "+infos[0].getChannelId() + " | "+infos[0].getChannelName() +" | "+infos[0].getBegintime() + " | " +infos[0].getDescription() + " | " +
    			infos[0].getEname()+" | "+infos[0].getImgPath() + " | "+ infos[0].getNibble2() + " | "+ infos[0].getProgress() +" | "+infos[0].getTypeName());
    	
    	if(infos[0].getBegintime() == 0){
    		infos[0].setBegintime(-8*3600);
    	}
    	if(infos[1].getBegintime() == 0){
    		infos[1].setBegintime(-8*3600);
    	}
    	return infos;
    }
    
	
	public int getFavoriteCount(){
		return DVBPlayManager.mFavoriteIndexList.size();
	}
	
	public int getCurrentPosition(){
		if(mCurrentPlayType == ServiceType.BC){
			return DVBPlayManager.mBcIndexList.indexOf(mCurrentChannelIndex);
		}else{
			return DVBPlayManager.mTvIndexList.indexOf(mCurrentChannelIndex);
		}
	}
	
	public int getCurrentPosition1(){
	    if(mCurrentPlayType == ServiceType.BC){
	        return 0;
	    }else{
	        return DVBPlayManager.mTvIndexList.indexOf(mCurrentChannelIndex);
	    }
    }
	
	public ArrayList<Integer> getProgramIdListBySid(int serviceId, long startTime, long endTime){
	    ArrayList<Integer> programIdList=new ArrayList<Integer>();
	    mEPGManager.nativeGetProgramIdListBySid(serviceId, startTime, endTime, programIdList);
	    return programIdList;
	}
	
	public NETEventInfo getProgramInfo(int programId){
	    NETEventInfo eventInfo=new NETEventInfo();
	    mEPGManager.nativeGetProgramInfo(programId, eventInfo);
	    eventInfo.setBegintime(eventInfo.getBegintime()*1000+EpgGuideWindow.TimeOffset);
	    eventInfo.setDuration(eventInfo.getDuration()*1000);
	    return eventInfo;
	}
	
	public NETEventInfo getProgramInfo_LiveGuide(int programId){
        NETEventInfo eventInfo=new NETEventInfo();
        mEPGManager.nativeGetProgramInfo(programId, eventInfo);
        return eventInfo;
    }
	
	public String getTVIcons(int serviceId){
	    return mEPGManager.nativeGetTVIcons(serviceId);
	}
	
	public int getAllChannelCount(){
		return DVBPlayManager.mTvIndexList.size() + DVBPlayManager.mBcIndexList.size();
	}

	public void showLiveGuide() {
		dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.SHOW_LIVE_GUIDE));
	}
	/**
     * 通过id获取分类列表
     * @param id 0xff 返回一级分类 ID 和分类名称;否则返回二级分类 ID 和分类名称
     * @param programTypeList 分类信息list，参数作为返回值
     * @return
     */
    public int getProgramTypes(int id, ArrayList<ProgramType> programTypeList) {
        return mEPGManager.nativeGetProgramTypes(id, programTypeList);
    }
    /**
     * 根据一级分类programId,获取正在播放或者即将播放的节目ID List
     * @param programId
     * @param startTime
     * @param endTime
     * @param programIdList 包含返回节目信息
     * @return
     */
    public int getProgramIdListByType(int programId, long startTime, long endTime, ArrayList<Integer> programIdList){
        return mEPGManager.nativeGetProgramIdListByType(programId, startTime, endTime, programIdList);
    }

	public void downF12() {
		dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.SHOW_PROGRAM_RESERVE_ALERT));
	}

	public void showMenu(Menu menu) {
		dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.SHOW_MAIN_MENU));
	}

	public int getmCurrentPlayType() {
		return mCurrentPlayType;
	}

	public boolean isBCPlaying() {
		return mCurrentPlayType == ServiceType.BC;
	}
	
	public int startEPGSearch(Transponder param , int type){
		return mEPGManager.nativeStartEPGSearch(param, type);
	}
	
	public int cancelEPGSearch(){
		return mEPGManager.nativeCancelEPGSearch();
	}

	public void exitLiveGuide() {
		dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.EXIT_LIVE_GUIDE));
	}

	public void exitEpgGuide() {
		dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.EXIT_PROGRAM_GUIDE));
	}
}
