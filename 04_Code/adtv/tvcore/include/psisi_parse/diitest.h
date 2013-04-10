//ZhaoQuanjun 2005-8-2
#ifndef NOVELSUPER_PSISI_PARSE_DIITEST_H_
#define NOVELSUPER_PSISI_PARSE_DIITEST_H_

#include "dii.h"
#include "imp/pretest.h"

namespace novelsuper
{
namespace psisi_parse 
{

inline std::ostream& operator << ( std::ostream& os, DiiSectionLoop const& d )
{
	return os
	<< "{dii_section_loop\n"
	<< "module_id " << FmtDec( d.module_id()) << ";\n"
	<< "module_size " << FmtDec( d.module_size()) << ";\n"
	<< "module_version " << FmtDec( d.module_version()) << ";\n"
	<< "module_info_length " << FmtDec( d.module_info_length()) << ";\n"
	<< "module_info " << FmtBinary( d.module_info_length(), d.module_info()) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, DiiSection const& d )
{
	return os
	<< "{diisection\n"
	<< FmtDsmccSection("transaction_id", d) << ";\n"
	<< "download_id " << FmtDec( d.download_id()) << ";\n"
	<< "block_size " << FmtDec( d.block_size()) << ";\n"
	<< "windows_size " << FmtDec( d.window_size()) << ";\n"
	<< "tc_download_window " << FmtDec( d.tc_download_window()) << ";\n"
	<< "tc_download_scenario " << FmtDec( d.tc_download_scenario()) << ";\n"
	<< "compatibility_descriptor " << FmtDec( d.compatibility_descriptor()) << ";\n"
	<< "number_of_modules " << FmtDec( d.number_of_modules()) << ";\n"
	<< "loop " << FmtLoop( d.loop_begin()) << ";\n"
	<< "private_data_length " << FmtDec( d.private_data_length()) << ";\n"
	<< "private_data " << FmtBinary(d.private_data_length(), d.private_data()) << ";\n"
	<< "}";
}

}
}

#endif
