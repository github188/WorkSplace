#ifndef _NOVERSUPEL_TIMEMONITOR_TASK_H_
#define _NOVERSUPEL_TIMEMONITOR_TASK_H_

#include "tvtask.h"
#include <tvcomm.h>

namespace TVCoreUtil{

struct TimeMonitorTask : public TVTask
{
public:
	TimeMonitorTask(TVTaskMgr* pTaskMgr)
		:TVTask(pTaskMgr)
	{
		m_uCRCValue = 0;
		m_pTSFilter = 0;
		m_iTSTime = 0;
		m_iTSOffset = 0;		
		task_start();
	}
	
	virtual ~TimeMonitorTask(){
		LOGTRACE(LOGINFO,"~TimeMonitorTask...");
	};

	virtual bool task_start();
	virtual bool task_cancel();
	virtual bool task_process(const U16 pid,U8 const* buffer, U32 size);
	virtual bool task_complete(bool bIsTimeout = false);
	virtual bool task_timeout(const U16 pid,const U8 table_id){
		LOGTRACE(LOGINFO,"task_timout(%d,%d)\n",pid,table_id);
		return true;
	}
	virtual U32	GetStartTime(){
		return m_iStarttime;
	}
	virtual U32 GetUsableTime(){
		return m_iMaxTimeout;
	}

	bool GetCurrentTSTime(U32& time){
		time = m_iTSTime ; 
		return true;
	}
	bool GetCurrentTSTime(U32& time,U32& offset){
		time = m_iTSTime;
		offset = m_iTSOffset;
		return true;
	}

private:
	void SetCurrentTSTime(const U32 time)
	{
		m_iTSTime = time;
	}
	void SetCurrentTSTime(const U32 time,const U32 offset)
	{
		m_iTSTime = time;
		m_iTSOffset = offset;
	}
	virtual bool	BuildFilter();
	virtual void	ClearFilter();
	virtual void	ReleaseFilter(U16 pid,U8 table_id){}

	U32     m_uCRCValue;
	U32		m_iStarttime;
	U32		m_iMaxTimeout;
	U32		m_iTSTime;
	U32		m_iTSOffset;		
	TSFilter	*m_pTSFilter;
};
}//TVCoreUtil

#endif//_NOVERSUPEL_TIMEMONITOR_TASK_H_

