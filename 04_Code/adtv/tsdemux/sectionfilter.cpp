#include "SectionFilter.h"

#define  LOG_TAG "libtsdemux"
#include "tvlog.h"
#include "stbruntime.h"

SectionFilter::SectionFilter()
{
	valid_ = false;
	beginTick_  = 0;
	handle_ = 0;
	context_ = 0;
	destoryCallback_=0;
	bTSOrSection = false;
	beginTick_= 0;
}

SectionFilter::~SectionFilter()
{
}

bool SectionFilter::matchFilter(utByte *filter,utByte *mask,utByte *SecData,long SecDataLen)
{
	if(SecDataLen<8)
		return false;

	int index[8]={0,3,4,5,6,7,8,9};
	for( int i=0; i<8; i++ )
	{
		if(index[i]<SecDataLen) 
		{
			if( (SecData[index[i]] & mask[i]) != (filter[i]&mask[i]) )
				return false;
		}
	}

	return true;
}

void SectionFilter::process_section(long iTuner,utPid pid,utByte *SecData,long SecDataSize)
{	
	if(valid_ && SecDataSize>=3)
	{
			//LOGTRACE(LOGINFO,"pid=%d,secData=%p,SecDataSize=%d\n",pid,SecData,SecDataSize);

		unsigned long passTick = NS_GetTickCount()-beginTick_;
		utTid tid=SecData[0];
		for(unsigned int i=0; i<filters_.size(); i++)
		{
			if( filters_[i].enable
				&& handle_
				&& filters_[i].filter.iTuner==iTuner 
				&& filters_[i].filter.pid==pid
				&& (passTick<filters_[i].filter.timeout||filters_[i].filter.timeout==UT_INFINITE)
				)
			{
				if( (filters_[i].filter.tid!=NO_TABLE_ID && filters_[i].filter.tid==tid)
					|| 
						(filters_[i].filter.tid==NO_TABLE_ID 
						&& matchFilter(filters_[i].filter.filterData,filters_[i].filter.filterMask,SecData,SecDataSize))
					)
				{
						//LOGTRACE(LOGINFO,"11111pid=%d,tid=%d,SecData=%p,SecDataSize=%d\n",pid,tid,SecData,SecDataSize);
					handle_(this,iTuner,pid,tid,SecData,SecDataSize,context_);
				}
			}
		}
	}
}


void SectionFilter::process(long iTuner,utPid pid,utByte *pData,long lLength,bool bDataType /* = false */)
{
	if(valid_ && lLength>=3)
	{
		if(!bDataType)
		{
			process_section(iTuner,pid,pData,lLength);
		}
		else
		{
			// 处理TS 分组包数据
			for(unsigned int i=0; i<filters_.size(); i++)
			{
				if(filters_[i].enable && filters_[i].filter.pid == pid && handle_)
				{
					handle_(this,iTuner,pid,0,pData,lLength,context_);
					break;
				}
			}
		}
	}
}

void SectionFilter::checkTimeOut()
{
	unsigned long passTick = NS_GetTickCount()-beginTick_;
	for(unsigned int i=0; i<filters_.size(); i++)
	{
		if( filters_[i].enable 
			&&passTick>=filters_[i].filter.timeout
			&&filters_[i].filter.timeout!=UT_INFINITE)
		{
			if(handle_)
				handle_(this,
					filters_[i].filter.iTuner,
					filters_[i].filter.pid,
					filters_[i].filter.tid,
					0,
					0,
					context_);
			filters_[i].enable=false;
		}
	}

}

