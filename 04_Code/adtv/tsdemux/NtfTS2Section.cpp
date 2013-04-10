#include "NtfTS2Section.h"
#include <string>

#define  LOG_TAG "libtsdemux"
#include "tvlog.h"

void TSInf_ResetSecData(TSInf *pTsInf,unsigned short pid)
{
    if(pTsInf) 
    {
        if(pTsInf->PidInf[pid].SecData)
            BkFree(pTsInf->SecAlloc,pTsInf->PidInf[pid].SecData);
        pTsInf->PidInf[pid].SecData=0;
        pTsInf->PidInf[pid].SecDataLen=0;
    }
}


unsigned short TSInf_AddSecData(TSInf *pTsInf,unsigned short pid,unsigned char *p,unsigned short len)
{
    if(pTsInf->PidInf[pid].SecDataLen+len>MAX_SECTION_LENGTH)
        return 0;
    memcpy(pTsInf->PidInf[pid].SecData+pTsInf->PidInf[pid].SecDataLen,p,len);
    pTsInf->PidInf[pid].SecDataLen+=len;
    return len;
}

void TSInf_Init(TSInf *pTsInf,void * PContext)
{
    if(pTsInf)
    {
        int i=0;
        for(i=0; i<MAX_PID_COUNT; i++)
        {
			pTsInf->PidInf[i].IsTsPid =0;
            pTsInf->PidInf[i].IsSecPid=0;
            pTsInf->PidInf[i].cc=-1;
            pTsInf->PidInf[i].SecData=0;
            pTsInf->PidInf[i].SecDataLen=0;
        }
        pTsInf->OnSecData=0;
        pTsInf->SecAlloc = BkInit(MAX_SECTION_COUNT,MAX_SECTION_LENGTH);
        pTsInf->Context=PContext;
    }
}

void TSInf_Uninit(TSInf *pTsInf)
{
    if(pTsInf)
        BkUninit(pTsInf->SecAlloc);
}

void TSInf_Reset(TSInf *pTsInf)
{
    if(pTsInf)
    {
        int i=0;
        for(i=0; i<MAX_PID_COUNT; i++)
        {
            if(pTsInf->PidInf[i].SecData)
                BkFree(pTsInf->SecAlloc,pTsInf->PidInf[i].SecData);
            
			pTsInf->PidInf[i].IsTsPid =0;
			pTsInf->PidInf[i].IsSecPid=0;
            pTsInf->PidInf[i].cc=-1;
            pTsInf->PidInf[i].SecData=0;
            pTsInf->PidInf[i].SecDataLen=0;
        }
    }
}

void TSInf_ClearAllPrivateSecPid(TSInf *pTsInf)
{
    if(pTsInf)
    {
        unsigned short i=0;
        for(i=0; i<MAX_PID_COUNT; i++) {
            if(pTsInf->PidInf[i].SecData)
                BkFree(pTsInf->SecAlloc,pTsInf->PidInf[i].SecData);

	    pTsInf->PidInf[i].IsTsPid =0;
            pTsInf->PidInf[i].IsSecPid=0;
            pTsInf->PidInf[i].cc=-1;
            pTsInf->PidInf[i].SecData=0;
            pTsInf->PidInf[i].SecDataLen=0;
        }
    }
}

void TSInf_ClearPrivateSecPid(TSInf *pTsInf,unsigned short pid)
{
    if(pTsInf && pid < MAX_PID_COUNT)
    {
        if(pTsInf->PidInf[pid].SecData)
            BkFree(pTsInf->SecAlloc,pTsInf->PidInf[pid].SecData);

	pTsInf->PidInf[pid].IsTsPid =0;
        pTsInf->PidInf[pid].IsSecPid=0;
        pTsInf->PidInf[pid].cc=-1;
        pTsInf->PidInf[pid].SecData=0;
        pTsInf->PidInf[pid].SecDataLen=0;
    }
}


void TSInf_SetSecDataHandler(TSInf *pTsInf,SecDataHandler h)
{
    if(pTsInf)
        pTsInf->OnSecData=h;
}
void TSInf_SetTSDataHandler(TSInf *pTsInf,TSDataHandler h)
{
	if(pTsInf)
		pTsInf->OnTSData=h;
}

