#ifndef _STBCA_30_H
#define _STBCA_30_H
#include "cdcas_hdr.h"
/*------------------------以下接口是STB提供给CA_LIB---------------------------*/
#ifndef WIN32
#define __stdcall
#endif
#ifdef  __cplusplus
extern "C" {
#endif
/*---------------------------------------- 线程管理----------------------------------------*/
/* 注册任务 */
 HANDLE __stdcall CDSTBCA_RegisterTask( const char* szName,
                                       CDCA_U8     byPriority,
                                       void*       pTaskFun,
                                       void*       pParam,
                                       CDCA_U16    wStackSize  );
/* 线程挂起 */
 void __stdcall CDSTBCA_Sleep(CDCA_U16 wMilliSeconds);
/*---------------------------------------- 信号量管理 --------*/
/* 初始化信号量 */
 void __stdcall CDSTBCA_SemaphoreInit( CDCA_Semaphore* pSemaphore,
                                   CDCA_BOOL       bInitVal );
/* 信号量给予信号 */
 void __stdcall CDSTBCA_SemaphoreSignal( CDCA_Semaphore* pSemaphore );
/* 信号量获取信号 */
 void __stdcall CDSTBCA_SemaphoreWait( CDCA_Semaphore* pSemaphore );
/*---------------------------------------- 内存管理 --------*/
/* 分配内存 */
 void * __stdcall CDSTBCA_Malloc( CDCA_U32 byBufSize );
/* 释放内存 */
 void  __stdcall CDSTBCA_Free( void* pBuf );
/* 内存赋值 */
 void  __stdcall CDSTBCA_Memset( void*    pDestBuf,
                             CDCA_U8  c,
                             CDCA_U32 wSize );
/* 内存复制 */
 void  __stdcall CDSTBCA_Memcpy( void*       pDestBuf,
                             const void* pSrcBuf,
                             CDCA_U32    wSize );
/*---------------------------------------- 存储空间（Flash）管理 ---------*/
/* 读取存储空间 */
 void __stdcall CDSTBCA_ReadBuffer( CDCA_U8   byBlockID,
                                CDCA_U8*  pbyData,
                                CDCA_U32* pdwLen );
/* 写入存储空间 */
 void __stdcall CDSTBCA_WriteBuffer( CDCA_U8        byBlockID,
                                 const CDCA_U8* pbyData,
                                 CDCA_U32       dwLen );
/*---------------------------------------- TS流管理 --------*/
/* 设置私有数据过滤器 */
 CDCA_BOOL __stdcall CDSTBCA_SetPrivateDataFilter( CDCA_U8        byReqID,  
											   const CDCA_U8* pbyFilter,  
											   const CDCA_U8* pbyMask, 
											   CDCA_U8        byLen, 
											   CDCA_U16       wPid, 
											   CDCA_U8        byWaitSeconds );
/* 释放私有数据过滤器 */
 void __stdcall CDSTBCA_ReleasePrivateDataFilter( CDCA_U8  byReqID,
                                              CDCA_U16 wPid );
/* 设置CW给解扰器 */
 void __stdcall CDSTBCA_ScrSetCW( CDCA_U16       wEcmPID,  
							  const CDCA_U8* pbyOddKey,  
							  const CDCA_U8* pbyEvenKey, 
							  CDCA_U8        byKeyLen, 
							  CDCA_BOOL      bTapingEnabled );
/*---------------------------------------- 智能卡管理 ---------*/
/* 智能卡复位 */
 CDCA_BOOL __stdcall CDSTBCA_SCReset( CDCA_U8* pbyATR, CDCA_U8* pbyLen );
/* 智能卡通讯 */
 CDCA_BOOL __stdcall CDSTBCA_SCPBRun( const CDCA_U8* pbyCommand, 
								  CDCA_U16       wCommandLen,  
								  CDCA_U8*       pbyReply,  
								  CDCA_U16*      pwReplyLen  );
/*-------- 授权信息管理 -------*/
/* 通知授权变化 */
 void __stdcall CDSTBCA_EntitleChanged( CDCA_U16 wTvsID );
/* 反授权确认码通知 */
 void __stdcall CDSTBCA_DetitleReceived( CDCA_U8 bstatus );
/*-------- 安全控制 --------*/
/* 读取机顶盒唯一编号 */
 void __stdcall CDSTBCA_GetSTBID( CDCA_U16* pwPlatformID,
                              CDCA_U32* pdwUniqueID);
/* 安全芯片接口 */
 CDCA_U16 __stdcall CDSTBCA_SCFunction( CDCA_U8* pData);
/*-------- IPPV应用 -------*/
/* IPPV节目通知 */
 void __stdcall CDSTBCA_StartIppvBuyDlg( CDCA_U8                 byMessageType,
                                     CDCA_U16                wEcmPid,
                                     const SCDCAIppvBuyInfo* pIppvProgram  );
/* 隐藏IPPV对话框 */
 void __stdcall CDSTBCA_HideIPPVDlg(CDCA_U16 wEcmPid);
/*------- 邮件/OSD显示管理 -------*/
/* 邮件通知 */
 void __stdcall CDSTBCA_EmailNotifyIcon( CDCA_U8 byShow, CDCA_U32 dwEmailID );
/* 显示OSD信息 */
 void __stdcall CDSTBCA_ShowOSDMessage( CDCA_U8     byStyle,
                                    const char* szMessage );
/* 隐藏OSD信息*/
 void __stdcall CDSTBCA_HideOSDMessage( CDCA_U8 byStyle );
/*-------- 子母卡应用 --------*/
/* 请求提示读取喂养数据结果 */
 void  __stdcall CDSTBCA_RequestFeeding( CDCA_BOOL bReadStatus );
/*-------- 强制切换频道 --------*/
/* 频道锁定 */
 void __stdcall CDSTBCA_LockService( const SCDCALockService* pLockService );
/* 解除频道锁定 */
 void __stdcall CDSTBCA_UNLockService( void );
/*-------- 显示界面管理 --------*/
/* 不能正常收看节目的提示 */
/*wEcmPID==0表示与wEcmPID无关的消息，且不能被其他消息覆盖*/
 void __stdcall CDSTBCA_ShowBuyMessage( CDCA_U16 wEcmPID,
                                    CDCA_U8  byMessageType );
/* 指纹显示 */
 void __stdcall CDSTBCA_ShowFingerMessage( CDCA_U16 wEcmPID,
                                       CDCA_U32 dwCardID );
/* 高级预览显示*/
/* 通知机顶盒显示或关闭高级预览*/
 void __stdcall CDSTBCA_ShowCurtainNotify( CDCA_U16 wEcmPID,
                                    CDCA_U16  wCurtainCode);
/* 进度显示 */
 void __stdcall CDSTBCA_ShowProgressStrip( CDCA_U8 byProgress,
                                       CDCA_U8 byMark );
// void __stdcall CDSTBCA_ShowCurtainNotify(CDCA_U16  ecmPid,CDCA_U16 uCurtainCode);
/*--------- 机顶盒通知 --------*/
/* 机顶盒通知 */
 void  __stdcall CDSTBCA_ActionRequest( CDCA_U16 wTVSID,
                                    CDCA_U8  byActionType );
/*---------------------------------------- 其它 --------*/
/* 获取字符串长度 */
 CDCA_U16 __stdcall CDSTBCA_Strlen(const char* pString );
/* 调试信息输出 */
 void __stdcall CDSTBCA_Printf(CDCA_U8 byLevel, const char* szMesssage );
#ifdef  __cplusplus
}
#endif
#endif
/*EOF*/
