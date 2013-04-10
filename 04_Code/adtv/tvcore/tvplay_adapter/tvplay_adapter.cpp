/* 
***************************************************************************************************
*  FileName    : tvplay_adapter.cpp
*  Author      : jianglei      Date: 2012-05-02
*  Description : 
*--------------------------------------------------------------------------------------------------
*  History     :
*  <time>        <version >   <author>   	<desc>
*  2012-05-02       V1.0.0       jianglei             first release for amlogic
*
***************************************************************************************************
*/


#include "tvplay_adapter.h"
extern "C" {
#include <amports/vformat.h>
#include <amports/aformat.h>
#include <codec.h>
}
#include <utils/Log.h>
#include <utils/SystemClock.h>
#include <cutils/properties.h>
#include <utils/threads.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <dirent.h>
#include <unistd.h>
#include <TsPlayer.h>
#define  LOG_TAG "libtvadapter"
#include "tvlog.h"



#include <signal.h>
#include <sys/stat.h>
#include <netinet/in.h>
#include <sys/socket.h>

#include <utils/Log.h>
#include <pthread.h>

//#include "p2p_webserver.h"

#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/ioctl.h>
#include <sys/un.h>
#include <stdarg.h>
#include <errno.h>
#include <unistd.h>
#include <sys/select.h>
#include <sys/time.h>
#include <ringbuffer.h>

static FILE* gfStream=0;
#define DUMP_TSPACKET0 (0)

//int tvplay_
static CTsPlayer *player = NULL;
static CCircleBuf *loop_buffer = new CCircleBuf();
static int player_sign = 0;
static unsigned char ContentBuffer[100*188]={0};


int osd_blank(char *path,int cmd)
{
    int fd;
    char  bcmd[16];
    fd = open(path, O_CREAT|O_RDWR | O_TRUNC, 0644);

    if(fd>=0) {
        sprintf(bcmd,"%d",cmd);
        write(fd,bcmd,strlen(bcmd));
        close(fd);
        return 0;
    }

    return -1;
}

int set_tsync_enable(int enable)
{
    int fd;
    char *path = "/sys/class/tsync/enable";
    char  bcmd[16];
    fd = open(path, O_CREAT | O_RDWR | O_TRUNC, 0644);
    if (fd >= 0) {
        sprintf(bcmd, "%d", enable);
        write(fd, bcmd, strlen(bcmd));
        close(fd);
        return 0;
    }
    
    return -1;
}
void *player_thread(void *arg)
{
	char *lei = NULL;
#if 1
	int sin_size;
	int ret =0,temp=0,bufdatalen= 40*188;
	LOGI("player_thread Enter");
	
	int iActiveCount = 0;
	
	while (1)
	{
		if(0 == ++iActiveCount%1000){
			LOGI("player_thread is active.\n");
		}
		if(player_sign ==0)
		{
			LOGI("player_thread Break player_sign ==0 line=%d",__LINE__);
			break;
		}
		else if(player_sign ==2)
		{
			LOGI("player_thread reset codec line=%d",__LINE__);
			player_sign =1;
			player->ResetPlay();
		}
		
		temp = loop_buffer->ReadDataEx(ContentBuffer,bufdatalen);
		if(temp)
		{
			if(player_sign ==0)
			{
				LOGI("player_thread Break player_sign ==0 line=%d",__LINE__);
				break;
			}
			else if(player_sign ==2)
			{
				LOGI("player_thread reset codec line=%d",__LINE__);
				player_sign =1;
				player->ResetPlay();
			}
			ret=player->WriteData(ContentBuffer,bufdatalen);

			while(ret < 0)
			{
				if(player_sign ==0)
				{
					LOGI("player_thread Break player_sign ==0 line=%d",__LINE__);
					break;
				}
				else if(player_sign ==2)
				{
					LOGI("player_thread reset codec line=%d",__LINE__);
					player_sign =1;
					player->ResetPlay();
				}
				ret = player->WriteData(ContentBuffer,bufdatalen);
				if(ret >= 0)
				{
					LOGI("WriteData Write again ok");
					break;
				}
				else
				{
					LOGE("WriteData Write again failed,Cause calling Resetplay.\n");
					if(player_sign != 0)
					{
						//100毫秒,当播放器刚刚启动时,amcodec还未初使化完成
						//此线程向amcodec写数据会失败,重启播放器会导致严重问题.
						usleep(5000 * 100);
						player->ResetPlay();
					}
				}
			}
				
		}	
		else
		{
			//LOGI("circule buffer no data");
			usleep(50);
		}
		
		// usleep(100);
	}
#endif
	
	loop_buffer->ClrBuffer();
	
	LOGI("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA------------------Leave thread------player");
	return lei;
} 
static int set_sys_int(const char *path,int val)
{
	int fd;
	char  bcmd[16];
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
}
int tvplay_adapter_stop()
{
	LOGI("tvplay_adapter_stop.\n");
	if((player_sign == 0)||(NULL == player))
	{
		LOGE("play has not start \n");
		return -1;
	}
	player_sign = 0;
	
	player->Stop();
	
	// player->VideoHide();


#if 0
	delete player;
	player = NULL;
#endif

#if DUMP_TSPACKET0
	 if(gfStream){
		 fclose(gfStream);
	 }
#endif 
	
	return 0;
}

