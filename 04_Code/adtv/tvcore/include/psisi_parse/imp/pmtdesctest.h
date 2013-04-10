//liyuequan 2005-3-10  创建
//liyuequan 2005-3-16  修改video stream
//                     修改AC3
//zqj       2005-9-23  添加vod描述符

//vod_descriptor
inline std::ostream& operator << ( std::ostream& os, DescVod const& d )
{
	os
	<< "{vod_descriptor\n"
	<< "private_data_length" << FmtDec(d.private_data_length()) << ";\n"
	
	U8 const* pData;
	U16 len = d.private_data( pData );
	os << "private_data_byte " << FmtBinary(len,pData) << ";\n"
	   << "number_of_modules " << FmtDec(d.number_of_modules()) << ";\n"
	   << "loop " << FmtLoop( d.loop_begin() ) << ";\n"
		<< "}";
	return os;
}

//video_steam_descriptor
inline std::ostream& operator << ( std::ostream& os, DescVideoStream const& d )
{
	os
	<< "{video_stream_descriptor\n"
	<< "multiple_frame_rate_flag " << FmtDec(d.mul_frame_rate()) << ";\n"
	<< "frame_rate_code " << FmtDec(d.frame_rate_code()) << ";\n"
	<< "MPEG1_only_flag " << FmtDec(d.MPEG1_only_flag()) << ";\n"
	<< "constraine_parameter_flag "<<FmtDec(d.constraine_parameter_flag()) << ";\n"
	<< "still_picture_flag " << FmtDec(d.still_picture_flag()) << ";\n";
	if(d.has_mpeg1_only())
	{
		os
		<< "profile_level_indication " << FmtDec(d.profile_level_indication()) << ";\n"
		<< "chroma_format " << FmtDec(d.chroma_format()) << ";\n"
		<< "frame_rate_extension_flag " << FmtDec(d.frame_rate_extension_flag()) << ";\n";
	}
	os << "}";
	return os;
}

//audio_stream_descriptor
inline std::ostream& operator << ( std::ostream& os, DescAudioStream const& d )
{
	return os
	<< "{audio_stream_descriptor\n"
	<< "free_format_flag " << FmtDec(d.free_format_flag()) << ";\n"
	<< "ID " << FmtDec(d.ID()) << ";\n"
	<< "layer " << FmtDec(d.layer()) << ";\n"
	<< "variable_rate_audio_indicator " << FmtDec(d.variable_rate_audio_indicator()) << ";\n"
	<< "}";
}

//ISO_639_language_descriptor
inline std::ostream& operator << ( std::ostream& os, DescLoopIso639Language const& d )
{
	return os 
	<< "{iso_639_language_descriptor_loop\n"
	<< "iso_639_language_code " << FmtString(d.iso_639_language_code()) << ";\n"
	<< "audio_type " << FmtDec(d.audio_type()) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, DescIso639Language const& d )
{
	return os
	<< "{iso_639_language_descriptor\n"
	<< "loop " << FmtLoop(d.loop_begin()) << ";\n"
	<< "}";
}

//system_clock_descriptor
inline std::ostream& operator << ( std::ostream& os, DescSystemClock const& d )
{
	return os 
	<< "{system_clock_descriptor\n"
	<< "external_clock_reference_indicator " << FmtDec(d.external_clock_reference_indicator()) << ";\n"
	<< "clock_accuracy_integer " << FmtDec(d.clock_accuracy_integer()) << ";\n"
	<< "clock_accuracy_exponent " << FmtDec(d.clock_accuracy_exponent()) << ";\n"
	<< "}";
}

//maximum bitrate descriptor
inline std::ostream& operator << ( std::ostream& os, DescMaxBitrate const& d )
{
	return os 
	<< "{maximum_bitrate_descriptor\n"
	<< "maximum_bitrate " << FmtDec(d.Max_bitrate()) << ";\n"
	<< "}";
}

