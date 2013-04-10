#include <linux/kernel.h>
#include <linux/sched.h>
#include <linux/signal.h>
#include <linux/errno.h>
#include <linux/poll.h>
#include <linux/init.h>
#include <linux/slab.h>
#include <linux/fcntl.h>
#include <linux/module.h>
#include <linux/kref.h>
#include <linux/usb.h>
#include <linux/list.h>
#include <asm/uaccess.h>
#include "Rt10upDriver.h"
//#include "NovelUSB.h"

//#define TEST_TS
#ifndef UBUNTU_OS
#include <linux/smp_lock.h>
#include <linux/spinlock.h>
#endif

#ifdef TEST_TS
struct file *filp = NULL;
#endif
static int load_firmare =0;
int gfdcount = 0;
static loff_t file_pos=0;
#ifdef USE_PROCTECT_SETFREQ 
extern spinlock_t PROTECT_SET_FREQ_LOCK_;
#endif
#ifdef USE_PROCTECT_GETTSDATA	
extern spinlock_t PROTECT_GET_TSDATA_LOCK_;
#endif
#ifdef USE_PROCTECT_SC_TRANSMIT 
extern spinlock_t PROTECT_SC_TRANSMIT_LOCK_;
#endif
#ifdef USE_PROCTECT_SC_RESET	
extern spinlock_t PROTECT_RESET_LOCK_;
#endif


extern int InitNovelUSB(USBDEV *udev);
extern int ExitNovelUSB(USBDEV *udev);
extern int rt10up_download_firmware(USBDEV *udev);
extern TUNERSTATUS NOVEL_SetFreq(USBDEV *udev,PNOVEL_USB_FREQ_SET_CONTROL pUsbFreq,UCHAR TunerNum);
extern int readVersion(  USBDEV *udev,  UCHAR TunerNum);

extern int NOVEL_SpeedMode(USBDEV *udev);
extern int NOVEL_GetSmartCartStatus( USBDEV *Dev );
extern int NOVEL_ResetSmartCard(USBDEV *Dev,  PUCHAR pBuffer,USHORT *pLength);
extern int NOVEL_GetDataFromSmartCard( USBDEV *Dev,PUCHAR pCMDBuffer,USHORT cmdLength,PUCHAR pRsBuffer,USHORT *pRslen);
extern int NOVEL_GetSTBID(USBDEV *Dev, UCHAR * pwPlatformID, UCHAR * pdwUniqueID);
extern int NOVEL_SetCW(USBDEV *udev,PNOVEL_USB_CW_SET_CONTROL pCW, UCHAR TunerNum );
extern int NOVEL_SCFunction(USBDEV *udev, PUCHAR pInput, USHORT InLen, PUCHAR pOutput, USHORT *outLen, UCHAR TunerNum );
extern int NOVEL_SCTransmit(USBDEV *udev, PUCHAR pInput, USHORT InLen, PUCHAR pOutput, USHORT * outLen, UCHAR TunerNum );

extern TUNERSTATUS Novel_GetLock( USBDEV *udev, PUCHAR buf, UCHAR TunerNum );
extern int NOVEL_GetTSData( UCHAR * Addr, ULONG bufferLength, UCHAR TunerNum );
extern int NOVEL_GetTSDatatoUser( UCHAR * Addr, ULONG bufferLength, UCHAR TunerNum );
extern void NOVEL_SetParams(USBDEV *udev,ULONG symbolrate, ULONG qammode,UCHAR TunerNum );
extern TUNERSTATUS NOVEL_GetTunerStatus(USBDEV *udev,PUCHAR buf,UCHAR bufferLength,UCHAR TunerNum);
extern TUNERSTATUS NOVEL_GetFreq(USBDEV *udev,PNOVEL_USB_FREQ_SET_CONTROL freq,UCHAR TunerNum);
extern int NOVEL_SetPid(USBDEV *udev,PNOVEL_USB_PID_SET_CONTROL pPids,UCHAR TunerNum);
extern int NOVEL_GetInfraredData( USBDEV *udev, PUCHAR buf, ULONG Length);
extern int Rtusb_ResetPipe(ULONG PipeNum);
extern int Rtusb_Reset(USBDEV *udev,UCHAR resetBit);
extern int Rtusb_ResetAllPipe(void);

