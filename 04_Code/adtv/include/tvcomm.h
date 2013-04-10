/*****************************************************************************
  Description: type define
  Copyright(c) 2010-2015 Novel-SuperTV, All rights reserved.
  Date        Author           Modification
  ----------------------------------------------------------------
  2011-03-07  WANG XING YU     Created
*****************************************************************************/
#ifndef 	JOYSEE_TVCOMM_H_
#define 	JOYSEE_TVCOMM_H_
#include "typ.h"
#include <set>
#include <map>
#include <list>
#include <deque>
#include <vector>
#include <string>
using namespace std;

#ifdef __cplusplus
extern "C" { 
#endif 


////////////////////////////////////////////////////////////////////////////////
// defines
////////////////////////////////////////////////////////////////////////////////

#define IN
#define OUT
#define INOUT

#define SERVICENAME_MAXLENGTH		64
#define AUDIOSTREAM_MAXCOUNT		3
#define EVENTNAME_MAXLENGTH			256
#define EVENTDESC_MAXLENGTH		    1024

#define MONITOR_VERSION_FILE_PATH			"/data/data/novel.supertv.dvb/databases/version.txt"
//-------------------------------------
// bit31~24,bit23~16,bit15~8,bit7~0
//-------------------------------------
const U32 STF_DTS   	= 0x01;    	///<数字音频服务
const U32 STF_DRSS  	= 0x02;  	///<数字音频服务
const U32 STF_NOVDR 	= 0x04;		///<NVOD参考服务
const U32 STF_NVODT 	= 0x05; 	///<NVOD时移服务
const U32 STF_MOSAIC	= 0x06;		///<马赛克业务
const U32 STF_FMS   	= 0x0A;		///<调频收音机服务
const U32 STF_FAVOR 	= 0x100; 	///<bit9 :自定义服务:(节目喜好)
const U32 STF_NONE  	= 0x200; 	///<bit10:全部服务

const U32 TUNER_QAM16   = 0; 	///<QAM16调制方式.
const U32 TUNER_QAM32   = 1; 	///<QAM32调制方式.
const U32 TUNER_QAM64   = 2; 	///<QAM54调制方式.
const U32 TUNER_QAM128 = 3; 	///<QAM128调制方式.
const U32 TUNER_QAM256 = 4; 	///<QAM256调制方式.

const U16 INVALID_PID	 = 0x1FFF;
const U32 INVALID_SID  = 0xFFFF;

////////////////////////////////////////////////////////////////////////////////
// enums
////////////////////////////////////////////////////////////////////////////////

/// TS包标识符定义
typedef enum tagPIDType
{
	PAT_PID	 = 0x0000,
	CAT_PID	 = 0x0001,
	TSDT_PID = 0x0002,
	NIT_PID  = 0x0010,
	SDT_PID  = 0x0011,
	BAT_PID  = 0x0011,
	EIT_PID  = 0x0012,
	RST_PID  = 0x0013,
	TDT_PID  = 0x0014,
	PID_NULL = 0x1FFF
}PIDType;

/// TS表标识符定义
typedef enum tagDVBTableID{
	TableId_Pat = 0x00,
	TableId_Cat = 0x01,
	TableId_Pmt = 0x02,
	TableId_NitA = 0x40,
	TableId_NitO = 0x41,
	TableId_SdtA = 0x42,
	TableId_SdtO = 0x46,
	TableId_Bat = 0x4A,
	TableId_Tdt = 0x70,
	TableId_Tot = 0x73,
	TableId_DsiDii = 0x3B,
	TableId_Ddb = 0x3C
}DVBTableID;

// 搜索模式
typedef enum tagSearchMode
{
	STVMODE_MANUAL,		///<手动搜索模式
	STVMODE_FULL,			///<全频搜索模式
	STVMODE_NIT	,		///<NIT搜索模式(NIT+PAT+PMT+SDT+CAT)
	STVMODE_NIT_S,			///<NIT搜索模式简化版(NIT+SDT)
	STVMODE_MONITOR_PMT,	///<手动方式(按SID取VPID+APID)并监视NIT表的版本
	STVMODE_NULL = -1
}STVMode;

typedef enum tagAudioStereoMode
{
	AUDIO_MODE_STEREO,
	AUDIO_MODE_LEFT,
	AUDIO_MODE_RIGHT,
	AUDIO_MODE_MONO,
	AUDIO_MODE_MAX
}AudioStereoMode;

// EIT事件类型
enum EITEventType
{
	EIT_EVENT_UN =-1, ///<未定义
	EIT_EVENT_PF,	///<事件表:(当前/其它流)P/F当前/后续事件
	EIT_EVENT_AS,	///<时间表当前流
	EIT_EVENT_OS,	///<时间表其它流
	EIT_EVENT_SH,	///<时间表
	EIT_EVENT_ALL	///<全部表
};

////////////////////////////////////////////////////////////////////////////////
// structs
////////////////////////////////////////////////////////////////////////////////
// 调频参数
typedef struct tagTuningParam
{
	// 频率，单位 kHz
	U32 freq;

	// 符号率 单位 Ksymbol/s
	U32 symb;

	// 调制方式
	// 0x00 16QAM
	// 0x01 32QAM
	// 0x02 64QAM
	// 0x03 128QAM
	// 0x04 256QAM
	U32 qam;

}TuningParam;

// 调制信号
typedef struct tagTunerSignal
{
	U32 	locked;	///<锁定状态(1:locked,0:unlock)
	U32	strength;	///<强度
	U32	quality;	///<质量
	tagTunerSignal()
	{
		locked=0;
		strength=0;
		quality=0;
	}
}TunerSignal;

typedef struct tagDVBCW 
{
	U16	 pid;			///<绑定id
	BYTE oddKey[8];		///<奇密钥
	BYTE eventKey[8];	///<偶密钥
} DVBCW;

// DVBTS
typedef struct tagDVBTS
{
	U16 ts_id;		///<传输流ID
	U16 net_id;		///<网络ID

	TuningParam tuning_param;

}DVBTS;

// DVBStream结构定义
typedef struct tagDVBStream

{
	U8 stream_type;
	U16 stream_pid;
	U16 ecm_pid;

	char name[SERVICENAME_MAXLENGTH];
	
}DVBStream;

// DVBService结构定义
typedef struct tagDVBService
{
	// 业务ID
	U16 serviceID;									///< 业务ID
	U16 channel_number;								///< 逻辑频道号
	// 业务名称
	char name[SERVICENAME_MAXLENGTH];				///< 业务名称
	// 业务类型
	U32 service_type;								///< 业务类型{数字电视、数字广播、NVOD点播...}
													///< bit8:'1' 喜欢,'0'不喜欢
													///< bit9:全部节目， 顶部高8bits 代表了节目分类
	U32	ca_mode;									///< 预留
	// 频道类别.可以有多个类别
	U8 category;									///< 频道类别.可以有多个类别.
	U8 nitVersion;									///< 预留
	// PCR PID
	U16 pcr_pid;									///< PCR PID.
	U32 emm_pid;									///< 预留
	// PMT PID
	U16 pmt_id;										///< PMTPid
	// 音量补偿
	S8 volume_comp;								///< 音量补偿.
	U8 batVersion;									///< 预留
	// 音量记忆
	U8 volume_reserve;								///<每个电视频道的音量都能单独调节、保存音量分级:32级
	// 声道设置(0:立体声 1:左声道 2:右声道 3:单声道)
    U8 audio_channel_set;							///<
	// 音频类型
	U8 audio_format;								///< 音频类型.
	// 频道当前使用音频的索引，对应audio_stream数组的下标
	U8 audio_index;									///< 音频流索引
	DVBStream video_stream;							///< 视频流
	DVBTS ts;										///< TS信息
	DVBStream audio_stream[AUDIOSTREAM_MAXCOUNT];	///< 音频流
}DVBService;

typedef struct tagSECFilter
{
public:
	U16 pid;				///<PSI/SI表 pid
							/// {[0]:table_id [1,2]:service_id [3]:Version 
							///  [4]:section_number [5]:last_section_number [6,7]:ts_id}
	U8 data[8];				///<支持的过滤字段					
	U8 mask[8];				///<字段掩码
	U32 timeout;			///<过滤器超时值

}SECFilter;

// EPG事件存储结构
struct EpgEvent
{
	EpgEvent() 
		: 
	id(0xffff),
		start_time( 0 ), 
		end_time( 0 ) 
	{}

	U16 id;				 	 ///<事件ID
	std::string name;      	 ///<节目名称.
	U32 start_time;      	 ///<节目开始时间.  
	U32 end_time;          	 ///<节目结束事件.
	std::string description; ///<节目简介.
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
	U32 FollowingVer;
	U32 EventsVer;
	EpgEvent Present;
	EpgEvent Following;
	EpgEventSet events;
};

struct StreamID
{
public:
	enum
	{
		// video
		MPEG1Video     = 0x01, 
		MPEG2Video     = 0x02, 
		MPEG4Video     = 0x10, 
		H264Video      = 0x1b, 
		OpenCableVideo = 0x80,
		VC1Video       = 0xea, 

		// audio
		MPEG1Audio     = 0x03, 
		MPEG2Audio     = 0x04, 
		MPEG2AACAudio  = 0x0f, 
		MPEG2AudioAmd1 = 0x11,
		EAC3Audio      = 0x7a, //私有定义
		EDTSAudio	   = 0x7b,
		AC3Audio       = 0x81,
		DTSAudio       = 0x8a,

		// DSM-CC Object Carousel
		DSMCC          = 0x08, 
		DSMCC_A        = 0x0a, 
		DSMCC_B        = 0x0b, 
		DSMCC_C        = 0x0c, 
		DSMCC_D        = 0x0d, 
		DSMCC_DL       = 0x14, 
		MetaDataPES    = 0x15, 
		MetaDataSec    = 0x16, 
		MetaDataDC     = 0x17, 
		MetaDataOC     = 0x18, 
		MetaDataDL     = 0x19, 

		// other
		PrivSec        = 0x05, 
		PrivData       = 0x06, 

		MHEG           = 0x07, 
		H222_1         = 0x09, 

		MPEG2Aux       = 0x0e, 

		FlexMuxPES     = 0x12, 
		FlexMuxSec     = 0x13, 

		MPEG2IPMP      = 0x1a, 
		MPEG2IPMP2     = 0x7f, 

		// special id's, not actually ID's but can be used in FindPIDs
		AnyMask        = 0xFFFF0000,
		AnyVideo       = 0xFFFF0001,
		AnyAudio       = 0xFFFF0002,
	};
	static bool IsVideo(U8 iType)
	{
		return ((StreamID::MPEG1Video == iType) ||
			(StreamID::MPEG2Video == iType) ||
			(StreamID::MPEG4Video == iType) ||
			(StreamID::H264Video  == iType) ||
			(StreamID::VC1Video   == iType) ||
			(StreamID::OpenCableVideo == iType));
	}
	static bool IsAudio(U8 iType)
	{
		return ((StreamID::MPEG1Audio == iType) ||
			(StreamID::MPEG2Audio     == iType) ||
			(StreamID::MPEG2AudioAmd1 == iType) ||
			(StreamID::MPEG2AACAudio  == iType) ||
			(StreamID::EAC3Audio	  == iType) ||
			(StreamID::AC3Audio       == iType) ||
			(StreamID::EDTSAudio      == iType) || 
			(StreamID::DTSAudio       == iType) || 
			(StreamID::PrivData		  == iType) ||
			(0x6A == iType));
	}
	static bool IsObjectCarousel(U8 iType)
	{
		return ((StreamID::DSMCC_A == iType) ||
			(StreamID::DSMCC_B == iType) ||
			(StreamID::DSMCC_C == iType) ||
			(StreamID::DSMCC_D == iType));
	}
};
/*
static bool DVBServiceSortBySID(const DVBService& one,const DVBService& two){
	return (one.serviceID < two.serviceID) ? true : false;
}
*/
////////////////////////////////////////////////////////////////////////////////
// typedefs 
////////////////////////////////////////////////////////////////////////////////
typedef ::std::vector<U16>				FreqListT;
typedef ::std::vector<U32> 				IDListT;
typedef ::std::vector<DVBTS>			DVBTVListT;
typedef ::std::vector<SECFilter> 		FilterListT;
typedef ::std::vector<TuningParam>		TuningParamListT;
typedef ::std::vector<DVBService>		DVBServiceListT;
typedef ::std::map<U32,DVBService> 		mapDVBServiceT;	// channel_number---service
typedef ::std::map<U16,EpgEventSet>		EPGEVENTS;	///<服务-事件集合映射表
typedef ::std::map<U16,ProgramEpg> 		EPGDataBaseT;

///////////////////////////////////////
//  TV Message
//////////////////////////////////////
#define TVNOTIFY_MINEPG				(100)	// PF 搜索完成通知
#define TVNOTIFY_EPGCOMPLETE		(101)	// EPG节目指南完成通知
#define TVNOTIFY_TUNER_SIGNAL		(102)	// TUNER实时信号状态通知
#define TVNOTIFY_UPDATE_SERVICE		(103)	// 通知更新DVBService(name,PMT and A/V pid)
#define TVNOTIFY_UPDATE_PROGRAM 	(104)	// 通知更新节目信息,重新搜台
#define TVNOTIFY_BUYMSG				(200)	// 不能正常收看节目的提示
#define TVNOTIFY_OSD				(201)	// 显示/隐藏OSD信息
#define TVNOTIFY_SHOW_FINGERPRINT	(202)	// 指纹显示
#define TVNOTIFY_SHOW_PROGRESSSTRIP	(203)	// 进度显示
#define TVNOTIFY_MAIL_NOTIFY  		(204)	// 新邮件通知消息
#define TVNOTIFY_BUYIPP				(205)	// 实时购买IPP
#define TVNOTIFY_GENCYBROADCAST		(206)	// 应急广播
#define TVNOTIFY_MOTHER_CARDPAIR	(207)	// 子母卡配对消息 
#define TVNOTIFY_AREAlOCK			(208)	// 单频点区域锁定
#define TVNOTIFY_ENTITLE_CHANGE 	(300)	// 授权改变
#define TVNOTIFY_DETITLE  			(301)	// 反授权通知消息

#define SHUMA_MSG					(10000) //

//TVNOTIFY_GENCYBROADCAST 消息
#define BROADCAST_STATUS_START		(1)		//开始应急广播
#define BROADCAST_STATUS_STOP		(0)		//停止应急广播

//TVNOTIFY_MOTHER_CARDPAIR 子母卡配对消息
// 获取母卡信息失败，请插入要配对的母卡
#define CARDPAIR_GET_MOTHERINFO_FAILED				1	
// 正在读取母卡信息，请稍等
#define CARDPAIR_READING_MOTHERINFO					2	
// 成功获取母卡信息，请插入要配对的子卡
#define CARDPAIR_INSERT_SONCARD						3	
// 恭喜，配对成功，请插入其他要配对的子卡
#define CARDPAIR_PAIR_SUCCEED						4	
//配对失败，请插入要配对的子卡
#define CARDPAIR_PAIR_FAILED						5	

//TVNOTIFY_AREAlOCK 单频点区域锁定
//开始获取区域信息
#define AREALOCK_BEGIN					0
//获取区域信息结束,正常完成,超时,锁频失败
#define AREALOCK_END_OK					1
#define AREALOCK_END_TIMEOUT			2
#define AREALOCK_END_LOCK_FAILED		3


/*---------- CAS提示信息---------*/
#define MESSAGE_CANCEL_TYPE      0x00  /* 取消当前的显示*/
#define MESSAGE_BADCARD_TYPE     0x01  /* 无法识别卡*/
#define MESSAGE_EXPICARD_TYPE    0x02  /* 智能卡过期，请更换新卡*/
#define MESSAGE_INSERTCARD_TYPE  0x03  /* 加扰节目，请插入智能卡*/
#define MESSAGE_NOOPER_TYPE      0x04  /* 卡中不存在节目运营商*/
#define MESSAGE_BLACKOUT_TYPE    0x05  /* 条件禁播*/
#define MESSAGE_OUTWORKTIME_TYPE 0x06  /* 当前时段被设定为不能观看*/
#define MESSAGE_WATCHLEVEL_TYPE  0x07  /* 节目级别高于设定的观看级别*/
#define MESSAGE_PAIRING_TYPE     0x08  /* 智能卡与本机顶盒不对应*/
#define MESSAGE_NOENTITLE_TYPE   0x09  /* 没有授权*/
#define MESSAGE_DECRYPTFAIL_TYPE 0x0A  /* 节目解密失败*/
#define MESSAGE_NOMONEY_TYPE	 0x0B  /* 卡内金额不足*/
#define MESSAGE_ERRREGION_TYPE   0x0C  /* 区域不正确*/
#define MESSAGE_NEEDFEED_TYPE    0x0D  /* 子卡需要和母卡对应，请插入母卡*/
#define MESSAGE_ERRCARD_TYPE     0x0E  /* 智能卡校验失败，请联系运营商*/
#define MESSAGE_UPDATE_TYPE      0x0F  /* 智能卡升级中，请不要拔卡或者关机*/
#define MESSAGE_LOWCARDVER_TYPE  0x10  /* 请升级智能卡*/
#define MESSAGE_VIEWLOCK_TYPE    0x11  /* 请勿频繁切换频道*/
#define MESSAGE_MAXRESTART_TYPE  0x12  /* 智能卡暂时休眠请5 分钟后重新开机*/
#define MESSAGE_FREEZE_TYPE      0x13  /* 智能卡已冻结，请联系运营商*/
#define MESSAGE_CALLBACK_TYPE    0x14  /* 智能卡已暂停请回传收视记录给运营商*/
#define MESSAGE_CURTAIN_TYPE	  0x15 /*高级预览节目，该阶段不能免费观看*/
#define MESSAGE_CARDTESTSTART_TYPE 0x16 /*升级测试卡测试中...*/
#define MESSAGE_CARDTESTFAILD_TYPE 0x17 /*升级测试卡测试失败，请检查机卡通讯模块*/
#define MESSAGE_CARDTESTSUCC_TYPE  0x18 /*升级测试卡测试成功*/
#define MESSAGE_NOCALIBOPER_TYPE    0x19/*卡中不存在移植库定制运营商*/
#define MESSAGE_NULL_1A			0x1A
#define MESSAGE_NULL_1B			0x1B
#define MESSAGE_NULL_1C			0x1C
#define MESSAGE_NULL_1D			0x1D
#define MESSAGE_NULL_1E			0x1E
#define MESSAGE_NULL_1F			0x1F
#define MESSAGE_STBLOCKED_TYPE   0x20  /* 请重启机顶盒*/	
#define MESSAGE_STBFREEZE_TYPE   0x21  /* 机顶盒被冻结*/

typedef struct tagMiniEPGNotify
{
	U16  ServiceID;
	char CurrentEventName[128];	// 当前节目名称
	U32  CurrentEventStartTime;	// 当前节目开始时间
	U32  CurrentEventEndTime;	// 当前节目结束时间
	char NextEventName[128];	// 后继节目名称
	U32  NextEventStartTime;	// 后继节目开始时间
	U32  NextEventEndTime;		// 后继节目结束时间
} MiniEPGNotify;

typedef struct tagTableItem
{
	int serviceType;
	const char *serviceName;
}ServiceTypeTableItem;

typedef int (*TVNOTIFY)(int notifyCode,long lParam,void *pParam);

#ifdef __cplusplus
}
#endif 

#endif  // defined(JOYSEE_TVCOMM_H_)
