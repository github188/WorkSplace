//liyuequan 2005-3-9  ´´½¨
//lixuejun  2005-3-14 ÐÞ¸ÄDescLinkageÀàµÄpriv_dataº¯Êý
//lixuejun  2005-3-15 Ôö¼ÓMosaicÃèÊö·û
//liyuequan 2005-3-16 ÐÞ¸ÄMosaicÃèÊö·û
//					  ÐÞ¸ÄCountryAvailabilityÃèÊö·û
//                    ÐÞ¸ÄDescLinkageÃèÊö·û
//                    ÐÞ¸ÄCA_identifierÃèÊö·û
//lixuejun  2005-3-17 Ôö¼ÓDescCaÃèÊö·û

#ifndef NOVELSUPER_PSISI_PARSE_COMMONDESC_H_
#define NOVELSUPER_PSISI_PARSE_COMMONDESC_H_

#include "pre.h"

namespace novelsuper
{
namespace psisi_parse 
{

// service_list_descriptor
struct DescLoopServiceList
{
	U16 service_id() const{ return n2h(service_id_); }
	U8 service_type() const{ return service_type_; }
	U8 length() const{ return 3; }
private:
	U8 service_id_[2];
	U8 service_type_;
};
struct DescServiceList : Descriptor<0x41>
{
	NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(DescLoopServiceList)
protected:
	U16 loop_len() const{ return length(); }                     
	U8 const* loop_address() const{ return data(); }
};
typedef DescList_Iterator<DescServiceList> ServiceList_I;


//bouquet_name_descriptor
struct DescBouquetName : Descriptor<0x47>
{
	std::string bouquet_name() const
	{
		return std::string(data(), data() + length());
	}
};
typedef DescList_Iterator<DescBouquetName> BouquetName_I;


//country_availability_descriptor
struct DescLoopCountryAvailability
{
	std::string country_code() const
	{
		return std::string(country_code_, country_code_ + 3);
	}
	U8 length() const{ return 3; }
private:
	U8 country_code_[3];
};
struct DescCountryAvailability : Descriptor<0x49>
{
	U8 country_availability_flag() const{return avail_flag_>>7;}
	NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(DescLoopCountryAvailability)
protected:
	U16 loop_len() const
	{
		if( length() < 1 )
			ThrowRuntimeError("DescCountryAvailability, loop_len, Length < 1");
		return length() - 1; 
	}
	U8 const* loop_address() const{ return data_; }
private:
	U8 avail_flag_;
	U8 data_[1];
};
typedef DescList_Iterator<DescCountryAvailability> CountryAvailability_I;

//linkage_descriptor
struct DescLinkage : Descriptor<0x4a>
{
	U16 ts_id() const{ return n2h( ts_id_ ); }
	U16 orig_network_id() const{ return n2h( orig_network_id_ ); }
	U16 service_id() const{ return n2h( service_id_ ); }
	U8 linkage_type() const{ return linkage_type_; }
	bool mobile_hand_over_linkage_type() const { return (linkage_type() == 0x08);}
	bool nit_origin_type() const { return (origin_type() == 0) ;}

	U8 hand_over_type() const
	{
		if( ! mobile_hand_over_linkage_type() ) 
			ThrowLogicError("There isn't the field of hand_over_origin_type!");
		return hand_over_origin_type_ >> 4;
	}

	U8 origin_type() const
	{
		if( ! mobile_hand_over_linkage_type() )
			ThrowLogicError("There isn't the field of origin_type!");
		return hand_over_origin_type_ & 0x01;
	}

	U16 network_id() const
	{
		if( ! has_network_id() )
			ThrowLogicError("There isn't the field of network_id!");
		return n2h16( data_ );
	}

	U16 initial_service_id() const
	{
		if( ! nit_origin_type() )
			ThrowLogicError("There isn't the field of initial_service_id!");
		unsigned offset = has_network_id() ? 2 : 0;
		return n2h16( data_ + offset );
	}

