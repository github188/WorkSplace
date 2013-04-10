#include "epgsearcher.h"
#include "tvcomm.h"
#include "ucsconvert.h"
#include "datetime.h"
#include "tvepg.h"
#include "tvdevice.h"

#include <all.h>
#include <imp/pre.h>

#define  LOG_TAG  "libtvplayer"
#include "tvlog.h"

namespace PSISI = novelsuper::psisi_parse;

namespace TVCoreUtil{

static EpgController* gpEpgController = 0;
void correct_text( std::string& text )
{
	if( !text.length() ) 
		return;

	if( text[0] >= 0x0 && text[0]<= 0x13 )
	{

		if( text[0] != 0x11)
			text.erase( 0, 1 );
		else
		{
			text.erase( 0, 1 );
			/*U8* data = new U8[text.size() + 2];*/
			// unsigned char *data = new unsigned char[text.size() + 2];
			unsigned char data[512];
			
			for( size_t i = 0; i < text.size(); i += 2 )
			{
				data[i] = text.at(i+1);
				data[i+1] = text.at(i);
			}
			data[text.size()] = 0;
			data[text.size()+1] = 0;

			char gbk[255];
			/*UnicodeToGBK( reinterpret_cast<U16*>(data), gbk );*/
			UnicodeToGBK( reinterpret_cast<unsigned short*>(data), gbk );
			text.assign( gbk );
			// delete []data;	
		}
	}

	size_t len=strlen(text.c_str());
	text.resize(len);
}

EpgController::EpgController(HANDLE hDevice,TVNOTIFY pNotify,TVTaskMgr* pTaskMgr/* =0 */)
	: m_hDevice(hDevice),m_pNotify(pNotify)
{
	if(0 == pTaskMgr){
		// 创建任务管理器
		m_bActiveTaskMgr = true;
		m_pTaskMgr = new TVTaskMgr();
	}
	else{
		m_bActiveTaskMgr = false;
		m_pTaskMgr = pTaskMgr;
	}

	gpEpgController = this;
}

EpgController::~EpgController()
{
	if(m_bActiveTaskMgr){
		delete m_pTaskMgr;
		m_bActiveTaskMgr = false;
	}
	
	m_listTasks.clear();
	m_pTaskMgr = 0;
	gpEpgController = 0;
}

// 主动开始EPG搜索任务
HANDLE EpgController::startEpgSearch(const TuningParam& tuning,const U16 sid,const EITEventType evtType)
{
	bool   bRet  = false;
	HANDLE hTask = 0;
	m_iServiceId = sid;

	LOGTRACE(LOGINFO,"Enter EpgController::startEpgSearch,t0=%d.\n",NS_GetTickCount());

	// 默认为当前频点不需要调频 
	if(0 != tuning.freq && 0 != tuning.qam && 0 != tuning.symb){
		bRet = tvdevice_tune(m_hDevice,tuning.freq,tuning.symb,tuning.qam);
		if(!bRet){
			LOGTRACE(LOGINFO,"EpgController::startEpgSearch,tune(%d,%d,%d) failed.\n",
				tuning.freq,tuning.symb,tuning.qam);

			return 0;
		}
	}

	TVTask* pEpgTask = 0;	
	if(EIT_EVENT_PF == evtType){
		pEpgTask = new PFEventSearchTask(m_pTaskMgr,sid,evtType,m_pNotify);
	}
	else{
		pEpgTask = new EpgSearchTask(this,m_pTaskMgr,sid,evtType);
	}

	m_pTaskMgr->add_task(pEpgTask);
	m_listTasks.insert(pEpgTask);

	hTask = static_cast<HANDLE>(pEpgTask);
	LOGTRACE(LOGINFO,"Leave EpgController::startEpgSearch,handle=%p.\n",hTask);

	return hTask;
}

// 主动取消EPG搜索任务
void EpgController::cancelEpgSearch(HANDLE hTask)
{
	std::set<TVTask*>::iterator it = m_listTasks.find(static_cast<TVTask*>(hTask));
	if(it != m_listTasks.end())
	{
		TVTask* pTask = static_cast<TVTask*>(*it);
		m_pTaskMgr->del_task(pTask);
		pTask = 0;
		m_listTasks.erase(it);
	} 
}

// 获取EPG全部数据
bool EpgController::getEpgData(EPGDataBaseT& epgs)
{
	{
		AutoLockT lock(m_mapEpgMutex_);
		epgs = m_mapEpgDB;
	}
	return true;
}

// 获取指定服务下的节目(事件)信息
bool EpgController::getEpgDataBySID(const U16 iServiceId,EpgEventSet& events)
{
	bool bRet = false;
	EPGDataBaseT epgdb;
	{
		AutoLockT lock(m_mapEpgMutex_);
		epgdb = m_mapEpgDB;
	}

	EPGDataBaseT::iterator it = epgdb.find(iServiceId);
	if(it != epgdb.end())
	{
		events = it->second.events;
	}
	return bRet;
}

// 获取指定服务下指定时间段的节目(事件)信息
bool EpgController::getEpgDataByDuration(const U16 iServiceId,EpgEventSet& events,const U32 iStartTime,const U32 iEndTime)
{
	bool bRet = false;
	EPGDataBaseT epgdb;
	{
		AutoLockT lock(m_mapEpgMutex_);
		epgdb = m_mapEpgDB;
	}
	EPGDataBaseT::iterator it = epgdb.find(iServiceId);
	if(it != epgdb.end())
	{
		LOGTRACE(LOGINFO,"getEpgDataByDuration,ServiceId=%u(%u).\n",iServiceId,it->second.events.size());

		EpgEventSet::iterator itProg = it->second.events.begin();
		U32 iEndTime_ = (0 == iEndTime) ? (iStartTime+86400) : iEndTime;
		for (;itProg != it->second.events.end();itProg++)
		{
			if((itProg->start_time >= iStartTime && itProg->start_time < iEndTime_) ||
			   (itProg->end_time > iStartTime && itProg->end_time <= iEndTime_)
			  )
			{
				events.insert(*itProg);
			}
		}

		LOGTRACE(LOGINFO,"getEpgDataByDuration,matched event count=%u\n",events.size());
		bRet = true;
	}

	return bRet ;
}


//收到一张EIT表,先把数据更新过来
void EpgController::OnEpgDataGot(void * wParam, void * pParam)
{
	//当前表的epg信息
	EpgEventSet * pEvents = (EpgEventSet*)pParam;
	U16 sid = *(U16*)wParam;
	//先不处理版本号检查
	{
		AutoLockT lock(m_mapEpgMutex_);
		m_mapEpgDB[sid].events.insert(pEvents->begin(), pEvents->end());
	}
}

//目前的EPG,如果一直开机,跨周情况下,版本号不变,会出现上周的egp信息还存在内存中的问题

//超时完成,或某个频道搜索完成
void EpgController::OnEpgCompletion(void* wParam,void* pParam)
{
	EPGDataBaseT* pEPGDB = static_cast<EPGDataBaseT*>(pParam);


	if(pEPGDB)
	{
		EPGDataBaseT epgdb;
		{
			AutoLockT lock(m_mapEpgMutex_);
			epgdb = m_mapEpgDB;
		}
		for (EPGDataBaseT::iterator it =pEPGDB->begin();it != pEPGDB->end() ; it++)
		{	
			// ??? 周六、周日的数据版本会变化么?
//			LOGTRACE(LOGINFO,"m_DefEPGInfo[%d].EventsVer=%d;it->second.EventsVer=%d events=%d\n",
//				it->first,epgdb[it->first].EventsVer,it->second.EventsVer, it->second.events.size());

			// EventsVer:0-31
			U32 iOldVersion = epgdb[it->first].EventsVer ;
			if(iOldVersion != it->second.EventsVer)
			{
				epgdb[it->first].events.clear();
			}

			U16 iSID = it->first ;
			EpgEventSet &events_ = it->second.events;
			//如果最新event大于以前搜索到的event个数
			//1.版本没有变化,以前搜索漏掉(Section CRC失败)
			//2.版本变化,以前搜索会清空
			//3.最新搜到的event个数少于等于以前的,绝对一样,除非event变化,而版本没变
//			LOGTRACE(LOGINFO,"events_ = %d, map_events = %d\n",events_.size() ,epgdb[iSID].events.size());
			if(events_.size() > epgdb[iSID].events.size())
			{
				EpgEventSet::iterator it1 = events_.begin() ;
				for (; it1 != events_.end();it1++)
				{
					epgdb[iSID].events.insert(*it1);
				}
			}

			epgdb[iSID].sid = iSID;
			epgdb[iSID].PresentVer = it->second.PresentVer;
			epgdb[iSID].Present    = it->second.Present;

			epgdb[iSID].FollowingVer = it->second.FollowingVer;
			epgdb[iSID].Following    = it->second.Following;
			epgdb[iSID].EventsVer    = it->second.EventsVer;
		}

		{
			AutoLockT lock(m_mapEpgMutex_);
			m_mapEpgDB = epgdb;
		}
		if(m_pNotify){
//			LOGTRACE(LOGINFO,"Send 'TVNOTIFY_EPGCOMPLETE' Message,t=%d.\n",NS_GetTickCount());
			//如果超时完成,sid传
			//当前频道或当前频点全部完成.当前频点的其它频道不处理
			if(m_iServiceId == (U16) *(long *)wParam ||
					(U16) *(long *)wParam == INVALID_SID)
				m_pNotify(TVNOTIFY_EPGCOMPLETE,0,0);
//			LOGTRACE(LOGINFO,"Exit 'TVNOTIFY_EPGCOMPLETE' Message,t=%d.\n",NS_GetTickCount());
		}
		else{
			LOGTRACE(LOGINFO,"CallBack Object is nil.\n");
		}
	}
	else{
		LOGTRACE(LOGINFO,"EpgController::OnEpgCompletion,parameter error.\n");
	}
}

void EpgController::NotifyChange(TVNOTIFY pNotify)
{
	m_pNotify = pNotify;
}

bool EpgSearchTask::BuildFilter()
{
	bool bRet = true;

	FilterListT listFilter_;
	bRet = tvepg_getEpgFilters(m_evtType,listFilter_);
	
	LOGTRACE(LOGINFO,"filters number:%d\n",listFilter_.size());
	
	if(!bRet || listFilter_.empty())
	{
		return false;
	}

	U32 iMaxTimeout = 0;
	TSFilter* pTSFilter = 0;

	U32 lFilterCount = listFilter_.size();
	utSectionFilter *pFilters  = new utSectionFilter[lFilterCount];
	for (U32 i = 0 ; i < listFilter_.size() ; i++)
	{
		pFilters[i].iTuner = 0;
		pFilters[i].pid    = listFilter_[i].pid;
		pFilters[i].tid    = NO_TABLE_ID;
		pFilters[i].timeout= listFilter_[i].timeout;
		memcpy(pFilters[i].filterData,listFilter_[i].data,sizeof(listFilter_[i].data)/sizeof(U8));
		memcpy(pFilters[i].filterMask,listFilter_[i].mask,sizeof(listFilter_[i].mask)/sizeof(U8));

		// 本批次过滤器最大超时值
		if(listFilter_[i].timeout > iMaxTimeout)
			iMaxTimeout = listFilter_[i].timeout;
	}

	pTSFilter = new TSFilter(this,pFilters,lFilterCount);
	pTSFilter->start_search();
	m_listTSFilter.push_back(pTSFilter);

	if(m_iTime1 <= 0 )
	{
		m_iTime1 = NS_GetTickCount();
		m_iMaxTimeout = iMaxTimeout;		// 最大超期时间
	}
	else
	{
		m_iTime2 = NS_GetTickCount();
		U32 iInterval = m_iTime2-m_iTime1 ;	// 实际耗费时间
		if(m_iMaxTimeout > iInterval)
		{
			U32 iAvailableTime = m_iMaxTimeout-iInterval; // 剩余时间
			if(iMaxTimeout > iAvailableTime)
			{
				m_iMaxTimeout +=(iMaxTimeout-iAvailableTime);
			}
		}
		else
		{
			// 已超期,无需修正时间
		}

		m_iTime1 = m_iTime2;
		LOGTRACE(LOGINFO,"m_iMaxTimeout=%d,m_t2-m_t1=%d\n",m_iMaxTimeout,iInterval);
	}
	
	return bRet; 
}

void   EpgSearchTask::ClearFilter()
{
	LOGTRACE(LOGINFO,"Enter EpgSearchTask::ClearFilter...\n");

	for(int i = 0 ;i < (int)m_listTSFilter.size() ;i++)
	{
		TSFilter*	pTSFilter = m_listTSFilter[i];
		pTSFilter->stop_search();
		delete pTSFilter ;
		pTSFilter = 0;
	}
	
	m_listTSFilter.clear();
	m_iTime1 = m_iTime2 = 0 ;
	
	LOGTRACE(LOGINFO,"Leave EpgSearchTask::ClearFilter...\n");
}

bool EpgSearchTask::task_start()
{
	bool bRet = false ;
	LOGTRACE(LOGINFO,"Enter EpgSearchTask::task_start.\n");

	tvepg_init(m_iServiceId,TVepgCallBack);

	// 构建过滤器
	BuildFilter();
	m_iStarttime = NS_GetTickCount();
	bRet = true ;

	LOGTRACE(LOGINFO,"Leave EpgSearchTask::task_start.\n");
	return bRet ;
}


bool EpgSearchTask::task_cancel()
{
	LOGTRACE(LOGINFO,"Enter EpgSearchTask::task_cancel.\n");

	ClearFilter();
	tvepg_uninit();

	LOGTRACE(LOGINFO,"Leave EpgSearchTask::task_cancel.\n");
	return true ;
}

// 任务处理
bool EpgSearchTask::task_process(const U16 pid,U8 const* buffer, U32 size)
{
	bool bRet = false;

	// LOGTRACE(LOGINFO,"Enter EpgSearchTask::task_process.\n");

	// 0 : continue 1: table complete 2 : freq complete 3: nit table complete 
	// 4 : updated table 
	int iFlag = 0 ; 
	
	try{
		
		U8 table_id = buffer[0];
		iFlag = tvepg_putSectionData(pid,buffer,size); 
		switch (iFlag)
		{
		case 1:
			/// 某表分析完成
	//		LOGTRACE(LOGINFO,"tableid=0x%x parsecomplete\n",table_id);
			//ReleaseFilter(pid,table_id);

			// P/F表时提前通知(针对同时获取时不能等任务结束才通知P/F)
			//if(0x4E == table_id || 0x4F == table_id)
			//{
			//	task_complete();
			//}
			break;
		case 2:
	//		LOGTRACE(LOGINFO,"pid=0x%x parsecomplete\n",table_id);
			bRet = true ; 
			break;
		case 4:
			LOGTRACE(LOGINFO,"request update filter\n");
			ClearFilter();
			NS_sleep(50);
			BuildFilter(); // 考虑其他表
			break;
		default:
			break;
		}
	}
	catch(...)
	{
		LOGTRACE(LOGINFO,"unknown exception in put_section_data().\n");
	}

	//LOGTRACE(LOGINFO,"Leave EpgSearchTask::task_process.\n");
	return bRet ;
}

bool EpgSearchTask::task_complete(bool bIsTimeout /* = false */)
{
	bool bRet = false;

	LOGTRACE(LOGINFO,"EpgSearchTask::task_complete,t1=%d.\n",NS_GetTickCount());

	EPGDataBaseT epgs;
	bRet = tvepg_getEpgData(epgs);

	// 通知获取EPG数据
	if(m_pController){
		long temp = INVALID_SID;
		m_pController->OnEpgCompletion(&temp, static_cast<void*>(&epgs));
		bRet = true;
	}
	
	return bRet;
}


int EpgSearchTask::TVepgCallBack(int notifyCode,long lParam,void *pParam)
{
//	LOGTRACE(LOGINFO,"Enter EpgSearchTask::TVepgCallBack.%d\n",notifyCode);

	// 通知获取EPG数据
	if(gpEpgController){
		if(notifyCode == 0x1000)
		{
			gpEpgController->OnEpgDataGot(&lParam, pParam);
		}
		else if(notifyCode == TVNOTIFY_EPGCOMPLETE)
			gpEpgController->OnEpgCompletion(&lParam,pParam);
	}
	
	return 0;
}

//====================MINI EPG ===================================
#define PF_TIMEOUT (0xffff)
bool PFEventSearchTask::BuildFilter()
{
	bool hr = true;

	LOGTRACE(LOGINFO,"Enter PFEventSearchTask::BuildFilter.\n");
	
	utSectionFilter pf_filter;
	pf_filter.iTuner  =0;
	pf_filter.pid = EIT_PID;	//eit'pid
	pf_filter.tid = NO_TABLE_ID;
	pf_filter.timeout = PF_TIMEOUT;
	
	memset(pf_filter.filterData,0,sizeof(pf_filter.filterData)/sizeof(U8));
	memset(pf_filter.filterMask,0,sizeof(pf_filter.filterData)/sizeof(U8));

	pf_filter.filterData[0] = 0x4e;
	pf_filter.filterMask[0] = 0xfe;
	
	// ServiceID作为过滤字段接收数据
	pf_filter.filterData[1] = (m_iCurServiceId >> 8) & 0xff;
	pf_filter.filterData[2] = m_iCurServiceId & 0xff ;
	pf_filter.filterMask[1] = 0xff;
	pf_filter.filterMask[2] = 0xff;

	m_pTSFilter = new TSFilter(this,&pf_filter,1);
	m_pTSFilter->start_search();
	m_iMaxTimeout = PF_TIMEOUT;

	LOGTRACE(LOGINFO,"Leave PFEventSearchTask::BuildFilter.\n");
	return hr; 
}

void	PFEventSearchTask::ClearFilter()
{
	LOGTRACE(LOGINFO,"Enter PFEventSearchTask::ClearFilter.\n");
	if(NULL != m_pTSFilter)
	{
		m_pTSFilter->stop_search();
		delete m_pTSFilter ;
		m_pTSFilter = NULL;
	}
	
	m_epgs.clear();

	LOGTRACE(LOGINFO,"Leave EpgSearchTask::ClearFilter.\n");
}

bool PFEventSearchTask::task_start()
{
	LOGTRACE(LOGINFO,"Enter PFEpgSearchTask::task_start.\n");

	// 构建过滤器
	BuildFilter();
	m_iStarttime = NS_GetTickCount();

	LOGTRACE(LOGINFO,"Leave PFEpgSearchTask::task_start.\n");
	return true ;
}

bool PFEventSearchTask::task_cancel()
{
	ClearFilter();
	return true ;
}

bool PFEventSearchTask::task_process(const U16 pid,U8 const* buffer, U32 size)
{
	bool bRet = false;

	LOGTRACE(LOGINFO,"Enter PFEventSearchTask::task_process buffer size=0x%x\n",size);
	if((0 == buffer) || (size <= 14))
	{
		return bRet;
	}

	PSISI::EitSection *eit = (PSISI::EitSection*)buffer;
	//by chehl:other talbe(0x4f)
	if((eit->table_id() != 0x4e)/* && (eit->table_id() != 0x4f)*/)
	{
		return bRet;
	}
	
	U32 uCRCValue = PSISI::n2h32(eit->CRC_32());
	if(m_uCRCValue == uCRCValue){
		return bRet;
	}
	else{
		m_uCRCValue = uCRCValue;
	}

	try {
		PSISI::EitSection::Loop_Iterator it = eit->loop_begin();

		if(it!= eit->loop_end())
		{
			PSISI::ShortEvent_I seit = it->begin<PSISI::DescShortEvent>();
			if(seit!=it->end<PSISI::DescShortEvent>()) {
				EpgEvent pf;
				pf.id = it->event_id();
				pf.name = seit->event_name_char();
				correct_text(pf.name);

				LOGTRACE(LOGINFO,"task_process sid=%d secnum=%d name=%s,version=%d\n",
					eit->service_id(),eit->section_number(),pf.name.c_str(),eit->version());

				StbTime st(it->start_time().utctime);
				pf.start_time = st.make_time();
				pf.end_time = pf.start_time+it->duration().totalseconds();
				pf.description = seit->text_char();
				correct_text(pf.description);

				U16 iSID = eit->service_id() ;
				U8  iVersion = eit->version();
				U8  iSectionNumber = eit->section_number();

				ProgramEpg NullProg;
				m_epgs.insert(EPGDataBaseT::value_type(iSID,NullProg));	
				EPGDataBaseT::iterator it = m_epgs.find(iSID);
				if(m_epgs.end() != it)
				{
					if(0 == iSectionNumber){
						it->second.Present	 = pf;
						it->second.PresentVer= iVersion;
					}
					else{
						it->second.Following	= pf ;
						it->second.FollowingVer = iVersion ; 	
					}

					if(it->second.Present.start_time  && 
					   it->second.Following.start_time)
					{
						LOGTRACE(LOGINFO,"[%d],EIT P/F Completed\n",iSID);
						bRet = true ;
					}
				}
			}
		}
	}
	catch(PSISI::RuntimeError &e)
	{
		LOGTRACE(LOGINFO,"PFEventSearchTask::task_process exception:%s.\n",e.what());
	}

	LOGTRACE(LOGINFO,"Leave PFEventSearchTask::task_process.\n");
	return bRet;
}

bool PFEventSearchTask::task_complete(bool bIsTimeout /* = false */)
{
	bool bRet = false;
	
	LOGTRACE(LOGINFO,"PFEventSearchTask::task_complete,%d, t1=%d.\n",m_iCurServiceId,NS_GetTickCount());

	EPGDataBaseT::iterator it = m_epgs.find(m_iCurServiceId);
	if(it != m_epgs.end())
	{
		MiniEPGNotify info_;
		
		info_.ServiceID = m_iCurServiceId;
		size_t iBufferLength = sizeof(info_.CurrentEventName)/sizeof(info_.CurrentEventName[0]);
		size_t iCopyLength = it->second.Present.name.size();
		
		memset(info_.NextEventName,0,iBufferLength);
		memset(info_.CurrentEventName,0,iBufferLength);
		
		iCopyLength = (iCopyLength > iBufferLength) ? iBufferLength : iCopyLength;		
		memcpy(info_.CurrentEventName,it->second.Present.name.c_str(),iCopyLength);
		info_.CurrentEventStartTime = it->second.Present.start_time;
		info_.CurrentEventEndTime   = it->second.Present.end_time;
		
		iCopyLength = it->second.Following.name.size();
		iCopyLength = (iCopyLength > iBufferLength) ? iBufferLength : iCopyLength;		
		memcpy(info_.NextEventName,it->second.Following.name.c_str(),iCopyLength);
		info_.NextEventStartTime = it->second.Following.start_time;
		info_.NextEventEndTime   = it->second.Following.end_time;
		
		if(0 != m_pNotify)
		{
			m_pNotify(TVNOTIFY_MINEPG,0,static_cast<void*>(&info_));
			LOGTRACE(LOGINFO,"Completed epg notification,t2=%d.\n",NS_GetTickCount());
		}
		bRet = true;
	}

	return bRet;
}

}
