/*-----------------------------------------------------------------------------
-                                   DVT移植库                                 -
-                                                                             -
-                        ( c ) Copyright 2012-2015                            -
-                          All Rights Reserved                                -
-
-  Description: Dvt interface is Dvt lib functions                                                                          -
-  是Dvt Lib 函数的简单封装. 其中有的名字借用了同方的名称
------------------------------------------------------------------------------*/
#include <typ.h>
#include <capture.h>
#include "cdcalib4stb.h"
#include "DVTCAS_STBInf.h"
#include <stbca_utility.h>
// #include "dvtcas_stbref.h"		// 它简单，供参考用

#define LOG_LEVEL LOG_TRACE
#define LOG_TAG "stbca_dvt"
#include <dxreport.h>

HRESULT DVT_VerfiryPin(SDVTCAPin *pPinCode);

extern ObjStbCa gStbCa;

extern void StartNitMonitor();
////////////////////////////////////////////////////////////////////////////////
// 以下为关键接口, 与解扰紧密相关, 为第一步要实现，测试,验证接口
////////////////////////////////////////////////////////////////////////////////

CDCA_BOOL CDCASTB_Init( CDCA_U8 byThreadPrior )
{
	dxreport("DVTCASTB_Init begin >>>\n");
	gStbCa.ignoreCAMsg = false;
	bool res=DVTCASTB_Init();
	DVTCASTB_AddDebugMsgSign(1);  // 打开debug 信息
	dxreport("DVTCASTB_Init end res:%d<<<\n", res);
	return res;
}

void CDCASTB_SetEcmPid( CDCA_U8 byType, const SCDCASServiceInfo* pServiceInfo )
{
	// 兼容novel 的first , last 参数
	if(pServiceInfo == NULL) return;
	
	SDVTCAServiceInfo info;
	info.m_wEcmPid = pServiceInfo->m_wEcmPid;
	info.m_wServiceID = pServiceInfo->m_wServiceID[0];
		
	dxreport("DVTCASTB_SetCurEcmInfo begin ecmpid:%d, serviceid:%d >>>\n",info.m_wEcmPid,info.m_wServiceID);
	DVTCASTB_SetCurEcmInfo(&info);
	dxreport("DVTCASTB_SetCurEcmInfo end <<<\n");
}
void  CDCASTB_SetEmmPid( CDCA_U16 wEmmPid )
{
	dxreport("CDCASTB_SetEmmPid begin >>>\n");
   	DVTCASTB_SetEmmPid(wEmmPid);
   	dxreport("CDCASTB_SetEmmPid end >>>\n");
}
void CDCASTB_PrivateDataGot( 
		CDCA_U8        byReqID,
		CDCA_BOOL      bTimeout,
		CDCA_U16       wPid,
		const CDCA_U8* pbyReceiveData,
		CDCA_U16       wLen )
{
	dxreport("DVTCASTB_StreamGuardDataGot begin >>>\n");
	bool bSuccess=true;
	if(wLen == 0) bSuccess = false;
	// dvt reqID 要变回原样
	DVTCASTB_StreamGuardDataGot(byReqID & 0x7F, bSuccess, wPid, wLen, pbyReceiveData);
	dxreport("DVTCASTB_StreamGuardDataGot end <<<\n");
}
/*  Description: 插入智能卡 */
CDCA_BOOL CDCASTB_SCInsert( void )
{
	CDCA_U8 byReaderNo = 0;
	dxreport("CDCASTB_SCInsert begin >>>\n");
	DVTCASTB_SCInsert(byReaderNo);
	dxreport("CDCASTB_SCInsert end <<<\n");
	return true;
}

void CDCASTB_SCRemove( void )
{
	dxreport("DVTCASTB_SCRemove begin >>>\n");
	DVTCASTB_SCRemove();
	dxreport("DVTCASTB_SCRemove end <<<\n");
}

//数码要传出外部卡号
CDCA_U16 CDCASTB_GetCardSN( char* pCardSN )
{
	SDVTCAManuInfo info;
	memset(&info, 0, sizeof(SDVTCAManuInfo));
	HRESULT res = DVTCASTB_GetStreamGuardManuInfo(&info);
	if(res == DVTCA_OK)
	{
//		memcpy(pCardSN, info.m_byszSerialNO, strlen((char *)info.m_byszSerialNO));
		strcpy(pCardSN,(char *)info.m_byszSerialNO);
		return CDCA_RC_OK;
	}
	return res;
}

