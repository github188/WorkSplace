#include <stdio.h>
#include <string.h>
#include "../pluginca.h"
#include "shuma.h"
static UTCA_U8 s_cw[16];
static UTCA_U8 _cardSN[4];
//int gh_sc_apdu_t0(UTCA_U8* send_buf, UTCA_U8 send_len, UTCA_U8 *rec_buf, UTCA_U8 *rec_len);
void SM_GetCAInterface(CA_Interface * Inter)
{
	/* CA_LIB��ʼ�� */
	Inter->CA_Init  = SM_Init;
	/* �ر�CA_LIB���ͷ���Դ */
	Inter->CA_Close = SM_Close;
	/* UTCASͬ���ж� */
	Inter->CA_IsUTCA = CA_Default_IsUTCA;
	/*------ Flash���� ------ */
	/* �洢�ռ�ĸ�ʽ�� */
	Inter->CA_FormatBuffer = CA_Default_FormatBuffer;
	/* ���ζԴ洢�ռ�Ķ�д���� */	
	Inter->CA_RequestMaskBuffer = CA_Default_RequestMaskBuffer;
	/* �򿪶Դ洢�ռ�Ķ�д���� */
	Inter->CA_RequestUpdateBuffer = CA_Default_RequestUpdateBuffer;
	/*------ TS������ ------*/
	/* ����ECM�ͽ�Ŀ��Ϣ */
	Inter->CA_SetEcmPid = SM_SetEcmPid;
	/* ����EMM��Ϣ */
	Inter->CA_SetEmmPid = SM_SetEmmPid;
	/* ˽�����ݽ��ջص� */
	Inter->CA_PrivateDataGot = SM_PrivateDataGot;
	/*------- ���ܿ����� -------*/
	/* �������ܿ� */
	Inter->CA_SCInsert = SM_SCInsert;
	/* �γ����ܿ�*/
	//Inter->CA_SCRemove = DVN_SCRemove;

	/* ��ȡ���ܿ��ⲿ���� */
	Inter->CA_GetCardSN = SM_GetCardSN;
	/*------- ������Ϣ��ѯ -------*/
	/* ��ѯCA_LIB�汾�� */
	Inter->CA_GetVer = SM_GetVer;
	/* ��ѯ������ƽ̨��� */
	Inter->CA_GetPlatformID = SM_GetPlatformID;
	Inter->CA_SetAtr = NULL;
	Inter->CA_ParseEcm = SM_ParseEcm;
	Inter->CA_ParseEmm = SM_ParseEmm;
	Inter->CA_GetCW = SM_GetCW;
}

/* CA_LIB��ʼ�� */
UTCA_BOOL SM_Init( UTCA_U8 byThreadPrior )
{
	GetCardNO();
	return  UTCA_TRUE;
}

UTCA_BOOL GetCardNO(void)
{
	static UTCA_U8 open_file1[] = {0x00, 0xA4, 0x04, 0x00, 0x02, 0x3F, 0x00};
	static UTCA_U8 open_file2[] = {0x00, 0xA4, 0x04, 0x00, 0x02, 0x4A, 0x00};
	static UTCA_U8 card_num[] = {0x00, 0xB2, 0x00, 0x05, 0x06, 0x00, 0x01, 0xFF, 0x00, 0x01, 0xFF};
	UTCA_U8 getCWCmd[5] = {0x00,0xc0,0x00,0x00};

	UTCA_U8 ReceiveData[256];
	UTCA_U16 ReceiveDataLen = (UTCA_U16)sizeof(ReceiveData);


	if( CDSTBCA_SCPBRun(open_file1,sizeof(open_file1),ReceiveData,&ReceiveDataLen) == UTCA_FALSE ){
		return UTCA_FALSE;
	}
	if( CDSTBCA_SCPBRun(open_file2,sizeof(open_file2),ReceiveData,&ReceiveDataLen) == UTCA_FALSE ){
		return UTCA_FALSE;
	}
	if( CDSTBCA_SCPBRun(card_num,sizeof(card_num),ReceiveData,&ReceiveDataLen) == UTCA_FALSE ){
		return UTCA_FALSE;
	}
	if( ReceiveDataLen < 2 ){
		return UTCA_FALSE;
	}

	/*	if (ReceiveData[0] != 0x61) {
		return UTCA_FALSE;
		}
		*/
	getCWCmd[4] = ReceiveData[1];
	if(  0 ==  CDSTBCA_SCPBRun(getCWCmd,5,ReceiveData,&ReceiveDataLen) )
	{
		return UTCA_FALSE;
	}
	memcpy(_cardSN, ReceiveData+3, 4);
	
	char buffer[64];
	sprintf(buffer,"cardno: 0x%x",*((unsigned int*)_cardSN));
	CDSTBCA_Printf(4,buffer);
	
	return UTCA_TRUE;
}


