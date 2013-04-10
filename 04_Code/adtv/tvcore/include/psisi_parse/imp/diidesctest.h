//lihuayu 2005-7-29

inline std::ostream& operator << ( std::ostream& os, DescCRC32 const& d )
{
    return os
        << "{CRC32_descriptor\n"
        << "CRC_32 " << FmtDec( d.crc32() ) << ";\n"
        << "}";
}

inline std::ostream& operator << ( std::ostream& os, DescModuleLink const& d )
{
    return os
        << "{module_link_descriptor\n"
        << "position " << FmtDec( d.position() ) << ";\n"
        << "module_id " << FmtDec( d.module_id() ) << ";\n"
        << "}";
}

inline std::ostream& operator << ( std::ostream& os, DescCompressModule const& d )
{
    return os
        << "{compress_module_descriptor\n"
        << "compression_method " << FmtDec( d.compression_method() ) << ";\n"
        << "original_size " << FmtDec( d.original_size() ) << ";\n"
        << "}";
}
