#include <jni.h>
#include <android/log.h>

#include <tvcore.h>
#include <tvcomm.h>

#ifdef LOG_TAG
#undef LOG_TAG
#undef LOGI
#undef LOGE
#endif

#define  LOG_TAG    "JniEpgSearch"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#define CLASS_tagEpgEvent "novel/supertv/dvb/jni/struct/tagEpgEvent"
#define CLASS_tagProgramEpg "novel/supertv/dvb/jni/struct/tagProgramEpg"
#define CLASS_Integer "java/lang/Integer"

//在ChannelPlay.cpp中定义
extern jclass gClsEpgSearch;
extern jclass gClsMini;
//extern "C" jclass gClsEpgSearch;
//extern "C" jclass gClsMini;
//这样写重复定义错误
/*
extern "C" {
	jclass gClsEpgSearch;
	jclass gClsMini;
}
*/

jstring CreateJstringFromGB2312(JNIEnv * env, const char * pStr);

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

extern "C" int DoEpgSearchComplete(JNIEnv * env, long lParam, void * pParam)
{
	LOGI("DoEpgSearchComplete in...\n");
	jmethodID callbackID = env->GetStaticMethodID(gClsEpgSearch,"onEpgSearchComplete","(I)V");
	if(CheckJniException(env,__FILE__,__LINE__))
		return 0;

	env->CallStaticVoidMethod(gClsEpgSearch,callbackID,lParam);
	LOGI("DoEpgSearchComplete out...\n");
	return 0;
}


//把一个EpgEvent结构体转换为Java对象
static jobjectArray convertEpgEvents(JNIEnv * env, EpgEventSet & events)
{
	jclass clsEvent = env->FindClass(CLASS_tagEpgEvent);

	jmethodID modCon = env->GetMethodID(clsEvent,"<init>","()V");
	jfieldID eidID = env->GetFieldID(clsEvent, "id", "I");
	jfieldID nameID = env->GetFieldID(clsEvent, "name", "Ljava/lang/String;");
	jfieldID start_timeID = env->GetFieldID(clsEvent, "start_time", "J");
	jfieldID end_timeID = env->GetFieldID(clsEvent, "end_time", "J");
	jfieldID descID = env->GetFieldID(clsEvent, "description", "Ljava/lang/String;");

	jobjectArray retArray = env->NewObjectArray(events.size(), clsEvent, NULL);

	EpgEventSet::iterator it = events.begin();
	int index = 0;
	for(;it != events.end(); it++)
	{
		jobject objTemp = env->NewObject(clsEvent, modCon);
		env->SetIntField(objTemp, eidID, (it->id) & 0x0000FFFF);
		jstring strName = CreateJstringFromGB2312(env, it->name.c_str());
		env->SetObjectField(objTemp, nameID, strName);
		env->SetLongField(objTemp, start_timeID, it->start_time);
		env->SetLongField(objTemp, end_timeID, it->end_time);
		jstring strDesc = CreateJstringFromGB2312(env, it->description.c_str());
		env->SetObjectField(objTemp, descID, strDesc);
		env->SetObjectArrayElement(retArray, index++, objTemp);

		env->DeleteLocalRef(strName);
		env->DeleteLocalRef(strDesc);
		env->DeleteLocalRef(objTemp);
	}
	env->DeleteLocalRef((jobject)clsEvent);
	return retArray;
}

