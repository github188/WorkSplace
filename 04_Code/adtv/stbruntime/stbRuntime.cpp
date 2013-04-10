#include "stbruntime.h"
#include <time.h>
#include <stdio.h>
#include <stdlib.h>
#include <string>

#ifdef WIN32
// _beginthreadex 函数
#include <process.h>

#else

#include <unistd.h>
#include <threads.h>
#include <pthread.h>
#include <sys/timeb.h>
#include "linuxInterface.h"
#define INFINITE            0xFFFFFFFF  // Infinite timeout
typedef struct 
{
	pthread_mutex_t	mutex;
	pthread_cond_t		condition;
	int			semCount;	
}sem_private_struct, *pSem_Private;

#endif
//#define LOG_LEVEL LOG_TRACE
//#define LOG_TAG "stbruntime"
//#include "dxreport.h"

////////////////////////////////////////////////////////////////////////////////
// 以下接口已在windows, linux(android) 平台上实现
// 1. 当windows 下有实现，linux 下也有实现时，用宏定义分开
// 2. 当windows 下有实现，linux 下无对应实现时，新添加linux 接口
// 3. 当windows 下实现及其复杂或linux 无对应机制时，应将windows
//      接口拆开在不同的接口中,  而不要直接使用
//  例如windows's CloseHandle(handle) 函数
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// 动态链接库接口
////////////////////////////////////////////////////////////////////////////////
HANDLE NS_LoadLibrary( LPCSTR lpLibFileName)
{
	return LoadLibrary(lpLibFileName);
}
BOOL NS_FreeLibrary( HANDLE hLibModule)
{
	return FreeLibrary((HMODULE)hLibModule);
}
LPVOID NS_GetProcAddress( HANDLE handle, LPCSTR lpProcName)
{
	return GetProcAddress((HMODULE)handle,lpProcName);
}

////////////////////////////////////////////////////////////////////////////////
// 事件接口
////////////////////////////////////////////////////////////////////////////////
HANDLE NS_CreateEvent(
		LPSECURITY_ATTRIBUTES lpEventAttributes, 
		BOOL bManualReset, 
		BOOL bInitialState, 
		LPSTR lpName 
		)
{
	return CreateEvent(lpEventAttributes,bManualReset,bInitialState,lpName);
}
BOOL NS_SetEvent(HANDLE hEvent )
{
	return SetEvent(hEvent);
}


BOOL NS_WaitEvent( HANDLE hEvent, DWORD timeoutInMS )
{
#ifdef WIN32
	if(hEvent) {
		return WaitForSingleObject( hEvent, timeoutInMS );
	}
	return true;
#else
	android::Condition * cond = (android::Condition *)hEvent;
	android::Mutex mutex;
	if(INFINITE == timeoutInMS)
	{
		return cond->wait(mutex);
	}
	else
	{
		DWORD timeoutInNs = timeoutInMS * 1000*1000;
		return cond->waitRelative(mutex,timeoutInNs);
	}
	//	return cond->wait(mutex);
#endif
}
void NS_DeleteEvent(HANDLE hEvent )
{
#ifdef WIN32
	CloseHandle(hEvent);
#else
	if(hEvent)
	{
		delete (android::Condition *)hEvent;
	}
#endif	
}

////////////////////////////////////////////////////////////////////////////////
// 线程接口
////////////////////////////////////////////////////////////////////////////////
HANDLE NS_CreateThread( 
		char const *pThreadName, 
		NS_THREADPROC pFunction, 
		void *pParam,
		U32 stackSize, 
		BOOL isSuspended,
		U32 *pThreadID)
{
	unsigned ThreadID;
	uintptr_t rt = NS__beginthreadex( NULL, 0, pFunction, 
			pParam, 0/*CREATE_SUSPENDED*/, &ThreadID );
	if(pThreadID){
		*pThreadID = ThreadID;
	}

	return (HANDLE)rt;	// thread_name, stacksize, threadID 都没有使用
}
uintptr_t NS__beginthreadex( 
		void *security,
		unsigned stack_size,
		unsigned ( __stdcall *start_address )( void * ),
		void *arglist,
		unsigned initflag,
		unsigned *thrdaddr 
		)
{
	return (unsigned int)_beginthreadex(security,stack_size,start_address,arglist,initflag,thrdaddr);
}

BOOL NS_IsCurrentThreadByID(U32 threadid)
{
	if(NS_GetCurrentThreadId() == threadid)
		return true;
	return false;
}
void NS_WaitThread(HANDLE hThread)
{
#ifdef WIN32			
	WaitForSingleObject(hThread,INFINITE);
	CloseHandle(hThread);
#else
	pthread_t * p= (pthread_t *)hThread; 
	pthread_join(*p,0);
#endif
}

