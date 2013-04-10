// tvseach.cpp : Defines the entry point for the DLL application.
//
#include "typ.h"

#include "tvsearchlocal.h"

/// 搜索模块初始化.
bool init_tv_search(IN int searchMode)
{
	return true;
}
/// 通知当前频率值改变
void change_tuning_param(IN TuningParam tuning_param)
{
}

/// 获取搜索频道时需要搜索那些表数据
unsigned long get_table_filters(OUT vector<SECFilter>& tFilters,IN BuildStep buildStep)
{
	return S_OK;
}

/// 释放搜索模块
unsigned long destroy_search()
{
	return S_OK;
}

/// 获取全频模式搜索频点列表
void get_full_search_tables(OUT vector<U16>& freqs)
{
	freqs.clear();
}

/// 取得Bat表的bouquet_id
void get_bat_bouquetid(IN U16 ts_index, OUT U32 *piBouquet_id )
{
}


/// 从NIT中获取频率表
unsigned long get_tuning_paramfrom_nit(OUT vector<TuningParam>& TuningParamList)
{
	TuningParamList.clear();
	return S_OK;
}


/// 发送相关表数据
int analyse_section_data(IN U16 pid,IN U8 *Data,IN U32 DataLen )
{
	return S_OK;
}

/// 获取当前搜索到的DVBService
unsigned long get_dvb_services(OUT vector<DVBService>& services )
{
	services.clear();
	return S_OK;
}

/// 获取搜索到的所有频道信息
U8 nitVersion=0xff;   // 初始值，要用你们搜索到的值替换
U8 batVersion=0xff;  // 初始值, 要用你们搜索到的值替换
U16 iSystemID=0x4ad2; // 对于你们来讲，是固定的。数码是0x4ad2, 永新为0x4a02

U32 get_total_dvb_service(OUT vector<DVBService>& services)
{
	services.clear();
    // 书写一下供监控使用的三个变量
    FILE *fp=fopen("/data/data/novel.supertv.dvb/databases/version.txt","w");
    if(fp)
    {
             fprintf(fp,"nitversion:%d\n",nitVersion);  // 搜索到的nit 版本号
             fprintf(fp,"batversion:%d\n",batVersion); // 搜索到的bat 版本号
             fprintf(fp,"systemID:%d\n",iSystemID);   // 数码的systemID 是0x4ad2, 十进制 19154
             fclose(fp);
    }
	// ......
	return S_OK;
}
/// 获取扩展服务列表
U32 get_ExtServTypeTable(OUT vector<ServiceTypeTableItem> &table)
{
	table.clear();
	return S_OK;
}

U32 GetVersion(IN const STVMode iMode,OUT BYTE* pNitVertion, OUT BYTE *pBatVersion)
{
	*pNitVertion=1;
	*pBatVersion=1;
	return S_OK;
}

Table_Status IGetCatSdtTableState()
{
	Table_Status v = Cat_Sdt_Table_ok;
	return v;
	
}
void ISetCatSdtTableState(BOOL v)
{
}
BOOL ICatSdtMissing()
{
	return true;
}

bool ChangeFullModeToNitMode() 
{
	return false;
}
int LC_SetParameter(int key, const void* request,int reqLength)
{
	return -1;
}
int LC_GetParameter(int key, void* reply,	int* replyLength)
{
	return -1;
}


