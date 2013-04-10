/*****************************************************************************
  Description: TVSearch Core Interface
  Copyright(c) 2010-2015 Novel-SuperTV, All rights reserved.
*****************************************************************************/
#ifndef INTERFACE_TVSEARCHER_CORE_H
#define INTERFACE_TVSEARCHER_CORE_H

#ifdef _WINDOWS
#define _EXPORT
#if defined _EXPORT
	#define TVSEARCHCORE_API __declspec(dllexport)
#else
	#define TVSEARCHCORE_API __declspec(dllimport)
#endif
#else
	#define TVSEARCHCORE_API
#endif

#include "isearchtvnotify.h"

// ע:-1 : ��ʾ������Ӫ��ID��������,
//     0 : ��ʾʹ��Ĭ����Ӫ��ids[0]
//     n : ʵ�ʵ���Ӫ��id
const int GET_OPERATOR_ID = 0x100; // ��Ӫ��ID
// ������Ӫ������ֵ
const int SET_OPERATOR_ACS= 0x101; // ��Ӫ��������

// ע��֪ͨ�ص�����
// TVSEARCHCORE_API ULONG RegNotifyCallBack(IN ISearchTVNotify *pNotify);
// ��ʼ��������Ƶ��
TVSEARCHCORE_API ULONG StartSearchTV(IN STVMode iMode, IN TuningParam *pTuningParam,IN ISearchTVNotify *pNotify);
// ȡ��Ƶ������
TVSEARCHCORE_API ULONG CancelSearchTV();

// ����/��ȡͨ��ģʽ
TVSEARCHCORE_API int SetParameter(int key, const void* request,int reqLength);
TVSEARCHCORE_API int GetParameter(int key, void* reply,  int* replyLength);

#endif  

