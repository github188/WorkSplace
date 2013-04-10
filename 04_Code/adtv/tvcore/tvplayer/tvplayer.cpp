#include "tvplayer.h"
#include "tvcontroller.h"

tvcontroller *gpController_ = 0;

// 初始化播放器
bool tvplayer_init()
{
	bool bRet = false;
	
	gpController_ = new tvcontroller();
	if(gpController_){
		bRet = true;
	}

	return bRet;
}

// 去初始化播放器资源
void tvplayer_uninit()
{
	if(gpController_){
		delete gpController_;
		gpController_ = 0;
	}
}

// 开始播放
bool tvplayer_play()
{
	bool bRet = false;
	if(gpController_){
		bRet = (0 == gpController_->play()) ? true : false;
	}
	
	return bRet ;
}

// 停止播放
bool tvplayer_stop()
{
	bool bRet = false;
	if(gpController_){
		bRet = (0 == gpController_->stop()) ? true : false;
	}

	return bRet;
}

int tvplayer_stop_descramb()
{
	int ret = 1;
	if(gpController_){
		ret = gpController_->stopDescramb();
	}
	return ret;
}

// 注册消息回调接口
bool tvplayer_register_notify_cb(IN TVNOTIFY pCBNotify)
{
	bool bRet = false;
	if(gpController_){
		gpController_->set_player_notify_callback(pCBNotify);
		bRet = true ;
	}

	return bRet;
}

// 切换频道
bool tvplayer_set_service(const DVBService& service)
{
	bool bRet = false;
	if(gpController_){
		bRet = (0 == gpController_->change_channel(service)) ? true : false;
	}

	return bRet;
}

// 获取Tuner状态
bool tvplayer_get_tuner_status(TunerSignal& status)
{	
	bool bRet = false;
	if(gpController_){
		bRet = gpController_->get_tuner_status(status);
	}
	return bRet;
}

bool tvplayer_getSTBId(char * pId, U8 buflen)
{
	bool bRet = false;
	if(gpController_)
	{
		bRet = gpController_->getSTBId(pId, buflen);
	}
	return bRet;
}
// 设置模块状态(1:搜索,0:电视...)
bool tvplayer_set_module_state(int iState)
{
	bool bRet = false;
	if(gpController_){
		gpController_->setModuleState(iState);
		bRet = true;
	}

	return bRet;
}

// 设置显示区域
bool tvplayer_setDisplayRect(int x,int y,int width,int height)
{
	bool bRet = false;
	if(gpController_){
		bRet = gpController_->setDisplayRect(x,y,width,height);
	}

	return bRet;
}

// 设置画面比例
bool tvplayer_setDisplayZoomMode(IN const int iMode)
{	
	bool bRet = false;
	if(gpController_){
		bRet = gpController_->setDisplayZoomMode(iMode);
	}

	return bRet;
}

// 清除最后视频帧
bool tvplayer_cleanVideoFrame(bool bClean)
{
	bool bRet = false;
	if(gpController_){
		bRet = gpController_->cleanVideoFrame(bClean);
	}

	return bRet;
}

bool tvplayer_setVideoLayer(int iState)
{
	bool bRet = false;
	if(gpController_)
	{
		bRet = gpController_->setVideoLayer(iState);
	}
	return bRet;
}

// 设置静音(AudioChannel)
int  tvplayer_mute(bool bMute)
{
	int iRet = -1;
	if(gpController_){
		iRet = gpController_->setMute(bMute);
	}

	return iRet;
}

// 设置或获取设备的主音量
int  tvplayer_getVolume()
{
	int iRet = -1;
	if(gpController_){
		iRet = gpController_->getVolume();
	}

	return iRet;
}

int  tvplayer_setVolume(float volume)
{
	int iRet = -1;
	if(gpController_){
		iRet = gpController_->setVolume(volume);
	}
	
	return iRet;
}

// 设置声道模式
bool tvplayer_setAudioChannel(AudioStereoMode iMode)
{
	bool bRet = false;
	if(gpController_){
		bRet = gpController_->setAudioChannel(iMode);
	}
	
	return bRet;
}

// 获取当前声道模式
AudioStereoMode tvplayer_getAudioChannel()
{
	AudioStereoMode eAudioMode = AUDIO_MODE_STEREO;
	if(gpController_){
		eAudioMode = gpController_->getAudioChannel();
	}
	
	return eAudioMode;
}

bool tvplayer_setAudioLang(U8 uIndex)
{
	bool bRet = false;
	if(gpController_){
		bRet = gpController_->setAudioLang(uIndex);
	}
	
	return bRet;
}
// 获取当前频道语种/伴音
U8   tvplayer_getAudioLang()
{
	U8 uRet = 0;
	if(gpController_){
		uRet = gpController_->getAudioLang();
	}

	return uRet;
}
// 获取TDT/TOT时间
bool tvplayer_getTDTTime(U32& time)
{
	bool bRet = false;
	if(gpController_){
		bRet = gpController_->getTDTTime(time);
	}

	return bRet;
}
bool tvplayer_getTOTTime(U32& time,U32& offset)
{
	bool bRet = false;
	if(gpController_){
		bRet = gpController_->getTOTTime(time,offset);
	}
	
	return bRet;
}

// 主动开始EPG搜索任务
// 注:除提供手动方式开始一个EPG搜索任务外(可以在非电视直播下使用),
// 内部在切换频道且频点发生改变的时候主动开启EPG搜索任务
bool tvplayer_startEpgSearch(const TuningParam& tuning,const EITEventType evtType)
{
	bool bRet = false;
	if(gpController_){
		bRet = gpController_->startEpgSearch(tuning,evtType);
	}

	return bRet;
}
// 主动取消EPG搜索任务
bool tvplayer_cancelEpgSearch()
{
	bool bRet = false;
	if(gpController_){
		bRet = gpController_->cancelEpgSearch();
	}

	return bRet;
}
// 获取EPG全部数据
bool tvplayer_getEpgData(EPGDataBaseT& epgs)
{
	bool bRet = false;
	if(gpController_){
		bRet = gpController_->getEpgData(epgs);
	}

	return bRet;
}
// 获取指定服务下的节目(事件)信息
bool tvplayer_getEpgDataBySID(const U16 iServiceId,EpgEventSet& events)
{
	bool bRet = false;
	if(gpController_){
		bRet = gpController_->getEpgDataBySID(iServiceId,events);
	}

	return bRet;
}
// 获取指定服务下指定时间段的节目(事件)信息
bool tvplayer_getEpgDataByDuration(const U16 iServiceId,EpgEventSet& events,const U32 iStartTime,const U32 iEndTime)
{
	bool bRet = false;
	if(gpController_){
		bRet = gpController_->getEpgDataByDuration(iServiceId,events,iStartTime,iEndTime);
	}

	return bRet;
}

// 清除播放缓冲区数据
bool tvplayer_resetTSBuffer()
{
	bool bRet = false;
	if(gpController_){
		bRet = gpController_->resetTSBuffer();
	}
	return bRet;
}
