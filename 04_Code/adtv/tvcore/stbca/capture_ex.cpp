#include <capture_ex.h>
#include <capture.h>

U32 DvtCASManuInfo(SDVTCAManuInfo * pManuInfo);

#define LOG_LEVEL LOG_TRACE
#define LOG_TAG "stbca"
#include "dxreport.h"

#define INVALID_VALUE 0xcdcdcdcd

enum ParamKey
{
	ParamKey_EmailRead = 1,				//设置邮件阅读完成
	ParamKey_GetCASInfo,				//获取CAS信息
	ParamKey_GetPinLocked,				//获取智能卡锁定状态
	ParamKey_GetAreaInfo,				//获取区域信息
	ParamKey_GetMontherInfo,			//获取母卡信息
	ParamKey_CorrespondInfo = 6,		//设置或获取子母卡配对信息
	ParamKey_PurseInfo = 7,				//获取钱包信息
	ParamKey_Ipps = 8,					//获取所有可预订/退订的Ipp
	ParamKey_BookIpp = 9,				//设置预订/退订的Ipp
	ParamKey_ViewedIpp = 10,			//获取已购买的IPP信息
	ParamKey_InquireBookIppOver = 11,	//设置实时购买IPP完成
	ParamKey_ShowOSDOver = 12,			//设置OSD显示完成一次
	ParamKey_DebugSign = 13,			//设置调试打印开关
	ParamKey_VerifyPin = 14,			//设置校验密码
	ParamKey_SmartCardStatus = 15,		//获取智能卡状态
	ParamKey_MotherCardPairOver = 16,	//子母卡自动配对完成
	ParamKey_AreaLockFreq = 17,			//设置或获取单频点信息
};

////////////////////////////////////////////////////////////////////////////////
// 获取object 节点数据需要的 data object 扩展
////////////////////////////////////////////////////////////////////////////////
void split(std::string data , std::string splt, std::vector<std::string>& result);
tree<J_NVItem>::iterator_base FindNodeByString(tree<J_NVItem>& tr,tree<J_NVItem>::iterator_base it,std::string tag);
CVariant_t /*tree<J_NVItem>::iterator_base*/ GetValue(tree<J_NVItem>& tr, std::string path);
// 获取STBCA的版本
void stbca_version_ex(OUT J_DataObject& ver)
{
	char version[128];
	stbca_version(version);

	J_DataObject trObj;
	J_NVItem root,child;

	root.name = "/";
	child.name = "version";
	child.value= version;
	
	J_DataObject::pre_order_iterator iter = trObj.set_root(root);
	trObj.append_child(iter,child);
	ver = trObj;
}

// 获取当前的CAS类型.
// test ok
void stbca_cas_type_ex(OUT J_DataObject& type)
{
	CasType casType = stbca_cas_type();

	J_DataObject trObj;
	J_NVItem root,child;

	root.name = "/";
	child.name = "casType";
	child.value= (J_U32)casType;
	
	J_DataObject::pre_order_iterator iter = trObj.set_root(root);
	trObj.append_child(iter,child);
	type = trObj;
}

// 获取STBID号.
// test ok
void stbca_get_stbid_ex(OUT J_DataObject& stb_id)
{
	WORD 	platformID; 
	ULONG 	uniqID;
	stbca_get_stbid(&platformID,&uniqID);

	J_DataObject trObj;
	J_NVItem root,child1, child2;

	root.name = "/";
	child1.name = "platformID";
	child1.value= platformID;
	child2.name = "uniqID";
	child2.value= (J_U32)uniqID;
	
	J_DataObject::pre_order_iterator iter = trObj.set_root(root);
	trObj.append_child(iter,child1);
	trObj.append_child(iter,child2);
	stb_id = trObj;
}

//==============邮件管理接口===================
// 获取指定邮件内容.
// 数码，永新不同。 (另写)
U32 stbca_get_email_content_ex(IN J_DataObject& email_id, OUT J_DataObject& email_content)
{
	J_DataObject::iterator_base itRootID = email_id.root();
	int id = itRootID->value.s32Val;

	SDVTCAEmailContent content;
	memset(&content, 0, sizeof(content));
	U32 ret = DvtCAGetEmailContent(id, &content);
	if(ret != 0)
		return ret;

	J_NVItem cont;
	cont.name = "email_content";
	cont.value = content.m_szEmail;

	dxreport("email content  id %d len %d %s\n", id, content.m_wEmailLength, content.m_szEmail);

	email_content.set_root(cont);
	return 0;
}

