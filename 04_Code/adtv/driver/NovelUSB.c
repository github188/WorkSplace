#include <linux/kernel.h>
#include <linux/sched.h>
#include <linux/signal.h>
#include <linux/errno.h>
#include <linux/poll.h>
#include <linux/init.h>
#include <linux/slab.h>
#include <linux/fcntl.h>
#include <linux/module.h>
#include <linux/spinlock.h>
#include <linux/kref.h>

#include <linux/usb.h>
#include <linux/list.h>
#include <asm/uaccess.h>
#include <stdarg.h>
#include <linux/workqueue.h>

#include <linux/vmalloc.h>
#include <linux/completion.h>
#include "RT10UPreg.h"

#include <linux/delay.h>
#include <linux/types.h>
#include <linux/fs.h>
#include <linux/string.h>
#include <linux/timer.h>
#include <linux/timex.h>
#include <linux/rtc.h>
#include "NovelUSB.h"
#include "Rt10upDriver.h"
#ifdef USE_FIRWARE_FILE
//#define FILE_DIR "/data/rt10up_firmware.bin"
#define FILE_DIR "./rt10up_firmware.bin"
#else
#include "firmwareRelease.h"
#endif
#ifndef UBUNTU_OS
#include <linux/smp_lock.h>
#include <linux/spinlock.h>
#endif
//#define TEST_TIME
//#define TEST_IORW_TIME
//#define TEST_TS_CC
//#define TEST_INITFIRMWARE_TIME
//#define TEST_SCRESET_TIME


#ifdef OUTPRINT_FILE
struct file *out_fp;
#endif




#define OS_27MHZ 0
#define ADPU_RETRY 600
#define SCRESET_RETRY 120




#if OS_27MHZ
#define OSCFREQUENCY 28687
#else
#define OSCFREQUENCY 28920
#endif
#define DEMOD2_REG_NUM 16

#define DEMOD2_DEV (0x38>>1)
#define TUNERD2_DEV (0xC2>>1)


/**********************gloables*************************/
static ULONG	gpidnum[3] = {0};//the number of pid
static USHORT	gindex[3] = {0};//index
static ULONG	gpidFilter[3]  = {0};//filter
static ULONG	gpidSatus[3][32] = {{0}};//pid ststus	

static UCHAR	glocked[3] = {0}; 	//locked					

static ULONG	gWritePosition[4] = {0},gReadPosition[4] = {0};//read and write pionter
static ULONG	gbulkflag[4] = {0};	//the number of data buffer for bulk  
static bool 	gKillThread = 0;//driver thread flag
static int  	gstartsync=0;   //record 0x47 
static ULONG 	readts=0;		//read piont in one bulk 
static char 	firstBulkflag=-1;	//data is the first bulk flag 



static NOVEL_USB_FREQ_SET_CONTROL gcurrentFreq;
static COMPLETION	gTSThreadDone;//task  ready finish
static COMPLETION	gdone[TSURBCOUNT];//bulk ready ok
static int	gpipe = 0;//pipe

#ifdef USE_FIRWARE_FILE 
static PUCHAR   grt10up_fw = NULL;//firware data pointer
#endif
static UCHAR    *gpAddr_TS[4];//driver buff
static UCHAR    *gbulk_in_buffer[TSURBCOUNT];//usb bulik
static USBDEV	*gpDev = NULL;//handle for device
static URB 		*gbulk_urb[TSURBCOUNT];//urb
static WORKQUEUE *gpTSqueue = NULL;//squene
static WORKTASK gTSwork;//work task
#ifdef TEST_TS_CC
ULONG gTScomtinuities0[0x2000] ={0};
PID_COUNTINUER packet_continue[0x2000]={{0,0}};
int Transport_packet_continuity_check(ULONG tunernumber,UCHAR* tspacket,ULONG* TScomtinuities);
#endif


/**********************end*************************/

#ifndef UBUNTU_OS	
spinlock_t TS_lock[4];
spinlock_t SETPID_lock_;
#endif

#ifdef USE_PROCTECT_INTERFACE	
spinlock_t PROTECT_REG_WR_lock_;
#endif


#if OS_27MHZ
/* os 27Mhz 晶振使用此参数*/
static UCHAR g_NEXUS_Platform_InitValues[DEMOD2_REG_NUM] = 
{ 
	0xa4, 0xc4, 0x1e, 0x23, 0xe0, 0x32, 0xab, 0x87, 0xb1, 0x05, 0x86, 0x1a, 0x10, 0x01, 0x2b, 0xc3 
};
#else
/* os 28.92Mhz 晶振使用此参数*/
static UCHAR g_NEXUS_Platform_InitValues[DEMOD2_REG_NUM] =
{ 
	0xa4, 0xc4, 0x1e, 0x23, 0xe0, 0x32, 0xab, 0x87, 0xb1, 0x06, 0x9c, 0x19, 0x0b, 0x01, 0x2b, 0xc3 
};
#endif


//内部寄存器参数

