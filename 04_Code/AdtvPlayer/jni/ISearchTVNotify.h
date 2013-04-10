#ifndef NOVELSUPERTV_ISEARCHTVNOTIFY_DEFINE_H
#define NOVELSUPERTV_ISEARCHTVNOTIFY_DEFINE_H
#include <vector>
using namespace std;
#include "tvcomm.h"

#include "ISearchTVNotify.h"

// 搜索频道回调接口
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

#define MSG_NEW_EMAIL					(0x18)	// 新邮件到达通知
#define MSG_SHOW_OSD					(0x30)	// 显示OSD通知
#define MSG_HIDE_OSD					(0x31)	// 隐藏OSD通知
#define MSG_CANNOT_PLAY_PROGRAM			(0x32)	// 节目无法播放通知
#define MSG_SHOW_FINGERPRINT			(0x33)	// 显示指纹(防伪码)通知

#define MSG_TUNER_SIGNAL_INTERRUPT		(0x80)	// Tuner信号中断通知
#define MSG_TUNER_SIGNAL_RESTORATION	(0x81)	// Tuner信号恢复通知

#define MSG_EPG_UPDATED					(0x82)	// 节目单发生更新通知(EPG分析完成,数据适配完成)

#define MSG_AUDIO_STREAM_CHANGE			(0x83)	// 
#define MSG_TVCORE_EXCEPTION			(0x84)  // TVCORE模块异常通知

#endif  // defined(NOVELSUPERTV_ISEARCHTVNOTIFY_DEFINE_H)
