#include "Common.h"
#include "assert.h"


JavaVM* JniCommon::jvm = NULL;
map<JniCommon::eJniObjectType, JniObject*> JniCommon::jniObjects;


uint8 PSI_CnvBcdToVal( uint8  Bcd )
{
     return( ( Bcd >> 4 ) * 10 + ( Bcd & 0x0f ) );				/* Convert BCD to Value */
}

void JniCommon::SetJavaVM(JavaVM *jvm){
    if(JniCommon::jvm == NULL){
        JniCommon::jvm = jvm;
        JNIEnv * env = NULL;
        jvm->GetEnv((void**)&env, JNI_VERSION_1_4);

		jniObjects.insert(make_pair(JniObjectType_Int, new JniIntObject()));
		jniObjects.insert(make_pair(JniObjectType_Char, new JniCharObject()));
		jniObjects.insert(make_pair(JniObjectType_Transponder, new JniTransponder()));
		jniObjects.insert(make_pair(JniObjectType_TunerLockParam, new JniTunerLockParam()));
		jniObjects.insert(make_pair(JniObjectType_stChannel, new JniChannel()));
		jniObjects.insert(make_pair(JniObjectType_stServiceIdent, new JniServiceIdent()));
		jniObjects.insert(make_pair(JniObjectType_Date, new JniDate()));
		jniObjects.insert(make_pair(JniObjectType_LoaderState, new JniLoaderState()));		
		jniObjects.insert(make_pair(JniObjectType_DVBDateTime, new JniDVBDateTime()));
		jniObjects.insert(make_pair(JniObjectType_stVideoTrack, new JnistVideoTrack()));
		jniObjects.insert(make_pair(JniObjectType_stAudioTrack, new JnistAudioTrack()));
		jniObjects.insert(make_pair(JniObjectType_stPidStream, new JnistPidStream()));
		jniObjects.insert(make_pair(JniObjectType_IPvrStreamInfo, new JniIPvrStreamInfo()));
		jniObjects.insert(make_pair(JniObjectType_IPvrRecOpenParam, new JniIPvrRecOpenParam()));
		jniObjects.insert(make_pair(JniObjectType_IPvrRecStartParam, new JniIPvrRecStartParam()));
		jniObjects.insert(make_pair(JniObjectType_IPvrPbOpenParam, new JniIPvrPbOpenParam()));
		jniObjects.insert(make_pair(JniObjectType_IPvrPbConfigParam, new JniIPvrPbConfigParam()));
		jniObjects.insert(make_pair(JniObjectType_IPvrPbStartParam, new JniIPvrPbStartParam()));
		jniObjects.insert(make_pair(JniObjectType_IPSearchTp, new JniIPSearchTp()));
		jniObjects.insert(make_pair(JniObjectType_IPSearchList, new JniIPSearchList()));
		jniObjects.insert(make_pair(JniObjectType_IPSearchOpenParam, new JniIPSearchOpenParam()));
		jniObjects.insert(make_pair(JniObjectType_IPSearchCBParam_List, new JniIPSearchCBParam_List()));
		jniObjects.insert(make_pair(JniObjectType_IPSearchCBParam_Tp, new JniIPSearchCBParam_Tp()));
		jniObjects.insert(make_pair(JniObjectType_stTunerState, new JnistTunerState()));
		jniObjects.insert(make_pair(JniObjectType_stNetwork, new JnistNetwork()));
		jniObjects.insert(make_pair(JniObjectType_IPSearchCBParam_TpInfo, new JniIPSearchCBParam_TpInfo()));
		jniObjects.insert(make_pair(JniObjectType_IPPlayOpenParam, new JniIPPlayOpenParam()));
		jniObjects.insert(make_pair(JniObjectType_IPPlayStartParam, new JniIPPlayStartParam()));
		jniObjects.insert(make_pair(JniObjectType_ISystemInfoParam, new JniISystemInfoParam()));

		for(map<JniCommon::eJniObjectType, JniObject*>::iterator i = jniObjects.begin() ;
			i != jniObjects.end() ; i ++){
			JniObject* pObject = (*i).second;
			pObject->initObject(env);
        }
    }
}

JNIEnv * JniCommon::GetCurrentEnv(){
    JNIEnv * currentEnv = NULL;
    if(jvm != NULL){
        if(jvm->GetEnv((void**)&currentEnv, JNI_VERSION_1_4) != JNI_OK){
            if(jvm->AttachCurrentThread(&currentEnv, NULL) != JNI_OK){
                return NULL;
            }
        }
        return currentEnv;
    }
    return NULL;
}

jstring JniCommon::CharTojstring(JNIEnv *env, const char *str, int len, const char *charCode)
{
	if (str == NULL || len == 0)
	{
		LOGE("JniCommon::CharTojstring(), invalid input!str=%p, len=%d ", str, len);
		return 0;
	}

	jstring rtn = 0;
	jclass clsstring = env->FindClass("java/lang/String");
	jstring strencode = env->NewStringUTF((charCode != NULL) ? charCode : "gbk");
	jmethodID mid = env->GetMethodID(clsstring,"<init>","([BLjava/lang/String;)V");
	jbyteArray barr = env->NewByteArray(len);
	env->SetByteArrayRegion(barr, 0, len, (jbyte*)str);

	rtn = (jstring)env->NewObject(clsstring,mid,barr,strencode);
	env->DeleteLocalRef(clsstring);
	env->DeleteLocalRef(strencode);
	env->DeleteLocalRef(barr);
	
	return rtn;
}

jstring JniCommon::CharTojstring(JNIEnv *env, stTextContent & text)
{
	const char *pText = "";
	uint32 textLength = 0;
	const char *pCharCode = "gbk";
	if (text.valid())
	{
		pText = (char *)text.pText;
		textLength = text.textLength;
		
		switch (text.codeType)
		{
			case eCharCode_UNICODE:
			case eCharCode_UCS2:
			case eCharCode_GB13000:
				pCharCode= "unicode";
				break;
			
			case eCharCode_unknown:
			case eCharCode_GB18030:
			case eCharCode_GBK:
			case eCharCode_GB2312:
			case eCharCode_ASCII:
				pCharCode = "gbk";
				break;
				
			case eCharCode_BIG5:
				pCharCode = "big5";
				break;
				
			case eCharCode_KSC5601:
				pCharCode = "ksc5601";
				break;

			default:
				LOGE("JniCommon::CharTojstring(), unsupport char code! type=%d ", text.codeType);
				return 0;
		}
	}

	jstring rtn = 0;
	jclass clsstring = env->FindClass("java/lang/String");
	jstring strencode = env->NewStringUTF(pCharCode);
	jmethodID mid = env->GetMethodID(clsstring,"<init>","([BLjava/lang/String;)V");
	jbyteArray barr = env->NewByteArray(textLength);
	env->SetByteArrayRegion(barr, 0, textLength, (jbyte*)pText);

	rtn = (jstring)env->NewObject(clsstring,mid,barr,strencode);
	env->DeleteLocalRef(clsstring);
	env->DeleteLocalRef(strencode);
	env->DeleteLocalRef(barr);
	
	return rtn;
}

string JniCommon::GetStringFromJava(JNIEnv *env, jstring str){
	if(str == 0 || env == NULL)
	return string(); // return a empty string

	int utf8Len = env->GetStringUTFLength(str);
	char strBuf[utf8Len + 1];
	env->GetStringUTFRegion(str, 0, env->GetStringLength(str), strBuf);
	strBuf[utf8Len] = '\0';

#if JNI_DEBUG_DATA_CONVERT
	LOGI("JniCommon::GetStringFromJava, strLen = %d ", utf8Len);
	PrintfHex(strBuf, strlen(strBuf));
#endif

	return string(strBuf);
}

JniObject * JniCommon::GetCommonObject(eJniObjectType type){
    JniObject * pTempObject = NULL;
	if( jniObjects.find(type) != jniObjects.end()){
        pTempObject = jniObjects.at(type);
    }
    return pTempObject;
}

int JniCommon::GetGlobalObjByName(JNIEnv* env, jobject& obj, const char* szString)
{
    jclass cls = env->FindClass(szString);
    jmethodID conid = env->GetMethodID(cls, "<init>", "()V");
    jobject objtmp = env->NewObject(cls, conid);
    obj = env->NewGlobalRef(objtmp);
    env->DeleteLocalRef(objtmp);
    env->DeleteLocalRef(cls);

    return 0;
}

void JniCommon::CurrentThreadExit(){
    jvm->DetachCurrentThread();
}

int JniCommon::GetYMDByMJD(uint16 nMJD, int* pYear, int* pMonth, int* pDay)
{
    uint16 wMJDTime = nMJD;

    jint iStartYear = (int)((wMJDTime - 15078.2)/365.25);
    jint iStartMonth = (int)(wMJDTime - 14956.1 - (int)(iStartYear*365.25))/30.6001;
    jint iStartDay = wMJDTime - 14956 - (int)(iStartYear*365.25) - (int)(iStartMonth * 30.6001);
    int k = 0;
    if(iStartMonth == 14 || iStartMonth == 15)
    {
        k = 1;
    }
    iStartYear = iStartYear + k;
    iStartMonth = iStartMonth - 1 - k * 12;

    *pYear = iStartYear;
    *pMonth = iStartMonth;
    *pDay = iStartDay;

    return 0;
}

int JniCommon::GetHMSByBCD(uint32 nBCD, int* pHour, int* pMinute, int* pSecond)
{
    uint8* pStartData = (uint8*)&nBCD;

    jint iStartHour = PSI_CnvBcdToVal(pStartData[2]);
    jint iStartMinute = PSI_CnvBcdToVal(pStartData[1]);
    jint iStartSecond = PSI_CnvBcdToVal(pStartData[0]);

    *pHour = iStartHour;
    *pMinute = iStartMinute;
    *pSecond = iStartSecond;

    return 0;
}

JniObject::JniObject(){

}

JniObject::~JniObject(){

}

jclass JniObject::getJClass(JNIEnv* env){
	return env->GetObjectClass(gObject);
}

jobject JniObject::createNewObject(JNIEnv*, void*){
	LOGI("%s::createNewObject was called !!!!!!!!!!!!", objName().c_str());
	assert(false);
	return 0;
}

void JniObject::toCppObject(JNIEnv*, jobject , void*){
	LOGI("%s::toCppObject was called !!!!!!!!!!!!", objName().c_str());
	assert(false);
}

void JniObject::setCppDataToJava(JNIEnv*, jobject , void*){
	LOGI("%s::setCppDataToJava was called !!!!!!!!!!!!", objName().c_str());
	assert(false);
}

void JniObject::setJavaDataToCpp(JNIEnv*, jobject , void*){
	LOGI("%s::setJavaDataToCpp was called !!!!!!!!!!!!", objName().c_str());
	assert(false);
}

JniIntObject::JniIntObject()
	:value(NULL)
{

}

void JniIntObject::initObject(JNIEnv *env){
    jclass cls = env->FindClass("java/lang/Integer");
    init = env->GetMethodID(cls, "<init>", "(I)V");
    jobject objtmp = env->NewObject(cls, init);
    gObject = env->NewGlobalRef(objtmp);
    value = env->GetFieldID(cls, "value", "I");
    env->DeleteLocalRef(objtmp);
    env->DeleteLocalRef(cls);
}

JniIntObject::~JniIntObject(){

}

jobject JniIntObject::createNewObject(JNIEnv * env, void * pData){
    LOGI("Create JniIntObject");
    jclass cls = env->GetObjectClass(gObject);
    jobject objout = env->NewObject(cls, init, *(int*)pData);
    env->DeleteLocalRef(cls);
    return objout;
}

void JniIntObject::setCppDataToJava(JNIEnv *env, jobject javaObj, void *cppObj){
	env->SetIntField(javaObj, value, *(int*)cppObj);
}

JniCharObject::JniCharObject(){

}

JniCharObject::~JniCharObject(){

}

void JniCharObject::initObject(JNIEnv *env){
	jclass cls = env->FindClass("java/lang/Character");
	init = env->GetMethodID(cls, "<init>", "(C)V");
	jobject objtmp = env->NewObject(cls, init);
	gObject = env->NewGlobalRef(objtmp);
	value = env->GetFieldID(cls, "value", "C");
	env->DeleteLocalRef(objtmp);
	env->DeleteLocalRef(cls);
}

jobject JniCharObject::createNewObject(JNIEnv *env, void *pData){
	LOGI("Create JniCharObject");
	if(pData == NULL)
		return 0;
	jclass cls = env->GetObjectClass(gObject);
	jobject objout = env->NewObject(cls, init, static_cast<uint16>((int)pData));
	env->DeleteLocalRef(cls);
	return objout;
}

void JniCharObject::setCppDataToJava(JNIEnv *env, jobject javaObj, void *cppObj){
	env->SetCharField(javaObj, value, static_cast<uint16>((int)cppObj));
}

JniTransponder::JniTransponder()
{

}

JniTransponder::~JniTransponder(){

}

void JniTransponder::initObject(JNIEnv *env){
    JniCommon::GetGlobalObjByName(env, gObject,"cn/vc/dvb/service/jni/struct/Transponder");
    jclass cls = env->GetObjectClass(gObject);

    frequency = env->GetFieldID(cls, "Frequency", "I");
    modulation = env->GetFieldID(cls, "Modulation", "I");
    symbolRate = env->GetFieldID(cls, "SymbolRate", "I");
    nPATVersion = env->GetFieldID(cls, "nPATVersion", "I");
    nSDTVersion = env->GetFieldID(cls, "nSDTVersion", "I");
    nCATVersion = env->GetFieldID(cls, "nCATVersion", "I");
    nNITVersion = env->GetFieldID(cls, "nNITVersion", "I");

    init = env->GetMethodID(cls, "<init>", "()V");
    env->DeleteLocalRef(cls);
}

