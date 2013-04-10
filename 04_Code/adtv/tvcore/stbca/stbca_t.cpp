// TestCA.cpp : Defines the entry point for the console application.
//

#include <stdio.h>
#include <stdlib.h>

#include "typ.h"
#include "cdcas_hdr.h"
#include "capture.h"
#include "stbca_utility.h"
#include "stbruntime.h"

#define LOG_LEVEL LOG_TRACE
#define LOG_TAG "stbca_t"
#include "dxreport.h"


extern ObjStbCa gStbCa;

#if 0

#define INITTESTDATA(service) \
{\
	memset(&service,0,sizeof(DVBService));\
	service.serviceID = 201 ; \
	service.pmt_id = 0xc9;\
	service.emm_pid= 0xd4;\
	service.ts.tuning_param.freq = 482000; \
	service.ts.tuning_param.symb = 6875; \
	service.ts.tuning_param.qam  = 2 ; \
	service.video_stream.ecm_pid = 0x2c0; \
	service.video_stream.stream_pid = 0x200; \
	service.video_stream.stream_type= 0x2; \
	service.audio_index = 0;\
	service.audio_stream[service.audio_index].ecm_pid = 0x2c0; \
	service.audio_stream[service.audio_index].stream_pid = 0x28a; \
	service.audio_stream[service.audio_index].stream_type= 0x4; \
}
#endif

#if 1
void INITTESTDATA(DVBService &service) 
{\
	memset(&service,0,sizeof(DVBService));
	service.serviceID = 0x8d ; 
	service.pmt_id = 0xc8;
	service.emm_pid= 0x1771;
	service.ts.tuning_param.freq = 602000; 
	service.ts.tuning_param.symb = 6875; 
	service.ts.tuning_param.qam  = TUNER_QAM64; /// 2 ; 
	service.video_stream.ecm_pid = 0x1785; 
	service.video_stream.stream_pid = 0xca; 
	service.video_stream.stream_type= 0x2; 
	service.audio_index = 0;
	service.audio_stream[service.audio_index].ecm_pid = 0x1785; 
	service.audio_stream[service.audio_index].stream_pid = 0xcb; 
	service.audio_stream[service.audio_index].stream_type= 0x3; 
}
#endif

