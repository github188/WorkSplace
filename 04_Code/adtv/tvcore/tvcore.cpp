#include "tvcomm.h"
#include "tvcore.h"
#include "tvcore_ex.h"
#include "capture.h"
#include "tvplayer.h"
#include "itvsearchcore.h"

#define  LOG_TAG "libtvcore"
#include "tvlog.h"

typedef std::set<TVNOTIFY> TVNotifyCBListT;

struct TVCoreController{
	TVNotifyCBListT	pCallBacks_;///<回调列表
	DVBService  stCurService_;	///<当前频道
	MiniEPGNotify	stPFInfo_;	///<当前频道PF
};
static TVCoreController *gpController_ = 0; 
//=========================================================================================
//	TVCORE API Function implement
//=========================================================================================
// 消息回调
static int NotifyCallBack(int notifyCode,long lParam,void *pParam)
{
	bool bShuma = false;
	if(notifyCode == SHUMA_MSG && lParam == TVNOTIFY_BUYMSG)
	{
		notifyCode = lParam;
		J_DataObject * tre = (J_DataObject*)pParam;

		tree<J_NVItem>::pre_order_iterator it = tre->begin_pre_order_iterator(tre->root());
		tree<J_NVItem>::pre_order_iterator eit = tre->end_pre_order_iterator(tre->root());
		while(it != eit)
		{
			if(strcmp(it->name.c_str(),"ca_msg") == 0)
			{
				lParam = it->value.u32Val;
				break;
			}
			it++;
		}
		bShuma = true;
	}
	LOGTRACE(LOGINFO,"Enter TVPlayerCallBack(%d,%d,%p).\n",notifyCode,lParam,pParam);
	if(TVNOTIFY_MINEPG == notifyCode){
		memcpy(&(gpController_->stPFInfo_),static_cast<MiniEPGNotify*>(pParam),sizeof(MiniEPGNotify));
	}
	else if(TVNOTIFY_BUYMSG == notifyCode){
	    // 前提条件:取消显示和请插入智能卡是成对的,否则会有问题
	    	
	    	if(MESSAGE_CANCEL_TYPE == lParam)
	    	{
			tvcore_resetTSBuffer();
				tvcore_setVideoLayer(2);
	    	}
		else
		{
				tvcore_setVideoLayer(1);
		}
		LOGTRACE(LOGINFO,"tvcore_resetTSBuffer.\n");
	}
	else if(TVNOTIFY_TUNER_SIGNAL == notifyCode && 1 == lParam){//added by jianglei for 0006686 20120810
		tvcore_resetTSBuffer();
			tvcore_setVideoLayer(1);
		LOGTRACE(LOGINFO,"tvcore_setVideoLayer lost signalaa.\n");
	}
	else if(TVNOTIFY_TUNER_SIGNAL == notifyCode && 0 == lParam){//added by jianglei for 0006686 20120810
		
		tvcore_resetTSBuffer();			
			tvcore_setVideoLayer(2);
		
		LOGTRACE(LOGINFO,"aaaatvcore_setVideoLayer-------restore signal.\n");
	}
		
	if(bShuma)
	{
		notifyCode = SHUMA_MSG; 
		lParam = TVNOTIFY_BUYMSG;
	}

	if(gpController_)
	{
		// 此处可能需要改成异步
		TVNotifyCBListT::iterator it = gpController_->pCallBacks_.begin();
		for(;it != gpController_->pCallBacks_.end();it++)
		{
			(*it)(notifyCode,lParam,pParam);
		}
	}
	else{
		LOGTRACE(LOGINFO,"Enter NotifyCallBack,gpController_ is nil.\n");
	}
	LOGTRACE(LOGINFO,"Leave TVPlayerCallBack.\n");
	
	return 0;
}

