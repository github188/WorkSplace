#ifndef __PLUGIN_CA_H__
#define __PLUGIN_CA_H__
#include "../utcas_priv.h"
#include "../utcasa.h"

////////////////////////////////////////////////////////////////////////////////
// ����ָ�����Ͷ���
////////////////////////////////////////////////////////////////////////////////
/*------ CA_LIB���ȹ��� ------*/
/* CA_LIB��ʼ�� */
typedef UTCA_BOOL (*CAFunc_Init)( UTCA_U8 byThreadPrior );
/* �ر�CA_LIB���ͷ���Դ */
typedef void (*CAFunc_Close)( void );
/* UTCASͬ���ж� */
typedef UTCA_BOOL  (*CAFunc_IsUTCA)(UTCA_U16 wCaSystemID);

/*------ Flash���� ------ */
/* �洢�ռ�ĸ�ʽ�� */
typedef void (*CAFunc_FormatBuffer)( void );
/* ���ζԴ洢�ռ�Ķ�д���� */
typedef void (*CAFunc_RequestMaskBuffer)(void);
/* �򿪶Դ洢�ռ�Ķ�д���� */
typedef void (*CAFunc_RequestUpdateBuffer)(void);

/*------ TS������ ------*/
/* ����ECM�ͽ�Ŀ��Ϣ */
typedef void (*CAFunc_SetEcmPid)( UTCA_U8  byType,
		const SCDCASServiceInfo* pServiceInfo );
/* ����EMM��Ϣ */
typedef void (*CAFunc_SetEmmPid)(UTCA_U16 wEmmPid);
/* ˽�����ݽ��ջص� */
typedef void (*CAFunc_PrivateDataGot)( UTCA_U8        byReqID,
		UTCA_BOOL      bTimeout,
		UTCA_U16       wPid,
		const UTCA_U8* pbyReceiveData,
		UTCA_U16       wLen            );

/*------- ���ܿ����� -------*/
/* �������ܿ� */
typedef UTCA_BOOL (*CAFunc_SCInsert)( void );
/* �γ����ܿ�*/
typedef void (*CAFunc_SCRemove)( void );
/* ��ȡ���ܿ��ⲿ���� */
typedef UTCA_U16 (*CAFunc_GetCardSN)( char* pCardSN );

/*------- ������Ϣ��ѯ -------*/
/* ��ѯCA_LIB�汾�� */
typedef UTCA_U32 (*CAFunc_GetVer)( void );
/* ��ѯ������ƽ̨��� */
typedef UTCA_U16 (*CAFunc_GetPlatformID)( void );
typedef void (*CAFunc_SetAtr)(ATR_S* art);
typedef UTCA_BOOL (*CAFunc_ParseEcm)(UTCA_U8* EcmBuf,UTCA_U16  wEcmLen);
typedef UTCA_BOOL (*CAFunc_ParseEmm)(UTCA_U8* EmmBuf,UTCA_U16  wEmmLen);
typedef void (*CAFunc_GetCW)(UTCA_U8* cwOdd, UTCA_U8* cwEven);

