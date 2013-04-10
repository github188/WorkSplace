//包含中文字符,供vim 识别
#include "typ.h"
#include <DVTCAS_STBInf.h>
#include <DVTSTB_CASInf.h>
#include "stbca_utility.h"
#include "capture_def.h"
#include "psisi_parse/all.h"
#include "psisi_parse/imp/pre.h"
#include "psisi_parse/imp/commondesc.h"

#define LOG_LEVEL LOG_TRACE
#define LOG_TAG "stbca"
#include "dxreport.h"
#include "capture_ex.h"

#define AREALOCk_CONFIG_FILE "/data/data/novel.supertv.dvb/arealock.conf"

#define STBCA_DEBUG_PRINT

extern ObjStbCa gStbCa;

namespace novelsuper
{
namespace psisi_parse 
{
struct DescNitHuangGang : Descriptor<0x5f>
{
	U32 privateData() const{ return n2h32( privateData_ ); }
private:
	U8 privateData_[4];
};
typedef DescList_Iterator<DescNitHuangGang> NitHuangGang_I;
}
}

//NIT区域锁定
static HANDLE gNitSecFilter = NULL;
int gPDSD = -1;

//单频点区域锁定
static HANDLE gCATSecFilter = NULL;
bool gAreaLockOK = false;

void NitPrivateDataGotCallBack( utHandle hFilter,long iTuner,utPid pid,utTid tid,utByte *data,long datasize,utContext context)
{
//	dxreport("Enter NitPrivateDataGotCallBack \n");
	if((NULL == data)  || (datasize < 10))
		return;

	novelsuper::psisi_parse::NitSection *pNit = (novelsuper::psisi_parse::NitSection *)data;

	if(!pNit->CRC_OK())
		return;

	// 黄冈私有描述符 0x5f
	int iPDSD = 0;
	novelsuper::psisi_parse::NitHuangGang_I privateIt = 
		pNit->begin<novelsuper::psisi_parse::DescNitHuangGang>();
	if(!privateIt.empty())
	{
		iPDSD = privateIt->privateData();
	}
//	dxreport("NitPrivateDataGotCallBack new %u, old %u\n", iPDSD, gPDSD);

	//更新文件
	if(iPDSD != gPDSD)
	{
	}
//	dxreport("Leave NitPrivateDataGotCallBack \n");
}

void StopNitMonitor()
{
	dxreport("StopNitMonitor filter handle:0x%x\n",gNitSecFilter);
	if(gNitSecFilter != NULL)
	{
		tsdemux_delSectionFilter(gNitSecFilter, NULL);
		gNitSecFilter = NULL;
	}
}

void StartNitMonitor()
{
	dxreport("Enter StartNitMonitor \n");
	if(gNitSecFilter != NULL)
		StopNitMonitor();

	utSectionFilter filter;
	memset(&filter, 0, sizeof(filter));

	filter.iTuner= 0;
	filter.pid 	= NIT_PID;
	filter.tid 	= NO_TABLE_ID;
	filter.timeout = 0xFFFFFFFF;
	filter.filterData[0] = TableId_NitA;
	filter.filterMask[0] = 0xff;

	HANDLE handle = tsdemux_addSectionFilter(&filter,1,NitPrivateDataGotCallBack,NULL);
	dxreport("StartNitMonitor filter added, handle:0x%x\n",handle);
	gNitSecFilter = handle;
}


void StopRetrieveEMM()
{
	if(gCATSecFilter != NULL)
	{
		tsdemux_delSectionFilter(gCATSecFilter, NULL);
		gCATSecFilter = NULL;
	}
}

void CATPrivateDataGotCallBack( utHandle hFilter,long iTuner,utPid pid,utTid tid,utByte *data,long datasize,utContext context)
{

	if((NULL == data)  || (datasize < 3) || pid != CAT_PID)
		return;

	try
	{
		novelsuper::psisi_parse::CatSection *pcat = (novelsuper::psisi_parse::CatSection *)data;

		U8 iCatVersion = pcat->version();


		for( novelsuper::psisi_parse::CA_I q = pcat->begin<novelsuper::psisi_parse::DescCA>(); ! q.empty(); ++q )
		{
			U32 emmPid = q->ca_pid();
			//处理数码CA
			if(q->ca_system_id() == 0x4ad2)
			{
				dxreport("CATPrivateDataGotCallBack emmpid %d\n",emmPid);
				//通知CA库.
				DVTCASTB_SetEmmPid(emmPid);
				//删除filter
				StopRetrieveEMM();
			}
		}
	}
	catch(novelsuper::psisi_parse::RuntimeError &e)
	{
		dxreport("CATPrivateDataGotCallBack RuntimeError!!! %s\n",e.what());
		return;
	}
}

