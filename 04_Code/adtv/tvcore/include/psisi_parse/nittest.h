//lixuejun 2005-3-9
#ifndef NOVELSUPER_PSISI_PARSE_NITTEST_H_
#define NOVELSUPER_PSISI_PARSE_NITTEST_H_

#include "nit.h"
#include "imp/pretest.h"

namespace novelsuper
{
namespace psisi_parse 
{

#include "imp/nitdesctest.h"

inline std::ostream& operator << ( std::ostream& os, NitSectionLoop const& d )
{
	return os
	<< "{nit_section_loop\n"
	<< "transport_stream_id " << FmtDec( d.ts_id() ) << ";\n"
	<< "original_network_id " << FmtDec( d.orig_network_id() ) << ";\n"
	<< "descriptor_SatelliteDeliverySys " << FmtLoop(d.begin<DescSatelliteDeliverySys>()) << ";\n"
	<< "descriptor_CableDeliverySys " << FmtLoop(d.begin<DescCableDeliverySys>()) << ";\n"
	<< "descriptor_TerrestrialDeliverySys " << FmtLoop(d.begin<DescTerrestrialDeliverySys>()) << ";\n"
	<< "descriptor_ServiceList " << FmtLoop(d.begin<DescServiceList>()) << ";\n"
	<< "descriptor_FrequencyList " << FmtLoop(d.begin<DescFrequencyList>()) << ";\n"
	<< "descriptor_PrivateDataSpecifier " << FmtLoop(d.begin<DescPrivateDataSpecifier>()) << ";\n"
	<< "general_descriptor " << FmtLoop(d.begin<GeneralDescriptor>()) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, NitSection const& d )
{
	return os
	<< "{nitsection\n"
	<< FmtSection( "network_id", d ) << ";\n"
	<< "network_descriptors_length " << FmtDec(d.descs_len()) << ";\n"
	<< "descriptor_Linkage " << FmtLoop(d.begin<DescLinkage>()) << ";\n"
	<< "descriptor_MultNetworkName " << FmtLoop(d.begin<DescMultNetworkName>()) << ";\n"
	<< "descriptor_NetworkName " << FmtLoop(d.begin<DescNetworkName>()) << ";\n"
	<< "descriptor_PrivateDataSpecifier " << FmtLoop(d.begin<DescPrivateDataSpecifier>()) << ";\n"
	<< "general_descriptor " << FmtLoop(d.begin<GeneralDescriptor>()) << ";\n"
	<< "loop " << FmtLoop(d.loop_begin()) << ";\n"
	<< "}";
}

template< typename Data, typename Traits >
std::ostream& operator << ( std::ostream& os, Nit<Data, Traits> const& d )
{
	return os
	<< "{nit\n"
	<< "sections " << FmtLoop(d.section_begin()) << ";\n"
	<< "descriptor_Linkage " << FmtLoop(d.template begin<DescLinkage>()) << ";\n"
	<< "descriptor_MultNetworkName " << FmtLoop(d.template begin<DescMultNetworkName>()) << ";\n"
	<< "descriptor_NetworkName " << FmtLoop(d.template begin<DescNetworkName>()) << ";\n"
	<< "descriptor_PrivateDataSpecifier " << FmtLoop(d.template begin<DescPrivateDataSpecifier>()) << ";\n"
	<< "descriptor_SatelliteDeliverySys " << FmtLoop(d.template begin<DescSatelliteDeliverySys>()) << ";\n"
	<< "descriptor_CableDeliverySys " << FmtLoop(d.template begin<DescCableDeliverySys>()) << ";\n"
	<< "descriptor_TerrestrialDeliverySys " << FmtLoop(d.template begin<DescTerrestrialDeliverySys>()) << ";\n"
	<< "descriptor_ServiceList " << FmtLoop(d.template begin<DescServiceList>()) << ";\n"
	<< "descriptor_FrequencyList " << FmtLoop(d.template begin<DescFrequencyList>()) << ";\n"
	<< "general_descriptor " << FmtLoop(d.template begin<GeneralDescriptor>()) << ";\n"
	<< "loop " << FmtLoop(d.loop_begin()) << ";\n"
	<< "}";
}

}
}

#endif
