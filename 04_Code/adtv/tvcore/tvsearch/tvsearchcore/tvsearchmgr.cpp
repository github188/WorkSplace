#include "tvsearchmgr.h"
#include "tvsearchlocal.h"
#include "tsdemux.h"
#include "tvdevice.h"
#include "tvconfig.h"
#define LOG_TAG "tvsearch"
#define LOG_LEVEL LOG_TRACE
#include "dxreport.h"

#define SINGLE_FREQ_TIMEOUT_MAX (15000)
#ifndef WAIT_OBJECT_0
#define WAIT_OBJECT_0       ( 0 )
#endif

extern GlobalConfigParamT gConfig;
CONST U16 defaultFreqs[] =
{
	53,  61,  69,  80,  88, 115, 123, 131, 139, 147,
	155, 163, 171, 179, 187, 195, 203, 211, 219, 227,
	235, 243, 251, 259, 267, 275, 283, 291, 299, 307,
	315, 323, 331, 339, 347, 355, 363, 371, 379, 387,
	395, 403, 411, 419, 427, 435, 443, 451, 459, 474,
	482, 490, 498, 506, 514, 522, 530, 538, 546, 554,
	562, 570, 578, 586, 594, 602, 610, 618, 626, 634,
	642, 650, 658, 666, 674, 682, 690, 698, 706, 714,
	722, 730, 738, 746, 754, 762, 770, 778, 786, 794,
	802, 810, 818, 826, 834, 842, 850
};

//==========================================================================
HANDLE TVSearchMgr::m_hTvDevice=0;
void TVSearchMgr::Init(STVMode eMode,TuningParam& stTuningParam,TVNotifyProxy *pNotifyProxy,CompleteCallBackProc pCallBack)
{
	m_pNotifyProxy = pNotifyProxy;
	m_eSearchMode = eMode;
	m_pTVSearchMgrThread = NULL;
	m_pCompleteCallBack = pCallBack;
	m_iMaxTimeout = 0;
	m_iPatTime = 0;
	//	m_sServiceId = 0;

	m_inputTuningParam.freq = stTuningParam.freq;
	m_inputTuningParam.qam = stTuningParam.qam;
	m_inputTuningParam.symb = stTuningParam.symb;

	m_hTvDevice=tvdevice_open();
	if(m_hTvDevice)
	{
		tsdemux_enableDemux(true);
		tvdevice_setPidFilter(m_hTvDevice,NULL,0);
	}
	else
	{
		dxreport("---TVSearchMgr::TVSearchMgr,can't open usb tvdevice!!!----\n");
	}
}

void TVSearchMgr::UnInit()
{
	dxreport("TVSearchMgr::~UnInit Begin >>>\n");

	m_pNotifyProxy = NULL;

	if(m_pTVSearchMgrThread != NULL )
	{
		m_pTVSearchMgrThread->stop();

		delete m_pTVSearchMgrThread;
		m_pTVSearchMgrThread = NULL;
	}
	m_listDVBService.clear();

	ClearSearchers();
	destroy_search();
	tvdevice_close(m_hTvDevice);
	dxreport("TVSearchMgr::~UnInit End <<<\n");
}

