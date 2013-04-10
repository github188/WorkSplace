#include "stdio.h"
#include "string.h"
#include "pluginca.h"
#include "shuma/shuma.h"
#include "gehua/gh.h"
// #include "DVN\DVN.h"


#ifdef  __cplusplus
extern "C" {
#endif

	/*
	*����֧�ֵ�CA����
	*/
	int GetCA_Num()
	{
		return 1;
	}

	/*
	*Inter:����CA�ӿ�ָ��
	*index:CA������������ֵС��GetCA_Num��������ֵ��һ
	*/
	void GetCAInterface(CA_Interface * Inter,const char* CA_Name)
	{
		/*
		if( strcmp(CA_Name,"DVN",3) == 0 ){
			DVN_GetCAInterface(Inter);
		}
		*/
		if( strcmp(CA_Name,"SHUMA") == 0 ){
			SM_GetCAInterface(Inter);
		}
		else if(strcmp(CA_Name, "GEHUA") == 0)
		{
			GH_GetCAInterface(Inter);
		}
	}

	/*------ CA_LIB���ȹ��� ------*/

/* CA_LIB��ʼ�� */
UTCA_BOOL CA_Default_Init( UTCA_U8 byThreadPrior )
{
	return UTCA_FALSE;
}

/* �ر�CA_LIB���ͷ���Դ */
void CA_Default_Close( void )
{
}

/* UTCASͬ���ж� */
UTCA_BOOL  CA_Default_IsUTCA(UTCA_U16 wCaSystemID)
{
	return UTCA_FALSE;
}


/*------ Flash���� ------ */

/* �洢�ռ�ĸ�ʽ�� */
void CA_Default_FormatBuffer( void )
{
}

/* ���ζԴ洢�ռ�Ķ�д���� */
void CA_Default_RequestMaskBuffer(void)
{
}

/* �򿪶Դ洢�ռ�Ķ�д���� */
void CA_Default_RequestUpdateBuffer(void)
{
}


/*------ TS������ ------*/

/* ����ECM�ͽ�Ŀ��Ϣ */
void CA_Default_SetEcmPid( UTCA_U8  byType,
                               const SCDCASServiceInfo* pServiceInfo )
{
}

/* ����EMM��Ϣ */
void CA_Default_SetEmmPid(UTCA_U16 wEmmPid)
{
}

/* ˽�����ݽ��ջص� */
void CA_Default_PrivateDataGot( UTCA_U8        byReqID,
								  	UTCA_BOOL      bTimeout,
									UTCA_U16       wPid,
									const UTCA_U8* pbyReceiveData,
									UTCA_U16       wLen            )
{
}

/*------- ���ܿ����� -------*/

/* �������ܿ� */
UTCA_BOOL CA_Default_SCInsert( void )
{
	return UTCA_TRUE;
}

/* �γ����ܿ�*/
void CA_Default_SCRemove( void )
{
}

/* ��ȡ���ܿ��ⲿ���� */
UTCA_U16 CA_Default_GetCardSN( char* pCardSN )
{
	return 0;
}

/*------- ������Ϣ��ѯ -------*/

/* ��ѯCA_LIB�汾�� */
UTCA_U32 CA_Default_GetVer( void )
{
	return 0;
}

/* ��ѯ������ƽ̨��� */
UTCA_U16 CA_Default_GetPlatformID( void )
{
	return 0;
}

void CA_Default_SetAtr(ATR_S* art)
{
}

UTCA_BOOL CA_Default_ParseEcm(UTCA_U8* EcmBuf,UTCA_U16  wEcmLen)
{
	return UTCA_TRUE;
}

void CA_Default_GetCW(UTCA_U8* cwOdd, UTCA_U8* cwEven)
{
}


char *MyPrintHex(UTCA_U8 * pBuf,UTCA_U16 size)
{
	int i;
	char buf[32];
	static char s_message[2048];
	
	s_message[0]=0;
	for(i=0;i<size;i++)
	{
		sprintf(buf,"%02x,",pBuf[i]);
		strcat(s_message,buf);
	}
	return  s_message;
}


#ifdef  __cplusplus
}
#endif
