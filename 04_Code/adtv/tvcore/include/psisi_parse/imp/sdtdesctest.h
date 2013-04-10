//lihuayu  2005-3-11 创建
//lixuejun 2005-3-15 应该增加下面3个类的子循环的重载函数
//DescNvodReference、DescMultServiceName、DescAnnouncementSupport
//在描述符中对子循环的调用暂且注释掉，补充完整后打开

inline std::ostream& operator << ( std::ostream& os, DescService const& d )
{
	return os
	<< "{service_descriptor\n"
	<< "service_type " << FmtDec( d.service_type() ) << ";\n"
	<< "service_provider_name " << FmtString( d.service_provider_name_char() ) << ";\n"
	<< "service_name " << FmtString( d.service_name_char() ) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, DescLoopNvodRef const& d )
{
	return os
	<< "{nvod_reference_descriptor_loop\n"
	<< "transport_stream_id " << FmtDec( d.transport_stream_id() ) << ";\n"
	<< "orig_network_id " << FmtDec( d.orig_network_id() ) << ";\n"
	<< "service_id " << FmtDec( d.service_id() ) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, DescNvodReference const& d )
{
	return os
	<< "{nvod_reference_descriptor\n"
	<< "loop " << FmtLoop( d.loop_begin() ) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, DescTimeShiftedService const& d )
{
	return os
	<< "{time_shifted_service_descriptor\n"
	<< "reference_service_id " << FmtDec( d.reference_srvice_id() ) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, DescLoopMultServiceName const& d )
{
	return os
	<< "{multilingual_service_name_descriptor_loop\n"
	<< "iso_639_language_code " << FmtString( d.iso_639_language_code() ) << ";\n"
	<< "service_provider_name " << FmtString( d.service_provider_name_char() ) << ";\n"
	<< "service_name " << FmtString( d.service_name_char() ) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, DescMultServiceName const& d )
{
	return os
	<< "{multilingual_service_name_descriptor\n"
	<< "loop " << FmtLoop( d.loop_begin() ) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, DescLoopAnnouncementSupport const& d )
{
	os
	<< "{announcement_support_descriptor_loop\n"
	<< "announcement_type " << FmtDec( d.announcement_type() ) << ";\n"
	<< "reference_type " << FmtDec( d.reference_type() ) << ";\n";

	if( d.has_data() )
		os << "orig_network_id " << FmtDec( d.orig_network_id() ) << ";\n"
		   << "ts_id " << FmtDec( d.ts_id() ) << ";\n"
		   << "service_id " << FmtDec( d.service_id() ) << ";\n"
		   << "component_tag " << FmtDec( d.component_tag() ) << ";\n"
		   << "}";

	return os;
}
	
inline std::ostream& operator << ( std::ostream& os, DescAnnouncementSupport const& d )
{
	return os
	<< "{announcement_support_descriptor\n"
	<< "announcement_support_indicator " << FmtDec( d.announcement_support_indicator() ) << ";\n"
	<< "loop " << FmtLoop( d.loop_begin() ) << ";\n"
	<< "}";
}
