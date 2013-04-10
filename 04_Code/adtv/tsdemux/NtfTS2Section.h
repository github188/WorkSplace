#ifndef NTF_Y4_CA_SECTION2_FORPCDRIVER_H_
#define NTF_Y4_CA_SECTION2_FORPCDRIVER_H_

#include "blkalloc.h"
#include "tshelp.h"

#ifdef __cplusplus
extern "C"
{
#endif


#define MAX_SECTION_COUNT	64
#define MAX_SECTION_LENGTH	4096
#define SEC_HEADER_SIZE		3

typedef struct _PIDInf
{	
	unsigned char IsTsPid;		// 不需要拼接的数据包
	unsigned char IsSecPid;
	short cc;					// continue counter
	unsigned char *SecData;		// Section data buffer
	unsigned short SecDataLen;	// Section data length
} PIDInf;

typedef void (*SecDataHandler)(void *context,unsigned short pid,unsigned char *SecData,unsigned short SecDataLen);
typedef void (*TSDataHandler)(void *context,unsigned short pid,unsigned char *pTSBuffer,unsigned short iTSBuffer);

typedef struct _TSInf
{
	SecDataHandler OnSecData;
	TSDataHandler  OnTSData;
	HBLKALLOC SecAlloc;
	void * Context;
	PIDInf PidInf[MAX_PID_COUNT];
} TSInf;


/////////////// TS to Section /////////////////////////////


//// public ////////////////////

void TSInf_Init(TSInf *pTsInf,void * PContext);

void TSInf_Uninit(TSInf *pTsInf);

void TSInf_Reset(TSInf *pTsInf);

void TSInf_ClearAllPrivateSecPid(TSInf *pTsInf);
void TSInf_ClearPrivateSecPid(TSInf *pTsInf,unsigned short pid);

__inline void TSInf_refSecPid(TSInf *pTsInf,unsigned long SecPid)
{
	if(pTsInf && SecPid<MAX_PID_COUNT)
		pTsInf->PidInf[SecPid].IsSecPid++;
}

__inline void TSInf_releaseSecPid(TSInf *pTsInf,unsigned long SecPid)
{
	if(pTsInf && SecPid<MAX_PID_COUNT)
		pTsInf->PidInf[SecPid].IsSecPid--;
}

__inline void TSInf_refTsPid(TSInf *pTsInf,unsigned long TsPid)
{
	if(pTsInf && TsPid<MAX_PID_COUNT)
		pTsInf->PidInf[TsPid].IsTsPid++;
}

__inline void TSInf_releaseTsPid(TSInf *pTsInf,unsigned long TsPid)
{
	if(pTsInf && TsPid<MAX_PID_COUNT)
		pTsInf->PidInf[TsPid].IsTsPid--;
}

void TSInf_SetSecDataHandler(TSInf *pTsInf,SecDataHandler h);
void TSInf_SetTSDataHandler(TSInf *pTsInf,TSDataHandler h);

void TSInf_PutTsPactet(TSInf *pTsInf,unsigned char *pk,unsigned long pkCount);
///////////////////////////////////////////////////////////////////////


#ifdef __cplusplus
}
#endif
#endif


