#ifndef NTF_Y4_BLOCK_H_
#define NTF_Y4_BLOCK_H_

#include <stdlib.h>

#ifdef __cplusplus
extern "C"
{
#endif

typedef struct _BlkAlloc
{ 
	unsigned long MaxBlkCount;
	unsigned long BlkSize;
	unsigned char *buf;
	unsigned char **stack;
	unsigned long stackpt;
} BlkAlloc;


typedef void * HBLKALLOC;

__inline void BkReset( HBLKALLOC hBlk)
{
	unsigned long i=0;	
	BlkAlloc *pPD=(BlkAlloc *)hBlk;
	for(i=0; i<pPD->MaxBlkCount; i++)
		pPD->stack[i]=pPD->buf+i*pPD->BlkSize;
	pPD->stackpt=pPD->MaxBlkCount;
}

__inline HBLKALLOC BkInit( unsigned long MaxBlkCount, unsigned long BlkSize )
{
	if(MaxBlkCount>0 && BlkSize>0)
	{
		BlkAlloc *pPD=0;
		pPD = (BlkAlloc *)malloc(sizeof(BlkAlloc));

		if(pPD==0)
			return 0;
		pPD->buf=(unsigned char *)malloc(MaxBlkCount*BlkSize);
		pPD->stack=(unsigned char **)malloc(MaxBlkCount*sizeof(unsigned char *));

		pPD->BlkSize=BlkSize;
		pPD->MaxBlkCount=MaxBlkCount;
		BkReset(pPD);
		return pPD;
	}
	return 0;
} 


__inline void BkUninit(HBLKALLOC hBlk)
{
	BlkAlloc *pPD=(BlkAlloc *)hBlk;
	if(pPD)
	{
		if(pPD->MaxBlkCount>0)
		{
			free(pPD->buf);
			free(pPD->stack);
		}
		free(pPD);
	}
}

__inline unsigned char *BkAlloc(HBLKALLOC hBlk)
{ 
	BlkAlloc *pPD=(BlkAlloc *)hBlk;
	if(pPD->stackpt>0)
	{
		pPD->stackpt=pPD->stackpt-1;
		return pPD->stack[pPD->stackpt];
	} 
	return 0;
} 

__inline void BkFree(HBLKALLOC hBlk, unsigned char *p)
{ 
	BlkAlloc *pPD=(BlkAlloc *)hBlk;
	pPD->stack[pPD->stackpt]=p;
	pPD->stackpt=pPD->stackpt+1;
}

#ifdef __cplusplus
}
#endif
#endif


