#pragma once

#include "isearchtvnotify.h"
#include "xprocess.h"
#include <list>

class TVNotifyProxy : 	private NovelSupertv::stbruntime::Thread
{
public:

	TVNotifyProxy(ISearchTVNotify *Notify);
	~TVNotifyProxy(void);

	virtual void OnDVBService(std::vector<DVBService> &services);
	virtual void OnProgress(U32 iPercent);
	virtual void OnTunerInfo(const TuningParam& tuning,const TunerSignal& signal);
	virtual void OnSearchTVComplete(std::vector<DVBService> &services, std::vector<ServiceTypeTableItem> &table);
	virtual void OnSEPGComplete();
	virtual void OnNitVersionChanged(U8 iVersion);
//	virtual void OnSubscriptionExpiry(U32 iId,TVSubscriptionT& info);

	//CAmessage 及EpgEvent 为什么要采用队列呢?
//	virtual void OnCAMessage(U8 type,const CAMessageT* pMSG);
//	virtual void OnEpgEventChange(EITEventType type);

private:

	enum{
		tyOnCAMessage,
		tyOnEpgEventChange
	};

	struct Msg	{
		U32 ty;
		U32 wParam;
		void *lParam;
	};

	TVNotifyProxy(void);
	virtual U32 do_run();

	ISearchTVNotify *RealNotify_;
	::std::list<Msg> Messages_;
	NovelSupertv::stbruntime::Mutex Mutex_;
	NovelSupertv::stbruntime::Event Event_;
};

