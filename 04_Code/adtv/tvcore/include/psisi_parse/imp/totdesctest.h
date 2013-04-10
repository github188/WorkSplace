//lihuayu  2005-3-11 ����
//lixuejun 2005-3-15 Ӧ������DescLocalTimeOffset�����ѭ�������غ���
//���������ж���ѭ���ĵ�������ע�͵��������������

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
