#include "typ.h"
#include "tvnotify.h"

TVNotifyProxy::TVNotifyProxy(void)
: 
NovelSupertv::stbruntime::Thread("TVNotifyProxy",64*1024),
RealNotify_(0)
{
}


TVNotifyProxy::TVNotifyProxy(ISearchTVNotify *Notify)
:
NovelSupertv::stbruntime::Thread("TVNotifyProxy",64*1024),
RealNotify_(Notify)
{
//	start();
}

TVNotifyProxy::~TVNotifyProxy(void)
{
//	stop();
}

void TVNotifyProxy::OnDVBService(std::vector<DVBService> &services)
{
	RealNotify_->OnDVBService(services);
}
void TVNotifyProxy::OnProgress(U32 iPercent)
{
	RealNotify_->OnProgress(iPercent);
}
void TVNotifyProxy::OnTunerInfo(const TuningParam& tuning,const TunerSignal& signal)
{
	RealNotify_->OnTunerInfo(tuning,signal);
}
void TVNotifyProxy::OnSearchTVComplete(std::vector<DVBService> &services, std::vector<ServiceTypeTableItem> &table)
{
	RealNotify_->OnSearchTVComplete(services,table);
}
void TVNotifyProxy::OnSEPGComplete()
{
	RealNotify_->OnSEPGComplete();
}
void TVNotifyProxy::OnNitVersionChanged(U8 iVersion)
{
	RealNotify_->OnNitVersionChanged(iVersion);
}
/*
void TVNotifyProxy::OnSubscriptionExpiry(U32 iId,TVSubscriptionT& info)
{
	RealNotify_->OnSubscriptionExpiry(iId,info);
}
*/
/*
void TVNotifyProxy::OnCAMessage(U8 type,const CAMessageT* pMSG)
{
	NovelSupertv::stbruntime::Lock<NovelSupertv::stbruntime::Mutex> lk(Mutex_);
	bool bAdd = true;
	if(type==MSG_CANNOT_PLAY_PROGRAM
		|| type==MSG_NEW_EMAIL
		|| type==MSG_SHOW_OSD
		|| type==MSG_HIDE_OSD
		|| type==MSG_SHOW_FINGERPRINT
		|| type==MSG_EPG_UPDATED)
	{
		for(::std::list<Msg>::iterator it=Messages_.begin(); it!=Messages_.end(); it++)
		{
			if((*it).ty==tyOnCAMessage && (*it).wParam==type)
			{
				bAdd=false;
				if(pMSG) {
					CAMessageT * caMsg=(CAMessageT *)((*it).lParam);
					caMsg->message=pMSG->message;
					caMsg->top=pMSG->top;
					caMsg->visible=pMSG->visible;
					caMsg->majortype = pMSG->majortype;
					caMsg->subtype   = pMSG->subtype;
				}
				else
					(*it).lParam=0;
				break;
			}
		}
	}
	if(bAdd) {
		CAMessageT* caMessage=0;
		if(pMSG)
			caMessage=new CAMessageT(pMSG->message,pMSG->visible,pMSG->top);
		Msg msg;
		msg.ty=tyOnCAMessage;
		msg.wParam=type;
		msg.lParam=caMessage;
		Messages_.push_back(msg);
	}

	Event_.unlock();
}
void TVNotifyProxy::OnEpgEventChange(EITEventType type)
{
	Msg msg;
	msg.ty=tyOnEpgEventChange;
	msg.wParam=(U32)type;
	msg.lParam=0;
	NovelSupertv::stbruntime::Lock<NovelSupertv::stbruntime::Mutex> lk(Mutex_);
	Messages_.push_back(msg);
	Event_.unlock();
}
*/
U32 TVNotifyProxy::do_run()
{
	/*		
	while( !signalled() )
	{
		Event_.lock(500);
		while(!signalled() && !Messages_.empty())
		{
			Msg msg;
			{
				NovelSupertv::stbruntime::Lock<NovelSupertv::stbruntime::Mutex> lk(Mutex_);
				msg = Messages_.front();
				Messages_.pop_front();
			}
*/			
/*
			switch(msg.ty)
			{
			case tyOnCAMessage:
				RealNotify_->OnCAMessage((U8)msg.wParam,(CAMessageT*)msg.lParam);
				if(msg.lParam)
					delete (CAMessageT*)msg.lParam;
				break;
			case tyOnEpgEventChange:
				RealNotify_->OnEpgEventChange((EITEventType)msg.wParam);
				break;
			}
		}
	}
	*/
	
	return 0;
}

