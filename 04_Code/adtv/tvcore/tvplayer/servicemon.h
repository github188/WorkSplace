#ifndef JOYSEE_SERVICE_MON_H_
#define JOYSEE_SERVICE_MON_H_

#include "tvtask.h"
#include "tvcomm.h"

namespace TVCoreUtil{


enum ServiceMonEventType
{
	PMT_PID_CHANGE,
	AV_PID_CHANGE,
	SERVICE_NAME_CHANGE,
	NIT_CHANGE,
	BAT_CHANGE,
};
// 
// DVB 服务/节目信息变化监测任务
// 	:Service Name 变更监测
// 	:PAT table    PMTID变化监测
// 	:PMT table    A/V 变化监测
struct ServiceMonTask : public TVTask
{
public:
	ServiceMonTask(TVTaskMgr* pTaskMgr,const DVBService &stServiceInfo, void * context, void * callback)
		:TVTask(pTaskMgr)
	{
		m_iMaxTimeout = 0;
		m_bSwitchPMTMon = false;
		m_iPMTVersion = m_iSDTVersion = m_iPATVersion = 0xFF;
		m_iBATVersion = m_iNitVersion = 0xFF;
		m_iCATVersion = 0xFF;
		m_pTSFilter[0]= m_pTSFilter[1]= 0;
		
		memcpy(&m_stService,&stServiceInfo,sizeof(DVBService));
		m_Context = context;
		m_MonEventCallback = callback;

//		m_iBATVersion = m_stService.batVersion;
//		m_iNitVersion = m_stService.nitVersion;
		GetNitBatVersions(m_iNitVersion,m_iBATVersion, m_CASystemID);
		LOGTRACE(LOGERR,"nitV = %hhu,batV = %hhu caid = %d\n",m_iNitVersion,m_iBATVersion, m_CASystemID);

		if(m_CASystemID != (int)0xFFFFFFFF)
			task_start();
		else
			LOGTRACE(LOGERR,"not init ca system id!!!!!!!\n",m_iNitVersion,m_iBATVersion, m_CASystemID);

	}
	
	virtual ~ServiceMonTask(){};

	virtual bool task_start();
	virtual bool task_cancel();
	virtual bool task_process(const U16 pid,U8 const* buffer, U32 size);
	virtual bool task_complete(bool bIsTimeout = false);
	virtual bool task_timeout(const U16 pid,const U8 table_id){
		LOGTRACE(LOGERR,"task_timout(%d,%d)\n",pid,table_id);
		return true;
	}
	// 任务开始的时间
	virtual U32		GetStartTime(){
		return m_iStarttime;
	}
	// 给一个任务分配的时间
	virtual U32     GetUsableTime(){
		return (U32)m_iMaxTimeout;
	}

	bool OnEventOccur(DVBService & service, ServiceMonEventType type);

private:

	virtual bool	BuildFilter();
	virtual void	ClearFilter();
	virtual void	ReleaseFilter(U16 pid,U8 table_id){}

	U16 GetPMTId(){
		return m_stService.pmt_id;
	}

	void UpdateVersions()
	{
		FILE *fp=fopen(MONITOR_VERSION_FILE_PATH,"w");
		if(fp)
		{
			fprintf(fp,"nitversion:%d\n",m_iNitVersion);
			fprintf(fp,"batversion:%d\n",m_iBATVersion);
			fprintf(fp,"systemID:%d\n",m_CASystemID);
			fclose(fp);
		}
	}

	void GetNitBatVersions(unsigned char & nitV, unsigned char& batV, int & systemid)
	{
		nitV = 0xFF;
		batV = 0xFF;
		//默认为永新
	//	systemid = 0x4a02;
		systemid = 0xFFFFFFFF;

		FILE * versionF = fopen(MONITOR_VERSION_FILE_PATH,"r");
		if(versionF == NULL)
			return;

		char buf[256] = "\0";
		while(true)
		{
			memset(buf, 0, sizeof(buf));
			char * pKV = fgets(buf,255,versionF);
			if(pKV == NULL)
				break;
			int v = 0xFF;
			char * pV = strchr(pKV, ':');
			if(pV != NULL)
			{
				sscanf(++pV,"%d",&v);
			}	
			if(strstr(pKV,"nit"))
			{
				nitV = v;
			}
			else if(strstr(pKV,"bat"))
			{
				batV = v;
			}
			else if(strncmp(pKV,"systemID",strlen("systemID")) == 0)
			{
				systemid = v;
			}
		}
		fclose(versionF);
	}
	
	bool on_sdt_got(const U8* pData,const U32 iLen);//；
	int on_pat_got(const U8* pData,const U32 iLen);
	bool on_pmt_got(const U8* pData,const U32 iLen);
	bool on_bat_got(const U8 * pData, const U32 iLen);
	bool on_nit_got(const U8 * pData, const U32 iLen);
	bool on_cat_got(const U8 * pData, U32 iLen);
	
	bool					m_bSwitchPMTMon;	///<
	U32						m_iStarttime;		///<任务的开始时间
	//U32					m_iTime1,m_iTime2;	///<计算过滤器之间的耗时
	U32						m_iMaxTimeout;		///<过滤器最大超时值(二次加入时会调整超时值)

	///<监测表的版本及监测数据等
	U8						m_iPATVersion;		///<PAT表版本信息
	U8						m_iPMTVersion;		///<PMT表版本信息
	U8						m_iSDTVersion;		///<SDT表版本信息
	U8						m_iBATVersion;		///<BAT表版本信息
	U8						m_iNitVersion;		///<NIT表版本信息
	U8						m_iCATVersion;		///<CAT表版本信息

	int						m_CASystemID;
	int						m_ShumaPDSD;
	//U16 					m_iPMTId;						///<当前节目的PMTID
	//std::string 			m_strSName;						///<当前节目名称
	//DVBStream				m_stVideo;						///<当前节目视频流
	//DVBStream 			m_stAudio[AUDIOSTREAM_MAXCOUNT];///<当前节目音频流
	void *					m_Context;
	void *					m_MonEventCallback;	
	DVBService              m_stService;		///<当前服务信息
	TSFilter*				m_pTSFilter[2];		
}; 

}

#endif // defined(JOYSEE_SERVICE_MON_H_)
