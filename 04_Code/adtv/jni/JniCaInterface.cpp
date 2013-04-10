#include <jni.h>
#include <android/log.h>

#include <string.h>
#include <stdio.h>
#include <stdlib.h>

#include "tvcore_ex.h"


#define JCLSNAME_MultiTree		"novel/supertv/dvb/jni/struct/MultiTree"
#define JCLSNAME_TreeNode		"novel/supertv/dvb/jni/struct/MultiTree$Node"
#define JCLSTYPE_TreeNode		"Lnovel/supertv/dvb/jni/struct/MultiTree$Node;"

#define JCLSNAME_String			"java/lang/String"
#define JCLSTYPE_String			"Ljava/lang/String;"
#define JCLSNAME_Object			"java/lang/Object"
#define JCLSTYPE_Object			"Ljava/lang/Object;"
#define JCLSNAME_Integer		"java/lang/Integer"
#define JCLSTYPE_Integer		"Ljava/lang/Integer;"
#define JCLSNAME_ArrayList		"java/util/ArrayList"
#define JCLSTYPE_ArrayList		"Ljava/util/ArrayList;"


#define  LOG_TAG    "CaInterface"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

jstring CreateJstringFromGB2312(JNIEnv * env, const char * pStr);

//{{novel_supertv_dvb_jni_JniChannelPlay.cpp中定义
extern jclass gClsMultiTree;
extern jclass gClsTreeNode;
//novel_supertv_dvb_jni_JniChannelPlay.cpp中定义}}

static inline jobject CreateIntegerObj(JNIEnv * env, int param)
{
	jclass clsInt = env->FindClass(JCLSNAME_Integer);
	jmethodID conInt_mid = env->GetMethodID(clsInt, "<init>", "(I)V");
	jobject retObj = env->NewObject(clsInt, conInt_mid, param);
	env->DeleteLocalRef(clsInt);
	return retObj;
}

static inline jobject CreateByteArray(JNIEnv * env, char * pData, int len)
{
	jbyteArray array = env->NewByteArray(len);
	env->SetByteArrayRegion(array,0,len,(jbyte*)pData);
	return array;
}


static inline jfieldID GetJRootFieldID(JNIEnv * env)
{
//	jclass clsTree = env->FindClass(JCLSNAME_MultiTree);
	jfieldID root_id = env->GetFieldID(gClsMultiTree, "mRoot", JCLSTYPE_TreeNode);
	return root_id;
}


struct JMultiTreeNode
{
	jfieldID m_Type_id;
	jfieldID m_Name_id;
	jfieldID m_Data_id;
	jfieldID m_Childs_id;

	jmethodID m_Construct_mid;

	bool initFieldIds(JNIEnv * env, jclass clsTreeNode)
	{
		m_Type_id = env->GetFieldID(clsTreeNode,"mType","I");
		m_Name_id = env->GetFieldID(clsTreeNode,"mName",JCLSTYPE_String);
		m_Data_id = env->GetFieldID(clsTreeNode,"mData",JCLSTYPE_Object);
		m_Childs_id = env->GetFieldID(clsTreeNode,"mChilds",JCLSTYPE_ArrayList);

		m_Construct_mid = env->GetMethodID(clsTreeNode, "<init>", "()V");
		return true;
	}

