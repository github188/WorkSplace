/*
***************************************************************************************************
*  FileName    : Common.h
*  Author      : dr      Date: 2012-03-09
*  Description :
*--------------------------------------------------------------------------------------------------
*  History     :
*  <time>        <version >   <author>   	<desc>
*                V1.0.0               
*
***************************************************************************************************
*/

#ifndef COMMON_H
#define COMMON_H

#include <jni.h>
#include <android/log.h>
#include <stdio.h>

#using namespace std;

#include <string.h>

/* debug flags */
#define JNI_DEBUG_DATA_CONVERT		TRUE




class JniTransponder;
class JniChannel;
class JniIntObject;

uint8 PSI_CnvBcdToVal( uint8  Bcd );

using namespace std;

class JniObject{
public:
	JniObject();
	virtual ~JniObject();

    virtual void initObject(JNIEnv * env) = 0;
	virtual string objName() = 0;
	jclass getJClass(JNIEnv* env);

	virtual jobject createNewObject(JNIEnv * env, void * pData);
	virtual void toCppObject(JNIEnv* env, jobject javaobj, void* outCppObj);

	virtual void setCppDataToJava(JNIEnv* env, jobject javaObj, void* cppObj);
	virtual void setJavaDataToCpp(JNIEnv* env, jobject javaObj, void* cppObj);
protected:
	jobject gObject;

	jmethodID init;
};

class JniCommon{
public:
	//ע��һ��Ҫ��SetJavaVM�����е�˳��һ��
	enum eJniObjectType { //JniObject
		JniObjectType_Int,
		JniObjectType_Char,
		JniObjectType_Transponder,
		JniObjectType_TunerLockParam,
		JniObjectType_stChannel,
		JniObjectType_stServiceIdent,
		JniObjectType_Date,
		JniObjectType_LoaderState,
		JniObjectType_EITEvent,
		JniObjectType_stEITEvent,
		JniObjectType_stEitEventExtDesc,
		JniObjectType_NetworkInfo,
		JniObjectType_DVBDateTime,
		JniObjectType_stVideoTrack,
		JniObjectType_stAudioTrack,
		JniObjectType_stPidStream,
		JniObjectType_IPvrStreamInfo,
		JniObjectType_IPvrRecOpenParam,
		JniObjectType_IPvrRecStartParam,
		JniObjectType_IPvrPbOpenParam,
		JniObjectType_IPvrPbConfigParam,
		JniObjectType_IPvrPbStartParam,
		JniObjectType_IPSearchTp,
		JniObjectType_IPSearchList,
		JniObjectType_IPSearchOpenParam,
		JniObjectType_IPSearchCBParam_List,
		JniObjectType_IPSearchCBParam_Tp,
		JniObjectType_stTunerState,
		JniObjectType_stNetwork,
		JniObjectType_IPSearchCBParam_TpInfo,
		JniObjectType_IPPlayOpenParam,
		JniObjectType_IPPlayStartParam,

	};

public:
	static jstring CharTojstring(JNIEnv *env, const char *str, int len, const char *charCode = "gbk");
	static jstring CharTojstring(JNIEnv *env,  stTextContent & text);
	static string GetStringFromJava(JNIEnv* env, jstring str);
	static void SetJavaVM(JavaVM * jvm);
	static JNIEnv * GetCurrentEnv();
	static void CurrentThreadExit();
	static JniObject * GetCommonObject(eJniObjectType type);
	static int GetGlobalObjByName(JNIEnv* env, jobject& obj, const char* szString);

	static int GetYMDByMJD(uint16 nMJD, int* pYear, int* pMonth, int* pDay);
	static int GetHMSByBCD(uint32 nBCD, int* pHour, int* pMinute, int* pSecond);

private:
	static JavaVM * jvm;
	static map<eJniObjectType, JniObject*> jniObjects;
};

class JniIntObject : public JniObject{
public:
    JniIntObject();
    ~JniIntObject();

    void initObject(JNIEnv * env);
	string objName(){return "JniIntObject";}