////////////////////////////////////////////////////////////////////////////////
// CA_Interface �ṹ���Ͷ���
////////////////////////////////////////////////////////////////////////////////
typedef struct {
	/* CA_LIB��ʼ�� */
	CAFunc_Init CA_Init ;
	/* �ر�CA_LIB���ͷ���Դ */
	CAFunc_Close CA_Close;
	/* UTCASͬ���ж� */
	CAFunc_IsUTCA CA_IsUTCA;
	/*------ Flash���� ------ */
	/* �洢�ռ�ĸ�ʽ�� */
	CAFunc_FormatBuffer CA_FormatBuffer;
	/* ���ζԴ洢�ռ�Ķ�д���� */
	CAFunc_RequestMaskBuffer CA_RequestMaskBuffer ;
	/* �򿪶Դ洢�ռ�Ķ�д���� */
	CAFunc_RequestUpdateBuffer CA_RequestUpdateBuffer;
	/*------ TS������ ------*/
	/* ����ECM�ͽ�Ŀ��Ϣ */
	CAFunc_SetEcmPid CA_SetEcmPid;
	/* ����EMM��Ϣ */
	CAFunc_SetEmmPid CA_SetEmmPid;
	/* ˽�����ݽ��ջص� */
	CAFunc_PrivateDataGot CA_PrivateDataGot;
	/*------- ���ܿ����� -------*/
	/* �������ܿ� */
	CAFunc_SCInsert CA_SCInsert;
	/* �γ����ܿ�*/
	CAFunc_SCRemove CA_SCRemove;
	/* ��ȡ���ܿ��ⲿ���� */
	CAFunc_GetCardSN CA_GetCardSN;
	/*------- ������Ϣ��ѯ -------*/
	/* ��ѯCA_LIB�汾�� */
	CAFunc_GetVer CA_GetVer;
	/* ��ѯ������ƽ̨��� */
	CAFunc_GetPlatformID CA_GetPlatformID;
	CAFunc_SetAtr CA_SetAtr;
	CAFunc_ParseEcm CA_ParseEcm;
	CAFunc_ParseEmm CA_ParseEmm;
	CAFunc_GetCW CA_GetCW;
}CA_Interface;

////////////////////////////////////////////////////////////////////////////////
// �����ĺ�������
////////////////////////////////////////////////////////////////////////////////
#ifdef  __cplusplus
extern "C" {
#endif
	int GetCA_Num();
    // ���˺������ã�����������
	void GetCAInterface(CA_Interface * Inter,const char* CA_Name);
	/*------ CA_LIB���ȹ��� ------*/
	/* CA_LIB��ʼ�� */
	UTCA_BOOL CA_Default_Init( UTCA_U8 byThreadPrior );
	/* �ر�CA_LIB���ͷ���Դ */
	void CA_Default_Close( void );
	/* UTCASͬ���ж� */
	UTCA_BOOL  CA_Default_IsUTCA(UTCA_U16 wCaSystemID);

	/*------ Flash���� ------ */
	/* �洢�ռ�ĸ�ʽ�� */
	void CA_Default_FormatBuffer( void );
	/* ���ζԴ洢�ռ�Ķ�д���� */
	void CA_Default_RequestMaskBuffer(void);
	/* �򿪶Դ洢�ռ�Ķ�д���� */
	void CA_Default_RequestUpdateBuffer(void);

	/*------ TS������ ------*/
	/* ����ECM�ͽ�Ŀ��Ϣ */
	void CA_Default_SetEcmPid( UTCA_U8  byType,
			const SCDCASServiceInfo* pServiceInfo );
	/* ����EMM��Ϣ */
	void CA_Default_SetEmmPid(UTCA_U16 wEmmPid);
	/* ˽�����ݽ��ջص� */
	void CA_Default_PrivateDataGot( UTCA_U8        byReqID,
			UTCA_BOOL      bTimeout,
			UTCA_U16       wPid,
			const UTCA_U8* pbyReceiveData,
			UTCA_U16       wLen            );

	/*------- ���ܿ����� -------*/
	/* �������ܿ� */
	UTCA_BOOL CA_Default_SCInsert( void );
	/* �γ����ܿ�*/
	void CA_Default_SCRemove( void );
	/* ��ȡ���ܿ��ⲿ���� */
	UTCA_U16 CA_Default_GetCardSN( char* pCardSN );

	/*------- ������Ϣ��ѯ -------*/
	/* ��ѯCA_LIB�汾�� */
	UTCA_U32 CA_Default_GetVer( void );
	/* ��ѯ������ƽ̨��� */
	UTCA_U16 CA_Default_GetPlatformID( void );
	void CA_Default_SetAtr(ATR_S* art);
	UTCA_BOOL CA_Default_ParseEcm(UTCA_U8* EcmBuf,UTCA_U16  wEcmLen);
	void CA_Default_GetCW(UTCA_U8* cwOdd, UTCA_U8* cwEven);

	
	char *MyPrintHex(UTCA_U8 * pBuf,UTCA_U16 size);
#ifdef  __cplusplus
}
#endif

#endif//__PLUGIN_CA_H__
