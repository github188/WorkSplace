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

// 注:-1 : 表示忽略运营商ID和特征码,
//     0 : 表示使用默认运营商ids[0]
//     n : 实际的运营商id
const int GET_OPERATOR_ID = 0x100; // 运营商ID
// 设置运营商特征值
const int SET_OPERATOR_ACS= 0x101; // 运营商特征码

// 注册通知回调函数
// TVSEARCHCORE_API ULONG RegNotifyCallBack(IN ISearchTVNotify *pNotify);
// 开始搜索电视频道
TVSEARCHCORE_API ULONG StartSearchTV(IN STVMode iMode, IN TuningParam *pTuningParam,IN ISearchTVNotify *pNotify);
// 取消频道搜索
TVSEARCHCORE_API ULONG CancelSearchTV();

// 设置/获取通用模式
TVSEARCHCORE_API int SetParameter(int key, const void* request,int reqLength);
TVSEARCHCORE_API int GetParameter(int key, void* reply,  int* replyLength);

#endif  

