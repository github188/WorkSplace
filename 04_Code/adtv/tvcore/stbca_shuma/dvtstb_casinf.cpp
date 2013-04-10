
#include "capture_def.h"
#include "typ.h"
#include "../stbca/stbca.h"
#include "DVTSTB_CASInf.h"
#include <stbca_utility.h>
#include "cdcalib4stb.h"
#include "utils/DataObject.h"
#include "utils/xinlin.hh"

#include <DVTCAS_STBInf.h>

#define LOG_LEVEL LOG_TRACE
#define LOG_TAG "stbca_dvt"
#include <dxreport.h>

//ģ������CA��flash�ڴ���׵�ַ
//flash�ڴ��С,�ĵ�Ҫ���ṩ32KB��С,NOVEL �ṩ��С 125K
#define FLASHBUF_ADDRBASE (0x1000)

extern ObjStbCa gStbCa;

//����NIT��������
extern int gPDSD;
//���뵥Ƶ����������
extern bool gAreaLockOK;

#ifdef JS_USE_SHUMA_CALIB_NORMAL
bool g_bEcmProcessing = false;
#endif
//���ļ�����Ҫ�������ʵ�ֲ��ṩ��CAģ��ʹ�õĽӿڡ�����HRESULT����ֵΪDVTCA_OK����ʾ�ɹ�������ֵ��ʾʧ�ܡ�
/*----------------------------------���½ӿ���STB�ṩ��CAS����--------------------------------------------*/
HRESULT DVTSTBCA_SCReset(void)
{
	BYTE pbyATR[sizeof(BYTE)];
	BYTE byLen;
	UINT8 ret = CDSTBCA_SCReset(pbyATR, &byLen);
	//����λͨ�ųɹ�
	if(ret > 0)
		return DVTCA_OK;

	//û�����ĵ����ҵ����ʵķ���ֵ,��UNKNOWN��
	return DVTCAERR_STB_UNKNOWN;
}


HRESULT DVTSTBCA_GetDataBufferAddr(long * lSize, char ** ppStartAddr)
{
	//�ļ���CAģ������ʱ�Ѵ���
	*lSize = (long)FLASH_FILE_SIZE;//0x128000
	*ppStartAddr = (char*)FLASHBUF_ADDRBASE;
	return DVTCA_OK;
}

HRESULT DVTSTBCA_ReadDataBuffer(const char * pStartAddr, long * plDataLen, BYTE * pData)
{
	dxreport( "Enter DVTSTBCA_ReadBuffer  %p %d\n", pStartAddr, *plDataLen);

	if (NULL == pStartAddr || NULL == plDataLen || NULL == pData)
		return DVTCAERR_STB_POINTER_INVALID;//�ж�ָ��ĺϷ���

	// start addr out of range?
	if (pStartAddr < (char*)FLASHBUF_ADDRBASE || pStartAddr > (char*)(FLASHBUF_ADDRBASE + FLASH_FILE_SIZE))
		return DVTCAERR_STB_POINTER_INVALID;

	// end addr out of range?
	if ((pStartAddr + *plDataLen) > (char*)(FLASHBUF_ADDRBASE + FLASH_FILE_SIZE))
		return DVTCAERR_STB_DATA_LEN_ERROR;

	//�ڴ�ƫ��
	long offset =  pStartAddr - (char*)FLASHBUF_ADDRBASE;	
	
	FILE * fp = fopen(gStbCa.fileName_,"rb");
	if(!fp)
	{
		dxreport( "DVTSTBCA_ReadBuffer  Error open File %s\n", gStbCa.fileName_);
		return DVTCAERR_STB_UNKNOWN;
	}
	
	int seekRet = fseek(fp, offset, SEEK_SET);
	
	size_t size=fread(pData, 1, *plDataLen, fp);
	//dxreport( "DVTSTBCA_ReadBuffer  offset %d, %d,%d \n", offset, seekRet, size);
	fclose(fp);
	if (!size || size != (size_t)(*plDataLen)) {
		dxreport("CDSTBCA_ReadBuffer Error Read File %s\n",gStbCa.fileName_);
		return DVTCAERR_STB_UNKNOWN;
	}

	dxreport( "Leave DVTSTBCA_ReadBuffer \n");
	return DVTCA_OK;
}

