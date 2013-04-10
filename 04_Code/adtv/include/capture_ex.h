#ifndef JOYSEE_CAPTUREEX_H
#define JOYSEE_CAPTUREEX_H

#include <typ.h>
#include <utils/DataObject.h>

#if defined(WIN32)
	#ifdef STBCA_EXPORTS
		#define STBCA_API __declspec(dllexport)
	#else
		#define STBCA_API __declspec(dllimport)
	#endif 
#else
	#define STBCA_API 
#endif 


#ifdef __cplusplus
extern "C"
{
#endif

// 获取STBCA的版本
STBCA_API void stbca_version_ex(OUT J_DataObject& ver);

// 获取当前的CAS类型.
STBCA_API void stbca_cas_type_ex(OUT J_DataObject& type);

// 获取STBID号.
STBCA_API void stbca_get_stbid_ex(OUT J_DataObject& stb_id);

// 获取指定邮件内容.
STBCA_API U32 stbca_get_email_content_ex(IN J_DataObject& email_id, OUT J_DataObject& email_content);

// 获取邮箱中的邮件数和邮箱还可以容纳的邮件数.
STBCA_API U32 stbca_get_email_space_ex(OUT J_DataObject& email_num);

// 获取指定邮件的邮件头.
STBCA_API bool stbca_get_email_head_ex(IN J_DataObject& email_id,OUT J_DataObject& email);

// 获取所有邮件头.
STBCA_API U32 stbca_get_email_heads_ex(OUT J_DataObject& email_heads);

// 删除一封邮件.
STBCA_API U32 stbca_delete_email_ex(IN J_DataObject& email_id);

// ================智能卡管理====================================
// GetCardID获取智能卡卡号.
STBCA_API U32 stbca_GetCardIDEx(OUT J_DataObject& sn);

// 修改Pin码.
STBCA_API U32  stbca_change_pin_code_ex(IN J_DataObject& pinCode);

// 获取/设置观看级别.
STBCA_API U32 	stbca_set_watch_rating_ex(IN  J_DataObject& level);
STBCA_API void  stbca_get_watch_rating_ex(OUT J_DataObject& level);

// 设置/获取观看时段.
STBCA_API U32  stbca_set_watch_time_ex(IN  J_DataObject& time);
STBCA_API U32  stbca_get_watch_time_ex(OUT J_DataObject& time);

STBCA_API U32  stbca_set_paired_ex(IN J_DataObject& num);

// 获取运营商ID序列
STBCA_API U32 stbca_get_operator_ids_ex(OUT J_DataObject& operator_ids);

//获取运营商信息.
STBCA_API U32 stbca_get_operator_info_ex(IN J_DataObject& operator_id, OUT J_DataObject& info);

// 获取运营商的特征值.
STBCA_API U32 stbca_get_operator_acs_ex(IN J_DataObject& operator_id, OUT J_DataObject& acs);

// 获取授权列表
STBCA_API U32 stbca_get_service_entitles_ex(IN J_DataObject& operator_id, OUT J_DataObject& entitles);

// 获取钱包ID列表
STBCA_API U32 stbca_get_purse_ids_ex(IN J_DataObject& operator_id,OUT J_DataObject& purse_ids);

// 获取钱包信息.
STBCA_API U32 stbca_get_purse_info_ex(IN J_DataObject& purse_id,OUT J_DataObject& purse_info);

STBCA_API U32 stbca_set_parameters(IN int fnID,IN J_DataObject& input,OUT J_DataObject& output);
STBCA_API U32 stbca_get_parameters(IN int fnID,IN J_DataObject& input,OUT J_DataObject& output);

#ifdef __cplusplus
}
#endif

#endif
