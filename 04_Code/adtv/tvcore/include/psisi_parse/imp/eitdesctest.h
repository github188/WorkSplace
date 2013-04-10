//lihuayu  2005-3-11 创建
//lixuejun 2005-3-15 应该增加下面4个类的子循环的重载函数
//DescExtendedEvent、DescContent、DescParentalRating、DescMultComponent
//在描述符中对子循环的调用暂且注释掉，补充完整后打开
//lixuejun 2005-3-28 去掉对字符串长度字段的输出

inline std::ostream& operator << ( std::ostream& os, DescLoopExtendedEvent const& d )
{
	return os
	<< "{extended_event_descriptor_loop\n"
	<< "item_description " << FmtString( d.item_description_char() ) << ";\n"
	<< "item " << FmtString( d.item_char() ) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, DescExtendedEvent const& d )
{
	return os
	<< "{extended_event_descriptor\n"
	<< "descriptor_number " << FmtDec( d.descriptor_number()) << ";\n"
	<< "last_descriptor_number " << FmtDec( d.last_descriptor_number() ) << ";\n"
	<< "iso_639_language_code " << FmtString( d.iso_639_language_code() ) << ";\n"
	<< "length_of_items " << FmtDec( d.length_of_items()) << ";\n"
	<< "loop " << FmtLoop( d.loop_begin() ) << ";\n"
	<< "text " << FmtString( d.text_char() ) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, DescShortEvent const& d )
{
	return os
	<< "{short_event_descriptor\n"
	<< "iso_639_language_code " << FmtString( d.iso_639_language_code() ) << ";\n"
	<< "event_name " << FmtString( d.event_name_char() ) << ";\n"
	<< "text " << FmtString( d.text_char() ) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, DescTimeShiftedEvent const& d )
{
	return os
	<< "{time_shifted_event_descriptor\n"
	<< "reference_service_id " << FmtDec( d.reference_service_id() ) << ";\n"
	<< "reference_event_id " << FmtDec( d.reference_event_id() ) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, DescComponent const& d )
{
	return os
	<< "{component_descriptor\n"
	<< "stream_content " << FmtDec( d.stream_content() ) << ";\n"
	<< "component_type " << FmtDec( d.component_type() ) << ";\n"
	<< "component_tag " << FmtDec( d.component_tag() ) << ";\n"
	<< "iso_639_language_code " << FmtString( d.iso_639_language_code() ) << ";\n"
	<< "text_char " << FmtString( d.text_char() ) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, DescLoopContent const& d )
{
	return os
	<< "{content_descriptor_loop\n"
	<< "content_nibble_level_1 " << FmtDec( d.content_nibble_level_1() ) << ";\n"
	<< "content_nibble_level_2 " << FmtDec( d.content_nibble_level_2() ) << ";\n"
	<< "user_nibble_1 " << FmtDec( d.user_nibble_1() ) << ";\n"
	<< "user_nibble_2 " << FmtDec( d.user_nibble_2() ) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, DescContent const& d )
{
	return os
	<< "{content_descriptor\n"
	<< "loop " << FmtLoop( d.loop_begin() ) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, DescLoopParentalRating const& d )
{
	return os
	<< "{parental_rating_descriptor_loop\n"
	<< "country_code " << FmtString( d.country_code() ) << ";\n"
	<< "rating " << FmtDec( d.tating() ) << ";\n"
	<< "}";
}
inline std::ostream& operator << ( std::ostream& os, DescParentalRating const& d )
{
	return os
	<< "{parental_rating_descriptor\n"
	<< "loop " << FmtLoop( d.loop_begin() ) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, DescLoopMultComponent const& d )
{
	return os
	<< "{multilingual_component_descriptor_loop\n"
	<< "iso_639_language_code " << FmtString( d.iso_639_language_code() ) << ";\n"
	<< "text " << FmtString( d.text_char() ) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, DescMultComponent const& d )
{
	return os
	<< "{multilingual_component_descriptor\n"
	<< "component_tag " << FmtDec( d.component_tag() ) << ";\n"
	<< "loop " << FmtLoop( d.loop_begin() ) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, DescShortSmoothingBuffer const& d )
{
	return os
	<< "{short_smoothing_buffer_descriptor\n"
	<< "sb_size " << FmtDec( d.sb_size() ) << ";\n"
	<< "sb_leak_rate " << FmtDec( d.sb_leak_rate() ) << ";\n"
	<< "dvb_reserved " << FmtString( d.dvb_reserved() ) << ";\n"
	<< "}";
}
