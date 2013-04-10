//包含中文字符,供vim 识别
#include "typ.h"
#include "stbca.h"
#include "capture.h"
#include "cdcalib4stb.h"
#ifdef JS_USE_SHUMA_CALIB_NORMAL
#include <DVTCAS_STBInf.h>
#include <DVTSTB_CASInf.h>
#endif
#include "stbca_utility.h"
#include "capture_def.h"
#include "simplethread.h"
#include "xprocess.h"

#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>

#define LOG_LEVEL LOG_TRACE
#define LOG_TAG "stbca"
#include "dxreport.h"

#pragma warning(disable:4311)
#define STBCA_DEBUG_PRINT

extern ObjStbCa gStbCa;
//extern MutexT			g_hResourceMutex;
#ifdef JS_USE_SHUMA_CALIB_NORMAL
extern bool g_bEcmProcessing;
#endif

ObjStbCa::ObjStbCa():emmpid_(0),bSCardStatus_(false),pCaMsgCallBack_(NULL)
{
#ifdef WIN32
	gStbCa.fileName_ = "stbca.bin";
#else
	gStbCa.fileName_ = "/data/data/novel.supertv.dvb/stbca.bin";
#endif
	InitFlashFile();
	saveDescramCount_ = 0;
	for(int i=0; i<8; i++)
	{
		memset(&saveDescrambs_[i],0,sizeof(Descrambling));
	}
	pEcmSendCaMsgRing_ = new BlockRing<SendToCAMsg,MAX_SEND_ECMSGQUEUE_SIZE>;
	pEmmSendCaMsgRing_ = new BlockRing<SendToCAMsg,MAX_SEND_EMMSGQUEUE_SIZE>;
	bStbCaHasInited_ = true;
	dxreport("Obj StbCA has Inited Succeed");
	return;

}
ObjStbCa::~ObjStbCa()
{
	dxreport("ObjStbCa de-initialized begin >>>\n");
	// 下面2句，完成资源访问互斥,解决当资源释放后，异步仍然回调问题
	//	AutoLockT	lock(g_hResourceMutex);
	Ca_Stop();
	//关闭已有的filters 列表
	{
		
//		AutoLockT lock_it1(gStbCa.mutexCalibControl_);
//		AutoLockT lock_it2(gStbCa.mutexMyControl_);
		CloseGMapDemuxFilters_Lock(gStbCa.mapCaLibControl_hFilter_ReqIDPID_,gStbCa.mutexCalibControl_);
		CloseGMapDemuxFilters_Lock(gStbCa.mapMyControl_hFilter_ReqIDPID_,gStbCa.mutexMyControl_);
	}
#if !defined(WIN32)
	CDCASTB_Close();
#endif 
	NS_sleep(100);	// 先给个保险系数
	if(hTVDevice_!=0)
	{
		tvdevice_close(hTVDevice_);
		hTVDevice_ = 0;
	}
	if( 0 != pEcmSendCaMsgRing_)
	{
		delete pEcmSendCaMsgRing_;
		pEcmSendCaMsgRing_ = 0;
	}
	if( 0 != pEmmSendCaMsgRing_)
	{
		delete pEmmSendCaMsgRing_;
		pEmmSendCaMsgRing_ = 0;
	}
	dxreport("ObjStbCa de-initialized end <<<\n");
}


int GetStbcaBinSize()
{
	struct stat st;
	int ret = stat(gStbCa.fileName_, &st);
	if(ret == 0)
		ret = st.st_size;
	else
		ret = -1;
	return ret;
}

/****************************************************************
 * Description:  	init flash file .
 * Input:        	NULL
 * Return:		NULL
 ****************************************************************/