	void cpp2Java(JNIEnv * env, const J_NVItem & item, jobject & jObj) const
	{
//		LOGI("Enter %s \n",__FUNCTION__);
		jobject jvalue = NULL;
		//处理值
		switch(item.value.vt)
		{
			case JVT_EMPTY:
				break;
			case JVT_U8:
			case JVT_S8:
				jvalue = CreateIntegerObj(env, 0xFF & item.value.s8Val);
				env->SetIntField(jObj, m_Type_id, JVT_U32);
				break;
			case JVT_U16:
			case JVT_S16:
				jvalue = CreateIntegerObj(env, 0xFFFF & item.value.s16Val);
				env->SetIntField(jObj, m_Type_id, JVT_U32);
				break;
			case JVT_U32:
			case JVT_S32:
				jvalue = CreateIntegerObj(env, item.value.s32Val);
				env->SetIntField(jObj, m_Type_id, JVT_U32);
				break;
			case JVT_BOOL:
				jvalue = CreateIntegerObj(env, item.value.bVal);
				env->SetIntField(jObj, m_Type_id, JVT_U32);
				break;
			case JVT_FLOAT:
				break;
			case JVT_DOUBLE:
				break;
			case JVT_STRING:
				{
					env->SetIntField(jObj, m_Type_id, JVT_STRING);
				//	jstring jstr = env->NewStringUTF(item.value.pStrVal->strVal);
					jstring jstr = CreateJstringFromGB2312(env, item.value.pStrVal->strVal);
					jvalue = jstr;
				}
				break;
			case JVT_BINARY:
				{
//					LOGI("%s Binary size %d\n",__FUNCTION__, item.value.pBinary->uSize);
					jvalue = CreateByteArray(env, (char*)item.value.pBinary->pByte, item.value.pBinary->uSize);
					env->SetIntField(jObj, m_Type_id, JVT_BINARY);
				}
				break;
			default:
					break;
		}
		if(jvalue != NULL)
		{
			env->SetObjectField(jObj, m_Data_id, jvalue);
			env->DeleteLocalRef(jvalue);
		}
		//处理名字
        jstring jname = env->NewStringUTF(item.name.c_str());
		env->SetObjectField(jObj, m_Name_id, jname);
		env->DeleteLocalRef(jname);

		//因有多个子节点需单独处理.
//		LOGI("Leave %s \n",__FUNCTION__);
	}
	
	void java2Cpp(JNIEnv * env, const jobject & jObj, J_NVItem & item) const
	{
//		J_NVItem itemTemp;
//		LOGI("Enter %s\n",__FUNCTION__);
	    jclass clsInt = env->FindClass(JCLSNAME_Integer);
		jfieldID intValue_id = env->GetFieldID(clsInt, "value", "I");	

		//处理type
		item.value.vt = env->GetIntField(jObj, m_Type_id);
//		LOGI("value.vt %d\n",item.value.vt);

		//处理数据
		jobject jdata = NULL;
		if(item.value.vt != JVT_EMPTY)
			jdata = env->GetObjectField(jObj, m_Data_id);

//		LOGI("jadta  %p\n",jdata);
		//处理值
		switch(item.value.vt)
		{
			case JVT_EMPTY:
				break;
			case JVT_U8:
			case JVT_S8:
			case JVT_U16:
			case JVT_S16:
			case JVT_U32:
			case JVT_S32:
			case JVT_BOOL:
				{
					item.value.s32Val = env->GetIntField(jdata, intValue_id);
				}
				break;
			case JVT_FLOAT:
				break;
			case JVT_DOUBLE:
				break;
			case JVT_STRING:
				{
					const char * pData = env->GetStringUTFChars((jstring)jdata, JNI_FALSE); 
					item.value = pData;
					env->ReleaseStringUTFChars((jstring)jdata,pData);
				}
				break;
			case JVT_BINARY:
				{
					int len = env->GetArrayLength((jbyteArray)jdata);
					char *buf = (char *)env->GetByteArrayElements((jbyteArray)jdata, 0);

					CVariant_t var((J_BYTE*)buf, len);
					item.value = var;
					
					env->ReleaseByteArrayElements((jbyteArray)jdata,(jbyte*)buf,0);
				}
				break;
			default:
					break;
		}

		//处理名字
		jstring jname = (jstring)env->GetObjectField(jObj, m_Name_id);
//		LOGI("jname %p\n",jname);

		const char * pName = env->GetStringUTFChars(jname, JNI_FALSE); 
		item.name = pName; 
//		LOGI("name %s\n",pName);
		env->ReleaseStringUTFChars(jname,pName);
		env->DeleteLocalRef(jname);


		if(jdata != NULL)
			env->DeleteLocalRef(jdata);

		env->DeleteLocalRef((jobject)clsInt);
//		LOGI("Leave %s\n",__FUNCTION__);
	}

};//struct JMultiTreeNode

