#ifndef _USB_PC_STB_
#define _USB_PC_STB_

//////////////////////////////////////////////////////////////////////
//
// File:      UsbPcStb.h
// 
//
// Purpose:
//    
//
// Environment:
//    kernel mode
//
// $Author: wwg
//
//  
// Copyright (c) 2007  Novel-tongfang
//
//////////////////////////////////////////////////////////////////////


typedef struct _NOVEL_USB_FREQ_SET_CONTROL
{
    ULONG      freq_;
    ULONG      QAM_;
    ULONG      SymbolRate_;
} NOVEL_USB_FREQ_SET_CONTROL, *PNOVEL_USB_FREQ_SET_CONTROL;


typedef struct _NOVEL_USB_PID_SET_CONTROL
{
    ULONG     num_;  // 0 È«ÂëÁ÷
    USHORT    pids_[32];

} NOVEL_USB_PID_SET_CONTROL, *PNOVEL_USB_PID_SET_CONTROL;

typedef struct _NOVEL_CW 
{
    ULONG      pids_;
    UCHAR       oddkey_[8];
    UCHAR       evenkey_[8];
} NOVEL_CW,*PNOVEL_CW;

typedef struct _NOVEL_SECRET_CW 
{
    USHORT      pid_;
    UCHAR       oddkey_[8];
    UCHAR       evenkey_[8];
} NOVEL_SECRET_CW,*PNOVEL_SECRET_CW;

typedef struct _NOVEL_USB_CW_SET_CONTROL
{
    ULONG     num_;
    NOVEL_CW   cw_[32];

} NOVEL_USB_CW_SET_CONTROL, *PNOVEL_USB_CW_SET_CONTROL;

typedef struct _NOVEL_VENDOR_OR_CLASS_REQUEST_CONTROL
{
    unsigned char     request_code;
    unsigned short    address;
    unsigned char     direction;
    unsigned short    length;
	unsigned char     buffer[256];
} NOVEL_VENDOR_OR_CLASS_REQUEST_CONTROL, *PNOVEL_VENDOR_OR_CLASS_REQUEST_CONTROL;

typedef struct _NOVEL_USB_STB_ID
{
    unsigned short  wPlatformID;
    ULONG dwUniqueID;
} NOVEL_USB_STB_ID, *PNOVEL_USB_STB_ID;

typedef struct _NOVEL_USB_CHIP_DATA
{
    UCHAR ChipData[256];
} NOVEL_USB_CHIP_DATA, *PNOVEL_USB_CHIP_DATA;



#define DVBC_TUNER_1            0x000
#define DVBC_TUNER_2            0x100
#define DVBC_TUNER_3            0x200
#define DVBC_TUNER_4            0x300
#define Ezusb_IOCTL_NOVELUSB_INDEX(Tuner)       (0x800|(Tuner))

#define CTL_CODE_TEMPLATE(index) CTL_CODE(FILE_DEVICE_UNKNOWN, index, METHOD_IN_DIRECT, FILE_ANY_ACCESS)

#define IOCTL_NOVELUSB_SET_FREQ(Tuner)         CTL_CODE_TEMPLATE( Ezusb_IOCTL_NOVELUSB_INDEX(Tuner)+34 )
#define IOCTL_NOVELUSB_GET_TUNER_STATUS(Tuner) CTL_CODE_TEMPLATE( Ezusb_IOCTL_NOVELUSB_INDEX(Tuner)+35 )
#define IOCTL_NOVELUSB_SET_PID(Tuner)          CTL_CODE_TEMPLATE( Ezusb_IOCTL_NOVELUSB_INDEX(Tuner)+36 )
#define IOCTL_NOVELUSB_SET_CW(Tuner)           CTL_CODE_TEMPLATE( Ezusb_IOCTL_NOVELUSB_INDEX(Tuner)+37 )
#define IOCTL_NOVELUSB_GET_TSDATA(Tuner)       CTL_CODE_TEMPLATE( Ezusb_IOCTL_NOVELUSB_INDEX(Tuner)+38 )
#define IOCTL_NOVELUSB_GET_SMARTCART_STATUS    CTL_CODE_TEMPLATE( Ezusb_IOCTL_NOVELUSB_INDEX(0)+39 )
#define IOCTL_NOVELUSB_GET_SMARTCART_DATA      CTL_CODE_TEMPLATE( Ezusb_IOCTL_NOVELUSB_INDEX(0)+40 )
#define IOCTL_NOVELUSB_RESET_SMARTCARD         CTL_CODE_TEMPLATE( Ezusb_IOCTL_NOVELUSB_INDEX(0)+41 )
#define IOCTL_NOVELUSB_GET_FREQ(Tuner)         CTL_CODE_TEMPLATE( Ezusb_IOCTL_NOVELUSB_INDEX(Tuner)+42 )
#define IOCTL_NOVELUSB_GET_INFRARED            CTL_CODE_TEMPLATE( Ezusb_IOCTL_NOVELUSB_INDEX(0)+43 )
#define IOCTL_NOVELUSB_GET_CONFIG              CTL_CODE_TEMPLATE( Ezusb_IOCTL_NOVELUSB_INDEX(0)+44 )
#define IOCTL_NOVELUSB_TEST_URB                CTL_CODE_TEMPLATE( Ezusb_IOCTL_NOVELUSB_INDEX(0)+45 )
#define IOCTL_NOVELUSB_GetSTBID                CTL_CODE_TEMPLATE( Ezusb_IOCTL_NOVELUSB_INDEX(0)+60 )
#define IOCTL_NOVELUSB_SCFunction(Tuner)       CTL_CODE_TEMPLATE( Ezusb_IOCTL_NOVELUSB_INDEX(Tuner)+61 )

//#define  USBBUFFERSIZE                          188*512                                    

#endif