// 初始化服务
int tvcore_init()
{
	LOGTRACE(LOGINFO,"Enter tvcore_init().\n");
	
	gpController_ = new TVCoreController();
	if(!gpController_){
		LOGTRACE(LOGINFO,"Enter tvcore_init(),new TVCoreController() failed.\n");
	}
	// 智能卡初始化
	Ca_Init();

	if(tvplayer_init())
	{
		//	注册相关回调函数
		tvplayer_register_notify_cb(NotifyCallBack);
		//  STBCA相关的回调消息
		Ca_SetCaMsgCallBack(NotifyCallBack);
	}
	
	LOGTRACE(LOGINFO,"Leave tvcore_init().\n");
	
	return 0;
}
// 释放服务资源
int tvcore_uninit()
{
	LOGTRACE(LOGINFO,"Enter tvcore_uninit.\n");

	if(gpController_){
		delete gpController_;
		gpController_ = 0;
	}
	
	Ca_Uninit();
	tvplayer_uninit();

	LOGTRACE(LOGINFO,"Leave tvcore_uninit.\n");
	
	return 0;
}
// 播放
int tvcore_play()
{
	LOGTRACE(LOGINFO,"Enter tvcore_play().\n");

	if(gpController_){
		memset(&(gpController_->stPFInfo_),0,sizeof(MiniEPGNotify));
	}
	
	// 初始化播放模块
	tvplayer_play();
	LOGTRACE(LOGINFO,"Leave tvcore_play().\n");

	return 0;
}
// 停止
int tvcore_stop()
{
	LOGTRACE(LOGINFO,"Enter tvcore_stop.\n");

	// 析构播放模块
	tvplayer_stop();

	LOGTRACE(LOGINFO,"Leave tvcore_stop.\n");

	return 0;
}
// 
int tvcore_setService(DVBService const *service)
{
	LOGTRACE(LOGINFO,"tvcore_setService.\n");

	//	调用播放模块---切换频道
	int loop = 3;
	bool istatus = false;
	while((loop >0)&&(loop<=3))
	{
		istatus = tvplayer_set_service(*service);
		loop--;
		if(istatus)
			break;
		LOGTRACE(LOGINFO,"neibu---AAAAAAAAAAAAAAAAAAAAAAAAAALeave tvcore_setService.--Istatus=%d,istatus=%d\n",loop,istatus);
	}
	LOGTRACE(LOGINFO,"AAAAAAAAAAAAAAAAAAAAAAAAAALeave tvcore_setService.--Istatus=%d,istatus=%d\n",loop,istatus);
	
	return 0;
}

// 获取Tuner的信号状态
bool	tvcore_get_tuner_status(TunerSignal& status)
{
	return tvplayer_get_tuner_status(status);
}

void tvcore_addTVNotify(TVNOTIFY callback)
{
	if(gpController_)
	{
		TVNotifyCBListT::iterator itCallBack = gpController_->pCallBacks_.find(callback);
		if(itCallBack == gpController_->pCallBacks_.end())
		{
			gpController_->pCallBacks_.insert(callback);
		}
	}
	else{
		LOGTRACE(LOGINFO,"gpController_ is nil.\n");
	}
}

void tvcore_delTVNotify(TVNOTIFY callback)
{
	LOGTRACE(LOGINFO,"Enter tvcore_delTVNotify.\n");
	
	if(gpController_)
	{
		TVNotifyCBListT::iterator it;
		it = gpController_->pCallBacks_.find(callback);
		if(it != gpController_->pCallBacks_.end())
		{
			// 任务序列中清除
			gpController_->pCallBacks_.erase(it);
		}
	}
	else{
		LOGTRACE(LOGINFO,"gpController_ is nil.\n");
	}
	
	LOGTRACE(LOGINFO,"Leave tvcore_delTVNotify.\n");
}

void tvcore_delAllTVNotify()
{
	if(gpController_)
	{
		gpController_->pCallBacks_.clear();
	}
}

// 修改pin码
U16  tvcore_change_pincode(const U8 *pOld,const U8 *pNew)
{
	return stbca_change_pin_code(const_cast<U8*>(pOld),const_cast<U8*>(pNew));
}
// 获取观看级别
U8   tvcore_get_watch_level()
{
	return stbca_get_watch_rating();
}