jobject JniTransponder::createNewObject(JNIEnv * env, void *data){
    if(data == NULL)
        return 0;
    LOGI("Create JniTransponder\n");
    jclass cls = env->GetObjectClass(gObject);
    stTransponder* transponder = (stTransponder*)data;
    jobject objout = env->NewObject(cls, init);

    env->SetIntField(objout, this->frequency, transponder->Frequency);
    env->SetIntField(objout, this->modulation, transponder->Modulation);
    env->SetIntField(objout, this->symbolRate, transponder->SymbolRate);
    env->SetIntField(objout, this->nPATVersion, transponder->nPATVersion);
    env->SetIntField(objout, this->nSDTVersion, transponder->nSDTVersion);
    env->SetIntField(objout, this->nCATVersion, transponder->nCATVersion);
    env->SetIntField(objout, this->nNITVersion, transponder->nNITVersion);

    env->DeleteLocalRef(cls);
    return objout;
}

void JniTransponder::toCppObject(JNIEnv *env, jobject javaobj, void *outCppObj){
	if(outCppObj == NULL)
		return;

	if (javaobj == NULL){
		LOGW("JniTransponder::toCppObject javaobj is null");
		return;
	}
	stTransponder* outTransponder = (stTransponder*)outCppObj;

	outTransponder->Frequency   = env->GetIntField(javaobj, frequency);
	outTransponder->Modulation  = (eModulation)env->GetIntField(javaobj, modulation);
	outTransponder->SymbolRate  = env->GetIntField(javaobj, symbolRate);
	outTransponder->nPATVersion = env->GetIntField(javaobj, nPATVersion);
	outTransponder->nSDTVersion = env->GetIntField(javaobj, nSDTVersion);
	outTransponder->nCATVersion = env->GetIntField(javaobj, nCATVersion);
	outTransponder->nNITVersion = env->GetIntField(javaobj, nNITVersion);
}


JniTunerLockParam::JniTunerLockParam()
{

}

JniTunerLockParam::~JniTunerLockParam()
{

}

void JniTunerLockParam::initObject(JNIEnv *env)
{
	JniCommon::GetGlobalObjByName(env, gObject,"cn/vc/dvb/service/jni/struct/stTunerLockParam");
	jclass cls = env->GetObjectClass(gObject);

	tpParam = env->GetFieldID(cls, "tpParam", "Lcn/vc/dvb/service/jni/struct/Transponder;");
	bUseLnb = env->GetFieldID(cls, "bUseLnb", "Z");
	lnbParam = env->GetFieldID(cls, "lnbParam", "Lcn/vc/dvb/service/jni/struct/stLNB;");

	init = env->GetMethodID(cls, "<init>", "()V");
	env->DeleteLocalRef(cls);
}

jobject JniTunerLockParam::createNewObject(JNIEnv * env, void *data)
{
	if(data == NULL)
		return 0;
	LOGI("Create JniTunerLockParam\n");
	
	jclass cls = env->GetObjectClass(gObject);
	IEpgAbstract::IEpgSearchListEntry* transponderList = ( IEpgAbstract::IEpgSearchListEntry*)data;
	jobject objout = env->NewObject(cls, init);

	JniObject* pTransponder= JniCommon::GetCommonObject(JniCommon::JniObjectType_Transponder);
	jobject objTransponder = pTransponder->createNewObject(env, &transponderList->tpParam);
	env->SetObjectField(objout, tpParam, objTransponder);
	env->DeleteLocalRef(objTransponder);

	env->SetBooleanField(objout, bUseLnb, transponderList->bUseLnb);

	JniObject* pLNB= JniCommon::GetCommonObject(JniCommon::JniObjectType_stLNB);
	jobject objLnbParam = pLNB->createNewObject(env, &transponderList->lnbParam);
	env->SetObjectField(objout, lnbParam, objLnbParam);
	env->DeleteLocalRef(objLnbParam);
	
	env->DeleteLocalRef(cls);
	return objout;
}

void JniTunerLockParam::toCppObject(JNIEnv *env, jobject javaobj, void *outCppObj)
{
	if(outCppObj == NULL)
		return;

	if (javaobj == NULL){
		LOGW("JniTunerLockParam::toCppObject javaobj is null");
		return;
	}
	
	IEpgAbstract::IEpgSearchListEntry* outTransponderList = (IEpgAbstract::IEpgSearchListEntry*)outCppObj;


	jobject objTransponder = env->GetObjectField(javaobj, tpParam);
	JniObject* pTransponder  = JniCommon::GetCommonObject(JniCommon::JniObjectType_Transponder);
	pTransponder->toCppObject(env, objTransponder, &outTransponderList->tpParam);
	env->DeleteLocalRef(objTransponder);

	outTransponderList->bUseLnb = env->GetBooleanField(javaobj, bUseLnb);

	jobject objLNB = env->GetObjectField(javaobj, lnbParam);
	JniObject* pLNB  = JniCommon::GetCommonObject(JniCommon::JniObjectType_stLNB);
	pLNB->toCppObject(env, objLNB, &outTransponderList->lnbParam);
	env->DeleteLocalRef(objLNB);

	return ;
}


JniChannel::JniChannel(){

}

JniChannel::~JniChannel(){

}

void JniChannel::initObject(JNIEnv *env){
	JniCommon::GetGlobalObjByName(env, gObject,"novel/supertv/dvb/jni/struct/stChannel");
    jclass cls = env->GetObjectClass(gObject);
	init = env->GetMethodID(cls, "<init>", "()V");

	ServiceIdent	= env->GetFieldID(cls, "ServiceIdent", "Lnovel/supertv/dvb/jni/struct/stServiceIdent;");
	ServiceName		= env->GetFieldID(cls, "ServiceName", "Ljava/lang/String;");
	ServiceType		= env->GetFieldID(cls, "ServiceType", "B");
	ServiceOrgType	= env->GetFieldID(cls, "ServiceOrgType", "B");
	ProviderName	= env->GetFieldID(cls, "ProviderName", "Ljava/lang/String;");

	VideoTrack		= env->GetFieldID(cls, "VideoTrack", "Lnovel/supertv/dvb/jni/struct/stVideoTrack;");
	AudioTrack		= env->GetFieldID(cls, "AudioTrack", "[Lnovel/supertv/dvb/jni/struct/stAudioTrack;");
	PcrPid			= env->GetFieldID(cls, "PcrPid", "C");

	Teletext		= env->GetFieldID(cls, "Teletext", "[Lnovel/supertv/dvb/jni/struct/stPidStream;");
	Subtitle		= env->GetFieldID(cls, "Subtitle", "[Lnovel/supertv/dvb/jni/struct/stPidStream;");

	CaSystemId		= env->GetFieldID(cls, "CaSystemId", "[C");
	CaEcmPid		= env->GetFieldID(cls, "CaEcmPid", "[C");

	LogicChNumber	= env->GetFieldID(cls, "LogicChNumber", "C");
	NvodTimeshiftServices = env->GetFieldID(cls, "NvodTimeshiftServices", "[Lnovel/supertv/dvb/jni/struct/stServiceIdent;");
	PmtPid			= env->GetFieldID(cls, "PmtPid", "C");
	PmtVersion		= env->GetFieldID(cls, "PmtVersion", "I");
	VolBalance		= env->GetFieldID(cls, "VolBalance", "B");
	VolCompensation = env->GetFieldID(cls, "VolCompensation", "B");

    env->DeleteLocalRef(cls);
}

jobject JniChannel::createNewObject(JNIEnv * env, void * pData){
    if(pData == NULL)
        return 0;
    LOGI("Create JniChannel");
	stChannel* pChannel = (stChannel*)pData;

	jclass cls = env->GetObjectClass(gObject);

	jobject objout = env->NewObject(cls, init);

	JniObject* pServiceIdent = JniCommon::GetCommonObject(JniCommon::JniObjectType_stServiceIdent);

	jobject objServiceIdent = pServiceIdent->createNewObject(env, &pChannel->ServiceIdent);
	env->SetObjectField(objout, ServiceIdent, objServiceIdent);
	env->DeleteLocalRef(objServiceIdent);

	jstring objServiceName = JniCommon::CharTojstring(env, pChannel->ServiceName);
	env->SetObjectField(objout, ServiceName, objServiceName);
	env->DeleteLocalRef(objServiceName);

	env->SetByteField(objout, ServiceType, pChannel->ServiceType);
	env->SetByteField(objout, ServiceOrgType, pChannel->ServiceOrgType);

	jstring objProviderName = JniCommon::CharTojstring(env, pChannel->ProviderName);
	env->SetObjectField(objout, ProviderName, objProviderName);
	env->DeleteLocalRef(objProviderName);

	jobject objVideoTrack = JniCommon::GetCommonObject(JniCommon::JniObjectType_stVideoTrack)
			->createNewObject(env, &pChannel->VideoTrack);
	env->SetObjectField(objout, VideoTrack, objVideoTrack);
	env->DeleteLocalRef(objVideoTrack);

	JniObject* pAudioTrack = JniCommon::GetCommonObject(JniCommon::JniObjectType_stAudioTrack);
	jclass clsAudioTrack = pAudioTrack->getJClass(env);
	jobjectArray objArrayAudioTrack = env->NewObjectArray(pChannel->AudioTrack.size(), clsAudioTrack, 0);
	for(vector<stAudioTrack>::iterator i = pChannel->AudioTrack.begin();
		i != pChannel->AudioTrack.end() ; i++){
		jobject objAudioTrack = pAudioTrack->createNewObject(env, &(*i));
		env->SetObjectArrayElement(objArrayAudioTrack, i - pChannel->AudioTrack.begin(), objAudioTrack);
		env->DeleteLocalRef(objAudioTrack);
	}
	env->DeleteLocalRef(clsAudioTrack);
	env->SetObjectField(objout, AudioTrack, objArrayAudioTrack);
	env->DeleteLocalRef(objArrayAudioTrack);

	env->SetCharField(objout, PcrPid, pChannel->PcrPid);

	JniObject* pPidStream = JniCommon::GetCommonObject(JniCommon::JniObjectType_stPidStream);
	jclass clsPidStream = pPidStream->getJClass(env);
	jobjectArray objArrayTeletext = env->NewObjectArray(pChannel->Teletext.size(), clsPidStream, 0);
	for(vector<stPidStream>::iterator i = pChannel->Teletext.begin();
		i != pChannel->Teletext.end(); i++){
		jobject objTeletext = pPidStream->createNewObject(env, &(*i));
		env->SetObjectArrayElement(objArrayTeletext, i - pChannel->Teletext.begin(), objTeletext);
		env->DeleteLocalRef(objTeletext);
	}
	env->SetObjectField(objout, Teletext, objArrayTeletext);
	env->DeleteLocalRef(objArrayTeletext);

	jobjectArray objArraySubtitle = env->NewObjectArray(pChannel->Subtitle.size(), clsPidStream, 0);
	for(vector<stPidStream>::iterator i = pChannel->Subtitle.begin();
		i != pChannel->Subtitle.end(); i++){
		jobject objSubtitle = pPidStream->createNewObject(env, &(*i));
		env->SetObjectArrayElement(objArraySubtitle, i - pChannel->Subtitle.begin(), objSubtitle);
		env->DeleteLocalRef(objSubtitle);
	}
	env->DeleteLocalRef(clsPidStream);
	env->SetObjectField(objout, Subtitle, objArraySubtitle);
	env->DeleteLocalRef(objArraySubtitle);

	jcharArray charArrayCaSystemId = env->NewCharArray(pChannel->CaSystemId.size());
	env->SetCharArrayRegion(charArrayCaSystemId, 0, pChannel->CaSystemId.size(),
							pChannel->CaSystemId.data());
	env->SetObjectField(objout, CaSystemId, charArrayCaSystemId);
	env->DeleteLocalRef(charArrayCaSystemId);

	jcharArray charArrayCaEcmPid = env->NewCharArray(pChannel->CaEcmPid.size());
	env->SetCharArrayRegion(charArrayCaEcmPid, 0, pChannel->CaEcmPid.size(), pChannel->CaEcmPid.data());
	env->SetObjectField(objout, CaEcmPid, charArrayCaEcmPid);
	env->DeleteLocalRef(charArrayCaEcmPid);

	jclass clsServiceIdent = pServiceIdent->getJClass(env);
	jobjectArray objArrayNvodTimeshiftServices =
			env->NewObjectArray(pChannel->NvodTimeshiftServices.size(), clsServiceIdent, 0);
	for(vector<stServiceIdent>::iterator i = pChannel->NvodTimeshiftServices.begin();
		i != pChannel->NvodTimeshiftServices.end(); i++){
		jobject objNvodTimeshiftService = pServiceIdent->createNewObject(env, &(*i));
		env->SetObjectArrayElement(objArrayNvodTimeshiftServices,
								   i - pChannel->NvodTimeshiftServices.begin(), objNvodTimeshiftService);
		env->DeleteLocalRef(objNvodTimeshiftService);
	}
	env->DeleteLocalRef(clsServiceIdent);
	env->SetObjectField(objout, NvodTimeshiftServices, objArrayNvodTimeshiftServices);
	env->DeleteLocalRef(objArrayNvodTimeshiftServices);

	env->SetCharField(objout, LogicChNumber, pChannel->LogicChNumber);

	env->SetCharField(objout, PmtPid, pChannel->PmtPid);
	env->SetIntField(objout, PmtVersion, pChannel->PmtVersion);
	env->SetByteField(objout, VolBalance, pChannel->VolBalance);
	env->SetByteField(objout, VolCompensation, pChannel->VolCompensation);

    env->DeleteLocalRef(cls);
    return objout;
}

