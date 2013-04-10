/** 
 * \file xmlparser.h
 * \brief XML解析器类型定义
 *
 * Copyright(c) 2007-2009 Novel-SuperTV, All rights reserved.
 *
 * Date        Author           Modification\n
 * ----------------------------------------------------------------\n
 * 2007-10-11  Fuwenchao        Created\n
 */
#ifndef NOVELSUPERTV_STBRT_XMLPARSER_H
#define NOVELSUPERTV_STBRT_XMLPARSER_H

#include <string>
#include <vector>

namespace NovelSupertv
{
namespace stbruntime
{

struct Property
{
    Property( std::string const& n, std::string const& v )
    : name( n )
    , value( v )
    {}
    
    std::string name;
    std::string value;
};

struct Parser
{
    virtual ~Parser() {}

    void parse( const char *str, int len, bool end = true );
    
protected:
    virtual void on_start( std::string const& name,
        std::vector<Property> const& propertys );
        
    virtual void on_data( std::string const& data );
    
    virtual void on_end( std::string const& name );
private:
    void parse_property( std::string const& tag );
};

void ParseXMLData( const char *data, int len, Parser & parser );

}
}

#endif

