#ifndef __NOVEL_SUPERTV_RT10UP_H__
#define __NOVEL_SUPERTV_RT10UP_H__

#if 1
#define ANDRIOD_LINUX

//#define UBUNTU_OS
//#define USE_PROCTECT_INTERFACE
//#define USE_PROCTECT_SETFREQ
//#define USE_PROCTECT_GETTSDATA
//#define USE_PROCTECT_SC_TRANSMIT
//#define USE_PROCTECT_SC_RESET
//#define USE_PROCTECT_SETPID
//#define USE_PROCTECT_SC_FUNCTION


//#define READ_DATA_DEG
//#define DBG_TUNER
//#define DBG_SC
//#define DBG_SC_WR
#define PRINT_EROR
//#define PRINT_DEG
//#define USE_FIRWARE_FILE
//#define OUTPRINT_FILE
//#define USE_READ_INTERFACE
//#define READ_SUCCES
#ifdef READ_DATA_DEG															
#define PRINT_READ_DATA(format, ...)	printk(format, ##__VA_ARGS__)
#else
#define PRINT_READ_DATA(format, ...);
#endif

#ifdef PRINT_DEG															
#define PRINT_DEG_OUT(format, ...)	printk(format, ##__VA_ARGS__)
#else
#define PRINT_DEG_OUT(format, ...);
#endif

#ifdef PRINT_EROR															
#define ERROR_OUT(format, ...)	printk(format, ##__VA_ARGS__)
#else
#define ERROR_OUT(format, ...);
#endif


#ifdef DBG_TUNER															
#define DEBUGPRINTF(format, ...)	printk(format, ##__VA_ARGS__)
#else
#define DEBUGPRINTF(format, ...);
#endif
#ifdef DBG_SC
#define DBG_SC_PRINT(format, ...)	printk(format, ##__VA_ARGS__)
#else
#define DBG_SC_PRINT(format, ...);
#endif

#ifdef DBG_SC_WR
#define DBG_SC_WR_P(format, ...)	printk(format, ##__VA_ARGS__)
#else
#define DBG_SC_WR_P(format, ...);
#endif
#endif

#define CPUCS_REG_EZUSB    0x7F92
#define CPUCS_REG_FX2	   0xE600


#define USBBUFFERSIZE	   (188*496*2)	 
#define USB_BULK_SIZE	   (188*496*2)	
#define COUN_TS_BYTE	   (USBBUFFERSIZE/496)	

#define TSURBCOUNT	   		(8)
#define BLOCKCOUNT			(TSURBCOUNT)

#define UUIUSB_IOC_MAGIC 		('k')
#define SCFUNCLENGTH			(257 )
#define ANCHOR_LOAD_INTERNAL	(0xA0)
#define TunerInterface			(0xb0)
#define MemoryInterface 		(0xb1)
#define InfraredInterface		(0xb2)
#define GPIOInterface			(0xb3)
#define GeneralInterface		(0xb4)

#define SYNC40 (0x40)
#define SYNC41 (0x41)
#define SYNC42 (0x42)
#define SYNC47 (0x47)


#define RT10UP_TUNER_SET_FREQ 			_IO(UUIUSB_IOC_MAGIC,1)
#define RT10UP_TUNER_GET_FREQ 			_IO(UUIUSB_IOC_MAGIC,2)
#define RT10UP_TUNER_SET_PID 			_IO(UUIUSB_IOC_MAGIC,3)
#define RT10UP_TUNER_GET_TSDATA 		_IO(UUIUSB_IOC_MAGIC,4)
#define RT10UP_TUNER_GET_SIGNAL_STATUS 	_IO(UUIUSB_IOC_MAGIC,5)
#define RT10UP_GET_INFRARED 			_IO(UUIUSB_IOC_MAGIC,6)
#define RT10UP_CA_SC_GET_STATUS 		_IO(UUIUSB_IOC_MAGIC,7)
#define RT10UP_CA_SC_RESET 				_IO(UUIUSB_IOC_MAGIC,8)
#define RT10UP_CA_SC_CMD 				_IO(UUIUSB_IOC_MAGIC,9)
#define RT10UP_CA_GET_STBID				_IO(UUIUSB_IOC_MAGIC,10)
#define RT10UP_CA_SC_SET_CW 			_IO(UUIUSB_IOC_MAGIC,11)
#define RT10UP_CA_SC_FUNCTION 			_IO(UUIUSB_IOC_MAGIC,12)
#define RT10UP_CA_SC_TRANSMIT			_IO(UUIUSB_IOC_MAGIC,13)
#define RT10UP_CA_SC_INSERT				_IO(UUIUSB_IOC_MAGIC,14)
#define RT10UP_TUNER_CHECKIICWR 		_IO(UUIUSB_IOC_MAGIC,15)
#define RT10UP_RESET_USB_PIPE 			_IO(UUIUSB_IOC_MAGIC,16)
#define RT10UP_USB_SPEED_MODE			_IO(UUIUSB_IOC_MAGIC,17)
#define RT10UP_FRONT_END_WRITE 			_IO(UUIUSB_IOC_MAGIC,20)
#define RT10UP_FRONT_END_READ 			_IO(UUIUSB_IOC_MAGIC,21)