// 获取邮箱中的邮件数和邮箱还可以容纳的邮件数.
// test ok
U32 stbca_get_email_space_ex(OUT J_DataObject& email_space)
{
	U8 email_num, empty_num;
	U32 bRes = DvtCAGetEmailSpaceInfo(&email_num, &empty_num);//stbca_get_email_space(email_num, empty_num);
	if(bRes != 0)
		return bRes;

	J_DataObject trObj;
	J_NVItem root,child1, child2;
	root.name = "/";
	child1.name = "email_total";
	child1.value= (U32)email_num;
	child2.name = "email_new";
	child2.value= (U32)empty_num;
	
	J_DataObject::pre_order_iterator iter = trObj.set_root(root);
	trObj.append_child(iter,child1);
	trObj.append_child(iter,child2);
	email_space = trObj;
	return 0;
}
// ok;
void AppendEmailHead(J_DataObject &trObj, J_DataObject::iterator_base &iter, SDVTCAEmailInfo& emailHead)
{
	J_NVItem child1, child2, child3, child4, child5;
	child1.name = "email_id";
	child1.value = (U32)emailHead.m_dwVersion;
	child2.name = "email_length";
	child2.value = (U32)emailHead.m_wEmailLength;
	child3.name = "sender";
	child3.value = emailHead.m_szSenderName;
	child4.name = "email_title";
	child4.value = emailHead.m_szTitle;
	child5.name = "new_email";
	child5.value = (U32)(emailHead.m_Status == DVTCAS_EMAIL_STATUS_INI ? 1 : 0);
	trObj.append_child(iter,child1);
	trObj.append_child(iter,child2);
	trObj.append_child(iter,child3);
	trObj.append_child(iter,child4);
	trObj.append_child(iter,child5);
}

// 获取指定邮件的邮件头.
// 数码,永新不同,数码不用
bool stbca_get_email_head_ex(IN J_DataObject& email_id,OUT J_DataObject& email)
{

	J_U32 id = (J_U32)GetValue(email_id,"/email_id");
	EmailHead emailHead;
	bool bRes=stbca_get_email_head(id,emailHead);

	J_DataObject trObj;
	J_NVItem root,child1;

	root.name = "/";
	child1.name = "email_head";
	//child1.value= ;
	J_DataObject::pre_order_iterator iter = trObj.set_root(root);
	J_DataObject::pre_order_iterator iter1 = trObj.append_child(iter,child1);
//	AppendEmailHead(trObj, iter1, emailHead);
	email = trObj;
	
	return bRes;
}

// 获取所有邮件头.无输入,返回所有邮件信息
U32 stbca_get_email_heads_ex(OUT J_DataObject& email_heads)
{
	U16 count = 50;
	SDVTCAEmailInfo pEmails[count];	
	memset(pEmails, 0, sizeof(pEmails));
	U32 ret = DvtCAGetEmailHeads(&count, pEmails);
	if(ret != 0)
		return ret;

	//for test
//	char buf[256];
//	count = 20;

	//先写假数据
	J_NVItem ro;
	ro.name = "/";
	J_NVItem child;
	child.name = "email_head";

	J_DataObject::iterator_base itroot = email_heads.set_root(ro);
	for(int i = 0; i < count; i++)
	{
		J_DataObject::iterator_base itchild = email_heads.append_child(itroot, child);
		AppendEmailHead(email_heads, itchild, pEmails[i]);
		
		/*
		//邮件ID
		J_NVItem emailid;
		emailid.name = "email_id";
		emailid.value = i + 1;
		email_heads.append_child(itchild, emailid);

		//邮件内容
		J_NVItem emailTitle;
		emailTitle.name = "email_title";
		memset(buf, 0, sizeof(buf));
		sprintf(buf,"testEmail%d",i + 1);
		emailTitle.value = buf;
		email_heads.append_child(itchild, emailTitle);
		
		//发送时间
		J_NVItem createTime;
		createTime.name = "send_time";
		createTime.value = i + 1;
		email_heads.append_child(itchild, createTime);
		
		//是否新邮件
		J_NVItem newEmail;
		newEmail.name = "new_email";
		newEmail.value = 1;
		email_heads.append_child(itchild, newEmail);
		
		//发送者
		J_NVItem sender;
		sender.name = "sender";
		memset(buf, 0, sizeof(buf));
		sprintf(buf,"sendeer %d",i + 1);
		sender.value = buf;
		email_heads.append_child(itchild, sender);
		*/
	}
	return ret;
}

//OK
// 删除一封邮件.
U32 stbca_delete_email_ex(IN J_DataObject& email_id)
{
	
	J_U32 id = (J_U32)GetValue(email_id,"/email_id");
	stbca_delete_email(id);
	
//	J_DataObject::iterator_base itRootID = email_id.root();
//	U32 id = itRootID->value.s32Val;
	dxreport("del email id %d \n", id);
	return DvtCADelEmail(id);
}

// ================智能卡管理====================================
// GetCardID获取智能卡卡号.
// test ok
U32 stbca_GetCardIDEx(OUT J_DataObject& sn)
{
	char cardID[128] = {0};
	int iRes = stbca_GetCardID(cardID);
//	int iRes = 0;
//	strcpy(cardID,"123456");
	
	J_DataObject trObj;
	J_NVItem root, child1;
	root.name = "/";
	child1.name = "card_number";
	child1.value= cardID;
	
	J_DataObject::pre_order_iterator iter = trObj.set_root(root);
	trObj.append_child(iter,child1);
	sn = trObj;
	return iRes;
}

// 修改Pin码.
// ok;
U32  stbca_change_pin_code_ex(IN J_DataObject& pinCode)
{
	J_U8 old_code[8];

	CVariant_t vPinCode = GetValue(pinCode,"/old_code");
	if(vPinCode.vt != JVT_STRING)
	{
		dxreport("%s get old code failed\n");
		return 1;
	}
	char * pPinCode	= vPinCode; 
	strncpy((char *)old_code, pPinCode, 8);

	J_U8 new_code[8];
	vPinCode = GetValue(pinCode,"/new_code");
	if(vPinCode.vt != JVT_STRING)
	{
		dxreport("%s get new code failed\n");
		return 1;
	}
	pPinCode = (char*)vPinCode; 
	strncpy((char *)new_code, pPinCode, 8);

	U32 uRes = DvtCAChangePin((char *)old_code, (char *)new_code);
	return uRes;
}

