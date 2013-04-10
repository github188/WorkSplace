#ifndef NOVELSUPER_PSISIPARSER_PMTDESCEX_H
#define NOVELSUPER_PSISIPARSER_PMTDESCEX_H


#include "psisi_parse/imp/pre.h"
#include "psisi_parse/imp/commondesc.h"

namespace novelsuper
{
namespace psisi_parse 
{
	struct DescEAC3 : Descriptor<0x7A>
	{
	};
	
	typedef DescList_Iterator<DescEAC3> EAC3_I;

	struct DescDTS : Descriptor<0x7B>
	{
	};

	typedef DescList_Iterator<DescDTS> DTS_I;
}
}
#endif