////////////////////////////////////////////////////////////////////////////////
// 以上为关键接口, 与解扰紧密相关, 为第一步要实现，测试, 验证接口
////////////////////////////////////////////////////////////////////////////////

CDCA_U16 CDCASTB_ChangePin( const CDCA_U8* pbyOldPin, const CDCA_U8* pbyNewPin)
{
	return DvtCAChangePin((const char*)pbyOldPin, (const char*)pbyNewPin);
}

U32 DvtCAChangePin( const char * pbyOldPin, const char * pbyNewPin)
{
	dxreport("Enter %s new %s old %s\n", __FUNCTION__, pbyNewPin, pbyOldPin);
	HRESULT hRes;
	SDVTCAPin newPin;
	SDVTCAPin oldPin;
	newPin.m_byLen = 8;
	oldPin.m_byLen = 8;
	memcpy(newPin.m_byszPin, pbyNewPin,8);
	memcpy(oldPin.m_byszPin, pbyOldPin,8);
	// 验证oldpin
	hRes=DVT_VerfiryPin(&oldPin);
	//if(hRes != DVTCA_OK) return CDCA_RC_UNKNOWN;
	if(hRes != DVTCA_OK) return hRes;

	hRes = CDCA_RC_UNKNOWN;
	bool pinPass = false;
	hRes = DVTCASTB_IsPinPass(&pinPass);
	if(pinPass == true)
	{
		hRes = DVTCASTB_ChangePin(&newPin);
		printf("DVTCASTB_ChangePin return hRes:%ld\n", hRes);
	}
	
	dxreport("Leave %s\n",__FUNCTION__);
	return hRes;
}

void CDCASTB_DelEmail( CDCA_U32 dwEmailID )
{
	 HRESULT res = DVTCASTB_DelEmail(dwEmailID);
}

CDCA_U16 CDCASTB_GetEmailContent( CDCA_U32 dwEmailID, SCDCAEmailContent* pEmailContent )
{
	SDVTCAEmailContent emaiCountent;
	memset(&emaiCountent, 0, sizeof(SDVTCAEmailContent));

	HRESULT res = DVTCASTB_GetEmailContent(dwEmailID,&emaiCountent);
	if(res == DVTCA_OK)
	{
		//数码邮件内容长1024 + 1,永新160 + 1,#####################注意越界
		memcpy(pEmailContent->m_szEmail, emaiCountent.m_szEmail, emaiCountent.m_wEmailLength);
	}
	return CDCA_RC_UNKNOWN;
}

CDCA_U16  CDCASTB_GetEmailHeads( SCDCAEmailHead* pEmailHead,
                                       CDCA_U8*        pbyCount,
                                       CDCA_U8*        pbyFromIndex )

{
	//还需要调用者,一次性获取50封邮件


	//数码文档中说传50个,最多是50
	WORD emailCount = 50;
	SDVTCAEmailInfo info[50];
	HRESULT res = DVTCASTB_GetEmailHead(&emailCount, info);
	if(res == DVTCA_OK)
	{
		for(int i = 0; i < emailCount; i++)
		{
			//数码还需要上传发送者姓名
			//m_szSenderName[DVTCA_MAXLEN_EMAIL_SENDERNAME];		//发送者姓名
			pEmailHead[i].m_dwActionID = info[i].m_dwVersion;		//数码邮件ID,就是发送时间
			pEmailHead[i].m_tCreateTime = info[i].m_dwVersion;
			pEmailHead[i].m_wImportance = 0;
			//注意,CDCA长30 + 1 ,DVTCA长20 + 1
			memcpy(pEmailHead[i].m_szEmailHead, info[i].m_szTitle, strlen(info[i].m_szTitle));
			pEmailHead[i].m_bNewEmail = info[i].m_Status == DVTCAS_EMAIL_STATUS_INI ? 1 : 0;
		}
		return CDCA_RC_OK;
	}
	return CDCA_RC_UNKNOWN;
}

