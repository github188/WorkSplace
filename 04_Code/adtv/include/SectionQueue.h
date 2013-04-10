#ifndef JOYSEE_SECTIONQUEUE_H_
#define JOYSEE_SECTIONQUEUE_H_

#include "xprocess.h"
#include "simplethread.h"
#include "tsdemux_def.h"

#define  LOG_TAG "SectionQueue"
#include "tvlog.h"

#include <queue>

/**
 *	Sample Code:
 *		SectionQueue sections;
 *		sections.SetOnSectionSkel(OnSection);
 *		tsdemux_addSectionFilter(&filter,1,sections.GetOnSectionStub(),&sections);
 *
 */
#define QUEUE_SIZE		(20)
#define SECTION_SIZE	(2048)

#ifdef __cplusplus
extern "C"{
#endif 

struct Section{
public:
	Section(){
		uPID_ = 0;
		uTableId_ = 0;
		hHandle_  = 0;
		hContext_ = 0;
		iActualLen= 0;
		memset(buffer,0,SECTION_SIZE);
	}
	Section(utHandle hFilter,U16 pid,U8 tid,BYTE* pData,U32 lDataSize,utContext context){
		uPID_ = pid;
		uTableId_ = tid;
		hHandle_  = hFilter;
		hContext_ = context;
		iActualLen= lDataSize;
		memcpy(buffer,pData,lDataSize);
	}

	U16 		uPID_;
	U8			uTableId_;
	utContext	hContext_;
	utHandle	hHandle_;

	U32		iActualLen;
	BYTE	buffer[SECTION_SIZE];
};
typedef std::queue<Section*> SectionQueueT;

struct SectionQueue{
public:
	SectionQueue(utFilterDataCallback OnSection,int iQueueSize = QUEUE_SIZE){
		OnSection_ = OnSection;
		CreateSectionPool(iQueueSize);
		thread_.start(SectionQueueProc,this);
	}
	~SectionQueue(){
		thread_.stop();
		DeleteSectionPool();
	}
	
	// 获取OnSection代理对象
	utFilterDataCallback GetOnSectionStub(){
		return SectionQueue::OnSection;
	}
	// 设置OnSection对象
	void SetOnSectionSkel(utFilterDataCallback pCallback){
		OnSection_ = pCallback;
	}
	// 代理函数
	static  void OnSection(utHandle hFilter,long iTuner,utPid pid,utTid tid,utByte *pData,long lDataSize,utContext context){
		
		//LOGTRACE(LOGINFO,"Enter OnSection(%d)\n",pid);

		SectionQueue *pSectionQueue = static_cast<SectionQueue*>(context);
		Section* pSection = pSectionQueue->GetSection();
		if(0 != pSection){
			pSection->uPID_ = pid;
			pSection->uTableId_ = tid;
			pSection->hHandle_  = hFilter;
			pSection->hContext_ = context;
			pSection->iActualLen = lDataSize;
			memcpy(pSection->buffer,pData,lDataSize);
			
			pSectionQueue->SectionPush(pSection);
		}
		else{
			LOGTRACE(LOGINFO,"no enough space.\n");
		}
	}
	// 队列派发循环体
	static  UINT __stdcall SectionQueueProc(LPVOID lpParam){

		simplethread*	pThread	  = reinterpret_cast<simplethread*>(lpParam);
		SectionQueue*   pSectionQueue = reinterpret_cast<SectionQueue*>(pThread->get_arglist());
		while (!pThread->check_stop())
		{
			// CA卡消息监测
			if(pSectionQueue->HasSection()){
				pSectionQueue->DispatchSecData();
			}
			else{
				NS_sleep(500);
				//LOGTRACE(LOGINFO,"SectionQueueProc  running...\n");
			}
		}

		return 0;
	}

	// 压段入对列
	void	SectionPush(Section *pSection){
		dataqueue_.push(pSection);
	}
	// 获取空闲段
	Section* GetSection(){
		Section *pSection = 0;
		if(!idelqueue_.empty())
		{
			pSection = idelqueue_.front();
			idelqueue_.pop();
		}
		return pSection;
	}

	bool HasSection(){
		return !dataqueue_.empty();
	}
	// 派发段数据
	void DispatchSecData(){
		Section* pSection = SectionPop();
		if(0 != pSection && 0 != OnSection_)
		{
			//LOGTRACE(LOGINFO,"Dispatch OnSection_.\n");
			DWORD dwTime1 = NS_GetTickCount();
			OnSection_(pSection->hHandle_,
				0,
				pSection->uPID_,
				pSection->uTableId_,
				pSection->buffer,
				pSection->iActualLen,
				pSection->hContext_);
			DWORD dwTime2 = NS_GetTickCount();
			LOGTRACE(LOGINFO,"OnSection_ cost time =%d\n",dwTime2-dwTime1);
			SetSection(pSection);

		}
	}
	void  CleanDataQueue(){
		AutoLockT lock(mutex_);
		Section *pSection = 0;
		while(!dataqueue_.empty())
		{
			pSection = dataqueue_.front();
			dataqueue_.pop();
			idelqueue_.push(pSection);
		}
	}

private:

	//  构建段数据队列池
	void CreateSectionPool(int iPoolSize){
		for(int i = 0 ; i < iPoolSize ; i++)
		idelqueue_.push(new Section());
	}
	//  析构段数据对列池
	void DeleteSectionPool(){
		Section *pSection = 0;
		while(!dataqueue_.empty())
		{
			pSection = dataqueue_.front();
			dataqueue_.pop();
			delete pSection;
		}

		while(!idelqueue_.empty())
		{
			pSection = idelqueue_.front();
			idelqueue_.pop();
			delete pSection;
		}
	}
	
	// 获取段数据
	Section* SectionPop(){
		AutoLockT lock(mutex_);
		Section *pSection = 0;
		if(!dataqueue_.empty())
		{
			pSection = dataqueue_.front();
			dataqueue_.pop();
		}
		return pSection;
	}

	// 将使用完的段放回空闲队列
	void SetSection(Section* pSection){
		pSection->uPID_ = 0;
		pSection->uTableId_ = 0;
		pSection->hContext_ = 0;
		pSection->hHandle_  = 0;
		pSection->iActualLen= 0;
		memset(pSection->buffer,0,SECTION_SIZE);
		idelqueue_.push(pSection);
	}
	
	SectionQueueT	dataqueue_;
	SectionQueueT	idelqueue_;

	MutexT					mutex_;
	simplethread			thread_;
	utFilterDataCallback	OnSection_;
};

#ifdef __cplusplus
}
#endif 

#endif // defined(JOYSEE_SECTIONQUEUE_H_)