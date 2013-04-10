#include "xmlparser.h"

namespace NovelSupertv
{
namespace stbruntime
{

// <?xml version="1.0"?>
inline bool is_version( std::string const& tag )
{
    if( tag.find( "?xml" ) != std::string::npos )
        return true;
    return false;
}
// <!-- -->
inline bool is_help( std::string const& tag )
{
	if( tag.find( "!--" ) != std::string::npos )
		return true;
	return false;
}

// </tag>
inline bool is_end( std::string const& tag )
{
    if( !tag.empty() && *tag.begin() == '/' )
        return true;
    return false;
}

// <tag/>
inline bool is_element( std::string const& tag )
{
    if( !tag.empty() && *tag.rbegin() == '/' )
        return true;
    return false;
}

inline void reArrange(std::string &s)
{
	for(unsigned int i=0; i<s.size(); i++)
		if(s[i]=='\n'||s[i]=='\r'||s[i]=='\t')
			s[i]=' ';
}

inline std::string property_name( std::string const& tag,
    std::string::size_type s_pos, std::string::size_type e_pos )
{
    std::string::size_type p1 = tag.find_first_not_of( ' ', s_pos + 1 );
    std::string::size_type p2 = tag.find( ' ', p1 );
    if( p2 == std::string::npos || p2 >  e_pos )
        return tag.substr( p1, e_pos - p1 );
    return tag.substr( p1, p2 - p1 );
}

void Parser::parse_property( std::string const& tag )
{
    std::vector<Property> propertys;
    std::string::size_type pos = tag.find( ' ' );
    if( pos == std::string::npos )
    {
        on_start( tag, propertys );
        return;
    }
    std::string tag_name = tag.substr( 0, pos );

    std::string::size_type  p1 = tag.find( '=', pos + 1 );
    while( p1 != std::string::npos 
        && pos != std::string::npos )
    {
        std::string name = property_name( tag, pos, p1 );
        std::string::size_type p2 = tag.find( '\x22', p1 );//find "
        if( p2 != std::string::npos )
        {
            std::string::size_type p3 = tag.find( '\x22', p2 + 1 );
            if( p3 != std::string::npos )
            {
                std::string value( tag.substr( p2 + 1, p3 - p2 - 1 ) );
                propertys.push_back( Property( name, value ) );
                pos = tag.find( ' ', p3 );
            }
            else
                pos = tag.find( ' ', p2 );
        }
        else if( ( p2 = tag.find( '\x27', p1 ) ) != std::string::npos )//find '
        {
            std::string::size_type p3 = tag.find( '\x27', p2 + 1 );
            if( p3 != std::string::npos )
            {
                std::string value( tag.substr( p2 + 1, p3 - p2 - 1 ) );
                propertys.push_back( Property( name, value ) );
                pos = tag.find( ' ', p3 );
            }
            else
                pos = tag.find( ' ', p2 );
        }
        else
        {
           pos = tag.find( ' ', p1 );
        }
        p1 = tag.find( '=', pos + 1 );
    }
    on_start( tag_name, propertys );
}

void Parser::parse( const char *str, int len, bool end )
{
    const char *p = str;
    const char *pos = p;
    const char *s = 0;//数据开始位置
    const char *e = 0;//数据结束位置
    while( len > 0 )
    {
        if( *p == '<' )
        {
            pos = p;
            if( s )
               e = p;//记下数据结束位置
        }
        else if( *p=='>' )
        {
            std::string tag( pos + 1, p - pos - 1 );
			reArrange(tag);

            if( is_version( tag ) )
                ;//std::cout << tag << "\n";
			else if(is_help(tag))
				;
            else if( is_end( tag ) )
            {
                if( s && e )
                {
                    std::string data( s + 1, e -s - 1 );
                    on_data( data );
                    s = e = 0;
                }
                on_end( tag );
            }
            else if( is_element( tag ) )
            {
                parse_property( tag );
                on_end( tag );
            }
            else
            {
				
                parse_property( tag );
                s = p;//记下数据开位置
            }
        }
        p++;
        len--;
    }
}

void Parser::on_start( std::string const& name,
    std::vector<Property> const& propertys )
{
}

void Parser::on_data( std::string const& data )
{
}

void Parser::on_end( std::string const& name )
{
}

void ParseXMLData( const char *data, int len, Parser & parser )
{
    parser.parse( data, len );
}

}
}
