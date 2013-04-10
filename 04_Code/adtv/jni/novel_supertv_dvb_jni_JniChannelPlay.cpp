/*
***************************************************************************************************
*  FileName    : novel_supertv_dvb_jni_JniChannelPlay.cpp
*  Author      : chehl      Date: 2012-03-10
***************************************************************************************************
*/

#include <jni.h>
#include <android/log.h>

#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#include <tvcore.h>
#include "JDVBService.h"

#ifdef LOG_TAG
#undef LOG_TAG
#undef LOGI
#undef LOGE
#endif

#define  LOG_TAG    "chehl"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

static jclass gClsPlay;
static jclass gClsOsd;

static jmethodID glPfNotifyid = 0;
static jmethodID gMonitorCallback = NULL;

static jclass gClsFinger;

//{{其它文件中有引用
JavaVM * gJavaVM = NULL;

jclass gClsService = NULL;
jclass gClsEpgSearch = NULL;
jclass gClsMini = NULL;
jclass gClsMultiTree = NULL;
jclass gClsTreeNode = NULL;
//for jstring
jclass gClsString = NULL;
jmethodID gMedStringConID = 0;
jstring gObjStrEncoding = NULL;
//其它文件中有引用}}


#define JCLSNAME_MultiTree		"novel/supertv/dvb/jni/struct/MultiTree"
#define JCLSNAME_TreeNode		"novel/supertv/dvb/jni/struct/MultiTree$Node"

jstring CreateJstringFromGB2312(JNIEnv * env, const char * pStr)
{
    if(pStr == NULL)
		return NULL;

	int strLen = strlen(pStr);
	//如果长度为0返回空字符串
	if(strLen == 0)
		return env->NewStringUTF("");

	jbyteArray bytes = env->NewByteArray(strLen);
	env->SetByteArrayRegion(bytes, 0, strLen, (jbyte*)pStr);
	jstring retStr = (jstring)env->NewObject(gClsString, gMedStringConID, bytes, gObjStrEncoding);
	env->DeleteLocalRef(bytes);
	return retStr;
}

inline bool CheckJniException(JNIEnv * env, const char * file, int line)
{
	jthrowable exc = env->ExceptionOccurred();
	if(exc)
	{
		env->ExceptionClear();
		LOGI("CheckJniException find excetion at %s:%d...\n",file,line);
		return true;
	}
	return false;
}

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_start
  (JNIEnv *env, jobject obj ,jint vidiopid,jint vfat,jint audiopid,jint afat)
{
 	LOGI("Java_novel_supertv_dvb_jni_JniChannelPlay_start ERROR----");
	return 1;
}
 

