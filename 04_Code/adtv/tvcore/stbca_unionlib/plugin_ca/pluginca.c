#include "stdio.h"
#include "string.h"
#include "pluginca.h"
#include "shuma/shuma.h"
#include "gehua/gh.h"
// #include "DVN\DVN.h"


#ifdef  __cplusplus
extern "C" {
#endif

	/*
	*返回支持的CA个数
	*/
	int GetCA_Num()
	{
		return 1;
	}

	/*
	*Inter:返回CA接口指针
	*index:CA的索引，索引值小于GetCA_Num方法返回值减一
	*/
	void GetCAInterface(CA_Interface * Inter,const char* CA_Name)
	{
		/*
		if( strcmp(CA_Name,"DVN",3) == 0 ){
			DVN_GetCAInterface(Inter);
		}
		*/
		if( strcmp(CA_Name,"SHUMA") == 0 ){
			SM_GetCAInterface(Inter);
		}
		else if(strcmp(CA_Name, "GEHUA") == 0)
		{
			GH_GetCAInterface(Inter);
		}
	}

	/*------ CA_LIB调度管理 ------*/

/* CA_LIB初始化 */
UTCA_BOOL CA_Default_Init( UTCA_U8 byThreadPrior )
{
	return UTCA_FALSE;
}

/* 关闭CA_LIB，释放资源 */
void CA_Default_Close( void )
{
}

/* UTCAS同密判断 */
UTCA_BOOL  CA_Default_IsUTCA(UTCA_U16 wCaSystemID)
{
	return UTCA_FALSE;
}


/*------ Flash管理 ------ */

/* 存储空间的格式化 */
void CA_Default_FormatBuffer( void )
{
}

/* 屏蔽对存储空间的读写操作 */
void CA_Default_RequestMaskBuffer(void)
{
}

/* 打开对存储空间的读写操作 */
void CA_Default_RequestUpdateBuffer(void)
{
}


/*------ TS流管理 ------*/

/* 设置ECM和节目信息 */
void CA_Default_SetEcmPid( UTCA_U8  byType,
                               const SCDCASServiceInfo* pServiceInfo )
{
}

/* 设置EMM信息 */
void CA_Default_SetEmmPid(UTCA_U16 wEmmPid)
{
}

/* 私有数据接收回调 */
void CA_Default_PrivateDataGot( UTCA_U8        byReqID,
								  	UTCA_BOOL      bTimeout,
									UTCA_U16       wPid,
									const UTCA_U8* pbyReceiveData,
									UTCA_U16       wLen            )
{
}

/*------- 智能卡管理 -------*/

/* 插入智能卡 */
UTCA_BOOL CA_Default_SCInsert( void )
{
	return UTCA_TRUE;
}

/* 拔出智能卡*/
void CA_Default_SCRemove( void )
{
}

/* 读取智能卡外部卡号 */
UTCA_U16 CA_Default_GetCardSN( char* pCardSN )
{
	return 0;
}

/*------- 基本信息查询 -------*/

/* 查询CA_LIB版本号 */
UTCA_U32 CA_Default_GetVer( void )
{
	return 0;
}

/* 查询机顶盒平台编号 */
UTCA_U16 CA_Default_GetPlatformID( void )
{
	return 0;
}

void CA_Default_SetAtr(ATR_S* art)
{
}

UTCA_BOOL CA_Default_ParseEcm(UTCA_U8* EcmBuf,UTCA_U16  wEcmLen)
{
	return UTCA_TRUE;
}

void CA_Default_GetCW(UTCA_U8* cwOdd, UTCA_U8* cwEven)
{
}


char *MyPrintHex(UTCA_U8 * pBuf,UTCA_U16 size)
{
	int i;
	char buf[32];
	static char s_message[2048];
	
	s_message[0]=0;
	for(i=0;i<size;i++)
	{
		sprintf(buf,"%02x,",pBuf[i]);
		strcat(s_message,buf);
	}
	return  s_message;
}


#ifdef  __cplusplus
}
#endif
