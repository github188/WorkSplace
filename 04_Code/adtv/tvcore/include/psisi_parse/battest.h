//lixuejun 2005-3-10
#ifndef NOVELSUPER_PSISI_PARSE_BATTEST_H_
#define NOVELSUPER_PSISI_PARSE_BATTEST_H_

#include "bat.h"
#include "imp/pretest.h"

namespace novelsuper
{
namespace psisi_parse 
{

#include "imp/batdesctest.h"

inline std::ostream& operator << ( std::ostream& os, BatSectionLoop const& d )
{
	return os
	<< "{bat_section_loop\n"
	<< "transport_stream_id " << FmtDec(d.ts_id()) << ";\n"
	<< "original_network_id " << FmtDec(d.orig_network_id()) << ";\n"
	<< "descriptor_ServiceList " << FmtLoop(d.begin<DescServiceList>()) << ";\n"
	<< "descriptor_ChannelOrder " << FmtLoop(d.begin<DescChannelOrder>()) << ";\n"
	<< "descriptor_PrivateDataSpecifier " << FmtLoop(d.begin<DescPrivateDataSpecifier>()) << ";\n"
	<< "general_descriptor " << FmtLoop(d.begin<GeneralDescriptor>()) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, BatSection const& d )
{
	return os
	<< "{batsection\n"
	<< FmtSection( "bouquet_id", d ) << ";\n"
	<< "bouquet_descriptors_length " << FmtDec((U16)d.descs_len()) << ";\n"
	<< "descriptor_BouquetName " << FmtLoop(d.begin<DescBouquetName>()) << ";\n"
	<< "descriptor_CAIdentifier " << FmtLoop(d.begin<DescCAIdentifier>()) << ";\n"
	<< "descriptor_CountryAvailability " << FmtLoop(d.begin<DescCountryAvailability>()) << ";\n"
	<< "descriptor_Linkage " << FmtLoop(d.begin<DescLinkage>()) << ";\n"
	<< "descriptor_MultBouquetName " << FmtLoop(d.begin<DescMultBouquetName>()) << ";\n"
	<< "descriptor_PrivateDataSpecifier " << FmtLoop(d.begin<DescPrivateDataSpecifier>()) << ";\n"
	<< "general_descriptor " << FmtLoop(d.begin<GeneralDescriptor>()) << ";\n"
	<< "loop " << FmtLoop(d.loop_begin()) << ";\n"
	<< "}";
}

template< typename Data, typename Traits >
std::ostream& operator << ( std::ostream& os, Bat<Data, Traits> const& d )
{
	return os
	<< "{bat\n"
	<< "sections " << FmtLoop(d.section_begin()) << ";\n"
	<< "descriptor_BouquetName " << FmtLoop(d.template begin<DescBouquetName>()) << ";\n"
	<< "descriptor_CAIdentifier " << FmtLoop(d.template begin<DescCAIdentifier>()) << ";\n"
	<< "descriptor_CountryAvailability " << FmtLoop(d.template begin<DescCountryAvailability>()) << ";\n"
	<< "descriptor_Linkage " << FmtLoop(d.template begin<DescLinkage>()) << ";\n"
	<< "descriptor_MultBouquetName " << FmtLoop(d.template begin<DescMultBouquetName>()) << ";\n"
	<< "descriptor_PrivateDataSpecifier " << FmtLoop(d.template begin<DescPrivateDataSpecifier>()) << ";\n"
	<< "descriptor_ServiceList " << FmtLoop(d.template begin<DescServiceList>()) << ";\n"
	<< "general_descriptor " << FmtLoop(d.template begin<GeneralDescriptor>()) << ";\n"
	<< "loop " << FmtLoop(d.loop_begin()) << ";\n"
	<< "}";
}

}
}

#endif
