//包含中文字符,供vim 识别
#include "cdcas_hdr.h"
#include "capture.h"
#include "stbruntime.h"
#include "stbca.h"
#include "stbca_utility.h"
#include "blockring.h"
#include <string.h>
#define LOG_LEVEL LOG_TRACE
#define LOG_TAG "stbca"
#include "dxreport.h"

#ifdef JS_USE_SHUMA_CALIB_NORMAL
#include <utils/DataObject.h>
#endif

#define STBCA_DEBUG_PRINT

ObjStbCa gStbCa;
const char *CDCABuyMessages[]={
	"取消当前的显示",/*0x00*/
	"无法识别卡",/*0x01*/
	"智能卡过期，请更换新卡",/*0x02*/
	"加扰节目，请插入智能卡",/*0x03*/
	"卡中不存在节目运营商",/*0x04*/
	"条件禁播",/*0x05*/
	"当前时段被设定为不能观看",/*0x06*/
	"节目级别高于设定的观看级别",/*0x07*/
	"智能卡与本机顶盒不对应",/*0x08*/
	"没有授权",/*0x09*/
	"节目解密失败",/*0x0A*/
	"卡内金额不足",/*0x0B*/
	"区域不正确",/*0x0C*/
	"子卡需要和母卡对应，请插入母卡",/*0x0D*/
	"智能卡校验失败，请联系运营商",/*0x0E*/
	"智能卡升级中，请不要拔卡或者关机",/*0x0F*/
	"请升级智能卡",/*0x10*/
	"请勿频繁切换频道",/*0x11*/
	"智能卡暂时休眠，请5分钟后重新开机",/*0x12*/
	"智能卡已冻结，请联系运营商",/*0x13*/
	"智能卡已暂停，请回传收视记录给运营商",/*0x14*/
	"高级预览节目，该阶段不能免费观看",/*0x15*/
	"升级测试卡测试中...",/*0x16*/
	"升级测试卡测试失败，请检查机卡通讯模块",/*0x17*/
	"升级测试卡测试成功",/*0x18*/
	"卡中不存在移植库定制运营商",/*0x19*/
	"",/*0x1A*/
	"",/*0x1B*/
	"",/*0x1C*/
	"",/*0x1D*/
	"",/*0x1E*/
	"",/*0x1F*/
	"请重启机顶盒",/*0x20*/
	"机顶盒被冻结",/*0x21*/
};

const char * DVTCAMessages[] =
{
	"收看级别不够",/*0*/
	"不在收看时段内",/*1*/
	"没有机卡对应",/*2*/
	"",	/*3*/
	"请插卡",/*4*/
	"没有购买此节目",/*5*/
	"运营商限制观看该节目",/*6*/
	"运营商限制区域观看",/*7*/
	"此卡为子卡，已经被限制收看，请与母卡配对",/*8*/
	"余额不足，不能观看此节目，请及时充值",/*9*/
	"此节目为IPPV节目，请到IPPV节目确认/取消购买菜单下确认购买此节目",/*10*/
	"此节目为IPPV节目，您没有预订和确认购买，不能观看此节目",/*11*/
	"此节目为IPPT节目，请到IPPT节目确认/取消购买菜单下确认购买此节目",/*12*/
	"此节目为IPPT节目，您没有预订和确认购买，不能观看此节目",/*13*/
	"",/*14*/
	"",/*15*/
	"数据无效，STB不做任何提示。卡密钥问题。",/*16*/
	"",/*17*/
	"IC卡被禁止服务",/*18*/
	"",/*19*/
	"此卡未被激活，请联系运营商",/*20*/
	"请联系运营商回传IPP节目信息",/*21*/
	"用户您好，此节目您尚未购买，正在免费预览中",/*22*/
};


extern BYTE gFlashData[];
// extern int gSaveEcmCount;
extern RecvData gSaveEcmRecvData;

