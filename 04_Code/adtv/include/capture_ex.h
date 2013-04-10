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

// ��ȡSTBCA�İ汾
STBCA_API void stbca_version_ex(OUT J_DataObject& ver);

// ��ȡ��ǰ��CAS����.
STBCA_API void stbca_cas_type_ex(OUT J_DataObject& type);

// ��ȡSTBID��.
STBCA_API void stbca_get_stbid_ex(OUT J_DataObject& stb_id);

// ��ȡָ���ʼ�����.
STBCA_API U32 stbca_get_email_content_ex(IN J_DataObject& email_id, OUT J_DataObject& email_content);

// ��ȡ�����е��ʼ��������仹�������ɵ��ʼ���.
STBCA_API U32 stbca_get_email_space_ex(OUT J_DataObject& email_num);

// ��ȡָ���ʼ����ʼ�ͷ.
STBCA_API bool stbca_get_email_head_ex(IN J_DataObject& email_id,OUT J_DataObject& email);

// ��ȡ�����ʼ�ͷ.
STBCA_API U32 stbca_get_email_heads_ex(OUT J_DataObject& email_heads);

// ɾ��һ���ʼ�.
STBCA_API U32 stbca_delete_email_ex(IN J_DataObject& email_id);

// ================���ܿ�����====================================
// GetCardID��ȡ���ܿ�����.
STBCA_API U32 stbca_GetCardIDEx(OUT J_DataObject& sn);

// �޸�Pin��.
STBCA_API U32  stbca_change_pin_code_ex(IN J_DataObject& pinCode);

// ��ȡ/���ùۿ�����.
STBCA_API U32 	stbca_set_watch_rating_ex(IN  J_DataObject& level);
STBCA_API void  stbca_get_watch_rating_ex(OUT J_DataObject& level);

// ����/��ȡ�ۿ�ʱ��.
STBCA_API U32  stbca_set_watch_time_ex(IN  J_DataObject& time);
STBCA_API U32  stbca_get_watch_time_ex(OUT J_DataObject& time);

STBCA_API U32  stbca_set_paired_ex(IN J_DataObject& num);

// ��ȡ��Ӫ��ID����
STBCA_API U32 stbca_get_operator_ids_ex(OUT J_DataObject& operator_ids);

//��ȡ��Ӫ����Ϣ.
STBCA_API U32 stbca_get_operator_info_ex(IN J_DataObject& operator_id, OUT J_DataObject& info);

// ��ȡ��Ӫ�̵�����ֵ.
STBCA_API U32 stbca_get_operator_acs_ex(IN J_DataObject& operator_id, OUT J_DataObject& acs);

// ��ȡ��Ȩ�б�
STBCA_API U32 stbca_get_service_entitles_ex(IN J_DataObject& operator_id, OUT J_DataObject& entitles);

// ��ȡǮ��ID�б�
STBCA_API U32 stbca_get_purse_ids_ex(IN J_DataObject& operator_id,OUT J_DataObject& purse_ids);

// ��ȡǮ����Ϣ.
STBCA_API U32 stbca_get_purse_info_ex(IN J_DataObject& purse_id,OUT J_DataObject& purse_info);

STBCA_API U32 stbca_set_parameters(IN int fnID,IN J_DataObject& input,OUT J_DataObject& output);
STBCA_API U32 stbca_get_parameters(IN int fnID,IN J_DataObject& input,OUT J_DataObject& output);

#ifdef __cplusplus
}
#endif

#endif
