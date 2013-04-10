#include "typ.h"
#include "parsebase.h"
#include "pmtdescex.h"
#include "ucsconvert.h"

#define LOG_TAG "tvsearch"
#define LOG_LEVEL LOG_TRACE
#include "dxreport.h"


#define NOVEL_SYSTEM_ID 0x4a02
#define SHUMA_SYSTEM_ID 0x4ad2
//#define GEHUA_SYSTEM_ID 0x1811
#define GEHUA_SYSTEM_ID 0x100


extern SECFilter NITAFILTER;
extern SECFilter SDTAFILTER;
extern SECFilter SDTOFILTER;
extern SECFilter BATFILTER ;

extern SECFilter PATFILTER ;
extern SECFilter CATFILTER ;
extern SECFilter PMTFILTERx; 

//////////////////// 伪造扩展功能测试表 begin >>> //////////////////// 
// service type name 定义
const char *stn1="中央高清";
const char *stn2="地方卫视";
const char *stn3="武打类";
const char *stn4="言情类";
const char *stn5="中央高清, 科技类";
// 扩展服务表
const int tableItems=5;
ServiceTypeTableItem stTable[tableItems]=
{
	{1,stn1},
	{2,stn2},
	{3,stn3},
	{4,stn4},
	{5,stn5},
};

//////////////////// 伪造扩展功能测试表 end <<< //////////////////// 

inline void A2WString( const std::string& szStr, std::wstring& wszStr )
{
	int nLength=0;
#ifdef _WINDOWS
	// nLength = MultiByteToWideChar( CP_ACP, 0, szStr.c_str(), -1, NULL, NULL );
#else
	nLength = mbstowcs( NULL, szStr.c_str(), 0);
#endif
	wszStr.resize(nLength);
	wchar_t *lpwszStr = new wchar_t[nLength];
#ifdef _WINDOWS
	//	MultiByteToWideChar( CP_ACP, 0, szStr.c_str(), -1, lpwszStr, nLength );
#else
	mbstowcs(lpwszStr,szStr.c_str(),nLength);
#endif
	wszStr = lpwszStr;
	delete [] lpwszStr;
}

void correct_text( std::string& text )
{
	if( !text.length() ) 
		return;

	//	if( (char)text[0] >= 0x0 && 
	if( (unsigned char)text[0]<= 0x13 )
	{

		if( text[0] != 0x11)
			text.erase( 0, 1 );
		else
		{
			text.erase( 0, 1 );
			U8* data = new U8[text.size() + 2];
			for( size_t i = 0; i < text.size(); i += 2 )
			{
				data[i] = text.at(i+1);
				data[i+1] = text.at(i);
			}
			data[text.size()] = 0;
			data[text.size()+1] = 0;

			char gbk[255];
			UnicodeToGBK( reinterpret_cast<U16*>(data), gbk );
			text.assign( gbk );

			delete []data;	
		}
	}

	size_t len=strlen(text.c_str());
	text.resize(len);
}

ParseBase::ParseBase(void)
{
	dxreport("<parseBase> ParseBase::ParseBase\n");
	m_bNitToFull=false;
	m_iNitVersion=0;
	m_iBatVersion=0;
	mode_=STVMODE_NULL;
	// Test
	areaparam_.bouquet_id = 257;

#ifdef JS_USE_NOVELCA_SEARCH
	m_iSystemID = NOVEL_SYSTEM_ID;
#endif

#ifdef JS_USE_SHUMACA_SEARCH
	m_iSystemID= SHUMA_SYSTEM_ID;
#endif
	
#ifdef JS_USE_GEHUACA_SEARCH
	m_iSystemID = GEHUA_SYSTEM_ID;
#endif
	dxreport("<parsebase> systemID: 0x%x\n", m_iSystemID);

}

ParseBase::~ParseBase(void)
{
	dxreport("<parsebase> ParseBase::~ParseBase\n");
}

bool ParseBase::InitTVSearch(int searchMode)
{
	mode_ = (STVMode)searchMode;
	m_iNitVersion = 0xff;
	m_iBatVersion = 0xff;
//	m_iLastPATCRC = 0 ;
	m_bNitToFull = false;

	vecBatParam_.clear();
	nit_info_.clear();
	dvballservice_.clear();
	OneFreqSeachInit();

	return true;
}

