
#include <novel_supertv_dvb_jni_JniChannelPlay.h>

#include <android/log.h>

#include <jni.h>

//#include "utils/Log.h"

#define LOGI(...)  __android_log_print(ANDROID_LOG_ERROR, "jiangleitest", __VA_ARGS__)
namespace android{
#define NULL 0
//static const char* LOG_TAG = "JniLoad";


static const char *classPathNameJniChannelPlay = "novel/supertv/dvb/jni/JniChannelPlay";
static JNINativeMethod methodsJniChannelPlay[] = {
    {"getUriString","()Ljava/lang/String;",  (void*)Java_novel_supertv_dvb_jni_JniChannelPlay_getUriString },
    
    {"open",       "()I",  (void*)Java_novel_supertv_dvb_jni_JniChannelPlay_open },
    {"close",       "()I",  (void*)Java_novel_supertv_dvb_jni_JniChannelPlay_close },
    {"getTunerCount","()I",  (void*)Java_novel_supertv_dvb_jni_JniChannelPlay_getTunerCount },
    {"tune",         "(IIII)Z",  (void*)Java_novel_supertv_dvb_jni_JniChannelPlay_tune },
    {"getLocked",    "(I)Z",  (void*)Java_novel_supertv_dvb_jni_JniChannelPlay_getLocked },
    {"addTsFilter",  "(Ljava/lang/String;I)I",  (void*)Java_novel_supertv_dvb_jni_JniChannelPlay_addTsFilter },
    {"delTsFilter",  "(Ljava/lang/String;I)I",  (void*)Java_novel_supertv_dvb_jni_JniChannelPlay_delTsFilter },
    {"delAllTsFilter","()I",  (void*)Java_novel_supertv_dvb_jni_JniChannelPlay_delAllTsFilter },
    {"addSectionFilter","(Ljava/lang/String;III)I",  (void*)Java_novel_supertv_dvb_jni_JniChannelPlay_addSectionFilter },
    {"delSectionFilter","(Ljava/lang/String;II)I",  (void*)Java_novel_supertv_dvb_jni_JniChannelPlay_delSectionFilter },
    {"delSectionAllFilter","(Ljava/lang/String;)I",  (void*)Java_novel_supertv_dvb_jni_JniChannelPlay_delSectionAllFilter },
    {"enableTS",        "(Z)I",  (void*)Java_novel_supertv_dvb_jni_JniChannelPlay_enableTS },
    {"setClientCallBack","(Ljava/lang/String;)I",  (void*)Java_novel_supertv_dvb_jni_JniChannelPlay_setClientCallBack },
};

/*
 * Register several native methods for one class.
 */
static int registerNativeMethods(JNIEnv* env, const char* className,
    JNINativeMethod* gMethods, int numMethods)
{
    jclass clazz;

    clazz = env->FindClass(className);
    if (clazz == NULL)
    {
    		LOGI("registerNativeMethods Find class error--LINE=%d",__LINE__);
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
		LOGI("registerNativeMethods register native error--LINE=%d",__LINE__);
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

    // ע���²��ŵĺ���
    if (!registerNativeMethods(env, classPathNameJniChannelPlay,
                   methodsJniChannelPlay, sizeof(methodsJniChannelPlay) / sizeof(methodsJniChannelPlay[0]))) {
      return JNI_FALSE;
    }
	
    return JNI_TRUE;
}



/*
 * This is called by the VM when the shared library is first loaded.
 */
jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    
	//(void) reserved;
	LOGI("JNI_OnLoad--LINE=%d",__LINE__);
	jint result = -1;
	JNIEnv* env = NULL;

	if (vm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) {
		LOGI("JNI_OnLoad--LINE=%d",__LINE__);
		goto bail;
	}

	if (registerNatives(env) != JNI_TRUE) {
		LOGI("JNI_OnLoad--LINE=%d",__LINE__);
		goto bail;
	}

	result = JNI_VERSION_1_6;

bail:
    return result;
}
};