void JniChannel::toCppObject(JNIEnv * env, jobject inJavaObj, void* outCppObj){
	if (outCppObj == NULL || env == NULL || inJavaObj == NULL){
		LOGE("JniChannel::toCppObject(), invalid param! outCppObj=0x%p, env=0x%p, inJavaObj=0x%p ", 
			outCppObj, env, inJavaObj);
		return;
	}

	stChannel* outChannel = (stChannel*)outCppObj;

	jobject objServiceIdent = env->GetObjectField(inJavaObj, ServiceIdent);
	JniObject* pServiceIdent = JniCommon::GetCommonObject(JniCommon::JniObjectType_stServiceIdent);
	pServiceIdent->toCppObject(env, objServiceIdent, &outChannel->ServiceIdent);
	env->DeleteLocalRef(objServiceIdent);

	jstring strServiceName = (jstring)env->GetObjectField(inJavaObj, ServiceName);
	if(strServiceName != 0){
		outChannel->ServiceName = JniCommon::GetStringFromJava(env, strServiceName);
		env->DeleteLocalRef(strServiceName);
	}

	outChannel->ServiceType = env->GetByteField(inJavaObj, ServiceType);
	outChannel->ServiceOrgType = env->GetByteField(inJavaObj, ServiceOrgType);

	jstring strProviderName = (jstring)env->GetObjectField(inJavaObj, ProviderName);
	if(strProviderName != 0){
		outChannel->ProviderName = JniCommon::GetStringFromJava(env, strProviderName);
		env->DeleteLocalRef(strProviderName);
	}

	jobject objstVideoTrack = env->GetObjectField(inJavaObj, VideoTrack);
	JniCommon::GetCommonObject(JniCommon::JniObjectType_stVideoTrack)
		->toCppObject(env, objstVideoTrack, &outChannel->VideoTrack);
	env->DeleteLocalRef(objstVideoTrack);

	JniObject* pAudioTrack = JniCommon::GetCommonObject(JniCommon::JniObjectType_stAudioTrack);
	jobjectArray objArrayAudioTrack = (jobjectArray)env->GetObjectField(inJavaObj, AudioTrack);
	if(objArrayAudioTrack != 0){
		jsize arrayLength = env->GetArrayLength(objArrayAudioTrack);
		for(int i = 0; i < arrayLength; i ++){
			jobject objAudioTrack = env->GetObjectArrayElement(objArrayAudioTrack, i);
			stAudioTrack tempAudioTrack;
			pAudioTrack->toCppObject(env, objAudioTrack, &tempAudioTrack);
			outChannel->AudioTrack.push_back(tempAudioTrack);
			env->DeleteLocalRef(objAudioTrack);
		}
		env->DeleteLocalRef(objArrayAudioTrack);
	}

	outChannel->PcrPid = env->GetCharField(inJavaObj, PcrPid);

	JniObject* pPidStream = JniCommon::GetCommonObject(JniCommon::JniObjectType_stPidStream);
	jobjectArray objArrayTeletext = (jobjectArray)env->GetObjectField(inJavaObj, Teletext);
	if(objArrayTeletext != 0){
		jsize arrayLength = env->GetArrayLength(objArrayTeletext);
		for(int i = 0; i < arrayLength; i ++){
			jobject objTeletext = env->GetObjectArrayElement(objArrayTeletext, i);
			stPidStream tempTeletext;
			pPidStream->toCppObject(env, objTeletext, &tempTeletext);
			outChannel->Teletext.push_back(tempTeletext);
			env->DeleteLocalRef(objTeletext);
		}
		env->DeleteLocalRef(objArrayTeletext);
	}

	jobjectArray objArraySubtitle = (jobjectArray)env->GetObjectField(inJavaObj, Subtitle);
	if(objArraySubtitle != 0){
		jsize arrayLength = env->GetArrayLength(objArraySubtitle);
		for(int i = 0; i < arrayLength; i ++){
			jobject objSubtitle = env->GetObjectArrayElement(objArraySubtitle, i);
			stPidStream tempSubtitle;
			pPidStream->toCppObject(env, objSubtitle, &tempSubtitle);
			outChannel->Subtitle.push_back(tempSubtitle);
			env->DeleteLocalRef(objSubtitle);
		}
		env->DeleteLocalRef(objArraySubtitle);
	}

	jcharArray charArrayCaSystemId = (jcharArray)env->GetObjectField(inJavaObj, CaSystemId);
	if(charArrayCaSystemId != 0){
		jsize arrayLength = env->GetArrayLength(charArrayCaSystemId);
		jchar* pCharCaSystemId = env->GetCharArrayElements(charArrayCaSystemId, NULL);
		for(int i = 0; i < arrayLength; i ++){
			outChannel->CaSystemId.push_back(pCharCaSystemId[i]);
		}
		env->ReleaseCharArrayElements(charArrayCaSystemId, pCharCaSystemId, JNI_ABORT);
		env->DeleteLocalRef(charArrayCaSystemId);
	}

	jcharArray charArrayCaEcmPid = (jcharArray)env->GetObjectField(inJavaObj, CaEcmPid);
	if(charArrayCaEcmPid != 0){
		jsize arrayLength = env->GetArrayLength(charArrayCaEcmPid);
		jchar* pCharCaEcmPid = env->GetCharArrayElements(charArrayCaEcmPid, NULL);
		for(int i = 0; i < arrayLength; i ++){
			outChannel->CaSystemId.push_back(pCharCaEcmPid[i]);
		}
		env->ReleaseCharArrayElements(charArrayCaEcmPid, pCharCaEcmPid, JNI_ABORT);
		env->DeleteLocalRef(charArrayCaEcmPid);
	}

	outChannel->LogicChNumber = env->GetCharField(inJavaObj, LogicChNumber);

	jobjectArray objArrayNvodTimeshiftServices =
		(jobjectArray)env->GetObjectField(inJavaObj, NvodTimeshiftServices);
	if(objArrayNvodTimeshiftServices != 0){
		jsize arrayLength = env->GetArrayLength(objArrayNvodTimeshiftServices);
		for(int i = 0; i < arrayLength; i ++){
			jobject objNvodTimeshiftService = env->GetObjectArrayElement(objArrayNvodTimeshiftServices, i);
			stServiceIdent tempNvodTimeshiftService;
			pServiceIdent->toCppObject(env, objNvodTimeshiftService, &tempNvodTimeshiftService);
			outChannel->NvodTimeshiftServices.push_back(tempNvodTimeshiftService);
			env->DeleteLocalRef(objNvodTimeshiftService);
		}
		env->DeleteLocalRef(objArrayNvodTimeshiftServices);
	}

	outChannel->PmtPid = env->GetCharField(inJavaObj, PmtPid);
	outChannel->PmtVersion = env->GetIntField(inJavaObj, PmtVersion);
	outChannel->VolBalance = env->GetByteField(inJavaObj, VolBalance);
	outChannel->VolCompensation = env->GetByteField(inJavaObj, VolCompensation);
}

JniServiceIdent::JniServiceIdent(){

}

JniServiceIdent::~JniServiceIdent(){

}

void JniServiceIdent::initObject(JNIEnv *env){
	JniCommon::GetGlobalObjByName(env, gObject,"novel/supertv/dvb/jni/struct/stServiceIdent");
    jclass cls = env->GetObjectClass(gObject);
	ServiceId = env->GetFieldID(cls, "ServiceId", "I");
	TsId = env->GetFieldID(cls, "TsId", "I");
	OrgNetId = env->GetFieldID(cls, "OrgNetId", "I");
    init = env->GetMethodID(cls, "<init>", "()V");
    env->DeleteLocalRef(cls);
}

jobject JniServiceIdent::createNewObject(JNIEnv *env, void *pData){
    if(pData == NULL)
        return 0;
//    LOGI("Create JniServiceIdent");
    stServiceIdent* pServiceIdent = (stServiceIdent*)pData;

    jclass cls = env->GetObjectClass(gObject);

    jobject objout = env->NewObject(cls, init);

	env->SetIntField(objout, ServiceId, pServiceIdent->service_id);
	env->SetIntField(objout, TsId, pServiceIdent->transport_stream_id);
	env->SetIntField(objout, OrgNetId, pServiceIdent->original_network_id);

    env->DeleteLocalRef(cls);

    return objout;
}

void JniServiceIdent::toCppObject(JNIEnv *env, jobject javaobj, void *outCppObj)
{
	if(javaobj == NULL)
        return;

	if (outCppObj == NULL){
        LOGW("toCPPTransponder serviceIdentObj is null");
        return;
    }

	stServiceIdent* outServiceIdent = (stServiceIdent*)outCppObj;

	outServiceIdent->service_id = env->GetIntField(javaobj, ServiceId);
    outServiceIdent->transport_stream_id =
			env->GetIntField(javaobj, TsId);
	outServiceIdent->original_network_id = env->GetIntField(javaobj, OrgNetId);
}

JniDate::JniDate(){

}

JniDate::~JniDate(){

}

void JniDate::initObject(JNIEnv *env){
    jclass cls = env->FindClass("java/util/Date");
    init = env->GetMethodID(cls, "<init>", "(J)V");//long milliseconds
    UTC = env->GetStaticMethodID(cls, "UTC", "(IIIIII)J");
    jobject objtmp = env->NewObject(cls, init, 0);
    gObject = env->NewGlobalRef(objtmp);

    env->DeleteLocalRef(objtmp);
    env->DeleteLocalRef(cls);
}

/**  * 该函数输入时间为UTC时间，转换后为GMT时间
     *
     * @param year
     *            the year, 0 is 1900.
     * @param month
     *            the month, 0 - 11.
     * @param day
     *            the day of the month, 1 - 31.
     * @param hour
     *            the hour of day, 0 - 23.
     * @param minute
     *            the minute of the hour, 0 - 59.
     * @param second
     *            the second of the minute, 0 - 59.
     */

jobject JniDate::createNewObject(JNIEnv *env, int year, int month, int day,
                                 int hour, int minute, int second)
{
    LOGI("Create JniDate");
    jclass cls = env->GetObjectClass(gObject);

    jlong milliseconds = env->CallStaticLongMethod(cls, UTC, year, month, day, hour, minute, second);
    jobject objout = env->NewObject(cls, init, milliseconds);

    env->DeleteLocalRef(cls);
    return objout;
}
JniLoaderState::JniLoaderState(){

}

JniLoaderState::~JniLoaderState(){

}

void JniLoaderState::initObject(JNIEnv* env){
    JniCommon::GetGlobalObjByName(env, gObject,"novel/supertv/dvb/jni/JniCommonDefination$stLoaderArg");
    jclass cls = env->GetObjectClass(gObject);
    init = env->GetMethodID(cls, "<init>", "()V");

    Serviceid=env->GetFieldID(cls, "Serviceid", "I");;
    stream_id=env->GetFieldID(cls, "stream_id", "I");;
    network_id=env->GetFieldID(cls, "network_id", "I");;
    linkage_type=env->GetFieldID(cls, "linkage_type", "I");;
    manufacture_code=env->GetFieldID(cls, "manufacture_code", "I");;
    hardware_version=env->GetFieldID(cls, "hardware_version", "I");;
    software_version=env->GetFieldID(cls, "software_version", "I");;
    serial_number_start=env->GetFieldID(cls, "serial_number_start", "I");;
    serial_number_end=env->GetFieldID(cls, "serial_number_end", "I");;
    Type=env->GetFieldID(cls, "Type", "I");

    env->DeleteLocalRef(cls);
}

jobject JniLoaderState::createNewObject(JNIEnv * env, void * data){
    if(data == NULL)
        return 0;
    LOGI("Create JniLoaderState");
    LoaderArgument * loaderarg = (LoaderArgument *)data;

    jclass cls = env->GetObjectClass(gObject);

    jobject objout = env->NewObject(cls, init);

    env->SetIntField(objout, Serviceid, loaderarg->Serviceid);
    env->SetIntField(objout, software_version, loaderarg->software_version);
    env->SetIntField(objout, network_id, loaderarg->network_id);
    env->SetIntField(objout, linkage_type, loaderarg->linkage_type);
    env->SetIntField(objout, manufacture_code, loaderarg->manufacture_code);
    env->SetIntField(objout, serial_number_start, loaderarg->serial_number_start);
    env->SetIntField(objout, serial_number_end, loaderarg->serial_number_end);
    env->SetIntField(objout, hardware_version, loaderarg->hardware_version);
    env->SetIntField(objout, software_version, loaderarg->software_version);
    env->SetIntField(objout, Type, loaderarg->Type);

    env->DeleteLocalRef(cls);

    return objout;
}

void JniLoaderState::toCPPLoaderInfo(JNIEnv * env,jobject loaobj, LoaderArgument * outLoaderArg){
    if(outLoaderArg == NULL)
        return;

    if (loaobj == NULL){
        return;
    }

    outLoaderArg->Serviceid = env->GetIntField(loaobj, Serviceid);
    outLoaderArg->software_version = env->GetIntField(loaobj, software_version);
    outLoaderArg->network_id = env->GetIntField(loaobj, network_id);
    outLoaderArg->linkage_type = env->GetIntField(loaobj, linkage_type);
    outLoaderArg->manufacture_code = env->GetIntField(loaobj, manufacture_code);
    outLoaderArg->serial_number_start = env->GetIntField(loaobj, serial_number_start);
    outLoaderArg->serial_number_end = env->GetIntField(loaobj, serial_number_end);
    outLoaderArg->hardware_version = env->GetIntField(loaobj, hardware_version);
    outLoaderArg->software_version = env->GetIntField(loaobj, software_version);
    outLoaderArg->Type = env->GetIntField(loaobj, Type);
}

JniDVBDateTime::JniDVBDateTime()
{

}

JniDVBDateTime::~JniDVBDateTime()
{

}