//数码不支持
CDCA_U16 CDCASTB_GetEmailHead( CDCA_U32 dwEmailID, SCDCAEmailHead* pEmailHead )
{
	return CDCA_RC_UNKNOWN;
}

CDCA_U16 CDCASTB_GetEmailSpaceInfo( CDCA_U8* pbyEmailNum, CDCA_U8* pbyEmptyNum )
{
	return DvtCAGetEmailSpaceInfo(pbyEmailNum, pbyEmptyNum);
}
//数码只支持查询,已读邮件数,未读邮件数,不支持可用空间查询######################注意
U32 DvtCAGetEmailSpaceInfo( CDCA_U8* pbyEmailNum, CDCA_U8* pbyEmptyNum )
{
	WORD emailCount;
	WORD newEmailCount;
	HRESULT res = DVTCASTB_GetEmailCount(&emailCount,&newEmailCount);
//	if(res == DVTCA_OK)
//	{
//		//处理返回值,参数类型也不相同 
//	}
	*pbyEmailNum  = emailCount;
	*pbyEmptyNum = newEmailCount;
	dxreport("space info  %d %d\n", emailCount, newEmailCount);
	return res;
	//return CDCA_RC_UNKNOWN;
}

CDCA_U16 CDCASTB_GetIPPVProgram( CDCA_U16 wTvsID, SCDCAIppvInfo* pIppv, CDCA_U16* pwNumber )
{
	return CDCA_RC_UNKNOWN;
}

//获取运营商列表
CDCA_U16 CDCASTB_GetOperatorIds( CDCA_U16* pwTVSID )
{
	//这里数码最多有5个运营商,永新最多有4个
	//如果使用5,有可能越界使用外边传进来的数组###################注意
	BYTE count = DVTCA_MAXNUMBER_TVSID;
	SDVTCATvsInfo info[DVTCA_MAXNUMBER_TVSID];
	dxreport("CDCASTB_GetOperatorIds begin <<<\n");
	HRESULT ret = DVTCASTB_GetOperatorInfo(-1, &count,info);
	dxreport("operator size is %d\n", count);
	if(ret == DVTCA_OK)
	{
		for(int i = 0; i < count; i++)
		{
			pwTVSID[i] = info[i].m_wTVSID;	
		}
		return CDCA_RC_OK;
	}
	return CDCA_RC_UNKNOWN;
}

//查询运营商信息
CDCA_U16 CDCASTB_GetOperatorInfo( CDCA_U16 wTVSID, SCDCAOperatorInfo* pOperatorInfo )
{
	CDCA_U8 byCount = 1;  // 最大支持5 个运营商
	SDVTCATvsInfo info;
	memset(&info, 0, sizeof(SDVTCATvsInfo));
	HRESULT res = DVTCASTB_GetOperatorInfo(wTVSID, &byCount, &info);
	if(res == DVTCA_OK)
	{
		strcpy(pOperatorInfo->m_szTVSPriInfo, info.m_szTVSName);
		return CDCA_RC_OK;
	}
	return CDCA_RC_UNKNOWN;
}

//授权ID列表,数码不支持
CDCA_U16 CDCASTB_GetEntitleIDs( CDCA_U16  wTVSID, CDCA_U32* pdwEntitleIds )
{
	return CDCA_RC_UNKNOWN;
}

CDCA_U32 CDCASTB_GetVer( void )
{
	SDVTCAManuInfo info;
	memset(&info, 0, sizeof(SDVTCAManuInfo));
	HRESULT res = DVTCASTB_GetStreamGuardManuInfo(&info);
	if(res == DVTCA_OK)
	{
		return info.m_dwSTBCASVer;
	}
	return CDCA_RC_UNKNOWN;
}


U32 DvtCAGetEmailContent(U32 email_id, SDVTCAEmailContent * pContent)
{
	return DVTCASTB_GetEmailContent(email_id, pContent);
}

U32 DvtCAInquireBookIppOver(U32 ecm)
{
	DVTCASTB_InquireBookIppOver((WORD)ecm);
	return 0;
}

U32 DvtCADelEmail(U32 id)
{
	return DVTCASTB_DelEmail(id);
}

U32 DvtCAVerifyPin(U8 * pin)
{
	SDVTCAPin dPin;
	memset(&dPin, 0, sizeof(dPin));
	dPin.m_byLen = strlen((char *)pin);
	memcpy(dPin.m_byszPin, pin, dPin.m_byLen);
	return DVTCASTB_VerifyPin(&dPin);
}

