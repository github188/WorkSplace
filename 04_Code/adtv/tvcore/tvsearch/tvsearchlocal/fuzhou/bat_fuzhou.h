
#include "imp/pre.h"

#ifndef NOVELSUPER_PSISI_PARSE_COMMONDESC_BAT_BEIJING_H_
#define NOVELSUPER_PSISI_PARSE_COMMONDESC_BAT_BEIJING_H_

#define SPECIAL_BOUQUET_PID 0x0320

typedef struct  
{
	U16 service_id;		//业务ID.
	U16 order_number;	// 该业务在网络搜索中排列顺序.
	U8	left_right_channel;	
	U8	volume_compensation;
}FuZhuoProgOrder_VoiceC_Desc;
typedef std::map<U16, FuZhuoProgOrder_VoiceC_Desc> MapProgOrder_VoiceC_DescT;


namespace novelsuper
{
	namespace psisi_parse 
	{

		struct DescLoopProgramOrderList
		{
			U16 service_id() const{ return n2h(service_id_); }
			U16 order_number() const{ return n2h(order_number_); }
			U8 left_right_channel() const{ return left_right_channel_; }
			U8 volume_compensation() const{ return volume_compensation_; }
			U8 length() const{ return 6; }
		private:
			U8 service_id_[2];
			U8 order_number_[2];	// 1- 400
			U8 left_right_channel_;	//0:立体声,1:左声道,2:右声道3:单声道
			U8 volume_compensation_; //-5 -- +5
		};

		struct DescProgOrder_Voice : Descriptor<0x80>
		{
			NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(DescLoopProgramOrderList)

		protected:
			U16 loop_len() const{ return length(); }                     
			U8 const* loop_address() const{ return data(); }
		};
		typedef DescList_Iterator<DescProgOrder_Voice> DescLoopProgOrder_Voice_List_I;

	}
}
#endif

