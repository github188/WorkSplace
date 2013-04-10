#include "TsCatcher.h"
#include "tvdevice.h"

#define	 LOG_TAG "libtsdemux"
#include "tvlog.h"

using namespace NovelSupertv::stbruntime;

TsCatcher::TsCatcher(void)
: Thread("TsCatcherThread",64*1024)
, device_(0)
, enableTs_(false)
{
	// open tuner device
	device_ = tvdevice_open();

	// allocate resource.
	TSInf_Init(&TsInf_,this);
	TSInf_SetSecDataHandler(&TsInf_,onSecDataHandler);
	TSInf_SetTSDataHandler(&TsInf_,onTSDataHandler);
}

TsCatcher::~TsCatcher(void)
{
	LOGTRACE(LOGINFO,"Enter ~TsCatcher().\n");

	enableTs(false);

	TSInf_Uninit(&TsInf_);
	// close tuner device.
	tvdevice_close(device_);

	LOGTRACE(LOGINFO,"Leave ~TsCatcher.\n");
}

utHandle TsCatcher::addSectionFilter(
	utSectionFilter const *filter,
	long filterCount,
	utFilterDataCallback callback,
	utContext *context
)
{
	AutoLockT lock(secMutex_);

	SectionFilter *pSectionFilter = 0;
	for(int i=0; i<MAX_SECFILTER_COUNT; i++)
	{
		if(!SecFilterList_[i].valid_)
		{
			pSectionFilter = SecFilterList_+i;
			break;
		}
	}
	if(pSectionFilter)
	{
		pSectionFilter->handle_ =callback;
		pSectionFilter->context_ = context;
		pSectionFilter->destoryCallback_= 0;
		pSectionFilter->bTSOrSection = false;
		pSectionFilter->filters_.clear();
		for(long i=0; i<filterCount; i++)
		{
			SectionFilterItem item;
			item.filter=filter[i];
			item.enable=true;
			pSectionFilter->filters_.push_back(item);
		}
		
		pSectionFilter->beginTick_= NS_GetTickCount();
		
		for(unsigned int i=0; i<pSectionFilter->filters_.size(); i++)
			TSInf_refSecPid(&TsInf_,pSectionFilter->filters_[i].filter.pid);
		pSectionFilter->valid_ = true;
	}

	LOGTRACE(LOGDEBUG,"[UsbTV] addSectionFilter return %p\n",pSectionFilter);
	
	return pSectionFilter;
}

void TsCatcher::delSectionFilter(
	utHandle hFilter,
	utFilterDestoryNotify destoryCallback)
{
	LOGTRACE(LOGDEBUG,"[UsbTV] delSectionFilter(%p)\n",(unsigned long)hFilter);

	if(!is_curthread())
	{
		AutoLockT lock(secMutex_);
		inner_delSectionFilter(hFilter);
	}
	else{
		inner_delSectionFilter(hFilter);
	}
}

void TsCatcher::inner_delSectionFilter(utHandle hFilter)
{
	if(hFilter)
	{
		SectionFilter* filter = (SectionFilter*)hFilter;
		if(filter->valid_)
		{
			for(unsigned int i=0; i<filter->filters_.size(); i++)
				TSInf_releaseSecPid(&TsInf_,filter->filters_[i].filter.pid);
			filter->filters_.clear();
			filter->valid_=false;
		}
	}
}

// TS 分组包过滤接口
utHandle TsCatcher::addTsPacketFilter(
						   utTsDataFilter const *filter,
						   long filterCount,
						   utFilterDataCallback callback,
						   utContext *context)
{
	// utTsDataFilter ==>utSectionFilter
	if(0 == filter || 0 == filterCount)
		return 0 ;

	AutoLockT lock(tsMutex_);
	SectionFilter *pSectionFilter = 0;
	for(int i=0; i<MAX_TSFILTER_COUNT; i++)
	{
		if(!TSDataFilterList_[i].valid_)
		{
			pSectionFilter = TSDataFilterList_+i;
			break;
		}
	}
	if(pSectionFilter)
	{
		pSectionFilter->handle_ =callback;
		pSectionFilter->context_ = context;
		pSectionFilter->destoryCallback_= 0;
		pSectionFilter->bTSOrSection = true;
		pSectionFilter->filters_.clear();
		for(long i=0; i<filterCount; i++)
		{
			SectionFilterItem item;
			item.filter.iTuner	= filter[i].iTuner;
			item.filter.pid		= filter[i].pid;
			item.filter.tid		= 0;
			item.filter.timeout	= 0xffffffff;
			item.enable=true;
			pSectionFilter->filters_.push_back(item);
		}
		
		pSectionFilter->beginTick_= NS_GetTickCount();
		
		for(unsigned int i=0; i<pSectionFilter->filters_.size(); i++)
			TSInf_refTsPid(&TsInf_,pSectionFilter->filters_[i].filter.pid);
		pSectionFilter->valid_ = true;
	}

	LOGTRACE(LOGDEBUG,"[UsbTV] addTsPacketFilter return %p\n",pSectionFilter);

	return pSectionFilter;
}

