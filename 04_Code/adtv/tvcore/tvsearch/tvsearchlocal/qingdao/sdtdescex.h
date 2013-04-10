#ifndef NOVELSUPER_PSISIPARSER_SDTDESCEX_H
#define NOVELSUPER_PSISIPARSER_SDTDESCEX_H


#include "psisi_parse/imp/pre.h"
#include "psisi_parse/imp/commondesc.h"

namespace novelsuper
{
	namespace psisi_parse 
	{

		struct ChannelNumberDescService : Descriptor<0x90>
		{
			//U8 service_type() const{ return data_[0]; }
			//U8 service_provider_name_length() const{ return service_provider_name_length_; }

			//std::string service_provider_name_char() const
			//{
			//	return std::string( data_, data_ + service_provider_name_length() );
			//}

			//U8 service_name_length() const{ return *(data_ + service_provider_name_length()); }

			//std::string service_name_char() const
			//{
			//	return std::string( data_ + service_provider_name_length() + 1, 
			//		data_ + service_provider_name_length() + 1 + service_name_length() );
			//}

			U16 channel_number() const{return n2h16(channel_);}
			U8  subsdribed_only_flag() const{return ((data_[0] >> 7) & 0x01);}
			U8  volumn_set() const{return ((data_[0] >>1) & 0x3f);}
			U8  track_set() const{return (data_[0] & 0x01)*2 + ((data_[1] >> 7) & 0x01);}
			U8  program_group() const{return ((data_[1] >> 3) & 0x07);}
			U8 data1() const{return data_[0];}
			U8 data2() const{return data_[1];}
			U8 data3() const{return data_[2];}
			U8 data4() const{return data_[3];}
			//U8 
		private:
				U8 channel_[2];
				U8 data_[2];
			};
		typedef DescList_Iterator<ChannelNumberDescService> ChannelNumberDescService_I;

	}
}
#endif
