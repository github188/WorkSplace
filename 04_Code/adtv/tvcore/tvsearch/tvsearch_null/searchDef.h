// 包含中文 由vim 识别
#ifndef DVBDEF_H_HEADER_INCLUDED_
#define DVBDEF_H_HEADER_INCLUDED_
#include "tvcomm.h"
typedef struct tagAreaInfo
{
	U32 AreaCode;	///<区域码
	U32 bouquet_id;
}AreaInfo;

typedef struct tagDVBParam
{
	TuningParam tuning_param;	///<当前频点
//	int parm_type;				///< 搜索类型

	// table status
	bool pat_ok;
	bool nita_ok;
	bool nito_ok;
	bool pmt_ok;
	bool cat_ok;
	bool sdta_ok;
	bool sdto_ok;
	bool bat_ok;

	U32  pmt_lenght;			///<pmt count
}DVBParam;

#define    NS_BATID    105


/// Pat参数类型定义.
typedef std::map<U32,U16>	mapPatParamT;

/// Bat参数类型定义.
struct BatParam
{
	U16 sid;		
	U16 ts_id;
	U16 net_id;
	U16 service_type;
	U32 bouquet_id;
};

/// put_section_data返回值定义
enum ePutDataRT
{
	RT_CONTINUE = 0,	///<无效或不充分的数据
	RT_TABLE_OK = 1,	///<通知当前表分析完成
	RT_SERVICE_OK = 2,	///<通知当前频点分析完成
	RT_UPDATE_FREQ = 3,	///<通知Nit表分析完成
	RT_UPDATE_FILTER = 4///<通知表更新
};

/// Cat参数类型定义.
struct CatParam
{
	U8  length;
	U16 ca_pid[256];		///<节目号.
	U16 ca_system_id[256];	///<节目号.
};

typedef enum
{
	Cat_Sdt_Table_Not_ok,
	Cat_Table_ok,
	Sdt_Table_ok,
	Cat_Sdt_Table_ok,
}Table_Status;

typedef enum TagBuildStep
{
	BS_PATCATSDT,
		BS_PMT,
		BS_NITBAT,
}BuildStep;

#endif
