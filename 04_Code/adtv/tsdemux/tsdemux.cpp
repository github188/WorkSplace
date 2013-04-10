#include "tsdemux.h"
#include "TsCatcher.h"

static TsCatcher controller_;

void tsdemux_enableDemux(bool enable)
{ 
	controller_.enableTs(enable);
}

// ���TS���ݹ�����
utHandle tsdemux_addTSFilter(
	utTsDataFilter const *filter,
	long filterCount,
	utFilterDataCallback dataHandle,
	utContext *context)
{
	return controller_.addTsPacketFilter(filter,filterCount,dataHandle,context);
}

// ɾ��TS���ݹ��������÷���Ϊ�첽����
void tsdemux_delTSFilter(
	utHandle hFilter,
	utFilterDestoryNotify destoryCallback)
{
	controller_.delTsPacketFilter(hFilter,destoryCallback);
}

// ��ӱ�Section������
utHandle tsdemux_addSectionFilter(
	utSectionFilter const *filter,
	long filterCount,
	utFilterDataCallback dataHandle,
	utContext *context)
{
	return controller_.addSectionFilter(filter,filterCount,dataHandle,context);
}

// ɾ����Section������
void tsdemux_delSectionFilter(
	utHandle hFilter,
	utFilterDestoryNotify destoryCallback)
{
	controller_.delSectionFilter(hFilter,destoryCallback);
}


