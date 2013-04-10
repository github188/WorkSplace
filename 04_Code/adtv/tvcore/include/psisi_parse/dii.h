//ZhaoQuanjun 2005-8-2
#ifndef NOVELSUPER_PSISI_PARSE_DII_H_
#define NOVELSUPER_PSISI_PARSE_DII_H_

#include "imp/pre.h"

namespace novelsuper
{
namespace psisi_parse 
{

struct DiiSectionLoop
{
	U16 module_id() const { return n2h(module_id_);}
	U32 module_size() const { return n2h32(module_size_);}
	U8 module_version() const { return module_version_;}
	U8 module_info_length() const { return module_info_length_;}
	U8 const* module_info() const { return data_;}
	U16 length() const{ return 8 + module_info_length(); }
protected:
	U8 module_id_[2];
	U8 module_size_[4];
    U8 module_version_;
    U8 module_info_length_;
	U8 data_[1];
};

struct DiiSection : DsmccSection
{
	U32 download_id() const { return n2h32(download_id_);}
	U16 block_size() const { return n2h(block_size_);}
	U8  window_size() const { return window_size_;}
	U32 tc_download_window() const { return n2h32(tc_download_window_);}
	U32 tc_download_scenario() const { return n2h32(tc_download_scenario_);}
	U16 compatibility_descriptor() const { return n2h(compatibility_descriptor_);}
	U16 number_of_modules() const { return n2h(number_of_modules_);}
	NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(DiiSectionLoop)
	U16 private_data_length() const { return n2h16(private_data_begin());}
    U8 const* private_data() const	{ return private_data_begin() + 2;}
	U16 loop_len() const { return section_length() - private_data_length() - 2 - 32 - 5 - 4;}
protected:
	U8 download_id_[4];
	U8 block_size_[2];
	U8 window_size_;
	U8 ack_period_;
	U8 tc_download_window_[4];
	U8 tc_download_scenario_[4];
	U8 compatibility_descriptor_[2];
    U8 number_of_modules_[2];
	U8 modules_data_[1];
	U8 const* private_data_begin() const
	{
		U16 module_infos_length = 0;
		U8 const* p = modules_data_;
		for(int i = 0; i < number_of_modules() ; i++)
		{
			module_infos_length += 8 + p[7];
			p = modules_data_ + module_infos_length;
		}
		return modules_data_ + module_infos_length;
	}
	U8 const* loop_address() const{ return modules_data_; }
};


}
}
#endif
