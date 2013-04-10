/* ---------------------------------------------------------------------------------------------------------
-                                              通用移植库
-                                              description
-
-                                   (c) Copyright 2012-2015, Wujie.Z
-                                           All Rights Reserved
-
- File : 
- By   : Wujie.Z
--------------------------------------------------------------------------------------------------------- */
#include <stdio.h>
#include <string.h>
#include "plugin_ca/pluginca.h"
#include "utcasa.h"
#include "utcas_interface.h"
#include "utcas_priv.h"

char *g_calibName = "";

//////////////////////////////////////////////////////////////////////////////// 
// 变量声明
//////////////////////////////////////////////////////////////////////////////// 
extern CA_Interface gCaInterface;

extern UTCA_Semaphore _ecmSemaphore;
extern UTCA_Semaphore _emmSemaphore;

extern ECMFliter_Info *_pEcmFliters[UTCA_MAXNUM_ECM];
extern UTCA_U32 _ecmCount;
extern PRIV_DATA _ecmData;

extern UTCA_U16 _emmPid;
extern PRIV_DATA _emmData;

extern UTCA_U8 _byThreadPrior;
extern UTCA_BOOL _taskRun;

//////////////////////////////////////////////////////////////////////////////// 
// 导 出 函 数
//////////////////////////////////////////////////////////////////////////////// 
UTCA_BOOL CDCASTB_Init( UTCA_U8 byThreadPrior )
{
	/*
	 * 测试卡类型
	 */
	unsigned char pOut[256];
	unsigned char nLen = (unsigned char)sizeof(pOut);
	unsigned int i;
	ATR_S atr;
	_byThreadPrior=byThreadPrior;
	CDSTBCA_Printf(1, "utcas CDCASTB_Init!");	
	memset(pOut,0,sizeof(pOut));
	CDSTBCA_SCReset(pOut,&nLen);
	//判断CA厂家
	
#ifdef JS_UNION_SHUMA_CALIB
	g_calibName="SHUMA";
#endif
#ifdef JS_UNION_GEHUA_CALIB
	g_calibName="GEHUA";
#endif

	//GetCAInterface(&gCaInterface,"DVN");
	GetCAInterface(&gCaInterface, g_calibName);
	if ( gCaInterface.CA_Init(0) == UTCA_FALSE){
		return UTCA_FALSE;
	}

	_ecmCount = 0;
	for( i = 0 ;  i < UTCA_MAXNUM_ECM ; i++  ){
		_pEcmFliters[i] = NULL;
	}
	CreateEcmTask(_byThreadPrior);
	CreateEmmTask(_byThreadPrior);
	return UTCA_TRUE;
}
void CDCASTB_Close( void )
{
	_taskRun = UTCA_FALSE;
	freeEcmFilters();
	KillEcmTask();
	KillEmmTask();
}
UTCA_BOOL  CDCASTB_IsUTCA(UTCA_U16 wCaSystemID)
{
	return gCaInterface.CA_IsUTCA(wCaSystemID);
}