#ifdef TS_COUNTER_PRINT
struct PIDCounter{
	U16 pid;
	S8  cc;
	U8  counter;
};
#include <map>
typedef std::map<U16,PIDCounter> mapPIDCounterT;
mapPIDCounterT pidcounter_;
#endif 

void TSInf_PutTsPactet(TSInf *pTsInf,unsigned char *pk,unsigned long pkCount)
{
    unsigned char transport_error_indicator=1;
    unsigned char payload_unit_start_indicator=1;
    unsigned char continuity_counter=0;
    unsigned char adaptation_field_control=0;
    unsigned char adapflags=0;
    unsigned short payload_old_start_at=0;
    unsigned short payload_new_start_at=0;
    unsigned short pid=0;
    unsigned char *p=0;
    unsigned char *p1=0;
    unsigned long i=0;
    //unsigned long i1=0;
    unsigned short PayloadLen=0;
    unsigned short len=0;
    unsigned short used=0;
    unsigned short SecLen=0;
    PIDInf *pPidInf=0;

	if(!pTsInf)
	{
		return;
	}

	for(i=0; (i<pkCount) ; i++)
	{
		p=pk+i*TS_PACKET_SIZE;

		if(0x47 != p[0])
		{
			continue;
		}

		pid = getTSInt13(p+1);
		if(MAX_PID_COUNT <= pid)
		{
			continue;
		}

#ifdef TS_COUNTER_PRINT
		
		PIDCounter temp={0,-1,0};
		pidcounter_.insert(mapPIDCounterT::value_type(pid,temp));
		mapPIDCounterT::iterator  it = pidcounter_.find(pid);
		if(it != pidcounter_.end())
		{
			bool bContinue = false;
			U8 adaptation_field_control = ( p[3] & 0x30 ) >> 4;
			U8 uPreCountAdd1 = (it->second.cc+1) & 0xf;
			it->second.counter = ( p[3] & 0x0f );

			
			if(0 == adaptation_field_control || 2 == adaptation_field_control)
				bContinue = true;
			if (it->second.counter == it->second.cc)
				bContinue = true;
			else if (it->second.counter== uPreCountAdd1)
				bContinue = true;
			else if ((0x0 == it->second.counter) && (0xf == it->second.cc))
				bContinue = true;
			else 
				bContinue = false;
			if(!bContinue)
			{
				U8 uLostPacket = (it->second.counter - uPreCountAdd1)& 0xf;
				LOGTRACE(LOGINFO,"[pid=%d],cc discontinuity,lost packet=%d.\n",pid,uLostPacket);
			}
			
			it->second.cc = it->second.counter;
		}
		continue;
#endif 

		pPidInf = &pTsInf->PidInf[pid];

		// TS ·Ö×é°üPID
		if(0 < pPidInf->IsTsPid)
		{
			pTsInf->OnTSData(pTsInf->Context,pid,p,188);
			continue;
		}

		transport_error_indicator    = ( p[1] & 0x80 ) ? 1 : 0;
		payload_unit_start_indicator = ( p[1] & 0x40 ) ? 1 : 0;
		adaptation_field_control     = ( p[3] & 0x30 ) >> 4;
		continuity_counter			 = ( p[3] & 0x0F );
		
     	payload_old_start_at = (adaptation_field_control & 0x2)!=0 ? (4+p[4]+1) : 4;
		payload_new_start_at = payload_old_start_at;

		if(payload_unit_start_indicator && payload_old_start_at<=TS_PACKET_SIZE )
		{
			payload_new_start_at = payload_old_start_at+p[payload_old_start_at]+1;
			payload_old_start_at+=1;
		}
		
		// does the packet contain an adaptation field ?
		if( (adaptation_field_control & 0x2) != 0 && p[4]>0)
		{
			adapflags=p[5];
		}

		// check continue counter
        if(pPidInf->cc!=-1 
            && !(adapflags & 0x80/*transport_adaptation_flag_discontinuity*/) )
        {
            if( ((pPidInf->cc+1)&0xf) != continuity_counter 
                && (adaptation_field_control&0x1)!=0 )
            {
                TSInf_ResetSecData(pTsInf,pid);
            }
        }
        pPidInf->cc=continuity_counter;

        // check 
        if( transport_error_indicator
            || payload_new_start_at >= TS_PACKET_SIZE
            || payload_old_start_at>=TS_PACKET_SIZE )
            continue;

        // ts packet is valid.
        PayloadLen = TS_PACKET_SIZE-payload_old_start_at;
        
		// Is the pid a section pid
        if(0 >= pPidInf->IsSecPid)
		{
			continue;
		}
    
		p1 = p+payload_old_start_at;
        
		if(0 == pPidInf->SecData)
		{
			pPidInf->SecData=BkAlloc(pTsInf->SecAlloc);
			if(0 == pPidInf->SecData)
			{
				continue ; 
			}
		}
        
		// Process section data
        if(payload_unit_start_indicator)
        {
            len = payload_new_start_at-payload_old_start_at;
            if(pPidInf->SecDataLen!=0)
            {
                if(pPidInf->SecDataLen+len<SEC_HEADER_SIZE)
                    TSInf_ResetSecData(pTsInf,pid);
                else
                {
                    if(pPidInf->SecDataLen<SEC_HEADER_SIZE)
                    {
                        used=TSInf_AddSecData(pTsInf,pid,p1,
                            SEC_HEADER_SIZE-pPidInf->SecDataLen);
                        p1+=used;
                        len-=used;
                    }
                    SecLen = SEC_HEADER_SIZE+ 
                        (((pPidInf->SecData[1]&0x0f)<<8) | pPidInf->SecData[2]);
                    if(SecLen>pPidInf->SecDataLen+len)
					{
						TSInf_ResetSecData(pTsInf,pid);
					}
                    else
                    {
                        used=TSInf_AddSecData(pTsInf,pid,p1,SecLen-pPidInf->SecDataLen);
                        if(pTsInf->OnSecData)
                        {
							//LOGTRACE(LOGINFO,"OnSecData(pid=%u,SecData=%p,SecDataLen=%u).\n",
							//	pid,pPidInf->SecData,pPidInf->SecDataLen);
                            pTsInf->OnSecData(pTsInf->Context,pid,pPidInf->SecData,pPidInf->SecDataLen);
                        }
                        TSInf_ResetSecData(pTsInf,pid);
                    }
                }
            }
            p1=p+payload_new_start_at;
            PayloadLen=TS_PACKET_SIZE-payload_new_start_at;
        }
        while(PayloadLen>0)
        {
            if(pPidInf->SecDataLen==0 && p1[0]==0xff )
                break;

			if(0 == pPidInf->SecData)
			{
				pPidInf->SecData=BkAlloc(pTsInf->SecAlloc);
				if(0 == pPidInf->SecData)
					break;
			}
         
			if(pPidInf->SecDataLen+PayloadLen<SEC_HEADER_SIZE)
            {
                TSInf_AddSecData(pTsInf,pid,p1,PayloadLen);
                p1+=PayloadLen;
                PayloadLen-=PayloadLen;
            }
            else
            {
                len=0;
                if(pPidInf->SecDataLen<SEC_HEADER_SIZE)
                    len = SEC_HEADER_SIZE-pPidInf->SecDataLen;
                TSInf_AddSecData(pTsInf,pid,p1,len);
                p1+=len;
                PayloadLen-=len;

                SecLen = SEC_HEADER_SIZE+ 
                    (((pPidInf->SecData[1]&0x0f)<<8) | pPidInf->SecData[2]);
                if(SecLen>pPidInf->SecDataLen+PayloadLen)
                    len = PayloadLen;
                else
                    len = SecLen-pPidInf->SecDataLen;
                TSInf_AddSecData(pTsInf,pid,p1,len);
                p1+=len;
                PayloadLen-=len;
                if(pPidInf->SecDataLen==SecLen)
                {
                    if(pTsInf->OnSecData) 
                    {
						//LOGTRACE(LOGINFO,"OnSecData(pid=%u,SecData=%p,SecDataLen=%u).\n",
						//	pid,pPidInf->SecData,pPidInf->SecDataLen);
                        pTsInf->OnSecData(pTsInf->Context,pid,pPidInf->SecData,pPidInf->SecDataLen);
                    }
                    TSInf_ResetSecData(pTsInf,pid);
                }
            }
        }
    }

}
