#include <jni.h>
#include <android/log.h>

#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#include <tvcomm.h>
#include <isearchtvnotify.h>
#include <tvcore.h>

#include "JDVBService.h"

#ifdef LOG_TAG
#undef LOG_TAG
#endif
#define  LOG_TAG    "chehl_JNIChannelSearch"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

extern "C" {
    JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelSearch_StartSearchTV(JNIEnv *env, jobject obj,jint stvMode, jobject tuningParam, jobject notify);
    JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelSearch_CancelSearchTV(JNIEnv *env, jobject obj);
	bool InitClassFieldID(JNIEnv * env);
};


//jobject * gnotify = NULL;
static jclass gClsSearch ;
static jmethodID gconid = 0;
static jmethodID gprogressid = 0;
static jmethodID gtuinfoid = 0;
static jmethodID gserviceid = 0;

static jclass clsTp;
static jclass clsTs;
static jclass gClsServiceType;
//{{在Play.cpp中定义
extern JavaVM * gJavaVM;
extern jclass gClsService;
extern jclass gClsString;
extern jstring gObjStrEncoding;
extern jmethodID gMedStringConID;
//在Play.cpp中定义}}

jstring CreateJstringFromGB2312(JNIEnv * env, const char * pStr);
struct JTuningParam
{
	jfieldID m_FreqID;
	jfieldID m_SymbID;
	jfieldID m_QamID; 

	static bool InitFieldIDs(JNIEnv * env, jclass cls, JTuningParam & fieldids);
	static bool ConvertObject(JNIEnv * env, JTuningParam & fieldids, jobject & obj, const TuningParam & tunParam);
};

bool JTuningParam::InitFieldIDs(JNIEnv * env, jclass cls, JTuningParam & fieldids)
{
	fieldids.m_FreqID = env->GetFieldID(cls, "Frequency", "I");
	fieldids.m_SymbID = env->GetFieldID(cls, "SymbolRate", "I");
	fieldids.m_QamID = env->GetFieldID(cls, "Modulation", "I");
	return true;
}

bool JTuningParam::ConvertObject(JNIEnv * env, JTuningParam & fieldids, jobject & obj, const TuningParam & tunParam)
{
	env->SetIntField(obj,fieldids.m_FreqID,tunParam.freq);
	env->SetIntField(obj,fieldids.m_QamID,tunParam.qam);
	env->SetIntField(obj,fieldids.m_SymbID,tunParam.symb);
	return true;
}

class MyNotify :public ISearchTVNotify
{
public:
	virtual void OnDVBService(std::vector<DVBService> &services)
	{
		LOGI("R... OnDVBService start\n");
		//gDVBService = services;
        JNIEnv * currentEnv = NULL;
        gJavaVM->AttachCurrentThread(&currentEnv, NULL);

		JDVBService fieldids;
		JDVBService::InitFieldIDs(currentEnv, gClsService, fieldids);

        jmethodID conidService = currentEnv->GetMethodID(gClsService, "<init>", "()V");

		DVBServiceListT::iterator it= services.begin();
		for(;it != services.end();it++)
		{
			jobject objtmpService = currentEnv->NewObject(gClsService, conidService);

			JDVBService::ConvertObject(currentEnv, fieldids, objtmpService, *it);

			currentEnv->CallStaticVoidMethod(gClsSearch,gserviceid, objtmpService);
			currentEnv->DeleteLocalRef(objtmpService);
		}
		gJavaVM->DetachCurrentThread();

	}
	
	virtual void OnProgress(U32 iPercent)
	{
		LOGI("R... OnProgress received %p %p %p\n",gJavaVM, gClsSearch, gprogressid);
		JNIEnv * currentEnv = NULL;
		gJavaVM->AttachCurrentThread(&currentEnv, NULL);
		LOGI("R... OnProgress received %p \n",currentEnv);

		currentEnv->CallStaticVoidMethod(gClsSearch, gprogressid,iPercent);
		gJavaVM->DetachCurrentThread();

	}

