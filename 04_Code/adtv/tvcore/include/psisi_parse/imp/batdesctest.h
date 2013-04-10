//lihuayu  2005-3-11 ����
//lixuejun 2005-3-15 ����DescLoopMultBouquetName�����غ���
//lixuejun 2005-3-28 ȥ�����ַ��������ֶε����
//Zhao Quanjun 2005-12-9 ���Ƶ�������������Ľ���

inline std::ostream& operator << ( std::ostream& os, DescLoopChannelOrder const& d )
{
	return os
	<< "{channel_order_descriptor_loop\n"
	<< "service_id " << FmtDec(d.service_id()) << ";\n"
	<< "channel_index_no " << FmtDec(d.channel_index_no()) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, DescChannelOrder const& d )
{
	return os
	<< "{channel_order_descriptor\n"
	<< "loop " << FmtLoop(d.loop_begin()) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, DescLoopMultBouquetName const& d )
{
	return os
	<< "{multilingual_bouquet_name_descriptor_loop\n"
	<< "iso_639_language_code " << FmtString(d.iso_639_language_code()) << ";\n"
	<< "bouquet_name " << FmtString(d.bouquet_name()) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, DescMultBouquetName const& d )
{
	return os
	<< "{multilingual_bouquet_name_descriptor\n"
	<< "loop " << FmtLoop(d.loop_begin()) << ";\n"
	<< "}";
}
