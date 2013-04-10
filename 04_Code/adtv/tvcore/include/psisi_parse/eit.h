//Zhao Quanjun 2005-3-10
#ifndef NOVELSUPER_PSISI_PARSE_EIT_H_
#define NOVELSUPER_PSISI_PARSE_EIT_H_

#include "imp/pre.h"
#include "imp/commondesc.h"

namespace novelsuper
{
namespace psisi_parse
{

#include "imp/eitdesc.h"

struct EitSectionLoop
{
	U16 event_id() const{
		return n2h(event_id_); 
	}
    //modified by zqj on 2006/6/30 
    //5105测试时,有时start_time()的值取错值,如event_id_开始的数据为:14 22 d2 6e 05 30 00 ......
    //取得的start_time()数据为14 22 d2 6e 00,正确数据应为: d2 6e 05 30 00,
    //造成开始时间有时出错，有时机顶盒遥控器长时间无法响应，系统停在mk_time()，
    //出错原因可能是字节对齐的原因。
	UTCTime start_time() const{ 		
    	U8 utc[5];
	    utc[0] = start_time_[0];
	    utc[1] = start_time_[1];
	    utc[2] = start_time_[2];
	    utc[3] = start_time_[3];
	    utc[4] = start_time_[4];
	    return *(UTCTime*)utc;
	}
	Duration duration() const{ 	
    	U8 dur[3];
	    dur[0] = duration_[0];
	    dur[1] = duration_[1];
	    dur[2] = duration_[2];
		return *(Duration*)dur; 
	}
	U8 running_status() const { return descriptors_loop_length_[0] >> 5; }
	U8 free_ca_mode() const { return (descriptors_loop_length_[0] >> 4) & 1; }
	U16 descs_len() const{ return n2h(descriptors_loop_length_) & 0xfff; }
	U16 length() const{ return descs_len() + 12; }
	NOVEL_TF_PSISI_PARSE_GENERAL_DESC_ITERATOR_BEGIN_END
protected:
	U8 event_id_[2];
	U8 start_time_[5];
	U8 duration_[3];
	U8 descriptors_loop_length_[2];
	U8 const* descs_address() const{ return descriptors_loop_length_ + 2;}
};

struct EitSection : Section
{
	U16 service_id() const{ return subtable_id(); }
	U16 transport_stream_id() const{ return n2h(transport_stream_id_); }
	U16 original_network_id() const{ return n2h(original_network_id_); }
	U8 segment_last_section_number() const{ return segment_last_section_number_; }
	U8 last_table_id() const{ return last_table_id_; }
	U16 descs_len() const{ return 0; }
	U16 loop_len() const
	{
		if( section_length() < 15 )
			ThrowRuntimeError("EitSection, loop_len, Length < 15");
		return section_length() - 15; 
	}
	NOVEL_TF_PSISI_PARSE_GENERAL_DESC_ITERATOR_BEGIN_END
	NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(EitSectionLoop)
protected:
	U8 transport_stream_id_[2];
	U8 original_network_id_[2];
	U8 segment_last_section_number_;
	U8 last_table_id_;
	U8 const* descs_address() const { return &last_table_id_ + 1; }
	U8 const* loop_address() const{ return &last_table_id_ + 1; }
};

template< typename Data, typename Traits = DefaultSectionsTraits<Data> >
struct Eit
{
	Eit( Data const& d ) : data_(d) {}
	NOVEL_TF_PSISI_PARSE_GENERAL_CROSS_SECTION_DESC_ITERATOR(EitSection)
	NOVEL_TF_PSISI_PARSE_GENERAL_CROSS_SECTION_LOOP_ITERATOR(EitSection, EitSectionLoop)
	NOVEL_TF_PSISI_PARSE_GENERAL_SECTION_ITERATOR(EitSection,Traits)
private:
	Data const& data_;
};

}
}
#endif
