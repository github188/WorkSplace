#include "tvepg.h"

#include "datetime.h"
#include "ucsconvert.h"
#include <all.h>
#include <xprocess.h>

#define  LOG_TAG "libtvepg"
#include <tvlog.h>

using namespace novelsuper::psisi_parse;

#define INVALID_EVENT_ID		(0xffff)
#define INVALID_EVENT_SERVICEID (0xffff)
typedef ::std::map<unsigned short,unsigned long> VersionTS;


#define PF_TIMEOUT  (3000)

#ifdef FUZHOU
#define EIT_TIMEOUT (5000)
#else
#define EIT_TIMEOUT (150000)
#endif 

//{{
static bool gbDyncTrace = false;
#define DYNAMICTRACEUNINIT	\
{							\
	gbDyncTrace = false;	\
}

#define DYNAMICTRACEINIT	\
{							\
	FILE * f = fopen("/data/data/novel.supertv.dvb/epgtrace.log","r");	\
	gbDyncTrace = (f != NULL) ? true : false;							\
	if(gbDyncTrace)														\
		fclose(f);														\
}

#define DYNAMICTRACE(...)		\
{								\
	if(gbDyncTrace)				\
		LOGTRACE(__VA_ARGS__);	\
}	
//}}

SECFilter EITFILTERS[]={
	{0x12,{0x4e,0,0,0,0,0,0,0},{0xfe,0,0,0,0,0,0,0}, PF_TIMEOUT},
	{0x12,{0x50,0,0,0,0,0,0,0},{0xf0,0,0,0,0,0,0,0},EIT_TIMEOUT},
	{0x12,{0x60,0,0,0,0,0,0,0},{0xf0,0,0,0,0,0,0,0},EIT_TIMEOUT}
};

SECFilter PATFILTER = { PAT_PID,{TableId_Pat,0,0,0,0,0,0,0},{0xff,0,0,0,0,0,0,0},3500 };

// EIT分析标志
struct EITParseResultT{
	int  iPFCount;
	bool bEITPF_A;
	bool bEITPF_O;
	bool bEITSH_A;
	bool bEITSH_O;
};

/// put_section_data返回值定义
enum PutDataReturnT
{
	RT_CONTINUE = 0,	///<无效或不充分的数据
	RT_TABLE_OK = 1,	///<通知当前表分析完成
	RT_SERVICE_OK = 2,	///<通知当前频点分析完成
	RT_UPDATE_FREQ = 3,	///<通知Nit表分析完成
	RT_UPDATE_FILTER = 4///<通知表更新
};

/// 全局参数定义
struct DataHandler; // 前向声明
struct EpgBuffer;	// 前向声明


typedef ::std::map<int,int>		PSIConstraintT;	
typedef ::std::map<int,bool>	mapEITFullT;	///{ServiceID--FullEIT}

struct TVEPGController{
	TVEPGController(){
		bIsTimeoutComplete_ = true;
		pEpgBuffer_ = 0 ;
		pEventHandler_[0] = pEventHandler_[1] = pEventHandler_[2] = 0;
		types_[0] = EIT_EVENT_PF;
		types_[1] = EIT_EVENT_AS;
		types_[2] = EIT_EVENT_OS;
		sSearchType_ = EIT_EVENT_PF;
		bFullPATTable_= false;
		cbNotify_ = NULL;
	}
	
	bool PATParser(const unsigned char* pData,const unsigned int iSize);
	bool IsFullEitData()
	{
		bool bRet = false;
		if(bFullPATTable_)
		{
			mapEITFullT::iterator it = programNumbers_.begin();
			for ( ;it != programNumbers_.end() ;it++)
			{
				if(!it->second)
				{
					return bRet;
				}
			}

			bRet = true;
		}
		
		return bRet;
	}

	MutexT		sDataMutex_;		///<
	EpgBuffer	*pEpgBuffer_;		///<
	EITEventType sSearchType_;		///<
	EITEventType types_[3];			///<
	DataHandler	*pEventHandler_[3];	///<
	
