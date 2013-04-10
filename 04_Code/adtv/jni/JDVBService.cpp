#include "JDVBService.h"

extern jclass gClsString;
extern jmethodID gMedStringConID;
extern jobject gObjStrEncoding;

jstring CreateJstringFromGB2312(JNIEnv * env, const char * pStr);

bool JDVBService::InitFieldIDs(JNIEnv * env, jclass service, JDVBService & fieldids)
{
	fieldids.sid_field = env->GetFieldID(service, "sid", "I");
	fieldids.cnum_field = env->GetFieldID(service, "channel_number", "I");
	fieldids.sname_field = env->GetFieldID(service, "name","Ljava/lang/String;");
	fieldids.service_type_field = env->GetFieldID(service, "service_type","I");
	fieldids.reserved1_field = env->GetFieldID(service, "reserved1","I");
	fieldids.category_field = env->GetFieldID(service, "category","I");
	fieldids.pcr_pid_field = env->GetFieldID(service, "pcr_pid","I");
	//fieldids.reserved3_field = env->GetFieldID(service, "reserved3","I"); //change to emm_pid
	fieldids.emmpid_field = env->GetFieldID(service, "emm_pid","I");
	fieldids.pmt_id_field = env->GetFieldID(service, "pmt_id","I");
	//fieldids.volume_comp_field = env->GetFieldID(service, "volume_comp","Ljava/lang/String;");
	fieldids.volume_reserve_field = env->GetFieldID(service, "volume_reserve","I");
	fieldids.audio_channel_set_field = env->GetFieldID(service, "audio_channel_set","I");
	fieldids.audio_format_field = env->GetFieldID(service, "audio_format","I");
	fieldids.audio_index_field = env->GetFieldID(service, "audio_index","I");

	fieldids.video_stream_type_field = env->GetFieldID(service, "video_stream_type","I");
	fieldids.video_stream_pid_field = env->GetFieldID(service, "video_stream_pid","I");
	fieldids.video_ecm_pid_field = env->GetFieldID(service, "video_ecm_pid","I");
	fieldids.ts_id_field = env->GetFieldID(service, "ts_id","I");
	fieldids.net_id_field = env->GetFieldID(service, "net_id","I");
	fieldids.audio_stream_type_field = env->GetFieldID(service, "audio_stream_type","I");
	fieldids.audio_stream_pid_field = env->GetFieldID(service, "audio_stream_pid","I");
	fieldids.audio_ecm_pid_field = env->GetFieldID(service, "audio_ecm_pid","I");
	fieldids.audio_stream_name_field = env->GetFieldID(service, "audio_stream_name","Ljava/lang/String;");

	fieldids.audio_stream_type1_field = env->GetFieldID(service, "audio_stream_type1","I");
	fieldids.audio_stream_pid1_field = env->GetFieldID(service, "audio_stream_pid1","I");
	fieldids.audio_ecm_pid1_field = env->GetFieldID(service, "audio_ecm_pid1","I");
	fieldids.audio_stream_name1_field = env->GetFieldID(service, "audio_stream_name1","Ljava/lang/String;");

	fieldids.audio_stream_type2_field = env->GetFieldID(service, "audio_stream_type2","I");
	fieldids.audio_stream_pid2_field = env->GetFieldID(service, "audio_stream_pid2","I");
	fieldids.audio_ecm_pid2_field = env->GetFieldID(service, "audio_ecm_pid2","I");
	fieldids.audio_stream_name2_field = env->GetFieldID(service, "audio_stream_name2","Ljava/lang/String;");
	fieldids.batVersion_field = env->GetFieldID(service, "batVersion", "I");
	fieldids.nitVersion_field = env->GetFieldID(service, "nitVersion", "I");
	//tuning
	fieldids.freqID = env->GetFieldID(service, "Frequency","I");
	fieldids.symbolID = env->GetFieldID(service, "SymbolRate","I");
	fieldids.modulationID = env->GetFieldID(service, "Modulation","I");
	return true;
}

