#include "tunerdevice.h"
 
#if defined(WIN32)
#include "UsbPCStb.h"
#include <WinIoCtl.h>

#else

#include "Rt10upDriver.h"
#include <utils/Log.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/ioctl.h>

#endif

#define  LOG_TAG "libtvdevice"
#include "tvlog.h"

#if 0 
TvDevice* TvDevice::m_pDevice = 0;
TvDevice* TvDevice::getDevice()
{    
	if (0 == m_pDevice)    
	{
		m_pDevice = new TvDevice();    
	}    
	
	return m_pDevice;
}

void TvDevice::cleanDevice()
{
	if(m_pDevice)
	{
		delete m_pDevice;
		m_pDevice = 0;
	}
}
#endif 

TvDevice::TvDevice() 
: h_(0)
{
}
 
TvDevice::~TvDevice()
{
	if(h_)
		close();
}

bool TvDevice::open()
{
	bool bRet = true;
#if defined(WIN32)
	h_ = CreateFile(
		"\\\\.\\ntdvbcusbcard-0",
		GENERIC_WRITE,
		FILE_SHARE_WRITE,
		NULL,
		OPEN_EXISTING,
		0,
		NULL);
	
	if(INVALID_HANDLE_VALUE == h_)
		bRet = false;
#else
	if ((h_ = ::open("/dev/rt10up", O_RDWR)) < 0)
		bRet = false;
#endif 

	LOGTRACE(LOGINFO,"TvDevice::open %s.\n",bRet ? "success":"failed");

	return bRet;
}

bool TvDevice::isopen() 
{
	bool bRet = true ;

#if defined(WIN32)
	bRet = (INVALID_HANDLE_VALUE != h_) ? true : false;
#else
	bRet = (0 != h_) ? true : false;
#endif 

	return bRet; 
}

void TvDevice::close()
{
	LOGTRACE(LOGDEBUG,"Enter TvDevice::close().\n");

#if defined(WIN32)
	if(INVALID_HANDLE_VALUE != h_) 
	{
		CloseHandle(h_);
		h_=INVALID_HANDLE_VALUE;
	}
#else
	if(0 != h_)
	{
		::close(h_);
	}
#endif 

	LOGTRACE(LOGINFO,"Leave TvDevice::close.\n");
}

bool TvDevice::tune(long iTuner,long freq,long qam,long symb)
{
	bool bRet = true;

	NOVEL_USB_FREQ_SET_CONTROL 	param;
	param.freq_       = static_cast<ULONG>(freq);
	param.QAM_        = static_cast<ULONG>(qam);
	param.SymbolRate_ = static_cast<ULONG>(symb);

#if defined(WIN32)
	DWORD uRetBytes = 0;
	BOOL rt = DeviceIoControl (
		h_,
		IOCTL_NOVELUSB_SET_FREQ(DVBC_TUNER_1),
		&param,
		sizeof(NOVEL_USB_FREQ_SET_CONTROL),
		NULL,
		0,
		&uRetBytes,
		NULL);
	
	bRet = (TRUE==rt) ? true :false;
#else
	{
		LOGTRACE(LOGINFO,"TvDevice::tune begin> >>>\n");
		AutoLockT lock(mutex_);
		int ret = ioctl(h_, RT10UP_TUNER_SET_FREQ, &param);
		bRet = (0 <= ret) ? true : false;
	}
#endif 
	LOGTRACE(LOGINFO,"TvDevice::tune(%d,%d,%d),bRet=%d.\n",freq,qam,symb,bRet);
	return bRet;
}

bool TvDevice::locked(long iTuner)
{
	bool bRet = false;
	
	long locked=0,strength=0,quality=0;
	bRet = getSignalStatus(locked,strength,quality);
	
	LOGTRACE(LOGINFO,"TvDevice::locked(%d).\n",locked);
	return bRet;
}