	bool bFullPATTable_;					///<PAT是否搜齐
	TVNOTIFY		cbNotify_;				///<
	PSIConstraintT patTableSectionNumber_;	///<PAT完整性校验
	mapEITFullT	   programNumbers_;			///<当前频道的EIT是否搜齐
	PSIConstraintT  eitConstraint_;			///<记录频道对应的第一个收到的EventID{ServiceID--EventID}
	PSIConstraintT  eitSectionNumber_;			///<记录频道对应的第一个收到的Section_number(ServiceID,Section_number << 16 + table_id)
	
	bool bIsTimeoutComplete_;		///<是否正常结束
	unsigned short iServiceId_;		///<当前播放节目ID
	FilterListT filters_;			///<默认的过滤器设置
	EITParseResultT sEITCompleted_;	///<EIT Table 分析完成标志
};

static TVEPGController	controller_;

void SetFilterBySID (unsigned short iServiceId);
void SetFilterByTSID(unsigned short iTSId);
bool IsCompleted(const EITEventType type,const EITParseResultT sParseState);

static void correct_text( std::string& text )
{
	if( !text.length() || text.empty()) 
		return;
	
	if( text[0] >= 0x0 && text[0]<= 0x13 )
	{

		if( text[0] != 0x11)
			text.erase( 0, 1 );
		else
		{
			text.erase( 0, 1 );
			//unsigned char *data = new unsigned char[text.size() + 2];
			unsigned char data[512];
			for( size_t i = 0; i < text.size(); i += 2 )
			{
				data[i] = text.at(i+1);
				data[i+1] = text.at(i);
			}
			data[text.size()] = 0;
			data[text.size()+1] = 0;

			char gbk[255];
			UnicodeToGBK( reinterpret_cast<unsigned short*>(data), gbk );
			text.assign( gbk );
			//delete[] data;
		}
	}

	size_t len=strlen(text.c_str());
	text.resize(len);
}
//=================================================================================================
struct EpgBuffer
{
	EpgBuffer(){}
	~EpgBuffer(){}

	void setEvent(unsigned short sid,EpgEventSet const *events,unsigned long iVersion)
	{
		ProgramEpg &pe = epg_[sid];
		pe.sid = sid ; 
		pe.events = *events;
		/*pe.EventsVer++;*/
		pe.EventsVer = iVersion ; 
	}
	
	bool setPresentEvent( unsigned short sid,EpgEvent const *Present,unsigned long iVersion)
	{
		bool bRet = false;
		ProgramEpg &pe = epg_[sid];

		if(pe.Present!=*Present)
		{
			pe.Present = *Present;
			/*pe.PresentVer++;*/
			pe.PresentVer = iVersion ; 
			bRet = true ;
		}
		return bRet  ; 
	}
	
	bool setFollowingEvent( unsigned short sid,EpgEvent const *Following,unsigned long iVersion)
	{
		bool bRet = false; 
		ProgramEpg &pe = epg_[sid];
		if(pe.Following!=*Following)
		{
			pe.Following = *Following;
			/*pe.FollowingVer++;*/
			pe.FollowingVer = iVersion;
			bRet = true;
		}
		return bRet ;
	}
	ProgramEpg const *getProgramEpg(unsigned short sid)
	{
		return find(sid);
	}
	void clear() 
	{
		AutoLockT lock(controller_.sDataMutex_);
		epg_.clear();
	}

	inline void getEPGData(EPGDataBaseT& epg)
	{
		AutoLockT lock(controller_.sDataMutex_);
		epg = epg_ ; 
	}
private:

	EPGDataBaseT epg_;

	ProgramEpg* find(unsigned short sid)
	{
		EPGDataBaseT::iterator it = epg_.find(sid);
		if(it!=epg_.end())
			return &it->second;
		return NULL;
	}
};


