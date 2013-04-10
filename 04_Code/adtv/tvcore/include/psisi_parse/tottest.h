//Zhao Quanjun 2005-3-10
#ifndef NOVELSUPER_PSISI_PARSE_TOTTEST_H_
#define NOVELSUPER_PSISI_PARSE_TOTTEST_H_

#include "tot.h"
#include "imp/pretest.h"

namespace novelsuper
{
namespace psisi_parse
{

#include "imp/totdesctest.h"

inline std::ostream& operator << ( std::ostream& os,TotSection const& d )
{
	return os
	<< "{totsection\n"
	<< SectionHeader(d) << ";\n"
	<< "UTC_time " << (UTCTime)d.utc_time() << ";\n"
	<< "descriptor_LocalTimeOffset " << FmtLoop(d.begin<DescLocalTimeOffset>()) << ";\n"
	<< "general_descriptor " << FmtLoop(d.begin<GeneralDescriptor>()) << ";\n"
	<< "}";
}


}
}

#endif
