/*
***************************************************************************************************
*  FileName    : novel_supertv_dvb_jni_JniChannelPlay.h
*  Author      : dr      Date: 2012-03-09
*  Description :
*--------------------------------------------------------------------------------------------------
*  History     :
*  <time>        <version >   <author>   	<desc>
*  2012-03-09       V1.0.0       dr             first release
*
***************************************************************************************************
*/

#include <jni.h>

#ifndef _Included_novel_supertv_dvb_jni_JniChannelPlay
#define _Included_novel_supertv_dvb_jni_JniChannelPlay
namespace android{
//extend "C"{
//#endif
/*
 * Class:     JniChannelPlay
 * Method:    getUriString
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_getUriString
  (JNIEnv *, jobject);
//#ifdef __cplusplus



/*
 * Class:     novel_supertv_dvb_jni_JniChannelPlay
 * Method:    open
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_open
  (JNIEnv *, jobject);

/*
 * Class:     novel_supertv_dvb_jni_JniChannelPlay
 * Method:    close
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_close
  (JNIEnv *, jobject);

/*
 * Class:     novel_supertv_dvb_jni_JniChannelPlay
 * Method:    getTunerCount
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_getTunerCount
  (JNIEnv *, jobject);

/*
 * Class:     novel_supertv_dvb_jni_JniChannelPlay
 * Method:    tune
 * Signature: (IIII)Z
 */
JNIEXPORT jboolean JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_tune
  (JNIEnv *, jobject,jint,jint,jint,jint);

/*
 * Class:     novel_supertv_dvb_jni_JniChannelPlay
 * Method:    getLocked
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_getLocked
  (JNIEnv *, jobject,jint);

/*
 * Class:     novel_supertv_dvb_jni_JniChannelPlay
 * Method:    addTsFilter
 * Signature: (Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_addTsFilter
  (JNIEnv *, jobject,jstring,jint);

/*
 * Class:     novel_supertv_dvb_jni_JniChannelPlay
 * Method:    delTsFilter
 * Signature: (Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_delTsFilter
  (JNIEnv *, jobject,jstring,jint);

/*
 * Class:     novel_supertv_dvb_jni_JniChannelPlay
 * Method:    delAllTsFilter
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_delAllTsFilter
  (JNIEnv *, jobject);

/*
 * Class:     novel_supertv_dvb_jni_JniChannelPlay
 * Method:    addSectionFilter
 * Signature: (Ljava/lang/String;III)I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_addSectionFilter
  (JNIEnv *, jobject,jstring,jint,jint,jint);

/*
 * Class:     novel_supertv_dvb_jni_JniChannelPlay
 * Method:    delSectionFilter
 * Signature: (Ljava/lang/String;II)I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_delSectionFilter
  (JNIEnv *, jobject,jstring,jint,jint,jint);

/*
 * Class:     novel_supertv_dvb_jni_JniChannelPlay
 * Method:    delSectionAllFilter
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_delSectionAllFilter
  (JNIEnv *, jobject, jstring);

/*
 * Class:     novel_supertv_dvb_jni_JniChannelPlay
 * Method:    enableTS
 * Signature: (Z)I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_enableTS
  (JNIEnv *, jobject,jboolean);

/*
 * Class:     novel_supertv_dvb_jni_JniChannelPlay
 * Method:    setClientCallBack
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_setClientCallBack
  (JNIEnv *, jobject,jstring);

};
#endif
