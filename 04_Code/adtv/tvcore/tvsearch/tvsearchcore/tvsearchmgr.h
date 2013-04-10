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

// ��Ƶ��������������һ��Ƶ�ʣ�Ҳ������һ���б�
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


	/// ֪ͨ�������,��ȡ�������
	ULONG OnDVBService();
	void   OnProgress(U8 iPercent);
	void   OnTunerInfo(TuningParam& tuning,TunerSignal& signal);
	ULONG OnBeforeSearch(simplethread *pThead){ m_pThread = pThead; return 0;}
	ULONG OnCompleteCallBack();

	Table_Status GetCatSdtTableState();
	void SetCatSdtTableState(BOOL v);
	BOOL CatSdtMissing();

private:
	STVMode			   		 	m_eSearchMode; 		///<����ģʽ(0:Manual,1:Ranger,2:Nit)
	TuningParam					m_inputTuningParam;	// ȫƵ����ʱ, ʹ����qam, symb ����
	TVNotifyProxy   		   *m_pNotifyProxy;			///<APP�ص�����(֪ͨ���ȡ��������)
	BuildStep					bldStep_;	// ��buildSearcher ��ֵ��waitdataarrive �ο�
	
	U32							m_iPatTime;	///<���������֮��ĺ�ʱ
	U32							m_iMaxTimeout;		///<���������ʱֵ(���μ���ʱ�������ʱֵ)
	simplethread      		   *m_pTVSearchMgrThread;  	///<�����߳�
	simplethread *				m_pThread;			// tvsearch thread handler
	std::vector<BaseSearcher*>	m_listBaseSearcher; 	///<��̬�������������б�
	
	std::vector<DVBService>		m_listDVBService;	///<�������
	CompleteCallBackProc        m_pCompleteCallBack;	///<�����������֪ͨ
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
