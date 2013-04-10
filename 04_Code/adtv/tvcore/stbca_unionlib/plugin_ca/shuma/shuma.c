#include <stdio.h>
#include <string.h>
#include "../pluginca.h"
#include "shuma.h"
static UTCA_U8 s_cw[16];
static UTCA_U8 _cardSN[4];
//int gh_sc_apdu_t0(UTCA_U8* send_buf, UTCA_U8 send_len, UTCA_U8 *rec_buf, UTCA_U8 *rec_len);
void SM_GetCAInterface(CA_Interface * Inter)
{
	/* CA_LIB初始化 */
	Inter->CA_Init  = SM_Init;
	/* 关闭CA_LIB，释放资源 */
	Inter->CA_Close = SM_Close;
	/* UTCAS同密判断 */
	Inter->CA_IsUTCA = CA_Default_IsUTCA;
	/*------ Flash管理 ------ */
	/* 存储空间的格式化 */
	Inter->CA_FormatBuffer = CA_Default_FormatBuffer;
	/* 屏蔽对存储空间的读写操作 */	
	Inter->CA_RequestMaskBuffer = CA_Default_RequestMaskBuffer;
	/* 打开对存储空间的读写操作 */
	Inter->CA_RequestUpdateBuffer = CA_Default_RequestUpdateBuffer;
	/*------ TS流管理 ------*/
	/* 设置ECM和节目信息 */
	Inter->CA_SetEcmPid = SM_SetEcmPid;
	/* 设置EMM信息 */
	Inter->CA_SetEmmPid = SM_SetEmmPid;
	/* 私有数据接收回调 */
	Inter->CA_PrivateDataGot = SM_PrivateDataGot;
	/*------- 智能卡管理 -------*/
	/* 插入智能卡 */
	Inter->CA_SCInsert = SM_SCInsert;
	/* 拔出智能卡*/
	//Inter->CA_SCRemove = DVN_SCRemove;

	/* 读取智能卡外部卡号 */
	Inter->CA_GetCardSN = SM_GetCardSN;
	/*------- 基本信息查询 -------*/
	/* 查询CA_LIB版本号 */
	Inter->CA_GetVer = SM_GetVer;
	/* 查询机顶盒平台编号 */
	Inter->CA_GetPlatformID = SM_GetPlatformID;
	Inter->CA_SetAtr = NULL;
	Inter->CA_ParseEcm = SM_ParseEcm;
	Inter->CA_ParseEmm = SM_ParseEmm;
	Inter->CA_GetCW = SM_GetCW;
}

/* CA_LIB初始化 */
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


/* 关闭CA_LIB，释放资源 */
void SM_Close( void )
{
}
/* UTCAS同密判断 */
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
	memcpy(ecmBuf+1,aEcmBuf,awEcmLen);  // 前面填上一个0
	UTCA_U16 wEcmLen=awEcmLen+1;
	
	UTCA_U8 secLengh = ecmBuf[3];
	UTCA_U8 command1[255] = {0x80,0x32,0x00,0x00};//ecm头命令
	command1[4] = secLengh+3;					  // 节的总长度
	UTCA_U8 getCWCmd[5] = {0x00,0xc0,0x00,0x00};


	if( memcmp(sPreEcm,ecmBuf,wEcmLen) == 0 ){
		CDSTBCA_Printf(4,"same ecm data");
		return UTCA_FALSE;
	}
	else{
		CDSTBCA_Memcpy(sPreEcm,ecmBuf,wEcmLen);
	}
	CDSTBCA_Memcpy(&command1[5],ecmBuf+1,command1[4]);

	// 打印输出
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
	CDSTBCA_SCPBRun(command1,command1[4]+5,reply,&replyLen);	//命令长度5 + 头部3 +节数据
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


/*------ Flash管理 ------ */
/* 存储空间的格式化 */
void SM_FormatBuffer( void )
{
}
/* 屏蔽对存储空间的读写操作 */
void SM_RequestMaskBuffer(void)
{
}
/* 打开对存储空间的读写操作 */
void SM_RequestUpdateBuffer(void)
{
}
/*------ TS流管理 ------*/
/* 设置ECM和节目信息 */
void SM_SetEcmPid( UTCA_U8 byType,
		const SCDCASServiceInfo* pServiceInfo )
{
}
/* 设置EMM信息 */
void SM_SetEmmPid(UTCA_U16 wEmmPid)
{
}
/* 私有数据接收回调 */
void SM_PrivateDataGot( UTCA_U8        byReqID,
		UTCA_BOOL      bTimeout,
		UTCA_U16       wPid,
		const UTCA_U8* pbyReceiveData,
		UTCA_U16       wLen            )
{
}
/*------- 智能卡管理 -------*/
/* 插入智能卡 */
UTCA_BOOL SM_SCInsert( void )
{
	return  UTCA_TRUE;
}
/* 拔出智能卡*/
void SM_SCRemove( void )
{
}
/* 读取智能卡外部卡号 */
UTCA_U16 SM_GetCardSN( char* pCardSN )
{
	unsigned int cardno;
	cardno = _cardSN[3]+(_cardSN[2]<<8)+(_cardSN[1]<<16)+(_cardSN[0]<<24); // 大头在前(big endian)
	sprintf(pCardSN,"%d",cardno);

//	sprintf(pCardSN,"%x",*((unsigned int*)_cardSN));
//	memcpy(pCardSN, _cardSN, 4);
	return 0;
}
/*------- 基本信息查询 -------*/
/* 查询CA_LIB版本号 */
UTCA_U32 SM_GetVer( void )
{
	return 0;
}
/* 查询机顶盒平台编号 */
UTCA_U16 SM_GetPlatformID( void )
{
	return 0;
}

