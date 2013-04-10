//lixuejun 2005-3-10
#ifndef NOVELSUPER_PSISI_PARSE_PMTTEST_H_
#define NOVELSUPER_PSISI_PARSE_PMTTEST_H_

#include "pmt.h"
#include "imp/pretest.h"

namespace novelsuper
{
namespace psisi_parse 
{

#include "imp/pmtdesctest.h"

inline std::ostream& operator << ( std::ostream& os, PmtSectionLoop const& d )
{
	return os
	<< "{pmt_section_loop\n"
	<< "stream_type " << FmtDec(d.stream_type()) << ";\n"
	<< "elementary_pid " << FmtDec(d.elementary_pid()) << ";\n"
	<< "es_info_length " << FmtDec(d.descs_len()) << ";\n"
	<< "descriptor_VideoStream " << FmtLoop(d.begin<DescVideoStream>()) << ";\n"
	<< "descriptor_AudioStream " << FmtLoop(d.begin<DescAudioStream>()) << ";\n"
	<< "descriptor_AC3 " << FmtLoop(d.begin<DescAC3>()) << ";\n"
	<< "descriptor_DataBroadcastId " << FmtLoop(d.begin<DescDataBroadcastId>()) << ";\n"
	<< "descriptor_StreamIdentifier " << FmtLoop(d.begin<DescStreamIdentifier>()) << ";\n"
	<< "descriptor_Subtitling " << FmtLoop(d.begin<DescSubtitling>()) << ";\n"
	<< "descriptor_Teletext " << FmtLoop(d.begin<DescTeletext>()) << ";\n"
	<< "descriptor_MaxBitrate " << FmtLoop(d.begin<DescMaxBitrate>()) << ";\n"
	<< "descriptor_SmoothingBuffer " << FmtLoop(d.begin<DescSmoothingBuffer>()) << ";\n"
	<< "descriptor_Iso639Language " << FmtLoop(d.begin<DescIso639Language>()) << ";\n"
	<< "descriptor_CA " << FmtLoop(d.begin<DescCA>()) << ";\n"
	<< "descriptor_PrivateDataSpecifier " << FmtLoop(d.begin<DescPrivateDataSpecifier>()) << ";\n"
	<< "general_descriptor " << FmtLoop(d.begin<GeneralDescriptor>()) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, PmtSection const& d )
{
	return os
	<< "{pmtsection\n"
	<< FmtSection( "program_number", d ) << ";\n"
	<< "pcr_pid " << FmtDec(d.pcr_pid()) << ";\n"
	<< "program_info_length " << FmtDec(d.descs_len()) << ";\n"
	<< "descriptor_CA " << FmtLoop(d.begin<DescCA>()) << ";\n"
	<< "descriptor_Iso639Language " << FmtLoop(d.begin<DescIso639Language>()) << ";\n"
	<< "descriptor_SystemClock " << FmtLoop(d.begin<DescSystemClock>()) << ";\n"
	<< "descriptor_Mosaic " << FmtLoop(d.begin<DescMosaic>()) << ";\n"
	<< "descriptor_MaxBitrate " << FmtLoop(d.begin<DescMaxBitrate>()) << ";\n"
	<< "descriptor_SmoothingBuffer " << FmtLoop(d.begin<DescSmoothingBuffer>()) << ";\n"
	<< "descriptor_PrivateDataSpecifier " << FmtLoop(d.begin<DescPrivateDataSpecifier>()) << ";\n"
	<< "general_descriptor " << FmtLoop(d.begin<GeneralDescriptor>()) << ";\n"
	<< "loop " << FmtLoop(d.loop_begin()) << ";\n"
	<< "}";
}

template< typename Data, typename Traits >
std::ostream& operator << ( std::ostream& os, Pmt<Data, Traits> const& d )
{
	return os
	<< "{pmt\n"
	<< "sections " << FmtLoop(d.section_begin()) << ";\n"
	<< "descriptor_Iso639Language " << FmtLoop(d.template begin<DescIso639Language>()) << ";\n"
	<< "descriptor_SystemClock " << FmtLoop(d.template begin<DescSystemClock>()) << ";\n"
	<< "descriptor_Mosaic " << FmtLoop(d.template begin<DescMosaic>()) << ";\n"
	<< "descriptor_MaxBitrate " << FmtLoop(d.template begin<DescMaxBitrate>()) << ";\n"
	<< "descriptor_SmoothingBuffer " << FmtLoop(d.template begin<DescSmoothingBuffer>()) << ";\n"
	<< "descriptor_PrivateDataSpecifier " 
		<< FmtLoop(d.template begin<DescPrivateDataSpecifier>()) << ";\n"
	<< "descriptor_VideoStream " << FmtLoop(d.template begin<DescVideoStream>()) << ";\n"
	<< "descriptor_AudioStream " << FmtLoop(d.template begin<DescAudioStream>()) << ";\n"
	<< "descriptor_AC3 " << FmtLoop(d.template begin<DescAC3>()) << ";\n"
	<< "descriptor_DataBroadcastId " << FmtLoop(d.template begin<DescDataBroadcastId>()) << ";\n"
	<< "descriptor_StreamIdentifier " << FmtLoop(d.template begin<DescStreamIdentifier>()) << ";\n"
	<< "descriptor_Subtitling " << FmtLoop(d.template begin<DescSubtitling>()) << ";\n"
	<< "descriptor_Teletext " << FmtLoop(d.template begin<DescTeletext>()) << ";\n"
	<< "descriptor_CA " << FmtLoop(d.template begin<DescCA>()) << ";\n"
	<< "general_descriptor " << FmtLoop(d.template begin<GeneralDescriptor>()) << ";\n"
	<< "loop " << FmtLoop(d.loop_begin()) << ";\n"
	<< "}";
}

}
}

#endif
