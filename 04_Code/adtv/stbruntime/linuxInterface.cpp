#include <stdio.h>

//�����źŵƲ��ܿ���̷��ʣ��ȴ���ʱ��Ҳû�г�ʱ�ĸ��������
#include <stdlib.h>
//��ͷ�ļ������������źŵ�ϵ�нṹ������
#include <semaphore.h>
//��ͷ�ļ������������źŵ�ϵ�к���
// #include <sys/sem.h>
//��ͷ�ļ�������IPCϵ�е����Լ�ֵ
#include <sys/ipc.h>
#include <string.h>
//���󷵻�ͷ
#include <errno.h>
#include <iostream>
#include <dlfcn.h>
#include <sys/sysinfo.h>
#include <sys/stat.h>
#include <threads.h>

#include "stbruntime.h"

using namespace std;

//�����źŵƽṹ��
typedef struct _NONAMESEM
{
    //�����źŵ��ڲ��ṹ
    sem_t m_Sem;
}NoNameSem , * LPNoNameSem;

//�����źŵƽṹ��
typedef struct _NAMESEM
{
    //�����źŵ�ID
    int m_SegID;
    //�����źŵ�����
    char m_SegName[20];
}NameSem , * LPNameSem;

typedef struct _HANDLEOBJECT
{
    //ͬ�����ͣ������źŵ�Ϊ1�������źŵ�Ϊ2
    unsigned int m_Type;
    //�����źŵƽṹ��
    NoNameSem  m_NoNameSem;
    //�����źŵƽṹ��
    NameSem m_NameSem;

}HandleObject , *LPHandleObject;

//�ȴ��ں��¼����źŵ�
//�����źŵ�û����ʱ����

//�رվ��

//�����źŵ�
//����1����ȫ���ԣ�û����
//����2����ʼ������
//����3��������
//����4���ؼ������������ú��ֺ��������źţ������֣�����semget��û�����־���sem_init

//�����źŵ�
//����1���ź������
//����2�����ӵļ���
//����3��ԭ���ļ�����������ʽ�޷�����



////////////////////////////////////////////////////////////////////////////////
// ��̬���ӿ�ӿ�
////////////////////////////////////////////////////////////////////////////////
//��Ҫ��GCC����ʱ����һ������ -ldl
//��ͷ�ļ�Ϊ���ض�̬���ӿ��ͷ�ļ�
//#include <dlfcn.h>
//���ض�̬���ӿ�
HANDLE LoadLibrary( LPCSTR lpLibFileName )
{
    //ʵ�ַ���������ϵͳ����ֱ��ʵ��
    //                                      ��֤�Ժ���صĿ����ʹ���Լ�  �����ж��Լ���������
	char buffer[512];
    return (HANDLE)dlopen(buffer, RTLD_GLOBAL       |       RTLD_NOW  );
}

BOOL FreeLibrary( HANDLE hLibModule )
{
    //ʵ�ַ���������ϵͳ����ֱ��ʵ��
    return dlclose( hLibModule );
}

LPVOID GetProcAddress( HANDLE HANDLE , LPCSTR lpProcName )
{
    //ʵ�ַ���������ϵͳ����ֱ��ʵ��
    return dlsym( HANDLE , lpProcName );
}
////////////////////////////////////////////////////////////////////////////////
// �¼��ӿ�
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

//�����ź�
BOOL SetEvent( HANDLE hEvent )
{
	android::Condition *cond = (android::Condition *)hEvent;
	cond->signal();
	return TRUE;
}

////////////////////////////////////////////////////////////////////////////////
// �߳̽ӿ�
////////////////////////////////////////////////////////////////////////////////
//���̲߳�����Ҫ����һ�����ļ���libpthread
//����GCC�����ʱ����Ҫ����һ�� -lpthread ����
//��ͷ�ļ�Ϊ���̲߳�����ͷ�ļ�
//#include <pthread.h>
//�����߳�
//����һ����ȫ����
//����������ʼ����ջ��С
//���������̺߳���
//�����ģ��̲߳���
//�����壺�̳߳�ʼ��״̬
//�����������ز������߳�ID
//����ֵ���̴߳����Ƿ�ɹ�
uintptr_t _beginthreadex(
   void *security,
   unsigned stack_size,
   unsigned ( __stdcall *start_address )( void * ),
   void *arglist,
   unsigned initflag,
   unsigned *thrdaddr
   )
{
    //ʵ�ַ�������Linux����ʵ��
    //Ŀǰȱ�㣺�޷����ð�ȫ���ԡ���ջ��С�Լ���ʼ��״̬
    //Ĭ��Ϊ��ջ�Զ����䣬��ʼ��֮����������
    //����Ҫ���ģ�Ҫ��  pthread_create  �ĵڶ�������
    //Ŀǰ��������
    pthread_create( (pthread_t*)thrdaddr , NULL , (void*(*)(void *))start_address , arglist );
	return (uintptr_t) thrdaddr;
}
DWORD GetCurrentThreadId()
{
	return pthread_self();
}

//��Ϊϵͳ��Ϣͷ�ļ�
//#include <sys/sysinfo.h>
//����ֵ������ϵͳ����ʱ�䣬�ú���������
DWORD GetTickCount(void)
{
	struct timeval tmp;
	gettimeofday(&tmp, 0);
	return 1000*tmp.tv_sec+tmp.tv_usec/1000;
}

// ��Ҫroot Ȩ��
BOOL  SetLocalTime(const SYSTEMTIME *lpSystemTime)
{
	return true;
}

//ʵ�ַ�������Linux����time ȡ��ʱ��,localtime ��ת��
//ע��:������ȷ��ȡ����ʱ�䣬���Ժ���̶�Ϊ0
void GetLocalTime( LPSYSTEMTIME lpSystemTime )
{
    if( lpSystemTime == NULL) return ;
    time_t now;                     //ʵ����time_t�ṹ
    tm *timenow;                    //ʵ����tm�ṹָ��
    time(&now);                     //time������ȡ���ڵ�ʱ��(���ʱ�׼ʱ��Ǳ���ʱ��)��Ȼ��ֵ��now
    timenow = localtime(&now);      //localtime�����Ѵ�timeȡ�õ�ʱ��now�����������е�ʱ��(���������õĵ���)
    lpSystemTime->wYear = (U16)timenow->tm_year + 1900;    //�꣬0Ϊ1900������Ҫ��1900
    lpSystemTime->wMonth = (U16)timenow->tm_mon + 1;       //�£�0Ϊ1�£�����Ҫ+1
    lpSystemTime->wDayOfWeek = (U16)timenow->tm_wday;      //���ڣ�0=�����գ�1=����һ...
    lpSystemTime->wDay = (U16)timenow->tm_mday;            //��
    lpSystemTime->wHour = (U16)timenow->tm_hour;           //ʱ
    lpSystemTime->wMinute = (U16)timenow->tm_min;          //��
    lpSystemTime->wSecond = (U16)timenow->tm_sec;          //��
    lpSystemTime->wMilliseconds = (U16)0;                  //����
}