void ParseBase::SetTuningParam(TuningParam tparam)
{
	OneFreqSeachInit();
	// 当前分析频点, dvbservice 要使用
	dvbstatus_.tuning_param.freq =  tparam.freq;
	dvbstatus_.tuning_param.qam =  tparam.qam;
	dvbstatus_.tuning_param.symb =  tparam.symb;
}

void ParseBase::OneFreqSeachInit()
{
	memset(&dvbstatus_,0,sizeof(dvbstatus_));
	memset(&catparam_,0,sizeof(catparam_));
	patparam_.clear();

	tuningparam_.clear();
	dvbOneFreqService_.clear();
	sdta_info_.clear();
	pmt_info_.clear();
	m_patTableSectionNumber.clear();
	m_sSdtATable.clear();
	m_sCatTable.clear();
	m_sNitATable.clear();
	m_sSdtOTable.Reset();
	m_sBatTable.clear();
	
	m_bUpdateFilter = false;

}

void ParseBase::SetAreaInfo(const AreaInfo areainfo)
{
	memcpy(&areaparam_ ,&areainfo ,sizeof(areainfo));
	dxreport("<parsebase> ParseBase::SetAreaInfo(%d)\n",areaparam_.AreaCode);
}


//
// STVMODE_NIT  模式:在取完NIT表后需要分析PAT,PMT,CAT表
// 手工模式或全频模式: first , 添加pat 和sdta 表过滤器
// 								: second , 添加pmt 表过滤器
int ParseBase::GetTableFilters(OUT vector<SECFilter>& tFilters, IN BuildStep bdStep)
{
	dxreport("<parsebase> ParseBase::GetTableFilters begin >>>. buildStep=%d\n",bdStep);

	if(bdStep==BS_NITBAT)
	{
		tFilters.push_back(NITAFILTER);
		tFilters.push_back(BATFILTER);
		dxreport("<parsebase> ParseBase::GetTableFilters NITAFILTER BATFILTER filter added\n");
	}
	else if(bdStep==BS_PATCATSDT)
	{
		tFilters.push_back(PATFILTER);
		tFilters.push_back(CATFILTER);
		tFilters.push_back(SDTAFILTER);
		dxreport("<parsebase> ParseBase::GetTableFilters pat cat sdta filter added\n");
	}
	else  // pmt
	{
		for (mapPatParamT::iterator it = patparam_.begin(); it != patparam_.end() ; it++)
		{
			PMTFILTERx.pid = it->second;
			tFilters.push_back(PMTFILTERx);
			dxreport("<parsebase> ParseBase::GetTableFilters PMT filter added, pid:0x%x\n",PMTFILTERx.pid);
		}
	}

	dxreport("<parsebase> ParseBase::GetTableFilters end <<<\n");
	return 0;
}


