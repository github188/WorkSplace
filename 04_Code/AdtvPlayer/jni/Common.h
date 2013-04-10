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
	//注意一定要与SetJavaVM方法中的顺序一致
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
	jfieldID	ServiceIdent;		/* 节目标识号 */
	jfieldID	ServiceName;		/* 节目名称 */
	jfieldID	ServiceType;		/* 节目类型，SDT表的service描述符中定义的节目类型码，可能根据要求在搜索时进行了类型变换。无效值为0xFF */
	jfieldID	ServiceOrgType; 	/* 原始节目类型，SDT表的service描述符中定义的原始节目类型码。无效值为0xFF */
	jfieldID	ProviderName;		/* 内容供应商名称 */

	jfieldID	VideoTrack; 		/* 视频流情况。其中的EcmPid字段没有使用 */
	jfieldID	AudioTrack;			/* 音频流情况。其中的EcmPid字段没有使用 */
	jfieldID	PcrPid; 			/* pcr pid */

	jfieldID	Teletext;			/* 图文流描述 */
	jfieldID	Subtitle;			/* 字幕流描述 */

	jfieldID	CaSystemId;			/* CA系统号，用于同密 */
	jfieldID	CaEcmPid; 			/* CA系统对应的ECMpid，用于同密 */

	jfieldID	LogicChNumber;		/* 逻辑频道号。无效值为0xFFFF */
	jfieldID	NvodTimeshiftServices;	/* 如果是参考业务，表示其对于的时移业务集合 */
	jfieldID	PmtPid; 			/* 该节目的pmt pid。无效值为0x1FFF */
	jfieldID	PmtVersion; 		/* 该节目的pmt表版本号。无效值为0xFFFFFFFF */
	jfieldID	VolBalance; 		/* 声道模式。无效值为0xFF */
	jfieldID	VolCompensation;	/* 音量补偿。无效值为0xFF */
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
	jfieldID	EcmPid;		/* video ecm pid，没有则置无效值0x1FFF */
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
	jfieldID	EcmPid;		/* audio ecm pid，没有则置无效值0x1FFF */
	jfieldID	PesType;	/* audio stream type, descripte in pmt */
	jfieldID	LangCode;	/* audio stream language code(ISO639-2) */
	jfieldID	channelType;	/* 声道模式*/
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
	jfieldID		StreamPid;		/* stream pid，无效值设置0x1FFF */
	jfieldID		EcmPid; 		/* stream ecm pid，没有则置无效值0x1FFF */
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
	jfieldID	bDecrypt;			/* 对加扰节目:是录制解扰流还是录制加扰流! TRUE:录制解扰流；FALSE:录制仍加扰的流 */
	jfieldID	VideoTrack;			/* 视频 */
	jfieldID	AudioTrack;			/* 音轨 */
	jfieldID	AudioTrackNum;		/* 音轨数目 */
	jfieldID	AudioPlayIndex;		/* 回放的默认音轨 */
	jfieldID	PcrPid;				/* pcr pid, descripte in pmt，一般无需过滤。如果不过滤，设置无效值0x1FFF */
	jfieldID	SubtitlePid;		/* subtitle pid */
	jfieldID	TeletextPid;		/* teletext pid */
	jfieldID	OtherPid;			/* 用户要求的伴随录制的其他pid流 */
	jfieldID	OtherPidNum;		/* 用户要求的伴随录制的其他pid流的数目 */
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
	jfieldID 	m_pPvrDirPath;		// 在析构时，如果该参数不为NULL，则将被自动释放掉(通过delete方法)
	jfieldID	m_Transponder;
	jfieldID	m_streamInfo;
	jfieldID	m_bTimeshift;		// 是否是时移录制
	jfieldID	m_recSizeLimitMB;	// 录制文件大小限制，单位MByte。无限制为-1
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
	jfieldID    outputWinId;	/* 视频输出 */
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
	jfieldID    m_pPvrDirPath; 	// 在析构时，如果该参数不为NULL，则将被自动释放掉(通过delete方法)
	jfieldID	m_bTimeshift;		// 是否是时移录制
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
	jfieldID	m_startTimeMs;		// 开始回放的时间位置。单位milli-second
	jfieldID	m_speed;			// 回放速度。
	jfieldID	m_audioLanguage;	// 音频语言码，内部会尽量播放匹配的音频。ISO639-2，比如法语为: [0]='f' [1]='r' [2]='e' [3]='\0'
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
	jfieldID	tpIndexOfList;		/* 当前搜索频点在当前搜索列表中的序号 */
	jfieldID	tpTotalNumOfList;	/* 当前搜索列表中待搜的频点总数 */
	jfieldID	tpTotalNumOfAll;		/* 所有列表中待搜索的频点总数 */
	jfieldID	frequencyKHz;		/* 当前频点的调频频率，单位KHz */
	jfieldID	symbolRateHz;		/* 当前频点的调频符号率，单位symbol/second */
	jfieldID	modulation;			/* 当前频点的调频方式 */
	jfieldID	listIndex;			/* 当前搜索列表的序号 */
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
	jfieldID	SignalStrength;		/* 信号强度	*/
	jfieldID	SignalQuality;		/* 信号质量	*/
	jfieldID	BitErrorRate;			/* 误码率		*/
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
	jfieldID	NetworkId;			/* 网络ID */
	jfieldID	NetworkName;		/* 网络名称 */
	jfieldID	NitVersion;			/* NIT version_number。无效值为0xFFFFFFFF */
	jfieldID	TotalTsNum; 		/* 总频点数目 */
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
	jfieldID	tsId;			/* 传输流ID。无效值0xFFFF */
	jfieldID	networkId;		/* 网络ID。无效值0xFFFF */
	jfieldID	orgNetworkId;	/* 原始网络ID。无效值0xFFFF */
	jfieldID	patVersion;		/* PAT version_number。无效值为0xFFFFFFFF */
	jfieldID	sdtVersion;		/* BAT version_number。无效值为0xFFFFFFFF */
	jfieldID	serviceNum;		/* 当前频点下可能节目数目。因为节目类型过滤等动作，可能实际滤出的节目小于该数目 */
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
    jfieldID		bAutoFixPcrPid;	/* 当PcrPid无效的时候(比如0x1FFE)，是否自动修正到视频或音频PID */
};

#endif // COMMON_H
