 
//CircleBuf.cpp   
#include "ringbuffer.h"   
//#include <new>   
#include <string.h>
 
#include <android/log.h>
namespace android {

uint32 CCircleBuf::BUFSIZE = 188 * 1024 * 400;  
//int CCircleBuf::READSIZE = 188;  
//int CCircleBuf::WRITESIZE = 188*496*2;  

CCircleBuf::CCircleBuf():circle_mutex(FALSE)
{  
	m_pucBuf = new unsigned char[BUFSIZE];  
	memset(m_pucBuf,0,BUFSIZE);
	m_iTail=0;  
	m_iHead=0;  
	LOGI("CCircleBuf::CCircleBuf() Creat");

}  
void CCircleBuf::ClrBuffer(void)
{
	circle_mutex.lock();
	memset(m_pucBuf,0,BUFSIZE);
	m_iTail = m_iHead = 0;
	circle_mutex.unlock();
}
CCircleBuf::~CCircleBuf(void)  
{  
	if(m_pucBuf !=NULL)
	{
		delete[] m_pucBuf;  
		m_pucBuf = NULL;
	}		
}  


bool CCircleBuf::WriteDataEx(unsigned char* buf, uint32 size)  
{  
	uint32 iNext = (1 + m_iTail) % BUFSIZE;  
	if (m_iHead == iNext)  
	{  
		return false;  
	}  
	else  
	{  
		uint32 iAvail = 0;  
		uint32 iAvailTail = 0;  
		iAvail = BUFSIZE - 1 - ((m_iTail - m_iHead + BUFSIZE) % BUFSIZE);  
		if (iAvail < size)  
		{  
			return false;  
		}  
		else  
		{  
			circle_mutex.lock();	// project buffer 
			if (m_iHead <= m_iTail)  
			{  
				iAvailTail = BUFSIZE - m_iTail;  

				if (iAvailTail < size)  
				{  

					memcpy(m_pucBuf + m_iTail, buf, iAvailTail);  
					memcpy(m_pucBuf, buf + iAvailTail, size - iAvailTail);  

					m_iTail = size - iAvailTail;  
				}  
				else  
				{  
					memcpy(m_pucBuf + m_iTail, buf, size);  
					m_iTail += size;  
				}  

			}  
			else  
			{  
				memcpy(m_pucBuf + m_iTail, buf, size);  
				m_iTail += size;  
			}  
			circle_mutex.unlock();  
			return true;  
		}  
	}  
}  
bool CCircleBuf::ReadDataEx(unsigned char* buf, uint32 size)  
{  
	if (m_iHead == m_iTail)  
	{  
		return false;  
	}  
	else  
	{  
		uint32 iAvail = 0;  
		uint32 iAvailTail = 0;  
		iAvail = (m_iTail - m_iHead + BUFSIZE) % BUFSIZE;  
		if (iAvail < size)  
		{  
			return false;  
		}  
		else  
		{  
			circle_mutex.lock();  
			if (m_iHead < m_iTail)  
			{  
				memcpy(buf, m_pucBuf + m_iHead, size);  
				m_iHead += size;  
			}  
			else  
			{  
				iAvailTail = BUFSIZE - m_iHead;  
				if (iAvailTail < size)  
				{  
					memcpy(buf, m_pucBuf + m_iHead, iAvailTail);  
					memcpy(buf + iAvailTail, m_pucBuf, size -iAvailTail);  
					m_iHead = size - iAvailTail;  
				}  	
				else  
				{  
					memcpy(buf, m_pucBuf + m_iHead, size);  
					m_iHead += size;  
				}  
			}  
			circle_mutex.unlock();  
			return true;  
		}  
	}  
}  
#if 0
#include <iostream>   

#include <iomanip>
using std::cout;
int main(void)
{
	CCircleBuf *buffer = new CCircleBuf();
	unsigned char *buf = new unsigned char[12];
	unsigned char *buf2 = new unsigned char[12];
	memcpy(buf,"abcdefghjkla",11);
	
	//buffer[11] = '\0';
	buffer->WriteDataEx(buf,12);
	buffer->ReadDataEx(buf2, 12);
	//std::cout<<" ";
	return 1;
}
#endif



}
