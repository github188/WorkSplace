#include <novel_supertv_dvb_jni_JniSetting.h>

#include <android/log.h>
#include <string.h>
#include <jni.h>

/*
 * Class:     novel_supertv_dvb_jni_JniSetting
 * Method:    getUriString
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_novel_supertv_dvb_jni_JniSetting_getUriString
  (JNIEnv *, jobject)
  {
    LOGI("jnisetting jni test ok!!!");
    return (env)->NewStringUTF("jnisetting jni test ok !!!");
  }

/*
 * Class:     novel_supertv_dvb_jni_JniSetting
 * Method:    tvcore_set_watch_time0
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniSetting_tvcore_1set_1watch_1time0
  (JNIEnv *, jobject, jint, jint)
  {
  }

/*
 * Class:     novel_supertv_dvb_jni_JniSetting
 * Method:    tvcore_set_watch_time1
 * Signature: (Ljava/lang/String;II)I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniSetting_tvcore_1set_1watch_1time1
  (JNIEnv *, jobject, jstring, jint, jint)
  {
  }

/*
 * Class:     novel_supertv_dvb_jni_JniSetting
 * Method:    tvcore_set_watch_time2
 * Signature: (Ljava/lang/String;IIIIII)I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniSetting_tvcore_1set_1watch_1time2
  (JNIEnv *, jobject, jstring, jint, jint, jint, jint, jint, jint)
  {
  }

/*
 * Class:     novel_supertv_dvb_jni_JniSetting
 * Method:    tvcore_change_pincode
 * Signature: (Ljava/lang/String;Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniSetting_tvcore_1change_1pincode
  (JNIEnv *, jobject, jstring, jstring)
  {
  }

/*
 * Class:     novel_supertv_dvb_jni_JniSetting
 * Method:    setVolume
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniSetting_setVolume
  (JNIEnv *, jobject, jint)
  {
  }

/*
 * Class:     novel_supertv_dvb_jni_JniSetting
 * Method:    setScreen
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniSetting_setScreen
  (JNIEnv *, jobject, jint)
  {
  }

/*
 * Class:     novel_supertv_dvb_jni_JniSetting
 * Method:    setLanguage
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniSetting_setLanguage
  (JNIEnv *, jobject, jint)
  {
  }

