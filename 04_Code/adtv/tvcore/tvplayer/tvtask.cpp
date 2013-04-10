#include "tvtask.h"

namespace TVCoreUtil{

void TSFilter::DemuxCallBack( utHandle hFilter,long iTuner,utPid pid,utTid tid,utByte *data,long datasize,utContext context)
{
	//LOGTRACE(LOGINFO,"TSFilter::DemuxCallBack:data:%x, datasize:%d, context:0x%x\n",data,datasize,context);
	
	TSFilter *pTSFilter = static_cast<TSFilter*>(context);
	pTSFilter->on_section_got(pid,data,datasize);
}

void TSFilter::DelSectionFilterCallBack(utHandle hFilter,utContext context)
{
	//LOGTRACE(LOGINFO,"TSFilter::DelSectionFilterCallBack: context:0x%x.\n",context);
	
	TSFilter *pTSFilter = static_cast<TSFilter*>(context);
	pTSFilter->SetCanDelete(true);
}

bool TSFilter::on_section_got(const U16 pid,const U8 *buffer, U32 size )
{
	if(NULL != m_pTask){
		m_pTask->put_data(pid,buffer,size);
	}

	return true;
}

void TSFilter::stop_search()
{
	LOGTRACE(LOGINFO,"Enter stop_search.\n");

	if(m_hFilter)
	{
		m_bCanDelete = false;
		tsdemux_delSectionFilter(m_hFilter,DelSectionFilterCallBack);
	}

	LOGTRACE(LOGINFO,"Leave stop_search.\n");
}

bool TVTask::put_data(const U16 pid,const U8* buffer,U32 size)
{
	bool bRet = false;
	if(NULL != m_pTaskMgr)
	{
		bRet = m_pTaskMgr->push(this,pid,buffer,size);
	}

	return bRet ;
}

}
