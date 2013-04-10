// �������� ��vim ʶ��
#ifndef DVBDEF_H_HEADER_INCLUDED_
#define DVBDEF_H_HEADER_INCLUDED_
#include "tvcomm.h"
typedef struct tagAreaInfo
{
	U32 AreaCode;	///<������
	U32 bouquet_id;
}AreaInfo;

typedef struct tagDVBParam
{
	TuningParam tuning_param;	///<��ǰƵ��
//	int parm_type;				///< ��������

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


/// Pat�������Ͷ���.
typedef std::map<U32,U16>	mapPatParamT;

/// Bat�������Ͷ���.
struct BatParam
{
	U16 sid;		
	U16 ts_id;
	U16 net_id;
	U16 service_type;
	U32 bouquet_id;
};

/// put_section_data����ֵ����
enum ePutDataRT
{
	RT_CONTINUE = 0,	///<��Ч�򲻳�ֵ�����
	RT_TABLE_OK = 1,	///<֪ͨ��ǰ��������
	RT_SERVICE_OK = 2,	///<֪ͨ��ǰƵ��������
	RT_UPDATE_FREQ = 3,	///<֪ͨNit��������
	RT_UPDATE_FILTER = 4///<֪ͨ�����
};

/// Cat�������Ͷ���.
struct CatParam
{
	U8  length;
	U16 ca_pid[256];		///<��Ŀ��.
	U16 ca_system_id[256];	///<��Ŀ��.
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
