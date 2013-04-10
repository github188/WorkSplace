//Zhao Quanjun 2005-3-10
#ifndef NOVELSUPER_PSISI_PARSE_SDT_H_
#define NOVELSUPER_PSISI_PARSE_SDT_H_

#include "imp/pre.h"
#include "imp/commondesc.h"

namespace novelsuper
{
namespace psisi_parse 
{

#include "imp/sdtdesc.h"

struct CSDctLoop
{
	U8  manufacturer() const { return manufacturer_; }
	U32 sequence_start() const { return n2h32(sequence_start_); }
	U32 sequence_end() const { return n2h32(sequence_end_); }
	U32 hardware_version() const { return (hardware_version_[0] * 256 + hardware_version_[1]) * 256 + hardware_version_[2]; }
	U8  software_version() const { return software_version_; }
	U16 length() const{ return 13; }           
protected:
	U8 manufacturer_;
    U8 sequence_start_[4];
	U8 sequence_end_[4];
	U8 hardware_version_[3];
	U8 software_version_;
};

//DescCSDct ³£ÊìÉý¼¶ÃèÊö·û
struct DescCSDct : Descriptor<0x4a>
{
	U16 ts_id() const{ return n2h( ts_id_ ); }
	U16 orig_network_id() const{ return n2h( orig_network_id_ ); }
	U16 service_id() const{ return n2h( service_id_ ); }
	U8 download_type() const{ return download_type_; }
	U8 linkage_type() const{ return linkage_type_; }
	U16 loop_len() const { return length() - 8;}	
	NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(CSDctLoop)	
private:
	U8 ts_id_[2];
	U8 orig_network_id_[2];
	U8 service_id_[2];
	U8 download_type_;
	U8 linkage_type_;
	U8 data_[1];
	U8 const* loop_address() const{ return data_; }
};
typedef DescList_Iterator<DescCSDct> CSDct_I;

struct SdtSectionLoop
{
	U16 service_id() const{ return n2h(service_id_); }
	U8 eit_schedule_flag() const{ return (eit_flag_ >>1) & 1; }
	U8 eit_present_follow_flag() const{ return eit_flag_ & 1; }
	U8 running_status() const { return descriptors_loop_length_[0] >> 5; }
	U8 free_ca_mode() const { return (descriptors_loop_length_[0] >> 4) & 1; }
	U16 descs_len() const{ return n2h(descriptors_loop_length_) & 0xfff; }
	U16 length() const{ return descs_len() + 5; }
	NOVEL_TF_PSISI_PARSE_GENERAL_DESC_ITERATOR_BEGIN_END
protected:
	U8 service_id_[2];
	U8 eit_flag_;
	U8 descriptors_loop_length_[2];
	U8 const* descs_address() const{ return descriptors_loop_length_ + 2; }
};

struct SdtSection : Section
{
	U16 transport_stream_id() const{ return subtable_id(); }
	U16 original_network_id() const{ return n2h(original_network_id_); }
	U8  reserved_future_use() const { return reserved_future_use_; }
	U16 descs_len() const{ return 0; } 
	U16 loop_len() const
	{
		if( section_length() < 12 )
			ThrowRuntimeError("SdtSection, loop_len, Length < 12");
		return section_length() - 12; 
	}
	NOVEL_TF_PSISI_PARSE_GENERAL_DESC_ITERATOR_BEGIN_END
	NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(SdtSectionLoop)
protected:
	U8 original_network_id_[2];
	U8 reserved_future_use_;
	U8 const* descs_address() const { return &reserved_future_use_ + 1; }
	U8 const* loop_address() const{ return &reserved_future_use_ + 1; }
};

template< typename Data, typename Traits = DefaultSectionsTraits<Data> >
struct Sdt
{
	Sdt( Data const& d ) : data_(d) {}
	NOVEL_TF_PSISI_PARSE_GENERAL_CROSS_SECTION_DESC_ITERATOR(SdtSection)
	NOVEL_TF_PSISI_PARSE_GENERAL_CROSS_SECTION_LOOP_ITERATOR(SdtSection,SdtSectionLoop)
	NOVEL_TF_PSISI_PARSE_GENERAL_SECTION_ITERATOR(SdtSection,Traits)
private:
	Data const& data_;
};

}
}
#endif