ePutDataRT ParseBase::AnalyseSectionData(U16 pid, U8 const *pData,U32 iDataLen)
{
//	dxreport("<parsebase> ParseBase::AnalyseSectionData begin >>> pid:0x%x pData:%p iDataLen:%d\n",pid,pData,iDataLen);

	ePutDataRT bRet = RT_CONTINUE ;

	if((pData == NULL ) || (iDataLen < 3))
	{
		dxreport("Warning! <parsebase> ParseBase::AnalyseSectionData Param not Valid, pData:0x%x, iDataLen:%d\n",pData, iDataLen);
		return bRet;
	}
/*
	// 测试是否频点切换成功
	// 该函数实现与逻辑不符合，所以忽略(hjj)
	// 实现是判断数据是否为新pat 表
	if(!IsNewPatGroup(pid,pData,iDataLen))
		return bRet;
*/
	if(pid == PAT_PID ) 
	{
		// TVCORE可能会推送多组PAT表数据;
		if(TableId_Pat == pData[0] && IsGoodPat(pData,iDataLen))
		{
			if(!m_bUpdateFilter && dvbstatus_.cat_ok)	
			{
				bRet = RT_UPDATE_FILTER;
				m_bUpdateFilter = true;
				dxreport("ParseBase::AnalyseSectionData pat ok,cat ok, need update filter\n");
			}
			else
			{
				bRet = RT_TABLE_OK;
			}
		}
	} 
	else if (pid == NIT_PID ) 
	{
		if((TableId_NitA == pData[0]) && IsGoodNitA(pData,iDataLen))
		{
			// NitA表分析完成
			if(mode_ == STVMODE_NIT	)
			{
				if(!m_bNitToFull && dvbstatus_.bat_ok)
				{
					m_bNitToFull = true ;
					bRet = RT_UPDATE_FREQ; 
				}
				else
				{
					bRet = RT_TABLE_OK ; 
				}
			}
			else
			{
				bRet = RT_TABLE_OK ; 
			}
		}
		if((TableId_NitO == pData[0]) && IsGoodNitO(pData,iDataLen))
		{
			// NitO表分析完成
			bRet = RT_TABLE_OK;
		}
	} 

	else if(pid == CAT_PID ) 
	{
		if((TableId_Cat == pData[0]) && IsGoodCat(pData,iDataLen))
		{
			if(!m_bUpdateFilter && dvbstatus_.pat_ok)	
			{
				bRet = RT_UPDATE_FILTER;
				m_bUpdateFilter = true;
				dxreport("ParseBase::AnalyseSectionData pat ok,cat ok, need update filter\n");
			}		
			else
			{
				// CAT表分析完成
				bRet = RT_TABLE_OK;
			}
		}
	} 

	else if (SDT_PID  == pid || pid == BAT_PID) // sdt_pd, bat_pid is equal
	{
		// BAT表
		if((TableId_Bat == pData[0]) && IsGoodBat(pData,iDataLen))
		{
			if(!m_bNitToFull && dvbstatus_.nita_ok)
			{
				m_bNitToFull = true ;
				bRet = RT_UPDATE_FREQ; 
			}
			else
			{
				bRet = RT_TABLE_OK;
			}
		}
		if((TableId_SdtA == pData[0]) && IsGoodSdtA(pData,iDataLen))
		{
			bRet = RT_TABLE_OK ; 
		}
		if((TableId_SdtO == pData[0]) && IsGoodSdtO(pData,iDataLen))
		{
			bRet = RT_TABLE_OK ; 
		}
	} 
	else
	{
		// other table ，当是pmt 表的时候(先判断)
		if(IsExistPmtPidInPat(pid) && IsGoodPmt(pData,iDataLen))
		{
			//PMT_PIDx表分析完成
			dxreport("ParseBase::AnalyseSectionData this pmt table ok, %d\n",pid);
			bRet = RT_TABLE_OK ; 
		}
	}

	if(ChkServiceOK())
	{
		dxreport("ParseBase::AnalyseSectionData service ok\n");
		bRet = RT_SERVICE_OK ; 
	}

	dxreport("curStae: pat_ok=%d,cat_ok=%d,nita_ok=%d,bat_ok=%d,sdta_ok=%d\n",dvbstatus_.pat_ok,dvbstatus_.cat_ok,dvbstatus_.nita_ok,dvbstatus_.bat_ok,dvbstatus_.sdta_ok);
	dxreport("<parsebase> ParseBase::AnalyseSectionData end <<<. ret=%d\n\n",bRet);
	return bRet;
}

bool ParseBase::IsExistPmtPidInPat(U16 pmtPid)
{
	for (mapPatParamT::iterator it = patparam_.begin(); it != patparam_.end(); it++)
	{
		if(it->second == pmtPid) return true;
	}

	return false;
}

bool ParseBase::IsExistProgNumberInPAT(const U32 iProgNum)
{
	mapPatParamT::iterator it = patparam_.find(iProgNum);
	if(it != patparam_.end()) return true;
	return false;
}

int ParseBase::GetTuningParamformNit(OUT vector<TuningParam>& TuningParamList)
{
	TuningParamList = tuningparam_;
	dxreport("<parsebase> ParseBase::GetTuningParamformNit size(%d)\n",TuningParamList.size());
	return 1;
}

int ParseBase::GetDVBExtServTypeTable(OUT vector<ServiceTypeTableItem>& table)
{
	// for test only
	table.clear();
	for(int i=0; i<tableItems; i++)
	{
		table.push_back(stTable[i]);
	}
	return tableItems;
}

