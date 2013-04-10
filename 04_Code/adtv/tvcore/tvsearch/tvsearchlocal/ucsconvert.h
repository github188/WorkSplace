#ifndef __UCSCONVERT__H__
#define __UCSCONVERT__H__

// #include "tvdefs.h"

#if defined(__cplusplus) 
extern "C" 
{ 
#endif 


/**
* ��UTF8������ַ���ת��ΪGBK������ַ���.
*
* \param[in]  utf8 UTF8������ַ���.
* \param[out] gbk  GBK������ַ���.
*
* \note ����gbk�������㹻�ռ���ת������ַ�.
*/
void UTF8ToGBK( const U8* utf8, char *gbk );

/**
* ��UTF16������ַ���ת��ΪGBK������ַ���.
*
* \param[in]  unicode UTF16 Little Endian������ַ���.
* \param[out] gbk GBK������ַ���.
*
* \note ����gbk�������㹻�ռ���ת������ַ�.
*/
void UnicodeToGBK( const U16* unicode, char* gbk );

/**
* �ж��ַ��Ƿ�ΪGBK�ַ���.
*
* \param[in] ch1 �ַ����ĵڶ����ַ�.
* \param[in] ch2 �ַ����ĵڶ����ַ�.
*
* \return �ַ��Ƿ�ΪGBK�ַ���.
* -true  ��GBK�ַ���.
* -false ����GBK�ַ���.
*/
bool IsGBK( U8 ch1, U8 ch2 );

#if defined(__cplusplus) 
}
#endif 

#endif // defined(__UCSCONVERT__H__)