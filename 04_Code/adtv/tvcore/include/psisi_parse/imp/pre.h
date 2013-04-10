//lixuejun 2005-3-8  创建
//lixuejun 2005-3-14 增加UTCTime、Duration的实现
//lixuejun 2005-3-18 增加CRC校验函数

#ifndef NOVELSUPER_PSISI_PARSE_PRE_H_
#define NOVELSUPER_PSISI_PARSE_PRE_H_

#include <string>
#include <iterator>
#include <stdexcept>

namespace novelsuper
{
namespace psisi_parse
{

enum DVBTable{
	Table_Pat = 0x00,
	Table_Cat = 0x01,
	Table_Pmt = 0x02,
	Table_NitA = 0x40,
	Table_NitO = 0x41,
	Table_SdtA = 0x42,
	Table_SdtO = 0x46,
	Table_Bat = 0x4A,
	Table_Tdt = 0x70,
	Table_Tot = 0x73,
	Table_DsiDii = 0x3B,
	Table_Ddb = 0x3C
};
typedef unsigned char U8;
typedef unsigned short U16;
typedef unsigned int U32;
typedef signed char S8;
typedef signed short S16;
typedef signed int S32;

inline U16 n2h16( U8 const* d )
{
	return d[0] * 256 + d[1];
}

inline U16 n2h( U8 const (&d)[2] )
{
	return n2h16(d);
}

inline U32 n2h32( U8 const* d )
{
	return (((d[0] * 256 + d[1]) * 256) + d[2]) * 256 + d[3];
}

inline U32 n2h( U8 const (&d)[4] )
{
	return n2h32(d);
}

struct UTCTime
{
	U16 year() const
	{
		U16 y, m, d;
		MJDConvert(y, m, d);
		return y;
	}

	U16 month() const
	{
		U16 y, m, d;
		MJDConvert(y, m, d);
		return m;
	}

	U16 day() const
	{
		U16 y, m, d;
		MJDConvert(y, m, d);
		return d;
	}

	U16 hour() const
	{
		return (utctime[2] >> 4) * 10 + (utctime[2] & 0x0F);
	}

	U16 minute() const
	{
		return (utctime[3] >> 4) * 10 + (utctime[3] & 0x0F);
	}

	U16 second() const
	{
		return (utctime[4] >> 4) * 10 + (utctime[4] & 0x0F);
	}

	U8 utctime[5];

private:
	void MJDConvert( U16& y, U16& m, U16& d ) const
	{
		U16 MJD = n2h16(utctime);
		U16 y1 = U16( ( MJD * 100 - 1507820 ) / 36525);
		U16 m1 = U16( ( MJD * 10000 - 149561000 - U16( y1 * 3652500 )) / 306001 );
		d = MJD - 14956 - U16( y1 * 36525 / 100 ) - U16( m1 * 306001 / 10000);

		U16 k = 1;
		if ( m1 == 14 || m1 == 15 )
			k = 1;
		else
			k = 0;

		y = 1900 + y1 + k;
		m = m1 -1 -k * 12;
	}
};

struct Duration
{
	U16 h() const
	{
		return (duration[0] >> 4) * 10 + (duration[0] & 0x0F);
	}

	U16 m() const
	{
		return (duration[1] >> 4) * 10 + (duration[1] & 0x0F);
	}

	U16 s() const
	{
		return (duration[2] >> 4) * 10 + (duration[2] & 0x0F);
	}

	U16 totalseconds() const
	{
		return h() * 3600 + m() * 60 + s();
	}

	U8 duration[3];
};

inline U32 CRC( U8 const* pData, U16 nDataLen )
{
	U8 bitbase[9] =	{ 0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01, 0x00 };
	if( NULL == pData || 0 == nDataLen )
		return 0;
	U32 crc = 0xFFFFFFFF;
	for( U16 i = 0; i < nDataLen; ++ i )
	{
		U8 data = pData[i];
		for( int j = 0; j <= 7; ++ j )
		{
			int temp = ((crc & 0x80000000) != 0) ^ ((data & bitbase[j]) != 0);
			crc  <<= 1;
			if( temp != 0 )
				crc ^= 0x04C11DB7; // 0x04C11DB7 的二进制表示为0000 0100 1100 0001 0001 1101 1011 0111
		}
	}
	return crc;
}

//exception
struct LogicError : std::logic_error
{
	LogicError(std::string const& s)
		: std::logic_error(s)
	{}
};

struct RuntimeError : std::runtime_error
{
	RuntimeError(std::string const& s)
		: std::runtime_error(s)
	{}
};

inline void ThrowLogicError(std::string const& s)
{
	throw LogicError(s);
}

inline void ThrowRuntimeError(std::string const& s)
{
	throw RuntimeError(s);
}

//template and marcos
template< typename Descriptor >
struct forward_iterator :
	std::iterator<std::forward_iterator_tag,Descriptor>
	{};

template< typename Desc >
struct DescList_Iterator : forward_iterator<Desc>
{
	DescList_Iterator( U8 const* b=0, U16 len=0 )
	: begin_(b), end_(b + len)
	{
		begin_ = Desc::first_desc(begin_,end_);
	}

