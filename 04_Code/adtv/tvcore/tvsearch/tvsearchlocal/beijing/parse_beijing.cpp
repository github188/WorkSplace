#include "typ.h"
#include "parse_beijing.h"
#include "bat_beijing.h"
#include <algorithm>

#define LOG_TAG "tvsearch"
#define LOG_LEVEL LOG_TRACE
#include "dxreport.h"

SECFilter NITAFILTER= { NIT_PID,{TableId_NitA,0,0,0,0,0,0,0},{0xff,0,0,0,0,0,0,0},10000 };
SECFilter SDTAFILTER= { SDT_PID,{TableId_SdtA,0,0,0,0,0,0,0},{0xff,0,0,0,0,0,0,0},7000};
SECFilter SDTOFILTER= { SDT_PID,{TableId_SdtO,0,0,0,0,0,0,0},{0xff,0,0,0,0,0,0,0},10000};
SECFilter BATFILTER = { BAT_PID,{TableId_Bat,0,0,0,0,0,0,0},{0xff,0,0,0,0,0,0,0},20000 };

SECFilter PATFILTER = { PAT_PID,{TableId_Pat,0,0,0,0,0,0,0},{0xff,0,0,0,0,0,0,0},1500 };
SECFilter CATFILTER = { CAT_PID,{TableId_Cat,0,0,0,0,0,0,0},{0xff,0,0,0,0,0,0,0},2500 };
//SECFilter PMTFILTERx= { 0xFF,   {TableId_Pmt,0,0,0,0,0,0,0},{0xff,0,0,0,0,0,0,0},2500}; 
SECFilter PMTFILTERx= { 0xFF,   {TableId_Pmt,0,0,0,0,0,0,0},{0xff,0,0,0,0,0,0,0},4500}; 

