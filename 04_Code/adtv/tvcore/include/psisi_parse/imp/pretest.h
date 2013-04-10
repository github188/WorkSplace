//lixuejun 2005-3-8  创建
//lixuejun 2005-3-14 增加对GeneralDescriptor和私有数据的处理函数
//lixuejun 2005-3-16 增加FmtSection结构及其输出重载函数
//					 修改FmtHex输出重载函数
//					 完善FmtString输出重载函数
//lixuejun 2005-3-16 增加FmtSectionHeader结构及其输出重载函数

#ifndef NOVELSUPER_PSISI_PARSE_PRE_TEST_H_
#define NOVELSUPER_PSISI_PARSE_PRE_TEST_H_

#include <ostream>
#include <iomanip>

namespace novelsuper
{
namespace psisi_parse
{


template<typename Iterator>
struct LoopOutput {
	LoopOutput( Iterator i ) : iter(i) {}
	Iterator iter;
};

template<typename Iterator>
std::ostream& operator << ( std::ostream& os, LoopOutput<Iterator> const& d )
{
	os << "[\n";
	for( Iterator q = d.iter; ! q.empty(); ++q )
		os << *q << ";\n";
 	return os << "]";
}

template< typename Iterator >
LoopOutput<Iterator> FmtLoop( Iterator i )
{ return LoopOutput<Iterator>(i); }

inline std::string FmtString(std::string const& s)
{
	std::string output_s;
	size_t i = 0;
	size_t size = s.size();

	if(size == 0)
		return "''";

	while( i < size )
	{
		if( s[i] == '\'' )
			output_s += "'";

		output_s += s[i];
		i++;
	}

	return "'" + output_s + "'";
}

template< typename U >
struct HexOutput {
	HexOutput(U v) : v_(v) {}
	U v_;
};

template< typename U >
HexOutput<U> FmtHex( U v )
{ return HexOutput<U>(v); }

template< typename U >
std::ostream& operator << ( std::ostream& os, HexOutput<U> data )
{
	os << "0x" << std::hex << std::setfill('0') << std::setw(sizeof(U)*2);

	if(sizeof(U) == 1)
		os << (U16)data.v_;
	else
		os << data.v_;

	return os;
}

struct FmtDec {
	FmtDec( U32 v ) : v_(v) {}
	U32 v_;
};

inline std::ostream& operator << ( std::ostream& os, FmtDec data )
{
	return os << std::dec << data.v_;
}

struct FmtBinary {
	FmtBinary( U16 size, U8 const* data )
		: size_(size), data_(data)
	{}
	U16 size_;
	U8 const* data_;
};

inline std::ostream& operator << ( std::ostream& os, FmtBinary const& a )
{
	os << std::hex << std::setfill('0') << "#";
	for( U16 i=0; i < a.size_; i++ )
		os << std::setw(2) << (U16)a.data_[i];
	return os;
}

inline std::ostream& operator << ( std::ostream& os, UTCTime const& t )
{
	return os
	<< std::dec << std::setfill('0')
	<< t.year() << "-"
	<< t.month() << "-"
	<< t.day() << " "
	<< std::setw(2) << t.hour() << ":"
	<< std::setw(2) << t.minute() << ":"
	<< std::setw(2) << t.second();
}

inline std::ostream& operator << ( std::ostream& os, Duration const& d )
{
	return os
	<< std::dec << std::setfill('0')
	<< std::setw(2) << d.h() << ":"
	<< std::setw(2) << d.m() << ":"
	<< std::setw(2) << d.s();
}

struct FmtSection {
	FmtSection( std::string s, Section const& data )
		: s_(s), data_(data)
	{}
	std::string s_;
	Section const& data_;
};


struct FmtDsmccSection {
	FmtDsmccSection( std::string s, DsmccSection const& data )
		: sid_(s), data_(data)
	{}
	std::string sid_;
	DsmccSection const& data_;
};

inline std::ostream& operator << ( std::ostream& os, FmtSection const& d )
{
	os
	<< "table_id " << FmtHex( d.data_.table_id() ) << ";\n"
	<< "section_length " << FmtDec( d.data_.section_length() ) << ";\n";

	if( ! d.s_.empty() )
		os << d.s_ << " " << FmtDec( d.data_.subtable_id() ) << ";\n";

	return os
	<< "version_number " << FmtDec( d.data_.version() ) << ";\n"
	<< "current_next_indicator " << FmtDec( d.data_.current_next_indicator() ) << ";\n"
	<< "section_number " << FmtDec( d.data_.section_number() ) << ";\n"
	<< "last_section_number " << FmtDec( d.data_.last_section_number() );
}

inline std::ostream& operator << ( std::ostream& os, FmtDsmccSection const& d )
{
	os
	<< FmtSection("table_id_extension", d.data_) << ";\n"
	<< "protocol_discriminator " << FmtDec(d.data_.protocol_discriminator()) << ";\n"
	<< "dsmcc_type " << FmtDec(d.data_.dsmcc_type()) << ";\n"
	<< "message_id " << FmtDec(d.data_.message_id()) << ";\n";

	if( ! d.sid_.empty() )
		os << d.sid_ << " " << FmtHex( d.data_.transaction_id() ) << ";\n";
    return os
	<< "adaptation_length " << FmtDec(d.data_.adaptation_length()) << ";\n"
	<< "message_length " << FmtDec(d.data_.message_length());
}

inline std::ostream& operator << ( std::ostream& os, SectionHeader const& d )
{
	return os
	<< "table_id " << FmtHex( d.table_id() ) << ";\n"
	<< "section_length " << FmtDec( d.section_length() );
}


#include "commondesctest.h"

}
}

#endif
