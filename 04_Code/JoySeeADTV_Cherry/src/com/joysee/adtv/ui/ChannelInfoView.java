package com.joysee.adtv.ui;


import android.content.Context;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DefaultParameter.ServiceType;
import com.joysee.adtv.common.DefaultParameter.ViewMessage;
import com.joysee.adtv.common.DvbMessage;
import com.joysee.adtv.common.DefaultParameter;
import com.joysee.adtv.controller.ViewController;
import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.adtv.logic.bean.MiniEpgNotify;

public class ChannelInfoView extends FrameLayout implements IDvbBaseView{
	
	private TextView mChannelNum;
	private TextView mChannelName;
	private ImageView mChannelFavorite;
	private ImageView mChannelMoney;
	private TextView mMiniSoundTrack;
	private TextView mMiniLanguage;

	private TextView mCurrentProgramName;
	private TextView mCurrentProgramTimeRange;

	private TextView mNextProgramName;
	private TextView mNextProgramTimeRange;

	private ImageView mVolumePestype;
	private String STREMPTY = "";
	
	String[] adjust_items = null;
	String[] adjust_lans;

	public ChannelInfoView(Context context) {
		this(context, null);
	}

	public ChannelInfoView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ChannelInfoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mChannelNum = (TextView) findViewById(R.id.dvb_channel_num);
		mChannelName = (TextView) findViewById(R.id.dvb_channel_name);
		mChannelFavorite = (ImageView) findViewById(R.id.dvb_channelFavorite);
		mChannelMoney = (ImageView) findViewById(R.id.dvb_channelMoney);
		mMiniSoundTrack = (TextView) findViewById(R.id.dvb_mini_soundtrack);
		mMiniLanguage = (TextView) findViewById(R.id.dvb_miniepg_language);
		mCurrentProgramName = (TextView) findViewById(R.id.dvb_current_programname);
		mCurrentProgramTimeRange = (TextView) findViewById(R.id.dvb_current_program_timerange);
		mNextProgramName = (TextView) findViewById(R.id.dvb_next_programname);
		mNextProgramTimeRange = (TextView) findViewById(R.id.dvb_next_program_timerange);
		mVolumePestype = (ImageView) findViewById(R.id.dvb_volume_pestype);
		
