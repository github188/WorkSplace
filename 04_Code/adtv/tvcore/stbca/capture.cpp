//包含中文字符,供vim 识别
#include "tvdevice.h"
#include "simplethread.h"
#include "cdcas_hdr.h"
#include "capture.h"
#include "cdcalib4stb.h"
#include "stbca_utility.h"
#include "blockring.h"
#include "xprocess.h"
#define LOG_LEVEL LOG_TRACE
#define LOG_TAG "stbca"
#include "dxreport.h"


#ifdef JS_USE_SHUMACA_SEARCH
	#define PINCODELENGTH 8
#endif
#ifdef JS_USE_NOVELCA_SEARCH
	#define PINCODELENGTH 6
#endif
#ifdef JS_USE_GEHUACA_SEARCH
	#define PINCODELENGTH 6
#endif	

////////////////////////////////////////////////////////////////////////////////
// 变量声明
////////////////////////////////////////////////////////////////////////////////
extern ObjStbCa gStbCa;
extern RecvData gSaveEcmRecvData;

STBCA_API bool Ca_Init()
{
	dxreport("Ca_Init begin >>>\n");
	gStbCa.hTVDevice_=tvdevice_open();
	if(!gStbCa.hTVDevice_){
		dxreport("error open device\n");
		return false;
	}
	if(!CDCASTB_Init(1))
	{
		dxreport("error CDCASTB_Init...\n");
		return false;
	}
	
//	CheckSCardStatus();
//	
//	if(gStbCa.bSCardStatus_) {
//		dxreport("card inserted\n");
//	}
//	else {
//		dxreport("card not inserted\n");
//	}
	Ca_Start();
	dxreport("Ca_Init end <<<\n");
	return true;
}

STBCA_API void Ca_Start()
{
	dxreport("Ca_Start called...\n");
	gStbCa.caThread_.start(CAWorkProc,&gStbCa.caThread_);
}

/****************************************************************
 * Description:  	STBCA structure uninitialization.
 * Input:        	index
 * Return:		
 ****************************************************************/
 
STBCA_API void Ca_Stop()
{
	dxreport("Ca_Stop called...\n");
	Descrambling nullDescramb;
	memset(&nullDescramb,0,sizeof(nullDescramb));
	Ca_SetDescrambling(&nullDescramb,1,0); // 无ecm, emm, 删原来的filter,
// 等待private data 改变后，退出ca 线程	
	gStbCa.caThread_.stop();		// 停止stbca 线程,使不再向CA_lib 发消息
	// 清理CW, 使画面播放不残留
	gStbCa.pEcmSendCaMsgRing_->ClearRing();
//	gStbCa.pEmmSendCaMsgRing_->ClearRing();
	ClearCW();		
	gStbCa.bSCardStatus_=false;  //Ca_Stop后，认为卡未插入,造成每次使卡复位
}
STBCA_API void Ca_Uninit()
{
	dxreport("Ca_Uninit begin >>>\n");
	Ca_Stop();
#if !defined(WIN32)
	CDCASTB_Close();		// 不调用不行? 奇怪?
#endif 
	tvdevice_close(gStbCa.hTVDevice_);
	gStbCa.hTVDevice_ = 0;
	dxreport("Ca_Uninit end <<<\n");
}
#ifdef JS_USE_SHUMA_CALIB_NORMAL
	extern bool g_bEcmProcessing;
#endif

