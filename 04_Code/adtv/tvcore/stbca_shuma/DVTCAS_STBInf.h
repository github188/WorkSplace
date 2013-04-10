//���ļ�����CAģ��ʵ���ṩ��STB���õĽӿ�

#ifndef _DVTCAS_STBINTERFACE_H_2004_12_31_
#define _DVTCAS_STBINTERFACE_H_2004_12_31_

#include "DVTCAS_STBDataPublic.h"

#ifdef  __cplusplus
extern "C" {
#endif 

/*-----------------------------------���½ӿ���CAS�ṩ��STB����----------------------------------*/

/*++
���ܣ�������������ʱ����ã���CASģ���ʼ����
����ֵ��
	true				��ʼ���ɹ���
	false				��ʼ��ʧ�ܡ�
--*/
bool DVTCASTB_Init(void);

/*++
���ܣ������л��CASID����øýӿ��ж�CASID�Ƿ���Ч�������Ч�򷵻�true�����򷵻�false��
������
	wCaSystemID:		�����з���SI/PSI��Ϣ��õ�CASID��
����ֵ��
	true				CASID��Ч
	false				CASID��Ч
--*/
bool DVTCASTB_CASIDVerify(WORD wCaSystemID);

/*++
���ܣ����û��忨ʱ��������������ã�֪ͨCASģ��
������
	byReaderNo:			�������ı��
--*/
void DVTCASTB_SCInsert(BYTE byReaderNo);

/*++
���ܣ����û��γ����ܿ�ʱ��������������ã�֪ͨCASģ�顣
--*/
void DVTCASTB_SCRemove(void);

/*++
���ܣ����õ�ǰ�����տ��Ľ�Ŀ����ϸ��Ϣ��
������
	pServiceInfo:		SDVTCAServiceInfo�ṹ��������Ŀ����ϸ��Ϣ��
˵����
	���û���Ƶ��ʱ�������б���Ҫ���ô˺����������ǰƵ��Ϊ�����ŵ�Ƶ������������m_wEcmPid��Ϊ0��
--*/
void DVTCASTB_SetCurEcmInfo(const SDVTCAServiceInfo * pServiceInfo);

/*++
���ܣ�����EMMPID��CASģ�齫�������õ�EMMPID��Ϣ����EMM���ݽ��մ���
������
	wEmmPid:			CAT���а�����descriptor�н���CA_system_ID��CA_PID������DVTCASTB_CASIDVerify()У����Ч��CA_PID��ΪEMM PID��
˵��:
	���������յ�CAT��󣬻�������Ҫ���ô˺�������EMM PID��������
--*/
void DVTCASTB_SetEmmPid(WORD wEmmPid);

/*++
���ܣ����������������յ�CAS˽�����ݻ���Time_Out����������ӿڽ������ṩ��CASģ�鴦��
������
	byReqID��				��ǰ������ȡ˽�б������ţ���DVTSTBCA_SetStreamGuardFilter���byReqID��Ӧ
	bSuccess��				��ȡ������û�гɹ����ɹ�Ϊtrue��ʧ��Ϊfalse
	wPID:					���յ����ݵ�����PID��
	byszReceiveData��		��ȡ��˽������
	wLen��					��ȡ����˽�����ݵĳ���
--*/
void DVTCASTB_StreamGuardDataGot(BYTE byReqID, bool bSuccess, WORD wPID, WORD wLen, const BYTE * byszReceiveData);

/*++
���ܣ��ж����ܿ���PIN��
������
	pPin:					PIN��
����ֵ��
	DVTCA_OK          				�ɹ�
	DVTCAERR_STB_PIN_INVALID		����PIN�����
	DVTCAERR_STB_PIN_LOCKED			PIN������
--*/
HRESULT DVTCASTB_VerifyPin(const SDVTCAPin * pPIN);

/*++
���ܣ��жϿ��Ƿ���ס��
������
	pbLocked:				���������true��ʾ��ס�ˣ�false��ʾû�С�
����ֵ��
	DVTCA_OK				�ɹ�
	����					ʧ��
--*/
HRESULT DVTCASTB_IsPinLocked(bool * pbLocked);

/*
���ܣ��ж��û��Ƿ�ͨ��pin����֤��
������
	pbPass:				���ʱtrue��ʾͨ�����ˣ�false����ʾû�С�
*/
HRESULT DVTCASTB_IsPinPass(bool * pbPass);

/*++
���ܣ��޸����ܿ���PIN�롣
������
	pNewPin:				�µ�PIN�롣	
����ֵ��
	DVTCA_OK          				�ɹ�
	DVTCAERR_STB_PIN_LOCKED			PIN������
˵��:
	���øú���֮ǰҪ��ȷ�����ù�VerifyPin�����ҳɹ���
--*/
HRESULT DVTCASTB_ChangePin(const SDVTCAPin * pNewPin);

/*++
���ܣ�������ܿ�Ŀǰ��߹ۿ�����
������
	pbyRating:				����ۿ�����
����ֵ��
	DVTCA_OK				�ɹ�
	����					ʧ��
˵��:
	�ۿ�����Ϊ3~18����15������ʼֵΪ18�������Թۿ����м���Ľ�Ŀ��
--*/
HRESULT DVTCASTB_GetRating(BYTE * pbyRating);

/*++
���ܣ��������ܿ��ۿ�����
������
	byRating:				Ҫ���õ��µĹۿ�����
����ֵ��
	DVTCA_OK				�ɹ�
	����					ʧ��
˵��:
	���øú���֮ǰҪ��ȷ�����ù�VerifyPin�����ҳɹ���
--*/
HRESULT DVTCASTB_SetRating(BYTE byRating);

/*++
���ܣ�������ܿ���ǰ���õĹ���ʱ��Ρ�
˵��������ʱ�ι���Ĭ�Ϲرգ���ʱ�����Ĺ���ʱ��ʼ����Ĭ��ֵ00:00-23:59���ַ���Ҫʱ��������ǰ�˿�����
������
	pbyStartHour			��ʼ������Сʱ
	pbyStartMinute			��ʼ�����ķ���
	pbyEndHour				����������Сʱ
	pbyEndMinute			���������ķ���
����ֵ��
	DVTCA_OK				�ɹ�
	����					ʧ��
˵��:
	����ʱ�γ�ʼֵΪ00:00~23:59����ȫ�춼���տ���
--*/
HRESULT DVTCASTB_GetWorkTime(BYTE * pbyStartHour, BYTE * pbyStartMinute, BYTE * pbyEndHour, BYTE * pbyEndMinute);

/*++
���ܣ��������ܿ�����ʱ�Ρ�
������
	byStartHour			��ʼ������Сʱ
	byStartMinute			��ʼ�����ķ���
	byEndHour				����������Сʱ
	byEndMinute			���������ķ���
����ֵ��
	DVTCA_OK				�ɹ�
	����					ʧ��
˵��:
	���øú���֮ǰҪ��ȷ�����ù�VerifyPin�����ҳɹ���
--*/
HRESULT DVTCASTB_SetWorkTime(BYTE byStartHour, BYTE byStartMinute, BYTE byEndHour, BYTE byEndMinute);


/*++
���ܣ����CAS��Ӧ����Ϣ��
������
	pManu					ָ�򱣴淵����Ϣ�������ַ��
����ֵ��
	DVTCA_OK				�ɹ�
	����					ʧ��
--*/
HRESULT DVTCASTB_GetStreamGuardManuInfo(SDVTCAManuInfo * pManu);

/*++
���ܣ����CAS��Ӫ����Ϣ��
������
	wTVSID:					CAS��Ӫ�̵�ID  ����ǣ�1 �򷵻����е���Ӫ����Ϣ
	pOperatorInfo:			ָ�򱣴淵����Ϣ�������ַ
	pbyCount:				��������������ĳ��ȣ�����ʱ��ʵ�ʷ�����Ӫ����Ϣ�ĸ���
����ֵ��
	DVTCA_OK				�ɹ�
	����					ʧ��
--*/
HRESULT DVTCASTB_GetOperatorInfo(WORD wTVSID, BYTE * pbyCount, SDVTCATvsInfo * pOperatorInfo);

/*++
���ܣ������ͨ��Ȩ��Ŀ���������������ڻ����н�������ʾ��
������
	wTVSID:					��Ӫ�̵��ڲ���š�
	pEntitleCount:			����ʱΪ����������������ʱΪʵ�ʻ�õĸ�����
	psEntitles:				���ص���Ȩ���顣						
����ֵ��
	DVTCA_OK					�ɹ�
	DVTCAERR_STB_TVS_NOT_FOUND	û���ҵ�����Ҫ�����Ӫ��
	����						ʧ��
--*/
HRESULT DVTCASTB_GetServiceEntitles(WORD wTVSID, BYTE * pEntitleCount, SDVTCAServiceEntitle * psEntitles);

/*++
���ܣ��򿪻�رյ�����Ϣ��
������
	bDebugMsgSign:			�򿪵�����Ϣ��־��1:�򿪵�����Ϣ;0:�رյ�����Ϣ��
--*/
void DVTCASTB_AddDebugMsgSign(bool bDebugMsgSign);

/*
���ܣ���������ʾ��һ��OSD��Ϣ��������ӿ���֪ͨCAģ�顣
����:	
	wDuration:				��Ϣ��ʾ�ľ���ʱ�䣬��λΪ�롣
--*/
void DVTCASTB_ShowOSDMsgOver(WORD wDuration);

/*++
����:�����л�ȡE-mail�����ӿڡ�
����:
	*pEmailCount:			��������E-mail�ܸ�����
	*pNewEmailCount:		��������δ������E-mail������
����ֵ��
	DVTCA_OK				�ɹ�
	����					ʧ��
--*/
HRESULT DVTCASTB_GetEmailCount(WORD * pEmailCount, WORD * pNewEmailCount);

/*++
����:�����л�ȡ���E-mail��Ҫ��Ϣ�ӿڡ�
����:
	*pEmailCount:			����ʱΪ�����ܸ��������ʱΪ��������E-mail�ܸ�����
	*pEmail:				��ȡ��E-mail���顣
����ֵ:
	DVTCA_OK:				��ȡE-mail�ɹ���
	DVTCAERR_STB_EMAILBOX_EMPTY:	E-mail����Ϊ�ա�
--*/
HRESULT DVTCASTB_GetEmailHead(WORD * pEmailCount, SDVTCAEmailInfo * pEmail);

/*++
����:�����л�ȡһ��E-mail�����ݡ�
����:
	tVersion:				E-mail�汾��
	*pEmail:				��ȡ��E-mail�ṹ��
����ֵ:
	DVTCA_OK:				��ȡE-mail�ɹ���
	DVTCAERR_STB_NO_EMAIL:	û�д�E-mail��
--*/
HRESULT DVTCASTB_GetEmailContent(DWORD tVersion, SDVTCAEmailContent * pEmail);

/*++
����:�����п���E-mail��֪ͨCAS�Ľӿڡ�
����:
	tVersion:				E-mail�汾��
����ֵ:
	DVTCA_OK:				����E-mailΪ�Ѷ��ɹ���
	����					ʧ��
--*/
HRESULT DVTCASTB_EmailRead(DWORD tVersion);

/*++
����:������ɾ��E-mail�ӿڡ�
����:
	tVersion:				E-mail�汾�����Ϊ-1����ʾɾ������������email������Ϊ�����id��
����ֵ:
	DVTCA_OK:				ɾ��E-mail�ɹ���
	DVTCAERR_STB_NO_EMAIL:	Ҫɾ����E-mail�����ڡ�
	����					ʧ��
--*/
HRESULT DVTCASTB_DelEmail(DWORD tVersion);

/*
���ܣ�����ĸ�������Ϣ��
������
	pLen:					����ʱΪ�������ݳ��ȣ����ʱ��Ϊʵ�ʵ�������ȡ�
	pData:					��������ݡ�
����ֵ��
	DVTCA_OK				�ɹ�
	����					ʧ��
*/
HRESULT DVTCASTB_GetCorrespondInfo(BYTE * pLen, BYTE * pData);

/*
���ܣ������ӿ������Ϣ
������
	DataLen:				��������ݳ��ȡ�
	pData:					��������ݡ�����������DVTCASTB_GetCorrespondInfo�õ����ݡ�
����ֵ��
	DVTCA_OK				�ɹ�
	����					ʧ��
*/
HRESULT DVTCASTB_SetCorrespondInfo(BYTE DataLen, const BYTE * pData);

//--------------------------------------------------------ipp begin----------------------------------------------------

/*
���ܣ��õ����п�Ԥ��/�˶���Ipp��Ŀ��
������
	pbyCount:				����Ϊ����ָ��ָ�����������ɽ�Ŀ���������Ϊ�õ��Ľ�Ŀ������
	pIppvs:					ipp��Ŀ����ָ�롣
����ֵ��
	DVTCA_OK				�ɹ�
	����					ʧ��
*/
HRESULT DVTCASTB_GetBookIpps(BYTE * pbyCount, SDVTCAIpp * pIpps);

/*
���ܣ�Ԥ��Ipp��Ŀ
������
	pIpp:						ҪԤ����ȷ�Ϲ����ipp��Ŀ��Ϣ��
	
����ֵ��
	DVTCA_OK					�ɹ�
	DVTCAERR_STB_MONEY_LACK		Ǯ���࣬��Ҫ��ʾ�û�"Ԥ���ɹ��������࣬�뼰ʱ��ֵ"
	DVTCAERR_STB_NEED_PIN_PASS	��Ҫͨ��PIN��֤
	DVTCAERR_STB_IC_COMMUNICATE	��IC��ͨѶ��������
	DVTCAERR_STB_TVS_NOT_FOUND	��Ӫ��ID��Ч
	DVTCAERR_STB_SLOT_NOT_FOUND	Ǯ��û�з���
	DVTCAERR_STB_VER_EXPIRED	��Ʒ�Ѿ�����
	DVTCAERR_STB_OPER_INVALID	��Ʒ�Ѿ����ڣ����ܲ���
	DVTCAERR_STB_NO_SPACE		û�пռ�
	DVTCAERR_STB_PROD_NOT_FOUND	��Ʒû�з���
	DVTCAERR_STB_PRICE_INVALID	�۸���Ч
	DVTCAERR_STB_UNKNOWN		δ֪����
*/
HRESULT DVTCASTB_BookIpp(const SDVTCAIpp * pIpp);

/*
���ܣ��õ������ѹۿ���Ipp��Ŀ��
������
	pbyCount:				����Ϊ����ָ��ָ�����������ɽ�Ŀ���������Ϊ�õ��Ľ�Ŀ������
	pIpps:					ippv��Ŀ����ָ�롣
����ֵ��
	DVTCA_OK				�ɹ�
	����					ʧ��
*/
HRESULT DVTCASTB_GetViewedIpps(BYTE * pbyCount, SDVTCAViewedIpp * pIpps);

/*++
���ܣ����˳���ʾ�û�Ԥ��IPP��Ʒ�Ŀ�ʱ�������е��ô˽ӿ�֪ͨCA��
������
	wEcmPid:				IPP��Ӧ��EcmPID
--*/
void DVTCASTB_InquireBookIppOver(WORD wEcmPid);

//--------------------------------------------------------ipp end----------------------------------------------------

/*
���ܣ��õ�ĳ����Ӫ�̵��ѻ���Ǯ����ʣ��Ǯ����
������
	wTVSID:					��Ӫ�̵��ڲ���š�
	pdwAllBalance:			����ѻ���Ǯ������λ�֡�
	pdwRemainder:			���ʣ��Ǯ������λ�֡�
����ֵ��
	DVTCA_OK				�ɹ�
	����					ʧ��
*/
HRESULT DVTCASTB_GetMoneyInfo(WORD wTVSID, DWORD * pdwAllBalance, DWORD * pdwRemainder);

/*
���ܣ�֪ͨCAģ�飬DVTSTBCA_SwitchChannel��ϡ�(��Ƶ����������ʹ��)
������
	flag==1:��ʱ
	flag==2:û��Ĭ��Ƶ�㡢����û�н�Ŀ
*/
void DVTCASTB_SwitchChannelOver(BYTE byFlag);

/*
����: ��ȡ������Ϣ
����:
	psAreaInfo				������Ϣ
����ֵ:
	0:						�ɹ�
	����ֵ:					ʧ��
*/	
HRESULT DVTCASTB_GetAreaInfo(SDVTCAAreaInfo * psAreaInfo);//weiye:2007.05.24

/*
����:��ȡ�û��Զ�������
����:
	byType					�Զ�����������
	pdwUserDefData			�û��Զ������ݵ�DWORD����ָ��
����ֵ:
	0:						�ɹ�
	����ֵ:					ʧ��
*/
HRESULT DVTCASTB_GetUserDefData(BYTE byType, DWORD *pdwUserDefData);

/*
����:�л���ʾ���ԣ�������CAģ���ʼ�������
����:
	byLanguage               ��������DVTCA_LANG_CHN_SIMΪ��������(Ĭ��) 	DVTCA_LANG_ENGΪӢ��
*/
void DVTCASTB_ChangeLanguage(BYTE byLanguage);

/*
����:�õ���ĸ����Ե���Ϣ
����:
	pdwMotherCardID    ��ȡĸ�����ţ���pdwMotherCardID��ֵΪ0�����ʾ��ǰ��Ϊĸ���� ���ֵ��Ϊ0����ʾ��ǰ��Ϊ�ӿ���ֵΪĸ�����š�
	                   
����ֵ:
	0:						�ɹ�
	����ֵ:					ʧ��
*/
#if defined(JS_USE_SHUMA_CALIB_TEST) || defined(JS_USE_SHUMA_CALIB_HUANGGANG) || defined(JS_USE_SHUMA_CALIB_NANPING)
HRESULT DVTCASTB_GetMotherInfo(DWORD *pdwMotherCardID);
#else
//#if defined(JS_USE_SHUMA_CALIB_YONGAN) || defined(JS_USE_SHUMA_CALIB_NINGDE)
// ����, ������Ҫʹ������ӿ�, ������������ӿ�
HRESULT DVTCASTB_GetMotherInfo(SDVTCACorresInfo *psCorresInfo);
#endif

/*----------------------------------���Ͻӿ���CAS�ṩ��STB����--------------------------------------------*/

#ifdef  __cplusplus
}
#endif
#endif  

