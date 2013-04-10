#include "typ.h"
#include "cdcas_hdr.h"
CDCA_BOOL CDCASTB_Init( CDCA_U8 byThreadPrior )
{
	return 0;
}
CDCA_BOOL CDCASTB_IsCDCa(CDCA_U16 wCaSystemID)
{
	return 0;
}
void CDCASTB_FormatBuffer( void )
{
}
void CDCASTB_RequestMaskBuffer(void)
{
}
void CDCASTB_RequestUpdateBuffer(void)
{
}
void CDCASTB_SetEmmPid(CDCA_U16 wEmmPid)
{
}
void CDCASTB_PrivateDataGot( CDCA_U8 byReqID, CDCA_BOOL bTimeout, CDCA_U16 wPid, const CDCA_U8* pbyReceiveData, CDCA_U16 wLen )
{
}
CDCA_BOOL CDCASTB_SCInsert( void )
{
	return 0;
}
void CDCASTB_SCRemove( void )
{
}
CDCA_U16 CDCASTB_GetCardSN( char* pCardSN )
{
	return 0;
}
CDCA_U16 CDCASTB_GetRating( CDCA_U8* pbyRating )
{
	return 0;
}
CDCA_U32 CDCASTB_GetVer( void )
{
	return 0;
}
CDCA_U16 CDCASTB_GetOperatorIds( CDCA_U16* pwTVSID )
{
	return 0;
}
CDCA_U16 CDCASTB_GetACList( CDCA_U16 wTVSID, CDCA_U32* pACArray )
{
	return 0;
}
CDCA_U16 CDCASTB_GetSlotIDs( CDCA_U16 wTVSID, CDCA_U8* pbySlotID )
{
	return 0;
}
CDCA_BOOL CDCASTB_GetDetitleReaded( CDCA_U16 wTvsID )
{
	return 0;
}
CDCA_U16 CDCASTB_GetPlatformID( void )
{
	return 0;
}
void CDCASTB_DelEmail( CDCA_U32 dwEmailID )
{
}
void CDCASTB_RefreshInterface( void )
{
}


void CDCASTB_SetEcmPid( CDCA_U8 byType, const SCDCASServiceInfo* pServiceInfo )
{
}
CDCA_U16 CDCASTB_GetEmailHead( CDCA_U32        dwEmailID, SCDCAEmailHead* pEmailHead )
{
	return 0;
}
CDCA_U16 CDCASTB_GetEmailContent( CDCA_U32           dwEmailID, SCDCAEmailContent* pEmailContent )
{
	return 0;
}
CDCA_U16 CDCASTB_GetEmailSpaceInfo( CDCA_U8* pbyEmailNum, CDCA_U8* pbyEmptyNum )
{
	return 0;
}
CDCA_U16 CDCASTB_ChangePin( const CDCA_U8* pbyOldPin, const CDCA_U8* pbyNewPin)
{
	return 0;
}
CDCA_U16 CDCASTB_SetRating( const CDCA_U8* pbyPin, CDCA_U8 byRating )
{
	return 0;
}
CDCA_U16 CDCASTB_GetWorkTime( CDCA_U8* pbyStartHour,
		CDCA_U8* pbyStartMin,
		CDCA_U8* pbyStartSec,
		CDCA_U8* pbyEndHour,
		CDCA_U8* pbyEndMin,
		CDCA_U8* pbyEndSec   )
{
	return 0;
}
CDCA_U16 CDCASTB_SetWorkTime( const CDCA_U8* pbyPin,
		CDCA_U8        byStartHour,
		CDCA_U8        byStartMin,
		CDCA_U8        byStartSec,
		CDCA_U8        byEndHour,
		CDCA_U8        byEndMin,
		CDCA_U8        byEndSec    )
{
	return 0;
}
CDCA_U16 CDCASTB_IsPaired( CDCA_U8* pbyNum, CDCA_U8* pbySTBID_List )
{
	return 0;
}
CDCA_U16 CDCASTB_GetOperatorInfo( CDCA_U16           wTVSID, SCDCAOperatorInfo* pOperatorInfo )
{
	return 0;
}
CDCA_U16 CDCASTB_GetServiceEntitles( CDCA_U16       wTVSID, SCDCAEntitles* pServiceEntitles )
{
	return 0;
}
CDCA_U16 CDCASTB_StopIPPVBuyDlg( CDCA_BOOL       bBuyProgram,
		CDCA_U16        wEcmPid,
		const CDCA_U8*  pbyPinCode,
		const SCDCAIPPVPrice* pPrice )
{
	return 0;
}
CDCA_U16 CDCASTB_GetIPPVProgram( CDCA_U16       wTvsID, SCDCAIppvInfo* pIppv, CDCA_U16*      pwNumber )
{
	return 0;
}
CDCA_U16 CDCASTB_GetSlotInfo( CDCA_U16          wTVSID, CDCA_U8           bySlotID, SCDCATVSSlotInfo* pSlotInfo )
{
	return 0;
}
CDCA_U16 CDCASTB_GetEntitleIDs( CDCA_U16  wTVSID, CDCA_U32* pdwEntitleIds )
{
	return 0;
}
CDCA_U16 CDCASTB_GetDetitleChkNums( CDCA_U16   wTvsID, CDCA_BOOL* bReadFlag, CDCA_U32*  pdwDetitleChkNums)
{
	return 0;
}
CDCA_BOOL CDCASTB_DelDetitleChkNum( CDCA_U16 wTvsID, CDCA_U32 dwDetitleChkNum )
{
	return 0;
}
CDCA_U16 CDCASTB_ReadFeedDataFromParent( CDCA_U16 wTVSID, CDCA_U8* pbyFeedData, CDCA_U8* pbyLen     )
{
	return 0;
}
CDCA_U16 CDCASTB_WriteFeedDataToChild( CDCA_U16       wTVSID, const CDCA_U8* pbyFeedData, CDCA_U8        byLen    )
{
	return 0;
}
CDCA_U16 CDCASTB_GetOperatorChildStatus( CDCA_U16   wTVSID,
		CDCA_U8*   pbyIsChild,
		CDCA_U16*   pwDelayTime,
		CDCA_TIME* pLastFeedTime,
		char*      pParentCardSN,
		CDCA_BOOL *pbIsCanFeed )
{
	return 0;
}
