#include <stdio.h>
#include <string.h>
#include "../pluginca.h"
#include "gh.h"


static UTCA_U16 s_emmPid;
static UTCA_U16 s_ecmPid;
static UTCA_U8 s_cw[16];
// static UTCA_U8 s_ecmtmp[512];
static UTCA_U16 s_cardSN  = 0;
/*------ CA_LIB���ȹ��� ------*/

/* CA_LIB��ʼ�� */
UTCA_BOOL GH_Init( UTCA_U8 byThreadPrior )
{
	UTCA_U8 commandCardSN[5] = {0xC1,0x12,0x01,0x0,0x19};
	UTCA_U8 inlen = 5;
	UTCA_U8 reply[256];
	UTCA_U16 replyLen = 128;
	UTCA_U8 ret = CDSTBCA_SCPBRun(commandCardSN,inlen,reply,&replyLen);
	
	char buf[256];
	sprintf(buf,"gehua ret:%d, replyLen:%d\n",ret, replyLen);
	CDSTBCA_Printf(4,buf);
	CDSTBCA_Printf(4,MyPrintHex(reply,replyLen));
//	if(0!= ret)
//	{
//		CDSTBCA_Printf(4,"GeHUA CardSN  error\n");
//		return 0;
//	}

	s_cardSN=*((UTCA_U16*)(reply+19));

	return UTCA_TRUE;
}

/* �ر�CA_LIB���ͷ���Դ */
void GH_Close( void )
{
}

/* UTCASͬ���ж� */
UTCA_BOOL  GH_IsUTCA(UTCA_U16 wCaSystemID)
{
	return UTCA_TRUE;
}

/*------ Flash���� ------ */

/* �洢�ռ�ĸ�ʽ�� */
void GH_FormatBuffer( void )
{
}

/* ���ζԴ洢�ռ�Ķ�д���� */
void GH_RequestMaskBuffer(void)
{
}
/* �򿪶Դ洢�ռ�Ķ�д���� */
void GH_RequestUpdateBuffer(void)
{
}

/*------ TS������ ------*/

/* ����ECM�ͽ�Ŀ��Ϣ */
void GH_SetEcmPid( UTCA_U8 byType,
		const SCDCASServiceInfo* pServiceInfo )
{
	s_ecmPid = pServiceInfo->m_wEcmPid;
}
/* ����EMM��Ϣ */
void GH_SetEmmPid(UTCA_U16 wEmmPid)
{
	s_emmPid = wEmmPid;
}
/* ˽�����ݽ��ջص� */
void GH_PrivateDataGot( UTCA_U8        byReqID,
		UTCA_BOOL      bTimeout,
		UTCA_U16       wPid,
		const UTCA_U8* pbyReceiveData,
		UTCA_U16       wLen            )
{
}
/*------- ���ܿ����� -------*/

/* �������ܿ� */
UTCA_BOOL GH_SCInsert( void )
{
	return UTCA_TRUE;
}
/* �γ����ܿ�*/
void GH_SCRemove( void )
{
}
/* ��ȡ���ܿ��ⲿ���� */
UTCA_U16 GH_GetCardSN( char* pCardSN )
{
	return 0;
}

/*------- ������Ϣ��ѯ -------*/

/* ��ѯCA_LIB�汾�� */
UTCA_U32 GH_GetVer( void )
{
	return 0;
}
/* ��ѯ������ƽ̨��� */
UTCA_U16 GH_GetPlatformID( void )
{
	return 0;
}
#if 0
/////////////////////////////////////////////////////////////////////////////

typedef struct {
	UTCA_U8* rev_buf;
	UTCA_U8 revlen;
	UTCA_U8 received_len;
}RevBuf;

RevBuf _RevBuf;

void sc_revbufinit(UTCA_U8* rec_buf, UTCA_U8 revLe)
{
	_RevBuf.rev_buf = rec_buf;
	_RevBuf.revlen = 0;
	_RevBuf.received_len = revLe;
}

UTCA_BOOL sc_isrevok__(UTCA_U8 rev)
{
	_RevBuf.rev_buf[_RevBuf.revlen] = rev;
	_RevBuf.revlen++;
	if( _RevBuf.received_len == _RevBuf.revlen ){
		return UTCA_TRUE;
	}
	return UTCA_FALSE;
}

