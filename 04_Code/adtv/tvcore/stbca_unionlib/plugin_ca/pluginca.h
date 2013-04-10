#ifndef __PLUGIN_CA_H__
#define __PLUGIN_CA_H__
#include "../utcas_priv.h"
#include "../utcasa.h"

////////////////////////////////////////////////////////////////////////////////
// 函数指针类型定义
////////////////////////////////////////////////////////////////////////////////
/*------ CA_LIB调度管理 ------*/
/* CA_LIB初始化 */
typedef UTCA_BOOL (*CAFunc_Init)( UTCA_U8 byThreadPrior );
/* 关闭CA_LIB，释放资源 */
typedef void (*CAFunc_Close)( void );
/* UTCAS同密判断 */
typedef UTCA_BOOL  (*CAFunc_IsUTCA)(UTCA_U16 wCaSystemID);

/*------ Flash管理 ------ */
/* 存储空间的格式化 */
typedef void (*CAFunc_FormatBuffer)( void );
/* 屏蔽对存储空间的读写操作 */
typedef void (*CAFunc_RequestMaskBuffer)(void);
/* 打开对存储空间的读写操作 */
typedef void (*CAFunc_RequestUpdateBuffer)(void);

/*------ TS流管理 ------*/
/* 设置ECM和节目信息 */
typedef void (*CAFunc_SetEcmPid)( UTCA_U8  byType,
		const SCDCASServiceInfo* pServiceInfo );
/* 设置EMM信息 */
typedef void (*CAFunc_SetEmmPid)(UTCA_U16 wEmmPid);
/* 私有数据接收回调 */
typedef void (*CAFunc_PrivateDataGot)( UTCA_U8        byReqID,
		UTCA_BOOL      bTimeout,
		UTCA_U16       wPid,
		const UTCA_U8* pbyReceiveData,
		UTCA_U16       wLen            );

/*------- 智能卡管理 -------*/
/* 插入智能卡 */
typedef UTCA_BOOL (*CAFunc_SCInsert)( void );
/* 拔出智能卡*/
typedef void (*CAFunc_SCRemove)( void );
/* 读取智能卡外部卡号 */
typedef UTCA_U16 (*CAFunc_GetCardSN)( char* pCardSN );

/*------- 基本信息查询 -------*/
/* 查询CA_LIB版本号 */
typedef UTCA_U32 (*CAFunc_GetVer)( void );
/* 查询机顶盒平台编号 */
typedef UTCA_U16 (*CAFunc_GetPlatformID)( void );
typedef void (*CAFunc_SetAtr)(ATR_S* art);
typedef UTCA_BOOL (*CAFunc_ParseEcm)(UTCA_U8* EcmBuf,UTCA_U16  wEcmLen);
typedef UTCA_BOOL (*CAFunc_ParseEmm)(UTCA_U8* EmmBuf,UTCA_U16  wEmmLen);
typedef void (*CAFunc_GetCW)(UTCA_U8* cwOdd, UTCA_U8* cwEven);