/*
 * Class:     novel_supertv_dvb_jni_JniChannelPlay
 * Method:    open
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_open
  (JNIEnv *env, jobject obj)
{
	LOGI("Java_novel_supertv_dvb_jni_JniChannelPlay_open ");
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

	LOGI("Java_novel_supertv_dvb_jni_JniChannelPlay_close ERROR----");
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
	LOGI("Java_novel_supertv_dvb_jni_JniChannelPlay_tune ERROR----");
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
	LOGI("Java_novel_supertv_dvb_jni_JniChannelPlay_addTsFilter ERROR----");
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
	LOGI("Java_novel_supertv_dvb_jni_JniChannelPlay_delTsFilter ERROR----");
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
	LOGI("Java_novel_supertv_dvb_jni_JniChannelPlay_delAllTsFilter ERROR----");

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
	LOGI("Java_novel_supertv_dvb_jni_JniChannelPlay_delAllTsFilter ERROR----");
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
	LOGI("Java_novel_supertv_dvb_jni_JniChannelPlay_setClientCallBack ERROR----");
	return 0;
}


bool InitClassFieldID(JNIEnv * env)
{
    env->GetJavaVM(&gJavaVM);

	if(gClsPlay == NULL)
	{
		jclass gClsPlay1 = env->FindClass("novel/supertv/dvb/jni/JniChannelPlay");
		gClsPlay = (jclass)env->NewGlobalRef(gClsPlay1);
		env->DeleteLocalRef(gClsPlay1);
	}
	
	//查找string类
	if(gClsString == NULL)
	{
		jclass gClsString1 = env->FindClass("java/lang/String");
		gClsString = (jclass)env->NewGlobalRef(gClsString1);
		env->DeleteLocalRef(gClsString1);
	}
	//创建string字符编码对象
	if(gObjStrEncoding == NULL)
	{
		jstring encoding = env->NewStringUTF("GB2312");
		gObjStrEncoding = (jstring)env->NewGlobalRef(encoding);
		env->DeleteLocalRef(encoding);
	}
	//查找string类构造方法
	gMedStringConID = env->GetMethodID(gClsString, "<init>", "([BLjava/lang/String;)V");

	//查找DVBService类
	if(gClsService == NULL)
	{
		jclass clsService = env->FindClass("novel/supertv/dvb/jni/struct/tagDVBService");
		gClsService = (jclass)env->NewGlobalRef(clsService);
		env->DeleteLocalRef(clsService);
	}
	//查找OsdInfo类
	if(gClsOsd == NULL)
	{
		jclass clsOsd = env->FindClass("novel/supertv/dvb/jni/struct/tagOsdInfo");
		gClsOsd = (jclass)env->NewGlobalRef(clsOsd);
		env->DeleteLocalRef(clsOsd);
	}
	//查找Finger信息
	if(gClsFinger == NULL)
	{
		jclass clsFinger = env->FindClass("novel/supertv/dvb/jni/struct/CAFingerStruct");
		if(CheckJniException(env,__FILE__, __LINE__) == false)
		{
			gClsFinger = (jclass)env->NewGlobalRef(clsFinger);
			env->DeleteLocalRef(clsFinger);
		}
	}

	if(gClsEpgSearch == NULL)
	{
		jclass gClsEpgSearch1 = env->FindClass("novel/supertv/dvb/jni/JniEpgSearch");	
		if(CheckJniException(env,__FILE__, __LINE__) == false)
		{
			gClsEpgSearch = (jclass)env->NewGlobalRef(gClsEpgSearch1);
			env->DeleteLocalRef(gClsEpgSearch1);
		}
	}
	

	if(gClsMultiTree == NULL)
	{
		jclass clsTree = env->FindClass(JCLSNAME_MultiTree);
		if(CheckJniException(env,__FILE__, __LINE__) == false)
		{
			gClsMultiTree = (jclass)env->NewGlobalRef(clsTree);
			env->DeleteLocalRef(clsTree);
		}
	}

	if(gClsTreeNode == NULL)
	{
		jclass clsNode = env->FindClass(JCLSNAME_TreeNode);
		if(CheckJniException(env,__FILE__, __LINE__) == false)
		{
			gClsTreeNode = (jclass)env->NewGlobalRef(clsNode);
			env->DeleteLocalRef(clsNode);
		}
	}
	//查找MiniEPGNotify类	
	if(gClsMini == NULL)
	{
		jclass gClsMini1 = env->FindClass("novel/supertv/dvb/jni/struct/tagMiniEPGNotify");
		gClsMini = (jclass)env->NewGlobalRef(gClsMini1);
		env->DeleteLocalRef(gClsMini1);
	}
	
    glPfNotifyid = env->GetStaticMethodID(gClsPlay, "onPfCallBack", "(Lnovel/supertv/dvb/jni/struct/tagMiniEPGNotify;)V");
    gMonitorCallback = env->GetStaticMethodID(gClsPlay, "onMonitorCallBack", "(ILjava/lang/Object;)V");
	CheckJniException(env,__FILE__,__LINE__);

    return true;
}


static int DoMiNiEpgNotify(JNIEnv * currentEnv, long lParam, void * pParam)
{
	MiniEPGNotify* pfInfo = static_cast<MiniEPGNotify*>(pParam);
		
//	LOGI("tvcoreNotifyFuc pParam.CurrentEventStartTime = %u",pfInfo->CurrentEventStartTime);
//	LOGI("pfCall pParam.CurrentEventEndTime = %u",pfInfo->CurrentEventEndTime);
//	LOGI("tvcoreNotifyFucram.NextEventStartTime = %u",pfInfo->NextEventStartTime);
//	LOGI("tvcoreNotifyFucram.NextEventEndTime = %u",pfInfo->NextEventEndTime);
//	LOGI("tvcoreNotifyFuc pParam.CurrentEventName = %s",pfInfo->CurrentEventName);
//	LOGI("pfCall pParam.NextEventName = %s",pfInfo->NextEventName);
	
	//获取方法的ID，init是构造函数
	jmethodID tagPfMid = currentEnv->GetMethodID(gClsMini, "<init>", "()V");
	//创建实例
	jobject tagPfObj = currentEnv->NewObject(gClsMini, tagPfMid);
	//获取类中每一个变量的定义
	jfieldID serviceID_field = currentEnv->GetFieldID(gClsMini, "serviceId", "I");
	if(CheckJniException(currentEnv, __FILE__, __LINE__))
		serviceID_field = 0;

	jfieldID curName_field = currentEnv->GetFieldID(gClsMini, "CurrentEventName", "Ljava/lang/String;");
	jfieldID curStar_field = currentEnv->GetFieldID(gClsMini, "CurrentEventStartTime", "J");
	jfieldID curEnd_field = currentEnv->GetFieldID(gClsMini, "CurrentEventEndTime", "J");
	jfieldID nextName_field = currentEnv->GetFieldID(gClsMini, "NextEventName", "Ljava/lang/String;");
	jfieldID nextStar_field = currentEnv->GetFieldID(gClsMini, "NextEventStartTime", "J");
	jfieldID nextEnd_field = currentEnv->GetFieldID(gClsMini, "NextEventEndTime", "J");
	//Java对象变量赋值
	currentEnv->SetLongField(tagPfObj,curStar_field,pfInfo->CurrentEventStartTime);
	currentEnv->SetLongField(tagPfObj,curEnd_field,pfInfo->CurrentEventEndTime);
	currentEnv->SetLongField(tagPfObj,nextStar_field,pfInfo->NextEventStartTime);
	currentEnv->SetLongField(tagPfObj,nextEnd_field,pfInfo->NextEventEndTime);
	if(serviceID_field)
		currentEnv->SetIntField(tagPfObj,serviceID_field,pfInfo->ServiceID);
	
	jstring currentName = CreateJstringFromGB2312(currentEnv,pfInfo->CurrentEventName);
	currentEnv->SetObjectField(tagPfObj,curName_field,currentName);
	
	jstring nextName = CreateJstringFromGB2312(currentEnv,pfInfo->NextEventName);
	currentEnv->SetObjectField(tagPfObj,nextName_field,nextName);
	
//	LOGI("tvcoreNotifyFuc  CallStaticVoidMethod start %d\n",NS_GetTickCount());
	currentEnv->CallStaticVoidMethod(gClsPlay,glPfNotifyid,tagPfObj);
//	LOGI("tvcoreNotifyFuc  CallStaticVoidMethod end %d\n",NS_GetTickCount());
	currentEnv->DeleteLocalRef(tagPfObj);
	return 0;
}

static inline jobject CreateIntegerObj(JNIEnv * env, int param)
{
	jclass clsInt = env->FindClass("java/lang/Integer");
	jmethodID modConInt = env->GetMethodID(clsInt, "<init>", "(I)V");
	jobject retObj = env->NewObject(clsInt, modConInt, param);
	env->DeleteLocalRef(clsInt);
	return retObj;
}

static jobject CreateOSDObj(JNIEnv * env, long lParam, void * pParam)
{
	//1获取Java结构体对象
	jfieldID osdMsg_field = env->GetFieldID(gClsOsd, "osdMsg", "Ljava/lang/String;");
	jfieldID osdState_field = env->GetFieldID(gClsOsd, "osdPosAndState", "I");
	jmethodID osdConID = env->GetMethodID(gClsOsd, "<init>", "()V");
	//创建Java结构体对象
	jobject retObj = env->NewObject(gClsOsd,osdConID);
	//准备数据
	env->SetIntField(retObj,osdState_field,lParam);
	//创建JString
	jstring josdStr = CreateJstringFromGB2312(env, (char *)pParam);
	env->SetObjectField(retObj,osdMsg_field,josdStr);
	env->DeleteLocalRef(josdStr);

	return retObj;
}

static jobject CreateFingerObj(JNIEnv * env, long lParam, void * pParam)
{
	jobject objFinger = NULL;

	if(gClsFinger == NULL)
		return objFinger;

	
	jfieldID card_field = env->GetFieldID(gClsFinger, "card_id", "I");
	jfieldID ecmp_field = env->GetFieldID(gClsFinger, "ecmp_id", "I");
	jmethodID fingerConID = env->GetMethodID(gClsFinger, "<init>", "()V");

	objFinger = env->NewObject(gClsFinger,fingerConID);
	//准备数据
	env->SetIntField(objFinger,card_field,*(int*)pParam);
	env->SetIntField(objFinger,ecmp_field,lParam);
	return objFinger;
}

static jobject CreateDVBServiceObj(JNIEnv * env, long lParam, void * pParam)
{
	DVBService * dvbservice  = static_cast<DVBService*>(pParam);

	JDVBService fieldids;
	JDVBService::InitFieldIDs(env, gClsService, fieldids);
	jmethodID conidService = env->GetMethodID(gClsService, "<init>", "()V");
	jobject objtmpService = env->NewObject(gClsService, conidService);
	JDVBService::ConvertObject(env, fieldids, objtmpService, *dvbservice);
	return objtmpService;

}

jobject DoShumaNotifyCallBack(JNIEnv * env , int keyCode, void * param);

static int DoMonitorNotify(JNIEnv * env, int type, long lParam, void * pParam)
{
	LOGI("Enter DoMonitorNotify key %d, lParam %d\n",type, lParam);

	if(gMonitorCallback == NULL)
		return 1;

	jobject retObj = NULL;
	switch(type)
	{
	case TVNOTIFY_TUNER_SIGNAL: //向下执行,都需要向上层传递lParam参数
	case TVNOTIFY_ENTITLE_CHANGE:
	case TVNOTIFY_MAIL_NOTIFY:
	case TVNOTIFY_BUYMSG:
	case TVNOTIFY_SHOW_PROGRESSSTRIP:
	{
		retObj = CreateIntegerObj(env, lParam);
		break;
	}
	case TVNOTIFY_OSD:
	{
		retObj = CreateOSDObj(env, lParam, pParam);	
		break;
	}
	case TVNOTIFY_UPDATE_SERVICE:
	{
		retObj = CreateDVBServiceObj(env, lParam, pParam); 
		break;
	}
	case TVNOTIFY_SHOW_FINGERPRINT:
	{
		retObj = CreateFingerObj(env,lParam,pParam);
		break;
	}
	case TVNOTIFY_DETITLE:
	case TVNOTIFY_UPDATE_PROGRAM: 
		break;
#ifdef JS_USE_SHUMA_CALIB_NORMAL
	case SHUMA_MSG:
		retObj = DoShumaNotifyCallBack(env, lParam, pParam);
		type = lParam;
		break;
#endif
	default:
		LOGI("DoMonitorNotify default %d",type);
		break;
	}	
	LOGI("Call DoMonitorNotify key %d, lParam %d\n",type, lParam);

	env->CallStaticVoidMethod(gClsPlay,gMonitorCallback, type, retObj);
	if(retObj) env->DeleteLocalRef(retObj);
	return 1;
}

//extern
int DoEpgSearchComplete(JNIEnv * env, long lParam, void * pParam);

int tvcoreNotifyFuc(int notifyCode,long lParam,void *pParam)
{
	LOGI("tvcoreNotifyFuc notifyCode = %d ,lParam = %ld\n",notifyCode, lParam);
	int ret = 0;
	JNIEnv * currentEnv = NULL;
	bool attach = false;
	gJavaVM->GetEnv((void**) &currentEnv, JNI_VERSION_1_4);
	if(currentEnv == NULL)
	{
		LOGI("tvcoreNotifyFuc gJavaVM->AttachCurrentThread ");
		gJavaVM->AttachCurrentThread(&currentEnv, NULL);
		attach = true;
	}
	if(currentEnv == NULL)
	{
		LOGI("tvcoreNotifyFuc gJavaVM->AttachCurrentThread failed out...");
		return 0;
	}
	switch(notifyCode)
	{
	case TVNOTIFY_MINEPG:
		ret = DoMiNiEpgNotify(currentEnv, lParam, pParam);
		break;
	case TVNOTIFY_EPGCOMPLETE:
		ret = DoEpgSearchComplete(currentEnv, lParam, pParam);
		break;
	default:
		ret = DoMonitorNotify(currentEnv, notifyCode,lParam, pParam);
		break;
	}
	if(attach)
	{
		LOGI("tvcoreNotifyFuc  DetachCurrentThread() \n");
		gJavaVM->DetachCurrentThread();
	}
	LOGI("tvcoreNotifyFuc out... \n");
    return ret;
}




/*
 * Class:     novel_supertv_dvb_jni_JniChannelPlay
 * Method:    init
 * Signature: ()I
 */

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_init
  (JNIEnv *env, jobject obj)
{
    LOGI("Java_novel_supertv_dvb_jni_JniChannelPlay_init...\n");

    tvcore_init();

    tvcore_addTVNotify(tvcoreNotifyFuc);

    LOGI("tvcore_addTVNotify(tvcoreNotifyFuc)");


    InitClassFieldID(env);

    return 0;
}


