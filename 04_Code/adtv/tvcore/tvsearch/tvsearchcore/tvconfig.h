#ifndef __TVCONFIG_H__
#define __TVCONFIG_H__

#include "tvnotify.h"
#include "tvsearchmgr.h"
#include "xml.h"
#include "xmlparser.h"

typedef struct _GLOBAL_CONFIG_PARAM
{

	U8			iExclusive_;				///<��ռģʽ(Ŀǰ:EPG����������ͬʱ����)
	BOOL		 bServiceChanged;			///<��Ŀ�Ѹ���(��ʱ�����������ļ�)
	DVBService	 curDvbService;				///<��ǰƵ������
	DVBServiceListT  defServices_;			///<��ǰ�Ѽ��ص�ȫ����Ŀ�嵥
	std::vector<ServiceTypeTableItem> stTable_;
	// TV������ز���
	BOOL			bForceStop;		///<����ȡ��
	STVMode         currentSTVMode_;		///<��ǰ����ģʽ(����ʱ������ʱ����)
	TuningParam     inputTunningParam;	///<���ô����ĵ�Ƶ����(����ʱ������ʱ����)

	TVNotifyProxy *pProxyNotify_;				///<֪ͨ�ص�����ָ��
	TVSearchMgr	    objTvSearchMgr_;			///<�����������
	MutexT			searchMgrMutex;
	
	_GLOBAL_CONFIG_PARAM():iExclusive_(0),bForceStop(FALSE)
	{
		pProxyNotify_ = NULL;
		bServiceChanged=FALSE;
	}

}GlobalConfigParamT,*PGlobalConfigParamT;

#endif // defined(__TVCONFIG_H__)