	virtual void OnTunerInfo(const TuningParam& tuning,const TunerSignal& signal)
	{
		LOGI("R... OnTunerInfo received Tuning(%d,%d,%d) start\n",tuning.freq,tuning.symb,tuning.qam);
	
		JNIEnv * currentEnv = NULL;
		gJavaVM->AttachCurrentThread(&currentEnv, NULL);

		if(currentEnv == NULL){
		   return;
		}

//LOGI("R... OnTunerInfo FindClass TuningParam \n");
//jclass clsTp = currentEnv->FindClass("novel/supertv/dvb/jni/struct/TuningParam");
//jclass clsTs = currentEnv->FindClass("novel/supertv/dvb/jni/struct/tagTunerSignal");
//LOGI("R... OnTunerInfo FindClass tagTunerSignal \n");

		jmethodID conidTp = currentEnv->GetMethodID(clsTp, "<init>", "(III)V");
		jobject objtmpTp = currentEnv->NewObject(clsTp, conidTp);
		JTuningParam fieldids;
		JTuningParam::InitFieldIDs(currentEnv, clsTp, fieldids);
		JTuningParam::ConvertObject(currentEnv, fieldids, objtmpTp,tuning);

		jmethodID conidTs = currentEnv->GetMethodID(clsTs, "<init>", "(III)V");
		jobject objtmpTs = currentEnv->NewObject(clsTs, conidTs);
		jfieldID level = currentEnv->GetFieldID(clsTs, "Level", "I");
		jfieldID cn = currentEnv->GetFieldID(clsTs, "CN", "I");
		jfieldID errRate = currentEnv->GetFieldID(clsTs, "ErrRate", "I");
		
		currentEnv->SetIntField(objtmpTs,level,signal.quality);
		currentEnv->SetIntField(objtmpTs,cn,signal.strength);
		currentEnv->SetIntField(objtmpTs,errRate,signal.locked);

		currentEnv->CallStaticVoidMethod(gClsSearch, gtuinfoid,objtmpTp,objtmpTs);
		gJavaVM->DetachCurrentThread();
		LOGI("R... OnTunerInfo received  end\n");
	}

	virtual void OnSearchTVComplete(std::vector<DVBService> &services, std::vector<ServiceTypeTableItem> &table)
	//virtual void OnSTVComplete()
	{
		LOGI("OnSTVComplete received .....%d\n",services.size());

        JNIEnv * currentEnv = NULL;
        gJavaVM->AttachCurrentThread(&currentEnv, NULL);

        jclass clsvec = currentEnv->FindClass("java/util/Vector");

        jmethodID vecConID = currentEnv->GetMethodID(clsvec,"<init>","()V");
        jobject vecObj = currentEnv->NewObject(clsvec,vecConID);
        LOGI("R... ONSTVComplete %p\n",vecObj);

        jmethodID addElemID = currentEnv->GetMethodID( clsvec, "addElement", "(Ljava/lang/Object;)V");
        if (addElemID == NULL) printf("method ID not valid\n\n");

		jclass clsArrayList = currentEnv->FindClass("java/util/ArrayList");
		jmethodID medArrayListCon_id = currentEnv->GetMethodID(clsArrayList, "<init>", "()V");
		jmethodID medAdd_id = currentEnv->GetMethodID(clsArrayList,"add","(Ljava/lang/Object;)Z");
		
		jmethodID medServiceTypeCon_id = currentEnv->GetMethodID(gClsServiceType, "<init>", "()V");
		jfieldID typeID_id = currentEnv->GetFieldID(gClsServiceType, "typeID","I");
		jfieldID typeName_id = currentEnv->GetFieldID(gClsServiceType, "typeName", "Ljava/lang/String;");

		jobject arrayObj = currentEnv->NewObject(clsArrayList, medArrayListCon_id);

		std::vector<ServiceTypeTableItem>::iterator itItem = table.begin();
		for(; itItem != table.end(); itItem++)
		{
			jobject objTmpType = currentEnv->NewObject(gClsServiceType, medServiceTypeCon_id);
			currentEnv->SetIntField(objTmpType, typeID_id, itItem->serviceType);
			jstring serviceName = CreateJstringFromGB2312(currentEnv,itItem->serviceName);	
			currentEnv->SetObjectField(objTmpType, typeName_id, serviceName);
			currentEnv->DeleteLocalRef(serviceName);

			currentEnv->CallBooleanMethod(arrayObj, medAdd_id, objTmpType);
			LOGI("R... OnDVBService  itItem->serviceName %s\n",itItem->serviceName);
			currentEnv->DeleteLocalRef(objTmpType);
		}


		JDVBService fieldids;
		JDVBService::InitFieldIDs(currentEnv, gClsService, fieldids);
		jmethodID conidService = currentEnv->GetMethodID(gClsService, "<init>", "()V");

		DVBServiceListT::iterator it= services.begin();
		for(;it != services.end();it++)
		{

			jobject objtmpService = currentEnv->NewObject(gClsService, conidService);
			JDVBService::ConvertObject(currentEnv, fieldids, objtmpService, *it);

			LOGI("R... OnDVBService  CallStaticVoidMethod audio index %d\n",it->audio_index);
			currentEnv->CallVoidMethod( vecObj, addElemID, objtmpService );
//			LOGI("R... OnDVBService  CallStaticVoidMethod end\n");
			currentEnv->DeleteLocalRef(objtmpService);
		}

		jmethodID jsize = currentEnv->GetMethodID( clsvec, "size", "()I");
		if (jsize == NULL) printf("method ID not valid\n\n");

		printf("---> Vector size after add elements %i\n", currentEnv->CallIntMethod(vecObj, jsize));

		LOGI("R... OnSTVComplete CallVoidMethod OnSTVComplete start\n");
		currentEnv->CallStaticVoidMethod(gClsSearch, gconid,vecObj,arrayObj);
		currentEnv->DeleteLocalRef(vecObj);
		currentEnv->DeleteLocalRef(arrayObj);
		LOGI("R... OnSTVComplete CallVoidMethod OnSTVComplete\n");

		currentEnv->DeleteGlobalRef(clsTp);
		currentEnv->DeleteGlobalRef(clsTs);
		currentEnv->DeleteGlobalRef(gClsSearch);
		currentEnv->DeleteGlobalRef(gClsServiceType);
		
		gJavaVM->DetachCurrentThread();
	}
	virtual void OnSEPGComplete()
	{
		LOGI("R... OnSEPGComplete received\n");
	}
	virtual void OnNitVersionChanged(U8 iVersion)
	{
		LOGI("R... OnNitVersionChanged received\n");
	}
};