bool ParseBeijing::IsGoodBat(U8 const *pData,U32 iDataLen)
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
			return false ;//只需要SPECIAL_BOUQUET_PID 的数据即可？,
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

		// 根据业务群过滤
		// if(bouquet_id != areaparam_.bouquet_id) continue ;

		for( novelsuper::psisi_parse::BatSection::Loop_Iterator batIter = pbat->loop_begin();
				!batIter.empty(); ++batIter )
		{
			//		U16 onid = batIter->orig_network_id();
			//		U16 tsid = batIter->ts_id();

			// 服务列表(tag=0x41)
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
			/*parse private descriptor in bat of beijing */
			//节目顺序号: tag = 0x82
			novelsuper::psisi_parse::DescLoopProgramOrderList_I s = batIter->begin<novelsuper::psisi_parse::DescProgramOrder>();
			for( ; !s.empty(); ++s )
			{
				//parse program order 
				novelsuper::psisi_parse::DescProgramOrder::Loop_Iterator q = s->loop_begin();
				for(;! q.empty(); ++q )
				{
					BeijingProgramOrderDesc programOrder = {0,0};
					programOrder.service_id = q->service_id();
					//				programOrder.tv_mode = q->tv_mode();
					programOrder.order_number = q->order_number();
					m_mapProgOrder[programOrder.service_id] = programOrder;				
				}
			}
			// 音量补偿:tag = 0x83
			novelsuper::psisi_parse::DescLoopProgramVoiceList_I rProgramVoice = batIter->begin<novelsuper::psisi_parse::DescProgramVoice>();
			for( ; !rProgramVoice.empty(); ++rProgramVoice )
			{
				//parse program voice
				novelsuper::psisi_parse::DescProgramVoice::Loop_Iterator q = rProgramVoice->loop_begin();
				for( ;! q.empty(); ++q )
				{
					BeijingProgramVoiceDesc progVoiceComp = {0,0,0};
					progVoiceComp.service_id = q->service_id();
					progVoiceComp.left_right_channel = q->left_right_channel();
					progVoiceComp.volume_compensation = q->volume_compensation();
					m_mapProgVoiceComp[progVoiceComp.service_id] = progVoiceComp;

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


int ParseBeijing::GetDVBServices(OUT vector<DVBService>& argServices)
{
		argServices.clear();
		dxreport("One Freq Service size: %d\n",dvbOneFreqService_.size());
		mapDVBServiceT::iterator it = dvbOneFreqService_.begin();
		for (;it !=dvbOneFreqService_.end();it++)
		{
			// 填充bat_version, nit_version
			it->second.nitVersion = m_iNitVersion;
			it->second.batVersion = m_iBatVersion;
			// 更新节目的Sdt相关信息
			U16 iSID = it->second.serviceID;
			memcpy(it->second.name,sdta_info_[iSID].name.c_str(),sdta_info_[iSID].name.size());
			it->second.ts.net_id = sdta_info_[iSID].net_id;
			it->second.ts.ts_id = sdta_info_[iSID].ts_id;
			it->second.service_type = sdta_info_[iSID].service_type;

			// 歌华有线高清节目
			if((0xF9 == it->second.service_type) ||
					(0xE4 == it->second.service_type))
			{
				it->second.service_type = 0x01;// 数字电视业务
			}
			else if((0x00 == it->second.service_type)	|| 
					(
					 (0x01 != it->second.service_type)	&& 
					 (0x02 != it->second.service_type)   &&
					 (0x06 != it->second.service_type)
					)
				   )
			{
				// 空或其他私有服务
				dxreport("Warning, service_type!!!:%d\n",it->second.service_type);
//				continue ; 
			}

			// 频点信息
			it->second.ts.tuning_param.freq = dvbstatus_.tuning_param.freq;
			it->second.ts.tuning_param.symb = dvbstatus_.tuning_param.symb;
			it->second.ts.tuning_param.qam = dvbstatus_.tuning_param.qam;

			// 更新bat 相关信息
			it->second.channel_number = m_mapProgOrder[it->first].order_number;
			it->second.volume_comp = m_mapProgVoiceComp[it->first].volume_compensation;

			argServices.push_back(it->second);
			dvballservice_.push_back(it->second); // 保存数据由于排序
		}


	dxreport("<ParseBeijing> ParseBeijing::GetDVBServices freq %d  size(%d)\n",dvbstatus_.tuning_param.freq,argServices.size());
	return 1;
}
int ParseBeijing::GetDVBALLService(OUT vector<DVBService>& services)
{
	//
	// 频道号自己定义
	// 公版:按ServiceID排序;电视排在前面,广播排在后面
	std::vector<DVBService> televisions,radios,others;
	std::vector<DVBService>::iterator it;

	for (it = dvballservice_.begin();it != dvballservice_.end();it++)
	{
		if((it->service_type & 0xff)  == STF_DTS || (it->service_type & 0xff) == STF_MOSAIC )
		{
			televisions.push_back(*it);
		}
		else if((it->service_type & 0xff) == STF_DRSS )
		{
			radios.push_back(*it);
		}
		else
		{
			dxreport("other type:0x%x\n",it->service_type);
			it->service_type = STF_DTS;
			others.push_back(*it);
		}
	}

	//	U16 iIndex = 1;
	if(mode_==STVMODE_NIT)
	{
		std::sort(televisions.begin(),televisions.end(),DVBServiceSortByChNo);
		std::sort(radios.begin(),radios.end(),DVBServiceSortByChNo);

// 伪造扩展类型，用于测试 ++ (前5个电视节目）
		if(televisions.size() > 5)
		{
			for(int i=0; i<5; i++)
			{
				televisions[i].service_type |= (i+1)<<24;
			}
		}
// 伪造扩展类型，用于测试 --

		if(0 < radios.size()) {services = radios ;} 
		services.insert(services.end(),televisions.begin(),televisions.end());
	}
	else if(mode_ == STVMODE_MANUAL || mode_ == STVMODE_FULL)
	{
		std::sort(televisions.begin(),televisions.end(),DVBServiceSortByServceID);
		std::sort(radios.begin(),radios.end(),DVBServiceSortByServceID);
		std::sort(others.begin(),others.end(),DVBServiceSortByServceID);
		U16 iIndex = 1;
		for(it=televisions.begin(); it!=televisions.end(); it++)
		{
			it->channel_number=iIndex++;
		}
		for(it=others.begin(); it!=others.end(); it++)
		{
			it->channel_number=iIndex++;
		}
		iIndex = 1;
		for(it=radios.begin(); it!=radios.end(); it++)
		{
			it->channel_number=iIndex++;
		}

		
		if(0 < televisions.size()) {services = televisions ;} 
		services.insert(services.end(),radios.begin(),radios.end());
		if(0 < others.size())
		{
			services.insert(services.end(),others.begin(),others.end());
		}

	}
	ParseBase::GetDVBALLService(services); // 书写版本号等
	dxreport("<parsebase> ParseBase::GetDVBALLService(%d)\n", services.size());

	return 0;
}

