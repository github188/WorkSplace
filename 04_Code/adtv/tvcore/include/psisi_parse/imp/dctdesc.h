//Zhao Quanjun 2005-11-30

struct DescHardware : Descriptor<0x82>
{
	U8 manufacture_id( U8 const* &p ) const
	{
		p = manufacture_id_;
		return 3;
	}
	U16 main_version() const { return n2h(main_version_); }
	U16 sub_version() const { return n2h(sub_version_); }
private:
    U8 reserved_;
    U8 manufacture_id_[3];
    U8 main_version_[2];
    U8 sub_version_[2];
    U8 reserved2_;
};
typedef DescList_Iterator<DescHardware> Hardware_I;


struct DescNet : Descriptor<0x44>
{
	U32 frequency() const { return n2h(frequency_); }
	U8  fec_outer() const { return fec_outer_ & 0xf; }
	U8  modulation() const { return modulation_; }
	U32 symbol_rate() const{ return (n2h(symbol_rate_) & 0xfffffff0) >> 4; }
	U8  fec_inner() const { return symbol_rate_[3] & 0xf; }
private:
    U8 frequency_[4];
    U8 reserved_;
    U8 fec_outer_;
    U8 modulation_;
    U8 symbol_rate_[4];
};
typedef DescList_Iterator<DescNet> Net_I;

struct DescSoftware : Descriptor<0x81>
{
	U8 manufacture_id( U8 const* &p ) const
	{
		p = manufacture_id_;
		return 3;
	}
	U16 main_version() const { return n2h(main_version_); }
	U16 sub_version() const { return n2h(sub_version_); }
	U8  software_version_char( U8 const* &p ) const
	{
		p = data_;
		return length() - 9;
	}
private:
    U8 reserved_;
    U8 manufacture_id_[3];
    U8 main_version_[2];
    U8 sub_version_[2];
    U8 reserved2_;
    U8 data_[1];
};
typedef DescList_Iterator<DescSoftware> Software_I;

struct DescFileInfo : Descriptor<0x80>
{
	U8  file_type() const { return file_type_; }
	U32 file_length() const { return n2h(file_length_); }
	U32 flash_begin() const { return n2h(flash_begin_); }	
	U8  file_rsa_signature( U8 const* &p ) const
	{
		p = file_rsa_signature_;
		return 16;
	}
private:
    U8 file_type_;
    U8 file_length_[4];
    U8 flash_begin_[4];
    U8 file_rsa_signature_[16];
};
typedef DescList_Iterator<DescFileInfo> FileInfo_I;