void JniDVBDateTime::initObject(JNIEnv* env)
{
    JniCommon::GetGlobalObjByName(env, gObject, "novel/supertv/dvb/jni/struct/DVBTime");
    jclass cls = env->GetObjectClass(gObject);
    YearFiledId = env->GetFieldID(cls, "iYear", "I");
    MonthFiledId = env->GetFieldID(cls, "iMonth", "I");
    DayFiledId = env->GetFieldID(cls, "iDay", "I");
    HourFiledId = env->GetFieldID(cls, "iHour", "I");
    MinuteFiledId = env->GetFieldID(cls, "iMinute", "I");
    SecondFiledId = env->GetFieldID(cls, "iSecond", "I");

    init  = env->GetMethodID(cls, "<init>", "()V");
    env->DeleteLocalRef(cls);
}

jobject JniDVBDateTime::createNewObject(JNIEnv* env, uint16 nMJD, uint32 nBCD)
{
    jclass cls = env->GetObjectClass(gObject);

    jobject objout = env->NewObject(cls, init);

    int nYear = 0, nMonth = 0, nDay = 0;
    int nHour = 0, nMinute = 0, nSecond = 0;
    JniCommon::GetYMDByMJD(nMJD, &nYear, &nMonth, &nDay);
    JniCommon::GetHMSByBCD(nBCD, &nHour, &nMinute, &nSecond);

    env->SetIntField(objout, YearFiledId, nYear);
    env->SetIntField(objout, MonthFiledId, nMonth);
    env->SetIntField(objout, DayFiledId, nDay);
    env->SetIntField(objout, HourFiledId, nHour);
    env->SetIntField(objout, MinuteFiledId, nMinute);
    env->SetIntField(objout, SecondFiledId, nSecond);
    env->DeleteLocalRef(cls);

    return objout;
}

jobject JniDVBDateTime::createNewObject(JNIEnv * env, void * pData)
{
    return NULL;
}

JnistVideoTrack::JnistVideoTrack(){

}

JnistVideoTrack::~JnistVideoTrack(){

}

void JnistVideoTrack::initObject(JNIEnv *env){
	JniCommon::GetGlobalObjByName(env, gObject, "novel/supertv/dvb/jni/struct/stVideoTrack");
	jclass cls = env->GetObjectClass(gObject);
	StreamPid = env->GetFieldID(cls, "StreamPid", "I");
	EcmPid = env->GetFieldID(cls, "EcmPid", "I");
	PesType = env->GetFieldID(cls, "PesType", "I");

	init  = env->GetMethodID(cls, "<init>", "()V");
	env->DeleteLocalRef(cls);
}

jobject JnistVideoTrack::createNewObject(JNIEnv *env, void *pData){
	jclass cls = env->GetObjectClass(gObject);

	jobject objout = env->NewObject(cls, init);

	stVideoTrack* pVideoTrack = (stVideoTrack*)pData;

	env->SetIntField(objout, StreamPid, pVideoTrack->StreamPid);
	env->SetIntField(objout, EcmPid, pVideoTrack->EcmPid);
	env->SetIntField(objout, PesType, pVideoTrack->PesType);

	env->DeleteLocalRef(cls);

	return objout;
}

void JnistVideoTrack::toCppObject(JNIEnv *env, jobject javaobj, void *outCppObj){
	if(outCppObj == NULL)
		return;

	if (javaobj == NULL){
		LOGW("JnistVideoTrack::toCppObject javaobj is null");
		return;
	}

	stVideoTrack* outPvrVideoTrack = (stVideoTrack*)outCppObj;

	outPvrVideoTrack->StreamPid = env->GetIntField(javaobj, StreamPid);
	outPvrVideoTrack->EcmPid = env->GetIntField(javaobj, EcmPid);
	outPvrVideoTrack->PesType = env->GetIntField(javaobj, PesType);
}

JnistAudioTrack::JnistAudioTrack(){

}

JnistAudioTrack::~JnistAudioTrack(){

}

void JnistAudioTrack::initObject(JNIEnv *env){
	JniCommon::GetGlobalObjByName(env, gObject, "novel/supertv/dvb/jni/struct/stAudioTrack");
	jclass cls = env->GetObjectClass(gObject);
	StreamPid = env->GetFieldID(cls, "StreamPid", "I");
	EcmPid = env->GetFieldID(cls, "EcmPid", "I");
	PesType = env->GetFieldID(cls, "PesType", "I");
	LangCode = env->GetFieldID(cls, "LangCode", "I");
	channelType = env->GetFieldID(cls, "channelType", "I");

	init  = env->GetMethodID(cls, "<init>", "()V");
	env->DeleteLocalRef(cls);
}

jobject JnistAudioTrack::createNewObject(JNIEnv *env, void *pData){
	jclass cls = env->GetObjectClass(gObject);

	jobject objout = env->NewObject(cls, init);

	stAudioTrack* pAudioTrack = (stAudioTrack*)pData;

	env->SetIntField(objout, StreamPid, pAudioTrack->StreamPid);
	env->SetIntField(objout, EcmPid, pAudioTrack->EcmPid);
	env->SetIntField(objout, PesType, pAudioTrack->PesType);
	env->SetIntField(objout, LangCode, pAudioTrack->LangCode);
	env->SetIntField(objout, channelType, pAudioTrack->channelType);

	env->DeleteLocalRef(cls);

	return objout;
}

void JnistAudioTrack::toCppObject(JNIEnv *env, jobject javaobj, void *outCppObj){
	if(outCppObj == NULL)
		return;

	if (javaobj == NULL){
		LOGW("JniIPvrAudioTrack::toCppObject javaobj is null");
		return;
	}

	stAudioTrack* outPvrAudioTrack = (stAudioTrack*)outCppObj;

	outPvrAudioTrack->StreamPid = env->GetIntField(javaobj, StreamPid);
	outPvrAudioTrack->EcmPid = env->GetIntField(javaobj, EcmPid);
	outPvrAudioTrack->PesType = env->GetIntField(javaobj, PesType);
	outPvrAudioTrack->LangCode = env->GetIntField(javaobj, LangCode);
	outPvrAudioTrack->channelType = (eAudioChannelMode)env->GetIntField(javaobj,channelType);
}

JnistPidStream::JnistPidStream(){

}

JnistPidStream::~JnistPidStream(){

}

void JnistPidStream::initObject(JNIEnv *env){
	JniCommon::GetGlobalObjByName(env, gObject, "novel/supertv/dvb/jni/struct/stPidStream");
	jclass cls = env->GetObjectClass(gObject);
	StreamPid = env->GetFieldID(cls, "StreamPid", "I");
	EcmPid = env->GetFieldID(cls, "EcmPid", "I");
	PesType = env->GetFieldID(cls, "PesType", "I");
	StreamDesc = env->GetFieldID(cls, "StreamDesc", "I");

	init  = env->GetMethodID(cls, "<init>", "()V");
	env->DeleteLocalRef(cls);
}

jobject JnistPidStream::createNewObject(JNIEnv *env, void *pData){
	jclass cls = env->GetObjectClass(gObject);

	jobject objout = env->NewObject(cls, init);

	stPidStream* pPidStream = (stPidStream*)pData;

	env->SetIntField(objout, StreamPid, pPidStream->StreamPid);
	env->SetIntField(objout, EcmPid, pPidStream->EcmPid);
	env->SetIntField(objout, PesType, pPidStream->PesType);
	env->SetIntField(objout, StreamDesc, pPidStream->StreamDesc);

	env->DeleteLocalRef(cls);

	return objout;
}

void JnistPidStream::toCppObject(JNIEnv *env, jobject javaobj, void *outCppObj){
	if(outCppObj == NULL)
		return;

	if (javaobj == NULL){
		LOGW("JniIPvrPidStream::toCppObject javaobj is null");
		return;
	}

	stPidStream* outPidStream = (stPidStream*)outCppObj;

	outPidStream->StreamPid = env->GetIntField(javaobj, StreamPid);
	outPidStream->EcmPid = env->GetIntField(javaobj, EcmPid);
	outPidStream->PesType = env->GetIntField(javaobj, PesType);
	outPidStream->StreamDesc = env->GetIntField(javaobj, StreamDesc);
}

JniIPvrStreamInfo::JniIPvrStreamInfo(){

}

JniIPvrStreamInfo::~JniIPvrStreamInfo(){

}

void JniIPvrStreamInfo::initObject(JNIEnv *env){
	JniCommon::GetGlobalObjByName(env, gObject, "novel/supertv/dvb/jni/struct/IPvrStreamInfo");
	jclass cls = env->GetObjectClass(gObject);
	bDecrypt = env->GetFieldID(cls, "bDecrypt", "Z");
	VideoTrack = env->GetFieldID(cls, "VideoTrack", "Lnovel/supertv/dvb/jni/struct/stVideoTrack;");
	AudioTrack = env->GetFieldID(cls, "AudioTrack", "[Lnovel/supertv/dvb/jni/struct/stAudioTrack;");
	AudioTrackNum = env->GetFieldID(cls, "AudioTrackNum", "I");
	AudioPlayIndex = env->GetFieldID(cls, "AudioPlayIndex", "I");
	PcrPid = env->GetFieldID(cls, "PcrPid", "I");
	SubtitlePid = env->GetFieldID(cls, "SubtitlePid", "I");
	TeletextPid = env->GetFieldID(cls, "TeletextPid", "I");
	OtherPid = env->GetFieldID(cls, "OtherPid", "[Lnovel/supertv/dvb/jni/struct/stPidStream;");
	OtherPidNum = env->GetFieldID(cls, "OtherPidNum", "I");
	init  = env->GetMethodID(cls, "<init>", "()V");
	env->DeleteLocalRef(cls);
}

void JniIPvrStreamInfo::toCppObject(JNIEnv *env, jobject javaobj, void *outCppObj){
	if(outCppObj == NULL)
		return;

	if (javaobj == NULL){
		LOGW("JniIPvrStreamInfo::toCppObject javaobj is null");
		return;
	}

	IPvrAbstract::IPvrStreamInfo* pStreamInfo = (IPvrAbstract::IPvrStreamInfo*)outCppObj;

	pStreamInfo->bDecrypt = env->GetBooleanField(javaobj, bDecrypt);
	jobject objVideoTrack = env->GetObjectField(javaobj, VideoTrack);
	jobjectArray objAudioTrackArray = (jobjectArray)env->GetObjectField(javaobj, AudioTrack);
	pStreamInfo->AudioTrackNum = env->GetIntField(javaobj, AudioTrackNum);
	pStreamInfo->AudioPlayIndex = env->GetIntField(javaobj, AudioPlayIndex);
	pStreamInfo->PcrPid = env->GetIntField(javaobj, PcrPid);
	pStreamInfo->SubtitlePid = env->GetIntField(javaobj, SubtitlePid);
	pStreamInfo->TeletextPid = env->GetIntField(javaobj, TeletextPid);
	jobjectArray objOtherPidArray = (jobjectArray)env->GetObjectField(javaobj, OtherPid);
	pStreamInfo->OtherPidNum = env->GetIntField(javaobj, OtherPidNum);

	JniCommon::GetCommonObject(JniCommon::JniObjectType_stVideoTrack)
			->toCppObject(env, objVideoTrack, &pStreamInfo->VideoTrack);
	env->DeleteLocalRef(objVideoTrack);

	jsize objAudioTrackArraySize = env->GetArrayLength(objAudioTrackArray);
	for(jint i = 0; i < objAudioTrackArraySize && i < 4; i ++)
	{
		jobject objAudioTrack = env->GetObjectArrayElement(objAudioTrackArray, i);
		JniCommon::GetCommonObject(JniCommon::JniObjectType_stAudioTrack)
				->toCppObject(env, objAudioTrack, &pStreamInfo->AudioTrack[i]);
		env->DeleteLocalRef(objAudioTrack);
	}
	env->DeleteLocalRef(objAudioTrackArray);

	jsize objOtherPidArraySize = env->GetArrayLength(objOtherPidArray);
	for(jint i = 0; i < objOtherPidArraySize && i < 4; i ++)
	{
		jobject objOtherPid = env->GetObjectArrayElement(objOtherPidArray, i);
		JniCommon::GetCommonObject(JniCommon::JniObjectType_stAudioTrack)
				->toCppObject(env, objOtherPid, &pStreamInfo->OtherPid[i]);
		env->DeleteLocalRef(objOtherPid);
	}
	env->DeleteLocalRef(objOtherPidArray);
}


JniIPvrRecOpenParam::JniIPvrRecOpenParam()
{

}

JniIPvrRecOpenParam::~JniIPvrRecOpenParam()
{

}

void JniIPvrRecOpenParam::initObject(JNIEnv *env)
{
	JniCommon::GetGlobalObjByName(env, gObject, "novel/supertv/dvb/jni/struct/IPvrRecOpenParam");
	jclass cls = env->GetObjectClass(gObject);
	m_pPvrDirPath = env->GetFieldID(cls, "m_pPvrDirPath", "Ljava/lang/String;");
	m_Transponder = env->GetFieldID(cls, "m_Transponder", "Lnovel/supertv/dvb/jni/struct/Transponder;");
	m_streamInfo = env->GetFieldID(cls, "m_streamInfo", "Lnovel/supertv/dvb/jni/struct/IPvrStreamInfo;");
	m_bTimeshift = env->GetFieldID(cls, "m_bTimeshift", "Z");
	m_recSizeLimitMB = env->GetFieldID(cls, "m_recSizeLimitMB", "I");

	init  = env->GetMethodID(cls, "<init>", "()V");
	env->DeleteLocalRef(cls);
}

