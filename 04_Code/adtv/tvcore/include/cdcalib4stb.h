#ifndef _CALIB_30_H
#define _CALIB_30_H
#include "cdcas_hdr.h"
/*---------------------------以下接口是CA_LIB提供给STB------------------------*/
#ifndef WIN32
#define __stdcall
#endif
#ifdef  __cplusplus
extern "C" {
#endif
/*------ CA_LIB调度管理 ------*/
/* CA_LIB初始化 */
extern  CDCA_BOOL __stdcall CDCASTB_Init( CDCA_U8 byThreadPrior );
/* 关闭CA_LIB，释放资源 */
extern void __stdcall CDCASTB_Close( void );
/* CDCAS同密判断 */
extern CDCA_BOOL  __stdcall CDCASTB_IsCDCa(CDCA_U16 wCaSystemID);
/*------ Flash管理 ------ */
/* 存储空间的格式化 */
extern void __stdcall CDCASTB_FormatBuffer( void );
/* 屏蔽对存储空间的读写操作 */
extern void __stdcall CDCASTB_RequestMaskBuffer(void);
/* 打开对存储空间的读写操作 */
extern void __stdcall CDCASTB_RequestUpdateBuffer(void);
/*------ TS流管理 ------*/
/* 设置ECM和节目信息 */
extern void __stdcall CDCASTB_SetEcmPid( CDCA_U8 byType,
                               const SCDCASServiceInfo* pServiceInfo );
/* 设置EMM信息 */
extern void  __stdcall CDCASTB_SetEmmPid(CDCA_U16 wEmmPid);
/* 私有数据接收回调 */
extern void __stdcall CDCASTB_PrivateDataGot( CDCA_U8        byReqID,
								  	CDCA_BOOL      bTimeout,
									CDCA_U16       wPid,
									const CDCA_U8* pbyReceiveData,
									CDCA_U16       wLen            );
/*------- 智能卡管理 -------*/
/* 插入智能卡 */
extern CDCA_BOOL __stdcall CDCASTB_SCInsert( void );
/* 拔出智能卡*/
extern void __stdcall CDCASTB_SCRemove( void );
/* 读取智能卡外部卡号 */
extern CDCA_U16 __stdcall CDCASTB_GetCardSN( char* pCardSN );
/* PIN码管理 */
extern CDCA_U16 __stdcall CDCASTB_ChangePin( const CDCA_U8* pbyOldPin,
                                   const CDCA_U8* pbyNewPin);
/* 设置用户观看级别 */
extern CDCA_U16 __stdcall CDCASTB_SetRating( const CDCA_U8* pbyPin,
                                   CDCA_U8 byRating );
/* 查询用户观看级别 */
extern CDCA_U16 __stdcall CDCASTB_GetRating( CDCA_U8* pbyRating );
/* 设置智能卡工作时段 */
extern CDCA_U16 __stdcall CDCASTB_SetWorkTime( const CDCA_U8* pbyPin,
									 CDCA_U8        byStartHour,
									 CDCA_U8        byStartMin,
									 CDCA_U8        byStartSec,
									 CDCA_U8        byEndHour,
									 CDCA_U8        byEndMin,
									 CDCA_U8        byEndSec    );
/* 查询智能卡当前工作时段 */
extern CDCA_U16 __stdcall CDCASTB_GetWorkTime( CDCA_U8* pbyStartHour,
									 CDCA_U8* pbyStartMin,
									 CDCA_U8* pbyStartSec,
									 CDCA_U8* pbyEndHour,
									 CDCA_U8* pbyEndMin,
									 CDCA_U8* pbyEndSec   );
/*------- 基本信息查询 -------*/
/* 查询CA_LIB版本号 */
extern CDCA_U32 __stdcall CDCASTB_GetVer( void );
/* 查询运营商ID列表 */
extern CDCA_U16 __stdcall CDCASTB_GetOperatorIds( CDCA_U16* pwTVSID );
/* 查询运营商信息 */
extern CDCA_U16 __stdcall CDCASTB_GetOperatorInfo( CDCA_U16           wTVSID,
                                         SCDCAOperatorInfo* pOperatorInfo );
/* 查询用户特征 */
extern CDCA_U16 __stdcall CDCASTB_GetACList( CDCA_U16 wTVSID, CDCA_U32* pACArray );
/* 查询钱包ID列表 */
extern CDCA_U16 __stdcall CDCASTB_GetSlotIDs( CDCA_U16 wTVSID, CDCA_U8* pbySlotID );
/* 查询钱包的详细信息 */
extern CDCA_U16 __stdcall CDCASTB_GetSlotInfo( CDCA_U16          wTVSID,
                                     CDCA_U8           bySlotID,
                                     SCDCATVSSlotInfo* pSlotInfo );
/* 查询普通授权节目购买情况 */
extern CDCA_U16 __stdcall CDCASTB_GetServiceEntitles( CDCA_U16       wTVSID,
                                            SCDCAEntitles* pServiceEntitles );
/*-- 高级预览节目购买记录查询(内部测试用) --*/
/*
参数说明：
	wTvsID	：运营商ID
	pCurtainRecs	：高级预览记录指针，要求数组大小为CDCA_MAXNUM_CURTAIN_RECORD。
	pNumber	：输出有效记录数，即从pCurtainRecs[0]开始的个数。
	
返回值：CDCA_RC_OK 	：表示成功
	其他值		：表示失败，具体查看pub_st.h功能调用返回值定义。
	
*/
extern CDCA_U16 __stdcall CDCASTB_GetCurtainRecords( CDCA_U16 wTvsID,
                                SCDCACurtainInfo* pCurtainRecs,
                                CDCA_U8* pNumber);
/*-------- 授权信息管理 --------*/
/* 查询授权ID列表 */
extern CDCA_U16 __stdcall CDCASTB_GetEntitleIDs( CDCA_U16  wTVSID,
                                       CDCA_U32* pdwEntitleIds );
/* 查询反授权确认码 */
extern CDCA_U16 __stdcall CDCASTB_GetDetitleChkNums( CDCA_U16   wTvsID,
                                           CDCA_BOOL* bReadFlag,
                                           CDCA_U32*  pdwDetitleChkNums);
/* 查询反授权信息读取状态 */
extern CDCA_BOOL __stdcall CDCASTB_GetDetitleReaded( CDCA_U16 wTvsID );
/* 删除反授权确认码 */
extern CDCA_BOOL __stdcall CDCASTB_DelDetitleChkNum( CDCA_U16 wTvsID,
                                           CDCA_U32 dwDetitleChkNum );
/*------- 机卡对应 -------*/
/* 查询机卡对应情况 */
extern CDCA_U16 __stdcall CDCASTB_IsPaired( CDCA_U8* pbyNum,
                                  CDCA_U8* pbySTBID_List );
/* 查询机顶盒平台编号 */
extern CDCA_U16 __stdcall CDCASTB_GetPlatformID( void );
/*-------- IPPV应用 -------*/
/* IPPV节目购买 */
extern CDCA_U16 __stdcall CDCASTB_StopIPPVBuyDlg( CDCA_BOOL       bBuyProgram,
                                        CDCA_U16        wEcmPid,
                                        const CDCA_U8*  pbyPinCode,
                                        const SCDCAIPPVPrice* pPrice );
/* IPPV节目购买情况查询 */
extern CDCA_U16 __stdcall CDCASTB_GetIPPVProgram( CDCA_U16       wTvsID,
                                        SCDCAIppvInfo* pIppv,
                                        CDCA_U16*      pwNumber );
/*-------- 邮件管理 --------*/
/* 查询邮件头信息 */
extern CDCA_U16 __stdcall CDCASTB_GetEmailHeads( SCDCAEmailHead* pEmailHead,
                                       CDCA_U8*        pbyCount,
                                       CDCA_U8*        pbyFromIndex );
/* 查询指定邮件的头信息 */
extern CDCA_U16 __stdcall CDCASTB_GetEmailHead( CDCA_U32        dwEmailID,
                                      SCDCAEmailHead* pEmailHead );
/* 查询指定邮件的内容 */
extern CDCA_U16 __stdcall CDCASTB_GetEmailContent( CDCA_U32           dwEmailID,
                                         SCDCAEmailContent* pEmailContent );
/* 删除邮件 */
extern void __stdcall CDCASTB_DelEmail( CDCA_U32 dwEmailID );
/* 查询邮箱使用情况 */
extern CDCA_U16 __stdcall CDCASTB_GetEmailSpaceInfo( CDCA_U8* pbyEmailNum,
                                           CDCA_U8* pbyEmptyNum );
/*-------- 子母卡应用 --------*/
/* 读取子母卡信息 */
extern CDCA_U16 __stdcall CDCASTB_GetOperatorChildStatus( CDCA_U16   wTVSID,
                                                CDCA_U8*   pbyIsChild,
                                                CDCA_U16*   pwDelayTime,
                                                CDCA_TIME* pLastFeedTime,
                                                char*      pParentCardSN,
                                                CDCA_BOOL *pbIsCanFeed );
/* 读取母卡喂养数据 */
extern CDCA_U16 __stdcall CDCASTB_ReadFeedDataFromParent( CDCA_U16 wTVSID,
                                                CDCA_U8* pbyFeedData,
                                                CDCA_U8* pbyLen     );
/* 喂养数据写入子卡 */
extern CDCA_U16 __stdcall CDCASTB_WriteFeedDataToChild( CDCA_U16       wTVSID,
                                              const CDCA_U8* pbyFeedData,
                                              CDCA_U8        byLen    );
/*-------- 显示界面管理 --------*/
/* 刷新界面 */
extern void __stdcall CDCASTB_RefreshInterface( void );
/*-------- 双向模块接口 -------*/
    
#ifdef  __cplusplus
}
#endif
#endif
/*EOF*/