//OK
// 获取/设置观看级别.
U32 	stbca_set_watch_rating_ex(IN  J_DataObject& level)
{
	U8 pin_code[16] = {0};
	/*
	char *pPinCode	= (char *)GetValue(level,"/pin_code");
	strncpy((char *)pin_code, pPinCode, 8);
	J_U8 rate = (J_U8)GetValue(level,"/rating");
	*/
	
	U8 rate = 0;
	J_DataObject::iterator_base it = level.root(); 
	tree<J_NVItem>::children_iterator itc = it.begin_children_iterator();
	tree<J_NVItem>::children_iterator eitc = it.end_children_iterator();
	while(itc != eitc)
	{
		if(itc->name == "pin_code")
		{
			strncpy((char *)pin_code, itc->value.pStrVal->strVal, 8);
		}
		else if(itc->name == "rating")
		{
			rate = (U32)itc->value;
		}
		++itc;
	}
	U32 uRes = DvtCASetRating((char *)pin_code, rate);//stbca_set_watch_rating(pin_code, rate);
	return uRes;
}

//OK
void  stbca_get_watch_rating_ex(OUT J_DataObject& level)
{
	U8 rating =	stbca_get_watch_rating();

	J_DataObject trObj;
	J_NVItem root, child1;
	root.name = "/";
	child1.name = "rating";
	child1.value= rating;
	
	J_DataObject::pre_order_iterator iter = trObj.set_root(root);
	trObj.append_child(iter,child1);
	level = trObj;
}

//OK
// 设置/获取观看时段.
U32  stbca_set_watch_time_ex(IN  J_DataObject& time)
{
	/*
	J_U8 pin_code[8];
	char *pPinCode	= (char *)GetValue(time,"/pin_code");
	strncpy((char *)pin_code, pPinCode, 8);
	J_U8 start_hour = (J_U8)GetValue(time,"/start_hour");
	J_U8 start_min = (J_U8)GetValue(time,"/start_min");
	J_U8 start_sec = (J_U8)GetValue(time,"/start_sec");
	J_U8 end_hour = (J_U8)GetValue(time,"/end_hour");
	J_U8 end_min = (J_U8)GetValue(time,"/end_min");
	J_U8 end_sec = (J_U8)GetValue(time,"/end_sec");
	*/

	J_U8 start_hour = 0, start_min = 0, start_sec = 0 ,
		 end_hour = 0,end_min = 0 ,end_sec = 0;
	J_U8 pin_code[16] = {0};
	
	J_DataObject::iterator_base it = time.root(); 
	tree<J_NVItem>::children_iterator itc = time.begin_children_iterator(it);
	tree<J_NVItem>::children_iterator eitc = time.end_children_iterator(it);
	while(itc != eitc)
	{
		dxreport("name %s\n",itc->name.c_str());
		if(itc->name == "pin_code")
		{
		//	dxreport("pin_code %s\n",itc->value.pStrVal->strVal);
			strncpy((char *)pin_code, itc->value, 8);
		}
		else if(itc->name == "start_hour")
		{
			start_hour = (U32)itc->value;
		}
		else if(itc->name == "start_min")
		{
			start_min = (U32)itc->value;
		}
		else if(itc->name == "end_hour")
		{
			end_hour = (U32)itc->value;
		}
		else if(itc->name == "end_min")
		{
			end_min = (U32)itc->value;
		}
		else if(itc->name == "start_sec")
		{
			start_sec = (U32)itc->value;
		}
		else if(itc->name == "end_sec")
		{
			end_sec = (U32)itc->value;
		}
		++itc;
	}
	dxreport("start_hour %d start_min %d start_sec %d end_hour %d end_min %d end_sec %d\n",
			start_hour, start_min, start_sec, end_hour, end_min, end_sec);
	U32 uRes = DvtCASetWorkTime((char *)pin_code, start_hour, start_min, start_sec,\
			end_hour, end_min, end_sec);
	
	return uRes;
}

//OK
U32  stbca_get_watch_time_ex(OUT J_DataObject& time)
{
	U8 start_hour, start_min, start_sec;
	U8 end_hour, end_min, end_sec;
	U32 uRes = stbca_get_watch_time2(start_hour, start_min, start_sec,\
			end_hour, end_min, end_sec);

	J_DataObject trObj;
	J_NVItem root, child1, child2, child3, child4, child5, child6;
	root.name = "/";
	child1.name = "start_hour";
	child1.value= start_hour;
	child2.name = "start_min";
	child2.value= start_min;
	child3.name = "start_sec";
	child3.value= start_sec;
	child4.name = "end_hour";
	child4.value= end_hour;
	child5.name = "end_min";
	child5.value= end_min;
	child6.name = "end_sec";
	child6.value= end_sec;

	J_DataObject::pre_order_iterator iter = trObj.set_root(root);
	trObj.append_child(iter,child1);
	trObj.append_child(iter,child2);
	trObj.append_child(iter,child3);
	trObj.append_child(iter,child4);
	trObj.append_child(iter,child5);
	trObj.append_child(iter,child6);
	time = trObj;
	return uRes;
}

