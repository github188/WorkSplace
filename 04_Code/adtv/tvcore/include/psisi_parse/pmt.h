//lixuejun 2005-3-8
#ifndef NOVELSUPER_PSISI_PARSE_PMT_H_
#define NOVELSUPER_PSISI_PARSE_PMT_H_

#include "imp/pre.h"
#include "imp/commondesc.h"

namespace novelsuper
{
namespace psisi_parse 
{

#include "imp/pmtdesc.h"

struct PmtSectionLoop
{
	U8 stream_type() const{ return stream_type_;}                     
	U16 elementary_pid() const{ return n2h(elementary_pid_) & 0x1fff; }                     
	U16 descs_len() const{ return n2h(es_info_len_) & 0xfff; }                     
	U16 length() const{ return descs_len() + 5; }
	NOVEL_TF_PSISI_PARSE_GENERAL_DESC_ITERATOR_BEGIN_END            
protected:
	U8 stream_type_;
	U8 elementary_pid_[2];
	U8 es_info_len_[2];
	U8 const* descs_address() const{ return es_info_len_ + 2;}
};

struct PmtSection : Section
{
	U16 program_number() const{ return subtable_id(); }                     
	U16 pcr_pid() const{ return n2h16(pcr_pid_) & 0x1fff; }                     
	U16 descs_len() const
	{
		U16 len = n2h(program_info_len_) & 0xfff;
		if( len + 13 > section_length())
			ThrowRuntimeError("PmtSection::descs_len, Descriptor length error");
		return len;
	}
	U16 loop_len() const
	{
		if( section_length() - descs_len() < 13 )
			ThrowRuntimeError("PmttSection, loop_len, Length < 13");
		return section_length() - descs_len() - 13; 
	}
	NOVEL_TF_PSISI_PARSE_GENERAL_DESC_ITERATOR_BEGIN_END
	NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(PmtSectionLoop)
protected:
	U8 pcr_pid_[2];
	U8 program_info_len_[2];
	U8 const* descs_address() const { return program_info_len_ + 2; }
	U8 const* loop_address() const{ return descs_address() + descs_len(); }
};

template< typename Data, typename Traits = DefaultSectionsTraits<Data> >
struct Pmt
{
	Pmt( Data const& d ) : data_(d) {}
	NOVEL_TF_PSISI_PARSE_GENERAL_CROSS_SECTION_DESC_ITERATOR(PmtSection)
	NOVEL_TF_PSISI_PARSE_GENERAL_CROSS_SECTION_LOOP_ITERATOR(PmtSection,PmtSectionLoop)
	NOVEL_TF_PSISI_PARSE_GENERAL_SECTION_ITERATOR(PmtSection,Traits)
private:
	Data const& data_;
};

}
}
#endif