    jobject createNewObject(JNIEnv * env, void * pData);
	void setCppDataToJava(JNIEnv* env, jobject javaObj, void* cppObj);
private:
    jfieldID value;
};

class JniCharObject : public JniObject{
public:
	JniCharObject();
	~JniCharObject();

	void initObject(JNIEnv * env);
	string objName(){return "JniIntObject";}

	jobject createNewObject(JNIEnv * env, void * pData);
	void setCppDataToJava(JNIEnv* env, jobject javaObj, void* cppObj);
private:
	jfieldID value;
};

class JniTransponder : public JniObject{
public:
    JniTransponder();
    ~JniTransponder();

    void initObject(JNIEnv * env);
	string objName(){return "JniTransponder";}

    jobject createNewObject(JNIEnv * env, void * data);
	void toCppObject(JNIEnv *env, jobject javaobj, void *outCppObj);
private:
    jfieldID frequency;
    jfieldID modulation;
    jfieldID symbolRate;
    jfieldID nPATVersion;
    jfieldID nSDTVersion;
    jfieldID nCATVersion;
    jfieldID nNITVersion;
};


class JniTunerLockParam : public JniObject
{
public:
	JniTunerLockParam();
	~JniTunerLockParam();

	void initObject(JNIEnv * env);
	string objName(){return "JniTunerLockParam";}

	jobject createNewObject(JNIEnv * env, void * data);
	void toCppObject(JNIEnv *env, jobject javaobj, void *outCppObj);
	
private:
	jfieldID tpParam;
	jfieldID bUseLnb;
	jfieldID lnbParam;
};


class JniChannel : public JniObject{
public:
    JniChannel();
    ~JniChannel();

    void initObject(JNIEnv * env);
	string objName(){return "JniChannel";}

    jobject createNewObject(JNIEnv * env, void * pData);
	void toCppObject(JNIEnv* env, jobject javaobj, void* outCppObj);
private:
	jfieldID	ServiceIdent;		/* ��Ŀ��ʶ�� */
	jfieldID	ServiceName;		/* ��Ŀ���� */
	jfieldID	ServiceType;		/* ��Ŀ���ͣ�SDT���service�������ж���Ľ�Ŀ�����룬���ܸ���Ҫ��������ʱ���������ͱ任����ЧֵΪ0xFF */
	jfieldID	ServiceOrgType; 	/* ԭʼ��Ŀ���ͣ�SDT���service�������ж����ԭʼ��Ŀ�����롣��ЧֵΪ0xFF */
	jfieldID	ProviderName;		/* ���ݹ�Ӧ������ */

	jfieldID	VideoTrack; 		/* ��Ƶ����������е�EcmPid�ֶ�û��ʹ�� */
	jfieldID	AudioTrack;			/* ��Ƶ����������е�EcmPid�ֶ�û��ʹ�� */
	jfieldID	PcrPid; 			/* pcr pid */

	jfieldID	Teletext;			/* ͼ�������� */
	jfieldID	Subtitle;			/* ��Ļ������ */

	jfieldID	CaSystemId;			/* CAϵͳ�ţ�����ͬ�� */
	jfieldID	CaEcmPid; 			/* CAϵͳ��Ӧ��ECMpid������ͬ�� */

	jfieldID	LogicChNumber;		/* �߼�Ƶ���š���ЧֵΪ0xFFFF */
	jfieldID	NvodTimeshiftServices;	/* ����ǲο�ҵ�񣬱�ʾ����ڵ�ʱ��ҵ�񼯺� */
	jfieldID	PmtPid; 			/* �ý�Ŀ��pmt pid����ЧֵΪ0x1FFF */
	jfieldID	PmtVersion; 		/* �ý�Ŀ��pmt��汾�š���ЧֵΪ0xFFFFFFFF */
	jfieldID	VolBalance; 		/* ����ģʽ����ЧֵΪ0xFF */
	jfieldID	VolCompensation;	/* ������������ЧֵΪ0xFF */
};

class JniLoaderState : public JniObject{
public:
    JniLoaderState();
    ~JniLoaderState();