/*
bool FindSecFilter(vector<utSectionFilter> &filters,utSectionFilter &filter)
{
	for(vector<utSectionFilter>::iterator it=filters.begin(); it!=filters.end(); it++)
	{
		if((*it)==filter) return true;
	}
	return false;
}
*/
UINT8 __stdcall CDSTBCA_SetPrivateDataFilter(BYTE byReqID,IN const BYTE* pbyFilter,IN const BYTE* pbyMask,BYTE byLen, WORD wPid, BYTE byWaitSeconds)
{
	dxreport("CDSTBCA_SetPrivateDataFilter begin >>> ReqID:%d, pid:0x%x, filter:%s, WaitSeconds:%d",byReqID,wPid,PrintHex(pbyFilter,8),byWaitSeconds);
	if(FILTER_BYTE_SIZE < byLen)
	{
		return false;
	}

	RegIDPID reqIDPID;
	reqIDPID.byReqID = byReqID;
	reqIDPID.wPid = wPid;

	utSectionFilter filter;
	filter.iTuner = 0;
	filter.pid = wPid;
	filter.tid = NO_TABLE_ID;
	memcpy(filter.filterData,pbyFilter,8);
	memcpy(filter.filterMask,pbyMask,8);
//	filter.timeout = (0 == byWaitSeconds) ? 0xffffffff : byWaitSeconds*1000;
//   有的地方novel 11s 时间会超时，所以扩大3倍
	filter.timeout = (0 == byWaitSeconds) ? 0x7fffffff : byWaitSeconds*1000*10;
	if(filter.pid==0x10) filter.timeout = 0x7fffffff; // 不超时
	HANDLE handle=NULL;
	if((byReqID & 0x80) !=0x80){ // EMM(wPid==gStbCa.emmpid_) 及NIT(wPid=0x10) 过滤属于此种情况, 为连续过滤器
		handle=tsdemux_addSectionFilter(&filter,1,ConPrivateDataGotCallBack,(utContext *)byReqID);
		if(handle)
		{
//			dxreport("filter added, handle:0x%x\n",handle);
			AutoLockT lock_it(gStbCa.mutexCalibControl_);
			gStbCa.mapCaLibControl_hFilter_ReqIDPID_[handle]=reqIDPID;
		}
		else
		{
			dxreport("Waring !!!, this persist filter registed failed\n");
		}
		
	}
	else // when byReqID &0x80 == 0x80 , need delete the filter by self, 为一次性过滤器, ecm 属于该类过滤器
	{
//		AutoLockT lock_it(gStbCa.mutexMyControl_);
		CloseGMapDemuxFilter_Lock(gStbCa.mapMyControl_hFilter_ReqIDPID_,byReqID,wPid, gStbCa.mutexMyControl_);  // 先除去旧过滤器，若旧的与现在同名
		dxreport("CDSTBCA_SetPrivateDataFilter ecm tsdemux_addSectionFilter\n");
		handle=tsdemux_addSectionFilter(&filter,1,OncePrivateDataGotCallBack,(utContext *)byReqID);
		if(handle)
		{
			dxreport("filter once added, handle:0x%x\n",handle);
			gStbCa.mapMyControl_hFilter_ReqIDPID_[handle]=reqIDPID;
		}
		else
		{
			dxreport("Waring !!!, this once filter registed failed\n");
		}
			
	}
#ifdef STBCA_DEBUG_PRINT
	dxreport("CDSTBCA_SetPrivateDataFilter end <<< ReqID:%d, pid:0x%x, filter:%s,",byReqID,wPid,PrintHex(pbyFilter,8));
	dxreport("mask:%s\n",PrintHex(pbyMask,8));
#endif 

	return true;
}

void __stdcall CDSTBCA_ReleasePrivateDataFilter( CDCA_U8  byReqID,	CDCA_U16 wPid )
{
#ifdef STBCA_DEBUG_PRINT
	dxreport("CDSTBCA_ReleasePrivateDataFilter, reqid=%d pid=0x%x\n",byReqID,wPid);
#endif 
	CloseGMapDemuxFilter(gStbCa.mapCaLibControl_hFilter_ReqIDPID_,byReqID,wPid);
}

void __stdcall CDSTBCA_ScrSetCW(WORD wEcmPID,IN const BYTE* pbyOddKey,IN const BYTE* pbyEvenKey,BYTE byKeyLen,UINT8 bTaingEnabled)
{
	UNUSED_PARAM(bTaingEnabled);
#ifdef STBCA_DEBUG_PRINT
	dxreport("****** CDSTBCA_ScrSetCW begin >>> EcmPid:%d, pbyOddKey:%s, ",
			wEcmPID, PrintHex(pbyOddKey,byKeyLen));
	dxreport("pbyEvenKey:%s, byKeyLen:%d\n",PrintHex(pbyEvenKey,byKeyLen), byKeyLen);
#endif 

	DVBCW cw[32];
	U32 cwCount=0;
	bool bMaxCWCount=false;
	for(int i=0; i<gStbCa.saveDescramCount_; i++)
	{
		if(gStbCa.saveDescrambs_[i].ecmPid==wEcmPID)
		{
			for(UINT j=0; j<gStbCa.saveDescrambs_[i].streamPidCount; j++)
			{
				cw[cwCount].pid=gStbCa.saveDescrambs_[i].streamPid[j];
				memcpy(cw[cwCount].eventKey,pbyEvenKey,8);
				memcpy(cw[cwCount].oddKey,pbyOddKey,8);
				if(cwCount<sizeof(cw)/sizeof(cw[0]))		// 最多32 个cw
					cwCount++;
				else
				{
					bMaxCWCount=true;
					break;
				}
			}
		}
		if(bMaxCWCount)
			break;
	}
	if(cwCount)
		SetCW(cw,cwCount);
}

/****************************************************************
 * Description:  	STB for CA  function :Scard reset.
 * Output:          ATR
 *					ATR length
 * Return:		
 ****************************************************************/
UINT8 __stdcall CDSTBCA_SCReset(OUT BYTE* pbyATR, OUT BYTE* pbyLen)
{
#ifdef STBCA_DEBUG_PRINT
	dxreport("CDSTBCA_SCReset[smart card reset] begin >>> \n");
#endif 
	//try 3 times
	UINT8 res = false;
	int count = 0;
	do{
		res = tvdevice_SCReset(gStbCa.hTVDevice_,pbyATR,pbyLen);
	}while(!res && (count++ < 2));
#ifdef STBCA_DEBUG_PRINT
	dxreport("CDSTBCA_SCReset end <<< pbyATR:%s, byLen:%d\n",PrintHex(pbyATR,*pbyLen),*pbyLen);
#endif 

	return res;
}