// 设置观看级别
U16	tvcore_set_watch_level(const U8 *pPinCode,const U8 iLevel)
{
	return stbca_set_watch_rating( const_cast<U8*>(pPinCode),iLevel);
}

// 设置观看时段
U16 tvcore_set_watch_time0(const U8 iStartHour,const U8 iEndHour)
{
	U16 uRet = 0;
	
	LOGTRACE(LOGINFO,"No implement.\n");
	return uRet;
}

U16 tvcore_set_watch_time1(const U8 *pPinCode,const U8 iStartHour,const U8 iEndHour)
{
	return stbca_set_watch_time1( const_cast<U8*>(pPinCode), iStartHour, iEndHour);
}

U16 tvcore_set_watch_time2(const U8 *pPinCode,
		                    const U8 iStarthour,const U8 iStartMin,const U8 iStartSec,
		                    const U8 iEndHour,const U8 iEndMin,const U8 iEndSec)
{
	return stbca_set_watch_time2( const_cast<U8*>(pPinCode), iStarthour, iStartMin,iStartSec, iEndHour, iEndMin, iEndSec);
}

// 获取观看时间
U16 tvcore_get_watch_time0(U8 *pStartHour,U8 *pEndHour)
{
	U16 uRet = 0;
	U8 start_hour = 0, end_hour = 0;
	
	uRet = stbca_get_watch_time1(start_hour,end_hour);
	
	*pStartHour = start_hour;
	*pEndHour   = end_hour;
	
	return uRet;
}

U16 tvcore_get_watch_time1(U8 *pStartHout,U8 *pStartMin,U8 *pStartSec,
		                    U8 *pEndHour,  U8 *pEndMin,  U8 *pEndSec)
{
	U16 uRet = 0;
	U8 start_hour=0,start_min=0,start_sec=0,end_hour=0,end_min=0,end_sec=0;
	
	uRet = stbca_get_watch_time2(start_hour, start_min, start_sec,end_hour, end_min,end_sec );
	
	*pStartHout = start_hour;
	*pStartMin  = start_min;
	*pStartSec	= start_sec;
	*pEndHour   = end_hour;
	*pEndMin	= end_min;
	*pEndSec	= end_sec;
	
	return uRet;
}

//  获取授权信息列表
U16 tvcore_get_authorization(const OperatorId id,EntitleListT& infos)
{
	return stbca_get_service_entitles(id,infos);
}

U16 tvcore_get_operator_acs(const OperatorId id, std::vector<U32> & list)
{
	return stbca_get_operator_acs(id, list);
}
//  获取智能卡号
bool tvcore_get_cardsn(char* pCardSN,U8 *iSNLength)
{
	bool bRet = false;
	
	std::string scard_id;
	bRet = stbca_scard_id(scard_id);
	if(0 != pCardSN && bRet)
	{
		*iSNLength = scard_id.size();
		memcpy(pCardSN,scard_id.c_str(),*iSNLength);
		
		bRet = true;
	}
	
	return bRet;
}


void tvcore_get_caVersion(char * pVer)
{
	stbca_version(pVer);
}

bool tvcore_getSTBId(char * pId, U8 buflen)
{
	return tvplayer_getSTBId(pId, buflen);
}

//  获取运营商ID
U16 tvcore_get_operator_id(OperatorIDListT& ids)
{
	return stbca_get_operator_ids(ids);
}

// 设置显示区域
bool tvcore_setDisplayRect(int x,int y,int width,int height)
{
	bool bRet = false;
	
	bRet = tvplayer_setDisplayRect(x,y,width,height);

	return bRet;
}

// 设置画面比例
bool tvcore_setDisplayZoomMode(const int iMode)
{
	bool bRet = false;
	
	bRet = tvplayer_setDisplayZoomMode(iMode);

	return bRet;
}

// 清除最后视频帧
bool tvcore_cleanVideoFrame(bool bClean)
{
	bool bRet = false;
	
	bRet = tvplayer_cleanVideoFrame(bClean);

	return bRet;
}

