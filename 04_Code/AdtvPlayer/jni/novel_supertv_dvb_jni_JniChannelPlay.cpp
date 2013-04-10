/*
***************************************************************************************************
*  FileName    : novel_supertv_dvb_jni_JniChannelPlay.cpp
*  Author      : dr      Date: 2012-03-10
*  Description : 
*--------------------------------------------------------------------------------------------------
*  History     :
*  <time>        <version >   <author>   	<desc>
*  2012-03-10       V1.0.0       dr             first release
*
***************************************************************************************************
*/


#include<string.h>
#include <sys/types.h>
#include <unistd.h>
#include <grp.h>
#include <binder/IPCThreadState.h>
#include <binder/ProcessState.h>
#include <binder/IServiceManager.h>
#include <cutils/log.h>




#include <NovelDemuxFilter.h>
#include <private/android_filesystem_config.h>
#include <dvb_types.h>
#include <NovelPlayer.h>

#include <time.h>

#include <jni.h>
#include <android/log.h>

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#define  LOG_TAG    "jianglei"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)


//#include "novel_supertv_dvb_jni_JniChannelPlay.h"
namespace android{
extern "C" {
   JNIEXPORT jstring JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_getUriString(JNIEnv *env, jobject obj);
	JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_open(JNIEnv *env, jobject obj);
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_close(JNIEnv *env, jobject obj);
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_getTunerCount(JNIEnv *env, jobject obj);
JNIEXPORT jboolean JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_tune(JNIEnv *, jobject,jint,jint,jint,jint);
JNIEXPORT jboolean JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_getLocked(JNIEnv *, jobject,jint);

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_addTsFilter(JNIEnv *, jobject,jstring,jint);

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_delTsFilter(JNIEnv *, jobject,jstring,jint);


JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_delAllTsFilter(JNIEnv *, jobject);


JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_addSectionFilter(JNIEnv *, jobject,jstring,jint,jint,jint);

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_delSectionFilter(JNIEnv *, jobject,jstring,jint,jint,jint);

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_delSectionAllFilter(JNIEnv *, jobject, jstring);


JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_enableTS(JNIEnv *, jobject,jboolean);


JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_setClientCallBack(JNIEnv *, jobject,jstring);
};

/*
 * Class:     JniChannelPlay
 * Method:    getUriString
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_getUriString
  (JNIEnv *env, jobject obj)
{
    return (env)->NewStringUTF("http:192.168.11.168:8080/test.ts");
}

/*
 * Class:     novel_supertv_dvb_jni_JniChannelPlay
 * Method:    open
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_open
  (JNIEnv *env, jobject obj)
{
   sp<IServiceManager> sm = defaultServiceManager();
    sp<IBinder> binder = sm->getService(String16("android.NovelDemuxFilter"));
    sp<INovelDemuxFilter> service = interface_cast<INovelDemuxFilter>(binder);
    
    service->novelDemux_open();
    
    //int bRet = service->novelDemux_tune(0,698000,2,6875);
    
   // bRet = service->novelDemux_getLocked(0);
    //LOGI("JIANGLEIcalling novelDemux_tune() bRet=%d",bRet);
   // sp<INovelPlayer> player = new NovelPlayer();
    //service->novelDemux_setClientCallBack("DemuxPlayer");
   //LOGI("add ts filter setcallback");
    //service->novelDemux_addSectionFilter("DemuxPlayer",0x10,0x40,0xffffffff);
    #if 1
        //service->novelDemux_addTsFilter("DemuxPlayer",0x05dc);
//LOGI("add ts filter 5dc");
    //service->novelDemux_addTsFilter("DemuxPlayer",0x00);
    //service->novelDemux_addTsFilter("DemuxPlayer",0x05de);
    //service->novelDemux_addTsFilter("DemuxPlayer",0x05df);
    #else
        //service->novelDemux_addTsFilter("DemuxPlayer",0x0190);
    //service->novelDemux_addTsFilter("DemuxPlayer",0x00);
    //service->novelDemux_addTsFilter("DemuxPlayer",0x0192);
    //service->novelDemux_addTsFilter("DemuxPlayer",0x0193);
    #endif
        //service->novelDemux_setClientCallBack("tsplayer",player);
        
        //android_utSetFilterCallBack("ESStream",&DemuxFilter_);
        
        //android_utAddTsFilter("ESStream",0xca);
        
        //android_utAddTsFilter("ESStream",0xcb);
        
        //service->novelDemux_enableTS(true);
#if 0
        while(1)
        {
          
          //LOGI("looping...\n");
          sleep(2);
        }
        
        service->novelDemux_delTsFilter("ESStream",0xca);
        service->novelDemux_delTsFilter("ESStream",0xcb);
        LOGI("looping...close\n");
        service->novelDemux_close();
#endif
        
    return 1;
}




/*
 * Class:     novel_supertv_dvb_jni_JniChannelPlay
 * Method:    close
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_close
  (JNIEnv *, jobject)
{
    sp<IServiceManager> sm = defaultServiceManager();
    sp<IBinder> binder = sm->getService(String16("android.NovelDemuxFilter"));
    sp<INovelDemuxFilter> service = interface_cast<INovelDemuxFilter>(binder);

    service->novelDemux_close();
    return 1;
}

/*
 * Class:     novel_supertv_dvb_jni_JniChannelPlay
 * Method:    getTunerCount
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_getTunerCount
  (JNIEnv *env, jobject obj)
{
    return 0;
}

/*
 * Class:     novel_supertv_dvb_jni_JniChannelPlay
 * Method:    tune
 * Signature: (IIII)Z
 */
JNIEXPORT jboolean JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_tune
  (JNIEnv *env, jobject obj,jint tuner,jint fre,jint qam,jint sym)
{
    sp<IServiceManager> sm = defaultServiceManager();
    sp<IBinder> binder = sm->getService(String16("android.NovelDemuxFilter"));
    sp<INovelDemuxFilter> service = interface_cast<INovelDemuxFilter>(binder);

    int bRet = service->novelDemux_tune(tuner,fre,qam,sym);
    return true;
}

/*
 * Class:     novel_supertv_dvb_jni_JniChannelPlay
 * Method:    getLocked
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_getLocked
  (JNIEnv *env, jobject obj,jint lock)
{
    return false;
}

/*
 * Class:     novel_supertv_dvb_jni_JniChannelPlay
 * Method:    addTsFilter
 * Signature: (Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_addTsFilter
  (JNIEnv *env, jobject obj,jstring name,jint ts)
{
    sp<IServiceManager> sm = defaultServiceManager();
    sp<IBinder> binder = sm->getService(String16("android.NovelDemuxFilter"));
    sp<INovelDemuxFilter> service = interface_cast<INovelDemuxFilter>(binder);
    
    service->novelDemux_addTsFilter("DemuxPlayer",ts);
    //LOGI("add ts filter = "+ts);
        
    return 1;
}

/*
 * Class:     novel_supertv_dvb_jni_JniChannelPlay
 * Method:    delTsFilter
 * Signature: (Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_delTsFilter
  (JNIEnv *env, jobject obj,jstring name,jint ts)
{
    sp<IServiceManager> sm = defaultServiceManager();
    sp<IBinder> binder = sm->getService(String16("android.NovelDemuxFilter"));
    sp<INovelDemuxFilter> service = interface_cast<INovelDemuxFilter>(binder);
    
    service->novelDemux_delTsFilter("DemuxPlayer",ts);
    //LOGI("delete ts filter = "+ts);
    return 1;
}

/*
 * Class:     novel_supertv_dvb_jni_JniChannelPlay
 * Method:    delAllTsFilter
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_delAllTsFilter
  (JNIEnv *env, jobject obj)
{
    sp<IServiceManager> sm = defaultServiceManager();
    sp<IBinder> binder = sm->getService(String16("android.NovelDemuxFilter"));
    sp<INovelDemuxFilter> service = interface_cast<INovelDemuxFilter>(binder);
    
    service->novelDemux_delAllTsFilter();

    return 0;
}

/*
 * Class:     novel_supertv_dvb_jni_JniChannelPlay
 * Method:    addSectionFilter
 * Signature: (Ljava/lang/String;III)I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_addSectionFilter
  (JNIEnv *, jobject,jstring,jint,jint,jint)
{
    return 0;
}

/*
 * Class:     novel_supertv_dvb_jni_JniChannelPlay
 * Method:    delSectionFilter
 * Signature: (Ljava/lang/String;II)I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_delSectionFilter
  (JNIEnv *, jobject,jstring,jint,jint,jint)
{
    return 0;
}

/*
 * Class:     novel_supertv_dvb_jni_JniChannelPlay
 * Method:    delSectionAllFilter
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_delSectionAllFilter
  (JNIEnv *, jobject, jstring)
{
    return 0;
}

/*
 * Class:     novel_supertv_dvb_jni_JniChannelPlay
 * Method:    enableTS
 * Signature: (Z)I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_enableTS
  (JNIEnv *env, jobject obj,jboolean enable)
{
    sp<IServiceManager> sm = defaultServiceManager();
    sp<IBinder> binder = sm->getService(String16("android.NovelDemuxFilter"));
    sp<INovelDemuxFilter> service = interface_cast<INovelDemuxFilter>(binder);
        
    service->novelDemux_enableTS(enable);
    return 0;
}

/*
 * Class:     novel_supertv_dvb_jni_JniChannelPlay
 * Method:    setClientCallBack
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_setClientCallBack
  (JNIEnv *env, jobject obj,jstring name)
{
    sp<IServiceManager> sm = defaultServiceManager();
    sp<IBinder> binder = sm->getService(String16("android.NovelDemuxFilter"));
    sp<INovelDemuxFilter> service = interface_cast<INovelDemuxFilter>(binder);

    service->novelDemux_setClientCallBack("DemuxPlayer");

    //LOGI("add ts filter setcallback");
    return 0;
}

};
