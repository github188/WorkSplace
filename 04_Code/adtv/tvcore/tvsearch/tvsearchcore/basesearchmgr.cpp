#include "typ.h"
#include "basesearchmgr.h"
#include "tvsearchlocal.h"

#define LOG_TAG "tvsearch"
#define LOG_LEVEL LOG_TRACE
#include "dxreport.h"
ULONG BaseSearchMgr::SetTuningParam(const TuningParam TuninfParam)
{
	dxreport("BaseSearchMgr::SetTuningParam Begin >>>(freq=%d,synb=%d,qam=%d).\n",
		TuninfParam.freq,TuninfParam.symb,TuninfParam.qam);
	change_tuning_param(TuninfParam); // 单频率或多频率
	dxreport("BaseSearchMgr::SetTuningParam End <<<\n");
	return  0;
}
DWORD BaseSearchMgr::WaitForDataReady()
{
	
	DWORD dwRet = WAIT_OBJECT_0;
	if(m_dequePacket.empty())
	{
		dwRet = NS_WaitEvent(m_hDataArrive,500);
		switch(dwRet)
		{
			case WAIT_OBJECT_0:
				break;
			case WAIT_TIMEOUT:
				dxreport("BaseSearchMgr::WaitForDataReady m_hDataArrive wait timeout\n");
				break;
			default:
				{
					dxreport("BaseSearchMgr::WaitForDataReady m_hDataArrive wait error !!!\n");
					//		 	DWORD err=GetLastError();
					dxreport("dwRet:%d\n",dwRet);
				}
		}
	}
	return	dwRet;
}
void BaseSearchMgr::SetSection(DVBSection *pSection)
{
	AutoLockT lock_it(m_dequeMutex);
	if(NULL != pSection)
	{
		m_dequePacket.push_back(pSection);
		NotifyDataArrived();
	}
}

void BaseSearchMgr::SetSection(BaseSearcher* pBaseSearcher,U16 pid,U8 table_id,const U8* pBuffer,U32 iSize)
{
	DVBSection *pSection = new DVBSection(pBaseSearcher,pid,table_id,pBuffer,iSize);
	SetSection(pSection);
}

DVBSection* BaseSearchMgr::GetSection()
{
	AutoLockT lock_it(m_dequeMutex);
	DVBSection *pSection = NULL;
	if(!m_dequePacket.empty())
	{
		pSection = m_dequePacket.front();
		m_dequePacket.pop_front();
	}
	return pSection ; 
}

void BaseSearchMgr::FreeSection(DVBSection* pSection)
{
	if(NULL != pSection) 
	{
		delete pSection;
	}
}

void BaseSearchMgr::RemoveSection(U16 pid, U8 tableID)
{
	AutoLockT lock_it(m_dequeMutex);
	
	DVBSection *pSection = NULL;
	for(U32 iCount = 0 ; iCount < m_dequePacket.size();iCount++)
	{
		pSection = m_dequePacket[iCount];
		FreeSection(pSection);
		m_dequePacket.erase(m_dequePacket.begin()+iCount);
	}
}

// 清除队列
void BaseSearchMgr::ClearSectionDeque()
{
	AutoLockT lock_it(m_dequeMutex);
	
	DVBSection *pSection = NULL;
	for(U32 iCount = 0 ; iCount < m_dequePacket.size();iCount++)
	{
		pSection = m_dequePacket[iCount];
		FreeSection(pSection);
	}
	m_dequePacket.clear();
}

//bool BaseSearchMgr::IsEmptyDeque()
//{
//	return m_dequePacket.empty();
//}
// U8 BaseSearchMgr::IsTimeOut()
// {
// 	return m_bOnTimeout ;
// }
// 
// void BaseSearchMgr::SetTimeOut(const U8 bTimeOut)
// {
// 	m_bOnTimeout = bTimeOut;
// }