BYTE gFlashData[FLASH_FILE_SIZE];
void InitFlashFile()
{
	dxreport("InitFlashFile before open file size 0x%X\n",GetStbcaBinSize());

	FILE *fp=fopen(gStbCa.fileName_,"ab+");
	if(fp)
	{
		dxreport("InitFlashFile after open file size 0x%X\n",GetStbcaBinSize());
		int seekRet = fseek(fp, 0, SEEK_END);
		int size = ftell(fp);
		dxreport("flash size:%d\n",size);
		fclose(fp);

		if (size != FLASH_FILE_SIZE)
		{
			dxreport("init flash file called\n");
			memset(gFlashData,0,sizeof(gFlashData));
			CDSTBCA_WriteBuffer(1,gFlashData,FLASH_FILE_SIZE);
		}
		return;
	}
	dxreport("init flash file failed\n");
}

/****************************************************************
 * Description:  	change hex number to char.
 * Input:        	hex number
 * Return:		char
 ****************************************************************/
char getHex(BYTE v)
{
	return (v&0xf)<=0x9 ? (v&0xf)+'0' : ((v&0xf)-0xa)+'A';
}

/****************************************************************
 * Description:  	print char.
 * Input:        	char* 
 *					buffer size
 * Return:			char*
 ****************************************************************/
char *PrintHex(const BYTE *pBuf,USHORT BufSize)
{
	static char s[1024];
	s[0]=' ';
	if(pBuf==NULL || BufSize == 0) 
	{
		s[0]=0;
		return s;
	}
	for(int i=0; (i<BufSize) && ((i*3+2)<1024); i++)
	{
		s[1+i*3+0]=getHex(pBuf[i]>>4);
		s[1+i*3+1]=getHex(pBuf[i]&0xf);
		s[1+i*3+2]=' ';
		s[1+i*3+3]=0;
	}

	s[1023]='\0';
	return s;
}
void CloseGMapDemuxFilters(map<HANDLE,RegIDPID>& map_hFilter_ReqIDPID)
{
	//关闭已有的filters 列表
	map<HANDLE,RegIDPID>::iterator it;
	for(it=map_hFilter_ReqIDPID.begin(); it!=map_hFilter_ReqIDPID.end();it++)
	{
		if(it->second.wPid !=0x10)
		{
			tsdemux_delSectionFilter(it->first,NULL);
			map_hFilter_ReqIDPID.erase(it);
		}
	}
//	map_hFilter_ReqIDPID.clear();
}
void CloseGMapDemuxFilters_Lock(map<HANDLE,RegIDPID>& map_hFilter_ReqIDPID, MutexT & mutex)
{
	map<HANDLE,RegIDPID> tmp;
	map<HANDLE,RegIDPID>::iterator it;

	//关闭已有的filters 列表
	{
		AutoLockT lock_it(mutex);
		tmp = map_hFilter_ReqIDPID;
		for(it=map_hFilter_ReqIDPID.begin(); it!=map_hFilter_ReqIDPID.end();it++)
		{
			if(it->second.wPid !=0x10)
			{
//				tsdemux_delSectionFilter(it->first,NULL);
				map_hFilter_ReqIDPID.erase(it);
			}
		}
	}
	for(it=tmp.begin(); it!=tmp.end();it++)
	{
		if(it->second.wPid !=0x10)
		{
			tsdemux_delSectionFilter(it->first,NULL);
		}
	}
//	map_hFilter_ReqIDPID.clear();
}