		adjust_lans = getResources().getStringArray(R.array.language_items);
		adjust_items = getResources().getStringArray(R.array.soundtrack_items);
	}

	private void resetChannelInfo(boolean all){
		mChannelNum.setText(STREMPTY);
		mChannelName.setText(STREMPTY);
		mChannelFavorite.setVisibility(View.INVISIBLE);
		mChannelMoney.setVisibility(View.INVISIBLE);
		mMiniSoundTrack.setText(STREMPTY);
		mMiniLanguage.setText(STREMPTY);
		if(all){
			mCurrentProgramName.setText(STREMPTY);
			mCurrentProgramTimeRange.setText(STREMPTY);
			mNextProgramName.setText(STREMPTY);
			mNextProgramTimeRange.setText(STREMPTY);
		}
		mVolumePestype.setVisibility(View.INVISIBLE);
	}
	/**
	 * @param service
	 * @param all true 重置所有频道信息 fasle 保留epg信息
	 */
	public void setChannelInfoBasic(DvbService service,boolean all) {
		if (service != null) {
			resetChannelInfo(all);
			final int channelNum = service.getLogicChNumber();
			final String channelName = service.getChannelName();
			final int favourite = service.getFavorite();
			final int videoEcmPid = service.getVideoEcmPid();
			final int audioEcmPid = service.getAudioEcmPid0();
			final int audioPesType = service.getAudioType0();
			final int audioLanguageIndex = service.getAudioIndex();
			final int audioVolumeIndex = service.getSoundTrack();
			
			mChannelNum.setText("" + String.format(DefaultParameter.CHANNEL_NUMBER_FORMAT,channelNum));
			mChannelName.setText(channelName != null ? channelName : STREMPTY);
			mChannelFavorite.setVisibility(favourite == 0 ? View.INVISIBLE : View.VISIBLE);
			if (videoEcmPid == 0 && audioEcmPid == 0) {
				mChannelMoney.setVisibility(View.INVISIBLE);
			} else {
				mChannelMoney.setVisibility(View.VISIBLE);
			}
			if (audioPesType == 6 || audioPesType == 122 || audioPesType == 129) {
				mVolumePestype.setVisibility(View.VISIBLE);
			} else {
				mVolumePestype.setVisibility(View.INVISIBLE);
			}
			mMiniLanguage.setText(adjust_lans[audioLanguageIndex]);
			mMiniSoundTrack.setText(adjust_items[audioVolumeIndex]);
		}
	}
	
	private static final int HIDE = 1;
	private static final int SHOW_TIME = 5000;
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			setVisibility(View.INVISIBLE);
		};
	};
	
	public void setChannelInfoMiniepg(MiniEpgNotify pf) {
		if (pf != null) {
			mCurrentProgramName.setText("" + pf.getCurrentEventName());
			mNextProgramName.setText("" + pf.getNextEventName());

			String startTime = (String) DateFormat.format("kk:mm", pf.getCurrentEventStartTime() * 1000 + 8 * 3600000);
			String endTime = (String) DateFormat.format("kk:mm", pf.getCurrentEventEndTime() * 1000 + 8 * 3600000);
			mCurrentProgramTimeRange.setText("" + startTime + "~" + endTime);

			startTime = "";
			endTime = "";
			startTime = (String) DateFormat.format("kk:mm", pf.getNextEventStartTime() * 1000 + 8 * 3600000);
			endTime = (String) DateFormat.format("kk:mm", pf.getNextEventEndTime() * 1000 + 8 * 3600000);
			mNextProgramTimeRange.setText("" + startTime + "~" + endTime);
		}
	}

    @Override
    public void processMessage(Object sender,DvbMessage msg) {
    	ViewController viewController = (ViewController) sender;
        switch (msg.what) {
        case ViewMessage.RECEIVED_CHANNEL_INFO_KEY:
        	DvbService tService = viewController.getCurrentChannel();
        	setChannelInfoBasic(tService,true);
        	MiniEpgNotify tPf = new MiniEpgNotify(); 
			int ret = viewController.getPf(tService.getServiceId(), tPf);
			if(ret >= 0)
				setChannelInfoMiniepg(tPf);
			setVisibility(View.VISIBLE);
			if((tService.getServiceType()&0x0F) == ServiceType.TV){
				mHandler.removeMessages(HIDE);
				mHandler.sendEmptyMessageDelayed(HIDE, SHOW_TIME);
			}
			break;
		case ViewMessage.SWITCH_CHANNEL:
			DvbService service = viewController.getCurrentChannel();
			setChannelInfoBasic(service,true);
			MiniEpgNotify miniEpg = viewController.getPfFromEPG(service);
			if(miniEpg != null)
				setChannelInfoMiniepg(miniEpg);
			setVisibility(View.VISIBLE);
			mHandler.removeMessages(HIDE);
			if((service.getServiceType()&0x0F) == ServiceType.TV){
				mHandler.sendEmptyMessageDelayed(HIDE, SHOW_TIME);
			}
			break;
	    case ViewMessage.FINISHED_SOUNDTRACK_AUDIOINDEX_SET:
	    case ViewMessage.FINISHED_FAVORITE_CHANNEL_SET:
	    	setChannelInfoBasic(viewController.getCurrentChannel(),false);
			break;
	    case ViewMessage.SWITCH_PLAY_MODE:
	    case ViewMessage.STOP_PLAY:
	    	mHandler.removeMessages(HIDE);
			mHandler.sendEmptyMessage(HIDE);
			break;
	    case ViewMessage.RECEIVED_CHANNEL_MINIEPG:
	    	MiniEpgNotify pf = (MiniEpgNotify) msg.obj;
	    	setChannelInfoMiniepg(pf);
			break;
        }
    }
}
