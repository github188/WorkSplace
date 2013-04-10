//lixuejun 2005-3-8
#ifndef NOVELSUPER_PSISI_PARSE_CAT_H_
#define NOVELSUPER_PSISI_PARSE_CAT_H_

#include "imp/pre.h"
#include "imp/commondesc.h"

namespace novelsuper
{
namespace psisi_parse 
{

struct CatSection : Section
{
	U16 descs_len() const
	{
		if( section_length() < 9 )
			ThrowRuntimeError("CatSection, descs_len, Length < 9");
		return section_length() - 9; 
	}
	NOVEL_TF_PSISI_PARSE_GENERAL_DESC_ITERATOR_BEGIN_END
protected:
	U8 const* descs_address() const { return data(); }
};

}
}
#endif
