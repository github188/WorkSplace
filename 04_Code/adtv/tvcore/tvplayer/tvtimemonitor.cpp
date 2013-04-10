#include "tvtimemonitor.h"
#include "ucsconvert.h"
#include "datetime.h"
#include <all.h>
#include <imp/pre.h>

#define  LOG_TAG  "libtvplayer"
#include "tvlog.h"

namespace PSISI = novelsuper::psisi_parse;


//{{
static bool gbDyncTrace = false;
#define DYNAMICTRACEUNINIT	\
{							\
	gbDyncTrace = false;	\
}

#define DYNAMICTRACEINIT	\
{							\
	int ret = access("/data/data/novel.supertv.dvb/timetrace.log",F_OK)	\
	gbDyncTrace = (ret == 0) ? true : false;							\
}

#define DYNAMICTRACE(...)		\
{								\
	if(gbDyncTrace)				\
		LOGTRACE(__VA_ARGS__);	\
}	
//}}




namespace TVCoreUtil{

bool TimeMonitorTask::BuildFilter()
{
	LOGTRACE(LOGINFO,"Enter TimeMonitorTask::BuildFilter.\n");
	bool ret = true;

	utSectionFilter filter[2];
	memset((U8*)filter,0,sizeof(filter));

	filter[0].iTuner = 0;
	filter[0].pid = TDT_PID;
	filter[0].tid = NO_TABLE_ID;
	filter[0].timeout = 0xffffffff;
	filter[0].filterData[0] = TableId_Tdt;
	filter[0].filterMask[0] = 0xff;
	
	filter[1].iTuner = 0;
	filter[1].pid = TDT_PID;
	filter[1].tid = NO_TABLE_ID;
	filter[1].timeout = 0xffffffff;
	filter[1].filterData[0] = TableId_Tot;
	filter[1].filterMask[0] = 0xff;

	m_pTSFilter = new TSFilter(this, filter, 2);
	m_pTSFilter->start_search();
	m_iMaxTimeout = 0xFFFFFFFF;

	LOGTRACE(LOGINFO,"Leave TimeMonitorTask::BuildFilter.\n");
	
	return ret;
}

void TimeMonitorTask::ClearFilter()
{
	LOGTRACE(LOGINFO,"Enter TimeMonitorTask::ClearFilter.\n");
	
	if(0 != m_pTSFilter)
	{
		m_pTSFilter->stop_search();
		NS_sleep(5);
		delete m_pTSFilter;
		m_pTSFilter = 0;
	}

	LOGTRACE(LOGINFO,"Leave TimeMonitorTask::ClearFilter\n");
}

bool TimeMonitorTask::task_start()
{
	BuildFilter();
	
	m_iStarttime = NS_GetTickCount();

	return true;
}

bool TimeMonitorTask::task_cancel()
{
	ClearFilter();
	return true;
}

bool TimeMonitorTask::task_process(const U16 pid, U8 const *buffer, U32 size)
{
	bool bRet = false;
	if(size < 8 || 0 == buffer)
	{
		LOGTRACE(LOGINFO,"TimeMonitorTask::task_process %d\n",size);
		return bRet;
	}
	
	PSISI::TdtSection * pTdtSec = (PSISI::TdtSection*)buffer;
	if(TableId_Tot == pTdtSec->table_id()) //TOT
	{
		// do tot
		PSISI::TotSection * pTotSec = (PSISI::TotSection*)buffer;
		U32 uCRCValue = PSISI::n2h32(pTotSec->CRC_32());
		if(m_uCRCValue == uCRCValue){
			return bRet;
		}
		else{
			m_uCRCValue = uCRCValue;
		}
		
		PSISI::DescList_Iterator<PSISI::DescLocalTimeOffset> it = pTotSec->begin<PSISI::DescLocalTimeOffset>();
		if(!it.empty())
		{
			novelsuper::psisi_parse::DescLocalTimeOffset::Loop_Iterator localIt = it->loop_begin();
			for(; localIt != it->loop_end(); ++localIt)
			{
				std::string name = localIt->country_code();
				U8 country_id = localIt->country_region_id();
				U16 offset = localIt->local_time_offset();
				StbTime st(localIt->time_of_change().utctime);
				
//				LOGTRACE(LOGINFO,"Recv tot:%d/%d/%d,%d:%d:%d %x,%d\n",st.year(),st.month(),
//					st.day(),st.hour(),st.minute(),st.second(), offset,country_id);
				SetCurrentTSTime(st.make_time(),offset);
			}
		}
		else
		{
			StbTime st(pTotSec->utc_time().utctime);
			SetCurrentTSTime(st.make_time());
        }
	}
	else //do tdt
	{
		StbTime st(pTdtSec->utc_time().utctime);
		SetCurrentTSTime(st.make_time());
//		LOGTRACE(LOGINFO,"Recv tdt:%d/%d/%d,%d:%d:%d\n",st.year(),st.month(),
//			st.day(),st.hour(),st.minute(),st.second());
	}
	
	return false;
}

bool TimeMonitorTask::task_complete(bool bIsTimeout/* = false */)
{
	LOGTRACE(LOGINFO,"TimeMonitorTask::task_complete\n");
	ClearFilter();

	return true;
}

}//TVCoreUtil
