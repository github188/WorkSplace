//lixuejun 2005-3-10
#ifndef NOVELSUPER_PSISI_PARSE_PATTEST_H_
#define NOVELSUPER_PSISI_PARSE_PATTEST_H_

#include "pat.h"
#include "imp/pretest.h"

namespace novelsuper
{
namespace psisi_parse 
{

inline std::ostream& operator << ( std::ostream& os, PatSectionLoop const& d )
{
	return os
	<< "{pat_section_loop\n"
	<< "program_number " << FmtDec( d.program_number() ) << ";\n"
	<< "pid " << FmtDec( d.pid() ) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, PatSection const& d )
{
	return os
	<< "{patsection\n"
	<< FmtSection( "transport_stream_id", d ) << ";\n"
	<< "loop " << FmtLoop(d.loop_begin()) << ";\n"
	<< "}";
}

}
}

#endif