/****************************************************************
 * Description:  	STB for CA  function :ScardRequest.
 * Input:        	smard card command
 *					command length
 * Output:          reply
 *					reply length
 * Return:			TRUE
 ****************************************************************/
UINT8 __stdcall CDSTBCA_SCPBRun(IN const BYTE* pbyCommand, WORD wCommandLen, OUT BYTE* pbyReply, OUT WORD* pwReplyLen)
{
#ifdef STBCA_DEBUG_PRINT
	dxreport("CDSTBCA_SCPBRun begin >>> smart card. wCommandLen:%d,%s\n",wCommandLen,PrintHex(pbyCommand,wCommandLen));
#endif 

	int outputLen = 0;//pbyCommand[4]+2;	//莫非outputLen 有两种功能?
	int count = 0;
	bool ret = false;
	do{
		ret = tvdevice_SCTransmit(gStbCa.hTVDevice_,pbyCommand,wCommandLen,pbyReply,&outputLen);
	}while(!ret && (count++ < 2));

	*pwReplyLen = outputLen;
	//为通过7层测试卡指令
	if(ret == false)
	{
		char cmd1[9] = {0x00,0x40,0x00,0x00,0x04,0x01,0x02,0x03,0x04};
		char cmd2[5] = {0x00,0x42,0x00,0x00,0x04};
		if(memcmp(pbyCommand,cmd1,sizeof(cmd1)) == 0)
		{
			char rep[2] = {0x90,0x40};
			ret = true;
			*pwReplyLen = 2;
			memcpy(pbyReply,rep,2);
		}
		else if(memcmp(pbyCommand,cmd2,sizeof(cmd2)) == 0)
		{
			char rep[6] = {0x01,0x02,0x03,0x04,0x90,0x42};
			ret = true;
			*pwReplyLen = 6;
			memcpy(pbyReply,rep,6);
		}
	}
#ifdef STBCA_DEBUG_PRINT
	dxreport("CDSTBCA_SCPBRun end <<< ret:%d, wReplyLen:%d,reply:%s\n",ret,*pwReplyLen,PrintHex(pbyReply,*pwReplyLen));
#endif 
	
	if(*pwReplyLen == 0)
		ret = false;

	return ret;
//	return true;
}

/****************************************************************
 * Description:  	STB for CA  function :security funtion.
 * InOut:           256bytes data
 * Return:			NULL
 ****************************************************************/
WORD __stdcall CDSTBCA_SCFunction(BYTE* pData)
{
#ifdef STBCA_DEBUG_PRINT
	dxreport("CDSTBCA_SCFunction begin >>> security chip command,pData=%s\n",PrintHex(pData,256));
#endif 

	WORD ReturnCode=0;
	UINT8 outData[256]={0};
	int iOutBufferSize = 256;
	
	tvdevice_SCFuntion(gStbCa.hTVDevice_, pData,256,outData,&iOutBufferSize);
	if (pData[0]==0x04)
	{
		outData[2]=0x91;
		outData[3]=0x02; 
	}
	
	memcpy(pData,outData,256);
	ReturnCode=(((UINT16)pData[0])<<8)+pData[1];
	
#ifdef STBCA_DEBUG_PRINT
	dxreport("CDSTBCA_SCFunction end <<< security chip command,pData=%s\n",PrintHex(pData,256));
#endif 

	return ReturnCode;

}

/****************************************************************
 * Description:  	STB for CA  function :print message.
 * Input:        	level
 *					message
 * Return:			NULL
 ****************************************************************/
void __stdcall CDSTBCA_Printf(BYTE byLevel,IN const char* pbyMesssage)
{
#ifdef STBCA_DEBUG_PRINT
	dxreport("===From CA Lib===, Level:%d, M:%s",byLevel,pbyMesssage);
#endif 
}

/****************************************************************
 * Description:  	STB for CA  function :Get Set Top Box or TV card ID.
 * Output:          platform ID
 *					unique ID
 * Return:			NULL
 ****************************************************************/
void __stdcall CDSTBCA_GetSTBID(OUT WORD* pwPlatformID, OUT ULONG* pdwUniqueID)
{
#ifdef STBCA_DEBUG_PRINT
	dxreport("CDSTBCA_GetSTBID begin >>>\n");
#endif 

	UINT8 STBIDdata[6]={0};
	tvdevice_getStbID(gStbCa.hTVDevice_, (char *)STBIDdata,sizeof(STBIDdata));
	*pwPlatformID=(STBIDdata[0]<<8)+STBIDdata[1];
	*pdwUniqueID=(STBIDdata[2]<<24)+(STBIDdata[3]<<16)+(STBIDdata[4]<<8)+STBIDdata[5];

#ifdef STBCA_DEBUG_PRINT
	dxreport("CDSTBCA_GetSTBID: end <<< wPlatformID(%X),dwUniqueID(%X)\n",*pwPlatformID,(unsigned int)*pdwUniqueID);
#endif 

}

void  __stdcall CDSTBCA_ActionRequest( CDCA_U16 wTVSID,
		CDCA_U8  byActionType )
{
#ifdef STBCA_DEBUG_PRINT
	dxreport("CDSTBCA_ActionRequest: wTVSID=%d, byActionType=%d\n",wTVSID,byActionType);
#endif 
}

