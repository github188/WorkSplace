//ZhaoQuanjun 2005-8-2
#ifndef NOVELSUPER_PSISI_PARSE_DDBTEST_H_
#define NOVELSUPER_PSISI_PARSE_DDBTEST_H_

#include "ddb.h"
#include "imp/pretest.h"

namespace novelsuper
{
namespace psisi_parse 
{

inline std::ostream& operator << ( std::ostream& os, DdbSection const& d )
{
	os
	<< "{ddbsection\n"
	<< FmtDsmccSection("download_id", d) << ";\n"
	<< "module_id " << FmtDec( d.module_id()) << ";\n"
	<< "module_version " << FmtDec( d.module_version()) << ";\n"
	<< "block_number " << FmtDec( d.block_number()) << ";\n";

	U8 const* p;
	unsigned len = d.block_data(p);

	return os
	<< "block_data " << FmtBinary(len, p) << ";\n"
	<< "}";
}

}
}

#endif
