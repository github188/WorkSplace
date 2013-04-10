#ifndef JOYSEE_TVEPG_H__
#define JOYSEE_TVEPG_H__

#include <tvcomm.h>

#if defined(WIN32)
	#ifdef TVEPG_EXPORTS
		#define TVEPG_API __declspec(dllexport)
	#else
		#define TVEPG_API __declspec(dllimport)
	#endif
#else
	#define TVEPG_API
#endif 

#ifdef __cplusplus
extern  "C" {
#endif

TVEPG_API bool tvepg_init(IN const U16 iServiceId,IN TVNOTIFY cbNotify);

TVEPG_API void tvepg_uninit();

TVEPG_API int  tvepg_putSectionData(IN const U16 pid,const U8 *pBuffer,IN const U32 iSize);

TVEPG_API bool tvepg_getEpgData(OUT EPGDataBaseT& epgs);

TVEPG_API bool tvepg_getEpgFilters(IN EITEventType evtType,OUT FilterListT& filters);

#ifdef __cplusplus
}
#endif 

#endif // defined(JOYSEE_TVEPG_H__)
