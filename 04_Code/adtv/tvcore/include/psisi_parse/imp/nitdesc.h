//lixuejun 2005-3-8

#define BCD(data) ( ( (data) >> 4 & 0xF ) * 10 + ( (data) & 0xF ) ) 

struct CSNitDctLoop
{
    U8  manufacturer() const { return manufacturer_; }
    U32 sequence_start() const { return n2h32(sequence_start_); }
    U32 sequence_end() const { return n2h32(sequence_end_); }
    U32 hardware_version() const { return (hardware_version_[0] * 256 + hardware_version_[1]) * 256 + hardware_version_[2]; }
    U8  software_version() const { return software_version_; }
    
    U8* private_data() const
    { 
        return (U8*)(mfr_hardware_version_); 
    }
    U16 length() const{ return 25; }           
protected:
    U8 manufacturer_;
    U8 sequence_start_[4];
    U8 sequence_end_[4];
    U8 hardware_version_[3];
    U8 software_version_;
    U8 mfr_hardware_version_[4];
    U8 mfr_software_version_[4];
    U8 mfr_tag_;
    U8 mfr_software_date_[3];
};

//DescCSDct 常熟升级描述符
struct DescCSNitDct : Descriptor<0x91>
{
    U16 ts_id() const{ return n2h( ts_id_ ); }
    U16 orig_network_id() const{ return n2h( orig_network_id_ ); }
    U16 service_id() const{ return n2h( service_id_ ); }
    U8 download_type() const{ return download_type_; }
    U8 linkage_type() const{ return linkage_type_; }
    U16 loop_len() const { return length() - 8;}    
    NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(CSNitDctLoop)  
private:
    U8 ts_id_[2];
    U8 orig_network_id_[2];
    U8 service_id_[2];
    U8 download_type_;
    U8 linkage_type_;
    U8 data_[1];
    U8 const* loop_address() const{ return data_; }
};
typedef DescList_Iterator<DescCSNitDct> CSNitDct_I;

/******************************************************************************/
//湖北省网软件下载链接描述符
struct DescHBNitDownload : Descriptor<0xA1>
{
    U16 manufacturer_id() const{ return n2h( manufacturer_id_ ); }
    U8 deliver_desc_tag() const { return deliver_desc_tag_; }
    U8 deliver_desc_len() const { return deliver_desc_len_; }
    U32 frequency() const { return n2h32( frequency_ ); }
    U8 FEC_outer() const{ return data_[1] & 0x0F; }
    U8 modulation() const{ return data_[2]; }
    U32 symbol_rate() const{ return n2h32( data_ + 3) >> 4; }
    U8 FEC_inner() const{ return data_[6] & 0x0F; }
    U16 download_pid() const { return n2h16( data_ + 7 ) >> 3; }
    U16 download_type() const { return data_ [8] & 0x07; }
    U8 private_data_len() const { return data_[9]; }
    U8 const* private_data() const
    {
        if( private_data_len() < 4 )
            ThrowRuntimeError("DescHBDownload, private_data, Length < 4");
        return data_ + 10;
    }

private:
    U8 manufacturer_id_[2];
    U8 deliver_desc_tag_;
    U8 deliver_desc_len_;
    U8 frequency_[4];
    U8 data_[1];
};
typedef DescList_Iterator<DescHBNitDownload> HBNitDownload_I;
/******************************************************************************/

/******************************************************************************/
//佳创数据广播，开机画面升级描述符
struct DescLogoUpdate : Descriptor<0xA8>
{
    U16 manufacturer_id() const{ return n2h( manufacturer_id_ ); }
    U8 stb_type_len() const { return stb_type_len_; }
    std::string stb_type() const
    {
        return std::string( data_ , data_ + stb_type_len() );
    }
    U16 logo_ver() const { return n2h16( data_ + stb_type_len()  ); }
    U8 deliver_desc_tag() const { return *( data_ + stb_type_len() + 2 ); }
    U8 deliver_desc_len() const { return *( data_ + stb_type_len() + 3 ); }
    
    U32 frequency() const 
    { 
        U8 *d = ( U8 * )( data_ + stb_type_len() + 4 );
        U32 freq = BCD( d[0] ) *100000 + BCD( d[1] ) * 1000 + BCD( d[2] ) *10 + BCD( d[3] );
        return freq; 
    }
    U8 FEC_outer() const{ return *( data_ + stb_type_len() + 9 ) & 0x0F; }
    U8 modulation() const{ return *( data_ + stb_type_len() + 10 ); }
    U32 symbol_rate() const
    { 
        U8 *d = ( U8 * )( data_ + stb_type_len() + 11 );
        U32 sym = BCD( d[0] ) *100000 + BCD( d[1] ) * 1000 + BCD( d[2] ) * 10  + BCD(d[3] & 0xF0)/10;      
        return sym; 
    }
    U8 FEC_inner() const{ return *( data_ + stb_type_len() + 14 ) & 0x0F; }
    U16 service_id() const{ return n2h16( data_ + stb_type_len() + 15 ); }    
    U8 file_path_len() const { return *( data_ + stb_type_len() + 17 ); }
    std::string file_path() const
    {
        return std::string( data_ + stb_type_len() + 18 , 
            data_ + stb_type_len() + 18 + file_path_len()  );
    }
    U8 file_name_len() const { return *( data_ + stb_type_len() + 18 + file_path_len() ); }
    std::string file_name() const
    {
        return std::string( data_ + stb_type_len() + file_path_len() + 19 , 
            data_ + stb_type_len() + file_path_len() +file_name_len() + 19  );
    }
  
private:
    U8 manufacturer_id_[2];
    U8 stb_type_len_;
    U8 data_[1];
};
typedef DescList_Iterator<DescLogoUpdate> LogoUpdate_I;
/******************************************************************************/

