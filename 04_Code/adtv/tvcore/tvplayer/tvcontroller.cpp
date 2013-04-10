#include "tvcontroller.h"
#include "capture.h"

#if !defined(WIN32)
#include "tvplay_adapter.h"
#endif 

#define  LOG_TAG  "libtvplayer"
#include "tvlog.h"

#ifdef MEASUREMENT_BITRATE
LONG tvcontroller::iStartTime_ = 0;
LONG tvcontroller::iTotalBytes_= 0;
#endif 
#include <time.h>

#ifdef TS_COUNTER_PRINT
struct PIDInf{
	U16 pid;
	S8  cc;
	U8  counter;
};
typedef std::map<U16,PIDInf> mapPIDInfT;
mapPIDInfT pidinf_;
#endif 

#ifdef USE_UDP
#include "udpclient.h"
UDPClient gUDPClient;
#endif 

#define BASE_PID_COUNT (0x06)
const U16 gDefHWPids[BASE_PID_COUNT]={0x00,0x01,0x10,0x12,0x14,0x11};
extern  tvcontroller *gpController_ ;

static int pIgnorePackage = 0;
#define IGNOREPACKAGENUM  12

tvcontroller::tvcontroller()
	:hDevice_(0),hTSFilter_(0)
{
	iSignalStatus_		= -1;
	iLastSignalStatus_	= -1;
	
	memset(&stService_,0,sizeof(DVBService));
	
	pTVNotify_ = 0;
	pTaskMgr_ = 0;
	pEpgController_ = 0;
	pTimeMonTask_ = 0;	
	pServiceMonTask_ = 0;
	pEpgPfTask_ = 0;
	pEpgTask_ = 0;
	iModuleState_   = 0;
	hasPrint = false;
	open();
	
	LOGTRACE(LOGINFO,"tvcontroller::tvcontroller.\n");
}

tvcontroller::~tvcontroller()
{
	close();
	LOGTRACE(LOGINFO,"tvcontroller::~tvcontroller.\n");
}

void tvcontroller::set_player_notify_callback(TVNOTIFY pCBNotify /* = 0 */)
{
	LOGTRACE(LOGINFO,"set_player_notify_callback(%p).\n",pCBNotify);
	pTVNotify_ = pCBNotify;

	if(pEpgController_)
		pEpgController_->NotifyChange(pCBNotify);
}

//	�򿪿�����
bool	 tvcontroller::open()
{
	bool  bRet = false;
	LOGTRACE(LOGINFO,"tvcontroller::open.\n");

	hDevice_ = tvdevice_open();
	if(0 != hDevice_)
	{
		tsdemux_enableDemux(true);
		bRet = true;
	}
	else{
		LOGTRACE(LOGFATAL,"tvcontroller::open,tvdevice_open failed.\n");
		return false;
	}
/*  //û����Ƶʱ,������ЩҲû������
	// ����Ĭ�ϵĹ���pid
	int pids[32]={0};
	for (int i = 0 ; i < BASE_PID_COUNT ; i++ )
	{
		pids[i] = gDefHWPids[i];
	}

	if(!tvdevice_setPidFilter(hDevice_,pids,BASE_PID_COUNT)){
		LOGTRACE(LOGINFO,"tvcontroller::open,tvdevice_setPidFilter failed.\n");	
	}
*/
	// CAS��Ϣʵʱ���
//	hRTMonitor_.start(CASMonitorProc,this);

	// �������������
	pTaskMgr_ = new TVTaskMgr();
	if(0 != pTaskMgr_)
	{
		pEpgController_ = new EpgController(hDevice_,pTVNotify_,pTaskMgr_);
		if(0 == pEpgController_)
		{
			LOGTRACE(LOGFATAL,"tvcontroller::open,new EpgController failed.\n");
			bRet = false;
		}
	}
	else{
		LOGTRACE(LOGFATAL,"tvcontroller::open,new TVTaskMgr failed.\n");
		bRet = false;
	}

	// ����TDT/TOT���(��פ����)
	start_tdt_monitor();

	LOGTRACE(LOGINFO,"tvcontroller::open,bRet = %d.\n",bRet);
	return bRet;
}

//	�رտ�����
bool	 tvcontroller::close()
{
	tsdemux_enableDemux(false);
	
	//hRTMonitor_.stop();
	LOGTRACE(LOGINFO,"tvcontroller::close,stopped hRTMonitor.\n");

	if(0 != pEpgController_)
	{
		delete pEpgController_;
		pEpgController_ = 0;
	}
	LOGTRACE(LOGINFO,"tvcontroller::close,release pEpgController_.\n");
	
	if(0 != pTaskMgr_)
	{
		delete pTaskMgr_;
		pTaskMgr_ = 0;
	}
	LOGTRACE(LOGINFO,"tvcontroller::close,release pTaskMgr_.\n");

	tvdevice_close(hDevice_);

	return true;
}

// ����
int	    tvcontroller::play()
{
	//��ʾ��Ƶ��,amcodec��ʹ�����Զ���ʾ��Ƶ��
	setVideoLayer(0);
	hRTMonitor_.start(CASMonitorProc,this);	
	//��֤����ֱ��ʱ,��֡������Ч.vod��Ѿ�֡�ر�,Ӧ��vod�˳�ʱ����
	cleanVideoFrame(false);
	
	return 0;
}

