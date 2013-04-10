#include "tvcomm.h"
#include "tvcore_ex.h"
#include "capture_ex.h"

#define  LOG_TAG "libtvcore"
#include "tvlog.h"

//===========================智能卡管理接口================================
// 修改pin码(初始pin码为6 '0')
TVCORE_API U32  tvcore_ChangePinCodeEx(IN J_DataObject& pinCode)
{
	return stbca_change_pin_code_ex(pinCode);
}

// 获取观看级别
TVCORE_API void tvcore_GetWatchLevelEx(OUT J_DataObject& level)
{
	return stbca_get_watch_rating_ex(level);
}

// 设置观看级别
TVCORE_API U32	tvcore_SetWatchLevelEx(IN  J_DataObject& level)
{
	return stbca_set_watch_rating_ex(level);
}

// 设置观看时段
TVCORE_API U32 tvcore_SetWatchTimeEx(IN  J_DataObject& time)
{
	return stbca_set_watch_time_ex(time);
}

// 获取观看时间
TVCORE_API U16 tvcore_GetWatchTimeEx(OUT J_DataObject& time)
{
	return stbca_get_watch_time_ex(time);
}

// 获取授权信息列表
TVCORE_API U16 tvcore_GetAuthorizationEx(IN J_DataObject& operator_id, 
                                         OUT J_DataObject& entitles)
{
	return stbca_get_service_entitles_ex(operator_id,entitles);
}
// 获取智能卡号
TVCORE_API U32 tvcore_GetCardsnEx(OUT J_DataObject& sn)
{
	 return stbca_GetCardIDEx(sn);
}

// 获取机顶盒ID
TVCORE_API bool tvcore_GetSTBIdEx(OUT J_DataObject& stb_id)
{
	stbca_get_stbid_ex(stb_id);
	return true;
}

// 获取运营商ID
TVCORE_API U16	tvcore_GetOperatorIDEx(OUT J_DataObject& operator_ids)
{
	return stbca_get_operator_ids_ex(operator_ids);
}

// 获取运营商特征值  
TVCORE_API U16	tvcore_GetOperatorAcsEx(IN J_DataObject& operator_id, OUT J_DataObject& acs)
{
	return stbca_get_operator_acs_ex(operator_id,acs);
}

//========================== 邮件管理 ==========================================
// 获取邮件的头信息
TVCORE_API U32 tvcore_getEMailHeadsEx(OUT J_DataObject& email_heads)
{
	return stbca_get_email_heads_ex(email_heads);
}

// 获取指定邮件的头信息
TVCORE_API bool tvcore_getEMailHeadEx(IN J_DataObject& email_id,OUT J_DataObject& email)
{
	return stbca_get_email_head_ex(email_id,email);
}

// 获取指定邮件的内容
TVCORE_API U32 tvcore_getEMailContentEx(IN J_DataObject& email_id, OUT J_DataObject& email_content)
{
	return stbca_get_email_content_ex(email_id,email_content);
}

// 删除邮件
TVCORE_API U32 tvcore_delEMailEx(IN J_DataObject& email_id)
{
	return stbca_delete_email_ex(email_id);
}

// 查询邮箱使用情况
// uEmailNum:已收邮件的个数,uEmptyNum:还能接收邮件的个数(邮箱容量限制:100)
TVCORE_API void tvcore_getEMailSpaceInfoEx(OUT J_DataObject& email_num)
{
	stbca_get_email_space_ex(email_num);
}

// 通用接口
TVCORE_API U32  tvcore_SetParameters(IN int fnID,IN J_DataObject& input,OUT J_DataObject& output)
{
	return stbca_set_parameters(fnID,input,output);
}

TVCORE_API U32  tvcore_GetParameters(IN int fnID,IN J_DataObject& input,OUT J_DataObject& output)
{
	return stbca_get_parameters(fnID,input,output);
}

