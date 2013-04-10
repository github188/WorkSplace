#ifndef __TVCONFIG_H__
#define __TVCONFIG_H__

#include "tvnotify.h"
#include "tvsearchmgr.h"
#include "xml.h"
#include "xmlparser.h"

typedef struct _GLOBAL_CONFIG_PARAM
{

	U8			iExclusive_;				///<独占模式(目前:EPG和搜索不能同时进行)
	BOOL		 bServiceChanged;			///<节目已更新(定时更新至本地文件)
	DVBService	 curDvbService;				///<当前频道数据
	DVBServiceListT  defServices_;			///<当前已加载的全部节目清单
	std::vector<ServiceTypeTableItem> stTable_;
	// TV搜索相关参数
	BOOL			bForceStop;		///<主动取消
	STVMode         currentSTVMode_;		///<当前搜索模式(搜索时保存临时参数)
	TuningParam     inputTunningParam;	///<调用传来的调频参数(搜索时保存临时参数)

	TVNotifyProxy *pProxyNotify_;				///<通知回调函数指针
	TVSearchMgr	    objTvSearchMgr_;			///<搜索管理对象
	MutexT			searchMgrMutex;
	
	_GLOBAL_CONFIG_PARAM():iExclusive_(0),bForceStop(FALSE)
	{
		pProxyNotify_ = NULL;
		bServiceChanged=FALSE;
	}

}GlobalConfigParamT,*PGlobalConfigParamT;

#endif // defined(__TVCONFIG_H__)