HRESULT DVTSTBCA_WriteDataBuffer(const char * pStartAddr, long * plDataLen, const BYTE * pData)
{
	if (NULL == pStartAddr || NULL == plDataLen || NULL == pData)
		return DVTCAERR_STB_POINTER_INVALID;

	// start addr out of range?
	if (pStartAddr < (char*)FLASHBUF_ADDRBASE || pStartAddr > (char*)(FLASHBUF_ADDRBASE + FLASH_FILE_SIZE))
		return DVTCAERR_STB_POINTER_INVALID;

	// end addr out of range?
	if ((pStartAddr + *plDataLen) > (char*)(FLASHBUF_ADDRBASE + FLASH_FILE_SIZE))
		return DVTCAERR_STB_DATA_LEN_ERROR;
	
	FILE *fp=fopen(gStbCa.fileName_,"rb+");
	if(!fp)
	{
		dxreport( "DVTSTBCA_WriteBuffer  Error open File %s\n", gStbCa.fileName_);
		return DVTCAERR_STB_UNKNOWN;
	}

	//�ڴ�ƫ��
	long offset =  pStartAddr - (char*)FLASHBUF_ADDRBASE;	
	int of = fseek(fp, offset, SEEK_SET);

	size_t size1=fwrite(pData, 1, *plDataLen, fp);
	int cloRet = fclose(fp);
//	dxreport("size1 is %d seek ret %d fclose %d\n",size1, of, cloRet);
	if(size1 != (size_t)(*plDataLen))
	{
		dxreport("DVTSTBCA_WriteBuffer Error Write File %s size %d,request %d\n",gStbCa.fileName_,size1, *plDataLen);
		return DVTCAERR_STB_UNKNOWN;
	}

	return DVTCA_OK;
}

//eeprom �洢���Բ�ʵ��,��Ӱ��ʹ��,��ʵ��ʱ���뷵��-1,�ο��ĵ�
HRESULT DVTSTBCA_GetDataFromEeprom(long * plDataLen, BYTE * pData)
{
	return (HRESULT)-1;
}
HRESULT DVTSTBCA_SaveDataToEeprom(long * plDataLen, const BYTE * pData)
{
	return (HRESULT)-1;
}

//�����Ǻ���,�ĵ�����˵,,,�����������
HRESULT DVTSTBCA_Sleep( int dwMicroseconds )
{
	//CDSTBCA����Ϊunsigned short,����Ϊint;
	//CDSTBCA_Sleep(dwMicroseconds);
	//ֱ�ӵ���,unsigned long
	NS_sleep(dwMicroseconds);
	return DVTCA_OK;
}
HRESULT DVTSTBCA_SemaphoreInit( DVTCA_Semaphore * pSemaphore )
{
	CDSTBCA_SemaphoreInit((CDCA_Semaphore*)pSemaphore, true);
	dxreport("DVTSTBCA_SemaphoreInit Semaphore:%d\n", *pSemaphore);
	return DVTCA_OK;
}
HRESULT DVTSTBCA_SemaphoreSignal( DVTCA_Semaphore * pSemaphore )
{
//	dxreport("DVTSTBCA_SemaphoreSignal Semaphore:%d\n", *pSemaphore);
	CDSTBCA_SemaphoreSignal((CDCA_Semaphore*)pSemaphore);
	return DVTCA_OK;
}
HRESULT DVTSTBCA_SemaphoreWait( DVTCA_Semaphore * pSemaphore )
{
//	dxreport("DVTSTBCA_SemaphoreWait Semaphore:%d\n", *pSemaphore);
	CDSTBCA_SemaphoreWait((CDCA_Semaphore*)pSemaphore);
	return DVTCA_OK;
}
HRESULT DVTSTBCA_RegisterTask(const char * szName, pThreadFunc pTaskFun)
{
	CDCA_U8 priority = 0; //no use
	void * param = NULL;
	CDCA_U16 stackSize = 1024; //no use
	HANDLE ret = CDSTBCA_RegisterTask(szName,priority, (void*)pTaskFun, param,stackSize);
	if(ret > 0)
		return DVTCA_OK;

	return DVTCAERR_STB_UNKNOWN;
}
HRESULT DVTSTBCA_SetStreamGuardFilter(BYTE byReqID, WORD wPID, const BYTE * szFilter, const BYTE * szMask, BYTE byLen, int nWaitSeconds)
{
	BYTE testFilter[8] = {0x80,0x0,0x0,0x0,0x0,0x0,0x0,0x0};
	BYTE testMask[8] = {0xF0,0x0,0x0,0x0,0x0,0x0,0x0,0x0};
	testFilter[0]=szFilter[0];
	testMask[0]=szMask[0];
	for(int i=1;i<6;i++)
	{
		testFilter[i]=szFilter[i+2];
		testMask[i]=szMask[i+2];
	}
	
	// shuma ���������յ����ݺ�ᱻɾ��
	if(byReqID == 1) byReqID |= 0x80;  // reqid = 1 Ϊecm ����������ecm ������, ������emm ������
	// ecm һ���Ż��Ŀ����ǣ����β�ͬ���������õ�ecm ��Ϣ�� Ŀǰδʹ��
	UINT8 ret = CDSTBCA_SetPrivateDataFilter(byReqID , (BYTE *)&testFilter, (BYTE *)&testMask, byLen, wPID, nWaitSeconds);
	dxreport("DVTSTBCA_SetStreamGuardFilter ret:%d\n",ret);
	if(ret == true)
		return DVTCA_OK;
	return DVTCAERR_STB_UNKNOWN;
}