static UCHAR reg8000[9] = {0x36,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
static UCHAR reg8001[9] = {0x45,0x0f,0x45,0x01,0xe7,0x6c,0x9a,0x5c,0x88};
static UCHAR reg8002[9] = {0xba,0xa8,0x59,0xe1,0xb8,0x92,0x81,0xfa,0xf3};
static UCHAR reg8003[9] = {0xc8,0x84,0x70,0x00,0x00,0x00,0x00,0x00,0x00};
static UCHAR reg8004[9] = {0x42,0xa2,0x86,0xfd,0x7f,0x37,0x02,0x60,0x69};
static UCHAR reg8005[9] = {0xfd,0xef,0xc5,0x3a,0x87,0x4f,0x12,0x21,0x7a};
static UCHAR reg8006[9] = {0x82,0x36,0xfc,0x6f,0xec,0x64,0xde,0x30,0xd1};
static UCHAR reg8007[9] = {0x2e,0x08,0x62,0x78,0x00,0x00,0x00,0x00,0x00};
static UCHAR reg8008[9] = {0x00,0x00,0x05,0xa0,0x00,0xb4,0x26,0x4c,0x5a};
static UCHAR reg8009[9] = {0x00,0x00,0x02,0xa4,0x84,0xe2,0x00,0x4e,0x20};
static UCHAR reg800a[9] = {0x00,0x38,0x40,0x45,0x1b,0x19,0xab,0xe9,0xa4};
static UCHAR reg800b[9] = {0x01,0xa1,0x41,0x33,0x66,0x34,0x67,0x43,0x45};
static UCHAR reg800c[9] = {0x00,0x09,0x99,0x9a,0x21,0xba,0xd0,0x66,0x66};
static UCHAR reg800d[9] = {0x00,0x00,0x09,0xc4,0x00,0x7d,0x00,0x07,0xd0};
static UCHAR reg800e[9] = {0x00,0x00,0x02,0x80,0x00,0x6d,0x60,0x75,0x30};
static UCHAR reg800f[9] = {0x00,0x0f,0x01,0x38,0x83,0xe0,0x83,0x08,0x20};
static UCHAR reg8010[9] = {0x00,0x3d,0x00,0x1a,0x00,0x00,0x18,0x00,0x25};
static UCHAR reg8011[9] = {0x03,0xc8,0x00,0x08,0x00,0x81,0xf5,0xce,0x66};
static UCHAR reg8012[9] = {0x00,0x01,0x0f,0xe1,0x6a,0x1e,0x42,0xc0,0x3c};
static UCHAR reg8013[9] = {0x00,0x01,0x03,0x12,0xa1,0x0a,0x12,0xb1,0x0c};
static UCHAR reg8014[9] = {0x00,0x00,0x02,0xe0,0x08,0x40,0x0c,0x00,0x28};
static UCHAR reg8015[9] = {0x00,0x00,0x03,0xa0,0x03,0xc0,0x0c,0x80,0x10};

#if OS_27MHZ
/* os 27Mhz 晶振使用此参数*/
static UCHAR reg8016[11] = 
{
	0x80, 0x16, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x89, 0x50
};
#else
/* os 28.92Mhz 晶振使用此参数*/
static UCHAR reg8016[/*11*/9] = 
{
	/*0x80, 0x16,*/ 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x81, 0x50
};
#endif

static void  WriteRegValueEx72bit( ULONG reg, USBDEV *udev, PUCHAR buf, UCHAR tunernumber );
static void  EnableICTuner(USBDEV *udev,UCHAR TunerNum, int On0ff); 
static int   tuner_write_TDAE3(USBDEV *udev,ULONG freq, UCHAR TunerNum );
static int	 StartThread(void);
static void  StopThread(void);
static int Ezusb_APDU(USBDEV	*Dev, PUCHAR pAPDUData, USHORT APDULen, PUCHAR pRSPData, USHORT *pRSPLen );
static int Ezusb_APDU1(USBDEV *Dev, PUCHAR pAPDUData, USHORT APDULen, PUCHAR pRSPData, USHORT *pRSPLen );
static ULONG CheckSmartCardStatus(USBDEV *Dev);
int Rtusb_Reset(USBDEV *udev,UCHAR resetBit);
int Rtusb_ResetParentPort(USBDEV *udev,UCHAR resetBit );




static int ___usb_control_msg__(USBDEV *dev, unsigned int pipe, UCHAR request,UCHAR requesttype, USHORT value, USHORT index, void *data,USHORT size, int timeout)
{
	int ret;
#ifdef TEST_IORW_TIME	
	struct timex  txc;
	struct timex  txc2;
#endif 
#ifdef USE_PROCTECT_INTERFACE	
	spin_lock(&PROTECT_REG_WR_lock_);
#endif	
#ifdef TEST_IORW_TIME				
	do_gettimeofday(&(txc.time));
#endif
	ret = usb_control_msg(dev, pipe,request,requesttype,value, index,data, size, 2000);
	if(ret != size)
	{
		ERROR_OUT("########################usb_control_msg ret=%d,request=0x%x,addr=0x%x$$$$$$$$$$$$$$$\n",ret,request,value);
	}
#ifdef TEST_IORW_TIME		//get end time 
	do_gettimeofday(&(txc2.time));
	if(ret == size)
	{
		printk("\n#######___usb_control_msg__size request=0x%x requesttype(RW)=0x%x addr=0x%x size =%d,cost time######## %ld\n",request,requesttype,value,size,(txc2.time.tv_sec*1000000+txc2.time.tv_usec)-(txc.time.tv_sec*1000000+txc.time.tv_usec));
	}
	else
	{
		printk("\n####___usb_control_msg__#########usb_control_msg fail %d\n",ret);
	}
#endif


#ifdef USE_PROCTECT_INTERFACE	
	spin_unlock(&PROTECT_REG_WR_lock_);
#endif	
	return ret;
}

/****************************************************************
* Description: 	change hex number to char.
* Input:			hex number
* Return:		char
****************************************************************/
char getHex(UCHAR v)
{
	return (v&0xf)<=0x9 ? (v&0xf)+'0' : ((v&0xf)-0xa)+'A';
}

/****************************************************************
* Description: 	print char.
* Input:			char* 
*					buffer size
* Return:			char*
****************************************************************/
char *PrintHex(const UCHAR *pBuf,USHORT BufSize)
{
	static char s[1024];
	USHORT i=0;
	s[0]='\t';
	for( i=0; (i<BufSize) && ((i*3+2)<1024); i++)
	{
		s[1+i*3+0]=getHex(pBuf[i]>>4);
		s[1+i*3+1]=getHex(pBuf[i]&0xf);
		s[1+i*3+2]=' ';
		s[1+i*3+3]=0;
	}

	s[1023]='\0';
	return s;
}


int NOVEL_GetSTBID( USBDEV *Dev,UCHAR * pwPlatformID, UCHAR * pdwUniqueID)
{
	UCHAR buf[ CA_DATA_MAX ]={0};
	int ret;

	buf[0]=0x04;
	////ANCHOR_LOAD_INTERNAL=0xb0=11000000,表示可以是USB标准命令，也可以用户自定义命令，此处为自定义命令
	//0x40表示用户命令，从主机到设备，
	//addr2个字节，高字节是msg的类型（1为输入，2为输出，3为特性）；低字节为msg的ID（预设为0）
	ret=___usb_control_msg__(Dev,usb_sndctrlpipe(Dev, 0),ANCHOR_LOAD_INTERNAL,0x40,0,0,buf,CA_DATA_MAX,0); 
	if(ret != CA_DATA_MAX)
	{
		ERROR_OUT("NOVEL_GetSTBID,___usb_control_msg__ ret = %d\n",ret);
	}
	msleep( 10 );
	ret=___usb_control_msg__(Dev,usb_rcvctrlpipe(Dev, 0),ANCHOR_LOAD_INTERNAL,0xc0,0,0,buf,CA_DATA_MAX,0);		
	if(ret != CA_DATA_MAX)
	{
		ERROR_OUT("NOVEL_GetSTBID,___usb_control_msg__ ret = %d\n",ret);
	}
	if(buf[2]==0x81&&buf[3]==0x01)
	{
		buf[2]=0x91;
		buf[3]=0x02;
	}  

	memcpy( (PUCHAR)pwPlatformID, buf+2, 2 );
	memcpy( (PUCHAR)pdwUniqueID, buf+4, 4 );
	DEBUGPRINTF("pwPlatformID =%02x %02x,pdwUniqueID=%02x %02x %02x %02x\n",pwPlatformID[0],pwPlatformID[1],pdwUniqueID[0],pdwUniqueID[1],pdwUniqueID[2],pdwUniqueID[3]);
	return NOERROR;

}
#ifdef ANDRIOD_LINUX
int NOVEL_GetDataFromSmartCard(USBDEV *Dev, PUCHAR pCMDBuffer,USHORT cmdLength,PUCHAR pRsBuffer,USHORT *pRslen)
{

	USHORT IOBytes = 0;
	USHORT i,j;
	int ret;
	DBG_SC_PRINT("\n###### Enter NOVEL_GetDataFromSmartCard>>>#########################################cmdLength = %d\n",cmdLength);
	if(cmdLength < 5/*||(*pRslen) < 2*/)
	{
		ERROR_OUT("NOVEL_GetDataFromSmartCard param error,0x%x,line=%d\n",cmdLength,__LINE__);
		*pRslen = 0;
		return	-1;
	}
	*pRslen = 0;
#ifdef DEBUG_HEX	
	for(i = 0; i < cmdLength;i++)
	{
		DBG_SC_PRINT("%x ",pCMDBuffer[i]);
	}
	DBG_SC_PRINT("\n ");

#endif

	ret = Ezusb_APDU(Dev, pCMDBuffer, 5, pRsBuffer, &IOBytes );
	
	DBG_SC_PRINT("\n######NOVEL_GetDataFromSmartCard start >>>Ezusb_APDU end ret = %dIOBytes %d,line=%d\n",ret,(int)IOBytes,__LINE__);
	if ( ret == SCDATAOK)
	{
		if ( IOBytes == 1 )
		{
			ret = Ezusb_APDU1(Dev, pCMDBuffer + 5, cmdLength - 5, pRsBuffer, &IOBytes );
			if ( ret != SCDATAOK)
			{
				ERROR_OUT("NOVEL_GetDataFromSmartCard from Ezusb_APDU1 ret =%d IOBytes %d\n",ret,(int)IOBytes);
				*pRslen = 0;
				ret = -1;
			}
			else
			{
				*pRslen = IOBytes;
			}
		}
		else if ( IOBytes > 2 )
		{
			IOBytes--;

			for ( i = 0;i < IOBytes;i++ )
			{
				pRsBuffer[ i ] = pRsBuffer[ i + 1 ];
			}
			*pRslen = IOBytes;
		}
		else
		{
			*pRslen = IOBytes;
		}
		
	}
	else
	{
		ERROR_OUT("NOVEL_GetDataFromSmartCard from Ezusb_APDU error ret =%d IOBytes %d\n",ret,(int)IOBytes);
		*pRslen = 0;
		ret = -1;
	}
	if(IOBytes >= CA_TRAM_CMD_MAX)
	{
		*pRslen = CA_TRAM_CMD_MAX-1;
	}
#ifdef DEBUG_HEX		
	for(i = 0; i < IOBytes;i++)
	{
		DBG_SC_PRINT("%x ",pRsBuffer[i]);
	}
#endif
	DBG_SC_PRINT("#####NOVEL_GetDataFromSmartCard end>>>>##########################################IOBytes=%d\n",(int)IOBytes);
	return ret;

}
#else
LONG NOVEL_GetDataFromSmartCard(USBDEV *Dev, PUCHAR pCMDBuffer,USHORT cmdLength,PUCHAR pRsBuffer,USHORT *pRslen)
{

	USHORT IOBytes = 0;
	USHORT i;
	LONG ret = 0;
	*pRslen = 0;
	if(cmdLength < 5)
	{
		return	-1;
	}
	DBG_SC_PRINT("enter NOVEL_GetDataFromSmartCard ret=%x\n##################pCMDBuffer=%x,%x,%x,%x,%x,%x %x,%x,%x,%x, cmdLength =%d\n",ret ,pCMDBuffer[0],pCMDBuffer[1],pCMDBuffer[2],pCMDBuffer[3],pCMDBuffer[4],pCMDBuffer[5],pCMDBuffer[6],pCMDBuffer[7],pCMDBuffer[8],pCMDBuffer[9],cmdLength);
	ret = Ezusb_APDU( Dev,pCMDBuffer, 5, pRsBuffer, &IOBytes );
	DBG_SC_PRINT("NOVEL_GetDataFromSmartCard ret=%x\n##################pRsBuffer=%x,%x,%x,%x,%x,%x IOBytes=%d\n",ret ,pRsBuffer[0],pRsBuffer[1],pRsBuffer[2],pRsBuffer[3],pRsBuffer[4],pRsBuffer[5],IOBytes);
	if ( ret == SCDATAOK)
	{
		if ( IOBytes > 2 )
		{
			IOBytes--;

			for ( i = 0;i < IOBytes;i++ )
				pRsBuffer[ i ] = pRsBuffer[ i + 1 ];
		}
		else
		{
			if ( IOBytes == 1 )
			{
				Ezusb_APDU( Dev,pCMDBuffer + 5, cmdLength - 5, pRsBuffer, &IOBytes );
				DBG_SC_PRINT("##################pRsBuffer%x %x\n",pRsBuffer[0],pRsBuffer[1]);
			}
		}
		*pRslen = IOBytes;
	}
	else
	{
		ERROR_OUT("##NOVEL_GetDataFromSmartCard############Ezusb_APDU: ret != SCDATAOK(%d)\n",ret);
	}
	return IOBytes;

}

#endif

static int Ezusb_APDU1(USBDEV *Dev, PUCHAR pAPDUData, USHORT APDULen, PUCHAR pRSPData, USHORT *pRSPLen )
{
	UCHAR Data[ 8 ]={0};
	int ret;
	ULONG RLen, Cnt;
	ULONG i = 0,j=0;
	ULONG times = 0;
	UCHAR temp[CA_TRAM_CMD_MAX]={0};
	*pRSPLen = 0;
#ifdef DEBUG_HEX	
	DEBUGPRINTF("\nEzusb_APDU1-------------------------------data send to card----------------\n");
	for( j=0; j<APDULen; j++) 
	{
		DEBUGPRINTF("%02x  ", pAPDUData[j]);
	}
	DEBUGPRINTF("\nEzusb_APDU1------------------------------------end------------------------\n");  
#endif	
	ret=___usb_control_msg__(Dev,usb_sndctrlpipe(Dev, 0),MemoryInterface,0x40,SmartCard-1,0,pAPDUData,APDULen+1,0); 
	msleep(1);
	if ( ret == (APDULen+1) )
	{
		Data[ 0 ] = APDULen & 0xff;
		Data[ 1 ] = ( APDULen >> 8 ) & 0xff;
		ret = ___usb_control_msg__(Dev,usb_sndctrlpipe(Dev, 0),MemoryInterface,0x40,SCsendl,0,Data,2,0); 
		msleep(1);
		if(ret != 2)
		{
			ERROR_OUT("Ezusb_APDU1,___usb_control_msg__ ret = %d,line=%d\n",ret,__LINE__);
		}
		Data[ 0 ] = 0x2;
		ret = ___usb_control_msg__(Dev,usb_sndctrlpipe(Dev, 0),MemoryInterface,0x40,SCcon,0,Data,1,0); 
		msleep(1);
		if(ret != 1)
		{
			ERROR_OUT("Ezusb_APDU1,___usb_control_msg__ ret = %dline=%d\n",ret,__LINE__);
		}
		for ( times = 0;times < ADPU_RETRY;times++ )
		{
			ret = CheckSmartCardStatus( Dev);

			if ( ret==SCDATAOK )
			{
				break;
			}

			if ( (ret == SCTIMEOUT)||(ret == SCNONE) )
			{
				return -ret;

			}

			msleep( 10 );
		}

		if ( times == ADPU_RETRY )
		{
			ERROR_OUT("22222222222##################################Ezusb_APDU1 SCTIMEOUT line=%d, CheckSmartCardStatus return =%d\n",__LINE__,ret);
			return -SCTIMEOUT;
		}

		ret=___usb_control_msg__(Dev,usb_rcvctrlpipe(Dev, 0),MemoryInterface,0xc0,SCcon,0,Data,8,0); 
		msleep(1);
		if ( ret == 8 )
		{
			if ( !( Data[ 1 ] & 0x80 ) )
			{
				return -SCNONE;
			}

			if ( Data[ 1 ] & 0x20 )
			{
				return -SCTIMEOUT;

			}

			RLen = Data[ 4 ] + ( Data[ 5 ] << 8 );

			//if ( RLen > 270 )
			if ( RLen > (CA_TRAM_CMD_MAX-5) )
			{
				ERROR_OUT("Ezusb_APDU1 retunr RLen error Rlen = %d,line=%d\n",(int)RLen,__LINE__);
				return -SCNONE;
			}
			
			ret = ___usb_control_msg__(Dev,usb_rcvctrlpipe(Dev, 0),MemoryInterface,0xc0,SmartCard,0,pRSPData,RLen,0); 
			msleep(1);
			if(ret != RLen)
			{
				ERROR_OUT("Ezusb_APDU1 ___usb_control_msg__ errorret=%d\n",ret);
			}
			while ( ( pRSPData[ i ] == 0x60 ) && (RLen > 0) )
			{
				i++;
				RLen--;
			}

			if ( i > 0 )
			{
				Cnt = i;

				for ( i = 0;i < RLen;i++ )
				{
					pRSPData[ i ] = pRSPData[ i + Cnt ];
				}
			}

			*pRSPLen = RLen;

			return SCDATAOK;

		}
		else
		{
			ERROR_OUT("_APDU1 __usb_control_msg__=%d\n",ret);
		}
	}
	else
	{
		ERROR_OUT("_APDU1 APDULen__usb_control_msg__=%d\n",ret);
	}
	return -SCTIMEOUT;
}


#ifdef ANDRIOD_LINUX
static int Ezusb_APDU(USBDEV 	*Dev, PUCHAR pAPDUData, USHORT APDULen, PUCHAR pRSPData, USHORT *pRSPLen )
{
#ifdef TEST_TIME	
	struct timex  txc;
	struct rtc_time tm; 
#endif    
	UCHAR Data[ 8 ]={0};
	int ret;
	ULONG RLen, Cnt;
	ULONG i = 0,j=0;
	ULONG times = 0;
	UCHAR temp[ 300 ]= {0};
	*pRSPLen = 0;
#ifdef DEBUG_HEX	
	DEBUGPRINTF("\nEzusb_APDU-------------------------------cmd send to card----------------line=%d\n",__LINE__);
	for( j=0; j<APDULen; j++) {
		DEBUGPRINTF("%02x  ",pAPDUData[j]);
	}
	DEBUGPRINTF("\nEzusb_APDU------------------------------------end------------------------line=%d\n",__LINE__);
#endif	

	if(APDULen > CA_TRAM_CMD_MAX)
	{
		printk("22222222222222222222######################APDULen =%d,CA_TRAM_CMD_MAX=%d \n",APDULen,CA_TRAM_CMD_MAX);
		return -1;
	}
	DBG_SC_WR_P("\n-------------------------------Ezusb_APDU---___usb_control_msg__  start-------------line=%d\n",__LINE__);

#ifdef TEST_TIME				
	do_gettimeofday(&(txc.time));
	printk("\n###############UTC time seconds:%d  microseconds :%d\n",txc.time.tv_sec,txc.time.tv_usec);
#endif

	ret=___usb_control_msg__(Dev,usb_sndctrlpipe(Dev, 0),MemoryInterface,0x40,SmartCard,0,pAPDUData,APDULen,0); 
	msleep(1);
#ifdef TEST_TIME		//get end time 
	do_gettimeofday(&(txc.time));
	printk("\n###############UTC time seconds:%d  microseconds :%d\n",txc.time.tv_sec,txc.time.tv_usec);
#endif

	DBG_SC_WR_P("\n-------------------------------Ezusb_APDU---___usb_control_msg__  end-------------line=%d\n",__LINE__);
	if ( ret == APDULen )
	{
		Data[ 0 ] = APDULen & 0xff;
		Data[ 1 ] = ( APDULen >> 8 ) & 0xff;
		DBG_SC_WR_P("\n-------------------------------Ezusb_APDU---___usb_control_msg__  start-------------%d\n",__LINE__);
		ret = ___usb_control_msg__(Dev,usb_sndctrlpipe(Dev, 0),MemoryInterface,0x40,SCsendl,0,Data,2,0); 
		msleep(1);
		if(ret != 2)
		{
			ERROR_OUT("Ezusb_APDU ___usb_control_msg__=%d\n",ret);
		}
		DBG_SC_WR_P("\n-------------------------------Ezusb_APDU---___usb_control_msg__  end-------------line=%d\n",__LINE__);
		Data[ 0 ] = 0x2;
		ret = ___usb_control_msg__(Dev,usb_sndctrlpipe(Dev, 0),MemoryInterface,0x40,SCcon,0,Data,1,0); 
		msleep(1);
		if(ret != 1)
		{
			ERROR_OUT("Ezusb_APDU ___usb_control_msg__=%d,-line=%d\n",ret,__LINE__);
		}
		for ( times = 0;times < ADPU_RETRY;times++ )
		{
			ret = CheckSmartCardStatus( Dev);

			if ( ret==SCDATAOK )
			{
				break;
			}

			if ( (ret == SCTIMEOUT) ||(ret == SCNONE) )
			{
				return -ret;

			}

			msleep( 10 );
		}

		if ( times == ADPU_RETRY )
		{
			return -SCTIMEOUT;
		}
		DBG_SC_WR_P("\n-------------------------------Ezusb_APDU---___usb_control_msg__  start-------------line=%d\n",__LINE__);
		ret=___usb_control_msg__(Dev,usb_rcvctrlpipe(Dev, 0),MemoryInterface,0xc0,SCcon,0,Data,8,0); 
		msleep(1);
		DBG_SC_WR_P("\n-------------------------------Ezusb_APDU---___usb_control_msg__  end-------------line=%d\n",__LINE__);
		if ( ret == 8 )
		{
			if ( !( Data[ 1 ] & 0x80 ) )
			{
				return -SCNONE;
			}
			if ( Data[ 1 ] & 0x20 )
			{
				return -SCTIMEOUT;

			}

			RLen = Data[ 4 ] + ( Data[ 5 ] << 8 );

			//if ( RLen > 270 )
			if ( RLen > (CA_TRAM_CMD_MAX-5) )
			{
				ERROR_OUT("Ezusb_APDU1 retunr RLen error Rlen = %d\n",(int)RLen);
				return -SCNONE;
			}

			DBG_SC_WR_P("\n-------------------------------Ezusb_APDU---___usb_control_msg__  start-------------line=%d\n",__LINE__);
			ret = ___usb_control_msg__(Dev,usb_rcvctrlpipe(Dev, 0),MemoryInterface,0xc0,SmartCard,0,pRSPData,RLen,0); 
			msleep(1);
			if(ret != RLen)
			{
				ERROR_OUT("Ezusb_APDU ___usb_control_msg__=%d,%d\n",ret,(int)RLen);
			}
			DBG_SC_WR_P("\n-------------------------------Ezusb_APDU---___usb_control_msg__  end-------------line=%d,pRSPData[0,1]=(0x%x,0x%x)RLen=%d\n",__LINE__,pRSPData[0],pRSPData[1],RLen);
			while ( ( pRSPData[ i ] == 0x60 ) && RLen > 0 )
			{
				i++;
				RLen--;
			}
			if ( i > 0 )
			{
				Cnt = i;

				for ( i = 0;i < RLen;i++ )
				{
					pRSPData[ i ] = pRSPData[ i + Cnt ];
				}
			}

			*pRSPLen = RLen;

			return SCDATAOK;

		}
		else
		{
			ERROR_OUT("___usb_control_msg__=%d\n",ret);
		}
	}
	else
	{
		ERROR_OUT("_APDU APDULen__usb_control_msg__=%d\n",ret);
	}

	return -SCTIMEOUT;
}

#else
int Ezusb_APDU( USBDEV 	*Dev,PUCHAR pAPDUData, USHORT APDULen, PUCHAR pRSPData, USHORT *pRSPLen )
{
	UCHAR Data[ 8 ]={0};
	int ret;
	ULONG RLen, Cnt;
	ULONG i = 0;
	ULONG times = 0;

	*pRSPLen = 0;
	DBG_SC_PRINT("Enter Card_APDU \n");
	DBG_SC_PRINT("enter Ezusb_APDU\n##################pAPDUData=%x,%x,%x,%x,%x,%x %x,%x,%x,%x, APDULen =%d\n" ,pAPDUData[0],pAPDUData[1],pAPDUData[2],pAPDUData[3],pAPDUData[4],pAPDUData[5],pAPDUData[6],pAPDUData[7],pAPDUData[8],pAPDUData[9],APDULen);
	ret=___usb_control_msg__(Dev,usb_sndctrlpipe(Dev, 0),MemoryInterface,0x40,SmartCard,0,pAPDUData,APDULen,0); 
	DBG_SC_PRINT("EnterEzusb_APDU ___usb_control_msg__ ret =%d \n",ret);

	if ( ret == APDULen )
	{
		Data[ 0 ] = APDULen & 0xff;
		Data[ 1 ] = ( APDULen >> 8 ) & 0xff;
		___usb_control_msg__(Dev,usb_sndctrlpipe(Dev, 0),MemoryInterface,0x40,SCsendl,0,Data,2,0); 
		DBG_SC_PRINT("22EnterEzusb_APDU ___usb_control_msg__ ret =%d \n",ret);
		Data[ 0 ] = 0x2;

		___usb_control_msg__(Dev,usb_sndctrlpipe(Dev, 0),MemoryInterface,0x40,SCcon,0,Data,1,0); 
		DBG_SC_PRINT("33EnterEzusb_APDU ___usb_control_msg__ ret =%d \n",ret);
		for ( times = 0;times < 100;times++ )
		{
			ret = CheckSmartCardStatus(Dev );

			if ( ret==SCDATAOK )
			{
				break;
			}

			if ( ret == SCTIMEOUT )
			{
				return -SCTIMEOUT;

			}

			if ( ret == SCNONE)
			{
				return -SCNONE;

			}


			msleep( 100 );
		}

		if ( times == 100 )
		{
			DBG_SC_PRINT( " ###############APDU time out 1\n" ) ;
			return -SCTIMEOUT;
		}

		ret=___usb_control_msg__(Dev,usb_rcvctrlpipe(Dev, 0),MemoryInterface,0xc0,SCcon,0,Data,8,0); 
		DBG_SC_PRINT("33333333#####EnterEzusb_APDU ___usb_control_msg__ ret =%d \n",ret);
		if ( ret == 8 )
		{
			if ( !( Data[ 1 ] & 0x80 ) )
				return -SCNONE;

			if ( Data[ 1 ] & 0x20 )
			{
				return -SCTIMEOUT;

			}

			RLen = Data[ 4 ] + ( Data[ 5 ] << 8 );

			if ( RLen > 270 )
				return -SCNONE;

			ret = ___usb_control_msg__(Dev,usb_rcvctrlpipe(Dev, 0),MemoryInterface,0xc0,SmartCard,0,pRSPData,RLen,0); 
			DBG_SC_PRINT("55555EnterEzusb_APDU ___usb_control_msg__ ret =%d \n",ret);
			while ( ( pRSPData[ i ] == 0x60 ) && RLen > 0 )
			{
				i++;
				RLen--;
			}

			if ( i > 0 )
			{
				Cnt = i;

				for ( i = 0;i < RLen;i++ )
					pRSPData[ i ] = pRSPData[ i + Cnt ];
			}

			*pRSPLen = RLen;

			return SCDATAOK;

		}
	}

	return -SCTIMEOUT;
}
#endif



int NOVEL_ResetSmartCard( USBDEV *Dev, PUCHAR pBuffer,USHORT *pLength)
{

	UCHAR Data[ 8 ]={0};
	int ret;
	UCHAR buf[ CA_ATR_MAX ] = {0};
	int i;
	ULONG times = 0;
#ifdef TEST_SCRESET_TIME	
	struct timex  txc1;
	struct timex  txc2;
#endif 	
#ifdef TEST_SCRESET_TIME				
	do_gettimeofday(&(txc1.time));
#endif	

	DBG_SC_PRINT("NOVEL_ResetSmartCard pbuffer=%x  %x %x %x %x pLength=%d",pBuffer[0],pBuffer[1],pBuffer[2],pBuffer[3],pBuffer[4],*pLength);
	*pLength = 0;
	DBG_SC_WR_P("\n-------------------------------NOVEL_ResetSmartCard---___usb_control_msg__  start-------------\n");

	ret=___usb_control_msg__(Dev,usb_rcvctrlpipe(Dev, 0),MemoryInterface,0xc0,SCcon,0,Data,8,1000); 
	if(ret != 8)
	{
		ERROR_OUT("NOVEL_ResetSmartCard,___usb_control_msg__ ret = %d\n",ret);
	}
	DBG_SC_WR_P("\n-------------------------------NOVEL_ResetSmartCard---___usb_control_msg__  end-------------\n");
	DBG_SC_PRINT("@@@@@@reset NOVEL_ResetSmartCard =%d %x  %x %x %x %x\n",ret,Data[0],Data[1],Data[2],Data[3],Data[6]);


	if ( ret == 8 )
	{

		if ( !( Data[ 1 ] & 0x80 ) )
		{
			return -SCNONE;
		}
		Data[ 6 ] &= ~0x10;
		//ret = ___usb_control_msg__(Dev,usb_sndctrlpipe(Dev, 0),MemoryInterface,0x40,SCother,0,&( Data[ 6 ] ),8,0);	
		DBG_SC_WR_P("\n-------------------------------NOVEL_ResetSmartCard---___usb_control_msg__  start-------------\n");
		ret = ___usb_control_msg__(Dev,usb_sndctrlpipe(Dev, 0),MemoryInterface,0x40,SCcon,0,Data,8,0); 
		if(ret != 8)
		{
			ERROR_OUT("NOVEL_ResetSmartCard,___usb_control_msg__ ret = %d\n",ret);
		}
		DBG_SC_WR_P("\n-------------------------------NOVEL_ResetSmartCard---___usb_control_msg__  end-------------\n");
		msleep( 50 );
		DBG_SC_PRINT("NOVEL_ResetSmartCard ret =%d\n",ret);
		Data[ 6 ] |= 0x10;
		//ret = ___usb_control_msg__(Dev,usb_sndctrlpipe(Dev, 0),MemoryInterface,0x40,SCother,0,&( Data[ 6 ] ),1,0);
		DBG_SC_WR_P("\n-------------------------------NOVEL_ResetSmartCard---___usb_control_msg__  start-------------\n");
		ret = ___usb_control_msg__(Dev,usb_sndctrlpipe(Dev, 0),MemoryInterface,0x40,SCcon,0,Data,8,0);  
		if(ret != 8)
		{
			ERROR_OUT("NOVEL_ResetSmartCard,___usb_control_msg__ ret = %d\n",ret);
		}
		DBG_SC_WR_P("\n-------------------------------NOVEL_ResetSmartCard---___usb_control_msg__  end-------------\n");
		msleep( 50 );
		Data[ 0 ] = (0x1 | 0x20);//reset
		DBG_SC_WR_P("\n-------------------------------NOVEL_ResetSmartCard---___usb_control_msg__  start-------------\n");
		ret = ___usb_control_msg__(Dev,usb_sndctrlpipe(Dev, 0),MemoryInterface,0x40,SCcon,0,Data,1,0);	
		DBG_SC_WR_P("\n-------------------------------NOVEL_ResetSmartCard---___usb_control_msg__  end-------------\n");
		if(ret != 1)
		{
			ERROR_OUT("NOVEL_ResetSmartCard,___usb_control_msg__ ret = %d\n",ret);
		}
		msleep( 50 );
		for ( times = 0;times < SCRESET_RETRY;times++ )
		{
			ret = CheckSmartCardStatus(Dev);
			if ( ret==SCDATAOK )
			{
				break;
			}

			if ( ret == SCTIMEOUT )
			{

				return -SCTIMEOUT;

			}

			if ( ret == SCNONE )
			{

				return -SCNONE;

			}

			msleep( 50 );
		}

		if ( times == SCRESET_RETRY )
		{

			return -SCTIMEOUT;
		}
		else
		{
			DBG_SC_WR_P("\n-------------------------------NOVEL_ResetSmartCard---___usb_control_msg__  start-------------\n");
			ret=___usb_control_msg__(Dev,usb_rcvctrlpipe(Dev, 0),MemoryInterface,0xc0,SCcon,0,Data,8,1000); 
			DBG_SC_WR_P("\n-------------------------------NOVEL_ResetSmartCard---___usb_control_msg__  end-------------\n");

			if (  ret == 8  )
			{

				if ( Data[ 4 ] > 0x40 )
				{

					return -SCUNRECOGNIZED;
				}
				*pLength = Data[ 4 ];
				memset(buf,0,16);
				DBG_SC_WR_P("\n-------------------------------NOVEL_ResetSmartCard---___usb_control_msg__  start-------------\n");
				ret=___usb_control_msg__(Dev,usb_rcvctrlpipe(Dev, 0),MemoryInterface,0xc0,SmartCard,0,buf,Data[ 4 ],1000); 
				if(ret != Data[ 4 ])
				{
					ERROR_OUT("NOVEL_ResetSmartCard,___usb_control_msg__ ret = %d\n",ret);
				}
				DBG_SC_WR_P("\n-------------------------------NOVEL_ResetSmartCard---___usb_control_msg__  end-------------\n");
				msleep( 50 );
				memcpy(pBuffer,buf,*pLength);
				DBG_SC_PRINT("NOVEL_ResetSmartCard ___usb_control_msg__ ret=%d\n",ret);
#ifdef DEBUG_HEX				
				for(i= 0;i<16;i++)
					DBG_SC_PRINT("*pBuffer = %d\n",pBuffer[i]);
#endif
#ifdef TEST_SCRESET_TIME				
				do_gettimeofday(&(txc2.time));
				printk("\n########################reset cost time######## %ld second,%ld usecond \n",(txc2.time.tv_sec)-(txc1.time.tv_sec),txc2.time.tv_usec-txc1.time.tv_usec);
#endif	

				return SCDATAOK;
			}
		}
	}
	DBG_SC_PRINT( "exit !!!!!!!!!!!!!!!!\n" );

	return -SCTIMEOUT;
}


int NOVEL_GetSmartCartStatus( USBDEV *Dev)
{
	int ret,retval;
	UCHAR data[ 8 ] = {0};
 
	DBG_SC_WR_P("\n-------------------------------NOVEL_GetSmartCartStatus---___usb_control_msg__  start-------------\n");
	ret=___usb_control_msg__(Dev,usb_rcvctrlpipe(Dev, 0),MemoryInterface,0xc0,SCcon,0,data,8,0); 
	DBG_SC_WR_P("\n-------------------------------NOVEL_GetSmartCartStatus---___usb_control_msg__  end-------------\n");


	if ( ret == 8 )
	{

		if ( ( data[1] & 0x80 ) )
		{
			retval= 1;
		}
		else
		{
			retval= -1;
		}
	}
	else
	{
		retval= -1;
	}

	return retval;

}


static ULONG CheckSmartCardStatus(USBDEV *Dev)
{
	UCHAR regval[ 8 ] ={0};
	int ret;

	DBG_SC_WR_P("\n-------------------------------CheckSmartCardStatus---___usb_control_msg__  start-------------\n");
	memset(regval,0,8);
	ret=___usb_control_msg__(Dev,usb_rcvctrlpipe(Dev, 0),MemoryInterface,0xc0,SCcon,0,regval,8,0); 
	DBG_SC_WR_P("\n-------------------------------CheckSmartCardStatus---___usb_control_msg__  end-------------\n");
	msleep(1);

	if ( ret == 8 )
	{
		if(!(regval[1]&0x80))
		{
			return SCNONE;
		}

		if((regval[6]&0x01 )||(regval[1]&0x01 ))
		{
#ifdef ANDRIOD_LINUX		
			DBG_SC_WR_P("\n-------------------------------CheckSmartCardStatus---___usb_control_msg__  start-------------\n");
			ret = ___usb_control_msg__(Dev,usb_sndctrlpipe(Dev, 0),MemoryInterface,0x40,SCcon,0,regval,8,0);
			DBG_SC_WR_P("\n-------------------------------CheckSmartCardStatus---___usb_control_msg__  end-------------\n");
#else
			ret = ___usb_control_msg__(Dev,usb_sndctrlpipe(Dev, 0),MemoryInterface,0x40,SCother,0,&regval[6],1,0);	
#endif
			msleep(1);
			if(ret != 8)
			{
				ERROR_OUT("CheckSmartCardStatus,___usb_control_msg__ ret = %d\n",ret);
			}
			if (regval[1]&0x20)
			{
				return SCTIMEOUT;
			}
			else if (regval[1]&0x40)
			{
				return SCDATAOK;
			}
		}

	}
	return SCWAITING;
}


int NOVEL_SpeedMode(USBDEV *udev)
{
	UCHAR buf;
	int ret,ReturnValue;
	buf = 0;
	ret=___usb_control_msg__(udev,usb_rcvctrlpipe(udev, 0),GeneralInterface,0xc0,USBIRQRegister,0,&buf,1,0); 

	if (ret==1)
	{
		if((buf & 0x20) != 0)
		{
			ReturnValue = 0x20;
		}
		else 
		{
			ReturnValue = 0x11; 
		}

		return ReturnValue;

	}
	else
	{
		return ERROR;

	}

}

int ClearPidData( PUCHAR buf, ULONG size )
{
	ULONG i;
#if 0
	for ( i = 0;i < size*2;i++ )
	{
		*( buf + i ) = 0x00;
	}
#else
	memset(buf,0,size*2);
#endif
	return 0;
}

static void ReparCWData( PUCHAR buf, UCHAR *oodkey, UCHAR *evenkey )
{
	ULONG i = 0;
	UCHAR a = 0;

	for ( i = 0;i < 8;i++ )
	{
		*( buf + i ) = *( evenkey + i );

		if ( i == 3 )
		{
			*( buf + i ) = a;
			continue;
		}
		else
		{
			if ( i == 4 )
			{
				a = 0;
			}
			else
			{
				if ( i == 7 )
				{
					*( buf + i ) = a;
					continue;
				}
			}
		}

		a += *( buf + i );
	}

	a = 0;

	for ( i = 8;i < 16;i++ )
	{
		*( buf + i ) = *( oodkey + ( i - 8 ) );

		if ( i == 11 )
		{
			*( buf + i ) = a;
			continue;
		}
		else
		{
			if ( i == 12 )
			{
				a = 0;
			}
			else
			{
				if ( i == 15 )
				{
					*( buf + i ) = a;
					continue;
				}
			}
		}

		a += *( buf + i );
	}

	return;
}




int NOVEL_SetCW(USBDEV *udev,PNOVEL_USB_CW_SET_CONTROL pCW, UCHAR TunerNum )
{
#if 0	
	ULONG i = 0;
	ULONG number = 0;
	ULONG a = 0;
	USHORT address;
	USHORT addr;
	int ret = 0 ;

	UCHAR SCFUNCTIONbuf[ 512 ] = {0};
	UCHAR buf[ 512 ] = {0};


	if ( pCW->num_ == 0 )
	{
		for ( i = 0;i < 32;i++ )
		{
			gpidSatus[ TunerNum ][ i ] = 0;

		}
		ClearPidData( buf, 32 );
		buf[1]=0x80;
		address = ( USHORT ) ( 0x0000 );

		switch ( TunerNum )
		{

		case 0:
			address = FrontEnd1;
			break;

		case 1:
			address = FrontEnd2;
			break;

		case 2:
			address = FrontEnd3;
			break;

		default:
			address = address;
			DBG_SC_PRINT(  "NOVEL_SetCW Choose tuner number error %d\n", TunerNum  );
		}
		gpidnum[ TunerNum ] = 0;
		ret = ___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),MemoryInterface,0x40,address,0,buf,64,0);
		if(ret != 64)
		{
			ERROR_OUT("CheckSmartCardStatus,___usb_control_msg__ ret = %d\n",ret);
		}
		return 0;
	}

	DBG_SC_PRINT(  " NOVEL_SetCW  Set CW number  = %d \n", (int)pCW->num_ );

	number = pCW->num_;

	for ( a = 0;a < number;a++ )
	{
		DBG_SC_PRINT("!!!!!!!!!!!!!!  a =  0x%u \n", (int)a);

		switch ( TunerNum )
		{

		case 0:
			addr = Descramb1;
			break;

		case 1:
			addr = Descramb2;
			break;

		case 2:
			addr = Descramb3;
			break;

		default:
			addr = Descramb1;
			DBG_SC_PRINT( "NOVEL_SetCW1 Choose tuner number error %d\n", TunerNum );
			break;
		}

		for ( i = 0;i < gpidnum[ TunerNum ];i++ )
		{
			if ( pCW->cw_[ a ].pids_ == gpidSatus[ TunerNum ][ i ] )
			{

				address = addr + ( USHORT ) ( 0x0020 );

				if ( gpidFilter[ TunerNum ] )
				{
					address = addr + ( USHORT ) ( 0x0010 );
				}

				DBG_SC_PRINT( "NOVEL_SetCW PID = %d \n",  (int)pCW->cw_[ a ].pids_ );
				ReparCWData( buf, pCW->cw_[ a ].oddkey_, pCW->cw_[ a ].evenkey_ );
				address = ( USHORT ) ( address + i * 0x10 );

				if ( i == 31 && gpidFilter[ TunerNum ] )
				{
					address = addr;

				}

				if ( i == 30 && !gpidFilter[ TunerNum ] )
				{
					address = addr;
				}

				DBG_SC_PRINT(  " NOVEL_SetCW CW address =  %#x CW= %x %x %x %x %x %x %x %x \\ %x %x %x %x %x %x %x %x \n", address,
					buf[ 0 ], buf[ 1 ], buf[ 2 ], buf[ 3 ], buf[ 4 ], buf[ 5 ], buf[ 6 ], buf[ 7 ],
					buf[ 8 ], buf[ 9 ], buf[ 10 ], buf[ 11 ], buf[ 12 ], buf[ 13 ], buf[ 14 ], buf[ 15 ]);

				buf[0]=0x17;
				buf[1]=TunerNum<<6;
				buf[1]|=i;
				memcpy(&SCFUNCTIONbuf[2],&buf[2],16);
				___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),0xa0,0x40,0x00,0,SCFUNCTIONbuf,CA_DATA_MAX,0);
				___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),0xb1,0x40,address,0,buf,16,0);

				break;
			}
		}



		DBG_SC_PRINT( " NOVEL_SetCW pdx->gpidnum[TunerNum] =  0x%x \n", (int)gpidnum[ TunerNum ]  );

		if ( gpidFilter[ TunerNum ] )
		{
			if ( gpidnum[ TunerNum ] >= 32 )
			{
				continue;
			}
		}
		else
		{
			if (gpidnum[ TunerNum ] >= 31 )
			{
				continue;
			}
		}

		if ( i == gpidnum[ TunerNum ] )
		{
			ReparPidData( buf, pCW->cw_[ a ].pids_, 1, 0 );

			address = ( USHORT ) ( 0x0002 + gpidnum[ TunerNum ] * 0x02 );

			if ( gpidFilter[ TunerNum ] )
			{
				address = ( USHORT ) ( 0x0000 + gpidnum[ TunerNum ] * 0x02 );
			}

			switch ( TunerNum )
			{

			case 0:
				address += FrontEnd1;
				break;

			case 1:
				address += FrontEnd2;
				break;

			case 2:
				address += FrontEnd3;
				break;

			default:
				address += FrontEnd1;
				DBG_SC_PRINT(  " NOVEL_SetCW Choose tuner number error\n"  );
				return 0;
				break;
			}


			___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),MemoryInterface,0x40,address,0,buf,2,0);



			DBG_SC_PRINT(  " NOVEL_SetCW PID = %d  address=0x%x \n ",  (int)pCW->cw_[ a ].pids_,  (int)address ) ;

			ReparCWData( buf, pCW->cw_[ a ].oddkey_, pCW->cw_[ a ].evenkey_ );


			address = ( USHORT ) ( 0x20 + gpidnum[ TunerNum ] * 0x10 );

			if (gpidFilter[ TunerNum ] )
			{
				address = ( USHORT ) ( 0x10 + gpidnum[ TunerNum ] * 0x10 );

			}

			if ( i == 31 && gpidFilter[ TunerNum ] )
			{
				address = ( USHORT ) ( 0x00 );

			}

			if ( i == 30 && !gpidFilter[ TunerNum ] )
			{
				address = ( USHORT ) ( 0x00 );
			}

			switch ( TunerNum )
			{

			case 0:
				address += Descramb1;
				break;

			case 1:
				address += Descramb2;
				break;

			case 2:
				address += Descramb3;
				break;

			default:
				address += Descramb1;
				DBG_SC_PRINT(  " NOVEL_SetCW Choose tuner number error\n"  );

				return -1;
			}

			buf[0]=0x17;
			buf[1]=TunerNum<<6;
			buf[1]|=i;
			memcpy(&SCFUNCTIONbuf[2],&buf[2],16);
			___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),0xa0,0x40,address,0,SCFUNCTIONbuf,CA_DATA_MAX,0);


			gpidSatus[ TunerNum ][ gpidnum[ TunerNum ] ] = pCW->cw_[ a ].pids_;
			gpidnum[ TunerNum ] ++;
			DBG_SC_PRINT(  " NOVEL_SetCW CW address= 0x%x  pidnum=  0x%x \n",  (int)address,  (int)gpidnum[ TunerNum ]  );
		}
	}
