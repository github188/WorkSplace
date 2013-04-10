/** \file RT10UPreg.h
  * *******************************************************************
  *  Copyright (c) 2008  CHINADTV
  *
  * *******************************************************************
  *
  *  Please review the terms of the license agreement before using
  *  this file. If you are not an authorized user, please destroy this
  *  source code file and notify CHINADTV immediately that you
  *  inadvertently received an unauthorized copy.
  * *******************************************************************
  * Summary :
  *  - Project name         : RT10UP
  *  - Project description  : Define RT10UP's registers for RT10UP controller
  *  - File name            : RT10UPreg.h
  *  - File contents        : definitions for RT10UP registers and bits
  *  - Purpose              :
  *  - Design Engineer      : Mason
  *  - Version              : 1.02
  *  - Last modification    : 2008-01-06
*/

#ifndef __RT10UPREG_H__
#define __RT10UPREG_H__


//========================================================
//  Base address for RT10UP's registers
//========================================================

// RT10UP code definitions

#define ROM8K    0x0000    //0x0000-0x1fff

#define RAMcode    0x2000    //0x2000-0x4fff
#define RAMxdata   0x5000    //0x5000-0x53ff
#define RAMother   0x5400    //0x5400-0x5fff


#define ModeSelect   0x6000    //0x6000 0x6000  1   ModeSelect(bit0: 1¡«PCI,0¡«USB)
//========================================================
// RT10UP PCI definitions
//========================================================

#define PCIBASE  0xC000
//default pci buffer max size
#define MAX_PCI_SIZE    256

#define pcidat     PCIBASE + 0x000   //-data change zone  000h - 0FFh

#define pcicon     PCIBASE + 0x100

#define pcidat0    PCIBASE + 0x101   //setupdat0
#define pcidat1    PCIBASE + 0x102   //setupdat1
#define pcidat2    PCIBASE + 0x103   //setupdat2
#define pcidat3    PCIBASE + 0x104   //setupdat3
//#define pcidat4  PCIBASE + 0x105   //setupdat4
//#define pcidat5  PCIBASE + 0x106   //setupdat5
#define pcidat6    PCIBASE + 0x105   //setupdat6
#define pcidat7    PCIBASE + 0x106   //setupdat7

#define PCIInit() pcicon|=R51init_MASK;   \
  pcicon|=pciR51ien_MASK

#define _ClearPCIivect() pcicon&=~pcibufready_MASK

#define _BufferOK() pcicon&=~bufbusy_MASK

/* Defintions for pciirq register bits masks */


#define bufbusy_MASK          0x01

#define R51reset_MASK         0x02

#define R51init_MASK          0x04

#define pcibufready_MASK      0x08

#define R51bufready_MASK      0x10

#define pciR51ien_MASK        0x20

#define R51pciien_MASK        0x40

#define R51pciivect_MASK      0x80

//========================================================
//  RT10UP OTP definitions
//========================================================

#define OTPBASE  0xD000

#define USBdesc         OTPBASE + 0x000    //0xD000 0xD0FF 256 USB descriptors
#define PublicKey       OTPBASE + 0x100    //0xD100 0xD17F 128 PublicKey
#define DSN             OTPBASE + 0x180    //0xD180 0xD185 6 DSN
#define SecLoaderKey    OTPBASE + 0x190    //0xD190 0xD19F 16 SecLoadKey
#define IV_HASH         OTPBASE + 0x1A0    //0xD1A0 0xD1AF 16 IV_HASH

#define DSKencrypt      OTPBASE + 0x1C0    //0xD1C0 0xD1FF 64 DSK' * 4


//========================================================
//  De-Scramble Module (4 K Byte)
//========================================================
#define DESBASE  0xE000

#define FrontEnd1   DESBASE + 0x000  //0xE000 0xE07F 128 FrontEnd1_PID_Filter
#define FrontEnd1test   DESBASE + 0x040  //FrontEnd1 Test Mode
#define FrontEnd1frq1   DESBASE + 0x041  //FrontEnd1 TS freq1
#define FrontEnd1frq2   DESBASE + 0x042  //FrontEnd1 TS freq2
#define FrontEnd1frq3   DESBASE + 0x043  //FrontEnd1 TS freq3