HRESULT DVTSTBCA_SetDescrCW(WORD wEcmPID, BYTE byKeyLen, const BYTE * szOddKey, const BYTE * szEvenKey, bool bTapingControl)
{
	dxreport("DVTSTBCA_SetDescrCW begin >>>\n");
	CDSTBCA_ScrSetCW(wEcmPID, szOddKey, szEvenKey, byKeyLen, bTapingControl);
	dxreport("DVTSTBCA_SetDescrCW end <<<\n");
	return DVTCA_OK;
}

void DVTSTBCA_AddDebugMsg(const char * pszMsg)
{
	//level ��д��1
	char buf[1024];
	// ȥ��\r,\n �ַ�����Ϊ��ӡ�����롣
	int i, j=0;
	for(i=0; i<1024; i++)
	{
		if(pszMsg[i] == 0) break;
		if(pszMsg[i] != 0xa && pszMsg[i] != 0xd)
		{
			buf[j++] = pszMsg[i];
		}
		else
		{
			buf[j] = 0;
			if(j==0) continue;
			CDSTBCA_Printf(1, buf);
			j=0;
		}
	}
	
	buf[j] = 0;
	CDSTBCA_Printf(1, buf);
}

HRESULT DVTSTBCA_SCAPDU(BYTE byReaderNo, BYTE * pbyLen, const BYTE * byszCommand, BYTE * byszReply)
{
	WORD replyLen = 0;
	HRESULT hRes;
#ifdef JS_USE_SHUMA_CALIB_NORMAL
//	if(*pbyLen == 121 || *pbyLen == 89)  // ��Чecm �����ݳ���, ��Чecm ���ݳ��ȻƸ�
//  ���command12, command5 ����
	{
		static U8 cmpCmd[]={0x80, 0x32, 0x0, 0x0};
		if(memcmp(byszCommand, cmpCmd, 4)==0)
		g_bEcmProcessing = true;
	}
#endif
	
	UINT8 ret = CDSTBCA_SCPBRun( byszCommand, (WORD)(0x00FF & (*pbyLen)), byszReply, &replyLen);
	*pbyLen = replyLen;
	if(ret == true)
	{
		hRes = DVTCA_OK;
	}
	
	else //(ret == false) // Ϊͨ��shuma ���γ�ָ��
	{
//		CheckSCardStatus();
//		if(gStbCa.bSCardStatus_==false) // ���Ѱγ�
		{
//			dxreport("card has removed!!!\n");
//			char rep[2]={0x90,0x0};
//			memcpy(byszReply,rep,2);
//			*byszReply = 0;
//			*pbyLen = 2;
//			*pbyLen = 0;
		}
//		hRes=DVTCAERR_STB_UNKNOWN;
		dxreport("DVTSTBCA_SCAPDU ----communicate failed\n");
		hRes=DVTCAERR_STB_CARD_INVALID;
//		hRes=DVTCA_OK;
	}
#ifdef JS_USE_SHUMA_CALIB_NORMAL
		if(replyLen == 5)	// ĩβecm ����
		{
			static U8 cmpData[]={0x0,0x0,0x9,0x90,0x0};
			if(memcmp(byszReply, cmpData, 5)==0)
			{
				g_bEcmProcessing = false;
			}
		}
#endif

	dxreport("DVTSTBCA_SCAPDU hRes: %d, replylen:%d\n", hRes, *pbyLen);
	
	return hRes;
}