// ֹͣ����
int	    tvcontroller::stop()
{
	stop_play();
	
	//������Ƶ��	
	setVideoLayer(2);


	setModuleState(1);
	//����ȡ�����н���,emmpid���ֲ���	
//	Descrambling descrabm;
//	memset(&descrabm, 0, sizeof(Descrambling));
//	Ca_SetDescrambling(&descrabm,0,stService_.emm_pid);
	hRTMonitor_.stop();
	//�������зǳ�פ����.
	if(pServiceMonTask_)
		pTaskMgr_->del_task(pServiceMonTask_);
	if(pEpgPfTask_)
		pTaskMgr_->del_task(pEpgPfTask_);
	if(pEpgTask_)
		pTaskMgr_->del_task(pEpgTask_);

	pServiceMonTask_ = 0;
	pEpgPfTask_ = 0;
	pEpgTask_ = 0;
	//�˳�ʱ������Ӳ������,������ǰ������
	//��ĸ�������Ҫ���˳������ܽ���ecm
//	int pids[6]={0x00};
//	set_control_pids(pids,0);
	//make sure lock tuner after search
	memset(&stService_, 0, sizeof(DVBService));
	return 0;
}

int	tvcontroller::stopDescramb()
{
	Descrambling descrabm;
	memset(&descrabm, 0, sizeof(Descrambling));
	Ca_SetDescrambling(&descrabm,0,stService_.emm_pid);
	return 0;
}

int tvcontroller::stop_play()
{
#if !defined(WIN32)
	tvplay_adapter_stop();
#endif
	
	unmap_av_pid(hTSFilter_);
	pIgnorePackage = 0;

	return 0;
}

bool tvcontroller::getSTBId(char * pId, U8 buflen)
{
	bool bRet = false;
	if(pId == NULL || buflen < 13)
		return bRet;

	if(buflen > 16)
		buflen = 16;

	char pSTBID[16] = {0};
	memset(pId,0,buflen);
	bRet = tvdevice_getStbID(hDevice_,pSTBID,buflen);
	if(bRet)
	{
		sprintf(pId,"%02x%02x%02x%02x%02x%02x",pSTBID[0],pSTBID[1],pSTBID[2],pSTBID[3],pSTBID[4],pSTBID[5]);
		LOGTRACE(LOGINFO,"tvcontroller::getSTBId,%02x%02x%02x%02x%02x%02x\n",pSTBID[0],pSTBID[1],pSTBID[2],pSTBID[3],pSTBID[4],pSTBID[5]);
	}
	return bRet;
}

// ��̨
int		tvcontroller::change_channel(const DVBService& service)
{
	int iRet = 0;
	LOGTRACE(LOGINFO,"tvcontroller::change_channel,t0=%d.\n",NS_GetTickCount());

	if(hDevice_ == NULL)
	{
		LOGTRACE(LOGINFO,"tvcontroller::change_channel device not initialize, change failed!!!!!\n");
		return -1;
	}
start:
	stop_play();

	setModuleState(0);

	bool bTuneStatus = true;
	bool bForceTune  = false;	///ǿ�Ƶ�Ƶ
	bool bEqualTuning= false;   ///�Ƿ��л�Ƶ��
	int freq=0,symb=0,qam=0;

	TunerSignal signal_;
	//ֹͣ����������,�ڶ�������falseʱ,��һ����������
	start_service_monitor(service, false);
	{//��service��
		AutoLockT lock(serviceMutex_);
		
		freq = service.ts.tuning_param.freq;
		symb = service.ts.tuning_param.symb;
		qam  = service.ts.tuning_param.qam;

		tvdevice_getTunerSignalStatus(hDevice_,&signal_);
		if((signal_.locked == 0) || (signal_.quality <=0)){
			bForceTune = true;
		}

		// ͬƵ�㲻���ظ���Ƶ���źŶ�ʧ
		if(!IsEqualTuningParam(service.ts.tuning_param,stService_.ts.tuning_param) || bForceTune)
		{
			tvdevice_tune(hDevice_,freq,symb,qam);
			bEqualTuning= true;
		}
		if((stService_.video_stream.stream_pid == service.video_stream.stream_pid)&&(stService_.video_stream.ecm_pid== service.video_stream.ecm_pid)&&
			(stService_.audio_stream[stService_.audio_index].stream_pid == service.audio_stream[stService_.audio_index].stream_pid)&&
			(stService_.audio_stream[stService_.audio_index].ecm_pid== service.audio_stream[stService_.audio_index].ecm_pid))
		{
			pIgnorePackage = 0;
			LOGI("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA-same program-different freq\n");
		}
		else
			pIgnorePackage = IGNOREPACKAGENUM;
		// ʹ��memmoveδʹ��memcpy��Ŀ����Ϊ�˷�ֹ��ַ�ص�
		memmove(&stService_,&service,sizeof(DVBService));
	}
	
	if(bTuneStatus)
	{
		m_TsData.changeService();
		set_service_pids(service, true);
		// ����mini�������
		start_detect_pf();
		// ����Ƿ��л�Ƶ��
		if(bEqualTuning)
		{
			start_epg_download();
		}

		
		U8 vtype = service.video_stream.stream_type;
		U8 atype = service.audio_stream[service.audio_index].stream_type;
		U16 vpid = service.video_stream.stream_pid;
		U16 apid = service.audio_stream[service.audio_index].stream_pid;
		
		// ����Ĭ�ϵ�Ƶ������������
		// adjustVolumeRatio(service.volume_ratio,false);

		// �������Ƿ���ڻ���Ч
		// checkExpirationForService(service);
		
		// ���ò���������
		U32 iStartPlayTime = NS_GetTickCount();
		int iPlayStatus = tvplay_adapter_play(vpid,vtype,apid,atype);
		if(iPlayStatus == -1)
		{
			LOGTRACE(LOGINFO,"tvplay_adter_play---TVcontrol ---jianglei-1228start to play failed\n");
			LOGTRACE(LOGINFO,"tvplay_adapter_play:iPlayStatus=%d,t0=%d,t1=%d.\n",iPlayStatus,iStartPlayTime,NS_GetTickCount());
		//�����������.
			iRet = -1;
		}
		else
		{
			start_service_monitor(stService_, true);
		}
	}
	else
	{
		iRet = -1;
		LOGTRACE(LOGINFO,"tvdevice_tune(%d,%d,%d),failed.\n",freq,symb,qam);
	}

	// �㲥,������Ƶ��a
	if((2 == service.service_type)&&(iRet != -1))
	{
		setVideoLayer(2);
	}
	hasPrint = false;

	LOGTRACE(LOGINFO,"tvcontroller::change_channel,t1=%d.\n",NS_GetTickCount());
	return iRet;
}