#ifdef OUTPRINT_FILE
extern struct file *out_fp;
#endif



/**************************************** DEFINE *************************************/
#define UUI_USB_VID 0x0511
#define UUI_USB_PID_DVBC    0x2002  //dvb-c

#define to_rt10up_dev(d) container_of(d, usb_rt10up, kref)




/************************************** PRO DECLEAR **********************************/
static struct usb_device_id rt10up_usb_table[];
static struct usb_driver rt10up_driver;

/**************************************** DECLEAR *************************************/
static int rt10up_suspend(struct usb_interface * interface,pm_message_t message);
static int rt10up_resume(struct usb_interface * interface);
static int rt10up_probe(struct usb_interface * interface, const struct usb_device_id * id);
static void rt10up_disconnect(struct usb_interface * interface);
static int rt10up_mmap(struct file *filp, struct vm_area_struct*vma);
static loff_t rt10up_llseek(struct file *file,loff_t off, int whence) ;
static ssize_t rt10up_read(struct file * file, char * buffer, size_t count, loff_t * ppos);
static ssize_t rt10up_write(struct file * file, const char * buffer, size_t count, loff_t * ppos);
static long  rt10up_ioctl( struct file * file, unsigned int cmd, unsigned long arg);
static int rt10up_open(struct inode * inode, struct file * file);
static int rt10up_release(struct inode * inode, struct file * file);
static void rt10up_delete(struct kref * kref);
extern int Rtusb_ResetParentPort(struct usb_device *udev,UCHAR resetBit );



/**************************************** STRUCT *************************************/
/* 驱动支持设备列表 */
static struct usb_device_id rt10up_usb_table[]=
{
	{ 
		USB_DEVICE(UUI_USB_VID, UUI_USB_PID_DVBC) 
	}
};

/* USB设备信息描述 */
static struct usb_driver rt10up_driver =
{
	//.owner = THIS_MODULE,
	.name = "rt10up",
	.id_table = rt10up_usb_table,
	.probe = rt10up_probe,
	.disconnect = rt10up_disconnect,
	.resume = rt10up_resume,
	.suspend = rt10up_suspend,
};

static struct file_operations rt10up_fops =
{
	owner:THIS_MODULE,
	llseek:rt10up_llseek,	
	read:rt10up_read,
	write:rt10up_write,
	unlocked_ioctl:rt10up_ioctl,
	open:rt10up_open,
	mmap:rt10up_mmap,
	release:rt10up_release,
};

/*  usb class driver info in order to get a minor number from the usb core,
*  and to have the device registered with devfs and the driver core
*/
static struct usb_class_driver rt10up_class =
{
	.name = "usb/skel%d",
	.fops = &rt10up_fops,
	.minor_base = 10,
};

/* USB设备结构体 */
typedef struct __usb_rt10up__
{
	struct usb_device * udev;   		/* the usb device for this device */
	struct usb_interface * interface;   /* the interface for this device */
	struct kref kref;

}usb_rt10up;

usb_rt10up currentDev;





//-----------------------------------------------------------------------------
//
// 驱动初始化(注册USB设备)
//
//-----------------------------------------------------------------------------
static int __init usb_rt10up_init(void)
{
	int result;
	result = usb_register(&rt10up_driver);
	if (result)
		err("usb_register failed. Error number %d", result);
	return result;
}
//-----------------------------------------------------------------------------
//
// 驱动退出(取消USB设备)
//
//-----------------------------------------------------------------------------
static void __exit usb_rt10up_exit(void)
{
	/* deregister this driver with the USB subsystem */
	usb_deregister(&rt10up_driver);
#ifdef TEST_TS
	if(filp)
	{
		filp_close(filp,NULL);
	}
#endif
}