U32 DvtCAGetOperatorInfo(U8 * pCount, SDVTCATvsInfo * pInfo)
{
	HRESULT ret = DVTCASTB_GetOperatorInfo((WORD)-1, pCount,pInfo);
	return ret;
}

U32 DvtCAEmailRead(U32 email_id)
{
	return DVTCASTB_EmailRead(email_id);
}

U32 DvtCAShowOSDOver(U32 duration)
{
	DVTCASTB_ShowOSDMsgOver(duration);
	return 0;
}


U32 DvtCAGetPurseInfo(U32 operid, U32 * balance, U32 * remainder)
{
	HRESULT ret = DVTCASTB_GetMoneyInfo(operid, (DWORD*)balance, (DWORD*)remainder);
	dxreport(" %s %x!\n",__FUNCTION__, ret);
	return ret;
}

U32 DvtCAGetIpps(U8 * count, SDVTCAIpp * pIpps)
{
	return DVTCASTB_GetBookIpps(count, pIpps);
}

U32 DvtCABookIpp(SDVTCAIpp * pIpp)
{
	return DVTCASTB_BookIpp(pIpp);
}

U32 DvtCAGetEmailHeads(U16 * pCount, SDVTCAEmailInfo * pEmails)
{
	return DVTCASTB_GetEmailHead(pCount, pEmails);
}

U32 DvtCAGetServiceEntitles(U32 sid, U8 * pCount, SDVTCAServiceEntitle * pEntitles)
{
	return DVTCASTB_GetServiceEntitles(sid, pCount, pEntitles);
}


U32 DvtCAAddDebugMsgSign(bool sign)
{
	DVTCASTB_AddDebugMsgSign(sign);
	return 0;
}

void DvtCACosVersion(char *version, int length)
{
	SDVTCAManuInfo info;
	memset(&info, 0, sizeof(SDVTCAManuInfo));
	version[0]=0;
	if(length < 5) return; // size too small
	HRESULT res = DVTCASTB_GetStreamGuardManuInfo(&info);
	if(res == DVTCA_OK)
	{
		sprintf(version,"%ld",info.m_dwSCCOSVer);
	}
}

U32 DvtCASManuInfo(SDVTCAManuInfo * pManuInfo)
{
	memset(pManuInfo, 0, sizeof(SDVTCAManuInfo));
	HRESULT res = DVTCASTB_GetStreamGuardManuInfo(pManuInfo);
	return res;	
}

void DvtCAManuName(char *name, int length)
{
	SDVTCAManuInfo info;
	memset(&info, 0, sizeof(SDVTCAManuInfo));
	name[0]=0;
	if(length<DVTCA_MAXLEN_MANUFACTURERNAME) return;
	HRESULT res = DVTCASTB_GetStreamGuardManuInfo(&info);
	if(res == DVTCA_OK)
	{
		strcpy(name,info.m_szSCCASManuName);
	}
}

bool DvtCAIsPinLocked(bool * pbLocked)
{
	HRESULT res = DVTCASTB_IsPinLocked(pbLocked);
	if(res == DVTCA_OK) 
		return true;
	return false;  
}

U32 DvtCAGetAreaInfo(SDVTCAAreaInfo * psAreaInfo)
{
	return DVTCASTB_GetAreaInfo(psAreaInfo);
}

U32 DvtCAGetMotherInfo(DWORD *pdwMotherCardID)
{
	HRESULT hRes;
	dxreport("enter DVTCASTB_GetMotherInfo\n");
#if defined(JS_USE_SHUMA_CALIB_TEST) || defined(JS_USE_SHUMA_CALIB_HUANGGANG) || defined(JS_USE_SHUMA_CALIB_NANPING)
	dxreport("not yongan class mother card info\n");
	hRes = DVTCASTB_GetMotherInfo(pdwMotherCardID);
#else
	//#if defined(JS_USE_SHUMA_CALIB_YONGAN) || defined(JS_USE_SHUMA_CALIB_NINGDE), 后续均如此
	SDVTCACorresInfo info;
	dxreport("yongan class mother card info\n");
	hRes = DVTCASTB_GetMotherInfo(&info);
	*pdwMotherCardID = info.m_dwMotherCardID;
#endif
	dxreport("leave DVTCASTB_GetMotherInfo, hRes:%d, dwMotherCardID:%d\n", hRes, *pdwMotherCardID);
	return hRes;
}