struct DataHandler
{
	DataHandler(IN EITEventType type,IN EITParseResultT* psState)
	{
		beginServiceID_ = INVALID_EVENT_SERVICEID;
		beginEventID_= INVALID_EVENT_ID;
		type_ = type;
		pParseResult_ = psState;
		events_.clear();
		versions_.clear();
	}

	~DataHandler(){}

	bool EventProcess(IN const unsigned char *pBuffer,IN unsigned int iSize);
	bool PFEventProcess(IN const unsigned char *pBuffer,IN unsigned int iSize);

	// 非正常退出时节目单获取
	void GetEPGData(EPGDataBaseT& epg);
private:

	EPGEVENTS events_;
	VersionTS versions_; // 记录节目的版本号
	unsigned short beginServiceID_ ;
	unsigned short beginEventID_ ; 
	EITParseResultT *pParseResult_;

	EITEventType type_;
};

bool DataHandler::PFEventProcess(IN const unsigned char *pBuffer,IN unsigned int iSize)
{
	bool bRet = false;

	LOGTRACE(LOGINFO,"Enter PFEventProcess buffer size=0x%x\n",iSize);

	if((NULL == controller_.pEpgBuffer_) && (EIT_EVENT_PF != type_))
		return bRet;

	if((0 == pBuffer) || (18 > iSize))
		return bRet;

	EitSection *eit = (EitSection *)pBuffer;
	if(eit->table_id() != 0x4e && eit->table_id() != 0x4f)
		return bRet;

	LOGTRACE(LOGINFO,"PFEventProcess sid=%d.\n",eit->service_id());

	try {
		EitSection::Loop_Iterator it = eit->loop_begin();
		if(it!= eit->loop_end())
		{
			ShortEvent_I seit = it->begin<DescShortEvent>();
			if(seit!=it->end<DescShortEvent>()) 
			{
				EpgEvent pf;
				pf.id = it->event_id();
				pf.name = seit->event_name_char();
				correct_text(pf.name);

				LOGTRACE(LOGINFO,"PFEventProcess sid=%d secnum=%d name=%s,version=%d\n",
					eit->service_id(),eit->section_number(),pf.name.c_str(),eit->version());

				StbTime st(it->start_time().utctime);
				pf.start_time = st.make_time();
				pf.end_time = pf.start_time+it->duration().totalseconds();
				pf.description = seit->text_char();
				correct_text(pf.description);

				{
					AutoLockT lock(controller_.sDataMutex_);
					if(0 == eit->section_number())
					{
						controller_.pEpgBuffer_->setPresentEvent(eit->service_id(),&pf,eit->version());
						pParseResult_->iPFCount++;
					}
					else
					{
						controller_.pEpgBuffer_->setFollowingEvent(eit->service_id(),&pf,eit->version());
						pParseResult_->iPFCount++;
					}
					if(2 <= pParseResult_->iPFCount)
					{
						LOGTRACE(LOGINFO,"Parse EIT P/F Completed\n");
						pParseResult_->bEITPF_A = true;
						bRet = true ;
					}
				}
			}
		}
	}
	catch(RuntimeError &e)
	{
		LOGTRACE(LOGINFO,"PFDataProcess exception:%s.\n",e.what());
	}
	
	LOGTRACE(LOGINFO,"Leave PFEventProcess.\n");
	return bRet;
}

