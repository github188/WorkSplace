//liyuequan 2005-3-10 创建
//lixuejun  2005-3-14 修改DescLinkage类的重载函数
//lixuejun  2005-3-15 增加Mosaic描述符的重载函数
//lixuejun  2005-3-15 应该增加mosaic类两层子循环的重载函数
//在描述符中对子循环的调用暂且注释掉，补充完整后打开
//liyuequan 2005-3-16 增加mosaic类两层子循环重载函数
//                    修改countryavailability重载函数
//                    修改ca_identifier重载函数
//lixuejun  2005-3-17 增加DescCa描述符的重载函数

inline std::ostream& operator << ( std::ostream& os, GeneralDescriptor const& d )
{
	return os
	<< "{general_descriptor\n"
	<< "tag " << FmtHex(d.tag()) << ";\n"
	<< "data " << FmtBinary(d.length(), d.data()) << ";\n"
	<< "}";
}

// service_list_descriptor
inline std::ostream& operator << ( std::ostream& os, DescLoopServiceList const& d )
{
	return os 
	<< "{service_list_descriptor_loop\n"
	<< "service_id " << FmtDec(d.service_id()) << ";\n"
	<< "service_type " << FmtDec(d.service_type()) << ";\n"
	<< "}";
}
inline std::ostream& operator << ( std::ostream& os, DescServiceList const& d )
{
	return os
	<< "{service_list_descriptor\n"
	<< "loop " << FmtLoop(d.loop_begin()) << ";\n"
	<< "}";
}

//bouquet_name_descriptor
inline std::ostream& operator << ( std::ostream& os, DescBouquetName const& d )
{
	return os 
	<< "{bouquet_name_descriptor\n"
	<< "bouquet_name " << FmtString(d.bouquet_name()) << ";\n"
	<< "}";
}

//country_availability_descriptor
inline std::ostream& operator << ( std::ostream& os, DescLoopCountryAvailability const& d )
{
	return os 
	<< "{country_availability_descriptor_loop\n"
	<< "country_code " << FmtString(d.country_code()) << ";\n"
	<< "}";
}
inline std::ostream& operator << ( std::ostream& os, DescCountryAvailability const& d )
{
	return os
	<< "{country_availability_descriptor\n"
	<< "country_availability_flag " << FmtDec(d.country_availability_flag()) << ";\n"
	<< "loop " << FmtLoop(d.loop_begin()) << ";\n"
	<< "}";
}

//linkage_descriptor
inline std::ostream& operator << ( std::ostream& os, DescLinkage const& d )
{
	os
	<< "{linkage_descriptor\n"
	<< "transport_steam_id " << FmtDec(d.ts_id()) << ";\n"
	<< "original_network_id " << FmtDec(d.orig_network_id()) << ";\n"
	<< "service_id " << FmtDec(d.service_id()) << ";\n"
	<< "linkage_type " << FmtDec((U16)d.linkage_type()) << ";\n";
	
	if( d.mobile_hand_over_linkage_type() )
	{
		U16 hand_over_type = (U16)d.hand_over_type();
		U16 original_type  = (U16)d.origin_type();
		
		os
		<< "hand_over_type " << FmtDec(hand_over_type) << ";\n"
		<< "original_type " << FmtDec(original_type) << ";\n";
		
		if( d.has_network_id())
			os << "network_id " << FmtDec(d.network_id()) << ";\n";
		
		if( d.nit_origin_type() )
			os << "initial_service_id " << FmtDec(d.initial_service_id()) << ";\n";
	}

	U8 const* pData;
	U16 len = d.priv_data( pData );
	os << "private_data " << FmtBinary(len, pData) << ";\n"
	   << "}";
	return os;
}

//CA_identifier_descriptor
inline std::ostream& operator << ( std::ostream& os, DescLoopCAIdentifier const& d )
{
	return os
	<< "{ca_identifier_descriptor_loop\n"
	<< "ca_system_id " << FmtDec(d.ca_system_id()) << ";\n"
	<< "}";
}
inline std::ostream& operator << ( std::ostream& os, DescCAIdentifier const& d )
{
	return os 
	<< "{ca_identifier_descriptor\n"
	<< "loop " << FmtLoop(d.loop_begin()) << ";\n"
	<< "}";
}

