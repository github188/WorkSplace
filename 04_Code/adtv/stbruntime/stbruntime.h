/*****************************************************************************
  Description: CPI OS interface

  Copyright(c) 2010-2015 Novel-SuperTV, All rights reserved.

  Date        Author           Modification
  ----------------------------------------------------------------
  2011-03-16  hejinjing    Created
*****************************************************************************/
#ifndef NOVELSUPER_STBRT_OS_H
#define NOVELSUPER_STBRT_OS_H

typedef unsigned int		U32;
typedef unsigned short		U16;

#ifdef WIN32
#include <windows.h>
typedef struct NS_SemTag
{
    HANDLE  Handle;
} NT_Sem;

typedef struct NT_MutexTag
{
    CRITICAL_SECTION Handle;
} NT_Mutex;


#else
#define __declspec(dllexport)
#define __stdcall 
#define FALSE               ( 0 )
#define TRUE                ( 1 )

#ifndef WAIT_OBJECT_0

#define WAIT_OBJECT_0       ( 0 )
typedef unsigned char		BYTE;
typedef int                 BOOL;
typedef unsigned long       DWORD;
typedef void *				HANDLE;
typedef void *				LPVOID;
typedef void *				HMODULE;

typedef char * LPSTR;
typedef const char *LPCSTR;


typedef struct _LIST_ENTRY {
   struct _LIST_ENTRY *Flink;
   struct _LIST_ENTRY *Blink;
} LIST_ENTRY, *PLIST_ENTRY, *RESTRICTED_POINTER;

typedef struct tagCRITICAL_SECTION
{
    U16   Type;
    U16   CreatorBackTraceIndex;
    struct _RTL_CRITICAL_SECTION *CriticalSection;
    LIST_ENTRY ProcessLocksList;
    DWORD EntryCount;
    DWORD ContentionCount;
    DWORD Flags;
    U16   CreatorBackTraceIndexHigh;
    U16   SpareWORD  ;
}CRITICAL_SECTION, *LPCRITICAL_SECTION;

typedef struct _SECURITY_ATTRIBUTES {
    DWORD nLength;
    LPVOID lpSecurityDescriptor;
    BOOL bInheritHandle;
} SECURITY_ATTRIBUTES, *PSECURITY_ATTRIBUTES, *LPSECURITY_ATTRIBUTES;


typedef struct _SYSTEMTIME {
    U16 wYear;
    U16 wMonth;
    U16 wDayOfWeek;
    U16 wDay;
    U16 wHour;
    U16 wMinute;
    U16 wSecond;
    U16 wMilliseconds;
} SYSTEMTIME, *PSYSTEMTIME, *LPSYSTEMTIME;
#endif
#endif


typedef unsigned int ( __stdcall *NS_THREADPROC)(void *);
#ifdef __cplusplus
extern "C"
{
#endif

///////////////////////////////////////////// functions used///////////////////////////////////////////////
DWORD __declspec(dllexport) NS_GetTickCount(void);
void  __declspec(dllexport) NS_SetCurrTime(U32 t);
U32   __declspec(dllexport) NS_GetCurrTime();

// Creates a thread 
HANDLE __declspec(dllexport) NS_CreateThread(
					  char const *pThreadName, // name of the thread,This parameter may be NULL.
					  NS_THREADPROC pFunction, // A pointer to the  function to be executed by the thread.
					  void *pParam, // A pointer to a variable to be passed to the thread
					  U32 stackSize,  // The initial size of the stack
					  BOOL isSuspended, // if IsSuspended is TRUE, the thread is created in a suspended state
					  U32 *pThreadID
);

// Close the thread handle
void __declspec(dllexport) NS_ThreadDelete( HANDLE threadHandle );
// Blocks the calling thread until a thread terminates
BOOL __declspec(dllexport) NS_ThreadJoin( 
					HANDLE threadHandle, // A handle to the handle to be terminated.
					U32 timeoutInMS // The time-out interval, in milliseconds
);

// Get handle of the current thread.
DWORD __declspec(dllexport) NS_GetCurrentThreadId(void);
DWORD __declspec(dllexport) NS_ResumeThread( HANDLE hThread);
void __declspec(dllexport) NS_WaitThread(HANDLE hThread);
BOOL __declspec(dllexport) NS_IsCurrentThreadByID(U32 threadid);

// Suspends the execution of the current thread for at least the specified interval
HANDLE __declspec(dllexport) NS_CreateEvent(
  LPSECURITY_ATTRIBUTES lpEventAttributes, 
  BOOL bManualReset, 
  BOOL bInitialState, 
  LPSTR lpName 
);
BOOL __declspec(dllexport) NS_SetEvent( HANDLE hEvent );
BOOL __declspec(dllexport) NS_WaitEvent( HANDLE hEvent, DWORD timeoutInMS );
void __declspec(dllexport) NS_DeleteEvent(HANDLE hEvent );

// Creates a mutex object
HANDLE __declspec(dllexport) NS_MutexCreate();
// Delete a mutex object
void __declspec(dllexport) NS_MutexDelete( HANDLE mutexHandle );
// Lock a mutex object
void __declspec(dllexport) NS_MutexLock( HANDLE mutexHandle );
// Release a mutex object
void __declspec(dllexport) NS_MutexUnlock( HANDLE mutexHandle );

unsigned int __declspec(dllexport) NS__beginthreadex( 
   void *security,
   unsigned stack_size,
   unsigned (__stdcall *start_address )( void * ),
   void *arglist,
   unsigned initflag,
   unsigned *thrdaddr 
   );
HANDLE __declspec(dllexport) NS_LoadLibrary( LPCSTR lpLibFileName);
BOOL __declspec(dllexport) NS_FreeLibrary( HANDLE hLibModule);
__declspec(dllexport) void * NS_malloc(DWORD size);
void __declspec(dllexport) NS_free(void *p);
void __declspec(dllexport) NS_memset(void *p,BYTE b, DWORD size);
void __declspec(dllexport) NS_memcpy(void*pDst, const void *pSrc, DWORD size);
U32 __declspec(dllexport) NS_strlen(const char*p);
void __declspec(dllexport) NS_sleep( DWORD timeInMS );

// Creates semaphore object
HANDLE __declspec(dllexport) NS_SemCreate( U16 count,U16 max_count );
// Deletes semaphore object
void __declspec(dllexport) NS_SemDelete( HANDLE semHandle );
// Increases the count of the specified semaphore object
void __declspec(dllexport) NS_SemSignal( HANDLE semHandle );
// Waits until the semaphore object is in the signaled state.
BOOL __declspec(dllexport) NS_SemWait( HANDLE semHandle, U32 timeoutInMS );

#ifdef __cplusplus
}

#endif

#endif