//-----------------------------------------------------------------------------
//
// 设备检测到时运行
//
//-----------------------------------------------------------------------------
static int rt10up_probe(struct usb_interface * interface, const struct usb_device_id * id)
{
	usb_rt10up * dev = NULL;
	struct usb_host_interface * iface_desc;
	
	int retval = - ENOMEM;
	if(load_firmare == 1)
		return 0;
	
	load_firmare = 1;
	

	/* allocate memory for our device state and initialize it */
	PRINT_DEG_OUT("##################\nRt10up probing....\n");
	dev = kmalloc(sizeof(usb_rt10up), GFP_KERNEL);
	if (dev == NULL)
	{
		ERROR_OUT("Out of memory");
		goto error;
	}
	memset(dev, 0x00, sizeof(* dev));
	kref_init(&dev->kref);
	DEBUGPRINTF("\nusb_get_dev....");

	dev->udev = usb_get_dev(interface_to_usbdev(interface));
	dev->interface = interface;
	iface_desc = interface->cur_altsetting;
	usb_set_intfdata(interface, dev);

	DEBUGPRINTF("\nusb_register_dev....");

	/* we can register the device now, as it is ready */
	retval = usb_register_dev(interface,&rt10up_class);
	if (retval)
	{
		usb_set_intfdata(interface, NULL);
		goto error;
	}

	currentDev=*dev;

	DEBUGPRINTF("##################\nrt10up_download_firmware....");

	rt10up_download_firmware(dev->udev);	
	DEBUGPRINTF("##################\nInitNovelUSB....");

	InitNovelUSB(dev->udev);

	return 0;

error:
	if (dev)
	{
		kref_put(&dev->kref, rt10up_delete);
	}
	return retval;
}

//-----------------------------------------------------------------------------
//
//suspend 操作
//
//-----------------------------------------------------------------------------

static int rt10up_suspend(struct usb_interface * interface,pm_message_t message)
{
	return 0;
}
//-----------------------------------------------------------------------------
//
//resume 操作
//
//-----------------------------------------------------------------------------

//static int rt10up_resume(struct usb_device *udev)
static int rt10up_resume(struct usb_interface * interface)

{
#if 0
   /* we change the device's upstream USB link,
     * but root hubs have no upstream USB link.
     */
	usb_rt10up * dev = NULL;
   	dev = kmalloc(sizeof(usb_rt10up), GFP_KERNEL);
	if (dev == NULL)
	{
		ERROR_OUT("Out of memory");
	}
	memset(dev, 0x00, sizeof(* dev));
	kref_init(&dev->kref);
	dev->udev = usb_get_dev(interface_to_usbdev(interface));
	Rtusb_Reset(dev->udev,1);
	Rtusb_ResetParentPort(dev->udev,1);
	Rtusb_ResetAllPipe();
	if (dev)
	{	
		kfree(dev);
		kref_put(&dev->kref, rt10up_delete);
	}
#endif	
    return 0;
}

//-----------------------------------------------------------------------------
//
// 断开连接操作
//
//-----------------------------------------------------------------------------
static void rt10up_disconnect(struct usb_interface * interface)
{
	usb_rt10up * dev;
	int minor = interface->minor;
	int ret;
	mm_segment_t fs;
	unsigned char *data="rt10up_disconnect......\n";
	dev = usb_get_intfdata(interface);
	usb_set_intfdata(interface, NULL);

	/* give back our minor */
	usb_deregister_dev(interface,&rt10up_class);


	/* decrement our usage count */
	kref_put(&dev->kref, rt10up_delete);
	load_firmare = 0;
	ERROR_OUT("\nUSB Skeleton #%d now disconnected\n", minor);
#if 0
	fs=get_fs();
	
	set_fs(KERNEL_DS);
	if(out_fp != NULL)
	{
		ret = out_fp->f_op->write(out_fp, data, strlen(data),&out_fp->f_pos);
	}
	set_fs(fs);
#endif

	
	
}

//-----------------------------------------------------------------------------
//
//设备内存映射
//
//-----------------------------------------------------------------------------

static int rt10up_mmap(struct file *filp, struct vm_area_struct*vma)
{
	return 0;
}


