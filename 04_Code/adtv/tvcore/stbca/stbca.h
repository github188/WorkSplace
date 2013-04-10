#ifndef _STBCA_30_H
#define _STBCA_30_H
#include "cdcas_hdr.h"
/*------------------------���½ӿ���STB�ṩ��CA_LIB---------------------------*/
#ifndef WIN32
#define __stdcall
#endif
#ifdef  __cplusplus
extern "C" {
#endif
/*---------------------------------------- �̹߳���----------------------------------------*/
/* ע������ */
 HANDLE __stdcall CDSTBCA_RegisterTask( const char* szName,
                                       CDCA_U8     byPriority,
                                       void*       pTaskFun,
                                       void*       pParam,
                                       CDCA_U16    wStackSize  );
/* �̹߳��� */
 void __stdcall CDSTBCA_Sleep(CDCA_U16 wMilliSeconds);
/*---------------------------------------- �ź������� --------*/
/* ��ʼ���ź��� */
 void __stdcall CDSTBCA_SemaphoreInit( CDCA_Semaphore* pSemaphore,
                                   CDCA_BOOL       bInitVal );
/* �ź��������ź� */
 void __stdcall CDSTBCA_SemaphoreSignal( CDCA_Semaphore* pSemaphore );
/* �ź�����ȡ�ź� */
 void __stdcall CDSTBCA_SemaphoreWait( CDCA_Semaphore* pSemaphore );
/*---------------------------------------- �ڴ���� --------*/
/* �����ڴ� */
 void * __stdcall CDSTBCA_Malloc( CDCA_U32 byBufSize );
/* �ͷ��ڴ� */
 void  __stdcall CDSTBCA_Free( void* pBuf );
/* �ڴ渳ֵ */
 void  __stdcall CDSTBCA_Memset( void*    pDestBuf,
                             CDCA_U8  c,
                             CDCA_U32 wSize );
/* �ڴ渴�� */
 void  __stdcall CDSTBCA_Memcpy( void*       pDestBuf,
                             const void* pSrcBuf,
                             CDCA_U32    wSize );
/*---------------------------------------- �洢�ռ䣨Flash������ ---------*/
/* ��ȡ�洢�ռ� */
 void __stdcall CDSTBCA_ReadBuffer( CDCA_U8   byBlockID,
                                CDCA_U8*  pbyData,
                                CDCA_U32* pdwLen );
/* д��洢�ռ� */
 void __stdcall CDSTBCA_WriteBuffer( CDCA_U8        byBlockID,
                                 const CDCA_U8* pbyData,
                                 CDCA_U32       dwLen );
/*---------------------------------------- TS������ --------*/
/* ����˽�����ݹ����� */
 CDCA_BOOL __stdcall CDSTBCA_SetPrivateDataFilter( CDCA_U8        byReqID,  
											   const CDCA_U8* pbyFilter,  
											   const CDCA_U8* pbyMask, 
											   CDCA_U8        byLen, 
											   CDCA_U16       wPid, 
											   CDCA_U8        byWaitSeconds );
/* �ͷ�˽�����ݹ����� */
 void __stdcall CDSTBCA_ReleasePrivateDataFilter( CDCA_U8  byReqID,
                                              CDCA_U16 wPid );
/* ����CW�������� */
 void __stdcall CDSTBCA_ScrSetCW( CDCA_U16       wEcmPID,  
							  const CDCA_U8* pbyOddKey,  
							  const CDCA_U8* pbyEvenKey, 
							  CDCA_U8        byKeyLen, 
							  CDCA_BOOL      bTapingEnabled );
/*---------------------------------------- ���ܿ����� ---------*/
/* ���ܿ���λ */
 CDCA_BOOL __stdcall CDSTBCA_SCReset( CDCA_U8* pbyATR, CDCA_U8* pbyLen );
/* ���ܿ�ͨѶ */
 CDCA_BOOL __stdcall CDSTBCA_SCPBRun( const CDCA_U8* pbyCommand, 
								  CDCA_U16       wCommandLen,  
								  CDCA_U8*       pbyReply,  
								  CDCA_U16*      pwReplyLen  );
/*-------- ��Ȩ��Ϣ���� -------*/
/* ֪ͨ��Ȩ�仯 */
 void __stdcall CDSTBCA_EntitleChanged( CDCA_U16 wTvsID );
/* ����Ȩȷ����֪ͨ */
 void __stdcall CDSTBCA_DetitleReceived( CDCA_U8 bstatus );
/*-------- ��ȫ���� --------*/
/* ��ȡ������Ψһ��� */
 void __stdcall CDSTBCA_GetSTBID( CDCA_U16* pwPlatformID,
                              CDCA_U32* pdwUniqueID);
/* ��ȫоƬ�ӿ� */
 CDCA_U16 __stdcall CDSTBCA_SCFunction( CDCA_U8* pData);
/*-------- IPPVӦ�� -------*/
/* IPPV��Ŀ֪ͨ */
 void __stdcall CDSTBCA_StartIppvBuyDlg( CDCA_U8                 byMessageType,
                                     CDCA_U16                wEcmPid,
                                     const SCDCAIppvBuyInfo* pIppvProgram  );
/* ����IPPV�Ի��� */
 void __stdcall CDSTBCA_HideIPPVDlg(CDCA_U16 wEcmPid);
/*------- �ʼ�/OSD��ʾ���� -------*/
/* �ʼ�֪ͨ */
 void __stdcall CDSTBCA_EmailNotifyIcon( CDCA_U8 byShow, CDCA_U32 dwEmailID );
/* ��ʾOSD��Ϣ */
 void __stdcall CDSTBCA_ShowOSDMessage( CDCA_U8     byStyle,
                                    const char* szMessage );
/* ����OSD��Ϣ*/
 void __stdcall CDSTBCA_HideOSDMessage( CDCA_U8 byStyle );
/*-------- ��ĸ��Ӧ�� --------*/
/* ������ʾ��ȡι�����ݽ�� */
 void  __stdcall CDSTBCA_RequestFeeding( CDCA_BOOL bReadStatus );
/*-------- ǿ���л�Ƶ�� --------*/
/* Ƶ������ */
 void __stdcall CDSTBCA_LockService( const SCDCALockService* pLockService );
/* ���Ƶ������ */
 void __stdcall CDSTBCA_UNLockService( void );
/*-------- ��ʾ������� --------*/
/* ���������տ���Ŀ����ʾ */
/*wEcmPID==0��ʾ��wEcmPID�޹ص���Ϣ���Ҳ��ܱ�������Ϣ����*/
 void __stdcall CDSTBCA_ShowBuyMessage( CDCA_U16 wEcmPID,
                                    CDCA_U8  byMessageType );
/* ָ����ʾ */
 void __stdcall CDSTBCA_ShowFingerMessage( CDCA_U16 wEcmPID,
                                       CDCA_U32 dwCardID );
/* �߼�Ԥ����ʾ*/
/* ֪ͨ��������ʾ��رո߼�Ԥ��*/
 void __stdcall CDSTBCA_ShowCurtainNotify( CDCA_U16 wEcmPID,
                                    CDCA_U16  wCurtainCode);
/* ������ʾ */
 void __stdcall CDSTBCA_ShowProgressStrip( CDCA_U8 byProgress,
                                       CDCA_U8 byMark );
// void __stdcall CDSTBCA_ShowCurtainNotify(CDCA_U16  ecmPid,CDCA_U16 uCurtainCode);
/*--------- ������֪ͨ --------*/
/* ������֪ͨ */
 void  __stdcall CDSTBCA_ActionRequest( CDCA_U16 wTVSID,
                                    CDCA_U8  byActionType );
/*---------------------------------------- ���� --------*/
/* ��ȡ�ַ������� */
 CDCA_U16 __stdcall CDSTBCA_Strlen(const char* pString );
/* ������Ϣ��� */
 void __stdcall CDSTBCA_Printf(CDCA_U8 byLevel, const char* szMesssage );
#ifdef  __cplusplus
}
#endif
#endif
/*EOF*/