// 未实现
U32  stbca_set_paired_ex(IN J_DataObject& num)
{
	J_U8 pin_code[8];
	char *pPinCode	= (char *)GetValue(num,"/pin_code");
	strncpy((char *)pin_code, pPinCode, 8);
	U32 uRes = stbca_set_paired(pin_code);
	return uRes;
}

//=======================================================================
void AppendOperatorID(J_DataObject &trObj, J_DataObject::pre_order_iterator &iter, OperatorId& operatorID)
{
	J_NVItem child1;
	child1.name = "operator_id";
	child1.value = operatorID;
	trObj.append_child(iter,child1);
}
// 获取运营商ID序列 ok
U32 stbca_get_operator_ids_ex(OUT J_DataObject& operator_ids)
{
	//永新 数码 不通用
//	vector<OperatorId> ids;
//	U16 uRes = stbca_get_operator_ids(ids);
	//AppendOperatorID(trObj, iter1, ids[i]);
	
	SDVTCATvsInfo info[5];
	U8 count = 5;
	U32 ret = DvtCAGetOperatorInfo(&count,info);
	if(ret != 0)
		return ret;

	J_DataObject trObj;
	J_NVItem root,child1;
	root.name = "/";
	child1.name = "operator_info";

	J_DataObject::pre_order_iterator iter = trObj.set_root(root);
	J_DataObject::pre_order_iterator iter1 = trObj.append_child(iter,child1);
	for(size_t i=0; i < count; i++)
	{
		J_NVItem child2,child3;
		child2.name = "operator_id";
		child2.value = info[i].m_wTVSID;
		child3.name = "operator_name";
		child3.value = info[i].m_szTVSName;
		
		trObj.append_child(iter1, child2);	
		trObj.append_child(iter1, child3);	
	}
	operator_ids = trObj;

	return ret;
}

//获取运营商信息. 数码不需要
U32 stbca_get_operator_info_ex(IN J_DataObject& o_operator_id, OUT J_DataObject& o_info)
{
//	 OperatorId id;
//	 OperatorInfo info;
//	stbca_get_operator_info(id, info);
	return 1;
}

// 获取运营商的特征值. 数码没有
U32 stbca_get_operator_acs_ex(IN J_DataObject& operator_id, OUT J_DataObject& acs)
{
	return 1;
}
// ok
inline static void AppendEntitles(J_DataObject & tre, J_DataObject::iterator_base & it, SDVTCAServiceEntitle * pEntitle)
{
	J_NVItem child1, child2, child3, child4, child5, child6;

	child1.name = "product_id";
	child1.value = (U32)pEntitle->m_wProductID;
	child2.name = "product_name";
	child2.value = pEntitle->m_szProductName;
	child3.name = "begin_date";
	child3.value = (U32)pEntitle->m_tStartTime;
	child4.name = "expired_date";
	child4.value = (U32)pEntitle->m_tEndTime;
	child5.name = "entitle_time";
	child5.value = (U32)pEntitle->m_tEntitleTime;
	child6.name = "tape_flag";
	child6.value = (U32)(pEntitle->m_bTapingFlag ? 1 : 0);

	tre.append_child(it, child1);
	tre.append_child(it, child2);
	tre.append_child(it, child3);
	tre.append_child(it, child4);
	tre.append_child(it, child5);
	tre.append_child(it, child6);

}

// 获取授权列表
// ok
U32 stbca_get_service_entitles_ex(IN J_DataObject& operator_id, OUT J_DataObject& entitles)
{
	dxreport("Enter %s\n",__FUNCTION__);

	U32 id = (U32)GetValue(operator_id,"/operator_id");

	SDVTCAServiceEntitle pEntitle[250];
	memset(pEntitle, 0, sizeof(pEntitle));
	U8 count = 250;
	U32 ret = DvtCAGetServiceEntitles(id, &count, pEntitle);
	if(ret != 0)
		return ret;


	J_NVItem root;
	root.name = "/";

	J_DataObject::pre_order_iterator iter = entitles.set_root(root);
	for(int i = 0; i < count; i++)
	{
		J_NVItem child;
		child.name = "service_entitles";
		J_DataObject::pre_order_iterator iter1 = entitles.append_child(iter,child);
		AppendEntitles(entitles, iter1, pEntitle + i);
	}
	dxreport("Leave %s %d\n",__FUNCTION__, sizeof(pEntitle));
	return ret;
}

// 获取钱包ID列表
U32 stbca_get_purse_ids_ex(IN J_DataObject& operator_id,OUT J_DataObject& purse_ids)
{
	return 1;
}

// 获取钱包信息.
U32 stbca_get_purse_info_ex(IN J_DataObject& purse_id,OUT J_DataObject& purse_info);



