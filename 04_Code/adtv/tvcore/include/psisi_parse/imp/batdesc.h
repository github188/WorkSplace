//lihuayu  2005-3-11 ´´½¨
//lixuejun 2005-3-15 ÐÞ¸Ä

struct DescLoopMultBouquetName
{
	std::string iso_639_language_code() const
	{ 
		return std::string(iso_639_language_code_, iso_639_language_code_ + 3);
	}

	U8 bouquet_name_length() const{ return bouquet_name_length_; }

	std::string bouquet_name() const
	{ 
		return std::string( &bouquet_name_length_ + 1, 
	                      &bouquet_name_length_ + 1 + bouquet_name_length());
	}

	U8 length() const{ return bouquet_name_length() + 4; }

private:
	U8 iso_639_language_code_[3];
	U8 bouquet_name_length_;
};

struct DescMultBouquetName : Descriptor<0x5c>
{
	NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(DescLoopMultBouquetName)

protected:
	U16 loop_len() const{ return length(); }                     
	U8 const* loop_address() const{ return data(); }
};
typedef DescList_Iterator<DescMultBouquetName> MultBouquetName_I;


struct DescLoopChannelOrder
{
	U16 service_id() const { return n2h(service_id_); }
	U16 channel_index_no() const{ return n2h(channel_index_no_); }
	U8 length() const{ return 4; }

private:
	U8 service_id_[2];
	U8 channel_index_no_[2];
};

struct DescChannelOrder : Descriptor<0x82>
{
	NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(DescLoopChannelOrder)

protected:
	U16 loop_len() const{ return length(); }                     
	U8 const* loop_address() const{ return data(); }
};
typedef DescList_Iterator<DescChannelOrder> ChannelOrder_I;



//AD_private_data_descriptor
struct DescAD : Descriptor<0xF0>
{
	U8 valid_flag() const { return flag&0x01; }
	U16 AD_version() const{ return n2h(version); }
  std::string Private_ID() const
	{
		return std::string(data_,  data_+ length() -3);
	}
 private:
	U8 flag;
	U8 version[2];
  U8 data_[1];
};
typedef DescList_Iterator<DescAD> AD_I;


//AD_Linkage_descriptor
struct DescADLinkage : Descriptor<0x4A >
{
	U16 ts_id() const{ return n2h( ts_id_ ); }
	U16 orig_network_id() const{ return n2h( orig_network_id_ ); }
	U16 service_id() const{ return n2h( service_id_ ); }
	U8 linkage_type() const{ return linkage_type_; }
  U8 des_length() const{ return data_[0]; }
  U32 ad_ts_freq() const
  { 
    int frq = ( data_[1]&0x0f )*100000 + (data_[2]>>4)*10000 + (data_[2]&0x0f) * 1000;
    return frq;
  }
  U8 ad_ts_mod() const{ return data_[5]; }
  U32 ad_ts_symbol_rate() const
  { 
    int symbol_rate = ( data_[6]>>4 )*100000 + ( data_[6]&0x0f )*10000 + (data_[7]>>4)*1000 + (data_[7]&0x0f) * 100
                      + (data_[8]>>4)*10 + (data_[8]&0x0f);
    return symbol_rate;
  }
  U8 AD_Headdate_table_id() const { return data_[10]; }
  U8 AD_Picdate_table_id() const { return data_[11]; }
private:
	U8 ts_id_[2];
	U8 orig_network_id_[2];
	U8 service_id_[2];
	U8 linkage_type_;
	U8 data_[1];

};
typedef DescList_Iterator<DescADLinkage> AD_Linkage_I;




