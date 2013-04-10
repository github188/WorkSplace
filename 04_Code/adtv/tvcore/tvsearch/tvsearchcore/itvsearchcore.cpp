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
// ��ʼƵ������(�첽)
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
// �й�Ƶ������ �Ķ���ӿں���
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

// ȫƵģʽʱpTuningParam �����Ƶ�㱻����.
// �ֶ�ģʽ��NITģʽʱpTuningParam���������Ƶ��)
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
		//full search mode ���nit ����û����;,���Ը�Ϊnit ������Ҫ����Ƶ�����
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

// ȡ��Ƶ������
TVSEARCHCORE_API ULONG CancelSearchTV()
{
	AutoLockT lock(gConfig.searchMgrMutex);
	dxreport("CancelSearchTV Begin >>>\n");
	ULONG hr = 0;

	gConfig.bForceStop = TRUE;
	gConfig.objTvSearchMgr_.Cancel() ;   // ֹͣ�����߳�
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

// ����/��ȡͨ��ģʽ
TVSEARCHCORE_API int SetParameter(int key, const void* request,int reqLength)
{
	return LC_SetParameter(key, request, reqLength);
}

TVSEARCHCORE_API int GetParameter(int key, void* reply,  int* replyLength)
{
	return LC_GetParameter(key, reply, replyLength);
}