int tvplay_adapter_play(IN U16 vpid,IN U8 vstype,IN U16 apid,IN U8 astype)
{
	LOGI("tvplay_adapter_play(vpid=%d,stype=%d,apid=%d,stype=%d).\n",vpid,vstype,apid,astype);
	
#if DUMP_TSPACKET0
	gfStream = fopen("/data/test_0.ts","wb");
#endif 
#if 0
	if(player != NULL)
	{
		player_sign = 0;
		LOGI("erooraaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		player->Stop();
		player->VideoHide();
		delete player;
		player = NULL;
	}
#endif
	int videopid = vpid;
	int audiopid = apid;
	int videotype = vstype;
	int audiotype= astype;
	pthread_t  playerthread;
	if(player == NULL)
	{
		player=new CTsPlayer();
		player->SetColorKey(1,0);
		
	}
	set_tsync_enable(1);
//	player->VideoShow();
	VIDEO_PARA_T VideoPara;
	AUDIO_PARA_T AudioPara;
	memset(&AudioPara,0,sizeof(AudioPara));
	
	VideoPara.pid = vpid;
	AudioPara.pid = apid;
	switch(vstype)
	{
		case 1:
		case 2:
			VideoPara.vFmt=VFORMAT_MPEG12;
			break;
		case 16:
			VideoPara.vFmt=VFORMAT_MPEG4;
			break;
		case 27:
			VideoPara.vFmt=VFORMAT_H264;
			break;
		case 234:
			VideoPara.vFmt=VFORMAT_VC1;
			break;
		default:
			VideoPara.vFmt=VFORMAT_MPEG12;
			break;
		
	}

	switch(astype)
	{
		case 3:
		case 4:
			AudioPara.aFmt=AFORMAT_MPEG;
			break;
		case 15:
			AudioPara.aFmt=AFORMAT_AAC;
			break;
		case 0x7B:
		case 0x8A:
			AudioPara.aFmt=AFORMAT_DTS;
			break;
		case 0x81:
		case 0x6a:
		case 0x7A:
		case 6:
			AudioPara.aFmt=AFORMAT_AC3;
			break;
		
		default:
			AudioPara.aFmt=AFORMAT_MPEG;
			break;
		
	}

	//LOGI("videopid=%d,vfat=%d,audiopid=%d,afat=%d",videopid,VideoPara.vFmt,audiopid,AudioPara.aFmt);
	player->InitVideo(&VideoPara);
	player->InitAudio(&AudioPara);

#if 1
	player_sign = 1;
	if(pthread_create(&playerthread, NULL, player_thread, NULL) != 0) {
		LOGE("pthread_create error!");
	}
#endif
	//loop_buffer->ClrBuffer();
	if(!player->StartPlay()){
		LOGI("Player start failed\n");
		player_sign = 0;
		delete player;
		player = NULL;
		return -1;
	}
	LOGI("AudioPara.aFmt=%d,VideoPara.vFmt=%d",AudioPara.aFmt,VideoPara.vFmt);
	

	
	LOGE("after player->StartPlay !");
	//player->SetVideoWindow(0,0,1920,1080);
	//player->SetScreenMode(3);
	//osd_blank("/sys/class/graphics/fb0/blank",1);
	//osd_blank("/sys/class/graphics/fb1/blank",1);
	//set_tsync_enable(1);
	
	LOGE("before pthread_create !");
	
	return 0;
}

static int iTSDataCount = 0;
int tvplay_adapter_putTSData(IN BYTE *pTSData,IN U32 iDataSize)
{
#if 0
	if(0 == ++iTSDataCount%5000){
		LOGI("tvplay_adapter_putTSData is active player_sign=%d,iDataSize=%d.pTSData[0]=%x\n",player_sign,iDataSize,pTSData[0]);
	}
#endif
	if(player_sign ==0)
	{
		loop_buffer->ClrBuffer();
	//	player->ResetPlay();
		return -1;
	}
	// LOGTRACE(LOGINFO,"tvplay_adapter_putTSData(%p,%d).\n",pTSData,iDataSize);
	if((NULL == pTSData)||( pTSData[0] !=0X47))
	{
		// LOGE("Player rev data error,player_sign=%d,pTSData[0]=%d,\n",player_sign,pTSData[0]);
		
		return -1;
	}

#if 0
	if(player_sign == 1)
	{
		int ret=player->WriteData(pTSData,iDataSize);
		while(ret < 0)
		{
			ret = player->WriteData(pTSData,iDataSize);
			if(ret >= 0)
				break;
			else
				usleep(50);
		}
	}
#else
	bool bRet = loop_buffer->WriteDataEx(pTSData,iDataSize);
	if(!bRet){
		LOGE("tvplay_adapter_putTSData writeDataEx failed.\n");
	}
		
	 static int buffer_length =0 ;
	 #if DUMP_TSPACKET0 
	 	
		if(gfStream){
			fwrite(buffer,buffer_length,1,gfStream);
			fclose(gfStream);
			gfStream = NULL;
	 	}
			
	 #endif 
#endif

	return 0;
}
int tvplay_setVideoWindow(int x,int y,int width,int height)
{
	LOGI("tvplay_setVideoWindow-x=%d,y=%d,width=%d,height=%d",x,y,width,height);
#if 0
	int ret = -1;
	if(player !=NULL)
		ret = player->SetVideoWindow(x,y,width,height);
	else
		LOGE("tvplay_setVideoWindow-class player has not built");
	
	LOGI("tvplay_setVideoWindow leave ret=%d ",ret);
	return ret;
#else
	
	int fd;
	char *path = "/sys/class/video/axis" ;
	char  bcmd[32];

	fd = open(path, O_CREAT | O_RDWR | O_TRUNC, 0644);
	if (fd >= 0) {
		sprintf(bcmd, "%d %d %d %d", x, y, x + width, y + height);
		write(fd, bcmd, strlen(bcmd));
		close(fd);
		return 0;
	}
	return -1;

//	chehl
/*
	char *path = "/sys/class/video/axis" ;
	char  bcmd[256] ={0};
	sprintf(bcmd, "echo %d %d %d %d>%s", x, y, x+width, y+height,path);
	int ret = system(bcmd);
	return ret >= 0 ? 0 : -1;
	*/
#endif
}
int tvplay_setScreenMode(int mode)
{
	LOGI("tvplay_setScreenMode enter mode =%d",mode);
	int ret = -1;
	if(player !=NULL)
		ret = player->SetScreenMode(mode);
	else
		LOGE("tvplay_setScreenMode-class player has not built");

	LOGI("tvplay_setScreenMode leave ret=%d ",ret);
	return ret;
}

int tvplay_set_black_policy(int blackout)
{
	LOGI("tvplay_set_black_policy enter blackout =%d",blackout);// 1:black_screen
	int ret = -1;
	if(player !=NULL)
		ret = player->Set_black_policy(blackout);
	else
		LOGE("tvplay_set_black_policy-class player has not built");
	LOGI("tvplay_set_black_policy leave ret=%d ",ret);
	return ret;
}
int tvplay_getVolume()
{
	LOGI("tvplay_getVolume enter ");
	int volume = 0;
	if(player !=NULL)
		volume=player->GetVolume();
	else
		LOGE("tvplay_getVolume-class player has not built");
	LOGI("tvplay_getVolume leave volume=%d ",volume);
	return volume;
}
int tvplay_setVolume(float volume)
{
	LOGI("tvplay_setVolume enter ");
	int ret = -1;
	if(player !=NULL)
	{
		if(volume > 40)
			volume = 40;
		ret = player->SetVolume(volume);
	}
	else
		LOGE("tvplay_setVolume-class player has not built");
	LOGI("tvplay_setVolume leave volume=%d ret=%d",volume,ret);
	return volume;
}
int tvplay_SetChannel(int index)
{
	LOGI("tvplay_SetLeftMono enter ");
	int ret = 0;
	if(player !=NULL)
	{
		player->Setmono(index);
	}
	else
		LOGE("tvplay_Setchannel-class player has not built");
	LOGI("tvplay_Setchannel leave ret=%d ",ret);
	return ret;
}
int tvplay_SetMute(int sign)
{
	int ret = 0;
	LOGI("tvplay_SetMute enter ");
	if(player !=NULL)
		ret=player->SetMute(true);
	else
		LOGE("tvplay_SetMute-class player has not built");
	return ret;
}

int tvplay_ClearVideoLayer(int sign)
{
	int ret = -1;
	LOGI("tvplay_ClearVideoLayer enter  sign=%d",sign);
	ret = set_sys_int("/sys/class/video/disable_video",sign);
	return ret;
}
int tvplay_adapter_clsBuffer()
{
	if(loop_buffer){
		loop_buffer->ClrBuffer();
	}
	
	if(player_sign == 1)
		player_sign = 2;
	LOGI("AAAAAAAAAAAAAA-tvplay_adapter_clsBuffer");
	return 0;
}