STBCA_API int Ca_SetDescrambling (Descrambling * descramb, int descrambCount, U16 wEmmPid)
{
	dxreport("Ca_SetDescrambling begin >>>，descrambCount:%d,ecm:%d, emm:%d\n",descrambCount,descramb[0].ecmPid,wEmmPid);
	
	{
//		AutoLockT lock_it(gStbCa.mutexMyControl_);
		CloseGMapDemuxFilters_Lock(gStbCa.mapMyControl_hFilter_ReqIDPID_, gStbCa.mutexMyControl_);
	}
	gStbCa.pEcmSendCaMsgRing_->ClearRing();
	gSaveEcmRecvData.ClearData();
#ifdef JS_USE_SHUMA_CALIB_NORMAL
	static int count=0;
	while(g_bEcmProcessing ==true)
	{
		if(count==0)
		{
			dxreport("waiting ecm process...\n"); // 1秒钟打一次
			count++;
			if(count >= 10) count = 0;
		}
		NS_sleep(100);
	}
	count = 0;
#endif

	
	//设置ecmpid
	CDCASTB_SetEcmPid(CDCA_LIST_FIRST, NULL);
	// 先清理gStbCa.saveDescrambs_内存，这样不管descrambCount 为0或其它，都是干净的
	for(int i=0; i< 8; i++)
	{
		gStbCa.saveDescrambs_[i],0,sizeof(Descrambling);
	}

	if(descrambCount > 0)
		gStbCa.serviceID_ = descramb[0].wServiceID;

	//先赋值,再设置过滤器,如果放在后边有可能,收到数据时这个变量还没有赋值
	gStbCa.saveDescramCount_ = descrambCount < 8 ? descrambCount : 8;
	for(int i=0;i<descrambCount && i<8; i++)
	{
		SCDCASServiceInfo serviceInfo;
		gStbCa.saveDescrambs_[i]=descramb[i];
		memset(&serviceInfo,0, sizeof(serviceInfo));
		serviceInfo.m_wEcmPid = descramb[i].ecmPid;
		serviceInfo.m_wServiceID[0] = descramb[i].wServiceID;
		serviceInfo.m_byServiceNum = 0;
		if(serviceInfo.m_wServiceID[0]!=0)
		{
			serviceInfo.m_byServiceNum = 1;
		}
		dxreport("Set ecmpid 0x%x, servidID:%d\n",descramb[i].ecmPid,serviceInfo.m_wServiceID[0]);
		CDCASTB_SetEcmPid(CDCA_LIST_ADD, &serviceInfo);
	}
	
	if((gStbCa.saveDescramCount_!=0 )&& (gStbCa.saveDescrambs_[0].ecmPid!=0))
	{
		gStbCa.enableEmm_ = false; // 不允许emm, 没有加锁, 因为无明显副作用。
	}
	else
	{
		gStbCa.enableEmm_ = true; // 清流允许emm, 包括ecmCount=0(同方), ecmCount=1但ecmpid=0(数码)没有加锁, 因为无明显副作用。
	}
	CDCASTB_SetEcmPid(CDCA_LIST_OK, NULL);
	//设置emmpid
	if(gStbCa.emmpid_!= wEmmPid && wEmmPid!=0) // 当调用ca_stop时, wEmmPid 为0， 此时不修正emmFilter, 不过好像正常不会调用ca_stop
	{
		//关闭已有的emm filter 列表,可能出现同线程反复加锁
//		AutoLockT lock_it(gStbCa.mutexCalibControl_);
		CloseGMapDemuxFilters(gStbCa.mapCaLibControl_hFilter_ReqIDPID_); // 需要删除该过滤器，因为它会重建,calib 还是没管它
		gStbCa.emmpid_ = wEmmPid;
		dxreport("Set EmmPid 0x%x\n",wEmmPid);
		CDCASTB_SetEmmPid(wEmmPid);
	}
	dxreport("Ca_SetDescrambling end <<< \n");
	
	return 0;
}

STBCA_API int  Ca_SetCaMsgCallBack(TVNOTIFY pCallBack)
{
	gStbCa.pCaMsgCallBack_=pCallBack;
	return 0;
}
	

// 获取当前的CAS类型.
STBCA_API CasType stbca_cas_type()
{
#ifdef JS_USE_NOVELCA_SEARCH	
	gStbCa.casType_ = CasType_TFCAS;
#endif
#ifdef JS_USE_SHUMACA_SEARCH
	gStbCa.casType_ = CasType_SMCAS;
#endif
#ifdef JS_USE_GEHUACA_SEARCH
	gStbCa.casType_ = CasType_GHCAS;
#endif
	dxreport("cas type is %d\n",gStbCa.casType_);
	return gStbCa.casType_;
//	return CasType_CDCAS;
}
STBCA_API void stbca_version(char *version)
{
	UINT32 ver=CDCASTB_GetVer();
	sprintf(version,"%x",ver);
}

