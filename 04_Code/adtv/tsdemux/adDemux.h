#ifndef ANDROID_DEMUX_H_
#define ANDROID_DEMUX_H_

#include "DemuxFilterInf.h"

//#if defined(WIN32)
//
//#ifdef USBTV_EXPORTS
//#define USBTV_API __declspec(dllexport)
//#else
//#define USBTV_API __declspec(dllimport)
//#endif
//
//#else 
//
//#define USBTV_API
//
//#endif 
#define USBTV_API

#ifdef __cplusplus
extern "C"
{
#endif

bool USBTV_API android_utOpen();
void USBTV_API android_utClose();
int  USBTV_API android_utGetTunerCount();
bool USBTV_API android_utTune(long iTuner,long freq,long qam,long symb);
bool USBTV_API android_utGetLocked(long iTuner);

void USBTV_API android_utEnableTs(bool bEnable);

void USBTV_API android_utAddTsFilter(const char* name,long pid);
void USBTV_API android_utDelTsFilter(const char* name,long pid);
void USBTV_API android_utDelAllTsFilter();
void USBTV_API android_utAddSectionFilter(const char* name,long pid,long tid,long timeout);
void USBTV_API android_utDelSectionFilter(const char* name,long pid,long tid);
void USBTV_API android_utDelSectionAllFilter(const char* name);

void USBTV_API android_utSetFilterCallBack(const char* name,IDemuxFilter* fn);
void USBTV_API android_utSetPidControl(const long* pids,const long lCount);

#ifdef __cplusplus
}
#endif

#endif //defined(ANDROID_DEMUX_H_)
