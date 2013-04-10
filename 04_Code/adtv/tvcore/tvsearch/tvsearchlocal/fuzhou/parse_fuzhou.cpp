#include "parse_fuzhou.h"
#include "bat_fuzhou.h"
#include <algorithm>

#define LOG_TAG "tvsearch"
#define LOG_LEVEL LOG_TRACE
#include "dxreport.h"

SECFilter NITAFILTER= { NIT_PID,{TableId_NitA,0,0,0,0,0,0,0},{0xff,0,0,0,0,0,0,0},10000 };
SECFilter SDTAFILTER= { SDT_PID,{TableId_SdtA,0,0,0,0,0,0,0},{0xff,0,0,0,0,0,0,0},7000};
SECFilter SDTOFILTER= { SDT_PID,{TableId_SdtO,0,0,0,0,0,0,0},{0xff,0,0,0,0,0,0,0},10000};
SECFilter BATFILTER = { BAT_PID,{TableId_Bat,0,0,0,0,0,0,0},{0xff,0,0,0,0,0,0,0},10000 };

SECFilter PATFILTER = { PAT_PID,{TableId_Pat,0,0,0,0,0,0,0},{0xff,0,0,0,0,0,0,0},3500 };
SECFilter CATFILTER = { CAT_PID,{TableId_Cat,0,0,0,0,0,0,0},{0xff,0,0,0,0,0,0,0},5000 };
SECFilter PMTFILTERx= { 0xFF,   {TableId_Pmt,0,0,0,0,0,0,0},{0xff,0,0,0,0,0,0,0},5000}; 