/*
 * Class:     novel_supertv_dvb_jni_JniChannelPlay
 * Method:    uninit
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_uninit
  (JNIEnv *env, jobject obj)
{
	LOGI("Java_novel_supertv_dvb_jni_JniChannelPlay_uninit...\n");
	//tvcore_stop();
	tvcore_uninit();
	env->DeleteGlobalRef(gClsPlay);
	env->DeleteGlobalRef(gClsMini);
	env->DeleteGlobalRef(gClsEpgSearch);
	env->DeleteGlobalRef(gClsString);
	env->DeleteGlobalRef(gClsService);
	env->DeleteGlobalRef(gClsOsd);

	env->DeleteGlobalRef(gObjStrEncoding);
    return 0;
}


/*
 * Class:     novel_supertv_dvb_jni_JniChannelPlay
 * Method:    play
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_play
  (JNIEnv *env, jobject obj)
{
	LOGI("Java_novel_supertv_dvb_jni_JniChannelPlay_play...\n");

	tvcore_play();

    return 0;
}


/*
 * Class:     novel_supertv_dvb_jni_JniChannelPlay
 * Method:    stop
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_stop
  (JNIEnv *env, jobject obj)
{
	LOGI("Java_novel_supertv_dvb_jni_JniChannelPlay_stop...\n");

	tvcore_stop();

    return 0;
}


/*
 * Class:     novel_supertv_dvb_jni_JniChannelPlay
 * Method:    delAllTVNotify
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_delAllTVNotify
  (JNIEnv *env, jobject obj)
{
    LOGI("Java_novel_supertv_dvb_jni_JniChannelPlay_delAllTVNotify...\n");

    tvcore_delAllTVNotify();

    return 0;
}


JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_getTunerSignalStatus(JNIEnv * env, jobject obj)
{
	LOGI("Java_novel_supertv_dvb_jni_JniChannelPlay_getTunerSignalStatus\n");
	int ret = -1;
	TunerSignal status;
	memset(&status, 0, sizeof(TunerSignal));
	bool r = tvcore_get_tuner_status(status);
	if(r)
	{
		if(status.locked)
			ret = 0;
		else
			ret = 1;
	}
	return ret;
}

JNIEXPORT jboolean JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_visibleVideoLayer(JNIEnv * env, jobject obj, jboolean visible)
{
	LOGI("Java_novel_supertv_dvb_jni_JniChannelPlay_visibleVideoLayer\n");
	return tvcore_setVideoLayer(visible);
}

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelPlay_setService(JNIEnv * env, jobject obj, jobject service)
{
	JDVBService fieldids;
	JDVBService::InitFieldIDs(env, gClsService, fieldids);

    LOGI("Java_novel_supertv_dvb_jni_JniChannelPlay_setService tagDVBService %p \n",service);
	//创建C++结构体
	DVBService dvbservice;
	memset(&dvbservice,0,sizeof(DVBService));

    dvbservice.serviceID = env->GetIntField(service, fieldids.sid_field);
	dvbservice.service_type = env->GetIntField(service, fieldids.service_type_field);

    TuningParam tuning;
    tuning.freq = env->GetIntField(service, fieldids.freqID);
    tuning.symb = env->GetIntField(service, fieldids.symbolID);
    tuning.qam  = env->GetIntField(service, fieldids.modulationID);

    DVBTS dvbts;
    dvbts.tuning_param = tuning;
    dvbts.ts_id = env->GetIntField(service, fieldids.ts_id_field);
    dvbts.net_id = env->GetIntField(service, fieldids.net_id_field);
    dvbservice.ts = dvbts;

    DVBStream videostream;
    videostream.stream_pid = env->GetIntField(service, fieldids.video_stream_pid_field);
    videostream.stream_type = env->GetIntField(service, fieldids.video_stream_type_field);
    dvbservice.video_stream = videostream;
    dvbservice.video_stream.ecm_pid = env->GetIntField(service, fieldids.video_ecm_pid_field);
	//获取音频信息
    DVBStream audiostream;
    audiostream.stream_pid = env->GetIntField(service, fieldids.audio_stream_pid_field);
    audiostream.stream_type = env->GetIntField(service, fieldids.audio_stream_type_field);
    audiostream.ecm_pid = env->GetIntField(service, fieldids.audio_ecm_pid_field);
    dvbservice.audio_stream[0] = audiostream;
    LOGI("Java_novel_supertv_dvb_jni_JniChannelPlay_setService <0> %d %d %d\n",audiostream.stream_pid,audiostream.stream_type,audiostream.ecm_pid);

    audiostream.stream_pid = env->GetIntField(service, fieldids.audio_stream_pid1_field);
    audiostream.stream_type = env->GetIntField(service, fieldids.audio_stream_type1_field);
    audiostream.ecm_pid = env->GetIntField(service, fieldids.audio_ecm_pid1_field);
    dvbservice.audio_stream[1] = audiostream;

    LOGI("Java_novel_supertv_dvb_jni_JniChannelPlay_setService <1> %d %d %d\n",audiostream.stream_pid,audiostream.stream_type,audiostream.ecm_pid);

    audiostream.stream_pid = env->GetIntField(service, fieldids.audio_stream_pid2_field);
    audiostream.stream_type = env->GetIntField(service, fieldids.audio_stream_type2_field);
    audiostream.ecm_pid = env->GetIntField(service, fieldids.audio_ecm_pid2_field);
    dvbservice.audio_stream[2] = audiostream;
    LOGI("Java_novel_supertv_dvb_jni_JniChannelPlay_setService <2> %d %d %d\n",audiostream.stream_pid,audiostream.stream_type,audiostream.ecm_pid);

	dvbservice.audio_index = env->GetIntField(service, fieldids.audio_index_field);
	if(dvbservice.audio_index > (AUDIOSTREAM_MAXCOUNT - 1) )
	{
		dvbservice.audio_index = 0;
	}

    dvbservice.pmt_id = env->GetIntField(service, fieldids.pmt_id_field);
	dvbservice.emm_pid = env->GetIntField(service, fieldids.emmpid_field);
	dvbservice.nitVersion = env->GetIntField(service, fieldids.nitVersion_field);
	dvbservice.batVersion = env->GetIntField(service, fieldids.batVersion_field);
    LOGI("Java_novel_supertv_dvb_jni_JniChannelPlay_setService %d %d\n",dvbservice.nitVersion,dvbservice.batVersion);

	dvbservice.pcr_pid = env->GetIntField(service, fieldids.pcr_pid_field);
	//获取节目名字
	jstring jstrName = (jstring)env->GetObjectField(service,fieldids.sname_field);
	jmethodID getBytesID = env->GetMethodID(gClsString,"getBytes","(Ljava/lang/String;)[B"); 
	jbyteArray jbyteName = (jbyteArray)env->CallObjectMethod(jstrName,getBytesID,gObjStrEncoding);
	char *strName = (char *)env->GetByteArrayElements(jbyteName, 0);
	//strlen(strName) 长度不准确
    int len = env->GetArrayLength(jbyteName);
	memcpy(dvbservice.name,strName,len);
	env->ReleaseByteArrayElements(jbyteName,(jbyte*)strName,0);
    LOGI("Java_novel_supertv_dvb_jni_JniChannelPlay_setService <%s>\n",dvbservice.name);

    tvcore_setService(&dvbservice);
	return 0;
}

#ifdef __cplusplus
}//extren "C"
#endif
