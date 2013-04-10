#ifndef JOYSEE_TVCOREEX_H
#define JOYSEE_TVCOREEX_H

#include "tvcomm.h"
#include "utils/DataObject.h"

#if defined(WIN32)

	#ifndef TVCORE_EXPORTS
		#define TVCORE_API __declspec(dllexport)
	#else
		#define TVCORE_API __declspec(dllimport)
	#endif 

#else
	
	#define TVCORE_API

#endif 

#ifdef __cplusplus
extern "C" {
#endif 

//===========================智能卡管理接口================================
// 修改pin码(初始pin码为6 '0')
TVCORE_API U32  tvcore_ChangePinCodeEx(IN J_DataObject& pinCode);
// 获取观看级别
TVCORE_API void tvcore_GetWatchLevelEx(OUT J_DataObject& level);
// 设置观看级别
TVCORE_API U32	tvcore_SetWatchLevelEx(IN  J_DataObject& level);

// 设置观看时段
TVCORE_API U32 tvcore_SetWatchTimeEx(IN  J_DataObject& time);
// 获取观看时间
TVCORE_API U16 tvcore_GetWatchTimeEx(OUT J_DataObject& time);
// 获取授权信息列表
TVCORE_API U16 tvcore_GetAuthorizationEx(IN J_DataObject& operator_id, 
                                         OUT J_DataObject& entitles);
// 获取智能卡号
TVCORE_API U32 tvcore_GetCardsnEx(OUT J_DataObject& sn);
// 获取机顶盒ID
TVCORE_API bool tvcore_GetSTBIdEx(OUT J_DataObject& stb_id);
// 获取运营商ID
TVCORE_API U16	tvcore_GetOperatorIDEx(OUT J_DataObject& operator_ids);
// 获取运营商特征值  
TVCORE_API U16	tvcore_GetOperatorAcsEx(IN J_DataObject& operator_id, OUT J_DataObject& acs);

//========================== 邮件管理 ==========================================
// 获取邮件的头信息
TVCORE_API U32 tvcore_getEMailHeadsEx(OUT J_DataObject& email_heads);
// 获取指定邮件的头信息
TVCORE_API bool tvcore_getEMailHeadEx(IN J_DataObject& email_id,OUT J_DataObject& email);
// 获取指定邮件的内容
TVCORE_API U32 tvcore_getEMailContentEx(IN J_DataObject& email_id, OUT J_DataObject& email_content);
// 删除邮件
TVCORE_API U32 tvcore_delEMailEx(IN J_DataObject& email_id);
// 查询邮箱使用情况
// uEmailNum:已收邮件的个数,uEmptyNum:还能接收邮件的个数(邮箱容量限制:100)
TVCORE_API void tvcore_getEMailSpaceInfoEx(OUT J_DataObject& email_num);
// 通用接口
TVCORE_API U32  tvcore_SetParameters(IN int fnID,IN J_DataObject& input,OUT J_DataObject& output);
TVCORE_API U32  tvcore_GetParameters(IN int fnID,IN J_DataObject& input,OUT J_DataObject& output);

#ifdef __cplusplus
}
#endif

#endif //defined(JOYSEE_TVCORE_H_)
