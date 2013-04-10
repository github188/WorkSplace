//Zhao Quanjun 2005-3-10
#ifndef NOVELSUPER_PSISI_PARSE_TDT_H_
#define NOVELSUPER_PSISI_PARSE_TDT_H_

#include "imp/pre.h"

namespace novelsuper
{
namespace psisi_parse
{

struct TdtSection : SectionHeader
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
protected:
	//UTCTime utc_time_;
	U8 utc_time_[5];
};


}
}
#endif
