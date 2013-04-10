#include "tsdemux.h"
#include "TsCatcher.h"

static TsCatcher controller_;

void tsdemux_enableDemux(bool enable)
{ 
	controller_.enableTs(enable);
}

// 添加TS数据过滤器
utHandle tsdemux_addTSFilter(
	utTsDataFilter const *filter,
	long filterCount,
	utFilterDataCallback dataHandle,
	utContext *context)
{
	return controller_.addTsPacketFilter(filter,filterCount,dataHandle,context);
}

// 删除TS数据过滤器，该方法为异步方法
void tsdemux_delTSFilter(
	utHandle hFilter,
	utFilterDestoryNotify destoryCallback)
{
	controller_.delTsPacketFilter(hFilter,destoryCallback);
}

// 添加表Section过滤器
utHandle tsdemux_addSectionFilter(
	utSectionFilter const *filter,
	long filterCount,
	utFilterDataCallback dataHandle,
	utContext *context)
{
	return controller_.addSectionFilter(filter,filterCount,dataHandle,context);
}

// 删除表Section过滤器
void tsdemux_delSectionFilter(
	utHandle hFilter,
	utFilterDestoryNotify destoryCallback)
{
	controller_.delSectionFilter(hFilter,destoryCallback);
}