U32 DvtCAGetViewedIpps(U8 * count, SDVTCAViewedIpp * pIpps)
{
	return DVTCASTB_GetViewedIpps(count, pIpps);
}


U32 DvtCASetCorrespondInfo(U8 len, U8 * data)
{
	return DVTCASTB_SetCorrespondInfo(len, data);
}

U32 DvtCAGetCorrespondInfo(U8 *len, U8 * data)
{
	HRESULT hRes = DVTCASTB_GetCorrespondInfo(len, data);
	dxreport("DVTCASTB_GetCorrespondInfo return: %d\n", hRes);
	return hRes;
}


U32 DvtCAMotherCardPairOver()
{
	gStbCa.SonMotherThread_.stop();
	bool ret = true;
	if(gStbCa.caThread_.check_stop())
		ret = gStbCa.caThread_.start(CAWorkProc,&gStbCa.caThread_);
	//成功返回0
	return ret ? 0 : 1;
}

//机卡对应功能,数码没有提供查询接口
CDCA_U16 CDCASTB_IsPaired( CDCA_U8* pbyNum, CDCA_U8* pbySTBID_List )
{
	return CDCA_RC_UNKNOWN;
}

CDCA_U16 CDCASTB_GetRating( CDCA_U8* pbyRating )
{
	HRESULT res = DVTCASTB_GetRating(pbyRating);
	if(res == DVTCA_OK)
		return CDCA_RC_OK;

	return CDCA_RC_UNKNOWN;
}


CDCA_U16 CDCASTB_SetRating( const CDCA_U8* pbyPin, CDCA_U8 byRating)
{
	return DvtCASetRating((const char *)pbyPin, byRating);
}

U32 DvtCASetRating( const char * pbyPin, U8 byRating )
{
	HRESULT hRes;
	SDVTCAPin pinCode;
	pinCode.m_byLen = 8;
	memcpy(pinCode.m_byszPin, pbyPin,8);
	hRes = DVT_VerfiryPin(&pinCode);
	if(hRes != DVTCA_OK) return hRes;
	
	hRes = DVTCASTB_SetRating(byRating);
	if(hRes != DVTCA_OK) return hRes;
	return CDCA_RC_OK;

}
HRESULT DVT_VerfiryPin(SDVTCAPin *pPinCode)
{
	// 验证pin code
	HRESULT hRes = DVTCASTB_VerifyPin(pPinCode);
	switch (hRes)
	{
		case DVTCA_OK:
			dxreport("pin veriry passed!\n");
			break;
		case DVTCAERR_STB_DATA_LEN_ERROR:
			dxreport("pin length error\n");
			break;
		case DVTCAERR_STB_PIN_INVALID:
			dxreport("pin invalid\n");
			break;
		case DVTCAERR_STB_PIN_LOCKED:
			dxreport("pin locked\n");
			break;
		default:
			dxreport("pin other value, hRes:%lu\n",hRes);
			;
	}
	return hRes;
}


CDCA_U16 CDCASTB_GetWorkTime( CDCA_U8* pbyStartHour, 
							CDCA_U8* pbyStartMin, 
							CDCA_U8* pbyStartSec, 
							CDCA_U8* pbyEndHour, 
							CDCA_U8* pbyEndMin, 
							CDCA_U8* pbyEndSec)
{
	HRESULT res = DVTCASTB_GetWorkTime(pbyStartHour, pbyStartMin, pbyEndHour, pbyEndMin);
	if(res == DVTCA_OK)
	{
		*pbyStartSec = 0;
		*pbyEndSec = 0;
		return CDCA_RC_OK;
	}
	return CDCA_RC_UNKNOWN;
}

CDCA_U16 CDCASTB_SetWorkTime( const CDCA_U8* pbyPin,
		CDCA_U8        byStartHour,
		CDCA_U8        byStartMin,
		CDCA_U8        byStartSec,
		CDCA_U8        byEndHour,
		CDCA_U8        byEndMin,
		CDCA_U8		   byEndSec)
{
	return DvtCASetWorkTime((const char*)pbyPin, byStartHour, byStartMin, byStartSec,
							byEndHour, byEndMin, byEndSec);
}