//-----------------------------------------------------------------------------
//
//定位设备操作
//
//-----------------------------------------------------------------------------
static loff_t rt10up_llseek(struct file *file,loff_t pos, int whence) 
{
	ssize_t ret=0;
	loff_t newpos=0;
	PRINT_READ_DATA("enter rt10up_llseekpos=0x%x,whence=%d\n",pos,whence);

#ifdef TEST_TS	
	if(filp)
	{
		;//ret = filp->f_op->llseek(filp,pos,whence);
	}
	switch(whence)
	{
		case 0: /* SEEK_SET */
			newpos = pos;
			break;
		case 1: /* SEEK_CUR */
			newpos = filp->f_pos + pos;
			break;
		case 2: /* SEEK_END */
			//newpos = dev->size + pos;
			break;
		default: /* can't happen */
			return -EINVAL;
	}
	if (newpos < 0)
		return -EINVAL;
	
	filp->f_pos = newpos;
	return newpos;
	PRINT_READ_DATA("############ret = %d\n",ret);
#else
#if 0	
	switch(whence)
	{
		case 0: /* SEEK_SET */
			newpos = pos;
			break;
		case 1: /* SEEK_CUR */
			newpos =file_pos + pos;			
			break;
		case 2: /* SEEK_END */
			//newpos = dev->size + off;
			break;
		default: /* can't happen */
			return -EINVAL;
	}
	if (newpos < 0)
		return -EINVAL;
	
	file_pos = newpos;
	return newpos;
#endif	
#endif
	return ret;
}

//-----------------------------------------------------------------------------
//
// 读取设备操作
//
//-----------------------------------------------------------------------------
static ssize_t rt10up_read(struct file * file, char __user * buffer, size_t count, loff_t * ppos) 
{
	ssize_t ret=0,retval=0;
#ifdef USE_READ_INTERFACE		
	PRINT_READ_DATA("###############enter rt10up_read count=%d,*ppos=0x%x\n",count,*ppos);
	//printk("###############enter rt10up_read count=%d,*ppos=0x%x\n",count,*ppos);
#ifdef TEST_TS	
	//printk("enter rt10up_read count=%d,*ppos=0x%x,filp->f_pos=%d\n",count,*ppos,filp->f_pos);	
	if(filp)
	{
		ret = filp->f_op->read(filp, buffer, count, &filp->f_pos);
	}
	*ppos += filp->f_pos;
#else
	//ret = NOVEL_GetTSDatatoUser( buffer, count, 0);
	ret = NOVEL_GetTSData( buffer, count, 0);
	*ppos = 0;
#endif
#endif
	return ret;
}

//-----------------------------------------------------------------------------
//
// 写设备操作
//
//-----------------------------------------------------------------------------
static ssize_t rt10up_write(struct file * file, const char __user * user_buffer, size_t count, 
							loff_t * ppos)
{
	PRINT_DEG_OUT("enter rt10up_read count=%d,*ppos=0x%x\n",count,*ppos);
	return 0;
}
//-----------------------------------------------------------------------------
//
// ioctl操作
//
//-----------------------------------------------------------------------------


//static int rt10up_ioctl(struct inode * inode, struct file * file, unsigned int cmd, 
//unsigned long arg)

