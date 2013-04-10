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
	TVTask* pTask;		///<任务数据的宿主
	U16     pid;		///<Section pid
	U8*		pBuffer;	///<任务数据缓冲区
	U32     iSize;		///<任务数据的大小
	bool    bIsTimeout;	///<标识是数据/超时包
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
	TVTask*		m_pTask;	///<宿主(数据隶属于的任务)
};

struct TVTaskMgr;
struct TVTask{
	TVTask(TVTaskMgr* pTaskMgr) 
		: m_pTaskMgr(pTaskMgr){};

	virtual ~TVTask(){
		LOGTRACE(LOGINFO,"Enter TVTask::~TVTask().\n");
	};

	// 开始一个任务
	virtual bool task_start() = 0;
	// 取消一个任务
	virtual bool task_cancel() = 0;

	// 任务处理
	virtual bool task_process(const U16 pid,U8 const* buffer, U32 size)= 0;
	// 任务超时(任务filter序列中某个filter超时通知)
	virtual bool task_timeout(const U16 pid,const U8 table_id)= 0;
	// 任务完成(正/非正常结束)
	virtual bool task_complete(bool bIsTimeout = false)=0;		

	// 获取任务的开始时间
	virtual U32		GetStartTime()=0;
	// 获取任务的超时时间(动态计算:限制一个任务的处理时间)
	virtual U32     GetUsableTime()=0;
	// 构建该任务中filter对象序列(至少一个)
	virtual bool	BuildFilter()=0;
	// 清除该任务中filter对象序列
	virtual void	ClearFilter()=0;
	// 清除该任务中特定的filter对象
	virtual void	ReleaseFilter(U16 pid,U8 table_id)=0;
	// 任务数据压队列(全部交给任务管理器,由任务管理器调度)
	bool put_data(const U16 pid,const U8* buffer,U32 size);
	// 获取任务管理器对象
	TVTaskMgr* GetTaskMgr(){
		return m_pTaskMgr;
	}

private:
	TVTaskMgr*		m_pTaskMgr;	///<任务管理器对象
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
	
	// 增加一个任务
	bool add_task(TVTask* pTask){
		bool bRet = false;
		if(NULL != pTask)
		{
			AutoLockT lock(m_oTaskLock);
			// 插入任务至序列中
			m_listTasks.insert(pTask);
			bRet = true ;
		}

		return bRet ;
	}

	// 删除指定的任务
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

	// 任务处理
	void process(){
		bool bRet = false;
		TVTaskPacket* pData = pop();
		if((NULL != pData) && (NULL != pData->pTask))
		{
			{
				AutoLockT lock(m_oTaskLock);
				if(find(pData->pTask))
				{
					// 任务可使用的时长
					U32 iUsableTime = pData->pTask->GetUsableTime();
					// 任务已执行的时长
					U32 iUsedTime= NS_GetTickCount()-pData->pTask->GetStartTime();
					if(iUsedTime > iUsableTime)
					{
						LOGTRACE(LOGINFO,"----[%d] Ocurred timeout.\n",iUsableTime);
						pData->pTask->task_timeout(pData->pid,pData->pBuffer[0]);
						bRet = true ;
					}
					else
					{
						// 派发任务
						bRet = pData->pTask->task_process(pData->pid,pData->pBuffer,pData->iSize);
					}

					if(bRet)
					{
						// 结束任务
						pData->pTask->task_complete(false);
						// 删除任务
						del_task(pData->pTask);
						if(m_listTasks.empty())
						{
							ReleaseTaskPacket();
						}
					}
				}
			}

			// 释放任务数据 
			ReleaseTaskPacket(pData);
		}
		else
		{
			// 超时判断
			std::set<TVTask*>::iterator it;
			std::list<TVTask*> deltasks;
			{
				AutoLockT lock(m_oTaskLock);
				it = m_listTasks.begin();
				for(; it != m_listTasks.end(); )
				{
					// 任务可使用的时长
					U32 iUsableTime = (*it)->GetUsableTime();
					// 任务已执行的时长
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
			// 避免在锁内调用回调函数
			for(it1 = deltasks.begin() ; it1 != deltasks.end() ; it1++)
			{
				(*it1)->task_complete(true);
				// 取消任务
				(*it1)->task_cancel();
				// 释放任务对象
				delete (*it1);
			}
		}
	}

	// 任务数据压队列
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

	// 任务数据压队列
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

	// 等待任务数据到达
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
			// 清除与该任务相关的数据包
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
			// 取消任务
			(*it)->task_cancel();
			// 释放任务对象
			delete (*it);
			// 任务序列中清除
			m_listTasks.erase(it);
		}
	}

	// 删除全部任务
	void clear(){
		AutoLockT lock(m_oTaskLock);
		std::set<TVTask*>::iterator it = m_listTasks.begin();
		for (; it != m_listTasks.end() ; it++)
		{
			// 取消任务
			(*it)->task_cancel();
			// 释放任务对象
			delete (*it);
		}

		// 清除任务序列
		m_listTasks.clear();

		// 清除所有的数据
		ReleaseTaskPacket();
	};

	// 任务数据出队
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

	// 通知任务数据到达
	void  notify_packet_arrived(){
		(NULL != m_hTaskEvent) ? NS_SetEvent(m_hTaskEvent) : NULL ;
	}

	// 释放任务数据
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

	// 释放所有的任务数据
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


	// 查找任务(清除队列中积压的已完成任务数据)
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

	MutexT						m_ObjLock;		///<数据同步
	std::deque<TVTaskPacket*>	m_taskdatas;	///<任务数据集合
	
	MutexT						m_oTaskLock;	///<任务同步
	std::set<TVTask*>			m_listTasks;	///<任务列表
	
	HANDLE						m_hTaskEvent;	///<任务事件对象
	//simplethread				m_hThread;		///<任务派发线程
};

}
#endif // defined(__JOYSEE_TVTASK_H__)