/* �ر�CA_LIB���ͷ���Դ */
void SM_Close( void )
{
}
/* UTCASͬ���ж� */
UTCA_BOOL  SM_IsUTCA(UTCA_U16 wCaSystemID)
{
	return UTCA_TRUE;
}

UTCA_BOOL SM_ParseEcm(UTCA_U8* aEcmBuf,UTCA_U16  awEcmLen)
{	
	static UTCA_U8 sPreEcm[256];
	UTCA_U8 tmp_cw[16];
	UTCA_U8 reply[256];
	UTCA_U16 replyLen = 0;
	UTCA_U8 len,i;
	UTCA_U8 ecmBuf[1024];
	ecmBuf[0]=0;
	memcpy(ecmBuf+1,aEcmBuf,awEcmLen);  // ǰ������һ��0
	UTCA_U16 wEcmLen=awEcmLen+1;
	
	UTCA_U8 secLengh = ecmBuf[3];
	UTCA_U8 command1[255] = {0x80,0x32,0x00,0x00};//ecmͷ����
	command1[4] = secLengh+3;					  // �ڵ��ܳ���
	UTCA_U8 getCWCmd[5] = {0x00,0xc0,0x00,0x00};


	if( memcmp(sPreEcm,ecmBuf,wEcmLen) == 0 ){
		CDSTBCA_Printf(4,"same ecm data");
		return UTCA_FALSE;
	}
	else{
		CDSTBCA_Memcpy(sPreEcm,ecmBuf,wEcmLen);
	}
	CDSTBCA_Memcpy(&command1[5],ecmBuf+1,command1[4]);

	// ��ӡ���
	char msgBuf[256];
	sprintf(msgBuf,"<SM_ParseEcm> secLengh:%d",secLengh);
	CDSTBCA_Printf(4,msgBuf);
	CDSTBCA_Printf(4,MyPrintHex(ecmBuf,wEcmLen));

	/*	if(  0 !=  gh_sc_apdu_t0(command1,command1[4]+5,reply,&replyLen) )
		{
		return UTCA_TRUE;
		}
		if( replyLen < 2 ){
		return UTCA_FALSE;
		}
		if(reply[0]!=0x61) return UTCA_FALSE;
		len = reply[1];
		getCWCmd[4] = reply[1];
		if(  0 !=  CDSTBCA_SCPBRun(getCWCmd,5,reply,&replyLen) )
		{
		return UTCA_TRUE;
		}

*/	
	replyLen=0;
	CDSTBCA_SCPBRun(command1,command1[4]+5,reply,&replyLen);	//�����5 + ͷ��3 +������
	if(reply[0]!=0x61) return UTCA_FALSE;
	sprintf(msgBuf,"replyLen:%d, reply[0]:0x%x, reply[1]:0x%x",replyLen, reply[0],reply[1]);
	CDSTBCA_Printf(4,msgBuf);

	getCWCmd[4] = reply[1];
	replyLen=0;
	CDSTBCA_SCPBRun(getCWCmd,5,reply,&replyLen);
	sprintf(msgBuf,"replyLen: %d",replyLen);
	CDSTBCA_Printf(4,msgBuf);
	CDSTBCA_Printf(4,MyPrintHex(reply,replyLen));
	len = reply[1];				// hjj
	
	i = 0;
	while( i < len ){
		if(reply[i] == 0x84 ){
			i++;
			i += reply[i];
		}
		else if(reply[i] == 0xa4 ){
			i++;
			i += reply[i];
		}
		else if(reply[i] == 0xb4 ){
			i++;
			i += reply[i];
		}
		else if(reply[i] == 0x83 ){
			i+=6;
			CDSTBCA_Memcpy(tmp_cw,&reply[i],4);
			CDSTBCA_Memcpy(tmp_cw+4,(&reply[i])+5,8);
			CDSTBCA_Memcpy(tmp_cw+12,(&reply[i])+5+9,4);
			break;
		}
		i++;
	}
	
	if( ecmBuf[1] ==  0x80 ){	
		CDSTBCA_Memcpy(&s_cw[8],tmp_cw,8);
		CDSTBCA_Memcpy(&s_cw[0],tmp_cw+8,8);
	}
	else if( ecmBuf[1] == 0x81 ){
		CDSTBCA_Memcpy(&s_cw[0],tmp_cw,8);
		CDSTBCA_Memcpy(&s_cw[8],tmp_cw+8,8);
	}
	return UTCA_TRUE;
}
UTCA_BOOL SM_ParseEmm(UTCA_U8* EmmBuf,UTCA_U16  wEmmLen)
{//80,b2,00,00

	UTCA_U8 sPreEmm[256];
	UTCA_U8 reply[256];
	UTCA_U16 ret = 0;
	UTCA_U8 getCWCmd[5] = {0x00,0xc0,0x00,0x00};
	UTCA_U8 allCard[4] = {0xff, 0xff, 0xff, 0xff};
	
	UTCA_U8 cardData[255] = {0x80,0x30,0x00,0x00};
	UTCA_U8 cardDataLen = EmmBuf[3];
	cardData[4] = cardDataLen+3;

	if( memcmp(sPreEmm,EmmBuf,wEmmLen) == 0 ){
		CDSTBCA_Printf(4,"same emm data");
		return UTCA_FALSE;
	}
	else{
		CDSTBCA_Memcpy(sPreEmm,EmmBuf,wEmmLen);
	}

	//	CDSTBCA_Printf(4,"SM_ParseEmm enter 1\n");
	if (!memcmp(EmmBuf+5, allCard, 4) || (!memcmp(EmmBuf+5, _cardSN, 4) /*&& (EmmBuf[0x0b] & 0x02 == 0x02)*/ )) 
	{
		CDSTBCA_Printf(4,"<SM_ParseEmm> enter 2\n");

		memcpy(&cardData[5],EmmBuf+1,cardData[4]);
		//return UTCA_TRUE;

		if(0 ==  CDSTBCA_SCPBRun(cardData,cardData[4]+5,reply,&ret)) {
			return UTCA_FALSE;
		}

		if( ret == 2 && reply[0] == 0x61) {

			getCWCmd[4] = reply[1];
			if(0 ==  CDSTBCA_SCPBRun(getCWCmd,5,reply,&ret)) {
				return UTCA_FALSE;
			}
		}
	}
	return UTCA_TRUE;
}
void SM_GetCW(UTCA_U8* cwOdd, UTCA_U8* cwEven)
{
	CDSTBCA_Memcpy(cwOdd,&s_cw[0],8);
	CDSTBCA_Memcpy(cwEven,&s_cw[8],8);
}


