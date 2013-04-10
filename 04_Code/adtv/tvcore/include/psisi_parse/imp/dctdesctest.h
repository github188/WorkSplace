//Zhao Quanjun 2005-12-2

inline std::ostream& operator << ( std::ostream& os, DescHardware const& d )
{
	U8 const* pData;
	U16 len = d.manufacture_id( pData );
    return os
        << "{hardware_descriptor\n"
        << "manufacture_id " << FmtBinary(len,pData) << ";\n"
        << "main_version " << FmtDec( d.main_version() ) << ";\n"
        << "sub_version " << FmtDec( d.sub_version() ) << ";\n"
        << "}";
}

inline std::ostream& operator << ( std::ostream& os, DescNet const& d )
{
    return os
        << "{net_descriptor\n"
		<< "frequency " << FmtDec(d.frequency()) << ";\n"
		<< "fec_outer " << FmtDec(d.fec_outer()) << ";\n"
		<< "modulation " << FmtDec(d.modulation()) << ";\n"
		<< "symbol_rate " << FmtDec(d.symbol_rate()) << ";\n"
		<< "fec_inner " << FmtDec(d.fec_inner()) << ";\n"
        << "}";
}

inline std::ostream& operator << ( std::ostream& os, DescSoftware const& d )
{
	U8 const* pData;
	U16 len = d.manufacture_id( pData );
	U8 const* pData2;
	U16 len2 = d.software_version_char( pData2);
    return os
        << "{hardware_descriptor\n"
        << "manufacture_id " << FmtBinary(len,pData) << ";\n"
        << "main_version " << FmtDec( d.main_version() ) << ";\n"
        << "sub_version " << FmtDec( d.sub_version() ) << ";\n"
        << "software_version_char " << FmtBinary(len2,pData2) << ";\n"
        << "}";
}

inline std::ostream& operator << ( std::ostream& os, DescFileInfo const& d )
{
	U8 const* pData;
	U16 len = d.file_rsa_signature( pData );
    return os
        << "{fileinfo_descriptor\n"
        << "file_type " << FmtDec( d.file_type() ) << ";\n"
        << "file_length " << FmtDec( d.file_length() ) << ";\n"
        << "flash_begin " << FmtDec( d.flash_begin() ) << ";\n"
        << "file_rsa_signature " << FmtBinary(len,pData) << ";\n"
        << "}";
}