// 关闭特定的demux filter
void CloseGMapDemuxFilter(map<HANDLE,RegIDPID>& map_hFilter_ReqIDPID,CDCA_U8  byReqID,	CDCA_U16 wPid)
{
	map<HANDLE,RegIDPID>::iterator it;
	for(it=map_hFilter_ReqIDPID.begin(); it!=map_hFilter_ReqIDPID.end();it++)
	{
		if(it->second.byReqID == byReqID && it->second.wPid == wPid)
		{
			dxreport("delete filter, byReqID:%d wPid:0x%x\n", byReqID, wPid);
			tsdemux_delSectionFilter(it->first,NULL);
			map_hFilter_ReqIDPID.erase(it);
			break;
		}
	}
}
/*
// 关闭特定的demux filter, 会崩溃(两个线程同时删时)
void CloseGMapDemuxFilter_Lock(map<HANDLE,RegIDPID>& map_hFilter_ReqIDPID,CDCA_U8  byReqID,	CDCA_U16 wPid, MutexT & mutex)
{
	map<HANDLE,RegIDPID>::iterator it;
	for(it=map_hFilter_ReqIDPID.begin(); it!=map_hFilter_ReqIDPID.end();it++)
	{
		if(it->second.byReqID == byReqID && it->second.wPid == wPid)
		{
			dxreport("delete filter, byReqID:%d wPid:0x%x\n", byReqID, wPid);
			tsdemux_delSectionFilter(it->first,NULL);
			{
				AutoLockT lockIt(mutex);
				map_hFilter_ReqIDPID.erase(it);
			}
			break;
		}
	}
}
*/
// 关闭特定的demux filter
void CloseGMapDemuxFilter_Lock(map<HANDLE,RegIDPID>& map_hFilter_ReqIDPID,CDCA_U8  byReqID,	CDCA_U16 wPid, MutexT & mutex)
{
	HANDLE handle = 0;
	{
		AutoLockT lockIt(mutex);
		map<HANDLE,RegIDPID>::iterator it;
		for(it=map_hFilter_ReqIDPID.begin(); it!=map_hFilter_ReqIDPID.end();it++)
		{
			if(it->second.byReqID == byReqID && it->second.wPid == wPid)
			{
				dxreport("delete filter, byReqID:%d wPid:0x%x\n", byReqID, wPid);
				//tsdemux_delSectionFilter(it->first,NULL);
				handle = it->first;
				map_hFilter_ReqIDPID.erase(it);
				break;
			}
		} 
	}
	if(handle!=0)
	{
		tsdemux_delSectionFilter(handle,NULL);
	}

}



/****************************************************************
 * Description:  	STB for CA  function :Set CW.
 * Input:        	ecmpid
 *					odd key
 *					even key
 *					key length
 *					bTaingEnabled
 * Return:			NULL
 有几个cwCount, 就设置几次
 ****************************************************************/
void SetCW(DVBCW *pDVBcw,int cwCount)
{
	unsigned char inData[256]={0};
	unsigned char outData[256]={0};
	int outputLen=256;

	//初始化SCFunction数据命令为setcw
	inData[0]=0x17;

	//设置tunerID至偏移20位
	inData[20]=(BYTE)0;
	inData[21]=(BYTE)cwCount;

	for (int i=0;i<cwCount;i++)
	{
		inData[22]=(BYTE)i;
		memcpy(inData+3,pDVBcw[i].oddKey,8);
		memcpy(inData+11,pDVBcw[i].eventKey,8);
		*(inData+2)=(BYTE)(pDVBcw[i].pid>>8);
		*(inData+1)=(BYTE)pDVBcw[i].pid;
		outputLen=256;

		tvdevice_SCFuntion(gStbCa.hTVDevice_,inData,256,outData,&outputLen);
	}
}
// 清理2组cw 控制字
// 当CA 停止运行时的清理操作，
// 注意不要与CA 线程冲突
void ClearCW(void)
{

	DVBCW cw[2];
	memset(cw,0,2*sizeof(DVBCW));
//	SetCW(cw,2);
}
// 检查卡状态
void CheckSCardStatus()
{
//	AutoLockT lock_it(gStbCa.mutexCheckCardStatus_);
	bool status = tvdevice_SCardInserted(gStbCa.hTVDevice_);
	if(status != gStbCa.bSCardStatus_)
	{
		gStbCa.bSCardStatus_ =status;
		if(status) {
			gStbCa.pEcmSendCaMsgRing_->ClearRing();
			CloseGMapDemuxFilters_Lock(gStbCa.mapCaLibControl_hFilter_ReqIDPID_,gStbCa.mutexCalibControl_);
			CloseGMapDemuxFilters_Lock(gStbCa.mapMyControl_hFilter_ReqIDPID_,gStbCa.mutexMyControl_);
			dxreport("..........Before Call CDCASTB_SCInsert....\n");
			unsigned char atr[64];
			BYTE len;
			CDSTBCA_SCReset(atr,&len); // 调用卡复位，提高速度
			CDCASTB_SCInsert();
		}
		else {
#ifdef JS_USE_SHUMA_CALIB_NORMAL
			g_bEcmProcessing = false;  // 无卡时切换台不要等待处理ecm
#endif			
			CloseGMapDemuxFilters_Lock(gStbCa.mapCaLibControl_hFilter_ReqIDPID_,gStbCa.mutexCalibControl_);
			CloseGMapDemuxFilters_Lock(gStbCa.mapMyControl_hFilter_ReqIDPID_,gStbCa.mutexMyControl_);
			CDCASTB_SCRemove();
		}
	}
}

