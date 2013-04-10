//Zhao Quanjun 2005-11-23
//lock service table(lst) 用于vod应用
#ifndef NOVELSUPER_PSISI_PARSE_LST_H_
#define NOVELSUPER_PSISI_PARSE_LST_H_

#include "imp/pre.h"

namespace novelsuper
{
namespace psisi_parse
{

struct LstSectionLoop
{
	U8  component_type() const { return component_type_; }
	U16 component_pid() const { return n2h(component_pid_); }
	U16 ecm_pid() const { return n2h(ecm_pid_); }
	U16 length() const{ return 5; }
protected:
	U8 component_type_;
	U8 component_pid_[2];
	U8 ecm_pid_[2];
};

struct LstSection : SectionHeader
{
	U8 section_no() const { return section_no_; }
	U8 section_last_no() const { return section_last_no_; }
	U32 card_sn() const { return n2h(card_sn_); }
	U32 frequency() const { return n2h(frequency_); }
	U8  fec_outer() const { return fec_outer_[1] & 0xf; }
	U8  modulation() const { return modulation_; }
	U32 symbol_rate() const { return (n2h(symbol_rate_) & 0xfffffff0) >> 4;}
	U8  fec_inner() const { return symbol_rate_[3] & 0xf; }
	U16 pcr_pid() const { return n2h(pcr_pid_); }
	U8  component_count() const { return component_count_; }
	U32 balance() const { return n2h32(data_ + loop_len()); }
	Duration start_time() const { 	return *(Duration*)(data_+ loop_len() + 4);	}
	U16 duration() const { return n2h16(data_+ loop_len() + 7); }
	U16 loop_len() const { return component_count() * 5;}	
	NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(LstSectionLoop)	
protected:
    U8 section_no_;
    U8 section_last_no_;
	U8 card_sn_[4];
	U8 frequency_[4];
	U8 fec_outer_[2];
	U8 modulation_;
	U8 symbol_rate_[4];
	U8 pcr_pid_[2];
	U8 component_count_;
	U8 data_[1];
	U8 const* loop_address() const{ return data_; }
};


}
}
#endif