bool ParseBase::IsGoodPat(U8 const *pData,U32 iDataLen)
{
	bool bRet = false;
	if((pData == NULL )  || (iDataLen < 3))
		return bRet;
	try
	{
		novelsuper::psisi_parse::PatSection *ppat = (novelsuper::psisi_parse::PatSection *)pData;

		U8 iSectionNumber = ppat->section_number();
		U8 iLastSectionNumber = ppat->last_section_number();

		if((iLastSectionNumber+1) == (U8)m_patTableSectionNumber.size())
		{
			dvbstatus_.pat_ok = true ;
			return true;
		}

		PSIConstraintT::iterator itConstraint = m_patTableSectionNumber.find(iSectionNumber);
		if(m_patTableSectionNumber.end() != itConstraint)
			return false;

		U16 iServiceID = 0,iPmtID=0;
		for( novelsuper::psisi_parse::PatSection::Loop_Iterator it = ppat->loop_begin(); !it.empty(); ++it)
		{
			iServiceID = it->program_number();
			if( 0 != iServiceID)
			{
				iPmtID = it->pid();
				dxreport("<parsebase> ParseBase::IsGoodPat find pmt_id=0x%x,service_id=0x%x\n",iPmtID,iServiceID);

				patparam_.insert(mapPatParamT::value_type(iServiceID,iPmtID));
			}
		}

		m_patTableSectionNumber.insert(PSIConstraintT::value_type(iSectionNumber,iSectionNumber));
		if((iLastSectionNumber+1) == (U8)m_patTableSectionNumber.size())
		{
			dvbstatus_.pat_ok = true ; 
			bRet = true;
		}
	}
	catch(novelsuper::psisi_parse::RuntimeError  &e)
	{
		dxreport("IsGoodPat: RuntimeError !!!, %s",e.what());
		return false;
	}
	return bRet;
}

bool ParseBase::IsGoodNitA(U8 const *pData,U32 iDataLen)
{
	bool bRet = false;
	if((pData == NULL )  || (iDataLen < 3))
		return bRet;
	try
	{
		novelsuper::psisi_parse::NitSection *pnit = (novelsuper::psisi_parse::NitSection *)pData;

		m_iNitVersion	  = pnit->version();
		U8 iSectionNumber = pnit->section_number();
		U8 iLastSectionNumber = pnit->last_section_number();

		if((iLastSectionNumber+1) == (U8)m_sNitATable.size())
		{
			dvbstatus_.nita_ok = true ; 
			return true;
		}

		PSIConstraintT::iterator itConstraint = m_sNitATable.find(iSectionNumber);
		if(m_sNitATable.end() != itConstraint)
			return false;

		NitInfoT info; // NIT表信息

		// 网络名
		novelsuper::psisi_parse::NetworkName_I nameIt = 
			pnit->begin<novelsuper::psisi_parse::DescNetworkName>();
		if(!nameIt.empty())
		{
			info.network_name = nameIt->network_name();
			correct_text(info.network_name);
#ifdef UNICODE
			std::wstring name;
			A2WString(info.network_name,name);
			dxreport("<parsebase> network_name=%s\n",name.c_str());
#else
			dxreport("<parsebase> network_name=%s\n",info.network_name.c_str());
#endif

		}

		for( novelsuper::psisi_parse::NitSection::Loop_Iterator r = pnit->loop_begin(); !r.empty(); ++r )
		{
			info.clear();
			info.iTsid = r->ts_id();
			info.iOnid = r->orig_network_id();

			// 频点描述
			novelsuper::psisi_parse::CableDeliverySys_I s = 
				r->begin<novelsuper::psisi_parse::DescCableDeliverySys>();
			if( !s.empty() )
			{
				TuningParam nitParam = {0,0,0};
				nitParam.freq = bcd2d(s->frequency()) / 10;
				nitParam.symb = bcd2d(s->symbol_rate()) / 10;
				nitParam.qam  = U8ToQam(s->modulation());
				tuningparam_.push_back(nitParam);

				info.iFreq = nitParam.freq;
				info.iQam  = nitParam.qam;
				info.iSymb = nitParam.symb;
			}

			// 业务描述
			novelsuper::psisi_parse::ServiceList_I s1 = 
				r->begin<novelsuper::psisi_parse::DescServiceList>();
			if(!s1.empty())
			{
				novelsuper::psisi_parse::DescServiceList::Loop_Iterator rr = s1->loop_begin();
				for (;!rr.empty();++rr)
				{
					NitServDescT desc;
					desc.service_id   = rr->service_id();
					desc.service_type = rr->service_type();
					info.sServList.push_back(desc);
				}
			}

			nit_info_.push_back(info);
		}

		m_sNitATable.insert(PSIConstraintT::value_type(iSectionNumber,iSectionNumber));
		if((iLastSectionNumber+1) == (U8)m_sNitATable.size())
		{
			dvbstatus_.nita_ok = true ; 
			bRet = true;
		}
	}
	catch(novelsuper::psisi_parse::RuntimeError &e)
	{
		dxreport("IsGoodNitA: RuntimeError!!! %s\n",e.what());
		return false;
	}

	return bRet;
}

