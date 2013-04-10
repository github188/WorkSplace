/*****************************************************************************
  Description: CPI data type definition

  Copyright(c) 2004-2009 Novel-SuperTV, All rights reserved.

  Date        Author           Modification
  ----------------------------------------------------------------
  2004-11-17  Novel-SuperTV    Created
*****************************************************************************/
#ifndef NOVELSUPERTV_STBRT_TYP_H
#define NOVELSUPERTV_STBRT_TYP_H

#ifdef __cplusplus
extern "C"
{
#endif



////////////////////////////////////////////////////////////////////////////////
// defines
////////////////////////////////////////////////////////////////////////////////
#ifndef _WINDOWS
#define __stdcall
#endif

#define INVALID_HANDLE_VALUE ((void *)-1)
#define INFINITE            0xFFFFFFFF  // Infinite timeout

// -------------------------------------------------------------------------------
typedef long HRESULT;
#define S_OK                                   ((HRESULT)0L)

#ifndef NULL
#define NULL 0
#endif
#define CONST               const

#define CREATE_SUSPENDED                  (0x00000004)
#define WAIT_OBJECT_0       ( 0 )
#define WAIT_TIMEOUT       ( 258L )
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

////////////////////////////////////////////////////////////////////////////////
// typedefs 
////////////////////////////////////////////////////////////////////////////////
typedef unsigned char		U8;
typedef signed char			S8;
typedef unsigned short		U16;
typedef signed short		S16;
typedef unsigned int		U32;
typedef signed int			S32;
typedef	signed long			LONG;
typedef	unsigned long		ULONG;

// typedef unsigned long		RESULT;
// typedef S32				NS_RESULT;
// typedef unsigned long		NS_Color;
// typedef U32				BOOL;
// typedef void *				NS_HANDLE;

// typedef NS_U8				U8;
//typedef NS_S8				S8;
// typedef NS_U16				U16;
//typedef NS_S16				S16;
//typedef U32				U32;
// typedef NS_S32				S32;

// typedef U32					NsTime;

// typedef U32 					Color;

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
typedef int                 intptr_t;
typedef unsigned int 		_dev_t; 
typedef unsigned short _ino_t;      /* i-node number (not used on DOS) */
typedef long _off_t;                /* file offset value */

typedef unsigned long       DWORD;
typedef int                 BOOL;
typedef unsigned char       BYTE;
typedef unsigned short      WORD;
typedef float               FLOAT;
typedef int INT;
typedef unsigned int UINT;

typedef long * LPLONG;
typedef char * LPSTR;
typedef const char *LPCSTR;
#ifdef _WINDOWS
typedef __int64 __time64_t;
#else
typedef long long __time64_t;
#endif
typedef unsigned long _fsize_t; //can be 64 bit for win32
/////////////////////////////////////////////////////////////////////////////////////////
// structs
/////////////////////////////////////////////////////////////////////////////////////////
typedef struct tagSRect
{
	S32 left;				
	S32 top;				
	S32 right;				
	S32 bottom;				
} NS_RECT;

typedef NS_RECT NsRect ;

typedef struct tagNsPoint
{
	S32 x;
	S32 y;
} NsPoint;

typedef struct tagNsSize
{
	S32 w;
	S32 h;
} NS_SIZE;

typedef NS_SIZE NsSize;

typedef struct tagNsVECTOR2
{
	S32 x;
	S32 y;
} NsVector2;

typedef struct tagNsVECTOR3
{
	S32 x;
	S32 y;
	S32 z;
} NsVector3;

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

/////////////////////////////////////////////////////////////////////////////////////////
// dummy functions
/////////////////////////////////////////////////////////////////////////////////////////
HANDLE NS_CreateEvent(
  LPSECURITY_ATTRIBUTES lpEventAttributes, 
  BOOL bManualReset, 
  BOOL bInitialState, 
  LPTSTR lpName 
);
BOOL NS_SetEvent(
  HANDLE hEvent 
);
BOOL NS_WaitEvent( HANDLE hEvent, DWORD timeoutInMS );

void NS_DeleteEvent(HANDLE hEvent );

unsigned int NS__beginthreadex( 
   void *security,
   unsigned stack_size,
   unsigned (__stdcall *start_address )( void * ),
   void *arglist,
   unsigned initflag,
   unsigned *thrdaddr 
   );
DWORD GetCurrentThreadId();

DWORD NS_WaitForSingleObject( 
  HANDLE hHandle, 
  DWORD dwMilliseconds 
);
BOOL NS_CloseHandle( 
  HANDLE hObject
);

HANDLE NS_CreateSemaphore(
  LPSECURITY_ATTRIBUTES lpSemaphoreAttributes, 
  LONG lInitialCount, 
  LONG lMaximumCount, 
  const char * lpName 
);
BOOL NS_ReleaseSemaphore(
  HANDLE hSemaphore, 
  LONG lReleaseCount, 
  LPLONG lpPreviousCount 
);


void NS_InitializeCriticalSection( 
  LPCRITICAL_SECTION lpCriticalSection 
);
void NS_EnterCriticalSection( 
  LPCRITICAL_SECTION lpCriticalSection 
);
BOOL NS_TryEnterCriticalSection( 
  LPCRITICAL_SECTION lpCriticalSection 
);

void NS_LeaveCriticalSection( 
  LPCRITICAL_SECTION lpCriticalSection 
  );

void NS_DeleteCriticalSection( 
  LPCRITICAL_SECTION lpCriticalSection 
);
// DWORD NS_GetTickCount(void);
BOOL NS_SetThreadPriority( 
  HANDLE hThread, 
  int nPriority
  );
DWORD NS_GetCurrentThreadId(void);
DWORD NS_ResumeThread( 
  HANDLE hThread
  );

void NS_Sleep(
  DWORD dwMilliseconds
  );
void NS_GetLocalTime( 
  LPSYSTEMTIME lpSystemTime 
  );
BOOL NS_SetLocalTime( 
  const SYSTEMTIME* lpSystemTime 
  );

BOOL NS_CreateProcess( 
    LPCSTR lpApplicationName,
    LPSTR lpCommandLine,
    LPSECURITY_ATTRIBUTES lpProcessAttributes,
    LPSECURITY_ATTRIBUTES lpThreadAttributes,
    BOOL bInheritHandles,
    DWORD dwCreationFlags,
    LPVOID lpEnvironment,
    LPCSTR lpCurrentDirectory,
    LPSTARTUPINFOA lpStartupInfo,
    LPPROCESS_INFORMATION lpProcessInformation
);

BOOL NS_TerminateProcess(
  HANDLE hProcess, 
  DWORD uExitCode
  );
HINSTANCE NS_LoadLibrary( 
  LPCSTR lpLibFileName
  );

BOOL NS_FreeLibrary( 
  HMODULE hLibModule
  );
FARPROC NS_GetProcAddress( 
  HMODULE hModule, 
  LPCSTR lpProcName
  );

DWORD NS_GetPrivateProfileString(
  const char* lpAppName,
  const char* lpKeyName,
  const char* lpDefault,
  const char* lpReturnedString,
  DWORD nSize,
  const char* lpFileName
  );

UINT NS_GetPrivateProfileInt(
  const char* lpAppName,
  const char* lpKeyName,
  INT nDefault,
  const char* lpFileName
  );

BOOL NS_WritePrivateProfileString(
  const char* lpAppName,
  const char* lpKeyName,
  const char* lpString,
  const char* lpFileName
  );
 
long NS__filelength( 
   int fd 
);
intptr_t NS__findfirst(
   const char *filespec,
   struct _finddata_t *fileinfo 
);
 
int NS__findnext(
   intptr_t handle,
   struct _finddata_t *fileinfo 
);
int NS__findclose( 
   intptr_t handle 
);
 
int NS__mkdir(
   const char *dirname 
);
struct _stat{
        _dev_t     st_dev;
        _ino_t     st_ino;
        unsigned short st_mode;
        short      st_nlink;
        short      st_uid;
        short      st_gid;
        _dev_t     st_rdev;
        _off_t     st_size;
        __time64_t st_atime;
        __time64_t st_mtime;
        __time64_t st_ctime;
 };

int NS__stat(
   const char *path,
   struct _stat *buffer 
);
DWORD NS_GetTickCount(void);

#ifdef __cplusplus
}
#endif

#endif

