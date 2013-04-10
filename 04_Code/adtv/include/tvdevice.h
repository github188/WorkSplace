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

// ��Tuner�����豸
TVDEVICE_API HANDLE tvdevice_open();
// �ر�Tuner�����豸
TVDEVICE_API void   tvdevice_close(HANDLE hDevice);
// ��Ƶ
TVDEVICE_API bool 	tvdevice_tune(HANDLE hDevice,int iFreq,int iSymb,int iQam);
// ��ȡ��Ƶ�źż�״̬
TVDEVICE_API bool 	tvdevice_getTunerSignalStatus(HANDLE hDevice,TunerSignal *pStatus);
// ����Ӳ������
TVDEVICE_API bool 	tvdevice_setPidFilter(HANDLE hDevice,const int *pPIDs,int iPidCount);
// ��ȡTS����
TVDEVICE_API int 	tvdevice_readTSData(HANDLE hDevice,BYTE **ppData);
// ��ȡоƬID
TVDEVICE_API bool   tvdevice_getStbID(HANDLE hDevice,char *pIDBuf,int iBufSize);
// ���ÿ�����
TVDEVICE_API bool 	tvdevice_setCW(HANDLE hDevice,DVBCW *pDVBcw,int cwCount);
// �����ܿ�ͨѶ
TVDEVICE_API bool 	tvdevice_SCTransmit(HANDLE hDevice,const BYTE *pInput, int inputLen,BYTE *pOutput,int *outputLen);
TVDEVICE_API bool 	tvdevice_SCFuntion(HANDLE hDevice,const BYTE *pInput, int inputLen,BYTE *pOutput,int *outputLen);
// ���ܿ���λ
TVDEVICE_API bool 	tvdevice_SCReset(HANDLE hDevice,OUT BYTE* pbyATR, OUT BYTE* pbyLen);
// ��ȡ���ܿ���״̬(�ж��Ƿ�忨)
TVDEVICE_API bool 	tvdevice_SCardInserted(HANDLE hDevice);

#ifdef __cplusplus
}
#endif


#endif // defined(JOYSEE_TVDEVICE_H_)
