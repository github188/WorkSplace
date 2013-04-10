// 一个相对完整的空接口实现文件
typedef unsigned  long  CDCA_U32;
typedef unsigned  short CDCA_U16;
typedef unsigned  char  CDCA_U8;
typedef signed long  CDCA_S32;

typedef CDCA_U8 CDCA_BOOL;

#define CDCA_RC_UNKNOWN 0x01

/*-- 邮件头 --*/
#define CDCA_MAXLEN_EMAIL_TITLE   30U   /* 邮件标题的长度 */
typedef struct {
	CDCA_U32   m_dwActionID;                 /* Email ID */
	CDCA_U32   m_tCreateTime;                /* EMAIL创建的时间 */
	CDCA_U16   m_wImportance;                /* 重要性： 0－普通，1－重要 */
	CDCA_U8    m_byReserved[2];              /* 保留 */
	char       m_szEmailHead[CDCA_MAXLEN_EMAIL_TITLE+1]; /* 邮件标题，最长为30 */    
	CDCA_U8    m_bNewEmail;                  /* 新邮件标记：0－已读邮件；1－新邮件 */
}SCDCAEmailHead;


/*-- 邮件内容 --*/
#define CDCA_MAXLEN_EMAIL_CONTENT 160U  /* 邮件内容的长度 */
typedef struct {
	    char     m_szEmail[CDCA_MAXLEN_EMAIL_CONTENT+1];      /* Email的正文 */
		    CDCA_U8  m_byReserved[3];              /* 保留 */
}SCDCAEmailContent;

/*-- IPPV节目信息 --*/
typedef CDCA_U16  CDCA_DATE;
typedef struct {
	CDCA_U32   m_dwProductID;   /* 节目的ID */
	CDCA_U8    m_byBookEdFlag;  /* 产品状态：BOOKING，VIEWED */ 
	CDCA_U8    m_bCanTape;      /* 是否可以录像：1－可以录像；0－不可以录像 */
	CDCA_U16   m_wPrice;        /* 节目价格 */
	CDCA_DATE  m_wExpiredDate;  /* 节目过期时间,IPPV用 */
	CDCA_U8    m_bySlotID;      /* 钱包ID */
	CDCA_U8    m_byReserved;    /* 保留 */
}SCDCAIppvInfo;

/*-- 运营商信息 --*/
#define CDCA_MAXLEN_TVSPRIINFO    32U   /* 运营商私有信息的长度 */
typedef struct {
	char     m_szTVSPriInfo[CDCA_MAXLEN_TVSPRIINFO+1];  /* 运营商私有信息 */
	CDCA_U8  m_byReserved[3];    /* 保留 */
}SCDCAOperatorInfo;

/*-- 授权信息 --*/
typedef struct {
	CDCA_U32  m_dwProductID;   /* 普通授权的节目ID */    
	CDCA_DATE m_tBeginDate;    /* 授权的起始时间 */
	CDCA_DATE m_tExpireDate;   /* 授权的过期时间 */
	CDCA_U8   m_bCanTape;      /* 用户是否购买录像：1－可以录像；0－不可以录像 */
	CDCA_U8   m_byReserved[3]; /* 保留 */
}SCDCAEntitle;

/*-- 授权信息集合 --*/
#define CDCA_MAXNUM_ENTITLE       300U  /* 授权产品的最大个数 */
typedef struct {
	CDCA_U16      m_wProductCount;
	CDCA_U8       m_m_byReserved[2];    /* 保留 */
	SCDCAEntitle  m_Entitles[CDCA_MAXNUM_ENTITLE]; /* 授权列表 */
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

