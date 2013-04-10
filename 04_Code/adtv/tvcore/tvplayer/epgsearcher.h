#ifndef __NOVERSUPEL_EPGSEARCHER_H__
#define __NOVERSUPEL_EPGSEARCHER_H__

#include "tvtask.h"
#include "tvcomm.h"
#include "simplethread.h"

namespace TVCoreUtil{

struct EpgController {
public:
	EpgController(HANDLE hDevice,TVNOTIFY pNotify,TVTaskMgr* pTaskMgr=0);
	virtual ~EpgController();

	// ��ʼEPG��������
	HANDLE startEpgSearch(const TuningParam& tuning,const U16 sid,const EITEventType evtType);
	// ȡ��EPG��������
	void cancelEpgSearch(HANDLE hTask);
	// ��ȡEPGȫ������
	bool getEpgData(EPGDataBaseT& epgs);
	// ��ȡָ�������µĽ�Ŀ(�¼�)��Ϣ
	bool getEpgDataBySID(const U16 iServiceId,EpgEventSet& events);
	// ��ȡָ��������ָ��ʱ��εĽ�Ŀ(�¼�)��Ϣ
	bool getEpgDataByDuration(const U16 iServiceId,EpgEventSet& events,const U32 iStartTime,const U32 iEndTime);

	void OnEpgCompletion(void* wParam,void* pParam);
	void OnEpgDataGot(void * wParam, void * pParam);

	void NotifyChange(TVNOTIFY pNotify);
private:
	HANDLE		    m_hDevice;			///<
	EPGDataBaseT	m_mapEpgDB;			///<
	MutexT	m_mapEpgMutex_;	//ȫ��EPG��Ϣ��

	TVNOTIFY		m_pNotify;			///<
	U16				m_iServiceId;		///<��ǰ��ĿID

	bool			m_bActiveTaskMgr;	///<
	TVTaskMgr*		m_pTaskMgr;			///<
	std::set<TVTask*>	m_listTasks;	///<�����б�
};

struct EpgSearchTask : public TVTask
{
public:
		EpgSearchTask(EpgController* pController,TVTaskMgr* pTaskMgr,U16 iServiceId,EITEventType evtType)
			:TVTask(pTaskMgr),m_pController(pController), m_evtType(evtType),m_iServiceId(iServiceId),
			 m_iMaxTimeout(0),m_iTime1(0),m_iTime2(0),m_iStarttime(0)
		{
			task_start();
		}

		virtual ~EpgSearchTask(){};

		virtual bool task_start();
		virtual bool task_cancel();
		virtual bool task_process(const U16 pid,U8 const* buffer, U32 size);
		virtual bool task_complete(bool bIsTimeout = false);
		virtual bool task_timeout(const U16 pid,const U8 table_id){
			LOGTRACE(LOGINFO,"task_timout(%d,%d)\n",pid,table_id);
			return true;
		}
		// ����ʼ��ʱ��
		virtual U32		GetStartTime(){
			return m_iStarttime;
		}
		// ��һ����������ʱ��
		virtual U32     GetUsableTime(){
			return (U32)1.25*m_iMaxTimeout;
		}

private:

		static int TVepgCallBack(int notifyCode,long lParam,void *pParam);

		virtual bool	BuildFilter();
		virtual void	ClearFilter();
		virtual void	ReleaseFilter(U16 pid,U8 table_id){}

		U32						m_iStarttime;		///<����Ŀ�ʼʱ��
		U32						m_iTime1,m_iTime2;	///<���������֮��ĺ�ʱ
		U32						m_iMaxTimeout;		///<���������ʱֵ(���μ���ʱ�������ʱֵ)

		U16						m_iServiceId;		///<��ǰ��ĿID
		EITEventType			m_evtType;			///<��ǰ�����¼�����
		EpgController*			m_pController;
		
		std::vector<TSFilter*>	m_listTSFilter;		///<filter��������
};

struct PFEventSearchTask : public TVTask
{
public:
	PFEventSearchTask(TVTaskMgr* pTaskMgr,U16 iServiceId,EITEventType evtType,TVNOTIFY pNotify)
		:TVTask(pTaskMgr)
	{
		m_pTSFilter = 0;
		m_uCRCValue = 0;
		m_iMaxTimeout = 0;
		m_evtType = evtType;
		m_pNotify = pNotify;
		m_iCurServiceId = iServiceId;
		task_start();
	}
	
	virtual ~PFEventSearchTask(){};

	virtual bool task_start();
	virtual bool task_cancel();
	virtual bool task_process(const U16 pid,U8 const* buffer, U32 size);
	virtual bool task_complete(bool bIsTimeout = false);
	virtual bool task_timeout(const U16 pid,const U8 table_id){
		LOGTRACE(LOGINFO,"task_timout(%d,%d)\n",pid,table_id);
		return true;
	}
	// ����ʼ��ʱ��
	virtual U32		GetStartTime(){
		return m_iStarttime;
	}
	// ��һ����������ʱ��
	virtual U32     GetUsableTime(){
		return (U32)1.25*m_iMaxTimeout;
	}

private:

	virtual bool	BuildFilter();
	virtual void	ClearFilter();
	virtual void	ReleaseFilter(U16 pid,U8 table_id){}

	U32					    m_uCRCValue;
	U32						m_iStarttime;		///<����Ŀ�ʼʱ��
	U32						m_iMaxTimeout;		///<���������ʱֵ(���μ���ʱ�������ʱֵ)

	U16						m_iCurServiceId;	///<��ǰ��ĿID
	EITEventType			m_evtType;			///<��ǰ�����¼�����
	TVNOTIFY				m_pNotify;			///<

	EPGDataBaseT			m_epgs;
	TSFilter*				m_pTSFilter;

}; 

}

#endif // defined(__NOVERSUPEL_EPGSEARCHER_H__)
