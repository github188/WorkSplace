//Zhao Quanjun 2005-3-10
#ifndef NOVELSUPER_PSISI_PARSE_EITTEST_H_
#define NOVELSUPER_PSISI_PARSE_EITTEST_H_

#include "eit.h"
#include "imp/pretest.h"

namespace novelsuper
{
namespace psisi_parse
{

#include "imp/eitdesctest.h"

inline std::ostream& operator << ( std::ostream& os, EitSectionLoop const& d )
{
	return os
	<< "{eit_section_loop\n"
	<< "event_id " << FmtDec(d.event_id()) << ";\n"
	<< "start_time " << (UTCTime)d.start_time() << ";\n"
	<< "duration " << (Duration)d.duration() << ";\n"
	<< "running_status " << FmtDec(d.running_status()) << ";\n"
	<< "free_CA_mode " << FmtDec(d.free_ca_mode()) << ";\n"
	<< "descriptor_ShortEvent " << FmtLoop(d.begin<DescShortEvent>()) << ";\n"
	<< "descriptor_DescExtendedEvent " << FmtLoop(d.begin<DescExtendedEvent>()) << ";\n"
	<< "descriptor_TimeShiftedEvent " << FmtLoop(d.begin<DescTimeShiftedEvent>()) << ";\n"
	<< "descriptor_Component " << FmtLoop(d.begin<DescComponent>()) << ";\n"
	<< "descriptor_Content " << FmtLoop(d.begin<DescContent>()) << ";\n"
	<< "descriptor_ParentalRating " << FmtLoop(d.begin<DescParentalRating>()) << ";\n"
	<< "descriptor_MultComponent " << FmtLoop(d.begin<DescMultComponent>()) << ";\n"
	<< "descriptor_ShortSmoothingBuffer " << FmtLoop(d.begin<DescShortSmoothingBuffer>()) << ";\n"
	<< "descriptor_PrivateDataSpecifier " << FmtLoop(d.begin<DescPrivateDataSpecifier>()) << ";\n"
	<< "general_descriptor " << FmtLoop(d.begin<GeneralDescriptor>()) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, EitSection const& d )
{
	return os
	<< "{eitsection\n"
	<< FmtSection("service_id", d) << ";\n"
	<< "transport_stream_id " << FmtDec(d.transport_stream_id()) << ";\n"
	<< "original_network_id " << FmtDec(d.original_network_id()) << ";\n"
	<< "segment_last_section_number " << FmtDec(d.segment_last_section_number()) << ";\n"
	<< "last_table_id " << FmtDec(d.last_table_id()) << ";\n"
	<< "loop " << FmtLoop(d.loop_begin()) << ";\n"
	<< "}";
}

template< typename Data, typename Traits >
std::ostream& operator << ( std::ostream& os, Eit<Data, Traits> const& d )
{
	return os
	<< "{eit\n"
	<< "sections " << FmtLoop(d.section_begin()) << ";\n"
	<< "descriptor_ShortEvent " << FmtLoop(d.template begin<DescShortEvent>()) << ";\n"
	<< "descriptor_DescExtendedEvent " << FmtLoop(d.template begin<DescExtendedEvent>()) << ";\n"
	<< "descriptor_TimeShiftedEvent " << FmtLoop(d.template begin<DescTimeShiftedEvent>()) << ";\n"
	<< "descriptor_Component " << FmtLoop(d.template begin<DescComponent>()) << ";\n"
	<< "descriptor_Content " << FmtLoop(d.template begin<DescContent>()) << ";\n"
	<< "descriptor_ParentalRating " << FmtLoop(d.template begin<DescParentalRating>()) << ";\n"
	<< "descriptor_MultComponent " << FmtLoop(d.template begin<DescMultComponent>()) << ";\n"
	<< "descriptor_ShortSmoothingBuffer " << FmtLoop(d.template begin<DescShortSmoothingBuffer>()) << ";\n"
	<< "descriptor_PrivateDataSpecifier " << FmtLoop(d.template begin<DescPrivateDataSpecifier>()) << ";\n"
	<< "general_descriptor " << FmtLoop(d.template begin<GeneralDescriptor>()) << ";\n"
	<< "loop " << FmtLoop(d.loop_begin()) << ";\n"
	<< "}";
}


}
}

#endif
