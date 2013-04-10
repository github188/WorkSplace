#include <jni.h>
#include <android/log.h>

#include <tvcore.h>
#include <tvcomm.h>
#include <capture_def.h>


#ifdef LOG_TAG
#undef LOG_TAG
#undef LOGI
#undef LOGE
#endif

#define  LOG_TAG    "chehl_JNISetting"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)


//在ChannelPlay中定义
jstring CreateJstringFromGB2312(JNIEnv * env, const char * pStr);

#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     novel_supertv_dvb_jni_JniSetting
 * Method:    setScreen
 * Signature: (I)I
 */
JNIEXPORT jboolean JNICALL Java_novel_supertv_dvb_jni_JniSetting_setDisplayRect
  (JNIEnv * env, jobject obj, jint x, jint y, jint width, jint height)
{
	LOGI("Jni_Setting_setDisplayRect in %d,%d,%d,%d\n", x, y, width, height);
	bool ret =  tvcore_setDisplayRect(x, y, width, height);
	LOGI("Jni_Setting_setDisplayRect out %s\n", ret ? "true" : "false");
	return ret;
}

JNIEXPORT jboolean JNICALL Java_novel_supertv_dvb_jni_JniSetting_setDisplayMode(JNIEnv * env, jobject obj, jint mode)
{
    LOGI("Jni_Setting_setDisplayMode in %d\n", mode);
    bool ret =  tvcore_setDisplayZoomMode(mode);
    LOGI("Jni_Setting_setDisplayMode out %s\n", ret ? "true" : "false");
    return ret;
}