bool DataHandler::EventProcess(IN const unsigned char *pBuffer,IN unsigned int iSize)
{
	bool bRet = false;

	if((NULL == controller_.pEpgBuffer_) && (EIT_EVENT_PF == type_))
		return bRet;

	if((0 == pBuffer) || (18 > iSize))
	{
		LOGTRACE(LOGINFO,"EventProcess pBuffer %p iSize %d error\n",pBuffer, iSize);
		return bRet;
	}

	EitSection *eit = (EitSection *)pBuffer;
	if(eit->table_id() < 0x50 || eit->table_id() > 0x6f )
	{
		LOGTRACE(LOGINFO,"EventProcess tableid %d error",eit->table_id());
		return bRet;
	}

	DYNAMICTRACE(LOGINFO,"EventProcess sid %d, table_id %d sectionid %d len %d\n",eit->service_id(),eit->table_id(),eit->section_number(), iSize);
	if(!(eit->CRC_OK()))
	{
		LOGTRACE(LOGINFO,"EventProcess CRC failed!\n");
		return bRet;
	}

	try{
		
		unsigned short uServiceID = eit->service_id();
		unsigned char  iVersion = eit->version();

		EpgEventSet EpgSet;
		versions_.insert(VersionTS::value_type(uServiceID,iVersion));
		
		for(EitSection::Loop_Iterator it= eit->loop_begin(); it!= eit->loop_end(); ++it)
		{
			ShortEvent_I seit = it->begin<DescShortEvent>();
			if(seit != it->end<DescShortEvent>()) 
			{
				EpgEvent e;
				e.id = it->event_id();
				e.name = seit->event_name_char();
				correct_text(e.name);

				StbTime st(it->start_time().utctime);
				e.start_time = st.make_time();
				e.end_time = e.start_time+it->duration().totalseconds();
				e.description = seit->text_char();
				correct_text(e.description);

				char * p = (char*)it->start_time().utctime;
				
				DYNAMICTRACE(LOGINFO,"eventid %d start %d end %d %x:%x:%x, %s %s\n",it->event_id(),e.start_time,e.end_time,p[2],p[3],p[4], e.name.c_str(),ctime((time_t*)&e.start_time));

				EpgSet.insert(e);
/*				
				// 检查每一个频道的事件ID是否重复作为该频道数据是否齐全
				PSIConstraintT::iterator itEitConstraint = controller_.eitConstraint_.find(uServiceID);
				if(itEitConstraint == controller_.eitConstraint_.end())
				{
					controller_.eitConstraint_.insert(PSIConstraintT::value_type(uServiceID,e.id));
				}
				else if(itEitConstraint->second == e.id)
				{

					mapEITFullT::iterator itFull = controller_.programNumbers_.find(uServiceID);
					if(itFull != controller_.programNumbers_.end()){
						itFull->second = true;
					}

					// 该频道数据已搜全
					it = events_.find(uServiceID);
					if(it != events_.end())
					{
						LOGTRACE(LOGINFO,"ServiceID=%u(%u),EIT is fulled.\n",uServiceID,it->second.size());
#if 1 
						// 通知当前节目电子节目指南 
						if(uServiceID == controller_.iServiceId_ && controller_.cbNotify_)
						{
							ProgramEpg prog_;
							prog_.sid	= it->first;
							prog_.events= it->second;
							VersionTS::iterator it1 = versions_.find(uServiceID);
							if(it1 != versions_.end()) 
							{
								prog_.EventsVer = it1->second;
							}

							EPGDataBaseT epg;
							epg.insert(EPGDataBaseT::value_type(uServiceID,prog_));
							LOGTRACE(LOGINFO,"Notify ServiceId=%u epg update.\n",uServiceID);
							controller_.cbNotify_(TVNOTIFY_EPGCOMPLETE,uServiceID,static_cast<void*>(&epg));
						}
#endif
					}
				}
				*/
			}
		}
		if(!EpgSet.empty())
		{
			EPGEVENTS::iterator it = events_.find(uServiceID);
			if(it != events_.end())
			{
				it->second.insert(EpgSet.begin(),EpgSet.end());
			}
			else
			{
				 events_.insert(EPGEVENTS::value_type(uServiceID,EpgSet));
			}
			if(controller_.cbNotify_ != NULL)
				controller_.cbNotify_(0x1000,uServiceID,static_cast<void*>(&EpgSet));
		}
		// 检查每一个频道的Section_number是否重复作为该频道数据是否齐全
		PSIConstraintT::iterator itEitSectionNumber = controller_.eitSectionNumber_.find(uServiceID);
		int tempMark = (eit->section_number() << 16) | eit->table_id();
		if(itEitSectionNumber == controller_.eitSectionNumber_.end())
		{
			controller_.eitSectionNumber_.insert( PSIConstraintT::value_type(uServiceID, tempMark));
		}
		else if(itEitSectionNumber->second == tempMark)
		{

			controller_.programNumbers_[uServiceID] = true;
			/*
			mapEITFullT::iterator itFull = controller_.programNumbers_.find(uServiceID);
			if(itFull != controller_.programNumbers_.end()){
				itFull->second = true;
			}
			*/
			// 该频道数据已搜全
			EPGEVENTS::iterator it = events_.find(uServiceID);
			if(it != events_.end())
			{
//				LOGTRACE(LOGINFO,"ServiceID=%u, event number %u ,EIT is fulled.\n",uServiceID,it->second.size());
#if 1 
				// 通知保存节目指南,当前频道会通知上层 
				if(/*uServiceID == controller_.iServiceId_ && */controller_.cbNotify_)
				{
					ProgramEpg prog_;
					prog_.sid	= it->first;
					prog_.events= it->second;
					VersionTS::iterator it1 = versions_.find(uServiceID);
					if(it1 != versions_.end()) 
					{
						prog_.EventsVer = it1->second;
					}

					EPGDataBaseT epg;
					epg.insert(EPGDataBaseT::value_type(uServiceID,prog_));
					controller_.cbNotify_(TVNOTIFY_EPGCOMPLETE,uServiceID,static_cast<void*>(&epg));
				}
#endif
			}
		}

		//if(EIT_EVENT_AS == type_)
		//{
		//	// 一次EIT-S数据循环完成
		//	if(controller_.IsFullEitData())
		//	{
		//		pParseResult_->bEITSH_A = true ;
		//		bRet = true ;
		//	}
		//}
	}
	catch(RuntimeError& e)
	{
		LOGTRACE(LOGINFO,"EventProcess exception:%s.\n",e.what());
	}

#if 0  // 
	try {

		bool CheckFinish=(beginServiceID_!=INVALID_EVENT_SERVICEID);
		unsigned short EventServiceID = eit->service_id();
		unsigned char  iVersion = eit->version();

		EpgEventSet EpgSet;
		versions_.insert(VersionTS::value_type(EventServiceID,iVersion));

		for(EitSection::Loop_Iterator it= eit->loop_begin(); it!= eit->loop_end(); ++it)
		{
			ShortEvent_I seit = it->begin<DescShortEvent>();
			if(seit != it->end<DescShortEvent>()) 
			{
				EpgEvent e;
				e.id = it->event_id();
				e.name = seit->event_name_char();
				correct_text(e.name);

				StbTime st(it->start_time().utctime);
				e.start_time = st.make_time();
				e.end_time = e.start_time+it->duration().totalseconds();
				e.description = seit->text_char();
				correct_text(e.description);
				
				EPGEVENTS::iterator it = events_.find(EventServiceID);
				if(it != events_.end())
				{
					it->second.insert(e);
				}
				else
				{
					EpgSet.insert(e);
					events_.insert(EPGEVENTS::value_type(EventServiceID,EpgSet));
				}

#if 0			// 使用超时的方式来处理数据完整性
				if(INVALID_EVENT_SERVICEID == beginServiceID_)
				{
					beginServiceID_ = EventServiceID;
					beginEventID_	= e.id;
					CheckFinish		= false;
					LOGTRACE(LOGINFO,"EventProcess begin_serviceid=%d begin_eventid=%d.\n",beginServiceID_,beginEventID_);
				}

				LOGTRACE(LOGINFO,"EventProcess sid=%d version=%d eid=%d ename=%s.\n",
					EventServiceID,eit->version(),e.id,e.name.c_str());

				if(CheckFinish && (EventServiceID==beginServiceID_) && (e.id==beginEventID_))
				{
					{
						AutoLockT lock(controller_.sDataMutex_);
						for( EPGEVENTS::iterator it=events_.begin(); it!=events_.end(); it++)
						{
							LOGTRACE(LOGINFO,"EventProcess setEvent serviceid=%d, event count=%d\n",
								it->first,it->second.size());

							controller_.pEpgBuffer_->setEvent(it->first,&it->second,versions_[it->first]);	
						}
					}

					// 一次EIT-S数据循环完成
					if(EIT_EVENT_AS == type_)
						pParseResult_->bEITSH_A = true ;
					if(EIT_EVENT_OS == type_)
						pParseResult_->bEITSH_O = true ;
					bRet = true ;
				}
#endif 
			}
		}	
	}
	catch(RuntimeError &e)
	{
		LOGTRACE(LOGINFO,"EventProcess exception:%s.\n",e.what());
	}
#endif 

	// LOGTRACE(LOGINFO,"Leave EventProcess.\n");
	return bRet;
}