void JniIPvrRecOpenParam::toCppObject(JNIEnv *env, jobject javaobj, void *outCppObj){
	if(outCppObj == NULL)
		return;

	if (javaobj == NULL){
		LOGW("JniIPvrRecOpenParam::toCppObject javaobj is null %p", javaobj);
		return;
	}

	IPvrAbstract::IPvrRecOpenParam* outPvrRecOpenParam = (IPvrAbstract::IPvrRecOpenParam*)outCppObj;

	jstring strPvrDirPath = (jstring)env->GetObjectField(javaobj, m_pPvrDirPath);
	jobject transponder = env->GetObjectField(javaobj, m_Transponder);
	jobject streamInfo = env->GetObjectField(javaobj, m_streamInfo);
	outPvrRecOpenParam->m_bTimeshift = env->GetBooleanField(javaobj, m_bTimeshift);
	outPvrRecOpenParam->m_recSizeLimitMB = env->GetIntField(javaobj, m_recSizeLimitMB);

	int utf8Len = env->GetStringUTFLength(strPvrDirPath);
        outPvrRecOpenParam->m_pPvrDirPath = new char[utf8Len + 1];
	env->GetStringUTFRegion(strPvrDirPath, 0, env->GetStringLength(strPvrDirPath), 
		outPvrRecOpenParam->m_pPvrDirPath);
	outPvrRecOpenParam->m_pPvrDirPath[utf8Len] = '\0';

	JniCommon::GetCommonObject(JniCommon::JniObjectType_Transponder)
			->toCppObject(env, transponder, &outPvrRecOpenParam->m_transponder);
	env->DeleteLocalRef(transponder);

	JniCommon::GetCommonObject(JniCommon::JniObjectType_IPvrStreamInfo)
			->toCppObject(env, streamInfo, &outPvrRecOpenParam->m_streamInfo);
	env->DeleteLocalRef(streamInfo);

}

JniIPvrRecStartParam::JniIPvrRecStartParam(){

}

JniIPvrRecStartParam::~JniIPvrRecStartParam(){

}

void JniIPvrRecStartParam::initObject(JNIEnv *env){
	JniCommon::GetGlobalObjByName(env, gObject, "novel/supertv/dvb/jni/struct/IPvrRecStartParam");
	jclass cls = env->GetObjectClass(gObject);

	init  = env->GetMethodID(cls, "<init>", "()V");
	env->DeleteLocalRef(cls);
}

void JniIPvrRecStartParam::toCppObject(JNIEnv *env, jobject javaobj, void *outCppObj){

}

JniIPvrPbConfigParam::JniIPvrPbConfigParam(){

}

JniIPvrPbConfigParam::~JniIPvrPbConfigParam(){

}

void JniIPvrPbConfigParam::initObject(JNIEnv *env){
	JniCommon::GetGlobalObjByName(env, gObject, "novel/supertv/dvb/jni/struct/IPvrPbConfigParam");
	jclass cls = env->GetObjectClass(gObject);
	outputWinId = env->GetFieldID(cls, "outputWinId", "I");

	init  = env->GetMethodID(cls, "<init>", "()V");
	env->DeleteLocalRef(cls);
}

void JniIPvrPbConfigParam::toCppObject(JNIEnv *env, jobject javaobj, void *outCppObj)
{
	if(outCppObj == NULL)
		return;

	if (javaobj == NULL){
		LOGW("JniIPvrPbConfigParam::toCppObject javaobj is null");
		return;
	}

	IPvrAbstract::IPvrPbConfigParam* outPvrPbConfigParam = (IPvrAbstract::IPvrPbConfigParam*)outCppObj;

	outPvrPbConfigParam->outputWinId = (DD_AV_OUTPUT_WINDOW)env->GetIntField(javaobj, outputWinId);
}

void JniIPvrPbConfigParam::setCppDataToJava(JNIEnv *env, jobject javaObj, void *cppObj)
{
	if(env == NULL || javaObj == NULL || cppObj == NULL )
	{
		LOGW("JniIPvrPbOpenParam::setCppDataToJava Param Error env(%p), javaObj(%p), cppObj(%p)",
			 env, javaObj, cppObj);
		return;
	}

	IPvrAbstract::IPvrPbConfigParam* pPbConfigParam = (IPvrAbstract::IPvrPbConfigParam*)cppObj;

	env->SetIntField(javaObj, outputWinId, pPbConfigParam->outputWinId);
}

JniIPvrPbOpenParam::JniIPvrPbOpenParam(){

}

JniIPvrPbOpenParam::~JniIPvrPbOpenParam(){

}

void JniIPvrPbOpenParam::initObject(JNIEnv *env){
	JniCommon::GetGlobalObjByName(env, gObject, "novel/supertv/dvb/jni/struct/IPvrPbOpenParam");
	jclass cls = env->GetObjectClass(gObject);
	m_pPvrDirPath = env->GetFieldID(cls, "m_pPvrDirPath", "Ljava/lang/String;");
	m_bTimeshift = env->GetFieldID(cls, "m_bTimeshift", "Z");
	m_configParam = env->GetFieldID(cls, "m_configParam", "Lnovel/supertv/dvb/jni/struct/IPvrPbConfigParam;");

	init  = env->GetMethodID(cls, "<init>", "()V");
	env->DeleteLocalRef(cls);
}

void JniIPvrPbOpenParam::toCppObject(JNIEnv *env, jobject javaobj, void *outCppObj)
{
	LOGI("JniIPvrPbOpenParam::toCppObject %d\n", __LINE__);
	if(outCppObj == NULL)
		return;

	if (javaobj == NULL){
		LOGW("JniIPvrPbOpenParam::toCppObject javaobj is null");
		return;
	}

	LOGI("JniIPvrPbOpenParam::toCppObject %d\n", __LINE__);

	IPvrAbstract::IPvrPbOpenParam* outPvrPbOpenParam = (IPvrAbstract::IPvrPbOpenParam*)outCppObj;

	jstring strPvrDirPath = (jstring)env->GetObjectField(javaobj, m_pPvrDirPath);
	outPvrPbOpenParam->m_bTimeshift = env->GetBooleanField(javaobj, m_bTimeshift);
	jobject configParamObj = env->GetObjectField(javaobj, m_configParam);

	int utf8Len = env->GetStringUTFLength(strPvrDirPath);
        outPvrPbOpenParam->m_pPvrDirPath = new char[utf8Len + 1];
	env->GetStringUTFRegion(strPvrDirPath, 0, env->GetStringLength(strPvrDirPath), 
		outPvrPbOpenParam->m_pPvrDirPath);
	outPvrPbOpenParam->m_pPvrDirPath[utf8Len] = '\0';

	LOGI("outPvrPbOpenParam->m_pPvrDirPath %s\n", outPvrPbOpenParam->m_pPvrDirPath);

	JniCommon::GetCommonObject(JniCommon::JniObjectType_IPvrPbConfigParam)
			->toCppObject(env, configParamObj, &outPvrPbOpenParam->m_configParam);
	env->DeleteLocalRef(configParamObj);

	LOGI("JniIPvrPbOpenParam::toCppObject %d\n", __LINE__);
}

JniIPvrPbStartParam::JniIPvrPbStartParam(){

}

JniIPvrPbStartParam::~JniIPvrPbStartParam(){

}

void JniIPvrPbStartParam::initObject(JNIEnv *env){
	JniCommon::GetGlobalObjByName(env, gObject, "novel/supertv/dvb/jni/struct/IPvrPbStartParam");
	jclass cls = env->GetObjectClass(gObject);
	m_startTimeMs = env->GetFieldID(cls, "m_startTimeMs", "I");
	m_speed = env->GetFieldID(cls, "m_speed", "I");
	m_audioLanguage = env->GetFieldID(cls, "m_audioLanguage", "I");

	init  = env->GetMethodID(cls, "<init>", "()V");
	env->DeleteLocalRef(cls);
}

void JniIPvrPbStartParam::toCppObject(JNIEnv *env, jobject javaobj, void *outCppObj)
{
	if(outCppObj == NULL)
		return;

	if (javaobj == NULL){
		LOGW("JniIPvrPbStartParam::toCppObject javaobj is null");
		return;
	}

	IPvrAbstract::IPvrPbStartParam* outPvrPbStartParam = (IPvrAbstract::IPvrPbStartParam*)outCppObj;

	outPvrPbStartParam->m_startTimeMs = (DD_AV_OUTPUT_WINDOW)env->GetIntField(javaobj, m_startTimeMs);
	outPvrPbStartParam->m_speed = (IPvrAbstract::IPvrSpeed) env->GetIntField(javaobj, m_speed);
	outPvrPbStartParam->m_audioLanguage = env->GetIntField(javaobj, m_audioLanguage);

}

JnistLNB::JnistLNB(){

}

JnistLNB::~JnistLNB(){

}

void JnistLNB::initObject(JNIEnv *env){
	JniCommon::GetGlobalObjByName(env, gObject, "novel/supertv/dvb/jni/struct/stLNB");

	jclass cls = env->GetObjectClass(gObject);
	dwaLnbFrequency = env->GetFieldID(cls, "dwaLnbFrequency", "[I");
	bTone22KHz = env->GetFieldID(cls, "bTone22KHz", "I");
	bSw12v = env->GetFieldID(cls, "bSw12v", "I");
	bDiseqcType = env->GetFieldID(cls, "bDiseqcType", "I");
	bDiseqcSw = env->GetFieldID(cls, "bDiseqcSw", "I");
	bLnbVoltage = env->GetFieldID(cls, "bLnbVoltage", "I");
	bPositioner = env->GetFieldID(cls, "bPositioner", "I");

	init  = env->GetMethodID(cls, "<init>", "()V");
	env->DeleteLocalRef(cls);
}

void JnistLNB::toCppObject(JNIEnv *env, jobject javaobj, void *outCppObj){
	if(outCppObj == NULL)
		return;

	if (javaobj == NULL){
		LOGW("JnistLNB::toCppObject javaobj is null");
		return;
	}

	stLNB* pSearchList = (stLNB*)outCppObj;
	jintArray intArrayLnbFrequency = (jintArray)env->GetObjectField(javaobj, dwaLnbFrequency);
	if(intArrayLnbFrequency != 0){
                env->GetIntArrayRegion(intArrayLnbFrequency, 0, 2, (jint*)pSearchList->dwaLnbFrequency);
		env->DeleteLocalRef(intArrayLnbFrequency);
	}

	pSearchList->bTone22KHz = env->GetIntField(javaobj, bTone22KHz);
	pSearchList->bSw12v = env->GetIntField(javaobj, bSw12v);
	pSearchList->bDiseqcType = env->GetIntField(javaobj, bDiseqcType);
	pSearchList->bDiseqcSw = env->GetIntField(javaobj, bDiseqcSw);
	pSearchList->bLnbVoltage = env->GetIntField(javaobj, bLnbVoltage);
	pSearchList->bPositioner = env->GetIntField(javaobj, bPositioner);
}


JniIPSearchTp::JniIPSearchTp(){

}

JniIPSearchTp::~JniIPSearchTp(){

}

void JniIPSearchTp::initObject(JNIEnv *env){
	JniCommon::GetGlobalObjByName(env, gObject, "novel/supertv/dvb/jni/struct/IPSearchTp");

	jclass cls = env->GetObjectClass(gObject);
	tpParam = env->GetFieldID(cls, "tpParam", "Lnovel/supertv/dvb/jni/struct/Transponder;");
	bSearchNit = env->GetFieldID(cls, "bSearchNit", "Z");
	bSearchBat = env->GetFieldID(cls, "bSearchBat", "Z");

	init  = env->GetMethodID(cls, "<init>", "()V");
	env->DeleteLocalRef(cls);
}

void JniIPSearchTp::toCppObject(JNIEnv *env, jobject javaobj, void *outCppObj){
	if(outCppObj == NULL)
		return;

	if (javaobj == NULL){
		LOGW("JniIPSearchTp::toCppObject javaobj is null");
		return;
	}

	IProgramSearchAbstract::IPSearchTp* pSearchList =
			(IProgramSearchAbstract::IPSearchTp*)outCppObj;

	jobject transponder = env->GetObjectField(javaobj, tpParam);

	JniCommon::GetCommonObject(JniCommon::JniObjectType_Transponder)
			->toCppObject(env, transponder, &pSearchList->tpParam);

	pSearchList->bSearchNit = env->GetBooleanField(javaobj, bSearchNit);
	pSearchList->bSearchBat = env->GetBooleanField(javaobj, bSearchBat);
}


JniIPSearchList::JniIPSearchList(){

}

JniIPSearchList::~JniIPSearchList(){

}

void JniIPSearchList::initObject(JNIEnv *env){
	JniCommon::GetGlobalObjByName(env, gObject, "novel/supertv/dvb/jni/struct/IPSearchList");

	jclass cls = env->GetObjectClass(gObject);
	tpList = env->GetFieldID(cls, "tpList", "[Lnovel/supertv/dvb/jni/struct/IPSearchTp;");
	bUseLnb = env->GetFieldID(cls, "bUseLnb", "Z");
	lnb = env->GetFieldID(cls, "lnb", "Lnovel/supertv/dvb/jni/struct/stLNB;");

	init  = env->GetMethodID(cls, "<init>", "()V");
	env->DeleteLocalRef(cls);
}

void JniIPSearchList::toCppObject(JNIEnv *env, jobject javaobj, void *outCppObj){
	if(outCppObj == NULL)
		return;

	if (javaobj == NULL){
		LOGW("JniIPSearchOpenParam::toCppObject javaobj is null");
		return;
	}

	IProgramSearchAbstract::IPSearchList* pSearchList =
			(IProgramSearchAbstract::IPSearchList*)outCppObj;

	JniObject* pJniSearchList = JniCommon::GetCommonObject(JniCommon::JniObjectType_IPSearchTp);
	jobjectArray objArrayIPSearchTp = (jobjectArray)env->GetObjectField(javaobj, tpList);

	if(objArrayIPSearchTp != 0){
		jsize sizeIPSearchList = env->GetArrayLength(objArrayIPSearchTp);

		for(int i = 0 ; i < sizeIPSearchList; i ++){
			jobject objIPSearchTp = env->GetObjectArrayElement(objArrayIPSearchTp, i);
			IProgramSearchAbstract::IPSearchTp tempSearchTp;
			pJniSearchList->toCppObject(env, objIPSearchTp, &tempSearchTp);
			pSearchList->tpList.push_back(tempSearchTp);

			env->DeleteLocalRef(objIPSearchTp);
		}
		env->DeleteLocalRef(objArrayIPSearchTp);
	}

	pSearchList->bUseLnb = env->GetByteField(javaobj, bUseLnb);
	jobject objLNB = env->GetObjectField(javaobj, lnb);

	JniCommon::GetCommonObject(JniCommon::JniObjectType_stLNB)
			->toCppObject(env, objLNB, &pSearchList->lnb);

	env->DeleteLocalRef(objLNB);
}