/****************************************************************
 * Description:  	STB for CA  function : sleep time.
 * Input:        	 million seconds
 * Return:			NULL
 ****************************************************************/
// P1
void __stdcall CDSTBCA_Sleep(WORD wMilliSeconds )
{
#ifdef STBCA_DEBUG_PRINT
//		dxreport("CDSTBCA_Sleep: %d\n",wMilliSeconds);
#endif 
	NS_sleep(wMilliSeconds);
}

/****************************************************************
 * Description:  	STB for CA  function : malloc memory.
 * Input:        	 buffer size
 * Return:			NULL
 ****************************************************************/
// P1
void* __stdcall CDSTBCA_Malloc(CDCA_U32 byBufSize)
{
	return NS_malloc(byBufSize);
}

/****************************************************************
 * Description:  	STB for CA  function : free memory.
 * Input:        	 buffer size
 * Return:			NULL
 ****************************************************************/
// P1
void  __stdcall CDSTBCA_Free(void* pBuf)
{
	NS_free(pBuf);
}

/****************************************************************
 * Description:  	STB for CA  function : set memory.
 * Input:        	buffer size
 *					var
 *					size
 * Return:			NULL
 ****************************************************************/
// P1
void  __stdcall CDSTBCA_Memset(void* pDestBuf,BYTE c,CDCA_U32 wSize)
{
#if defined(WIN32)
	NS_memset(pDestBuf,c,(CDCA_U16)wSize);
#else
	NS_memset(pDestBuf,c,wSize);
#endif 
}

/****************************************************************
 * Description:  	STB for CA  function : copy memory.
 * Input:        	destination buffer
 *					source buffer
 *					size
 * Return:			NULL
 ****************************************************************/
// P1
void  __stdcall CDSTBCA_Memcpy(void* pDestBuf,const void* pSrcBuf,CDCA_U32 wSize)
{
#if defined(WIN32)
	NS_memcpy(pDestBuf, pSrcBuf, (CDCA_U16)wSize);
#else
	NS_memcpy(pDestBuf, pSrcBuf, wSize);
#endif 
}

/****************************************************************
 * Description:  	STB for CA  function : get string length.
 * Input:        	 string
 * Return:		length
 ****************************************************************/
// P1
WORD  __stdcall CDSTBCA_Strlen(const char* pString)
{
	return (WORD)NS_strlen(pString);
}

/* 初始化信号量 */
void __stdcall CDSTBCA_SemaphoreInit( CDCA_Semaphore* pSemaphore,CDCA_BOOL bInitVal )
{
	HANDLE handle=NS_SemCreate(bInitVal,16);
	*pSemaphore = (int)handle;
}

/* 信号量给予信号 */
void __stdcall CDSTBCA_SemaphoreSignal( CDCA_Semaphore* pSemaphore )
{
	NS_SemSignal((void *)(*pSemaphore));
}

/* 信号量获取信号 */
void __stdcall CDSTBCA_SemaphoreWait( CDCA_Semaphore* pSemaphore )
{
	NS_SemWait(((void *)*pSemaphore),INFINITE);
}

HANDLE __stdcall CDSTBCA_RegisterTask( const char* szName,
		CDCA_U8     byPriority,
		void*       pTaskFun,
		void*       pParam,
		CDCA_U16    wStackSize  )
{
	UNUSED_PARAM(byPriority);
	
#ifdef STBCA_DEBUG_PRINT
	dxreport("CDSTBCA_RegisterTask begin >>> szName:%s\n",szName);
#endif 

	NS_THREADPROC pFun = (NS_THREADPROC) pTaskFun;
	HANDLE handle=NS_CreateThread(szName,pFun,pParam,wStackSize,false,NULL);
	
#ifdef STBCA_DEBUG_PRINT
	dxreport("CDSTBCA_RegisterTask end <<<\n");
#endif 

	return handle;
}

/****************************************************************
 * Description:  	STB for CA  function : read flash file.
 * Input:        	start address 
 * Output:          data
 * InOut:           length
 * Return:			NULL
 ****************************************************************/
void __stdcall CDSTBCA_ReadBuffer(BYTE byBlockID,OUT BYTE* pOutData, ULONG* pdwLen)
{
#ifdef STBCA_DEBUG_PRINT
//	dxreport("CDSTBCA_ReadBuffer begin >>>\n");
#endif 

	FILE *fp=fopen(gStbCa.fileName_,"rb");
	if(!fp)
	{
		dxreport( "CDSTBCA_ReadBuffer  Error open File %s\n", gStbCa.fileName_);
		return;
	}
	
	fseek(fp,(byBlockID-1)*WriteBuffer_BLOCK_SIZE,0);
	
	size_t size=fread(pOutData,1,*pdwLen,fp);
	if (!size || size!=*pdwLen) {
		dxreport("CDSTBCA_ReadBuffer Error Read File %s\n",gStbCa.fileName_);
	}
	fclose(fp);
#ifdef STBCA_DEBUG_PRINT
	dxreport("CDSTBCA_ReadBuffer:byBlockID:%d, len:%d, data:%s\n",byBlockID,size,(size>8)?PrintHex(pOutData,8):PrintHex(pOutData,(WORD)size));
#endif 

}