	unsigned  priv_data( U8 const*& p ) const
	{
		if( ! mobile_hand_over_linkage_type() )
		{
			p = &hand_over_origin_type_;
			if( length() < 7 )
				ThrowRuntimeError("DescLinkage, priv_data, Length < 7");	
			return length() - 7;
		}

		unsigned offset = 0;
		if( has_network_id() )
			offset += 2;
		if( origin_type() == 0 )
			offset += 2;
		p = &data_[offset];

		if( length() < static_cast<signed>(p - &tag_) )
			ThrowRuntimeError("DescLinkage, priv_data, Length error");
		return length() - static_cast<signed>(p - &tag_);
	}
	bool has_network_id() const
	{
		U8 h = hand_over_type();
		return h == 0x01 || h == 0x02 || h == 0x03;
	}
private:
	U8 ts_id_[2];
	U8 orig_network_id_[2];
	U8 service_id_[2];
	U8 linkage_type_;
	U8 hand_over_origin_type_;
	U8 data_[1];
};
typedef DescList_Iterator<DescLinkage> Linkage_I;

//CA_identifier_descriptor
struct DescLoopCAIdentifier
{
	U16 ca_system_id() const {return n2h(ca_system_id_);}
	U8 length() const{ return 2; }
private:
	U8 ca_system_id_[2];
};
struct DescCAIdentifier : Descriptor<0x53>
{
	NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(DescLoopCAIdentifier)
protected:
	U16 loop_len() const{ return length(); }                     
	U8 const* loop_address() const{ return data(); }
};
typedef DescList_Iterator<DescCAIdentifier> CAIdentifier_I;

//private_data_specifier_descriptor
struct DescPrivateDataSpecifier: Descriptor<0x5F>
{
	U32 private_specifier() const {return n2h32(private_specifier_);};
private:
	U8 private_specifier_[4];
};
typedef DescList_Iterator<DescPrivateDataSpecifier> PrivateSpecifier_I;

//data_broadcast_descriptor
struct DescDataBroadcast : Descriptor<0x64>
{
	U16 data_broadcast_id()const {return n2h(data_broadcast_id_);};
	U8 component_tag() const {return component_tag_;};
	U8 selector_length() const {return selector_length_;};

	std::string iso_639_language_code()const
	{
		return std::string(data_+selector_length_, data_+selector_length_+3);
	}
	
	U8 text_length() const {return *(data_ + selector_length_+3);}
	
	unsigned  selector_byte( U8 const*& p ) const
	{
		p = data_;
		return selector_length();
	}
	
	std::string text_char()const
	{
		return std::string( data_ + selector_length_ +4, 
		data_ + selector_length_ + 4 + text_length());
	}
private:
	U8 data_broadcast_id_[2];
	U8 component_tag_;
	U8 selector_length_;
	U8 data_[1];
};
typedef DescList_Iterator<DescDataBroadcast> DataBroadcast_I;

//mosaic descriptor
struct DescSubLoopMosaic
{
	U8 elementary_cell_id() const{ return elementary_cell_id_ & 0x3f; }
	U8 length() const{ return 1; }
private:
	U8 elementary_cell_id_;
};

struct DescLoopMosaic
{
	U8 logical_cell_id() const{	return logical_cell_id_ >> 2; }

	U8 logical_cell_presentation_info() const
	{ return logical_cell_presentation_info_ & 0x07; }

	U8 elementary_cell_length() const
	{ return elementary_cell_length_; }

	NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(DescSubLoopMosaic)

	U8 cell_linkage_info()const
	{ return *(&elementary_cell_length_ + elementary_cell_length() + 1); }

	U16 bouquet_id()const
	{
		if( !has_bouquet_id() )
			ThrowLogicError("There isn't the field of bouquet_id!");
		return n2h16( data_ + elementary_cell_length() + 1 );
	}