struct DescNetworkName : Descriptor<0x40>
{
    std::string network_name() const
    { 
        return std::string( data(), data() + length() );
    }
};
typedef DescList_Iterator<DescNetworkName> NetworkName_I;

struct DescSatelliteDeliverySys : Descriptor<0x43>
{
    U32 frequency() const{ return n2h32( frequency_ ); }
    U16 orbital_position() const{ return n2h(orbital_position_); }
    U8 west_east_flag() const{ return west_east_flag_ >> 7; }
    U8 polarization() const{ return ( west_east_flag_ & 0x60 ) >> 5; }
    U8 modulation() const{ return west_east_flag_ & 0x1F; }
    U32 symbol_rate() const{ return n2h32(data_) >> 4; }
    U8 FEC_inner() const{ return data_[3] & 0x0F; }
private:
    U8 frequency_[4];
    U8 orbital_position_[2];
    U8 west_east_flag_;
    U8 data_[1];
};
typedef DescList_Iterator<DescSatelliteDeliverySys> SatelliteDeliverySys_I;

struct DescCableDeliverySys : Descriptor<0x44>
{
    U32 frequency() const{ return n2h32( frequency_ ); }
    U8 FEC_outer() const{ return data_[1] & 0x0F; }
    U8 modulation() const{ return data_[2]; }
    U32 symbol_rate() const{ return n2h32( data_ + 3) >> 4; }
    U8 FEC_inner() const{ return data_[6] & 0x0F; }
private:
    U8 frequency_[4];
    U8 data_[1];
};
typedef DescList_Iterator<DescCableDeliverySys> CableDeliverySys_I;

struct DescTerrestrialDeliverySys : Descriptor<0x5a>
{
    U32 centre_frequency() const{ return n2h32( centre_frequency_ ); }
    U8 bandwidth() const{ return data_[0] >> 5; }
    U8 constellation() const{ return data_[1] >> 6; }
    U8 hierarchy_info() const{ return (data_[1] & 0x38) >> 3 ; }
    U8 code_rate_HP_stream() const{ return data_[1] & 0x07; }
    U8 code_rate_LP_stream() const{ return data_[2] >> 5; }
    U8 guard_interval() const{ return (data_[2] & 0x18) >> 3; }
    U8 transmission_mode() const{ return (data_[2] & 0x06) >> 1; }
    U8 other_frequency_flag() const{ return data_[2] & 0x01; }
private:
    U8 centre_frequency_[4];
    U8 data_[1];
};
typedef DescList_Iterator<DescTerrestrialDeliverySys> TerrestrialDeliverySys_I;

struct DescLoopMultNetworkName
{
    std::string iso_639_language_code() const
    {
        return std::string(iso_639_language_code_, iso_639_language_code_ + 3);
    }

    U8 network_name_length() const{ return network_name_length_; }

    std::string network_name() const
    {
        return std::string( &network_name_length_ + 1, 
            &network_name_length_ + 1 + network_name_length());
    }

    U8 length() const{ return network_name_length() + 4; }
private:
    U8 iso_639_language_code_[3];
    U8 network_name_length_;
};

struct DescMultNetworkName : Descriptor<0x5B>
{
    NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(DescLoopMultNetworkName)
protected:
    U16 loop_len() const{ return length(); }                     
    U8 const* loop_address() const{ return data(); }
};
typedef DescList_Iterator<DescMultNetworkName> MultNetworkName_I;

struct DescLoopFrequencyList
{
    U32 centre_frequency() const{ return n2h32( centre_frequency_ ); }
    U8 length() const{ return 4; }
private:
    U8 centre_frequency_[4];
};

struct DescFrequencyList : Descriptor<0x62>
{
    U8 coding_type() const{ return coding_type_ & 0x03; }
    NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(DescLoopFrequencyList)
protected:
    U16 loop_len() const
    {
        if( length() < 1 )
            ThrowRuntimeError("DescFrequencyList, loop_len, Length < 1");
        return length() - 1; 
    }
    U8 const* loop_address() const{ return data_; }
private:
    U8 coding_type_;
    U8 data_[1];
};
typedef DescList_Iterator<DescFrequencyList> FrequencyList_I;


struct DescLoopCodeDownLoad
{   
    U16 descs_len() const
    {
        return data_[1];
    }
    U16 download_pid() const
    {
        return n2h16(data_ + 2 + data_[1]) >> 3; 
    }
    U8 download_type() const
    {
        return n2h16(data_ + 2 + data_[1]) & 0x7; 
    }
    U8 private_data_Len() const
    {
        return *(data_ + 2 + data_[1] + 2);
    }
    U8* private_data() const
    { 
        return (U8*)(data_ + 2 + data_[1] + 2 + 1); 
    }          
    U8 length() const{ return 2 + data_[1] + 3 + private_data_Len(); }
    NOVEL_TF_PSISI_PARSE_GENERAL_DESC_ITERATOR_BEGIN_END
protected:
    U8 data_[1];
    U8 const* descs_address() const { return data_; }
};

struct DescCodeDownload : Descriptor<0xA1>
{
    U16 stb_manufacturer_id() const{ return n2h(stb_manufacturer_id_);}
    NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(DescLoopCodeDownLoad)
protected:
    U16 loop_len() const
    {
        if( length() < 1 )
            ThrowRuntimeError("DescCodeDownload, loop_len, Length < 1");
        return length() - 2; 
    }
    U8 const* loop_address() const{ return stb_manufacturer_id_ + 2; }
private:
    U8 stb_manufacturer_id_[2]; 
};
typedef DescList_Iterator<DescCodeDownload> CodeDownload_I;