// ��ȡ��������״̬
int		tvcontroller::get_player_status()
{
	return 0;
}

bool	tvcontroller::get_tuner_status(TunerSignal& status)
{
	bool bRet = false;
	if(hDevice_)
	{
		tvdevice_getTunerSignalStatus(hDevice_,&status);
		bRet = true;
	}

	return bRet;
}

// ����ģ��״̬
void	 tvcontroller::setModuleState(int iState)
{
	AutoLockT  lock(modMutex_);
	iModuleState_ = iState;
}

bool     tvcontroller::isTVModule()
{
	bool bRet = false;

	AutoLockT  lock(modMutex_);
	bRet = (0 == iModuleState_) ? true : false;
	
	return bRet;
}

int	tvcontroller::descrambling(const DVBService& service)
{
	int iECMPidCount = 0;
	U8 audio_index = service.audio_index;
	if(AUDIOSTREAM_MAXCOUNT <= audio_index)	 
	{
		audio_index = 0;
	}

	U16 emm_pid       = service.emm_pid;
	U16 video_ecm_pid = service.video_stream.ecm_pid;
	U16 audio_ecm_pid = service.audio_stream[audio_index].ecm_pid;
	
	Descrambling descrabm[2];
	if(0 != video_ecm_pid || 0 != audio_ecm_pid)
	{
		// (��Ҫ��������Ƶ����?)
		iECMPidCount = (video_ecm_pid != audio_ecm_pid) ? 2 : 1 ;

		U16 vpid = service.video_stream.stream_pid;
		U16 apid = service.audio_stream[audio_index].stream_pid;

		descrabm[0].ecmPid = video_ecm_pid;
		descrabm[0].wServiceID = service.serviceID;
		if(1 == iECMPidCount)
		{
			descrabm[0].streamPidCount = 2;
			descrabm[0].streamPid[0] = vpid;
			descrabm[0].streamPid[1] = apid;
		}
		else
		{
			descrabm[0].streamPidCount = 1;
			descrabm[0].streamPid[0] = vpid;

			descrabm[1].wServiceID = service.serviceID;
			descrabm[1].streamPidCount = 1;
			descrabm[1].streamPid[0] = apid;
			descrabm[1].ecmPid = audio_ecm_pid;
		}
	}

	//����,�軪��̨��Ҫsid
	if(iECMPidCount == 0)
	{
			descrabm[0].streamPidCount = 1;
			descrabm[0].streamPid[0] = 0;
			descrabm[0].ecmPid = 0;
			descrabm[0].wServiceID = service.serviceID;
			iECMPidCount = 1;
	}

	// ���ý���
	Ca_SetDescrambling (descrabm,iECMPidCount,emm_pid);

	return iECMPidCount;
}

void	tvcontroller::start_detect_pf()
{
	LOGTRACE(LOGINFO,"Enter start_detect_pf.\n");

	if(0 != pEpgPfTask_)
	{
		pEpgController_->cancelEpgSearch(pEpgPfTask_);
	}
	
	TuningParam tuning;
	tuning.freq = tuning.symb = tuning.qam = 0;
	pEpgPfTask_ = static_cast<PFEventSearchTask*>(pEpgController_->startEpgSearch(tuning,stService_.serviceID,EIT_EVENT_PF));
	
	LOGTRACE(LOGINFO,"Leave start_detect_pf.\n");
}

void	tvcontroller::start_epg_download()
{
	LOGTRACE(LOGINFO,"Enter start_search_epg.\n");

	if(0 != pEpgTask_)
	{
		pEpgController_->cancelEpgSearch(pEpgTask_);
		pEpgTask_ = 0;
	}

	TuningParam tuning;
	tuning.freq = tuning.symb = tuning.qam = 0;
	pEpgTask_ = static_cast<EpgSearchTask*>(pEpgController_->startEpgSearch(tuning,stService_.serviceID,EIT_EVENT_AS));

	LOGTRACE(LOGINFO,"Leave start_search_epg.\n");
}

void tvcontroller::start_tdt_monitor()
{
	LOGTRACE(LOGINFO,"Enter start_tdt_monitor.\n");
	if(0 != pTimeMonTask_)
	{
		pTaskMgr_->del_task(pTimeMonTask_);
		pTimeMonTask_ = 0;
	}

	pTimeMonTask_ = new TimeMonitorTask(pTaskMgr_);
	pTaskMgr_->add_task(pTimeMonTask_);
	
	LOGTRACE(LOGINFO,"Leave start_tdt_monitor.\n");
}

void tvcontroller::start_service_monitor(const DVBService & stservice, bool bstart)
{
	LOGTRACE(LOGINFO,"Enter start_service_monitor. %d\n",bstart);
	if(0 != pServiceMonTask_)
	{
		pTaskMgr_->del_task(pServiceMonTask_);
		pServiceMonTask_ = 0;
	}
	if(bstart)
	{
		pServiceMonTask_ = new ServiceMonTask(pTaskMgr_,stservice,(void*)this,(void*)onServiceMonEvent);
		pTaskMgr_->add_task(pServiceMonTask_);
	}
	
	LOGTRACE(LOGINFO,"Leave start_service_monitor.\n");
}