void TsCatcher::delTsPacketFilter(
					   utHandle hFilter,
					   utFilterDestoryNotify destoryCallback)
{
	LOGTRACE(LOGDEBUG,"[UsbTV] delTsPacketFilter(%p)\n",(unsigned long)hFilter);

	if(!is_curthread())
	{
		AutoLockT lock(tsMutex_);
		inner_delTsFilter(hFilter);
	}
	else{
		inner_delTsFilter(hFilter);
	}
}
void TsCatcher::inner_delTsFilter(utHandle hFilter)
{
	if(hFilter)
	{
		SectionFilter* filter = (SectionFilter*)hFilter;
		if(filter->valid_)
		{
			for(unsigned int i=0; i<filter->filters_.size(); i++)
				TSInf_releaseTsPid(&TsInf_,filter->filters_[i].filter.pid);
			filter->filters_.clear();
			filter->valid_=false;
		}
	}
}

void TsCatcher::enableTs(bool enable)
{
	LOGTRACE(LOGINFO,"Enter enableTs(%s).\n",enable ? "true" : "false");
	if(enableTs_!=enable)
	{
		LOGTRACE(LOGDEBUG,"[UsbTV] TsCatcher::enableTs(%d)\n",enable);

		enableTs_=enable;
		if(enable) 
		{
			start();
		}
		else
		{
			stop();
		}
	}
	LOGTRACE(LOGINFO,"Leave enableTs.\n");
}

U32 TsCatcher::do_run()
{
	LOGTRACE(LOGDEBUG,"[UsbTV] TsCatcher::do_run() begin\n");

	U32 iActiveCount = 0;
	while( !signalled() )
	{
		iActiveCount++;
	
		if(device_)
		{
			BYTE *pBuffer = 0;
			if(0 < tvdevice_readTSData(device_,&pBuffer))
			{
				TSInf_PutTsPactet(&TsInf_,pBuffer,TD_TSPACKET);
				if(0 == iActiveCount%10){
					LOGTRACE(LOGINFO,"Exist TS Packet.\n");
				}
			}
		}
		{
			AutoLockT  lock(secMutex_);
			for(int i=0; i<MAX_SECFILTER_COUNT; i++)
			{
				if(SecFilterList_[i].valid_) 
					SecFilterList_[i].checkTimeOut();
			}
		}
			
		{
			AutoLockT lock(tsMutex_);
			for(int i=0; i<MAX_TSFILTER_COUNT; i++)
			{
				if(TSDataFilterList_[i].valid_) 
					TSDataFilterList_[i].checkTimeOut();
			}
		}


		NS_sleep(1);
		if(0 == iActiveCount%100){
			LOGTRACE(LOGINFO,"TsCatcher::do_run() is active.\n");
		}
	}
	
	LOGTRACE(LOGDEBUG,"[UsbTV] TsCatcher::do_run() end\n");
	
	return 0;
}

void TsCatcher::onSecDataHandler(void *context,unsigned short pid,unsigned char *SecData,unsigned short SecDataLen)
{
	TsCatcher *This = (TsCatcher *)context;

	AutoLockT lock(This->secMutex_);
	for(int i=0;i<MAX_SECFILTER_COUNT; i++)
	{
		if(This->SecFilterList_[i].valid_)
			This->SecFilterList_[i].process(0,pid,SecData,SecDataLen,false);
	}
}

void TsCatcher::onTSDataHandler(void *context,unsigned short pid,unsigned char *pTSBuffer,unsigned short iTSBuffer)
{
	TsCatcher *This = (TsCatcher *)context;

	AutoLockT lock(This->tsMutex_);
	for(int i=0;i<MAX_TSFILTER_COUNT; i++)
	{
		if(This->TSDataFilterList_[i].valid_)
			This->TSDataFilterList_[i].process(0,pid,pTSBuffer,iTSBuffer,true);
	}
}
