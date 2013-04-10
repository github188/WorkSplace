#include "tvplayer.h"
#include "tvcontroller.h"

tvcontroller *gpController_ = 0;

// ��ʼ��������
bool tvplayer_init()
{
	bool bRet = false;
	
	gpController_ = new tvcontroller();
	if(gpController_){
		bRet = true;
	}

	return bRet;
}

// ȥ��ʼ����������Դ
void tvplayer_uninit()
{
	if(gpController_){
		delete gpController_;
		gpController_ = 0;
	}
}

// ��ʼ����
bool tvplayer_play()
{
	bool bRet = false;
	if(gpController_){
		bRet = (0 == gpController_->play()) ? true : false;
	}
	
	return bRet ;
}

// ֹͣ����
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

// ע����Ϣ�ص��ӿ�
bool tvplayer_register_notify_cb(IN TVNOTIFY pCBNotify)
{
	bool bRet = false;
	if(gpController_){
		gpController_->set_player_notify_callback(pCBNotify);
		bRet = true ;
	}

	return bRet;
}

// �л�Ƶ��
bool tvplayer_set_service(const DVBService& service)
{
	bool bRet = false;
	if(gpController_){
		bRet = (0 == gpController_->change_channel(service)) ? true : false;
	}

	return bRet;
}

// ��ȡTuner״̬
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
// ����ģ��״̬(1:����,0:����...)
bool tvplayer_set_module_state(int iState)
{
	bool bRet = false;
	if(gpController_){
		gpController_->setModuleState(iState);
		bRet = true;
	}

	return bRet;
}

// ������ʾ����
bool tvplayer_setDisplayRect(int x,int y,int width,int height)
{
	bool bRet = false;
	if(gpController_){
		bRet = gpController_->setDisplayRect(x,y,width,height);
	}

	return bRet;
}

// ���û������
bool tvplayer_setDisplayZoomMode(IN const int iMode)
{	
	bool bRet = false;
	if(gpController_){
		bRet = gpController_->setDisplayZoomMode(iMode);
	}

	return bRet;
}

// ��������Ƶ֡
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

// ���þ���(AudioChannel)
int  tvplayer_mute(bool bMute)
{
	int iRet = -1;
	if(gpController_){
		iRet = gpController_->setMute(bMute);
	}

	return iRet;
}

// ���û��ȡ�豸��������
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

// ��������ģʽ
bool tvplayer_setAudioChannel(AudioStereoMode iMode)
{
	bool bRet = false;
	if(gpController_){
		bRet = gpController_->setAudioChannel(iMode);
	}
	
	return bRet;
}

// ��ȡ��ǰ����ģʽ
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
// ��ȡ��ǰƵ������/����
U8   tvplayer_getAudioLang()
{
	U8 uRet = 0;
	if(gpController_){
		uRet = gpController_->getAudioLang();
	}

	return uRet;
}
// ��ȡTDT/TOTʱ��
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

// ������ʼEPG��������
// ע:���ṩ�ֶ���ʽ��ʼһ��EPG����������(�����ڷǵ���ֱ����ʹ��),
// �ڲ����л�Ƶ����Ƶ�㷢���ı��ʱ����������EPG��������
bool tvplayer_startEpgSearch(const TuningParam& tuning,const EITEventType evtType)
{
	bool bRet = false;
	if(gpController_){
		bRet = gpController_->startEpgSearch(tuning,evtType);
	}

	return bRet;
}
// ����ȡ��EPG��������
bool tvplayer_cancelEpgSearch()
{
	bool bRet = false;
	if(gpController_){
		bRet = gpController_->cancelEpgSearch();
	}

	return bRet;
}
// ��ȡEPGȫ������
bool tvplayer_getEpgData(EPGDataBaseT& epgs)
{
	bool bRet = false;
	if(gpController_){
		bRet = gpController_->getEpgData(epgs);
	}

	return bRet;
}
// ��ȡָ�������µĽ�Ŀ(�¼�)��Ϣ
bool tvplayer_getEpgDataBySID(const U16 iServiceId,EpgEventSet& events)
{
	bool bRet = false;
	if(gpController_){
		bRet = gpController_->getEpgDataBySID(iServiceId,events);
	}

	return bRet;
}
// ��ȡָ��������ָ��ʱ��εĽ�Ŀ(�¼�)��Ϣ
bool tvplayer_getEpgDataByDuration(const U16 iServiceId,EpgEventSet& events,const U32 iStartTime,const U32 iEndTime)
{
	bool bRet = false;
	if(gpController_){
		bRet = gpController_->getEpgDataByDuration(iServiceId,events,iStartTime,iEndTime);
	}

	return bRet;
}

// ������Ż���������
bool tvplayer_resetTSBuffer()
{
	bool bRet = false;
	if(gpController_){
		bRet = gpController_->resetTSBuffer();
	}
	return bRet;
}
