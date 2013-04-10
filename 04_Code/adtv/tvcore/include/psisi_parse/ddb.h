//ZhaoQuanjun 2005-8-2
#ifndef NOVELSUPER_PSISI_PARSE_DDB_H_
#define NOVELSUPER_PSISI_PARSE_DDB_H_

#include "imp/pre.h"

namespace novelsuper
{
namespace psisi_parse 
{

struct DdbSection : DsmccSection
{
	U32 download_id() const { return transaction_id();}
	U16 module_id() const { return n2h(module_id_);}
	U8  module_version() const { return module_version_;}
	U16 block_number() const { return n2h(block_number_);}
	unsigned block_data( U8 const* &p ) const
	{
		p = block_data_;
		return section_length() - 27;
	}
protected:
	U8 module_id_[2];
	U8 module_version_;
    U8 reserverd_;
	U8 block_number_[2];
	U8 block_data_[1];
};

struct VodDdbSection : Section
{
	U16 module_id() const { return module_id_;}
	U8  module_version() const { return module_version_;}
	U16 block_number() const { return n2h(block_number_);}
	unsigned block_data( U8 const* &p ) const
	{
		p = block_data_;
		return (n2h(section_length_) & 0xfff) - 14;
	}
protected:
	U8 module_id_;
	U8 module_version_;
   U8 reserverd_;
	U8 block_number_[2];
	U8 block_data_[1];
};

}
}
#endif