void DVTSTBCA_ShowPromptMessage(BYTE byMesageNo)
{
	dxreport("DVTSTBCA_ShowPromptMessage begin >>> MessageNo:%d\n", byMesageNo);

	//������ڽ�����������,�򲻴�����Ϣ
	if(gStbCa.ignoreCAMsg)
		return;

	//����CA�������CDCA_MESSAGE_CANCEL_TYPE,��������ʹֵ
	BYTE cdcaMsg = CDCA_MESSAGE_CANCEL_TYPE;
	switch(byMesageNo)
	{
		case DVTCA_RATING_TOO_LOW:
			cdcaMsg = CDCA_MESSAGE_DVTCA_RATING_TOO_LOW;
			break;
		case DVTCA_NOT_IN_WATCH_TIME:
			cdcaMsg = CDCA_MESSAGE_DVTCA_NOT_IN_WATCH_TIME;
			break;
		case DVTCA_NOT_PAIRED:
			cdcaMsg = CDCA_MESSAGE_DVTCA_NOT_PAIRED;
			break;
		case DVTCA_PLEASE_INSERT_CARD:
			cdcaMsg = CDCA_MESSAGE_DVTCA_PLEASE_INSERT_CARD;
			break;
		case DVTCA_NO_ENTITLE:
			cdcaMsg = CDCA_MESSAGE_DVTCA_NO_ENTITLE;
			break;
		case DVTCA_PRODUCT_RESTRICT:
			cdcaMsg = CDCA_MESSAGE_DVTCA_PRODUCT_RESTRICT;
			break;
		case DVTCA_AREA_RESTRICT:
			cdcaMsg = CDCA_MESSAGE_DVTCA_AREA_RESTRICT;
			break;
		case DVTCA_MOTHER_RESTRICT:
			gStbCa.caThread_.stop(); // ֹͣԭCA �߳�
			gStbCa.SonMotherThread_.start(SonMotherWorkProc,&gStbCa.SonMotherThread_);
			cdcaMsg = CDCA_MESSAGE_DVTCA_MOTHER_RESTRICT;
			break;
		case DVTCA_NO_MONEY:
			cdcaMsg = CDCA_MESSAGE_DVTCA_NO_MONEY;
			break;
		case DVTCA_IPPV_NO_CONFIRM:
			cdcaMsg = CDCA_MESSAGE_DVTCA_IPPV_NO_CONFIRM;
			break;
		case DVTCA_IPPV_NO_BOOK:
			cdcaMsg = CDCA_MESSAGE_DVTCA_IPPV_NO_BOOK;
			break;
		case DVTCA_IPPT_NO_CONFIRM:
			cdcaMsg = CDCA_MESSAGE_DVTCA_IPPT_NO_CONFIRM;
			break;
		case DVTCA_IPPT_NO_BOOK:
			cdcaMsg = CDCA_MESSAGE_DVTCA_IPPT_NO_BOOK;
			break;
		case DVTCA_DATA_INVALID:
			cdcaMsg = CDCA_MESSAGE_DVTCA_DATA_INVALID;
			break;
		case DVTCA_SC_NOT_SERVER:
			cdcaMsg = CDCA_MESSAGE_DVTCA_SC_NOT_SERVER;
			break;
		case DVTCA_KEY_NOT_FOUND:
			cdcaMsg = CDCA_MESSAGE_DVTCA_KEY_NOT_FOUND;
			break;
		case DVTCA_IPPNEED_CALLBACK:
			cdcaMsg = CDCA_MESSAGE_DVTCA_IPPNEED_CALLBACK;
			break;
		case DVTCA_FREE_PREVIEWING:
			cdcaMsg = CDCA_MESSAGE_DVTCA_FREE_PREVIEWING;
			break;
		default:
			dxreport("%s default DVT message %d\n", __FUNCTION__, byMesageNo);
	}

	//��һ������ECMID,��0��ʾECM�޹���Ϣ
	if(cdcaMsg != CDCA_MESSAGE_CANCEL_TYPE)
		CDSTBCA_ShowBuyMessage(0, cdcaMsg);
	dxreport("DVTSTBCA_ShowPromptMessage end <<< cdcaMsg:%d\n", cdcaMsg);
}