// 
HANDLE	tvcontroller::map_av_pid(const U16 vpid,const U16 apid)
{
	int iPIDCount = 0;
	utTsDataFilter filters[4]={{0,0x00},{0,0x00}};
	if(vpid < INVALID_PID)
		filters[iPIDCount++].pid = vpid;
	if(apid < INVALID_PID)
		filters[iPIDCount++].pid = apid;

#ifdef USE_UDP
	filters[2].pid = 0x0;
	filters[3].pid = stService_.pmt_id;
	iPIDCount = 4;
#endif

	return tsdemux_addTSFilter(filters,iPIDCount,OnTSData,0);
}
void	tvcontroller::unmap_av_pid(HANDLE hTSFilter)
{
	if(0 != hTSFilter){
		tsdemux_delTSFilter(hTSFilter_,0);
		hTSFilter_ = 0 ;
	}
}

// �Ƿ���ͬһ��Ŀ
bool tvcontroller::IsEqualDVBService(const DVBService& s1,const DVBService& s2)
{
	bool bRet = false;
	U8 audio_index1 = (AUDIOSTREAM_MAXCOUNT >= s1.audio_index) ? 0: s1.audio_index;
	U8 audio_index2 = (AUDIOSTREAM_MAXCOUNT >= s2.audio_index) ? 0: s2.audio_index;

	if(s1.video_stream.stream_pid != s2.video_stream.stream_pid &&
	   s1.video_stream.stream_type!= s1.video_stream.stream_type&&
	   s1.audio_stream[audio_index1].stream_pid != s2.audio_stream[audio_index2].stream_pid &&
	   s1.audio_stream[audio_index1].stream_type!= s2.audio_stream[audio_index2].stream_type)
	{
		bRet = true ;
	}

	return bRet ; 
}

// �Ƿ���ͬһƵ��
bool tvcontroller::IsEqualTuningParam(const TuningParam& t1,const TuningParam& t2)
{
	bool bRet = false;
	if(t1.freq == t2.freq && 
	   t1.symb == t2.symb &&
	   t1.qam == t2.qam) 
	{
		bRet = true ;
	}
	
	return bRet;
}

// ��������Ƶ����
void    tvcontroller::OnTSData(utHandle hFilter,long iTuner,utPid pid,utTid tid,utByte *pData,long lDataSize,utContext context)
{
	if((pid==gpController_->GetCurService().audio_stream[0].stream_pid || pid==gpController_->GetCurService().video_stream.stream_pid)&&(gpController_->hasPrint==false))
	{
		gpController_->hasPrint = true;
		LOGTRACE(LOGINFO,"tvcontroller::OnTSData [pid=%d]\n",pid);
		
	}

	gpController_->m_TsData.gotTsData();

#ifdef TS_COUNTER_PRINT
	PIDInf temp = {0,-1,0};
	pidinf_.insert(mapPIDInfT::value_type(pid,temp));
	mapPIDInfT::iterator  it = pidinf_.find(pid);
	if(it != pidinf_.end())
	{
		bool bContinue = false;
		
		U8 adaptation_field_control = ( pData[3] & 0x30 ) >> 4;
		// pre count plus 1.
		U8 uPreCountAdd1 = (it->second.cc+1)&0xf;
		// now count 
		it->second.counter = ( pData[3] & 0x0f );
		
		if(0 == adaptation_field_control || 2 == adaptation_field_control)
			bContinue = true;
		if (it->second.counter == it->second.cc)
			bContinue = true;
		else if (it->second.counter== uPreCountAdd1)
			bContinue = true;
		else if ((0x0 == it->second.counter) && (0xf == it->second.cc))
			bContinue = true;
		else 
			bContinue = false;
		if(!bContinue)
		{
			U8 uLostPacket =  (it->second.counter - uPreCountAdd1)&0xf ;
			LOGTRACE(LOGINFO,"[pid=%d],cc discontinuity,lost packet %d.\n",pid,uLostPacket);
		}
		
		it->second.cc = it->second.counter;
	}
#endif 

#ifdef USE_UDP
	//gUDPClient.push(pData,lDataSize);

#endif 

#if !defined(WIN32)
	if(pIgnorePackage < IGNOREPACKAGENUM)
	{
		pIgnorePackage++;
		return ;
	}
	tvplay_adapter_putTSData(pData,lDataSize);
#endif 

#ifdef MEASUREMENT_BITRATE
	double fBitRate = 0.0;
	iTotalBytes_ += lDataSize ;
	if(0 == iStartTime_) {
		iStartTime_ = NS_GetTickCount();
	}
	else{
		LONG iCurTime = NS_GetTickCount();
		LONG iDiffTime= iCurTime-iStartTime_;
		iStartTime_   = iCurTime;

		fBitRate =(8*iTotalBytes_ >> 10)/iDiffTime;
		iStartTime_   = iCurTime;
		LOGTRACE(LOGINFO,"=== bitrate=%.2f Mbps ===\n",fBitRate);
	}
	
#endif 
}

void    tvcontroller::OnSectionData(utHandle hFilter,long iTuner,utPid pid,utTid tid,utByte *pData,long lDataSize,utContext context)
{
	LOGTRACE(LOGINFO,"calling OnSectionData(%d,%d,%d,0x%08X,%d).\n",iTuner,pid,tid,pData,lDataSize);
}

void    tvcontroller::OnDestoryCallBack(utHandle hFilter,utContext context)
{
	//tvcontroller* pController = static_cast<tvcontroller*>(context);
}

void	tvcontroller::GetCASEvents()
{
	//LOGTRACE(LOGINFO,"tvcontroller::GetEvents().\n");
}