//OK
static U32 DoGetCASInfo(J_DataObject & in, J_DataObject & out) 
{
	//in无信息
	
	dxreport("Enter %s\n",__FUNCTION__);

	SDVTCAManuInfo manu;
	memset(&manu, 0, sizeof(SDVTCAManuInfo));
	U32 ret = DvtCASManuInfo(&manu);
	if(ret != 0)
		return ret;

	J_NVItem ro;
	ro.name = "/";
	
	J_DataObject::iterator_base itRoot = out.set_root(ro);
	J_NVItem child1, child2, child3, child4;

	child1.name = "card_id";
	child2.name = "stbcas_ver";
	child3.name = "sccos_ver";
	child4.name = "sccas_name";

	child1.value = (U32)manu.m_dwCardID;
	child2.value = (U32)manu.m_dwSTBCASVer;
	child3.value = (U32)manu.m_dwSCCOSVer;
	child4.value = manu.m_szSCCASManuName;

	out.append_child(itRoot, child1);
	out.append_child(itRoot, child2);
	out.append_child(itRoot, child3);
	out.append_child(itRoot, child4);

	dxreport("Leave %s\n",__FUNCTION__);

	return ret;
}

//OK
static U32 DoGetPinLocked(J_DataObject & in, J_DataObject & out) 
{
	dxreport("Enter %s\n",__FUNCTION__);

	bool result = false;
	bool ret = DvtCAIsPinLocked(&result);
	if(ret == false)
		return 1;

	J_NVItem ro, child;
	ro.name = "/";

	child.name = "locked";
	child.value = (result ? 1 : 0);

	J_DataObject::iterator_base it = out.set_root(ro);
	out.append_child(it, child);

	dxreport("Leave %s\n",__FUNCTION__);
	return 0;
}

//OK
static U32 DoGetAreaInfo(J_DataObject & in, J_DataObject & out) 
{
	dxreport("Enter %s\n",__FUNCTION__);

	SDVTCAAreaInfo areaInfo;
	memset(&areaInfo, 0, sizeof(SDVTCAManuInfo));
	long ret = DvtCAGetAreaInfo(&areaInfo);
	if(ret != 0)
		return 1;

	J_NVItem ro;
	ro.name = "/";
	
	J_DataObject::iterator_base itRoot = out.set_root(ro);
	J_NVItem child1, child2, child3;

	child1.name = "card_area";
	child2.name = "start_flag";
	child3.name = "stream_time";

	child1.value = (U32)areaInfo.m_dwCardArea;
	child2.value = (U32)(0xFF & areaInfo.m_byStartFlag);
	child3.value = (U32)areaInfo.m_tSetStreamTime;

	out.append_child(itRoot, child1);
	out.append_child(itRoot, child2);
	out.append_child(itRoot, child3);

	dxreport("Leave %s\n",__FUNCTION__);

	return 0;
}

//ok
static U32 DoGetMontherInfo(J_DataObject & in, J_DataObject & out) 
{
	dxreport("Enter %s\n",__FUNCTION__);

	DWORD info = 0;
	long ret = DvtCAGetMotherInfo(&info);
	if(ret != 0)
		return 1;


	J_NVItem ro,child;
	ro.name = "/";
	child.name = "mother_cardid";
	child.value = (U32)info;
	J_DataObject::iterator_base it = out.set_root(ro);
	out.append_child(it, child);

	dxreport("Leave %s\n",__FUNCTION__);
	return 0;
}

U32 DoEmailRead(J_DataObject & in, J_DataObject & out)
{
	U32 id = (U32)GetValue(in,"/email_id");
	dxreport("%s id %d\n",__FUNCTION__, id);
	return DvtCAEmailRead(id);
}

U32 DoShowOSDOver(J_DataObject & in, J_DataObject & out)
{
	U32 duration = (U32)GetValue(in, "/show_duration");
	dxreport("%s duration %d\n",__FUNCTION__, duration);
	return DvtCAShowOSDOver(duration);
}

U32 DoSetCorrespondInfo(J_DataObject & in, J_DataObject & out)
{
	J_Binary * pData = (J_Binary*)GetValue(in, "data_info"); 
	
	return DvtCASetCorrespondInfo(pData->uSize, pData->pByte);

//	dxreport("%s len:%d data:%hhd %hhd %hhd %hhd %hhd\n",__FUNCTION__, pData->uSize, 
//			pData->pByte[0], pData->pByte[1],pData->pByte[2],pData->pByte[3],pData->pByte[4]);
//	return 1;
}

void printTree(const tree<J_NVItem> & tre)
{

	tree<J_NVItem>::pre_order_iterator it = tre.begin_pre_order_iterator(tre.root());
	tree<J_NVItem>::pre_order_iterator eit = tre.end_pre_order_iterator(tre.root());
	while(it != eit)
	{
		dxreport("%s\n",it->name.c_str());
		if(it->value.vt == JVT_STRING)
			dxreport("vt:%d val:%s\n",it->value.vt, it->value.pStrVal->strVal);
		else if(it->value.vt == JVT_BINARY)
			dxreport("vt:%d val size:%d\n",it->value.vt, it->value.pBinary->uSize);
		else
		{
			dxreport("vt:%d val:%d\n",it->value.vt, it->value.s32Val);
		}
		fflush(stdout);
		++it;
	}
}


U32 DoGetCorrespondInfo(J_DataObject & in, J_DataObject & out)
{
	dxreport("Enter %s\n",__FUNCTION__);

	J_NVItem ro,child;
	ro.name = "/";
	child.name = "data_info";
	
	char buf[250];
	U8 len = 250;
	U32 ret = DvtCAGetCorrespondInfo(&len, (U8*)buf);	
	if(ret != 0)
		return ret;
//	char buf[5] = {0x1,0x2,0x3,0x4,0x5};

	CVariant_t var((J_BYTE*)buf, len);
	child.value = var;
	J_DataObject::iterator_base it = out.set_root(ro);
	out.append_child(it, child);

	dxreport("Leave %s\n",__FUNCTION__);
	return 0;
}