#endif
	return 0;
}

int NOVEL_SCFunction(USBDEV *udev,PUCHAR pInChipData, USHORT inBufferLength, PUCHAR pOutBuffer, USHORT *outBufferLength, UCHAR TunerNum)
{
#ifdef TEST_TIME 
	struct timeval t_start,t_end; 	   
	struct timex  txc;
	struct rtc_time tm;
#endif    
	int ret;
	NOVEL_USB_CHIP_DATA InChipData;
	NOVEL_USB_CHIP_DATA *requestControl =&InChipData;
	NOVEL_SECRET_CW *PidCwCw ;
	UCHAR ReturnChipData[ CA_DATA_MAX ] = {0};
	UCHAR TenDataFront[ 10 ] = {0};
	UCHAR TenDataBack[ 10 ] = {0};
	UCHAR TenCWOdd[ 10 ] = {0};
	UCHAR TenCWEven[ 10 ] = {0};
	ULONG i = 0;
	USHORT address;
	USHORT addr;
	UCHAR buf[ CA_DATA_MAX ] = {0};
	USHORT TempPid;

	DBG_SC_PRINT("\n################NOVEL_SCFunction%x,%x,%x,%x,%x\n",pInChipData[0],pInChipData[1],pInChipData[2],pInChipData[3],pInChipData[4]);
	TenDataFront[ 8 ] = 0x00;
	TenDataFront[ 9 ] = 0x02;
	TenDataBack[ 8 ] = 0x00;
	TenDataBack[ 9 ] = 0x06;
	TenCWEven[ 9 ] = 0x12;
	TenCWOdd[ 9 ] = 0x12;
	memcpy(requestControl->ChipData,pInChipData,CA_DATA_MAX);
	if ( inBufferLength!= sizeof( NOVEL_USB_CHIP_DATA ) )
	{
		return -1;
	}

	if ( TunerNum >2 )
	{
		DBG_SC_PRINT("\nTunerNum >2 !\n");
		return -2;
	}

	PidCwCw = ( PNOVEL_SECRET_CW ) & ( requestControl->ChipData[ 1 ] );

	memcpy( TenDataFront, &requestControl->ChipData[ 3 ], 8 );
	memcpy( TenDataBack, &requestControl->ChipData[ 11 ], 8 );
	memcpy( TenCWOdd, PidCwCw->oddkey_, 8 );
	memcpy( TenCWEven, PidCwCw->evenkey_, 8 );


	switch ( requestControl->ChipData[ 0 ] )
	{
	case 0x04:
		DBG_SC_WR_P("\n-------------------------------NOVEL_SCFunction---___usb_control_msg__  start-------------\n");
		ret=___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),ANCHOR_LOAD_INTERNAL,0x40,0,0,requestControl->ChipData,CA_DATA_MAX,1000); 
		if(ret != CA_DATA_MAX)
		{
			ERROR_OUT("NOVEL_SCFunction,___usb_control_msg__ ret = %d\n",ret);
		}
		DBG_SC_WR_P("\n-------------------------------NOVEL_SCFunction---___usb_control_msg__  end-------------\n");
		msleep( 30 );
		DBG_SC_WR_P("\n-------------------------------NOVEL_SCFunction---___usb_control_msg__  start-------------\n");
		ret=___usb_control_msg__(udev,usb_rcvctrlpipe(udev, 0),ANCHOR_LOAD_INTERNAL,0xc0,0,0,ReturnChipData,CA_DATA_MAX,1000);	
		if(ret != CA_DATA_MAX)
		{
			ERROR_OUT("CheckSmartCardStatus,___usb_control_msg__ ret = %d\n",ret);
		}  
		DBG_SC_WR_P("\n-------------------------------NOVEL_SCFunction---___usb_control_msg__  end-------------\n");

		if(ReturnChipData[2]==0x81&&ReturnChipData[3]==0x01)
		{
			ReturnChipData[2]=0x91;
			ReturnChipData[3]=0x02;
		}
		memcpy( pOutBuffer, ReturnChipData, CA_DATA_MAX );
		*outBufferLength=CA_DATA_MAX;

		return 0;


	case 0x17:

		//取20 得到tunerID
		TunerNum = requestControl->ChipData[ 20 ];
		DBG_SC_PRINT("NOVEL_SCFunction #####################TunerNum =%x\n",TunerNum);

		if ( TunerNum == 0 )
			addr = FrontEnd1;	
		else  if ( TunerNum == 1 )
			addr = FrontEnd2;
		else  if ( TunerNum == 2 )
			addr = FrontEnd3;

		TempPid = PidCwCw->pid_;
		DBG_SC_PRINT("NOVEL_SCFunction #####################PidCwCw->pid_ =%x\n",TempPid);
		TempPid |= 0xE000;	

		for ( i = 1;i < 32;i++ )
		{
			if ( PidCwCw->pid_ == ( USHORT ) gpidSatus[ TunerNum ][ i ] )
			{

				address = ( USHORT ) ( addr + i * 0x20 );

				if ( i == 31 )
				{
					address = addr;
				} 							   

				buf[0]=0x17;
				buf[1]=TunerNum<<6;
				if ( i == 31 )
					buf[1]|=0;
				else
					buf[1]|=i+1;

				memcpy(&buf[2],TenCWOdd,8);
				memcpy(&buf[10],TenCWEven,8);
#ifdef TEST_TIME		        
				do_gettimeofday(&(txc.time));
				printk("\n###############UTC time seconds:%d  microseconds :%d\n",txc.time.tv_sec,txc.time.tv_usec);
				rtc_time_to_tm(txc.time.tv_sec,&tm);
				printk("\n##########################UTC time=>current time :%d-%d-%d %d:%d:%d \n",tm.tm_year+1900,tm.tm_mon, tm.tm_mday,tm.tm_hour+8,tm.tm_min,tm.tm_sec);
#endif
				DBG_SC_PRINT("\n ------------ driver------------- set cw!\n");
				//msleep( 200 );
				DBG_SC_WR_P("\n-------------------------------NOVEL_SCFunction---___usb_control_msg__  start-------------\n");
				ret=___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),ANCHOR_LOAD_INTERNAL,0x40,0,0,buf,CA_DATA_MAX,1000); 
				if(ret != CA_DATA_MAX)
				{
					ERROR_OUT("NOVEL_SCFunction,___usb_control_msg__ ret = %d\n",ret);
				}
				DBG_SC_WR_P("\n-------------------------------NOVEL_SCFunction---___usb_control_msg__  end-------------\n");
				msleep( 50 );
				DBG_SC_WR_P("\n-------------------------------NOVEL_SCFunction---___usb_control_msg__  start-------------\n");
				ret=___usb_control_msg__(udev,usb_rcvctrlpipe(udev, 0),ANCHOR_LOAD_INTERNAL,0xc0,0,0,ReturnChipData,CA_DATA_MAX,1000);	
				if(ret != CA_DATA_MAX)
				{
					ERROR_OUT("NOVEL_SCFunction,___usb_control_msg__ ret = %d\n",ret);
				}
				DBG_SC_WR_P("\n-------------------------------NOVEL_SCFunction---___usb_control_msg__  end-------------\n");

				memcpy( pOutBuffer, ReturnChipData, CA_DATA_MAX );

				*outBufferLength=CA_DATA_MAX;
				PRINT_DEG_OUT("\n********setcw***********  NOVEL_SCFunction  = %x,ret=%d\n", pInChipData[0],ret);
				return 0;

			}
		}

		if ( gindex[ TunerNum ] >= 31 )
			gindex[ TunerNum ] = 1;
		else
			gindex[ TunerNum ] ++;

		gpidSatus[ TunerNum ][ gindex[ TunerNum ] ] = PidCwCw->pid_;

		if ( requestControl->ChipData[ 22 ] == 0 )
			gpidnum[ TunerNum ] += requestControl->ChipData[ 21 ];

		if ( gpidnum[ TunerNum ] != 0 )
		{

			//计算过滤pid地址[0x00-0x7F][0x80-0xFF][0x100-0x17F]
			address = TunerNum * 0x0080 + DESBASE + 2 * gindex[ TunerNum ];

			ret=___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),MemoryInterface,0x40,address,0,( UCHAR* ) & TempPid,2,1000);			  
			if(ret != 2)
			{
				ERROR_OUT("NOVEL_SCFunction,___usb_control_msg__ ret = %d\n",ret);
			}
			//计算需要设置地址
			if ( TunerNum == 0 )
				addr = ( USHORT ) ( Descramb1 + gindex[ TunerNum ] * 0x10 );
			else  if ( TunerNum == 1 )
				addr = ( USHORT ) ( Descramb2  + gindex[ TunerNum ] * 0x10 );
			else  if ( TunerNum == 2 )
				addr = ( USHORT ) ( Descramb3  + gindex[ TunerNum ] * 0x10 );


			buf[0]=0x17;
			buf[1]=TunerNum<<6;
			if (gindex[ TunerNum ]>=31)
				buf[1]|=0;
			else 
				buf[1]|=gindex[ TunerNum ]+1;
			memcpy(&buf[2],TenCWOdd,8);
			memcpy(&buf[10],TenCWEven,8);
			//msleep( 80 );
			DBG_SC_WR_P("\n-------------------------------NOVEL_SCFunction---___usb_control_msg__  start-------------\n");
			ret=___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),ANCHOR_LOAD_INTERNAL,0x40,0,0,buf,CA_DATA_MAX,0); 
			if(ret != CA_DATA_MAX)
			{
				ERROR_OUT("NOVEL_SCFunction,___usb_control_msg__ ret = %d\n",ret);
			}
			DBG_SC_WR_P("\n-------------------------------NOVEL_SCFunction---___usb_control_msg__  end-------------\n");
			msleep( 50 );
			DBG_SC_WR_P("\n-------------------------------NOVEL_SCFunction---___usb_control_msg__  start-------------\n");
			ret=___usb_control_msg__(udev,usb_rcvctrlpipe(udev, 0),ANCHOR_LOAD_INTERNAL,0xc0,0,0,ReturnChipData,CA_DATA_MAX,1000);	
			DBG_SC_WR_P("\n-------------------------------NOVEL_SCFunction---___usb_control_msg__  end-------------\n");
			if(ret != CA_DATA_MAX)
			{
				ERROR_OUT("NOVEL_SCFunction,___usb_control_msg__ ret = %d\n",ret);
			}

		}

		memcpy( pOutBuffer, ReturnChipData, CA_DATA_MAX );
		*outBufferLength=CA_DATA_MAX;


		return 0;


	default:
		if(requestControl->ChipData[ 0 ] == 0x02)
			requestControl->ChipData[ 0 ] = 0x03;

		memcpy(buf,requestControl->ChipData,CA_DATA_MAX);
