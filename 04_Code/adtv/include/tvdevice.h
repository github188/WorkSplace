#ifndef JOYSEE_TVDEVICE_H_
#define JOYSEE_TVDEVICE_H_

#include "typ.h"
#include "tvcomm.h"

#if defined(WIN32)

	#ifdef  TVDEVICE_EXPORTS
		#define TVDEVICE_API __declspec(dllexport)
	#else
		#define TVDEVICE_API __declspec(dllimport)
	#endif 

#else

	#define TVDEVICE_API

#endif 

#ifdef  __cplusplus
extern "C" {
#endif

// 打开Tuner驱动设备
TVDEVICE_API HANDLE tvdevice_open();
// 关闭Tuner驱动设备
TVDEVICE_API void   tvdevice_close(HANDLE hDevice);
// 调频
TVDEVICE_API bool 	tvdevice_tune(HANDLE hDevice,int iFreq,int iSymb,int iQam);
// 获取调频信号及状态
TVDEVICE_API bool 	tvdevice_getTunerSignalStatus(HANDLE hDevice,TunerSignal *pStatus);
// 设置硬件过滤
TVDEVICE_API bool 	tvdevice_setPidFilter(HANDLE hDevice,const int *pPIDs,int iPidCount);
// 获取TS码流
TVDEVICE_API int 	tvdevice_readTSData(HANDLE hDevice,BYTE **ppData);
// 获取芯片ID
TVDEVICE_API bool   tvdevice_getStbID(HANDLE hDevice,char *pIDBuf,int iBufSize);
// 设置控制字
TVDEVICE_API bool 	tvdevice_setCW(HANDLE hDevice,DVBCW *pDVBcw,int cwCount);
// 与智能卡通讯
TVDEVICE_API bool 	tvdevice_SCTransmit(HANDLE hDevice,const BYTE *pInput, int inputLen,BYTE *pOutput,int *outputLen);
TVDEVICE_API bool 	tvdevice_SCFuntion(HANDLE hDevice,const BYTE *pInput, int inputLen,BYTE *pOutput,int *outputLen);
// 智能卡复位
TVDEVICE_API bool 	tvdevice_SCReset(HANDLE hDevice,OUT BYTE* pbyATR, OUT BYTE* pbyLen);
// 获取智能卡的状态(判断是否插卡)
TVDEVICE_API bool 	tvdevice_SCardInserted(HANDLE hDevice);

#ifdef __cplusplus
}
#endif


#endif // defined(JOYSEE_TVDEVICE_H_)