// 参数: bldStep
// 1. 搜索pat, cat,sdt
// 2. 搜索pmt.
// 3. 搜索nit bat,
ULONG TVSearchMgr::BuildSearchers(IN BuildStep bldStep)
{	
	dxreport("TVSearchMgr::BuildSearchers begin <<<  bldStep:%d\n",bldStep);
	bldStep_=bldStep;
	std::vector<SECFilter> listFilter_;
	ULONG hr = get_table_filters(listFilter_,bldStep);	
	if(hr != 0 || listFilter_.empty())
	{
		return -1;
	}

	U32 iMaxTimeout = 0;
	BaseSearcher *pBaseSearcher = NULL ;
	utSectionFilter secfilter;
	memset(&secfilter, 0, sizeof(secfilter));
	for (U32 i = 0 ; i < listFilter_.size() ; i++)
	{
		secfilter.iTuner = 0;
		secfilter.pid = listFilter_[i].pid;
		secfilter.tid = listFilter_[i].data[0];
		secfilter.timeout = listFilter_[i].timeout;
		pBaseSearcher = new BaseSearcher(secfilter,this);
		NS_sleep(10);	// 不延时有可能搜台不全
		m_listBaseSearcher.push_back(pBaseSearcher);
		// 本批次过滤器最大超时值
		if(listFilter_[i].timeout > iMaxTimeout)
			iMaxTimeout = listFilter_[i].timeout;
	}

	if(bldStep == BS_PATCATSDT || bldStep == BS_NITBAT)			// 建立PAT 表过滤, nit 主频点搜索也走该处
	{
		m_iPatTime = NS_GetTickCount();
		m_iMaxTimeout = iMaxTimeout;		// 最大超期时间
	}
	else if(bldStep == BS_PMT)
	{
		// 第二次调用buildSearcher 的时间, 
		U32 iInterval =  NS_GetTickCount()-m_iPatTime ;	
		S32 iAvailableTime = m_iMaxTimeout-iInterval; // 剩余时间
		if(iAvailableTime >= 0)
		{
			S32  iCompensateTime = iMaxTimeout - iAvailableTime;
			if(iCompensateTime>=0)
			{
				m_iMaxTimeout += iCompensateTime;
			}
			else
			{
				// 无需修正时间
			}
		}
		else  // 如果是正常逻辑, 不会到这，因为第一次build 已超时.
		{
			m_iMaxTimeout+=iMaxTimeout;
		}
		dxreport("TVSearchMgr::buildSearcher call buildSearch delta time: %d\n",iInterval);
	}
	dxreport("TVSearchMgr::buildSearcher m_iMaxTimeout=%d, m_iPatTime:%d.\n", m_iMaxTimeout,m_iPatTime);
	dxreport("TVSearchMgr::BuildSearchers end >>>\n");
	return hr; 
}

void TVSearchMgr::ClearSearchers()
{
	dxreport("TVSearchMgr::ClearSearchers Begin >>>\n");
	for(int i = 0 ;i < (int)m_listBaseSearcher.size() ;i++)
	{
		BaseSearcher *pSearcher = m_listBaseSearcher[i];
		delete pSearcher ;
	}
	m_listBaseSearcher.clear();
	ClearSectionDeque();
	m_iPatTime = 0 ;  
	dxreport("TVSearchMgr::ClearSearchers End <<<\n");
}

void TVSearchMgr::RemoveOneSearcher(U16 pid,U8 table_id)
{
	dxreport("TVSearchMgr::RemoveOneSearcher Begin >>>. pid:%d, table_id:%d.\n",pid,table_id);
	for(int i = 0 ;i < (int)m_listBaseSearcher.size() ;i++)
	{
		if((pid == m_listBaseSearcher[i]->GetPID()) && 	(table_id == m_listBaseSearcher[i]->GetTableId()))
		{
			BaseSearcher *pSearcher = m_listBaseSearcher[i];
			//			pSearcher->DelSectionFilter();		
			delete pSearcher ;
			m_listBaseSearcher.erase(m_listBaseSearcher.begin()+i);
		}
	}

	dxreport("TVSearchMgr::RemoveOneSearcher End <<<\n");
}

void TVSearchMgr::GetListTunnerParams(STVMode searchMode,std::vector<TuningParam> &freqList)
{
	if(searchMode != STVMODE_FULL )
	{
		/// 单频点或nit 搜索(主频点)
		freqList.push_back(m_inputTuningParam);
		return;
	}
	// 全频模式
	freqList.clear();
	TuningParam tParams;
	tParams.symb = m_inputTuningParam.symb;
	tParams.qam  = m_inputTuningParam.qam;

	std::vector<U16> freqs;
	get_full_search_tables(freqs);
	if(freqs.size()) 
	{
		/// 使用动态的频点列表, 从tvsearchLocal 中获取的
		for( U32 i = 0; i < (U32)freqs.size(); i++ )
		{
			tParams.freq = freqs[i]*1000 ;
			freqList.push_back(tParams);
		}
	}
	else
	{
		/// 使用默认的频点列表
		for( U32 i = 0; i < sizeof(defaultFreqs)/sizeof(U16); i++ )
		{
			tParams.freq = defaultFreqs[i]*1000 ;
			freqList.push_back(tParams);
		}
	}

}

