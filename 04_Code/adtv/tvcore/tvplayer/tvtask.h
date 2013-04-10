#ifndef __JOYSEE_TVTASK_H__
#define __JOYSEE_TVTASK_H__

#include "tvcomm.h"
#include "simplethread.h"
#include "xprocess.h"
#include "tsdemux.h"
#include "stbruntime.h"

#define	 LOG_TAG "libtvplayer"
#include "tvlog.h"


namespace TVCoreUtil{

struct TVTask;
struct TVTaskPacket{
	TVTask* pTask;		///<�������ݵ�����
	U16     pid;		///<Section pid
	U8*		pBuffer;	///<�������ݻ�����
	U32     iSize;		///<�������ݵĴ�С
	bool    bIsTimeout;	///<��ʶ������/��ʱ��
};

struct TSFilter
{
public:
	TSFilter(TVTask* pTask,utSectionFilter* filters,U32 lFilterCount)
	{
		m_pTask   = pTask;
		m_hFilter = tsdemux_addSectionFilter(filters,lFilterCount,DemuxCallBack,(void**)this);
	}
	virtual ~TSFilter(){}

	void start_search(){}
	void stop_search();
	void SetCanDelete(bool data)
	{
		m_bCanDelete = data;
	}
	bool GetCanDelete(){return m_bCanDelete;}

protected:
	static void DemuxCallBack( utHandle hFilter,long iTuner,utPid pid,utTid tid,utByte *data,long datasize,utContext context);
	static void DelSectionFilterCallBack(utHandle hFilter,utContext context);

	virtual bool on_section_got(const U16 pid,const U8 *buffer, U32 size);
	virtual void on_time_out(){};

	bool		m_bCanDelete;
	HANDLE		m_hFilter;
	TVTask*		m_pTask;	///<����(���������ڵ�����)
};

struct TVTaskMgr;
struct TVTask{
	TVTask(TVTaskMgr* pTaskMgr) 
		: m_pTaskMgr(pTaskMgr){};

	virtual ~TVTask(){
		LOGTRACE(LOGINFO,"Enter TVTask::~TVTask().\n");
	};

	// ��ʼһ������
	virtual bool task_start() = 0;
	// ȡ��һ������
	virtual bool task_cancel() = 0;

	// ������
	virtual bool task_process(const U16 pid,U8 const* buffer, U32 size)= 0;
	// ����ʱ(����filter������ĳ��filter��ʱ֪ͨ)
	virtual bool task_timeout(const U16 pid,const U8 table_id)= 0;
	// �������(��/����������)
	virtual bool task_complete(bool bIsTimeout = false)=0;		

	// ��ȡ����Ŀ�ʼʱ��
	virtual U32		GetStartTime()=0;
	// ��ȡ����ĳ�ʱʱ��(��̬����:����һ������Ĵ���ʱ��)
	virtual U32     GetUsableTime()=0;
	// ������������filter��������(����һ��)
	virtual bool	BuildFilter()=0;
	// �����������filter��������
	virtual void	ClearFilter()=0;
	// ������������ض���filter����
	virtual void	ReleaseFilter(U16 pid,U8 table_id)=0;
	// ��������ѹ����(ȫ���������������,���������������)
	bool put_data(const U16 pid,const U8* buffer,U32 size);
	// ��ȡ�������������
	TVTaskMgr* GetTaskMgr(){
		return m_pTaskMgr;
	}

private:
	TVTaskMgr*		m_pTaskMgr;	///<�������������
};

struct TVTaskMgr : public NovelSupertv::stbruntime::Thread
{
public:
	TVTaskMgr(): Thread("TsCatcherThread",64*1024)
	{
		m_hTaskEvent = NS_CreateEvent(NULL,FALSE,FALSE,NULL);
		start();
	}

	virtual ~TVTaskMgr(){
		
		LOGTRACE(LOGINFO,"Enter ~TVTaskMgr().\n");

		clear();
		LOGTRACE(LOGINFO,"~TVTaskMgr,clear.\n");

		stop();
		LOGTRACE(LOGINFO,"~TVTaskMgr,stop.\n");

		if(NULL !=m_hTaskEvent)
		{
			NS_DeleteEvent(m_hTaskEvent);
		}

		LOGTRACE(LOGINFO,"Leave ~TVTaskMgr().\n");
	}
	
