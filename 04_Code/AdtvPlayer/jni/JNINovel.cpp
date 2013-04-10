/*
***************************************************************************************************
*  FileName    : JNINovel.cpp
*  Author      : jianglei      Date: 2012-3-9
*  Description : 
*--------------------------------------------------------------------------------------------------
*  History     :       
*  <time>        <version >   <author>   	<desc>
*  2012-3-9      V1.0.0       jianglei      JNI Frameworks
                                                                      
***************************************************************************************************
*/

#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <assert.h>
#include <jni.h>

static const char* LOG_TAG = "Novel-JNI_LOAD";



/*
 * Register several native methods for one class.
 */
static int registerNativeMethods(JNIEnv* env, const char* className,
    JNINativeMethod* gMethods, int NumOfMethods)
{
    jclass clazz;

    clazz = env->FindClass(className);
    if (clazz == NULL)
    {
        LOGE("%s Native registration unable to find class '%s'", LOG_TAG, className);
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        LOGE("%s RegisterNatives failed for '%s'", LOG_TAG, className);
        return JNI_FALSE;
    }

    env->DeleteLocalRef(clazz);

    return JNI_TRUE;
}

/*
 * Register native methods for all classes we know about.
 *
 * returns JNI_TRUE on success.
 */
static int registerNatives(JNIEnv* env)
{

	
	// 注册新播放的函数
	if (!registerNativeMethods(env, classPathNameJniChannelPlay,
		methodsJniChannelPlay, sizeof(methodsJniChannelPlay) / sizeof(methodsJniChannelPlay[0]))) {
		return JNI_FALSE;
	}
	#if 0
	// register epg model of jni
	if (!registerNativeMethods(env, classPathNameJniEPG,
		   methodsJniEPG,sizeof(methodsJniEPG) / sizeof(methodsJniEPG[0]))) {
		return JNI_FALSE;
	}
	// 注册搜索的函数
	if (!registerNativeMethods(env, classPathNameJniChannelSearch,
		   methodsJniChannelSearch,sizeof(methodsJniChannelSearch) / sizeof(methodsJniChannelSearch[0]))) {
		return JNI_FALSE;
	}
	#endif
	return JNI_TRUE;
}

/*
 * This is called by the VM when the shared library is first loaded.
 */
jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
  //  JniCommon::SetJavaVM(vm);

 //   (void) reserved;

    jint result = -1;
    JNIEnv* env = NULL;

    LOGI("%s JNI_OnLoad", LOG_TAG);

    if (vm->GetEnv((void**)&env, JNI_VERSION_1_4) != JNI_OK) {
        LOGE("%s GetEnv Failed", LOG_TAG);
        goto bail;
    }

    if (registerNatives(env) != JNI_TRUE) {
        LOGE("%s ERROR: registerNatives failed", LOG_TAG);
        goto bail;
    }

    LOGI("%s JNI Load Success", LOG_TAG);

    result = JNI_VERSION_1_4;

bail:
    return result;
}