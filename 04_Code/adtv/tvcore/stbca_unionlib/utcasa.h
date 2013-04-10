#ifndef UTCAS_CALIB_APIEX_H
#define UTCAS_CALIB_APIEX_H

#ifdef  __cplusplus
extern "C" {
#endif


/*-------------------------------------������������---------------------------------------*/
typedef unsigned  int  UTCA_U32;
typedef unsigned  short UTCA_U16;
typedef unsigned  char  UTCA_U8;

typedef UTCA_U32  UTCA_Semaphore;

typedef UTCA_U8 UTCA_BOOL;

#define UTCA_TRUE    ((UTCA_BOOL)1)
#define UTCA_FALSE   ((UTCA_BOOL)0)

/*----------------------------------------�궨��--------------------------------------*/

/*--------- FLASH���� -------*/
#define UTCA_FLASH_BLOCK_A        1     /* BLOCK A */
#define UTCA_FLASH_BLOCK_B        2     /* BLOCK B */

/*--------- ���ܿ�������� --------*/
#define UTCA_MAXLEN_SN            16U   /* ���ܿ����кŵĳ��� */
#define UTCA_MAXLEN_PINCODE       6U    /* PIN��ĳ��� */
#define UTCA_MAXLEN_TVSPRIINFO    32U   /* ��Ӫ��˽����Ϣ�ĳ��� */
#define UTCA_MAXNUM_OPERATOR      4U    /* ������Ӫ�̸��� */
#define UTCA_MAXNUM_ACLIST        18U   /* ���ܿ��ڱ����ÿ����Ӫ�̵��û��������� */
#define UTCA_MAXNUM_SLOT          20U   /* ���洢�����Ǯ���� */

#define UTCA_MAXNUM_CURTAIN_RECORD	80U /*�߼�Ԥ����Ŀ��¼��������*/


#define UTCA_MAXNUM_IPPVP         300U  /* ���ܿ��������IPPV��Ŀ�ĸ��� */
#define UTCA_MAXNUM_PRICE         2U    /* ����IPPV�۸���� */

#define UTCA_MAXNUM_ENTITLE       300U  /* ��Ȩ��Ʒ�������� */

/*---------- ������������� ----------*/
#define UTCA_MAXNUM_PROGRAMBYCW   4U    /* һ������������Ľ�Ŀ�� */
#define UTCA_MAXNUM_ECM           8U    /* ͬʱ���մ����ECMPID��������� */

#define UTCA_MAXNUM_DETITLE       5U    /* ÿ����Ӫ���¿ɱ���ķ���Ȩ����� */

/*---------- CAS��ʾ��Ϣ ---------*/
#define UTCA_MESSAGE_CANCEL_TYPE      0x00  /* ȡ����ǰ����ʾ */
#define UTCA_MESSAGE_BADCARD_TYPE     0x01  /* �޷�ʶ�� */
#define UTCA_MESSAGE_EXPICARD_TYPE    0x02  /* ���ܿ����ڣ�������¿� */
#define UTCA_MESSAGE_INSERTCARD_TYPE  0x03  /* ���Ž�Ŀ����������ܿ� */
#define UTCA_MESSAGE_NOOPER_TYPE      0x04  /* ���в����ڽ�Ŀ��Ӫ�� */
#define UTCA_MESSAGE_BLACKOUT_TYPE    0x05  /* �������� */
#define UTCA_MESSAGE_OUTWORKTIME_TYPE 0x06  /* ��ǰʱ�α��趨Ϊ���ܹۿ� */
#define UTCA_MESSAGE_WATCHLEVEL_TYPE  0x07  /* ��Ŀ��������趨�Ĺۿ����� */
#define UTCA_MESSAGE_PAIRING_TYPE     0x08  /* ���ܿ��뱾�����в���Ӧ */
#define UTCA_MESSAGE_NOENTITLE_TYPE   0x09  /* û����Ȩ */
#define UTCA_MESSAGE_DECRYPTFAIL_TYPE 0x0A  /* ��Ŀ����ʧ�� */
#define UTCA_MESSAGE_NOMONEY_TYPE     0x0B  /* ���ڽ��� */
#define UTCA_MESSAGE_ERRREGION_TYPE   0x0C  /* ������ȷ */
#define UTCA_MESSAGE_NEEDFEED_TYPE    0x0D  /* �ӿ���Ҫ��ĸ����Ӧ�������ĸ�� */
#define UTCA_MESSAGE_ERRCARD_TYPE     0x0E  /* ���ܿ�У��ʧ�ܣ�����ϵ��Ӫ�� */
#define UTCA_MESSAGE_UPDATE_TYPE      0x0F  /* ���ܿ������У��벻Ҫ�ο����߹ػ� */
#define UTCA_MESSAGE_LOWCARDVER_TYPE  0x10  /* ���������ܿ� */
#define UTCA_MESSAGE_VIEWLOCK_TYPE    0x11  /* ����Ƶ���л�Ƶ�� */
#define UTCA_MESSAGE_MAXRESTART_TYPE  0x12  /* ���ܿ���ʱ���ߣ���5���Ӻ����¿��� */
#define UTCA_MESSAGE_FREEZE_TYPE      0x13  /* ���ܿ��Ѷ��ᣬ����ϵ��Ӫ�� */
#define UTCA_MESSAGE_CALLBACK_TYPE    0x14  /* ���ܿ�����ͣ����ش����Ӽ�¼����Ӫ�� */
#define UTCA_MESSAGE_CURTAIN_TYPE	  0x15 /*�߼�Ԥ����Ŀ���ý׶β�����ѹۿ�*/
#define UTCA_MESSAGE_CARDTESTSTART_TYPE 0x16 /*�������Կ�������...*/
#define UTCA_MESSAGE_CARDTESTFAILD_TYPE 0x17 /*�������Կ�����ʧ�ܣ��������ͨѶģ��*/
#define UTCA_MESSAGE_CARDTESTSUCC_TYPE  0x18 /*�������Կ����Գɹ�*/
#define UTCA_MESSAGE_NOCALIBOPER_TYPE    0x19/*���в�������ֲ�ⶨ����Ӫ��*/

#define UTCA_MESSAGE_STBLOCKED_TYPE   0x20  /* ������������ */
#define UTCA_MESSAGE_STBFREEZE_TYPE   0x21  /* �����б����� */



/*---------- ���ܵ��÷���ֵ���� ----------*/
#define UTCA_RC_OK                    0x00  /* �ɹ� */
#define UTCA_RC_UNKNOWN               0x01  /* δ֪���� */
#define UTCA_RC_POINTER_INVALID       0x02  /* ָ����Ч */
#define UTCA_RC_CARD_INVALID          0x03  /* ���ܿ���Ч */
#define UTCA_RC_PIN_INVALID           0x04  /* PIN����Ч */
#define UTCA_RC_DATASPACE_SMALL       0x06  /* �����Ŀռ䲻�� */
#define UTCA_RC_CARD_PAIROTHER        0x07  /* ���ܿ��Ѿ���Ӧ��Ļ����� */
#define UTCA_RC_DATA_NOT_FIND         0x08  /* û���ҵ���Ҫ������ */
#define UTCA_RC_PROG_STATUS_INVALID   0x09  /* Ҫ����Ľ�Ŀ״̬��Ч */
#define UTCA_RC_CARD_NO_ROOM          0x0A  /* ���ܿ�û�пռ��Ź���Ľ�Ŀ */
#define UTCA_RC_WORKTIME_INVALID      0x0B  /* �趨�Ĺ���ʱ����Ч */
#define UTCA_RC_IPPV_CANNTDEL         0x0C  /* IPPV��Ŀ���ܱ�ɾ�� */
#define UTCA_RC_CARD_NOPAIR           0x0D  /* ���ܿ�û�ж�Ӧ�κεĻ����� */
#define UTCA_RC_WATCHRATING_INVALID   0x0E  /* �趨�Ĺۿ�������Ч */
#define UTCA_RC_CARD_NOTSUPPORT       0x0F  /* ��ǰ���ܿ���֧�ִ˹��� */
#define UTCA_RC_DATA_ERROR            0x10  /* ���ݴ������ܿ��ܾ� */
#define UTCA_RC_FEEDTIME_NOT_ARRIVE   0x11  /* ι��ʱ��δ�����ӿ����ܱ�ι�� */
#define UTCA_RC_CARD_TYPEERROR        0x12  /* ��ĸ��ι��ʧ�ܣ��������ܿ����ʹ��� */

#define UTCA_RC_CAS_FAILED                  0x20 //����casָ��ִ��ʧ��
#define UTCA_RC_OPER_FAILED                0x21 //������Ӫ��ָ��ִ��ʧ��


/*-- �������е����ܿ�״̬ --*/
#define UTCA_SC_OUT         0x00    /* ��������û�п�          */
#define UTCA_SC_REMOVING    0x01    /* ���ڰο�������״̬      */
#define UTCA_SC_INSERTING   0x02    /* ���ڲ忨����ʼ��        */
#define UTCA_SC_IN          0x03    /* ���������ǿ��õĿ�      */
#define UTCA_SC_ERROR       0x04    /* �������Ŀ�����ʶ��      */
#define UTCA_SC_UPDATE      0x05    /* �������Ŀ������� */
#define UTCA_SC_UPDATE_ERR  0x06    /* �������Ŀ�����ʧ��      */

/*---------- ECM_PID���õĲ������� ---------*/
#define UTCA_LIST_OK          0x00
#define UTCA_LIST_FIRST       0x01
#define UTCA_LIST_ADD         0x02


/*------------ IPPV/IPPT��ͬ����׶���ʾ -------------*/
#define UTCA_IPPV_FREEVIEWED_SEGMENT  0x00  /* IPPV���Ԥ���׶Σ��Ƿ��� */
#define UTCA_IPPV_PAYVIEWED_SEGMENT   0x01  /* IPPV�շѽ׶Σ��Ƿ��� */
#define UTCA_IPPT_PAYVIEWED_SEGMENT   0x02  /* IPPT�շѶΣ��Ƿ��� */

/*------------ IPPV�۸����� ------------*/
#define UTCA_IPPVPRICETYPE_TPPVVIEW       0x0  /* ���ش�����¼������ */
#define UTCA_IPPVPRICETYPE_TPPVVIEWTAPING 0x1  /* ���ش�����¼������ */

/*------------ IPPV��Ŀ��״̬ -----------*/
#define UTCA_IPPVSTATUS_BOOKING   0x01  /* Ԥ�� */
#define UTCA_IPPVSTATUS_VIEWED    0x03  /* �ѿ� */


/*---------- Ƶ������Ӧ����ض��� ---------*/
#define UTCA_MAXNUM_COMPONENT     5U    /* ��Ŀ��������� */
#define UTCA_MAXLEN_LOCKMESS      40U


/*---------- ����Ȩȷ����Ӧ����ض��� --------*/
#define UTCA_Detitle_All_Read     0x00  /* ���з���Ȩȷ�����Ѿ�����������ͼ�� */
#define UTCA_Detitle_Received     0x01  /* �յ��µķ���Ȩ�룬��ʾ����Ȩ��ͼ�� */
#define UTCA_Detitle_Space_Small  0x02  /* ����Ȩ��ռ䲻�㣬�ı�ͼ��״̬��ʾ�û� */
#define UTCA_Detitle_Ignore       0x03  /* �յ��ظ��ķ���Ȩ�룬�ɺ��ԣ��������� */


/*---------- ��������ʾ��Ϣ ---------*/
#define UTCA_SCALE_RECEIVEPATCH   1     /* �������ݽ����� */
#define UTCA_SCALE_PATCHING       2     /* ���ܿ������� */



/*-------------------------------------end of �궨��--------------------------------------*/



/*----------------------------------------���ݽṹ----------------------------------------*/

/*-- ϵͳʱ�� --*/
typedef UTCA_U32  UTCA_TIME;
typedef UTCA_U16  UTCA_DATE;

/*-- �ź������壨��ͬ�Ĳ���ϵͳ���ܲ�һ����--*/
// typedef UTCA_U32  UTCA_Semaphore;

/*-- ��Ӫ����Ϣ --*/
typedef struct {
    char     m_szTVSPriInfo[UTCA_MAXLEN_TVSPRIINFO+1];  /* ��Ӫ��˽����Ϣ */
    UTCA_U8  m_byReserved[3];    /* ���� */
}SUTCAOperatorInfo;

/*-- ��Ŀ��Ϣ --*/
/* Y10_update : ֻ��ҪECMPID��ServiceID���� */
typedef struct {
    UTCA_U16  m_wEcmPid;         /* ��Ŀ��Ӧ������Ϣ��PID */
    UTCA_U8   m_byServiceNum;    /* ��ǰPID�µĽ�Ŀ���� */
    UTCA_U8   m_byReserved;      /* ���� */
    UTCA_U16  m_wServiceID[UTCA_MAXNUM_PROGRAMBYCW]; /* ��ǰPID�µĽ�ĿID�б� */
}SCDCASServiceInfo;

/*-- ��Ȩ��Ϣ --*/
typedef struct {
    UTCA_U32  m_dwProductID;   /* ��ͨ��Ȩ�Ľ�ĿID */    
    UTCA_DATE m_tBeginDate;    /* ��Ȩ����ʼʱ�� */
    UTCA_DATE m_tExpireDate;   /* ��Ȩ�Ĺ���ʱ�� */
    UTCA_U8   m_bCanTape;      /* �û��Ƿ���¼��1������¼��0��������¼�� */
    UTCA_U8   m_byReserved[3]; /* ���� */
}SUTCAEntitle;

/*-- ��Ȩ��Ϣ���� --*/
typedef struct {
    UTCA_U16      m_wProductCount;
    UTCA_U8       m_m_byReserved[2];    /* ���� */
    SUTCAEntitle  m_Entitles[UTCA_MAXNUM_ENTITLE]; /* ��Ȩ�б� */
}SUTCAEntitles;

/*-- Ǯ����Ϣ --*/
typedef struct {
    UTCA_U32  m_wCreditLimit; /* ���öȣ�������*/
    UTCA_U32  m_wBalance;     /* �ѻ��ĵ��� */
}SUTCATVSSlotInfo;

/*-- IPPV/IPPT��Ŀ�ļ۸� --*/
typedef struct {
    UTCA_U16  m_wPrice;       /* ��Ŀ�۸񣨵�����*/
    UTCA_U8   m_byPriceCode;  /* ��Ŀ�۸����� */
    UTCA_U8   m_byReserved;   /* ���� */
}SUTCAIPPVPrice; 

/*-- IPPV/IPPT��Ŀ������ʾ��Ϣ --*/
typedef struct {
    UTCA_U32        m_dwProductID;          /* ��Ŀ��ID */
    UTCA_U16        m_wTvsID;               /* ��Ӫ��ID */
    UTCA_U8         m_bySlotID;             /* Ǯ��ID */
    UTCA_U8         m_byPriceNum;           /* ��Ŀ�۸���� */
    SUTCAIPPVPrice  m_Price[UTCA_MAXNUM_PRICE]; /* ��Ŀ�۸� */
    union {
        UTCA_DATE   m_wExpiredDate;         /* ��Ŀ����ʱ��,IPPV�� */
        UTCA_U16    m_wIntervalMin;         /* ʱ��������λ����,IPPT �� */
    }m_wIPPVTime;
    UTCA_U8         m_byReserved[2];        /* ���� */
}SUTCAIppvBuyInfo;

/*-- IPPV��Ŀ��Ϣ --*/
typedef struct {
    UTCA_U32   m_dwProductID;   /* ��Ŀ��ID */
    UTCA_U8    m_byBookEdFlag;  /* ��Ʒ״̬��BOOKING��VIEWED */ 
    UTCA_U8    m_bCanTape;      /* �Ƿ����¼��1������¼��0��������¼�� */
    UTCA_U16   m_wPrice;        /* ��Ŀ�۸� */
    UTCA_DATE  m_wExpiredDate;  /* ��Ŀ����ʱ��,IPPV�� */
    UTCA_U8    m_bySlotID;      /* Ǯ��ID */
    UTCA_U8    m_byReserved;    /* ���� */
}SUTCAIppvInfo;



/*-- Ƶ��������Ϣ --*/
/*-- ��Ŀ�����Ϣ --*/
typedef struct {    /* �������֪ͨ�����н�Ŀ���ͼ�PID����Ϣ��һ����Ŀ���ܰ��������� */
    UTCA_U16   m_wCompPID;     /* ���PID */
    UTCA_U16   m_wECMPID;      /* �����Ӧ��ECM����PID���������ǲ����ŵģ���Ӧȡ0�� */
    UTCA_U8    m_CompType;     /* ������� */
    UTCA_U8    m_byReserved[3];/* ���� */
}SUTCAComponent;

/*-- Ƶ��������Ϣ --*/
typedef struct {    
    UTCA_U32   m_dwFrequency;              /* Ƶ�ʣ�BCD�� */
    UTCA_U32   m_symbol_rate;              /* �����ʣ�BCD�� */
    UTCA_U16   m_wPcrPid;                  /* PCR PID */
    UTCA_U8    m_Modulation;               /* ���Ʒ�ʽ */
    UTCA_U8    m_ComponentNum;             /* ��Ŀ������� */
    SUTCAComponent m_CompArr[UTCA_MAXNUM_COMPONENT];       /* ��Ŀ����б� */
    UTCA_U8    m_fec_outer;                /* ǰ��������� */
    UTCA_U8    m_fec_inner;                /* ǰ��������� */
    char       m_szBeforeInfo[UTCA_MAXLEN_LOCKMESS+1]; /* ���� */
    char       m_szQuitInfo[UTCA_MAXLEN_LOCKMESS+1];   /* ���� */
    char       m_szEndInfo[UTCA_MAXLEN_LOCKMESS+1];    /* ���� */
}SUTCALockService;

/*-- �߼�Ԥ����Ŀ��Ϣ --*/
typedef struct {
    UTCA_U16   m_wProgramID;        /* ��Ŀ��ID */
    UTCA_TIME  m_dwStartWatchTime;   /* ��ʼ�ۿ�ʱ�� */
    UTCA_U8    m_byWatchTotalCount;  /* �ۼƹۿ����� */
    UTCA_U16   m_wWatchTotalTime;    /* �ۼƹۿ�ʱ��,(cp������) */
}SUTCACurtainInfo;


/*-----------------------------------------------------------------------------------
a. ��ϵͳ�У�����m_dwFrequency��m_symbol_rateʹ��BCD�룬����ǰȡMHzΪ��λ��
   ����ʱ��ǰ4��4-bit BCD���ʾС����ǰ��ֵ����4��4-bit BCD���ʾС������ֵ��
   ���磺
        ��Ƶ��Ϊ642000KHz����642.0000MHz�����Ӧ��m_dwFrequency��ֵӦΪ0x06420000��
        ��������Ϊ6875KHz����6.8750MHz�����Ӧ��m_symbol_rate��ֵӦΪ0x00068750��

b. ��ϵͳ�У�m_Modulation��ȡֵ���£�
    0       Reserved
    1       QAM16
    2       QAM32
    3       QAM64
    4       QAM128
    5       QAM256
    6��255  Reserved
------------------------------------------------------------------------------------*/ 


/*------------------------------------end of ���ݽṹ-------------------------------------*/



/*---------------------------���½ӿ���CA_LIB�ṩ��STB------------------------*/
#if 0
// ���½ӿ�����utcas_interface.h ���岢ʵ��
/*------ CA_LIB���ȹ��� ------*/

/* CA_LIB��ʼ�� */
UTCA_BOOL CDCASTB_Init( UTCA_U8 byThreadPrior );

/* �ر�CA_LIB���ͷ���Դ */
void CDCASTB_Close( void );

/* UTCASͬ���ж� */
extern UTCA_BOOL  CDCASTB_IsUTCA(UTCA_U16 wCaSystemID);

/*------ TS������ ------*/

/* ����ECM�ͽ�Ŀ��Ϣ */
extern void CDCASTB_SetEcmPid( UTCA_U8 byType,
                               const SCDCASServiceInfo* pServiceInfo );

/* ����EMM��Ϣ */
extern void  CDCASTB_SetEmmPid(UTCA_U16 wEmmPid);

/* ˽�����ݽ��ջص� */
extern void CDCASTB_PrivateDataGot( UTCA_U8        byReqID,
								  	UTCA_BOOL      bTimeout,
									UTCA_U16       wPid,
									const UTCA_U8* pbyReceiveData,
									UTCA_U16       wLen            );

/*------- ���ܿ����� -------*/

/* �������ܿ� */
extern UTCA_BOOL CDCASTB_SCInsert( void );

/* �γ����ܿ�*/
extern void CDCASTB_SCRemove( void );

/* ��ȡ���ܿ��ⲿ���� */
extern UTCA_U16 CDCASTB_GetCardSN( char* pCardSN );

#endif

/*------ Flash���� ------ */

/* �洢�ռ�ĸ�ʽ�� */
extern void CDCASTB_FormatBuffer( void );

/* ���ζԴ洢�ռ�Ķ�д���� */
extern void CDCASTB_RequestMaskBuffer(void);

/* �򿪶Դ洢�ռ�Ķ�д���� */
extern void CDCASTB_RequestUpdateBuffer(void);



/*------- ������Ϣ��ѯ -------*/

/* ��ѯCA_LIB�汾�� */
extern UTCA_U32 CDCASTB_GetVer( void );

/* ��ѯ������ƽ̨��� */
extern UTCA_U16 CDCASTB_GetPlatformID( void );


/*------------------------���Ͻӿ���CA_LIB�ṩ��STB---------------------------*/

/******************************************************************************/

/*------------------------���½ӿ���STB�ṩ��CA_LIB---------------------------*/

/*-------- �̹߳��� --------*/

/* ע������ */
extern UTCA_BOOL CDSTBCA_RegisterTask( const char* szName,
                                       UTCA_U8     byPriority,
                                       void*       pTaskFun,
                                       void*       pParam,
                                       UTCA_U16    wStackSize  );

/* �̹߳��� */
extern void CDSTBCA_Sleep(UTCA_U16 wMilliSeconds);




/*--------- �洢�ռ䣨Flash������ ---------*/

/* ��ȡ�洢�ռ� */
extern void CDSTBCA_ReadBuffer( UTCA_U8   byBlockID,
                                UTCA_U8*  pbyData,
                                UTCA_U32* pdwLen );

/* д��洢�ռ� */
extern void CDSTBCA_WriteBuffer( UTCA_U8        byBlockID,
                                 const UTCA_U8* pbyData,
                                 UTCA_U32       dwLen );


/*-------- TS������ --------*/

/* ����˽�����ݹ����� */
extern UTCA_BOOL CDSTBCA_SetPrivateDataFilter( UTCA_U8        byReqID,  
											   const UTCA_U8* pbyFilter,  
											   const UTCA_U8* pbyMask, 
											   UTCA_U8        byLen, 
											   UTCA_U16       wPid, 
											   UTCA_U8        byWaitSeconds );


/* �ͷ�˽�����ݹ����� */
extern void CDSTBCA_ReleasePrivateDataFilter( UTCA_U8  byReqID,
                                              UTCA_U16 wPid );

/* ����CW�������� */
extern void CDSTBCA_ScrSetCW( UTCA_U16       wEcmPID,  
							  const UTCA_U8* pbyOddKey,  
							  const UTCA_U8* pbyEvenKey, 
							  UTCA_U8        byKeyLen, 
							  UTCA_BOOL      bTapingEnabled );


/*--------- ���ܿ����� ---------*/

/* ���ܿ���λ */
extern UTCA_BOOL CDSTBCA_SCReset( UTCA_U8* pbyATR, UTCA_U8* pbyLen );

/* ���ܿ�ͨѶ */
extern UTCA_BOOL CDSTBCA_SCPBRun( const UTCA_U8* pbyCommand, 
								  UTCA_U16       wCommandLen,  
								  UTCA_U8*       pbyReply,  
								  UTCA_U16*      pwReplyLen  );

/* ���ܿ��������� */
extern UTCA_BOOL SMSTBCA_SCSend( const UTCA_U8* pbyData,
								 UTCA_U32       wDataLen );

/* ���ܿ��������� */
extern UTCA_BOOL SMSTBCA_SCRev( UTCA_U8*  pbyData,
								UTCA_U32 wDataLen );

extern UTCA_BOOL CDSTBCA_SCapdu(UTCA_U8* send_buf, UTCA_U32 send_len, UTCA_U8 *rec_buf, UTCA_U32 *rec_len);
								 
								 
/*-------- ��ȫ���� --------*/

/* ��ȡ������Ψһ��� */
extern void CDSTBCA_GetSTBID( UTCA_U16* pwPlatformID,
                              UTCA_U32* pdwUniqueID);

/* ��ȫоƬ�ӿ� */
extern UTCA_U16 CDSTBCA_SCFunction( UTCA_U8* pData);

/*-------- ���� --------*/

/* ��ȡ�ַ������� */
extern UTCA_U16 CDSTBCA_Strlen(const char* pString );

/* ������Ϣ��� */
extern void CDSTBCA_Printf(UTCA_U8 byLevel, const char* szMesssage );


/*-------- �ź������� --------*/

/* ��ʼ���ź��� */
void CDSTBCA_SemaphoreInit( UTCA_Semaphore* pSemaphore,
                                   UTCA_BOOL       bInitVal );
/* �ź��������ź� */
void CDSTBCA_SemaphoreSignal( UTCA_Semaphore* pSemaphore );

/* �ź�����ȡ�ź� */
void CDSTBCA_SemaphoreWait( UTCA_Semaphore* pSemaphore );

void CDSTBCA_InitLock(int* lock);
void CDSTBCA_FreeLock(int lock);
void CDSTBCA_Lock(int lock);
void CDSTBCA_UnLock(int lock);

/*-------- �ڴ���� --------*/

/* �����ڴ� */
void* CDSTBCA_Malloc( UTCA_U32 byBufSize );

/* �ͷ��ڴ� */
void  CDSTBCA_Free( void* pBuf );

/* �ڴ渳ֵ */
void  CDSTBCA_Memset( void*    pDestBuf,
                             UTCA_U8  c,
                             UTCA_U32 wSize );

/* �ڴ渴�� */
void  CDSTBCA_Memcpy( void*       pDestBuf,
                             const void* pSrcBuf,
                             UTCA_U32    wSize );


/*---------------------------���Ͻӿ���STB�ṩ��CA_LIB------------------------*/

#ifdef  __cplusplus
}
#endif
#endif
/*EOF*/