int Ca_Send_7816_Para(UTCA_U8* send_buf, UTCA_U8 send_len, UTCA_U8* sendLc, UTCA_U8* revLe)
{
	if( (send_buf[4]+5) == send_len ){
		*sendLc = send_buf[4];
		*revLe = 2 ;
		return 0;
	}
	else{
		*sendLc = 0;
		*revLe = send_buf[4] + 2 ;
		return 0;
	}
	return -1;
}

UTCA_BOOL sc_isrevok(UTCA_U8* rev,UTCA_U8 rev_len)
{
	CDSTBCA_Memcpy(&_RevBuf.rev_buf[_RevBuf.revlen],rev,rev_len);
	_RevBuf.revlen += rev_len;
	if( _RevBuf.received_len == _RevBuf.revlen ){
		return UTCA_TRUE;
	}
	return UTCA_FALSE;
}
int gh_sc_apdu_t0(UTCA_U8* send_buf, UTCA_U8 send_len, UTCA_U8 *rec_buf, UTCA_U8 *rec_len)
{
	int rlt;
	UTCA_U8 _INS;
	UTCA_U8  *ptr;
	UTCA_U8 sendLc;
	UTCA_U8 revLe;
	UTCA_U8 tmp[256];	
	*rec_len = 0;


	rlt = Ca_Send_7816_Para(send_buf, send_len, &sendLc, &revLe);
	if(rlt < 0)
	{
		return 1;
	}

	_INS = * (send_buf + 1);
	ptr = send_buf;
	sc_revbufinit(rec_buf, revLe);

	CDSTBCA_SCSend(ptr, 5);	//����5�ֽڵ�����ͷ

	ptr = rec_buf;						  //���ܻ����׵�ַ

	if (sendLc > 0)  //д��, Lc�����Ҳ�Ϊ0
	{		
		while(1)
		{		// receive one byte _INS
			if(CDSTBCA_SCRev(ptr, 1)==-1)return -1;
			if (!( *ptr == 0x60) )  //0x60 �ȴ���
				break;
		}						
		ptr = send_buf + 5;					//���ݶ���ַ
		if ( ( rec_buf[0] == _INS )||( rec_buf[0] == (_INS^0x01) ) )
		{
			CDSTBCA_Sleep(1000);	//1ms		
			CDSTBCA_SCSend(ptr, sendLc);					//����Lc���ֽ�

			while(1)
			{		// receive one byte
				if(CDSTBCA_SCRev(ptr, 1)==-1)return -1;
				if (!( *ptr == 0x60) ){  //0x60 �ȴ���
					sc_isrevok(ptr,1);
					break;
				}
			}						
			while(1){
				if(CDSTBCA_SCRev(tmp, 1) == -1)return -1;
				if(sc_isrevok(tmp,1))break;
			}
		}
		else 
		{	// Sw1+ SW2, ��SW2
			ptr++;		//������һ���յ����ֽڣ���SW1
			_RevBuf.revlen++;			
			if(CDSTBCA_SCRev(ptr, 1)==-1)return -1;
			ptr++;
			_RevBuf.revlen++;
		}	
	}
	else
	{		//����,Lc ������(-1)
		while(1)
		{		// receive one byte _INS
			if(CDSTBCA_SCRev(ptr, 1)==-1)return -1;
			if (!( *ptr == 0x60) )  //0x60 �ȴ���
				break;
		}

		if ( ( rec_buf[0] == _INS )||( rec_buf[0] == (_INS^0x01) ) )
		{
			while(1){
				if(CDSTBCA_SCRev(tmp, revLe)==-1)return -1;
				if(sc_isrevok(tmp,revLe))	break;
			}			
		}		
		else 
		{		// Sw1+ SW2, ��SW2
			ptr++;		//������һ���յ����ֽڣ���SW1
			_RevBuf.revlen++;					
			if(CDSTBCA_SCRev(ptr, 1)==-1)return -1;
			ptr++;
			_RevBuf.revlen++;
		}			
	}

	*rec_len = _RevBuf.revlen;
	return 0;
}
/////////////////////////////////////////////////////////////////////////////
#endif

