//Zhao Quanjun 2005-3-10
#ifndef NOVELSUPER_PSISI_PARSE_TDTTEST_H_
#define NOVELSUPER_PSISI_PARSE_TDTTEST_H_

#include "tdt.h"
#include "imp/pretest.h"

namespace novelsuper
{
namespace psisi_parse
{

inline std::ostream& operator << ( std::ostream& os, TdtSection const& d )
{
	return os
	<< "{tdtsection\n"
	<< SectionHeader(d) << ";\n"
	<< "UTC_time " << (UTCTime)d.utc_time() << ";\n"
	<< "}";
}

}
}

#endif
