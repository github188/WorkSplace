#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#include <fcntl.h>
#include <com_ismartv_util_JniSetting.h>
#define LOG_TAG "jnisetting"
#include <utils/Log.h>


JNIEXPORT jint JNICALL Java_com_ismartv_util_JniSetting_setDisplayMode
  (JNIEnv *env, jobject obj, jint val)
{
	LOGI("hello,%1$d",val); 
	/**
	FILE* file = fopen("/sys/class/video/screen_mode","w+"); 
    if (file != NULL)
    {
        fputc(val, file);
        fflush(file);
        fclose(file);
    }
	**/
	
	int fd;
	char  bcmd[16];
	char *path = "/sys/class/video/screen_mode" ;
	fd=open(path, O_CREAT|O_RDWR | O_TRUNC, 0644);
	if(fd>=0)
	{
		sprintf(bcmd,"%d",val);
		write(fd,bcmd,strlen(bcmd));
		LOGI("set fs%s=%d ok\n",path,val);
		close(fd);
		return 0;
	}
	LOGI("set fs %s=%d failed\n",path,val);
	return -1;
	
  	return 0 ;
}

JNIEXPORT jint JNICALL Java_com_ismartv_util_JniSetting_getDisplayMode
  (JNIEnv *env, jobject obj)
{
	LOGI("JNICALL Java_com_ismartv_util_JniSetting_getDisplayMode  enter"); 

	int fd;
	char  bcmd[16];
	char *path = "/sys/class/video/screen_mode" ;
	fd=open(path, O_CREAT|O_RDWR | O_TRUNC, 0644);
	if(fd>=0)
	{
                read(fd,bcmd,strlen(bcmd));
		LOGI("getDisplayMode---Read-bcmd=%s\n",bcmd);
                //sprintf(val,"%d",bcmd);
                LOGI("get fs%s=%d ok\n",path,atoi(bcmd));
		close(fd);
		return atoi(bcmd);
	}

        LOGI("JNICALL Java_com_ismartv_util_JniSetting_getDisplayMode  leave"); 
	
  	return -1;
}
/**
JNIEXPORT jstring JNICALL Java_com_ismartv_util_JniSetting_getSTBID
  (JNIEnv *env, jobject obj)
{
	int ret = 0;
	char pIDBuf[16]={0};
	int iBufSize;
	void * fd = 0;
	fd = tvdevice_open();
	if(fd <= 0)
	{
		return env->NewStringUTF((char *)"");
	}	
	ret = tvdevice_getStbID(fd,pIDBuf,12);
	tvdevice_close(fd);
	char pResult[16] = {0};
	sprintf(pResult,"%02x%02x%02x%02x%02x%02x",pIDBuf[0],pIDBuf[1],pIDBuf[2],pIDBuf[3],pIDBuf[4],pIDBuf[5]);
	return (env)->NewStringUTF(pResult); 
}
**/
  