	// ����һ������
	bool add_task(TVTask* pTask){
		bool bRet = false;
		if(NULL != pTask)
		{
			AutoLockT lock(m_oTaskLock);
			// ����������������
			m_listTasks.insert(pTask);
			bRet = true ;
		}

		return bRet ;
	}

	// ɾ��ָ��������
	void del_task(TVTask* pTask=NULL)
	{	
		LOGTRACE(LOGINFO,"Enter del_task.\n");

		if(!is_curthread()){
			AutoLockT lock(m_oTaskLock);
			inner_del_task(pTask);
		}
		else{
			inner_del_task(pTask);
		}

		LOGTRACE(LOGINFO,"Leave del_task.\n");
	}

	// ������
	void process(){
		bool bRet = false;
		TVTaskPacket* pData = pop();
		if((NULL != pData) && (NULL != pData->pTask))
		{
			{
				AutoLockT lock(m_oTaskLock);
				if(find(pData->pTask))
				{
					// �����ʹ�õ�ʱ��
					U32 iUsableTime = pData->pTask->GetUsableTime();
					// ������ִ�е�ʱ��
					U32 iUsedTime= NS_GetTickCount()-pData->pTask->GetStartTime();
					if(iUsedTime > iUsableTime)
					{
						LOGTRACE(LOGINFO,"----[%d] Ocurred timeout.\n",iUsableTime);
						pData->pTask->task_timeout(pData->pid,pData->pBuffer[0]);
						bRet = true ;
					}
					else
					{
						// �ɷ�����
						bRet = pData->pTask->task_process(pData->pid,pData->pBuffer,pData->iSize);
					}

					if(bRet)
					{
						// ��������
						pData->pTask->task_complete(false);
						// ɾ������
						del_task(pData->pTask);
						if(m_listTasks.empty())
						{
							ReleaseTaskPacket();
						}
					}
				}
			}

			// �ͷ��������� 
			ReleaseTaskPacket(pData);
		}
		else
		{
			// ��ʱ�ж�
			std::set<TVTask*>::iterator it;
			std::list<TVTask*> deltasks;
			{
				AutoLockT lock(m_oTaskLock);
				it = m_listTasks.begin();
				for(; it != m_listTasks.end(); )
				{
					// �����ʹ�õ�ʱ��
					U32 iUsableTime = (*it)->GetUsableTime();
					// ������ִ�е�ʱ��
					U32 iUsedTime= NS_GetTickCount()-(*it)->GetStartTime();
					if(iUsedTime > iUsableTime)
					{
						//LOGTRACE(LOGINFO,"----[%d] Ocurred timeout.\n",iUsableTime);
						deltasks.push_back(*it);
						m_listTasks.erase(it++);
					}
					else
						it++;
				}
			}
			std::list<TVTask*>::iterator it1;
			// ���������ڵ��ûص�����
			for(it1 = deltasks.begin() ; it1 != deltasks.end() ; it1++)
			{
				(*it1)->task_complete(true);
				// ȡ������
				(*it1)->task_cancel();
				// �ͷ��������
				delete (*it1);
			}
		}
	}

	// ��������ѹ����
	bool push(TVTask* pTask,const U16 pid ,const U8* buffer,U32 size)
	{
		bool bRet = false;
		if(NULL != buffer  && size >0)
		{
			AutoLockT lock(m_ObjLock);
			TVTaskPacket* pData = new TVTaskPacket;
			if(NULL != pData)
			{
				pData->pid   = pid;
				pData->pTask = pTask ; 
				pData->bIsTimeout = false;
				pData->iSize   = size;
				pData->pBuffer = new U8[size];
				memcpy(pData->pBuffer,buffer,pData->iSize);
				m_taskdatas.push_back(pData);
				notify_packet_arrived();
				bRet = true;
			}
		}

		return bRet;
	}

