//lihuayu 2005-3-9

//added by zqj on 2007/4/23
struct DescChannelNumber : Descriptor<0x90>
{
	U16 channel_number() const { return n2h(channel_number_); }
	U8  product_id() const {return product_id_; }
	U8  subscribed_only_flag() const { return subscribed_only_flag_; }
	U8  volume_set() const { return volume_set_; }
	U8  track_set() const { return track_set_; }
private:
	U8 channel_number_[2];
	U8 product_id_;
	U8 subscribed_only_flag_;
	U8 volume_set_;
	U8 track_set_;
};
typedef DescList_Iterator<DescChannelNumber> ChannelNumber_I;

struct DescService : Descriptor<0x48>
{
	U8 service_type() const{ return service_type_; }
	U8 service_provider_name_length() const{ return service_provider_name_length_; }

	std::string service_provider_name_char() const
	{
		return std::string( data_, data_ + service_provider_name_length() );
	}

	U8 service_name_length() const{ return *(data_ + service_provider_name_length()); }

	std::string service_name_char() const
	{
		return std::string( data_ + service_provider_name_length() + 1, 
                         data_ + service_provider_name_length() + 1 + service_name_length() );
	}

private:
	U8 service_type_;
	U8 service_provider_name_length_;
	U8 data_[1];
};
typedef DescList_Iterator<DescService> Service_I;

struct DescLoopNvodRef
{
	U16 transport_stream_id() const{ return n2h( transport_stream_id_ ); }
	U16 orig_network_id() const{ return n2h( orig_network_id_ ); }
	U16 service_id() const{ return n2h( service_id_ ); }
	U8 length() const{ return 6; }

private:
	U8 transport_stream_id_[2];
	U8 orig_network_id_[2];
	U8 service_id_[2];
};
struct DescNvodReference : Descriptor<0x4B>
{
	NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END( DescLoopNvodRef )

protected:
	U8 const* loop_address() const{ return data(); }
	U8 loop_len() const{ return length(); }
};
typedef DescList_Iterator<DescNvodReference> NvodReferenc_I;

struct DescTimeShiftedService : Descriptor<0x4c>
{
	U16 reference_srvice_id() const{ return n2h( reference_service_id_ ); }

private:
	U8 reference_service_id_[2];
};
typedef DescList_Iterator<DescTimeShiftedService> TimeShiftedService_I;

struct DescLoopMultServiceName
{
	std::string iso_639_language_code() const
	{ 
		return std::string( iso_639_language_code_, iso_639_language_code_ + 3 ); 
	}

	U8 service_provider_name_length() const
	{ return service_provider_name_length_; }

	std::string service_provider_name_char() const
	{
		return std::string( data_,data_ + service_provider_name_length() ); 
	}

	U8 service_name_length() const
	{ return *(data_ + service_provider_name_length()); }

	std::string service_name_char() const
	{ 
		return std::string( data_ + service_provider_name_length() +1, 
	                  data_ + service_provider_name_length() + 1 + service_name_length() ); 
	}

	U8 length() const
	{ return service_provider_name_length() + service_name_length() + 5; }

private:
	U8 iso_639_language_code_[3];
	U8 service_provider_name_length_;
	U8 data_[1];
};

struct DescMultServiceName : Descriptor<0x5D>
{
	NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(DescLoopMultServiceName)
	
protected:
	U8 loop_len() const{ return length(); }
	U8 const* loop_address() const{ return data(); }
};
typedef DescList_Iterator<DescMultServiceName> MultServiceName_I;

struct DescLoopAnnouncementSupport
{
	U8 announcement_type() const{ return announcement_reference_type_ >> 4; }
	U8 reference_type() const{ return announcement_reference_type_ & 0x01; }

	bool has_data() const
	{
		U8 h = reference_type();
		return h == 0x01 || h == 0x02 || h == 0x03;
	}

	U16 orig_network_id() const
	{
		if( !has_data() )
			ThrowLogicError("There isn't the field of reference_type!");
		return n2h16( data_ );
	}

	U16 ts_id() const
	{
		if( !has_data() )
			ThrowLogicError("There isn't the field of reference_type!");
		return n2h16( data_ + 2 );
	}

	U16 service_id() const
	{
		if( !has_data() )
			ThrowLogicError("There isn't the field of reference_type!");
		return n2h16( data_ + 4 );
	}

	U8 component_tag() const
	{
		if( !has_data() )
			ThrowLogicError("There isn't the field of reference_type!");
		return *(data_ + 6);
	}

	U8 length() const
	{ 
		if( has_data() )
			return 8;
		return 1;
	}

private:
	U8 announcement_reference_type_;
	U8 data_[1];
};

struct DescAnnouncementSupport : Descriptor<0x6E>
{
	U16 announcement_support_indicator() const
	{ return n2h(announcement_support_indicator_); }
   
	NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(DescLoopAnnouncementSupport)

protected:
	U8 loop_len() const
	{
		if( length() < 2 )
			ThrowRuntimeError("DescAnnouncementSupport, loop_len, Length < 2");
		return length() - 2; 
	}
	U8 const* loop_address() const{ return data() + 2; }

private:
	U8 announcement_support_indicator_[2];
};
typedef DescList_Iterator<DescAnnouncementSupport> AnnouncementSupport_I;