void DVTSTBCA_HidePromptMessage(void)
{
	dxreport("DVTSTBCA_HidePromptMessage begin>>>\n");
	//ȡ����ʾ,��һ������ECMID,��0��ʾ��ECM�޹�
	CDSTBCA_ShowBuyMessage(0, CDCA_MESSAGE_CANCEL_TYPE);
	dxreport("DVTSTBCA_HidePromptMessage end <<<\n");
}

void DVTSTBCA_ShowFingerPrinting(DWORD dwCardID, WORD wDuration)
{
	//����û��ʱ��Ҫ��,cardidΪ0ʱȡ����ʾ
	CDSTBCA_ShowFingerMessage(wDuration, dwCardID);
}

void DVTSTBCA_ShowOSDMsg(BYTE byPriority,const char * szOSD)
{
	//����OSDֻҪ����ʾ����Ļ����,priority���Բ�����
	//���������ѿ�����ʾʱ��,������ʾ��ɺ�֪ͨCA��
	CDSTBCA_ShowOSDMessage(0, szOSD);
}

void DVTSTBCA_EmailNotify(BYTE byShow)
{
	//Email id Ϊ0
	// byShow, 0-�����ʼ�, 1-������, 2-����
	//byShow��ֵ���������²���ͬ
	if(byShow == DVTCAS_EMAIL_NEW)
	{
		byShow = 1;
	}
	else if(byShow == DVTCAS_EMAIL_NEW_NO_ROOM)
	{
		byShow = 2;
	}
	else if(byShow == DVTCAS_EMAIL_NONE)
	{
		byShow = 0;
	}
	CDSTBCA_EmailNotifyIcon(byShow, 0x0);
}

//Ӧ���㲥����
void * ThreadUrgencyBroadcast(void *arg)
{
	NS_sleep((int)arg * 1000);
	DVTSTBCA_CancelUrgencyBroadcast();
	return 0;
}

void DoUrgencyBroadcast(int duration)
{
	pthread_t pt;
	if(pthread_create(&pt, NULL, ThreadUrgencyBroadcast, (void*)duration) != 0) {
		dxreport("DoUrgencyBroadcast create thread failed!");
	}
}

//Ӧ���㲥,�漰��̨,��̨,osd,ң������
void DVTSTBCA_UrgencyBroadcast(WORD wOriNetID, WORD wTSID, WORD wServiceID, WORD wDuration)
{

	J_DataObject tre;
	J_NVItem ro, child1, child2, child3, child4, child5;

	ro.name = "/";

	child1.name = "broadcast_status";
	child1.value = (U32)BROADCAST_STATUS_START;
	child2.name = "orinet_id";
	child2.value = (U32)(0xFFFF & wOriNetID);
	child3.name = "ts_id";
	child3.value = (U32)(0xFFFF & wTSID);
	child4.name = "service_id";
	child4.value = (U32)(0xFFFF & wServiceID);
	child5.name = "duration";
	child5.value = (U32)(0xFFFF & wDuration);


	J_DataObject::iterator_base it = tre.set_root(ro);
	tre.append_child(it, child1);
	tre.append_child(it, child2);
	tre.append_child(it, child3);
	tre.append_child(it, child4);
	tre.append_child(it, child5);

	if(gStbCa.pCaMsgCallBack_)
		gStbCa.pCaMsgCallBack_(SHUMA_MSG,TVNOTIFY_GENCYBROADCAST,&tre);
	else
		dxreport("%s ca msg call back is null\n",__FUNCTION__);


	DoUrgencyBroadcast(wDuration);
}