// 视频层控制
bool tvcore_setVideoLayer(int iState)
{
	return tvplayer_setVideoLayer(iState);
}


// 静音(AudioChannel mute)
int  tvcore_mute(bool bMute)
{
	return tvplayer_mute(bMute);
}

// 设置或获取设备的主音量
int  tvcore_getVolume()
{
	return tvplayer_getVolume();
}

int  tvcore_setVolume(float volume)
{	
	return tvplayer_setVolume(volume);
}

// 设置声道模式
bool tvCore_setAudioChannel(AudioStereoMode iMode)
{
	return tvplayer_setAudioChannel(iMode);
}

// 获取当前声道模式
AudioStereoMode tvcore_getAudioChannel()
{
	return tvplayer_getAudioChannel();
}

bool tvcore_set_audio_lang(U8 uIndex)
{
	return tvplayer_setAudioLang(uIndex);
}
U8   tvcore_get_audio_lang(void)
{
	return tvplayer_getAudioLang();
}

// 获取TDT/TOT时间
bool tvcore_get_tdt(U32& time)
{
	return tvplayer_getTDTTime(time);
}
bool tvcore_get_tot(U32& time,U32& offset)
{
	return tvplayer_getTOTTime(time,offset);
}


// 主动开始EPG搜索任务
// 注:除提供手动方式开始一个EPG搜索任务外(可以在非电视直播下使用),
// 内部在切换频道且频点发生改变的时候主动开启EPG搜索任务
bool tvcore_startEpgSearch(const TuningParam& tuning,const EITEventType evtType)
{
	return tvplayer_startEpgSearch(tuning,evtType);
}
// 主动取消EPG搜索任务
bool tvcore_cancelEpgSearch()
{
	return tvplayer_cancelEpgSearch();
}
// 获取EPG全部数据
bool tvcore_getEpgData(EPGDataBaseT& epgs)
{
	return tvplayer_getEpgData(epgs);
}
// 获取指定服务下的节目(事件)信息
bool tvcore_getEpgDataBySID(const U16 iServiceId,EpgEventSet& events)
{
	return tvplayer_getEpgDataBySID(iServiceId,events);
}
// 获取指定服务下指定时间段的节目(事件)信息
bool tvcore_getEpgDataByDuration(const U16 iServiceId,EpgEventSet& events,const U32 iStartTime,const U32 iEndTime)
{
	return tvplayer_getEpgDataByDuration(iServiceId,events,iStartTime,iEndTime);
}

// 开始搜索电视频道
U32 tvcore_startSearchTV(STVMode iMode, TuningParam *pTuningParam,ISearchTVNotify *pNotify)
{
	//停止解扰
	tvplayer_stop_descramb();
	// 获取运营商ids;
	int nId = 0;
	int nReplyLen = sizeof(int);
	int nRet = GetParameter(GET_OPERATOR_ID,&nId,&nReplyLen);
	if(-1 == nRet || -1 == nId){
		tvplayer_set_module_state(1);
		return StartSearchTV(iMode,pTuningParam,pNotify);
	}
	
	std::vector<OperatorId> ids;
	U16 uRet = stbca_get_operator_ids(ids);
	if(0 != uRet || ids.size() <= 0){
		LOGTRACE(LOGINFO,"GetOperatorIds failed or not exist.\n");
		return uRet;
	}
	
	nId = (0 == nId) ? ids[0] :nId;	
	
	bool bMatched = false;
	std::vector<OperatorId>::iterator iter = ids.begin();
	for(;iter != ids.end();iter++){
		if(nId == *iter){
			bMatched = true;
			break;
		}
	}
	
	if(!bMatched){
		LOGTRACE(LOGINFO,"not exist operator id.\n");
		return -1;
	}
	
	std::vector<U32> acs;
	uRet = (U32)stbca_get_operator_acs(nId,acs);
	if(0 != uRet || acs.size() <= 0){
		LOGTRACE(LOGINFO,"GetOperatorAcs failed or not exist.\n");
		return uRet;
	}

	int nReqLen = sizeof(U32) * acs.size();
	SetParameter(SET_OPERATOR_ACS,&acs,nReqLen);
	
	tvplayer_set_module_state(1);
	return StartSearchTV(iMode,pTuningParam,pNotify);
}

