// tvseach.cpp : Defines the entry point for the DLL application.
//
#include "typ.h"

#include "tvsearchlocal.h"

/// ����ģ���ʼ��.
bool init_tv_search(IN int searchMode)
{
	return true;
}
/// ֪ͨ��ǰƵ��ֵ�ı�
void change_tuning_param(IN TuningParam tuning_param)
{
}

/// ��ȡ����Ƶ��ʱ��Ҫ������Щ������
unsigned long get_table_filters(OUT vector<SECFilter>& tFilters,IN BuildStep buildStep)
{
	return S_OK;
}

/// �ͷ�����ģ��
unsigned long destroy_search()
{
	return S_OK;
}

/// ��ȡȫƵģʽ����Ƶ���б�
void get_full_search_tables(OUT vector<U16>& freqs)
{
	freqs.clear();
}

/// ȡ��Bat���bouquet_id
void get_bat_bouquetid(IN U16 ts_index, OUT U32 *piBouquet_id )
{
}


/// ��NIT�л�ȡƵ�ʱ�
unsigned long get_tuning_paramfrom_nit(OUT vector<TuningParam>& TuningParamList)
{
	TuningParamList.clear();
	return S_OK;
}


/// ������ر�����
int analyse_section_data(IN U16 pid,IN U8 *Data,IN U32 DataLen )
{
	return S_OK;
}

/// ��ȡ��ǰ��������DVBService
unsigned long get_dvb_services(OUT vector<DVBService>& services )
{
	services.clear();
	return S_OK;
}

/// ��ȡ������������Ƶ����Ϣ
U8 nitVersion=0xff;   // ��ʼֵ��Ҫ��������������ֵ�滻
U8 batVersion=0xff;  // ��ʼֵ, Ҫ��������������ֵ�滻
U16 iSystemID=0x4ad2; // ���������������ǹ̶��ġ�������0x4ad2, ����Ϊ0x4a02

U32 get_total_dvb_service(OUT vector<DVBService>& services)
{
	services.clear();
    // ��дһ�¹����ʹ�õ���������
    FILE *fp=fopen("/data/data/novel.supertv.dvb/databases/version.txt","w");
    if(fp)
    {
             fprintf(fp,"nitversion:%d\n",nitVersion);  // ��������nit �汾��
             fprintf(fp,"batversion:%d\n",batVersion); // ��������bat �汾��
             fprintf(fp,"systemID:%d\n",iSystemID);   // �����systemID ��0x4ad2, ʮ���� 19154
             fclose(fp);
    }
	// ......
	return S_OK;
}
/// ��ȡ��չ�����б�
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


