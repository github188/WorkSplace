#ifndef STB_PC_CAPTURE_DEF_H
#define STB_PC_CAPTURE_DEF_H

#include "typ.h"
#include <string>
#include <vector>

#define MESSAGE_GET_VER         			0x0001
#define MESSAGE_SET_SERVICE_ID				0x0008
// 
#define MESSAGE_GET_EVENT_NOTIFY			0x000a
#define MESSAGE_CANCEL_EVENT_NOTIFY			0x000b
//#define MESSAGE_GET_VER         			0x000c

// email
#define MESSAGE_GET_EMAIL_HEADERS			0x0010
#define MESSAGE_GET_EMAIL_HEADERS_REVERSE	0x0011
#define MESSAGE_GET_EMAIL_HEADER			0x0012
#define MESSAGE_GET_EMAIL_CONTENT			0x0013
#define MESSAGE_DELETE_EMAIL				0x0014
#define MESSAGE_GET_EMAILBOX_INFO			0x0015
#define MESSAGE_ENTER_EMAIL_MANAGER			0x0016
#define MESSAGE_LEAVE_EMAIL_MANAGER			0x0017
#define EVENT_EMAIL_NOTIFY					0x0018

// Card Manager
#define MESSAGE_CHANGE_PIN					0x0020

//#define MESSAGE_CHECK_PIN_LOCK			0x0021
#define MESSAGE_GET_RATING					0x0022
#define MESSAGE_SET_RATING					0x0023
#define MESSAGE_GET_WORKTIME				0x0024
#define MESSAGE_SET_WORKTIME				0x0025
#define MESSAGE_CHECK_STB_CARD_PAIRED		0x0026
#define MESSAGE_GET_PLATFORM_ID      		0x0027
#define MESSAGE_GET_OPERATOR_ID				0x0028
#define MESSAGE_GET_OPERATOR_INFO			0x0029
#define MESSAGE_GET_AC_LIST					0x002A
#define MESSAGE_GET_SERVICE_ENTITLEMENT		0x002B
#define MESSAGE_GET_CARDSN		            0x002C
#define MESSAGE_GET_STBID		            0x002D

// OSD
#define EVENT_OSD_NOTIFY					0x0030
#define EVENT_HIDE_OSD_NOTIFY				0x0031
#define EVENT_CANNOT_PLAY_PROGRAM_NOTIFY	0x0032
#define EVENT_SHOW_FINGERPRINT_NOTIFY		0x0033
#define MESSAGE_REFRESH_WINDOW				0x0034
#define EVENT_SHOW_PROGRESSSTRIP            0x0037

// IPPV 
#define EVENT_IPPV_PROGRAM_NOTIFY			0x0040
#define EVENT_HIDE_IPPV_PROGRAM_NOTIFY		0x0041
#define MESSAGE_BUY_IPPV_PROGRAM			0x0042
#define MESSAGE_GET_IPPV_PROGRAM			0x0043
#define MESSAGE_GET_SLOT_ID					0x0045
#define MESSAGE_GET_SLOT_INFO				0x0046

// Lock Service
#define EVENT_LOCK_SERVICE_NOTIFY			0x0050
#define EVENT_UNLOCK_SERVICE_NOTIFY			0x0051

//Entitle Manager
#define EVENT_ENTITLECHANGED_NOTIFY			0x0060
#define EVENT_DETITLERECEIVED_NOTIFY		0x0061
#define MESSAGE_GET_ENTITLEID 			    0x0062
#define MESSAGE_GET_DETITLE_CHKNUMS 	    0x0063
#define MESSAGE_GET_DETITLE_READED 		    0x0064
#define MESSAGE_DEL_DETITLE_CHKNUM 		    0x0065

//Basic/Additional Card
#define EVENT_REQUESTFEEDING_NOTIFY         0x0070 
#define MESSAGE_READFEEDDATAFROMPARENT      0x0071
#define MESSAGE_WRITEFEEDDATATOCHILD        0x0072
#define MESSAGE_GETOPERATORCHILDSTATUS      0x0073
#define MAX_EVENT_COUNT	64
#define FLASH_FILE_SIZE 0x128000

#define MAXLEN_EMAIL_CONTENT				160
#define MAXLEN_EMAIL_TITLE					30 /* �ʼ����⣬�Ϊ30 */    