BOOL NS_ThreadJoin( HANDLE threadHandle, U32 timeoutInMS )
{
	if(threadHandle) {
#ifdef WIN32
		HANDLE h = (HANDLE)threadHandle;
		DWORD ret= WaitForSingleObject(h, timeoutInMS );

#else
		pthread_t * p = (pthread_t *)threadHandle; 
		DWORD ret = pthread_join(*p,0);
#endif
		if( ret == WAIT_OBJECT_0 )
			return true;
	}
	return false;
}

void NS_ThreadDelete( HANDLE threadHandle )
{
#ifdef WIN32	
	if(threadHandle)
	{
		HANDLE h = (HANDLE)threadHandle;
		if(h)
			CloseHandle( h );
	}
#else
	// android 不用处理	
#endif	
}

DWORD NS_GetCurrentThreadId(void)
{
	return GetCurrentThreadId();
}
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

DWORD NS_GetTickCount(void)
{
	return GetTickCount();
}

U32 NS_GetCurrTime() 
{
	struct tm ptm;
	SYSTEMTIME ltime;
	GetLocalTime(&ltime);
	ptm.tm_year	   = ltime.wYear - 1900;
	ptm.tm_mon	   = ltime.wMonth - 1;
	ptm.tm_mday	   = ltime.wDay;
	ptm.tm_hour	   = ltime.wHour;
	ptm.tm_min	   = ltime.wMinute;
	ptm.tm_sec	   = ltime.wSecond;
	ptm.tm_isdst	= -1;
	return (U32)mktime(&ptm);
}

void NS_SetCurrTime(U32 t)
{
	struct tm * stm = gmtime((time_t *)&t);
	SYSTEMTIME systime;
	systime.wYear = stm->tm_year+1900;
	systime.wMonth = stm->tm_mon+1;
	systime.wDay = stm->tm_mday;
	systime.wHour = stm->tm_hour;
	systime.wMinute = stm->tm_min;
	systime.wSecond = stm->tm_sec;
	systime.wMilliseconds = 0;
	if(systime.wYear <= 1980 || systime.wYear >= 2099)
		return;
	SetLocalTime(&systime);
}
////////////////////////////////////////////////////////////////////////////////
// 互斥量
////////////////////////////////////////////////////////////////////////////////
HANDLE NS_MutexCreate()
{
#ifdef WIN32	
	NT_Mutex* pMutex = (NT_Mutex*)malloc( sizeof( NT_Mutex ) );
	if( !pMutex )
		return NULL;
	InitializeCriticalSection( &( pMutex->Handle ) );
	return pMutex;
#else
	android::Mutex *pMutex=new android::Mutex();
	return pMutex;
#endif
}

void NS_MutexDelete( HANDLE mutexHandle )
{
#ifdef WIN32	
	NT_Mutex* pMutex = (NT_Mutex*)mutexHandle;
	if(pMutex) {
		DeleteCriticalSection( &(pMutex->Handle) );
		free( pMutex );
	}
#else
	android::Mutex *pMutex = (android::Mutex *)mutexHandle;
	if(pMutex) delete pMutex;
#endif
}
void NS_MutexLock( HANDLE mutexHandle )
{
#ifdef WIN32	
	NT_Mutex* pMutex = (NT_Mutex*)mutexHandle;
	if(pMutex) 
		EnterCriticalSection( &( pMutex->Handle ) );
#else
	android::Mutex *pMutex = (android::Mutex *)mutexHandle;
	pMutex->lock();
#endif
}

void NS_MutexUnlock( HANDLE mutexHandle )
{
#ifdef WIN32	
	NT_Mutex* pMutex = (NT_Mutex*)mutexHandle;
	if(pMutex)
		LeaveCriticalSection( &( pMutex->Handle ) );
#else
	android::Mutex *pMutex = (android::Mutex *)mutexHandle;
	pMutex->unlock();
#endif
}


void *NS_malloc(DWORD size)
{
	return malloc(size);
}
void NS_free(void *p)
{
	free(p);
}
void NS_memset(void *p,BYTE b, DWORD size)
{
	memset(p,b,size);
}
void NS_memcpy(void*pDst, const void *pSrc, DWORD size)
{
	memcpy(pDst,pSrc,size);
}
size_t NS_strlen(const char*p)
{
	return (U32)strlen(p);
}
void NS_sleep(DWORD dwMilliseconds)
{
#ifdef WIN32
	Sleep(dwMilliseconds);
#else
	usleep(1000 * dwMilliseconds);
#endif
}

