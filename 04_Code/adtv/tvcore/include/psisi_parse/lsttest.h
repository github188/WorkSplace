//Zhao Quanjun 2005-3-10
#ifndef NOVELSUPER_PSISI_PARSE_LSTTEST_H_
#define NOVELSUPER_PSISI_PARSE_LSTTEST_H_

#include "lst.h"
#include "imp/pretest.h"

namespace novelsuper
{
namespace psisi_parse
{

inline std::ostream& operator << ( std::ostream& os, LstSectionLoop const& d )
{
	return os
	<< "{lst_section_loop\n"
	<< "component_type " << FmtDec( d.component_type()) << ";\n"
	<< "component_pid " << FmtDec( d.component_pid()) << ";\n"
	<< "ecm_pid " << FmtDec( d.ecm_pid()) << ";\n"
	<< "}";
}
	
inline std::ostream& operator << ( std::ostream& os, LstSection const& d )
{
	return os
	<< "{lstsection\n"
	<< SectionHeader(d) << ";\n"
	<< "section_no " << FmtDec( d.section_no()) << ";\n"
	<< "section_last_no " << FmtDec( d.section_last_no()) << ";\n"
	<< "card_sn " << FmtDec( d.card_sn()) << ";\n"
	<< "frequency " << FmtDec( d.frequency()) << ";\n"
	<< "fec_outer " << FmtDec( d.fec_outer()) << ";\n"
	<< "modulation " << FmtDec( d.modulation()) << ";\n"
	<< "symbol_rate " << FmtDec( d.symbol_rate()) << ";\n"
	<< "fec_inner " << FmtDec( d.fec_inner()) << ";\n"
	<< "pcr_pid " << FmtDec( d.pcr_pid()) << ";\n"
	<< "component_count " << FmtDec( d.component_count()) << ";\n"
	<< "loop " << FmtLoop( d.loop_begin()) << ";\n"
	<< "}";
}

}
}

#endif