	DescList_Iterator& operator ++ ()
	{
		begin_ = Desc::first_desc( begin_ + begin_[1] + 2, end_ );
		return *this;
	}

	DescList_Iterator operator ++ (int)
	{
		DescList_Iterator old( *this );
		++*this;
		return old;
	}

	Desc const& operator* () const
	{
		return *reinterpret_cast<Desc const*>(begin_);
	}

	Desc const* operator-> () const
	{
		return reinterpret_cast<Desc const*>(begin_);
	}

	bool operator == (DescList_Iterator const& other) const
	{
		return begin_ == other.begin_;
	}

	bool operator != (DescList_Iterator const& other) const
	{
		return !(*this == other);
	}

	bool empty() const
	{
		if( begin_ > end_ )
			ThrowRuntimeError("DescList_Iterator,out of range.");
		return begin_ == end_;
	}

private:
	U8 const* begin_;//begin_指向一串Desc的开始
	U8 const* end_;  //end_指向一串Desc的最后一个字节＋1
};

template< typename Loop >
struct Loop_Iterator_Imp : forward_iterator<Loop>
{
	Loop_Iterator_Imp( U8 const * b = 0, U16 len = 0 )
	: begin_(b), end_(b + len)
	{}

	Loop_Iterator_Imp& operator ++ ()
	{
		if(! empty() )
			begin_ += reinterpret_cast<Loop const*>(begin_)->length();
		return *this;
	}

	Loop const& operator* () const
	{
		return *reinterpret_cast<Loop const*>(begin_);
	}

	Loop const* operator-> () const
	{
		return reinterpret_cast<Loop const*>(begin_);
	}

	bool operator == (Loop_Iterator_Imp const& other) const
	{
		return begin_ == other.begin_;
	}

	bool operator != (Loop_Iterator_Imp const& other) const
	{
		return !(*this == other);
	}

	bool empty() const
	{
		if( begin_ > end_ )
			ThrowRuntimeError("Loop_Iterator_Imp,out of range.");
		return begin_ == end_;
	}

private:
	U8 const* begin_;//begin_指向一串Loop的开始
	U8 const* end_;//end_指向一串Loop的最后一个字节＋1
};

//通用的Descriptor_Iterator
#define NOVEL_TF_PSISI_PARSE_GENERAL_DESC_ITERATOR_BEGIN_END     \
	template<typename Desc>\
	DescList_Iterator<Desc> begin() const\
	{ return DescList_Iterator<Desc>(descs_address(), descs_len()); }\
	template<typename Desc>\
	DescList_Iterator<Desc> end() const\
	{ return DescList_Iterator<Desc>(descs_address() + descs_len(), 0); }

//通用的Loop_Iterator
#define NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(LOOP)     \
	typedef Loop_Iterator_Imp<LOOP> Loop_Iterator;\
	Loop_Iterator_Imp<LOOP> loop_begin() const\
	{ return Loop_Iterator_Imp<LOOP>(loop_address(), loop_len()); }\
	Loop_Iterator_Imp<LOOP> loop_end() const\
	{ return Loop_Iterator_Imp<LOOP>(loop_address() + loop_len(), 0); }

template< typename Section, typename Desc >
struct DescTraits{
	typedef DescList_Iterator<Desc> Iterator;
	typedef Desc Item;
	static Iterator find_first_in_section(Section const& s)
		{ return s.template begin<Desc>(); }

};

template< typename Section, typename Loop >
struct LoopTraits{
	typedef Loop_Iterator_Imp<Loop> Iterator;
	typedef Loop Item;
	static Iterator find_first_in_section(Section const& s)
		{  return s.loop_begin(); }
};

template< typename Section, typename Traits, typename ItemTraits >
struct CrossSection_List_Iterator : forward_iterator<typename ItemTraits::Item>
{
	typedef typename ItemTraits::Item Item;
	typedef typename ItemTraits::Iterator ItemIterator;

	CrossSection_List_Iterator( typename Traits::iterator F, typename Traits::iterator L )
	: first_(F), last_(L), item_(0,0)
	{
		while( first_ != last_ ) {
			item_ = find_first_item();
			if( ! item_.empty() )
				break;
			++first_;
		}
	}

