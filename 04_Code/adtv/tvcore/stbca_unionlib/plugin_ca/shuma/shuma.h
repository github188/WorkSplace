#ifndef _SM_H_
#define _SM_H_
#ifdef  __cplusplus
extern "C" {
#endif
#include "../../utcas_interface.h"

	
int SM_sc_apdu_t0(UTCA_U8* send_buf, UTCA_U8 send_len, UTCA_U8 *rec_buf, UTCA_U8 *rec_len);

/*------ CA_LIB调度管理 ------*/

/* CA_LIB初始化 */
UTCA_BOOL SM_Init( UTCA_U8 byThreadPrior );
UTCA_BOOL GetCardNO(void);

/* 关闭CA_LIB，释放资源 */
void SM_Close( void );

/* UTCAS同密判断 */
UTCA_BOOL  SM_IsUTCA(UTCA_U16 wCaSystemID);


/*------ Flash管理 ------ */

/* 存储空间的格式化 */
void SM_FormatBuffer( void );

/* 屏蔽对存储空间的读写操作 */
void SM_RequestMaskBuffer(void);

/* 打开对存储空间的读写操作 */
void SM_RequestUpdateBuffer(void);


/*------ TS流管理 ------*/

/* 设置ECM和节目信息 */
void SM_SetEcmPid( UTCA_U8 byType,
                               const SCDCASServiceInfo* pServiceInfo );

/* 设置EMM信息 */
void SM_SetEmmPid(UTCA_U16 wEmmPid);

/* 私有数据接收回调 */
void SM_PrivateDataGot( UTCA_U8        byReqID,
								  	UTCA_BOOL      bTimeout,
									UTCA_U16       wPid,
									const UTCA_U8* pbyReceiveData,
									UTCA_U16       wLen            );

/*------- 智能卡管理 -------*/

/* 插入智能卡 */
UTCA_BOOL SM_SCInsert( void );

/* 拔出智能卡*/
void SM_SCRemove( void );

/* 读取智能卡外部卡号 */
UTCA_U16 SM_GetCardSN( char* pCardSN );


/*------- 基本信息查询 -------*/

/* 查询CA_LIB版本号 */
UTCA_U32 SM_GetVer( void );

/* 查询机顶盒平台编号 */
UTCA_U16 SM_GetPlatformID( void );


UTCA_BOOL SM_ParseEcm(UTCA_U8* EcmBuf,UTCA_U16  wEcmLen);

UTCA_BOOL SM_ParseEmm(UTCA_U8* EmmBuf,UTCA_U16  wEmmLen);

void SM_GetCW(UTCA_U8* cwOdd, UTCA_U8* cwEven);


void SM_GetCAInterface(CA_Interface * Inter);




#ifdef  __cplusplus
}
#endif
#endif /* _SM_H_ */
