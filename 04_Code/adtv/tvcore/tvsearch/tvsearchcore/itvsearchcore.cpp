#include "tvcomm.h"
#include "itvsearchcore.h"
#include "tvsearchlocal.h"
#include "tvsearchmgr.h"
#include "tvnotify.h"
#include "tvconfig.h"

#define LOG_TAG "tvsearch"
#define LOG_LEVEL LOG_TRACE
#include "dxreport.h"


#ifdef _MANAGED
#pragma managed(push, off)
#endif

void ClearExclusive();
bool Exclusive();
// 开始频道搜索(异步)
UINT  AsyncStartSTV();

GlobalConfigParamT gConfig;

BOOL __stdcall DllMain( /*HMODULE hModule*/ void *hModule,
		DWORD  ul_reason_for_call,
		LPVOID lpReserved
		)
{
	BOOL bRet = TRUE ;
	return bRet;
}

#ifdef _MANAGED
#pragma managed(pop)
#endif

///////////////////////////////////////////////////////////////////////////////
// 有关频道搜索 的对外接口函数
///////////////////////////////////////////////////////////////////////////////
ULONG RegNotifyCallBack(IN ISearchTVNotify *pNotify)
{
	if(gConfig.pProxyNotify_) {
		delete gConfig.pProxyNotify_;
		gConfig.pProxyNotify_=0;
	}
	if(pNotify)
		gConfig.pProxyNotify_ = new TVNotifyProxy(pNotify);
	else
		gConfig.pProxyNotify_=0;
	return 0;
}

// 全频模式时pTuningParam 传入的频点被忽略.
// 手动模式或NIT模式时pTuningParam传入的是主频点)
TVSEARCHCORE_API ULONG StartSearchTV(IN STVMode iMode, IN TuningParam *pTuningParam, IN ISearchTVNotify *pNotify)
{
	ULONG hr = -1 ; 

	RegNotifyCallBack(pNotify);
	if(pTuningParam == NULL )
	{
		dxreport("Warning! pTuningParam is null. Leave StartSearchTV, \n");
		return hr ;
	}

	dxreport("StartSearchTV Begin iMode:%d >>>\n",iMode);
	if(Exclusive() == false )
	{
		dxreport("Start... in Exclusive\n");
		
		if(iMode == STVMODE_FULL) 
		{
		//full search mode 相比nit 搜索没有用途,所以改为nit 搜索，要求传主频点参数
			if(ChangeFullModeToNitMode())
			{
				iMode = STVMODE_NIT;
				dxreport("StartSearchTV change iMode, iMode:%d >>>\n",iMode);
			}
		}
		
		gConfig.currentSTVMode_ = iMode;
		memcpy(&(gConfig.inputTunningParam),pTuningParam,sizeof(TuningParam));
		AsyncStartSTV();
	}

	dxreport("StartSearchTV End <<<\n");
	return hr; 
}

// 取消频道搜索
TVSEARCHCORE_API ULONG CancelSearchTV()
{
	AutoLockT lock(gConfig.searchMgrMutex);
	dxreport("CancelSearchTV Begin >>>\n");
	ULONG hr = 0;

	gConfig.bForceStop = TRUE;
	gConfig.objTvSearchMgr_.Cancel() ;   // 停止搜索线程
	dxreport("CancelSearchTV End <<<\n");
	return hr;
}
void ClearExclusive()
{
	gConfig.iExclusive_ = 0 ; 
}
bool Exclusive()
{
	bool hr = (gConfig.iExclusive_ == 0 ) ? false  : true ; 
	gConfig.iExclusive_ = 1; 
	return hr ;
}

// 设置/获取通用模式
TVSEARCHCORE_API int SetParameter(int key, const void* request,int reqLength)
{
	return LC_SetParameter(key, request, reqLength);
}

TVSEARCHCORE_API int GetParameter(int key, void* reply,  int* replyLength)
{
	return LC_GetParameter(key, reply, replyLength);
}

