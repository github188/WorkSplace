#include "parse_qingdao.h"

#include <algorithm>
#include <iostream>
#include <sstream> 

#include "pmtdescex.h"
#include "sdtdescex.h"
#include "nitdescex.h"

#define LOG_TAG "tvsearch_qingdao"
#define LOG_LEVEL LOG_TRACE
#include "dxreport.h"

#define SCRAMBLING    (0x1000)	//加扰
#define NOTSCRAMBLING (0x0000) //不加扰
#define  QINGDAO_MENHU_SHIPIN_CHANNAL_NO 10000  //门户视频的频道号
#define SETTING_INIPATH_ASTR_QINGDAO "./MctvData/TVCore/qingdaoTemp.ini"

enum {eHighQuality = 0, eNormalQulity =1,eAudioBroadCast =2, eDataBroadCast = 12, eDataBroadCast132 =132, eOtherCategory = 200 /*256 */};

//


SECFilter BATFILTER = { BAT_PID,{TableId_Bat,0,0,0,0,0,0,0},{0xff,0,0,0,0,0,0,0},10000 };
SECFilter NITAFILTER= { NIT_PID,{TableId_NitA,0,0,0,0,0,0,0},{0xff,0,0,0,0,0,0,0},4000 };

SECFilter PATFILTER = { PAT_PID,{TableId_Pat,0,0,0,0,0,0,0},{0xff,0,0,0,0,0,0,0},1500 };
SECFilter CATFILTER = { CAT_PID,{TableId_Cat,0,0,0,0,0,0,0},{0xff,0,0,0,0,0,0,0},500 };
SECFilter SDTAFILTER= { SDT_PID,{TableId_SdtA,0,0,0,0,0,0,0},{0xff,0,0,0,0,0,0,0},7000};
SECFilter SDTOFILTER= { SDT_PID,{TableId_SdtO,0,0,0,0,0,0,0},{0xff,0,0,0,0,0,0,0},10000};

SECFilter PMTFILTERx= { 0xFF,   {TableId_Pmt,0,0,0,0,0,0,0},{0xff,0,0,0,0,0,0,0},1500}; 

bool ParseQingDao::InitTVSearch(int searchMode)
{
	ParseBase::InitTVSearch(searchMode);
	GetValidBouquetID();
	return true;
}

