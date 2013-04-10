/*
***************************************************************************************************
*  FileName    : novel_supertv_dvb_jni_JniChannelPlay.h
*  Author      : dr      Date: 2012-04-05
*  Description :
*--------------------------------------------------------------------------------------------------
*  History     :
*  <time>        <version >   <author>   	<desc>
*  2012-04-05       V1.0.0       dr             first release
*
***************************************************************************************************
*/

#include <jni.h>

#ifndef _Included_novel_supertv_dvb_jni_JniChannelSearch
#define _Included_novel_supertv_dvb_jni_JniChannelSearch
namespace android{

typedef enum tagSearchMode
{
	STVMODE_MANUAL,		
	STVMODE_FULL,		
	STVMODE_NIT	,	
	STVMODE_NIT_S,		
	STVMODE_MONITOR_PMT,
	STVMODE_NULL = -1
}STVMode;

typedef struct tagTuningParam
{
	U32 freq;

	U32 symb;

	// 0x00 16QAM
	// 0x01 32QAM
	// 0x02 64QAM
	// 0x03 128QAM
	// 0x04 256QAM
	U32 qam;

}TuningParam;

/*
 * Class:     novel_supertv_dvb_jni_JniChannelSearch
 * Method:    StartSearchTV
 * Signature: (I;Lnovel/supertv/dvb/jni/struct/TuningParam;Lnovel/supertv/dvb/jni/ISearchTVNotify;)I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelSearch_StartSearchTV
  (JNIEnv *, jobject, jint, jobject, jobject);

/*
 * Class:     novel_supertv_dvb_jni_JniChannelSearch
 * Method:    CancelSearchTV
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelSearch_CancelSearchTV
  (JNIEnv *, jobject);

/*
 * Class:     novel_supertv_dvb_jni_JniChannelSearch
 * Method:    getString
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_novel_supertv_dvb_jni_JniChannelSearch_getNameString
  (JNIEnv *, jobject);

};
#endif

