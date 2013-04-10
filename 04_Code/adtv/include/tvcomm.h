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
const U32 STF_DTS   	= 0x01;    	///<������Ƶ����
const U32 STF_DRSS  	= 0x02;  	///<������Ƶ����
const U32 STF_NOVDR 	= 0x04;		///<NVOD�ο�����
const U32 STF_NVODT 	= 0x05; 	///<NVODʱ�Ʒ���
const U32 STF_MOSAIC	= 0x06;		///<������ҵ��
const U32 STF_FMS   	= 0x0A;		///<��Ƶ����������
const U32 STF_FAVOR 	= 0x100; 	///<bit9 :�Զ������:(��Ŀϲ��)
const U32 STF_NONE  	= 0x200; 	///<bit10:ȫ������

const U32 TUNER_QAM16   = 0; 	///<QAM16���Ʒ�ʽ.
const U32 TUNER_QAM32   = 1; 	///<QAM32���Ʒ�ʽ.
const U32 TUNER_QAM64   = 2; 	///<QAM54���Ʒ�ʽ.
const U32 TUNER_QAM128 = 3; 	///<QAM128���Ʒ�ʽ.
const U32 TUNER_QAM256 = 4; 	///<QAM256���Ʒ�ʽ.

const U16 INVALID_PID	 = 0x1FFF;
const U32 INVALID_SID  = 0xFFFF;

////////////////////////////////////////////////////////////////////////////////
// enums
////////////////////////////////////////////////////////////////////////////////

/// TS����ʶ������
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

/// TS���ʶ������
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