//UTCA_U8 GHCA_Cardflag = 1000;
UTCA_U8 GHCA_Cardflag = 1;
UTCA_U8 command1[5] = {0xC1,0x3C,0x01,0x5C,0x5C};
UTCA_U8 command2[5] = {0xC1,0x3A,0x00,0x00,0x10};
UTCA_U8 PreEcm[256];
UTCA_BOOL GH_ParseEcm(UTCA_U8* EcmBuf,UTCA_U16  wEcmLen)
{
	UTCA_U8 reply[128];
	UTCA_U16 replyLen = 0;
	UTCA_U8 cmdBuf[256];
	UTCA_U32 inlen = 97;
	
	char msgBuf[256];
	sprintf(msgBuf,"<GH_ParseEcm>wEcmLen:%d",wEcmLen);
	CDSTBCA_Printf(4,msgBuf);
	CDSTBCA_Printf(4,MyPrintHex(EcmBuf,wEcmLen));
	
#if 0
	UTCA_U32 i;
	EcmBuf++;
	wEcmLen--;
	/*if( !EcmBuf || wEcmLen != 100 )
	  return;*/
	//�ж�ecm�����Ƿ���100
	wEcmLen = 0;
	for( i = 0; i < 101 ; i++ )
	{
		if( (EcmBuf[i] == 0xff) && 
				(EcmBuf[i+1] == 0xff) &&
				(EcmBuf[i+2] == 0xff) &&
				(EcmBuf[i+3] == 0xff) ){
			wEcmLen = i;
			break;
		}
	}
	
	sprintf(msgBuf,"wEcmLen:%d",wEcmLen);
	CDSTBCA_Printf(4,msgBuf);
	CDSTBCA_Printf(4,MyPrintHex(EcmBuf,wEcmLen));
	
	if( wEcmLen != 100 ){
		return UTCA_FALSE;
	}
	wEcmLen = 100;

#endif	
//	if( EcmBuf[8] ==0x10 && EcmBuf[9] == 0x01 )
//		return UTCA_FALSE;
	if( EcmBuf[8] !=0x10 || EcmBuf[9] != 0x01 )
			return UTCA_FALSE;

	if( memcmp(&PreEcm[10],&EcmBuf[10],5) == 0 ) 
	{
		return UTCA_FALSE;
	}
	else{
		CDSTBCA_Memcpy(PreEcm,EcmBuf,100);
	}

	CDSTBCA_Memcpy(cmdBuf,command1,5);
	CDSTBCA_Memcpy(cmdBuf + 3,EcmBuf+ 7,1);
	CDSTBCA_Memcpy(cmdBuf + 5,EcmBuf + 8,wEcmLen - 8);
	inlen = 5 + wEcmLen - 8;

	replyLen = 128;
/*	
	if(  0 !=  gh_sc_apdu_t0(cmdBuf,inlen,reply,&replyLen) )
	{
		UTCA_U8 cmd[255];
		UTCA_U8 avrlen=255;
		if (GHCA_Cardflag==1)
		{
			CDSTBCA_SCReset(cmd, &avrlen);
		}

		return UTCA_TRUE;
	}
*/
	CDSTBCA_SCPBRun(cmdBuf,inlen,reply,&replyLen);

	sprintf(msgBuf,"ecm replyLen:%d\n", replyLen);
	CDSTBCA_Printf(4,msgBuf);
	CDSTBCA_Printf(4,MyPrintHex(reply,replyLen));

	if( replyLen < 2 ){
		return UTCA_FALSE;
	}

	if( reply[replyLen-2] == 0x90 && reply[replyLen-1] == 0x00)
	{

		/*errorCount=0;
		  error2Count=0;
		  scardCount=0;
		  CDSTBCA_ShowBuyMessage(0,CDCA_MESSAGE_CANCEL_TYPE);
		  */


	}
	else if (reply[replyLen-2] == 0x93 && reply[replyLen-1] != 0x00)
	{
		/*loopCount = 0;
		  errorCount++;
		  error2Count = 0;
		  scardCount = 0;
		  if (errorCount>=2)
		  {
		  CDSTBCA_ShowBuyMessage(0,CDCA_MESSAGE_NOENTITLE_TYPE);
		  errorCount=0;
		  }*/
		return UTCA_FALSE;
	}
	else if (reply[replyLen-2] == 0x94 && reply[replyLen-1] != 0x00)
	{
		/*loopCount = 0;
		  errorCount = 0;
		  error2Count++;
		  scardCount = 0;
		  if (error2Count>50)
		  {*/
		UTCA_U8 cmd[255];
		UTCA_U8 avrlen=255;

		CDSTBCA_SCReset(cmd, &avrlen);
		/*error2Count=0;
		  }*/
		return UTCA_FALSE;
	}

	inlen = 5;
	replyLen = 128;
//	if(0!=CDSTBCA_SCPBRun(command2,inlen,reply,&replyLen))
//	{
//		return UTCA_FALSE;
//	}
	CDSTBCA_SCPBRun(command2,inlen,reply,&replyLen);
	
	sprintf(msgBuf,"command2 replyLen:%d\n", replyLen);
	CDSTBCA_Printf(4,msgBuf);
	CDSTBCA_Printf(4,MyPrintHex(reply,replyLen));
	
	if( replyLen < 2 )
	{
		return UTCA_FALSE;
	}


	if( reply[replyLen-2] != 0x90 || reply[replyLen-1] != 0x00)
	{
		return UTCA_FALSE;
	}
	
	UTCA_U8 calc = 0;
	int i;
	for( i = 0; i< replyLen; i++)
	{
		if( (i+1)%4 == 0)
		{
			reply[i] = calc;
			calc =0;
			continue;
		}
		calc += reply[i];
	}
	
//	if( EcmBuf[0] == 0x80 ){	
		CDSTBCA_Memcpy(&s_cw[8],reply,8);
		CDSTBCA_Memcpy(&s_cw[0],reply+8,8);
//	}
//	else if( EcmBuf[0] == 0x81 ){
//		CDSTBCA_Memcpy(&s_cw[0],reply,8);
//		CDSTBCA_Memcpy(&s_cw[8],reply+8,8);
//	}


	CDSTBCA_Memset(reply,0,sizeof(reply));


	return UTCA_TRUE;
}