bool TvDevice::getSignalStatus(long& locked,long& strength,long& quality)
{
	bool bRet = false;
	int  iRet = 0;
	BYTE	szBuf[10]={0};
	
#if defined(WIN32)
	ULONG uBufferSize = 10;
	DWORD uRetBytes = 0;
	BOOL  rt = DeviceIoControl (
		h_,
		IOCTL_NOVELUSB_GET_TUNER_STATUS(DVBC_TUNER_1),
		NULL,
		0,
		szBuf,
		uBufferSize,
		(unsigned long *)&uRetBytes,
		NULL);
	if(rt && szBuf[0] !=0 ) 
	{
		bRet = true;
	}
#else
	{
		AutoLockT lock(mutex_);
		iRet = ioctl(h_, RT10UP_TUNER_GET_SIGNAL_STATUS, szBuf);
		bRet = (0 <= iRet && szBuf[0] != 0) ? true : false;
	}
#endif 
	
	if(bRet)
	{
		locked = (BYTE)szBuf[0];
		strength = (BYTE)szBuf[1];
		quality  = (BYTE)szBuf[2];
	}
	else{
		locked = 0;
		strength =0;
		quality =0;
	}
	
	LOGTRACE(LOGINFO,"TvDevice::getSignalStatus(%d,%d,%d),iRet=%d.\n",locked,strength,quality,iRet);
	return bRet;
}

bool TvDevice::smartcard_inserted()
{
	bool bRet = false;
	int  iRet = 0;
	BYTE  cStatus;
	
#if defined(WIN32)
	
	DWORD dwRetBytes = 0;
	BOOL rt = DeviceIoControl (
		h_,
		IOCTL_NOVELUSB_GET_SMARTCART_STATUS,
		NULL,
		0,
		&cStatus,
		sizeof(BYTE),
		&dwRetBytes,
		NULL);
	if(rt && 0 != cStatus) 
	{
		bRet = true;
	}
#else
	{
		AutoLockT lock(mutex_);
		iRet =ioctl(h_, RT10UP_CA_SC_GET_STATUS,&cStatus);
		if(0 <= iRet)
		{
			bRet = true;
		}
	}
#endif 

	LOGTRACE(LOGINFO,"TvDevice::smartcard_inserted(%d),iRet =%d.\n",bRet,iRet);
	return bRet;
}
bool TvDevice::smartcard_reset(OUT BYTE* pbyATR, OUT BYTE* pbyLen)
{
	bool bRet = false;
	int  iRet = 0;
	LOGTRACE(LOGINFO,"smartcard_reset.\n");
	
#if defined(WIN32)
	DWORD  nRetBytes = 0;
	DWORD  dwOutBufSize = *pbyLen;
	BOOL rt = DeviceIoControl (
		h_,
		IOCTL_NOVELUSB_RESET_SMARTCARD,
		NULL,
		0,
		pbyATR,
		dwOutBufSize,
		&nRetBytes,
		NULL);
	if(rt)
	{
		*pbyLen = (BYTE)nRetBytes;
		bRet = true;
	}

#else
	{
		AutoLockT lock(mutex_);
		CA_SCARD_ATR  scATR;
		scATR.nLen = *pbyLen;
		iRet =ioctl(h_, RT10UP_CA_SC_RESET,&scATR );
		if(0 <= iRet)
		{
			*pbyLen = scATR.nLen;
			memcpy(pbyATR,scATR.Data,scATR.nLen);
			bRet = true;
		}
	}

#endif 

	LOGTRACE(LOGINFO,"smartcard_reset(%p,%d),bRet=%d,iRet=%d.\n",pbyATR,*pbyLen,bRet,iRet);

	return bRet;
}
// 获取芯片ID
bool TvDevice::getStbID(char *pIDBuf,int iBufSize)
{
	bool bRet = false ;
	int  iRet = 0;
	
#if defined(WIN32)
	DWORD dwRetBytes = 0;
	BOOL b =  DeviceIoControl (
		h_,
		IOCTL_NOVELUSB_GetSTBID,
		NULL,
		0,
		pIDBuf,
		iBufSize,
		&dwRetBytes,
		NULL);
	bRet = b  ? true : false;
#else

	STB_ID stbId;
	iRet =ioctl(h_, RT10UP_CA_GET_STBID, &stbId);
	if(0 <= iRet)
	{
		memcpy(pIDBuf,	stbId.nPlatformID,2);
		memcpy(pIDBuf+2,stbId.nUniqueID,4);
		bRet = true;
	}
#endif 

	return bRet;
}