JNIEXPORT jboolean JNICALL Java_novel_supertv_dvb_jni_JniSetting_cleanVideoFrame(JNIEnv * env, jobject obj, jboolean clean)
{
    LOGI("Jni_Setting_cleanVideoFrame in %s\n", clean ? "true" : "false");
    bool ret =  tvcore_cleanVideoFrame(clean);
    LOGI("Jni_Setting_cleanVideoFrame out %s\n", ret ? "true" : "false");
    return ret;
}
/*
 * Class:     novel_supertv_dvb_jni_JniSetting
 * Method:    setVolume
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniSetting_setVolume
  (JNIEnv * env, jobject obj, jint volume)
{
    LOGI("Jni_Setting_setVolume in %d\n", volume);
    int ret = tvcore_setVolume(volume);
    LOGI("Jni_Setting_setVolume out %d\n", ret);
    return ret;
}

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniSetting_getVolume
  (JNIEnv * env, jobject obj)
{
    LOGI("Jni_Setting_getVolume in \n");
    int volume = tvcore_getVolume();
    LOGI("Jni_Setting_getVolume out %d \n",volume);
    return volume;
}

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniSetting_setAudioChannel(JNIEnv * env, jobject obj, jint channel)
{
    LOGI("Jni_Setting_setAudioChannel in %d\n",channel);
    bool ret = tvCore_setAudioChannel((AudioStereoMode)channel);
    LOGI("Jni_Setting_setAudioChannel out %s\n", ret ? "true" : "false");
    return ret ? 1 : -1;
}

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniSetting_getAudioChannel(JNIEnv * env, jobject obj)
{
    LOGI("Jni_Setting_getAudioChannel in\n");
    int channel = tvcore_getAudioChannel();
    LOGI("Jni_Setting_getAudioChannel out %d\n",channel);
    return channel;
}

/*
 * Class:     novel_supertv_dvb_jni_JniSetting
 * Method:    setLanguage
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniSetting_setAudioLanguage
  (JNIEnv * env, jobject obj, jint index)
{
    LOGI("Jni_Setting_setLanguage in %d...\n",index);
	if(index > AUDIOSTREAM_MAXCOUNT)
	{
		index = 0;
	}
	bool ret = tvcore_set_audio_lang(index);
    return ret ? 1 : -1;
}

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniSetting_getAudioLanguage
  (JNIEnv * env, jobject obj)
{
    LOGI("Jni_Setting_getLanguage in...\n");
	return tvcore_get_audio_lang();
}

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniSetting_caInit(JNIEnv * env, jobject obj)
{ 
    LOGI("Jni_Setting_caInit in \n");
	return 0;//tvcore_init();
}

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniSetting_caUnInit(JNIEnv * env, jobject obj)
{
	LOGI("Jni_Setting_caUnInit in \n");
	return 0;//tvcore_uninit();
}

//////////////////////////////////////////////
/*
 * Class:     novel_supertv_dvb_jni_JniSetting
 * Method:    tvcore_set_watch_time0
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniSetting_setWatchTime0
  (JNIEnv *env , jobject obj, jint startHour, jint endHour)
{
    LOGI("Jni_Setting_setWatchTime0 in %d,%d\n", startHour, endHour);
  //  tvcore_set_watch_time0((U8)startHour, (U8)endHour);
    LOGI("Jni_Setting_setWatchTime0 out \n");
    return 1;
}

/*
 * Class:     novel_supertv_dvb_jni_JniSetting
 * Method:    tvcore_set_watch_time1
 * Signature: (Ljava/lang/String;II)I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniSetting_setWatchTime1
  (JNIEnv * env, jobject obj, jstring pin, jint startHour, jint endHour)
{
    LOGI("Jni_Setting_setWatchTime1 in\n");
    const char * pPIN = env->GetStringUTFChars(pin, JNI_FALSE); 

	LOGI("Jni_Setting_setWatchTime2 outstrlen(pPIN),%d,%s\n",strlen(pPIN),pPIN);;
    U16 ret = tvcore_set_watch_time1((U8*)pPIN,(U8)startHour, (U8)endHour);
    env->ReleaseStringUTFChars(pin,pPIN);
    LOGI("Jni_Setting_setWatchTime1 out \n");
    return ret;
}

/*
 * Class:     novel_supertv_dvb_jni_JniSetting
 * Method:    tvcore_set_watch_time2
 * Signature: (Ljava/lang/String;IIIIII)I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniSetting_setWatchTime2
  (JNIEnv * env, jobject obj, jstring pin, jint startHour, jint endHour, jint startMin, jint endMin, jint startSec, jint endSec)
{
    LOGI("Jni_Setting_setWatchTime2 in\n");
    const char * pPIN = env->GetStringUTFChars(pin, JNI_FALSE); 
	
    U16 ret = tvcore_set_watch_time2((U8*)pPIN,(U8)startHour, (U8)endHour,
                        (U8)startMin,(U8)endMin,(U8)startSec,(U8)endSec);
    env->ReleaseStringUTFChars(pin, pPIN);
    LOGI("Jni_Setting_setWatchTime2 out\n");
    return ret;
}

/*
 * Class:     novel_supertv_dvb_jni_JniSetting
 * Method:    get_watchtime
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniSetting_getWatchTime
  (JNIEnv * env, jobject obj, jobject watchObj)
{
    LOGI("Jni_Setting_getWatchTime in\n");
    U8 startH = 0, startM = 0,startS = 0;
    U8 endH = 0, endM = 0, endS = 0;
    U16 ret = tvcore_get_watch_time1(&startH, &startM, &startS, &endH, &endM, &endS);
    jclass watchClass = env->FindClass("novel/supertv/dvb/jni/struct/tagWatchTime");
//	jmethodID watchConID = env->GetMethodID(watchClass,"<init>", "()V");
 //   jobject watchObj = env->NewObject(watchClass, watchConID);
    
    jfieldID startHID = env->GetFieldID(watchClass, "startHour","I");
    jfieldID startMID = env->GetFieldID(watchClass, "startMin","I");
    jfieldID startSID = env->GetFieldID(watchClass, "startSec","I");
    jfieldID endHID = env->GetFieldID(watchClass, "endHour","I");
    jfieldID endMID = env->GetFieldID(watchClass, "endMin","I");
    jfieldID endSID = env->GetFieldID(watchClass, "endSec","I");
    
    LOGI("Jni_Setting_getWatchTime %d,%d,%d,%d,%d,%d\n",startH,endH,startM,endM,startS,endS);
    env->SetIntField(watchObj, startHID, startH & 0x00FF);
    env->SetIntField(watchObj, startMID, startM & 0x00FF);
    env->SetIntField(watchObj, startSID, startS & 0x00FF);
    env->SetIntField(watchObj, endHID, endH & 0x00FF);
    env->SetIntField(watchObj, endMID, endM & 0x00FF);
    env->SetIntField(watchObj, endSID, endS & 0x00FF);
    
    LOGI("Jni_Setting_getWatchTime out\n");
    return ret;
}

/*
 * Class:     novel_supertv_dvb_jni_JniSetting
 * Method:    tvcore_change_pincode
 * Signature: (Ljava/lang/String;Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniSetting_changePincode
  (JNIEnv * env, jobject obj, jstring oldPin, jstring newPin)
{
    LOGI("Jni_Setting_changePincode in\n");
    const char * pOld = env->GetStringUTFChars(oldPin, JNI_FALSE);   
    const char * pNew = env->GetStringUTFChars(newPin, JNI_FALSE);   
	
	LOGI("Jni_Setting_chagngePincodeout,%d,%s\n",strlen(pNew),pNew);;
    LOGI("Jni_Setting_changePincode old:%s,new:%s\n", pOld, pNew);
    U16 ret = tvcore_change_pincode((U8*)pOld, (U8*)pNew);

    env->ReleaseStringUTFChars(oldPin,pOld);
    env->ReleaseStringUTFChars(newPin,pNew);
    LOGI("Jni_Setting_changePincode out %d\n",ret);
    return jint(ret & 0x0000FFFF);
}


JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniSetting_getWatchLevel(JNIEnv * env, jobject obj)
{
    LOGI("Jni_Setting_getWatchLevel in\n");
    int ret = (tvcore_get_watch_level() & 0x000000FF);
    LOGI("Jni_Setting_getWatchLevel out\n");
    return ret;
}

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniSetting_setWatchLevel(JNIEnv * env, jobject obj, jstring pin, jint level)
{
    LOGI("Jni_Setting_setWatchLevel in\n");
    const char * pPIN = env->GetStringUTFChars(pin, JNI_FALSE);   
    jint ret = (jint)(tvcore_set_watch_level((U8*)pPIN,(U8)level) & 0x0000FFFF);
    env->ReleaseStringUTFChars(pin, pPIN);
    LOGI("Jni_Setting_setWatchLevel out\n");
    return ret;
}

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniSetting_getOperatorAC(JNIEnv * env, jobject obj, jint operId, jobject listObj)
{
    jclass clsArrayList = env->FindClass("java/util/ArrayList");
	jmethodID medAdd_id = env->GetMethodID(clsArrayList,"add","(Ljava/lang/Object;)Z");
    jclass intClass = env->FindClass("java/lang/Integer");
    jmethodID intConID = env->GetMethodID(intClass, "<init>","(I)V");

	std::vector<U32> acs;
	U16 ret = tvcore_get_operator_acs(operId, acs);
	for(int i = 0; i < acs.size(); i++)
	{
        jobject intObj = env->NewObject(intClass, intConID, acs[i]);
        env->CallBooleanMethod(listObj, medAdd_id, intObj);
        env->DeleteLocalRef(intObj);
	}
	return ret;
}

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniSetting_getAuthorization(JNIEnv * env, jobject obj, jint operId, jobject vecObj)
{
    LOGI("Jni_Setting_getAuthorization in\n");
    EntitleListT infos;
    U16 ret = tvcore_get_authorization((OperatorId)operId,infos);
    LOGI("Jni_Setting_getAuthorization entitle size %d\n",infos.size());

    jclass clsvec = env->FindClass("java/util/Vector");

//    jmethodID vecConID = env->GetMethodID(clsvec,"<init>","()V");
//   jobject vecObj = env->NewObject(clsvec,vecConID);

     // Get "addElement" method id of clase vector
    jmethodID addElemID = env->GetMethodID( clsvec, "addElement", "(Ljava/lang/Object;)V");
    if (addElemID == NULL)
        printf("method ID not valid\n\n");

    jclass entitleClass = env->FindClass("novel/supertv/dvb/jni/struct/tagEntitle");
    jmethodID entitleConID = env->GetMethodID( entitleClass, "<init>","()V");

    jfieldID productID = env->GetFieldID(entitleClass, "product_id","I");
    jfieldID recordID = env->GetFieldID(entitleClass, "is_record","Z");
    jfieldID startTimeID = env->GetFieldID(entitleClass, "start_time","I");
    jfieldID expiredTimeID = env->GetFieldID(entitleClass, "expired_time","I");

    //循环复制
    EntitleListT::iterator it = infos.begin();
    for(; it != infos.end(); it++)
    {
        jobject entitleObj = env->NewObject(entitleClass, entitleConID);

        env->SetIntField(entitleObj,productID,it->product_id);
        env->SetBooleanField(entitleObj,recordID,it->is_record);
        env->SetIntField(entitleObj,startTimeID,it->start_time);
        env->SetIntField(entitleObj,expiredTimeID,it->expired_time);

        env->CallVoidMethod(vecObj, addElemID, entitleObj);
        env->DeleteLocalRef(entitleObj);
    }
    LOGI("Jni_Setting_getAuthorization out\n");
    return ret;
}

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniSetting_getOperatorID(JNIEnv * env, jobject obj, jobject vecObj)
{
    LOGI("Jni_Setting_getOperatorID in\n");
    OperatorIDListT ids;
    U16 ret = tvcore_get_operator_id(ids);
    LOGI("Jni_Setting_getOperatorID operator size %d\n",ids.size());
	
    jclass clsvec = env->FindClass("java/util/Vector");

//    jmethodID vecConID = env->GetMethodID(clsvec,"<init>","()V");
//    jobject vecObj = env->NewObject(clsvec,vecConID);

     // Get "addElement" method id of clase vector
    jmethodID addElemID = env->GetMethodID( clsvec, "addElement", "(Ljava/lang/Object;)V");
    if (addElemID == NULL)
        printf("method ID not valid\n\n");

    jclass intClass = env->FindClass("java/lang/Integer");
    jmethodID intConID = env->GetMethodID(intClass, "<init>","(I)V");

    //循环复制
    OperatorIDListT::iterator it = ids.begin();
    for(; it != ids.end(); it++)
    {
        jobject intObj = env->NewObject(intClass, intConID, (int)((*it) & 0x0000FFFF));
        env->CallVoidMethod(vecObj, addElemID, intObj);
        env->DeleteLocalRef(intObj);
    }
    LOGI("Jni_Setting_getOperatorID ont\n");
    return ret;
}


JNIEXPORT jstring JNICALL Java_novel_supertv_dvb_jni_JniSetting_getCAVersion(JNIEnv * env, jobject obj)
{
    LOGI("Jni_Setting_getCAVersion in\n");
	char buf[256];
	memset(buf,0,256);
	tvcore_get_caVersion(buf);
	if(strlen(buf) > 0)
	{
		LOGI("Jni_Setting_getCAVersion %s\n",buf);
        return env->NewStringUTF(buf);
	}
    return env->NewStringUTF("");
    LOGI("Jni_Setting_getCAVersion out\n");
}

JNIEXPORT jstring JNICALL Java_novel_supertv_dvb_jni_JniSetting_getCardSN(JNIEnv * env, jobject obj)
{
    LOGI("Jni_Setting_getCardSN in\n");
    char buf[256];
    memset(buf,0,256);
    U8 snlen = 255;
    bool ret = tvcore_get_cardsn(buf,&snlen);
    if(ret)
    {
        LOGI("Jni_Setting_getCardSN out %s\n",buf);
        LOGI("Jni_Setting_getCardSN out len %d\n",snlen);
        return env->NewStringUTF(buf);
    }
    LOGI("Jni_Setting_getCardSN error out\n");
    return env->NewStringUTF("");
}

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniSetting_getEMailHeads(JNIEnv * env, jobject thiz, jobject list)
{
	EmailHead heads[10];
	U8 count = 0;
	U8 index = 0;
	bool bRet = false;

	jclass clsEmailHead = env->FindClass("novel/supertv/dvb/jni/struct/tagEmailHead");
    jmethodID medEmailHeadConID = env->GetMethodID(clsEmailHead,"<init>", "()V");

	jfieldID emailID_id = env->GetFieldID(clsEmailHead,"mEmailID","I");
	jfieldID emailNew_id = env->GetFieldID(clsEmailHead,"mNewEmail","Z");
	jfieldID emailTitle_id = env->GetFieldID(clsEmailHead,"mEmailTitle","Ljava/lang/String;");
	jfieldID emailSendTime_id = env->GetFieldID(clsEmailHead,"mEmailSendTime","I");
	jfieldID emailLevel_id = env->GetFieldID(clsEmailHead,"mEmailLevel","I");
	
    jclass clsArrayList = env->FindClass("java/util/ArrayList");
	jmethodID medAdd_id = env->GetMethodID(clsArrayList,"add","(Ljava/lang/Object;)Z");


	while(true)
	{
		count = 10;
		memset(&heads, 0, sizeof(heads));
		bRet = tvcore_getEMailHeads(heads, &count, &index); 
		if(bRet == false)
			return -1;

		//复制数据
		for(int i = 0; i < count; i++)
		{
			LOGI("Jni_Setting_getEMailHeads %d %d %d %d %s\n",heads[i].email_id,heads[i].new_email,
										heads[i].send_time,heads[i].email_level,heads[i].email_title);
			jobject head = env->NewObject(clsEmailHead, medEmailHeadConID);

			env->SetIntField(head, emailID_id, heads[i].email_id);
			env->SetBooleanField(head, emailNew_id, heads[i].new_email);
			jstring jstrTitle = CreateJstringFromGB2312(env, heads[i].email_title);
			env->SetObjectField(head, emailTitle_id, jstrTitle);
			env->DeleteLocalRef(jstrTitle);
			env->SetIntField(head, emailSendTime_id, heads[i].send_time);
			env->SetIntField(head, emailLevel_id, heads[i].email_level);

			env->CallBooleanMethod(list, medAdd_id, head);
			env->DeleteLocalRef(head);
		}
		if(count < 10)
			break;
	}
	return 0;
}

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniSetting_getEMailHead(JNIEnv * env, jobject thiz, jint emailId, jobject head)
{
	EmailHead emailhead;
	memset(&emailhead, 0, sizeof(EmailHead));


	bool ret = tvcore_getEMailHead(emailId, emailhead);
	if(ret)
	{
		jclass clsEmailHead = env->FindClass("novel/supertv/dvb/jni/struct/tagEmailHead");
		jfieldID emailID_id = env->GetFieldID(clsEmailHead,"mEmailID","I");
		jfieldID emailNew_id = env->GetFieldID(clsEmailHead,"mNewEmail","Z");
		jfieldID emailTitle_id = env->GetFieldID(clsEmailHead,"mEmailTitle","Ljava/lang/String;");
		jfieldID emailSendTime_id = env->GetFieldID(clsEmailHead,"mEmailSendTime","I");
		jfieldID emailLevel_id = env->GetFieldID(clsEmailHead,"mEmailLevel","I");

		env->SetIntField(head, emailID_id, emailhead.email_id);
		env->SetBooleanField(head, emailNew_id, emailhead.new_email);

		jstring jstrTitle = CreateJstringFromGB2312(env, emailhead.email_title);
		env->SetObjectField(head, emailTitle_id, jstrTitle);

		env->SetIntField(head, emailSendTime_id, emailhead.send_time);
		env->SetIntField(head, emailLevel_id, emailhead.email_level);
		return 1;
	}
	return -1;
}

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniSetting_getEMailContent(JNIEnv * env, jobject thiz, jint emailId, jobject content)
{
	EmailContent cont;
	memset(&cont,0,sizeof(EmailContent));

	LOGI("Jni_Setting_getEMailContent %d\n",emailId);
	bool ret = tvcore_getEMailContent(emailId,&cont);
	if(ret)
	{
		LOGI("Jni_Setting_getEMailContent %d %s\n",emailId,cont.m_szEmail);
		jclass clsEmailContent = env->FindClass("novel/supertv/dvb/jni/struct/tagEmailContent");
		jfieldID emailContent_id = env->GetFieldID(clsEmailContent,"mEmailContent","Ljava/lang/String;");
		jfieldID emailReserved_id = env->GetFieldID(clsEmailContent, "mReserved", "I");

		jstring jstrContent = CreateJstringFromGB2312(env, cont.m_szEmail);
		env->SetObjectField(content, emailContent_id, jstrContent);
		int reserved = (*(int*)(cont.m_byReserved)) & 0x00FFFFFF;
		env->SetIntField(content, emailReserved_id, reserved);
		return 1;
	}
	return 0;
}


JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniSetting_delEMail(JNIEnv * env, jobject thiz, jint emailId)
{
    LOGI("Jni_Setting_delEMail %d\n",emailId);
	tvcore_delEMail(emailId);
	return 1;
}

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniSetting_getEMailIdleSpace(JNIEnv * env, jobject thiz)
{
	U8 used = 0;
	U8 empty = 0;
	tvcore_getEMailSpaceInfo(used,empty);
	return empty;
}

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniSetting_getEMailUsedSpace(JNIEnv * env, jobject thiz)
{
	U8 used = 0;
	U8 empty = 0;
	tvcore_getEMailSpaceInfo(used,empty);
	return used;
}

JNIEXPORT jstring JNICALL Java_novel_supertv_dvb_jni_JniSetting_getSTBId(JNIEnv * env , jobject)
{
	char pId[16] = {0};
	bool result = tvcore_getSTBId(pId, 16);
	if(result)
		return env->NewStringUTF(pId);
	return env->NewStringUTF("");
}

JNIEXPORT jstring JNICALL Java_novel_supertv_dvb_jni_JniSetting_getUTCTime(JNIEnv * env, jobject thiz)
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



#ifdef __cplusplus
}
#endif
