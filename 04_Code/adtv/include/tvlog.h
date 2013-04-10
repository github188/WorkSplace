#ifndef JOYSEE_TVLOG_H_
#define JOYSEE_TVLOG_H_

#include "typ.h"
#include <string>

#if !defined(WIN32)
	
	#include <android/log.h>  // NDK env,    LOCAL_LDLIBS := -llog 
	#include <utils/Log.h>    // android env,LOCAL_SHARED_LIBRARIES := libutils

	#define LOGDEBUG	ANDROID_LOG_DEBUG
	#define LOGINFO		ANDROID_LOG_INFO
	#define LOGDEF		ANDROID_LOG_FATAL
	#define LOGERR		ANDROID_LOG_ERROR
	#define LOGFATAL	ANDROID_LOG_FATAL
	#define LOGWARN		ANDROID_LOG_WARN
#else

	#define LOGDEF		(0x0)
	#define LOGDEBUG	(0x1)
	#define LOGINFO     (0x2)
	#define LOGWARN		(0x3)
	#define LOGERR		(0x4)
	#define LOGFATAL	(0x5)
#endif 
	
#ifdef __cplusplus
extern "C"{
#endif 
	
	static inline void OutDebugStringT(int level,const char *format, ... )
	{
		va_list ap;
		char buf[1024]={0};    
		va_start(ap, format);

#if !defined(WIN32)
		vsnprintf(buf, 1024, format, ap);
		va_end(ap);
		__android_log_write(level, LOG_TAG, buf);
#else
		size_t iTag = strlen(LOG_TAG);
		strncpy(buf,LOG_TAG,iTag);
		strcat(buf," ");
		vsprintf(buf+iTag+1,format,ap);
		va_end(ap);
		OutputDebugStringA(buf);
#endif  
	}
	
	#define LOGTRACE		OutDebugStringT
 
#ifdef __cplusplus
}
#endif 

#endif //define(JOYSEE_TVLOG_H_)
