//liyuequan 2005-3-9    创建
//liyuequan 2005-3-16   修改AC3描述符
//                      修改videostream描述符
//zqj       2005-9-23   添加vod描述符
//zqj       2005-9-26   添加浏览器服务描述符eis_descriptor

//eis_descriptor
struct DescEis : Descriptor<0x52>
{
   bool is_eis_service() const { return data >= 0xA0 || data >= 10; }
	U8 data;
};
typedef DescList_Iterator<DescEis> Eis_I;

struct DescVodLoop
{
	U8  module_id() const { return module_id_; }
	U8  timeout() const { return timeout_; }
	U32 module_size() const
	{ 
		return ((mmodule_size_[0] * 256 + mmodule_size_[1]) * 256) + mmodule_size_[2];
	}
	U8 length() const {return 5;}
private:
	U8 module_id_;
	U8 mmodule_size_[3];
	U8 timeout_;
};

//vod_descriptor
struct DescVod : Descriptor<0x90>
{
	U8 private_data_length() const	{ return data_[0]; }

	//由于0x90是自定义类型,使用时需要先判断是否为vod descriptor
	bool is_vod_descriptor() const
	{
		if(data_[0] != 5)
			return false;

	   return *(data_ + 1) == 'N' &&
		      *(data_ + 2) == 'T' &&
		      *(data_ + 3) == 'V' &&
		      *(data_ + 4) == 'O' &&
		      *(data_ + 5) == 'D';
	}
	unsigned  private_data( U8 const*& p ) const
	{
		p = data_ + 1;
		return private_data_length();
	}
	
	U8 number_of_modules() const { return *(data_ + 6); }
	NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(DescVodLoop)
protected:
	U16 loop_len() const { return length() - 7;}
	U8 const* loop_address() const { return data_ + 7; }
private:
	U8 data_[1];
};
typedef DescList_Iterator<DescVod> Vod_I;

//video_steam_descriptor
struct DescVideoStream : Descriptor<0x02>
{
	U8 mul_frame_rate() const {return (data_[0] >>7);}
	U8 frame_rate_code() const {return  (data_[0] & 0x78)>>3;}
	U8 MPEG1_only_flag() const {return  (data_[0] & 0x04)>>2;}
	U8 constraine_parameter_flag() const {return (data_[0] & 0x02)>>1;}
	U8 still_picture_flag() const{return (data_[0] & 0x01);}

	U8 profile_level_indication() const
	{
		if(!has_mpeg1_only())
			ThrowLogicError("There isn't the field of profile_level_indication!");
		return data_[1];
	}

	U8 chroma_format() const
	{
		if(!has_mpeg1_only())
			ThrowLogicError("There isn't the field of chroma_format!");	
		return (data_[2] & 0xc0) >> 6;
	}

	U8 frame_rate_extension_flag() const
	{
		if(!has_mpeg1_only())
			ThrowLogicError("There isn't the field of frame_rate_extension_flag!");
		return (data_[2] & 0x20) >> 5;
	}

	bool has_mpeg1_only() const {return (MPEG1_only_flag() == 1);}
private:
	U8 data_[3];
};
typedef DescList_Iterator<DescVideoStream> VideoSteam_I;

//audio_stream_descriptor
struct DescAudioStream : Descriptor<0x03>
{
	U8 free_format_flag() const {return data_ >>7;}
	U8 ID() const {return  (data_ & 0x40)>>6;}
	U8 layer() const {return  (data_ & 0x30)>>4;}
	U8 variable_rate_audio_indicator() const {return (data_ & 0x80)>>3;}
private:
	U8 data_;
};
typedef DescList_Iterator<DescAudioStream> AudioSteam_I;

//ISO_639_language_descriptor
struct DescLoopIso639Language
{
	std::string iso_639_language_code() const
	{
		return std::string(iso_639_language_code_, iso_639_language_code_ + 3);
	}

	U8 audio_type() const {return audio_type_;}
	U8 length() const {return 4;}
private:
	U8 iso_639_language_code_[3];
	U8 audio_type_;
};
struct DescIso639Language : Descriptor<0x0A>
{
	NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(DescLoopIso639Language)
protected:
	U16 loop_len() const{ return length(); }                     
	U8 const* loop_address() const{ return data(); }
};
typedef DescList_Iterator<DescIso639Language> Iso639Language_I;

//system_clock_descriptor
struct DescSystemClock : Descriptor<0x0B>
{
	U8 external_clock_reference_indicator() const{return (data_[0] >>7);}
	U8 clock_accuracy_integer() const {return (data_[0] & 0x3f);}
	U8 clock_accuracy_exponent() const {return (data_[1] & 0xe0)>>5;}
private:
	U8 data_[2];
};
typedef DescList_Iterator<DescSystemClock> SystemClock_I;

//maximum bitrate descriptor
struct  DescMaxBitrate: Descriptor<0x0E>
{
	U32 Max_bitrate() const {return (U32)(((data_[0] & 0x3f)*256 + data_[1])*256 + data_[2]);}
private:
	U8 data_[3];
};
typedef DescList_Iterator<DescMaxBitrate> MaxBitrate_I;

//smoothing buffer descriptor
struct  DescSmoothingBuffer : Descriptor<0x10>
{
	U32 sb_leak_rate() const {return (U32)(((data1_[0] & 0x3f)*256 + data1_[1])*256 + data1_[2]);}
	U32 sb_size() const {return (U32)(((data2_[0] & 0x3f)*256 + data2_[1])*256 + data2_[2]);}
private:
	U8 data1_[3];
	U8 data2_[3];
};
typedef DescList_Iterator<DescSmoothingBuffer> SmoothingBuffer_I;

