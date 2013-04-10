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

typedef std::map<U32, bool>	mapFavoriteT; //�߼�Ƶ����

typedef U32 ServiceTypeFilterT;
//-------------------------------------
// bit31~24,bit23~16,bit15~8,bit7~0
//-------------------------------------
const U32 STF_DTS   = 0x01;  ///<������Ƶ����
const U32 STF_DRSS  = 0x02;  ///<������Ƶ����
const U32 STF_NOVDR = 0x04;  ///<NVOD�ο�����
const U32 STF_NVODT = 0x05;  ///<NVODʱ�Ʒ���
const U32 STF_MOSAIC= 0x06;	 ///<������ҵ��
const U32 STF_FMS   = 0x0A;	 ///<��Ƶ����������
const U32 STF_FAVOR = 0x100; ///<bit9 :�Զ������:(��Ŀϲ��)
const U32 STF_NONE  = 0x200; ///<bit10:ȫ������

// ------------ Play type----------
const U32 PLAY_NORMAL		= 0x0;
const U32 PLAY_PROGRAM_PLUS	= 0x1;
const U32 PLAY_PROGRAM_SUB	= 0x2;

// ����ģʽ
typedef enum tagSearchMode
{
	STVMODE_MANUAL,		///<�ֶ�����ģʽ
	STVMODE_FULL,		///<ȫƵ����ģʽ
	STVMODE_NIT	,		///<NIT����ģʽ(NIT+PAT+PMT+SDT+CAT)
	STVMODE_NIT_S,		///<NIT����ģʽ�򻯰�(NIT+SDT)
	STVMODE_MONITOR_PMT,///<�ֶ���ʽ(��SIDȡVPID+APID)������NIT��İ汾
	STVMODE_NULL = -1
}STVMode;

// ��Ƶ����
//##ModelId=4D89B9300318
typedef struct tagTuningParam
{
	// Ƶ�ʣ���λ kHz
	//##ModelId=4D89BDF000B5
	U32 freq;

	// ������ ��λ Ksymbol/s
	//##ModelId=4D89BE1203DC
	U32 symb;

	// ���Ʒ�ʽ
	// 0x00 16QAM
	// 0x01 32QAM
	// 0x02 64QAM
	// 0x03 128QAM
	// 0x04 256QAM
	//##ModelId=4D89BE220152
	U32 qam;

}TuningParam;

// �����ź�
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
	// ������ID
	//##ModelId=4D8D50920003
	U16 ts_id;

	// ����ID
	//##ModelId=4D8D509B030A
	U16 net_id;

	//##ModelId=4D8D50BC039F
	TuningParam tuning_param;

}DVBTS;

// DVBStream�ṹ����
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

// DVBService�ṹ����
//##ModelId=4D89CAB501B4
typedef struct tagDVBService
{
	// ҵ��ID
	//##ModelId=4D8D4EE300CA
	U16 sid;										///< ҵ��ID

	U16 channel_number;								///< �߼�Ƶ����

	// ҵ������
	//##ModelId=4D8D4EE90322
	char name[SERVICENAME_MAXLENGTH];				///< ҵ������

	// ҵ������
	//##ModelId=4D8D4F11033E
	U32 service_type;								///< ҵ������{���ֵ��ӡ����ֹ㲥��NVOD�㲥...}
													///< bit8:'1' ϲ��,'0'��ϲ��
													///< bit9:ȫ����Ŀ

	U32	reserved1;									///< Ԥ��

	// Ƶ�����.�����ж�����
	//##ModelId=4D8D4F2A001D
	U8 category;									///< Ƶ�����.�����ж�����.

	U8 reserved2;									///< Ԥ��

	// PCR PID
	//##ModelId=4D8D4F4001C1
	U16 pcr_pid;									///< PCR PID.

	U32 reserved3;									///< Ԥ��

	// PMT PID
	U16 pmt_id;										///< PMTPid

	// ��������
	//##ModelId=4D8D4F9F01F1
	S8 volume_ratio;								///< ��������.

	U8 reserved4;									///< Ԥ��

	// ��������
	//##ModelId=4D8D4FAF0326
	U8 volume_reserve;								///<ÿ������Ƶ�����������ܵ������ڡ����������ּ�:32��

	// ��������(0:������ 1:������ 2:������ 3:������)
    U8 audio_channel_set;							///<

	// ��Ƶ����
	//##ModelId=4D8D4FC0000B
	U8 audio_format;								///< ��Ƶ����.

	// Ƶ����ǰʹ����Ƶ����������Ӧaudio_stream������±�
	//##ModelId=4D8D53C00078
	U8 audio_index;									///< ��Ƶ������
	
	//##ModelId=4D8D504D0300
	DVBStream video_stream;							///< ��Ƶ��

	//##ModelId=4D8D50500120
	DVBTS ts;										///< TS��Ϣ

	//##ModelId=4D8D536502E8
	DVBStream audio_stream[AUDIOSTREAM_MAXCOUNT];	///< ��Ƶ��
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
	U16 pid;					///<PSI/SI�� pid

	//##ModelId=4D8D553300D8
	/// {[0]:table_id [1,2]:service_id [3]:Version 
	///  [4]:section_number [5]:last_section_number [6,7]:ts_id}
	U8 data[8];					///<֧�ֵĹ����ֶ�					

	//##ModelId=4D8D55440058
	U8 mask[8];					///<�ֶ�����

	//##ModelId=4D8D554F0333
	U32 timeout;				///<��������ʱֵ

}SECFilter;
typedef std::vector<SECFilter> FilterListT;