/****************************************************************
 * Description:  	push One Message from Msg Ring come from CA lib to UI
 * Input:        	none
 * Output:	MsgBuf.
 * Return:	data copied.
 ****************************************************************/
//	typedef int (*TVNOTIFY)(int notifyCode,long lParam,void *pParam);

void STB_RecvFromCaMsg()
{
}

/****************************************************************
 * Description:  	每10ms, 处理一次发往CA LIB 的消息
 每1秒，检查一次卡状态
 每2秒，核对一下邮件状态
 * Input:        	param
 * Return:			NULL
 ****************************************************************/
// CA工作线程过程
// #define _TEST_OSD_MSG
#ifdef _TEST_OSD_MSG
const char *pTestMsg = "test osd string";
#endif
unsigned int __stdcall CAWorkProc(PVOID param)
{
	int nCheckStatus=0;
	int nCheckEmail=0;
#ifdef _TEST_OSD_MSG
	int  nCheckOsd = 0;
#endif
	CheckSCardStatus();
	simplethread *pThread = reinterpret_cast<simplethread*>(param);
	NS_GetTickCount();
	while(!pThread->check_stop())
	{
		nCheckStatus++;
		nCheckEmail++;
#ifdef _TEST_OSD_MSG
		nCheckOsd ++ ;
#endif
		STB_SendDataToCaLib();
		if(nCheckStatus>=15)	// 1500 ms check 一次
		{
			nCheckStatus = 0;
			CheckSCardStatus();
		}
#ifdef _TEST_OSD_MSG

		if(nCheckOsd==100)	// 10 s check 一次
		{
			CDSTBCA_ShowOSDMessage(0,"pTestMsg");
		}
		if(nCheckOsd==200)	// 20 s check 一次
		{
			nCheckOsd = 0;
			CDSTBCA_HideOSDMessage(0);
		}
#endif
		CDSTBCA_Sleep(100);
	}

	return 0;
}
RecvData gSaveEcmRecvData;
// int gSaveEcmCount;

RecvData gSaveEmmRecvData;


bool MyMemCmp(RecvData *rd, char *p, int len)
{
	//len == 0 时认为数据发生了变化。拔掉信号线情况下，数据一直为0,如果不告诉CA库，CA库会一直等数据，但刚才的Filter已经过期，永远不会收到数据
	if(rd->len != len || len == 0) return false;
	for(int i=0; i<len; i++)
	{
		if(rd->data[i]!=p[i]) return false;
	}
	return true;
}

bool IsCorrectEcmPid(WORD pid)
{
	for(int i=0; i<gStbCa.saveDescramCount_;i++)
	{
		if(gStbCa.saveDescrambs_[i].ecmPid == pid) return true;
	}
	return false;
}

/****************************************************************
 * Description:  	send one buffered CA Message(ecm, emm) to CA lib.
 * Input:        	none
 * Output:	none                    
 * Return:	none
 ****************************************************************/