//stream_identifier_decriptor
struct  DescStreamIdentifier : Descriptor<0x52>
{
	U8 component_tag() const {return component_tag_;}
private:
	U8 component_tag_;
};
typedef DescList_Iterator<DescStreamIdentifier>StreamIdentifier_I;

//teletext_descriptor
struct DescLoopTeletext
{
	std::string iso_639_language_code() const
	{
		return std::string(iso_639_language_code_, iso_639_language_code_ + 3);
	}
	U8 teletext_type() const {return (data_[0] & 0xf8)>>3;}
	U8 teletext_magazine_number() const {return (data_[0] & 0x07);}
	U8 teletext_page_number() const {return data_[1];}
	U8 length() const{return 5;}
private:
	U8 iso_639_language_code_[3];
	U8 data_[2];
};
struct  DescTeletext : Descriptor<0x56>
{
	NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(DescLoopTeletext)
protected:
	U16 loop_len() const{ return length(); }                     
	U8 const* loop_address() const{ return data(); }	
};
typedef DescList_Iterator<DescTeletext> Teletext_I;

//subtitling_descriptor
struct DescLoopSubtitling
{
	std::string iso_639_language_code() const
	{
		return std::string(iso_639_language_code_, iso_639_language_code_ + 3);
	}
   	U8 subtitling_type() const {return subtitling_type_;}
	U16 composition_page_id() const {return n2h(composition_page_id_);}
	U16 ancillary_page_id() const {return n2h(ancillary_page_id_);}
	U8 length() const {return 8;}
private:
	U8 iso_639_language_code_[3];
	U8 subtitling_type_;
	U8 composition_page_id_[2];
	U8 ancillary_page_id_[2];
};
struct  DescSubtitling : Descriptor<0x59>
{
	NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(DescLoopSubtitling)
protected:
	U16 loop_len() const{ return length(); }                     
	U8 const* loop_address() const{ return data(); }	
};
typedef DescList_Iterator<DescSubtitling>Subtitling_I;

//data_broadcast_id_descriptor
struct  DescDataBroadcastId : Descriptor<0x66>
{
	U16 data_broadcast_id() const {return n2h(data_broadcast_id_);}
	unsigned  id_selector_type( U8 const*& p ) const
	{
		p = data_;
		if( length() < 2 )			
			ThrowRuntimeError("DescDataBroadcastId, id_selector_type, Length < 2");
		return length() - 2;
	}
private:
	U8 data_broadcast_id_[2];
	U8 data_[1];
};
typedef DescList_Iterator<DescDataBroadcastId> DataBroadcastId_I;

//AC-3_descriptor
struct  DescAC3: Descriptor<0x6A>
{
	U8 component_type_flag() const {return (flag_ & 0x80)>>7;}
	U8 bsid_flag() const { return (flag_ & 0x40)>>6;}
	U8 mainid_flag() const {return (flag_ & 0x20)>>5;}
	U8 asvc_flag() const {return (flag_ & 0x10) >> 4;}
	bool bool_component_type_flag() const { return (component_type_flag() == 1);}
	bool bool_bsid_flag() const { return (bsid_flag() == 1);}
	bool bool_mainid_flag() const { return (mainid_flag() == 1);}
	bool bool_asvc_flag() const { return (asvc_flag() == 1);}

	U8 ac3_type() const
	{
		if( !bool_component_type_flag())
			ThrowLogicError("There isn't the field of AC-3_type!");
		return *data_;
	}

	U8 bsid() const
	{
		if( !bool_bsid_flag())
			ThrowLogicError("There isn't the field of bsid!");
		if(!bool_component_type_flag()) 
			return *data_;
		else 
			return *(data_+1);
	}

	U8 mainid() const
	{
		if( !mainid_flag())
			ThrowLogicError("There isn't the field of mainid!");
		U8 i = 0;
		if(bool_component_type_flag()) 
			i++;
		if(bool_bsid_flag()) 
			i++;
		if (i == 0) 
			return *data_;
		else if(i == 1) 
			return *(data_+1);
		else 	
			return *(data_+2);
	}

	U8 asvc() const
	{
		if( !bool_asvc_flag())
			ThrowLogicError("There isn't the field of asvc!");
		U8 i = 0;
		if(bool_component_type_flag()) 
			i++;
		if(bool_bsid_flag()) 
			i++;
		if(bool_mainid_flag()) 
			i++;
		
		if (i == 0) 
			return *data_;
		else if(i == 1) 
			return *(data_ + 1);
		else if(i == 2)  
			return *(data_ + 2);
		else 	
			return *(data_ + 3);
	}

	unsigned  additional_info( U8 const*& p ) const
	{
		U8 i = 0;
		if(bool_component_type_flag()) 
			i++;
		if(bool_bsid_flag()) 
			i++;
		if(bool_mainid_flag()) 
			i++;
		if(bool_asvc_flag()) 
			i++;
   		p = data_ + i;
		if( length() < (static_cast<signed>(p - &tag_) - 2) )
			ThrowRuntimeError("DescAC3, additional_info, Length < 2");
		return length() - (static_cast<signed>(p - &tag_) - 2);
	}
private:
	U8 flag_;
	U8 data_[1];
};
typedef DescList_Iterator<DescAC3> AC3_I;
