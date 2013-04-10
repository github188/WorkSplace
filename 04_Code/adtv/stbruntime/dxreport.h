#ifndef NOVELSUPER_DXREPORT_H
#define NOVELSUPER_DXREPORT_H

#include <stdio.h>
#include <stdarg.h>

#if !defined(WIN32)
#include <android/log.h>  
#define LOG_ERR		ANDROID_LOG_ERROR
#define LOG_WARN 	ANDROID_LOG_WARN
#define LOG_INFO 	ANDROID_LOG_INFO
#define LOG_TRACE	ANDROID_LOG_VERBOSE
#else
#define LOG_ERR		3
#define LOG_WARN 	2
#define LOG_INFO 	1
#define LOG_TRACE	0

#endif

#ifdef __cplusplus
extern "C"
{
#endif
// 请在调用的cpp 文件中定义LOG_TAG 和LOG_LEVEL
// #define LOG_TAG "test"
//#define LOG_LEVEL LOG_TRACE
inline static void dxreport(const char *format, ... )
{
	va_list ap;
	char buf[1024]={0};    
	va_start(ap, format);

#if !defined(WIN32)
	vsnprintf(buf, 1024, format, ap);
	va_end(ap);
	__android_log_write(LOG_LEVEL, LOG_TAG, buf);
//	printf("[%s] %s",LOG_TAG, buf);
#else
	U32 iTag = strlen(LOG_TAG);
	strncpy(buf,LOG_TAG,iTag);
	strcat(buf," ");
	vsprintf(buf+iTag+1,format,ap);
	va_end(ap);
//	OutputDebugStringA(buf);
	printf("%s",buf);
#endif  
}

#ifdef __cplusplus
}
#endif

#endif

