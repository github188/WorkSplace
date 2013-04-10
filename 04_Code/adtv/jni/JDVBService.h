#ifndef __JNI_JDVBService_H__
#define __JNI_JDVBService_H__

#include <jni.h>
#include <tvcomm.h>

struct JDVBService
{
	jfieldID sid_field;
    jfieldID cnum_field;
	jfieldID sname_field;
	jfieldID service_type_field;
	jfieldID reserved1_field;
	jfieldID category_field;
	jfieldID reserved2_field;
	jfieldID pcr_pid_field;
	jfieldID emmpid_field;
	jfieldID pmt_id_field;
	jfieldID reserved4_field;
	jfieldID volume_reserve_field;
	jfieldID audio_channel_set_field;
	jfieldID audio_format_field;
	jfieldID audio_index_field;
	jfieldID video_stream_type_field;
	jfieldID video_stream_pid_field;
	jfieldID video_ecm_pid_field;
	jfieldID ts_id_field;
	jfieldID net_id_field;
	jfieldID audio_stream_type_field;
	jfieldID audio_stream_pid_field;
	jfieldID audio_ecm_pid_field;
	jfieldID audio_stream_name_field;

	jfieldID audio_stream_type1_field;
	jfieldID audio_stream_pid1_field;
	jfieldID audio_ecm_pid1_field;
	jfieldID audio_stream_name1_field;
	jfieldID audio_stream_type2_field;
	jfieldID audio_stream_pid2_field;
	jfieldID audio_ecm_pid2_field;
	jfieldID audio_stream_name2_field;

	jfieldID batVersion_field;
	jfieldID nitVersion_field;
	//tuning
	jfieldID freqID;
	jfieldID symbolID;
	jfieldID modulationID;

	static bool InitFieldIDs(JNIEnv * env, jclass service, JDVBService & fieldids);
	static bool ConvertObject(JNIEnv * env, JDVBService & fieldids, jobject & objService, DVBService & service);
};

#endif//__JNI_JDVBService_H__
