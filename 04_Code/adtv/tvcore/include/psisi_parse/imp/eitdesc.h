//lixuejun 2005-3-8

struct DescLoopExtendedEvent
{
	U8 item_description_length() const
	{ return item_description_length_; }

	std::string item_description_char() const
	{ 
		return std::string( data_, data_ + item_description_length() );
	}

	U8 item_length() const
	{ return *(data_ + item_description_length()); }

	std::string item_char() const
	{ 
		return std::string( data_ + item_description_length() + 1, 
				    data_ + item_description_length() + 1 + item_length() );
	}

	U8 length() const
	{ return item_description_length() + item_length() + 2;}

private:
	U8 item_description_length_;
	U8 data_[1];
};

struct DescExtendedEvent : Descriptor<0x4E>
{
	U8 descriptor_number() const{ return descriptor_number_ >> 4; }
    
	U8 last_descriptor_number() const{ return descriptor_number_ & 0xf; }

	std::string iso_639_language_code()const
	{ 
		return std::string(iso_639_language_code_, iso_639_language_code_ + 3);
	}

	U8 length_of_items() const{ return length_of_items_; }

	NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(DescLoopExtendedEvent)

	U8 text_length() const{ return *(data_ + length_of_items()); }

	std::string text_char()const
	{ 
		return std::string( data_ + length_of_items() + 1, 
					      data_ + length_of_items() + 1 + text_length());
	}
	
protected:
	U16	loop_len()const { return length_of_items(); }
	U8 const* loop_address() const{ return data_; }

private:
	U8 descriptor_number_;
	U8 iso_639_language_code_[3];
	U8 length_of_items_;
	U8 data_[1];
};
typedef DescList_Iterator<DescExtendedEvent> ExtendedEvent_I;

//lihuayu 2005-3-10
struct DescShortEvent : Descriptor<0x4D>
{
	std::string iso_639_language_code() const
	{ 
		return std::string( iso_639_language_code_, iso_639_language_code_ + 3 );
	}

	U8 event_name_length() const{ return event_name_length_; }

	std::string event_name_char() const
	{
		return std::string( data_, data_ + event_name_length() ); 
	}

	U8 text_length() const{ return *( data_ + event_name_length() ); }

	std::string text_char() const
	{ 
		return std::string( data_ + event_name_length() + 1, 
	                        data_ + event_name_length() + 1 + text_length() );
	}

private:
	U8 iso_639_language_code_[3];
	U8 event_name_length_;
	U8 data_[1];
};
typedef DescList_Iterator<DescShortEvent> ShortEvent_I;

struct DescTimeShiftedEvent : Descriptor<0x4F>
{
	U16 reference_service_id() const{ return n2h( reference_service_id_ ); }
	U16 reference_event_id() const{ return n2h( reference_event_id_ ); }

private:
	U8 reference_service_id_[2];
	U8 reference_event_id_[2];
};
typedef DescList_Iterator<DescTimeShiftedEvent> TimeShiftedEvent_I;

struct DescComponent : Descriptor<0x50>
{
	U8 stream_content() const{ return stream_content_ & 0x0F; }
	U8 component_type() const{ return component_type_; }
	U8 component_tag() const{ return component_tag_; }

	std::string iso_639_language_code() const
	{ 
		return std::string( iso_639_language_code_, iso_639_language_code_ + 3 ); 
	}

	std::string text_char() const
	{ 
		if( length() < 6 )			
			ThrowRuntimeError("DescComponent, text_char, Length < 6");
		return std::string( data_, data_ + length() - 6 ); 
	}

private:
	U8 stream_content_;
	U8 component_type_;
	U8 component_tag_;
	U8 iso_639_language_code_[3];
	U8 data_[1];
};
typedef DescList_Iterator<DescComponent> Component_I;

struct DescLoopContent
{
	U8 content_nibble_level_1() const{ return content_nibble_level_ >> 4; }
	U8 content_nibble_level_2() const{ return content_nibble_level_ & 0x0F; }
	U8 user_nibble_1() const{ return user_nibble_ >> 4; }
	U8 user_nibble_2() const{ return user_nibble_ & 0x0F; }
	U8 length() const{ return 2; }

private:
	U8 content_nibble_level_;
	U8 user_nibble_;
};

struct DescContent : Descriptor<0x54>
{
	NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(DescLoopContent)

protected:
	U8 loop_len() const{ return length(); }
	U8 const* loop_address() const{ return data(); }
};
typedef DescList_Iterator<DescContent> Content_I;

struct DescLoopParentalRating
{
	std::string country_code() const
	{ 
		return std::string( country_code_, country_code_ + 3); 
	}

	U8 tating() const{ return rating_; }
	U8 length() const{ return 4; }

private:
	U8 country_code_[3];
	U8 rating_;
};

struct DescParentalRating :Descriptor<0x55>
{
	NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(DescLoopParentalRating)

protected:
	U8 loop_len() const{ return length(); }
	U8 const* loop_address() const{ return data(); }
};
typedef DescList_Iterator<DescParentalRating> ParentalRating_I;

struct DescLoopMultComponent
{
	std::string iso_639_language_code() const
	{
		return std::string( iso_639_language_code_, iso_639_language_code_ + 3 ); 
	}

	U8 text_description_length() const{ return text_description_length_; }

	std::string text_char() const
	{ 
		return std::string( data_, data_ + text_description_length() ); 
	}

	U8 length() const{ return text_description_length() + 4; }

private:
	U8 iso_639_language_code_[3];
	U8 text_description_length_;
	U8 data_[1];
};

struct DescMultComponent : Descriptor<0x5E>
{
	U8 component_tag() const{ return component_tag_; }

	NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(DescLoopMultComponent)

protected:
	U8 loop_len() const
	{
		if( length() < 1 )
			ThrowRuntimeError("DescMultComponent, loop_len, Length < 1");
		return length() - 1; 
	}
	U8 const* loop_address() const{ return data() +1; }

private:
	U8 component_tag_;
};
typedef DescList_Iterator<DescMultComponent> MultComponent_I;

struct DescShortSmoothingBuffer : Descriptor<0x61>
{
	U8 sb_size() const{ return sb_size_and_leak_rate_  >> 6; }
	U8 sb_leak_rate() const{ return sb_size_and_leak_rate_ & 0x3F; }

	std::string dvb_reserved() const
	{
		if( length() < 1 )
			ThrowRuntimeError("DescShortSmoothingBuffer, dvb_reserved, Length < 1");
		return std::string( data_, data_ + length() - 1 ); 
	}

private:
	U8 sb_size_and_leak_rate_;
	U8 data_[1];
};
typedef DescList_Iterator<DescShortSmoothingBuffer> ShortSmoothingBuffer_I;