#define MSG_TUNER_SIGNAL_INTERRUPT		0x01
#define MSG_TUNER_SIGNAL_RESTORATION	0x00

void	tvcontroller::TunerHeartBeat()
{
	//LOGTRACE(LOGINFO,"Enter TunerHeartBeat.\n");
	
	TunerSignal signal_;
	{
		AutoLockT lock(serviceMutex_);
		tvdevice_getTunerSignalStatus(hDevice_,&signal_);
		if(!signal_.locked) 
		{
			iSignalStatus_ = 0 ;				// �ź��ж�

			tvdevice_tune(hDevice_,stService_.ts.tuning_param.freq,stService_.ts.tuning_param.symb,stService_.ts.tuning_param.qam);

			DVBService service = stService_;
			// Ӳ������(pat,nit,pmt,vpid,apid,ecmpid(V/A),emmpid)
			int iPidCount= 0;
			int pids[6]={0x00};
			if(service.video_stream.stream_pid < INVALID_PID)
				pids[iPidCount++] = service.video_stream.stream_pid;
			if(service.audio_stream[service.audio_index].stream_pid < INVALID_PID)
				pids[iPidCount++] = service.audio_stream[service.audio_index].stream_pid;
			if(service.video_stream.ecm_pid < INVALID_PID)
				pids[iPidCount++] = service.video_stream.ecm_pid;
			if(service.emm_pid < INVALID_PID)
				pids[iPidCount++] = service.emm_pid;
			if(service.pmt_id < INVALID_PID)
				pids[iPidCount++] = service.pmt_id;

			if( service.audio_stream[service.audio_index].ecm_pid != service.video_stream.ecm_pid && service.audio_stream[service.audio_index].ecm_pid < INVALID_PID)
				pids[iPidCount++] = service.audio_stream[service.audio_index].ecm_pid;

			set_control_pids(pids,iPidCount);

			tvdevice_getTunerSignalStatus(hDevice_,&signal_);
			if(signal_.locked)
			{
				iSignalStatus_ = 1 ;			// �źŻָ�
			}
			else
				usleep(500);
		}
		else
		{
			iSignalStatus_ = 1;					// ���ź�
		}
	}

	// ��г�ź�֪ͨ
	if(0 != pTVNotify_)
	{
		if((-1 != iSignalStatus_) && (iLastSignalStatus_ != iSignalStatus_))
		{
			U8 uType = (0 == iSignalStatus_)  ? MSG_TUNER_SIGNAL_INTERRUPT : MSG_TUNER_SIGNAL_RESTORATION ;
			iLastSignalStatus_	= iSignalStatus_ ; 
			pTVNotify_(TVNOTIFY_TUNER_SIGNAL,uType,0);
			LOGTRACE(LOGINFO,"TVNOTIFY_TUNER_SIGNAL,type=%d\n",uType);
		}
	}

	//LOGTRACE(LOGINFO,"Leave TunerHeartBeat.\n");
}

UINT	tvcontroller::CASMonitorProc(LPVOID lpParam)
{
	simplethread*	pThread	  = reinterpret_cast<simplethread*>(lpParam);
	tvcontroller* pController = reinterpret_cast<tvcontroller*>(pThread->get_arglist());

	U32 iActiveCount = 0;
	while (!pThread->check_stop())
	{
		if(pController->isTVModule())
		{
			// �����źż��,
			if(iActiveCount % 4 == 0)
				pController->TunerHeartBeat();		
			
			//�������ݼ��,������źŲż��,chehl
			if(pController->getSignalStatus() == 1)
				pController->m_TsData.checkTsData();

			if(0 == ++iActiveCount%40){
				LOGTRACE(LOGINFO,"tvcontroller::CASMonitorProc is active.\n");
			}
			NS_sleep(500);	
		}
		else{
			NS_sleep(5000);
		}
	}
	 
	return 0;
}

// ������ʾ����
bool tvcontroller::setDisplayRect(int x,int y,int width,int height)
{
	bool bRet = false;

	LOGTRACE(LOGINFO,"Enter tvcontroller::setDisplayRect(%d,%d,%d,%d).\n",x,y,width,height);

#if !defined(WIN32)
	bRet = (0 == tvplay_setVideoWindow(x,y,width,height)) ? true : false; 
#else
	bRet = true;
#endif 

	LOGTRACE(LOGINFO,"Leave tvcontroller::setDisplayRect,bRet=%d.\n",bRet);

	return bRet;
}
// ���û������
bool tvcontroller::setDisplayZoomMode(const int iMode)
{
	bool bRet = false;
	
	LOGTRACE(LOGINFO,"Enter tvcontroller::setDisplayZoomMode(%d).\n",iMode);

#if !defined(WIN32)
	bRet = (0 == tvplay_setScreenMode(iMode)) ? true : false;
#else
	bRet=  true;
#endif 

	LOGTRACE(LOGINFO,"Enter tvcontroller::setDisplayZoomMode,bRet=%d.\n",bRet);

	return bRet ;
}

bool tvcontroller::setVideoLayer(int iState)
{
	bool bRet = false;

	LOGTRACE(LOGINFO,"Enter tvcontroller::visibleVideoLayer(%d).\n",iState);

#if !defined(WIN32)
	if(0 == tvplay_ClearVideoLayer(iState)){
		bRet = true;
	}
#else
	bRet = true;
#endif

	LOGTRACE(LOGINFO,"Leave tvcontroller::visibleVideoLayer.\n");

	return bRet;

}