int ParseQingDao::GetDVBServices(OUT vector<DVBService>& argServices)
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

		// 频点信息
		it->second.ts.tuning_param.freq = dvbstatus_.tuning_param.freq;
		it->second.ts.tuning_param.symb = dvbstatus_.tuning_param.symb;
		it->second.ts.tuning_param.qam = dvbstatus_.tuning_param.qam;

		// 更新bat 相关信息
		//		it->second.channel_number = m_mapProgOrder[it->first].order_number;
		//		it->second.volume_comp = m_mapProgVoiceComp[it->first].volume_compensation;

		argServices.push_back(it->second);
		dvballservice_.push_back(it->second); // 保存数据由于排序
	}

	AddServicesCategory(argServices);
	return 1;
}
void  ParseQingDao::AddServicesCategory(INOUT vector<DVBService>& argServices)
{
	//添加分类
	for (vector<DVBService>::iterator it=argServices.begin(); it!=argServices.end();it++)
	{
		U16 iSID = it->serviceID;
		dxreport("sid :%d, service_type:%d\n", iSID, it->service_type);
		if(0x01 == it->service_type) 
		{
			mapSidChannalVolumeTrackset::iterator itrMap = mapSid_CVT_.find(iSID);

			if (itrMap != mapSid_CVT_.end())
			{
				it->category = mapSid_CVT_[iSID].program_group ? eHighQuality : eNormalQulity ; 	
			}

			it->service_type |= it->ca_mode ? SCRAMBLING : NOTSCRAMBLING;
			dxreport("iSID %d SCRAMBLING %d  ",  iSID  ,   it->ca_mode	);
		}
		else if(2 == it->service_type) 
		{
			it->category = eAudioBroadCast ;
			it->service_type |= it->ca_mode ? SCRAMBLING : NOTSCRAMBLING;
			dxreport("iSID %d SCRAMBLING %d  ",  iSID  ,   it->ca_mode	);
		}
		else if(12 == it->service_type) 
		{
			it->category = eDataBroadCast ;
		}
		else if(132 == it->service_type)
		{
			it->category = eDataBroadCast132 ;		
			dxreport("数据广播 service_type %d",it->service_type  );
		}
		else
		{
			it->category = eOtherCategory ; 	
		}
	}
}
int ParseQingDao::GetDVBALLService(OUT std::vector<DVBService>& argServices)
{
	dxreport("GetDVBALLService begin mapSid_CVT_:%d, dvballservice_:%d\n",mapSid_CVT_.size(),dvballservice_.size());

	argServices = dvballservice_;
	SetChannelInfo(mapSid_CVT_,argServices);//设置音量补偿与声道设置等等。

	//获取门户视频的频道
	//	U16 channelNO = -1;
	vector<DVBService> vecMainSID;
	//	U32 sidMenHuShiPin = GetPrivateProfileIntA("SERVICE","id",0, SETTING_INIPATH_ASTR_QINGDAO);//从配置文件中获取门户视频的sid号
	U32 mainSID = 100;
	for (vector<DVBService>::iterator itVec = argServices.begin();itVec != argServices.end();itVec++)
	{
		if (mainSID == itVec->serviceID)
		{ 
			itVec->service_type =0x01;
			vecMainSID.push_back(*itVec);
		}
	}

	// 过滤
	DeleteReduntantBySdtA(mapSid_CVT_,argServices);//用sdta过滤
	dxreport("GetDVBALLService 01 mapSid_CVT_.size (%d) dvballservice_.size(%d) \n",mapSid_CVT_.size(),argServices.size());

	// bat 中有bouquet ID, 在搜索时过滤即可
	// map<U32, BatParam> mapBatParamValid;
	// GetBatParam(vecBatParam_, mapBatParamValid);
	// DeleteReduntantByBat(mapBatParamValid,argServices);//用bat表中数据过虑

	// dxreport("GetDVBALLService 02 mapBatParamValid.size (%d) argServices.size(%d) \n",mapBatParamValid.size(),argServices.size());

	// 排序
	sort(argServices.begin(),argServices.end(),DVBServiceSortByChNo); 
	std::vector<DVBService>::iterator it;
	std::vector<DVBService> televisionsGaoQing, televisionsBiaoQing , radios,digitalBroadcast,others;

	for (it = argServices.begin();it != argServices.end();it++)
	{
		if(eHighQuality == it->category )
		{
			televisionsGaoQing.push_back(*it);
		}
		else if(eNormalQulity == it->category )
		{
			televisionsBiaoQing.push_back(*it);
		}
		else if(eDataBroadCast == it->category )
		{
			digitalBroadcast.push_back(*it);
		}
		else if(eAudioBroadCast == it->category )
		{
			radios.push_back(*it);
		}
		else 
		{
			others.push_back(*it);
		}
	}

	U16 iIndex = 1;

	for (it = televisionsBiaoQing.begin();it != televisionsBiaoQing.end();it++){
		it->channel_number = iIndex++;
	}

	for (it = televisionsGaoQing.begin();it !=televisionsGaoQing.end();it++){
		it->channel_number = iIndex++;
	}

	for (it = digitalBroadcast.begin();it != digitalBroadcast.end();it++){
		it->channel_number = iIndex++;
	}

	for (it = radios.begin();it != radios.end();it++){
		it->channel_number = iIndex++;
	}

	for (it = others.begin();it != others.end();it++){
		it->channel_number = iIndex++;
	}

	for (it = vecMainSID.begin();it != vecMainSID.end();it++){
		it->channel_number = iIndex++;
		if (mainSID == it->serviceID)
		{
			it->channel_number = QINGDAO_MENHU_SHIPIN_CHANNAL_NO;
		}
	}	

	vector<DVBService> fatalServices;
	if(0 < radios.size()) {fatalServices = radios ;} 
	fatalServices.insert(fatalServices.end(),televisionsGaoQing.begin(),televisionsGaoQing.end());
	fatalServices.insert(fatalServices.end(),televisionsBiaoQing.begin(),televisionsBiaoQing.end());
	fatalServices.insert(fatalServices.end(),digitalBroadcast.begin(),digitalBroadcast.end());
	fatalServices.insert(fatalServices.end(),others.begin(),others.end());
	fatalServices.insert(fatalServices.end(),vecMainSID.begin(),vecMainSID.end());

	argServices.clear();
	argServices = fatalServices ;

	dxreport("GetDVBALLService OutPut === GetDVBALLService(%d) \r\n televisionsGaoQing(%d) \r\n televisionsBiaoQing(%d) \r\n digitalBroadcast(%d) \r\n radios(%d) vecMainSID(%d) === End\n",
			argServices.size(),televisionsGaoQing.size(),
			televisionsBiaoQing.size(),digitalBroadcast.size(),radios.size(),vecMainSID.size());

	// for Debug 
	for (vector<DVBService>::iterator itrVecNew =  argServices.begin(); itrVecNew != argServices.end(); itrVecNew++) {
		dxreport("GetDVBALLService for debug [serviceID(%d),category(%d),channel_number(%d),volume_comp(%d),service_type (%d)]\n",
				itrVecNew->serviceID, itrVecNew->category, itrVecNew->channel_number, itrVecNew->volume_comp, itrVecNew->service_type);
	}

	return 0;
}


