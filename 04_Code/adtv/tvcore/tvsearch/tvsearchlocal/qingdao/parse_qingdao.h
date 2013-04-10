
#ifndef __PARSER_OF_QINGDAO_HH__
#define __PARSER_OF_QINGDAO_HH__

#include "parsebase.h"
#include <map>
using namespace std;
typedef struct stChNum{
	U32 sid ;
	U16 channel_number;
	U8 subscribed_only_flag;
	U8 volumn_set;
	U8 track_set;
	U8 program_group;

}stChNum;
typedef map<U16,stChNum> mapSidChannalVolumeTrackset;


typedef struct bouquetCountry{
	U16 bouquet;
	U32 country_code;
}bouquetCountry;
typedef std::vector<bouquetCountry> bouquetCountryListT;

class ParseQingDao : public ParseBase
{
public:
	ParseQingDao()
	{
	};
	~ParseQingDao()
	{
	};
	int SetParameter(int key, const void* request,int reqLength);
	int GetParameter(int key, void* reply,	int* replyLength);

	bool InitTVSearch(int searchMode);
	int GetDVBServices(OUT vector<DVBService>& services);
	int GetDVBALLService(OUT std::vector<DVBService>& services);
	bool IsGoodBat(U8 const *pData,U32 iDataLen);
	bool IsGoodSdtA(U8 const *pData,U32 iDataLen);
	bool IsGoodNitA(U8 const *pData,U32 iDataLen);

private:

//	void GetBatParam(IN vector<BatParam>& batParam, map<U32, BatParam>& mapBatParamValid);
	void GetValidBouquetID();
	void AddServicesCategory(INOUT vector<DVBService>& argServices);
	
	void DeleteReduntantByBat(map<U32, BatParam>& mapBatParamValid,INOUT vector<DVBService>& services) const;
	void DeleteReduntantBySdtA(mapSidChannalVolumeTrackset& mapChIndex,INOUT vector<DVBService>& services)const;
	void SetChannelInfo(mapSidChannalVolumeTrackset& mapChIndex,INOUT vector<DVBService>& services) const;
	U32  ConvertCountryCode(U32 input) const;
	
	mapSidChannalVolumeTrackset     mapSid_CVT_;   /// ≈≈–Ú Chanel Index
	bouquetCountryListT sBouquetCountryList;
	U32	m_validBouquetID;

};

#endif
