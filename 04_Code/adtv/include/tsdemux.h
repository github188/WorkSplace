#ifndef JOYSEE_TSDEMUX_H_
#define JOYSEE_TSDEMUX_H_

#include "tsdemux_def.h"

#if defined(WIN32)

	#ifdef  TSDEMUX_EXPORTS

	#define TSDEMUX_API __declspec(dllexport)

	#else

	#define TSDEMUX_API __declspec(dllimport)

	#endif 

#else

	#define TSDEMUX_API

#endif 

#ifdef __cplusplus
extern "C" {
#endif 

TSDEMUX_API void tsdemux_enableDemux(bool enable);
// 快速清除缓冲数据
TSDEMUX_API void tsdemux_reset();

// 添加TS数据过滤器
TSDEMUX_API utHandle tsdemux_addTSFilter(
	utTsDataFilter const *filter,
	long filterCount,
	utFilterDataCallback dataHandle,
	utContext *context);
// 删除TS数据过滤器，该方法为异步方法
TSDEMUX_API void tsdemux_delTSFilter(
	utHandle hFilter,
	utFilterDestoryNotify destoryCallback);

// 添加表Section过滤器
TSDEMUX_API utHandle tsdemux_addSectionFilter(
	utSectionFilter const *filter,
	long filterCount,
	utFilterDataCallback dataHandle,
	utContext *context);
// 删除表Section过滤器
TSDEMUX_API void tsdemux_delSectionFilter(
	utHandle hFilter,
	utFilterDestoryNotify destoryCallback);

#ifdef __cplusplus
}
#endif 

#endif //defined(JOYSEE_TSDEMUX_H_)
