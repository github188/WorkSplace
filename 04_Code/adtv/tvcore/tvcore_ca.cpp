#include "tvcomm.h"
#include "tvcore_ex.h"
#include "capture_ex.h"

#define  LOG_TAG "libtvcore"
#include "tvlog.h"

//===========================���ܿ�����ӿ�================================
// �޸�pin��(��ʼpin��Ϊ6 '0')
TVCORE_API U32  tvcore_ChangePinCodeEx(IN J_DataObject& pinCode)
{
	return stbca_change_pin_code_ex(pinCode);
}

// ��ȡ�ۿ�����
TVCORE_API void tvcore_GetWatchLevelEx(OUT J_DataObject& level)
{
	return stbca_get_watch_rating_ex(level);
}

// ���ùۿ�����
TVCORE_API U32	tvcore_SetWatchLevelEx(IN  J_DataObject& level)
{
	return stbca_set_watch_rating_ex(level);
}

// ���ùۿ�ʱ��
TVCORE_API U32 tvcore_SetWatchTimeEx(IN  J_DataObject& time)
{
	return stbca_set_watch_time_ex(time);
}

// ��ȡ�ۿ�ʱ��
TVCORE_API U16 tvcore_GetWatchTimeEx(OUT J_DataObject& time)
{
	return stbca_get_watch_time_ex(time);
}

// ��ȡ��Ȩ��Ϣ�б�
TVCORE_API U16 tvcore_GetAuthorizationEx(IN J_DataObject& operator_id, 
                                         OUT J_DataObject& entitles)
{
	return stbca_get_service_entitles_ex(operator_id,entitles);
}
// ��ȡ���ܿ���
TVCORE_API U32 tvcore_GetCardsnEx(OUT J_DataObject& sn)
{
	 return stbca_GetCardIDEx(sn);
}

// ��ȡ������ID
TVCORE_API bool tvcore_GetSTBIdEx(OUT J_DataObject& stb_id)
{
	stbca_get_stbid_ex(stb_id);
	return true;
}

// ��ȡ��Ӫ��ID
TVCORE_API U16	tvcore_GetOperatorIDEx(OUT J_DataObject& operator_ids)
{
	return stbca_get_operator_ids_ex(operator_ids);
}

// ��ȡ��Ӫ������ֵ  
TVCORE_API U16	tvcore_GetOperatorAcsEx(IN J_DataObject& operator_id, OUT J_DataObject& acs)
{
	return stbca_get_operator_acs_ex(operator_id,acs);
}

//========================== �ʼ����� ==========================================
// ��ȡ�ʼ���ͷ��Ϣ
TVCORE_API U32 tvcore_getEMailHeadsEx(OUT J_DataObject& email_heads)
{
	return stbca_get_email_heads_ex(email_heads);
}

// ��ȡָ���ʼ���ͷ��Ϣ
TVCORE_API bool tvcore_getEMailHeadEx(IN J_DataObject& email_id,OUT J_DataObject& email)
{
	return stbca_get_email_head_ex(email_id,email);
}

// ��ȡָ���ʼ�������
TVCORE_API U32 tvcore_getEMailContentEx(IN J_DataObject& email_id, OUT J_DataObject& email_content)
{
	return stbca_get_email_content_ex(email_id,email_content);
}

// ɾ���ʼ�
TVCORE_API U32 tvcore_delEMailEx(IN J_DataObject& email_id)
{
	return stbca_delete_email_ex(email_id);
}

// ��ѯ����ʹ�����
// uEmailNum:�����ʼ��ĸ���,uEmptyNum:���ܽ����ʼ��ĸ���(������������:100)
TVCORE_API void tvcore_getEMailSpaceInfoEx(OUT J_DataObject& email_num)
{
	stbca_get_email_space_ex(email_num);
}

// ͨ�ýӿ�
TVCORE_API U32  tvcore_SetParameters(IN int fnID,IN J_DataObject& input,OUT J_DataObject& output)
{
	return stbca_set_parameters(fnID,input,output);
}

TVCORE_API U32  tvcore_GetParameters(IN int fnID,IN J_DataObject& input,OUT J_DataObject& output)
{
	return stbca_get_parameters(fnID,input,output);
}