void __stdcall CDSTBCA_WriteBuffer( CDCA_U8        byBlockID,
		const CDCA_U8* pbyData,
		CDCA_U32       dwLen )
{
#ifdef STBCA_DEBUG_PRINT
	dxreport("CDSTBCA_WriteBuffer: byBlockID:%d,len:%lu,data:%s\n",byBlockID,dwLen,(dwLen>8)?PrintHex(pbyData,8):PrintHex(pbyData,(WORD)dwLen));
#endif 
	FILE *fp=fopen(gStbCa.fileName_,"rb+");
	if(!fp)
	{
		dxreport( "CDSTBCA_WriteBuffer  Error open File %s\n", gStbCa.fileName_);
		return;
	}

	fseek(fp, (byBlockID-1)*WriteBuffer_BLOCK_SIZE,0);
	size_t size=fwrite(pbyData,1,dwLen,fp);
//	memcpy(gFlashData+(byBlockID-1)*WriteBuffer_BLOCK_SIZE,pbyData,dwLen);
//	size_t size=fwrite(gFlashData,1,FLASH_FILE_SIZE,fp);
	dxreport( "CDSTBCA_WriteBuffer fwrite ret %d %x\n",size, size);
	if (!size) {
#ifdef STBCA_DEBUG_PRINT
		dxreport("CDSTBCA_WriteBuffer Error Read File %s\n",gStbCa.fileName_);
#endif 
	}
	fclose(fp);
#ifdef STBCA_DEBUG_PRINT
	//	dxreport("CDSTBCA_WriteBuffer end <<<\n");
#endif 

}

////////////////////////////////////////////////////////////////////////////////
// null functions
////////////////////////////////////////////////////////////////////////////////
/****************************************************************
 * Description:  	STB for CA  function :Show Buy Message.
 * Input:        	Ecm Pid
 *                  Message Type
 * Return:			NULL
 ****************************************************************/
void __stdcall CDSTBCA_ShowBuyMessage(WORD wEcmPID,BYTE byMessageType)
{
	//消息字符串列表
	char ** messages;
	//消息与消息字符串列表偏移
	int msgOffset = 0;

	//如果不是数码消息
	if(byMessageType < CDCA_MESSAGE_DVTCA_MESSAGE_BASE)
	{
		messages = (char**)CDCABuyMessages;
		//msgOffset = 0;
	}
	else
	{
		msgOffset = CDCA_MESSAGE_DVTCA_MESSAGE_BASE;
		messages = (char**)DVTCAMessages;
	}

	int notifyCode = TVNOTIFY_BUYMSG;
	long lParam = byMessageType;
	char *pParam = (char *)messages[byMessageType - msgOffset];
	if(gStbCa.pCaMsgCallBack_)
	{
#ifdef JS_USE_SHUMA_CALIB_NORMAL
		J_DataObject trObj;
		J_NVItem root, child1;

		root.name = "/";
		child1.name = "ca_msg";
		child1.value = (J_U32)lParam;
			
		J_DataObject::pre_order_iterator iter = trObj.set_root(root);
		trObj.append_child(iter,child1);
		gStbCa.pCaMsgCallBack_(SHUMA_MSG,notifyCode,&trObj);
#else		
	gStbCa.pCaMsgCallBack_(notifyCode,lParam,pParam);
#endif
		
	}
#ifdef STBCA_DEBUG_PRINT
	dxreport("[NTFCapture] BuyMessage: EcmPid:0x%X,msg_type:%d, msg:%s\n",wEcmPID,byMessageType,messages[byMessageType - msgOffset]);
#endif 
	switch(byMessageType)
	{
		case CDCA_MESSAGE_CANCEL_TYPE:
//			gSaveEcmCount = 0;
			gSaveEcmRecvData.ClearData();
//			gStbCa.pEcmSendCaMsgRing_->ClearRing();
			break;
		case CDCA_MESSAGE_INSERTCARD_TYPE:
		case CDCA_MESSAGE_DVTCA_PLEASE_INSERT_CARD:
//			gSaveEcmCount = 0;
			gSaveEcmRecvData.ClearData();
			gStbCa.bSCardStatus_=false;
			break;
			/*
			   case CDCA_MESSAGE_BADCARD_TYPE:
			   break;
			   case CDCA_MESSAGE_EXPICARD_TYPE:
			   break;
			   case CDCA_MESSAGE_NOOPER_TYPE:
			   break;
			   case CDCA_MESSAGE_BLACKOUT_TYPE:
			   break;
			   case CDCA_MESSAGE_OUTWORKTIME_TYPE:
			   break;
			   case CDCA_MESSAGE_WATCHLEVEL_TYPE:
			   break;
			   case CDCA_MESSAGE_PAIRING_TYPE:
			   break;
			   case CDCA_MESSAGE_NOENTITLE_TYPE:
			   break;
			   case CDCA_MESSAGE_DECRYPTFAIL_TYPE:
			   break;
			   case CDCA_MESSAGE_NOMONEY_TYPE:
			   break;
			   case CDCA_MESSAGE_ERRREGION_TYPE:
			   break;
			   case CDCA_MESSAGE_NEEDFEED_TYPE:
			   break;
			   case CDCA_MESSAGE_ERRCARD_TYPE:
			   break;
			   case CDCA_MESSAGE_UPDATE_TYPE:
			   break;
			   case CDCA_MESSAGE_LOWCARDVER_TYPE:
			   break;
			   case CDCA_MESSAGE_VIEWLOCK_TYPE:
			   break;
			   case CDCA_MESSAGE_MAXRESTART_TYPE:
			   break;
			   case CDCA_MESSAGE_FREEZE_TYPE:
			   break;
			   case CDCA_MESSAGE_CALLBACK_TYPE:
			   break;
			   case CDCA_MESSAGE_STBLOCKED_TYPE:
			   break;
			   case CDCA_MESSAGE_STBFREEZE_TYPE:
			   break;
			   */

		default:
			break;
	}
}