void DVTSTBCA_CancelUrgencyBroadcast( void )
{
	J_DataObject tre;
	J_NVItem ro, child1;

	ro.name = "/";

	child1.name = "broadcast_status";
	child1.value = (U32)BROADCAST_STATUS_STOP;

	J_DataObject::iterator_base it = tre.set_root(ro);
	tre.append_child(it, child1);

	if(gStbCa.pCaMsgCallBack_)
		gStbCa.pCaMsgCallBack_(SHUMA_MSG,TVNOTIFY_GENCYBROADCAST,&tre);
	else
		dxreport("%s ca msg call back is null\n",__FUNCTION__);
}

void AppendIpps(J_DataObject & tre, J_DataObject::iterator_base & it, const SDVTCAIpp * pIpp)
{
	J_NVItem child1, child2, child3, child4, child5, child6;
	J_NVItem child7, child8, child9, child10, child11, child12;
	J_NVItem child13, child14, child15, child16, child17, child18;

	child1.name = "operator_id";
	child1.value = (U32)pIpp->m_wTVSID;
	child2.name = "prod_id";
	child2.value = (U32)pIpp->m_wProdID; 
	child3.name = "slot_id";
	child3.value = (U32)pIpp->m_bySlotID;
	child4.name = "prod_name";
	child4.value = pIpp->m_szProdName;
	child5.name = "start_time";
	child5.value = (U32)pIpp->m_tStartTime;
	child6.name = "duration_time";
	child6.value = (U32)pIpp->m_dwDuration;
	child7.name = "service_name";
	child7.value = pIpp->m_szServiceName;
	child8.name = "curtpp_tapprice";
	child8.value = (U32)pIpp->m_wCurTppTapPrice;
	child9.name = "curtpp_notapprice";
	child9.value = (U32)pIpp->m_wCurTppNoTapPrice;
	child10.name = "curcpp_tapprice";
	child10.value = (U32)pIpp->m_wCurCppTapPrice;
	child11.name = "curcpp_notapprice";
	child11.value = (U32)pIpp->m_wCurCppNoTapPrice;
	child12.name = "booked_price";
	child12.value = (U32)pIpp->m_wBookedPrice;
	child13.name = "booked_pricetype";
	child13.value = (U32)pIpp->m_byBookedPriceType;
	child14.name = "booked_interval";
	child14.value = (U32)pIpp->m_byCurInterval;
	child15.name = "cur_interval";
	child15.value = (U32)pIpp->m_byCurInterval;
	child16.name = "ipp_status";
	child16.value = (U32)pIpp->m_byIppStatus;
	child17.name = "unit";
	child17.value = (U32)pIpp->m_byUnit;
	child18.name = "ippt_period";
	child18.value = (U32)pIpp->m_wIpptPeriod;

//	child1.value = 2; 
//	child2.value = 3; 
//	child3.value = 4; 
//	child4.value = "aaaaaaaaaaab";
//	child5.value = 5; 
//	child6.value = 6;
//	child7.value = "servicename";
//	child9.value = 8; 
//	child8.value = 7;
//	child10.value = 9;
//	child11.value = 10; 
//	child12.value = 11; 
//	child13.value = 12; 
//	child14.value = 13; 
//	child15.value = 14; 
//	child16.value = 15; 
//	child17.value = 16; 
//	child18.value = 17; 

	tre.append_child(it, child1);
	tre.append_child(it, child2);
	tre.append_child(it, child3);
	tre.append_child(it, child4);
	tre.append_child(it, child5);
	tre.append_child(it, child6);
	tre.append_child(it, child7);
	tre.append_child(it, child8);
	tre.append_child(it, child9);
	tre.append_child(it, child10);
	tre.append_child(it, child11);
	tre.append_child(it, child12);
	tre.append_child(it, child13);
	tre.append_child(it, child14);
	tre.append_child(it, child15);
	tre.append_child(it, child16);
	tre.append_child(it, child17);
	tre.append_child(it, child18);
}

