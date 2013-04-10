#include "parsebase.h"
#include "bat_fuzhou.h"
class ParseFuZhou : public ParseBase
{
	public:
	bool IsGoodBat(U8 const *pData,U32 iDataLen);
	int GetDVBServices(OUT vector<DVBService>& services);
	int GetDVBALLService(OUT vector<DVBService>& services);
	
	private:
	MapProgOrder_VoiceC_DescT m_mapProgOrder_VoiceC;		
};