static long rt10up_ioctl(struct file * file, unsigned int cmd, unsigned long arg)
{
	usb_rt10up * dev;
	int i;
	int retval = -1;
	char * buffer = NULL;
	unsigned char buffertemp[CA_DATA_MAX]={0};	
	
	CA_SCARD_ATR  * scATR;
	CA_SCARD_CMD  * scCMD;
	STB_ID		  *	pstbid;
	unsigned char bulk_in_buffer[CA_TRAM_CMD_MAX]={0};
	USHORT bulk_in_buffer_size = 0;
	unsigned char bulk_out_buffer[CA_TRAM_CMD_MAX]={0};
	USHORT bulk_out_buffer_size = 0;

	int buffer_size;

	dev =&currentDev;

	if (dev->udev == NULL)
	{
		retval = - ENODEV;
		DEBUGPRINTF("\ndev NULL!");
		goto exit;
	}

	buffer =(unsigned char *)arg;

	switch (cmd)
	{
	case RT10UP_TUNER_CHECKIICWR:
		retval=readVersion(dev->udev,0);
		break;
	case RT10UP_FRONT_END_WRITE:
		DEBUGPRINTF("Enter FrontEnd_Write\n");
		buffer_size = buffer[0];		
		memset(buffertemp, 0, CA_DATA_MAX); 	
		copy_from_user(buffertemp,buffer+1,buffer_size);		
		DEBUGPRINTF("\nbuf: %x	%x	%x	%x	",buffertemp[0],buffertemp[1],buffertemp[2],buffertemp[3]); 	
		usb_control_msg(dev->udev,usb_sndctrlpipe(dev->udev, 0),0xb1,0x40,0xe000,0,buffertemp,buffer_size,0);

		break;

	case RT10UP_FRONT_END_READ:

		DEBUGPRINTF("\nEnter FrontEnd_Read\n"); 	
		buffer_size = buffer[0];		
		memset(buffertemp, 0, CA_DATA_MAX); 	
		usb_control_msg(dev->udev,usb_rcvctrlpipe(dev->udev, 0),0xb1,0xc0,0xe000,0,buffertemp,buffer_size,0);		
		DEBUGPRINTF("\nbuf: %x	%x	%x	%x	",buffertemp[0],buffertemp[1],buffertemp[2],buffertemp[3]); 	
		copy_to_user(buffer+1,buffertemp,buffer_size);			
		break;

	case RT10UP_TUNER_SET_FREQ:
		PRINT_DEG_OUT("Enter Lock_Freq\n");

		retval=NOVEL_SetFreq(dev->udev,(PNOVEL_USB_FREQ_SET_CONTROL)buffer,0);
	
		PRINT_DEG_OUT("\nregvalue = %02x\n",retval);
		break;

	case RT10UP_TUNER_GET_SIGNAL_STATUS:
		PRINT_DEG_OUT("\nEnter GetTunerStatus\n");
		retval=NOVEL_GetTunerStatus(dev->udev,bulk_out_buffer,6,0);
		if (retval==TUNERSUCESS)
		{
			copy_to_user(buffer,bulk_out_buffer,6);
		}
		PRINT_DEG_OUT("\nEnd GetTunerStatus\n");		
		break;

	case RT10UP_TUNER_GET_FREQ:
		DEBUGPRINTF("\nEnter GET_FREQ\n");		
		retval=NOVEL_GetFreq(dev->udev,(PNOVEL_USB_FREQ_SET_CONTROL)buffer,0);
		break;

	case RT10UP_TUNER_GET_TSDATA:
		retval=NOVEL_GetTSData( buffer, USBBUFFERSIZE, 0 );
		break;

	case RT10UP_TUNER_SET_PID:


		DEBUGPRINTF("\nEnter SetPids\n");		
		retval=NOVEL_SetPid(dev->udev,(PNOVEL_USB_PID_SET_CONTROL)buffer,0);

		break;

	case RT10UP_GET_INFRARED:
		DEBUGPRINTF("\nEnter GetInfrared\n");	

		retval=NOVEL_GetInfraredData(dev->udev,bulk_out_buffer,5);
		if (retval==5)
		{	
			copy_to_user(buffer,bulk_out_buffer,5);
		}
		break;
	case RT10UP_CA_SC_INSERT:
	case RT10UP_CA_SC_GET_STATUS:
#ifdef USE_PROCTECT_SC_TRANSMIT 
		spin_lock(&PROTECT_SC_TRANSMIT_LOCK_);
#endif			
		DEBUGPRINTF("\nEnter GetSmartCardStatus\n");		
		retval=NOVEL_GetSmartCartStatus (dev->udev);
		copy_to_user(buffer,&retval,sizeof(int));
#ifdef USE_PROCTECT_SC_TRANSMIT 
		spin_lock(&PROTECT_SC_TRANSMIT_LOCK_);
#endif			
		break;

	case RT10UP_CA_SC_RESET:
		PRINT_DEG_OUT("\n********RT10UP_CA_SC_RESET***********enter\n");
		DEBUGPRINTF("\nEnter ResetSmartCard\n");	

		scATR =(CA_SCARD_ATR * )buffer;
		retval=NOVEL_ResetSmartCard(dev->udev ,bulk_out_buffer,&bulk_out_buffer_size);
		DEBUGPRINTF("#######ioctlr###########%d\n",bulk_out_buffer_size);

		if (retval==0)
		{
			scATR->nLen=bulk_out_buffer_size;
			copy_to_user(scATR->Data,bulk_out_buffer,scATR->nLen);
		}	
		PRINT_DEG_OUT("\n********RT10UP_CA_SC_RESET***********retval=%d\n", retval);

		break;
	case RT10UP_CA_SC_TRANSMIT:
	case RT10UP_CA_SC_CMD:

		PRINT_DEG_OUT("\n********RT10UP_CA_SC_TRANSMIT enter***********  \n");
		
#ifdef DEBUG_HEX
		DEBUGPRINTF("\n >>>>rt10up_ioctl################RT10UP_CA_SC_TRANSMIT input start >>>%d\n",scCMD->nCommandLen);
		for(i = 0;i < scCMD->nCommandLen; i++)
		{
			DEBUGPRINTF("%x ",scCMD->Command[i]);
		}
		DEBUGPRINTF("\n<<<<rt10up_ioctl#####################################input end <<<<\n"); 
#endif

		scCMD=(CA_SCARD_CMD *)buffer;
	
		if(scCMD->nCommandLen<= CA_TRAM_CMD_MAX)
		{
			copy_from_user(bulk_in_buffer,scCMD->Command,scCMD->nCommandLen);
		}
		bulk_out_buffer_size = scCMD->nReplyLen;
	
		retval=NOVEL_GetDataFromSmartCard(dev->udev,bulk_in_buffer,scCMD->nCommandLen,bulk_out_buffer,&bulk_out_buffer_size);
		scCMD->nReplyLen=bulk_out_buffer_size;

		
		if((retval>=0)&&(scCMD->nReplyLen<=CA_TRAM_CMD_MAX))
		{
			copy_to_user(scCMD->Reply,bulk_out_buffer,bulk_out_buffer_size);
		}
		else{
			ERROR_OUT("#####SmartCard ERROR=%d,%d\n",retval,bulk_out_buffer_size);
		}
	
	
#ifdef DEBUG_HEX		
		DEBUGPRINTF("\n >>>>rt10up_ioctl################RT10UP_CA_SC_TRANSMIT output start >>>>%d\n",scCMD->nReplyLen);
		for(i = 0;i < scCMD->nReplyLen; i++)
		{
			DEBUGPRINTF(" %x",scCMD->Reply[i]);
		}
		DEBUGPRINTF("\n<<<rt10up_ioctl#####################################output end <<<<<retval=%d \n",retval); 
	
#endif
		PRINT_DEG_OUT("\n********RT10UP_CA_SC_TRANSMIT end*********** retval=%d\n", retval);
		
		break;

	case RT10UP_CA_GET_STBID:
		pstbid=(STB_ID *)buffer;
		retval=NOVEL_GetSTBID(dev->udev,pstbid->nPlatformID,pstbid->nUniqueID);
		break;
	case  RT10UP_CA_SC_FUNCTION:
		PRINT_DEG_OUT("\nNOVEL_SCFunction enter\n");	

		scCMD=(CA_SCARD_CMD *)buffer;
	
		copy_from_user(bulk_in_buffer,scCMD->Command,CA_DATA_MAX);
	
#ifdef DEBUG_HEX		
		DEBUGPRINTF("\n>>>rt10up_ioctl@@@@@@@@@@@@@@@@@@@@@@@@RT10UP_CA_SC_FUNCTION input start >>>>%d\n",scCMD->nCommandLen);
		for(i = 0;i < scCMD->nCommandLen; i++)
		{
			DEBUGPRINTF(" %x",scCMD->Command[i]);
		}
		DEBUGPRINTF("\n<<<nrt10up_ioctl@@@@@@@@@@@@@@@@@@@@@@@@RT10UP_CA_SC_FUNCTION input end<<<<<<\n"); 
#endif		
		retval=NOVEL_SCFunction(dev->udev,bulk_in_buffer,(USHORT)scCMD->nCommandLen,bulk_out_buffer,&bulk_out_buffer_size,0);
		DEBUGPRINTF("\n>>>rt10up_ioctl@@@@@@@@@@@@@@@@@@@@@@@@RT10UP_CA_SC_FUNCTION output start >>>>>>>>>%d\n",bulk_out_buffer_size);
		
		memset(scCMD->Reply,0,CA_DATA_MAX);
		if(retval>=0)
		{
			scCMD->nReplyLen = bulk_out_buffer_size;
			copy_to_user(scCMD->Reply,bulk_out_buffer,scCMD->nReplyLen);
		}
#ifdef DEBUG_HEX		
		for(i = 0;i < scCMD->nReplyLen; i++)
		{
			DEBUGPRINTF(" %x",scCMD->Reply[i]);
		}
		DEBUGPRINTF("\n<<<<rt10up_ioctl RT10UP_CA_SC_FUNCTION@@@@@@@@@@@@@@@@@@@@@@@@output end <<<<<<<retval=%d \n",retval); 
#endif
		
	
		PRINT_DEG_OUT("\nNOVEL_SCFunction end\n");	
		break;
	case RT10UP_CA_SC_SET_CW:
		retval=NOVEL_SetCW(dev->udev,(PNOVEL_USB_CW_SET_CONTROL)buffer,0);
		break;	
	case RT10UP_RESET_USB_PIPE:
		retval = Rtusb_ResetPipe(*buffer);
		break;
	case  RT10UP_USB_SPEED_MODE:
		retval = NOVEL_SpeedMode(dev->udev);
		break;
	default:
		ERROR_OUT("Unknow cmd :%d\n", cmd);
		break;
	}

exit:
	/* return that we did not understand this ioctl call */
	return retval;
}


