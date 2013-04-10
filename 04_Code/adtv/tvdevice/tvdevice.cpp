#include "tvdevice.h"
#include "tunerdevice.h"

#define  LOG_TAG "libtvdevice"
#include "tvlog.h"

HANDLE tvdevice_open()
{
	TvDevice* pDevice = new TvDevice();
	if(pDevice)
	{
		if(!pDevice->open())
		{
			delete pDevice;
			pDevice = 0;
		}
	}

	return static_cast<HANDLE>(pDevice);
}

void   tvdevice_close(HANDLE hDevice)
{
	TvDevice* pDevice = static_cast<TvDevice*>(hDevice);
	if(pDevice)
	{
		delete pDevice;
	}
}

bool    tvdevice_getStbID(HANDLE hDevice,char *pIDBuf,int iBufSize)
{
	bool bRet = false;
	TvDevice* pDevice = static_cast<TvDevice*>(hDevice);
	if(0 != pDevice)
	{
		bRet = pDevice->getStbID(pIDBuf,iBufSize);
	}

	return bRet;
}

bool 	tvdevice_tune(HANDLE hDevice,int iFreq,int iSymb,int iQam)
{
	bool bRet = false;
	TvDevice* pDevice = static_cast<TvDevice*>(hDevice);
	if(0 != pDevice)
	{
		bRet = pDevice->tune(0,iFreq,iQam,iSymb);
	}

	return bRet;
}

bool 	tvdevice_getTunerSignalStatus(HANDLE hDevice, TunerSignal *pStatus)
{
	bool bRet = false;
	TvDevice* pDevice = static_cast<TvDevice*>(hDevice);
	if(0 != pDevice)
	{
		long locked = 0,strength=0,quality=0;
		bRet = pDevice->getSignalStatus(locked,strength,quality);
		if(pStatus)
		{
			pStatus->locked		= locked;
			pStatus->strength	= strength;
			pStatus->quality	= quality;
		}
	}

	return bRet;
}	

bool 	tvdevice_setPidFilter(HANDLE hDevice,const int *pPIDs,int iPidCount)
{
	bool bRet = false;
	TvDevice* pDevice = static_cast<TvDevice*>(hDevice);
	if(0 != pDevice)
	{
		bRet = pDevice->setpid_control(pPIDs,iPidCount);
	}

	return bRet;
}	

int 	tvdevice_readTSData(HANDLE hDevice,BYTE **ppData)
{
	int iRet = -1;

	TvDevice* pDevice = static_cast<TvDevice*>(hDevice);
	if(0 != pDevice)
	{
		*ppData = pDevice->getTsData();
		iRet = (0 != *ppData) ? TD_TSDATSZIE : -1;
	}

	return iRet;
}	

bool 	tvdevice_setCW(HANDLE hDevice, DVBCW *pDVBcw,int cwCount)
{
	bool bRet = false;
	TvDevice* pDevice = static_cast<TvDevice*>(hDevice);
	if(0 != pDevice)
	{
		bRet = pDevice->setCW(pDVBcw,cwCount);
	}

	return bRet;
}	

bool 	tvdevice_SCTransmit(HANDLE hDevice,const BYTE *pInput, int inputLen,BYTE *pOutput,int *outputLen)
{
	bool bRet = false;
	TvDevice* pDevice = static_cast<TvDevice*>(hDevice);
	if(0 != pDevice)
	{
		bRet = pDevice->smartcard_transmit(pInput,inputLen,pOutput,outputLen);
	}

	return bRet;
}	

bool 	tvdevice_SCFuntion(HANDLE hDevice,const BYTE *pInput, int inputLen,BYTE *pOutput,int *outputLen)
{
	bool bRet = false;
	TvDevice* pDevice = static_cast<TvDevice*>(hDevice);
	if(0 != pDevice)
	{
		bRet = pDevice->smartcard_funtion(pInput,inputLen,pOutput,outputLen);
	}

	return bRet;
}	

bool 	tvdevice_SCReset(HANDLE hDevice,OUT BYTE* pbyATR, OUT BYTE* pbyLen)
{
	bool bRet = false;
	TvDevice* pDevice = static_cast<TvDevice*>(hDevice);
	if(0 != pDevice)
	{
		bRet = pDevice->smartcard_reset(pbyATR,pbyLen);
	}

	return bRet;
}	

bool 	tvdevice_SCardInserted(HANDLE hDevice)
{
	bool bRet = false;
	TvDevice* pDevice = static_cast<TvDevice*>(hDevice);
	if(0 != pDevice)
	{
		bRet = pDevice->smartcard_inserted();
	}
	
	return bRet;
}	
