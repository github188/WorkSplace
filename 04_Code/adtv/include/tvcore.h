#ifndef JOYSEE_TVCORE_H_
#define JOYSEE_TVCORE_H_

#include "tvcomm.h"
#include "capture_def.h"	//for stbca
#include "isearchtvnotify.h"

#if defined(WIN32)

	#ifndef TVCORE_EXPORTS
		#define TVCORE_API __declspec(dllexport)
	#else
		#define TVCORE_API __declspec(dllimport)
	#endif 

#else
	
	#define TVCORE_API

#endif 

#ifdef __cplusplus
extern "C" {
#endif 

// 电视模块初始化(仅执行一次)
TVCORE_API int  tvcore_init();
// 电视模块资源释放(仅执行一次)
TVCORE_API int  tvcore_uninit();
// 电视播放(依赖tvcore_setService)
TVCORE_API int  tvcore_play();
// 停止播放
TVCORE_API int  tvcore_stop();
// 切换频道
TVCORE_API int  tvcore_setService(DVBService const *service);
// 获取调谐器状态
TVCORE_API bool	tvcore_get_tuner_status(TunerSignal& status);
// 设置当前频道(语种/伴音)
TVCORE_API bool tvcore_set_audio_lang(U8 uIndex);
// 获取当前频道(语种/伴音)数量
TVCORE_API U8   tvcore_get_audio_lang(void);
// 获取TDT/TOT时间
TVCORE_API bool tvcore_get_tdt(U32& time);
TVCORE_API bool tvcore_get_tot(U32& time,U32& offset);

//获取CA库版本

TVCORE_API void tvcore_get_caVersion(char * pVer);

// 主动开始EPG搜索任务
TVCORE_API bool tvcore_startEpgSearch(const TuningParam& tuning,const EITEventType evtType);
// 主动取消EPG搜索任务
TVCORE_API bool tvcore_cancelEpgSearch();
// 获取EPG全部数据
TVCORE_API bool tvcore_getEpgData(EPGDataBaseT& epgs);
// 获取指定服务下的节目(事件)信息
TVCORE_API bool tvcore_getEpgDataBySID(const U16 iServiceId,EpgEventSet& events);
// 获取指定服务下指定时间段的节目(事件)信息
TVCORE_API bool tvcore_getEpgDataByDuration(const U16 iServiceId,EpgEventSet& events,const U32 iStartTime,const U32 iEndTime);
// 获取当前节目PF数据
bool tvocre_getPFEventInfo(MiniEPGNotify* pPFEvent);
// 清除播放缓冲区数据
TVCORE_API bool tvcore_resetTSBuffer();

// 增加通知回调
TVCORE_API void tvcore_addTVNotify(TVNOTIFY callback);
// 删除通知回调
TVCORE_API void tvcore_delTVNotify(TVNOTIFY callback);
// 清除所有的通知回调
TVCORE_API void tvcore_delAllTVNotify();

// 设置显示区域
TVCORE_API bool tvcore_setDisplayRect(int x,int y,int width,int height);
// 设置画面比例
TVCORE_API bool tvcore_setDisplayZoomMode(const int iMode);
// 清除最后视频帧
TVCORE_API bool tvcore_cleanVideoFrame(bool bClean);
// 视频层控制:显示(0),隐藏(1),置黑(2)视频层
TVCORE_API bool tvcore_setVideoLayer(int iState);

// 设置或获取设备的主音量
TVCORE_API int  tvcore_mute(bool bMute);
TVCORE_API int  tvcore_getVolume();
TVCORE_API int  tvcore_setVolume(float volume);
// 设置声道模式
TVCORE_API bool tvCore_setAudioChannel(AudioStereoMode iMode);
// 获取当前声道模式
TVCORE_API AudioStereoMode tvcore_getAudioChannel();

//===========================智能卡管理接口================================
// 修改pin码(初始pin码为6 '0')
TVCORE_API U16  tvcore_change_pincode(const U8 *pOld,const U8 *pNew);
// 获取观看级别
TVCORE_API U8   tvcore_get_watch_level();
// 设置观看级别
TVCORE_API U16	tvcore_set_watch_level(const U8 *pPinCode,const U8 iLevel);
// 设置观看时段
TVCORE_API U16 tvcore_set_watch_time0(const U8 iStartHour,const U8 iEndHour);
TVCORE_API U16 tvcore_set_watch_time1(const U8 *pPinCode,const U8 iStartHour,const U8 iEndHour);
TVCORE_API U16 tvcore_set_watch_time2(const U8 *pPinCode,
		                               const U8 iStarthour,const U8 iStartMin,const U8 iStartSec,
		                               const U8 iEndHour,const U8 iEndMin,const U8 iEndSec);
// 获取观看时间
TVCORE_API U16 tvcore_get_watch_time0(U8 *pStartHour,U8 *pEndHour);
TVCORE_API U16 tvcore_get_watch_time1(U8 *pStartHout,U8 *pStartMin,U8 *pStartSec,
		                               U8 *pEndHour,  U8 *pEndMin,  U8 *pEndSec);
//  获取授权信息列表
TVCORE_API U16 tvcore_get_authorization(const OperatorId id,EntitleListT& infos);
//  获取智能卡号
TVCORE_API bool tvcore_get_cardsn(char* pCardSN,U8 *iSNLength);
//获取机顶盒ID
TVCORE_API bool tvcore_getSTBId(char * pId, U8 buflen);
//  获取运营商ID
TVCORE_API U16	tvcore_get_operator_id(OperatorIDListT& ids);

TVCORE_API U16	tvcore_get_operator_acs(const OperatorId id, std::vector<U32> & list);

// 开始搜索电视频道
TVCORE_API U32 tvcore_startSearchTV(STVMode iMode, TuningParam *pTuningParam,ISearchTVNotify *pNotify);
// 取消频道搜索
TVCORE_API U32 tvcore_cancelSearchTV();

//========================== 邮件管理 ==========================================
// 获取邮件的头信息
TVCORE_API bool tvcore_getEMailHeads(EmailHead* pEmailHead,U8* pCount,U8* pFromIndex);
// 获取指定邮件的头信息
TVCORE_API bool tvcore_getEMailHead(EmailId id, EmailHead& head);
// 获取指定邮件的内容
TVCORE_API bool tvcore_getEMailContent(EmailId id,EmailContent* pContent);
// 删除邮件
TVCORE_API void tvcore_delEMail(EmailId id);
// 查询邮箱使用情况
// uEmailNum:已收邮件的个数,uEmptyNum:还能接收邮件的个数(邮箱容量限制:100)
TVCORE_API void tvcore_getEMailSpaceInfo(U8& uEmailNum,U8& uEmptyNum);

#ifdef __cplusplus
}
#endif

#endif //defined(JOYSEE_TVCORE_H_)