U32 DvtCAShowMotherCardPair(U8 type)
{
	J_DataObject trObj;
	J_NVItem root, child1;

	root.name = "/";
	child1.name = "ca_msg";
	child1.value = (J_U32)type;
		
	J_DataObject::pre_order_iterator iter = trObj.set_root(root);
	trObj.append_child(iter,child1);
	if(gStbCa.pCaMsgCallBack_)
		gStbCa.pCaMsgCallBack_(SHUMA_MSG,TVNOTIFY_MOTHER_CARDPAIR,&trObj);
	else
		dxreport("%s call back is null\n",__FUNCTION__);
	return 0;
}

void DVTSTBCA_InquireBookIpp(WORD wEcmPid, const SDVTCAIpp * pIpp )
{
	J_DataObject tre;
	J_NVItem ro,child;
	ro.name = "/";
	child.name = "book_ipp";

	J_DataObject::iterator_base it = tre.set_root(ro);
	J_DataObject::iterator_base itChild = tre.append_child(it, child);

	J_NVItem ecmItem;
	ecmItem.name = "ecm_pid";
	ecmItem.value = (U32)wEcmPid;

	tre.append_child(itChild, ecmItem);
	AppendIpps(tre, itChild, pIpp);

	if(gStbCa.pCaMsgCallBack_)
		gStbCa.pCaMsgCallBack_(SHUMA_MSG,TVNOTIFY_BUYIPP, &tre);
	else
		dxreport("%s call back is null\n",__FUNCTION__);
}

extern unsigned int AreaLockFunc (void*);
extern bool GetFreqInfo(U32 & freq, U32 & symb, U32 & qam);

//��Ƶ���������,�Ǳ�Ҫ����
HRESULT DVTSTBCA_SwitchChannel(WORD wWaitSeconds)
{
	dxreport("Enter %s duration %d\n",__FUNCTION__,wWaitSeconds);
	//���û�����õ�Ƶ��
	U32 freq, symb, qam;	
	freq = symb = qam = 0;
	GetFreqInfo(freq, symb, qam);
	if(!freq || !symb || !qam)
	{
		dxreport("Leave %s freq %d, symb %d, qam %d\n",__FUNCTION__,freq, symb, qam);
		DVTCASTB_SwitchChannelOver(2);
		return DVTCAERR_STB_UNKNOWN;
	}

	gStbCa.SonMotherThread_.stop();
	gStbCa.SonMotherThread_.start(AreaLockFunc, (void*)wWaitSeconds);
	dxreport("Leave %s\n",__FUNCTION__);
	return DVTCA_OK;
}

//��Ƶ���������,�Ǳ�Ҫ����
void DVTSTBCA_AreaLockOk(void)
{
	dxreport("Enter %s\n",__FUNCTION__);
	DVTCASTB_SwitchChannelOver(0);
	gAreaLockOK = true;
	dxreport("Leave %s\n",__FUNCTION__);
}

//����������汾��
DWORD DVTSTBCA_GetSoftVer(void)
{
	return 0;
}

//�����ּ���,�Ǳ�Ҫ����
HRESULT DVTSTBCA_GetCPUID(BYTE * pbyCPUID)
{
	return DVTCA_OK;
}

HRESULT	DVTSTBCA_GetNitValue(DWORD * pdwData)
{

	dxreport("Enter %s\n",__FUNCTION__);
	HRESULT ret = -1;
	if(gPDSD != -1)
	{
		*pdwData = gPDSD;
		ret = DVTCA_OK;
	}
	else //δʵ��ʱ����
	{
		*pdwData = 0;
	}
	dxreport("Leave %s ret %d data %d \n",__FUNCTION__,ret, *pdwData);
	return ret;
}