#define FrontEnd2   DESBASE + 0x080  //0xE080 0xE0FF 128 FrontEnd2_PID_Filter
#define FrontEnd2test   DESBASE + 0x0C0  //FrontEnd2 Test Mode
#define FrontEnd2frq1   DESBASE + 0x0C1  //FrontEnd2 TS freq1
#define FrontEnd2frq2   DESBASE + 0x0C2  //FrontEnd2 TS freq2
#define FrontEnd2frq3   DESBASE + 0x0C3  //FrontEnd2 TS freq3

#define FrontEnd3   DESBASE + 0x100  //0xE100 0xE17F 128 FrontEnd3_PID_Filter
#define FrontEnd3test   DESBASE + 0x140  //FrontEnd3 Test Mode
#define FrontEnd3frq1   DESBASE + 0x141  //FrontEnd3 TS freq1
#define FrontEnd3frq2   DESBASE + 0x142  //FrontEnd3 TS freq2
#define FrontEnd3frq3   DESBASE + 0x143  //FrontEnd3 TS freq3

#define I2CBUS      DESBASE + 0x300  //0xE300 0xE300 1 I2C Bus Select

#define SmartCard   DESBASE + 0x400  //0xE400 0xE606 519 Smartcard Interface
#define SCcon           DESBASE + 0x600  //SmartCard control register
#define SCsta           DESBASE + 0x601  //SmartCard status register
#define SCsendl         DESBASE + 0x602  //SmartCard send length low byte register
#define SCsendh         DESBASE + 0x603  //SmartCard send length high byte register
#define SCreturnl       DESBASE + 0x604  //SmartCard return length low byte register
#define SCreturnh       DESBASE + 0x605  //SmartCard return length high byte register
#define SCother         DESBASE + 0x606  //SmartCard int and supply register



#define SFIFO       DESBASE + 0x700  //0xE700 0xE7FF 256 SFIFO_MUX
#define SFIFOtuner1l    DESBASE + 0x700  //0xE700 0xE701 2 SFIFO_tuner1 lost package
#define SFIFOtuner1r    DESBASE + 0x702  //0xE702 0xE703 2 SFIFO_tuner1 received package
#define SFIFOtuner2l    DESBASE + 0x704  //0xE704 0xE705 2 SFIFO_tuner2 lost package
#define SFIFOtuner2r    DESBASE + 0x706  //0xE706 0xE707 2 SFIFO_tuner2 received package
#define SFIFOtuner3l    DESBASE + 0x708  //0xE708 0xE709 2 SFIFO_tuner3 lost package
#define SFIFOtuner3r    DESBASE + 0x70A  //0xE70a 0xE70b 2 SFIFO_tuner3 received package

#define Descramb1   DESBASE + 0x800  //0xE800 0xE9FF 512 De-scrambler1 CW Buffer
#define Descramb2   DESBASE + 0xA00  //0xEA00 0xEBFF 512 De-scrambler2 CW Buffer
#define Descramb3   DESBASE + 0xC00  //0xEC00 0xEDFF 512 De-scrambler3 CW Buffer

#define TDESreg     DESBASE + 0xE00  //0xEE00 0xEE2F 32 TDES registers
#define TDESindata      DESBASE + 0xE00  //0xEE00 0xEE07 8 TDES indata
#define TDEScwadd       DESBASE + 0xE08  //0xEE08 0xEE08 1 TDES CW address
#define TDEScon         DESBASE + 0xE09  //0xEE09   0xEE09 1 TDES control
#define TDESoutdata     DESBASE + 0xE10  //0xEE10 0xEE17 8 TDES outdata
#define TDESindsk       DESBASE + 0xE20  //0xEE20 0xEE2F 16 TDES in DSK

#define DESreg      DESBASE + 0xF00  //0xEF00 0xEF2F 32 DES register
#define DESindata       DESBASE + 0xF00  //0xEF00 0xEF07 8 DES indata
#define DESkey          DESBASE + 0xF08  //0xEF08 0xEF08 8 DES key
#define DEScon          DESBASE + 0xF10  //0xEF09   0xEF09 1 DES control
#define DESoutdata      DESBASE + 0xF20  //0xEF20 0xEF27 8 DES outdata



//========================================================
//  CUSB2 Module
//========================================================
#define USBBASE  0xF000

#define USBreg      USBBASE + 0x000  //0xF000 0xE3FF 1K USB

#define USBIRQRegister  0xF18C


#endif