bool TvDevice::smartcard_getNumber(::std::string& cardNumber)
{
	bool bRet = true ;
	int  iRet = 0;

#if defined(WIN32)
	const DWORD BUF_SIZE=512;

	DWORD nRetBytes = 0;
	BYTE  szOutBuf[BUF_SIZE] = {0};
	BYTE  szInBuf[BUF_SIZE] = {0};

	BYTE CardCmd1[]={0x80, 0x46, 0x00, 0x00, 0x04, 0x01, 0x00, 0x00, 0x14};
	BYTE CardCmd2[]={0x00, 0xC0, 0x00, 0x00, 0x14};
	BOOL rt=FALSE;
	
	rt =  DeviceIoControl (
		h_,
		IOCTL_NOVELUSB_GET_SMARTCART_DATA,
		CardCmd1,
		sizeof(CardCmd1),
		szOutBuf,
		BUF_SIZE,
		&nRetBytes,
		NULL);
	if (!rt) 
	{
		return false;
	}
	
	rt =  DeviceIoControl (
		h_,
		IOCTL_NOVELUSB_GET_SMARTCART_DATA,
		CardCmd2,
		sizeof(CardCmd2),
		szOutBuf,
		BUF_SIZE,
		&nRetBytes,
		NULL);
	if (!rt) 
	{
		return false;
	}
	char buffer[32];
	memcpy(buffer,szOutBuf+4,16);
	buffer[16]=0;
	cardNumber=buffer;
#else
	{
		AutoLockT lock(mutex_);
		CA_SCARD_CMD ca_cmd;
		BYTE CardCmd2[]={0x00, 0xC0, 0x00, 0x00, 0x14};
		ca_cmd.nCommandLen = 5;
		memcpy(ca_cmd.Command,CardCmd2,ca_cmd.nCommandLen);
		iRet =ioctl(h_, RT10UP_CA_SC_TRANSMIT, &ca_cmd); 
		if(0 <= iRet)
		{
			char buffer[32]={0};
			memcpy(buffer,ca_cmd.Reply+4,16);
			cardNumber=buffer;
			bRet = true;
		}
	}
#endif 

	return bRet;
}

BYTE* TvDevice::getTsData()
{
	BYTE* pData = 0;

#if defined(WIN32)
	DWORD uRecByte = 0;
	BOOL rt = DeviceIoControl (
		h_,
		IOCTL_NOVELUSB_GET_TSDATA(DVBC_TUNER_1),
		NULL,
		0,
		buf_,
		TD_TSDATSZIE,
		&uRecByte,
		NULL);
	if(rt && uRecByte==TD_TSDATSZIE)
	{
		pData = buf_;
	}
#else

	memset(buf_,0,TD_TSDATSZIE);
	int ret = ioctl(h_,RT10UP_TUNER_GET_TSDATA,buf_);
	if(0 < ret)
	{
		pData =  buf_;
	}

#endif

	return pData;
}

bool TvDevice::setpid_control(const int* pids, int iCount)
{
	bool bRet = false;
	int  iRet = 0;
	
	NOVEL_USB_PID_SET_CONTROL oRequest;
	for ( int i = 0; i < iCount; i++ )
	{
		oRequest.pids_[i] = pids[i];
	}
	oRequest.num_ = iCount;

#if defined(WIN32)
	DWORD uRetBytes;
	BOOL  b = DeviceIoControl (
		h_,
		IOCTL_NOVELUSB_SET_PID(DVBC_TUNER_1),
		&oRequest,
		sizeof(NOVEL_USB_PID_SET_CONTROL),
		NULL,
		0,
		&uRetBytes,
		NULL);
	bRet = b ? true : false;

#else
	{
		AutoLockT lock(mutex_);
		iRet = ioctl(h_, RT10UP_TUNER_SET_PID, &oRequest);
		bRet = (0 <= iRet) ? true : false;
	}
#endif

	return bRet;
}

// 设置控制字
bool TvDevice::setCW(DVBCW *pDVBcw,int cwCount)
{
	bool bRet = false;
	int  iRet = 0;

	NOVEL_USB_CW_SET_CONTROL oRequest;
	for(int i = 0; i < cwCount ; i++)
	{
		// 绑定CW和PID
		memcpy( oRequest.cw_[i].oddkey_ , pDVBcw->oddKey  , 8 ); // 奇密钥,长度为8字节
		memcpy( oRequest.cw_[i].evenkey_, pDVBcw->eventKey, 8 ); // 偶密钥,长度为8字节
	}
	oRequest.num_ = cwCount;


#if defined(WIN32)
	
	DWORD uRetBytes;
	BOOL  b = DeviceIoControl (
		h_,
		IOCTL_NOVELUSB_SET_CW(DVBC_TUNER_1),
		&oRequest,
		sizeof(NOVEL_USB_CW_SET_CONTROL),
		NULL,
		0,
		&uRetBytes,
		NULL);
	bRet = b ? true : false;
#else
	iRet =ioctl(h_, RT10UP_CA_SC_SET_CW, &oRequest); 
	bRet = (0 <= iRet) ? true : false;
#endif 

	return bRet;
}

