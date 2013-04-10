#include "utcas_priv.h"
#include "plugin_ca/pluginca.h"

/*
 *CA����ӿ�
 */
CA_Interface gCaInterface;


/*-- �ź������壨��ͬ�Ĳ���ϵͳ���ܲ�һ����--*/
// static UTCA_Semaphore _SCSemaphore;
// static UTCA_Semaphore _dataSemaphore;
UTCA_Semaphore _ecmSemaphore;
UTCA_Semaphore _emmSemaphore;


ECMFliter_Info *_pEcmFliters[UTCA_MAXNUM_ECM];
UTCA_U32 _ecmCount;
PRIV_DATA _ecmData;

UTCA_U16 _emmPid;
PRIV_DATA _emmData;

UTCA_U8 _byThreadPrior;

UTCA_BOOL _taskRun;

//ECM�߳��˳����
UTCA_BOOL _taskExit;

/*
   ---------------------------------------------------------------------------------------------------------
   -                            �� �� �� ��
   ---------------------------------------------------------------------------------------------------------
   */

void CreateEcmTask(UTCA_U8 byThreadPrior)
{
	_taskRun =	UTCA_TRUE;

	CDSTBCA_SemaphoreInit(&_ecmSemaphore, UTCA_FALSE);
	CDSTBCA_Printf(2, "ecm semaphore init ok!");
	if (CDSTBCA_RegisterTask("ecmTask", byThreadPrior, &ecmTask, 0, 1024) == UTCA_FALSE) {
		CDSTBCA_Printf(2, "create ecm task error!");
		return;
	}
	CDSTBCA_Printf(2, "create ecm task ok!");
}
void CreateEmmTask(UTCA_U8 byThreadPrior)
{

	CDSTBCA_SemaphoreInit(&_emmSemaphore, UTCA_FALSE);
	CDSTBCA_Printf(2, "emm semaphore init ok!");
	if (CDSTBCA_RegisterTask("emmTask", byThreadPrior, &emmTask, 0, 1024) == UTCA_FALSE) {
		CDSTBCA_Printf(2, "create emm task error!");
		return;
	}
	CDSTBCA_Printf(2, "create emm task ok!");
}

// Ŀǰû��ʹ��
static void msgTask()
{
	while(UTCA_TRUE == _taskRun) {
		CDSTBCA_Sleep(100);
	}
}
static void ecmTask()
{
	while(UTCA_TRUE == _taskRun) {
		if(_ecmCount == 0)		// ��û��ecm ʱ�� ˯�ߵȴ�
		{
			CDSTBCA_Sleep(100);
			continue;
		}
		HandleEcmRequest(_ecmCount);
	}
	_taskExit = UTCA_TRUE;
	CDSTBCA_Printf(2, "Ecm task quit!");
}

static void emmTask()
{
	UTCA_U8   masks[64];
	UTCA_U8   fliters[64];
	UTCA_U8   reqID = EMM_REQ_ID_MIN ;
	while(UTCA_TRUE == _taskRun) {
		if(_emmPid == 0) 		// ����emm ʱ(_emmPid == 0)�� ˯�ߵȴ�
		{
			CDSTBCA_Sleep(100);
			continue;
		}
		// emm request ID Ӧ�ñ�0x80 С
		if (CDSTBCA_SetPrivateDataFilter(reqID,  
					fliters,  
					masks, 
					8, 
					_emmPid, 
					30) != UTCA_TRUE) 
		{
			static int fatalErr=0;
			if(fatalErr==0)
			{
				fatalErr = 1;
				CDSTBCA_Printf(2,"Fatal Error!, emm filter can't set\n");
			}
			CDSTBCA_Sleep(100);
			continue;
		}

		CDSTBCA_SemaphoreWait(&_emmSemaphore);

		if (gCaInterface.CA_ParseEmm(_emmData.buf, _emmData.wLen) != UTCA_TRUE) {
			continue;
		}

	}
}