bool ParseQingDao::IsGoodNitA(U8 const *pData,U32 iDataLen)
{
	bool bRet = false;
	if((NULL == pData)  || (iDataLen < 3))
		return bRet;

	novelsuper::psisi_parse::NitSection *pnit = (novelsuper::psisi_parse::NitSection *)pData;
	novelsuper::psisi_parse::BouquetList_I b1 = pnit->begin<novelsuper::psisi_parse::DescBouquetList>();
	if (!b1.empty())
	{
		novelsuper::psisi_parse::DescBouquetList::Loop_Iterator rr = b1->loop_begin();
		for (;!rr.empty();++rr)
		{
			bouquetCountry desc;
			desc.bouquet = rr->bouquetid();
			desc.country_code = rr->countrycode();
			sBouquetCountryList.push_back(desc);
		}
	}

	return true;
}

bool ParseQingDao::IsGoodSdtA(U8 const *pData,U32 iDataLen)
{
	dxreport("IsGoodSdtA iDataLen:%d\n",iDataLen);
	bool bRet = false;
	if((NULL == pData)  || (iDataLen < 3))
		return bRet;
	ParseBase::IsGoodSdtA(pData, iDataLen);
	 
	novelsuper::psisi_parse::SdtSection *psdt = (novelsuper::psisi_parse::SdtSection *)pData;
	for( novelsuper::psisi_parse::SdtSection::Loop_Iterator r = psdt->loop_begin(); !r.empty(); ++r )
	{
		U16 iSID = r->service_id();
		SdtInfoT info_;

		//获取声道、排序号、音量补偿、特殊授权标记
		novelsuper::psisi_parse::ChannelNumberDescService_I z = r->begin<novelsuper::psisi_parse::ChannelNumberDescService>();
		if( !z.empty() )
		{
			stChNum    stChInfoSet; 
			stChInfoSet.sid = iSID;
			stChInfoSet.channel_number = z->channel_number();
			stChInfoSet.subscribed_only_flag = z->subsdribed_only_flag();//是否需要特殊授权的标识
			stChInfoSet.volumn_set = z->volumn_set();//音量补偿
			stChInfoSet.track_set = z->track_set();//声道设置
			stChInfoSet.program_group = z->program_group();//?ljt:这个从何而来？周俊华说这个是分类标记。

			dxreport("iSID %d  channel_numbe %d volumn_set  %d  track_set %d isGaoQing %d",iSID,stChInfoSet.channel_number, stChInfoSet.volumn_set, stChInfoSet.track_set, stChInfoSet.program_group);
			mapSid_CVT_.insert(map <U16,stChNum>::value_type(stChInfoSet.sid,stChInfoSet));
		}

	}
	return true;
}

