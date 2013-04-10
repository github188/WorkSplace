#include "typ.h"
#include "baseSearch.h"
#define LOG_TAG "tvsearch"
#define LOG_LEVEL LOG_TRACE
#include "dxreport.h"

#define SINGLE_FREQ_TIMEOUT_MAX (15000)
BaseSearcher::BaseSearcher(utSectionFilter const &filter,BaseSearchMgr *pSearchMgr)
{
	dxreport("BaseSearcher:BaseSearcher pid=0x%x, tid=%d,timeout=%d.BaseSearchMgr:%p\n",filter.pid,filter.tid,filter.timeout,pSearchMgr);
	utSecfilter_ =filter;
	m_pBaseSearchMgr=pSearchMgr;
	AddSectionFilter();
}
void BaseSearcher::AddSectionFilter()
{
	hFilter_=tsdemux_addSectionFilter(&utSecfilter_,1,DemuxCallBack,(void **)this);
}

void BaseSearcher::DemuxCallBack( utHandle hFilter,long iTuner,utPid pid,utTid tid,utByte *data,long datasize,utContext context)
{
	dxreport("BaseSearcher::DemuxCallBack:pid:0x%x, tid:%d, data:%p, datasize:%d, context:0x%x\n",pid,tid,data,datasize,context);
	BaseSearcher *pBaseSearcher = (BaseSearcher *)context;
	pBaseSearcher->SetSection(data,datasize);
}
bool BaseSearcher::SetSection( U8 const* data, U32 size )
{
	if(m_pBaseSearchMgr != NULL)
	{
		m_pBaseSearchMgr->SetSection(this,utSecfilter_.pid,utSecfilter_.tid,data,size);
	}
	
	return true;
}
void BaseSearcher::DelSectionFilter()
{
	if(hFilter_)
	{
//		dxreport("DelSectionFilter...\n");
		m_bCanDelete = false;
		tsdemux_delSectionFilter(hFilter_,NULL/*DelSectionFilterCallBack*/);
/*		
		int loop=0;
		while(!m_bCanDelete)
		{
			NS_sleep(5);
			loop++;
			if(loop > 1000)
			{
				dxreport("error:DelSectionFilter wait CallBack failed...\n");
				break;
			}
		}
*/		
		hFilter_=NULL;
	}
}
void BaseSearcher::DelSectionFilterCallBack( utHandle hFilter,utContext context)
{
//	dxreport("BaseSearcher::DelSectionFilterCallBack: context:0x%x\n",context);
	BaseSearcher *pSearch = (BaseSearcher *)context;
	pSearch->SetCanDelete(true);
}