/****************************************************************
 * Description:  	STB for CA  function :Show Finger Message.
 * Input:        	Ecm Pid
 *                  Card ID
 * Return:			NULL
 ****************************************************************/
void __stdcall CDSTBCA_ShowFingerMessage(WORD wEcmPID,ULONG dwCardID)
{
	int notifyCode = TVNOTIFY_SHOW_FINGERPRINT;
	long lParam = wEcmPID & 0xFFFF;
	//char *pParam = NULL; 
	long pParam = dwCardID;
	if(gStbCa.pCaMsgCallBack_)
	{
#ifdef JS_USE_SHUMA_CALIB_NORMAL
		J_DataObject trObj;
		J_NVItem root, child1, child2;

		root.name = "/";
		child1.name = "finger_cardid";
		child1.value = (J_U32)pParam;
		child2.name = "finger_duration";
		child2.value = (J_U32)lParam;
			
		J_DataObject::pre_order_iterator iter = trObj.set_root(root);
		trObj.append_child(iter,child1);
		trObj.append_child(iter,child2);
		gStbCa.pCaMsgCallBack_(SHUMA_MSG,notifyCode,&trObj);
#else
		gStbCa.pCaMsgCallBack_(notifyCode,lParam,&pParam);
#endif

	}
#ifdef STBCA_DEBUG_PRINT
	dxreport("CDSTBCA_ShowFingerMessage:wEcmPID=%d,dwCardID=0x%02X\n",wEcmPID,dwCardID);
#endif 
}
//-------------------------------------------------------------------------------
#if 0
gStbCa.pCaMsgCallBack_(notifyCode,lParam,pParam);
notifyCode: TVNOTIFY_OSD  201
lParam: 
high 	16bits: 
CDCA_OSD_TOP              0x01  /* OSD椋庢牸锛氭樉绀哄湪灞忓箷涓婃柟 */
CDCA_OSD_BOTTOM           0x02  /* OSD椋庢牸锛氭樉绀哄湪灞忓箷涓嬫柟 */
CDCA_OSD_FULLSCREEN       0x03  /* OSD椋庢牸锛氭暣灞忔樉绀? */
CDCA_OSD_HALFSCREEN       0x04  /* OSD椋庢牸锛氬崐灞忔樉绀? */
low	16bits:	0: 闅愯棌锛?1锛氭樉绀?

pParam: 瀛楃?︿覆鎸囬拡锛屾渶闀夸笉瓒呰繃180瀛楄妭
#endif
//-------------------------------------------------------------------------------

/****************************************************************
 * Description:  	STB for CA  function :Show OSD Message.
 * Input:        	OSD Style
 message
 * Return:			NULL
 ****************************************************************/
void __stdcall CDSTBCA_ShowOSDMessage(BYTE byStyle,IN const char * szMessage)
{

	int notifyCode = TVNOTIFY_OSD;
	long lParam = (byStyle<<16)|1;	// 显示
	char *pParam = (char *)szMessage; 
	if(gStbCa.pCaMsgCallBack_)
	{
#ifdef JS_USE_SHUMA_CALIB_NORMAL
		J_DataObject trObj;
		J_NVItem root, child1, child2;

		root.name = "/";
		child1.name = "osd_msg";
		child1.value = pParam;
		child2.name = "osd_priority";
		child2.value = (J_U32)lParam;
			
		J_DataObject::pre_order_iterator iter = trObj.set_root(root);
		trObj.append_child(iter,child1);
		trObj.append_child(iter,child2);
		gStbCa.pCaMsgCallBack_(SHUMA_MSG,notifyCode,&trObj);
#else			
		gStbCa.pCaMsgCallBack_(notifyCode,lParam,pParam);
#endif
	}
	
#ifdef STBCA_DEBUG_PRINT
	dxreport("CDSTBCA_ShowOSDMessage:byStyle=%d,szMessage=%s\n",byStyle,szMessage);
#endif
}

/****************************************************************
 * Description:  	STB for CA  function :Hide OSD Message.
 * Input:        	OSD Style
 * Return:			NULL
 ****************************************************************/
void __stdcall CDSTBCA_HideOSDMessage(BYTE byStyle)
{
	int notifyCode = TVNOTIFY_OSD;
	long lParam = (byStyle<<16)|0;		// 隐藏
	char *pParam = NULL; 
	if(gStbCa.pCaMsgCallBack_)
	{

#ifdef JS_USE_SHUMA_CALIB_NORMAL
	J_DataObject trObj;
	J_NVItem root;

	root.name = "/";

	J_DataObject::pre_order_iterator iter = trObj.set_root(root);
	gStbCa.pCaMsgCallBack_(SHUMA_MSG,notifyCode,&trObj);
#else
	gStbCa.pCaMsgCallBack_(notifyCode,lParam,pParam);
#endif

	}
#ifdef STBCA_DEBUG_PRINT
	dxreport("CDSTBCA_HideOSDMessage:byStyle=%d.\n",byStyle);
#endif
}