// Creates semaphore object
HANDLE NS_SemCreate( U16 initCount,U16 max_count )
{
#ifdef WIN32	
	NT_Sem *pSem= (NT_Sem *)malloc( sizeof( NT_Sem ) );
	if( !pSem )
		return NULL;

	pSem->Handle = CreateSemaphore( NULL, initCount, max_count , NULL );
	if( pSem->Handle == NULL )
	{
		free( pSem );
		return NULL;
	}
	return (HANDLE)pSem;	
#else	
	pSem_Private    pToken;
	int rc;
	pToken = (pSem_Private) malloc(sizeof(sem_private_struct));
	rc = pthread_mutex_init(&(pToken->mutex), NULL);
	if(rc!=0)
	{
		free(pToken);
		return NULL; /*RC_OBJECT_NOT_CREATED;*/
	}
	rc = pthread_cond_init(&(pToken->condition), NULL);
	if(rc!=0)
	{
		pthread_mutex_destroy( &(pToken->mutex) );
		free(pToken);
		return NULL;/*RC_OBJECT_NOT_CREATED;*/
	}

	pToken->semCount = initCount/* 1 */;
	return pToken;
#endif
}
// Deletes semaphore object
void NS_SemDelete( HANDLE semHandle )
{
#ifdef WIN32	
	NT_Sem *pSem = (NT_Sem *)semHandle;
	if(pSem) {
		CloseHandle( pSem->Handle );
		free( pSem );
	}
#else
	pSem_Private	pToken = (pSem_Private)semHandle;
	if(pToken) {
		pthread_mutex_destroy(&(pToken->mutex));
		pthread_cond_destroy(&(pToken->condition));
		free (pToken);
	}
#endif
}
// Increases the count of the specified semaphore object
void NS_SemSignal( HANDLE semHandle )
{
#ifdef WIN32	
	NT_Sem *pSem = (NT_Sem *)semHandle;
	if(pSem) 
		ReleaseSemaphore( pSem->Handle , 1 , NULL );
#else	
	pSem_Private	pToken = (pSem_Private)semHandle;
	int rc = pthread_mutex_lock(&(pToken->mutex));
	if (rc)	return /* RC_SEM_WAIT_ERROR*/;
	pToken->semCount++;
	rc = pthread_mutex_unlock(&(pToken->mutex));
	//	dxreport("NS_SemSignal:pToken->semCount:%d\n",pToken->semCount);
	rc = pthread_cond_signal(&(pToken->condition));
	if (rc) return /*RC_SEM_POST_ERROR;*/;
	return;

#endif
}
// Waits until the semaphore object is in the signaled state.
BOOL NS_SemWait( HANDLE semHandle, U32 timeoutInMS )
{
#ifdef WIN32	
	NT_Sem *pSem = (NT_Sem *)semHandle;
	if(pSem) {
		if( WaitForSingleObject( pSem->Handle, timeoutInMS ) == WAIT_TIMEOUT )
			return FALSE;
	}
	return TRUE;
#else
	pSem_Private	pToken = (pSem_Private)semHandle;
	int rc;
	struct timespec tm;
	struct timeb tp;
	long sec, millisec;
	rc = pthread_mutex_lock(&(pToken->mutex));
	if (rc)	return false; /*RC_SEM_WAIT_ERROR;*/

	sec = timeoutInMS / 1000;
	millisec = timeoutInMS % 1000;
	ftime( &tp );
	tp.time += sec;
	tp.millitm += millisec;
	if( tp.millitm > 999 )
	{
		tp.millitm -= 1000;
		tp.time++;
	}
	tm.tv_sec = tp.time;
	tm.tv_nsec = tp.millitm * 1000000 ;
	//	dxreport("NS_SemWait:pToken is %p, semCount:%d\n", pToken,pToken->semCount);
	while (pToken->semCount <= 0)
	{
		//		dxreport("!!! waiting... test this NS_SemWait:pToken is %p, semCount:%d\n", pToken,pToken->semCount);
		//		rc = 0;  // test
		rc = pthread_cond_timedwait(&(pToken->condition), &(pToken->mutex), &tm);
		if (rc && (errno != EINTR) )
			break;
	}
	if ( rc )
	{
		if ( pthread_mutex_unlock(&(pToken->mutex)) )
			return false; /*RC_SEM_WAIT_ERROR*/ 

		if ( rc == ETIMEDOUT) /* we have a time out */
			return false; /*RC_TIMEOUT*/

		return false; /*RC_SEM_WAIT_ERROR*/

	}
	pToken->semCount--;
	rc = pthread_mutex_unlock(&(pToken->mutex));
	if (rc)	return false;/*RC_SEM_WAIT_ERROR;*/	
	return true;
#endif
}


#ifdef WIN32
BOOL APIENTRY DllMain( HANDLE HANDLE,
		DWORD  ul_reason_for_call,
		LPVOID lpReserved
		)
{
	return TRUE;
}
#endif