void TVSearchMgr::Start(DVBServiceListT& services)
{
	if( m_eSearchMode == STVMODE_MANUAL )
	{
		m_listDVBService=services;
	}
	init_tv_search(m_eSearchMode);
	
	m_pTVSearchMgrThread = new simplethread();
	if(m_pTVSearchMgrThread != NULL )
	{
		bool bFlag = m_pTVSearchMgrThread->start(TVSearchMgr::TVSearchMgrPollProc,this); 
	}
}

UINT __stdcall TVSearchMgr::TVSearchMgrPollProc(LPVOID lpParam)
{
	dxreport("TVSearchMgr::TVSearchMgrPollProc Begin >>> \n");
	simplethread *pThread	= reinterpret_cast<simplethread*>(lpParam);
	TVSearchMgr *pTVSearchMgr = reinterpret_cast<TVSearchMgr*>(pThread->get_arglist());
	
	pTVSearchMgr->OnBeforeSearch(pThread);
	std::vector < TuningParam >  freqList;
	
	pTVSearchMgr->GetListTunnerParams(pTVSearchMgr->m_eSearchMode,freqList);
	if(pTVSearchMgr->m_eSearchMode == STVMODE_NIT)
	{
		// nit 搜索时，会进行2遍搜索
		// 第一遍搜索主频点, 获取所有频点列表,
		bool res=pTVSearchMgr->SearchOneFreq(freqList[0],BS_NITBAT);
		if(res)
		{
			// NIT模式获取找到的频点列表
			freqList.clear();
			ULONG hr = get_tuning_paramfrom_nit(freqList);
			dxreport("get_tuning_paramfrom_nit: freq number:%d\n",freqList.size());
			// 第二遍，搜索找到的频率列表
			pTVSearchMgr->SearchFreqs(freqList);		}
		else
		{
			dxreport("main freq search failed!!!\n");
		}
	}
	else
	{
		// 单频点或全频点，搜索频率即可
		pTVSearchMgr->SearchFreqs(freqList);
	}
	if(!pThread->check_stop())
	{
		pTVSearchMgr->OnProgress(100);
	}
	pTVSearchMgr->OnCompleteCallBack();
	dxreport("TVSearchMgr::TVSearchMgrPollProc End <<<\n");
	return 0;
}