void STB_SendDataToCaLib()
{
	SendToCAMsg	*pMsg = gStbCa.pEcmSendCaMsgRing_->BeginPop();
	if(pMsg)
	{
		switch(pMsg->type)
		{
			case CAMT_PRIVATEDATA_GOT:
				{
					CAPrivateDataGot &ref = pMsg->PrivateDataGot_;

//					if(gStbCa.bSCardStatus_==true )
					{
						// 若发现pid 不符合，请找demux 协助解决，demux 要保证delete filter 后不再有数据上来
						// 换台时会清理过滤器
						// 这里不能reject, 否则不能更新过滤器
						// 换台后，ECM pid 不相同不能进，因为可能是旧的ecm . 频繁换台时出现
						if(IsCorrectEcmPid(ref.pid))
						{
							dxreport("ecm SendTo Calib, reqid:%d, pid:0x%x,timeout:%d\n",ref.ReqID,ref.pid,ref.bTimeOut);
							CDCASTB_PrivateDataGot(ref.ReqID, ref.bTimeOut, ref.pid, ref.RecvData, ref.RecvDataLen);
							gStbCa.enableEmm_ = true;
						}
						else if(ref.pid == 0x10) // 放过nit id
						{
							dxreport("SendTo Calib, reqid:%d, pid:0x%x,timeout:%d,v5\n",ref.ReqID,ref.pid,ref.bTimeOut);
							CDCASTB_PrivateDataGot(ref.ReqID, ref.bTimeOut, ref.pid, ref.RecvData, ref.RecvDataLen);
						}
						
						else
						{
							dxreport("pid invalid, discard!");
						}
					}
				}
				break;
			default:
				;		
		}
		gStbCa.pEcmSendCaMsgRing_->EndPop();
		return; // 当有ecm 时，就不再处理emm 了，以此提高ecm 优先权(100 ms)
	}

	pMsg = gStbCa.pEmmSendCaMsgRing_->BeginPop();
	if(pMsg)
	{
		switch(pMsg->type)
		{
			case CAMT_PRIVATEDATA_GOT:
				{
					CAPrivateDataGot &ref = pMsg->PrivateDataGot_;
					dxreport("emm SendTo Calib, reqid:%d, pid:%d,timeout:%d\n",ref.ReqID,ref.pid,ref.bTimeOut);
					CDCASTB_PrivateDataGot(ref.ReqID, ref.bTimeOut, ref.pid, ref.RecvData, ref.RecvDataLen);
				}
				break;
			default:
				;		
		}
		gStbCa.pEmmSendCaMsgRing_->EndPop();
//		gStbCa.pEmmSendCaMsgRing_->PrintPos("Emm Buffer->Calib");
	}
}

void OncePrivateDataGotCallBack( utHandle hFilter,long iTuner,utPid pid,utTid tid,utByte *data,long datasize,utContext context)
{
	UNUSED_PARAM(hFilter);
	UNUSED_PARAM(iTuner);
#ifdef STBCA_DEBUG_PRINT
		dxreport("OncePrivateDataGotCallBack begin >>>hFilter %p",hFilter);
#endif 
	if(pid ==0x10 && gStbCa.bSCardStatus_== false) return;  //0x10 nit 信息在卡未准备好时先不送
	bool bTimeout = (0 == datasize) ? true : false;
	int reqID = reinterpret_cast<int >(static_cast<int *>(context));
	
	if(BUFEcmCASTB_PrivateDataGot(reqID, bTimeout, pid, data, (U16)datasize))
	{
//		AutoLockT lock_it(gStbCa.mutexMyControl_);
		CloseGMapDemuxFilter_Lock(gStbCa.mapMyControl_hFilter_ReqIDPID_,reqID,pid, gStbCa.mutexMyControl_);
//		tsdemux_delSectionFilter(hFilter,NULL);
	}
#ifdef STBCA_DEBUG_PRINT
	dxreport("OncePrivateDataGotCallBack end <<< ,reqID :%d, pid:0x%x, utTid:%d, data:%p, datasize:%ld,data:%s\n",reqID,pid,tid,data,datasize, PrintHex(data,datasize));
#endif 
}