//private_data_specifier_descriptor
inline std::ostream& operator << ( std::ostream& os, DescPrivateDataSpecifier const& d )
{
	return os
	<< "{private_data_specifier_descriptor\n"
	<< "private_specifier " << FmtDec(d.private_specifier()) << ";\n"
	<< "}";
}

//data_broadcast_descriptor
inline std::ostream& operator << ( std::ostream& os, DescDataBroadcast const& d )
{
	U8 const* pData;
	U16 len = d.selector_byte( pData );

	return os
	<< "{data_broadcast_descriptor\n"
	<< "data_broadcast_id " << FmtDec(d.data_broadcast_id()) << ";\n"
	<< "component_tag " << FmtDec(d.component_tag()) << ";\n"
	<< "selector_length " << FmtDec(d.selector_length()) << ";\n"
	<< "selector_byte " << FmtBinary(len, pData) << ";\n"
	<< "iso_639_language_code " << FmtString(d.iso_639_language_code()) << ";\n"
	<< "text_length " << FmtDec(d.text_length()) << ";\n"
	<< "text_char " << FmtString(d.text_char()) << ";\n"
	<< "}";
}

//mosaic descriptor
inline std::ostream& operator << ( std::ostream& os, DescSubLoopMosaic const& d )
{
	return os 
	<< "{mosaic_descriptor_sub_loop\n"
	<< "elementary_cell_id " << FmtDec(d.elementary_cell_id()) << ";\n"
	<< "}";
}
inline std::ostream& operator << ( std::ostream& os, DescLoopMosaic const& d )
{
	os
	<< "{Mosaic_descriptor_loop\n"
	<< "logical_cell_id " << FmtDec(d.logical_cell_id()) << ";\n"
	<< "logical_cell_presentation_info " << FmtDec(d.logical_cell_presentation_info()) << ";\n"
	<< "elementary_cell_length " << FmtDec(d.elementary_cell_length()) << ";\n"
	<< "sub_loop " << FmtLoop(d.loop_begin()) << ";\n";

	if(d.has_bouquet_id())
	{
		os << "bouquet_id "<< FmtDec(d.bouquet_id()) << ";\n";
	}

	if(d.has_other_id())
	{
		os
		<< "original_network_id " << FmtDec(d.original_network_id()) << ";\n"
		<< "transport_stream_id " << FmtDec(d.ts_id()) << ";\n"
		<< "service_id " << FmtDec(d.service_id()) << ";\n";
	}

	if(d.has_event_id())
	{
		os << "event_id " << FmtDec(d.event_id()) << ";\n";
	}
	os << "}";

	return os;
}
inline std::ostream& operator << ( std::ostream& os, DescMosaic const& d )
{
	return os
	<< "{mosaic_descriptor\n"
	<< "mosaic_entry_point " << FmtDec(d.mosaic_entry_point()) << ";\n"
	<< "number_of_horizontal_elementary_cells " 
		<< FmtDec(d.number_of_horizontal_elem_cells()) << ";\n"
	<< "number_of_vertical_elementary_cells " 
		<< FmtDec(d.number_of_vertical_elem_cells()) << ";\n"
	<< "loop " << FmtLoop(d.loop_begin()) << ";\n"
	<< "}";
}

//ca descriptor
inline std::ostream& operator <<( std::ostream& os, DescCA const& d )
{
	U8 const* pdata;
	U16 len = d.private_data( pdata );

	return os
	<< "{ca_descriptor\n"
	<< "ca_system_id " << FmtDec( d.ca_system_id() ) << ";\n"
	<< "ca_pid " << FmtDec( d.ca_pid()) << ";\n"
	<< "private_data " << FmtBinary( len, pdata ) << ";\n"
	<< "}";
}