void TVSearchMgr::SearchFreqs(std::vector<TuningParam> &listFreq)
{
	static int errs;
	errs=0;
	dxreport("[TVSearchMgr::SearchFreqs] freqs: %d\n",listFreq.size());
	for(U32 iLoop = 0 ; iLoop< listFreq.size();iLoop++)
	{
		// 主动结束
		if(m_pThread->check_stop())
		{
			dxreport("force stop called, exit TVSearchMgrPollProc...\n");
			break;
		}
		dxreport("TVSearchMgrPollProc: Searching Freq:%d...\n",listFreq[iLoop].freq);
		OnProgress(CalculatePercent(iLoop,listFreq.size()));
		SearchOneFreq(listFreq[iLoop],BS_PATCATSDT);
/*
		// for test quickly
		dxreport("TVSearchMgrPollProc: Searching qam:%d, symb:%d...\n",listFreq[iLoop].qam, listFreq[iLoop].symb);
		{
			TuningParam tuner;
			tuner.freq = 474000;
			tuner.qam = 2;
			tuner.symb = 6875;
			SearchOneFreq(tuner,BS_PATCATSDT); 
		}
		break;
*/		
	}
}
bool TVSearchMgr::SearchOneFreq (TuningParam tuner, BuildStep buildStep)
{

	// usbtv 锁频点
	TunerSignal signal;
	int lock_res=Lock_Tuner_Freq(tuner.freq,tuner.qam,tuner.symb,signal);
	dxreport("Lock_Tuner_Freq res is %d\n",lock_res);
	if(lock_res==-2||lock_res==-1) // -1, -2 均为锁频失败
	{
		OnTunerInfo(tuner,signal); //when not locked, still notify frequency
		dxreport("Lock_Tuner_Freq failed. freq:%d, qam:%d, symb:%d\n",tuner.freq,tuner.qam,tuner.symb);
		return false;		// 当不能锁频时，不必搜索
	}
	else
	{
		dxreport("Lock_Tuner_Freq succeed. freq:%d, qam:%d, symb:%d,signal. strength:%d,quality:%d,locked:%d\n",\
				tuner.freq,tuner.qam,tuner.symb,signal.strength,signal.quality,signal.locked);
	}
	ULONG hr = SetTuningParam(tuner);
	OnTunerInfo(tuner,signal);
	// 开始单频点搜索
	DWORD dwStart = NS_GetTickCount();
	BuildSearchers(buildStep);
	//	waitdata arrive			
	bool bWaitingData = true;
	bool res=false;
	while(bWaitingData)
	{
		// 主动结束
		if(m_pThread->check_stop())
		{
			dxreport("force stop called, exit TVSearchMgrPollProc...\n");
			return false;
		}
		WaitDataStatus wds=WaitDataArrive(bldStep_);	// 该成员变量bldStep_由BuildSearchers 创建
		switch(wds)
		{
			case WDS_DATA_NOT_READY:
				dxreport("SearchOneFreq: freq:%d no data.\n",tuner);
				break;
			case WDS_DATA_TIMEOUT:
				dxreport("++++----timeout Ocurred: %d.\n",m_iMaxTimeout);
				bWaitingData=false;  // search to next frequency
				res=false;		// failed
				break;
			case WDS_DATA_READY:
				{
					ProcessDataStatus pds=ProcessData();
					switch(pds)
					{
						case PDS_DATA_READY:
							dxreport("SearchOneFreq: this freq complete:%d\n",tuner.freq);
							bWaitingData=false;
							res=true;			// succeed
							break;
						case PDS_PATCATSDT_READY:	
							BuildSearchers(BS_PMT);
							break;
						case PDS_WAIT_DATA:
							break;
						default:
							break;
					}
				}
				break;
			default:
				break;
		}

	}
	return res;
}
WaitDataStatus TVSearchMgr::WaitDataArrive(BuildStep buildStep)
{
	if(WaitForDataReady() == WAIT_OBJECT_0 ) return WDS_DATA_READY;
	if(NS_GetTickCount()-m_iPatTime < m_iMaxTimeout) return WDS_DATA_NOT_READY;
	// timeout process code
	// 央视内网无CAT 表，SDT 也有问题，例如无名称，所以加此段代码
	if(buildStep==BS_PATCATSDT && CatSdtMissing()) // 正常不走该代码
	{
		Table_Status status=GetCatSdtTableState();
		switch(status)
		{
			case Cat_Sdt_Table_ok:
			case Sdt_Table_ok:   //cat table not ok., set it to ok
				RemoveOneSearcher(CAT_PID,TableId_Cat);
				RemoveOneSearcher(SDT_PID,TableId_SdtA);
				SetCatSdtTableState(1);		// 虚假设置Cat table OK, 方便后面判断
				dxreport("cat or sdt timeout Ocurred: %d. continue pmt search...\n",m_iMaxTimeout);
				BuildSearchers(BS_PMT);	// 当没有Cat 表时，可以忽略, 继续搜PMT
				return WDS_DATA_NOT_READY;
			default:	//Cat_Sdt_Table_not_ok, cat_table_ok, timeout
				;
		}
	}

	OnDVBService() ; 
	ClearSearchers();
	return WDS_DATA_TIMEOUT;
}
ProcessDataStatus TVSearchMgr::ProcessData()
{
	// 从栈中取数，处理，删除的结构用指针操作，会快一些
	DVBSection *pSection = GetSection();
	if(pSection == NULL )
	{
		dxreport("TVSearchMgrPollProc: pSection is NULL.\n");
		return PDS_WAIT_DATA;
	}

	//处理数据一定要快，否则可能阻塞或增加耗时
	// 本来需要处理deque中的所有数据, 但考虑数据的同步性，处理
	// 一个section 数据后程序会改变执行流程，所以只处理了一个数据。
	// 但考虑至少99%以上deque 只会存一个数据。多余浪费的数据只会增加一点时间
	// 所以该程序结构不会有问题
	int iFlag = analyse_section_data(pSection->pid_,pSection->data_,pSection->dataLen_); 

	U16 pid = pSection->pid_ ; 
	U8  talbe_id = pSection->table_id_;
	FreeSection(pSection);

	if(iFlag == RT_CONTINUE) 
		return PDS_WAIT_DATA;


	// 分析数据后，如果table_id 不是PMT table, 就删除该searcher
	if(talbe_id != 0x02) 
	{
		RemoveOneSearcher(pid,talbe_id);
		RemoveSection(pid, talbe_id); // 增加删除section 中多余数据
	}
	switch(iFlag)
	{
		case  RT_UPDATE_FILTER:  /// 手工或全频搜索pat分析完成,需要搜索新的过滤表(PMT filters)
//			BuildSearchers(BS_PMT);
			return PDS_PATCATSDT_READY;
		case RT_TABLE_OK:			// 表分析完成，等待服务分析完成
			return PDS_WAIT_DATA;
		case RT_SERVICE_OK :		/// 某频点分析完成，退出switch,清理searcher, 进行下一频点搜索
			OnDVBService();
			break;
		case RT_UPDATE_FREQ:	    

			break;
		default:		// 不会有
			;
	}
	ClearSearchers();	
	return PDS_DATA_READY;
	
}
BOOL TVSearchMgr::Lock_Tuner_Freq(U32 freq, U32 qam, U32 symb, TunerSignal& aSignal)
{
	BOOL bRet1=false, bRet2=false;
	bRet1 = tvdevice_tune(m_hTvDevice,freq,symb,qam);
	if(!bRet1) return -2;
	NS_sleep(10);
	bRet2=tvdevice_getTunerSignalStatus(m_hTvDevice, &aSignal);
	if(!bRet2) return -1;
	return 0;
}
int TVSearchMgr::CalculatePercent(int num, size_t total)
{
	return 100*num/(int)total;
}

