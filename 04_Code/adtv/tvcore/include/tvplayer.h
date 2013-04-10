#ifndef TVPLAYER_H_
#define TVPLAYER_H_

#if defined(WIN32)

#ifdef TVPLAYER_EXPORTS
#define TVPLAYER_API __declspec(dllexport)
#else
#define TVPLAYER_API __declspec(dllimport)
#endif

#else
#define TVPLAYER_API 
#endif 

#include "tvcomm.h"

#ifdef __cplusplus
extern "C"{
#endif 

// ��ʼ��������
TVPLAYER_API bool tvplayer_init();
// ȥ��ʼ����������Դ
TVPLAYER_API void tvplayer_uninit();
// ����
TVPLAYER_API bool tvplayer_play();
// ֹͣ
TVPLAYER_API bool tvplayer_stop();
//ֹͣ���� 20130128
TVPLAYER_API int tvplayer_stop_descramb();
// ע����Ϣ�ص��ӿ�
TVPLAYER_API bool tvplayer_register_notify_cb(TVNOTIFY pCBNotify);
// �л�Ƶ��
TVPLAYER_API bool tvplayer_set_service(const DVBService& service);
// ��ȡTuner״̬
TVPLAYER_API bool tvplayer_get_tuner_status(TunerSignal& status);
//��ȡSTBid
TVPLAYER_API bool tvplayer_getSTBId(char * pId, U8 buflen);
// ����ģ��״̬(1:����,0:����...)
TVPLAYER_API bool tvplayer_set_module_state(int iState);

// ������ʾ����
TVPLAYER_API bool tvplayer_setDisplayRect(int x,int y,int width,int height);
// ���û������
TVPLAYER_API bool tvplayer_setDisplayZoomMode(IN const int iMode);
// ��������Ƶ֡
TVPLAYER_API bool tvplayer_cleanVideoFrame(bool bClean);

// ��Ƶ�����:��ʾ(0),����(1),�ú�(2)��Ƶ��
TVPLAYER_API bool tvplayer_setVideoLayer(int iState);

// ���þ���(AudioChannel)
TVPLAYER_API int  tvplayer_mute(bool bMute);
// ���û��ȡ�豸��������
TVPLAYER_API int  tvplayer_getVolume();
TVPLAYER_API int  tvplayer_setVolume(float volume);
// ��������ģʽ
TVPLAYER_API bool tvplayer_setAudioChannel(AudioStereoMode iMode);
// ��ȡ��ǰ����ģʽ
TVPLAYER_API AudioStereoMode tvplayer_getAudioChannel();
// ���õ�ǰƵ������/����
TVPLAYER_API bool tvplayer_setAudioLang(U8 uIndex);
// ��ȡ��ǰƵ������/����
TVPLAYER_API U8   tvplayer_getAudioLang();
// ��ȡTDT/TOTʱ��
TVPLAYER_API bool tvplayer_getTDTTime(U32& time);
TVPLAYER_API bool tvplayer_getTOTTime(U32& time,U32& offset);

// ������ʼEPG�������� 
// ע:���ṩ�ֶ���ʽ��ʼһ��EPG����������(�����ڷǵ���ֱ����ʹ��),
// �ڲ����л�Ƶ����Ƶ�㷢���ı��ʱ����������EPG��������
TVPLAYER_API bool tvplayer_startEpgSearch(const TuningParam& tuning,const EITEventType evtType);
// ����ȡ��EPG��������
TVPLAYER_API bool tvplayer_cancelEpgSearch();
// ��ȡEPGȫ������
TVPLAYER_API bool tvplayer_getEpgData(EPGDataBaseT& epgs);
// ��ȡָ�������µĽ�Ŀ(�¼�)��Ϣ
TVPLAYER_API bool tvplayer_getEpgDataBySID(const U16 iServiceId,EpgEventSet& events);
// ��ȡָ��������ָ��ʱ��εĽ�Ŀ(�¼�)��Ϣ
TVPLAYER_API bool tvplayer_getEpgDataByDuration(const U16 iServiceId,EpgEventSet& events,const U32 iStartTime,const U32 iEndTime);
// ������Ż���������
TVPLAYER_API bool tvplayer_resetTSBuffer();

#ifdef  __cplusplus
}
#endif 

#endif //defined(TVPLAYER_H_)