/*------ Flash���� ------ */
/* �洢�ռ�ĸ�ʽ�� */
void SM_FormatBuffer( void )
{
}
/* ���ζԴ洢�ռ�Ķ�д���� */
void SM_RequestMaskBuffer(void)
{
}
/* �򿪶Դ洢�ռ�Ķ�д���� */
void SM_RequestUpdateBuffer(void)
{
}
/*------ TS������ ------*/
/* ����ECM�ͽ�Ŀ��Ϣ */
void SM_SetEcmPid( UTCA_U8 byType,
		const SCDCASServiceInfo* pServiceInfo )
{
}
/* ����EMM��Ϣ */
void SM_SetEmmPid(UTCA_U16 wEmmPid)
{
}
/* ˽�����ݽ��ջص� */
void SM_PrivateDataGot( UTCA_U8        byReqID,
		UTCA_BOOL      bTimeout,
		UTCA_U16       wPid,
		const UTCA_U8* pbyReceiveData,
		UTCA_U16       wLen            )
{
}
/*------- ���ܿ����� -------*/
/* �������ܿ� */
UTCA_BOOL SM_SCInsert( void )
{
	return  UTCA_TRUE;
}
/* �γ����ܿ�*/
void SM_SCRemove( void )
{
}
/* ��ȡ���ܿ��ⲿ���� */
UTCA_U16 SM_GetCardSN( char* pCardSN )
{
	unsigned int cardno;
	cardno = _cardSN[3]+(_cardSN[2]<<8)+(_cardSN[1]<<16)+(_cardSN[0]<<24); // ��ͷ��ǰ(big endian)
	sprintf(pCardSN,"%d",cardno);

//	sprintf(pCardSN,"%x",*((unsigned int*)_cardSN));
//	memcpy(pCardSN, _cardSN, 4);
	return 0;
}
/*------- ������Ϣ��ѯ -------*/
/* ��ѯCA_LIB�汾�� */
UTCA_U32 SM_GetVer( void )
{
	return 0;
}
/* ��ѯ������ƽ̨��� */
UTCA_U16 SM_GetPlatformID( void )
{
	return 0;
}