// 获取当前搜索到的DVBService
ULONG TVSearchMgr::OnDVBService()
{
	dxreport("TVSearchMgr::OnDVBService Begin >>>\n");
	if(m_eSearchMode != STVMODE_MONITOR_PMT )
	{
		std::vector<DVBService> one_freq_service;
		get_dvb_services(one_freq_service);
		if(m_pNotifyProxy != NULL )
		{
			// 推送到界面用于显示
			m_pNotifyProxy->OnDVBService(one_freq_service);
		}
	}
	dxreport("TVSearchMgr::OnDVBService End <<<\n");
	return 0;
}

void   TVSearchMgr::OnProgress(U8 iPercent)
{
	if(m_pNotifyProxy != NULL )
	{
		dxreport("TVSearchMgr::OnProgress: %d\n",iPercent);
		m_pNotifyProxy->OnProgress(iPercent);
	}
}

void   TVSearchMgr::OnTunerInfo(TuningParam& tuning,TunerSignal& signal)
{
	if(m_pNotifyProxy != NULL )
	{
		dxreport("TVSearchMgr::OnTunerInfo tuning={%d,%d,%d},signal={%d,%d,%d}.\n",
				tuning.freq,tuning.qam,tuning.symb,signal.locked,signal.strength,signal.quality);
		m_pNotifyProxy->OnTunerInfo(tuning,signal);
	}
}

ULONG TVSearchMgr::OnCompleteCallBack()
{
	U8 iNitVersion = 0;
	U8 iBatVersion = 0;
	GetVersion(m_eSearchMode,&iNitVersion,&iBatVersion);
	dxreport("TVSearchMgr::OnCompleteCallBack: Current NIT table version:%d. BAT version:%d\n",iNitVersion,iBatVersion);
	// 推送排序后的数据给TVCORE模块
	get_total_dvb_service(m_listDVBService);
	get_ExtServTypeTable(gConfig.stTable_);
	m_pCompleteCallBack(m_listDVBService,gConfig.stTable_);

	return 0;
}

ULONG TVSearchMgr::Cancel()
{
	NotifyDataArrived();
	if(m_pTVSearchMgrThread != NULL )
	{
		m_pTVSearchMgrThread->stop();
	}
	return  0 ;	
}
Table_Status TVSearchMgr::GetCatSdtTableState()
{
	return IGetCatSdtTableState();
}
void TVSearchMgr::SetCatSdtTableState(BOOL v)
{
	ISetCatSdtTableState(v);
}
BOOL TVSearchMgr::CatSdtMissing()
{
	return ICatSdtMissing();
}

