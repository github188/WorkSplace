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

// 初始化播放器
TVPLAYER_API bool tvplayer_init();
// 去初始化播放器资源
TVPLAYER_API void tvplayer_uninit();
// 播放
TVPLAYER_API bool tvplayer_play();
// 停止
TVPLAYER_API bool tvplayer_stop();
//停止解扰 20130128
TVPLAYER_API int tvplayer_stop_descramb();
// 注册消息回调接口
TVPLAYER_API bool tvplayer_register_notify_cb(TVNOTIFY pCBNotify);
// 切换频道
TVPLAYER_API bool tvplayer_set_service(const DVBService& service);
// 获取Tuner状态
TVPLAYER_API bool tvplayer_get_tuner_status(TunerSignal& status);
//获取STBid
TVPLAYER_API bool tvplayer_getSTBId(char * pId, U8 buflen);
// 设置模块状态(1:搜索,0:电视...)
TVPLAYER_API bool tvplayer_set_module_state(int iState);

// 设置显示区域
TVPLAYER_API bool tvplayer_setDisplayRect(int x,int y,int width,int height);
// 设置画面比例
TVPLAYER_API bool tvplayer_setDisplayZoomMode(IN const int iMode);
// 清除最后视频帧
TVPLAYER_API bool tvplayer_cleanVideoFrame(bool bClean);

// 视频层控制:显示(0),隐藏(1),置黑(2)视频层
TVPLAYER_API bool tvplayer_setVideoLayer(int iState);

// 设置静音(AudioChannel)
TVPLAYER_API int  tvplayer_mute(bool bMute);
// 设置或获取设备的主音量
TVPLAYER_API int  tvplayer_getVolume();
TVPLAYER_API int  tvplayer_setVolume(float volume);
// 设置声道模式
TVPLAYER_API bool tvplayer_setAudioChannel(AudioStereoMode iMode);
// 获取当前声道模式
TVPLAYER_API AudioStereoMode tvplayer_getAudioChannel();
// 设置当前频道语种/伴音
TVPLAYER_API bool tvplayer_setAudioLang(U8 uIndex);
// 获取当前频道语种/伴音
TVPLAYER_API U8   tvplayer_getAudioLang();
// 获取TDT/TOT时间
TVPLAYER_API bool tvplayer_getTDTTime(U32& time);
TVPLAYER_API bool tvplayer_getTOTTime(U32& time,U32& offset);

// 主动开始EPG搜索任务 
// 注:除提供手动方式开始一个EPG搜索任务外(可以在非电视直播下使用),
// 内部在切换频道且频点发生改变的时候主动开启EPG搜索任务
TVPLAYER_API bool tvplayer_startEpgSearch(const TuningParam& tuning,const EITEventType evtType);
// 主动取消EPG搜索任务
TVPLAYER_API bool tvplayer_cancelEpgSearch();
// 获取EPG全部数据
TVPLAYER_API bool tvplayer_getEpgData(EPGDataBaseT& epgs);
// 获取指定服务下的节目(事件)信息
TVPLAYER_API bool tvplayer_getEpgDataBySID(const U16 iServiceId,EpgEventSet& events);
// 获取指定服务下指定时间段的节目(事件)信息
TVPLAYER_API bool tvplayer_getEpgDataByDuration(const U16 iServiceId,EpgEventSet& events,const U32 iStartTime,const U32 iEndTime);
// 清除播放缓冲区数据
TVPLAYER_API bool tvplayer_resetTSBuffer();

#ifdef  __cplusplus
}
#endif 

#endif //defined(TVPLAYER_H_)
