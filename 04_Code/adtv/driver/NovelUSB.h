#ifndef __NOVELUSB3TUNER_H__
#define __NOVELUSB3TUNER_H__
#include "Rt10upDriver.h"

int InitNovelUSB(USBDEV *udev);
int ExitNovelUSB(USBDEV *udev);
int InitFirmware(void);
int rt10up_download_firmware(USBDEV *udev);
int NOVEL_GetInfraredData( USBDEV *udev, PUCHAR buf, ULONG Length);
int ReparPidData( PUCHAR buf, ULONG pid, USHORT decryption, USHORT allpid );
int NOVEL_SpeedMode(USBDEV *udev);
int NOVEL_GetSmartCartStatus( USBDEV *Dev );
int NOVEL_ResetSmartCard(USBDEV *Dev,  PUCHAR pBuffer,USHORT *pLength);
int NOVEL_GetDataFromSmartCard( USBDEV *Dev,PUCHAR pCMDBuffer,USHORT cmdLength,PUCHAR pRsBuffer,USHORT *pRslen);
int NOVEL_GetSTBID(USBDEV *Dev, UCHAR * pwPlatformID, UCHAR * pdwUniqueID);
int NOVEL_SetCW(USBDEV *udev,PNOVEL_USB_CW_SET_CONTROL pCW, UCHAR TunerNum );
int NOVEL_SCFunction(USBDEV *udev, PUCHAR pInput, USHORT InLen, PUCHAR pOutput, USHORT *outLen, UCHAR TunerNum );
int NOVEL_SCTransmit(USBDEV *udev, PUCHAR pInput, USHORT InLen, PUCHAR pOutput, USHORT * outLen, UCHAR TunerNum );
int Rtusb_ResetPipe(ULONG PipeNum);



//apls api
TUNERSTATUS NOVEL_SetFreq(USBDEV *udev,PNOVEL_USB_FREQ_SET_CONTROL pUsbFreq,UCHAR TunerNum);
int readVersion(  USBDEV *udev,  UCHAR TunerNum);
ULONG ReadRegValueEx72bit( ULONG regAddr, USBDEV *udev, PUCHAR buf, UCHAR tunernumber );
ULONG ReadRegValueEx( ULONG regAddr, USBDEV *udev, PUCHAR buf, UCHAR tunernumber );
void  WriteDemoRegValue( UCHAR reg, USBDEV *udev, PUCHAR buf, UCHAR tunernumber );

TUNERSTATUS TunerFreqOffset( USBDEV *udev,  PUCHAR buf, ULONG freq, UCHAR TunerNum );
UCHAR ReadRegValue( UCHAR reg, USBDEV *udev, PUCHAR buf, UCHAR tunernumber );
void CalcFrequency( ULONG Frequency, PUCHAR buf );
void InitQAM( USBDEV *udev, PUCHAR buf, UCHAR TunerNum);
TUNERSTATUS SetQAM(  USBDEV *udev,  PUCHAR buf, ULONG Mode, UCHAR TunerNum);
TUNERSTATUS SetSymbolRate(	USBDEV *udev, PUCHAR buf, ULONG SymbolRate, UCHAR TunerNum);
TUNERSTATUS Novel_GetLock( USBDEV *udev, PUCHAR buf, UCHAR TunerNum );
int NOVEL_GetTSData( UCHAR * Addr, ULONG bufferLength, UCHAR TunerNum );
void NOVEL_SetParams(USBDEV *udev,ULONG symbolrate, ULONG qammode,UCHAR TunerNum );
TUNERSTATUS NOVEL_GetTunerStatus(USBDEV *udev,PUCHAR buf,UCHAR bufferLength,UCHAR TunerNum);
TUNERSTATUS NOVEL_GetFreq(USBDEV *udev,PNOVEL_USB_FREQ_SET_CONTROL freq,UCHAR TunerNum);
int NOVEL_SetPid(USBDEV *udev,PNOVEL_USB_PID_SET_CONTROL pPids,UCHAR TunerNum);


#endif