// EIT�¼�����
enum EITEventType
{
	EIT_EVENT_PF,///<�¼���:(��ǰ/������)P/F��ǰ/�����¼�
	EIT_EVENT_AS,///<ʱ���ǰ��
	EIT_EVENT_OS,///<ʱ���������
	EIT_EVENT_SH,///<ʱ���
	EIT_EVENT_ALL///<ȫ����
};

typedef U32 Time;

// EPG�¼��洢�ṹ
struct EpgEvent
{
	EpgEvent() 
		: 
	id(0xffff),
		start_time( 0 ), 
		end_time( 0 ) 
	{}

	U16 id;					///<�¼�ID
	std::string name;       ///<��Ŀ����.
	Time start_time;      	///<��Ŀ��ʼʱ��.  
	Time end_time;          ///<��Ŀ�����¼�.
	std::string description;///<��Ŀ���.
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

typedef ::std::set<EpgEvent> EpgEventSet;		///<�¼�����
typedef ::std::map<U16,EpgEventSet> EPGEVENTS;	///<����-�¼�����ӳ���

// EPG���ݴ洢�ṹ
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
	U16 sid;										///< ҵ��ID
	U16 channel_number;								///< �߼�Ƶ����			
	U32     id;										///< �¼�ID
	U32     iStartTime;								///< ��Ŀ��ʼʱ��
	U32     iEndTime;								///< ��Ŀ����ʱ��
	char    sname[SERVICENAME_MAXLENGTH];			///< ҵ��/Ƶ����
	char	ename[SERVICENAME_MAXLENGTH];			///< ��Ŀ��
}TVSubscriptionT;
typedef std::vector<U32> SubscriptionIdT;					///< ��������
typedef std::vector<TVSubscriptionT> TVSubscriptionST;		///<
typedef std::map<U32,TVSubscriptionT> mapSubscriptionsT;	///<

static bool OrderRuleofSubscription(TVSubscriptionT& one,TVSubscriptionT& two){
	return (one.iStartTime < two.iStartTime) ? true : false;
} 

/// CAS��Ϣ���Ͷ���
typedef struct CAMessage
{
	/**
	* ���캯��.
	*
	* \param[in] msg ��Ϣ����
	* \param[in] show �Ƿ�ɼ�
	* \param[in] t �Ƿ�λ����Ļ�Ϸ�
	* -true ��ʾ����Ļ�Ϸ�.
	* -false ��ʾ����Ļ�·�.
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

// ��ʾ��������
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
// PSI/SI Section�ص�����
// ����ֵ:
//	0 : ��Ч�򲻳�ֵ�����
//  1 : ֪ͨ��ǰ��������
//  2 : ֪ͨ���б�������(TVCore�������������)
//  ע: �����й��˱��г�ʱ���ֵ����ʱ���Զ�������������
typedef int (*SECTIONPROC)(const U16 pid,const U8* pData,const U32 iLen);

#if defined(__cplusplus) 
}
#endif 

#endif  // defined(NOVELSUPERTV_TVCOMM_DEFINE_H)