bool ParseBase::IsGoodSdtA(U8 const *pData,U32 iDataLen)
{
	dxreport("IsGoodSdtA iDataLen:%d\n",iDataLen);
	bool bRet = false;
	if((pData == NULL )  || (iDataLen < 3))
		return bRet;
	try
	{
		novelsuper::psisi_parse::SdtSection *psdt = (novelsuper::psisi_parse::SdtSection *)pData;

		U8 iSectionNumber = psdt->section_number();
		U8 iLastSectionNumber = psdt->last_section_number();

		if((iLastSectionNumber+1) == (U8)m_sSdtATable.size())
		{
			dvbstatus_.sdta_ok = true ; 
			return true;
		}

		PSIConstraintT::iterator itConstraint = m_sSdtATable.find(iSectionNumber);
		if(m_sSdtATable.end() != itConstraint)
			return false;

		// 与其他表并行解析(获取结果时合并数据)--解决丢失节目名称
		for( novelsuper::psisi_parse::SdtSection::Loop_Iterator it_sdt = psdt->loop_begin(); !it_sdt.empty(); ++it_sdt )
		{
			SdtInfoT info_;

			info_.sid	= it_sdt->service_id();
			info_.net_id= psdt->original_network_id();
			info_.ts_id	= psdt->transport_stream_id();

			//service描述符
			novelsuper::psisi_parse::Service_I it_serv = it_sdt->begin<novelsuper::psisi_parse::DescService>();
			if( !it_serv.empty() )
			{
				std::string name = it_serv->service_name_char();
				correct_text(name);
				info_.name = name ;
				name = it_serv->service_provider_name_char();
				correct_text(name);

				info_.provider_name = name ;
				info_.service_type	= it_serv->service_type();
			}

			sdta_info_.insert(mapSdtInfoT::value_type(info_.sid,info_));
		}

		m_sSdtATable.insert(PSIConstraintT::value_type(iSectionNumber,iSectionNumber));
		if((iLastSectionNumber+1) == (U8) m_sSdtATable.size())
		{
			dvbstatus_.sdta_ok = true ; 
			bRet = true;
		}
	}
	catch(novelsuper::psisi_parse::RuntimeError &e)
	{
		dxreport("IsGoodSdtA: RuntimeError!!! %s\n",e.what());
		return false;
	}


	return bRet;
}

bool ParseBase::IsGoodSdtO(U8 const *pData,U32 iDataLen)
{
	bool bRet = false;
	if((pData == NULL )  || (iDataLen < 3))
		return bRet;

	U8 iCount = 0;
	novelsuper::psisi_parse::SdtSection *psdt = (novelsuper::psisi_parse::SdtSection *)pData;

	// 与其他表并行解析(获取结果时合并数据)--解决丢失节目名称
	for( novelsuper::psisi_parse::SdtSection::Loop_Iterator r = psdt->loop_begin(); !r.empty(); ++r )
	{
		U16 iSID = r->service_id();
		if( iSID == m_sSdtOTable.iBeginServiceId)
		{
			// 重复
			dvbstatus_.sdto_ok = true ; 
			return true;
		}

		sdta_info_[iSID].sid   = iSID ;
		sdta_info_[iSID].ts_id = psdt->transport_stream_id();
		sdta_info_[iSID].net_id= psdt->original_network_id();

		//service描述符
		novelsuper::psisi_parse::Service_I t = r->begin<novelsuper::psisi_parse::DescService>();
		if( !t.empty() )
		{
			std::string name = t->service_name_char();
			correct_text(name);
			sdta_info_[iSID].name = name;

			name = t->service_provider_name_char();
			correct_text(name);
			sdta_info_[iSID].provider_name = name;
			sdta_info_[iSID].service_type = t->service_type();

			iCount++;
		}

		if((0 != iCount)  && (INVALID_EVENT_SERVICEID == m_sSdtOTable.iBeginServiceId))
		{
			m_sSdtOTable.iBeginServiceId = iSID;
		}
	}

	return bRet;
}


