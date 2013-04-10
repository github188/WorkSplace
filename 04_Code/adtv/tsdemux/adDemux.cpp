#include "adDemux.h"
#include "tvcomm.h"
#include "tsdemux.h"

#define	 LOG_TAG "adDemux"
#include "tvlog.h"

struct FilterCondT{
	long		iTuner;
	long		pid;
	long		tableid;
	long		timeout;
	utHandle	handle;
};
typedef std::list<FilterCondT> ListFilterCondT;

struct DemuxBindInfo{
	::std::string name;
	ListFilterCondT ts;
	ListFilterCondT section;
	IDemuxFilter*	pCallBack;
};
typedef std::map<std::string,DemuxBindInfo> mapDemuxBindInfoT;

static mapDemuxBindInfoT DemuxBindInfo_;
static long filter_pid[8192]={0};
static int filter_pidnum = 0;
static void UTFilterDataCallback(utHandle hFilter,long iTuner,utPid pid,utTid tid,utByte *data,long datasize,utContext context);
static void UTTsDataCallback(utHandle hFilter,long iTuner,utPid pid,utTid tid,utByte *data,long datasize,utContext context);

bool android_utOpen(){
return true;
}

void android_utClose()
{
	return;
}

int	 android_utGetTunerCount()
{
	return 1;
}

bool android_utTune(long iTuner,long freq,long qam,long symb)
{
	return true;
}

bool android_utGetLocked(long iTuner)
{
	return true;
}

void android_utEnableTs(bool bEnable)
{
	tsdemux_enableDemux(bEnable);
}

void android_utAddTsFilter(const char* name,long pid)
{
	LOGTRACE(LOGINFO,"Enter android_utAddTsFilter(%s,%d).\n",name,pid);
	FilterCondT filter_;
	utTsDataFilter filter[1];

	mapDemuxBindInfoT::iterator it = DemuxBindInfo_.find(name);
	if(it != DemuxBindInfo_.end())
	{
		filter[0].iTuner = 0;
		filter[0].pid    = pid;		filter_.pid    = pid;
		filter_.iTuner = 0;
		filter_.tableid= 0;
		filter_.timeout= 0;

		filter_.handle = tsdemux_addTSFilter(filter,1,UTTsDataCallback,reinterpret_cast<utContext*>(it->second.pCallBack));
		if(0 != filter_.handle)
		{
			it->second.ts.push_back(filter_);
		}
	}

	LOGTRACE(LOGINFO,"Leave android_utAddTsFilter.\n");
}

void android_utDelTsFilter(const char* name,long pid)
{
	LOGTRACE(LOGINFO,"Enter android_utDelTsFilter(%s,%d).\n",name,pid);
	mapDemuxBindInfoT::iterator it = DemuxBindInfo_.find(name);
	if(it != DemuxBindInfo_.end())
	{
		ListFilterCondT::iterator itCond = it->second.ts.begin();
		for (;itCond != it->second.ts.end();itCond++)
		{
			if(itCond->pid == pid)
			{
				tsdemux_delTSFilter(itCond->handle,0);
				it->second.ts.erase(itCond);
				break;
			}
		}
	}
	LOGTRACE(LOGINFO,"Leave android_utDelTsFilter(%s,%d).\n");
}

void android_utDelAllTsFilter()
{
	return ;
}

void android_utAddSectionFilter(const char* name,long pid,long tid,long timeout)
{
	if((pid == 0x1025) || (pid == 0x1ffe))
		return;
	LOGTRACE(LOGINFO,"Enter android_utAddSectionFilter(%s,%d,%d,%d).\n",name,pid,tid,timeout);
	FilterCondT filter_;
	utSectionFilter filter[1];

	mapDemuxBindInfoT::iterator it = DemuxBindInfo_.find(name);
	if(it != DemuxBindInfo_.end())
	{
		filter[0].iTuner = 0;
		filter[0].pid    = pid;
		filter[0].tid    = tid;
		filter[0].timeout= timeout;

		filter_.iTuner = 0;
		filter_.pid    = pid;
		filter_.tableid= tid;
		filter_.timeout= timeout;
		filter_.handle = tsdemux_addSectionFilter(filter,1,UTFilterDataCallback,reinterpret_cast<utContext*>(it->second.pCallBack));
		if(0 != filter_.handle)
		{
			it->second.section.push_back(filter_);
		}
	}

	LOGTRACE(LOGINFO,"Leave android_utAddSectionFilter.\n");
}

void android_utDelSectionFilter(const char* name,long pid,long tid)
{
	LOGTRACE(LOGINFO,"Enter android_utDelSectionFilter(%s,%d,%d).\n",name,pid,tid);

	mapDemuxBindInfoT::iterator it = DemuxBindInfo_.find(name);
	if(it != DemuxBindInfo_.end())
	{
		ListFilterCondT::iterator itCond = it->second.section.begin();
		for (;itCond != it->second.section.end();itCond++)
		{
			if(itCond->pid == pid && itCond->tableid==tid)
			{
				tsdemux_delSectionFilter(itCond->handle,0);
				it->second.section.erase(itCond);
				break;
			}
		}
	}
	LOGTRACE(LOGINFO,"Leave android_utDelSectionFilter.\n");
}

void android_utDelSectionAllFilter(const char* name)
{
	LOGTRACE(LOGINFO,"Enter android_utDelSectionAllFilter(%s).\n",name);

	mapDemuxBindInfoT::iterator it = DemuxBindInfo_.begin();
	for(;it != DemuxBindInfo_.end();it++)
	{
		ListFilterCondT::iterator itCond = it->second.section.begin();
		for (;itCond != it->second.section.end();itCond++)
		{
			tsdemux_delSectionFilter(itCond->handle,0);
		}
		it->second.section.clear();
	}
	
	LOGTRACE(LOGINFO,"Leave android_utDelSectionAllFilter.\n");
}

void android_utSetFilterCallBack(const char* name,IDemuxFilter* fn)
{
	LOGTRACE(LOGINFO,"Enter android_utSetFilterCallBack(%s,0x%08X).\n",name,fn);
	DemuxBindInfo info;
	mapDemuxBindInfoT::iterator it = DemuxBindInfo_.find(name);
	if(it == DemuxBindInfo_.end())
	{
		info.name = name;
		info.pCallBack = fn;
		DemuxBindInfo_.insert(mapDemuxBindInfoT::value_type(name,info));
	}
	else
	{
		it->second.pCallBack = fn;
	}
	LOGTRACE(LOGINFO,"Leave android_utSetFilterCallBack.\n");
}

void UTFilterDataCallback(utHandle hFilter,long iTuner,utPid pid,utTid tid,utByte *data,long datasize,utContext context)
{
	IDemuxFilter* pCallBack = static_cast<IDemuxFilter*>(context);
	if(0 != pCallBack)
	{
		pCallBack->OnSectionData(iTuner,pid,tid,data,datasize);
	}
}

void UTTsDataCallback(utHandle hFilter,long iTuner,utPid pid,utTid tid,utByte *data,long datasize,utContext context)
{
	IDemuxFilter* pCallBack = static_cast<IDemuxFilter*>(context);
	if(0 != pCallBack)
	{
		pCallBack->OnTSData(iTuner,pid,data,datasize);
	}
}

void android_utSetPidControl(const long* pids,const long lCount)
{
	return ;
}
