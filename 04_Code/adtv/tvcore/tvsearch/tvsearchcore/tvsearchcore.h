/*****************************************************************************
  Description: TVSearch Core Header
  Copyright(c) 2010-2015 Novel-SuperTV, All rights reserved.
*****************************************************************************/

#ifndef NOVELSUPERTV_TVCORE_H
#define NOVELSUPERTV_TVCORE_H
#include "itvsearchcore.h"
#include "searchDef.h"

///////////////////////////////////////////////////////////////////////////////
// 有关频道搜索 的操作
///////////////////////////////////////////////////////////////////////////////

// 开始频道搜索(异步)
UINT  AsyncStartSTV();
// 频道搜索完成回调函数
unsigned long AsyncStartSTV_CompleteCallBack(DVBServiceListT &serices, std::vector<ServiceTypeTableItem> &table);
void UpdateDVBService(DVBServiceListT& src);
#endif  // defined(NOVELSUPERTV_TVCORE_H)
