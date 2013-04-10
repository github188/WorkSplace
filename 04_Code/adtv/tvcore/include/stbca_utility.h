#ifndef __STBCA_UTILITY_
#define __STBCA_UTILITY_
#include "typ.h"
#include "cdcas_hdr.h"
#include "tsdemux.h"
#include "tvdevice.h"
#include "capture_def.h"
#include "blockring.h"
#include "cdcalib4stb.h"
#include "capture.h"
#include "simplethread.h"
#include "xprocess.h"

////////////////////////////////////////////////////////////////////////////////
// 宏定义
////////////////////////////////////////////////////////////////////////////////

#define FILTER_BYTE_SIZE		8
#define MAX_PRIVATEDATA_SIZE	4096
//#define MAX_SEND_ECMSGQUEUE_SIZE		2
// 为了防止在频繁换台时，ecm 缓冲溢出并凑巧清除了有效
// ecm 数据而设置缓冲为5
#define MAX_SEND_ECMSGQUEUE_SIZE		5
#define MAX_SEND_EMMSGQUEUE_SIZE		128
// #define WriteBuffer_BLOCK_SIZE 64000
#define WriteBuffer_BLOCK_SIZE 65536

////////////////////////////////////////////////////////////////////////////////
// 类型声明
////////////////////////////////////////////////////////////////////////////////

typedef struct _CASetEmmPid
{
	WORD EmmPid;
} CASetEmmPid;
typedef struct _CASetEcmPid
{
	BYTE byType;/*type*/
	SCDCASServiceInfo ServiceInfo;/*service infomation*/
} CASetEcmPid;
typedef struct _CAPrivateDataGot
{
	BYTE ReqID;/*request id*/
	WORD pid;
	BYTE bTimeOut;/*return status*/
	WORD RecvDataLen;
	BYTE RecvData[MAX_PRIVATEDATA_SIZE];
} CAPrivateDataGot;
typedef enum _MsgType
{
	CAMT_SET_ECMPID,
		CAMT_SET_EMMPID,
		CAMT_PRIVATEDATA_GOT
}MsgType;
struct RecvData
{
	int len;
	char data[1024];
	RecvData()
	{
		len=0;
		memset(data,0,1024);
	}
	void ClearData()
	{
		memset(data,0,1024);
	}
};


typedef struct _SendToCAMsg
{
	DWORD type; // message type
	union
	{
		// STB to CA
		CASetEcmPid SetEcmPid_;
		CASetEmmPid SetEmmPid_;
		CAPrivateDataGot PrivateDataGot_;
	};
} SendToCAMsg;
class ObjStbCa{
	public:	
		CasType casType_;	// 目前有TF, SM, GH 三种
		U16  emmpid_;
		bool bSCardStatus_;
		int saveDescramCount_;
		HANDLE hTVDevice_;
		const char *fileName_;
		Descrambling saveDescrambs_[8];
		bool	bStbCaHasInited_;
		TVNOTIFY		pCaMsgCallBack_;
		BlockRing<SendToCAMsg,MAX_SEND_ECMSGQUEUE_SIZE> *pEcmSendCaMsgRing_;// 收集STB 发往CA  的ecm 消息
		BlockRing<SendToCAMsg,MAX_SEND_EMMSGQUEUE_SIZE> *pEmmSendCaMsgRing_;// 收集STB 发往CA  emm  消息
		
		map<HANDLE,RegIDPID> mapCaLibControl_hFilter_ReqIDPID_;			// 受calib 控制的map, 不过感觉也要自己管理
		map<HANDLE,RegIDPID> mapMyControl_hFilter_ReqIDPID_;		// 由自己管理的map
		MutexT mutexCalibControl_;	// 互斥保护mapCaLibControl_hFilter_ReqIDPID_
		MutexT mutexMyControl_;		// // 互斥保护mapMyControl_hFilter_ReqIDPID_
		MutexT mutexCheckCardStatus_; // 互斥保护调用CheckCardStatus;
		simplethread  	caThread_;
		bool enableEmm_;
		simplethread  	SonMotherThread_;		//子母卡自动配对(dvtca),单频点区域锁定(dvtca)
		bool ignoreCAMsg;						//单频点区域锁定时,需要忽略消息(dvtca)
//		simplethread  	areaLockThread_;

		U16 serviceID_;		//正在播放的频道号
	public:
		ObjStbCa();
		~ObjStbCa();

};
////////////////////////////////////////////////////////////////////////////////
// 函数原型
////////////////////////////////////////////////////////////////////////////////
#ifndef WIN32
#define __stdcall
#endif
void InitFlashFile();
char getHex(BYTE v);
char *PrintHex(const BYTE *pBuf,USHORT BufSize);
void CloseGMapDemuxFilter(map<HANDLE,RegIDPID>& map_hFilter_ReqIDPID,CDCA_U8  byReqID,	CDCA_U16 wPid);
void CloseGMapDemuxFilter_Lock(map<HANDLE,RegIDPID>& map_hFilter_ReqIDPID,CDCA_U8  byReqID,	CDCA_U16 wPid, MutexT& mutex);
void CloseGMapDemuxFilters(map<HANDLE,RegIDPID>& map_hFilter_ReqIDPID);
void CloseGMapDemuxFilters_Lock(map<HANDLE,RegIDPID>& map_hFilter_ReqIDPID, MutexT &mutex);
void SetCW(DVBCW *pDVBcw,int cwCount);
void CheckSCardStatus();
DWORD STB_RecvFromCaMsg(LPVOID MsgBuf, DWORD MsgBufSize);
void STB_SendDataToCaLib();
unsigned int __stdcall CAWorkProc(PVOID param);
bool  BUFEcmCASTB_PrivateDataGot( CDCA_U8 byReqID, CDCA_BOOL bTimeout, CDCA_U16 wPid, const CDCA_U8* pbyData,CDCA_U16 wLen);
bool  BUFEmmCASTB_PrivateDataGot( CDCA_U8 byReqID, CDCA_BOOL bTimeout, CDCA_U16 wPid, const CDCA_U8* pbyData,CDCA_U16 wLen);
void OncePrivateDataGotCallBack( utHandle hFilter,long iTuner,utPid pid,utTid tid,utByte *data,long datasize,utContext context);
void ConPrivateDataGotCallBack( utHandle hFilter,long iTuner,utPid pid,utTid tid,utByte *data,long datasize,utContext context);
void ClearCW(void);

unsigned int __stdcall SonMotherWorkProc(PVOID param);

#endif
