#ifndef RANGESEARCH_H_HEADER_INCLUDED_B26FA543
#define RANGESEARCH_H_HEADER_INCLUDED_B26FA543

#include "baseSearch.h"
#include "simplethread.h"
#include "tvnotify.h"
#include "searchDef.h"


typedef enum TagWaitDataStatus
{
	WDS_DATA_NOT_READY,
		WDS_DATA_READY,
		WDS_DATA_TIMEOUT
}WaitDataStatus;

typedef enum TagProcessDataStatus
{
	PDS_WAIT_DATA,
		PDS_DATA_READY,
		PDS_PATCATSDT_READY,
}ProcessDataStatus;


typedef ULONG (* CompleteCallBackProc)(std::vector<DVBService> &serices,std::vector<ServiceTypeTableItem> &table);

// 按频率搜索，可以是一个频率，也可以是一个列表
class TVSearchMgr : public BaseSearchMgr
{

public:
	TVSearchMgr(){}
	void Init(STVMode eMode,TuningParam& sTuningParam,TVNotifyProxy *pNotify,CompleteCallBackProc pCallBack) ; 
	void UnInit();
	virtual ~TVSearchMgr(){UnInit();}
	void Start(INOUT DVBServiceListT& services);
	virtual ULONG Cancel();
	ULONG BuildSearchers(IN BuildStep bldStep);
	void   RemoveOneSearcher(U16 pid,U8 table_id);
	void   ClearSearchers();
	void GetListTunnerParams(STVMode searchMode,std::vector<TuningParam> &FreqList);


	/// 通知分析完成,获取分析结果
	ULONG OnDVBService();
	void   OnProgress(U8 iPercent);
	void   OnTunerInfo(TuningParam& tuning,TunerSignal& signal);
	ULONG OnBeforeSearch(simplethread *pThead){ m_pThread = pThead; return 0;}
	ULONG OnCompleteCallBack();

	Table_Status GetCatSdtTableState();
	void SetCatSdtTableState(BOOL v);
	BOOL CatSdtMissing();

private:
	STVMode			   		 	m_eSearchMode; 		///<搜索模式(0:Manual,1:Ranger,2:Nit)
	TuningParam					m_inputTuningParam;	// 全频搜索时, 使用其qam, symb 参数
	TVNotifyProxy   		   *m_pNotifyProxy;			///<APP回调函数(通知进度、搜索结果)
	BuildStep					bldStep_;	// 由buildSearcher 赋值，waitdataarrive 参考
	
	U32							m_iPatTime;	///<计算过滤器之间的耗时
	U32							m_iMaxTimeout;		///<过滤器最大超时值(二次加入时会调整超时值)
	simplethread      		   *m_pTVSearchMgrThread;  	///<搜索线程
	simplethread *				m_pThread;			// tvsearch thread handler
	std::vector<BaseSearcher*>	m_listBaseSearcher; 	///<动态的搜索器对象列表
	
	std::vector<DVBService>		m_listDVBService;	///<搜索结果
	CompleteCallBackProc        m_pCompleteCallBack;	///<搜索完成主动通知
	static HANDLE						m_hTvDevice;
	WaitDataStatus WaitDataArrive(BuildStep buildStep);
	ProcessDataStatus ProcessData();
	void SearchFreqs(std::vector<TuningParam> &listFreq);
	bool SearchOneFreq (TuningParam tuner, BuildStep buildStep);
	static UINT __stdcall TVSearchMgrPollProc(LPVOID lpParam) ;
	static BOOL Lock_Tuner_Freq(U32 freq, U32 qam, U32 symb,TunerSignal& aSignal);
	static int CalculatePercent(int num, size_t total);
	
};

#endif /* RANGESEARCH_H_HEADER_INCLUDED_B26FA543 */
