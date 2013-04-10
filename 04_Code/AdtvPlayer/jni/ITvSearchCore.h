/*****************************************************************************
  Description: TVSearch Core Interface
  Copyright(c) 2010-2015 Novel-SuperTV, All rights reserved.
*****************************************************************************/
#ifndef INTERFACE_TVSEARCHER_CORE_H
#define INTERFACE_TVSEARCHER_CORE_H

#define TVSEARCHCORE_API
typedef unsigned int RESULT;

#include "tvcomm.h"
#include "ISearchTVNotify.h"


TVSEARCHCORE_API RESULT StartSearchTV(IN STVMode iMode, IN TuningParam *pTuningParam,IN ISearchTVNotify *pNotify);

TVSEARCHCORE_API RESULT CancelSearchTV();

#endif  

