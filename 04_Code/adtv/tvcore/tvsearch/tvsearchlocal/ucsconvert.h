#ifndef __UCSCONVERT__H__
#define __UCSCONVERT__H__

// #include "tvdefs.h"

#if defined(__cplusplus) 
extern "C" 
{ 
#endif 


/**
* 将UTF8编码的字符串转化为GBK编码的字符串.
*
* \param[in]  utf8 UTF8编码的字符串.
* \param[out] gbk  GBK编码的字符串.
*
* \note 参数gbk必须有足够空间存放转换完的字符.
*/
void UTF8ToGBK( const U8* utf8, char *gbk );

/**
* 将UTF16编码的字符串转化为GBK编码的字符串.
*
* \param[in]  unicode UTF16 Little Endian编码的字符串.
* \param[out] gbk GBK编码的字符串.
*
* \note 参数gbk必须有足够空间存放转换完的字符.
*/
void UnicodeToGBK( const U16* unicode, char* gbk );

/**
* 判断字符是否为GBK字符集.
*
* \param[in] ch1 字符串的第二个字符.
* \param[in] ch2 字符串的第二个字符.
*
* \return 字符是否为GBK字符集.
* -true  是GBK字符集.
* -false 不是GBK字符集.
*/
bool IsGBK( U8 ch1, U8 ch2 );

#if defined(__cplusplus) 
}
#endif 

#endif // defined(__UCSCONVERT__H__)