// 中文 for vim
#ifndef NOVELSUPERTV_ISEARCHTVNOTIFY_DEFINE_H
#define NOVELSUPERTV_ISEARCHTVNOTIFY_DEFINE_H

#include "tvcomm.h"
//#include "searchDef.h"

// 搜索频道回调接口
class ISearchTVNotify
{
  public:
	virtual ~ISearchTVNotify(){};
    // 单频点搜索结果
	virtual void OnDVBService(std::vector<DVBService> &services)=0;
	// 全频搜索时的进度条 
    virtual void OnProgress(U32 iPercent)=0;
	// 全频搜索时的频点信息(开始搜索的频点及信号强度等)
	virtual void OnTunerInfo(const TuningParam& tuning,const TunerSignal& signal)=0;
	// 频道搜索完成通知
	virtual void OnSearchTVComplete(std::vector<DVBService> &services, std::vector<ServiceTypeTableItem> &table)=0;
	// 节目搜索完成通知
	virtual void OnSEPGComplete()=0;
	// 主频点NIT版本更新通知
	virtual void OnNitVersionChanged(U8 iVersion)=0;
};


#endif  // defined(NOVELSUPERTV_ISEARCHTVNOTIFY_DEFINE_H)
