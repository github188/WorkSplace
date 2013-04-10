#ifndef TSCATCHER_H_
#define TSCATCHER_H_

#include "xprocess.h"
#include "SectionFilter.h"
#include "NtfTS2Section.h"
#include "tvdevice.h"

#define MAX_SECFILTER_COUNT		64
#define MAX_TSFILTER_COUNT		32

struct TsCatcher : public NovelSupertv::stbruntime::Thread
{
public:
	TsCatcher(void);
	~TsCatcher(void);

	// PSI Section段过滤接口
	utHandle addSectionFilter(
		utSectionFilter const *filter,
		long filterCount,
		utFilterDataCallback callback,
		utContext *context);
	void delSectionFilter(
		utHandle hFilter,
		utFilterDestoryNotify destoryCallback);

	// TS 分组包过滤接口
	utHandle addTsPacketFilter(
		utTsDataFilter const *filter,
		long filterCount,
		utFilterDataCallback callback,
		utContext *context);
	void delTsPacketFilter(
		utHandle hFilter,
		utFilterDestoryNotify destoryCallback);
	
	// 使能数据接口(线程控制)
	void enableTs(bool enable);

private:
	
	virtual U32 do_run();

	MutexT	secMutex_;
	SectionFilter SecFilterList_[MAX_SECFILTER_COUNT];

	MutexT	tsMutex_;
	SectionFilter TSDataFilterList_[MAX_TSFILTER_COUNT];

	HANDLE	device_;
	TSInf	TsInf_;
	bool	enableTs_;

	void inner_delSectionFilter(utHandle hFilter);
	void inner_delTsFilter(utHandle hFilter);

	static void onSecDataHandler(void *context,unsigned short pid,unsigned char *SecData,unsigned short SecDataLen);
	static void onTSDataHandler(void *context,unsigned short pid,unsigned char *pTSBuffer,unsigned short iTSBuffer);
};

#endif //defined(TSCATCHER_H_)