    void initObject(JNIEnv * env);
	string objName(){return "JniLoaderState";}

    jobject createNewObject(JNIEnv * env, void * data);

    void toCPPLoaderInfo(JNIEnv * env,jobject loaobj, LoaderArgument * outLoaderArg);
private:
    jfieldID Serviceid;
    jfieldID stream_id;
    jfieldID network_id;
    jfieldID linkage_type;
    jfieldID manufacture_code;
    jfieldID hardware_version;
    jfieldID software_version;
    jfieldID serial_number_start;
    jfieldID serial_number_end;
    jfieldID Type;
};

class JniServiceIdent : public JniObject{
public:
    JniServiceIdent();
    ~JniServiceIdent();

    void initObject(JNIEnv * env);
	string objName(){return "JniServiceIdent";}

    jobject createNewObject(JNIEnv * env, void * pData);
	void toCppObject(JNIEnv *env, jobject javaobj, void *outCppObj);

private:
	jfieldID ServiceId;
	jfieldID TsId;
	jfieldID OrgNetId;
};

class JniDate : public JniObject {
public:
    JniDate();
    ~JniDate();
    void initObject(JNIEnv * env);
	string objName(){return "JniDate";}

    jobject createNewObject(JNIEnv *env, int year, int month, int day,
                            int hour, int minute, int second);
private:
    jmethodID UTC;
};

class JnistEitEvent : public JniObject {
public:
    JnistEitEvent();
    ~JnistEitEvent();

    void initObject(JNIEnv * env);
	string objName(){return "JnistEitEvent";}

	jobject createNewObject(JNIEnv *env, void* pData);

private:
	jfieldID		ServiceIdent;
   	jfieldID        EventId;			/* event id */
	jfieldID		StartMjd;			/* start time mjd (day from start of 1900) */
	jfieldID		StartUtc;			/* start time of h:m:s\u3002(BCD\u5f62\u5f0f\uff0c\u4f4e3\u4f4d\u5206\u522b\u4e3a\u65f6\u5206\u79d2\u7684BCD\u7801) */
	jfieldID		DurationUtc;			/* duration time of h:m:s\u3002(BCD\u5f62\u5f0f\uff0c\u4f4e3\u4f4d\u5206\u522b\u4e3a\u65f6\u5206\u79d2\u7684BCD\u7801) */
	jfieldID		EventName;			/* event name */
	jfieldID		Text;				/* event short describe */

    jfieldID		EventType;			/* event type. value meanings [0]: P\uff1b[1]: F\uff1b[2]: Sch */
    jfieldID		parentRating;		/* parent rating code\uff0cmatched local country code */
		
	jfieldID		bTimeshiftEvent;		/* is a timeshift event? */
    jfieldID		refServiceId;			/* reference service id if it's a timeshift event */
    jfieldID		refEventId;
};
class JnistEitEventExtDesc : public JniObject {
public:
	JnistEitEventExtDesc();
	~JnistEitEventExtDesc();
	void initObject(JNIEnv * env);
	string objName(){return "JnistEitEventExtDesc";}
	jobject createNewObject(JNIEnv *env, void* pData);

private:
	jfieldID EventId;
	jfieldID Text;

};

class JniDVBDateTime: public JniObject
{
public:
    JniDVBDateTime();
    ~JniDVBDateTime();

    void initObject(JNIEnv * env);
	string objName(){return "JniDVBDateTime";}
    jobject createNewObject(JNIEnv * env, uint16 nMJD, uint32 nBCD);

    jobject createNewObject(JNIEnv * env, void * pData);

private:
    jfieldID    YearFiledId;
    jfieldID    MonthFiledId;
    jfieldID    DayFiledId;
    jfieldID    HourFiledId;
    jfieldID    MinuteFiledId;
    jfieldID    SecondFiledId;
};

class JnistVideoTrack: public JniObject
{
public:
	JnistVideoTrack();
	~JnistVideoTrack();

