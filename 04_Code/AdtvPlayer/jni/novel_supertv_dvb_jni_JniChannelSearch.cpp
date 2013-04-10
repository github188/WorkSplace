/*
***************************************************************************************************
*  FileName    : novel_supertv_dvb_jni_JniChannelSearch.cpp
*  Author      : dr      Date: 2012-04-05
*  Description :
*--------------------------------------------------------------------------------------------------
*  History     :
*  <time>        <version >   <author>   	<desc>
*  2012-04-05       V1.0.0       dr             first release
*
***************************************************************************************************
*/

#include <string.h>
#include <sys/types.h>
#include <unistd.h>
#include <grp.h>
#include <binder/IPCThreadState.h>
#include <binder/ProcessState.h> 
#include <binder/IServiceManager.h>
#include <cutils/log.h>




#include <private/android_filesystem_config.h>
//#include <dvb_types.h>
//#include <NovelPlayer.h>

#include <time.h>

#include <jni.h>
#include <android/log.h>

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#include <tvcomm.h>
#include <ISearchTVNotify.h>
#include <ITvSearchCore.h>
#define  LOG_TAG    "jianglei"
//#include "novel_supertv_dvb_jni_JniChannelSearch.h"
namespace android{
extern "C" {
        JNIEXPORT jstring JNICALL Java_novel_supertv_dvb_jni_JniChannelSearch_getNameString(JNIEnv *env, jobject obj);

	JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelSearch_StartSearchTV(JNIEnv *env, jobject obj,jint stvMode, jobject tuningParam, jobject notify);

        JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniChannelSearch_CancelSearchTV(JNIEnv *env, jobject obj);

};

/*
 * Class:     novel_supertv_dvb_jni_JniChannelSearch
 * Method:    getString
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_novel_supertv_dvb_jni_JniChannelSearch_getNameString
  (JNIEnv *env, jobject obj)
{
    return (env)->NewStringUTF("http:192.168.11.168:8080/JniChannelSearch.ts");
}

//JNIEnv genv;
//jobject * gnotify = NULL;
JavaVM * jvm = NULL;
jclass gclsa ;
//java的搜索完成方法的id
jmethodID gconid = 0;
//java的进度方法的id
jmethodID gprogressid = 0;
//java的tunerInfo方法的id
jmethodID gtuinfoid = 0;
//java的OnDVBService的id
jmethodID gserviceid = 0;




jclass clsTp;
jclass clsTs;
jclass clsService;

//定义java String类 strClass
jclass gstrClass;

typedef std::vector<DVBService> DVBServiceListT;

//DVBServiceListT gDVBService;

class MyNotify :public ISearchTVNotify
{
public:
	virtual ~MyNotify(){

                LOGI("MyNotify ~MyNotify() DetachCurrentThread() start\n");
                jvm->DetachCurrentThread();
                LOGI("MyNotify ~MyNotify() DetachCurrentThread() end\n");

        };
    
	virtual void OnDVBService(std::vector<DVBService> &services)
	{
		LOGI("R... OnDVBService start\n");
		//gDVBService = services;
                JNIEnv * currentEnv = NULL;
                jvm->AttachCurrentThread(&currentEnv, NULL);


		DVBServiceListT::iterator it= services.begin();
		for(;it != services.end();it++)
		{

                    //这种强制转换不能成功
		    //jstring serviceName = (jstring)it->name;
                    //jstring serviceName = currentEnv->NewStringUTF(it->name);
                    LOGI("R... OnDVBService  name(%s)\n",it->name);

//获取java String类方法String(byte[],String)的构造器,用于将本地byte[]数组转换为一个新String
jmethodID ctorID = currentEnv->GetMethodID(gstrClass, "<init>", "([BLjava/lang/String;)V");
jbyteArray bytes = currentEnv->NewByteArray(strlen(it->name));//建立byte数组
currentEnv->SetByteArrayRegion(bytes, 0, strlen(it->name), (jbyte*)it->name);//将char* 转换为byte数组
jstring encoding = currentEnv->NewStringUTF("GB2312"); // 设置String, 保存语言类型,用于byte数组转换至String时的参数
jstring serviceName = (jstring)currentEnv->NewObject(gstrClass, ctorID, bytes, encoding);//将byte数组转换为java String,并输出
//test ok
//jstring serviceName = currentEnv->NewStringUTF("湖南卫视");

		    jint serviceid = (jint)it->sid;
                    LOGI("R... OnDVBService  sid(%d)\n",it->sid);
                    jint chnumber = it->channel_number;
                    LOGI("R... OnDVBService  channel_number(%d)\n",it->channel_number);
                    jint type = it->service_type;
                    LOGI("R... OnDVBService  service_type(%d)\n",it->service_type);
                    jint reserved1 = it->reserved1;
                    LOGI("R... OnDVBService  reserved1(%d)\n",it->reserved1);
                    jint cate = it->category;
                    LOGI("R... OnDVBService  category(%d)\n",it->category);
                    jint reserved2 = it->reserved2;
                    LOGI("R... OnDVBService  reserved2(%d)\n",it->reserved2);
                    jint pcr_pid = it->pcr_pid;
                    LOGI("R... OnDVBService  pcr_pid(%d)\n",it->pcr_pid);
                    jint reserved3 = it->reserved3;
                    LOGI("R... OnDVBService  reserved3(%d)\n",it->reserved3);
                    jint pmt_id = it->pmt_id;
                    LOGI("R... OnDVBService  pmt_id(%d)\n",it->pmt_id);
                    jchar volume_ratio = it->volume_ratio;
                    LOGI("R... OnDVBService  volume_ratio(%c)\n",it->volume_ratio);
                    jint reserved4 = it->reserved4;
                    LOGI("R... OnDVBService  reserved4(%d)\n",it->reserved4);
                    jint volume_reserve = it->volume_reserve;
                    LOGI("R... OnDVBService  volume_reserve(%d)\n",it->volume_reserve);
                    jint audio_channel_set = it->audio_channel_set;
                    LOGI("R... OnDVBService  audio_channel_set(%d)\n",it->audio_channel_set);
                    jint audio_format = it->audio_format;
                    LOGI("R... OnDVBService  audio_format(%d)\n",it->audio_format);
                    jint audio_index = it->audio_index;
                    LOGI("R... OnDVBService  audio_index(%d)\n",it->audio_index);
                    DVBStream video_stream = it->video_stream;
                    LOGI("R... OnDVBService  video_stream.stream_pid(%d)\n",it->video_stream.stream_pid);
                    LOGI("R... OnDVBService  video_stream.stream_type(%d)\n",it->video_stream.stream_type);
                    LOGI("R... OnDVBService  video_stream.ecm_pid(%d)\n",it->video_stream.ecm_pid);
                    LOGI("R... OnDVBService  video_stream.name(%s)\n",it->video_stream.name);
                    DVBTS ts = it->ts;
                    LOGI("R... OnDVBService  ts.ts_id(%d)\n",it->ts.ts_id);
                    LOGI("R... OnDVBService  ts.net_id(%d)\n",it->ts.net_id);
                    //TuningParam:
                    //LOGI("R... OnDVBService  ts.ts_id(%d)\n",it->ts.tuning_param);
                    //是个数组:
                    for(int i=0;i<3;i++)
                    {
                        DVBStream audio_stream = it->audio_stream[i];

                        //LOGI("R... OnDVBService  audio_stream.stream_pid(%d)\n",it->audio_stream.stream_pid);
                        //LOGI("R... OnDVBService  audio_stream.stream_type(%d)\n",it->audio_stream.stream_type);
                        //LOGI("R... OnDVBService  audio_stream.ecm_pid(%d)\n",it->audio_stream.ecm_pid);
                        //LOGI("R... OnDVBService  audio_stream.name(%s)\n",it->audio_stream.name);

                        LOGI("R... OnDVBService  audio_stream[%d].stream_pid(%d)\n",i,it->audio_stream[i].stream_pid);
                        LOGI("R... OnDVBService  audio_stream[%d].stream_type(%d)\n",i,it->audio_stream[i].stream_type);
                        LOGI("R... OnDVBService  audio_stream[%d].ecm_pid(%d)\n",i,it->audio_stream[i].ecm_pid);
                        LOGI("R... OnDVBService  audio_stream[%d].name(%s)\n",i,it->audio_stream[i].name);

                    }



                    //获取方法的ID，init是构造函数，(III)V是三个int参数和V是void
                    jmethodID conidService = currentEnv->GetMethodID(clsService, "<init>", "(IILjava/lang/String;)V");

                    //创建实例
                    jobject objtmpService = currentEnv->NewObject(clsService, conidService);

                    // 销毁实例类
                    //currentEnv->DeleteLocalRef(clsTp);
    
                    //获取类中每一个变量的定义
                    jclass clsaService = currentEnv->GetObjectClass(objtmpService);
    

                    jfieldID sid_field = currentEnv->GetFieldID(clsaService, "sid", "I");
                    jfieldID cnum_field = currentEnv->GetFieldID(clsaService, "channel_number", "I");
                    jfieldID sname_field = currentEnv->GetFieldID(clsaService, "name","Ljava/lang/String;");
                    jfieldID service_type_field = currentEnv->GetFieldID(clsaService, "service_type","I");
                    jfieldID reserved1_field = currentEnv->GetFieldID(clsaService, "reserved1","I");
                    jfieldID category_field = currentEnv->GetFieldID(clsaService, "category","I");
                    jfieldID reserved2_field = currentEnv->GetFieldID(clsaService, "reserved2","I");
                    jfieldID pcr_pid_field = currentEnv->GetFieldID(clsaService, "pcr_pid","I");
                    jfieldID reserved3_field = currentEnv->GetFieldID(clsaService, "reserved3","I");
                    jfieldID pmt_id_field = currentEnv->GetFieldID(clsaService, "pmt_id","I");
                    //jfieldID volume_ratio_field = currentEnv->GetFieldID(clsaService, "volume_ratio","Ljava/lang/String;");
                    jfieldID reserved4_field = currentEnv->GetFieldID(clsaService, "reserved4","I");
                    jfieldID volume_reserve_field = currentEnv->GetFieldID(clsaService, "volume_reserve","I");
                    jfieldID audio_channel_set_field = currentEnv->GetFieldID(clsaService, "audio_channel_set","I");
                    jfieldID audio_format_field = currentEnv->GetFieldID(clsaService, "audio_format","I");
                    jfieldID audio_index_field = currentEnv->GetFieldID(clsaService, "audio_index","I");



                    //新加入字段
                    jfieldID video_stream_type_field = currentEnv->GetFieldID(clsaService, "video_stream_type","I");
                    jfieldID video_stream_pid_field = currentEnv->GetFieldID(clsaService, "video_stream_pid","I");
                    jfieldID video_ecm_pid_field = currentEnv->GetFieldID(clsaService, "video_ecm_pid","I");
                    jfieldID ts_id_field = currentEnv->GetFieldID(clsaService, "ts_id","I");
                    jfieldID net_id_field = currentEnv->GetFieldID(clsaService, "net_id","I");
                    jfieldID audio_stream_type_field = currentEnv->GetFieldID(clsaService, "audio_stream_type","I");
                    jfieldID audio_stream_pid_field = currentEnv->GetFieldID(clsaService, "audio_stream_pid","I");
                    jfieldID audio_ecm_pid_field = currentEnv->GetFieldID(clsaService, "audio_ecm_pid","I");
                    
                    //
                    //jfieldID video_stream_field = currentEnv->GetFieldID(clsaService, "video_stream","I");
                    //jfieldID ts_field = currentEnv->GetFieldID(clsaService, "ts","I");
                    //jfieldID audio_stream_field = currentEnv->GetFieldID(clsaService, "audio_stream","I");

                    // 赋值
                    currentEnv->SetIntField(objtmpService,sid_field,serviceid);
                    currentEnv->SetIntField(objtmpService,cnum_field,chnumber);
                    currentEnv->SetObjectField(objtmpService,sname_field,serviceName);
                    currentEnv->SetIntField(objtmpService,service_type_field,it->service_type);
                    currentEnv->SetIntField(objtmpService,reserved1_field,it->reserved1);
                    currentEnv->SetIntField(objtmpService,category_field,it->category);
                    currentEnv->SetIntField(objtmpService,reserved2_field,it->reserved2);
                    currentEnv->SetIntField(objtmpService,pcr_pid_field,it->pcr_pid);
                    currentEnv->SetIntField(objtmpService,reserved3_field,it->reserved3);
                    currentEnv->SetIntField(objtmpService,pmt_id_field,it->pmt_id);
                    //currentEnv->SetIntField(objtmpService,volume_ratio_field,it->volume_ratio);
                    currentEnv->SetIntField(objtmpService,reserved4_field,it->reserved4);
                    currentEnv->SetIntField(objtmpService,volume_reserve_field,it->volume_reserve);
                    currentEnv->SetIntField(objtmpService,audio_channel_set_field,it->audio_channel_set);
                    currentEnv->SetIntField(objtmpService,audio_format_field,it->audio_format);
                    currentEnv->SetIntField(objtmpService,audio_index_field,it->audio_index);

                    //新加入字段
                    currentEnv->SetIntField(objtmpService,video_stream_type_field,it->video_stream.stream_type);
                    currentEnv->SetIntField(objtmpService,video_stream_pid_field,it->video_stream.stream_pid);
                    currentEnv->SetIntField(objtmpService,video_ecm_pid_field,it->video_stream.ecm_pid);
                    currentEnv->SetIntField(objtmpService,ts_id_field,it->ts.ts_id);
                    currentEnv->SetIntField(objtmpService,net_id_field,it->ts.net_id);
                    currentEnv->SetIntField(objtmpService,audio_stream_type_field,it->audio_stream[0].stream_type);
                    currentEnv->SetIntField(objtmpService,audio_stream_pid_field,it->audio_stream[0].stream_pid);
                    currentEnv->SetIntField(objtmpService,audio_ecm_pid_field,it->audio_stream[0].ecm_pid);


                    LOGI("R... OnDVBService  CallStaticVoidMethod start\n");
                    currentEnv->CallStaticVoidMethod(gclsa,gserviceid, objtmpService);
                    LOGI("R... OnDVBService  CallStaticVoidMethod end\n");
		}
                
                
                jvm->DetachCurrentThread();

	}
	
        // 搜索进度
	virtual void OnProgress(U32 iPercent)
	{
		LOGI("R... OnProgress received\n");
                JNIEnv * currentEnv = NULL;
                jvm->AttachCurrentThread(&currentEnv, NULL);

                currentEnv->CallStaticVoidMethod(gclsa, gprogressid,iPercent);
                jvm->DetachCurrentThread();

	}

	// Tuner状态的参数
	virtual void OnTunerInfo(const TuningParam& tuning,const TunerSignal& signal)
	{
		LOGI("R... OnTunerInfo received Tuning(%d,%d,%d) start\n",tuning.freq,tuning.symb,tuning.qam);
                LOGI("R... OnTunerInfo received Signal(%d,%d,%d) start \n",signal.Level,signal.CN,signal.ErrRate);
                
                // 定义全局JNIEnv对象
                JNIEnv * currentEnv = NULL;
                jvm->AttachCurrentThread(&currentEnv, NULL);

                if(currentEnv == NULL){
                   return;
                }

//不能从这里取得，运行时会报错
//LOGI("R... OnTunerInfo FindClass TuningParam \n");
//jclass clsTp = currentEnv->FindClass("novel/supertv/dvb/jni/struct/TuningParam");
//jclass clsTs = currentEnv->FindClass("novel/supertv/dvb/jni/struct/tagTunerSignal");
//LOGI("R... OnTunerInfo FindClass tagTunerSignal \n");

    //获取方法的ID，init是构造函数，(III)V是三个int参数和V是void
    jmethodID conidTp = currentEnv->GetMethodID(clsTp, "<init>", "(III)V");

    //创建实例
    jobject objtmpTp = currentEnv->NewObject(clsTp, conidTp);

    // 销毁实例类
    //currentEnv->DeleteLocalRef(clsTp);
    
    //获取类中每一个变量的定义
    jclass clsaTp = currentEnv->GetObjectClass(objtmpTp);
    
    jfieldID freq = currentEnv->GetFieldID(clsaTp, "Frequency", "I");
    jfieldID symb = currentEnv->GetFieldID(clsaTp, "SymbolRate", "I");
    jfieldID qam = currentEnv->GetFieldID(clsaTp, "Modulation", "I");
    
    // 赋值
    currentEnv->SetIntField(objtmpTp,freq,tuning.freq);
    currentEnv->SetIntField(objtmpTp,symb,tuning.symb);
    currentEnv->SetIntField(objtmpTp,qam,tuning.qam);

    // 销毁临时实例类，不能在这里使用，所以注释掉
    //currentEnv->DeleteLocalRef(objtmpTp);
    //currentEnv->DeleteLocalRef(clsaTp);

        //获取方法的ID，init是构造函数，(III)V是三个int参数和V是void
    jmethodID conidTs = currentEnv->GetMethodID(clsTs, "<init>", "(III)V");

    //创建实例
    jobject objtmpTs = currentEnv->NewObject(clsTs, conidTs);

    // 销毁实例类,不能在这里使用，所以注释掉
    //currentEnv->DeleteLocalRef(clsTs);
    
    //获取类中每一个变量的定义
    jclass clsaTs = currentEnv->GetObjectClass(objtmpTs);
    


    jfieldID level = currentEnv->GetFieldID(clsaTs, "Level", "I");
    jfieldID cn = currentEnv->GetFieldID(clsaTs, "CN", "I");
    jfieldID errRate = currentEnv->GetFieldID(clsaTs, "ErrRate", "I");
    
    // 赋值
    currentEnv->SetIntField(objtmpTs,level,signal.Level);
    currentEnv->SetIntField(objtmpTs,cn,signal.CN);
    currentEnv->SetIntField(objtmpTs,errRate,signal.ErrRate);

    // 销毁临时实例类
    //currentEnv->DeleteLocalRef(objtmpTs);
    //currentEnv->DeleteLocalRef(clsaTs);

//jvalue argSetMessage[2]; 
//argSetMessage[0].l = (jobject)tuning; 
//argSetMessage[1].l = (jobject)signal;

//currentEnv->CallStaticVoidMethodA(gclsa, gtuinfoid,argSetMessage);

currentEnv->CallStaticVoidMethod(gclsa, gtuinfoid,objtmpTp,objtmpTs);
jvm->DetachCurrentThread();
                LOGI("R... OnTunerInfo received  end\n");
	}
	// ÆµµÀËÑË÷Íê³ÉÍšÖª
	virtual void OnSTVComplete()
	{
		LOGI("R... OnSTVComplete received\n");
                //jclass cls = JNIEnv*->FindClass("novel/supertv/dvb/jni/struct/TuningParam");
                //jmethodID conid = JNIEnv*->GetMethodID(cls, "<init>", "()V");
                
                //use gobal menber
                //jclass clsa = genv->GetObjectClass(gnotify);
                //jclass cls = genv->FindClass("novel/supertv/dvb/jni/ISearchTVNotify");
                //jmethodID conid = genv->GetMethodID(cls, "<OnSTVComplete>", "()V");

                // use getEnv
                JNIEnv * currentEnv = NULL;

               // JavaVM * jvm = NULL;
                
                jvm->AttachCurrentThread(&currentEnv, NULL);
                //jvm->GetEnv((void**)&currentEnv, JNI_VERSION_1_4);     
                //jvm->GetEnv((void**)&currentEnv, JNI_VERSION_1_4);
                LOGI("R... OnSTVComplete CallVoidMethod AttachCurrentThread start\n");
                //jclass clsa = currentEnv->GetObjectClass(gnotify);
//LOGI("R... OnSTVComplete CallVoidMethod GetObjectClass start\n");
                //jclass cls = currentEnv->FindClass("novel/supertv/dvb/jni/struct/SearchCallBack");
                //jclass cls = currentEnv->FindClass("novel/supertv/dvb/jni/ISearchTVNotify");
LOGI("R... OnSTVComplete CallVoidMethod FindClass start\n");
                //jmethodID conid = currentEnv->GetMethodID(clsa, "OnSTVComplete", "()V");
                LOGI("R... OnSTVComplete CallVoidMethod OnSTVComplete start\n");
                //currentEnv->CallVoidMethod(gclsa, gconid);
                currentEnv->CallStaticVoidMethod(gclsa, gconid);
//currentEnv->DeleteLocalRef(gclsa);
                LOGI("R... OnSTVComplete CallVoidMethod OnSTVComplete\n");
                jvm->DetachCurrentThread();

	}
	// œÚÄ¿ËÑË÷Íê³ÉÍšÖª
	virtual void OnSEPGComplete()
	{
		LOGI("R... OnSEPGComplete received\n");
	}
	// Ö÷ÆµµãNIT°æ±ŸžüÐÂÍšÖª
	virtual void OnNitVersionChanged(U8 iVersion)
	{
		LOGI("R... OnNitVersionChanged received\n");
	}
	// œÚÄ¿Ô€ÔŒµœÆÚÍšÖª
	virtual void OnSubscriptionExpiry(U32 iId,TVSubscriptionT& info)
	{
		LOGI("R... OnSubscriptionExpiry received\n");
	}
	// CAÏûÏ¢ÍšÖª(ŒÓÈÅ,OSD...)
	virtual void OnCAMessage(U8 type,const CAMessageT* pMSG)
	{
		LOGI("R... OnCAMessage received\n");
	}
	// EPG P/FÊÂŒþÍšÖª(P/F,AS...)
	virtual void OnEpgEventChange(EITEventType type)
	{
		LOGI("R... OnEpgEventChange received\n");
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
	
    TuningParam tpp;

    //»ñÈ¡ÀàÖÐÃ¿Ò»žö±äÁ¿µÄ¶šÒå
    jclass clsa = env->GetObjectClass(tuningParam);
	
  	 env->GetJavaVM(&jvm);

    jfieldID freq = env->GetFieldID(clsa, "Frequency", "I");
    jfieldID symb = env->GetFieldID(clsa, "SymbolRate", "I");
    jfieldID qam = env->GetFieldID(clsa, "Modulation", "I");

// test ok
LOGI("... StartSearchTV FindClass TuningParam \n");
clsTp = env->FindClass("novel/supertv/dvb/jni/struct/TuningParam");
clsTs = env->FindClass("novel/supertv/dvb/jni/struct/tagTunerSignal");
clsService = env->FindClass("novel/supertv/dvb/jni/struct/tagDVBService");
gstrClass = env->FindClass("java/lang/String");
LOGI("... StartSearchTV FindClass tagTunerSignal \n");

 	//jclass cls = env->FindClass("novel/supertv/dvb/jni/struct/TuningParam");
    //jmethodID conid = env->GetMethodID(cls, "<init>", "()V");
    //jobject objtmp = env->NewObject(cls, conid);
//jclass clsa = env->GetObjectClass(objtmp);
    //jfieldID freq = env->GetFieldID(clsa, "Frequency", "I");
    //jfieldID symb = env->GetFieldID(clsa, "SymbolRate", "I");
    //jfieldID qam = env->GetFieldID(clsa, "Modulation", "I");



    // ³õÊŒ»¯£¬ÇåÁã
    tpp.freq = 0;
    tpp.symb = 0;
    tpp.qam = 0;

    // ž³Öµ
    tpp.freq = env->GetIntField(tuningParam,freq);
    tpp.symb = env->GetIntField(tuningParam,symb);
    tpp.qam = env->GetIntField(tuningParam,qam);
	LOGI("FREQ=%d,sysm=%d,qam=%d",tpp.freq,tpp.symb,tpp.qam);
    //switch(stvMode){
    //case 0:
    //      StartSearchTV(STVMODE_MANUAL, tpp, notify);
    //      break;    
    //case 1:
    //      StartSearchTV(STVMODE_FULL, tpp, notify);
    //      break;
    //case 2:
    //      StartSearchTV(STVMODE_NIT, tpp, notify);
    //      break;
    //}

    // set the call back function.
    //genv = env;
    //gnotify = notify;
    gclsa = env->GetObjectClass(obj);
    //gconid = env->GetMethodID(gclsa, "OnSTVComplete", "()V");
    gconid = env->GetStaticMethodID(gclsa, "OnSTVComplete", "()V");
    gprogressid = env->GetStaticMethodID(gclsa, "OnProgress", "(I)V");
    gtuinfoid = env->GetStaticMethodID(gclsa, "OnTunerInfo", "(Lnovel/supertv/dvb/jni/struct/TuningParam;Lnovel/supertv/dvb/jni/struct/tagTunerSignal;)V");
    gserviceid = env->GetStaticMethodID(gclsa, "OnDVBService", "(Lnovel/supertv/dvb/jni/struct/tagDVBService;)V");

    //env->CallStaticVoidMethod(gclsa, gconid);
    
    LOGI("StartSearchTV stvMode = %d",stvMode);

    if(stvMode == 0)
    {
        LOGI("StartSearchTV STVMODE_MANUAL");
	StartSearchTV(STVMODE_MANUAL, &tpp, &callback);
    }else if(stvMode == 1){
        LOGI("StartSearchTV STVMODE_FULL");
	StartSearchTV(STVMODE_FULL, &tpp, &callback);
    }else if(stvMode == 2){
        LOGI("StartSearchTV STVMODE_NIT");
	StartSearchTV(STVMODE_NIT, &tpp, &callback);
    }
    //env->ExceptionDescribe();
    //env->ExceptionClear();
    //env->DeleteLocalRef(obj);
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
    CancelSearchTV();
    return 1;
}


};

