#ifndef _GH_H_
#define _GH_H_
#ifdef  __cplusplus
extern "C" {
#endif

#include "../../utcas_interface.h"

// CDSTBCA_SCPBRun ����	
// int gh_sc_apdu_t0(UTCA_U8* send_buf, UTCA_U8 send_len, UTCA_U8 *rec_buf, UTCA_U8 *rec_len);

/*------ CA_LIB���ȹ��� ------*/

/* CA_LIB��ʼ�� */
UTCA_BOOL GH_Init( UTCA_U8 byThreadPrior );

/* �ر�CA_LIB���ͷ���Դ */
void GH_Close( void );

/* UTCASͬ���ж� */
UTCA_BOOL  GH_IsUTCA(UTCA_U16 wCaSystemID);


/*------ Flash���� ------ */

/* �洢�ռ�ĸ�ʽ�� */
void GH_FormatBuffer( void );

/* ���ζԴ洢�ռ�Ķ�д���� */
void GH_RequestMaskBuffer(void);

/* �򿪶Դ洢�ռ�Ķ�д���� */
void GH_RequestUpdateBuffer(void);


/*------ TS������ ------*/

/* ����ECM�ͽ�Ŀ��Ϣ */
void GH_SetEcmPid( UTCA_U8 byType,
                               const SCDCASServiceInfo* pServiceInfo );

/* ����EMM��Ϣ */
void GH_SetEmmPid(UTCA_U16 wEmmPid);

/* ˽�����ݽ��ջص� */
void GH_PrivateDataGot( UTCA_U8        byReqID,
								  	UTCA_BOOL      bTimeout,
									UTCA_U16       wPid,
									const UTCA_U8* pbyReceiveData,
									UTCA_U16       wLen            );

/*------- ���ܿ����� -------*/

/* �������ܿ� */
UTCA_BOOL GH_SCInsert( void );

/* �γ����ܿ�*/
void GH_SCRemove( void );

/* ��ȡ���ܿ��ⲿ���� */
UTCA_U16 GH_GetCardSN( char* pCardSN );


/*------- ������Ϣ��ѯ -------*/

/* ��ѯCA_LIB�汾�� */
UTCA_U32 GH_GetVer( void );

/* ��ѯ������ƽ̨��� */
UTCA_U16 GH_GetPlatformID( void );


UTCA_BOOL GH_ParseEcm(UTCA_U8* EcmBuf,UTCA_U16  wEcmLen);

UTCA_BOOL GH_ParseEmm(UTCA_U8* EmmBuf,UTCA_U16  wEmmLen);

void GH_GetCW(UTCA_U8* cwOdd, UTCA_U8* cwEven);


void GH_GetCAInterface(CA_Interface * Inter);




#ifdef  __cplusplus
}
#endif
#endif /* _GH_H_ */
