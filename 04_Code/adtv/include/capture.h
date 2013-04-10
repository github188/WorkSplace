#ifndef _STB_PC_CAPTURE_H
#define _STB_PC_CAPTURE_H

#include "capture_def.h"
#include "tvcomm.h"

#if defined(WIN32)
	#ifdef STBCA_EXPORTS
		#define STBCA_API __declspec(dllexport)
	#else
		#define STBCA_API __declspec(dllimport)
	#endif 
#else
	#define STBCA_API 
#endif 


#ifdef __cplusplus
extern "C"
{
#endif

STBCA_API bool Ca_Init();
STBCA_API void Ca_Start();
STBCA_API void Ca_Stop();
STBCA_API void Ca_Uninit();

STBCA_API int  Ca_SetDescrambling (Descrambling  *descramb,  int descrambCount,U16 wEmmPid);
//STBCA_API BOOL Ca_ProcessMsg( LPVOID lpInBuffer,DWORD dwInBufferSize,LPVOID lpOutBuffer,DWORD dwOutBufferSize,DWORD &dwBytesReturned);
STBCA_API int  Ca_SetCaMsgCallBack(TVNOTIFY pCallBack);

// 获取STBCA的版本
STBCA_API void stbca_version(char *version);

// 获取当前的CAS类型.
STBCA_API CasType stbca_cas_type();

// 获取STBID号.
// STBCA_API char * stbca_get_stbid(char *stdid);
STBCA_API void stbca_get_stbid(OUT WORD* pwPlatformID, OUT ULONG* pdwUniqueID);

//==============邮件管理接口===================
// 进入邮件管理调用的函数
STBCA_API void stbca_enter_mail_manager();
// 离开邮件管理调用的函数
STBCA_API void stbca_leave_mail_manager();
// 获取邮件内容.
STBCA_API bool stbca_get_email_content( EmailId email_id, EmailContent *pEmailContent);
//注：使用以上两个函数提高邮件管理效率
// 获取邮箱中的邮件数和邮箱还可以容纳的邮件数.
STBCA_API U32 stbca_get_email_space( U8 &email_num, U8 & empty_num );
// 获取指定邮件的邮件头.
STBCA_API bool stbca_get_email_head( EmailId email_id, EmailHead & email_head );
// 获取所有邮件头.
STBCA_API bool stbca_get_email_heads(EmailHead* pEmailHead,U8 *pCount,U8 *pFromIndex);
// 删除一封邮件.
STBCA_API void stbca_delete_email( EmailId email_id );
// 删除所有的邮件
STBCA_API void stbca_delete_emails();

// ================智能卡管理====================================
// GetCardID获取智能卡卡号.
//

//获取智能卡状态
STBCA_API int stbca_get_CardStatus(U8 * status);

STBCA_API bool stbca_scard_id( std::string &scard_id );  // deprecated
STBCA_API int stbca_GetCardID( char *pCardID );
// 修改Pin码.
STBCA_API U32  stbca_change_pin_code( U8 *old_code, U8 *new_code );
// 获取观看级别.
STBCA_API U8 	stbca_get_watch_rating();
// 设置观看级别.
STBCA_API U32 	stbca_set_watch_rating( U8 *pin_code, U8 rate );
// 获取观看时段.
STBCA_API U16 	stbca_get_watch_time1( U8 & start_hour, U8 & end_hour );
// 获取观看时段.
STBCA_API U16 	stbca_get_watch_time2( U8 & start_hour, U8 & start_min, U8 & start_sec,U8 & end_hour, U8 & end_min, U8 & end_sec );
// 设置观看时段.
STBCA_API U32	stbca_set_watch_time1( U8 *pin_code, U8 start_hour, U8 end_hour );
// 设置观看时段.
STBCA_API U32	stbca_set_watch_time2( U8 *pin_code, U8 start_hour, U8 start_min,U8 start_sec, U8 end_hour, U8 end_min, U8 end_sec );

//=======================================================================
// 判断Pin码是否锁定.
STBCA_API bool stbca_pincode_locked();

#ifdef JS_USE_SHUMACA_SEARCH
#include "DVTCAS_STBDataPublic.h"
STBCA_API U32 DvtCAChangePin(const char * pbyOldPin, const char* pbyNewPin);
STBCA_API U32 DvtCASetWorkTime( const char * pbyPin,
		U8        byStartHour,
		U8        byStartMin,
		U8        byStartSec,
		U8        byEndHour,
		U8        byEndMin,
		U8		   byEndSec);
STBCA_API U32 DvtCASetRating( const char * pbyPin, U8 byRating );
STBCA_API U32 DvtCAGetEmailSpaceInfo( U8* pbyEmailNum, U8* pbyEmptyNum );
STBCA_API void DvtCACosVersion(char *version, int length);
STBCA_API void DvtCAManuName(char *name, int length);
STBCA_API U32 DvtCASManuInfo(SDVTCAManuInfo * pManuInfo); 
STBCA_API U32 DvtCAGetOperatorInfo(U8 * pCount, SDVTCATvsInfo * pInfo);
STBCA_API U32 DvtCAGetServiceEntitles(U32 sid, U8 * pCount, SDVTCAServiceEntitle * pEntitles);
STBCA_API U32 DvtCAGetEmailHeads(U16 * pCount, SDVTCAEmailInfo * pEmails);
STBCA_API U32 DvtCAGetEmailContent(U32 email_id, SDVTCAEmailContent * pContent);
STBCA_API U32 DvtCAEmailRead(U32 email_id);
STBCA_API U32 DvtCAShowOSDOver(U32 duration);
STBCA_API U32 DvtCAGetPurseInfo(U32 operid, U32 * balance, U32 * remainder);
STBCA_API U32 DvtCAGetIpps(U8 * count, SDVTCAIpp * pIpps);
STBCA_API U32 DvtCABookIpp(SDVTCAIpp * pIpp);
STBCA_API U32 DvtCAGetViewedIpps(U8 * count, SDVTCAViewedIpp * pIpps);
STBCA_API U32 DvtCAAddDebugMsgSign(bool sign);
STBCA_API U32 DvtCAVerifyPin(U8 * pin);
STBCA_API U32 DvtCADelEmail(U32 id);
STBCA_API U32 DvtCAInquireBookIppOver(U32 ecm);
STBCA_API U32 DvtCAConrr(U32 ecm);
STBCA_API U32 DvtCASetCorrespondInfo(U8 len, U8 * data);
STBCA_API U32 DvtCAGetCorrespondInfo(U8 *len, U8 * data);
STBCA_API U32 DvtCAMotherCardPairOver();
STBCA_API U32 DvtCAShowMotherCardPair(U8 type);

STBCA_API bool DvtCAIsPinLocked(bool * pbLocked);
STBCA_API U32 DvtCAGetAreaInfo(SDVTCAAreaInfo * psAreaInfo);
STBCA_API U32 DvtCAGetMotherInfo(DWORD *pdwMotherCardID);

#endif


// 机卡配对
STBCA_API U16  stbca_set_paired( U8 *pin_code );
// 判断机卡是否已经配对
STBCA_API U16  stbca_is_paired1();
// 判断机卡是否已经配对    
STBCA_API U16  stbca_is_paired2( U8* stb_num, U8* stb_id_list );

//=======================================================================
// 获取运营商ID序列
STBCA_API U16 stbca_get_operator_ids( std::vector<OperatorId>& ids );
//获取运营商信息.
STBCA_API U16 stbca_get_operator_info( OperatorId id, OperatorInfo & info );
// 获取运营商的特征值.
STBCA_API U16 stbca_get_operator_acs( OperatorId id, std::vector<U32>& acs);
// 获取授权列表
STBCA_API U16 stbca_get_service_entitles( OperatorId id,std::vector<Entitle> & entitles );
// 获取钱包ID列表
STBCA_API U16 stbca_get_purse_ids( OperatorId id, std::vector<PurseId>& purse_ids );
// 获取钱包信息.
STBCA_API U16 stbca_get_purse_info( OperatorId id, PurseId purse_id,PurseInfo & info );

#ifdef __cplusplus
}
#endif

#endif