void DataHandler::GetEPGData(EPGDataBaseT& epg)
{
	AutoLockT lock(controller_.sDataMutex_);
	for( EPGEVENTS::iterator it=events_.begin(); it!=events_.end(); it++)
	{
		ProgramEpg prog_;
		prog_.sid	= it->first;
		prog_.events= it->second;
		VersionTS::iterator it1 = versions_.find(prog_.sid);
		if(it1 != versions_.end()) {
			prog_.EventsVer = it1->second;
		}
		
		epg.insert(EPGDataBaseT::value_type(prog_.sid,prog_));
	}

	events_.clear();
	versions_.clear();
	beginServiceID_=INVALID_EVENT_SERVICEID;
	beginEventID_=INVALID_EVENT_ID; 
}

//========================================================================================
bool tvepg_init(IN const unsigned short iServiceId,IN TVNOTIFY cbNotify)
{	
	LOGTRACE(LOGINFO,"Enter tvepg_init\n");
	bool bRet = false;
	controller_.pEpgBuffer_ = new EpgBuffer();

	for (int i = 0 ; i < 3 ;i++)
	{
		controller_.pEventHandler_[i] = new DataHandler(controller_.types_[i],&controller_.sEITCompleted_);
	}

	memset(&controller_.sEITCompleted_,0,sizeof(EITParseResultT));
	
	// 按节目输出P/F事件
	controller_.iServiceId_ = iServiceId ; 
	SetFilterBySID(iServiceId);
	
	controller_.bIsTimeoutComplete_ = true;
	
	controller_.bFullPATTable_ = false;
	controller_.cbNotify_ = cbNotify;
	controller_.programNumbers_.clear();
	controller_.patTableSectionNumber_.clear();
	controller_.eitConstraint_.clear();
	controller_.eitSectionNumber_.clear();
	//for test
	DYNAMICTRACEINIT;
	bRet = true ;
	LOGTRACE(LOGINFO,"Leave tvepg_init\n");
	return bRet;
}