//smoothing buffer descriptor
inline std::ostream& operator << ( std::ostream& os, DescSmoothingBuffer const& d )
{
	return os 
	<< "{smoothing_buffer_descriptor\n"
	<< "sb_leak_rate " << FmtDec(d.sb_leak_rate()) << ";\n"
	<< "sb_size " << FmtDec(d.sb_size()) << ";\n"
	<< "}";
}

//stream_identifier_decriptor
inline std::ostream& operator << ( std::ostream& os, DescStreamIdentifier const& d )
{
	return os 
	<< "{stream_identifier_descriptor\n"
	<< "component_tag " << FmtDec(d.component_tag()) << ";\n"
	<< "}";
}

//teletext_descriptor
inline std::ostream& operator << ( std::ostream& os, DescLoopTeletext const& d )
{
	return os 
	<< "{teletext_descriptor_loop\n"
	<< "iso_639_language_code " << FmtString(d.iso_639_language_code()) << ";\n"
	<< "teletext_type " << FmtDec(d.teletext_type()) << ";\n"
	<< "teletext_magazine_number " << FmtDec(d.teletext_magazine_number()) << ";\n"
	<< "teletext_page_number " << FmtDec(d.teletext_page_number()) << ";\n"
	<< "}";
}
inline std::ostream& operator << ( std::ostream& os, DescTeletext const& d )
{
	return os
	<< "{teletext_descriptor\n"
	<< "loop " << FmtLoop(d.loop_begin()) << ";\n"
	<< "}";
}

//subtitling_descriptor
inline std::ostream& operator << ( std::ostream& os, DescLoopSubtitling const& d )
{
	return os 
	<< "{subtitling_descriptor_loop\n"
	<< "iso_639_language_code " << FmtString(d.iso_639_language_code()) << ";\n"
	<< "subtitling_type " << FmtDec(d.subtitling_type()) << ";\n"
	<< "composition_page_id " << FmtDec(d.composition_page_id()) << ";\n"
	<< "ancillary_page_id " << FmtDec(d.ancillary_page_id()) << ";\n"
	<< "}";
}
inline std::ostream& operator << ( std::ostream& os, DescSubtitling const& d )
{
	return os
	<< "{subtitling_descriptor\n"
	<< "loop " << FmtLoop(d.loop_begin()) << ";\n"
	<< "}";
}

//data_broadcast_id_descriptor
inline std::ostream& operator << ( std::ostream& os, DescDataBroadcastId const& d )
{
	U8 const* pData;
	U16 len = d.id_selector_type( pData );

	return os 
	<< "{data_broadcast_id_descriptor\n"
	<< "data_broadcast_id " << FmtDec(d.data_broadcast_id()) << ";\n"
	<< "id_selector_type " << FmtBinary(len, pData) << ";\n"
	<< "}";
}

//AC-3_descriptor
inline std::ostream& operator << ( std::ostream& os, DescAC3 const& d )
{
	os 
	<< "{AC3_descriptor\n"
	<< "component_type_flag " << FmtDec(d.component_type_flag()) << ";\n"
	<< "bsid_flag " << FmtDec(d.bsid_flag()) << ";\n"
	<< "mainid_flag " << FmtDec(d.mainid_flag()) << ";\n"
	<< "asvc_flag " << FmtDec(d.asvc_flag()) << ";\n";
	if(d.bool_component_type_flag())
	{
		os
		<< "ac3_type " << FmtDec(d.ac3_type()) << ";\n";
	}
	if(d.bool_bsid_flag())
	{
		os
		<< "bsid " << FmtDec(d.bsid()) << ";\n";
	}
	if(d.bool_mainid_flag())
	{
		os
		<< "mainid " << FmtDec(d.mainid()) << ";\n";
	}
	if(d.bool_asvc_flag())
	{
		os
		<< "asvc " << FmtDec(d.asvc()) << ";\n";
	}
	U8 const* pData;
	U16 len = d.additional_info( pData );
	os << "additional_info " << FmtBinary(len,pData) << ";\n"
	   << "}";
	return os;
}
