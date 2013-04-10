//lihuayu 2005-3-9
struct DescCa : Descriptor<0x09>
{
	U16 ca_system_id() const{ return n2h( ca_system_id_ ); }
	U16 ca_pid() const{ return n2h( ca_pid_ ) & 0x1FFF; }
    unsigned *private_data() const{ return (unsigned*)data_; }
	U8 private_data_length() const{ return length() - 4; }

private:
	U8 ca_system_id_[2];
	U8 ca_pid_[2];
	U8 data_[1];
};
typedef DescList_Iterator<DescCa> Ca_I;