bool ParseQingDao::IsGoodBat(U8 const *pData,U32 iDataLen)
{
	bool bRet = false;
	if(NULL == pData  || (iDataLen < 3))
		return bRet;
	try
	{
		novelsuper::psisi_parse::BatSection *pbat = (novelsuper::psisi_parse::BatSection *)pData;
		U8 iSectionNumber = pbat->section_number();
		U8 iLastSectionNumber = pbat->last_section_number();
		m_iBatVersion = pbat->version();
		dxreport("m_iBatVersion:%d ,iSectionNumber:%d, iLastSectionNumber:%d\n", m_iBatVersion, iSectionNumber,iLastSectionNumber);


		if ((iLastSectionNumber + 1)== (U8)m_sBatTable.size())
		{
			dvbstatus_.bat_ok = true ; 
			dxreport("return is true\n");
			return true;
		}
		U16 bouquet_id = pbat->bouquet_id();
		if(bouquet_id!=m_validBouquetID) return false;
		
		for( novelsuper::psisi_parse::BatSection::Loop_Iterator batIter = pbat->loop_begin();
				!batIter.empty(); ++batIter )
		{
			// 服务列表(tag=0x41)
			novelsuper::psisi_parse::ServiceList_I r = batIter->begin<novelsuper::psisi_parse::DescServiceList>();
			for( ; !r.empty(); ++r )
			{
				for( novelsuper::psisi_parse::DescServiceList::Loop_Iterator q = r->loop_begin();
						! q.empty(); ++q )
				{
					BatParam batparam = {0,0,0,0,0};
					batparam.bouquet_id = bouquet_id;
					batparam.sid = q->service_id();
					batparam.service_type = q->service_type();
					vecBatParam_.push_back(batparam);
					dxreport("IsGoodBat bouquet_id(%d) sid(%d)  service_type(%d) ",batparam.bouquet_id ,batparam.sid ,batparam.service_type );
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

U32 ParseQingDao::ConvertCountryCode(U32 input) const
{
	stringstream tempss;
	string s;
	int t;
	char a[20];
	string temp1;
	tempss << input;
	string temps = tempss.str();
	tempss.str("");

	for (unsigned int i= 0;i<temps.length()/2;i++)
	{
		s = temps.substr(i*2,2);
		t = atoi(s.c_str());
		//		itoa(t,a,16);
		sprintf(a,"%x",t);
		temp1 = a;
		t = atoi(temp1.c_str());
		tempss << t;
	}

	int ret;
	temp1 = tempss.str();
	sscanf(temp1.c_str(),"%x",&ret); 
	return (U32)ret;
}

/*
   根据广电私有数据，设置频道的声道，排序号，音量补偿等
   */
void ParseQingDao::SetChannelInfo(mapSidChannalVolumeTrackset& mapChIndex,INOUT vector<DVBService>& services) const
{
	vector<DVBService>::iterator it1 ;
	for (it1 = services.begin();it1 != services.end();it1++)
	{
		U32 iSid = it1->serviceID;
		mapSidChannalVolumeTrackset::iterator it2 = mapChIndex.find(iSid);
		if (it2 != mapChIndex.end())
		{
			//音量补偿映射
			S8 tempVolumeRatio =it2->second.volumn_set;
			if ((1<=tempVolumeRatio)&&(tempVolumeRatio <= 32))
			{
				tempVolumeRatio -=16;
			}
			else
			{
				tempVolumeRatio =0;
			}
			it1->volume_comp= tempVolumeRatio;

			it1->channel_number =it2->second.channel_number;//排序号

			U8 tempAudioTrack =it2->second.track_set;//声道

			switch (tempAudioTrack)
			{
				case 1:
					tempAudioTrack = 2;
					break;
				case 2:
					tempAudioTrack = 1;
					break;
				case 3:
					tempAudioTrack = 0;
					break;
				default:
					tempAudioTrack = 0;
					break;
			}

			it1->audio_channel_set = tempAudioTrack;//将青岛广电的声道转换为joysee 的声道.

		}

	}
}

void ParseQingDao::DeleteReduntantBySdtA(mapSidChannalVolumeTrackset& mapChIndex,INOUT vector<DVBService>& services) const
{
	dxreport("DeleteReduntantBySdtA: begin mapChIndex.size %d ",mapChIndex.size());

	vector<DVBService> resVecService;

	mapSidChannalVolumeTrackset::iterator itMap = mapChIndex.begin();

	for (; itMap != mapChIndex.end(); itMap ++)
	{
		for (vector<DVBService>::iterator itVec = services.begin(); itVec != services.end(); itVec++)
		{
			if (itMap->second.sid == itVec->serviceID)
			{
				dxreport("DeleteReduntantBySdtA:  find sid (%d) as same ,so insert it.", itVec->serviceID);
				resVecService.push_back(*itVec);
				break;
			}
		}
	}

	services.clear();

	services = resVecService;

	dxreport("DeleteReduntantBySdtA: end mapChIndex.size(%d) services.size(%d)",mapChIndex.size(),services.size());
}

void ParseQingDao::DeleteReduntantByBat(map<U32, BatParam>& mapBatParamValid,INOUT vector<DVBService>& services) const
{
	dxreport("DeleteReduntantByBat:: begin mapBatParamValid.size %d ",mapBatParamValid.size());

	vector<DVBService> resService;

	map<U32, BatParam>::iterator it1 = mapBatParamValid.begin();

	for (; it1 != mapBatParamValid.end(); it1 ++)
	{
		dxreport("DeleteReduntantByBat bouquet_id(%d) sid(%d)  service_type(%d) ",it1->second.bouquet_id ,it1->second.sid ,it1->second.service_type );
		for (vector<DVBService>::iterator it2 = services.begin(); it2 != services.end(); it2++)
		{
			if (it1->second.sid == it2->serviceID)
			{
				dxreport("DeleteReduntantByBat::  find sid (%d) as same ,so insert it.", it2->serviceID);
				resService.push_back(*it2);
				break;
			}
		}
	}

	services.clear();

	services = resService;

	dxreport("DeleteReduntantByBat:: end mapBatParamValid.size(%d) services.size(%d)",mapBatParamValid.size(),services.size());
}

// 已失去意义，目前仅为设置有效bouquetid
// void ParseQingDao::GetBatParam(IN vector<BatParam>& vecBatParam, map<U32, BatParam>& mapBatParamValid)

const int GET_OPERATOR_ID = 0x100; // 运营商ID
// 设置运营商特征值
const int SET_OPERATOR_ACS= 0x101; // 运营商特征码

int ParseQingDao::SetParameter(int key, const void* request,int reqLength)
{
	if(key != SET_OPERATOR_ACS)	return -1;

	U32 contry = -1;
	/*
	if(reqLength >= 8)
	{
		unsigned long acs = *((int *)request+1);
		U32 contry = ConvertCountryCode(acs);
	}
	*/
	return 0;
}
int ParseQingDao::GetParameter(int key, void* reply,	int* replyLength)
{
	UNUSED_PARAM(reply);
	UNUSED_PARAM(replyLength);
	if(key != GET_OPERATOR_ID) return -1;
	return 0;
}

void ParseQingDao::GetValidBouquetID()
{
	
	std::vector<unsigned long> acs;
	std::vector<unsigned short> ids;
	//	GetOperatorID(ids);
	U32 contry = -1;
	if (ids.size() != 0)
	{
		//GetOperatorACS(ids[0],acs);
	}
	if (acs.size() != 0)
	{
		contry = ConvertCountryCode(acs[1]);
	}
	U32 bouquetid = 24678;//默认值
	for (std::vector<bouquetCountry>::iterator it = sBouquetCountryList.begin();it != sBouquetCountryList.end(); it++)
	{
		if(it->country_code == contry)
		{
			bouquetid = it->bouquet;
			break;
		}
	}
	/*	
	for (vector<BatParam>::iterator it = vecBatParam.begin();it != vecBatParam.end();it++)
	{
		dxreport("bouquet_id:%d\n",it->bouquet_id);
		if (it->bouquet_id == bouquetid)//比较bouquet_id
		{
			mapBatParamValid[it->sid] = *it;
		}
	}
	*/
	m_validBouquetID = bouquetid;
	dxreport("GetBatParam end. m_validBouquetID:%d\n",m_validBouquetID);

}