// ����ģʽ
typedef enum tagSearchMode
{
	STVMODE_MANUAL,		///<�ֶ�����ģʽ
	STVMODE_FULL,			///<ȫƵ����ģʽ
	STVMODE_NIT	,		///<NIT����ģʽ(NIT+PAT+PMT+SDT+CAT)
	STVMODE_NIT_S,			///<NIT����ģʽ�򻯰�(NIT+SDT)
	STVMODE_MONITOR_PMT,	///<�ֶ���ʽ(��SIDȡVPID+APID)������NIT��İ汾
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

// EIT�¼�����
enum EITEventType
{
	EIT_EVENT_UN =-1, ///<δ����
	EIT_EVENT_PF,	///<�¼���:(��ǰ/������)P/F��ǰ/�����¼�
	EIT_EVENT_AS,	///<ʱ���ǰ��
	EIT_EVENT_OS,	///<ʱ���������
	EIT_EVENT_SH,	///<ʱ���
	EIT_EVENT_ALL	///<ȫ����
};

////////////////////////////////////////////////////////////////////////////////
// structs
////////////////////////////////////////////////////////////////////////////////
// ��Ƶ����
typedef struct tagTuningParam
{
	// Ƶ�ʣ���λ kHz
	U32 freq;

	// ������ ��λ Ksymbol/s
	U32 symb;

	// ���Ʒ�ʽ
	// 0x00 16QAM
	// 0x01 32QAM
	// 0x02 64QAM
	// 0x03 128QAM
	// 0x04 256QAM
	U32 qam;

}TuningParam;

// �����ź�
typedef struct tagTunerSignal
{
	U32 	locked;	///<����״̬(1:locked,0:unlock)
	U32	strength;	///<ǿ��
	U32	quality;	///<����
	tagTunerSignal()
	{
		locked=0;
		strength=0;
		quality=0;
	}
}TunerSignal;

typedef struct tagDVBCW 
{
	U16	 pid;			///<��id
	BYTE oddKey[8];		///<����Կ
	BYTE eventKey[8];	///<ż��Կ
} DVBCW;

// DVBTS
typedef struct tagDVBTS
{
	U16 ts_id;		///<������ID
	U16 net_id;		///<����ID

	TuningParam tuning_param;

}DVBTS;

// DVBStream�ṹ����
typedef struct tagDVBStream

{
	U8 stream_type;
	U16 stream_pid;
	U16 ecm_pid;

	char name[SERVICENAME_MAXLENGTH];
	
}DVBStream;

// DVBService�ṹ����
typedef struct tagDVBService
{
	// ҵ��ID
	U16 serviceID;									///< ҵ��ID
	U16 channel_number;								///< �߼�Ƶ����
	// ҵ������
	char name[SERVICENAME_MAXLENGTH];				///< ҵ������
	// ҵ������
	U32 service_type;								///< ҵ������{���ֵ��ӡ����ֹ㲥��NVOD�㲥...}
													///< bit8:'1' ϲ��,'0'��ϲ��
													///< bit9:ȫ����Ŀ�� ������8bits �����˽�Ŀ����
	U32	ca_mode;									///< Ԥ��
	// Ƶ�����.�����ж�����
	U8 category;									///< Ƶ�����.�����ж�����.
	U8 nitVersion;									///< Ԥ��
	// PCR PID
	U16 pcr_pid;									///< PCR PID.
	U32 emm_pid;									///< Ԥ��
	// PMT PID
	U16 pmt_id;										///< PMTPid
	// ��������
	S8 volume_comp;								///< ��������.
	U8 batVersion;									///< Ԥ��
	// ��������
	U8 volume_reserve;								///<ÿ������Ƶ�����������ܵ������ڡ����������ּ�:32��
	// ��������(0:������ 1:������ 2:������ 3:������)
    U8 audio_channel_set;							///<
	// ��Ƶ����
	U8 audio_format;								///< ��Ƶ����.
	// Ƶ����ǰʹ����Ƶ����������Ӧaudio_stream������±�
	U8 audio_index;									///< ��Ƶ������
	DVBStream video_stream;							///< ��Ƶ��
	DVBTS ts;										///< TS��Ϣ
	DVBStream audio_stream[AUDIOSTREAM_MAXCOUNT];	///< ��Ƶ��
}DVBService;

typedef struct tagSECFilter
{
public:
	U16 pid;				///<PSI/SI�� pid
							/// {[0]:table_id [1,2]:service_id [3]:Version 
							///  [4]:section_number [5]:last_section_number [6,7]:ts_id}
	U8 data[8];				///<֧�ֵĹ����ֶ�					
	U8 mask[8];				///<�ֶ�����
	U32 timeout;			///<��������ʱֵ

}SECFilter;

// EPG�¼��洢�ṹ
struct EpgEvent
{
	EpgEvent() 
		: 
	id(0xffff),
		start_time( 0 ), 
		end_time( 0 ) 
	{}

	U16 id;				 	 ///<�¼�ID
	std::string name;      	 ///<��Ŀ����.
	U32 start_time;      	 ///<��Ŀ��ʼʱ��.  
	U32 end_time;          	 ///<��Ŀ�����¼�.
	std::string description; ///<��Ŀ���.
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
		EAC3Audio      = 0x7a, //˽�ж���
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
typedef ::std::map<U16,EpgEventSet>		EPGEVENTS;	///<����-�¼�����ӳ���
typedef ::std::map<U16,ProgramEpg> 		EPGDataBaseT;

///////////////////////////////////////
//  TV Message
//////////////////////////////////////
#define TVNOTIFY_MINEPG				(100)	// PF �������֪ͨ
#define TVNOTIFY_EPGCOMPLETE		(101)	// EPG��Ŀָ�����֪ͨ
#define TVNOTIFY_TUNER_SIGNAL		(102)	// TUNERʵʱ�ź�״̬֪ͨ
#define TVNOTIFY_UPDATE_SERVICE		(103)	// ֪ͨ����DVBService(name,PMT and A/V pid)
#define TVNOTIFY_UPDATE_PROGRAM 	(104)	// ֪ͨ���½�Ŀ��Ϣ,������̨
#define TVNOTIFY_BUYMSG				(200)	// ���������տ���Ŀ����ʾ
#define TVNOTIFY_OSD				(201)	// ��ʾ/����OSD��Ϣ
#define TVNOTIFY_SHOW_FINGERPRINT	(202)	// ָ����ʾ
#define TVNOTIFY_SHOW_PROGRESSSTRIP	(203)	// ������ʾ
#define TVNOTIFY_MAIL_NOTIFY  		(204)	// ���ʼ�֪ͨ��Ϣ
#define TVNOTIFY_BUYIPP				(205)	// ʵʱ����IPP
#define TVNOTIFY_GENCYBROADCAST		(206)	// Ӧ���㲥
#define TVNOTIFY_MOTHER_CARDPAIR	(207)	// ��ĸ�������Ϣ 
#define TVNOTIFY_AREAlOCK			(208)	// ��Ƶ����������
#define TVNOTIFY_ENTITLE_CHANGE 	(300)	// ��Ȩ�ı�
#define TVNOTIFY_DETITLE  			(301)	// ����Ȩ֪ͨ��Ϣ

#define SHUMA_MSG					(10000) //

//TVNOTIFY_GENCYBROADCAST ��Ϣ
#define BROADCAST_STATUS_START		(1)		//��ʼӦ���㲥
#define BROADCAST_STATUS_STOP		(0)		//ֹͣӦ���㲥

//TVNOTIFY_MOTHER_CARDPAIR ��ĸ�������Ϣ
// ��ȡĸ����Ϣʧ�ܣ������Ҫ��Ե�ĸ��
#define CARDPAIR_GET_MOTHERINFO_FAILED				1	
// ���ڶ�ȡĸ����Ϣ�����Ե�
#define CARDPAIR_READING_MOTHERINFO					2	
// �ɹ���ȡĸ����Ϣ�������Ҫ��Ե��ӿ�
#define CARDPAIR_INSERT_SONCARD						3	
// ��ϲ����Գɹ������������Ҫ��Ե��ӿ�
#define CARDPAIR_PAIR_SUCCEED						4	
//���ʧ�ܣ������Ҫ��Ե��ӿ�
#define CARDPAIR_PAIR_FAILED						5	

//TVNOTIFY_AREAlOCK ��Ƶ����������
//��ʼ��ȡ������Ϣ
#define AREALOCK_BEGIN					0
//��ȡ������Ϣ����,�������,��ʱ,��Ƶʧ��
#define AREALOCK_END_OK					1
#define AREALOCK_END_TIMEOUT			2
#define AREALOCK_END_LOCK_FAILED		3


/*---------- CAS��ʾ��Ϣ---------*/
#define MESSAGE_CANCEL_TYPE      0x00  /* ȡ����ǰ����ʾ*/
#define MESSAGE_BADCARD_TYPE     0x01  /* �޷�ʶ��*/
#define MESSAGE_EXPICARD_TYPE    0x02  /* ���ܿ����ڣ�������¿�*/
#define MESSAGE_INSERTCARD_TYPE  0x03  /* ���Ž�Ŀ����������ܿ�*/
#define MESSAGE_NOOPER_TYPE      0x04  /* ���в����ڽ�Ŀ��Ӫ��*/
#define MESSAGE_BLACKOUT_TYPE    0x05  /* ��������*/
#define MESSAGE_OUTWORKTIME_TYPE 0x06  /* ��ǰʱ�α��趨Ϊ���ܹۿ�*/
#define MESSAGE_WATCHLEVEL_TYPE  0x07  /* ��Ŀ��������趨�Ĺۿ�����*/
#define MESSAGE_PAIRING_TYPE     0x08  /* ���ܿ��뱾�����в���Ӧ*/
#define MESSAGE_NOENTITLE_TYPE   0x09  /* û����Ȩ*/
#define MESSAGE_DECRYPTFAIL_TYPE 0x0A  /* ��Ŀ����ʧ��*/
#define MESSAGE_NOMONEY_TYPE	 0x0B  /* ���ڽ���*/
#define MESSAGE_ERRREGION_TYPE   0x0C  /* ������ȷ*/
#define MESSAGE_NEEDFEED_TYPE    0x0D  /* �ӿ���Ҫ��ĸ����Ӧ�������ĸ��*/
#define MESSAGE_ERRCARD_TYPE     0x0E  /* ���ܿ�У��ʧ�ܣ�����ϵ��Ӫ��*/
#define MESSAGE_UPDATE_TYPE      0x0F  /* ���ܿ������У��벻Ҫ�ο����߹ػ�*/
#define MESSAGE_LOWCARDVER_TYPE  0x10  /* ���������ܿ�*/
#define MESSAGE_VIEWLOCK_TYPE    0x11  /* ����Ƶ���л�Ƶ��*/
#define MESSAGE_MAXRESTART_TYPE  0x12  /* ���ܿ���ʱ������5 ���Ӻ����¿���*/
#define MESSAGE_FREEZE_TYPE      0x13  /* ���ܿ��Ѷ��ᣬ����ϵ��Ӫ��*/
#define MESSAGE_CALLBACK_TYPE    0x14  /* ���ܿ�����ͣ��ش����Ӽ�¼����Ӫ��*/
#define MESSAGE_CURTAIN_TYPE	  0x15 /*�߼�Ԥ����Ŀ���ý׶β�����ѹۿ�*/
#define MESSAGE_CARDTESTSTART_TYPE 0x16 /*�������Կ�������...*/
#define MESSAGE_CARDTESTFAILD_TYPE 0x17 /*�������Կ�����ʧ�ܣ��������ͨѶģ��*/
#define MESSAGE_CARDTESTSUCC_TYPE  0x18 /*�������Կ����Գɹ�*/
#define MESSAGE_NOCALIBOPER_TYPE    0x19/*���в�������ֲ�ⶨ����Ӫ��*/
#define MESSAGE_NULL_1A			0x1A
#define MESSAGE_NULL_1B			0x1B
#define MESSAGE_NULL_1C			0x1C
#define MESSAGE_NULL_1D			0x1D
#define MESSAGE_NULL_1E			0x1E
#define MESSAGE_NULL_1F			0x1F
#define MESSAGE_STBLOCKED_TYPE   0x20  /* ������������*/	
#define MESSAGE_STBFREEZE_TYPE   0x21  /* �����б�����*/

typedef struct tagMiniEPGNotify
{
	U16  ServiceID;
	char CurrentEventName[128];	// ��ǰ��Ŀ����
	U32  CurrentEventStartTime;	// ��ǰ��Ŀ��ʼʱ��
	U32  CurrentEventEndTime;	// ��ǰ��Ŀ����ʱ��
	char NextEventName[128];	// ��̽�Ŀ����
	U32  NextEventStartTime;	// ��̽�Ŀ��ʼʱ��
	U32  NextEventEndTime;		// ��̽�Ŀ����ʱ��
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
