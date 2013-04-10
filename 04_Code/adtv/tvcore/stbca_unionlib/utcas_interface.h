#ifndef _UTCAS_INTERFACE_H_
#define _UTCAS_INTERFACE_H_

#include "utcasa.h"

#ifdef  __cplusplus
extern "C" {
#endif

/*------ CA_LIB调度管理 ------*/
UTCA_BOOL CDCASTB_Init( UTCA_U8 byThreadPrior );
void CDCASTB_Close( void );
UTCA_BOOL  CDCASTB_IsUTCA(UTCA_U16 wCaSystemID);

/*------ TS流管理 ------*/
void CDCASTB_SetEcmPid( UTCA_U8 byType, const SCDCASServiceInfo* pServiceInfo );
void  CDCASTB_SetEmmPid(UTCA_U16 wEmmPid);

void CDCASTB_PrivateDataGot( UTCA_U8        byReqID,
						  	 UTCA_BOOL      bTimeout,
							 UTCA_U16       wPid,
							 const UTCA_U8* pbyReceiveData,
							 UTCA_U16       wLen            );

/*------- 智能卡管理 -------*/
UTCA_BOOL CDCASTB_SCInsert( void );
void CDCASTB_SCRemove( void );
UTCA_U16 CDCASTB_GetCardSN( char* pCardSN );

#ifdef  __cplusplus
}
#endif
#endif /* _UTCAS_H_ */

