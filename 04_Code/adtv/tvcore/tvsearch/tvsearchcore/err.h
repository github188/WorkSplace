/** 
 * \file typedef.h
 * \brief 窗口系统类型定义
 *
 * Copyright(c) 2005-2009 Novel-SuperTV, All rights reserved.
 *
 * Date        Author           Modification\n
 * ----------------------------------------------------------------\n
 * 2005-04-04  Fuwenchao        Created\n
 * 2005-08-03  George           Modified\n
 */
#ifndef NOVELSUPERTV_STBRT_ERROR_H
#define NOVELSUPERTV_STBRT_ERROR_H

#include "typ.h"
#include <stdexcept>
#include <string>

namespace NovelSupertv
{
namespace stbruntime
{

struct exception : std::runtime_error 
{
	exception( ::std::string const& what, U32 e=0 );

    U32 error; 
};

}
}
#endif

