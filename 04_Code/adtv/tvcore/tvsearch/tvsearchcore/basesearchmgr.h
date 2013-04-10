
#ifndef __BASESEARCHMGR_H__
#define __BASESEARCHMGR_H__
#include "typ.h"
#include "isearchtvnotify.h"
#include <deque>
#include "xprocess.h"
#include "stbruntime.h"

struct BaseSearcher;

// DVBSection
struct DVBSection
{
	DVBSection(BaseSearcher* pSearcher,U16 pid,U8 table_id,const U8* pData,U32 iLen)
	{
		m_pSearcher = pSearcher;
		pid_ = pid;
		table_id_ = table_id;
		dataLen_ = iLen;
		data_ = new U8[dataLen_];
		memcpy(data_,pData,dataLen_);
	}

	virtual ~DVBSection()
	{
		if(NULL != data_) {
			delete[] data_;
			data_ = 0;
		}
	}

	U16 pid_;
	U8  table_id_;
	U8 *data_;
	U32 dataLen_;
	BaseSearcher* m_pSearcher;
};


class BaseSearchMgr{
public:
	BaseSearchMgr()
	{
		m_hDataArrive   = NS_CreateEvent(NULL,FALSE,FALSE,NULL);
	}
	virtual ~BaseSearchMgr(){
		// 清除队列
		ClearSectionDeque();
		if(NULL != m_hDataArrive) NS_DeleteEvent(m_hDataArrive);
	}

	void NotifyDataArrived()
	{
		NS_SetEvent(m_hDataArrive);
	}
	virtual ULONG SetTuningParam(const TuningParam TuninfParam);
	DWORD WaitForDataReady();
	void RemoveSection(U16 pid, U8 tableID);
	virtual void   ClearSectionDeque();
	void SetSection(DVBSection *pSection);
	void SetSection(BaseSearcher* pSearcher,U16 pid,U8 table_id,const U8* pBuffer,U32 iSize);
	DVBSection* GetSection();
	void FreeSection(DVBSection* pSection);

protected:
	HANDLE                  m_hDataArrive;

	//// 数据队列
	MutexT					m_dequeMutex;
	std::deque<DVBSection*> m_dequePacket;
	
};
#endif