bool ParseFuZhou::IsGoodBat(U8 const *pData,U32 iDataLen)
{
	bool bRet = false;
	if(pData   == NULL || (iDataLen < 3))
		return bRet;
	try
	{
		novelsuper::psisi_parse::BatSection *pbat = (novelsuper::psisi_parse::BatSection *)pData;

		U16 bouquet_id = pbat->bouquet_id();
		if(bouquet_id != SPECIAL_BOUQUET_PID)  //sort channel and volume compasation
		{
			dxreport("Warning! bouquet_id is %d\n",bouquet_id);
			return false ;//ֻ��ҪSPECIAL_BOUQUET_PID �����ݼ��ɣ�,
		}

		U8 iSectionNumber = pbat->section_number();
		U8 iLastSectionNumber = pbat->last_section_number();
		m_iBatVersion = pbat->version();
		dxreport("bouquet_id:%d ,iSectionNumber:%d, iLastSectionNumber:%d\n", bouquet_id, iSectionNumber,iLastSectionNumber);


		if ((iLastSectionNumber + 1)== (U8)m_sBatTable.size())
		{
			dvbstatus_.bat_ok = true ; 
			dxreport("return is true\n");
			return true;
		}

		// ����ҵ��Ⱥ����
		// if(bouquet_id != areaparam_.bouquet_id) continue ;

		for( novelsuper::psisi_parse::BatSection::Loop_Iterator batIter = pbat->loop_begin();
				!batIter.empty(); ++batIter )
		{
			//		U16 onid = batIter->orig_network_id();
			//		U16 tsid = batIter->ts_id();

			// �����б�(tag=0x41)
			novelsuper::psisi_parse::ServiceList_I r = batIter->begin<novelsuper::psisi_parse::DescServiceList>();
			for( ; !r.empty(); ++r )
			{
				for( novelsuper::psisi_parse::DescServiceList::Loop_Iterator q = r->loop_begin();
						! q.empty(); ++q )
				{
					BatParam batparam = {0,0,0,0,0};
					batparam.sid = q->service_id();
					batparam.service_type = q->service_type();
					vecBatParam_.push_back(batparam);
				}
			}
			/*parse private descriptor in bat of fuzhou */
			// tag = 0x80
			//��Ŀ˳���
			// ��������
			novelsuper::psisi_parse::DescLoopProgOrder_Voice_List_I s = batIter->begin<novelsuper::psisi_parse::DescProgOrder_Voice>();
			for( ; !s.empty(); ++s )
			{
				//parse program order 
				novelsuper::psisi_parse::DescProgOrder_Voice::Loop_Iterator q = s->loop_begin();
				for(;! q.empty(); ++q )
				{
					FuZhuoProgOrder_VoiceC_Desc progOrder_VoiceC = {0,0,0,0};
					progOrder_VoiceC.service_id = q->service_id();
					progOrder_VoiceC.order_number = q->order_number();
					progOrder_VoiceC.left_right_channel= q->left_right_channel();
					progOrder_VoiceC.volume_compensation = q->volume_compensation();
					m_mapProgOrder_VoiceC[progOrder_VoiceC.service_id] = progOrder_VoiceC;				
				}
			}

		}

		m_sBatTable.insert(PSIConstraintT::value_type(iSectionNumber, iSectionNumber));
		if ((iLastSectionNumber +1) == (U8)m_sBatTable.size())
		{
			dvbstatus_.bat_ok = true;
			bRet = true;
		}
	}
	catch(novelsuper::psisi_parse::RuntimeError &e)
	{
		dxreport("IsGoodBat: RuntimeError!!! %s\n",e.what());
		return false;
	}

	dxreport("return is %d\n",bRet);
	return bRet;
}
int ParseFuZhou::GetDVBServices(OUT vector<DVBService>& argServices)
{
		argServices.clear();
		dxreport("One Freq Service size: %d\n",dvbOneFreqService_.size());
		mapDVBServiceT::iterator it = dvbOneFreqService_.begin();
		for (;it !=dvbOneFreqService_.end();it++)
		{
			// ���bat_version, nit_version
			it->second.nitVersion = m_iNitVersion;
			it->second.batVersion = m_iBatVersion;
			// ���½�Ŀ��Sdt�����Ϣ
			U16 iSID = it->second.serviceID;
			memcpy(it->second.name,sdta_info_[iSID].name.c_str(),sdta_info_[iSID].name.size());
			it->second.ts.net_id = sdta_info_[iSID].net_id;
			it->second.ts.ts_id = sdta_info_[iSID].ts_id;
			it->second.service_type = sdta_info_[iSID].service_type;

			// �軪���߸����Ŀ
			if((0xF9 == it->second.service_type) ||
					(0xE4 == it->second.service_type))
			{
				it->second.service_type = 0x01;// ���ֵ���ҵ��
			}
			else if((0x00 == it->second.service_type)	|| 
					(
					 (0x01 != it->second.service_type)	&& 
					 (0x02 != it->second.service_type)   &&
					 (0x06 != it->second.service_type)
					)
				   )
			{
				// �ջ�����˽�з���
				dxreport("Warning, service_type!!!:%d\n",it->second.service_type);
				continue ; 
			}

			//���ֵ������͵�û����Ƶ��Ϣ,�ĳɹ㲥����,���ݳ����������
			if(it->second.video_stream.stream_type == 0 &&
					it->second.service_type == 0x01)
			{
				dxreport("Warning, sid[%d] type is %d ,but there is no video pid[%d],type[%d] \n",it->second.serviceID,it->second.service_type,it->second.video_stream.stream_pid,it->second.video_stream.stream_type);

				it->second.service_type = 0x02; 
			}

			// Ƶ����Ϣ
			it->second.ts.tuning_param.freq = dvbstatus_.tuning_param.freq;
			it->second.ts.tuning_param.symb = dvbstatus_.tuning_param.symb;
			it->second.ts.tuning_param.qam = dvbstatus_.tuning_param.qam;

			// ����bat �����Ϣ
			it->second.channel_number = m_mapProgOrder_VoiceC[it->first].order_number;
			it->second.audio_channel_set = m_mapProgOrder_VoiceC[it->first].left_right_channel;
			it->second.volume_comp = m_mapProgOrder_VoiceC[it->first].volume_compensation;

			argServices.push_back(it->second);
			dvballservice_.push_back(it->second); // ����������������
		}

	dxreport("<parsebase> ParseBase::GetDVBServices freq %d  size(%d)\n",dvbstatus_.tuning_param.freq,argServices.size());
	return 1;
}


