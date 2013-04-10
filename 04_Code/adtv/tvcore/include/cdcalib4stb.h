#ifndef _CALIB_30_H
#define _CALIB_30_H
#include "cdcas_hdr.h"
/*---------------------------���½ӿ���CA_LIB�ṩ��STB------------------------*/
#ifndef WIN32
#define __stdcall
#endif
#ifdef  __cplusplus
extern "C" {
#endif
/*------ CA_LIB���ȹ��� ------*/
/* CA_LIB��ʼ�� */
extern  CDCA_BOOL __stdcall CDCASTB_Init( CDCA_U8 byThreadPrior );
/* �ر�CA_LIB���ͷ���Դ */
extern void __stdcall CDCASTB_Close( void );
/* CDCASͬ���ж� */
extern CDCA_BOOL  __stdcall CDCASTB_IsCDCa(CDCA_U16 wCaSystemID);
/*------ Flash���� ------ */
/* �洢�ռ�ĸ�ʽ�� */
extern void __stdcall CDCASTB_FormatBuffer( void );
/* ���ζԴ洢�ռ�Ķ�д���� */
extern void __stdcall CDCASTB_RequestMaskBuffer(void);
/* �򿪶Դ洢�ռ�Ķ�д���� */
extern void __stdcall CDCASTB_RequestUpdateBuffer(void);
/*------ TS������ ------*/
/* ����ECM�ͽ�Ŀ��Ϣ */
extern void __stdcall CDCASTB_SetEcmPid( CDCA_U8 byType,
                               const SCDCASServiceInfo* pServiceInfo );
/* ����EMM��Ϣ */
extern void  __stdcall CDCASTB_SetEmmPid(CDCA_U16 wEmmPid);
/* ˽�����ݽ��ջص� */
extern void __stdcall CDCASTB_PrivateDataGot( CDCA_U8        byReqID,
								  	CDCA_BOOL      bTimeout,
									CDCA_U16       wPid,
									const CDCA_U8* pbyReceiveData,
									CDCA_U16       wLen            );
/*------- ���ܿ����� -------*/
/* �������ܿ� */
extern CDCA_BOOL __stdcall CDCASTB_SCInsert( void );
/* �γ����ܿ�*/
extern void __stdcall CDCASTB_SCRemove( void );
/* ��ȡ���ܿ��ⲿ���� */
extern CDCA_U16 __stdcall CDCASTB_GetCardSN( char* pCardSN );
/* PIN����� */
extern CDCA_U16 __stdcall CDCASTB_ChangePin( const CDCA_U8* pbyOldPin,
                                   const CDCA_U8* pbyNewPin);
/* �����û��ۿ����� */
extern CDCA_U16 __stdcall CDCASTB_SetRating( const CDCA_U8* pbyPin,
                                   CDCA_U8 byRating );
/* ��ѯ�û��ۿ����� */
extern CDCA_U16 __stdcall CDCASTB_GetRating( CDCA_U8* pbyRating );
/* �������ܿ�����ʱ�� */
extern CDCA_U16 __stdcall CDCASTB_SetWorkTime( const CDCA_U8* pbyPin,
									 CDCA_U8        byStartHour,
									 CDCA_U8        byStartMin,
									 CDCA_U8        byStartSec,
									 CDCA_U8        byEndHour,
									 CDCA_U8        byEndMin,
									 CDCA_U8        byEndSec    );
/* ��ѯ���ܿ���ǰ����ʱ�� */
extern CDCA_U16 __stdcall CDCASTB_GetWorkTime( CDCA_U8* pbyStartHour,
									 CDCA_U8* pbyStartMin,
									 CDCA_U8* pbyStartSec,
									 CDCA_U8* pbyEndHour,
									 CDCA_U8* pbyEndMin,
									 CDCA_U8* pbyEndSec   );
/*------- ������Ϣ��ѯ -------*/
/* ��ѯCA_LIB�汾�� */
extern CDCA_U32 __stdcall CDCASTB_GetVer( void );
/* ��ѯ��Ӫ��ID�б� */
extern CDCA_U16 __stdcall CDCASTB_GetOperatorIds( CDCA_U16* pwTVSID );
/* ��ѯ��Ӫ����Ϣ */
extern CDCA_U16 __stdcall CDCASTB_GetOperatorInfo( CDCA_U16           wTVSID,
                                         SCDCAOperatorInfo* pOperatorInfo );
/* ��ѯ�û����� */
extern CDCA_U16 __stdcall CDCASTB_GetACList( CDCA_U16 wTVSID, CDCA_U32* pACArray );
/* ��ѯǮ��ID�б� */
extern CDCA_U16 __stdcall CDCASTB_GetSlotIDs( CDCA_U16 wTVSID, CDCA_U8* pbySlotID );
/* ��ѯǮ������ϸ��Ϣ */
extern CDCA_U16 __stdcall CDCASTB_GetSlotInfo( CDCA_U16          wTVSID,
                                     CDCA_U8           bySlotID,
                                     SCDCATVSSlotInfo* pSlotInfo );
/* ��ѯ��ͨ��Ȩ��Ŀ������� */
extern CDCA_U16 __stdcall CDCASTB_GetServiceEntitles( CDCA_U16       wTVSID,
                                            SCDCAEntitles* pServiceEntitles );
/*-- �߼�Ԥ����Ŀ�����¼��ѯ(�ڲ�������) --*/
/*
����˵����
	wTvsID	����Ӫ��ID
	pCurtainRecs	���߼�Ԥ����¼ָ�룬Ҫ�������СΪCDCA_MAXNUM_CURTAIN_RECORD��
	pNumber	�������Ч��¼��������pCurtainRecs[0]��ʼ�ĸ�����
	
����ֵ��CDCA_RC_OK 	����ʾ�ɹ�
	����ֵ		����ʾʧ�ܣ�����鿴pub_st.h���ܵ��÷���ֵ���塣
	
*/
extern CDCA_U16 __stdcall CDCASTB_GetCurtainRecords( CDCA_U16 wTvsID,
                                SCDCACurtainInfo* pCurtainRecs,
                                CDCA_U8* pNumber);
/*-------- ��Ȩ��Ϣ���� --------*/
/* ��ѯ��ȨID�б� */
extern CDCA_U16 __stdcall CDCASTB_GetEntitleIDs( CDCA_U16  wTVSID,
                                       CDCA_U32* pdwEntitleIds );
/* ��ѯ����Ȩȷ���� */
extern CDCA_U16 __stdcall CDCASTB_GetDetitleChkNums( CDCA_U16   wTvsID,
                                           CDCA_BOOL* bReadFlag,
                                           CDCA_U32*  pdwDetitleChkNums);
/* ��ѯ����Ȩ��Ϣ��ȡ״̬ */
extern CDCA_BOOL __stdcall CDCASTB_GetDetitleReaded( CDCA_U16 wTvsID );
/* ɾ������Ȩȷ���� */
extern CDCA_BOOL __stdcall CDCASTB_DelDetitleChkNum( CDCA_U16 wTvsID,
                                           CDCA_U32 dwDetitleChkNum );
/*------- ������Ӧ -------*/
/* ��ѯ������Ӧ��� */
extern CDCA_U16 __stdcall CDCASTB_IsPaired( CDCA_U8* pbyNum,
                                  CDCA_U8* pbySTBID_List );
/* ��ѯ������ƽ̨��� */
extern CDCA_U16 __stdcall CDCASTB_GetPlatformID( void );
/*-------- IPPVӦ�� -------*/
/* IPPV��Ŀ���� */
extern CDCA_U16 __stdcall CDCASTB_StopIPPVBuyDlg( CDCA_BOOL       bBuyProgram,
                                        CDCA_U16        wEcmPid,
                                        const CDCA_U8*  pbyPinCode,
                                        const SCDCAIPPVPrice* pPrice );
/* IPPV��Ŀ���������ѯ */
extern CDCA_U16 __stdcall CDCASTB_GetIPPVProgram( CDCA_U16       wTvsID,
                                        SCDCAIppvInfo* pIppv,
                                        CDCA_U16*      pwNumber );
/*-------- �ʼ����� --------*/
/* ��ѯ�ʼ�ͷ��Ϣ */
extern CDCA_U16 __stdcall CDCASTB_GetEmailHeads( SCDCAEmailHead* pEmailHead,
                                       CDCA_U8*        pbyCount,
                                       CDCA_U8*        pbyFromIndex );
/* ��ѯָ���ʼ���ͷ��Ϣ */
extern CDCA_U16 __stdcall CDCASTB_GetEmailHead( CDCA_U32        dwEmailID,
                                      SCDCAEmailHead* pEmailHead );
/* ��ѯָ���ʼ������� */
extern CDCA_U16 __stdcall CDCASTB_GetEmailContent( CDCA_U32           dwEmailID,
                                         SCDCAEmailContent* pEmailContent );
/* ɾ���ʼ� */
extern void __stdcall CDCASTB_DelEmail( CDCA_U32 dwEmailID );
/* ��ѯ����ʹ����� */
extern CDCA_U16 __stdcall CDCASTB_GetEmailSpaceInfo( CDCA_U8* pbyEmailNum,
                                           CDCA_U8* pbyEmptyNum );
/*-------- ��ĸ��Ӧ�� --------*/
/* ��ȡ��ĸ����Ϣ */
extern CDCA_U16 __stdcall CDCASTB_GetOperatorChildStatus( CDCA_U16   wTVSID,
                                                CDCA_U8*   pbyIsChild,
                                                CDCA_U16*   pwDelayTime,
                                                CDCA_TIME* pLastFeedTime,
                                                char*      pParentCardSN,
                                                CDCA_BOOL *pbIsCanFeed );
/* ��ȡĸ��ι������ */
extern CDCA_U16 __stdcall CDCASTB_ReadFeedDataFromParent( CDCA_U16 wTVSID,
                                                CDCA_U8* pbyFeedData,
                                                CDCA_U8* pbyLen     );
/* ι������д���ӿ� */
extern CDCA_U16 __stdcall CDCASTB_WriteFeedDataToChild( CDCA_U16       wTVSID,
                                              const CDCA_U8* pbyFeedData,
                                              CDCA_U8        byLen    );
/*-------- ��ʾ������� --------*/
/* ˢ�½��� */
extern void __stdcall CDCASTB_RefreshInterface( void );
/*-------- ˫��ģ��ӿ� -------*/
    
#ifdef  __cplusplus
}
#endif
#endif
/*EOF*/