#ifdef DEBUG_HEX		
		DBG_SC_PRINT("\n>>>11111111111111111111111111111111111#NOVEL_SCFunction  start>>>>\n");
		for(i = 0;i < CA_DATA_MAX; i++)
		{
			DBG_SC_PRINT(" %x",buf[i]);
		}
		DBG_SC_PRINT("\n<<<1111111111111111111111111111111111#NOVEL_SCFunction  end<<<<<<\n"); 
#endif		
		//msleep( 80 );
		DBG_SC_WR_P("\n-------------------------------NOVEL_SCFunction---___usb_control_msg__  start-------------\n");
		ret=___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),ANCHOR_LOAD_INTERNAL,0x40,0,0,buf,CA_DATA_MAX,0); 
		if(ret != CA_DATA_MAX)
		{
			ERROR_OUT("NOVEL_SCFunction,___usb_control_msg__ ret = %d\n",ret);
		}
		DBG_SC_WR_P("\n-------------------------------NOVEL_SCFunction---___usb_control_msg__  end-------------\n");
		msleep( 50 );
		DBG_SC_WR_P("\n-------------------------------NOVEL_SCFunction---___usb_control_msg__  start-------------\n");
		ret=___usb_control_msg__(udev,usb_rcvctrlpipe(udev, 0),ANCHOR_LOAD_INTERNAL,0xc0,0,0,ReturnChipData,CA_DATA_MAX,1000);	
		DBG_SC_WR_P("\n-------------------------------NOVEL_SCFunction---___usb_control_msg__  end-------------\n");
		if(ret != CA_DATA_MAX)
		{
			ERROR_OUT("NOVEL_SCFunction,___usb_control_msg__ ret = %d\n",ret);
		}
#ifdef DEBUG_HEX
		DBG_SC_PRINT("\n>>>1111111111111111111111111111111111NOVEL_SCFunction ReturnChipData start>>>>%d\n",ret);
		for(i = 0;i < ret; i++)
		{
			DBG_SC_PRINT(" %x",ReturnChipData[i]);
		}	
		DBG_SC_PRINT("\n<<<111111111111111111111111111111111111NOVEL_SCFunction ReturnChipData end<<<<<<\n"); 
#endif		
		if(ret == CA_DATA_MAX)
		{
			memcpy( pOutBuffer, ReturnChipData, CA_DATA_MAX );
			*outBufferLength =CA_DATA_MAX;

			return 0;
		}
		else
		{

			return -1;
		}


	}


	return 0;

}



int NOVEL_SCTransmit(USBDEV *udev, PUCHAR pInput, USHORT InLen, PUCHAR pOutput, USHORT *outLen, UCHAR TunerNum )
{
	USHORT IOBytes = 0;
	USHORT i;
	UCHAR ret = 0;
	*outLen = 0;

	if(InLen < 5)
	{
		return	-1;
	}
	DBG_SC_PRINT("enter NOVEL_SmartCardCMD ret=%x\n##################pInput=%x,%x,%x,%x,%x,%x %x,%x,%x,%x, InLen =%d\n",ret ,pInput[0],pInput[1],pInput[2],pInput[3],pInput[4],pInput[5],pInput[6],pInput[7],pInput[8],pInput[9],InLen);
	ret = Ezusb_APDU( udev,pInput, 5, pOutput, &IOBytes );
	DBG_SC_PRINT("NOVEL_SmartCardCMD ret=%x\n##################pOutput=%x,%x,%x,%x,%x,%x IOBytes=%d\n",ret ,pOutput[0],pOutput[1],pOutput[2],pOutput[3],pOutput[4],pOutput[5],IOBytes);
	*outLen = IOBytes;
	if ( ret == SCDATAOK)
	{
		*outLen = IOBytes;
		if ( IOBytes > 2 )
		{
			IOBytes--;
			*outLen = IOBytes;
			for ( i = 0;i < IOBytes;i++ )
				pOutput[ i ] = pOutput[ i + 1 ];
		}
		else
		{
			if ( IOBytes == 1 )
			{
				*outLen = IOBytes;
				Ezusb_APDU( udev,pInput + 5, InLen - 5, pOutput, &IOBytes );
				DBG_SC_PRINT("##################pOutput%x %x\n",pOutput[0],pOutput[1]);
			}
		}
	}
	*outLen = IOBytes;
	return IOBytes;
}
int NOVEL_SCardInserted(USBDEV *udev)
{
	return 0;
}



TUNERSTATUS NOVEL_SetPid(USBDEV *udev,PNOVEL_USB_PID_SET_CONTROL pPids,UCHAR TunerNum)
{

	USHORT addr;
	ULONG len,i;
	UCHAR buf[ 64 ]={0};
	int ret = 0;

	if (pPids->num_>32)
	{
		DEBUGPRINTF("\nNOVEL_SetPid PID number Error!\n");
		return INVAILDPARM;
	}

	switch ( TunerNum )
	{

	case 0:
		addr = FrontEnd1;
		break;

	case 1:
		addr = FrontEnd2;
		break;

	case 2:
		addr = FrontEnd3;
		break;

	default:
		addr = FrontEnd1;
		DEBUGPRINTF( "NOVEL_SetPid Choose tuner number error %d\n", (int)TunerNum );
	}

	len=0;

	memset( buf, 0, 64 );
	ret = ___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),MemoryInterface,0x40,addr,0,buf,64,0);	
	if(ret != 64)
	{
		ERROR_OUT("NOVEL_SCFunction,___usb_control_msg__ ret = %d\n",ret);
	}
	if ( pPids->num_ == 0 )
	{
		ReparPidData( buf, 0, 0, 1 );
		len = 2;
		gpidnum[ TunerNum ] = 0;
		gpidFilter[ TunerNum ] = 0;
	}
	else
	{

		for ( i = 0;i < pPids->num_;i++ )
		{
			ReparPidData( ( buf + 2 * i ), pPids->pids_[ i ], 1, 0 );
			gpidSatus[ TunerNum ][ i ] = pPids->pids_[ i ];
		}

		len = 2 * ( pPids->num_ );
		gpidFilter[ TunerNum ] = 1;
		gpidnum[ TunerNum ] = pPids->num_;

	}
	ret = ___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),MemoryInterface,0x40,addr,0,buf,len,0);	
	if(ret != len)
	{
		ERROR_OUT("NOVEL_SCFunction,___usb_control_msg__ ret = %d\n",ret);
	}
#ifndef UBUNTU_OS    
	spin_lock(&SETPID_lock_);
#endif
	gReadPosition[ 0 ] = 0; 
	gWritePosition[0] = 0;
	gbulkflag[ 0 ] = 0;      
#ifndef UBUNTU_OS    	
	spin_unlock(&SETPID_lock_);
#endif
	DEBUGPRINTF( " NOVELUSB_SETPID Filter flag = %d \n", (int)gbulkflag[ 0 ]);


	return NOERROR;

}


TUNERSTATUS ReparPidData( PUCHAR buf, ULONG pid, USHORT decryption, USHORT allpid )
{
	UCHAR low = 0;
	UCHAR high = 0;
	UCHAR decry = 0;
	UCHAR all = 0;
	low |= pid;
	high |= ( pid >> 8 ) ;

	if ( decryption )
	{
		decry = 0x20;
	}
	else
	{
		decry = 0x00;
	}

	high |= decry;

	if ( allpid )
	{
		all = 0x80;
	}
	else
	{
		all = 0xc0;
	}

	high |= all;
	( *buf ) = low;
	buf++;
	( *buf ) = high;
	return NOERROR;
}




TUNERSTATUS NOVEL_GetFreq(USBDEV *udev,PNOVEL_USB_FREQ_SET_CONTROL freq,UCHAR TunerNum)
{

	if (glocked[TunerNum])
	{
		freq->freq_=gcurrentFreq.freq_;
		freq->QAM_=gcurrentFreq.QAM_;
		freq->SymbolRate_=gcurrentFreq.SymbolRate_;
		return TUNERSUCESS;
	}
	else
	{
		return INVAILDFREQ;

	}
}

TUNERSTATUS NOVEL_GetTunerStatus(USBDEV *udev,PUCHAR buf,UCHAR bufferLength,UCHAR TunerNum)
{
	UCHAR	strength[3];			
	UCHAR	quality[3];			
	UCHAR	BER[3];	
	if ( bufferLength < 6 )
	{
		DEBUGPRINTF("\nNOVEL_GetTunerStatus bufferLength Error!\n");
		return TUNERERROR;
	} 
	glocked[ TunerNum] = 0;
	Novel_GetLock(udev,buf,0);
	if ( glocked[ TunerNum ] )
	{
		//quality[ TunerNum ] = NOVEL_GetQualitystatus(udev, buf,1, 0)
		quality[ TunerNum ] = ReadRegValue( 0x17, udev, buf, TunerNum );
		quality[ TunerNum ] = ((quality[ TunerNum ] %70)+128);
		quality[ TunerNum ] = ((quality[ TunerNum ] * 100)/255);
		
		if ( quality[ TunerNum ] > 90 )
		{
			quality[ TunerNum ] = 80;
		}
		else
		{
			if ( quality[ TunerNum ] < 5 )
			{
				quality[ TunerNum ] = 5;
			}
		}

		//strength[ TunerNum ] = NOVEL_GetStrengthstatus(udev, buf,1, 0)
		strength[ TunerNum ] = ReadRegValue( 0x15, udev, buf, TunerNum );
		strength[ TunerNum ] = ((strength[ TunerNum ] >> 1)+128);
		strength[ TunerNum ] = ((strength[ TunerNum ] * 100)/255);
	
		if ( strength[ TunerNum ] > 90 )
		{
			strength[ TunerNum ] = 90;
		}
		else
		{
			if ( strength[ TunerNum ] < 5 )
			{
				strength[ TunerNum ] = 5;
			}
		}
	
		BER[ 0 ] = 0;
		BER[ 1 ] = 0;
		BER[ 2 ] = 0;

	}
	else
	{
		strength[ TunerNum ] = 0;
		quality[ TunerNum ] = 0;
		BER[ 0 ] = 0;
		BER[ 1 ] = 0;
		BER[ 2 ] = 0;

	}

	buf[0]=glocked[ TunerNum ] ;
	buf[1]=strength[ TunerNum ];
	buf[2]=quality[ TunerNum ];
	buf[3]=BER[ 0 ];
	buf[4]=BER[ 1 ];
	buf[5]=BER[ 2 ];
	DEBUGPRINTF("####################locked[ TunerNum ] =%u\n",glocked[ TunerNum ]);
	DEBUGPRINTF("####################quality[ TunerNum ] =%u\n",quality[ TunerNum ]);
	DEBUGPRINTF("####################strength[ TunerNum ] =%u\n",strength[ TunerNum ]);
	PRINT_DEG_OUT("\n********NOVEL_GetTunerStatus***********  = %d,ret=%d\n", glocked[ TunerNum ],TUNERSUCESS);
	return TUNERSUCESS;


}