	U16 original_network_id()const
	{
		if( !has_other_id())
			ThrowLogicError("There isn't the field of original_network_id!");
		return n2h16( data_ + elementary_cell_length() + 1 );
	}

	U16 ts_id()const
	{
		if( !has_other_id() )
			ThrowLogicError("There isn't the field of ts_id!");
		return n2h16( data_ + elementary_cell_length() + 3 );
	}

	U16 service_id()const
	{
		if( !has_other_id() )
			ThrowLogicError("There isn't the field of service_id!");
		return n2h16( data_ + elementary_cell_length() + 5 );
	}

	U16 event_id()const
	{
		if(  !has_event_id() )
			ThrowLogicError("There isn't the field of event_id!");
		return n2h16( data_ + elementary_cell_length() + 7 );
	}

	U8 length()const
	{
		unsigned char len = 3 + elementary_cell_length() + 1;

		switch(cell_linkage_info())
		{
			case 1:
				len += 2;
				break;
			case 2:
			case 3:
				len += 6;
				break;
			case 4:
				len += 8;
				break;
			default:
				break;
		}

		return len;
	}

	bool has_bouquet_id() const
	{
		return (cell_linkage_info() == 0x01);
	}

	bool has_other_id() const
	{
		return (cell_linkage_info() == 0x02 || cell_linkage_info() == 0x03 || cell_linkage_info() == 0x04);
	}

	bool has_event_id() const
	{
		return (cell_linkage_info() == 0x04);
	}
protected:
	U8 const* loop_address() const{ return &elementary_cell_length_ + 1; }
	U16 loop_len() const{ return elementary_cell_length(); }

private:
	U8 logical_cell_id_;
	U8 logical_cell_presentation_info_;
	U8 elementary_cell_length_;
	U8 data_[1];
};

struct DescMosaic : Descriptor<0x51>
{
	U8 mosaic_entry_point() const{ return mosaic_entry_elementry_ >> 7; } //modified by zqj on 2007/02/12 for mosaic
	U8 number_of_horizontal_elem_cells() const{	return (mosaic_entry_elementry_ & 0x70) >> 4; }//modified by zqj on 2007/02/12 for mosaic
	U8 number_of_vertical_elem_cells() const{ return mosaic_entry_elementry_ & 0x07; }
	NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(DescLoopMosaic)
protected:
	U16 loop_len() const
	{
		if( length() < 1 )
			ThrowRuntimeError("DescMosaic, loop_len, Length < 1");
		return length() - 1 ; 
	}
	U8 const* loop_address() const{ return data_; }
private:
	U8 mosaic_entry_elementry_;
	U8 data_[1];
};
typedef DescList_Iterator<DescMosaic> Mosaic_I;

//ca descriptor
struct DescCA : Descriptor<0x09>
{
	U16 ca_system_id() const{ return n2h( ca_system_id_ ); }
	U16 ca_pid() const{ return n2h( ca_pid_ ) & 0x1FFF; }

	unsigned private_data( U8 const* &p ) const
	{
		p = data_;
		if( length() < 4 )
			ThrowRuntimeError("DescCA, private_data, Length < 4");
		return length() - 4;
	}
private:
	U8 ca_system_id_[2];
	U8 ca_pid_[2];
	U8 data_[1];
};
typedef DescList_Iterator<DescCA> CA_I;

struct DescType : Descriptor<0x01>
{
    std::string text_char()const
    {
        return std::string( data_, data_ + length() );
    }
private:
    U8 data_[1];
};

struct DescName : Descriptor<0x02>
{
    std::string text_char()const
    {
        return std::string( data_, data_ + length() );
    }
private:
    U8 data_[1];
};

struct DescLocation : Descriptor<0x06>
{
    U8 location_tag()const { return location_tag_; }
private:
    U8 location_tag_;
};

}
}
#endif
