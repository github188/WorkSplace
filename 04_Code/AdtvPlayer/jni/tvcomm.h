/*****************************************************************************
  Description: type define

  Copyright(c) 2010-2015 Novel-SuperTV, All rights reserved.

  Date        Author           Modification
  ----------------------------------------------------------------
  2011-03-07  WANG XING YU     Created
*****************************************************************************/
#ifndef NOVELSUPERTV_TVCOMM_DEFINE_H
#define NOVELSUPERTV_TVCOMM_DEFINE_H

#include <map>
#include <set>
#include <list>
#include <string>
#include <vector>
#include "typ.h"

#if defined(__cplusplus) 
extern "C" 
{ 
#endif 

#define IN
#define OUT
#define INOUT

#define TVCORE_OK					(0x0L)	
#define TVCORE_FAILURE				(0x1L)

#define SERVICENAME_MAXLENGTH		64
#define AUDIOSTREAM_MAXCOUNT		3
#define EVENTNAME_MAXLENGTH			256
#define EVENTDESC_MAXLENGTH		    1024
#define CFG_FILE_PATH				_T("./MctvData/TVCore/")
#define CFG_FILE_NAME               _T("tvcore.ini")
#define CFG_FILE_PATH_SYS			_T("./MctvData/SysSetting/")
#define CFG_FILE_NAME_SYS           _T("ServerSetting.ini")
#define CFG_FILE_NAME_CONFIG		_T("SysConfig.ini")

typedef std::map<U32, bool>	mapFavoriteT; //逻辑频道号

typedef U32 ServiceTypeFilterT;
//-------------------------------------
// bit31~24,bit23~16,bit15~8,bit7~0
//-------------------------------------
const U32 STF_DTS   = 0x01;  ///<数字音频服务
const U32 STF_DRSS  = 0x02;  ///<数字音频服务
const U32 STF_NOVDR = 0x04;  ///<NVOD参考服务
const U32 STF_NVODT = 0x05;  ///<NVOD时移服务
const U32 STF_MOSAIC= 0x06;	 ///<马赛克业务
const U32 STF_FMS   = 0x0A;	 ///<调频收音机服务
const U32 STF_FAVOR = 0x100; ///<bit9 :自定义服务:(节目喜好)
const U32 STF_NONE  = 0x200; ///<bit10:全部服务

// ------------ Play type----------
const U32 PLAY_NORMAL		= 0x0;
const U32 PLAY_PROGRAM_PLUS	= 0x1;
const U32 PLAY_PROGRAM_SUB	= 0x2;

// 搜索模式
typedef enum tagSearchMode
{
	STVMODE_MANUAL,		///<手动搜索模式
	STVMODE_FULL,		///<全频搜索模式
	STVMODE_NIT	,		///<NIT搜索模式(NIT+PAT+PMT+SDT+CAT)
	STVMODE_NIT_S,		///<NIT搜索模式简化版(NIT+SDT)
	STVMODE_MONITOR_PMT,///<手动方式(按SID取VPID+APID)并监视NIT表的版本
	STVMODE_NULL = -1
}STVMode;

// 调频参数
//##ModelId=4D89B9300318
typedef struct tagTuningParam
{
	// 频率，单位 kHz
	//##ModelId=4D89BDF000B5
	U32 freq;

	// 符号率 单位 Ksymbol/s
	//##ModelId=4D89BE1203DC
	U32 symb;

	// 调制方式
	// 0x00 16QAM
	// 0x01 32QAM
	// 0x02 64QAM
	// 0x03 128QAM
	// 0x04 256QAM
	//##ModelId=4D89BE220152
	U32 qam;

}TuningParam;

// 调制信号
typedef struct tagTunerSignal
{
	U32 Level;
	U32 CN;
	U32 ErrRate;
}TunerSignal;

typedef enum tagAudioStereoMode
{
	AUDIO_MODE_STEREO,
	AUDIO_MODE_LEFT,
	AUDIO_MODE_RIGHT,
	AUDIO_MODE_MONO,
	AUDIO_MODE_MAX
}AudioStereoMode;

// DVBTS
//##ModelId=4D8D4EBF0182
typedef struct tagDVBTS
{
	// 传输流ID
	//##ModelId=4D8D50920003
	U16 ts_id;

	// 网络ID
	//##ModelId=4D8D509B030A
	U16 net_id;

	//##ModelId=4D8D50BC039F
	TuningParam tuning_param;

}DVBTS;

// DVBStream结构定义
//##ModelId=4D8D4BC30159
typedef struct tagDVBStream

{
	//##ModelId=4D8D505801B2
	U8 stream_type;

	//##ModelId=4D8D5065039A
	U16 stream_pid;

	//##ModelId=4D8D50750029
	U16 ecm_pid;

	// name
	char name[SERVICENAME_MAXLENGTH];
	
}DVBStream;

// DVBService结构定义
//##ModelId=4D89CAB501B4
typedef struct tagDVBService
{
	// 业务ID
	//##ModelId=4D8D4EE300CA
	U16 sid;										///< 业务ID

	U16 channel_number;								///< 逻辑频道号

	// 业务名称
	//##ModelId=4D8D4EE90322
	char name[SERVICENAME_MAXLENGTH];				///< 业务名称

	// 业务类型
	//##ModelId=4D8D4F11033E
	U32 service_type;								///< 业务类型{数字电视、数字广播、NVOD点播...}
													///< bit8:'1' 喜欢,'0'不喜欢
													///< bit9:全部节目

	U32	reserved1;									///< 预留

	// 频道类别.可以有多个类别
	//##ModelId=4D8D4F2A001D
	U8 category;									///< 频道类别.可以有多个类别.

	U8 reserved2;									///< 预留

	// PCR PID
	//##ModelId=4D8D4F4001C1
	U16 pcr_pid;									///< PCR PID.

	U32 reserved3;									///< 预留

	// PMT PID
	U16 pmt_id;										///< PMTPid

	// 音量补偿
	//##ModelId=4D8D4F9F01F1
	S8 volume_ratio;								///< 音量补偿.

	U8 reserved4;									///< 预留

	// 音量记忆
	//##ModelId=4D8D4FAF0326
	U8 volume_reserve;								///<每个电视频道的音量都能单独调节、保存音量分级:32级

	// 声道设置(0:立体声 1:左声道 2:右声道 3:单声道)
    U8 audio_channel_set;							///<

	// 音频类型
	//##ModelId=4D8D4FC0000B
	U8 audio_format;								///< 音频类型.

	// 频道当前使用音频的索引，对应audio_stream数组的下标
	//##ModelId=4D8D53C00078
	U8 audio_index;									///< 音频流索引
	
	//##ModelId=4D8D504D0300
	DVBStream video_stream;							///< 视频流

	//##ModelId=4D8D50500120
	DVBTS ts;										///< TS信息

	//##ModelId=4D8D536502E8
	DVBStream audio_stream[AUDIOSTREAM_MAXCOUNT];	///< 音频流
}DVBService;
typedef std::vector<DVBService>		DVBServiceListT;

static bool DVBServiceSortBySID(const DVBService& one,const DVBService& two){
	return (one.sid < two.sid) ? true : false;
}
static bool DVBServiceSortByChl(const DVBService& one,const DVBService& two){
	return (one.channel_number < two.channel_number) ? true : false;
}

//##ModelId=4D8D551E02BB
typedef struct tagSECFilter
{
public:
	//##ModelId=4D8D552D02C2
	U16 pid;					///<PSI/SI表 pid

	//##ModelId=4D8D553300D8
	/// {[0]:table_id [1,2]:service_id [3]:Version 
	///  [4]:section_number [5]:last_section_number [6,7]:ts_id}
	U8 data[8];					///<支持的过滤字段					

	//##ModelId=4D8D55440058
	U8 mask[8];					///<字段掩码

	//##ModelId=4D8D554F0333
	U32 timeout;				///<过滤器超时值

}SECFilter;
typedef std::vector<SECFilter> FilterListT;

// EIT事件类型
enum EITEventType
{
	EIT_EVENT_PF,///<事件表:(当前/其它流)P/F当前/后续事件
	EIT_EVENT_AS,///<时间表当前流
	EIT_EVENT_OS,///<时间表其它流
	EIT_EVENT_SH,///<时间表
	EIT_EVENT_ALL///<全部表
};

typedef U32 Time;

// EPG事件存储结构
struct EpgEvent
{
	EpgEvent() 
		: 
	id(0xffff),
		start_time( 0 ), 
		end_time( 0 ) 
	{}

	U16 id;					///<事件ID
	std::string name;       ///<节目名称.
	Time start_time;      	///<节目开始时间.  
	Time end_time;          ///<节目结束事件.
	std::string description;///<节目简介.
	bool operator==(EpgEvent const &e) const {
		return name==e.name
			&& start_time==e.start_time
			&& end_time==e.end_time
			&& description==e.description;
	}
	bool operator!=(EpgEvent const &e) const {
		return !operator==(e);
	}
	bool operator < ( EpgEvent const& e ) const { 
		return start_time < e.start_time;
	}
};

typedef ::std::set<EpgEvent> EpgEventSet;		///<事件集合
typedef ::std::map<U16,EpgEventSet> EPGEVENTS;	///<服务-事件集合映射表

// EPG数据存储结构
struct ProgramEpg
{
	ProgramEpg()
	{
		sid = 0;
		PresentVer=0;
		FollowingVer=0;
		EventsVer=0;
	}
	U16 sid;
	U32 PresentVer;
	EpgEvent Present;
	U32 FollowingVer;
	EpgEvent Following;
	U32 EventsVer;
	EpgEventSet events;
};
typedef ::std::map<U16,ProgramEpg> EPGDataBaseT;

typedef struct TVSubscription{
	TVSubscription(): sid(0),channel_number(0),id(0),iStartTime(0),iEndTime(0){
		sname[0] = '\0';
		ename[0] = '\0';
	}
	U16 sid;										///< 业务ID
	U16 channel_number;								///< 逻辑频道号			
	U32     id;										///< 事件ID
	U32     iStartTime;								///< 节目开始时间
	U32     iEndTime;								///< 节目结束时间
	char    sname[SERVICENAME_MAXLENGTH];			///< 业务/频道名
	char	ename[SERVICENAME_MAXLENGTH];			///< 节目名
}TVSubscriptionT;
typedef std::vector<U32> SubscriptionIdT;					///< 订阅序列
typedef std::vector<TVSubscriptionT> TVSubscriptionST;		///<
typedef std::map<U32,TVSubscriptionT> mapSubscriptionsT;	///<

static bool OrderRuleofSubscription(TVSubscriptionT& one,TVSubscriptionT& two){
	return (one.iStartTime < two.iStartTime) ? true : false;
} 

/// CAS消息类型定义
typedef struct CAMessage
{
	/**
	* 构造函数.
	*
	* \param[in] msg 消息内容
	* \param[in] show 是否可见
	* \param[in] t 是否位于屏幕上方
	* -true 显示在屏幕上方.
	* -false 显示在屏幕下方.
	*/
	CAMessage( std::string const& msg, bool show ,bool t = false )
		: message( msg )
		, visible( show )
		, top( t )
		, majortype(-1)
		, subtype(-1)
	{}
	std::string message;
	bool        visible;
	bool        top;
	int			majortype;
	int			subtype;
}CAMessageT;

// 显示区域坐标
typedef struct MixerRect{
	MixerRect():left(left),top(top),right(right),bottom(bottom){}

	MixerRect(U32 left,U32 top,U32 right,U32 bottom)
		:left(left),top(top),right(right),bottom(bottom){}
	
	MixerRect& operator=(const MixerRect& rc)
	{
		left	= rc.left;
		top		= rc.top;
		right	= rc.right;
		bottom	= rc.bottom;
		return *this;
	}

	U32 left;
	U32 top;
	U32 right;
	U32 bottom;
}MixerRectT;

typedef struct IntelMixerParams
{
	IntelMixerParams(){
		display.left = 0,display.bottom = 0 ,display.right = 0,display.top=0;
		fullscreen = 1,curMode = 1;
	}
	
	MixerRectT	display;		// video display area
	U8			fullscreen;		// 0:No full,1: full
	U8			curMode;		// 0:Normal,1:Zoomed,2:Stretched,
								// 3:NonLinear,4:RawStretched
}IntelMixerParamsT;


typedef std::vector<U32> IDListT;

// 
// PSI/SI Section回调函数
// 返回值:
//	0 : 无效或不充分的数据
//  1 : 通知当前表分析完成
//  2 : 通知所有表分析完成(TVCore会结束本次任务)
//  注: 在所有过滤表中超时最大值超期时会自动结束本次任务
typedef int (*SECTIONPROC)(const U16 pid,const U8* pData,const U32 iLen);

#if defined(__cplusplus) 
}
#endif 

#endif  // defined(NOVELSUPERTV_TVCOMM_DEFINE_H)
