//ZhaoQuanjun 2005-8-2
#ifndef NOVELSUPER_PSISI_PARSE_DSI_H_
#define NOVELSUPER_PSISI_PARSE_DSI_H_

#include "imp/pre.h"

namespace novelsuper
{
namespace psisi_parse 
{

struct DsiSectionLoop
{
	U32 group_id() const{ return n2h32(group_id_);}
	U32 group_size() const{ return n2h32(group_size_);}
	U16 group_info_length() const{ return n2h(group_info_length_);}
	U8 const* group_info() const	{ return data_;}
	U16 private_data_length() const { return n2h16(data_ + group_info_length());}
	U8 const* private_data() const	{ return data_ + group_info_length() + 2;}
	U16 length() const{ return 12 + group_info_length() + 2 + private_data_length(); }
protected:
	U8 group_id_[4];
	U8 group_size_[4];
    U8 group_compatibility_[2];
    U8 group_info_length_[2];
	U8 data_[1];

};

struct DsiSection : DsmccSection
{
	unsigned server_id( U8 const* &p) const
	{ 
		p = server_id_; 
		return 20;
	}
	U16 private_data_length() const{ return n2h(private_data_length_);}
	U16 number_of_groups() const{ return n2h(number_of_groups_);}
	U16 loop_len() const { return private_data_length() - 2;}
	NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(DsiSectionLoop)
protected:
	U8 server_id_[20];
	U8 compatibility_descriptor_[2];
    U8 private_data_length_[2];
	U8 number_of_groups_[2];
	U8 groups_data_[1];
	U8 const* loop_address() const{ return groups_data_; }
};


}
}
#endif