// ��������Ƶ֡
bool tvcontroller::cleanVideoFrame(bool bClean)
{
	bool bRet = false;

	LOGTRACE(LOGINFO,"Enter tvcontroller::cleanVideoFrame(%d).\n",bClean);

#if !defined(WIN32)
	bRet = (0 == tvplay_set_black_policy(static_cast<int>(bClean))) ? true : false;
#else
	bRet = true;
#endif

	LOGTRACE(LOGINFO,"Leave tvcontroller::cleanVideoFrame.\n");

	return bRet;
}
// ���û��ȡ��ǰƵ����������ֵ
bool tvcontroller::adjustVolumeRatio(const S8 iRatio,const bool bIsSync)
{
	bool bRet = true;
	return bRet;
}

// ���û��ȡaudio�豸������
int  tvcontroller::getVolume()
{
	int iRet = -1;

	LOGTRACE(LOGINFO,"Enter tvcontroller::getVolume.\n");

#if !defined(WIN32)
	iRet = tvplay_getVolume();
#else
	iRet = 0;
#endif 

	LOGTRACE(LOGINFO,"Leave tvcontroller::getVolume.\n");

	return iRet ;
}

int  tvcontroller::setVolume(float volume)
{
	int iRet = -1;
	
	LOGTRACE(LOGINFO,"Enter tvcontroller::setVolume(%f).\n",volume);

#if !defined(WIN32)
	iRet = tvplay_setVolume(volume);
#else
	iRet = 0;
#endif 
	
	LOGTRACE(LOGINFO,"Leave tvcontroller::setVolume,iRet=%d.\n",iRet);

	return iRet;
}

bool tvcontroller::setMute(bool bMute)
{
	bool bRet = true;

#if !defined(WIN32)
	bRet = (0 == tvplay_SetMute(static_cast<int>(bMute))) ? true :false;
#else
	bRet = true;
#endif 

	return bRet;
}

// ��������ģʽ
bool tvcontroller::setAudioChannel(AudioStereoMode iMode)
{
	bool bRet = false;
	
	LOGTRACE(LOGINFO,"Enter tvcontroller::setAudioChannel(%d).\n",iMode);
	
#if !defined(WIN32)
	bRet = (0 == tvplay_SetChannel(static_cast<int>(iMode))) ? true : false;
#else
	bRet = true;
#endif 

	LOGTRACE(LOGINFO,"Leave tvcontroller::setAudioChannel,iRet=%d.\n",bRet);

	return bRet ;
}

// ��ȡ��ǰ����ģʽ
AudioStereoMode tvcontroller::getAudioChannel()
{
	AudioStereoMode iAudioMode = AUDIO_MODE_STEREO;

	LOGTRACE(LOGINFO,"tvcontroller::getAudioChannel no implement.\n");

	return iAudioMode;
}

bool tvcontroller::setAudioLang(U8 uIndex)
{
	bool bRet = false;
	LOGTRACE(LOGINFO,"tvcontroller::setAudioLang(%d).\n",uIndex);
	
	{
		AutoLockT lock(serviceMutex_);
		//��ͬ���������ظ�����
		if(stService_.audio_index == uIndex)
			return true;

		stService_.audio_index = uIndex;
	}

	int iRet = change_channel(stService_);
	bRet = (0 == iRet) ? true : false;

	LOGTRACE(LOGINFO,"tvcontroller::setAudioLang.\n");

	return bRet;
}

U8	 tvcontroller::getAudioLang()
{
	U8 uAudioLangCount = 0;

	// AutoLockT lock(serviceMutex_);
	for(U8 i = 0 ; i < AUDIOSTREAM_MAXCOUNT ; i++)
	{
		LOGTRACE(LOGINFO,"getAudioLang {pid:%d,type:%d}\n",stService_.audio_stream[i].stream_pid,
			stService_.audio_stream[i].stream_type);
		if( (0 < stService_.audio_stream[i].stream_pid) && 
			(0 < stService_.audio_stream[i].stream_type))
		{
			uAudioLangCount++;
		}
	}

	return uAudioLangCount;
}

bool tvcontroller::getTDTTime(U32& time)
{
	bool bRet = false;
	if(0 != pTimeMonTask_){
		bRet = pTimeMonTask_->GetCurrentTSTime(time);
	}

	return bRet;
}

bool tvcontroller::getTOTTime(U32& time,U32& offset)
{
	bool bRet = false;
	if(0 != pTimeMonTask_){
		bRet = pTimeMonTask_->GetCurrentTSTime(time,offset);
	}

	return bRet;
}


bool tvcontroller::startEpgSearch(const TuningParam& tuning,const EITEventType evtType)
{
	LOGTRACE(LOGINFO,"tvcontroller::startEpgSearch no implement.\n");
	return true;
}

// ����ȡ��EPG��������
bool tvcontroller::cancelEpgSearch()
{
	LOGTRACE(LOGINFO,"tvcontroller::startEpgSearch no implement.\n");
	return true;
}

// ��ȡEPGȫ������
bool tvcontroller::getEpgData(EPGDataBaseT& epgs)
{
	bool bRet = false;
	if(pEpgController_){
		bRet = pEpgController_->getEpgData(epgs);
	}
	else{
		LOGTRACE(LOGERR,"tvcontroller::getEpgData,pEpgController_ is nil.\n");
	}

	return bRet;
}

// ��ȡָ�������µĽ�Ŀ(�¼�)��Ϣ
bool tvcontroller::getEpgDataBySID(const U16 iServiceId,EpgEventSet& events)
{
	bool bRet = false;
	if(pEpgController_){
		bRet = pEpgController_->getEpgDataBySID(iServiceId,events);
	}
	else{
		LOGTRACE(LOGERR,"tvcontroller::getEpgDataBySID,pEpgController_ is nil.\n");
	}

	return bRet;
}

