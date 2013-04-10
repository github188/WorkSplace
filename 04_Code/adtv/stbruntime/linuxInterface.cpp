#include <stdio.h>

//匿名信号灯不能跨进程访问，等待的时候也没有超时的概念。。。。
#include <stdlib.h>
//此头文件包含了匿名信号灯系列结构及函数
#include <semaphore.h>
//此头文件包含了命名信号灯系列函数
// #include <sys/sem.h>
//此头文件包含了IPC系列的属性及值
#include <sys/ipc.h>
#include <string.h>
//错误返回头
#include <errno.h>
#include <iostream>
#include <dlfcn.h>
#include <sys/sysinfo.h>
#include <sys/stat.h>
#include <threads.h>

#include "stbruntime.h"

using namespace std;

//匿名信号灯结构体
typedef struct _NONAMESEM
{
    //匿名信号灯内部结构
    sem_t m_Sem;
}NoNameSem , * LPNoNameSem;

//命名信号灯结构体
typedef struct _NAMESEM
{
    //命名信号灯ID
    int m_SegID;
    //命名信号灯名字
    char m_SegName[20];
}NameSem , * LPNameSem;

typedef struct _HANDLEOBJECT
{
    //同步类型，匿名信号灯为1，命名信号灯为2
    unsigned int m_Type;
    //匿名信号灯结构体
    NoNameSem  m_NoNameSem;
    //命名信号灯结构体
    NameSem m_NameSem;

}HandleObject , *LPHandleObject;

//等待内核事件、信号灯
//匿名信号灯没有延时功能

//关闭句柄

//创建信号灯
//参数1：安全属性，没有用
//参数2：初始化计数
//参数3：最大计数
//参数4：关键参数，决定用何种函数创建信号，有名字，就用semget，没有名字就用sem_init

//增加信号灯
//参数1：信号量句柄
//参数2：增加的计数
//参数3：原来的计数，匿名方式无法返回



////////////////////////////////////////////////////////////////////////////////
// 动态链接库接口
////////////////////////////////////////////////////////////////////////////////
//需要在GCC编译时加入一个参数 -ldl
//此头文件为加载动态链接库的头文件
//#include <dlfcn.h>
//加载动态链接库
HANDLE LoadLibrary( LPCSTR lpLibFileName )
{
    //实现方法，利用系统函数直接实现
    //                                      保证以后加载的库可以使用自己  立刻判断自己的依赖性
	char buffer[512];
    return (HANDLE)dlopen(buffer, RTLD_GLOBAL       |       RTLD_NOW  );
}

BOOL FreeLibrary( HANDLE hLibModule )
{
    //实现方法，利用系统函数直接实现
    return dlclose( hLibModule );
}

LPVOID GetProcAddress( HANDLE HANDLE , LPCSTR lpProcName )
{
    //实现方法，利用系统函数直接实现
    return dlsym( HANDLE , lpProcName );
}
////////////////////////////////////////////////////////////////////////////////
// 事件接口
////////////////////////////////////////////////////////////////////////////////
HANDLE CreateEvent(
  LPSECURITY_ATTRIBUTES lpEventAttributes, 
  BOOL bManualReset, 
  BOOL bInitialState, 
  LPSTR lpName 
)
{
	android::Condition *cond=new android::Condition();
		return cond;
}

//设置信号
BOOL SetEvent( HANDLE hEvent )
{
	android::Condition *cond = (android::Condition *)hEvent;
	cond->signal();
	return TRUE;
}

////////////////////////////////////////////////////////////////////////////////
// 线程接口
////////////////////////////////////////////////////////////////////////////////
//做线程操作需要导入一个库文件：libpthread
//或者GCC编译的时候需要加入一个 -lpthread 参数
//此头文件为做线程操作的头文件
//#include <pthread.h>
//开启线程
//参数一：安全属性
//参数二：初始化堆栈大小
//参数三：线程函数
//参数四：线程参数
//参数五：线程初始化状态
//参数六：返回参数，线程ID
//返回值：线程创建是否成功
uintptr_t _beginthreadex(
   void *security,
   unsigned stack_size,
   unsigned ( __stdcall *start_address )( void * ),
   void *arglist,
   unsigned initflag,
   unsigned *thrdaddr
   )
{
    //实现方法，用Linux函数实现
    //目前缺点：无法设置安全属性、堆栈大小以及初始化状态
    //默认为堆栈自动分配，初始化之后立刻启动
    //如需要更改，要改  pthread_create  的第二个参数
    //目前先这样吧
    pthread_create( (pthread_t*)thrdaddr , NULL , (void*(*)(void *))start_address , arglist );
	return (uintptr_t) thrdaddr;
}
DWORD GetCurrentThreadId()
{
	return pthread_self();
}

//此为系统信息头文件
//#include <sys/sysinfo.h>
//返回值：返回系统启动时间，用毫秒来计算
DWORD GetTickCount(void)
{
	struct timeval tmp;
	gettimeofday(&tmp, 0);
	return 1000*tmp.tv_sec+tmp.tv_usec/1000;
}

// 需要root 权限
BOOL  SetLocalTime(const SYSTEMTIME *lpSystemTime)
{
	return true;
}

//实现方法，用Linux函数time 取秒时间,localtime 来转换
//注意:不能正确获取毫秒时间，所以毫秒固定为0
void GetLocalTime( LPSYSTEMTIME lpSystemTime )
{
    if( lpSystemTime == NULL) return ;
    time_t now;                     //实例化time_t结构
    tm *timenow;                    //实例化tm结构指针
    time(&now);                     //time函数读取现在的时间(国际标准时间非北京时间)，然后传值给now
    timenow = localtime(&now);      //localtime函数把从time取得的时间now换算成你电脑中的时间(就是你设置的地区)
    lpSystemTime->wYear = (U16)timenow->tm_year + 1900;    //年，0为1900，所以要加1900
    lpSystemTime->wMonth = (U16)timenow->tm_mon + 1;       //月，0为1月，所以要+1
    lpSystemTime->wDayOfWeek = (U16)timenow->tm_wday;      //星期，0=星期日，1=星期一...
    lpSystemTime->wDay = (U16)timenow->tm_mday;            //日
    lpSystemTime->wHour = (U16)timenow->tm_hour;           //时
    lpSystemTime->wMinute = (U16)timenow->tm_min;          //分
    lpSystemTime->wSecond = (U16)timenow->tm_sec;          //秒
    lpSystemTime->wMilliseconds = (U16)0;                  //毫秒
}