U32 DoGetPurseInfo(J_DataObject & in , J_DataObject & out)
{
	U32 id = (U32)GetValue(in,"/operator_id");
	U32 balance = 0;
	U32 remainder = 0;
	U32 ret = DvtCAGetPurseInfo(id, &balance, &remainder);
	if(ret != 0)
		return ret;

	//返回3级节点
	J_NVItem ro,child;
	ro.name = "/";
	child.name = "purses";
	J_DataObject::iterator_base it = out.set_root(ro);
	J_DataObject::iterator_base itChild2 = out.append_child(it, child);

	J_NVItem child3_1, child3_2;
	child3_1.name = "all_balance";
	child3_1.value = balance;
	child3_2.name = "remainder";
	child3_2.value = remainder;

	out.append_child(itChild2, child3_1);
	out.append_child(itChild2, child3_2);
	return ret;
}


void AppendIpps(J_DataObject & tre, J_DataObject::iterator_base & it, SDVTCAIpp * pIpp)
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

//	U8 status = (~(0x08 | 0x02));
//	child1.value = 690; 
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
//	child16.value = status; 
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

U32 DoGetIpps(J_DataObject & in, J_DataObject & out)
{
	U8 count = 250;
	SDVTCAIpp pIpps[250];
	memset(pIpps, 0, sizeof(pIpps));
	U32 ret = DvtCAGetIpps(&count, pIpps);
	if(ret != 0)
		return ret;

	J_NVItem ro,child;
	ro.name = "/";
	child.name = "book_ipp";
	J_DataObject::iterator_base it = out.set_root(ro);
	for(int i = 0; i < count; i++)
	{
		J_DataObject::iterator_base itChild = out.append_child(it, child);
		AppendIpps(out, itChild, pIpps + i);
	}
	return ret;
}


U32 DoBookIpp(J_DataObject & in, J_DataObject & out)
{
	SDVTCAIpp ipp;
	memset(&ipp, 0, sizeof(ipp));
	
	printTree(in);

	ipp.m_wTVSID = (J_U32)GetValue(in,"/book_ipp/operator_id");
	ipp.m_wProdID = (J_U32)GetValue(in,"/book_ipp/prod_id");
	ipp.m_bySlotID = (U32)GetValue(in, "/book_ipp/slot_id");
	char * prodName = GetValue(in, "/book_ipp/prod_name");
	strcpy(ipp.m_szProdName,prodName);
	ipp.m_tStartTime = (U32)GetValue(in, "/book_ipp/start_time");
	ipp.m_dwDuration = (U32)GetValue(in, "/book_ipp/duration_time");
	char * serviceName = GetValue(in, "/book_ipp/service_name");
	strcpy(ipp.m_szServiceName, serviceName);
	ipp.m_wCurTppTapPrice = (U32)GetValue(in, "/book_ipp/curtpp_tapprice");
	ipp.m_wCurTppNoTapPrice = (U32)GetValue(in, "/book_ipp/curtpp_notapprice");
	ipp.m_wCurCppTapPrice = (U32)GetValue(in, "/book_ipp/curcpp_tapprice");
	ipp.m_wCurCppNoTapPrice = (U32)GetValue(in, "/book_ipp/curcpp_notapprice");
	ipp.m_wBookedPrice = (U32)GetValue(in, "/book_ipp/booked_price");
	ipp.m_byBookedPriceType = (U32)GetValue(in, "/book_ipp/booked_pricetype");
	ipp.m_byCurInterval = (U32)GetValue(in, "/book_ipp/booked_interval");
	ipp.m_byCurInterval = (U32)GetValue(in, "/book_ipp/cur_interval");
	ipp.m_byIppStatus = (U32)GetValue(in, "/book_ipp/ipp_status");
	ipp.m_byUnit = (U32)GetValue(in, "/book_ipp/unit");
	ipp.m_wIpptPeriod = (U32)GetValue(in, "/book_ipp/ippt_period");

	return DvtCABookIpp(&ipp);
}



void AppendViewedIpp(J_DataObject &trObj, J_DataObject::iterator_base &iter, SDVTCAViewedIpp& viewedIpp)
{
	J_NVItem child1, child2, child3, child4;
	J_NVItem child5, child6, child7, child8, child9;

	child1.name = "operator_id";
	child1.value = (U32)viewedIpp.m_wTVSID;
	child2.name = "prod_name";
	child2.value = viewedIpp.m_szProdName;
	child3.name = "start_time";
	child3.value = (U32)viewedIpp.m_tStartTime;
	child4.name = "duration_time";
	child4.value = (U32)viewedIpp.m_dwDuration;
	child5.name = "booked_price";
	child5.value = (U32)viewedIpp.m_wBookedPrice;
	child6.name = "booked_pricetype";
	child6.value = (U32)viewedIpp.m_byBookedPriceType;
	child7.name = "booked_interval";
	child7.value = (U32)viewedIpp.m_byBookedInterval;
	child8.name = "other_info";
	child8.value = viewedIpp.m_szOtherInfo;
	child9.name = "unit";
	child9.value = (U32)viewedIpp.m_byUnit;

//	child1.value = 1;
//	child2.value = "fdafdsfaf"; 
//	child3.value = 2; 
//	child4.value = 3; 
//	child5.value = 4; 
//	child6.value = 5; 
//	child7.value = 6; 
//	child8.value = "other_info"; 
//	child9.value = 7;  

	trObj.append_child(iter, child1);
	trObj.append_child(iter, child2);
	trObj.append_child(iter, child3);
	trObj.append_child(iter, child4);
	trObj.append_child(iter, child5);
	trObj.append_child(iter, child6);
	trObj.append_child(iter, child7);
	trObj.append_child(iter, child8);
	trObj.append_child(iter, child9);
}