//获取STBID号.
STBCA_API void stbca_get_stbid(OUT WORD* pwPlatformID, OUT ULONG* pdwUniqueID)
{
	UINT8 STBIDdata[6+1]={0};
	tvdevice_getStbID(gStbCa.hTVDevice_, (char *)STBIDdata,sizeof(STBIDdata));
	*pwPlatformID =(STBIDdata[0]<<8)+STBIDdata[1];
	*pdwUniqueID=(STBIDdata[2]<<24)+(STBIDdata[3]<<16)+(STBIDdata[4]<<8)+STBIDdata[5];
}

//==============邮件管理接口===================
/// 进入邮件管理调用的函数
STBCA_API void stbca_enter_mail_manager()
{
}

/// 离开邮件管理调用的函数
STBCA_API void stbca_leave_mail_manager()
{
}

//注：使用以上两个函数提高邮件管理效率
// 获取邮箱中的邮件数和邮箱还可以容纳的邮件数.
STBCA_API U32 stbca_get_email_space( U8 &email_num, U8 & empty_num )
{
//	U32 bRet = 1;
//	if(CDCA_RC_OK == CDCASTB_GetEmailSpaceInfo(&email_num,&empty_num)){
//		bRet = 0;
//	}

	return CDCASTB_GetEmailSpaceInfo(&email_num,&empty_num);
}

// 获取指定邮件的邮件头.
STBCA_API bool stbca_get_email_head( EmailId email_id, EmailHead & email_head )
{
	bool bRet = false;
	SCDCAEmailHead sc_email_head;
	if(CDCA_RC_OK == CDCASTB_GetEmailHead(email_id,&sc_email_head))
	{
		email_head.email_id = sc_email_head.m_dwActionID;
		email_head.email_level = sc_email_head.m_wImportance ? EmailLevel_Important : EmailLevel_Normal;
//		email_head.email_title = sc_email_head.m_szEmailHead;
		memcpy(email_head.email_title,sc_email_head.m_szEmailHead,sizeof(sc_email_head.m_szEmailHead));
		email_head.new_email = sc_email_head.m_bNewEmail ? true : false;
		email_head.send_time = sc_email_head.m_tCreateTime;
		bRet = true;
	}

	return bRet;
}

// 获取所有邮件头.
STBCA_API bool stbca_get_email_heads(EmailHead* pEmailHead,U8 *pCount,U8 *pFromIndex)
{
	bool bRet = false;
	if(CDCA_RC_OK == CDCASTB_GetEmailHeads(reinterpret_cast<SCDCAEmailHead*>(pEmailHead),pCount,pFromIndex))
	{
		bRet = true;
	}
	
	return bRet;
}

// 获取邮件内容. 返回局部指针不当，注意使用方法
STBCA_API bool stbca_get_email_content( EmailId email_id, EmailContent *pEmailContent)
{
	bool bRet = false;
	if(CDCA_RC_OK == CDCASTB_GetEmailContent(email_id,reinterpret_cast<SCDCAEmailContent*>(pEmailContent))){
		bRet = true;
	}
	
	return bRet;
}

// 删除一封邮件.
STBCA_API void stbca_delete_email( EmailId email_id )
{
	return CDCASTB_DelEmail(email_id);
}

// 删除所有的邮件
STBCA_API void stbca_delete_emails()
{
	//not implyment
}

// ================智能卡管理====================================
// GetCardID获取智能卡卡号.
STBCA_API bool stbca_scard_id( std::string &scard_id )
{
	char CardSN[CDCA_MAXLEN_SN+1];
	CardSN[0]=0;
	dxreport("stbca_scard_id begin >>>\n");
	UINT16 res=CDCASTB_GetCardSN(CardSN);
	dxreport("stbca_scard_id end <<< res:%d, CardSN:%s\n",res,CardSN);
	if(!res)
	{
		scard_id=CardSN;
		return true;
	}

	return false;
}