static inline void InitTreeFieldIds(JNIEnv * env, JMultiTreeNode & fields)
{
//	jclass clsNode = env->FindClass(JCLSNAME_TreeNode);
	fields.initFieldIds(env, gClsTreeNode);
}

void treeToJava(JNIEnv * env, const tree<J_NVItem>::iterator_base & it, jobject & jnode, const JMultiTreeNode & fields)
{
//	LOGI("Enter %s %p\n",__FUNCTION__,&it);
	//转换当前节点
	fields.cpp2Java(env, *it, jnode);	

	//处理子节点
	tree<J_NVItem>::children_iterator itc = it.begin_children_iterator();
	tree<J_NVItem>::children_iterator eitc = it.end_children_iterator();

	//jclass clsNode = env->FindClass(JCLSNAME_TreeNode);
    jclass clsArrayList = env->FindClass(JCLSNAME_ArrayList);
	jmethodID add_mid = env->GetMethodID(clsArrayList,"add","(Ljava/lang/Object;)Z");
	//当前节点孩子列表
	jobject childList = env->GetObjectField(jnode, fields.m_Childs_id);
	while(itc != eitc)
	{
		//创建java子节点
		jobject jchild = env->NewObject(gClsTreeNode, fields.m_Construct_mid);
		//转换子节点
		treeToJava(env, itc, jchild, fields);
		//添加子节点到当前节点孩子列表
        env->CallBooleanMethod(childList, add_mid, jchild);

		env->DeleteLocalRef(jchild);

		++itc;
	}		
	env->DeleteLocalRef((jobject)clsArrayList);
	env->DeleteLocalRef(childList);
//	LOGI("Leave %s %p\n",__FUNCTION__, &it);
}


void treeFromJava(JNIEnv * env, const jobject & jnode, tree<J_NVItem>::iterator_base & it, const JMultiTreeNode & fields, tree<J_NVItem> & tre)
{
//	LOGI("Enter %s %p\n",__FUNCTION__, &it);
	//转换当前节点
	fields.java2Cpp(env, jnode, *it);	
	
	//处理子节点
	jobject childList = env->GetObjectField(jnode, fields.m_Childs_id);
	jclass clsArrayList = env->FindClass(JCLSNAME_ArrayList);
	jmethodID sizeList_mid = env->GetMethodID(clsArrayList, "size", "()I");

	jmethodID getList_mid = env->GetMethodID(clsArrayList, "get", "(I)Ljava/lang/Object;");

	int childCount = env->CallIntMethod(childList, sizeList_mid);
	J_NVItem item;
	for(int i = 0; i < childCount; i++)
	{
		jobject jchild = env->CallObjectMethod(childList, getList_mid, i);	

		tree<J_NVItem>::iterator_base itchild = tre.append_child(it, item);
		treeFromJava(env, jchild, itchild, fields, tre);

		env->DeleteLocalRef(jchild);
	}

//	LOGI("Leave %s %p\n",__FUNCTION__, &it);
}

static inline void NativeTreeToJTree(JNIEnv * env, const tree<J_NVItem> & tre, jobject & jtre)
{
	jfieldID root_id = GetJRootFieldID(env);
	jobject jidRoot = env->GetObjectField(jtre, root_id);

	JMultiTreeNode fields;
	InitTreeFieldIds(env, fields);

	J_DataObject::iterator_base itroot = tre.root();
	treeToJava(env, itroot, jidRoot, fields);
	env->DeleteLocalRef(jidRoot);
}