bool StartRetrieveEMM()
{
	dxreport("Enter StartRetrieveEMM\n");

	if(gCATSecFilter != NULL)
		StopRetrieveEMM();

	utSectionFilter filter;
	memset(&filter, 0, sizeof(filter));

	filter.iTuner= 0;
	filter.pid 	= CAT_PID;
	filter.tid 	= NO_TABLE_ID;
	filter.timeout = 0xFFFFFFFF;
	filter.filterData[0] = TableId_Cat;
	filter.filterMask[0] = 0xff;

	HANDLE handle = tsdemux_addSectionFilter(&filter,1,CATPrivateDataGotCallBack,NULL);
	dxreport("Leave StartRetrieveEMM filter added, handle:0x%x\n",handle);
	gCATSecFilter = handle;
	return true;
}

bool SendAreaLockMsg(U32 status)
{
	J_DataObject tre;
	J_NVItem ro, child1;

	ro.name = "/";
	child1.name = "arealock_status";
	child1.value = status;

	J_DataObject::iterator_base it = tre.set_root(ro);
	tre.append_child(it, child1);

	if(gStbCa.pCaMsgCallBack_)
		gStbCa.pCaMsgCallBack_(SHUMA_MSG,TVNOTIFY_AREAlOCK,&tre);

	return true;
}

bool GetFreqInfo(U32 & freq, U32 & symb, U32 & qam)
{
	freq = 0;
	symb = 0;
	qam = 0;

	FILE * versionF = fopen(AREALOCk_CONFIG_FILE,"r");
	if(versionF == NULL)
		return false;

	char buf[256] = "\0";
	while(true)
	{
		memset(buf, 0, sizeof(buf));
		char * pKV = fgets(buf,255,versionF);
		if(pKV == NULL)
			break;
		int v = 0xFF;
		char * pV = strchr(pKV, ':');
		if(pV != NULL)
		{
			sscanf(++pV,"%d",&v);
		}	
		if(strstr(pKV,"freq"))
		{
			freq = v;
		}
		else if(strstr(pKV,"symb"))
		{
			symb = v;
		}
		else if(strncmp(pKV,"qam",strlen("qam")) == 0)
		{
			qam = v;
		}
	}
	fclose(versionF);
	return true;
}


void * AreaLockFunc(void * param)
{
	simplethread *pThread = reinterpret_cast<simplethread*>(param);
	//毫秒
	U32 duration = (U32)(pThread->get_arglist()) * 1000;

	gAreaLockOK = false;

	U32 startTime = 0;
	bool lockRet = false;
	TunerSignal signal_;
	U32 freq, symb, qam;
	freq = symb = qam = 0;
	GetFreqInfo(freq, symb, qam);

	//1.通知上层应用
	//	1).显示提示消息
	//	2).停止播放
	//	3).禁止调台
	//	4).显示背景图
	SendAreaLockMsg(AREALOCK_BEGIN);
	//等待上层处理消息
	NS_sleep(10);
		
	//2.设置状态,忽略CA消息.
	gStbCa.ignoreCAMsg = true;
	//3.锁频.
	if(gStbCa.hTVDevice_ == NULL)
	{
		dxreport("%s device is null\n",__FUNCTION__);
		DVTCASTB_SwitchChannelOver(2);
		goto END;
	}

	lockRet = tvdevice_tune(gStbCa.hTVDevice_,freq,symb,qam);
	if(lockRet == false)
	{
		dxreport("%s lock failed\n",__FUNCTION__);
		DVTCASTB_SwitchChannelOver(2);
		goto END;
	}

	NS_sleep(10);
	lockRet = tvdevice_getTunerSignalStatus(gStbCa.hTVDevice_,&signal_);
	if(!lockRet || !signal_.locked) 
	{
		dxreport("%s get tuner signal status failed \n",__FUNCTION__);
		DVTCASTB_SwitchChannelOver(2);
		goto END;
	}
	
	//设置全码流
	tvdevice_setPidFilter(gStbCa.hTVDevice_,NULL,0);

	startTime = NS_GetTickCount();
	//4.处理CAT表.
	StartRetrieveEMM();

	//5.等待CA处理
	while(!pThread->check_stop())
	{
		NS_sleep(500);

		//是否完成
		if(gAreaLockOK)
		{
			dxreport("%s break ,area lock ok\n",__FUNCTION__);
			break;
		}

		//是否超时
		U32 now = NS_GetTickCount();
		if(now - startTime > duration)
		{
			dxreport("%s break while timeout!\n",__FUNCTION__);
			DVTCASTB_SwitchChannelOver(1);
			break;
		}
	}

END:
	StopRetrieveEMM();
	gStbCa.ignoreCAMsg = false;
	SendAreaLockMsg(AREALOCK_END_OK);
	return 0;
}


void * testFunc(void*)
{
	NS_sleep(1000 * 20);
	DVTSTBCA_SwitchChannel(20);
	return NULL;
}

int testAreaLockFreq()
{
//	pthread_t pt;
//	pthread_create(&pt, NULL, testFunc, NULL);
	return 1;
}


