
#include "imp/pre.h"

#ifndef NOVELSUPER_PSISI_PARSE_COMMONDESC_BAT_BEIJING_H_
#define NOVELSUPER_PSISI_PARSE_COMMONDESC_BAT_BEIJING_H_

#define SPECIAL_BOUQUET_PID 0x7011

namespace novelsuper
{
	namespace psisi_parse 
	{

		struct DescLoopProgramOrderList
		{
			U16 service_id() const{ return n2h(service_id_); }
			U16 order_number() const{ return n2h(order_number_); }
			U8 length() const{ return 4; }
		private:
			U8 service_id_[2];
			U8 order_number_[2];
		};

		struct DescProgramOrder : Descriptor<0x82>
		{
			NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(DescLoopProgramOrderList)

		protected:
			U16 loop_len() const{ return length(); }                     
			U8 const* loop_address() const{ return data(); }
		};
		typedef DescList_Iterator<DescProgramOrder> DescLoopProgramOrderList_I;

		struct DescLoopProgramVoiceList
		{
			U16 service_id() const{ return n2h(service_id_); }
			U8 left_right_channel() const{ return left_right_channel_; }
			U8 volume_compensation() const{ return volume_compensation_; }
			U8 length() const{ return 4; }
		private:
			U8 service_id_[2];
			U8 left_right_channel_;
			U8 volume_compensation_;
		};

		struct DescProgramVoice : Descriptor<0x83>
		{
			NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(DescLoopProgramVoiceList)
		protected:
			U16 loop_len() const{ return length(); }                     
			U8 const* loop_address() const{ return data(); }
		};
		typedef DescList_Iterator<DescProgramVoice> DescLoopProgramVoiceList_I;

	}
}
#endif