JniIPSearchOpenParam::JniIPSearchOpenParam(){

}

JniIPSearchOpenParam::~JniIPSearchOpenParam(){

}

void JniIPSearchOpenParam::initObject(JNIEnv *env){
	JniCommon::GetGlobalObjByName(env, gObject, "novel/supertv/dvb/jni/struct/IPSearchOpenParam");
	jclass cls = env->GetObjectClass(gObject);
	searchList = env->GetFieldID(cls, "searchList", "[Lnovel/supertv/dvb/jni/struct/IPSearchList;");
	ValidNetId = env->GetFieldID(cls, "ValidNetId", "[C");
	ValidServiceType = env->GetFieldID(cls, "ValidServiceType", "[B");
	bServiceTypeAdjustByElement = env->GetFieldID(cls, "bServiceTypeAdjustByElement", "Z");
	bTsidByPATOtherwiseSDT = env->GetFieldID(cls, "bTsidByPATOtherwiseSDT", "Z");
	bSearchServiceFTA = env->GetFieldID(cls, "bSearchServiceFTA", "Z");
	bSearchServiceScramble = env->GetFieldID(cls, "bSearchServiceScramble", "Z");
	bDirectSearchServiceAfterNitFailed = env->GetFieldID(cls, "bDirectSearchServiceAfterNitFailed", "Z");

	init  = env->GetMethodID(cls, "<init>", "()V");
	env->DeleteLocalRef(cls);
}

void JniIPSearchOpenParam::toCppObject(JNIEnv *env, jobject javaobj, void* outCppObj){
	if(outCppObj == NULL)
		return;

	if (javaobj == NULL){
		LOGW("JniIPSearchOpenParam::toCppObject javaobj is null");
		return;
	}

	IProgramSearchAbstract::IPSearchOpenParam* pOpenParam =
			(IProgramSearchAbstract::IPSearchOpenParam*)outCppObj;

	JniObject* pJniIPSearchList = JniCommon::GetCommonObject(JniCommon::JniObjectType_IPSearchList);
	jobjectArray objArrayIPSearchList = (jobjectArray)env->GetObjectField(javaobj, searchList);
	if(objArrayIPSearchList != 0){
		jsize sizeIPSearchList = env->GetArrayLength(objArrayIPSearchList);

		for(int i = 0 ; i < sizeIPSearchList; i ++){
			jobject objIPSearchList = env->GetObjectArrayElement(objArrayIPSearchList, i);
			IProgramSearchAbstract::IPSearchList tempSearchList;
			pJniIPSearchList->toCppObject(env, objIPSearchList, &tempSearchList);
			pOpenParam->searchList.push_back(tempSearchList);

			env->DeleteLocalRef(objIPSearchList);
		}
		env->DeleteLocalRef(objArrayIPSearchList);
	}

	jcharArray charArrayValidNetId = (jcharArray)env->GetObjectField(javaobj, ValidNetId);
	if(charArrayValidNetId != 0){
		jsize sizeValidNetId = env->GetArrayLength(charArrayValidNetId);
		jchar* pValidNetId = env->GetCharArrayElements(charArrayValidNetId, NULL);
		for(int i = 0; i < sizeValidNetId; i ++){
			pOpenParam->ValidNetId.insert(pValidNetId[i]);
		}
		env->ReleaseCharArrayElements(charArrayValidNetId, pValidNetId, JNI_ABORT);
	}

	jbyteArray byteArrayValidServiceType = (jbyteArray)env->GetObjectField(javaobj, ValidServiceType);
	if(byteArrayValidServiceType != 0){
		jsize sizeValidServiceType = env->GetArrayLength(byteArrayValidServiceType);
		jbyte* pValidServiceType = env->GetByteArrayElements(byteArrayValidServiceType, NULL);
		for(int i = 0; i < sizeValidServiceType; i ++){
			pOpenParam->ValidServiceType.insert(pValidServiceType[i]);
		}
		env->ReleaseByteArrayElements(byteArrayValidServiceType, pValidServiceType, JNI_ABORT);
	}

	pOpenParam->bServiceTypeAdjustByElement = env->GetBooleanField(javaobj, bServiceTypeAdjustByElement);
	pOpenParam->bTsidByPATOtherwiseSDT = env->GetBooleanField(javaobj, bTsidByPATOtherwiseSDT);
	pOpenParam->bSearchServiceFTA = env->GetBooleanField(javaobj, bSearchServiceFTA);
	pOpenParam->bSearchServiceScramble = env->GetBooleanField(javaobj, bSearchServiceScramble);
	pOpenParam->bDirectSearchServiceAfterNitFailed = env->GetBooleanField(javaobj, bDirectSearchServiceAfterNitFailed);
}

JniIPSearchCBParam_List::JniIPSearchCBParam_List(){

}

JniIPSearchCBParam_List::~JniIPSearchCBParam_List(){

}

void JniIPSearchCBParam_List::initObject(JNIEnv *env){
	JniCommon::GetGlobalObjByName(env, gObject, "novel/supertv/dvb/jni/struct/IPSearchCBParam_List");
	jclass cls = env->GetObjectClass(gObject);

	listIndex = env->GetFieldID(cls, "listIndex", "C");
	listTotalNum = env->GetFieldID(cls, "listTotalNum", "C");

	init  = env->GetMethodID(cls, "<init>", "()V");
	env->DeleteLocalRef(cls);
}

jobject JniIPSearchCBParam_List::createNewObject(JNIEnv *env, void *pData){
	if(pData == NULL)
		return 0;
	LOGI("Create JniIPSearchCBParam_List");
	IProgramSearchAbstract::IPSearchCBParam_List* pCBParam_List =
			(IProgramSearchAbstract::IPSearchCBParam_List*)pData;

	jclass cls = env->GetObjectClass(gObject);

	jobject objout = env->NewObject(cls, init);

	env->SetCharField(objout, listIndex, pCBParam_List->listIndex);
	env->SetCharField(objout, listTotalNum, pCBParam_List->listTotalNum);

	env->DeleteLocalRef(cls);

	return objout;
}

JniIPSearchCBParam_Tp::JniIPSearchCBParam_Tp(){

}

JniIPSearchCBParam_Tp::~JniIPSearchCBParam_Tp(){

}

void JniIPSearchCBParam_Tp::initObject(JNIEnv *env){
	JniCommon::GetGlobalObjByName(env, gObject, "novel/supertv/dvb/jni/struct/IPSearchCBParam_Tp");
	jclass cls = env->GetObjectClass(gObject);

	tpIndexOfList = env->GetFieldID(cls, "tpIndexOfList", "C");
	tpTotalNumOfList = env->GetFieldID(cls, "tpTotalNumOfList", "C");
	tpTotalNumOfAll = env->GetFieldID(cls, "tpTotalNumOfAll", "C");
	frequencyKHz = env->GetFieldID(cls, "frequencyKHz", "I");
	symbolRateHz = env->GetFieldID(cls, "symbolRateHz", "I");
	modulation = env->GetFieldID(cls, "modulation", "I");
	listIndex = env->GetFieldID(cls, "listIndex", "I");

	init  = env->GetMethodID(cls, "<init>", "()V");
	env->DeleteLocalRef(cls);
}

jobject JniIPSearchCBParam_Tp::createNewObject(JNIEnv *env, void *pData){
	if(pData == NULL)
		return 0;
	LOGI("Create JniIPSearchCBParam_Tp");
	IProgramSearchAbstract::IPSearchCBParam_Tp* pCBParam_Tp =
			(IProgramSearchAbstract::IPSearchCBParam_Tp*)pData;

	jclass cls = env->GetObjectClass(gObject);

	jobject objout = env->NewObject(cls, init);

	env->SetCharField(objout, tpIndexOfList, pCBParam_Tp->tpIndexOfList);
	env->SetCharField(objout, tpTotalNumOfList, pCBParam_Tp->tpTotalNumOfList);
	env->SetCharField(objout, tpTotalNumOfAll, pCBParam_Tp->tpTotalNumOfAll);
	env->SetIntField(objout, frequencyKHz, pCBParam_Tp->frequencyKHz);
	env->SetIntField(objout, symbolRateHz, pCBParam_Tp->symbolRateHz);
	env->SetIntField(objout, modulation, pCBParam_Tp->modulation);
	env->SetIntField(objout, listIndex, pCBParam_Tp->listIndex);

	env->DeleteLocalRef(cls);

	return objout;
}

JnistTunerState::JnistTunerState(){

}

JnistTunerState::~JnistTunerState(){

}

void JnistTunerState::initObject(JNIEnv *env){
	JniCommon::GetGlobalObjByName(env, gObject, "novel/supertv/dvb/jni/struct/stTunerState");
	jclass cls = env->GetObjectClass(gObject);

	LockState = env->GetFieldID(cls, "LockState", "I");
	Frequency = env->GetFieldID(cls, "Frequency", "I");
	SymbolRate = env->GetFieldID(cls, "SymbolRate", "I");
	Modulation = env->GetFieldID(cls, "Modulation", "I");
	SignalStrength = env->GetFieldID(cls, "SignalStrength", "I");
	SignalQuality = env->GetFieldID(cls, "SignalQuality", "I");
	BitErrorRate = env->GetFieldID(cls, "BitErrorRate", "I");

	init  = env->GetMethodID(cls, "<init>", "()V");
	env->DeleteLocalRef(cls);
}

jobject JnistTunerState::createNewObject(JNIEnv *env, void *pData){
	if(pData == NULL)
		return 0;
	LOGI("Create JnistTunerState");
	stTunerState* pTunerState =
			(stTunerState*)pData;

	jclass cls = env->GetObjectClass(gObject);

	jobject objout = env->NewObject(cls, init);

	env->SetIntField(objout, LockState, pTunerState->LockState);
	env->SetIntField(objout, Frequency, pTunerState->Frequency);
	env->SetIntField(objout, SymbolRate, pTunerState->SymbolRate);
	env->SetIntField(objout, Modulation, pTunerState->Modulation);
	env->SetIntField(objout, SignalStrength, pTunerState->SignalStrength);
	env->SetIntField(objout, SignalQuality, pTunerState->SignalQuality);
	env->SetIntField(objout, BitErrorRate, pTunerState->BitErrorRate);

	env->DeleteLocalRef(cls);

	return objout;
}

JnistNetwork::JnistNetwork(){

}

JnistNetwork::~JnistNetwork(){

}

void JnistNetwork::initObject(JNIEnv *env){
	JniCommon::GetGlobalObjByName(env, gObject, "novel/supertv/dvb/jni/struct/stNetwork");
	jclass cls = env->GetObjectClass(gObject);

	NetworkId = env->GetFieldID(cls, "NetworkId", "C");
	NetworkName = env->GetFieldID(cls, "NetworkName", "Ljava/lang/String;");
	NitVersion = env->GetFieldID(cls, "NitVersion", "I");
	TotalTsNum = env->GetFieldID(cls, "TotalTsNum", "C");

	init  = env->GetMethodID(cls, "<init>", "()V");
	env->DeleteLocalRef(cls);
}

jobject JnistNetwork::createNewObject(JNIEnv *env, void *pData){
	if(pData == NULL)
		return 0;
	LOGI("Create JnistNetwork");
	stNetwork* pNetwork = (stNetwork*)pData;

	jclass cls = env->GetObjectClass(gObject);

	jobject objout = env->NewObject(cls, init);

	jstring strNetworkName = JniCommon::CharTojstring(env, pNetwork->NetworkName);

	env->SetCharField(objout, NetworkId, pNetwork->NetworkId);
	env->SetObjectField(objout, NetworkName, strNetworkName);
	env->SetIntField(objout, NitVersion, pNetwork->NitVersion);
	env->SetCharField(objout, TotalTsNum, pNetwork->TotalTsNum);

	env->DeleteLocalRef(cls);
	env->DeleteLocalRef(strNetworkName);

	return objout;
}

JnistWindowRect::JnistWindowRect(){

}

JnistWindowRect::~JnistWindowRect(){

}

void JnistWindowRect::initObject(JNIEnv *env){
    JniCommon::GetGlobalObjByName(env, gObject, "android/graphics/Rect");
    jclass cls = env->GetObjectClass(gObject);

    left = env->GetFieldID(cls, "left", "I");
    top = env->GetFieldID(cls, "top", "I");
    right = env->GetFieldID(cls, "right", "I");
    bottom = env->GetFieldID(cls, "bottom", "I");

    init  = env->GetMethodID(cls, "<init>", "()V");
    env->DeleteLocalRef(cls);
}

jobject JnistWindowRect::createNewObject(JNIEnv *env, void *pData){
    if(env == NULL || pData == NULL){
        LOGW("JnistWindowRect createNewObject env is %p, pData is %p", env, pData);
        return 0;
    }

    LOGI("Create JnistWindowRect");
    stWindowRect* pRect = (stWindowRect*)pData;

    jclass cls = env->GetObjectClass(gObject);

    jobject objout = env->NewObject(cls, init);

    env->SetIntField(objout, left, pRect->left);
    env->SetCharField(objout, top, pRect->top);
    env->SetCharField(objout, right, pRect->left + pRect->width);
    env->SetIntField(objout, bottom, pRect->top + pRect->height);

    env->DeleteLocalRef(cls);

    return objout;
}