//把一个ProgramEpg结构体转换为Java对象
static jobject convertProgramEpgs(JNIEnv * env, EPGDataBaseT& programs)
{
	//for ProgramEpg
	jclass clsProgram = env->FindClass(CLASS_tagProgramEpg);
	jmethodID modConProgram = env->GetMethodID(clsProgram, "<init>", "()V");
	jfieldID sidID = env->GetFieldID(clsProgram,"serviceId","I");
	jfieldID presentVID = env->GetFieldID(clsProgram,"presentVersion","I");
	jfieldID followingVID = env->GetFieldID(clsProgram,"followingVersion","I");
	jfieldID eventVID = env->GetFieldID(clsProgram,"eventVersion","I");
	jfieldID presentEpgID = env->GetFieldID(clsProgram,"presentEpgEvent","Lnovel/supertv/dvb/jni/struct/tagEpgEvent;");
	jfieldID followEpgID = env->GetFieldID(clsProgram,"followingEpgEvent","Lnovel/supertv/dvb/jni/struct/tagEpgEvent;");
	jfieldID eventsID = env->GetFieldID(clsProgram,"events","[Lnovel/supertv/dvb/jni/struct/tagEpgEvent;");
	//for EpgEvent
	jclass clsEvent = env->FindClass(CLASS_tagEpgEvent);
	jmethodID modConEvent = env->GetMethodID(clsEvent,"<init>","()V");
	jfieldID eidID = env->GetFieldID(clsEvent, "id", "I");
	jfieldID nameID = env->GetFieldID(clsEvent, "name", "Ljava/lang/String;");
	jfieldID start_timeID = env->GetFieldID(clsEvent, "start_time", "J");
	jfieldID end_timeID = env->GetFieldID(clsEvent, "end_time", "J");
	jfieldID descID = env->GetFieldID(clsEvent, "description", "Ljava/lang/String;");
	//for Integer
	jclass clsInt = env->FindClass(CLASS_Integer);
	jmethodID modConInt = env->GetMethodID(clsInt, "<init>", "(I)V");
	//for HashMap
	jclass clsMap = env->FindClass("java/util/HashMap");
	jmethodID modConMap = (env)->GetMethodID(clsMap, "<init>", "()V");
	jmethodID mapPutID = env->GetMethodID( clsMap, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
	jobject retMap = (env)->NewObject(clsMap, modConMap);
	
	EPGDataBaseT::iterator it = programs.begin();
	for(;it != programs.end(); it++)
	{
		jobject objInt = env->NewObject(clsInt, modConInt, (it->first) & 0x0000FFFF);
		jobject objProgram = env->NewObject(clsProgram, modConProgram);
		env->SetIntField(objProgram, sidID, it->second.sid);
		env->SetIntField(objProgram, presentVID, it->second.PresentVer);
		env->SetIntField(objProgram, followingVID, it->second.FollowingVer);
		env->SetIntField(objProgram, eventVID, it->second.EventsVer);
		//Present
		jobject objPre = env->NewObject(clsEvent, modConEvent);
		env->SetIntField(objPre, eidID, (it->second.Present.id) & 0x0000FFFF);
		jstring strName = CreateJstringFromGB2312(env, it->second.Present.name.c_str());
		env->SetObjectField(objPre, nameID, strName);
		env->SetLongField(objPre, start_timeID, it->second.Present.start_time);
		env->SetLongField(objPre, end_timeID, it->second.Present.end_time);
		jstring strDesc = CreateJstringFromGB2312(env, it->second.Present.description.c_str());
		env->SetObjectField(objPre, descID, strDesc);
		//设置Present
		env->SetObjectField(objProgram, presentEpgID, objPre);
		env->DeleteLocalRef(strName);
		env->DeleteLocalRef(strDesc);
		env->DeleteLocalRef(objPre);
		//Following
		jobject objFollow = env->NewObject(clsEvent, modConEvent);
		env->SetIntField(objFollow, eidID, (it->second.Following.id) & 0x0000FFFF);
		strName = CreateJstringFromGB2312(env, it->second.Following.name.c_str());
		env->SetObjectField(objFollow, nameID, strName);
		env->SetLongField(objFollow, start_timeID, it->second.Following.start_time);
		env->SetLongField(objFollow, end_timeID, it->second.Following.end_time);
		strDesc = CreateJstringFromGB2312(env, it->second.Following.description.c_str());
		env->SetObjectField(objFollow, descID, strDesc);
		//设置Following
		env->SetObjectField(objProgram, followEpgID, objFollow);
		env->DeleteLocalRef(strName);
		env->DeleteLocalRef(strDesc);
		env->DeleteLocalRef(objFollow);
		
		jobjectArray events = convertEpgEvents(env, it->second.events);
		env->SetObjectField(objProgram, eventsID, events);
		env->DeleteLocalRef(events);

		env->CallObjectMethod(retMap,mapPutID,objInt,objProgram);
		env->DeleteLocalRef(objInt);
		env->DeleteLocalRef(objProgram);
	}		
	return retMap;
}




#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     novel_supertv_dvb_jni_JniEpgSearch
 * Method:    startEpgSearch
 * Signature: (Lnovel/supertv/dvb/jni/struct/tagTuningParam;Lnovel/supertv/dvb/jni/struct/EitEventType;)Z
 */
JNIEXPORT jboolean JNICALL Java_novel_supertv_dvb_jni_JniEpgSearch_startEpgSearch
  (JNIEnv * env, jobject thiz, jobject tun, int type)
{
	LOGI("startEpgSearch in...\n");
	//获取类中每一个变量的定义
	jclass clsaTp = env->GetObjectClass(tun);
	jfieldID freqID = env->GetFieldID(clsaTp, "Frequency", "I");
	jfieldID symbID = env->GetFieldID(clsaTp, "SymbolRate", "I");
	jfieldID qamID = env->GetFieldID(clsaTp, "Modulation", "I");

	TuningParam tunParam;
	tunParam.freq = env->GetIntField(tun, freqID);
	tunParam.symb = env->GetIntField(tun, symbID);
	tunParam.qam = env->GetIntField(tun, qamID);
	
	bool ret = tvcore_startEpgSearch(tunParam, (EITEventType)type);
	LOGI("startEpgSearch out...%d\n",ret);
	return ret;
}

/*
 * Class:     novel_supertv_dvb_jni_JniEpgSearch
 * Method:    cancleEpgSearch
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_novel_supertv_dvb_jni_JniEpgSearch_cancleEpgSearch
  (JNIEnv * env, jobject thiz)
{
	 LOGI("cancleEpgSearch in...\n");
	bool ret = tvcore_cancelEpgSearch();
	LOGI("cancleEpgSearch out...%d\n",ret);
	return ret;
}

/*
 * Class:     novel_supertv_dvb_jni_JniEpgSearch
 * Method:    getEpgData
 * Signature: ()Ljava/util/Map;
 */
JNIEXPORT jobject JNICALL Java_novel_supertv_dvb_jni_JniEpgSearch_getEpgData
  (JNIEnv * env, jobject thiz)
{
	LOGI("getEpgSearch in...\n");
	EPGDataBaseT epgs;
	bool ret = tvcore_getEpgData(epgs);
	jobject retMap = NULL;
	if(ret)
		retMap = convertProgramEpgs(env, epgs);
	LOGI("getEpgSearch out...%d\n",ret);
	return retMap;
}

JNIEXPORT jstring JNICALL Java_novel_supertv_dvb_jni_JniEpgSearch_getUTCTime(JNIEnv * env, jobject thiz)
{
	char buf[32] = {0};
	U32 times = 0;
	U32 offset = 0;
	bool ret = tvcore_get_tot(times, offset);
	if(ret == false)
	{
		times = offset = 0;
	}
	else
	{
		//转成秒数
		offset = ((offset & 0x0F) + ((offset >> 4) & 0x0F) * 10) * 60 +	//低8位为分钟
			(((offset >> 8) & 0x0F) + ((offset >> 12) & 0x0F) * 10 ) * 3600;	//高8位为小时
	}
	sprintf(buf,"%d:%d",times,offset);
	return env->NewStringUTF(buf);	
}

/*
 * Class:     novel_supertv_dvb_jni_JniEpgSearch
 * Method:    getEpgDataBySID
 * Signature: (I)[Lnovel/supertv/dvb/jni/struct/tagEpgEvent;
 */
JNIEXPORT jobjectArray JNICALL Java_novel_supertv_dvb_jni_JniEpgSearch_getEpgDataBySID
  (JNIEnv * env, jobject thiz, jint sid)
{
	LOGI("getEpgDataBySID in...\n");
	EpgEventSet events;

	bool ret = tvcore_getEpgDataBySID(sid, events);
	if(ret == false)
	{
		LOGI("getEpgDataBySID failed out..\n");
		return NULL;
	}
	jobjectArray retArray = convertEpgEvents(env,events);
//	env->DeleteLocalRef(retArray);
	LOGI("getEpgDataBySID out..\n");
	return retArray;
}

/*
 * Class:     novel_supertv_dvb_jni_JniEpgSearch
 * Method:    getEpgDataByDuration
 * Signature: (III)[Lnovel/supertv/dvb/jni/struct/tagEpgEvent;
 */
JNIEXPORT jobjectArray JNICALL Java_novel_supertv_dvb_jni_JniEpgSearch_getEpgDataByDuration
  (JNIEnv * env, jobject thiz, jint sid, jlong startTime, jlong endTime)
{
	LOGI("getEpgDataByDuration in...startTime %lld, endTime %lld\n",startTime,endTime);

	EpgEventSet events;
	bool ret = tvcore_getEpgDataByDuration(sid, events, (U32)(startTime / 1000 ), (U32)(endTime / 1000));
	if(ret == false)
	{
		LOGI("getEpgDataByDuration failed out...\n");
		return NULL;
	}

	LOGI("getEpgDataByDuration get event size %d..\n",events.size());
	jobjectArray retArray = convertEpgEvents(env,events);
//	env->DeleteLocalRef(retArray);
	LOGI("getEpgDataByDuration out...\n");
	return retArray;
}

JNIEXPORT jobject JNICALL Java_novel_supertv_dvb_jni_JniEpgSearch_getPFEventInfo
  (JNIEnv * env, jobject thiz)
{
	jobject tagPfObj = NULL;
	MiniEPGNotify pfEvent;

	if(false  == tvocre_getPFEventInfo(&pfEvent))
	{
		return tagPfObj;
	}

	//获取方法的ID，init是构造函数
	jmethodID tagPfMid = env->GetMethodID(gClsMini, "<init>", "()V");
	//创建实例
	tagPfObj = env->NewObject(gClsMini, tagPfMid);
	//获取类中每一个变量的定义
	jfieldID curName_field = env->GetFieldID(gClsMini, "CurrentEventName", "Ljava/lang/String;");
	jfieldID curStar_field = env->GetFieldID(gClsMini, "CurrentEventStartTime", "J");
	jfieldID curEnd_field = env->GetFieldID(gClsMini, "CurrentEventEndTime", "J");
	jfieldID nextName_field = env->GetFieldID(gClsMini, "NextEventName", "Ljava/lang/String;");
	jfieldID nextStar_field = env->GetFieldID(gClsMini, "NextEventStartTime", "J");
	jfieldID nextEnd_field = env->GetFieldID(gClsMini, "NextEventEndTime", "J");
	//Java对象变量赋值
	env->SetLongField(tagPfObj,curStar_field,pfEvent.CurrentEventStartTime);
	env->SetLongField(tagPfObj,curEnd_field,pfEvent.CurrentEventEndTime);
	env->SetLongField(tagPfObj,nextStar_field,pfEvent.NextEventStartTime);
	env->SetLongField(tagPfObj,nextEnd_field,pfEvent.NextEventEndTime);
	
//	LOGI("tvcoreNotifyFuc current s=%d,e=%d,next s=%d,e=%d\n",pfEvent.CurrentEventStartTime,pfEvent.CurrentEventEndTime,pfEvent.NextEventStartTime,pfEvent.NextEventEndTime);
	
	jstring currentName = CreateJstringFromGB2312(env,pfEvent.CurrentEventName);
	env->SetObjectField(tagPfObj,curName_field,currentName);
	
	jstring nextName = CreateJstringFromGB2312(env,pfEvent.NextEventName);
	env->SetObjectField(tagPfObj,nextName_field,nextName);

	return tagPfObj;
}

#ifdef __cplusplus
}
#endif
