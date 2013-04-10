//lihuayu 2005-3-11
//lixuejun 2005-4-1 ÐÞ¸Ätime_of_change()ºÍlocal_time_offset_plarity()

struct DescLoopLocalTimeOffset
{
	std::string country_code() const
	{ 
		return std::string( country_code_, country_code_ + 3 ); 
	}

	U8 country_region_id() const{ return region_id_and_time_offset_polarity_ >> 2; }
	U8 local_time_offset_plarity() const{ return region_id_and_time_offset_polarity_ & 0x01; }
	U16 local_time_offset() const{ return n2h( local_time_offset_ ); }
	//modified by zqj on 2006/7/7	
	UTCTime time_of_change() const 
	{ 
    	U8 utc[5];
	    utc[0] = time_of_change_[0];
	    utc[1] = time_of_change_[1];
	    utc[2] = time_of_change_[2];
	    utc[3] = time_of_change_[3];
	    utc[4] = time_of_change_[4];
		return  *(UTCTime*)utc;
	}
	U16 next_time_offset() const{ return n2h( next_time_offset_ ); }
	U8 length() const{ return 13; }
private:
	U8 country_code_[3];
	U8 region_id_and_time_offset_polarity_;
	U8 local_time_offset_[2];
	//UTCTime time_of_change_;
	U8 time_of_change_[5];
	U8 next_time_offset_[2];
};

struct DescLocalTimeOffset : Descriptor<0x58>
{
	NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END( DescLoopLocalTimeOffset )
protected:
	U8 loop_len() const{ return length(); }
	U8 const* loop_address() const{ return data(); }

};
typedef DescList_Iterator<DescLocalTimeOffset> LocalTimeOffset_I;