////////////////////////////////////////////////////////////////////////////////
// enum defines
////////////////////////////////////////////////////////////////////////////////
/// CAS���Ͷ���
enum CasType
{
	CasType_NONE,  ///<��CAS
	CasType_TFCAS, ///<TFCAS
	CasType_SMCAS, ///<shuma
	CasType_GHCAS,///<gehua
	CasType_CDCAS  ///<CDCAS
};
/// �ʼ��ȼ����Ͷ���.
enum EmailLevel
{
	EmailLevel_Normal,  ///<��ͨ�ȼ�
	EmailLevel_Important///<��Ҫ�ȼ�
};
////////////////////////////////////////////////////////////////////////////////
// type defines
////////////////////////////////////////////////////////////////////////////////
/// �ʼ�ID���Ͷ���
typedef U32 EmailId;
typedef U32 DateTime;

/// ��Ӫ��ID���Ͷ���
typedef U16 OperatorId;
typedef std::vector<OperatorId> OperatorIDListT;

/// Ǯ��ID���Ͷ���
typedef U8 PurseId;
////////////////////////////////////////////////////////////////////////////////
// struct defines
////////////////////////////////////////////////////////////////////////////////
typedef struct tagDescrambling
{
	unsigned short 	ecmPid;
	unsigned int 	streamPidCount;
	unsigned short 	streamPid[8];
	unsigned short  wServiceID;
} Descrambling;
typedef struct  tagReqIDPID
{
	BYTE byReqID;
	WORD wPid;
}RegIDPID;
/*
/// �ʼ�ͷ���Ͷ���
struct EmailHead
{
	U32 email_id;            ///<�ʼ�id
	bool new_email;          ///<�Ƿ������ʼ�
	std::string email_title; ///<�ʼ�����
	DateTime send_time;      ///<�ʼ�����ʱ��
	EmailLevel email_level;  ///<�ʼ�����
};
*/
typedef struct {
    U32   email_id;                 /* Email ID */
    U32   send_time;                /* EMAIL������ʱ�� */
    //EmailLevel   email_level;               /* ��Ҫ�ԣ� 0����ͨ��1����Ҫ */
    U16   email_level;               /* ��Ҫ�ԣ� 0����ͨ��1����Ҫ */
    U8    m_byReserved[2];              /* ���� */
	char  email_title[MAXLEN_EMAIL_TITLE+1]; /* �ʼ����⣬�Ϊ30 */    
    U8    new_email;                  /* ���ʼ���ǣ�0���Ѷ��ʼ���1�����ʼ� */
}EmailHead;

/*-- �ʼ����� --*/
struct EmailContent{
    char    m_szEmail[MAXLEN_EMAIL_CONTENT+1];      /* Email������ */
    U8  	m_byReserved[3];              /* ���� */
};

/// ��Ȩ��Ϣ���Ͷ���
struct Entitle
{
	U32 product_id;        ///<��Ȩ�Ľ�ĿID
	bool is_record;        ///<�Ƿ����¼��
	DateTime start_time;   ///<��Ŀ��ʼ����
	DateTime expired_time; ///<��Ŀ��������
};
typedef std::vector<Entitle>	EntitleListT;

/// ��Ӫ����Ϣ���Ͷ���
struct OperatorInfo
{
	/**
	 * ���캯��
	 * 
	 * \param aname ��Ӫ������.
	 */
	OperatorInfo( std::string const& aname ) : name( aname ) {}
	std::string name;///<��Ӫ������.
};

/// Ǯ����Ϣ���Ͷ���
struct PurseInfo
{
	PurseInfo( U32 cl, U32 b ) : credit_limit( cl ) , balance( b ) {}
	U32 credit_limit; ///<���ö�
	U32 balance;      ///<�ѻ��ѵĵ���
};

#define MAX_RECEIVE_CAMSG_COUNT	32
#define MAX_RECEIVE_CAMSG_LENGTH	4096

typedef struct _NTFCA_IOCtl_OutMsg
{
	ULONG msgLength;						/* message length*/
	UCHAR msgData[MAX_RECEIVE_CAMSG_LENGTH]; /* message data array*/
} ReceiveFromCaMsg;

#endif //defined(STB_PC_CAPTURE_DEF_H)