U32 DoGetViewedIpps(J_DataObject & in, J_DataObject & out)
{
	U8 count = 250;
	SDVTCAViewedIpp pIpps[250];
	memset(&pIpps, 0, sizeof(pIpps));

	U32 ret = DvtCAGetViewedIpps(&count, pIpps);
	if(ret != 0)
		return ret;

	J_NVItem root;
	root.name = "/";
	J_NVItem child;
	child.name = "viewed_ipp";

	J_DataObject::pre_order_iterator iter = out.set_root(root);
	for(int i = 0; i < count; i++)
	{
		J_DataObject::pre_order_iterator iter1 = out.append_child(iter,child);
		AppendViewedIpp(out, iter1, pIpps[i]);
	}

	return 0;
}

U32 DoSetDebugSign(J_DataObject & in, J_DataObject & out)
{
	bool sign = (U32)GetValue(in, "/debug_sign");
	return DvtCAAddDebugMsgSign(sign);
}


U32 DoVerifyPin(J_DataObject & in, J_DataObject & out)
{
	J_U8 code[16] = {0};
	char *pPinCode	= (char *)GetValue(in,"/pin_code");
	strncpy((char *)code, pPinCode, 8);
	return DvtCAVerifyPin(code);
}

U32 DoGetSmartCardStatus(J_DataObject & in, J_DataObject & out)
{

	U8 status = 0;
	U32 ret = stbca_get_CardStatus(&status);
	if(ret != 0)
		return ret;

	J_NVItem root;
	root.name = "/";
	J_NVItem child;
	child.name = "smartcard_status";
	child.value = status;
	
	J_DataObject::pre_order_iterator iter = out.set_root(root);
	out.append_child(iter, child);

	return 0;
}

U32 DoInquireBookIppOver(J_DataObject & in, J_DataObject & out)
{
	U32 ecm = (U32)GetValue(in,"/ecm_pid");
	dxreport("Enter %s ecm = %u\n",__FUNCTION__, ecm);
	return DvtCAInquireBookIppOver(ecm);
}

U32 DoMotherCardPairOver(J_DataObject & in, J_DataObject & out)
{
	return DvtCAMotherCardPairOver();
}


//bool GetConfigs(std::map<std::string, std::string> & confs)
//{
//	FILE *fp=fopen(MONITOR_VERSION_FILE_PATH,"r");
//	if(fp == NULL)
//	{
//		dxreport("%s open %s failed!\n",__FUNCTION__, MONITOR_VERSION_FILE_PATH);
//		return false;
//	}
//	fseek(fp,0,SEEK_SET);
//
//	char buf[256];
//	while(!feof(fp))
//	{
//		memset(buf, 0, sizeof(buf));
//		char * pKV = fgets(buf,255,fp);
//		if(pKV == NULL)
//		{
//			dxreport("%s fgets return null,break\n",__FUNCTION__);
//			break;
//		}
//
//		char * pV = strchr(pKV, ':');
//		if(pV != NULL)
//		{
//			*pV = '\0';
//			++pV;
//		}
//		else
//		{
//			pV = "";
//		}
//
//		std::string key = pKV;
//		std::string value = pV;
//		confs.insert(std::map<string,string>::value_type(key,value));
//	}
//
//	fclose(fp);
//	return true;
//}
//
//bool AddConfig(std::string key, std::string value)
//{
//}


#define AREALOCk_CONFIG_FILE "/data/data/novel.supertv.dvb/arealock.conf"

extern bool GetFreqInfo(U32 & freq, U32 & symb, U32 & qam);

bool SetFreqInfo(U32 freq, U32 symb, U32 qam)
{
	FILE *fp=fopen(AREALOCk_CONFIG_FILE,"w");
	if(fp)
	{
		fprintf(fp,"freq:%d\n",freq);
		fprintf(fp,"symb:%d\n",symb);
		fprintf(fp,"qam:%d\n",qam);
		fclose(fp);
	}
	return true;
}

U32 DoGetAreaLockFreq(J_DataObject & in, J_DataObject & out)
{

	U32 freq, symb, qam; 
	GetFreqInfo(freq, symb, qam);

	J_NVItem root;
	root.name = "/";
	J_NVItem child1, child2, child3;
	child1.name = "freq";
	child1.value = (U32)freq;
	
	child2.name = "symb";
	child1.value = (U32)symb;

	child1.name = "qam";
	child1.value = (U32)symb;


	J_DataObject::pre_order_iterator iter = out.set_root(root);
	out.append_child(iter, child1);
	out.append_child(iter, child2);
	out.append_child(iter, child3);
	return 0;

}

