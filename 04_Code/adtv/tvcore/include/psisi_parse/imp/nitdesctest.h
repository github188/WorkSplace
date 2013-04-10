inline std::ostream& operator << ( std::ostream& os, DescNetworkName const& d )
{
	return os 
	<< "{network_name_descriptor\n"
	<< "network_name " << FmtString(d.network_name()) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, DescSatelliteDeliverySys const& d )
{
	return os
	<< "{satellite_delivery_system_descriptor\n"
	<< "frequency " << FmtDec(d.frequency()) << ";\n"
	<< "orbital_position " << FmtDec(d.orbital_position()) << ";\n"
	<< "west_east_flag " << FmtDec(d.west_east_flag()) << ";\n"
	<< "polarization " << FmtDec(d.polarization()) << ";\n"
	<< "modulation " << FmtDec(d.modulation()) << ";\n"
	<< "symbol_rate " << FmtDec(d.symbol_rate()) << ";\n"
	<< "FEC_inner " << FmtDec(d.FEC_inner()) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, DescCableDeliverySys const& d )
{
	return os
	<< "{cable_delivery_system_descriptor\n"
	<< "frequency " << FmtDec(d.frequency()) << ";\n"
	<< "FEC_outer " << FmtDec(d.FEC_outer()) << ";\n"
	<< "modulation " << FmtDec(d.modulation()) << ";\n"
	<< "symbol_rate " << FmtDec(d.symbol_rate()) << ";\n"
	<< "FEC_inner " << FmtDec(d.FEC_inner()) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, DescTerrestrialDeliverySys const& d )
{
	return os
	<< "{terrestrial_delivery_system_descriptor\n"
	<< "centre_frequency " << FmtDec(d.centre_frequency()) << ";\n"
	<< "bandwidth " << FmtDec(d.bandwidth()) << ";\n"
	<< "constellation " << FmtDec(d.constellation()) << ";\n"
	<< "hierarchy_info " << FmtDec(d.hierarchy_info()) << ";\n"
	<< "code_rate_HP_stream " << FmtDec(d.code_rate_HP_stream()) << ";\n"
	<< "code_rate_LP_stream " << FmtDec(d.code_rate_LP_stream()) << ";\n"
	<< "guard_interval " << FmtDec(d.guard_interval()) << ";\n"
	<< "transmission_mode " << FmtDec(d.transmission_mode()) << ";\n"
	<< "other_frequency_flag " << FmtDec(d.other_frequency_flag()) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, DescLoopMultNetworkName const& d )
{
	return os
	<< "{multilingual_network_name_descriptor_loop\n"
	<< "iso_639_language_code " << FmtString(d.iso_639_language_code()) << ";\n"
	<< "network_name " << FmtString(d.network_name()) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, DescMultNetworkName const& d )
{
	return os
	<< "{multilingual_network_name_descriptor\n"
	<< "loop " << FmtLoop(d.loop_begin()) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, DescLoopFrequencyList const& d )
{
	return os 
	<< "{frequency_list_descriptor_loop\n"
	<< "centre_frequency " << FmtDec(d.centre_frequency()) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, DescFrequencyList const& d )
{
	return os
	<< "{frequency_list_descriptor\n"
	<< "coding_type " << FmtDec(d.coding_type()) << ";\n"
	<< "loop " << FmtLoop(d.loop_begin()) << ";\n"
	<< "}";
}