// ��ȡָ��������ָ��ʱ��εĽ�Ŀ(�¼�)��Ϣ
bool tvcontroller::getEpgDataByDuration(const U16 iServiceId,EpgEventSet& events,const U32 iStartTime,const U32 iEndTime)
{
	bool bRet = false;
	if(pEpgController_){
		bRet = pEpgController_->getEpgDataByDuration(iServiceId,events,iStartTime,iEndTime);
	}
	else{
		LOGTRACE(LOGERR,"tvcontroller::getEpgDataByDuration,pEpgController_ is nil.\n");
	}

	LOGTRACE(LOGINFO,"Leave tvcontroller::getEpgDataByDuration(%u,%u,%u,%u),bRet=%d.\n",
			 iServiceId,events.size(),iStartTime,iEndTime,bRet);
	return bRet ;
}

bool tvcontroller::onServiceMonEvent(void * context, DVBService & newService, int type)
{
	tvcontroller * pThis = (tvcontroller*)context;
	return pThis->onServiceMonEventPri(newService, type);
}

bool tvcontroller::onServiceMonEventPri(DVBService & newService, int type)
{
	switch(type)
	{
	case PMT_PID_CHANGE:
	{
		{
			AutoLockT lock(serviceMutex_);
			//���ڵ�̨���Ѿ���̨,������
			if(newService.serviceID != stService_.serviceID)
				break;
			stService_.pmt_id = newService.pmt_id;
		}
		set_service_pids(newService, false);
		//����pmt pid�ı�,���п���,����ƵpidҲ�ı���
		//��ʱû������Ƶ��������,�յ�pmt���ʱ������
		//һ������pmt��,�����µ�̨.�����µĹ�����
		//�������Ƶ�ı���,�յ�pmt������,��Ϊ�ղ��Ѿ����ù�ȫ����,��֪ͨ����һ��(���������,֪ͨû��Ӱ��)
		m_TsData.changeService();
		//���ֻpmt�仯������,�ָ�Ϊ���ϲ�ģ���̨.	
		//���av�仯����ȥ֪ͨ�ϲ��̨.
/*		if(pTVNotify_)
		{
			pTVNotify_(TVNOTIFY_UPDATE_SERVICE,0,&newService);
		}
*/
		break;
	}
	case AV_PID_CHANGE:
	{
//		stop_play();
//		NS_sleep(400); //�������,amcodec��ʹ����ʧ��
		
		{
			AutoLockT lock(serviceMutex_);
			//���ڵ�̨���Ѿ���̨,������
			if(newService.serviceID != stService_.serviceID){
				break;
			}
			
			stService_.pmt_id = newService.pmt_id;
			stService_.video_stream.stream_pid = newService.video_stream.stream_pid;
			stService_.video_stream.stream_type = newService.video_stream.stream_type;
			stService_.video_stream.ecm_pid = newService.video_stream.ecm_pid;
			stService_.audio_stream[0].stream_pid = newService.audio_stream[0].stream_pid;
			stService_.audio_stream[0].stream_type= newService.audio_stream[0].stream_type;
			stService_.audio_stream[0].ecm_pid = newService.audio_stream[0].ecm_pid;
			stService_.audio_stream[1].stream_pid = newService.audio_stream[1].stream_pid;
			stService_.audio_stream[1].stream_type= newService.audio_stream[1].stream_type;
			stService_.audio_stream[1].ecm_pid = newService.audio_stream[1].ecm_pid;
			stService_.audio_stream[2].stream_pid = newService.audio_stream[2].stream_pid;
			stService_.audio_stream[2].stream_type= newService.audio_stream[2].stream_type;
			stService_.audio_stream[2].ecm_pid = newService.audio_stream[2].ecm_pid;
			stService_.audio_index = newService.audio_index;
		}
		//��̨����,�����ϲ�	
/*		set_service_pids(stService_,true);
		
		// start player
		U8 vtype = stService_.video_stream.stream_type;
		U8 atype = stService_.audio_stream[stService_.audio_index].stream_type;
		U16 vpid = stService_.video_stream.stream_pid;
		U16 apid = stService_.audio_stream[stService_.audio_index].stream_pid;

		tvplay_adapter_play(vpid,vtype,apid,atype);
*/		
		if(pTVNotify_){
			pTVNotify_(TVNOTIFY_UPDATE_SERVICE,0,&newService);
		}
		break;
	}
	case SERVICE_NAME_CHANGE:
		{
			AutoLockT lock(serviceMutex_);
			//���ڵ�̨���Ѿ���̨,������
			if(newService.serviceID != stService_.serviceID){
				break;
			}

			memset(stService_.name,0,sizeof(stService_.name));
			memcpy(stService_.name,newService.name,strlen(newService.name));
		}
		
		if(pTVNotify_){
			pTVNotify_(TVNOTIFY_UPDATE_SERVICE,0,&newService);
		}
		break;
	case NIT_CHANGE:
	case BAT_CHANGE:
		// ������̨ 
		if(pTVNotify_){
			pTVNotify_(TVNOTIFY_UPDATE_PROGRAM,0,0);
		}
		break;
	default:
		break;
	}

	LOGTRACE(LOGERR,"tvcontroller::onServiceMonEvent,type = %d\n",type);
	return true;
}