// 取消频道搜索
U32 tvcore_cancelSearchTV()
{
	return CancelSearchTV();
}


// 获取邮件的头信息
bool tvcore_getEMailHeads(EmailHead* pEmailHead,U8* pCount,U8* pFromIndex)
{
	return stbca_get_email_heads(pEmailHead,pCount,pFromIndex);
}
// 获取指定邮件的头信息
bool tvcore_getEMailHead(EmailId id, EmailHead& head)
{
	return stbca_get_email_head(id,head);
}
// 获取指定邮件的内容
bool tvcore_getEMailContent(EmailId id,EmailContent* pContent)
{
	return stbca_get_email_content(id,pContent);
}
// 删除邮件
void tvcore_delEMail(EmailId id)
{
	stbca_delete_email(id);
}
// 查询邮箱使用情况
// uEmailNum:已收邮件的个数,uEmptyNum:还能接收邮件的个数(邮箱容量限制:100)
void tvcore_getEMailSpaceInfo(U8& uEmailNum,U8& uEmptyNum)
{
	stbca_get_email_space(uEmailNum,uEmptyNum);
}

// 获取当前节目PF数据
bool tvocre_getPFEventInfo(MiniEPGNotify* pPFEvent)
{
	bool bRet = false;
	if(gpController_ && 0 != pPFEvent){
		memcpy(pPFEvent,&(gpController_->stPFInfo_),sizeof(MiniEPGNotify));
		bRet = true;
	}
	
	return bRet;
}

// 清除播放缓冲区数据
bool tvcore_resetTSBuffer()
{
	return tvplayer_resetTSBuffer();
}



/*
bool tvcore_getEMailHeadsEx(OUT J_DataObject& email_heads)
{
	typedef tree<J_NVItem> J_Tree;
	typedef J_Tree::pre_order_iterator iterator;
	J_NVItem ro;
	ro.name = "root";
	ro.value.vt = JVT_S32;
	ro.value.s32Val = 1000;
	iterator itroot = email_heads.set_root(ro);
	for(int i = 0; i < 5; i++)
	{
		J_NVItem child;
		char buf[32];
		memset(buf, 0, sizeof(buf));
		sprintf(buf,"child%d",i + 1);
		child.name = buf;
		child.value = i + 1;
		iterator itchild = email_heads.append_child(itroot, child);
		
		//第一个叶子
		J_NVItem emailTitle;
		emailTitle.name = "szEmailHead";
		memset(buf, 0, sizeof(buf));
		sprintf(buf,"testEmail%d",i + 1);
		emailTitle.value = buf;
		email_heads.append_child(itchild, emailTitle);

		//第二个叶子
		J_NVItem createTime;
		createTime.name = "tCreateTime";
		memset(buf, 0, sizeof(buf));
		createTime.value = i + 1;
		email_heads.append_child(itchild, createTime);
	}
	return true;
}

void printTree(const tree<J_NVItem> & tre)
{

	tree<J_NVItem>::pre_order_iterator it = tre.begin_pre_order_iterator(tre.root());
	tree<J_NVItem>::pre_order_iterator eit = tre.end_pre_order_iterator(tre.root());
	while(it != eit)
	{
		LOGTRACE(LOGINFO,"%s\n",it->name.c_str());
		if(it->value.vt == JVT_STRING)
		{
			char * p = it->value;
			LOGTRACE(LOGINFO,"vt:%d val:%s\n",it->value.vt, p);
		}
		else
		{
			LOGTRACE(LOGINFO,"vt:%d val:%d\n",it->value.vt, it->value.s32Val);
		}
		++it;
	}
}

bool tvcore_getEMailHeadEx(IN J_DataObject& email_id,OUT J_DataObject& email)
{
	printTree(email_id);
	tvcore_getEMailHeadsEx(email);
	return true;
}

*/
