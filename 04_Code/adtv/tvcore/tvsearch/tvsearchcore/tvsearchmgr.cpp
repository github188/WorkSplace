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

// ����: bldStep
// 1. ����pat, cat,sdt
// 2. ����pmt.
// 3. ����nit bat,
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
		NS_sleep(10);	// ����ʱ�п�����̨��ȫ
		m_listBaseSearcher.push_back(pBaseSearcher);
		// �����ι��������ʱֵ
		if(listFilter_[i].timeout > iMaxTimeout)
			iMaxTimeout = listFilter_[i].timeout;
	}

	if(bldStep == BS_PATCATSDT || bldStep == BS_NITBAT)			// ����PAT �����, nit ��Ƶ������Ҳ�߸ô�
	{
		m_iPatTime = NS_GetTickCount();
		m_iMaxTimeout = iMaxTimeout;		// �����ʱ��
	}
	else if(bldStep == BS_PMT)
	{
		// �ڶ��ε���buildSearcher ��ʱ��, 
		U32 iInterval =  NS_GetTickCount()-m_iPatTime ;	
		S32 iAvailableTime = m_iMaxTimeout-iInterval; // ʣ��ʱ��
		if(iAvailableTime >= 0)
		{
			S32  iCompensateTime = iMaxTimeout - iAvailableTime;
			if(iCompensateTime>=0)
			{
				m_iMaxTimeout += iCompensateTime;
			}
			else
			{
				// ��������ʱ��
			}
		}
		else  // ����������߼�, ���ᵽ�⣬��Ϊ��һ��build �ѳ�ʱ.
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
		/// ��Ƶ���nit ����(��Ƶ��)
		freqList.push_back(m_inputTuningParam);
		return;
	}
	// ȫƵģʽ
	freqList.clear();
	TuningParam tParams;
	tParams.symb = m_inputTuningParam.symb;
	tParams.qam  = m_inputTuningParam.qam;

	std::vector<U16> freqs;
	get_full_search_tables(freqs);
	if(freqs.size()) 
	{
		/// ʹ�ö�̬��Ƶ���б�, ��tvsearchLocal �л�ȡ��
		for( U32 i = 0; i < (U32)freqs.size(); i++ )
		{
			tParams.freq = freqs[i]*1000 ;
			freqList.push_back(tParams);
		}
	}
	else
	{
		/// ʹ��Ĭ�ϵ�Ƶ���б�
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
		// nit ����ʱ�������2������
		// ��һ��������Ƶ��, ��ȡ����Ƶ���б�,
		bool res=pTVSearchMgr->SearchOneFreq(freqList[0],BS_NITBAT);
		if(res)
		{
			// NITģʽ��ȡ�ҵ���Ƶ���б�
			freqList.clear();
			ULONG hr = get_tuning_paramfrom_nit(freqList);
			dxreport("get_tuning_paramfrom_nit: freq number:%d\n",freqList.size());
			// �ڶ��飬�����ҵ���Ƶ���б�
			pTVSearchMgr->SearchFreqs(freqList);		}
		else
		{
			dxreport("main freq search failed!!!\n");
		}
	}
	else
	{
		// ��Ƶ���ȫƵ�㣬����Ƶ�ʼ���
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
		// ��������
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

	// usbtv ��Ƶ��
	TunerSignal signal;
	int lock_res=Lock_Tuner_Freq(tuner.freq,tuner.qam,tuner.symb,signal);
	dxreport("Lock_Tuner_Freq res is %d\n",lock_res);
	if(lock_res==-2||lock_res==-1) // -1, -2 ��Ϊ��Ƶʧ��
	{
		OnTunerInfo(tuner,signal); //when not locked, still notify frequency
		dxreport("Lock_Tuner_Freq failed. freq:%d, qam:%d, symb:%d\n",tuner.freq,tuner.qam,tuner.symb);
		return false;		// ��������Ƶʱ����������
	}
	else
	{
		dxreport("Lock_Tuner_Freq succeed. freq:%d, qam:%d, symb:%d,signal. strength:%d,quality:%d,locked:%d\n",\
				tuner.freq,tuner.qam,tuner.symb,signal.strength,signal.quality,signal.locked);
	}
	ULONG hr = SetTuningParam(tuner);
	OnTunerInfo(tuner,signal);
	// ��ʼ��Ƶ������
	DWORD dwStart = NS_GetTickCount();
	BuildSearchers(buildStep);
	//	waitdata arrive			
	bool bWaitingData = true;
	bool res=false;
	while(bWaitingData)
	{
		// ��������
		if(m_pThread->check_stop())
		{
			dxreport("force stop called, exit TVSearchMgrPollProc...\n");
			return false;
		}
		WaitDataStatus wds=WaitDataArrive(bldStep_);	// �ó�Ա����bldStep_��BuildSearchers ����
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
	// ����������CAT ��SDT Ҳ�����⣬���������ƣ����ԼӴ˶δ���
	if(buildStep==BS_PATCATSDT && CatSdtMissing()) // �������߸ô���
	{
		Table_Status status=GetCatSdtTableState();
		switch(status)
		{
			case Cat_Sdt_Table_ok:
			case Sdt_Table_ok:   //cat table not ok., set it to ok
				RemoveOneSearcher(CAT_PID,TableId_Cat);
				RemoveOneSearcher(SDT_PID,TableId_SdtA);
				SetCatSdtTableState(1);		// �������Cat table OK, ��������ж�
				dxreport("cat or sdt timeout Ocurred: %d. continue pmt search...\n",m_iMaxTimeout);
				BuildSearchers(BS_PMT);	// ��û��Cat ��ʱ�����Ժ���, ������PMT
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
	// ��ջ��ȡ��������ɾ���Ľṹ��ָ����������һЩ
	DVBSection *pSection = GetSection();
	if(pSection == NULL )
	{
		dxreport("TVSearchMgrPollProc: pSection is NULL.\n");
		return PDS_WAIT_DATA;
	}

	//��������һ��Ҫ�죬����������������Ӻ�ʱ
	// ������Ҫ����deque�е���������, ���������ݵ�ͬ���ԣ�����
	// һ��section ���ݺ�����ı�ִ�����̣�����ֻ������һ�����ݡ�
	// ����������99%����deque ֻ���һ�����ݡ������˷ѵ�����ֻ������һ��ʱ��
	// ���Ըó���ṹ����������
	int iFlag = analyse_section_data(pSection->pid_,pSection->data_,pSection->dataLen_); 

	U16 pid = pSection->pid_ ; 
	U8  talbe_id = pSection->table_id_;
	FreeSection(pSection);

	if(iFlag == RT_CONTINUE) 
		return PDS_WAIT_DATA;


	// �������ݺ����table_id ����PMT table, ��ɾ����searcher
	if(talbe_id != 0x02) 
	{
		RemoveOneSearcher(pid,talbe_id);
		RemoveSection(pid, talbe_id); // ����ɾ��section �ж�������
	}
	switch(iFlag)
	{
		case  RT_UPDATE_FILTER:  /// �ֹ���ȫƵ����pat�������,��Ҫ�����µĹ��˱�(PMT filters)
//			BuildSearchers(BS_PMT);
			return PDS_PATCATSDT_READY;
		case RT_TABLE_OK:			// �������ɣ��ȴ�����������
			return PDS_WAIT_DATA;
		case RT_SERVICE_OK :		/// ĳƵ�������ɣ��˳�switch,����searcher, ������һƵ������
			OnDVBService();
			break;
		case RT_UPDATE_FREQ:	    

			break;
		default:		// ������
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

// ��ȡ��ǰ��������DVBService
ULONG TVSearchMgr::OnDVBService()
{
	dxreport("TVSearchMgr::OnDVBService Begin >>>\n");
	if(m_eSearchMode != STVMODE_MONITOR_PMT )
	{
		std::vector<DVBService> one_freq_service;
		get_dvb_services(one_freq_service);
		if(m_pNotifyProxy != NULL )
		{
			// ���͵�����������ʾ
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
	// �������������ݸ�TVCOREģ��
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