int NOVEL_GetInfraredData( USBDEV *udev, PUCHAR buf, ULONG Length)
{
	int ret;

	if ( Length < 5 )
	{
		DEBUGPRINTF("\nNOVEL_GetInfraredData length Error!\n");
		return -1;
	} 

	ret=___usb_control_msg__(udev,usb_rcvctrlpipe(udev, 0),InfraredInterface,0xc0,0,0,buf,5,0); 

	DEBUGPRINTF("\nNOVEL_GetInfraredData ret: %d!\n",ret);

	return Length;

}
#ifdef USE_READ_INTERFACE
//每次获取数据最大不允许超过一个bulk的大小
int NOVEL_GetTSData( UCHAR * Addr, ULONG bufferLength, UCHAR TunerNum )
{
	int count = 0;
	UCHAR *p;
	int i=0;
	
	if(bufferLength == 0)
	{
		
		return 0;
	}
	if ((Addr == NULL)||(gpAddr_TS[TunerNum] == NULL))
	{
		PRINT_READ_DATA("\n addr == null \n");
		return -1;
	}
	count = 0;
#ifdef READ_SUCCES	
	while(count < 500)
#endif
	{
		count ++;

		if ( gbulkflag[ TunerNum ] > 0)
		{
			if((readts + bufferLength)>(2*USBBUFFERSIZE))
			{
				ERROR_OUT("\n######## read data  > USBBUFFERSIZE \n");
#ifdef READ_SUCCES					
				msleep(1);
				continue;
#else
				return 0;
#endif				
			}
#if 0			
			else if((readts + bufferLength)==(2*USBBUFFERSIZE))
			{
#ifdef READ_SUCCES					
				msleep(1);
				continue;
#else
				PRINT_READ_DATA("\n######## read data  === USBBUFFERSIZE \n");
				return 0;
#endif
			}
#endif			
			else if((readts + bufferLength)==USBBUFFERSIZE)
			{
				PRINT_READ_DATA("readts + bufferLength ======================= USBBUFFERSIZE\n");
#ifndef UBUNTU_OS						
				spin_lock(&TS_lock[0]);
#endif
				gbulkflag[ TunerNum ] -- ;
				readts += bufferLength;
				readts -= USBBUFFERSIZE;

#ifndef UBUNTU_OS
				spin_unlock(&TS_lock[0]); 
#endif		
				
#if 0				
				if((gReadPosition[ TunerNum ]+bufferLength)== (USBBUFFERSIZE*BLOCKCOUNT))
				{
					copy_to_user( Addr, gpAddr_TS[ TunerNum ] + gReadPosition[ TunerNum ], bufferLength );
					gReadPosition[ TunerNum ] = 0;
					PRINT_READ_DATA("==USBBUFFERSIZE*BLOCKCOUNT gReadPosition[ TunerNum ]=%d,gWritePosition=%d,gbulkflag[ TunerNum ]=%d\n",gReadPosition[ TunerNum ],gWritePosition[0],gbulkflag[ TunerNum ]);
				}
				else
				{
					copy_to_user( Addr, gpAddr_TS[ TunerNum ] + gReadPosition[ TunerNum ], bufferLength );
					gReadPosition[ TunerNum ] += bufferLength;				
				}
				return bufferLength;
#endif				
			}
			else if( (readts + bufferLength) > USBBUFFERSIZE)
			{
				PRINT_READ_DATA("readts + bufferLength >>>>>>>>>>>>>>>>>>>>>>>>>> USBBUFFERSIZE\n");
				PRINT_READ_DATA("before gbulkflag=%d,readts=%d,readts+buflen=%d,gWritePosition[0]=%d,gReadPosition=%d\n",gbulkflag[0],readts,readts + bufferLength,gWritePosition[0],gReadPosition[0]);
				if(gbulkflag[ TunerNum ] > 1)
				{
#ifndef UBUNTU_OS						
					spin_lock(&TS_lock[0]);
#endif
					gbulkflag[ TunerNum ] -- ;
					readts += bufferLength;
					readts -= USBBUFFERSIZE;

#ifndef UBUNTU_OS
					spin_unlock(&TS_lock[0]); 
#endif					
					
					
#if 0					
					if((gReadPosition[ TunerNum ]+bufferLength)< (USBBUFFERSIZE*BLOCKCOUNT))
					{		
						copy_to_user( Addr, gpAddr_TS[ TunerNum ] + gReadPosition[ TunerNum ], bufferLength );
						gReadPosition[ TunerNum ] += bufferLength;
				
						PRINT_READ_DATA("<<USBBUFFERSIZE*BLOCKCOUNT gReadPosition[ TunerNum ]=%d,gWritePosition[0]=%d,gbulkflag[ TunerNum ]=%d\n",gReadPosition[ TunerNum ],gWritePosition[0],gbulkflag[ TunerNum ]);
					}
					else if((gReadPosition[ TunerNum ]+bufferLength)== (USBBUFFERSIZE*BLOCKCOUNT))
					{
						copy_to_user( Addr, gpAddr_TS[ TunerNum ] + gReadPosition[ TunerNum ], bufferLength );
						gReadPosition[ TunerNum ] = 0;
						PRINT_READ_DATA("==USBBUFFERSIZE*BLOCKCOUNT gReadPosition[ TunerNum ]=%d,gWritePosition[0]=%d,gbulkflag[ TunerNum ]=%d\n",gReadPosition[ TunerNum ],gWritePosition[0],gbulkflag[ TunerNum ]);
					}
					else
					{
						copy_to_user( Addr, gpAddr_TS[ TunerNum ] + gReadPosition[ TunerNum ], USBBUFFERSIZE*BLOCKCOUNT-gReadPosition[ TunerNum ]);
						copy_to_user( Addr, gpAddr_TS[ TunerNum ] , bufferLength-(USBBUFFERSIZE*BLOCKCOUNT-gReadPosition[ TunerNum ]));
						gReadPosition[ TunerNum ] =  bufferLength-(USBBUFFERSIZE*BLOCKCOUNT-gReadPosition[ TunerNum ]);
						PRINT_READ_DATA(">> gReadPosition[ TunerNum ]=%d,gWritePosition[0]=%d,gbulkflag[ TunerNum ]=%d\n",gReadPosition[ TunerNum ],gWritePosition[0],gbulkflag[ TunerNum ]);
					}
					
					PRINT_READ_DATA("after gbulkflag=%d,readts=%d,readts+buflen=%d,gWritePosition[0]=%d,gReadPosition=%d\n",gbulkflag[0],readts,readts + bufferLength,gWritePosition[0],gReadPosition[0]);
					return bufferLength;
#endif					
				}
				else
				{
					//printk("data < len,gbulkflag[ TunerNum ]=%d\n",gbulkflag[ TunerNum ]);
#ifdef READ_SUCCES					
					msleep(1);
					continue;
#else
					return 0;
#endif	
				}
				
			}
			else if((readts + bufferLength) < USBBUFFERSIZE)
			{
				readts += bufferLength;
				PRINT_READ_DATA("readts + bufferLength <<<<<<<<<<<<<<< USBBUFFERSIZE\n");
#if 0

				copy_to_user( Addr, gpAddr_TS[ TunerNum ] + gReadPosition[ TunerNum ], bufferLength );
				gReadPosition[ TunerNum ] += bufferLength;
				
				PRINT_READ_DATA("<<USBBUFFERSIZE*BLOCKCOUNT gReadPosition[ TunerNum ]=%d,gWritePosition[0]=%d,gbulkflag[ TunerNum ]=%d\n",gReadPosition[ TunerNum ],gWritePosition[0],gbulkflag[ TunerNum ]);
				return bufferLength;
#endif				
			}
			else
			{
				PRINT_READ_DATA("#################error\n");
				return 0;
			}
#ifndef UBUNTU_OS						
			spin_lock(&TS_lock[0]);
#endif
			

			if((gReadPosition[ TunerNum ]+bufferLength)< (USBBUFFERSIZE*BLOCKCOUNT))
			{		
				copy_to_user( Addr, gpAddr_TS[ TunerNum ] + gReadPosition[ TunerNum ], bufferLength );
				gReadPosition[ TunerNum ] += bufferLength;
				
				PRINT_READ_DATA("<<USBBUFFERSIZE*BLOCKCOUNT gReadPosition[ TunerNum ]=%d,gbulkflag[ TunerNum ]=%d\n",gReadPosition[ TunerNum ],gbulkflag[ TunerNum ]);
			}
			else if((gReadPosition[ TunerNum ]+bufferLength)== (USBBUFFERSIZE*BLOCKCOUNT))
			{
				copy_to_user( Addr, gpAddr_TS[ TunerNum ] + gReadPosition[ TunerNum ], bufferLength );
				gReadPosition[ TunerNum ] = 0;
				PRINT_READ_DATA("==USBBUFFERSIZE*BLOCKCOUNT gReadPosition[ TunerNum ]=%d,gbulkflag[ TunerNum ]=%d\n",gReadPosition[ TunerNum ],gbulkflag[ TunerNum ]);
			}
			else
			{
				if(gbulkflag[0] >= 1)
				{
					copy_to_user( Addr, gpAddr_TS[ TunerNum ] + gReadPosition[ TunerNum ], USBBUFFERSIZE*BLOCKCOUNT-gReadPosition[ TunerNum ]);
					copy_to_user( Addr+USBBUFFERSIZE*BLOCKCOUNT-gReadPosition[ TunerNum ], gpAddr_TS[ TunerNum ] , bufferLength-(USBBUFFERSIZE*BLOCKCOUNT-gReadPosition[ TunerNum ]));
					gReadPosition[ TunerNum ] =  bufferLength-(USBBUFFERSIZE*BLOCKCOUNT-gReadPosition[ TunerNum ]);
					PRINT_READ_DATA(">> gReadPosition[ TunerNum ]=%d,gbulkflag[ TunerNum ]=%d\n",gReadPosition[ TunerNum ],gbulkflag[ TunerNum ]);
				}
				else
				{
					printk("#####################\n");
				}

			}
			return bufferLength;
#ifndef UBUNTU_OS						
			spin_unlock(&TS_lock[0]);
#endif				
	
		}

	}
	//printk("read == 0\n");
	return 0;
}
#endif
#if 0
int NOVEL_GetTSDatatoUser( UCHAR * Addr, ULONG bufferLength, UCHAR TunerNum )
{
	static int count = 0;
	UCHAR *p;
	int i=0;
	static ULONG readts=0;
	if(bufferLength == 0)
	{
		return 0;
	}
	if ((Addr == NULL)||(gpAddr_TS[TunerNum] == NULL))
	{
		DEBUGPRINTF("\n addr == null \n");
		return -1;
	}
	//readts += bufferLength;
	while(1)
	{
		count ++;
		if(count > 5)
	{
		
		break;
	}
	if ( gbulkflag[ TunerNum ] > 0)
	{
		if((readts + bufferLength)>(2*USBBUFFERSIZE))
		{
			DEBUGPRINTF("\n read data  > USBBUFFERSIZE \n");
			//return 0;
			msleep(1);
			continue;
		}
		else if((readts + bufferLength)>=USBBUFFERSIZE)
		{
			if(gbulkflag[ TunerNum ]>=2)
			{
				gbulkflag[ TunerNum ] -- ;
				readts += bufferLength;
				readts -= USBBUFFERSIZE;
			}
			else
			{
				printk("data < len\n");
				//return 0;
				msleep(1);
			continue;
			}
			DEBUGPRINTF("gbulkflag=%d,readts=%d,readts+buflen=%d\n",readts,readts + bufferLength);
			
		}
			
		if((gReadPosition[ TunerNum ]+bufferLength)< (USBBUFFERSIZE*BLOCKCOUNT))
		{		
			copy_to_user( Addr, gpAddr_TS[ TunerNum ] + gReadPosition[ TunerNum ], bufferLength );
			gReadPosition[ TunerNum ] += bufferLength;
			DEBUGPRINTF("gReadPosition[ TunerNum ]=%d\n",gReadPosition[ TunerNum ]);
		}
		else
		{
			copy_to_user( Addr, gpAddr_TS[ TunerNum ] + gReadPosition[ TunerNum ], USBBUFFERSIZE*BLOCKCOUNT-gReadPosition[ TunerNum ]);
			copy_to_user( Addr+USBBUFFERSIZE*BLOCKCOUNT-gReadPosition[ TunerNum ], gpAddr_TS[ TunerNum ] , bufferLength-(USBBUFFERSIZE*BLOCKCOUNT-gReadPosition[ TunerNum ]));
			gReadPosition[ TunerNum ] =  bufferLength-(USBBUFFERSIZE*BLOCKCOUNT-gReadPosition[ TunerNum ]);
			DEBUGPRINTF("gReadPosition[ TunerNum ]=%d\n",gReadPosition[ TunerNum ]);
		}
		return bufferLength;
	}
	

	}

	return 0;
}


#endif

#ifndef USE_READ_INTERFACE

int NOVEL_GetTSData( UCHAR * Addr, ULONG bufferLength, UCHAR TunerNum )
{
	static int count = 0;
	UCHAR *p;
	int i=0;
	if ( bufferLength != USBBUFFERSIZE)
	{
		DEBUGPRINTF("\n bufferlen error\n");
		return -1;
	}


	if ((Addr == NULL)||(gpAddr_TS[TunerNum] == NULL))
	{
		DEBUGPRINTF("\n addr == null \n");
		return -1;

	}


	//if ( gbulkflag[ TunerNum ] > 0 )
	if ( gbulkflag[ TunerNum ] > 1)
	{
		count ++;
		memcpy( Addr, gpAddr_TS[ TunerNum ] + gReadPosition[ TunerNum ], USBBUFFERSIZE );
#ifdef TEST_TS_CC 
		p = Addr;
		for(i = 0;i<(USBBUFFERSIZE/188);i++)
		{
			Transport_packet_continuity_check(0,(p+188*i),gTScomtinuities0);
		}
#endif		
#ifndef UBUNTU_OS
		spin_lock(&TS_lock[TunerNum]);
#endif
		gbulkflag[ TunerNum ]--;
		if(count >= 50)
		{
			count = 0;
			PRINT_DEG_OUT("\n*******************  NOVEL_GetTSData flag  = %d sync = %x,gstartsync=%d\n", (int)gbulkflag[ 0 ],Addr[0],gstartsync);
		}
#ifndef UBUNTU_OS		
		spin_unlock(&TS_lock[TunerNum]);
#endif

		if ( gReadPosition[ TunerNum ] >= ( USBBUFFERSIZE * (BLOCKCOUNT-1) ) )
		{
			gReadPosition[ TunerNum ] = 0;
		}
		else
		{
			gReadPosition[ TunerNum ] += USBBUFFERSIZE;
		}


		return USBBUFFERSIZE;

	}
	else if((gbulkflag[ TunerNum ] > 0) &&(gstartsync == 0))
	{
		count ++;
		memcpy( Addr, gpAddr_TS[ TunerNum ] + gReadPosition[ TunerNum ], USBBUFFERSIZE );
#ifdef TEST_TS_CC 
		p=Addr;
		for(i = 0;i<(USBBUFFERSIZE/188);i++)
		{
			ret = Transport_packet_continuity_check(0,(p+188*i),gTScomtinuities0);
			if(ret == 1)
			{
				PRINT_DEG_OUT("#Transport_packet_continuity_check=###########%d",i);
			}
		}
#endif			
#ifndef UBUNTU_OS
		spin_lock(&TS_lock[TunerNum]);
#endif
		gbulkflag[ TunerNum ]--;
		if(count >= 15)
		{
			count = 0;
			PRINT_DEG_OUT("\n*******************  NOVEL_GetTSData flag  = %d sync = %x\n", (int)gbulkflag[ 0 ],Addr[0]);
		}
#ifndef UBUNTU_OS		
		spin_unlock(&TS_lock[TunerNum]);
#endif

		if ( gReadPosition[ TunerNum ] >= ( USBBUFFERSIZE * (BLOCKCOUNT-1) ) )
		{
			gReadPosition[ TunerNum ] = 0;
		}
		else
		{
			gReadPosition[ TunerNum ] += USBBUFFERSIZE;
		}


		return USBBUFFERSIZE;

	}
	
	else
	{
		//msleep( 50 );
	}
	return 0;
}
#endif

static void bulk_read_callback(URB * urb)
{
	complete((COMPLETION *)urb->context);
	if (urb->actual_length!=USBBUFFERSIZE)
	{
		ERROR_OUT("\n###########bulk_read_callback: .....actual_length = %d\n", urb->actual_length);
	}
	if (urb->status && !(urb->status == -ENOENT || urb->status == -ECONNRESET || urb->status == -ESHUTDOWN)) 
	{	
		ERROR_OUT("%s - nonzero read bulk status received: %d\n",__FUNCTION__, urb->status);  
	}  
	return;
}

static void TSThreadProc(struct work_struct *data)
{
	int count = 0;
	int i,retval[TSURBCOUNT];
	ULONG j = 0,mm=0;
	int buffer_size = USBBUFFERSIZE;
	unsigned int pipe;
	//申请批量管道
	pipe = usb_rcvbulkpipe(gpDev, 0x81);
	usb_pipebulk(pipe);
	//初始化15 chenel,set the usb higt speed chennel
	for (i = 0;i < TSURBCOUNT ;i++)
	{
		//void usb_fill_int_urb(URB *urb, USBDEV *dev, unsigned int pipe,
		//void *transfer_buffer, int buffer_length,
		//usb_complete_t complete, void *context, int interval);
		//transfer_buffer是一个要送/收的数据的缓冲，buffer_length是它的长度，complete是urb完成回调函数的入口，
		//context由用户定义，可能会在回调函数中使用的数据，interval就是urb被调度的间隔
		//memset(gbulk_in_buffer[i],0,buffer_size);
		usb_fill_bulk_urb(gbulk_urb[i], gpDev,pipe,gbulk_in_buffer[i], buffer_size,bulk_read_callback, NULL);
		init_completion(&gdone[i]);	
		gbulk_urb[i]->context = &gdone[i];
		retval[i] = usb_submit_urb(gbulk_urb[i], GFP_KERNEL);
		if(retval[i]!=0)
		{
			ERROR_OUT("usb_submit_urb fail \n");
		}
	}
	while(1)
	{
		count++;
		if(count >= 50)
		{
			count = 0;
			PRINT_DEG_OUT("\n*******************  TSThreadProc is running ...............");
		}
		for (i = 0 ; i < TSURBCOUNT ; i++ )
		{	
			if (gKillThread)
			{
				DEBUGPRINTF("\n Start Stopping TSThreadproc!");
				for (i=0;i<TSURBCOUNT;i++)
				{
					if(gbulk_urb[i] != NULL)
					{
						usb_free_urb(gbulk_urb[i]);
					}
					if(gbulk_in_buffer[i] != NULL)
					{
						kfree(gbulk_in_buffer[i]);	
					}
					gbulk_in_buffer[i] = NULL;
					gbulk_urb[i]=NULL;
				}			
				complete(&gTSThreadDone);			
				DEBUGPRINTF("\nTSThreadproc Stopped!\n");
				ERROR_OUT("#################\nTSThreadproc Stopped!\n");
				return ;
			}
#if 0
			if (gbulk_urb[i] == NULL)
			{	
				printk("\nbulk_urb[i]==NULL)");
				continue ;
			}
#endif			
			//而返回了一个大于0的值则表示接到通知了
			//0表示超时
			if( (retval[i] != 0) || (!wait_for_completion_timeout(&gdone[i],10000*200)))
			{
				usb_fill_bulk_urb(gbulk_urb[i], gpDev,pipe,gbulk_in_buffer[i], buffer_size,bulk_read_callback, NULL);
				init_completion(&gdone[i]);	
				gbulk_urb[i]->context = &gdone[i];
				retval[i] = usb_submit_urb(gbulk_urb[i], GFP_KERNEL);	
				msleep(10);
				ERROR_OUT("##############time out ####################retval[i]=%d\n",retval[i]);
				continue;
			}
			else
			{
				
				PRINT_DEG_OUT("\n##############wait_for_completion_timeout success ####################i=%dgbulk_urb[i]=%x\n", i,gbulk_urb[i]);
			}
			if(firstBulkflag == 1)
			{
				msleep(1);
				firstBulkflag = 0;
				gbulk_urb[i]->actual_length=0;
				init_completion(&gdone[i]);
				retval[i] = usb_submit_urb(gbulk_urb[i], GFP_KERNEL);
				//printk("Tsthreadproc#################################firstBulkflag = %d\n",firstBulkflag);
				continue;
			}
			if (  gbulk_urb[i]->actual_length == buffer_size)
			{         
#ifndef UBUNTU_OS    
				spin_lock(&SETPID_lock_);                      
#endif                  
				//找同步字

				if( gbulkflag[ 0 ] >= BLOCKCOUNT)
				{

					ERROR_OUT("\n############ Lost Data !!!!!!!!");
#ifndef UBUNTU_OS						
					spin_lock(&TS_lock[0]);
#endif
					gbulkflag[ 0 ] = 0; //溢出的情况下，重新从第一个快开始存储
#ifndef UBUNTU_OS
					spin_unlock(&TS_lock[0]); 
#endif
#ifndef UBUNTU_OS

					gWritePosition[0] = 0; 
					readts = 0;
					gReadPosition[0] = 0;
#endif					

				}
#if 1					
				if ( gbulkflag[0] < BLOCKCOUNT )
				{
					//copy 188*496*2 data
					//找同步字
					//从每一个block的第一个字节开始
					for (j=0 ;j <(USBBUFFERSIZE  - 188*2);j++ )
					{
						if ( gbulk_in_buffer[i][ j ] == SYNC47 )
						{
							if ( gbulk_in_buffer[i][ j + 188 ] == SYNC47)
							{
								if ( ( gbulk_in_buffer[i][ j + 188 * 2 ] == SYNC47))
								{
									if ( ((gbulk_in_buffer[i][ j  ]+1) !=gbulk_in_buffer[i][ j	+ 1] )&&((gbulk_in_buffer[i][ j  ]+2) !=gbulk_in_buffer[i][ j+2  ] )&&((gbulk_in_buffer[i][ j  ]+3) !=gbulk_in_buffer[i][ j+3  ] ))
									{
										gstartsync = j;
										break;
									}
								}
							}
						}
					}
			
#ifndef UBUNTU_OS
					spin_lock(&TS_lock[0]);
#endif
					if(gWritePosition[0] == 0)
					{
						memcpy( gpAddr_TS[0] + USBBUFFERSIZE * BLOCKCOUNT-j, gbulk_in_buffer[i], j );
					}
					else if(gWritePosition[0] >=  USBBUFFERSIZE * BLOCKCOUNT - buffer_size)
					{
						memcpy( gpAddr_TS[0] + USBBUFFERSIZE * BLOCKCOUNT - j, gbulk_in_buffer[i], j );
					}
					else
					{
						memcpy( gpAddr_TS[0] + gWritePosition[0] -j, gbulk_in_buffer[i], j );
					}
					memcpy( gpAddr_TS[0] + gWritePosition[0], &gbulk_in_buffer[i][j], buffer_size-j );
					//memset(gbulk_in_buffer[i],0,buffer_size);
					//Transport_packet_continuity_check(0,gpAddr_TS[0] + gWritePosition[0],gTScomtinuities0);
					if ( gWritePosition[0] >=  USBBUFFERSIZE * BLOCKCOUNT - buffer_size )
						gWritePosition[0] = 0;
					else
						gWritePosition[0] += buffer_size;
#ifndef UBUNTU_OS					
					spin_lock(&TS_lock[0]);
#endif
					if ( gbulkflag[0] < BLOCKCOUNT )
					{
#ifndef UBUNTU_OS					
						spin_lock(&TS_lock[0]);
#endif
						gbulkflag[ 0 ]++;
#ifndef UBUNTU_OS
						spin_unlock(&TS_lock[0]);
#endif
					}

				}
				
#endif				
#ifndef UBUNTU_OS			
				spin_unlock(&SETPID_lock_);
#endif
				gbulk_urb[i]->actual_length=0;

				//completion是一种简单的同步机制,对已使用过的completion的重新初始
				init_completion(&gdone[i]);
				
				//一旦urb被USB驱动程序正确地创建和初始化后，就可以递交到USB核心以发送到USB设备
				//在提交urb到USB核心后，直到完成函数被调用之前，不要访问urb中的任何成员
				//urb的控制权被移交给USB核心，该函数返回0；否则，返回错误号
				retval[i] = usb_submit_urb(gbulk_urb[i], GFP_KERNEL);

			}
			else
			{
				ERROR_OUT("####actual_length << blocksize#######################actual_length=%d\n",gbulk_urb[i]->actual_length);
			}
			
		}	
	}
}


