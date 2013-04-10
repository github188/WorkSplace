// #include "tvdefs.h"
// #include <Windows.h>

class TVSearchWrapper;
extern TVSearchWrapper TVSearchDLL;

// 
// 声明DLL中的函数
typedef bool    (*p_init_tv_search)(IN int ParamType);
typedef void	(*p_get_full_search_tables)(OUT std::vector<U16> &Freqs);
typedef void	(*p_get_bat_bouquetid)(IN U16 ts_index, OUT U32 *piBouquet_id );	
typedef ULONG	(*p_get_tuning_paramform_nit)(OUT std::vector<TuningParam> &TuningParamList);
typedef ULONG	(*p_get_table_filters)(OUT std::vector<SECFilter> &tFilters);
typedef void	(*p_change_tuning_param)(IN TuningParam tuning_param);
typedef int		(*p_put_section_data)(IN U16 pid,IN U8 *Data,IN U32 DataLen);
typedef ULONG	(*p_get_dvb_service)(OUT std::vector<DVBService> &services);
typedef U32		(*p_get_total_dvb_service)(OUT std::vector<DVBService> &services);
typedef U32		(*p_get_data)(IN const STVMode iMode,OUT void* pData);
typedef void	(*p_change_to)(IN U16 iSID);
typedef ULONG	(*p_destroy_search)();
typedef int		(*p_get_property1)(IN const char* param1,IN const char* param2);
typedef bool	(*p_get_property2)(IN const char* param1,IN const char* param2,
								   IN const int iBufSize,OUT char* pBuffer);

class TVSearchWrapper
{
public:
	TVSearchWrapper(){
		LoadLib();
	}
	virtual ~TVSearchWrapper()
	{
		if(NULL != m_hDll){
			NS_FreeLibrary(m_hDll);
		}
	}
	
	void* GetFunctionProc(const char* name);

private:

	void LoadLib();
	
	p_init_tv_search			init_tv_search_;
	p_get_full_search_tables	get_full_search_tables_;
	p_get_bat_bouquetid			get_bat_bouquetid_;
	p_get_tuning_paramform_nit	get_tuning_paramform_nit_;
	p_get_table_filters			get_table_filters_;
	p_change_tuning_param		change_tuning_param_;
	p_put_section_data			put_section_data_;
	p_get_dvb_service			get_dvb_service_;
	p_get_total_dvb_service		get_total_dvb_service_;
	p_get_data					get_data_;
	p_change_to					change_to_;
	p_destroy_search			destroy_search_;
	p_get_property1				get_property1_;
	p_get_property2				get_property2_;

	HINSTANCE  m_hDll;
};

#define init_tv_search  \
	reinterpret_cast<p_init_tv_search>(TVSearchDLL.GetFunctionProc("init_tv_search"))
#define get_full_search_tables \
	reinterpret_cast<p_get_full_search_tables>(TVSearchDLL.GetFunctionProc("get_full_search_tables"))
#define get_bat_bouquetid	\
	reinterpret_cast<p_get_bat_bouquetid>(TVSearchDLL.GetFunctionProc("get_bat_bouquetid"))
#define get_tuning_paramform_nit \
	reinterpret_cast<p_get_tuning_paramform_nit>(TVSearchDLL.GetFunctionProc("get_tuning_paramform_nit"))
#define get_table_filters \
	reinterpret_cast<p_get_table_filters>(TVSearchDLL.GetFunctionProc("get_table_filters"))
#define change_tuning_param \
	reinterpret_cast<p_change_tuning_param>(TVSearchDLL.GetFunctionProc("change_tuning_param"))
#define put_section_data \
	reinterpret_cast<p_put_section_data>(TVSearchDLL.GetFunctionProc("put_section_data"))
#define get_dvb_service \
	reinterpret_cast<p_get_dvb_service>(TVSearchDLL.GetFunctionProc("get_dvb_service"))
#define get_total_dvb_service \
	reinterpret_cast<p_get_total_dvb_service>(TVSearchDLL.GetFunctionProc("get_total_dvb_service"))
#define get_data \
	reinterpret_cast<p_get_data>(TVSearchDLL.GetFunctionProc("get_data"))
#define change_to \
	reinterpret_cast<p_change_to>(TVSearchDLL.GetFunctionProc("change_to"))
#define destroy_search \
	reinterpret_cast<p_destroy_search>(TVSearchDLL.GetFunctionProc("destroy_search"))
#define get_property1 \
	reinterpret_cast<p_get_property1>(TVSearchDLL.GetFunctionProc("get_property1"))
#define get_property2 reinterpret_cast<p_get_property2>(TVSearchDLL.GetFunctionProc("get_property2"))