int main(int argc, char* argv[])
{

	if(!Ca_Init()) 
	{
		printf("Ca_Init failed\n");
		return 1;
	}
	printf("wait a while, 10 seconds...\n");
	dxreport("wait a while, 10 seconds...\n");
	NS_sleep(10000); // 等待智能卡复位完成

	printf("=====================after Ca_Init=====================\n");
	dxreport("=====================after Ca_Init=====================\n");
	// 获取STBCA的版本
	char version[64];
	stbca_version(version);
	printf("version: %s\n",version);
	// 获取STBCA的类型
	CasType type = stbca_cas_type();
	printf("type: %d\n",type);

	// 获取STBID号.
	WORD platformID;
	ULONG uniqueID;
	stbca_get_stbid(&platformID, &uniqueID);
	printf("platformID: %d, uniqueID: %lu\n",platformID, uniqueID);
	U8 emailNum, freeNum;
	stbca_get_email_space(emailNum, freeNum);
	printf("emailNum: %d, freeNum: %d\n",emailNum,freeNum);

	if(gStbCa.bSCardStatus_== 0/*SC_OUT*/)
	{
		printf("card not inserted\n");
		return 1;
	}

#if 0

	// 与卡有关	
	char szCardID[128];
	szCardID[0]=0;
	int res=stbca_GetCardID(szCardID);
	if(!res)
	{
		printf("szCardID is %s\n",szCardID);
	}
	else
	{
		printf("J_StbcaGetCardID return error, szCardID is %s, ret is %d\n",szCardID,res);
	}
#if 1	
	unsigned char oldpin[]="000000";
	unsigned char newpin[]="123456";
#else	
	unsigned char oldpin[]="123456";
	unsigned char newpin[]="000000";
#endif	

	res=stbca_change_pin_code(oldpin, newpin);
	if(res==CDCA_RC_OK)
	{
		printf("change pin succeed, res:%d\n",res);
	}
	else
	{
		printf("change pin failed, res:%d\n",res);
	}
	// 设置观看级别	
	res=stbca_set_watch_rating(newpin,8);
	if(res==CDCA_RC_OK)
	{
		printf("set watch rating succeed,res:%d\n",res);
	}
	else
	{
		printf("set watch rating failed, res: %d\n",res);
	}
	// 获取观看级别 
	int rating=stbca_get_watch_rating();
	printf("rating is %d\n", rating);

	U8 start_h=0, end_h=23;
	U8 start_m=0, start_s=0, end_m=59, end_s=59;
	//设置观看时段
	res=stbca_set_watch_time1(newpin, start_h,  end_h);
	if(res==CDCA_RC_OK)
	{
		printf("set watch time ok,res:%d\n",res);
	}
	else
	{
		printf("set watch time failed, res:%d\n",res);
	}
	res=stbca_set_watch_time2(newpin, start_h, start_m, start_s,end_h,end_m,end_s);
	if(res==CDCA_RC_OK)
	{
		printf("set watch time 2 ok,res:%d\n",res);
	}
	else
	{
		printf("set watch time 2 failed, res:%d\n",res);
	}
	//读取观看时段	
	stbca_get_watch_time1(start_h, end_h);
	printf("start_h: %d, end_h:%d\n", start_h, end_h);
	stbca_get_watch_time2(start_h, start_m, start_s, end_h,end_m,end_s);
	printf("start_h: %d, start_m:%d, start_s:%d, end_h:%d, end_m:%d, end_s:%d\n", start_h,start_m,start_s, end_h,end_m,end_s);


	// 获取运营商ID 列表	
	std::vector<OperatorId> operatorIDs;
	std::vector<PurseId> purseIds;
	OperatorInfo info("");
	stbca_get_operator_ids(operatorIDs);
	for(unsigned int i=0; i<operatorIDs.size(); i++)
	{
		printf("operator id: %d,size:%d\n",operatorIDs[i], operatorIDs.size());
		stbca_get_operator_info(operatorIDs[i],info);
		printf("info.name is %s\n",info.name.c_str());
		res=stbca_get_purse_ids(operatorIDs[i],purseIds);
		//		stbca_get_purse_ids(operatorIDs[1],purseIds);
		printf("purseIDS size is %d\n", purseIds.size());
		for(UINT j=0; j<purseIds.size(); j++)
		{
			printf("purse IDs is %d\n",purseIds[j]);
		}
	}
	//	stbca_get_operator_info

	DVBService s;
	INITTESTDATA(s);
#endif

	while(1)
	{
	}

	Ca_Uninit();
	return 0;
}
// 换台
int		change_channel(const DVBService& service)
{
	int iRet = 0;


	int freq = service.ts.tuning_param.freq;
	int symb = service.ts.tuning_param.symb;
	int qam  = service.ts.tuning_param.qam;

	bool bTuneStatus=0;
	//	bool bTuneStatus = tvdevice_tune(hDevice_,freq,symb,qam);

	if(bTuneStatus)
	{

		U16 vpid = service.video_stream.stream_pid;
		U8 vtype = service.video_stream.stream_type;
		U16 apid = service.audio_stream[service.audio_index].stream_pid;
		U8 atype = service.audio_stream[service.audio_index].stream_type;

		//		memcpy(&stService_,&service,sizeof(DVBService));

		// 设置解扰
		int iECMPidCount;
		//	int iECMPidCount = descrambling(service);

		// 硬件过滤(pat,nit,pmt,vpid,apid,ecmpid(V/A),emmpid)
		int iPidCount = 8;
		int pids[9]={0,0x10,0x12,0x00,0x00,0x00,0x00,0x00};
		pids[3]	= service.pmt_id;
		pids[4] = service.video_stream.stream_pid;
		pids[5]	= service.audio_stream[service.audio_index].stream_pid;
		pids[6] = service.video_stream.ecm_pid;
		pids[7] = service.emm_pid;
		if(2 == iECMPidCount)
		{
			pids[8] = service.audio_stream[service.audio_index].ecm_pid;
			iPidCount = 9;
		}

		// 设置硬件过滤序列
		printf("=== vpid=%d,apid=%d,vecm_pid=%d,aecm_pid=%d ===.\n",
				vpid,apid,service.video_stream.ecm_pid,service.audio_stream[service.audio_index].ecm_pid);
		//		tvdevice_setPidFilter(hDevice_,pids,iPidCount);

		// 同步码流
		//		unmap_av_pid(hTSFilter_);
		//		hTSFilter_ = map_av_pid(vpid,apid);


		// 调用播放适配器
#if !defined(WIN32)
		//		int iPlayStatus = tvplay_adapter_play(vpid,vtype,apid,atype);
		//		LOGTRACE(LOGINFO,"tvplay_adapter_play:iPlayStatus=%d.\n",iPlayStatus);
#endif
	}
	return iRet;
}