	// ��������ѹ����
	bool push(TVTaskPacket* pData){
		bool bRet = false;
		if(NULL != pData)
		{
			AutoLockT lock(m_ObjLock);
			m_taskdatas.push_back(pData);
			bRet = true;
		}
		return bRet ;
	}

	// �ȴ��������ݵ���
	DWORD wait_task_packet(){
		DWORD dwRet = WAIT_OBJECT_0;
		if(m_taskdatas.empty())
		{
			dwRet = NS_WaitEvent(m_hTaskEvent,200);
		}
		return dwRet ;
	}
	
private:
	virtual U32 do_run(){

		LOGTRACE(LOGINFO,"Enter TVTaskMgr::do_run().\n");
		
		while( !signalled() )
		{
			wait_task_packet();
			process();
			// NS_sleep(10);
		}

		LOGTRACE(LOGINFO,"Leave TVTaskMgr::do_run().\n");
		return 0;
	}

	void inner_del_task(TVTask* pTask=NULL)
	{	
#if 0 		
		{
			// ������������ص����ݰ�
			AutoLockT lock(m_ObjLock);
			std::deque<TVTaskPacket*>::iterator it = m_taskdatas.begin();
			for(; it != m_taskdatas.end();it++)
			{
				if((*it)->pTask == pTask){
					ReleaseTaskPacket(*it);
					it = m_taskdatas.erase(it);
				}
			}
		}
#endif 		
		std::set<TVTask*>::iterator it;
		it = m_listTasks.find(pTask);
		if(it != m_listTasks.end())
		{
			// ȡ������
			(*it)->task_cancel();
			// �ͷ��������
			delete (*it);
			// �������������
			m_listTasks.erase(it);
		}
	}

	// ɾ��ȫ������
	void clear(){
		AutoLockT lock(m_oTaskLock);
		std::set<TVTask*>::iterator it = m_listTasks.begin();
		for (; it != m_listTasks.end() ; it++)
		{
			// ȡ������
			(*it)->task_cancel();
			// �ͷ��������
			delete (*it);
		}

		// �����������
		m_listTasks.clear();

		// ������е�����
		ReleaseTaskPacket();
	};

	// �������ݳ���
	TVTaskPacket* pop()
	{
		TVTaskPacket* pData = NULL;
		AutoLockT lock(m_ObjLock);
		if(!m_taskdatas.empty())
		{
			pData = m_taskdatas.front();
			m_taskdatas.pop_front();
		}

		return pData ; 
	}

	// ֪ͨ�������ݵ���
	void  notify_packet_arrived(){
		(NULL != m_hTaskEvent) ? NS_SetEvent(m_hTaskEvent) : NULL ;
	}

	// �ͷ���������
	bool ReleaseTaskPacket(TVTaskPacket* pData)
	{
		bool bRet = false;
		if(NULL != pData)
		{
			delete[] pData->pBuffer; 
			pData->pTask = NULL;
			delete pData;
			bRet = true;
		}

		return bRet;
	}

	// �ͷ����е���������
	void ReleaseTaskPacket()
	{
		AutoLockT lock(m_ObjLock);
		TVTaskPacket* pData = NULL;
		for (; !m_taskdatas.empty() ; )
		{
			pData = m_taskdatas.front();
			m_taskdatas.pop_front();
			ReleaseTaskPacket(pData);
		}
	}


	// ��������(��������л�ѹ���������������)
	bool find(TVTask* pTask){
		bool bRet = false;
		// AutoLockT lock(m_oTaskLock);
		std::set<TVTask*>::iterator it;
		it = m_listTasks.find(pTask);
		if(it != m_listTasks.end())
		{
			bRet = true ;
		}

		return bRet;
	} 

	MutexT						m_ObjLock;		///<����ͬ��
	std::deque<TVTaskPacket*>	m_taskdatas;	///<�������ݼ���
	
	MutexT						m_oTaskLock;	///<����ͬ��
	std::set<TVTask*>			m_listTasks;	///<�����б�
	
	HANDLE						m_hTaskEvent;	///<�����¼�����
	//simplethread				m_hThread;		///<�����ɷ��߳�
};

}
#endif // defined(__JOYSEE_TVTASK_H__)