void tvepg_uninit()
{
	if(NULL != controller_.pEpgBuffer_)
	{
		delete controller_.pEpgBuffer_;
		controller_.pEpgBuffer_ = 0;
	}

	for ( int i = 0 ; i < 3 ; i++)
	{
		delete controller_.pEventHandler_[i];
		controller_.pEventHandler_[i] = 0 ; 
	}

	controller_.filters_.clear();
	controller_.iServiceId_ = INVALID_EVENT_SERVICEID ; 
	//for test
	DYNAMICTRACEUNINIT;
}

bool IsCompleted(const EITEventType type,const EITParseResultT sParseState)
{
	bool bRet = false;
	
	//LOGTRACE(LOGINFO,"TVEPG::IsCompleted{pf_a=%d,pf_o=%d,sh_a=%d,sh_o=%d}\n",
	//	sParseState.bEITPF_A,sParseState.bEITPF_O,sParseState.bEITSH_A,sParseState.bEITSH_O);
	if(EIT_EVENT_PF == type)
	{
		bRet = (sParseState.bEITPF_A || sParseState.bEITPF_O) ? true : false;
	}
	else if(EIT_EVENT_AS == type)
	{
		bRet = sParseState.bEITSH_A ? true : false;
	}
	else if(EIT_EVENT_OS == type)
	{
		bRet = sParseState.bEITSH_O ? true : false;
	}
	else if(EIT_EVENT_ALL== type)
	{
		bRet = ((sParseState.bEITPF_A || sParseState.bEITPF_O) && 
			    sParseState.bEITSH_A && sParseState.bEITSH_O) ? true : false;
	}
	else if(EIT_EVENT_SH == type)
	{
		bRet = (sParseState.bEITSH_A && sParseState.bEITSH_O) ? true : false;
	}

	return bRet ; 
}