void CDCASTB_SetEcmPid( UTCA_U8 byType, const SCDCASServiceInfo* pServiceInfo )
{
	switch(byType) {
		case UTCA_LIST_FIRST:
			{
				KillEcmTask();
				freeEcmFilters();
				break;
			}
		case UTCA_LIST_ADD:
			{
				ECMFliter_Info *p;

				p = (ECMFliter_Info *)CDSTBCA_Malloc(sizeof(ECMFliter_Info));
				CDSTBCA_Memset(p, 0x00, sizeof(ECMFliter_Info));

				p->pid = pServiceInfo->m_wEcmPid;
				p->masks[0] = 0xFE;
				p->fliters[0] = 0x80;
				p->reqID = _ecmCount | 0x80;
#ifdef JS_SHUMA_CALIB_1102
				// 黄冈和莆田
				//黄冈++			
				p->fliters[1]=0x11;
				p->fliters[2]=0x02;
				p->masks[1]=0xff;			
				p->masks[2]=0xff;			
				//黄冈--
#endif
#ifdef JS_SHUMA_CALIB_1103
				//宁德++	 // 要扔掉01,03		
				p->fliters[1]=0x11;
				p->fliters[2]=0x03;
				p->masks[1]=0xff;			
				p->masks[2]=0xff;			
				//宁德--

#endif
				_pEcmFliters[_ecmCount++] = p;
				break;
			}
		case UTCA_LIST_OK:
			{
				CreateEcmTask(_byThreadPrior);
				break;
			}
		default: break;
	}
}
void  CDCASTB_SetEmmPid(UTCA_U16 wEmmPid)
{
	UTCA_U8   masks[8];
	UTCA_U8   fliters[8];
	UTCA_U8   reqID = EMM_REQ_ID_MIN ;
	_emmPid = wEmmPid;

	if(_emmPid != 0)
	{
		CDSTBCA_SetPrivateDataFilter(reqID,  
				fliters,  
				masks, 
				8, 
				_emmPid, 
				30) ;
	}

}
void CDCASTB_PrivateDataGot( UTCA_U8        byReqID,
		UTCA_BOOL      bTimeout,
		UTCA_U16       wPid,
		const UTCA_U8* pbyReceiveData,
		UTCA_U16       wLen            )
{
	UTCA_U8 reqID = byReqID & 0x7F;
	if( (wLen > sizeof(_ecmData.buf)) || (wLen <= 0) ){
		return ;
	}
	//printf("CDCASTB_PrivateDataGot wLen = %d\n",wLen);
	if (/*(reqID >= ECM_REQ_ID_MIN) &&*/ (reqID <= ECM_REQ_ID_MAX)) {
		_ecmData.byReqID = byReqID;
		_ecmData.bTimeout = bTimeout;
		_ecmData.wPid = wPid;
		_ecmData.wLen = wLen;
		CDSTBCA_Memcpy(_ecmData.buf, pbyReceiveData, wLen);

		CDSTBCA_SemaphoreSignal(&_ecmSemaphore);
	} else if ((reqID >= EMM_REQ_ID_MIN) && (reqID <= EMM_REQ_ID_MAX)) {
		_emmData.byReqID = byReqID;
		_emmData.bTimeout = bTimeout;
		_emmData.wPid = wPid;
		_emmData.wLen = wLen;
		CDSTBCA_Memcpy(_emmData.buf, pbyReceiveData, wLen);

		CDSTBCA_SemaphoreSignal(&_emmSemaphore);
	}
}
/*  Description: 插入智能卡 */
UTCA_BOOL CDCASTB_SCInsert( void )
{
	UTCA_U8 atrBuf[32];
	UTCA_U8 atrLen = 0;
	ATR_S atr;
	CDSTBCA_Printf(1, "CDCASTB_SCInsert!");	
	if (CDSTBCA_SCReset(atrBuf, &atrLen) != UTCA_TRUE) {
		CDSTBCA_Printf(1, "get card ATR error!");
		return UTCA_FALSE;
	}
	CDSTBCA_Printf(1, "get card ATR OK!");
	gCaInterface.CA_Init(0);	// 参数无用，获取卡号
	/*	CDSTBCA_Memset(&atr, 0x00, sizeof(ATR_S));
		if (parseATR(atrBuf, atrLen, &atr) != UTCA_TRUE) {
		CDSTBCA_Printf(1, "parse ATR error!");
		return UTCA_FALSE;
		}
		printf("parse ATR OK!");

		gCaInterface.CA_SetAtr(&atr);
		*/	
	if(_taskRun == UTCA_FALSE)
		CreateEcmTask(_byThreadPrior);
	return UTCA_TRUE;
}

void CDCASTB_SCRemove( void )
{
	_taskRun=UTCA_FALSE;
	CDSTBCA_SemaphoreSignal(&_ecmSemaphore);
	CDSTBCA_Sleep(100);	// wait task exit
	CDSTBCA_Printf(1, "CDCASTB_SCRemove!");
}

UTCA_U16 CDCASTB_GetCardSN( char* pCardSN )
{
	return gCaInterface.CA_GetCardSN(pCardSN);
}