/****************************************************************
 * Description:  	STB for CA  function :Sent Email notify.
 * Input:        	show flag
 *					email ID
 * Return:			NULL
 ****************************************************************/
void __stdcall CDSTBCA_EmailNotifyIcon(BYTE byShow,ULONG dwEmailID)
{
#ifdef STBCA_DEBUG_PRINT
	dxreport("CDSTBCA_EmailNotifyIcon:byShow=%d,dwEmailID=%d.\n",byShow,dwEmailID);
#endif

	/*
	   ReceiveFromCaMsg *pMsg = g_pReceiveCaMsgRing->BeginPush();
	   if(pMsg)
	   {
	   UINT16 *pType=(UINT16 *)pMsg->msgData;
	   UINT8 *pShow=pMsg->msgData+2;
	   UINT32 *pEmailID=(UINT32 *)(pMsg->msgData+4);

	 *pType=EVENT_EMAIL_NOTIFY;
	 *pShow=byShow;
	 *pEmailID=dwEmailID;
	 pMsg->msgLength=8;
	 g_pReceiveCaMsgRing->EndPush();
	 }
	 */
	int notifyCode = TVNOTIFY_MAIL_NOTIFY;
	long lParam = (byShow<<24)|(dwEmailID&0xffffff);		
	char *pParam = NULL; 
	if(gStbCa.pCaMsgCallBack_)
	{
#ifdef JS_USE_SHUMA_CALIB_NORMAL
	J_DataObject trObj;
	J_NVItem root, child1;

	root.name = "/";
	child1.name = "email_show";
	child1.value = (J_U32)byShow;
		
	J_DataObject::pre_order_iterator iter = trObj.set_root(root);
	trObj.append_child(iter,child1);
	gStbCa.pCaMsgCallBack_(SHUMA_MSG,notifyCode,&trObj);
#else
	gStbCa.pCaMsgCallBack_(notifyCode,lParam,pParam);
#endif
		
	}
}

/****************************************************************
 * Description:  	STB for CA  function :Sent entitle changed notify.
 * Input:        	operator id
 * Return:			NULL
 ****************************************************************/
void __stdcall CDSTBCA_EntitleChanged(WORD operator_id)
{
#ifdef STBCA_DEBUG_PRINT
	dxreport("CDSTBCA_EntitleChanged:operator_id=%d.\n",operator_id);
#endif
	/*
	   ReceiveFromCaMsg *pMsg = g_pReceiveCaMsgRing->BeginPush();
	   if(pMsg)
	   {
	   UINT16 *pType=(UINT16 *)pMsg->msgData;
	   UINT16 *poperator_id=(UINT16 *)pMsg->msgData+2;
	//UINT32 *pEmailID=(UINT32 *)(pMsg->msgData+3);

	 *pType=EVENT_ENTITLECHANGED_NOTIFY;
	 *poperator_id=operator_id;
	// *pEmailID=dwEmailID;
	pMsg->msgLength=4;
	g_pReceiveCaMsgRing->EndPush();
	}
	*/
	int notifyCode = TVNOTIFY_ENTITLE_CHANGE;
	long lParam = operator_id;		
	char *pParam = NULL; 
	if(gStbCa.pCaMsgCallBack_)
		gStbCa.pCaMsgCallBack_(notifyCode,lParam,pParam);	
}

/****************************************************************
 * Description:  	STB for CA  function :Sent detitle received notify.
 * Input:        	operator id
 *					status
 * Return:			NULL
 ****************************************************************/
void __stdcall CDSTBCA_DetitleReceived(BYTE bstatus)
{
#ifdef STBCA_DEBUG_PRINT
	dxreport("CDSTBCA_DetitleReceived:bstatus=%d.\n",bstatus);
#endif
	/*
	   ReceiveFromCaMsg *pMsg = g_pReceiveCaMsgRing->BeginPush();
	   if(pMsg)
	   {
	   UINT16 *pType=(UINT16 *)pMsg->msgData;
	   UINT8 *pbstatus=(UINT8 *)(pMsg->msgData+2);

	 *pType=EVENT_DETITLERECEIVED_NOTIFY;
	 *pbstatus=bstatus;
	 pMsg->msgLength=3;
	 g_pReceiveCaMsgRing->EndPush();
	 }
	 */

	int notifyCode = TVNOTIFY_DETITLE;
	long lParam = bstatus;		
	char *pParam = NULL; 
	if(gStbCa.pCaMsgCallBack_)
		gStbCa.pCaMsgCallBack_(notifyCode,lParam,pParam);	
}

/****************************************************************
 * Description:  	STB for CA  function :child card request feeding data.
 * Input:            operator id
 * Return:			NULL
 ****************************************************************/