//-----------------------------------------------------------------------------
//
// 打开设备操作
//
//-----------------------------------------------------------------------------
static int rt10up_open(struct inode * inode, struct file * file)
{
	mm_segment_t old_fs;
	//gfdcount++;
	PRINT_DEG_OUT("rt10up_open\n");
	//printk("rt10up_open\n");
#ifdef TEST_TS	
	filp = filp_open("/data/0611.ts", O_RDONLY , 0);
	old_fs = get_fs();
	set_fs(get_ds());

	if(filp)
	{
		filp->f_op->llseek(filp,0,0);
	}
#endif	
	return 0;
}

//-----------------------------------------------------------------------------
//
// 释放操作
//
//-----------------------------------------------------------------------------
static int rt10up_release(struct inode * inode, struct file * file)
{
	return 0;
}

//-----------------------------------------------------------------------------
//
// 删除操作
//
//-----------------------------------------------------------------------------
static void rt10up_delete(struct kref * kref)
{
	int ret = 0;
	mm_segment_t fs;
	unsigned char *data="rt10up_delete......\n";
	usb_rt10up * dev = to_rt10up_dev(kref);
	load_firmare = 0;
	ExitNovelUSB(dev->udev);

	usb_put_dev(dev->udev);

	kfree(dev);
#ifdef OUTPRINT_FILE
	fs=get_fs();

	set_fs(KERNEL_DS);
	if(out_fp != NULL)
	{
		ret = out_fp->f_op->write(out_fp, data, strlen(data),&out_fp->f_pos);
	}
	set_fs(fs);
	if(out_fp != NULL)
	{
		filp_close(out_fp,NULL);
	}
#endif
	printk("\n###########rt10up_delete\n");
}

module_init(usb_rt10up_init);
module_exit(usb_rt10up_exit);
MODULE_LICENSE("GPL");
