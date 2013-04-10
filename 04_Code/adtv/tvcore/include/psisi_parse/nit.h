//lixuejun 2005-3-8
#ifndef NOVELSUPER_PSISI_PARSE_NIT_H_
#define NOVELSUPER_PSISI_PARSE_NIT_H_

#include "imp/pre.h"
#include "imp/commondesc.h"

namespace novelsuper
{
namespace psisi_parse 
{

#include "imp/nitdesc.h"

struct NitSectionLoop
{
	U16 ts_id() const{ return n2h(ts_id_);}                     
	U16 orig_network_id() const{ return n2h(orig_network_id_); }                     
	U16 descs_len() const{ return n2h(ts_descs_len_) & 0xfff; }                     
	U16 length() const{ return descs_len() + 6; }
	NOVEL_TF_PSISI_PARSE_GENERAL_DESC_ITERATOR_BEGIN_END
protected:
	U8 ts_id_[2];
	U8 orig_network_id_[2];
	U8 ts_descs_len_[2];
	U8 const* descs_address() const{ return ts_descs_len_ + 2;}
};

struct NitSection : Section
{
	U16 network_id() const{ return subtable_id(); }                     
	U16 descs_len() const
	{
		U16 len = n2h(network_descs_len_) & 0xfff; 
		if( len + 13 > section_length() )
			ThrowRuntimeError("NitSection::descs_len, Descriptor length error");
		return len;
	}
	U16 loop_len() const
	{
		U16 len = n2h16(descs_address() + descs_len()) & 0xfff;
		if( len + descs_len() + 13 != section_length() )
			ThrowRuntimeError("NitSection::loop_len, Loop length error");
		return len;
	}
	NOVEL_TF_PSISI_PARSE_GENERAL_DESC_ITERATOR_BEGIN_END
	NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(NitSectionLoop)
protected:
	U8 network_descs_len_[2];
	U8 const* descs_address() const { return network_descs_len_ + 2; }
	U8 const* loop_address() const{ return descs_address() + descs_len() + 2; }
};

template< typename Data, typename Traits = DefaultSectionsTraits<Data> >
struct Nit
{
	Nit( Data const& d ) : data_(d) {}
	NOVEL_TF_PSISI_PARSE_GENERAL_CROSS_SECTION_DESC_ITERATOR(NitSection)
	NOVEL_TF_PSISI_PARSE_GENERAL_CROSS_SECTION_LOOP_ITERATOR(NitSection,NitSectionLoop)
	NOVEL_TF_PSISI_PARSE_GENERAL_SECTION_ITERATOR(NitSection,Traits)
private:
	Data const& data_;
};

}
}
#endif
