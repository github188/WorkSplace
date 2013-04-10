#include "tvsearchcore.h"
#include "tvsearchmgr.h"
#include "tvconfig.h"
#include "tvnotify.h"
#include "capture.h"
#define LOG_TAG "tvsearch"
#define LOG_LEVEL LOG_TRACE
#include "dxreport.h"

#ifdef _MANAGED
#pragma managed(push, off)
#endif

void ClearExclusive();
bool Exclusive();

extern GlobalConfigParamT gConfig;

///////////////////////////////////////////////////////////////////////////////
// 有关频道搜索 的操作
///////////////////////////////////////////////////////////////////////////////

// 搜索的异步过程
UINT  AsyncStartSTV()
{
	ULONG hr = -1;

	dxreport("AsyncStartSTV Begin >>>\n");
	dxreport("init & start TVSearchMgr... \n");
	gConfig.objTvSearchMgr_.Init(gConfig.currentSTVMode_,gConfig.inputTunningParam,gConfig.pProxyNotify_,AsyncStartSTV_CompleteCallBack);
	gConfig.objTvSearchMgr_.Start(gConfig.defServices_);
//	ClearExclusive();
	dxreport("AsyncStartSTV End <<<\n");
	return 0 ;
}
// 搜索完成后，将所有services 转存到gConfig.defservice_
// 设置异步事件信号
ULONG AsyncStartSTV_CompleteCallBack(DVBServiceListT& services, std::vector<ServiceTypeTableItem> &table)
{
	ULONG hr = 0;
	dxreport("AsyncStartSTV_CompleteCallBack Begin >>>\n");

	///根据新要求，主动取消时也更新
	// if(!gConfig.bForceStop)	
	UpdateDVBService(IN services);	// 保存到gConfig.defServices_
	// for display only ++	
	for(UINT i=0; i<gConfig.defServices_.size(); i++)
	{
		dxreport("channel number: %d, serviceID:%d, freq:%d, ExtServiceType:%d, video_ecm_pid:0x%x, audio_ecm_pid0:0x%x\n",\
			gConfig.defServices_[i].channel_number,gConfig.defServices_[i].serviceID, \
			gConfig.defServices_[i].ts.tuning_param.freq, gConfig.defServices_[i].service_type>>24, \
			gConfig.defServices_[i].video_stream.ecm_pid, gConfig.defServices_[i].audio_stream[0].ecm_pid);
	}
	for(UINT i=0; i<table.size();i++)
	{
		dxreport("ExtServiceTypeId:%d, descriptor:%s\n", table[i].serviceType, table[i].serviceName);
	}
	// for display only --
	
	// 通知界面
	if(gConfig.pProxyNotify_ != NULL )
	{
		dxreport("Notify OnSearchTVComplete..., push programs: %d\n",gConfig.defServices_.size());
		gConfig.pProxyNotify_->OnSearchTVComplete(gConfig.defServices_,gConfig.stTable_);
	}
	// 释放搜索资源
	{
			AutoLockT lock(gConfig.searchMgrMutex);
			dxreport("Stop search and Release searchmgr resource...\n");
			gConfig.objTvSearchMgr_.Cancel() ;   // 停止搜索线程
			gConfig.objTvSearchMgr_.UnInit();
		}
	ClearExclusive();
	dxreport("AsyncStartSTV_CompleteCallBack End <<< :DefServices_.size()=%d\n",gConfig.defServices_.size());
	return hr;
}

//
// 同步更新频道信息
void UpdateDVBService(IN DVBServiceListT& services)
{
	dxreport("UpdateDVBService size=%d\n",services.size());
	gConfig.defServices_.clear();
	gConfig.defServices_=services;

	// 频道更新时,Chl所存储的频道可能会不存在
	if(gConfig.defServices_.size() > 0)
	{
		gConfig.curDvbService =  gConfig.defServices_[0];
	}
	
	// 频道信息有更新
	gConfig.bServiceChanged = TRUE ; 
	
}
