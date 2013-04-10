#ifndef 	UDPCLIENT_H_
#define 	UDPCLIENT_H_

#include "tvcomm.h"
#include <sys/socket.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <strings.h>
#include <errno.h>
#include <fcntl.h>

#define UDP_SVR_ADDR  "192.168.11.17"
#define UDP_SVR_PORT  (8000)

#define  LOG_TAG    "libtvplayer"
#include "tvlog.h"

struct UDPClient{
public:
	UDPClient(const char* addr =UDP_SVR_ADDR, int iPort=UDP_SVR_PORT)
	{
		m_iSockfd = socket(AF_INET,SOCK_DGRAM,IPPROTO_UDP);
		if(0> m_iSockfd)
		{
			LOGTRACE(LOGINFO,"create udp socket failed errno=%d.\n",errno);
			return;		
		}


		// server  side information
		bzero(&m_stSvrAddr,sizeof(struct sockaddr_in));
		m_stSvrAddr.sin_family = AF_INET;
		m_stSvrAddr.sin_addr.s_addr =inet_addr(addr);
		m_stSvrAddr.sin_port = htons(iPort);
			
	}
	~UDPClient(){
		if(0 < m_iSockfd)
			close(m_iSockfd);
	}

	bool push(unsigned char* pData,long lSize){
		bool bRet = false;
		if(0 <  m_iSockfd)
		{
			int iRet =  sendto(m_iSockfd,pData,lSize,0,(struct sockaddr*)&m_stSvrAddr,sizeof(m_stSvrAddr));
			//LOGTRACE(LOGINFO,"UDPClient::push iRet = %d.\n",iRet);
			bRet = (-1 == iRet) ? false: true;	
			
		}
		else
		{
			LOGTRACE(LOGINFO,"m_iSockfd is invalid.\n");
		}
		
		return bRet;
	}
private:
	
	struct sockaddr_in m_stSvrAddr;
	int m_iSockfd;
};

#endif // defined(UDPCLIENT_H_)