static int  StartThread(void)
{
	DEBUGPRINTF( "StartThread\n");

	init_completion(&gTSThreadDone);  

	gpTSqueue = create_workqueue("NovelUSBTS");
	if (!gpTSqueue)
	{
		goto err;
	}

	INIT_WORK(&gTSwork, TSThreadProc);
	queue_work(gpTSqueue, &gTSwork);

	return 0;
err:
	return -1;
}

static void StopThread(void)
{
	int i;
	DEBUGPRINTF( "Starting StopThread!!\n");

	gKillThread=true;

	for (i=0;i<TSURBCOUNT;i++)
	{
		complete(&gdone[i]);
	}

	wait_for_completion(&gTSThreadDone);

	destroy_workqueue(gpTSqueue);

	DEBUGPRINTF( "StopThread!!\n");

}
int InitNovelUSB(USBDEV *udev)
{
	int i;
	UCHAR data;//,ret
	int buffer_size;
	int ret;
	unsigned char buf[256]={0};
	mm_segment_t fs;

	buffer_size = USBBUFFERSIZE;


	gpDev = udev;
#ifdef OUTPRINT_FILE
	out_fp=filp_open("/data/log.txt",O_CREAT|O_RDWR|O_APPEND, 0644);

	fs=get_fs();

	set_fs(KERNEL_DS);

	if(IS_ERR(out_fp))
	{
		printk("#####open file fail\n");
	}
	memcpy(buf,"InitNovelUSB......\n",20);
	out_fp->f_op->write(out_fp, buf, strlen(buf),&out_fp->f_pos);
	set_fs(fs);
#endif
	//Rtusb_Reset(udev,1);
	data = 0x11;
	ret = ___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),0xb4,0x40,0xf1a2,0,&data,1,0); 
	if(ret != 1)
	{
		ERROR_OUT("InitNovelUSB,___usb_control_msg__ ret = %d\n",ret);
	}
	data = 0x71;
	ret = ___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),0xb4,0x40,0xf1a2,0,&data,1,0); 
	if(ret != 1)
	{
		ERROR_OUT("InitNovelUSB,___usb_control_msg__ ret = %d\n",ret);
	}
	gpAddr_TS[0]=NULL;
	gKillThread=false;

	gpAddr_TS[ 0 ] =vmalloc(USBBUFFERSIZE * BLOCKCOUNT );  

	if ( gpAddr_TS[ 0 ] != NULL  )
	{
		DEBUGPRINTF( "\nMemory allocate OK!!!!!");
		memset(gpAddr_TS[0],0,USBBUFFERSIZE * BLOCKCOUNT);
		gWritePosition[ 0 ] = 0;
		gReadPosition[ 0 ] = 0;

	}
	else
	{
		ERROR_OUT( "\ngpAddr_TS Memory allocate ERROR!!!!!");
		return -1;
	}
#if 0	
	gpall_Addr = vmalloc( USBBUFFERSIZE * BLOCKCOUNT );
	if ( gpall_Addr[ 0 ] != NULL  )
	{
		DEBUGPRINTF( "\nMemory allocate OK!!!!!");
		memset(gpall_Addr[0],0,USBBUFFERSIZE * BLOCKCOUNT);

	}
	else
	{
		ERROR_OUT( "\ngpall_Addr Memory allocate ERROR!!!!!");
		return -1;
	}
#endif
	gbulkflag[ 0 ] = 0;
#ifndef UBUNTU_OS	
	TS_lock[0] = SPIN_LOCK_UNLOCKED;
	spin_lock_init(&TS_lock[0]);

	SETPID_lock_ = SPIN_LOCK_UNLOCKED;
	spin_lock_init(&SETPID_lock_);
#endif
#ifdef USE_PROCTECT_INTERFACE	
	PROTECT_REG_WR_lock_ = SPIN_LOCK_UNLOCKED;
	spin_lock_init(&PROTECT_REG_WR_lock_);
#endif


	for (i = 0 ;i < TSURBCOUNT ; i++ )
	{
		gbulk_in_buffer[i] = kmalloc(buffer_size, GFP_KERNEL);
		PRINT_DEG_OUT("#####################gbulk_in_buffer[%d]=0x%x\n",i,gbulk_in_buffer[i]);
		if(gbulk_in_buffer[i] != NULL)
		{
			memset(gbulk_in_buffer[i],0,buffer_size);
			DEBUGPRINTF("\n####################memset#################\n");
		}
		//gbulk_in_buffer[i] = usb_buffer_alloc(udev, count, GFP_KERNEL, &urb->transfer_dma);
		if (gbulk_in_buffer[i] == NULL)
		{
			ERROR_OUT(" gbulk_in_buffer kmalloc error!\n");
			return -1;
		}

		/* 创建 一个urb,并且给它分配一个缓存*/

		gbulk_urb[i]=usb_alloc_urb(0, GFP_KERNEL);
		PRINT_DEG_OUT("#####################gbulk_urb[%d]=0x%x\n",i,gbulk_urb[i]);
		if (!gbulk_urb[i])
		{
			ERROR_OUT(" gbulk_urb kmalloc error!\n");
			return -1;
		}

	}
	msleep(500);
	Rtusb_Reset(udev,1);
	Rtusb_ResetParentPort(udev,1);
	StartThread();
	return 1;

}


int ExitNovelUSB(USBDEV *udev)
{
    int i = 0 ;
	StopThread();

	gKillThread=false;
	if(gpAddr_TS[ 0 ] != NULL)
	{
		vfree(gpAddr_TS[ 0 ] );
	}
	//vfree(gpall_Addr);
	gpAddr_TS[ 0 ]=NULL;
#if 1	
	for (i = 0 ;i < TSURBCOUNT ; i++ )
	{
		if(gbulk_in_buffer[i] != NULL)
		{
			kfree(gbulk_in_buffer[i]);	
		}

		gbulk_in_buffer[i] = NULL;
		/* frew 一个urb,*/
		if(gbulk_urb[i] != NULL)
		{
			usb_free_urb(gbulk_urb[i]);
		}

		gbulk_urb[i] = NULL;
	}
#endif	
	return 1;
}
void reInitl(void)
{
	int i;
	
	//spin_lock(&TS_lock[0]);
	gbulkflag[ 0 ]=0;
	//spin_unlock(&TS_lock[0]);
	
	for ( i = 0;i < 4; i++ )
	{
		gWritePosition[ i ] = 0;
		gReadPosition[ i ] = 0;
	 }
	for ( i = 0;i < TSURBCOUNT; i++ )
	{
		if(gbulk_in_buffer[i]!=NULL)
		{
			memset(gbulk_in_buffer[i],0,USBBUFFERSIZE);
		}
	}
	if(gpAddr_TS[0]!=NULL)
	{
		memset(gpAddr_TS[0],0,USBBUFFERSIZE * BLOCKCOUNT);
	}
}

TUNERSTATUS NOVEL_SetFreq(USBDEV *udev,PNOVEL_USB_FREQ_SET_CONTROL pUsbFreq,UCHAR TunerNum)
{
#ifdef TEST_TIME    
	struct timex  txc,txc1;
	struct rtc_time tm,tm1;
#endif    
	UCHAR buf[ 16 ]={0};
	UCHAR temp[256]={0};
	TUNERSTATUS ret;
	int retval = 0,i;
	ULONG retrytimes = 0;
	NOVEL_USB_PID_SET_CONTROL pidctl;

	DEBUGPRINTF( " NOVELUSB_SETFreq Enter requestControl->freq_= =%lu,QAM_=%lu,SymbolRate_=%lu \n", (int)pUsbFreq->freq_,pUsbFreq->QAM_,pUsbFreq->SymbolRate_);
#ifdef TEST_TIME		        
	do_gettimeofday(&(txc.time));
	printk("\nNOVEL_SetFreq##############UTC time seconds:%d  microseconds :%d\n",txc.time.tv_sec,txc.time.tv_usec);
	rtc_time_to_tm(txc.time.tv_sec,&tm);
	printk("\nNOVEL_SetFreq##########################UTC time=>current time :%d-%d-%d %d:%d:%d \n",tm.tm_year+1900,tm.tm_mon, tm.tm_mday,tm.tm_hour+8,tm.tm_min,tm.tm_sec);
#endif	
	if ( pUsbFreq->freq_ < 1000 || pUsbFreq->freq_ > 966000 || pUsbFreq->QAM_>5)
	{
		DEBUGPRINTF("NOVEL_setfreq exit\n");
		return INVAILDPARM;
	}	
	firstBulkflag = 1;
	gcurrentFreq.freq_=pUsbFreq->freq_;
	gcurrentFreq.QAM_=pUsbFreq->QAM_;
	gcurrentFreq.SymbolRate_=pUsbFreq->SymbolRate_;

	glocked[TunerNum]=0;
	DEBUGPRINTF("pUsbFreq->freq_=%lu\n",pUsbFreq->freq_);
	DEBUGPRINTF("pUsbFreq->QAM_=%lu\n",pUsbFreq->QAM_);
	DEBUGPRINTF("pUsbFreq->SymbolRate_=%lu\n",pUsbFreq->SymbolRate_);
#if 1	
	for(i = 0;i<32;i++)
	{
		pidctl.num_ = 32;
		pidctl.pids_[i] = (USHORT)0xFFFF;
	}
	ret = NOVEL_SetPid(udev,&pidctl,0);
	if(ret < 0)
	{
		ERROR_OUT("clear filter fail\n");
	}
#endif

#ifndef UBUNTU_OS	
	spin_lock(&TS_lock[0]);
#endif
	gbulkflag[ 0 ]=0;
				
				
	for ( i = 0;i < 4; i++ )
	{
		gWritePosition[ i ] = 0;
		gReadPosition[ i ] = 0;
	}
	readts = 0;
#ifndef UBUNTU_OS	
	spin_unlock(&TS_lock[0]);
#endif
	InitQAM( udev, buf, TunerNum );	
	msleep(5);
#if 0	

	DEBUGPRINTF( " NOVELUSB_SETFreq tuner_write_TDAE3\n");
	tuner_write_TDAE3(udev,pUsbFreq->freq_,TunerNum);

	//tuner_write_TDAE3(udev,pUsbFreq->freq_,TunerNum);
	DEBUGPRINTF( " NOVELUSB_SETFreq DemodBM6111_SetParams\n"); 
	NOVEL_SetParams(udev,pUsbFreq->SymbolRate_,pUsbFreq->QAM_+1,TunerNum );
	//Display_DemodBM6111_Regs(udev);
#else
	ret = TunerFreqOffset(udev,buf,pUsbFreq->freq_,TunerNum);
	if(ret != NOERROR)
	{

		return ret;
	}
	//msleep(20);
	buf[ 0 ] = 0x7f;
	buf[ 1 ] = 0x00;//enble 0x02
	retval = ___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),TunerInterface,0x40,0x001c,0,buf,2,0);/* chip stop */
	
	SetQAM( udev, buf, pUsbFreq->QAM_+1, TunerNum );
	msleep(5);

	SetSymbolRate( udev, buf, pUsbFreq->SymbolRate_, TunerNum );
	msleep(5);
	buf[ 0 ] = 0x7f;
	buf[ 1 ] = 0x01;//enble 0x02
	retval = ___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),TunerInterface,0x40,0x001c,0,buf,2,0);/* chip start */
	msleep(5);

	//清空PID
	for ( i = 0;i < 32;i++ )
	{
		gpidSatus[0][ i ] = 0;
	}
	gpidnum[0]=0;
	gindex[0]=0;
	memset(temp,0,64);
	temp[1]=0x80;
	retval = ___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),0xb1,0x40,(TunerNum * 0x0080 + DESBASE),0,temp,64,0);
	if(retval != 64)
	{
		ERROR_OUT("#NOVEL_SetFreq,___usb_control_msg__ ret = %d,addr=0x%x\n",retval,(TunerNum * 0x0080 + DESBASE));
	}

#endif	
	while ( retrytimes < 42 )
	{
		if(Novel_GetLock(udev,buf,0)==TUNERSUCESS)
		{
		
			DEBUGPRINTF("########################LOCK OK\n");
			ret = TUNERSUCESS;
			break;
		}
		else
		{
			DEBUGPRINTF("########################LOCK Fail\n");

			ret = LOCKFAIL;
		}
		msleep(25);
		retrytimes ++;
	}
	PRINT_DEG_OUT("\n*******************  PNOVEL_USB_FREQ_SET_CONTROL  = %lu,%lu,%lu,ret=%d\n", pUsbFreq->freq_,pUsbFreq->QAM_,pUsbFreq->SymbolRate_,ret);

	

#ifdef TEST_TIME				
		do_gettimeofday(&(txc1.time));
		printk("\nendNOVEL_SetFreq end##############UTC time seconds:%d  microseconds :%d\n",txc1.time.tv_sec,txc1.time.tv_usec);
		printk("\nendNOVEL_SetFreq waste time  microseconds :%d\n",txc1.time.tv_usec+1000000-txc.time.tv_usec);
		rtc_time_to_tm(txc.time.tv_sec,&tm);
		printk("\nendNOVEL_SetFreq##########################UTC time=>current time :%d-%d-%d %d:%d:%d \n",tm.tm_year+1900,tm.tm_mon, tm.tm_mday,tm.tm_hour+8,tm.tm_min,tm.tm_sec);
#endif
	

	return ret;
}

void NOVEL_SetParams(USBDEV *udev,ULONG symbolrate, ULONG qammode,UCHAR TunerNum )
{

	UCHAR buf[16];
	USHORT addr = 0x0000;

	DEBUGPRINTF("NEXUS_Platform_DemodBM6111_SetParams symbolrate = %u QAM=%u\n", symbolrate, qammode);
	memset(buf,0,16);

	buf[ 0 ] = 0x7f;
	buf[ 1 ] = 0x00;//enable demo
	switch ( TunerNum )
	{

	case 0:
		addr = 0x0000|DEMOD2_DEV;
		break;

	case 1:
		addr = 0x0100|DEMOD2_DEV;
		break;

	case 2:
		addr = 0x0200|DEMOD2_DEV;
		break;

	default:
		addr = 0x0000|DEMOD2_DEV;


	}
	___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),TunerInterface,0x40,addr,0,buf,2,0);/*chip stop*/


	SetSymbolRate(udev, buf,symbolrate,TunerNum);
	//msleep(30);
	SetQAM( udev, buf, qammode, TunerNum );

	buf[ 0 ] = 0x7f;
	buf[ 1 ] = 0x01;//enble 0x02

	___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),TunerInterface,0x40,addr,0,buf,2,0);/* chip start */
	return;



}


TUNERSTATUS Novel_GetLock( USBDEV *udev, PUCHAR buf, UCHAR TunerNum )
{
	UCHAR carrlock;
	TUNERSTATUS ret;
	carrlock = ReadRegValue( 0x14, udev, buf, TunerNum);
	if(( carrlock & 0x40) == 0x40)
	{
		DEBUGPRINTF("\nDemodBM6111_GetLockstatus locked!!!var:%x\n",carrlock);
		glocked[ TunerNum] = 1; /*locked*/
		ret =  TUNERSUCESS;
	}
	else
	{
		DEBUGPRINTF("\nDemodBM6111_GetLockstatus unlocked!!!var:%x\n",carrlock);
		glocked[ TunerNum] = 0; /*not locked*/
		ret =  LOCKFAIL;
	}
	return ret ; 

}

void InitQAM( USBDEV *udev, PUCHAR buf, UCHAR TunerNum)
{
	int i = 0;
	USHORT addr = 0x0000;
	UCHAR data[ 16 ] ={0};
	int ret = 0 ;
	data[0] = 0x7f;
	data[1] = 0x04;
	

	DEBUGPRINTF("\nenter InitQAM\n");
	switch ( TunerNum )
	{

	case 0:
		addr = (0x0000|DEMOD2_DEV);
		break;

	case 1:
		addr = 0x0100|DEMOD2_DEV;
		break;

	case 2:
		addr = 0x0200|DEMOD2_DEV;
		break;

	default:
		addr = (0x0000|DEMOD2_DEV);

	}
	ret = ___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),TunerInterface,0x40,addr,0,data,2,0); 
	if(ret != 2)
	{
		ERROR_OUT("InitQAM,___usb_control_msg__ ret = %d\n",ret);
	}
	data[0]=(UCHAR)(0x8016>>8);
	data[1]=(UCHAR)0x8016;
	memcpy(&data[2],reg8016,9);
	//msleep(20);
	ret = ___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),TunerInterface,0x40,addr,0,data,11,0);
	if(ret != 11)
	{
		ERROR_OUT("InitQAM,___usb_control_msg__ ret = %d\n",ret);
	}
	msleep(2);
	for (i = 0; i < DEMOD2_REG_NUM; i++)
	{	
		data[0] = 1+i ;
		data[1]=g_NEXUS_Platform_InitValues[i];
		//msleep(20);
		ret = ___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),TunerInterface,0x40,addr,0,data,2,0);
		if(ret != 2)
		{
			ERROR_OUT("InitQAM,___usb_control_msg__ ret = %d\n",ret);
		}
	}
	msleep(5);

	WriteRegValueEx72bit(0x8000,udev,reg8000, TunerNum);
	msleep(2);
	WriteRegValueEx72bit(0x8001,udev,reg8001, TunerNum);
	msleep(2);
	WriteRegValueEx72bit(0x8002,udev,reg8002, TunerNum);
	msleep(2);
	WriteRegValueEx72bit(0x8003,udev,reg8003, TunerNum);
	msleep(2);
	WriteRegValueEx72bit(0x8004,udev,reg8004, TunerNum);
	msleep(2);
	WriteRegValueEx72bit(0x8005,udev,reg8005, TunerNum);
	msleep(2);
	WriteRegValueEx72bit(0x8006,udev,reg8006, TunerNum);
	msleep(2);
	WriteRegValueEx72bit(0x8007,udev,reg8007, TunerNum);
	msleep(2);
	WriteRegValueEx72bit(0x8008,udev,reg8008, TunerNum);
	msleep(2);
	WriteRegValueEx72bit(0x8009,udev,reg8009, TunerNum);
	msleep(2);
	WriteRegValueEx72bit(0x800a,udev,reg800a, TunerNum);
	msleep(2);
	WriteRegValueEx72bit(0x800b,udev,reg800b, TunerNum);
	msleep(2);
	WriteRegValueEx72bit(0x800c,udev,reg800c, TunerNum);
	msleep(2);
	WriteRegValueEx72bit(0x800d,udev,reg800d, TunerNum);
	msleep(2);
	WriteRegValueEx72bit(0x800e,udev,reg800e, TunerNum);
	msleep(2);
	WriteRegValueEx72bit(0x800f,udev,reg800f, TunerNum);
	msleep(2);
	WriteRegValueEx72bit(0x8010,udev,reg8010, TunerNum);
	msleep(2);
	WriteRegValueEx72bit(0x8011,udev,reg8011, TunerNum);
	msleep(2);
	WriteRegValueEx72bit(0x8012,udev,reg8012, TunerNum);
	msleep(2);
	WriteRegValueEx72bit(0x8013,udev,reg8013, TunerNum);
	msleep(2);
	WriteRegValueEx72bit(0x8014,udev,reg8014, TunerNum);
	msleep(2);
	WriteRegValueEx72bit(0x8015,udev,reg8015, TunerNum);
 

	return;
}