int ParseFuZhou::GetDVBALLService(OUT vector<DVBService>& services)
{
	//
	// Ƶ�����Լ�����
	// ����:��ServiceID����;��������ǰ��,�㲥���ں���
	std::vector<DVBService> std_teles,high_teles,radios;
	std::vector<DVBService>::iterator it;

	for (it = dvballservice_.begin();it != dvballservice_.end();it++)
	{
		/*	
		if(it->video_stream.stream_type  == StreamID::MPEG2Video)
		{
			std_teles.push_back(*it);
		}
		else if(it->video_stream.stream_type == StreamID::H264Video )
		{
			high_teles.push_back(*it);
		}
		else if(it->video_stream.stream_type == 0)
		{
				radios.push_back(*it);
		}
		*/	
		if(it->service_type == 0x01)
		{
			if(it->video_stream.stream_type != StreamID::H264Video)
				std_teles.push_back(*it);
			else
				high_teles.push_back(*it);
		}
		else if(it->service_type == 0x02)
		{
			radios.push_back(*it);
		}	
	}

	if(mode_ == STVMODE_NIT)
	{
		std::sort(std_teles.begin(),std_teles.end(),DVBServiceSortByChNo);
		std::sort(radios.begin(),radios.end(),DVBServiceSortByChNo);
		std::sort(high_teles.begin(),high_teles.end(),DVBServiceSortByChNo);

		// #define _CHANGE_CHANNELNO_TO_SEQUENCENO
		dxreport("<parsebase> ParseBase::GetDVBALLService() change channel number to sequence number!!!,dvballservice_.size:%d\n",dvballservice_.size());
		U16 iIndex = 1;
		for(it=std_teles.begin(); it!=std_teles.end();it++)
		{
			it->channel_number = iIndex++;
		}
		iIndex = 151;
		for(it=radios.begin(); it!=radios.end();it++)
		{
			it->channel_number = iIndex++;
		}
		iIndex = 201;
		for(it=high_teles.begin(); it!=high_teles.end();it++)
		{
			it->channel_number = iIndex++;
		}

		if(0 < std_teles.size()) {services = std_teles;}
		services.insert(services.end(),radios.begin(),radios.end());
		services.insert(services.end(),high_teles.begin(),high_teles.end());
	}
	else if(mode_ == STVMODE_MANUAL || mode_ == STVMODE_FULL)
	{
		std::sort(std_teles.begin(),std_teles.end(),DVBServiceSortByServceID);
		std::sort(radios.begin(),radios.end(),DVBServiceSortByServceID);
		std::sort(high_teles.begin(),high_teles.end(),DVBServiceSortByServceID);
		U16 iIndex = 1;
		for(it=std_teles.begin(); it!=std_teles.end(); it++)
		{
			it->channel_number=iIndex++;
		}
		for(it=high_teles.begin(); it!=high_teles.end(); it++)
		{
			it->channel_number=iIndex++;
		}
		iIndex = 1;
		for(it=radios.begin(); it!=radios.end(); it++)
		{
			it->channel_number=iIndex++;
		}


		if(0 < std_teles.size()) {services = std_teles ;} 
		services.insert(services.end(),radios.begin(),radios.end());
		if(0 < high_teles.size())
		{
			services.insert(services.end(),high_teles.begin(),high_teles.end());
		}

	}
	
	ParseBase::GetDVBALLService(services); // ��д�汾�ŵ�

	dxreport("<parsebase> ParseBase::GetDVBALLService(%d)\n", services.size());

	return 0;
}

