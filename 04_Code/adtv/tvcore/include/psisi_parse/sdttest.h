//Zhao Quanjun 2005-3-10
#ifndef NOVELSUPER_PSISI_PARSE_SDTTEST_H_
#define NOVELSUPER_PSISI_PARSE_SDTTEST_H_

#include "sdt.h"
#include "imp/pretest.h"

namespace novelsuper
{
namespace psisi_parse
{

#include "imp/sdtdesctest.h"

inline std::ostream& operator << ( std::ostream& os, SdtSectionLoop const& d )
{
	return os
	<< "{sdt_section_loop\n"
	<< "service_id " << FmtDec(d.service_id()) << ";\n"
	<< "EIT_schedule_flag "<< FmtDec(d.eit_schedule_flag()) << ";\n"
	<< "EIT_present_following_flag " << FmtDec(d.eit_present_follow_flag()) << ";\n"
	<< "running_status " << FmtDec(d.running_status()) << ";\n"
	<< "free_CA_mode " << FmtDec(d.free_ca_mode()) << ";\n"
	<< "descriptor_Service " << FmtLoop(d.begin<DescService>()) << ";\n"
	<< "descriptor_NvodReference " << FmtLoop(d.begin<DescNvodReference>()) << ";\n"
	<< "descriptor_TimeShiftedService " << FmtLoop(d.begin<DescTimeShiftedService>()) << ";\n"
	<< "descriptor_Mosaic " << FmtLoop(d.begin<DescMosaic>()) << ";\n"
	<< "descriptor_MultServiceName " << FmtLoop(d.begin<DescMultServiceName>()) << ";\n"
	<< "descriptor_AnnouncementSupport " << FmtLoop(d.begin<DescAnnouncementSupport>()) << ";\n"
	<< "descriptor_PrivateDataSpecifier " << FmtLoop(d.begin<DescPrivateDataSpecifier>()) << ";\n"
	<< "general_descriptor " << FmtLoop(d.begin<GeneralDescriptor>()) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, SdtSection const& d )
{
	return os
	<< "{sdtsection\n"
	<< FmtSection("transport_stream_id", d) << ";\n"
	<< "original_network_id " << FmtDec(d.original_network_id()) << ";\n"
	<< "loop " << FmtLoop(d.loop_begin()) << ";\n"
	<< "}";
}

template< typename Data, typename Traits >
std::ostream& operator << ( std::ostream& os, Sdt<Data, Traits>  const& d )
{
	return os
	<< "{sdt\n"
	<< "sections " << FmtLoop(d.section_begin()) << ";\n"
	<< "descriptor_Service " << FmtLoop(d.template begin<DescService>()) << ";\n"
	<< "descriptor_NvodReference " << FmtLoop(d.template begin<DescNvodReference>()) << ";\n"
	<< "descriptor_TimeShiftedService " << FmtLoop(d.template begin<DescTimeShiftedService>()) << ";\n"
	<< "descriptor_Mosaic " << FmtLoop(d.template begin<DescMosaic>()) << ";\n"
	<< "descriptor_MultServiceName " << FmtLoop(d.template begin<DescMultServiceName>()) << ";\n"
	<< "descriptor_AnnouncementSupport " << FmtLoop(d.template begin<DescAnnouncementSupport>()) << ";\n"
	<< "descriptor_PrivateDataSpecifier " << FmtLoop(d.template begin<DescPrivateDataSpecifier>()) << ";\n"
	<< "general_descriptor " << FmtLoop(d.template begin<GeneralDescriptor>()) << ";\n"
	<< "loop " << FmtLoop(d.loop_begin()) << ";\n"
	<< "}";
}


}
}

#endif