inline static void JTreeToNativeTree(JNIEnv * env, const jobject & jtre, tree<J_NVItem> & tre)
{
	//默认认为tre没有root节点
	J_NVItem item;
	tre.set_root(item);

	jfieldID root_id = GetJRootFieldID(env);
	jobject jidRoot = env->GetObjectField(jtre, root_id);

	JMultiTreeNode fields;
	InitTreeFieldIds(env, fields);

	J_DataObject::iterator_base itroot = tre.root();
	treeFromJava(env, jidRoot, itroot, fields, tre);
	env->DeleteLocalRef(jidRoot);
}

extern "C"
{

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniCaInterface_nativeGetEmailHeads(JNIEnv * env, jobject thiz, jobject jheads)
{
	LOGI("Enter %s\n",__FUNCTION__);
	J_DataObject heads;
	int result = tvcore_getEMailHeadsEx(heads);
	if(result != 0)
		return result;

	NativeTreeToJTree(env, heads, jheads);
	LOGI("Leave %s\n",__FUNCTION__);
	return 0;
}

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniCaInterface_nativeGetEmailHead(JNIEnv * env, jobject thiz, jobject jid, jobject jhead)
{
	LOGI("Enter %s\n",__FUNCTION__);
	jfieldID root_id = GetJRootFieldID(env);
	jobject jidRoot = env->GetObjectField(jid, root_id);

	JMultiTreeNode fields;
	InitTreeFieldIds(env, fields);

	tree<J_NVItem> idTree;
	J_NVItem idRoot;
	idTree.set_root(idRoot);
	J_DataObject::iterator_base itroot = idTree.root();
	treeFromJava(env, jidRoot, itroot, fields, idTree);
	env->DeleteLocalRef(jidRoot);

	tree<J_NVItem> headTree;
	bool result = tvcore_getEMailHeadEx(idTree, headTree);
	if(result == false)
		return 1;

	jobject jheadRoot = env->GetObjectField(jhead, root_id);
	J_DataObject::iterator_base itHeadRoot = headTree.root();
	treeToJava(env, itHeadRoot, jheadRoot, fields);
	env->DeleteLocalRef(jheadRoot);
	LOGI("Leave %s\n",__FUNCTION__);
	return 0;
}

void printTree(const tree<J_NVItem> & tre)
{

	tree<J_NVItem>::pre_order_iterator it = tre.begin_pre_order_iterator(tre.root());
	tree<J_NVItem>::pre_order_iterator eit = tre.end_pre_order_iterator(tre.root());
	while(it != eit)
	{
		LOGI("%s\n",it->name.c_str());
		if(it->value.vt == JVT_STRING)
			LOGI("vt:%d val:%s\n",it->value.vt, it->value.pStrVal->strVal);
		else if(it->value.vt == JVT_BINARY)
			LOGI("vt:%d val size:%d\n",it->value.vt, it->value.pBinary->uSize);
		else
		{
			LOGI("vt:%d val:%d\n",it->value.vt, it->value.s32Val);
		}
		fflush(stdout);
		++it;
	}
}

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniCaInterface_nativeGetEmailContent(JNIEnv * env, jobject thiz, jobject jid, jobject jcontent)
{
	LOGI("Enter %s\n",__FUNCTION__);
	jfieldID root_id = GetJRootFieldID(env);
	jobject jidRoot = env->GetObjectField(jid, root_id);

	JMultiTreeNode fields;
	InitTreeFieldIds(env, fields);

	tree<J_NVItem> idTree;
	J_NVItem iRoot;
	idTree.set_root(iRoot);
	J_DataObject::iterator_base itroot = idTree.root();
	treeFromJava(env, jidRoot, itroot, fields, idTree);
	env->DeleteLocalRef(jidRoot);

	tree<J_NVItem> contentTree;
	int result = tvcore_getEMailContentEx(idTree, contentTree);
	if(result != 0)
		return result;

	printTree(contentTree);

	jobject jcontentRoot = env->GetObjectField(jcontent, root_id);
	J_DataObject::iterator_base itContentRoot = contentTree.root();
	treeToJava(env, itContentRoot, jcontentRoot, fields);
	env->DeleteLocalRef(jcontentRoot);

	LOGI("Leave %s\n",__FUNCTION__);

	return 0;
}

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniCaInterface_nativeGetEmailSpaceInfo(JNIEnv * env, jobject thiz, jobject jspace)
{
	LOGI("Enter %s\n",__FUNCTION__);

	tree<J_NVItem> spaceTree;
	tvcore_getEMailSpaceInfoEx(spaceTree);
	NativeTreeToJTree(env, spaceTree, jspace);

	LOGI("Leave %s\n",__FUNCTION__);
	return 0;

}

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniCaInterface_nativeDelEmail(JNIEnv * env, jobject thiz, jobject jid)
{
	LOGI("Enter %s\n",__FUNCTION__);

	tree<J_NVItem> idTree;
	JTreeToNativeTree(env, jid, idTree);
	int ret = tvcore_delEMailEx(idTree);

	LOGI("Leave %s\n",__FUNCTION__);
	return ret;
}

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniCaInterface_nativeGetParameters(JNIEnv * env, jobject thiz, jint jkey, jobject in, jobject out)
{
	LOGI("Enter %s\n",__FUNCTION__);
	tree<J_NVItem> treIn, treOut;

	JTreeToNativeTree(env, in, treIn);

	int ret = tvcore_GetParameters(jkey, treIn, treOut);
	if(ret == 0)
		NativeTreeToJTree(env, treOut, out);

	LOGI("Leave %s %u %d %X\n",__FUNCTION__, ret, ret, ret);
	return ret;
}

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniCaInterface_nativeSetParameters(JNIEnv * env, jobject thiz, jint jkey, jobject in, jobject out)
{
	LOGI("Enter %s\n",__FUNCTION__);
	tree<J_NVItem> treIn, treOut;

	JTreeToNativeTree(env, in, treIn);

	int ret = tvcore_SetParameters(jkey, treIn, treOut);
	
	if(ret == 0)
		NativeTreeToJTree(env, treOut, out);

	LOGI("Leave %s %u %d %X\n",__FUNCTION__, ret, ret, ret);
	return ret;
}

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniCaInterface_nativeSetWorkTime(JNIEnv * env, jobject thiz, jobject jtime)
{
	LOGI("Enter %s\n",__FUNCTION__);

	
	tree<J_NVItem> timeTree;
	JTreeToNativeTree(env, jtime, timeTree);
	int ret = tvcore_SetWatchTimeEx(timeTree);
	

	/*
	tree<J_NVItem> tre;
	J_NVItem item;
	tre.set_root(item);

	jfieldID root_id = GetJRootFieldID(env);
	jobject jidRoot = env->GetObjectField(jtime, root_id);

	JMultiTreeNode fields;
	InitTreeFieldIds(env, fields);

	J_DataObject::iterator_base itroot = tre.root();
	treeFromJava(env, jidRoot, itroot, fields, tre);
	env->DeleteLocalRef(jidRoot);

	printTree(tre);

	int ret = tvcore_SetWatchTimeEx(tre);
*/
	LOGI("Leave %s\n",__FUNCTION__);
	return ret;
}

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniCaInterface_nativeGetWorkTime(JNIEnv * env, jobject thiz, jobject jtime)
{
	LOGI("Enter %s\n",__FUNCTION__);

	tree<J_NVItem> timeTree;
	int ret = tvcore_GetWatchTimeEx(timeTree);
	if(ret != 0)
		return ret;

	NativeTreeToJTree(env, timeTree, jtime);

	LOGI("Leave %s\n",__FUNCTION__);
	return ret;
}

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniCaInterface_nativeGetRating(JNIEnv * env, jobject thiz, jobject jrating)
{
	LOGI("Enter %s\n",__FUNCTION__);

	tree<J_NVItem> ratingTree;
	tvcore_GetWatchLevelEx(ratingTree);
	NativeTreeToJTree(env, ratingTree, jrating);

	LOGI("Leave %s\n",__FUNCTION__);
	return 0;
}

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniCaInterface_nativeSetRating(JNIEnv * env, jobject thiz, jobject jrating)
{
	LOGI("Enter %s\n",__FUNCTION__);

	tree<J_NVItem> ratingTree;
	JTreeToNativeTree(env, jrating, ratingTree);
	int ret = tvcore_SetWatchLevelEx(ratingTree);

	LOGI("Leave %s\n",__FUNCTION__);
	return ret;
}

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniCaInterface_nativeChangePinCode(JNIEnv * env, jobject thiz, jobject jpin)
{
	LOGI("Enter %s\n",__FUNCTION__);

	tree<J_NVItem> pinTree;
	JTreeToNativeTree(env, jpin, pinTree);
	int ret = tvcore_ChangePinCodeEx(pinTree);

	LOGI("Leave %s\n",__FUNCTION__);
	return ret;
}

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniCaInterface_nativeGetOperatorID(JNIEnv * env, jobject thiz, jobject jids)
{
	LOGI("Enter %s\n",__FUNCTION__);

	tree<J_NVItem> idsTre;
	int ret = tvcore_GetOperatorIDEx(idsTre);
	if(ret == 0)
		NativeTreeToJTree(env, idsTre, jids);

	LOGI("Leave %s\n",__FUNCTION__);
	return ret;
}

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniCaInterface_nativeGetAuthorization(JNIEnv * env, jobject thiz, jobject jid, jobject jauths)
{
	LOGI("Enter %s\n",__FUNCTION__);

	tree<J_NVItem> idTree, authsTree;
	JTreeToNativeTree(env, jid, idTree);
	int ret = tvcore_GetAuthorizationEx(idTree, authsTree);

	if(ret == 0)
		NativeTreeToJTree(env, authsTree, jauths);

	LOGI("Leave %s\n",__FUNCTION__);
	return ret;
}

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniCaInterface_nativeGetCardSN(JNIEnv * env, jobject thiz, jobject jcard)
{
	LOGI("Enter %s\n",__FUNCTION__);

	tree<J_NVItem> cardTre;
	int ret = tvcore_GetCardsnEx(cardTre);
	if(ret == 0)
		NativeTreeToJTree(env, cardTre, jcard);

	LOGI("Leave %s\n",__FUNCTION__);
	return ret;
}

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniCaInterface_nativeGetSTBId(JNIEnv * env, jobject thiz, jobject jid)
{
	LOGI("Enter %s\n",__FUNCTION__);

	tree<J_NVItem> idTre;
	int ret = tvcore_GetSTBIdEx(idTre);
	if(ret == 0)
		NativeTreeToJTree(env, idTre, jid);

	LOGI("Leave %s\n",__FUNCTION__);
	return ret;
}

JNIEXPORT jint JNICALL Java_novel_supertv_dvb_jni_JniCaInterface_nativeGetOperatorACS(JNIEnv * env, jobject thiz, jobject jid, jobject jacs)
{
	LOGI("Enter %s\n",__FUNCTION__);

	tree<J_NVItem> idTre, acsTre;
	JTreeToNativeTree(env, jid, idTre);
	int ret = tvcore_GetOperatorAcsEx(idTre, acsTre);

	if(ret == 0)
		NativeTreeToJTree(env, acsTre, jacs);

	LOGI("Leave %s\n",__FUNCTION__);
	return ret;
}


jobject DoShumaNotifyCallBack(JNIEnv * env, int keyCode, void * param)
{
	LOGI("Enter %s\n",__FUNCTION__);
	J_DataObject * pTree = (J_DataObject*)param;

	//创建Java tree
	jobject jtree;
	jmethodID conTree_mid = env->GetMethodID(gClsMultiTree, "<init>", "()V");
	jtree = env->NewObject(gClsMultiTree,conTree_mid);
	NativeTreeToJTree(env, *pTree, jtree);
	LOGI("Leave %s \n",__FUNCTION__);

	return jtree;
}


}//extern "C"