void JnistWindowRect::toCppObject(JNIEnv *env, jobject javaobj, void *outCppObj){
    if(javaobj == NULL){
        LOGW("JniIPPlayStartParam toCppObject javaobj is null");
        return;
    }

    if (outCppObj == NULL){
        LOGW("JniIPPlayStartParam toCppObject outCppObj is null");
        return;
    }

    stWindowRect* pRect = (stWindowRect*)outCppObj;
    pRect->left = env->GetIntField(javaobj, left);
    pRect->top = env->GetIntField(javaobj, top);
    pRect->width = env->GetIntField(javaobj, right) - pRect->left;
    pRect->height = env->GetIntField(javaobj, bottom) - pRect->top;

}

JniIPSearchCBParam_TpInfo::JniIPSearchCBParam_TpInfo(){

}

JniIPSearchCBParam_TpInfo::~JniIPSearchCBParam_TpInfo(){

}

void JniIPSearchCBParam_TpInfo::initObject(JNIEnv *env){
    JniCommon::GetGlobalObjByName(env, gObject, "novel/supertv/dvb/jni/struct/IPSearchCBParam_TpInfo");
    jclass cls = env->GetObjectClass(gObject);

    tsId = env->GetFieldID(cls, "tsId", "C");
    networkId = env->GetFieldID(cls, "networkId", "C");
    orgNetworkId = env->GetFieldID(cls, "orgNetworkId", "C");
    serviceNum = env->GetFieldID(cls, "serviceNum", "C");
    patVersion = env->GetFieldID(cls, "patVersion", "I");
    sdtVersion = env->GetFieldID(cls, "sdtVersion", "I");

    init  = env->GetMethodID(cls, "<init>", "()V");
    env->DeleteLocalRef(cls);
}

jobject JniIPSearchCBParam_TpInfo::createNewObject(JNIEnv *env, void *pData){
    if(pData == NULL)
        return 0;
    LOGI("Create JniIPSearchCBParam_TpInfo");
    IProgramSearchAbstract::IPSearchCBParam_TpInfo* pTpInfo =
                    (IProgramSearchAbstract::IPSearchCBParam_TpInfo*)pData;

    jclass cls = env->GetObjectClass(gObject);

    jobject objout = env->NewObject(cls, init);

    env->SetCharField(objout, tsId, pTpInfo->tsId);
    env->SetCharField(objout, networkId, pTpInfo->networkId);
    env->SetCharField(objout, orgNetworkId, pTpInfo->orgNetworkId);
    env->SetIntField(objout, patVersion, pTpInfo->patVersion);
    env->SetIntField(objout, sdtVersion, pTpInfo->sdtVersion);
    env->SetCharField(objout, serviceNum, pTpInfo->serviceNum);

    env->DeleteLocalRef(cls);

    return objout;
}

JnistBouquet::JnistBouquet(){

}

JnistBouquet::~JnistBouquet(){

}

void JnistBouquet::initObject(JNIEnv *env){
	JniCommon::GetGlobalObjByName(env, gObject, "novel/supertv/dvb/jni/struct/stBouquet");
	jclass cls = env->GetObjectClass(gObject);

	BouquetId = env->GetFieldID(cls, "BouquetId", "C");
	BouquetName = env->GetFieldID(cls, "BouquetName", "Ljava/lang/String;");
	BatVersion = env->GetFieldID(cls, "BatVersion", "I");
	ServiceList = env->GetFieldID(cls, "ServiceList", "[Lnovel/supertv/dvb/jni/struct/stServiceIdent;");

	init  = env->GetMethodID(cls, "<init>", "()V");
	env->DeleteLocalRef(cls);
}

jobject JnistBouquet::createNewObject(JNIEnv *env, void *pData){
	if(pData == NULL)
		return 0;
	LOGI("Create JnistBouquet");
	stBouquet* pBouquest = (stBouquet*)pData;

	jclass cls = env->GetObjectClass(gObject);

	jobject objout = env->NewObject(cls, init);

	jstring strBouquetName = JniCommon::CharTojstring(env, pBouquest->BouquetName);

	env->SetCharField(objout, BouquetId, pBouquest->BouquetId);
	env->SetObjectField(objout, BouquetName, strBouquetName);
	env->SetIntField(objout, BatVersion, pBouquest->BatVersion);

	JniObject* pServiceIdent = JniCommon::GetCommonObject(JniCommon::JniObjectType_stServiceIdent);
	jclass clsServiceIdent = pServiceIdent->getJClass(env);
	jobjectArray objArrayServiceList = env->NewObjectArray(pBouquest->ServiceList.size(),
														   clsServiceIdent, 0);
	env->DeleteLocalRef(clsServiceIdent);
	for(int i = 0; i < pBouquest->ServiceList.size(); i ++){
		jobject service = pServiceIdent->createNewObject(env, &pBouquest->ServiceList[i]);
		env->SetObjectArrayElement(objArrayServiceList, i, service);
		env->DeleteLocalRef(service);
	}
	env->SetObjectField(objout, ServiceList, objArrayServiceList);

	env->DeleteLocalRef(strBouquetName);
	env->DeleteLocalRef(objArrayServiceList);
	env->DeleteLocalRef(cls);

	return objout;
}

JniIBouquetInfoList::JniIBouquetInfoList(){

}

JniIBouquetInfoList::~JniIBouquetInfoList(){

}

void JniIBouquetInfoList::initObject(JNIEnv *env){
	JniCommon::GetGlobalObjByName(env, gObject, "novel/supertv/dvb/jni/struct/IBouquetInfoList");
	jclass cls = env->GetObjectClass(gObject);

	bouquetId = env->GetFieldID(cls, "bouquetId", "C");
	bouquetName = env->GetFieldID(cls, "bouquetName", "Ljava/lang/String;");
	bouquetVersion = env->GetFieldID(cls, "bouquetVersion", "I");
	serviceNum = env->GetFieldID(cls, "serviceNum", "C");

	init  = env->GetMethodID(cls, "<init>", "()V");
	env->DeleteLocalRef(cls);
}

jobject JniIBouquetInfoList::createNewObject(JNIEnv *env, void *pData){
    if(pData == NULL)
            return 0;
    LOGI("Create JniIBouqetInfoList");

    IBouquetMonitorAbstract::IBouquetInfoList* pBouquest = (IBouquetMonitorAbstract::IBouquetInfoList*)pData;

    jclass cls = env->GetObjectClass(gObject);

    jobject objout = env->NewObject(cls, init);

    jstring strBouquetName = JniCommon::CharTojstring(env, pBouquest->bouquetName);

    env->SetCharField(objout, bouquetId, pBouquest->bouquetId);
    env->SetObjectField(objout, bouquetName, strBouquetName);
    env->SetIntField(objout, bouquetVersion, pBouquest->bouquetVersion);
    env->SetCharField(objout, serviceNum, pBouquest->serviceNum);

    env->DeleteLocalRef(strBouquetName);
    env->DeleteLocalRef(cls);

    return objout;
}

JnistEitEvent::JnistEitEvent(){

}

JnistEitEvent::~JnistEitEvent(){

}

void JnistEitEvent::initObject(JNIEnv *env){
    JniCommon::GetGlobalObjByName(env, gObject,"novel/supertv/dvb/jni/struct/stEitEvent");
    jclass cls = env->GetObjectClass(gObject);
//
	ServiceIdent = env->GetFieldID(cls, "ServiceIdent", "Lnovel/supertv/dvb/jni/struct/stServiceIdent;");
    EventId = env->GetFieldID(cls, "EventId", "C");//
    EventName = env->GetFieldID(cls, "EventName", "Ljava/lang/String;");//
    Text = env->GetFieldID(cls, "Text", "Ljava/lang/String;");//
    StartMjd = env->GetFieldID(cls, "StartMjd", "I");//
    StartUtc = env->GetFieldID(cls, "StartUtc", "I");//
    DurationUtc = env->GetFieldID(cls, "DurationUtc", "I");//
    refServiceId = env->GetFieldID(cls, "refServiceId", "C");
    refEventId = env->GetFieldID(cls, "refEventId", "C");
    bTimeshiftEvent = env->GetFieldID(cls, "bTimeshiftEvent", "Z");
    EventType=env->GetFieldID(cls, "EventType", "B");
    parentRating=env->GetFieldID(cls, "parentRating", "B");
//
    init = env->GetMethodID(cls, "<init>", "()V");
    env->DeleteLocalRef(cls);
}

jobject JnistEitEvent::createNewObject(JNIEnv *env, void *pData){
//    LOGI("Create JniEITEvent");
    stEitEvent* pEITEvent = (stEitEvent*)pData;

    jclass cls = env->GetObjectClass(gObject);

    jobject objout = env->NewObject(cls, init);
    env->DeleteLocalRef(cls);

	JniObject* pServiceIdent = JniCommon::GetCommonObject(JniCommon::JniObjectType_stServiceIdent);
	jobject objServiceIdent = pServiceIdent->createNewObject(env, &(pEITEvent->servIdent));
	env->SetObjectField(objout, this->ServiceIdent, objServiceIdent);
	env->DeleteLocalRef(objServiceIdent);
	
    env->SetCharField(objout, this->EventId, pEITEvent->EventId);
    env->SetIntField(objout, this->StartMjd, pEITEvent->StartMjd);
    env->SetIntField(objout, this->StartUtc, pEITEvent->StartUtc);
    env->SetIntField(objout, this->DurationUtc, pEITEvent->DurationUtc);
    jstring name = JniCommon::CharTojstring(env, pEITEvent->EventName);
    env->SetObjectField(objout, this->EventName, name);
    env->DeleteLocalRef(name);
    jstring eit_text = JniCommon::CharTojstring(env, pEITEvent->Text);
    env->SetObjectField(objout, this->Text, eit_text);

    env->DeleteLocalRef(eit_text);
    env->SetCharField(objout, this->refServiceId, pEITEvent->refServiceId);
    env->SetCharField(objout, this->refEventId, pEITEvent->refEventId);
	
    env->SetBooleanField(objout, this->bTimeshiftEvent, pEITEvent->bTimeshiftEvent);//bool
    env->SetByteField(objout, this->EventType, pEITEvent->EventType);//bye
    env->SetByteField(objout, this->parentRating, pEITEvent->parentRating);//bye
 

    return objout;
}
JnistEitEventExtDesc::JnistEitEventExtDesc(){

}

JnistEitEventExtDesc::~JnistEitEventExtDesc(){

}

void JnistEitEventExtDesc::initObject(JNIEnv *env){
	JniCommon::GetGlobalObjByName(env, gObject, "novel/supertv/dvb/jni/struct/stEITEventExtDesc");
	jclass cls = env->GetObjectClass(gObject);

//	BouquetId = env->GetFieldID(cls, "BouquetId", "C");
//	BouquetName = env->GetFieldID(cls, "BouquetName", "Ljava/lang/String;");
	EventId = env->GetFieldID(cls, "EventId", "C");
   	Text = env->GetFieldID(cls, "Text", "Ljava/lang/String;");
	init  = env->GetMethodID(cls, "<init>", "()V");
	env->DeleteLocalRef(cls);
}

jobject JnistEitEventExtDesc::createNewObject(JNIEnv *env, void *pData){
	if(pData == NULL)
		return 0;
	LOGI("Create JnistEitEvent");
	stEitEventExtDesc* EitEventdesc = (stEitEventExtDesc*)pData;

	jclass cls = env->GetObjectClass(gObject);
	jobject objout = env->NewObject(cls, init);
  	env->DeleteLocalRef(cls);

	env->SetCharField(objout, EventId, EitEventdesc->EventId);
	
	jstring eit_text = JniCommon::CharTojstring(env, EitEventdesc->Text);
	env->SetObjectField(objout, Text, eit_text);
	env->DeleteLocalRef(eit_text);

	return objout;
}

JniCAIPP::JniCAIPP()
{

}

JniCAIPP::~JniCAIPP()
{

}

void JniCAIPP::initObject(JNIEnv *env){
	JniCommon::GetGlobalObjByName(env, gObject, "novel/supertv/dvb/jni/ConditionAccess$CAIpp");
	jclass cls = env->GetObjectClass(gObject);
	
    TVSID = env->GetFieldID(cls, "TVSID", "I");
    ProdID = env->GetFieldID(cls, "ProdID", "I");
    SlotID = env->GetFieldID(cls, "SlotID", "I");
    ProdName = env->GetFieldID(cls, "ProdName", "Ljava/lang/String;");
    StartTime = env->GetFieldID(cls, "StartTime", "J");
    Duration = env->GetFieldID(cls, "Duration", "I");
    ServiceName = env->GetFieldID(cls, "ServiceName", "Ljava/lang/String;");
    CurTppTapPrice = env->GetFieldID(cls, "CurTppTapPrice", "I");
    CurTppNoTapPrice = env->GetFieldID(cls, "CurTppNoTapPrice", "I");
    CurCppTapPrice = env->GetFieldID(cls, "CurCppTapPrice", "I");
    CurCppNoTapPrice = env->GetFieldID(cls, "CurCppNoTapPrice", "I");
    BookedPrice = env->GetFieldID(cls, "BookedPrice", "I");
    PriceType = env->GetFieldID(cls, "PriceType", "I");
    Interval = env->GetFieldID(cls, "Interval", "I");
    CurInterval = env->GetFieldID(cls, "CurInterval", "I");
    IppStatus = env->GetFieldID(cls, "IppStatus", "I");
    Unit = env->GetFieldID(cls, "Unit", "I");
    IpptPeriod = env->GetFieldID(cls, "IpptPeriod", "I");
	
	init  = env->GetMethodID(cls, "<init>", "()V");
	env->DeleteLocalRef(cls);
}


