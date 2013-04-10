//lixuejun 2005-3-10
#ifndef NOVELSUPER_PSISI_PARSE_CATTEST_H_
#define NOVELSUPER_PSISI_PARSE_CATTEST_H_

#include "cat.h"
#include "imp/pretest.h"

namespace novelsuper
{
namespace psisi_parse 
{

inline std::ostream& operator << ( std::ostream& os, CatSection const& d )
{
	return os
	<< "{catsection\n"
	<< FmtSection( "", d ) << ";\n"
	<< "descriptor_CA " << FmtLoop(d.begin<DescCA>()) << ";\n"
	<< "general_descriptor " << FmtLoop(d.begin<GeneralDescriptor>()) << ";\n"
	<< "}";
}

}
}

#endif
