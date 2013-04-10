#ifndef __DVB_TYPE__H_
#define __DVB_TYPE__H_

#ifndef  _BASETYPE_H_ 
typedef signed char int8;         /* 8 bit signed */
typedef unsigned char uint8;      /* 8 bit unsigned */
typedef short int16;              /* 16 bit signed */
typedef unsigned short uint16;    /* 16 bit unsigned */
typedef int int32;                /* 32 bit signed */
typedef unsigned int uint32;      /* 32 bit unsigned */
typedef long long int64;           /* 64 bit signed */
typedef unsigned long long uint64; /* 64 bit unsigned */
#ifndef __cplusplus
//typedef int bool;
#endif
#endif  //_BASETYPE_H_

typedef int64 longlong;
typedef uint64 ulonglong;
typedef unsigned char uchar;
typedef unsigned short ushort;
//typedef unsigned int uint;
typedef unsigned long ulong;



#ifndef TRUE
#define TRUE 1
#endif

#ifndef FALSE
#define FALSE 0
#endif

#ifndef NULL
#define NULL 0
#endif




#endif   /*__DVB_TYPE__H_*/

