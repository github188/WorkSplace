#include "typ.h"
#include "cdcas_hdr.h"
#include <capture.h>
#include "stbca_utility.h"
#include <stbruntime.h>
#include <DVTCAS_STBInf.h>

#define LOG_LEVEL LOG_TRACE
#define LOG_TAG "stbca_t"
#include "dxreport.h"


extern ObjStbCa gStbCa;

int main(int argc, char* argv[])
{
	
		if(!Ca_Init()) 
		{
			printf("Ca_Init failed\n");
			return 1;
		}
		printf("**************\n");
		printf("wait a while for card init, 10 seconds...\n");
		dxreport("wait a while for card init, 10 seconds...\n");
//		NS_sleep(10000); // 等待智能卡复位完成, 数码不需要等待
	
		printf("=====================after Ca_Init=====================\n");
		dxreport("=====================after Ca_Init=====================\n");
////////////////////////////////////////////////////////////////////////////////			
// 获取STBCA的类型, 自己程序定义的
////////////////////////////////////////////////////////////////////////////////			
		CasType type = stbca_cas_type();
		printf("type: %d\n",type);
	
////////////////////////////////////////////////////////////////////////////////			
// 获取STBID号. 获取盒子号
////////////////////////////////////////////////////////////////////////////////			
		WORD platformID;
		ULONG uniqueID;
		stbca_get_stbid(&platformID, &uniqueID);
		printf("platformID: %d, uniqueID: %lu\n",platformID, uniqueID);
////////////////////////////////////////////////////////////////////////////////
// 获取STBCA的版本(机顶盒CA模块版本)
////////////////////////////////////////////////////////////////////////////////
		
		U8 emailNum=0, freeNum=0;
		stbca_get_email_space(emailNum, freeNum);
		printf("emailNum: %d, freeNum: %d\n",emailNum,freeNum);
	
		if(gStbCa.bSCardStatus_== 0/*SC_OUT*/)
		{
			printf("card not inserted\n");
			return 1;
		}
		
////////////////////////////////////////////////////////////////////////////////			
// 与卡有关 
////////////////////////////////////////////////////////////////////////////////			
// 获取卡号	, cos version, ca module version, manufacture name
////////////////////////////////////////////////////////////////////////////////			
		char szCardID[128];
		szCardID[0]=0;
		memset(szCardID, 0, 128);
		int res=stbca_GetCardID(szCardID);
		if(!res)
		{
			printf("szCardID is %s\n",szCardID);
		}
		else
		{
			printf("J_StbcaGetCardID return error, szCardID is %s, ret is %d\n",szCardID,res);
		}
		
		char szCosVersion[64];
		DvtCACosVersion(szCosVersion, sizeof(szCosVersion));
		printf("szCosVersion is %s\n", szCosVersion);

		char szCaModuleVersion[64];
		stbca_version(szCaModuleVersion);
		printf("szCaModuleVersion: %s\n",szCaModuleVersion);

		char name[128];
		DvtCAManuName(name, sizeof(name));
		printf("Manufactor name: %s\n", name);
////////////////////////////////////////////////////////////////////////////////
// 区域码	
////////////////////////////////////////////////////////////////////////////////
		SDVTCAAreaInfo areaInfo;
		if(DvtCAGetAreaInfo(&areaInfo)==DVTCA_OK)
		{
			printf("CardArea:%lu, SetCardTime:%lu,flag:%d\n", areaInfo.m_dwCardArea,\
					areaInfo.m_tSetStreamTime, areaInfo.m_byStartFlag);
		}
		else
		{
			printf("DvtCAGetAreaInfo return failed\n");
		}
////////////////////////////////////////////////////////////////////////////////
// 子母卡属性
////////////////////////////////////////////////////////////////////////////////
		DWORD motheID;
		if(DvtCAGetMotherInfo(&motheID)==DVTCA_OK)
		{
			if(motheID == 0)
			{
				printf("this is mother card\n");
			}
			else
			{
				printf("this is son card, ID:%lu\n",motheID);
			}
		}
		else
		{
			printf("DvtCAGetMotherInfo return failed\n");
		}



////////////////////////////////////////////////////////////////////////////////
// 获取运营商ID 列表	
////////////////////////////////////////////////////////////////////////////////
		std::vector<OperatorId> operatorIDs;
		std::vector<PurseId> purseIds;
		std::vector<Entitle> entitles;
		OperatorInfo info("");
		operatorIDs.clear();
		stbca_get_operator_ids(operatorIDs);
		int operSize = operatorIDs.size();
		printf("operatorIDs size :%d\n",operSize );
		for(unsigned int i=0; i<operatorIDs.size(); i++)
		{
			stbca_get_operator_info(operatorIDs[i],info);
			printf("operator name is %s\n",info.name.c_str());
/*			
			U16 res=stbca_get_purse_ids(operatorIDs[i],purseIds);
			printf("purseIDS size is %d\n", purseIds.size());
			for(UINT j=0; j<purseIds.size(); j++)
			{
				printf("purse IDs is %d\n",purseIds[j]);
			}
*/			
			stbca_get_service_entitles(operatorIDs[i], entitles);
			printf("entitles size is %d\n", entitles.size());
			for(UINT j=0; j<entitles.size(); j++)
			{
				printf("entitles IDs is %d\n",entitles[j].product_id);
			}
		}
////////////////////////////////////////////////////////////////////////////////			
// 验证pin 码， 初始值12345678, 合并到后面
////////////////////////////////////////////////////////////////////////////////			
#if 0
		SDVTCAPin PIN;
		PIN.m_byLen = 8;
		PIN.m_byszPin[0] = 0x01;
		PIN.m_byszPin[1] = 0x02;
		PIN.m_byszPin[2] = 0x03;
		PIN.m_byszPin[3] = 0x04;
		PIN.m_byszPin[4] = 0x05;
		PIN.m_byszPin[5] = 0x06;
		PIN.m_byszPin[6] = 0x07;
		PIN.m_byszPin[7] = 0x08;
		HRESULT nRet = DVTCASTB_VerifyPin(&PIN);
		switch (nRet)
		{
			case DVTCA_OK:
				printf("pin veriry passed!\n");
				break;
			case DVTCAERR_STB_DATA_LEN_ERROR:
				printf("pin length error\n");
				break;
			case DVTCAERR_STB_PIN_INVALID:
				printf("pin invalid\n");
				break;
			case DVTCAERR_STB_PIN_LOCKED:
				printf("pin locded\n");
				break;
			default:
				printf("pin other value, nRet:%lu\n",nRet);
				;
		}
#endif		

////////////////////////////////////////////////////////////////////////////////			
// 修改pin 码， 改为88888888, 8个8, 慎用， 三次修改不成功会锁定
// 其它修改观看级别，修改时间段pin 码三次不对，也会锁定
////////////////////////////////////////////////////////////////////////////////			
#if 1	
		unsigned char oldpin[]="12345678";
		unsigned char newpin[]="12345678";
//		unsigned char newpin[]="88888888";
#else	
		unsigned char oldpin[]="12345678";
		unsigned char newpin[]="88888888";
#endif	
#if 0
		res=stbca_change_pin_code(oldpin, newpin);
		if(res==CDCA_RC_OK)
		{
			printf("change pin succeed, res:%d\n",res);
		}
		else
		{
			printf("change pin failed, res:%d\n",res);
		}
#endif
////////////////////////////////////////////////////////////////////////////////
// pin 码, 观看级别，工作时段
// 注意. 工作时段默认关闭, 虽显示修改成功,但读出仍为23:59
// 欲打开请联系shuma
////////////////////////////////////////////////////////////////////////////////
		// pin 码锁定否
		bool pLocked;
		bool bRes = DvtCAIsPinLocked(&pLocked);
		if(bRes)
		{
			printf("DvtCAIsPinLocked: %d\n", pLocked);
		}
		// 获取观看级别 
		int rating=stbca_get_watch_rating();
		printf("rating is %d\n", rating);
		

		// 设置观看级别 
		res=stbca_set_watch_rating(newpin,8);
		if(res==CDCA_RC_OK)
		{
			printf("set watch rating succeed,res:%d\n",res);
		}
		else
		{
			printf("set watch rating failed, res: %d\n",res);
		}
		//读取观看时段	
		U8 start_h=0, end_h=23;
		U8 start_m=0, start_s=0, end_m=59, end_s=59;
		stbca_get_watch_time2(start_h, start_m, start_s, end_h,end_m,end_s);
		printf("start_h: %d, start_m:%d, start_s:%d, end_h:%d, end_m:%d, end_s:%d\n", start_h,start_m,start_s, end_h,end_m,end_s);
		
		//设置观看时段
		start_h = 2;
		start_m = 1;
		start_s = 1;
		
		end_h = 22;
		end_m = 1;
		end_s = 1;
//		res=stbca_set_watch_time1(newpin, start_h,	end_h);
		res=stbca_set_watch_time2(newpin, start_h, start_m, start_s, end_h, end_m, end_s);
		if(res==CDCA_RC_OK)
		{
			printf("set watch time ok,res:%d\n",res);
		}
		else
		{
			printf("set watch time failed, res:%d\n",res);
		}
		stbca_get_watch_time2(start_h, start_m, start_s, end_h,end_m,end_s);
		printf("start_h: %d, start_m:%d, start_s:%d, end_h:%d, end_m:%d, end_s:%d\n", start_h,start_m,start_s, end_h,end_m,end_s);
		
#if 0
		DVBService s;
		INITTESTDATA(s);
#endif
		printf("waiting...\n");
		while(1)
		{
			printf("waiting2...\n");
			NS_sleep(1000);
		}

		Ca_Uninit();
		return 0;

}


