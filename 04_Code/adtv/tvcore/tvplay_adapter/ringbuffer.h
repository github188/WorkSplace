//CircleBuf.h   

#include <linux/kernel.h>
#include <linux/ioctl.h>
#include <utils/Log.h>
#include <utils/threads.h>
/*open*/
#include <fcntl.h>
#include <sys/stat.h> 
#include <sys/types.h>
#include <dvb_types.h>

#pragma once   
namespace android {


class CCircleBuf//:public CCircleBufAbstract
{  
public:  
	CCircleBuf();  
	
	
public:  
	~CCircleBuf(void);  
	void ClrBuffer(void);
	bool WriteDataEx(unsigned char* buf, uint32 size /*= WRITESIZE*/);  
	bool ReadDataEx(unsigned char* buf, uint32 size/* = READSIZE*/);  

public:  

	static uint32 BUFSIZE;  
//	static int READSIZE; 
//	static int WRITESIZE;
private:
	uint8* m_pucBuf;  
	uint32 m_iHead;  
	uint32 m_iTail;  
	Mutex circle_mutex;
}; 
}
