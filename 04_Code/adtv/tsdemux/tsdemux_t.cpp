#include "tsdemux_def.h"
#include "tsdemux.h"
#include "tvdevice.h"
#include "stbruntime.h"
#include <stdio.h>
#include <signal.h>

#define	 LOG_TAG "tsdemux_t "
#include "tvlog.h"

static bool bExitProcess = false;
static bool bComplete    = false;
void  OnDelComplete(utHandle hFilter,utContext context)
{
	LOGTRACE(LOGINFO,"calling OnDelComplete().\n");
}

void OnTSData(utHandle hFilter,long iTuner,utPid iPid,utTid tid,utByte *pData,long iLength,utContext context)
{
	LOGTRACE(LOGINFO,"calling OnTSData(%d,%d,0x%08X,%d).\n",iTuner,iPid,pData,iLength);
}

void OnSectionData(utHandle hFilter,long iTuner,utPid iPid,utTid tid,utByte *pData,long iLength,utContext context)
{
	LOGTRACE(LOGINFO,"calling OnSectionData(%d,%d,%d,0x%08X,%d).\n",iTuner,iPid,tid,pData,iLength);
}

void SignalProcessHandler(int)
{
	bExitProcess = true;
}

void testTsFilter(HANDLE hDevice)
{
	HANDLE hTSFilter;
	utTsDataFilter filters[2]={{0,0xca},{0,0xcb}};
	long lFilterCount = 2;

	hTSFilter = tsdemux_addTSFilter(filters,lFilterCount,OnTSData,0);
	if(hTSFilter)
	{
		tsdemux_enableDemux(true);
		while(!bExitProcess)
		{
			NS_sleep(200);
		}

		// async operation.
		tsdemux_delTSFilter(hTSFilter,OnDelComplete);
#if 0 
		while(!bComplete)
		{
			NS_sleep(100);
		}
#endif 
	}
}
void testSecFilter(HANDLE hDevice)
{

	HANDLE hTSFilter;
	utSectionFilter filters={0,0x10,NO_TABLE_ID,{0x40,0,0,0,0,0,0,0},{0xff,0,0,0,0,0,0,0},0xffff};
	long lFilterCount = 1;

	hTSFilter = tsdemux_addSectionFilter(&filters,lFilterCount,OnSectionData,0);
	if(hTSFilter)
	{
		tsdemux_enableDemux(true);
		while(!bExitProcess)
		{
			NS_sleep(200);
		}

		// async operation.
		tsdemux_delSectionFilter(hTSFilter,OnDelComplete);
#if 0 
		while(!bComplete)
		{
			NS_sleep(100);
		}
#endif 
	}
}

#include "adDemux.h"
struct data_cb : public IDemuxFilter
{
	virtual void OnTSData(long iTuner,long iPid,unsigned char *pData,long iLength)
	{
		LOGTRACE(LOGINFO,"========OnTSData======.\n");
	}
	virtual void OnSectionData(long iTuner,long iPid,long tid,unsigned char *pData,long iLength)
	{
		LOGTRACE(LOGINFO,"========OnSectionData======.\n");
	}
};

void testadDemux()
{
	data_cb callback;
	android_utSetFilterCallBack("test",&callback);
	android_utAddTsFilter("test",0xca);
	android_utAddTsFilter("test",0xcb);
	android_utEnableTs(true);
	while(!bExitProcess)
	{
		NS_sleep(200);
	}
}
int main(int argc, char* argv[])
{

	signal(SIGINT ,SignalProcessHandler);

	bool bRet = false;
	TunerSignal stTunerSignal;
	HANDLE hDevice = tvdevice_open();
	if(hDevice)
	{
		bRet = tvdevice_tune(hDevice,602000,6875,2);
		LOGTRACE(LOGINFO,"calling tvdevice_tune() %s.\n",bRet ? "successed":"failed");

		bRet = tvdevice_getTunerSignalStatus(hDevice,&stTunerSignal);
		LOGTRACE(LOGINFO,"calling tvdevice_getTunerSignalStatus %s.\n",bRet ? "successed":"failed");

		char strID[256]={0};
		tvdevice_getStbID(hDevice,strID,256);

		//testTsFilter(hDevice);
		//testSecFilter(hDevice);
		testadDemux();

		tvdevice_close(hDevice);
	}

	return 0;
}


