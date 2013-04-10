//lihuayu 2005-3-11
inline std::ostream& operator << ( std::ostream& os, DescLocalTimeOffset const& d )
{
	return os
	<< "{local time offset descriptor\n"
	<< "loop" << FmtLoop( d.loop_begin() ) << ";\n"
	<< "};\n"
}
