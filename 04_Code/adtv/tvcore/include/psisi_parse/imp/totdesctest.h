//lihuayu  2005-3-11 创建
//lixuejun 2005-3-15 应该增加DescLocalTimeOffset类的子循环的重载函数
//在描述符中对子循环的调用暂且注释掉，补充完整后打开

inline std::ostream& operator << ( std::ostream& os, DescLoopLocalTimeOffset const& d )
{
	return os
	<< "{local_time_offset_descriptor_loop\n"
	<< "country_code " << FmtString( d.country_code() ) << ";\n"
	<< "country_region_id " << FmtDec( d.country_region_id() ) << ";\n"
	<< "local_time_offset_plarity " << FmtDec( d.local_time_offset_plarity() ) << ";\n"
	<< "local_time_offset " << FmtDec( d.local_time_offset() ) << ";\n"
	<< "time_of_change " << (UTCTime)(d.time_of_change()) << ";\n"
	<< "next_time_offset " << FmtDec( d.next_time_offset() ) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, DescLocalTimeOffset const& d )
{
	return os
	<< "{local_time_offset_descriptor\n"
	<< "loop " << FmtLoop( d.loop_begin() ) << ";\n"
	<< "}";
}
