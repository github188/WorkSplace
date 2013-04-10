// һ����������Ŀսӿ�ʵ���ļ�
typedef unsigned  long  CDCA_U32;
typedef unsigned  short CDCA_U16;
typedef unsigned  char  CDCA_U8;
typedef signed long  CDCA_S32;

typedef CDCA_U8 CDCA_BOOL;

#define CDCA_RC_UNKNOWN 0x01

/*-- �ʼ�ͷ --*/
#define CDCA_MAXLEN_EMAIL_TITLE   30U   /* �ʼ�����ĳ��� */
typedef struct {
	CDCA_U32   m_dwActionID;                 /* Email ID */
	CDCA_U32   m_tCreateTime;                /* EMAIL������ʱ�� */
	CDCA_U16   m_wImportance;                /* ��Ҫ�ԣ� 0����ͨ��1����Ҫ */
	CDCA_U8    m_byReserved[2];              /* ���� */
	char       m_szEmailHead[CDCA_MAXLEN_EMAIL_TITLE+1]; /* �ʼ����⣬�Ϊ30 */    
	CDCA_U8    m_bNewEmail;                  /* ���ʼ���ǣ�0���Ѷ��ʼ���1�����ʼ� */
}SCDCAEmailHead;


/*-- �ʼ����� --*/
#define CDCA_MAXLEN_EMAIL_CONTENT 160U  /* �ʼ����ݵĳ��� */
typedef struct {
	    char     m_szEmail[CDCA_MAXLEN_EMAIL_CONTENT+1];      /* Email������ */
		    CDCA_U8  m_byReserved[3];              /* ���� */
}SCDCAEmailContent;

/*-- IPPV��Ŀ��Ϣ --*/
typedef CDCA_U16  CDCA_DATE;
typedef struct {
	CDCA_U32   m_dwProductID;   /* ��Ŀ��ID */
	CDCA_U8    m_byBookEdFlag;  /* ��Ʒ״̬��BOOKING��VIEWED */ 
	CDCA_U8    m_bCanTape;      /* �Ƿ����¼��1������¼��0��������¼�� */
	CDCA_U16   m_wPrice;        /* ��Ŀ�۸� */
	CDCA_DATE  m_wExpiredDate;  /* ��Ŀ����ʱ��,IPPV�� */
	CDCA_U8    m_bySlotID;      /* Ǯ��ID */
	CDCA_U8    m_byReserved;    /* ���� */
}SCDCAIppvInfo;

/*-- ��Ӫ����Ϣ --*/
#define CDCA_MAXLEN_TVSPRIINFO    32U   /* ��Ӫ��˽����Ϣ�ĳ��� */
typedef struct {
	char     m_szTVSPriInfo[CDCA_MAXLEN_TVSPRIINFO+1];  /* ��Ӫ��˽����Ϣ */
	CDCA_U8  m_byReserved[3];    /* ���� */
}SCDCAOperatorInfo;

/*-- ��Ȩ��Ϣ --*/
typedef struct {
	CDCA_U32  m_dwProductID;   /* ��ͨ��Ȩ�Ľ�ĿID */    
	CDCA_DATE m_tBeginDate;    /* ��Ȩ����ʼʱ�� */
	CDCA_DATE m_tExpireDate;   /* ��Ȩ�Ĺ���ʱ�� */
	CDCA_U8   m_bCanTape;      /* �û��Ƿ���¼��1������¼��0��������¼�� */
	CDCA_U8   m_byReserved[3]; /* ���� */
}SCDCAEntitle;

/*-- ��Ȩ��Ϣ���� --*/
#define CDCA_MAXNUM_ENTITLE       300U  /* ��Ȩ��Ʒ�������� */
typedef struct {
	CDCA_U16      m_wProductCount;
	CDCA_U8       m_m_byReserved[2];    /* ���� */
	SCDCAEntitle  m_Entitles[CDCA_MAXNUM_ENTITLE]; /* ��Ȩ�б� */
}SCDCAEntitles;