STBCA_API int stbca_get_CardStatus(U8 * status)
{
	*status = gStbCa.bSCardStatus_ ? 1 : 0;
	return 0;
}

// 下面这个函数未用
STBCA_API int stbca_GetCardID(char *pCardID)
{
	pCardID[0]=0;
	U16 res=CDCASTB_GetCardSN(pCardID);
	dxreport("stbca_GetCardID: res:%d, card id:%s\n",res,pCardID);
	return res;
}

// 修改Pin码.
STBCA_API U32  stbca_change_pin_code( U8 *old_code, U8 *new_code )
{
	unsigned char oldPin[PINCODELENGTH];
	unsigned char newPin[PINCODELENGTH];
	for(int i=0; i<PINCODELENGTH; i++)
	{
		oldPin[i]=old_code[i]-0x30;
		newPin[i]=new_code[i]-0x30;
	}
	return CDCASTB_ChangePin(oldPin,newPin);
}

// 获取观看级别.
STBCA_API U8 	stbca_get_watch_rating()
{
	UINT8 rating;
	UINT16 res=CDCASTB_GetRating(&rating);
	if(!res)
		return rating;
	return 0;
}

// 设置观看级别.
STBCA_API U32 	stbca_set_watch_rating( U8 *pin_code, U8 rate )
{
	unsigned char Pin_bin[PINCODELENGTH];
	for(int i=0; i<PINCODELENGTH; i++)
	{
		Pin_bin[i]=pin_code[i]-0x30;
	}
	
	return CDCASTB_SetRating(Pin_bin,rate);
}

// 获取观看时段.
STBCA_API U16 	stbca_get_watch_time1( U8 & start_hour, U8 & end_hour )
{
	BYTE byStartHour, byStartMin, byStartSec, byEndHour,byEndMin, byEndSec;
	start_hour=0;
	end_hour=0;
	UINT16 res=CDCASTB_GetWorkTime(&byStartHour,&byStartMin,&byStartSec,&byEndHour,&byEndMin,&byEndSec);
	if(!res)
	{
		start_hour=byStartHour;
		end_hour=byEndHour;
	}
	return res;
}

// 获取观看时段.
STBCA_API U16 	stbca_get_watch_time2( U8 & start_hour, U8 & start_min, U8 & start_sec,U8 & end_hour, U8 & end_min, U8 & end_sec )
{
	UINT16 res=CDCASTB_GetWorkTime(&start_hour,&start_min,&start_sec,&end_hour,&end_min,&end_sec);
	return res;
}

// 设置观看时段.
STBCA_API U32 	stbca_set_watch_time1( U8 *pin_code, U8 start_hour, U8 end_hour )
{
	unsigned char Pin_bin[PINCODELENGTH];
	for(int i=0; i<PINCODELENGTH; i++)
	{
		Pin_bin[i]=pin_code[i]-0x30;
	}
	return CDCASTB_SetWorkTime(Pin_bin,start_hour,0,0,end_hour,0,0);
}

// 设置观看时段.
STBCA_API U32	stbca_set_watch_time2( U8 *pin_code, U8 start_hour, U8 start_min,U8 start_sec, U8 end_hour, U8 end_min, U8 end_sec )
{
	unsigned char Pin_bin[PINCODELENGTH];
	for(int i=0; i<PINCODELENGTH; i++)
	{
		Pin_bin[i]=pin_code[i]-0x30;
	}
	return CDCASTB_SetWorkTime(Pin_bin,start_hour,start_min,start_sec,end_hour,end_min,end_sec);
}

//=======================================================================
// 判断Pin码是否锁定.
STBCA_API bool stbca_pincode_locked()
{
	return false;
}

// 机卡配对
STBCA_API U16  stbca_set_paired( U8 *pin_code )
{
	UNUSED_PARAM(pin_code);
	return 0;
}

// 判断机卡是否已经配对
STBCA_API U16  stbca_is_paired1()
{
	return 0;
}

// 判断机卡是否已经配对    
STBCA_API U16  stbca_is_paired2( U8* stb_num, U8* stb_id_list )
{
	return CDCASTB_IsPaired(stb_num,stb_id_list);
}