	void initObject(JNIEnv *env);
	string objName(){return "JnistVideoTrack";}
	jobject createNewObject(JNIEnv *env, void *pData);
	void toCppObject(JNIEnv *env, jobject javaobj, void *outCppObj);
private:
	jfieldID	StreamPid;	/* video pid, descripte in pmt */
	jfieldID	EcmPid;		/* video ecm pid��û��������Чֵ0x1FFF */
	jfieldID	PesType;	/* video stream type, descripte in pmt */
};

class JnistAudioTrack: public JniObject
{
public:
	JnistAudioTrack();
	~JnistAudioTrack();

	void initObject(JNIEnv *env);
	string objName(){return "JnistAudioTrack";}
	jobject createNewObject(JNIEnv *env, void *pData);
	void toCppObject(JNIEnv *env, jobject javaobj, void *outCppObj);
private:
	jfieldID	StreamPid;	/* audio pid, descripte in pmt */
	jfieldID	EcmPid;		/* audio ecm pid��û��������Чֵ0x1FFF */
	jfieldID	PesType;	/* audio stream type, descripte in pmt */
	jfieldID	LangCode;	/* audio stream language code(ISO639-2) */
	jfieldID	channelType;	/* ����ģʽ*/
};

class JnistPidStream: public JniObject
{
public:
	JnistPidStream();
	~JnistPidStream();

	string objName(){return "JnistPidStream";}
	void initObject(JNIEnv *env);
	jobject createNewObject(JNIEnv *env, void *pData);
	void toCppObject(JNIEnv *env, jobject javaobj, void *outCppObj);
private:
	jfieldID		StreamPid;		/* stream pid����Чֵ����0x1FFF */
	jfieldID		EcmPid; 		/* stream ecm pid��û��������Чֵ0x1FFF */
	jfieldID		PesType;		/* stream pes type, descripte in pmt */
	jfieldID		StreamDesc;		/* stream description, define & used by user */
};


class JniIPvrStreamInfo: public JniObject
{
public:
	JniIPvrStreamInfo();
	~JniIPvrStreamInfo();

	string objName(){return "JniIPvrStreamInfo";}
	void initObject(JNIEnv *env);
	void toCppObject(JNIEnv *env, jobject javaobj, void *outCppObj);
private:
	jfieldID	bDecrypt;			/* �Լ��Ž�Ŀ:��¼�ƽ���������¼�Ƽ�����! TRUE:¼�ƽ�������FALSE:¼���Լ��ŵ��� */
	jfieldID	VideoTrack;			/* ��Ƶ */
	jfieldID	AudioTrack;			/* ���� */
	jfieldID	AudioTrackNum;		/* ������Ŀ */
	jfieldID	AudioPlayIndex;		/* �طŵ�Ĭ������ */
	jfieldID	PcrPid;				/* pcr pid, descripte in pmt��һ��������ˡ���������ˣ�������Чֵ0x1FFF */
	jfieldID	SubtitlePid;		/* subtitle pid */
	jfieldID	TeletextPid;		/* teletext pid */
	jfieldID	OtherPid;			/* �û�Ҫ��İ���¼�Ƶ�����pid�� */
	jfieldID	OtherPidNum;		/* �û�Ҫ��İ���¼�Ƶ�����pid������Ŀ */
};

class JniIPvrRecOpenParam: public JniObject
{
public:
	JniIPvrRecOpenParam();
	~JniIPvrRecOpenParam();

	string objName(){return "JniIPvrRecOpenParam";}
	void initObject(JNIEnv *env);
	void toCppObject(JNIEnv* env, jobject javaobj, void* outCppObj);
private:
	jfieldID 	m_pPvrDirPath;		// ������ʱ������ò�����ΪNULL���򽫱��Զ��ͷŵ�(ͨ��delete����)
	jfieldID	m_Transponder;
	jfieldID	m_streamInfo;
	jfieldID	m_bTimeshift;		// �Ƿ���ʱ��¼��
	jfieldID	m_recSizeLimitMB;	// ¼���ļ���С���ƣ���λMByte��������Ϊ-1
};

class JniIPvrRecStartParam: public JniObject
{
public:
	JniIPvrRecStartParam();
	~JniIPvrRecStartParam();

