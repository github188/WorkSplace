#ifndef TVDEVICE_H_
#define TVDEVICE_H_

#include "tvcomm.h"
#include "xprocess.h"
#include <string>

#if defined(WIN32)
typedef HANDLE HANDLE_T;
#else
typedef int    HANDLE_T;
#endif 

#define TD_TSDATSZIE	(188*496*2)

class TvDevice
{
public:
	TvDevice();
	~TvDevice();
	//static TvDevice* getDevice();
	//static void cleanDevice();

	bool open();
	void close();
	HANDLE_T handle() { return h_; }
	bool isopen();
	bool tune(long iTuner,long freq,long qam,long symb);
	bool locked(long iTuner);
	bool getSignalStatus(long& locked,long& strong,long& quality);
	bool setpid_control(const int* pids, int iCount);
	bool smartcard_inserted();
	bool smartcard_reset(OUT BYTE* pbyATR, OUT BYTE* pbyLen);
	bool smartcard_getNumber(::std::string & cardNumber);
	// 读取机顶盒唯一编号
	bool getStbID(char *pIDBuf,int iBufSize);
	// 设置控制字
	bool setCW(DVBCW *pDVBcw,int cwCount);
	bool smartcard_transmit(const BYTE *pInput, int inputLen,BYTE *pOutput,int *outputLen);
	// 安全芯片接口
	bool smartcard_funtion(const BYTE *pInput, int inputLen,BYTE *pOutput,int *outputLen);

	BYTE *getTsData();
	long getTsDataSize(){ return TD_TSDATSZIE; }
	char *PrintHex(const BYTE *pBuf,USHORT BufSize);

private:
	//TvDevice();
	//~TvDevice();
	//static TvDevice* m_pDevice;

	HANDLE_T h_;
	MutexT   mutex_;
	unsigned char buf_[TD_TSDATSZIE];
	
};

#endif //defined(TVDEVICE_H_)