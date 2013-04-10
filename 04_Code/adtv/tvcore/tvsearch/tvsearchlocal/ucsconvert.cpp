#include "typ.h"
#include "ucsconvert.h"

struct UTF2GB
{
    U16 uft;
    U16 gb;
};

const U16 ascii_table[] =
{
#include "ascii_table.dat"
};

const U16 utf2gb_table[] =
{
#include "utf2gb_table.dat"
};


const UTF2GB symucs2gb_table[] =
{
#include "symucs2gb_table.dat"
};

bool IsAscii( U16 ucs2 )
{
    if( ucs2 == 0x00 || ucs2 == 0x0a || ucs2 == 0x0d )
        return true;
    if( ucs2 == ascii_table[0x24] )
        return true;
    if( ucs2 == ascii_table[0x40] )
        return true;
    if( ucs2 == 0x24 )
        return true;
    if( ucs2 == 0x40 )
        return true;
    if( ucs2 >= 0x20 && ucs2 <= 0x5a )
        return true;
    if( ucs2 >= 0x61 && ucs2 <= 0x7a )
        return true;

    for( int i = 0; i <= 0x1f; i++ )
        if( ucs2 == ascii_table[i] )
            return true;

    for( int i = 0x5b; i <= 0x60; i++ )
        if( ucs2 == ascii_table[i] )
            return true;

    for( int i = 0x7b; i<= 0x7f; i++ )
        if( ucs2 == ascii_table[i] )
            return true;
    return false;
}

U8 UTFToAscii( U16 uft2 )
{
    if( uft2 == 0x00 || uft2 == 0x0a || uft2 == 0x0d )
        return (U8)uft2;
    if( uft2 == ascii_table[0x24] )
        return 0x24;
    if( uft2 == ascii_table[0x40] )
        return 0x40;
    if( uft2 == 0x24 )
        return 0x02;
    if( uft2 == 0x40 )
        return 0x80;
    if( uft2 >= 0x20 && uft2 <= 0x5a )
        return (U8)uft2;
    if( uft2 >= 0x61 && uft2 <= 0x7a )
        return (U8)uft2;

    for( int i = 0; i <= 0x1f; i++ )
        if( uft2 == ascii_table[i] )
            return i;

    for( int i = 0x5b; i <= 0x60; i++ )
        if( uft2 == ascii_table[i] )
            return i;

    for( int i = 0x7b; i <= 0x7f; i++ )
        if( uft2 == ascii_table[i] )
            return i;

    return '?';
}

U16 Unicode2GB( U16  uft )
{
    int  first = 0;
    int  last = 717-1;
    int  mid;

    if( uft >= 0x4E00 && uft <= 0x9FA5 )
        return utf2gb_table[uft - 0x4E00];
    else
    {
        while ( last >= first )
        {
            mid = ( first + last ) >> 1;

            if( uft < symucs2gb_table[mid].uft )
              last = mid - 1;
            else if( uft > symucs2gb_table[mid].uft )
              first = mid + 1;
            else if( uft == symucs2gb_table[mid].uft )
              return symucs2gb_table[mid].gb;
        }
    }
    return 0xffff;
}

int UTF8ToUnicode( const U8 *uft8, U16 *unicode, int max_len )
{
    int len = 0;

    if ( unicode && uft8 )
    {
        U8  byte;
        while( ( byte = *uft8 ) && ( max_len-- > 0 ) )
        {
            if( ( byte & 0x80 ) == 0 )
            {
                // One Byte
                *unicode++ = byte;
                // Move to Next Char
                uft8++;
            }
            else if( ( byte & 0xe0 ) == 0xc0 )
            {
                // Two Bytes
                *unicode++ = ( ( byte & 0x1f ) << 6 ) | \
                        ( ( *( uft8 + 1 ) & 0x3f ) );
                // Move to Next Char
                uft8 += 2;
            } else if( ( byte & 0xf0 ) == 0xe0 )
            {
                // Three Bytes
                *unicode++ = ( ( byte & 0x0f) << 12 ) | \
                        ( ( *( uft8 + 1 ) & 0x3f ) << 6 ) | \
                        ( ( *( uft8 + 2 ) & 0x3f ) );
                // Move to Next Char
                uft8 += 3;
            }
            else
            {
                // We Donnot Support Now.
                break;
            }
            // Increse Char Number
            len++;
        }
        // Make the last UCS2 char '0' as a terminal
        *unicode = 0;
    }

    // Return Converted UCS2 String Length
    return len;
}

int StrLen( const U8* utf8 )
{
    int len = 0;
    while( *utf8++ )
        ++len;
    return len;
}

void UnicodeToGBK( const U16* unicode, char* gbk )
{
   while( *unicode )
   {
       if( ( *unicode <= 0x7f ) || IsAscii( *unicode ) )
       {
           *gbk = UTFToAscii( *unicode );
           gbk++;
           unicode++;
       }
       else
       {
          U16 gb = Unicode2GB( *unicode );
          *gbk = (U8)( gb >> 8 );
          gbk++;
          *gbk = (U8)( gb & 0x00ff );
          gbk++;
          unicode++;
       }
   }
   *gbk = '\0';
}

void UTF8ToGBK( const U8* utf8, char *gbk )
{

    int len = StrLen( utf8 );
    U16 *unicode = new U16[len + 1];
    UTF8ToUnicode( utf8, unicode, len );
    UnicodeToGBK( unicode, gbk );
    delete []unicode;
}