bool JDVBService::ConvertObject(JNIEnv * env, JDVBService & fieldids, jobject & objService, DVBService & service)
{

	env->SetIntField(objService,fieldids.sid_field,service.serviceID);
	env->SetIntField(objService,fieldids.cnum_field,service.channel_number);

	jstring serviceName = CreateJstringFromGB2312(env, service.name);
	env->SetObjectField(objService,fieldids.sname_field,serviceName);
	env->DeleteLocalRef(serviceName);

	env->SetIntField(objService,fieldids.service_type_field,service.service_type);
	env->SetIntField(objService,fieldids.reserved1_field,service.ca_mode);
	env->SetIntField(objService,fieldids.category_field,service.category);
	env->SetIntField(objService,fieldids.pcr_pid_field,service.pcr_pid);
	env->SetIntField(objService,fieldids.emmpid_field,service.emm_pid);
	env->SetIntField(objService,fieldids.pmt_id_field,service.pmt_id);
	env->SetIntField(objService,fieldids.volume_reserve_field,service.volume_reserve);
	env->SetIntField(objService,fieldids.audio_channel_set_field,service.audio_channel_set);
	env->SetIntField(objService,fieldids.audio_format_field,service.audio_format);
	env->SetIntField(objService,fieldids.audio_index_field,service.audio_index);
	env->SetIntField(objService,fieldids.video_stream_type_field,service.video_stream.stream_type);
	env->SetIntField(objService,fieldids.video_stream_pid_field,service.video_stream.stream_pid);
	env->SetIntField(objService,fieldids.video_ecm_pid_field,service.video_stream.ecm_pid);
	env->SetIntField(objService,fieldids.ts_id_field,service.ts.ts_id);
	env->SetIntField(objService,fieldids.net_id_field,service.ts.net_id);
	env->SetIntField(objService,fieldids.nitVersion_field,service.nitVersion);
	env->SetIntField(objService,fieldids.batVersion_field,service.batVersion);

	env->SetIntField(objService,fieldids.audio_stream_type_field,service.audio_stream[0].stream_type);
	env->SetIntField(objService,fieldids.audio_stream_pid_field,service.audio_stream[0].stream_pid);
	env->SetIntField(objService,fieldids.audio_ecm_pid_field,service.audio_stream[0].ecm_pid);
	jstring audioName; 
	audioName = CreateJstringFromGB2312(env, service.audio_stream[0].name);
	env->SetObjectField(objService, fieldids.audio_stream_name_field,audioName);
	env->DeleteLocalRef(audioName);


	env->SetIntField(objService,fieldids.audio_stream_type1_field,service.audio_stream[1].stream_type);
	env->SetIntField(objService,fieldids.audio_stream_pid1_field,service.audio_stream[1].stream_pid);
	env->SetIntField(objService,fieldids.audio_ecm_pid1_field,service.audio_stream[1].ecm_pid);
	jstring audioName1;
	audioName1 = CreateJstringFromGB2312(env, service.audio_stream[1].name);
	env->SetObjectField(objService, fieldids.audio_stream_name1_field,audioName1);
	env->DeleteLocalRef(audioName1);

	env->SetIntField(objService,fieldids.audio_stream_type2_field,service.audio_stream[2].stream_type);
	env->SetIntField(objService,fieldids.audio_stream_pid2_field,service.audio_stream[2].stream_pid);
	env->SetIntField(objService,fieldids.audio_ecm_pid2_field,service.audio_stream[2].ecm_pid);
	jstring audioName2;
	audioName2 = CreateJstringFromGB2312(env, service.audio_stream[2].name);
	env->SetObjectField(objService, fieldids.audio_stream_name2_field,audioName2);
	env->DeleteLocalRef(audioName2);

	env->SetIntField(objService,fieldids.freqID,service.ts.tuning_param.freq);
	env->SetIntField(objService,fieldids.symbolID,service.ts.tuning_param.symb);
	env->SetIntField(objService,fieldids.modulationID,service.ts.tuning_param.qam);
	return true;
}
