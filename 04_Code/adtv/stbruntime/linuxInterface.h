#ifndef _LINUX_INTERFACE_H
#define _LINUX_INTERFACE_H

////////////////////////////////////////////////////////////////////////////////
// ��̬���ӿ�ӿ�
////////////////////////////////////////////////////////////////////////////////
HANDLE LoadLibrary( LPCSTR lpLibFileName );
BOOL FreeLibrary( HANDLE hLibModule );
LPVOID GetProcAddress( HANDLE HANDLE , LPCSTR lpProcName );

////////////////////////////////////////////////////////////////////////////////
// �¼��ӿ�
////////////////////////////////////////////////////////////////////////////////
HANDLE CreateEvent(
  LPSECURITY_ATTRIBUTES lpEventAttributes, 
  BOOL bManualReset, 
  BOOL bInitialState, 
  LPSTR lpName 
);
BOOL SetEvent(HANDLE hEvent);
// WaitEvent( HANDLE hEvent, DWORD timeoutInMS )
// void NS_DeleteEvent(HANDLE hEvent )

////////////////////////////////////////////////////////////////////////////////
// �߳̽ӿ�
////////////////////////////////////////////////////////////////////////////////
typedef unsigned int uintptr_t;
uintptr_t _beginthreadex(
   void *security,
   unsigned stack_size,
   unsigned ( __stdcall *start_address )( void * ),
   void *arglist,
   unsigned initflag,
   unsigned *thrdaddr
   );
////////////////////////////////////////////////////////////////////////////////
DWORD GetTickCount(void);
BOOL  SetLocalTime(const SYSTEMTIME *lpSystemTime);
void  GetLocalTime(LPSYSTEMTIME lpSystemTime);
DWORD GetCurrentThreadId();

#endif