jobject JniCAIPP ::createNewObject(JNIEnv *env, void *pData){
	if(pData == NULL)
		return 0;
	LOGI("Create JniCAIPP");
	CCDTCAIpp* pCaIpp = (CCDTCAIpp*)pData;

	jclass cls = env->GetObjectClass(gObject);

	jobject objout = env->NewObject(cls, init);
  	env->DeleteLocalRef(cls);
		
    env->SetIntField(objout, TVSID, pCaIpp->m_wTVSID);
    env->SetIntField(objout, ProdID, pCaIpp->m_wProdID);
    env->SetIntField(objout, SlotID, pCaIpp->m_bySlotID);
    env->SetIntField(objout, Duration, pCaIpp->m_dwDuration);
    env->SetIntField(objout, CurTppTapPrice, pCaIpp->m_wCurTppTapPrice);
    env->SetIntField(objout, CurTppNoTapPrice, pCaIpp->m_wCurTppNoTapPrice);
    env->SetIntField(objout, CurCppTapPrice, pCaIpp->m_wCurCppTapPrice);
    env->SetIntField(objout, CurCppNoTapPrice, pCaIpp->m_wCurCppNoTapPrice);
    env->SetIntField(objout, BookedPrice, pCaIpp->m_wBookedPrice);
    env->SetIntField(objout, PriceType, pCaIpp->m_byBookedPriceType);
    env->SetIntField(objout, Interval, pCaIpp->m_byBookedInterval);
    env->SetIntField(objout, CurInterval, pCaIpp->m_byCurInterval);
    env->SetIntField(objout, IppStatus, pCaIpp->m_byIppStatus);
    env->SetIntField(objout, Unit, pCaIpp->m_byUnit);
    env->SetIntField(objout, IpptPeriod, pCaIpp->m_wIpptPeriod);
	env->SetLongField(objout, StartTime, pCaIpp->m_tStartTime);

    jstring sProdName = JniCommon::CharTojstring(env, pCaIpp->m_szProdName, strlen(pCaIpp->m_szProdName));
    env->SetObjectField(objout, ProdName, sProdName);
    env->DeleteLocalRef(sProdName);

    jstring sServiceName = JniCommon::CharTojstring(env, pCaIpp->m_szServiceName, strlen(pCaIpp->m_szServiceName));
    env->SetObjectField(objout, ServiceName, sServiceName);
    env->DeleteLocalRef(sServiceName);

	return objout;
}



JniEpgEventCbParam_SnapshotUpdate::JniEpgEventCbParam_SnapshotUpdate(){

}

JniEpgEventCbParam_SnapshotUpdate::~JniEpgEventCbParam_SnapshotUpdate(){

}

void JniEpgEventCbParam_SnapshotUpdate::initObject(JNIEnv *env){
	JniCommon::GetGlobalObjByName(env, gObject, "novel/supertv/dvb/jni/struct/IEpgEventCbParam_SnapshotUpdate");
	jclass cls = env->GetObjectClass(gObject);

	snapshotHandle	= env->GetFieldID(cls, "snapshotHandle", "I");
	init  			= env->GetMethodID(cls, "<init>", "()V");
	env->DeleteLocalRef(cls);
}

jobject JniEpgEventCbParam_SnapshotUpdate::createNewObject(JNIEnv *env, void *pData){
	if(pData == NULL)
		return 0;
	LOGI("Create JniEpgEventCbParam_SnapshotUpdate");
	IEpgAbstract::IEpgEventCbParam_SnapshotUpdate* pIEpgEventCbParam_ServiceUpdate = (IEpgAbstract::IEpgEventCbParam_SnapshotUpdate*)pData;
	jclass cls = env->GetObjectClass(gObject);
	jobject objout = env->NewObject(cls, init);
  	env->DeleteLocalRef(cls);
	env->SetIntField(objout, snapshotHandle, pIEpgEventCbParam_ServiceUpdate->snapshotHandle);

	return objout;
}

JniIPPlayOpenParam::JniIPPlayOpenParam(){

}

JniIPPlayOpenParam::~JniIPPlayOpenParam(){

}

void JniIPPlayOpenParam::initObject(JNIEnv *env){
    JniCommon::GetGlobalObjByName(env, gObject, "novel/supertv/dvb/jni/struct/IPPlayOpenParam");
    jclass cls = env->GetObjectClass(gObject);

    livePlayMode	= env->GetFieldID(cls, "livePlayMode", "I");
    bAutoFixPcrPid      = env->GetFieldID(cls, "bAutoFixPcrPid", "Z");
    init                = env->GetMethodID(cls, "<init>", "()V");
    env->DeleteLocalRef(cls);
}

void JniIPPlayOpenParam::toCppObject(JNIEnv *env, jobject javaobj, void *outCppObj){
    if(javaobj == NULL){
        LOGW("JniIPPlayOpenParam toCppObject javaobj is null");
        return;
    }

    if (outCppObj == NULL){
        LOGW("JniIPPlayOpenParam toCppObject outCppObj is null");
        return;
    }

    IProgramPlayAbstract::IPPlayOpenParam* pCppOpenParam = (IProgramPlayAbstract::IPPlayOpenParam*)outCppObj;
    pCppOpenParam->livePlayMode = (eLivePlayMode)env->GetIntField(javaobj, livePlayMode);
    pCppOpenParam->bAutoFixPcrPid = env->GetBooleanField(javaobj, bAutoFixPcrPid);
}

JniIPPlayStartParam::JniIPPlayStartParam(){

}

JniIPPlayStartParam::~JniIPPlayStartParam(){

}

void JniIPPlayStartParam::initObject(JNIEnv *env){
    JniCommon::GetGlobalObjByName(env, gObject, "novel/supertv/dvb/jni/struct/IPPlayStartParam");
    jclass cls = env->GetObjectClass(gObject);

    bUseLnb	= env->GetFieldID(cls, "bUseLnb", "Z");
    lnbParam= env->GetFieldID(cls, "lnbParam", "Lnovel/supertv/dvb/jni/struct/stLNB;");
    tpParam	= env->GetFieldID(cls, "tpParam", "Lnovel/supertv/dvb/jni/struct/Transponder;");
    service = env->GetFieldID(cls, "service", "Lnovel/supertv/dvb/jni/struct/stChannel;");
    winSize	= env->GetFieldID(cls, "winSize", "Landroid/graphics/Rect;");
    videoWin    = env->GetFieldID(cls, "videoWin", "I");
    audioMode   = env->GetFieldID(cls, "audioMode", "I");

    init                = env->GetMethodID(cls, "<init>", "()V");
    env->DeleteLocalRef(cls);
}

void JniIPPlayStartParam::toCppObject(JNIEnv *env, jobject javaobj, void *outCppObj){
    if(javaobj == NULL){
        LOGW("JniIPPlayStartParam toCppObject javaobj is null");
        return;
    }

    if (outCppObj == NULL){
        LOGW("JniIPPlayStartParam toCppObject outCppObj is null");
        return;
    }

    IProgramPlayAbstract::IPPlayStartParam* pCppStartParam = (IProgramPlayAbstract::IPPlayStartParam*)outCppObj;
    pCppStartParam->bUseLnb = env->GetBooleanField(javaobj, bUseLnb);
    jobject objLnbParam = env->GetObjectField(javaobj, lnbParam);
    if(objLnbParam != 0){
        JniCommon::GetCommonObject(JniCommon::JniObjectType_stLNB)
                ->toCppObject(env, objLnbParam, &pCppStartParam->lnbParam);
        env->DeleteLocalRef(objLnbParam);
    }

    jobject objTransponder = env->GetObjectField(javaobj, tpParam);
    if(objTransponder != 0){
        JniCommon::GetCommonObject(JniCommon::JniObjectType_Transponder)
                ->toCppObject(env, objTransponder, &pCppStartParam->tpParam);
        env->DeleteLocalRef(objTransponder);
    }

    jobject objstChannel = env->GetObjectField(javaobj, service);
    if(objstChannel != 0){
        JniCommon::GetCommonObject(JniCommon::JniObjectType_stChannel)
                ->toCppObject(env, objstChannel, &pCppStartParam->service);
        env->DeleteLocalRef(objstChannel);
    }

    jobject objRect = env->GetObjectField(javaobj, winSize);
    if(objRect != 0){
        JniCommon::GetCommonObject(JniCommon::JniObjectType_stWindowRect)
                ->toCppObject(env, objRect, &pCppStartParam->winSize);
        env->DeleteLocalRef(objRect);
    }

    pCppStartParam->videoWin = (eVideoWindow)env->GetBooleanField(javaobj, videoWin);
    pCppStartParam->audioMode = (eAudioChannelMode)env->GetBooleanField(javaobj, audioMode);
}

JniIBatOrder::JniIBatOrder(){

}

JniIBatOrder::~JniIBatOrder(){

}

void JniIBatOrder::initObject(JNIEnv *env){
    JniCommon::GetGlobalObjByName(env, gObject,"novel/supertv/dvb/jni/struct/IPSearchBatOrder");
    jclass cls = env->GetObjectClass(gObject);
    init = env->GetMethodID(cls, "<init>", "()V");

    ServiceId=env->GetFieldID(cls, "ServiceId", "I");
    m_ChOrder=env->GetFieldID(cls, "m_ChOrder", "I");

    env->DeleteLocalRef(cls);
}

jobject JniIBatOrder::createNewObject(JNIEnv * env, void * data){
    if(data == NULL)
        return 0;
//    LOGI("Create JniIBatOrder");
    ChOrder * pChOrder = (ChOrder*)data;

    jclass cls = env->GetObjectClass(gObject);

    jobject objout = env->NewObject(cls, init);

    env->SetIntField(objout, ServiceId, pChOrder->ServiceId);
    env->SetIntField(objout, m_ChOrder, pChOrder->m_ChOrder);

    env->DeleteLocalRef(cls);

    return objout;
}

void JniIBatOrder::toCppObject(JNIEnv *env, jobject javaobj, void *Obj){
    if(javaobj == NULL){
        LOGW("JniIBatOrder toCppObject javaobj is null");
        return;
    }

    if (Obj == NULL){
        LOGW("JniIBatOrder toCppObject Obj is null");
        return;
    }

    ChOrder * pChOrder = (ChOrder*)Obj;

    pChOrder->ServiceId=env->GetIntField(javaobj, ServiceId);
    pChOrder->m_ChOrder=env->GetIntField(javaobj, m_ChOrder);

}


JniISystemInfoParam::JniISystemInfoParam(){

}

JniISystemInfoParam::~JniISystemInfoParam(){

}

void JniISystemInfoParam::initObject(JNIEnv *env){
    JniCommon::GetGlobalObjByName(env, gObject,"novel/supertv/dvb/jni/struct/ISystemInfoParam");
    jclass cls = env->GetObjectClass(gObject);
    init = env->GetMethodID(cls, "<init>", "()V");

    bEvent=env->GetFieldID(cls, "bEvent", "I");
    bMode=env->GetFieldID(cls, "bMode", "I");
    bIntervalTime=env->GetFieldID(cls, "bIntervalTime", "I");

    env->DeleteLocalRef(cls);
}

jobject JniISystemInfoParam::createNewObject(JNIEnv * env, void * data){
    if(data == NULL)
        return 0;

    ISystemInfoAbstract::ISystemInfo_Param * pParam = (ISystemInfoAbstract::ISystemInfo_Param*)data;

    jclass cls = env->GetObjectClass(gObject);

    jobject objout = env->NewObject(cls, init);

    env->SetIntField(objout, bEvent, pParam->bEvent);
    env->SetIntField(objout, bIntervalTime, pParam->bIntervalTime);
    env->SetIntField(objout, bMode, pParam->bMode);

    env->DeleteLocalRef(cls);

    return objout;
}

void JniISystemInfoParam::toCppObject(JNIEnv *env, jobject javaobj, void *Obj){
    if(javaobj == NULL){
        LOGW("JniISystemInfoParam toCppObject javaobj is null");
        return;
    }

    if (Obj == NULL){
        LOGW("JniISystemInfoParam toCppObject Obj is null");
        return;
    }

    ISystemInfoAbstract::ISystemInfo_Param * pParam
            = (ISystemInfoAbstract::ISystemInfo_Param*)Obj;

    pParam->bEvent = (ISystemInfoAbstract::ISystemInfoEvent)env->GetIntField(javaobj, bEvent);
    pParam->bIntervalTime = env->GetIntField(javaobj, bIntervalTime);
    pParam->bMode = (ISystemInfoAbstract::ISystemInfoMode)env->GetIntField(javaobj, bMode);

}

JnistSCAOPPVEntitle::JnistSCAOPPVEntitle(){

}

JnistSCAOPPVEntitle::~JnistSCAOPPVEntitle(){

}

void JnistSCAOPPVEntitle::initObject(JNIEnv *env){
	JniCommon::GetGlobalObjByName(env, gObject,"novel/supertv/dvb/jni/struct/stSCAOPPVEntitle");
	jclass cls = env->GetObjectClass(gObject);
	init = env->GetMethodID(cls, "<init>", "()V");

	event_id=env->GetFieldID(cls, "event_id", "I");
	start_time=env->GetFieldID(cls, "start_time", "I");
	end_time=env->GetFieldID(cls, "end_time", "I");

	env->DeleteLocalRef(cls);
}

jobject JnistSCAOPPVEntitle::createNewObject(JNIEnv *env, void *data){
	if(data == NULL)
		return 0;

	SCCDT_CA_OPPVEntitle* pParam = (SCCDT_CA_OPPVEntitle*)data;

	jclass cls = env->GetObjectClass(gObject);

	jobject objout = env->NewObject(cls, init);

	env->SetIntField(objout, event_id, pParam->event_id);
	env->SetIntField(objout, start_time, pParam->start_time);
	env->SetIntField(objout, end_time, pParam->end_time);

	env->DeleteLocalRef(cls);

	return objout;
}
