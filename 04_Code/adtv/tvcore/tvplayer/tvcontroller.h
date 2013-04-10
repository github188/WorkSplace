#ifndef JOYSEE_TVCONTROLLER_H_
#define JOYSEE_TVCONTROLLER_H_

#include "tvcomm.h"
#include "tsdemux.h"
#include "tsdemux_def.h"
#include "tvdevice.h"
#include "tvtask.h"
#include "tvtimemonitor.h"
#include "epgsearcher.h"
#include "servicemon.h"

using namespace TVCoreUtil;


class TsDataObserver
{

public:
	TsDataObserver();
	void gotTsData();
	void changeService();
	bool checkTsData();

private:
	bool m_Recorded;
	bool m_AlreadyChecked;			//当前节目设置过全码流
	int m_TsDataCount;				//如果有Ts数据非0
	unsigned int m_ChangeServiceTime;		//最近一次调台的时间
	MutexT	m_TsMutex;	
};

struct tvcontroller{
public:
		     tvcontroller();
	virtual ~tvcontroller();

	void     set_player_notify_callback(TVNOTIFY pCBNotify = 0);
	// 播放
	int		 play();
	// 停止
	int	    stop();
	//停止解扰
	int		stopDescramb();
	// 换台
	int		 change_channel(const DVBService& service);
	// 获取播放器的状态
	int 	 get_player_status();
	// 获取Tuner状态
	bool	 get_tuner_status(TunerSignal& status);
	// 设置模块状态
	void	 setModuleState(int iState);
	bool     isTVModule();

	// 设置显示区域
	bool setDisplayRect(int x,int y,int width,int height);
	// 设置画面比例(16:9或4:3)
	bool setDisplayZoomMode(IN const int iMode);
	// 清除最后视频帧(黑屏)
	bool cleanVideoFrame(bool bClean);
	// 显示(0)/隐藏(1)/置黑(2)视频层
	bool setVideoLayer(int iState);

	// 设置或获取当前频道音量补偿值
	bool adjustVolumeRatio(const S8 iRatio,const bool bIsSync);
	// 设置或获取audio设备的音量
	int  getVolume();
	int  setVolume(float volume);
	// 设置静音
	bool setMute(bool bMute);
	// 设置声道模式
	bool setAudioChannel(AudioStereoMode iMode);
	// 获取当前声道模式
	AudioStereoMode getAudioChannel();
	// 设置当前频道的语种/伴音
	bool setAudioLang(U8 uIndex);
	U8	 getAudioLang();
	// 获取TDT/TOT时间
	bool getTDTTime(U32& time);
	bool getTOTTime(U32& time,U32& offset);
	
	// EPG节目指南相关
	bool startEpgSearch(const TuningParam& tuning,const EITEventType evtType);
	// 主动取消EPG搜索任务
	bool cancelEpgSearch();
	// 获取EPG全部数据
	bool getEpgData(EPGDataBaseT& epgs);
	// 获取指定服务下的节目(事件)信息
	bool getEpgDataBySID(const U16 iServiceId,EpgEventSet& events);
	// 获取指定服务下指定时间段的节目(事件)信息
	bool getEpgDataByDuration(const U16 iServiceId,EpgEventSet& events,const U32 iStartTime,const U32 iEndTime);
	// 清除播放缓冲区数据
	bool resetTSBuffer();

	// 接收音视频数据
static  void    OnTSData(utHandle hFilter,long iTuner,utPid pid,utTid tid,utByte *pData,long lDataSize,utContext context);
static  void    OnSectionData(utHandle hFilter,long iTuner,utPid pid,utTid tid,utByte *pData,long lDataSize,utContext context);
static  void    OnDestoryCallBack(utHandle hFilter,utContext context);

	// EPG搜索完成通知 
static  int		OnSTVEPGComplete();

	static bool onServiceMonEvent(void * context, DVBService & newService, int eventType);
	bool onServiceMonEventPri(DVBService & newService, int type);

// CAS消息及状态监测
static  UINT __stdcall CASMonitorProc(LPVOID lpParam);
	bool getSTBId(char * pId, U8 buflen);

	bool set_control_pids(int *pPids,int iPidCount);

private:
	//	打开控制器
	bool	 open();
	//	关闭控制器
	bool	 close();

	int stop_play();

	// 获取CAS消息
	void GetCASEvents();
	void TunerHeartBeat();

	// 设置解扰(返回ECMPID的数量)
	int		descrambling(const DVBService& service);
	// 当前节目是否加扰
	bool    IsScrambling();
	// 启动EIT P/F 表探测任务
	void    start_detect_pf();
	// 启动当前频点EPG下载
	void	start_epg_download();
	// 启动TDT/TOT监控
	void	start_tdt_monitor();
	void	start_service_monitor(const DVBService & dvbservice, bool bstart);

	HANDLE  map_av_pid(const U16 vpid,const U16 apid);
	void    unmap_av_pid(HANDLE hTSFilter);
	
	// 是否是同一节目
	bool IsEqualDVBService(const DVBService& s1,const DVBService& s2);
	// 是否是同一频点
	bool IsEqualTuningParam(const TuningParam& t1,const TuningParam& t2);

	bool set_service_pids(const DVBService & service, bool updataAV);
	DVBService & GetCurService(){return stService_;}

private:
	LONG getSignalStatus(){return iSignalStatus_;}	//chehl,信号监测线程中调用
	
	HANDLE				hDevice_;		///<TV设备
	TVNOTIFY			pTVNotify_;		///<通知回调函数
	HANDLE				hTSFilter_;		///<当前节目过滤器

	MutexT				serviceMutex_;	///<DVBService锁
	DVBService          stService_;  	///<当前播放节目
		
	TVTaskMgr			*pTaskMgr_;		///<任务管理器
	
    TimeMonitorTask		*pTimeMonTask_; ///<TOT/TDT任务
	PFEventSearchTask	*pEpgPfTask_;	///<MiniEPG任务
	EpgSearchTask		*pEpgTask_;		///<EPG搜索任务
	ServiceMonTask		*pServiceMonTask_;
	EpgController		*pEpgController_;	///<EPG搜索管理

	simplethread		hRTMonitor_;	///<CAS消息状态侦测

	// 信号检测(避免重复发送中断或恢复)
	LONG iSignalStatus_;				///<当前信号状态
	LONG iLastSignalStatus_;			///<上一次信号状态


	MutexT	modMutex_;	
	int		iModuleState_;				///<模块运行标记

#ifdef MEASUREMENT_BITRATE
	static LONG iStartTime_;	///< 开始时间
	static LONG iTotalBytes_;   ///< 传输字节数
#endif 
public:
	bool	hasPrint;
	TsDataObserver		m_TsData;
};	

#endif //defined(JOYSEE_TVCONTROLLER_H_)