void HandleEcmRequest(int ecmCount)
{
	int i;
	UTCA_U8 cwOdd[8];
	UTCA_U8 cwEven[8];
	ECMFliter_Info *pEcm;
	for (i=0; i<ecmCount; i++) {
		pEcm = _pEcmFliters[i];
		if (pEcm != 0 && pEcm->pid == 0 ) {	// ������ecmfilter ��pid Ϊ0�� ������һ��ecm fileter
			continue;
		}

		if (CDSTBCA_SetPrivateDataFilter(pEcm->reqID,  
					pEcm->fliters,  
					pEcm->masks, 
					8, 
					pEcm->pid, 
					30) != UTCA_TRUE) 
		{ //���ù�����
			static int fatalErr=0;
			if(fatalErr==0)
			{
				fatalErr = 1;
				CDSTBCA_Printf(2,"Fatal Error!, ecm filter can't set\n");
			}
			CDSTBCA_Sleep(100);
			continue;
		}

		//�ȴ����ݵ���
		CDSTBCA_SemaphoreWait(&_ecmSemaphore); // ���￴������ͬ��ecmfilter ��ͬ�����е�
		//�п������˳�ʱ�Ļ���
		if(UTCA_TRUE != _taskRun)
		{
			CDSTBCA_Printf(2,"HandleEcmRequest break");
			break;
		}


		if ((_ecmData.bTimeout == UTCA_TRUE)
				|| (_ecmData.byReqID != pEcm->reqID)
				|| (_ecmData.wPid != pEcm->pid) ) {

			CDSTBCA_Printf(2,"Wrong Param");
			continue;
		}
		//			sprintf(message,"ecmTask, data:%x!!!! len:%d",_ecmData.buf[0],_ecmData.wLen);
		//			CDSTBCA_Printf(2,message);
		/*			
		// �Ƹ�++	
		// ����ecm data length:84 �� 116 ����Ч, �ɹ���������extention tableid����
		if(_ecmData.wLen < 100)	// ���г��ȹ��ǣ�debux ���ܸ������������ݣ����ȹ��ǲ�ȫ)
		continue;
		// �Ƹ�--
		*/
		// ��������
		// �����������ܿ�ͨ��,��ʱ�ϳ�
		if (gCaInterface.CA_ParseEcm(_ecmData.buf, _ecmData.wLen) != UTCA_TRUE) {
			CDSTBCA_Printf(3,"Wrong Data");
			continue;
		}

		CDSTBCA_Printf(3,"Get CW now.");
		gCaInterface.CA_GetCW(cwOdd, cwEven);

		CDSTBCA_Printf(3,"Set CW now.");
		CDSTBCA_ScrSetCW(pEcm->pid,  
				cwOdd,  
				cwEven, 
				8, 
				UTCA_FALSE); 
		// hjj add, ���¹����֣�ʹȡ��һ��ecmdata
		if(_ecmData.buf[0]==0x80)
		{
			pEcm->fliters[0]=0x81;
			pEcm->masks[0]=0xff;
		}
		else
		{
			pEcm->fliters[0]=0x80;
			pEcm->masks[0]=0xff;
		}
	}
}
void freeEcmFilters()
{
	unsigned int i;
	for (i=0; i<_ecmCount; i++) { 
		void *p = _pEcmFliters[i];
		CDSTBCA_Free(p);

		_pEcmFliters[i] = 0;
	}

	_ecmCount = 0;
}
void KillEmmTask()
{
	CDSTBCA_SemaphoreSignal(&_emmSemaphore);
	CDSTBCA_Sleep(100);	// wait task exit
}

void KillEcmTask()
{
	_taskExit = UTCA_FALSE;

	_taskRun=UTCA_FALSE;
	CDSTBCA_SemaphoreSignal(&_ecmSemaphore);
	//��ECM�߳��˳�ʱ,��ѱ�־��Ϊture
	while(_taskExit == UTCA_FALSE)
		CDSTBCA_Sleep(100);	// wait task exit

}

// Ŀǰû��ʹ��
static UTCA_BOOL parseATR(UTCA_U8 *pbyBuf, UTCA_U8 byLen, void *pAtr)
{
	ATR_S *atr = (ATR_S *)pAtr;
	int done = 0;
	int index = 0;
	UTCA_U8 curByte;
	UTCA_U8 check;
	UTCA_U8 interfaceIteration = 0;
	UTCA_U8 historicalBytes = 0;
	UTCA_U8 expectedCharacters = 0;

	while (!done)
	{
		if (index >= byLen) {
			return UTCA_FALSE;
		}

		// Read and store ATR byte
		curByte = pbyBuf[index++];
		if (index == 1)
		{
			atr->TS = curByte;
			if ((curByte != 0x3f) && (curByte != 0x3b))
			{
				return UTCA_FALSE;
			}
			check = 0;
		}
		else
			check ^= curByte;
		if (index == 2)
		{
			historicalBytes = curByte & 0x0F;
			expectedCharacters = curByte & 0xF0;
			atr->T0 = curByte;
		}
		if (index > 2)
		{
			switch(expectedCharacters)
			{
				case 0x00:
					// Historical characters
					historicalBytes--;
					atr->historical[atr->historicalLength++] = curByte;
					if (historicalBytes == 0)
					{
						//T����ԭ����
						/*if (T == 0)
						  {
						  done = 1;
						  }
						  else{
						  expectedCharacters = 0x01;
						// Go to checksum state
						}*/
					}
					break;
				case 0x01:
					// TCK case                
					atr->TCK = curByte;
					done = 1;
					break;
				case 0x10:
				case 0x30:
				case 0x50:
				case 0x70:
				case 0x90:
				case 0xB0:
				case 0xD0:
				case 0xF0:
					// TA case
					expectedCharacters &= 0xE0;
					atr->TA[interfaceIteration] = curByte;
					break;
				case 0x20:
				case 0x60:
				case 0xA0:
				case 0xE0:
					// TB case
					expectedCharacters &= 0xD0;
					atr->TB[interfaceIteration] = curByte;
					break;
				case 0x40:
				case 0xC0:
					// TC case
					expectedCharacters &= 0xB0;
					atr->TC[interfaceIteration] = curByte;
					break;
				case 0x80:
					// TD case
					expectedCharacters=(curByte&0xF0);
					// Handle zero historical characters
					if ((expectedCharacters == 0x00) && (historicalBytes == 0))
					{
						//T����ԭ����
						/*if (T == 0)
						  {
						  done = 1;
						  }
						  else
						  expectedCharacters = 0x01;*/
					}
					atr->TD[interfaceIteration] = curByte;
					// If we get TD1, we have the first protocol selection
					if ((interfaceIteration==1))
					{	//T����ԭ����
						//T = curByte & 0x0F;
					}
					else
					{
						// Changing protocols is only valid under ISO (not allowed in EMV)
						//T����ԭ����
						/*if ((curByte & 0x0F) != T)
						  {
						  T = curByte & 0x0F;
						  }*/
					}
					interfaceIteration++;
					break;
				default:
					return UTCA_FALSE;
					//break;
			}
		}	
	}//end !done

	return UTCA_TRUE;
}