	CrossSection_List_Iterator& operator ++ ()
	{
		++item_;
		while( first_ != last_ && item_.empty() ) {
			if(++first_ == last_)
				break;
			item_ = find_first_item();
		}
		return *this;
	}

	CrossSection_List_Iterator operator ++ (int)
	{
		CrossSection_List_Iterator old( *this );
		++*this;
		return old;
	}

	Item const& operator* () const
	{
		return *item_;
	}

	Item const* operator-> () const
	{
		return &*item_;
	}

	bool operator == (CrossSection_List_Iterator const& other) const
	{
		return item_ == other.item_;
	}

	bool operator != (CrossSection_List_Iterator const& other) const
	{
		return !(*this == other);
	}

	bool empty() const
	{
		return item_.empty();
	}

private:
	typename Traits::iterator first_, last_;
	ItemIterator item_;

	ItemIterator find_first_item() const
	{
		Section const& s = *reinterpret_cast<Section const*>(Traits::section_data(first_));
		if( s.section_length() + 3 != Traits::section_size(first_) )
			ThrowRuntimeError("CrossSection_List_Iterator::find_first_item,bad section length");
		return ItemTraits::find_first_in_section( s );
	}
};

template< typename Data >
struct DefaultSectionsTraits {
	typedef Data data_type;
	typedef typename Data::const_iterator iterator;
	static iterator begin( Data const& d ) { return d.begin(); }
	static iterator end( Data const& d ) { return d.end(); }
	static U8 const* section_data( iterator i )
		{ return reinterpret_cast<U8 const*>(&(*i)[0]); }
	static U16 section_size( iterator i ) { return static_cast<U32>(i->size()); }
};

template< typename Section, typename Traits >
struct CastIterator {
	typedef typename Traits::iterator data_iterator;
	CastIterator( data_iterator b, data_iterator e )
		: begin_(b), end_(e)
		{}
	Section const* operator -> () const
		{ return reinterpret_cast<Section const*>( Traits::section_data(begin_) ); }
	Section const& operator * () const
		{ return *operator->(); }
	CastIterator& operator ++ ()
		{
			++begin_;
			return *this;
		}
	CastIterator operator ++ ( int )
		{
			CastIterator old(*this);
			++*this;
			return old;
		}
	bool operator == ( CastIterator const& other ) const
		{ return begin_ == other.begin_; }
	bool operator != ( CastIterator const& other ) const
		{ return ! operator == (other); }
	bool empty() const
	{
		return begin_ == end_;
	}
private:
	data_iterator begin_, end_;
};

//通用的跨Section Desc Iterator
#define NOVEL_TF_PSISI_PARSE_GENERAL_CROSS_SECTION_DESC_ITERATOR(SECTION)     \
	template< typename Desc >\
	struct DescIterator {\
		typedef CrossSection_List_Iterator<SECTION,Traits, DescTraits<SECTION, Desc> > type;\
	};\
	template< typename Desc >\
	typename DescIterator<Desc>::type begin() const\
	{ return typename DescIterator<Desc>::type(Traits::begin(data_), Traits::end(data_));}\
	template< typename Desc >\
	typename DescIterator<Desc>::type end() const\
	{ return typename DescIterator<Desc>::type(Traits::end(data_), Traits::end(data_));}

//通用的跨Section Loop Iterator
#define NOVEL_TF_PSISI_PARSE_GENERAL_CROSS_SECTION_LOOP_ITERATOR(SECTION,LOOP)     \
	typedef CrossSection_List_Iterator<SECTION,Traits,LoopTraits<SECTION, LOOP> > \
		Loop_Iterator;\
	Loop_Iterator loop_begin() const\
	{ return Loop_Iterator(Traits::begin(data_), Traits::end(data_));}\
	Loop_Iterator loop_end() const\
	{ return Loop_Iterator(Traits::end(data_), Traits::end(data_));}

//通用的跨Section Section Iterator
#define NOVEL_TF_PSISI_PARSE_GENERAL_SECTION_ITERATOR(SECTION, TRAITS)     \
	typedef CastIterator<SECTION,TRAITS> section_iterator; \
	section_iterator section_begin() const \
	{ return section_iterator(TRAITS::begin(data_),TRAITS::end(data_)); } \
	section_iterator section_end() const \
	{ return section_iterator(TRAITS::end(data_),TRAITS::end(data_)); }

//Descriptors
struct DescBase
{
	U8 tag() const{ return tag_; }
	U8 length() const{ return length_; }
	U8 const* data() const{ return &length_ + 1; }

protected:
	U8 tag_;
	U8 length_;
};

struct GeneralDescriptor : DescBase
{
	static U8 const* first_desc ( U8 const* data, U8 const* )
	{
		return data;
	}
};

template<U8 TAG>
struct Descriptor : DescBase
{
	enum { Tag = TAG };