MyNotify  callback;

/*
 * Class:     novel_supertv_dvb_jni_JniChannelSearch
 * Method:    StartSearchTV
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelSearch_StartSearchTV
  (JNIEnv *env, jobject obj, jint stvMode, jobject tuningParam, jobject notify)
{ 
	//如果整个应用在其它模块崩掉,当进入搜索时全变量会失效,而直接进入搜索不会重新调用Play_init
	InitClassFieldID(env);	

	LOGI("... StartSearchTV FindClass TuningParam \n");
	jclass clsTp1 = env->FindClass("novel/supertv/dvb/jni/struct/TuningParam");
	jclass clsTs1 = env->FindClass("novel/supertv/dvb/jni/struct/tagTunerSignal");

	clsTp = (jclass)env->NewGlobalRef(clsTp1);
	env->DeleteLocalRef(clsTp1);

	clsTs = (jclass)env->NewGlobalRef(clsTs1);
	env->DeleteLocalRef(clsTs1);

	JTuningParam fieldids;
	JTuningParam::InitFieldIDs(env, clsTp, fieldids);
    TuningParam tpp;
    tpp.freq = env->GetIntField(tuningParam,fieldids.m_FreqID);
    tpp.symb = env->GetIntField(tuningParam,fieldids.m_SymbID);
    tpp.qam = env->GetIntField(tuningParam,fieldids.m_QamID);
	LOGI("FREQ=%d,sysm=%d,qam=%d",tpp.freq,tpp.symb,tpp.qam);

    jclass gClsSearch1 = env->GetObjectClass(obj);
	gClsSearch = (jclass)env->NewGlobalRef(gClsSearch1);
	env->DeleteLocalRef(gClsSearch1);
	
	jclass clsServiceType = env->FindClass("novel/supertv/dvb/jni/struct/ServiceType");
	gClsServiceType = (jclass)env->NewGlobalRef(clsServiceType);
	env->DeleteLocalRef(clsServiceType);


    gconid = env->GetStaticMethodID(gClsSearch, "OnSTVComplete", "(Ljava/util/Vector;Ljava/util/ArrayList;)V");

    gprogressid = env->GetStaticMethodID(gClsSearch, "OnProgress", "(I)V");
    gtuinfoid = env->GetStaticMethodID(gClsSearch, "OnTunerInfo", "(Lnovel/supertv/dvb/jni/struct/TuningParam;Lnovel/supertv/dvb/jni/struct/tagTunerSignal;)V");
    gserviceid = env->GetStaticMethodID(gClsSearch, "OnDVBService", "(Lnovel/supertv/dvb/jni/struct/tagDVBService;)V");

    
    LOGI("StartSearchTV stvMode = %d",stvMode);

    if(stvMode == 0)
    {
        LOGI("StartSearchTV STVMODE_MANUAL");
		tvcore_startSearchTV(STVMODE_MANUAL, &tpp, &callback);
        LOGI("StartSearchTV STVMODE_MANUAL111");
    }else if(stvMode == 1){
        LOGI("StartSearchTV STVMODE_FULL");
		tvcore_startSearchTV(STVMODE_FULL, &tpp, &callback);
    }else if(stvMode == 2){
        LOGI("StartSearchTV STVMODE_NIT");
		tvcore_startSearchTV(STVMODE_NIT, &tpp, &callback);
    }
    return 1;
}

/*
 * Class:     novel_supertv_dvb_jni_JniChannelSearch
 * Method:    CancelSearchTV
 * Signature: ()
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelSearch_CancelSearchTV
  (JNIEnv *, jobject)
{
    LOGI("CancelSearchTV() /n");
    tvcore_cancelSearchTV();
    return 1;
}
