//lixuejun 2005-3-8
#ifndef NOVELSUPER_PSISI_PARSE_PAT_H_
#define NOVELSUPER_PSISI_PARSE_PAT_H_

#include "imp/pre.h"

namespace novelsuper
{
namespace psisi_parse 
{

struct PatSectionLoop
{
	U16 program_number() const{ return n2h(program_number_);}                     
	U16 pid() const{ return n2h(pid_) & 0x1fff; }                     
	U16 length() const{ return 4; }
protected:
	U8 program_number_[2];
	U8 pid_[2];
};

class PatSection : public Section
{
public:	
	U16 ts_id() const{ return subtable_id(); }                     
	U16 loop_len() const
	{
		if( section_length() < 9 )
			ThrowRuntimeError("PatSection, loop_len, Length < 9");
		return section_length() - 9; 
	}
	NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(PatSectionLoop)
protected:
	U8 const* loop_address() const{ return data(); }
};


}
}
#endif
