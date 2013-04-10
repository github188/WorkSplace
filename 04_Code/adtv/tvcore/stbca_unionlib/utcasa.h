#ifndef UTCAS_CALIB_APIEX_H
#define UTCAS_CALIB_APIEX_H

#ifdef  __cplusplus
extern "C" {
#endif


/*-------------------------------------基本数据类型---------------------------------------*/
typedef unsigned  int  UTCA_U32;
typedef unsigned  short UTCA_U16;
typedef unsigned  char  UTCA_U8;

typedef UTCA_U32  UTCA_Semaphore;

typedef UTCA_U8 UTCA_BOOL;

#define UTCA_TRUE    ((UTCA_BOOL)1)
#define UTCA_FALSE   ((UTCA_BOOL)0)

/*----------------------------------------宏定义--------------------------------------*/

/*--------- FLASH类型 -------*/
#define UTCA_FLASH_BLOCK_A        1     /* BLOCK A */
#define UTCA_FLASH_BLOCK_B        2     /* BLOCK B */

/*--------- 智能卡相关限制 --------*/
#define UTCA_MAXLEN_SN            16U   /* 智能卡序列号的长度 */
#define UTCA_MAXLEN_PINCODE       6U    /* PIN码的长度 */
#define UTCA_MAXLEN_TVSPRIINFO    32U   /* 运营商私有信息的长度 */
#define UTCA_MAXNUM_OPERATOR      4U    /* 最多的运营商个数 */
#define UTCA_MAXNUM_ACLIST        18U   /* 智能卡内保存的每个运营商的用户特征个数 */
#define UTCA_MAXNUM_SLOT          20U   /* 卡存储的最大钱包数 */

#define UTCA_MAXNUM_CURTAIN_RECORD	80U /*高级预览节目记录的最大个数*/


#define UTCA_MAXNUM_IPPVP         300U  /* 智能卡保存最多IPPV节目的个数 */
#define UTCA_MAXNUM_PRICE         2U    /* 最多的IPPV价格个数 */

#define UTCA_MAXNUM_ENTITLE       300U  /* 授权产品的最大个数 */

/*---------- 机顶盒相关限制 ----------*/
#define UTCA_MAXNUM_PROGRAMBYCW   4U    /* 一个控制字最多解的节目数 */
#define UTCA_MAXNUM_ECM           8U    /* 同时接收处理的ECMPID的最大数量 */

#define UTCA_MAXNUM_DETITLE       5U    /* 每个运营商下可保存的反授权码个数 */

/*---------- CAS提示信息 ---------*/
#define UTCA_MESSAGE_CANCEL_TYPE      0x00  /* 取消当前的显示 */
#define UTCA_MESSAGE_BADCARD_TYPE     0x01  /* 无法识别卡 */
#define UTCA_MESSAGE_EXPICARD_TYPE    0x02  /* 智能卡过期，请更换新卡 */
#define UTCA_MESSAGE_INSERTCARD_TYPE  0x03  /* 加扰节目，请插入智能卡 */
#define UTCA_MESSAGE_NOOPER_TYPE      0x04  /* 卡中不存在节目运营商 */
#define UTCA_MESSAGE_BLACKOUT_TYPE    0x05  /* 条件禁播 */
#define UTCA_MESSAGE_OUTWORKTIME_TYPE 0x06  /* 当前时段被设定为不能观看 */
#define UTCA_MESSAGE_WATCHLEVEL_TYPE  0x07  /* 节目级别高于设定的观看级别 */
#define UTCA_MESSAGE_PAIRING_TYPE     0x08  /* 智能卡与本机顶盒不对应 */
#define UTCA_MESSAGE_NOENTITLE_TYPE   0x09  /* 没有授权 */
#define UTCA_MESSAGE_DECRYPTFAIL_TYPE 0x0A  /* 节目解密失败 */
#define UTCA_MESSAGE_NOMONEY_TYPE     0x0B  /* 卡内金额不足 */
#define UTCA_MESSAGE_ERRREGION_TYPE   0x0C  /* 区域不正确 */
#define UTCA_MESSAGE_NEEDFEED_TYPE    0x0D  /* 子卡需要和母卡对应，请插入母卡 */
#define UTCA_MESSAGE_ERRCARD_TYPE     0x0E  /* 智能卡校验失败，请联系运营商 */
#define UTCA_MESSAGE_UPDATE_TYPE      0x0F  /* 智能卡升级中，请不要拔卡或者关机 */
#define UTCA_MESSAGE_LOWCARDVER_TYPE  0x10  /* 请升级智能卡 */
#define UTCA_MESSAGE_VIEWLOCK_TYPE    0x11  /* 请勿频繁切换频道 */
#define UTCA_MESSAGE_MAXRESTART_TYPE  0x12  /* 智能卡暂时休眠，请5分钟后重新开机 */
#define UTCA_MESSAGE_FREEZE_TYPE      0x13  /* 智能卡已冻结，请联系运营商 */
#define UTCA_MESSAGE_CALLBACK_TYPE    0x14  /* 智能卡已暂停，请回传收视记录给运营商 */
#define UTCA_MESSAGE_CURTAIN_TYPE	  0x15 /*高级预览节目，该阶段不能免费观看*/
#define UTCA_MESSAGE_CARDTESTSTART_TYPE 0x16 /*升级测试卡测试中...*/
#define UTCA_MESSAGE_CARDTESTFAILD_TYPE 0x17 /*升级测试卡测试失败，请检查机卡通讯模块*/
#define UTCA_MESSAGE_CARDTESTSUCC_TYPE  0x18 /*升级测试卡测试成功*/
#define UTCA_MESSAGE_NOCALIBOPER_TYPE    0x19/*卡中不存在移植库定制运营商*/

#define UTCA_MESSAGE_STBLOCKED_TYPE   0x20  /* 请重启机顶盒 */
#define UTCA_MESSAGE_STBFREEZE_TYPE   0x21  /* 机顶盒被冻结 */



/*---------- 功能调用返回值定义 ----------*/
#define UTCA_RC_OK                    0x00  /* 成功 */
#define UTCA_RC_UNKNOWN               0x01  /* 未知错误 */
#define UTCA_RC_POINTER_INVALID       0x02  /* 指针无效 */
#define UTCA_RC_CARD_INVALID          0x03  /* 智能卡无效 */
#define UTCA_RC_PIN_INVALID           0x04  /* PIN码无效 */
#define UTCA_RC_DATASPACE_SMALL       0x06  /* 所给的空间不足 */
#define UTCA_RC_CARD_PAIROTHER        0x07  /* 智能卡已经对应别的机顶盒 */
#define UTCA_RC_DATA_NOT_FIND         0x08  /* 没有找到所要的数据 */
#define UTCA_RC_PROG_STATUS_INVALID   0x09  /* 要购买的节目状态无效 */
#define UTCA_RC_CARD_NO_ROOM          0x0A  /* 智能卡没有空间存放购买的节目 */
#define UTCA_RC_WORKTIME_INVALID      0x0B  /* 设定的工作时段无效 */
#define UTCA_RC_IPPV_CANNTDEL         0x0C  /* IPPV节目不能被删除 */
#define UTCA_RC_CARD_NOPAIR           0x0D  /* 智能卡没有对应任何的机顶盒 */
#define UTCA_RC_WATCHRATING_INVALID   0x0E  /* 设定的观看级别无效 */
#define UTCA_RC_CARD_NOTSUPPORT       0x0F  /* 当前智能卡不支持此功能 */
#define UTCA_RC_DATA_ERROR            0x10  /* 数据错误，智能卡拒绝 */
#define UTCA_RC_FEEDTIME_NOT_ARRIVE   0x11  /* 喂养时间未到，子卡不能被喂养 */
#define UTCA_RC_CARD_TYPEERROR        0x12  /* 子母卡喂养失败，插入智能卡类型错误 */

#define UTCA_RC_CAS_FAILED                  0x20 //发卡cas指令执行失败
#define UTCA_RC_OPER_FAILED                0x21 //发卡运营商指令执行失败


/*-- 读卡器中的智能卡状态 --*/
#define UTCA_SC_OUT         0x00    /* 读卡器中没有卡          */
#define UTCA_SC_REMOVING    0x01    /* 正在拔卡，重置状态      */
#define UTCA_SC_INSERTING   0x02    /* 正在插卡，初始化        */
#define UTCA_SC_IN          0x03    /* 读卡器中是可用的卡      */
#define UTCA_SC_ERROR       0x04    /* 读卡器的卡不能识别      */
#define UTCA_SC_UPDATE      0x05    /* 读卡器的卡可升级 */
#define UTCA_SC_UPDATE_ERR  0x06    /* 读卡器的卡升级失败      */

/*---------- ECM_PID设置的操作类型 ---------*/
#define UTCA_LIST_OK          0x00
#define UTCA_LIST_FIRST       0x01
#define UTCA_LIST_ADD         0x02


/*------------ IPPV/IPPT不同购买阶段提示 -------------*/
#define UTCA_IPPV_FREEVIEWED_SEGMENT  0x00  /* IPPV免费预览阶段，是否购买 */
#define UTCA_IPPV_PAYVIEWED_SEGMENT   0x01  /* IPPV收费阶段，是否购买 */
#define UTCA_IPPT_PAYVIEWED_SEGMENT   0x02  /* IPPT收费段，是否购买 */

/*------------ IPPV价格类型 ------------*/
#define UTCA_IPPVPRICETYPE_TPPVVIEW       0x0  /* 不回传，不录像类型 */
#define UTCA_IPPVPRICETYPE_TPPVVIEWTAPING 0x1  /* 不回传，可录像类型 */

/*------------ IPPV节目的状态 -----------*/
#define UTCA_IPPVSTATUS_BOOKING   0x01  /* 预定 */
#define UTCA_IPPVSTATUS_VIEWED    0x03  /* 已看 */


/*---------- 频道锁定应用相关定义 ---------*/
#define UTCA_MAXNUM_COMPONENT     5U    /* 节目组件最大个数 */
#define UTCA_MAXLEN_LOCKMESS      40U


/*---------- 反授权确认码应用相关定义 --------*/
#define UTCA_Detitle_All_Read     0x00  /* 所有反授权确认码已经被读，隐藏图标 */
#define UTCA_Detitle_Received     0x01  /* 收到新的反授权码，显示反授权码图标 */
#define UTCA_Detitle_Space_Small  0x02  /* 反授权码空间不足，改变图标状态提示用户 */
#define UTCA_Detitle_Ignore       0x03  /* 收到重复的反授权码，可忽略，不做处理 */


/*---------- 进度条提示信息 ---------*/
#define UTCA_SCALE_RECEIVEPATCH   1     /* 升级数据接收中 */
#define UTCA_SCALE_PATCHING       2     /* 智能卡升级中 */



/*-------------------------------------end of 宏定义--------------------------------------*/



/*----------------------------------------数据结构----------------------------------------*/

/*-- 系统时间 --*/
typedef UTCA_U32  UTCA_TIME;
typedef UTCA_U16  UTCA_DATE;

/*-- 信号量定义（不同的操作系统可能不一样）--*/
// typedef UTCA_U32  UTCA_Semaphore;

/*-- 运营商信息 --*/
typedef struct {
    char     m_szTVSPriInfo[UTCA_MAXLEN_TVSPRIINFO+1];  /* 运营商私有信息 */
    UTCA_U8  m_byReserved[3];    /* 保留 */
}SUTCAOperatorInfo;

/*-- 节目信息 --*/
/* Y10_update : 只需要ECMPID和ServiceID即可 */
typedef struct {
    UTCA_U16  m_wEcmPid;         /* 节目相应控制信息的PID */
    UTCA_U8   m_byServiceNum;    /* 当前PID下的节目个数 */
    UTCA_U8   m_byReserved;      /* 保留 */
    UTCA_U16  m_wServiceID[UTCA_MAXNUM_PROGRAMBYCW]; /* 当前PID下的节目ID列表 */
}SCDCASServiceInfo;

/*-- 授权信息 --*/
typedef struct {
    UTCA_U32  m_dwProductID;   /* 普通授权的节目ID */    
    UTCA_DATE m_tBeginDate;    /* 授权的起始时间 */
    UTCA_DATE m_tExpireDate;   /* 授权的过期时间 */
    UTCA_U8   m_bCanTape;      /* 用户是否购买录像：1－可以录像；0－不可以录像 */
    UTCA_U8   m_byReserved[3]; /* 保留 */
}SUTCAEntitle;

/*-- 授权信息集合 --*/
typedef struct {
    UTCA_U16      m_wProductCount;
    UTCA_U8       m_m_byReserved[2];    /* 保留 */
    SUTCAEntitle  m_Entitles[UTCA_MAXNUM_ENTITLE]; /* 授权列表 */
}SUTCAEntitles;

/*-- 钱包信息 --*/
typedef struct {
    UTCA_U32  m_wCreditLimit; /* 信用度（点数）*/
    UTCA_U32  m_wBalance;     /* 已花的点数 */
}SUTCATVSSlotInfo;

/*-- IPPV/IPPT节目的价格 --*/
typedef struct {
    UTCA_U16  m_wPrice;       /* 节目价格（点数）*/
    UTCA_U8   m_byPriceCode;  /* 节目价格类型 */
    UTCA_U8   m_byReserved;   /* 保留 */
}SUTCAIPPVPrice; 

/*-- IPPV/IPPT节目购买提示信息 --*/
typedef struct {
    UTCA_U32        m_dwProductID;          /* 节目的ID */
    UTCA_U16        m_wTvsID;               /* 运营商ID */
    UTCA_U8         m_bySlotID;             /* 钱包ID */
    UTCA_U8         m_byPriceNum;           /* 节目价格个数 */
    SUTCAIPPVPrice  m_Price[UTCA_MAXNUM_PRICE]; /* 节目价格 */
    union {
        UTCA_DATE   m_wExpiredDate;         /* 节目过期时间,IPPV用 */
        UTCA_U16    m_wIntervalMin;         /* 时间间隔，单位分钟,IPPT 用 */
    }m_wIPPVTime;
    UTCA_U8         m_byReserved[2];        /* 保留 */
}SUTCAIppvBuyInfo;

/*-- IPPV节目信息 --*/
typedef struct {
    UTCA_U32   m_dwProductID;   /* 节目的ID */
    UTCA_U8    m_byBookEdFlag;  /* 产品状态：BOOKING，VIEWED */ 
    UTCA_U8    m_bCanTape;      /* 是否可以录像：1－可以录像；0－不可以录像 */
    UTCA_U16   m_wPrice;        /* 节目价格 */
    UTCA_DATE  m_wExpiredDate;  /* 节目过期时间,IPPV用 */
    UTCA_U8    m_bySlotID;      /* 钱包ID */
    UTCA_U8    m_byReserved;    /* 保留 */
}SUTCAIppvInfo;



/*-- 频道锁定信息 --*/
/*-- 节目组件信息 --*/
typedef struct {    /* 组件用于通知机顶盒节目类型及PID等信息，一个节目可能包含多个组件 */
    UTCA_U16   m_wCompPID;     /* 组件PID */
    UTCA_U16   m_wECMPID;      /* 组件对应的ECM包的PID，如果组件是不加扰的，则应取0。 */
    UTCA_U8    m_CompType;     /* 组件类型 */
    UTCA_U8    m_byReserved[3];/* 保留 */
}SUTCAComponent;

/*-- 频道参数信息 --*/
typedef struct {    
    UTCA_U32   m_dwFrequency;              /* 频率，BCD码 */
    UTCA_U32   m_symbol_rate;              /* 符号率，BCD码 */
    UTCA_U16   m_wPcrPid;                  /* PCR PID */
    UTCA_U8    m_Modulation;               /* 调制方式 */
    UTCA_U8    m_ComponentNum;             /* 节目组件个数 */
    SUTCAComponent m_CompArr[UTCA_MAXNUM_COMPONENT];       /* 节目组件列表 */
    UTCA_U8    m_fec_outer;                /* 前项纠错外码 */
    UTCA_U8    m_fec_inner;                /* 前项纠错内码 */
    char       m_szBeforeInfo[UTCA_MAXLEN_LOCKMESS+1]; /* 保留 */
    char       m_szQuitInfo[UTCA_MAXLEN_LOCKMESS+1];   /* 保留 */
    char       m_szEndInfo[UTCA_MAXLEN_LOCKMESS+1];    /* 保留 */
}SUTCALockService;

/*-- 高级预览节目信息 --*/
typedef struct {
    UTCA_U16   m_wProgramID;        /* 节目的ID */
    UTCA_TIME  m_dwStartWatchTime;   /* 起始观看时间 */
    UTCA_U8    m_byWatchTotalCount;  /* 累计观看次数 */
    UTCA_U16   m_wWatchTotalTime;    /* 累计观看时长,(cp周期数) */
}SUTCACurtainInfo;


/*-----------------------------------------------------------------------------------
a. 本系统中，参数m_dwFrequency和m_symbol_rate使用BCD码，编码前取MHz为单位。
   编码时，前4个4-bit BCD码表示小数点前的值，后4个4-bit BCD码表示小数点后的值。
   例如：
        若频率为642000KHz，即642.0000MHz，则对应的m_dwFrequency的值应为0x06420000；
        若符号率为6875KHz，即6.8750MHz，则对应的m_symbol_rate的值应为0x00068750。

b. 本系统中，m_Modulation的取值如下：
    0       Reserved
    1       QAM16
    2       QAM32
    3       QAM64
    4       QAM128
    5       QAM256
    6～255  Reserved
------------------------------------------------------------------------------------*/ 


/*------------------------------------end of 数据结构-------------------------------------*/



/*---------------------------以下接口是CA_LIB提供给STB------------------------*/
#if 0
// 以下接口已由utcas_interface.h 定义并实现
/*------ CA_LIB调度管理 ------*/

/* CA_LIB初始化 */
UTCA_BOOL CDCASTB_Init( UTCA_U8 byThreadPrior );

/* 关闭CA_LIB，释放资源 */
void CDCASTB_Close( void );

/* UTCAS同密判断 */
extern UTCA_BOOL  CDCASTB_IsUTCA(UTCA_U16 wCaSystemID);

/*------ TS流管理 ------*/

/* 设置ECM和节目信息 */
extern void CDCASTB_SetEcmPid( UTCA_U8 byType,
                               const SCDCASServiceInfo* pServiceInfo );

/* 设置EMM信息 */
extern void  CDCASTB_SetEmmPid(UTCA_U16 wEmmPid);

/* 私有数据接收回调 */
extern void CDCASTB_PrivateDataGot( UTCA_U8        byReqID,
								  	UTCA_BOOL      bTimeout,
									UTCA_U16       wPid,
									const UTCA_U8* pbyReceiveData,
									UTCA_U16       wLen            );

/*------- 智能卡管理 -------*/

/* 插入智能卡 */
extern UTCA_BOOL CDCASTB_SCInsert( void );

/* 拔出智能卡*/
extern void CDCASTB_SCRemove( void );

/* 读取智能卡外部卡号 */
extern UTCA_U16 CDCASTB_GetCardSN( char* pCardSN );

#endif

/*------ Flash管理 ------ */

/* 存储空间的格式化 */
extern void CDCASTB_FormatBuffer( void );

/* 屏蔽对存储空间的读写操作 */
extern void CDCASTB_RequestMaskBuffer(void);

/* 打开对存储空间的读写操作 */
extern void CDCASTB_RequestUpdateBuffer(void);



/*------- 基本信息查询 -------*/

/* 查询CA_LIB版本号 */
extern UTCA_U32 CDCASTB_GetVer( void );

/* 查询机顶盒平台编号 */
extern UTCA_U16 CDCASTB_GetPlatformID( void );


/*------------------------以上接口是CA_LIB提供给STB---------------------------*/

/******************************************************************************/

/*------------------------以下接口是STB提供给CA_LIB---------------------------*/

/*-------- 线程管理 --------*/

/* 注册任务 */
extern UTCA_BOOL CDSTBCA_RegisterTask( const char* szName,
                                       UTCA_U8     byPriority,
                                       void*       pTaskFun,
                                       void*       pParam,
                                       UTCA_U16    wStackSize  );

/* 线程挂起 */
extern void CDSTBCA_Sleep(UTCA_U16 wMilliSeconds);




/*--------- 存储空间（Flash）管理 ---------*/

/* 读取存储空间 */
extern void CDSTBCA_ReadBuffer( UTCA_U8   byBlockID,
                                UTCA_U8*  pbyData,
                                UTCA_U32* pdwLen );

/* 写入存储空间 */
extern void CDSTBCA_WriteBuffer( UTCA_U8        byBlockID,
                                 const UTCA_U8* pbyData,
                                 UTCA_U32       dwLen );


/*-------- TS流管理 --------*/

/* 设置私有数据过滤器 */
extern UTCA_BOOL CDSTBCA_SetPrivateDataFilter( UTCA_U8        byReqID,  
											   const UTCA_U8* pbyFilter,  
											   const UTCA_U8* pbyMask, 
											   UTCA_U8        byLen, 
											   UTCA_U16       wPid, 
											   UTCA_U8        byWaitSeconds );


/* 释放私有数据过滤器 */
extern void CDSTBCA_ReleasePrivateDataFilter( UTCA_U8  byReqID,
                                              UTCA_U16 wPid );

/* 设置CW给解扰器 */
extern void CDSTBCA_ScrSetCW( UTCA_U16       wEcmPID,  
							  const UTCA_U8* pbyOddKey,  
							  const UTCA_U8* pbyEvenKey, 
							  UTCA_U8        byKeyLen, 
							  UTCA_BOOL      bTapingEnabled );


/*--------- 智能卡管理 ---------*/

/* 智能卡复位 */
extern UTCA_BOOL CDSTBCA_SCReset( UTCA_U8* pbyATR, UTCA_U8* pbyLen );

/* 智能卡通讯 */
extern UTCA_BOOL CDSTBCA_SCPBRun( const UTCA_U8* pbyCommand, 
								  UTCA_U16       wCommandLen,  
								  UTCA_U8*       pbyReply,  
								  UTCA_U16*      pwReplyLen  );

/* 智能卡发送数据 */
extern UTCA_BOOL SMSTBCA_SCSend( const UTCA_U8* pbyData,
								 UTCA_U32       wDataLen );

/* 智能卡接收数据 */
extern UTCA_BOOL SMSTBCA_SCRev( UTCA_U8*  pbyData,
								UTCA_U32 wDataLen );

extern UTCA_BOOL CDSTBCA_SCapdu(UTCA_U8* send_buf, UTCA_U32 send_len, UTCA_U8 *rec_buf, UTCA_U32 *rec_len);
								 
								 
/*-------- 安全控制 --------*/

/* 读取机顶盒唯一编号 */
extern void CDSTBCA_GetSTBID( UTCA_U16* pwPlatformID,
                              UTCA_U32* pdwUniqueID);

/* 安全芯片接口 */
extern UTCA_U16 CDSTBCA_SCFunction( UTCA_U8* pData);

/*-------- 其它 --------*/

/* 获取字符串长度 */
extern UTCA_U16 CDSTBCA_Strlen(const char* pString );

/* 调试信息输出 */
extern void CDSTBCA_Printf(UTCA_U8 byLevel, const char* szMesssage );


/*-------- 信号量管理 --------*/

/* 初始化信号量 */
void CDSTBCA_SemaphoreInit( UTCA_Semaphore* pSemaphore,
                                   UTCA_BOOL       bInitVal );
/* 信号量给予信号 */
void CDSTBCA_SemaphoreSignal( UTCA_Semaphore* pSemaphore );

/* 信号量获取信号 */
void CDSTBCA_SemaphoreWait( UTCA_Semaphore* pSemaphore );

void CDSTBCA_InitLock(int* lock);
void CDSTBCA_FreeLock(int lock);
void CDSTBCA_Lock(int lock);
void CDSTBCA_UnLock(int lock);

/*-------- 内存管理 --------*/

/* 分配内存 */
void* CDSTBCA_Malloc( UTCA_U32 byBufSize );

/* 释放内存 */
void  CDSTBCA_Free( void* pBuf );

/* 内存赋值 */
void  CDSTBCA_Memset( void*    pDestBuf,
                             UTCA_U8  c,
                             UTCA_U32 wSize );

/* 内存复制 */
void  CDSTBCA_Memcpy( void*       pDestBuf,
                             const void* pSrcBuf,
                             UTCA_U32    wSize );


/*---------------------------以上接口是STB提供给CA_LIB------------------------*/

#ifdef  __cplusplus
}
#endif
#endif
/*EOF*/

