#ifndef NOVELSUPERTV_ISEARCHTVNOTIFY_DEFINE_H
#define NOVELSUPERTV_ISEARCHTVNOTIFY_DEFINE_H
#include <vector>
using namespace std;
#include "tvcomm.h"

#include "ISearchTVNotify.h"

// ����Ƶ���ص��ӿ�
//##ModelId=4D89C938000E
class ISearchTVNotify
{
  public:
	virtual ~ISearchTVNotify(){};
    
	virtual void OnDVBService(std::vector<DVBService> &services)=0;
	
    virtual void OnProgress(U32 iPercent)=0;
	
	virtual void OnTunerInfo(const TuningParam& tuning,const TunerSignal& signal)=0;
	 
	virtual void OnSTVComplete()=0;

};

#define MSG_NEW_EMAIL					(0x18)	// ���ʼ�����֪ͨ
#define MSG_SHOW_OSD					(0x30)	// ��ʾOSD֪ͨ
#define MSG_HIDE_OSD					(0x31)	// ����OSD֪ͨ
#define MSG_CANNOT_PLAY_PROGRAM			(0x32)	// ��Ŀ�޷�����֪ͨ
#define MSG_SHOW_FINGERPRINT			(0x33)	// ��ʾָ��(��α��)֪ͨ

#define MSG_TUNER_SIGNAL_INTERRUPT		(0x80)	// Tuner�ź��ж�֪ͨ
#define MSG_TUNER_SIGNAL_RESTORATION	(0x81)	// Tuner�źŻָ�֪ͨ

#define MSG_EPG_UPDATED					(0x82)	// ��Ŀ����������֪ͨ(EPG�������,�����������)

#define MSG_AUDIO_STREAM_CHANGE			(0x83)	// 
#define MSG_TVCORE_EXCEPTION			(0x84)  // TVCOREģ���쳣֪ͨ

#endif  // defined(NOVELSUPERTV_ISEARCHTVNOTIFY_DEFINE_H)