bool ParseBase::IsGoodCat(U8 const *pData,U32 iDataLen)
{
	bool bRet = false;
	if((NULL == pData)  || (iDataLen < 3))
		return bRet;
	try
	{
		novelsuper::psisi_parse::CatSection *pcat = (novelsuper::psisi_parse::CatSection *)pData;
		U8 iSectionNumber = pcat->section_number();
		U8 iLastSectionNumber = pcat->last_section_number();

		if((iLastSectionNumber+1) == (U8) m_sCatTable.size())
		{
			dvbstatus_.cat_ok = true ; 
			return true;
		}

		PSIConstraintT::iterator itConstraint = m_sCatTable.find(iSectionNumber);
		if(m_sCatTable.end() != itConstraint)
			return false;

		for( novelsuper::psisi_parse::CA_I q = pcat->begin<novelsuper::psisi_parse::DescCA>(); ! q.empty(); ++q )
		{
			catparam_.ca_pid[catparam_.length] = q->ca_pid();
			catparam_.ca_system_id[catparam_.length] = q->ca_system_id();
			dxreport("ca_pid:0x%x ca_system_id:0x%x\n",catparam_.ca_pid[catparam_.length],catparam_.ca_system_id[catparam_.length]);
			catparam_.length++; 
		}

		m_sCatTable.insert(PSIConstraintT::value_type(iSectionNumber,iSectionNumber));
		if((iLastSectionNumber+1) == (U8) m_sCatTable.size())
		{
			dvbstatus_.cat_ok = true ; 
			bRet = true;
		}
	}
	catch(novelsuper::psisi_parse::RuntimeError &e)
	{
		dxreport("IsGoodCat: RuntimeError!!! %s\n",e.what());
		return false;
	}

	return bRet;
}