int tvepg_putSectionData(IN const unsigned short iPID ,IN const unsigned char *pBuffer,IN const unsigned int iSize)
{
	int iRet = 0;
	bool bRet = false ; 

	EITEventType type ; 
	if(NULL == pBuffer)
		return 0 ;

#if 1 	
	//
	// 处理PAT表
	if(iPID == PAT_PID)
	{
		iRet = controller_.PATParser(pBuffer,iSize) ? RT_TABLE_OK : RT_CONTINUE;
		return iRet;
	}
#endif 

	if(pBuffer[0] == 0x4E || pBuffer[0] == 0x4F)
	{
		type = EIT_EVENT_PF ; 
	}
	else if(0x50 <= pBuffer[0] && 0x5F >= pBuffer[0])
	{
		type = EIT_EVENT_AS ;
	}
	else if(0x60 <= pBuffer[0] && 0x6F >= pBuffer[0])
	{
		type = EIT_EVENT_OS;
	}
	else 
	{
		type = EIT_EVENT_UN;
		// other table.
	}
	
	if(EIT_EVENT_PF == type && (NULL != controller_.pEventHandler_[0]))
	{
		bRet = controller_.pEventHandler_[0]->PFEventProcess(pBuffer,iSize);
		iRet = (true == bRet) ? RT_TABLE_OK :RT_CONTINUE;
	}
	else if(EIT_EVENT_AS == type || EIT_EVENT_OS == type)
	{
		if(NULL != controller_.pEventHandler_[type])
		{
			if(controller_.pEventHandler_[(int)type]->EventProcess(pBuffer,iSize))
				iRet = RT_TABLE_OK;
			else
				iRet = RT_CONTINUE;
		}
	}

	if(IsCompleted(controller_.sSearchType_,controller_.sEITCompleted_))
	{
		iRet = RT_SERVICE_OK ;
		controller_.bIsTimeoutComplete_ = false;
	}
	
	// LOGTRACE(LOGINFO,"TVEPG::putSectionData return=%d\n",iRet);
	return iRet;
}

bool tvepg_getEpgData(OUT EPGDataBaseT& epgs)
{
	bool bRet = false;
	if( (NULL == controller_.pEventHandler_[1]) || 
		(NULL == controller_.pEventHandler_[2]) ||
		(NULL == controller_.pEpgBuffer_))
	{
		return bRet;
	}
  
	if(controller_.bIsTimeoutComplete_)
	{
		controller_.pEventHandler_[1]->GetEPGData(epgs);
		controller_.pEventHandler_[2]->GetEPGData(epgs);
		//超时完成,有些频道可能会没有egp信息
		for(mapEITFullT::iterator it = controller_.programNumbers_.begin();
				it != controller_.programNumbers_.end(); it++)
		{
			epgs[it->first].sid = it->first;	//如果没有对应的sid就插入
		}
	}
	else
	{
		controller_.pEpgBuffer_->getEPGData(epgs);
	}

	bRet = epgs.size() ? true : false;
	return bRet ;
}

/// ServiceID作为过滤字段接收数据
void SetFilterBySID(unsigned short iServiceId)
{
	EITFILTERS[0].data[1] = (iServiceId >> 8) & 0xff;
	EITFILTERS[0].data[2] =  iServiceId & 0xff;

	EITFILTERS[0].mask[1] = 0xff;
	EITFILTERS[0].mask[2] = 0xff;
}

