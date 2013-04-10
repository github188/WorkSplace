#include "tvcomm.h"
#include "stbruntime.h"
#include "tsdemux.h"
#include "tsdemux_def.h"
#include "tvdevice.h"
#include "simplethread.h"

#include "tvplayer.h"
#include <stdio.h>
#include <signal.h>
#include "tvservice.h"

#define	 LOG_TAG "tvplayer_t"
#include "tvlog.h"

bool bExitProcess = false;
void SignalProcessHandler(int)
{
	LOGTRACE(LOGINFO,"SignalProcessHandler.\n");
	bExitProcess = true;
}


#define INITTESTDATA(service) \
	{\
	memset(&service,0,sizeof(DVBService));\
	service.serviceID = 101 ; \
	service.pmt_id = 0x101;\
	service.ca_mode = 0x1772;\
	service.ts.tuning_param.freq = 610000; \
	service.ts.tuning_param.symb = 6875; \
	service.ts.tuning_param.qam  = 2 ; \
	service.video_stream.ecm_pid = 0x1772; \
	service.video_stream.stream_pid = 0x200; \
	service.video_stream.stream_type= 0x2; \
	service.audio_index = 0;\
	service.audio_stream[service.audio_index].ecm_pid = 0x1772; \
	service.audio_stream[service.audio_index].stream_pid = 0x28a; \
	service.audio_stream[service.audio_index].stream_type= 0x3; \
	}

utSectionFilter secFilter[3]={
	{0,NIT_PID,NO_TABLE_ID,{TableId_NitA,0,0,0,0,0,0,0},{0xff,0,0,0,0,0,0,0},2000},
	{0,SDT_PID,NO_TABLE_ID,{TableId_SdtA,0,0,0,0,0,0,0},{0xff,0,0,0,0,0,0,0},3000},
	{0,PAT_PID,NO_TABLE_ID,{TableId_Pat, 0,0,0,0,0,0,0},{0xff,0,0,0,0,0,0,0},1000},
};

static int iLoopCount = 0;
struct tsdemux_t
{
public:
	tsdemux_t(bool bExitSignal)
		:exit_(bExitSignal){}
	virtual ~tsdemux_t(){}
	
	void start_t(){

		utTsDataFilter tsFilter[2]={{0,0x00},{0,0x00}};
		tsFilter[0].pid = 0xca;
		tsFilter[1].pid = 0xcb;

		tsdemux_enableDemux(true);
		thread_.start(async_t,this);
	}
	void stop_t(){
		thread_.stop();
		tsdemux_enableDemux(false);
	}

	static UINT __stdcall async_t(LPVOID lpParam){
		simplethread*	pThread		= reinterpret_cast<simplethread*>(lpParam);
		tsdemux_t*		pController = reinterpret_cast<tsdemux_t*>(pThread->get_arglist());

		printf("[%p],enter async_t.\n",pController);

		while(!pThread->check_stop())
		{
			utHandle h0 = tsdemux_addSectionFilter(secFilter,3,OnSectionData,(void**)pController);
			
			LOGTRACE(LOGINFO,"[%p],h=%p,tsdemux_addSectionFilter\n",pController,h0);

			NS_sleep(1000);
			
			tsdemux_delSectionFilter(h0,0);
			LOGTRACE(LOGINFO,"[%p],h=%p,tsdemux_delSectionFilter\n",pController,h0);
			NS_sleep(1000);

			
		}
		return 0;
	}

	
	static void OnSectionData( utHandle hFilter,long iTuner,utPid pid,utTid tid,utByte *data,long datasize,utContext context)
	{
		tsdemux_t*		pController = reinterpret_cast<tsdemux_t*>(context);
		LOGTRACE(LOGINFO,"[%p],h=[%p],OnSectionData.\n",pController,hFilter);
		//if(0 == ++iLoopCount%10)
		//{
		//	printf("++[%p],h=[%p],tsdemux_delSectionFilter.\n",pController,hFilter);
		//	tsdemux_delSectionFilter(hFilter,0);
		//}
	}

	
	static void OnTSData( utHandle hFilter,long iTuner,utPid pid,utTid tid,utByte *data,long datasize,utContext context)
	{
		printf("OnTSData.\n");
	}

private:
	bool         exit_;
	simplethread thread_;
};

int main(int argc, char* argv[])
{
	signal(SIGINT ,SignalProcessHandler);
#if !defined(WIN32)
	signal(SIGKILL,SignalProcessHandler);
#endif 
	
	//HANDLE hDevice = tvdevice_open();
	//if(hDevice)
	//{
	//	bool bRet = tvdevice_tune(hDevice,602000,6875,2);
	//	printf("tvdevice_tune(602000,6875,2),bRet=%d",bRet);
	//}

	//#define TEST_COUNT (10)
	//tsdemux_t* pSequence[TEST_COUNT];
	//for(int i = 0; i< TEST_COUNT ;i++)
	//{
	//	pSequence[i] = new tsdemux_t(bExitProcess);
	//	if(pSequence[i]){
	//		pSequence[i]->start_t();
	//	}
	//	NS_sleep(10);
	//}
	tvservice_init();
	tvservice_play();

	DVBService s;
	INITTESTDATA(s);

	tvservice_setService(&s);

	while(!bExitProcess)
	{
		NS_sleep(200);
	}
	
	//for(int i = 0; i< TEST_COUNT ;i++)
	//{
	//	if(pSequence[i]){
	//		pSequence[i]->stop_t();
	//	}
	//	NS_sleep(10);
	//}

	tvservice_stop();
	tvservice_uninit();
}
