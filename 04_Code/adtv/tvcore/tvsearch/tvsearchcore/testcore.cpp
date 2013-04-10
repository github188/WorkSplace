// TestCore.cpp : Defines the entry point for the console application 中文 for vim
#include "typ.h"
#include "isearchtvnotify.h"
#include "itvsearchcore.h"
#include "stbruntime.h"

int timeBegin;

class MyNotify :public ISearchTVNotify
{
public:
	virtual ~MyNotify(){};
	void DisplayServices(DVBService &service)
	{
		printf("data... serviceID:%d, pmt_id:%d, video_stream.pid:%d audio_stream.pid:%d,\
name:%s,ch_no:%d,vol_comp:%d\n",\
			service.serviceID, service.pmt_id,service.video_stream.stream_pid,service.audio_stream[0].stream_pid,\
			service.name, service.channel_number,service.volume_comp);
	}
    // 单频点搜索结果
	virtual void OnDVBService(std::vector<DVBService> &services)
	{
		printf("R... OnDVBService received\n");
		for(unsigned int i=0; i<services.size(); i++)
		{
			DisplayServices(services[i]);
		}
	}
	// 全频搜索时的进度条 
    virtual void OnProgress(U32 iPercent)
	{
		printf("R... OnProgress received. iPercent is %d\n",iPercent);
	}
	// 全频搜索时的频点信息(开始搜索的频点及信号强度等)
	virtual void OnTunerInfo(const TuningParam& tuning,const TunerSignal& signal)
	{
		printf("R... OnTunerInfo received,freq: %d,signal strength:%d, signal quality:%d\n",tuning.freq, signal.strength,signal.quality);
	}
	// 频道搜索完成通知
	virtual void OnSearchTVComplete(std::vector<DVBService> &services, std::vector<ServiceTypeTableItem> &table)
	{
		int deltaTime=NS_GetTickCount()-timeBegin;
		printf("R... OnSearchTVComplete received, services:%d, use time:%d ms\n",services.size(),deltaTime);
		for(UINT i=0; i<services.size(); i++)
		{
			printf("channel number: %d, serviceID:%d, freq:%d, emm:%d\n",\
				services[i].channel_number,services[i].serviceID, services[i].ts.tuning_param.freq, services[i].emm_pid);
		}
		for(UINT i=0; i<table.size(); i++)
		{
			printf("table id: %d, descriptor: %s\n",table[i].serviceType, table[i].serviceName);
		}
	}
	// 节目搜索完成通知
	virtual void OnSEPGComplete()
	{
		printf("R... OnSEPGComplete received\n");
	}
	// 主频点NIT版本更新通知
	virtual void OnNitVersionChanged(U8 iVersion)
	{
		printf("R... OnNitVersionChanged received\n");
	}

};
int main(int argc, char* argv[])
{
	MyNotify notify;
	TuningParam param;
//	param.freq =602000;		// 全频搜索时, 该频率点被忽略,由freqs_searchtable 确定
	param.freq =642000;		// 全频搜索时, 该频率点被忽略,由freqs_searchtable 确定
//	param.freq =586000;		// 
	param.qam = 2;
	param.symb =6875;
	bool loop1;
	do
	{
		loop1=false;
		printf("select search MODE:\n");
		printf("0 -- MANUAL\n");
		printf("1 -- FULL\n");
		printf("2 -- NIT\n");
		char ch=getchar();
		switch(ch)
		{
			case '0':
				StartSearchTV(STVMODE_MANUAL,&param,&notify);
				break;
			case '1':
				StartSearchTV(STVMODE_FULL,&param,&notify);
				break;
			case '2':
				StartSearchTV(STVMODE_NIT,&param,&notify);
				break;
			default:
				loop1=true;
					
		}
	}while(loop1);
	timeBegin=NS_GetTickCount();
	while(1)
	{
/*		
#ifdef _WINDOWS
		Sleep(100);
#else
		usleep(1000*100);
#endif
*/
	NS_sleep(100);
	}
	return 0;
}