void ConPrivateDataGotCallBack( utHandle hFilter,long iTuner,utPid pid,utTid tid,utByte *data,long datasize,utContext context)
{
	UNUSED_PARAM(hFilter);
	UNUSED_PARAM(iTuner);
	bool bTimeout = (0 == datasize) ? true : false;
	int reqID = reinterpret_cast<int >(static_cast<int *>(context));

#ifdef STBCA_DEBUG_PRINT
		dxreport("ConPrivateDataGotCallBack,hFilter %p,reqID:%d, pid:0x%x, utTid:%d, datasize:%ld, data:%s\n",hFilter,reqID,pid,tid,datasize,((datasize>8)?PrintHex(data,8):PrintHex(data,datasize)));
#endif 

	// 换台后，ecm 未来之前,emm 数据不能进buffer，保证切台显示画面的速度
	if(gStbCa.enableEmm_ == false )  // 非清流(简单判断), ecm未到达之前扔掉。由换台请enableEmm
	{
		dxreport("emm discarded-v4\n");
		return;
	}
	bool res=BUFEmmCASTB_PrivateDataGot(reqID, bTimeout, pid, data, (U16)datasize);
	
// 数码CA 需要删除该过滤器	
#ifdef JS_USE_SHUMACA_SEARCH
	if(res) // 已经放入缓冲区
	{
		dxreport("emm data enter to buffer\n");
//		AutoLockT lock_it(gStbCa.mutexCalibControl_);
		CloseGMapDemuxFilter_Lock(gStbCa.mapCaLibControl_hFilter_ReqIDPID_,reqID,pid,gStbCa.mutexCalibControl_);
//		tsdemux_delSectionFilter(hFilter,NULL);
	}
#endif

}