int SetQAM(  USBDEV *udev,  PUCHAR buf, ULONG Mode, UCHAR TunerNum)
{
	UCHAR value;
	USHORT addr = 0x0000;
	TUNERSTATUS ret;
	DEBUGPRINTF("\nenter SetQAM = %lu\n",Mode); 
	switch ( TunerNum )
	{

	case 0:
		addr = (0x0000|DEMOD2_DEV);
		break;

	case 1:
		addr = 0x0100|DEMOD2_DEV;
		break;

	case 2:
		addr = 0x0200|DEMOD2_DEV;
		break;

	default:
		addr = (0x0000|DEMOD2_DEV);

	}

	value = ReadRegValue( 0x09, udev, buf, TunerNum);
	//msleep(20);
	value = value & 0x8f;
	value = value | (Mode << 4);
	buf[0] = 0x09;
	buf[1] = value;
	ret = ___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),TunerInterface,0x40,addr,0,buf,2,0);
	msleep(5);
	if(ret != 2)
	{
		ERROR_OUT("SetQAM###usb_control_msg failret =%d\n",ret);
		return SETQAM_ERROR;
	}

	//___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),TunerInterface,0x40,addr,0,buf,2,0); 

	return NOERROR;
}




TUNERSTATUS TunerFreqOffset( USBDEV *udev,  PUCHAR buf, ULONG freq, UCHAR TunerNum )
{
	USHORT addr = 0x0000;
	int ret,i;

	//buf[ 1 ] = ReadRegValue( 0x7f,udev, buf, TunerNum );//读取地址reg------?????????????????????此处register需要修改
	DEBUGPRINTF(" buf:%#2x,%#2x",buf[0],buf[1]);
	EnableICTuner(udev,TunerNum, 1)	;

	CalcFrequency( freq, buf );
	DEBUGPRINTF("end CalcFrequency call\n");

	// write to device 4 byte freq info

	switch ( TunerNum )
	{


	case 0:
		addr = 0x0000|TUNERD2_DEV;
		break;

	case 1:
		addr = 0x0100|TUNERD2_DEV;
		break;

	case 2:
		addr = 0x0200|TUNERD2_DEV;
		break;

	default:
		addr = 0x0000|TUNERD2_DEV;


	}
	DEBUGPRINTF("freq reg send:%#2x,%#2x,%#2x,%#2x,%#2x,%#2x",buf[0],buf[1],buf[2],buf[3],buf[4],buf[5]);

	//频率寄存器五个。将这5个至送入频率寄存器，buf[0][1]为地址
	ret=___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),TunerInterface,0x40,addr,0,buf,5,0);
	msleep( 5 );
	if(ret != 5)
	{
		ERROR_OUT("TunerFreqOffset###usb_control_msg failret=%d\n",ret);
		return USBICWR_ERROR;
	}

	EnableICTuner(udev,TunerNum, 0);
	//msleep( 20 );
	return NOERROR;
}



TUNERSTATUS SetSymbolRate(  USBDEV *udev, PUCHAR buf, ULONG SymbolRate, UCHAR TunerNum)
{
	UCHAR value;
	UCHAR userdata1 = 0;
	UCHAR userdata2 = 0;
	UCHAR userdata3 = 0;
	int m_intsymbolrate = 0;
	int m_decsymbolrate = 0;
	int ret;
	USHORT addr = 0x0000;

	//SymbolRate = SymbolRate/1000;
	DEBUGPRINTF("\nenter SetSymbolRate = %lu\n",SymbolRate);
	m_intsymbolrate = (int) (OSCFREQUENCY / (2 * (SymbolRate + 100)));
	DEBUGPRINTF("\nenter compter SetSymbolRate = %d\n",m_intsymbolrate);
	if(m_intsymbolrate<2)
	{
		m_intsymbolrate=2;
	}
	else if(m_intsymbolrate>16)
	{
		m_intsymbolrate=16;  
	}

	if ( 0 == SymbolRate )
	{
		return 0 ;
	}

	switch ( TunerNum )
	{

	case 0:
		addr = (0x0000|DEMOD2_DEV);
		break;

	case 1:
		addr = 0x0100|DEMOD2_DEV;
		break;

	case 2:
		addr = 0x0200|DEMOD2_DEV;
		break;

	default:
		addr = (0x0000|DEMOD2_DEV);


	}

	value = ReadRegValue( 0x09, udev, buf, TunerNum);
	//msleep(20);
	DEBUGPRINTF("\nSetSymbolRate ReadRegValue 0x09:%x,%x\n",*buf,value);
	userdata1 = value;
	userdata1 = userdata1 & 0xf0;
	userdata1 = userdata1 | ((m_intsymbolrate - 1) & 0x0f);
	DEBUGPRINTF("\nSetSymbolRate to 0x09 userdata1:%x\n",userdata1);
	buf[ 0 ] = 0x09;
	buf[ 1 ] = userdata1;
	ret = ___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),TunerInterface,0x40,addr,0,buf,2,0); 
	if(ret != 2)
	{
		ERROR_OUT("SetSymbolRate###usb_control_msg failret =%d\n",ret);
	}
	value = ReadRegValue( 0x09, udev, buf, TunerNum);
	//msleep(20);
	DEBUGPRINTF("\nwrite == read?SetSymbolRate ReadRegValue 0x09:%x,%x\n",*buf,value);
	m_decsymbolrate = (int) (OSCFREQUENCY* 1024* 32 / 2 / SymbolRate / m_intsymbolrate- 1024 * 32);
	DEBUGPRINTF("\nm_decsymblrate=:0x%x\n",m_decsymbolrate);
	userdata2 = (m_decsymbolrate >> 8) & 0xff;
	userdata3 = (m_decsymbolrate & 0xff);
	buf[ 0 ] = 0x0a;
	buf[ 1 ] = userdata2;
	ret = ___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),TunerInterface,0x40,addr,0,buf,2,0); 
	if(ret != 2)
	{
		ERROR_OUT("SetSymbolRate###usb_control_msg fail=%d\n",ret);
	}
	buf[ 0 ] = 0x0b;
	buf[ 1 ] = userdata3;
	ret = ___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),TunerInterface,0x40,addr,0,buf,2,0); 
	if(ret != 2)
	{
		ERROR_OUT("SetSymbolRate###usb_control_msg failret=%d\n",ret);
	}

	return NOERROR;

}

//读取制定reg的值
//返回reg的值到buf
UCHAR ReadRegValue( UCHAR reg, USBDEV *udev, PUCHAR buf, UCHAR tunernumber )
{
	UCHAR regvalue;
	USHORT addr;
	int ret;

	//0x801F
	*buf = reg;
	//DEBUGPRINTF("\nenter ReadRegValue#######################ReadReg addr 0x%x,0x%x\n",*buf,reg);
	switch ( tunernumber )
	{

	case 0:
		addr = (0x0000|DEMOD2_DEV);
		break;

	case 1:
		addr = 0x0100|DEMOD2_DEV;
		break;

	case 2:
		addr = 0x0200|DEMOD2_DEV;
		break;

	default:
		addr = (0x0000|DEMOD2_DEV);
	}
	//TunerInterface=0xb0=10110000 地址,表示可以是USB标准命令，也可以用户自定义命令，此处为自定义命令
	//0x40表示用户命令，从主机到设备，
	//addr2个字节，高字节是msg的类型（1为输入，2为输出，3为特性）；低字节为msg的ID（预设为0）

	//DBG_PRINT (( "ReadRegValue reg addr= 0x%x %#02x " ,reg, addr )) ;
	ret = ___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),TunerInterface,0x40,addr,0,buf,1,1000); 
	if(ret != 1)
	{
		ERROR_OUT("ReadRegValue###usb_control_msg failret=%d\n",ret);
	}
	//DEBUGPRINTF("ReadRegValue ___usb_control_msg__ ret =%d\n",ret);
	//msleep( 15 );
	//0xC0表示用户命令，从设备到主机，
	//从设备应答
	ret = ___usb_control_msg__(udev,usb_rcvctrlpipe(udev, 0),TunerInterface,0xc0,addr,0,buf,1,1000); 
	if(ret != 1)
	{
		ERROR_OUT("ReadRegValue###usb_control_msg fail=%d\n",ret);
	}
	//DEBUGPRINTF("ReadRegValue ___usb_control_msg__ ret =%d\n",ret);
	//DBG_PRINT ( ( "read value=  %#02x \n" , *buf ) );

	regvalue = *buf;
	//DEBUGPRINTF("#ReadRegValue#####################ReadRegValue[0x%x]= %x\n",reg,*buf);
	return regvalue;	

}
ULONG ReadRegValueEx( ULONG regAddr, USBDEV *udev, PUCHAR buf, UCHAR tunernumber )
{
	ULONG regvalue;
	USHORT addr;
	int ret=0;

	switch ( tunernumber )
	{

	case 0:
		addr = (0x0000|DEMOD2_DEV);
		break;

	case 1:
		addr = 0x0100|DEMOD2_DEV;
		break;

	case 2:
		addr = 0x0200|DEMOD2_DEV;
		break;

	default:
		addr = (0x0000|DEMOD2_DEV);
	}
	//TunerInterface=0xb0=10110000 地址,表示可以是USB标准命令，也可以用户自定义命令，此处为自定义命令
	//0x40表示用户命令，从主机到设备，
	//addr2个字节，高字节是msg的类型（1为输入，2为输出，3为特性）；低字节为msg的ID（预设为0）

	*(buf + 0) = regAddr >> 8;
	*(buf + 1) = (UCHAR)regAddr;
	//DEBUGPRINTF( "ReadRegValueEx regAddr  %#02x,%#02x,%#02x\n" ,regAddr,buf[0],buf[1] ) ;
	ret = ___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),TunerInterface,0x40,addr,0,buf,2,0); 
	if(ret != 2)
	{
		ERROR_OUT("#ReadRegValueEx##usb_control_msg fail=%d\n",ret);
	}
	//DEBUGPRINTF("ReadRegValueEx:___usb_control_msg__ ret = %d\n",ret);
	//msleep( 50 );
	//0xC0表示用户命令，从设备到主机，
	//从设备应答
	ret = ___usb_control_msg__(udev,usb_rcvctrlpipe(udev, 0),TunerInterface,0xc0,addr,0,buf,9,0); 
	if(ret != 9)
	{
		ERROR_OUT("#ReadRegValueEx##usb_control_msg fail=%d\n",ret);
	}
	//DEBUGPRINTF("ReadRegValueEx:___usb_control_msg__ ret = %d\n",ret);

	regvalue = buf[0] << 8;
	regvalue |= buf[1];
	//DEBUGPRINTF("ReadRegValueEx= %x,%x",regvalue);
	DEBUGPRINTF("#read########read buf= %x,%x,%x,%x,%x,%x,%x,%x,%x,%x\n",buf[0],buf[1],buf[2],buf[3],buf[4],buf[5],buf[6],buf[7],buf[8],buf[9]);
	return regvalue;	

}


void CalcFrequency( ULONG Frequency, PUCHAR buf )
{
	ULONG baseif = 36125;
	ULONG DivideValue;
	ULONG TuData;
	int i;


	baseif = 36125;//中频
	DivideValue =(unsigned short ) ((Frequency +36125) *10 / 625);	//62.5kHz
	TuData = ( ULONG ) DivideValue;

	*(buf + 0) = ( UCHAR ) ( ( TuData >> 8 ) & 0xff );

	*( buf + 1 ) = ( UCHAR ) ( TuData  & 0xff );

	*( buf + 2 ) = 0x9b;
	if(Frequency>= 47000 && Frequency< 125000)
	{
		*( buf +3)  = 0xa0;
		*( buf +4)  = 0xc6;
	}
	else if(Frequency>= 125000 && Frequency < 366000)
	{
		*( buf +3) = 0xa2;
		*( buf +4)  = 0xc6;
	}
	else if(Frequency>= 366000 && Frequency < 622000)
	{
		*( buf +3)  = 0x68;
		*( buf +4)  = 0xc6;
	}
	else if(Frequency >= 622000 && Frequency < 726000)
	{
		*( buf +3)  = 0xa8;
		*( buf +4)  = 0xc6;
	}
	else if(Frequency >= 726000 && Frequency < 862000)
	{
		*( buf +3) = 0xe8;
		*( buf +4)  = 0xc6;
	}
	DEBUGPRINTF("\n____________________\n");
	for(i = 0;i<= 5;i++)
	{
		DEBUGPRINTF("0x%x   ",buf[i]);
	}
	DEBUGPRINTF("\n___________________\n");
	return ;
}



int rt10up_download_firmware(USBDEV *udev)
{
	PREQUEST_CONTROL request;
	PREQUEST_CONTROL requestConfirm;
	unsigned short nReturnCode;
	int i,ret;

	UCHAR * ptr;
	int nCount;
	char buf[256]={0};


	UCHAR  B3DATA[10]={0xFF,0xFF,0xFF,0xFF,0x11,0x11,0xF6,0x3B,0x00,0x00};

	DEBUGPRINTF(KERN_ALERT "[RT10UP]rt10up_download_firmware!\n");

	ret=InitFirmware();
	if (ret!=0)
	{	
		ERROR_OUT("###########InitFirmware fail\n");
		return -1;
	}

#ifdef USE_FIRWARE_FILE		
	ptr = grt10up_fw;
#else
	ptr = firmwareRelease;
#endif
	nCount = *ptr;
#ifdef USE_FIRWARE_FILE
	nCount = grt10up_fw[0];
#else
	nCount = firmwareRelease[0];
#endif
	request=(PREQUEST_CONTROL)kmalloc(sizeof(REQUEST_CONTROL),GFP_KERNEL);
	if(request == NULL)
	{
		DEBUGPRINTF("[RT10UP]:Firmware downloading...request malloc fail\n");
	}
	requestConfirm=(PREQUEST_CONTROL)kmalloc(sizeof(REQUEST_CONTROL),GFP_KERNEL);		
	if(requestConfirm == NULL)
	{
		DEBUGPRINTF("[RT10UP]:Firmware downloading...requestConfirm malloc fail\n");
	}
	for(i = 0; i < nCount; i++)
	{
#ifdef USE_FIRWARE_FILE
		ptr = &grt10up_fw [ i * SCFUNCLENGTH + 2 ];
#else
		ptr = &firmwareRelease [ i * SCFUNCLENGTH + 2 ];
#endif		

		memset(request,0,sizeof(REQUEST_CONTROL));
		request->request_code = ANCHOR_LOAD_INTERNAL;
		request->address = 0;//0xe000;
		request->direction = 0;
		request->length = SCFUNCLENGTH - 1;//256
		memcpy(request->buffer, ptr , request->length);

		ret=___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),request->request_code,0x40,request->address,0,request->buffer,request->length,0); 	
		msleep(20);
		if(ret != request->length)
		{
			ERROR_OUT("###########rt10up_download_firmware ___usb_control_msg__ fail request(i=%d) ret =%d,request->length=%d\n",i,ret,request->length);
		}
		DEBUGPRINTF("[RT10UP]:Firmware downloading...Line<%d>, Bytes<%d>\n", i, request->length);

		if(i == nCount - 2)
		{
			udelay(1000); 
		}
		else
		{
			udelay(400);
		}


		requestConfirm->request_code = ANCHOR_LOAD_INTERNAL;
		requestConfirm->address = 0;//0xe000;
		requestConfirm->direction = 1;
		requestConfirm->length = sizeof(short);
		ret=___usb_control_msg__(udev,usb_rcvctrlpipe(udev, 0),requestConfirm->request_code,0xc0,requestConfirm->address,0,requestConfirm->buffer,requestConfirm->length,0); 	
		if(ret != requestConfirm->length)
		{
			ERROR_OUT("###########rt10up_download_firmware ___usb_control_msg__ fail request requestConfirm->length=%d ret =%d\n",requestConfirm->length,ret);
		}
		msleep(20);
		nReturnCode = (unsigned int)(*(requestConfirm->buffer));
		DEBUGPRINTF("[RT10UP]:Firmware downloading...Return<%d>\n", nReturnCode);
		if(nReturnCode != 0)
		{
			break;
		}	

	}

	//Turn on the device led
	msleep(100);

	ret=___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),GPIOInterface,0x40,0,0,B3DATA,10,0); 	
	if(ret != 10)
	{
		ERROR_OUT("###########rt10up_download_firmware ___usb_control_msg__ fail  led ret =%d\n",ret);
	}
	DEBUGPRINTF("[RT10UP]:Turn on the LED. \n");

	kfree(request);
	kfree(requestConfirm);
#ifdef USE_FIRWARE_FILE		
	if (grt10up_fw)
	{
		vfree(grt10up_fw);
	}
#endif	

	buf[ 0 ] = 0x7f;
	buf[ 1 ] = 0x00;//enble 0x02
	ret = ___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),TunerInterface,0x40,0x001c,0,buf,2,0);/* chip start */
	if(ret != 2)
	{
		ERROR_OUT("###########rt10up_download_firmware ___usb_control_msg__ fail  chip stop ret =%d\n",ret);
	}
	Rtusb_ResetParentPort(udev,1);
	return 0;
}

int InitFirmware()
{

	struct file *filp = NULL;
	mm_segment_t old_fs;
	int FileLength;
	//UCHAR * pTemp;
	UCHAR pTemp[257];
#ifdef TEST_INITFIRMWARE_TIME	
	struct timex  txc;
	struct timex  txc2;
	struct rtc_time tm; 
	struct rtc_time tm1; 
#endif 

	ssize_t ret;
#ifdef TEST_INITFIRMWARE_TIME				
	do_gettimeofday(&(txc.time));
	printk("\n###############UTC time seconds:%d  microseconds :%d\n",txc.time.tv_sec,txc.time.tv_usec);
	rtc_time_to_tm(txc.time.tv_sec,&tm);
	printk("\n##########################UTC time=>current time :%d-%d-%d %d:%d:%d \n",tm.tm_year+1900,tm.tm_mon, tm.tm_mday,tm.tm_hour+8,tm.tm_min,tm.tm_sec);
#endif
	DEBUGPRINTF("[RT10UP]:Init Firmware...... \n");
	DEBUGPRINTF("<0>""##################################[RT10UP]:Init Firmware...... \n");
#ifdef USE_FIRWARE_FILE	
	//pTemp=kmalloc(257,GFP_KERNEL);
	//if(pTemp == NULL)
	{
		//ERROR_OUT("[RT10UP]:pTemp kmalloc error\n");
	}
	filp = filp_open(FILE_DIR, O_RDONLY , 0);

	if( IS_ERR( filp ) )
	{

		ERROR_OUT("[RT10UP]:Open file Error...... \n");
		return -1;

	}
	DEBUGPRINTF("[RT10UP]:Open file sucess...... \n");

	old_fs = get_fs();
	set_fs(get_ds());



	filp->f_op->llseek(filp,0,0);

	FileLength=0;

	while(1)
	{
		ret = filp->f_op->read(filp, pTemp, 257, &filp->f_pos);
		DEBUGPRINTF("[RT10UP]:read length %d\n",ret);

		if( ret == 0 )
		{
			break;
		}

		else if(ret==257)
		{
			FileLength+=257;
		}

		else
		{
			//kfree(pTemp);
			ERROR_OUT("[RT10UP]:Firmware length error\n");
			return -1;	
		}
	}


	DEBUGPRINTF("[RT10UP]:Firmware length: %d \n", FileLength);

	grt10up_fw=vmalloc(FileLength+1);
	if(grt10up_fw == NULL)
	{
		ERROR_OUT("###############grt10up_fw malloc fail\n");
	}
	grt10up_fw[0] = (FileLength/257);

	filp->f_op->llseek(filp,0,0);
	ret = filp->f_op->read(filp, &(grt10up_fw[1]), FileLength, &filp->f_pos);

	if (ret!=FileLength)
	{
		//kfree(pTemp);
		ERROR_OUT("[RT10UP]:Firmware length error\n");
		return -1;	
	}

	set_fs(old_fs);

	if(filp)
	{
		filp_close(filp,NULL);
	}
#endif	
	//kfree(pTemp);
#ifdef TEST_INITFIRMWARE_TIME		//get end time 
	do_gettimeofday(&(txc2.time));
	
	printk("\n###############UTC time seconds:%d  microseconds :%d\n",txc2.time.tv_sec,txc2.time.tv_usec);
	rtc_time_to_tm(txc2.time.tv_sec,&tm1);
	printk("\n##########################UTC time=>current time :%d-%d-%d %d:%d:%d \n",tm1.tm_year+1900,tm1.tm_mon, tm1.tm_mday,tm1.tm_hour+8,tm1.tm_min,tm1.tm_sec);
#endif
	return 0;
}

