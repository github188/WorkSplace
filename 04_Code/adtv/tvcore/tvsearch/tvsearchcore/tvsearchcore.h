/*****************************************************************************
  Description: TVSearch Core Header
  Copyright(c) 2010-2015 Novel-SuperTV, All rights reserved.
*****************************************************************************/

#ifndef NOVELSUPERTV_TVCORE_H
#define NOVELSUPERTV_TVCORE_H
#include "itvsearchcore.h"
#include "searchDef.h"

///////////////////////////////////////////////////////////////////////////////
// �й�Ƶ������ �Ĳ���
///////////////////////////////////////////////////////////////////////////////

// ��ʼƵ������(�첽)
UINT  AsyncStartSTV();
// Ƶ��������ɻص�����
unsigned long AsyncStartSTV_CompleteCallBack(DVBServiceListT &serices, std::vector<ServiceTypeTableItem> &table);
void UpdateDVBService(DVBServiceListT& src);
#endif  // defined(NOVELSUPERTV_TVCORE_H)
