//本文件定义CA模块实现提供给STB调用的接口
#include "DVTCAS_STBDataPublic.h"
/*-----------------------------------以下接口是CAS提供给STB调用----------------------------------*/
bool DVTCASTB_Init(void);
bool DVTCASTB_CASIDVerify(WORD wCaSystemID);
void DVTCASTB_SCInsert(BYTE byReaderNo);
void DVTCASTB_SCRemove(void);
void DVTCASTB_SetCurEcmInfo(const SDVTCAServiceInfo * pServiceInfo);
void DVTCASTB_SetEmmPid(WORD wEmmPid);
void DVTCASTB_StreamGuardDataGot(BYTE byReqID, bool bSuccess, WORD wPID, WORD wLen, const BYTE * byszReceiveData);
HRESULT DVTCASTB_VerifyPin(const SDVTCAPin * pPIN);
HRESULT DVTCASTB_IsPinLocked(bool * pbLocked);
HRESULT DVTCASTB_IsPinPass(bool * pbPass);
HRESULT DVTCASTB_ChangePin(const SDVTCAPin * pNewPin);
HRESULT DVTCASTB_GetRating(BYTE * pbyRating);
HRESULT DVTCASTB_SetRating(BYTE byRating);
HRESULT DVTCASTB_GetWorkTime(BYTE * pbyStartHour, BYTE * pbyStartMinute, BYTE * pbyEndHour, BYTE * pbyEndMinute);
HRESULT DVTCASTB_SetWorkTime(BYTE byStartHour, BYTE byStartMinute, BYTE byEndHour, BYTE byEndMinute);
HRESULT DVTCASTB_GetStreamGuardManuInfo(SDVTCAManuInfo * pManu);
HRESULT DVTCASTB_GetOperatorInfo(WORD wTVSID, BYTE * pbyCount, SDVTCATvsInfo * pOperatorInfo);
HRESULT DVTCASTB_GetServiceEntitles(WORD wTVSID, BYTE * pEntitleCount, SDVTCAServiceEntitle * psEntitles);
void DVTCASTB_AddDebugMsgSign(bool bDebugMsgSign);
void DVTCASTB_ShowOSDMsgOver(WORD wDuration);
HRESULT DVTCASTB_GetEmailCount(WORD * pEmailCount, WORD * pNewEmailCount);
HRESULT DVTCASTB_GetEmailHead(WORD * pEmailCount, SDVTCAEmailInfo * pEmail);
HRESULT DVTCASTB_GetEmailContent(DWORD tVersion, SDVTCAEmailContent * pEmail);
HRESULT DVTCASTB_EmailRead(DWORD tVersion);
HRESULT DVTCASTB_DelEmail(DWORD tVersion);
HRESULT DVTCASTB_GetCorrespondInfo(BYTE * pLen, BYTE * pData);
HRESULT DVTCASTB_SetCorrespondInfo(BYTE DataLen, const BYTE * pData);
//--------------------------------------------------------ipp begin----------------------------------------------------
HRESULT DVTCASTB_GetBookIpps(BYTE * pbyCount, SDVTCAIpp * pIpps);
HRESULT DVTCASTB_BookIpp(const SDVTCAIpp * pIpp);
HRESULT DVTCASTB_GetViewedIpps(BYTE * pbyCount, SDVTCAViewedIpp * pIpps);
void DVTCASTB_InquireBookIppOver(WORD wEcmPid);
//--------------------------------------------------------ipp end----------------------------------------------------
HRESULT DVTCASTB_GetMoneyInfo(WORD wTVSID, DWORD * pdwAllBalance, DWORD * pdwRemainder);
void DVTCASTB_SwitchChannelOver(BYTE byFlag);
HRESULT DVTCASTB_GetAreaInfo(SDVTCAAreaInfo * psAreaInfo);//weiye:2007.05.24
HRESULT DVTCASTB_GetUserDefData(BYTE byType, DWORD *pdwUserDefData);
void DVTCASTB_ChangeLanguage(BYTE byLanguage);
HRESULT DVTCASTB_GetMotherInfo(DWORD *pdwMotherCardID);