#define MAX_PID_NUM     				(32)
#define CA_TRAM_CMD_MAX 				(320)
#define CA_ATR_MAX 						(33)
#define CA_DATA_MAX 					(256)

#define SCDATAOK			(0)	
#define SCWAITING			(1)
#define SCTIMEOUT			(2)
#define SCNONE				(3)
#define SCUNRECOGNIZED		(4)

typedef	signed long    LONG;
typedef unsigned long  ULONG;
typedef unsigned short USHORT;
typedef unsigned char  UCHAR;
typedef unsigned char* PUCHAR;	
typedef unsigned long* PULONG;


typedef struct usb_device   USBDEV;
typedef struct completion   COMPLETION;
typedef struct urb          URB;
typedef struct workqueue_struct WORKQUEUE;
typedef struct work_struct  WORKTASK;


typedef enum {
	SETSYBOL_ERROR = -9,
	SETQAM_ERROR =-8,
	USBICWR_ERROR=-7,
	IICWRFAIL	=	-6,
	INVAILDFREQ = -5,
	MEMORYERROR = -4,
	INVAILDPARM = -3,
	LOCKFAIL	= -2,
	TUNERERROR	= -1,
	ERROR	= -1,
	TIMEOUT 	= -1,
	NOERROR 	= 0,
	TUNERSUCESS = 0
}TUNERSTATUS,SCSTATUS,STATUS;

typedef struct _tm10023Qam_t
{
	UCHAR  bQam;
	UCHAR  bLockthr;
	UCHAR  bMseth;
	UCHAR  bAref;
	UCHAR  bAgcRefNyq;
	UCHAR  bErAgcNyqThd;
} tmQam_t;

typedef struct RT10UP_REQUEST_CONTROL
{	
	UCHAR	 request_code;	
	USHORT	  address;	
	UCHAR	  direction;	
	USHORT	  length;	
	UCHAR	  buffer[CA_DATA_MAX];
}REQUEST_CONTROL,*PREQUEST_CONTROL;   

typedef struct _NOVEL_USB_FREQ_SET_CONTROL
{
	ULONG	   freq_;
	ULONG	  QAM_;
	ULONG	   SymbolRate_;
} NOVEL_USB_FREQ_SET_CONTROL, *PNOVEL_USB_FREQ_SET_CONTROL;

typedef struct _NOVEL_USB_PID_SET_CONTROL
{
	ULONG	  num_;  // 0 全码流
	USHORT		pids_[MAX_PID_NUM];

} NOVEL_USB_PID_SET_CONTROL, *PNOVEL_USB_PID_SET_CONTROL;

typedef struct _NOVEL_CW 
{
	ULONG	   pids_;
	UCHAR		oddkey_[8];
	UCHAR		evenkey_[8];
} NOVEL_CW,*PNOVEL_CW;

typedef struct _NOVEL_SECRET_CW 
{
	USHORT		pid_;
	UCHAR		oddkey_[8];
	UCHAR		evenkey_[8];
} NOVEL_SECRET_CW,*PNOVEL_SECRET_CW;

typedef struct _NOVEL_USB_CW_SET_CONTROL
{
	ULONG	  num_;
	NOVEL_CW   cw_[MAX_PID_NUM];

} NOVEL_USB_CW_SET_CONTROL, *PNOVEL_USB_CW_SET_CONTROL;

typedef struct _NOVEL_VENDOR_OR_CLASS_REQUEST_CONTROL
{
	UCHAR	 request_code;
	USHORT	  address;
	UCHAR	  direction;
	USHORT	  length;
	UCHAR	  buffer[CA_DATA_MAX];
} NOVEL_VENDOR_OR_CLASS_REQUEST_CONTROL, *PNOVEL_VENDOR_OR_CLASS_REQUEST_CONTROL;

typedef struct _NOVEL_USB_STB_ID
{
	USHORT	wPlatformID;
	ULONG dwUniqueID;
} NOVEL_USB_STB_ID, *PNOVEL_USB_STB_ID;

typedef struct _NOVEL_USB_CHIP_DATA
{
	UCHAR ChipData[CA_DATA_MAX];
} NOVEL_USB_CHIP_DATA, *PNOVEL_USB_CHIP_DATA;

typedef struct
{
	ULONG nLen;
	UCHAR Data[CA_DATA_MAX];
}CA_SCARD_ATR;


typedef struct
{
	UCHAR Command[CA_TRAM_CMD_MAX];
	ULONG nCommandLen;  
	UCHAR Reply[CA_TRAM_CMD_MAX];  
	ULONG nReplyLen;
}CA_SCARD_CMD;

typedef struct
{
	UCHAR nPlatformID[2];
	UCHAR nReserve[2];
	UCHAR nUniqueID[4]; // 过滤器值
}STB_ID;
typedef struct
{
	ULONG PID;
	ULONG counter;
}PID_COUNTINUER;



#if 0
#define EMM_PID_NUM 	(8 )
#define STREAM_PID_NUM 	(32)
#define ECM_PID_NUM 	(16)
typedef struct
{
	unsigned int nCaSysId[EMM_PID_NUM];
	unsigned int nEmmPid[EMM_PID_NUM];
}EMM_PID;


typedef struct
{
	unsigned int nCount;
	unsigned int nEcmPid;
	unsigned int nPids[STREAM_PID_NUM];
}STREAM_PID;
#endif

#endif

