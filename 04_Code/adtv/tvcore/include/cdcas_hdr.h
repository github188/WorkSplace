#ifndef CDCAS_CALIB_APIEX_H
#define CDCAS_CALIB_APIEX_H

#ifndef WIN32
#define __stdcall
#endif
#ifdef  __cplusplus
extern "C" {
#endif


/*-------------------------------------基本数据类型---------------------------------------*/
typedef unsigned  long  CDCA_U32;
typedef unsigned  short CDCA_U16;
typedef unsigned  char  CDCA_U8;
typedef signed long  CDCA_S32;

typedef CDCA_U8 CDCA_BOOL;

#define CDCA_TRUE    ((CDCA_BOOL)1)
#define CDCA_FALSE   ((CDCA_BOOL)0)

/*----------------------------------------宏定义--------------------------------------*/

/*--------- FLASH类型 -------*/
#define CDCA_FLASH_BLOCK_A        1     /* BLOCK A */
#define CDCA_FLASH_BLOCK_B        2     /* BLOCK B */

/*--------- 智能卡相关限制 --------*/
#define CDCA_MAXLEN_STBSN            12U   /* 机顶盒号的长度 */
#define CDCA_MAXLEN_SN            16U   /* 智能卡序列号的长度 */
#define CDCA_MAXLEN_PINCODE       6U    /* PIN码的长度 */
#define CDCA_MAXLEN_TVSPRIINFO    32U   /* 运营商私有信息的长度 */
#define CDCA_MAXNUM_OPERATOR      4U    /* 最多的运营商个数 */
#define CDCA_MAXNUM_ACLIST        18U   /* 智能卡内保存的每个运营商的用户特征个数 */
#define CDCA_MAXNUM_SLOT          20U   /* 卡存储的最大钱包数 */
#define CDCA_MAXNUM_CURTAIN_RECORD	80U /*高级预览节目记录的最大个数*/

#define CDCA_MAXNUM_IPPVP         300U  /* 智能卡保存最多IPPV节目的个数 */
#define CDCA_MAXNUM_PRICE         2U    /* 最多的IPPV价格个数 */

#define CDCA_MAXNUM_ENTITLE       300U  /* 授权产品的最大个数 */

/*---------- 机顶盒相关限制 ----------*/
#define CDCA_MAXNUM_PROGRAMBYCW   4U    /* 一个控制字最多解的节目数 */
#define CDCA_MAXNUM_ECM           8U    /* 同时接收处理的ECMPID的最大数量 */

#define CDCA_MAXNUM_DETITLE       5U    /* 每个运营商下可保存的反授权码个数 */

/*---------- CAS提示信息 ---------*/
#define CDCA_MESSAGE_CANCEL_TYPE      0x00  /* 取消当前的显示 */
#define CDCA_MESSAGE_BADCARD_TYPE     0x01  /* 无法识别卡 */
#define CDCA_MESSAGE_EXPICARD_TYPE    0x02  /* 智能卡过期，请更换新卡 */
#define CDCA_MESSAGE_INSERTCARD_TYPE  0x03  /* 加扰节目，请插入智能卡 */
#define CDCA_MESSAGE_NOOPER_TYPE      0x04  /* 卡中不存在节目运营商 */
#define CDCA_MESSAGE_BLACKOUT_TYPE    0x05  /* 条件禁播 */
#define CDCA_MESSAGE_OUTWORKTIME_TYPE 0x06  /* 当前时段被设定为不能观看 */
#define CDCA_MESSAGE_WATCHLEVEL_TYPE  0x07  /* 节目级别高于设定的观看级别 */
#define CDCA_MESSAGE_PAIRING_TYPE     0x08  /* 智能卡与本机顶盒不对应 */
#define CDCA_MESSAGE_NOENTITLE_TYPE   0x09  /* 没有授权 */
#define CDCA_MESSAGE_DECRYPTFAIL_TYPE 0x0A  /* 节目解密失败 */
#define CDCA_MESSAGE_NOMONEY_TYPE     0x0B  /* 卡内金额不足 */
#define CDCA_MESSAGE_ERRREGION_TYPE   0x0C  /* 区域不正确 */
#define CDCA_MESSAGE_NEEDFEED_TYPE    0x0D  /* 子卡需要和母卡对应，请插入母卡 */
#define CDCA_MESSAGE_ERRCARD_TYPE     0x0E  /* 智能卡校验失败，请联系运营商 */
#define CDCA_MESSAGE_UPDATE_TYPE      0x0F  /* 智能卡升级中，请不要拔卡或者关机 */
#define CDCA_MESSAGE_LOWCARDVER_TYPE  0x10  /* 请升级智能卡 */
#define CDCA_MESSAGE_VIEWLOCK_TYPE    0x11  /* 请勿频繁切换频道 */
#define CDCA_MESSAGE_MAXRESTART_TYPE  0x12  /* 智能卡暂时休眠，请5分钟后重新开机 */
#define CDCA_MESSAGE_FREEZE_TYPE      0x13  /* 智能卡已冻结，请联系运营商 */
#define CDCA_MESSAGE_CALLBACK_TYPE    0x14  /* 智能卡已暂停，请回传收视记录给运营商 */
#define CDCA_MESSAGE_CURTAIN_TYPE	  0x15 /*高级预览节目，该阶段不能免费观看*/
#define CDCA_MESSAGE_CARDTESTSTART_TYPE 0x16 /*升级测试卡测试中...*/
#define CDCA_MESSAGE_CARDTESTFAILD_TYPE 0x17 /*升级测试卡测试失败，请检查机卡通讯模块*/
#define CDCA_MESSAGE_CARDTESTSUCC_TYPE  0x18 /*升级测试卡测试成功*/
#define CDCA_MESSAGE_NOCALIBOPER_TYPE    0x19/*卡中不存在移植库定制运营商*/
#define CDCA_MESSAGE_STBLOCKED_TYPE   0x20  /* 请重启机顶盒 */
#define CDCA_MESSAGE_STBFREEZE_TYPE   0x21  /* 机顶盒被冻结 */

//CAS提示消息 for SHUMA
//把数码提示信息添加为永新的扩展,从0x10 + CDCA_MESSAGE_STBFREEZE_TYPE 开始
//所有消息最大值为unsigned char 256
#define CDCA_MESSAGE_DVTCA_MESSAGE_OFFSET		0x10
#define CDCA_MESSAGE_DVTCA_MESSAGE_BASE			(CDCA_MESSAGE_DVTCA_MESSAGE_OFFSET + CDCA_MESSAGE_STBFREEZE_TYPE)
                                                                              
#define DVTCA_RATING_TOO_LOW                    0           //收看级别不够
#define CDCA_MESSAGE_DVTCA_RATING_TOO_LOW		(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_RATING_TOO_LOW)

#define DVTCA_NOT_IN_WATCH_TIME                 1           //不在收看时段内
#define CDCA_MESSAGE_DVTCA_NOT_IN_WATCH_TIME		(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_NOT_IN_WATCH_TIME)

#define DVTCA_NOT_PAIRED                        2           //没有机卡对应
#define CDCA_MESSAGE_DVTCA_NOT_PAIRED			(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_NOT_PAIRED)

#define DVTCA_PLEASE_INSERT_CARD                4           //请插卡
#define CDCA_MESSAGE_DVTCA_PLEASE_INSERT_CARD		(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_PLEASE_INSERT_CARD)

#define DVTCA_NO_ENTITLE                        5           //没有购买此节目
#define CDCA_MESSAGE_DVTCA_NO_ENTITLE			(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_NO_ENTITLE)

#define DVTCA_PRODUCT_RESTRICT                  6           //运营商限制观看该节目
#define CDCA_MESSAGE_DVTCA_PRODUCT_RESTRICT		(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_PRODUCT_RESTRICT)

#define DVTCA_AREA_RESTRICT                     7           //运营商限制区域观看
#define CDCA_MESSAGE_DVTCA_AREA_RESTRICT		(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_AREA_RESTRICT)

//V2.1新增的提示信息
#define DVTCA_MOTHER_RESTRICT                   8           //此卡为子卡，已经被限制收看，请与母卡配对
#define CDCA_MESSAGE_DVTCA_MOTHER_RESTRICT		(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_MOTHER_RESTRICT)

#define DVTCA_NO_MONEY                          9           //余额不足，不能观看此节目，请及时充值
#define CDCA_MESSAGE_DVTCA_NO_MONEY			(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_NO_MONEY)

#define DVTCA_IPPV_NO_CONFIRM                   10          //此节目为IPPV节目，请到IPPV节目确认/取消购买菜单下确认购买此节目
#define CDCA_MESSAGE_DVTCA_IPPV_NO_CONFIRM		(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_IPPV_NO_CONFIRM)

#define DVTCA_IPPV_NO_BOOK                      11          //此节目为IPPV节目，您没有预订和确认购买，不能观看此节目
#define CDCA_MESSAGE_DVTCA_IPPV_NO_BOOK			(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_IPPV_NO_BOOK)

#define DVTCA_IPPT_NO_CONFIRM                   12          //此节目为IPPT节目，请到IPPT节目确认/取消购买菜单下确认购买此节目
#define CDCA_MESSAGE_DVTCA_IPPT_NO_CONFIRM		(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_IPPT_NO_CONFIRM)

#define DVTCA_IPPT_NO_BOOK                      13          //此节目为IPPT节目，您没有预订和确认购买，不能观看此节目
#define CDCA_MESSAGE_DVTCA_IPPT_NO_BOOK			(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_IPPT_NO_BOOK)

//xb:20050617
#define DVTCA_DATA_INVALID                      16          //数据无效，STB不做任何提示。卡密钥问题。
#define CDCA_MESSAGE_DVTCA_DATA_INVALID			(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_DATA_INVALID)

#define DVTCA_SC_NOT_SERVER                     18          //IC卡被禁止服务
#define CDCA_MESSAGE_DVTCA_SC_NOT_SERVER		(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_SC_NOT_SERVER)

#define DVTCA_KEY_NOT_FOUND                     20          //此卡未被激活，请联系运营商
#define CDCA_MESSAGE_DVTCA_KEY_NOT_FOUND		(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_KEY_NOT_FOUND)

#define DVTCA_IPPNEED_CALLBACK                  21          //请联系运营商回传IPP节目信息
#define CDCA_MESSAGE_DVTCA_IPPNEED_CALLBACK		(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_IPPNEED_CALLBACK)

#define DVTCA_FREE_PREVIEWING                   22          //用户您好，此节目您尚未购买，正在免费预览中
#define CDCA_MESSAGE_DVTCA_FREE_PREVIEWING		(CDCA_MESSAGE_DVTCA_MESSAGE_BASE + DVTCA_FREE_PREVIEWING)


/*---------- 功能调用返回值定义 ----------*/
#define CDCA_RC_OK                    0x00  /* 成功 */
#define CDCA_RC_UNKNOWN               0x01  /* 未知错误 */
#define CDCA_RC_POINTER_INVALID       0x02  /* 指针无效 */
#define CDCA_RC_CARD_INVALID          0x03  /* 智能卡无效 */
#define CDCA_RC_PIN_INVALID           0x04  /* PIN码无效 */
#define CDCA_RC_DATASPACE_SMALL       0x06  /* 所给的空间不足 */
#define CDCA_RC_CARD_PAIROTHER        0x07  /* 智能卡已经对应别的机顶盒 */
#define CDCA_RC_DATA_NOT_FIND         0x08  /* 没有找到所要的数据 */
#define CDCA_RC_PROG_STATUS_INVALID   0x09  /* 要购买的节目状态无效 */
#define CDCA_RC_CARD_NO_ROOM          0x0A  /* 智能卡没有空间存放购买的节目 */
#define CDCA_RC_WORKTIME_INVALID      0x0B  /* 设定的工作时段无效 */
#define CDCA_RC_IPPV_CANNTDEL         0x0C  /* IPPV节目不能被删除 */
#define CDCA_RC_CARD_NOPAIR           0x0D  /* 智能卡没有对应任何的机顶盒 */
#define CDCA_RC_WATCHRATING_INVALID   0x0E  /* 设定的观看级别无效 */
#define CDCA_RC_CARD_NOTSUPPORT       0x0F  /* 当前智能卡不支持此功能 */
#define CDCA_RC_DATA_ERROR            0x10  /* 数据错误，智能卡拒绝 */
#define CDCA_RC_FEEDTIME_NOT_ARRIVE   0x11  /* 喂养时间未到，子卡不能被喂养 */
#define CDCA_RC_CARD_TYPEERROR        0x12  /* 子母卡喂养失败，插入智能卡类型错误 */
#define CDCA_RC_CAS_FAILED                  0x20 //发卡cas指令执行失败
#define CDCA_RC_OPER_FAILED                0x21 //发卡运营商指令执行失败
/*-- 读卡器中的智能卡状态 --*/
#define CDCA_SC_OUT         0x00    /* 读卡器中没有卡          */
#define CDCA_SC_REMOVING    0x01    /* 正在拔卡，重置状态      */
#define CDCA_SC_INSERTING   0x02    /* 正在插卡，初始化        */
#define CDCA_SC_IN          0x03    /* 读卡器中是可用的卡      */
#define CDCA_SC_ERROR       0x04    /* 读卡器的卡不能识别      */
#define CDCA_SC_UPDATE      0x05    /* 读卡器的卡可升级 */
#define CDCA_SC_UPDATE_ERR  0x06    /* 读卡器的卡升级失败      */

/*---------- ECM_PID设置的操作类型 ---------*/
#define CDCA_LIST_OK          0x00
#define CDCA_LIST_FIRST       0x01
#define CDCA_LIST_ADD         0x02


/*------------ 邮件大小及数量限制 ------------*/
#define CDCA_MAXNUM_EMAIL         100U  /* 机顶盒保存的最大邮件个数 */
#define CDCA_MAXLEN_EMAIL_TITLE   30U   /* 邮件标题的长度 */
#define CDCA_MAXLEN_EMAIL_CONTENT 160U  /* 邮件内容的长度 */

/*------------ 邮件图标显示方式 ------------*/
#define CDCA_Email_IconHide       0x00  /* 隐藏邮件通知图标 */
#define CDCA_Email_New            0x01  /* 新邮件通知，显示新邮件图标 */
#define CDCA_Email_SpaceExhaust   0x02  /* 磁盘空间以满，图标闪烁。 */

/*------------ OSD的长度限制 -----------*/
#define CDCA_MAXLEN_OSD           180U  /* OSD内容的最大长度 */

/*------------ OSD显示类型 ------------*/
#define CDCA_OSD_TOP              0x01  /* OSD风格：显示在屏幕上方 */
#define CDCA_OSD_BOTTOM           0x02  /* OSD风格：显示在屏幕下方 */
#define CDCA_OSD_FULLSCREEN       0x03  /* OSD风格：整屏显示 */
#define CDCA_OSD_HALFSCREEN       0x04  /* OSD风格：半屏显示 */


/*------------ IPPV/IPPT不同购买阶段提示 -------------*/
#define CDCA_IPPV_FREEVIEWED_SEGMENT  0x00  /* IPPV免费预览阶段，是否购买 */
#define CDCA_IPPV_PAYVIEWED_SEGMENT   0x01  /* IPPV收费阶段，是否购买 */
#define CDCA_IPPT_PAYVIEWED_SEGMENT   0x02  /* IPPT收费段，是否购买 */

/*------------ IPPV价格类型 ------------*/
#define CDCA_IPPVPRICETYPE_TPPVVIEW       0x0  /* 不回传，不录像类型 */
#define CDCA_IPPVPRICETYPE_TPPVVIEWTAPING 0x1  /* 不回传，可录像类型 */

/*------------ IPPV节目的状态 -----------*/
#define CDCA_IPPVSTATUS_BOOKING   0x01  /* 预定 */
#define CDCA_IPPVSTATUS_VIEWED    0x03  /* 已看 */


/*---------- 频道锁定应用相关定义 ---------*/
#define CDCA_MAXNUM_COMPONENT     5U    /* 节目组件最大个数 */
#define CDCA_MAXLEN_LOCKMESS      40U


/*---------- 反授权确认码应用相关定义 --------*/
#define CDCA_Detitle_All_Read     0x00  /* 所有反授权确认码已经被读，隐藏图标 */
#define CDCA_Detitle_Received     0x01  /* 收到新的反授权码，显示反授权码图标 */
#define CDCA_Detitle_Space_Small  0x02  /* 反授权码空间不足，改变图标状态提示用户 */
#define CDCA_Detitle_Ignore       0x03  /* 收到重复的反授权码，可忽略，不做处理 */


/*---------- 进度条提示信息 ---------*/
#define CDCA_SCALE_RECEIVEPATCH   1     /* 升级数据接收中 */
#define CDCA_SCALE_PATCHING       2     /* 智能卡升级中 */
#define CDCA_CURTAIN_BASE				   0x0000		/*高级预览状态码基础值,高字节编码0x00*/
#define CDCA_CURTAIN_CANCLE			   (CDCA_CURTAIN_BASE+0x00)  /*取消高级预览显示*/
#define CDCA_CURTAIN_OK    				   (CDCA_CURTAIN_BASE+0x01)  /*高级预览节目正常解密*/
#define CDCA_CURTAIN_TOTTIME_ERROR	   (CDCA_CURTAIN_BASE+0x02)  /*高级预览节目禁止解密：已经达到总观看时长*/
#define CDCA_CURTAIN_WATCHTIME_ERROR (CDCA_CURTAIN_BASE+0x03)  /*高级预览节目禁止解密：已经达到WatchTime限制*/
#define CDCA_CURTAIN_TOTCNT_ERROR 	   (CDCA_CURTAIN_BASE+0x04)  /*高级预览节目禁止解密：已经达到总允许观看次数*/
#define CDCA_CURTAIN_ROOM_ERROR 	   (CDCA_CURTAIN_BASE+0x05)  /*高级预览节目禁止解密：高级预览节目记录空间不足*/
#define CDCA_CURTAIN_PARAM_ERROR 	   (CDCA_CURTAIN_BASE+0x06)  /*高级预览节目禁止解密：节目参数错误*/
#define CDCA_CURTAIN_TIME_ERROR 		   (CDCA_CURTAIN_BASE+0x07)  /*高级预览节目禁止解密：数据错误*/

/*-------------------------------------end of 宏定义--------------------------------------*/



/*----------------------------------------数据结构----------------------------------------*/

/*-- 系统时间 --*/
typedef CDCA_U32  CDCA_TIME;
typedef CDCA_U16  CDCA_DATE;

/*-- 信号量定义（不同的操作系统可能不一样）--*/
typedef CDCA_U32  CDCA_Semaphore;

/*-- 运营商信息 --*/
typedef struct {
    char     m_szTVSPriInfo[CDCA_MAXLEN_TVSPRIINFO+1];  /* 运营商私有信息 */
    CDCA_U8  m_byReserved[3];    /* 保留 */
}SCDCAOperatorInfo;

/*-- 节目信息 --*/
/* Y10_update : 只需要ECMPID和ServiceID即可 */
typedef struct {
    CDCA_U16  m_wEcmPid;         /* 节目相应控制信息的PID */
    CDCA_U8   m_byServiceNum;    /* 当前PID下的节目个数 */
    CDCA_U8   m_byReserved;      /* 保留 */
    CDCA_U16  m_wServiceID[CDCA_MAXNUM_PROGRAMBYCW]; /* 当前PID下的节目ID列表 */
}SCDCASServiceInfo;

/*-- 授权信息 --*/
typedef struct {
    CDCA_U32  m_dwProductID;   /* 普通授权的节目ID */    
    CDCA_DATE m_tBeginDate;    /* 授权的起始时间 */
    CDCA_DATE m_tExpireDate;   /* 授权的过期时间 */
    CDCA_U8   m_bCanTape;      /* 用户是否购买录像：1－可以录像；0－不可以录像 */
    CDCA_U8   m_byReserved[3]; /* 保留 */
}SCDCAEntitle;

/*-- 授权信息集合 --*/
typedef struct {
    CDCA_U16      m_wProductCount;
    CDCA_U8       m_m_byReserved[2];    /* 保留 */
    SCDCAEntitle  m_Entitles[CDCA_MAXNUM_ENTITLE]; /* 授权列表 */
}SCDCAEntitles;

/*-- 钱包信息 --*/
typedef struct {
    CDCA_U32  m_wCreditLimit; /* 信用度（点数）*/
    CDCA_U32  m_wBalance;     /* 已花的点数 */
}SCDCATVSSlotInfo;

/*-- IPPV/IPPT节目的价格 --*/
typedef struct {
    CDCA_U16  m_wPrice;       /* 节目价格（点数）*/
    CDCA_U8   m_byPriceCode;  /* 节目价格类型 */
    CDCA_U8   m_byReserved;   /* 保留 */
}SCDCAIPPVPrice; 

/*-- IPPV/IPPT节目购买提示信息 --*/
typedef struct {
    CDCA_U32        m_dwProductID;          /* 节目的ID */
    CDCA_U16        m_wTvsID;               /* 运营商ID */
    CDCA_U8         m_bySlotID;             /* 钱包ID */
    CDCA_U8         m_byPriceNum;           /* 节目价格个数 */
    SCDCAIPPVPrice  m_Price[CDCA_MAXNUM_PRICE]; /* 节目价格 */
    union {
        CDCA_DATE   m_wExpiredDate;         /* 节目过期时间,IPPV用 */
        CDCA_U16    m_wIntervalMin;         /* 时间间隔，单位分钟,IPPT 用 */
    }m_wIPPVTime;
    CDCA_U8         m_byReserved[2];        /* 保留 */
}SCDCAIppvBuyInfo;

/*-- IPPV节目信息 --*/
typedef struct {
    CDCA_U32   m_dwProductID;   /* 节目的ID */
    CDCA_U8    m_byBookEdFlag;  /* 产品状态：BOOKING，VIEWED */ 
    CDCA_U8    m_bCanTape;      /* 是否可以录像：1－可以录像；0－不可以录像 */
    CDCA_U16   m_wPrice;        /* 节目价格 */
    CDCA_DATE  m_wExpiredDate;  /* 节目过期时间,IPPV用 */
    CDCA_U8    m_bySlotID;      /* 钱包ID */
    CDCA_U8    m_byReserved;    /* 保留 */
}SCDCAIppvInfo;


/*-- 邮件头 --*/
typedef struct {
    CDCA_U32   m_dwActionID;                 /* Email ID */
    CDCA_U32   m_tCreateTime;                /* EMAIL创建的时间 */
    CDCA_U16   m_wImportance;                /* 重要性： 0－普通，1－重要 */
    CDCA_U8    m_byReserved[2];              /* 保留 */
    char       m_szEmailHead[CDCA_MAXLEN_EMAIL_TITLE+1]; /* 邮件标题，最长为30 */    
    CDCA_U8    m_bNewEmail;                  /* 新邮件标记：0－已读邮件；1－新邮件 */
}SCDCAEmailHead;

/*-- 邮件内容 --*/
typedef struct {
    char     m_szEmail[CDCA_MAXLEN_EMAIL_CONTENT+1];      /* Email的正文 */
    CDCA_U8  m_byReserved[3];              /* 保留 */
}SCDCAEmailContent;


/*-- 频道锁定信息 --*/
/*-- 节目组件信息 --*/
typedef struct {    /* 组件用于通知机顶盒节目类型及PID等信息，一个节目可能包含多个组件 */
    CDCA_U16   m_wCompPID;     /* 组件PID */
    CDCA_U16   m_wECMPID;      /* 组件对应的ECM包的PID，如果组件是不加扰的，则应取0。 */
    CDCA_U8    m_CompType;     /* 组件类型 */
    CDCA_U8    m_byReserved[3];/* 保留 */
}SCDCAComponent;

/*-- 频道参数信息 --*/
typedef struct {    
    CDCA_U32   m_dwFrequency;              /* 频率，BCD码 */
    CDCA_U32   m_symbol_rate;              /* 符号率，BCD码 */
    CDCA_U16   m_wPcrPid;                  /* PCR PID */
    CDCA_U8    m_Modulation;               /* 调制方式 */
    CDCA_U8    m_ComponentNum;             /* 节目组件个数 */
    SCDCAComponent m_CompArr[CDCA_MAXNUM_COMPONENT];       /* 节目组件列表 */
    CDCA_U8    m_fec_outer;                /* 前项纠错外码 */
    CDCA_U8    m_fec_inner;                /* 前项纠错内码 */
    char       m_szBeforeInfo[CDCA_MAXLEN_LOCKMESS+1]; /* 保留 */
    char       m_szQuitInfo[CDCA_MAXLEN_LOCKMESS+1];   /* 保留 */
    char       m_szEndInfo[CDCA_MAXLEN_LOCKMESS+1];    /* 保留 */
}SCDCALockService;


/*-- 高级预览节目信息 --*/
typedef struct {
    CDCA_U16   m_wProgramID;        /* 节目的ID */
    CDCA_TIME  m_dwStartWatchTime;   /* 起始观看时间 */
    CDCA_U8    m_byWatchTotalCount;  /* 累计观看次数 */
    CDCA_U16   m_wWatchTotalTime;    /* 累计观看时长,(cp周期数) */
}SCDCACurtainInfo;
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
#ifdef  __cplusplus
}
#endif
#endif
/*EOF*/