bool  BUFEcmCASTB_PrivateDataGot( CDCA_U8 byReqID, CDCA_BOOL bTimeout, CDCA_U16 wPid, const CDCA_U8* pbyData,CDCA_U16 wLen)
{
	//	AutoLockT	lock(g_hResourceMutex);
	// 重复的filter 数据，不用往buffer 中存放
	if(MyMemCmp(&gSaveEcmRecvData, (char *)pbyData,wLen))
	{
		dxreport("BUFEcmCASSTB_PrivateDataGot recv same data,discard!\n");
		return false;
	}
	{
		gSaveEcmRecvData.len = wLen;
		memcpy(gSaveEcmRecvData.data,pbyData,wLen);
		
	}
//#define _NO_ECM_BUFFER	
#ifdef _NO_ECM_BUFFER	
	CDCASTB_PrivateDataGot(byReqID, bTimeout, wPid, pbyData, wLen);
	return true;	
#endif	
//#define _ONLY_ONE_ECM
#ifdef _ONLY_ONE_ECN
gStbCa.pEcmSendCaMsgRing_->ClearRing();
#endif

again:	
	SendToCAMsg *pMsg = gStbCa.pEcmSendCaMsgRing_->BeginPush();
	if(pMsg)
	{
		pMsg->type = CAMT_PRIVATEDATA_GOT;
		pMsg->PrivateDataGot_.ReqID = byReqID;
		pMsg->PrivateDataGot_.pid = wPid;
		pMsg->PrivateDataGot_.bTimeOut = bTimeout;
		memcpy(pMsg->PrivateDataGot_.RecvData, pbyData, wLen);
		pMsg->PrivateDataGot_.RecvDataLen = wLen;
		gStbCa.pEcmSendCaMsgRing_->EndPush();
	}
	else
	{
		dxreport("Warning! Ecm Buffer Ring Overflow!!!,reqID:%d, pid:0x%x\n",byReqID, wPid);
//		gStbCa.pEcmSendCaMsgRing_->PrintPos("Ecm Ring Overflow");
		gStbCa.pEcmSendCaMsgRing_->ClearRing();
		goto again;
	}
	return true;
}
bool  BUFEmmCASTB_PrivateDataGot( CDCA_U8 byReqID, CDCA_BOOL bTimeout, CDCA_U16 wPid, const CDCA_U8* pbyData,CDCA_U16 wLen)
{
	//	AutoLockT	lock(g_hResourceMutex);
	//	if(g_bResourceRemoved==TRUE) return;	// 资源已释放时，直接返回
	
	if(MyMemCmp(&gSaveEmmRecvData, (char *)pbyData,wLen))
	{
		return false;
	}
	gSaveEmmRecvData.len = wLen;
	memcpy(gSaveEmmRecvData.data,pbyData,wLen);
			
	SendToCAMsg *pMsg = gStbCa.pEmmSendCaMsgRing_->BeginPush();
	if(pMsg)
	{
		pMsg->type = CAMT_PRIVATEDATA_GOT;
		pMsg->PrivateDataGot_.ReqID = byReqID;
		pMsg->PrivateDataGot_.pid = wPid;
		pMsg->PrivateDataGot_.bTimeOut = bTimeout;
		memcpy(pMsg->PrivateDataGot_.RecvData, pbyData, wLen);
		pMsg->PrivateDataGot_.RecvDataLen = wLen;
		gStbCa.pEmmSendCaMsgRing_->EndPush();
//		gStbCa.pEmmSendCaMsgRing_->PrintPos("Emm stb->buffer");
	}
	else
	{
		dxreport("Warning! Emm Buffer Ring Overflow!!!\n");
//		gStbCa.pEmmSendCaMsgRing_->PrintPos("Emm Ring Overflow");
		gStbCa.pEmmSendCaMsgRing_->ClearHalfRing();
	}
	return true;
}

/****************************************************************
 * Description:  	每10ms, 处理一次发往CA LIB 的消息
 每1秒，检查一次卡状态
 * Input:        	param
 * Return:			NULL
 ****************************************************************/
// 子母卡工作线程过程, for 数码
enum SonMotherStatus
{
	GetMotherInfo,
	SetMotherInfo,
};