U32 DoSetAreaLockFreq(J_DataObject & in, J_DataObject & out)
{
	U32 freq = (U32)GetValue(in, "/freq");
	U32 symb = (U32)GetValue(in, "/symb");
	U32 qam = (U32)GetValue(in, "/qam");
	SetFreqInfo(freq, symb, qam);
	return 0;
}

U32 stbca_get_parameters(IN int fnID,IN J_DataObject& input,OUT J_DataObject& output)
{
	dxreport("Enter %s key %d\n",__FUNCTION__, fnID);
	U32 ret = 1;
	switch(fnID)
	{
		case ParamKey_GetCASInfo:
			ret = DoGetCASInfo(input, output);
			break;
		case ParamKey_GetPinLocked:
			ret = DoGetPinLocked(input, output); 
			break;
		case ParamKey_GetAreaInfo:
			ret = DoGetAreaInfo(input, output);
			break;
		case ParamKey_GetMontherInfo:
			ret = DoGetMontherInfo(input, output);
			break;
		case ParamKey_CorrespondInfo:
			ret = DoGetCorrespondInfo(input, output);
			break;
		case ParamKey_PurseInfo:
			ret = DoGetPurseInfo(input, output);
			break;
		case ParamKey_Ipps:
			ret = DoGetIpps(input, output);
			break;
		case ParamKey_ViewedIpp:
			ret = DoGetViewedIpps(input, output);
			break;
		case ParamKey_SmartCardStatus:
			ret = DoGetSmartCardStatus(input, output);
			break;
		case ParamKey_AreaLockFreq:
			ret = DoGetAreaLockFreq(input, output);
			break;
		default:
			break;
	}
	dxreport("Leave %s\n",__FUNCTION__);
	return ret;
}

U32 stbca_set_parameters(IN int fnID,IN J_DataObject& input,OUT J_DataObject& output)
{
	dxreport("Enter %s key %d\n",__FUNCTION__, fnID);
	U32 ret = 1;
	switch(fnID)
	{
		case ParamKey_CorrespondInfo:
			ret = DoSetCorrespondInfo(input, output);
			break;
		case ParamKey_EmailRead:
			ret = DoEmailRead(input, output);
			break;
		case ParamKey_ShowOSDOver:
			ret = DoShowOSDOver(input, output);
			break;
		case ParamKey_BookIpp:
			ret = DoBookIpp(input, output);
			break;
		case ParamKey_DebugSign:
			ret = DoSetDebugSign(input, output);
			break;
		case ParamKey_VerifyPin:
			ret = DoVerifyPin(input, output);
			break;
		case ParamKey_InquireBookIppOver:
			ret = DoInquireBookIppOver(input, output);
			break;
		case ParamKey_MotherCardPairOver:
			ret = DoMotherCardPairOver(input, output);
			break;
		case ParamKey_AreaLockFreq:
			ret = DoSetAreaLockFreq(input, output);
			break;
		default:
			break;
	}
	dxreport("Leave %s\n",__FUNCTION__);
	return ret;
}

////////////////////////////////////////////////////////////////////////////////
// 获取object 节点数据需要的 data object 扩展
////////////////////////////////////////////////////////////////////////////////

//根据路径vector，一步一步的往下找节点，直到找到目标
CVariant_t /*tree<J_NVItem>::iterator_base*/ GetValue(tree<J_NVItem>& tr, std::string path)
{
	std::string delimiter ="/";
	std::vector<std::string> vecPath;
	split(path,delimiter,vecPath );

	J_DataObject::pre_order_iterator itRoot = tr.root();
	tree<J_NVItem>:: iterator_base itNode = itRoot;

	for (std::vector<std::string>::iterator itPath = vecPath.begin();
		itPath != vecPath.end(); itPath++)
	{
		itNode = FindNodeByString(tr,itNode,*itPath);
	}
	if(itNode.node != NULL)
	{
		return itNode->value;
	}
	else
	{
		return INVALID_VALUE;
	}

//	return itNode->value;
}
//在节点下，遍历所有的子节点，找到与tag匹配的节点。
//找到则，打印，否则返回空。
tree<J_NVItem>::iterator_base FindNodeByString(tree<J_NVItem>& tr,tree<J_NVItem>::iterator_base it,std::string tag)
{
	if (tag ==it.node->data.name)
	{
		return it;
	}

	tree<J_NVItem>::children_iterator itChild = tr.begin_children_iterator(it);
	for( ; itChild != tr.end_children_iterator(it);  itChild++)
	{
		if (tag == itChild.node->data.name)
		{
//			std::cout<<"找到了目标"<< itChild.node->data.name <<std::endl;
			return itChild;
		}
	}

	return itChild; // tr.end_children_iterator(it);
}
//将data字符串，按照 split char的分隔符，进行分割。分割的片段存在 vector中。
void split(std::string data , std::string splt, std::vector<std::string>& result)
{
	size_t begin=0,end;
	if(data[0]=='/')
	{
		result.push_back("/");  // 增加一个根元素
		begin=1;
	}
	while(1)
	{
		end = data.find_first_of(splt,begin);
		if(end == std::string::npos) break;
		result.push_back(data.substr(begin,end-begin));
		begin = end + 1;
	}

	//存储分割剩余的子串
	if (begin != data.size())
	{
		result.push_back(data.substr(begin,data.size() - begin));
	}
}
		