UTCA_BOOL GH_ParseEmm(UTCA_U8* EmmBuf,UTCA_U16  wEmmLen)
{
	UTCA_U8 reply[128];
	UTCA_U16 replyLen = 0;
	UTCA_U8 buf[256];
	UTCA_U32 inlen = 97;

	EmmBuf += *EmmBuf + 1;//���㵽emm������ 

	if (*((UTCA_U32 *)(EmmBuf)) != 0x01630084 )
	{
		return UTCA_FALSE;
	}


	if (*((UTCA_U16 *)(EmmBuf+6)) != s_cardSN ){
		return UTCA_FALSE;
	}

	CDSTBCA_Memcpy(buf,command1,5);
	CDSTBCA_Memcpy(buf + 5,EmmBuf + 10,wEmmLen - 10);
	inlen = 5 + wEmmLen - 10;

	replyLen = 128;

	/*
	if(  0 !=  CDSTBCA_SCPBRun(buf,inlen,reply,&replyLen))
	{

		UTCA_U8 cmd[255];
		UTCA_U8 avrlen=255;
		if (GHCA_Cardflag==1)
		{

			CDSTBCA_SCReset(cmd, &avrlen);
		}

	}
	*/
	CDSTBCA_SCPBRun(buf,inlen,reply,&replyLen);
	char buffer[256];
	sprintf(buffer,"emm replyLen:%d\n", replyLen);
	CDSTBCA_Printf(4,buffer);
	CDSTBCA_Printf(4,MyPrintHex(reply,replyLen));
	
	return UTCA_TRUE;
}

void GH_GetCW(UTCA_U8* cwOdd, UTCA_U8* cwEven)
{
	CDSTBCA_Memcpy(cwOdd,&s_cw[0],8);
	CDSTBCA_Memcpy(cwEven,&s_cw[8],8);
}

void GH_GetCAInterface(CA_Interface * Inter)
{
	/* CA_LIB��ʼ�� */
	Inter->CA_Init  = GH_Init;

	/* �ر�CA_LIB���ͷ���Դ */
	Inter->CA_Close = GH_Close;

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
	Inter->CA_SetEcmPid = GH_SetEcmPid;

	/* ����EMM��Ϣ */
	Inter->CA_SetEmmPid = GH_SetEmmPid;

	/* ˽�����ݽ��ջص� */
	Inter->CA_PrivateDataGot = GH_PrivateDataGot;

	/*------- ���ܿ����� -------*/
	/* �������ܿ� */
	Inter->CA_SCInsert = GH_SCInsert;

	/* �γ����ܿ�*/
	//Inter->CA_SCRemove = DVN_SCRemove;

	/* ��ȡ���ܿ��ⲿ���� */
	Inter->CA_GetCardSN = GH_GetCardSN;

	/*------- ������Ϣ��ѯ -------*/
	/* ��ѯCA_LIB�汾�� */
	Inter->CA_GetVer = GH_GetVer;

	/* ��ѯ������ƽ̨��� */
	Inter->CA_GetPlatformID = GH_GetPlatformID;

	Inter->CA_SetAtr = NULL;

	Inter->CA_ParseEcm = GH_ParseEcm;

	Inter->CA_ParseEmm = GH_ParseEmm;

	Inter->CA_GetCW = GH_GetCW;
}
