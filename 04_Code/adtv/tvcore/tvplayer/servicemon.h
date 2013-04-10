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
// DVB ����/��Ŀ��Ϣ�仯�������
// 	:Service Name ������
// 	:PAT table    PMTID�仯���
// 	:PMT table    A/V �仯���
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
	// ����ʼ��ʱ��
	virtual U32		GetStartTime(){
		return m_iStarttime;
	}
	// ��һ����������ʱ��
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
		//Ĭ��Ϊ����
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
	
	bool on_sdt_got(const U8* pData,const U32 iLen);//��
	int on_pat_got(const U8* pData,const U32 iLen);
	bool on_pmt_got(const U8* pData,const U32 iLen);
	bool on_bat_got(const U8 * pData, const U32 iLen);
	bool on_nit_got(const U8 * pData, const U32 iLen);
	bool on_cat_got(const U8 * pData, U32 iLen);
	
	bool					m_bSwitchPMTMon;	///<
	U32						m_iStarttime;		///<����Ŀ�ʼʱ��
	//U32					m_iTime1,m_iTime2;	///<���������֮��ĺ�ʱ
	U32						m_iMaxTimeout;		///<���������ʱֵ(���μ���ʱ�������ʱֵ)

	///<����İ汾��������ݵ�
	U8						m_iPATVersion;		///<PAT��汾��Ϣ
	U8						m_iPMTVersion;		///<PMT��汾��Ϣ
	U8						m_iSDTVersion;		///<SDT��汾��Ϣ
	U8						m_iBATVersion;		///<BAT��汾��Ϣ
	U8						m_iNitVersion;		///<NIT��汾��Ϣ
	U8						m_iCATVersion;		///<CAT��汾��Ϣ

	int						m_CASystemID;
	int						m_ShumaPDSD;
	//U16 					m_iPMTId;						///<��ǰ��Ŀ��PMTID
	//std::string 			m_strSName;						///<��ǰ��Ŀ����
	//DVBStream				m_stVideo;						///<��ǰ��Ŀ��Ƶ��
	//DVBStream 			m_stAudio[AUDIOSTREAM_MAXCOUNT];///<��ǰ��Ŀ��Ƶ��
	void *					m_Context;
	void *					m_MonEventCallback;	
	DVBService              m_stService;		///<��ǰ������Ϣ
	TSFilter*				m_pTSFilter[2];		
}; 

}

#endif // defined(JOYSEE_SERVICE_MON_H_)