bool ParseBase::IsGoodPmt(U8 const *pData,U32 iDataLen)
{
	dxreport("ParseBase::IsGoodPmt begin >>> pData:%p, iDatalen:%d\n",pData, iDataLen);
	bool bRet = false;
	if((pData == NULL )  || (iDataLen < 3))
	{
		dxreport("Warning! ParseBase::IsGoodPmt pData:%p, iDataLen:%d",pData,iDataLen);
		return false;
	}
	try
	{
		novelsuper::psisi_parse::PmtSection *ppmt = (novelsuper::psisi_parse::PmtSection *)pData;

		U16 iServiceID = ppmt->program_number();
		if(!IsExistProgNumberInPAT(iServiceID))
		{
			dxreport("Info, programNum not in pat table.\n");
			return false;
		}
		mapDVBServiceT::iterator it = dvbOneFreqService_.find(iServiceID);
		if(it != dvbOneFreqService_.end())
		{
			dxreport("Info, alread in dvbService.\n");
			return false ;
		}

		DVBService one_;
		memset(&one_,0,sizeof(DVBService));
		one_.video_stream.stream_pid = INVALID_PID;
		one_.audio_stream[0].stream_pid = INVALID_PID;
		one_.audio_stream[1].stream_pid = INVALID_PID;
		one_.audio_stream[2].stream_pid = INVALID_PID;
		for(int i=0; i<catparam_.length; i++)
		{
			if(catparam_.ca_system_id[i] == m_iSystemID ) 
			{
				one_.emm_pid = catparam_.ca_pid[i];
				break;	
			}
		}
		mapPatParamT::iterator itPAT = patparam_.find(iServiceID);
		if(itPAT != patparam_.end())
		{
			one_.pmt_id = itPAT->second;
		}

		one_.serviceID		= iServiceID; 		//program num 等同于serviceID
		one_.pcr_pid	= ppmt->pcr_pid();
		one_.audio_index= 0;
		//hjj
		U16 ecm_pid = 0;
		U16 system_id = 0;

		// 首先提取descriptor loop 中的CA 描述符中的ecm_pid, 若存在保留之.
		novelsuper::psisi_parse::DescList_Iterator<novelsuper::psisi_parse::DescCA> d_it;
		for(d_it=ppmt->begin<novelsuper::psisi_parse::DescCA>();d_it!=ppmt->end<novelsuper::psisi_parse::DescCA>(); ++d_it)
		{
			if(!d_it.empty())
			{
				system_id = d_it->ca_system_id();
				if(system_id == m_iSystemID )
				{
#ifdef JS_USE_GEHUACA_SEARCH
					unsigned const char *p;
					d_it->private_data(p);
					dxreport("p[0]:0x%x, p[1]:0x%x\n",p[0],p[1]); 
					if(p[0]!=01 || p[1]!=0x2d)
						continue;
#endif
					
					ecm_pid=d_it->ca_pid();
					break;
				}
			}
		}
		// 
		U8 iAudio = 0;
		for( novelsuper::psisi_parse::PmtSection::Loop_Iterator r = ppmt->loop_begin();
				!r.empty(); ++r )
		{
			U8	iStream_Type = r->stream_type() ; 
			U16 iESPid = r->elementary_pid();
			// hjj	然后仍提取视频,音频流中的ecm_pid, 若存在，用后者。
			// 	注意，这里只是简单的后盖前, 根据实际情况，可以修改为实际的逻辑。
		//	novelsuper::psisi_parse::CA_I q8 = r->begin<novelsuper::psisi_parse::DescCA>();
		//	dxreport("attention!!\n");

			novelsuper::psisi_parse::DescList_Iterator<novelsuper::psisi_parse::DescCA> q8, q8b;
			for(q8=r->begin<novelsuper::psisi_parse::DescCA>();q8!= r->end<novelsuper::psisi_parse::DescCA>(); ++q8)
			{
				if(!q8.empty())
				{
					system_id = q8->ca_system_id();
//					q8b=++q8;
//					U16 system_id2 = q8b->ca_system_id();
					
					dxreport("system_id(pmt): 0x%x\n",system_id);
					if(system_id == m_iSystemID )
					{
//#ifdef JS_USE_GEHUA_SEARCH
//						q8b = ++q8;		// gehua 用下一个CA 描述符
//						ecm_pid = q8b->ca_pid();
//#else
#ifdef JS_USE_GEHUACA_SEARCH
											unsigned const char *p;
											q8->private_data(p);
											dxreport("p[0]:0x%x, p[1]:0x%x\n",p[0],p[1]); 
											if(p[0]!=01 || p[1]!=0x2d)
												continue;
#endif

						ecm_pid = q8->ca_pid();
//#endif
						dxreport("ecm_pid:0x%x\n",ecm_pid);
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
				if(iStream_Type == 0x06 )
				{
					iStream_Type = 0x7A;
				}
			}

			novelsuper::psisi_parse::DTS_I q2 = r->begin<novelsuper::psisi_parse::DescDTS>();
			if(!q2.empty())
			{
				if(iStream_Type == 0x06 )
				{
					iStream_Type = 0x7B;
				}
			}

			if(0x06 == iStream_Type){
				iStream_Type = 0x6A;
			}
			
			if(StreamID::IsVideo(iStream_Type))
			{
				one_.video_stream.stream_type= iStream_Type;
				one_.video_stream.stream_pid = iESPid;
				one_.video_stream.ecm_pid = ecm_pid;
			}
			else if(StreamID::IsAudio(iStream_Type))
			{
				// ??? 某些节目可能有超过3个以上的音频流 ???
				if(AUDIOSTREAM_MAXCOUNT > iAudio)
				{
					one_.audio_stream[iAudio].stream_type= iStream_Type;
					one_.audio_stream[iAudio].stream_pid = iESPid;
					one_.audio_stream[iAudio].ecm_pid = 0;
					one_.audio_stream[iAudio].name[0]='\0';
					one_.audio_stream[iAudio].ecm_pid = ecm_pid;
					// 音频流中如果存在多中类型时,标准中定义的码流优先
					iAudio++;
				}
			}
			else if(StreamID::IsObjectCarousel(iStream_Type))
			{
				dxreport("<parsebase> DSM-CC type:%d\n",iStream_Type);
			}
		}

		// 音频流中如果存在多中类型时,标准中定义的码流优先
		if(1 < iAudio)
		{
			for(int i =0 ; i < iAudio ;i++)
			{
				U8 iType = one_.audio_stream[i].stream_type;
				if((StreamID::PrivData != iType) && StreamID::IsAudio(iType))
				{
					one_.audio_index = i ;
					break;
				}
			}
		}

		if((INVALID_PID == one_.video_stream.stream_pid) && (INVALID_PID == one_.audio_stream[0].stream_pid))
		{
			dxreport("Warning, find INVALID_PID!,video_stream_pid:%d, audio_stream_pid:%d\n",one_.video_stream.stream_pid, one_.audio_stream[0].stream_pid);
			bRet = false;
		}
		else
		{
			dvbOneFreqService_.insert(mapDVBServiceT::value_type(iServiceID,one_));
			dxreport("insert one service...video_stream.ecm_pid:0x%x, audio_stream0.ecm_pid:0x%x,dvbOneFreqService_ size:%d\n", 
				one_.video_stream.ecm_pid, one_.audio_stream[0].ecm_pid,dvbOneFreqService_.size());
			bRet = true ;
		}
	}
	catch(novelsuper::psisi_parse::RuntimeError &e)
	{
		dxreport("IsGoodPmt: RuntimeError!!! %s\n",e.what());
		return false;
	}
	dxreport("ParseBase::IsGoodPmt end <<<\n");
	return bRet;
}

U32 ParseBase::bcd2d( U32 bcd )
{
	U32 d = bcd & 0x0F;
	unsigned long i, c, tail;
	for( i = 1, c = 1, bcd >>= 4; bcd != 0; ++i )
	{
		tail = bcd & 0x0F;
		c = c * 10;
		d+= c * tail;
		bcd  >>= 4;
	}
	return d;
};

U8 ParseBase::U8ToQam( U8 m )
{
	U8 iRet = 0x0;
	switch(m)
	{
		case 0:
		case 1:
			iRet = 0x0;	// QAM16
			break;
		case 2:
			iRet = 0x1;	// QAM32
			break;
		case 3:
			iRet = 0x2;	// QAM64
			break;
		case 4:
			iRet = 0x3;	// QAM128
			break;
		case 5:
			iRet = 0x4;	// QAM256
			break;
		default:
			iRet = 0x10;//
			break;
	}
	return iRet ; 
}

// 检查是否已完成服务搜索
bool ParseBase::ChkServiceOK()
{
	// STVMODE_MANUAL,STVMODE_FULL,STVMODE_NIT
	if(!dvbstatus_.pat_ok || !dvbstatus_.sdta_ok || !dvbstatus_.cat_ok) return false;
	if(patparam_.size() != dvbOneFreqService_.size()) return false; // 判定该频点pmt 表搜索完整
	return true;
}


int ParseBase::GetVersion(IN const STVMode iMode,OUT BYTE* pNitVersion, OUT BYTE *pBatVersion)
{
	//	(void)iMode;
	if(iMode==STVMODE_NIT)
	{
		*pNitVersion = m_iNitVersion;
		*pBatVersion = m_iBatVersion;
	}
	return 0 ;
}

bool ParseBase::DVBServiceSortByChNo(const DVBService& one,const DVBService& two){
	return (one.channel_number < two.channel_number) ? true : false;
}
bool ParseBase::DVBServiceSortByServceID(const DVBService& one,const DVBService& two){
	return (one.serviceID < two.serviceID) ? true : false;
}

int ParseBase::GetDVBALLService(IN vector<DVBService>& services)
{
	// 书写一下版本号
#ifndef WIN32		
		vector<DVBService>::iterator it=services.begin();
		if(it!=services.end())
		{
			FILE *fp=fopen("/data/data/novel.supertv.dvb/databases/version.txt","w");
			if(fp)
			{
				fprintf(fp,"nitversion:%d\n",it->nitVersion);
				fprintf(fp,"batversion:%d\n",it->batVersion);
				fprintf(fp,"systemID:%d\n",m_iSystemID);
				fclose(fp);
			}
		}
#endif
	return 0;
}


/*
bool ParseBase::FindFreqInfoFromNit(U16 iSID,TuningParam& sTuning)
{
	bool bRet = false;
	for(NitInfoListT::iterator it = nit_info_.begin();it != nit_info_.end();it++)
	{
		for (NitServiceListT::iterator servIt = it->sServList.begin(); servIt != it->sServList.end();servIt++)
		{
			if(iSID == servIt->service_id)
			{
				sTuning.freq = it->iFreq;
				sTuning.symb = it->iSymb;
				sTuning.qam  = it->iQam;
				bRet = true ;
				break;
			}
		}
	}

	return bRet;
}

bool ParseBase::IsNewPatGroup(U16 pid , U8 const *pData,U32 iLen)
{
	bool bRet = true;
	if(mode_ == STVMODE_MANUAL) return true;  // 手动搜索仅一个频点，一定为真
	if(pid != PAT_PID ) return true; // 不是pat 表，我们认为真。 理论上每个频点上PAT表数据最先发
	
	U32 iPATCRC = GetPATCRC(pData,iLen);
	if(m_iLastPATCRC == iPATCRC) return false; //PAT CRC未改变，还未切换，不是新PAT
	
	m_iLastPATCRC = iPATCRC ; 
	return true ;
}

U32  ParseBase::GetPATCRC(U8 const *pData,U32 iLen)
{
	UNUSED_PARAM(iLen);
	novelsuper::psisi_parse::PatSection *ppat = (novelsuper::psisi_parse::PatSection *)pData;
	U32 iCRC =  novelsuper::psisi_parse::n2h32(ppat->CRC_32());

	dxreport("<parsebase> ParseBase::GetPATCRC iCRC = %u\n",iCRC);
	return iCRC;
}


*/

