//Zhao Quanjun 2005-12-2
#ifndef NOVELSUPER_PSISI_PARSE_DCTTEST_H_
#define NOVELSUPER_PSISI_PARSE_DCTTEST_H_

#include "dct.h"
#include "imp/pretest.h"

namespace novelsuper
{
namespace psisi_parse
{

#include "imp/dctdesctest.h"

inline std::ostream& operator << ( std::ostream& os, DctFileInfoLoop const& d )
{
	return os
	<< "{dct_fileinfo_loop\n"
	<< "ddt_tableid " << FmtDec(d.ddt_tableid()) << ";\n"
	<< "descriptor_FileInfo " << FmtLoop(d.begin<DescFileInfo>()) << ";\n"
	<< "general_descriptor " << FmtLoop(d.begin<GeneralDescriptor>()) << ";\n"
	<< "}";
}


inline std::ostream& operator << ( std::ostream& os, DctSoftwareLoop const& d )
{
	return os
	<< "{dct_software_loop\n"
	<< "descriptor_Software " << FmtLoop(d.begin<DescSoftware>()) << ";\n"
	<< "files_length " << FmtDec(d.files_length()) << ";\n"
	<< "general_descriptor " << FmtLoop(d.begin<GeneralDescriptor>()) << ";\n"
	<< "loop " << FmtLoop(d.loop_begin()) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, DctSectionLoop const& d )
{
	return os
	<< "{dct_section_loop\n"
	<< "descriptor_Hardware " << FmtLoop(d.begin<DescHardware>()) << ";\n"
	<< "descriptor_Net " << FmtLoop(d.begin<DescNet>()) << ";\n"
	<< "pid_of_ddt " << FmtDec(d.pid_of_ddt()) << ";\n"
	<< "software_length " << FmtDec(d.software_length()) << ";\n"
	<< "general_descriptor " << FmtLoop(d.begin<GeneralDescriptor>()) << ";\n"
	<< "loop " << FmtLoop(d.loop_begin()) << ";\n"
	<< "}";
}

inline std::ostream& operator << ( std::ostream& os, DctSection const& d )
{
	U8 const* pData;
	U16 len = d.dct_rsa_signature( pData );
	return os
	<< "{dctsection\n"
	<< FmtSection( "hardware_version", d ) << ";\n"
	<< "force_upgrade_flag " << FmtDec(d.force_upgrade_flag()) << ";\n"
	<< "hardware_length " << FmtDec(d.hardware_length()) << ";\n"
	<< "dct_rsa_signature " << FmtBinary(len,pData) << ";\n"
	<< "loop " << FmtLoop(d.loop_begin()) << ";\n"
	<< "}";
}

template< typename Data, typename Traits >
std::ostream& operator << ( std::ostream& os, Dct<Data, Traits> const& d )
{
	return os
	<< "{dct\n"
	<< "sections " << FmtLoop(d.section_begin()) << ";\n"
	<< "loop " << FmtLoop(d.loop_begin()) << ";\n"
	<< "}";
}

}
}

#endif