////////////////////////////////////////////////////////////////////////////////
// CA_Interface 结构类型定义
////////////////////////////////////////////////////////////////////////////////
typedef struct {
	/* CA_LIB初始化 */
	CAFunc_Init CA_Init ;
	/* 关闭CA_LIB，释放资源 */
	CAFunc_Close CA_Close;
	/* UTCAS同密判断 */
	CAFunc_IsUTCA CA_IsUTCA;
	/*------ Flash管理 ------ */
	/* 存储空间的格式化 */
	CAFunc_FormatBuffer CA_FormatBuffer;
	/* 屏蔽对存储空间的读写操作 */
	CAFunc_RequestMaskBuffer CA_RequestMaskBuffer ;
	/* 打开对存储空间的读写操作 */
	CAFunc_RequestUpdateBuffer CA_RequestUpdateBuffer;
	/*------ TS流管理 ------*/
	/* 设置ECM和节目信息 */
	CAFunc_SetEcmPid CA_SetEcmPid;
	/* 设置EMM信息 */
	CAFunc_SetEmmPid CA_SetEmmPid;
	/* 私有数据接收回调 */
	CAFunc_PrivateDataGot CA_PrivateDataGot;
	/*------- 智能卡管理 -------*/
	/* 插入智能卡 */
	CAFunc_SCInsert CA_SCInsert;
	/* 拔出智能卡*/
	CAFunc_SCRemove CA_SCRemove;
	/* 读取智能卡外部卡号 */
	CAFunc_GetCardSN CA_GetCardSN;
	/*------- 基本信息查询 -------*/
	/* 查询CA_LIB版本号 */
	CAFunc_GetVer CA_GetVer;
	/* 查询机顶盒平台编号 */
	CAFunc_GetPlatformID CA_GetPlatformID;
	CAFunc_SetAtr CA_SetAtr;
	CAFunc_ParseEcm CA_ParseEcm;
	CAFunc_ParseEmm CA_ParseEmm;
	CAFunc_GetCW CA_GetCW;
}CA_Interface;

////////////////////////////////////////////////////////////////////////////////
// 导出的函数名称
////////////////////////////////////////////////////////////////////////////////
#ifdef  __cplusplus
extern "C" {
#endif
	int GetCA_Num();
    // 仅此函数有用，其它都无用
	void GetCAInterface(CA_Interface * Inter,const char* CA_Name);
	/*------ CA_LIB调度管理 ------*/
	/* CA_LIB初始化 */
	UTCA_BOOL CA_Default_Init( UTCA_U8 byThreadPrior );
	/* 关闭CA_LIB，释放资源 */
	void CA_Default_Close( void );
	/* UTCAS同密判断 */
	UTCA_BOOL  CA_Default_IsUTCA(UTCA_U16 wCaSystemID);

	/*------ Flash管理 ------ */
	/* 存储空间的格式化 */
	void CA_Default_FormatBuffer( void );
	/* 屏蔽对存储空间的读写操作 */
	void CA_Default_RequestMaskBuffer(void);
	/* 打开对存储空间的读写操作 */
	void CA_Default_RequestUpdateBuffer(void);

	/*------ TS流管理 ------*/
	/* 设置ECM和节目信息 */
	void CA_Default_SetEcmPid( UTCA_U8  byType,
			const SCDCASServiceInfo* pServiceInfo );
	/* 设置EMM信息 */
	void CA_Default_SetEmmPid(UTCA_U16 wEmmPid);
	/* 私有数据接收回调 */
	void CA_Default_PrivateDataGot( UTCA_U8        byReqID,
			UTCA_BOOL      bTimeout,
			UTCA_U16       wPid,
			const UTCA_U8* pbyReceiveData,
			UTCA_U16       wLen            );

	/*------- 智能卡管理 -------*/
	/* 插入智能卡 */
	UTCA_BOOL CA_Default_SCInsert( void );
	/* 拔出智能卡*/
	void CA_Default_SCRemove( void );
	/* 读取智能卡外部卡号 */
	UTCA_U16 CA_Default_GetCardSN( char* pCardSN );

	/*------- 基本信息查询 -------*/
	/* 查询CA_LIB版本号 */
	UTCA_U32 CA_Default_GetVer( void );
	/* 查询机顶盒平台编号 */
	UTCA_U16 CA_Default_GetPlatformID( void );
	void CA_Default_SetAtr(ATR_S* art);
	UTCA_BOOL CA_Default_ParseEcm(UTCA_U8* EcmBuf,UTCA_U16  wEcmLen);
	void CA_Default_GetCW(UTCA_U8* cwOdd, UTCA_U8* cwEven);

	
	char *MyPrintHex(UTCA_U8 * pBuf,UTCA_U16 size);
#ifdef  __cplusplus
}
#endif

#endif//__PLUGIN_CA_H__
