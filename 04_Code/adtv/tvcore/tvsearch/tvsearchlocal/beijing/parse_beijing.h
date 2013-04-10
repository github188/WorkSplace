#include "parsebase.h"


typedef struct  
{
	U16 service_id;		//业务ID.
	//	U8 tv_mode;			// 电视制式。0:PAL, 1:NTSC, 2:SECAM.
	U16 order_number;	// 该业务在网络搜索中排列顺序.
}BeijingProgramOrderDesc;
typedef std::map<U16, BeijingProgramOrderDesc> MapProgramOrderDescT;

/**
 *	descriptor_tag: 0x89
 *	service_id: 对应TS 中的业务ID 
 *	left_right_channel: 左右声道是否播放。00:无声音播出，01:左声道播出，02:右声道播出，03:左右声道播出（立体声）。
 *	volume_compensation:音量调整。音量调节值范围为(0—32),表示音量的大小。机顶盒的音量补偿值= volume_compensation -16
 **/
typedef struct  
{
	U16 service_id;			//	对应TS中的业务ID.
	U8	left_right_channel;	
	U8	volume_compensation;
}BeijingProgramVoiceDesc;
typedef std::map<U16, BeijingProgramVoiceDesc> MapProgramVoiceDescT;

class ParseBeijing : public ParseBase
{
	public:
	bool IsGoodBat(U8 const *pData,U32 iDataLen);
	int GetDVBServices(OUT vector<DVBService>& services);
	int GetDVBALLService(OUT vector<DVBService>& services);
	
	private:
		MapProgramOrderDescT m_mapProgOrder;
		MapProgramVoiceDescT m_mapProgVoiceComp;
};
