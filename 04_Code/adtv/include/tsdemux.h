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
// ���������������
TSDEMUX_API void tsdemux_reset();

// ���TS���ݹ�����
TSDEMUX_API utHandle tsdemux_addTSFilter(
	utTsDataFilter const *filter,
	long filterCount,
	utFilterDataCallback dataHandle,
	utContext *context);
// ɾ��TS���ݹ��������÷���Ϊ�첽����
TSDEMUX_API void tsdemux_delTSFilter(
	utHandle hFilter,
	utFilterDestoryNotify destoryCallback);

// ��ӱ�Section������
TSDEMUX_API utHandle tsdemux_addSectionFilter(
	utSectionFilter const *filter,
	long filterCount,
	utFilterDataCallback dataHandle,
	utContext *context);
// ɾ����Section������
TSDEMUX_API void tsdemux_delSectionFilter(
	utHandle hFilter,
	utFilterDestoryNotify destoryCallback);

#ifdef __cplusplus
}
#endif 

#endif //defined(JOYSEE_TSDEMUX_H_)