//=======================================================================
// 获取运营商ID序列
STBCA_API U16 stbca_get_operator_ids( std::vector<OperatorId>& ids )
{
	CDCA_U16 wIDs[CDCA_MAXNUM_OPERATOR];
	memset(wIDs, 0, sizeof(wIDs));
	int res=CDCASTB_GetOperatorIds(wIDs);
	if(res) return res;
	for(UINT i=0; i<CDCA_MAXNUM_OPERATOR; i++)
	{
//		printf("wIDs[i]: %d\n", wIDs[i]);
		if(wIDs[i]==0) break;
		ids.push_back(wIDs[i]);
	}
	return 0;
	
}

//获取运营商信息.
STBCA_API UINT16 stbca_get_operator_info( OperatorId id, OperatorInfo & info )
{
	SCDCAOperatorInfo scInfo;
	UINT16 res=CDCASTB_GetOperatorInfo(id,&scInfo);
	if(res==CDCA_RC_OK)
	{
		dxreport("ok, operator name is:%s\n",scInfo.m_szTVSPriInfo);
		info.name = scInfo.m_szTVSPriInfo;
	}
	else
		dxreport("failed, get operator info return:%d\n",res);
	return res;
}

// 获取运营商的特征值.
STBCA_API U16 stbca_get_operator_acs( OperatorId id, std::vector<U32>& acs)
{
	ULONG array[CDCA_MAXNUM_ACLIST];
	UINT16 res=CDCASTB_GetACList(id,array);
	if(res==CDCA_RC_OK)
	{
		for(unsigned int i=0; i<CDCA_MAXNUM_ACLIST; i++)
			acs.push_back(array[i]);
	}
	return res;
}

// 获取授权列表
STBCA_API U16 stbca_get_service_entitles( OperatorId id,std::vector<Entitle> & entitles )
{
	Entitle entitle;
	SCDCAEntitles sc_entitles;
	UINT16 res=CDCASTB_GetServiceEntitles(id,&sc_entitles);
	if(res==CDCA_RC_OK)
	{
		for(int i=0; i<sc_entitles.m_wProductCount; i++)
		{
			entitle.product_id = sc_entitles.m_Entitles[i].m_dwProductID;
			entitle.start_time = sc_entitles.m_Entitles[i].m_tBeginDate;
			entitle.expired_time = sc_entitles.m_Entitles[i].m_tExpireDate;
			entitle.is_record = sc_entitles.m_Entitles[i].m_bCanTape ? true:false;
			entitles.push_back(entitle);
			dxreport("product_id:%d, start_time:%d, end_time:%d, is_record:%d\n",
				entitle.product_id,entitle.start_time,entitle.expired_time,entitle.is_record);
		}
	}
	return res;
}

// 获取钱包ID列表
STBCA_API U16 stbca_get_purse_ids( OperatorId id, std::vector<PurseId>& purse_ids )
{
	dxreport("stbca_get_purse_ids begin, id:%d >>>\n",id); 
	SCDCAIppvInfo arrayIppv[CDCA_MAXNUM_IPPVP];
	UINT16 ippvCount=CDCA_MAXNUM_IPPVP;
	UINT16 res=CDCASTB_GetIPPVProgram(id,arrayIppv,&ippvCount);
	if(res==CDCA_RC_OK)
	{
		for(int i=-0; i<ippvCount; i++)
		{
			purse_ids.push_back((U8)arrayIppv[i].m_dwProductID);
		}
		dxreport("succeed, res is %d, ippvCount:%d\n",res,ippvCount);
	}
	else
	{
		dxreport("failed,res is %d\n",res);
	}
	dxreport("stbca_get_purse_ids end <<<\n"); 
	return res;
}

// 获取钱包信息.
STBCA_API U16 stbca_get_purse_info( OperatorId id, PurseId purse_id,PurseInfo & info )
{
	UNUSED_PARAM(id);
	UNUSED_PARAM(purse_id);
	UNUSED_PARAM(info);
	return 0;
}