	static U8 const* first_desc( U8 const* data, U8 const* end )
	{
		while( data != end && data[0] != Tag )
		{
			if( end < data + 2 || end < data + data[1] + 2 )
				ThrowRuntimeError("Descriptor::first_desc");
			data += data[1] + 2;
		}
		return data;
	}
};

//section
class SectionHeader {
public:
	U8 table_id() const{ return table_id_; }
	U16 section_length() const
	{
		int len = n2h(section_length_) & 0xfff;
		bool lengtherror = false;
		switch(table_id_){
		case Table_Pat:
		case Table_Cat:
			{
				if(len < 9)
					lengtherror = true;
			}
			break;
		case Table_Pmt:
		case Table_NitA:
		case Table_NitO:
		case Table_Bat:
			{
				if(len < 13)
					lengtherror = true;
			}
			break;
		case Table_SdtA:
		case Table_SdtO:
			{
				if(len < 12)
					lengtherror = true;
			}
			break;
		case Table_Tdt:
			{
				if( len < 5 )
					lengtherror = true;
			}
			break;
		case Table_Tot:
			{
				if(len < 11)
					lengtherror = true;
			}
			break;
		case Table_DsiDii:
			{
				if(len < 8)
					lengtherror = true;
			}
			break;
		case Table_Ddb:
			{
				if(len < 8)
					lengtherror = true;
			}
			break;
		default:
			if( table_id_ >= 0x4e  && table_id_ <= 0x6f ){
				if(len < 15)
					lengtherror = true;
			}else{
				ThrowRuntimeError("Parse, File content format error");
			}
			break;
		}
		if(lengtherror)
			ThrowRuntimeError("SectionHeader::section_length,too short");
		return len;
	}
protected:
	U8 table_id_;
	U8 section_length_[2];
};

class Section :  public SectionHeader
{
public:	
	U16 subtable_id() const{ return n2h(subtable_id_); }
	U8 version() const{ return (version_ & 0x3E) >> 1; }
	U8 current_next_indicator() const{ return (version_ & 0x01); }
	U8 section_number() const{ return section_number_; }
	U8 last_section_number() const{ return last_section_number_; }
	U8 const* CRC_32() const
	{
		if( section_length() < 4 )
			ThrowRuntimeError("Section, CRC_32, Section Length < 4");
		return subtable_id_ + section_length() - 4;
	}

	bool CRC_OK() const
	{
		if( section_length() < 1 )
			ThrowRuntimeError("Section, CRC_OK, Length < 1");
		return ( n2h32(CRC_32()) == CRC( &table_id_, section_length() - 1 ) );
	}

protected:
	U8 subtable_id_[2];
	U8 version_;
	U8 section_number_;
	U8 last_section_number_;
	U8 const* data() const{ return &last_section_number_ + 1; }
};


struct DsmccSection : Section
{
	U16 table_id_extension() const{ return subtable_id(); }
	U8  protocol_discriminator() const{ return protocol_discriminator_;}
	U8  dsmcc_type() const{ return dsmcc_type_;}
	U16 message_id() const{ return n2h(message_id_);}
	U32 transaction_id() const{ return n2h32(transaction_id_);}
	U8  adaptation_length() const{ return adaptation_length_;}
	U16 message_length() const{ return n2h(message_length_);}
protected:
	U8 protocol_discriminator_;
    U8 dsmcc_type_;
	U8 message_id_[2];
	U8 transaction_id_[4];
	U8 reserverd_;
	U8 adaptation_length_;
	U8 message_length_[2];
};

//装载多个section的数据
template<typename InputIterator, typename Sections>
bool LoadSections(InputIterator first, InputIterator last, Sections &secs )
{
	typename Sections::value_type buffer;

	while( first != last )
	{
		buffer.clear();

		for(int i=0; i<3; i++)
		{
			if(first == last)
				return false;
			else
				buffer.push_back(*first++);
		}
		unsigned short section_length = ( (unsigned char)(buffer[1]) * 256 +
			(unsigned char)(buffer[2]) ) & 0xfff;

		for(int i=0; i<section_length; i++)
		{
			if(first == last)
				return false;
			else
				buffer.push_back(*first++);
		}
		secs.push_back(buffer);
	}

	return true;
}

template<typename Section>
Section const& Cast(void const * data, size_t size)
{
	if(size < 3)
		ThrowRuntimeError("Section::section_length too short");
	Section const& sec = *reinterpret_cast<Section const*>(data);
	int len = sec.section_length();
	if(len + 3 != size)
		ThrowRuntimeError("Section::section_length + 3 != data size");
	return sec;
}

template<typename Section, typename Container>
Section const & Cast(Container const& data)
{
	return Cast<Section>(&data[0], data.size());
}

}
}
#endif