#ifdef JS_USE_SHUMA_CALIB_NORMAL
unsigned int __stdcall SonMotherWorkProc(PVOID param)
{
	dxreport("Enter SonMotherWorkProc\n");
	simplethread *pThread = reinterpret_cast<simplethread*>(param);
	int nCheckStatus=5;
	bool bReqireRemoveCard= true;  // 请求拔卡
	bool bHasJudgeMotherCard = false;
	bool bIsMotherCard = false;;
	bool bBeginWaiting = false; // 开始等待ecm 数据到达，2 s 后调用读母卡信息
	int iWaitCount = -1;;
	DWORD cardID;
	SonMotherStatus status=GetMotherInfo;
	
	BYTE dataBuf[250];
	BYTE dataLen = sizeof(dataBuf);
	while(!pThread->check_stop())
	{
		dxreport("SonMotherWorkProc checkstatus %d\n",nCheckStatus);
		nCheckStatus++;
		STB_SendDataToCaLib();
		CDSTBCA_Sleep(100);
		if(0<nCheckStatus && nCheckStatus<5) continue;
		// 500 ms check 一次卡是否插上或拔出			
		nCheckStatus = 0;
		CheckSCardStatus();
		if(gStbCa.bSCardStatus_==false) // 卡被拔出
		{
			dxreport("SonMotherWorkProc card removeed\n");
			bReqireRemoveCard = false;
			bHasJudgeMotherCard = false;
			bBeginWaiting = false;
			iWaitCount = -1;
		}
		else // 卡在卡槽里
		{
			dxreport("SonMotherWorkProc card inserted, ReqireRemoveCard %d\n",bReqireRemoveCard);
			if(bReqireRemoveCard == true) continue;// 卡未被拔出过,等待....
			if(bHasJudgeMotherCard == false) //	
			{
				dxreport("SonMotherWorkProc has judge mother %d\n",bHasJudgeMotherCard);
				if(DvtCAGetMotherInfo(&cardID)==DVTCA_OK)
				{
					dxreport("SonMotherWorkProc getMotherid %d\n",cardID);
					if(cardID !=0 ) // 插入的卡是子卡
					{
						bIsMotherCard = false;
						dxreport("SonMotherWorkProc getMotherid failed??????\n");
					}
					else
					{
						//提示: 正在读取母卡信息，请稍等
						bBeginWaiting = true;
						iWaitCount = 4;
						bIsMotherCard = true;
						DvtCAShowMotherCardPair(CARDPAIR_READING_MOTHERINFO);
					}
					bHasJudgeMotherCard = true;
				}
//				continue; // 继续
			}
			if(bIsMotherCard && status == GetMotherInfo) // 已判断是母卡
			{
				dxreport("SonMotherWorkProc ismothercard waitCount %d\n",iWaitCount);
				if(0<iWaitCount && iWaitCount <= 4)
				{
					iWaitCount--;
					continue;  // 等待
				}
				else if(iWaitCount == 0 && status == GetMotherInfo) // 2s 延时到
				{
					if(DVTCASTB_GetCorrespondInfo(&dataLen,dataBuf)==DVTCA_OK)
					{
						dxreport("SonMotherWorkProc getCorrespond ok!!!\n");
						// 成功获取母卡信息，请插入要配对的子卡
						bReqireRemoveCard = true;
						status = SetMotherInfo;

						DvtCAShowMotherCardPair(CARDPAIR_INSERT_SONCARD);
					}
					else
					{
						dxreport("SonMotherWorkProc getCorrespond failed!!!\n");
						// 读母卡信息失败?
						// 提示:获取母卡信息失败，请插入要配对的母卡
						bReqireRemoveCard = true; // 请求拔卡
						DvtCAShowMotherCardPair(CARDPAIR_GET_MOTHERINFO_FAILED);
					}
				}
				else // 其它时间段
				{
					dxreport("SonMotherWorkProc noway!!!\n");
					// 不可能发生
				}
			}
			else  // 是子卡
			{
				dxreport("SonMotherWorkProc is childcard!!!\n");
				if(status == GetMotherInfo)
				{
					dxreport("SonMotherWorkProc status is GetMotherInfo!!!\n");
					// 提示:获取母卡信息失败，请插入要配对的母卡
					bReqireRemoveCard = true; // 请求拔卡
					DvtCAShowMotherCardPair(CARDPAIR_GET_MOTHERINFO_FAILED);
				}
				else  // setMotherInfo
				{
					dxreport("SonMotherWorkProc status is SetMotherInfo!!!\n");
					// 提示:获取母卡信息失败，请插入要配对的母卡
					if(DVTCASTB_SetCorrespondInfo(dataLen, dataBuf) == DVTCA_OK)
					{
						dxreport("SonMotherWorkProc SetCorresponInfo ok!!!\n");
						bReqireRemoveCard = true;
						// 恭喜，配对成功，请插入其他要配对的子卡
						DvtCAShowMotherCardPair(CARDPAIR_PAIR_SUCCEED);
					}
					else
					{
						dxreport("SonMotherWorkProc SetCorresponInfo failed!!!\n");
						bReqireRemoveCard = true;
						//配对失败，请插入要配对的子卡
						DvtCAShowMotherCardPair(CARDPAIR_PAIR_FAILED);
					}
					
				}
			}
		}
				
	}
	dxreport("Leave SonMotherWorkProc\n");
	return 0;
}
#endif