bool TvDevice::smartcard_transmit(const BYTE *pInput, int inputLen,BYTE *pOutput,int *outputLen)
{
	bool bRet = false;
	int  iRet = 0;

#ifdef TVDEVICE_DEBUG_PRINT
	LOGTRACE(LOGINFO,"===IN=== pInput=%s,len=%d.\n",PrintHex(pInput,inputLen),inputLen);
#endif 

#if defined(WIN32)

	DWORD dwRetBytes = 0;
	DWORD dwOutBufSize  = *outputLen;
	BOOL  b = DeviceIoControl (
		h_,
		IOCTL_NOVELUSB_GET_SMARTCART_DATA,
		(LPVOID)pInput,
		inputLen,
		pOutput,
		dwOutBufSize,
		&dwRetBytes,
		NULL);
	if(b)
	{
		*outputLen = dwRetBytes;
		bRet = true;
	}

#else
	{
		AutoLockT lock(mutex_);
		CA_SCARD_CMD ca_cmd;
		ca_cmd.nCommandLen = inputLen;
		memcpy(ca_cmd.Command,pInput,inputLen);

		iRet = ioctl(h_, RT10UP_CA_SC_TRANSMIT, &ca_cmd); 
	
		if(0 <= iRet)
		{
			bRet = true;
			*outputLen = ca_cmd.nReplyLen;
			memcpy(pOutput,ca_cmd.Reply,ca_cmd.nReplyLen);
		}

	}
#endif 

#ifdef TVDEVICE_DEBUG_PRINT
	LOGTRACE(LOGINFO,"===OUT===pOutput=%s,len=%d,bRet=%d,iRet=%d.\n",PrintHex(pOutput,*outputLen),*outputLen,bRet,iRet);
#endif 
	
	return bRet;
}

bool TvDevice::smartcard_funtion(const BYTE *pInput, int inputLen,BYTE *pOutput,int *outputLen)
{
	bool bRet = false;
	int  iRet = 0;

#ifdef TVDEVICE_DEBUG_PRINT
	LOGTRACE(LOGINFO,"[smartcard_funtion] pInput=%s,inputLen=%d.\n",PrintHex(pInput,inputLen),inputLen);
#endif 

#if defined(WIN32)
	DWORD dwRetBytes = 0;
	DWORD dwOutSize  = *outputLen;
	BOOL  b = DeviceIoControl (
		h_,
		IOCTL_NOVELUSB_SCFunction(DVBC_TUNER_1),
		(LPVOID)pInput,
		inputLen,
		pOutput,
		dwOutSize,
		&dwRetBytes,
		NULL);
	if(b)
	{
		*outputLen = dwRetBytes;
		bRet = true;
	}

#else
	{
		AutoLockT lock(mutex_);
		CA_SCARD_CMD ca_cmd;
		ca_cmd.nCommandLen = inputLen;
		memcpy(ca_cmd.Command,pInput,inputLen);
		iRet =ioctl(h_, RT10UP_CA_SC_FUNCTION, &ca_cmd); 
		if(0 <= iRet)
		{
			bRet = true;
			*outputLen = ca_cmd.nReplyLen;
			memcpy(pOutput,ca_cmd.Reply,ca_cmd.nReplyLen);
		}
	}
#endif 
	
#ifdef TVDEVICE_DEBUG_PRINT
	LOGTRACE(LOGINFO,"[smartcard_funtion] pOutput=%s,outputLen=%d,iRet=%d.\n",PrintHex(pOutput,*outputLen),*outputLen,iRet);
#endif 

	return bRet;
}

static char getHex(BYTE v)
{
	return (v&0xf)<=0x9 ? (v&0xf)+'0' : ((v&0xf)-0xa)+'A';
}

char* TvDevice::PrintHex(const BYTE *pBuf,USHORT BufSize)
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