U32 DvtCASetWorkTime( const char * pbyPin,
		U8        byStartHour,
		U8        byStartMin,
		U8        byStartSec,
		U8        byEndHour,
		U8        byEndMin,
		U8		   byEndSec)
{
	HRESULT hRes;
	SDVTCAPin pinCode;
	pinCode.m_byLen = 8;
	memcpy(pinCode.m_byszPin, pbyPin,8);
	hRes = DVT_VerfiryPin(&pinCode);
	//if(hRes != DVTCA_OK) return CDCA_RC_UNKNOWN;
	if(hRes != DVTCA_OK) return hRes;
	
	hRes= DVTCASTB_SetWorkTime(byStartHour, byStartMin, byEndHour, byEndMin);

	return CDCA_RC_OK;
}

#include <time.h>
// 输入: dvtTime -- 1970 年开始的秒数
// 输出: 2000年开始的天数
static U32 DVTTime2CDCATime(U32 dvtTime)
{
	//数码时间为time_t格式,1970-1-1 0:0:0
	time_t tt = (time_t)dvtTime;
	tm * pTime = localtime(&tt);	
	//计算出天数
	U32 days = 0;
	U32 year = pTime->tm_year + 1900;
	U32 mon = pTime->tm_mon;
	U32 mday = pTime->tm_mday;
	//从2000年开始算天数,如果在2000年前,天数忽略,,,,,当前年的天数由月日取得
	for(U32 i = 2000; i < year; i++)
	{
		if((i%4 == 0 && i%100 != 0) || i%400 == 0)
			days += 366;
		else
			days += 365;
	}

	if(year >= 2000)
	{
		//计算当前年月份的天数
		days += (mon - 1) * 30;
		if(mon < 8)
			days += mon / 2;
		else
			days += (mon + 1) / 2;

		if(mon > 2)
		{
			if((year%4 == 0 && year%100 != 0) || year%400 == 0)
				--days;
			else
				days -= 2;
		}

		//因1月1日应为0,所以当前天不算
		days += mday - 1;
	}

	//永新不需要时分秒
	return days;
	//return ((days << 16) + (pTime->m_hour << 11) + (pTime->m_min << 5) + pTime->m_sec);
}


CDCA_U16 CDCASTB_GetServiceEntitles( CDCA_U16 wTVSID, SCDCAEntitles* pServiceEntitles )
{
	//数码最多250
	CDCA_U8 entitleCount = 250;
	SDVTCAServiceEntitle entitle[250];
	HRESULT res = DVTCASTB_GetServiceEntitles(wTVSID, &entitleCount, entitle);
	if(res == DVTCA_OK)
	{
		pServiceEntitles->m_wProductCount = entitleCount;
		for(int i = 0; i < entitleCount; i++)
		{
			pServiceEntitles->m_Entitles[i].m_dwProductID = entitle[i].m_wProductID;
			pServiceEntitles->m_Entitles[i].m_tBeginDate= DVTTime2CDCATime(entitle[i].m_tStartTime);
			pServiceEntitles->m_Entitles[i].m_tExpireDate= DVTTime2CDCATime(entitle[i].m_tEndTime);
			pServiceEntitles->m_Entitles[i].m_bCanTape= entitle[i].m_bTapingFlag;
			dxreport("dvt: productID:%d, startTime:%d, endTime:%d, tapflag:%d\n",
				entitle[i].m_wProductID,entitle[i].m_tStartTime,entitle[i].m_tEndTime,entitle[i].m_bTapingFlag);
		}
		return CDCA_RC_OK;
	}
	else if(res == (HRESULT)DVTCAERR_STB_TVS_NOT_FOUND)
	{
		return CDCA_RC_DATA_NOT_FIND;
	}
	return CDCA_RC_UNKNOWN;
}

//数码不支持
CDCA_U16 CDCASTB_GetACList( CDCA_U16 wTVSID, CDCA_U32* pACArray )
{
	return CDCA_RC_UNKNOWN;
}

//数码不支持
void CDCASTB_Close( void )
{
}