CDCA_U16 CDCASTB_ChangePin( const CDCA_U8* pbyOldPin, const CDCA_U8* pbyNewPin)
{
	return CDCA_RC_UNKNOWN;
}

void CDCASTB_DelEmail( CDCA_U32 dwEmailID )
{
//	return CDCA_RC_UNKNOWN;
}

CDCA_U16 CDCASTB_GetEmailContent( CDCA_U32           dwEmailID, SCDCAEmailContent* pEmailContent )
{
	return CDCA_RC_UNKNOWN;
}

CDCA_U16 CDCASTB_GetEmailHeads( CDCA_U32        dwEmailID, SCDCAEmailHead* pEmailHead )
{
	return CDCA_RC_UNKNOWN;
}
CDCA_U16 CDCASTB_GetEmailHead( CDCA_U32        dwEmailID, SCDCAEmailHead* pEmailHead )
{
	return CDCA_RC_UNKNOWN;
}

CDCA_U16 CDCASTB_GetEmailSpaceInfo( CDCA_U8* pbyEmailNum, CDCA_U8* pbyEmptyNum )
{
	return CDCA_RC_UNKNOWN;
}

CDCA_U16 CDCASTB_GetIPPVProgram( CDCA_U16       wTvsID, SCDCAIppvInfo* pIppv, CDCA_U16*      pwNumber )
{
	return CDCA_RC_UNKNOWN;
}

CDCA_U16 CDCASTB_GetOperatorIds( CDCA_U16* pwTVSID )
{
	return CDCA_RC_UNKNOWN;
}

CDCA_U16 CDCASTB_GetOperatorInfo( CDCA_U16  wTVSID, SCDCAOperatorInfo* pOperatorInfo )
{
	return CDCA_RC_UNKNOWN;
}

CDCA_U16 CDCASTB_GetRating( CDCA_U8* pbyRating )
{
	return CDCA_RC_UNKNOWN;
}

CDCA_U16 CDCASTB_GetEntitleIDs( CDCA_U16  wTVSID, CDCA_U32* pdwEntitleIds )
{
	return CDCA_RC_UNKNOWN;
}

CDCA_U32 CDCASTB_GetVer( void )
{
	return CDCA_RC_UNKNOWN;
}

CDCA_U16 CDCASTB_GetWorkTime( CDCA_U8* pbyStartHour,
		CDCA_U8* pbyStartMin,
		CDCA_U8* pbyStartSec,
		CDCA_U8* pbyEndHour,
		CDCA_U8* pbyEndMin,
		CDCA_U8* pbyEndSec   )
{
	return CDCA_RC_UNKNOWN;
}

CDCA_U16 CDCASTB_IsPaired( CDCA_U8* pbyNum, CDCA_U8* pbySTBID_List )
{
	return CDCA_RC_UNKNOWN;
}

CDCA_U16 CDCASTB_SetRating( const CDCA_U8* pbyPin, CDCA_U8 byRating )
{
	return CDCA_RC_UNKNOWN;
}

CDCA_U16 CDCASTB_SetWorkTime( const CDCA_U8* pbyPin,
		CDCA_U8        byStartHour,
		CDCA_U8        byStartMin,
		CDCA_U8        byStartSec,
		CDCA_U8        byEndHour,
		CDCA_U8        byEndMin,
		CDCA_U8        byEndSec    )
{
	return CDCA_RC_UNKNOWN;
}
CDCA_U16 CDCASTB_GetServiceEntitles( CDCA_U16       wTVSID, SCDCAEntitles* pServiceEntitles )
{
		return CDCA_RC_UNKNOWN;
}
CDCA_U16 CDCASTB_GetACList( CDCA_U16 wTVSID, CDCA_U32* pACArray )
{
		return CDCA_RC_UNKNOWN;
}

