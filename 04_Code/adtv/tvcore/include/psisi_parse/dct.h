//Zhao Quanjun 2005-11-29
#ifndef NOVELSUPER_PSISI_PARSE_DCT_H_
#define NOVELSUPER_PSISI_PARSE_DCT_H_

#include "imp/pre.h"

namespace novelsuper
{
namespace psisi_parse
{

#include "imp/dctdesc.h"

struct DctFileInfoLoop
{
	U8  ddt_tableid() const { return ddt_tableid_; }
	U16 length() const{ return 1 + descs_len(); }           
	U16 descs_len() const { return data_[1] + 2; }
	NOVEL_TF_PSISI_PARSE_GENERAL_DESC_ITERATOR_BEGIN_END
protected:
	U8 ddt_tableid_;
    U8 data_[1];
	U8 const* descs_address() const { return data_; }
};

struct DctSoftwareLoop
{
	U32 files_length() const { return n2h32(data_ + data_[1] + 2); }
	U16 length() const{ return descs_len() + 4 + files_length(); }           
	U16 descs_len() const { return data_[1] + 2; }
	U16 loop_len() const { return files_length();}	
	NOVEL_TF_PSISI_PARSE_GENERAL_DESC_ITERATOR_BEGIN_END
	NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(DctFileInfoLoop)	
protected:
    U8 data_[1];
	U8 const* descs_address() const { return data_; }
	U8 const* loop_address() const{ return data_ + data_[1] + 6; }
};

struct DctSectionLoop
{
	U16 pid_of_ddt() const { return n2h16(data_ + descs_len()); }
	U32 software_length() const { return n2h32(data_ + descs_len() + 2); }
	U16 length() const { return descs_len() + 6 + software_length(); }                 
	U16 descs_len() const { return 24;}
	U16 loop_len() const { return software_length();}	
	NOVEL_TF_PSISI_PARSE_GENERAL_DESC_ITERATOR_BEGIN_END
	NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(DctSoftwareLoop)	
protected:
    U8 data_[1];
	U8 const* descs_address() const { return data_; }
	U8 const* loop_address() const{ return data_ + descs_len() + 6; }
};

struct DctSection : Section
{
	U8  force_upgrade_flag() const { return force_upgrade_flag_; }
	U32 hardware_length() const { return n2h(hardware_length_); }
	U8  dct_rsa_signature( U8 const* &p ) const
	{
		p = data_ + loop_len();
		return 16;
	}
	U16 loop_len() const { return hardware_length() - 16; }	
	NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(DctSectionLoop)	
protected:
	U8 force_upgrade_flag_;
	U8 hardware_length_[4];
	U8 data_[1];
	U8 const* loop_address() const{ return data_; }
};

template< typename Data, typename Traits = DefaultSectionsTraits<Data> >
struct Dct
{
	Dct( Data const& d ) : data_(d) {}
	NOVEL_TF_PSISI_PARSE_GENERAL_CROSS_SECTION_DESC_ITERATOR(DctSection)
	NOVEL_TF_PSISI_PARSE_GENERAL_CROSS_SECTION_LOOP_ITERATOR(DctSection, DctSectionLoop)
	NOVEL_TF_PSISI_PARSE_GENERAL_SECTION_ITERATOR(DctSection,Traits)
private:
	Data const& data_;
};

}
}
#endif
