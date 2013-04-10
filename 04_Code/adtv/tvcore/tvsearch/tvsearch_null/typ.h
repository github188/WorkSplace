/*****************************************************************************
  Description: CPI data type definition

  Copyright(c) 2010-2020 Novel-SuperTV, All rights reserved.
  该头文件为基本数据类型定义，立足于NT 的数据类型定义头文件，并向其它系统扩�?  NS_打头的数据类型，将逐步淡出该文件�?
  Date        Author           Modification
  ----------------------------------------------------------------
  2012-04-20  hjj				Created
*****************************************************************************/
#ifndef NOVELSUPERTV_STBRT_TYP_H
#define NOVELSUPERTV_STBRT_TYP_H

#if defined(WIN32)
#include <Windows.h>
#endif 

#ifdef __cplusplus
extern "C"
{
#endif
#define IN
#define OUT
#define INOUT

typedef unsigned char		U8;
typedef signed char			S8;
typedef unsigned short		U16;
typedef signed short		S16;
typedef unsigned int		U32;
typedef signed int			S32;
typedef     signed long		LONG;
typedef unsigned long		ULONG;
typedef unsigned long       DWORD;
typedef unsigned char		BYTE;
typedef unsigned long long  UINT64;
typedef   signed long long  INT64;
typedef unsigned short      WORD;
typedef float               FLOAT;
typedef int INT;
typedef int                 BOOL;
typedef unsigned int UINT;

typedef short CSHORT;
typedef char CCHAR;      

#if !defined(WIN32)

////////////////////////////////////////////////////////////////////////////////
// defines
////////////////////////////////////////////////////////////////////////////////
#ifndef _WINDOWS
#define __stdcall
#endif
#define UNUSED_PARAM(var) (void)var

#define INVALID_HANDLE_VALUE ((void *)-1)
#define INFINITE            0xFFFFFFFF  // Infinite timeout

// -------------------------------------------------------------------------------
typedef long HRESULT;
typedef char CHAR;
typedef short SHORT;
typedef long LONG;
typedef int INT;
typedef unsigned int UINT;
typedef unsigned char UCHAR;
typedef unsigned short USHORT;
typedef unsigned short      UINT16, *PUINT16;
typedef unsigned int        UINT32, *PUINT32;
typedef unsigned long ULONG;
typedef unsigned char       UINT8, *PUINT8;
typedef UCHAR BOOLEAN;           
typedef unsigned long       DWORD;
typedef unsigned long ULONG_PTR, *PULONG_PTR;
typedef  LONG NTSTATUS;
typedef int                 intptr_t;
typedef unsigned int 		_dev_t; 
typedef unsigned short _ino_t;      /* i-node number (not used on DOS) */
typedef long _off_t;                /* file offset value */
typedef unsigned long _fsize_t; //can be 64 bit for win32

typedef unsigned int		size_t;

#define S_OK                                   ((HRESULT)0L)

#ifndef NULL
#define NULL 0
#endif
#define CONST               const

#define CREATE_SUSPENDED                  (0x00000004)
#define WAIT_OBJECT_0       ( 0 )
#ifdef _WINDOWS
#define WAIT_TIMEOUT       ( 258L )
#else
#define WAIT_TIMEOUT       ( -110L )
#endif
#define MAX_PATH          ( 260 )
#ifndef FALSE
#define FALSE               ( 0 )
#endif

#ifndef TRUE
#define TRUE                ( 1 )
#endif

#define	THREAD_PRIORITY_LOWEST		-2
#define	THREAD_PRIORITY_BELOW_NORMAL	-1
#define	THREAD_PRIORITY_NORMAL		0
#define	THREAD_PRIORITY_ABOVE_NORMAL	1
#define	THREAD_PRIORITY_HIGHEST		2

#define STARTF_USESHOWWINDOW    0x00000001
#define SW_HIDE             0
#define SW_SHOW             5
#define _INTSIZEOF(n)   ( (sizeof(n) + sizeof(int) - 1) & ~(sizeof(int) - 1) )
// #define va_start(ap,v)  ( ap = (va_list)(&v) + _INTSIZEOF(v) )
// #define va_end(ap)      ( ap = (va_list)0 )

//------------------------------------------------------------------------------
typedef void *				PVOID;
typedef void *				HANDLE;
typedef void *				HMODULE;
typedef void *				HINSTANCE;
typedef void *				LPVOID;
typedef void *				FARPROC;
typedef unsigned char *		LPBYTE;
typedef char *				LPTSTR;
typedef const char *		LPCTSTR;
typedef UCHAR *				PUCHAR;
typedef ULONG *				PULONG;
typedef void *				PVOID;
typedef void *				LPVOID;

typedef long * LPLONG;
typedef char * LPSTR;
typedef const char *LPCSTR;
#ifdef _WINDOWS
typedef __int64 __time64_t;
#else
typedef long long __time64_t;
#endif
/////////////////////////////////////////////////////////////////////////////////////////
// structs
/////////////////////////////////////////////////////////////////////////////////////////

typedef struct _LIST_ENTRY {
   struct _LIST_ENTRY *Flink;
   struct _LIST_ENTRY *Blink;
} LIST_ENTRY, *PLIST_ENTRY, *RESTRICTED_POINTER;


typedef struct tagCRITICAL_SECTION
{
    WORD   Type;
    WORD   CreatorBackTraceIndex;
    struct _RTL_CRITICAL_SECTION *CriticalSection;
    LIST_ENTRY ProcessLocksList;
    DWORD EntryCount;
    DWORD ContentionCount;
    DWORD Flags;
    WORD   CreatorBackTraceIndexHigh;
    WORD   SpareWORD  ;
}CRITICAL_SECTION, *LPCRITICAL_SECTION;

typedef struct _SECURITY_ATTRIBUTES {
    DWORD nLength;
    LPVOID lpSecurityDescriptor;
    BOOL bInheritHandle;
} SECURITY_ATTRIBUTES, *PSECURITY_ATTRIBUTES, *LPSECURITY_ATTRIBUTES;

typedef struct tagPROCESS_INFORMATION
{
    HANDLE hProcess;
    HANDLE hThread;
    DWORD dwProcessId;
    DWORD dwThreadId;
    
}PROCESS_INFORMATION,*LPPROCESS_INFORMATION;

typedef struct _SYSTEMTIME {
    WORD wYear;
    WORD wMonth;
    WORD wDayOfWeek;
    WORD wDay;
    WORD wHour;
    WORD wMinute;
    WORD wSecond;
    WORD wMilliseconds;
} SYSTEMTIME, *PSYSTEMTIME, *LPSYSTEMTIME;
typedef struct _STARTUPINFOA {
    DWORD   cb;
    LPSTR   lpReserved;
    LPSTR   lpDesktop;
    LPSTR   lpTitle;
    DWORD   dwX;
    DWORD   dwY;
    DWORD   dwXSize;
    DWORD   dwYSize;
    DWORD   dwXCountChars;
    DWORD   dwYCountChars;
    DWORD   dwFillAttribute;
    DWORD   dwFlags;
    WORD    wShowWindow;
    WORD    cbReserved2;
    LPBYTE  lpReserved2;
    HANDLE  hStdInput;
    HANDLE  hStdOutput;
    HANDLE  hStdError;
} STARTUPINFO, *LPSTARTUPINFOA;
struct _finddata_t{
        unsigned    attrib;
        __time64_t  time_create;    /* -1 for FAT file systems */
        __time64_t  time_access;    /* -1 for FAT file systems */
        __time64_t  time_write;
        _fsize_t    size;
        char        name[260];
};

#endif 

#ifdef __cplusplus
}
#endif

#endif

