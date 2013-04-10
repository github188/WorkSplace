#ifndef _SM_H_
#define _SM_H_
#ifdef  __cplusplus
extern "C" {
#endif
#include "../../utcas_interface.h"

	
int SM_sc_apdu_t0(UTCA_U8* send_buf, UTCA_U8 send_len, UTCA_U8 *rec_buf, UTCA_U8 *rec_len);

/*------ CA_LIB���ȹ��� ------*/

/* CA_LIB��ʼ�� */
UTCA_BOOL SM_Init( UTCA_U8 byThreadPrior );
UTCA_BOOL GetCardNO(void);

/* �ر�CA_LIB���ͷ���Դ */
void SM_Close( void );

/* UTCASͬ���ж� */
UTCA_BOOL  SM_IsUTCA(UTCA_U16 wCaSystemID);


/*------ Flash���� ------ */

/* �洢�ռ�ĸ�ʽ�� */
void SM_FormatBuffer( void );

/* ���ζԴ洢�ռ�Ķ�д���� */
void SM_RequestMaskBuffer(void);

/* �򿪶Դ洢�ռ�Ķ�д���� */
void SM_RequestUpdateBuffer(void);


/*------ TS������ ------*/

/* ����ECM�ͽ�Ŀ��Ϣ */
void SM_SetEcmPid( UTCA_U8 byType,
                               const SCDCASServiceInfo* pServiceInfo );

/* ����EMM��Ϣ */
void SM_SetEmmPid(UTCA_U16 wEmmPid);

/* ˽�����ݽ��ջص� */
void SM_PrivateDataGot( UTCA_U8        byReqID,
								  	UTCA_BOOL      bTimeout,
									UTCA_U16       wPid,
									const UTCA_U8* pbyReceiveData,
									UTCA_U16       wLen            );

/*------- ���ܿ����� -------*/

/* �������ܿ� */
UTCA_BOOL SM_SCInsert( void );

/* �γ����ܿ�*/
void SM_SCRemove( void );

/* ��ȡ���ܿ��ⲿ���� */
UTCA_U16 SM_GetCardSN( char* pCardSN );


/*------- ������Ϣ��ѯ -------*/

/* ��ѯCA_LIB�汾�� */
UTCA_U32 SM_GetVer( void );

/* ��ѯ������ƽ̨��� */
UTCA_U16 SM_GetPlatformID( void );


UTCA_BOOL SM_ParseEcm(UTCA_U8* EcmBuf,UTCA_U16  wEcmLen);

UTCA_BOOL SM_ParseEmm(UTCA_U8* EmmBuf,UTCA_U16  wEmmLen);

void SM_GetCW(UTCA_U8* cwOdd, UTCA_U8* cwEven);


void SM_GetCAInterface(CA_Interface * Inter);




#ifdef  __cplusplus
}
#endif
#endif /* _SM_H_ */
