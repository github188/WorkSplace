//Zhao Quanjun 2005-3-10 创建
//lixjuejun    2005-3-18 增加CRC校验函数

#ifndef NOVELSUPER_PSISI_PARSE_TOT_H_
#define NOVELSUPER_PSISI_PARSE_TOT_H_

#include "imp/pre.h"
#include "imp/commondesc.h"

namespace novelsuper
{
namespace psisi_parse
{

#include "imp/totdesc.h"

struct TotSection : SectionHeader
{
	//modified by zqj on 2006/7/7	
	UTCTime utc_time() const
	{ 
    	U8 utc[5];
	    utc[0] = utc_time_[0];
	    utc[1] = utc_time_[1];
	    utc[2] = utc_time_[2];
	    utc[3] = utc_time_[3];
	    utc[4] = utc_time_[4];
		return  *(UTCTime*)utc;
	}
	U16 descs_len() const
	{
		U16 len =  n2h(descriptors_loop_length_) & 0xfff; 
		if( len + 11 > section_length() )
			ThrowRuntimeError("TotSection::descs_len, Descriptor length error");
		return len;
	}
	NOVEL_TF_PSISI_PARSE_GENERAL_DESC_ITERATOR_BEGIN_END
	U8 const* CRC_32() const
	{
		if( section_length() < 2 )
			ThrowRuntimeError("TotSection, CRC_32, Section Length < 2");
		return section_length_ + section_length() - 2; 
	}

	bool CRC_OK() const
	{
		if( section_length() < 1 )
			ThrowRuntimeError("TotSection, CRC_OK, Section Length < 1");
		return ( n2h32(CRC_32()) == CRC( &table_id_, section_length() - 1 ) ); 
	}
protected:
	//UTCTime utc_time_;
	U8 utc_time_[5];
	U8 descriptors_loop_length_[2];
	U8 const* descs_address() const{ return descriptors_loop_length_ + 2;}
};


}
}
#endif