	string objName(){return "JniIPvrRecStartParam";}
	void initObject(JNIEnv *env);
	void toCppObject(JNIEnv* env, jobject javaobj, void* outCppObj);
private:
};

class JniIPvrPbConfigParam: public JniObject
{
public:
	JniIPvrPbConfigParam();
	~JniIPvrPbConfigParam();

	string objName(){return "JniIPvrPbConfigParam";}
	void initObject(JNIEnv *env);
	void toCppObject(JNIEnv* env, jobject javaobj, void* outCppObj);
	void setCppDataToJava(JNIEnv *env, jobject javaObj, void *cppObj);
private:
	jfieldID    outputWinId;	/* ��Ƶ��� */
};

class JniIPvrPbOpenParam: public JniObject
{
public:
	JniIPvrPbOpenParam();
	~JniIPvrPbOpenParam();

	string objName(){return "JniIPvrPbOpenParam";}
	void initObject(JNIEnv *env);
	void toCppObject(JNIEnv* env, jobject javaobj, void* outCppObj);
private:
	jfieldID    m_pPvrDirPath; 	// ������ʱ������ò�����ΪNULL���򽫱��Զ��ͷŵ�(ͨ��delete����)
	jfieldID	m_bTimeshift;		// �Ƿ���ʱ��¼��
	jfieldID	m_configParam;
};

class JniIPvrPbStartParam: public JniObject
{
public:
	JniIPvrPbStartParam();
	~JniIPvrPbStartParam();

	string objName(){return "JniIPvrPbStartParam";}
	void initObject(JNIEnv *env);
	void toCppObject(JNIEnv* env, jobject javaobj, void* outCppObj);
private:
	jfieldID	m_startTimeMs;		// ��ʼ�طŵ�ʱ��λ�á���λmilli-second
	jfieldID	m_speed;			// �ط��ٶȡ�
	jfieldID	m_audioLanguage;	// ��Ƶ�����룬�ڲ��ᾡ������ƥ�����Ƶ��ISO639-2�����編��Ϊ: [0]='f' [1]='r' [2]='e' [3]='\0'
};

class JniIPSearchTp: public JniObject
{
public:
	JniIPSearchTp();
	~JniIPSearchTp();

	string objName(){return "JniIPSearchTp";}
	void initObject(JNIEnv *env);
	void toCppObject(JNIEnv* env, jobject javaobj, void* outCppObj);
private:
	jfieldID 	tpParam;
	jfieldID 	bSearchNit;
	jfieldID	bSearchBat;
};


class JniIPSearchList: public JniObject
{
public:
	JniIPSearchList();
	~JniIPSearchList();

	string objName(){return "JniIPSearchList";}
	void initObject(JNIEnv *env);
	void toCppObject(JNIEnv *env, jobject javaobj, void *outCppObj);
private:
	jfieldID 	tpList;
	jfieldID 	bUseLnb;
	jfieldID	lnb;
};

class JniIPSearchOpenParam: public JniObject
{
public:
	JniIPSearchOpenParam();
	~JniIPSearchOpenParam();

	string objName(){return "JniIPSearchOpenParam";}
	void initObject(JNIEnv *env);
	void toCppObject(JNIEnv *env, jobject javaobj, void *outCppObj);
private:
	jfieldID 	searchList;
	jfieldID 	ValidNetId;
	jfieldID	ValidServiceType;
	jfieldID	bServiceTypeAdjustByElement;
	jfieldID	bTsidByPATOtherwiseSDT;
	jfieldID	bSearchServiceFTA;
	jfieldID	bSearchServiceScramble;
	jfieldID	bDirectSearchServiceAfterNitFailed;
};

class JniIPSearchCBParam_List: public JniObject
{
public:
	JniIPSearchCBParam_List();
	~JniIPSearchCBParam_List();

	string objName(){return "JniIPSearchCBParam_List";}
	void initObject(JNIEnv *env);
	jobject createNewObject(JNIEnv *env, void *pData);
private:
	jfieldID	listIndex;
	jfieldID	listTotalNum;
};