void  WriteRegValueEx72bit( ULONG reg, USBDEV *udev, PUCHAR buf, UCHAR tunernumber )
{
	int ret = 0; 
	char data[16]={0};
	USHORT addr;
	data[0]=(UCHAR)(reg>>8);
	data[1]=(UCHAR)reg;
	memcpy(&data[2],buf,9);


	switch ( tunernumber )
	{

	case 0:
		addr = (0x0000|DEMOD2_DEV);
		break;

	case 1:
		addr = 0x0100|DEMOD2_DEV;
		break;

	case 2:
		addr = 0x0200|DEMOD2_DEV;
		break;

	default:
		addr = (0x0000|DEMOD2_DEV);
	}

	//TunerInterface=0xb0=10110000 地址,表示可以是USB标准命令，也可以用户自定义命令，此处为自定义命令
	//0x40表示用户命令，从主机到设备，
	//addr2个字节，高字节是msg的类型（1为输入，2为输出，3为特性）；低字节为msg的ID（预设为0）

	//DEBUGPRINTF  ( "WriteRegValueEx72bit reg=  %#02x " , addr ) ;
	ret = ___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),TunerInterface,0x40,addr,0,data,11,1000); 
	if(ret != 11)
	{
		ERROR_OUT("#####WriteRegValueEx72bit ___usb_control_msg__ fail=%d\n",ret);
	}
	return ;	

}
ULONG  ReadRegValueEx72bit( ULONG reg, USBDEV *udev, PUCHAR buf, UCHAR tunernumber )
{
	UCHAR ret;
	USHORT addr;
	buf[0]=(UCHAR)(reg>>8);
	buf[1]=(UCHAR)reg;
	//memcpy(&data[2],buf,9);


	switch ( tunernumber )
	{

	case 0:
		addr = (0x0000|DEMOD2_DEV);
		break;

	case 1:
		addr = 0x0100|DEMOD2_DEV;
		break;

	case 2:
		addr = 0x0200|DEMOD2_DEV;
		break;

	default:
		addr = (0x0000|DEMOD2_DEV);
	}
	//TunerInterface=0xb0=10110000 地址,表示可以是USB标准命令，也可以用户自定义命令，此处为自定义命令
	//0x40表示用户命令，从主机到设备，
	//addr2个字节，高字节是msg的类型（1为输入，2为输出，3为特性）；低字节为msg的ID（预设为0）

	//DEBUGPRINTF  ( "ReadRegValueEx72bit reg=	%#02x " , addr ) ;
	ret = ___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),TunerInterface,0x40,addr,0,buf,2,0); 
	//msleep(50);
	ret = ___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),TunerInterface,0xC0,addr,0,buf,9,0); 
	return 0;	

}
int readVersion(  USBDEV *udev,	UCHAR TunerNum)
{
	ULONG value;
	UCHAR data[16];
	memset(data,0,16);

	ReadRegValueEx(0x801F,udev,data,TunerNum);
	msleep(20);
	DEBUGPRINTF("\nreadVersion########DEMOTYPE:(%c%c%c%c%x%x%c)\n",data[1],data[2],data[3],data[4],data[5],data[6],data[7]);
	value = data[5];
	value = (value<<8)|data[6];

	return value;
}

int NOVEL_GetQualitystatus(USBDEV *udev,PUCHAR buf,UCHAR bufferLength,ULONG TunerNum)
{


	UCHAR  regValue = 0;

	regValue = ReadRegValue( 0x15, udev, buf, TunerNum );

	return regValue; 

}
int NOVEL_GetStrengthstatus(USBDEV *udev,PUCHAR buf,UCHAR bufferLength,ULONG TunerNum)
{


	UCHAR regValue = 0;

	regValue = ReadRegValue( 0x17, udev, buf, TunerNum );


	return regValue; 


}

static void  EnableICTuner(USBDEV *udev,UCHAR TunerNum, int On0ff) 
{
	UCHAR buf[16];
	USHORT addr = 0x0000;
	int ret ;
	memset(buf,0,16);
	buf[ 1 ] = ReadRegValue( 0x7f,udev, buf, TunerNum );
	msleep(5);
	switch ( TunerNum )
	{

	case 0:
		addr = 0x0000|DEMOD2_DEV;
		break;

	case 1:
		addr = 0x0100|DEMOD2_DEV;
		break;

	case 2:
		addr = 0x0200|DEMOD2_DEV;
		break;

	default:
		addr = 0x0000|DEMOD2_DEV;
	}
	buf[ 0 ] = 0x7f;
	if(On0ff == 1)
	{
		buf[ 1 ] =0x02;//enable tuner
	}
	else
	{
		buf[ 1 ] =0x00;//disable tuner
	}
	DEBUGPRINTF("EnableICTuner = %x\n",buf[1]);
	ret = ___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),TunerInterface,0x40,addr,0,buf,2,0); 
	if(ret != 2)
	{
		ERROR_OUT("EnableICTuner###usb_control_msg fail=%d\n",ret);
	}

}
int tuner_write_TDAE3(USBDEV *udev,ULONG freq, UCHAR TunerNum )
{
	UCHAR LSBaddr = 0xc2;
	UCHAR data[16],*buf = data;
	USHORT addr = 0x0000;
	UCHAR rettuner;
	//PUCHAR pbuf = buf;
	int ret;
	unsigned short devider = 0;
	ULONG bm6111_current_frequency=freq;

	memset(data,0,16);
	devider =(unsigned short ) ((bm6111_current_frequency +36125) *10 / 625);	//62.5kHz

	buf[0] = (UCHAR)(devider >> 8);
	buf[1] = (UCHAR)(devider);

	buf[2] = 0x9b;
	if(bm6111_current_frequency>= 47000 && bm6111_current_frequency< 125000)
	{
		buf[3] = 0xa0;
		buf[4] = 0xc6;
	}
	else if(bm6111_current_frequency>= 125000 && bm6111_current_frequency < 366000)
	{
		buf[3] = 0xa2;
		buf[4] = 0xc6;
	}
	else if(bm6111_current_frequency>= 366000 && bm6111_current_frequency < 622000)
	{
		buf[3] = 0x68;
		buf[4] = 0xc6;
	}
	else if(bm6111_current_frequency >= 622000 && bm6111_current_frequency < 726000)
	{
		buf[3] = 0xa8;
		buf[4] = 0xc6;
	}
	else if(bm6111_current_frequency >= 726000 && bm6111_current_frequency < 862000)
	{
		buf[3] = 0xe8;
		buf[4] = 0xc6;
	}
	else
	{
		return -1;
	}

	LSBaddr = (0xc2>>1);
	switch ( TunerNum )
	{


	case 0:
		addr = 0x0000|TUNERD2_DEV;
		break;

	case 1:
		addr = 0x0100|TUNERD2_DEV;
		break;

	case 2:
		addr = 0x0200|TUNERD2_DEV;
		break;

	default:
		addr = 0x0000|TUNERD2_DEV;

	}	
	DEBUGPRINTF(KERN_ALERT "addr = %x!\n", addr);
	DEBUGPRINTF(KERN_ALERT "i2c_buffer = %x!\n", buf[0]);
	DEBUGPRINTF(KERN_ALERT "i2c_buffer = %x!\n", buf[1]);
	DEBUGPRINTF(KERN_ALERT "i2c_buffer = %x!\n", buf[2]);
	DEBUGPRINTF(KERN_ALERT "i2c_buffer = %x!\n", buf[3]);
	DEBUGPRINTF(KERN_ALERT "i2c_buffer = %x!\n", buf[4]);
	EnableICTuner(udev,0,1);	
	//msleep( 90 );
	DEBUGPRINTF("freq reg send:%#2x,%#2x,%#2x,%#2x,%#2x,LSBaddr=0x%x,addr=0x%x\n",buf[0],buf[1],buf[2],buf[3],buf[4],LSBaddr,addr);
	//频率寄存器五个。将这5个至送入频率寄存器，buf[0][1]为地址
	ret=___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),TunerInterface,0x40,addr,0,buf,5,0); 
	if(ret != 5)
	{
		ERROR_OUT("###usb_control_msg fail=%d\n",ret);
	}
	addr = (0xC2>>1);
	ret=___usb_control_msg__(udev,usb_rcvctrlpipe(udev, 0),TunerInterface,0xC0,addr,0,&rettuner,1,0); 
	if(ret != 1)
	{
		ERROR_OUT("###usb_control_msg fail=%d\n",ret);
	}
	DEBUGPRINTF("@@@@@@@@@@@@@tuner staus:%#2x,ret=%d\n",rettuner,ret);

	EnableICTuner(udev,0,0);
	return 0;
}
void Display_DemodBM6111_Regs(USBDEV *udev)
{
	UCHAR regs[9];
	int	addr;
	int i;
	DEBUGPRINTF("\n\n===================display All Regs============================\n\n");

	for(addr=0x8000;addr<0x8017;addr++)
	{
		memset(regs,0,9);
		ReadRegValueEx(addr,udev,regs,0);
		DEBUGPRINTF("\n=======REGS[%x]======",addr);
		for(i=0;i<9;i++)
			DEBUGPRINTF("%x,",regs[i]);
		DEBUGPRINTF("\n");
	}

	DEBUGPRINTF("\n\n===================display All Regs Over============================\n\n");



}

#ifdef TEST_TS_CC 
#define EBIT2(x1,x2) x2; x1;
#define EBIT3(x1,x2,x3) x3; x2; x1;
#define EBIT4(x1,x2,x3,x4) x4; x3; x2; x1;
#define EBIT5(x1,x2,x3,x4,x5) x5; x4; x3; x2; x1;
#define EBIT6(x1,x2,x3,x4,x5,x6) x6; x5; x4; x3; x2; x1;
typedef struct _transport_packet {
	UCHAR sync_byte;
	EBIT4(UCHAR transport_error_indicator	: 1,
		UCHAR payload_unit_start_indicator	: 1,
		UCHAR transport_priority		: 1,
		UCHAR pid_hi				: 5)
		UCHAR pid_lo;
	EBIT3(UCHAR transport_scrambling_control	: 2,
		UCHAR adaptation_field_control	: 2,
		UCHAR continuity_counter		: 4)
} transport_packet,*ptransport_packet;
int Transport_packet_continuity_check(ULONG tunernumber,UCHAR* tspacket,ULONG* TScomtinuities)
{
	ULONG precount;
	ULONG nowcount;
	ULONG Adaptation;
	ULONG PID;
	ULONG ContinuityCounter;
	ptransport_packet packet;
	static int count=0;
	packet=(transport_packet*)tspacket;

	PID=packet->pid_lo+(((ULONG)packet->pid_hi)<<8);

	//if (PID>=0x1FFF)
		//return 0;


	precount=TScomtinuities[PID];
	nowcount=packet->continuity_counter;

	//if (packet->adaptation_field_control==0||packet->adaptation_field_control==2)
		//return 0;

	TScomtinuities[PID]=nowcount;


	if (nowcount==precount)
		return 0;
	else if (nowcount==(precount+1))
		return 0;
	else if (nowcount==0x0&&precount==0xF)
		return 0;
	else
	{
		ERROR_OUT("##################TS data No. %lu  mm LCC PID= %lu(%x)  error %d,count =%d\n",tunernumber,PID,PID,nowcount-precount,count);
		return	1;
	}
}
int check_packet_continuity(UCHAR* count,ULONG* comtinuities)
{
	ULONG TEMP=0;
	ULONG precount;
	ULONG nowcount;

	TEMP+=((ULONG)(*(count+2)))<<16;
	TEMP+=((ULONG)(*(count+1)))<<8;
	TEMP+=*count;

	nowcount=TEMP;
	precount=*comtinuities;

	if (nowcount==1)
		TEMP=0;
	else 
	{
		if((nowcount>=precount)&&((nowcount-precount)==1))
			TEMP=0;
		else if((nowcount>=precount)&&((nowcount-precount)!=1))
		{
			DEBUGPRINTF( ( "check data No. M++ error %d\n",nowcount-precount) );
		}
		else 
		{
			DEBUGPRINTF( ( "check data No. M-- error %d\n",precount-nowcount ) );
		};

	}
	*comtinuities=nowcount;

	return TEMP;

}
#endif


int Rtusb_ResetPipe(ULONG PipeNum)
{
#if 1
	int retval[TSURBCOUNT];
	usb_fill_bulk_urb(gbulk_urb[PipeNum], gpDev,gpipe,gbulk_in_buffer[PipeNum], USBBUFFERSIZE,bulk_read_callback, NULL);
	init_completion(&gdone[PipeNum]);	
	gbulk_urb[PipeNum]->context = &gdone[PipeNum];
	retval[PipeNum] = usb_submit_urb(gbulk_urb[PipeNum], GFP_KERNEL);
	return 0;
#endif
}
int Rtusb_ResetAllPipe(void)
{
	int i,retval[TSURBCOUNT];
	int buffer_size = USBBUFFERSIZE;
	unsigned int pipe;
	//申请批量管道
	pipe = usb_rcvbulkpipe(gpDev, 0x81);
	gpipe = pipe;
	usb_pipebulk(pipe);
	
	//初始化15 chenel,set the usb higt speed chennel
	for (i = 0;i < TSURBCOUNT ;i++)
	{
		//void usb_fill_int_urb(URB *urb, USBDEV *dev, unsigned int pipe,
		//void *transfer_buffer, int buffer_length,
		//usb_complete_t complete, void *context, int interval);
		//transfer_buffer是一个要送/收的数据的缓冲，buffer_length是它的长度，complete是urb完成回调函数的入口，
		//context由用户定义，可能会在回调函数中使用的数据，interval就是urb被调度的间隔
		//memset(gbulk_in_buffer[i],0,buffer_size);
		usb_fill_bulk_urb(gbulk_urb[i], gpDev,pipe,gbulk_in_buffer[i], buffer_size,bulk_read_callback, NULL);
		init_completion(&gdone[i]);	
		gbulk_urb[i]->context = &gdone[i];
		retval[i] = usb_submit_urb(gbulk_urb[i], GFP_KERNEL);
	}
	return 0;
}


int Rtusb_ResetParentPort(USBDEV *udev,UCHAR resetBit )
{
	int ret = 0;
	UCHAR buf[256]={0};
	UCHAR data = 0;
	buf[0] = resetBit;// 1:reset,0: no reset
	////reset ep1 fifo
	buf[0]=0x11;
	DEBUGPRINTF("#######################Rtusb_ResetParentPort################################\n");
	

	ret=___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),0xb4,0x40,0xf1a2,0,buf,1,0);		
	if(ret != 1)
	{
		ERROR_OUT("0xb4###Rtusb_ResetParentPort usb_control_msg  fail=%d\n",ret);
	}
	buf[0]=0x71;
	ret=___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),0xb4,0x40,0xf1a2,0,buf,1,0);		
	if(ret != 1)
	{
		ERROR_OUT("0xb4###Rtusb_ResetParentPort usb_control_msg fail=%d\n",ret);
	}
#if 0

	ret=___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),0xb1,0x40,0xf1a2,0,&buf,1,0);		
	if(ret != 1)
	{
		ERROR_OUT("0xb1 ###usb_control_msg fail\n");
	}
	buf[0]=0x71;
	ret=___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),0xb1,0x40,0xf1a2,0,&buf,1,0);		
	if(ret != 1)
	{
		ERROR_OUT("0xb1 ###usb_control_msg fail\n");
	}
#endif	
#if 0	
	ret=___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),ANCHOR_LOAD_INTERNAL,0x40,CPUCS_REG_EZUSB,0,buf,1,0);		
	if(ret != 1)
	{
		ERROR_OUT("0xa0###Rtusb_ResetParentPort###usb_control_msg fail=%d\n",ret);
	}
	ret=___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),ANCHOR_LOAD_INTERNAL,0x40,CPUCS_REG_FX2,0,buf,1,0);		
	if(ret != 1)
	{
		ERROR_OUT("0xa0###Rtusb_ResetParentPort###usb_control_msg fail=%d\n",ret);
	}
#endif	
	DEBUGPRINTF("#######################Rtusb_ResetParentPort end################################\n");
	return ret;
}


int Rtusb_Reset(USBDEV *udev,UCHAR resetBit)
{

	UCHAR buf[ 256 ]={0};
	int ntStatus = 0;
	int i = 0;
	int ret = 0;
	UCHAR * ptr;

	//usb_stor_pre_reset
	//usb_reset(PipeNum);
	PREQUEST_CONTROL request;
	request=(PREQUEST_CONTROL)kmalloc(sizeof(REQUEST_CONTROL),GFP_KERNEL);
	if(request == NULL)
	{
		return -1;
	}
	memset(request,0,sizeof(REQUEST_CONTROL));
	request->request_code = ANCHOR_LOAD_INTERNAL;
	request->address = CPUCS_REG_EZUSB;//0xe000;
	request->length = 1;
	request->buffer[0] = resetBit;

	ret=___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),request->request_code,0x40,request->address,0,request->buffer,request->length,0); 	
	if(ret != request->length)
	{
		ERROR_OUT("Rtusb_Reset###usb_control_msg fail=%d\n",ret);
	}
	memset(request,0,sizeof(REQUEST_CONTROL));
	request->request_code = ANCHOR_LOAD_INTERNAL;
	request->address = CPUCS_REG_FX2;//0xe000;
	request->length = 1;
	request->buffer[0] = resetBit;

	ret=___usb_control_msg__(udev,usb_sndctrlpipe(udev, 0),request->request_code,0x40,request->address,0,request->buffer,request->length,0); 	
	if(ret != request->length)
	{
		ERROR_OUT("Rtusb_Reset###usb_control_msg fail=%d\n",ret);
	}

	kfree(request);
	DEBUGPRINTF("#######################Rtusb_Reset################################");
	return ntStatus;
	
}

