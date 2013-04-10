#ifndef CDCAS_CALIB_APIEX_H
#define CDCAS_CALIB_APIEX_H

#ifndef WIN32
#define __stdcall
#endif
#ifdef  __cplusplus
extern "C" {
#endif


/*-------------------------------------������������---------------------------------------*/
typedef unsigned  long  CDCA_U32;
typedef unsigned  short CDCA_U16;
typedef unsigned  char  CDCA_U8;
typedef signed long  CDCA_S32;

typedef CDCA_U8 CDCA_BOOL;

#define CDCA_TRUE    ((CDCA_BOOL)1)
#define CDCA_FALSE   ((CDCA_BOOL)0)

/*----------------------------------------�궨��--------------------------------------*/

/*--------- FLASH���� -------*/
#define CDCA_FLASH_BLOCK_A        1     /* BLOCK A */
#define CDCA_FLASH_BLOCK_B        2     /* BLOCK B */

/*--------- ���ܿ�������� --------*/
#define CDCA_MAXLEN_STBSN            12U   /* �����кŵĳ��� */
#define CDCA_MAXLEN_SN            16U   /* ���ܿ����кŵĳ��� */
#define CDCA_MAXLEN_PINCODE       6U    /* PIN��ĳ��� */
#define CDCA_MAXLEN_TVSPRIINFO    32U   /* ��Ӫ��˽����Ϣ�ĳ��� */
#define CDCA_MAXNUM_OPERATOR      4U    /* ������Ӫ�̸��� */
#define CDCA_MAXNUM_ACLIST        18U   /* ���ܿ��ڱ����ÿ����Ӫ�̵��û��������� */
#define CDCA_MAXNUM_SLOT          20U   /* ���洢�����Ǯ���� */
#define CDCA_MAXNUM_CURTAIN_RECORD	80U /*�߼�Ԥ����Ŀ��¼��������*/

#define CDCA_MAXNUM_IPPVP         300U  /* ���ܿ��������IPPV��Ŀ�ĸ��� */
#define CDCA_MAXNUM_PRICE         2U    /* ����IPPV�۸���� */

#define CDCA_MAXNUM_ENTITLE       300U  /* ��Ȩ��Ʒ�������� */

/*---------- ������������� ----------*/
#define CDCA_MAXNUM_PROGRAMBYCW   4U    /* һ������������Ľ�Ŀ�� */
#define CDCA_MAXNUM_ECM           8U    /* ͬʱ���մ����ECMPID��������� */

#define CDCA_MAXNUM_DETITLE       5U    /* ÿ����Ӫ���¿ɱ���ķ���Ȩ����� */

/*---------- CAS��ʾ��Ϣ ---------*/
#define CDCA_MESSAGE_CANCEL_TYPE      0x00  /* ȡ����ǰ����ʾ */
#define CDCA_MESSAGE_BADCARD_TYPE     0x01  /* �޷�ʶ�� */
#define CDCA_MESSAGE_EXPICARD_TYPE    0x02  /* ���ܿ����ڣ�������¿� */
#define CDCA_MESSAGE_INSERTCARD_TYPE  0x03  /* ���Ž�Ŀ����������ܿ� */
#define CDCA_MESSAGE_NOOPER_TYPE      0x04  /* ���в����ڽ�Ŀ��Ӫ�� */
#define CDCA_MESSAGE_BLACKOUT_TYPE    0x05  /* �������� */
#define CDCA_MESSAGE_OUTWORKTIME_TYPE 0x06  /* ��ǰʱ�α��趨Ϊ���ܹۿ� */
#define CDCA_MESSAGE_WATCHLEVEL_TYPE  0x07  /* ��Ŀ��������趨�Ĺۿ����� */
#define CDCA_MESSAGE_PAIRING_TYPE     0x08  /* ���ܿ��뱾�����в���Ӧ */
#define CDCA_MESSAGE_NOENTITLE_TYPE   0x09  /* û����Ȩ */
#define CDCA_MESSAGE_DECRYPTFAIL_TYPE 0x0A  /* ��Ŀ����ʧ�� */
#define CDCA_MESSAGE_NOMONEY_TYPE     0x0B  /* ���ڽ��� */
#define CDCA_MESSAGE_ERRREGION_TYPE   0x0C  /* ������ȷ */
#define CDCA_MESSAGE_NEEDFEED_TYPE    0x0D  /* �ӿ���Ҫ��ĸ����Ӧ�������ĸ�� */
#define CDCA_MESSAGE_ERRCARD_TYPE     0x0E  /* ���ܿ�У��ʧ�ܣ�����ϵ��Ӫ�� */
#define CDCA_MESSAGE_UPDATE_TYPE      0x0F  /* ���ܿ������У��벻Ҫ�ο����߹ػ� */
#define CDCA_MESSAGE_LOWCARDVER_TYPE  0x10  /* ���������ܿ� */
#define CDCA_MESSAGE_VIEWLOCK_TYPE    0x11  /* ����Ƶ���л�Ƶ�� */
#define CDCA_MESSAGE_MAXRESTART_TYPE  0x12  /* ���ܿ���ʱ���ߣ���5���Ӻ����¿��� */
#define CDCA_MESSAGE_FREEZE_TYPE      0x13  /* ���ܿ��Ѷ��ᣬ����ϵ��Ӫ�� */
#define CDCA_MESSAGE_CALLBACK_TYPE    0x14  /* ���ܿ�����ͣ����ش����Ӽ�¼����Ӫ�� */
#define CDCA_MESSAGE_CURTAIN_TYPE	  0x15 /*�߼�Ԥ����Ŀ���ý׶β�����ѹۿ�*/
#define CDCA_MESSAGE_CARDTESTSTART_TYPE 0x16 /*�������Կ�������...*/
#define CDCA_MESSAGE_CARDTESTFAILD_TYPE 0x17 /*�������Կ�����ʧ�ܣ��������ͨѶģ��*/
#define CDCA_MESSAGE_CARDTESTSUCC_TYPE  0x18 /*�������Կ����Գɹ�*/
#define CDCA_MESSAGE_NOCALIBOPER_TYPE    0x19/*���в�������ֲ�ⶨ����Ӫ��*/
#define CDCA_MESSAGE_STBLOCKED_TYPE   0x20  /* ������������ */
#define CDCA_MESSAGE_STBFREEZE_TYPE   0x21  /* �����б����� */

//CAS��ʾ��Ϣ for SHUMA
//��������ʾ��Ϣ���Ϊ���µ���չ,��0x10 + CDCA_MESSAGE_STBFREEZE_TYPE ��ʼ
//������Ϣ���ֵΪunsigned char 256
#define CDCA_MESSAGE_DVTCA_MESSAGE_OFFSET		0x10
#define CDCA_MESSAGE_DVTCA_MESSAGE_BASE			(CDCA_MESSAGE_DVTCA_MESSAGE_OFFSET + CDCA_MESSAGE_STBFREEZE_TYPE)
                                                                              
#define DVTCA_RATING_TOO_LOW                    0           //�տ����𲻹�
#define CDCA_MESSAGE_DVTCA_RATING_TOO_LOW		(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_RATING_TOO_LOW)

#define DVTCA_NOT_IN_WATCH_TIME                 1           //�����տ�ʱ����
#define CDCA_MESSAGE_DVTCA_NOT_IN_WATCH_TIME		(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_NOT_IN_WATCH_TIME)

#define DVTCA_NOT_PAIRED                        2           //û�л�����Ӧ
#define CDCA_MESSAGE_DVTCA_NOT_PAIRED			(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_NOT_PAIRED)

#define DVTCA_PLEASE_INSERT_CARD                4           //��忨
#define CDCA_MESSAGE_DVTCA_PLEASE_INSERT_CARD		(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_PLEASE_INSERT_CARD)

#define DVTCA_NO_ENTITLE                        5           //û�й���˽�Ŀ
#define CDCA_MESSAGE_DVTCA_NO_ENTITLE			(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_NO_ENTITLE)

#define DVTCA_PRODUCT_RESTRICT                  6           //��Ӫ�����ƹۿ��ý�Ŀ
#define CDCA_MESSAGE_DVTCA_PRODUCT_RESTRICT		(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_PRODUCT_RESTRICT)

#define DVTCA_AREA_RESTRICT                     7           //��Ӫ����������ۿ�
#define CDCA_MESSAGE_DVTCA_AREA_RESTRICT		(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_AREA_RESTRICT)

//V2.1��������ʾ��Ϣ
#define DVTCA_MOTHER_RESTRICT                   8           //�˿�Ϊ�ӿ����Ѿ��������տ�������ĸ�����
#define CDCA_MESSAGE_DVTCA_MOTHER_RESTRICT		(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_MOTHER_RESTRICT)

#define DVTCA_NO_MONEY                          9           //���㣬���ܹۿ��˽�Ŀ���뼰ʱ��ֵ
#define CDCA_MESSAGE_DVTCA_NO_MONEY			(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_NO_MONEY)

#define DVTCA_IPPV_NO_CONFIRM                   10          //�˽�ĿΪIPPV��Ŀ���뵽IPPV��Ŀȷ��/ȡ������˵���ȷ�Ϲ���˽�Ŀ
#define CDCA_MESSAGE_DVTCA_IPPV_NO_CONFIRM		(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_IPPV_NO_CONFIRM)

#define DVTCA_IPPV_NO_BOOK                      11          //�˽�ĿΪIPPV��Ŀ����û��Ԥ����ȷ�Ϲ��򣬲��ܹۿ��˽�Ŀ
#define CDCA_MESSAGE_DVTCA_IPPV_NO_BOOK			(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_IPPV_NO_BOOK)

#define DVTCA_IPPT_NO_CONFIRM                   12          //�˽�ĿΪIPPT��Ŀ���뵽IPPT��Ŀȷ��/ȡ������˵���ȷ�Ϲ���˽�Ŀ
#define CDCA_MESSAGE_DVTCA_IPPT_NO_CONFIRM		(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_IPPT_NO_CONFIRM)

#define DVTCA_IPPT_NO_BOOK                      13          //�˽�ĿΪIPPT��Ŀ����û��Ԥ����ȷ�Ϲ��򣬲��ܹۿ��˽�Ŀ
#define CDCA_MESSAGE_DVTCA_IPPT_NO_BOOK			(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_IPPT_NO_BOOK)

//xb:20050617
#define DVTCA_DATA_INVALID                      16          //������Ч��STB�����κ���ʾ������Կ���⡣
#define CDCA_MESSAGE_DVTCA_DATA_INVALID			(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_DATA_INVALID)

#define DVTCA_SC_NOT_SERVER                     18          //IC������ֹ����
#define CDCA_MESSAGE_DVTCA_SC_NOT_SERVER		(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_SC_NOT_SERVER)

#define DVTCA_KEY_NOT_FOUND                     20          //�˿�δ���������ϵ��Ӫ��
#define CDCA_MESSAGE_DVTCA_KEY_NOT_FOUND		(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_KEY_NOT_FOUND)

#define DVTCA_IPPNEED_CALLBACK                  21          //����ϵ��Ӫ�̻ش�IPP��Ŀ��Ϣ
#define CDCA_MESSAGE_DVTCA_IPPNEED_CALLBACK		(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_IPPNEED_CALLBACK)

#define DVTCA_FREE_PREVIEWING                   22          //�û����ã��˽�Ŀ����δ�����������Ԥ����
#define CDCA_MESSAGE_DVTCA_FREE_PREVIEWING		(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_FREE_PREVIEWING)


/*---------- ���ܵ��÷���ֵ���� ----------*/
#define CDCA_RC_OK                    0x00  /* �ɹ� */
#define CDCA_RC_UNKNOWN               0x01  /* δ֪���� */
#define CDCA_RC_POINTER_INVALID       0x02  /* ָ����Ч */
#define CDCA_RC_CARD_INVALID          0x03  /* ���ܿ���Ч */
#define CDCA_RC_PIN_INVALID           0x04  /* PIN����Ч */
#define CDCA_RC_DATASPACE_SMALL       0x06  /* �����Ŀռ䲻�� */
#define CDCA_RC_CARD_PAIROTHER        0x07  /* ���ܿ��Ѿ���Ӧ��Ļ����� */
#define CDCA_RC_DATA_NOT_FIND         0x08  /* û���ҵ���Ҫ������ */
#define CDCA_RC_PROG_STATUS_INVALID   0x09  /* Ҫ����Ľ�Ŀ״̬��Ч */
#define CDCA_RC_CARD_NO_ROOM          0x0A  /* ���ܿ�û�пռ��Ź���Ľ�Ŀ */
#define CDCA_RC_WORKTIME_INVALID      0x0B  /* �趨�Ĺ���ʱ����Ч */
#define CDCA_RC_IPPV_CANNTDEL         0x0C  /* IPPV��Ŀ���ܱ�ɾ�� */
#define CDCA_RC_CARD_NOPAIR           0x0D  /* ���ܿ�û�ж�Ӧ�κεĻ����� */
#define CDCA_RC_WATCHRATING_INVALID   0x0E  /* �趨�Ĺۿ�������Ч */
#define CDCA_RC_CARD_NOTSUPPORT       0x0F  /* ��ǰ���ܿ���֧�ִ˹��� */
#define CDCA_RC_DATA_ERROR            0x10  /* ���ݴ������ܿ��ܾ� */
#define CDCA_RC_FEEDTIME_NOT_ARRIVE   0x11  /* ι��ʱ��δ�����ӿ����ܱ�ι�� */
#define CDCA_RC_CARD_TYPEERROR        0x12  /* ��ĸ��ι��ʧ�ܣ��������ܿ����ʹ��� */
#define CDCA_RC_CAS_FAILED                  0x20 //����casָ��ִ��ʧ��
#define CDCA_RC_OPER_FAILED                0x21 //������Ӫ��ָ��ִ��ʧ��
/*-- �������е����ܿ�״̬ --*/
#define CDCA_SC_OUT         0x00    /* ��������û�п�          */
#define CDCA_SC_REMOVING    0x01    /* ���ڰο�������״̬      */
#define CDCA_SC_INSERTING   0x02    /* ���ڲ忨����ʼ��        */
#define CDCA_SC_IN          0x03    /* ���������ǿ��õĿ�      */
#define CDCA_SC_ERROR       0x04    /* �������Ŀ�����ʶ��      */
#define CDCA_SC_UPDATE      0x05    /* �������Ŀ������� */
#define CDCA_SC_UPDATE_ERR  0x06    /* �������Ŀ�����ʧ��      */

/*---------- ECM_PID���õĲ������� ---------*/
#define CDCA_LIST_OK          0x00
#define CDCA_LIST_FIRST       0x01
#define CDCA_LIST_ADD         0x02


/*------------ �ʼ���С���������� ------------*/
#define CDCA_MAXNUM_EMAIL         100U  /* �����б��������ʼ����� */
#define CDCA_MAXLEN_EMAIL_TITLE   30U   /* �ʼ�����ĳ��� */
#define CDCA_MAXLEN_EMAIL_CONTENT 160U  /* �ʼ����ݵĳ��� */

/*------------ �ʼ�ͼ����ʾ��ʽ ------------*/
#define CDCA_Email_IconHide       0x00  /* �����ʼ�֪ͨͼ�� */
#define CDCA_Email_New            0x01  /* ���ʼ�֪ͨ����ʾ���ʼ�ͼ�� */
#define CDCA_Email_SpaceExhaust   0x02  /* ���̿ռ�������ͼ����˸�� */

/*------------ OSD�ĳ������� -----------*/
#define CDCA_MAXLEN_OSD           180U  /* OSD���ݵ���󳤶� */

/*------------ OSD��ʾ���� ------------*/
#define CDCA_OSD_TOP              0x01  /* OSD�����ʾ����Ļ�Ϸ� */
#define CDCA_OSD_BOTTOM           0x02  /* OSD�����ʾ����Ļ�·� */
#define CDCA_OSD_FULLSCREEN       0x03  /* OSD���������ʾ */
#define CDCA_OSD_HALFSCREEN       0x04  /* OSD��񣺰�����ʾ */


/*------------ IPPV/IPPT��ͬ����׶���ʾ -------------*/
#define CDCA_IPPV_FREEVIEWED_SEGMENT  0x00  /* IPPV���Ԥ���׶Σ��Ƿ��� */
#define CDCA_IPPV_PAYVIEWED_SEGMENT   0x01  /* IPPV�շѽ׶Σ��Ƿ��� */
#define CDCA_IPPT_PAYVIEWED_SEGMENT   0x02  /* IPPT�շѶΣ��Ƿ��� */

/*------------ IPPV�۸����� ------------*/
#define CDCA_IPPVPRICETYPE_TPPVVIEW       0x0  /* ���ش�����¼������ */
#define CDCA_IPPVPRICETYPE_TPPVVIEWTAPING 0x1  /* ���ش�����¼������ */

/*------------ IPPV��Ŀ��״̬ -----------*/
#define CDCA_IPPVSTATUS_BOOKING   0x01  /* Ԥ�� */
#define CDCA_IPPVSTATUS_VIEWED    0x03  /* �ѿ� */


/*---------- Ƶ������Ӧ����ض��� ---------*/
#define CDCA_MAXNUM_COMPONENT     5U    /* ��Ŀ��������� */
#define CDCA_MAXLEN_LOCKMESS      40U


/*---------- ����Ȩȷ����Ӧ����ض��� --------*/
#define CDCA_Detitle_All_Read     0x00  /* ���з���Ȩȷ�����Ѿ�����������ͼ�� */
#define CDCA_Detitle_Received     0x01  /* �յ��µķ���Ȩ�룬��ʾ����Ȩ��ͼ�� */
#define CDCA_Detitle_Space_Small  0x02  /* ����Ȩ��ռ䲻�㣬�ı�ͼ��״̬��ʾ�û� */
#define CDCA_Detitle_Ignore       0x03  /* �յ��ظ��ķ���Ȩ�룬�ɺ��ԣ��������� */


/*---------- ��������ʾ��Ϣ ---------*/
#define CDCA_SCALE_RECEIVEPATCH   1     /* �������ݽ����� */
#define CDCA_SCALE_PATCHING       2     /* ���ܿ������� */
#define CDCA_CURTAIN_BASE				   0x0000		/*�߼�Ԥ��״̬�����ֵ,���ֽڱ���0x00*/
#define CDCA_CURTAIN_CANCLE			   (CDCA_CURTAIN_BASE+0x00)  /*ȡ���߼�Ԥ����ʾ*/
#define CDCA_CURTAIN_OK    				   (CDCA_CURTAIN_BASE+0x01)  /*�߼�Ԥ����Ŀ��������*/
#define CDCA_CURTAIN_TOTTIME_ERROR	   (CDCA_CURTAIN_BASE+0x02)  /*�߼�Ԥ����Ŀ��ֹ���ܣ��Ѿ��ﵽ�ܹۿ�ʱ��*/
#define CDCA_CURTAIN_WATCHTIME_ERROR (CDCA_CURTAIN_BASE+0x03)  /*�߼�Ԥ����Ŀ��ֹ���ܣ��Ѿ��ﵽWatchTime����*/
#define CDCA_CURTAIN_TOTCNT_ERROR 	   (CDCA_CURTAIN_BASE+0x04)  /*�߼�Ԥ����Ŀ��ֹ���ܣ��Ѿ��ﵽ������ۿ�����*/
#define CDCA_CURTAIN_ROOM_ERROR 	   (CDCA_CURTAIN_BASE+0x05)  /*�߼�Ԥ����Ŀ��ֹ���ܣ��߼�Ԥ����Ŀ��¼�ռ䲻��*/
#define CDCA_CURTAIN_PARAM_ERROR 	   (CDCA_CURTAIN_BASE+0x06)  /*�߼�Ԥ����Ŀ��ֹ���ܣ���Ŀ��������*/
#define CDCA_CURTAIN_TIME_ERROR 		   (CDCA_CURTAIN_BASE+0x07)  /*�߼�Ԥ����Ŀ��ֹ���ܣ����ݴ���*/

/*-------------------------------------end of �궨��--------------------------------------*/



/*----------------------------------------���ݽṹ----------------------------------------*/

/*-- ϵͳʱ�� --*/
typedef CDCA_U32  CDCA_TIME;
typedef CDCA_U16  CDCA_DATE;

/*-- �ź������壨��ͬ�Ĳ���ϵͳ���ܲ�һ����--*/
typedef CDCA_U32  CDCA_Semaphore;

/*-- ��Ӫ����Ϣ --*/
typedef struct {
    char     m_szTVSPriInfo[CDCA_MAXLEN_TVSPRIINFO+1];  /* ��Ӫ��˽����Ϣ */
    CDCA_U8  m_byReserved[3];    /* ���� */
}SCDCAOperatorInfo;

/*-- ��Ŀ��Ϣ --*/
/* Y10_update : ֻ��ҪECMPID��ServiceID���� */
typedef struct {
    CDCA_U16  m_wEcmPid;         /* ��Ŀ��Ӧ������Ϣ��PID */
    CDCA_U8   m_byServiceNum;    /* ��ǰPID�µĽ�Ŀ���� */
    CDCA_U8   m_byReserved;      /* ���� */
    CDCA_U16  m_wServiceID[CDCA_MAXNUM_PROGRAMBYCW]; /* ��ǰPID�µĽ�ĿID�б� */
}SCDCASServiceInfo;

/*-- ��Ȩ��Ϣ --*/
typedef struct {
    CDCA_U32  m_dwProductID;   /* ��ͨ��Ȩ�Ľ�ĿID */    
    CDCA_DATE m_tBeginDate;    /* ��Ȩ����ʼʱ�� */
    CDCA_DATE m_tExpireDate;   /* ��Ȩ�Ĺ���ʱ�� */
    CDCA_U8   m_bCanTape;      /* �û��Ƿ���¼��1������¼��0��������¼�� */
    CDCA_U8   m_byReserved[3]; /* ���� */
}SCDCAEntitle;

/*-- ��Ȩ��Ϣ���� --*/
typedef struct {
    CDCA_U16      m_wProductCount;
    CDCA_U8       m_m_byReserved[2];    /* ���� */
    SCDCAEntitle  m_Entitles[CDCA_MAXNUM_ENTITLE]; /* ��Ȩ�б� */
}SCDCAEntitles;

/*-- Ǯ����Ϣ --*/
typedef struct {
    CDCA_U32  m_wCreditLimit; /* ���öȣ�������*/
    CDCA_U32  m_wBalance;     /* �ѻ��ĵ��� */
}SCDCATVSSlotInfo;

/*-- IPPV/IPPT��Ŀ�ļ۸� --*/
typedef struct {
    CDCA_U16  m_wPrice;       /* ��Ŀ�۸񣨵�����*/
    CDCA_U8   m_byPriceCode;  /* ��Ŀ�۸����� */
    CDCA_U8   m_byReserved;   /* ���� */
}SCDCAIPPVPrice; 

/*-- IPPV/IPPT��Ŀ������ʾ��Ϣ --*/
typedef struct {
    CDCA_U32        m_dwProductID;          /* ��Ŀ��ID */
    CDCA_U16        m_wTvsID;               /* ��Ӫ��ID */
    CDCA_U8         m_bySlotID;             /* Ǯ��ID */
    CDCA_U8         m_byPriceNum;           /* ��Ŀ�۸���� */
    SCDCAIPPVPrice  m_Price[CDCA_MAXNUM_PRICE]; /* ��Ŀ�۸� */
    union {
        CDCA_DATE   m_wExpiredDate;         /* ��Ŀ����ʱ��,IPPV�� */
        CDCA_U16    m_wIntervalMin;         /* ʱ��������λ����,IPPT �� */
    }m_wIPPVTime;
    CDCA_U8         m_byReserved[2];        /* ���� */
}SCDCAIppvBuyInfo;

/*-- IPPV��Ŀ��Ϣ --*/
typedef struct {
    CDCA_U32   m_dwProductID;   /* ��Ŀ��ID */
    CDCA_U8    m_byBookEdFlag;  /* ��Ʒ״̬��BOOKING��VIEWED */ 
    CDCA_U8    m_bCanTape;      /* �Ƿ����¼��1������¼��0��������¼�� */
    CDCA_U16   m_wPrice;        /* ��Ŀ�۸� */
    CDCA_DATE  m_wExpiredDate;  /* ��Ŀ����ʱ��,IPPV�� */
    CDCA_U8    m_bySlotID;      /* Ǯ��ID */
    CDCA_U8    m_byReserved;    /* ���� */
}SCDCAIppvInfo;


/*-- �ʼ�ͷ --*/
typedef struct {
    CDCA_U32   m_dwActionID;                 /* Email ID */
    CDCA_U32   m_tCreateTime;                /* EMAIL������ʱ�� */
    CDCA_U16   m_wImportance;                /* ��Ҫ�ԣ� 0����ͨ��1����Ҫ */
    CDCA_U8    m_byReserved[2];              /* ���� */
    char       m_szEmailHead[CDCA_MAXLEN_EMAIL_TITLE+1]; /* �ʼ����⣬�Ϊ30 */    
    CDCA_U8    m_bNewEmail;                  /* ���ʼ���ǣ�0���Ѷ��ʼ���1�����ʼ� */
}SCDCAEmailHead;

/*-- �ʼ����� --*/
typedef struct {
    char     m_szEmail[CDCA_MAXLEN_EMAIL_CONTENT+1];      /* Email������ */
    CDCA_U8  m_byReserved[3];              /* ���� */
}SCDCAEmailContent;


/*-- Ƶ��������Ϣ --*/
/*-- ��Ŀ�����Ϣ --*/
typedef struct {    /* �������֪ͨ�����н�Ŀ���ͼ�PID����Ϣ��һ����Ŀ���ܰ��������� */
    CDCA_U16   m_wCompPID;     /* ���PID */
    CDCA_U16   m_wECMPID;      /* �����Ӧ��ECM����PID���������ǲ����ŵģ���Ӧȡ0�� */
    CDCA_U8    m_CompType;     /* ������� */
    CDCA_U8    m_byReserved[3];/* ���� */
}SCDCAComponent;

/*-- Ƶ��������Ϣ --*/
typedef struct {    
    CDCA_U32   m_dwFrequency;              /* Ƶ�ʣ�BCD�� */
    CDCA_U32   m_symbol_rate;              /* �����ʣ�BCD�� */
    CDCA_U16   m_wPcrPid;                  /* PCR PID */
    CDCA_U8    m_Modulation;               /* ���Ʒ�ʽ */
    CDCA_U8    m_ComponentNum;             /* ��Ŀ������� */
    SCDCAComponent m_CompArr[CDCA_MAXNUM_COMPONENT];       /* ��Ŀ����б� */
    CDCA_U8    m_fec_outer;                /* ǰ��������� */
    CDCA_U8    m_fec_inner;                /* ǰ��������� */
    char       m_szBeforeInfo[CDCA_MAXLEN_LOCKMESS+1]; /* ���� */
    char       m_szQuitInfo[CDCA_MAXLEN_LOCKMESS+1];   /* ���� */
    char       m_szEndInfo[CDCA_MAXLEN_LOCKMESS+1];    /* ���� */
}SCDCALockService;


/*-- �߼�Ԥ����Ŀ��Ϣ --*/
typedef struct {
    CDCA_U16   m_wProgramID;        /* ��Ŀ��ID */
    CDCA_TIME  m_dwStartWatchTime;   /* ��ʼ�ۿ�ʱ�� */
    CDCA_U8    m_byWatchTotalCount;  /* �ۼƹۿ����� */
    CDCA_U16   m_wWatchTotalTime;    /* �ۼƹۿ�ʱ��,(cp������) */
}SCDCACurtainInfo;
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
#ifdef  __cplusplus
}
#endif
#endif
/*EOF*/

