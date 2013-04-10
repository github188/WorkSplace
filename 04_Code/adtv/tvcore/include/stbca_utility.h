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
// �궨��
////////////////////////////////////////////////////////////////////////////////

#define FILTER_BYTE_SIZE		8
#define MAX_PRIVATEDATA_SIZE	4096
//#define MAX_SEND_ECMSGQUEUE_SIZE		2
// Ϊ�˷�ֹ��Ƶ����̨ʱ��ecm ��������������������Ч
// ecm ���ݶ����û���Ϊ5
#define MAX_SEND_ECMSGQUEUE_SIZE		5
#define MAX_SEND_EMMSGQUEUE_SIZE		128
// #define WriteBuffer_BLOCK_SIZE 64000
#define WriteBuffer_BLOCK_SIZE 65536

////////////////////////////////////////////////////////////////////////////////
// ��������
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
		CasType casType_;	// Ŀǰ��TF, SM, GH ����
		U16  emmpid_;
		bool bSCardStatus_;
		int saveDescramCount_;
		HANDLE hTVDevice_;
		const char *fileName_;
		Descrambling saveDescrambs_[8];
		bool	bStbCaHasInited_;
		TVNOTIFY		pCaMsgCallBack_;
		BlockRing<SendToCAMsg,MAX_SEND_ECMSGQUEUE_SIZE> *pEcmSendCaMsgRing_;// �ռ�STB ����CA  ��ecm ��Ϣ
		BlockRing<SendToCAMsg,MAX_SEND_EMMSGQUEUE_SIZE> *pEmmSendCaMsgRing_;// �ռ�STB ����CA  emm  ��Ϣ
		
		map<HANDLE,RegIDPID> mapCaLibControl_hFilter_ReqIDPID_;			// ��calib ���Ƶ�map, �����о�ҲҪ�Լ�����
		map<HANDLE,RegIDPID> mapMyControl_hFilter_ReqIDPID_;		// ���Լ������map
		MutexT mutexCalibControl_;	// ���Ᵽ��mapCaLibControl_hFilter_ReqIDPID_
		MutexT mutexMyControl_;		// // ���Ᵽ��mapMyControl_hFilter_ReqIDPID_
		MutexT mutexCheckCardStatus_; // ���Ᵽ������CheckCardStatus;
		simplethread  	caThread_;
		bool enableEmm_;
		simplethread  	SonMotherThread_;		//��ĸ���Զ����(dvtca),��Ƶ����������(dvtca)
		bool ignoreCAMsg;						//��Ƶ����������ʱ,��Ҫ������Ϣ(dvtca)
//		simplethread  	areaLockThread_;

		U16 serviceID_;		//���ڲ��ŵ�Ƶ����
	public:
		ObjStbCa();
		~ObjStbCa();

};
////////////////////////////////////////////////////////////////////////////////
// ����ԭ��
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
