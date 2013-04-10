//lihuayu 2005-7-29

inline std::ostream& operator << ( std::ostream& os, DescGroupLink const& d )
{
    return os
        << "{group_link_descriptor\n"
        << "position " << FmtDec( d.position() ) << ";\n"
        << "group_id " << FmtDec( d.group_id() ) << ";\n"
        << "}";
}
