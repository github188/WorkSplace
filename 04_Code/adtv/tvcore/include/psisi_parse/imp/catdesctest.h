//lihuayu 2005-3-11
inline std::ostream& operator <<( std::ostream& os, DescCa const& d )
{
	return os
	<< "{ca_descriptor:\n"
	<< "ca system id: " << std::dec << (U16)d.ca_system_id() << ";\n"
	<< "ca pid: " << std::dec << (U16)d.ca_pid() << ";\n"
	<< "private data length: " << std::dec << (U16)d.private_data_length() << ";\n"
	<< "private data:  #" << (U16*)d.private_data() << ";\n"
	<< "};\n";
}