class JniIPSearchCBParam_Tp: public JniObject
{
public:
	JniIPSearchCBParam_Tp();
	~JniIPSearchCBParam_Tp();

	string objName(){return "JniIPSearchCBParam_Tp";}
	void initObject(JNIEnv *env);
	jobject createNewObject(JNIEnv *env, void *pData);
private:
	jfieldID	tpIndexOfList;		/* ��ǰ����Ƶ���ڵ�ǰ�����б��е���� */
	jfieldID	tpTotalNumOfList;	/* ��ǰ�����б��д��ѵ�Ƶ������ */
	jfieldID	tpTotalNumOfAll;		/* �����б��д�������Ƶ������ */
	jfieldID	frequencyKHz;		/* ��ǰƵ��ĵ�ƵƵ�ʣ���λKHz */
	jfieldID	symbolRateHz;		/* ��ǰƵ��ĵ�Ƶ�����ʣ���λsymbol/second */
	jfieldID	modulation;			/* ��ǰƵ��ĵ�Ƶ��ʽ */
	jfieldID	listIndex;			/* ��ǰ�����б����� */
};

class JnistTunerState: public JniObject
{
public:
	JnistTunerState();
	~JnistTunerState();

	string objName(){return "JnistTunerState";}
	void initObject(JNIEnv *env);
	jobject createNewObject(JNIEnv *env, void *pData);
private:
	jfieldID	LockState;//DD hardware state
	jfieldID	Frequency;
	jfieldID	SymbolRate;
	jfieldID	Modulation;
	jfieldID	SignalStrength;		/* �ź�ǿ��	*/
	jfieldID	SignalQuality;		/* �ź�����	*/
	jfieldID	BitErrorRate;			/* ������		*/
};

class JnistNetwork: public JniObject
{
public:
	JnistNetwork();
	~JnistNetwork();

	string objName(){return "JnistNetwork";}
	void initObject(JNIEnv *env);
	jobject createNewObject(JNIEnv *env, void *pData);
private:
	jfieldID	NetworkId;			/* ����ID */
	jfieldID	NetworkName;		/* �������� */
	jfieldID	NitVersion;			/* NIT version_number����ЧֵΪ0xFFFFFFFF */
	jfieldID	TotalTsNum; 		/* ��Ƶ����Ŀ */
};

class JniIPSearchCBParam_TpInfo: public JniObject
{
public:
	JniIPSearchCBParam_TpInfo();
	~JniIPSearchCBParam_TpInfo();

	string objName(){return "JniIPSearchCBParam_TpInfo";}
	void initObject(JNIEnv *env);
	jobject createNewObject(JNIEnv *env, void *pData);
private:
	jfieldID	tsId;			/* ������ID����Чֵ0xFFFF */
	jfieldID	networkId;		/* ����ID����Чֵ0xFFFF */
	jfieldID	orgNetworkId;	/* ԭʼ����ID����Чֵ0xFFFF */
	jfieldID	patVersion;		/* PAT version_number����ЧֵΪ0xFFFFFFFF */
	jfieldID	sdtVersion;		/* BAT version_number����ЧֵΪ0xFFFFFFFF */
	jfieldID	serviceNum;		/* ��ǰƵ���¿��ܽ�Ŀ��Ŀ����Ϊ��Ŀ���͹��˵ȶ���������ʵ���˳��Ľ�ĿС�ڸ���Ŀ */
};

class JniIPPlayOpenParam: public JniObject
{
public:
    JniIPPlayOpenParam();
    ~JniIPPlayOpenParam();

    string objName(){return "JniIPPlayOpenParam";}
    void initObject(JNIEnv *env);

    void toCppObject(JNIEnv *env, jobject javaobj, void *outCppObj);
private:
    jfieldID            livePlayMode;	/* [NOTICE] each mode can only be opened once!!! */
    jfieldID		bAutoFixPcrPid;	/* ��PcrPid��Ч��ʱ��(����0x1FFE)���Ƿ��Զ���������Ƶ����ƵPID */
};

#endif // COMMON_H
