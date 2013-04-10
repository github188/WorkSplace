//ZhaoQuanjun 2005-8-2
#ifndef NOVELSUPER_PSISI_PARSE_DSITEST_H_
#define NOVELSUPER_PSISI_PARSE_DSITEST_H_

#include "dsi.h"
#include "imp/pretest.h"

namespace novelsuper
{
namespace psisi_parse 
{

inline std::ostream& operator << ( std::ostream& os, DsiSectionLoop const& d )
{
	return os
	<< "{dsi_section_loop\n"
	<< "group_id " << FmtHex( d.group_id() ) << ";\n"
	<< "group_size " << FmtDec( d.group_size() ) << ";\n"
	<< "group_info_length " << FmtDec( d.group_info_length() ) << ";\n"
	<< "group_info " << FmtBinary( d.group_info_length(), d.group_info()) << ";\n"
	<< "private_data_length " << FmtDec( d.private_data_length() ) << ";\n"
	<< "private_data " << FmtBinary( d.private_data_length(), d.private_data()) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, DsiSection const& d )
{
	os
	<< "{dsisection\n"
	<< FmtDsmccSection("transaction_id", d) << ";\n";
	U8 const* p;
	unsigned len = d.server_id(p);
	return os<< "server_id " << FmtBinary( len, p) << ";\n"
	<< "private_data_length " << FmtDec( d.private_data_length()) << ";\n"
	<< "number_of_groups " << FmtDec( d.number_of_groups()) << ";\n"
	<< "loop " << FmtLoop( d.loop_begin()) << ";\n"
	<< "}";
}

}
}

#endif