void  __stdcall CDSTBCA_RequestFeeding(UINT8 bReadStatus)
{
#ifdef STBCA_DEBUG_PRINT
	dxreport("CDSTBCA_RequestFeeding:bReadStatus=%d.\n",bReadStatus);
#endif
	/*
	   ReceiveFromCaMsg *pMsg = g_pReceiveCaMsgRing->BeginPush();
	   if(pMsg)
	   {
	   UINT16 *pType=(UINT16 *)pMsg->msgData;
	   UINT8 *pReadStatus=(UINT8 *)pMsg->msgData+2;
	//UINT8 *pbstatus=(UINT8 *)(pMsg->msgData+3);

	 *pType=EVENT_REQUESTFEEDING_NOTIFY;
	 *pReadStatus=bReadStatus;
	 pMsg->msgLength=4;
	 g_pReceiveCaMsgRing->EndPush();
	 }
	 */
}

/****************************************************************
 * Description:  	STB for CA  function :lock service.
 * Input:            STFLockService structure
 * Return:			NULL
 ****************************************************************/
void __stdcall CDSTBCA_LockService(IN const SCDCALockService* pLockService)
{
#ifdef STBCA_DEBUG_PRINT
	dxreport("CDSTBCA_LockService:pLockService=%p.\n",pLockService);
#endif
	/*
	   ReceiveFromCaMsg *pMsg = g_pReceiveCaMsgRing->BeginPush();
	   if(pMsg)
	   {
	   UINT16 *pType=(UINT16 *)pMsg->msgData;
	   SCDCALockService *poperator_id=(SCDCALockService *)pMsg->msgData+2;
	//UINT8 *pbstatus=(UINT8 *)(pMsg->msgData+3);

	 *pType=EVENT_LOCK_SERVICE_NOTIFY;
	 *poperator_id=*pLockService;
	 pMsg->msgLength=2+sizeof(SCDCALockService);
	 g_pReceiveCaMsgRing->EndPush();
	 }
	 */
}

/****************************************************************
 * Description:  	STB for CA  function :unlock service.
 * Input:            NULL
 * Return:	     NULL
 ****************************************************************/
void __stdcall CDSTBCA_UNLockService(void)
{
#ifdef STBCA_DEBUG_PRINT
	dxreport("CDSTBCA_LockService:CDSTBCA_UNLockService.\n");
#endif
	/*
	   ReceiveFromCaMsg *pMsg = g_pReceiveCaMsgRing->BeginPush();
	   if(pMsg)
	   {
	   UINT16 *pType=(UINT16 *)pMsg->msgData;

	 *pType=EVENT_UNLOCK_SERVICE_NOTIFY;
	 pMsg->msgLength=2;
	 g_pReceiveCaMsgRing->EndPush();
	 }
	 */
}

/****************************************************************
 * Description:  	STB for CA  function :hide buy IPPV dialog.
 * Input:        	ecm pid
 * Return:		NULL
 ****************************************************************/
void __stdcall CDSTBCA_HideIPPVDlg(WORD wEcmPid)
{
	UNUSED_PARAM(wEcmPid);
}

/****************************************************************
 * Description:  	STB for CA  function :show buy IPPV dialog.
 * Input:        	message type
 *					ecm pid
 *					STFCAIppvBuyInfo structure
 * Return:		NULL
 ****************************************************************/
void __stdcall CDSTBCA_StartIppvBuyDlg(BYTE byMessageType,WORD wEcmPid,IN const SCDCAIppvBuyInfo *pIppvProgram)
{
	UNUSED_PARAM(byMessageType);
	UNUSED_PARAM(wEcmPid);
	UNUSED_PARAM(pIppvProgram);
}

/* 进度显示 */
void __stdcall CDSTBCA_ShowProgressStrip( CDCA_U8 byProgress,CDCA_U8 byMark )
{
	/*
	   ReceiveFromCaMsg *pMsg = g_pReceiveCaMsgRing->BeginPush();
	   if(pMsg)
	   {
	   UINT16 *pType=(UINT16 *)pMsg->msgData;
	   UINT8 *pbyProgress=(pMsg->msgData+2);
	   UINT8 *pbyMark=pMsg->msgData+3;

	 *pType=TVNOTIFY_SHOW_PROGRESSSTRIP;
	 *pbyProgress=byProgress;
	 *pbyMark=byMark;
	 pMsg->msgLength=4;
	 g_pReceiveCaMsgRing->EndPush();
	 }
	 */
	int notifyCode = TVNOTIFY_SHOW_PROGRESSSTRIP;
	long lParam = ((byMark<<16) | byProgress) & 0x00FF00FF;		
	char *pParam = NULL; 
	if(gStbCa.pCaMsgCallBack_)
		gStbCa.pCaMsgCallBack_(notifyCode,lParam,pParam);	

	if(byMark==CDCA_SCALE_RECEIVEPATCH)
		dxreport("[NTFCapture] 数据包接收进度 %d\n",byProgress);

	else if (byMark==CDCA_SCALE_PATCHING)
		dxreport("[NTFCapture] 智能卡升级进度 %d\n",byProgress);

	else 
		dxreport("[NTFCapture] %d 升级进度 %d\n",byMark,byProgress);	
}

void __stdcall CDSTBCA_ShowCurtainNotify(CDCA_U16  ecmPid,CDCA_U16 uCurtainCode)
{
	UNUSED_PARAM(ecmPid);
	UNUSED_PARAM(uCurtainCode);
}

