#ifndef JOYSEE_TVCONTROLLER_H_
#define JOYSEE_TVCONTROLLER_H_

#include "tvcomm.h"
#include "tsdemux.h"
#include "tsdemux_def.h"
#include "tvdevice.h"
#include "tvtask.h"
#include "tvtimemonitor.h"
#include "epgsearcher.h"
#include "servicemon.h"

using namespace TVCoreUtil;


class TsDataObserver
{

public:
	TsDataObserver();
	void gotTsData();
	void changeService();
	bool checkTsData();

private:
	bool m_Recorded;
	bool m_AlreadyChecked;			//��ǰ��Ŀ���ù�ȫ����
	int m_TsDataCount;				//�����Ts���ݷ�0
	unsigned int m_ChangeServiceTime;		//���һ�ε�̨��ʱ��
	MutexT	m_TsMutex;	
};

struct tvcontroller{
public:
		     tvcontroller();
	virtual ~tvcontroller();

	void     set_player_notify_callback(TVNOTIFY pCBNotify = 0);
	// ����
	int		 play();
	// ֹͣ
	int	    stop();
	//ֹͣ����
	int		stopDescramb();
	// ��̨
	int		 change_channel(const DVBService& service);
	// ��ȡ��������״̬
	int 	 get_player_status();
	// ��ȡTuner״̬
	bool	 get_tuner_status(TunerSignal& status);
	// ����ģ��״̬
	void	 setModuleState(int iState);
	bool     isTVModule();

	// ������ʾ����
	bool setDisplayRect(int x,int y,int width,int height);
	// ���û������(16:9��4:3)
	bool setDisplayZoomMode(IN const int iMode);
	// ��������Ƶ֡(����)
	bool cleanVideoFrame(bool bClean);
	// ��ʾ(0)/����(1)/�ú�(2)��Ƶ��
	bool setVideoLayer(int iState);

	// ���û��ȡ��ǰƵ����������ֵ
	bool adjustVolumeRatio(const S8 iRatio,const bool bIsSync);
	// ���û��ȡaudio�豸������
	int  getVolume();
	int  setVolume(float volume);
	// ���þ���
	bool setMute(bool bMute);
	// ��������ģʽ
	bool setAudioChannel(AudioStereoMode iMode);
	// ��ȡ��ǰ����ģʽ
	AudioStereoMode getAudioChannel();
	// ���õ�ǰƵ��������/����
	bool setAudioLang(U8 uIndex);
	U8	 getAudioLang();
	// ��ȡTDT/TOTʱ��
	bool getTDTTime(U32& time);
	bool getTOTTime(U32& time,U32& offset);
	
	// EPG��Ŀָ�����
	bool startEpgSearch(const TuningParam& tuning,const EITEventType evtType);
	// ����ȡ��EPG��������
	bool cancelEpgSearch();
	// ��ȡEPGȫ������
	bool getEpgData(EPGDataBaseT& epgs);
	// ��ȡָ�������µĽ�Ŀ(�¼�)��Ϣ
	bool getEpgDataBySID(const U16 iServiceId,EpgEventSet& events);
	// ��ȡָ��������ָ��ʱ��εĽ�Ŀ(�¼�)��Ϣ
	bool getEpgDataByDuration(const U16 iServiceId,EpgEventSet& events,const U32 iStartTime,const U32 iEndTime);
	// ������Ż���������
	bool resetTSBuffer();

	// ��������Ƶ����
static  void    OnTSData(utHandle hFilter,long iTuner,utPid pid,utTid tid,utByte *pData,long lDataSize,utContext context);
static  void    OnSectionData(utHandle hFilter,long iTuner,utPid pid,utTid tid,utByte *pData,long lDataSize,utContext context);
static  void    OnDestoryCallBack(utHandle hFilter,utContext context);

	// EPG�������֪ͨ 
static  int		OnSTVEPGComplete();

	static bool onServiceMonEvent(void * context, DVBService & newService, int eventType);
	bool onServiceMonEventPri(DVBService & newService, int type);

// CAS��Ϣ��״̬���
static  UINT __stdcall CASMonitorProc(LPVOID lpParam);
	bool getSTBId(char * pId, U8 buflen);

	bool set_control_pids(int *pPids,int iPidCount);

private:
	//	�򿪿�����
	bool	 open();
	//	�رտ�����
	bool	 close();

	int stop_play();

	// ��ȡCAS��Ϣ
	void GetCASEvents();
	void TunerHeartBeat();

	// ���ý���(����ECMPID������)
	int		descrambling(const DVBService& service);
	// ��ǰ��Ŀ�Ƿ����
	bool    IsScrambling();
	// ����EIT P/F ��̽������
	void    start_detect_pf();
	// ������ǰƵ��EPG����
	void	start_epg_download();
	// ����TDT/TOT���
	void	start_tdt_monitor();
	void	start_service_monitor(const DVBService & dvbservice, bool bstart);

	HANDLE  map_av_pid(const U16 vpid,const U16 apid);
	void    unmap_av_pid(HANDLE hTSFilter);
	
	// �Ƿ���ͬһ��Ŀ
	bool IsEqualDVBService(const DVBService& s1,const DVBService& s2);
	// �Ƿ���ͬһƵ��
	bool IsEqualTuningParam(const TuningParam& t1,const TuningParam& t2);

	bool set_service_pids(const DVBService & service, bool updataAV);
	DVBService & GetCurService(){return stService_;}

private:
	LONG getSignalStatus(){return iSignalStatus_;}	//chehl,�źż���߳��е���
	
	HANDLE				hDevice_;		///<TV�豸
	TVNOTIFY			pTVNotify_;		///<֪ͨ�ص�����
	HANDLE				hTSFilter_;		///<��ǰ��Ŀ������

	MutexT				serviceMutex_;	///<DVBService��
	DVBService          stService_;  	///<��ǰ���Ž�Ŀ
		
	TVTaskMgr			*pTaskMgr_;		///<���������
	
    TimeMonitorTask		*pTimeMonTask_; ///<TOT/TDT����
	PFEventSearchTask	*pEpgPfTask_;	///<MiniEPG����
	EpgSearchTask		*pEpgTask_;		///<EPG��������
	ServiceMonTask		*pServiceMonTask_;
	EpgController		*pEpgController_;	///<EPG��������

	simplethread		hRTMonitor_;	///<CAS��Ϣ״̬���

	// �źż��(�����ظ������жϻ�ָ�)
	LONG iSignalStatus_;				///<��ǰ�ź�״̬
	LONG iLastSignalStatus_;			///<��һ���ź�״̬


	MutexT	modMutex_;	
	int		iModuleState_;				///<ģ�����б��

#ifdef MEASUREMENT_BITRATE
	static LONG iStartTime_;	///< ��ʼʱ��
	static LONG iTotalBytes_;   ///< �����ֽ���
#endif 
public:
	bool	hasPrint;
	TsDataObserver		m_TsData;
};	

#endif //defined(JOYSEE_TVCONTROLLER_H_)
