#include "servicemon.h"
#include "tvcomm.h"
#include "ucsconvert.h"
#include "datetime.h"
#include "../tvsearch/tvsearchlocal/pmtdescex.h"

#include <all.h>

#define  LOG_TAG  "libtvplayer"
#include "tvlog.h"

namespace PSISI = novelsuper::psisi_parse;

namespace TVCoreUtil{

extern void correct_text( std::string& text );


const U32 PAT_TIMEOUT = 0xffffffff;
const U32 PMT_TIMEOUT = 0xffffffff;
const U32 SDT_TIMEOUT = 0xffffffff;
const U32 BAT_TIMEOUT = 0xffffffff;
const U32 NIT_TIMEOUT = 0xffffffff;
const U32 CAT_TIMEOUT = 0xFFFFFFFF;

bool ServiceMonTask::BuildFilter()
{
	bool hr = true;

	
	utSectionFilter filter[5];
	memset(filter, 0, sizeof(filter));
	LOGTRACE(LOGINFO,"Enter ServiceMonTask::BuildFilter.%d \n",sizeof(filter));

	if(!m_bSwitchPMTMon)
	{
		filter[0].iTuner= 0;
		filter[0].pid 	= PAT_PID;
		filter[0].tid 	= NO_TABLE_ID;
		filter[0].timeout = PAT_TIMEOUT;
		filter[0].filterData[0] = TableId_Pat;
		filter[0].filterMask[0] = 0xff;
		
		filter[1].iTuner= 0;
		filter[1].pid 	= SDT_PID;
		filter[1].tid 	= NO_TABLE_ID;
		filter[1].timeout = SDT_TIMEOUT;
		filter[1].filterData[0] = TableId_SdtA;
		filter[1].filterMask[0] = 0xff;
		
		filter[2].iTuner= 0;
		filter[2].pid 	= NIT_PID;
		filter[2].tid 	= NO_TABLE_ID;
		filter[2].timeout = NIT_TIMEOUT;
		filter[2].filterData[0] = TableId_NitA;
		filter[2].filterMask[0] = 0xff;
		
		filter[3].iTuner= 0;
		filter[3].pid 	= BAT_PID;
		filter[3].tid 	= NO_TABLE_ID;
		filter[3].timeout = BAT_TIMEOUT;
		filter[3].filterData[0] = TableId_Bat;
		filter[3].filterMask[0] = 0xff;

		filter[4].iTuner= 0;
		filter[4].pid 	= CAT_PID;
		filter[4].tid 	= NO_TABLE_ID;
		filter[4].timeout = CAT_TIMEOUT;
		filter[4].filterData[0] = TableId_Cat;
		filter[4].filterMask[0] = 0xff;

		m_pTSFilter[0] = new TSFilter(this,filter,5);
		m_pTSFilter[0]->start_search();

		m_iMaxTimeout = SDT_TIMEOUT;
	}
	else{
		filter[0].iTuner= 0;
		filter[0].pid 	= m_stService.pmt_id;
		filter[0].tid 	= NO_TABLE_ID;
		filter[0].timeout = PMT_TIMEOUT;
		memset(filter[0].filterData,0,sizeof(filter[0].filterData)/sizeof(U8));
		memset(filter[0].filterMask,0,sizeof(filter[0].filterData)/sizeof(U8));	
		
		filter[0].filterData[0] = TableId_Pmt;
		filter[0].filterMask[0] = 0xff;
		
		m_pTSFilter[1] = new TSFilter(this,filter,1);
		m_pTSFilter[1]->start_search();
		
		m_iMaxTimeout = PMT_TIMEOUT;
	}
	
	LOGTRACE(LOGINFO,"Leave ServiceMonTask::BuildFilter.\n");
	return hr; 
}

void	ServiceMonTask::ClearFilter()
{
	LOGTRACE(LOGINFO,"Enter ServiceMonTask::ClearFilter.\n");
	if(0 != m_pTSFilter[0])
	{
		m_pTSFilter[0]->stop_search();
		delete m_pTSFilter[0];
		m_pTSFilter[0] = 0;
	}
	if(0 != m_pTSFilter[1])
	{
		m_pTSFilter[1]->stop_search();
		delete m_pTSFilter[1];
		m_pTSFilter[1] = 0;
	}
	
	LOGTRACE(LOGINFO,"Leave ServiceMonTask::ClearFilter.\n");
}

bool ServiceMonTask::task_start()
{
	LOGTRACE(LOGINFO,"Enter ServiceMonTask::task_start.\n");

	BuildFilter();
	m_iStarttime = NS_GetTickCount();

	LOGTRACE(LOGINFO,"Leave ServiceMonTask::task_start.\n");
	return true ;
}

bool ServiceMonTask::task_cancel()
{
	ClearFilter();
	return true ;
}

bool ServiceMonTask::on_sdt_got(const U8* pData,const U32 iLen)
{
	bool bRet = false;
	if((NULL == pData)  || (iLen < 3))
		return bRet;

	try{
		novelsuper::psisi_parse::SdtSection *psdt = (novelsuper::psisi_parse::SdtSection *)pData;

		U8 iSDTVersion = psdt->version();
		if(iSDTVersion == m_iSDTVersion)
			return bRet;

		for( novelsuper::psisi_parse::SdtSection::Loop_Iterator r = psdt->loop_begin(); !r.empty(); ++r )
		{
			U16 iSID = r->service_id();
		
			if(m_stService.serviceID == iSID)
			{
				novelsuper::psisi_parse::Service_I t = r->begin<novelsuper::psisi_parse::DescService>();
				if( !t.empty() )
				{
					std::string name = t->service_name_char();
					correct_text(name);

					LOGTRACE(LOGINFO,"[ServiceMonTask] name sid=%d,servicename=%s\n",iSID,m_stService.name);
					LOGTRACE(LOGINFO,"[ServiceMonTask] name sid=%d,servicename=%s\n",iSID,name.c_str());
					
					if(strcmp(m_stService.name,name.c_str()) == 0)
					{
						LOGTRACE(LOGINFO,"[ServiceMonTask] the same name!\n");
						break;
					}

					memset(m_stService.name,0,sizeof(m_stService.name));		
					memcpy(m_stService.name,name.c_str(),name.size());
					LOGTRACE(LOGINFO,"[ServiceMonTask] name change sid=%d,servicename=%s\n",iSID,m_stService.name);
					bRet = true;
					break;
				}
			}
		}

		m_iSDTVersion = iSDTVersion;
	}
	catch(novelsuper::psisi_parse::RuntimeError &e)
	{
		LOGTRACE(LOGINFO,"on_sdt_got: RuntimeError!!! %s\n",e.what());
		return false;
	}
	return bRet ;
}

int ServiceMonTask::on_pat_got(const U8* pData,const U32 iLen)
{
	int bRet = -1;
	if((NULL == pData)  || (iLen < 3))
		return bRet;
	
	novelsuper::psisi_parse::PatSection *ppat = (novelsuper::psisi_parse::PatSection *)pData;
	
	U8 iPatVersion = ppat->version();
	if(iPatVersion == m_iPATVersion) 
		return bRet;
	
	for( novelsuper::psisi_parse::PatSection::Loop_Iterator it = ppat->loop_begin(); !it.empty(); ++it)
	{
		if(m_stService.serviceID == it->program_number())
		{
			bRet = 0;
			m_iPATVersion = iPatVersion ;
			LOGTRACE(LOGINFO,"[ServiceMonTask] sid=%d,pmt_pid = %d,pmt_pid1 = %d\n",m_stService.serviceID,m_stService.pmt_id,it->pid());
			if(m_stService.pmt_id != it->pid())
			{
				m_stService.pmt_id = it->pid();
				bRet = 1;	
			}
			break;
		}
	}
	return bRet;
}

bool ServiceMonTask::on_pmt_got(const U8* pData,const U32 iLen)
{
	bool bRet = false;
	if((NULL == pData)  || (iLen < 12))
		return bRet;

	try{
		novelsuper::psisi_parse::PmtSection *ppmt = (novelsuper::psisi_parse::PmtSection *)pData;

		U8 iPMTVersion = ppmt->version();
		U16 iSID = ppmt->program_number();
		
		if(0 == m_stService.serviceID)
			m_stService.serviceID = iSID;
		else if(m_stService.serviceID != iSID)
		{
			return bRet ; 
		}

		if(m_iPMTVersion == iPMTVersion)
		{
			return bRet;
		}
		else
		{
			//m_iPMTVersion = iPMTVersion;
		}
		
		DVBService serviceTemp;
		memset(&serviceTemp,0,sizeof(DVBService));
		serviceTemp.audio_stream[0].stream_pid = INVALID_PID;
		serviceTemp.audio_stream[1].stream_pid = INVALID_PID;
		serviceTemp.audio_stream[2].stream_pid = INVALID_PID;	

		serviceTemp.pcr_pid = ppmt->pcr_pid();
		serviceTemp.audio_index = 0;

		U16 system_id = 0;
		U16 ecm_pid = 0;

		novelsuper::psisi_parse::DescList_Iterator<novelsuper::psisi_parse::DescCA> d_it;
		for(d_it=ppmt->begin<novelsuper::psisi_parse::DescCA>();d_it!=ppmt->end<novelsuper::psisi_parse::DescCA>(); d_it++)
		{
			if(!d_it.empty())
			{
				system_id = d_it->ca_system_id();
				if(system_id == m_CASystemID)
				{
					ecm_pid = d_it->ca_pid();
					LOGTRACE(LOGINFO,"[ServiceMonTask::on_pmt_got ecm1 %d\n",ecm_pid);
					break;
				}
			}
		}
		U8 iAudio = 0;
		for( novelsuper::psisi_parse::PmtSection::Loop_Iterator r = ppmt->loop_begin();
			!r.empty(); ++r )
		{
			U8	iStream_Type = r->stream_type() ; 
			U16 iESPid = r->elementary_pid();
			novelsuper::psisi_parse::DescList_Iterator<novelsuper::psisi_parse::DescCA> q8, q8b;
			for(q8=r->begin<novelsuper::psisi_parse::DescCA>();q8!= r->end<novelsuper::psisi_parse::DescCA>(); ++q8)
			{
				if(!q8.empty())
				{
					system_id = q8->ca_system_id();
					
					LOGTRACE(LOGINFO,"system_id(pmt): 0x%x\n",system_id);
					if(system_id == m_CASystemID)
					{
#ifdef JS_USE_GEHUACA_SEARCH
						unsigned const char *p;
						q8->private_data(p);
						if(p[0]!=01 || p[1]!=0x2d)
							continue;
#endif
						
						ecm_pid = q8->ca_pid();
						LOGTRACE(LOGINFO,"ecm_pid:0x%x\n",ecm_pid);
						break;
					}
				}
			}

			novelsuper::psisi_parse::AC3_I q0 = r->begin<novelsuper::psisi_parse::DescAC3>();
			if(!q0.empty())
			{
				if(iStream_Type == 0x06 )
				{
					iStream_Type = 0x6A;
				}
			}

			novelsuper::psisi_parse::EAC3_I q1 = r->begin<novelsuper::psisi_parse::DescEAC3>();
			if(!q1.empty())
			{
				if(0x06 == iStream_Type)
				{
					iStream_Type = 0x7A;
				}
			}
			
			novelsuper::psisi_parse::DTS_I q2 = r->begin<novelsuper::psisi_parse::DescDTS>();
			if(!q2.empty())
			{
				if(0x06 == iStream_Type)
				{
					iStream_Type = 0x7B;
				}
			}

			if(0x06 == iStream_Type){
				iStream_Type = 0x6A;
			}

			if(StreamID::IsVideo(iStream_Type))
			{
				serviceTemp.video_stream.stream_type= iStream_Type;
				serviceTemp.video_stream.stream_pid = iESPid;
				serviceTemp.video_stream.ecm_pid = ecm_pid;
			}
			else if(StreamID::IsAudio(iStream_Type))
			{
				if(AUDIOSTREAM_MAXCOUNT > iAudio)
				{
					serviceTemp.audio_stream[iAudio].stream_type= iStream_Type;
					serviceTemp.audio_stream[iAudio].stream_pid = iESPid;
					serviceTemp.audio_stream[iAudio].ecm_pid = ecm_pid;
					serviceTemp.audio_stream[iAudio].name[0]='\0';

					iAudio++;
				}
			}
			else if(StreamID::IsObjectCarousel(iStream_Type))
			{
				LOGTRACE(LOGINFO,"DSM-CC type:%d\n",iStream_Type);
			}
		}

		if(1 < iAudio)
		{
			for(int i =0 ; i < iAudio ;i++)
			{
				U8 iType = serviceTemp.audio_stream[i].stream_type;
				if((StreamID::PrivData != iType) && StreamID::IsAudio(iType))
				{
					serviceTemp.audio_index = i ;
					break;
				}
			}
		}

		LOGTRACE(LOGINFO,"[ServiceMonTask::on_pmt_got PMTVer=%u,{V:%u,A:%u}.\n",
			iPMTVersion,
			serviceTemp.video_stream.stream_pid,
			serviceTemp.audio_stream[0].stream_pid);
		
		if(serviceTemp.video_stream.stream_pid != m_stService.video_stream.stream_pid ||
				serviceTemp.video_stream.ecm_pid != m_stService.video_stream.ecm_pid ||
				serviceTemp.video_stream.stream_type != m_stService.video_stream.stream_type)
		{
			LOGTRACE(LOGINFO,"[ServiceMonTask::on_pmt_got video pid %d %d\n",
			m_stService.video_stream.stream_pid ,serviceTemp.video_stream.stream_pid);

			LOGTRACE(LOGINFO,"[ServiceMonTask::on_pmt_got video ecm pid %d %d\n",
			m_stService.video_stream.ecm_pid ,serviceTemp.video_stream.ecm_pid);

			LOGTRACE(LOGINFO,"[ServiceMonTask::on_pmt_got video type %d %d\n",
			m_stService.video_stream.stream_type ,serviceTemp.video_stream.stream_type);

			m_stService.video_stream.stream_pid = serviceTemp.video_stream.stream_pid;
			m_stService.video_stream.stream_type = serviceTemp.video_stream.stream_type;
			m_stService.video_stream.ecm_pid = serviceTemp.video_stream.ecm_pid;
			bRet = true;
		}
		//设置伴音后,index不为0,这里index一般为伴0,此时不应该产生变化.	
//		if(serviceTemp.audio_index != m_stService.audio_index)
//		{
//			m_stService.audio_index = serviceTemp.audio_index;
//			bRet = true;
//		}
		
		for(int i = 0; i < AUDIOSTREAM_MAXCOUNT; i++)
		{
			if(serviceTemp.audio_stream[i].stream_pid != m_stService.audio_stream[i].stream_pid ||
				serviceTemp.audio_stream[i].stream_type != m_stService.audio_stream[i].stream_type ||
				serviceTemp.audio_stream[i].ecm_pid != m_stService.audio_stream[i].ecm_pid)
			{
				LOGTRACE(LOGINFO,"[ServiceMonTask::on_pmt_got audio ecm pid %d %d\n",
				m_stService.audio_stream[i].ecm_pid, serviceTemp.audio_stream[i].ecm_pid);

				LOGTRACE(LOGINFO,"[ServiceMonTask::on_pmt_got audio pid %d %d\n",
				m_stService.audio_stream[i].stream_pid , serviceTemp.audio_stream[i].stream_pid);

				LOGTRACE(LOGINFO,"[ServiceMonTask::on_pmt_got audio type %d %d\n",
				m_stService.audio_stream[i].stream_type , serviceTemp.audio_stream[i].stream_type);
				m_stService.audio_stream[i].stream_pid = serviceTemp.audio_stream[i].stream_pid;
				m_stService.audio_stream[i].stream_type = serviceTemp.audio_stream[i].stream_type;
				m_stService.audio_stream[i].ecm_pid = serviceTemp.audio_stream[i].ecm_pid;
				bRet = true;
			}
		}

//		if(m_iPMTVersion != iPMTVersion && m_iPMTVersion != -1)	
//			bRet = true;

		m_iPMTVersion = iPMTVersion;
	}
	catch(novelsuper::psisi_parse::RuntimeError &e)
	{
		LOGTRACE(LOGINFO,"on_pmt_got: RuntimeError!!! %s\n",e.what());
		return false;
	}
	return bRet;
}

bool ServiceMonTask::on_bat_got(const U8 * pData, U32 iLen)
{
	bool bRet = false;
	if((NULL == pData)  || (iLen < 10))
		return bRet;

	novelsuper::psisi_parse::BatSection *pBat = (novelsuper::psisi_parse::BatSection *)pData;

	U8 iBatVersion = pBat->version();
	if(m_iBATVersion != iBatVersion)
	{
		if(m_iBATVersion == 0xFF)
		{
			m_iBATVersion = iBatVersion;
			UpdateVersions();		
			return false;
		}

		if(!pBat->CRC_OK())
			return bRet;

		m_iBATVersion = iBatVersion;
		bRet = true;
	}
	return bRet;
}

bool ServiceMonTask::on_nit_got(const U8 * pData, U32 iLen)
{
	bool bRet = false;
	if((NULL == pData)  || (iLen < 10))
		return bRet;

	novelsuper::psisi_parse::NitSection *pNit = (novelsuper::psisi_parse::NitSection *)pData;

	U8 iNitVersion = pNit->version();

//	LOGTRACE(LOGINFO,"[ServiceMonTask::on_nit_got Ver=%u %u \n",m_iNitVersion,iNitVersion);
//
//	int iPDSD = 0;
//	novelsuper::psisi_parse::NitHuangGang_I privateIt = 
//		pNit->begin<novelsuper::psisi_parse::DescNitHuangGang>();
//	if(!privateIt.empty())
//	{
//		iPDSD = privateIt->privateData();
//	}
//	LOGTRACE(LOGINFO,"[ServiceMonTask::on_nit_got iPDSD %d\n",iPDSD);

	if(m_iNitVersion != iNitVersion)
	{
		if(!pNit->CRC_OK())
			return bRet;

		LOGTRACE(LOGINFO,"[ServiceMonTask::on_nit_got Ver=%u %u \n",m_iNitVersion,iNitVersion);
		if(m_iNitVersion == 0xFF)
		{
			m_iNitVersion = iNitVersion;
			UpdateVersions();		
			return bRet;
		}
		
		m_iNitVersion = iNitVersion;
		bRet = true;
	}
	return bRet;
}

bool ServiceMonTask::on_cat_got(const U8 * pData, U32 iLen)
{
	bool bRet = false;
	if((NULL == pData)  || (iLen < 3))
		return bRet;

	try
	{
		novelsuper::psisi_parse::CatSection *pcat = (novelsuper::psisi_parse::CatSection *)pData;

		U8 iCatVersion = pcat->version();
//		LOGTRACE(LOGINFO,"on_cat_got ver %d newver %d\n",m_iCATVersion, iCatVersion);

		if(iCatVersion == m_iCATVersion)
			return bRet;

		for( novelsuper::psisi_parse::CA_I q = pcat->begin<novelsuper::psisi_parse::DescCA>(); ! q.empty(); ++q )
		{
			U32 emmPid = q->ca_pid();
			if(q->ca_system_id() == m_CASystemID && m_stService.emm_pid != emmPid)
			{
				LOGTRACE(LOGINFO,"on_cat_got emmpid change! ca = %d,oldemm = %d newemm = %d ver = %d",m_CASystemID, m_stService.emm_pid, emmPid, m_iCATVersion);
				m_stService.emm_pid = emmPid;
				if(m_iCATVersion != 0xFF)
					bRet = true;
			}
		}
		m_iCATVersion = iCatVersion;
	}
	catch(novelsuper::psisi_parse::RuntimeError &e)
	{
		LOGTRACE(LOGINFO, " RuntimeError!!! %s\n",e.what());
		return false;
	}
	return bRet;
}

bool ServiceMonTask::task_process(const U16 pid,U8 const* buffer, U32 size)
{
	bool bRet = false;
	
	//LOGTRACE(LOGINFO, "ServiceMonTask::task_process pid %d size %d\n",pid,size);
	if(PAT_PID == pid)
	{
		switch(on_pat_got(buffer,size))
		{
		case 1: 
			// 通知PMT PID改变,更新数据库
			OnEventOccur(m_stService,PMT_PID_CHANGE);
			// 向下执行.监测新的PMT PID
		case 0:
			if(0 != m_pTSFilter[1]){
				m_pTSFilter[1]->stop_search();
				delete m_pTSFilter[1];
				m_pTSFilter[1] = 0;
			}
			
			m_bSwitchPMTMon = true;
			BuildFilter();
			break;
		case -1:
		default:
			break;
		}
	}
	else if(SDT_PID == pid || BAT_PID == pid) //SDT,BAT
	{
		if(TableId_SdtA == buffer[0] )
		{
			// 传递回新的DVBService,通知 频道名 改变
			if(on_sdt_got(buffer,size))
				OnEventOccur(m_stService,SERVICE_NAME_CHANGE);
		}
		/*
		else if(TableId_Bat == buffer[0])
		{ 
			//通知BAT信息改变
			if(on_bat_got(buffer,size))
				OnEventOccur(m_stService,BAT_CHANGE);
		}
		*/
	}
	else if(pid == GetPMTId()) 
	{
		if(on_pmt_got(buffer, size))
		{
			// 传递回新的DVBService,通知新AV PID改变
			OnEventOccur(m_stService,AV_PID_CHANGE);
		}
	}
	else if(pid == NIT_PID)
	{
		if(TableId_NitA == buffer[0] && on_nit_got(buffer, size))
		{
			UpdateVersions();		
			OnEventOccur(m_stService,NIT_CHANGE);
		}
		
	}
	else if(pid == CAT_PID)
	{
		if((TableId_Cat == buffer[0]) && on_cat_got(buffer,size))
		{
			OnEventOccur(m_stService,AV_PID_CHANGE);
		}
	}
	return bRet;
}

bool ServiceMonTask::task_complete(bool bIsTimeout /* = false */)
{
	bool bRet = false;
	return bRet;
}

bool ServiceMonTask::OnEventOccur(DVBService & service, ServiceMonEventType type)
{
	LOGTRACE(LOGINFO,"Enter ServiceMonTask::OnEventOccur.\n");
	bool ret = false;
	if(m_MonEventCallback != NULL)
		ret = ((bool (*)(void*,DVBService&,int))m_MonEventCallback)(m_Context, service, type);
	LOGTRACE(LOGINFO,"Leave ServiceMonTask::OnEventOccur.\n");
	return ret;
}

}
