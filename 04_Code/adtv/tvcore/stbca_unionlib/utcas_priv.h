#ifndef _UTCAS_PRIV_H
#define _UTCAS_PRIV_H
#include "utcasa.h"

#define ECM_REQ_ID_MIN   0
#define ECM_REQ_ID_MAX   UTCA_MAXNUM_ECM
#define EMM_REQ_ID_MIN   UTCA_MAXNUM_ECM + 10
#define EMM_REQ_ID_MAX   UTCA_MAXNUM_ECM + 100
#define OTHER_REQ_ID_MIN UTCA_MAXNUM_ECM + 110
#define OTHER_REQ_ID_MAX UTCA_MAXNUM_ECM + 200


typedef struct {
	UTCA_U8 TS;
	UTCA_U8 T0;
	UTCA_U8 TA[4];
	UTCA_U8 TB[4];
	UTCA_U8 TC[4];
	UTCA_U8 TD[4];
	UTCA_U8 historical[16];
	UTCA_U8 TCK;
	UTCA_U8 historicalLength;
}ATR_S;

typedef struct {
	UTCA_U16  pid;
	UTCA_U8   masks[8];
	UTCA_U8   fliters[8];
	UTCA_U8   reqID;
}ECMFliter_Info;

#define PRIVATE_DATA_LENGTH 4096
typedef struct
{
	UTCA_U8   byReqID;
	UTCA_BOOL bTimeout; // 过滤器值
	UTCA_U16  wPid;
	UTCA_U16  wLen;
	UTCA_U8   buf[PRIVATE_DATA_LENGTH];
}PRIV_DATA;

// 私有函数
void HandleEcmRequest(int ecmCount);
void freeEcmFilters();
static void msgTask();
static void ecmTask();
static void emmTask();

void CreateEcmTask(UTCA_U8 byThreadPrior);
void CreateEmmTask(UTCA_U8 byThreadPrior);
void KillEcmTask();
void KillEmmTask();
static UTCA_BOOL parseATR(UTCA_U8 *pbyBuf, UTCA_U8 byLen, void *pAtr);


#endif
