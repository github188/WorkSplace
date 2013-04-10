// tvseach.cpp : Defines the entry point for the DLL application.
//
#include "typ.h"

#include "tvsearchlocal.h"
#define LOG_TAG "tvsearch"
#define LOG_LEVEL LOG_TRACE
#include "dxreport.h"

#ifdef JS_BEJING_NIT_SEARCH
#include "beijing/parse_beijing.h"
ParseBeijing parseObj_;
#endif


#ifdef JS_FUZHOU_NIT_SEARCH
#include "fuzhou/parse_fuzhou.h"
ParseFuZhou parseObj_;
#endif

#ifdef JS_QINGDAO_NIT_SEARCH
#include "qingdao/parse_qingdao.h"
ParseQingDao parseObj_;
#endif

AreaInfo  t_area_info;


#ifdef _MANAGED
#pragma managed(push, off)
#endif
#ifdef _WINDOWS
BOOL __stdcall DllMain( HMODULE hModule,
                      DWORD  ul_reason_for_call,
                      LPVOID lpReserved
                      )
{
  return TRUE;
}
#endif

#ifdef _MANAGED
#pragma managed(pop)
#endif



// Range , ȫƵ��
const U16 localInitFreqs[] =
{
	53,  61,  69,  80,  88, 115, 123, 131, 139, 147,
	155, 163, 171, 179, 187, 195, 203, 211, 219, 227,
	235, 243, 251, 259, 267, 275, 283, 291, 299, 307,
	315, 323, 331, 339, 347, 355, 363, 371, 379, 387,
	395, 403, 411, 419, 427, 435, 443, 451, 459, 474,
	482, 490, 498, 506, 514, 522, 530, 538, 546, 554,
	562, 570, 578, 586, 594, 602, 610, 618, 626, 634,
	642, 650, 658, 666, 674, 682, 690, 698, 706, 714,
	722, 730, 738, 746, 754, 762, 770, 778, 786, 794,
	802, 810, 818, 826, 834, 842, 850,0
};


/// ����ģ���ʼ��.
bool init_tv_search(IN int searchMode)
{
#ifdef _WIN32
//	t_area_info.AreaCode = GetPrivateProfileInt("Local","AreaCode",400000,SETTING_INIPATH_WSTR);
#endif
	dxreport("<tvsearchLocal> init_tv_search(%d)\n",searchMode);
	t_area_info.bouquet_id = 0;

	parseObj_.InitTVSearch(searchMode);
	parseObj_.SetAreaInfo(t_area_info);

	return true;
}
/// ֪ͨ��ǰƵ��ֵ�ı�
void change_tuning_param(IN TuningParam tuning_param)
{
	parseObj_.SetTuningParam(tuning_param);
}

/// ��ȡ����Ƶ��ʱ��Ҫ������Щ������
unsigned long get_table_filters(OUT vector<SECFilter>& tFilters,IN BuildStep buildStep)
{
	return parseObj_.GetTableFilters(tFilters,buildStep);
}

/// �ͷ�����ģ��
unsigned long destroy_search()
{
	return S_OK;
}

/// ��ȡȫƵģʽ����Ƶ���б�
void get_full_search_tables(OUT vector<U16>& freqs)
{
	U8 iCount = sizeof(localInitFreqs)/sizeof(U16);
	for(U8 i = 0; i < iCount ; i++)
		freqs.push_back(localInitFreqs[i]);

	dxreport("<tvsearchLocal> get_full_search_tables (%d)\n",iCount);
}

/// ȡ��Bat���bouquet_id
void get_bat_bouquetid(IN U16 ts_index, OUT U32 *piBouquet_id )
{
	UNUSED_PARAM(ts_index);
	if(0 != t_area_info.bouquet_id)
	{
		*piBouquet_id = t_area_info.bouquet_id;
		dxreport("<tvsearchLocal> get_bat_bouquetid readly bouquet_id (%d)\n",t_area_info.bouquet_id);
		return;
	}

	switch(t_area_info.AreaCode)
	{
	case 400000: //����
		t_area_info.bouquet_id = NS_BATID;
		break;
	case 300000: //NS ��˾�ڲ�
		t_area_info.bouquet_id = NS_BATID;
		break;
	case 370000: //����
		t_area_info.bouquet_id = 0x6100;
		break;
	default:
		t_area_info.bouquet_id = NS_BATID;
		break;
	}

	*piBouquet_id = t_area_info.bouquet_id;
	parseObj_.SetAreaInfo(t_area_info);

	dxreport("<tvsearchLocal> get_bat_bouquetid bouquet_id (%d)\n",*piBouquet_id);
}


/// ��NIT�л�ȡƵ�ʱ�
unsigned long get_tuning_paramfrom_nit(OUT vector<TuningParam>& TuningParamList)
{
	return parseObj_.GetTuningParamformNit(TuningParamList);
}


/// ������ر�����
int analyse_section_data(IN U16 pid,IN U8 *Data,IN U32 DataLen )
{
	return parseObj_.AnalyseSectionData(pid,Data,DataLen);
}

/// ��ȡ��ǰ��������DVBService
unsigned long get_dvb_services(OUT vector<DVBService>& services )
{
	return parseObj_.GetDVBServices(services);
}

/// ��ȡ������������Ƶ����Ϣ
U32 get_total_dvb_service(OUT vector<DVBService>& services)
{
	services.clear();
	parseObj_.GetDVBALLService(services);
	return S_OK;
}
/// ��ȡ��չ�����б�
U32 get_ExtServTypeTable(OUT vector<ServiceTypeTableItem> &table)
{
	return parseObj_.GetDVBExtServTypeTable(table);
}

U32 GetVersion(IN const STVMode iMode,OUT BYTE* pNitVertion, OUT BYTE *pBatVersion)
{
	return parseObj_.GetVersion(iMode,pNitVertion,pBatVersion);
}

Table_Status IGetCatSdtTableState()
{
	Table_Status v = parseObj_.GetCatSdtTableState();
	dxreport("IGetCatTableState return %d\n",v);
	return v;
	
}
void ISetCatSdtTableState(BOOL v)
{
	parseObj_.SetCatSdtTableState(v);
}
BOOL ICatSdtMissing()
{
	return parseObj_.CatSdtMissing();
}

bool ChangeFullModeToNitMode() {return false;}
int LC_SetParameter(int key, const void* request,int reqLength)
{
	return parseObj_.SetParameter(key,request,reqLength);
}
int LC_GetParameter(int key, void* reply,	int* replyLength)
{
	return parseObj_.GetParameter(key,reply,replyLength);
}


