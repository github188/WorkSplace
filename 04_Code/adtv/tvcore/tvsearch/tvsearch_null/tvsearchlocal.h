#ifndef SEARCHTV_H_HEADER_INCLUDED_B26F867C
#define SEARCHTV_H_HEADER_INCLUDED_B26F867C
#include "searchDef.h"
#include <vector>
using namespace std;

#ifdef _WINDOWS
#ifdef TVSEARCH_EXPORTS
#define TVSEARCH_API __declspec(dllexport)
#else
#define TVSEARCH_API __declspec(dllimport)
#endif
#else
#define TVSEARCH_API __attribute__((visibility("default")))
#endif

#ifdef __cplusplus
extern "C" {
#endif
	/// 初始化操作.
	TVSEARCH_API bool init_tv_search(IN int ParamType);
	/// 改变当前频率值
	TVSEARCH_API  void change_tuning_param(IN TuningParam tuning_param);
	/// 获取搜索频道时需要搜索那些表数据
	TVSEARCH_API  unsigned long get_table_filters(OUT vector<SECFilter> &tFilters,BuildStep buildStep);
	/// 取得频点表
	TVSEARCH_API void get_full_search_tables(OUT vector<U16> &Freqs);
	/// 取得Bat表的bouquet_id
	TVSEARCH_API void get_bat_bouquetid(IN U16 ts_index, OUT U32 *piBouquet_id );	
	/// 从NIT中获取频率表
	TVSEARCH_API  unsigned long get_tuning_paramfrom_nit(OUT vector<TuningParam> &TuningParamList);
	TVSEARCH_API int analyse_section_data(IN U16 pid,IN U8 *Data,IN U32 DataLen);
	/// 获取当前搜索到的DVBService
	TVSEARCH_API unsigned long get_dvb_services(OUT vector<DVBService> &services);
	/// 获取搜索到的所有频道信息
	TVSEARCH_API U32 get_total_dvb_service(OUT vector<DVBService> &services);
	/// 获取扩展服务列表
	TVSEARCH_API U32 get_ExtServTypeTable(OUT vector<ServiceTypeTableItem> &table);
	/// 获取针对特定操作的数据
	TVSEARCH_API U32 GetVersion(IN const STVMode iMode,OUT BYTE* pNitVersion, OUT BYTE *pBatVersion);
	/// 结束搜索
	TVSEARCH_API  unsigned long destroy_search();
	/// 通用接口
	TVSEARCH_API  bool ChangeFullModeToNitMode();
	TVSEARCH_API Table_Status IGetCatSdtTableState();
	TVSEARCH_API void ISetCatSdtTableState(BOOL v);
	TVSEARCH_API BOOL ICatSdtMissing();
	
	// 设置/获取通用模式
	TVSEARCH_API int LC_SetParameter(int key, const void* request,int reqLength);
	TVSEARCH_API int LC_GetParameter(int key, void* reply,  int* replyLength);
#if defined(__cplusplus) 
}
#endif

#endif /* SEACHTV_H_HEADER_INCLUDED_B26F867C */