/// TSID作为过滤字段接受数据
void SetFilterByTSID(unsigned short iTSId)
{
	int  iFilterCount = sizeof(EITFILTERS)/sizeof(EITFILTERS[0]);
	for (int i = 0 ; i < iFilterCount ; i++)
	{
		EITFILTERS[i].data[6] = (iTSId >> 8) & 0xff;
		EITFILTERS[i].data[7] =  iTSId & 0xff;

		EITFILTERS[i].mask[6] = 0xff;
		EITFILTERS[i].mask[7] = 0xff;
	}
}

/// P/F接收完成后设置成有新版本时发送数据
void SetFilterByVer(unsigned char iNewVersion)
{
	int  iFilterCount = sizeof(EITFILTERS)/sizeof(EITFILTERS[0]);
	for (int i = 0 ; i < iFilterCount ; i++)
	{
		EITFILTERS[i].data[3] = iNewVersion; 
		EITFILTERS[i].mask[3] = 0xff;
	}
}

bool tvepg_getEpgFilters(IN EITEventType evtType,OUT FilterListT& filters)
{
	controller_.sSearchType_ = evtType ; 
	switch (evtType)
	{
	case EIT_EVENT_PF:
		controller_.filters_.push_back(EITFILTERS[0]);
		break;
	case EIT_EVENT_AS:
		controller_.filters_.push_back(PATFILTER);
		controller_.filters_.push_back(EITFILTERS[1]);
		break;
	case EIT_EVENT_OS:
		//controller_.filters_.push_back(PATFILTER);
		controller_.filters_.push_back(EITFILTERS[2]);
		break;
	case EIT_EVENT_SH:
		//controller_.filters_.push_back(PATFILTER);
		controller_.filters_.push_back(EITFILTERS[1]);
		controller_.filters_.push_back(EITFILTERS[2]);
		break;
	case EIT_EVENT_ALL:
	default:
		//controller_.filters_.push_back(PATFILTER);
		controller_.filters_.push_back(EITFILTERS[0]);
		controller_.filters_.push_back(EITFILTERS[1]);
		controller_.filters_.push_back(EITFILTERS[2]);
		break;
	}

	filters = controller_.filters_;
	return true;
}

bool TVEPGController::PATParser(const unsigned char* pData,const unsigned int iSize)
{
	bool bRet = false;
	if((pData == NULL )  || (iSize < 3))
		return bRet;
	
	try
	{
		novelsuper::psisi_parse::PatSection *ppat = (novelsuper::psisi_parse::PatSection *)pData;
		
		unsigned char iSectionNumber = ppat->section_number();
		unsigned char iLastSectionNumber = ppat->last_section_number();
		if((iLastSectionNumber+1) == patTableSectionNumber_.size())
		{
			bFullPATTable_ = true;
			return true;
		}

		PSIConstraintT::iterator itConstraint = patTableSectionNumber_.find(iSectionNumber);
		if(patTableSectionNumber_.end() != itConstraint)
			return false;

		int iServiceID = 0,iPmtID=0;
		for( novelsuper::psisi_parse::PatSection::Loop_Iterator it = ppat->loop_begin(); !it.empty(); ++it)
		{
			iServiceID = it->program_number();
			if(0 != iServiceID)
			{
				programNumbers_.insert(mapEITFullT::value_type(iServiceID,false));
				LOGTRACE(LOGINFO,"PATParser,service_id=%d\n",iServiceID);
			}
		}

		patTableSectionNumber_.insert(PSIConstraintT::value_type(iSectionNumber,iSectionNumber));
		if((iLastSectionNumber+1) == patTableSectionNumber_.size())
		{
			bFullPATTable_ = true;
			bRet = true;
		}
	}
	catch(novelsuper::psisi_parse::RuntimeError  &e)
	{
		LOGTRACE(LOGINFO,"IsGoodPat: RuntimeError !!!, %s",e.what());
		return false;
	}
	return bRet;
}