bool tvcontroller::set_service_pids(const DVBService & service, bool updataAV)
{
	U8 vtype = service.video_stream.stream_type;
	U8 atype = service.audio_stream[service.audio_index].stream_type;
	U16 vpid = service.video_stream.stream_pid;
	U16 apid = service.audio_stream[service.audio_index].stream_pid;
	
	// ���ý���
	int iECMPidCount = descrambling(service);
	
	// Ӳ������(pat,nit,pmt,vpid,apid,ecmpid(V/A),emmpid)
	int iPidCount= 0;
	int pids[6]={0x00};
	if(service.video_stream.stream_pid < INVALID_PID)
		pids[iPidCount++] = service.video_stream.stream_pid;
	if(service.audio_stream[service.audio_index].stream_pid < INVALID_PID)
		pids[iPidCount++] = service.audio_stream[service.audio_index].stream_pid;
	if(service.video_stream.ecm_pid < INVALID_PID)
		pids[iPidCount++] = service.video_stream.ecm_pid;
	if(service.emm_pid < INVALID_PID)
		pids[iPidCount++] = service.emm_pid;
	if(service.pmt_id < INVALID_PID)
		pids[iPidCount++] = service.pmt_id;

	if(2 == iECMPidCount && service.audio_stream[service.audio_index].ecm_pid < INVALID_PID)
		pids[iPidCount++] = service.audio_stream[service.audio_index].ecm_pid;
	
	// ����Ӳ����������
	LOGTRACE(LOGINFO,"=== vpid=%d,apid=%d,vecm_pid=%d,aecm_pid=%d,emm_pid=%d===.\n",
		vpid,apid,service.video_stream.ecm_pid,
		service.audio_stream[service.audio_index].ecm_pid,service.emm_pid);
	
	set_control_pids(pids,iPidCount);
	if(updataAV)
	{
		// ͬ������
		unmap_av_pid(hTSFilter_);
		hTSFilter_ = map_av_pid(vpid,apid);
	}

	return true;
}

bool tvcontroller::set_control_pids(int *pPids,int iPidCount)
{
	if(iPidCount == 0)
	{
		tvdevice_setPidFilter(hDevice_,NULL,0);
		return true;
	}

	int iCount=0;
	int pids_[32] ={0};
	for ( int i = 0 ; i < BASE_PID_COUNT ; i++)
	{
		pids_[iCount] = gDefHWPids[i];
		iCount++;
	}

	for( int i = 0 ; i < iPidCount ; i++)
	{
		pids_[iCount] = pPids[i];
		iCount++;
	}

	iCount = (iCount > 32 ) ? 32 : iCount; 
	bool bRet = tvdevice_setPidFilter(hDevice_,pids_,iCount);
	
	LOGTRACE(LOGINFO,"tvcontroller::set_control_pids(%d),bRet=%d\n",iCount,bRet);
	
	return bRet;
}

// ������Ż���������
bool tvcontroller::resetTSBuffer()
{
	bool bRet = false;
#if !defined(WIN32)
	if(IsScrambling()){
		bRet = (0== tvplay_adapter_clsBuffer()) ? true : false;
	}
#else
	LOGTRACE(LOGINFO,"no implement.\n");
#endif 
	return bRet;
}

// ��ǰ��Ŀ�Ƿ����
bool tvcontroller::IsScrambling()
{

	bool bRet = false;
	{
		AutoLockT lock(serviceMutex_);
		if(0 != stService_.video_stream.ecm_pid ||
		   0 != stService_.audio_stream[stService_.audio_index].ecm_pid)
		{
			bRet = true;
		}
	}
	
	return bRet;
}

#include <sys/time.h>
TsDataObserver::TsDataObserver()
{
	m_Recorded = true;
	m_ChangeServiceTime =0;
	m_TsDataCount = 1;
	m_AlreadyChecked = true;
}

void TsDataObserver::changeService()
{
	LOGTRACE(LOGINFO,"Enter TsDataObserver::changeService()\n");
	m_Recorded = false;
	{
		AutoLockT lock(m_TsMutex);
		m_TsDataCount = 0;
		m_ChangeServiceTime = NS_GetTickCount();
		m_AlreadyChecked = false;
	}
	LOGTRACE(LOGINFO,"Leave TsDataObserver::changeService()\n");
}

//����յ�����ʱ�պõ�̨,m_Recorded�����Դ���û��Ӱ��
//���û������,����������ᱻ����
//���������,��������ᱻ��������,��һ��û�м�¼��Count,��Ӱ���´�
void TsDataObserver::gotTsData()
{
	if(m_Recorded == false)
	{
		LOGTRACE(LOGINFO,"Enter TsDataObserver::gotTsData()\n");
		m_Recorded = true;

		AutoLockT lock(m_TsMutex);
		m_TsDataCount = 1;
		LOGTRACE(LOGINFO,"Leave TsDataObserver::gotTsData()\n");
	}
}

//���ts����,�����̨���յ�ts����,��Ϊtrue
bool TsDataObserver::checkTsData()
{
	LOGTRACE(LOGINFO,"Enter TsDataObserver::checkTsData()\n");
	AutoLockT lock(m_TsMutex);

	//������ֱ�ӷ���
	if(m_TsDataCount != 0 || m_AlreadyChecked)
	{
		LOGTRACE(LOGINFO,"Leave TsDataObserver::checkTsData() ts %d, checked %d\n",m_TsDataCount, m_AlreadyChecked);
		return true;
	}

	unsigned int curTime = NS_GetTickCount();

	//��̨��ȥ����1000������Ϊ������
	//ʵ�ʷ���������500���п�������
	//NS_Get�������ص���1000��������
	if((curTime - m_ChangeServiceTime < 1001))
	{
		LOGTRACE(LOGINFO,"Leave TsDataObserver::checkTsData()cur %d change %d\n", curTime, m_ChangeServiceTime);
		return true; 
	}

	//�ߵ�����϶�û������,����ȫ����
	//Ϊ��֤״̬,����ȫ������������,��ֹ��ȡ���˵�̨״̬,�����ڵ�̨�߳����ù�����
	if(gpController_ != NULL)
	{
		m_AlreadyChecked = true;
		gpController_->set_control_pids(NULL, 0);
	}
	LOGTRACE(LOGINFO,"Leave TsDataObserver::checkTsData() %d\n", m_TsDataCount);
	return m_TsDataCount;
}




